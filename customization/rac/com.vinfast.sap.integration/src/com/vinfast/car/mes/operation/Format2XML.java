package com.vinfast.car.mes.operation;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vinfast.sap.bom.MEOPData;
import com.vinfast.sap.util.PropertyDefines;

public class Format2XML {
	public static String formatOperation(MEOPData dataMap) {
		StringBuffer dataString = new StringBuffer();
		dataString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		dataString.append("<OperationsList xmlns=\"VINFAST.Manufacturing.Engineering\">");

		if (dataMap.getMEOPType().equals("Poke Yoke")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + "Traceability" + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<DataGroup>" + dataMap.getKeyData() + "</DataGroup>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<MaterialToVerify>");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("<MaterialLists>");
			dataString.append("</MaterialLists>");
			dataString.append("</MaterialToVerify>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");

			String pokeyoke = dataMap.getReferenceComponent();
			if (pokeyoke.length() != 0) {
				dataString.append("<ParentOperationPokeYoke>");
				dataString.append(pokeyoke);
				dataString.append("</ParentOperationPokeYoke>");
			}

			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");
			dataString.append("</MaterialConsumptions>");

			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}

			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Traceability")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + dataMap.getMEOPType() + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<DataGroup>" + dataMap.getKeyData() + "</DataGroup>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<MaterialToVerify>");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("<MaterialLists>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}
			dataString.append("<IsMHU>" + dataMap.getMHU() + "</IsMHU>");
			dataString.append("<IsVCU>" + dataMap.getIsVcu() + "</IsVCU>");
			dataString.append("</MaterialLists>");
			dataString.append("</MaterialToVerify>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");

			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}

			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Group Traceability")) {
			ArrayList<String> MaterialData = dataMap.getMaterialList();
			String material = MaterialData.get(0);
			String existTime = Integer.toString(MaterialData.size());
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + dataMap.getMEOPType().replace(" ", "") + "\" Quantity=\"" + existTime + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<DataGroup>" + dataMap.getKeyData() + "</DataGroup>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<MaterialToVerify>");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("<MaterialLists>");
			dataString.append("<Material ID=\"" + material + "\" Quantity=\"" + existTime + "\" />");
			dataString.append("</MaterialLists>");
			dataString.append("</MaterialToVerify>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");
			dataString.append("<Material ID=\"" + material + "\" Quantity=\"" + existTime + "\" />");
			dataString.append("</MaterialConsumptions>");

			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}

			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Part Verification")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + "PartVerification" + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<MaterialToVerify>");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("<MaterialLists>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialLists>");
			dataString.append("</MaterialToVerify>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Screwing")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + dataMap.getMEOPType() + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<ToolOperation>");
			dataString.append("<Details>" + "" + "</Details>");

			ArrayList<String> toolData = dataMap.getToolList();
			if (toolData.isEmpty() == false) {
				for (int i = 0; i < toolData.size(); i++) {
					dataString.append("<Tool ID=\"" + toolData.get(i).trim() + "\" ProgramNumberID=\"" + dataMap.getProgramID().trim() + "\"/>");
				}
			}

			dataString.append("</ToolOperation>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Filling")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + dataMap.getMEOPType() + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<ToolOperation>");
			dataString.append("<Details>" + "" + "</Details>");

			ArrayList<String> toolData = dataMap.getToolList();
			if (toolData.isEmpty() == false) {
				for (int i = 0; i < toolData.size(); i++) {
					dataString.append("<Tool ID=\"" + toolData.get(i).trim() + "\" ProgramNumberID=\"" + "01" + "\"/>");
				}
			}

			dataString.append("</ToolOperation>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Automatic Consumption")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + "AutomaticConsumed" + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("ECU")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + dataMap.getMEOPType() + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<ECU HWNumber=\"" + dataMap.getHWPartNumber() + "\" HWVersion=\"" + dataMap.getHWVersion() + "\" SWNumber=\"" + dataMap.getSWPartNumber() + "\" SWVersion=\"" + dataMap.getSWVersion() + "\">");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("</ECU>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		} else if (dataMap.getMEOPType().equals("Buy-Off")) {
			dataString.append("<Operation ID=\"" + dataMap.getMEOPID() + "\" Revision=\"" + dataMap.getMEOPRevID() + "\" OperationType=\"" + "BuyOff" + "\">");
			dataString.append("<Description>" + dataMap.getMEOPName() + "</Description>");
			dataString.append("<Workplaces>");
			if (dataMap.getWorkStationID().length() > 0) {
				String[] WSArr = dataMap.getWorkStationID().split(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER);
				for (int i = 0; i < WSArr.length; i++) {
					dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
				}
			}
			dataString.append("</Workplaces>");
			dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
			dataString.append("<OperationAttribute>");
			dataString.append("<BuyOff>");
			dataString.append("<Details>" + "" + "</Details>");
			dataString.append("</BuyOff>");
			dataString.append("<WorkInstructions>");
			dataString.append("</WorkInstructions>");
			dataString.append("<AgingTime>" + dataMap.getAgingTime() + "</AgingTime>");
			dataString.append("</OperationAttribute>");
			dataString.append("<MaterialConsumptions>");

			ArrayList<String> MaterialData = dataMap.getMaterialList();
			if (MaterialData.isEmpty() == false) {
				for (int i = 0; i < MaterialData.size(); i++) {
					dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
				}
			}

			dataString.append("</MaterialConsumptions>");
			if (dataMap.getPredecessor().isEmpty() == false) {
				dataString.append("<Predecessors>");
				for (String nid : dataMap.getPredecessor()) {
					dataString.append("<Operation NId=\"" + nid.trim() + "\" />");
				}

				dataString.append("</Predecessors>");
			}
			dataString.append("</Operation>");
		}
		dataString.append("<WorkplaceSequence>" + dataMap.getWorkplaceSequence() + "</WorkplaceSequence>");
		dataString.append("</OperationsList>");

		return dataString.toString();
	}

	public static String formatOperationJson(MEOPData dataMap) {
		JSONArray workpalaceArray = new JSONArray();
		if (dataMap.getWorkStationID().length() > 0) {
			String[] WSArr = dataMap.getWorkStationID().split(",");
			for (String ws : WSArr) {
				JSONObject workpalaceItem = new JSONObject();
				workpalaceItem.put("NId", ws.trim());
				workpalaceArray.put(workpalaceItem);
			}
		}

		JSONArray materialConsumptionsArray = new JSONArray();

		JSONArray specificationsArray = new JSONArray();

		JSONArray predecessorsArray = new JSONArray();

		JSONArray addAttributesArray = new JSONArray();

		JSONArray operationPlatformArray = new JSONArray();

		double quantity = 0.0;

		String parentOperationPokeYoke = "";

		String operationType = "";
		
		if (dataMap.getMEOPType().equals("Poke Yoke")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			parentOperationPokeYoke = dataMap.getReferenceComponent();
			operationType = "Traceability";
		} else if (dataMap.getMEOPType().equals("Traceability")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}
			operationType = "Traceability";
		} else if (dataMap.getMEOPType().equals("Group Traceability")) {
			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			quantity = dataMap.getMaterialList().size();
			operationType = "GroupTraceability";
		} else if (dataMap.getMEOPType().equals("Part Verification")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}
			operationType = "PartVerification";
		} else if (dataMap.getMEOPType().equals("Screwing")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			if (!dataMap.getToolList().isEmpty()) {
				for (String tool : dataMap.getToolList()) {
					JSONObject item = new JSONObject();
					item.put("ID", tool.trim());
					item.put("Val", dataMap.getProgramID().trim());
					item.put("Type", "");
					item.put("File", "");
					item.put("Program_Date", "");
					item.put("Version", "");
					item.put("DID", "");
					item.put("Rev", "");
					// item.put("Flash", false);
					item.put("RSWin", "");
					addAttributesArray.put(item);
				}
			}
			operationType = "Screwing";
		} else if (dataMap.getMEOPType().equals("Filling")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			if (!dataMap.getToolList().isEmpty()) {
				for (String tool : dataMap.getToolList()) {
					JSONObject item = new JSONObject();
					item.put("ID", tool.trim());
					item.put("Val", dataMap.getProgramID().trim());
					item.put("Type", "");
					item.put("File", "");
					item.put("Program_Date", "");
					item.put("Version", "");
					item.put("DID", "");
					item.put("Rev", "");
					// item.put("Flash", false);
					item.put("RSWin", "");
					addAttributesArray.put(item);
				}
			}
			operationType = "Filling";
		} else if (dataMap.getMEOPType().equals("Automatic Consumption")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			if (!dataMap.getToolList().isEmpty()) {
				for (String tool : dataMap.getToolList()) {
					JSONObject item = new JSONObject();
					item.put("ID", tool.trim());
					item.put("Val", dataMap.getProgramID().trim());
					item.put("Type", "");
					item.put("File", "");
					item.put("Program_Date", "");
					item.put("Version", "");
					item.put("DID", "");
					item.put("Rev", "");
					// item.put("Flash", false);
					item.put("RSWin", "");
					addAttributesArray.put(item);
				}
			}
			operationType = "AutomaticConsumed";
		} else if (dataMap.getMEOPType().equals("ECU")) {
			if (!dataMap.getMaterialList().isEmpty()) {
				for (String mat : dataMap.getMaterialList()) {
					JSONObject item = new JSONObject();
					item.put("MaterialDef_NId", mat.trim());
					materialConsumptionsArray.put(item);
				}
			}

			if (!dataMap.getPredecessor().isEmpty()) {
				for (String nid : dataMap.getPredecessor()) {
					JSONObject item = new JSONObject();
					item.put("NId", nid.trim());
					predecessorsArray.put(item);
				}
			}

			if (!dataMap.getMaterialDetails().isEmpty()) {
				for (HashMap<String, String> material : dataMap.getMaterialDetails()) {
					JSONObject item = new JSONObject();
					item.put("ID", material.get("MATERIALID").trim());
					item.put("Val", "");
					item.put("Type", material.get(PropertyDefines.REV_TYPE).trim());
					item.put("File", material.get(PropertyDefines.REV_FILE).trim());
					item.put("Program_Date", material.get(PropertyDefines.REV_PROGRAM_DATE).trim());
					item.put("Version", material.get("MATERIALREV").trim());
					item.put("DID", material.get(PropertyDefines.REV_DID).trim());
					item.put("Rev", material.get(PropertyDefines.BOM_VERSION).trim());
//					item.put("Flash", material.get(PropertyDefines.REV_FLASH).trim());
					item.put("RSWin", material.get(PropertyDefines.REV_RXSWIN).trim());
					addAttributesArray.put(item);
				}
			}
			operationType = "ECU";
		} else if (dataMap.getMEOPType().equals("Buy-Off")) {
			operationType = "BuyOff";
		} else if (dataMap.getMEOPType().equals("Machining")) {
			operationType = "Machining";
		}

		JSONObject item = new JSONObject();
		item.put("NId", dataMap.getMEOPID());
		item.put("Name", dataMap.getMEOPID());
		item.put("Revision", dataMap.getMEOPRevID());
		item.put("Workplace", workpalaceArray);
		item.put("Description", dataMap.getMEOPName());
		item.put("OperationDefType", operationType);
		item.put("Specifications", specificationsArray);
		item.put("MaterialConsumptions", materialConsumptionsArray);
		item.put("AddtionalAttributes", addAttributesArray);
		item.put("DataGroup", dataMap.getKeyData());
		item.put("Predecessors", predecessorsArray);
		item.put("ParentOperationPokeYoke", parentOperationPokeYoke);
		item.put("OperationPlatform", operationPlatformArray);
		item.put("Quantity", quantity);
		item.put("ECUBOPReference", "");
//		item.put("IsMHU", dataMap.getMHU());
//		item.put("IsVCU", dataMap.getIsVcu());
		item.put("AgingTime", 0);
		if (dataMap.getWorkplaceSequence().contentEquals("0") == false) item.put("WorkplaceSequence", dataMap.getWorkplaceSequence());
		return "{\"command\":{\"OperationsList\":[" + item.toString() + "]}}";
	}
}
