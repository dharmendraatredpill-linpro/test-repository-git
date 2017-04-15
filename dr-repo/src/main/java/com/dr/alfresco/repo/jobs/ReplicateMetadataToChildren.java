package com.dr.alfresco.repo.jobs;

import java.util.List;
import java.util.Properties;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.dr.alfresco.repo.model.DrModel;
import com.dr.alfresco.repo.utils.ApplicationContextHolder;
import com.dr.alfresco.repo.utils.UpdateMetadataOnChildren;

public class ReplicateMetadataToChildren implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(ReplicateMetadataToChildren.class);
  protected NodeService nodeService;
  protected SearchService searchService;
  protected UpdateMetadataOnChildren updateMetadataOnChildren;
  protected BehaviourFilter behaviourFilter;

  protected RetryingTransactionHelper retryingTransactionHelper;

  /**
   * Executer implementation
   */
  public void execute() {
    LOG.info("Running the scheduled job");

    // Find all document with the aspect: dr:documentMetadataUpdatedStatusAspect
    ResultSet folders = findUpdatedFolders();

    List<NodeRef> nodeRefs = folders.getNodeRefs();
    for (NodeRef parentFolder : nodeRefs) {
      updateChildren(parentFolder, parentFolder);

      nodeService.removeAspect(parentFolder, DrModel.ASPECT_DOCUMENT_METADATA_UDATED_STATUS);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Remove aspect: " + DrModel.ASPECT_DOCUMENT_METADATA_UDATED_STATUS.getLocalName() + " on node " + parentFolder);
      }
    }
  }

  private void updateChildren(NodeRef parentFolder, NodeRef childItem) {
    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(childItem);
    for (ChildAssociationRef child : childAssocs) {
      NodeRef childNr = child.getChildRef();
      if (nodeService.getType(childNr).equals(DrModel.TYPE_DR_BASE_FOLDER) || nodeService.getType(childNr).equals(DrModel.TYPE_CUSTOMER_FOLDER)
          || nodeService.getType(childNr).equals(DrModel.TYPE_PROJECT_FOLDER) || nodeService.getType(childNr).equals(DrModel.TYPE_NAVIGATION_BOARD_FOLDER)
          || nodeService.getType(childNr).equals(DrModel.TYPE_OPPORTUNITY_FOLDER) || nodeService.getType(childNr).equals(DrModel.TYPE_PROJECT_ESTIMATION_FOLDER)) {
        Properties properties = ApplicationContextHolder.getApplicationContext().getBean("global-properties", Properties.class);
        String aspectReact = properties.getProperty("replicate.metadata.aspect", "documentMetadataUpdatedStatusAspect");
        QName ASPECT_REACT = QName.createQName(DrModel.URI, aspectReact);
        nodeService.addAspect(childNr, ASPECT_REACT, null);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Add aspect: " + ASPECT_REACT + " on node " + childNr);
        }
      }
      updateMetadataOnChildren.updateMetadataOnChildren(parentFolder, childNr);
    }
  }

  private ResultSet findUpdatedFolders() {

    StringBuffer query = new StringBuffer();

    query.append("(TYPE:\"dr:drBaseFolder\" OR TYPE:\"dr:customerFolder\" OR TYPE:\"dr:projectFolder\" OR TYPE:\"dr:opportunityFolder\" OR TYPE:\"dr:navigationBoardFolder\" OR TYPE:\"dr:projectEstimationFolder\") AND ");
    query.append("ASPECT:\"dr:documentMetadataUpdatedStatusAspect\"  ");

    SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery(query.toString());

    ResultSet result = searchService.query(searchParameters);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Documents found for query: " + query.toString());
      LOG.debug("Count: " + result.length());
      LOG.debug("");
    }

    return result;
  }

  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public void setUpdateMetadataOnChildren(UpdateMetadataOnChildren updateMetadataOnChildren) {
    this.updateMetadataOnChildren = updateMetadataOnChildren;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    this.behaviourFilter = behaviourFilter;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService);
    Assert.notNull(behaviourFilter);
    Assert.notNull(searchService);
    Assert.notNull(searchService);

  }
}
