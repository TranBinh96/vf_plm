package com.vinfast.scooter.sap.assembly;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.car.sap.unitbom.UnitBOMBean;
import com.vinfast.car.sap.unitbom.UnitBOMCompare;
import com.vinfast.car.sap.unitbom.UnitOWPBean;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.dialogs.BOMBOPDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class Scooter extends AbstractHandler{

	private ProgressMonitorDialog progressDlg = null;
	private OrganizationInformationAbstract serverInfo = null;
	private ArrayList<UnitBOMBean> transferRecords = null;
	private ArrayList<UnitBOMBean> transferCRecords = null;
	private ArrayList<UnitOWPBean> transferOPRecords = null;
	private ArrayList<String> skipRecords = null;
	private static ArrayList<String> operationIDs = null;
	private String company = "";
	BOMManager BOMManager = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			String company = UIGetValuesUtility.getCompanyCode(event);
			TCComponentItemRevision selectedObject = null;
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}else {
				selectedObject = (TCComponentItemRevision)selectedObjects[0];
			}
			TCSession clientSession = selectedObject.getSession();

			BOMManager = new BOMManager(company).loadChangeAttachments(clientSession, selectedObject);
			if (BOMManager == null) {
				MessageBox.post("Error reading change object data. Please contact Teamcenter Support.", "Error", MessageBox.ERROR);
				return null;
			}

			BOMBOPDialog transferDlg = new BOMBOPDialog(new Shell());
			transferDlg.create();
			transferDlg.setTitle("Assembly BOM/BOP Transfer");
			transferDlg.setModel(BOMManager.getModel());
			transferDlg.setYear(BOMManager.getYear());
			transferDlg.setShop(BOMManager.getShopName());
			transferDlg.setPlant(BOMManager.getPlant());
			transferDlg.setMCN(BOMManager.getMCN());
			transferDlg.setServer("PRODUCTION");

			Button prepareBtn = transferDlg.getPrepareButton();
			Button transferBtn = transferDlg.getTransferButton();
			prepareBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferBtn.setEnabled(false);
					String serverType = transferDlg.cbServer.getText();
					String cmd = event.getCommand().toString();
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, clientSession);
					if(prepareData(transferDlg) == true && BOMManager.hasError() == false) {
						transferDlg.setMessage("Please click \"Transfer\" to send data to SAP", IMessageProvider.INFORMATION);
						transferBtn.setEnabled(true);
						prepareBtn.setEnabled(false);
					}else {
						transferDlg.setMessage("Please fix the error(s) shown in the report and re-try", IMessageProvider.ERROR);
						prepareBtn.setEnabled(true);
					}
				}
			});

			transferBtn.setEnabled(false);
			transferBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferBtn.setEnabled(false);
					String serverType = transferDlg.cbServer.getText();
					String cmd = event.getCommand().toString();
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, clientSession);
					transferRecords(transferDlg);
					transferDlg.getShell().dispose();
				}
			});

			transferDlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean prepareData(BOMBOPDialog transferDlg) {

		boolean isValidate = true;
		String server = transferDlg.getServer();
		transferRecords  = new ArrayList<UnitBOMBean>();
		transferCRecords  = new ArrayList<UnitBOMBean>();
		transferOPRecords = new ArrayList<UnitOWPBean>();
		skipRecords =  new ArrayList<String>();
		operationIDs =  new ArrayList<String>();
		UnitBOMCompare BOMCompareData =  new UnitBOMCompare(BOMManager);

		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Preparing Data...", IProgressMonitor.UNKNOWN);
					try {
						DataManagementService DMService = DataManagementService.getService(BOMManager.getSession());
						TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(DMService, BOMManager.getChangeObject(), new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);
						for(TCComponent operation : solutionItems) {
							operationIDs.add(operation.getProperty(PropertyDefines.ITEM_ID));
						}

						String[] printValues;
						BOMManager.initReport();
						ArrayList<String> transferIDs = new ArrayList<String>();
						//Get Impacted items and find already transferred to SAP and objects needs to transfer to SAP
						TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(DMService, BOMManager.getChangeObject(), new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
						if (impactedItems == null) impactedItems = new TCComponent[0];
						if(impactedItems !=null) {
							monitor.subTask("Loading Items to transfer to SAP...");
							for (TCComponent impactedItem : impactedItems) {
								String itemID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
								String itemRevID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
								if (server.equals("PRODUCTION")) {
									String transferToSAP = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
									if (transferToSAP.isEmpty() || !transferToSAP.contains(server)) {
										transferIDs.add(itemID);
									} else {
										printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
										BOMManager.printReport("PRINT", printValues);
									}
								} else {
									transferIDs.add(itemID);
								}
							}

							{
								monitor.subTask("Opening MBOM Structure...");
								Boolean isWindowOpen = BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING);
								if (BOMManager.hasError() && BOMManager.getErrorMessage().isEmpty() == false && isWindowOpen == false) {
									BOMManager.setError(true);
									return;
								}else {
									transferRecords = new ArrayList<UnitBOMBean>();
									if(!transferIDs.isEmpty()) {
										String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");
										monitor.subTask("Searching Items in MBOM Structure...");
										ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(BOMManager.getSession(), new String[] { searchIDs }, BOMManager.getMBOMTraverseLine());
										TCComponent[] impactedBOMLines = impactLines.toArray(new TCComponent[0]);
										//Start Transfer of subgroup one by one
										for (TCComponent impactedLine : impactedBOMLines) {
											LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = null;
											boolean isStructureLoaded;
											TCComponentBOMLine solutionItem = (TCComponentBOMLine) impactedLine;
											TCComponentItemRevision solutionRevision = solutionItem.getItemRevision();
											String subGroup = solutionRevision.getProperty(PropertyDefines.ITEM_ID)+"/"+solutionRevision.getProperty(PropertyDefines.ITEM_REV_ID);
											monitor.subTask("Fetching "+subGroup+" records in MBOM Structure...");
											TCComponentItemRevision previousRevision = UIGetValuesUtility.getPreviousRevision(solutionRevision);
											if(previousRevision != null) {
												CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(BOMManager.getSession(), previousRevision);
												TCComponentBOMLine problemItem = output[0].bomLine;
												isStructureLoaded = BOMCompareData.inputOutputMap(BOMManager.getSession(), problemItem, solutionItem, subGroup);
												if(isStructureLoaded == true) {
													transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
													BOMCompareData.processProblemItem(transferValues);
													BOMCompareData.processSolutionItem(transferValues, BOMManager.getBOPTOPLine());
												}
												UIGetValuesUtility.closeWindow(BOMManager.getSession(), output[0].bomWindow);
											}else {
												isStructureLoaded = BOMCompareData.inputOutputMap(BOMManager.getSession(), previousRevision, solutionItem, subGroup);
												if(isStructureLoaded == true) {
													transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
													BOMCompareData.processSolutionItem(transferValues, BOMManager.getBOPTOPLine());
												}
											}
											UnitBOMBean bean = new UnitBOMBean(solutionRevision, transferValues);
											transferRecords.add(bean);
										}
										printBOMBOPReport(transferRecords);
									}
									monitor.subTask("Loading Operations in MBOP Structure...");
									TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(DMService, BOMManager.getChangeObject(), new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);
									if (problemItems != null) {
										if(transferOPRecords == null) {
											transferOPRecords = new ArrayList<UnitOWPBean>();
										}
										for (TCComponent items : problemItems) {
											TCComponentItemRevision problem = (TCComponentItemRevision) items;
											CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(BOMManager.getSession(), problem);
											TCComponentBOMWindow operationWindow = output[0].bomWindow;
											TCComponentBOMLine operationLine = output[0].bomLine;
											if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
												ArrayList<HashMap<String, String>> OWP = BOMCompareData.createOWPBOP(BOMManager.getSession(), operationLine, "D");
												if(OWP != null) {
													UnitOWPBean OWPBean = new UnitOWPBean(problem, OWP);
													transferOPRecords.add(OWPBean);
												}
											}
											operationWindow.close();
										}
									}
									ArrayList<TCComponent> partsOperation = new ArrayList<TCComponent>();
									if (solutionItems != null) {
										if(transferOPRecords == null) {
											transferOPRecords = new ArrayList<UnitOWPBean>();
										}
										ArrayList<String> operationIDs = new ArrayList<String>();
										for (TCComponent solutionItem : solutionItems) {
											TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;
											operationIDs.add(operations.getProperty("item_id"));
										}
										String searchIDs = UIGetValuesUtility.convertArrayToString(operationIDs, ";");
										TCComponent[] operationsList = UIGetValuesUtility.searchStruture(BOMManager.getSession(), searchIDs, BOMManager.getBOPTraverseLine());
										if (operationsList != null && operationsList.length > 0) {
											ArrayList<String> alreadyTransfered = new ArrayList<String>();
											for(TCComponent bomline : operationsList) {
												TCComponentBOMLine bomlinetag = (TCComponentBOMLine)bomline;
												String BOMID = bomlinetag.getProperty(PropertyDefines.BOM_ITEM_ID);
												monitor.subTask("Fetching Operations "+BOMID+" records in MBOP Structure...");
												if(alreadyTransfered.contains(BOMID) == false) {
													if (UIGetValuesUtility.hasMaterials(bomlinetag) == false) {
														ArrayList<HashMap<String, String>> OWP = BOMCompareData.createOWPBOP(BOMManager.getSession(), bomline, "A");
														if(OWP != null) {
															UnitOWPBean OWPBean = new UnitOWPBean(bomlinetag.getItemRevision(), OWP);
															transferOPRecords.add(OWPBean);
															alreadyTransfered.add(BOMID);
														}
													}else {
														//collect solution operations with parts
														partsOperation.add(bomlinetag);
													}
												}
											}
										}
									}
									printOperationReport(transferOPRecords);

								}

								if(BOMManager.isWindowOpen()) {
									monitor.subTask("Closing MBOM Strucuture...");
									BOMManager.closeMBOMBOPWindows();
								}
							}
						}



					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

			if(!BOMManager.getErrorMessage().isBlank()) {
				MessageBox.post(BOMManager.getErrorMessage(), "Error", MessageBox.ERROR);
				isValidate = false;
			}else {
				BOMManager.finishReport("");
				BOMManager.popupReport("BOM");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isValidate;
	}

	private void printBOMBOPReport(ArrayList<UnitBOMBean> transferValues) {
		if (transferValues.isEmpty() == false) {
			for(UnitBOMBean beanClass : transferValues) {
				String subGroup = beanClass.getSubGroupID();
				LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> records = beanClass.getBOMBOPRecords();
				if(records != null) {
					for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : records.entrySet()) {
						HashMap<String, String> bomMap = entry.getKey();
						ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
						if (bomMap.isEmpty() == false) {
							skipRecords.add(bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART")+ "_" + bomMap.get("BOMLINEID"));
							String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), "BOM record will be sent to SAP", bomMap.get("ACTION"), "Info" };
							BOMManager.printReport("PRINT", printValues);
							for (HashMap<String, String> bopMap : bopMaps) {
								if (bopMap.isEmpty() == false) {
									printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), "BOP record will be sent to SAP", bomMap.get("ACTION"), "Info" };
									BOMManager.printReport("PRINT", printValues);
								}
							}
						}
						BOMManager.incrementSerialNo();
					}
				}
			}
		}
	}

	private void printOperationReport(ArrayList<UnitOWPBean> transferValues) {
		if (transferValues != null) {
			for(UnitOWPBean OWPBOP : transferValues) {
				ArrayList<HashMap<String, String>> BOP_Values_arr = OWPBOP.getOWPMap();
				for(HashMap<String, String> BOP_Values: BOP_Values_arr) {
					String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "OWP", OWPBOP.getOperationID(), BOP_Values.get("WORKSTATION"), "OWP record will be sent to SAP", BOP_Values.get("ACTION"), "Info" };
					BOMManager.printReport("PRINT", printValues);
					BOMManager.incrementSerialNo();
				}
			}
		}
	}


	private void transferRecords(BOMBOPDialog transferDlg) {
		String server = transferDlg.getServer();
		BOMManager.resetSerialNo();
		SAPURL SAPConnect = new SAPURL();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = BOMManager.getSession().getUserName();
		LocalDateTime now = LocalDateTime.now();
		String setValue = String.format("%s~%s~%s", transferDlg.getServer(), userName, dtf.format(now));
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + transferDlg.getMCN());
		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();
		BOMManager.initReport();
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Transferring Data to SAP...", IProgressMonitor.UNKNOWN);
					boolean isSubGroupTransferred = true;
					try {
						if (transferRecords.isEmpty() == false) {
							for(UnitBOMBean beanClass : transferRecords) {
								
								String subGroup = beanClass.getSubGroupID();
								LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> records = beanClass.getBOMBOPRecords();
								for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : records.entrySet()) {
									HashMap<String, String> bomMap = entry.getKey();
									ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
									if (bomMap.isEmpty() == false) {
										String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
										String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
										if(message[0].equals("S") || message[1].contains("Record is exist in staging table")) {
											String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
											BOMManager.printReport("PRINT", printValues);
											for (HashMap<String, String> bopMap : bopMaps) {
												if (bopMap.isEmpty() == false) {
													String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
													bopMap.put("MCN", BOMManager.getMCN());
													String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
													if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
														printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
														BOMManager.printReport("PRINT", printValues);
													} else {
														printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
														BOMManager.printReport("PRINT", printValues);
														BOMManager.setError(true);
													}
												}
											}
										}else {
											String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
											BOMManager.printReport("PRINT", printValues);
											BOMManager.setError(true);
										}

										BOMManager.incrementSerialNo();
									}
								}

								if(isSubGroupTransferred == true && server.equals("PRODUCTION")) {
									try {
										beanClass.getSubGroupRevision().setProperty("vf3_transfer_to_sap", setValue);
									} catch (TCException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}

						if (transferCRecords.isEmpty() == false) {
							for(UnitBOMBean beanClass : transferCRecords) {
								String subGroup = beanClass.getSubGroupID();
								LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> records = beanClass.getBOMBOPRecords();
								for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : records.entrySet()) {
									HashMap<String, String> bomMap = entry.getKey();
									ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
									if (bomMap.isEmpty() == false) {
										String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
										if(skipRecords.contains(BOMFile) == false) {
											for (HashMap<String, String> bopMap : bopMaps) {
												if (bopMap.isEmpty() == false) {
													String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
													bopMap.put("MCN", BOMManager.getMCN());
													String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
													if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
														String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION")+"("+bomMap.get("CHILDPART")+")", bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
														BOMManager.printReport("PRINT", printValues);
													} else {
														String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION")+"("+bomMap.get("CHILDPART")+")", bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
														BOMManager.printReport("PRINT", printValues);
														BOMManager.setError(true);
													}
												}
											}
										}
										BOMManager.incrementSerialNo();
									}
								}
							}
						}

						if(transferOPRecords.isEmpty() == false) {
							for(UnitOWPBean OWPBOP : transferOPRecords) {
								ArrayList<HashMap<String, String>> BOP_Values_arr = OWPBOP.getOWPMap();
								for(HashMap<String, String> BOP_Values : BOP_Values_arr) {
									String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
									BOP_Values.put("MCN", BOMManager.getMCN());
									String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
									if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
										String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Success" };
										BOMManager.printReport("PRINT", printValues);
										if (server.equals("PRODUCTION")) {
											try {
												OWPBOP.getOperationRevision().setProperty("vf3_transfer_to_sap", setValue);
											} catch (TCException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									} else {
										String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Error" };
										BOMManager.printReport("PRINT", printValues);
										BOMManager.setError(true);
									}
									BOMManager.incrementSerialNo();
								}
							}

						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			String previousData = Logger.previousTransaction(logFolder, "BOM");
			BOMManager.finishReport(previousData);
			File file = BOMManager.popupReport("BOM");
			if (server.equals("PRODUCTION") == true && BOMManager.hasError() == false) {
				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(DataManagementService.getService(BOMManager.getSession()), BOMManager.getChangeObject(), "IMAN_specification", file.getName(), "Transfer Report", "HTML", "IExplore");
				if (newDataset != null) {
					UIGetValuesUtility.uploadNamedReference(BOMManager.getSession(), newDataset, file, "HTML", true, true);
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
