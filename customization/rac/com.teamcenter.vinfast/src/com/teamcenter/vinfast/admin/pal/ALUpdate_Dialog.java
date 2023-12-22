package com.teamcenter.vinfast.admin.pal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Text;

public class ALUpdate_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private Button btnAccept;

	private TCComponentAssignmentList alItem = null;
	private Text txtName;
	private Text txtDesc;

	public ALUpdate_Dialog(Shell parentShell, TCComponentAssignmentList alItem) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.alItem = alItem;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		parent.setFocus();

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartNumber.setText("Name:");
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblTargetTask = new Label(container, SWT.NONE);
			lblTargetTask.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			lblTargetTask.setText("Description:");
			lblTargetTask.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

			txtDesc = new Text(container, SWT.BORDER | SWT.MULTI);
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesc.heightHint = 53;
			txtDesc.setLayoutData(gd_txtDesc);

			updateData();
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("User Update");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 210);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
		btnAccept.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to EDIT?");
				messageBox.setText("EDIT assignment list");
				int response = messageBox.open();
				if (response == SWT.YES) {
					editAL();
				}
			}
		});
	}

	public void updateData() {
		txtName.setText(alItem.getName());
		txtDesc.setText(alItem.getDescription());
	}

//	private String[] getDescArray(String paramString) {
//		String[] arrayOfString = null;
//		int i = paramString.length() / 240;
//		int j = paramString.length() % 240;
//		if (j > 0)
//			i++;
//		arrayOfString = new String[i];
//		boolean bool = false;
//		int k = 0;
//		if (i > 1) {
//			k = 240;
//		} else {
//			k = j;
//		}
//		for (byte b = 0; b < i; b++) {
//			arrayOfString[b] = paramString.substring(bool, k);
//			bool += true;
//			if (b == i - 2 && j > 0) {
//				k += j;
//			} else {
//				k += 240;
//			}
//		}
//		return arrayOfString;
//	}

	private void editAL() {
		if (txtName.getText().isEmpty()) {
			MessageBox.post("Please input all required information.", "Warning", MessageBox.WARNING);
			return;
		}

		try {
			String alName = txtName.getText();
			String[] alDescription = convertStringToArray(txtDesc.getText());

			TCComponentTaskTemplate taskTemplate = alItem.getProcessTemplate();
			ResourceMember[] currentResourcesAL = alItem.getDetails();
			alItem.modify(alName, alDescription, taskTemplate, currentResourcesAL, true);
			MessageBox.post("EDIT success.", "Success", MessageBox.INFORMATION);
			this.close();
		} catch (Exception e) {
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	private String[] convertStringToArray(String inputStr) {
		if (inputStr.contains("\r\n")) {
			String[] str = inputStr.split("\r\n");
			if (str.length > 1) {
				return str;
			}
		}

		return new String[] { inputStr };
	}
}
