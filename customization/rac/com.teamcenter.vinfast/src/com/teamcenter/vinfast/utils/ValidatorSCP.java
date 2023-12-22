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
import vfplm.soa.common.define.Constant.VFDesign;

public class ValidatorSCP implements IValidator {
	TCSession session;

	public ValidatorSCP() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator validator01 = new Validator(partBomLine, "Q Checker") {
			@Override
			public String validate() throws TCException {
				String qChecker = revision.getProperty(Constant.VFDesign.ATTR_Q_CHECKER);
				String errorMsg = "";

				if (qChecker != null && qChecker.trim().toLowerCase().equals("yes") == false) {
					errorMsg = "Please fill part's Q-Checker as Yes.";
				}
				return errorMsg;
			}
		};
		validators.add(validator01);

		Validator validator02 = new Validator(partBomLine, "Description") {
			@Override
			public String validate() throws TCException {
				String desc = revision.getProperty(Constant.VFWorkspaceObject.DESCRIPTION);
				String errorMsg = "";

				if (desc != null && desc.trim().isEmpty()) {
					errorMsg = "Please fill part's Description.";
				}
				return errorMsg;
			}
		};
		validators.add(validator02);

		Validator validator04 = new Validator(partBomLine, "Make/Buy") {
			@Override
			public String validate() throws TCException {
				String partName = item.getProperty(Constant.VFItem.ATTR_MAKE_BUY);
				String errorMsg = "";

				if (partName != null && partName.trim().toLowerCase().equals("buy") == false) {
					errorMsg = "Part's Make/Buy is allowed only Buy for starting sourcing.";
				}
				return errorMsg;
			}
		};
		validators.add(validator04);

		Validator validator06 = new Validator(partBomLine, "Donor Vehicle") {
			@Override
			public String validate() throws TCException {
				String donorVeh = item.getProperty(Constant.VFDesign.ATTR_DONOR_VEHICLE);
				String errorMsg = "";

				if (donorVeh != null && donorVeh.trim().isEmpty()) {
					errorMsg = "Please fill part's Donor Vehicle.";
				}
				return errorMsg;
			}
		};
		validators.add(validator06);

		Validator validator09 = new Validator(partBomLine, "Long or Short Lead") {
			@Override
			public String validate() throws TCException {
				String longShortLead = revision.getProperty(Constant.VFDesign.ATTR_LONG_SHORT_LEAD);
				String errorMsg = "";

				if (longShortLead != null && longShortLead.trim().isEmpty()) {
					errorMsg = "Please fill part's Long or Short Lead.";
				}
				return errorMsg;
			}
		};
		validators.add(validator09);

		Validator validator10 = new Validator(partBomLine, "SOR Number") {
			@Override
			public String validate() throws TCException {
				String sorNumber = revision.getProperty(Constant.VFItem.ATTR_SOR_NUMBER);
				String errorMsg = "";

				if (sorNumber != null && sorNumber.trim().isEmpty()) {
					errorMsg = "Please fill part's SOR number.";
				}
				return errorMsg;
			}
		};
		validators.add(validator10);

		Validator validator11 = new Validator(partBomLine, "SOR Name") {
			@Override
			public String validate() throws TCException {
				String sorName = revision.getProperty(Constant.VFItem.ATTR_SOR_NAME);
				String errorMsg = "";

				if (sorName != null && sorName.trim().isEmpty()) {
					errorMsg = "Please fill part's SOR name.";
				}
				return errorMsg;
			}
		};
		validators.add(validator11);

		Validator validator12 = new Validator(partBomLine, "Release Date") {
			@Override
			public String validate() throws TCException {
				String sorRelDate = revision.getProperty(Constant.VFItem.ATTR_SOR_RELEASE_DATE);
				String errorMsg = "";

				if (sorRelDate != null && sorRelDate.trim().isEmpty()) {
					errorMsg = "Please fill part's SOR Release Date.";
				}
				return errorMsg;
			}
		};
		validators.add(validator12);

		Validator validator14 = new Validator(partBomLine, "Supplier Type") {
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
		validators.add(validator14);

//		Validator validator13 = new Validator(partBomLine, "Target Cost") {
//			@Override
//			public String validate() throws Exception {
//				CostUtils costUtil = new CostUtils(session, rev);
//				TCComponent targetCost = costUtil.getOrCreateOrSearchAndRelateTargetCost(false);
//
//				String field1 = "";
//				String field2 = "";
//				String field5 = "";
//
//				String errorMsg = "";
//				if (targetCost != null) {
//					field1 = targetCost.getProperty(Constant.VFCost.VFTargetCost.ATTR_TARGET_TOOLING_COST);
//					field2 = targetCost.getProperty(Constant.VFCost.VFTargetCost.ATTR_TARGET_PIECE_COST);
//					field5 = targetCost.getProperty(Constant.VFCost.VFTargetCost.ATTR_TARGET_EDD_COST);
//					
//					String supplierType = rev.getProperty(Constant.VFDesign.ATTR_SUPPLIER_TYPE);
//					boolean edndIsRequired = (supplierType == null || supplierType.trim().toLowerCase().equals("b2p") == false);
//					
//					if (edndIsRequired) {
//						if (field1.isEmpty() || field2.isEmpty() || field5.isEmpty()) {
//							errorMsg = "Please fill all required fields for Piece, Tooling and ED&D costs.";
//						}	
//					} else {
//						if (field1.isEmpty() || field2.isEmpty()) {
//							errorMsg = "Please fill all required fields for Piece, Tooling costs.";
//						}
//					}
//					
//					if (errorMsg.isEmpty()) {
//						double targetPieceCost = targetCost.getDoubleProperty(Constant.VFCost.VFTargetCost.ATTR_TARGET_PIECE_COST);
//						if (targetPieceCost <= 0) {
//							errorMsg = "Target piece cost need to be greater than 0.";
//						}
//					}
//				} else {
//					throw new CannotFoundTargetCostException("Cannot found target cost on part \""+rev.getProperty("object_string")+"\"");
//				}
//				
//				return errorMsg;
//			}
//		};
//		validators.add(validator13);

		Validator validator05 = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				String purchaseLevel = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String[] purchaseLevelAvai = new String[] { PropertyDefines.PUR_LEVEL_P, PropertyDefines.PUR_LEVEL_AXS, PropertyDefines.PUR_LEVEL_DPT, PropertyDefines.PUR_LEVEL_DPS, PropertyDefines.PUR_LEVEL_BL };

				if (purchaseLevel.isEmpty())
					return "Purchase Level is blank. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				else {
					if (!Arrays.asList(purchaseLevelAvai).contains(purchaseLevel))
						return "Purchase Level is not valid. Please update, valid values: " + String.join(", ", purchaseLevelAvai);
				}
				return "";
			}
		};
		validators.add(validator05);

		Validator validator15 = new Validator(partBomLine, "S Release") {
			@Override
			public String validate() throws TCException {
				String releaseStatuses = revision.getProperty(Constant.VFWorkspaceObject.ATTR_RELEASE_STATUS_LIST);
				String errorMsg = "";
				if (releaseStatuses != null && releaseStatuses.contains("Sourcing")) {
					errorMsg = "Part completed Starting Sourcing process already.";
				}
				return errorMsg;
			}
		};
		validators.add(validator15);

//		Validator validator16 = new Validator(partBomLine, "Material Type") {
//			@Override
//			public String validate() throws TCException {
//				String supplierType = rev.getProperty("vl5_material_type");
//				String errorMsg = "";
//
//				if (supplierType != null && supplierType.trim().isEmpty()) {
//					errorMsg = "Please fill part's Material Type.";
//				}
//				return errorMsg;
//			}
//		};
//		validators.add(validator16);

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
