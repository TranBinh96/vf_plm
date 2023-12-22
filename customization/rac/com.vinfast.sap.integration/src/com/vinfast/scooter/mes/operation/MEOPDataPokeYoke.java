package com.vinfast.scooter.mes.operation;

import org.json.JSONArray;
import org.json.JSONObject;

public class MEOPDataPokeYoke extends MEOPDataAbstract {
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
		
		if (!this.getMaterialList().isEmpty()) {
			for (String mat : this.getMaterialList()) {
				JSONObject item = new JSONObject();
				item.put("MaterialDef_NId", mat.trim());
				materialConsumptionsArray.put(item);
			}
		}

		if (!this.getPredecessor().isEmpty()) {
			for (String nid : this.getPredecessor()) {
				JSONObject item = new JSONObject();
				item.put("NId", nid.trim());
				predecessorsArray.put(item);
			}
		}

		parentOperationPokeYoke = this.getReferenceComponent();
		operationType = "Traceability";
		
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
		dataString.append("<Operation ID=\"" + this.getMEOPID() + "\" Revision=\"" + this.getMEOPRevID() + "\" OperationType=\"" + "Traceability" + "\">");
		dataString.append("<Description>" + this.getMEOPName() + "</Description>");
		dataString.append("<DataGroup>" + this.getKeyData() + "</DataGroup>");
		dataString.append("<Workplaces>");
		if (this.getWorkStationID().length() > 0) {
			String[] WSArr = this.getWorkStationID().split(",");
			for (int i = 0; i < WSArr.length; i++) {
				dataString.append("<Workplace ID=\"" + WSArr[i].trim() + "\" />");
			}
		}
		dataString.append("</Workplaces>");
		dataString.append("<WorkplaceSequence>" + this.getWorkplaceSequence() + "</WorkplaceSequence>");
		dataString.append("<OperationAttribute>");
		dataString.append("<MaterialToVerify>");
		dataString.append("<Details>" + "" + "</Details>");
		dataString.append("<MaterialLists>");
		dataString.append("</MaterialLists>");
		dataString.append("</MaterialToVerify>");
		dataString.append("<WorkInstructions>");
		dataString.append("</WorkInstructions>");
		String pokeyoke = this.getReferenceComponent();

		if (pokeyoke.length() != 0) {

			dataString.append("<ParentOperationPokeYoke>");
			dataString.append(pokeyoke);
			dataString.append("</ParentOperationPokeYoke>");
		}

		dataString.append("</OperationAttribute>");
		dataString.append("<MaterialConsumptions>");
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
		if (getReferenceComponent().length() == 0) {
			return "PLM: POKO YOKE operation should have reference component";
		} else if (getMaterialList().size() > 0) {
			return "PLM: Remove materials from POKO YOKE operation";
		}
		return "";
	}
}
