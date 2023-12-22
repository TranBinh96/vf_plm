package com.teamcenter.vinfast.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sc.utilities.PropertyDefines;

import vfplm.soa.common.define.Constant;
import vfplm.soa.common.define.Constant.VFBOMLINE;
import vfplm.soa.common.define.Constant.VFItem;

public class ValidatorAFSMagna implements IValidator {
	TCSession session;

	public ValidatorAFSMagna() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator validator01 = new Validator(partBomLine, "Is After Sale Relevant") {
			@Override
			public String validate() throws TCException {
				String afsRelevant = item.getProperty(VFItem.ATTR_AFS_RELEVANT);
				String errorMsg = "";
				if (afsRelevant != null && (afsRelevant.trim().isEmpty() || (afsRelevant.compareToIgnoreCase("true") != 0 && afsRelevant.compareToIgnoreCase("TRUE") != 0))) {
					errorMsg = "Please fill part's Aftersales Relevant is TRUE";
				}
				return errorMsg;
			}
		};
		validators.add(validator01);

		Validator validator16 = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String makeBuy = item.getProperty(VFItem.ATTR_MAKE_BUY);
				String errorMsg = "";

				if (makeBuy != null && makeBuy.trim().isEmpty()) {
					errorMsg = "Please fill part's Make/Buy.";
				}
				return errorMsg;
			}
		};
		validators.add(validator16);

		Validator validator03 = new Validator(partBomLine, "UOM") {
			@Override
			public String validate() throws TCException {
				String uom = item.getProperty(VFItem.ATTR_UOM);
				String errorMsg = "";

				if (uom != null && uom.trim().isEmpty()) {
					errorMsg = "Please fill part's UOM.";
				}
				return errorMsg;
			}
		};
		validators.add(validator03);

		Validator purchaseLevelValidate = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				String purchaseLevel = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String[] purchaseLevelAvai = new String[] { PropertyDefines.PUR_LEVEL_D, PropertyDefines.PUR_LEVEL_DPT, PropertyDefines.PUR_LEVEL_DPS, PropertyDefines.PUR_LEVEL_S };

				if (purchaseLevel.isEmpty())
					return "Purchase Level is blank. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				else {
					if (!Arrays.asList(purchaseLevelAvai).contains(purchaseLevel))
						return "Purchase Level is not valid. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				}

				return "";
			}
		};
		validators.add(purchaseLevelValidate);

		Validator validator07 = new Validator(partBomLine, "Change Index") {
			@Override
			public String validate() throws TCException {
				String changeIndex = bomLine.getProperty(VFBOMLINE.ATTR_VF3_CHANGE_INDEX);
				String errorMsg = "";

				if (changeIndex != null && changeIndex.trim().isEmpty()) {
					errorMsg = "Please fill part line's Change Index.";
				}
				return errorMsg;
			}
		};
		validators.add(validator07);

		Validator valiAFSCritical = new Validator(partBomLine, "Aftersales Critical") {
			@Override
			public String validate() throws TCException {
				String afsCritical = bomLine.getProperty(VFBOMLINE.ATTR_AFS_CRITICAL);
				String errorMsg = "";

				if (afsCritical != null && afsCritical.trim().isEmpty()) {
					errorMsg = "Please fill part line's Aftersales Critical.";
				}
				return errorMsg;
			}
		};
		validators.add(valiAFSCritical);

		Validator moduleGrpVali = new Validator(partBomLine, "Module Group English") {
			@Override
			public String validate() throws TCException {
				String moduleGrp = bomLine.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_GROUP_ENGLISH);
				String errorMsg = "";
				if (moduleGrp != null && moduleGrp.trim().isEmpty()) {
					errorMsg = "Please fill part line's Module Group English.";
				}
				return errorMsg;
			}
		};
		validators.add(moduleGrpVali);

		Validator moduleName = new Validator(partBomLine, "Module Name") {
			@Override
			public String validate() throws TCException {
				String moduleName = bomLine.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_NAME);
				String errorMsg = "";
				if (moduleName != null && moduleName.trim().isEmpty()) {
					errorMsg = "Please fill part line's Module Name.";
				}
				return errorMsg;
			}
		};
		validators.add(moduleName);

		Validator mainModule = new Validator(partBomLine, "Main Module English") {
			@Override
			public String validate() throws TCException {
				String mainModule = bomLine.getProperty(Constant.VFBOMLINE.ATTR_SNS_MAIN_MODULE);
				String errorMsg = "";
				if (mainModule != null && mainModule.trim().isEmpty()) {
					errorMsg = "Please fill part line's Main Module English.";
				}
				return errorMsg;
			}
		};
		validators.add(mainModule);

		Validator validator19 = new Validator(partBomLine, "AFS Part Volume") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String errorMsg = "";
				String partVol = bomLine.getProperty("VF4_AFS_Part_Volumne");
				if (!partVol.isEmpty()) {
					try {
						double d = Double.parseDouble(partVol);
					} catch (NumberFormatException nfe) {
						errorMsg = "AFS Part Volume need to be number";
					}
				} else {
					errorMsg = "Please fill AFS Part Volume";
				}
				return errorMsg;
			}
		};
		validators.add(validator19);

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
