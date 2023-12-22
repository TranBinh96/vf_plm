package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.Calendar;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Button;

public class VFFSDocumentCreate_Composite extends SpecDocumentCreateAbstract {
//	private Text txtID;
//	private Text txtName;
//	private Text txtDescription;
//	private Combo cbModelCode;
//	private Combo cbModuleName;
//	private Combo cbDomain;
//	private Combo cbModule;
//	private Combo cbWorkProduct;
//	private DateTime datTargetReleaseDate;
//	private Button ckbTargetReviseDate;
//	private DateTime datTargetReviseDate;

	public VFFSDocumentCreate_Composite(Composite c) {
		super(c);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		Listener genID = new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				if (cbModelCode.getText().isEmpty() || cbModuleName.getText().isEmpty()) {
					txtID.setText("");
					return;
				}

				String inputString = prefixName + (String) cbModelCode.getData(cbModelCode.getText()) + (String) cbModuleName.getData(cbModuleName.getText());
				txtID.setText(generateNextID(inputString, objectTypeMapping.get(prefixName)));
			}
		};

		Label lblId = new Label(this, SWT.NONE);
		lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblId.setText("ID: (*)");
		lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtID = new Text(this, SWT.BORDER);
		txtID.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		txtID.setEnabled(false);
		txtID.setEditable(false);
		txtID.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: (*)");
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtName = new Text(this, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText("Description:");
		lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtDescription = new Text(this, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblModelCode = new Label(this, SWT.NONE);
		lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblModelCode.setText("Model Code: (*)");

		cbModelCode = new Combo(this, SWT.READ_ONLY);
		cbModelCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		cbModelCode.addListener(SWT.Modify, genID);

		Label lblModuleName = new Label(this, SWT.NONE);
		lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblModuleName.setText("Module Name: (*)");

		cbModuleName = new Combo(this, SWT.READ_ONLY);
		cbModuleName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		cbModuleName.addListener(SWT.Modify, genID);

		Label lblDomain = new Label(this, SWT.NONE);
		lblDomain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDomain.setText("Domain: (*)");
		lblDomain.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbDomain = new Combo(this, SWT.READ_ONLY);
		cbDomain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbDomain.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				String domain = cbDomain.getText();
				if (!domain.isEmpty()) {
					LinkedHashMap<String, String> moduleDataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf4_domain", objectType + "Revision", new String[] { domain }, session);
					StringExtension.UpdateValueTextCombobox(cbModule, moduleDataForm);
				}
			}
		});

		Label lblModule = new Label(this, SWT.NONE);
		lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModule.setText("Module: (*)");
		lblModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbModule = new Combo(this, SWT.READ_ONLY);
		cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblWorkProduct = new Label(this, SWT.NONE);
		lblWorkProduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblWorkProduct.setText("Work Product:");
		lblWorkProduct.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		cbWorkProduct = new Combo(this, SWT.READ_ONLY);
		cbWorkProduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTargetReleaseDate = new Label(this, SWT.NONE);
		lblTargetReleaseDate.setText("Target Release Date: (*)");
		lblTargetReleaseDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		datTargetReleaseDate = new DateTime(this, SWT.BORDER);
		datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		ckbTargetReviseDate = new Button(this, SWT.CHECK);
		ckbTargetReviseDate.setText("Target Revise Date");
		ckbTargetReviseDate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updateTargetReviseDateUI();
			}
		});

		datTargetReviseDate = new DateTime(this, SWT.BORDER);
		datTargetReviseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
	}

	@Override
	public void initData(String prefix) {
		prefixName = prefix;
		objectType = objectTypeMapping.getOrDefault(prefixName, "VF3_spec_doc");

		String[] domainDataForm = TCExtension.GetLovValues("vf4_domain", objectType + "Revision", session);
		String[] workProductDataForm = TCExtension.GetPreferenceValues("VINF_VFFS_WORK_PRODUCT", session);

		StringExtension.UpdateValueTextCombobox(cbModuleName, moduleNameDataForm);
		StringExtension.UpdateValueTextCombobox(cbModelCode, modelCodeDataForm);
		cbDomain.setItems(domainDataForm);
		cbWorkProduct.setItems(workProductDataForm);

		datTargetReleaseDate.setDate(targetReleaseDate.get(Calendar.YEAR), targetReleaseDate.get(Calendar.MONTH), targetReleaseDate.get(Calendar.DAY_OF_MONTH));
		updateTargetReviseDateUI();
	}

	@Override
	public boolean checkRequired() {
		if (txtID.getText().isEmpty())
			return false;

		if (txtName.getText().isEmpty())
			return false;

		if (cbDomain.getText().isEmpty())
			return false;

		if (cbModule.getText().isEmpty())
			return false;

		return true;
	}

	@Override
	public String createNewItem(TCComponent selectedObject, boolean isOpenOnCreate) {
		String message = "";
		DataManagementService dms = DataManagementService.getService(session);
		String objectType = objectTypeMapping.getOrDefault(prefixName, "VF3_spec_doc");
		String id = txtID.getText();
		String name = txtName.getText();
		String description = txtDescription.getText();
		String vehicleCategory = "AUTOMOBILE";

		String modelCode = (String) cbModelCode.getData(cbModelCode.getText());
		String moduleName = (String) cbModuleName.getData(cbModuleName.getText());
		String domain = cbDomain.getText();
		String module = cbModule.getText();
		String workProduct = cbWorkProduct.getText();
		Calendar targetReleaseDate = StringExtension.getDatetimeFromWidget(datTargetReleaseDate);
		Calendar targetReviseDate = StringExtension.getDatetimeFromWidget(datTargetReviseDate);
		try {
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf3_veh_category", vehicleCategory);
			itemDef.data.stringProps.put("vf3_doc_type", prefixName);
			itemDef.data.stringProps.put("vf3_model_code", modelCode);
			itemDef.data.stringProps.put("vf3_module_name", moduleName);

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf4_domain", domain);
			revDef.stringProps.put("vf4_module", module);
			revDef.stringProps.put("vf4_work_product", workProduct);
			revDef.dateProps.put("vf4_target_release_date", targetReleaseDate);
			if (ckbTargetReviseDate.getSelection())
				revDef.dateProps.put("vf4_target_revise_date", targetReviseDate);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

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

	private void resetDialog() {
		txtID.setText("");
		txtName.setText("");
	}
}
