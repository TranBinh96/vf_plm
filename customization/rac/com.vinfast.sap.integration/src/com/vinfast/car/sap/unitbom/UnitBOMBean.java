package com.vinfast.car.sap.unitbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.sap.util.PropertyDefines;

public class UnitBOMBean {
	String subgroup;
	LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> BOMBOPMap;
	TCComponentItemRevision subGroupObject;
	
	public UnitBOMBean(TCComponentItemRevision group, LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> map){
		subGroupObject = group;
		try {
			subgroup = group.getProperty(PropertyDefines.ITEM_ID)+"/"+group.getProperty(PropertyDefines.ITEM_REV_ID);
		} catch (TCException e) {
			e.printStackTrace();
		}
		BOMBOPMap = map;
	}
	
	public String getSubGroupID() {
		return subgroup;
	}
	public TCComponentItemRevision getSubGroupRevision() {
		return subGroupObject;
	}
	
	public LinkedHashMap<HashMap<String, String>, ArrayList<HashMap<String, String>>> getBOMBOPRecords(){
		return BOMBOPMap;
	}
}