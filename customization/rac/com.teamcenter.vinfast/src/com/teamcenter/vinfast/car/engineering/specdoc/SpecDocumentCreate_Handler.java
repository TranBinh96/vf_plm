package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SpecDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	private SpecDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private SpecDocumentCreateAbstract specCom = null;
	private String VEHICLE_CATEGORY = "AUTOMOBILE";

	public SpecDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			// init Data
			LinkedHashMap<String, String> objectTypeMapping = new LinkedHashMap<>();

			LinkedHashMap<String, String> prefixNameDataForm = new LinkedHashMap<>();
			String[] prefixPreferenceValues = TCExtension.GetPreferenceValues("VINF_SPECBOOK_PREFIX_NAME", session);
			if (prefixPreferenceValues != null) {
				for (String values : prefixPreferenceValues) {
					String[] str = values.split("=");
					if (str.length >= 4) {
						if (str[1].compareTo(VEHICLE_CATEGORY) == 0) {
							objectTypeMapping.put(str[0], str[3]);
							prefixNameDataForm.put(str[0], str[0] + " - " + str[2]);
						}
					}
				}
			}

			LinkedHashMap<String, String> modelCodeDataForm = new LinkedHashMap<>();
			String[] modelCodePreferenceValues = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODEL_CODE", session);
			if (modelCodePreferenceValues != null) {
				for (String values : modelCodePreferenceValues) {
					String[] str = values.split("=");
					if (str.length >= 3) {
						if (str[1].compareTo(VEHICLE_CATEGORY) == 0) {
							modelCodeDataForm.put(str[0], str[0] + " - " + str[2]);
						}
					}
				}
			}

			LinkedHashMap<String, String> moduleNameDataForm = new LinkedHashMap<>();
			String[] moduleNamePreferenceValues = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODULE_NAME", session);
			if (moduleNamePreferenceValues != null) {
				for (String values : moduleNamePreferenceValues) {
					String[] str = values.split("=");
					if (str.length >= 3) {
						if (str[1].compareTo(VEHICLE_CATEGORY) == 0) {
							moduleNameDataForm.put(str[0], str[0] + " - " + str[2]);
						}
					}
				}
			}
			// init UI
			dlg = new SpecDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbPrefixName, TCExtension.SortingLOV(prefixNameDataForm));
			dlg.cbPrefixName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					if (specCom != null)
						specCom.dispose();

					String prefix = "";
					if (!dlg.cbPrefixName.getText().isEmpty())
						prefix = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());

					if (specCom == null || specCom.isDisposed()) {
						specCom = SpecDocumentCreateFactory.generateComposite(dlg.container, prefix);
						specCom.setObjectTypeMapping(objectTypeMapping);
						specCom.setPrefixNameDataForm(prefixNameDataForm);
						specCom.setModelCodeDataForm(modelCodeDataForm);
						specCom.setModuleNameDataForm(moduleNameDataForm);
						specCom.setSession(session);
						specCom.getTargetReleaseDate();
						specCom.initData(prefix);
					}
					specCom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
					resizeUI();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!specCom.checkRequired()) {
						dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
						return;
					}
					String mess = specCom.createNewItem(selectedObject, dlg.ckbOpenOnCreate.getSelection());
					if (!mess.isEmpty())
						dlg.setMessage(mess);
				}
			});

			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void resizeUI() {
		Point currentSize = dlg.getShell().getSize();
		dlg.getShell().setSize(currentSize.x, ++currentSize.y);
	}
}