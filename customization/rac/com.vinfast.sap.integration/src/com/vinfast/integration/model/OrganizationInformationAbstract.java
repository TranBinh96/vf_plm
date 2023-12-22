package com.vinfast.integration.model;

public abstract class OrganizationInformationAbstract {

	public enum ORG_TYPE {
		ORG_VES, ORG_VINFAST, ORG_VFE;
	}

	public OrganizationInformationAbstract(ORG_TYPE orgType) {
		this.orgType = orgType;
	}

	public abstract String getAuth();

	public abstract String getServerIP();

	public ORG_TYPE getOrgType() {
		return orgType;
	}

	protected String auth;
	protected String serverIP;
	protected ORG_TYPE orgType;
}
