package com.teamcenter.vinfast.aftersale.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SCRCreate_Handler extends AbstractHandler {
	private TCSession session;
	private SCRCreate_Dialog dlg;
	private TCComponent selectedObject;
	private static String OBJECT_TYPE = "VF4_SCR";

	public SCRCreate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			// init data
			LinkedHashMap<String, String> vehicleProgramDataForm = TCExtension.GetLovValueAndDisplay("vf4_veh_program", OBJECT_TYPE + "Revision", session);
			dlg = new SCRCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleProgram, vehicleProgramDataForm);
			dlg.cbVehicleProgram.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String program = dlg.cbVehicleProgram.getText();
					dlg.txtPrefixName.setText("[" + mappingNewProgram(program) + "]");
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					createNewObject();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createNewObject() {
		try {
			if (!createValidate()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dmService = DataManagementService.getService(session);

			String vehicleProgram = (String) dlg.cbVehicleProgram.getData(dlg.cbVehicleProgram.getText());
			String name = dlg.txtPrefixName.getText() + dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			boolean afsPartUpdateRequired = dlg.ckbAFSPartUpdateRequired.getSelection();
			boolean afsRelevantUpdateRequired = dlg.ckbAFSRelevantUpdateRequired.getSelection();
			boolean dcrUpdateRequired = dlg.ckbDCRUpdateRequired.getSelection();
			boolean epcUpdateRequired = dlg.ckbECPUpdateRequired.getSelection();
			boolean marketPurchaseRequired = dlg.ckbMarketSpecificPurchaseRequired.getSelection();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf4_veh_program", vehicleProgram);
			revDef.boolProps.put("vf4_is_afs_part_update", afsPartUpdateRequired);
			revDef.boolProps.put("vf4_is_afs_relevant_update", afsRelevantUpdateRequired);
			revDef.boolProps.put("vf4_is_dcr_update", dcrUpdateRequired);
			revDef.boolProps.put("vf4_is_epc_update", epcUpdateRequired);
			revDef.boolProps.put("vf4_is_market_specific", marketPurchaseRequired);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "VF4_SCN_GenID";
			idGen.stringProps.put("vf4_vehicle_programs", vehicleProgram);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });
			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage("Create unsuccessfully. Exception: " + TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

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
				resetDialog();
			} else {
				dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean createValidate() {
		if (dlg.cbVehicleProgram.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDescription.getText().isEmpty())
			return false;

		return true;
	}

	private void resetDialog() {
		dlg.cbVehicleProgram.deselectAll();
		dlg.txtName.setText("");
		dlg.txtPrefixName.setText("");
		dlg.txtDescription.setText("");
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String mappingNewProgram(String program) {
		switch (program) {
		case "VFe32":
			return "VF5";
		case "VFe33":
			return "VF6";
		case "VFe34S":
			return "VF7";
		case "VFe35":
			return "VF8";
		case "VFe36":
			return "VF9";
		default:
			return program;
		}
	}
}
