package com.teamcenter.integration.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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

import com.teamcenter.integration.arch.DialogAbstract;
import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.sap.util.PropertyDefines;

public class SAPTransferDialog extends DialogAbstract {
	int total = 0;
	int counter = 1;

	public Combo cbServer;

	@Override
	public Combo getComboServer() {
		return cbServer;
	}

	public Button btnTransfer;
	public Button btnPrepare;
	private Text txtPlant;
	private Text txtMCN;
	private ProgressBar progressBar;
	private Label lbProcessStatus;
	private Label lblModel;
	private Label lblYear;
	private Label lblShop_1;
	private Text txtModel;
	private Text txtYear;
	private Text txtShop;
	private Browser brwReport;
	private Label label;

	@Override
	public void setClientSession(TCSession clientSession) {
	}

	public SAPTransferDialog(Shell parent) {
		super(parent);
		super.setTitle("SAP Transfer");
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Please refer report after transfer for errors", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		lblModel = new Label(container, SWT.NONE);
		lblModel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblModel.setText("Model:");

		txtModel = new Text(container, SWT.BORDER);
		txtModel.setEditable(false);
		txtModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblYear = new Label(container, SWT.NONE);
		lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblYear.setText("Year:");

		txtYear = new Text(container, SWT.BORDER);
		txtYear.setEditable(false);
		txtYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setText("Plant:");

		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblShop_1 = new Label(container, SWT.NONE);
		lblShop_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblShop_1.setText("Shop:");

		txtShop = new Text(container, SWT.BORDER);
		txtShop.setEditable(false);
		txtShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMCN = new Label(container, SWT.NONE);
		lblMCN.setText("MCN:");

		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setText("SERVER:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] { PropertyDefines.SERVER_PRODUCTION, PropertyDefines.SERVER_QA, PropertyDefines.SERVER_DEV });

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_progressBar.heightHint = 26;
		progressBar.setLayoutData(gd_progressBar);

		lbProcessStatus = new Label(container, SWT.NONE);
		lbProcessStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		lbProcessStatus.setText("Start.....");

		label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_label.widthHint = 443;
		label.setLayoutData(gd_label);

		brwReport = new Browser(container, SWT.NONE);
		brwReport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		return area;
	}

	@Override
	public Label getLbProcessStatus() {
		return lbProcessStatus;
	}

	public void setLbProcessStatus(Label lbProcessStatus) {
		this.lbProcessStatus = lbProcessStatus;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP Transfer");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 400);
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
		btnTransfer = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnPrepare = createButton(parent, IDialogConstants.CLOSE_ID, "Prepare", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public void setMCN(String MCN) {
		this.txtMCN.setText(MCN);
	}

	public String getMCN() {
		return this.txtMCN.getText();
	}

	public void setPlant(String plant) {
		this.txtPlant.setText(plant);
	}

	public void setYear(String year) {
		this.txtYear.setText(year);
	}

	public void setServer(String server) {
		this.cbServer.setText(server);
	}

	@Override
	public String getServer() {
		return this.cbServer.getText();
	}

	public void setServerIP(String IP) {
//		this.comboIP.setText(IP);
	}

	@Override
	public String getServerIP() {
		return "";// this.comboIP.getText();
	}

	public Button getOkButton() {
		return this.btnTransfer;
	}

	@Override
	public void setTotal(int total) {
		this.total = total;
	}

	public void setProgressStatus(int percentage) {
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
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

	@Override
	protected Button getBtnTransfer() {
		return btnTransfer;
	}

	@Override
	protected Button getBtnPrepare() {
		return btnPrepare;
	}

	@Override
	public void setLabelProcessStatus(String text) {
		this.lbProcessStatus.setText(text);
	}

	@Override
	public void setProcessPercent(int percent) {
		this.progressBar.setSelection(percent);
		this.progressBar.redraw();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
