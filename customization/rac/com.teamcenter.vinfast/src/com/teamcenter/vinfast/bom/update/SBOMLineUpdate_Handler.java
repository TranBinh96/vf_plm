package com.teamcenter.vinfast.bom.update;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.vinfast.subdialog.MassUpdateComboboxValue_Dialog;
import com.teamcenter.vinfast.subdialog.MassUpdateDateTimeValue_Dialog;
import com.teamcenter.vinfast.subdialog.MassUpdateListValue_Dialog;
import com.teamcenter.vinfast.subdialog.MassUpdateTextValue_Dialog;
import com.vf.utils.TCExtension;

public class SBOMLineUpdate_Handler extends AbstractHandler {
	enum MASSUPDATE_TYPE {
		COMBOBOX, TEXT, LIST, DATETIME;
	}

	private TCSession session;
	private SBOMLineUpdate_Dialog dlg;
	private LinkedList<TCComponentBOMLine> selectedObjects = null;

	private LinkedHashMap<String, Boolean> bomInfoTemplate = new LinkedHashMap<String, Boolean>() {
		private static final long serialVersionUID = 1L;
		{
			put("", false);
			put("bl_item_item_id", false);
			put("bl_rev_item_revision_id", false);
			put("bl_item_object_name", false);
			put("VF3_pos_ID", false);
			put("bl_level_starting_0", false);
			put("bl_quantity", false);
			put("bl_item_vf4_orginal_part_number", false);
			put("VF4_supersessionCode", true);
			put("VF4_effective_date", true);
			put("VF4_valid_till_date", true);
			put("VF4_market", true);
			put("VF4_first_vin_newp", true);
			put("VF4_LNT_af_critical", true);
			put("VF4_EPC_Variant", true);
			put("VF4_remarks", true);
		}
	};

	private LinkedHashMap<String, String> superSessionDataForm = null;

	public SBOMLineUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObjects = new LinkedList<>();
			for (InterfaceAIFComponent item : targetComp) {
				if (item instanceof TCComponentBOMLine) {
					selectedObjects.add((TCComponentBOMLine) item);
				}
			}
			// Init data
			superSessionDataForm = TCExtension.GetLovValueAndDisplay("VF4_supersession_code");
			String[] marketDataForm = TCExtension.GetPreferenceValues("VINF_SBOM_Info_Market", session);
			String[] aftersaleDataForm = TCExtension.GetLovValues("VF4_Critical");
			String[] epcVariantDataForm = TCExtension.GetLovValues("VF4_EPC_Variant_LOV");
			// Init UI
			dlg = new SBOMLineUpdate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Update SBOM Information");

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectAll();
				}
			});

			dlg.btnSupersession.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					massUpdate("Supersession", "VF4_supersessionCode", superSessionDataForm.values().toArray(new String[0]), MASSUPDATE_TYPE.COMBOBOX);
				}
			});

			dlg.btnAftersalesCritical.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					massUpdate("Aftersales Critical", "VF4_LNT_af_critical", aftersaleDataForm, MASSUPDATE_TYPE.COMBOBOX);
				}
			});

			dlg.btnEpcVariant.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					massUpdate("EPC Variant", "VF4_EPC_Variant", epcVariantDataForm, MASSUPDATE_TYPE.COMBOBOX);
				}
			});

			dlg.btn1stVINNewPart.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					massUpdate("1st VIN New Part", "VF4_first_vin_newp", null, MASSUPDATE_TYPE.TEXT);
				}
			});

			dlg.btnRemarks.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					massUpdate("Remarks", "VF4_remarks", null, MASSUPDATE_TYPE.TEXT);
				}
			});

			dlg.btnMarket.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					massUpdate("Market", "VF4_market", marketDataForm, MASSUPDATE_TYPE.LIST);
				}
			});

			dlg.btnEffectiveDate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					massUpdate("Effective Date", "VF4_effective_date", null, MASSUPDATE_TYPE.DATETIME);
				}
			});

			dlg.btnValidtillDate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					massUpdate("Valid till Date", "VF4_valid_till_date", null, MASSUPDATE_TYPE.DATETIME);
				}
			});

			dlg.tblBom.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (dlg.tblBom.getSelectionIndex() >= 0)
						editBomlineInfo();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						updateBomInfo();
					} catch (Exception ex) {
						dlg.setMessage(ex.toString());
						ex.printStackTrace();
					}
				}
			});

			updateCurrentBomline();
			selectAll();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void editBomlineInfo() {
		int editIndex = dlg.tblBom.getSelectionIndex();
		TableItem bomlineSelect = dlg.tblBom.getItem(editIndex);
		LinkedHashMap<String, String> bomInfo = new LinkedHashMap<String, String>();
		int i = 0;
		for (Map.Entry<String, Boolean> bom : bomInfoTemplate.entrySet()) {
			bomInfo.put(bom.getKey(), bomlineSelect.getText(i));
			i++;
		}

		SBOMLineEdit_Dialog addDlg = new SBOMLineEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, bomInfo);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put("bl_item_item_id", addDlg.txtPartNo.getText());
						put("bl_rev_item_revision_id", addDlg.txtRevision.getText());
						put("bl_item_object_name", addDlg.txtPartName.getText());
						put("VF3_pos_ID", addDlg.txtPOSID.getText());
						put("bl_level_starting_0", addDlg.txtLevel.getText());
						put("bl_quantity", addDlg.txtQuantity.getText());
						put("bl_item_vf4_orginal_part_number", addDlg.txtOriginalPart.getText());
						put("VF4_supersessionCode", addDlg.cbSupersession.getText());
						if (addDlg.ckbEffectiveDate.getSelection())
							put("VF4_effective_date", getDatetimeFromWidget(addDlg.datEffectiveDate));
						else
							put("VF4_effective_date", "");
						if (addDlg.ckbValidtillDate.getSelection())
							put("VF4_valid_till_date", getDatetimeFromWidget(addDlg.datValidtillDate));
						else
							put("VF4_valid_till_date", "");
						put("VF4_market", String.join(", ", addDlg.lstMarket.getItems()));
						put("VF4_1st_VIN_new_part", addDlg.txt1stVINNewPart.getText());
						put("VF4_LNT_af_critical", addDlg.cbAftersalesCritical.getText());
						put("VF4_EPC_Variant", addDlg.cbEPCVariant.getText());
						put("VF4_remarks", addDlg.txtRemarks.getText());
					}
				};
				addDlg.close();
				editTableRow(bomline, editIndex);
			}
		});
	}

	private void selectAll() {
		for (TableItem item : dlg.tblBom.getItems()) {
			item.setChecked(dlg.ckbCheckAll.getSelection());
		}
	}

	private void massUpdate(String title, String property, String[] values, MASSUPDATE_TYPE type) {
		try {
			if (type == MASSUPDATE_TYPE.COMBOBOX) {
				MassUpdateComboboxValue_Dialog massUpdateDlg = new MassUpdateComboboxValue_Dialog(dlg.getShell());
				massUpdateDlg.create();
				massUpdateDlg.lblValue.setText(title);
				massUpdateDlg.cbValue.setItems(values);
				massUpdateDlg.open();
				Button okButton = massUpdateDlg.getOKButton();
				okButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						for (TableItem tableItem : dlg.tblBom.getItems()) {
							if (tableItem.getChecked()) {
								tableItem.setText(getPosBomInfoTemplate(property), massUpdateDlg.valueSelected);
							}
						}
						massUpdateDlg.close();
					}
				});
			} else if (type == MASSUPDATE_TYPE.TEXT) {
				MassUpdateTextValue_Dialog massUpdateDlg = new MassUpdateTextValue_Dialog(dlg.getShell());
				massUpdateDlg.create();
				massUpdateDlg.lblValue.setText(title);
				massUpdateDlg.open();
				Button okButton = massUpdateDlg.getOKButton();
				okButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						for (TableItem tableItem : dlg.tblBom.getItems()) {
							if (tableItem.getChecked()) {
								tableItem.setText(getPosBomInfoTemplate(property), massUpdateDlg.txtValue.getText());
							}
						}
						massUpdateDlg.close();
					}
				});
			} else if (type == MASSUPDATE_TYPE.LIST) {
				MassUpdateListValue_Dialog massUpdateDlg = new MassUpdateListValue_Dialog(dlg.getShell());
				massUpdateDlg.create();
				massUpdateDlg.lblValue.setText(title);
				massUpdateDlg.cbValue.setItems(values);
				massUpdateDlg.open();
				Button okButton = massUpdateDlg.getOKButton();
				okButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						for (TableItem tableItem : dlg.tblBom.getItems()) {
							if (tableItem.getChecked()) {
								tableItem.setText(getPosBomInfoTemplate(property), String.join(", ", massUpdateDlg.lstValue.getItems()));
							}
						}
						massUpdateDlg.close();
					}
				});
			} else if (type == MASSUPDATE_TYPE.DATETIME) {
				MassUpdateDateTimeValue_Dialog massUpdateDlg = new MassUpdateDateTimeValue_Dialog(dlg.getShell());
				massUpdateDlg.create();
				massUpdateDlg.lblValue.setText(title);
				massUpdateDlg.open();
				Button okButton = massUpdateDlg.getOKButton();
				okButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						for (TableItem tableItem : dlg.tblBom.getItems()) {
							if (tableItem.getChecked()) {
								String dateValue = "";
								if (massUpdateDlg.valueCheck) {
									dateValue = getDatetimeFromWidget(massUpdateDlg.datValue);
								}
								tableItem.setText(getPosBomInfoTemplate(property), dateValue);
							}
						}
						massUpdateDlg.close();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateBomInfo() throws Exception {
		for (int i = 0; i < dlg.tblBom.getItemCount(); i++) {
			try {
				TCComponentBOMLine bomLine = selectedObjects.get(i);
				for (Map.Entry<String, Boolean> entry : bomInfoTemplate.entrySet()) {
					if (entry.getValue()) {
						if (entry.getKey().compareTo("VF4_supersessionCode") == 0) {
							bomLine.setProperty(entry.getKey(), getIndexOfSupersession(dlg.tblBom.getItem(i).getText(getPosBomInfoTemplate(entry.getKey()))));
						} else {
							bomLine.setProperty(entry.getKey(), dlg.tblBom.getItem(i).getText(getPosBomInfoTemplate(entry.getKey())));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dlg.setMessage("Update Bom attribute success.", IMessageProvider.INFORMATION);
	}

	private void updateCurrentBomline() {
		try {
			for (int i = 0; i < selectedObjects.size(); i++) {
				TCComponentBOMLine bomline = selectedObjects.get(i);
				LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
				for (Map.Entry<String, Boolean> bomInfoTemp : bomInfoTemplate.entrySet()) {
					if (!bomInfoTemp.getKey().isEmpty()) {
						String value = bomline.getPropertyDisplayableValue(bomInfoTemp.getKey());
						output.put(bomInfoTemp.getKey(), value);
					}
				}
				addTableRow(output, i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIndexOfSupersession(String value) {
		for (Map.Entry<String, String> entry : superSessionDataForm.entrySet()) {
			if (entry.getValue().compareTo(value) == 0)
				return entry.getKey();
		}
		return "";
	}

	private int getPosBomInfoTemplate(String value) {
		if (bomInfoTemplate.containsKey(value)) {
			return new ArrayList<String>(bomInfoTemplate.keySet()).indexOf(value);
		}
		return 0;
	}

	private void addTableRow(LinkedHashMap<String, String> bomline, int addIndex) {
		List<String> tableValue = new LinkedList<String>();
		tableValue.add("");
		for (Map.Entry<String, String> bomInfoTemp : bomline.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomInfoTemp.getValue();
			}
			tableValue.add(value);
		}

		TableItem item = null;
		if (addIndex < 0)
			item = new TableItem(dlg.tblBom, SWT.NONE);
		else
			item = new TableItem(dlg.tblBom, SWT.NONE, addIndex);
		item.setText(tableValue.toArray(new String[0]));
	}

	private void editTableRow(LinkedHashMap<String, String> bomline, int editIndex) {
		List<String> tableValue = new LinkedList<String>();
		tableValue.add("");
		for (Map.Entry<String, String> bomInfoTemp : bomline.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomInfoTemp.getValue();
			}
			tableValue.add(value);
		}

		TableItem item = dlg.tblBom.getItem(editIndex);
		item.setText(tableValue.toArray(new String[0]));
	}

	private String getDatetimeFromWidget(DateTime control) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, control.getYear());
		cal.set(Calendar.MONTH, control.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, control.getDay());
		return format.format(cal.getTime());
	}
}
