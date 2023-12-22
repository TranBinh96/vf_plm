package com.vinfast.sap.util;

import java.util.HashMap;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.services.Logger;

public class BOMCompareReport {
	public HashMap<String, TCComponent> problem_Input_Parts = null;
	public HashMap<String, TCComponent> problem_Output_Parts = null;
	public HashMap<String, TCComponent> sol_Input_Parts = null;
	public HashMap<String, TCComponent> sol_Output_Parts = null;
	public HashMap<String, TCComponent> added_Parts = null;
	public HashMap<String, TCComponent> deleted_Parts = null;
	public HashMap<String, TCComponent> common_Parts = new HashMap<String, TCComponent>();
	public HashMap<String, TCComponent> problemParts = null;
	public HashMap<String, TCComponent> solutionParts = null;
	
	private TCSession session = null;
	private DataManagementService dmService = null;
	
	public BOMCompareReport() {
	}

	public boolean inputOutputMap(TCSession _session, TCComponent ProblemItem, TCComponent SolutionItem, int count, StringBuilder strBuilder) {
		session = _session;
		dmService = DataManagementService.getService(session);
		boolean isSuccess = true;
		try {
			problem_Input_Parts = new HashMap<String, TCComponent>();
			problem_Output_Parts = new HashMap<String, TCComponent>();

			sol_Input_Parts = new HashMap<String, TCComponent>();
			sol_Output_Parts = new HashMap<String, TCComponent>();

			if (ProblemItem != null) {
				String subGroupID = ProblemItem.getProperty("bl_item_item_id");
				String subGroupRev = ProblemItem.getProperty("bl_rev_item_revision_id");
				ExpandPSData[] allLines = UIGetValuesUtility.traverseSingleLevelBOM(session, (TCComponentBOMLine) ProblemItem);
				TCComponentBOMLine[] problemLines = null;
				if (allLines.length != 0) {
					problemLines = new TCComponentBOMLine[allLines.length];
					int bomLinesCount = 0;
					for (ExpandPSData PSData : allLines) {
						problemLines[bomLinesCount] = PSData.bomLine;
						bomLinesCount++;
					}

					dmService.getProperties(problemLines, new String[] { "bl_item_item_id","VF4_bomline_id", "VF4_manuf_code",PropertyDefines.BOM_QUANTITY,"VF4_sap_bom_id" });

					for (TCComponentBOMLine problemLine : problemLines) {
						String ID = problemLine.getPropertyDisplayableValue("bl_item_item_id");
						String findNo = problemLine.getPropertyDisplayableValue("VF4_bomline_id");
						String manufCode = problemLine.getPropertyDisplayableValue("VF4_manuf_code");
//						String Quantity = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
//						if (Quantity.equals("")) {
//							String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID, findNo, "Quantity is empty in Problem Item.", "-", "Error" };
//							Logger.bufferResponse("PRINT", printValues, strBuilder);
//							count++;
//							isSuccess = false;
//						}
						
						if (manufCode.equals("")) {
							String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID, findNo, "Manufacturing Code is empty in Problem Item.", "-", "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
							isSuccess = false;
						} else {
							if (manufCode.equals("Assy") == false) {
								String key = ID + "~" + findNo + "~" + manufCode;
								if (findNo.length() != 0 && manufCode.length() != 0) {
									if (manufCode.equals("Input")) {
										problem_Input_Parts.put(key, problemLine);
									}
									/*if (manufCode.equals("Output")) {
										String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID,findNo, "BOMLine ID must be empty on Output Part. Please clear the filled value in Problem Item.", "-", "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										count++;
										isSuccess = false;
									}*/
								} else {
									if (manufCode.equals("Output") == false) {
										String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID,findNo, "BOMLine ID is empty in Problem Item.", "-", "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										count++;
										isSuccess = false;
									} else {
										problem_Output_Parts.put(key, problemLine);
									}
								}
							}
						}
					}
				}
			}

			if (SolutionItem != null) {
				String subGroupID = SolutionItem.getProperty("bl_item_item_id");
				String subGroupRev = SolutionItem.getProperty("bl_rev_item_revision_id");
				ExpandPSData[] allLines = UIGetValuesUtility.traverseSingleLevelBOM(session, (TCComponentBOMLine) SolutionItem);
				TCComponentBOMLine[] solutionLines = null;
				if (allLines.length != 0) {
					solutionLines = new TCComponentBOMLine[allLines.length];
					int bomLinesCount = 0;
					for (ExpandPSData PSData : allLines) {
						solutionLines[bomLinesCount] = PSData.bomLine;
						bomLinesCount++;
					}

					dmService.getProperties(solutionLines, new String[] { "bl_item_item_id","bl_rev_item_revision_id","VF4_bomline_id", "VF4_manuf_code", PropertyDefines.BOM_OBJECT_TYPE, "VF4_sap_bom_id" });
					for (TCComponentBOMLine solutionLine : solutionLines) {
						String ID = solutionLine.getProperty("bl_item_item_id");
						String findNo = solutionLine.getProperty("VF4_bomline_id");
						String manufCode = solutionLine.getProperty("VF4_manuf_code");
						String Quantity = solutionLine.getProperty(PropertyDefines.BOM_QUANTITY);
						if (Quantity.equals("")) {
							String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID, findNo, "Quantity is empty in Solution Item.", "-", "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
							isSuccess = false;
						}
						String[] values = solutionLine.getProperties(new String[] { "bl_item_item_id", "VF4_bomline_id", "VF4_manuf_code", PropertyDefines.BOM_QUANTITY });

						String validateFlexError = UIGetValuesUtility.validateFlexPart(solutionLine, dmService);
						if (!validateFlexError.isEmpty()) {
							String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID, findNo, validateFlexError, "-", "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
							isSuccess = false;
						}

						if (manufCode.equals("")) {
							String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID, findNo, "Manufacturing Code is empty in Solution Item.", "-", "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
							isSuccess = false;
						} else {
							if (manufCode.equals("Assy") == false) {
								String key = ID + "~" + findNo + "~" + manufCode;
								if (findNo.length() != 0 && manufCode.length() != 0) {
									if (manufCode.equals("Input")) {
										sol_Input_Parts.put(key, solutionLine);
									}
									if (manufCode.equals("Output")) {
										String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, ID,findNo, "BOMLine ID must be empty on Output Part. Please clear the filled value in Solution Item.", "-", "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										count++;
										isSuccess = false;
									}
								} else {
									if (manufCode.equals("Output") == false) {
										String[] printValues = new String[] { Integer.toString(count), subGroupID+"/"+subGroupRev, values[0], values[1], "BOMLine ID is empty in Solution Item.", "-", "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										count++;
										isSuccess = false;
									} else {
										sol_Output_Parts.put(key, solutionLine);
									}
								}
							}
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public HashMap<String, TCComponent> getProblemInputItems() {
		return problem_Input_Parts;
	}

	public HashMap<String, TCComponent> getProblemOutputItems() {
		return problem_Output_Parts;
	}

	public HashMap<String, TCComponent> getSolutionInputItems() {
		return sol_Input_Parts;
	}

	public HashMap<String, TCComponent> getSolutionOutputItems() {
		return sol_Output_Parts;
	}

	public HashMap<String, TCComponent> getProblemItems() {
		return problemParts;
	}

	public HashMap<String, TCComponent> getSolutionItems() {
		return solutionParts;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, TCComponent> addPart(HashMap<String, TCComponent> sol_items, HashMap<String, TCComponent> prob_items) {
		HashMap<String, TCComponent> addBOM = (HashMap<String, TCComponent>) sol_items.clone();
		HashMap<String, TCComponent> addST = (HashMap<String, TCComponent>) prob_items.clone();
		Set<String> addSTBOM = addBOM.keySet();
		Set<String> addSTPart = addST.keySet();
		addSTBOM.removeAll(addSTPart);
		return addBOM;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, TCComponent> delPart(HashMap<String, TCComponent> sol_items, HashMap<String, TCComponent> prob_items) {
		HashMap<String, TCComponent> addBOM = (HashMap<String, TCComponent>) sol_items.clone();
		HashMap<String, TCComponent> addST = (HashMap<String, TCComponent>) prob_items.clone();
		Set<String> addDelBOM = addBOM.keySet();
		Set<String> addPart = addST.keySet();
		addPart.removeAll(addDelBOM);
		return addST;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, TCComponent> changePart(HashMap<String, TCComponent> hpbom_items, HashMap<String, TCComponent> hpST_items) {
		HashMap<String, TCComponent> changeParts = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> addSTBOM = (HashMap<String, TCComponent>) hpbom_items.clone();
		HashMap<String, TCComponent> addSTPart = (HashMap<String, TCComponent>) hpST_items.clone();
		Set<String> addBOM = addSTBOM.keySet();
		Set<String> addST = addSTPart.keySet();
		addBOM.retainAll(addST);
		for (String key : addSTBOM.keySet()) {
			try {
				TCComponent solutionItem = addSTBOM.get(key);
				TCComponent problemItem = addSTPart.get(key);
				String[] sol_values = solutionItem.getProperties(new String[] { "bl_quantity" });
				String[] prb_values = problemItem.getProperties(new String[] { "bl_quantity" });
				if ((sol_values[0].equals(prb_values[0]) && sol_values[1].equals(prb_values[1])) == false) {
					changeParts.put(key, solutionItem);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return changeParts;
	}

	public boolean problemSolutionMap(TCSession session, TCComponent ProblemItem, TCComponent SolutionItem, int count, StringBuilder strBuilder) {
		boolean isSuccess = true;
		try {
			problemParts = new HashMap<String, TCComponent>();
			solutionParts = new HashMap<String, TCComponent>();

			if (ProblemItem != null) {
				String subGroup = ProblemItem.getProperty(PropertyDefines.BOM_OBJECT_STR);
				subGroup = subGroup.substring(0, subGroup.indexOf("-"));
				HashMap<TCComponent, TCComponent[]> allLines = UIGetValuesUtility.expandBOMOneLevel(session, new TCComponent[] { ProblemItem });

				for (TCComponent problemItemLine : allLines.keySet()) {
					TCComponent[] childLines = allLines.get(problemItemLine);
					if (childLines.length != 0) {
						dmService.getProperties(childLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE, PropertyDefines.BOM_QUANTITY, "VF4_sap_bom_id" });

						for (TCComponent problemLine : childLines) {
							String ID = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String findNo = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
							String manufCode = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
							String quanity = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
							if (quanity.equals("")) {
								String[] printValues = new String[] { Integer.toString(count), subGroup, ID, findNo,
										"Quantity is empty in Problem Item.", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
								isSuccess = false;
							}

							if (findNo.equals("")) {
								String[] printValues = new String[] { Integer.toString(count), subGroup, ID, findNo,
										"BOMLine ID is empty in Problem Item.", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
								isSuccess = false;
							} else {
								if (manufCode.equals("Assy") == false) {
									String key = ID + "~" + findNo;
									problemParts.put(key, problemLine);
								}
							}
						}
					}
				}
			}

			if (SolutionItem != null) {
				String subGroup = SolutionItem.getProperty(PropertyDefines.BOM_OBJECT_STR);
				subGroup = subGroup.substring(0, subGroup.indexOf("-"));
				HashMap<TCComponent, TCComponent[]> allLines = UIGetValuesUtility.expandBOMOneLevel(session, new TCComponent[] { SolutionItem });

				for (TCComponent solutionItemLine : allLines.keySet()) {
					TCComponent[] childLines = allLines.get(solutionItemLine);
					if (childLines.length != 0) {
						dmService.getProperties(childLines, new String[] { PropertyDefines.BOM_ITEM_ID,
								PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE, "VF4_sap_bom_id" });
						for (TCComponent problemLine : childLines) {
							String ID = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String findNo = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
							String manufCode = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
							String quanity = problemLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
							if (quanity.equals("")) {
								String[] printValues = new String[] { Integer.toString(count), subGroup, ID, findNo,
										"Quantity is empty in Solution Item.", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
								isSuccess = false;
							}
							
							if (findNo.equals("")) {
								String[] printValues = new String[] { Integer.toString(count), subGroup, ID, findNo, "BOMLine ID is empty in Solution Item.", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
								isSuccess = false;
							} else {
								if (manufCode.equals("Assy") == false) {
									String key = ID + "~" + findNo;
									solutionParts.put(key, problemLine);
								}
							}
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
}
