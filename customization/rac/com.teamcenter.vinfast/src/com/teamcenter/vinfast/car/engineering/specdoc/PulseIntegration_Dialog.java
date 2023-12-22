package com.teamcenter.vinfast.car.engineering.specdoc;

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
import org.eclipse.wb.swt.ResourceManager;

public class PulseIntegration_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	private Label lblId;
	private Label lblName;
	public Text txtMRV;
	public Text txtDownloadFolder;
	public Button btnFolderSelect;

	public PulseIntegration_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Get VRM Information");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId.setText("VRM: (*)");
			}
			{
				txtMRV = new Text(container, SWT.BORDER);
				GridData gd_txtMRV = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_txtMRV.heightHint = 50;
				txtMRV.setLayoutData(gd_txtMRV);
			}
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Download Folder: (*)");
			}
			{
				txtDownloadFolder = new Text(container, SWT.BORDER);
				txtDownloadFolder.setEditable(false);
				txtDownloadFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnFolderSelect = new Button(container, SWT.NONE);
				btnFolderSelect.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/foldercollapsed_16.png"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
		btnAccept.setText("Download");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 340);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Pulse Integration");
	}
}
