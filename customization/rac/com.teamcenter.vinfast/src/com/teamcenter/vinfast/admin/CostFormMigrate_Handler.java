package com.teamcenter.vinfast.admin;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class CostFormMigrate_Handler extends AbstractHandler {
	private TCSession session;
	private CostFormMigrate_Dialog dlg;

	private LinkedHashMap<TCComponentForm, Boolean> selectedObjects;
	private Set<String> partIDList;
	private static String[] GROUP_PERMISSION = { "dba" };

	public CostFormMigrate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized to migrate.", "Please change to group: " + String.join(", ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			selectedObjects = new LinkedHashMap<>();
			partIDList = new HashSet<>();

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			for (InterfaceAIFComponent interfaceAIFComponent : targetComp) {
				if (interfaceAIFComponent instanceof TCComponentForm) {
					TCComponentForm form = (TCComponentForm) interfaceAIFComponent;
					selectedObjects.put(form, false);
					String id = form.getPropertyDisplayableValue("object_name");
					if (id.contains("_")) {
						String[] split = id.split("_");
						partIDList.add(split[0]);
					}
				}
			}

			dlg = new CostFormMigrate_Dialog(new Shell());
			dlg.create();

			// Init Data

			// Init UI
			dlg.txtObjectType.setText("VF Design;");
			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					migrate();
				}
			});

			updateTable(false);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void migrate() {
		String objectType = dlg.txtObjectType.getText();
		if (objectType.isEmpty()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		LinkedHashMap<String, String> queryList = new LinkedHashMap<>();
		queryList.put("Item ID", String.join(";", partIDList));
		queryList.put("Type", objectType);
		TCComponent[] queryItems = TCExtension.queryItem(session, queryList, "Item...");
		if (queryItems == null || queryItems.length == 0) {
			dlg.setMessage("Part not exist.", IMessageProvider.ERROR);
			return;
		}

		for (Map.Entry<TCComponentForm, Boolean> entry : selectedObjects.entrySet()) {
			Boolean check = false;
			try {
				TCComponentForm formItem = entry.getKey();
				String formID = "";
				String id = formItem.getPropertyDisplayableValue("object_name");
				if (id.contains("_")) {
					String[] split = id.split("_");
					formID = split[0];
				}

				for (TCComponent queryItem : queryItems) {
					String partNo = queryItem.getPropertyDisplayableValue("item_id");
					if (partNo.compareTo(formID) == 0) {
						TCComponentGroup itemOwningGroup = (TCComponentGroup) queryItem.getReferenceProperty("owning_group");
						TCComponentUser itemOwningUser = (TCComponentUser) queryItem.getReferenceProperty("owning_user");

						check = changeOwner(formItem, itemOwningGroup, itemOwningUser);
						if (check) {
							List<TCComponent> vfCostRevision = TCExtension.getReferenced(formItem, "VF4_SourcingCostFormRela", session, "VF4_CostRevision");
							if (vfCostRevision != null && vfCostRevision.get(0) instanceof TCComponentItemRevision) {
								changeOwner(vfCostRevision.get(0), itemOwningGroup, itemOwningUser);
								TCComponent[] costForms = vfCostRevision.get(0).getRelatedComponents("VF4_SourcingCostFormRela");
								if (costForms != null && costForms.length > 0) {
									for (TCComponent costForm : costForms) {
										if (costForm.getType().compareTo("VF4_TargetCostForm") == 0) {
											changeOwner(costForm, itemOwningGroup, itemOwningUser);
										}
									}
								}
								TCComponentItem vfCostItem = ((TCComponentItemRevision) vfCostRevision.get(0)).getItem();
								if (vfCostItem != null) {
									changeOwner(vfCostItem, itemOwningGroup, itemOwningUser);
								}
							}
						}

						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			entry.setValue(check);
		}
		updateTable(true);
		dlg.setMessage("Process done.", IMessageProvider.INFORMATION);
	}

	private boolean changeOwner(TCComponent item, TCComponentGroup partOwningGroup, TCComponentUser partOwningUser) throws TCException {
		TCComponentGroup itemOwningGroup = (TCComponentGroup) item.getReferenceProperty("owning_group");
		TCComponentUser itemOwningUser = (TCComponentUser) item.getReferenceProperty("owning_user");
		if (itemOwningGroup != partOwningGroup || itemOwningUser != partOwningUser) {
			try {
				item.changeOwner(partOwningUser, partOwningGroup);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	private void updateTable(boolean updateStatus) {
		StringBuilder copResult = new StringBuilder();
		copResult.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		copResult.append(StringExtension.htmlTableCss);
		copResult.append("<body style=\"margin: 0px;\">");

		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("Form", "80");
				put("Status", "20");
			}
		};

		try {
			copResult.append("<table>");
			copResult.append(StringExtension.genTableHeader(header));
			for (Map.Entry<TCComponentForm, Boolean> entry : selectedObjects.entrySet()) {
				String formName = "";
				try {
					formName = entry.getKey().getPropertyDisplayableValue("object_name");
				} catch (Exception e) {
					e.printStackTrace();
				}
				copResult.append("<tr>");
				copResult.append("<td>" + formName + "</td>");
				if (updateStatus)
					copResult.append("<td>" + (entry.getValue() ? StringExtension.genBadgetSuccess("Success") : StringExtension.genBadgetFail("Unsuccess")) + "</td>");
				else
					copResult.append("<td></td>");
				copResult.append("</tr>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		copResult.append("</table>");
		copResult.append("</body></html>");
		dlg.browser.setText(copResult.toString());
	}
}
