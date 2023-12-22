package com.vinfast.car.sap.sw;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.util.PropertyDefines;

public class UpdateSWBOMValidator {
	
	public static String isValidSWBOM(TCComponent swBOM, TCSession session) {
		String err = "";

		String type = swBOM.getType().toString();
		
		if(!type.equals(PropertyDefines.TYPE_BOMLINE)) {
			err += "You must select a BOMLine.\n";
			return err;
		}
		
		TCComponentBOMLine bl = (TCComponentBOMLine) swBOM;
		
		DataManagementService dmCoreService = DataManagementService.getService(session);
		dmCoreService.getProperties(new TCComponent[] {bl}, new String[] {PropertyDefines.BOM_IS_PRECISE});
		try {
			String isPrecise = bl.getProperty(PropertyDefines.BOM_IS_PRECISE);
			if(isPrecise.equals("False")) {
				err += "Software BOM must be precise.\n";
			}
			
			TCComponentBOMLine parent = (TCComponentBOMLine) bl.getReferenceProperty(PropertyDefines.BOM_PARENT);
			if(parent != null) {
				err += "Please select TopLine of SWBOM.\n";
			}
	
		} catch (TCException e) {
			e.printStackTrace();
		}
		return err;
	}
	
	public static String isValidSubGroup(TCComponent subgroup, TCSession session) {
		
		String err = "";
		String type = subgroup.getType().toString();
		
		if(!type.equals(PropertyDefines.TYPE_BOMLINE)) {
			err += "You must select a BOMLine.\n";
			return err;
		}
		
		TCComponentBOMLine bl = (TCComponentBOMLine) subgroup;

		DataManagementService dmCoreService = DataManagementService.getService(session);
		dmCoreService.getProperties(new TCComponent[] {bl}, new String[] {PropertyDefines.BOM_IS_PRECISE});
		try {
			String isPrecise = bl.getProperty(PropertyDefines.BOM_IS_PRECISE);
			if(isPrecise.equals("False")) {
				err += "Subgroup must be precise.\n";
			}
			
			TCComponentBOMLine parent = (TCComponentBOMLine) bl.getReferenceProperty(PropertyDefines.BOM_PARENT);
			if(parent == null) {
				err += "Please select Subgroup of MBOM.\n";
			}
			
		} catch (TCException e) {
			e.printStackTrace();
		}
		return err;
	}
	
}
