package com.vinfast.integration.model;

import com.vinfast.sap.util.PropertyDefines;

public class VFEOrganizationInformation extends OrganizationInformationAbstract{
	String serverType = null;
	public VFEOrganizationInformation(String serverType) {
		super(ORG_TYPE.ORG_VFE);
		auth = PropertyDefines.SERVER_SAP_VF_AUTH;
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
			return PropertyDefines.SERVER_SAP_VF_PROD;
		case PropertyDefines.SERVER_QA:
			return PropertyDefines.SERVER_SAP_VF_QA;
		case PropertyDefines.SERVER_DEV:
			return PropertyDefines.SERVER_SAP_VF_DEV;
		default:
			return null;			
		}
	}

}
