package com.teamcenter.vinfast.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public abstract class Validator {
	public TCComponentBOMLine bomLine;
	public TCComponentItem item;
	public TCComponentItemRevision revision;
	public String validationName;

	public Validator(TCComponent targetItem, String validationName) throws TCException {
		if (targetItem instanceof TCComponentItemRevision) {
			this.bomLine = null;
			this.item = ((TCComponentItemRevision) targetItem).getItem();
			this.revision = (TCComponentItemRevision) targetItem;
			this.validationName = validationName;
		} else {
			this.bomLine = (TCComponentBOMLine) targetItem;
			this.item = ((TCComponentBOMLine) targetItem).getItem();
			this.revision = ((TCComponentBOMLine) targetItem).getItemRevision();
			this.validationName = validationName;
		}
	}

	public String getValidationName() {
		return this.validationName;
	}

	public abstract String validate() throws TCException, Exception;
}
