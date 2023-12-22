package com.teamcenter.vinfast.car.manuf.update;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class MEPartAttributesUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private MEPartAttributesUpdate_Dialog dlg;
	private static String[] partTraceabilityDisableStatus = new String[] { "PR", "PPR" };
	private static String[] objectTypeAvailable = new String[] { "ME Part" };

	public MEPartAttributesUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if (validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one ME Part.", "Information", MessageBox.INFORMATION);
			return null;
		}

		try {
			// ---------------------------- Init Data -------------------------------------
			LinkedHashMap<String, String> partTraceableDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", "VF3_manuf_part", session);

			// ---------------------------- UI -------------------------------------
			dlg = new MEPartAttributesUpdate_Dialog(new Shell());
			dlg.create();

			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceableDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateItem();
				}
			});
			// -----------------------------------------------------------------------
			updateCurrentValue();
			validateUpdate();
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void updateItem() {
		try {
			String partTraceable = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
			String isAfterSale = "";
			if (dlg.rbtIsAfterSaleTrue.getSelection())
				isAfterSale = "1";
			if (dlg.rbtIsAfterSaleFalse.getSelection())
				isAfterSale = "0";

			Map<String, String> input = new LinkedHashMap<String, String>();
			if (!dlg.btnInfoIsAfterSale.getVisible())
				input.put("vf4_itm_after_sale_relevant", isAfterSale);
			if (!dlg.btnInfoPartTraceability.getVisible())
				input.put("vf4_item_is_traceable", partTraceable);

			selectedObject.setProperties(input);

			dlg.setMessage("Update part attribute(s) success.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			dlg.setMessage(e.toString(), IMessageProvider.ERROR);
			e.printStackTrace();
		}
	}

	private void validateUpdate() {
		try {
			boolean otherCheck = false;
			boolean traceabilityCheck = false;
			dlg.btnInfoPartTraceability.setVisible(false);
			dlg.btnInfoIsAfterSale.setVisible(false);
			// check write access
			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				dlg.setMessage("You don't have write access to update information: " + selectedObject.getPropertyDisplayableValue("item_id"), IMessageProvider.INFORMATION);
				for (Control ctrl : dlg.getShell().getChildren()) {
					ctrl.setEnabled(false);
				}
				return;
			}

			// check have working rev
			TCComponentItemRevision[] relRevs = selectedObject.getReleasedItemRevisions();
			TCComponent[] allRevs = selectedObject.getRelatedComponents("revision_list");
			if (relRevs.length == allRevs.length) {
				otherCheck = true;
				dlg.rbtIsAfterSaleFalse.setEnabled(false);
				dlg.rbtIsAfterSaleTrue.setEnabled(false);
				String mess = "Cannot update attribute(s) when all revisions of item has been released.";
				dlg.btnInfoIsAfterSale.setVisible(true);
				dlg.btnInfoIsAfterSale.setToolTipText(mess);
			}

			// check part is released
			if (checkPartsReleasedFlag()) {
				traceabilityCheck = true;
				dlg.cbPartTraceability.setEnabled(false);
				dlg.btnInfoPartTraceability.setVisible(true);
				dlg.btnInfoPartTraceability.setToolTipText("Cannot update attribute(s) when part have status: " + String.join(", ", partTraceabilityDisableStatus));
			}
			if (traceabilityCheck && otherCheck)
				dlg.btnCreate.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateCurrentValue() {
		try {
			String partTraceable = selectedObject.getPropertyDisplayableValue("vf4_item_is_traceable");
			if (partTraceable.compareTo("Y") == 0)
				partTraceable = "Yes";
			else if (partTraceable.compareTo("N") == 0)
				partTraceable = "No";

			dlg.cbPartTraceability.setText(partTraceable);

			String isAfterSale = selectedObject.getPropertyDisplayableValue("vf4_itm_after_sale_relevant");
			if (isAfterSale.compareToIgnoreCase("true") == 0 || isAfterSale.compareToIgnoreCase("1") == 0)
				dlg.rbtIsAfterSaleTrue.setSelection(true);
			if (isAfterSale.compareToIgnoreCase("false") == 0 || isAfterSale.compareToIgnoreCase("0") == 0)
				dlg.rbtIsAfterSaleFalse.setSelection(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkPartsReleasedFlag() throws TCException, NotLoadedException {
		TCComponentItemRevision[] relRevs = selectedObject.getReleasedItemRevisions();
		for (TCComponentItemRevision item : relRevs) {
			String statusList = item.getPropertyDisplayableValue("release_status_list");
			if (!statusList.isEmpty()) {
				if (Arrays.stream(partTraceabilityDisableStatus).anyMatch(statusList::equals))
					return true;
			}
		}

		return false;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		try {
			if (targetComponents == null)
				return true;
			if (targetComponents.length > 1)
				return true;
			if (targetComponents[0] instanceof TCComponentBOMLine) {
				selectedObject = ((TCComponentBOMLine) targetComponents[0]).getItem();
			} else if (targetComponents[0] instanceof TCComponentItemRevision) {
				selectedObject = ((TCComponentItemRevision) targetComponents[0]).getItem();
			} else if (targetComponents[0] instanceof TCComponentItem) {
				selectedObject = (TCComponentItem) targetComponents[0];
			}

			if (selectedObject == null)
				return true;

			boolean check = false;
			String objectType = selectedObject.getPropertyDisplayableValue("object_type");
			for (String objectTypeOk : objectTypeAvailable) {
				if (objectTypeOk.compareTo(objectType) == 0)
					check = true;
			}
			if (!check)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
