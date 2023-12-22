package com.vinfast.car.sap.unitbom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.dialogs.BOMBOPDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.BOMCompareReport;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class UnitShopMA extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision changeObject = null;
	private TCComponentItemRevision obj_shopTopNode = null;
	private TCComponentItemRevision obj_Shop = null;
	private DataManagementService dmService;
	private BOMBOPDialog dlg;
	File logFile = null;
	OrganizationInformationAbstract serverInfo = null;
	boolean IS_SEND_BOP_ONLY = true;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select one MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(changeObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			dmService = DataManagementService.getService(session);

			String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
			TCComponent[] impactedShop = changeObject.getRelatedComponents(PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop.length == 0 || impactedShop.length > 1) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			obj_Shop = (TCComponentItemRevision) impactedShop[0];
			obj_shopTopNode = UIGetValuesUtility.getTopLevelItemRevision(session, obj_Shop, PropertyDefines.REVISION_RULE_WORKING);
			String err = UIGetValuesUtility.checkValidShop(obj_Shop, plant);
			if (!err.isEmpty()) {
				MessageBox.post(err, "Error", MessageBox.ERROR);
				return null;
			}

			String platform = "";
			String modelYear = "";

			String mainGroup = obj_Shop.getProperty("object_name");

			if (obj_shopTopNode != null) {
				MaterialPlatformCode platformCode = UIGetValuesUtility.getPlatformCode(dmService, obj_shopTopNode);
				if ((platformCode.getPlatformCode().equals("") || platformCode.getModelYear().equals(""))) {
					platform = obj_shopTopNode.getPropertyDisplayableValue("item_id");
					modelYear = obj_shopTopNode.getPropertyDisplayableValue("object_name");
				} else {
					platform = platformCode.getPlatformCode();
					modelYear = platformCode.getModelYear();
				}
			} else {
				obj_shopTopNode = obj_Shop;
			}

			dlg = new BOMBOPDialog(new Shell());
			dlg.create();
			dlg.setTitle("Unit BOP - MA Shop - Transfer");
			dlg.setModel(platform);
			dlg.setYear(modelYear);
			dlg.setShop(mainGroup);
			dlg.setPlant(plant);
			dlg.setServer("PRODUCTION");
			dlg.setMCN(MCN);
			Button transferBtn = dlg.getTransferButton();
			transferBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferBtn.setEnabled(false);
					String serverType = dlg.cbServer.getText();
					serverInfo = OrganizationInformationFactory.generateOrganizationInformation(event.getCommand().toString(), serverType, session);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							loadTransferRecords();
//								String excelFilePath = "D:\\pin\\BOP_phantom.xlsx";
//								transferByCodeBOP(dlg,session, changeObject,excelFilePath);
							dlg.getShell().dispose();
						}
					});
				}
			});
			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void loadTransferRecords() {
		BOMCompareReport bomcompare = new BOMCompareReport();
		new Logger();
		SAPURL SAPConnect = new SAPURL();
		UnitTransfer unitObj = new UnitTransfer();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = session.getUserName();
		LocalDateTime now = LocalDateTime.now();
		String setValue = String.format("%s~%s~%s", dlg.getServer(), userName, dtf.format(now));

		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();
		try {
			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
			if (impactedItems == null || impactedItems.length == 0) {
				MessageBox.post("No Items in Impacted Items folder to transfer to SAP.", "Error", MessageBox.ERROR);
				return;
			}

			String serverValue = dlg.cbServer.getText();
			String shopValue = dlg.txtShop.getText();
			String modelValue = dlg.txtModel.getText();
			String yearValue = dlg.txtYear.getText();
			String plantValue = dlg.txtPlant.getText();
			String mcnIDValue = dlg.txtMCN.getText();
			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnIDValue);

			TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);
			int problemCount = problemItems != null ? problemItems.length : 0;

			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);
			int solutionCount = solutionItems != null ? solutionItems.length : 0;

			int count = 1;
			int percentage = 1;
			int total = impactedItems.length + problemCount + solutionCount + 3;

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<html><body>");
			String[] printValues = new String[] { "Model : " + modelValue + "_" + yearValue, "Shop : " + shopValue, "User : " + session.getUserName(), "Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
			Logger.bufferResponse("DETAILS", printValues, strBuilder);

			printValues = new String[] { "S.No", "Sub Group", "Record", "BOMLine ID", "Message", "Action", "Result" };
			Logger.bufferResponse("HEADER", printValues, strBuilder);

			ArrayList<String> transferIDs = new ArrayList<String>();
			for (TCComponent impactedItem : impactedItems) {
				String itemID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String itemRevID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);

				if (serverValue.equals("PRODUCTION") || serverValue.equals("QA")) {
					String transferToSAP = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
					if (transferToSAP.isEmpty() || !transferToSAP.contains(serverValue)) {
						transferIDs.add(itemID);
					} else {
						printValues = new String[] { Integer.toString(count), itemID + "/" + itemRevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
						Logger.bufferResponse("PRINT", printValues, strBuilder);
						percentage++;
						count++;
					}
				} else {
					transferIDs.add(itemID);
				}
			}

			TCComponent traverseStructure = null;
			OpenContextInfo[] createdBOMViews = null;
			boolean attachLog = true;
			if (transferIDs.isEmpty() == false) {
				TCComponent BOMLine = null;
				TCComponent BOPLine = null;
				createdBOMViews = UIGetValuesUtility.createContextViews(session, obj_shopTopNode.getItem());
				percentage++;
				for (OpenContextInfo views : createdBOMViews) {
					if (views.context.getType().equals("BOMLine")) {
						BOMLine = views.context;
					}
					if (views.context.getType().equals("Mfg0BvrPlantBOP")) {
						BOPLine = views.context;
					}
				}

				if (BOMLine == null || BOPLine == null) {
					MessageBox.post("BOM and BOP linked error.", "Please check BOM and BOP is linked or Contact Teamcenter Admin.", "Link...", MessageBox.ERROR);
					return;
				}

				TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem().getRelatedComponent("IMAN_master_form");
				String searchPlantModel = topLineMasterForm.getProperty("user_data_2");

				if (!searchPlantModel.isEmpty()) {
					ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchPlantModel }, BOPLine);
					if (!plantModelList.isEmpty()) {
						traverseStructure = plantModelList.get(0);
						UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
					} else {
						if (BOPLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(searchPlantModel) == false) {
							TCComponentItem plantModel = UIGetValuesUtility.findItem(session, searchPlantModel);
							OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
							BOPLine = createdBOPView[0].context;
							UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
						}

						traverseStructure = BOPLine;
					}
				} else {
					UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
					traverseStructure = BOPLine;
				}

				percentage++;

				ArrayList<TCComponent> searchLines = null;

				String impactShopID = obj_Shop.getProperty("item_id");

				if (BOMLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(impactShopID)) {
					searchLines = new ArrayList<TCComponent>();
					searchLines.add(BOMLine);
				} else {
					searchLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { impactShopID }, BOMLine);
				}

				if (searchLines != null) {
					String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");
					ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchIDs }, searchLines.get(0));
					percentage++;

					String[] properties = new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_ITEM_REV_ID };

					TCComponent[] impactedBOMLines = new TCComponent[impactLines.size()];
					for (int i = 0; i < impactLines.size(); i++) {
						impactedBOMLines[i] = impactLines.get(i);
					}

					dmService.getProperties(impactedBOMLines, properties);

					for (TCComponent impactedLine : impactLines) {
						boolean isTransferedToSAP = false;
						String ID = impactedLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						String RevID = impactedLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);

						TCComponentBOMLine solutionItem = (TCComponentBOMLine) impactedLine;
						TCComponentItemRevision solution_Rev = solutionItem.getItemRevision();
						TCComponentItemRevision problemItemRevision = null;
						int revID = Integer.parseInt(RevID);
						
						if (revID > 1) {
							String search_rev = Integer.toString(revID - 1);
							for (int i = search_rev.length(); i < RevID.length(); i++) {
								search_rev = "0" + search_rev;
							}

							problemItemRevision = solution_Rev.findRevision(search_rev);
							if (problemItemRevision != null) {
								CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problemItemRevision);
								TCComponentBOMLine problemItem = output[0].bomLine;

								boolean IOValues = bomcompare.inputOutputMap(session, problemItem, solutionItem, count, strBuilder);
								if (IOValues == true) {
									boolean isError = false;
									HashMap<String, TCComponent> prob_P_Output = bomcompare.getProblemOutputItems();
									HashMap<String, TCComponent> sol_P_Output = bomcompare.getSolutionOutputItems();
									HashMap<String, TCComponent> prob_C_Input = bomcompare.getProblemInputItems();
									HashMap<String, TCComponent> sol_C_Input = bomcompare.getSolutionInputItems();
									// Parent
									// DRYRUN prob_P_Output.clear();
									prob_P_Output.clear();
									HashMap<String, TCComponent> add_Output_parts = bomcompare.addPart(sol_P_Output, prob_P_Output);
									HashMap<String, TCComponent> del_Output_parts = bomcompare.delPart(sol_P_Output, prob_P_Output);

									// Child
									// DRYRUN prob_C_Input.clear();
									prob_C_Input.clear();
									HashMap<String, TCComponent> add_Input_parts = bomcompare.addPart(sol_C_Input, prob_C_Input);
									HashMap<String, TCComponent> del_Input_parts = bomcompare.delPart(sol_C_Input, prob_C_Input);

									if ((add_Output_parts.isEmpty() && del_Output_parts.isEmpty() && add_Input_parts.isEmpty() && del_Input_parts.isEmpty()) == false) {
										// DELETE (PROBLEM ITEMS)
										HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = new HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
										Set<String> del_Input_keys = del_Input_parts.keySet();
										for (String delLine : del_Input_keys) {
											TCComponent del_Input_Part = del_Input_parts.get(delLine);
											Set<String> del_Out_keys = prob_P_Output.keySet();
											for (String delOutputLine : del_Out_keys) {
												TCComponent del_Ouput_Part = prob_P_Output.get(delOutputLine);
												HashMap<String, String> transferBOMMap = new HashMap<String, String>();

												dmService.getProperties(new TCComponent[] { del_Ouput_Part, del_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });
												transferBOMMap.put("MCN", mcnIDValue);
												transferBOMMap.put("PLANTCODE", plantValue);
												transferBOMMap.put("ACTION", "D");
												transferBOMMap.put("PARENTPART", del_Ouput_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
												transferBOMMap.put("CHILDPART", del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
												String BOMLineID = del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);

												if (BOMLineID.length() != 0) {
													for (int i = BOMLineID.length(); i < 4; i++) {
														BOMLineID = "0" + BOMLineID;
													}
												}
												transferBOMMap.put("BOMLINEID", BOMLineID.trim());

												String quantity = del_Input_Part.getPropertyDisplayableValue("bl_quantity");
												if (quantity.length() == 0) {
													quantity = "1.000";
												}

												transferBOMMap.put("QUANTITY", quantity);
												transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
												UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) del_Input_Part, transferBOMMap);

												transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
											}
											prob_C_Input.remove(delLine);
										}

										Set<String> del_Output_keys = del_Output_parts.keySet();

										for (String delLine : del_Output_keys) {
											TCComponent del_Part = del_Output_parts.get(delLine);
											Set<String> del_input_keys = prob_C_Input.keySet();
											for (String delInputLine : del_input_keys) {
												TCComponent del_Input_Part = prob_C_Input.get(delInputLine);
												HashMap<String, String> transferBOMMap = new HashMap<String, String>();

												dmService.getProperties(new TCComponent[] { del_Part, del_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });

												transferBOMMap.put("MCN", mcnIDValue);
												transferBOMMap.put("PLANTCODE", plantValue);
												transferBOMMap.put("ACTION", "D");
												transferBOMMap.put("PARENTPART", del_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
												transferBOMMap.put("CHILDPART", del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
												String BOMLineID = del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
												if (BOMLineID.length() != 0) {
													for (int i = BOMLineID.length(); i < 4; i++) {
														BOMLineID = "0" + BOMLineID;
													}
												}
												transferBOMMap.put("BOMLINEID", BOMLineID.trim());

												String quantity = del_Input_Part.getPropertyDisplayableValue("bl_quantity");
												if (quantity.length() == 0) {
													quantity = "1.000";
												}

												transferBOMMap.put("QUANTITY", quantity);
												transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
												UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) del_Input_Part, transferBOMMap);

												transferValues.put(transferBOMMap, new ArrayList<HashMap<String, String>>());
											}
											prob_P_Output.remove(delLine);
										}

										Collection<TCComponent> values = add_Input_parts.values();
										NodeInfo[] BOPLinkedLines = null;

										if (values.isEmpty() == false) {
											TCComponent[] inputLines = new TCComponent[values.size()];
											Iterator<TCComponent> iterator = values.iterator();
											int itr = 0;
											while (iterator.hasNext()) {
												inputLines[itr] = iterator.next();
												itr++;
											}

											BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);
											if (BOPLinkedLines != null) {
												for (NodeInfo addLine : BOPLinkedLines) {
													TCComponent add_Input_Part = addLine.originalNode;
													TCComponent[] foundBOPNode = addLine.foundNodes;

													String key = "";
													Set<String> add_Out_keys = sol_P_Output.keySet();

													for (String addOutputLine : add_Out_keys) {
														TCComponent add_Ouput_Part = sol_P_Output.get(addOutputLine);
														// BOM MAP
														HashMap<String, String> transferBOMMap = new HashMap<String, String>();
														dmService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });

														String parentID = add_Ouput_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
														String childID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
														transferBOMMap.put("MCN", mcnIDValue);
														transferBOMMap.put("PLANTCODE", plantValue);
														transferBOMMap.put("ACTION", "A");
														transferBOMMap.put("PARENTPART", parentID);
														transferBOMMap.put("CHILDPART", childID);
														UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) add_Input_Part, transferBOMMap);
														String BOMLineID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);

														key = childID + "~" + BOMLineID + "~Input";

														if (BOMLineID.length() != 0) {
															for (int i = BOMLineID.length(); i < 4; i++) {
																BOMLineID = "0" + BOMLineID;
															}
														}
														transferBOMMap.put("BOMLINEID", BOMLineID.trim());

														String quantity = add_Input_Part.getPropertyDisplayableValue("bl_quantity");
														if (quantity.length() == 0) {
															quantity = "1.000";
														}

														transferBOMMap.put("QUANTITY", quantity);
														transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
														for (TCComponent bop : foundBOPNode) {
															// BOP MAP
															HashMap<String, String> transferBOPMap = new HashMap<String, String>();
															transferBOPMap.put("SAPPLANT", plantValue);
															transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
															transferBOPMap.put("TOPLEVELPART", parentID);
															transferBOPMap.put("HEADERPART", parentID);
															transferBOPMap.put("ACTION", "A");
															transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

															// DRYRUN if (foundBOPNode == null || foundBOPNode.length ==
															// 0) continue;
															HashMap<String, String> dataMap = unitObj.transferBOP(dmService, bop, transferBOMMap, ID, count, strBuilder);
															if (dataMap != null) {
																transferBOPMap.putAll(dataMap);
																transferBOPMaps.add(transferBOPMap);
															} else {
																isError = true;
																attachLog = false;
															}
														}

														transferValues.put(transferBOMMap, transferBOPMaps);

													}
													sol_C_Input.remove(key);
												}
											}
										}

										Set<String> add_Output_keys = add_Output_parts.keySet();

										if (add_Output_keys.isEmpty() == false) {
											values = sol_C_Input.values();
											if (values.isEmpty() == false) {
												TCComponent[] inputLines = new TCComponent[values.size()];
												Iterator<TCComponent> iterator = values.iterator();
												int itr = 0;
												while (iterator.hasNext()) {
													inputLines[itr] = iterator.next();
													itr++;
												}

												BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);

												if (BOPLinkedLines != null) {

													for (String addLine : add_Output_keys) {

														TCComponent add_Ouput_Part = add_Output_parts.get(addLine);

														for (NodeInfo addInputLine : BOPLinkedLines) {

															TCComponent add_Input_Part = addInputLine.originalNode;
															TCComponent[] foundBOPNode = addInputLine.foundNodes;

															dmService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });

															HashMap<String, String> transferBOMMap = new HashMap<String, String>();
															dmService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });

															String parentID = add_Ouput_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
															String childID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);

															transferBOMMap.put("MCN", mcnIDValue);
															transferBOMMap.put("PLANTCODE", plantValue);
															transferBOMMap.put("ACTION", "A");
															transferBOMMap.put("PARENTPART", parentID);
															transferBOMMap.put("CHILDPART", childID);
															
															UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) add_Input_Part, transferBOMMap);
															String BOMLineID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);

															if (BOMLineID.length() != 0) {
																for (int i = BOMLineID.length(); i < 4; i++) {
																	BOMLineID = "0" + BOMLineID;
																}
															}
															transferBOMMap.put("BOMLINEID", BOMLineID.trim());

															String quantity = add_Input_Part.getPropertyDisplayableValue("bl_quantity");
															if (quantity.length() == 0) {
																quantity = "1.000";
															}

															transferBOMMap.put("QUANTITY", quantity);
															transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

															// BOP MAP
															ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
															for (TCComponent bop : foundBOPNode) {
																HashMap<String, String> transferBOPMap = new HashMap<String, String>();
																transferBOPMap.put("SAPPLANT", plantValue);
																transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
																transferBOPMap.put("TOPLEVELPART", parentID);
																transferBOPMap.put("HEADERPART", parentID);
																transferBOPMap.put("ACTION", "A");
																transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

																// DRYRUN if (foundBOPNode == null ||
																// foundBOPNode.length == 0) continue;
																HashMap<String, String> dataMap = unitObj.transferBOP(dmService, bop, transferBOMMap, ID, count, strBuilder);

																if (dataMap != null) {
																	transferBOPMap.putAll(dataMap);
																	transferBOPMaps.add(transferBOPMap);
																} else {
																	isError = true;
																	attachLog = false;
																}
															}
															transferValues.put(transferBOMMap, transferBOPMaps);
														}
														sol_P_Output.remove(addLine);
													}
												}
											}
										}

										UIGetValuesUtility.closeWindow(session, output[0].bomWindow);

										if (transferValues.isEmpty() == false && isError == false) {
											isTransferedToSAP = true;
											for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : transferValues.entrySet()) {
												HashMap<String, String> bomMap = entry.getKey();
												ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
												
												if (bomMap.isEmpty() == false) {
													String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
													String [] message = null;
													if (!IS_SEND_BOP_ONLY) {
														message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
													} else {
														message = new String[] {"S", "DRYRUN"};;
													}


													if (!IS_SEND_BOP_ONLY && (message[0].equals("E") && !message[1].contains("Record is exist in staging table"))) {
														printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														isTransferedToSAP = false;
														attachLog = false;
													} else {
														printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);

														for (HashMap<String, String> bopMap : bopMaps) {
															if (bopMap.isEmpty() == false) {
																String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
																String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
																if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
																	printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
																	Logger.bufferResponse("PRINT", printValues, strBuilder);
																	isTransferedToSAP = false;
																	attachLog = false;
																} else {
																	printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
																	Logger.bufferResponse("PRINT", printValues, strBuilder);
																}
															}
														}
													}
													count++;
												}
											}
										}
									} else {
										printValues = new String[] { Integer.toString(count), ID + "/" + RevID, "", "", "No change in old revision and current revision", "", "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										attachLog = false;
									}
								}
							}
						} else {
							HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> transferValues = new HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
							boolean IOValues = bomcompare.inputOutputMap(session, null, solutionItem, count, strBuilder);
							if (IOValues == true) {
								boolean isError = false;
								HashMap<String, TCComponent> sol_P_Output = bomcompare.getSolutionOutputItems();
								HashMap<String, TCComponent> sol_C_Input = bomcompare.getSolutionInputItems();

								// ADD
								Collection<TCComponent> values = sol_C_Input.values();
								NodeInfo[] BOPLinkedLines = null;

								if (values.isEmpty() == false) {
									TCComponent[] inputLines = new TCComponent[values.size()];
									Iterator<TCComponent> iterator = values.iterator();
									int itr = 0;
									while (iterator.hasNext()) {
										inputLines[itr] = iterator.next();
										itr++;
									}

									BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOP(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);
									if (BOPLinkedLines != null) {
										for (NodeInfo addLine : BOPLinkedLines) {
											TCComponent add_Input_Part = addLine.originalNode;
											TCComponent[] foundBOPNode = addLine.foundNodes;

											String key = "";
											Set<String> add_Out_keys = sol_P_Output.keySet();

											for (String addOutputLine : add_Out_keys) {
												TCComponent add_Ouput_Part = sol_P_Output.get(addOutputLine);
												// BOM MAP
												HashMap<String, String> transferBOMMap = new HashMap<String, String>();
												dmService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID, "bl_quantity" });

												String parentID = add_Ouput_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
												String childID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);

												transferBOMMap.put("MCN", mcnIDValue);
												transferBOMMap.put("PLANTCODE", plantValue);
												transferBOMMap.put("ACTION", "A");
												transferBOMMap.put("PARENTPART", parentID);
												transferBOMMap.put("CHILDPART", childID);
												UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) add_Input_Part, transferBOMMap);
												String BOMLineID = add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);

												key = childID + "~" + BOMLineID + "~Input";

												if (BOMLineID.length() != 0) {
													for (int i = BOMLineID.length(); i < 4; i++) {
														BOMLineID = "0" + BOMLineID;
													}
												}
												transferBOMMap.put("BOMLINEID", BOMLineID);

												String quantity = add_Input_Part.getPropertyDisplayableValue("bl_quantity");

												if (quantity.length() == 0) {
													quantity = "1.000";
												}

												transferBOMMap.put("QUANTITY", quantity);
												transferBOMMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

												// BOP MAP
												ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
												for (TCComponent bop : foundBOPNode) {
													HashMap<String, String> transferBOPMap = new HashMap<String, String>();
													transferBOPMap.put("SAPPLANT", plantValue);
													transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
													transferBOPMap.put("TOPLEVELPART", parentID);
													transferBOPMap.put("HEADERPART", parentID);
													transferBOPMap.put("ACTION", "A");
													transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

													// DRYRUN if (foundBOPNode == null || foundBOPNode.length == 0)
													// continue;
													HashMap<String, String> dataMap = unitObj.transferBOP(dmService, bop, transferBOMMap, ID, count, strBuilder);

													if (dataMap != null) {
														transferBOPMap.putAll(dataMap);
														transferBOPMaps.add(transferBOPMap);
													} else {
														isError = true;
														attachLog = false;
													}
												}
												transferValues.put(transferBOMMap, transferBOPMaps);
											}
											sol_C_Input.remove(key);
										}
									}
								}

								if (transferValues.isEmpty() == false && isError == false) {
									isTransferedToSAP = true;
									for (Entry<HashMap<String, String>, ArrayList<HashMap<String, String>>> entry : transferValues.entrySet()) {
										HashMap<String, String> bomMap = entry.getKey();
										ArrayList<HashMap<String, String>> bopMaps = entry.getValue();
										if (bomMap.isEmpty() == false) {
//											String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
//											SOAPMessage BOMSoapRequest = request.createRequest(bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
//											String[] message = ins.callWebService(BOMSoapRequest, SAPConnect.assybomWebserviceURL(SERVER_IP), "O_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder);
//
//											if (message[0].equals("E") && !message[1].contains("Record is exist in staging table")) {
//												printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
//												log.bufferResponse("PRINT", printValues, strBuilder);
//												isTransferedToSAP = false;
//												attachLog = false;
//											} else {
											{
												String[] message = new String[] {"S", "DRYRUN"};;
												printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
												Logger.bufferResponse("PRINT", printValues, strBuilder);
												for (HashMap<String, String> bopMap : bopMaps) {
													if (bopMap.isEmpty() == false) {
														String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
														String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
														if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
															printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
															Logger.bufferResponse("PRINT", printValues, strBuilder);
															isTransferedToSAP = false;
															attachLog = false;
														} else {
															printValues = new String[] { Integer.toString(count), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Success" };
															Logger.bufferResponse("PRINT", printValues, strBuilder);
														}
													}
												}
											}
											count++;
										}
									}
								}
							}
						}
						percentage++;
						if (isTransferedToSAP == true) {
							if (dlg.getServer().equals("PRODUCTION") || dlg.getServer().equals("QA")) {
								UIGetValuesUtility.setProperty(dmService, solution_Rev, "vf3_transfer_to_sap", setValue);
							}
						}
						percentage++;
					}
				}
			}

			if (problemItems != null) {
				for (TCComponent problemItem : problemItems) {
					TCComponentItemRevision problem = (TCComponentItemRevision) problemItem;
					CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
					TCComponentBOMWindow operationWindow = output[0].bomWindow;
					TCComponentBOMLine operationLine = output[0].bomLine;

					if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
						dmService.getProperties(new TCComponent[] { operationLine }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_ITEM_REV_ID, PropertyDefines.BOM_BOM_ID, "vf4_operation_type", "bl_rev_vf3_transfer_to_sap", "bl_rev_vf5_line_supply_method" });

						String ID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						String RevID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
						String HT = operationLine.getItemRevision().getProperty("vf4_reference_component");

						if (HT.equals("")) {
							printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "On Operation \"Reference Component\" column Header Part Information is missing", "-", "Error" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							count++;
						} else {
							String WS = UIGetValuesUtility.getWorkStation(new String[] { "vf3_transfer_to_mes", "vf4_user_notes" }, problem);
							String[] split = WS.split(",");

							String LSM = operationLine.getPropertyDisplayableValue("bl_rev_vf5_line_supply_method");
							if (LSM.equals("")) {
								LSM = "JIS";
							}

							String MES = operationLine.getPropertyDisplayableValue("vf4_operation_type");
							if (MES.equals("") || MES.equals("NA")) {
								MES = "N";
							} else {
								MES = "Y";
							}

							for (String workstation : split) {
								HashMap<String, String> BOP_Values = new HashMap<String, String>();
								BOP_Values.put("SAPPLANT", plantValue);
								BOP_Values.put("TOPLEVELPART", HT);
								BOP_Values.put("HEADERPART", HT);
								BOP_Values.put("BOMLINEID", "");
								BOP_Values.put("ACTION", "D");
								BOP_Values.put("WORKSTATION", workstation);
								BOP_Values.put("LINESUPPLYMETHOD", LSM);
								BOP_Values.put("BOPID", ID);
								BOP_Values.put("MESBOPINDICATOR", MES);
								BOP_Values.put("REVISION", RevID);
								BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

								String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
								String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
								if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
									printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Error" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
									attachLog = false;
								} else {
									printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Success" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);

									if (dlg.getServer().equals("PRODUCTION") || dlg.getServer().equals("QA")) {
										UIGetValuesUtility.setProperty(dmService, problem, "vf3_transfer_to_sap", setValue);
									}
								}
								count++;
							}
						}
					}
					percentage++;
					operationWindow.close();
				}
			}

			if (solutionItems != null) {
				if (traverseStructure == null) {
					TCComponent BOMLine = null;
					TCComponent BOPLine = null;
					createdBOMViews = UIGetValuesUtility.createContextViews(session, obj_shopTopNode.getItem());
					percentage++;
					for (OpenContextInfo views : createdBOMViews) {
						if (views.context.getType().equals("BOMLine")) {
							BOMLine = views.context;
						}
						if (views.context.getType().equals("Mfg0BvrPlantBOP")) {
							BOPLine = views.context;
						}
					}

					TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem().getRelatedComponent("IMAN_master_form");
					String searchPlantModel = topLineMasterForm.getProperty("user_data_2");

					if (searchPlantModel.length() != 0) {
						ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchPlantModel }, BOPLine);
						if (!plantModelList.isEmpty()) {
							traverseStructure = plantModelList.get(0);
							UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
						} else {
							if (BOPLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(searchPlantModel) == false) {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(session, searchPlantModel);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
								BOPLine = createdBOPView[0].context;
								UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
							}
							traverseStructure = BOPLine;
						}
					} else {
						UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
						traverseStructure = BOPLine;
					}

					percentage++;
				}

				ArrayList<String> operationIDs = new ArrayList<String>();
				for (TCComponent solutionItem : solutionItems) {
					TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;
					operationIDs.add(operations.getProperty("item_id"));
				}
				String searchIDs = UIGetValuesUtility.convertArrayToString(operationIDs, ";");
				TCComponent[] operationsList = UIGetValuesUtility.searchStruture(session, searchIDs, traverseStructure);
				if (operationsList != null && operationsList.length > 0) {
					for (TCComponent operationItem : operationsList) {
						TCComponentBOMLine operationLine = (TCComponentBOMLine) operationItem;
						TCComponentItemRevision operationRevision = operationLine.getItemRevision();

						if (UIGetValuesUtility.hasMaterials(operationLine) == false) {
							dmService.getProperties(new TCComponent[] { operationLine }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_ITEM_REV_ID, PropertyDefines.BOM_BOM_ID, "vf4_operation_type", "bl_rev_vf3_transfer_to_sap", "bl_rev_vf5_line_supply_method", "vf4_reference_component" });
							String ID = operationLine.getProperty(PropertyDefines.BOM_ITEM_ID);
							String RevID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
							String HT = operationLine.getPropertyDisplayableValue("vf4_reference_component");

							if (HT.equals("")) {
								printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "On Operation \"Reference Component\" column Header Part Information is missing", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							} else {
								String LSM = operationLine.getPropertyDisplayableValue("bl_rev_vf5_line_supply_method");
								if (LSM.equals("")) {
									LSM = "JIS";
								}

								String MES = operationLine.getPropertyDisplayableValue("vf4_operation_type");
								if (MES.equals("") || MES.equals("NA")) {
									MES = "N";
								} else {
									MES = "Y";
								}
								HashMap<String, String> BOP_Values = new HashMap<String, String>();
								BOP_Values.put("SAPPLANT", plantValue);
								BOP_Values.put("TOPLEVELPART", HT);
								BOP_Values.put("HEADERPART", HT);
								BOP_Values.put("BOMLINEID", "");
								BOP_Values.put("ACTION", "A");
								BOP_Values.put("WORKSTATION", UIGetValuesUtility.getWorkStationID(operationLine, "bl_rev_object_name"));
								BOP_Values.put("LINESUPPLYMETHOD", LSM);
								BOP_Values.put("BOPID", ID);
								BOP_Values.put("MESBOPINDICATOR", MES);
								BOP_Values.put("REVISION", RevID);
								BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());

								String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
								String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
								if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
									printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Error" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
									attachLog = false;
								} else {
									printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Success" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
									if (dlg.getServer().equals("PRODUCTION") || dlg.getServer().equals("QA")) {
										UIGetValuesUtility.setProperty(dmService, operationRevision, "vf3_transfer_to_sap", setValue);
									}
								}
								count++;
							}
						}
					}
					percentage++;
				}
			}

			if (createdBOMViews != null) {
				UIGetValuesUtility.closeAllContext(session, createdBOMViews);
			}

			strBuilder.append("</table>");
			String data = Logger.previousTransaction(logFolder, "BOM");
			if (!data.equals("")) {
				strBuilder.append("<br>");
				strBuilder.append(data);
			}
			strBuilder.append("</body></html>");
			logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "BOM");
			if (attachLog == true && dlg.getServer().equals("PRODUCTION") == true) {
				TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmService, changeObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
				if (newDataset != null) {
					UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
				}
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						StringViewerDialog viewdialog = new StringViewerDialog(logFile);
						viewdialog.setSize(600, 400);
						viewdialog.setLocationRelativeTo(null);
						viewdialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		if (targetComponents[0] instanceof TCComponentItemRevision)
			changeObject = (TCComponentItemRevision) targetComponents[0];
		if (changeObject == null)
			return true;
		return false;
	}
}
