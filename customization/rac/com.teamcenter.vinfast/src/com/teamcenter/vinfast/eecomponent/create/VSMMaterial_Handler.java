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
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
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

public class VSMMaterial_Handler extends AbstractHandler {
	private TCSession session;
	private VSMMaterial_Dialog dlg;
	private TCComponent selectedObject = null;
	private Boolean IsAddToBom = false;

	private static String OBJECT_TYPE_REAL = "VF4_Compo_Design";

	private LinkedHashMap<String, String> materialStdDataForm = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> materialNonStdDataForm = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> materialNonStdCategoryDataForm = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> packagingDataForm = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> packagingCategoryDataForm = new LinkedHashMap<String, String>();

	public VSMMaterial_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			IsAddToBom = false;
			if (selectedObject instanceof TCComponentBOMLine) {
				IsAddToBom = true;
			}
			// Init data
			materialStdDataForm = TCExtension.GetLovValueAndDescription("vf4_material_std", OBJECT_TYPE_REAL, session);
			materialNonStdDataForm = TCExtension.GetLovValueAndDescription("vf4_material_non_std", OBJECT_TYPE_REAL, session);
			materialNonStdCategoryDataForm = TCExtension.GetLovValueAndDescription("vf4_material_nonStdCategory", OBJECT_TYPE_REAL, session);
			packagingDataForm = TCExtension.GetLovValueAndDescription("vf4_packaging_material", OBJECT_TYPE_REAL, session);
			packagingCategoryDataForm = TCExtension.GetLovValueAndDescription("vf4_packaging_mat_category", OBJECT_TYPE_REAL, session);
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", OBJECT_TYPE_REAL, session);
//			String[] componentCategoryDataFom = TCExtension.GetLovValues("vf4_component_category", OBJECT_TYPE_REAL, session);
			String[] componentCategoryDataFom = { "Material Standard", "Material Non-Standard", "Packaging Material" };
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] makeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE_REAL, session);
			// Init UI
			dlg = new VSMMaterial_Dialog(new Shell());
			dlg.create();

			dlg.cbComponentCategory.setItems(componentCategoryDataFom);
			dlg.cbComponentCategory.select(0);
			dlg.cbComponentCategory.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateUI();
				}
			});
			updateUI();

			dlg.cbMaterial.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					generateNextID();
				}
			});

			dlg.cbMaterialCategory.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					generateNextID();
				}
			});

			dlg.cbUoM.setItems(uomDataForm);

			dlg.cbPartMakeBuy.setItems(makeBuyDataForm);
			dlg.cbPartMakeBuy.setEnabled(false);
			dlg.cbPartMakeBuy.setText("Buy");

			StringExtension.UpdateValueTextCombobox(dlg.cbDonorVehicle, donorVehicleDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createNewItem() {
		try {
			if (!validationSubmit()) {
				dlg.setMessage("Please input all required information.", IMessageProvider.WARNING);
				return;
			}

			String componentCategory = dlg.cbComponentCategory.getText();

			DataManagementService dms = DataManagementService.getService(session);
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();

			String materialStd = "", materialNonStd = "", packaing = "";
			materialStd = materialNonStd = packaing = (String) dlg.cbMaterial.getData(dlg.cbMaterial.getText());

			String materialNonStdCategory = "", packaingCategory = "";
			materialNonStdCategory = packaingCategory = (String) dlg.cbMaterialCategory.getData(dlg.cbMaterialCategory.getText());

			String partMakeBuy = dlg.cbPartMakeBuy.getText();
			String uom = dlg.cbUoM.getText();
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			String donorVehicle = (String) dlg.cbDonorVehicle.getData(dlg.cbDonorVehicle.getText());

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE_REAL;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_component_category", componentCategory);

			if (dlg.cbComponentCategory.getText().compareToIgnoreCase("Material Standard") == 0) {
				itemDef.data.stringProps.put("vf4_material_std", materialStd);
			} else if (dlg.cbComponentCategory.getText().compareToIgnoreCase("Material Non-Standard") == 0) {
				itemDef.data.stringProps.put("vf4_material_non_std", materialNonStd);
				itemDef.data.stringProps.put("vf4_material_nonStdCategory", materialNonStdCategory);
				StringExtension.UpdateValueTextCombobox(dlg.cbMaterialCategory, materialNonStdCategoryDataForm);
			} else {
				itemDef.data.stringProps.put("vf4_packaging_material", packaing);
				itemDef.data.stringProps.put("vf4_packaging_mat_category", packaingCategory);
			}

			itemDef.data.stringProps.put("vf4_donor_vehicle", donorVehicle);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE_REAL + "Revision";
			revDef.stringProps.put("object_desc", description);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItemRevision cfgContext = null;
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals(OBJECT_TYPE_REAL + "Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
				if (cfgContext != null) {
					Boolean addToNewstuff = true;
					if (selectedObject != null) {
						if (IsAddToBom) {
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
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
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

	private void addToBOMLine(TCComponentItemRevision newRev) {
		TCComponentBOMLine bomline = (TCComponentBOMLine) selectedObject;
		try {
			TCComponentBOMLine child = bomline.add(newRev.getItem(), newRev, null, false);
			child.setProperty("bl_quantity", "1");

			TCComponentBOMWindow window = bomline.window();
			window.save();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private boolean validationSubmit() {
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		if (dlg.cbDonorVehicle.getText().isEmpty())
			return false;

		return true;
	}

	private void generateNextID() {
		String componentCategory = dlg.cbComponentCategory.getText();
		if (dlg.cbMaterial.getText().isEmpty())
			return;
		if (componentCategory.compareToIgnoreCase("Material Standard") != 0 && dlg.cbMaterialCategory.getText().isEmpty())
			return;
		try {
			String search_Items = (String) dlg.cbMaterial.getData(dlg.cbMaterial.getText());
			if (componentCategory.compareToIgnoreCase("Material Standard") != 0)
				search_Items += (String) dlg.cbMaterialCategory.getData(dlg.cbMaterialCategory.getText());

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Component Category", componentCategory);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "__Latest Component ID");

			if (item_search == null || item_search.length == 0) {
				if (componentCategory.compareToIgnoreCase("Material Standard") == 0) {
					newIDValue = search_Items + "200001";
				} else if (componentCategory.compareToIgnoreCase("Material Non-Standard") == 0) {
					newIDValue = search_Items + "2001";
				} else {
					newIDValue = search_Items + "1001";
				}
			} else {
				int id = 0;
				String split = "";

				if (componentCategory.compareToIgnoreCase("Material Standard") == 0) {
					split = item_search[0].toString().substring(3, 9);
					if (StringExtension.isInteger(split, 10)) {
						if (id < Integer.parseInt(split))
							id = Integer.parseInt(split);

						if (Integer.parseInt(StringExtension.ConvertNumberToString(id + 1, 6)) <= 200001) {
							newIDValue = search_Items + "200001";
						} else {
							newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 6);
						}
					} else {
						newIDValue = search_Items + "200001";
					}
				} else if (componentCategory.compareToIgnoreCase("Material Non-Standard") == 0) {
					split = item_search[0].toString().substring(5, 9);
					if (StringExtension.isInteger(split, 10)) {
						if (id < Integer.parseInt(split))
							id = Integer.parseInt(split);

						if (Integer.parseInt(StringExtension.ConvertNumberToString(id + 1, 4)) <= 2001) {
							newIDValue = search_Items + "2001";
						} else {
							newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
						}
					} else {
						newIDValue = search_Items + "2001";
					}
				} else {
					split = item_search[0].toString().substring(5, 9);
					if (StringExtension.isInteger(split, 10)) {
						if (id < Integer.parseInt(split))
							id = Integer.parseInt(split);
						if (Integer.parseInt(StringExtension.ConvertNumberToString(id + 1, 4)) <= 1001) {
							newIDValue = search_Items + "1001";
						} else {
							newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
						}
					} else {
						newIDValue = search_Items + "1001";
					}
				}

//				if (componentCategory.compareToIgnoreCase("Material Standard") != 0) {
//					split = item_search[0].toString().substring(5, 9);
//					if (StringExtension.isInteger(split, 10)) {
//						if (id < Integer.parseInt(split))
//							id = Integer.parseInt(split);
//						newIDValue = StringExtension.ConvertNumberToString(id + 1, 4);
//					} else {
//						newIDValue = search_Items + "0001";
//					}
//				} else {
//					split = item_search[0].toString().substring(3, 9);
//					if (StringExtension.isInteger(split, 10)) {
//						if (id < Integer.parseInt(split))
//							id = Integer.parseInt(split);
//						newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 6);
//					} else {
//						newIDValue = search_Items + "100001";
//					}
//				}
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception ex) {
			System.out.println(" ::: UpdateItemIDTextBox - >  " + ex);
		}
	}

	private void updateUI() {
		dlg.txtID.setText("");
		if (dlg.cbComponentCategory.getText().compareToIgnoreCase("Material Standard") == 0) {
			dlg.lblMaterialCategory.setVisible(false);
			dlg.cbMaterialCategory.setVisible(false);

			StringExtension.UpdateValueTextCombobox(dlg.cbMaterial, materialStdDataForm);
		} else if (dlg.cbComponentCategory.getText().compareToIgnoreCase("Material Non-Standard") == 0) {
			dlg.lblMaterialCategory.setVisible(true);
			dlg.cbMaterialCategory.setVisible(true);

			StringExtension.UpdateValueTextCombobox(dlg.cbMaterial, materialNonStdDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbMaterialCategory, materialNonStdCategoryDataForm);
		} else {
			dlg.lblMaterialCategory.setVisible(true);
			dlg.cbMaterialCategory.setVisible(true);

			StringExtension.UpdateValueTextCombobox(dlg.cbMaterial, packagingDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbMaterialCategory, packagingCategoryDataForm);
		}
	}

	private void resetDialog() {
		dlg.cbMaterial.deselectAll();
		dlg.cbMaterialCategory.deselectAll();
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbUoM.deselectAll();
	}
}
