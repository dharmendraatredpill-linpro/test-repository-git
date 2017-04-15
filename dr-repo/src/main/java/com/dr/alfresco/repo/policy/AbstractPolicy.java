package com.dr.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class AbstractPolicy implements InitializingBean {

  private final static Logger LOG = Logger.getLogger(AbstractPolicy.class);

  protected NodeService nodeService;

  protected PermissionService permissionService;

  protected PolicyComponent policyComponent;

  protected BehaviourFilter behaviourFilter;

  protected LockService lockService;

  public void setPolicyComponent(final PolicyComponent policyComponent) {
    this.policyComponent = policyComponent;
  }

  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setPermissionService(final PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    this.behaviourFilter = behaviourFilter;
  }

  public void setLockService(final LockService lockService) {
    this.lockService = lockService;
  }

  @Override
  public void afterPropertiesSet() {
    Assert.notNull(nodeService);
    Assert.notNull(permissionService);
    Assert.notNull(policyComponent);
    Assert.notNull(behaviourFilter);
    Assert.notNull(lockService, "You must provide an instance of the LockService.");
  }

  protected boolean shouldSkipPolicy(final NodeRef nodeRef) {
    // if the node does not exist, exit
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      return true;
    }

    // don't do this for working copies
    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return true;
    }

    // if it's not the spaces store, exit
    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return true;
    }

    // if it's anything but locked, don't do anything
    if (lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK) {
      return true;
    }

    return false;
  }

  public void runSafe(final RunSafe runSafe) {
    List<QName> classNames = runSafe.getClassNames();

    if (classNames == null) {
      // Disable all behaviours
      behaviourFilter.disableBehaviour(runSafe.getNodeRef());
    } else {
      // disable behaviours for the list of classnames for this node
      for (QName className : classNames) {
        behaviourFilter.disableBehaviour(runSafe.getNodeRef(), className);
      }
    }
  }

}

interface RunSafe {

  NodeRef getNodeRef();

  String getUser();

  List<QName> getClassNames();

  void execute();

}

abstract class DefaultRunSafe implements RunSafe {

  private NodeRef nodeRef;

  private String user;

  private List<QName> classNames;

  public DefaultRunSafe(NodeRef nodeRef) {
    this(nodeRef, null);
  }

  public DefaultRunSafe(NodeRef nodeRef, String user) {
    this(nodeRef, user, null);
  }

  public DefaultRunSafe(NodeRef nodeRef, String user, List<QName> classNames) {
    nodeRef = nodeRef;
    user = user;
    classNames = classNames;

    if (classNames == null) {
      classNames = new ArrayList<QName>();
      classNames.add(ContentModel.ASPECT_AUDITABLE);
      classNames.add(ContentModel.ASPECT_VERSIONABLE);
    }
  }

  @Override
  public NodeRef getNodeRef() {
    return nodeRef;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public List<QName> getClassNames() {
    return classNames;
  }

}

abstract class DisableAllBehavioursRunSafe implements RunSafe {

  private NodeRef nodeRef;

  private String user;

  private List<QName> classNames;

  public DisableAllBehavioursRunSafe(NodeRef nodeRef) {
    this(nodeRef, null);
  }

  public DisableAllBehavioursRunSafe(NodeRef nodeRef, String user) {
    this(nodeRef, user, null);
  }

  public DisableAllBehavioursRunSafe(NodeRef nodeRef, String user, List<QName> classNames) {
    nodeRef = nodeRef;
    user = user;
    classNames = classNames;

  }

  @Override
  public NodeRef getNodeRef() {
    return nodeRef;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public List<QName> getClassNames() {
    return classNames;
  }

}
