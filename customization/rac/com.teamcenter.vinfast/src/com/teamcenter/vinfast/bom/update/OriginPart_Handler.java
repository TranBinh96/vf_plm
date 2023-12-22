package com.teamcenter.vinfast.bom.update;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sc.utilities.PropertyDefines;
import com.vinfast.sc.utilities.Utilities;

public class OriginPart_Handler {

	public static String isValidFlexPart(TCComponent item) {
		String err = "";
		String type = item.getType().toString();

		if (!type.equals(PropertyDefines.TYPE_BOMLINE)) {
			err += "You must copy a BOMLine.\n";
			return err;
		}
		TCComponentBOMLine bl = (TCComponentBOMLine) item;
		TCComponentItemRevision rev;
		try {
			rev = bl.getItemRevision();
			if (!rev.getType().equals(PropertyDefines.TYPE_FLEXIBLE_PART)) {
				err += "You must select a flexible part.\n";
				return err;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return err;
	}

	public static String isValidSubgroup(TCComponent subgroup) {

		String err = "";
		String type = subgroup.getType().toString();

		if (!type.equals(PropertyDefines.TYPE_BOMLINE)) {
			err += "You must select a BOMLine.\n";
			return err;
		}

		TCComponentBOMLine bl = (TCComponentBOMLine) subgroup;
		try {
			TCComponentItemRevision rev = bl.getItemRevision();
			if (!rev.getType().equals(PropertyDefines.TYPE_VF_ITEM_REVISION)
					&& !rev.getType().equals(PropertyDefines.TYPE_STR_PART_REVISION)) {
				err += "Subgroup must be VF Item or Structure Part.\n";
				return err;
			}

			TCComponentBOMLine parent = (TCComponentBOMLine) bl.getReferenceProperty(PropertyDefines.BOM_PARENT);
			if (parent == null) {
				err += "Please select Subgroup of MBOM.\n";
			}

		} catch (TCException e) {
			e.printStackTrace();
		}
		return err;
	}

	public static String addBOMline(TCComponent flexPart, TCComponent parent) {
		String err = "";
		try {
			TCSession session = parent.getSession();
			TCComponentBOMLine flexLine = (TCComponentBOMLine) flexPart;
			TCComponentBOMLine parentLine = (TCComponentBOMLine) parent;

			// get origin part
			TCComponentItem flexItem = flexLine.getItem();
			DataManagementService dmCoreService = DataManagementService.getService(session);
			dmCoreService.getProperties(new TCComponent[] { flexItem },
					new String[] { PropertyDefines.ITEM_ID, "VF4_original_purchased_part" });
			TCComponent[] originparts = Utilities.getRelatedComponents(dmCoreService, flexItem, new String[] {},
					"VF4_original_purchased_part");
			if (originparts == null || originparts.length == 0) {
				err = "Your Flex part must link to an origin part.\n";
				return err;
			}
			TCComponentItem originpart = (TCComponentItem) originparts[0];

			// add flex line
			TCComponentBOMLine newBOMLine = parentLine.addBOMLine(parentLine, flexLine, null);
			// overide flex line with origin part
			//TCComponentBOMView bomview = (TCComponentBOMView) newBOMLine.getRelatedComponent("bl_bomview");
			newBOMLine.replace(originpart, originpart.getLatestItemRevision(), null);
			
//			System.out.println(newBOMLine.getProperty(PropertyDefines.BOM_ITEM_ID));
//			System.out.println(newBOMLine.getProperty(PropertyDefines.BOM_VF3_PUR_LEVEL));
//			System.out.println(newBOMLine.getProperty(PropertyDefines.BOM_VL5_PUR_LEVEL));
//			System.out.println(newBOMLine.getProperty(PropertyDefines.BOM_QUANTITY));
//			System.out.println(newBOMLine.getProperty(PropertyDefines.BOM_MANUF_CODE));
		} catch (TCException e) {
			err = "Some exceptions occur, please contact admin for support\n";
			e.printStackTrace();
		}
		return err;
	}
}
