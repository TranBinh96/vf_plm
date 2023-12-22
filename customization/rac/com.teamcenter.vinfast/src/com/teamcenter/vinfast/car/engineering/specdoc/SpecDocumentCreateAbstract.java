package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public abstract class SpecDocumentCreateAbstract extends Composite {
	protected Text txtID;
	protected Text txtName;
	protected Text txtDescription;
	protected Combo cbModelCode;
	protected Combo cbModuleName;
	protected Combo cbDomain;
	protected Combo cbModule;
	protected Combo cbWorkProduct;
	protected Button btnCreate;
	protected Text txtAddingNumber;
	protected Button btnAddingRemove;
	protected Button btnAddingSearch;
	protected DateTime datTargetReleaseDate;
	protected DateTime datTargetReviseDate;
	protected Button ckbTargetReviseDate;
	protected TCSession session;

	protected LinkedHashMap<String, String> objectTypeMapping;
	protected LinkedHashMap<String, String> prefixNameDataForm;
	protected LinkedHashMap<String, String> modelCodeDataForm;
	protected LinkedHashMap<String, String> moduleNameDataForm;

	protected String prefixName;
	protected String objectType;
	protected TCComponent specbook = null;
	protected Calendar targetReleaseDate = null;

	public SpecDocumentCreateAbstract(Composite c) {
		super(c, SWT.NONE);
	}

	public void setSession(TCSession session) {
		this.session = session;
	}

	public LinkedHashMap<String, String> getObjectTypeMapping() {
		return objectTypeMapping;
	}

	public void setObjectTypeMapping(LinkedHashMap<String, String> objectTypeMapping) {
		this.objectTypeMapping = objectTypeMapping;
	}

	public LinkedHashMap<String, String> getPrefixNameDataForm() {
		return prefixNameDataForm;
	}

	public void setPrefixNameDataForm(LinkedHashMap<String, String> prefixNameDataForm) {
		this.prefixNameDataForm = prefixNameDataForm;
	}

	public LinkedHashMap<String, String> getModelCodeDataForm() {
		return modelCodeDataForm;
	}

	public void setModelCodeDataForm(LinkedHashMap<String, String> modelCodeDataForm) {
		this.modelCodeDataForm = modelCodeDataForm;
	}

	public LinkedHashMap<String, String> getModuleNameDataForm() {
		return moduleNameDataForm;
	}

	public void setModuleNameDataForm(LinkedHashMap<String, String> moduleNameDataForm) {
		this.moduleNameDataForm = moduleNameDataForm;
	}

	protected void openOnCreate(TCComponent object) {
		try {
			TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String generateNextID(String inputString, String type) {
		String newIDValue = "";
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", inputString + "*");
			inputQuery.put("Type", type);
			TCComponent[] item_search = TCExtension.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = inputString + "0001";
			} else {
				int id = 0;
				String split = "";
				split = item_search[0].toString().substring(9, 13);

				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = inputString + StringExtension.ConvertNumberToString(id + 1, 4);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return newIDValue;
	}

	protected void getTargetReleaseDate() {
		Date today = new Date();
		targetReleaseDate = Calendar.getInstance();
		targetReleaseDate.setTime(today);
		targetReleaseDate.add(Calendar.DAY_OF_YEAR, 45);
	}

	protected void updateTargetReviseDateUI() {
		datTargetReviseDate.setVisible(ckbTargetReviseDate.getSelection());
	}

	public abstract void initData(String subCategory);

	public abstract boolean checkRequired();

	public abstract String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate);
}
