package com.vinfast.sap.util;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;

/*
 * MCN Validator
 * 
 * 1. Check the selection is only 1 MCN
 * 2. Check has write access on MCN
 * 3. Check shop has only 1 and latest revision
 * 4. Check Change already transferred
 * 5. Only VF Item Revision, VF Structure Revision and Product Item in Impacted Shop
 * 6. Check Valid Shop
 * 
 */
public class MCNValidator {

	public MCNValidator(InterfaceAIFComponent[] selectedObjects) {
		// TODO Auto-generated constructor stub
	}

	public static VFResponse validate(InterfaceAIFComponent[] selectedObjects) {
		VFResponse response = new VFResponse();
		TCSession clientSession = null;
		TCComponentChangeItemRevision changeObject = null;
		TCComponent impactedShopRevision = null;
		String plant = "";
		try {
			if (selectedObjects.length != 1) {
				response.setError(true);
				response.setErrorMessage("Invalid number of selections. Please select only one MCN");
				return response;
			} else {
				changeObject = (TCComponentChangeItemRevision) selectedObjects[0];
				plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
				clientSession = changeObject.getSession();
			}

			if (changeObject.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {
				response.setError(true);
				response.setErrorMessage("MCN is not transfer. Please transfer MCN first to do any transfer.");
				return response;
			}

			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);
			if (UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == false) {
				response.setError(true);
				response.setErrorMessage("You are not authorized to transfer MCN. Please check group/role and try again.");
				return response;
			}
			TCComponent[] impactedShop = changeObject.getContents(PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop.length != 1) {
				response.setError(true);
				response.setErrorMessage("Invalid number of Shops. Please copy only one valid \"Revision\" or \"Product Item\" in Impacted Shop.");
				return response;
			} else {
				impactedShopRevision = impactedShop[0];
				if (impactedShopRevision.isTypeOf(new String[] { PropertyDefines.TYPE_ITEM_REVISION, PropertyDefines.TYPE_STR_PART_REVISION, PropertyDefines.TYPE_VF_ITEM_REVISION, PropertyDefines.TYPE_PRODUCT_ITEM }) == true) {
					if (impactedShopRevision.getType().equals(PropertyDefines.TYPE_ITEM_REVISION) || impactedShopRevision.getType().equals(PropertyDefines.TYPE_VF_ITEM_REVISION) || impactedShopRevision.getType().equals(PropertyDefines.TYPE_STR_PART_REVISION)) {
						String revID = impactedShopRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
						TCComponent[] foundObjects = UIGetValuesUtility.quickSearch(clientSession, new String[] { "Item ID", "Type" }, new String[] { impactedShopRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID), impactedShopRevision.getType() }, "Latest Item Revision...");
						if (foundObjects != null) {
							TCComponent object = foundObjects[0];
							String compareRevID = object.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
							if (revID.equals(compareRevID) == false) {
								response.setError(true);
								response.setErrorMessage("Shop Revision is not latest. Please copy latest Shop revision in Impacted Shop.");
								return response;
							}
						}

						String error = UIGetValuesUtility.checkValidShop((TCComponentItemRevision) impactedShopRevision, plant);
						if (!error.equals("")) {
							response.setError(true);
							response.setErrorMessage(error);
							return response;
						}
					}

				} else {
					response.setError(true);
					response.setErrorMessage("Invalid Shop object Type. Allowed only valid \"Item/Structure/VF Item Revision\" Type or \"Product Item\" in Impacted Shop.");
					return response;
				}
			}

		} catch (TCException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return response;
	}

}
