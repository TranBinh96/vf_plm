package com.teamcenter.integration.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class UnitBOMBOPTransferModel extends ModelAbstract {
	private TCSession session;
	private String subGroup;
	private TCComponent currentItemRev;
	private TCComponent previousItemRevision;
	private ArrayList<UnitBOMBOPModel> bomDataList;
	private ArrayList<UnitBOMBOPModel> bopDataList;
	private TCComponentBOMLine topBOPLine;

	private HashMap<String, TCComponent> problem_Input_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> problem_Output_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> sol_Input_Parts = new HashMap<String, TCComponent>();
	private HashMap<String, TCComponent> sol_Output_Parts = new HashMap<String, TCComponent>();

	public UnitBOMBOPTransferModel() {
		super(ModelType.DATA_SEND);
	}

	public String getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}

	public String getSubGroupNumber() {
		try {
			if (currentItemRev instanceof TCComponentBOMLine)
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
			else
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getSubGroupRev() {
		try {
			if (currentItemRev instanceof TCComponentBOMLine)
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
			else
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getSubGroupName() {
		try {
			if (currentItemRev instanceof TCComponentBOMLine)
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.BOM_NAME);
			else
				return currentItemRev.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public TCComponent getItemRev() {
		return currentItemRev;
	}

	public void setItemRev(TCComponent currentItemRev) {
		this.currentItemRev = currentItemRev;
	}

	public TCComponent getPreviousItemRevision() {
		return previousItemRevision;
	}

	public void setPreviousItemRevision(TCComponent previousItemRevision) {
		this.previousItemRevision = previousItemRevision;
	}

	public ArrayList<UnitBOMBOPModel> getBomDataList() {
		return bomDataList;
	}

	public void setBomDataList(ArrayList<UnitBOMBOPModel> bomDataList) {
		this.bomDataList = bomDataList;
	}

	public ArrayList<UnitBOMBOPModel> getBopDataList() {
		return bopDataList;
	}

	public void setBopDataList(ArrayList<UnitBOMBOPModel> bopDataList) {
		this.bopDataList = bopDataList;
	}

	public void process(TCSession session) {
		bomDataList = new ArrayList<UnitBOMBOPModel>();
		bopDataList = new ArrayList<UnitBOMBOPModel>();
		this.session = session;
		if (currentItemRev != null)
			getInputOutputLines(currentItemRev, "problem");

		if (previousItemRevision != null)
			getInputOutputLines(previousItemRevision, "solution");

		if (getPrepareStatus().compareTo(PREPARE_STATUS.NOT_VALIDATE) == 0) {
			setMessage("Not validate.");
			return;
		}

		processProblemItem();
		processSolutionItem();
	}

	private void getInputOutputLines(TCComponent bomline, String type) {
		HashMap<String, TCComponent> Input_Parts = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> Output_Parts = new HashMap<String, TCComponent>();

		ExpandPSData[] allLines = UIGetValuesUtility.traverseSingleLevelBOM(session, (TCComponentBOMLine) bomline);
		TCComponentBOMLine[] bomChildLines = null;
		if (allLines != null && allLines.length > 0) {
			bomChildLines = new TCComponentBOMLine[allLines.length];
			int bomLinesCount = 0;
			for (ExpandPSData PSData : allLines) {
				bomChildLines[bomLinesCount] = PSData.bomLine;
				bomLinesCount++;
			}

//				dmService.getProperties(bomChildLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, PropertyDefines.BOM_MANUF_CODE });
			for (TCComponentBOMLine bomChildLine : bomChildLines) {
				String ID = "";
				String findNo = "";
				String manufCode = "";
				try {
					if (bomChildLine != null) {
//						ID = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
//						findNo = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
//						manufCode = bomChildLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (manufCode.isEmpty()) {
					UnitBOMBOPModel newBOMItem = new UnitBOMBOPModel();
					newBOMItem.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
					newBOMItem.setMessage("Manufacturing Code is empty.");
					bomDataList.add(newBOMItem);

					setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				} else {
					if (!manufCode.equals("Assy")) {
						String key = ID + "~" + findNo + "~" + manufCode;
						if (!findNo.isEmpty()) {
							if (manufCode.equals("Input")) {
								Input_Parts.put(key, bomChildLine);
							} else if (manufCode.equals("Output")) {
								Output_Parts.put(key, bomChildLine);
							}
						} else {
							if (!manufCode.equals("Output")) {
								UnitBOMBOPModel newBOMItem = new UnitBOMBOPModel();
								newBOMItem.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
								newBOMItem.setMessage("BOMLine ID is empty.");
								bomDataList.add(newBOMItem);

								setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
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
	}

	private void processProblemItem() {
		// Delete Input lines removed from latest revision
		HashMap<String, TCComponent> del_Input_parts = deleteInputPart();
		if (del_Input_parts != null && del_Input_parts.size() > 0) {
			Set<String> del_Input_keys = del_Input_parts.keySet();
			for (String delLine : del_Input_keys) {
				TCComponent del_Input_Part = del_Input_parts.get(delLine);
				Set<String> del_Out_keys = problem_Output_Parts.keySet();
				for (String delOutputLine : del_Out_keys) {
					TCComponent del_Ouput_Part = problem_Output_Parts.get(delOutputLine);
					UnitBOMBOPModel newBomData = new UnitBOMBOPModel();
					newBomData.generateBOMItem(del_Ouput_Part, del_Input_Part, "D");
					bomDataList.add(newBomData);
				}
				problem_Input_Parts.remove(delLine);
			}
		}
		// Delete Output lines removed from latest revision
		HashMap<String, TCComponent> del_Output_parts = deleteOutputPart();
		if (del_Output_parts != null && del_Output_parts.size() > 0) {
			Set<String> del_Output_keys = del_Output_parts.keySet();
			for (String delLine : del_Output_keys) {
				TCComponent del_Part = del_Output_parts.get(delLine);
				Set<String> del_input_keys = problem_Input_Parts.keySet();
				for (String delInputLine : del_input_keys) {
					TCComponent del_Input_Part = problem_Input_Parts.get(delInputLine);
					UnitBOMBOPModel newBomData = new UnitBOMBOPModel();
					newBomData.generateBOMItem(del_Part, del_Input_Part, "D");
					bomDataList.add(newBomData);
				}
				problem_Output_Parts.remove(delLine);
			}
		}
	}

	private void processSolutionItem() {
		HashMap<String, TCComponent> sol_Input_Parts = addInputPart();
		if (sol_Input_Parts != null && sol_Input_Parts.size() > 0) {
			TCComponent[] inputLines = sol_Input_Parts.values().toArray(new TCComponent[0]);
			NodeInfo[] bopLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, topBOPLine);
			if (bopLinkedLines != null && bopLinkedLines.length > 0) {
				for (NodeInfo addLine : bopLinkedLines) {
					TCComponent add_Input_Part = addLine.originalNode;
					TCComponent[] foundBOPNode = addLine.foundNodes;
					String key = "";
					Set<String> add_Out_keys = sol_Output_Parts.keySet();
					for (String addOutputLine : add_Out_keys) {
						TCComponent add_Ouput_Part = sol_Output_Parts.get(addOutputLine);
						UnitBOMBOPModel newBomData = new UnitBOMBOPModel();
						newBomData.generateBOMItem(add_Ouput_Part, add_Input_Part, "A");
						bomDataList.add(newBomData);
						try {
							key = add_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID) + "~" + add_Ouput_Part.getProperty(PropertyDefines.BOM_BOM_ID) + "~Input";
						} catch (Exception e) {
							e.printStackTrace();
						}
						for (TCComponent bopItem : foundBOPNode) {
							UnitBOMBOPModel newBopData = new UnitBOMBOPModel();
							newBopData.generateBOPItem(bopItem, "A");
							bopDataList.add(newBopData);
						}
					}
					sol_Input_Parts.remove(key);
				}
			}
		}

		HashMap<String, TCComponent> sol_Output_Parts = addOutputPart();
		if (sol_Output_Parts != null && sol_Output_Parts.size() > 0) {
			Set<String> add_Output_keys = sol_Output_Parts.keySet();
			if (sol_Input_Parts != null && sol_Input_Parts.size() > 0) {
				TCComponent[] inputLines = sol_Input_Parts.values().toArray(new TCComponent[0]);
				NodeInfo[] bopLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, topBOPLine);
				if (bopLinkedLines != null && bopLinkedLines.length > 0) {
					for (String addLine : add_Output_keys) {
						TCComponent add_Ouput_Part = sol_Output_Parts.get(addLine);
						for (NodeInfo addInputLine : bopLinkedLines) {
							TCComponent add_Input_Part = addInputLine.originalNode;
							TCComponent[] foundBOPNode = addInputLine.foundNodes;

							UnitBOMBOPModel newBomData = new UnitBOMBOPModel();
							newBomData.generateBOMItem(add_Ouput_Part, add_Input_Part, "A");
							bomDataList.add(newBomData);
							for (TCComponent bopItem : foundBOPNode) {
								UnitBOMBOPModel newBopData = new UnitBOMBOPModel();
								newBopData.generateBOPItem(bopItem, "A");
								bopDataList.add(newBopData);
							}
						}
						sol_Output_Parts.remove(addLine);
					}
				}
			}
		}
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
}
