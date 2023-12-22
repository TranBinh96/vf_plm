package com.teamcenter.vinfast.escooter.engineering.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

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
import com.teamcenter.vinfast.handlers.MBOMPartSearchDialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ScooterPartAttributesUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private ScooterPartAttributesUpdate_Dialog dlg;
	private static String OBJECT_TYPE = "VF3_Scooter_part";
	private List<String> idList = new ArrayList<String>();
	private static String[] partTraceabilityDisableStatus = new String[] { "PR", "PPR" };

	public ScooterPartAttributesUpdate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if (!validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one Scooter Part.", "Information", MessageBox.INFORMATION);
			return null;
		}

		try {
			LinkedHashMap<String, String> partMakeBuyDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_make_buy", OBJECT_TYPE, session);
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", OBJECT_TYPE, session);
			LinkedHashMap<String, String> partTraceableDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", OBJECT_TYPE, session);
			// ---------------------------- UI -------------------------------------
			dlg = new ScooterPartAttributesUpdate_Dialog(new Shell());
			dlg.create();

			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			StringExtension.UpdateValueTextCombobox(dlg.cbMakeBuy, partMakeBuyDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleLine, vehicleLineDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceableDataForm);

			dlg.btnAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								MBOMPartSearchDialog partSearch = new MBOMPartSearchDialog();
								partSearch.createAndShowGUI(session, dlg.table, idList, "VF3_Scooter_part;VF3_me_scooter");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			});

			dlg.btnRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.table.remove(dlg.table.getSelectionIndices());
				}
			});

			dlg.btnSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateItem();
				}
			});
			// -----------------------------------------------------------------------
			updateCurrentValue();
			validateUpdate();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}

		return null;
	}

	private void updateItem() {
		try {
			String isAfterSale = dlg.ckbIsAfterSale.getSelection() ? "1" : "0";
			String partMakeBuy = (String) dlg.cbMakeBuy.getData(dlg.cbMakeBuy.getText());
			String vehicleLine = (String) dlg.cbVehicleLine.getData(dlg.cbVehicleLine.getText());
			String partTraceable = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
			String originPart = "";

			if (dlg.table.getItems().length > 0) {
				Set<String> originPartList = new HashSet<String>();
				for (TableItem item : dlg.table.getItems()) {
					originPartList.add(item.getText(0));
				}
				originPart = String.join(";", originPartList);
			}

			Map<String, String> input = new LinkedHashMap<String, String>();
			input.put("vf4_itm_after_sale_relevant", isAfterSale);
			input.put("vf4_item_make_buy", partMakeBuy);
			input.put("vf4_es_model_veh_line", vehicleLine);
			input.put("vf4_orginal_part_number", originPart);
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
				dlg.setMessage("Cannot update information when all revisions of item has been released.", IMessageProvider.WARNING);
				for (Control ctrl : dlg.getShell().getChildren()) {
					ctrl.setEnabled(false);
				}
				return;
			}
			// check part is released
			if (checkPartsReleasedFlag()) {
				dlg.cbPartTraceability.setEnabled(false);

				dlg.btnInfoPartTraceability.setVisible(true);
				dlg.btnInfoPartTraceability.setToolTipText("Cannot update attribute(s) when part have status: " + String.join(", ", partTraceabilityDisableStatus));
			}
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

	private void updateCurrentValue() {
		try {
			String isAfterSale = selectedObject.getPropertyDisplayableValue("vf4_itm_after_sale_relevant");
			String makeBuy = selectedObject.getPropertyDisplayableValue("vf4_item_make_buy");
			String vehicleLine = selectedObject.getPropertyDisplayableValue("vf4_es_model_veh_line");
			String partNumbers = selectedObject.getPropertyDisplayableValue("vf4_orginal_part_number");
			String partTraceable = selectedObject.getPropertyDisplayableValue("vf4_item_is_traceable");
			String[] values = null;
			if (partNumbers.length() != 0) {
				if (partNumbers.contains(";")) {
					values = partNumbers.split(";");
				} else {
					values = new String[] { partNumbers };
				}
				for (String value : values) {
					TableItem tableItem = new TableItem(dlg.table, SWT.NONE);
					tableItem.setText(new String[] { value });
				}
			}

			if (isAfterSale.compareToIgnoreCase("true") == 0 || isAfterSale.compareToIgnoreCase("1") == 0) {
				dlg.ckbIsAfterSale.setSelection(true);
			}
			dlg.cbMakeBuy.setText(makeBuy);
			dlg.cbVehicleLine.setText(vehicleLine);
			dlg.cbPartTraceability.setText(partTraceable);
		} catch (Exception e) {

		}
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
