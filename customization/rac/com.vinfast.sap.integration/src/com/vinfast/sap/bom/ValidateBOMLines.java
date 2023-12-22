package com.vinfast.sap.bom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ValidateBOMLines {
	public static HashMap<TCComponent, TCComponent[]> superBOM(BOMManager BOMUtility, HashMap<TCComponent, TCComponent[]> parentChildLines) {
		HashMap<TCComponent, TCComponent[]> validChildLines = new HashMap<TCComponent, TCComponent[]>();
		DataManagementService dataManagmentService = BOMUtility.getDataManagementService();
		try {
			String[] properties = new String[8];
			properties[0] = PropertyDefines.BOM_BOM_ID;
			properties[1] = PropertyDefines.BOM_OBJECT_TYPE;
			properties[2] = PropertyDefines.BOM_VF3_PUR_LEVEL;
			properties[3] = PropertyDefines.BOM_VL5_PUR_LEVEL;
			properties[4] = PropertyDefines.BOM_MANUF_CODE;
			properties[5] = PropertyDefines.BOM_QUANTITY;
			properties[6] = PropertyDefines.BOM_ITEM_ID;
			properties[7] = PropertyDefines.BOM_OBJECT_STR;

			for (TCComponent parentLine : parentChildLines.keySet()) {
				boolean hasError = false;
				dataManagmentService.getProperties(new TCComponent[] { parentLine }, new String[] { PropertyDefines.BOM_OBJECT_STR });
				String itemID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
				itemID = itemID.substring(0, itemID.indexOf("-"));

				TCComponent[] childLines = parentChildLines.get(parentLine);
				if (childLines.length != 0) {
					dataManagmentService.getProperties(childLines, properties);
					ArrayList<TCComponent> validChilderns = new ArrayList<TCComponent>();
					for (TCComponent childLine : childLines) {
						// skip P/IB parts and part marked manufacturing code with Assy
						boolean result = isSuperBOMLineValid(childLine);
						if (result == true) {
							String childItemID = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String errorMessage = "";
							String validateFlexError = UIGetValuesUtility.validateFlexPart(childLine, dataManagmentService);
							String BOMLineID = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
							if (BOMLineID.isEmpty()) {
								errorMessage = buildErrorMessage(errorMessage, "BOMLine ID");
							}

							String objectType = "";
							try {
								objectType = ((TCComponentBOMLine)childLine).getItemRevision().getType();
							} catch (TCException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							String quantity = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
							if (quantity.isEmpty()) {
								errorMessage = buildErrorMessage(errorMessage, "Quantity");
							}

//							String purLevel = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_VF3_PUR_LEVEL);
//							if (purLevel.isEmpty())
							String purLevel = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_VL5_PUR_LEVEL);
							if (purLevel.isEmpty() && !objectType.equals(PropertyDefines.TYPE_FRS_REVISION)) {
								errorMessage = buildErrorMessage(errorMessage, "Purchase Level");
							}

							if (errorMessage.isEmpty() && validateFlexError.isEmpty()) {
								validChilderns.add(childLine);
							} else {
								String[] printValues = new String[] { Integer.toString(BOMUtility.getSerialNo()), itemID, childItemID, BOMLineID, (!errorMessage.isEmpty() ? (errorMessage + " mandatory value(s) are empty.") : ("")) + validateFlexError, "-", "Error" };
								BOMUtility.printReport("PRINT", printValues);
								hasError = true;
								BOMUtility.setError(hasError);
							}
						}
					}

					if (hasError == false) {
						if (validChilderns.size() == 0) {
							validChildLines.put(parentLine, new TCComponent[] {});
						} else {
							childLines = new TCComponentBOMLine[validChilderns.size()];
							for (int i = 0; i < validChilderns.size(); i++) {
								childLines[i] = validChilderns.get(i);
							}
							validChildLines.put(parentLine, childLines);
						}
					}
				} else {
					validChildLines.put(parentLine, new TCComponent[] {});
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return validChildLines;
	}

	public static HashMap<TCComponent, TCComponent[]> assyBOM(BOMManager BOMUtility, HashMap<TCComponent, TCComponent[]> parentChildLines) {
		HashMap<TCComponent, TCComponent[]> validChildLines = new HashMap<TCComponent, TCComponent[]>();
		DataManagementService dataManagmentService = BOMUtility.getDataManagementService();
		try {
			BOMUtility.hasError();
			String[] properties = new String[6];
			properties[0] = PropertyDefines.BOM_BOM_ID;
			properties[1] = PropertyDefines.BOM_OBJECT_TYPE;
			properties[2] = PropertyDefines.BOM_MANUF_CODE;
			properties[3] = PropertyDefines.BOM_QUANTITY;
			properties[4] = PropertyDefines.BOM_ITEM_ID;
			properties[5] = PropertyDefines.BOM_OBJECT_STR;

			for (TCComponent parentLine : parentChildLines.keySet()) {
				boolean hasError = false;
				dataManagmentService.getProperties(new TCComponent[] { parentLine }, new String[] { PropertyDefines.BOM_OBJECT_STR });
				String itemID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
				itemID = itemID.substring(0, itemID.indexOf("-"));
				TCComponent[] childLines = parentChildLines.get(parentLine);
				if (childLines.length != 0) {
					dataManagmentService.getProperties(childLines, properties);
					ArrayList<TCComponent> validChilderns = new ArrayList<TCComponent>();
					for (TCComponent childLine : childLines) {
						boolean result = isAssyBOMLineValid(childLine);
						if (result == true) {
							String childItemID = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String errorMessage = "";
							String BOMLineID = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
							if (BOMLineID.trim().equals("")) {
								errorMessage = buildErrorMessage(errorMessage, "BOMLine ID");
							}

							String quantity = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
							if (quantity.trim().equals("")) {
								errorMessage = buildErrorMessage(errorMessage, "Quantity");
							}

							if (errorMessage.equals("")) {
								validChilderns.add(childLine);
							} else {
								String[] printValues = new String[] { Integer.toString(BOMUtility.getSerialNo()), itemID, childItemID, BOMLineID, errorMessage + " values are empty. Fill mandatory values.", "-", "Error" };
								BOMUtility.printReport("PRINT", printValues);
								hasError = true;
								BOMUtility.setError(hasError);
							}
						}
					}

					if (hasError == false) {
						if (validChilderns.size() == 0) {
							validChildLines.put(parentLine, new TCComponent[] {});
						} else {
							childLines = new TCComponentBOMLine[validChilderns.size()];
							for (int i = 0; i < validChilderns.size(); i++) {
								childLines[i] = validChilderns.get(i);
							}
							validChildLines.put(parentLine, childLines);
						}
					}

				} else {
					validChildLines.put(parentLine, new TCComponent[] {});
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return validChildLines;
	}

	public static boolean isSuperBOMLineValid(TCComponent bomLine) {
		boolean isBOMLineValid = false;
		try {
			String objectType = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);
			String purLevel = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_VL5_PUR_LEVEL);
			String materialType = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_MATERIAL_TYPE);
			String partType = "";
			TCComponentItem item = ((TCComponentBOMLine) bomLine).getItem();
			partType = item.getPropertyDisplayableValue(PropertyDefines.ITEM_SW_PART_TYPE).trim().toLowerCase();
			if (partType.isEmpty()) {
				partType = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_PART_TYPE).trim().toLowerCase();
			}

			String manufCode = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
			if (!manufCode.equals("Assy") && (!objectType.equals(PropertyDefines.TYPE_STR_PART_REVISION) && !objectType.equals(PropertyDefines.TYPE_COST_ITEM_REVISION) && !objectType.equals(PropertyDefines.TYPE_ITEM_REVISION) && !objectType.equals(PropertyDefines.TYPE_VF_ITEM_REVISION) && !objectType.equals(PropertyDefines.TYPE_MECNTOOL_REVISION))
					&& (purLevel.equals(PropertyDefines.PUR_LEVEL_P) || purLevel.equals(PropertyDefines.PUR_LEVEL_IB) || validFRSPart(purLevel, partType, materialType) || objectType.equals(PropertyDefines.TYPE_FRS_REVISION) )) {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		return isBOMLineValid;
	}

	private static boolean validFRSPart(String purLevel, String partType, String materialType) {
		if (!purLevel.equals(PropertyDefines.PUR_LEVEL_IB) && !purLevel.equals(PropertyDefines.PUR_LEVEL_P) && !partType.isEmpty() && !purLevel.isEmpty()) {
			return true;
		}

//		if(!purLevel.equals(PropertyDefines.PUR_LEVEL_IB) && !purLevel.equals(PropertyDefines.PUR_LEVEL_P)
//			&& (partType.equals(PropertyDefines.PART_HARDWARE) || (partType.equals(PropertyDefines.PART_SOFTWARE) || partType.equals(PropertyDefines.PART_IN_HOUSE) || partType.equals(PropertyDefines.PART_SOFTWARE_APP)|| partType.equals(PropertyDefines.PART_SOFTWARE_BASIC) || partType.equals(PropertyDefines.PART_PURCHASE)))
//			){
//			return true;
//		}
		return false;
	}

	public static boolean isAssyBOMLineValid(TCComponent bomLine) {
		boolean isBOMLineValid = false;
		try {
			String objectType = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);
			String manufCode = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);

			if (!manufCode.equals("Assy") && (!objectType.equals(PropertyDefines.TYPE_COST_ITEM_REVISION) || !objectType.equals(PropertyDefines.TYPE_ITEM_REVISION) || !objectType.equals(PropertyDefines.TYPE_VF_ITEM_REVISION) || !objectType.equals(PropertyDefines.TYPE_MECNTOOL_REVISION))) {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return isBOMLineValid;
	}

	private static String buildErrorMessage(String errorMsg, String message) {
		if (errorMsg.isEmpty()) {
			errorMsg = message;
		} else {
			errorMsg = errorMsg + "," + message;
		}
		return errorMsg;
	}
}
