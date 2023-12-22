package com.teamcenter.vinfast.escooter.engineering.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
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

public class ScooterPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ScooterPartCreate_Dialog dlg;
	private TCComponent selectedObject;

	private Boolean isAddToBom = false;
	private String OBJECT_TYPE = "VF3_Scooter_part";

	public ScooterPartCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			isAddToBom = false;
			if (selectedObject instanceof TCComponentBOMLine) {
				isAddToBom = true;
			}

			// init data
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", OBJECT_TYPE, session);
			LinkedHashMap<String, String> materialGroupDataForm = TCExtension.GetLovValueAndDescription("vf3_mat_group", OBJECT_TYPE, session);
			String[] partCategoryDataForm = TCExtension.GetLovValues("vf4_part_category", OBJECT_TYPE, session);
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE, session);
			String[] categoryDataForm = TCExtension.GetLovValues("vf3_category", OBJECT_TYPE, session);
			String[] uomDataForm = TCExtension.GetUOMList(session);

			// init UI
			dlg = new ScooterPartCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleLine, vehicleLineDataForm);
			dlg.cbCategory.setItems(categoryDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbMaterialGroup, materialGroupDataForm);
			dlg.cbPartCategory.setItems(partCategoryDataForm);
			dlg.cbPartCategory.setText("NONE");
			dlg.cbMakeBuy.setItems(partMakeBuyDataForm);
			dlg.cbUoM.setItems(uomDataForm);
			dlg.cbUoM.setText("PC");

			dlg.cbMaterialGroup.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					checkComboboxToGenID();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			dlg.lblQuantity.setVisible(false);
			dlg.txtQuantity.setVisible(false);

			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void checkComboboxToGenID() {
		if (!dlg.cbMaterialGroup.getText().isEmpty()) {
			String materialGroup = (String) dlg.cbMaterialGroup.getData(dlg.cbMaterialGroup.getText());

			if (!materialGroup.isEmpty()) {
				generateNextID(materialGroup);
			}
		}
	}

	private void generateNextID(String materialGroup) {
		try {
			String search_Items = "";
			search_Items = materialGroup;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", "Scooter Part");
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "00000001";
			} else {
				int id = 0;
				String split = "";
				split = item_search[0].toString().substring(3, 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);
				newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 8);
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean checkRequired() {
		boolean check = true;

		if (dlg.txtID.getText().isEmpty()) {
			warningLabel(dlg.lblID, true);
			check = false;
		} else {
			warningLabel(dlg.lblID, false);
		}
		if (dlg.txtName.getText().isEmpty()) {
			warningLabel(dlg.lblName, true);
			check = false;
		} else {
			warningLabel(dlg.lblName, false);
		}
		if (dlg.cbMaterialGroup.getText().isEmpty()) {
			warningLabel(dlg.lblMaterialGroup, true);
			check = false;
		} else {
			warningLabel(dlg.lblMaterialGroup, false);
		}
		if (dlg.cbMakeBuy.getText().isEmpty()) {
			warningLabel(dlg.lblPartMakebuy, true);
			check = false;
		} else {
			warningLabel(dlg.lblPartMakebuy, false);
		}
		if (dlg.cbUoM.getText().isEmpty()) {
			warningLabel(dlg.lblUoM, true);
			check = false;
		} else {
			warningLabel(dlg.lblUoM, false);
		}
		if (dlg.cbVehicleLine.getText().isEmpty()) {
			warningLabel(dlg.lblEsModelvehicleLine, true);
			check = false;
		} else {
			warningLabel(dlg.lblEsModelvehicleLine, false);
		}
		if (dlg.cbPartCategory.getText().isEmpty()) {
			warningLabel(dlg.lblPartCategory, true);
			check = false;
		} else {
			warningLabel(dlg.lblPartCategory, false);
		}

		return check;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dmService = DataManagementService.getService(session);
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String materialGroup = (String) dlg.cbMaterialGroup.getData(dlg.cbMaterialGroup.getText());
			String description = dlg.txtDescription.getText();
			String partMakeBuy = dlg.cbMakeBuy.getText();
			String uom = dlg.cbUoM.getText();
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			String vehicleLine = (String) dlg.cbVehicleLine.getData(dlg.cbVehicleLine.getText());
			String category = dlg.cbCategory.getText();

			String partReference = dlg.txtPartReference.getText();
			String partCategory = dlg.cbPartCategory.getText();
			String partNameVietname = dlg.txtVietnamName.getText();
			boolean isAfterSale = dlg.rbtYes.getSelection();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("vf3_mat_group", materialGroup);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_es_model_veh_line", vehicleLine);
			itemDef.data.boolProps.put("vf4_itm_after_sale_relevant", isAfterSale);
			itemDef.data.stringProps.put("vf4_part_reference", partReference);
			itemDef.data.stringProps.put("vf4_part_category", partCategory);
			itemDef.data.stringProps.put("vf3_category", category);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("item_revision_id", "01");
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf3_viet_desciption", partNameVietname);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage("Create unsuccessfully. Exception: " + TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			TCComponentItemRevision cfgContext = null;
			for (TCComponent rev : response.output[0].objects) {
				if (rev.getType().equals(OBJECT_TYPE + "Revision")) {
					cfgContext = (TCComponentItemRevision) rev;
				}
			}
			if (cfgContext != null) {
				Boolean addToNewstuff = true;
				if (selectedObject != null) {
					if (isAddToBom) {
						addToBOMLine(cfgContext);
						addToNewstuff = false;
						dlg.setMessage("Created successfully, new item has been add to BOM struct.", IMessageProvider.INFORMATION);
					} else {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext.getItem());
								addToNewstuff = false;
								dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				if (addToNewstuff) {
					try {
						session.getUser().getNewStuffFolder().add("contents", cfgContext.getItem());
						dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
						openOnCreate(cfgContext);
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
				resetDialog();
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void addToBOMLine(TCComponentItemRevision newRev) {
		TCComponentBOMLine bomline = (TCComponentBOMLine) selectedObject;
		try {
			TCComponentBOMLine child = bomline.add(newRev.getItem(), newRev, null, false);
			int quantity = 0;
			if (StringExtension.isInteger(dlg.txtQuantity.getText(), 10)) {
				quantity = (int) Integer.parseInt(dlg.txtQuantity.getText());
			}
			if (quantity > 0) {
				child.setProperty("bl_quantity", Integer.toString(quantity));
			} else {
				child.setProperty("bl_quantity", "1");
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.cbMaterialGroup.deselectAll();
		dlg.txtDescription.setText("");
		dlg.cbMakeBuy.deselectAll();
		dlg.cbVehicleLine.deselectAll();
		dlg.cbCategory.deselectAll();
		dlg.txtPartReference.setText("");
		dlg.cbPartCategory.setText("NONE");
		dlg.txtVietnamName.setText("");
		dlg.txtQuantity.setText("1");
	}

	private void warningLabel(Label target, boolean warning) {
		if (warning)
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
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
