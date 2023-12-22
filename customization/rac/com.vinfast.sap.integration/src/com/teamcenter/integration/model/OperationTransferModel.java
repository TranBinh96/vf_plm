package com.teamcenter.integration.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.scooter.mes.operation.MEOPDataAbstract;
import com.vinfast.scooter.mes.operation.MEOPDataFactory;

public class OperationTransferModel {
	private TCComponentItemRevision operationRevObject = null;
	private TCComponentBOMLine operationBomlineObject = null;
	private String itemID = "";
	private String revID = "";
	private String opeType = "";
	private boolean isValid = true;
	private String jsonData = "";
	private String xmlData = "";
	private String message = "";
	private String transferMessage = "";
	private boolean isCheckAlreadyTransfer = false;
	private boolean noNeedTransfer = false;
	private MEOPDataAbstract operationMap = null;

	public OperationTransferModel() {

	}

	public OperationTransferModel(boolean isCheckAlreadyTransfer) {
		this.isCheckAlreadyTransfer = isCheckAlreadyTransfer;
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
					isValid = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public TCComponentBOMLine getOperationBomlineObject() {
		return operationBomlineObject;
	}

	public void setOperationBomlineObject(TCComponentBOMLine operationBomlineObject, HashMap<String, ArrayList<TCComponent>> duplicateMEOP) {
		this.operationBomlineObject = operationBomlineObject;
		if (operationBomlineObject != null) {
			try {
				MEOPDataFactory.getInstance();
				opeType = operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
				operationMap = MEOPDataFactory.getMEOPData(opeType);
				operationMap.setMEOPID(itemID);
				operationMap.setMEOPRevID(revID);
				operationMap.setMEOPName(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_NAME));
				operationMap.setMEOPType(opeType);
				operationMap.setHwVersion(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_HW_VERSION));
				operationMap.setSwVersion(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_SW_VERSION));
				operationMap.setHwPartNumber(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_HW_NUMBER));
				operationMap.setSwPartNumber(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_SW_NUMBER));
				operationMap.setKeyData(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_KEYDATA));
				operationMap.setReferenceComponent(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID));
				operationMap.setProgramID(operationBomlineObject.getPropertyDisplayableValue(PropertyDefines.BOM_PROGRAM_ID));
				ArrayList<TCComponent> meopList = duplicateMEOP.get(itemID);
				String workstationID = "";

				for (TCComponent meop : meopList) {
					TCComponentBOMLine meopLine = (TCComponentBOMLine) meop;
					if (workstationID.isEmpty())
						workstationID = UIGetValuesUtility.getWorkStationID(meopLine, PropertyDefines.BOM_NAME);
					else
						workstationID = workstationID + "," + UIGetValuesUtility.getWorkStationID(meopLine, PropertyDefines.BOM_NAME);
				}
				operationMap.setWorkStationID(workstationID);
				operationMap.setIsMHU(operationRevObject.getProperty(PropertyDefines.REV_ISMHU));
				operationMap.setIsVcu(operationRevObject.getProperty(PropertyDefines.REV_ISVCU));
				operationMap.setWorkplaceSequence(operationRevObject.getProperty(PropertyDefines.REV_WORKPLACE_SEQ));
				operationMap.setAgingTime(operationRevObject.getProperty(PropertyDefines.REV_AGINGTIME));

				String predecessorValue = operationBomlineObject.getProperty(PropertyDefines.BOM_PREDECESSOR_ID);
				if (!predecessorValue.isEmpty()) {
					String delimeter = predecessorValue.contains(",") ? "," : ";";
					if (predecessorValue.contains(delimeter)) {
						for (String predecessor : predecessorValue.split(delimeter)) {
							if (!predecessor.isEmpty())
								operationMap.setPredecessor(predecessor);
						}
					} else {
						operationMap.setPredecessor(predecessorValue);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setNoNeedTransfer(boolean noNeedTransfer) {
		this.noNeedTransfer = noNeedTransfer;
	}

	public boolean isNoNeedTransfer() {
		return noNeedTransfer;
	}

	public boolean isValidate() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getItemID() {
		return itemID;
	}

	public String getRevID() {
		return revID;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public MEOPDataAbstract getOperationMap() {
		return operationMap;
	}

	public TCComponentItemRevision getOperationRevObject() {
		return operationRevObject;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public String getTransferMessage() {
		return transferMessage;
	}

	public void setTransferMessage(String transferMessage) {
		this.transferMessage = transferMessage;
	}

	public String getOpeType() {
		return opeType;
	}
}
