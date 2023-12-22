package com.teamcenter.vinfast.change;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponent;

public class ECRInput {
	public static final String ECR_INFO_BOM_TABLE_ATTR = "vf6_bom_information";

	private Map<String, String> attributeNamesAndValues;
	private List<Map<String, String>> bomAttributesAndValues;
	private TCComponent ecrItem;
	private TCComponent ecrRev;

	public ECRInput(Map<String, String> attributeNamesAndValues, List<Map<String, String>> bomAttributesAndValues, TCComponent ecrItem, TCComponent ecrRev) {
		super();
		this.attributeNamesAndValues = attributeNamesAndValues;
		this.bomAttributesAndValues = bomAttributesAndValues;
		this.ecrItem = ecrItem;
		this.ecrRev = ecrRev;
	}

	public Map<String, String> getAttributeNamesAndValues() {
		return attributeNamesAndValues;
	}

	public void setAttributeNamesAndValues(Map<String, String> attributeNamesAndValues) {
		this.attributeNamesAndValues = attributeNamesAndValues;
	}

	public List<Map<String, String>> getBomAttributesAndValues() {
		return bomAttributesAndValues;
	}

	public void setBomAttributesAndValues(List<Map<String, String>> bomAttributesAndValues) {
		this.bomAttributesAndValues = bomAttributesAndValues;
	}

	public TCComponent getEcrItem() {
		return ecrItem;
	}

	public void setEcrItem(TCComponent ecrItem) {
		this.ecrItem = ecrItem;
	}

	public TCComponent getEcrRev() {
		return ecrRev;
	}

	public void setEcrRev(TCComponent ecrRev) {
		this.ecrRev = ecrRev;
	}

	public static ECRInput createEmptyInput(TCComponent ecrItem, TCComponent ecrRev) {
		return new ECRInput(new HashMap<String, String>(), new LinkedList<Map<String, String>>(), ecrItem, ecrRev);
	}

	public boolean isEmpty() {
		return attributeNamesAndValues.isEmpty();
	}
}
