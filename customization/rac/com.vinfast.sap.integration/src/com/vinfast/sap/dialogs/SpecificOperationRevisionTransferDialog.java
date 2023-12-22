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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SpecificOperationRevisionTransferDialog extends TitleAreaDialog{
	
	int total = 0;
	int counter = 1;
	
	public Combo comboServer;
	public Button btnTransfer;
	public Button btnPrepare;
	public Button getBtnPrepare() {
		return btnPrepare;
	}

	private Text textPlant;
	private Text textMCN;
	private ProgressBar progressBar;
	private Label lblIp;
	private Combo comboIP;
	private TCSession clientSession;

	public SpecificOperationRevisionTransferDialog(Shell parent, TCSession session) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
		this.clientSession = session;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		setMessage("Please refer report after transfer for errors",IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.heightHint = 174;
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);
		
		Label lblMCN = new Label(container, SWT.NONE);
		GridData gd_textMCN = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_textMCN.verticalIndent = 10;
		gd_textMCN.horizontalIndent = 5;
		lblMCN.setLayoutData(gd_textMCN);
		lblMCN.setText("MCN:");
		
		textMCN = new Text(container, SWT.BORDER);
		textMCN.setEditable(false);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text_1.verticalIndent = 10;
		gd_text_1.horizontalIndent = 5;
		textMCN.setLayoutData(gd_text_1);
		
		Label lblServer = new Label(container, SWT.NONE);
		GridData gd_lblServer = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblServer.verticalIndent = 10;
		gd_lblServer.horizontalIndent = 5;
		lblServer.setLayoutData(gd_lblServer);
		lblServer.setText("SERVER:");
		
		comboServer = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.verticalIndent = 10;
		gd_combo.horizontalIndent = 5;
		comboServer.setLayoutData(gd_combo);
		comboServer.setItems(new String[] {"PRODUCTION","QA","DEV"});
		
		Label lblShop = new Label(container, SWT.NONE);
		GridData gd_lblShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblShop.verticalIndent = 10;
		gd_lblShop.horizontalIndent = 5;
		lblShop.setLayoutData(gd_lblShop);
		lblShop.setText("PLANT:");
		
		textPlant = new Text(container, SWT.BORDER);
		textPlant.setEditable(false);
		GridData gd_text_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_2.verticalIndent = 10;
		gd_text_2.horizontalIndent = 5;
		textPlant.setLayoutData(gd_text_2);
		
		lblIp = new Label(container, SWT.NONE);
		GridData gd_lblIp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblIp.verticalIndent = 10;
		gd_lblIp.horizontalIndent = 5;
		lblIp.setLayoutData(gd_lblIp);
		lblIp.setText("IP:");
		
		comboIP = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo1.verticalIndent = 10;
		gd_combo1.horizontalIndent = 5;
		comboIP.setLayoutData(gd_combo1);
		
		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_progressBar.heightHint = 33;
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);
		
		comboServer.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				String serverValue = comboServer.getText();

				if(serverValue.equals("PRODUCTION")) {

					serverValue = "VF_MES_PLANTCODE_IP";
					
				}else if(serverValue.equals("QA")) {

					serverValue = "VF_MES_PLANTCODE_IP_QA";

				}else {

					serverValue = "VF_MES_PLANTCODE_IP_DEV";
				}
				
				new UIGetValuesUtility();
				String[] values = UIGetValuesUtility.getPreferenceValues(clientSession, serverValue);
				
				if(values != null) {
					
					String serverIP = "";

					for(String value : values) {

						String[] splitValue = value.split("=");

						if(splitValue[0].trim().equals(textPlant.getText())) {

							serverIP =  splitValue[1].trim();
							break;
						}
					}

					if(serverIP.equals("")) {

						MessageBox.post("MES Server IP details not configured for this plant. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
						comboIP.select(-1);
					}else {

						if(serverIP.contains(",")) {
							
							String[] IPAddress = serverIP.split(",");
							comboIP.setItems(IPAddress);
							comboIP.select(0);
							
						}else {
							
							comboIP.setItems(new String[] {serverIP});
							comboIP.select(0);
						}

					}
				}else {

					MessageBox.post("Error loading MES Server IP details. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				}

			}
		});

		return area;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(481, 405);
	}
	
	@Override
	public void setTitle(String newTitle) {
		// TODO Auto-generated method stub
		super.setTitle(newTitle);
	}
	
	public void setProgressVisible(boolean status) {
		
		progressBar.setVisible(true);
		progressBar.getShell().redraw();
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		btnTransfer = createButton(parent, IDialogConstants.OK_ID, "Transfer", true);
		btnPrepare = createButton(parent, 4576, "Prepare", true);
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
	
	public String getServer() {
		
		return this.comboServer.getText();
	}
	
	public void setServerIP(String IP) {
		
		this.comboIP.setText(IP);
	}
	
	public String getServerIP() {
		return this.comboIP.getText();
	}
	
	public Button getTransferButton() {
		
		return this.btnTransfer;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		//super.okPressed();
	}
	
	public void updateprogressBar() {
		
		float value = (float)counter/(float)total;
		this.progressBar.setSelection((int) (value*100));
		this.progressBar.redraw();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		counter++;
	}

}
