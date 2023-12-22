package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.subdialog.MassUpdateComboboxValue_Dialog;
import com.vf.utils.TCExtension;

public class DVPRDocumentTestReportUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private DVPRDocumentTestReportUpdate_Dialog dlg;
	private TCComponent selectedObject = null;

	private LinkedHashMap<String, String> bomInfoTemplate = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("", "");
			put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM, "");
			put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME, "");
			put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS, "");
			put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT, "");
			put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT, "");
		}
	};

	public DVPRDocumentTestReportUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			if (selectedObject.getType().compareTo("VF3_AT_DVPR_docRevision") != 0) {
				MessageBox.post("Please select DVPR Document Revision.", "Access", MessageBox.WARNING);
				return null;
			}
			
			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("Current logined user does not have access on the selected object.", "Warning", MessageBox.WARNING);
				return null;
			}

			String[] progressDataForm = TCExtension.GetLovValues("VF4_progress_lov");
			String[] testResultDataForm = TCExtension.GetLovValues("VF4_test_result_lov");

			dlg = new DVPRDocumentTestReportUpdate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Update Test Report Information - " + selectedObject.toString());

			dlg.btnProgressUpdateAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Progress", progressDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnTestResultUpdateAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Test Result", testResultDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectAll();
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
						updateTestReportInfo();
						removeBomline(true);
						selectedObject.refresh();
						dlg.setMessage("Update success.", IMessageProvider.INFORMATION);
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
		for (Map.Entry<String, String> bom : bomInfoTemplate.entrySet()) {
			bomInfo.put(bom.getKey(), bomlineSelect.getText(i));
			i++;
		}

		DVPRDocumentTestReportEdit_Dialog addDlg = new DVPRDocumentTestReportEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, bomInfo);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM, addDlg.txtVFTE.getText());
						put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME, addDlg.txtTestName.getText());
						put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS, addDlg.cbProgress.getText());
						put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT, addDlg.cbTestResult.getText());
						put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT, addDlg.txtComment.getText());
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

	private void massUpdate(String title, String[] values) {
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
						if (title.compareToIgnoreCase("Progress") == 0)
							tableItem.setText(getPosBomInfoTemplate(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Test Result") == 0)
							tableItem.setText(getPosBomInfoTemplate(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT), massUpdateDlg.valueSelected);
					}
				}
			}
		});
	}

	private void updateTestReportInfo() throws Exception {
		TCComponent[] testReportRows = selectedObject.getRelatedComponents(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
		if (testReportRows.length == dlg.tblBom.getItemCount()) {
			for (int i = 0; i < testReportRows.length; i++) {
				TableItem tableItem = dlg.tblBom.getItem(i);
				testReportRows[i].setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM, tableItem.getText(1));
				testReportRows[i].setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME, tableItem.getText(2));
				testReportRows[i].setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS, tableItem.getText(3));
				testReportRows[i].setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT, tableItem.getText(4));
				testReportRows[i].setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT, tableItem.getText(5));
			}
		}

		selectedObject.refresh();
	}

	private void updateCurrentBomline() {
		try {
			TCComponent[] bomInfo = selectedObject.getRelatedComponents(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
			if (bomInfo.length > 0) {
				for (TCComponent bomline : bomInfo) {
					LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
					for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
						if (!bomInfoTemp.getKey().isEmpty()) {
							String value = bomline.getPropertyDisplayableValue(bomInfoTemp.getKey());
							output.put(bomInfoTemp.getKey(), value);
						}
					}
					addTableRow(output, -1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeBomline(boolean removeAll) {
		if (removeAll) {
			dlg.tblBom.removeAll();
		} else {
			int totalDel = 0;
			for (TableItem tableItem : dlg.tblBom.getItems()) {
				if (tableItem.getChecked()) {
					totalDel++;
				}
			}
			for (int j = 0; j < totalDel; j++) {
				int i = 0;
				for (TableItem tableItem : dlg.tblBom.getItems()) {
					if (tableItem.getChecked()) {
						totalDel++;
						dlg.tblBom.remove(i);
						break;
					}
					i++;
				}
			}
		}
		dlg.tblBom.setFocus();
		dlg.tblBom.selectAll();
	}

	private int getPosBomInfoTemplate(String value) {
		if (bomInfoTemplate.containsKey(value)) {
			return new ArrayList<String>(bomInfoTemplate.keySet()).indexOf(value);
		}
		return 0;
	}

	private void addTableRow(LinkedHashMap<String, String> bomline, int addIndex) {
		List<String> tableValue = new LinkedList<String>();
		for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomline.get(bomInfoTemp.getKey());
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
		for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomline.get(bomInfoTemp.getKey());
			}
			tableValue.add(value);
		}

		TableItem item = dlg.tblBom.getItem(editIndex);
		item.setText(tableValue.toArray(new String[0]));
	}
}
