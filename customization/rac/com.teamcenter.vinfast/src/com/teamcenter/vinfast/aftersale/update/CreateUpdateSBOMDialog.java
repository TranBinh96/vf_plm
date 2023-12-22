package com.teamcenter.vinfast.aftersale.update;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class CreateUpdateSBOMDialog extends TitleAreaDialog {

	public Combo cb_Rule;
	public Combo cb_Variant;
	public ProgressBar progressBar;
	public Button bt_Update;
	public Text logText;

	public CreateUpdateSBOMDialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Update SBOM");
		setMessage("Please fill all the fields to update SBOM", IMessageProvider.INFORMATION);
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Composite container = toolkit.createComposite(parent);
		container.setBackgroundMode(SWT.INHERIT_FORCE);
		container.setLayout(new GridLayout(2, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);

		//Donor Vehicle Label
		Label lb_Rule = new Label(container, SWT.NONE);
		GridData gd_Rule = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_Rule.verticalIndent = 10;
		gd_Rule.horizontalIndent = 5;
		lb_Rule.setLayoutData(gd_Rule);
		lb_Rule.setText("Revision Rule :");

		cb_Rule = new Combo(container, SWT.READ_ONLY);
		GridData gdCb_Rule = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdCb_Rule.widthHint = 223;
		gdCb_Rule.verticalIndent = 10;
		gdCb_Rule.horizontalIndent = 5;
		cb_Rule.setLayoutData(gdCb_Rule);

		progressBar = new ProgressBar(container, SWT.SMOOTH);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setVisible(false);
		
		logText = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_txtLog = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_txtLog.heightHint = 140;
		logText.setLayoutData(gd_txtLog);
		logText.setVisible(false);
		
		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SBOM Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 220);
	}

	@Override
	public void setTitle(String newTitle) {
		// TODO Auto-generated method stub
		super.setTitle(newTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		bt_Update = createButton(parent, IDialogConstants.OK_ID, "Update", true);
		bt_Update.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		// super.okPressed();
	}
	
}
