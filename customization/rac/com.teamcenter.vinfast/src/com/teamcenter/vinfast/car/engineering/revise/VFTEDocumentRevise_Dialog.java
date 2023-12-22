package com.teamcenter.vinfast.car.engineering.revise;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.DateTime;

public class VFTEDocumentRevise_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	private Label lblId;
	private Label lblName;
	private Label lblId_1;
	public Text txtID;
	public Text txtName;
	public Text txtDescription;
	public Text txtRevision;
	private Label label;
	public Button ckbTargetReviseDate;
	public DateTime datTargetReviseDate;
	private Label lblTargetReleaseDate;
	public DateTime datTargetReleaseDate;

	public VFTEDocumentRevise_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
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
				label = new Label(container, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				label.setText("/");
			}
			{
				txtRevision = new Text(container, SWT.BORDER);
				txtRevision.setEnabled(false);
				txtRevision.setEditable(false);
				GridData gd_txtRevision = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
				gd_txtRevision.widthHint = 70;
				txtRevision.setLayoutData(gd_txtRevision);
			}
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				lblId_1 = new Label(container, SWT.NONE);
				lblId_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_1.setText("Description:");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
				gd_txtDescription.heightHint = 54;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblTargetReleaseDate = new Label(container, SWT.NONE);
				lblTargetReleaseDate.setText("Target Release Date:");
			}
			{
				datTargetReleaseDate = new DateTime(container, SWT.BORDER);
				datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
			}
			{
				ckbTargetReviseDate = new Button(container, SWT.CHECK);
				ckbTargetReviseDate.setText("Target Revise Date:");
			}
			{
				datTargetReviseDate = new DateTime(container, SWT.BORDER);
				datTargetReviseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 560);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("VFTE Document");
	}
}
