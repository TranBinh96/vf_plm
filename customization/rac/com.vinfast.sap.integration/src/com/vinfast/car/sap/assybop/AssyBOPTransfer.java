package com.vinfast.car.sap.assybop;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.services.GetProperties;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.services.ReadXMLInputFile;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class AssyBOPTransfer extends AbstractHandler {

	TCSession session = null;
	TCComponent tcComp = null;
	GetProperties propClass = null;

	public AssyBOPTransfer() {

	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public TCComponentBOMLine getOperation(TCComponentBOMLine bopChild) {

		TCComponentBOMLine operationList = null;
		try {

			TCComponentBOMLine operation = (TCComponentBOMLine) bopChild.getReferenceProperty("bl_parent");

			if (operation != null && operation.getItemRevision().getType().equals("MEOPRevision")) {
				return operation;
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return operationList;
	}

	public HashMap<String, String> transferBOP(DataManagementService dmCoreService, TCComponent childBOPLine, HashMap<String, String> Values, String subGroup, HashMap<String, TCComponentBOMLine> operationsMap, int count, StringBuilder strBuilder) {

		HashMap<String, String> dataMap = null;
		new UIGetValuesUtility();
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

				operationsMap.put(operationID, operationLine);

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataMap;
	}

	public TCComponentBOMLine transferBOPItem(TCSession session, TCComponentBOMLine child_BOM, TCComponentBOMLine child_BOP, HashMap<String, String> bomLineValues, int count, String logFolder) {

		new Logger();
		ReadXMLInputFile readFile = new ReadXMLInputFile();
		TCComponentBOMLine operation = null;
		UIGetValuesUtility utility = new UIGetValuesUtility();
		boolean toProcess = true;

		try {

			String[] prop_Values = { "bl_rev_item_id", "bl_rev_object_type", "VF4_bomline_id", "VF3_purchase_lvl_vf", "VF4_manuf_code" };

			String[] bomlinePropValues = child_BOM.getProperties(prop_Values);

			if (child_BOM.getProperty("VF4_manuf_code").equals("Parent Part") == true) {

				TCComponentBOMLine parent = (TCComponentBOMLine) child_BOM.getReferenceProperty("bl_parent");
				bomLineValues.put("HEADERPART", parent.getProperty("bl_item_item_id").trim());
				parent = (TCComponentBOMLine) parent.getReferenceProperty("bl_parent");

				bomLineValues.put("TOPLEVELPART", parent.getProperty("bl_item_item_id").trim());
				bomLineValues.put("PHANTOM", "Yes");

			} else {
				TCComponentBOMLine parent = (TCComponentBOMLine) child_BOM.getReferenceProperty("bl_parent");
				bomLineValues.put("TOPLEVELPART", parent.getProperty("bl_item_item_id").trim());
				bomLineValues.put("HEADERPART", parent.getProperty("bl_item_item_id").trim());
				bomLineValues.put("PHANTOM", "No");
			}

			TCComponentItemRevision itemRevision = child_BOP.getItemRevision();

			operation = getOperation(child_BOP);

			if (operation == null) {

				return operation;
			}

			String fileName = readFile.getXML(itemRevision);

			if (fileName == null) {

				MessageBox.post("Parser file name unknown. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;

			} else {

				InputStream traverse_File = readFile.loadXML(fileName);

				if (traverse_File == null) {

					MessageBox.post("Property file parser error. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
					return null;

				}

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(traverse_File);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName(SAPURL.ASSY_BOP_HEADER_TAG);

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

										if (relation.equalsIgnoreCase("PARENT")) {

											TCComponentBOMLine BOMLine = null;

											if (sap_tag.equals("WORKSTATION")) {

												propTagValue = UIGetValuesUtility.getWorkStationID(operation, plm_tag);
											} else {
												BOMLine = utility.getParentBOMLine(child_BOP, type_tag);
												propTagValue = BOMLine.getProperty(plm_tag).trim();
											}

											if (BOMLine != null) {

											}

											if (sap_tag.equalsIgnoreCase("MESBOPINDICATOR")) {

												if (propTagValue.equals("") || propTagValue.equalsIgnoreCase("NA")) {
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

											propTagValue = child_BOP.getProperty(plm_tag).trim();

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

										if (bomLineValues.containsKey(sap_tag) == false) {
											if (is_mandatory.equals("Yes") && propTagValue.equals("")) {
												String[] response = new String[] { bomlinePropValues[0], "", sap_tag + " is mandatory", "", "Error" };
												Logger.writeResponse(response, logFolder, "BOM");
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
			}
			if (toProcess == false) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return operation;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String, String>> changeInWorkStation(LinkedHashMap<String, LinkedHashMap<String, String>> old_items, LinkedHashMap<String, LinkedHashMap<String, String>> new_items) {

		LinkedHashMap<String, LinkedHashMap<String, String>> oldBOP = (LinkedHashMap<String, LinkedHashMap<String, String>>) old_items.clone();
		LinkedHashMap<String, LinkedHashMap<String, String>> newBOP = (LinkedHashMap<String, LinkedHashMap<String, String>>) new_items.clone();
		LinkedHashMap<String, LinkedHashMap<String, String>> differentWorkStation = new LinkedHashMap<String, LinkedHashMap<String, String>>();

		for (String key : newBOP.keySet()) {

			try {

				LinkedHashMap<String, String> newLine = newBOP.get(key);
				LinkedHashMap<String, String> oldLine = oldBOP.get(key);

				if (oldLine != null) {

					String newLineBOP = newLine.get("BOPID");
					String oldLineBOP = oldLine.get("BOPID");

					String newLineWS = newLine.get("WORKSTATION");
					String oldLineWS = oldLine.get("WORKSTATION");

					String newMESIndicator = newLine.get("MESBOPINDICATOR");
					String oldMESIndicator = oldLine.get("MESBOPINDICATOR");

					if ((newLineBOP.equals(oldLineBOP)) == false || (newLineWS.equals(oldLineWS)) == false || (newMESIndicator.equals(oldMESIndicator)) == false) {
						differentWorkStation.put(key, newLine);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(key);
		}

		return differentWorkStation;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String, String>> changeInOperationRevision(LinkedHashMap<String, LinkedHashMap<String, String>> old_items, LinkedHashMap<String, LinkedHashMap<String, String>> new_items) {

		LinkedHashMap<String, LinkedHashMap<String, String>> oldBOP = (LinkedHashMap<String, LinkedHashMap<String, String>>) old_items.clone();
		LinkedHashMap<String, LinkedHashMap<String, String>> newBOP = (LinkedHashMap<String, LinkedHashMap<String, String>>) new_items.clone();
		LinkedHashMap<String, LinkedHashMap<String, String>> differentOperationRevision = new LinkedHashMap<String, LinkedHashMap<String, String>>();

		for (String key : newBOP.keySet()) {
			try {

				LinkedHashMap<String, String> newLine = newBOP.get(key);
				LinkedHashMap<String, String> oldLine = oldBOP.get(key);

				if (oldLine != null) {

					String newLineOP = newLine.get("BOPID");
					String oldLineOP = oldLine.get("BOPID");

					String newLineWS = newLine.get("WORKSTATION");
					String oldLineWS = oldLine.get("WORKSTATION");

					String newLineRev = newLine.get("REVISION");
					String oldLineRev = oldLine.get("REVISION");

					if ((newLineOP.equals(oldLineOP) == true) && (newLineWS.equals(oldLineWS) == true) && (newLineRev.equals(oldLineRev) == false)) {
						differentOperationRevision.put(key, newLine);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return differentOperationRevision;
	}

	public boolean transferAssemblyBOP(TCSession session, TCComponentBOMLine operation, HashMap<String, String> bomLineValues, String logFolder) {

		new Logger();
		boolean toProcess = true;
		try {
			TCComponentItemRevision itemRevision = operation.getItemRevision();
			ReadXMLInputFile readFile = new ReadXMLInputFile();
			String fileName = readFile.getXML(itemRevision);

			if (fileName == null) {

				String[] response = new String[] { itemRevision.getProperty("item_id"), "Traversal file locked. Please relogin.", "Error" };
				Logger.writeResponse(response, logFolder, "BOM");
				return false;
			} else {

				InputStream traverse_File = readFile.loadXML(fileName);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(traverse_File);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName(SAPURL.ASSY_BOP_HEADER_TAG);

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

										if (relation.equalsIgnoreCase("HASHMAP")) {

											propTagValue = bomLineValues.get(plm_tag);

										} else if (relation.equalsIgnoreCase("PARENT")) {

											String action = bomLineValues.get("ACTION");
											if (sap_tag.equals("WORKSTATION")) {
												if (action.equals("A")) {
													propTagValue = getWorkStationID(operation, plm_tag);
												} else {
													propTagValue = getDeletedWorkStation(operation.getItemRevision());
												}

											} else {
												TCComponentBOMLine parentBOMLine = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
												if (parentBOMLine == null) {
													propTagValue = "";
												} else {
													if (parentBOMLine.getType().equalsIgnoreCase(type_tag)) {

														propTagValue = parentBOMLine.getProperty(plm_tag).trim();
													}
												}
											}

										} else if (relation.equalsIgnoreCase("BOMLINE")) {

											if (type_tag.equals("Operation Revision")) {
												propTagValue = operation.getProperty(plm_tag).trim();

												if (sap_tag.equals("MESBOPINDICATOR")) {

													if (propTagValue.equals("") || propTagValue.equalsIgnoreCase("NA")) {
														propTagValue = "N";
													} else {
														propTagValue = "Y";
													}

												}

												if (sap_tag.equals("LINESUPPLYMETHOD") && propTagValue.equals("")) {

													propTagValue = "JIS";
												}

												if (sap_tag.equals("SELECTIONSTRING")) {

													if (propTagValue.equals("") == false) {

														propTagValue = propTagValue.substring(0, propTagValue.length());

														Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(propTagValue);
														while (m.find()) {
															propTagValue = propTagValue.replace(m.group(1), bomLineValues.get("PLATFORM") + "_" + bomLineValues.get("MODELYEAR"));
														}
													}
												}
											} else {
												propTagValue = operation.getProperty(plm_tag).trim();
											}
										} else {

											if (sap_tag.equals("SEQUENCE")) {
												new UIGetValuesUtility();
												propTagValue = UIGetValuesUtility.getSequenceID();
											} else {
												propTagValue = "";
											}
										}

										if (bomLineValues.containsKey(sap_tag) == false) {

											bomLineValues.put(sap_tag, propTagValue.toUpperCase());
										}
										if (is_mandatory.equals("Yes") && sap_tag.equals("WORKSTATION") && propTagValue.equals("")) {

											String[] response = new String[] { bomLineValues.get("PARTNO"), "WORKSTATION is missing", "Error" };
											Logger.writeResponse(response, logFolder, "BOM");
											toProcess = false;
											break;

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

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return toProcess;
	}

	private String getDeletedWorkStation(TCComponentItemRevision operationRev) {
		// TODO Auto-generated method stub
		String workstationID = "";

		try {

			workstationID = operationRev.getProperty("vf3_transfer_to_sap");

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workstationID;
	}

	public String getWorkStationID(TCComponentBOMLine operation, String plm_tag) {
		// TODO Auto-generated method stub
		String workstationID = "";

		try {

			TCComponentBOMLine WS_BL = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
			String WS = WS_BL.getProperty("bl_rev_object_name");
			workstationID = WS.substring(WS.length() - 4);

			TCComponentBOMLine procLine = (TCComponentBOMLine) WS_BL.getReferenceProperty("bl_parent");
			workstationID = procLine.getProperty("bl_rev_object_name").substring(0, 2) + workstationID;
			TCComponent procShop = procLine.getReferenceProperty("bl_parent");
			workstationID = procShop.getProperty("bl_rev_object_name").substring(0, 2) + workstationID;

			TCComponent procPlant = procShop.getReferenceProperty("bl_parent");
			workstationID = procPlant.getProperty("bl_rev_object_name").substring(0, 4) + "_" + workstationID;

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}

	public TCComponent whereUsedMEOP(TCComponentBOMLine itemRev) {

		TCComponent usedParts = null;
		try {
			usedParts = itemRev.getReferenceProperty("bl_parent");
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return usedParts;
	}

	public void traverseBOPLines(AIFComponentContext tcComp) {

		try {
			if (tcComp.getComponent().getType().equals("BOMLine")) {
				TCComponentBOMLine BL = (TCComponentBOMLine) tcComp.getComponent();
				String BL_Type = BL.getProperty("bl_item_object_type");
				if (BL_Type.equals("MEOP")) {
					AIFComponentContext[] child_parts = BL.getChildren();
					for (int i = 0; i < child_parts.length; i++) {

						TCComponentBOMLine item = (TCComponentBOMLine) child_parts[i].getComponent();
						String child_Type = item.getProperty("bl_item_object_type");
						if (child_Type.equals("Car Part")) {

						}
					}
				}

			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getWSID(TCComponent wsObject) {
		// TODO Auto-generated method stub
		String workstationID = "";

		try {

			TCComponentBOMLine WS_BL = (TCComponentBOMLine) wsObject;
			String WS = WS_BL.getProperty("bl_rev_object_name");
			if (WS != null && WS.contains("-")) {
				workstationID = WS.replace("-", "");
			}

			TCComponent procLine = WS_BL.getReferenceProperty("bl_parent");

			TCComponent procShop = procLine.getReferenceProperty("bl_parent");
			workstationID = procShop.getProperty("bl_rev_object_name").substring(0, 2) + workstationID;

			TCComponent procPlant = procShop.getReferenceProperty("bl_parent");
			System.out.println(procPlant.getProperty("bl_rev_object_name"));
			workstationID = procPlant.getProperty("bl_rev_object_name").substring(0, 4) + "_" + workstationID;

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}
}
