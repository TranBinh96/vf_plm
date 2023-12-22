package com.vinfast.car.sap.unitbom;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedConfigParameters;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedInputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedOutputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedParentInfo;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.services.ReadXMLInputFile;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class UnitTransfer {
	TCSession session = null;
	TCComponentItemRevision tcComp = null;
	public void getOutputAssemblies(AIFComponentContext[] childLines, ArrayList<TCComponentBOMLine> output, ArrayList<TCComponentBOMLine> input) {
		try {
			for (AIFComponentContext child : childLines) {
				TCComponentBOMLine childLine = (TCComponentBOMLine) child.getComponent();
				String manufacturing_code = childLine.getProperty("VF4_manuf_code");
				if (manufacturing_code.equals("Output")) {
					output.add(childLine);
				}
				if (manufacturing_code.equals("Input")) {
					input.add(childLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<TCComponentBOMLine> getInputAssemblies(AIFComponentContext[] childLines) {
		ArrayList<TCComponentBOMLine> inPutAssemblies = new ArrayList<TCComponentBOMLine>();
		try {
			for (AIFComponentContext child : childLines) {
				TCComponentBOMLine childLine = (TCComponentBOMLine) child.getComponent();
				String manufacturing_code = childLine.getProperty("VF4_manuf_code");

				if (manufacturing_code.equals("Input")) {
					inPutAssemblies.add(childLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return inPutAssemblies;
	}

	public boolean transferBOM(TCComponent child_Item, TCComponent parentLine, HashMap<String, String> BOM_Values, int count, String logFolder) {
		new Logger();
		String[] prop_Values = null;
		boolean toProcess = true;
		ReadXMLInputFile readFile = new ReadXMLInputFile();

		try {
			TCComponentBOMLine BOMLine = (TCComponentBOMLine) child_Item;
			TCComponentItemRevision child_Rev = BOMLine.getItemRevision();
			prop_Values = child_Item.getProperties(new String[] { "bl_rev_item_id", "bl_rev_object_type" });
			String fileName = readFile.getXML(child_Rev);

			if (fileName == null) {
				String[] response = new String[] { prop_Values[0], "Traversal file locked. Please relogin.", "Error" };
				Logger.writeResponse(response, logFolder, "BOM");
				return false;
			} else {
				InputStream traverse_File = readFile.loadXML(fileName);
				if (traverse_File == null) {
					String[] response = new String[] { prop_Values[0], "Property file parser error. Please contact Teamcenter Admin.", "Error" };
					Logger.writeResponse(response, logFolder, "BOM");
					MessageBox.post("Property file parser error. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
					return false;
				} else {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(traverse_File);
					doc.getDocumentElement().normalize();
					NodeList nList = doc.getElementsByTagName(SAPURL.ASSY_BOM_HEADER_TAG);

					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element tagElement = (Element) nNode;
							NodeList dataList = tagElement.getChildNodes();
							for (int jnx = 0; jnx < dataList.getLength(); jnx++) {
								Node dataNode = dataList.item(jnx);
								if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
									Element dataElement = (Element) dataNode;
									NodeList attributes = dataElement.getChildNodes();
									for (int knx = 0; knx < attributes.getLength(); knx++) {
										Node attribute = attributes.item(knx);
										if (attribute.getNodeType() == Node.ELEMENT_NODE) {
											Element attributeElement = (Element) attribute;

											String sap_tag = attributeElement.getAttribute("erp_tag");
											String plm_tag = attributeElement.getAttribute("plm_tag");
											String relation = attributeElement.getAttribute("relation");
											String is_mandatory = attributeElement.getAttribute("mandatory");

											String propTagValue = "";

											if (relation.equalsIgnoreCase("PARENT")) {
												propTagValue = parentLine.getProperty(plm_tag).trim();
											} else if (relation.equalsIgnoreCase("BOMLINE")) {
												propTagValue = child_Item.getProperty(plm_tag).trim();
												if (sap_tag.equals("OPTION")) {
													if (propTagValue.equals("") == false) {
														propTagValue = propTagValue.substring(0, propTagValue.length());
														Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(propTagValue);
														while (m.find()) {
															propTagValue = propTagValue.replace(m.group(1),
																	BOM_Values.get("PLATFORM") + "_"
																			+ BOM_Values.get("MODELYEAR"));
														}
													}
												}
												if (sap_tag.equals("OPTION")) {
													if (propTagValue.equals("")) {
														propTagValue = "1";
													}
												}
												if (sap_tag.equalsIgnoreCase("BOMLINEID")) {

													if (propTagValue.length() != 0) {
														for (int i = propTagValue.length(); i < 4; i++) {
															propTagValue = "0" + propTagValue;
														}
													}
												}
											} else if (relation.equalsIgnoreCase("HASHMAP")) {
												propTagValue = BOM_Values.get(plm_tag);
											} else if (relation.equalsIgnoreCase("AUTOMATIC")) {
												propTagValue = UIGetValuesUtility.getSequenceID();
											} else {
												propTagValue = "";
											}

											if (BOM_Values.containsKey(sap_tag) == false) {
												if (is_mandatory.equals("Yes") && propTagValue.equals("")) {
													String[] response = new String[] { prop_Values[0],
															"Traversal file locked. Please relogin.", "Error" };
													Logger.writeResponse(response, logFolder, "BOM");
													toProcess = false;
													break;
												} else {
													BOM_Values.put(sap_tag, propTagValue.toUpperCase());
												}
											}
										}
									}
								}
							}
						}
					}
					if (toProcess == false) {
						return false;
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public ArrayList<HashMap<String, String>> transferBOP(TCComponent childBOMLine, HashMap<String, String> Values, TCComponentBOMLine BOPTopLine, String subGroup, int count, StringBuilder strBuilder) {
		ArrayList<HashMap<String, String>> BOPMap = new ArrayList<HashMap<String, String>>();
		new Logger();
		UIGetValuesUtility utility = new UIGetValuesUtility();
		HashMap<String, String> bomLineValues = null;
		ReadXMLInputFile readFile = new ReadXMLInputFile();
		TCSession session = childBOMLine.getSession();
		boolean toProcess = true;

		try {
			String[] prop_Values = childBOMLine.getProperties(new String[] { "bl_rev_item_id", "bl_rev_object_type" });
			TCComponentBOMLine childLine = (TCComponentBOMLine) childBOMLine;
			TCComponentItemRevision itemRevision = childLine.getItemRevision();
			String fileName = readFile.getXML(itemRevision);
			if (fileName == null) {
				Logger.bufferResponse("PRINT", new String[] { prop_Values[0], "", "Parser file name unknown. Please contact Teamcenter Admin.", "", "Error" }, strBuilder);
				return null;
			} else {
				InputStream traverse_File = readFile.loadXML(fileName);
				if (traverse_File == null) {
					Logger.bufferResponse("PRINT", new String[] { prop_Values[0], "", "Property file parser error. Please contact Teamcenter Admin.", "", "Error" }, strBuilder);
					return null;
				}

				NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOP(session, new TCComponent[] { childBOMLine }, BOPTopLine, subGroup, count, strBuilder);

				TCComponent[] foundBOPLine = bopNode[0].foundNodes;
				if (foundBOPLine != null) {
					TCComponentBOMLine bopLine = (TCComponentBOMLine) foundBOPLine[0];
					TCComponentBOMLine operationLine = (TCComponentBOMLine) bopLine.getReferenceProperty("bl_parent");
					TCComponentItemRevision operationRevision = operationLine.getItemRevision();

					if (operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder.parse(traverse_File);
						doc.getDocumentElement().normalize();
						NodeList nList = doc.getElementsByTagName(SAPURL.ASSY_BOP_HEADER_TAG);

						bomLineValues = new HashMap<String, String>();
						bomLineValues.putAll(Values);
						for (int temp = 0; temp < nList.getLength(); temp++) {
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
								Element tagElement = (Element) nNode;
								NodeList dataList = tagElement.getChildNodes();
								for (int jnx = 0; jnx < dataList.getLength(); jnx++) {
									Node dataNode = dataList.item(jnx);
									if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
										Element dataElement = (Element) dataNode;
										NodeList attributes = dataElement.getChildNodes();
										for (int knx = 0; knx < attributes.getLength(); knx++) {
											Node attribute = attributes.item(knx);
											if (attribute.getNodeType() == Node.ELEMENT_NODE) {
												Element attributeElement = (Element) attribute;

												String sap_tag = attributeElement.getAttribute("erp_tag");
												String plm_tag = attributeElement.getAttribute("plm_tag");
												String type_tag = attributeElement.getAttribute("objectType");
												String relation = attributeElement.getAttribute("relation");
												String is_mandatory = attributeElement.getAttribute("mandatory");

												String propTagValue = "";
												if (bomLineValues.containsKey(sap_tag) == false) {
													if (relation.equalsIgnoreCase("PARENT")) {
														TCComponentBOMLine BOMLine = null;
														if (sap_tag.equals("WORKSTATION")) {
															propTagValue = UIGetValuesUtility.getWorkStationID(operationLine, plm_tag);
														} else {
															BOMLine = utility.getParentBOMLine(bopLine, type_tag);
															propTagValue = BOMLine.getProperty(plm_tag).trim();
														}

														if (BOMLine != null) {

														}

														if (sap_tag.equalsIgnoreCase("MESBOPINDICATOR")) {
															if (propTagValue.equals("")
																	|| propTagValue.equalsIgnoreCase("NA")
																	|| propTagValue.equalsIgnoreCase(
																			"Automatic Consumption")) {
																propTagValue = "N";
															} else {
																propTagValue = "Y";
															}
														}

														if (sap_tag.equalsIgnoreCase("LINESUPPLYMETHOD")) {
															if (propTagValue.equals("") || propTagValue.equalsIgnoreCase("NA")) {
																propTagValue = "JIS";
															}
														}
													} else if (relation.equalsIgnoreCase("BOMLINE")) {
														propTagValue = bopLine.getProperty(plm_tag).trim();
														if (sap_tag.equals("OPTION")) {
															if (propTagValue.equals("") == false) {
																propTagValue = propTagValue.substring(0, propTagValue.length());
																Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(propTagValue);
																while (m.find()) {
																	propTagValue = propTagValue.replace(m.group(1), bomLineValues.get("PLATFORM") + "_" + bomLineValues.get("MODELYEAR"));
																}
															}
														}
														if (sap_tag.equalsIgnoreCase("BOMLINEID")) {

															if (propTagValue.length() != 0) {
																for (int i = propTagValue.length(); i < 4; i++) {
																	propTagValue = "0" + propTagValue;
																}
															}
														}
													} else if (relation.equalsIgnoreCase("AUTOMATIC")) {
														propTagValue = UIGetValuesUtility.getSequenceID();
													} else if (relation.equalsIgnoreCase("HASHMAP")) {
														propTagValue = bomLineValues.get(plm_tag);
													} else {
														propTagValue = "";
													}

													if (is_mandatory.equals("Yes") && propTagValue.equals("")) {
														Logger.bufferResponse("PRINT", new String[] { prop_Values[0], "", sap_tag + " is mandatory", "", "Error" }, strBuilder);
														toProcess = false;
														break;
													} else {
														bomLineValues.put(sap_tag, propTagValue.toUpperCase());
													}
												}
											}
										}
									}
								}
							}
						}
						if (toProcess == false) {
							return null;
						} else {
							BOPMap.add(bomLineValues);
						}
					} else {
						Logger.bufferResponse("PRINT", new String[] { prop_Values[0], "", "BOPLine is not under operation.", "", "Error" }, strBuilder);
					}
				} else {
					Logger.bufferResponse("PRINT", new String[] { prop_Values[0], "", "BOM not linked to BOP.", "", "Error" }, strBuilder);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BOPMap;
	}

	public HashMap<String, String> transferBOP(DataManagementService dmCoreService, TCComponent childBOPLine, HashMap<String, String> Values, String subGroup, int count, StringBuilder strBuilder) {
		HashMap<String, String> dataMap = null;
		new Logger();

		try {
			TCComponentBOMLine BOPLine = (TCComponentBOMLine) childBOPLine;
			dmCoreService.getProperties(new TCComponent[] { BOPLine }, new String[] { "bl_parent" });
			TCComponentBOMLine operationLine = (TCComponentBOMLine) BOPLine.getReferenceProperty("bl_parent");
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();
			if (operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {
				dataMap = new HashMap<String, String>();
				dmCoreService.getProperties(new TCComponent[] { operationLine }, new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "vf5_line_supply_method", "vf4_operation_type" });

				String operationID = operationLine.getPropertyDisplayableValue("bl_item_item_id");
				String operationRev = operationLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
				String MESInd = operationLine.getPropertyDisplayableValue("vf4_operation_type");

				if (MESInd.equals("") || MESInd.equalsIgnoreCase("NA")) {
					MESInd = "N";
				} else {
					MESInd = "Y";
				}

				String JIS = operationLine.getPropertyDisplayableValue("vf5_line_supply_method");
				if (JIS.equals("") || JIS.equalsIgnoreCase("NA")) {
					JIS = "JIS";
				}

				String workStation = UIGetValuesUtility.getWorkStationID(operationLine, "bl_rev_object_name");
				if (workStation.equals("")) {
					Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), subGroup, Values.get("CHILDPART"), Values.get("BOMLINEID"), "WorkStation ID is wrong or incorrect.", "", "Error" }, strBuilder);
					count++;
					return null;
				}

				dataMap.put("BOPID", operationID);
				dataMap.put("REVISION", operationRev);
				dataMap.put("LINESUPPLYMETHOD", JIS);
				dataMap.put("MESBOPINDICATOR", MESInd);
				dataMap.put("WORKSTATION", workStation);
			} else {
				Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), subGroup, Values.get("CHILDPART"), Values.get("BOMLINEID"), "BOPLine is not under operation.", "", "Error" }, strBuilder);
				count++;
				return null;
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataMap;
	}

	public ArrayList<TCComponent> traverseAssembly(TCComponentBOMWindow wnd, TCComponentItemRevision itemRevision, String plant_Code, String IDs) {
		UIGetValuesUtility utility = new UIGetValuesUtility();
		ArrayList<TCComponent> operations = null;
		try {
			DataManagementService dmService = DataManagementService.getService(session);

			WhereUsedConfigParameters configparam = new WhereUsedConfigParameters();
			configparam.boolMap.put("whereUsedPreciseFlag", false);
			configparam.intMap.put("numLevels", new BigInteger("-1"));
			configparam.tagMap.put("revision_rule", null);

			WhereUsedInputData inputData = new WhereUsedInputData();
			inputData.clientId = "";
			inputData.inputObject = itemRevision;
			inputData.useLocalParams = false;
			inputData.inputParams = configparam;

			WhereUsedResponse WUResponse = dmService.whereUsed(new WhereUsedInputData[] { inputData }, configparam);
			WhereUsedOutputData[] outputData = WUResponse.output;
			WhereUsedParentInfo[] info = outputData[0].info;
			TCComponentItemRevision topLineRevision = (TCComponentItemRevision) info[0].parentObject;
			TCComponentBOMLine topLine = wnd.setWindowTopLine(topLineRevision.getItem(), topLineRevision, null, null);
			AIFComponentContext[] childContext = topLine.getChildren();
			for (AIFComponentContext childItem : childContext) {
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) childItem.getComponent();
				TCComponentItemRevision itemRev = childBOMLine.getItemRevision();

				if (itemRev.equals(itemRevision)) {
					if (utility.isPurchaseMaterial(itemRevision, plant_Code) == false) {
						operations = UIGetValuesUtility.searchPartsInStruture(wnd.getSession(), new String[] { IDs }, childBOMLine);
						break;
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return operations;
	}

	public void CollectAssembly(TCComponentBOMLine BOMLine, HashMap<String, TCComponentBOMLine> assembiles) {
		try {
			AIFComponentContext[] childContext = BOMLine.getChildren();
			for (int i = 0; i < childContext.length; i++) {
				TCComponentBOMLine childBomLine = (TCComponentBOMLine) childContext[i].getComponent();
				if (childBomLine.hasChildren() == true) {
					String ID = childBomLine.getProperty("bl_item_item_id");
					assembiles.put(ID, childBomLine);
					CollectAssembly(childBomLine, assembiles);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
}
