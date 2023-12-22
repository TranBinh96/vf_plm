package com.teamcenter.vinfast.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventResponse;
import com.vf.dialog.NewPartSearchOldPartDialog;

public class MBOMOldPartSWTDialog extends AbstractSWTDialog {
	protected TCSession session;
	protected Object result;
	private Table table;
	private Button btnAdd;
	private Button btnRemove;
	private Button btnSave;
	boolean isAccess = false;
	boolean isReadOnly = false;
	boolean isFilled = false;
	private TCComponent item = null;
	private List<OldPartNumberPostAction> postActions;

	public MBOMOldPartSWTDialog(Shell parent, int style, List<OldPartNumberPostAction> postActions) {
		super(parent, style);
		setBlockOnOpen(false);
		this.postActions = postActions;
	}

	protected Control createDialogArea(Composite parent) {
		List<String> idList = new ArrayList<String>();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_shlUpdatePartNumber = new GridLayout(3, false);
		gl_shlUpdatePartNumber.marginHeight = 10;
		gl_shlUpdatePartNumber.marginLeft = 10;
		gl_shlUpdatePartNumber.marginBottom = 10;
		container.setLayout(gl_shlUpdatePartNumber);

		table = new Table(container, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setEnabled(true);

		TableColumn column = new TableColumn(table, SWT.CENTER);
		column.setResizable(true);
		column.setWidth(150);
		column.setText("Part Number(s)");

		TableColumn moduleCol = new TableColumn(table, SWT.CENTER);
		moduleCol.setResizable(true);
		moduleCol.setWidth(250);
		moduleCol.setText("Module Group");

		btnAdd = new Button(container, SWT.PUSH);
		btnAdd.setImage(new Image(null, MBOMOldPartSWTDialog.class.getClassLoader().getResourceAsStream("icons/edit_16.png")));
		btnAdd.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		btnAdd.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		new Label(container, SWT.NONE);

		btnAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							NewPartSearchOldPartDialog partSearch = new NewPartSearchOldPartDialog();
							partSearch.createAndShowGUI(session, table, idList, "VF4_*Design;VF3_Scooter_part");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		btnRemove = new Button(container, SWT.PUSH);
		btnRemove.setImage(new Image(null, MBOMOldPartSWTDialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
		btnRemove.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		new Label(container, SWT.NONE);

		btnRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				table.remove(table.getSelectionIndices());
			}
		});

		String[] partValues = getOldPartNumbers();

		if (partValues != null) {
			for (String part : partValues) {
				new TableItem(table, SWT.NONE).setText(part);
			}
			isFilled = true;
			table.setEnabled(false);
			btnAdd.setEnabled(false);
			btnRemove.setEnabled(false);
		}

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update Original Part Number...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 330);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		super.createButtonsForButtonBar(parent);
		btnSave = getButton(IDialogConstants.OK_ID);
		btnSave.setImage(new Image(null, MBOMOldPartSWTDialog.class.getClassLoader().getResourceAsStream("icons/save_16.png")));
		btnSave.setText("Save");
		if (isFilled == true || isAccess == true || isReadOnly == true) {
			btnSave.setEnabled(false);
		}

	}

	@Override
	protected void okPressed() {
		String saveValue = "";
		String saveValueModule = "";
		System.out.println("Remove Clicked");
		List<TCComponent> oldItems = new LinkedList<TCComponent>();
		Control[] ctrl = table.getChildren();

		int counter = 0;
		for (TableItem item : table.getItems()) {
			if (saveValue.length() == 0) {
				saveValue = item.getText();
			} else {
				saveValue = saveValue + ";" + item.getText();
			}
			oldItems.add((TCComponent) item.getData());

			CCombo moduleGrp = (CCombo) ctrl[counter];

			if (moduleGrp.getText().isEmpty()) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setMessage("Please input Module Group of part: " + saveValue);
				mb.open();
				return;
			}

			if (saveValueModule.length() == 0) {
				saveValueModule = moduleGrp.getText();
			} else {
				saveValueModule = saveValueModule + ";" + moduleGrp.getText();
			}
			counter++;
		}

		StringBuffer allErrorMsgs = new StringBuffer();
		String currentOriginalPN = null;
		try {
			// TODO create event REV_COPY_OLD_PART_INFO
			PostEventObjectProperties[] postEvtInputs = new PostEventObjectProperties[1];
			postEvtInputs[0] = new PostEventObjectProperties();
			postEvtInputs[0].primaryObject = item.getRelatedComponents("revision_list")[0];
			PostEventResponse response = DataManagementService.getService(session).postEvent(postEvtInputs, "Fnd0MultiSite_Unpublish");
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setMessage("Exception: Update old part number not success");
				mb.open();
			} else {
				item.setProperty("vf3_old_part_number", "True");
				item.setProperty("vf4_hw_version", "true");
				item.setProperty("vf4_orginal_part_number", saveValue);
				item.setProperty("vf4_vf_code", saveValueModule);
				item.setProperty("vf4_hw_version", "");
				item.refresh();
				currentOriginalPN = item.getProperty("vf4_orginal_part_number");

				boolean updateOriginalPNValueSuccessfully = currentOriginalPN.equals(saveValue);

				MessageBox mb = new MessageBox(getShell(), allErrorMsgs.length() <= 2 ? SWT.ICON_INFORMATION : SWT.ICON_WARNING);
				mb.setText(updateOriginalPNValueSuccessfully ? (allErrorMsgs.length() <= 2 ? "Info" : "Warning") : "Error");
				String msg = updateOriginalPNValueSuccessfully ? (allErrorMsgs.length() <= 2 ? "Updated \"Original Base Part Number\" successfully." : "Updated \"Original Base Part Number\" successfully with warning(s) below.\n") : "Updated \"Original Base Part Number\" NOT successfully.";

				msg += allErrorMsgs.toString();
				mb.setMessage(msg);
				mb.open();
			}
		} catch (Exception e) {
			allErrorMsgs.append(e.getMessage());
			allErrorMsgs.append("\n");
		}

		super.okPressed();
	}

	public String[] getOldPartNumbers() {
		String[] values = null;
		InterfaceAIFComponent object = AIFUtility.getCurrentApplication().getTargetComponent();
		TCAccessControlService acl = session.getTCAccessControlService();
		try {
			TCComponentGroup loginGroup = session.getCurrentGroup();
			Class cls = object.getClass();
			String name = cls.getSimpleName();
			if (name.equals("TCComponentBOMLine")) {
				TCComponentBOMLine bomLine = (TCComponentBOMLine) object;
				TCComponentItemRevision rev = bomLine.getItemRevision();
				TCComponent[] list = rev.getReferenceListProperty("release_status_list");
				if (list.length != 0 && !loginGroup.getGroupName().contentEquals("dba")) {
					isAccess = true;
				}
				item = bomLine.getItem();
			} else {
				TCComponentItemRevision rev = (TCComponentItemRevision) object;
				TCComponent[] list = rev.getReferenceListProperty("release_status_list");
				if (list.length != 0 && !loginGroup.getGroupName().contentEquals("dba")) {
					isAccess = true;
				}
				item = rev.getItem();
			}

			if (acl.checkPrivilege(item, "WRITE") == false) {
				isReadOnly = true;
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				mb.setText("Access Denied...");
				mb.setMessage("You do not have \"Write\" access on item. Do you want to view?");
				int rlt = mb.open();
				if (rlt == SWT.CANCEL) {
					return null;
				} else {
					table.setEnabled(false);
					btnAdd.setEnabled(false);
					btnRemove.setEnabled(false);
				}
			}
			String partNumbers = item.getProperty("vf4_orginal_part_number");
			if (partNumbers.length() != 0) {
				if (partNumbers.contains(";")) {
					values = partNumbers.split(";");
				} else {
					values = new String[] { partNumbers };
				}
			}

			if (isAccess) {
				table.setEnabled(false);
				btnAdd.setEnabled(false);
				btnRemove.setEnabled(false);
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_INFORMATION);
				mb.setText("Access Denied...");
				mb.setMessage("Part already released. Read access is permitted");
				mb.open();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return values;
	}

	public interface OldPartNumberPostAction {
		public void run(List<TCComponent> oldItems, TCComponent newItem) throws Exception;

		public String getName();
	}
}