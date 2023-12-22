package com.teamcenter.vinfast.general.create.mepart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ScooterPartCreate_Composite extends MEPartCreateAbstract {
//	private Combo cbPartMakeBuy;
//	private Combo cbUOM;
//	private Combo cbPartTraceability;
//	private Combo cbPartCategory;
//	private Combo cbVehicleLine;
//
//	private Text txtID;
//	private Text txtName;
//	private Text txtDesc;
//	private Text txtPartReference;
//	private Text txtPartNameVietnamese;
//
//	private Button rbtIsAfterSaleTrue;
//	private Button rbtIsAfterSaleFalse;

	public ScooterPartCreate_Composite(Composite c) {
		super(c);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		setLayout(new GridLayout(3, false));

		Label lblId = new Label(this, SWT.NONE);
		lblId.setText("ID: (*)");
		lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		txtID = new Text(this, SWT.BORDER);
		txtID.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtID.setEnabled(false);
		txtID.setEditable(false);
		txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: (*)");
		lblName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtName = new Text(this, SWT.BORDER);

		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		lblDescription.setText("Description: (*)");
		lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		txtDesc = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_txtDesc.heightHint = 44;
		txtDesc.setLayoutData(gd_txtDesc);

		Label lblPartMakeBuy = new Label(this, SWT.NONE);
		lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartMakeBuy.setText("Part Make/Buy: (*)");
		lblPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbPartMakeBuy = new Combo(this, SWT.READ_ONLY);
		cbPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblUnitOfMeasure = new Label(this, SWT.NONE);
		lblUnitOfMeasure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUnitOfMeasure.setText("Unit Of Measure: (*)");
		lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbUOM = new Combo(this, SWT.READ_ONLY);
		cbUOM.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPartTraceabilityIndicator = new Label(this, SWT.NONE);
		lblPartTraceabilityIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartTraceabilityIndicator.setText("Part Traceability Indicator:");
		lblPartTraceabilityIndicator.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblPartTraceabilityIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbPartTraceability = new Combo(this, SWT.READ_ONLY);
		cbPartTraceability.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblUnitOfMeasure_2 = new Label(this, SWT.NONE);
		lblUnitOfMeasure_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUnitOfMeasure_2.setText("Is After Sale Revelant:");
		lblUnitOfMeasure_2.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		rbtIsAfterSaleTrue = new Button(this, SWT.RADIO);
		rbtIsAfterSaleTrue.setText("True");
		rbtIsAfterSaleTrue.setEnabled(false);

		rbtIsAfterSaleFalse = new Button(this, SWT.RADIO);
		rbtIsAfterSaleFalse.setText("False");
		rbtIsAfterSaleFalse.setEnabled(false);

		Label lblUnitOfMeasure_3 = new Label(this, SWT.NONE);
		lblUnitOfMeasure_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUnitOfMeasure_3.setText("Part Category: (*)");
		lblUnitOfMeasure_3.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbPartCategory = new Combo(this, SWT.READ_ONLY);
		cbPartCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblEsModelvehicleLine = new Label(this, SWT.NONE);
		lblEsModelvehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblEsModelvehicleLine.setText("ES Model/Vehicle Line: (*)");
		lblEsModelvehicleLine.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblEsModelvehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbVehicleLine = new Combo(this, SWT.READ_ONLY);

		Label lblPartReference = new Label(this, SWT.NONE);
		lblPartReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartReference.setText("Part Reference:");
		lblPartReference.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblPartReference.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbVehicleLine.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbVehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		txtPartReference = new Text(this, SWT.BORDER);
		txtPartReference.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtPartReference.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtPartReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPartNameVietnamese = new Label(this, SWT.NONE);
		lblPartNameVietnamese.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartNameVietnamese.setText("Part Name Vietnamese:");
		lblPartNameVietnamese.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblPartNameVietnamese.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtPartNameVietnamese = new Text(this, SWT.BORDER);
		txtPartNameVietnamese.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtPartNameVietnamese.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtPartNameVietnamese.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	}

	@Override
	public void initData(String subCategory) {
		this.subCategory = subCategory;
		cbPartMakeBuy.setItems(makeBuyDataForm);
		cbUOM.setItems(uomDataForm);
		StringExtension.UpdateValueTextCombobox(cbPartTraceability, partTraceabilityDataForm);
		cbPartCategory.setItems(partCategoryDataForm);
		StringExtension.UpdateValueTextCombobox(cbVehicleLine, vehicleLineDataForm);
		updateUI();

		String inputString = subCategory;
		inputString += modelAndPrefixNumber.get("Scooter");
		txtID.setText(generateNextID(inputString, "ME Scooter"));
	}

	@Override
	public boolean checkRequired() {
		if (!checkRequiredCommon())
			return false;

		if (cbVehicleLine.getText().isEmpty())
			return false;

		return true;
	}

	@Override
	public String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate) {
		String message = "";
		DataManagementService dms = DataManagementService.getService(session);
		String objectType = "VF3_me_scooter";
		String id = txtID.getText();
		String name = txtName.getText();
		String partMakeBuy = cbPartMakeBuy.getText();
		String description = txtDesc.getText();
		String uom = cbUOM.getText();
		String partTraceability = "";
		if (!cbPartTraceability.getText().isEmpty())
			partTraceability = (String) cbPartTraceability.getData(cbPartTraceability.getText());

		String partCategory = cbPartCategory.getText();
		Boolean isAfterSale = rbtIsAfterSaleTrue.getSelection();
		// for Scooter
		String vehicleLine = (String) cbVehicleLine.getData(cbVehicleLine.getText());
		String partRef = txtPartReference.getText();
		String partNameViet = txtPartNameVietnamese.getText();
		TCComponent UOMTag = null;
		try {
			UOMTag = TCExtension.GetUOMItem(uom);

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.boolProps.put("vf4_itm_after_sale_relevant", isAfterSale);
			itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);
			itemDef.data.stringProps.put("vf4_part_category", partCategory);
			itemDef.data.stringProps.put("vf4_es_model_veh_line", vehicleLine);
			itemDef.data.stringProps.put("vf4_part_reference", partRef);
			// -------------------------
			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = objectType + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			itemRevisionDef.stringProps.put("vf3_viet_desciption", partNameViet);
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
								message = "Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder.";
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							message = "Created successfully, new item has been copied to your Newstuff folder";
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					message = "Create unsuccessfully, please contact with administrator.";
				}
			} else {
				MessageBox.post(TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	private void resetDialog() {
		txtID.setText("");
		txtName.setText("");
		txtDesc.setText("");
		cbPartTraceability.deselectAll();
		cbVehicleLine.deselectAll();
		txtPartNameVietnamese.setText("");
		txtPartReference.setText("");
	}
}
