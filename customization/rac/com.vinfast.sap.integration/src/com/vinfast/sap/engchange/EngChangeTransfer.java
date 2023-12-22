package com.vinfast.sap.engchange;

import java.util.HashMap;

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

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.dialogs.GenericDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class EngChangeTransfer extends AbstractHandler {
	OrganizationInformationAbstract serverInfo = null;
	String cmd = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentItemRevision changeObject = (TCComponentItemRevision) selectedObjects[0];
			TCSession clientSession = changeObject.getSession();
			cmd = event.getCommand().toString();
			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);
			if (UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true) {
				DataManagementService dmCoreService = DataManagementService.getService(clientSession);
				dmCoreService.getProperties(new TCComponent[] { changeObject }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ECM_PLANT });
				String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
				
				GenericDialog transferDlg = new GenericDialog(new Shell());
				transferDlg.create();
				transferDlg.setTitle("Change Transfer");
				transferDlg.setMCN(MCN);
				transferDlg.setPlant(plant);
				transferDlg.setServer("PRODUCTION");
				transferDlg.setSession(clientSession);
				Button transferBtn = transferDlg.getOkButton();
				transferBtn.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						transferBtn.setEnabled(false);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								transferChange(transferDlg, changeObject, transferDlg.comboServer.getText());
								transferDlg.getShell().dispose();
							}
						});
					}
				});

				transferDlg.open();
			} else {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access...", MessageBox.ERROR);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void transferChange(GenericDialog transferDlg, TCComponentItemRevision changeObject, String serverType) {
		SAPURL SAPConnect = new SAPURL();
		try {
			serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, transferDlg.getSession());
			if (serverInfo.getServerIP() == null) {
				MessageBox.post("Error in PLM-SAP server connection.", "SAP Server...", MessageBox.ERROR);
				return;
			}

			DataManagementService dmCoreService = DataManagementService.getService(transferDlg.getSession());

			String[] properties = new String[11];
			properties[0] = PropertyDefines.ITEM_ID;
			properties[1] = PropertyDefines.ITEM_NAME;
			properties[2] = PropertyDefines.ECM_ACTION;
			properties[3] = PropertyDefines.ECM_PLANT;
			properties[4] = PropertyDefines.ECM_REASON;
			properties[5] = PropertyDefines.ECM_TYPE;
			properties[6] = PropertyDefines.ECM_COORDINATE_CODE;
			properties[7] = PropertyDefines.ECM_DISPOSAL_CODE;
			properties[8] = PropertyDefines.ECM_MODEL_YEAR;
			properties[9] = PropertyDefines.ECM_EFF_DATE;
			properties[10] = PropertyDefines.ECM_COMMENTS;

			TCComponentItem changeItem = changeObject.getItem();

			transferDlg.setProgressStatus(50);

			dmCoreService.getProperties(new TCComponent[] { changeItem }, properties);

			String MCN_ID = transferDlg.getMCN();

			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + MCN_ID);

			HashMap<String, String> dataMap = new HashMap<String, String>();

			dataMap.put("MCN", MCN_ID);
			dataMap.put("PLANT_CODE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT));
			dataMap.put("ECM_DESCRIPTION", changeItem.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME));
			dataMap.put("CHANGE_REASON", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_REASON));
			dataMap.put("CHANGE_TYPE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_TYPE));
			dataMap.put("COORDINATE_CODE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_COORDINATE_CODE));
			dataMap.put("DISPOSAL_CODE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_DISPOSAL_CODE));
			dataMap.put("MODEL_YEAR", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_MODEL_YEAR));
			dataMap.put("PROPOSED_DATE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_EFF_DATE));
			dataMap.put("COMMENTS", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_COMMENTS));
			dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

			if (transferDlg.getServer().equals("PRODUCTION")) {
				if (changeObject.getProperty(PropertyDefines.REV_TO_SAP).trim().length() > 0) {
					dataMap.put("ACTION", "C");
				} else {
					dataMap.put("ACTION", "A");
				}
			} else {
				dataMap.put("ACTION", "A");
			}

			transferDlg.setProgressStatus(100);
			String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.changeWebserviceURL(serverInfo.getServerIP()), dataMap, SAPURL.ECN_HEADER, SAPURL.ECN_TAG, SAPURL.ECN_NAMESPACE, "I_CHG_" + MCN_ID, logFolder, serverInfo.getAuth());
			if (message[0].equals("S") || message[1].contains("already existed")) {
				MessageBox.post(MCN_ID + " successfully transferred to SAP", message[1], "Change Transfer...", MessageBox.INFORMATION);
				if (transferDlg.getServer().equals("PRODUCTION") == true) {
					UIGetValuesUtility.setProperty(dmCoreService, changeObject, PropertyDefines.REV_TO_SAP, changeItem.getPropertyDisplayableValue("vf6_sap_plant"));
				}
			} else {
				MessageBox.post("Response from SAP:", message[1], "Change Transfer...", MessageBox.ERROR);
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
