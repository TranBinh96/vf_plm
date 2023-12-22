package com.vinfast.scooter.mes.operation;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class MEOPDataMachining extends MEOPDataAbstract {
	@Override
	void jsonAppendBody(StringBuffer dataString) {
		JSONArray workpalaceArray = new JSONArray();
		if (this.getWorkStationID().length() > 0) {
			String[] WSArr = this.getWorkStationID().split(",");
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

		operationType = "Machining";

		JSONObject item = new JSONObject();
		item.put("NId", this.getMEOPID());
		item.put("Name", this.getMEOPID());
		item.put("Revision", this.getMEOPRevID());
		item.put("Workplace", workpalaceArray);
		item.put("Description", this.getMEOPName());
		item.put("OperationDefType", operationType);
		item.put("Specifications", specificationsArray);
		item.put("MaterialConsumptions", materialConsumptionsArray);
		item.put("AddtionalAttributes", addAttributesArray);
		item.put("DataGroup", this.getKeyData());
		item.put("Predecessors", predecessorsArray);
		item.put("ParentOperationPokeYoke", parentOperationPokeYoke);
		item.put("OperationPlatform", operationPlatformArray);
		item.put("Quantity", quantity);
		item.put("ECUBOPReference", "");
		item.put("AgingTime", 0);
		if (this.getWorkplaceSequence().contentEquals("0") == false) item.put("WorkplaceSequence", this.getWorkplaceSequence());
		dataString.append(item.toString());
	}

	@Override
	void xmlAppendBody(StringBuffer dataString) {
		dataString.append("<Operation ID=\"" + this.getMEOPID() + "\" Revision=\"" + this.getMEOPRevID() + "\" OperationType=\"" + "Machining" + "\">");
		dataString.append("<Description>" + this.getMEOPName() + "</Description>");
		dataString.append("<Workplaces>");
		if (this.getWorkStationID().length() > 0) {
			String[] WSArr = this.getWorkStationID().split(",");
			for (int i = 0; i < WSArr.length; i++) {
				dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
			}
		}
		dataString.append("</Workplaces>");
		dataString.append("<WorkplaceSequence>" + this.getWorkplaceSequence() + "</WorkplaceSequence>");
		dataString.append("<MaterialConsumptions>");

		ArrayList<String> MaterialData = this.getMaterialList();
		if (MaterialData.isEmpty() == false) {
			for (int i = 0; i < MaterialData.size(); i++) {
				dataString.append("<Material ID=\"" + MaterialData.get(i).trim() + "\" />");
			}
		}

		dataString.append("</MaterialConsumptions>");
		if (this.getPredecessor().isEmpty() == false) {
			dataString.append("<Predecessors>");
			for (String nid : this.getPredecessor()) {
				dataString.append("<Operation Nid=\"" + nid.trim() + "\" />");
			}

			dataString.append("</Predecessors>");
		}
		dataString.append("</Operation>");
	}

	@Override
	public String isValidOperation() {
		return "";
	}
}
