package com.teamcenter.vines.update;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VESPartAttributesUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private VESPartAttributesUpdate_Dialog dlg;
	private static String[] partTraceabilityDisableStatus = new String[] { "PR", "PPR" };
	private static String[] isAFSDisableStatus = new String[] { "P", "PCR", "I", "ICR", "PR", "PPR" };
	private static String[] OBJECT_TYPE_AVAILABLE = new String[] { "VinES BP Design" };

	public VESPartAttributesUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if (validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one " + String.join("/", OBJECT_TYPE_AVAILABLE) + ".", "Information", MessageBox.INFORMATION);
			return null;
		}

		try {
			// ---------------------------- Init Data -------------------------------------
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", "VF4_Design", session);
			LinkedHashMap<String, String> supplierTypeDataForm = TCExtension.GetLovValueAndDisplay("vf4_supplier_type", "VF4_Design", session);
			LinkedHashMap<String, String> longShortLeadDataForm = TCExtension.GetLovValueAndDisplay("vf4_long_short_lead", "VF4_Design", session);
			LinkedHashMap<String, String> partCategoryDataForm = TCExtension.GetLovValueAndDisplay("vf4_part_category", "VF4_Design", session);
			LinkedHashMap<String, String> partTraceableDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", "VF4_Design", session);
			LinkedHashMap<String, String> homoCountriesDataForm = TCExtension.GetLovValueAndDisplay("vf4_homologation_countries", "VF4_Design", session);

			// ---------------------------- UI -------------------------------------
			dlg = new VESPartAttributesUpdate_Dialog(new Shell());
			dlg.create();

			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			StringExtension.UpdateValueTextCombobox(dlg.cbDonorVehicle, donorVehicleDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbSupplierType, supplierTypeDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbLongOrShortlead, longShortLeadDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartCategory, partCategoryDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceableDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbHomologationCountries, homoCountriesDataForm);
			dlg.btnInfoPartTraceability.setVisible(false);
			dlg.btnInfoIsAfterSale.setVisible(false);
			dlg.btnInfoDonorVehicle.setVisible(false);
			dlg.btnInfoLongShort.setVisible(false);
			dlg.btnInfoPartCategory.setVisible(false);
			dlg.btnInfoSupplierType.setVisible(false);
			dlg.btnInfoHomologationCountries.setVisible(false);
			dlg.btnInfoIsHomologation.setVisible(false);

			dlg.cbHomologationCountries.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					String country = dlg.cbHomologationCountries.getText();
					if (!country.isEmpty()) {
						String[] homoContries = dlg.lstHomologationCountries.getItems();
						boolean check = true;
						if (homoContries.length > 0) {
							for (String item : homoContries) {
								if (item.compareTo(country) == 0)
									check = false;
							}
						}
						if (check) {
							dlg.lstHomologationCountries.add(country);
						}
					}
				}
			});

			dlg.btnRemoveHologation.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.lstHomologationCountries.remove(dlg.lstHomologationCountries.getSelectionIndex());
				}
			});

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
			String donorVehicle = (String) dlg.cbDonorVehicle.getData(dlg.cbDonorVehicle.getText());
			String supplierType = (String) dlg.cbSupplierType.getData(dlg.cbSupplierType.getText());
			String longShortLead = (String) dlg.cbLongOrShortlead.getData(dlg.cbLongOrShortlead.getText());
			String partCategory = (String) dlg.cbPartCategory.getData(dlg.cbPartCategory.getText());
			String partTraceable = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
			String isHomologation = dlg.ckbIsHomologation.getSelection() ? "1" : "0";
			String[] homoContriesValue = dlg.lstHomologationCountries.getItems();
			List<String> homoContries = new LinkedList<String>();
			if (homoContriesValue.length > 0) {
				for (String item : homoContriesValue) {
					homoContries.add((String) dlg.cbHomologationCountries.getData(item));
				}
			}

			Map<String, String> input = new LinkedHashMap<String, String>();
			if (dlg.ckbIsAfterSale.getEnabled())
				input.put("vf4_itm_after_sale_relevant", isAfterSale);
			if (dlg.cbDonorVehicle.getEnabled())
				input.put("vf4_donor_vehicle", donorVehicle);
			if (dlg.cbSupplierType.getEnabled())
				input.put("vf4_supplier_type", supplierType);
			if (dlg.cbLongOrShortlead.getEnabled())
				input.put("vf4_long_short_lead", longShortLead);
			if (dlg.cbPartCategory.getEnabled())
				input.put("vf4_part_category", partCategory);
			if (dlg.cbPartTraceability.getEnabled())
				input.put("vf4_item_is_traceable", partTraceable);
			if (dlg.ckbIsHomologation.getEnabled())
				input.put("vf4_homologation_relevant", isHomologation);

			selectedObject.setProperties(input);
			if (dlg.cbHomologationCountries.getEnabled())
				TCExtension.setStringArrayProperty(selectedObject, "vf4_homologation_countries", homoContries.toArray(new String[0]));

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
//			boolean isAFSCheck = false;
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
				dlg.ckbIsAfterSale.setEnabled(false);
				dlg.cbDonorVehicle.setEnabled(false);
				dlg.cbLongOrShortlead.setEnabled(false);
				dlg.cbPartCategory.setEnabled(false);
				dlg.cbSupplierType.setEnabled(false);
				dlg.ckbIsHomologation.setEnabled(false);
				dlg.cbHomologationCountries.setEnabled(false);
				dlg.btnRemoveHologation.setEnabled(false);

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
				dlg.btnInfoIsHomologation.setVisible(true);
				dlg.btnInfoIsHomologation.setToolTipText(mess);
				dlg.btnInfoHomologationCountries.setVisible(true);
				dlg.btnInfoHomologationCountries.setToolTipText(mess);
//				dlg.setMessage("Cannot update information when all revisions of item has been released: "
//						+ selectedObject.getPropertyDisplayableValue("item_id"), IMessageProvider.WARNING);
//				for (Control ctrl : dlg.getShell().getChildren()) {
//					ctrl.setEnabled(false);
//				}
//				return;
			}
			// check part is released
			if (checkPartsReleasedFlag()) {
				traceabilityCheck = true;
				dlg.cbPartTraceability.setEnabled(false);

				dlg.btnInfoPartTraceability.setVisible(true);
				dlg.btnInfoPartTraceability.setToolTipText("Cannot update attribute(s) when part have status: " + String.join(", ", partTraceabilityDisableStatus));
			}

//			if(checkPartsReleasedFlag1()) {
//				isAFSCheck = true;
//				dlg.ckbIsAfterSale.setEnabled(false);
//
//				dlg.btnInfoIsAfterSale.setVisible(true);
//				dlg.btnInfoIsAfterSale.setToolTipText("Cannot update attribute(s) when part have status: " + String.join(", ", isAFSDisableStatus));
//			}
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
			String isHomologation = selectedObject.getPropertyDisplayableValue("vf4_homologation_relevant");
			String homoCountries = selectedObject.getPropertyDisplayableValue("vf4_homologation_countries");

			if (isAfterSale.compareToIgnoreCase("true") == 0 || isAfterSale.compareToIgnoreCase("1") == 0) {
				dlg.ckbIsAfterSale.setSelection(true);
			}
			dlg.cbDonorVehicle.setText(donorVehicle);
			dlg.cbSupplierType.setText(supplierType);
			dlg.cbLongOrShortlead.setText(longShortLead);
			dlg.cbPartCategory.setText(partCategory);
			dlg.cbPartTraceability.setText(partTraceable);
			if (isHomologation.compareToIgnoreCase("true") == 0 || isHomologation.compareToIgnoreCase("1") == 0) {
				dlg.ckbIsHomologation.setSelection(true);
			}
			if (!homoCountries.isEmpty()) {
				if (homoCountries.contains(",")) {
					for (String item : homoCountries.split(",")) {
						if (!item.isEmpty())
							dlg.lstHomologationCountries.add(item);
					}
				} else {
					dlg.lstHomologationCountries.add(homoCountries);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkPartIsReleased() throws NotLoadedException {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Item ID", selectedObject.getPropertyDisplayableValue("item_id"));
		parameter.put("Type", selectedObject.getPropertyDisplayableValue("object_type") + " Revision");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Latest Matured Revision");
		if (item_list == null || item_list.length == 0) {
			return false;
		}
		for (TCComponent tcComponent : item_list) {
			if (!tcComponent.getPropertyDisplayableValue("release_status_list").isEmpty())
				return true;
		}

		return false;
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

	private boolean checkPartsReleasedFlag1() throws TCException, NotLoadedException {
		TCComponentItemRevision[] relRevs = selectedObject.getReleasedItemRevisions();
		for (TCComponentItemRevision item : relRevs) {
			String statusList = item.getPropertyDisplayableValue("release_status_list");
			if (!statusList.isEmpty()) {
				if (Arrays.stream(isAFSDisableStatus).anyMatch(statusList::equals))
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

			String type = selectedObject.getPropertyDisplayableValue("object_type");
			return !Arrays.asList(OBJECT_TYPE_AVAILABLE).contains(type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}
