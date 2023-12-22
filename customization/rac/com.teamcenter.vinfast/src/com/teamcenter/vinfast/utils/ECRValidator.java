package com.teamcenter.vinfast.utils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTypeService;

public class ECRValidator implements IValidator {
	TCSession session;
	public ECRValidator() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	@Override
	public List<ValidationResult> validate(TCComponent bomlineComp) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCTypeService typeSrv = session.getTypeService();
		
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { bomlineComp }));
		
		Set<String> validatingBomAttrs = new LinkedHashSet<String>();
		String[] notEmptyProperties = session.getPreferenceService().getStringValues("VF_ECR_Validation_Not_Empty_BOM_Property");
		for (String notEmptyProperty : notEmptyProperties) {
			validatingBomAttrs.add(notEmptyProperty);
		}
		//validatingBomAttrs.add(VFBOMLINE.PUR_LEVEL_VF);
		
		for (String validatingBomAttr : validatingBomAttrs) {
			TCPropertyDescriptor propDesc = typeSrv.getTypeComponent("BOMLine").getPropDesc(validatingBomAttr);
			if (propDesc != null) {
				final String attrDisplayName = propDesc.getUiName();
				Validator validator = new Validator(bomlineComp, attrDisplayName) {

					@Override
					public String validate() throws TCException {
						String val = bomLine.getProperty(validatingBomAttr);
						String errorMsg = "";

						if (val != null && val.isEmpty()) {
							errorMsg = "Please fill " + attrDisplayName;
						}
						return errorMsg;
					}
				};
				validators.add(validator);
			}
		}
		
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
