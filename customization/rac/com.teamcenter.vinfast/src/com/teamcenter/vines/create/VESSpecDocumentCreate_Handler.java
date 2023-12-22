package com.teamcenter.vines.create;

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

public class VESSpecDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	String[] prefixNameDataForm;
	LinkedHashMap<String, String> prefixNameValue;
	LinkedHashMap<String, String> modelCodeValue;
	LinkedHashMap<String, String> moduleNameValue;
	VESSpecDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	public VESSpecDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			dlg = new VESSpecDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			// ---------------------------- Init -------------------------------------
			String[] typeDataForm = TCExtension.GetPreferenceValues("VES_SPECBOOK_VEHICLE_TYPE", session);
			prefixNameDataForm = TCExtension.GetPreferenceValues("VES_SPECBOOK_PREFIX_NAME", session);
			LinkedHashMap<String, String> indexDataForm = TCExtension.GetLovValueAndDescription("vf3_index", "VF3_VINES_doc", session);
			LinkedHashMap<String, String> modelCodeDataForm = TCExtension.GetLovValueAndDescription("vf3_model_code", "VF3_VINES_doc", session);
			LinkedHashMap<String, String> moduleNameDataForm = TCExtension.GetLovValueAndDescription("vf3_module_name", "VF3_VINES_doc", session);
			// -----------------------------------------------------------------------
			dlg.cbType.setItems(typeDataForm);
			dlg.cbType.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateCombobox();
				}
			});			

			dlg.cbPrefixName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbIndex, indexDataForm);
			dlg.cbIndex.addModifyListener(new ModifyListener() {
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
					if (!validationCreate()) {
						dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
						return;
					}
					createSpecBook();
				}
			});
			// -----------------------------------------------------------------------
			dlg.cbType.setText(typeDataForm[0]);
			updateCombobox();

			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void createSpecBook() {
		try {
			if (checkIDExist(dlg.txtID.getText())) {
				dlg.setMessage("ID exists in Teamcenter", IMessageProvider.WARNING);
			} else {
				DataManagementService dms = DataManagementService.getService(session);
				String objectType = "VF3_VINES_doc";
				String id = dlg.txtID.getText();
				String name = dlg.txtName.getText();
				String description = dlg.txtDescription.getText();

				String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());
				String index = (String) dlg.cbIndex.getData(dlg.cbIndex.getText());
				String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
				String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());

				objectType = StringExtension.GetObjectTypeReal(prefixNameDataForm, prefixName);
				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = objectType;
				itemDef.data.stringProps.put("item_id", id);
				itemDef.data.stringProps.put("object_name", name);
				itemDef.data.stringProps.put("object_desc", description);
				itemDef.data.stringProps.put("vf3_index", index);
				itemDef.data.stringProps.put("vf3_model_code", modelCode);
				itemDef.data.stringProps.put("vf3_module_name", moduleName);

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
								dlg.setMessage(
										"Created successfully, new item has been copied to "
												+ selectedObject.getProperty("object_name") + " folder",
										IMessageProvider.INFORMATION);
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
						dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder",
								IMessageProvider.INFORMATION);
					}
					updateCombobox();
					dlg.txtName.setText("");
					dlg.txtDescription.setText("");
					dlg.txtID.setText("");
				} else {
					ServiceData serviceData = response.serviceData;
					for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
						for (String msg : serviceData.getPartialError(i).getMessages()) {
							MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
						}
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (!dlg.cbPrefixName.getText().isEmpty() && !dlg.cbIndex.getText().isEmpty()
				&& !dlg.cbModelCode.getText().isEmpty() && !dlg.cbModuleName.getText().isEmpty()) {

			String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());
			String index = (String) dlg.cbIndex.getData(dlg.cbIndex.getText());
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());

			if (!prefixName.isEmpty() && !modelCode.isEmpty() && !moduleName.isEmpty()) {
				generateNextID(dlg.cbType.getText(), prefixName, index, modelCode, moduleName);
			}
		}
	}

	private void generateNextID(String vehicleCategory, String prefixName, String index, String modelCode, String moduleName) {
		try {
			String search_Items = "";
			search_Items = prefixName + index + modelCode + moduleName;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", StringExtension.GetObjectTypeReal(prefixNameDataForm, prefixName));

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "0001";
			} else {
				int id = 0;
				String split = "";
				split = item_search[0].toString().substring(10, 14);

				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception ex) {
			System.out.println(" ::: UpdateItemIDTextBox - >  " + ex);
		}
	}

	private Boolean checkIDExist(String newIDValue) {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Item ID", newIDValue);
		parameter.put("Type", "Spec Document");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Item...");// getItemList(session, newIDValue);
		if (item_list == null || item_list.length == 0) {
			return false;
		}
		return true;
	}

	private void updateCombobox() {
		resetDialog();
		String vehType = dlg.cbType.getText();
		prefixNameValue = StringExtension.GetComboboxValue(prefixNameDataForm, vehType);

		StringExtension.UpdateValueTextCombobox(dlg.cbPrefixName, prefixNameValue);
	}

	private void resetDialog() {
		dlg.cbPrefixName.deselectAll();
		dlg.cbIndex.deselectAll();
		dlg.cbModelCode.deselectAll();
		dlg.cbModuleName.deselectAll();
	}

	private boolean validationCreate() {
		if (dlg.txtName.getText().isEmpty())
			return false;

		return true;
	}
}
