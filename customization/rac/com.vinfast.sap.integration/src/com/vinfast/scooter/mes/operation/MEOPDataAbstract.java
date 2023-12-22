package com.vinfast.scooter.mes.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;

public abstract class MEOPDataAbstract {
	enum OperationType {
		InvalidType, PokeYoke, Traceability, PartVerification, Screwing, Filling, AutomaticConsumption, ECU, BuyOff, Machining,
	}

	public MEOPDataAbstract() {

	}
	
	String workStationID;
	String itemID;
	String revisionID;
	String operationType;
	String hwVersion;
	String swVersion;
	String hwPart;
	String swPart;
	String programID;
	String refComp;
	String scopeFlow;
	String keyData;
	String isMHU;
	String workplaceSequence;
	String agingTime;
	String isVcu;
	String equipment;
	String name;
	ArrayList<String> MaterialList = new ArrayList<String>();
	ArrayList<String> ToolList = new ArrayList<String>();
	ArrayList<String> predecessor = new ArrayList<String>();
	ArrayList<HashMap<String, String>> materialDetails = new ArrayList<HashMap<String, String>>();
	String xmlbuffer;
	TCComponent[] childItems;

	abstract void xmlAppendBody(StringBuffer str);

	abstract void jsonAppendBody(StringBuffer str);

	public abstract String isValidOperation();

	public TCComponent[] getChildItems() {
		return childItems;
	}

	public void setChildItems(TCComponent[] childItems) {
		this.childItems = childItems;
	}

	public String getAgingTime() {
		return agingTime;
	}

	public void setAgingTime(String agingTime) {
		this.agingTime = agingTime;
	}

	public String getWorkplaceSequence() {
		return workplaceSequence;
	}

	public void setWorkplaceSequence(String workplaceSequence) {
		this.workplaceSequence = workplaceSequence;
	}

	public String getHwVersion() {
		return hwVersion;
	}

	public String getIsVcu() {
		return isVcu;
	}

	public void setIsVcu(String isVcu) {
		this.isVcu = isVcu;
	}

	public void setHwVersion(String hwVersion) {
		this.hwVersion = hwVersion;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public void setSwVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public String getHwPartNumber() {
		return hwPart;
	}

	public void setHwPartNumber(String hwPart) {
		this.hwPart = hwPart;
	}

	public String getSwPartNumber() {
		return swPart;
	}

	public void setSwPartNumber(String swPart) {
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

	public String getKeyData() {
		return keyData;
	}

	public void setKeyData(String keyData) {
		this.keyData = keyData;
	}

	public String getIsMHU() {
		return isMHU;
	}

	public void setIsMHU(String isMHU) {
		this.isMHU = isMHU;
	}

	public String getEquipment() {
		return equipment;
	}

	public void setEquipment(String equipment) {
		this.equipment = equipment;
	}

	public String getMEOPName() {
		return name;
	}

	public void setMEOPName(String name) {
		this.name = name;
	}

	public ArrayList<String> getMaterialList() {
		return MaterialList;
	}

	public void setMaterialList(ArrayList<String> materialList) {
		MaterialList = materialList;
	}

	public ArrayList<String> getToolList() {
		return ToolList;
	}

	public void setToolList(ArrayList<String> toolList) {
		ToolList = toolList;
	}

	public ArrayList<String> getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(String predecessor) {
		this.predecessor.add(predecessor);
	}

//	public ArrayList<HashMap<String, String>> getMaterialDetails() {
//		return materialDetails;
//	}
//
//	public void setMaterialDetails(ArrayList<HashMap<String, String>> materialDetails) {
//		this.materialDetails = materialDetails;
//	}

	public String getXmlbuffer() {
		return xmlbuffer;
	}

	public void setXmlbuffer(String xmlbuffer) {
		this.xmlbuffer = xmlbuffer;
	}

	public String getWorkStationID() {
		return workStationID;
	}

	public void setWorkStationID(String workStationID) {
		this.workStationID = workStationID;
	}

	public String getMEOPID() {
		return itemID;
	}

	public void setMEOPID(String itemID) {
		this.itemID = itemID;
	}

	public String getMEOPRevID() {
		return revisionID;
	}

	public void setMEOPRevID(String revisionID) {
		this.revisionID = revisionID;
	}

	public String getMEOPType() {
		return operationType;
	}

	public void setMEOPType(String operationType) {
		this.operationType = operationType;
	}

	public void xmlAppendHeader(StringBuffer dataString) {
		dataString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		dataString.append("<OperationsList xmlns=\"VINFAST.Manufacturing.Engineering\">");
	}

	public void xmlAppendFooter(StringBuffer dataString) {
		dataString.append("</OperationsList>");
	}
}
