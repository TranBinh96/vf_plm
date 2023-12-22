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
import vfplm.soa.common.define.Constant.VFCost.VFActualPieceCost;
import vfplm.soa.common.define.Constant.VFDesign;
import vfplm.soa.common.define.Constant.VFItem;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class ValidatorCar implements IValidator {
	TCSession session;

	public ValidatorCar() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator qCheckerValidator = new Validator(partBomLine, "Q Checker") {
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
		validators.add(qCheckerValidator);

		Validator descriptionValidator = new Validator(partBomLine, "Description") {
			@Override
			public String validate() throws TCException {
				String desc = revision.getProperty(VFWorkspaceObject.DESCRIPTION);
				String errorMsg = "";

				if (desc != null && desc.trim().isEmpty()) {
					errorMsg = "Please fill part's Description.";
				}
				return errorMsg;
			}
		};
		validators.add(descriptionValidator);

		Validator partMakeBuyValidator = new Validator(partBomLine, "Make/Buy") {
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
		validators.add(partMakeBuyValidator);

		Validator donorVehicleValidator = new Validator(partBomLine, "Donor Vehicle") {
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
		validators.add(donorVehicleValidator);

		Validator dsuvVehicleTypeValidator = new Validator(partBomLine, "DSUV Vehicle Type") {
			@Override
			public String validate() throws TCException {
				String vehType = revision.getProperty(VFDesign.ATTR_CUV_VEHICLE_TYPE);
				String errorMsg = "";

				if (vehType != null && vehType.trim().isEmpty()) {
					errorMsg = "Please fill part's CUV Vehicle Type.";
				}
				return errorMsg;
			}
		};
		validators.add(dsuvVehicleTypeValidator);

		Validator longShortValidator = new Validator(partBomLine, "Long or Short Lead") {
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
		validators.add(longShortValidator);

		Validator supplierTypeValidator = new Validator(partBomLine, "Supplier Type") {
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
		validators.add(supplierTypeValidator);

		Validator pieceCostValidator = new Validator(partBomLine, "Actual Piece Cost") {
			@Override
			public String validate() throws Exception {
				String errorMsg = "";
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				if (bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("H") == 0 && bomView.isEmpty()) {
					CostUtils costUtil = new CostUtils(session, revision);
					TCComponent pieceCost = costUtil.getOrCreateOrSearchAndRelatePieceCost(false);

					String totalPieceCost = "";
					String currency = "";
					String laborCost = "";
					if (pieceCost != null) {
						totalPieceCost = pieceCost.getProperty(VFActualPieceCost.PRD_TOTAL_COST);
						laborCost = pieceCost.getProperty(VFActualPieceCost.PRD_MISCELLANEOUS_COST_VALUE);
						currency = pieceCost.getProperty(VFActualPieceCost.PRD_PIECE_COST_CURRENCY_STATUS);

						if (currency.isEmpty()) {
							errorMsg = "Please fill in the piece cost";
							return errorMsg;
						} else if (currency.compareToIgnoreCase("NO COST") == 0) {
							if (Double.valueOf(totalPieceCost) > 0) {
								errorMsg = "Piece Cost Currency Status must be different NO COST";
								return errorMsg;
							}
						} else {
							if (Double.valueOf(totalPieceCost) <= 0 && laborCost.isEmpty()) {
								errorMsg = "Please fill in the piece cost";
								return errorMsg;
							}
						}
					} else {
						return "Cannot found actual piece cost on part \"" + revision.getProperty("object_string") + "\"";
					}
				} else {
					return "NOT REQUIRED";
				}
				return errorMsg;
			}
		};
		validators.add(pieceCostValidator);

		Validator purchaseLevelValidator = new Validator(partBomLine, "Purchase Level") {
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
		validators.add(purchaseLevelValidator);

		Validator moduleGroupValidator = new Validator(partBomLine, "Module Group English") {
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
		validators.add(moduleGroupValidator);

		Validator moduleNameValidator = new Validator(partBomLine, "Module Name") {
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
		validators.add(moduleNameValidator);

		Validator mainModuleValidator = new Validator(partBomLine, "Main Module English") {
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
		validators.add(mainModuleValidator);

		Validator sorNumberValidator = new Validator(partBomLine, "SOR Number") {
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
		validators.add(sorNumberValidator);

		Validator sorNameValidator = new Validator(partBomLine, "SOR Name") {
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
		validators.add(sorNameValidator);

		Validator releaseDateValidator = new Validator(partBomLine, "Release Date") {
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
		validators.add(releaseDateValidator);

		Validator sReleaseValidator = new Validator(partBomLine, "S Release") {
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
		validators.add(sReleaseValidator);

		Validator cadWeightValidator = new Validator(partBomLine, "CAD Weight") {
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
		validators.add(cadWeightValidator);

		Validator materialValidator = new Validator(partBomLine, "Material") {
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
		validators.add(materialValidator);

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
				String passImage = ValidationResult.passImage;
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
