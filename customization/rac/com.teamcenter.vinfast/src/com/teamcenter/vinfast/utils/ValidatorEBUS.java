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
import vfplm.soa.common.define.Constant.VFDesign;
import vfplm.soa.common.define.Constant.VFItem;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class ValidatorEBUS implements IValidator {
	TCSession session;

	public ValidatorEBUS() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator validator01 = new Validator(partBomLine, "Q Checker") {

			@Override
			public String validate() throws TCException {
				String qChecker = revision.getProperty(VFDesign.ATTR_Q_CHECKER);
				String errorMsg = "";

				if (qChecker != null && qChecker.trim().toLowerCase().equals("yes") == false) {
					errorMsg = "Please fill part's Q-Checker as Yes.";
				}
				return errorMsg;
			}
		};
		validators.add(validator01);

		Validator validator03 = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String makeBuy = item.getProperty(VFItem.ATTR_MAKE_BUY);
				String errorMsg = "";

				if (makeBuy != null && makeBuy.trim().isEmpty()) {
					errorMsg = "Please fill part's  Make/Buy.";
				}
				return errorMsg;
			}
		};
		validators.add(validator03);

		Validator validator04 = new Validator(partBomLine, "Donor Vehicle") {
			@Override
			public String validate() throws TCException {
				String donorVeh = item.getProperty(VFDesign.ATTR_DONOR_VEHICLE);
				String errorMsg = "";

				if (donorVeh != null && donorVeh.trim().isEmpty()) {
					errorMsg = "Please fill part's Donor Vehicle.";
				}
				return errorMsg;
			}
		};
		validators.add(validator04);

		Validator validator05 = new Validator(partBomLine, "EBUS Vehicle Type") {
			@Override
			public String validate() throws TCException {
				String vehType = revision.getProperty("vf4_ebus_veh_type");
				String errorMsg = "";

				if (vehType != null && vehType.trim().isEmpty()) {
					errorMsg = "Please fill part's EBUS Vehicle Type.";
				}
				return errorMsg;
			}
		};
		validators.add(validator05);

		Validator validator06 = new Validator(partBomLine, "Long or Short Lead") {
			@Override
			public String validate() throws TCException {
				String longShortLead = item.getProperty(VFDesign.ATTR_LONG_SHORT_LEAD);
				String errorMsg = "";

				if (longShortLead != null && longShortLead.trim().isEmpty()) {
					errorMsg = "Please fill part's Long or Short Lead.";
				}
				return errorMsg;
			}
		};
		validators.add(validator06);

		Validator validator07 = new Validator(partBomLine, "Supplier Type") {
			@Override
			public String validate() throws TCException {
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				String purchaseLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String errorMsg = "";

				if (supplierType != null && supplierType.trim().isEmpty()) {
					errorMsg = "Please fill part's Supplier Type.";
				} else if (purchaseLvl.compareToIgnoreCase("H") == 0 && supplierType.compareToIgnoreCase("NONE") != 0) {
					errorMsg = "Supplier Type must be NONE with H part";
				}
				return errorMsg;
			}
		};
		validators.add(validator07);

		Validator validator10 = new Validator(partBomLine, "Purchase Level") {
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
		validators.add(validator10);

		Validator moduleGrpVali = new Validator(partBomLine, "Module Group English") {
			@Override
			public String validate() throws TCException {
				String moduleGrp = bomLine.getProperty(VFBOMLINE.MODULE_GROUP_ENGLISH);
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
				String moduleName = bomLine.getProperty(VFBOMLINE.MODULE_ENGLISH);
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
				String mainModule = bomLine.getProperty(VFBOMLINE.MAIN_MODULE_ENGLISH);
				String errorMsg = "";
				if (mainModule != null && mainModule.trim().isEmpty()) {
					errorMsg = "Please fill part line's Main Module English.";
				}
				return errorMsg;
			}
		};
		validators.add(mainModule);

		Validator validator12 = new Validator(partBomLine, "SOR Number") {

			@Override
			public String validate() throws TCException {
				String sorNumber = revision.getProperty(VFItem.ATTR_SOR_NUMBER);
				String errorMsg = "";

				if (sorNumber != null && !sorNumber.trim().isEmpty()) {
					errorMsg = "Please remove part's SOR Number.";
				}
				return errorMsg;
			}
		};
		validators.add(validator12);

		Validator validator13 = new Validator(partBomLine, "SOR Name") {

			@Override
			public String validate() throws TCException {
				String sorName = revision.getProperty(VFItem.ATTR_SOR_NAME);
				String errorMsg = "";

				if (sorName != null && !sorName.trim().isEmpty()) {
					errorMsg = "Please remove part's SOR name.";
				}
				return errorMsg;
			}
		};
		validators.add(validator13);

		Validator validator14 = new Validator(partBomLine, "Release Date") {

			@Override
			public String validate() throws TCException {
				String sorRelDate = revision.getProperty(VFItem.ATTR_SOR_RELEASE_DATE);
				String errorMsg = "";

				if (sorRelDate != null && !sorRelDate.trim().isEmpty()) {
					errorMsg = "Please remove part's SOR Release Date.";
				}
				return errorMsg;
			}
		};
		validators.add(validator14);

		Validator validator15 = new Validator(partBomLine, "S Release") {

			@Override
			public String validate() throws TCException, NotLoadedException {
				String errorMsg = "";
				for (int i = 0; i < item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST).length; i++) {
					String releaseStatuses = item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST)[i].getProperty(VFWorkspaceObject.ATTR_RELEASE_STATUS_LIST);
					if (releaseStatuses != null && releaseStatuses.contains("Sourcing")) {
						errorMsg = "Part completed Sourcing with revision " + item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST)[i].getPropertyDisplayableValue(VFWorkspaceObject.ITEM_REVISION_ID);
						break;
					}
				}
				return errorMsg;
			}
		};
		validators.add(validator15);

		Validator validator17 = new Validator(partBomLine, "CAD Weight") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String errorMsg = "";
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0) {
					String weight = bomLine.getProperty("bl_rev_vf4_cad_weight");
					if (weight.isEmpty()) {
						errorMsg = "Please fill CAD Weight";
					}
				}
				return errorMsg;
			}
		};
		validators.add(validator17);

		Validator validator18 = new Validator(partBomLine, "Material") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String errorMsg = "";
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0) {
					String material = bomLine.getProperty(VFBOMLINE.ATTR_CAD_MATERIAL);
					if (material.isEmpty()) {
						errorMsg = "Please fill Material";
					}
				}
				return errorMsg;
			}
		};
		validators.add(validator18);

		Validator cadThicknessValidator = new Validator(partBomLine, "CAD Thickness") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String errorMsg = "";
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String purchaseLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0 && purchaseLvl.compareToIgnoreCase("H") == 0) {
					String cadThickness = bomLine.getProperty("bl_rev_vf4_cad_thickness");
					if (cadThickness.isEmpty()) {
						errorMsg = "Please fill CAD Thickness";
					}
				}
				return errorMsg;
			}
		};
		validators.add(cadThicknessValidator);

		for (int i = 0; i < validators.size(); i++) {
			String errorMsg = validators.get(i).validate();

			ValidationResult result = null;
			if (errorMsg.isEmpty() == false && errorMsg.compareToIgnoreCase("NOT REQUIRED") != 0) {
				result = new ValidationResult(validators.get(i).getValidationName(), false, errorMsg);
			} else if (errorMsg.compareToIgnoreCase("NOT REQUIRED") == 0) {
				validators.remove(i);
				i--;
				continue;
			} else {
				String passImage = ValidationResult.getValidationPassImage();
				result = new ValidationResult(validators.get(i).getValidationName(), true, passImage);
			}
			results.add(result);
			if (result.getValidationName().equals("S Release") && result.isPassed() == true) {
				result.setShown(false);
			}
		}
		return results;
	}
}
