package com.dr.alfresco.repo.policy;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import com.dr.alfresco.repo.model.DrModel;
import com.dr.alfresco.repo.utils.UpdateMetadataOnChildren;

public class ReplicateMetadataToChildrenPolicyTest {

  ReplicateMetadataToChildrenPolicy impl;

  private static final Logger LOG = Logger.getLogger(ReplicateMetadataToChildrenPolicyTest.class);
  private Behaviour behaviour;
  private static boolean initialized = false;
  protected UpdateMetadataOnChildren updateMetadataOnChildren;

  private Mockery m;
  protected NodeService nodeService;
  protected PermissionService permissionService;
  protected PolicyComponent policyComponent;
  protected BehaviourFilter behaviourFilter;
  protected LockService lockService;
  final NodeRef parentNodeRef = new NodeRef("workspace://SpacesStore/11111111-20f0-46f4-a768-d6bbeafb0411");
  final NodeRef childrenNodeRef = new NodeRef("workspace://SpacesStore/22222222-20f0-46f4-a768-d6bbeafb0480");

  @Before
  public void setup() throws Exception {
    m = new JUnit4Mockery();
    nodeService = m.mock(NodeService.class);
    lockService = m.mock(LockService.class);
    updateMetadataOnChildren = m.mock(UpdateMetadataOnChildren.class);
    
    impl = new ReplicateMetadataToChildrenPolicy();
    impl.setNodeService(nodeService);
    impl.setLockService(lockService);
    impl.setUpdateMetadataOnChildren(updateMetadataOnChildren);

  }

  @Test
  public void testOnCreateNode() {
    /*
    String nameParent = "12345";
    String nameChild = "11111";
    
    Map<QName, Serializable> parentProperties = new HashMap<QName, Serializable>();
    parentProperties.put(DrModel.PROP_NAME_OF_CUSTOMER, nameParent);
   
    nodeService.addProperties(parentNodeRef, parentProperties);
    
    Map<QName, Serializable> childProperties = new HashMap<QName, Serializable>();
    childProperties.put(DrModel.PROP_NAME_OF_CUSTOMER, nameChild);
    nodeService.addProperties(childrenNodeRef, childProperties);
    
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(childrenNodeRef);
        will(returnValue(true));

        // oneOf(StoreRef).STORE_REF_WORKSPACE_SPACESSTORE.equals(childrenNodeRef.getStoreRef());
        // will(returnValue(true));

        oneOf(lockService).getLockStatus(childrenNodeRef);
        will(returnValue(true));

        oneOf(nodeService).getPrimaryParent(childrenNodeRef);
        will(returnValue(parentNodeRef));

        oneOf(updateMetadataOnChildren).updateMetadataOnChildren(parentNodeRef, childrenNodeRef);
        
      }
    });

    assertEquals("Children node should inherit parent metadata", nodeService.getProperty(parentNodeRef, DrModel.PROP_NAME_OF_CUSTOMER),
        nodeService.getProperty(childrenNodeRef, DrModel.PROP_NAME_OF_CUSTOMER));
    m.assertIsSatisfied();
*/
  }
}