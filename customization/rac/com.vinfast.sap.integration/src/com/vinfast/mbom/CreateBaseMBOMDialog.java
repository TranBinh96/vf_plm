package com.vinfast.mbom;


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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

public class CreateBaseMBOMDialog extends AbstractSWTDialog {

	public Combo cb_Donor;
	public Combo cb_Plant;
	public Combo cb_Type;
	public Button bt_Create;

	public CreateBaseMBOMDialog(Shell parent) {
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
		//Plant Label Start
		Label lb_Plant = new Label(container, SWT.NONE);
		GridData gd_lbPlant = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lbPlant.verticalIndent = 10;
		gd_lbPlant.horizontalIndent = 5;
		lb_Plant.setLayoutData(gd_lbPlant);
		lb_Plant.setText("Plant :");

		cb_Plant = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.verticalIndent = 10;
		gd_combo.horizontalIndent = 5;
		cb_Plant.setLayoutData(gd_combo);
		cb_Plant.setToolTipText("To enable \"Create\" button, fill all values");
		//Type Label Start
		Label lbType = new Label(container, SWT.NONE);
		GridData gd_lbType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lbType.verticalIndent = 10;
		gd_lbType.horizontalIndent = 5;
		lbType.setLayoutData(gd_lbType);
		lbType.setText("Assembly Location :");

		cb_Type = new Combo(container, SWT.READ_ONLY);
		GridData gd_cb_Type = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cb_Type.verticalIndent = 10;
		gd_cb_Type.horizontalIndent = 5;
		cb_Type.setLayoutData(gd_cb_Type);
		cb_Type.setToolTipText("To enable \"Create\" button, fill all values");
		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("MBOM Create Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 200);
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
