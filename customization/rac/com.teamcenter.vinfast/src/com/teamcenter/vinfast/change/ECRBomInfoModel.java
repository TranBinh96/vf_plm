package com.teamcenter.vinfast.change;

public class ECRBomInfoModel {
	private String ecrNameProperty;
	private String bomNameProperty;
	private String valueProperty;
	
	public ECRBomInfoModel(String ecrNameProperty, String bomNameProperty) {
		this.ecrNameProperty = ecrNameProperty;
		this.bomNameProperty = bomNameProperty;
	}
	
	public String getEcrNameProperty() {
		return ecrNameProperty;
	}
	public void setEcrNameProperty(String ecrNameProperty) {
		this.ecrNameProperty = ecrNameProperty;
	}
	public String getBomNameProperty() {
		return bomNameProperty;
	}
	public void setBomNameProperty(String bomNameProperty) {
		this.bomNameProperty = bomNameProperty;
	}
	public String getValueProperty() {
		return valueProperty;
	}
	public void setValueProperty(String valueProperty) {
		this.valueProperty = valueProperty;
	}
}
