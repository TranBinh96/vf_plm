package com.teamcenter.integration.arch;

import java.io.File;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class IntegrationHandlerAbstract extends AbstractHandler {
	protected TCComponentItemRevision mbomTOPItemRev;
	protected TCComponent mbomTOPLine;
	protected TCComponent bopTOPLine;
	protected MCNInformation mcnInfo = null;
	protected TCSession session;
	protected TCComponentItemRevision changeObject = null;
	protected DataManagementService dmService = null;
	protected boolean transferByAPI = false;
	protected ProgressMonitorDialog progressDlg;
	protected StringBuilder resultReport = new StringBuilder();
	protected boolean transferValid = true;
	private boolean openBomWindow = false;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		return null;
	}

	protected boolean initData() {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select 1 MCN Revision.", "Error", MessageBox.ERROR);
				return false;
			}

			if (!TCExtension.checkPermissionAccess(changeObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return false;
			}

			String[] preferenceValue = TCExtension.GetPreferenceValues("VF_MES_TRANSFER_API", session);
			if (preferenceValue != null && preferenceValue.length > 0) {
				transferByAPI = preferenceValue[0].compareTo("true") == 0;
			}

			loadMCNInformation();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void loadMCNInformation() {
		try {
			String plant = this.changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			String shop = this.changeObject.getItem().getPropertyDisplayableValue("vf6_shop");
			String model = "";
			String year = "";
			String materialCode = "";
			String MCN_SAPID = this.changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);

			mcnInfo = new MCNInformation();
			mcnInfo.setPlant(plant);
			mcnInfo.setSapID(MCN_SAPID);
			mcnInfo.setMcnID(MCN);
			mcnInfo.setShop(shop);
			mcnInfo.setPlatForm(model);
			mcnInfo.setModelYear(year);
			mcnInfo.setMaterialCode(materialCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		if (targetComponents[0] instanceof TCComponentItemRevision) {
			changeObject = (TCComponentItemRevision) targetComponents[0];
		}
		if (changeObject == null)
			return true;
		return false;
	}

	protected void getMBOMTopItemRev(TCComponent shopItem, String company) {
		try {
			if (company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
				mbomTOPItemRev = (TCComponentItemRevision) shopItem;
			} else {
				if (shopItem instanceof TCComponentItemRevision)
					mbomTOPItemRev = UIGetValuesUtility.getTopLevelItemRevision(session, (TCComponentItemRevision) shopItem, PropertyDefines.REVISION_RULE_WORKING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void openBOMWindow() {
		if (openBomWindow)
			return;

		OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, mbomTOPItemRev);
		if (createdBOMViews != null) {
			for (OpenContextInfo views : createdBOMViews) {
				if (views.context.getType().equals("BOMLine"))
					mbomTOPLine = (TCComponentBOMLine) views.context;
				else if (views.context.getType().equals("Mfg0BvrPlantBOP"))
					bopTOPLine = (TCComponentBOMLine) views.context;
				else
					UIGetValuesUtility.closeContext(session, (TCComponentBOMLine) views.context);
			}
		}
		openBomWindow = true;
	}

	protected void printReport(String argument, String[] values) {
		switch (argument) {
		case "APPEND":
			for (String info : values) {
				resultReport.append(info);
			}
			break;
		case "DETAILS":
			resultReport.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
			resultReport.append("<tr>");
			for (String details : values) {
				resultReport.append("<th><b>" + details + "</b></th>");
			}
			resultReport.append("</tr></table>");
			break;
		case "HEADER":
			resultReport.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");
			for (String header : values) {
				resultReport.append("<th><b>" + header + "</b></th>");
			}
			resultReport.append("</tr>");
			break;
		case "PRINT":
			if (values[values.length - 1].equalsIgnoreCase("Error") || values[values.length - 1].equalsIgnoreCase("NOT MATCH")) {
				resultReport.append("<tr>");
				for (String header : values) {
					resultReport.append("<td align=\"center\"><font color=\"red\">" + header + "</font></td>");
				}
				resultReport.append("</tr>");
			} else {
				resultReport.append("<tr>");
				for (String header : values) {
					resultReport.append("<td align=\"center\">" + header + "</td>");
				}
				resultReport.append("</tr>");
			}
			break;
		}
	}

	protected File popupReport(String fileName) {
		File attachFile = Logger.writeBufferResponse(resultReport.toString(), UIGetValuesUtility.createLogFolder("MCN" + mcnInfo.getMcnID()), fileName);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StringViewerDialog viewdialog = new StringViewerDialog(attachFile);
					viewdialog.setTitle("Transfer Status");
					viewdialog.setSize(600, 400);
					viewdialog.setLocationRelativeTo(null);
					viewdialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return attachFile;
	}

	protected void updateOperationAfterTransferSuccess(TCComponent operationRevision, String workStationID) {
		if (workStationID.length() < PropertyDefines.MAX_REV_TO_MES_LENGTH) {
			UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, workStationID);
		} else {
			UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_TO_MES, "YES");
			UIGetValuesUtility.setProperty(dmService, operationRevision, PropertyDefines.REV_USERNOTE, workStationID);
		}
	}
}
