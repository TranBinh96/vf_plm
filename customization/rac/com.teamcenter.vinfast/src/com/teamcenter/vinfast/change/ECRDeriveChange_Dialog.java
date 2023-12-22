package com.teamcenter.vinfast.change;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.SWTResourceManager;

public class ECRDeriveChange_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	private Label lblEcr;
	private Label lblId;
	private Label lblNewLabel;
	private Label lblDescription;
	public Text txtName;
	public Text txtDescription;
	private Label lblVehicleGroup;
	public Combo cbVehicleGroup;
	private Label lblModuleGroup;
	public Combo cbModuleGroup;
	public Text txtECR;
	public Text txtID;

	public ECRDeriveChange_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblEcr = new Label(container, SWT.NONE);
				lblEcr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblEcr.setText("ECR: ");
			}
			{
				txtECR = new Text(container, SWT.BORDER);
				txtECR.setEnabled(false);
				txtECR.setEditable(false);
				txtECR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setEnabled(false);
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblDescription = new Label(container, SWT.NONE);
				lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDescription.setText("Description: (*)");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_txtDescription.heightHint = 50;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblVehicleGroup = new Label(container, SWT.NONE);
				lblVehicleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblVehicleGroup.setText("Vehicle Group:");
			}
			{
				cbVehicleGroup = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbVehicleGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbVehicleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblModuleGroup = new Label(container, SWT.NONE);
				lblModuleGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblModuleGroup.setText("Module Group:");
			}
			{
				cbModuleGroup = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbModuleGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModuleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
		} catch (Exception e) {

		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.OK_ID, "Create", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Derive Change");
	}

	@Override
	protected void okPressed() {

	}
}
