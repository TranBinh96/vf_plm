package com.vinfast.sap.material;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedConfigParameters;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedInputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedOutputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedParentInfo;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedResponse;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.dialogs.GenericDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class FinishGoodTransfer extends AbstractHandler {

	UIGetValuesUtility util = null;	
	OrganizationInformationAbstract serverInfo = null;
	String cmd = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {

			ISelection selection = HandlerUtil.getCurrentSelection( event );

			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];

			TCSession clientSession = changeObject.getSession();

			util	= new UIGetValuesUtility();
			
			cmd = event.getCommand().toString();

			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);

			if(UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true){

				DataManagementService dmCoreService =  DataManagementService.getService(clientSession);

				dmCoreService.getProperties(new TCComponent[] {changeObject}, new String[] {PropertyDefines.ITEM_ID,PropertyDefines.ECM_PLANT});

				String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);

				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

				String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);

				GenericDialog transferDlg = new GenericDialog(new Shell());

				transferDlg.create();

				transferDlg.setTitle("Material Transfer");

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

								transferMaterial(transferDlg, changeObject, transferDlg.comboServer.getText());

								transferDlg.getShell().dispose();
							}
						});
					}
				});

				transferDlg.open();
			}else {

				MessageBox.post("You are not authorized to transfer MCN.","Please check group/role and try again.", "Access...", MessageBox.ERROR);
				return null;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void transferMaterial( GenericDialog transferDlg, TCComponentItemRevision changeObject, String serverType) {

		SAPURL SAPConnect 	= new SAPURL();
		new Logger();
		TCSession session	= transferDlg.getSession();
		new UIGetValuesUtility();

		DataManagementService dataManagmentService =  DataManagementService.getService(session);

		try {

			String plantCode = transferDlg.getPlant();

			serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, transferDlg.getSession());

			String[] materialTypes =  new String[1];

			materialTypes[0] = PropertyDefines.TYPE_VF_ITEM_REVISION;

			TCComponent[] materials = UIGetValuesUtility.getRelatedComponents(dataManagmentService, changeObject, materialTypes,PropertyDefines.REL_SOL_ITEMS);

			if(materials != null ){

				int total = materials.length;

				String[] itemproperties =  new String[2];
				itemproperties[0] = PropertyDefines.ITEM_MAKE_BUY;
				itemproperties[1] = PropertyDefines.ITEM_UOM;

				String[] revproperties =  new String[3];
				revproperties[0] = PropertyDefines.ITEM_ID;
				revproperties[1] = PropertyDefines.ITEM_REV_ID;
				revproperties[2] = PropertyDefines.ITEM_NAME;

				dataManagmentService.getProperties(materials, revproperties);

				String SAP_ID = transferDlg.getMCN();
				String logFolder = UIGetValuesUtility.createLogFolder("MCN"+SAP_ID);

				StringBuilder strBuilder =  new StringBuilder();

				strBuilder.append("<html><body>");

				String[] printValues = new String[] {"MCN:"+transferDlg.getMCN(),"Plant:"+transferDlg.getPlant(),"User:"+session.getUserName(),"Time:"+new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())};
				Logger.bufferResponse("DETAILS", printValues, strBuilder);

				printValues = new String[] {"S.No","Material","Message","Result"};
				Logger.bufferResponse("HEADER", printValues, strBuilder);

				int percentage = 1;
				int count = 1;

				for(TCComponent solution_Item : materials){

					HashMap<String, String> dataMap = null;

					String errorString = "";

					transferDlg.setProgressStatus(getProgress(percentage, total));

					TCComponentItemRevision materialRevision = (TCComponentItemRevision)solution_Item;

					String revID = materialRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

					String revRevID = materialRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);

					TCComponentItem materialItem = materialRevision.getItem();

					dataManagmentService.getProperties(new TCComponent[] {materialItem}, itemproperties);

					String itemUOM = materialItem.getPropertyDisplayableValue(PropertyDefines.ITEM_UOM);
					if(itemUOM.equals("")) {

						errorString = "Unit of Measure (UOM) value is empty. Please fill information on Item";
					}else {

						if(itemUOM.equals("each")) {

							itemUOM = "EA";
						}
					}



					String materialType = "ZFG";
					String makebuyValue = "E";


					TCProperty functionalCode = materialItem.getTCProperty(PropertyDefines.ITEM_FUNC_CODE);
					String functionalCodeValue = "";
					if(functionalCode != null) {

						functionalCodeValue = functionalCode.getStringValue().trim();
					}

					TCProperty approvalCode = materialItem.getTCProperty(PropertyDefines.ITEM_APPOV_CODE);
					String approvalCodeValue = "";
					if(approvalCode != null) {

						approvalCodeValue = approvalCode.getStringValue().trim();
					}

					TCProperty gmPart = materialItem.getTCProperty(PropertyDefines.ITEM_GM_PARTNO);
					String gmPartValue = "";
					if(gmPart != null) {

						gmPartValue = gmPart.getStringValue().trim();
					}

					TCProperty oldPart = materialItem.getTCProperty(PropertyDefines.ITEM_OLD_PARTNO);
					String oldPartValue = "";
					if(oldPart != null) {

						oldPartValue = oldPart.getStringValue().trim();
					}

					TCProperty partCat = materialItem.getTCProperty(PropertyDefines.ITEM_PART_CATEGORY);
					String partCatValue = "";
					if(partCat != null) {

						partCatValue = partCat.getStringValue();
					}

					TCProperty traceableProp = materialItem.getTCProperty(PropertyDefines.ITEM_IS_TRACEABLE);
					String traceable = "N";
					if(traceableProp != null && traceableProp.getStringValue() != null && !traceableProp.getStringValue().trim().isEmpty()) {

						traceable = traceableProp.getStringValue().trim();
					}

					TCProperty broadCode = materialRevision.getTCProperty(PropertyDefines.REV_BROADCAST);
					String broadCodeValue = "";
					if(broadCode != null) {

						broadCodeValue = broadCode.getStringValue().trim();
					}

					TCProperty vnDescription = materialRevision.getTCProperty(PropertyDefines.REV_VIET_DESCRIPTION);
					String vnDescriptionValue = "";
					if(vnDescription != null && vnDescription.getStringValue() != null) {

						vnDescriptionValue = vnDescription.getStringValue().trim();
					}

					String donorVehicle = getWhereUsedPrograms(session, materialRevision, PropertyDefines.RULE_WORKING);

					dataMap =  new HashMap<String, String>();
					dataMap.put("MATERIALNUMBER", revID);
					dataMap.put("MCN", SAP_ID);
					dataMap.put("ACTION", PropertyDefines.ACTION_ADD);
					dataMap.put("SAP_PLANT",plantCode);
					dataMap.put("REVISIONNUMBER",revRevID);
					dataMap.put("DESCRIPTION",materialRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME));
					dataMap.put("MATERIALTYPE",materialType);
					dataMap.put("SEQUENCE",UIGetValuesUtility.getSequenceID());
					dataMap.put("MAKEBUY", makebuyValue);
					dataMap.put("FUNCTIONALCLASS",functionalCodeValue);
					dataMap.put("APPROVALCLASS", approvalCodeValue);
					dataMap.put("OLDMATERIALNUMBER", oldPartValue);
					dataMap.put("PART_TYPE", partCatValue);
					dataMap.put("GM_PART",gmPartValue);
					dataMap.put("BRDCODE", broadCodeValue);
					dataMap.put("UOM", itemUOM);
					dataMap.put("TRACEABLEPART", traceable);
					dataMap.put("FUNCTIONAL", donorVehicle);

					if (!vnDescriptionValue.isEmpty()) dataMap.put("DESCRIPTIONVIETNAMESE", vnDescriptionValue);

					

					if(errorString.equals("")) {

						CreateSoapHttpRequest.sendRequest(SAPConnect.materialWebserviceURL(serverInfo.getServerIP()), dataMap,SAPURL.MAT_HEADER, SAPURL.MAT_TAG, SAPURL.MAT_NAMESPACE,"I_MAT_"+count+"_"+revID+"_"+revRevID, logFolder, serverInfo.getAuth());

//						if(message[0].equals("E")) {
//
//							printValues = new String[]{Integer.toString(count),revID+"/"+revRevID,message[1],"Error"};
//							log.bufferResponse("PRINT", printValues, strBuilder);
//
//						}else {
//
//							printValues = new String[]{Integer.toString(count),revID+"/"+revRevID,message[1],"Success"};
//							log.bufferResponse("PRINT", printValues, strBuilder);
//
//						}
//
//						accessService.checkPrivileges(materialRevision.getUid(), new String[] {"WRITE"}, accessHolder);
//
//						values = accessHolder.value;
//
//						if(server.equals("PRODUCTION") && values[0] == true) {
//							materialRevision.setProperty(PropertyDefines.REV_TO_SAP, "Yes");
//							setTransferToERP(materialRevision.getItem());
//						}
					}else {

						printValues = new String[]{Integer.toString(count),revID+"/"+revRevID,errorString,"Error"};
						Logger.bufferResponse("PRINT", printValues, strBuilder);
					}

					count++;

					percentage++;
				}

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
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

			}else {

				MessageBox.post("Solution Items folder is empty. Please copy materials and retry", "Error", MessageBox.ERROR);
				return;
			}


		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return;
	}
	
	public void setTransferToERP(TCComponent item) {
		if(item == null) {
			return;
		}
		String transferStatus;
		try {
			transferStatus = item.getProperty(PropertyDefines.ITEM_IS_IN_SAP).trim();
			if(transferStatus.length() > 1){
				return;
			}else {
				item.setProperty(PropertyDefines.ITEM_IS_IN_SAP, "True");
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getProgress(int current, int total) {

		float value = (float)current/(float)total;

		int percent = (int) (value*100);

		return percent;

	}

	public String getWhereUsedPrograms(TCSession session, TCComponentItemRevision material, String revRule) {

		String contextIds = "";

		TCComponentRevisionRule RevisionRule = null;

		try {

			if(revRule != null) {

				RevisionRule = UIGetValuesUtility.getRevisionRule(revRule, session);
			}

			WhereUsedConfigParameters configparam = new WhereUsedConfigParameters();
			configparam.boolMap.put("whereUsedPreciseFlag", false); 
			configparam.intMap.put("numLevels", new BigInteger("-1"));
			configparam.tagMap.put("revision_rule", RevisionRule);

			WhereUsedInputData inputData = new WhereUsedInputData();
			inputData.clientId = "";
			inputData.inputObject = material;
			inputData.useLocalParams = false;
			inputData.inputParams = configparam;

			WhereUsedResponse WUResponse= DataManagementService.getService(session).whereUsed(new WhereUsedInputData[] {inputData}, configparam);

			WhereUsedOutputData[] outputData = WUResponse.output ;

			WhereUsedParentInfo[] info = outputData[0].info;

			if(info != null) {

				ArrayList<TCComponentItemRevision> programCodes =  new ArrayList<>();

				int toplevel = 0;

				for(WhereUsedParentInfo parentInfo : info) {

					int level = parentInfo.level;

					TCComponentItemRevision topLineRevision = (TCComponentItemRevision) parentInfo.parentObject;

					if(topLineRevision.getType().equals(PropertyDefines.TYPE_VF_ITEM_REVISION)) {

						if(level > toplevel) {
							toplevel = level;
							programCodes.clear();
						}

						if(level == toplevel) {

							programCodes.add(topLineRevision);
						}
					}

				} 

				if(programCodes.isEmpty() == false) {

					for(TCComponentItemRevision revision : programCodes) {

						TCComponent item  = revision.getItem();

						TCComponent[] context = item.getReferenceListProperty(PropertyDefines.REL_VARIANT_CONFIG);
						
						if(context.length < 1) {
							continue;
						}

						String contextID = context[0].getProperty(PropertyDefines.ITEM_OBJECT_STR);

						if(contextIds.equals("")) {
							
							contextIds = contextID.substring(0, 9).replace("-", "_");
							
						}else {
							
							contextIds = contextIds +","+ contextID.substring(0, 9).replace("-", "_");
						}
					}
				}
			}
		}	catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contextIds;
	}
}
