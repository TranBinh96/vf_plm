package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.administration.PreferenceManagementService;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.SetPreferences2In;
import com.teamcenter.soa.client.model.ServiceData;
import com.vf.utils.TCExtension;

public class UpdateALForModuleValidate_Handler extends AbstractHandler {
	private TCSession session;
	private UpdateALForModuleValidate_Dialog dlg;
	private LinkedHashMap<String, LinkedHashMap<String, Set<String>>> preValueList;
	private Set<String> moduleLevel1Unique;
	private Set<String> moduleLevel2Unique;
	private Set<String> moduleLevel3Unique;
	private LinkedHashMap<String, String> excelInList;
	private LinkedHashMap<String, String> lovInList;

	private static String GROUP_PERMISSION = "dba";
	private String VF_PART_CREATE_MODULE_VALIDATE = "VF_PART_CREATE_MODULE_VALIDATE";
	private String[] lovModuleL1;
	private String[] lovModuleL2;
	private String[] lovModuleL3;

	public UpdateALForModuleValidate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!checkPermission()) {
			MessageBox.post("You are not authorized to import AL preference.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}
		// init data
		try {
			lovModuleL1 = TCExtension.GetLovValues("VL5_module_group_english");
			lovModuleL2 = TCExtension.GetLovValues("VL5_main_module_english");
			lovModuleL3 = TCExtension.GetLovValues("VL5_module_english");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init UI
		dlg = new UpdateALForModuleValidate_Dialog(new Shell());
		dlg.create();
		dlg.txtPreference.setText(VF_PART_CREATE_MODULE_VALIDATE);

		dlg.btnOpenFile.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExt = { "*.xlsx", "*.xls" };
				fileDialog.setFilterExtensions(filterExt);
				readExcelFile(fileDialog.open());
			}
		});

		dlg.btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updatePreference();
			}
		});

		dlg.btnExcel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportExcel();
			}
		});

		dlg.open();
		return null;
	}

	private void updatePreference() {
		List<String> preferenceValues = new LinkedList<String>();
		for (Map.Entry<String, LinkedHashMap<String, Set<String>>> preference : preValueList.entrySet()) {
			String moduleL1 = preference.getKey();
			for (Map.Entry<String, Set<String>> moduleL2 : preference.getValue().entrySet()) {
				for (String moduleL3 : moduleL2.getValue()) {
					preferenceValues.add(moduleL1 + ";" + moduleL2.getKey() + ";" + moduleL3);
				}
			}
		}

		if (preValueList.size() > 1) {
			SetPreferences2In prefeItem = new SetPreferences2In();
			prefeItem.preferenceName = VF_PART_CREATE_MODULE_VALIDATE;
			prefeItem.values = preferenceValues.toArray(new String[0]);
			PreferenceManagementService prefService = PreferenceManagementService.getService(session);
			ServiceData response = prefService.setPreferences2(new SetPreferences2In[] { prefeItem });
			if (response.sizeOfPartialErrors() > 0) {
				for (int i = 0; i < response.sizeOfPartialErrors(); i++) {
					for (String msg : response.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			} else {
				dlg.setMessage("Preference update success", IMessageProvider.INFORMATION);
			}
		}
	}

	private void readExcelFile(String path) {
		preValueList = new LinkedHashMap<String, LinkedHashMap<String, Set<String>>>();
		excelInList = new LinkedHashMap<String, String>();
		lovInList = new LinkedHashMap<String, String>();
		moduleLevel1Unique = new HashSet<String>();
		moduleLevel2Unique = new HashSet<String>();
		moduleLevel3Unique = new HashSet<String>();

		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				@SuppressWarnings("resource")
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int cellCount = 0;
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					cellCount = 0;
					if (rowCount > 1) {
						String moduleL1Value = "";
						String moduleL2Value = "";
						String moduleL3Value = "";
						for (Cell cell : row) {
							cellCount++;
							if (cellCount == 1) {
								moduleL1Value = cell.getStringCellValue();
							} else if (cellCount == 2) {
								moduleL2Value = cell.getStringCellValue();
							} else if (cellCount == 3) {
								moduleL3Value = cell.getStringCellValue();
							}
						}
						updatePreferenceValue(moduleL1Value, moduleL2Value, moduleL3Value);
					}
				}
				getHaveLOVNotInExcel();
				updateMessageTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void updatePreferenceValue(String moduleL1Value, String moduleL2Value, String moduleL3Value) {
		boolean validate = true;
		if (!validateLOV(moduleL1Value, lovModuleL1)) {
			excelInList.put(moduleL1Value, "Module Level 1");
//			validate = false;
		}
		if (!validateLOV(moduleL2Value, lovModuleL2)) {
			excelInList.put(moduleL2Value, "Module Level 2");
//			validate = false;
		}
		if (!validateLOV(moduleL3Value, lovModuleL3)) {
			excelInList.put(moduleL3Value, "Module Level 3");
//			validate = false;
		}
		if (validate) {
			moduleLevel1Unique.add(moduleL1Value);
			moduleLevel2Unique.add(moduleL2Value);
			moduleLevel3Unique.add(moduleL3Value);

			LinkedHashMap<String, Set<String>> moduleL2 = preValueList.get(moduleL1Value);
			if (moduleL2 == null) {
				moduleL2 = new LinkedHashMap<String, Set<String>>();
				preValueList.put(moduleL1Value, moduleL2);
			}

			Set<String> moduleL3 = moduleL2.get(moduleL2Value);
			if (moduleL3 == null) {
				moduleL3 = new HashSet<String>();
				moduleL2.put(moduleL2Value, moduleL3);
			}

			moduleL3.add(moduleL3Value);
		}
	}

	private void getHaveLOVNotInExcel() {
		for (String moduleLovItem : lovModuleL1) {
			if (!moduleLevel1Unique.contains(moduleLovItem))
				lovInList.put(moduleLovItem, "Module Level 1");
		}

		for (String moduleLovItem : lovModuleL2) {
			if (!moduleLevel2Unique.contains(moduleLovItem))
				lovInList.put(moduleLovItem, "Module Level 2");
		}

		for (String moduleLovItem : lovModuleL3) {
			if (!moduleLevel3Unique.contains(moduleLovItem))
				lovInList.put(moduleLovItem, "Module Level 3");
		}
	}

	private boolean validateLOV(String value, String[] lov) {
		if (value.isEmpty())
			return false;
		for (String item : lov) {
			if (item.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	private void updateMessageTable() {
		dlg.tblExcelIn.removeAll();
		dlg.tblExcelIn.redraw();
		if (excelInList != null) {
			for (Map.Entry<String, String> entry : sorting(excelInList).entrySet()) {
				TableItem item = new TableItem(dlg.tblExcelIn, SWT.NONE);
				item.setText(new String[] { entry.getKey(), entry.getValue() });
			}
		}

		dlg.tblLOVIn.removeAll();
		dlg.tblLOVIn.redraw();
		if (lovInList != null) {
			for (Map.Entry<String, String> entry : sorting(lovInList).entrySet()) {
				TableItem item = new TableItem(dlg.tblLOVIn, SWT.NONE);
				item.setText(new String[] { entry.getKey(), entry.getValue() });
			}
		}
	}

	private boolean checkPermission() {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			if (group.toString().compareToIgnoreCase(GROUP_PERMISSION) == 0)
				return true;
		} catch (Exception e) {

		}
		return false;
	}

	private LinkedHashMap<String, String> sorting(LinkedHashMap<String, String> output) {
		List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(output.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> a, Entry<String, String> b) {
				return a.getKey().compareTo(b.getKey());
			}
		});
		LinkedHashMap<String, String> sortedMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private void exportExcel() {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet spreadsheet = wb.createSheet("Data");

		int i = 0;
		int j = 0;
		String[] headers = { "Value", "Level" };
		XSSFRow headerRow = spreadsheet.createRow(i++);
		for (String header : headers) {
			XSSFCell cell1 = headerRow.createCell(j++);
			cell1.setCellValue(header);
		}

		for (TableItem row : dlg.tblLOVIn.getItems()) {
			XSSFRow bodyRow = spreadsheet.createRow(i++);

			for (int k = 0; k < headers.length; k++) {
				XSSFCell cell1 = bodyRow.createCell(k);
				cell1.setCellValue(row.getText(k));
			}
		}

		try {
			File file = new File(System.getenv("tmp") + "AL_Export.xlsx");
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			Desktop.getDesktop().open(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
