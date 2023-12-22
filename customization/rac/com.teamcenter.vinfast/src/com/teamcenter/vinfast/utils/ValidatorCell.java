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

public class ValidatorCell implements IValidator {
	TCSession session;

	public ValidatorCell() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator validator02 = new Validator(partBomLine, "Description") {
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
		validators.add(validator02);

		Validator validator03 = new Validator(partBomLine, "Make/Buy") {
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
		validators.add(validator03);

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
				} else if (purchaseLvl.compareToIgnoreCase("H") == 0 && supplierType.compareToIgnoreCase("B2P") != 0) {
					errorMsg = "Supplier Type must be B2P with H part";
				}
				return errorMsg;
			}
		};
		validators.add(validator07);

		Validator validator09 = new Validator(partBomLine, "Actual Piece Cost") {
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
						throw new CannotFoundTargetCostException("Cannot found actual piece cost on part \"" + revision.getProperty("object_string") + "\"");
					}
				} else {
					return "NOT REQUIRED";
				}
				return errorMsg;
			}
		};
		validators.add(validator09);

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
