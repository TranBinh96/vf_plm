package com.vinfast.car.sap.unitbom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.sap.util.PropertyDefines;

public class UnitOWPBean {
	String operationID;
	TCComponentItemRevision operationRevision;
	ArrayList<HashMap<String, String>> OWPMap;

	public UnitOWPBean(TCComponentItemRevision revision, ArrayList<HashMap<String, String>> Map) {
		operationRevision = revision;
		OWPMap = Map;
		try {
			operationID = revision.getProperty(PropertyDefines.ITEM_ID) + "/" + revision.getProperty(PropertyDefines.ITEM_REV_ID);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String getOperationID() {
		return operationID;
	}

	public TCComponentItemRevision getOperationRevision() {
		return operationRevision;
	}

	public ArrayList<HashMap<String, String>> getOWPMap() {
		return OWPMap;
	}
}
