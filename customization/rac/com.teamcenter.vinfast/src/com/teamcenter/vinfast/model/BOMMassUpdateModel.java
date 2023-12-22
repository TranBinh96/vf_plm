package com.teamcenter.vinfast.model;

import java.util.LinkedHashMap;

public class BOMMassUpdateModel {
	private int lineNo = 0;
	private String partNumber = "";
	private String posID = "";
	private LinkedHashMap<String, String> updateProperties = new LinkedHashMap<String, String>();
	private String validateMessage = "";
	private StringBuilder updateMessage = new StringBuilder();

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getPosID() {
		return posID;
	}

	public void setPosID(String posID) {
		this.posID = posID;
	}

	public LinkedHashMap<String, String> getUpdateProperties() {
		return updateProperties;
	}

	public void setUpdateProperties(LinkedHashMap<String, String> updateProperties) {
		this.updateProperties = updateProperties;
	}

	public String getValidateMessage() {
		return validateMessage;
	}

	public void setValidateMessage(String validateMessage) {
		this.validateMessage = validateMessage;
	}

	public String getUpdateMessage() {
		return updateMessage.toString();
	}

	public void setUpdateMessage(String updateMessage) {
		this.updateMessage.append(updateMessage);
	}

}
