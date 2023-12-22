/*********************************************
 * Date				Owner			Ticket				Description
 * 11-07-2023		Rafi			10886				Updated to handle empty response code from SAP
 * 
 * 
 * 
 * 
 */

package com.vinfast.sap.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.ExecutionEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.administration.IRMService;
import com.teamcenter.services.rac.administration.PreferenceManagementService;
import com.teamcenter.services.rac.administration._2006_03.IRM;
import com.teamcenter.services.rac.administration._2006_03.IRM.CheckAccessorPrivilegesResponse;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.PreferenceValue;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSParentData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.cad._2019_06.StructureManagement.CreateWindowsInfo3;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.CommitDatasetFileInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.DatasetFileTicketInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.rac.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.rac.core._2006_03.FileManagement.GetDatasetWriteTicketsResponse;
import com.teamcenter.services.rac.core._2006_03.Session.GetSessionGroupMemberResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationship;
import com.teamcenter.services.rac.core._2009_10.DataManagement;
import com.teamcenter.services.rac.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.rac.core._2009_10.DataManagement.GetItemFromAttributeItemRevOutput;
import com.teamcenter.services.rac.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.rac.core._2010_04.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.rac.core._2010_04.DataManagement.DatasetInfo;
import com.teamcenter.services.rac.core._2010_09.DataManagement.NameValueStruct1;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PropInfo;
import com.teamcenter.services.rac.core._2010_09.DataManagement.SetPropertyResponse;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedConfigParameters;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedInputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedOutputData;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedParentInfo;
import com.teamcenter.services.rac.core._2012_02.DataManagement.WhereUsedResponse;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing.StructureSearchService;
import com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.StructureSearchResultResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.teamcenter.services.rac.manufacturing._2011_06.StructureManagement.ReferencedContexts;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf4.services.rac.custom.IntegrationService;
import com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPInput;
import com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.SuperScooterReport;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMBOPData.ActionType;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.services.Logger;

/**
 * @author vf_install
 *
 */
public class UIGetValuesUtility {

	ResourceBundle bundle = null;

	public UIGetValuesUtility() {

		bundle = ResourceBundle.getBundle("Transfer");

	}
	
	public static String getCompanyCode(ExecutionEvent event) {
		
		String company = PropertyDefines.VIN_FAST;
		String command = event.getCommand().toString();
		if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
			company = PropertyDefines.VIN_FAST_ELECTRIC;
		} else if (command.contains(PropertyDefines.VIN_ES)) {
			company = PropertyDefines.VIN_ES;
		}
		return company;
	}

	public static boolean isPortOpen(String server, int port) {

		try (Socket ignored = new Socket(server, port)) {
			return true;
		} catch (ConnectException e) {
			return false;
		} catch (IOException e) {
			throw new IllegalStateException("Error while trying to check open port", e);
		}
	}

	public static boolean isServerOn(String server) {
		boolean reachable = false;
		try {
			InetAddress address = InetAddress.getByName(server);
			reachable = address.isReachable(10000);

			System.out.println("Is host reachable? " + reachable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reachable;
	}

	@SuppressWarnings("unchecked")
	public Set<String> addedLines(HashMap<String, BOMBOPData> old_items, HashMap<String, BOMBOPData> new_items) {

		HashMap<String, BOMBOPData> old_BOM = (HashMap<String, BOMBOPData>) old_items.clone();
		HashMap<String, BOMBOPData> new_BOM = (HashMap<String, BOMBOPData>) new_items.clone();

		Set<String> addoldBOM = old_BOM.keySet();
		Set<String> addNewBOM = new_BOM.keySet();

		addNewBOM.removeAll(addoldBOM);

		return addNewBOM;
	}

	public String buildErrorMessage(String errorMsg, String message) {

		if (errorMsg.equals("")) {
			errorMsg = message;
		} else {
			errorMsg = errorMsg + "," + message;
		}
		return errorMsg;
	}

	public static TCComponent[] fetchValidScooterBOMItems2(DataManagementService dmCoreService, ExpandPSData[] BOMItems, String subGroup, int count, ArrayList<SuperScooterReport> reportList) {

		ArrayList<String> duplicateBOMID = new ArrayList<String>();
		ArrayList<TCComponent> validBOMLinesVector = new ArrayList<TCComponent>();
		TCComponent[] validBOMLines = null;
		boolean isError = false;

		String[] prop_Values = new String[] { "bl_rev_object_type", "VL5_purchase_lvl_vf", "VF3_purchase_lvl_vf", "VF4_manuf_code", "VF4_bomline_id", "bl_item_item_id", "bl_quantity", "bl_formula" };

		try {

			TCComponent[] BOMLines = new TCComponent[BOMItems.length];

			for (int i = 0; i < BOMItems.length; i++) {

				BOMLines[i] = BOMItems[i].bomLine;
			}

			dmCoreService.getProperties(BOMLines, prop_Values);
			dmCoreService.refreshObjects(BOMLines);
			for (TCComponent childLine : BOMLines) {

				String ID = childLine.getProperty("bl_item_item_id");

				String purchaseLevel = childLine.getProperty("VL5_purchase_lvl_vf");

				String manufCode = childLine.getProperty("VF4_manuf_code");

				String type = childLine.getProperty("bl_rev_object_type");

				String BOMLineID = childLine.getProperty("VF4_bomline_id");

				if (isToProcessAssembly(type.trim(), manufCode.trim()) == true) {

					if (isBOMLinePurchase(purchaseLevel.trim()) == true) {
						String errorMessage = "";

						int bomLineIDCount = BOMLineID.trim().length();
						int purchaseLevelCount = purchaseLevel.trim().length();

						if (bomLineIDCount == 0 || purchaseLevelCount == 0) {

							if (bomLineIDCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "BOMLine ID";
								} else {
									errorMessage = errorMessage + ",BOMLine ID";
								}
							}
							if (purchaseLevelCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "Purchase Level";
								} else {
									errorMessage = errorMessage + ",Purchase Level";
								}
							}
						}

						if (errorMessage.length() != 0) {
							SuperScooterReport rp = new SuperScooterReport();
							rp.setNo(Integer.toString(count++));
							rp.setSubGroup(subGroup);
							rp.setRecord(ID);
							rp.setBomlineId(BOMLineID);
							rp.setMessage(String.format("%s %s", errorMessage, "mandatory value(s) are empty."));
							rp.setAction("-");
							rp.setType(UpdateType.UPDATE_BODY_ERROR);
							reportList.add(rp);
							isError = true;
							break;
						} else {

							if (duplicateBOMID.contains(BOMLineID.trim())) {
								errorMessage = "BOMLine ID";
								SuperScooterReport rp = new SuperScooterReport();
								rp.setNo(Integer.toString(count++));
								rp.setSubGroup(subGroup);
								rp.setRecord(ID);
								rp.setBomlineId(BOMLineID);
								rp.setMessage(String.format("%s %s", errorMessage, " value(s) is duplicate."));
								rp.setAction("-");
								rp.setType(UpdateType.UPDATE_BODY_ERROR);
								reportList.add(rp);
								isError = true;
								break;
							} else {

								duplicateBOMID.add(BOMLineID.trim());
							}
						}

						if (errorMessage.length() == 0) {
							validBOMLinesVector.add(childLine);
						}

					} else {
						SuperScooterReport rp = new SuperScooterReport();
						rp.setNo(Integer.toString(count++));
						rp.setSubGroup(subGroup);
						rp.setRecord(ID);
						rp.setBomlineId(BOMLineID);
						rp.setMessage("Purchase level is NOT valid!");
						rp.setAction("-");
						rp.setType(UpdateType.UPDATE_BODY_INFO);
						reportList.add(rp);
					}
				}
			}
			if (validBOMLinesVector.isEmpty() == false && isError == false) {

				validBOMLines = new TCComponent[validBOMLinesVector.size()];

				for (int i = 0; i < validBOMLinesVector.size(); i++) {
					validBOMLines[i] = validBOMLinesVector.get(i);

				}

			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return validBOMLines;

	}

	public TCComponent[] fetchValidBOMItems(DataManagementService dmCoreService, ExpandPSData[] BOMItems, String subGroup, int count, StringBuilder strBuilder) {

		ArrayList<String> duplicateBOMID = new ArrayList<String>();
		ArrayList<TCComponent> validBOMLinesVector = new ArrayList<TCComponent>();
		TCComponent[] validBOMLines = null;
		boolean isError = false;

		String[] prop_Values = new String[] { "bl_rev_object_type", "VL5_purchase_lvl_vf", "VF3_purchase_lvl_vf", "VF4_manuf_code", "VF4_bomline_id", "bl_item_item_id", "bl_quantity", "bl_formula" };

		try {

			TCComponent[] BOMLines = new TCComponent[BOMItems.length];

			for (int i = 0; i < BOMItems.length; i++) {

				BOMLines[i] = BOMItems[i].bomLine;
			}

			dmCoreService.getProperties(BOMLines, prop_Values);

			for (TCComponent childLine : BOMLines) {

				String ID = childLine.getProperty("bl_item_item_id");

				String purchaseLevel = childLine.getProperty("VF3_purchase_lvl_vf");

				if (purchaseLevel.isEmpty()) {

					purchaseLevel = childLine.getProperty("VL5_purchase_lvl_vf");
				}

				String manufCode = childLine.getProperty("VF4_manuf_code");

				String type = childLine.getProperty("bl_rev_object_type");

				String BOMLineID = childLine.getProperty("VF4_bomline_id");

				if (isToProcessAssembly(type.trim(), manufCode.trim()) == true) {

					if (isBOMLinePurchase(purchaseLevel.trim()) == true) {
						String errorMessage = "";

						int bomLineIDCount = BOMLineID.trim().length();
						int purchaseLevelCount = purchaseLevel.trim().length();

						if (bomLineIDCount == 0 || purchaseLevelCount == 0) {

							if (bomLineIDCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "BOMLine ID";
								} else {
									errorMessage = errorMessage + ",BOMLine ID";
								}
							}
							if (purchaseLevelCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "Purchase Level";
								} else {
									errorMessage = errorMessage + ",Purchase Level";
								}
							}
						}

						if (errorMessage.length() != 0) {

							String[] response = new String[] { Integer.toString(count), subGroup, ID, BOMLineID, errorMessage + " mandatory value(s) are empty.", "-", "Error" };
							Logger.bufferResponse("PRINT", response, strBuilder);
							count++;
							isError = true;
							break;
						} else {

							if (duplicateBOMID.contains(BOMLineID.trim())) {

								errorMessage = "BOMLine ID";
								String[] response = new String[] { Integer.toString(count), subGroup, ID, BOMLineID, errorMessage + " value(s) is duplicate.", "-", "Error" };
								Logger.bufferResponse("PRINT", response, strBuilder);
								count++;
								isError = true;
								break;
							} else {

								duplicateBOMID.add(BOMLineID.trim());
							}
						}

						if (errorMessage.length() == 0) {
							validBOMLinesVector.add(childLine);
						}

					}
				}
			}
			if (validBOMLinesVector.isEmpty() == false && isError == false) {

				validBOMLines = new TCComponent[validBOMLinesVector.size()];

				for (int i = 0; i < validBOMLinesVector.size(); i++) {
					validBOMLines[i] = validBOMLinesVector.get(i);

				}

			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return validBOMLines;

	}

	@SuppressWarnings("unchecked")
	public HashMap<String, BOMBOPData> changeInOperationRevision(HashMap<String, BOMBOPData> old_items, HashMap<String, BOMBOPData> new_items) {

		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) old_items.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) new_items.clone();
		HashMap<String, BOMBOPData> differentOperationRevision = new HashMap<String, BOMBOPData>();

		for (String key : newBOP.keySet()) {
			try {

				BOMBOPData newLine = newBOP.get(key);
				BOMBOPData oldLine = oldBOP.get(key);

				if (oldLine != null) {

					String newLineOperation = newLine.getBOPID();
					String oldLineOperation = oldLine.getBOPID();

					String newLineWS = newLine.getWorkStation();
					String oldLineWS = oldLine.getWorkStation();

					String newRevision = newLine.getBOPRevision();
					String oldRevision = oldLine.getBOPRevision();

					if ((newLineOperation.equals(oldLineOperation) == true) && (newLineWS.equals(oldLineWS) == true) && (newRevision.equals(oldRevision) == false)) {
						differentOperationRevision.put(key, newLine);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return differentOperationRevision;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, BOMBOPData> changeInWorkStation(HashMap<String, BOMBOPData> old_items, HashMap<String, BOMBOPData> new_items, String transferMode) {

		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) old_items.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) new_items.clone();
		HashMap<String, BOMBOPData> differentWorkStation = new HashMap<String, BOMBOPData>();

		for (String key : newBOP.keySet()) {

			try {

				BOMBOPData newLine = newBOP.get(key);
				BOMBOPData oldLine = oldBOP.get(key);

				if (oldLine != null) {

					String newLineOperation = newLine.getBOPID();
					String oldLineOperation = oldLine.getBOPID();

					String newLineWS = newLine.getWorkStation();
					String oldLineWS = oldLine.getWorkStation();

					String newLineMESInd = newLine.getMESIndicator();
					String oldLineMESInd = oldLine.getMESIndicator();

					switch (transferMode) {

					case PropertyDefines.TRANSFER_MODE_ASSY:

						if ((newLineOperation.equals(oldLineOperation)) == false || (newLineWS.equals(oldLineWS)) == false || (newLineMESInd.equals(oldLineMESInd)) == false) {
							differentWorkStation.put(key, newLine);
						}
						break;
					case PropertyDefines.TRANSFER_MODE_SUPER:

						String newLineFD = newLine.getFamilyAddress();
						String oldLineFD = oldLine.getFamilyAddress();

						if ((newLineOperation.equals(oldLineOperation)) == false || (newLineWS.equals(oldLineWS)) == false || (newLineMESInd.equals(oldLineMESInd)) == false || (newLineFD.equals(oldLineFD)) == false) {
							differentWorkStation.put(key, newLine);
						}
						break;
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return differentWorkStation;
	}

	/**
	 * @param session
	 * @return True|False
	 * @author RAFI
	 * @apiNote Check object access on object to logged in User
	 */
	public static boolean checkAccess(TCSession session, TCComponentGroupMember groupMember, TCComponent object) {

		boolean result = false;

		try {

			IRMService irmService = IRMService.getService(session);
			CheckAccessorPrivilegesResponse response = irmService.checkAccessorsPrivileges(groupMember, new TCComponent[] { object }, new String[] { "WRITE" });
			ServiceData sd = response.serviceData;

			if (sd.sizeOfPartialErrors() > 0) {

				return result;

			} else {

				IRM.PrivilegeReport[] report = response.privilegeReports;
				IRM.Privilege[] privilege = report[0].privilegeInfos;
				result = privilege[0].verdict;
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String checkValidShop(TCComponentItemRevision shopItem, String plantCode) {
		try {
			TCComponentForm form = (TCComponentForm) shopItem.getItem().getRelatedComponent("IMAN_master_form");
			String shopItemID = shopItem.getItem().getProperty(PropertyDefines.ITEM_ID);
			String shopProjectID = form.getProperty("project_id");

			if (shopProjectID.trim().equals("")) {
				return String.format("Please correct \"Project ID\" information MasterForm of Shop [%s] before transfer. (Plant Code of MCN is %s)", shopItemID, plantCode);
			}
			if (shopProjectID.equals(plantCode)) {
				return "";
			} else {
				return String.format("Plant Code of MCN is %s while \"Project ID\" in MasterForm of Shop [%s] (%s). Please correct them before transfer", plantCode, shopItemID, shopProjectID);
			}

		} catch (TCException e) {
			e.printStackTrace();
			return "A exception occur when comparing Plant Code of MCN and Project ID in MasterForm of ImpactedShop";
		}
	}

	public static void closeAllContext(TCSession session, OpenContextInfo[] window) {

		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponent[] windows = new TCComponent[window.length];

		for (int i = 0; i < window.length; i++) {

			windows[i] = window[i].context;
		}

		dmService.closeContexts(windows);
	}

	public static void closeContext(TCSession session, TCComponent window) {
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		dmService.closeContexts(new TCComponent[] { window });
	}

	public static void closeWindow(TCSession session, TCComponentBOMWindow window) {

		StructureManagementService SMService = StructureManagementService.getService(session);

		SMService.closeBOMWindows(new TCComponentBOMWindow[] { window });
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, BOMBOPData> commonBOMLines(HashMap<String, BOMBOPData> old_items, HashMap<String, BOMBOPData> new_items, String transferMode) {

		HashMap<String, BOMBOPData> addSTBOM = (HashMap<String, BOMBOPData>) old_items.clone();
		HashMap<String, BOMBOPData> addSTPart = (HashMap<String, BOMBOPData>) new_items.clone();
		HashMap<String, BOMBOPData> common_Parts = new HashMap<String, BOMBOPData>();
		Set<String> addBOM = addSTBOM.keySet();
		Set<String> addST = addSTPart.keySet();

		addBOM.retainAll(addST);

		for (String key : addSTBOM.keySet()) {
			try {

				BOMBOPData solutionItem = addSTBOM.get(key);
				BOMBOPData problemItem = addSTPart.get(key);

				String solQty = solutionItem.getQuanity();
				String prbQty = problemItem.getQuanity();

				float solq = Float.parseFloat(solQty);
				float prbq = Float.parseFloat(prbQty);

				switch (transferMode) {

				case PropertyDefines.TRANSFER_MODE_ASSY:

					if ((solq == prbq) == false) {
						common_Parts.put(key, solutionItem);
					}
					break;
				case PropertyDefines.TRANSFER_MODE_SUPER:

					String solOption = solutionItem.getFormula();
					String prbOption = problemItem.getFormula();

					if ((solq == prbq && solOption.equals(prbOption)) == false) {
						common_Parts.put(key, solutionItem);
					}
					break;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return common_Parts;
	}

	public static TCComponent continueSearchX(StructureSearchService strutService, TCComponent cursor, ArrayList<TCComponent> searchParts, int findAmount) {

		StructureSearchResultResponse nextResponse;
		TCComponent nextcursor = null;
		try {
			nextResponse = strutService.nextSearch(cursor);

			nextcursor = nextResponse.searchCursor;

			TCComponent[] objects = nextResponse.objects;

			if (nextResponse.finished == false) {

				if (objects.length > 0) {

					for (TCComponent object : objects) {

						searchParts.add(object);
					}
				}
				if (findAmount > searchParts.size()) {
					continueSearchX(strutService, nextcursor, searchParts, findAmount);
				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextcursor;
	}

	public static TCComponent continueSearch(StructureSearchService strutService, TCComponent cursor, ArrayList<TCComponent> searchParts) {

		StructureSearchResultResponse nextResponse;
		TCComponent nextcursor = null;
		try {
			nextResponse = strutService.nextSearch(cursor);

			nextcursor = nextResponse.searchCursor;

			TCComponent[] objects = nextResponse.objects;

			if (nextResponse.finished == false) {

				if (objects.length > 0) {

					for (TCComponent object : objects) {

						searchParts.add(object);
					}
				}
				continueSearch(strutService, nextcursor, searchParts);

			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextcursor;
	}

	public static TCComponentBOMLine findTopMBOMFromSubgroup(TCComponentBOMLine subgroup) {
		TCComponentBOMLine parent = subgroup;
		TCComponentBOMLine topbom = parent;
		// finding topline
		while (parent != null) {
			try {
				topbom = parent;
				parent = (TCComponentBOMLine) parent.getReferenceProperty("bl_parent");
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		return topbom;
	}

	public static String convertArrayToString(ArrayList<String> arrayList, String seperator) {

		String strArray = "";

		for (String ID : arrayList) {

			if (strArray.equals("")) {

				strArray = ID;
			} else {
				strArray = strArray + seperator + ID;
			}
		}

		return strArray;
	}

	public String[] convertStringToArray(String strValue, String seperator) {

		String[] strArray = null;

		strArray = strValue.split(seperator);

		return strArray;
	}

	public static CreateBOMWindowsOutput[] createBOMNoRuleWindow(TCSession session, TCComponentItemRevision itemRev) {

		CreateBOMWindowsOutput[] BOMOutput = null;

		try {

			CreateWindowsInfo3 BOMWindowInfo = new CreateWindowsInfo3();
			BOMWindowInfo.item = itemRev.getItem();
			BOMWindowInfo.itemRev = itemRev;

			StructureManagementService SMService = StructureManagementService.getService(session);
			CreateBOMWindowsResponse BOM = SMService.createOrReConfigureBOMWindows(new CreateWindowsInfo3[] { BOMWindowInfo });

			BOMOutput = BOM.output;

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return BOMOutput;
	}

	public static CreateBOMWindowsOutput[] createBOMWindow(TCSession session, TCComponent[] itemRevisions) {

		CreateBOMWindowsOutput[] BOMOutput = null;
		try {

			CreateWindowsInfo3[] BOMWindowInfo = new CreateWindowsInfo3[itemRevisions.length];

			for (int i = 0; i < itemRevisions.length; i++) {

				TCComponentItemRevision itemRevision = (TCComponentItemRevision) itemRevisions[i];
				BOMWindowInfo[i] = new CreateWindowsInfo3();
				BOMWindowInfo[i].clientId = itemRevision.getProperty(PropertyDefines.ITEM_ID);
				BOMWindowInfo[i].itemRev = itemRevision;
				BOMWindowInfo[i].item = itemRevision.getItem();

			}

			StructureManagementService SMService = StructureManagementService.getService(session);

			CreateBOMWindowsResponse BOMWindows = SMService.createOrReConfigureBOMWindows(BOMWindowInfo);

			ServiceData serviceDate = BOMWindows.serviceData;

			if (serviceDate.sizeOfPartialErrors() > 0) {

				MessageBox.post(SoaUtil.buildErrorMessage(serviceDate), "Error", MessageBox.ERROR);

			} else {

				BOMOutput = BOMWindows.output;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BOMOutput;
	}

	public static OpenContextInfo[] createContextViews(TCSession session, TCComponent object) {

		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponentRevisionRule RevisionRule = getRevisionRule("Working; Any Status", session);
		CreateInput input = new CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;
		if (sd.sizeOfPartialErrors() > 0) {
			SoaUtil.buildErrorMessage(sd);
		} else {
			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;

	}

	public static OpenContextInfo[] createContextViews(TCSession session, TCComponent object, String rule) {

		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		// DRYRUN __ADMIN_Only_Approved
		TCComponentRevisionRule RevisionRule = getRevisionRule(rule, session);
		// TCComponentRevisionRule RevisionRule =
		// getRevisionRule("VINFAST_RELEASE_RULE", session);
		// TCComponentRevisionRule RevisionRule = getRevisionRule("__ADMIN_TEMP",
		// session);
		CreateInput input = new CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;

		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 1; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}

		} else {

			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;

	}

	public static TCComponentDataset createDataset(DataManagementService dmService, TCComponentItemRevision container, String relation, String datasetName, String description, String datasetType, String toolUsed) {
		TCComponentDataset newDataset = null;
		DatasetInfo datasetProperties = new DatasetInfo();
		datasetProperties.clientId = "createdataset";
		datasetProperties.type = datasetType;
		datasetProperties.name = datasetName;
		datasetProperties.description = description;
		datasetProperties.toolUsed = toolUsed;

		if (container != null) {
			datasetProperties.relationType = relation;
			datasetProperties.container = container;
		}

		CreateDatasetsResponse response = dmService.createDatasets(new DatasetInfo[] { datasetProperties });
		if (response.datasetOutput != null && response.datasetOutput.length > 0) {
			newDataset = response.datasetOutput[0].dataset;
		}

		return newDataset;
	}

	public static String createLogFolder(String name) {
		String LOG_PATH_U = "C:\\Temp";
		String LOG_PATH = "";
		String logFolder = "";

		File temp_Folder_U = new File(LOG_PATH_U);

		if (temp_Folder_U.exists() == false && temp_Folder_U.isDirectory() == false) {
			if (temp_Folder_U.mkdir()) {
				LOG_PATH = temp_Folder_U.getPath();
			} else {
				System.out.println("Failed to create log directory!");
			}
		} else {
			LOG_PATH = temp_Folder_U.getPath();
		}

		File file = new File(LOG_PATH + "\\" + name);
		if (!file.exists() && !file.isDirectory()) {
			if (file.mkdir()) {
				logFolder = file.getPath();
			} else {
				System.out.println("Failed to create log directory!");
			}
		} else {
			logFolder = file.getPath();
		}

		return logFolder;
	}

	public static void createFolder(String path) {
		File temp_Folder_U = new File(path);
		if (!temp_Folder_U.exists() && !temp_Folder_U.isDirectory()) {
			if (temp_Folder_U.mkdir()) {
				System.out.println("Create log directory success!");
			} else {
				System.out.println("Failed to create log directory!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Set<String> deletedLines(HashMap<String, BOMBOPData> old_items, HashMap<String, BOMBOPData> new_items) {

		HashMap<String, BOMBOPData> old_BOM = (HashMap<String, BOMBOPData>) old_items.clone();
		HashMap<String, BOMBOPData> new_BOM = (HashMap<String, BOMBOPData>) new_items.clone();

		Set<String> addoldBOM = old_BOM.keySet();
		Set<String> addNewBOM = new_BOM.keySet();

		addoldBOM.removeAll(addNewBOM);

		return addoldBOM;
	}

	public static File downloadDataset(TCSession session, String outputPath, TCComponentDataset dataset) {

		File downloadFile = null;
		try {

			FileManagementService fmService = FileManagementService.getService(session);
			FileManagementUtility fileUtility = new FileManagementUtility(session.getSoaConnection());

			if (dataset.getType().equals("Text")) {

				TCComponentTcFile[] files = dataset.getTcFiles();

				if (files.length != 0) {

					FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);

					Map<TCComponentTcFile, String> map = ticketResp.tickets;

					for (TCComponentTcFile tcFile : map.keySet()) {
						String fileName = tcFile.getProperty("original_file_name");
						String ticket = map.get(tcFile);
						downloadFile = fileUtility.getTransientFile(ticket, outputPath + fileName);
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return downloadFile;
	}

	public static HashMap<TCComponent, TCComponent[]> expandBOMOneLevel(TCSession session, TCComponent[] parentLines) {
		HashMap<TCComponent, TCComponent[]> expandLines = new HashMap<TCComponent, TCComponent[]>();
		try {
			for (TCComponent parent : parentLines) {
				TcResponseHelper responseHelper = TcBOMService.expand(session, parent);
				TCComponent[] returnedObjects = responseHelper.getReturnedObjects();
				expandLines.put(parent, returnedObjects);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return expandLines;
	}

	public static HashMap<TCComponent, TCComponent[]> expandBOPOneLevel(TCSession session, TCComponent[] parentLines) {
		HashMap<TCComponent, TCComponent[]> expandLines = new HashMap<TCComponent, TCComponent[]>();
		DataManagementService dataManagementService = DataManagementService.getService(session);
		dataManagementService.getProperties(parentLines, new String[] { PropertyDefines.BOM_ALL_MATERIAL });
		try {
			for (TCComponent parent : parentLines) {
				TCComponent[] returnedObjects = parent.getRelatedComponents(PropertyDefines.BOM_ALL_MATERIAL);
				expandLines.put(parent, returnedObjects);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return expandLines;
	}

	public static NodeInfo[] findBOMLineInBOP(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, String subGroup, int count, StringBuilder strBuilder) {
		boolean noLink = false;
		DataManagementService dmService = DataManagementService.getService(session);
		CoreService structService = CoreService.getService(session);
		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;
		for (NodeInfo bopNode : bomNodeBop) {

			TCComponentBOMLine original = (TCComponentBOMLine) bopNode.originalNode;
			TCComponent[] found = bopNode.foundNodes;

			try {

				if (found.length < 1) {

					dmService.getProperties(new TCComponent[] { original }, new String[] { "bl_formatted_parent_name", "bl_item_item_id", "VF4_bomline_id" });

					String ID = original.getPropertyDisplayableValue("bl_item_item_id");
					String BOMID = original.getPropertyDisplayableValue("VF4_bomline_id");

					if (subGroup.equals("")) {

						String[] parent = original.getPropertyDisplayableValue("bl_formatted_parent_name").split("/");
						subGroup = parent[0];
					}

					Logger.bufferResponse("PRINT", new String[] { Integer.toString(count), subGroup, ID, BOMID, "BOM Linked to " + found.length + " BOP", "-", "Error" }, strBuilder);
					count++;
					noLink = true;
				}

			} catch (NotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// DRYRUN noLink = false;
		if (noLink == true) {
			return null;
		}

		return bomNodeBop;
	}
	
	public static NodeInfo[] findBOMLineInBOP(TCSession session, BOMManager bomManager, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, String subGroup) {
		boolean noLink = false;
		DataManagementService dmService = DataManagementService.getService(session);
		CoreService structService = CoreService.getService(session);
		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;
		for (NodeInfo bopNode : bomNodeBop) {

			TCComponentBOMLine original = (TCComponentBOMLine) bopNode.originalNode;
			TCComponent[] found = bopNode.foundNodes;

			try {

				if (found.length < 1) {

					dmService.getProperties(new TCComponent[] { original }, new String[] { "bl_formatted_parent_name", "bl_item_item_id", "VF4_bomline_id" });

					String ID = original.getPropertyDisplayableValue("bl_item_item_id");
					String BOMID = original.getPropertyDisplayableValue("VF4_bomline_id");

					if (subGroup.equals("")) {

						String[] parent = original.getPropertyDisplayableValue("bl_formatted_parent_name").split("/");
						subGroup = parent[0];
					}

					bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), subGroup, ID, BOMID, "BOM Linked to " + found.length + " BOP", "-", "Error" });
					bomManager.incrementSerialNo();
					noLink = true;
				}

			} catch (NotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// DRYRUN noLink = false;
		if (noLink == true) {
			return null;
		}

		return bomNodeBop;
	}

	public static NodeInfo[] findBOMLineInBOPScooter(TCSession session,  BOMManager bomManager , TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, String subGroup) {

		DataManagementService dmService = DataManagementService.getService(session);
		CoreService structService = CoreService.getService(session);

		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;

		boolean noLink = false;

		for (NodeInfo bopNode : bomNodeBop) {
			TCComponent original = bopNode.originalNode;
			TCComponent[] found = bopNode.foundNodes;
			try {
				if (found.length == 0) {
					TCComponentBOMLine BL = (TCComponentBOMLine) original;
					dmService.getProperties(new TCComponent[] { BL }, new String[] { "bl_item_item_id", "VF4_bomline_id" });

					String ID = BL.getProperty("bl_item_item_id");
					String BOMID = BL.getProperty("VF4_bomline_id");

					bomManager.printReport("PRINT", new String[] { Integer.toString(bomManager.getSerialNo()), subGroup, ID, BOMID, "BOM Linked to " + found.length + " BOP", "-", "Error" });
					bomManager.incrementSerialNo();
					noLink = true;
				}
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (noLink == true) {
			return null;
		}

		return bomNodeBop;
	}

	public static NodeInfo[] findBOMLineInBOPScooterNoVerifyLink(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, String subGroup, int count, StringBuilder strBuilder) {

		CoreService structService = CoreService.getService(session);

		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;

		return bomNodeBop;
	}

	public static NodeInfo[] findBOMLineInBOP(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine) {
		CoreService structService = CoreService.getService(session);

		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;

		return bomNodeBop;
	}

	public static TCComponentItem findItem(TCSession session, String ID) {

		TCComponentItem item = null;
		DataManagementService dmService = DataManagementService.getService(session);

		Map<String, String> itemMap = new HashMap<String, String>();
		itemMap.put("item_id", ID);

		GetItemFromAttributeInfo attrInfo = new GetItemFromAttributeInfo();
		attrInfo.itemAttributes = itemMap;

		GetItemFromAttributeResponse response = dmService.getItemFromAttribute(new GetItemFromAttributeInfo[] { attrInfo }, 0, new GetItemFromIdPref());
		if (response.output != null && response.output.length > 0) {
			DataManagement.GetItemFromAttributeItemOutput[] itemOutput = response.output;
			item = itemOutput[0].item;
		}

		return item;
	}

	public HashMap<TCComponentBOMLine, ArrayList<BOMBOPData>> findLinkedLines(BOMManager BOMManager, HashMap<TCComponentBOMLine, TCComponentBOMLine[]> subGroups, int linkCount, String structure) {

		HashMap<TCComponentBOMLine, ArrayList<BOMBOPData>> dataSubGroupVector = new HashMap<TCComponentBOMLine, ArrayList<BOMBOPData>>();
		;
		TCComponent target = null;
		int iterator = 0;
		boolean isLinkError = false;

		try {

			TCSession session = BOMManager.getSession();

			DataManagementService dataManagementService = BOMManager.getDataManagementService();

			CoreService structService = CoreService.getService(session);

			if (structure.equals("BOM")) {

				target = BOMManager.getMBOMTraverseLine();
			}

			if (structure.equals("BOP")) {

				target = BOMManager.getBOPTraverseLine();
			}

			FindNodeInContextInputInfo[] findNodeInputInfo = new FindNodeInContextInputInfo[subGroups.size()];
			HashMap<String, TCComponentBOMLine> isList = new HashMap<String, TCComponentBOMLine>();

			for (TCComponentBOMLine parent : subGroups.keySet()) {

				TCComponentBOMLine[] nodes = subGroups.get(parent);

				String clientID = parent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
				isList.put(clientID, parent);
				findNodeInputInfo[iterator] = new FindNodeInContextInputInfo();
				findNodeInputInfo[iterator].clientID = clientID;
				findNodeInputInfo[iterator].context = target;
				findNodeInputInfo[iterator].nodes = nodes;
				findNodeInputInfo[iterator].allContexts = false;
				findNodeInputInfo[iterator].byIdOnly = true;
				findNodeInputInfo[iterator].relationDepth = 0;
				findNodeInputInfo[iterator].relationDirection = 1;
				findNodeInputInfo[iterator].relationTypes = new String[] { "FND_TraceLink" };

				iterator++;
			}

			FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(findNodeInputInfo);

			ServiceData serviceData = InContextInputResponse.serviceData;

			if (serviceData.sizeOfPartialErrors() > 0) {

				MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);

			} else {

				FoundNodesInfo[] foundNodeInfo = InContextInputResponse.resultInfo;

				if (foundNodeInfo.length != 0) {

					for (FoundNodesInfo foundNode : foundNodeInfo) {

						String subGroupID = foundNode.clientID;

						ArrayList<BOMBOPData> subGroupMap = null;

						NodeInfo[] linkedNodes = foundNode.resultNodes;

						if (linkedNodes.length != 0) {

							subGroupMap = new ArrayList<BOMBOPData>();

							for (NodeInfo nodeInfo : linkedNodes) {

								BOMBOPData dataMap = null;

								TCComponentBOMLine MBOMLine = null;
								TCComponent[] BOPLines = null;

								if (structure.equals("BOM")) {

									String[] properties = new String[3];
									properties[0] = PropertyDefines.BOM_BOM_ID;
									properties[1] = PropertyDefines.BOM_ITEM_ID;
									properties[2] = PropertyDefines.BOM_OBJECT_TYPE;

									BOPLines = new TCComponent[] { nodeInfo.originalNode };
									dataManagementService.getProperties(new TCComponent[] { BOPLines[0] }, properties);

									if (nodeInfo.foundNodes.length == linkCount) {

										MBOMLine = (TCComponentBOMLine) nodeInfo.foundNodes[0];
										dataMap = new BOMBOPData();
										dataMap.setMBOMLine(MBOMLine);
										dataMap.setMBOPLine(BOPLines);

									} else {

										String partID = BOPLines[0].getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
										String BOMID = BOPLines[0].getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
										String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroupID, partID, BOMID, "Linking error. Please correct BOP to BOM.", "-", "Error" };
										BOMManager.printReport("PRINT", printValues);
										isLinkError = true;
									}

								} else if (structure.equals("BOP")) {

									MBOMLine = (TCComponentBOMLine) nodeInfo.originalNode;
									dataManagementService.getProperties(new TCComponent[] { MBOMLine }, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_BOM_ID });
									if (nodeInfo.foundNodes.length == linkCount) {

										dataMap = new BOMBOPData();
										dataMap.setMBOMLine(MBOMLine);
										dataMap.setMBOPLine(nodeInfo.foundNodes);

									} else {

										String partID = MBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
										String BOMID = MBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
										String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), subGroupID, partID, BOMID, "Linking error. Please correct BOM to BOP.", "-", "Error" };
										BOMManager.printReport("PRINT", printValues);
										isLinkError = true;
									}
								}

								if (dataMap != null) {

									subGroupMap.add(dataMap);
								}
							}
						} else {

							subGroupMap = new ArrayList<BOMBOPData>();
						}

						dataSubGroupVector.put(isList.get(subGroupID), subGroupMap);
					}
				}
			}

			if (isLinkError) {
				return null;
			}
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataSubGroupVector;
	}

	public static TCComponentItemRevision findRevision(TCSession session, String ID, String Rev) {

		TCComponentItemRevision revision = null;
		DataManagementService dmService = DataManagementService.getService(session);

		Map<String, String> itemMap = new HashMap<String, String>();
		itemMap.put("item_id", ID);
		itemMap.put("rev_id", Rev);

		GetItemFromAttributeInfo attrInfo = new GetItemFromAttributeInfo();
		attrInfo.itemAttributes = itemMap;

		GetItemFromAttributeResponse response = dmService.getItemFromAttribute(new GetItemFromAttributeInfo[] { attrInfo }, 0, new GetItemFromIdPref());
		DataManagement.GetItemFromAttributeItemOutput[] itemOutput = response.output;
		GetItemFromAttributeItemRevOutput revOutput[] = itemOutput[0].itemRevOutput;
		revision = revOutput[0].itemRevision;

		return revision;
	}

	public LinkedHashMap<String, String> generateBOMStructure(BOMBOPData BOMData, String action) {

		LinkedHashMap<String, String> newbomValues = new LinkedHashMap<String, String>();

		newbomValues.put("MAINGROUP", BOMData.getMainGroup());
		newbomValues.put("SUBGROUP", BOMData.getSubGroup());
		newbomValues.put("PARTNO", BOMData.getPartNumber());
		newbomValues.put("BOMLINEID", BOMData.getBOMLineID());
		newbomValues.put("PLANT", BOMData.getPlantCode());
		newbomValues.put("QUANTITY", BOMData.getQuanity());
		newbomValues.put("OPTION", BOMData.getFormula());
		newbomValues.put("GROUPDESCRIPTION", BOMData.getDescription());
		newbomValues.put("DCR", BOMData.getDCR());
		newbomValues.put("PLATFORM", BOMData.getPlatform());
		if (action.equals("")) {
			newbomValues.put("ACTION", BOMData.getAction());
		} else {
			newbomValues.put("ACTION", action);
		}
		newbomValues.put("MODELYEAR", BOMData.getModelYear());
		newbomValues.put("SEQUENCE", BOMData.getSequenceTime());
		newbomValues.put("MCN", BOMData.getMCN());

		return newbomValues;
	}

	public HashMap<String, String> generateBOPStructure(BOMBOPData BOMData, String action) {

		HashMap<String, String> newbomValues = new HashMap<String, String>();

		newbomValues.put("MAINGROUP", BOMData.getMainGroup());
		newbomValues.put("BOMLINEID", BOMData.getBOMLineID());
		newbomValues.put("SAPPLANT", BOMData.getPlantCode());
		newbomValues.put("BOPID", BOMData.getBOPID());
		newbomValues.put("OPTION", "");
		newbomValues.put("GROUPDESCRIPTION", BOMData.getDescription());
		newbomValues.put("SUBGROUP", BOMData.getSubGroup());
		newbomValues.put("MESBOPINDICATOR", BOMData.getMESIndicator());
		newbomValues.put("PLATFORM", BOMData.getPlatform());

		if (action.equals("")) {
			newbomValues.put("ACTION", BOMData.getAction());
		} else {
			newbomValues.put("ACTION", action);
		}

		newbomValues.put("MODELYEAR", BOMData.getModelYear());
		newbomValues.put("LINESUPPLYMETHOD", BOMData.getLineSupplyMethod());
		newbomValues.put("SEQUENCE", BOMData.getSequenceTime());
		newbomValues.put("WORKSTATION", BOMData.getWorkStation());
		newbomValues.put("FAMILY_ADDR", BOMData.getFamilyAddress());
		newbomValues.put("L_R_HAND", BOMData.getLeftRightHand());
		newbomValues.put("MCN", BOMData.getMCN());
		newbomValues.put("REVISION", BOMData.getBOPRevision());

		return newbomValues;
	}

	public static TCComponentGroupMember getAccessor(TCSession session) {

		TCComponentGroupMember groupMember = null;

		try {

			SessionService SService = SessionService.getService(session);
			GetSessionGroupMemberResponse response = SService.getSessionGroupMember();
			ServiceData sd = response.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
				ErrorValue[] errorValue = errorStack.getErrorValues();
				for (int inx = 1; inx < errorValue.length; inx++) {
					System.out.println(errorValue[inx].getMessage());
				}

			} else {

				groupMember = response.groupMember;
			}

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return groupMember;
	}

	public LinkedHashMap<String, LinkedHashMap<String, String>> getBOMCompareList(String key, LinkedHashMap<String, LinkedHashMap<String, String>> oldBOMValuesMap) {

		LinkedHashMap<String, LinkedHashMap<String, String>> compareList = null;
		String[] values = null;
		String prefix = "";
		try {
			values = key.split("~");

			prefix = values[0] + "~" + values[1];

			if (values != null) {

				compareList = new LinkedHashMap<String, LinkedHashMap<String, String>>();

				for (String keyObj : oldBOMValuesMap.keySet()) {

					if (keyObj.startsWith(prefix)) {

						compareList.put(keyObj, oldBOMValuesMap.get(keyObj));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return compareList;
	}

	public LinkedHashMap<String, LinkedHashMap<String, String>> getBOPCompareList(Set<String> keys, LinkedHashMap<String, LinkedHashMap<String, String>> oldBOMValuesMap) {

		LinkedHashMap<String, LinkedHashMap<String, String>> compareList = null;

		if (keys.isEmpty() == false) {
			compareList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			for (String keyObj : keys) {

				compareList.put(keyObj, oldBOMValuesMap.get(keyObj));
			}
		}
		return compareList;
	}

	public LinkedHashMap<String, LinkedHashMap<String, String>> getOldBOMStructure(TCSession session, File oldBOM, String plantCode) {

		LinkedHashMap<String, LinkedHashMap<String, String>> oldValues = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(oldBOM));

			String sCurrentLine = "";

			while ((sCurrentLine = br.readLine()) != null) {

				LinkedHashMap<String, String> line = new LinkedHashMap<String, String>();

				String[] key_values = sCurrentLine.split("=", 2);

				if (key_values.length == 2) {

					if (plantCode.equals("3002")) {

						String[] bom = key_values[0].split("~");
						String[] bom_bop = key_values[1].split("~");

						line.put("PARENTPART", bom[1]);
						line.put("CHILDPART", bom[2]);
						line.put("BOMLINEID", bom[3]);
						line.put("PLANTCODE", bom_bop[0]);
						line.put("SAPPLANT", bom_bop[0]);
						line.put("QUANTITY", bom_bop[1]);
						line.put("TOPLEVELPART", bom_bop[2]);
						line.put("HEADERPART", bom_bop[3]);
						line.put("LINESUPPLYMETHOD", bom_bop[4]);
						line.put("BOPID", bom_bop[5]);
						line.put("WORKSTATION", bom_bop[6]);
						line.put("MESBOPINDICATOR", bom_bop[7]);
						line.put("ACTION", bom_bop[8]);
						line.put("MCN", bom_bop[9]);
						line.put("REVISION", bom_bop[10]);
						line.put("PHANTOM", bom_bop[11]);

						oldValues.put(key_values[0], line);

					} else {

						String[] bom = key_values[0].split("~");
						String[] bom_bop = key_values[1].split("~");

						line.put("MAINGROUP", bom[0]);
						line.put("SUBGROUP", bom[1]);
						line.put("PARTNO", bom[2]);
						line.put("BOMLINEID", bom[3]);
						line.put("SAPPLANT", bom_bop[0]);
						line.put("PLANT", bom_bop[0]);
						line.put("QUANTITY", bom_bop[1]);
						line.put("OPTION", bom_bop[2]);
						line.put("GROUPDESCRIPTION", bom_bop[3]);
						line.put("PLATFORM", bom_bop[4]);
						line.put("ACTION", bom_bop[5]);
						line.put("MODELYEAR", bom_bop[6]);
						line.put("BOPID", bom_bop[7]);
						line.put("WORKSTATION", bom_bop[8]);
						line.put("LINESUPPLYMETHOD", bom_bop[9]);
						line.put("MESBOPINDICATOR", bom_bop[10]);
						line.put("FAMILY_ADDR", bom_bop[11]);
						line.put("L_R_HAND", bom_bop[12]);
						line.put("MCN", bom_bop[13]);
						line.put("REVISION", bom_bop[14]);
						line.put("DCR", "");

						oldValues.put(key_values[0], line);

					}

				} else {

					return null;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Throwable e) {
					br = null;
				}
			}
		}
		return oldValues;
	}

	public TCComponentBOMLine getParentBOMLine(TCComponentBOMLine BOMLine, String type) {

		TCComponentBOMLine parent = null;

		try {

			parent = (TCComponentBOMLine) BOMLine.getReferenceProperty("bl_parent");

			if (parent.getItemRevision().getDisplayType().equalsIgnoreCase(type)) {

				return parent;
			} else {

				parent = getParentBOMLine(parent, type);
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parent;
	}

	// delete
	public static TCComponent getPlantForm(TCComponentItem materialItem, String plantCode) {
		ResourceBundle transferBundle = ResourceBundle.getBundle("Transfer");
		TCComponent plantForm = null;
		try {
			String materialType = materialItem.getType();
			String formRelation = transferBundle.getString(materialType + ".FORM");
			if (!formRelation.equals("")) {
				TCComponent[] plant_Forms = materialItem.getRelatedComponents(formRelation);
				for (TCComponent form : plant_Forms) {
					if (form.getProperty(transferBundle.getString(materialType + ".PLANT")).equals(plantCode)) {
						plantForm = form;
						break;
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plantForm;
	}

	public static String getPlantMakeBuy(TCComponent materialItem, String plantCode) {
		ResourceBundle transferBundle = ResourceBundle.getBundle("Transfer");
		String makeBuyValue = "";
		try {
			String materialType = materialItem.getType();
			String formRelation = transferBundle.getString(materialType + ".FORM");
			if (!formRelation.equals("")) {
				TCComponent[] plant_Forms = materialItem.getRelatedComponents(formRelation);
				for (TCComponent form : plant_Forms) {
					if (form.getProperty(transferBundle.getString(materialType + ".PLANT")).equals(plantCode)) {
						makeBuyValue = form.getTCProperty(transferBundle.getString(materialType + ".PURLEVEL")).getStringValue();
						if (makeBuyValue.length() > 1) {
							makeBuyValue = new EnumTypesUtil().getMakeBuy(makeBuyValue);
						}
						break;
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return makeBuyValue;
	}

	public static MaterialPlatformCode getPlatformCode(DataManagementService dataManagementService, TCComponentCfg0ProductItem context) {
		MaterialPlatformCode matplatformCode = new MaterialPlatformCode();
		dataManagementService.getProperties(new TCComponent[] { context }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ITEM_OBJECT_STR });
		try {
			String contextID = context.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			int contextIDLength = contextID.length();
			if (contextIDLength > 9) {
				matplatformCode.setMaterialCode(contextID);
				matplatformCode.setPlatformCode(contextID.substring(0, 4));
				matplatformCode.setModelYear(contextID.substring(5, 9));
			} else if (contextID.length() == 9) {
				matplatformCode.setMaterialCode("");
				matplatformCode.setPlatformCode(contextID.substring(0, 4));
				matplatformCode.setModelYear(contextID.substring(5, 9));
			} else {
				contextID = context.getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);
				matplatformCode.setMaterialCode("");
				matplatformCode.setPlatformCode(contextID.substring(0, 4));
				matplatformCode.setModelYear(contextID.substring(5, 9));
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return matplatformCode;
	}

	public static MaterialPlatformCode getPlatformCode(DataManagementService dataManagementService, TCComponentItemRevision topLevelBOMRevision) {
		MaterialPlatformCode matplatformCode = new MaterialPlatformCode();
		try {
			TCComponentItem topLevelBOMItem = topLevelBOMRevision.getItem();
			dataManagementService.getProperties(new TCComponent[] { topLevelBOMItem }, new String[] { PropertyDefines.ITEM_NAME, PropertyDefines.REL_VARIANT_CONFIG });
			TCComponent[] context = topLevelBOMItem.getReferenceListProperty(PropertyDefines.REL_VARIANT_CONFIG);
			if (context.length == 0) {
				matplatformCode.setMaterialCode("NA");
				matplatformCode.setPlatformCode("NA");
				matplatformCode.setModelYear("NA");
			} else {
				dataManagementService.getProperties(new TCComponent[] { context[0] }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ITEM_OBJECT_STR });
				String contextID = context[0].getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				int contextIDLength = contextID.length();
				if (contextIDLength > 9) {
					matplatformCode.setMaterialCode(contextID);
					matplatformCode.setPlatformCode(contextID.substring(0, 4));
					matplatformCode.setModelYear(contextID.substring(5, 9));
				} else if (contextID.length() == 9) {
					matplatformCode.setMaterialCode("");
					String tempContextID = topLevelBOMItem.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME);
					if (tempContextID.substring(0, 4).equals(contextID.substring(0, 4))) {
						matplatformCode.setPlatformCode(contextID.substring(0, 4));
					} else {
						matplatformCode.setPlatformCode(tempContextID.substring(0, 4));
					}

					if (tempContextID.substring(5, 9).equals(contextID.substring(5, 9))) {
						matplatformCode.setModelYear(contextID.substring(5, 9));
					} else {
						matplatformCode.setModelYear(tempContextID.substring(5, 9));
					}
				} else {
					contextID = context[0].getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);
					String tempContextID = topLevelBOMItem.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME);
					if (tempContextID.substring(0, 4).equals(contextID.substring(0, 4))) {
						matplatformCode.setPlatformCode(contextID.substring(0, 4));
					} else {
						matplatformCode.setPlatformCode(tempContextID.substring(0, 4));
					}

					if (tempContextID.substring(5, 9).equals(contextID.substring(5, 9))) {
						matplatformCode.setModelYear(contextID.substring(5, 9));
					} else {
						matplatformCode.setModelYear(tempContextID.substring(5, 9));
					}
					matplatformCode.setMaterialCode("NA");
				}
			}

		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return matplatformCode;
	}

	public static MaterialPlatformCode getPlatformCode(TCComponentItemRevision topLevelBOMRevision) {
		MaterialPlatformCode matplatformCode = new MaterialPlatformCode();
		try {
			TCComponentItem topLevelBOMItem = topLevelBOMRevision.getItem();
			TCComponent[] context = topLevelBOMItem.getReferenceListProperty(PropertyDefines.REL_VARIANT_CONFIG);
			if (context.length == 0) {
				matplatformCode.setMaterialCode("NA");
				matplatformCode.setPlatformCode("NA");
				matplatformCode.setModelYear("NA");
			} else {
				String contextID = context[0].getProperty(PropertyDefines.ITEM_ID);
				int contextIDLength = contextID.length();
				if (contextIDLength > 9 || contextID.length() == 9) {
					matplatformCode.setMaterialCode(contextID);
					matplatformCode.setPlatformCode(contextID.substring(0, 4));
					matplatformCode.setModelYear(contextID.substring(5, 9));
				} else {
					contextID = context[0].getPropertyDisplayableValue(PropertyDefines.ITEM_OBJECT_STR);
					String tempContextID = topLevelBOMItem.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME);
					if (tempContextID.substring(0, 4).equals(contextID.substring(0, 4))) {
						matplatformCode.setPlatformCode(contextID.substring(0, 4));
					} else {
						matplatformCode.setPlatformCode(tempContextID.substring(0, 4));
					}

					if (tempContextID.substring(5, 9).equals(contextID.substring(5, 9))) {
						matplatformCode.setModelYear(contextID.substring(5, 9));
					} else {
						matplatformCode.setModelYear(tempContextID.substring(5, 9));
					}
					matplatformCode.setMaterialCode("NA");
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return matplatformCode;
	}

	public static String[] getPreferenceValues(TCSession session, String prefName) {

		String[] IP_values = null;
		PreferenceManagementService preferenceService = PreferenceManagementService.getService(session);
		GetPreferencesResponse prefReponse = preferenceService.getPreferences(new String[] { prefName }, false);
		CompletePreference[] pref = prefReponse.response;
		PreferenceValue values = pref[0].values;
		IP_values = values.values;

		return IP_values;
	}

	public static TCComponent[] getRelatedComponents(DataManagementService dmService, TCComponent primaryObject, String[] objectType, String relation) {

		TCComponent[] relatedObj = null;

		RelationAndTypesFilter filter = new RelationAndTypesFilter();
		filter.otherSideObjectTypes = objectType;
		filter.relationTypeName = relation;

		ExpandGRMRelationsPref2 relationPref = new ExpandGRMRelationsPref2();
		relationPref.expItemRev = false;
		relationPref.returnRelations = false;
		relationPref.info = new RelationAndTypesFilter[] { filter };

		ExpandGRMRelationsResponse2 response = dmService.expandGRMRelationsForPrimary(new TCComponent[] { primaryObject }, relationPref);
		ExpandGRMRelationsOutput2[] output = response.output;
		ExpandGRMRelationsData2[] data = output[0].relationshipData;
		ExpandGRMRelationship[] relations = data[0].relationshipObjects;

		if (relations.length != 0) {
			
			ArrayList<TCComponent> list =  new ArrayList<TCComponent>();
			relatedObj = new TCComponent[relations.length];
			for (ExpandGRMRelationship relationObj : relations) {
				if(relationObj.otherSideObject != null) {
					list.add(relationObj.otherSideObject);
				}
			}
			if(list.isEmpty() == false) {
				return list.toArray(new TCComponent[0]);
			}
		}
		return relatedObj;
	}

	public static String[] getResponseError(String xml) {

		String[] value = { "", "" };
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));

			for (int temp = 0; temp < doc.getElementsByTagName("DataResponse").getLength(); temp++) {

				Node nNode = doc.getElementsByTagName("DataResponse").item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					NodeList node = eElement.getElementsByTagName("MessafgeCode");
					if (node.getLength() == 0) {
						node = eElement.getElementsByTagName("MessageCode");
					}

					value[0] = node.item(0).getTextContent();
					value[1] = eElement.getElementsByTagName("MessageContent").item(0).getTextContent();
					break;
				}
			}

			for (int temp = 0; temp < doc.getElementsByTagName("MSGRETURN").getLength(); temp++) {

				Node nNode = doc.getElementsByTagName("MSGRETURN").item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					NodeList node = eElement.getElementsByTagName("MSGINFOR");
					if (node.getLength() == 0) {
						node = eElement.getElementsByTagName("MSGINFO");
					}

					value[0] = eElement.getElementsByTagName("MSGTYPE").item(0).getTextContent();
					value[1] = node.item(0).getTextContent();
				}
			}

			for (int temp = 0; temp < doc.getElementsByTagName("ResultFlag").getLength(); temp++) {

				Node nNode = doc.getElementsByTagName("ResultFlag").item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					value[0] = nNode.getTextContent();
				}
			}

			for (int temp = 0; temp < doc.getElementsByTagName("ResultMessage").getLength(); temp++) {

				Node nNode = doc.getElementsByTagName("ResultMessage").item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					value[1] = nNode.getTextContent();
				}
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(value[0].equals("")) {
			value[0] = "E";
			value[1] = "Internal Error parsing request in SAP";
		}

		return value;
	}

	public static TCComponentRevisionRule getRevisionRule(String ruleObj, TCSession session) {

		TCComponentRevisionRule revRule = null;

		try {

			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			for (RevisionRuleInfo rule : RuleInfo) {

				if (rule.revRule.getProperty("object_name").equals(ruleObj)) {
					revRule = rule.revRule;
					break;
				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return revRule;

	}

	public static String getSequenceID() {
		String timeStamp = "";
		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime());

		return timeStamp;
	}

	public HashMap<String, BOMBOPData> getSubGroupLines(String key, HashMap<String, BOMBOPData> oldBOMValuesMap) {

		HashMap<String, BOMBOPData> compareList = new HashMap<String, BOMBOPData>();

		try {

			for (String keyObj : oldBOMValuesMap.keySet()) {

				if (keyObj.startsWith(key)) {

					compareList.put(keyObj, oldBOMValuesMap.get(keyObj));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return compareList;
	}

	public static TCComponentItemRevision getTopLevelItemRevision(TCSession session, TCComponentItemRevision shopRevision, String revRule) throws TCException {
		TCComponentItemRevision topLineRevision = shopRevision;
		TCComponentRevisionRule RevisionRule = null;
		if (revRule != null) {
			RevisionRule = getRevisionRule(revRule, session);
		}
		WhereUsedConfigParameters configparam = new WhereUsedConfigParameters();
		configparam.boolMap.put("whereUsedPreciseFlag", false);
		configparam.intMap.put("numLevels", new BigInteger("-2"));
		configparam.tagMap.put("revision_rule", RevisionRule);

		WhereUsedInputData inputData = new WhereUsedInputData();
		inputData.clientId = "";
		inputData.inputObject = shopRevision;
		inputData.useLocalParams = false;
		inputData.inputParams = configparam;

		WhereUsedResponse WUResponse = DataManagementService.getService(session).whereUsed(new WhereUsedInputData[] { inputData }, configparam);
		WhereUsedOutputData[] outputData = WUResponse.output;
		if (outputData.length > 0) {
			WhereUsedParentInfo[] info = outputData[0].info;
			for (WhereUsedParentInfo parentInfo : info) {
				TCComponentItemRevision topLineRevisionCandidate = (TCComponentItemRevision) parentInfo.parentObject;
				String foundTopType = topLineRevisionCandidate.getTypeComponent().getTypeName();
				if (PropertyDefines.VF_TOP_MBOM_TYPES.contains(foundTopType + ";")) {
					topLineRevision = topLineRevisionCandidate.getItem().getLatestItemRevision();
					break;
				}
			}
		}
		return topLineRevision;
	}

	public TCComponent[] getValidBOMLines(DataManagementService dmCoreService, ExpandPSOneLevelOutput impactedLine, int count, StringBuilder strBuilder) {

		ArrayList<String> duplicateBOMID = new ArrayList<String>();
		ArrayList<TCComponent> validBOMLinesVector = new ArrayList<TCComponent>();
		TCComponent[] validBOMLines = null;
		boolean isError = false;

		try {

			ExpandPSParentData parentData = impactedLine.parent;
			ExpandPSData[] childerns = impactedLine.children;

			TCComponent subGroup = parentData.itemRevOfBOMLine;

			String subGroupID = subGroup.getPropertyDisplayableValue("item_id");
			String subGroupRev = subGroup.getPropertyDisplayableValue("item_revision_id");

			if (childerns.length != 0) {

				TCComponent[] BOMLines = new TCComponent[childerns.length];

				for (int i = 0; i < childerns.length; i++) {

					BOMLines[i] = childerns[i].bomLine;
				}

				String[] prop_Values = new String[] { "bl_rev_object_type", "VL5_purchase_lvl_vf", "VF3_purchase_lvl_vf", "VF4_manuf_code", "VF4_bomline_id", "bl_item_item_id", "bl_quantity", "bl_formula" };

				dmCoreService.getProperties(BOMLines, prop_Values);

				for (TCComponent childLine : BOMLines) {

					String ID = childLine.getPropertyDisplayableValue("bl_item_item_id");

					String purchaseLevel = childLine.getPropertyDisplayableValue("VF3_purchase_lvl_vf");

					if (purchaseLevel.isEmpty()) {

						purchaseLevel = childLine.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
					}

					String BOMLineID = childLine.getPropertyDisplayableValue("VF4_bomline_id");

					if (isToProcessAssembly(childLine.getPropertyDisplayableValue("bl_rev_object_type"), childLine.getPropertyDisplayableValue("VF4_manuf_code")) == true) {

						if (isBOMLinePurchase(purchaseLevel.trim()) == true) {
							String errorMessage = "";

							int bomLineIDCount = BOMLineID.trim().length();
							int purchaseLevelCount = purchaseLevel.trim().length();

							if (bomLineIDCount == 0 || purchaseLevelCount == 0) {

								if (bomLineIDCount == 0) {

									if (errorMessage.length() == 0) {
										errorMessage = "BOMLine ID";
									} else {
										errorMessage = errorMessage + ",BOMLine ID";
									}
								}
								if (purchaseLevelCount == 0) {

									if (errorMessage.length() == 0) {
										errorMessage = "Purchase Level";
									} else {
										errorMessage = errorMessage + ",Purchase Level";
									}
								}
							}

							if (errorMessage.length() != 0) {

								String[] response = new String[] { Integer.toString(count), subGroupID + "/" + subGroupRev, ID, BOMLineID, errorMessage + " mandatory value(s) are empty.", "-", "Error" };
								Logger.bufferResponse("PRINT", response, strBuilder);
								count++;
								isError = true;
								break;

							} else {

								if (duplicateBOMID.contains(BOMLineID.trim())) {

									errorMessage = "BOMLine ID";
									String[] response = new String[] { Integer.toString(count), subGroupID + "/" + subGroupRev, ID, BOMLineID, errorMessage + " value(s) is duplicate.", "-", "Error" };
									Logger.bufferResponse("PRINT", response, strBuilder);
									count++;
									isError = true;
									break;
								} else {

									duplicateBOMID.add(BOMLineID.trim());
								}
							}

							if (errorMessage.length() == 0) {

								validBOMLinesVector.add(childLine);
							}
						}
					}
				}

				if (validBOMLinesVector.isEmpty() == false && isError == false) {

					validBOMLines = new TCComponent[validBOMLinesVector.size()];

					for (int i = 0; i < validBOMLinesVector.size(); i++) {

						validBOMLines[i] = validBOMLinesVector.get(i);
					}
				}
			}

		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return validBOMLines;
	}

	public static String getSingleWorkStationID(TCComponentBOMLine operation, String plm_tag) {

		String workstationID = "";
		TCComponentBOMLine procLine = null;
		TCComponentBOMLine shopLine = null;
		try {

			TCComponentItemRevision operationRevision = operation.getItemRevision();
			if (operationRevision.getType().equals("MEOPRevision")) {

				TCComponentBOMLine workstation = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
				TCComponentItemRevision workstationRevision = workstation.getItemRevision();

				if (workstationRevision.getType().equals("Mfg0MEProcStatnRevision")) {
					procLine = (TCComponentBOMLine) workstation.getReferenceProperty("bl_parent");
					shopLine = (TCComponentBOMLine) procLine.getReferenceProperty("bl_parent");// PS
				}

				workstationID = generateWorkplaceIDFromStation(plm_tag, workstation, workstationRevision, shopLine);
			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}

	public static String getWorkStationID(TCComponentBOMLine operation, String plm_tag) {
		String workstationID = "";
		TCComponentBOMLine procLine = null;
		TCComponentBOMLine shopLine = null;
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		try {

			TCComponentItemRevision operationRevision = operation.getItemRevision();
			if (operationRevision.getType().equals("MEOPRevision")) {

				TCComponentBOMLine workstation = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
				TCComponentItemRevision workstationRevision = workstation.getItemRevision();

				if (workstationRevision.getType().equals("Mfg0MEProcStatnRevision")) {
					procLine = (TCComponentBOMLine) workstation.getReferenceProperty("bl_parent");
					shopLine = (TCComponentBOMLine) procLine.getReferenceProperty("bl_parent");// PS
				}

				workstationID = generateWorkplaceIDFromStation(plm_tag, workstation, workstationRevision, shopLine);

				AIFComponentContext[] opChildren = operation.getChildren();
				for (AIFComponentContext opChild : opChildren) {
					TCComponentBOMLine stationLine = (TCComponentBOMLine) opChild.getComponent();
					TCComponentItem wsItem = stationLine.getItem();
					String wsID = wsItem.getProperty("item_id");
					if (wsItem.getType().equals("MEStation")) {
						ArrayList<TCComponent> allWorkstationLines = searchPartsInStruture(session, new String[] { wsID }, shopLine);
						for (TCComponent allWorkstationComp : allWorkstationLines) {
							TCComponentBOMLine allWorkstationLine = (TCComponentBOMLine) allWorkstationComp;
							TCComponentBOMLine parentLine = (TCComponentBOMLine) allWorkstationLine.getReferenceProperty("bl_parent");
							if (allWorkstationLine.equals(stationLine) || !allWorkstationLine.getItem().getType().equals("MEStation") || !parentLine.getItem().getType().equals("Mfg0MEProcStatn"))
								continue;

							TCComponentBOMLine proceesWS = (TCComponentBOMLine) allWorkstationLine.getReferenceProperty("bl_parent");
							System.out.println("");
							String otherWorkplaceID = generateWorkplaceIDFromStation(plm_tag, proceesWS, proceesWS.getItemRevision(), shopLine);
							workstationID += "," + otherWorkplaceID;

						}
					}
				}

			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}

	private static String generateWorkplaceIDFromStation(String plm_tag, TCComponentBOMLine workstation, TCComponentItemRevision workstationRevision, TCComponentBOMLine shopLineOUT) throws TCException {
		TCComponentBOMLine procLine;
		TCComponentBOMLine topLine;
		String workstationID = "";
		if (workstationRevision.getType().equals("Mfg0MEProcStatnRevision")) {

			String workStationName = workstation.getProperty(plm_tag);// PT-01LH
			String workStatName = workStationName.substring(workStationName.length()-4).trim();

			procLine = (TCComponentBOMLine) workstation.getReferenceProperty("bl_parent");
			String procLineName = procLine.getProperty(plm_tag).substring(0, 2).trim();// PT

			String shopName = "";
			String plantName = "";
			shopLineOUT = (TCComponentBOMLine) procLine.getReferenceProperty("bl_parent");// PS

			if (shopLineOUT.getItemRevision().getType().equals("Mfg0MEPlantBOPRevision")) {
				try {
					shopName = procLineName;
					plantName = shopLineOUT.getProperty(plm_tag).substring(0, 4).trim();
				} catch (Exception e) {
					return "";
				}
			} else {
				try {
					shopName = shopLineOUT.getProperty(plm_tag).substring(0, 2).trim();
					topLine = (TCComponentBOMLine) shopLineOUT.getReferenceProperty("bl_parent");
					plantName = topLine.getProperty(plm_tag).substring(0, 4).trim();
				} catch (Exception e) {
					return "";
				}
			}

			workStatName = (shopName.contentEquals("EM") && procLineName.contains("EC") && workStationName.startsWith("EC_")) ? workStationName : workStatName;
			workstationID = plantName + "_" + shopName + procLineName + workStatName;
		}
		return workstationID;
	}

	public static TCComponent[] hasMaterials(DataManagementService dataManagementService, TCComponent[] parents) {

		TCComponent[] noChildComponent = null;
		dataManagementService.getProperties(parents, new String[] { PropertyDefines.BOM_OPERATION_TYPE, PropertyDefines.REL_PR4D_CAD });
		boolean hasMaterial = false;

		try {
			ArrayList<TCComponent> noChildItems = new ArrayList<TCComponent>();

			for (TCComponent parent : parents) {

				hasMaterial = false;

				String OperationType = parent.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);

				if ((OperationType.equals("") || OperationType.equals("NA")) == false) {

					TCComponent[] childObjects = parent.getRelatedComponents(PropertyDefines.REL_PR4D_CAD);

					if (childObjects.length != 0) {

						dataManagementService.getProperties(childObjects, new String[] { PropertyDefines.BOM_OBJECT_TYPE });

						for (TCComponent object : childObjects) {

							String object_Type = object.getPropertyDisplayableValue(PropertyDefines.ITEM_TYPE);

							if (object_Type.equals(PropertyDefines.TYPE_MECNTOOL) == false) {

								hasMaterial = true;
								break;
							}
						}
					}
				} else {

					hasMaterial = true;
				}

				if (hasMaterial == false) {

					noChildItems.add(parent);
				}
			}

			if (!noChildItems.isEmpty()) {

				noChildComponent = new TCComponent[noChildItems.size()];
				noChildComponent = noChildItems.toArray(noChildComponent);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return noChildComponent;
	}

	public static boolean hasMaterials(TCComponentBOMLine operationLine) {

		boolean hasMaterial = false;
		try {
			String OperationType = operationLine.getProperty(PropertyDefines.BOM_OPERATION_TYPE);

			if ((OperationType.equals("") || OperationType.equals("NA")) == false) {

				AIFComponentContext[] operationChildren = operationLine.getChildren();

				for (int jnx = 0; jnx < operationChildren.length; jnx++) {

					TCComponentBOMLine childLine = (TCComponentBOMLine) operationChildren[jnx].getComponent();

					String object_Type = childLine.getProperty(PropertyDefines.BOM_OBJECT_TYPE);

					if ((object_Type.equals("Mfg0MENCToolRevision") == false && object_Type.equals("MEStationRevision") == false)) {

						return true;
					}
				}

			} else {
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hasMaterial;
	}

	public static boolean isBOMLinePurchase(String purchaseLevel) {
		boolean isPurchase = false;
		try {
			if (purchaseLevel.equalsIgnoreCase("P") || purchaseLevel.equalsIgnoreCase("IB") || purchaseLevel.equalsIgnoreCase("K")) {
				isPurchase = true;
				return isPurchase;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return isPurchase;
	}

	public boolean isPurchaseMaterial(TCComponentItemRevision solItemRevision, String plant_Code) {
		String prop_Values = "";
		boolean isPurchase = false;
		try {
			TCComponentItem item = solItemRevision.getItem();
			String object_Type = item.getType();

			if (!object_Type.equals("VF3_vfitem")) {

				String values = bundle.getString(object_Type + ".PROP");
				if (!values.equals("")) {
					String split[] = values.split(",");
					AIFComponentContext[] plant_Forms = item.getRelated(split[0]);
					for (AIFComponentContext form : plant_Forms) {

						TCComponentForm plantForm = (TCComponentForm) form.getComponent();
						if (plantForm.getProperty(split[1]).equals(plant_Code)) {
							prop_Values = plantForm.getProperty(split[2]);
						}
					}
				}
				if (prop_Values.equals("Purchase")) {
					isPurchase = true;
					return isPurchase;
				}
			} else {

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return isPurchase;
	}

	public static boolean isToProcessAssembly(String objectType, String manufCode) {
		boolean isToProcess = true;
		try {
			if ((objectType.equals("Structure Part") == true || objectType.equals("VF Item Revision") == true || manufCode.equalsIgnoreCase("Assy"))) {
				isToProcess = false;
				return isToProcess;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return isToProcess;
	}

	public String MESSERVERIP(String server) {

		String serverIP = null;

		switch (server) {
		case "PRODUCTION":
			serverIP = "vfs-po-pr";
			break;
		case "QA":
			serverIP = "vfs-po-qa";
			break;
		case "DEV":
			serverIP = "vfs-po-dev";
			break;
		}

		return serverIP;
	}

	public static HashMap<String, BOMBOPData> previousAssyBOMStructure(TCSession session, File oldBOM, String MCN) {

		HashMap<String, BOMBOPData> currentMBOMStructure = null;
		BufferedReader bufferReader = null;
		try {

			bufferReader = new BufferedReader(new FileReader(oldBOM));

			if (bufferReader.readLine() != null) {

				String sCurrentLine = "";

				currentMBOMStructure = new HashMap<String, BOMBOPData>();

				while ((sCurrentLine = bufferReader.readLine()) != null) {

					BOMBOPData BOMBOPData = new BOMBOPData();

					String[] key_values = sCurrentLine.split("=", 2);

					if (key_values.length == 2) {

						String[] bom = key_values[0].split("~");
						String[] bom_bop = key_values[1].split("~");

						BOMBOPData.setParentPart(bom[0]);
						BOMBOPData.setChildPart(bom[1]);
						BOMBOPData.setBOMLineID(bom[2]);

						BOMBOPData.setPlantCode(bom_bop[0]);
						BOMBOPData.setQuanity(bom_bop[1]);
						BOMBOPData.setTopLevelPart(bom_bop[2]);
						BOMBOPData.setHeaderPart(bom_bop[3]);
						BOMBOPData.setLineSupplyMethod(bom_bop[4]);
						BOMBOPData.setBOPID(bom_bop[5]);
						BOMBOPData.setWorkStation(bom_bop[6]);
						BOMBOPData.setMESIndicator(bom_bop[7]);
						BOMBOPData.setAction("A");
						BOMBOPData.setMCN(MCN);
						BOMBOPData.setBOPRevision(bom_bop[10]);
						BOMBOPData.setPhantom(bom_bop[11]);

						currentMBOMStructure.put(key_values[0], BOMBOPData);

					} else {

						return null;
					}
				}
			}

			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (Throwable e) {
					bufferReader = null;
				}
			}
		}
		return currentMBOMStructure;
	}

	public static HashMap<String, BOMBOPData> previousSuperBOMStructure(TCSession session, File oldBOM, String MCN) {

		HashMap<String, BOMBOPData> currentMBOMStructure = null;
		BufferedReader bufferReader = null;
		try {

			bufferReader = new BufferedReader(new FileReader(oldBOM));

			if (bufferReader.ready()) {

				String sCurrentLine = "";

				currentMBOMStructure = new HashMap<String, BOMBOPData>();

				while ((sCurrentLine = bufferReader.readLine()) != null) {

					BOMBOPData BOMBOPData = new BOMBOPData();

					String[] key_values = sCurrentLine.split("=", 2);

					if (key_values.length == 2) {

						String[] bom = key_values[0].split("~");
						String[] bom_bop = key_values[1].split("~");

						if (bom.length >= 4) {
							BOMBOPData.setMainGroup(bom[0]);
							BOMBOPData.setSubGroup(bom[1]);
							BOMBOPData.setPartNumber(bom[2]);
							BOMBOPData.setBOMLineID(bom[3]);
							BOMBOPData.setOperationNoPart(bom[3].isEmpty());
						} else {
							BOMBOPData.setMainGroup("");
							BOMBOPData.setSubGroup("");
							BOMBOPData.setPartNumber("");
							BOMBOPData.setBOMLineID("");
							BOMBOPData.setOperationNoPart(true);
							BOMBOPData.processDataOnActionType(ActionType.DELETE_OPERATION_NO_PART);
						}

						BOMBOPData.setPlantCode(bom_bop[0]);
						BOMBOPData.setQuanity(bom_bop[1]);
						BOMBOPData.setFormula(bom_bop[2]);
						BOMBOPData.setDescription(bom_bop[3]);
						BOMBOPData.setPlatform(bom_bop[4]);
						// BOMBOPData.setAction(bom_bop[5]);
						BOMBOPData.setModelYear(bom_bop[6]);
						BOMBOPData.setBOPID(bom_bop[7]);
						BOMBOPData.setWorkStation(bom_bop[8]);
						BOMBOPData.setLineSupplyMethod(bom_bop[9]);
						BOMBOPData.setMESIndicator(bom_bop[10]);
						BOMBOPData.setFamilyAddress(bom_bop[11]);
						BOMBOPData.setLeftRightHand(bom_bop[12]);
						BOMBOPData.setMCN(MCN);
						BOMBOPData.setBOPRevision(bom_bop[14]);
						BOMBOPData.setDCR("");

						currentMBOMStructure.put(key_values[0], BOMBOPData);

					} else {

						return null;
					}
				}
			}

			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (Throwable e) {
					bufferReader = null;
				}
			}
		}
		return currentMBOMStructure;
	}

	public LinkedHashMap<String, String> readOldFile(TCSession session, File oldBOM) {

		LinkedHashMap<String, String> oldValues = null;
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(oldBOM));
			String sCurrentLine = "";
			int line_Count = 1;
			oldValues = new LinkedHashMap<String, String>();

			while ((sCurrentLine = br.readLine()) != null) {

				String[] key_values = sCurrentLine.split("=", 2);
				if (key_values.length == 2) {
					oldValues.put(key_values[0], key_values[1]);
				} else {
					System.out.println("Error in parsing line:-" + line_Count);
					return null;
				}
				line_Count++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Throwable e) {
					br = null;
				}
			}
		}
		return oldValues;
	}

	public static String SAPSERVERIP(String server) {

		String serverIP = null;

		switch (server) {
		case "PRODUCTION":
			serverIP = PropertyDefines.SERVER_SAP_VF_PROD;
			break;
		case "QA":
			serverIP = PropertyDefines.SERVER_SAP_VF_QA;
			break;
		case "DEV":
			serverIP = PropertyDefines.SERVER_SAP_VF_DEV;
			break;
		}

		return serverIP;
	}

	public static TCComponent[] searchDataset(TCSession session, String[] entires, String[] values, String query_name) {
		TCComponent[] datasets = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput findQuery = new FindSavedQueriesCriteriaInput();
			findQuery.queryNames = new String[] { query_name };
			findQuery.queryDescs = new String[] { "" };
			findQuery.queryType = 0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] { findQuery });
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput savedQuery = new SavedQueryInput();

			savedQuery.query = (TCComponentQuery) result[0];
			savedQuery.entries = entires;
			savedQuery.values = values;
			savedQuery.limitListCount = 10;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(new SavedQueryInput[] { savedQuery });

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {

				datasets = results[0].objects;
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return datasets;
	}

	public static ArrayList<TCComponent> searchPartsInStruture(TCSession session, String[] IDs, TCComponent impactedShop) {
		TCComponent[] comps = searchStruture(session, String.join(";", IDs), impactedShop);
		ArrayList<TCComponent> searchParts = new ArrayList<TCComponent>();
		if (comps != null) {
			searchParts.addAll(Arrays.asList(comps));
		}
		return searchParts;

	}

	public static TCComponent[] searchStruture(TCSession session, String searchObjects, TCComponent searchScope) {
		StructureSearchInMPPInput input = new StructureSearchInMPPInput();
		input.queryName = "Item...";
		input.queryCriteria = new String[] { "Item ID" };
		input.queryCriteriaValues = new String[] { searchObjects };
		input.searchScope = (TCComponentBOMLine) searchScope;
		IntegrationService sv = IntegrationService.getService(session);
		StructureSearchInMPPOutput output = sv.structureSearchInMPP(input);
		if (output.errorString.isEmpty()) {
			return output.foundLines;
		}
		return null;
	}

	public static boolean setProperty(DataManagementService dmService, TCComponent object, String property, String value) {
		boolean result = false;
		NameValueStruct1 tStruct = new NameValueStruct1();
		tStruct.name = property;
		tStruct.values = new String[] { value };

		PropInfo propertInfo = new PropInfo();
		propertInfo.object = object;
		propertInfo.vecNameVal = new NameValueStruct1[] { tStruct };

		SetPropertyResponse response = dmService.setProperties(new PropInfo[] { propertInfo }, new String[] {});
		ServiceData sd = response.data;

		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 0; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}

		} else {
			return true;
		}
		return result;
	}

	public static boolean setViewReference(TCSession session, TCComponent context, TCComponent refContext) {
		if (session == null || context == null || refContext == null)
			return false;
		ReferencedContexts contextInfo = new ReferencedContexts();
		contextInfo.context = refContext;
		contextInfo.addRefContexts = new TCComponent[] { context };
		contextInfo.removeExistingRef = false;

		com.teamcenter.services.rac.manufacturing.StructureManagementService structService = com.teamcenter.services.rac.manufacturing.StructureManagementService.getService(session);
		ServiceData sd = structService.setReferenceContexts(new ReferencedContexts[] { contextInfo });
		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 1; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}
			return false;
		}

		return true;
	}

	public static ExpandPSData[] traverseSingleLevelBOM(TCSession session, TCComponentBOMLine bomLine) {

		StructureManagementService SMService = StructureManagementService.getService(session);
		ExpandPSOneLevelInfo oneLevelInfo = new ExpandPSOneLevelInfo();
		oneLevelInfo.parentBomLines = new TCComponentBOMLine[] { bomLine };
		oneLevelInfo.excludeFilter = "None";

		ExpandPSOneLevelResponse expandOutput = SMService.expandPSOneLevel(oneLevelInfo, new ExpandPSOneLevelPref());
		ExpandPSOneLevelOutput output[] = expandOutput.output;
		if (output.length > 0)
			return output[0].children;

		return null;
	}

	public void uploadAssyBOMStructure(TCSession session, File newBOM, HashMap<String, BOMBOPData> assyBOMData) {

		BufferedWriter output = null;
		try {

			output = new BufferedWriter(new FileWriter(newBOM));

			for (Entry<String, BOMBOPData> mapKey : assyBOMData.entrySet()) {

				String key = "";
				String Value = "";
				BOMBOPData bomLinesArray = mapKey.getValue();

				String parentValue = bomLinesArray.getParentPart();
				String childValue = bomLinesArray.getChildPart();
				String bomlineValue = bomLinesArray.getBOMLineID();

				String plantValue = bomLinesArray.getPlantCode();
				String qtyValue = bomLinesArray.getQuanity();
				String headerValue = bomLinesArray.getHeaderPart();
				String topLevelValue = bomLinesArray.getTopLevelPart();
				String actionValue = bomLinesArray.getAction();
				String BOPID = bomLinesArray.getBOPID();
				String WSID = bomLinesArray.getWorkStation();
				String lineSupplyMethod = bomLinesArray.getLineSupplyMethod();
				String MESValue = bomLinesArray.getMESIndicator();
				String mcnValue = bomLinesArray.getMCN();
				String BOPRevision = bomLinesArray.getBOPRevision();
				String phantomValue = bomLinesArray.getPhantom();

				key = parentValue + "~" + childValue + "~" + bomlineValue;
				Value = plantValue + "~" + qtyValue + "~" + topLevelValue + "~" + headerValue + "~" + lineSupplyMethod + "~" + BOPID + "~" + WSID + "~" + MESValue + "~" + actionValue + "~" + mcnValue + "~" + BOPRevision + "~" + phantomValue;

				output.write(key + "=" + Value);
				output.newLine();
			}
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void uploadNamedReference(TCSession session, TCComponentDataset newDataset, File uploadFile, String namedRefName, boolean isText, boolean allowReplace) {

		FileManagementService fileManagmentService = FileManagementService.getService(session);
		FileManagementUtility fileManagmentUtility = new FileManagementUtility(session.getSoaConnection());

		DatasetFileInfo dsFileInfo = new DatasetFileInfo();
		dsFileInfo.clientId = "uploadFile";
		dsFileInfo.fileName = uploadFile.getName();
		dsFileInfo.isText = isText;
		dsFileInfo.allowReplace = allowReplace;
		dsFileInfo.namedReferencedName = namedRefName;

		GetDatasetWriteTicketsInputData writeTicketData = new GetDatasetWriteTicketsInputData();
		writeTicketData.dataset = newDataset;
		writeTicketData.createNewVersion = true;
		writeTicketData.datasetFileInfos = new DatasetFileInfo[] { dsFileInfo };

		GetDatasetWriteTicketsResponse response = fileManagmentService.getDatasetWriteTickets(new GetDatasetWriteTicketsInputData[] { writeTicketData });
		ServiceData serviceData = response.serviceData;

		if (serviceData.sizeOfPartialErrors() == 0) {

			String ticket = response.commitInfo[0].datasetFileTicketInfos[0].ticket;

			fileManagmentUtility.putFileViaTicket(ticket, uploadFile);

			DatasetFileTicketInfo ticketInfo = new DatasetFileTicketInfo();
			ticketInfo.datasetFileInfo = dsFileInfo;
			ticketInfo.ticket = ticket;

			CommitDatasetFileInfo commitDSFileInfo = new CommitDatasetFileInfo();
			commitDSFileInfo.createNewVersion = true;
			commitDSFileInfo.dataset = newDataset;
			commitDSFileInfo.datasetFileTicketInfos = new DatasetFileTicketInfo[] { ticketInfo };

			ServiceData sd = fileManagmentService.commitDatasetFiles(new CommitDatasetFileInfo[] { commitDSFileInfo });
			sd.sizeOfPartialErrors();
		}
	}

	public static void uploadSuperBOMStructure(TCSession session, File newBOM, ArrayList<BOMBOPData> superBOMData) {

		BufferedWriter output = null;
		try {

			output = new BufferedWriter(new FileWriter(newBOM));

			for (BOMBOPData mapKey : superBOMData) {

				String key = "";
				String Value = "";
				BOMBOPData bomLinesArray = mapKey;
				String mainGroupValue = bomLinesArray.getMainGroup();
				String subGroupValue = bomLinesArray.getSubGroup();
				String partValue = bomLinesArray.getPartNumber();
				String bomlineValue = bomLinesArray.getBOMLineID();

				String plantValue = bomLinesArray.getPlantCode();
				String qtyValue = bomLinesArray.getQuanity();
				String optionValue = bomLinesArray.getFormula();
				String groupValue = bomLinesArray.getDescription();
				String platformValue = bomLinesArray.getPlatform();
				String actionValue = bomLinesArray.getAction();
				String modelValue = bomLinesArray.getModelYear();
				String BOPID = bomLinesArray.getBOPID();
				String WSID = bomLinesArray.getWorkStation();
				String lineSupplyMethod = bomLinesArray.getLineSupplyMethod();
				String MESValue = bomLinesArray.getMESIndicator();
				String family_addr = bomLinesArray.getFamilyAddress();
				String lrhandValue = bomLinesArray.getLeftRightHand();
				String mcnValue = bomLinesArray.getMCN();
				String BOPRevision = bomLinesArray.getBOPRevision();

				key = mainGroupValue + "~" + subGroupValue + "~" + partValue + "~" + bomlineValue;
				Value = plantValue + "~" + qtyValue + "~" + optionValue + "~" + groupValue + "~" + platformValue + "~" + actionValue + "~" + modelValue + "~" + BOPID + "~" + WSID + "~" + lineSupplyMethod + "~" + MESValue + "~" + family_addr + "~" + lrhandValue + "~" + mcnValue + "~" + BOPRevision;

				output.write(key + "=" + Value);
				output.newLine();
			}
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void uploadSuperBOMStructure(TCSession session, File newBOM, HashMap<String, BOMBOPData> superBOMData) {

		BufferedWriter output = null;
		try {

			output = new BufferedWriter(new FileWriter(newBOM));

			for (Entry<String, BOMBOPData> mapKey : superBOMData.entrySet()) {

				String key = "";
				String Value = "";
				BOMBOPData bomLinesArray = mapKey.getValue();
				String mainGroupValue = bomLinesArray.getMainGroup();
				String subGroupValue = bomLinesArray.getSubGroup();
				String partValue = bomLinesArray.getPartNumber();
				String bomlineValue = bomLinesArray.getBOMLineID();

				String plantValue = bomLinesArray.getPlantCode();
				String qtyValue = bomLinesArray.getQuanity();
				String optionValue = bomLinesArray.getFormula();
				String groupValue = bomLinesArray.getDescription();
				String platformValue = bomLinesArray.getPlatform();
				String actionValue = bomLinesArray.getAction();
				String modelValue = bomLinesArray.getModelYear();
				String BOPID = bomLinesArray.getBOPID();
				String WSID = bomLinesArray.getWorkStation();
				String lineSupplyMethod = bomLinesArray.getLineSupplyMethod();
				String MESValue = bomLinesArray.getMESIndicator();
				String family_addr = bomLinesArray.getFamilyAddress();
				String lrhandValue = bomLinesArray.getLeftRightHand();
				String mcnValue = bomLinesArray.getMCN();
				String BOPRevision = bomLinesArray.getBOPRevision();

				key = mainGroupValue + "~" + subGroupValue + "~" + partValue + "~" + bomlineValue;
				Value = plantValue + "~" + qtyValue + "~" + optionValue + "~" + groupValue + "~" + platformValue + "~" + actionValue + "~" + modelValue + "~" + BOPID + "~" + WSID + "~" + lineSupplyMethod + "~" + MESValue + "~" + family_addr + "~" + lrhandValue + "~" + mcnValue + "~" + BOPRevision;

				output.write(key + "=" + Value);
				output.newLine();
			}
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static TCComponent[] fetchValidScooterBOMItems(DataManagementService dmCoreService, BOMManager bomManager, ExpandPSData[] BOMItems, String subGroup) throws Exception {

		ArrayList<String> duplicateBOMID = new ArrayList<String>();
		ArrayList<TCComponent> validBOMLinesVector = new ArrayList<TCComponent>();
		TCComponent[] validBOMLines = null;
		boolean isError = false;

		String[] prop_Values = new String[] { "bl_rev_object_type", "VL5_purchase_lvl_vf", "VF3_purchase_lvl_vf", "VF4_manuf_code", "VF4_bomline_id", "bl_item_item_id", "bl_quantity", "bl_formula" };

		try {

			TCComponent[] BOMLines = new TCComponent[BOMItems.length];

			for (int i = 0; i < BOMItems.length; i++) {

				BOMLines[i] = BOMItems[i].bomLine;
			}

			dmCoreService.getProperties(BOMLines, prop_Values);
			dmCoreService.refreshObjects(BOMLines);
			for (TCComponent childLine : BOMLines) {

				String ID = childLine.getProperty("bl_item_item_id");

				String purchaseLevel = childLine.getProperty("VL5_purchase_lvl_vf");

				String manufCode = childLine.getProperty("VF4_manuf_code");

				String type = childLine.getProperty("bl_rev_object_type");

				String BOMLineID = childLine.getProperty("VF4_bomline_id");

				if (isToProcessAssembly(type.trim(), manufCode.trim()) == true) {

					if (isBOMLinePurchase(purchaseLevel.trim()) == true) {
						String errorMessage = "";

						int bomLineIDCount = BOMLineID.trim().length();
						int purchaseLevelCount = purchaseLevel.trim().length();

						if (bomLineIDCount == 0 || purchaseLevelCount == 0) {

							if (bomLineIDCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "BOMLine ID";
								} else {
									errorMessage = errorMessage + ",BOMLine ID";
								}
							}
							if (purchaseLevelCount == 0) {

								if (errorMessage.length() == 0) {
									errorMessage = "Purchase Level";
								} else {
									errorMessage = errorMessage + ",Purchase Level";
								}
							}
						}

						String validateFlexError = validateFlexPart(childLine, dmCoreService);

						if (!errorMessage.isEmpty() && !validateFlexError.isEmpty()) {

							String[] response = new String[] { Integer.toString(bomManager.getSerialNo()), subGroup, ID, BOMLineID, (!errorMessage.isEmpty() ? (errorMessage + " mandatory value(s) are empty.") : ("")) + validateFlexError, "-", "Error" };
							bomManager.printReport("PRINT", response);
							bomManager.incrementSerialNo();
							isError = true;
							break;
						} else {

							if (duplicateBOMID.contains(BOMLineID.trim())) {

								errorMessage = "BOMLine ID";
								String[] response = new String[] { Integer.toString(bomManager.getSerialNo()), subGroup, ID, BOMLineID, errorMessage + " value(s) is duplicate.", "-", "Error" };
								bomManager.printReport("PRINT", response);
								bomManager.incrementSerialNo();
								isError = true;
								break;
							} else {

								duplicateBOMID.add(BOMLineID.trim());
							}
						}

						if (errorMessage.length() == 0) {
							validBOMLinesVector.add(childLine);
						}

					} else {
						String[] response = new String[] { Integer.toString(bomManager.getSerialNo()), subGroup, ID, BOMLineID, "Purchase level is NOT valid!", "-", "WARNING" };
						bomManager.printReport("PRINT", response);
					}

				}
			}
			if (validBOMLinesVector.isEmpty() == false && isError == false) {

				validBOMLines = new TCComponent[validBOMLinesVector.size()];

				for (int i = 0; i < validBOMLinesVector.size(); i++) {
					validBOMLines[i] = validBOMLinesVector.get(i);

				}

			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return validBOMLines;

	}

	public static NodeInfo[] findBOMLineInBOPScooter2(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine, String subGroup, int count, ArrayList<SuperScooterReport> reportList) {

		DataManagementService dmService = DataManagementService.getService(session);
		CoreService structService = CoreService.getService(session);

		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;

		boolean noLink = false;

		for (NodeInfo bopNode : bomNodeBop) {

			TCComponent original = bopNode.originalNode;
			TCComponent[] found = bopNode.foundNodes;

			try {

				if (found.length == 0) {
					TCComponentBOMLine BL = (TCComponentBOMLine) original;
					dmService.getProperties(new TCComponent[] { BL }, new String[] { "bl_item_item_id", "VF4_bomline_id" });

					String ID = BL.getProperty("bl_item_item_id");
					String BOMID = BL.getProperty("VF4_bomline_id");

					SuperScooterReport rp = new SuperScooterReport();
					rp.setNo(Integer.toString(count++));
					rp.setSubGroup(subGroup);
					rp.setRecord(ID);
					rp.setBomlineId(BOMID);
					rp.setMessage("BOM Linked to 0 BOP");
					rp.setAction("-");
					rp.setType(UpdateType.UPDATE_BODY_ERROR);
					reportList.add(rp);
					noLink = true;
				}
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (noLink == true) {
			return null;
		}

		return bomNodeBop;
	}

	public static TCComponent[] query(String queryName, LinkedHashMap<String, String> queryInput, TCSession clientSession) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(clientSession);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[queryInput.size()];
			qc.values = new String[queryInput.size()];
			int i = 0;
			for (Entry<String, String> pair : queryInput.entrySet()) {
				qc.entries[i] = pair.getKey();
				qc.values[i] = pair.getValue();
				i++;
			}
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			} else {
				System.out.println("NO Cost Object FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objects;
	}

	public static TCComponent[] quickSearch(TCSession clientSession, String[] entry, String[] value, String query) {

		SavedQueryService QRservices = SavedQueryService.getService(clientSession);
		try {

			FindSavedQueriesCriteriaInput findQuery = new FindSavedQueriesCriteriaInput();
			findQuery.queryNames = new String[] { query };// "Dataset Name"
			findQuery.queryDescs = new String[0];
			findQuery.queryType = 0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] { findQuery });
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput savedQuery = new SavedQueryInput();

			savedQuery.query = (TCComponentQuery) result[0];
			savedQuery.entries = entry;
			savedQuery.values = value;
			savedQuery.maxNumToReturn = 0;
			savedQuery.resultsType = 0;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(new SavedQueryInput[] { savedQuery });

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {

				return results[0].objects;
			}

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getWorkStation(String[] listProperties, TCComponentItemRevision operation) {
		String WS = "";
		for (String pro : listProperties) {
			try {
				WS = operation.getProperty(pro);
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (WS.length() < 13) {
				continue;
			} else {
				break;
			}
		}
		if (WS.length() < 13) {
			WS = "EmptyStation";
		}
		return WS;
	}

	public static String validateFlexPart(TCComponent childLine, DataManagementService dmCoreService) {
		try {
			String objectType = childLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);
			if (objectType.equals(PropertyDefines.TYPE_FLEXIBLE_PART)) {
				TCComponentBOMLine bomLine = (TCComponentBOMLine) childLine;
				TCComponentItemRevision revision;
				try {
					revision = bomLine.getItemRevision();
					dmCoreService.getProperties(new TCComponent[] { revision }, new String[] { PropertyDefines.REV_ORIGIN_PURCHASE_PART });
					String originPart;
					try {
						originPart = revision.getPropertyDisplayableValue(PropertyDefines.REV_ORIGIN_PURCHASE_PART);
						return String.format("VF Flexible Part is used for designing only. Please use Original Purchased Part %s, instead.", originPart);
					} catch (NotLoadedException e) {
						return "BOM contains flexible Part. Please use Original Purchased Part instead";
					}
				} catch (TCException e) {
					return "BOM contains flexible Part. Please use Original Purchased Part instead";
				}
			}
		} catch (NotLoadedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return "";
	}

	public static TCComponent getComponent() {
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponent tcComponent = (TCComponent) targetComponents[0];
		return tcComponent;
	}

	public static boolean isReleasedItem(TCComponentItemRevision rev) {
		String statusList;
		try {
			statusList = rev.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
			return statusList.length() > 0;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void updateNodeForSubtitutePart(TCComponentBOMLine line, HashMap<String, String> data) {
		try {
			if (!line.getProperty(PropertyDefines.BOM_PART_GROUPID).isEmpty()) {
				data.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, line.getProperty(PropertyDefines.BOM_PART_GROUPID));
				if (line.hasSubstitutes()) {
					data.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "1");// main part
				} else {
					data.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "0");// sub part
				}
			} else {
				data.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
				data.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static String[] getLOVValues(String lovName) {
		try {
			TCComponentListOfValues listOfValues = TCComponentListOfValuesType.findLOVByName(lovName);
			if (listOfValues != null) {
				if (listOfValues.getListOfValues() != null) {
					return listOfValues.getListOfValues().getLOVDisplayValues();
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentItemRevision getPreviousRevision(TCComponentItemRevision revision) {

		TCComponentItemRevision previousItemRevision = null;
		try {
			String rev = revision.getProperty(PropertyDefines.ITEM_REV_ID);
			int revID = Integer.parseInt(rev);
			if (revID > 1) {
				String search_rev = Integer.toString(revID - 1);
				if (revID <= 10 & revID > 0) {
					search_rev = "0" + search_rev;
				}
//				for (int i = search_rev.length(); i < rev.length(); i++) {
//					search_rev = "0" + search_rev;
//				}
				previousItemRevision = revision.findRevision(search_rev);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return previousItemRevision;
	}

	public static String hasOnlyColorParts(TCComponent[] materials, String shop) {
		String errorMsg = "";
		boolean isSimilarID = true;
		boolean isColorPart = false;
		boolean isTraceable = true;
		String basePartID = "";
		try {
			for (TCComponent bomlinePart : materials) {
				String materialType = bomlinePart.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);
				if(Arrays.asList(PropertyDefines.TYPES_VALID_MATERIALS).contains(materialType) == true) {

					String isColorPartID = bomlinePart.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
					if(isColorPartID.length() == 14 || isColorPartID.length() == 16) {
						isColorPart = true;
						String isTreacable = bomlinePart.getPropertyDisplayableValue(PropertyDefines.BOM_IS_TRACEABLE);
						if (isTreacable.equals("Yes") == false) {
							isTraceable = false;
						}
						String ID = isColorPartID.substring(0,11);
						if(basePartID.equals(ID) == false) {
							if(basePartID.equals("")) {
								basePartID = ID;
							}else {
								isSimilarID = false;
							}
						}
					}else {
						String isTreacable = bomlinePart.getPropertyDisplayableValue(PropertyDefines.BOM_IS_TRACEABLE);
						if (isTreacable.equals("Yes") == false) {
							isTraceable = false;
						}
						if(basePartID.equals(isColorPartID) == false) {
							if(basePartID.equals("")) {
								basePartID = isColorPartID;
							}else {
								isSimilarID = false;
							}
						}
					}
				}

			}

			boolean isExcludedShops = shop.compareToIgnoreCase("GA SHOP") == 0 || shop.compareToIgnoreCase("EScooter") == 0;// || shop.compareToIgnoreCase("PAINT SHOP") == 0 || shop.compareToIgnoreCase("BODY SHOP") == 0;
			if(isColorPart == true && isSimilarID == false && isExcludedShops == false) {
				errorMsg = "Materials are not related to same \"Base Part\". Please add only same \"Base Part\" ID Materials. ";
			}
			if(isColorPart == false && isSimilarID == false && isExcludedShops == false) {
				errorMsg = errorMsg+"Materials are not of same ID. Please add only same ID Materials. ";
			}
			if(isTraceable ==  false) {
				errorMsg = errorMsg+"\"Traceability\" operation required material with \"Part Traceable Indicator\" as \"Yes\".";
			}
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return errorMsg;
	}
}
