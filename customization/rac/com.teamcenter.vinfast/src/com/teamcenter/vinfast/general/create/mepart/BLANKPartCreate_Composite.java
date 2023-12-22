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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BLANKPartCreate_Composite extends MEPartCreateAbstract {
//	private Combo cbModel;
//	private Combo cbPartMakeBuy;
//	private Combo cbUOM;
//	private Combo cbPartTraceability;
//	private Combo cbPartCategory;
//	private Combo cbSupplierType;
//	private Combo cbMaterial;
//	private Combo cbCoating;
//	private Combo cbBlankType;
//
//	private Text txtID;
//	private Text txtName;
//	private Text txtDesc;
//	private Text txtLinkPart;
//
//	private Spinner txtThickness;
//	private Spinner txtWidth;
//	private Spinner txtLength;
//
//	private Button rbtIsAfterSaleTrue;
//	private Button rbtIsAfterSaleFalse;
//	private Button btnAutoFill;
//	private Button ckbIsPurchase;
//
//	private Label lblNameWarning;

	public BLANKPartCreate_Composite(Composite c) {
		super(c);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		Listener blankGenNameListener = new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				generateBLANKName();
			}
		};

		Label lblModel = new Label(this, SWT.NONE);
		lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModel.setText("Model: (*)");
		lblModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbModel = new Combo(this, SWT.READ_ONLY);
		cbModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblId = new Label(this, SWT.NONE);
		lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblId.setText("ID: (*)");
		lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtID = new Text(this, SWT.BORDER);
		txtID.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		txtID.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtID.setEnabled(false);
		txtID.setEditable(false);
		txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblBlankType = new Label(this, SWT.NONE);
		lblBlankType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblBlankType.setText("Blank Type:");
		lblBlankType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblBlankType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbBlankType = new Combo(this, SWT.READ_ONLY);
		cbBlankType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbBlankType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBlankType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		cbBlankType.addListener(SWT.Modify, blankGenNameListener);

		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: (*)");
		lblName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtName = new Text(this, SWT.BORDER);
		txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		btnAutoFill = new Button(this, SWT.NONE);
		btnAutoFill.setText("Validate");

		lblNameWarning = new Label(this, SWT.NONE);
		lblNameWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		lblNameWarning.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblNameWarning.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.ITALIC));
		btnAutoFill.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				blankValidate();
			}
		});

		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		lblDescription.setText("Description: (*)");
		lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtDesc = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
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
		cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblUnitOfMeasure = new Label(this, SWT.NONE);
		lblUnitOfMeasure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUnitOfMeasure.setText("Unit Of Measure: (*)");
		lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbUOM = new Combo(this, SWT.READ_ONLY);
		cbUOM.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblPartTraceabilityIndicator = new Label(this, SWT.NONE);
		lblPartTraceabilityIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartTraceabilityIndicator.setText("Part Traceability Indicator:");
		lblPartTraceabilityIndicator.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblPartTraceabilityIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbPartTraceability = new Combo(this, SWT.READ_ONLY);
		cbPartTraceability.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

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
		new Label(this, SWT.NONE);

		Label lblUnitOfMeasure_3 = new Label(this, SWT.NONE);
		lblUnitOfMeasure_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUnitOfMeasure_3.setText("Part Category: (*)");
		lblUnitOfMeasure_3.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbPartCategory = new Combo(this, SWT.READ_ONLY);
		cbPartCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblSupplierType = new Label(this, SWT.NONE);
		lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSupplierType.setText("Supplier Type:");
		lblSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbSupplierType = new Combo(this, SWT.READ_ONLY);
		cbSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblNewLabel_1 = new Label(this, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Material: (*)");
		lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbMaterial = new Combo(this, SWT.READ_ONLY);
		cbMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		cbMaterial.addListener(SWT.Modify, blankGenNameListener);

		Label lblNewLabel_2 = new Label(this, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Coating:");
		lblNewLabel_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbCoating = new Combo(this, SWT.READ_ONLY);
		cbCoating.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbCoating.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		cbCoating.addListener(SWT.Modify, blankGenNameListener);

		Label lblNewLabel_3 = new Label(this, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Thickness: (*)");
		lblNewLabel_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtThickness = new Spinner(this, SWT.BORDER);
		txtThickness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		txtThickness.setMaximum(1000000);
		txtThickness.setIncrement(10);
		txtThickness.setDigits(2);
		txtThickness.addListener(SWT.Modify, blankGenNameListener);

		Label lblNewLabel_6 = new Label(this, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("Width: (*)");
		lblNewLabel_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtWidth = new Spinner(this, SWT.BORDER);
		txtWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		txtWidth.setMaximum(1000000);
		txtWidth.addListener(SWT.Modify, blankGenNameListener);

		Label lblNewLabel_7 = new Label(this, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("Length: (*)");
		lblNewLabel_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtLength = new Spinner(this, SWT.BORDER);
		txtLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		txtLength.setMaximum(1000000);
		txtLength.addListener(SWT.Modify, blankGenNameListener);

		Label lblIsMotherCoil_1 = new Label(this, SWT.NONE);
		lblIsMotherCoil_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblIsMotherCoil_1.setText("Is Purchase Part:");
		lblIsMotherCoil_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		ckbIsPurchase = new Button(this, SWT.CHECK);
		ckbIsPurchase.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		Label lblNewLabel_9 = new Label(this, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("Link:");
		lblNewLabel_9.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtLinkPart = new Text(this, SWT.BORDER);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		txtLinkPart.setEditable(false);
		txtLinkPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		new Label(this, SWT.NONE);
	}

	@Override
	public void initData(String subCategory) {
		this.subCategory = subCategory;
		StringExtension.UpdateValueTextCombobox(cbModel, modelAndDisplayName);
		cbPartMakeBuy.setItems(makeBuyDataForm);
		cbUOM.setItems(uomDataForm);
		StringExtension.UpdateValueTextCombobox(cbPartTraceability, partTraceabilityDataForm);
		cbPartCategory.setItems(partCategoryDataForm);
		cbSupplierType.setItems(supplierTypeDataForm);
		cbMaterial.setItems(materialDataForm);
		cbCoating.setItems(coatingDataForm.toArray(new String[0]));
		String[] blankTypeDataForm = new String[] { "BLANK", "SHA", "REC" };
		cbBlankType.setItems(blankTypeDataForm);
		cbBlankType.select(0);

		updateUI();
		updateUIWhenMakeBuyChange();

		cbModel.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				if (cbModel.getText().isEmpty()) {
					txtID.setText("");
					return;
				}
				String inputString = subCategory;
				inputString += modelAndPrefixNumber.get(cbModel.getData(cbModel.getText()));
				txtID.setText(generateNextID(inputString, "ME Part"));
			}
		});

		cbPartMakeBuy.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updateUIWhenMakeBuyChange();
			}
		});
		fillRawMaterialAttribute();
	}

	@Override
	public boolean checkRequired() {
		if (!checkRequiredCommon())
			return false;
		if (cbModel.getText().isEmpty())
			return false;

		String name = txtName.getText();
		if (!blankValidate()) {
			return false;
		}
		txtName.setText(name);
		lblNameWarning.setText("");
		if (cbMaterial.getText().isEmpty())
			return false;
		if (txtThickness.getText().isEmpty() || txtThickness.getText().compareToIgnoreCase("0.00") == 0)
			return false;
		if (txtWidth.getText().isEmpty() || txtWidth.getText().compareToIgnoreCase("0") == 0)
			return false;
		if (txtLength.getText().isEmpty() || txtLength.getText().compareToIgnoreCase("0") == 0)
			return false;

		return true;
	}

	@Override
	public String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate) {
		lblNameWarning.setText("");
		String message = "";
		DataManagementService dms = DataManagementService.getService(session);
		String objectType = "VF3_manuf_part";
		String id = txtID.getText();
		String name = txtName.getText();
		String model = (String) cbModel.getData(cbModel.getText());
		String partMakeBuy = cbPartMakeBuy.getText();
		String description = txtDesc.getText();
		String uom = cbUOM.getText();
		String partTraceability = "";
		if (!cbPartTraceability.getText().isEmpty())
			partTraceability = (String) cbPartTraceability.getData(cbPartTraceability.getText());

		String partCategory = cbPartCategory.getText();
		Boolean isAfterSale = rbtIsAfterSaleTrue.getSelection();
		String supplierType = cbSupplierType.getText();
		// for BLANK Part
		boolean isBlankPurchase = ckbIsPurchase.getSelection();
		String blankMaterial = cbMaterial.getText();
		String blankCoating = cbCoating.getText();
		double blankThickness = 0;
		try {
			blankThickness = Double.parseDouble(txtThickness.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		double blankWidth = 0;
		try {
			blankWidth = Double.parseDouble(txtWidth.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		double blankLength = 0;
		try {
			blankLength = Double.parseDouble(txtLength.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!checkPartExistSpec(blankMaterial, blankCoating, blankThickness, blankWidth, blankLength)) {
			return "Create unsuccessfully, please contact with administrator.";
		}

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
			itemDef.data.stringProps.put("vf4_donor_veh", model);
			itemDef.data.stringProps.put("vf4_me_part_type", subCategory);
			itemDef.data.stringProps.put("vf4_supplier_type", supplierType);

			itemDef.data.stringProps.put("vf4_COI_material", blankMaterial);
			itemDef.data.stringProps.put("vf4_coating", blankCoating);
			itemDef.data.doubleProps.put("vf4_thickness", blankThickness);
			itemDef.data.doubleProps.put("vf4_width", blankWidth);
			itemDef.data.doubleProps.put("vf4_length", blankLength);
			itemDef.data.boolProps.put("vf4_is_purchase_ME_part", isBlankPurchase);
			// -------------------------

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = objectType + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
				createRelation(itemRev);
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
							if (isOpenOnCreate)
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
				TCExtension.hanlderServiceData(response.serviceData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	private boolean blankValidate() {
		boolean isFormatValid = true;
		String name = txtName.getText();
		if (name.contains("_")) {
			String[] str = name.split("_");
			if (str[0].compareToIgnoreCase("BLANK") != 0 && str[0].compareToIgnoreCase("SHA") != 0 && str[0].compareToIgnoreCase("REC") != 0) {
				isFormatValid = false;
//				lblNameWarning.setText("Prefix name only available for BLANK.");
			} else {
				if (str.length == 6) {
					cbBlankType.setText(str[0]);
					// material
					if (checkExistInLOV(str[1], cbMaterial.getItems())) {
						cbMaterial.setText(str[1]);
					} else {
						cbMaterial.deselectAll();
						isFormatValid = false;
					}
					// coating
					if (checkExistInLOV(str[2], cbCoating.getItems())) {
						cbCoating.setText(str[2]);
					} else {
						cbCoating.deselectAll();
						isFormatValid = false;
					}
					// thickness
					if (StringExtension.isInteger(str[3], 10)) {
						txtThickness.setSelection(Integer.parseInt(str[3]));
					} else {
						txtThickness.setSelection(0);
					}
					// width
					if (StringExtension.isInteger(str[4], 10)) {
						txtWidth.setSelection(Integer.parseInt(str[4]));
					} else {
						txtWidth.setSelection(0);
					}
					// length
					if (StringExtension.isInteger(str[5], 10)) {
						txtLength.setSelection(Integer.parseInt(str[5]));
					} else {
						txtLength.setSelection(0);
					}
				} else if (str.length == 5) {
					// material
					if (checkExistInLOV(str[1], cbMaterial.getItems())) {
						cbMaterial.setText(str[1]);
					} else {
						cbMaterial.deselectAll();
						isFormatValid = false;
					}
					// thickness
					if (StringExtension.isInteger(str[2], 10)) {
						txtThickness.setSelection(Integer.parseInt(str[2]));
					} else {
						txtThickness.setSelection(0);
					}
					// width
					if (StringExtension.isInteger(str[3], 10)) {
						txtWidth.setSelection(Integer.parseInt(str[3]));
					} else {
						txtWidth.setSelection(0);
					}
					// length
					if (StringExtension.isInteger(str[4], 10)) {
						txtLength.setSelection(Integer.parseInt(str[4]));
					} else {
						txtLength.setSelection(0);
					}
					cbCoating.deselectAll();
				} else {
					isFormatValid = false;
				}
				txtName.setText(name);
			}
		} else {
			isFormatValid = false;
		}
		if (isFormatValid)
			lblNameWarning.setText("");
		else
			lblNameWarning.setText("Please follow naming rule: Prefix_Material_Coating_Thickness_Width_Length. Prefix: BLANK or SHA or REC.");

		return isFormatValid;
	}

	private boolean checkExistInLOV(String input, String[] lov) {
		for (String value : lov) {
			if (value.compareToIgnoreCase(input) == 0)
				return true;
		}

		return false;
	}

	private void generateBLANKName() {
		lblNameWarning.setText("");

		String material = cbMaterial.getText();
		String coating = cbCoating.getText();
		String thickness = txtThickness.getText();
		String width = txtWidth.getText();
		String length = txtLength.getText();

		int thicknessValue = 0;
		if (StringExtension.isDouble(thickness)) {
			Double thicknessValueDouble = (Double.parseDouble(thickness) * 100);
			thicknessValue = thicknessValueDouble.intValue();
		}
		if (String.valueOf(thicknessValue).length() < 3)
			thickness = "0" + String.valueOf(thicknessValue);
		else
			thickness = String.valueOf(thicknessValue);

		String prefix = cbBlankType.getText();
		txtName.setText(prefix + "_" + material + (coating.isEmpty() ? "" : "_" + coating) + "_" + thickness + "_" + width + "_" + length);
	}

	private void createRelation(TCComponentItemRevision itemRev) {
		try {
			if (parentPart != null) {
				String parentType = parentPart.getPropertyDisplayableValue("object_type");
				if (parentType.compareTo("ME Part Revision") != 0) {
					itemRev.setRelated("VF4_H_Part_Relation", new TCComponent[] { parentPart });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		txtID.setText("");
		txtName.setText("");
		txtDesc.setText("");
		cbPartTraceability.deselectAll();
		cbMaterial.deselectAll();
		cbCoating.deselectAll();
		txtThickness.setSelection(0);
		txtWidth.setSelection(0);
		txtLength.setSelection(0);
		ckbIsPurchase.setSelection(false);
	}

	private void fillRawMaterialAttribute() {
		if (parentPart != null) {
			try {
				String parentType = parentPart.getPropertyDisplayableValue("object_type");
				String objectItem = parentPart.getPropertyDisplayableValue("object_string");
				txtLinkPart.setText(objectItem);
				if (parentType.compareTo("VF Design Revision") == 0) {
					String material = parentPart.getPropertyDisplayableValue("vf4_catia_material");
					String coating = parentPart.getPropertyDisplayableValue("vf4_cad_coating");
					String thickness = parentPart.getPropertyDisplayableValue("vf4_cad_thickness");

					cbMaterial.setText(material);
					cbCoating.setText(coating);
					if (StringExtension.isDouble(thickness)) {
						int thicknessConvert = (int) (Double.parseDouble(thickness) * 100);
						txtThickness.setSelection(thicknessConvert);
					}
				} else if (parentType.compareTo("ME Part Revision") == 0) {
					String material = parentPart.getItem().getPropertyDisplayableValue("vf4_COI_material");
					String coating = parentPart.getItem().getPropertyDisplayableValue("vf4_coating");
					String thickness = parentPart.getItem().getPropertyDisplayableValue("vf4_thickness");
					String width = parentPart.getItem().getPropertyDisplayableValue("vf4_width");
					String length = parentPart.getItem().getPropertyDisplayableValue("vf4_length");

					cbMaterial.setText(material);
					cbCoating.setText(coating);
					if (StringExtension.isDouble(thickness)) {
						txtThickness.setSelection((int) Double.parseDouble(thickness));
					}
					if (StringExtension.isDouble(width)) {
						txtWidth.setSelection((int) Double.parseDouble(width));
					}
					if (StringExtension.isDouble(length)) {
						txtLength.setSelection((int) Double.parseDouble(length));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
