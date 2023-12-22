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
import org.eclipse.wb.swt.SWTResourceManager;

public class BOMBOPDialog extends TitleAreaDialog {
	int total = 0;
	int counter = 1;
	public Text txtModel;
	public Text txtYear;
	public Text txtMCN;
	public Text txtPlant;
	public Text txtShop;
	public Combo cbServer;
	public Combo comboSystem;
	public Button btnTransfer;
	public Button btnPrepare;
	public String materialCode;
	public boolean isDryrunEnable = false;

	public BOMBOPDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Click \"Prepare Data\" to see error report and transfer", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.grabExcessVerticalSpace = false;
		container.setLayoutData(gd_container);

		Label lblModel = new Label(container, SWT.NONE);
		lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModel.setText("MODEL:");
		
		txtModel = new Text(container, SWT.BORDER);
		txtModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtModel.setEditable(false);
		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblYear = new Label(container, SWT.NONE);
		lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblYear.setText("YEAR:");

		txtYear = new Text(container, SWT.BORDER);
		txtYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtYear.setEditable(false);
		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPlant = new Label(container, SWT.NONE);
		lblPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPlant.setText("PLANT:");
		
		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblShop.setText("SHOP:");

		txtShop = new Text(container, SWT.BORDER);
		txtShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtShop.setEditable(false);
		txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMcn = new Label(container, SWT.NONE);
		lblMcn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMcn.setText("MCN:");
		
		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblServer.setText("SERVER:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("PLM SAP Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(460, 260);
	}

	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnTransfer = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnPrepare = createButton(parent, 4576, "Prepare Data", true);
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

	public void setShop(String shop) {

		this.txtShop.setText(shop);
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

	public String getShop() {

		return this.txtShop.getText();
	}

	public String getServer() {

		return this.cbServer.getText();
	}

	public Button getTransferButton() {

		return this.btnTransfer;
	}
	
	public Button getPrepareButton() {

		return this.btnPrepare;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getCounter() {
		return counter++;
	}

	public void setMaterialCode(String materialCode) {

		this.materialCode = materialCode;
	}

	public String getMaterialCode() {

		return materialCode;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
}
