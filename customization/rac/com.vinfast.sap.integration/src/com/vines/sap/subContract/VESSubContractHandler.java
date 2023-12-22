package com.vines.sap.subContract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.dialog.SAPTransfer_Dialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.BOMCompareReport;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFBOM;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class VESSubContractHandler extends AbstractHandler {
	private DataManagementService dmService = null;
	private SAPTransfer_Dialog dlg;
	private HashMap<TCComponentItemRevision, ArrayList<HashMap<String, String>>> transferRecords = null;
	private File logFile = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			VFResponse response = MCNValidator.validate(targetComponents);
			if (response.hasError()) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
			}else {
				
				TCComponentChangeItemRevision selectedObject = (TCComponentChangeItemRevision) targetComponents[0];
				TCSession session = selectedObject.getSession();
				dmService = DataManagementService.getService(session);

				String plant = selectedObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
				String MCN_SAPID = selectedObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
				TCComponent[] impactedShop = selectedObject.getContents(PropertyDefines.REL_IMPACT_SHOP);
				TCComponentItemRevision obj_Shop = (TCComponentItemRevision) impactedShop[0];
				String mainGroup = obj_Shop.getProperty(PropertyDefines.ITEM_NAME);

				dlg = new SAPTransfer_Dialog(new Shell());
				dlg.create();
				dlg.setTitle("Sub Contract BOM (Only BOM)");
				dlg.txtModel.setText(mainGroup);
				dlg.txtMCN.setText(MCN);
				dlg.txtPlant.setText(plant);
				dlg.cbServer.setText("PRODUCTION");
				dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						dlg.btnPrepare.setEnabled(false);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (prepareData(session, selectedObject) == false) {
									dlg.btnPrepare.setEnabled(false);
									dlg.btnAccept.setEnabled(true);
								} else {
									dlg.btnPrepare.setEnabled(true);
									dlg.btnAccept.setEnabled(false);
								}
							}
						});
					}
				});

				dlg.btnAccept.setEnabled(false);
				dlg.btnAccept.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						dlg.btnAccept.setEnabled(false);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								String cmd = event.getCommand().toString();
								OrganizationInformationAbstract serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, dlg.cbServer.getText(), session);
								transferRecords(serverInfo, session, selectedObject);
								//dlg.getShell().dispose();
							}
						});
					}
				});
				dlg.open();
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private boolean prepareData(TCSession session, TCComponentChangeItemRevision selectedObject) {
		
		String mcnNumber = dlg.txtMCN.getText();
		String plantCode = dlg.txtPlant.getText();
		String server = dlg.cbServer.getText();
		boolean hasError = false;
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnNumber);
		transferRecords = new HashMap<TCComponentItemRevision, ArrayList<HashMap<String, String>>>();
		BOMCompareReport bomcompare = new BOMCompareReport();
//		boolean isQA = false;
//		if (dlg.cbServer.getText().contentEquals("QA")) {
//			isQA = true;
//		}
		
		try {
			
			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
			if (impactedItems != null) {

				int count = 1;

				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("<html><body>");
				String[] printValues = new String[] { "User :" + session.getUserName(), "MCN :"+mcnNumber , "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
				Logger.bufferResponse("DETAILS", printValues, strBuilder);
				printValues = new String[] { "S.No", "SUB GROUP", "RECORD", "BOMLINE ID", "SAP BOM ID", "MESSAGE", "ACTION", "RESULT" };
				Logger.bufferResponse("HEADER", printValues, strBuilder);

				dmService.getProperties(impactedItems, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ITEM_REV_ID, PropertyDefines.REV_TO_SAP });
				ArrayList<TCComponentItemRevision> transferRevisions = new ArrayList<TCComponentItemRevision>();
				for (TCComponent impacted : impactedItems) {
					String itemID = impacted.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String itemRevID = impacted.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					if (server.equals("PRODUCTION")) {
						String transferToSAP = impacted.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
						if (transferToSAP.isEmpty()) {
							transferRevisions.add((TCComponentItemRevision) impacted);
						} else {
							printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, "", "", "", "Sub Group already transferred to SAP.", "", "Info" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
						}
					} else {
						transferRevisions.add((TCComponentItemRevision) impacted);
					}
				}

				if (transferRevisions.isEmpty() == false) {
					
					StructureManagementService SMService = StructureManagementService.getService(session);
					TCComponentRevisionRule RevisionRule = UIGetValuesUtility.getRevisionRule(PropertyDefines.REVISION_RULE_WORKING, session);
					
					for (TCComponentItemRevision solutionRevision : transferRevisions) {
						ArrayList<HashMap<String, String>> subGroupRecords = new ArrayList<HashMap<String, String>>();
						String itemID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
						String itemRevID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
						CreateBOMWindowsOutput[] bomWindowSolItems = VFBOM.openBOM(SMService, new TCComponent[] { solutionRevision }, RevisionRule);
						if(bomWindowSolItems != null) {
							for(CreateBOMWindowsOutput bomOutput : bomWindowSolItems) {
								TCComponentBOMLine solutionLine = (TCComponentBOMLine) bomOutput.bomLine;
								TCComponentItemRevision problemRevision = UIGetValuesUtility.getPreviousRevision(solutionRevision);
								if (problemRevision != null) {
									CreateBOMWindowsOutput[] bomWindowPrbItems = VFBOM.openBOM(SMService, new TCComponent[] {problemRevision}, null);
									for(CreateBOMWindowsOutput bomPrbOutput : bomWindowPrbItems) {
										TCComponentBOMLine problemLine = bomPrbOutput.bomLine;
										boolean result = bomcompare.inputOutputMap(session, problemLine, solutionLine, count, strBuilder);
										if (result == true) {
											HashMap<String, TCComponent> prob_P_Output = bomcompare.getProblemOutputItems();
											HashMap<String, TCComponent> sol_P_Output = bomcompare.getSolutionOutputItems();

											HashMap<String, TCComponent> prob_C_Input = bomcompare.getProblemInputItems();
											HashMap<String, TCComponent> sol_C_Input = bomcompare.getSolutionInputItems();

											// Parent
											HashMap<String, TCComponent> add_Output_parts = bomcompare.addPart(sol_P_Output, prob_P_Output);
											HashMap<String, TCComponent> del_Output_parts = bomcompare.delPart(sol_P_Output, prob_P_Output);

											// Child
											HashMap<String, TCComponent> add_Input_parts = bomcompare.addPart(sol_C_Input, prob_C_Input);
											HashMap<String, TCComponent> del_Input_parts = bomcompare.delPart(sol_C_Input, prob_C_Input);

											// DELETE
											if ((add_Output_parts.isEmpty() && del_Output_parts.isEmpty() && add_Input_parts.isEmpty() && del_Input_parts.isEmpty()) == false) {
												// DELETE (PROBLEM ITEMS)
												Set<String> del_Input_keys = del_Input_parts.keySet();

												for (String delLine : del_Input_keys) {
													TCComponent del_Input_Part = del_Input_parts.get(delLine);
													Set<String> del_Out_keys = prob_P_Output.keySet();
													for (String delOutputLine : del_Out_keys) {
														TCComponent del_Ouput_Part = prob_P_Output.get(delOutputLine);
														HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
														trasferBOMMap.put("MCN", mcnNumber);
														trasferBOMMap.put("PLANTCODE", plantCode);
														trasferBOMMap.put("ACTION", "D");
														trasferBOMMap.put("PARENTPART", del_Ouput_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) del_Input_Part));
														trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) del_Input_Part));
														trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														trasferBOMMap.put("CHILDPART", del_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
														trasferBOMMap.put("ALTERNATIVE", del_Ouput_Part.getProperty("VF4_sap_bom_id"));// TODO: will change to VF4_bom_sap_id
														
														subGroupRecords.add(trasferBOMMap);
														printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														count++;
													}
													prob_C_Input.remove(delLine);
												}

												Set<String> del_Output_keys = del_Output_parts.keySet();

												for (String delLine : del_Output_keys) {
													TCComponent del_Part = del_Output_parts.get(delLine);
													Set<String> del_input_keys = prob_C_Input.keySet();
													for (String delInputLine : del_input_keys) {
														TCComponent del_Input_Part = prob_C_Input.get(delInputLine);
														HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
														trasferBOMMap.put("MCN", mcnNumber);
														trasferBOMMap.put("PLANTCODE", plantCode);
														trasferBOMMap.put("ACTION", "D");
														trasferBOMMap.put("PARENTPART", del_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) del_Input_Part));
														trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) del_Input_Part));
														trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														trasferBOMMap.put("CHILDPART", del_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
														trasferBOMMap.put("ALTERNATIVE", del_Part.getProperty("VF4_sap_bom_id"));// TODO: will change to VF4_bom_sap_id);
														subGroupRecords.add(trasferBOMMap);
														printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														count++;
													}
												}

												// ADD

												Set<String> add_Input_keys = add_Input_parts.keySet();

												for (String addLine : add_Input_keys) {
													TCComponent add_Input_Part = add_Input_parts.get(addLine);
													Set<String> add_Out_keys = sol_P_Output.keySet();
													for (String addOutputLine : add_Out_keys) {
														TCComponent add_Ouput_Part = sol_P_Output.get(addOutputLine);
														HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
														trasferBOMMap.put("MCN", mcnNumber);
														trasferBOMMap.put("PLANTCODE", plantCode);
														trasferBOMMap.put("ACTION", "A");
														trasferBOMMap.put("PARENTPART", add_Ouput_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Input_Part));
														trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Input_Part));
														trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														trasferBOMMap.put("CHILDPART", add_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
														trasferBOMMap.put("ALTERNATIVE", add_Ouput_Part.getProperty("VF4_sap_bom_id"));
														subGroupRecords.add(trasferBOMMap);
														printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														count++;
													}
													sol_C_Input.remove(addLine);
												}

												Set<String> add_Output_keys = add_Output_parts.keySet();

												for (String addLine : add_Output_keys) {
													TCComponent add_Part = add_Output_parts.get(addLine);
													Set<String> add_input_keys = sol_C_Input.keySet();
													for (String addInputLine : add_input_keys) {
														TCComponent add_Input_Part = sol_C_Input.get(addInputLine);
														HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
														trasferBOMMap.put("MCN", mcnNumber);
														trasferBOMMap.put("PLANTCODE", plantCode);
														trasferBOMMap.put("ACTION", "A");
														trasferBOMMap.put("PARENTPART", add_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Input_Part));
														trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Input_Part));
														trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														trasferBOMMap.put("CHILDPART", add_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
														trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
														trasferBOMMap.put("ALTERNATIVE", add_Part.getProperty("VF4_sap_bom_id"));
														subGroupRecords.add(trasferBOMMap);
														printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), trasferBOMMap.get("ALTERNATIVE"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														count++;
													}
												}
											} else {
												printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, "-", "-", "-", "No change in this Sub Group", "-", "Info" };
												Logger.bufferResponse("PRINT", printValues, strBuilder);
											}

											transferRecords.put(solutionRevision, subGroupRecords);
										} else {
											hasError = true;
										}
										
									}
									VFBOM.closeBOM(SMService, bomWindowPrbItems);
								}else {
									
									boolean result = bomcompare.inputOutputMap(session, null, solutionLine, count, strBuilder);
									if (result == true) {
										HashMap<String, TCComponent> sol_P_Output = bomcompare.sol_Output_Parts;
										HashMap<String, TCComponent> sol_C_Input = bomcompare.sol_Input_Parts;
										// ADD
										Set<String> child_keys = sol_C_Input.keySet();

										for (String childLine : child_keys) {
											TCComponent child_Part = sol_C_Input.get(childLine);
											Set<String> parent_keys = sol_P_Output.keySet();
											for (String ParentLine : parent_keys) {
												TCComponent parent_Part = sol_P_Output.get(ParentLine);
												HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
												trasferBOMMap.put("MCN", mcnNumber);
												trasferBOMMap.put("PLANTCODE", plantCode);
												trasferBOMMap.put("ACTION", "A");
												trasferBOMMap.put("PARENTPART", parent_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
												trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) child_Part));
												trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) child_Part));
												trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
												trasferBOMMap.put("CHILDPART", child_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
												trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
												trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
												trasferBOMMap.put("ALTERNATIVE", parent_Part.getProperty("VF4_sap_bom_id"));
												subGroupRecords.add(trasferBOMMap);
												printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
												Logger.bufferResponse("PRINT", printValues, strBuilder);
												count++;
											}
										}
										transferRecords.put(solutionRevision, subGroupRecords);
									} else {
										hasError = true;
									}
								}
							}
						}
						VFBOM.closeBOM(SMService, bomWindowSolItems);
					}
				}

				strBuilder.append("</table>");
				strBuilder.append("</body></html>");
				logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
				dlg.brwReport.setText(strBuilder.toString());
			} else {
				MessageBox.post("No \"Impacted Items\" in MCN to transfer to SAP. Please verify and retry transfer", "Error", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasError;
	}

	private void transferRecords(OrganizationInformationAbstract serverInfo, TCSession session, TCComponentChangeItemRevision selectedObject) {
		String mcnNumber = dlg.txtMCN.getText();
		String server = dlg.cbServer.getText();

		SAPURL SAPConnect = new SAPURL();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = session.getUserName();
		String SERVER_IP = serverInfo.getServerIP();
		try {
			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnNumber);
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<html><body>");
			String[] printValues = new String[] { "User :" + session.getUserName(), "MCN :"+mcnNumber , "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
			Logger.bufferResponse("DETAILS", printValues, strBuilder);
			printValues = new String[] { "S.No", "SUB GROUP", "RECORD", "BOMLINE ID", "SAP BOM ID", "MESSAGE", "ACTION", "RESULT" };
			Logger.bufferResponse("HEADER", printValues, strBuilder);
			boolean attachLog = true;
			if (transferRecords.isEmpty() == false) {
				int count = 1;
				for (TCComponentItemRevision solutionRevision : transferRecords.keySet()) {
					String itemID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String itemRevID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					ArrayList<HashMap<String, String>> records = transferRecords.get(solutionRevision);
					for (HashMap<String, String> trasferBOMMap : records) {
						String BOMFile = trasferBOMMap.get("CHILDPART") + "_" + trasferBOMMap.get("BOMLINEID");
						String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), trasferBOMMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + trasferBOMMap.get("ACTION") + "_" + BOMFile, logFolder, serverInfo.getAuth());
						if (message.length != 0) {
							if (message[0].equals("E") && !message[1].contains("Record is exist in staging table")) {
								attachLog = false;
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), message[1], trasferBOMMap.get("ACTION"), "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							} else if (message[0].equals("E") && message[1].contains("Record is exist in staging table")) {
								attachLog = false;
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), message[1], trasferBOMMap.get("ACTION"), "Error" };
								Logger.bufferResponse("WARN", printValues, strBuilder);
								count++;
							} else {
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), trasferBOMMap.get("ALTERNATIVE"), message[1], trasferBOMMap.get("ACTION"), "Success" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							}
						}
					}
					if (server.equals("PRODUCTION") == true && solutionRevision.okToModify()) {
						LocalDateTime now = LocalDateTime.now();
						String setValue = "Yes" + "~" + userName + "~" + dtf.format(now);
						UIGetValuesUtility.setProperty(dmService, solutionRevision, PropertyDefines.REV_TO_SAP, setValue);
					}
				}
			}

			strBuilder.append("</table>");
			strBuilder.append("</body></html>");
			logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
			if (attachLog == true && server.equals("PRODUCTION") == true) {
				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, selectedObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
				if (newDataset != null) {
					UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
				}
			}
			dlg.brwReport.setText(strBuilder.toString());
		} catch (TCException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getFindNo(TCComponentBOMLine child) {
		String bomLineID = "";
		try {
			bomLineID = child.getProperty("VF4_bomline_id");
			for (int i = bomLineID.length(); i < 4; i++) {
				bomLineID = "0" + bomLineID;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return bomLineID.trim();
	}

	private String getQuantity(TCComponentBOMLine child) {
		String quantity = "";
		try {
			quantity = child.getProperty("bl_quantity");
			if (quantity.equals("")) {
				quantity = "1";
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return quantity.trim();
	}
}
