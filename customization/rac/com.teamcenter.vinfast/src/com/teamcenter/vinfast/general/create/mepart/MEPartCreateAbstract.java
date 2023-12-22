package com.teamcenter.vinfast.general.create.mepart;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public abstract class MEPartCreateAbstract extends Composite {
	protected Combo cbModel;
	protected Combo cbPartMakeBuy;
	protected Combo cbUOM;
	protected Combo cbPartTraceability;
	protected Combo cbPartCategory;
	protected Combo cbSupplierType;
	protected Combo cbMaterial;
	protected Combo cbCoating;
	protected Combo cbVehicleLine;
	protected Combo cbBlankType;

	protected Text txtID;
	protected Text txtName;
	protected Text txtDesc;
	protected Text txtLinkPart;
	protected Text txtPartReference;
	protected Text txtPartNameVietnamese;

	protected Spinner txtThickness;
	protected Spinner txtWidth;
	protected Spinner txtLength;

	protected Button rbtIsAfterSaleTrue;
	protected Button rbtIsAfterSaleFalse;
	protected Button btnAutoFill;
	protected Button ckbIsPurchase;
	protected Button btnRemoveLink;

	protected Label lblNameWarning;

	protected String subCategory;
	protected String[] makeBuyDataForm;
	protected String[] uomDataForm;
	protected LinkedHashMap<String, String> partTraceabilityDataForm;
	protected String[] partCategoryDataForm;
	protected String[] supplierTypeDataForm;
	protected LinkedHashMap<String, String> vehicleLineDataForm;
	protected String[] materialDataForm;
	protected List<String> coatingDataForm;

	protected LinkedHashMap<String, String> modelAndPrefixNumber;
	protected LinkedHashMap<String, String> modelAndDisplayName;
	protected LinkedHashMap<String, Boolean> isAfterSaleSetting;
	protected LinkedHashMap<String, String> partTraceabilitySetting;
	protected LinkedHashMap<String, List<String>> uomSetting;
	protected LinkedHashMap<String, List<String>> partMakeBuySetting;

	protected TCComponentItemRevision parentPart = null;

	protected TCSession session;

	public MEPartCreateAbstract(Composite c) {
		super(c, SWT.NONE);
	}

	protected void setMakeBuyDataForm(String[] makeBuyDataForm) {
		this.makeBuyDataForm = makeBuyDataForm;
	}

	public void setUomDataForm(String[] uomDataForm) {
		this.uomDataForm = uomDataForm;
	}

	public void setPartTraceabilityDataForm(LinkedHashMap<String, String> partTraceabilityDataForm) {
		this.partTraceabilityDataForm = partTraceabilityDataForm;
	}

	public void setPartCategoryDataForm(String[] partCategoryDataForm) {
		this.partCategoryDataForm = partCategoryDataForm;
	}

	public void setSupplierTypeDataForm(String[] supplierTypeDataForm) {
		this.supplierTypeDataForm = supplierTypeDataForm;
	}

	public void setModelAndPrefixNumber(LinkedHashMap<String, String> modelAndPrefixNumber) {
		this.modelAndPrefixNumber = modelAndPrefixNumber;
	}

	public void setModelAndDisplayName(LinkedHashMap<String, String> modelAndDisplayName) {
		this.modelAndDisplayName = modelAndDisplayName;
	}

	public void setIsAfterSaleSetting(LinkedHashMap<String, Boolean> isAfterSaleSetting) {
		this.isAfterSaleSetting = isAfterSaleSetting;
	}

	public void setPartTraceabilitySetting(LinkedHashMap<String, String> partTraceabilitySetting) {
		this.partTraceabilitySetting = partTraceabilitySetting;
	}

	public void setUomSetting(LinkedHashMap<String, List<String>> uomSetting) {
		this.uomSetting = uomSetting;
	}

	public void setPartMakeBuySetting(LinkedHashMap<String, List<String>> partMakeBuySetting) {
		this.partMakeBuySetting = partMakeBuySetting;
	}

	public void setVehicleLineDataForm(LinkedHashMap<String, String> vehicleLineDataForm) {
		this.vehicleLineDataForm = vehicleLineDataForm;
	}

	public void setMaterialDataForm(String[] materialDataForm) {
		this.materialDataForm = materialDataForm;
	}

	public void setCoatingDataForm(List<String> coatingDataForm) {
		this.coatingDataForm = coatingDataForm;
	}

	public void setParentPart(TCComponentItemRevision parentPart) {
		this.parentPart = parentPart;
	}

	public void setSession(TCSession session) {
		this.session = session;
	}

	protected void updateUI() {
		Boolean isAfterSale = isAfterSaleSetting.get(subCategory);
		String partTrace = partTraceabilitySetting.get(subCategory);
		List<String> uom = uomSetting.get(subCategory);
		List<String> makeBuy = partMakeBuySetting.get(subCategory);
		if (isAfterSale == null) {
			rbtIsAfterSaleTrue.setSelection(false);
			rbtIsAfterSaleFalse.setSelection(false);
		} else {
			rbtIsAfterSaleTrue.setSelection(isAfterSale);
			rbtIsAfterSaleFalse.setSelection(!isAfterSale);
		}

//		if (partTrace == null || partTrace.isEmpty()) {
//			cbPartTraceability.setEnabled(true);
//		} else {
//			cbPartTraceability.setEnabled(false);
//			cbPartTraceability.setText(partTrace);
//		}

		if (uom != null) {
			cbUOM.setItems(uom.toArray(new String[0]));
			if (uom.size() == 1)
				cbUOM.select(0);
			else
				cbUOM.deselectAll();
		} else {
			cbUOM.removeAll();
			cbUOM.setItems(uomDataForm);
		}

		if (makeBuy != null) {
			cbPartMakeBuy.setItems(makeBuy.toArray(new String[0]));
			if (makeBuy.size() == 1)
				cbPartMakeBuy.select(0);
			else
				cbPartMakeBuy.deselectAll();
		} else
			cbPartMakeBuy.removeAll();
	}

	protected void updateUIWhenMakeBuyChange() {
		String makeBuy = cbPartMakeBuy.getText();
		if (makeBuy.compareTo("Make") == 0) {
			updateSupplierType(true);
			cbSupplierType.setEnabled(false);
			cbSupplierType.setText("NONE");
		} else {
			updateSupplierType(false);
			cbSupplierType.setEnabled(true);
			cbSupplierType.setText("");
		}
	}

	protected void updateSupplierType(boolean addNone) {
		boolean haveNone = false;
		for (String value : cbSupplierType.getItems()) {
			if (value.compareTo("NONE") == 0) {
				haveNone = true;
				break;
			}
		}
		if (addNone) {
			if (!haveNone)
				cbSupplierType.add("NONE");
		} else {
			if (haveNone)
				cbSupplierType.remove("NONE");
		}
	}

	protected boolean checkRequiredCommon() {
		if (subCategory.isEmpty())
			return false;
		if (txtID.getText().isEmpty())
			return false;
		if (txtName.getText().isEmpty())
			return false;
		if (txtDesc.getText().isEmpty())
			return false;
		if (cbPartMakeBuy.getText().isEmpty())
			return false;
		if (cbUOM.getText().isEmpty())
			return false;
		if (cbPartCategory.getText().isEmpty())
			return false;

		return true;
	}

	protected void openOnCreate(TCComponent object) {
		try {
			TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String generateNextID(String inputString, String type) {
		try {
			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", inputString + "*");
			inputQuery.put("Type", type);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");
			int remainNo = 11 - inputString.length();
			if (item_search == null || item_search.length == 0) {
				newIDValue = inputString + StringExtension.ConvertNumberToString(1, remainNo);
			} else {
				int id = 0;

				String split = item_search[0].toString().substring(inputString.length(), 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = inputString + StringExtension.ConvertNumberToString(id + 1, remainNo);
			}
			return newIDValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	protected boolean checkPartExistSpec(String material, String coating, Double thickness, Double width, Double length) {
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Material", material);
			inputQuery.put("Coating", coating);
			if (thickness != null)
				inputQuery.put("Thickness", thickness.toString());
			if (width != null)
				inputQuery.put("Width", width.toString());
			if (length != null)
				inputQuery.put("Length", length.toString());
			if (!subCategory.isEmpty())
				inputQuery.put("ME Part Type", subCategory);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "__TNH_FindMEPart_BySpec");
			if (item_search != null && item_search.length > 0) {
				int i = 0;
				Set<String> partList = new HashSet<String>();
				for (TCComponent item : item_search) {
					if (i == 10) {
						partList.add("...");
						break;
					}
					partList.add(item.getPropertyDisplayableValue("object_string"));
					i++;
				}

				MessageBox.post("Part(s) with same spec information exist in system.\n" + String.join("\n", partList), "WARNING", MessageBox.WARNING);
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	public abstract void initData(String subCategory);

	public abstract boolean checkRequired();

	public abstract String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate);
}
