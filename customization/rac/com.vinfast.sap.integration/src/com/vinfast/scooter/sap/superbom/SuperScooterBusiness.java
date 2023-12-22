package com.vinfast.scooter.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.integration.arch.BusinessHandlerAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ActionCommand.Command;
import com.vinfast.integration.model.NoticeMessage;
import com.vinfast.integration.model.NoticeMessage.NoticeType;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.ScooterSuperBomBopDataSend;
import com.vinfast.integration.model.ServerInformation;
import com.vinfast.integration.model.SuperScooterReport;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.scooter.sap.assembly.OperationsWorkStations;

public class SuperScooterBusiness extends BusinessHandlerAbstract{
	
	TCComponentItemRevision obj_shopTopNode = null;
	TCComponentItemRevision obj_Shop = null;
	TCComponent traverseStructure = null;
	TCComponent[] workStations = null;
	MCNInformation mcn = null;
	ServerInformation serverInfo = null;
	SuperScooterDataHelper helper = new SuperScooterDataHelper();
	int count = 0;
	
	public SuperScooterBusiness(String processName) {
		super(processName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(ModelAbstract msg) {
		switch(msg.getModelType()) {
		case ACTION_COMMAND:
			onCommand((ActionCommand)msg);
			break;
		case SERVER_INFORMATION:
			serverInfo = (ServerInformation)msg;
			System.out.println(String.format("SERVER_INFORMATION %s:%s", serverInfo.getServerType(), serverInfo.getIp()));
		default:
			break;
		}
		
	}

	private void onCommand(ActionCommand cmd) {
		switch(cmd.getCommand()) {
		case COMMAND_PREPARE:
			try {
				prepare();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case COMMAND_LOAD_MCN_INFO:
			loadMCNInformation();
			break;
		default:
			break;
		}	
	}

	private void loadMCNInformation() {
		try {

			TCHelper.getInstance().dmService.getProperties(new TCComponent[] {TCHelper.getInstance().changeObject}, new String[] {"item_id","vf6_sap_plant",PropertyDefines.REL_IMPACT_SHOP});

			String plant = TCHelper.getInstance().changeObject.getPropertyDisplayableValue("vf6_sap_plant");

			String MCN_SAPID = TCHelper.getInstance().changeObject.getPropertyDisplayableValue("item_id");

			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);

			AIFComponentContext[] impactedShop =  TCHelper.getInstance().changeObject.getRelated(PropertyDefines.REL_IMPACT_SHOP);

			if(impactedShop.length == 0 || impactedShop.length > 1){
				publish(PropertyDefines.UI_STORE, new NoticeMessage("No or more items exists in impacted shop.", NoticeType.ERROR));
				return;

			}else{

				obj_Shop = (TCComponentItemRevision)impactedShop[0].getComponent();

				obj_shopTopNode = UIGetValuesUtility.getTopLevelItemRevision(TCHelper.getInstance().session, obj_Shop, PropertyDefines.RULE_WORKING);

				String err = UIGetValuesUtility.checkValidShop(obj_Shop, plant);

				if(!err.isEmpty()) {
					publish(PropertyDefines.UI_STORE, new NoticeMessage(err, NoticeType.ERROR));
					return;
				}

			}


			String mainGroup = obj_Shop.getProperty("object_name");
			String platform = "";
			String modelYear = "";
			
			MaterialPlatformCode platformCode = null;

			if(obj_shopTopNode != null) {

				platformCode = UIGetValuesUtility.getPlatformCode(TCHelper.getInstance().dmService, obj_shopTopNode);

				if((platformCode.getPlatformCode().equals("") || platformCode.getModelYear().equals(""))) {

					platform = obj_shopTopNode.getPropertyDisplayableValue("item_id");
					modelYear = obj_shopTopNode.getPropertyDisplayableValue("object_name");
					
				}else {
					
					platform = platformCode.getPlatformCode();
					modelYear = platformCode.getModelYear();
				}
			}else {

				obj_shopTopNode = obj_Shop;

			}
			mcn = new MCNInformation();
			mcn.setPlant(plant);
			mcn.setSapID(MCN_SAPID);
			mcn.setMcnID(MCN);
			mcn.setPlatForm(platform);
			mcn.setModelYear(modelYear);
			mcn.setMainGroup(mainGroup);
			if(platformCode != null) {
				mcn.setMaterialCode(platformCode.getMaterialCode());
			}
			//send to update ui
			publish(PropertyDefines.UI_STORE, mcn);
			publish(PropertyDefines.REPORT_PROCESSOR_NAME, mcn);
			publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, mcn);
			publish(PropertyDefines.UI_STORE, new ProcessStatus("Update MCN Info done"));
			publish(PropertyDefines.UI_STORE, new ProcessStatus(10));
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void prepare() throws Exception {
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Preparing data"));
		ArrayList<SuperScooterReport> reportList = new ArrayList<SuperScooterReport>();
		publish(PropertyDefines.REPORT_PROCESSOR_NAME,new ActionCommand(Command.COMMAND_INIT_REPORT));
		publish(PropertyDefines.UI_STORE, new ProcessStatus(20));
		try {

			TCHelper.getInstance().dmService.getProperties(new TCComponent[] {TCHelper.getInstance().changeObject}, new String[] {"Vf6_impacted_shop","CMHasImpactedItem","EC_solution_item_rel"});

			AIFComponentContext[] impacted_Shop = TCHelper.getInstance().changeObject.getRelated("Vf6_impacted_shop");

			if(impacted_Shop.length == 0 || impacted_Shop.length > 1) {

				MessageBox.post("\"Impacted Shop\" folder cannot be empty or more than one item.", "Error",MessageBox.ERROR);
				return;
			}else {

				TCComponentItemRevision shopItemRev = (TCComponentItemRevision) impacted_Shop[0].getComponent();

				String err = UIGetValuesUtility.checkValidShop(shopItemRev, mcn.getPlant());

				if (!err.equals("")) {

					MessageBox.post(err, "Error", MessageBox.ERROR);
					return;

				}else {

					TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(TCHelper.getInstance().dmService, TCHelper.getInstance().changeObject, new String[] {"MEOPRevision"},"CMHasProblemItem");


					TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(TCHelper.getInstance().dmService, TCHelper.getInstance().changeObject, new String[] {"MEOPRevision"},"EC_solution_item_rel");


					TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(TCHelper.getInstance().dmService, TCHelper.getInstance().changeObject, new String[] {},"CMHasImpactedItem");

					if (impactedItems == null) {

						MessageBox.post("No \"Impacted Items\" to transfer to SAP.", "Error", MessageBox.ERROR);
						return;

					}else {
						TCComponent BOMLine = null;
						TCComponent BOPLine = null;

						OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(TCHelper.getInstance().session, obj_shopTopNode.getItem());

						for(OpenContextInfo views : createdBOMViews) {

							if(views.context.getType().equals("BOMLine")) {
								BOMLine = views.context;
							}
							if(views.context.getType().equals("Mfg0BvrPlantBOP")) {
								BOPLine = views.context;
							}
						}

						TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem().getRelatedComponent("IMAN_master_form");

						String searchPlantModel = topLineMasterForm.getProperty("user_data_2");

						if(searchPlantModel.length() !=0) {

							ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(TCHelper.getInstance().session, new String[] {searchPlantModel}, BOPLine);
							if(!plantModelList.isEmpty()) {

								traverseStructure = plantModelList.get(0);
								UIGetValuesUtility.setViewReference(TCHelper.getInstance().session, BOMLine, BOPLine);

							}else {

								TCComponentItem plantModel = UIGetValuesUtility.findItem(TCHelper.getInstance().session, searchPlantModel);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(TCHelper.getInstance().session, plantModel);
								BOPLine =  createdBOPView[0].context;
								UIGetValuesUtility.setViewReference(TCHelper.getInstance().session, BOMLine, BOPLine);
								traverseStructure = BOPLine; 
							}
						}else {
							traverseStructure = BOPLine; 
						}
						
						ArrayList<TCComponent> searchLines = null;

						String impactShopID = obj_Shop.getProperty("item_id");
						
						if(BOMLine.getProperty("bl_item_item_id").equals(impactShopID)) {

							searchLines =  new ArrayList<TCComponent>();
							searchLines.add(BOMLine);

						}else {

							searchLines = UIGetValuesUtility.searchPartsInStruture(TCHelper.getInstance().session,new String[] {impactShopID}, BOMLine);
						}

						
						if(searchLines.isEmpty() == false) {

							TCComponent shop = searchLines.get(0);

							LinkedHashMap<String, TCComponentItemRevision> impactedItemsList = new LinkedHashMap<String, TCComponentItemRevision>();

							for(TCComponent impactedItem : impactedItems){

								TCComponentItemRevision impactedRev = (TCComponentItemRevision) impactedItem;

								impactedItemsList.put(impactedItem.getProperty("item_id"), impactedRev);
							}

							if(impactedItemsList.isEmpty() == false) {

								String impactItemsIDs = impactedItemsList.keySet().toString().replace(",", ";");

								Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(impactItemsIDs);

								while(m.find()) {

									impactItemsIDs = m.group(1);
								}
								
								ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(TCHelper.getInstance().session,new String[] { impactItemsIDs },shop);

								if(impactLines != null) {

									for(TCComponent item : impactLines) {
										
										count = 1;

										boolean isError = false;

										String itemID = item.getProperty("bl_item_item_id");

										TCComponentItemRevision solution_Rev = impactedItemsList.get(itemID);

										String itemRevID =  solution_Rev.getProperty("item_revision_id");

										impactedItemsList.remove(itemID);

										TCComponentBOMLine solutionItem = (TCComponentBOMLine)item;

										if((serverInfo.getServerType().equals("PRODUCTION") == true&& solution_Rev.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) 
											|| serverInfo.getServerType().equals("PRODUCTION") == false ) {

											int revID = Integer.parseInt(itemRevID);
											if((revID != 0 || revID != 1) && revID > 1){

												String search_rev = Integer.toString(revID - 1);

												for(int i = search_rev.length(); i<itemRevID.length(); i++){

													search_rev = "0"+search_rev;
												}
												//Problem Item
												HashMap<String, HashMap<String, String>>  prDataMap = new HashMap<String, HashMap<String, String>>();
												
												TCComponentItemRevision problemItemRevision = solution_Rev.findRevision(search_rev);
												prDataMap = getReleaseBomData(problemItemRevision, itemID, reportList);
												isError = prDataMap == null || prDataMap.size() == 0;
												

												//Solution Item
												HashMap<String, HashMap<String, String>>  solBOMDataMap = new HashMap<String, HashMap<String, String>>();
												HashMap<String, ArrayList<HashMap<String,String>>>  solBOPDataMap = new HashMap<String, ArrayList<HashMap<String,String>>>();
												
												getWorkingBomBopData(solBOMDataMap, solBOPDataMap, solutionItem, reportList, itemID);
												isError = solBOMDataMap == null || solBOPDataMap == null;
												
												if(isError == false) {
													helper.setPrDataMap(prDataMap);
													helper.setSolBOMDataMap(solBOMDataMap);
													helper.setSolBOPDataMap(solBOPDataMap);
													ArrayList<ScooterSuperBomBopDataSend> data2Send = helper.processImpactedData(itemID, reportList, solution_Rev);
													for(ScooterSuperBomBopDataSend data : data2Send) {
														publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, data);
													}
												}
											}else {

												//Solution Item
												HashMap<String, HashMap<String, String>>  solBOMDataMap = new HashMap<String, HashMap<String, String>>();
												HashMap<String, ArrayList<HashMap<String,String>>>  solBOPDataMap = new HashMap<String, ArrayList<HashMap<String,String>>>();
												
												getWorkingBomBopData(solBOMDataMap, solBOPDataMap, solutionItem, reportList, itemID);
												isError = solBOMDataMap == null || solBOPDataMap == null;
												
												if(isError == false) {
													helper.setSolBOMDataMap(solBOMDataMap);
													helper.setSolBOPDataMap(solBOPDataMap);
													ArrayList<ScooterSuperBomBopDataSend> data2Send = helper.processImpactedData(itemID, reportList, solution_Rev);
													for(ScooterSuperBomBopDataSend data : data2Send) {
														publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, data);
													}
												}
											}
										}else {
											SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),itemID,"","","Subgroup already transferred to SAP","-","Info");
											rp.setType(UpdateType.UPDATE_BODY_INFO);
											reportList.add(rp);
										}
									}

									if(impactedItemsList.isEmpty() == false) {
										for (Map.Entry<String,TCComponentItemRevision> entry : impactedItemsList.entrySet()) {
											SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),entry.getKey(),"","","Subgroup no found in Shop","-","Error");
											rp.setType(UpdateType.UPDATE_BODY_ERROR);
											reportList.add(rp);
										}
									}
								}
							}

						}
						
						publish(PropertyDefines.UI_STORE, new ProcessStatus(50));
						
						ArrayList<ScooterSuperBomBopDataSend> deletedOperationNoPart = helper.getDeletedOperationNoPart(mcn,problemItems, reportList);
						ArrayList<ScooterSuperBomBopDataSend> addedOPerationPart = helper.getAddedOperationNoPart(mcn,solutionItems, reportList, traverseStructure);
						for(ScooterSuperBomBopDataSend operation : deletedOperationNoPart) {
							publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, operation);
						}
						for(ScooterSuperBomBopDataSend operation : addedOPerationPart) {
							publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, operation);
						}

						UIGetValuesUtility.closeAllContext(TCHelper.getInstance().session, createdBOMViews);
					}
				}
			}
			for(SuperScooterReport report : reportList) {
				publish(PropertyDefines.REPORT_PROCESSOR_NAME, report);
			}
			
			publish(PropertyDefines.REPORT_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_POPUP_REPORT));
			publish(PropertyDefines.UI_STORE, new ActionCommand(Command.ALLOW_TO_TRANSFER));
			publish(PropertyDefines.UI_STORE, new ProcessStatus(100));

		}
		catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}
	
	HashMap<String, HashMap<String, String>> getReleaseBomData(TCComponentItemRevision problemItemRevision, String itemID, ArrayList<SuperScooterReport> reportList){
		
		HashMap<String, HashMap<String, String>> releaseBomData = new HashMap<String, HashMap<String, String>>();
		if(problemItemRevision != null){

			CreateBOMWindowsOutput[] prWindow = UIGetValuesUtility.createBOMNoRuleWindow(TCHelper.getInstance().session, problemItemRevision);
			TCComponentBOMLine prBOMLine = prWindow[0].bomLine;
			TCComponentBOMWindow window = prWindow[0].bomWindow;

			ExpandPSData[] PSData = UIGetValuesUtility.traverseSingleLevelBOM(TCHelper.getInstance().session, prBOMLine);

			if(PSData != null) {

				TCComponent[] problemLines = UIGetValuesUtility.fetchValidScooterBOMItems2(TCHelper.getInstance().dmService, PSData, itemID, count, reportList);

				if(problemLines != null) {

					TCHelper.getInstance().dmService.getProperties(problemLines, new String[] {"bl_rev_object_type","VL5_purchase_lvl_vf","VF3_purchase_lvl_vf","VF4_manuf_code","VF4_bomline_id","bl_item_item_id","bl_quantity","bl_formula"});

					for(TCComponent problemLine : problemLines) {

						TCComponentBOMLine problemBOMLine = (TCComponentBOMLine) problemLine;

						HashMap<String, HashMap<String,String>> bomLineValues = SuperScooterTransfer.traverseProblemItems2(mcn, problemBOMLine, itemID, count, reportList);

						if(bomLineValues != null) {

							releaseBomData.putAll(bomLineValues);
						}else {
							return null;						
						}
					}
				}
			}
			UIGetValuesUtility.closeWindow(TCHelper.getInstance().session, window);
		}
		return releaseBomData;
	}
	

	
	void getWorkingBomBopData(HashMap<String, HashMap<String, String>> solBOMDataMap, HashMap<String, ArrayList<HashMap<String,String>>> solBOPDataMap, TCComponentBOMLine solutionItem, ArrayList<SuperScooterReport> reportList, String subGroupId) throws Exception{
		// Open sub group one level (solutionItem is subgroup and itemID is subgroup ID)
		ExpandPSData[] solData = UIGetValuesUtility.traverseSingleLevelBOM(TCHelper.getInstance().session, solutionItem);
		OperationsWorkStations opWs = new OperationsWorkStations();
		if(solData.length != 0) {

			// get bomdata from lines under the current subgroup
			TCComponent[] BOMLines = UIGetValuesUtility.fetchValidScooterBOMItems2(TCHelper.getInstance().dmService, solData,subGroupId, count, reportList);

			if(BOMLines != null) {

				TCHelper.getInstance().dmService.getProperties(BOMLines, new String[] {"bl_rev_object_type","VL5_purchase_lvl_vf","VF3_purchase_lvl_vf","VF4_manuf_code","VF4_bomline_id","bl_item_item_id","bl_quantity","bl_formula"});

				NodeInfo[] bopNode = UIGetValuesUtility.findBOMLineInBOPScooter2(TCHelper.getInstance().session, BOMLines, (TCComponentBOMLine)traverseStructure,subGroupId, count, reportList);

				if(bopNode != null) {

					for(NodeInfo Node : bopNode) {

						ArrayList<String> wsList = null;
						String ID = "";
						TCComponentBOMLine operationBOMLine = null;
						TCComponentBOMLine inputBOMLine = (TCComponentBOMLine) Node.originalNode;
						TCComponent[] inputBOPLine = Node.foundNodes;

						HashMap<String, HashMap<String,String>> bomLineValues = SuperScooterTransfer.traverseSolutionItems2(mcn, inputBOMLine,  subGroupId, count, reportList);

						if(bomLineValues != null) {

							solBOMDataMap.putAll(bomLineValues);

							String BOMkey = (String) bomLineValues.keySet().toArray()[0];
							HashMap<String, String> bomMap = bomLineValues.get(BOMkey);

							if(inputBOPLine != null) {

								for(TCComponent foundLine : inputBOPLine) {

									TCComponentBOMLine bopLine = (TCComponentBOMLine)foundLine;
									operationBOMLine = (TCComponentBOMLine) bopLine.getReferenceProperty("bl_parent");
									TCComponentItemRevision operationRevision = operationBOMLine.getItemRevision();

									if(operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {

										TCHelper.getInstance().dmService.getProperties(new TCComponent[] {operationBOMLine}, new String[] {"vf4_operation_type","bl_rev_item_revision_id","vf5_line_supply_method"});

										ID = operationRevision.getPropertyDisplayableValue("item_id");

										wsList = opWs.getWorkStationsID(ID);

										if(wsList == null) {

											ArrayList<TCComponent> operations = UIGetValuesUtility.searchPartsInStruture(TCHelper.getInstance().session, new String[] {ID}, traverseStructure);

											if(operations != null) {

												for(TCComponent ops: operations) {

													String workStation = opWs.generateWorkStationID((TCComponentBOMLine)ops, "bl_rev_object_name");

													if(workStation.equals("") == false) {
														opWs.setWorkStationsID(ID, workStation);
													}
												}
											}
										}
									}else {
										SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),subGroupId, bomMap.get("PARTNO"), bomMap.get("BOMLINEID"),"Workstation information is incorrect. Please check and correct",bomMap.get("ACTION"),"Error");
										rp.setType(UpdateType.UPDATE_BODY_ERROR);
										reportList.add(rp);
										solBOMDataMap = null;
										return;
									}
								}

								String MESIndicator = operationBOMLine.getProperty("vf4_operation_type");
								if(MESIndicator.equals("NA") || MESIndicator.equals("")) {
									MESIndicator = "N";
								}else {
									MESIndicator = "Y";
								}

								String JES = operationBOMLine.getProperty("vf5_line_supply_method");
								if(JES.equals("NA") || JES.equals("")) {
									JES = "JIS";
								}

								ArrayList<String> workStations = opWs.getWorkStationsID(ID);
								ArrayList<HashMap<String,String>> BOPMap = null;

								if(workStations.isEmpty() == false) {

									BOPMap = new ArrayList<HashMap<String,String>>();

									for(String wsID : workStations) {

										HashMap<String, String> transferBOPMap = new HashMap<String, String>();
										transferBOPMap.put("SAPPLANT", mcn.getPlant());
										transferBOPMap.put("BOMLINEID", bomMap.get("BOMLINEID"));
										if(mcn.getMaterialCode().equals("")) {
											
											transferBOPMap.put("TOPLEVELPART",mcn.getPlatForm()+"_"+mcn.getModelYear());
											transferBOPMap.put("HEADERPART",mcn.getPlatForm()+"_"+mcn.getModelYear());
											
										}else {
											
											transferBOPMap.put("TOPLEVELPART",mcn.getMaterialCode());
											transferBOPMap.put("HEADERPART",mcn.getMaterialCode());
										}
										
										transferBOPMap.put("ACTION", "A");
										transferBOPMap.put("WORKSTATION", wsID);
										transferBOPMap.put("LINESUPPLYMETHOD", JES);
										transferBOPMap.put("BOPID", ID);
										transferBOPMap.put("MESBOPINDICATOR", MESIndicator);
										transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
										transferBOPMap.put("REVISION", operationBOMLine.getProperty("bl_rev_item_revision_id"));
										BOPMap.add(transferBOPMap);
									}

									solBOPDataMap.put(BOMkey, BOPMap);
								}
							}else {
								SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),subGroupId, bomMap.get("PARTNO"), bomMap.get("BOMLINEID"),"BOP is wrong. Please correct BOM and BOP link",bomMap.get("ACTION"),"Error");
								rp.setType(UpdateType.UPDATE_BODY_ERROR);
								reportList.add(rp);
							}
						}else {
							solBOPDataMap = null;
							return;
						}
					}
				}
			}
		}
	}
	


}
