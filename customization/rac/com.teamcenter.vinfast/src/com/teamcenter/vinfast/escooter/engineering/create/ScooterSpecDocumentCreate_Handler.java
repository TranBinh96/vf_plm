package com.teamcenter.vinfast.escooter.engineering.create;

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

public class ScooterSpecDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ScooterSpecDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	private LinkedHashMap<String, String> objectTypeMapping = null;
	private String VEHICLE_CATEGORY = "E-SCOOTER";

	public ScooterSpecDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			// init Data
			objectTypeMapping = new LinkedHashMap<>();

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
			dlg = new ScooterSpecDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbPrefixName, prefixNameDataForm);
			dlg.cbPrefixName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbModelCode, modelCodeDataForm);
			dlg.cbModelCode.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbModuleName, moduleNameDataForm);
			dlg.cbModuleName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			// -----------------------------------------------------------------------
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void createNewItem() {
		if (!validationCreate()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		try {
			DataManagementService dms = DataManagementService.getService(session);
			String objectType = "VF3_spec_doc";
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();

			String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());

			if (objectTypeMapping.containsKey(prefixName))
				objectType = objectTypeMapping.get(prefixName);

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf3_veh_category", VEHICLE_CATEGORY);
			itemDef.data.stringProps.put("vf3_doc_type", prefixName);
			itemDef.data.stringProps.put("vf3_model_code", modelCode);
			itemDef.data.stringProps.put("vf3_module_name", moduleName);

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			revDef.stringProps.put("object_desc", description);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
				return;
			}
			TCComponent cfgContext = response.output[0].objects[0];
			Boolean addToFolder = false;
			if (selectedObject != null) {
				String type = selectedObject.getProperty("object_type");
				if (type.compareToIgnoreCase("Folder") == 0) {
					try {
						selectedObject.add("contents", cfgContext);
						addToFolder = true;
						dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder", IMessageProvider.INFORMATION);
						if (dlg.ckbOpenOnCreate.getSelection())
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
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (!dlg.cbPrefixName.getText().isEmpty() && !dlg.cbModelCode.getText().isEmpty() && !dlg.cbModuleName.getText().isEmpty()) {

			String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());

			if (!prefixName.isEmpty() && !modelCode.isEmpty() && !moduleName.isEmpty()) {
				generateNextID(prefixName, modelCode, moduleName);
			}
		}
	}

	private void generateNextID(String prefixName, String modelCode, String moduleName) {
		try {
			String search_Items = "";
			search_Items = prefixName + modelCode + moduleName;
			String objectType = objectTypeMapping.getOrDefault(prefixName, "VF3_spec_doc");

			if (prefixName.compareTo("VFDS") == 0) {
				objectType += ";VF3_AT_DVPR_doc";
			}

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", objectType);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "001";
			} else {
				int id = 0;
				String split = "";
				split = item_search[0].toString().substring(10, 13);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 3);
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbPrefixName.deselectAll();
		dlg.cbModelCode.deselectAll();
		dlg.cbModuleName.deselectAll();
	}

	private boolean validationCreate() {
		if (dlg.txtName.getText().isEmpty())
			return false;

		return true;
	}
}