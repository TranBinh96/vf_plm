package com.teamcenter.vinfast.suppliercollaboration;

import java.util.Calendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DDUCreate_Dialog extends Dialog{
	public Button btnCreate;
	public Text txtDescription;
	public Text txtRemark;
	public Combo cbVendor;
	public Combo cbFormat;
	public DateTime dtDueDate;
	
	public DDUCreate_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();
		
		GridLayout gl_shell = new GridLayout(2, false);
		container.setLayout(gl_shell);

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("To Vendor: (*)");
		
		cbVendor = new Combo(container, SWT.NONE | SWT.READ_ONLY);
		cbVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbVendor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				validateAccept();
			}
		});
		
		Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText("Description:");
		
		txtDescription = new Text(container, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblDueDate = new Label(container, SWT.NONE);
		lblDueDate.setText("Due Date: (*)");
		
		dtDueDate = new DateTime(container, SWT.BORDER);
		dtDueDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblRemark = new Label(container, SWT.NONE);
		lblRemark.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblRemark.setText("Remark:");
		
		txtRemark = new Text(container, SWT.BORDER);
		txtRemark.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblExpectedFormat = new Label(container, SWT.NONE);
		lblExpectedFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblExpectedFormat.setText("Expected format: (*)");
		
		cbFormat = new Combo(container, SWT.NONE | SWT.READ_ONLY);
		cbFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbFormat.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				validateAccept();
			}
		});
		
		return container;
	}
	
	@Override
	   protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.OK_ID, "Accept", false);
	    btnCreate.setEnabled(false);
	 }

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("DDU Create");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 250);
	}
	
	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}
	
	@Override
	protected void okPressed() {
	}
	
	private void validateAccept() {
		boolean check = true;
		if(cbVendor.getText().isEmpty()) check = false;
		if(cbFormat.getText().isEmpty()) check = false;
		btnCreate.setEnabled(check);	
	}
}
