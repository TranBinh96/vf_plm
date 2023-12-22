package com.teamcenter.vines.create;

import java.util.LinkedHashMap;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VESMEPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private VESMEPartCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	private static String OBJECT_TYPE = "VF4_VES_ME_Part";
	private LinkedHashMap<String, String> modelAndPrefixNumber;

	public VESMEPartCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			// Load data form
			modelAndPrefixNumber = new LinkedHashMap<String, String>();
			String[] modelDataForm = TCExtension.GetLovValues("vf4_model", OBJECT_TYPE, session);
			String[] preference = TCExtension.GetPreferenceValues("VES_MEPART_MODEL_PREFIX_NUMBER", session);
			for (String row : preference) {
				String[] str = row.split("=");
				if (str.length > 1) {
					String value = str[0];
					String prefixNumber = str[1];
					modelAndPrefixNumber.put(value, prefixNumber);
				}
			}

			LinkedHashMap<String, String> subCategoryDataForm = new LinkedHashMap<String, String>();
			String[] preference1 = TCExtension.GetPreferenceValues("VES_MEPART_SUB_CATEGORY", session);
			for (String row : preference1) {
				String[] str = row.split("=");
				if (str.length > 1) {
					String value = str[0];
					String display = str[0] + " - " + str[1];
					subCategoryDataForm.put(value, display);
				}
			}

			String[] makeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", "Item", session);
			String[] uomDataForm = TCExtension.GetUOMList(session);

			// Init UI
			dlg = new VESMEPartCreate_Dialog(new Shell());
			dlg.create();

			dlg.cbModel.setItems(modelDataForm);
			dlg.cbModel.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					onEventChange_Combobox();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbCategory, subCategoryDataForm);
			dlg.cbCategory.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					onEventChange_Combobox();
				}
			});

			dlg.cbPartMakeBuy.setItems(makeBuyDataForm);

			dlg.cbUOM.setItems(uomDataForm);

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					createNewItem();
				}
			});

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void onEventChange_Combobox() {
		if (!dlg.cbModel.getText().isEmpty() && !dlg.cbCategory.getText().isEmpty()) {
			String category = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
			if (category.compareTo("SEM") == 0) {
				dlg.txtID.setText("");
				dlg.txtID.setEnabled(true);
			} else {
				String inputString = "";
				String type = "VinES ME Part";
				inputString += category + modelAndPrefixNumber.get(dlg.cbModel.getText());
				dlg.txtID.setText(generateNextID(inputString, type));
				dlg.txtID.setEnabled(false);
			}
		} else {
			dlg.txtID.setText("");
		}
	}

	private void resetDialog() {
		dlg.cbModel.deselectAll();
		dlg.cbCategory.deselectAll();
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDesc.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.cbUOM.deselectAll();
	}

	private Boolean checkRequired() {
		if (dlg.cbModel.getText().isEmpty())
			return false;
		if (dlg.cbCategory.getText().isEmpty())
			return false;
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDesc.getText().isEmpty())
			return false;
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		if (dlg.cbUOM.getText().isEmpty())
			return false;

		return true;
	}

	private String generateNextID(String inputString, String type) {
		try {
			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", inputString + "*");
			inputQuery.put("Type", type);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");
			int remainNo = 11 - inputString.length();
			if (item_search == null || item_search.length == 0) {
				newIDValue = inputString + StringExtension.ConvertNumberToString(001, remainNo);
			} else {
				int id = 0;

				String split = item_search[0].toString().substring(inputString.length(), 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = inputString + StringExtension.ConvertNumberToString(id + 1, remainNo);
			}
			return newIDValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		DataManagementService dms = DataManagementService.getService(session);
		String id = dlg.txtID.getText();

		if (id.length() != 11) {
			dlg.setMessage("Part Number not valid.", IMessageProvider.WARNING);
			return;
		}

		String name = dlg.txtName.getText();
		String model = dlg.cbModel.getText();
		String subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
		String partMakeBuy = dlg.cbPartMakeBuy.getText();
		String description = dlg.txtDesc.getText();
		String uom = dlg.cbUOM.getText();

		TCComponent UOMTag = null;
		try {
			UOMTag = TCExtension.GetUOMItem(uom);
			// Item
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
//			itemDef.data.stringProps.put("vf4_donor_veh", model);
			itemDef.data.stringProps.put("vf4_model", model);
			itemDef.data.stringProps.put("vf4_category", subCategory);
			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				if (cfgContext != null) {
					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				dlg.setMessage(TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
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
}
