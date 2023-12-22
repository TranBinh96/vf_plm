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

import vfplm.soa.common.define.Constant.VFBOMLINE;
import vfplm.soa.common.define.Constant.VFItem;

public class ValidatorAFS implements IValidator {
	TCSession session;

	public ValidatorAFS() {
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
				if (afsRelevant != null && (afsRelevant.trim().isEmpty() || (afsRelevant.compareToIgnoreCase("true") != 0 && afsRelevant.compareToIgnoreCase("TRUE") != 0))) {
					return "Please fill part's Aftersales Relevant is TRUE";
				}
				return "";
			}
		};
		validators.add(validator01);

		Validator makeBuyValidate = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String makeBuy = item.getProperty(VFItem.ATTR_MAKE_BUY);

				if (makeBuy != null && makeBuy.trim().isEmpty()) {
					return "Please fill part's Make/Buy.";
				}
				return "";
			}
		};
		validators.add(makeBuyValidate);

		Validator uomValidate = new Validator(partBomLine, "UOM") {
			@Override
			public String validate() throws TCException {
				String uom = item.getProperty(VFItem.ATTR_UOM);

				if (uom != null && uom.trim().isEmpty()) {
					return "Please fill part's UOM.";
				}
				return "";
			}
		};
		validators.add(uomValidate);

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

		Validator afterSaleCriticalValidate = new Validator(partBomLine, "Aftersales Critical") {
			@Override
			public String validate() throws TCException {
				String afsCritical = bomLine.getProperty(VFBOMLINE.ATTR_AFS_CRITICAL);
				if (afsCritical != null && afsCritical.trim().isEmpty()) {
					return "Please fill part line's Aftersales Critical.";
				}
				return "";
			}
		};
		validators.add(afterSaleCriticalValidate);

		Validator moduleGroupEnglishValidate = new Validator(partBomLine, "Module Group English") {
			@Override
			public String validate() throws TCException {
				String moduleGrp = bomLine.getProperty(VFBOMLINE.MODULE_GROUP_ENGLISH);
				if (moduleGrp != null && moduleGrp.trim().isEmpty()) {
					return "Please fill part line's Module Group English.";
				}
				return "";
			}
		};
		validators.add(moduleGroupEnglishValidate);

		Validator moduleNameValidate = new Validator(partBomLine, "Module Name") {
			@Override
			public String validate() throws TCException {
				String moduleName = bomLine.getProperty(VFBOMLINE.MODULE_ENGLISH);
				if (moduleName != null && moduleName.trim().isEmpty()) {
					return "Please fill part line's Module Name.";
				}
				return "";
			}
		};
		validators.add(moduleNameValidate);

		Validator mainModuleEnglishValidate = new Validator(partBomLine, "Main Module English") {
			@Override
			public String validate() throws TCException {
				String mainModule = bomLine.getProperty(VFBOMLINE.MAIN_MODULE_ENGLISH);
				if (mainModule != null && mainModule.trim().isEmpty()) {
					return "Please fill part line's Main Module English.";
				}
				return "";
			}
		};
		validators.add(mainModuleEnglishValidate);

		Validator afsPartVolumeValidate = new Validator(partBomLine, "AFS Part Volume") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String partVol = bomLine.getProperty("VF4_AFS_Part_Volumne");
				if (!partVol.isEmpty()) {
					try {
						Double.parseDouble(partVol);
					} catch (NumberFormatException nfe) {
						return "AFS Part Volume need to be number";
					}
				} else {
					return "Please fill AFS Part Volume";
				}
				return "";
			}
		};
		validators.add(afsPartVolumeValidate);

		for (Validator validator : validators) {
			String errorMsg = validator.validate();

			ValidationResult result = null;
			if (errorMsg.isEmpty() == false) {
				result = new ValidationResult(validator.getValidationName(), false, errorMsg);
			} else {
				String passImage = ValidationResult.passImage;
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
