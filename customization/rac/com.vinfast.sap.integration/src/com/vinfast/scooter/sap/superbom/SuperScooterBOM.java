package com.vinfast.scooter.sap.superbom;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
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
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
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
import com.vinfast.scooter.sap.assembly.OperationsWorkStations;
import com.vinfast.url.SAPURL;

public class SuperScooterBOM extends AbstractHandler {

	private ProgressMonitorDialog progressDlg = null;
	String cmd = null;

	HashMap<String, PrepareBomBopData> bombop2Send = new HashMap<String, PrepareBomBopData>();
	ArrayList<HashMap<String, String>> operationNoPart2Send = new ArrayList<HashMap<String, String>>();
	OrganizationInformationAbstract serverInfo = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// TODO Auto-generated method stub

		try {

			TCComponentItemRevision changeObject = null;
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}else {
				changeObject = (TCComponentItemRevision)selectedObjects[0];
			}
			TCSession clientSession = changeObject.getSession();

			BOMManager BOMManager = new BOMManager(UIGetValuesUtility.getCompanyCode(event)).loadChangeAttachments(clientSession, changeObject);
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
					loadTransferRecords(transferDlg, BOMManager);
					if(BOMManager.hasError() == false) {
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
					realTransfer(transferDlg,BOMManager);
					transferDlg.getShell().dispose();
				}
			});

			transferDlg.open();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void loadTransferRecords(BOMBOPDialog transferDlg, BOMManager bomManager) {
		// TODO Auto-generated method stub
		OperationsWorkStations opWs = new OperationsWorkStations();
		String server = transferDlg.getServer();
		DataManagementService dmCoreService = DataManagementService.getService(bomManager.getSession());
		/*****************************************************
		 * STEP 1: CHANGE VALIDATION AND TRANSFER
		 ***************************************************/
		try {

			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Preparing Data...", IProgressMonitor.UNKNOWN);

					try {
						String[] printValues;
						TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, bomManager.getChangeObject(), new String[] {"MEOPRevision"},"CMHasProblemItem");

						TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, bomManager.getChangeObject(), new String[] {"MEOPRevision"},"EC_solution_item_rel");

						TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, bomManager.getChangeObject(), new String[] {},"CMHasImpactedItem");

						if (impactedItems == null) impactedItems = new TCComponent[0];
						
						{
							monitor.subTask("Loading Items to transfer to SAP...");
							bomManager.initReport();
							ArrayList<String> transferIDs = new ArrayList<String>();
							for (TCComponent impactedItem : impactedItems) {
								String itemID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
								String itemRevID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
								if (server.equals("PRODUCTION")) {
									String transferToSAP = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
									if (transferToSAP.isEmpty() || !transferToSAP.contains(server)) {
										transferIDs.add(itemID);
									} else {
										printValues = new String[] { Integer.toString(bomManager.getSerialNo()), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
										bomManager.printReport("PRINT", printValues);
										bomManager.incrementSerialNo();
									}
								} else {
									transferIDs.add(itemID);
								}
							}

							if(transferIDs.isEmpty() == false) {

								monitor.subTask("Opening MBOM Structure...");
								Boolean isWindowOpen = bomManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING);
								if (bomManager.hasError() && bomManager.getErrorMessage().isEmpty() == false && isWindowOpen == false) {
									bomManager.setError(true);
									return;
								}else {

									String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");
									monitor.subTask("Searching Items in MBOM Structure...");
									ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(bomManager.getSession(), new String[] { searchIDs }, bomManager.getMBOMTraverseLine());
									TCComponent[] impactedBOMLines = impactLines.toArray(new TCComponent[0]);

									for(TCComponent impactedLine : impactedBOMLines) {

										boolean isError = false;

										String itemID = impactedLine.getProperty("bl_item_item_id");

										TCComponentItemRevision solution_Rev = ((TCComponentBOMLine)impactedLine).getItemRevision();

										String itemRevID =  solution_Rev.getProperty("item_revision_id");

										String subGroup = itemID+"/"+itemRevID;

										monitor.subTask("Fetching "+subGroup+" records in MBOM Structure...");

										HashMap<String, HashMap<String, String>>  prDataMap = new HashMap<String, HashMap<String, String>>();

										TCComponentItemRevision previousRevision = UIGetValuesUtility.getPreviousRevision(solution_Rev);

										if(previousRevision != null) {

											CreateBOMWindowsOutput[] prWindow = UIGetValuesUtility.createBOMNoRuleWindow(bomManager.getSession(), previousRevision);
											TCComponentBOMLine prBOMLine = prWindow[0].bomLine;
											TCComponentBOMWindow window = prWindow[0].bomWindow;

											ExpandPSData[] PSData = UIGetValuesUtility.traverseSingleLevelBOM(bomManager.getSession(), prBOMLine);

											if(PSData != null) {

												TCComponent[] problemLines = UIGetValuesUtility.fetchValidScooterBOMItems(dmCoreService, bomManager, PSData,itemID);

												if(problemLines != null) {

													dmCoreService.getProperties(problemLines, new String[] {"bl_rev_object_type","VL5_purchase_lvl_vf","VF3_purchase_lvl_vf","VF4_manuf_code","VF4_bomline_id","bl_item_item_id","bl_quantity","bl_formula"});

													for(TCComponent problemLine : problemLines) {

														TCComponentBOMLine problemBOMLine = (TCComponentBOMLine) problemLine;

														HashMap<String, HashMap<String,String>> bomLineValues = SuperScooterTransfer.traverseProblemItems(transferDlg, bomManager, problemBOMLine, itemID);

														if(bomLineValues != null) {

															prDataMap.putAll(bomLineValues);
														}else {

															isError = true;
														}
													}
												}
											}

											UIGetValuesUtility.closeWindow(bomManager.getSession(), window);
										}
										//Solution Item
										HashMap<String, HashMap<String, String>>  solBOMDataMap = new HashMap<String, HashMap<String, String>>();
										HashMap<String, ArrayList<HashMap<String,String>>>  solBOPDataMap = new HashMap<String, ArrayList<HashMap<String,String>>>();

										// Open sub group one level (solutionItem is subgroup and itemID is subgroup ID)
										ExpandPSData[] solData = UIGetValuesUtility.traverseSingleLevelBOM(bomManager.getSession(), (TCComponentBOMLine)impactedLine);

										if(solData.length != 0) {

											// get bomdata from lines under the current subgroup
											TCComponent[] BOMLines = UIGetValuesUtility.fetchValidScooterBOMItems(dmCoreService, bomManager, solData,itemID);

											if(BOMLines != null) {

												dmCoreService.getProperties(BOMLines, new String[] {"bl_rev_object_type","VL5_purchase_lvl_vf","VF3_purchase_lvl_vf","VF4_manuf_code","VF4_bomline_id","bl_item_item_id","bl_quantity","bl_formula"});

												NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOPScooter(bomManager.getSession(), bomManager,BOMLines, (TCComponentBOMLine)bomManager.getBOPTraverseLine(),itemID);

												if(bopNode != null) {

													for(NodeInfo Node : bopNode) {

														ArrayList<String> wsList = null;
														String ID = "";
														TCComponentBOMLine operationBOMLine = null;
														TCComponentBOMLine inputBOMLine = (TCComponentBOMLine) Node.originalNode;
														TCComponent[] inputBOPLine = Node.foundNodes;

														HashMap<String, HashMap<String,String>> bomLineValues = SuperScooterTransfer.traverseSolutionItems(transferDlg, bomManager, inputBOMLine,  itemID);

														if(bomLineValues != null) {

															solBOMDataMap.putAll(bomLineValues);

															String BOMkey = (String) bomLineValues.keySet().toArray()[0];
															HashMap<String, String> bomMap = bomLineValues.get(BOMkey);

															if(inputBOPLine != null) {

																for(TCComponent foundLine : inputBOPLine) {

																	TCComponentBOMLine bopLine = (TCComponentBOMLine)foundLine;
																	operationBOMLine = (TCComponentBOMLine) bopLine.getReferenceProperty("bl_parent");
																	TCComponentItemRevision operationRevision = operationBOMLine.getItemRevision();

																	if(operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {

																		dmCoreService.getProperties(new TCComponent[] {operationBOMLine}, new String[] {"vf4_operation_type","bl_rev_item_revision_id","vf5_line_supply_method"});

																		ID = operationRevision.getPropertyDisplayableValue("item_id");

																		wsList = opWs.getWorkStationsID(ID);

																		if(wsList == null) {

																			ArrayList<TCComponent> operations = UIGetValuesUtility.searchPartsInStruture(bomManager.getSession(), new String[] {ID}, (TCComponentBOMLine)bomManager.getBOPTraverseLine());

																			if(operations != null) {

																				for(TCComponent ops: operations) {

																					String workStation = opWs.generateWorkStationID((TCComponentBOMLine)ops, "bl_rev_object_name");

																					if(workStation.equals("") == false) {

																						opWs.setWorkStationsID(ID, workStation);

																					}
																				}
																			}
																		}
																	}else {
																		printValues = new String[] {Integer.toString(bomManager.getSerialNo()),itemID, bomMap.get("PARTNO"), bomMap.get("BOMLINEID"),"Workstation information is incorrect. Please check and correct",bomMap.get("ACTION"),"Error"};
																		bomManager.printReport("PRINT", printValues);
																		bomManager.incrementSerialNo();
																		isError = true;
																	}
																}

																String MESIndicator = operationBOMLine.getProperty("vf4_operation_type");
																if(MESIndicator.equals("NA") || MESIndicator.equals("")) {
																	MESIndicator = "N";
																}else {
																	MESIndicator = "Y";
																}

																String JES = operationBOMLine.getProperty("vf5_line_supply_method");
																if(JES.equals("NA") || JES.equals("")) {
																	JES = "JIS";
																}

																ArrayList<String> workStations = opWs.getWorkStationsID(ID);
																ArrayList<HashMap<String,String>> BOPMap = null;

																if(workStations.isEmpty() == false) {

																	BOPMap = new ArrayList<HashMap<String,String>>();

																	for(String wsID : workStations) {

																		HashMap<String, String> transferBOPMap = new HashMap<String, String>();
																		transferBOPMap.put("SAPPLANT", bomManager.getPlant());
																		transferBOPMap.put("BOMLINEID", bomMap.get("BOMLINEID"));
																		if(bomManager.getMaterialCode().equals("")) {

																			transferBOPMap.put("TOPLEVELPART",bomManager.getModel()+"_"+bomManager.getYear());
																			transferBOPMap.put("HEADERPART",bomManager.getModel()+"_"+bomManager.getYear());

																		}else {

																			transferBOPMap.put("TOPLEVELPART",bomManager.getMaterialCode());
																			transferBOPMap.put("HEADERPART",bomManager.getMaterialCode());
																		}

																		transferBOPMap.put("ACTION", "A");
																		transferBOPMap.put("WORKSTATION", wsID);
																		transferBOPMap.put("LINESUPPLYMETHOD", JES);
																		transferBOPMap.put("BOPID", ID);
																		transferBOPMap.put("MESBOPINDICATOR", MESIndicator);
																		transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
																		transferBOPMap.put("REVISION", operationBOMLine.getProperty("bl_rev_item_revision_id"));
																		transferBOPMap.put("MCN", bomManager.getMCN());
																		BOPMap.add(transferBOPMap);
																	}

																	solBOPDataMap.put(BOMkey, BOPMap);
																}
															}else {

																printValues = new String[] {Integer.toString(bomManager.getSerialNo()),itemID, bomMap.get("PARTNO"), bomMap.get("BOMLINEID"),"BOP is wrong. Please correct BOM and BOP link",bomMap.get("ACTION"),"Error"};
																bomManager.printReport("PRINT", printValues);
																bomManager.incrementSerialNo();
															}
														}else {

															isError = true;
														}
													}
												}
											}
										}

										if(isError == false) {

											PrepareBomBopData pD = new PrepareBomBopData();
											pD.setOldBOMDataMap(prDataMap);
											pD.setNewBOMDataMap(solBOMDataMap);
											pD.setNewBOPDataMap(solBOPDataMap);
											bombop2Send.put(itemID, pD);
											Set<String> deletedParts = SuperScooterTransfer.delPart(prDataMap, solBOMDataMap);
											Set<String> addedParts = SuperScooterTransfer.addPart(prDataMap, solBOMDataMap);

											HashMap<String, HashMap<String, String>> commonPartsMap = SuperScooterTransfer.commonPart(prDataMap, solBOMDataMap);

											Set<String> commonParts = commonPartsMap.keySet();

											if(deletedParts.isEmpty() == false){

												Iterator<String> iterator = deletedParts.iterator();

												while(iterator.hasNext()) {

													String delKey = iterator.next();

													HashMap<String, String> bomMap = prDataMap.get(delKey);

													printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),"BOM record will be send to SAP.",bomMap.get("ACTION"),"Info"};
													bomManager.printReport("PRINT", printValues);
													bomManager.incrementSerialNo();
												}
											}

											if(addedParts.isEmpty() == false){

												Iterator<String> iterator = addedParts.iterator();

												while(iterator.hasNext()) {

													String addKey = iterator.next();

													HashMap<String, String> bomMap = solBOMDataMap.get(addKey);

													printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),"BOM record will be send to SAP.",bomMap.get("ACTION"),"Info"};
													bomManager.printReport("PRINT", printValues);

													ArrayList<HashMap<String, String>> bopMap = solBOPDataMap.get(addKey);

													for(HashMap<String, String> BOP : bopMap) {

														printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"),"BOP record will be send to SAP.",bomMap.get("ACTION"),"Info"};
														bomManager.printReport("PRINT", printValues);
													}


													bomManager.incrementSerialNo();
												}
											}

											if(commonParts.isEmpty() == false){

												Iterator<String> iterator = commonParts.iterator();

												while(iterator.hasNext()) {

													String commKey = iterator.next();

													HashMap<String, String> bomMap = solBOMDataMap.get(commKey);

													bomMap.replace("ACTION", bomMap.get("ACTION"), "C");

													printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),"BOM record will be send to SAP.",bomMap.get("ACTION"),"Info"};
													bomManager.printReport("PRINT", printValues);
													bomManager.incrementSerialNo();
												}
											}
										}
									}
								}

							}

						}
						if(problemItems != null) {

							for(TCComponent problemItem : problemItems){

								TCComponentItemRevision problem = (TCComponentItemRevision)problemItem;

								CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(bomManager.getSession(), problem);

								TCComponentBOMWindow operationWindow = output[0].bomWindow;
								TCComponentBOMLine operationLine = output[0].bomLine;

								if(UIGetValuesUtility.hasMaterials(operationLine) == false){

									dmCoreService.getProperties(new TCComponent[] {operationLine},new String[] {"bl_item_item_id","bl_rev_item_revision_id","VF4_bomline_id","vf4_operation_type","bl_rev_vf3_transfer_to_sap","bl_rev_vf5_line_supply_method","vf4_user_notes"});

									String ID = operationLine.getProperty("bl_item_item_id");
									String RevID = operationLine.getProperty("bl_rev_item_revision_id");
									String HT = "";
									if(bomManager.getMaterialCode().equals("")) {
										HT = bomManager.getModel()+"_"+bomManager.getYear();
									}else {
										HT = bomManager.getMaterialCode();
									}

									if(HT.equals("")) {

										printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",ID+"/"+RevID,"","Header Part information missing on operation. Fill in Bomline","-","Error"};
										bomManager.printReport("PRINT", printValues);
										bomManager.incrementSerialNo();

									}else {

										String WS = UIGetValuesUtility.getWorkStation(new String[] {"vf3_transfer_to_mes", "vf4_user_notes"}, problem);
										String[] split = WS.split(",");

										String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
										if(LSM.equals("")) {
											LSM = "JIS";
										}

										String MES = operationLine.getProperty("vf4_operation_type");
										if(MES.equals("") || MES.equals("NA")) {
											MES = "N";
										}else {
											MES = "Y";
										}

										for(String workstation : split) {

											HashMap<String, String> BOP_Values = new HashMap<String, String>();
											BOP_Values.put("SAPPLANT", bomManager.getPlant());
											BOP_Values.put("TOPLEVELPART", HT);
											BOP_Values.put("HEADERPART", HT);
											BOP_Values.put("BOMLINEID", "");
											BOP_Values.put("ACTION", "D");
											BOP_Values.put("WORKSTATION", workstation);
											BOP_Values.put("LINESUPPLYMETHOD",LSM);
											BOP_Values.put("BOPID",ID);
											BOP_Values.put("MESBOPINDICATOR",MES);
											BOP_Values.put("REVISION",RevID);
											BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											BOP_Values.put("MCN", bomManager.getMCN());
											operationNoPart2Send.add(BOP_Values);

											printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",BOP_Values.get("BOPID")+"/"+BOP_Values.get("REVISION"),"","BOP record will be send to SAP.",BOP_Values.get("ACTION"),"Info"};
											bomManager.printReport("PRINT", printValues);
											bomManager.incrementSerialNo();
										}

									}
								}
								operationWindow.close();
							}
						}

						if(solutionItems != null) {

							for (TCComponent solutionItem : solutionItems)
							{

								TCComponentItemRevision operations = (TCComponentItemRevision)solutionItem;

								String ID = operations.getProperty("item_id");

								ArrayList<TCComponent> operationsList = UIGetValuesUtility.searchPartsInStruture(bomManager.getSession(), new String[] {ID}, bomManager.getMBOMTraverseLine());

								if(operationsList.isEmpty() == false) {

									TCComponentBOMLine operationLine = (TCComponentBOMLine)operationsList.get(0);

									if(UIGetValuesUtility.hasMaterials(operationLine) == false){

										dmCoreService.getProperties(new TCComponent[] {operationLine},new String[] {"bl_item_item_id","bl_rev_item_revision_id","VF4_bomline_id","vf4_operation_type","bl_rev_vf3_transfer_to_sap","bl_rev_vf5_line_supply_method"});

										String RevID = operationLine.getProperty("bl_rev_item_revision_id");
										String HT = bomManager.getModel()+"_"+bomManager.getYear();

										if(HT.equals("")) {

											printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",ID+"/"+RevID,"","Header Part information missing on operation. Fill in Bomline","-","Error"};
											bomManager.printReport("PRINT", printValues);
											bomManager.incrementSerialNo();

										}else {

											String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
											if(LSM.equals("")) {
												LSM = "JIS";
											}

											String MES = operationLine.getProperty("vf4_operation_type");
											if(MES.equals("") || MES.equals("NA")) {
												MES = "N";
											}else {
												MES = "Y";
											}

											for(TCComponent operation : operationsList) {

												HashMap<String, String> BOP_Values = new HashMap<String, String>();
												BOP_Values.put("SAPPLANT", bomManager.getPlant());
												BOP_Values.put("TOPLEVELPART", HT);
												BOP_Values.put("HEADERPART", HT);
												BOP_Values.put("BOMLINEID", "");
												BOP_Values.put("ACTION", "A");
												BOP_Values.put("WORKSTATION", UIGetValuesUtility.getWorkStationID((TCComponentBOMLine)operation, "bl_rev_object_name"));
												BOP_Values.put("LINESUPPLYMETHOD",LSM);
												BOP_Values.put("BOPID",ID);
												BOP_Values.put("MESBOPINDICATOR",MES);
												BOP_Values.put("REVISION",RevID);
												BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
												BOP_Values.put("MCN", bomManager.getMCN());
												operationNoPart2Send.add(BOP_Values);

												printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",BOP_Values.get("BOPID")+"/"+BOP_Values.get("REVISION"),"","BOP record will be send to SAP.",BOP_Values.get("ACTION"),"Info"};
												bomManager.printReport("PRINT", printValues);
												bomManager.incrementSerialNo();
											}

										}
									}

								}
							}
						}

						if(bomManager.isWindowOpen()) {
							monitor.subTask("Closing MBOM Strucuture...");
							bomManager.closeMBOMBOPWindows();
						}

					}catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotLoadedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			bomManager.finishReport("");
			bomManager.popupReport("BOM");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public int getProgress(int current, int total) {

		float value = (float)current/(float)total;

		int percent = (int) (value*100);

		return percent;

	}

	public void realTransfer(BOMBOPDialog transferDlg, BOMManager bomManager) {
		// TODO Auto-generated method stub
		String auth = serverInfo.getAuth();
		String SERVER_IP = serverInfo.getServerIP();
		SAPURL SAPConnect		 	= new SAPURL();
		String server = transferDlg.getServer();
		bomManager.resetSerialNo();
		bomManager.initReport();
		try {
			DataManagementService dmCoreService = DataManagementService.getService(bomManager.getSession());
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(transferDlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Transferring Data...", IProgressMonitor.UNKNOWN);
					try {
						String logFolder = UIGetValuesUtility.createLogFolder("MCN"+bomManager.getMCN());
						String[] printValues = null;
						for (Map.Entry<String, PrepareBomBopData> entry : bombop2Send.entrySet()) {
							String itemID = entry.getKey();
							PrepareBomBopData data2Send = entry.getValue();
							HashMap<String, HashMap<String, String>>  prDataMap = data2Send.getOldBOMDataMap();
							HashMap<String, HashMap<String, String>>  solBOMDataMap = data2Send.getNewBOMDataMap();
							HashMap<String, ArrayList<HashMap<String,String>>>  solBOPDataMap = data2Send.getNewBOPDataMap();
							if(prDataMap.size() > 0) {
								Set<String> deletedParts = SuperScooterTransfer.delPart(prDataMap, solBOMDataMap);
								Set<String> addedParts = SuperScooterTransfer.addPart(prDataMap, solBOMDataMap);
								HashMap<String, HashMap<String, String>> commonPartsMap = SuperScooterTransfer.commonPart(prDataMap, solBOMDataMap);

								Set<String> commonParts = commonPartsMap.keySet();

								if(deletedParts.isEmpty() == false){

									Iterator<String> iterator = deletedParts.iterator();

									while(iterator.hasNext()) {

										String delKey = iterator.next();

										HashMap<String, String> bomMap = prDataMap.get(delKey);

										String BOMFile = bomMap.get("PARTNO") + "_" + bomMap.get("BOMLINEID");
										String[] message = null;
										message = CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(SERVER_IP), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
										if(message[0].equals("S")){
											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"),"Success"};
											bomManager.printReport("PRINT", printValues);
										}else {
											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"),"Error"};
											bomManager.printReport("PRINT", printValues);
										}
										bomManager.incrementSerialNo();
									}
								}

								if(addedParts.isEmpty() == false){

									Iterator<String> iterator = addedParts.iterator();

									while(iterator.hasNext()) {

										String addKey = iterator.next();

										HashMap<String, String> bomMap = solBOMDataMap.get(addKey);

										String BOMFile = bomMap.get("PARTNO") + "_" + bomMap.get("BOMLINEID");
										String[]  message = null;
										message =  CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(SERVER_IP), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
										if(message[0].equals("S")){

											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Success"};
											bomManager.printReport("PRINT", printValues);

											ArrayList<HashMap<String, String>> bopMap = solBOPDataMap.get(addKey);

											for(HashMap<String, String> BOP : bopMap) {

												String BOPFile = BOP.get("BOPID") + "_" + BOP.get("WORKSTATION");
												String []msg = null;
												msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP,SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOP.get("ACTION")+"_"+ BOPFile, logFolder, auth);
												if(msg[0].equals("S")){

													printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"), msg[1],BOP.get("ACTION"),"Success"};
													bomManager.printReport("PRINT", printValues);
												}else {
													printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"),msg[1],BOP.get("ACTION"),"Error"};
													bomManager.printReport("PRINT", printValues);
												}
											}

										}else {
											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Error"};
											bomManager.printReport("PRINT", printValues);
										}

										bomManager.incrementSerialNo();
									}
								}

								if(commonParts.isEmpty() == false){

									Iterator<String> iterator = commonParts.iterator();

									while(iterator.hasNext()) {

										String commKey = iterator.next();

										HashMap<String, String> bomMap = solBOMDataMap.get(commKey);

										bomMap.replace("ACTION", bomMap.get("ACTION"), "C");

										String BOMFile = bomMap.get("PARTNO") + "_" + bomMap.get("BOMLINEID");
										String[] message = null;
										message = CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(SERVER_IP), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
										if(message[0].equals("S")){

											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Success"};
											bomManager.printReport("PRINT", printValues);
										}else {
											printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Error"};
											bomManager.printReport("PRINT", printValues);
										}
										bomManager.incrementSerialNo();
									}
								}
								if(server.equals("PRODUCTION"))
								{
									TCComponentItemRevision solution_Rev = data2Send.getNewItemRevision();
									UIGetValuesUtility.setProperty(dmCoreService, solution_Rev, "vf3_transfer_to_sap", "Yes");
								}
							}else {
								Set<String> keys = solBOMDataMap.keySet();

								for(String addKey : keys) {

									HashMap<String, String> bomMap = solBOMDataMap.get(addKey);

									String BOMFile = bomMap.get("PARTNO") + "_" + bomMap.get("BOMLINEID");
									String[]  message = null;
									message = CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(SERVER_IP), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
									if(message[0].equals("S")){

										printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Success"};
										bomManager.printReport("PRINT", printValues);

										ArrayList<HashMap<String, String>> bopMap = solBOPDataMap.get(addKey);

										for(HashMap<String, String> BOP : bopMap) {

											String BOPFile = BOP.get("BOPID") + "_" + BOP.get("WORKSTATION");
											String []msg = null;
											msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP,SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOP.get("ACTION")+"_"+ BOPFile, logFolder, auth);
											if(msg[0].equals("S")){

												printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"),msg[1],BOP.get("ACTION"),"Success"};
												bomManager.printReport("PRINT", printValues);
											}else {
												printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"),msg[1],BOP.get("ACTION"),"Error"};
												bomManager.printReport("PRINT", printValues);
											}
										}

									}else {
										printValues = new String[]{Integer.toString(bomManager.getSerialNo()),itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Error"};
										bomManager.printReport("PRINT", printValues);
									}
									bomManager.incrementSerialNo();
								}

								if(server.equals("PRODUCTION"))
								{
									TCComponentItemRevision solution_Rev = data2Send.getNewItemRevision();
									UIGetValuesUtility.setProperty(dmCoreService, solution_Rev, "vf3_transfer_to_sap", "Yes");
								}

							}
						}
						Iterator<HashMap<String, String>> iter = operationNoPart2Send.iterator();

						while (iter.hasNext()) {
							HashMap<String, String> BOP_Values = iter.next();
							String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
							String[] msg = null;
							msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values,SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOP_Values.get("ACTION")+"_"+ BOPFile, logFolder, auth);
							if(msg[0].equals("E")){
								printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",BOP_Values.get("BOPID")+"/"+BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"),"Error"};
								bomManager.printReport("PRINT", printValues);

							}else{
								printValues = new String[] {Integer.toString(bomManager.getSerialNo()),"OWP",BOP_Values.get("BOPID")+"/"+BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"),"Success"};
								bomManager.printReport("PRINT", printValues);

							}
							bomManager.incrementSerialNo();
						}


						String previousData = Logger.previousTransaction(logFolder, "BOM");
						bomManager.finishReport(previousData);
						File file = bomManager.popupReport("BOM");
						if (server.equals("PRODUCTION") == true) {
							TCComponentDataset newDataset = UIGetValuesUtility.createDataset(DataManagementService.getService(bomManager.getSession()), bomManager.getChangeObject(), "IMAN_specification", file.getName(), "Transfer Report", "HTML", "IExplore");
							if (newDataset != null) {
								UIGetValuesUtility.uploadNamedReference(bomManager.getSession(), newDataset, file, "HTML", true, true);
							}
						}


					}catch(Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
