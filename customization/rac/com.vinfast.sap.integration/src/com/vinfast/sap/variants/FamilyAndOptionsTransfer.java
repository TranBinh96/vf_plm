package com.vinfast.sap.variants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCfg0ConfiguratorPerspective;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.configurator.ConfigManager;
import com.vinfast.sap.dialogs.ConfiguratorDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class FamilyAndOptionsTransfer extends AbstractHandler{

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	private String auth = PropertyDefines.SERVER_SAP_VF_AUTH;
	private DataManagementService dmCoreService;
	private ArrayList<LinkedHashMap<String,String>> loadValues = null;
	private static LinkedHashMap<String, TCComponent> validFamilGroupMap = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {	
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];
			TCSession clientSession = changeObject.getSession();
			dmCoreService =  DataManagementService.getService(clientSession);
			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}else {
				String company = UIGetValuesUtility.getCompanyCode(event);
				ConfigManager configManager = new ConfigManager(company).loadChangeAttachments(clientSession, changeObject);
				if (configManager.getModel().isEmpty() || configManager.getYear().isEmpty()) {
					MessageBox.post("Error to get Plaform and Model Year. Contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
					return false;
				}

				ConfiguratorDialog configDialog = new ConfiguratorDialog(new Shell());
				configDialog.create();
				configDialog.setTitle("Rules Transfer");
				configDialog.setModel(configManager.getModel());
				configDialog.setYear(configManager.getYear());
				configDialog.setMCN(configManager.getMCN());
				configDialog.setPlant(configManager.getPlant());
				configDialog.setServer("PRODUCTION");
				configDialog.getPrepareButton().addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								loadValues = loadDataTransfer(configManager, changeObject);
								StringBuilder strBuilder = writeToReport(configManager);
								File logFile = Logger.writeBufferResponse(strBuilder.toString(), UIGetValuesUtility.createLogFolder("MCN" + configManager.getMCN()), "RULES");
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										try {
											StringViewerDialog viewdialog = new StringViewerDialog(logFile);
											viewdialog.setTitle("Transfer Status");
											viewdialog.setSize(600, 400);
											viewdialog.setLocationRelativeTo(null);
											viewdialog.setVisible(true);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
								configDialog.getOkButton().setEnabled(true);
							}
						});
					}
				});
				configDialog.getOkButton().addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								String server = configDialog.getServer();
								File logFile = transferData(configManager, clientSession, loadValues, configDialog.getServer());
								if(logFile != null && server.equals("PRODUCTION")) {
									TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmCoreService, changeObject, PropertyDefines.REL_IMAN_Specification, logFile.getName(), "Transfer Report", "HTML", "IExplore");
									if(newDataset != null ) {
										UIGetValuesUtility.uploadNamedReference(clientSession, newDataset, logFile, "HTML", true, true);
									}
								}
								configDialog.close();
							}
						});
					}
				});
				configDialog.getOkButton().setEnabled(false);
				configDialog.open();
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private StringBuilder writeToReport(ConfigManager configManager) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<html><body>");
		String[] printValues = new String[] { "Model : " + configManager.getModel() + "_" + configManager.getYear(), "MCN :" + configManager.getMCN(), "User : " + configManager.getSession().getUserName(), "Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		Logger.bufferResponse("DETAILS", printValues, strBuilder);
		printValues = new String[] { "S.No", "Rules", "Message", "Type", "Action", "Result" };
		Logger.bufferResponse("HEADER", printValues, strBuilder);
		try {
			int count = 1;
			for (LinkedHashMap<String, String> dataMap : loadValues) {
				String fileName = dataMap.get("FAMILYGROUP")+"_"+dataMap.get("FAMILYID")+"_"+dataMap.get("OPTIONID");
				printValues = new String[] {Integer.toString(count),fileName,"Record will be sent to SAP","M","A","Ok"};
				Logger.bufferResponse("PRINT", printValues, strBuilder);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		strBuilder.append("</table>");
		strBuilder.append("</body></html>");

		return strBuilder;
	}

	protected ArrayList<LinkedHashMap<String,String>> loadDataTransfer(ConfigManager configManager, TCComponentItemRevision changeObject) {

		ArrayList<LinkedHashMap<String,String>> familyOptionValues = null;
		try {
			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {},PropertyDefines.REL_SOL_ITEMS);
			if(solutionItems != null) {
				familyOptionValues = new ArrayList<LinkedHashMap<String,String>>();
				validFamilGroupMap = new LinkedHashMap<String, TCComponent>();
				TCComponentCfg0ConfiguratorPerspective contextPerspective = (TCComponentCfg0ConfiguratorPerspective) configManager.getTopLevelItemRevision().getRelatedComponent(PropertyDefines.CFG_CONFIG_PERSPECTIVE) ;
				TCComponent[] familyGroup = contextPerspective.getRelatedComponents(PropertyDefines.CFG_FAMILY_GROUP);
				if(familyGroup != null) {
					dmCoreService.getProperties(familyGroup, new String[]{PropertyDefines.CFG_OBJECT_ID,PropertyDefines.ITEM_DESC});
					for(TCComponent family : familyGroup) {
						validFamilGroupMap.put(family.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID), family);
					}
				}
				for(int counter = 0 ; counter < solutionItems.length ; counter++){
					TCComponent solItem = solutionItems[counter];
					String solType = solItem.getType();
					if (solType.equals(PropertyDefines.CFG_ITEM_PRODUCTITEM))
					{
						TCComponent[] modelValues = contextPerspective.getRelatedComponents(PropertyDefines.CFG_MODELS);
						TCComponent[] optionValues = contextPerspective.getRelatedComponents(PropertyDefines.CFG_OPTION_VALUE);
						if(modelValues.length !=0) {
							dmCoreService.getProperties(modelValues, new String[]{PropertyDefines.CFG_OBJECT_ID,PropertyDefines.ITEM_DESC, PropertyDefines.CFG_MODEL_FAMILY});
							for(TCComponent model : modelValues) {
								FamilyAndOptions.getModels(dmCoreService, model, familyOptionValues);
							}
						}
						dmCoreService.getProperties(new TCComponent[] {configManager.getTopLevelItemRevision()}, new String[] {PropertyDefines.CFG_OPTION_VALUE});
						if(optionValues.length !=0) {
							dmCoreService.getProperties(optionValues, new String[]{PropertyDefines.CFG_OBJECT_ID,PropertyDefines.ITEM_DESC,PropertyDefines.CFG_OPTION_FAMILY});
							for(TCComponent option : optionValues) {
								FamilyAndOptions.getOptionValues(dmCoreService, option, familyOptionValues);
							}
						}
					}
					else if (solType.equals(PropertyDefines.CFG_ITEM_PRODUCTMODEL))
					{
						FamilyAndOptions.getModels(dmCoreService, solItem, familyOptionValues);
					}
					else if (solType.equals(PropertyDefines.CFG_ITEM_LIT_OPTIONVALUE))
					{
						FamilyAndOptions.getOptionValues(dmCoreService, solItem, familyOptionValues);
					}
					else if (solType.equals(PropertyDefines.CFG_ITEM_PKG_OPTIONVALUE))
					{
						FamilyAndOptions.getOptionValues(dmCoreService, solItem, familyOptionValues);
					}
				}

			}else {
				MessageBox.post("No valid context related objects to transfer to SAP. Please copy valid objects in Solution Items", "Error", MessageBox.ERROR);
			}

		}catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return familyOptionValues;
	}

	protected File transferData(ConfigManager configManager, TCSession session, ArrayList<LinkedHashMap<String,String>> familyOptionValues, String server) {
		SAPURL SAPConnect =  new SAPURL();
		File logFile = null;
		String SERVER_IP = UIGetValuesUtility.SAPSERVERIP(server);
		int count = 1;

		String model = configManager.getModel();
		String year = configManager.getYear();

		String logFolder = UIGetValuesUtility.createLogFolder("MCN"+configManager.getMCN());

		if(familyOptionValues.isEmpty() == false) {

			TCPreferenceService serv = session.getPreferenceService() ;
			String[] prefValues = serv.getStringValues(model+"_"+year+ "_FamilySequence");

			HashMap<String, String> familySeq = new HashMap<String, String>() ;

			if(prefValues != null) {
				for (int inx = 0  ; inx < prefValues.length ; inx ++)
				{
					String[] familyInfo = prefValues[inx].split("=") ;
					familySeq.put(familyInfo[0], familyInfo[1]) ;

				}
			}

			StringBuilder strBuilder =  new StringBuilder();
			strBuilder.append("<html><body>");
			String[] printValues = new String[] {"Model :"+model+"_"+year+"("+configManager.getMaterialCode()+")","MCN :"+configManager.getMCN(),"User :"+session.getUserName(),"Time :"+new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())};
			Logger.bufferResponse("DETAILS", printValues, strBuilder);
			printValues = new String[] {"S.No","Family/Options","Message","Type","Action","Result"};
			Logger.bufferResponse("HEADER", printValues, strBuilder);
			try {
				for(LinkedHashMap<String, String> dataMap : familyOptionValues) {
					dataMap.put("PLATFORM", model);
					dataMap.put("MODELYEAR", year);
					dataMap.put("FAMILYTYPE", "M");
					if (familySeq.get(dataMap.get("FAMILYID"))!= null)
					{
						dataMap.put("FAMILY_SEQ", familySeq.get(dataMap.get("FAMILYID")));
					}else
					{
						dataMap.put("FAMILY_SEQ", "000");
					}

					dataMap.put("MATERIALCODE", configManager.getMaterialCode());
					dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
					String fileName = dataMap.get("FAMILYGROUP")+"_"+dataMap.get("FAMILYID")+"_"+dataMap.get("OPTIONID");

					String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.familyWebserviceURL(SERVER_IP), dataMap,SAPURL.FAM_HEADER, SAPURL.FAM_TAG, SAPURL.FAM_NAMESPACE, "I_"+fileName, logFolder, auth);
					if(message[0].equals("S") || message[1].contains("Record is exist in staging table")){
						printValues = new String[] {Integer.toString(count),fileName,"success",dataMap.get("FAMILYTYPE"),"A","Success"};
						Logger.bufferResponse("PRINT", printValues, strBuilder);
					}else{
						printValues = new String[] {Integer.toString(count),fileName,"success",dataMap.get("FAMILYTYPE"),"A","Success"};
						Logger.bufferResponse("PRINT", printValues, strBuilder);
					}
					count++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			strBuilder.append("</table>");

			String data = Logger.previousTransaction(logFolder, "FAMILYOPTION");

			if(!data.equals("")) {

				strBuilder.append(data);
				strBuilder.append("<br>");
			}

			strBuilder.append("</body></html>");

			logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "FAMILYOPTION");

			//set is latest context transfered to SAP
			try {
				DataManagementService dataManagementService = DataManagementService.getService(session);
				String vehProg = configManager.getTopLevelItemRevision().getProperty(PropertyDefines.CONTEXT_VEHICLE_PROGRAM);


				//1.search other context having same program & set them to old
				LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
				queryInput.put("Is Latest ERP Transferred", "TRUE");
				queryInput.put("Vehicle Program", vehProg);
				TCComponent[] contexts = UIGetValuesUtility.query("Admin - Configurator Context", queryInput, session);
				if(contexts !=null && contexts.length > 0) {
					for(TCComponent context : contexts) {
						UIGetValuesUtility.setProperty(dataManagementService, context, PropertyDefines.LATEST_CONTEXT_TRANSFERED_TO_SAP, "FALSE");
						String id = context.getProperty(PropertyDefines.CONTEXT_CURRENT_ID);
						System.out.println("Context is not latest anymore: " + id);
					}	
				}
				//2.set this one to the latest
				UIGetValuesUtility.setProperty(dataManagementService, configManager.getTopLevelItemRevision(), PropertyDefines.LATEST_CONTEXT_TRANSFERED_TO_SAP, "TRUE");
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post("Error to set property for context. Please contact IT to get support", "Error", MessageBox.ERROR);
			} 

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					try {
						StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(strBuilder.toString(), logFolder, "FAMILYOPTION"));
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

			MessageBox.post("No data to transfer to SAP. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
		}

		return logFile;
	}


	public static boolean isValidFamily(String value) {
		if(validFamilGroupMap.containsKey(value)) {
			return true;
		}else {
			return false;
		}
	}
}
