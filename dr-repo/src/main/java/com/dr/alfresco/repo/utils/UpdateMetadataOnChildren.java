package com.dr.alfresco.repo.utils;

import org.alfresco.service.cmr.repository.NodeRef;

public interface UpdateMetadataOnChildren {
	
	public void updateMetadataOnChildren(NodeRef parentFolder, NodeRef nodeRef);
}
