package com.vinfast.car.mes.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.JSONObject;

import com.teamcenter.integration.dialog.MESTransferDialog;
import com.teamcenter.integration.model.BaseModel;
import com.teamcenter.integration.model.MaterialTransferModel;
import com.teamcenter.integration.ulti.BOMManagement;
import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.bom.MEOPData;
import com.vinfast.sap.services.CallMESWebService;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;

public class OperationTransfer extends AbstractHandler {
	private TCSession session;
	private MESTransferDialog dlg;
	private ProgressMonitorDialog progressDlg;
	private DataManagementService dmService = null;
	private BOMManagement bomManager = null;

	private String revisionRule = "VINFAST_WORKING_RULE";
	private boolean isSWOperationTransfer = false;
	private boolean transferValid = true;
	private boolean transferByAPI = false;
	private static LinkedList<MEOPData> operationTransferList;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) selectedObjects[0];
			session = changeRevision.getSession();
			
			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}
			
			dmService = DataManagementService.getService(session);

			revisionRule = selecteRule(event);

			String company = PropertyDefines.VIN_FAST;
			String command = event.getCommand().toString();
			if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
				company = PropertyDefines.VIN_FAST_ELECTRIC;
			} else if (command.contains(PropertyDefines.VIN_ES)) {
				company = PropertyDefines.VIN_ES;
			}
			bomManager = new BOMManagement(session, changeRevision, company);
			if (bomManager == null) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			String[] preferenceValue = TCExtension.GetPreferenceValues("VF_MES_TRANSFER_API", session);
			if (preferenceValue != null && preferenceValue.length > 0) {
				transferByAPI = preferenceValue[0].compareTo("true") == 0;
			}

			dlg = new MESTransferDialog(new Shell(), session, company, transferByAPI);
			dlg.create();
			dlg.progressBar.setVisible(false);
			dlg.setMessage("Please Prepare Data before Transfer.", IMessageProvider.INFORMATION);
			if (isSWOperationTransfer) {
				dlg.setTitle("Software Operation(s) Transfer");
			} else {
				dlg.setTitle("Operation(s) Transfer");
			}

			dlg.setPlant(bomManager.getPlant());
			dlg.setMCN(bomManager.getMCN());
			dlg.txtShop.setText(bomManager.getShop());
			dlg.setTotal(10);

			if (session.getRole().getRoleName().compareToIgnoreCase("DBA") == 0) { // || role.toString().compareToIgnoreCase("MB Engineer") == 0) {
				dlg.lblSendByServer.setVisible(true);
				dlg.ckbSendByServer.setVisible(true);
			}

			dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					try {
						prepareData(dlg.cbServer.getText());
						dlg.btnSave.setEnabled(transferValid);
						if (transferValid) {
							dlg.setMessage("Prepare success. You can transfer data.", IMessageProvider.INFORMATION);
						} else {
							dlg.setMessage("Data not valid. Please check report.", IMessageProvider.ERROR);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.btnSave.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					if (dlg.getServer().isEmpty()) {
						MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
						return;
					}

					dlg.btnSave.setEnabled(false);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (dlg.ckbSendByServer.getSelection())
								transferDataBySOAService();
							else {
								if (transferByAPI)
									transferDataByJson();
								else
									transferData();
							}
						}
					});
				}
			});

			dlg.btnSave.setEnabled(false);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void prepareData(String server) {
		transferValid = true;

		TCComponent[] solutionItems = bomManager.getSolutionItems(PropertyDefines.TYPE_OPERATION_REVISION);
		if (solutionItems == null || solutionItems.length == 0) {
			MessageBox.post("No operations found in Solution Items.", "Error", MessageBox.ERROR);
			return;
		}
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(dlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Data preparing...", IProgressMonitor.UNKNOWN);
					try {
						int stepCount = 0;
						operationTransferList = new LinkedList<MEOPData>();
						Set<String> searchOperationID = new HashSet<String>();
						for (TCComponent objectRevision : solutionItems) {
							monitor.subTask("Get Data: " + ++stepCount + "/" + solutionItems.length + " OPE");

							MEOPData newItem = new MEOPData(server.compareToIgnoreCase("PRODUCTION") == 0);
							newItem.setOperationRevObject((TCComponentItemRevision) objectRevision);
							newItem.setShop(bomManager.getShop());
							if (!newItem.isNoNeedTransfer())
								searchOperationID.add(newItem.getMEOPID());

							operationTransferList.add(newItem);
						}

						if (searchOperationID != null) {
							monitor.subTask("Open BOM window...");
							if (bomManager.isWindowOpen() == false)
								bomManager.createMBOMBOPWindowWithRule(revisionRule);

							// search operation list in BOP structure
							stepCount = 0;
							TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, String.join(";", searchOperationID), bomManager.getBOPTraverseLine());
							for (MEOPData opeTransferItem : operationTransferList) {
								monitor.subTask("Search Operation(s): " + ++stepCount + "/" + solutionItems.length);

								if (!opeTransferItem.isValidate() || opeTransferItem.isNoNeedTransfer())
									continue;

								if (foundObjects == null) {
									opeTransferItem.setMessage("Operation not have in BOP structure.");
									continue;
								}

								boolean checkHave = false;
								for (TCComponent lineObject : foundObjects) {
									TCComponentBOMLine object = (TCComponentBOMLine) lineObject;
									try {
										String operationID = object.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
										if (operationID.compareTo(opeTransferItem.getMEOPID()) == 0) {
											checkHave = true;
											boolean valid = validateOperation(object, opeTransferItem);
											if (!valid)
												transferValid = false;

											break;
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								if (!checkHave) {
									opeTransferItem.setMessage("Operation not have in BOP structure.");
									transferValid = false;
								}
							}

							boolean checkHaveOneTransfer = false;
							stepCount = 0;
							HashMap<TCComponent, TCComponent[]> childLines = UIGetValuesUtility.expandBOMOneLevel(session, foundObjects);
							for (MEOPData opeTransferItem : operationTransferList) {
								monitor.subTask("Validate Operation(s): " + ++stepCount + "/" + operationTransferList.size());

								if (!opeTransferItem.isValidate() || opeTransferItem.isNoNeedTransfer())
									continue;

								try {
									boolean valid = validateOperationChild(opeTransferItem.getOperationBomlineObject(), opeTransferItem, childLines.get(opeTransferItem.getOperationBomlineObject()));
									if (!valid)
										transferValid = false;
									else
										checkHaveOneTransfer = true;
								} catch (Exception e) {
									opeTransferItem.setMessage(e.toString());
									transferValid = false;
								}
							}
							if (transferValid) {
								transferValid = checkHaveOneTransfer;
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		refreshReport();
	}

	//Transfer by API is False
	private void transferData() {
		String MCNID = dlg.txtMCN.getText();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + MCNID);
		String server = dlg.cbServer.getText();
		String serverIP = dlg.cbIP.getText();

		if (progressDlg == null)
			progressDlg = new ProgressMonitorDialog(dlg.getShell());
		try {
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Data transfering...", IProgressMonitor.UNKNOWN);
					boolean statusError = false;
					int stepCount = 0;
					bomManager.printReport("APPEND", new String[] { "<html>", "<body>" });
					Set<String> topInfo = new HashSet<String>();
					topInfo.add("User: " + session.getUserName());
					topInfo.add("MCN:" + MCNID);
					topInfo.add("Server: " + server + " (" + serverIP + ")");
					topInfo.add("Time: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
					bomManager.printReport("DETAILS", topInfo.toArray(new String[0]));
					bomManager.printReport("HEADER", new String[] { "No", "Operation", "Message", "Operation Type", "Transfer Result" });
					// body
					try {
						for (MEOPData operationData : operationTransferList) {
							monitor.subTask("Transferring Operation(s): " + ++stepCount + "/" + operationTransferList.size());
							if (operationData.isValidate() || !operationData.isNoNeedTransfer()) {
								TCComponentItemRevision operationRevision = operationData.getOperationRevObject();
								String requestData;
								if (operationData.hasMultipleStations() == true) {
									requestData = Format2XML.formatOperation(operationData);
								} else {
									requestData = FormatXML.formatOperation(operationData);
								}
								if (requestData.length() != 0) {
									// save xml data
									String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + UIGetValuesUtility.getSequenceID() + ".html";
									BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
									String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
									File fileXml = new File(xmlUrl);
									writer.write(requestData.replaceAll("&", "_"));
									writer.newLine();
									writer.close();

									int returnCode = new CallMESWebService().callService(requestData, serverIP);
									switch (returnCode) {
									case 200:
										if (server.compareToIgnoreCase("PRODUCTION") == 0) {
											
											if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
											} else {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
											}

											TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
											if (newDataset != null) {
												UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
											}
										}
										String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Transferred to MES", operationData.getMEOPType(), "Success" };
										bomManager.printReport("PRINT", printValues);
										break;
									default:
										statusError = true;
										printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Failed transfer to MES", operationData.getMEOPType(), "Error" };
										bomManager.printReport("PRINT", printValues);
										break;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					//
					bomManager.printReport("APPEND", new String[] { "</table>" });

					String data = Logger.previousTransaction(logFolder, "OPERATION");
					if (!data.isEmpty())
						bomManager.printReport("APPEND", new String[] { "<br>", data });

					bomManager.printReport("APPEND", new String[] { "</body>", "</html>" });

					File logReport = null;
					if (isSWOperationTransfer)
						logReport = bomManager.generateReport("SW_OPERATION");
					else
						logReport = bomManager.generateReport("OPERATION");

					if (!statusError && server.equals("PRODUCTION")) {
						TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, bomManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
						if (newDataset != null) {
							UIGetValuesUtility.uploadNamedReference(session, newDataset, logReport, "HTML", true, true);
						}
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		refreshResultReport();
	}

	//Transfer by API is True
	private void transferDataByJson() {
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + dlg.txtMCN.getText());

		String server = dlg.cbServer.getText();
		String serverIP = dlg.cbIP.getText();
		LinkedHashMap<String, String> userAcc = dlg.accMapping.get(server);

		String token = CallMESWebService.getAccessToken(serverIP, userAcc.get("Username"), userAcc.get("Password"));
		if (token.isEmpty()) {
			MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator.", "Error", MessageBox.ERROR);
			return;
		}
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(dlg.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Data transfering...", IProgressMonitor.UNKNOWN);
					bomManager.printReport("APPEND", new String[] { "<html>", "<body>" });
					Set<String> topInfo = new HashSet<String>();
					topInfo.add("User: " + session.getUserName());
					topInfo.add("Server: " + server + " (" + serverIP + ")");
					topInfo.add("Time: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
					bomManager.printReport("DETAILS", topInfo.toArray(new String[0]));
					bomManager.printReport("HEADER", new String[] { "No", "Operation", "Message", "Operation Type", "Transfer Result" });
					try {
						int stepCount = 0;
						int i = 0;
						boolean statusError = false;
						for (MEOPData operationData : operationTransferList) {
							monitor.subTask("Transferring Operation(s): " + ++stepCount + "/" + operationTransferList.size());
							if (operationData.isValidate() || !operationData.isNoNeedTransfer()) {
								TCComponentItemRevision operationRevision = operationData.getOperationRevObject();
								String requestData;
								if (operationData.hasMultipleStations() == true) {
									requestData = Format2XML.formatOperationJson(operationData);
								} else {
									requestData = FormatXML.formatOperationJson(operationData);
								}
								if (requestData.length() != 0) {
									operationData.setJsonData(requestData);

									// save xml data
									String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + UIGetValuesUtility.getSequenceID() + ".html";
									BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
									String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
									File fileXml = new File(xmlUrl);
									writer.write(operationData.getJsonData());
									writer.newLine();
									writer.close();

									BaseModel returModel = new CallMESWebService().callNewService(token, requestData, serverIP);
									if (returModel.getErrorCode().compareTo("00") == 0) {
										if (server.compareToIgnoreCase("PRODUCTION") == 0) {
											if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
											} else {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
											}

											TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
											if (newDataset != null) {
												UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
											}

											String[] printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Transferred success.", operationData.getMEOPType(), "Success" };
											bomManager.printReport("PRINT", printValues);
										}
									} else if (returModel.getErrorCode().compareTo("01") == 0) {
										operationData.setTransferMessage(returModel.getMessage());
										String[] printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Failed transfer to MES", operationData.getMEOPType(), "Error" };
										bomManager.printReport("PRINT", printValues);
										statusError = true;
									} else {
										operationData.setTransferMessage("Please contact with MES Administrator.");
										String[] printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Failed transfer to MES", operationData.getMEOPType(), "Error" };
										bomManager.printReport("PRINT", printValues);
										statusError = true;
									}
								}
							}
						}

						bomManager.printReport("APPEND", new String[] { "</table>" });

						String data = Logger.previousTransaction(logFolder, "OPERATION");
						if (!data.isEmpty())
							bomManager.printReport("APPEND", new String[] { "<br>", data });

						bomManager.printReport("APPEND", new String[] { "</body>", "</html>" });

						File logReport = null;
						if (isSWOperationTransfer)
							logReport = bomManager.generateReport("SW_OPERATION");
						else
							logReport = bomManager.generateReport("OPERATION");

						if (statusError == false && server.equals("PRODUCTION")) {
							TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, bomManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
							if (newDataset != null) {
								UIGetValuesUtility.uploadNamedReference(session, newDataset, logReport, "HTML", true, true);
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		refreshResultReport();
		dlg.setMessage("Transfer success. Please check result report.", IMessageProvider.INFORMATION);
	}

	//Transfer by send to server
	private void transferDataBySOAService() {
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + dlg.txtMCN.getText());

		String server = dlg.cbServer.getText();
		String serverIP = dlg.cbIP.getText();

		if (progressDlg == null)
			progressDlg = new ProgressMonitorDialog(dlg.getShell());
		try {
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Data transfering...", IProgressMonitor.UNKNOWN);
					// header
					int stepCount = 0;
					boolean statusError = false;
					bomManager.printReport("APPEND", new String[] { "<html>", "<body>" });
					Set<String> topInfo = new HashSet<String>();
					topInfo.add("User: " + session.getUserName());
					topInfo.add("Server: " + server + " (" + serverIP + ")");
					topInfo.add("Time: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
					bomManager.printReport("DETAILS", topInfo.toArray(new String[0]));
					bomManager.printReport("HEADER", new String[] { "No", "Operation", "Message", "Operation Type", "Transfer Result" });
					// body
					try {
						LinkedHashMap<String, String> inputDataList = new LinkedHashMap<String, String>();
						LinkedHashMap<String, String> opIDandData = new LinkedHashMap<String, String>();
						int i = 0;
						for (MEOPData operationData : operationTransferList) {
							monitor.subTask("Preparing request(s): " + ++stepCount + "/" + operationTransferList.size());
							if (operationData.isValidate() || !operationData.isNoNeedTransfer()) {
								TCComponentItemRevision operationRevision = operationData.getOperationRevObject();
								String requestData;
								if (operationData.hasMultipleStations() == true) {
									requestData = Format2XML.formatOperation(operationData);
								} else {
									requestData = FormatXML.formatOperation(operationData);
								}
								if (requestData.length() != 0) {
									operationData.setXmlData(requestData);
									inputDataList.put(operationRevision.getUid(), requestData);
									opIDandData.put(operationData.getMEOPID(), requestData);
								}
							}
						}
						if (inputDataList.size() > 0) {
							String sequenceID = UIGetValuesUtility.getSequenceID();
							LinkedHashMap<String, String> outputDataList = CallMESWebService.callSOAService(session, serverIP, inputDataList);
							stepCount = 0;
							for (Map.Entry<String, String> outputItem : outputDataList.entrySet()) {
								for (MEOPData operationData : operationTransferList) {
									monitor.subTask("Transferring Operation(s): " + ++stepCount + "/" + operationTransferList.size());
									TCComponentItemRevision operationRevision = operationData.getOperationRevObject();
									if (outputItem.getKey().compareTo(operationRevision.getUid()) == 0) {
										// save xml data
										String transferredData = opIDandData.get(operationData.getMEOPID());
										String inputString = transferredData.replaceAll("&", "_");
										String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + sequenceID + ".html";
										String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
										File fileXml = new File(xmlUrl);
										DataManagementService dataManagementService = DataManagementService.getService(session);
										BufferedWriter writer = new BufferedWriter(new FileWriter(fileXml));
										writer.write(inputString);
										writer.newLine();
										writer.close();

										switch (outputItem.getValue()) {
										case "200":
											if (server.compareToIgnoreCase("PRODUCTION") == 0) {
												if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
													UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
												} else {
													UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
													UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
												}

												TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
												if (newDataset != null) {
													UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
												}
											}

											TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
											if (newDataset != null) {
												UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
											}

											String[] printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Transferred success.", operationData.getMEOPType(), "Success" };
											bomManager.printReport("PRINT", printValues);
											break;
										default:
											statusError = true;
											printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Failed transfer to MES", operationData.getMEOPType(), "Error" };
											bomManager.printReport("PRINT", printValues);
											break;
										}
										break;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					bomManager.printReport("APPEND", new String[] { "</table>" });

					String data = Logger.previousTransaction(logFolder, "OPERATION");
					if (!data.isEmpty())
						bomManager.printReport("APPEND", new String[] { "<br>", data });

					bomManager.printReport("APPEND", new String[] { "</body>", "</html>" });

					File logReport = null;
					if (isSWOperationTransfer)
						logReport = bomManager.generateReport("SW_OPERATION");
					else
						logReport = bomManager.generateReport("OPERATION");

					if (!statusError && server.equals("PRODUCTION")) {
						TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, bomManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
						if (newDataset != null) {
							UIGetValuesUtility.uploadNamedReference(session, newDataset, logReport, "HTML", true, true);
						}
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

	private boolean validateOperation(TCComponentBOMLine object, MEOPData opeTransferItem) throws Exception {
		
		//Get values of all operations
		opeTransferItem.setOperationBomlineObject(object);
		String operationType = opeTransferItem.getMEOPType();
		if (operationType.isEmpty()) {
			opeTransferItem.setMessage("Operation Type value not filled. Please fill Operation Type.");
			return false;
		}

		if (isSWOperationTransfer && operationType.compareTo("ECU") != 0) {
			boolean isFRSNumberTransfer = false;
			if (operationType.compareTo("Automatic Consumption") == 0) {
				AIFComponentContext[] childrenLines = object.getChildren();
				if (childrenLines.length == 1) {
					TCComponentBOMLine child = (TCComponentBOMLine) childrenLines[0].getComponent();
					if (child.getItemRevision().getType().equals(PropertyDefines.TYPE_FRS_REVISION)) {
						isFRSNumberTransfer = true;
					}
				}
			}

			if (isFRSNumberTransfer == false) {
				opeTransferItem.setMessage("\"" + operationType + "\" required Operation Transfer interface.");
				return false;
			}
		}

		if (!isSWOperationTransfer && operationType.compareTo("ECU") == 0) {
			opeTransferItem.setMessage("\"" + operationType + "\" required Software Operation Transfer interface.");
			return false;
		}

		if (operationType.equals("NA")) {
			TCComponentItemRevision objectRevision = object.getItemRevision();
			UIGetValuesUtility.setProperty(dmService, objectRevision, PropertyDefines.REV_TO_MES, "NA");
			opeTransferItem.setMessage("NA operations not required to send to MES.");
			opeTransferItem.setNoNeedTransfer(true);
			return true;
		}
		if (opeTransferItem.getWorkStationID().isEmpty() || opeTransferItem.getWorkStationID().length() < 13) {
			opeTransferItem.setMessage("Error in generating workstation ID.");
			return false;
		}

		return true;
	}

	private boolean validateOperationChild(TCComponentBOMLine object, MEOPData opeTransferItem, TCComponent[] childLines) throws Exception {
		String validateFlexError = "";
		ArrayList<String> materials = new ArrayList<String>();
		ArrayList<String> tools = new ArrayList<String>();
		boolean objectTypeChildPartNull = false;

		if (childLines != null) {
			for (TCComponent childComponent : childLines) {
				HashMap<String, String> materialDetails = new HashMap<String, String>();
				TCComponentBOMLine bomline = (TCComponentBOMLine) childComponent;
				TCComponentItemRevision revision = bomline.getItemRevision();
				TCComponentItem item = revision.getItem();
				materialDetails.put("MATERIALID", bomline.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
				materialDetails.put("MATERIALREV", bomline.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID));
				String supplierRevision = bomline.getPropertyDisplayableValue(PropertyDefines.BOM_VERSION);
				if (supplierRevision.isEmpty())
					supplierRevision = bomline.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
				materialDetails.put(PropertyDefines.BOM_VERSION, supplierRevision);
				materialDetails.put(PropertyDefines.BOM_VERSION_TMP, bomline.getPropertyDisplayableValue(PropertyDefines.BOM_VERSION_TMP));
				materialDetails.put(PropertyDefines.REV_DID, revision.getPropertyDisplayableValue(PropertyDefines.REV_DID));
				materialDetails.put(PropertyDefines.REV_FILE, revision.getPropertyDisplayableValue(PropertyDefines.REV_FILE));
				materialDetails.put(PropertyDefines.REV_VERISON, revision.getPropertyDisplayableValue(PropertyDefines.REV_VERISON));
				materialDetails.put(PropertyDefines.REV_PROGRAM_DATE, revision.getPropertyDisplayableValue(PropertyDefines.REV_PROGRAM_DATE));
				materialDetails.put(PropertyDefines.REV_FLASH, item.getPropertyDisplayableValue(PropertyDefines.REV_FLASH));
				materialDetails.put(PropertyDefines.REV_RXSWIN, item.getPropertyDisplayableValue(PropertyDefines.REV_RXSWIN));
				materialDetails.put(PropertyDefines.ITEM_SW_PART_TYPE, item.getPropertyDisplayableValue(PropertyDefines.ITEM_SW_PART_TYPE));

				String partType = item.getPropertyDisplayableValue(PropertyDefines.ITEM_SW_PART_TYPE);
				if (partType.isEmpty()) {
					partType = bomline.getPropertyDisplayableValue(PropertyDefines.BOM_PART_TYPE);
					objectTypeChildPartNull = true;
				}
				materialDetails.put(PropertyDefines.REV_TYPE, partType);

				opeTransferItem.setMaterialDetails(materialDetails);

				String object_Type = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);

				if (object_Type.equals(PropertyDefines.TYPE_ACAR_REVISION)) {
					String GMPartNumber = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_GM_NUMBER);
					if (!GMPartNumber.isEmpty()) {
						materials.add(GMPartNumber);
					} else {
						materials.add(childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
					}
				} else if (object_Type.equals(PropertyDefines.TYPE_MECNTOOL_REVISION)) {
					String toolID = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_TOOL_ID);
					if (!toolID.isEmpty()) {
						tools.add(toolID);
					}
				} else {
					if (Arrays.asList(PropertyDefines.TYPES_VALID_MATERIALS).contains(object_Type) == true) {
						validateFlexError = UIGetValuesUtility.validateFlexPart(childComponent, dmService);
						String materialID = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						if (revision.getType().equals(PropertyDefines.TYPE_FRS_REVISION)) {
							materialID = MaterialTransferModel.generateMaterialID(item);
						}
						materials.add(materialID);
					}
				}
			}
		}

		opeTransferItem.setMaterialList(materials);
		opeTransferItem.setToolList(tools);

		if (!validateFlexError.isEmpty()) {
			opeTransferItem.setMessage(validateFlexError);
			return false;
		}

		if (opeTransferItem.getMEOPType().equalsIgnoreCase("Poke Yoke") && opeTransferItem.getReferenceComponent().isEmpty()) {
			opeTransferItem.setMessage("POKO YOKE operation should have reference component.");
			return false;
		}

		if (opeTransferItem.getMEOPType().equals("Poke Yoke") && materials.size() > 0) {
			opeTransferItem.setMessage("Remove materials from POKO YOKE operation");
			return false;
		}

		if (opeTransferItem.getMEOPType().equals("Screwing")) {
			if (tools.isEmpty()) {
				opeTransferItem.setMessage("Screwing required atleast one Tool ID. Please add tool and fill ToolID and program ID");
				return false;
			} else {
				StringBuilder workAreaProgramId = new StringBuilder();
				StringBuilder workAreaId = new StringBuilder();
				StringBuilder mencToolID = new StringBuilder();
				StringBuilder mencToolProgramId = new StringBuilder();
				getMENCToolInfo(object, mencToolProgramId, mencToolID);
				TCComponentBOMLine parentLine = object.parent();
				TCComponentItem parentItem = parentLine.getItem();
				String stationString = "NOT_FOUND_Mfg0MEProcStatn_in_" + parentItem.getObjectString();
				if (parentItem.getTypeComponent().getUid().contains("Mfg0MEProcStatn")) {
					AIFComponentContext[] childrenLines = parentLine.getChildren();
					for (AIFComponentContext childrenLine : childrenLines) {
						TCComponentBOMLine childLine = (TCComponentBOMLine) childrenLine.getComponent();
						TCComponentItem childItem = childLine.getItem();
						stationString = childItem.getProperty("object_string");
						if (childItem.getTypeComponent().getUid().contains("MEWorkarea")) {
							workAreaProgramId.append(childLine.getProperty("VF4_program_id"));
							workAreaId.append(childLine.getProperty(PropertyDefines.BOM_ITEM_ID));
							break;
						}
					}
				}

				if (workAreaProgramId.toString().trim().isEmpty()) {
					opeTransferItem.setMessage("Screwing required Tool ID filled in Plant Model. Please fill \"Tool ID\" as \"" + (!mencToolProgramId.toString().isEmpty() ? mencToolProgramId : "Tool ID") + "\" on station \"" + stationString + "\" in plant model and re-transfer plant model.");
					return false;
				}

				if (mencToolProgramId.toString().isEmpty()) {
					opeTransferItem.setMessage("Screwing required MENCTool added to the operation. Please add MENCTool and fill \"Tool ID\" in the line.");
					return false;
				}
				if (!workAreaProgramId.toString().trim().isEmpty() && !workAreaProgramId.toString().contains(mencToolProgramId)) {
					opeTransferItem.setMessage(String.format("WorkArea %s with Tool ID [%s] doesn't contain MENCTool %s Tool ID [%s])", workAreaId, workAreaProgramId, mencToolID, mencToolProgramId));
					return false;
				}
			}
		}

		if (opeTransferItem.getMEOPType().equals("Screwing") && opeTransferItem.getProgramID().trim().isEmpty()) {
			opeTransferItem.setMessage("Program ID is empty. Please fill program ID on Operation");
			return false;
		}
		
		if (opeTransferItem.getShop().equals("EMotor") && (opeTransferItem.getMEOPType().equals("Screwing") || opeTransferItem.getMEOPType().equals("Buy-Off")) && (opeTransferItem.getWorkplaceSequence().trim().equals("0") || opeTransferItem.getWorkplaceSequence().trim().isEmpty())) {
			opeTransferItem.setMessage("Workspace Sequence is empty. Please fill Workspace Sequence on Operation");
			return false;
		}

		if (opeTransferItem.getMEOPType().equals("Part Verification") && materials.isEmpty()) {
			opeTransferItem.setMessage("\"Part Verification\" required atlease one Part");
			return false;
		}

		if (opeTransferItem.getMEOPType().equals("ECU")) {
			if (materials.isEmpty()) {
				opeTransferItem.setMessage("\"ECU\" required atlease one Part");
				return false;
			}
			if (objectTypeChildPartNull) {
				opeTransferItem.setMessage("\"ECU\" required \"Software Part Type\" filled in Part.");
				return false;
			}
		}

		if (opeTransferItem.getMEOPType().equals("Group Traceability") && materials.isEmpty()) {
			opeTransferItem.setMessage("\"Traceability\" required atlease one Part");
			return false;
		}

		if (opeTransferItem.getMEOPType().equals("Traceability")) {
			if (materials.isEmpty()) {
				opeTransferItem.setMessage("Operation has no material or empty. Please add only one Traceable Part in \"Traceability\" operation.");
				return false;
			} else {

				if (materials.size() >= 2) {
					String shop = opeTransferItem.getShop();
					String result = UIGetValuesUtility.hasOnlyColorParts(childLines, shop);
					if (!result.equals("")) {
						opeTransferItem.setMessage(result);
						return false;
					}
				} else {
					boolean bool_isTreacable = false;
					for (TCComponent bomlinePart : childLines) {
						String isTreacable = bomlinePart.getPropertyDisplayableValue(PropertyDefines.BOM_IS_TRACEABLE);
						if (isTreacable.equals("Yes")) {
							bool_isTreacable = true;
						}
					}
					if (!bool_isTreacable) {
						opeTransferItem.setMessage("Traceability required atlease one Part Traceable Indicator \"Yes\"");
						return false;
					}
				}

			}
		}
		return true;
	}

	private void refreshResultReport() {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("No", "5");
				put("Operation", "15");
				put("Message", "20");
				put("Description", "30");
				put("Operation Type", "15");
				put("", "15");
			}
		};
		validationResultText.append(StringExtension.genTableHeader(header));
		//
		int i = 0;
		for (MEOPData transferItem : operationTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getMEOPID() + "/" + transferItem.getMEOPRevID() + "</td>");
			if (transferItem.getTransferMessage().isEmpty())
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Transfer success.") + "</td>");
			else
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail("Transfer error. Please contact MES team.") + "</td>");
			validationResultText.append("<td>" + transferItem.getTransferMessage() + "</td>");
			validationResultText.append("<td>" + transferItem.getMEOPType() + "</td>");
			validationResultText.append("<td>" + "<button type=\"button\" onclick=\"javaFunction('" + i + "')\">View JSON</button>" + "</td>");
			validationResultText.append("</tr>");
		}
		//
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
		new JavaFunction(dlg.brwReport, "javaFunction");
	}

	private String selecteRule(ExecutionEvent event) {
		String cmd = event.getCommand().toString();
		if (cmd.contains("mesOpTrans")) {
			isSWOperationTransfer = false;
			return PropertyDefines.REVISION_RULE_WORKING;
		} else if (cmd.contains("mesOpSWTrans")) {
			isSWOperationTransfer = true;
			return PropertyDefines.REVISION_RULE_WORKING_PRECISE;
		}
		return PropertyDefines.REVISION_RULE_WORKING;
	}

	private void getMENCToolInfo(TCComponentBOMLine opLine, StringBuilder mencToolProgramId, StringBuilder mencToolId) throws TCException {
		AIFComponentContext[] opChildren = opLine.getChildren();
		for (AIFComponentContext opChild : opChildren) {
			TCComponentBOMLine childLine = (TCComponentBOMLine) opChild.getComponent();
			TCComponentItem childItem = childLine.getItem();
			if (childItem.getTypeComponent().getUid().contains("Mfg0MENCTool")) {
				mencToolProgramId.append(childLine.getProperty("VF4_program_id").trim());
				mencToolId.append(childLine.getProperty(PropertyDefines.BOM_ITEM_ID));
				break;
			}
		}
	}

	private void refreshReport() {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		LinkedHashMap<String, String> header = null;
		if (isSWOperationTransfer) {
			header = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("No", "5");
					put("Operation", "10");
					put("Message", "35");
					put("Operation Type", "15");
					put("Part Number", "15");
					put("Revision (MES)", "10");
					put("SW Part Type", "10");
				}
			};
		} else {
			header = new LinkedHashMap<String, String>() {

				private static final long serialVersionUID = 1L;
				{
					put("No", "5");
					put("Operation", "15");
					put("Message", "65");
					put("Operation Type", "15");
				}
			};
		}
		validationResultText.append(StringExtension.genTableHeader(header));
		//
		int i = 0;
		for (MEOPData transferItem : operationTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getMEOPID() + "/" + transferItem.getMEOPRevID() + "</td>");
			if (transferItem.isNoNeedTransfer()) {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetDefault(transferItem.getMessage()) + "</td>");
			} else {
				if (transferItem.isValidate())
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Ready to transfer") + "</td>");
				else
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(transferItem.getMessage()) + "</td>");
			}
			validationResultText.append("<td>" + transferItem.getMEOPType() + "</td>");
			if (isSWOperationTransfer) {
				validationResultText.append("<td>" + "" + "</td>");
				validationResultText.append("<td>" + "" + "</td>");
				validationResultText.append("<td>" + "" + "</td>");
			}
			validationResultText.append("</tr>");
			// in case op is auto consump, it's restricted in previous validation (check
			// valid operation for normal operation transfer and software operation
			// transfer)
			if (isSWOperationTransfer && transferItem.getMEOPType().equals("Automatic Consumption") == false) {
				for (HashMap<String, String> childParts : transferItem.getMaterialDetails()) {
					validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
					validationResultText.append("<td>" + "" + "</td>");
					validationResultText.append("<td>" + "" + "</td>");
					validationResultText.append("<td>" + "" + "</td>");
					validationResultText.append("<td>" + "" + "</td>");
					validationResultText.append("<td>" + childParts.get("MATERIALID") + "</td>");
					validationResultText.append("<td>" + childParts.get(PropertyDefines.BOM_VERSION) + "</td>");
					String swPartType = childParts.get(PropertyDefines.ITEM_SW_PART_TYPE);
					validationResultText.append("<td>" + (swPartType.isEmpty() ? StringExtension.genBadgetFail("Required filled") : swPartType) + "</td>");
					validationResultText.append("</tr>");
				}
			}
		}
		//
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
	}

	private static class JavaFunction extends BrowserFunction {
		JavaFunction(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			if (arguments.length > 0) {
				MEOPData item = operationTransferList.get(Integer.parseInt(arguments[0].toString()) - 1);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject json = new JSONObject(item.getJsonData());  
							StringViewerDialog viewdialog = new StringViewerDialog(new String[] {json.toString(4)});
							viewdialog.setSize(400, 400);
							viewdialog.setLocationRelativeTo(null);
							viewdialog.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			return null;
		}
	}
}
