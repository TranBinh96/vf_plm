package com.teamcenter.vinfast.bom.update;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BOMValidate_Handler extends AbstractHandler {
	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> moduleValidate = null;
	private LinkedHashMap<String, LinkedList<String>> purchaseLevelValidate = null;
	protected TableEditor[] bomEditors;
	protected TableEditor[] bomEditors1;
	protected TableEditor[] bomEditors2;
	protected TableEditor[] bomEditors3;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		return null;
	}

	protected boolean validatePurchaseLevel(String partMakeBuy, String purchaseLevel) {
		if (partMakeBuy.isEmpty())
			return false;

		LinkedList<String> purchaseLevelList = purchaseLevelValidate.get(partMakeBuy);
		if (purchaseLevelList != null && purchaseLevelList.size() > 0) {
			for (String item : purchaseLevelList) {
				if (item.compareToIgnoreCase(purchaseLevel) == 0)
					return true;
			}
		}

		return false;
	}

	protected boolean validateModuleGroup(String module1, String module2, String module3) {
		if (module1.isEmpty() || module2.isEmpty() || module3.isEmpty())
			return false;

		for (Map.Entry<String, LinkedHashMap<String, LinkedList<String>>> moduleL1Item : moduleValidate.entrySet()) {
			if (moduleL1Item.getKey().compareToIgnoreCase(module1) == 0) {
				for (Map.Entry<String, LinkedList<String>> moduleL2Item : moduleL1Item.getValue().entrySet()) {
					if (moduleL2Item.getKey().compareToIgnoreCase(module2) == 0) {
						for (String moduleL3Item : moduleL2Item.getValue()) {
							if (moduleL3Item.compareToIgnoreCase(module3) == 0)
								return true;
						}
					}
				}
			}
		}

		return false;
	}

	protected void updateModuleLevel2(Combo cbModuleL1, Combo cbModuleL2, Combo cbModuleL3) {
		String moduleLevel1 = cbModuleL1.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			cbModuleL2.setItems(module2.keySet().toArray(new String[0]));
			cbModuleL3.deselectAll();
		}
	}

	protected void updateModuleLevel3(Combo cbModuleL1, Combo cbModuleL2, Combo cbModuleL3) {
		String moduleLevel1 = cbModuleL1.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			String moduleLevel2 = cbModuleL2.getText();
			LinkedList<String> module3 = module2.get(moduleLevel2);
			if (module3 != null) {
				cbModuleL3.setItems(module3.toArray(new String[0]));
			}
		}
	}

	protected void updatePurchaseLevelValidate(TCSession session) {
		String[] valuePurchases = TCExtension.GetPreferenceValues("VF_PART_CREATE_PURCHASELEVEL_VALIDATE", session);
		if (valuePurchases != null && valuePurchases.length > 0) {
			if (valuePurchases.length > 0) {
				purchaseLevelValidate = new LinkedHashMap<String, LinkedList<String>>();
				for (String purchase : valuePurchases) {
					if (purchase.contains(";")) {
						String[] strArray = purchase.split(";");
						String partMakeBuy = strArray[0];
						LinkedList<String> purchaseList = new LinkedList<String>();
						if (strArray[1].contains(",")) {
							String[] strArray2 = strArray[1].split(",");
							for (String value : strArray2) {
								purchaseList.add(value);
							}
						} else {
							purchaseList.add(strArray[1]);
						}
						purchaseLevelValidate.put(partMakeBuy, purchaseList);
					}
				}
			}
		}
	}

	protected void updateModuleLevelValidate(TCSession session) {
		String[] values = TCExtension.GetPreferenceValues("VF_PART_CREATE_MODULE_VALIDATE", session);
		if (values != null && values.length > 0) {
			moduleValidate = new LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>>();
			for (String value : values) {
				String[] str = value.split(";");
				if (str.length > 2) {
					String module1 = str[0];
					String module2 = str[1];
					String module3 = str[2];

					if (moduleValidate.containsKey(module1)) {
						LinkedHashMap<String, LinkedList<String>> oldModule1 = moduleValidate.get(module1);
						if (oldModule1.containsKey(module2)) {
							LinkedList<String> oldModule2 = oldModule1.get(module2);
							oldModule2.add(module3);
						} else {
							LinkedList<String> newModule3 = new LinkedList<String>();
							newModule3.add(module3);
							oldModule1.put(module2, newModule3);
						}
					} else {
						LinkedList<String> newModule3 = new LinkedList<String>();
						newModule3.add(module3);
						LinkedHashMap<String, LinkedList<String>> newModule2 = new LinkedHashMap<String, LinkedList<String>>();
						newModule2.put(module2, newModule3);
						moduleValidate.put(module1, newModule2);
					}
				}
			}
		}
	}

	protected void updateTableItem(int i, String bomline, String partMakeBuy, String purchaseLevel, String moduleL1,
			String moduleL2, String moduleL3, Table tblUpdate) {
		final TableItem item = new TableItem(tblUpdate, SWT.NONE);
		item.setText(new String[] { bomline, partMakeBuy });
		// Create the editor and purchase level combobox
		bomEditors[i] = new TableEditor(tblUpdate);
		Combo cbBOMPurchaseLevel = new Combo(tblUpdate, SWT.READ_ONLY);
		cbBOMPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMPurchaseLevel.setText("Color...");
		cbBOMPurchaseLevel.computeSize(SWT.DEFAULT, tblUpdate.getItemHeight());
		bomEditors[i].grabHorizontal = true;
		bomEditors[i].minimumHeight = cbBOMPurchaseLevel.getSize().y;
		bomEditors[i].minimumWidth = cbBOMPurchaseLevel.getSize().x;
		LinkedList<String> purchaseLevelList = purchaseLevelValidate.get(partMakeBuy);
		if (purchaseLevelList != null) {
			cbBOMPurchaseLevel.setItems(purchaseLevelList.toArray(new String[0]));
			if (StringExtension.checkValueExistInList(purchaseLevel, purchaseLevelList)) {
				cbBOMPurchaseLevel.setText(purchaseLevel);
			}
		}
		bomEditors[i].setEditor(cbBOMPurchaseLevel, item, 2);

		// Create the editor and module 1 combobox
		bomEditors1[i] = new TableEditor(tblUpdate);
		Combo cbBOMModuleGroupEnglish = new Combo(tblUpdate, SWT.READ_ONLY);
		cbBOMModuleGroupEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMModuleGroupEnglish.setText("Color...");
		cbBOMModuleGroupEnglish.computeSize(SWT.DEFAULT, tblUpdate.getItemHeight());
		bomEditors1[i].grabHorizontal = true;
		bomEditors1[i].minimumHeight = cbBOMModuleGroupEnglish.getSize().y;
		bomEditors1[i].minimumWidth = cbBOMModuleGroupEnglish.getSize().x;
		bomEditors1[i].setEditor(cbBOMModuleGroupEnglish, item, 3);

		// Create the editor and module 2 combobox
		bomEditors2[i] = new TableEditor(tblUpdate);
		Combo cbBOMMainModuleEnglish = new Combo(tblUpdate, SWT.READ_ONLY);
		cbBOMMainModuleEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMMainModuleEnglish.setText("Color...");
		cbBOMMainModuleEnglish.computeSize(SWT.DEFAULT, tblUpdate.getItemHeight());
		bomEditors2[i].grabHorizontal = true;
		bomEditors2[i].minimumHeight = cbBOMMainModuleEnglish.getSize().y;
		bomEditors2[i].minimumWidth = cbBOMMainModuleEnglish.getSize().x;
		bomEditors2[i].setEditor(cbBOMMainModuleEnglish, item, 4);

		// Create the editor and module 3 combobox
		bomEditors3[i] = new TableEditor(tblUpdate);
		Combo cbBOMModuleName = new Combo(tblUpdate, SWT.READ_ONLY);
		cbBOMModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMModuleName.setText("Color...");
		cbBOMModuleName.computeSize(SWT.DEFAULT, tblUpdate.getItemHeight());
		bomEditors3[i].grabHorizontal = true;
		bomEditors3[i].minimumHeight = cbBOMModuleName.getSize().y;
		bomEditors3[i].minimumWidth = cbBOMModuleName.getSize().x;
		bomEditors3[i].setEditor(cbBOMModuleName, item, 5);

		// ------------------------------------------------------------------------------
		cbBOMModuleGroupEnglish.setItems(moduleValidate.keySet().toArray(new String[0]));
		if (StringExtension.checkValueExistInList(moduleL1, moduleValidate.keySet())) {
			cbBOMModuleGroupEnglish.setText(moduleL1);
			LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleL1);
			if (module2 != null) {
				cbBOMMainModuleEnglish.setItems(module2.keySet().toArray(new String[0]));
				if (StringExtension.checkValueExistInList(moduleL2, module2.keySet())) {
					cbBOMMainModuleEnglish.setText(moduleL2);
					LinkedList<String> module3 = module2.get(moduleL2);
					if (module3 != null) {
						cbBOMModuleName.setItems(module3.toArray(new String[0]));
						if (StringExtension.checkValueExistInList(moduleL3, module3)) {
							cbBOMModuleName.setText(moduleL3);
						}
					}
				}
			}
		}

		cbBOMModuleGroupEnglish.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
				Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
				Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

				updateModuleLevel2(cbModuleL1, cbModuleL2, cbModuleL3);
			}
		});

		cbBOMMainModuleEnglish.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
				Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
				Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

				updateModuleLevel3(cbModuleL1, cbModuleL2, cbModuleL3);
			}
		});
	}
}
