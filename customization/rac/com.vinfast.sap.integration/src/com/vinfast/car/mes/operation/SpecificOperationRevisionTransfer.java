/*
 * Authour Rafi
 * Company Siemens
 * 
 * Send Operations to MES and attach log to MCN
 */
package com.vinfast.car.mes.operation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.ServiceException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.rac.core._2006_03.Session.GetSessionGroupMemberResponse;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soaictstubs.ICCTAccessControlService;
import com.teamcenter.soaictstubs.booleanSeq_tHolder;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.bom.MEOPData;
import com.vinfast.sap.dialogs.SpecificOperationRevisionTransferDialog;
import com.vinfast.sap.services.CallMESWebService;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SpecificOperationRevisionTransfer extends AbstractHandler {

	private ICCTAccessControlService accessService = null;
	UIGetValuesUtility utilities = null;
	LinkedList<HashMap<TCComponent, MEOPData>> data2Send = new LinkedList<HashMap<TCComponent, MEOPData>>();
	TCSession clientSession = null;
	DataManagementService dataManagementService = null;

	HashMap<String, String> oldXmlData = new HashMap<String, String>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub

		ISelection selection = HandlerUtil.getCurrentSelection(event);

		InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

		TCComponentItemRevision changeObject = (TCComponentItemRevision) selectedObjects[0];

		clientSession = changeObject.getSession();

		accessService = new ICCTAccessControlService(clientSession.getSoaConnection());

		utilities = new UIGetValuesUtility();

		try {

			booleanSeq_tHolder accessHolder = new booleanSeq_tHolder();

			accessService.checkPrivileges(changeObject.getUid(), new String[] { "WRITE" }, accessHolder);

//			if(!userIsDBA(clientSession)) {
//				MessageBox.post("You are not DBA group member", "Error", MessageBox.WARNING);
//				return null;
//			}else {

			BOMManager BOMManager = new BOMManager(UIGetValuesUtility.getCompanyCode(event)).loadChangeAttachments(clientSession, changeObject);
			dataManagementService = BOMManager.getDataManagementService();

//				if(BOMManager.getSolutionItems().length != 1) {
//					MessageBox.post("You can transfer only one operation! Please remove all others ", "Error", MessageBox.ERROR);
//					return null;
//				}

			SpecificOperationRevisionTransferDialog transferDialog = new SpecificOperationRevisionTransferDialog(new Shell(), clientSession);

			transferDialog.create();

			transferDialog.setTitle("Specific Operation Revision  Transfer");

			transferDialog.setPlant(BOMManager.getPlant());

			transferDialog.setMCN(BOMManager.getMCN());

			transferDialog.setTotal(BOMManager.getTotal());

			Button transferBtn = transferDialog.getTransferButton();
			Button prepareBtn = transferDialog.getBtnPrepare();
			transferBtn.setEnabled(false);
			transferBtn.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {

					transferBtn.setEnabled(false);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferOldXmlToMES(transferDialog, oldXmlData, BOMManager); // old xml
							transferDataToMES(BOMManager, transferDialog, data2Send); // still open bom
							transferDialog.getShell().dispose();
						}
					});
				}
			});

			prepareBtn.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event e) {

					if (transferDialog.getServer().equals("")) {

						MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
						return;
					}

					if (transferDialog.getServerIP().equals("")) {

						MessageBox.post("Please select server IP to transfer.", "Error", MessageBox.ERROR);
						return;
					}

					prepareBtn.setEnabled(false);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferBtn.setEnabled(prepareData(BOMManager, transferDialog));
						}
					});
				}
			});

			transferDialog.open();

//			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;

	}

	private boolean prepareData(BOMManager BOMManager, SpecificOperationRevisionTransferDialog dialog) {

		BOMManager.initReport(new String[] { "S.No", "Operation", "Message", "Type", "Server", "Result" });

		String[] printValues = null;
		data2Send = new LinkedList<HashMap<TCComponent, MEOPData>>();

		TCComponent[] operations = BOMManager.getSolutionItems();
		try {
			oldXmlData = new HashMap<String, String>();
			for (TCComponent operation : operations) {
				TCComponentItemRevision rev = (TCComponentItemRevision) operation;
				String itemId = rev.getProperty(PropertyDefines.ITEM_ID);
				String itemRevId = rev.getProperty(PropertyDefines.ITEM_REV_ID);
				boolean hasXml = loadOldXml(rev, oldXmlData, BOMManager);
				if (!hasXml) {
					try {
						HashMap<TCComponent, MEOPData> data = loadOperationRevisionData(rev, BOMManager);
						if (data.size() > 0) {
							exportXmlOnly(BOMManager, data);
							printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), String.format("%s/%s", itemId, itemRevId), "Will transfer operation by opening BOM. Data may be not correct", "...", "...", "Warning" };
							BOMManager.printReport("PRINT", printValues);
							data2Send.add(data);
						} else {
							printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), String.format("%s/%s", itemId, itemRevId), "Prepare data fail", "As Before", "...", "Ready" };
							BOMManager.printReport("PRINT", printValues);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), String.format("%s/%s", itemId, itemRevId), "Will transfer operation by using old xml", "As Before", "...", "Ready" };
					BOMManager.printReport("PRINT", printValues);
				}
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}

		String data = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()), "OPERATION");
		BOMManager.finishReport(data);
		BOMManager.popupReport("OPERATION");
		return true;

	}

	private boolean transferOldXmlToMES(SpecificOperationRevisionTransferDialog dialog, HashMap<String, String> oldXmlData, BOMManager BOMManager) {
		String MES_SERVER_IP = dialog.getServerIP();
		BOMManager.initReport(new String[] { "S.No", "Operation", "Message", "Type", "Server", "Result" });
		String[] printValues = null;

		for (Map.Entry<String, String> xml : oldXmlData.entrySet()) {
			int returnCode = new CallMESWebService().callService(xml.getValue(), MES_SERVER_IP);
			switch (returnCode) {

			case 200:
				printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), xml.getKey(), "MES: Transferred to MES", "As Before", MES_SERVER_IP, "Success" };
				BOMManager.printReport("PRINT", printValues);
				break;

			case 401:
				printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), xml.getKey(), "MES: Fail to transferred to MES", "As Before", MES_SERVER_IP, "FAIL" };
				BOMManager.printReport("PRINT", printValues);
				break;

			default:

				MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator", "Error", MessageBox.ERROR);
				return false;
			}
		}

		return true;
	}

	private boolean transferDataToMES(BOMManager BOMManager, SpecificOperationRevisionTransferDialog dialog, LinkedList<HashMap<TCComponent, MEOPData>> operationDetails) {

		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN());
		String MES_SERVER_IP = dialog.getServerIP();

		try {
			for (HashMap<TCComponent, MEOPData> operationDetail : operationDetails) {
				for (TCComponent valueEntry : operationDetail.keySet()) {

					TCComponentItemRevision operationRevision = (TCComponentItemRevision) valueEntry;
					MEOPData operationData = operationDetail.get(valueEntry);

					String requestData = FormatXML.formatOperation(operationData);

					if (requestData.length() != 0) {

						String inputString = requestData.replaceAll("&", "_");
						String sequenceID = UIGetValuesUtility.getSequenceID();
						BufferedWriter writer = new BufferedWriter(new FileWriter(logFolder + "\\" + operationData.getMEOPID() + "_" + sequenceID + ".xml"));
						writer.write(inputString);
						writer.newLine();
						writer.close();

						// DRYRUN
						int returnCode = new CallMESWebService().callService(inputString, MES_SERVER_IP);
						switch (returnCode) {

						case 200:

							booleanSeq_tHolder accessHolder = new booleanSeq_tHolder();

							accessService.checkPrivileges(operationRevision.getUid(), new String[] { "WRITE" }, accessHolder);

							String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Transferred to MES", operationData.getMEOPType(), MES_SERVER_IP, "Success" };
							BOMManager.printReport("PRINT", printValues);
							break;

						case 401:
							printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationData.getMEOPID() + "/" + operationData.getMEOPRevID(), "MES: Failed transfer to MES", operationData.getMEOPType(), MES_SERVER_IP, "Error" };
							BOMManager.printReport("PRINT", printValues);
							break;

						default:

							MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator", "Error", MessageBox.ERROR);
							return false;
						}
					}

				}
			}

			String data = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()), "OPERATION");
			BOMManager.finishReport(data);
			BOMManager.popupReport("OPERATION");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	HashMap<TCComponent, MEOPData> loadOperationRevisionData(TCComponentItemRevision operationRevision, BOMManager bOManager) throws Exception {

		HashMap<TCComponent, MEOPData> operationData = new HashMap<TCComponent, MEOPData>();
		OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(bOManager.getSession(), operationRevision);

		if (createdBOMViews != null) {
			for (OpenContextInfo views : createdBOMViews) {
				if (views.context.getType().equals("Mfg0BvrOperation")) {
					TCComponentBOMLine operationLine = (TCComponentBOMLine) views.context;
					TcResponseHelper responseHelper = TcBOMService.expand(bOManager.getSession(), operationLine);
					TCComponent[] returnedObjects = responseHelper.getReturnedObjects();
					HashMap<TCComponent, TCComponent[]> expandLines = new HashMap<TCComponent, TCComponent[]>();
					expandLines.put(operationLine, returnedObjects);
					operationData = new SpecificOperationDataHandler().objectDetails(bOManager, operationLine, expandLines);
				}
			}
		}
		return operationData;
	}

	void exportXmlOnly(BOMManager BOMManager, HashMap<TCComponent, MEOPData> data2Send) {

		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN());
		for (TCComponent valueEntry : data2Send.keySet()) {
			MEOPData operationData = data2Send.get(valueEntry);

			String requestData = FormatXML.formatOperation(operationData);

			if (requestData.length() != 0) {

				String inputString = requestData.toString().replaceAll("&", "_");
				String sequenceID = UIGetValuesUtility.getSequenceID();
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(logFolder + "\\" + operationData.getMEOPID() + "_" + sequenceID + ".xml"));
					writer.write(inputString);
					writer.newLine();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public TCComponentUser getUser(TCSession session) throws TCException {
		TCComponentUser user = null;
		TCComponentGroupMember groupMember;
		try {
			SessionService SService = SessionService.getService(session);
			GetSessionGroupMemberResponse response;
			try {
				response = SService.getSessionGroupMember();
				ServiceData sd = response.serviceData;
				if (sd.sizeOfPartialErrors() > 0) {
					ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
					ErrorValue[] errorValue = errorStack.getErrorValues();
					for (int inx = 1; inx < errorValue.length; inx++) {
						System.out.println(errorValue[inx].getMessage());
					}

				} else {
					groupMember = response.groupMember;
					user = groupMember.getUser();
				}
			} catch (com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	public boolean userIsDBA(TCSession session) {
		try {
			TCComponentUser user = getUser(session);
			TCComponentGroup currentLogInGroup = user.getLoginGroup();
			if (currentLogInGroup.isDBA()) {
				return true;
			} else {
				return false;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean loadOldXml(TCComponentItemRevision item, HashMap<String, String> mapXml, BOMManager BOMManager) {
		String oldXmlData = null;
		try {
			String itemId = item.getProperty(PropertyDefines.ITEM_ID);
			String itemRevId = item.getProperty(PropertyDefines.ITEM_REV_ID);
			String xmlFileName = String.format("%s_%s_%s", "OP", itemId, itemRevId);
			TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(clientSession, new String[] { "Name", "Dataset Type" }, new String[] { xmlFileName, "HTML" }, "Dataset...");

			if (oldDatasets != null) {

				File oldBOMFile = null;

				HashMap<Date, TCComponent> datasetMap = new HashMap<Date, TCComponent>();

				dataManagementService.getProperties(oldDatasets, new String[] { "creation_date" });

				for (TCComponent dataset : oldDatasets) {
					Date date_creation = dataset.getDateProperty("creation_date");
					datasetMap.put(date_creation, dataset);
				}

				ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet());

				Collections.sort(sortedKeys, Collections.reverseOrder());

				TCComponentDataset olddataset = (TCComponentDataset) datasetMap.get(sortedKeys.get(0));

				String logFolder = UIGetValuesUtility.createLogFolder("MCN" + BOMManager.getMCN()) + "\\";

				oldBOMFile = downloadDataset(clientSession, logFolder, olddataset, "HTML", xmlFileName + ".xml");

				if (oldBOMFile != null) {
					oldXmlData = usingBufferedReader(logFolder + xmlFileName + ".xml");
					mapXml.put(String.format("%s/%s", item, itemId), oldXmlData);
					return true;
				}
			}
		} catch (TCException e1) {
			return false;
		}
		return false;
	}

	public File downloadDataset(TCSession session, String outputPath, TCComponentDataset dataset, String dataSetType, String fileName) {

		File downloadFile = null;
		try {

			FileManagementService fmService = FileManagementService.getService(session);
			FileManagementUtility fileUtility = new FileManagementUtility(session.getSoaConnection());

			if (dataset.getType().equals(dataSetType)) {

				TCComponentTcFile[] files = dataset.getTcFiles();

				if (files.length != 0) {

					FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);

					Map<TCComponentTcFile, String> map = ticketResp.tickets;

					for (TCComponentTcFile tcFile : map.keySet()) {
//						String fileName = tcFile.getProperty("original_file_name");		    
						String ticket = map.get(tcFile);
						downloadFile = fileUtility.getTransientFile(ticket, outputPath + fileName);
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return downloadFile;
	}

	private static String usingBufferedReader(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}
}
