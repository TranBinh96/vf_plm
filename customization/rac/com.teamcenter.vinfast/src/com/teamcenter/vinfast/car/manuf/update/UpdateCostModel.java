package com.teamcenter.vinfast.car.manuf.update;

import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class UpdateCostModel {
	private TCComponentItemRevision objectItem = null;
	private String validMessage = "";
	private String processMessage = "";

	public TCComponentItemRevision getObjectItem() {
		return objectItem;
	}

	public void setObjectItem(TCComponentItemRevision objectItem) {
		this.objectItem = objectItem;
	}

	public String getValidMessage() {
		return validMessage;
	}

	public void setValidMessage(String validMessage) {
		this.validMessage = validMessage;
	}

	public String getProcessMessage() {
		return processMessage;
	}

	public void setProcessMessage(String processMessage) {
		this.processMessage = processMessage;
	}

	public boolean isReadyProcess() {
		return validMessage.isEmpty();
	}

	public String getItemID() {
		try {
			if (objectItem != null) {
				return objectItem.getPropertyDisplayableValue("item_id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getRevisionID() {
		try {
			if (objectItem != null)
				return objectItem.getPropertyDisplayableValue("item_revision_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getObjectType() {
		try {
			if (objectItem != null) {
				return objectItem.getTypeObject().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
