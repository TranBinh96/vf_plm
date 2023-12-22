package com.teamcenter.vinfast.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

import vfplm.soa.common.define.Constant;

public class SnSValidator implements IValidator {
	TCSession session;
	public SnSValidator() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	@Override
	public List<ValidationResult> validate(TCComponent bomlineComp) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { bomlineComp }));
		
		Validator validator01 = new Validator(bomlineComp, "Purchase Level") {

			@Override
			public String validate() throws TCException {
				String val = bomLine.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL);
				String errorMsg = "";

				if (val != null && val.isEmpty()) {
					errorMsg = "Please fill Purchase Level Vinfast.";
				}
				return errorMsg;
			}
		};
		validators.add(validator01);
		
		
		for (int i = 0; i < validators.size(); i++) {
			String errorMsg = validators.get(i).validate();

			ValidationResult result = null;
			if (errorMsg.isEmpty() == false) {
				result = new ValidationResult(validators.get(i).getValidationName(), false, errorMsg);
			} else {
				String passImage = ValidationResult.getValidationPassImage();
				result = new ValidationResult(validators.get(i).getValidationName(), true, passImage);
			}
			results.add(result);
		}
		
		return results;
	}

}
