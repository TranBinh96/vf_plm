package com.teamcenter.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public class MCNInformation extends ModelAbstract {
	public MCNInformation() {
		super(ModelType.MCN_INFORMATION);
	}

	String plant;
	String sapID;
	String mcnID;
	String modelYear;
	String platForm;
	String mainGroup;
	String materialCode;
	String shop;

	public String getMaterialCode() {
		return materialCode;
	}

	public void setMaterialCode(String materialCode) {
		this.materialCode = materialCode;
	}

	public String getModelYear() {
		return modelYear;
	}

	public void setModelYear(String modelYear) {
		this.modelYear = modelYear;
	}

	public String getPlatForm() {
		return platForm;
	}

	public void setPlatForm(String platForm) {
		this.platForm = platForm;
	}

	public String getMainGroup() {
		return mainGroup;
	}

	public void setMainGroup(String mainGroup) {
		this.mainGroup = mainGroup;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	public String getSapID() {
		return sapID;
	}

	public void setSapID(String sapID) {
		this.sapID = sapID;
	}

	public String getMcnID() {
		return mcnID;
	}

	public void setMcnID(String mcnID) {
		this.mcnID = mcnID;
	}

	public String getShop() {
		return shop;
	}

	public void setShop(String shop) {
		this.shop = shop;
	}

}
