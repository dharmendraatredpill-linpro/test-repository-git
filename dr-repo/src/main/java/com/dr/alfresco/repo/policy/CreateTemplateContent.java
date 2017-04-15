package com.dr.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.dr.alfresco.repo.model.DrModel;
import com.dr.alfresco.repo.utils.ApplicationContextHolder;

public class CreateTemplateContent extends AbstractPolicy
		implements InitializingBean, NodeServicePolicies.OnCreateNodePolicy {
	private String templateDirectory;
	private SearchService searchService;
	private NamespaceService namespaceService;
	private SiteService siteService;
	private FileFolderService fileFolderService;

	private Logger LOG = Logger.getLogger(CreateTemplateContent.class);

	@Override
	public void afterPropertiesSet() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,	new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, DrModel.TYPE_DR_BASE_FOLDER,new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef nodeRef = childAssocRef.getChildRef();

		if (nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) == null) {
			try {
				List<NodeRef> rs = searchService.selectNodes(
						nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), templatePath(nodeRef), null,
						namespaceService, false);
				NodeRef template = rs.get(0);
				List<FileInfo> listFiles = fileFolderService.list(template);
				String folderName = null;
				for (FileInfo file : listFiles) {
					NodeRef nodRefTemplateFiles = file.getNodeRef();
					folderName = (String) nodeService.getProperty(nodRefTemplateFiles, ContentModel.PROP_NAME);
					fileFolderService.copy(nodRefTemplateFiles, nodeRef, folderName);
				}

			} catch (Exception e) {
				LOG.error("Failed to add template to content of node: " + nodeRef, e);
			}
		} else if (nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) != null && allowUpdate(nodeRef)) {
			changeType(nodeRef);
		}

	}

	private boolean changeType(NodeRef nodeRef) {

		if (allowChangeType(nodeRef)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Changing type of content to " + DrModel.TYPE_DR_BASE_CONTENT + ": " + nodeRef);
			}
			nodeService.setType(nodeRef, DrModel.TYPE_DR_BASE_CONTENT);
			return true;
		} else {
			return false;
		}
	}

	private boolean allowUpdate(final NodeRef nodeRef) {
		// Do not update nodes outside the workspace spacesstore
		if (!nodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Tried to set document metadata on node (" + nodeRef + ") in store " + nodeRef.getStoreRef()
						+ " which is ignored.");
			}
			return false;
		}

		if (!nodeService.exists(nodeRef)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Node does not exist. Skipping...");
			}
			return false;
		}

		QName nodeType = nodeService.getType(nodeRef);
		if (ContentModel.TYPE_THUMBNAIL.equals(nodeType)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Node type is cm:thumbnail. Skipping...");
			}
			return false;
		}

		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Node is working copy. Skipping...");
			}
			return false;
		}

		try {

			// Check that the node is not locked by another user
			lockService.checkForLock(nodeRef);

			// Find out if a node is within the document library. An exception
			// will be
			// thrown if it is not within the document library.
		} catch (final NodeLockedException nle) {
			LOG.debug("Tried to set document metadata on locked node: " + nodeRef + " which is ignored.", nle);
			return false;
		} catch (Exception e) {
			LOG.warn("Unhandled exception in allowUpdate()", e);
			return false;
		}
		return true;
	}

	private boolean allowChangeType(NodeRef nodeRef) {
		return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>() {
			@Override
			public Boolean doWork() throws Exception {
				try {
					SiteInfo site = siteService.getSite(nodeRef);
					if (site != null) {
						// File is within a site
						NodeRef container = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
						fileFolderService.getNameOnlyPath(container, nodeRef);
						return true;
					} else {
						// File in shared or user home
						List<String> path = fileFolderService.getNameOnlyPath(null, nodeRef);
						if (path.contains("Company Home") && (path.contains("User Homes") || path.contains("Shared"))) {
							return true;
						}
					}
					return false;
				} catch (FileNotFoundException fnfe) {
					LOG.debug("Tried to set document metadata on node outside of document library: " + nodeRef, fnfe);
					return false;
				}
			}
		});
	}

	private String templatePath(NodeRef nodeRef) {
		return String.format("%s/%s", templateDirectory, templateName(nodeRef));
	}

	private class NoMatchingTemplate extends RuntimeException {
		private static final long serialVersionUID = 1L;

		NoMatchingTemplate(String type) {
			super("Could not find matching template for type: " + type);
		}
	}

	private String templateName(NodeRef nodeRef) {

		String templateName = null;
		Properties properties = ApplicationContextHolder.getApplicationContext().getBean("global-properties",
				Properties.class);
		String allValuesInGlobalProperties = properties.getProperty("dr.template.folder.list");

		if (LOG.isDebugEnabled())
			LOG.debug("All properties in alfresco-global.properties: " + allValuesInGlobalProperties);

		if (allValuesInGlobalProperties != null && !StringUtils.isEmpty(allValuesInGlobalProperties)) {

			List<String> allValuesInGlobalPropertiesList = new ArrayList<String>(
					Arrays.asList(allValuesInGlobalProperties.split(",")));

			for (int i = 0; i < allValuesInGlobalPropertiesList.size(); i++)
				if (allValuesInGlobalPropertiesList.get(i)
						.equalsIgnoreCase(nodeService.getType(nodeRef).getLocalName())) {
					if (allValuesInGlobalPropertiesList.size() > (i + 1)) {
						templateName = allValuesInGlobalPropertiesList.get(i + 1);
						break;
					}
				}
		}
		if (templateName == null) {
			throw new NoMatchingTemplate(nodeService.getType(nodeRef).getLocalName());

		}

		return templateName;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

}