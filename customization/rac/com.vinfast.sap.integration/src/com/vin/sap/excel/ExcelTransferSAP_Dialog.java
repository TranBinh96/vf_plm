package com.vin.sap.excel;

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
import org.eclipse.wb.swt.SWTResourceManager;

public class ExcelTransferSAP_Dialog extends TitleAreaDialog {
	int total = 0;
	int counter = 1;
	public Text txtLink;
	public Combo cbServer;
	public Combo cbFormat;
	public ProgressBar progressBar;
	public Button btnSave;
	public Button btnDownLoadForm;
	public Button btnUpLoadForm;
	public Text txtMCN;
	private Label lblMcn;
	private Label lblForm;

	public ExcelTransferSAP_Dialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(4, false);
		container.setLayout(gl_container);
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.grabExcessVerticalSpace = false;
		container.setLayoutData(gd_container);

		lblMcn = new Label(container, SWT.NONE);
		lblMcn.setText("MCN:");
		lblMcn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setText("Server:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });

		Label lblLink = new Label(container, SWT.NONE);
		lblLink.setText("Link:");

		txtLink = new Text(container, SWT.BORDER);
		txtLink.setEditable(false);
		txtLink.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lblForm = new Label(container, SWT.NONE);
		lblForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblForm.setText("Form:");

		cbFormat = new Combo(container, SWT.READ_ONLY);
		cbFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_progressBar.verticalIndent = 5;
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
		return new Point(550, 375);
	}

	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", true);
		btnDownLoadForm = createButton(parent, IDialogConstants.CLOSE_ID, "Download Form", true);
		btnUpLoadForm = createButton(parent, IDialogConstants.CLOSE_ID, "Upload Form", true);
	}

	public void setProgressStatus(int percentage) {
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void updateprogressBar() {
		float value = (float) counter / (float) total;
		this.progressBar.setSelection((int) (value * 100));
		this.progressBar.redraw();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter++;
	}

	public int getCounter() {
		return counter++;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
}
