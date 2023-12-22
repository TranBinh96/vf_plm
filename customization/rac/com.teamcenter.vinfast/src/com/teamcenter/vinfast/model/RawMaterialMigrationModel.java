package com.teamcenter.vinfast.model;

import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;

public class RawMaterialMigrationModel {
	private TCSession session;
	private boolean isBlank = true;
	private TCComponentItemRevision partRevision = null;
	private String partNumber = "";
	private String partName = "";
	private String partMaterial = "";
	private String partCoating = "";
	private double partThickness = 0.0;
	private double partWidth = 0.0;
	private double partLength = 0.0;
	private boolean partPurchase = false;
	private LinkedHashMap<String, TCComponentItemRevision> relationList = new LinkedHashMap<String, TCComponentItemRevision>();
	private LinkedHashMap<String, TCComponentItemRevision> relationHPartList = new LinkedHashMap<String, TCComponentItemRevision>();
	private LinkedHashMap<String, TCComponentItemRevision> relationBlankList = new LinkedHashMap<String, TCComponentItemRevision>();
	private LinkedHashMap<String, TCComponentItemRevision> relationCoilList = new LinkedHashMap<String, TCComponentItemRevision>();

	public RawMaterialMigrationModel(TCSession _session, boolean _isBlank) {
		session = _session;
		isBlank = _isBlank;
	}

	public void addRelationItem(String partNumber, TCComponentItemRevision relationItem) {
		if (relationList.containsKey(partNumber))
			return;

		relationList.put(partNumber, relationItem);
	}

	public void addRelationItem(String partNumber, String partRevision) {
		if (relationList.containsKey(partNumber))
			return;

		if (!partNumber.isEmpty()) {
			TCComponentItemRevision relationItem = null;
			if (!partRevision.isEmpty()) {
				LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
				inputQuery.put("Item ID", partNumber);
				inputQuery.put("Revision", partRevision);
				inputQuery.put("Type", "VF Design Revision");
				TCComponent[] item_search = Query.queryItem(session, inputQuery, "Item Revision...");
				if (item_search != null && item_search.length > 0)
					relationItem = (TCComponentItemRevision) item_search[0];
			} else {
				LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
				inputQuery.put("ID", partNumber);
				inputQuery.put("Type", "VF Design Revision");
				TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Matured Revision");
				if (item_search != null && item_search.length > 0)
					relationItem = (TCComponentItemRevision) item_search[0];
			}
			if (relationItem != null) {
				relationList.put(partNumber, relationItem);
			}
		}
	}

	public TCComponentItemRevision getPartRevision() {
		return partRevision;
	}

	public void setPartRevision(TCComponentItemRevision partRevision) {
		this.partRevision = partRevision;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public boolean setPartNumber(TCComponentItemRevision partRev) {
		partRevision = partRev;
		try {
			partNumber = partRevision.getPropertyDisplayableValue("item_id");
			partName = partRevision.getPropertyDisplayableValue("object_name");
			if (!partName.isEmpty()) {
				if (isBlank)
					return blankValidate();
				else
					return coilValidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean setPartNumber(TCComponentItemRevision partRev, String name) {
		partRevision = partRev;
		try {
			partNumber = partRevision.getPropertyDisplayableValue("item_id");
			partName = name;
			if (!partName.isEmpty()) {
				if (isBlank)
					return blankValidate();
				else
					return coilValidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getPartMaterial() {
		return partMaterial;
	}

	public void setPartMaterial(String partMaterial) {
		this.partMaterial = partMaterial;
	}

	public String getPartCoating() {
		return partCoating;
	}

	public void setPartCoating(String partCoating) {
		this.partCoating = partCoating;
	}

	public double getPartThickness() {
		return partThickness;
	}

	public void setPartThickness(Double partThickness) {
		this.partThickness = partThickness;
	}

	public double getPartWidth() {
		return partWidth;
	}

	public void setPartWidth(Double partWidth) {
		this.partWidth = partWidth;
	}

	public double getPartLength() {
		return partLength;
	}

	public void setPartLength(Double partLength) {
		this.partLength = partLength;
	}

	public boolean isPartPurchase() {
		return partPurchase;
	}

	public void setPartPurchase(String partPurchase) {
		this.partPurchase = (partPurchase.compareTo("Yes") == 0);
	}

	public LinkedHashMap<String, TCComponentItemRevision> getRelationList() {
		return relationList;
	}

	private boolean blankValidate() {
		if (partName.contains("_")) {
			String[] str = partName.split("_");
			if (str[0].compareToIgnoreCase("BLANK") != 0 && str[0].compareToIgnoreCase("BLN") != 0) {
				return false;
			}
			if (str.length == 6) {
				// material
				partMaterial = str[1];
				// coating
				partCoating = str[2];
				// thickness
				if (StringExtension.isInteger(str[3], 10)) {
					partThickness = (double) Integer.parseInt(str[3]) / 100;
				}
				// width
				if (StringExtension.isInteger(str[4], 10)) {
					partWidth = Integer.parseInt(str[4]);
				}
				// length
				if (StringExtension.isInteger(str[5], 10)) {
					partLength = Integer.parseInt(str[5]);
				}
			} else if (str.length == 5) {
				// material
				partMaterial = str[1];
				// thickness
				if (StringExtension.isInteger(str[2], 10)) {
					partThickness = (double) Integer.parseInt(str[2]) / 100;
				}
				// width
				if (StringExtension.isInteger(str[3], 10)) {
					partWidth = Integer.parseInt(str[3]);
				}
				// length
				if (StringExtension.isInteger(str[4], 10)) {
					partLength = Integer.parseInt(str[4]);
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean coilValidate() {
		if (partName.contains("_")) {
			String[] str = partName.split("_");
			if (str[0].compareToIgnoreCase("COIL") != 0) {
				return false;
			}
			if (str.length == 5) {
				// material
				partMaterial = str[1];
				// coating
				partCoating = str[2];
				// thickness
				if (StringExtension.isInteger(str[3], 10)) {
					partThickness = (double) Integer.parseInt(str[3]) / 100;
				}
				// width
				if (StringExtension.isInteger(str[4], 10)) {
					partWidth = Integer.parseInt(str[4]);
				}
			} else if (str.length == 4) {
				// material
				partMaterial = str[1];
				// thickness
				if (StringExtension.isInteger(str[2], 10)) {
					partThickness = (double) Integer.parseInt(str[2]) / 100;
				}
				// width
				if (StringExtension.isInteger(str[3], 10)) {
					partWidth = Integer.parseInt(str[3]);
				}
				// length
				if (StringExtension.isInteger(str[4], 10)) {
					partLength = Integer.parseInt(str[4]);
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	public void addRelationHPartItem(String partNumber, TCComponentItemRevision relationItem) {
		if (relationHPartList.containsKey(partNumber))
			return;

		if (relationItem == null)
			return;

		relationHPartList.put(partNumber, relationItem);
	}

	public void addRelationBlankItem(String partNumber, TCComponentItemRevision relationItem) {
		if (relationBlankList.containsKey(partNumber))
			return;

		if (relationItem == null)
			return;

		relationBlankList.put(partNumber, relationItem);
	}

	public void addRelationCoilItem(String partNumber, TCComponentItemRevision relationItem) {
		if (relationCoilList.containsKey(partNumber))
			return;

		if (relationItem == null)
			return;

		relationCoilList.put(partNumber, relationItem);
	}

	public LinkedHashMap<String, TCComponentItemRevision> getRelationHPartList() {
		return relationHPartList;
	}

	public LinkedHashMap<String, TCComponentItemRevision> getRelationBlankList() {
		return relationBlankList;
	}

	public LinkedHashMap<String, TCComponentItemRevision> getRelationCoilList() {
		return relationCoilList;
	}
}
