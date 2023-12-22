package com.vinfast.car.mes.plantmodel;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.integration.arch.DialogAbstract;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class PlantModelDialog extends DialogAbstract {
	int total = 0;
	int counter = 1;

	public Combo comboServer;

	@Override
	public Combo getComboServer() {
		return comboServer;
	}

	public Button btnTransfer;
	public Button btnPrepare;
	private Text textPlant;
	private Text textMCN;
	private ProgressBar progressBar;
	private Label lblIp;
	private Combo comboIP;
	private TCSession clientSession;
	private Label lbProcessStatus;

	@Override
	public void setClientSession(TCSession clientSession) {
		this.clientSession = clientSession;
	}

	public PlantModelDialog(Shell parent) {
		super(parent);
		super.setTitle("MES PlantModel Transfer");
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

		Label lblMCN = new Label(container, SWT.NONE);
		lblMCN.setText("MCN:");

		textMCN = new Text(container, SWT.BORDER);
		textMCN.setEditable(false);
		textMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setText("SERVER:");

		comboServer = new Combo(container, SWT.READ_ONLY);
		comboServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboServer.setItems(new String[] { PropertyDefines.SERVER_PRODUCTION, PropertyDefines.SERVER_QA, PropertyDefines.SERVER_DEV, PropertyDefines.SERVER_DEV_TEST });

		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setText("PLANT:");

		textPlant = new Text(container, SWT.BORDER);
		textPlant.setEditable(false);
		textPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblIp = new Label(container, SWT.NONE);
		lblIp.setText("IP:");

		comboIP = new Combo(container, SWT.READ_ONLY);
		comboIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_progressBar.heightHint = 26;
		progressBar.setLayoutData(gd_progressBar);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		lbProcessStatus = new Label(container, SWT.NONE);
		lbProcessStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		lbProcessStatus.setText("Start.....");

		comboServer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String serverValue = comboServer.getText();
				if (serverValue.equals("PRODUCTION")) {
					serverValue = "VF_MES_PLANTCODE_IP";
				} else if (serverValue.equals("QA")) {
					serverValue = "VF_MES_PLANTCODE_IP_QA";
				} else {
					serverValue = "VF_MES_PLANTCODE_IP_DEV";
				}

				String[] values = UIGetValuesUtility.getPreferenceValues(clientSession, serverValue);
				if (values != null) {
					String serverIP = "";
					for (String value : values) {
						String[] splitValue = value.split("=");
						if (splitValue[0].trim().equals(textPlant.getText())) {
							serverIP = splitValue[1].trim();
							break;
						}
					}

					if (serverIP.equals("")) {
						MessageBox.post("MES Server IP details not configured for this plant. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
						comboIP.select(-1);
					} else {
						if (serverIP.contains(",")) {
							String[] IPAddress = serverIP.split(",");
							comboIP.setItems(IPAddress);
							comboIP.select(0);
						} else {
							comboIP.setItems(new String[] { serverIP });
							comboIP.select(0);
						}
					}
				} else {
					MessageBox.post("Error loading MES Server IP details. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				}
			}
		});

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
		newShell.setText("MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
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
		this.textMCN.setText(MCN);
	}

	public String getMCN() {
		return this.textMCN.getText();
	}

	public void setPlant(String plant) {
		this.textPlant.setText(plant);
	}

	public void setServer(String server) {
		this.comboServer.setText(server);
	}

	@Override
	public String getServer() {
		return this.comboServer.getText();
	}

	public void setServerIP(String IP) {
		this.comboIP.setText(IP);
	}

	@Override
	public String getServerIP() {
		return this.comboIP.getText();
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
