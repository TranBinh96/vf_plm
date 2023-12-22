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

public class VSMSemiFinishedGoods_Handler extends AbstractHandler {
	private TCSession session;
	private VSMSemiFinishedGoods_Dialog dlg;
	private TCComponent selectedObject = null;
	private Boolean IsAddToBom = false;

	private static String OBJECT_TYPE_REAL = "VF4_Compo_Design";
	private static String PRODUCT_CATEGORY = "Semi-Finished Goods";

	public VSMSemiFinishedGoods_Handler() {
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
			LinkedHashMap<String, String> productDataForm = TCExtension.GetLovValueAndDescription("vf4_semi_product",
					OBJECT_TYPE_REAL, session);
			LinkedHashMap<String, String> productCategoryDataForm = TCExtension
					.GetLovValueAndDescription("vf4_semi_product_category", OBJECT_TYPE_REAL, session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] makeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE_REAL, session);
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle",
					OBJECT_TYPE_REAL, session);
			// Init UI
			dlg = new VSMSemiFinishedGoods_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbProduct, productDataForm);
			dlg.cbProduct.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					generateNextID();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbProductCategory, productCategoryDataForm);
			dlg.cbProductCategory.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					generateNextID();
				}
			});

			dlg.cbUoM.setItems(uomDataForm);

			dlg.cbPartMakeBuy.setItems(makeBuyDataForm);
			dlg.cbPartMakeBuy.setEnabled(false);
			dlg.cbPartMakeBuy.setText("Make");
			
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
			
			DataManagementService dms = DataManagementService.getService(session);
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			String product = (String) dlg.cbProduct.getData(dlg.cbProduct.getText());
			String productCategory = (String) dlg.cbProductCategory.getData(dlg.cbProductCategory.getText());
			String partMakeBuy = dlg.cbPartMakeBuy.getText();
			String uom = dlg.cbUoM.getText();
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			String donorVehicle = (String)dlg.cbDonorVehicle.getData(dlg.cbDonorVehicle.getText());

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE_REAL;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_semi_product", product);
			itemDef.data.stringProps.put("vf4_semi_product_category", productCategory);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_donor_vehicle", donorVehicle);
			itemDef.data.stringProps.put("vf4_component_category", PRODUCT_CATEGORY);

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
							dlg.setMessage("Created successfully, new item has been add to BOM struct.",
									IMessageProvider.INFORMATION);
						} else {
							String type = selectedObject.getProperty("object_type");
							if (type.compareToIgnoreCase("Folder") == 0) {
								try {
									selectedObject.add("contents", cfgContext.getItem());
									addToNewstuff = false;
									dlg.setMessage(
											"Created successfully, new item has been copied to "
													+ selectedObject.getProperty("object_name") + " folder.",
											IMessageProvider.INFORMATION);
								} catch (TCException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
					if (addToNewstuff) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext.getItem());
							dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder",
									IMessageProvider.INFORMATION);
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
		if (dlg.cbProduct.getText().isEmpty())
			return;
		if (dlg.cbProductCategory.getText().isEmpty())
			return;
		try {
			String search_Items = (String) dlg.cbProduct.getData(dlg.cbProduct.getText())
					+ (String) dlg.cbProductCategory.getData(dlg.cbProductCategory.getText());
			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Component Category", PRODUCT_CATEGORY);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "__Latest Component ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "00001";
			} else {
				int id = 0;
				String split = item_search[0].toString().substring(6, 11);
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
		dlg.cbProduct.deselectAll();
		dlg.cbProductCategory.deselectAll();
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbUoM.deselectAll();
	}
}
