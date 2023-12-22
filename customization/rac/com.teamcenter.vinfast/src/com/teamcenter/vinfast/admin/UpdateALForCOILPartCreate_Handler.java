package com.teamcenter.vinfast.admin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.administration.PreferenceManagementService;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.SetPreferences2In;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ServiceData;
import com.vf.utils.TCExtension;

public class UpdateALForCOILPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private UpdateALForCOILPartCreate_Dialog dlg;
	private static String[] GROUP_PERMISSION = { "dba" };
	
	public UpdateALForCOILPartCreate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
			MessageBox.post("You are not authorized to import AL preference.", "Please change to group: " + String.join(", ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}
		// init data
		String[] preferenceDataForm = { "VF_MEPART_COIL_MATERIALS", "VF_MEPART_COIL_COATINGS", "VF_COP_ASSIGNMENT_LIST" };

		// init UI
		dlg = new UpdateALForCOILPartCreate_Dialog(new Shell());
		dlg.create();
		dlg.cbWorkprocess.setItems(preferenceDataForm);
		dlg.cbWorkprocess.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				refreshPreference();
			}
		});

		dlg.btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updatePreference();
//				autoCreatePart();
			}
		});

		dlg.open();
		return null;
	}
	
	private void refreshPreference() {
		String preferenceName = dlg.cbWorkprocess.getText();
		if(!preferenceName.isEmpty()) {
			String[] preValue = TCExtension.GetPreferenceValues(preferenceName, session);
			dlg.lstValue.setItems(preValue);
		}
	}
	
	private void updatePreference() {
		if(!checkRequired())
			return;

		String newValue = dlg.txtNewValue.getText();
		String[] newValues = null;
		if(newValue.contains("\r\n")) {
			newValues = newValue.split("\r\n");
		}else {
			newValues = new String[] { newValue };
		}
		
		Set<String> preferenceValues = new HashSet<String>();
		String[] preferenceValue = dlg.lstValue.getItems();
		for (String value : preferenceValue) {
			preferenceValues.add(value);
		}
		for (String value : newValues) {
			if(!value.trim().isEmpty())
				preferenceValues.add(value);
		}
		
		if (preferenceValues.size() > 1) {
			SetPreferences2In prefeItem = new SetPreferences2In();
			prefeItem.preferenceName = dlg.cbWorkprocess.getText();
			prefeItem.values = preferenceValues.toArray(new String[0]);
			PreferenceManagementService prefService = PreferenceManagementService.getService(session);
			ServiceData response = prefService.setPreferences2(new SetPreferences2In[] { prefeItem });
			if (response.sizeOfPartialErrors() > 0) {
				for (int i = 0; i < response.sizeOfPartialErrors(); i++) {
					for (String msg : response.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			} else {
				dlg.setMessage("Preference update success", IMessageProvider.INFORMATION);
				dlg.txtNewValue.setText("");
				refreshPreference();
			}
		}
	}
	
	private boolean checkRequired() {
		if (dlg.txtNewValue.getText().isEmpty())
			return false;
		if (dlg.cbWorkprocess.getText().isEmpty())
			return false;

		return true;
	}
	
	private void autoCreatePart() {
		String newValue = dlg.txtNewValue.getText();
		String[] newValues = null;
		if(newValue.contains("\r\n")) {
			newValues = newValue.split("\r\n");
		} else {
			newValues = new String[] { newValue };
		}
		
		for (String value : newValues) {
			String[] str = value.split(";");
			createNewItem(str[1], str[0], "Make", "NONE", "VF33", "Y");
		}
	}
	
	private void createNewItem(String id, String name, String partMakeBuy, String partCategory, String donorVehicle, String partTraceability) {
		try {
			DataManagementService dms = DataManagementService.getService(session);
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "Design";
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.stringProps.put("vf4_part_category", partCategory);
			itemDef.data.stringProps.put("vf4_donor_vehicle", donorVehicle);
			itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);

			CreateInput revDef = new CreateInput();
			revDef.boName = "Design Revision";
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItemRevision cfgContext = null;
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals("Design Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
				try {
					session.getUser().getNewStuffFolder().add("contents", cfgContext.getItem());
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			} else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
}
