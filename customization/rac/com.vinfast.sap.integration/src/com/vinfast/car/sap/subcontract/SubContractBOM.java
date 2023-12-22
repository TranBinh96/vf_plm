package com.vinfast.car.sap.subcontract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.dialogs.BOMOnlyDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.BOMCompareReport;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class SubContractBOM extends AbstractHandler {
	private HashMap<TCComponentItemRevision, ArrayList<HashMap<String, String>>> transferRecords = null;
	private TCComponentItemRevision obj_Shop = null;
	private DataManagementService dmCoreService = null;
	private File logFile = null;
	private OrganizationInformationAbstract serverInfo = null;
	private String cmd = null;
	private int finalCounter = 0;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			cmd = event.getCommand().toString();
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentChangeItemRevision changeObject = (TCComponentChangeItemRevision) selectedObjects[0];
			TCSession clientSession = changeObject.getSession();

			VFResponse response = MCNValidator.validate(selectedObjects);
			if (response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}

			dmCoreService = DataManagementService.getService(clientSession);
			dmCoreService.getProperties(new TCComponent[] { changeObject }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ECM_PLANT, PropertyDefines.REL_IMPACT_SHOP });
			String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
			TCComponent[] impactedShop = changeObject.getContents(PropertyDefines.REL_IMPACT_SHOP);
			obj_Shop = (TCComponentItemRevision) impactedShop[0];

			String mainGroup = obj_Shop.getProperty("object_name");
			BOMOnlyDialog transferDlg = new BOMOnlyDialog(new Shell());
			transferDlg.create();
			transferDlg.setTitle("Sub Contract BOM (Only BOM)");
			transferDlg.setModel(mainGroup);
			transferDlg.setMCN(MCN);
			transferDlg.setPlant(plant);
			transferDlg.setServer("PRODUCTION");
			transferDlg.btnData.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferDlg.btnData.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (prepareData(transferDlg, clientSession, changeObject)) {
								transferDlg.btnSave.setEnabled(false);
								transferDlg.btnData.setEnabled(true);
							} else {
								transferDlg.btnSave.setEnabled(true);
								transferDlg.btnData.setEnabled(false);
							}
						}
					});
				}
			});

			transferDlg.btnSave.setEnabled(false);
			transferDlg.btnSave.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferDlg.btnSave.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferRecords(transferDlg, clientSession, changeObject);
							transferDlg.getShell().dispose();
						}
					});
				}
			});
			transferDlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private boolean prepareData(BOMOnlyDialog transferDlg, TCSession clientSession, TCComponentChangeItemRevision changeObject) {
		boolean hasError = false;
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + transferDlg.getMCN());
		transferRecords = new HashMap<TCComponentItemRevision, ArrayList<HashMap<String, String>>>();
		BOMCompareReport bomcompare = new BOMCompareReport();
		try {
			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
			if (impactedItems != null) {
				String MCN_ID = transferDlg.getMCN();
				String MCN_Plant_Code = transferDlg.getPlant();
				UIGetValuesUtility.createLogFolder("MCN" + MCN_ID);

				int count = 1;
				int percentage = 1;
				int total = impactedItems.length;

				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("<html><body>");
				String[] printValues = new String[] { "User :" + clientSession.getUserName(),"MCN :"+MCN_ID, "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
				Logger.bufferResponse("DETAILS", printValues, strBuilder);
				printValues = new String[] { "S.No", "SUB GROUP", "RECORD", "BOMLINE ID", "MESSAGE", "ACTION", "RESULT" };
				Logger.bufferResponse("HEADER", printValues, strBuilder);

				dmCoreService.getProperties(impactedItems, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ITEM_REV_ID, PropertyDefines.REV_TO_SAP });
				ArrayList<TCComponentItemRevision> transferRevisions = new ArrayList<TCComponentItemRevision>();
				for (TCComponent impacted : impactedItems) {
					String itemID = impacted.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String itemRevID = impacted.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					if (transferDlg.getServer().equals("PRODUCTION")) {
						String transferToSAP = impacted.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
						if (transferToSAP.isEmpty()) {
							transferRevisions.add((TCComponentItemRevision) impacted);
						} else {
							printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
						}
					} else {
						transferRevisions.add((TCComponentItemRevision) impacted);
					}
				}

				if (transferRevisions.isEmpty() == false) {
					for (TCComponentItemRevision solutionRevision : transferRevisions) {
						ArrayList<HashMap<String, String>> subgrouprecords = new ArrayList<HashMap<String, String>>();
						String itemID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
						String itemRevID = solutionRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
						OpenContextInfo[] output = UIGetValuesUtility.createContextViews(clientSession, solutionRevision.getItem());
						TCComponentBOMLine solutionLine = (TCComponentBOMLine) output[0].context;
						TCComponentItemRevision problemRevision = UIGetValuesUtility.getPreviousRevision(solutionRevision);

						if (problemRevision != null) {
							CreateBOMWindowsOutput[] problemWindow = UIGetValuesUtility.createBOMNoRuleWindow(clientSession, problemRevision);
							TCComponentBOMLine problemLine = problemWindow[0].bomLine;
							TCComponentBOMWindow window = problemWindow[0].bomWindow;

							boolean result = bomcompare.inputOutputMap(clientSession, problemLine, solutionLine, count, strBuilder);
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
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "D");
											trasferBOMMap.put("PARENTPART", del_Ouput_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) del_Input_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) del_Input_Part));
											trasferBOMMap.put("LINE", del_Input_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", del_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											subgrouprecords.add(trasferBOMMap);
											printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											count++;
											finalCounter++;
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
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "D");
											trasferBOMMap.put("PARENTPART", del_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) del_Input_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) del_Input_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", del_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("LINE", del_Input_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											subgrouprecords.add(trasferBOMMap);
											printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											count++;
											finalCounter++;
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
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "A");
											trasferBOMMap.put("PARENTPART", add_Ouput_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Input_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Input_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", add_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("LINE", add_Input_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											subgrouprecords.add(trasferBOMMap);
											printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											count++;
											finalCounter++;
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
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "A");
											trasferBOMMap.put("PARENTPART", add_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Input_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Input_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", add_Input_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put("LINE", add_Input_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											subgrouprecords.add(trasferBOMMap);
											printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											count++;
											finalCounter++;
										}
									}
								} else {
									printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, "-", "-", "No change in this Sub Group", "-", "Info" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
								}

								transferRecords.put(solutionRevision, subgrouprecords);
							} else {
								hasError = true;
							}

							UIGetValuesUtility.closeWindow(clientSession, window);
						} else {
							boolean result = bomcompare.inputOutputMap(clientSession, null, solutionLine, count, strBuilder);

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
										trasferBOMMap.put("MCN", MCN_ID);
										trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
										trasferBOMMap.put("ACTION", "A");
										trasferBOMMap.put("PARENTPART", parent_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
										trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) child_Part));
										trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) child_Part));
										trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
										trasferBOMMap.put("CHILDPART", child_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
										trasferBOMMap.put("LINE", child_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
										trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
										trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
										subgrouprecords.add(trasferBOMMap);
										printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), "BOM record will be sent to SAP", trasferBOMMap.get("ACTION"), "Info" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										count++;
										finalCounter++;
									}
								}
								transferRecords.put(solutionRevision, subgrouprecords);
							} else {
								hasError = true;
							}
						}

						transferDlg.setProgressStatus(getProgress(percentage, total));
						percentage++;
					}

				}

				strBuilder.append("</table>");
				strBuilder.append("</body></html>");
				logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							StringViewerDialog viewdialog = new StringViewerDialog(logFile);
							viewdialog.setSize(600, 400);
							viewdialog.setLocationRelativeTo(null);
							viewdialog.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				MessageBox.post("No \"Impacted Items\" in MCN to transfer to SAP. Please verify and retry transfer", "Error", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasError;
	}

	private void transferRecords(BOMOnlyDialog transferDlg, TCSession clientSession, TCComponentItemRevision changeObject) {
		SAPURL SAPConnect = new SAPURL();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = clientSession.getUserName();
		serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, transferDlg.getServer(), clientSession);
		String SERVER_IP = serverInfo.getServerIP();
		try {

			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + transferDlg.getMCN());
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<html><body>");
			String[] printValues = new String[] { "User :" + clientSession.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
			Logger.bufferResponse("DETAILS", printValues, strBuilder);
			printValues = new String[] { "S.No", "SUB GROUP", "RECORD", "BOMLINE ID", "MESSAGE", "ACTION", "RESULT" };
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
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), message[1], trasferBOMMap.get("ACTION"), "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							} else if (message[0].equals("E") && message[1].contains("Record is exist in staging table")) {
								attachLog = false;
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), message[1], trasferBOMMap.get("ACTION"), "Error" };
								Logger.bufferResponse("WARN", printValues, strBuilder);
								count++;
							} else {
								printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, trasferBOMMap.get("CHILDPART"), trasferBOMMap.get("BOMLINEID"), message[1], trasferBOMMap.get("ACTION"), "Success" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							}
						}
						transferDlg.setProgressStatus(getProgress(count, finalCounter));
					}

					if (transferDlg.getServer().equals("PRODUCTION") == true && solutionRevision.okToModify()) {

						LocalDateTime now = LocalDateTime.now();
						String setValue = "Yes" + "~" + userName + "~" + dtf.format(now);
						UIGetValuesUtility.setProperty(dmCoreService, solutionRevision, PropertyDefines.REV_TO_SAP, setValue);
					}
				}
			}

			strBuilder.append("</table>");
			strBuilder.append("</body></html>");
			logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
			if (attachLog == true && transferDlg.getServer().equals("PRODUCTION") == true) {
				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmCoreService, changeObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
				if (newDataset != null) {
					UIGetValuesUtility.uploadNamedReference(clientSession, newDataset, logFile, "HTML", true, true);
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						StringViewerDialog viewdialog = new StringViewerDialog(logFile);
						viewdialog.setSize(600, 400);
						viewdialog.setLocationRelativeTo(null);
						viewdialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
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

	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}
}
