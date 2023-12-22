package com.vinfast.integration.model;

import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.sap.util.PropertyDefines;

public class OrganizationInformationFactory {

	public static OrganizationInformationAbstract generateOrganizationInformation(String org, String serverType, TCSession session) {
		if (org.contains(PropertyDefines.VIN_ES)) {
			return new VESOrganizationInformation(serverType);
		} else if (org.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
			return new VFEOrganizationInformation(serverType);
		} else {
			return new VFOrganizationInformation(serverType);
		}
	}
}
