package com.vinfast.sap.bom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class FindConnectedLines {
	HashMap<TCComponent, TCComponent[]> BOMLines = new HashMap<TCComponent, TCComponent[]>();

	public HashMap<TCComponent, TCComponent[]> getBOMLines() {
		return BOMLines;
	}

	public void setBOMLines(TCComponent parent, TCComponent[] childLines) {
		BOMLines.put(parent, childLines);
	}

	public HashMap<TCComponent, ArrayList<BOMBOPData>> inBOP(BOMManager BOMManager,
			HashMap<TCComponent, TCComponent[]> parentChildLines, int noOfLinks) throws TCException {
		HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = new HashMap<TCComponent, ArrayList<BOMBOPData>>();
		boolean isLinkError = false;
		int iterator = 0;
		List<TCComponent> bomlinesWithoutBOP = new LinkedList<TCComponent>();
		Map<String, Integer> parentChildLinesNum = new HashMap<String, Integer>();
		try {
			TCSession session = BOMManager.getSession();
			TCComponent target = BOMManager.getBOPTraverseLine();
			CoreService structService = CoreService.getService(session);
			FindNodeInContextInputInfo[] findNodeInputInfo = new FindNodeInContextInputInfo[parentChildLines.size()];
			HashMap<String, TCComponent> isList = new HashMap<String, TCComponent>();
			for (TCComponent parentLine : parentChildLines.keySet()) {
				String parentID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
				parentID = parentID.substring(0, parentID.indexOf("-"));
				isList.put(parentID, parentLine);

				TCComponent[] childLines = parentChildLines.get(parentLine);
				String clientID = parentID;
				parentChildLinesNum.put(parentID, Integer.valueOf(childLines.length));
				findNodeInputInfo[iterator] = new FindNodeInContextInputInfo();
				findNodeInputInfo[iterator].clientID = clientID;
				findNodeInputInfo[iterator].context = target;
				findNodeInputInfo[iterator].nodes = childLines;
				findNodeInputInfo[iterator].allContexts = false;
				findNodeInputInfo[iterator].byIdOnly = true;
				findNodeInputInfo[iterator].relationDepth = 0;
				findNodeInputInfo[iterator].relationDirection = 1;
				findNodeInputInfo[iterator].relationTypes = new String[] { "FND_TraceLink" };
				iterator++;
			}

			FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(findNodeInputInfo);
			ServiceData serviceData = InContextInputResponse.serviceData;
			if (serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);
			} else {
				FoundNodesInfo[] foundLinkedLines = InContextInputResponse.resultInfo;
				if (foundLinkedLines.length != 0) {
					for (FoundNodesInfo foundParentLines : foundLinkedLines) {
						isLinkError = false;
						String parentID = foundParentLines.clientID;
						Integer bomLinesNum = parentChildLinesNum.get(parentID);
						ArrayList<BOMBOPData> childLinesList = new ArrayList<BOMBOPData>();
						NodeInfo[] childLinkedNodes = foundParentLines.resultNodes;
						if (childLinkedNodes.length == bomLinesNum) {
							BOMBOPData childLineMap = null;
							for (NodeInfo childNode : childLinkedNodes) {
								TCComponent BOM = childNode.originalNode;
								TCComponent[] BOP = childNode.foundNodes;

								if (BOP.length == noOfLinks) {
									childLineMap = new BOMBOPData();
									childLineMap.setMBOMLine(BOM);
									childLineMap.setMBOPLine(BOP);
									childLinesList.add(childLineMap);
								} else if (BOP.length == 0) {
									bomlinesWithoutBOP.clear();
									bomlinesWithoutBOP.add(BOM);

									Map<TCComponent, List<TCComponent>> bomsAndBops = findLines(BOMManager, target,
											bomlinesWithoutBOP);
									for (Entry<TCComponent, List<TCComponent>> bomAndBops : bomsAndBops.entrySet()) {
										TCComponent bom = bomAndBops.getKey();
										List<TCComponent> bops = bomAndBops.getValue();
										if (bops.size() == noOfLinks) {
											childLineMap = new BOMBOPData();
											childLineMap.setMBOMLine(bom);
											childLineMap.setMBOPLine(bops.toArray(new TCComponent[0]));
											childLinesList.add(childLineMap);
										} else {
											String partID = BOM
													.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
											String BOMID = BOM.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
											String[] printValues = new String[] {
													Integer.toString(BOMManager.getSerialNo()), parentID, partID, BOMID,
													"Linked to " + BOP.length
															+ " lines in BOP. Please correct BOM to BOP link.",
													"-", "Error" };
											BOMManager.printReport("PRINT", printValues);
											isLinkError = true;
											BOMManager.setError(isLinkError);
										}
									}
								} else {
									String partID = BOM.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
									String BOMID = BOM.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
									String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()),
											parentID, partID, BOMID, "Linked to " + BOP.length
													+ " lines in BOP. Please correct BOM to BOP link.",
											"-", "Error" };
									BOMManager.printReport("PRINT", printValues);
									isLinkError = true;
									BOMManager.setError(isLinkError);
								}
							}
						} else {
							String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), parentID,
									"", "", "Search " + bomLinesNum.toString() + " BOM lines but found "
											+ childLinkedNodes.length + " BOP lines.",
									"-", "Error" };
							BOMManager.printReport("PRINT", printValues);
							isLinkError = true;
							BOMManager.setError(isLinkError);
						}

						if (isLinkError == false) {
							parentChildDataMap.put(isList.get(parentID), childLinesList);
						}
					}
				}
			}

			if (isLinkError) {
				return null;
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return parentChildDataMap;
	}

	private Map<TCComponent, List<TCComponent>> findLines(BOMManager bomManager, TCComponent target,
			List<TCComponent> bomlinesWithoutBOP) throws TCException {
		Map<TCComponent, List<TCComponent>> result = new HashMap<TCComponent, List<TCComponent>>();

		for (TCComponent bomline : bomlinesWithoutBOP) {
			String itemID = bomline.getProperty(PropertyDefines.BOM_ITEM_ID);
			TCComponent[] foundLines = UIGetValuesUtility.searchStruture(bomManager.getSession(), itemID, target);
		//((TCComponentBOMLine)target).window().getRevisionRule()
			ArrayList<TCComponent> filteredFoundLInes = new ArrayList<TCComponent>();
			if (foundLines != null) {
				String idInContext01 = bomline.getProperty("bl_abs_occ_id");
				System.out.println(idInContext01 + " id context to find");
				for (TCComponent foundLine : foundLines) {
					String idInContext02 = foundLine.getProperty("bl_abs_occ_id");
					System.out.println(idInContext02);
					if (idInContext01.compareTo(idInContext02) == 0) {
						filteredFoundLInes.add(foundLine);
					}
				}

				result.put(bomline, filteredFoundLInes);
			}
		}

		return result;
	}

	public HashMap<TCComponent, ArrayList<BOMBOPData>> inBOM(BOMManager BOMManager,
			HashMap<TCComponent, TCComponent[]> parentChildLines, int noOfLinks) throws TCException {
		HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = new HashMap<TCComponent, ArrayList<BOMBOPData>>();
		boolean isLinkError = false;
		int iterator = 0;
		Map<String, Integer> parentChildLinesNum = new HashMap<String, Integer>();
		List<TCComponent> bopLinesWithoutBOM = new LinkedList<TCComponent>();
		try {
			TCSession session = BOMManager.getSession();
			TCComponent target = BOMManager.getMBOMTraverseLine();
			CoreService structService = CoreService.getService(session);
			FindNodeInContextInputInfo[] findNodeInputInfo = new FindNodeInContextInputInfo[parentChildLines.size()];
			HashMap<String, TCComponent> isList = new HashMap<String, TCComponent>();
			for (TCComponent parentLine : parentChildLines.keySet()) {
				String parentID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
				parentID = parentID.substring(0, parentID.indexOf("-"));
				isList.put(parentID, parentLine);

				TCComponent[] childLines = parentChildLines.get(parentLine);
				parentChildLinesNum.put(parentID, Integer.valueOf(childLines.length));
				String clientID = parentID;
				findNodeInputInfo[iterator] = new FindNodeInContextInputInfo();
				findNodeInputInfo[iterator].clientID = clientID;
				findNodeInputInfo[iterator].context = target;
				findNodeInputInfo[iterator].nodes = childLines;
				findNodeInputInfo[iterator].allContexts = false;
				findNodeInputInfo[iterator].byIdOnly = true;
				findNodeInputInfo[iterator].relationDepth = 0;
				findNodeInputInfo[iterator].relationDirection = 1;
				findNodeInputInfo[iterator].relationTypes = new String[] { "FND_TraceLink" };
				iterator++;
			}

			FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(findNodeInputInfo);
			ServiceData serviceData = InContextInputResponse.serviceData;
			if (serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);
			} else {
				FoundNodesInfo[] foundLinkedLines = InContextInputResponse.resultInfo;
				if (foundLinkedLines.length != 0) {
					for (FoundNodesInfo foundParentLines : foundLinkedLines) {
						ArrayList<TCComponent> childBOMList = null;
						isLinkError = false;
						String parentID = foundParentLines.clientID;
						ArrayList<BOMBOPData> childLinesList = new ArrayList<BOMBOPData>();
						NodeInfo[] childLinkedNodes = foundParentLines.resultNodes;
						Integer bomLinesNum = parentChildLinesNum.get(parentID);
						if (bomLinesNum == childLinkedNodes.length) {
							if (childLinkedNodes.length > 0) {
								childBOMList = new ArrayList<TCComponent>();
								BOMBOPData childLineMap = null;
								for (NodeInfo childNode : childLinkedNodes) {
									TCComponent[] BOM = childNode.foundNodes;
									TCComponent BOP = childNode.originalNode;

									if (BOM.length == noOfLinks) {
										childLineMap = new BOMBOPData();
										childLineMap.setMBOMLine(BOM[0]);
										childLineMap.setMBOPLine(new TCComponent[] { BOP });
										childBOMList.add(BOM[0]);
										childLinesList.add(childLineMap);
									} else if (BOM.length == 0) { 
										bopLinesWithoutBOM.clear();
										bopLinesWithoutBOM.add(BOP);

										Map<TCComponent, List<TCComponent>> bomsAndBops = findLines(BOMManager, target,
												bopLinesWithoutBOM);
										for (Entry<TCComponent, List<TCComponent>> bomAndBops : bomsAndBops.entrySet()) {
											TCComponent bom = bomAndBops.getKey();
											List<TCComponent> bops = bomAndBops.getValue();
											if (bops.size() == noOfLinks) {
												childLineMap = new BOMBOPData();
												childLineMap.setMBOMLine(bom);
												childLineMap.setMBOPLine(bops.toArray(new TCComponent[0]));
												childLinesList.add(childLineMap);
											} else {
												String partID = BOP.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
												String BOMID = BOP.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
												String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()),
														parentID, partID, BOMID, "Linked to " + BOM.length
																+ " lines in BOM. Please correct BOM to BOP link.",
														"-", "Error" };
												BOMManager.printReport("PRINT", printValues);
												isLinkError = true;
												BOMManager.setError(isLinkError);
											}
										}
									} else {
										String partID = BOP.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
										String BOMID = BOP.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
										String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()),
												parentID, partID, BOMID, "Linked to " + BOM.length
														+ " lines in BOM. Please correct BOM to BOP link.",
												"-", "Error" };
										BOMManager.printReport("PRINT", printValues);
										isLinkError = true;
										BOMManager.setError(isLinkError);
									}
								}
							}
						} else {
							String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), parentID,
									"", "", "Search " + bomLinesNum.toString() + " BOP lines but found "
											+ childLinkedNodes.length + " BOM lines.",
									"-", "Error" };
							BOMManager.printReport("PRINT", printValues);
							isLinkError = true;
							BOMManager.setError(isLinkError);
						}

						if (isLinkError == false) {
							if (childBOMList != null) {
								TCComponent[] childLines = new TCComponent[childBOMList.size()];
								childLines = childBOMList.toArray(childLines);
								setBOMLines(isList.get(parentID), childLines);
							}

							parentChildDataMap.put(isList.get(parentID), childLinesList);
						}
					}
				} else {
					
				}
			}

			if (isLinkError) {
				return null;
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return parentChildDataMap;
	}
}
