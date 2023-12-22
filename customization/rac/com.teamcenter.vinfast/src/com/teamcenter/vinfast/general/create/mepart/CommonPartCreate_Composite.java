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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchPartRev_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class CommonPartCreate_Composite extends MEPartCreateAbstract {
//	private Combo cbModel;
//	private Combo cbPartMakeBuy;
//	private Combo cbUOM;
//	private Combo cbPartTraceability;
//	private Combo cbPartCategory;
//	private Combo cbSupplierType;
//
//	private Text txtID;
//	private Text txtName;
//	private Text txtDesc;
//
//	private Button rbtIsAfterSaleTrue;
//	private Button rbtIsAfterSaleFalse;
	public CommonPartCreate_Composite(Composite c) {
		super(c);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		setLayout(new GridLayout(3, false));

		Label lblModel = new Label(this, SWT.NONE);
		lblModel.setText("Model: (*)");
		lblModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		cbModel = new Combo(this, SWT.READ_ONLY);
		cbModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblId = new Label(this, SWT.NONE);
		lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblId.setText("ID: (*)");
		lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

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
		txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText("Description: (*)");
		lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtDesc = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_txtDesc.heightHint = 40;
		txtDesc.setLayoutData(gd_txtDesc);
		txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
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
		lblUnitOfMeasure.setText("Unit Of Measure: (*)");
		lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblUnitOfMeasure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

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
		lblUnitOfMeasure_2.setText("Is After Sale Revelant:");
		lblUnitOfMeasure_2.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_composite.heightHint = 23;
		composite.setLayoutData(gd_composite);

		rbtIsAfterSaleTrue = new Button(composite, SWT.RADIO);

		rbtIsAfterSaleFalse = new Button(composite, SWT.RADIO);

		Label lblUnitOfMeasure_3 = new Label(this, SWT.NONE);
		lblUnitOfMeasure_3.setText("Part Category: (*)");
		lblUnitOfMeasure_3.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblUnitOfMeasure_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblUnitOfMeasure_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		rbtIsAfterSaleTrue.setText("True");
		rbtIsAfterSaleTrue.setEnabled(false);

		cbPartCategory = new Combo(this, SWT.READ_ONLY);
		rbtIsAfterSaleFalse.setText("False");
		rbtIsAfterSaleFalse.setEnabled(false);

		Label lblSupplierType = new Label(this, SWT.NONE);
		lblSupplierType.setText("Supplier Type:");
		lblSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		cbSupplierType = new Combo(this, SWT.READ_ONLY);
		cbPartCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblLink = new Label(this, SWT.NONE);
		lblLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblLink.setText("Link:");
		lblLink.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		lblLink.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtLinkPart = new Text(this, SWT.BORDER | SWT.READ_ONLY);

		btnRemoveLink = new Button(this, SWT.NONE);
		txtLinkPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtLinkPart.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SearchPartRev_Dialog searchDlg = new SearchPartRev_Dialog(c.getShell());
				searchDlg.open();
				Button accept = searchDlg.getOKButton();

				accept.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						int index = searchDlg.tblSearch.getSelectionIndex();

						TCComponent item = searchDlg.itemSearch.get(index);
						try {
							String objectItem = item.getPropertyDisplayableValue("object_string");
							txtLinkPart.setText(objectItem);
							txtLinkPart.setData(item);
						} catch (Exception e2) {
							e2.printStackTrace();
						}

						searchDlg.getShell().dispose();
					}
				});
			}
		});
		btnRemoveLink.setText("Remove");
		btnRemoveLink.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				txtLinkPart.setText("");
				txtLinkPart.setData(null);
			}
		});

		cbSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
		cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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

	private void fillRawMaterialAttribute() {
		if (parentPart != null) {
			try {
				String parentType = parentPart.getPropertyDisplayableValue("object_type");
				String objectItem = parentPart.getPropertyDisplayableValue("object_string");
				txtLinkPart.setText(objectItem);
				txtLinkPart.setData(parentPart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean checkRequired() {
		if (!checkRequiredCommon())
			return false;
		if (cbModel.getText().isEmpty())
			return false;

		return true;
	}

	@Override
	public String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate) {
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
								if (isOpenOnCreate)
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
				MessageBox.post(TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return message;
	}

	private void createRelation(TCComponentItemRevision itemRev) {
		try {
			if (!txtLinkPart.getText().isEmpty()) {
				itemRev.setRelated("VF4_H_Part_Relation", new TCComponent[] { (TCComponent) txtLinkPart.getData() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		txtID.setText("");
		txtName.setText("");
		txtDesc.setText("");
		txtLinkPart.setText("");
		cbPartTraceability.deselectAll();
		cbSupplierType.deselectAll();
	}
}
