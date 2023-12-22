package com.teamcenter.vinfast.general.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchDocument_Dialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SpecDocument_Handler extends AbstractHandler {
	private TCSession session;
	String[] prefixNameDataForm;
	String[] modeCodeDataForm;
	String[] moduleNameDataForm;
	LinkedHashMap<String, String> prefixNameValue;
	LinkedHashMap<String, String> modelCodeValue;
	LinkedHashMap<String, String> moduleNameValue;
	SpecDocument_Dialog dlg;
	private TCComponent selectedObject = null;
	private TCComponent specbook = null;

	public SpecDocument_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {

			ISelection selection = HandlerUtil.getCurrentSelection(arg0);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			session = (TCSession) selectedObjects[0].getSession();

			dlg = new SpecDocument_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			// ---------------------------- Init -------------------------------------
			String[] vehicleCategoryDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_VEHICLE_TYPE", session);
			prefixNameDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_PREFIX_NAME", session);
			modeCodeDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODEL_CODE", session);
			moduleNameDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODULE_NAME", session);
			// -----------------------------------------------------------------------
			dlg.cbVehicleCategory.setItems(vehicleCategoryDataForm);
			dlg.cbVehicleCategory.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateCombobox();
				}
			});

			dlg.cbPrefixName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateAddingNumberUI();
					checkComboboxToGenID();
				}
			});

			dlg.cbModelCode.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

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

			dlg.btnAddingSearch.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					SearchAdding();
				}
			});

			dlg.btnAddingRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.txtAddingNumber.setText("");
					if (getPrefixName().compareToIgnoreCase("VFTE") == 0) {
						dlg.cbModelCode.deselectAll();
						dlg.cbModuleName.deselectAll();
					}
				}
			});
			// -----------------------------------------------------------------------
			dlg.cbVehicleCategory.setText("AUTOMOBILE");
			updateCombobox();
			updateAddingNumberUI();
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
			String objectType = "VF3_spec_doc";
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			String vehicleCategory = dlg.cbVehicleCategory.getText();

			String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());// StringExtension.GetValueFromText(dlg.cbPrefixName.getText());
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());// StringExtension.GetValueFromText(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());// StringExtension.GetValueFromText(dlg.cbModuleName.getText());

			objectType = StringExtension.GetObjectTypeReal(prefixNameDataForm, prefixName);
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf3_veh_category", vehicleCategory);
			itemDef.data.stringProps.put("vf3_doc_type", prefixName);
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
				if (prefixName.compareToIgnoreCase("VFDS") == 0) {
					linkSORNumber(cfgContext);
				} else if (prefixName.compareToIgnoreCase("VFTE") == 0) {
					linkDVPNumber(cfgContext);
				}
				updateCombobox();
				dlg.txtName.setText("");
				dlg.txtDescription.setText("");
				dlg.txtID.setText("");
				dlg.txtAddingNumber.setText("");
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

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void linkSORNumber(TCComponent vfdsItem) {
		try {
			if (!dlg.txtAddingNumber.getText().isEmpty()) {
				TCComponent sorNumber = specbook;
				if (sorNumber != null)
					vfdsItem.setReferenceProperty("vf3_SOR_doc", sorNumber);// vf3_DVPR_doc
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void linkDVPNumber(TCComponent vfteItem) {
		try {
			if (!dlg.txtAddingNumber.getText().isEmpty()) {
				TCComponent dvprItemRev = specbook;
				if (dvprItemRev != null)
					dvprItemRev.setRelated("VF4_TestReports", new TCComponent[] { vfteItem });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (!dlg.cbPrefixName.getText().isEmpty() && !dlg.cbModelCode.getText().isEmpty() && !dlg.cbModuleName.getText().isEmpty()) {

			String prefixName = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());// StringExtension.GetValueFromText(dlg.cbPrefixName.getText());
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());// StringExtension.GetValueFromText(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());// StringExtension.GetValueFromText(dlg.cbModuleName.getText());

			if (!prefixName.isEmpty() && !modelCode.isEmpty() && !moduleName.isEmpty()) {
				generateNextID(dlg.cbVehicleCategory.getText(), prefixName, modelCode, moduleName);
			}
		}
	}

	private void updateAddingNumberUI() {
		dlg.txtAddingNumber.setText("");
		dlg.cbModelCode.deselectAll();
		dlg.cbModuleName.deselectAll();
		specbook = null;
		String prefix = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());// StringExtension.GetValueFromText(dlg.cbPrefixName.getText());
		if (prefix == null)
			prefix = "";
		if (prefix.compareToIgnoreCase("VFDS") == 0) {
			dlg.lblAddingNumber.setText("SOR Number:");
			dlg.lblAddingNumber.setVisible(true);
			dlg.txtAddingNumber.setVisible(true);
			dlg.btnAddingSearch.setVisible(true);
			dlg.btnAddingRemove.setVisible(true);
			//
			dlg.cbModelCode.setEnabled(true);
			dlg.cbModuleName.setEnabled(true);
		} else if (prefix.compareToIgnoreCase("VFTE") == 0) {
			dlg.lblAddingNumber.setText("DVP Number: (*)");
			dlg.lblAddingNumber.setVisible(true);
			dlg.txtAddingNumber.setVisible(true);
			dlg.btnAddingSearch.setVisible(true);
			dlg.btnAddingRemove.setVisible(true);
			//
			dlg.cbModelCode.setEnabled(false);
			dlg.cbModuleName.setEnabled(false);
		} else if (prefix.compareToIgnoreCase("DTSS") == 0 || prefix.compareToIgnoreCase("VTSS") == 0) {
			dlg.lblAddingNumber.setVisible(false);
			dlg.txtAddingNumber.setVisible(false);
			dlg.btnAddingSearch.setVisible(false);
			dlg.btnAddingRemove.setVisible(false);
			//
			dlg.cbModelCode.setEnabled(true);
			dlg.cbModuleName.setText(moduleNameValue.get("VHC"));
			dlg.cbModuleName.setEnabled(false);
		} else {
			dlg.lblAddingNumber.setVisible(false);
			dlg.txtAddingNumber.setVisible(false);
			dlg.btnAddingSearch.setVisible(false);
			dlg.btnAddingRemove.setVisible(false);
			//
			dlg.cbModelCode.setEnabled(true);
			dlg.cbModuleName.setEnabled(true);
		}
	}

	private void generateNextID(String vehicleCategory, String prefixName, String modelCode, String moduleName) {
		try {
			String search_Items = "";
			search_Items = prefixName + modelCode + moduleName;
			String objectType = StringExtension.GetObjectTypeReal(prefixNameDataForm, prefixName);
			if (prefixName.compareTo("VFDS") == 0) {
				objectType += ";VF3_AT_DVPR_doc";
			}

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", objectType);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + "0001";
				} else {
					newIDValue = search_Items + "001";
				}
			} else {
				int id = 0;
				String split = "";
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					split = item_search[0].toString().substring(9, 13);
				} else {
					split = item_search[0].toString().substring(10, 13);
				}
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
				} else {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 3);
				}
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception ex) {
			System.out.println(" ::: UpdateItemIDTextBox - >  " + ex);
		}
	}

	private void updateCombobox() {
		resetDialog();
		String vehType = dlg.cbVehicleCategory.getText();
		prefixNameValue = StringExtension.GetComboboxValue(prefixNameDataForm, vehType);
		modelCodeValue = StringExtension.GetComboboxValue(modeCodeDataForm, vehType);
		moduleNameValue = StringExtension.GetComboboxValue(moduleNameDataForm, vehType);

		StringExtension.UpdateValueTextCombobox(dlg.cbPrefixName, prefixNameValue);
		StringExtension.UpdateValueTextCombobox(dlg.cbModelCode, modelCodeValue);
		StringExtension.UpdateValueTextCombobox(dlg.cbModuleName, moduleNameValue);
	}

	private void resetDialog() {
		dlg.cbPrefixName.removeAll();
		dlg.cbModelCode.removeAll();
		dlg.cbModuleName.removeAll();
	}

	private boolean validationCreate() {
		if (dlg.txtName.getText().isEmpty())
			return false;
		String prefixName = getPrefixName();
		if (prefixName.compareToIgnoreCase("VFTE") == 0) {
			if (dlg.txtAddingNumber.getText().isEmpty())
				return false;
		}

		return true;
	}

	private void SearchAdding() {
		String prefix = getPrefixName();
		String docTypeSearch = "";
		switch (prefix) {
		case "VFDS":
			docTypeSearch = "VFSR";
			break;
		case "VFTE":
			docTypeSearch = "DVPR";
			break;
		default:
			break;
		}

		SearchDocument_Dialog searchDlg = new SearchDocument_Dialog(dlg.getShell(), dlg.getShell().getStyle(), docTypeSearch);
		searchDlg.open();
		Button btnOK = searchDlg.getOKButton();

		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Table partTable = searchDlg.getSearchTable();
				TableItem[] items = partTable.getSelection();
				dlg.txtAddingNumber.setText(items[0].getText());
				if (searchDlg.getMapSORCompo().containsKey(items[0].getText())) {
					specbook = searchDlg.getMapSORCompo().get(items[0].getText());
					dlg.txtAddingNumber.setText(items[0].getText());
					if (prefix.compareToIgnoreCase("VFTE") == 0) {
						UpdateIDForVFTE();
					}
				}
				searchDlg.getShell().dispose();
			}
		});
	}

	private void UpdateIDForVFTE() {
		try {
			TCProperty modelCodeProperty = specbook.getTCProperty("vf3_model_code");
			String modelCode = modelCodeProperty.getStringValue();

			TCProperty moduleNameProperty = specbook.getTCProperty("vf3_module_name");
			String moduleName = moduleNameProperty.getStringValue();
			dlg.cbModelCode.setText(modelCodeValue.get(modelCode));
			dlg.cbModuleName.setText(moduleNameValue.get(moduleName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getPrefixName() {
		String prefix = (String) dlg.cbPrefixName.getData(dlg.cbPrefixName.getText());
		if (prefix == null)
			return "";
		return prefix;
	}
}