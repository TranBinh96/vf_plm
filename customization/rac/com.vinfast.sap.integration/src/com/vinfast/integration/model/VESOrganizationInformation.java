package com.vinfast.integration.model;

import com.vinfast.sap.util.PropertyDefines;

public class VESOrganizationInformation extends OrganizationInformationAbstract{
	String serverType = null;
	public VESOrganizationInformation(String serverType) {
		super(ORG_TYPE.ORG_VES);
		if(serverType.equals(PropertyDefines.SERVER_PRODUCTION)) {
			auth = PropertyDefines.SERVER_SAP_VES_PRD_AUTH;
		}else {
			auth = PropertyDefines.SERVER_SAP_VES_NON_PRD_AUTH;
		}
		
		this.serverType = serverType;
	}
	
	@Override
	public String getAuth() {
		return auth;
	}

	@Override
	public String getServerIP() {
		switch(this.serverType){
			case PropertyDefines.SERVER_PRODUCTION:
				return PropertyDefines.SERVER_SAP_VES_PROD;
			case PropertyDefines.SERVER_QA:
				return PropertyDefines.SERVER_SAP_VES_QA;
			case PropertyDefines.SERVER_DEV:
				return PropertyDefines.SERVER_SAP_VES_DEV;
			default:
				return null;
				
		}
	}

}
