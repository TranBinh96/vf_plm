package com.teamcenter.vinfast.swbom.create;

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
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
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

public class VFECUCreate_Handler extends AbstractHandler {
	private TCSession session;
	private VFECUCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private String OBJECT_TYPE = "VF3_ECU";
	private boolean isAddBom = false;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		try {
			selectedObject = (TCComponent) targetComp[0];
			if (selectedObject instanceof TCComponentBOMLine)
				isAddBom = true;

			// Init data
			String[] ecuTypeDataForm = TCExtension.GetLovValues("vf4_ECU_type", OBJECT_TYPE, session);
			LinkedHashMap<String, String> programDataForm = TCExtension.GetLovValueAndDisplay("vf4_program", "VF3_FRSRevision", session);
			// Init UI
			dlg = new VFECUCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create VF ECU");
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, programDataForm);
			dlg.cbECUType.setItems(ecuTypeDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dms = DataManagementService.getService(session);

			String name = dlg.txtName.getText();
			String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
			String description = dlg.txtDescription.getText();
			String ecuType = dlg.cbECUType.getText();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_ECU_type", ecuType);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "VF4_FRS_Gen_ID";
			idGen.stringProps.put("vf4_program", program);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
				if (cfgContext != null) {
					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					if (isAddBom)
						addToBOMLine(itemRev);
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void addToBOMLine(TCComponentItemRevision newRev) {
		TCComponentBOMLine bomline = (TCComponentBOMLine) selectedObject;
		try {
			TCComponentBOMLine child = bomline.add(newRev.getItem(), newRev, null, false);
			bomline.add(child, true);
			child.setProperty("bl_quantity", "1");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private Boolean checkRequired() {
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.cbProgram.getText().isEmpty())
			return false;
		if (dlg.cbECUType.getText().isEmpty())
			return false;

		return true;
	}

	private void resetDialog() {
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbECUType.deselectAll();
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
