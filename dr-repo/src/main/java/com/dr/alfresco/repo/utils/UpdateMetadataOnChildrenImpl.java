package com.dr.alfresco.repo.utils;

import java.util.Properties;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class UpdateMetadataOnChildrenImpl implements UpdateMetadataOnChildren, InitializingBean {

  private static final Logger LOG = Logger.getLogger(UpdateMetadataOnChildrenImpl.class);
  protected NodeService nodeService;

  public void updateMetadataOnChildren(NodeRef parentFolder, NodeRef childrenNodeRef) {
    if (LOG.isDebugEnabled())
      LOG.debug("Update metadata on children");
    Properties properties = ApplicationContextHolder.getApplicationContext().getBean("global-properties", Properties.class);
    String allValuesInGlobalProperties = properties.getProperty("update.metadata.on.children.properties", "nameOfCustomer");
    String[] splittedValues = allValuesInGlobalProperties.split(",");
    if (LOG.isDebugEnabled())
      LOG.debug("All properties in alfresco-global.properties: " + allValuesInGlobalProperties);

    // Set the parent folders metadata on children
    for (int i = 0; i < splittedValues.length; i++) {
      String property = splittedValues[i];
      setPropertyOnChild(childrenNodeRef, parentFolder, property);
    }
  }

	private void setPropertyOnChild(NodeRef nodeRef, NodeRef parentNodeRef, String property) {
		QName PROPERTY = QName.createQName("http://www.digitalroute.com/model/content/1.0", property);
		String parentProperty = (String) nodeService.getProperty(parentNodeRef, PROPERTY);
		if (parentProperty != null) {
			nodeService.setProperty(nodeRef, PROPERTY, parentProperty);
			if (LOG.isDebugEnabled())
				LOG.debug("Update " + nodeRef + " with metadata " + PROPERTY.getLocalName() + ": " + parentProperty	+ " from " + parentNodeRef);
		}
	}

  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService);
  }
}
