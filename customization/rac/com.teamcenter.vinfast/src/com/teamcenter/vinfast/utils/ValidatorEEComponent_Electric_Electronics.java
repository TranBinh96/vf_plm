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

public class ValidatorEEComponent_Electric_Electronics implements IValidator {
	TCSession session;

	public ValidatorEEComponent_Electric_Electronics() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public List<ValidationResult> validate(TCComponent partBomLine) throws Exception {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<Validator> validators = new LinkedList<Validator>();
		TCComponentType.refresh(Arrays.asList(new TCComponent[] { partBomLine }));

		Validator descriptionVal = new Validator(partBomLine, "Description") {
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
		validators.add(descriptionVal);

		Validator makeBuyVal = new Validator(partBomLine, "Make/Buy") {
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
		validators.add(makeBuyVal);

		Validator purchaseLevelVal = new Validator(partBomLine, "Purchase Level") {
			@Override
			public String validate() throws TCException {
				String purchaseLvl = bomLine.getProperty(VFBOMLINE.PUR_LEVEL_VF);
				String errorMsg = "";

				if (purchaseLvl != null && purchaseLvl.trim().isEmpty()) {
					errorMsg = "Please fill part line's Purchase Level Vinfast.";
				}
				return errorMsg;
			}
		};
		validators.add(purchaseLevelVal);

		Validator sorNumberVal = new Validator(partBomLine, "SOR Number") {
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
		validators.add(sorNumberVal);

		Validator sorNameVal = new Validator(partBomLine, "SOR Name") {
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
		validators.add(sorNameVal);

		Validator releaseDateVal = new Validator(partBomLine, "Release Date") {
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
		validators.add(releaseDateVal);

		Validator sReleaseVal = new Validator(partBomLine, "S Release") {
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
		validators.add(sReleaseVal);

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
