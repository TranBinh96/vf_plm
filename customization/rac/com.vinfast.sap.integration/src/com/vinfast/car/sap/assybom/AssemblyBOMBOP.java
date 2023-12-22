package com.vinfast.car.sap.assybom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.bom.FindConnectedLines;
import com.vinfast.sap.bom.ValidateBOMLines;
import com.vinfast.sap.dialogs.BOMBOPDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class AssemblyBOMBOP extends AbstractHandler {

	UIGetValuesUtility utilities = null;
	OrganizationInformationAbstract serverInfo = null;
	String cmd = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection selection = HandlerUtil.getCurrentSelection( event );

		InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

		TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];

		TCSession clientSession = changeObject.getSession();

		utilities	= new UIGetValuesUtility();
		
		cmd = event.getCommand().toString();

		TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);

		if(UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true){

			BOMManager BOMManager = new BOMManager(UIGetValuesUtility.getCompanyCode(event)).loadChangeAttachments(clientSession, changeObject);

			if(BOMManager != null) {

				BOMBOPDialog dialog =  new BOMBOPDialog(new Shell());

				dialog.create();

				dialog.setTitle("Assembly BOM Transfer");

				dialog.setModel(BOMManager.getModel());

				dialog.setYear(BOMManager.getYear());

				dialog.setShop(BOMManager.getShopName());

				dialog.setPlant(BOMManager.getPlant());

				dialog.setServer("PRODUCTION");

				dialog.setTotal(20);

				dialog.setMCN(BOMManager.getMCN());

				Button transferBtn = dialog.getTransferButton();

				transferBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event e) {

						transferBtn.setEnabled(false);

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {

								try {
									startProcessing(BOMManager, dialog, dialog.getServer());
								} catch (TCException e) {
									e.printStackTrace();
								}
								dialog.getShell().dispose();
							}
						});
					}
				});

				dialog.open();
			}else {

				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

		}else {

			MessageBox.post("You are not authorized to transfer MCN.","Please check group/role and try again.", "Access...", MessageBox.ERROR);
			return null;
		}


		return null;
	}

	private void startProcessing(BOMManager BOMManager, BOMBOPDialog dialog, String server) throws TCException {

		boolean isSuccess = false;

		new Logger();

		BOMManager.printReport("APPEND", new String[] {"<html>","<body>"});

		String[] printValues = new String[] {"Model :"+BOMManager.getModel()+"_"+BOMManager.getYear(),"User :"+BOMManager.getSession().getUserName(),"Time :"+new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())};

		BOMManager.printReport("DETAILS", printValues);

		printValues = new String[] {"S.No","Parent","Child","BOMLine ID","Message","Action","Result"};

		BOMManager.printReport("HEADER", printValues);

		HashMap<TCComponent, HashMap<String,BOMBOPData>> currentBOMValues = null;
		HashMap<TCComponent, HashMap<String,BOMBOPData>> currentBOPValues = null;
		HashMap<TCComponent, BOMBOPData> OWPBOPValues = null;

		String loadSubGroupIDs = objectsNotTransferToSAP(BOMManager, dialog, BOMManager.getImpactedItems(), server);

		if(loadSubGroupIDs.equals("") == false) {

			if(BOMManager.isWindowOpen() == false) {

				if(BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING)) {

					currentBOMValues = loadSubGroupChilds(BOMManager, dialog, loadSubGroupIDs);
				}
			}
		}
		dialog.setCounter(6);

		if(BOMManager.hasError() == false) {

			String loadOperationIDs = objectsNotTransferToSAP(BOMManager, dialog, BOMManager.getSolutionItems(), server); //Counter 7

			if(loadOperationIDs.equals("") == false) {

				if(BOMManager.isWindowOpen() == false) {

					if(BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING)) {

						currentBOPValues = loadOperationChilds(BOMManager, dialog, loadOperationIDs);
					}
				}else {

					currentBOPValues = loadOperationChilds(BOMManager, dialog, loadOperationIDs);
				}
			}
		}
		dialog.setCounter(12);

		if(BOMManager.hasError() == false) {

			if(BOMManager.isWindowOpen() == false) {

				if(BOMManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_WORKING)) {

					OWPBOPValues = loadOperationNoPart(BOMManager, dialog, BOMManager.getProblemItems());
				}

			}else {

				OWPBOPValues = loadOperationNoPart(BOMManager, dialog, BOMManager.getProblemItems());
			}

		}
		dialog.setCounter(16);
		
		if(BOMManager.hasError() == false) {

			if((currentBOMValues != null || currentBOPValues != null || OWPBOPValues != null) == true ) {

				isSuccess = transferRecordsToSAP(BOMManager, dialog, currentBOMValues, currentBOPValues, OWPBOPValues);
			}
		}
		dialog.setCounter(20);

		if(BOMManager.isWindowOpen()) {

			BOMManager.closeMBOMBOPWindows();
		}

		BOMManager.printReport("APPEND", new String[] {"</table>"});

		String data = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN"+dialog.getMCN()), "BOM");

		if(!data.equals("")) {

			BOMManager.printReport("APPEND", new String[] {"<br>"});
			BOMManager.printReport("APPEND", new String[] {data});
		}

		BOMManager.printReport("APPEND", new String[] {"</body>","</html>"});

		File logReport = BOMManager.popupReport("BOM");

		if(isSuccess == true && server.equals("PRODUCTION")) {

			TCComponentDataset newDataset = UIGetValuesUtility.createDataset(BOMManager.getDataManagementService(), BOMManager.getChangeObject(), "IMAN_specification", logReport.getName(), "Transfer Report", "HTML", "IExplore");

			if(newDataset != null) {

				UIGetValuesUtility.uploadNamedReference(BOMManager.getSession(), newDataset, logReport, "HTML", true, true);
			}
		}
	}

	private String objectsNotTransferToSAP(BOMManager BOMManager, BOMBOPDialog dialog, TCComponent[] objects,  String server) {

		String searchIds = "";

		try {

			if(objects != null) {

				BOMManager.getDataManagementService().getProperties(objects, new String[] {PropertyDefines.ITEM_ID, PropertyDefines.ITEM_OBJECT_STR,PropertyDefines.REV_TO_SAP});

				for(TCComponent objectRevision : objects) {
					
					String ItemID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

					String impactedRevisionID = objectRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);

					// TODO: concat item id & rev ID to avoid impact fro object_string change
					impactedRevisionID = impactedRevisionID.substring(0, impactedRevisionID.indexOf("-"));

					if(server.equals("PRODUCTION")) {

						if(objectRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {

							if(searchIds.equals("")) {

								searchIds = ItemID;

							}else {

								searchIds =  searchIds +";"+ItemID;
							}

						}else {

							BOMManager.printReport("PRINT", new String[]{Integer.toString(BOMManager.getSerialNo()),impactedRevisionID,"-","-","Already transferred to SAP.","-","Info"});
						}
					}else {

						if(searchIds.equals("")) {

							searchIds = ItemID;

						}else {

							searchIds =  searchIds +";"+ItemID;
						}

					}
				}

			}
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return searchIds;

	}

	private HashMap<TCComponent, HashMap<String, BOMBOPData>> loadSubGroupChilds(BOMManager BOMManager, BOMBOPDialog dialog, String searchObjects) throws TCException {

		HashMap<TCComponent, HashMap<String, BOMBOPData>> traversalDataMap = null;

		TCSession session = BOMManager.getSession();

		TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, searchObjects, BOMManager.getMBOMTraverseLine());


		if(foundObjects != null) {

			HashMap<TCComponent, TCComponent[]> parentChildLines = UIGetValuesUtility.expandBOMOneLevel(session, foundObjects);


			if(!parentChildLines.isEmpty()) {

				new ValidateBOMLines();
				parentChildLines = ValidateBOMLines.assyBOM(BOMManager, parentChildLines);


				if(BOMManager.hasError() == false) {

					if(parentChildLines.isEmpty() == false) {

						HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = new FindConnectedLines().inBOP(BOMManager, parentChildLines, 1);


						if(BOMManager.hasError() == false) {

							if(parentChildDataMap != null) {

								traversalDataMap = new AssyBOMTransfer().loadBOMBOPData(BOMManager, parentChildDataMap);

							}

						}else {

							BOMManager.printReport("APPEND", new String[] {"</table>"});

							BOMManager.printReport("APPEND", new String[] {"</body>","</html>"});

							BOMManager.popupReport("BOM");
						}
					}

				}else {

					BOMManager.printReport("APPEND", new String[] {"</table>"});

					BOMManager.printReport("APPEND", new String[] {"</body>","</html>"});

					BOMManager.popupReport("BOM");

				}
			}
		}

		return traversalDataMap;
	}

	private HashMap<TCComponent, HashMap<String, BOMBOPData>> loadOperationChilds(BOMManager BOMManager, BOMBOPDialog dialog, String operations) throws TCException{

		HashMap<TCComponent, HashMap<String, BOMBOPData>> traversalDataMap = null;

		TCSession session = BOMManager.getSession();

		TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, operations, BOMManager.getBOPTraverseLine());


		if(foundObjects != null) {

			HashMap<TCComponent, TCComponent[]> parentChildLines = UIGetValuesUtility.expandBOPOneLevel(session, foundObjects);


			if(!parentChildLines.isEmpty()) {

				FindConnectedLines findLines =  new FindConnectedLines();

				HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = findLines.inBOM(BOMManager, parentChildLines, 1);


				if(BOMManager.hasError() == false) {

					if(parentChildDataMap != null) {

						HashMap<TCComponent, TCComponent[]> allBOPBOMLines = findLines.getBOMLines();

						new ValidateBOMLines();
						parentChildLines = ValidateBOMLines.assyBOM(BOMManager, allBOPBOMLines);


						if(BOMManager.hasError() == false) {

							traversalDataMap = new AssyBOMTransfer().loadBOMBOPData(BOMManager, parentChildDataMap);

						}else {

							BOMManager.printReport("APPEND", new String[] {"</table>"});

							BOMManager.printReport("APPEND", new String[] {"</body>","</html>"});

							BOMManager.popupReport("BOM");
						}

					}

				}else {

					BOMManager.printReport("APPEND", new String[] {"</table>"});

					BOMManager.printReport("APPEND", new String[] {"</body>","</html>"});

					BOMManager.popupReport("BOM");
				}
			}
		}

		return traversalDataMap;
	}

	private HashMap<TCComponent, BOMBOPData> loadOperationNoPart(BOMManager BOMManager, BOMBOPDialog dialog, TCComponent[] operations){

		HashMap<TCComponent, BOMBOPData> traversalDataMap = new HashMap<TCComponent, BOMBOPData>();

		TCSession session = BOMManager.getSession();

		try{

			if(operations != null) {
				
				operations = UIGetValuesUtility.hasMaterials(BOMManager.getDataManagementService(), operations);
				
				
				if(operations != null) {
					
					CreateBOMWindowsOutput[] BOMWindows = UIGetValuesUtility.createBOMWindow(session, operations);


					for(CreateBOMWindowsOutput window : BOMWindows) {

						TCComponentBOMLine MEOPBOMLine = window.bomLine;
						TCComponentBOMWindow BOMWindow = window.bomWindow;

						if(UIGetValuesUtility.hasMaterials(MEOPBOMLine) == false) {

							BOMBOPData BOPData = new AssyBOMTransfer().loadBOPData(BOMManager, MEOPBOMLine, true);

							if(BOPData != null) {

								traversalDataMap.put(MEOPBOMLine.getItemRevision(), BOPData);
							}
						}
						UIGetValuesUtility.closeWindow(session, BOMWindow);
					}
				}

			}
			dialog.setCounter(14);

			if(!BOMManager.getOperationNoPart().isEmpty()) {

				ArrayList<TCComponentBOMLine> BOPNoPart = BOMManager.getOperationNoPart();

				for(TCComponentBOMLine MEOPBOMLine : BOPNoPart) {

					if(UIGetValuesUtility.hasMaterials(MEOPBOMLine) == false) {

						BOMBOPData BOPData = new AssyBOMTransfer().loadBOPData(BOMManager, MEOPBOMLine, false);
						traversalDataMap.put(MEOPBOMLine.getItemRevision(), BOPData);
					}
				}
			}else {

				// prepare list of subgroup ids for searching & filter subgroup already transferred to SAP
				String operationIDs = objectsNotTransferToSAP(BOMManager, dialog, BOMManager.getSolutionItems(), dialog.getServer());

				if(operationIDs.length() != 0) {

					TCComponent[] foundOperations = UIGetValuesUtility.searchStruture(session, operationIDs, BOMManager.getBOPTraverseLine());

					
					if(foundOperations != null) {

						for(TCComponent MEOPBOM : foundOperations) {

							TCComponentBOMLine MEOPBOMLine =  (TCComponentBOMLine) MEOPBOM;

							if(UIGetValuesUtility.hasMaterials(MEOPBOMLine) == false) {

								BOMBOPData BOPData = new AssyBOMTransfer().loadBOPData(BOMManager, MEOPBOMLine, false);
								traversalDataMap.put(MEOPBOMLine.getItemRevision(), BOPData);
							}
						}
					}
				}
			}

			dialog.setCounter(16);

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return traversalDataMap;
	}

	private boolean transferRecordsToSAP(BOMManager BOMManager,BOMBOPDialog dialog, HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOMLines, HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOPLines, HashMap<TCComponent, BOMBOPData> OWPLines) {

		int count = 1;
		boolean isSuccess = true;
		boolean isTransferred = false;
		File oldBOMFile = null;

		HashMap<String, BOMBOPData> previousBOMStructure = null;

		String serverType = dialog.getServer();

		serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverType, BOMManager.getSession());
		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();
		DataManagementService dataManagementService = BOMManager.getDataManagementService();

		String logFolder = UIGetValuesUtility.createLogFolder("MCN"+dialog.getMCN());

		try {

			TCSession session = BOMManager.getSession();

			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(session);
			
			String dataset_name = dialog.getPlant()+"_BOM_BOP_"+dialog.getShop();

			TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(session, new String[]{"Name","Dataset Type"}, new String[]{dataset_name,"Text"}, "Dataset...");

			if(oldDatasets != null) {

				HashMap<Date, TCComponent> datasetMap =  new HashMap<Date, TCComponent>();

				dataManagementService.getProperties(oldDatasets, new String[] {"creation_date"});

				for(TCComponent dataset : oldDatasets){

					Date date_creation = dataset.getDateProperty("creation_date");
					datasetMap.put(date_creation, dataset);
				}

				ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet()); 

				Collections.sort(sortedKeys, Collections.reverseOrder()); 

				TCComponentDataset olddataset = (TCComponentDataset)datasetMap.get(sortedKeys.get(0));

				oldBOMFile = UIGetValuesUtility.downloadDataset(session,System.getProperty("java.io.tmpdir"), olddataset);

				if(oldBOMFile != null) {

					previousBOMStructure = UIGetValuesUtility.previousAssyBOMStructure(session, oldBOMFile, dialog.getMCN());
				}
			}

			SAPURL SAPConnect	 = new SAPURL();

			if(previousBOMStructure == null) {

				if(currentBOMLines != null) {

					previousBOMStructure = new HashMap<String, BOMBOPData>();

					Set<TCComponent> groupKeys = currentBOMLines.keySet();
					boolean hasSubGroupError  = false;

					for(TCComponent groupKey : groupKeys) {

						hasSubGroupError  = false;
						HashMap<String, BOMBOPData> latestSubGroupStructure = currentBOMLines.get(groupKey);

						if(!latestSubGroupStructure.isEmpty()) {

							Set<String> addedParts = latestSubGroupStructure.keySet();

							if(addedParts.isEmpty() == false){

								Iterator<String> iterator = addedParts.iterator();

								while(iterator.hasNext()) {

									String addRecord = iterator.next();
									BOMBOPData BOMBOPData = latestSubGroupStructure.get(addRecord);
									HashMap<String, String> BOMDataMap = BOMBOPData.getASSYBOM("A");
									String ID = BOMBOPData.getChildPart()+"_"+BOMBOPData.getBOMLineID();
									
									String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), BOMDataMap,SAPURL.ASSY_BOM_HEADER,SAPURL.ASSY_BOM_TAG,SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_A_"+count+"_"+ID, logFolder, auth);
									if(message[0].equals("S")){

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);

										HashMap<String, String> BOPDataMap = BOMBOPData.getASSYBOP("A");
										BOPDataMap.put("MCN", dialog.getMCN());
										
										String BOPID = BOMBOPData.getBOPID()+"_"+BOMBOPData.getBOMLineID();
										String msg[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPDataMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_A_"+count+"_"+BOPID, logFolder, auth);
										isTransferred = true;
										if(msg[0].equals("S")){

											printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getBOPID(),BOMBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Success"};
											BOMManager.printReport("PRINT", printValues);
											previousBOMStructure.put(addRecord, BOMBOPData);
											isTransferred = true;
										}else{

											printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getBOPID(),BOMBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Error"};
											BOMManager.printReport("PRINT", printValues);
											isSuccess = false;
											hasSubGroupError  = true;
										}

									}else{

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasSubGroupError  = true;
									}
									count++;
								}
							}

							if(hasSubGroupError == false && serverType.equals("PRODUCTION") && UIGetValuesUtility.checkAccess(session, groupMember, groupKey)) {

								UIGetValuesUtility.setProperty(dataManagementService, groupKey, PropertyDefines.REV_TO_SAP, "Yes");
							}
						}
					}
				}


			}else {

				if(currentBOMLines != null) {

					Set<TCComponent> subGroupkeys = currentBOMLines.keySet();

					for(TCComponent subGroup : subGroupkeys) {

						boolean hasSubGroupError  = false;

						String subGroupFetchKey = subGroup.getProperty(PropertyDefines.ITEM_ID);

						HashMap<String, BOMBOPData> previousSubGroupStructure = utilities.getSubGroupLines(subGroupFetchKey, previousBOMStructure);

						HashMap<String, BOMBOPData> latestSubGroupStructure = currentBOMLines.get(subGroup);

						if(latestSubGroupStructure.isEmpty()) {

							Set<String> deletedParts = previousSubGroupStructure.keySet();

							if(deletedParts.isEmpty() == false){

								Iterator<String> iterator = deletedParts.iterator();

								while(iterator.hasNext()) {

									String deleteRecord = iterator.next();
									BOMBOPData BOMBOPData = previousSubGroupStructure.get(deleteRecord);
									BOMBOPData.setSequenceTime(UIGetValuesUtility.getSequenceID());
									HashMap<String, String> BOMDataMap = BOMBOPData.getASSYBOM("D");
									String ID = BOMBOPData.getChildPart()+"_"+BOMBOPData.getBOMLineID();
									String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), BOMDataMap,SAPURL.ASSY_BOM_HEADER,SAPURL.ASSY_BOM_TAG,SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_D_"+count+"_"+ID, logFolder, auth);
									if(message[0].equals("S")){

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);
										previousBOMStructure.remove(deleteRecord);
										isTransferred = true;
									}else{

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasSubGroupError  = true;
									}
									count++;
								}

								if(hasSubGroupError == false && serverType.equals("PRODUCTION") && UIGetValuesUtility.checkAccess(session, groupMember, subGroup)) {

									UIGetValuesUtility.setProperty(dataManagementService, subGroup, PropertyDefines.REV_TO_SAP, "Yes");
								}
							}

						}else {

							Set<String> deletedParts = utilities.deletedLines(previousSubGroupStructure, latestSubGroupStructure);

							if(deletedParts.isEmpty() == false){

								Iterator<String> iterator = deletedParts.iterator();

								while(iterator.hasNext()) {

									String deleteRecord = iterator.next();
									BOMBOPData BOMBOPData = previousSubGroupStructure.get(deleteRecord);
									BOMBOPData.setSequenceTime(UIGetValuesUtility.getSequenceID());
									HashMap<String, String> BOMDataMap = BOMBOPData.getASSYBOM("D");
									String ID = BOMBOPData.getChildPart()+"_"+BOMBOPData.getBOMLineID();
									String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), BOMDataMap,SAPURL.ASSY_BOM_HEADER,SAPURL.ASSY_BOM_TAG,SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_D_"+count+"_"+ID, logFolder, auth);
									if(message[0].equals("S")){

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);
										previousBOMStructure.remove(deleteRecord);
										isTransferred = true;
									}else{

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasSubGroupError  = true;
									}
									count++;
								}
							}

							Set<String> addedParts = utilities.addedLines(previousSubGroupStructure, latestSubGroupStructure);

							if(addedParts.isEmpty() == false){

								Iterator<String> iterator = addedParts.iterator();

								while(iterator.hasNext()) {

									String addRecord = iterator.next();
									BOMBOPData BOMBOPData = latestSubGroupStructure.get(addRecord);
									HashMap<String, String> BOMDataMap = BOMBOPData.getASSYBOM("A");
									String ID = BOMBOPData.getChildPart()+"_"+BOMBOPData.getBOMLineID();
									String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), BOMDataMap,SAPURL.ASSY_BOM_HEADER,SAPURL.ASSY_BOM_TAG,SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_A_"+count+"_"+ID, logFolder, auth);
									if(message[0].equals("S")){

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);

										HashMap<String, String> BOPDataMap = BOMBOPData.getASSYBOP("A");
										BOPDataMap.put("MCN", dialog.getMCN());
										String BOPID = BOMBOPData.getBOPID()+"_"+BOMBOPData.getBOMLineID();
										String msg[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPDataMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_A_"+count+"_"+BOPID, logFolder, auth);
										isTransferred = true;
										if(msg[0].equals("S")){

											printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getBOPID(),BOMBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Success"};
											BOMManager.printReport("PRINT", printValues);
											previousBOMStructure.put(addRecord, BOMBOPData);
											isTransferred = true;
										}else{

											printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getBOPID(),BOMBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Error"};
											BOMManager.printReport("PRINT", printValues);
											isSuccess = false;
											hasSubGroupError  = true;
										}

									}else{

										String[] printValues = new String[]{Integer.toString(count),BOMBOPData.getParentPart(),BOMBOPData.getChildPart(),BOMBOPData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasSubGroupError  = true;
									}
									count++;
								}
							}

							HashMap<String,BOMBOPData> commonBOMPartsMap = utilities.commonBOMLines(previousSubGroupStructure, latestSubGroupStructure, PropertyDefines.TRANSFER_MODE_ASSY);

							Set<String> commonBOMParts = commonBOMPartsMap.keySet();

							if(commonBOMParts.isEmpty() == false){

								Iterator<String> iterator = commonBOMParts.iterator();

								while(iterator.hasNext()) {

									String changeRecord = iterator.next();
									BOMBOPData BOMData = latestSubGroupStructure.get(changeRecord);
									HashMap<String, String> BOMDataMap = BOMData.getASSYBOM("C");
									String ID = BOMData.getChildPart()+"_"+BOMData.getBOMLineID();
									String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), BOMDataMap,SAPURL.ASSY_BOM_HEADER,SAPURL.ASSY_BOM_TAG,SAPURL.ASSY_BOM_NAMESPACE,"I_BOM_D_"+count+"_"+ID, logFolder, auth);
									if(message[0].equals("S")){

										String[] printValues = new String[]{Integer.toString(count),BOMData.getParentPart(),BOMData.getChildPart(),BOMData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);
										BOMBOPData ChangeData = previousBOMStructure.get(changeRecord);
										ChangeData.setQuanity(BOMData.getQuanity());
										ChangeData.setFormula(BOMData.getFormula());
										previousBOMStructure.put(changeRecord,ChangeData);
										isTransferred = true;
										
									}else{

										String[] printValues = new String[]{Integer.toString(count),BOMData.getParentPart(),BOMData.getChildPart(),BOMData.getBOMLineID(),message[1],BOMDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasSubGroupError  = true;
									}
									count++;
								}
							}

							if(hasSubGroupError == false && serverType.equals("PRODUCTION") && UIGetValuesUtility.checkAccess(session, groupMember, subGroup)) {

								UIGetValuesUtility.setProperty(dataManagementService, subGroup, PropertyDefines.REV_TO_SAP, "Yes");
							}
						}
					}
				}
			}

			if(currentBOPLines != null) {

				Set<TCComponent> currentBOPKeys = currentBOPLines.keySet();

				HashMap<String,BOMBOPData> previousBOPLines = new HashMap<String,BOMBOPData>();

				for(TCComponent operationKey : currentBOPKeys) {

					boolean hasOperationError = false;

					HashMap<String, BOMBOPData> currentBOPLine = currentBOPLines.get(operationKey);

					if(currentBOPLine != null) {
						for(String BOPKey : currentBOPLine.keySet()) {
	
							BOMBOPData BOMData = previousBOMStructure.get(BOPKey);
							previousBOPLines.put(BOPKey, BOMData);
						}
					}

					if(previousBOPLines.isEmpty() == false) {

						HashMap<String, BOMBOPData> changeInWorkStationsMap = utilities.changeInWorkStation(previousBOPLines, currentBOPLine, PropertyDefines.TRANSFER_MODE_ASSY);

						if(changeInWorkStationsMap.isEmpty() == false){

							Set<String> differentWorkStationKeys = changeInWorkStationsMap.keySet();

							Iterator<String> iterator = differentWorkStationKeys.iterator();

							while(iterator.hasNext()) {

								String changeWSRecord = iterator.next();
								BOMBOPData previousBOPData = previousBOPLines.get(changeWSRecord);
								previousBOPData.setSequenceTime(UIGetValuesUtility.getSequenceID());
								HashMap<String, String> BOPDataMap = previousBOPData.getASSYBOP("D");
								BOPDataMap.put("MCN", dialog.getMCN());
								String BOPID = previousBOPData.getBOPID()+"_"+previousBOPData.getBOMLineID();
								String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPDataMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_D_"+count+"_"+BOPID, logFolder, auth);
								if(message[0].equals("S")){

									String[] printValues = new String[]{Integer.toString(count),previousBOPData.getParentPart(),previousBOPData.getBOPID(),previousBOPData.getBOMLineID(),message[1],BOPDataMap.get("ACTION"),"Success"};
									BOMManager.printReport("PRINT", printValues);

									BOMBOPData newBOPData = currentBOPLine.get(changeWSRecord);
									BOPDataMap = newBOPData.getASSYBOP("A");
									BOPDataMap.put("MCN", dialog.getMCN());
									BOPID = newBOPData.getBOPID()+"_"+newBOPData.getBOMLineID();
									String msg[] = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPDataMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_A_"+count+"_"+BOPID, logFolder, auth);
									isTransferred = true;

									if(msg[0].equals("S")){

										printValues = new String[]{Integer.toString(count),previousBOPData.getParentPart(),previousBOPData.getBOPID(),previousBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Success"};
										BOMManager.printReport("PRINT", printValues);
										previousBOMStructure.replace(changeWSRecord, newBOPData);
										isTransferred = true;

									}else{

										printValues = new String[]{Integer.toString(count),previousBOPData.getParentPart(),previousBOPData.getBOPID(),previousBOPData.getBOMLineID(),msg[1],BOPDataMap.get("ACTION"),"Error"};
										BOMManager.printReport("PRINT", printValues);
										isSuccess = false;
										hasOperationError = true;
									}

								}else{
									String[] printValues = new String[]{Integer.toString(count),previousBOPData.getParentPart(),previousBOPData.getBOPID(),previousBOPData.getBOMLineID(),message[1],BOPDataMap.get("ACTION"),"Error"};
									BOMManager.printReport("PRINT", printValues);
									isSuccess = false;
									hasOperationError = true;
								}
								count++;
							}
						}

						HashMap<String, BOMBOPData> differentOperationRevision = utilities.changeInOperationRevision(previousBOPLines, currentBOPLine);

						Set<String> differentOperationRevisionKeys = differentOperationRevision.keySet();

						if(differentOperationRevisionKeys.isEmpty() == false){

							Iterator<String> iterator = differentOperationRevisionKeys.iterator();

							while(iterator.hasNext()) {

								String changeRecord = iterator.next();
								BOMBOPData newBOPData = currentBOPLine.get(changeRecord);
								HashMap<String, String> BOPDataMap = newBOPData.getASSYBOP("C");
								BOPDataMap.put("MCN", dialog.getMCN());
								String BOPID = newBOPData.getBOPID()+"_"+newBOPData.getBOMLineID();
								String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPDataMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_C_"+count+"_"+BOPID, logFolder, auth);

								if(message[0].equals("S")){

									String[] printValues = new String[]{Integer.toString(count),newBOPData.getParentPart(),newBOPData.getBOPID(),newBOPData.getBOMLineID(),message[1],BOPDataMap.get("ACTION"),"Success"};
									BOMManager.printReport("PRINT", printValues);
									BOMBOPData previousBOPData = previousBOPLines.get(changeRecord);
									previousBOPData.setBOPRevision(newBOPData.getBOPRevision());
									previousBOMStructure.replace(changeRecord, previousBOPData);
									isTransferred = true;

								}else{

									String[] printValues = new String[]{Integer.toString(count),newBOPData.getParentPart(),newBOPData.getBOPID(),newBOPData.getBOMLineID(),message[1],BOPDataMap.get("ACTION"),"Error"};
									BOMManager.printReport("PRINT", printValues);
									isSuccess = false;
									hasOperationError = true;
								}
								count++;
							}
						}

						if(hasOperationError == false && serverType.equals("PRODUCTION") && UIGetValuesUtility.checkAccess(session, groupMember, operationKey))
							UIGetValuesUtility.setProperty(dataManagementService, operationKey, PropertyDefines.REV_TO_SAP, "Yes");
					}
				}
			}

			if(!OWPLines.isEmpty()) {

				for(TCComponent OWPLine : OWPLines.keySet()) {
					boolean hasOperationError = false;
					BOMBOPData BOPDataMap = OWPLines.get(OWPLine);
					TCComponentItemRevision MEOPRevison = (TCComponentItemRevision) OWPLine;
					HashMap<String, String> BOPMap = BOPDataMap.getASSYBOP(BOPDataMap.getAction());
					String BOPID = BOPDataMap.getBOPID()+"_"+BOPDataMap.getBOPRevision();
					BOPMap.put("MCN", dialog.getMCN());
					String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOPMap,SAPURL.ASSY_BOP_HEADER,SAPURL.ASSY_BOP_TAG,SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOPDataMap.getAction()+"_"+count+"_"+BOPID, logFolder, auth);
					if(message[0].equals("S")){

						String[] printValues = new String[]{Integer.toString(count),"OWP",BOPDataMap.getBOPID()+"/"+BOPDataMap.getBOPRevision(),"",message[1],BOPDataMap.getAction(),"Success"};
						BOMManager.printReport("PRINT", printValues);
						isTransferred = true;

					}else{

						String[] printValues = new String[]{Integer.toString(count),"OWP",BOPDataMap.getBOPID()+"/"+BOPDataMap.getBOPRevision(),"",message[1],BOPDataMap.getAction(),"Error"};
						BOMManager.printReport("PRINT", printValues);
						isSuccess = false;
						hasOperationError = true;
					}
					count++;

					if(hasOperationError == false && serverType.equals("PRODUCTION") && UIGetValuesUtility.checkAccess(session, groupMember, MEOPRevison)) {

						UIGetValuesUtility.setProperty(dataManagementService, MEOPRevison, PropertyDefines.REV_TO_SAP, BOPDataMap.getWorkStation());
					}
				}
			}

			if(isTransferred && serverType.equals("PRODUCTION")) {

				utilities.uploadAssyBOMStructure(session, oldBOMFile, previousBOMStructure);

				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dataManagementService, null, null, dataset_name, dataset_name, "Text", "TextEditor");

				if(newDataset == null){

					MessageBox.post("Failed to create report. Please contact Teamcenter Administrator.","Error", MessageBox.ERROR);
					return false;

				}else {

					UIGetValuesUtility.uploadNamedReference(session, newDataset, oldBOMFile, "Text", true, true);
				}
			}

		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isSuccess;
	}
}
