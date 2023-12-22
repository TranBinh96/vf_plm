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

import vfplm.soa.common.define.Constant.VFBOMLINE;
import vfplm.soa.common.define.Constant.VFCost.VFActualPieceCost;
import vfplm.soa.common.define.Constant.VFDesign;
import vfplm.soa.common.define.Constant.VFItem;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class ValidatorBatteryCar implements IValidator {
	TCSession session;

	public ValidatorBatteryCar() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator qCheckerValidator = new Validator(partBomLine, "Q Checker") {
			@Override
			public String validate() throws TCException {
				String partName = revision.getProperty(VFDesign.ATTR_Q_CHECKER);

				if (partName != null && partName.trim().toLowerCase().equals("yes") == false) {
					return "Please fill part's Q-Checker as Yes.";
				}
				return "";
			}
		};
		validators.add(qCheckerValidator);

		Validator descriptionValidator = new Validator(partBomLine, "Description") {
			@Override
			public String validate() throws TCException {
				String partName = revision.getProperty(VFWorkspaceObject.DESCRIPTION);

				if (partName != null && partName.trim().isEmpty()) {
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

				if (makeBuy != null && makeBuy.trim().toLowerCase().equals("buy") == false && makeBuy.trim().toLowerCase().equals("make") == false) {
					return "Part's Make/Buy is allowed only Buy for starting sourcing.";
				}
				return "";
			}
		};
		validators.add(makeBuyValidator);

		Validator donorVehicleValidator = new Validator(partBomLine, "Donor Vehicle") {
			@Override
			public String validate() throws TCException {
				String donorVeh = item.getProperty(VFDesign.ATTR_DONOR_VEHICLE);

				if (donorVeh != null && donorVeh.trim().isEmpty()) {
					return "Please fill part's Donor Vehicle.";
				}
				return "";
			}
		};
		validators.add(donorVehicleValidator);

		Validator longShortLeadValidator = new Validator(partBomLine, "Long or Short Lead") {
			@Override
			public String validate() throws TCException {
				String longShortLead = item.getProperty(VFDesign.ATTR_LONG_SHORT_LEAD);

				if (longShortLead != null && longShortLead.trim().isEmpty()) {
					return "Please fill part's Long or Short Lead.";
				}
				return "";
			}
		};
		validators.add(longShortLeadValidator);

		Validator sorNumberValidator = new Validator(partBomLine, "SOR Number") {
			@Override
			public String validate() throws TCException {
				String sorNumber = revision.getProperty(VFItem.ATTR_SOR_NUMBER);

				if (sorNumber != null && !sorNumber.trim().isEmpty()) {
					return "Please remove part's SOR Number.";
				}
				return "";
			}
		};
		validators.add(sorNumberValidator);

		Validator sorNameValidator = new Validator(partBomLine, "SOR Name") {
			@Override
			public String validate() throws TCException {
				String sorName = revision.getProperty(VFItem.ATTR_SOR_NAME);

				if (sorName != null && !sorName.trim().isEmpty()) {
					return "Please remove part's SOR name.";
				}
				return "";
			}
		};
		validators.add(sorNameValidator);

		Validator releaseDateValidator = new Validator(partBomLine, "Release Date") {
			@Override
			public String validate() throws TCException {
				String sorRelDate = revision.getProperty(VFItem.ATTR_SOR_RELEASE_DATE);

				if (sorRelDate != null && !sorRelDate.trim().isEmpty()) {
					return "Please remove part's SOR Release Date.";
				}
				return "";
			}
		};
		validators.add(releaseDateValidator);

		Validator supplierTypeValidator = new Validator(partBomLine, "Supplier Type") {
			@Override
			public String validate() throws TCException {
				if (bomLine == null)
					return "";

				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				String purchaseLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);

				if (supplierType != null && supplierType.trim().isEmpty()) {
					return "Please fill part's Supplier Type.";
				} else if (purchaseLvl.compareToIgnoreCase("H") == 0 && supplierType.compareToIgnoreCase("B2P") != 0) {
					return "Supplier Type must be B2P with H part";
				}
				return "";
			}
		};
		validators.add(supplierTypeValidator);

		Validator pieceCostValidator = new Validator(partBomLine, "Actual Piece Cost") {
			@Override
			public String validate() throws Exception {
				if (bomLine == null)
					return "";

				if (bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("H") == 0) {
					CostUtils costUtil = new CostUtils(session, revision);
					TCComponent pieceCost = costUtil.getOrCreateOrSearchAndRelatePieceCost(false);

					String totalPieceCost = "";
					String currency = "";

					if (pieceCost != null) {
						totalPieceCost = pieceCost.getProperty(VFActualPieceCost.PRD_TOTAL_COST);
						currency = pieceCost.getProperty(VFActualPieceCost.PRD_PIECE_COST_CURRENCY_STATUS);

						if (currency.isEmpty()) {
							return "Please fill in the piece cost";
						} else if (currency.compareToIgnoreCase("NO COST") == 0) {
							if (Double.valueOf(totalPieceCost) > 0) {
								return "Piece Cost Currency Status must be different NO COST";
							}
						} else {
							if (Double.valueOf(totalPieceCost) <= 0) {
								return "Please fill in the piece cost";
							}
						}

					} else {
						throw new CannotFoundTargetCostException("Cannot found actual piece cost on part \"" + revision.getProperty("object_string") + "\"");
					}
				}
				return "";
			}
		};
		validators.add(pieceCostValidator);

		Validator purchaseLevelValidator = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				if (bomLine == null)
					return "";

				String purLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);

				if (purLvl != null && purLvl.trim().isEmpty()) {
					return "Please fill part line's Purchase Level Vinfast.";
				}
				return "";
			}
		};
		validators.add(purchaseLevelValidator);

		Validator sReleaseValidator = new Validator(partBomLine, "S Release") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				for (int i = 0; i < item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST).length; i++) {
					String releaseStatuses = item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST)[i].getProperty(VFWorkspaceObject.ATTR_RELEASE_STATUS_LIST);
					if (releaseStatuses != null && releaseStatuses.contains("Sourcing")) {
						return "Part completed Sourcing with revision " + item.getRelatedComponents(VFWorkspaceObject.ATTR_REVISION_LIST)[i].getPropertyDisplayableValue(VFWorkspaceObject.ITEM_REVISION_ID);
					}
				}
				return "";
			}
		};
		validators.add(sReleaseValidator);

		Validator cadWeightValidator = new Validator(partBomLine, "CAD Weight") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0) {
					String weight = bomLine.getProperty("bl_rev_vf4_cad_weight");
					if (weight.isEmpty()) {
						return "Please fill CAD Weight";
					}
				}
				return "";
			}
		};
		validators.add(cadWeightValidator);

		Validator materialValidator = new Validator(partBomLine, "Material") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0) {
					String material = bomLine.getProperty(VFBOMLINE.ATTR_CAD_MATERIAL);
					if (material.isEmpty()) {
						return "Please fill Material";
					}
				}
				return "";
			}
		};
		validators.add(materialValidator);

		Validator cadThicknessValidator = new Validator(partBomLine, "CAD Thickness") {
			@Override
			public String validate() throws TCException, NotLoadedException {
				if (bomLine == null)
					return "";

				String bomView = revision.getProperty(VFBOMLINE.ATTR_STRUCTURE_REVISIONS);
				String purchaseLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String supplierType = item.getProperty(VFDesign.ATTR_SUPPLIER_TYPE);
				if (bomView.isEmpty() && supplierType.compareTo("B2P") == 0 && purchaseLvl.compareToIgnoreCase("H") == 0) {
					String cadThickness = bomLine.getProperty("bl_rev_vf4_cad_thickness");
					if (cadThickness.isEmpty()) {
						return "Please fill CAD Thickness";
					}
				}
				return "";
			}
		};
		validators.add(cadThicknessValidator);

		Validator moduleGroupEnglishValidator = new Validator(partBomLine, "Module Group English") {
			@Override
			public String validate() throws TCException {
				if (bomLine == null)
					return "";
				
				String moduleGrp = bomLine.getProperty(VFBOMLINE.MODULE_GROUP_ENGLISH);
				if (moduleGrp != null && moduleGrp.trim().isEmpty()) {
					return "Please fill part line's Module Group English.";
				}
				return "";
			}
		};
		validators.add(moduleGroupEnglishValidator);

		Validator moduleNameValidator = new Validator(partBomLine, "Module Name") {
			@Override
			public String validate() throws TCException {
				if (bomLine == null)
					return "";
				
				String moduleName = bomLine.getProperty(VFBOMLINE.MODULE_ENGLISH);
				if (moduleName != null && moduleName.trim().isEmpty()) {
					return "Please fill part line's Module Name.";
				}
				return "";
			}
		};
		validators.add(moduleNameValidator);

		Validator mainModuleEnglishValidator = new Validator(partBomLine, "Main Module English") {
			@Override
			public String validate() throws TCException {
				if (bomLine == null)
					return "";
				
				String mainModule = bomLine.getProperty(VFBOMLINE.MAIN_MODULE_ENGLISH);
				if (mainModule != null && mainModule.trim().isEmpty()) {
					return "Please fill part line's Main Module English.";
				}
				return "";
			}
		};
		validators.add(mainModuleEnglishValidator);

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
			if (result.getValidationName().equals("S Release") && result.isPassed() == true) {
				result.setShown(false);
			}
			results.add(result);
		}
		return results;
	}
}
