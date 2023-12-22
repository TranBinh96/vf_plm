package com.teamcenter.vinfast.car.engineering.update;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventResponse;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class OriginalPartNumberUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private OriginalPartNumberUpdate_Dialog dlg;
	private LinkedList<TableEditor> bomEditors;
	private String[] moduleGroupDataForm = null;

	public OriginalPartNumberUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();

		if (!validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one VF Design, VF BP Design, Bomline.", "Information", MessageBox.WARNING);
			return null;
		}

		if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
			MessageBox.post("You do not have \"WRITE\" access on item.", "Access Denied", MessageBox.WARNING);
			return null;
		}

		try {
			if (!selectedObject.getPropertyDisplayableValue("vf4_orginal_part_number").isEmpty()) {
				MessageBox.post("Original Part Number not empty. Please contact with administrator if you want to update.", "Information", MessageBox.WARNING);
				return null;
			}

//			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
//				List<String> statusList = itemObject.getLatestItemRevision().getPropertyDisplayableValues("release_status_list");
//				if (statusList != null && statusList.size() > 0) {
//					for (String status : statusList) {
//						if (releaseStatus.contains(status)) {
//							MessageBox.post("Part already released. Please contact with administrator if you want to update.", "Information", MessageBox.WARNING);
//							return null;
//						}
//					}
//				}
//			}
			moduleGroupDataForm = TCExtension.GetLovValues("VL5_module_group", "BOMLine", session);
			bomEditors = new LinkedList<TableEditor>();
			dlg = new OriginalPartNumberUpdate_Dialog(new Shell());
			dlg.create();

			dlg.txtPartNumber.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						searchPart();
					}
				}
			});

			dlg.btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchPart();
				}
			});

			dlg.btnAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					addPart();
				}
			});

			dlg.btnRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					removePart();
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateOriginalPart();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void searchPart() {
		dlg.tblSearchResult.removeAll();
		String partNumberSearch = dlg.txtPartNumber.getText();
		if (partNumberSearch.isEmpty())
			partNumberSearch = "*";

		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Item ID", partNumberSearch);
		inputQuery.put("Type", "VF4_*Design;VF3_Scooter_part");
		TCComponent[] objectQuery = Query.queryItem(session, inputQuery, "Item...");
		if (objectQuery != null) {
			for (TCComponent object : objectQuery) {
				try {
					String[] propValues = object.getProperties(new String[] { "object_string" });
					TableItem row = new TableItem(dlg.tblSearchResult, SWT.NONE);
					row.setText(propValues);
					row.setData(object);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		dlg.tblSearchResult.redraw();
	}

	private void addPart() {
		LinkedList<TCComponentItem> selectedObjectList = new LinkedList<TCComponentItem>();
		for (TableItem tableItem : dlg.tblOriginalPart.getItems()) {
			selectedObjectList.add((TCComponentItem) tableItem.getData());
		}

		for (TableItem tableItemSelect : dlg.tblSearchResult.getSelection()) {
			if (tableItemSelect.getData() == selectedObject)
				continue;

			if (!selectedObjectList.contains(tableItemSelect.getData())) {
				updateTableItem((TCComponentItem) tableItemSelect.getData());
			}
		}
	}

	private void removePart() {
		LinkedList<TableEditor> bomEditRemoveList = new LinkedList<TableEditor>();
		for (Integer selectIndex : dlg.tblOriginalPart.getSelectionIndices()) {
			bomEditRemoveList.add(bomEditors.get(selectIndex));
		}

		for (TableEditor tableEditor : bomEditRemoveList) {
			bomEditors.remove(tableEditor);
			tableEditor.getEditor().dispose();
		}

		dlg.tblOriginalPart.remove(dlg.tblOriginalPart.getSelectionIndices());
		dlg.tblOriginalPart.redraw();
	}

	private void updateOriginalPart() {
		if (dlg.tblOriginalPart.getItemCount() == 0) {
			dlg.setMessage("Please select original part.", IMessageProvider.WARNING);
			return;
		}

		if (!validateTableData()) {
			dlg.setMessage("Please input module group.", IMessageProvider.WARNING);
			return;
		}

		Set<String> partList = new HashSet<String>();
		Set<String> moduleGroupList = new HashSet<String>();

		int counter = 0;
		for (TableItem item : dlg.tblOriginalPart.getItems()) {
			partList.add(item.getText(0));
			Combo cbModuleGroup = (Combo) bomEditors.get(counter).getEditor();
			moduleGroupList.add(cbModuleGroup.getText());
			counter++;
		}

		try {
			PostEventObjectProperties[] postEvtInputs = new PostEventObjectProperties[1];
			postEvtInputs[0] = new PostEventObjectProperties();
			postEvtInputs[0].primaryObject = selectedObject.getRelatedComponents("revision_list")[0];
			PostEventResponse response = DataManagementService.getService(session).postEvent(postEvtInputs, "Fnd0MultiSite_Unpublish");

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage(TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			selectedObject.setProperty("vf3_old_part_number", "True");
			selectedObject.setProperty("vf4_hw_version", "true");
			selectedObject.setProperty("vf4_orginal_part_number", String.join(";", partList));
			selectedObject.setProperty("vf4_vf_code", String.join(";", moduleGroupList));
			selectedObject.setProperty("vf4_hw_version", "");
			selectedObject.refresh();
			String currentOriginalPN = selectedObject.getProperty("vf4_orginal_part_number");

			if (currentOriginalPN.equals(String.join(";", partList))) {
				dlg.setMessage("Update successfully.", IMessageProvider.INFORMATION);
			} else {
				dlg.setMessage("Update NOT successfully.", IMessageProvider.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean validateTableData() {
		for (int i = 0; i < dlg.tblOriginalPart.getItemCount(); i++) {
			Combo cbModuleGroup = (Combo) bomEditors.get(i).getEditor();
			if (cbModuleGroup.getText().isEmpty())
				return false;
		}

		return true;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		try {
			if (targetComponents == null)
				return true;

			if (targetComponents.length > 1)
				return true;

			if (targetComponents[0] instanceof TCComponentBOMLine) {
				selectedObject = ((TCComponentBOMLine) targetComponents[0]).getItem();
			} else if (targetComponents[0] instanceof TCComponentItem) {
				selectedObject = (TCComponentItem) targetComponents[0];
			}

			if (selectedObject == null)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void updateTableItem(TCComponentItem object) {
		String partID = "";
		try {
			partID = object.getPropertyDisplayableValue("item_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		final TableItem item = new TableItem(dlg.tblOriginalPart, SWT.NONE);
		item.setText(new String[] { partID, "" });
		item.setData(object);

		TableEditor bomEditor = new TableEditor(dlg.tblOriginalPart);
		Combo cbModuleGroup = new Combo(dlg.tblOriginalPart, SWT.READ_ONLY);
		cbModuleGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbModuleGroup.computeSize(SWT.DEFAULT, dlg.tblOriginalPart.getItemHeight());
		bomEditor.grabHorizontal = true;
		bomEditor.minimumHeight = cbModuleGroup.getSize().y;
		bomEditor.minimumWidth = cbModuleGroup.getSize().x;
		cbModuleGroup.setItems(moduleGroupDataForm);
		bomEditor.setEditor(cbModuleGroup, item, 1);

		bomEditors.add(bomEditor);
	}
}
