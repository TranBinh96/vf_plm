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

public class ValidatorEScooter implements IValidator {
	TCSession session;

	public ValidatorEScooter() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator groupVehicleValidate = new Validator(partBomLine, "Group Vehicle") {
			@Override
			public String validate() throws TCException {
				String property = "VF4_LNT_es_group_veh";
				String grpVehicle = bomLine.getProperty(property);
				String propDisplayName = bomLine.getTypeComponent().getPropertyDescriptor(property).getDisplayName();
				if (grpVehicle != null && grpVehicle.trim().isEmpty())
					return propDisplayName + " is blank. Please update.";

				return "";
			}
		};
		validators.add(groupVehicleValidate);

		Validator vehicleLineValidate = new Validator(partBomLine, "EScooter Model/Vehicle Line") {
			@Override
			public String validate() throws TCException {
				try {
					String property = "vf4_es_model_veh_line_cp";
					String vehicleLine = revision.getProperty(property);
					String propDisplayName = revision.getTypeComponent().getPropertyDescriptor(property).getDisplayName();
					if (vehicleLine != null && vehicleLine.trim().isEmpty())
						return propDisplayName + " is blank. Please update.";
				} catch (Exception e) {
					e.printStackTrace();
				}

				return "";
			}
		};
		validators.add(vehicleLineValidate);

		Validator makeBuyValidate = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String property = "vf4_item_make_buy";
				String makeBuy = item.getProperty(property);
				String propDisplayName = item.getTypeComponent().getPropertyDescriptor(property).getDisplayName();
				if (makeBuy != null && makeBuy.trim().isEmpty())
					return propDisplayName + " is blank. Please update.";

				return "";
			}
		};
		validators.add(makeBuyValidate);

		Validator longShortLeadValidate = new Validator(partBomLine, "Long or Short Lead") {
			@Override
			public String validate() throws TCException {
				String longOrShortLead = revision.getProperty("vf4_long_short_lead");
				if (longOrShortLead.isEmpty()) {
					return "Long or Short Lead is blank. Please update.";
				}

				return "";
			}
		};
		validators.add(longShortLeadValidate);

		Validator supplierTypeValidate = new Validator(partBomLine, "Supplier Type") {
			@Override
			public String validate() throws TCException {
				String supplierType = revision.getProperty("vf3_supplier_type");
				if (supplierType != null && supplierType.trim().isEmpty()) {
					return "Supplier Type is blank. Please update.";
				}

				return "";
			}
		};
		validators.add(supplierTypeValidate);

		Validator purchaseLevelValidate = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				String purchaseLevel = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String[] purchaseLevelAvai = new String[] { PropertyDefines.PUR_LEVEL_P, PropertyDefines.PUR_LEVEL_AXS, PropertyDefines.PUR_LEVEL_DPT, PropertyDefines.PUR_LEVEL_DPS, PropertyDefines.PUR_LEVEL_BL, PropertyDefines.PUR_LEVEL_H, PropertyDefines.PUR_LEVEL_D };

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

		Validator sReleaseValidate = new Validator(partBomLine, "S Release") {
			@Override
			public String validate() throws TCException {
				String releaseStatuses = revision.getProperty(VFWorkspaceObject.ATTR_RELEASE_STATUS_LIST);
				if (releaseStatuses != null && releaseStatuses.contains("Sourcing") && !(releaseStatuses.contains("Pre") && releaseStatuses.contains("Sourcing"))) {
					return "Part completed Starting Sourcing process already.";
				}

				return "";
			}
		};
		validators.add(sReleaseValidate);

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
