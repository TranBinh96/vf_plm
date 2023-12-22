package com.teamcenter.vinfast.admin;

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

import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Group;

public class BaselineRequest_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	private Label lblPackageQuantity;
	public Text txtPackageQuantity;
	public Button ckbBaseline;
	private Group grpBaseline;
	private Label lblPackageQuantity_1;
	public Text txtDesc;
	private Label lblBaselineRevisionRule_1;
	public Combo cbBaselineTemplate;
	private Label lblRevisionRule_1;
	public Combo cbRevisionRule;
	private Group grpSnapshoot;
	private Group grpPackage;
	private Button ckbSnapshoot;
	
	public BaselineRequest_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		
		try {
			setMessage("Define business object create information");
			setTitle("Create Baseline automation ticket");

			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				grpPackage = new Group(container, SWT.NONE);
				grpPackage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
				grpPackage.setText("Package");
				grpPackage.setLayout(new GridLayout(2, false));
				grpPackage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
				{
					lblPackageQuantity = new Label(grpPackage, SWT.NONE);
					lblPackageQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					lblPackageQuantity.setText("Quantity: (*)");
				}
				{
					txtPackageQuantity = new Text(grpPackage, SWT.BORDER);
					txtPackageQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
			}
			{
				ckbBaseline = new Button(container, SWT.CHECK);
				ckbBaseline.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			}
			{
				grpBaseline = new Group(container, SWT.NONE);
				grpBaseline.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
				grpBaseline.setText("Baseline");
				grpBaseline.setLayout(new GridLayout(2, false));
				grpBaseline.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				{
					lblPackageQuantity_1 = new Label(grpBaseline, SWT.NONE);
					lblPackageQuantity_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblPackageQuantity_1.setText("Description:");
				}
				{
					txtDesc = new Text(grpBaseline, SWT.BORDER);
					txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
				{
					lblBaselineRevisionRule_1 = new Label(grpBaseline, SWT.NONE);
					lblBaselineRevisionRule_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblBaselineRevisionRule_1.setText("Template: (*)");
				}
				{
					cbBaselineTemplate = new Combo(grpBaseline, SWT.READ_ONLY);
					cbBaselineTemplate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
				{
					lblRevisionRule_1 = new Label(grpBaseline, SWT.NONE);
					lblRevisionRule_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblRevisionRule_1.setText("Revision Rule: (*)");
				}
				{
					cbRevisionRule = new Combo(grpBaseline, SWT.READ_ONLY);
					cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
				
				container.setLayout(gl_container);
				container.setLayoutData(new GridData(GridData.FILL_BOTH));
			}
			{
				ckbSnapshoot = new Button(container, SWT.CHECK);
				ckbSnapshoot.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			}
			{
				grpSnapshoot = new Group(container, SWT.NONE);
				grpSnapshoot.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
				grpSnapshoot.setText("Snapshoot");
				grpSnapshoot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			}
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Baseline Automation");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(470, 463);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Baseline", true);
		btnCreate.setText("Start");
	}
}
