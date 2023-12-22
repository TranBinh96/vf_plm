package com.vinfast.sap.material;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.dialog.SAPTransfer_Dialog;
import com.teamcenter.integration.model.MaterialTransferModel;
import com.teamcenter.integration.model.MaterialTransferModel.PREPARE_STATUS;
import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class MaterialTransferHandler extends AbstractHandler {
	private TCSession session;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private OrganizationInformationAbstract serverInfo = null;
	private TCComponent selectedObject = null;
	private DataManagementService dmService;
	private SAPTransfer_Dialog dlg;
	private LinkedList<MaterialTransferModel> materialTransferList;
	private String userRole = "";
	private String[] materialTypes = { 
			PropertyDefines.TYPE_ACAR_REVISION, 
			PropertyDefines.TYPE_CAR_PART_REVISION, 
			PropertyDefines.TYPE_DESIGN_REVISION, 
			PropertyDefines.TYPE_MFG_PART_REVISION, 
			PropertyDefines.TYPE_ES_PART_REVISION, 
			PropertyDefines.TYPE_ME_SCOOTER_REVISION, 
			PropertyDefines.TYPE_BP_DESIGN_REVISION, 
			PropertyDefines.TYPE_SVC_CHAPTER_REVISION, 
			PropertyDefines.TYPE_SVC_KIT_REVISION, 
			PropertyDefines.TYPE_DESIGN_REVISION_ALT, 
			PropertyDefines.TYPE_CELL_DESIGN_REVISION,
			PropertyDefines.TYPE_COMPONENT_DESIGN_REVISION, 
			PropertyDefines.TYPE_VES_ME_PART_REVISION,
			PropertyDefines.TYPE_FRS_REVISION
	};

	public MaterialTransferHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select 1 MCN Revision.", "Warning", MessageBox.ERROR);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			dmService = DataManagementService.getService(session);
			String plant = selectedObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			if (plant.isEmpty()) {
				MessageBox.post("SAP Plant is empty. Please fill SAP Plant and try again.", "Warning", MessageBox.ERROR);
				return null;
			}

			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject, materialTypes, PropertyDefines.REL_SOL_ITEMS);
			if (solutionItems == null || solutionItems.length == 0) {
				MessageBox.post("Solution Items folder is empty. Please copy material(s) and try again.", "Warning", MessageBox.ERROR);
				return false;
			}

			userRole = session.getRole().getRoleName();

			String MCN_SAPID = selectedObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String mcn = MCN_SAPID.substring(MCN_SAPID.length() - 8);

			dlg = new SAPTransfer_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Prepare Data before Transfer.", IMessageProvider.INFORMATION);
			dlg.setTitle("Material Transfer");
			dlg.txtMCN.setText(mcn);
			dlg.txtPlant.setText(plant);
			dlg.cbServer.setText("PRODUCTION");
			dlg.btnAccept.setEnabled(false);
			dlg.lblFrsNumber.setVisible(false);
			dlg.txtFRSNumber.setVisible(false);
			dlg.lblReleaseDCR.setVisible(false);
			dlg.txtReleaseDCR.setVisible(false);

			dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.setMessage("Prepare Data before Transfer.", IMessageProvider.INFORMATION);
					dlg.lblFrsNumber.setVisible(false);
					dlg.txtFRSNumber.setVisible(false);
					dlg.lblReleaseDCR.setVisible(false);
					dlg.txtReleaseDCR.setVisible(false);
					showProgressDialog(true);
					dlg.btnAccept.setEnabled(prepareData());
					showProgressDialog(false);
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(PropertyDefines.VIN_FAST, dlg.cbServer.getText(), session);
					showProgressDialog(true);
					transferData();
					showProgressDialog(false);
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean prepareData() {
		materialTransferList = new LinkedList<MaterialTransferModel>();
		TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject, materialTypes, PropertyDefines.REL_SOL_ITEMS);

		for (TCComponent object : solutionItems) {
			TCComponentItemRevision objectRevision = null;
			if (object instanceof TCComponentItemRevision)
				objectRevision = (TCComponentItemRevision) object;
			else if (object instanceof TCComponentItem) {
				try {
					ArrayList<String> valid_status_list = MaterialTransferValidate.getRevisionRuleStatusEntries(session, PropertyDefines.REVISION_RULE_RELEASE);
					TCComponentItemRevision latestReleasedRevision = MaterialTransferValidate.getLatestValidReleasedRevision(session, (TCComponentItem) object, valid_status_list, object);
					if (latestReleasedRevision != null)
						objectRevision = latestReleasedRevision;
					else {
						if (isByPassWithNotYetReleasedPart((TCComponentItem) object)) {
							objectRevision = latestReleasedRevision;
						} else {
							MaterialTransferModel newItem = new MaterialTransferModel(session, dmService);
							newItem.setMaterialItemObject((TCComponentItem) object);
							newItem.setMessage("Material not yet released. Please released before transfer.");
							newItem.setPrepareStatus(PREPARE_STATUS.NOT_YET_RELEASE);
							materialTransferList.add(newItem);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (objectRevision != null) {
				MaterialTransferModel newItem = new MaterialTransferModel(session, dmService);
				newItem.setPlantCode(dlg.txtPlant.getText());
				newItem.setMcnNumber(dlg.txtMCN.getText());
				newItem.setMaterialRevObject(objectRevision);
				materialTransferList.add(newItem);

				if (!newItem.isAlreadyTransfer()) {
					try {
						validateData(newItem);
					} catch (Exception e) {
						e.printStackTrace();
						newItem.setMessage(e.toString());
						newItem.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
					}
				}
			}
		}
		refreshReport();
		boolean haveTranferValidate = false;
		boolean haveNotReleased = false;
		for (MaterialTransferModel transfer : materialTransferList) {
			if (transfer.getPrepareStatus() == PREPARE_STATUS.NOT_VALIDATE)
				return false;
			else if (transfer.getPrepareStatus() == PREPARE_STATUS.VALIDATE)
				haveTranferValidate = true;
			else if (transfer.getPrepareStatus() == PREPARE_STATUS.NOT_YET_RELEASE)
				haveNotReleased = true;
		}

		if (haveNotReleased) {
			dlg.lblReleaseDCR.setVisible(true);
			dlg.txtReleaseDCR.setVisible(true);
			dlg.lblFrsNumber.setVisible(true);
			dlg.txtFRSNumber.setVisible(true);
			dlg.setMessage("Input \"Release DCR\" (mandatory) and \"FRS Number\" before transfer.", IMessageProvider.WARNING);
			return true;
		}

		return haveTranferValidate;
	}

	private void validateData(MaterialTransferModel object) throws Exception {
		ArrayList<String> valid_status_list = MaterialTransferValidate.getRevisionRuleStatusEntries(session, PropertyDefines.REVISION_RULE_RELEASE);
		TCComponentItemRevision latestReleasedRevision = MaterialTransferValidate.getLatestValidReleasedRevision(session, object.getMaterialRevObject().getItem(), valid_status_list, object.getMaterialRevObject());
		if (latestReleasedRevision == null) {
			if (!isByPassWithNotYetReleasedPart(object.getMaterialRevObject().getItem())) {
				object.setMessage("Material not yet released. Please released before transfer.");
				object.setPrepareStatus(PREPARE_STATUS.NOT_YET_RELEASE);
				return;
			}
		} else {
			String latestRevID = latestReleasedRevision.getProperty(PropertyDefines.ITEM_REV_ID);
			if (latestRevID.compareToIgnoreCase(object.getRevisionNumber()) != 0) {
				object.setMessage("Revision " + object.getRevisionNumber() + " is not latest released revision. Please replace with latest released revision (" + latestRevID + ").");
				object.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				return;
			}
		}

		if (object.getUom().isEmpty()) {
			object.setMessage("Unit of Measure (UOM) is empty. Please fill information on Item");
			object.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
			return;
		}

		if (object.getMaterialType().isEmpty()) {
			object.setMessage("Material Type is empty. Please fill Plant Make/buy & Material Type information on Item");
			object.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
			return;
		}

		String plantCode = dlg.txtPlant.getText();
		if (object.getPlantForm() == null) {
			if (object.isFRS() == false) {
				object.setMessage(plantCode + " related plant form missing on material. Please attach Plant form or fill plant code");
				object.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				return;
			}
//			else {
//				object.setPlantCode("3001");
//				object.setMakeBuy("F");
//			}
		}
		
		if (object.isFRS() == false) {
			String procurement = UIGetValuesUtility.getPlantMakeBuy(object.getMaterialRevObject().getItem(), plantCode);
			if (procurement.isEmpty()) {
				object.setMessage("Procurement Type is empty. Please fill on Plant form");
				object.setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				return;
			}
		}
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
				put("Material", "15");
				put("Material Type", "15");
				put("Message", "65");
			}
		};
		validationResultText.append(StringExtension.genTableHeader(header));
		//
		int i = 0;
		for (MaterialTransferModel transferItem : materialTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getMaterialNumber() + "/" + transferItem.getRevisionNumber() + "</td>");
			validationResultText.append("<td>" + transferItem.getMaterialType() + "</td>");

			if (transferItem.getPrepareStatus() == PREPARE_STATUS.ALREADY_TRANSFER) {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetDefault("Already transfer to SAP") + "</td>");
			} else if (transferItem.getPrepareStatus() == PREPARE_STATUS.VALIDATE) {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Ready to transfer") + "</td>");
			} else if (transferItem.getPrepareStatus() == PREPARE_STATUS.NOT_VALIDATE) {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(transferItem.getMessage()) + "</td>");
			} else {
				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(transferItem.getMessage()) + "</td>");
			}
			validationResultText.append("</tr>");
		}
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
	}

	private void transferData() {
		if (dlg.txtReleaseDCR.getVisible()) {
			if (dlg.txtReleaseDCR.getText().isEmpty())
				return;
		}

		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + dlg.txtMCN.getText());
		SAPURL SAPConnect = new SAPURL();
		ArrayList<Thread> listThread = new ArrayList<Thread>();
		ExecutorService executor = Executors.newFixedThreadPool(100);
		int count = 0;

		StringBuilder printReport = new StringBuilder();
		Logger.bufferResponse("APPEND", new String[] { "<html>", "<body>" }, printReport);

		Set<String> topInfo = new HashSet<String>();
		topInfo.add("User: " + session.getUserName());
		topInfo.add("MCN: " + dlg.txtMCN.getText());
		topInfo.add("Server: " + dlg.cbServer.getText());
		topInfo.add("Time: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
		topInfo.add("Plant:" + dlg.txtPlant.getText());
		Logger.bufferResponse("DETAILS", topInfo.toArray(new String[0]), printReport);
		Logger.bufferResponse("HEADER", new String[] { "No", "Material", "Message", "Result" }, printReport);
		try {
			for (MaterialTransferModel materialData : materialTransferList) {
				if (materialData.getPrepareStatus() == PREPARE_STATUS.VALIDATE || materialData.getPrepareStatus() == PREPARE_STATUS.NOT_YET_RELEASE) {
					MaterialTransferThread transfer = new MaterialTransferThread();
					transfer.setServerInfo(serverInfo);
					transfer.setTransferModel(materialData);
					transfer.setStrBuilder(printReport);
					transfer.setLogFolder(logFolder);
					transfer.setSAPConnect(SAPConnect);
					transfer.setCount(++count);
					Thread t = new Thread(transfer);
					executor.execute(t);
					listThread.add(t);
				}
			}
			executor.shutdown();

			for (Thread t : listThread) {
				t.join();
			}

			while (!executor.isTerminated()) {
				System.out.println("Wait for thread pool terminate!");
				Thread.sleep(2000);
			}

			if (dlg.txtReleaseDCR.getVisible()) {
				selectedObject.setProperty("vf6_Released_DCR_Number", dlg.txtReleaseDCR.getText());
				selectedObject.setProperty("vf6_FRS_Number", dlg.txtFRSNumber.getText());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logger.bufferResponse("APPEND", new String[] { "</table>", "</body>", "</html>" }, printReport);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(printReport.toString(), logFolder, "MATERIAL"));
					viewdialog.setTitle("Transfer Status");
					viewdialog.setSize(600, 400);
					viewdialog.setLocationRelativeTo(null);
					viewdialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		selectedObject = (TCComponent) targetComponents[0];
		return false;
	}

	private void showProgressDialog(boolean isShow) {
		if (isShow) {
			if (progressMonitorDialog == null) {
				progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
				progressMonitorDialog.open();
			}
		} else {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
		}
	}

	private boolean isByPassWithNotYetReleasedPart(TCComponentItem itemRev) {
		try {
			if (userRole.contains("MB Engineer")) {
				String partNo = itemRev.getPropertyDisplayableValue("item_id");
				if (partNo.substring(0, 3).compareTo("INP") == 0 || partNo.substring(0, 3).compareTo("SUB") == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
