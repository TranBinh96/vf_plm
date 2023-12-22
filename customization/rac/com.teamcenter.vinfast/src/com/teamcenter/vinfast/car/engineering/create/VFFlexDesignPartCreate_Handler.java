package com.teamcenter.vinfast.car.engineering.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VFFlexDesignPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private VFFlexDesignPartCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private Boolean IsAddToBom = false;
	private String[] uomDataForm;

	public VFFlexDesignPartCreate_Handler() {
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
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", "VF4_Design", session);
			String[] partCategoryDataForm = TCExtension.GetLovValues("vf4_part_category", "VF4_Design", session);
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle",
					"VF4_Design", session);
			LinkedHashMap<String, String> partTraceabilityDataForm = TCExtension
					.GetLovValueAndDisplay("vf4_item_is_traceable", "VF4_Design", session);
			uomDataForm = TCExtension.GetUOMList(session);
			String[] longShortDataForm = TCExtension.GetLovValues("vf4_long_short_lead", "VF4_Design", session);
			String[] supplierTypeDataForm = TCExtension.GetLovValues("vf4_supplier_type", "VF4_Design", session);
			// Init UI
			dlg = new VFFlexDesignPartCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create VF Flex Part");
			dlg.setMessage("Define business object create information");

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);
			dlg.cbPartCategory.setItems(partCategoryDataForm);
			dlg.cbPartCategory.setText("NONE");
			StringExtension.UpdateValueTextCombobox(dlg.cbDonorVehicle, donorVehicleDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceabilityDataForm);
			dlg.cbUoM.setItems(uomDataForm);
			dlg.cbLongShortLead.setItems(longShortDataForm);
			dlg.cbSupplierType.setItems(supplierTypeDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			fillData();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void fillData() throws Exception {
		TCComponentItem item = null;
		TCComponentItemRevision itemRev = null;
		if (IsAddToBom) {
			itemRev = ((TCComponentBOMLine) selectedObject).getItemRevision();
		} else {
			itemRev = (TCComponentItemRevision) selectedObject;
		}
		item = itemRev.getItem();

		String name = itemRev.getPropertyDisplayableValue("object_name");
		String description = itemRev.getPropertyDisplayableValue("object_desc");
		String partMakeBuy = item.getPropertyDisplayableValue("vf4_item_make_buy");
		String partCategory = item.getPropertyDisplayableValue("vf4_part_category");
		String donorVehicle = item.getPropertyDisplayableValue("vf4_donor_vehicle");
		String partTraceability = item.getPropertyDisplayableValue("vf4_item_is_traceable");
		String uom = item.getPropertyDisplayableValue("uom_tag");
		String longShortLead = item.getPropertyDisplayableValue("vf4_long_short_lead");
		String supplierType = item.getPropertyDisplayableValue("vf4_supplier_type");
		String cuvType = itemRev.getProperty("vf4_cuv_veh_type");
		String manufComponent = item.getProperty("vf4_manu_component");

		dlg.txtName.setText(name);
		dlg.txtDescription.setText(description);
		dlg.cbPartMakeBuy.setText(partMakeBuy);
		dlg.cbPartCategory.setText(partCategory);
		dlg.cbDonorVehicle.setText(donorVehicle);
		dlg.cbPartTraceability.setText(partTraceability);
		dlg.cbUoM.setText(TCExtension.GetUOMValue(uom, uomDataForm));
		dlg.cbLongShortLead.setText(longShortLead);
		dlg.cbSupplierType.setText(supplierType);
		if (!cuvType.isEmpty()) {
			if (cuvType.contains(",")) {
				String[] strs = cuvType.split(",");
				for (String str : strs) {
					dlg.lstCUVType.add(str);
				}
			} else {
				dlg.lstCUVType.add(cuvType);
			}
		}
		dlg.rbtManufTrue.setSelection(manufComponent.compareToIgnoreCase("True") == 0);
		dlg.rbtManufFalse.setSelection(manufComponent.compareToIgnoreCase("True") != 0);

		dlg.cbPartMakeBuy.setEnabled(false);
		dlg.cbPartCategory.setEnabled(false);
		dlg.cbDonorVehicle.setEnabled(false);
		dlg.cbPartTraceability.setEnabled(false);
		dlg.cbUoM.setEnabled(false);
		dlg.cbLongShortLead.setEnabled(false);
		dlg.cbSupplierType.setEnabled(false);
		dlg.rbtManufTrue.setEnabled(false);
		dlg.rbtManufFalse.setEnabled(false);
		dlg.lstCUVType.setEnabled(false);
	}

	private Boolean checkRequired() {
		if (dlg.txtID.getText().isEmpty() || dlg.txtName.getText().isEmpty() || dlg.cbPartMakeBuy.getText().isEmpty()
				|| dlg.cbPartCategory.getText().isEmpty() || dlg.cbDonorVehicle.getText().isEmpty())
			return false;
		return true;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			} else {
				String objectType = "VF4_FlexDesign";
				DataManagementService dms = DataManagementService.getService(session);
				String id = dlg.txtID.getText();
				String name = dlg.txtName.getText();
				String description = dlg.txtDescription.getText();
				String partMakeBuy = dlg.cbPartMakeBuy.getText();
				String partCategory = dlg.cbPartCategory.getText();
				String donorVehicle = (String) dlg.cbDonorVehicle.getData(dlg.cbDonorVehicle.getText());
				String partTraceability = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
				String uom = dlg.cbUoM.getText();
				TCComponent UOMTag = TCExtension.GetUOMItem(uom);
				String longShortLead = dlg.cbLongShortLead.getText();
				String supplierType = dlg.cbSupplierType.getText();
				String[] cuvType = dlg.lstCUVType.getItems();

				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = objectType;
				itemDef.data.stringProps.put("item_id", id);
				itemDef.data.stringProps.put("object_name", name);
				itemDef.data.stringProps.put("object_desc", description);
				itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
				itemDef.data.stringProps.put("vf4_part_category", partCategory);
				itemDef.data.stringProps.put("vf4_donor_vehicle", donorVehicle);
				itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);
				itemDef.data.tagProps.put("uom_tag", UOMTag);
				itemDef.data.stringProps.put("vf4_long_short_lead", longShortLead);
				itemDef.data.stringProps.put("vf4_supplier_type", supplierType);
				itemDef.data.boolProps.put("vf4_manu_component", dlg.rbtManufTrue.getSelection());

				CreateInput revDef = new CreateInput();
				revDef.boName = objectType + "Revision";
				revDef.stringProps.put("object_desc", description);
				revDef.stringArrayProps.put("vf4_cuv_veh_type", cuvType);

				itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

				CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

				if (response.serviceData.sizeOfPartialErrors() == 0) {
					TCComponentItemRevision cfgContext = null;
					for (TCComponent rev : response.output[0].objects) {
						if (rev.getType().equals(objectType + "Revision")) {
							cfgContext = (TCComponentItemRevision) rev;
						}
					}
					if (cfgContext != null) {
						relationToOriginPart(cfgContext.getItem());
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
						dlg.setMessage("Create unsuccessfully, please contact with administrator.",
								IMessageProvider.ERROR);
					}
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

	// VF4_original_purchased_part
	private void relationToOriginPart(TCComponentItem item) throws Exception {
		TCComponentItem originItem = null;
		if (selectedObject instanceof TCComponentItemRevision) {
			TCComponentItemRevision originItemRevision = (TCComponentItemRevision) selectedObject;
			originItem = originItemRevision.getItem();
		} else if (selectedObject instanceof TCComponentBOMLine) {
			originItem = ((TCComponentBOMLine) selectedObject).getItem();
		}
		if (originItem != null) {
			item.setRelated("VF4_original_purchased_part", new TCComponent[] {originItem});
		}
	}

	private void resetDialog() {
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.cbPartCategory.deselectAll();
		dlg.cbDonorVehicle.deselectAll();
		dlg.cbPartTraceability.deselectAll();
		dlg.cbUoM.deselectAll();
		dlg.cbLongShortLead.deselectAll();
		dlg.cbSupplierType.deselectAll();
		dlg.lstCUVType.removeAll();
	}
}
