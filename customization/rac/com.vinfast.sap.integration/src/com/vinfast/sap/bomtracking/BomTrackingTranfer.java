package com.vinfast.sap.bomtracking;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.SwingUtilities;

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
import org.eclipse.swt.widgets.TableItem;
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
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class BomTrackingTranfer extends AbstractHandler{
	UIGetValuesUtility util = null;
	BomTrackingTranferDialog transferDlg;
	TCSession clientSession;
	OrganizationInformationAbstract serverInfo = null;
	String cmd = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];
			clientSession = changeObject.getSession();
			cmd = event.getCommand().toString();
			util = new UIGetValuesUtility();
			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);
			
			if(UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true){
				DataManagementService dmCoreService =  DataManagementService.getService(clientSession);
				dmCoreService.getProperties(new TCComponent[] {changeObject}, new String[] {PropertyDefines.ITEM_ID,PropertyDefines.ECM_PLANT});
				String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
				
				transferDlg = new BomTrackingTranferDialog(new Shell());
				transferDlg.create();
				transferDlg.setTitle("BOM Tracking Tranfer");
				transferDlg.setMCN(MCN);
				transferDlg.setPlant(plant);
				transferDlg.setServer("PRODUCTION");
				transferDlg.setSession(clientSession);
				transferDlg.setTableItem(changeObject.getRelatedComponents("vf6_detail_changes"));
				Button btnTranfer = transferDlg.getOkButton();
				btnTranfer.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						btnTranfer.setEnabled(false);
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
			}
			else {
				MessageBox.post("You are not authorized to transfer MCN.","Please check group/role and try again.", "Access...", MessageBox.ERROR);
				return null;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public int getProgress(int current, int total) {
		float value = (float)current/(float)total;
		int percent = (int) (value*100);
		return percent;
	}

	public void transferChange(BomTrackingTranferDialog transferDlg, TCComponentItemRevision changeObject, String serverType) {
		transferDlg.setProgressStatus(0);
		SAPURL SAPConnect = new SAPURL();
		new Logger();
		try {
			String mcn = "";
			String platformCode = "";
			String modelYear = "";
			serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, transferDlg.getSession());
			String SERVER_IP = serverInfo.getServerIP();
			String auth = serverInfo.getAuth();
			
			if(SERVER_IP == null) {
				MessageBox.post("Error in PLM-SAP server connection.", "SAP Server...", MessageBox.ERROR);
				return;
			}
			
			DataManagementService dmCoreService =  DataManagementService.getService(transferDlg.getSession());
			String[] properties =  { 
										PropertyDefines.ITEM_ID,
										PropertyDefines.ITEM_NAME,
										PropertyDefines.ECM_ACTION,
										PropertyDefines.ECM_PLANT,
										PropertyDefines.ECM_REASON,
										PropertyDefines.ECM_TYPE,
										PropertyDefines.ECM_COORDINATE_CODE,
										PropertyDefines.ECM_DISPOSAL_CODE,
										PropertyDefines.ECM_MODEL_YEAR,
										PropertyDefines.ECM_EFF_DATE,
										PropertyDefines.ECM_COMMENTS
									};
			
			TCComponentItem changeItem = changeObject.getItem();
			dmCoreService.getProperties(new TCComponent[] {changeItem}, properties);
			mcn = transferDlg.getMCN();
			
//			String actionString = "";
//			if(transferDlg.getServer().equals("PRODUCTION")) {
//				if(changeObject.getProperty(PropertyDefines.REV_TO_SAP).trim().length() > 0) {
//					actionString = "C";
//				}
//				else {
//					actionString = "A";
//				}
//			}
//			else {
//				actionString = "A";
//			}
			TCComponent[] impactedShop = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_SHOP);
			if(impactedShop.length > 0) {
				TCComponentItemRevision topLevelItemRevision = UIGetValuesUtility.getTopLevelItemRevision(clientSession, (TCComponentItemRevision) impactedShop[0],PropertyDefines.RULE_WORKING);
				MaterialPlatformCode materialCode = UIGetValuesUtility.getPlatformCode(dmCoreService, topLevelItemRevision);
				platformCode = materialCode.getPlatformCode();
				modelYear = materialCode.getModelYear();
			}
			
			HashMap<String, String> dataMap;
			TCComponent[] objectChildComponents = changeObject.getRelatedComponents("vf6_detail_changes");
			int index = 0;
			
			StringBuilder strBuilder =  new StringBuilder();
			
			strBuilder.append("<html><body>");
			
			String[] printValues = new String[] {"MCN:" + transferDlg.getMCN(), "Plant:" + transferDlg.getPlant(), "User:" + clientSession.getUserName(), "Time:"+new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())};
			Logger.bufferResponse("DETAILS", printValues, strBuilder);

			printValues = new String[] {"No.","Action","Main Group","Sub Group","BOMLine ID","Message","Result"};
			Logger.bufferResponse("HEADER", printValues, strBuilder);
			
			int percentage = 1;
			int count = 1;
			int total = 0;
			
			if(objectChildComponents.length > 0) {
				for(index = 0; index < objectChildComponents.length; index++ ) {
					TableItem tableItem = transferDlg.getTableItem().getItem(index);
					if(tableItem.getChecked()) {
						total++;
					}
				}
				index = 0;
				String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcn);
				for (TCComponent tcComponent : objectChildComponents) {
					TableItem tableItem = transferDlg.getTableItem().getItem(index++);
					if(tableItem.getChecked()) {
						String mainGroup = tcComponent.getPropertyDisplayableValue("vf6_main_group_str");
						String subGroup = tcComponent.getPropertyDisplayableValue("vf6_sub_group_str");
						String bomID = tcComponent.getPropertyDisplayableValue("vf6_bomline_id");
						String partNo = tcComponent.getPropertyDisplayableValue("vf6_part_number");
						String quantity = tcComponent.getPropertyDisplayableValue("vf6_quantity");
						String action = tcComponent.getPropertyDisplayableValue("vf6_action");
						String oldPart = tcComponent.getPropertyDisplayableValue("vf6_replaced_part_number");
						String newPart = tcComponent.getPropertyDisplayableValue("vf6_new_part_number");
						
						transferDlg.setProgressStatus(getProgress(percentage, total));
						dataMap = new HashMap<String, String>();
						dataMap.put("MODELYEAR", modelYear);
						dataMap.put("PLANTCODE", changeItem.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT));
						dataMap.put("PLATFORM", platformCode);
						//
						dataMap.put("MAINGROUP", mainGroup);
						dataMap.put("SUBGROUP", subGroup);
						dataMap.put("BOMID", bomID);
						dataMap.put("PARTNO", partNo);
						dataMap.put("QUANTITY", quantity);
						dataMap.put("ACTION", action.equals("Add") ? "A" : action.equals("Delete") ? "D" : "R");
						dataMap.put("OLD_PART", oldPart);
						dataMap.put("NEW_PART", newPart);
						//
						dataMap.put("MCN", mcn);
						dataMap.put("TIMESTAMP", UIGetValuesUtility.getSequenceID());
						
						String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.trackingWebserviceURL(SERVER_IP), dataMap, SAPURL.TRACKING_BOM_HEADER, SAPURL.TRACKING_BOM_TAG, SAPURL.TRACKING_BOM_NAMESPACE, "I_TRK_" + mcn, logFolder, auth);
						if(message[0].equals("E") || message[0].equals("")) {
							printValues = new String[] { 
									Integer.toString(count),
									action,
									mainGroup,
									subGroup,
									bomID,
									message[1],
									"Error"
							};
							Logger.bufferResponse("PRINT", printValues, strBuilder);
						}
						else {
							printValues = new String[] { 
									Integer.toString(count),
									action,
									mainGroup,
									subGroup,
									bomID,
									message[1],
									"Success"
							};
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							
							if(transferDlg.getServer().equals("PRODUCTION") == true ) {
								tcComponent.setProperty("vf6_is_transferred", "true");
							}
						}
					}
					count++;

					percentage++;
				}
				
				transferDlg.setProgressStatus(100);
				
				strBuilder.append("</table>");
				strBuilder.append("</body></html>");

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							new Logger();
							StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(strBuilder.toString(), logFolder, "MATERIAL"));
							viewdialog.setTitle("Transfer Status");
							viewdialog.setSize(600, 400);
							viewdialog.setLocationRelativeTo(null);
							viewdialog.setVisible(true);
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				});	
			}
		}
		catch (TCException e) {
			e.printStackTrace();
		} 
		catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
}
