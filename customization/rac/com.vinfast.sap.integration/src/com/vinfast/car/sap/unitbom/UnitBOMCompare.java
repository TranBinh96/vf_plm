package com.vinfast.car.sap.unitbom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class UnitBOMCompare {

	private HashMap<String, TCComponent> problem_Input_Parts = null;
	private HashMap<String, TCComponent> problem_Output_Parts = null;
	private HashMap<String, TCComponent> sol_Input_Parts = null;
	private HashMap<String, TCComponent> sol_Output_Parts = null;
	private static String PROBLEM_ITEM = "problem";
	private static String SOLUTION_ITEM = "solution";
	private static String SUBGROUP = "";
	private static TCSession session = null;
	private BOMManager bomManager = null;
	public UnitBOMCompare(BOMManager manager) {
		// TODO Auto-generated constructor stub
		bomManager = manager;
	}

	public boolean inputOutputMap(TCSession connection, TCComponent ProblemItem, TCComponent SolutionItem, String subGroup) {
		session = connection;
		SUBGROUP = subGroup;
		problem_Input_Parts = new HashMap<String, TCComponent>();
		problem_Output_Parts = new HashMap<String, TCComponent>();
		sol_Input_Parts = new HashMap<String, TCComponent>();
		sol_Output_Parts = new HashMap<String, TCComponent>();
		boolean isProblemError = false;
		if (ProblemItem != null) {
			isProblemError = getInputOutputLines(ProblemItem, PROBLEM_ITEM);
		}
		boolean isSolutionError = getInputOutputLines(SolutionItem, SOLUTION_ITEM);
		if (isProblemError == true || isSolutionError == true) {
			return false;
		}
		return true;
	}

	public boolean getInputOutputLines(TCComponent bomline, String type) {

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

				dmService.getProperties(bomChildLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE, PropertyDefines.BOM_QUANTITY });
				for (TCComponentBOMLine bomChildLine : bomChildLines) {
					String ID = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
					String findNo = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
					String manufCode = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
					String Quantity = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY);
					if (Quantity.equals("") && type.contentEquals(SOLUTION_ITEM)) {
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "Quantity is empty in " + type + " item.", "-", "Error" };
						bomManager.printReport("PRINT", printValues);
						bomManager.incrementSerialNo();
						bomManager.setError(true);
					}
					
					if (manufCode.equals("")) {
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "Manufacturing Code is empty in " + type + " item.", "-", "Error" };
						bomManager.printReport("PRINT", printValues);
						bomManager.incrementSerialNo();
						bomManager.setError(true);
					} else {
						if (manufCode.equals("Assy") == false) {
							String key = ID + "~" + findNo + "~" + manufCode;
							if (findNo.length() != 0 && manufCode.length() != 0) {
								if (manufCode.equals("Input")) {
									Input_Parts.put(key, bomChildLine);
								}
								if (manufCode.equals("Output") && type.contentEquals(SOLUTION_ITEM)) {
									String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "BOMLine ID must be empty on Output Part. Please clear the filled value on " + type + " item.", "-", "Error" };
									bomManager.printReport("PRINT", printValues);
									bomManager.incrementSerialNo();
									bomManager.setError(true);
								}
							} else {
								if (manufCode.equals("Output") == false) {
									String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "BOMLine ID is empty in " + type + " item.", "-", "Error" };
									bomManager.printReport("PRINT", printValues);
									bomManager.incrementSerialNo();
									bomManager.setError(true);
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
		return bomManager.hasError();
	}

	public boolean getCInputOutputLines(TCSession sess, TCComponent bomline, TCComponentBOMLine input, String type) {

		try {
			session = sess;
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

				dmService.getProperties(bomChildLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE,PropertyDefines.BOM_QUANTITY });
				String inputID = input.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
				String inputfindNo = input.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
				String inputmanufCode = input.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
				Input_Parts.put(inputID + "~" + inputfindNo + "~" + inputmanufCode, input);

				for (TCComponentBOMLine bomChildLine : bomChildLines) {
					String ID = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
					String findNo = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
					String manufCode = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);

					if (manufCode.isEmpty()) {
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "Manufacturing Code is empty in " + type + " item.", "-", "Error" };
						bomManager.printReport("PRINT", printValues);
						bomManager.incrementSerialNo();
						bomManager.setError(true);
					} else {
						if (manufCode.equals("Assy") == false) {
							String key = ID + "~" + findNo + "~" + manufCode;
							if (findNo.length() != 0) {
								if (manufCode.equals("Output")) {
									String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "BOMLine ID must be empty on Output Part. Please clear the filled value on " + type + " item.", "-", "Error" };
									bomManager.printReport("PRINT", printValues);
									bomManager.incrementSerialNo();
									bomManager.setError(true);
								}
							} else {
								if (manufCode.equals("Output") == false) {
									String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, ID, findNo, "BOMLine ID is empty in " + type + " item.", "-", "Error" };
									bomManager.printReport("PRINT", printValues);
									bomManager.incrementSerialNo();
									bomManager.setError(true);
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
		return bomManager.hasError();
	}

	public HashMap<String, TCComponent> addOutputPart() {
		if (sol_Output_Parts == null) {
			return sol_Output_Parts;
		}
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Output_Parts);
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Output_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		solutionKeys.removeAll(problemKeys);
		return solution;

	}

	public HashMap<String, TCComponent> deleteOutputPart() {
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

	public HashMap<String, TCComponent> addInputPart() {
		if (sol_Input_Parts == null) {
			return sol_Input_Parts;
		}
		HashMap<String, TCComponent> solution = new HashMap<String, TCComponent>(sol_Input_Parts);
		HashMap<String, TCComponent> problem = new HashMap<String, TCComponent>(problem_Input_Parts);
		Set<String> solutionKeys = solution.keySet();
		Set<String> problemKeys = problem.keySet();
		solutionKeys.removeAll(problemKeys);
		return solution;
	}

	public HashMap<String, TCComponent> deleteInputPart() {
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

	public void processSolutionItem(HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues, TCComponent traverseStructure) {

		Collection<TCComponent> values = addInputPart().values();
		NodeInfo[] BOPLinkedLines = null;
		if (values.isEmpty() == false) {
			TCComponent[] inputLines = values.toArray(new TCComponent[0]);
			BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, bomManager, inputLines,  (TCComponentBOMLine) traverseStructure, SUBGROUP);
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
							HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "A");
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

				BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, bomManager, inputLines, (TCComponentBOMLine) traverseStructure, SUBGROUP);

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
								HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "A");
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

	public void processSolutionCItem(HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues, TCComponent traverseStructure, int count, StringBuilder strBuilder) {

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
						HashMap<String, String> transferBOMMap = createBOM(add_Ouput_Part, add_Input_Part, "C");
						key = transferBOMMap.get("CHILDPART") + "~" + transferBOMMap.get("BOMLINEID") + "~Input";
						ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
						for (TCComponent bop : foundBOPNode) {
							// BOP MAP
							HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "C");
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
								HashMap<String, String> transferBOPMap = createBOP(transferBOMMap, bop, "A");
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

	public void processOWPItems(ArrayList<HashMap<String, String>> transferOWPValues, TCComponent[] bomlineItems, String action) {
		try {

			if (action.equals("A")) {
				for (TCComponent bomline : bomlineItems) {
					if (UIGetValuesUtility.hasMaterials((TCComponentBOMLine) bomline) == false) {
						ArrayList<HashMap<String, String>> OWP = createOWPBOP(session, bomline, action);
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
						ArrayList<HashMap<String, String>> OWP = createOWPBOP(session, operationLine, action);
						if (OWP != null) {
							transferOWPValues.addAll(OWP);
						}
					}
					operationWindow.close();
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HashMap<String, String> createBOM(TCComponent parent, TCComponent child, String action) {

		HashMap<String, String> transferBOMMap = new HashMap<String, String>();
		try {
			transferBOMMap.put("MCN", bomManager.getMCN());
			transferBOMMap.put("PLANTCODE", bomManager.getPlant());
			transferBOMMap.put("ACTION", action);
			transferBOMMap.put("PARENTPART", parent.getProperty(PropertyDefines.BOM_ITEM_ID));
			transferBOMMap.put("CHILDPART", child.getProperty(PropertyDefines.BOM_ITEM_ID));

			transferBOMMap.put("LINE", child.getProperty(PropertyDefines.BOM_DESIGNATOR));
			
			String BOMLineID = child.getProperty(PropertyDefines.BOM_BOM_ID);
			if (BOMLineID.length() != 0) {
				for (int i = BOMLineID.length(); i < 4; i++) {
					BOMLineID = "0" + BOMLineID;
				}
			}else {
				bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "\"BOMLineID\" is empty. Please fill the mandatory \"BOMLineID\" on BOMLine", "", "Error" });
				bomManager.incrementSerialNo();
				bomManager.setError(true);
				return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transferBOMMap;
	}

	private HashMap<String, String> createBOP(HashMap<String, String> transferBOMMap, TCComponent bop, String action) {
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

				if (MESInd.equals("") || MESInd.equalsIgnoreCase("NA")) {
					MESInd = "N";
				} else {
					MESInd = "Y";
				}

				String JIS = operationLine.getProperty("vf5_line_supply_method");
				if (JIS.equals("") || JIS.equalsIgnoreCase("NA")) {
					JIS = "JIS";
				}

				String workStation = UIGetValuesUtility.getSingleWorkStationID(operationLine, "bl_rev_object_name");

				if (transferMES.isEmpty() && bomManager.isOperationInMCN(operationID)) {
					bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "Please first transfer operation [" + operationID + "/" + operationRev + "] to MES and then transfer BOM/BOP to SAP.", "", "Error" });
					return null;
				}
				if (workStation.equals("") || workStation.length() < 13) {
					bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "WorkStation ID is wrong or incorrect.", "", "Error" });
					return null;
				}

				transferBOPMap.put("BOPID", operationID);
				transferBOPMap.put("REVISION", operationRev);
				transferBOPMap.put("LINESUPPLYMETHOD", JIS);
				transferBOPMap.put("MESBOPINDICATOR", MESInd);
				transferBOPMap.put("WORKSTATION", workStation);
			} else {
				bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), SUBGROUP, transferBOMMap.get("CHILDPART"), transferBOMMap.get("BOMLINEID"), "BOPLine is not under operation.", "", "Error" });
				return null;
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return transferBOPMap;
	}

	public ArrayList<HashMap<String, String>> createOWPBOP(TCSession sess, TCComponent bop, String action) {
		session = sess;
		ArrayList<HashMap<String, String>> transferBOPMap = null;
		try {
			TCComponentBOMLine operationLine = (TCComponentBOMLine) bop;
			String ID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
			String adhocOperationID = ";049892;049895;";// list operation IDs which required to be fixed in all outputs => get headerpart from BOMLineID
			boolean isAddHocOperation = false;
			String RevID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
			String transferMES = operationLine.getProperty(PropertyDefines.BOM_TRANSFER_TO_MES);
			
			if (adhocOperationID.contains(";" + ID + ";")) {
				isAddHocOperation = true;
			}
			
			String HT = isAddHocOperation ? operationLine.getProperty(PropertyDefines.BOM_BOM_ID) : operationLine.getItemRevision().getProperty("vf4_reference_component");
			String fieldNameToFillHeaderPart = isAddHocOperation ? "BOMLine ID" : "Reference Component";
			if (HT.equals("")) {
				String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), "OWP", ID + "/" + RevID, "", "On Operation \"" + fieldNameToFillHeaderPart  + "\" column Header Part Information is missing", action, "Error" };
				bomManager.printReport("PRINT", printValues);
				bomManager.incrementSerialNo();
				bomManager.setError(true);
			} else {

				if (UIGetValuesUtility.findItem(session, HT) == null) {
					String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), "OWP", ID + "/" + RevID, "", "On Operation \"" + fieldNameToFillHeaderPart  + "\" column Header Part ID is wrong or does not exists.", action, "Error" };
					bomManager.printReport("PRINT", printValues);
					bomManager.incrementSerialNo();
					bomManager.setError(true);
				} else {
					transferBOPMap = new ArrayList<HashMap<String, String>>();
					String LSM = operationLine.getPropertyDisplayableValue("bl_rev_vf5_line_supply_method");
					if (LSM.equals("")) {
						LSM = "JIS";
					}

					String MES = operationLine.getPropertyDisplayableValue("vf4_operation_type");
					if (MES.equals("") || MES.equals("NA")) {
						MES = "N";
					} else {
						MES = "Y";
					}
					if (transferMES.isEmpty()) {
						bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), "OWP", ID + "/" + RevID, "", "Please first transfer operation to MES and then transfer BOM/BOP to SAP.", "", "Error" });
						bomManager.incrementSerialNo();
						bomManager.setError(true);
						return null;
					}
					String WS = UIGetValuesUtility.getWorkStation(new String[] { "vf3_transfer_to_mes", "vf4_user_notes" }, operationLine.getItemRevision());
					if (WS.length() == 0) {

						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), "OWP", ID + "/" + RevID, "", "Workstation ID Information is missing", action, "Error" };
						bomManager.printReport("PRINT", printValues);
						bomManager.incrementSerialNo();
						bomManager.setError(true);
					} else {
						String[] split = WS.split(",");
						for (String workstation : split) {

							if (workstation.length() < 13) {

								String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), "OWP", ID + "/" + RevID, "", "Workstation ID Information is wrong", action, "Error" };
								bomManager.printReport("PRINT", printValues);
								bomManager.incrementSerialNo();
								bomManager.setError(true);
							} else {

								HashMap<String, String> tempBOPMap = new HashMap<String, String>();
								tempBOPMap.put("SAPPLANT", bomManager.getPlant());
								tempBOPMap.put("TOPLEVELPART", HT);
								tempBOPMap.put("HEADERPART", HT);
								tempBOPMap.put("BOMLINEID", "");
								tempBOPMap.put("ACTION", action);
								tempBOPMap.put("WORKSTATION", workstation);
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
