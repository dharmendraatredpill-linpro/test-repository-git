package com.dr.alfresco.repo.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import com.dr.alfresco.repo.model.DrModel;
import com.dr.alfresco.repo.utils.ApplicationContextHolder;
import com.dr.alfresco.repo.utils.UpdateMetadataOnChildren;

public class ReplicateMetadataToChildrenPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnUpdatePropertiesPolicy {

  private static final Logger LOG = Logger.getLogger(ReplicateMetadataToChildrenPolicy.class);
  private Behaviour behaviour;
  private static boolean initialized = false;
  protected UpdateMetadataOnChildren updateMetadataOnChildren;

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef childrenNodeRef = childAssocRef.getChildRef();
    inheritParentMetadata(childrenNodeRef);
  }

  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // The policy should be configurable which aspects to react on.
    Properties properties = ApplicationContextHolder.getApplicationContext().getBean("global-properties", Properties.class);
    String aspectReact = properties.getProperty("replicate.metadata.aspect", "documentMetadataUpdatedStatusAspect");
    QName ASPECT_REACT = QName.createQName(DrModel.URI, aspectReact);

    nodeService.addAspect(nodeRef, ASPECT_REACT, null);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Add aspect: " + ASPECT_REACT + " on node " + nodeRef);
    }
  }

  private void inheritParentMetadata(NodeRef childrenNodeRef) {
    if (childrenNodeRef == null || !nodeService.exists(childrenNodeRef)) {
      LOG.debug("Aborting. Node does not exist");
      return;
    }

    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(childrenNodeRef.getStoreRef())) {
      LOG.debug("Aborting. Node is not within workspace store");
      return;
    }

    if (lockService.getLockStatus(childrenNodeRef) != LockStatus.NO_LOCK) {
      LOG.debug("Aborting. Node is locked");
      return;
    }
    ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(childrenNodeRef);
    NodeRef parentNodeRef = childAssociationRef.getParentRef();
    updateMetadataOnChildren.updateMetadataOnChildren(parentNodeRef, childrenNodeRef);
  }

  public void setUpdateMetadataOnChildren(UpdateMetadataOnChildren updateMetadataOnChildren) {
    this.updateMetadataOnChildren = updateMetadataOnChildren;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    if (!initialized) {
      LOG.info("Initialized " + this.getClass().getName());

      behaviour = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, DrModel.TYPE_DR_BASE_CONTENT, behaviour);
      behaviour = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, DrModel.TYPE_DR_BASE_FOLDER, behaviour);

      behaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, DrModel.TYPE_DR_BASE_FOLDER, behaviour);

      initialized = true;
    }
  }

}