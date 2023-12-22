package com.teamcenter.vinfast.model;

public class BOMReport_CADModel {
	private String JT_Last_Save_Date;
	private String CADPart_Last_Save_Date;
	private boolean CATPart_Have_Data = true;
	private boolean CATProduct_Have_Data = true;
	
	public BOMReport_CADModel() {
		this.JT_Last_Save_Date = "";
		this.CADPart_Last_Save_Date = "";
	}
	
	public boolean getCATPart_Have_Data() {
		return CATPart_Have_Data;
	}

	public void setCATPart_Have_Data(boolean value) {
		CATPart_Have_Data = value;
	}

	public boolean getCATProduct_Have_Data() {
		return CATProduct_Have_Data;
	}

	public void setCATProduct_Have_Data(boolean value) {
		CATProduct_Have_Data = value;
	}

	public String getJT_Last_Save_Date() {
		return JT_Last_Save_Date;
	}
	
	public void setJT_Last_Save_Date(String value) {
		JT_Last_Save_Date = value;
	}
	
	public String getCADPart_Last_Save_Date() {
		return CADPart_Last_Save_Date;
	}
	
	public void setCADPart_Last_Save_Date(String value) {
		CADPart_Last_Save_Date = value;
	}
}
