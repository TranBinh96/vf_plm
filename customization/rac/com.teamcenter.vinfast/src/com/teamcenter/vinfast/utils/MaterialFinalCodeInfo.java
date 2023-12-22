package com.teamcenter.vinfast.utils;

import java.util.ArrayList;

public class MaterialFinalCodeInfo {
	
	private String materialCode;
	private String context;
	private String name;
	private ArrayList<String> familyFeature;
	
	public MaterialFinalCodeInfo() {
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMaterialCode() {
		return materialCode;
	}

	public void setMaterialCode(String materialCode) {
		this.materialCode = materialCode;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public ArrayList<String> getFamilyFeature() {
		return familyFeature;
	}

	public void setFamilyFeature(ArrayList<String> familyFeature) {
		this.familyFeature = familyFeature;
	}
}
