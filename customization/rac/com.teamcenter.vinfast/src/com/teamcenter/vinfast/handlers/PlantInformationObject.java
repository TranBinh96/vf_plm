package com.teamcenter.vinfast.handlers;

public class PlantInformationObject {
	private String partNumber;
	private String plantCode;
	private String department;
	private String makeOrBuy;
	private String posID;
	
	public String getPosID() {
		return posID;
	}
	public void setPosID(String posID) {
		this.posID = posID;
	}
	public String getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	public String getPlantCode() {
		return plantCode;
	}
	public void setPlantCode(String plantCode) {
		this.plantCode = plantCode;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getMakeOrBuy() {
		return makeOrBuy;
	}
	public void setMakeOrBuy(String makeOrBuy) {
		this.makeOrBuy = makeOrBuy;
	}
	
	public PlantInformationObject() {
		this.plantCode = "";
		this.department = "";
		this.makeOrBuy = "";
		this.plantCode = "";
		this.posID = "";
	}
	
	
}
