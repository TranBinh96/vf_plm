package com.vinfast.sap.dialogs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.sap.util.PropertyDefines;

public class MESTransferDialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	int total = 0;
	int counter = 1;

	public Combo cbServer;
	public Button btnSave;
	public Button btnPrepare;
	public Text txtPlant;
	public Text txtMCN;
	public ProgressBar progressBar;
	private Label lblIp;
	public Combo cbIP;
	public Browser brwReport;

	private TCSession session;
	private LinkedHashMap<String, String[]> ipMapping = null;
	public LinkedHashMap<String, LinkedHashMap<String, String>> accMapping = null;
	private String company;
	private Label lblShop_2;
	public Text txtShop;
	public Label lblSendByServer;
	public Button ckbSendByServer;

	public MESTransferDialog(Shell parent, TCSession session, String company) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
		this.session = session;
		this.company = company;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Please refer report after transfer for errors", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);

		container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(4, false);
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblMCN = new Label(container, SWT.NONE);
		lblMCN.setText("MCN:");

		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setText("SERVER:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });

		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setText("PLANT:");

		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblIp = new Label(container, SWT.NONE);
		lblIp.setText("IP:");

		cbIP = new Combo(container, SWT.READ_ONLY);
		cbIP.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblShop_2 = new Label(container, SWT.NONE);
		lblShop_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblShop_2.setText("SHOP:");

		txtShop = new Text(container, SWT.BORDER);
		txtShop.setEditable(false);
		txtShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblSendByServer = new Label(container, SWT.NONE);
		lblSendByServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSendByServer.setText("Send by Server:");
		lblSendByServer.setVisible(false);

		ckbSendByServer = new Button(container, SWT.CHECK);
		ckbSendByServer.setVisible(false);

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);

		brwReport = new Browser(container, SWT.NONE);
		brwReport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		cbServer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String environment = cbServer.getText();
				String shop;
				if (company.compareTo(PropertyDefines.VIN_FAST) == 0 || company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
					shop = txtShop.getText();
				} else {
					shop = txtPlant.getText();
				}
				cbIP.removeAll();
				if (ipMapping.containsKey(environment + "_" + shop)) {
					cbIP.setItems(ipMapping.get(environment + "_" + shop));
					cbIP.select(0);
				}
//				String serverValue = cbServer.getText();
//				if (serverValue.equals("PRODUCTION")) {
//					serverValue = "VF_MES_PLANTCODE_IP";
//				} else if (serverValue.equals("QA")) {
//					serverValue = "VF_MES_PLANTCODE_IP_QA";
//				} else {
//					serverValue = "VF_MES_PLANTCODE_IP_DEV";
//				}
//
//				String[] values = UIGetValuesUtility.getPreferenceValues(session, serverValue);
//				if (values != null) {
//					String serverIP = "";
//					for (String value : values) {
//						String[] splitValue = value.split("=");
//						if (splitValue[0].trim().equals(txtPlant.getText())) {
//							serverIP = splitValue[1].trim();
//							break;
//						}
//					}
//
//					if (serverIP.equals("")) {
//						MessageBox.post("MES Server IP details not configured for this plant. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
//						cbIP.select(-1);
//					} else {
//						if (serverIP.contains(",")) {
//							String[] IPAddress = serverIP.split(",");
//							cbIP.setItems(IPAddress);
//							cbIP.select(0);
//						} else {
//							cbIP.setItems(new String[] { serverIP });
//							cbIP.select(0);
//						}
//					}
//				} else {
//					MessageBox.post("Error loading MES Server IP details. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
//				}
			}
		});

		loadIPConfig();

		return area;
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
		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnPrepare = createButton(parent, IDialogConstants.CLOSE_ID, "Prepare Data", false);
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

	public void setServer(String server) {
		this.cbServer.setText(server);
	}

	public String getServer() {
		return this.cbServer.getText();
	}

	public void setServerIP(String IP) {
		this.cbIP.setText(IP);
	}

	public String getServerIP() {
		return this.cbIP.getText();
	}

	public Button getOkButton() {
		return this.btnSave;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public void setProgressStatus(int percentage) {
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(1000);
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

	private void loadIPConfig() {
		LinkedHashMap<String, String> ips = null;
		if (company.compareTo(PropertyDefines.VIN_FAST) == 0 || company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
			ips = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("PRODUCTION", "VF_MES_SHOP_IP");
					put("QA", "VF_MES_SHOP_IP_QA");
					put("DEV", "VF_MES_SHOP_IP_DEV");
				}
			};
		} else {
			ips = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("PRODUCTION", "VF_MES_PLANTCODE_IP");
					put("QA", "VF_MES_PLANTCODE_IP_QA");
					put("DEV", "VF_MES_PLANTCODE_IP_DEV");
				}
			};
		}

		ipMapping = new LinkedHashMap<String, String[]>();

		for (Map.Entry<String, String> ip : ips.entrySet()) {
			String[] values = TCExtension.GetPreferenceValues(ip.getValue(), session);
			if (values != null && values.length > 0) {
				for (String item : values) {
					if (item.contains("=")) {
						String[] str = item.split("=");
						if (str.length >= 2) {
							String environment = ip.getKey() + "_" + str[0];
							if (str[1].contains(",")) {
								ipMapping.put(environment, str[1].split(","));
							} else {
								ipMapping.put(environment, new String[] { str[1] });
							}
						}
					}
				}
			}
		}

		LinkedHashMap<String, String> accs = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("PRODUCTION", "VF_MES_API_ACCOUNT");
				put("QA", "VF_MES_API_ACCOUNT_QA");
				put("DEV", "VF_MES_API_ACCOUNT_DEV");
			}
		};

		accMapping = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		for (Map.Entry<String, String> acc : accs.entrySet()) {
			String[] values = TCExtension.GetPreferenceValues(acc.getValue(), session);
			if (values != null && values.length > 0) {
				for (String item : values) {
					if (item.contains("==")) {
						String[] str = item.split("==");
						if (str.length >= 2) {
							LinkedHashMap<String, String> accInfo = new LinkedHashMap<String, String>();
							accInfo.put("Username", str[0]);
							accInfo.put("Password", str[1]);
							accMapping.put(acc.getKey(), accInfo);
						}
					}
				}
			}
		}
	}
}
