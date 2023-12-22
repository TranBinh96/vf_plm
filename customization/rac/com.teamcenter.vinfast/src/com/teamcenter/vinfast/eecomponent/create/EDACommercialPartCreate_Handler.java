package com.teamcenter.vinfast.eecomponent.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.teamcenter.vinfast.subdialog.SearchVendor_Dialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class EDACommercialPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private EDACommercialPartCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private Boolean IsAddToBom = false;

	private static String OBJECT_TYPE = "VF4_Eda_Comm_Prt";

	private TCComponent vendor;
	private DataManagementService dmService;

	public EDACommercialPartCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			IsAddToBom = false;
			if (selectedObject instanceof TCComponentBOMLine) {
				IsAddToBom = true;
			}
			// Init data
			LinkedHashMap<String, String> materialStdDataForm = TCExtension.GetLovValueAndDescription("vf4_material_std", OBJECT_TYPE, session);
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_sourcing_program", OBJECT_TYPE + "Revision", session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] makeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE, session);
			String[] mainCategoryComp = TCExtension.GetLovValues("vf4_Main_Category_Compound", OBJECT_TYPE + "Revision", session);
			// Init UI
			dlg = new EDACommercialPartCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbMaterial, materialStdDataForm);

			dlg.cbMaterial.addModifyListener(new ModifyListener() {
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

			dlg.cbMainCategory.setItems(mainCategoryComp);

			dlg.btnVendorSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchVendor_Dialog searchDlg = new SearchVendor_Dialog(dlg.getShell());
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int[] items = searchDlg.tblSearch.getSelectionIndices();
							for (int index = 0; index < items.length; index++) {
								vendor = searchDlg.itemSearch.get(items[index]);
							}

							dlg.txtVendor.setText(vendor.toString());

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
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

	private void createVendorPart() {
		try {
			String vendorPartNumber = dlg.txtVendorPartNumber.getText();

			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_Vendor_Part";

			GetNextIdsResponse IDReponse = dmService.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_Vendor_Part";
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("vf4_Vendor_Part_Number", vendorPartNumber);
			itemDef.data.stringProps.put("object_name", "Vendor Name");
			if (vendor != null) {
//				itemDef.data.tagProps.put("vendors", vendor);
				itemDef.data.tagProps.put("vm0vendor_ref", vendor);
			}

			CreateInput revDef = new CreateInput();
			revDef.boName = "VF4_Vendor_PartRevision";

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage(TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNewItem() {
		try {
			if (!validationSubmit()) {
				dlg.setMessage("Please input all required information.", IMessageProvider.WARNING);
				return;
			}

			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			String materialStd = (String) dlg.cbMaterial.getData(dlg.cbMaterial.getText());
			String partMakeBuy = dlg.cbPartMakeBuy.getText();
			String uom = dlg.cbUoM.getText();
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			String donorVehicle = (String) dlg.cbDonorVehicle.getData(dlg.cbDonorVehicle.getText());
			String mainCategoryCompound = dlg.cbMainCategory.getText();
			String footPrint = dlg.txtFootPrint.getText();
			String symbolName = dlg.txtSymbolName.getText();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_Main_Category", mainCategoryCompound);

			itemDef.data.stringProps.put("vf4_material_std", materialStd);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf4_Foot_Print", footPrint);
			revDef.stringProps.put("vf4_Symbol_Name", symbolName);
			revDef.stringProps.put("vf4_donor_sourcing_program", donorVehicle);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage("Create new item unsuccessfully. Exception: " + TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			TCComponentItemRevision itemRevision = null;
			for (TCComponent rev : response.output[0].objects) {
				if (rev.getType().equals(OBJECT_TYPE + "Revision")) {
					itemRevision = (TCComponentItemRevision) rev;
				}
			}

			if (itemRevision == null) {
				dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				return;
			}

			createVFVendorPart(itemRevision);
			Boolean addToNewstuff = true;
			if (selectedObject != null) {
				if (IsAddToBom) {
					addToBOMLine(itemRevision);
					addToNewstuff = false;
					dlg.setMessage("Created successfully, new item has been add to BOM struct.", IMessageProvider.INFORMATION);
				} else {
					String type = selectedObject.getProperty("object_type");
					if (type.compareToIgnoreCase("Folder") == 0) {
						try {
							selectedObject.add("contents", itemRevision.getItem());
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
					session.getUser().getNewStuffFolder().add("contents", itemRevision.getItem());
					dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
			resetDialog();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void createVFVendorPart(TCComponentItemRevision itemRevision) {
		try {
			String vendorPartNumber = dlg.txtVendorPartNumber.getText();

			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_Vendor_Part";

			GetNextIdsResponse IDReponse = dmService.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_Vendor_Part";
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("vf4_Vendor_Part_Number", vendorPartNumber);
			itemDef.data.stringProps.put("object_name", itemRevision.getPropertyDisplayableValue("item_id"));
			if (vendor != null) {
//				itemDef.data.tagProps.put("vendors", vendor);
				itemDef.data.tagProps.put("vm0vendor_ref", vendor);
			}

			CreateInput revDef = new CreateInput();
			revDef.boName = "VF4_Vendor_PartRevision";

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItem vendorPart = null;
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals("VF4_Vendor_Part")) {
						vendorPart = (TCComponentItem) rev;
					}
				}
				itemRevision.setRelated("VF4_Supplier_Main_Item", new TCComponentItem[] { vendorPart });
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		if (vendor == null)
			return false;
		if (dlg.txtVendorPartNumber.getText().isEmpty())
			return false;
		if (dlg.cbMainCategory.getText().isEmpty())
			return false;
		if (dlg.txtFootPrint.getText().isEmpty())
			return false;
		if (dlg.txtSymbolName.getText().isEmpty())
			return false;
		if (dlg.cbUoM.getText().isEmpty())
			return false;

		return true;
	}

	private void generateNextID() {
		if (dlg.cbMaterial.getText().isEmpty())
			return;
		try {
			String search_Items = (String) dlg.cbMaterial.getData(dlg.cbMaterial.getText());

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", OBJECT_TYPE);

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + "200001";
			} else {
				int id = 0;
				String split = "";

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
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.cbMaterial.deselectAll();
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbUoM.deselectAll();
		dlg.cbDonorVehicle.deselectAll();
		dlg.cbMainCategory.deselectAll();
		dlg.txtFootPrint.setText("");
		dlg.txtSymbolName.setText("");
		dlg.txtVendorPartNumber.setText("");
		dlg.txtVendor.setText("");
		vendor = null;
	}
}
