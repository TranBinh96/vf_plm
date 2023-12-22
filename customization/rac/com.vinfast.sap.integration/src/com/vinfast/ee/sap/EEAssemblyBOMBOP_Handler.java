//impactedShoppackage com.vinfast.ee.sap;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import javax.swing.SwingUtilities;
//
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Shell;
//
//import com.teamcenter.integration.ulti.TCExtension;
//import com.teamcenter.rac.aif.AbstractAIFUIApplication;
//import com.teamcenter.rac.aif.kernel.AIFComponentContext;
//import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
//import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.kernel.TCComponent;
//import com.teamcenter.rac.kernel.TCComponentBOMLine;
//import com.teamcenter.rac.kernel.TCComponentBOMWindow;
//import com.teamcenter.rac.kernel.TCComponentDataset;
//import com.teamcenter.rac.kernel.TCComponentForm;
//import com.teamcenter.rac.kernel.TCComponentItem;
//import com.teamcenter.rac.kernel.TCComponentItemRevision;
//import com.teamcenter.rac.kernel.TCException;
//import com.teamcenter.rac.kernel.TCSession;
//import com.teamcenter.rac.util.MessageBox;
//import com.teamcenter.rac.util.StringViewerDialog;
//import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
//import com.teamcenter.services.rac.core.DataManagementService;
//import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
//import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
//import com.teamcenter.soa.exceptions.NotLoadedException;
//import com.vinfast.integration.model.OrganizationInformationAbstract;
//import com.vinfast.integration.model.OrganizationInformationFactory;
//import com.vinfast.sap.services.CreateSoapHttpRequest;
//import com.vinfast.sap.services.Logger;
//import com.vinfast.sap.util.BOMCompareReport;
//import com.vinfast.sap.util.PropertyDefines;
//import com.vinfast.sap.util.UIGetValuesUtility;
//import com.vinfast.sap.variants.MaterialPlatformCode;
//import com.vinfast.scooter.sap.assembly.OperationsWorkStations;
//import com.vinfast.url.SAPURL;
//
//public class EEAssemblyBOMBOP_Handler extends AbstractHandler {
//	private TCSession session;
//	private OrganizationInformationAbstract serverInfo = null;
//	private TCComponentItemRevision selectedObject = null;
//	private DataManagementService dmService = null;
//	private EEAssemblyBOMBOP_Dialog dlg;
//	private TCComponentItemRevision obj_shopTopNode = null;
//	private TCComponentItemRevision obj_Shop = null;
//
//	File logFile;
//
//	public EEAssemblyBOMBOP_Handler() {
//		super();
//	}
//
//	@Override
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//		try {
//			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
//
//			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
//			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
//			if (validObjectSelect(targetComponents)) {
//				MessageBox.post("Please Select 1 MCN Revision.", "Error", MessageBox.ERROR);
//				return null;
//			}
//
//			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
//				MessageBox.post("You are not have permission for this MCN.", "Error", MessageBox.ERROR);
//				return null;
//			}
//
//			dmService = DataManagementService.getService(session);
//			AIFComponentContext[] impactedShopItems = selectedObject.getRelated("Vf6_impacted_shop");
//			if (impactedShopItems == null || impactedShopItems.length != 1) {
//				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
//				return null;
//			}
//
//			String plant = selectedObject.getPropertyDisplayableValue("vf6_sap_plant");
//			String MCN_SAPID = selectedObject.getPropertyDisplayableValue("item_id");
//			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
//
//			obj_Shop = (TCComponentItemRevision) impactedShopItems[0].getComponent();
//			obj_shopTopNode = UIGetValuesUtility.getTopLevelItemRevision(session, obj_Shop,
//					PropertyDefines.RULE_WORKING);
//
//			String err = UIGetValuesUtility.checkValidShop(obj_Shop, plant);
//			if (!err.isEmpty()) {
//				MessageBox.post(err, "Error", MessageBox.ERROR);
//				return null;
//			}
//
//			String mainGroup = obj_Shop.getProperty("object_name");
//			String platform = "";
//			String modelYear = "";
//
//			if (obj_shopTopNode != null) {
//				MaterialPlatformCode platformCode = UIGetValuesUtility.getPlatformCode(dmService, obj_shopTopNode);
//				if ((platformCode.getPlatformCode().equals("") || platformCode.getModelYear().equals(""))) {
//					platform = obj_shopTopNode.getPropertyDisplayableValue("item_id");
//					modelYear = obj_shopTopNode.getPropertyDisplayableValue("object_name");
//				} else {
//					platform = platformCode.getPlatformCode();
//					modelYear = platformCode.getModelYear();
//				}
//			} else {
//				obj_shopTopNode = obj_Shop;
//			}
//
//			dlg = new EEAssemblyBOMBOP_Dialog(new Shell());
//			dlg.create();
//
//			dlg.setTitle("VFE Assembly BOM/BOP");
//			dlg.txtModel.setText(platform);
//			dlg.txtYear.setText(modelYear);
//			dlg.txtShop.setText(mainGroup);
//			dlg.txtPlant.setText(plant);
//			dlg.txtMCN.setText(MCN);
//			dlg.cbServer.setText("PRODUCTION");
//
//			dlg.btnSave.addListener(SWT.Selection, new Listener() {
//				@Override
//				public void handleEvent(Event e) {
//					dlg.btnSave.setEnabled(false);
//					String serverType = dlg.cbServer.getText();
//					serverInfo = OrganizationInformationFactory.generateOrganizationInformation("vfe", serverType,
//							session);
//					Display.getDefault().asyncExec(new Runnable() {
//						@Override
//						public void run() {
//							loadTransferRecords();
//							dlg.getShell().dispose();
//						}
//					});
//				}
//			});
//
//			dlg.open();
//		} catch (TCException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	private HashMap<String, String> getTransferBOMMapItem(String mcn, String plantCode, String action,
//			String parentPart, String bomlineID, String quantity, String childPart) {
//		HashMap<String, String> transferBOMMap = new HashMap<String, String>();
//		transferBOMMap.put("MCN", mcn);
//		transferBOMMap.put("PLANTCODE", plantCode);
//		transferBOMMap.put("ACTION", action);
//		transferBOMMap.put("PARENTPART", parentPart);
//		transferBOMMap.put("BOMLINEID", bomlineID);
//		transferBOMMap.put("QUANTITY", quantity);
//		transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
//		transferBOMMap.put("CHILDPART", childPart);
//		return transferBOMMap;
//	}
//
//	public void loadTransferRecords() {
//		// Get data from dialog
//		String modelValue = dlg.txtModel.getText();
//		String yearValue = dlg.txtYear.getText();
//		String plantValue = dlg.txtPlant.getText();
//		String shopValue = dlg.txtShop.getText();
//		String mcnValue = dlg.txtMCN.getText();
//		String serverValue = dlg.cbServer.getText();
//
//		// ------------------
//		BOMCompareReport bomcompare = new BOMCompareReport();
//		OperationsWorkStations opWs = new OperationsWorkStations();
//		new Logger();
//		SAPURL SAPConnect = new SAPURL();
//
//		String auth = serverInfo.getAuth();
//		String serverIP = serverInfo.getServerIP();
//
//		try {
//			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnValue);
//
//			TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject,
//					new String[] { "MEOPRevision" }, "CMHasProblemItem");
//			int problemCount = 0;
//			if (problemItems != null)
//				problemCount = problemItems.length;
//
//			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject,
//					new String[] { "MEOPRevision" }, "EC_solution_item_rel");
//			int solutionCount = 0;
//			if (solutionItems != null)
//				solutionCount = solutionItems.length;
//
//			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmService, selectedObject,
//					new String[] {}, "CMHasImpactedItem");
//			if (impactedItems == null || impactedItems.length == 0) {
//				MessageBox.post("No \"Impacted Items\" to transfer to SAP.", "Error", MessageBox.ERROR);
//				return;
//			}
//
//			TCComponent BOMLine = null;
//			TCComponent BOPLine = null;
//			TCComponent traverseStructure = null;
//			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session,
//					obj_shopTopNode.getItem());
//
//			for (OpenContextInfo views : createdBOMViews) {
//				if (views.context.getType().equals("BOMLine"))
//					BOMLine = views.context;
//				if (views.context.getType().equals("Mfg0BvrPlantBOP"))
//					BOPLine = views.context;
//			}
//
//			TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem()
//					.getRelatedComponent("IMAN_master_form");
//
//			String searchPlantModel = topLineMasterForm.getProperty("user_data_2");
//			if (searchPlantModel.length() != 0) {
//				ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(session,
//						new String[] { searchPlantModel }, BOPLine);
//				if (!plantModelList.isEmpty()) {
//					traverseStructure = plantModelList.get(0);
//					UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
//				} else {
//					TCComponentItem plantModel = UIGetValuesUtility.findItem(session, searchPlantModel);
//					OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
//					BOPLine = createdBOPView[0].context;
//					UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
//					traverseStructure = BOPLine;
//				}
//			} else {
//				UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
//				traverseStructure = BOPLine;
//			}
//
//			String impactShopID = obj_Shop.getProperty("item_id");
//			ArrayList<TCComponent> searchLines = UIGetValuesUtility.searchPartsInStruture(session,
//					new String[] { impactShopID }, BOMLine);
//
//			if (searchLines == null || searchLines.size() == 0) {
//				MessageBox.post("Impacted Shop not found in MBOM. Please contact PLM Administrator.", "Error",
//						MessageBox.ERROR);
//				return;
//			}
//
//			int count = 1;
//			int finishedTask = 1;
//			int totalTask = impactedItems.length + problemCount + solutionCount;
//
//			StringBuilder strBuilder = new StringBuilder();
//			String[] printValues;
//			strBuilder.append("<html><body>");
//
//			Logger.bufferResponse("DETAILS", new String[] { "Model :" + modelValue + "_" + yearValue, "Shop :" + shopValue,
//					"User :" + session.getUserName(),
//					"Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) },
//					strBuilder);
//
//			Logger.bufferResponse("HEADER",
//					new String[] { "S.No", "Sub Group", "Record", "BOMLine ID", "Message", "Action", "Result" },
//					strBuilder);
//
//			LinkedHashMap<String, TCComponentItemRevision> impactedItemsList = new LinkedHashMap<String, TCComponentItemRevision>();
//			for (TCComponent impactedItem : impactedItems) {
//				TCComponentItemRevision impactedRev = (TCComponentItemRevision) impactedItem;
//				impactedItemsList.put(impactedItem.getPropertyDisplayableValue("item_id"), impactedRev);
//			}
//
//			TCComponent shop = searchLines.get(0);
//			ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(session,
//					impactedItemsList.keySet().toArray(new String[0]), shop);
//
//			if (impactLines != null) {
//				for (TCComponent item : impactLines) {
//					dlg.setProgressStatus(finishedTask, totalTask);
//					TCComponentBOMLine solutionItem = (TCComponentBOMLine) item;
//					String itemID = solutionItem.getPropertyDisplayableValue("bl_item_item_id");
//					String itemRevID = solutionItem.getPropertyDisplayableValue("bl_rev_item_revision_id");
//					TCComponentItemRevision solution_Rev = impactedItemsList.get(itemID);
//					impactedItemsList.remove(itemID);
//					if (solution_Rev.getProperty("vf3_transfer_to_sap").trim().isEmpty()) {
//						TCComponentItemRevision problemItemRevision = null;
//						int revID = Integer.parseInt(itemRevID);
//						if ((revID != 0 || revID != 1) && revID > 1) {
//							String search_rev = Integer.toString(revID - 1);
//							for (int i = search_rev.length(); i < itemRevID.length(); i++) {
//								search_rev = "0" + search_rev;
//							}
//							problemItemRevision = solution_Rev.findRevision(search_rev);
//							if (problemItemRevision != null) {
//								CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session,
//										problemItemRevision);
//								TCComponentBOMLine problemItem = output[0].bomLine;
//								boolean IOValues = bomcompare.inputOutputMap(session, problemItem, solutionItem, count,
//										strBuilder);
//								System.out.println(strBuilder.toString());
//								if (IOValues == true) {
//									boolean isError = false;
//									HashMap<String, TCComponent> prob_P_Output = bomcompare.getProblemOutputItems();
//									HashMap<String, TCComponent> sol_P_Output = bomcompare.getSolutionOutputItems();
//									HashMap<String, TCComponent> prob_C_Input = bomcompare.getProblemInputItems();
//									HashMap<String, TCComponent> sol_C_Input = bomcompare.getSolutionInputItems();
//									// Parent
//									HashMap<String, TCComponent> add_Output_parts = bomcompare.addPart(sol_P_Output, prob_P_Output);
//									HashMap<String, TCComponent> del_Output_parts = bomcompare.delPart(sol_P_Output, prob_P_Output);
//									// Child
//									HashMap<String, TCComponent> add_Input_parts = bomcompare.addPart(sol_C_Input, prob_C_Input);
//									HashMap<String, TCComponent> del_Input_parts = bomcompare.delPart(sol_C_Input, prob_C_Input);
//
//									if ((add_Output_parts.isEmpty() && del_Output_parts.isEmpty() && add_Input_parts.isEmpty() && del_Input_parts.isEmpty()) == false) {
//										HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = new HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
//
//										// DELETE (PROBLEM ITEMS)
//										for (Map.Entry<String, TCComponent> delInputLine : del_Input_parts.entrySet()) {
//											TCComponentBOMLine del_Input_Part = (TCComponentBOMLine) delInputLine.getValue();
//											for (Map.Entry<String, TCComponent> delOutputLine : prob_P_Output.entrySet()) {
//												TCComponentBOMLine del_Output_Part = (TCComponentBOMLine) delOutputLine.getValue();
//												HashMap<String, String> transferBOMMap = getTransferBOMMapItem(mcnValue,
//														plantValue, "D", del_Output_Part.getProperty("bl_item_item_id"),
//														getFindNo(del_Input_Part),
//														getQuantity(del_Input_Part),
//														del_Input_Part.getProperty("bl_item_item_id"));
//												
//												UIGetValuesUtility.updateNodeForSubtitutePart(del_Input_Part, transferBOMMap);
//												transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
//											}
//											prob_C_Input.remove(delInputLine.getKey());
//										}
//										
//										for (Map.Entry<String, TCComponent> delOutputLine : del_Output_parts.entrySet()) {
//											TCComponentBOMLine del_Output_Part = (TCComponentBOMLine) delOutputLine.getValue();
//											for (Map.Entry<String, TCComponent> delInputLine : prob_C_Input.entrySet()) {
//												TCComponentBOMLine del_Input_Part = (TCComponentBOMLine) delInputLine.getValue();
//												HashMap<String, String> transferBOMMap = getTransferBOMMapItem(mcnValue,
//														plantValue, "D", del_Output_Part.getProperty("bl_item_item_id"),
//														getFindNo(del_Input_Part),
//														getQuantity(del_Input_Part),
//														del_Input_Part.getProperty("bl_item_item_id"));
//												
//												UIGetValuesUtility.updateNodeForSubtitutePart(del_Input_Part, transferBOMMap);
//												transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
//											}
//										}
//										// ADD
//										if(add_Input_parts != null && add_Input_parts.size() > 0) {
//											
//										}
//										Set<String> add_Input_keys = add_Input_parts.keySet();
//										if (add_Input_keys.size() != 0) {
//											TCComponentBOMLine[] inputLines = new TCComponentBOMLine[add_Input_keys
//													.size()];
//											int iterator = 0;
//											for (String addLine : add_Input_keys) {
//												inputLines[iterator] = (TCComponentBOMLine) add_Input_parts
//														.get(addLine);
//												iterator++;
//											}
//
//											NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOPScooter(session,
//													inputLines, (TCComponentBOMLine) traverseStructure, itemID, count,
//													strBuilder);
//
//											if (bopNode != null) {
//												for (NodeInfo addLine : bopNode) {
//													ArrayList<String> wsList = null;
//													String ID = "";
//													TCComponentBOMLine operationBOMLine = null;
//													TCComponent inputBOMLine = addLine.originalNode;
//													TCComponent[] inputBOPLine = addLine.foundNodes;
//
//													String[] values = inputBOMLine.getProperties(new String[] { "bl_item_item_id", "VF4_bomline_id", "VF4_manuf_code" });
//													String key = values[0] + "~" + values[1] + "~" + values[2];
//													for (TCComponent foundLine : inputBOPLine) {
//														TCComponentBOMLine bopLine = (TCComponentBOMLine) foundLine;
//														operationBOMLine = (TCComponentBOMLine) bopLine
//																.getReferenceProperty("bl_parent");
//														TCComponentItemRevision operationRevision = operationBOMLine
//																.getItemRevision();
//														if (operationRevision.getDisplayType()
//																.equalsIgnoreCase("Operation Revision")) {
//															dmService.getProperties(
//																	new TCComponent[] { operationBOMLine },
//																	new String[] { "vf4_operation_type",
//																			"bl_rev_item_revision_id",
//																			"vf5_line_supply_method" });
//
//															ID = operationRevision
//																	.getPropertyDisplayableValue("item_id");
//
//															ArrayList<TCComponent> operations = UIGetValuesUtility
//																	.searchPartsInStruture(session, new String[] { ID },
//																			traverseStructure);
//
//															if (operations != null) {
//
//																for (TCComponent ops : operations) {
//
//																	wsList = opWs.getWorkStationsID(ID);
//
//																	if (wsList == null) {
//
//																		String workStation = opWs.generateWorkStationID(
//																				(TCComponentBOMLine) ops,
//																				"bl_rev_object_name");
//
//																		if (workStation.equals("") == false) {
//
//																			opWs.setWorkStationsID(ID, workStation);
//
//																		}
//																	} else {
//
//																		String workStation = opWs.generateWorkStationID(
//																				(TCComponentBOMLine) ops,
//																				"bl_rev_object_name");
//																		opWs.setWorkStationsID(ID, workStation);
//																	}
//																}
//															}
//														} else {
//
//															// error code
//														}
//													}
//
//													String MESIndicator = operationBOMLine
//															.getProperty("vf4_operation_type");
//													if (MESIndicator.equals("NA") || MESIndicator.equals("")) {
//														MESIndicator = "N";
//													} else {
//														MESIndicator = "Y";
//													}
//
//													String JES = operationBOMLine.getProperty("vf5_line_supply_method");
//													if (JES.equals("NA") || JES.equals("")) {
//														JES = "JIS";
//													}
//
//													String childID = inputBOMLine.getProperty("bl_item_item_id");
//													Set<String> add_Out_keys = sol_P_Output.keySet();
//
//													for (String addOutputLine : add_Out_keys) {
//
//														TCComponentBOMLine add_Ouput_Part = (TCComponentBOMLine) sol_P_Output
//																.get(addOutputLine);
//														String parentID = add_Ouput_Part.getProperty("bl_item_item_id");
//
//														HashMap<String, String> transferBOMMap = new HashMap<String, String>();
//														transferBOMMap.put("MCN", mcnValue);
//														transferBOMMap.put("PLANTCODE", plantValue);
//														transferBOMMap.put("ACTION", "A");
//														transferBOMMap.put("PARENTPART", parentID);
//														transferBOMMap.put("BOMLINEID",
//																getFindNo((TCComponentBOMLine) inputBOMLine));
//														transferBOMMap.put("QUANTITY",
//																getQuantity((TCComponentBOMLine) inputBOMLine));
//														transferBOMMap.put("SEQUENCE",
//																UIGetValuesUtility.getSequenceID());
//														transferBOMMap.put("CHILDPART", childID);
//														UIGetValuesUtility.updateNodeForSubtitutePart(
//																(TCComponentBOMLine) inputBOMLine, transferBOMMap);
//
//														ArrayList<String> workStations = opWs.getWorkStationsID(ID);
//														ArrayList<HashMap<String, String>> BOPMap = null;
//
//														if (workStations.isEmpty() == false) {
//
//															BOPMap = new ArrayList<HashMap<String, String>>();
//
//															for (String wsID : workStations) {
//
//																HashMap<String, String> transferBOPMap = new HashMap<String, String>();
//																transferBOPMap.put("SAPPLANT", plantValue);
//																transferBOPMap.put("BOMLINEID",
//																		transferBOMMap.get("BOMLINEID"));
//																transferBOPMap.put("TOPLEVELPART", parentID);
//																transferBOPMap.put("HEADERPART", parentID);
//																transferBOPMap.put("ACTION", "A");
//																transferBOPMap.put("WORKSTATION", wsID);
//																transferBOPMap.put("LINESUPPLYMETHOD", JES);
//																transferBOPMap.put("BOPID", ID);
//																transferBOPMap.put("MESBOPINDICATOR", MESIndicator);
//																transferBOPMap.put("SEQUENCE",
//																		UIGetValuesUtility.getSequenceID());
//																transferBOPMap.put("REVISION", operationBOMLine
//																		.getProperty("bl_rev_item_revision_id"));
//																BOPMap.add(transferBOPMap);
//															}
//														}
//
//														transferValues.put(transferBOMMap, BOPMap);
//													}
//
//													sol_C_Input.remove(key);
//												}
//
//											}
//
//										}
//
//										Set<String> add_Output_keys = add_Output_parts.keySet();
//
//										for (String addLine : add_Output_keys) {
//
//											Set<String> add_input_keys = sol_C_Input.keySet();
//
//											if (add_input_keys.size() != 0) {
//
//												TCComponentBOMLine[] inputLines = new TCComponentBOMLine[add_input_keys
//														.size()];
//												int iterator = 0;
//												for (String addLines : add_input_keys) {
//
//													inputLines[iterator] = (TCComponentBOMLine) sol_C_Input
//															.get(addLines);
//													iterator++;
//												}
//
//												NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOPScooter(session,
//														inputLines, (TCComponentBOMLine) traverseStructure, itemID,
//														count, strBuilder);
//
//												if (bopNode != null) {
//
//													for (NodeInfo addLines : bopNode) {
//
//														ArrayList<String> wsList = null;
//														String ID = "";
//														TCComponentBOMLine operationBOMLine = null;
//														TCComponent inputBOMLine = addLines.originalNode;
//														TCComponent[] inputBOPLine = addLines.foundNodes;
//
//														for (TCComponent foundLine : inputBOPLine) {
//
//															TCComponentBOMLine bopLine = (TCComponentBOMLine) foundLine;
//															operationBOMLine = (TCComponentBOMLine) bopLine
//																	.getReferenceProperty("bl_parent");
//															TCComponentItemRevision operationRevision = operationBOMLine
//																	.getItemRevision();
//
//															if (operationRevision.getDisplayType()
//																	.equalsIgnoreCase("Operation Revision")) {
//
//																dmService.getProperties(
//																		new TCComponent[] { operationBOMLine },
//																		new String[] { "vf4_operation_type",
//																				"bl_rev_item_revision_id",
//																				"vf5_line_supply_method" });
//
//																ID = operationRevision
//																		.getPropertyDisplayableValue("item_id");
//
//																ArrayList<TCComponent> operations = UIGetValuesUtility
//																		.searchPartsInStruture(session,
//																				new String[] { ID },
//																				traverseStructure);
//
//																if (operations != null) {
//
//																	for (TCComponent ops : operations) {
//
//																		wsList = opWs.getWorkStationsID(ID);
//
//																		if (wsList == null) {
//
//																			String workStation = opWs
//																					.generateWorkStationID(
//																							(TCComponentBOMLine) ops,
//																							"bl_rev_object_name");
//
//																			if (workStation.equals("") == false) {
//
//																				opWs.setWorkStationsID(ID, workStation);
//
//																			}
//																		}
//																	}
//																}
//															} else {
//																// error code
//															}
//														}
//
//														String MESIndicator = operationBOMLine
//																.getProperty("vf4_operation_type");
//														if (MESIndicator.equals("NA") || MESIndicator.equals("")) {
//															MESIndicator = "N";
//														} else {
//															MESIndicator = "Y";
//														}
//
//														String JES = operationBOMLine
//																.getProperty("vf5_line_supply_method");
//														if (JES.equals("NA") || JES.equals("")) {
//															JES = "JIS";
//														}
//
//														String childID = inputBOMLine.getProperty("bl_item_item_id");
//
//														TCComponentBOMLine add_Part = (TCComponentBOMLine) add_Output_parts
//																.get(addLine);
//														String parentID = add_Part.getProperty("bl_item_item_id");
//
//														HashMap<String, String> transferBOMMap = new HashMap<String, String>();
//														transferBOMMap.put("MCN", mcnValue);
//														transferBOMMap.put("PLANTCODE", plantValue);
//														transferBOMMap.put("ACTION", "A");
//														transferBOMMap.put("PARENTPART", parentID);
//														transferBOMMap.put("BOMLINEID",
//																getFindNo((TCComponentBOMLine) inputBOMLine));
//														transferBOMMap.put("QUANTITY",
//																getQuantity((TCComponentBOMLine) inputBOMLine));
//														transferBOMMap.put("SEQUENCE",
//																UIGetValuesUtility.getSequenceID());
//														transferBOMMap.put("CHILDPART", childID);
//														UIGetValuesUtility.updateNodeForSubtitutePart(
//																(TCComponentBOMLine) inputBOMLine, transferBOMMap);
//
//														ArrayList<String> workStations = opWs.getWorkStationsID(ID);
//														ArrayList<HashMap<String, String>> BOPMap = null;
//
//														if (workStations.isEmpty() == false) {
//
//															BOPMap = new ArrayList<HashMap<String, String>>();
//
//															for (String wsID : workStations) {
//
//																HashMap<String, String> transferBOPMap = new HashMap<String, String>();
//																transferBOPMap.put("SAPPLANT", plantValue);
//																transferBOPMap.put("BOMLINEID",
//																		transferBOMMap.get("BOMLINEID"));
//																transferBOPMap.put("TOPLEVELPART", parentID);
//																transferBOPMap.put("HEADERPART", parentID);
//																transferBOPMap.put("ACTION", "A");
//																transferBOPMap.put("WORKSTATION", wsID);
//																transferBOPMap.put("LINESUPPLYMETHOD", JES);
//																transferBOPMap.put("BOPID", ID);
//																transferBOPMap.put("MESBOPINDICATOR", MESIndicator);
//																transferBOPMap.put("SEQUENCE",
//																		UIGetValuesUtility.getSequenceID());
//																transferBOPMap.put("REVISION", operationBOMLine
//																		.getProperty("bl_rev_item_revision_id"));
//																BOPMap.add(transferBOPMap);
//															}
//														}
//
//														transferValues.put(transferBOMMap, BOPMap);
//
//													}
//												}
//											}
//
//										}
//										UIGetValuesUtility.closeWindow(session, output[0].bomWindow);
//
//										if (transferValues.isEmpty() == false && isError == false) {
//
//											for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : transferValues
//													.entrySet()) {
//
//												HashMap<String, String> bomMap = entry.getKey();
//												ArrayList<HashMap<String, String>> bopMap = entry.getValue();
//
//												if (bomMap.isEmpty() == false) {
//
//													String BOMFile = bomMap.get("CHILDPART") + "_"+ bomMap.get("PARENTPART") + "_" + bomMap.get("BOMLINEID");
//													String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(serverIP), bomMap,
//															SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG,
//															SAPURL.ASSY_BOM_NAMESPACE,
//															"I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder,
//															auth);
//													if (message[0].equals("E") && !message[1]
//															.contains("Record is exist in staging table")) {
//
//														printValues = new String[] { Integer.toString(count), itemID,
//																bomMap.get("PARENTPART"), bomMap.get("BOMLINEID"),
//																message[1], bomMap.get("ACTION"), "Error" };
//														Logger.bufferResponse("PRINT", printValues, strBuilder);
//													} else {
//														printValues = new String[] { Integer.toString(count), itemID,
//																bomMap.get("PARENTPART"), bomMap.get("BOMLINEID"),
//																message[1], bomMap.get("ACTION"), "Success" };
//														Logger.bufferResponse("PRINT", printValues, strBuilder);
//													}
//
//													for (HashMap<String, String> BOP : bopMap) {
//
//														String BOPFile = bomMap.get("BOMLINEID") + "_"
//																+ BOP.get("TOPLEVELPART") + "_"
//																+ BOP.get("WORKSTATION");
//														String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(serverIP), BOP,
//																SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG,
//																SAPURL.ASSY_BOP_NAMESPACE,
//																"I_BOP_" + BOP.get("ACTION") + "_" + BOPFile, logFolder,
//																auth);
//														if (msg[0].equals("E") && !msg[1]
//																.contains("Record is exist in staging table")) {
//
//															printValues = new String[] { Integer.toString(count),
//																	itemID,
//																	BOP.get("BOPID") + "/" + BOP.get("REVISION"),
//																	BOP.get("BOMLINEID"), msg[1], bomMap.get("ACTION"),
//																	"Error" };
//															Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//														} else {
//
//															printValues = new String[] { Integer.toString(count),
//																	itemID,
//																	BOP.get("BOPID") + "/" + BOP.get("REVISION"),
//																	BOP.get("BOMLINEID"), msg[1], bomMap.get("ACTION"),
//																	"Success" };
//															Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//														}
//													}
//													count++;
//												}
//
//											}
//											if (serverValue.equals("PRODUCTION")) {
//												UIGetValuesUtility.setProperty(dmService, solution_Rev,
//														"vf3_transfer_to_sap", "Yes");
//											}
//
//										}
//
//									} else {
//
//										printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID,
//												"", "", "No change in old revision and current revision", "", "Error" };
//										Logger.bufferResponse("PRINT", printValues, strBuilder);
//									}
//								}
//							}
//						} else {
//
//							HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = new HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
//
//							boolean IOValues = bomcompare.inputOutputMap(session, null,
//									solutionItem, count, strBuilder);
//
//							if (IOValues == true) {
//
//								boolean isError = false;
//
//								HashMap<String, TCComponent> sol_P_Output = bomcompare.getSolutionOutputItems();
//								HashMap<String, TCComponent> sol_C_Input = bomcompare.getSolutionInputItems();
//
//								// ADD
//								Set<String> add_Input_keys = sol_C_Input.keySet();
//
//								if (add_Input_keys.size() != 0) {
//
//									TCComponentBOMLine[] inputLines = new TCComponentBOMLine[add_Input_keys.size()];
//									int iterator = 0;
//									for (String addLine : add_Input_keys) {
//
//										inputLines[iterator] = (TCComponentBOMLine) sol_C_Input.get(addLine);
//										iterator++;
//									}
//
//									NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOPScooter(session, inputLines,
//											(TCComponentBOMLine) traverseStructure, itemID, count, strBuilder);
//
//									if (bopNode != null) {
//
//										for (NodeInfo addLine : bopNode) {
//
//											ArrayList<String> wsList = null;
//											String ID = "";
//											TCComponentBOMLine operationBOMLine = null;
//											TCComponent inputBOMLine = addLine.originalNode;
//											TCComponent[] inputBOPLine = addLine.foundNodes;
//
//											for (TCComponent foundLine : inputBOPLine) {
//
//												TCComponentBOMLine bopLine = (TCComponentBOMLine) foundLine;
//												operationBOMLine = (TCComponentBOMLine) bopLine
//														.getReferenceProperty("bl_parent");
//												TCComponentItemRevision operationRevision = operationBOMLine
//														.getItemRevision();
//
//												if (operationRevision.getDisplayType()
//														.equalsIgnoreCase("Operation Revision")) {
//
//													dmService.getProperties(new TCComponent[] { operationBOMLine },
//															new String[] { "vf4_operation_type",
//																	"bl_rev_item_revision_id",
//																	"vf5_line_supply_method" });
//
//													ID = operationRevision.getPropertyDisplayableValue("item_id");
//
//													ArrayList<TCComponent> operations = UIGetValuesUtility
//															.searchPartsInStruture(session, new String[] { ID },
//																	traverseStructure);
//
//													if (operations != null) {
//
//														for (TCComponent ops : operations) {
//
//															wsList = opWs.getWorkStationsID(ID);
//
//															if (wsList == null) {
//
//																String workStation = opWs.generateWorkStationID(
//																		(TCComponentBOMLine) ops, "bl_rev_object_name");
//
//																if (workStation.equals("") == false) {
//
//																	opWs.setWorkStationsID(ID, workStation);
//
//																}
//															}
//														}
//													}
//												} else {
//
//													// error code
//												}
//											}
//
//											String MESIndicator = operationBOMLine.getProperty("vf4_operation_type");
//											if (MESIndicator.equals("NA") || MESIndicator.equals("")) {
//												MESIndicator = "N";
//											} else {
//												MESIndicator = "Y";
//											}
//
//											String JES = operationBOMLine.getProperty("vf5_line_supply_method");
//											if (JES.equals("NA") || JES.equals("")) {
//												JES = "JIS";
//											}
//
//											String childID = inputBOMLine.getProperty("bl_item_item_id");
//											Set<String> add_Out_keys = sol_P_Output.keySet();
//
//											for (String addOutputLine : add_Out_keys) {
//
//												TCComponentBOMLine add_Ouput_Part = (TCComponentBOMLine) sol_P_Output
//														.get(addOutputLine);
//												String parentID = add_Ouput_Part.getProperty("bl_item_item_id");
//
//												HashMap<String, String> transferBOMMap = new HashMap<String, String>();
//												transferBOMMap.put("MCN", mcnValue);
//												transferBOMMap.put("PLANTCODE", plantValue);
//												transferBOMMap.put("ACTION", "A");
//												transferBOMMap.put("PARENTPART", parentID);
//												transferBOMMap.put("BOMLINEID",
//														getFindNo((TCComponentBOMLine) inputBOMLine));
//												transferBOMMap.put("QUANTITY",
//														getQuantity((TCComponentBOMLine) inputBOMLine));
//												transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
//												transferBOMMap.put("CHILDPART", childID);
//												UIGetValuesUtility.updateNodeForSubtitutePart(
//														(TCComponentBOMLine) inputBOMLine, transferBOMMap);
//
//												ArrayList<String> workStations = opWs.getWorkStationsID(ID);
//												ArrayList<HashMap<String, String>> BOPMap = null;
//
//												if (workStations != null) {
//													BOPMap = new ArrayList<HashMap<String, String>>();
//													for (String wsID : workStations) {
//														HashMap<String, String> transferBOPMap = new HashMap<String, String>();
//														transferBOPMap.put("SAPPLANT", plantValue);
//														transferBOPMap.put("BOMLINEID",
//																transferBOMMap.get("BOMLINEID"));
//														transferBOPMap.put("TOPLEVELPART", parentID);
//														transferBOPMap.put("HEADERPART", parentID);
//														transferBOPMap.put("ACTION", "A");
//														transferBOPMap.put("WORKSTATION", wsID);
//														transferBOPMap.put("LINESUPPLYMETHOD", JES);
//														transferBOPMap.put("BOPID", ID);
//														transferBOPMap.put("MESBOPINDICATOR", MESIndicator);
//														transferBOPMap.put("SEQUENCE",
//																UIGetValuesUtility.getSequenceID());
//														transferBOPMap.put("REVISION", operationBOMLine
//																.getProperty("bl_rev_item_revision_id"));
//														BOPMap.add(transferBOPMap);
//													}
//												}
//												transferValues.put(transferBOMMap, BOPMap);
//											}
//										}
//									}
//								}
//								if (transferValues.isEmpty() == false && isError == false) {
//									for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : transferValues
//											.entrySet()) {
//										HashMap<String, String> bomMap = entry.getKey();
//										ArrayList<HashMap<String, String>> bopMap = entry.getValue();
//
//										if (bomMap.isEmpty() == false) {
//
//											String BOMFile = bomMap.get("CHILDPART") + "_" + bomMap.get("PARENTPART")
//													+ "_" + bomMap.get("BOMLINEID");
//											String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(serverIP), bomMap,
//													SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG,
//													SAPURL.ASSY_BOM_NAMESPACE,
//													"I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
//											if (message[0].equals("E")
//													&& !message[1].contains("Record is exist in staging table")) {
//
//												printValues = new String[] { Integer.toString(count), itemID,
//														bomMap.get("PARENTPART"), bomMap.get("BOMLINEID"), message[1],
//														bomMap.get("ACTION"), "Error" };
//												Logger.bufferResponse("PRINT", printValues, strBuilder);
//											} else {
//												printValues = new String[] { Integer.toString(count), itemID,
//														bomMap.get("PARENTPART"), bomMap.get("BOMLINEID"), message[1],
//														bomMap.get("ACTION"), "Success" };
//												Logger.bufferResponse("PRINT", printValues, strBuilder);
//											}
//
//											for (HashMap<String, String> BOP : bopMap) {
//
//												String BOPFile = bomMap.get("BOMLINEID") + "_" + BOP.get("TOPLEVELPART")
//														+ "_" + BOP.get("WORKSTATION");
//												String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(serverIP), BOP,
//														SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG,
//														SAPURL.ASSY_BOP_NAMESPACE,
//														"I_BOP_" + BOP.get("ACTION") + "_" + BOPFile, logFolder, auth);
//												if (msg[0].equals("E")
//														&& !msg[1].contains("Record is exist in staging table")) {
//
//													printValues = new String[] { Integer.toString(count), itemID,
//															BOP.get("BOPID") + "/" + BOP.get("REVISION"),
//															BOP.get("BOMLINEID"), msg[1], bomMap.get("ACTION"),
//															"Error" };
//													Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//												} else {
//
//													printValues = new String[] { Integer.toString(count), itemID,
//															BOP.get("BOPID") + "/" + BOP.get("REVISION"),
//															BOP.get("BOMLINEID"), msg[1], bomMap.get("ACTION"),
//															"Success" };
//													Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//												}
//											}
//										}
//										count++;
//									}
//									if (serverValue.equals("PRODUCTION")) {
//										UIGetValuesUtility.setProperty(dmService, solution_Rev, "vf3_transfer_to_sap",
//												"Yes");
//									}
//								}
//							}
//						}
//					} else {
//						dlg.setProgressStatus(finishedTask, totalTask);
//						printValues = new String[] { Integer.toString(count), itemID, "", "",
//								"Subgroup already transferred to SAP", "-", "Info" };
//						Logger.bufferResponse("PRINT", printValues, strBuilder);
//						count++;
//					}
//					finishedTask++;
//				}
//
//				if (impactedItemsList.isEmpty() == false) {
//					for (Map.Entry<String, TCComponentItemRevision> entry : impactedItemsList.entrySet()) {
//						dlg.setProgressStatus(finishedTask, totalTask);
//						printValues = new String[] { Integer.toString(count), entry.getKey(), "", "",
//								"Subgroup no found in Shop", "-", "Error" };
//						Logger.bufferResponse("PRINT", printValues, strBuilder);
//						count++;
//						finishedTask++;
//					}
//				}
//			}
//
//			if (problemItems != null) {
//				for (TCComponent problemItem : problemItems) {
//					dlg.setProgressStatus(finishedTask, totalTask);
//					TCComponentItemRevision problem = (TCComponentItemRevision) problemItem;
//					CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
//					TCComponentBOMWindow operationWindow = output[0].bomWindow;
//					TCComponentBOMLine operationLine = output[0].bomLine;
//					if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
//						dmService.getProperties(new TCComponent[] { operationLine },
//								new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "VF4_bomline_id",
//										"vf4_operation_type", "bl_rev_vf3_transfer_to_sap",
//										"bl_rev_vf5_line_supply_method", "vf4_user_notes",
//										"bl_rev_vf3_transfer_to_mes" });
//						dmService.getProperties(new TCComponent[] { problem },
//								new String[] { "vf3_transfer_to_sap", "vf3_transfer_to_mes", "vf4_user_notes" });
//						String ID = operationLine.getProperty("bl_item_item_id");
//						String RevID = operationLine.getProperty("bl_rev_item_revision_id");
//						String HT = operationLine.getProperty("vf4_reference_component");
//
//						if (HT.equals("")) {
//							printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "",
//									"Header Part information missing on operation. Fill in Bomline", "-", "Error" };
//							Logger.bufferResponse("PRINT", printValues, strBuilder);
//							count++;
//						} else {
//							String WS = UIGetValuesUtility
//									.getWorkStation(new String[] { "vf3_transfer_to_mes", "vf4_user_notes" }, problem);
//
//							String[] split = WS.split(",");
//
//							String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
//							if (LSM.equals("")) {
//								LSM = "JIS";
//							}
//
//							String MES = operationLine.getProperty("vf4_operation_type");
//							if (MES.equals("") || MES.equals("NA")) {
//								MES = "N";
//							} else {
//								MES = "Y";
//							}
//
//							for (String workstation : split) {
//								HashMap<String, String> BOP_Values = new HashMap<String, String>();
//								BOP_Values.put("SAPPLANT", plantValue);
//								BOP_Values.put("TOPLEVELPART", HT);
//								BOP_Values.put("HEADERPART", HT);
//								BOP_Values.put("BOMLINEID", "");
//								BOP_Values.put("ACTION", "D");
//								BOP_Values.put("WORKSTATION", workstation);
//								BOP_Values.put("LINESUPPLYMETHOD", LSM);
//								BOP_Values.put("BOPID", ID);
//								BOP_Values.put("MESBOPINDICATOR", MES);
//								BOP_Values.put("REVISION", RevID);
//								BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
//
//								String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
//								String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(serverIP), BOP_Values, SAPURL.ASSY_BOP_HEADER,
//										SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,
//										"I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
//								if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
//									printValues = new String[] { Integer.toString(count), "OWP",
//											BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1],
//											BOP_Values.get("ACTION"), "Error" };
//									Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//								} else {
//									printValues = new String[] { Integer.toString(count), "OWP",
//											BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1],
//											BOP_Values.get("ACTION"), "Success" };
//									Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//								}
//								count++;
//							}
//
//						}
//					}
//					finishedTask++;
//					operationWindow.close();
//				}
//			}
//
//			if (solutionItems != null) {
//				ArrayList<String> operationIDs = new ArrayList<String>();
//				for (TCComponent solutionItem : solutionItems) {
//					TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;
//					operationIDs.add(operations.getProperty("item_id"));
//				}
//				String searchIDs = UIGetValuesUtility.convertArrayToString(operationIDs, ";");
//
//				TCComponent[] operationsList = UIGetValuesUtility.searchStruture(session, searchIDs, traverseStructure);
//
//				if (operationsList != null && operationsList.length > 0) {
//					for (TCComponent operationItem : operationsList) {
//						TCComponentBOMLine operationLine = (TCComponentBOMLine) operationItem;
//						TCComponentItemRevision operationRevision = operationLine.getItemRevision();
//						if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
//							dmService.getProperties(new TCComponent[] { operationLine },
//									new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "VF4_bomline_id",
//											"vf4_operation_type", "bl_rev_vf3_transfer_to_sap",
//											"bl_rev_vf5_line_supply_method" });
//							String ID = operationLine.getProperty(PropertyDefines.BOM_ITEM_ID);
//							String RevID = operationLine.getProperty("bl_rev_item_revision_id");
//							String HT = operationLine.getProperty("VF4_bomline_id");
//
//							if (HT.equals("")) {
//								printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "",
//										"Header Part information missing on operation. Fill in Bomline", "-", "Error" };
//								Logger.bufferResponse("PRINT", printValues, strBuilder);
//								count++;
//							} else {
//								String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
//								if (LSM.equals(""))
//									LSM = "JIS";
//
//								String MES = operationLine.getProperty("vf4_operation_type");
//								if (MES.equals("") || MES.equals("NA"))
//									MES = "N";
//								else
//									MES = "Y";
//
//								HashMap<String, String> BOP_Values = new HashMap<String, String>();
//								BOP_Values.put("SAPPLANT", plantValue);
//								BOP_Values.put("TOPLEVELPART", HT);
//								BOP_Values.put("HEADERPART", HT);
//								BOP_Values.put("BOMLINEID", "");
//								BOP_Values.put("ACTION", "A");
//								BOP_Values.put("WORKSTATION",
//										UIGetValuesUtility.getWorkStationID(operationLine, "bl_rev_object_name"));
//								BOP_Values.put("LINESUPPLYMETHOD", LSM);
//								BOP_Values.put("BOPID", ID);
//								BOP_Values.put("MESBOPINDICATOR", MES);
//								BOP_Values.put("REVISION", RevID);
//								BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
//
//								String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
//								String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(serverIP), BOP_Values, SAPURL.ASSY_BOP_HEADER,
//										SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,
//										"I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
//								if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
//
//									printValues = new String[] { Integer.toString(count), "OWP",
//											BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1],
//											BOP_Values.get("ACTION"), "Error" };
//									Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//								} else {
//									if (serverValue.equals("PRODUCTION") || serverValue.equals("QA")) {
//										UIGetValuesUtility.setProperty(dmService, operationRevision,
//												"vf3_transfer_to_sap", "Yes");
//									}
//									printValues = new String[] { Integer.toString(count), "OWP",
//											BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1],
//											BOP_Values.get("ACTION"), "Success" };
//									Logger.bufferResponse("PRINT", printValues, strBuilder);
//
//								}
//								count++;
//							}
//						}
//					}
//					finishedTask++;
//				}
//			}
//
//			strBuilder.append("</table>");
//			strBuilder.append("</body></html>");
//
//			String data = Logger.previousTransaction(logFolder, "BOM");
//
//			if (!data.equals("")) {
//				strBuilder.append("<br>");
//				strBuilder.append(data);
//			}
//
//			strBuilder.append("</body></html>");
//
//			logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
//
//			if (serverValue.equals("PRODUCTION") == true) {
//				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, selectedObject,
//						"IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
//
//				if (newDataset != null) {
//					UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
//				}
//			}
//			SwingUtilities.invokeLater(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						StringViewerDialog viewdialog = new StringViewerDialog(logFile);
//						viewdialog.setSize(500, 400);
//						viewdialog.setLocationRelativeTo(null);
//						viewdialog.setVisible(true);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			UIGetValuesUtility.closeAllContext(session, createdBOMViews);
//		} catch (TCException e1) {
//			e1.printStackTrace();
//		} catch (NotLoadedException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private String getFindNo(TCComponentBOMLine child) {
//		String bomLineID = "";
//		try {
//			bomLineID = child.getProperty("VF4_bomline_id");
//			for (int i = bomLineID.length(); i < 4; i++) {
//				bomLineID = "0" + bomLineID;
//			}
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		return bomLineID;
//	}
//
//	private String getQuantity(TCComponentBOMLine child) {
//		String quantity = "";
//		try {
//			quantity = child.getProperty("bl_quantity");
//			if (quantity.equals("")) {
//				quantity = "1";
//			}
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		return quantity;
//	}
//
//	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
//		if (targetComponents == null)
//			return true;
//		if (targetComponents.length > 1)
//			return true;
//
//		if (targetComponents[0] instanceof TCComponentItemRevision) {
//			selectedObject = (TCComponentItemRevision) targetComponents[0];
//		}
//
//		return false;
//	}
//}
