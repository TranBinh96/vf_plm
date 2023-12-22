package com.teamcenter.vinfast.bom.update;

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.services.*;
import com.vf.utils.TCExtension;

public class BOMValidateUpdateBOMAttr_Handler extends BOMValidate_Handler {
	private TCSession session;
	private BOMValidateUpdateBOMAttr_Dialog dlg;
	private LinkedList<TCComponentBOMLine> selectedObjects = null;

	public BOMValidateUpdateBOMAttr_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			selectedObjects = new LinkedList<TCComponentBOMLine>();
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			for (InterfaceAIFComponent item : targetComp) {
				if (item instanceof TCComponentBOMLine) {
					selectedObjects.add((TCComponentBOMLine) item);
				}
			}
			// Init data
			String[] purchaseLevelDataForm = TCExtension.GetLovValues("VL5_purchase_lvl_vf", "BOMLine", session);
			updatePurchaseLevelValidate(session);
			updateModuleLevelValidate(session);
			// Init UI
			dlg = new BOMValidateUpdateBOMAttr_Dialog(new Shell());
			dlg.create();
			dlg.cbPurchaseLevel.setItems(purchaseLevelDataForm);
			updateUI();

			dlg.cbPurchaseLevel.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String purchaseLevel = dlg.cbPurchaseLevel.getText();
					if (!purchaseLevel.isEmpty()) {
						for (int i = 0; i < selectedObjects.size(); i++) {
							boolean check = false;
							Combo cbPurchaseLevel = (Combo) bomEditors[i].getEditor();
							String[] values = cbPurchaseLevel.getItems();
							if (values.length > 0) {
								for (String value : values) {
									check = true;
									break;
								}
							}
							if (check) {
								cbPurchaseLevel.setText(purchaseLevel);
							}
						}
					}
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateAttribute();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateUI() {
		int rowNum = selectedObjects.size();
		bomEditors = new TableEditor[rowNum];
		bomEditors1 = new TableEditor[rowNum];
		bomEditors2 = new TableEditor[rowNum];
		bomEditors3 = new TableEditor[rowNum];

		for (int i = 0; i < selectedObjects.size(); i++) {
			TCComponentBOMLine bomline = selectedObjects.get(i);
			String bomName = "";
			String makeBuy = "";
			String purchaseLevel = "";
			String moduleL1 = "";
			String moduleL2 = "";
			String moduleL3 = "";
			try {
				bomName = bomline.getPropertyDisplayableValue("bl_indented_title");
				makeBuy = bomline.getPropertyDisplayableValue("bl_item_vf4_item_make_buy");
				purchaseLevel = bomline.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
				moduleL1 = bomline.getPropertyDisplayableValue("VL5_module_group");
				moduleL2 = bomline.getPropertyDisplayableValue("VL5_main_module");
				moduleL3 = bomline.getPropertyDisplayableValue("VL5_module_name");
			} catch (Exception e) {

			}
			updateTableItem(i, bomName, makeBuy, purchaseLevel, moduleL1, moduleL2, moduleL3, dlg.tblUpdate);
		}
	}

	private void updateAttribute() {
		for (int i = 0; i < dlg.tblUpdate.getItemCount(); i++) {
			Combo cbPurchaseLevel = (Combo) bomEditors[i].getEditor();
			Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
			Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
			Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

			try {
				TCComponentBOMLine bomLine = selectedObjects.get(i);
				bomLine.setProperty("VL5_purchase_lvl_vf", cbPurchaseLevel.getText());
				bomLine.setProperty("VL5_module_group", cbModuleL1.getText());
				bomLine.setProperty("VL5_main_module", cbModuleL2.getText());
				bomLine.setProperty("VL5_module_name", cbModuleL3.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dlg.setMessage("Update Bom attribute success.", IMessageProvider.INFORMATION);
	}
}
