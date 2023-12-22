package com.teamcenter.vinfast.admin;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.teamcenter.vinfast.model.UpdateALForWorkprocess_Model;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class UpdateALForWorkprocess_Handler extends AbstractHandler {
	private TCSession session;
	private UpdateALForWorkprocess_Dialog dlg;

	//
	private int conditionTemplate;
	private int gateTemplate;
	private LinkedList<UpdateALForWorkprocess_Model> preValueList;
	private HashMap<String, String> notvalidList;
	private String objectTypePrefeRow = "";
	private String conditionPrefeRow = "";
	private String gatePrefeRow = "";

	private static String GROUP_PERMISSION = "dba";
	//
	private static String WORKPROCESS_PREFERENCE_MANAGEMENT = "VINF_WORKPROCESS_PREFERENCE_MANAGEMENT";

	public UpdateALForWorkprocess_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!CheckPermission()) {
			MessageBox.post("You are not authorized to import AL preference.",
					"Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}

		dlg = new UpdateALForWorkprocess_Dialog(new Shell());
		dlg.create();

		// init data
		preValueList = new LinkedList<UpdateALForWorkprocess_Model>();
		notvalidList = new HashMap<String, String>();
		conditionTemplate = 0;
		gateTemplate = 0;
		String[] preference = TCExtension.GetPreferenceValues(WORKPROCESS_PREFERENCE_MANAGEMENT, session);
		LinkedHashMap<String, String> preferenceFormData = new LinkedHashMap<String, String>();
		if (preference != null && preference.length > 0) {
			for (String value : preference) {
				String[] str = value.split(";");
				if (str.length > 1) {
					preferenceFormData.put(str[1], str[0]);
				}
			}
		}
		// init UI
		if (preferenceFormData != null) {
			StringExtension.UpdateValueTextCombobox(dlg.cbWorkprocess, preferenceFormData);
		}
		dlg.cbWorkprocess.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				LoadPreferenceOfWorkprocess();
			}
		});

		dlg.btnOpenFile.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExt = { "*.xlsx", "*.xls" };
				fileDialog.setFilterExtensions(filterExt);
				ReadExcelFile(fileDialog.open());
			}
		});

		dlg.btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				UpdatePreference();
			}
		});

		dlg.open();
		return null;
	}

	private void UpdatePreference() {
		String[] preferenceValues = new String[preValueList.size() + 3];
		preferenceValues[0] = objectTypePrefeRow;
		preferenceValues[1] = conditionPrefeRow;
		preferenceValues[2] = gatePrefeRow;
		int j = 2;
		for (UpdateALForWorkprocess_Model item : preValueList) {
			j++;
			preferenceValues[j] = item.GetConditions() + "=" + item.GetGates();
		}

		if (preValueList.size() > 1) {
			SetPreferences2In prefeItem = new SetPreferences2In();
			prefeItem.preferenceName = (String) dlg.cbWorkprocess.getData(dlg.cbWorkprocess.getText());
			prefeItem.values = preferenceValues;
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

	private void ReadExcelFile(String path) {
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				@SuppressWarnings("resource")
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					if (rowCount > 1) {
						UpdateALForWorkprocess_Model newItem = new UpdateALForWorkprocess_Model(session);
						String error = "";
						for (int i = 0; i < conditionTemplate; i++) {
							newItem.AddCondition(row.getCell(i).getStringCellValue());
						}
						for (int i = conditionTemplate; i < conditionTemplate + gateTemplate; i++) {
							String value = "";
							try {
								value = row.getCell(i).getStringCellValue();
							} catch (Exception e) {
							}

							String result = newItem.AddGate(value);
							if (!result.isEmpty()) {
								if (!error.isEmpty()) {
									error += ", ";
								}
								error += result;
							}
						}
						if (error.isEmpty()) {
							preValueList.add(newItem);
						} else {
							notvalidList.put(String.valueOf(rowCount), error);
						}
					}
				}
				UpdateMessageTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void UpdateMessageTable() {
		dlg.tblMessage.removeAll();
		dlg.tblMessage.redraw();
		if (notvalidList != null) {
			for (Map.Entry<String, String> entry : notvalidList.entrySet()) {
				TableItem item = new TableItem(dlg.tblMessage, SWT.NONE);
				item.setText(new String[] { entry.getKey(), entry.getValue() });
			}
		} else {

		}
	}

	private void LoadPreferenceOfWorkprocess() {
		if (!dlg.cbWorkprocess.getText().isEmpty()) {
			String preferenceName = (String) dlg.cbWorkprocess.getData(dlg.cbWorkprocess.getText());
			String[] values = TCExtension.GetPreferenceValues(preferenceName, session);
			if (values.length >= 3) {
				objectTypePrefeRow = values[0];
				conditionPrefeRow = values[1];
				if (!values[1].isEmpty()) {
					conditionTemplate = GetTemplate(values[1]);
				}
				gatePrefeRow = values[2];
				if (!values[2].isEmpty()) {
					gateTemplate = GetTemplate(values[2]);
				}
			}
		} else {

		}
	}

	private int GetTemplate(String input) {
		String[] condition = input.split(";");
		if (condition.length > 0) {
			return condition.length;
		}
		return 1;
	}

	private boolean CheckPermission() {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			if (group.toString().compareToIgnoreCase(GROUP_PERMISSION) == 0)
				return true;
		} catch (Exception e) {
		}
		return false;
	}
}
