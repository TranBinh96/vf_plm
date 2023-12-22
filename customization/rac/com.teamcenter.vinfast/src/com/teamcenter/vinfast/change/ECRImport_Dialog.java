package com.teamcenter.vinfast.change;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.List;

public class ECRImport_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	private Label lblEcr;
	public Text txtECR;
	public Browser browser;
	public Button btnUpload;
	public Button btnCancel;
	public Button btnDownload;
	
	public ECRImport_Dialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
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
				btnUpload = new Button(container, SWT.NONE);
				btnUpload.setToolTipText("Upload File");
				btnUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnUpload.setText("Upload File");
			}
			{
				btnDownload = new Button(container, SWT.NONE);
				btnDownload.setToolTipText("Download Template");
				btnDownload.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				btnDownload.setText("Download Template");
			}
			{
				browser = new Browser(container, SWT.NONE);
				browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
			}
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Import", false);
		btnCreate.setEnabled(false);
		
		btnCancel = createButton(parent, IDialogConstants.CLOSE_ID, "Cancel", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 400);
		super.configureShell(newShell);
		newShell.setText("ECR Information Upload");
	}
}
