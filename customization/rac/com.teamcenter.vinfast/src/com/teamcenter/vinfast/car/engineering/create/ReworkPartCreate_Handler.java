package com.teamcenter.vinfast.car.engineering.create;

import java.util.ArrayList;
import java.util.Collections;
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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ReworkPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ReworkPartCreate_Dialog dlg;
	private TCComponentItemRevision selectedRev = null;
	private BasePartInfo basePartInfo = null;
	private String OBJECT_TYPE = "VF3_manuf_part";

	public class BasePartInfo {
		public String bPartId;
		public String bPartName;
		public String bDesc;
		public String bMakeBuy;
		public String bPartCategory;
		public String bDonorVeh;
		public String bPartTracebility;
		public String bUOM;
		public String bLongShortLead;
		public String bSupType;
		public String bManufCompo;
		public String[] bCuvVehType;
		public String bIsAfterSaleRelevant;
		public String bSoftwarePartType;
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		try {
			if (targetComp.length > 1) {
				MessageBox.post("Please select single part", "WARNING", MessageBox.WARNING);
				return null;
			} else {
				// TODO get info from base part
				if (targetComp[0] instanceof TCComponentBOMLine) {
					TCComponentBOMLine bom = (TCComponentBOMLine) targetComp[0];
					selectedRev = bom.getItemRevision();
					String objectType = selectedRev.getPropertyDisplayableValue("object_type");
					if (objectType.compareToIgnoreCase("VF Design Revision") != 0) {
						MessageBox.post("This function is not applicable for " + objectType, "WARNING", MessageBox.WARNING);
					}
				} else {
					selectedRev = (TCComponentItemRevision) targetComp[0];
				}

				basePartInfo = getBasePartInfo(selectedRev);
			}

			// Init data
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE, session);
			String[] partCategoryDataForm = TCExtension.GetLovValues("vf4_part_category", OBJECT_TYPE, session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] supplierTypeDataForm = TCExtension.GetLovValues("vf4_supplier_type", OBJECT_TYPE, session);
			LinkedHashMap<String, String> partTraceabilityDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", OBJECT_TYPE, session);

			dlg = new ReworkPartCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create Re-work Part");
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);
			dlg.cbPartCategory.setItems(partCategoryDataForm);
			dlg.cbUoM.setItems(uomDataForm);
			dlg.cbSupplierType.setItems(supplierTypeDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceabilityDataForm);

			dlg.txtID.setText((String) basePartInfo.bPartId.subSequence(0, 11));
			dlg.txtSuffID.setText(getNewSuffixPartNum(basePartInfo.bPartId));
			dlg.txtName.setText(basePartInfo.bPartName);
			dlg.txtDescription.setText(basePartInfo.bDesc);
			dlg.cbPartMakeBuy.setText(basePartInfo.bMakeBuy);
			dlg.cbPartCategory.setText(basePartInfo.bPartCategory);
			dlg.cbSupplierType.setText(basePartInfo.bSupType);
			dlg.cbUoM.setText(basePartInfo.bUOM);
			dlg.cbSupplierType.setText(basePartInfo.bSupType);
			dlg.cbPartTraceability.setText(basePartInfo.bPartTracebility);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			dlg.open();
		} catch (TCException | NotLoadedException e) {
			e.printStackTrace();
		}

		return null;
	}

	public BasePartInfo getBasePartInfo(TCComponentItemRevision basePartCompo) throws NotLoadedException, TCException {
		BasePartInfo out = new BasePartInfo();
		if (basePartCompo != null) {
			out.bPartId = basePartCompo.getPropertyDisplayableValue("item_id");
			out.bPartName = basePartCompo.getPropertyDisplayableValue("object_name");
			out.bDesc = basePartCompo.getPropertyDisplayableValue("object_desc");
			out.bMakeBuy = basePartCompo.getItem().getPropertyDisplayableValue("vf4_item_make_buy");
			out.bPartCategory = basePartCompo.getItem().getPropertyDisplayableValue("vf4_part_category");
			out.bDonorVeh = basePartCompo.getItem().getPropertyDisplayableValue("vf4_donor_vehicle");
			out.bPartTracebility = basePartCompo.getItem().getPropertyDisplayableValue("vf4_item_is_traceable");
			out.bUOM = basePartCompo.getItem().getPropertyDisplayableValue("uom_tag");
			out.bLongShortLead = basePartCompo.getItem().getPropertyDisplayableValue("vf4_long_short_lead");
			out.bSupType = basePartCompo.getItem().getPropertyDisplayableValue("vf4_supplier_type");
			out.bManufCompo = basePartCompo.getItem().getPropertyDisplayableValue("vf4_manu_component");
			out.bCuvVehType = basePartCompo.getPropertyDisplayableValue("vf4_cuv_veh_type").split(",");
			out.bSoftwarePartType = basePartCompo.getItem().getPropertyDisplayableValue("vf4_software_part_type");
			out.bIsAfterSaleRelevant = basePartCompo.getItem().getPropertyDisplayableValue("vf4_manu_component");
		}
		return out;
	}

	public String getNewSuffixPartNum(String currPartNum) throws NotLoadedException {
		String id = currPartNum.substring(0, 11);
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
		queryInput.put("Item ID", "*" + id + "*");
		queryInput.put("Type", OBJECT_TYPE);

		TCComponent[] qryOut = Query.queryItem(session, queryInput, "Item...");
		ArrayList<String> lstSuffix = new ArrayList<String>();
		if (qryOut != null) {
			for (TCComponent com : qryOut) {
				String checkingId = com.getPropertyDisplayableValue("item_id");
				if (!currPartNum.contains("JF")) {
					// only get partnumber @@@NNNNNNNN@@, @@@NNNNNNNN@@@@@
					if (checkingId.length() != 13 || checkingId.contains("JF")) {
						continue;
					}
					lstSuffix.add(checkingId.substring(11, 13));
				} else {
					if (checkingId.length() != 16 || !checkingId.contains("JF")) {
						continue;
					}
					lstSuffix.add(checkingId.substring(11, 13));
				}
			}
		}
		if (lstSuffix.size() >= 1) {
			Collections.sort(lstSuffix);
			Collections.reverse(lstSuffix);
			int charValue = lstSuffix.get(0).charAt(1);
			String next = String.valueOf((char) (charValue + 1));
			return lstSuffix.get(0).charAt(0) + next;
		} else {
			return "HA";
		}
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			} else {
				DataManagementService dms = DataManagementService.getService(session);
				String id = "";
				id = dlg.txtID.getText() + dlg.txtSuffID.getText();
				String name = dlg.txtName.getText();
				String description = dlg.txtDescription.getText();
				String partMakeBuy = dlg.cbPartMakeBuy.getText();
				String partCategory = dlg.cbPartCategory.getText();
				String uom = dlg.cbUoM.getText();
				TCComponent UOMTag = TCExtension.GetUOMItem(uom);
				String supplierType = dlg.cbSupplierType.getText();

				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = OBJECT_TYPE;
				itemDef.data.stringProps.put("item_id", id);
				itemDef.data.stringProps.put("object_name", name);
				itemDef.data.stringProps.put("object_desc", description);
				itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
				itemDef.data.stringProps.put("vf4_part_category", partCategory);
				itemDef.data.tagProps.put("uom_tag", UOMTag);
				itemDef.data.stringProps.put("vf4_supplier_type", supplierType);
				itemDef.data.stringProps.put("vf4_orginal_part_number", basePartInfo.bPartId);

				CreateInput revDef = new CreateInput();
				revDef.boName = OBJECT_TYPE + "Revision";
				revDef.stringProps.put("object_desc", description);

				itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

				CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

				if (response.serviceData.sizeOfPartialErrors() == 0) {
					TCComponent cfgContext = null;
					try {
						cfgContext = response.output[0].objects[0];
						session.getUser().getNewStuffFolder().add("contents", cfgContext);

						// TODO create event REV_COPY_OLD_PART_INFO
						PostEventObjectProperties[] postEvtInputs = new PostEventObjectProperties[1];
						postEvtInputs[0] = new PostEventObjectProperties();
						postEvtInputs[0].primaryObject = ((TCComponentItem) cfgContext).getRelatedComponents("revision_list")[0];
						dms.postEvent(postEvtInputs, "Fnd0MultiSite_Unpublish");
					} catch (TCException e1) {
						MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
					}
					dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
					openOnCreate(cfgContext);
					resetDialog();
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

	private Boolean checkRequired() {
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		if (dlg.cbPartCategory.getText().isEmpty())
			return false;
		if (dlg.cbUoM.getText().isEmpty())
			return false;

		return true;
	}

	private void resetDialog() {
		dlg.txtID.setText("");
		dlg.txtSuffID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.cbPartCategory.deselectAll();
		dlg.cbUoM.deselectAll();
		dlg.cbSupplierType.deselectAll();
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
