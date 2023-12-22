package com.vinfast.car.sap.assybom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

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

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.dialogs.BOMOnlyDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.BOMCompareReport;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class AssemblyBOM  extends AbstractHandler {

	UIGetValuesUtility utilities = null;
	String auth = PropertyDefines.SERVER_SAP_VF_AUTH;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		
		String modelName = "";
		
		try {	

			ISelection selection = HandlerUtil.getCurrentSelection( event );

			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];

			TCSession clientSession = changeObject.getSession();
			
			if(changeObject.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {
				MessageBox.post("MCN is not transfer. Please transfer MCN first to do any transfer.", "Error", MessageBox.ERROR);
				return null;
			}

			utilities = new UIGetValuesUtility();

			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);

			if(UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true){

				DataManagementService dmCoreService =  DataManagementService.getService(clientSession);

				dmCoreService.getProperties(new TCComponent[] {changeObject}, new String[] {PropertyDefines.ITEM_ID,PropertyDefines.ECM_PLANT,PropertyDefines.REL_IMPACT_SHOP});

				String MCN = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

				String MCN_ID = MCN.substring(MCN.length() - 8);

				String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);

				AIFComponentContext[] impactedShop =  changeObject.getRelated(PropertyDefines.REL_IMPACT_SHOP);

				if(impactedShop.length == 0 || impactedShop.length > 1){

					MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
					return null;

				}else{

					TCComponentItemRevision obj_Shop = (TCComponentItemRevision)impactedShop[0].getComponent();

					modelName = obj_Shop.getProperty(PropertyDefines.ITEM_NAME);
				}

				BOMOnlyDialog transferDlg = new BOMOnlyDialog(new Shell());

				transferDlg.create();

				transferDlg.setTitle("Assembly BOM Transfer");

				transferDlg.setModel(modelName);

				transferDlg.setPlant(plant);

				transferDlg.setMCN(MCN_ID);

				transferDlg.setServer("PRODUCTION");
				
				transferDlg.setSession(clientSession);
				
				transferDlg.setDataManagmentService(dmCoreService);

				Button transferBtn = transferDlg.getOkButton();

				transferBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event e) {

						transferBtn.setEnabled(false);

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {

								startProcessing(transferDlg, changeObject);

								transferDlg.getShell().dispose();
							}

						});

					}
				});
				transferDlg.open();
			}

		} catch (TCException | NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void startProcessing(BOMOnlyDialog transferDlg, TCComponentItemRevision changeObject) {
		// TODO Auto-generated method stub

		new Logger();
		SAPURL SAPConnect	= new SAPURL();
		BOMCompareReport bomcompare = new BOMCompareReport() ;
		
		TCSession session =  transferDlg.getSession();
		
		DataManagementService dataManagementService = transferDlg.getDataManagmentService();
		
		String SERVER_IP = utilities.MESSERVERIP(transferDlg.getServer());

		try {

			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dataManagementService, changeObject, new String[] {},PropertyDefines.REL_IMPACT_ITEMS);

			if(impactedItems != null) {

				dataManagementService.getProperties(impactedItems, new String[]{PropertyDefines.ITEM_ID, PropertyDefines.REV_TO_SAP, PropertyDefines.ITEM_OBJECT_STR});

				StringBuilder strBuilder =  new StringBuilder();

				String MCN_ID = transferDlg.getMCN();

				String logFolder = UIGetValuesUtility.createLogFolder("MCN"+MCN_ID);

				String MCN_Plant_Code = transferDlg.getPlant();

				int count = 1;
				
				int traversalCount = 1;
				
				int total = impactedItems.length;

				strBuilder.append("<html><body>");

				String[] printValues = new String[] {"Model:"+transferDlg.getModel(),"MCN :"+MCN_ID ,"User:"+session.getUserName(),"Time:"+new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())};
				Logger.bufferResponse("DETAILS", printValues, strBuilder);

				printValues = new String[] {"S.No","Sub Group","Record","BOMLine ID","Message","Action","Result"};
				Logger.bufferResponse("HEADER", printValues, strBuilder);

				boolean isError = false;

				for(TCComponent impacted : impactedItems) {
					
					transferDlg.setProgressStatus(getProgress(traversalCount, total));

					isError = false;

					TCComponentItemRevision revision = (TCComponentItemRevision)impacted;
					
					String revisionObject = revision.getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);
					
					revisionObject = revisionObject.substring(0, revisionObject.indexOf("-"));
					
					String solID = revision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

					String solRev = revision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);

					if(revision.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {

						OpenContextInfo[] output = UIGetValuesUtility.createContextViews(session, revision.getItem());

						TCComponentBOMLine solutionItem =  (TCComponentBOMLine) output[0].context;

						TCComponentItemRevision problemRevision = null;

						int revision_ID = Integer.parseInt(solRev);

						if((revision_ID != 0 || revision_ID != 1) && revision_ID > 1){

							String search_rev = Integer.toString(revision_ID - 1);

							for(int i = search_rev.length() ;i<solRev.length(); i++){

								search_rev = "0"+search_rev;
							}

							problemRevision = UIGetValuesUtility.findRevision(session, solID, search_rev);

							if(problemRevision != null){

								CreateBOMWindowsOutput[] problemWindow = UIGetValuesUtility.createBOMNoRuleWindow(session, problemRevision);

								TCComponentBOMLine problemLine =  problemWindow[0].bomLine;
								TCComponentBOMWindow window =  problemWindow[0].bomWindow;

								boolean result = bomcompare.problemSolutionMap(session, problemLine, solutionItem, count, strBuilder);

								if(result == true) {

									dataManagementService.getProperties(new TCComponent[] {problemRevision}, new String[]{PropertyDefines.ITEM_ID});

									String ID = problemRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

									HashMap<String, TCComponent> problemInputParts = bomcompare.getProblemItems();
									HashMap<String, TCComponent> solutionInputParts = bomcompare.getSolutionItems();

									HashMap<String, TCComponent> add_parts = bomcompare.addPart(solutionInputParts, problemInputParts);
									HashMap<String, TCComponent> change_parts = bomcompare.changePart(solutionInputParts, problemInputParts);
									HashMap<String, TCComponent> del_parts = bomcompare.delPart(solutionInputParts, problemInputParts);

									if((add_parts.isEmpty() && change_parts.isEmpty() && del_parts.isEmpty()) == false) {

										//DELETE
										Set<String> del_keys = del_parts.keySet();

										for(String delLine : del_keys){

											TCComponent del_Part = del_parts.get(delLine);

											HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "D");
											trasferBOMMap.put("PARENTPART", ID);
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) del_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) del_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", del_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											trasferBOMMap.put("LINE", del_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											
											String BOMFile = trasferBOMMap.get("CHILDPART") + "_" + trasferBOMMap.get("BOMLINEID");
											String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), trasferBOMMap,SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_"+trasferBOMMap.get("ACTION")+"_"+ BOMFile, logFolder, PropertyDefines.SERVER_SAP_VF_AUTH);

											if(message.length !=0) {

												if(message[0].equals("E")){

													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Error"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
													isError = true;

												}else{
													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Success"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
												}

												count++;
											}
										}

										//CHANGE

										Set<String> change_keys = change_parts.keySet();

										for(String addLine : change_keys){

											TCComponent change_Part = change_parts.get(addLine);

											HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "C");
											trasferBOMMap.put("PARENTPART", solID);
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) change_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) change_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", change_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											trasferBOMMap.put("LINE", change_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));

											String BOMFile = trasferBOMMap.get("CHILDPART") + "_" + trasferBOMMap.get("BOMLINEID");
											String[]  message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), trasferBOMMap,SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_"+trasferBOMMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
											if(message.length !=0) {

												if(message[0].equals("E")){

													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Error"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
													isError = true;

												}else{
													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Success"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
												}

												count++;
											}
										}

										//ADD

										Set<String> add_keys = add_parts.keySet();

										for(String addLine : add_keys){

											TCComponent add_Part = add_parts.get(addLine);
											HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
											trasferBOMMap.put("MCN", MCN_ID);
											trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
											trasferBOMMap.put("ACTION", "A");
											trasferBOMMap.put("PARENTPART", solID);
											trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Part));
											trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Part));
											trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
											trasferBOMMap.put("CHILDPART", add_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
											trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
											trasferBOMMap.put("LINE", add_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));
											
											String BOMFile = trasferBOMMap.get("CHILDPART") + "_" + trasferBOMMap.get("BOMLINEID");
											String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), trasferBOMMap,SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_"+trasferBOMMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
											if(message.length !=0) {

												if(message[0].equals("E")){

													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Error"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
													isError = true;

												}else{
													printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Success"};
													Logger.bufferResponse("PRINT", printValues, strBuilder);
												}

												count++;
											}

										}

									}else {

										printValues = new String[] {Integer.toString(count),revisionObject,"","","No change in old revision and current revision","","Error"};
										Logger.bufferResponse("PRINT", printValues, strBuilder);
									}

								}

								UIGetValuesUtility.closeWindow(session, window);
							}
						}
						else{

							boolean result = bomcompare.problemSolutionMap(session, null, solutionItem, count, strBuilder);

							if(result == true) {

								HashMap<String, TCComponent> solutionInputParts = bomcompare.getSolutionItems();

								Set<String> add_keys = solutionInputParts.keySet();

								for(String addLine : add_keys){

									TCComponent add_Part = solutionInputParts.get(addLine);
									HashMap<String, String> trasferBOMMap = new HashMap<String, String>();
									trasferBOMMap.put("MCN", MCN_ID);
									trasferBOMMap.put("PLANTCODE", MCN_Plant_Code);
									trasferBOMMap.put("ACTION", "A");
									trasferBOMMap.put("PARENTPART", solID);
									trasferBOMMap.put("BOMLINEID", getFindNo((TCComponentBOMLine) add_Part));
									trasferBOMMap.put("QUANTITY", getQuantity((TCComponentBOMLine) add_Part));
									trasferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
									trasferBOMMap.put("CHILDPART", add_Part.getProperty(PropertyDefines.BOM_ITEM_ID));
									trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
									trasferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
									trasferBOMMap.put("LINE", add_Part.getProperty(PropertyDefines.BOM_DESIGNATOR));

									String BOMFile = trasferBOMMap.get("CHILDPART") + "_" + trasferBOMMap.get("BOMLINEID");
									String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), trasferBOMMap,SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_"+trasferBOMMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
									if(message.length !=0) {

										if(message[0].equals("E")){

											printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Error"};
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											isError = true;

										}else{
											printValues = new String[] {Integer.toString(count),revisionObject,trasferBOMMap.get("CHILDPART"),trasferBOMMap.get("BOMLINEID"),message[1],trasferBOMMap.get("ACTION"),"Success"};
											Logger.bufferResponse("PRINT", printValues, strBuilder);
										}

										count++;
									}

								}
							}
						}

						UIGetValuesUtility.closeContext(session, solutionItem);
						
						if(isError == false && transferDlg.getServer().equals("PRODUCTION")) {
							
							UIGetValuesUtility.setProperty(dataManagementService, revision, PropertyDefines.REV_TO_SAP, "Yes");
						}

					}else {

						printValues = new String[] {Integer.toString(count),revisionObject,"","","Subgroup already transferred to sap","","Info"};
						Logger.bufferResponse("PRINT", printValues, strBuilder);
					}
					
					traversalCount++;
				}
				strBuilder.append("</table>");
				String data = Logger.previousTransaction(logFolder, "BOM");

				if(!data.equals("")) {

					strBuilder.append("<br>");
					strBuilder.append(data);

				}
				strBuilder.append("</body></html>");

				File logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");

				if(isError == false && transferDlg.getServer().equalsIgnoreCase("PRODUCTION")) {

					TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, changeObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");

					if(newDataset != null) {

						UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
					}

				}

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {

						try {
							new Logger();
							StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM"));
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

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getFindNo(TCComponentBOMLine child) {

		String bomLineID = "";
		try {
			bomLineID = child.getProperty("VF4_bomline_id");
			for(int i=bomLineID.length(); i<4 ;i++){
				bomLineID = "0"+bomLineID;
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bomLineID;
	}

	private String getQuantity(TCComponentBOMLine child) {

		String quantity = "";
		try {
			quantity = child.getProperty("bl_quantity");
			if(quantity.equals("")) {
				quantity = "1";
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quantity;
	}
	
	public int getProgress(int current, int total) {

		float value = (float)current/(float)total;

		int percent = (int) (value*100);

		return percent;

	}
}
