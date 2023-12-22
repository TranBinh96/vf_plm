package com.teamcenter.vinfast.change;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf4.services.rac.cm.ChangeManagementService;
import com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeInput;
import com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeResponse;
import com.vf4.services.rac.cm._2020_12.ChangeManagement.PropertyAndValues;

public class ECRDeriveChange_Handler extends AbstractHandler {
	private TCSession session;
	private ECRDeriveChange_Dialog dlg;

	private TCComponent selectedObject;
	private boolean firstRun = true;
	private static String[] GROUP_PERMISSION = { "dba" };

	public ECRDeriveChange_Handler() {

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			firstRun = true;
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + String.join(";", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}
			selectedObject = (TCComponent) targetComp[0];
			if (!validationECR()) {
				MessageBox.post("Selected item is not Engineering Change Request Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!validationExistECN()) {
				MessageBox.post("ECN exist in selected item.", "Error", MessageBox.ERROR);
				return null;
			}

			if (checkIDExist(getID())) {
				MessageBox.post("ECN: " + getID() + " already exist in system. Please contact administrator to get support", "Error", MessageBox.ERROR);
				return null;
			}

			// Load init Data
			LinkedHashMap<String, String> vehicleGroupDataForm = TCExtension.GetLovValueAndDisplay("vf6_vehicle_group", "Vf6_ECN", session);

			// Init UI
			dlg = new ECRDeriveChange_Dialog(new Shell());
			dlg.create();

			dlg.setTitle("Change Item Create Information");
			dlg.setMessage("Define change item create information");

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleGroup, vehicleGroupDataForm);
			dlg.cbVehicleGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateCombobox();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			updateDefaultUI();

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			} else {
				String id = dlg.txtID.getText();
				String name = dlg.txtName.getText();
				String description = dlg.txtDescription.getText();
				String vehicleGroup = (String) dlg.cbVehicleGroup.getData(dlg.cbVehicleGroup.getText());
				String moduleGroup = (String) dlg.cbModuleGroup.getData(dlg.cbModuleGroup.getText());
				String initiator = selectedObject.getPropertyDisplayableValue("vf6_initiator");

				ChangeManagementService cms = ChangeManagementService.getService(session);
				DeriveChangeInput ins = new DeriveChangeInput();
				ins = new DeriveChangeInput();
				ins.changeObjRev = selectedObject;
				ins.derivedObjType = "Vf6_ECN";

				PropertyAndValues proList[] = new PropertyAndValues[6];
				proList[0] = new PropertyAndValues();
				proList[0].propertyName = "item_id";
				proList[0].propertyValue = id;

				proList[1] = new PropertyAndValues();
				proList[1].propertyName = "object_name";
				proList[1].propertyValue = name;

				proList[2] = new PropertyAndValues();
				proList[2].propertyName = "object_desc";
				proList[2].propertyValue = description;

				proList[3] = new PropertyAndValues();
				proList[3].propertyName = "vf6_vehicle_group";
				proList[3].propertyValue = vehicleGroup;

				proList[4] = new PropertyAndValues();
				proList[4].propertyName = "vf6_module_group";
				proList[4].propertyValue = moduleGroup;

				proList[5] = new PropertyAndValues();
				proList[5].propertyName = "vf6_initiator";
				proList[5].propertyValue = initiator;

				ins.propertyAndValues = proList;
				ins.relationToAttachChangeObj = "CMImplements";

				DeriveChangeResponse response = cms.deriveChange(new DeriveChangeInput[] { ins });
				if (response.serviceData.sizeOfCreatedObjects() == 0) {
					dlg.setMessage("Created successfully", IMessageProvider.INFORMATION);
					dlg.btnCreate.setEnabled(false);
				} else {
					ServiceData serviceData = response.serviceData;
					for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
						for (String msg : serviceData.getPartialError(i).getMessages()) {
							MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validationECR() {
		try {
			if (selectedObject != null) {
				String type = selectedObject.getProperty("object_type");
				if (type.equals("Engineering Change Request Revision")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private Boolean validationExistECN() {
		try {
			if (selectedObject != null) {
				TCComponent[] objectChildComponents = selectedObject.getRelatedComponents("CMImplementedBy");
				for (TCComponent tcComponent : objectChildComponents) {
					if (tcComponent.getType().compareToIgnoreCase("Vf6_ECNRevision") == 0) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void updateDefaultUI() {
		try {
			dlg.txtECR.setText(getECR());
			dlg.txtID.setText(getID());
			dlg.txtName.setText(selectedObject.getPropertyDisplayableValue("object_name"));
			dlg.txtDescription.setText(selectedObject.getPropertyDisplayableValue("object_desc"));
			dlg.cbVehicleGroup.setText(selectedObject.getPropertyDisplayableValue("vf6_vehicle_group"));
//			dlg.cbModuleGroup.setText(selectedObject.getPropertyDisplayableValue("vf6_module_group"));
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private String getID() throws NotLoadedException {
		String ecrID = selectedObject.getPropertyDisplayableValue("item_id");
		return "ECN" + ecrID.substring(3, ecrID.length());
	}

	private String getECR() throws NotLoadedException {
		String ecrID = selectedObject.getPropertyDisplayableValue("item_id");
		String ecrRevision = selectedObject.getPropertyDisplayableValue("item_revision_id");
		String ecrName = selectedObject.getPropertyDisplayableValue("object_name");
		return ecrID + "/" + ecrRevision + " - " + ecrName;
	}

	private void updateCombobox() {
		resetDialog();
		String vehGroup = "";

		try {
			vehGroup = (String) dlg.cbVehicleGroup.getData(dlg.cbVehicleGroup.getText());
			String[] parentPrefix = { vehGroup };
			LinkedHashMap<String, String> moduleGroupDataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf6_vehicle_group", "Vf6_ECN", parentPrefix, session);
			StringExtension.UpdateValueTextCombobox(dlg.cbModuleGroup, moduleGroupDataForm);
			if (firstRun) {
				dlg.cbModuleGroup.setText(selectedObject.getPropertyDisplayableValue("vf6_module_group_comp"));
				firstRun = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.cbModuleGroup.removeAll();
	}

	private Boolean checkRequired() {
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDescription.getText().isEmpty())
			return false;
		return true;
	}

	private Boolean checkIDExist(String newIDValue) {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Item ID", newIDValue);
		parameter.put("Type", "Engineering Change Notice");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Item...");// getItemList(session, newIDValue);
		if (item_list == null || item_list.length == 0) {
			return false;
		}
		return true;
	}
}
