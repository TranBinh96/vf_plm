package com.teamcenter.vinfast.aftersale.create;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
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
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

public class ServicePartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ServicePartCreate_Dialog dlg;
	private TCComponentBOMLine selectedObject;
	private LinkedHashMap<Object, String> modelGroupDataForm;
	private LinkedHashMap<String, LinkedList<String>> purchaseLevelValidate;

	public ServicePartCreate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please select \"Service Chapter\" to create \"Service Part\"", "ERROR", MessageBox.ERROR);
				return null;
			}

			// init data
			String[] materialGroupDataForm = TCExtension.GetPreferenceValues("VF_AfterSalePartCreation_MATERIAL_GROUP", session);
			String[] prefeValue = TCExtension.GetPreferenceValues("VF_AfterSalePartCreation_MODEL_GROUP_and_PREFIX_ID", session);
			modelGroupDataForm = new LinkedHashMap<Object, String>();
			for (String value : prefeValue) {
				String[] str = value.split("=");
				if (str.length > 1) {
					modelGroupDataForm.put(str[0], str[1]);
				}
			}
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", "VF4_Design", session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			LinkedHashMap<String, String> donorVehicleDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", "VF4_Design", session);
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", "VF3_me_scooter", session);

			prefeValue = TCExtension.GetPreferenceValues("VF_PART_CREATE_PURCHASELEVEL_VALIDATE", session);
			if (prefeValue != null) {
				purchaseLevelValidate = new LinkedHashMap<String, LinkedList<String>>();
				for (String purchase : prefeValue) {
					if (purchase.contains(";")) {
						String[] strArray = purchase.split(";");
						String partMakeBuy = strArray[0];
						LinkedList<String> purchaseList = new LinkedList<String>();
						if (strArray[1].contains(",")) {
							String[] strArray2 = strArray[1].split(",");
							for (String value : strArray2) {
								purchaseList.add(value);
							}
						} else {
							purchaseList.add(strArray[1]);
						}
						purchaseLevelValidate.put(partMakeBuy, purchaseList);
					}
				}
			}

			String[] supplierTypeDataForm = TCExtension.GetLovValues("vf4_supplier_type", "VF3_manuf_part", session);
			LinkedHashMap<String, String> partTraceabilityDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", "VF4_Design", session);
			String[] suffIDDataForm = new String[] { "", "AA" };

			dlg = new ServicePartCreate_Dialog(new Shell());
			dlg.create();

			dlg.cbMaterialGroup.setItems(materialGroupDataForm);
			dlg.cbMaterialGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					setItemID();
					if (dlg.cbMaterialGroup.getText().equals("SST")) {
						dlg.cbSupplierType.setText("B2P");
						dlg.cbSupplierType.setEnabled(false);
						dlg.cbPartMakeBuy.setText("Buy");
						dlg.cbPartMakeBuy.setEnabled(false);
						dlg.cbPurchaseLevel.setText("S");
						dlg.cbPurchaseLevel.setEnabled(false);
						dlg.cbTraceIndicator.deselectAll();
						dlg.cbTraceIndicator.setEnabled(true);
					} else {
						dlg.cbSupplierType.deselectAll();
						dlg.cbSupplierType.setEnabled(true);
						dlg.cbPartMakeBuy.deselectAll();
						dlg.cbPartMakeBuy.setEnabled(true);
						dlg.cbPurchaseLevel.deselectAll();
						dlg.cbPurchaseLevel.setEnabled(true);
						dlg.cbTraceIndicator.setText("No");
						dlg.cbTraceIndicator.setEnabled(false);
					}
					dlg.getShell().redraw();
				}
			});

			dlg.cbModelGroup.setItems(modelGroupDataForm.keySet().toArray(new String[0]));
			dlg.cbModelGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					if (isScooter()) {
						StringExtension.UpdateValueTextCombobox(dlg.cbDonorVehOrScooterVEHLine, vehicleLineDataForm);
						dlg.lblDonorVehicle.setText("Vehicle Line: (*)");
						try {
							dlg.cbDonorVehOrScooterVEHLine.setText(selectedObject.getItem().getProperty(PropertyDefines.ITEM_SVC_DONOR_VEH));
						} catch (TCException e) {
							e.printStackTrace();
						}
					} else {
						StringExtension.UpdateValueTextCombobox(dlg.cbDonorVehOrScooterVEHLine, donorVehicleDataForm);
						dlg.lblDonorVehicle.setText("Donor Vehicle: (*)");
						try {
							dlg.cbDonorVehOrScooterVEHLine.setText(selectedObject.getItem().getProperty(PropertyDefines.ITEM_SVC_DONOR_VEH));
						} catch (TCException e) {
							e.printStackTrace();
						}
					}
					setItemID();
				}
			});

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);
			dlg.cbPartMakeBuy.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					LinkedList<String> purchaseLevel = purchaseLevelValidate.get(dlg.cbPartMakeBuy.getText());
					dlg.cbPurchaseLevel.setItems(purchaseLevel.toArray(new String[0]));
				}
			});

			dlg.cbSuffID.setItems(suffIDDataForm);
			dlg.cbUom.setItems(uomDataForm);
			dlg.cbUom.setEnabled(false);
			dlg.cbUom.setText("PC");

			dlg.cbSupplierType.setItems(supplierTypeDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbTraceIndicator, partTraceabilityDataForm);

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					createNewObject(session, selectedObject);
				}
			});

			dlg.open();
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
		return null;
	}

	private void createNewObject(TCSession session, TCComponentBOMLine selectedObject) {
		try {
			String objectItemType = "";
			String objectRevType = "";
			if (!createValidate()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dms = DataManagementService.getService(session);
			if (dlg.cbModelGroup.getText().toLowerCase().contains("scooter")) {
				objectItemType = PropertyDefines.TYPE_SCOOTER;
				objectRevType = PropertyDefines.TYPE_ES_PART_REVISION;
			} else {
				if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SVC")) {
					objectItemType = PropertyDefines.TYPE_DESIGN;
					objectRevType = PropertyDefines.TYPE_DESIGN_REVISION;
				}
				if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SVK")) {
					objectItemType = PropertyDefines.TYPE_SVC_KIT;
					objectRevType = PropertyDefines.TYPE_SVC_KIT_REVISION;
				}
				if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SST")) {
					objectItemType = PropertyDefines.TYPE_SVC_TOOL;
					objectRevType = PropertyDefines.TYPE_SVC_TOOL_REVISION;
				}
			}
			String id = dlg.txtID.getText() + dlg.cbSuffID.getText();
			String materialGroup = dlg.cbMaterialGroup.getText();
			String refNumber = dlg.txtReferenceNumber.getText();
			String vnDesc = dlg.txtVietnameseDesc.getText();
			String colorCode = dlg.txtColorCode.getText();
			String name = dlg.txtName.getText();
			String partMakeBuy = dlg.cbPartMakeBuy.getText();
			String donorVehicle = (String) dlg.cbDonorVehOrScooterVEHLine.getData(dlg.cbDonorVehOrScooterVEHLine.getText());
			String traceIndicator = (String) dlg.cbTraceIndicator.getData(dlg.cbTraceIndicator.getText());
			String purchaseLevel = dlg.cbPurchaseLevel.getText();
			TCComponent uomTag = TCExtension.GetUOMItem(dlg.cbUom.getText());

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectItemType;
			itemDef.data.stringProps.put(PropertyDefines.ITEM_ID, id);
			itemDef.data.stringProps.put(PropertyDefines.ITEM_NAME, name);
			itemDef.data.stringProps.put(PropertyDefines.ITEM_MAKE_BUY, partMakeBuy);
			itemDef.data.stringProps.put(PropertyDefines.PROP_COLOR_CODE, colorCode);
			itemDef.data.stringProps.put(PropertyDefines.PROP_OBJ_DESC, vnDesc);
			itemDef.data.stringProps.put(PropertyDefines.PROP_AFS_RELEVENT, "True");
			itemDef.data.stringProps.put(PropertyDefines.ITEM_IS_TRACEABLE, traceIndicator);

			CreateInput revDef = new CreateInput();
			revDef.boName = objectRevType;
			revDef.stringProps.put(PropertyDefines.PROP_OBJ_DESC, vnDesc);
			revDef.stringProps.put(PropertyDefines.PROP_VIET_DESC, vnDesc);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });
			if (objectRevType.compareToIgnoreCase(PropertyDefines.TYPE_ES_PART_REVISION) == 0) {
				itemDef.data.stringProps.put(PropertyDefines.PROP_MAT_GROUP, materialGroup);
				itemDef.data.stringProps.put(PropertyDefines.ITEM_VEHICLE_LINE, donorVehicle);
				revDef.stringProps.put(PropertyDefines.PROP_REF_NUMBER, refNumber);
			} else {
				itemDef.data.stringProps.put(PropertyDefines.ITEM_DONOR_VEHICLE2, donorVehicle);
				itemDef.data.stringProps.put(PropertyDefines.ITEM_SUPPLIER_TYPE, (String) dlg.cbSupplierType.getData(dlg.cbSupplierType.getText()));
				revDef.stringProps.put(PropertyDefines.PROP_REF_NUMBER2, refNumber);
				revDef.stringArrayProps.put(PropertyDefines.ITEM_DONOR_VEHICLE, new String[] { donorVehicle });
			}

			itemDef.data.tagProps.put(PropertyDefines.ITEM_UOM, uomTag);

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				CreateOut[] output = response.output;
				TCComponent[] objects = output[0].objects;
				for (TCComponent obj : objects) {
					if (obj instanceof TCComponentItemRevision) {
						TCComponentItemRevision revision = (TCComponentItemRevision) obj;
						TCComponentBOMLine child = selectedObject.add(revision.getItem(), revision, null, false);
						child.setProperty(PropertyDefines.BOM_VL5_PUR_LEVEL, purchaseLevel);
						selectedObject.window().save();
					}
				}
				dlg.setMessage("Create new item success.", IMessageProvider.INFORMATION);
				resetUIDefault();
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			MessageBox.post("Creation fail!\nPlease contact your administrators.", "Error", MessageBox.ERROR);
		}
	}

	private boolean createValidate() {
		if (dlg.cbMaterialGroup.getText().isEmpty())
			return false;
		if (dlg.cbModelGroup.getText().isEmpty())
			return false;
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.cbDonorVehOrScooterVEHLine.getText().isEmpty())
			return false;

		return true;
	}

	private void setItemID() {
		String objectItemType = "";
		if (!dlg.cbMaterialGroup.getText().isEmpty() && !dlg.cbModelGroup.getText().isEmpty()) {
			if (dlg.cbModelGroup.getText().toLowerCase().contains("scooter")) {
				objectItemType = PropertyDefines.TYPE_SCOOTER;
			} else {
				if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SVC"))
					objectItemType = PropertyDefines.TYPE_DESIGN;
				else if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SVK"))
					objectItemType = PropertyDefines.TYPE_SVC_KIT;
				else if (dlg.cbMaterialGroup.getText().equalsIgnoreCase("SST"))
					objectItemType = PropertyDefines.TYPE_SVC_TOOL;
			}
			String value = generateNextID(objectItemType + ";" + PropertyDefines.TYPE_DESIGN);
			dlg.txtID.setText(value);
		}
	}

	private String generateNextID(String type) {
		try {
			String newIDValue = "";
			String materialGroup = dlg.cbMaterialGroup.getText();
			String modelGroupIdNumber = modelGroupDataForm.get(dlg.cbModelGroup.getText());
			String idPrefix = materialGroup + modelGroupIdNumber;
			String searchStringForLatestID = idPrefix + "*";

			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", searchStringForLatestID);
			inputQuery.put("Type", type);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");
			if (item_search == null || item_search.length == 0) {
				newIDValue = materialGroup + modelGroupIdNumber + StringExtension.ConvertNumberToString(0, 4);
			} else {
				int id = 0;
				String split = item_search[0].toString().substring(7, 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);
				newIDValue = materialGroup + modelGroupIdNumber + StringExtension.ConvertNumberToString(id + 1, 4);
			}

			return newIDValue;
		} catch (Exception ex) {
			return null;
		}
	}

	private boolean isScooter() {
		return dlg.cbModelGroup.getText().toLowerCase().contains("scooter");
	}

	private void resetUIDefault() {
		dlg.cbMaterialGroup.deselectAll();
		dlg.cbModelGroup.deselectAll();
		dlg.txtID.setText("");
		dlg.cbSuffID.deselectAll();
		dlg.txtName.setText("");
		dlg.txtReferenceNumber.setText("");
		dlg.txtColorCode.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.txtVietnameseDesc.setText("");
		dlg.cbDonorVehOrScooterVEHLine.deselectAll();
		dlg.cbUom.deselectAll();
		dlg.cbPurchaseLevel.deselectAll();
		dlg.cbSupplierType.deselectAll();
		dlg.cbTraceIndicator.deselectAll();
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		if (targetComponents[0] instanceof TCComponentBOMLine) {
			selectedObject = (TCComponentBOMLine) targetComponents[0];
		}
		if (selectedObject == null)
			return true;

		try {
			if (!selectedObject.getItemRevision().getType().equals("VF7_service_chptRevision"))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
