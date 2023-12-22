package com.teamcenter.vinfast.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.sc.utilities.PropertyDefines;

import vfplm.soa.common.define.Constant.VFBOMLINE;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class ValidatorCOP_CUV implements IValidator {
	TCSession session;

	public ValidatorCOP_CUV() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator validator05 = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				String purchaseLevel = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String[] purchaseLevelAvai = new String[] { PropertyDefines.PUR_LEVEL_P, PropertyDefines.PUR_LEVEL_AXS, PropertyDefines.PUR_LEVEL_DPT, PropertyDefines.PUR_LEVEL_DPS, PropertyDefines.PUR_LEVEL_BL };

				if (purchaseLevel.isEmpty())
					return "Purchase Level is blank. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				else {
					if (!Arrays.asList(purchaseLevelAvai).contains(purchaseLevel))
						return "Purchase Level is not valid. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				}

				return "";
			}
		};
		validators.add(validator05);

		Validator validator07 = new Validator(partBomLine, "Change Index") {
			@Override
			public String validate() throws TCException {
				String changeIndex = bomLine.getProperty(VFBOMLINE.CHANGE_INDEX);
				String errorMsg = "";

				if (changeIndex != null && changeIndex.trim().isEmpty()) {
					errorMsg = "Please fill part line's Change Index.";
				}
				return errorMsg;
			}
		};
		validators.add(validator07);

		Validator validator15 = new Validator(partBomLine, "S Release") {
			@Override
			public String validate() throws TCException {
				String releaseStatuses = revision.getProperty(VFWorkspaceObject.ATTR_RELEASE_STATUS_LIST);
				String errorMsg = "";
				if (releaseStatuses != null && releaseStatuses.contains("Sourcing")) {
					errorMsg = "Part completed Starting Sourcing process already.";
				}
				return errorMsg;
			}
		};
		validators.add(validator15);

		for (Validator validator : validators) {
			String errorMsg = validator.validate();

			ValidationResult result = null;
			if (errorMsg.isEmpty() == false) {
				result = new ValidationResult(validator.getValidationName(), false, errorMsg);
			} else {
				String passImage = ValidationResult.getValidationPassImage();
				result = new ValidationResult(validator.getValidationName(), true, passImage);
			}
			results.add(result);
			if (result.getValidationName().equals("S Release") && result.isPassed() == true) {
				result.setShown(false);
			}
		}
		return results;
	}

}
