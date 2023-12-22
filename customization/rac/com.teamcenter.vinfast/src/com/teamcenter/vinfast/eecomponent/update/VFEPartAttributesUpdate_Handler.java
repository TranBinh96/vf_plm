package com.teamcenter.vinfast.eecomponent.update;

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

public class VFEPartAttributesUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private VFEPartAttributesUpdate_Dialog dlg;
	public static final String[] partTraceabilityDisableStatus = new String[] { "I", "ICR", "PR", "PPR" };
	public static String OBJECT_TYPE = "VF4_Compo_Design";

	public VFEPartAttributesUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if(!validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one VF Component.", "Information", MessageBox.INFORMATION);
			return null;
		}

		try {
			// ---------------------------- Init Data -------------------------------------
			LinkedHashMap<String, String> donorProgramDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", OBJECT_TYPE, session);
			LinkedHashMap<String, String> supplierTypeDataForm = TCExtension.GetLovValueAndDisplay("vf4_supplier_type", OBJECT_TYPE, session);
			LinkedHashMap<String, String> longShortLeadDataForm = TCExtension.GetLovValueAndDisplay("vf4_long_short_lead", OBJECT_TYPE, session);
			LinkedHashMap<String, String> partCategoryDataForm = TCExtension.GetLovValueAndDisplay("vf4_part_category", OBJECT_TYPE, session);
			LinkedHashMap<String, String> partTraceableDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", OBJECT_TYPE, session);

			// ---------------------------- UI -------------------------------------
			dlg = new VFEPartAttributesUpdate_Dialog(new Shell());
			dlg.create();

			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			StringExtension.UpdateValueTextCombobox(dlg.cbDonorProgram, donorProgramDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbSupplierType, supplierTypeDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbLongOrShortlead, longShortLeadDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartCategory, partCategoryDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceableDataForm);
			dlg.btnInfoPartTraceability.setVisible(false);
			dlg.btnInfoIsAfterSale.setVisible(false);
			dlg.btnInfoDonorVehicle.setVisible(false);
			dlg.btnInfoLongShort.setVisible(false);
			dlg.btnInfoPartCategory.setVisible(false);
			dlg.btnInfoSupplierType.setVisible(false);

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
			String isAfterSale = dlg.ckbIsAfterSale.getSelection() ? "1" : "0";
			String donorVehicle = (String) dlg.cbDonorProgram.getData(dlg.cbDonorProgram.getText());
			String supplierType = (String) dlg.cbSupplierType.getData(dlg.cbSupplierType.getText());
			String longShortLead = (String) dlg.cbLongOrShortlead.getData(dlg.cbLongOrShortlead.getText());
			String partCategory = (String) dlg.cbPartCategory.getData(dlg.cbPartCategory.getText());
			String partTraceable = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());

			Map<String, String> input = new LinkedHashMap<String, String>();
			if (dlg.ckbIsAfterSale.getEnabled())
				input.put("vf4_itm_after_sale_relevant", isAfterSale);
			if (dlg.cbDonorProgram.getEnabled())
				input.put("vf4_donor_vehicle", donorVehicle);
			if (dlg.cbSupplierType.getEnabled())
				input.put("vf4_supplier_type", supplierType);
			if (dlg.cbLongOrShortlead.getEnabled())
				input.put("vf4_long_short_lead", longShortLead);
			if (dlg.cbPartCategory.getEnabled())
				input.put("vf4_part_category", partCategory);
			if (dlg.cbPartTraceability.getEnabled())
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
			// check write access
			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				dlg.setMessage("You don't have write access to update information: "
						+ selectedObject.getPropertyDisplayableValue("item_id"), IMessageProvider.INFORMATION);
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
				dlg.ckbIsAfterSale.setEnabled(false);
				dlg.cbDonorProgram.setEnabled(false);
				dlg.cbLongOrShortlead.setEnabled(false);
				dlg.cbPartCategory.setEnabled(false);
				dlg.cbSupplierType.setEnabled(false);

				String mess = "Cannot update attribute(s) when all revisions of item has been released.";
				dlg.btnInfoIsAfterSale.setVisible(true);
				dlg.btnInfoIsAfterSale.setToolTipText(mess);
				dlg.btnInfoLongShort.setVisible(true);
				dlg.btnInfoLongShort.setToolTipText(mess);
				dlg.btnInfoDonorVehicle.setVisible(true);
				dlg.btnInfoDonorVehicle.setToolTipText(mess);
				dlg.btnInfoPartCategory.setVisible(true);
				dlg.btnInfoPartCategory.setToolTipText(mess);
				dlg.btnInfoSupplierType.setVisible(true);
				dlg.btnInfoSupplierType.setToolTipText(mess);
			}
			// check part is released
			if (checkPartsReleasedFlag()) {
				traceabilityCheck = true;
				dlg.cbPartTraceability.setEnabled(false);

				dlg.btnInfoPartTraceability.setVisible(true);
				dlg.btnInfoPartTraceability.setToolTipText("Cannot update attribute(s) when part have status: I, ICR, PR or PPR.");
			}
			if (traceabilityCheck && otherCheck)
				dlg.btnCreate.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateCurrentValue() {
		try {
			String isAfterSale = selectedObject.getPropertyDisplayableValue("vf4_itm_after_sale_relevant");
			String donorVehicle = selectedObject.getPropertyDisplayableValue("vf4_donor_vehicle");
			String supplierType = selectedObject.getPropertyDisplayableValue("vf4_supplier_type");
			String longShortLead = selectedObject.getPropertyDisplayableValue("vf4_long_short_lead");
			String partCategory = selectedObject.getPropertyDisplayableValue("vf4_part_category");
			String partTraceable = selectedObject.getPropertyDisplayableValue("vf4_item_is_traceable");

			if (isAfterSale.compareToIgnoreCase("true") == 0 || isAfterSale.compareToIgnoreCase("1") == 0) {
				dlg.ckbIsAfterSale.setSelection(true);
			}
			dlg.cbDonorProgram.setText(donorVehicle);
			dlg.cbSupplierType.setText(supplierType);
			dlg.cbLongOrShortlead.setText(longShortLead);
			dlg.cbPartCategory.setText(partCategory);
			dlg.cbPartTraceability.setText(partTraceable);
		} catch (Exception e) {

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
				return false;
			if (selectedObject.getType().compareToIgnoreCase(OBJECT_TYPE) != 0)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
