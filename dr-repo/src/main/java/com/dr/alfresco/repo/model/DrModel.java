package com.dr.alfresco.repo.model;

import org.alfresco.service.namespace.QName;

public interface DrModel {

	static final String URI = "http://www.digitalroute.com/model/content/1.0";
	static final String PREFIX = "dr";

	/**
	 * DR Folder Types
	 */
	static final QName TYPE_DR_BASE_FOLDER = QName.createQName(URI, "drBaseFolder");
	static final QName TYPE_CUSTOMER_FOLDER = QName.createQName(URI, "customerFolder");
	static final QName TYPE_PROJECT_FOLDER = QName.createQName(URI, "projectFolder");
	static final QName TYPE_OPPORTUNITY_FOLDER = QName.createQName(URI, "opportunityFolder");
	static final QName TYPE_PROJECT_ESTIMATION_FOLDER = QName.createQName(URI, "projectEstimationFolder");
	static final QName TYPE_NAVIGATION_BOARD_FOLDER = QName.createQName(URI, "navigationBoardFolder");

	/**  
	 * DR Content Types
	 */
	static final QName TYPE_DR_BASE_CONTENT = QName.createQName(URI, "drBaseContent");
	static final QName TYPE_CUSTOMER_AGREEMENT = QName.createQName(URI, "customerAgreement");
	static final QName TYPE_GENERAL_DOCUMENT = QName.createQName(URI, "generalDocument");
	static final QName TYPE_COMPANY_INFORMATION = QName.createQName(URI, "companyInformation");
	static final QName TYPE_NAVIGATION_BOARD = QName.createQName(URI, "navigationBoard");
	static final QName TYPE_MZ_DOCUMENTATION = QName.createQName(URI, "mzDocumentation");
	static final QName TYPE_PROPOSAL = QName.createQName(URI, "proposal");
	static final QName TYPE_PURCHASE_ORDER = QName.createQName(URI, "purchaseOrder");
	static final QName TYPE_RFX = QName.createQName(URI, "RFX");
	static final QName TYPE_SDR = QName.createQName(URI, "SDR");
	static final QName TYPE_ARCHIVE = QName.createQName(URI, "archive");

	static final QName ASPECT_DR_BASE_CONTENT = QName.createQName(URI, "drBaseContentAspect");
	static final QName PROP_DOCUMENT_ID = QName.createQName(URI, "documentId");
	static final QName PROP_WF = QName.createQName(URI, "wf");
	static final QName PROP_WF_STATUS = QName.createQName(URI, "wfStatus");
	static final QName PROP_DOCUMENT_STATUS = QName.createQName(URI, "documentStatus");
	static final QName PROP_SECURITY_CLASSIFICATION_OPTIONS = QName.createQName(URI, "secClassificationOptions");

	static final QName ASPECT_CUSTOMER_AGREEMENT_SUB_TYPE = QName.createQName(URI, "customerAgreementSubTypeAspect");
	static final QName PROP_CUSTOMER_AGREEMENT_SUB_TYPE = QName.createQName(URI, "customerAgreementSubType");

	static final QName ASPECT_CUSTOMER_AGREEMENT = QName.createQName(URI, "customerAgreementAspect");
	static final QName PROP_TOTAL_INSURANCE_LIABILITY = QName.createQName(URI, "totalInsuranceLiability");
	static final QName PROP_GOVERNING_LAW = QName.createQName(URI, "governingLaw");

	static final QName ASPECT_GENERAL_DOCUMENT_SUBTYPE = QName.createQName(URI, "generalDocumentSubTypeAspect");
	static final QName PROP_GENERAL_DOCUMENT_SUB_TYPE = QName.createQName(URI, "generalDocumentSubType");

	static final QName ASPECT_NAVIGATION_BOARD_SUB_TYPE = QName.createQName(URI, "navigationBoardSubTypeAspect");
	static final QName PROP_NAVIGATION_BOARD_SUB_TYPE = QName.createQName(URI, "navigationBoardSubType");

	static final QName ASPECT_MZ_DOCUMENTATION_SUB_TYPE = QName.createQName(URI, "mzDocumentationSubTypeAspect");
	static final QName PROP_MZ_DOCUMENTATION_SUB_TYPE = QName.createQName(URI, "mzDocumentationSubType");

	static final QName ASPECT_END_CUSTOMER = QName.createQName(URI, "endCustomerAspect");
	static final QName PROP_END_CUSTOMER = QName.createQName(URI, "endCustomer");

	static final QName ASPECT_NOTICE = QName.createQName(URI, "noticeAspect");
	static final QName PROP_NOTICE_DATE = QName.createQName(URI, "noticeDate");

	static final QName ASPECT_SOFTWARE = QName.createQName(URI, "softwareAspect");
	static final QName PROP_SOFTWARE_NAME = QName.createQName(URI, "softwareName");
	static final QName PROP_RELEASE_VERSION = QName.createQName(URI, "releaseVersion");

	static final QName ASPECT_CONTRACT_VALID = QName.createQName(URI, "contractValidAspect");
	static final QName PROP_VALID_FROM = QName.createQName(URI, "validFrom");
	static final QName PROP_TERMINATION_DATE = QName.createQName(URI, "terminationDate");

	static final QName ASPECT_PURCHASE_ORDER = QName.createQName(URI, "purchaseOrderAspect");
	static final QName PROP_PONUMBER = QName.createQName(URI, "poNumber");

	static final QName ASPECT_SIGNED = QName.createQName(URI, "signedAspect");
	static final QName PROP_SIGED = QName.createQName(URI, "signed");

	static final QName ASPECT_ARCHIVE_SUB_TYPE = QName.createQName(URI, "archiveSubTypeAspect");
	static final QName PROP_ARCHIVE_SUB_TYPE = QName.createQName(URI, "archiveSubType");

	static final QName ASPECT_CUSTOMER_FOLDER = QName.createQName(URI, "customerFolderAspect");
	static final QName PROP_NAME_OF_CUSTOMER = QName.createQName(URI, "nameOfCustomer");
	static final QName PROP_CUSTOMER_NUMBER = QName.createQName(URI, "customerNumber");
	static final QName PROP_REGION = QName.createQName(URI, "region");

	static final QName ASPECT_PROJECT_FOLDER = QName.createQName(URI, "projectFolderAspect");
	static final QName PROP_PROJECT_NUMBER = QName.createQName(URI, "projectNumber");
	static final QName PROP_PROJECT_NAME = QName.createQName(URI, "projectName");

	static final QName ASPECT_OPPORTUNITY_FOLDER = QName.createQName(URI, "opportunityFolderAspect");
	static final QName PROP_OPPORTUNITY_ID = QName.createQName(URI, "opportunityID");
	static final QName PROP_OPPORTUNITY_NAME = QName.createQName(URI, "opportunityName");

	static final QName ASPECT_DOCUMENT_METADATA_UDATED_STATUS = QName.createQName(URI,
			"documentMetadataUpdatedStatusAspect");
	static final QName PROP_STATUS = QName.createQName(URI, "status");

}