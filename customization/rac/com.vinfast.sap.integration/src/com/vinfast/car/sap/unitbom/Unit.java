package com.vinfast.car.sap.unitbom;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.dialogs.UnitTransferDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class Unit extends AbstractHandler {
	private OrganizationInformationAbstract serverInfo = null;
	private static int counter = 1;
	private static int percentage = 1;
	private ArrayList<UnitBOMBean> transferRecords = null;
	private ArrayList<UnitBOMBean> transferCRecords = null;
	private ArrayList<UnitOWPBean> transferOPRecords = null;
	private ArrayList<String> skipRecords = null;
	private static ArrayList<String> operationIDs = null;
	private ProgressMonitorDialog progressDlg = null;
	private static String company = "";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) selectedObjects[0];
			TCSession session = changeRevision.getSession();

			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}
			
			company = UIGetValuesUtility.getCompanyCode(event);
			
			BOMManager BOMManager = new BOMManager(company).loadChangeAttachments(session, changeRevision);
			
			UnitTransferDialog transferDialog =  new UnitTransferDialog(session, changeRevision);
			transferDialog.create();
			transferDialog.setTitle("Unit BOM/BOP Transfer");

			Button validateBtn = transferDialog.getValidateButton();
			Button transferBtn = transferDialog.getTransferButton();

			validateBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					validateBtn.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							validate(transferDialog, BOMManager);
							if(BOMManager.hasError() == false) {
								transferDialog.setMessage("Please click \"Transfer\" to send data to SAP", IMessageProvider.INFORMATION);
								transferBtn.setEnabled(true);
							}else {
								transferDialog.setMessage("Please fix the error shown in the report and re-try", IMessageProvider.ERROR);
								validateBtn.setEnabled(true);
							}
						}
					});
				}
			});

			transferBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferBtn.setEnabled(false);
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(event.getCommand().toString(), UnitTransferDialog.getServer(), session);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							loadTransferRecords(transferDialog, BOMManager);
							transferDialog.getShell().dispose();
						}
					});
				}
			});
			transferDialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void validate(UnitTransferDialog transferDialog, BOMManager BOMManager) {

		transferRecords  = new ArrayList<UnitBOMBean>();
		transferCRecords  = new ArrayList<UnitBOMBean>();
		transferOPRecords = new ArrayList<UnitOWPBean>();
		skipRecords =  new ArrayList<String>();
		operationIDs =  new ArrayList<String>();
		counter = 1;
		String server = UnitTransferDialog.getServer();
		TCSession session = BOMManager.getSession();
		TCComponentChangeItemRevision changeRevision = UnitTransferDialog.getChangeRevision();
		DataManagementService DMService = DataManagementService.getService(session);
		UnitBOMCompare BOMCompareData =  new UnitBOMCompare(BOMManager);
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDialog.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Preparing Data...", IProgressMonitor.UNKNOWN);
					try {
						String[] printValues;

						TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);

						TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);

						TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(DMService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);

						for(TCComponent operation : solutionItems) {
							operationIDs.add(operation.getProperty(PropertyDefines.ITEM_ID));
						}

						BOMManager.initReport();
						ArrayList<String> transferIDs = new ArrayList<String>();
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
										printValues = new String[] { Integer.toString(counter), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
										BOMManager.printReport("PRINT", printValues);
										BOMManager.incrementSerialNo();
										BOMManager.setError(true);
									}
								} else {
									transferIDs.add(itemID);
								}
							}
						}
						//Got to transfer subgroups and start iterate
						monitor.subTask("Opening MBOM Structure...");
						Boolean isWindowOpen = BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING);
						if (BOMManager.hasError() && BOMManager.getErrorMessage().isEmpty() == false && isWindowOpen == false) {
							BOMManager.setError(true);
							MessageBox.post("BOM and BOP linked error.", "Please check BOM and BOP is linked or Contact Teamcenter Admin.", "Link...", MessageBox.ERROR);
							return;
						}else {
							transferRecords = new ArrayList<UnitBOMBean>();
							if(!transferIDs.isEmpty()) {

								String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");
								monitor.subTask("Searching Items in MBOM Structure...");
								ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchIDs }, BOMManager.getMBOMTraverseLine());
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
										CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, previousRevision);
										TCComponentBOMLine problemItem = output[0].bomLine;
										isStructureLoaded = BOMCompareData.inputOutputMap(session, problemItem, solutionItem, subGroup);
										if(isStructureLoaded == true) {
											transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
											BOMCompareData.processProblemItem(transferValues);
											BOMCompareData.processSolutionItem(transferValues, BOMManager.getBOPTraverseLine());
										}
										UIGetValuesUtility.closeWindow(session, output[0].bomWindow);
									}else {
										isStructureLoaded = BOMCompareData.inputOutputMap(session, previousRevision, solutionItem, subGroup);
										if(isStructureLoaded == true) {
											transferValues = new LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
											BOMCompareData.processSolutionItem(transferValues, BOMManager.getBOPTraverseLine());
										}
									}
									UnitBOMBean bean = new UnitBOMBean(solutionRevision, transferValues);
									transferRecords.add(bean);
								}
								printBOMBOPReport(transferRecords, BOMManager);
							}
							monitor.subTask("Loading Operations in MBOP Structure...");
							if (problemItems != null) {
								if(transferOPRecords == null) {
									transferOPRecords = new ArrayList<UnitOWPBean>();
								}
								for (TCComponent items : problemItems) {
									TCComponentItemRevision problem = (TCComponentItemRevision) items;
									CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
									TCComponentBOMWindow operationWindow = output[0].bomWindow;
									TCComponentBOMLine operationLine = output[0].bomLine;
									if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
										ArrayList<HashMap<String, String>> OWP = BOMCompareData.createOWPBOP(session, operationLine, "D");
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
								TCComponent[] operationsList = UIGetValuesUtility.searchStruture(session, searchIDs, BOMManager.getBOPTraverseLine());
								if (operationsList != null && operationsList.length > 0) {
									ArrayList<String> alreadyTransfered = new ArrayList<String>();
									for(TCComponent bomline : operationsList) {
										TCComponentBOMLine bomlinetag = (TCComponentBOMLine)bomline;
										String BOMID = bomlinetag.getProperty(PropertyDefines.BOM_ITEM_ID);
										monitor.subTask("Fetching Operations "+BOMID+" records in MBOP Structure...");
										if(alreadyTransfered.contains(BOMID) == false) {
											if (UIGetValuesUtility.hasMaterials(bomlinetag) == false) {
												ArrayList<HashMap<String, String>> OWP = BOMCompareData.createOWPBOP(session, bomline, "A");
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
							printOperationReport(transferOPRecords, BOMManager);

							if(BOMManager.isWindowOpen()) {
								monitor.subTask("Closing MBOM Strucuture...");
								BOMManager.closeMBOMBOPWindows();
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

			if(!BOMManager.getErrorMessage().isBlank()) {
				MessageBox.post(BOMManager.getErrorMessage(), "Error", MessageBox.ERROR);
			}else {
				BOMManager.finishReport("");
				BOMManager.popupReport("BOM");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printBOMBOPReport(ArrayList<UnitBOMBean> transferValues,BOMManager BOMManager) {
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

	private void printOperationReport(ArrayList<UnitOWPBean> transferValues, BOMManager BOMManager) {
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

	public void loadTransferRecords(UnitTransferDialog transferDialog, BOMManager bomManager) {
		counter = 1;
		SAPURL SAPConnect = new SAPURL();
		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();
		String server = UnitTransferDialog.getServer();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + bomManager.getMCN());

		bomManager.initReport();
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDialog.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Transferring Data to SAP...", IProgressMonitor.UNKNOWN);
					boolean isSubGroupTransferred = true;
					try {
						String[] printValues;
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
										if (message[0].equals("S") || message[1].contains("Record is exist in staging table")) {
											
											printValues = new String[] { Integer.toString(counter), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
											bomManager.printReport("PRINT", printValues);

											for (HashMap<String, String> bopMap : bopMaps) {
												if (bopMap.isEmpty() == false) {
													String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
													bopMap.put("MCN", bomManager.getMCN());
													String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
													if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
														printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
														bomManager.printReport("PRINT", printValues);
													} else {
														printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
														bomManager.printReport("PRINT", printValues);
														isSubGroupTransferred = false;
													}
												}
											}
											
										} else {
											printValues = new String[] { Integer.toString(counter), subGroup, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
											bomManager.printReport("PRINT", printValues);
											isSubGroupTransferred = false;
										}
										counter++;
									}
								}

								if(isSubGroupTransferred == true && server.equals("PRODUCTION")) {
									try {
										beanClass.getSubGroupRevision().setProperty("vf3_transfer_to_sap", "Y");
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
													bopMap.put("MCN",bomManager.getMCN());
													String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
													if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
														printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION")+"("+bomMap.get("CHILDPART")+")", bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
														bomManager.printReport("PRINT", printValues);
													} else {
														printValues = new String[] { Integer.toString(counter), subGroup, bopMap.get("BOPID") + "/" + bopMap.get("REVISION")+"("+bomMap.get("CHILDPART")+")", bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
														bomManager.printReport("PRINT", printValues);
													}
												}
											}
										}
										counter++;
									}
								}
							}
						}

						if(transferOPRecords.isEmpty() == false) {

							for(UnitOWPBean OWPBOP : transferOPRecords) {
								ArrayList<HashMap<String, String>> BOP_Values_arr = OWPBOP.getOWPMap();
								for(HashMap<String, String> BOP_Values : BOP_Values_arr) {
									String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
									BOP_Values.put("MCN", bomManager.getMCN());
									String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
									if (msg[0].equals("S") || msg[1].contains("Record is exist in staging table")) {
										printValues = new String[] { Integer.toString(counter), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Success" };
										bomManager.printReport("PRINT", printValues);
										if (server.equals("PRODUCTION") || server.equals("QA")) {
											try {
												OWPBOP.getOperationRevision().setProperty("vf3_transfer_to_sap", "Y");
											} catch (TCException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									} else {
										printValues = new String[] { Integer.toString(counter), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), BOP_Values.get("WORKSTATION"), msg[1], BOP_Values.get("ACTION"), "Error" };
										bomManager.printReport("PRINT", printValues);
									}
									counter++;
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
			bomManager.finishReport(previousData);
			File file = bomManager.popupReport("BOM");
			if (server.equals("PRODUCTION") == true) {
				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(DataManagementService.getService(bomManager.getSession()), UnitTransferDialog.getChangeRevision(), "IMAN_specification", file.getName(), "Transfer Report", "HTML", "IExplore");
				if (newDataset != null) {
					UIGetValuesUtility.uploadNamedReference(bomManager.getSession(), newDataset, file, "HTML", true, true);
				}
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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

	public HashMap<TCComponent,TCComponentBOMLine> findParentGroup(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, int count, StringBuilder strBuilder) {

		HashMap<TCComponent,TCComponentBOMLine> subGroupList = new HashMap<TCComponent,TCComponentBOMLine>();
		CoreService structService = CoreService.getService(session);
		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;
		for (NodeInfo bopNode : bomNodeBop) {
			TCComponent[] bomLines = bopNode.foundNodes;
			try {
				if (bomLines.length >= 1) {
					TCComponentBOMLine BOMLine = (TCComponentBOMLine)bomLines[0];
					subGroupList.put(BOMLine.parent(),BOMLine);
				}
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return subGroupList;
	}

	public static boolean isOperationInMCN(String operationID) {

		if(operationIDs.contains(operationID)) {
			return true;
		}else {
			return false;
		}
	}
}
