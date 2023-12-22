package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.Calendar;

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
import org.eclipse.swt.widgets.DateTime;

public class CommonDocumentCreate_Composite extends SpecDocumentCreateAbstract {
//	private Text txtID;
//	private Text txtName;
//	private Text txtDescription;
//	private Combo cbModelCode;
//	private Combo cbModuleName;
//	private DateTime datTargetReleaseDate;
//	private DateTime datTargetReviseDate;
//	private Button ckbTargetReviseDate;

	public CommonDocumentCreate_Composite(Composite c) {
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
		txtID.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		txtID.setEnabled(false);
		txtID.setEditable(false);

		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: (*)");
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		txtName = new Text(this, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblName_1 = new Label(this, SWT.NONE);
		lblName_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName_1.setText("Description:");
		lblName_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

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

		Label lblTargetReleaseDate = new Label(this, SWT.NONE);
		lblTargetReleaseDate.setText("Target Release Date: (*)");
		lblTargetReleaseDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		datTargetReleaseDate = new DateTime(this, SWT.BORDER);
		datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		ckbTargetReviseDate = new Button(this, SWT.CHECK);
		ckbTargetReviseDate.setText("TargetReviseDate");
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
		this.prefixName = prefix;
		objectType = objectTypeMapping.getOrDefault(prefixName, "VF3_spec_doc");

		StringExtension.UpdateValueTextCombobox(cbModuleName, moduleNameDataForm);
		StringExtension.UpdateValueTextCombobox(cbModelCode, modelCodeDataForm);

		datTargetReleaseDate.setDate(targetReleaseDate.get(Calendar.YEAR), targetReleaseDate.get(Calendar.MONTH), targetReleaseDate.get(Calendar.DAY_OF_MONTH));
		updateTargetReviseDateUI();
	}

	@Override
	public boolean checkRequired() {
		if (txtID.getText().isEmpty())
			return false;

		if (txtName.getText().isEmpty())
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
