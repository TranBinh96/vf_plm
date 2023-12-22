package com.vines.sap.unitBomBop;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.integration.service.VESUnitBOMBOPTransferService;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCAccessControlService;
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
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.car.sap.unitbom.UnitBOMBean;
import com.vinfast.car.sap.unitbom.UnitOWPBean;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.dialogs.UnitTransferDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.OpenWindowConfig;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class VESUnitBomBopTransferHandler extends AbstractHandler {
	File logFile = null;
	OrganizationInformationAbstract serverInfo = null;
	private static int counter = 1;
	private static int percentage = 1;
	private ArrayList<UnitBOMBean> transferRecords = null;
	private ArrayList<UnitOWPBean> transferOPRecords = null;
	private static boolean canTransfer = true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) selectedObjects[0];
			TCSession session = changeRevision.getSession();
			if (!TCExtension.checkPermissionAccess(changeRevision, TCAccessControlService.WRITE, session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			if(changeRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {
				MessageBox.post("MCN is not transfer. Please transfer MCN first to do any transfer.", "Error", MessageBox.ERROR);
				return null;
			}
			
			TCComponent[] impactedShop = changeRevision.getContents(PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop.length == 0 || impactedShop.length > 1) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}
			TCComponentItemRevision obj_Shop = (TCComponentItemRevision) impactedShop[0];
			String errorMessage = UIGetValuesUtility.checkValidShop(obj_Shop, changeRevision.getProperty(PropertyDefines.ECM_PLANT));
			if (!errorMessage.isEmpty()) {
				MessageBox.post(errorMessage, "Error", MessageBox.ERROR);
				return null;
			}

			UnitTransferDialog transferDialog = new UnitTransferDialog(session, changeRevision);
			transferDialog.create();
			transferDialog.setTitle("Unit BOM/BOP Transfer");

			Button validateBtn = transferDialog.getValidateButton();
			Button transferBtn = transferDialog.getTransferButton();

			validateBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					validateBtn.setEnabled(false);
					UnitTransferDialog.setVisibleProgressBar(true);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							canTransfer = true;
							resetPercentage();
							validate(session);
							UnitTransferDialog.setVisibleProgressBar(false);
							if (canTransfer == true) {
								transferBtn.setEnabled(true);
							}
						}
					});
				}
			});

			transferBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferBtn.setEnabled(false);
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(PropertyDefines.VIN_ES, UnitTransferDialog.getServer(), session);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							resetPercentage();
							try {
								loadTransferRecords(session);
							} catch (Exception e) {
								MessageBox.post("Please contact IT for this exception:\n" + e.getMessage(), "Error", MessageBox.ERROR);
							}
							transferDialog.getShell().dispose();
						}
					});
				}
			});
			transferDialog.open();
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void validate(TCSession session) {
		transferRecords = new ArrayList<UnitBOMBean>();
		transferOPRecords = new ArrayList<UnitOWPBean>();
		counter = 1;
		TCComponentChangeItemRevision changeRevision = UnitTransferDialog.getChangeRevision();
		DataManagementService DMService = DataManagementService.getService(session);
		String MCN = UnitTransferDialog.getMCNID();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + MCN);
		VESUnitBOMBOPTransferService BOMCompareData = new VESUnitBOMBOPTransferService();

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = session.getUserName();
		LocalDateTime now = LocalDateTime.now();
		String setValue = String.format("%s~%s~%s", UnitTransferDialog.getServer(), userName, dateFormat.format(now));

		try {
			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
			int impactCount = impactedItems != null ? impactedItems.length : 0;

			TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);
			int problemCount = problemItems != null ? problemItems.length : 0;

			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);
			int solutionCount = solutionItems != null ? solutionItems.length : 0;

			int total = impactCount + problemCount + solutionCount + 3;
			System.out.println("Total objects to traverse " + total);

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<html><body>");
			String[] printValues = new String[] { "Model : " + UnitTransferDialog.getModel() + "_" + UnitTransferDialog.getYear(), "Shop : " + UnitTransferDialog.getShop(), "User : " + userName,"MCN :"+MCN , "Time : " + setValue };
			Logger.bufferResponse("DETAILS", printValues, strBuilder);

			printValues = new String[] { "S.No", "Sub Group", "Record", "BOMLine ID", "Message", "Action", "Result" };
			Logger.bufferResponse("HEADER", printValues, strBuilder);
			UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
			ArrayList<String> transferIDs = new ArrayList<String>();
			if (impactedItems != null) {
				for (TCComponent impactedItem : impactedItems) {
					String itemID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String itemRevID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					if (UnitTransferDialog.getServer().equals("PRODUCTION")) {
						String transferToSAP = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
						if (transferToSAP.isEmpty() || !transferToSAP.contains(UnitTransferDialog.getServer())) {
							transferIDs.add(itemID);
						} else {
							printValues = new String[] { Integer.toString(counter), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							counter++;
						}
					} else {
						transferIDs.add(itemID);
					}
				}
			}
			UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
			// Got to transfer subgroups and start iterate
			OpenWindowConfig windowConfig = new OpenWindowConfig(session, UnitTransferDialog.getShopRevision());
			UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
			if (windowConfig.isLoaded() == false) {
				canTransfer = false;
				MessageBox.post("BOM and BOP linked error.", "Please check BOM and BOP is linked or Contact Teamcenter Admin.", "Link...", MessageBox.ERROR);
				return;
			} else {
				transferRecords = new ArrayList<UnitBOMBean>();
				if (!transferIDs.isEmpty()) {
					String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");
					ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchIDs }, windowConfig.SearchIn);
					TCComponent[] impactedBOMLines = impactLines.toArray(new TCComponent[0]);
					// Start Transfer of subgroup one by one
					for (TCComponent impactedLine : impactedBOMLines) {
						LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = null;
						boolean isStructureLoaded;
						UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
						TCComponentBOMLine solutionItem = (TCComponentBOMLine) impactedLine;
						TCComponentItemRevision solutionRevision = solutionItem.getItemRevision();
						String subGroup = solutionRevision.getProperty(PropertyDefines.ITEM_ID) + "/" + solutionRevision.getProperty(PropertyDefines.ITEM_REV_ID);
						TCComponentItemRevision previousRevision = UIGetValuesUtility.getPreviousRevision(solutionRevision);
						if (previousRevision != null) {
							CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, previousRevision);
							TCComponentBOMLine problemItem = output[0].bomLine;
							isStructureLoaded = BOMCompareData.inputOutputMap(session, problemItem, solutionItem, subGroup, counter, strBuilder);
							if (isStructureLoaded == true) {
								transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
								BOMCompareData.processProblemItem(transferValues);
								BOMCompareData.processSolutionItem(transferValues, windowConfig.BOPTopLine, counter, strBuilder);
							}
							UIGetValuesUtility.closeWindow(session, output[0].bomWindow);
						} else {
							isStructureLoaded = BOMCompareData.inputOutputMap(session, previousRevision, solutionItem, subGroup, counter, strBuilder);
							if (isStructureLoaded == true) {
								transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
								BOMCompareData.processSolutionItem(transferValues, windowConfig.BOPTopLine, counter, strBuilder);
							}
						}
						UnitBOMBean bean = new UnitBOMBean(solutionRevision, transferValues);
						transferRecords.add(bean);
					}
					printBOMBOPReport(transferRecords, strBuilder);
				}

				if (problemItems != null) {
					if (transferOPRecords == null) {
						transferOPRecords = new ArrayList<UnitOWPBean>();
					}
					for (TCComponent items : problemItems) {
						UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
						TCComponentItemRevision problem = (TCComponentItemRevision) items;
						CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
						TCComponentBOMWindow operationWindow = output[0].bomWindow;
						TCComponentBOMLine operationLine = output[0].bomLine;
						if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
							ArrayList<HashMap<String, String>> OWP = VESUnitBOMBOPTransferService.createOWPBOP(operationLine, "D", counter, strBuilder);
							if (OWP != null) {
								UnitOWPBean OWPBean = new UnitOWPBean(problem, OWP);
								transferOPRecords.add(OWPBean);
							}
						}
						operationWindow.close();
					}
				}

				if (solutionItems != null) {
					if (transferOPRecords == null) {
						transferOPRecords = new ArrayList<UnitOWPBean>();
					}
					ArrayList<String> operationIDs = new ArrayList<String>();
					for (TCComponent solutionItem : solutionItems) {
						TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;
						operationIDs.add(operations.getProperty("item_id"));
					}
					String searchIDs = UIGetValuesUtility.convertArrayToString(operationIDs, ";");
					TCComponent[] operationsList = UIGetValuesUtility.searchStruture(session, searchIDs, windowConfig.FindIn);
					if (operationsList != null && operationsList.length > 0) {
						ArrayList<String> alreadyTransfered = new ArrayList<String>();
						for (TCComponent bomline : operationsList) {
							UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
							TCComponentBOMLine bomlinetag = (TCComponentBOMLine) bomline;
							String BOMID = bomlinetag.getProperty(PropertyDefines.BOM_ITEM_ID);
							if(alreadyTransfered.contains(BOMID) == false) {
								if (UIGetValuesUtility.hasMaterials(bomlinetag) == false) {
									ArrayList<HashMap<String, String>> OWP = VESUnitBOMBOPTransferService.createOWPBOP(bomline, "A", counter, strBuilder);
									if (OWP != null) {
										UnitOWPBean OWPBean = new UnitOWPBean(bomlinetag.getItemRevision(), OWP);
										transferOPRecords.add(OWPBean);
										alreadyTransfered.add(BOMID);
									}
								}
							}
						}
					}
				}
				printOperationReport(transferOPRecords, strBuilder);
			}

			// Work on Operations
			// code here
			windowConfig.closeWindowConfig();
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
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	}

	private void printBOMBOPReport(ArrayList<UnitBOMBean> transferValues, StringBuilder strBuilder) {
		if (transferValues.isEmpty() == false) {
			for (UnitBOMBean beanClass : transferValues) {
				String subGroup = beanClass.getSubGroupID();
				LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> records = beanClass.getBOMBOPRecords();
				if (records != null) {
					for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : records.entrySet()) {
						HashMap<String, String> bomMap = entry.getKey();
						ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
						if (bomMap.isEmpty() == false) {
							String[] printValues = new String[] { Integer.toString(counter), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), "BOM record will be sent to SAP", bomMap.get("ACTION"), "Info" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							for (HashMap<String, String> bopMap : bopMaps) {
								if (bopMap.isEmpty() == false) {
									printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), "BOP record will be sent to SAP", bomMap.get("ACTION"), "Info" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);

								}
							}
						}
						counter++;
					}
				}
			}
		}
	}

	private void printOperationReport(ArrayList<UnitOWPBean> transferValues, StringBuilder strBuilder) {
		if (transferValues != null) {
			for (UnitOWPBean OWPBOP : transferValues) {
				ArrayList<HashMap<String, String>> BOP_Values_arr = OWPBOP.getOWPMap();
				for (HashMap<String, String> BOP_Values : BOP_Values_arr) {
					String[] printValues = new String[] { Integer.toString(counter), "OWP", OWPBOP.getOperationID(), BOP_Values.get("WORKSTATION"), "OWP record will be sent to SAP", BOP_Values.get("ACTION"), "Info" };
					Logger.bufferResponse("PRINT", printValues, strBuilder);
					counter++;
				}
			}
		}
	}

	public void loadTransferRecords(TCSession session) throws Exception {
		counter = 1;
		resetPercentage();
		SAPURL SAPConnect = new SAPURL();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = session.getUserName();
		LocalDateTime now = LocalDateTime.now();
		String setValue = String.format("%s~%s~%s", UnitTransferDialog.getServer(), userName, dtf.format(now));

		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + UnitTransferDialog.getMCNID());
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<html><body>");
		String[] printValues = new String[] { "Model : " + UnitTransferDialog.getModel() + "_" + UnitTransferDialog.getYear(), "Shop : " + UnitTransferDialog.getShop(), "User : " + userName, "Time : " + setValue };
		Logger.bufferResponse("DETAILS", printValues, strBuilder);

		printValues = new String[] { "S.No", "Sub Group", "Record", "BOMLine ID", "Message", "Action", "Result" };
		Logger.bufferResponse("HEADER", printValues, strBuilder);

		int total = transferRecords.size() + transferOPRecords.size();

		if (transferRecords.isEmpty() == false) {
			for (UnitBOMBean beanClass : transferRecords) {
				UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
				UnitTransferDialog.setVisibleProgressBar(true);
				boolean isSubGroupTransferred = true;
				String subGroup = beanClass.getSubGroupID();
				LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> records = beanClass.getBOMBOPRecords();
				for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : records.entrySet()) {
					HashMap<String, String> bomMap = entry.getKey();
					ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
					if (bomMap.isEmpty() == false) {
						String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
						//String msg[] = CreateSoapHttpRequest.sendRequest(dataMap.getSAPWebServiceUrl(serverInfo.getServerIP()), listXmlData, sapUrlInfo[0], sapUrlInfo[1], sapUrlInfo[2], dataMap.getXmlFileName(FileType.IN_PUT, count++), logFolder, serverInfo.getAuth());

						String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);

						if (message[0].equals("E") && !message[1].contains("Record is exist in staging table")) {
							printValues = new String[] { Integer.toString(counter), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							isSubGroupTransferred = false;
						} else {
							printValues = new String[] { Integer.toString(counter), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);

							for (HashMap<String, String> bopMap : bopMaps) {
								if (bopMap.isEmpty() == false) {
									String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
									//bopMap.put("MCN", UnitTransferDialog.getMCNID());
									String[] msg =  CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
									if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
										printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										isSubGroupTransferred = false;
									} else {
										printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
									}
								}
							}
						}
						counter++;
					}
				}

				if (isSubGroupTransferred == true && UnitTransferDialog.getServer().equals("PRODUCTION")) {
					try {
						beanClass.getSubGroupRevision().setProperty("vf3_transfer_to_sap", setValue);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		if (transferOPRecords.isEmpty() == false) {

			for (UnitOWPBean OWPBOP : transferOPRecords) {
				UnitTransferDialog.setProgressStatus(getProgress(getPercentage(), total));
				ArrayList<HashMap<String, String>> BOP_Values_arr = OWPBOP.getOWPMap();
				for (HashMap<String, String> BOP_Values : BOP_Values_arr) {
					String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
					//BOP_Values.put("MCN", UnitTransferDialog.getMCNID());
					String[] msg =  CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
					if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
						printValues = new String[] { Integer.toString(counter), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Error" };
						Logger.bufferResponse("PRINT", printValues, strBuilder);
					} else {
						printValues = new String[] { Integer.toString(counter), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Success" };
						Logger.bufferResponse("PRINT", printValues, strBuilder);
						if (UnitTransferDialog.getServer().equals("PRODUCTION") || UnitTransferDialog.getServer().equals("QA")) {
							try {
								OWPBOP.getOperationRevision().setProperty("vf3_transfer_to_sap", setValue);
							} catch (TCException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					counter++;
				}
			}
		}

		strBuilder.append("</table>");
		String data = Logger.previousTransaction(logFolder, "BOM");
		if (!data.equals("")) {
			strBuilder.append("<br>");
			strBuilder.append(data);
		}
		strBuilder.append("</body></html>");
		logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
		if (UnitTransferDialog.getServer().equals("PRODUCTION") == true) {
			TCComponentDataset newDataset = UIGetValuesUtility.createDataset(DataManagementService.getService(session), UnitTransferDialog.getChangeRevision(), "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
			if (newDataset != null) {
				UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
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

	}

	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}

	public static int getPercentage() {
		return percentage++;
	}

	public static int resetPercentage() {
		return percentage = 1;
	}

	public static int counterIncrement() {
		return counter++;
	}

	public static void canTransfer(boolean value) {
		canTransfer = value;
	}
}
