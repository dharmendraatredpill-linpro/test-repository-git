package com.dr.alfresco.repo.policy;

import java.util.Map;

import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.redpill.alfresco.numbering.component.NumberingComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.dr.alfresco.repo.model.DrModel;

public class DocumentNumberPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnMoveNodePolicy, OnUpdateNodePolicy, OnCopyCompletePolicy, InitializingBean {

  private static final Logger LOG = Logger.getLogger(DocumentNumberPolicy.class);
  private Behaviour behaviour;
  private static boolean initialized = false;
  private NumberingComponent numberingComponent;

  protected void setDocumentNumber(NodeRef nodeRef) {
    setDocumentNumber(nodeRef, false);
  }

  protected void setDocumentNumber(NodeRef nodeRef, boolean forceNewNumber) {
    boolean enabled = behaviourFilter.isEnabled(nodeRef);
    if (enabled) {
      behaviourFilter.disableBehaviour(nodeRef);
    }
    String docNumber = (String) nodeService.getProperty(nodeRef, DrModel.PROP_DOCUMENT_ID);
    if (docNumber == null || !StringUtils.hasText(docNumber) || forceNewNumber) {
      String decoratedNextNumber = numberingComponent.getDecoratedNextNumber(nodeRef);
      nodeService.setProperty(nodeRef, DrModel.PROP_DOCUMENT_ID, decoratedNextNumber);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting document number for " + decoratedNextNumber + " for node " + nodeRef.toString());
      }
    }
    if (enabled) {
      behaviourFilter.enableBehaviour(nodeRef);
    }
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode begin - " + childAssocRef.getParentRef() + "->" + childAssocRef.getChildRef());
    }

    final NodeRef nodeRef = childAssocRef.getChildRef();

    setDocumentNumber(nodeRef);

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode end");
    }
  }

  @Override
  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCopyComplete end");
    }

    final NodeRef nodeRef = targetNodeRef;

    setDocumentNumber(nodeRef, true);

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCopyComplete end");
    }
  }

  @Override
  public void onUpdateNode(NodeRef nodeRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onUpdateNode start");
    }
    setDocumentNumber(nodeRef);

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onUpdateNode end");
    }
  }

  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onMoveNode begin - " + newChildAssocRef.getParentRef() + "->" + newChildAssocRef.getChildRef());
    }

    final NodeRef nodeRef = newChildAssocRef.getChildRef();

    setDocumentNumber(nodeRef);

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onMoveNode end");
    }
  }

  public void setNumberingComponent(NumberingComponent numberingComponent) {
    this.numberingComponent = numberingComponent;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    Assert.notNull(numberingComponent);
    if (!initialized) {
      LOG.info("Initialized " + this.getClass().getName());

      behaviour = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, DrModel.TYPE_DR_BASE_CONTENT, behaviour);

      behaviour = new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, DrModel.ASPECT_DR_BASE_CONTENT, behaviour);

      behaviour = new JavaBehaviour(this, "onCopyComplete", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, DrModel.ASPECT_DR_BASE_CONTENT, behaviour);

      behaviour = new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
      policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, DrModel.ASPECT_DR_BASE_CONTENT, behaviour);

      initialized = true;
    }
  }
}
