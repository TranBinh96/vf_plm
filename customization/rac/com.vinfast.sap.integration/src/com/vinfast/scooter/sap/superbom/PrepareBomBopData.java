package com.vinfast.scooter.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class PrepareBomBopData {
	
	public PrepareBomBopData() {
		// TODO Auto-generated constructor stub
	}
	
	private HashMap<String, HashMap<String, String>>  oldBOMDataMap = new HashMap<String, HashMap<String, String>>() ;
	private HashMap<String, HashMap<String, String>>  newBOMDataMap = new HashMap<String, HashMap<String, String>>() ;
	private HashMap<String, ArrayList<HashMap<String,String>>>  newBOPDataMap = new HashMap<String, ArrayList<HashMap<String,String>>>();
	private TCComponentItemRevision newItemRevision = null;
	
	public TCComponentItemRevision getNewItemRevision() {
		return newItemRevision;
	}
	public void setNewItemRevision(TCComponentItemRevision newItemRevision) {
		this.newItemRevision = newItemRevision;
	}
	public HashMap<String, HashMap<String, String>> getOldBOMDataMap() {
		return oldBOMDataMap;
	}
	public void setOldBOMDataMap(HashMap<String, HashMap<String, String>> oldBOMDataMap) {
		this.oldBOMDataMap = oldBOMDataMap;
	}
	public HashMap<String, HashMap<String, String>> getNewBOMDataMap() {
		return newBOMDataMap;
	}
	public void setNewBOMDataMap(HashMap<String, HashMap<String, String>> newBOMDataMap) {
		this.newBOMDataMap = newBOMDataMap;
	}
	public HashMap<String, ArrayList<HashMap<String, String>>> getNewBOPDataMap() {
		return newBOPDataMap;
	}
	public void setNewBOPDataMap(HashMap<String, ArrayList<HashMap<String, String>>> newBOPDataMap) {
		this.newBOPDataMap = newBOPDataMap;
	}
}
