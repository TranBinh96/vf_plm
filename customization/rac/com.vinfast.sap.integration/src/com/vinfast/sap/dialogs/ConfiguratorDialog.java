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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConfiguratorDialog extends TitleAreaDialog{
	public Text txtModel;
	public Text txtYear;
	public Text txtMCN;
	public Text txtPlant;
	public Combo cbServer;
	public Combo comboSystem;
	public Button btnSave;
	public Button btnPrepare;
	public String materialCode;
	
	public ConfiguratorDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer",IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblModel = new Label(container, SWT.NONE);
		lblModel.setText("MODEL:");
		
		txtModel = new Text(container, SWT.BORDER);
		txtModel.setEditable(false);
		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblYear = new Label(container, SWT.NONE);
		lblYear.setText("YEAR:");

		txtYear = new Text(container, SWT.BORDER);
		txtYear.setEditable(false);
		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMcn = new Label(container, SWT.NONE);
		lblMcn.setText("MCN:");

		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblServer.setText("SERVER:");
				
		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] {"PRODUCTION","QA","DEV"});

		Label lblPlant = new Label(container, SWT.NONE);
		lblPlant.setText("PLANT:");

		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 250);
	}
	
	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnPrepare = createButton(parent, IDialogConstants.CLOSE_ID, "Prepare Data", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	public void setModel(String model) {
		this.txtModel.setText(model);
	}
	
	public void setYear(String year) {
		this.txtYear.setText(year);
	}
	
	public void setMCN(String MCN) {
		this.txtMCN.setText(MCN);
	}
	
	public void setPlant(String plant) {
		this.txtPlant.setText(plant);
	}
	
	public void setServer(String server) {
		this.cbServer.setText(server);
	}
	
	public String getModel() {
		return this.txtModel.getText();
	}
	
	public String getYear() {
		return this.txtYear.getText();
	}
	
	public String getMCN() {
		return this.txtMCN.getText();
	}
	
	public String getPlant() {
		return this.txtPlant.getText();
	}
	
	public String getServer() {
		return this.cbServer.getText();
	}
	
	public Button getOkButton() {
		return this.btnSave;
	}
	
	public Button getPrepareButton() {
		return this.btnPrepare;
	}
	
	public void setMaterialCode(String materialCode) {
		this.materialCode = materialCode;
	}
	
	public String getMaterialCode() {
		return materialCode;
	}
	
}
