package com.vinfast.scooter.mes.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;

import com.teamcenter.integration.dialog.MESTransferDialog;
import com.teamcenter.integration.model.BaseModel;
import com.teamcenter.integration.model.OperationTransferModel;
import com.teamcenter.integration.ulti.BOMManagement;
import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.services.CallMESWebService;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ScooterOperationTransferHandler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private MESTransferDialog dlg;
	private DataManagementService dmService = null;
	private BOMManagement bomManager = null;
	private static LinkedList<OperationTransferModel> operationTransferList = null;
	private boolean transferByAPI = false;
	private ProgressMonitorDialog progressDlg;
	boolean sendServer;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please select one MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			TCComponent[] impactedShop = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject, new String[] {}, PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop == null || impactedShop.length == 0) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			String company = PropertyDefines.VIN_FAST;
			String command = event.getCommand().toString();
			if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
				company = PropertyDefines.VIN_FAST_ELECTRIC;
			} else if (command.contains(PropertyDefines.VIN_ES)) {
				company = PropertyDefines.VIN_ES;
			}
			bomManager = new BOMManagement(session, selectedObject, company);

			String[] preferenceValue = TCExtension.GetPreferenceValues("VF_MES_TRANSFER_API", session);
			if (preferenceValue != null && preferenceValue.length > 0) {
				transferByAPI = preferenceValue[0].compareTo("true") == 0;
			}

			dlg = new MESTransferDialog(new Shell(), session, company, transferByAPI);
			dlg.create();
			dlg.setTitle("EScooter Operation(s) Transfer");
			dlg.txtPlant.setText(bomManager.getPlant());
			dlg.txtMCN.setText(bomManager.getMCN());
			dlg.txtShop.setText(bomManager.getShop());
			dlg.setTotal(10);

			dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					try {
						dlg.btnSave.setEnabled(prepareData());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentRole role = groupMember.getRole();
			if (role.toString().compareToIgnoreCase("DBA") == 0) { // || role.toString().compareToIgnoreCase("MB Engineer") == 0) {
				dlg.lblSendByServer.setVisible(true);
				dlg.ckbSendByServer.setVisible(true);
			}

			dlg.btnSave.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					dlg.btnPrepare.setEnabled(false);
					if (dlg.getServer().isEmpty() || dlg.getServerIP().isEmpty()) {
						MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
						return;
					}
					if (transferByAPI)
						transferDataByJson();
					else
						transferDataBySOAService();

				}
			});

			dlg.btnSave.setEnabled(false);
			dlg.open();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean prepareData() {
		boolean transferValid = true;
		operationTransferList = new LinkedList<OperationTransferModel>();
		
		TCComponent[] solutionItems = bomManager.getSolutionItems(PropertyDefines.TYPE_OPERATION_REVISION);
		if (solutionItems == null || solutionItems.length == 0) {
			MessageBox.post("No operations found in Solution Items.", "Error", MessageBox.ERROR);
			return false;
		}
		dlg.setProgressStatus(10);
		Set<String> searchOperationID = new HashSet<String>();
		for (TCComponent objectRevision : solutionItems) {
			OperationTransferModel newItem = new OperationTransferModel(dlg.cbServer.getText().compareToIgnoreCase("PRODUCTION") == 0);
			newItem.setOperationRevObject((TCComponentItemRevision) objectRevision);
			if (!newItem.isNoNeedTransfer())
				searchOperationID.add(newItem.getItemID());

			operationTransferList.add(newItem);
		}
		dlg.setProgressStatus(20);
		if (searchOperationID != null) {
			if (bomManager.isWindowOpen() == false)
				bomManager.createMBOMBOPWindow();
			dlg.setProgressStatus(40);
			// search operation list in BOP structure
			TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, String.join(";", searchOperationID), bomManager.getBOPTraverseLine());
			HashMap<String, ArrayList<TCComponent>> duplicateMEOP = new HashMap<String, ArrayList<TCComponent>>();
			if (foundObjects != null) {
				for (TCComponent impactedObject : foundObjects) {
					try {
						String ID = impactedObject.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						if (duplicateMEOP.containsKey(ID)) {
							ArrayList<TCComponent> meopList = duplicateMEOP.get(ID);
							meopList.add(impactedObject);
						} else {
							ArrayList<TCComponent> meopList = new ArrayList<TCComponent>();
							meopList.add(impactedObject);
							duplicateMEOP.put(ID, meopList);
						}
					} catch (NotLoadedException e) {
						e.printStackTrace();
					}
				}

				foundObjects = new TCComponent[duplicateMEOP.size()];
				int count = 0;
				for (String key : duplicateMEOP.keySet()) {
					foundObjects[count] = duplicateMEOP.get(key).get(0);
					count++;
				}
			}
			dlg.setProgressStatus(60);
			for (OperationTransferModel opeTransferItem : operationTransferList) {
				if (!opeTransferItem.isValidate() || opeTransferItem.isNoNeedTransfer()) {
					continue;
				}
				if (foundObjects == null) {
					opeTransferItem.setValid(false);
					opeTransferItem.setMessage("Operation not have in BOP structure.");
					continue;
				}

				boolean checkHave = false;
				for (TCComponent lineObject : foundObjects) {
					TCComponentBOMLine object = (TCComponentBOMLine) lineObject;
					try {
						String operationID = object.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						if (operationID.compareTo(opeTransferItem.getItemID()) == 0) {
							checkHave = true;
							boolean transfer = validateOperation(object, opeTransferItem, duplicateMEOP);
							if (!transfer) {
								opeTransferItem.setValid(false);
								transferValid = false;
							}

							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (!checkHave) {
					opeTransferItem.setMessage("Operation not have in BOP structure.");
					opeTransferItem.setValid(false);
					transferValid = false;
				}
			}
			dlg.setProgressStatus(80);
			boolean checkHaveOneTransfer = false;
			HashMap<TCComponent, TCComponent[]> childLines = UIGetValuesUtility.expandBOMOneLevel(session, foundObjects);
			for (OperationTransferModel opeTransferItem : operationTransferList) {
				if (!opeTransferItem.isValidate() || opeTransferItem.isNoNeedTransfer())
					continue;

				try {
					boolean transfer = validateOperationChild(opeTransferItem.getOperationBomlineObject(), opeTransferItem, childLines.get(opeTransferItem.getOperationBomlineObject()));
					if (!transfer) {
						opeTransferItem.setValid(false);
						transferValid = false;
					} else {
						checkHaveOneTransfer = true;
						opeTransferItem.setMessage("Ready to Transfer.");
					}
				} catch (Exception e) {
					e.printStackTrace();
					opeTransferItem.setMessage(e.toString());
					opeTransferItem.setValid(false);
					transferValid = false;
				}
			}
			if (transferValid) {
				transferValid = checkHaveOneTransfer;
			}
		}
		dlg.setProgressStatus(100);
		refreshReport();
		return transferValid;
	}

	private boolean validateOperation(TCComponentBOMLine object, OperationTransferModel opeTransferItem, HashMap<String, ArrayList<TCComponent>> duplicateMEOP) throws Exception {
		opeTransferItem.setOperationBomlineObject(object, duplicateMEOP);
		String operationType = opeTransferItem.getOperationMap().getMEOPType();
		if (operationType.isEmpty()) {
			opeTransferItem.setMessage("PLM: Operation Type value not filled. Please fill Operation Type.");
			return false;
		}

		if (operationType.equals("NA")) {
			TCComponentItemRevision objectRevision = object.getItemRevision();
			UIGetValuesUtility.setProperty(dmService, objectRevision, PropertyDefines.REV_TO_MES, "NA");
			opeTransferItem.setMessage("No need transfer.");
			opeTransferItem.setNoNeedTransfer(true);
			return true;
		}
		if (opeTransferItem.getOperationMap().getWorkStationID().isEmpty()) {
			opeTransferItem.setMessage("PLM: Error in generating workstation ID.");
			return false;
		}

		return true;
	}

	private boolean validateOperationChild(TCComponentBOMLine object, OperationTransferModel opeTransferItem, TCComponent[] childLines) throws Exception {
		ArrayList<String> materials = new ArrayList<String>();
		ArrayList<String> tools = new ArrayList<String>();

		if (childLines != null) {
			for (TCComponent childComponent : childLines) {
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
					materials.add(childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
				}
			}
		}

		opeTransferItem.getOperationMap().setChildItems(childLines);
		opeTransferItem.getOperationMap().setMaterialList(materials);
		opeTransferItem.getOperationMap().setToolList(tools);

		String message = opeTransferItem.getOperationMap().isValidOperation();
		if (!message.isEmpty()) {
			opeTransferItem.setMessage(message);
			return false;
		}

		return true;
	}

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
					monitor.beginTask("Data preparing...", IProgressMonitor.UNKNOWN);
					try {
						int stepCount = 0;
						for (OperationTransferModel operationItem : operationTransferList) {
							monitor.subTask("Transfer: " + ++stepCount + "/" + operationTransferList.size() + " OPE");
							if (operationItem.isValidate()) {
								TCComponentItemRevision operationRevision = operationItem.getOperationRevObject();
								MEOPDataAbstract operationData = operationItem.getOperationMap();
								String requestData = FormatXMLScooter2.jsonFormatOperation(operationData);
								if (requestData.length() != 0) {
									operationItem.setJsonData(requestData);
									BaseModel returModel = new CallMESWebService().callNewService(token, requestData, serverIP);
									if (returModel.getErrorCode().compareTo("00") == 0) {
										if (server.compareToIgnoreCase("PRODUCTION") == 0) {
											if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
											} else {
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
												UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
											}

											// save xml data
											String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + UIGetValuesUtility.getSequenceID() + ".html";
											BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
											String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
											File fileXml = new File(xmlUrl);
											writer.write(requestData.replaceAll("&", "_"));
											writer.newLine();
											writer.close();
											TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
											if (newDataset != null) {
												UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
											}
										}
									} else if (returModel.getErrorCode().compareTo("01") == 0) {
										operationItem.setTransferMessage(returModel.getMessage());
									} else {
										operationItem.setTransferMessage("Please contact with MES Administrator.");
									}
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		refreshResultReport();
		dlg.setMessage("Transfer success. Please check result report.", IMessageProvider.INFORMATION);
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
		int i = 0;
		for (OperationTransferModel transferItem : operationTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getItemID() + "/" + transferItem.getRevID() + "</td>");
			if (transferItem.getTransferMessage().isEmpty())
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Transfer success.") + "</td>");
			else
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail("Transfer error. Please contact MES team.") + "</td>");
			validationResultText.append("<td>" + transferItem.getTransferMessage() + "</td>");
			validationResultText.append("<td>" + transferItem.getOpeType() + "</td>");
			validationResultText.append("<td>" + "<button type=\"button\" onclick=\"javaFunction('" + i + "')\">View JSON</button>" + "</td>");
			validationResultText.append("</tr>");
		}
		//
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
		new JavaFunction(dlg.brwReport, "javaFunction");
	}

	private void transferDataBySOAService() {
		
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + dlg.txtMCN.getText());
		String server = dlg.cbServer.getText();
		String serverIP = dlg.cbIP.getText();
		sendServer = dlg.ckbSendByServer.getSelection();
		// header
		bomManager.printReport("APPEND", new String[] { "<html>", "<body>" });
		Set<String> topInfo = new HashSet<String>();
		topInfo.add("User: " + session.getUserName());
		topInfo.add("Server: " + server + " (" + serverIP + ")");
		topInfo.add("Time: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
		bomManager.printReport("DETAILS", topInfo.toArray(new String[0]));
		bomManager.printReport("HEADER", new String[] { "No", "Operation", "Message", "Operation Type", "Transfer Result" });
		// body
		if (progressDlg == null)
			progressDlg = new ProgressMonitorDialog(dlg.getShell());
		try {
		progressDlg.run(true, false, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Data preparing...", IProgressMonitor.UNKNOWN);
				try {

					LinkedHashMap<String, String> inputDataList = new LinkedHashMap<String, String>();
					int i = 0;
					int status = 1;
					for (OperationTransferModel operationItem : operationTransferList) {
						monitor.subTask("Transferring: "+operationItem.getItemID()+" (" + (status++) + "/" + operationTransferList.size()+")");
						if (operationItem.isValidate()) {
							TCComponentItemRevision operationRevision = operationItem.getOperationRevObject();
							MEOPDataAbstract operationData = operationItem.getOperationMap();
							String requestData = FormatXMLScooter2.xmlFormatOperation(operationData);
							if (requestData.length() != 0) {
								inputDataList.put(operationRevision.getUid(), requestData);
								operationItem.setXmlData(requestData);
							}
							LinkedHashMap<String, String> outputDataList = null;
							if(sendServer) {
								outputDataList = CallMESWebService.callSOAService(session, serverIP, inputDataList);
							}else {
								outputDataList = CallMESWebService.callService(serverIP, inputDataList);
							}
							for (Map.Entry<String, String> outputItem : outputDataList.entrySet()) {

								File fileXml = null;
								String xmlName = "";
								switch (outputItem.getValue()) {
								case "200":
									operationItem.setTransferMessage("Transferred to MES");
									if (server.equals("PRODUCTION") == true) {
										if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
											UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
										} else {
											UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
											UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
										}

										// save xml data
										String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + UIGetValuesUtility.getSequenceID() + ".html";
										BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
										xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
										fileXml = new File(xmlUrl);
										writer.write(operationItem.getXmlData().replaceAll("&", "_"));
										writer.newLine();
										writer.close();
										TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
										if (newDataset != null) {
											UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
										}
									}

									String[] printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Transferred to MES", operationData.getMEOPType(), serverIP, "Success" };
									bomManager.printReport("PRINT", printValues);
									break;
								default:
									operationItem.setTransferMessage("Failed transfer to MES");
									printValues = new String[] { Integer.toString(++i), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Failed transfer to MES", operationData.getMEOPType(), serverIP, "Error" };
									bomManager.printReport("PRINT", printValues);
									break;
								}
								break;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		// footer
		bomManager.printReport("APPEND", new String[] { "</table>" });

		String data = Logger.previousTransaction(logFolder, "OPERATION");
		if (!data.isEmpty())
			bomManager.printReport("APPEND", new String[] { "<br>", data });

		bomManager.printReport("APPEND", new String[] { "</body>", "</html>" });

		refreshReport();
		File logReport = null;
		logReport = bomManager.popupReport2("ESCOOTER OPERATION");

		if (server.equals("PRODUCTION")) {
			TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, bomManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
			if (newDataset != null) {
				UIGetValuesUtility.uploadNamedReference(session, newDataset, logReport, "HTML", true, true);
			}
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {

		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		if (targetComponents[0] instanceof TCComponentItemRevision) {
			selectedObject = (TCComponentItemRevision) targetComponents[0];
		}
		if (selectedObject == null)
			return true;
		return false;
	}

	private void refreshReport() {

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
				put("Message", "65");
				put("Operation Type", "15");
			}
		};

		validationResultText.append(StringExtension.genTableHeader(header));
		int i = 0;
		for (OperationTransferModel transferItem : operationTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getItemID() + "/" + transferItem.getRevID() + "</td>");
			if (transferItem.isNoNeedTransfer()) {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetDefault(transferItem.getMessage()) + "</td>");
			} else {
				if (transferItem.isValidate() && transferItem.getTransferMessage().isEmpty()) {
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess(transferItem.getMessage()) + "</td>");
				}
				else if(transferItem.isValidate() && transferItem.getTransferMessage().isEmpty() == false) {
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess(transferItem.getTransferMessage()) + "</td>");
				}
				else {
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(transferItem.getMessage()) + "</td>");
				}
			}
			if (transferItem.getOperationMap() != null)
				validationResultText.append("<td>" + transferItem.getOperationMap().getMEOPType() + "</td>");
			else
				validationResultText.append("<td>" + "" + "</td>");
			validationResultText.append("</tr>");
		}

		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
		dlg.brwReport.redraw();
	}

	private static class JavaFunction extends BrowserFunction {
		JavaFunction(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			if (arguments.length > 0) {
				OperationTransferModel item = operationTransferList.get(Integer.parseInt(arguments[0].toString()) - 1);
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
