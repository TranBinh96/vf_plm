package com.vinfast.car.sap.superbom;

public class BomTrackingModel {
//	public enum ActionType{
//		ACTION_ADD,
//		ACTION_DELETE,
//		ACTION_REPLACE,
//	}

	private String actionType;
	private String mainGroup;
	private String subGroup;
	private String bomlineID;
	private String partNumber;
	private String quantity;
	private String newPartNumber;
	private String replacedPartNumber;
	private String isTransferred;

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		if (!actionType.equals("*")) {
			this.actionType = actionType;
		}
		if (actionType.equals("A")) {
			this.actionType = "Add";
		}

		if (actionType.equals("D")) {
			this.actionType = "Delete";
		}

		if (actionType.equals("R")) {
			this.actionType = "Replaced";
		}
	}

	public String getMainGroup() {
		return mainGroup;
	}

	public void setMainGroup(String mainGroup) {
		if (!mainGroup.equals("*"))
			this.mainGroup = mainGroup;
	}

	public String getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(String subGroup) {
		if (!subGroup.equals("*"))
			this.subGroup = subGroup;
	}

	public String getBomlineID() {
		return bomlineID;
	}

	public void setBomlineID(String bomlineID) {
		if (!bomlineID.equals("*"))
			this.bomlineID = bomlineID;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		if (!partNumber.equals("*"))
			this.partNumber = partNumber;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		if (!quantity.equals("*"))
			this.quantity = quantity;
	}

	public String getNewPartNumber() {
		return newPartNumber;
	}

	public void setNewPartNumber(String newPartNumber) {
		if (!newPartNumber.equals("*"))
			this.newPartNumber = newPartNumber;
	}

	public String getReplacedPartNumber() {
		return replacedPartNumber;
	}

	public void setReplacedPartNumber(String replacedPartNumber) {
		if (!replacedPartNumber.equals("*"))
			this.replacedPartNumber = replacedPartNumber;
	}

	public String getIsTransferred() {
		return isTransferred;
	}

	public void setIsTransferred(String isTransferred) {
		if (!isTransferred.equals("*"))
			this.isTransferred = isTransferred;
	}

}
