package com.teamcenter.vinfast.admin;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

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
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ServiceData;
import com.vf.utils.TCExtension;

public class DummyPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private DummyPartCreate_Dialog dlg;

	private static String[] GROUP_PERMISSION = { "dba" };

	private LinkedList<LinkedHashMap<String, String>> partList = null;

	public DummyPartCreate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
			MessageBox.post("You are not authorized.", "Please change to group: " + String.join(", ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}

		dlg = new DummyPartCreate_Dialog(new Shell());
		dlg.create();

		// init data
		partList = new LinkedList<LinkedHashMap<String, String>>();
		// init UI
		dlg.btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				prefData();
				createNewItem();
			}
		});

		dlg.open();
		return null;
	}

	private void prefData() {
		String attributeList = dlg.txtProperty.getText();
		String newValue = dlg.txtValue.getText();
		String[] newValues = null;
		if (newValue.contains("\r\n")) {
			newValues = newValue.split("\r\n");
		} else {
			newValues = new String[] { newValue };
		}

		if (attributeList.contains(";")) {
			String[] attributes = attributeList.split(";");

			for (String newVal : newValues) {
				if (newVal.contains(";")) {
					LinkedHashMap<String, String> newItem = new LinkedHashMap<String, String>();
					String[] val = newVal.split(";");
					int i = 0;
					for (String attr : attributes) {
						if (val.length >= i) {
							newItem.put(attr, val[i]);
						} else {
							newItem.put(attr, "");
						}
						i++;
					}
					partList.add(newItem);
				}
			}
		}
	}

	private void createNewItem() {
		try {
			if (!validationCreate()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dms = DataManagementService.getService(session);
			for (LinkedHashMap<String, String> part : partList) {
				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = dlg.txtItemType.getText();

				for (Map.Entry<String, String> property : part.entrySet()) {
					itemDef.data.stringProps.put(property.getKey(), property.getValue());
				}
//				itemDef.data.stringProps.put("item_id", id);
//				itemDef.data.stringProps.put("object_name", name);
//				itemDef.data.stringProps.put("object_desc", description);
//				itemDef.data.stringProps.put("vf4_donor_vehicle", description);
//				itemDef.data.stringProps.put("vf4_part_category", partCategory);
//				itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);

				CreateInput revDef = new CreateInput();
				revDef.boName = dlg.txtRevType.getText();

				itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });
				CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

				if (response.serviceData.sizeOfPartialErrors() == 0) {
					TCComponentItemRevision cfgContext = null;
					for (TCComponent rev : response.output[0].objects) {
						if (rev.getType().equals(dlg.txtRevType.getText())) {
							cfgContext = (TCComponentItemRevision) rev;
						}
					}
					if (cfgContext != null) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext.getItem());
							dlg.setMessage("Created successfully.", IMessageProvider.INFORMATION);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					} else {
						dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
					}
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

	private boolean validationCreate() {
		if (dlg.txtItemType.getText().isEmpty())
			return false;

		if (dlg.txtRevType.getText().isEmpty())
			return false;

		return true;
	}
}
