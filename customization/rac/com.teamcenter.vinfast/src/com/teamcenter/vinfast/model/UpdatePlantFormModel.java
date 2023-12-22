package com.teamcenter.vinfast.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.TCExtension;

public class UpdatePlantFormModel {
	private String partNumber = "";
	private String partName = "";
	private String releaseStatus = "";
	private String plantForm = "";
	private String partMakeBuy = "";
	private String taskStatus = "";
	private TCComponentItem objectItem;
	private LinkedHashMap<String, PlantFormConfigModel> objectList;

	public UpdatePlantFormModel(TCComponentItem _objectItem, LinkedHashMap<String, PlantFormConfigModel> _objectList) {
		try {
			this.objectItem = _objectItem;
			this.objectList = _objectList;
			this.partNumber = objectItem.getPropertyDisplayableValue("item_id");
			this.partName = objectItem.getPropertyDisplayableValue("object_name");
			String objectType = objectItem.getType();
			PlantFormConfigModel formConfig = objectList.get(objectType);
			this.partMakeBuy = objectItem.getPropertyDisplayableValue(formConfig.getPartMakeBuyProperty());
			this.releaseStatus = getReleaseStatus(objectItem);
			this.plantForm = getPlantForm(objectItem);

//			newItem.setPartNumber(targetItem.getPropertyDisplayableValue("item_id"));
//			newItem.setPartName(targetItem.getPropertyDisplayableValue("object_name"));
//			newItem.setPartMakeBuy(targetItem.getPropertyDisplayableValue("vf4_item_make_buy"));	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private String getReleaseStatus(TCComponentItem item) throws TCException, NotLoadedException {
		TCComponentItemRevision[] itemRevision = item.getReleasedItemRevisions();
		if (itemRevision.length > 0) {
			return itemRevision[0].getPropertyDisplayableValue("release_status_list");
		}
		return "";
	}

	private String getPlantForm(TCComponentItem item) throws TCException, NotLoadedException {
		List<String> plantFormList = new LinkedList<String>();
		PlantFormConfigModel planformConfig = objectList.get(item.getType());
		String relationName = planformConfig.getRelationName();
		if (relationName.isEmpty())
			return "";
		TCComponent[] forms = item.getRelatedComponents(relationName);
		if (forms.length > 0) {
			String plantProperty = planformConfig.getPlantProperty();
			String procecurementProperty = planformConfig.getProcumentProperty();
			String plantForm = "";
			for (TCComponent form : forms) {
				String plantCode = form.getPropertyDisplayableValue(plantProperty);
				String procuType = TCExtension.GetPropertyRealValue(form, procecurementProperty);
				if (!plantCode.isEmpty() || !procuType.isEmpty()) {
					plantFormList.add(plantCode + "-" + procuType);

				}
			}
			List<String> sortedList = plantFormList.stream().sorted().collect(Collectors.toList());
			return String.join(" | ", sortedList);
		}

		return "";
	}

	public String getPlantForm() {
		return plantForm;
	}

	public void setPlantForm(String plantForm) {
		this.plantForm = plantForm;
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
