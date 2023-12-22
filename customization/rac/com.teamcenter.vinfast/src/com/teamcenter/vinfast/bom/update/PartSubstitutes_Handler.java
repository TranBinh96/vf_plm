package com.teamcenter.vinfast.bom.update;

import java.util.HashSet;
import java.util.Random;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
//import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.vinfast.sc.utilities.PropertyDefines;

public class PartSubstitutes_Handler {
	public static String isValidSubstitutePart(TCComponent item) {
		String err = "";
		String type = item.getType().toString();

		if (!type.equals(PropertyDefines.TYPE_BOMLINE)) {
			err += "You must copy a BOMLine.\n";
			return err;
		}
		TCComponentBOMLine bl = (TCComponentBOMLine) item;

		if (!bl.hasSubstitutes()) {
			err += "Your selected BOMLine doesn't have any substitutes.\n";
			return err;
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

	public static String addMainPartAndSubstitutes(TCComponent partHasSubstitutes, TCComponent parent) {
		String err = "";
		String groupID = "";
		try {
			TCComponentBOMLine partEBOMLine = (TCComponentBOMLine) partHasSubstitutes;
			TCComponentBOMLine parentLine = (TCComponentBOMLine) parent;
			TCComponentBOMLine mainLine = parentLine.addBOMLine(parentLine, partEBOMLine, null);
			groupID = generateGroupID(parentLine);
			mainLine.setProperty(PropertyDefines.BOM_PART_GROUPID, groupID);

			TCComponentBOMLine[] subList = partEBOMLine.listSubstitutes();
			for (TCComponentBOMLine substitute : subList) {
				// copy main part again
				TCComponentBOMLine subLine = parentLine.addBOMLine(parentLine, partEBOMLine, null);
				subLine.setProperty(PropertyDefines.BOM_PART_GROUPID, groupID);
				// remove bom attribute "substitute" of each substitute
				TCComponentBOMLine[] subList2 = subLine.listSubstitutes();
				for (TCComponentBOMLine substitute2 : subList2) {
					substitute2.cut();
				}
				// replace main part with substitute
//				TCComponentBOMView bomview = (TCComponentBOMView) subLine.getRelatedComponent("bl_bomview");
				TCComponentItem subItem = substitute.getItem();
				TCComponentItemRevision subItemRev = substitute.getItemRevision();
				subLine.replace(subItem, subItemRev, null);	
			}
		} catch (TCException e) {
			err = e.toString();
			e.printStackTrace();
		}
		return err;
	}

	public static String removeAllBOMLineInGroup(TCComponent part) {
		String err = "";
		try {
			TCSession session = part.getSession();
			TCComponentBOMLine partLine = (TCComponentBOMLine) part;
			TCComponentBOMLine subGroupLine = partLine.parent();
			String groupId = partLine.getProperty(PropertyDefines.BOM_PART_GROUPID);
			try {
				TcResponseHelper responseHelper = TcBOMService.expand(session, subGroupLine);
				TCComponent[] childrens = responseHelper.getReturnedObjects();
				for (TCComponent children : childrens) {
					if (children.getProperty(PropertyDefines.BOM_PART_GROUPID).equals(groupId)) {
						((TCComponentBOMLine) children).cut();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (TCException e) {
			err = e.toString();
			e.printStackTrace();
		}
		return err;
	}

	private static String generateGroupID(TCComponentBOMLine subgroup) {
		String retId = "";

		TcResponseHelper responseHelper;
		try {
			responseHelper = TcBOMService.expand(subgroup.getSession(), subgroup);
			TCComponent[] childrens = responseHelper.getReturnedObjects();
			HashSet<String> existedID = new HashSet<String>();
			for (TCComponent children : childrens) {
				existedID.add(children.getProperty(PropertyDefines.BOM_PART_GROUPID));
			}

			Random random = new Random();
			String generatedString = "";
			int leftLimit = 48; // numeral '0'
			int rightLimit = 122; // letter 'z'
			int targetStringLength = 2;
			do {
				generatedString = random.ints(leftLimit, rightLimit + 1)
						.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
						.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			} while (existedID.contains(generatedString));
			retId = generatedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retId;
	}
}
