package com.vinfast.integration.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.sap.util.PropertyDefines;

public class ScooterSuperBomBopDataSend extends ModelAbstract{
	TCComponentItemRevision item = null;
	HashMap<String, String> bomData = new HashMap<String, String>();
	ArrayList<HashMap<String, String>> bopData = new ArrayList<HashMap<String, String>>();
	HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> data = new HashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>>();
	String subGroupId = "";
	public HashMap<String, String> getBomData() {
		return bomData;
	}

	public void setBomData(HashMap<String, String> bomData) {
		this.bomData = bomData;
	}
	
	public TCComponentItemRevision getItem() {
		return item;
	}

	public void setItem(TCComponentItemRevision item) {
		this.item = item;
		try {
			subGroupId = item.getProperty(PropertyDefines.ITEM_ID);
		} catch (TCException e) {
		}
		
	}
	
	public String getSubGroupId() {
		return subGroupId;
	}

	public ArrayList<HashMap<String, String>> getBopData() {
		return bopData;
	}

	public void setBopData(ArrayList<HashMap<String, String>> bopData) {
		this.bopData = bopData;
	}
	
	public void setSingleBopData(HashMap<String, String> bopData) {
		this.bopData.add(bopData);
	}

	public ScooterSuperBomBopDataSend() {
		super(ModelType.DATA_SEND);
	}
}
