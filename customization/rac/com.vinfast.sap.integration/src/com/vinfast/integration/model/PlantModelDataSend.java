package com.vinfast.integration.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class PlantModelDataSend extends ModelAbstract{
	TCComponentItemRevision item;
	ArrayList<HashMap<String, StringBuffer>> data;
	
	public PlantModelDataSend() {
		super(ModelType.DATA_SEND);
	}
	public TCComponentItemRevision getItem() {
		return item;
	}
	public void setItem(TCComponentItemRevision item) {
		this.item = item;
	}
	public ArrayList<HashMap<String, StringBuffer>> getData() {
		return data;
	}
	public void setData(ArrayList<HashMap<String, StringBuffer>> data) {
		this.data = data;
	}
}
