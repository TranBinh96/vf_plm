package com.vinfast.scooter.mes.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.model.BaseModel;
import com.teamcenter.integration.ulti.BOMManagement;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.dialogs.MESTransferDialog;
import com.vinfast.sap.services.CallMESWebService;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;

public class OperationToMES extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private MESTransferDialog dlg;
	private DataManagementService dataManagementService = null;
	private BOMManagement bomManager = null;
	private boolean transferByAPI = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dataManagementService = DataManagementService.getService(session);

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			
			VFResponse response = MCNValidator.validate(targetComponents);
			
			if(response.hasError() == false) {
				
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

				dlg = new MESTransferDialog(new Shell(), session, company);
				dlg.create();
				dlg.setTitle("Operation(s) Transfer");
				dlg.txtPlant.setText(bomManager.getPlant());
				dlg.txtMCN.setText(bomManager.getMCN());
				dlg.txtShop.setText(bomManager.getShop());
				dlg.setTotal(10);
				dlg.btnSave.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						if (dlg.getServer().isEmpty() || dlg.getServerIP().isEmpty()) {
							MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
							return;
						}

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								startProcessing();
								dlg.getShell().dispose();
							}
						});
					}
				});

				dlg.open();
			}else {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void startProcessing() {
		boolean statusError = true;
		boolean isDataTransferred = false;

		new Logger();
		String server = dlg.cbServer.getText();
		bomManager.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "Model :" + bomManager.getModel() + "_" + bomManager.getYear(), "User :" + session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		bomManager.printReport("DETAILS", printValues);
		printValues = new String[] { "S.No", "Operation", "Message", "Type", "Server", "Result" };
		bomManager.printReport("HEADER", printValues);

		TCComponent[] solutionItems = bomManager.getSolutionItems(PropertyDefines.TYPE_OPERATION_REVISION);
		if (solutionItems == null || solutionItems.length == 0) {
			MessageBox.post("No operations found in Solution Items.", "Error", MessageBox.ERROR);
			return;
		}

		String searchIDs = sendItemIDToMES(solutionItems, server);
		if (searchIDs.length() != 0) {
			if (!bomManager.isWindowOpen()) {
				if (bomManager.createMBOMBOPWindow()) {
					HashMap<TCComponent, MEOPDataAbstract> operationsData = loadOperationsData(searchIDs);
					if (operationsData != null) {
						isDataTransferred = true;
						if (transferByAPI)
							statusError = transferDataByJson(operationsData, server);
						else
							statusError = transferDataToMES(operationsData, server);
					}
				}
			}
		}

		if (bomManager.isWindowOpen()) {
			bomManager.closeMBOMBOPWindows();
		}

		bomManager.printReport("APPEND", new String[] { "</table>" });
		String data = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + bomManager.getMCN()), "OPERATION");
		if (!data.equals("")) {
			bomManager.printReport("APPEND", new String[] { "<br>" });
			bomManager.printReport("APPEND", new String[] { data });
		}

		bomManager.printReport("APPEND", new String[] { "</body>", "</html>" });
		File logReport = bomManager.popupReport("OPERATION");
		if (isDataTransferred == true && statusError == false && server.equals("PRODUCTION")) {
			TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, bomManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");
			if (newDataset != null) {
				UIGetValuesUtility.uploadNamedReference(session, newDataset, logReport, "HTML", true, true);
			}
		}
	}

	private boolean transferDataToMES(HashMap<TCComponent, MEOPDataAbstract> operationDetails, String server) {

		String MES_SERVER_IP = dlg.getServerIP();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + bomManager.getMCN());
		boolean isError = false;
		for (TCComponent valueEntry : operationDetails.keySet()) {
			TCComponentItemRevision operationRevision = (TCComponentItemRevision) valueEntry;
			MEOPDataAbstract operationData = operationDetails.get(valueEntry);

			String requestData = FormatXMLScooter2.xmlFormatOperation(operationData);
			if (requestData.length() != 0) {
				String inputString = requestData.toString().replaceAll("&", "_");
				try {
					String sequenceID = UIGetValuesUtility.getSequenceID();
					String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + sequenceID + ".html";
					BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
					String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
					File fileXml = new File(xmlUrl);
					writer.write(inputString);
					writer.newLine();
					writer.close();

					int returnCode = new CallMESWebService().callService(inputString, MES_SERVER_IP);
					switch (returnCode) {
					case 200:
						if (server.equals("PRODUCTION") == true) {
							if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
							} else {
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
							}
							// save xml data
							TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
							if (newDataset != null) {
								UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
							}
						}
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Transferred to MES", operationData.getMEOPType(), MES_SERVER_IP, "Success" };
						bomManager.printReport("PRINT", printValues);
						break;
					case 401:
						isError = true;
						printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Failed transfer to MES", operationData.getMEOPType(), MES_SERVER_IP, "Error" };
						bomManager.printReport("PRINT", printValues);
						break;
					default:
						MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator", "Error", MessageBox.ERROR);
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return isError;
	}

	private boolean transferDataByJson(HashMap<TCComponent, MEOPDataAbstract> operationDetails, String server) {

		String MES_SERVER_IP = dlg.getServerIP();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + bomManager.getMCN());
		boolean isError = false;
		// get token
		LinkedHashMap<String, String> userAcc = dlg.accMapping.get(server);
		String token = CallMESWebService.getAccessToken(MES_SERVER_IP, userAcc.get("Username"), userAcc.get("Password"));

		for (TCComponent valueEntry : operationDetails.keySet()) {
			TCComponentItemRevision operationRevision = (TCComponentItemRevision) valueEntry;
			MEOPDataAbstract operationData = operationDetails.get(valueEntry);

			String requestData = FormatXMLScooter2.xmlFormatOperation(operationData);
			if (requestData.length() != 0) {
				String inputString = requestData.toString().replaceAll("&", "_");
				try {
					String sequenceID = UIGetValuesUtility.getSequenceID();
					String xmlUrl = logFolder + "\\" + operationData.getMEOPID() + "_" + sequenceID + ".html";
					BufferedWriter writer = new BufferedWriter(new FileWriter(xmlUrl));
					String xmlName = String.format("%s_%s_%s", "OP", operationData.getMEOPID(), operationData.getMEOPRevID());
					File fileXml = new File(xmlUrl);
					writer.write(inputString);
					writer.newLine();
					writer.close();

					BaseModel returnModel = new CallMESWebService().callNewService(token, inputString, MES_SERVER_IP);
					if (returnModel.getErrorCode().compareTo("00") == 0) {
						if (server.equals("PRODUCTION") == true) {
							if (operationData.getWorkStationID().length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_TO_MES, operationData.getWorkStationID());
							} else {
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
								UIGetValuesUtility.setProperty(dataManagementService, operationRevision, PropertyDefines.REV_USERNOTE, operationData.getWorkStationID());
							}
							// save xml data
							TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, operationRevision, "IMAN_specification", xmlName, "Transfer Record", "HTML", "IExplore");
							if (newDataset != null) {
								UIGetValuesUtility.uploadNamedReference(session, newDataset, fileXml, "HTML", true, true);
							}
						}
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Transferred to MES", operationData.getMEOPType(), MES_SERVER_IP, "Success" };
						bomManager.printReport("PRINT", printValues);
					} else if (returnModel.getErrorCode().compareTo("01") == 0) {
						isError = true;
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), returnModel.getMessage(), operationData.getMEOPType(), MES_SERVER_IP, "Error" };
						bomManager.printReport("PRINT", printValues);
					} else {
						isError = true;
						String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "Please contact with MES Administrator.", operationData.getMEOPType(), "Error" };
						bomManager.printReport("PRINT", printValues);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return isError;
	}

	private String sendItemIDToMES(TCComponent[] objects, String server) {
		Set<String> searchIds = new HashSet<String>();
		try {
			if (objects.length != 0) {
				for (TCComponent objectRevision : objects) {
					String itemID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String revID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					if (server.equals("PRODUCTION")) {
						if (objectRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_MES).isEmpty()) {
							searchIds.add(itemID);
						} else {
							dlg.updateprogressBar();
							bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), itemID + "/" + revID, "Operation already transferred to MES.", "-", "-", "Info" });
						}
					} else {
						searchIds.add(itemID);
						dlg.updateprogressBar();
					}
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return String.join(";", searchIds);
	}

	private HashMap<TCComponent, MEOPDataAbstract> loadOperationsData(String objectIDs) {
		HashMap<TCComponent, MEOPDataAbstract> traversalDataMap = null;
		TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, objectIDs, bomManager.getBOPTraverseLine());
		dlg.updateprogressBar();
		if (foundObjects != null) {
			traversalDataMap = new ScooterObjectsAndDetailsV2().objectDetails(bomManager, foundObjects, dataManagementService, session);
			dlg.updateprogressBar();
		}
		return traversalDataMap;
	}

}
