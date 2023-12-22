package com.vinfast.sap.material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.RevisionRuleEntry;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

/**
 * @author RafiMs
 *
 */
public class MaterialTransferValidate {
	public MaterialTransferValidate() {

	}

	public boolean validateType(TCComponent Revision) {
		if (Revision instanceof TCComponentChangeItemRevision && Revision.getType().equals("VF4_MCNRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentCfg0ProductItem && Revision.getType().equals("Cfg0ProductItem")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && Revision.getType().equals("VF3_manuf_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && Revision.getType().equals("Vf8_AcarPartRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && Revision.getType().equals("VF3_car_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && Revision.getType().equals("VF3_Scooter_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && Revision.getType().equals("VF3_me_scooterRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentCfg0ProductItem && Revision.getType().equals("Cfg0ProductItem")) {
			return true;
		}
		if (Revision instanceof TCComponentCfg0ProductItem && Revision.getType().equals("Mfg0MEProcStatnRevision")) {
			return true;
		}
		return false;
	}

	public boolean validateMaterial(TCComponent Revision) {
		String material_Type = Revision.getType();
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF3_manuf_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("Vf8_AcarPartRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF3_car_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF3_Scooter_partRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF3_me_scooterRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF3_serviceRevision")) {
			return true;
		}
		if (Revision instanceof TCComponentItemRevision && material_Type.equals("VF7_service_chptRevision")) {
			return true;
		}
		return false;
	}

	public boolean checkMandatoryValueFilled(TCComponent tcComp, String propertyName) {
		try {
			if (!tcComp.getProperty(propertyName).equals("")) {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * @author RafiMs
	 * @param session
	 * @param status_name
	 * @return
	 */
	public static ArrayList<String> getRevisionRuleStatusEntries(TCSession session, String status_name) {
		ArrayList<String> valid_status_list = null;
		TCComponentRevisionRule revision_rule = UIGetValuesUtility.getRevisionRule(PropertyDefines.REVISION_RULE_RELEASE, session);
		try {
			RevisionRuleEntry[] entries = revision_rule.getEntries();
			if (entries != null) {
				valid_status_list = new ArrayList<String>();
				for (int i = 0; i < entries.length; i++) {
					valid_status_list.add(entries[i].getTCComponent().getProperty("status_type"));
				}
			}

			String currentRole = session.getRole().getRoleName();
			boolean isMoldEngineer = currentRole.contains("Mold Engineer");
			if (isMoldEngineer)
				valid_status_list.add("VF Released");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valid_status_list;
	}

	public static boolean isValidReleaseStatus(ArrayList<String> valid_status_list, String status_list) {
		boolean isValid = false;
		String[] status_list_arr = null;
		if (status_list.isEmpty() == false) {
			if (status_list.contains(",")) {
				status_list_arr = status_list.split(",");
			} else {
				status_list_arr = new String[] { status_list };
			}
			for (String status : status_list_arr) {

				if (valid_status_list.contains(status))
					isValid = true;
			}
		}

		return isValid;
	}

	public static TCComponentItemRevision getLatestRevision(TCComponentItem item, TCSession session) throws TCException {
		TCComponent[] itemRevs = item.getRelatedComponents(PropertyDefines.REV_REVISION_LIST);
		TCComponentItemRevision latestRev = null;
		int maxRevNum = 1;
		for (int i = 0; i < itemRevs.length; i++) {
			if (!itemRevs[i].getProperty(PropertyDefines.ITEM_REV_ID).contains(".")) {
				int revNum = 1;
				try {
					revNum = Integer.valueOf(itemRevs[i].getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID));
				} catch (Exception ex) {
					latestRev = (TCComponentItemRevision) itemRevs[i];
				}
				if (revNum >= maxRevNum) {
					maxRevNum = revNum;
					latestRev = (TCComponentItemRevision) itemRevs[i];
				}
			}
		}
		return latestRev;
	}

	public static TCComponentItemRevision getLatestValidReleasedRevision(TCSession session, TCComponentItem item, ArrayList<String> validStatus, TCComponent objectInSolutions) throws TCException {
		boolean isAdminSupport = false;
		try {
			String currentRole = session.getRole().getRoleName();
			// isMBOMRole = currentRole.contains("MB Engineer") ||
			// currentRole.contains("DBA");
			isAdminSupport = currentRole.contains("DBA");
		} catch (TCException e) {
			e.printStackTrace();
		}

		TCComponent[] itemRevs = item.getReleasedItemRevisions();
		TCComponentItemRevision latestReleasedRev = null;
		try {
			LinkedHashMap<String, TCComponent> revID = new LinkedHashMap<String, TCComponent>();
			for (int i = 0; i < itemRevs.length; i++) {
				String revIDValue = itemRevs[i].getProperty(PropertyDefines.ITEM_REV_ID);
				if (!revIDValue.contains(".")) {
					String statusValue = itemRevs[i].getProperty(PropertyDefines.REV_RELEASE_STATUS_LIST);
					itemRevs[i].getType().equals(PropertyDefines.TYPE_FRS_REVISION);
					if (isValidReleaseStatus(validStatus, statusValue) ) {
						revID.put(itemRevs[i].getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID), itemRevs[i]);
					}
				}
			}
			if (revID.isEmpty() == false) {
				ArrayList<String> values = new ArrayList<String>();
				values.addAll(revID.keySet());
				Collections.sort(values);
				latestReleasedRev = (TCComponentItemRevision) revID.get(values.get(values.size() - 1));
			}
			
			if (isAdminSupport && latestReleasedRev == null && objectInSolutions instanceof TCComponentItemRevision) {
				latestReleasedRev = (TCComponentItemRevision) objectInSolutions;
			}
			
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return latestReleasedRev;
	}
}
