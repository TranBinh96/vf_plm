package com.teamcenter.vinfast.change;

import java.util.Arrays;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.TCExtension;

public class ECRProcessValidate {
	public static ValidationResult validateObjectNull(TCComponent validateObject, String validateName, String validateAttribute) throws Exception {
		ValidationResult itemValidate = null;
		if (validateObject.getPropertyDisplayableValue(validateAttribute).isEmpty()) {
			itemValidate = new ValidationResult(validateName, false, "Fill attribute " + validateName);
		} else {
			itemValidate = new ValidationResult(validateName, true, ValidationResult.passImage);
		}

		return itemValidate;
	}

	public static ValidationResult validateObjectInWorkflow(TCComponent part, String processTemplate) throws Exception {
		boolean inWorkflow = false;
		TCComponent[] startedWorkflowTask = part.getRelatedComponents("fnd0StartedWorkflowTasks");
		for (TCComponent startedWorkflow : startedWorkflowTask) {
			if (startedWorkflow.toString().compareToIgnoreCase(processTemplate) == 0) {
				break;
			}
		}
		if (!inWorkflow) {
			TCComponent[] allWorkflowTask = part.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent allWorkflow : allWorkflowTask) {
				if (allWorkflow.toString().compareToIgnoreCase(processTemplate) == 0) {
					inWorkflow = true;
					break;
				}
			}
		}
		ValidationResult itemValidate = null;
		if (inWorkflow) {
			itemValidate = new ValidationResult("Not in ECR/ECN Process", false, "Remove to proposal folder");
		} else {
			itemValidate = new ValidationResult("Not in ECR/ECN Process", true, ValidationResult.passImage);
		}
		return itemValidate;
	}

	public static ValidationResult validateObjectIsAfterSale(TCComponent part) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			TCComponentItem partItem = ((TCComponentItemRevision) part).getItem();
			TCComponentItemRevision[] partRevisionList = partItem.getReleasedItemRevisions();
			if (partRevisionList.length > 0) {
				boolean check = false;
				for (TCComponentItemRevision partRevision : partRevisionList) {
					String statusList = partRevision.getPropertyDisplayableValue("release_status_list");
					if (!statusList.isEmpty()) {
						if (Arrays.stream(TCExtension.releaseStatusList).anyMatch(statusList::equals)) {
							check = true;
							break;
						}
					}
				}
				if (check) {
					return validateObjectNull(((TCComponentItemRevision) part).getItem(), "Is After Sale", "vf4_itm_after_sale_relevant");
				}
			}
		}
		return null;
	}

	public static ValidationResult validateObjectReleaseCombineStatus(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			TCComponentItem partItem = ((TCComponentItemRevision) part).getItem();
			boolean check = true;
			int statusIndex = TCExtension.getReleaseStatusIndex(status);
			TCComponentItemRevision[] partRevisionList = partItem.getReleasedItemRevisions();
			if (partRevisionList != null && partRevisionList.length > 0) {
				for (TCComponentItemRevision partRevision : partRevisionList) {
					String statusList = partRevision.getPropertyDisplayableValue("release_status_list");
					int index = TCExtension.getReleaseStatusIndex(statusList);
					if (index >= statusIndex) {
						check = false;
						break;
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Part Release", false, "Part already release");
			} else {
				itemValidate = new ValidationResult("Part Release", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	public static ValidationResult validateObjectBaselineCheck(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			boolean check = false;
			TCComponentItemRevision[] baselineRevList = ((TCComponentItemRevision) part).listBaselineRevs("");
			if (baselineRevList != null && baselineRevList.length > 0) {
				for (TCComponentItemRevision baselineRev : baselineRevList) {
					String statusList = baselineRev.getPropertyDisplayableValue("release_status_list");
					if (statusList.compareTo(status) == 0) {
						check = true;
						break;
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Baseline Check", false, "Baseline part with " + status + " status");
			} else {
				itemValidate = new ValidationResult("Baseline Check", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	public static ValidationResult validateObjectChildRelease(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			boolean check = true;
			int indexStatus = TCExtension.getReleaseStatusIndex(status);
			AIFComponentContext[] childPartList = ((TCComponentItemRevision) part).getChildren("PR4D_cad");
			if (childPartList != null && childPartList.length > 0) {
				for (AIFComponentContext childPart : childPartList) {
					if (childPart.getComponent() instanceof TCComponentItemRevision) {
						TCComponentItemRevision child = (TCComponentItemRevision) childPart.getComponent();
						String statusList = child.getPropertyDisplayableValue("release_status_list");
						int index = TCExtension.getReleaseStatusIndex(statusList);
						if (index < indexStatus) {
							check = false;
							break;
						}
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Child Part Release", false, "Release child part with " + status + " status");
			} else {
				itemValidate = new ValidationResult("Child Part Release", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	public static ValidationResult validateObjectIsModifiedPart(TCComponent part) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			String isModified = ((TCComponentItemRevision) part).getItem().getPropertyDisplayableValue("vf4_is_modified_part");
			if (isModified.compareTo("Yes") == 0) {
				return validateObjectNull(((TCComponentItemRevision) part).getItem(), "Old Part Number", "vf4_orginal_part_number");
			}
		}
		return null;
	}

	public static ValidationResult validateFolderNotHaveBaselineRevision(TCComponent[] partList, String folderName) throws Exception {
		if (partList != null && partList.length > 0) {
			for (TCComponent part : partList) {
				String revID = part.getPropertyDisplayableValue("item_revision_id");
				if (revID.contains("."))
					return new ValidationResult(folderName, false, "Have baseline revision");
			}
		}
		return new ValidationResult(folderName, true, ValidationResult.passImage);
	}
}
