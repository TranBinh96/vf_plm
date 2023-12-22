package com.teamcenter.vinfast.eecomponent.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class EECompDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	private EECompDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	public EECompDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			dlg = new EECompDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			// ---------------------------- Init -------------------------------------

			LinkedHashMap<String, String> programNameDataForm = new LinkedHashMap<String, String>();
			LinkedHashMap<String, String> docTypeDataForm = new LinkedHashMap<String, String>();
			String[] values = null;
			values = TCExtension.GetPreferenceValues("VINF_COMPDOC_PROGRAM_NAME", session);
			if (values != null && values.length > 0) {
				for (String value : values) {
					if (value.contains("==")) {
						String[] str = value.split("==");
						if (str.length > 1) {
							programNameDataForm.put(str[0], str[0] + " (" + str[1] + ")");
						}
					}
				}
			}

			values = TCExtension.GetPreferenceValues("VINF_COMPDOC_DOC_TYPE", session);
			if (values != null && values.length > 0) {
				for (String value : values) {
					if (value.contains("==")) {
						String[] str = value.split("==");
						if (str.length > 1) {
							docTypeDataForm.put(str[0], str[0] + " (" + str[1] + ")");
						}
					}
				}
			}

			// -----------------------------------------------------------------------

			StringExtension.UpdateValueTextCombobox(dlg.cbProgramName, programNameDataForm);
			dlg.cbProgramName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbDocumentType, docTypeDataForm);
			dlg.cbDocumentType.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!validationCreate()) {
						dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
						return;
					}
					createSpecBook();
				}
			});

			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void createSpecBook() {
		try {

			DataManagementService dms = DataManagementService.getService(session);
			String objectType = "VF3_Compo_doc";
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			revDef.stringProps.put("object_desc", description);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				Boolean addToFolder = false;
				if (selectedObject != null) {
					String type = selectedObject.getProperty("object_type");
					if (type.compareToIgnoreCase("Folder") == 0) {
						try {
							selectedObject.add("contents", cfgContext);
							addToFolder = true;
							dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
				}
				if (!addToFolder) {
					try {
						session.getUser().getNewStuffFolder().add("contents", cfgContext);
					} catch (TCException e1) {
						MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
					}
					dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
					openOnCreate(cfgContext);
				}
				resetDialog();
			} else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (!dlg.cbProgramName.getText().isEmpty() && !dlg.cbDocumentType.getText().isEmpty()) {

			String programName = (String) dlg.cbProgramName.getData(dlg.cbProgramName.getText());
			String docType = (String) dlg.cbDocumentType.getData(dlg.cbDocumentType.getText());

			if (!programName.isEmpty() && !docType.isEmpty()) {
				generateNextID(programName, docType);
			}
		}
	}

	private void generateNextID(String programName, String docType) {
		try {
			String search_Items = "";
			search_Items = programName + docType;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", "VF3_Compo_doc");

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "00001";
			} else {
				int id = 0;
				String split = "";
				split = item_search[0].toString().substring(7, 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 5);
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception ex) {
			System.out.println(" ::: UpdateItemIDTextBox - >  " + ex);
		}
	}

	private void resetDialog() {
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.txtID.setText("");
		dlg.cbProgramName.deselectAll();
		dlg.cbDocumentType.deselectAll();
	}

	private boolean validationCreate() {
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;

		return true;
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
