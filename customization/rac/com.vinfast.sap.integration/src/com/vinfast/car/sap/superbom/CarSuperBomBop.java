package com.vinfast.car.sap.superbom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.car.sap.sw.UpdateSWBOMValidator;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMBOPData.FileType;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.dialogs.CarSuperBomBopDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.MutableBoolean;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;

public class CarSuperBomBop extends AbstractHandler {
	private TCSession session = null;
	private TCComponentItemRevision selectedObject = null;
	private CarSuperBomBopDialog dlg = null;
	private CarSuperBomBopBusiness business = null;
	private DataManagementService dmService = null;
	private OrganizationInformationAbstract serverInfo = null;
	private String revisionRule = PropertyDefines.REVISION_RULE_WORKING;
	boolean isSWBOM = false;
	private static ArrayList<String> operationIDs = null;
	private ProgressMonitorDialog progressDlg = null;
	private boolean isSuccess = false;
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			revisionRule = selecteRule(event);
			business = new CarSuperBomBopBusiness();
			String cmd = event.getCommand().toString();

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			VFResponse response = MCNValidator.validate(targetComponents);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}else {
				selectedObject = (TCComponentItemRevision)targetComponents[0];
			}

			BOMManager BOMManager = new BOMManager(UIGetValuesUtility.getCompanyCode(event)).loadChangeAttachments(session, selectedObject);

			if (BOMManager == null) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			dlg = new CarSuperBomBopDialog(new Shell(), isSWBOM);
			dlg.create();
			if (revisionRule.equals(PropertyDefines.REVISION_RULE_WORKING)) {
				dlg.setTitle("CAR SUPER BOM&BOP TRANSFER");
			} else {
				dlg.setTitle("SOFTWARE BOM&BOP TRANSFER");
			}

			dlg.txtModel.setText(BOMManager.getModel());
			dlg.txtYear.setText(BOMManager.getYear());
			dlg.txtShop.setText(BOMManager.getShopName());
			dlg.txtPlant.setText(BOMManager.getPlant());
			dlg.txtMCN.setText(BOMManager.getMCN());
			dlg.cbServer.setText("PRODUCTION");

			dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					dlg.btnPrepare.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								boolean prepareDataSuccess = prepareData(BOMManager);
								dlg.btnTransfer.setEnabled(prepareDataSuccess);
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});

			dlg.btnTransfer.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					dlg.btnTransfer.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, dlg.cbServer.getText(), session);
							transferRecordsToSAP(BOMManager);
							dlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnUpdateSWBOM.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					dlg.btnTransfer.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							TCComponent component = UIGetValuesUtility.getComponent();
							String err = UpdateSWBOMValidator.isValidSWBOM(component, BOMManager.getSession());
							if (err.isEmpty()) {
								try {
									dlg.txtSWBOM.setText(component.getProperty(PropertyDefines.BOM_ITEM_ID));
								} catch (TCException e) {
									e.printStackTrace();
								}
							} else {
								MessageBox.post(err, "Error", MessageBox.ERROR);
							}
						}
					});
				}
			});

			dlg.btnTransfer.setEnabled(false);

			dlg.open();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		return null;
	}

	private boolean prepareData(BOMManager BOMManager) throws TCException {
		isSuccess = false;
		if (isSWBOM && !preCheckSyncBetweenSWBOMAndMBOM(BOMManager, dlg.txtSWBOM.getText())) {
			return false;
		}
		String serverValue = dlg.cbServer.getText();
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(dlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Preparing Data...", IProgressMonitor.UNKNOWN);
					try {
						BOMManager.initReport();
						BOMManager.initReport2();

						HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOMValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOMValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						HashMap<TCComponent, HashMap<String, BOMBOPData>> OWPBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						monitor.subTask("Loading MCN...");
						operationIDs = new ArrayList<String>();
						TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, BOMManager.getChangeObject(), new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);

						for (TCComponent operation : solutionItems) {
							operationIDs.add(operation.getProperty(PropertyDefines.ITEM_ID));
						}

						// open working revision data
						String[] loadSubGroupIDs = objectsNotYetTransferToSAP(BOMManager, BOMManager.getImpactedItems(), serverValue);
						String[] loadOperationIDs = objectsNotYetTransferToSAP(BOMManager, BOMManager.getSolutionItems(), serverValue);
						monitor.subTask("Loading Solution Subgroup Items...");
						if (!loadSubGroupIDs[0].isEmpty()) {
							if (!BOMManager.isWindowOpen()) {
								BOMManager.createMBOMBOPWindow(revisionRule);
							}
							currentBOMValues = CarSuperBomBopLoadData.loadSubGroupChilds(BOMManager, monitor, loadSubGroupIDs[0]);
						}
						monitor.subTask("Loading Solution Operations...");
						if (BOMManager.hasError() == false) {
							if (!loadOperationIDs[0].isEmpty()) {
								if (!BOMManager.isWindowOpen())
									BOMManager.createMBOMBOPWindow(revisionRule);
								currentBOPValues = CarSuperBomBopLoadData.loadOperationChilds(BOMManager, monitor, loadOperationIDs[0]);
							}
						}
						monitor.subTask("Loading Solution Operations No Part...");
						if (!BOMManager.hasError()) {
							if (BOMManager.isWindowOpen()) {
								OWPBOPValues.putAll(CarSuperBomBopLoadData.loadOperationsNoPart(BOMManager, monitor, false));
							}
						}

						if (BOMManager.isWindowOpen()) {
							BOMManager.closeMBOMBOPWindows();
						}

						// open previous revision data
						MutableBoolean isEnoughSubgroupDataSet = new MutableBoolean(false);
						monitor.subTask("Loading Problem Subgroup items...");
						releaseBOMValues = CarSuperBomBopLoadData.loadSubGroupReleaseData(BOMManager, loadSubGroupIDs[1], isEnoughSubgroupDataSet);
						String problemItems = getProblemOperationIds(BOMManager, BOMManager.getProblemItems(), loadOperationIDs[1]);
						loadOperationIDs[1] = loadOperationIDs[1] + ";" + problemItems;

						MutableBoolean isEnoughOperationDataSet = new MutableBoolean(false);
						HashMap<TCComponent, HashMap<String, BOMBOPData>> savedOpertionHavePartData = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						HashMap<TCComponent, HashMap<String, BOMBOPData>> savedOpertionNoPartData = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
						monitor.subTask("Loading Problem Operations...");
						CarSuperBomBopLoadData.loadOperationReleaseData(BOMManager, loadOperationIDs[1], isEnoughOperationDataSet, savedOpertionHavePartData, savedOpertionNoPartData);
						if (isEnoughOperationDataSet.bVal) {
							releaseBOPValues = savedOpertionHavePartData;
							OWPBOPValues.putAll(savedOpertionNoPartData);
						} else if (BOMManager.hasError() == false) {
							if (loadOperationIDs[1].equals("") == false) {
								if (BOMManager.isWindowOpen() == false) {
									if (BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_RELEASE)) {
										releaseBOPValues = CarSuperBomBopLoadData.loadOperationChilds(BOMManager, monitor, loadOperationIDs[1]);
									}
								} else {
									releaseBOPValues = CarSuperBomBopLoadData.loadOperationChilds(BOMManager, monitor, loadOperationIDs[1]);
								}
							}
							monitor.subTask("Loading Problem Operations No Part...");
							if (!BOMManager.hasError()) {
								if (BOMManager.isWindowOpen()) {
									OWPBOPValues.putAll(CarSuperBomBopLoadData.loadOperationsNoPart(BOMManager, monitor, true));
								}
							}
						}

						// ========================================================================================================
						if (BOMManager.isWindowOpen()) {
							BOMManager.closeMBOMBOPWindows();
						}
						monitor.subTask("Comparing Problem and Solution Data...");
						if (BOMManager.hasError() == false) {
							business.setOWPBOPValues(OWPBOPValues);
							business.setWorkingBOMValues(currentBOMValues);
							business.setWorkingBOPValues(currentBOPValues);
							business.setReleaseBOMValues(releaseBOMValues);
							business.setReleaseBOPValues(releaseBOPValues);
							business.setBomManager(BOMManager);
							isSuccess = business.process();
						}

					}catch (Exception e) {
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

		if(!BOMManager.getErrorMessage().isBlank()) {
			MessageBox.post(BOMManager.getErrorMessage(), "Error", MessageBox.ERROR);
		}else {
			BOMManager.finishReport2("");
			BOMManager.popupReport2("BOM");
			BOMManager.finishReport(Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()), "BOM"));
			BOMManager.popupReport("BOM");
		}
		
		return isSuccess;
	}

	private String[] objectsNotYetTransferToSAP(BOMManager BOMManager, TCComponent[] objects, String server) {
		String res[] = new String[2];
		Set<String> searchWorkingIds = new HashSet<String>();
		Set<String> searchReleaseIds = new HashSet<String>();
		try {
			if (objects != null) {
				for (TCComponent objectRevision : objects) {
					String itemID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String itemRevID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					String impactedRevisionID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);
					impactedRevisionID = impactedRevisionID.substring(0, impactedRevisionID.indexOf("-"));
					if (!StringExtension.isInteger(itemRevID, 10)) {
						BOMManager.printReport("PRINT", new String[] { Integer.toString(BOMManager.getSerialNo()), impactedRevisionID, "-", "-", "Revision number not valid.", "-", "Info" });
					} else {
						int revID = Integer.parseInt(itemRevID);
						if (server.compareToIgnoreCase("PRODUCTION") == 0 || server.compareToIgnoreCase("QA") == 0) {
							String revToSap = objectRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
							if (revToSap.isEmpty() || !revToSap.contains(server)) {
								searchWorkingIds.add(itemID);
								if (revID > 1)
									searchReleaseIds.add(itemID);
							} else {
								BOMManager.printReport("PRINT", new String[] { Integer.toString(BOMManager.getSerialNo()), impactedRevisionID, "-", "-", "Already transferred to SAP.", "-", "Info" });
							}
						} else {
							searchWorkingIds.add(itemID);
							if (revID > 1)
								searchReleaseIds.add(itemID);
						}
					}
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		res[0] = String.join(";", searchWorkingIds);
		res[1] = String.join(";", searchReleaseIds);

		return res;
	}

	private String getProblemOperationIds(BOMManager BOMManager, TCComponent[] objects, String existedItem) {
		Set<String> res = new HashSet<String>();
		if (objects != null && objects.length > 0) {
			for (TCComponent objectRevision : objects) {
				String itemID = "";
				try {
					itemID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!itemID.isEmpty() && !existedItem.contains(itemID)) {
					res.add(itemID);
				}
			}
		}
		return String.join(";", res);
	}

	private void transferRecordsToSAP(BOMManager BOMManager) {
		isSuccess = true;
		String serverValue = dlg.cbServer.getText();
		String userName = BOMManager.getSession().getUserName();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN());
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(dlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Transferring...", IProgressMonitor.UNKNOWN);
					BOMManager.initReport();
					BOMManager.initReport2();
					TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(session);
					ArrayList<SuperBomBopTransferData> list = business.getData2Send();
					int count = 0;
					try {
						// transfer all
						monitor.subTask("Sending records to SAP...");
						for (int i = 0; i < list.size(); i++) {
							boolean isTransferComponentOk = true;
							ArrayList<BOMBOPData> dataMaps = list.get(i).getData2Send();
							for (BOMBOPData dataMap : dataMaps) {
								HashMap<String, String> listXmlData = dataMap.getXmlData();
								String[] sapUrlInfo = dataMap.getSAPWebServiceInfo();
								if (serverValue.equals("PRODUCTION") || serverValue.equals("QA") || serverValue.equals("DEV")) {
									String msg[] = CreateSoapHttpRequest.sendRequest(dataMap.getSAPWebServiceUrl(serverInfo.getServerIP()), listXmlData, sapUrlInfo[0], sapUrlInfo[1], sapUrlInfo[2], dataMap.getXmlFileName(FileType.IN_PUT, count++), logFolder, serverInfo.getAuth());
									if (msg != null) {
										if (msg[0].equals("S")) {
											if (dataMap.isBomData()) {
												BOMManager.printReport("PRINT", dataMap.getPrintResultMessage(msg[1], true, count));
											} else {
												BOMManager.printReport2("PRINT", dataMap.getPrintResultMessage(msg[1], true, count));
											}
										} 
										else if (msg[0].equals("E") && msg[1].contains("exist the records with the same keys")) {
											if (dataMap.isBomData()) {
												BOMManager.printReport("WARN", dataMap.getPrintResultMessage(msg[1], false, count));
											} else {
												BOMManager.printReport2("WARN", dataMap.getPrintResultMessage(msg[1], false, count));
											}
										}else if (msg[0].equals("E03") && msg[1].contains("already existed in staging")) {
											if (dataMap.isBomData()) {
												BOMManager.printReport("WARN", dataMap.getPrintResultMessage(msg[1], false, count));
											} else {
												BOMManager.printReport2("WARN", dataMap.getPrintResultMessage(msg[1], false, count));
											}
										} else {
											if (dataMap.isBomData()) {
												BOMManager.printReport("PRINT", dataMap.getPrintResultMessage(msg[1], false, count));
											} else {
												BOMManager.printReport2("PRINT", dataMap.getPrintResultMessage(msg[1], false, count));
											}
											isTransferComponentOk = false;
											isSuccess = false;
											break;
										}
									} else {
										String[] msgNullAnoucement = new String[] { Integer.toString(count), "...", "...", "....", "Response message is null!", "...", "Error" };
										if (dataMap.isBomData()) {
											BOMManager.printReport("PRINT", msgNullAnoucement);
										} else {
											BOMManager.printReport2("PRINT", msgNullAnoucement);
										}

										isTransferComponentOk = false;
										isSuccess = false;
										break;
									}
								}
							}

							// set flag
							if (isTransferComponentOk && (serverValue.equals("PRODUCTION") || serverValue.equals("QA")) && UIGetValuesUtility.checkAccess(session, groupMember, list.get(i).getItem())) {
								LocalDateTime now = LocalDateTime.now();
								String setValue = String.format("%s~%s~%s", serverValue, userName, dtf.format(now));
								UIGetValuesUtility.setProperty(dmService, list.get(i).getItem(), PropertyDefines.REV_TO_SAP, setValue);
							}
							CarSuperBomBopLoadData.uploadDataSet(list.get(i), BOMManager, logFolder);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
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

		// load previous report
		String previousReport = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()), "BOM");
		BOMManager.finishReport2(previousReport);
		BOMManager.popupReport2("BOM");

		previousReport = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()), "BOM");
		BOMManager.finishReport(previousReport);
		File logReport = BOMManager.popupReport("BOM");

		// upload html report file
		if (isSuccess && serverValue.equals("PRODUCTION")) {
			TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, BOMManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
			if (newDataset != null) {
				UIGetValuesUtility.uploadNamedReference(BOMManager.getSession(), newDataset, logReport, "HTML", true, true);
			}
		}

		File btr = createBomTrackingDataFile(String.format("%s\\%s", logFolder, String.format("%s_%s.txt", BOMManager.getMCN(), "BOM_TRACKING")), business.getBomTrackingData());
		TCComponentDataset newBomTrackingData = UIGetValuesUtility.createDataset(dmService, BOMManager.getChangeObject(), "IMAN_specification", btr.getName(), "BomTracking RawData", "Text", "TextEditor");
		if (newBomTrackingData != null) {
			UIGetValuesUtility.uploadNamedReference(BOMManager.getSession(), newBomTrackingData, btr, "Text", true, true);
		}
	}

	private File createBomTrackingDataFile(String fileName, StringBuffer data) {
		File btr = new File(fileName);
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(btr));
			output.write(data.toString());
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return btr;
	}

	private String selecteRule(ExecutionEvent event) {
		String cmd = event.getCommand().toString();
		if (cmd.contains("SuperBOM")) {
			isSWBOM = false;
			return PropertyDefines.REVISION_RULE_WORKING;
		} else if (cmd.contains("SuperSwBOM")) {
			isSWBOM = true;
			return PropertyDefines.REVISION_RULE_WORKING_PRECISE;
		}
		return PropertyDefines.REVISION_RULE_WORKING;
	}

	private boolean preCheckSyncBetweenSWBOMAndMBOM(BOMManager BOMManager, String swBOMID) {
		try {
			Date lastTimeUpdateReport = null;
			Date lastTimeModifiedOfBomView = null;
			TCComponent[] subgroups = BOMManager.getImpactedItems();
			boolean isFound = false;
			TCComponent swSubGroup = null;
			for (TCComponent subgroup : subgroups) {
				String subGroupID = subgroup.getProperty(PropertyDefines.ITEM_ID);
				String reportFileName = String.format("SW_COMPARE_%s_%s", "M" + subGroupID, "S" + swBOMID);
				TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(BOMManager.getSession(), new String[] { "Name", "Dataset Type" }, new String[] { reportFileName, "HTML" }, "Dataset...");
				if (oldDatasets != null) {
					dmService.getProperties(oldDatasets, new String[] { "creation_date" });

					for (TCComponent dataset : oldDatasets) {
						Date date_creation = dataset.getDateProperty("creation_date");
						if (lastTimeUpdateReport == null || lastTimeUpdateReport.after(date_creation)) {
							lastTimeUpdateReport = date_creation;
							isFound = true;
							break;
						}
					}
				}
				if (isFound) {
					swSubGroup = subgroup;
					break;
				}
			}

			if (lastTimeUpdateReport == null) {
				MessageBox.post("Can't get last time updating validation report. Please validate MBOM & SW BOM before transfer.", "Error", MessageBox.ERROR);
				return false;
			}

			if (BOMManager.getImpactedItems() == null || BOMManager.getImpactedItems().length == 0) {
				return false;
			} else {
				TCComponentItemRevision rev = (TCComponentItemRevision) swSubGroup;
				TCComponent bomView = rev.getRelatedComponent(PropertyDefines.ITEM_STRUCTURE_REVISION);
				lastTimeModifiedOfBomView = bomView.getDateProperty("last_mod_date");
			}

			if (lastTimeModifiedOfBomView == null) {
				MessageBox.post("Can't get last time modifying MBOM. Please contact admin for more details.", "Error", MessageBox.ERROR);
				return false;
			}

			if (lastTimeModifiedOfBomView.after(lastTimeUpdateReport)) {
				MessageBox.post(String.format("Last Time BOMView Modified is: %s.\n" + "Last Time validate MBOM & SWBOM is: %s.\n" + "Please validate the sync between MBOM & SW BOM before transfer by using menu <Update SoftwareBOM Validation>", lastTimeModifiedOfBomView.toString(), lastTimeUpdateReport.toString()), "Error", MessageBox.ERROR);
				return false;
			} else {
				return true;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isOperationInMCN(String operationID) {
		if (operationIDs.contains(operationID)) {
			return true;
		} else {
			return false;
		}
	}
}
