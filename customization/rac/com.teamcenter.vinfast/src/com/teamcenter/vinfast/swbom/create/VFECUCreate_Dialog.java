package com.teamcenter.vinfast.swbom.create;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VFECUCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	public Button ckbOpenOnCreate;
	private Label lblName;
	private Label lblId_1;
	private Label lblId_2;
	public Text txtName;
	public Text txtDescription;
	public Combo cbECUType;
	private Label lblName_1;
	public Combo cbProgram;

	public VFECUCreate_Dialog(Shell parentShell) throws TCException {
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
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblName_1 = new Label(container, SWT.NONE);
				lblName_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName_1.setText("Program: (*)");
			}
			{
				cbProgram = new Combo(container, SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblId_1 = new Label(container, SWT.NONE);
				lblId_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_1.setText("Description:");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_txtDescription.heightHint = 54;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblId_2 = new Label(container, SWT.NONE);
				lblId_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_2.setText("ECU Type: (*)");
			}
			{
				cbECUType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbECUType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbECUType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 400);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create VF ECU");
	}
}
