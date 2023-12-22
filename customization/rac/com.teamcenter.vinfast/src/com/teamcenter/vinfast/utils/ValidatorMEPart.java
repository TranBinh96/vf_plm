package com.teamcenter.vinfast.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import vfplm.soa.common.define.Constant.VFItem;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class ValidatorMEPart implements IValidator {
	TCSession session;

	public ValidatorMEPart() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator descriptionValidator = new Validator(partBomLine, "Description") {
			@Override
			public String validate() throws TCException {
				String desc = revision.getProperty(VFWorkspaceObject.DESCRIPTION);

				if (desc != null && desc.trim().isEmpty()) {
					return "Please fill part's Description.";
				}

				return "";
			}
		};
		validators.add(descriptionValidator);

		Validator makeBuyValidator = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String makeBuy = item.getProperty(VFItem.ATTR_MAKE_BUY);

				if (makeBuy != null && makeBuy.trim().isEmpty()) {
					return "Please fill part's Make/Buy.";
				}
				return "";
			}
		};
		validators.add(makeBuyValidator);

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
