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
import com.teamcenter.soaictstubs.ICCTAccessControlService;
import com.teamcenter.soaictstubs.booleanSeq_tHolder;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.dialogs.PhantomDialog;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.BOMCompareReport;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class UnitPhantom extends AbstractHandler {

	TCComponentItemRevision obj_shopTopNode = null;
	TCComponentItemRevision obj_Shop = null;
	private ICCTAccessControlService accessService = null;
	File logFile = null;
	OrganizationInformationAbstract serverInfo = null;
	String cmd = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {

			ISelection selection = HandlerUtil.getCurrentSelection(event);

			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			TCComponentItemRevision changeObject = (TCComponentItemRevision) selectedObjects[0];

			TCSession clientSession = changeObject.getSession();

			accessService = new ICCTAccessControlService(clientSession.getSoaConnection());

			booleanSeq_tHolder accessHolder = new booleanSeq_tHolder();

			accessService.checkPrivileges(changeObject.getUid(), new String[] { "WRITE" }, accessHolder);

			cmd = event.getCommand().toString();

			boolean values[] = accessHolder.value;

			if (values[0] == false) {

				MessageBox.post("You do not have access on selected MCN to transfer.", "Error", MessageBox.ERROR);

			} else {

				DataManagementService dmCoreService = DataManagementService.getService(clientSession);

				dmCoreService.getProperties(new TCComponent[] { changeObject }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ECM_PLANT, PropertyDefines.REL_IMPACT_SHOP });

				String plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);

				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);

				String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);

				TCComponent[] impactedShop = changeObject.getRelatedComponents(PropertyDefines.REL_IMPACT_SHOP);

				if (impactedShop.length == 0 || impactedShop.length > 1) {

					MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
					return null;

				} else {

					obj_Shop = (TCComponentItemRevision) impactedShop[0];

					obj_shopTopNode = UIGetValuesUtility.getTopLevelItemRevision(clientSession, obj_Shop, PropertyDefines.REVISION_RULE_WORKING);

					String err = UIGetValuesUtility.checkValidShop(obj_Shop, plant);

					if (!err.isEmpty()) {
						MessageBox.post(err, "Error", MessageBox.ERROR);
						return null;
					}
				}

				String platform = "";

				String mainGroup = obj_Shop.getProperty("object_name");

				if (obj_shopTopNode != null) {

					MaterialPlatformCode platformCode = UIGetValuesUtility.getPlatformCode(dmCoreService, obj_shopTopNode);

					if ((platformCode.getPlatformCode().equals("") || platformCode.getModelYear().equals(""))) {
						platform = obj_shopTopNode.getPropertyDisplayableValue("item_id");
					} else {

						platform = platformCode.getPlatformCode();
					}
				} else {

					obj_shopTopNode = obj_Shop;

				}

				String rootMaterial = getRootMaterial(obj_shopTopNode);

				PhantomDialog transferDlg = new PhantomDialog(new Shell());

				transferDlg.create();

				transferDlg.setTitle("Unit BOM/BOP (Phantom) Transfer");

				transferDlg.setModel(platform);

				transferDlg.setShop(mainGroup);

				transferDlg.setPlant(plant);

				transferDlg.setRootMaterial(rootMaterial);

				transferDlg.setServer("PRODUCTION");

				transferDlg.setMCN(MCN);

				Button transferBtn = transferDlg.getOkButton();

				transferBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event e) {

						transferBtn.setEnabled(false);

						String serverValue = transferDlg.comboServer.getText();

						serverInfo = OrganizationInformationFactory.generateOrganizationInformation(cmd, serverValue, clientSession);

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {

								if (transferDlg.getRootMaterial().isEmpty()) {
									MessageBox.post("Please fill Root Material before transfer.", "Error", MessageBox.ERROR);
								} else {
									loadTransferRecords(transferDlg, clientSession, changeObject);
								}
								transferDlg.getShell().dispose();
							}
						});
					}
				});
				transferDlg.open();

			}

		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void loadTransferRecords(PhantomDialog transferDlg, TCSession session, TCComponentItemRevision changeObject) {

		BOMCompareReport bomcompare = new BOMCompareReport();
		new Logger();
		SAPURL SAPConnect = new SAPURL();
		UnitTransfer unitObj = new UnitTransfer();

		DataManagementService dmCoreService = DataManagementService.getService(session);
		String SERVER_IP = serverInfo.getServerIP();
		String auth = serverInfo.getAuth();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String userName = session.getUserName();

		try {

			String plant = transferDlg.getPlant();

			String MCN_SAPID = transferDlg.getMCN();

			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + MCN_SAPID);

			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);

			TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);

			int problemCount = 0;
			if (problemItems != null) {

				problemCount = problemItems.length;
			}

			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);

			int solutionCount = 0;

			if (solutionItems != null) {

				solutionCount = solutionItems.length;
			}

			if (impactedItems != null) {

				int count = 1;
				int percentage = 1;
				int total = impactedItems.length + problemCount + solutionCount + 3;

				StringBuilder strBuilder = new StringBuilder();

				strBuilder.append("<html><body>");

				String[] printValues = new String[] { "Model : " + transferDlg.getModel(), "Shop : " + transferDlg.getShop(), "User : " + session.getUserName(), "Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
				Logger.bufferResponse("DETAILS", printValues, strBuilder);

				printValues = new String[] { "S.No", "Sub Group", "Record", "BOMLine ID", "Message", "Action", "Result" };
				Logger.bufferResponse("HEADER", printValues, strBuilder);

				dmCoreService.getProperties(impactedItems, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ITEM_REV_ID, PropertyDefines.REV_TO_SAP });

				ArrayList<String> transferIDs = new ArrayList<String>();

				for (TCComponent impactedItem : impactedItems) {

					String ID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String RevID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
					String transferToSAP = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);

					if (transferDlg.getServer().equals("PRODUCTION")) {

						if (transferToSAP.length() == 0) {

							transferIDs.add(ID);

						} else {

							transferDlg.setProgressStatus(getProgress(percentage, total));
							printValues = new String[] { Integer.toString(count), ID + "/" + RevID, "", "", "Sub Group already transferred to SAP.", "", "Info" };
							Logger.bufferResponse("PRINT", printValues, strBuilder);
							percentage++;
							count++;
						}

					} else {

						transferIDs.add(ID);
					}
				}

				TCComponent traverseStructure = null;
				OpenContextInfo[] createdBOMViews = null;
				boolean attachLog = true;
				if (transferIDs.isEmpty() == false) {

					TCComponent BOMLine = null;
					TCComponent BOPLine = null;

					createdBOMViews = UIGetValuesUtility.createContextViews(session, obj_shopTopNode.getItem());

					transferDlg.setProgressStatus(getProgress(percentage, total));
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

					if (searchPlantModel.length() != 0) {

						ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchPlantModel }, BOPLine);
						if (!plantModelList.isEmpty()) {

							traverseStructure = plantModelList.get(0);
							UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
						} else {

							if (BOPLine.getProperty("bl_item_item_id").equals(searchPlantModel) == false) {

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

					transferDlg.setProgressStatus(getProgress(percentage, total));
					percentage++;

					ArrayList<TCComponent> searchLines = null;

					String impactShopID = obj_Shop.getProperty("item_id");

					if (BOMLine.getProperty("bl_item_item_id").equals(impactShopID)) {

						searchLines = new ArrayList<TCComponent>();
						searchLines.add(BOMLine);

					} else {

						searchLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { impactShopID }, BOMLine);
					}

					if (searchLines != null) {

						String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");

						ArrayList<TCComponent> impactLines = UIGetValuesUtility.searchPartsInStruture(session, new String[] { searchIDs }, searchLines.get(0));

						transferDlg.setProgressStatus(getProgress(percentage, total));
						percentage++;

						String[] properties = new String[] { "bl_item_item_id", "bl_rev_item_revision_id" };

						TCComponent[] impactedBOMLines = new TCComponent[impactLines.size()];

						for (int i = 0; i < impactLines.size(); i++) {

							impactedBOMLines[i] = impactLines.get(i);
						}

						dmCoreService.getProperties(impactedBOMLines, properties);

						for (TCComponent impactedLine : impactLines) {

							boolean isTransferedToSAP = false;

							String ID = impactedLine.getPropertyDisplayableValue("bl_item_item_id");
							String RevID = impactedLine.getPropertyDisplayableValue("bl_rev_item_revision_id");

							transferDlg.setProgressStatus(getProgress(percentage, total));
							TCComponentBOMLine solutionItem = (TCComponentBOMLine) impactedLine;

							TCComponentItemRevision solution_Rev = solutionItem.getItemRevision();

							TCComponentItemRevision problemItemRevision = null;

							int revID = Integer.parseInt(RevID);

							if ((revID != 0 || revID != 1) && revID > 1) {

								String search_rev = Integer.toString(revID - 1);

								for (int i = search_rev.length(); i < RevID.length(); i++) {

									search_rev = "0" + search_rev;
								}

								problemItemRevision = UIGetValuesUtility.findRevision(session, ID, search_rev);

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
										HashMap<String, TCComponent> add_Output_parts = bomcompare.addPart(sol_P_Output, prob_P_Output);
										HashMap<String, TCComponent> del_Output_parts = bomcompare.delPart(sol_P_Output, prob_P_Output);

										// Child
										// DRYRUN prob_C_Input.clear();
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

													dmCoreService.getProperties(new TCComponent[] { del_Ouput_Part, del_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

													transferBOMMap.put("MCN", MCN_SAPID);
													transferBOMMap.put("PLANTCODE", plant);
													transferBOMMap.put("ACTION", "D");
													transferBOMMap.put("PARENTPART", del_Ouput_Part.getPropertyDisplayableValue("bl_item_item_id"));
													transferBOMMap.put("LINE", del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR));
													transferBOMMap.put("CHILDPART", del_Input_Part.getPropertyDisplayableValue("bl_item_item_id"));
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
													String BOMLineID = del_Input_Part.getPropertyDisplayableValue("VF4_bomline_id");

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

													dmCoreService.getProperties(new TCComponent[] { del_Part, del_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

													transferBOMMap.put("MCN", MCN_SAPID);
													transferBOMMap.put("PLANTCODE", plant);
													transferBOMMap.put("ACTION", "D");
													transferBOMMap.put("PARENTPART", del_Part.getPropertyDisplayableValue("bl_item_item_id"));
													transferBOMMap.put("CHILDPART", del_Input_Part.getPropertyDisplayableValue("bl_item_item_id"));
													transferBOMMap.put("LINE", del_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR));
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
													String BOMLineID = del_Input_Part.getPropertyDisplayableValue("VF4_bomline_id");
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

												BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOPScooterNoVerifyLink(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);

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
															dmCoreService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

															String parentID = add_Ouput_Part.getPropertyDisplayableValue("bl_item_item_id");
															String childID = add_Input_Part.getPropertyDisplayableValue("bl_item_item_id");

															transferBOMMap.put("MCN", MCN_SAPID);
															transferBOMMap.put("PLANTCODE", plant);
															transferBOMMap.put("ACTION", "A");
															transferBOMMap.put("PARENTPART", parentID);
															transferBOMMap.put("CHILDPART", childID);
															transferBOMMap.put("LINE", add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR));
															transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
															transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
															String BOMLineID = add_Input_Part.getPropertyDisplayableValue("VF4_bomline_id");

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

															// BOP MAP
															ArrayList<HashMap<String, String>> transferBOPMaps = new ArrayList<HashMap<String, String>>();
															for (TCComponent bop : foundBOPNode) {
																HashMap<String, String> transferBOPMap = new HashMap<String, String>();
																transferBOPMap.put("SAPPLANT", plant);
																transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
																transferBOPMap.put("TOPLEVELPART", parentID);
																transferBOPMap.put("HEADERPART", parentID);
																transferBOPMap.put("ACTION", "A");
																transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
																transferBOPMap.put("MCN", MCN_SAPID);
																// DRYRUN if (foundBOPNode == null || foundBOPNode.length == 0) continue;
																HashMap<String, String> dataMap = unitObj.transferBOP(dmCoreService, bop, transferBOMMap, ID, count, strBuilder);

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

													BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOPScooterNoVerifyLink(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);

													if (BOPLinkedLines != null) {

														for (String addLine : add_Output_keys) {

															TCComponent add_Ouput_Part = add_Output_parts.get(addLine);

															for (NodeInfo addInputLine : BOPLinkedLines) {

																TCComponent add_Input_Part = addInputLine.originalNode;
																TCComponent[] foundBOPNode = addInputLine.foundNodes;

																dmCoreService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

																HashMap<String, String> transferBOMMap = new HashMap<String, String>();
																dmCoreService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

																String parentID = add_Ouput_Part.getPropertyDisplayableValue("bl_item_item_id");
																String childID = add_Input_Part.getPropertyDisplayableValue("bl_item_item_id");

																transferBOMMap.put("MCN", MCN_SAPID);
																transferBOMMap.put("PLANTCODE", plant);
																transferBOMMap.put("ACTION", "A");
																transferBOMMap.put("PARENTPART", parentID);
																transferBOMMap.put("CHILDPART", childID);
																transferBOMMap.put("LINE", add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR));
																transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
																transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
																String BOMLineID = add_Input_Part.getPropertyDisplayableValue("VF4_bomline_id");

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
																	transferBOPMap.put("SAPPLANT", plant);
																	transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
																	transferBOPMap.put("TOPLEVELPART", parentID);
																	transferBOPMap.put("HEADERPART", parentID);
																	transferBOPMap.put("ACTION", "A");
																	transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
																	transferBOPMap.put("MCN", MCN_SAPID);
																	// DRYRUN if (foundBOPNode == null || foundBOPNode.length == 0) continue;
																	HashMap<String, String> dataMap = unitObj.transferBOP(dmCoreService, bop, transferBOMMap, ID, count, strBuilder);

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
														String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
														if (message[0].equals("E") && !message[1].contains("Record is exist in staging table")) {
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
								// all is new
								boolean IOValues = bomcompare.inputOutputMap(session, null, solutionItem, count, strBuilder);

								if (IOValues == true) {

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

										BOPLinkedLines = UIGetValuesUtility.findBOMLineInBOPScooterNoVerifyLink(session, inputLines, (TCComponentBOMLine) traverseStructure, ID, count, strBuilder);

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
													dmCoreService.getProperties(new TCComponent[] { add_Ouput_Part, add_Input_Part }, new String[] { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", PropertyDefines.BOM_DESIGNATOR });

													String parentID = add_Ouput_Part.getPropertyDisplayableValue("bl_item_item_id");
													String childID = add_Input_Part.getPropertyDisplayableValue("bl_item_item_id");

													transferBOMMap.put("MCN", MCN_SAPID);
													transferBOMMap.put("PLANTCODE", plant);
													transferBOMMap.put("ACTION", "A");
													transferBOMMap.put("PARENTPART", parentID);
													transferBOMMap.put("CHILDPART", childID);
													transferBOMMap.put("LINE", add_Input_Part.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR));
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
													transferBOMMap.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
													String BOMLineID = add_Input_Part.getPropertyDisplayableValue("VF4_bomline_id");

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
													HashMap<String, String> bomMap = transferBOMMap;
													String BOMFile = bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART") + "_" + bomMap.get("BOMLINEID");
													String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.assybomWebserviceURL(SERVER_IP), bomMap, SAPURL.ASSY_BOM_HEADER, SAPURL.ASSY_BOM_TAG, SAPURL.ASSY_BOM_NAMESPACE, "I_BOM_" + bomMap.get("ACTION") + "_" + BOMFile, logFolder, auth);
													if (message == null) {
														printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), "No SAP Response", bomMap.get("ACTION"), "Error" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
													} else if (message[0].equals("E") && !message[1].contains("Record is exist in staging table")) {
														printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Error" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
														isTransferedToSAP = false;
														attachLog = false;
													} else {
														printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bomMap.get("PARENTPART") + "_" + bomMap.get("CHILDPART"), bomMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
														Logger.bufferResponse("PRINT", printValues, strBuilder);
													}

													for (TCComponent singleBopNode : foundBOPNode) {
														// BOP MAP
														HashMap<String, String> transferBOPMap = new HashMap<String, String>();
														transferBOPMap.put("SAPPLANT", plant);
														transferBOPMap.put("BOMLINEID", transferBOMMap.get("BOMLINEID"));
														transferBOPMap.put("TOPLEVELPART", transferDlg.getRootMaterial());
														transferBOPMap.put("HEADERPART", parentID);
														transferBOPMap.put("ACTION", "A");
														transferBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
														transferBOPMap.put("MCN", MCN_SAPID);
														HashMap<String, String> dataMap = unitObj.transferBOP(dmCoreService, singleBopNode, transferBOMMap, ID, count, strBuilder);

														if (dataMap != null) {
															transferBOPMap.putAll(dataMap);
															HashMap<String, String> bopMap = transferBOPMap;
															String BOPFile = bopMap.get("BOPID") + "_" + bopMap.get("WORKSTATION");
															String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), bopMap, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + bopMap.get("ACTION") + "_" + BOPFile, logFolder, auth);
															if (message == null) {
																printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), "No SAP Response", bomMap.get("ACTION"), "Error" };
																Logger.bufferResponse("PRINT", printValues, strBuilder);
															} else if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {
																printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), msg[1], bomMap.get("ACTION"), "Error" };
																Logger.bufferResponse("PRINT", printValues, strBuilder);
																isTransferedToSAP = false;
																attachLog = false;
															} else {
																printValues = new String[] { Integer.toString(count++), ID + "/" + RevID, bopMap.get("BOPID") + "/" + bopMap.get("REVISION"), bopMap.get("BOMLINEID"), message[1], bomMap.get("ACTION"), "Success" };
																Logger.bufferResponse("PRINT", printValues, strBuilder);
															}

														} else {
															attachLog = false;
														}
													}
												}
												sol_C_Input.remove(key);
											}
										}
									}
								}
							}

							percentage++;

							if (isTransferedToSAP == true) {

								if (transferDlg.getServer().equals("PRODUCTION")) {

									LocalDateTime now = LocalDateTime.now();
									String setValue = "Yes" + "~" + userName + "~" + dtf.format(now);
									UIGetValuesUtility.setProperty(dmCoreService, solution_Rev, "vf3_transfer_to_sap", setValue);
									saveRootMaterial(obj_shopTopNode, dmCoreService, transferDlg.getRootMaterial());
								}
							}

							percentage++;
						}
					}
				}

				if (problemItems != null) {

					for (TCComponent problemItem : problemItems) {

						transferDlg.setProgressStatus(getProgress(percentage, total));

						TCComponentItemRevision problem = (TCComponentItemRevision) problemItem;

						CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);

						TCComponentBOMWindow operationWindow = output[0].bomWindow;
						TCComponentBOMLine operationLine = output[0].bomLine;

						if (UIGetValuesUtility.hasMaterials(operationLine) == false) {

							dmCoreService.getProperties(new TCComponent[] { operationLine }, new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "VF4_bomline_id", "vf4_operation_type", "bl_rev_vf3_transfer_to_sap", "bl_rev_vf5_line_supply_method" });

							String ID = operationLine.getPropertyDisplayableValue("bl_item_item_id");
							String RevID = operationLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
							String HT = operationLine.getItemRevision().getProperty("vf4_reference_component");

							if (HT.equals("")) {

								printValues = new String[] { Integer.toString(count), "OWP", ID + "/" + RevID, "", "On Operation \"Reference Component\" column Header Part Information is missing", "-", "Error" };
								Logger.bufferResponse("PRINT", printValues, strBuilder);
								count++;
							} else {

								String WS[] = operationLine.getPropertyDisplayableValue("bl_rev_vf3_transfer_to_mes").split("~");
								String[] split = WS[0].split(",");

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
									BOP_Values.put("SAPPLANT", plant);
									BOP_Values.put("TOPLEVELPART", transferDlg.getRootMaterial());
									BOP_Values.put("HEADERPART", HT);
									BOP_Values.put("BOMLINEID", "");
									BOP_Values.put("ACTION", "D");
									BOP_Values.put("WORKSTATION", workstation);
									BOP_Values.put("LINESUPPLYMETHOD", LSM);
									BOP_Values.put("BOPID", ID);
									BOP_Values.put("MESBOPINDICATOR", MES);
									BOP_Values.put("REVISION", RevID);
									BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
									BOP_Values.put("MCN", MCN_SAPID);
									String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
									String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
									if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {

										printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Error" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);
										attachLog = false;

									} else {

										printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Success" };
										Logger.bufferResponse("PRINT", printValues, strBuilder);

										if (transferDlg.getServer().equals("PRODUCTION")) {

											LocalDateTime now = LocalDateTime.now();
											String setValue = "Yes" + "~" + userName + "~" + dtf.format(now);
											UIGetValuesUtility.setProperty(dmCoreService, problem, "vf3_transfer_to_sap", setValue);
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

						transferDlg.setProgressStatus(getProgress(percentage, total));
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

								if (BOPLine.getProperty("bl_item_item_id").equals(searchPlantModel) == false) {

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

						transferDlg.setProgressStatus(getProgress(percentage, total));
						percentage++;
					}

					for (TCComponent solutionItem : solutionItems) {
						transferDlg.setProgressStatus(getProgress(percentage, total));

						TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;

						String ID = operations.getProperty("item_id");

						ArrayList<TCComponent> operationsList = UIGetValuesUtility.searchPartsInStruture(session, new String[] { ID }, traverseStructure);

						if (operationsList.isEmpty() == false) {

							TCComponentBOMLine operationLine = (TCComponentBOMLine) operationsList.get(0);

							if (UIGetValuesUtility.hasMaterials(operationLine) == false) {

								dmCoreService.getProperties(new TCComponent[] { operationLine }, new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "VF4_bomline_id", "vf4_operation_type", "bl_rev_vf3_transfer_to_sap", "bl_rev_vf5_line_supply_method", "vf4_reference_component" });

								String RevID = operationLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
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

									for (TCComponent operation : operationsList) {

										HashMap<String, String> BOP_Values = new HashMap<String, String>();
										BOP_Values.put("SAPPLANT", plant);
										BOP_Values.put("TOPLEVELPART", transferDlg.getRootMaterial());
										BOP_Values.put("HEADERPART", HT);
										BOP_Values.put("BOMLINEID", "");
										BOP_Values.put("ACTION", "A");
										BOP_Values.put("WORKSTATION", UIGetValuesUtility.getWorkStationID((TCComponentBOMLine) operation, "bl_rev_object_name"));
										BOP_Values.put("LINESUPPLYMETHOD", LSM);
										BOP_Values.put("BOPID", ID);
										BOP_Values.put("MESBOPINDICATOR", MES);
										BOP_Values.put("REVISION", RevID);
										BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
										BOP_Values.put("MCN", MCN_SAPID);
										String BOPFile = BOP_Values.get("BOPID") + "_" + BOP_Values.get("REVISION");
										String[] msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(SERVER_IP), BOP_Values, SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE, "I_BOP_" + BOP_Values.get("ACTION") + "_" + BOPFile, logFolder, auth);
										if (msg[0].equals("E") && !msg[1].contains("Record is exist in staging table")) {

											printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Error" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											attachLog = false;

										} else {

											printValues = new String[] { Integer.toString(count), "OWP", BOP_Values.get("BOPID") + "/" + BOP_Values.get("REVISION"), "", msg[1], BOP_Values.get("ACTION"), "Success" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											if (transferDlg.getServer().equals("PRODUCTION")) {

												LocalDateTime now = LocalDateTime.now();
												String setValue = "Yes" + "~" + userName + "~" + dtf.format(now);
												UIGetValuesUtility.setProperty(dmCoreService, operations, "vf3_transfer_to_sap", setValue);
											}

										}
										count++;
									}

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

				if (attachLog == true && transferDlg.getServer().equals("PRODUCTION") == true) {

					TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmCoreService, changeObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");

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

			} else {

				MessageBox.post("No Items in \"Impacted Items\" folder to transfer to SAP.", "Error", MessageBox.ERROR);
			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
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

	private String getRootMaterial(TCComponentItemRevision itemRevision) {
		String rootMaterial = "";
		try {
			TCComponentItem topLevelItem = itemRevision.getItem();
			TCComponentForm topLevelItemMasterForm = (TCComponentForm) topLevelItem.getRelatedComponent("IMAN_master_form");
			rootMaterial = topLevelItemMasterForm.getProperty(PropertyDefines.ITEM_COMMENT);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rootMaterial;
	}

	/**
	 * @param itemRevision
	 * @param dmCoreService
	 * @param rootMaterial  Save root material to field "item comment" to master
	 *                      form of top MBOM.
	 */
	private void saveRootMaterial(TCComponentItemRevision itemRevision, DataManagementService dmCoreService, String rootMaterial) {
		try {
			TCComponentItem topLevelItem = itemRevision.getItem();
			TCComponentForm topLevelItemMasterForm = (TCComponentForm) topLevelItem.getRelatedComponent("IMAN_master_form");
			UIGetValuesUtility.setProperty(dmCoreService, topLevelItemMasterForm, PropertyDefines.ITEM_COMMENT, rootMaterial);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
