package com.vinfast.sap.dialogs;

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

public class SAPTransfer_Dialog extends TitleAreaDialog {
	public Combo cbServer;
	public Button btnSave;
	public Text txtPlant;
	public Text txtMCN;
	public ProgressBar progressBar;

	public SAPTransfer_Dialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);

		Label lblMCN = new Label(container, SWT.NONE);
		lblMCN.setText("MCN:");

		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblServer.setText("Server:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });

		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblShop.setText("Plant:");

		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}

	public void setProgressVisible(boolean status) {
		progressBar.setVisible(true);
		progressBar.getShell().redraw();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
	}

	public void setProgressStatus(int current, int total) {
		float value = (float) current / (float) total;
		int percentage = (int) (value * 100);
		
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
