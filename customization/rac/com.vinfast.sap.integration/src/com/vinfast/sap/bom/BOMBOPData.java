package com.vinfast.sap.bom;

import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class BOMBOPData implements Cloneable {

	String partNumber;
	String platform;
	String modelyear;
	String plant;
	String maingroup;
	String subgroup;
	String groupdescription;
	String bomlineid;
	String quantity;
	String option;
	String dcr;
	String mcn;
	String sequence;
	String action;
	String workstation;
	String stationID;
	String linesupplymethod;
	String bopid;
	String mesbopindicator;
	String family_addr;
	String l_r_hand;
	String revision;
	String parent;
	String child;
	String topLevelPart;
	String headerPart;
	String phantom;
	String vfDesignator;
	
	ActionType actionType;
	boolean isSubGroupReleased;
	boolean isWorkStationReleased;
	boolean isOperationReleased;
	boolean isOperationNoPart = false;

	public boolean isOperationNoPart() {
		return isOperationNoPart;
	}

	public void setOperationNoPart(boolean isOperationNoPart) {
		this.isOperationNoPart = isOperationNoPart;
	}

	TCComponent MBOMLine;
	TCComponent[] MBOPLine;

	SAPURL SAPConnect = new SAPURL();

	public enum ActionType {
		ADD_PART_IN_BOM, ADD_PART_IN_BOP, DELETE_PART_IN_BOM, DELETE_PART_IN_BOP, ADD_OPERATION_NO_PART, DELETE_OPERATION_NO_PART, CHANGE_PART_IN_BOM, CHANGE_OPERATION_REVISION,
	}

	public enum FileType {
		IN_PUT, OUT_PUT,
	}

	public BOMBOPData() {
		super();
	}

	@Override
	public BOMBOPData clone() throws CloneNotSupportedException {
		return (BOMBOPData) super.clone();
	}

	public HashMap<String, String> getSUPBOM(String action) {

		HashMap<String, String> SUPBOM = new HashMap<String, String>();
		SUPBOM.put("MAINGROUP", this.getMainGroup());
		SUPBOM.put("SUBGROUP", this.getSubGroup());
		SUPBOM.put("PARTNO", this.getPartNumber());
		SUPBOM.put("BOMLINEID", this.getBOMLineID());
		SUPBOM.put("PLANT", this.getPlantCode());
		SUPBOM.put("QUANTITY", this.getQuanity());
		SUPBOM.put("OPTION", this.getFormula());
		SUPBOM.put("GROUPDESCRIPTION", this.getDescription());
		SUPBOM.put("DCR", this.getDCR());
		SUPBOM.put("PLATFORM", this.getPlatform());
		SUPBOM.put("ACTION", action);
		SUPBOM.put("MODELYEAR", this.getModelYear());
		SUPBOM.put("SEQUENCE", this.getSequenceTime());
		SUPBOM.put("MCN", this.getMCN());
		this.action = action;
		return SUPBOM;
	}

	public HashMap<String, String> getSUPBOP(String action) {

		HashMap<String, String> SUPBOP = new HashMap<String, String>();
		SUPBOP.put("MAINGROUP", this.getMainGroup());
		SUPBOP.put("BOMLINEID", this.getBOMLineID());
		SUPBOP.put("SAPPLANT", this.getPlantCode());
		SUPBOP.put("BOPID", this.getBOPID());
		SUPBOP.put("OPTION", "");
		SUPBOP.put("GROUPDESCRIPTION", this.getDescription());
		SUPBOP.put("SUBGROUP", this.getSubGroup());
		SUPBOP.put("MESBOPINDICATOR", this.getMESIndicator());
		SUPBOP.put("PLATFORM", this.getPlatform());
		SUPBOP.put("ACTION", action);
		SUPBOP.put("MODELYEAR", this.getModelYear());
		SUPBOP.put("LINESUPPLYMETHOD", this.getLineSupplyMethod());
		SUPBOP.put("SEQUENCE", this.getSequenceTime());
		SUPBOP.put("WORKSTATION", this.getWorkStation());
		SUPBOP.put("FAMILY_ADDR", this.getFamilyAddress());
		SUPBOP.put("L_R_HAND", this.getLeftRightHand());
		SUPBOP.put("MCN", this.getMCN());
		SUPBOP.put("REVISION", this.getBOPRevision());
		this.action = action;
		return SUPBOP;
	}

	public HashMap<String, String> getASSYBOM(String action) {

		HashMap<String, String> ASSYBOM = new HashMap<String, String>();

		ASSYBOM.put("PLANTCODE", this.getPlantCode());
		ASSYBOM.put("PARENTPART", this.getParentPart());
		ASSYBOM.put("BOMLINEID", this.getBOMLineID());
		ASSYBOM.put("CHILDPART", this.getChildPart());
		ASSYBOM.put("MCN", this.getMCN());
		ASSYBOM.put("LINE", this.getVfDesignator());
		ASSYBOM.put("QUANTITY", this.getQuanity());
		ASSYBOM.put("SEQUENCE", this.getSequenceTime());
		ASSYBOM.put("ACTION", action);
		ASSYBOM.put(PropertyDefines.INF_VF_ASSY_XML_GROUP, "");
		ASSYBOM.put(PropertyDefines.INF_VF_ASSY_XML_PRIORITY, "");

		return ASSYBOM;
	}

	public HashMap<String, String> getASSYBOP(String action) {

		HashMap<String, String> ASSYBOP = new HashMap<String, String>();
		ASSYBOP.put("SAPPLANT", this.getPlantCode());
		ASSYBOP.put("TOPLEVELPART", this.getTopLevelPart());
		ASSYBOP.put("HEADERPART", this.getHeaderPart());
		ASSYBOP.put("BOMLINEID", this.getBOMLineID());
		ASSYBOP.put("WORKSTATION", this.getWorkStation());
		ASSYBOP.put("LINESUPPLYMETHOD", this.getLineSupplyMethod());
		ASSYBOP.put("BOPID", this.getBOPID());
		ASSYBOP.put("MESBOPINDICATOR", this.getMESIndicator());
		ASSYBOP.put("SEQUENCE", this.getSequenceTime());
		ASSYBOP.put("ACTION", action);
		ASSYBOP.put("REVISION", this.getBOPRevision());
		return ASSYBOP;
	}

	public String getPhantom() {
		return phantom;
	}

	public void setPhantom(String phantom) {
		this.phantom = phantom;
	}

	public String getParentPart() {
		return parent;
	}

	public void setParentPart(String parent) {
		this.parent = parent;
	}

	public String getChildPart() {
		return child;
	}

	public void setChildPart(String child) {
		this.child = child;
	}

	public String getTopLevelPart() {
		return topLevelPart;
	}

	public void setTopLevelPart(String topLevelPart) {
		this.topLevelPart = topLevelPart;
	}

	public String getHeaderPart() {
		return headerPart;
	}

	public void setHeaderPart(String headerPart) {
		this.headerPart = headerPart;
	}

	public void setPartNumber(String partNo) {

		this.partNumber = partNo;
	}

	public void setPlatform(String model) {

		this.platform = model;
	}

	public void setModelYear(String year) {

		this.modelyear = year;
	}

	public void setPlantCode(String code) {

		this.plant = code;
	}

	public void setMainGroup(String main) {

		this.maingroup = main;
	}

	public void setSubGroup(String sub) {

		this.subgroup = sub;
	}

	public void setDescription(String description) {

		this.groupdescription = description;
	}

	public void setBOMLineID(String bomline) {

		this.bomlineid = bomline;
	}

	public void setQuanity(String qty) {

		this.quantity = qty;
	}

	public void setFormula(String formula) {

		this.option = formula;
	}

	public void setDCR(String dcr) {

		this.dcr = dcr;
	}

	public void setMCN(String mcn) {

		this.mcn = mcn;
	}

	public void setSequenceTime(String time) {

		this.sequence = time;
	}

	public void setAction(String action) {

		this.action = action;
	}

	public void setWorkStation(String station) {

		this.workstation = station;
	}

	public void setLineSupplyMethod(String lsm) {

		this.linesupplymethod = lsm;
	}

	public void setBOPID(String operationID) {

		this.bopid = operationID;
	}

	public void setBOPRevision(String operationRev) {

		this.revision = operationRev;
	}

	public void setMESIndicator(String indicator) {

		this.mesbopindicator = indicator;
	}

	public void setFamilyAddress(String fmyaddr) {

		this.family_addr = fmyaddr;
	}

	public void setLeftRightHand(String lfHand) {

		this.l_r_hand = lfHand;
	}

	public String getPartNumber() {

		return partNumber;
	}

	public String getPlatform() {

		return platform;
	}

	public String getModelYear() {

		return modelyear;
	}

	public String getPlantCode() {

		return plant;
	}

	public String getMainGroup() {

		return maingroup;
	}

	public String getSubGroup() {
		return subgroup;
	}

	public String getDescription() {

		return groupdescription;
	}

	public String getBOMLineID() {

		return bomlineid;
	}

	public String getQuanity() {

		return quantity;
	}

	public String getFormula() {

		return option;
	}

	public String getDCR() {

		return dcr;
	}

	public String getMCN() {

		return mcn;
	}

	public String getSequenceTime() {
		if (sequence == null || sequence.isEmpty()) {
			sequence = UIGetValuesUtility.getSequenceID();
		}
		return sequence;
	}

	public String getAction() {

		return action;
	}

	public String getWorkStation() {

		return workstation;
	}

	public String getLineSupplyMethod() {

		return linesupplymethod;
	}

	public String getBOPID() {

		return bopid;
	}

	public String getBOPRevision() {

		return revision;
	}

	public String getMESIndicator() {

		return mesbopindicator;
	}

	public String getFamilyAddress() {

		return family_addr;
	}

	public String getLeftRightHand() {

		return l_r_hand;
	}

	public TCComponent getMBOMLine() {
		return MBOMLine;
	}

	public void setMBOMLine(TCComponent mBOMLine) {
		MBOMLine = mBOMLine;
	}

	public TCComponent[] getMBOPLine() {
		return MBOPLine;
	}

	public void setMBOPLine(TCComponent[] mBOPLine) {
		MBOPLine = mBOPLine;
	}

	public String getStationID() {
		return stationID;
	}

	public void setStationID(String stationID) {
		this.stationID = stationID;
	}

	public void processDataOnActionType(ActionType action) {
		actionType = action;
		switch (action) {
		case ADD_PART_IN_BOM:
			getSUPBOM("A");
			break;
		case ADD_PART_IN_BOP:
			getSUPBOP("A");
			break;
		case DELETE_PART_IN_BOM:
			getSUPBOM("D");
			break;
		case DELETE_PART_IN_BOP:
			getSUPBOP("D");
			break;
		case CHANGE_PART_IN_BOM:
			getSUPBOM("C");
			break;
		case CHANGE_OPERATION_REVISION:
			getSUPBOP("C");
			break;
		case ADD_OPERATION_NO_PART:
			getSUPBOP("A");
			break;
		case DELETE_OPERATION_NO_PART:
			getSUPBOP("D");
			break;

		default:
			break;
		}
	}

	public String getXmlFileName(FileType type, int count) {
		String prefix = "";
		switch (type) {
		case IN_PUT:
			prefix = "I";
			break;
		case OUT_PUT:
			prefix = "O";
			break;
		default:
			break;
		}
		String fileName = "";
		switch (actionType) {
		case ADD_PART_IN_BOM:
			fileName = String.format("%s_BOM_A_%s_%s_%s_%s", prefix, Integer.toString(count), this.getPartNumber(), this.getSubGroup(), this.getBOMLineID());
			break;
		case ADD_PART_IN_BOP:
			fileName = String.format("%s_BOP_A_%s_%s_%s_%s", prefix, Integer.toString(count), this.getWorkStation(), this.getBOPID(), this.getBOMLineID());
			break;
		case DELETE_PART_IN_BOM:
			fileName = String.format("%s_BOM_D_%s_%s_%s_%s", prefix, Integer.toString(count), this.getPartNumber(), this.getSubGroup(), this.getBOMLineID());
			break;
		case DELETE_PART_IN_BOP:
			fileName = String.format("%s_BOP_D_%s_%s_%s_%s", prefix, Integer.toString(count), this.getWorkStation(), this.getBOPID(), this.getBOMLineID());
			break;
		case ADD_OPERATION_NO_PART:
			fileName = String.format("%s_BOP_A_%s_%s_%s", prefix, Integer.toString(count), this.getWorkStation(), this.getBOPID());
			break;
		case DELETE_OPERATION_NO_PART:
			fileName = String.format("%s_BOP_D_%s_%s_%s", prefix, Integer.toString(count), this.getWorkStation(), this.getBOPID());
			break;
		case CHANGE_PART_IN_BOM:
			fileName = String.format("%s_BOM_C_%s_%s_%s_%s", prefix, Integer.toString(count), this.getPartNumber(), this.getSubGroup(), this.getBOMLineID());
			break;
		case CHANGE_OPERATION_REVISION:
			fileName = String.format("%s_BOP_C_%s_%s_%s_%s", prefix, Integer.toString(count), this.getPartNumber(), this.getBOPID(), this.getBOMLineID());
			break;

		default:
			break;
		}

		return fileName;
	}

	public HashMap<String, String> getXmlData() {
		HashMap<String, String> data = new HashMap<String, String>();
		switch (actionType) {
		case ADD_PART_IN_BOM:
			data = getSUPBOM("A");
			break;
		case ADD_PART_IN_BOP:
			data = getSUPBOP("A");
			break;
		case DELETE_PART_IN_BOM:
			data = getSUPBOM("D");
			break;
		case DELETE_PART_IN_BOP:
			data = getSUPBOP("D");
			break;
		case ADD_OPERATION_NO_PART:
			data = getSUPBOP("A");
			break;
		case DELETE_OPERATION_NO_PART:
			data = getSUPBOP("D");
			break;
		case CHANGE_OPERATION_REVISION:
			data = getSUPBOP("C");
			break;
		case CHANGE_PART_IN_BOM:
			data = getSUPBOM("C");
			break;
		default:
			break;
		}
		return data;
	}

	public String getSAPWebServiceUrl(String serverIp) {
		switch (actionType) {
		case ADD_PART_IN_BOM:
		case CHANGE_PART_IN_BOM:
		case DELETE_PART_IN_BOM:
			return SAPConnect.superbomWebserviceURL(serverIp);

		case ADD_PART_IN_BOP:
		case DELETE_PART_IN_BOP:
		case DELETE_OPERATION_NO_PART:
		case ADD_OPERATION_NO_PART:
		case CHANGE_OPERATION_REVISION:
			return SAPConnect.superbopWebserviceURL(serverIp);

		default:
			return "";
		}
	}

	public String[] getSAPWebServiceInfo() {
		int header = 0;
		int tag = 1;
		int namespace = 2;
		String[] info = new String[3];
		switch (actionType) {
		case ADD_PART_IN_BOM:
		case CHANGE_PART_IN_BOM:
		case DELETE_PART_IN_BOM:
			info[header] = SAPURL.SUP_BOM_HEADER;
			info[tag] = SAPURL.SUP_BOM_TAG;
			info[namespace] = SAPURL.SUP_BOM_NAMESPACE;
			break;
		case ADD_PART_IN_BOP:
		case DELETE_PART_IN_BOP:
		case DELETE_OPERATION_NO_PART:
		case ADD_OPERATION_NO_PART:
		case CHANGE_OPERATION_REVISION:
			info[header] = SAPURL.SUP_BOP_HEADER;
			info[tag] = SAPURL.SUP_BOP_TAG;
			info[namespace] = SAPURL.SUP_BOP_NAMESPACE;
			break;
		default:
			return info;
		}
		return info;
	}

	public String[] getPrintResultMessage(String response, boolean isSucces, int count) {
		String[] msgs = new String[7];
		switch (actionType) {
		case ADD_PART_IN_BOM:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getPartNumber(), this.getBOMLineID(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case ADD_PART_IN_BOP:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getBOMLineID(), this.getWorkStation(), this.getBOPID(), this.getBOPRevision(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case DELETE_PART_IN_BOM:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getPartNumber(), this.getBOMLineID(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case DELETE_PART_IN_BOP:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getBOMLineID(), this.getWorkStation(), this.getBOPID(), this.getBOPRevision(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case CHANGE_PART_IN_BOM:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getPartNumber(), this.getBOMLineID(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case ADD_OPERATION_NO_PART:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), "-", this.getWorkStation(), this.getBOPID(), this.getBOPRevision(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case DELETE_OPERATION_NO_PART:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), "-", this.getWorkStation(), this.getBOPID(), this.getBOPRevision(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		case CHANGE_OPERATION_REVISION:
			msgs = new String[] { Integer.toString(count), this.getSubGroup(), this.getBOMLineID(), this.getWorkStation(), this.getBOPID(), this.getBOPRevision(), response, this.getAction(), isSucces ? "Success" : "Error" };
			break;
		default:
			break;
		}
		return msgs;
	}

	public boolean isSubGroupReleased() {
		return isSubGroupReleased;
	}

	public void setSubGroupReleased(boolean isSubGroupReleased) {
		this.isSubGroupReleased = isSubGroupReleased;
	}

	public boolean isWorkStationReleased() {
		return isWorkStationReleased;
	}

	public void setWorkStationReleased(boolean isWorkStationReleased) {
		this.isWorkStationReleased = isWorkStationReleased;
	}

	public boolean isOperationReleased() {
		return isOperationReleased;
	}

	public void setOperationReleased(boolean isOperationReleased) {
		this.isOperationReleased = isOperationReleased;
	}
	
	public void setVfDesignator(String vfDesignator) {
		this.vfDesignator = vfDesignator;
	}
	
	private String getVfDesignator() {
		return this.vfDesignator;
	}


	public boolean isBomData() {
		switch (actionType) {
		case ADD_PART_IN_BOM:
		case CHANGE_PART_IN_BOM:
		case DELETE_PART_IN_BOM:
			return true;

		case ADD_PART_IN_BOP:
		case DELETE_PART_IN_BOP:
		case DELETE_OPERATION_NO_PART:
		case ADD_OPERATION_NO_PART:
		case CHANGE_OPERATION_REVISION:
			return false;

		default:
			return true;
		}
	}

}
