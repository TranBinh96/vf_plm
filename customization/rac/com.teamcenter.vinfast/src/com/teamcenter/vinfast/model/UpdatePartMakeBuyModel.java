package com.teamcenter.vinfast.model;

import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class UpdatePartMakeBuyModel {
	private String partNumber = "";
	private String partName = "";
	private String releaseStatus = "";
	private String partMakeBuy = ""; 
	private String taskStatus = "";
	
	private TCComponentItem objectItem;
	
	public UpdatePartMakeBuyModel(TCComponentItem _objectItem) {
		try {
			this.objectItem = _objectItem;
			this.partNumber = objectItem.getPropertyDisplayableValue("item_id");
			this.partName = objectItem.getPropertyDisplayableValue("object_name");
			this.partMakeBuy = objectItem.getPropertyDisplayableValue("vf4_item_make_buy");
			this.releaseStatus = getReleaseStatus(objectItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getReleaseStatus(TCComponentItem item) throws TCException, NotLoadedException {
		TCComponentItemRevision[] itemRevision = item.getReleasedItemRevisions();
		if (itemRevision.length > 0) {
			return itemRevision[0].getPropertyDisplayableValue("release_status_list");
		}
		return "";
	}
	
	public String getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}
	public String getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	public String getPartName() {
		return partName;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	public String getReleaseStatus() {
		return releaseStatus;
	}
	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}
	public TCComponentItem getObjectItem() {
		return objectItem;
	}
	public void setObjectItem(TCComponentItem objectItem) {
		this.objectItem = objectItem;
	}
	public String getPartMakeBuy() {
		return partMakeBuy;
	}
	public void setPartMakeBuy(String partMakeBuy) {
		this.partMakeBuy = partMakeBuy;
	}
}
