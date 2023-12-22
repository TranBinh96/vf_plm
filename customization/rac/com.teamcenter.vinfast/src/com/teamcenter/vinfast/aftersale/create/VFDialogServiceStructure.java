package com.teamcenter.vinfast.aftersale.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

public class VFDialogServiceStructure extends AbstractSWTDialog {

	public Combo cb_Donor;
	public Button bt_Create;
	public Combo cb_Veh;
	public ProgressBar progressBar;

	public VFDialogServiceStructure(Shell parent) {
		super(parent);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Composite container = toolkit.createComposite(parent);
		container.setBackgroundMode(SWT.INHERIT_FORCE);
		container.setLayout(new GridLayout(2, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);

		Label lb_VehCategory = new Label(container, SWT.NONE);
		GridData gd_VehCategory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_VehCategory.verticalIndent = 10;
		gd_VehCategory.horizontalIndent = 5;
		lb_VehCategory.setLayoutData(gd_VehCategory);
		lb_VehCategory.setText("Vehicle Category :");
		
		cb_Veh = new Combo(container, SWT.READ_ONLY);
		GridData gd_Veh = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_Veh.verticalIndent = 10;
		gd_Veh.horizontalIndent = 5;
		cb_Veh.setLayoutData(gd_Veh);
		cb_Veh.setToolTipText("To enable \"Create\" button, fill all values");
		
		//Donor Vehicle Label
		Label lb_Donor = new Label(container, SWT.NONE);
		GridData gd_Donor = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_Donor.verticalIndent = 10;
		gd_Donor.horizontalIndent = 5;
		lb_Donor.setLayoutData(gd_Donor);
		lb_Donor.setText("Donor Vehicle :");

		cb_Donor = new Combo(container, SWT.READ_ONLY);
		GridData gd_cb_Donor = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cb_Donor.verticalIndent = 10;
		gd_cb_Donor.horizontalIndent = 5;
		cb_Donor.setLayoutData(gd_cb_Donor);
		cb_Donor.setToolTipText("To enable \"Create\" button, fill all values");

		progressBar = new ProgressBar(container, SWT.SMOOTH);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setVisible(false);

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ServiceBOM Create Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(350, 180);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		bt_Create = createButton(parent, IDialogConstants.OK_ID, "Create", true);
		bt_Create.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		// super.okPressed();
	}

}
