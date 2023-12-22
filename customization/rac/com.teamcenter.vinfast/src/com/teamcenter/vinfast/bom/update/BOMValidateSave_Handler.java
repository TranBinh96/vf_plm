package com.teamcenter.vinfast.bom.update;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pca.views.VariantExpressionEditorView;
import com.teamcenter.rac.pse.AbstractPSEApplication;
import com.teamcenter.rac.pse.PSEApplicationPanel;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.services.*;
import com.teamcenter.rac.ui.views.AbstractContentViewPart;

public class BOMValidateSave_Handler extends BOMValidate_Handler {
	private TCSession session;
	private BOMValidateSave_Dialog dlg;
	private LinkedList<TCComponentBOMLine> bomlineNotValid = null;

	public BOMValidateSave_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			// Init data
			updatePurchaseLevelValidate(session);
			updateModuleLevelValidate(session);
			if (getPendingEditNotValid()) {
				saveBom();
			} else {
				generateUI();
			}
//			saveVariant(arg0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void generateUI() {
		// Init UI
		dlg = new BOMValidateSave_Dialog(new Shell());
		dlg.create();
		updateUI();
		dlg.btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateAttribute();
			}
		});
		dlg.open();
	}

	private boolean getPendingEditNotValid() {
		boolean validationAll = true;
		try {
			AIFDesktop desk = AIFDesktop.getActiveDesktop();
			AbstractAIFUIApplication abstractAIFUIApplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
			if (abstractAIFUIApplication instanceof PSEApplicationService
					&& ((PSEApplicationService) abstractAIFUIApplication).getViewableTreeTable() != null) {
				PSEApplicationService bom = (PSEApplicationService) abstractAIFUIApplication;
				BOMPanel bomPanel = bom.getBOMPanel();
				TCComponentBOMLine[] pendingEditList = bomPanel.getAllPendingEditLines();

				if (pendingEditList != null && pendingEditList.length > 0) {
					bomlineNotValid = new LinkedList<TCComponentBOMLine>();
					for (TCComponentBOMLine bomline : pendingEditList) {
						String partMakeBuy = bomline.getPropertyDisplayableValue("bl_item_vf4_item_make_buy");
						String purchaseLevel = bomline.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
						String moduleL1 = bomline.getPropertyDisplayableValue("VL5_module_group");
						String moduleL2 = bomline.getPropertyDisplayableValue("VL5_main_module");
						String moduleL3 = bomline.getPropertyDisplayableValue("VL5_module_name");

						boolean check = false;
						if (validatePurchaseLevel(partMakeBuy, purchaseLevel)) {
							if (validateModuleGroup(moduleL1, moduleL2, moduleL3)) {
								check = true;
							}
						}
						if (!check) {
							bomlineNotValid.add(bomline);
							validationAll = false;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return validationAll;
	}

	private void updateUI() {
		int rowNum = bomlineNotValid.size();
		bomEditors = new TableEditor[rowNum];
		bomEditors1 = new TableEditor[rowNum];
		bomEditors2 = new TableEditor[rowNum];
		bomEditors3 = new TableEditor[rowNum];

		for (int i = 0; i < bomlineNotValid.size(); i++) {
			TCComponentBOMLine bomline = bomlineNotValid.get(i);
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
		if (!validateSave()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		for (int i = 0; i < dlg.tblUpdate.getItemCount(); i++) {
			Combo cbPurchaseLevel = (Combo) bomEditors[i].getEditor();
			Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
			Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
			Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

			try {
				TCComponentBOMLine bomLine = bomlineNotValid.get(i);
				bomLine.setProperty("VL5_purchase_lvl_vf", cbPurchaseLevel.getText());
				bomLine.setProperty("VL5_module_group", cbModuleL1.getText());
				bomLine.setProperty("VL5_main_module", cbModuleL2.getText());
				bomLine.setProperty("VL5_module_name", cbModuleL3.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		saveBom();
	}

	private boolean validateSave() {
		for (int i = 0; i < dlg.tblUpdate.getItemCount(); i++) {
			Combo cbPurchaseLevel = (Combo) bomEditors[i].getEditor();
			if (cbPurchaseLevel.getText().isEmpty())
				return false;
			Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
			if (cbModuleL1.getText().isEmpty())
				return false;
			Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
			if (cbModuleL2.getText().isEmpty())
				return false;
			Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();
			if (cbModuleL3.getText().isEmpty())
				return false;
		}

		return true;
	}

	private void saveBom() {
		final AbstractPSEApplication PSEApplication = (AbstractPSEApplication) AIFUtility.getCurrentApplication();
		TCComponentBOMLine topBOMLine = (TCComponentBOMLine) PSEApplication.getTopBOMLine();
		if (topBOMLine != null) {
			try {
				TCComponentBOMWindow tcWindow = (TCComponentBOMWindow) topBOMLine.getCachedWindow();
				tcWindow.save();
				dlg.setMessage("Save Bom attribute success.", IMessageProvider.INFORMATION);
			} catch (Exception ex) {
				ex.printStackTrace();
				dlg.setMessage(ex.toString(), IMessageProvider.ERROR);
			}
		}
	}
	
	private void saveVariant(ExecutionEvent evt) {
        ICommandService iCommandService = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);  
        Command command = iCommandService.getCommand("org.eclipse.ui.file.save");
        try {
            command.executeWithChecks(evt);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
	}
}
