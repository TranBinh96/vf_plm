package com.vinfast.sap.bom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class MEOPData {
	private TCComponentItemRevision operationRevObject = null;
	private TCComponentBOMLine operationBomlineObject = null;
	private String shop = "";
	private String itemID = "";
	private String revID = "";
	private String name = "";
	private String type = "";
	private String hwVersion = "";
	private String swVersion = "";
	private String hwPart = "";
	private String swPart = "";
	private String programID = "";
	private String refComp = "";
	private String scopeFlow = "";
	private String keyData = "";
	private String isMHU = "";
	private String isVcu = "";
	private String workStationID = "";
	private String workplaceSequence = "";
	private String agingTime = "";
	//
	private String jsonData = "";
	private String xmlData = "";
	private String message = "";
	private String transferMessage = "";
	private boolean isCheckAlreadyTransfer = false;
	private boolean noNeedTransfer = false;
	private boolean hasMultipleStations = false;
	//
	private ArrayList<String> materialList;
	private ArrayList<HashMap<String, String>> materialDetails = new ArrayList<HashMap<String, String>>();
	private ArrayList<String> toolList;
	private ArrayList<String> predecessorList = new ArrayList<String>();

	public MEOPData() {

	}

	public MEOPData(boolean isCheckAlreadyTransfer) {
		this.isCheckAlreadyTransfer = isCheckAlreadyTransfer;
	}

	public TCComponentItemRevision getOperationRevObject() {
		return operationRevObject;
	}

	public void setOperationRevObject(TCComponentItemRevision operationRevObject) {
		this.operationRevObject = operationRevObject;
		if (operationRevObject != null) {
			try {
				itemID = operationRevObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				revID = operationRevObject.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
				if (isCheckAlreadyTransfer && !operationRevObject.getPropertyDisplayableValue(PropertyDefines.REV_TO_MES).isEmpty()) {
					message = "Already transfer to MES";
					noNeedTransfer = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public TCComponentBOMLine getOperationBomlineObject() {
		return operationBomlineObject;
	}

	public void setOperationBomlineObject(TCComponentBOMLine operationBomlineObject) {
		this.operationBomlineObject = operationBomlineObject;
		if (operationBomlineObject != null) {
			try {
				operationRevObject = operationBomlineObject.getItemRevision();
				itemID = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
				revID = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
				name = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_NAME);
				type = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
				hwVersion = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_HW_VERSION);
				swVersion = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_SW_VERSION);
				hwPart = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_HW_NUMBER);
				swPart = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_SW_NUMBER);
				keyData = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_KEYDATA);
				refComp = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_REF_COMP);
				programID = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_PROGRAM_ID);
				workStationID = UIGetValuesUtility.getWorkStationID(operationBomlineObject, PropertyDefines.BOM_NAME);
				if (workStationID.length() > 13 && workStationID.contains(PropertyDefines.MULTIPLE_WORKPLACE_DELIMETER)) {
					hasMultipleStations = true;
				}
				isMHU = operationRevObject.getProperty(PropertyDefines.REV_ISMHU);
				isVcu = operationRevObject.getProperty(PropertyDefines.REV_ISVCU);
				workplaceSequence = operationRevObject.getProperty(PropertyDefines.REV_WORKPLACE_SEQ);
				agingTime = operationRevObject.getProperty(PropertyDefines.REV_AGINGTIME);
				String predecessorValue = operationBomlineObject.getProperty(PropertyDefines.BOM_PREDECESSOR_ID);
				if (!predecessorValue.isEmpty()) {
					String delimeter = predecessorValue.contains(",") ? "," : ";";
					if (predecessorValue.contains(delimeter)) {
						for (String predecessor : predecessorValue.split(delimeter)) {
							if (!predecessor.isEmpty())
								predecessorList.add(predecessor);
						}
					} else {
						predecessorList.add(predecessorValue);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isNoNeedTransfer() {
		return noNeedTransfer;
	}

	public void setNoNeedTransfer(boolean noNeedTransfer) {
		this.noNeedTransfer = noNeedTransfer;
	}

	public boolean isValidate() {
		return message.isEmpty();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransferMessage() {
		return transferMessage;
	}

	public void setTransferMessage(String transferMessage) {
		this.transferMessage = transferMessage;
	}

	public String getAgingTime() {
		return agingTime;
	}

	public void setAgingTime(String agingTime) {
		this.agingTime = agingTime;
	}

	public String getIsVcu() {
		return isVcu;
	}

	public void setIsVcu(String isVcu) {
		this.isVcu = isVcu;
	}

	public String getWorkplaceSequence() {
		return workplaceSequence;
	}

	public void setWorkplaceSequence(String workplaceSequence) {
		this.workplaceSequence = workplaceSequence;
	}

	public void setMHU(String mhuvalue) {
		this.isMHU = mhuvalue;
	}

	public String getMHU() {
		return isMHU;
	}

	public ArrayList<HashMap<String, String>> getMaterialDetails() {
		return materialDetails;
	}

	public void setMaterialDetails(HashMap<String, String> materials) {
		this.materialDetails.add(materials);
	}

	public ArrayList<String> getPredecessor() {
		return predecessorList;
	}

	public void setPredecessor(String predecessor) {
		this.predecessorList.add(predecessor);
	}

	public ArrayList<String> getMaterialList() {
		return materialList;
	}

	public void setMaterialList(ArrayList<String> materialList) {
		this.materialList = materialList;
	}

	public ArrayList<String> getToolList() {
		return toolList;
	}

	public void setToolList(ArrayList<String> toolList) {
		this.toolList = toolList;
	}

	public String getWorkStationID() {
		return workStationID;
	}

	public void setWorkStationID(String workStationID) {
		this.workStationID = workStationID;
	}

	public String getKeyData() {
		return keyData;
	}

	public void setKeyData(String keyData) {
		this.keyData = keyData;
	}

	public String getMEOPID() {
		return itemID;
	}

	public void setMEOPID(String itemID) {
		this.itemID = itemID;
	}

	public String getMEOPRevID() {
		return revID;
	}

	public void setMEOPRevID(String revID) {
		this.revID = revID;
	}

	public String getMEOPName() {
		return name;
	}

	public void setMEOPName(String name) {
		this.name = name;
	}

	public String getMEOPType() {
		return type;
	}

	public void setMEOPType(String type) {
		this.type = type;
	}

	public String getHWVersion() {
		return hwVersion;
	}

	public void setHWVersion(String hwVersion) {
		this.hwVersion = hwVersion;
	}

	public String getSWVersion() {
		return swVersion;
	}

	public void setSWVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public String getHWPartNumber() {
		return hwPart;
	}

	public void setHWPartNumber(String hwPart) {
		this.hwPart = hwPart;
	}

	public String getSWPartNumber() {
		return swPart;
	}

	public void setSWPartNumber(String swPart) {
		this.swPart = swPart;
	}

	public String getProgramID() {
		return programID;
	}

	public void setProgramID(String programID) {
		this.programID = programID;
	}

	public String getReferenceComponent() {
		return refComp;
	}

	public void setReferenceComponent(String refComp) {
		this.refComp = refComp;
	}

	public String getScopeFlow() {
		return scopeFlow;
	}

	public void setScopeFlow(String scopeFlow) {
		this.scopeFlow = scopeFlow;
	}

	public boolean hasMultipleStations() {
		return hasMultipleStations;
	}

	public String getShop() {
		return shop;
	}

	public void setShop(String shop) {
		this.shop = shop;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}
}
