package com.teamcenter.integration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vines.sap.unitBomBop.VESUnitBomBopTransferHandler;
import com.vinfast.sap.dialogs.UnitTransferDialog;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class VESUnitBOMBOPTransferService {
	private HashMap<String, TCComponent> problem_Input_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> problem_Output_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> sol_Input_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> sol_Output_Parts = new HashMap<String, TCComponent>();
	private static String PROBLEM_ITEM = "problem";
	private static String SOLUTION_ITEM = "solution";
	private static String SUBGROUP = "";
	private TCSession session = null;

	public boolean inputOutputMap(TCSession connection, TCComponent ProblemItem, TCComponent SolutionItem, String subGroup, int count, StringBuilder strBuilder) {
		session = connection;
		SUBGROUP = subGroup;
		boolean isProblemError = false;
		if (ProblemItem != null) {
			isProblemError = getInputOutputLines(ProblemItem, count, strBuilder, PROBLEM_ITEM);
		}
		boolean isSolutionError = getInputOutputLines(SolutionItem, count, strBuilder, SOLUTION_ITEM);
		if (isProblemError == true || isSolutionError == true) {
			return false;
		}
		return true;
	}

	private boolean getInputOutputLines(TCComponent bomline, int count, StringBuilder strBuilder, String type) {
		boolean hasError = false;
		try {
			DataManagementService dmService = DataManagementService.getService(session);
			HashMap<String, TCComponent> Input_Parts = new HashMap<String, TCComponent>();
			HashMap<String, TCComponent> Output_Parts = new HashMap<String, TCComponent>();

			ExpandPSData[] allLines = UIGetValuesUtility.traverseSingleLevelBOM(session, (TCComponentBOMLine) bomline);
			TCComponentBOMLine[] bomChildLines = null;
			if (allLines.length != 0) {
				bomChildLines = new TCComponentBOMLine[allLines.length];
				int bomLinesCount = 0;
				for (ExpandPSData PSData : allLines) {
					bomChildLines[bomLinesCount] = PSData.bomLine;
					bomLinesCount++;
				}

				dmService.getProperties(bomChildLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE });
				for (TCComponentBOMLine bomChildLine : bomChildLines) {
					String ID = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
					String findNo = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
					String manufCode = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
					String quanity = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
					if (quanity.equals("")) {
						VESUnitBomBopTransferHandler.canTransfer(false);
						String[] printValues = new String[] { Integer.toString(count), SUBGROUP, ID, findNo, "Quantity is empty " + type + " on BOMLine.", "-", "Error" };
						Logger.bufferResponse("PRINT", printValues, strBuilder);
						VESUnitBomBopTransferHandler.counterIncrement();
						hasError = true;
					}
					

					if (manufCode.equals("")) {
						VESUnitBomBopTransferHandler.canTransfer(false);
						String[] printValues = new String[] { Integer.toString(count), SUBGROUP, ID, findNo, "Manufacturing Code is empty " + type + " item.", "-", "Error" };
						Logger.bufferResponse("PRINT", printValues, strBuilder);
						VESUnitBomBopTransferHandler.counterIncrement();
						hasError = true;
					} else {
						if (manufCode.equals("Assy") == false) {
							String key = ID + "~" + findNo + "~" + manufCode;
							if (findNo.length() != 0 && manufCode.length() != 0) {
								if (manufCode.equals("Input")) {
									Input_Parts.put(key, bomChildLine);
								}
								if (manufCode.equals("Output")) {
									Output_Parts.put(key, bomChildLine);
								}
							} else {
								if (manufCode.equals("Output") == false) {
									VESUnitBomBopTransferHandler.canTransfer(false);
									String[] printValues = new String[] { Integer.toString(count), SUBGROUP, ID, findNo, "BOMLine ID is empty " + type + " item.", "-", "Error" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
									VESUnitBomBopTransferHandler.counterIncrement();
									hasError = true;
								} else {
									Output_Parts.put(key, bomChildLine);
								}
							}
						}
					}
				}
				switch (type) {
				case "problem":
					problem_Input_Parts = Input_Parts;
					problem_Output_Parts = Output_Parts;
					break;
				case "solution":
					sol_Input_Parts = Input_Parts;
					sol_Output_Parts = Output_Parts;
					break;
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return hasError;
	}

	private HashMap<String, TCComponent> addOutputPart() {
		if (problem_Output_Parts == null) {
			return sol_Output_Parts;
		}
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Output_Parts);
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Output_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		solutionKeys.removeAll(problemKeys);
		return solution;
	}

	private HashMap<String, TCComponent> deleteOutputPart() {
		if (problem_Output_Parts == null) {
			return problem_Output_Parts;
		}
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Output_Parts);
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Output_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		problemKeys.removeAll(solutionKeys);
		return problem;
	}

	private HashMap<String, TCComponent> addInputPart() {
		if (problem_Input_Parts == null) {
			return sol_Input_Parts;
		}
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Input_Parts);
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Input_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		solutionKeys.removeAll(problemKeys);
		return solution;
	}

	private HashMap<String, TCComponent> deleteInputPart() {
		if (problem_Input_Parts == null) {
			return problem_Input_Parts;
		}
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Input_Parts);
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Input_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		problemKeys.removeAll(solutionKeys);
		return problem;
	}

	public void processProblemItem(HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues) {
		// Delete Input lines removed from latest revision
		HashMap<String, TCComponent> del_Input_parts = deleteInputPart();
		if (del_Input_parts != null) {
			Set<String> del_Input_keys = del_Input_parts.keySet();
			for (String delLine : del_Input_keys) {
				TCComponent del_Input_Part = del_Input_parts.get(delLine);
				Set<String> del_Out_keys = problem_Output_Parts.keySet();
				for (String delOutputLine : del_Out_keys) {
					TCComponent del_Ouput_Part = problem_Output_Parts.get(delOutputLine);
					HashMap<String, String> transferBOMMap = createBOM(del_Ouput_Part, del_Input_Part, "D");
					transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
				}
				problem_Input_Parts.remove(delLine);
			}
		}
		// Delete Output lines removed from latest revision
		HashMap<String, TCComponent> del_Output_parts = deleteOutputPart();
		if (del_Output_parts != null) {
			Set<String> del_Output_keys = del_Output_parts.keySet();
			for (String delLine : del_Output_keys) {
				TCComponent del_Part = del_Output_parts.get(delLine);
				Set<String> del_input_keys = problem_Input_Parts.keySet();
				for (String delInputLine : del_input_keys) {
					TCComponent del_Input_Part = problem_Input_Parts.get(delInputLine);
					HashMap<String, String> transferBOMMap = createBOM(del_Part, del_Input_Part, "D");
					transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
				}
				problem_Output_Parts.remove(delLine);
			}
		}
	}

	public void processSolutionItem(HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues, TCComponent traverseStructure, int count, StringBuilder strBuilder) {
		Collection<TCComponent> values = addInputPart().values();
		NodeInfo[] BOPLinkedLines = null;
		if (values.isEmpty() == false) {
			TCComponent[] inputLines = values.toArray(new TCComponent[0]);
			BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, (TCComponentBOMLine) traverseStructure, SUBGROUP, count, strBuilder);
			if (BOPLinkedLines != null) {
				for (NodeInfo addLine : BOPLinkedLines) {
					TCComponent add_Input_Part = addLine.originalNode;
					TCComponent[] foundBOPNode = addLine.foundNodes;
					String key = "";
					Set<String> add_Out_keys = sol_Output_Parts.keySet();
					for (String addOutputLine : add_Out_keys) {
						TCComponent add_Ouput_Part = sol_Output_Parts.get(addOutputLine);
						HashMap<String, String> transferBOMMap = createBOM(add_Ouput_Part, add_Input_Part, "A");
						key = transferBOMMap.get("CHILDPART") + "~" + transferBOMMap.get("BOMLINEID") + "~Input";
						ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
						for (TCComponent bop : foundBOPNode) {
							// BOP MAP
							HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "A", count, strBuilder);
							if (transferBOPMap != null) {
								transferBOPMaps.add(transferBOPMap);
							}
						}
						transferValues.put(transferBOMMap, transferBOPMaps);
					}
					sol_Input_Parts.remove(key);
				}
			}
		}

		Set<String> add_Output_keys = addOutputPart().keySet();

		if (add_Output_keys.isEmpty() == false) {
			values = sol_Input_Parts.values();
			if (values.isEmpty() == false) {
				TCComponent[] inputLines = new TCComponent[values.size()];
				Iterator<TCComponent> iterator = values.iterator();
				int itr = 0;
				while (iterator.hasNext()) {
					inputLines[itr] = iterator.next();
					itr++;
				}

				BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, (TCComponentBOMLine) traverseStructure, SUBGROUP, count, strBuilder);
				if (BOPLinkedLines != null) {
					for (String addLine : add_Output_keys) {
						TCComponent add_Ouput_Part = sol_Output_Parts.get(addLine);
						for (NodeInfo addInputLine : BOPLinkedLines) {
							TCComponent add_Input_Part = addInputLine.originalNode;
							TCComponent[] foundBOPNode = addInputLine.foundNodes;

							HashMap<String, String> transferBOMMap = createBOM(add_Ouput_Part, add_Input_Part, "A");
							// BOP MAP
							ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
							for (TCComponent bop : foundBOPNode) {
								HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "A", count, strBuilder);
								if (transferBOPMap != null) {
									transferBOPMaps.add(transferBOPMap);
								}
							}
							transferValues.put(transferBOMMap, transferBOPMaps);
						}
						sol_Output_Parts.remove(addLine);
					}
				}
			}
		}
	}

	public void processOWPItems(ArrayList<HashMap<String, String>> transferOWPValues, TCComponent[] bomlineItems, String action, int count, StringBuilder strBuilder) {
		try {
			if (action.equals("A")) {
				for (TCComponent bomline : bomlineItems) {
					if (UIGetValuesUtility.hasMaterials((TCComponentBOMLine) bomline) == false) {
						ArrayList<HashMap<String, String>> OWP = createOWPBOP(bomline, action, count, strBuilder);
						if (OWP != null) {
							transferOWPValues.addAll(OWP);
						}
					}
				}
			} else {
				for (TCComponent items : bomlineItems) {
					TCComponentItemRevision problem = (TCComponentItemRevision) items;
					CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
					TCComponentBOMWindow operationWindow = output[0].bomWindow;
					TCComponentBOMLine operationLine = output[0].bomLine;
					if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
						ArrayList<HashMap<String, String>> OWP = createOWPBOP(operationLine, action, count, strBuilder);
						if (OWP != null) {
							transferOWPValues.addAll(OWP);
						}
					}
					operationWindow.close();
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, String> createBOM(TCComponent parent, TCComponent child, String action) {
//		boolean isQA = false;
//		if (UnitTransferDialog.getServer().contentEquals("QA")) {
//			isQA = true;
//		}
		
		HashMap<String, String> transferBOMMap = new HashMap<String, String>();
		try {
			transferBOMMap.put("MCN", UnitTransferDialog.getMCNID());
			transferBOMMap.put("PLANTCODE", UnitTransferDialog.getPlant());
			transferBOMMap.put("ACTION", action);
			transferBOMMap.put("PARENTPART", parent.getProperty(PropertyDefines.BOM_ITEM_ID));
			transferBOMMap.put("CHILDPART", child.getProperty(PropertyDefines.BOM_ITEM_ID));
			transferBOMMap.put("ALTERNATIVE", "");
			String BOMLineID = child.getProperty(PropertyDefines.BOM_BOM_ID);
			if (BOMLineID.length() != 0) {
				for (int i = BOMLineID.length(); i < 4; i++) {
					BOMLineID = "0" + BOMLineID;
				}
			}
			transferBOMMap.put("BOMLINEID", BOMLineID.trim());

			String quantity = child.getProperty(PropertyDefines.BOM_QUANTITY);
			if (quantity.length() == 0) {
				quantity = "1.000";
			}

			transferBOMMap.put("QUANTITY", quantity);
			transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) child, transferBOMMap);
		} catch (TCException e) {
			e.printStackTrace();
		}
		return transferBOMMap;
	}

	private HashMap<String, String> createBOP(HashMap<String, String> transferBOMMap, TCComponent bop, String action, int count, StringBuilder strBuilder) {
		HashMap<String, String> transferBOPMap = null;
		try {
			transferBOPMap = new HashMap<String, String>();
			transferBOPMap.put("SAPPLANT", transferBOMMap.get("PLANTCODE"));
			transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
			transferBOPMap.put("TOPLEVELPART", transferBOMMap.get("PARENTPART"));
			transferBOPMap.put("HEADERPART", transferBOMMap.get("PARENTPART"));
			transferBOPMap.put("ACTION", action);
			transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			TCComponentBOMLine operationLine = (TCComponentBOMLine) bop.getReferenceProperty("bl_parent");
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();
			if (operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {
				String operationID = operationLine.getProperty("bl_item_item_id");
				String operationRev = operationLine.getProperty("bl_rev_item_revision_id");
				String MESInd = operationLine.getProperty("vf4_operation_type");
				String transferMES = operationLine.getProperty(PropertyDefines.BOM_TRANSFER_TO_MES);

				if (MESInd.equals("") || MESInd.equalsIgnoreCase("NA"))
					MESInd = "N";
				else
					MESInd = "Y";

				String JIS = operationLine.getProperty("vf5_line_supply_method");
				if (JIS.equals("") || JIS.equalsIgnoreCase("NA"))
					JIS = "JIS";

				String workStation = UIGetValuesUtility.getWorkStationID(operationLine, "bl_rev_object_name");

				if (transferMES.trim().isEmpty()) {
					VESUnitBomBopTransferHandler.canTransfer(false);
					Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "Please first transfer operation [" + operationID + "/" + operationRev + "] to MES and then transfer BOM/BOP to SAP.", "", "Error" }, strBuilder);
					VESUnitBomBopTransferHandler.counterIncrement();
					return null;
				}
				if (workStation.equals("") || workStation.length() < 13) {
					VESUnitBomBopTransferHandler.canTransfer(false);
					Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "WorkStation ID is wrong or incorrect.", "", "Error" }, strBuilder);
					VESUnitBomBopTransferHandler.counterIncrement();
					return null;
				}

				transferBOPMap.put("BOPID", operationID);
				transferBOPMap.put("REVISION", operationRev);
				transferBOPMap.put("LINESUPPLYMETHOD", JIS);
				transferBOPMap.put("MESBOPINDICATOR", MESInd);
				transferBOPMap.put("WORKSTATION", workStation);
			} else {
				VESUnitBomBopTransferHandler.canTransfer(false);
				Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "BOPLine is not under operation.", "", "Error" }, strBuilder);
				VESUnitBomBopTransferHandler.counterIncrement();
				return null;
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return transferBOPMap;
	}

	public static ArrayList<HashMap<String, String>> createOWPBOP(TCComponent bop, String action, int count, StringBuilder strBuilder) {
		ArrayList<HashMap<String, String>> transferBOPMap = null;
		try {
			TCComponentBOMLine operationLine = (TCComponentBOMLine) bop;
			String ID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
			String RevID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
			String referenceComponent = operationLine.getItemRevision().getProperty(PropertyDefines.BOM_REF_COMP);
			String transferMES = operationLine.getProperty(PropertyDefines.BOM_TRANSFER_TO_MES);
			String operationType = operationLine.getProperty(PropertyDefines.BOM_OPERATION_TYPE);

			if (referenceComponent.isEmpty()) {
				VESUnitBomBopTransferHandler.canTransfer(false);
				String[] printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "On Operation \"Reference Component\" column Header Part Information is missing", action, "Error" };
				Logger.bufferResponse("PRINT", printValues, strBuilder);
				VESUnitBomBopTransferHandler.counterIncrement();
			} else {
				transferBOPMap = new ArrayList<HashMap<String, String>>();
				String LSM = operationLine.getPropertyDisplayableValue("bl_rev_vf5_line_supply_method");
				if (LSM.equals(""))
					LSM = "JIS";

				String MES = operationLine.getPropertyDisplayableValue("vf4_operation_type");
				if (MES.equals("") || MES.equals("NA"))
					MES = "N";
				else
					MES = "Y";

				if (transferMES.isEmpty()) {
					VESUnitBomBopTransferHandler.canTransfer(false);
					Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "Please first transfer operation to MES and then transfer BOM/BOP to SAP.", "", "Error" }, strBuilder);
					VESUnitBomBopTransferHandler.counterIncrement();
					return null;
				}
				String workStation = UIGetValuesUtility.getWorkStation(new String[] { "vf3_transfer_to_mes", "vf4_user_notes" }, operationLine.getItemRevision());
				if (workStation.isEmpty()) {
					String[] printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "Workstation ID Information is missing", action, "Error" };
					Logger.bufferResponse("PRINT", printValues, strBuilder);
					VESUnitBomBopTransferHandler.counterIncrement();
				} else {
					List<String> workStationList = new ArrayList<String>();
					if (workStation.contains(","))
						workStationList = Arrays.asList(workStation.split(","));
					else
						workStationList.add(workStation);

					for (String workStationItem : workStationList) {
						if (workStationItem.length() < 13) {
							String[] printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "Workstation ID Information is wrong", action, "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							VESUnitBomBopTransferHandler.counterIncrement();
						} else {
							List<String> referenceComponentList = new ArrayList<String>();
							if (operationType.contains("Buy-Off") || operationType.contains("Screwing")) {
								if (referenceComponent.contains(",")) {
									referenceComponentList = Arrays.asList(referenceComponent.split(","));
								} else {
									referenceComponentList.add(referenceComponent);
								}
							} else {
								referenceComponentList.add(referenceComponent);
							}
							
							for (String referenceComponentItem : referenceComponentList) {
								HashMap<String, String> tempBOPMap = new HashMap<String, String>();
								tempBOPMap.put("SAPPLANT", UnitTransferDialog.getPlant());
								tempBOPMap.put("TOPLEVELPART", referenceComponentItem);
								tempBOPMap.put("HEADERPART", referenceComponentItem);
								tempBOPMap.put("BOMLINEID", "");
								tempBOPMap.put("ACTION", action);
								tempBOPMap.put("WORKSTATION", workStationItem);
								tempBOPMap.put("LINESUPPLYMETHOD", LSM);
								tempBOPMap.put("BOPID", ID);
								tempBOPMap.put("MESBOPINDICATOR", MES);
								tempBOPMap.put("REVISION", RevID);
								tempBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
								transferBOPMap.add(tempBOPMap);
							}
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return transferBOPMap;
	}
}
