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

public class PhantomDialog extends TitleAreaDialog{

	int total = 0;
	int counter = 1;
	public Text textModel;
	public Text textRootMaterial;
	public Text textMCN;
	public Text textPlant;
	public Text textShop;
	public Combo comboServer;
	public Combo comboSystem;
	public ProgressBar progressBar;
	public Button btnSave;
	public Button btnDryRun;
	public String materialCode;
	public boolean isDryrunEnable = false;
	
	public PhantomDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		// TODO Auto-generated constructor stub
	}
	
	public PhantomDialog(Shell parent, boolean isDryrunEnable) {
		super(parent);
		this.isDryrunEnable = isDryrunEnable;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {

		setMessage("Dry run to see error report and transfer",IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(6, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.grabExcessVerticalSpace = false;
		container.setLayoutData(gd_container);
		
		Label lblModel = new Label(container, SWT.NONE);
		GridData gd_lblModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblModel.verticalIndent = 10;
		gd_lblModel.horizontalIndent = 5;
		lblModel.setLayoutData(gd_lblModel);
		lblModel.setText("MODEL:");
		
		textModel = new Text(container, SWT.BORDER);
		textModel.setEditable(false);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_text.verticalIndent = 10;
		gd_text.horizontalIndent = 5;
		textModel.setLayoutData(gd_text);
		
		Label lblRootMaterial = new Label(container, SWT.NONE);
		GridData gd_lbRootMaterial = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lbRootMaterial.horizontalIndent = 5;
		gd_lbRootMaterial.verticalIndent = 10;
		lblRootMaterial.setLayoutData(gd_lbRootMaterial);
		lblRootMaterial.setText("ROOT MATERIAL:");
		
		textRootMaterial = new Text(container, SWT.BORDER);
		textRootMaterial.setEditable(true);
		GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_text_1.horizontalIndent = 5;
		gd_text_1.verticalIndent = 10;
		textRootMaterial.setLayoutData(gd_text_1);
		
		Label lblPlant = new Label(container, SWT.NONE);
		GridData gd_lblPlant = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPlant.verticalIndent = 10;
		gd_lblPlant.horizontalIndent = 5;
		lblPlant.setLayoutData(gd_lblPlant);
		lblPlant.setText("PLANT:");
		
		textPlant = new Text(container, SWT.BORDER);
		textPlant.setEditable(false);
		GridData gd_text_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_text_2.verticalIndent = 10;
		gd_text_2.horizontalIndent = 5;
		textPlant.setLayoutData(gd_text_2);
		
		Label lblShop = new Label(container, SWT.NONE);
		GridData gd_lblShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblShop.verticalIndent = 10;
		gd_lblShop.horizontalIndent = 5;
		lblShop.setLayoutData(gd_lblShop);
		lblShop.setText("SHOP:");
		
		textShop = new Text(container, SWT.BORDER);
		textShop.setEditable(false);
		GridData gd_text_3 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_text_3.verticalIndent = 10;
		gd_text_3.horizontalIndent = 5;
		textShop.setLayoutData(gd_text_3);
		
		Label lblMcn = new Label(container, SWT.NONE);
		GridData gd_lblMcn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMcn.verticalIndent = 10;
		gd_lblMcn.horizontalIndent = 5;
		lblMcn.setLayoutData(gd_lblMcn);
		lblMcn.setText("MCN:");
		
		textMCN = new Text(container, SWT.BORDER);
		textMCN.setEditable(false);
		GridData gd_text_4 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_text_4.verticalIndent = 10;
		gd_text_4.horizontalIndent = 5;
		textMCN.setLayoutData(gd_text_4);
		
		Label lblServer = new Label(container, SWT.NONE);
		GridData gd_lblServer = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblServer.verticalIndent = 10;
		gd_lblServer.horizontalIndent = 5;
		lblServer.setLayoutData(gd_lblServer);
		lblServer.setText("SERVER:");
		
		comboServer = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_combo.verticalIndent = 10;
		gd_combo.horizontalIndent = 5;
		comboServer.setLayoutData(gd_combo);
		comboServer.setItems(new String[] {"PRODUCTION","QA","DEV"});

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1);
		gd_progressBar.verticalIndent = 5;
		progressBar.setLayoutData(gd_progressBar);

		return area;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 321);
	}
	
	@Override
	public void setTitle(String newTitle) {
		// TODO Auto-generated method stub
		super.setTitle(newTitle);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Transfer", true);
		if (isDryrunEnable) btnDryRun = createButton(parent, 4576, "Test-Transfer", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	public void setModel(String model) {
		
		this.textModel.setText(model);
	}
	
	public void setRootMaterial(String rm) {
		
		this.textRootMaterial.setText(rm);
	}
	
	public void setMCN(String MCN) {
		
		this.textMCN.setText(MCN);
	}
	
	public void setPlant(String plant) {
		
		this.textPlant.setText(plant);
	}
	
	public void setShop(String shop) {
		
		this.textShop.setText(shop);
	}
	
	public void setServer(String server) {
		
		this.comboServer.setText(server);
	}
	
	public String getModel() {
		
		return this.textModel.getText();
	}
	
	public String getRootMaterial() {
		
		return this.textRootMaterial.getText();
	}
	
	public String getMCN() {
		
		return this.textMCN.getText();
	}
	
	public String getPlant() {
		
		return this.textPlant.getText();
	}
	
	public String getShop() {
		
		return this.textShop.getText();
	}
	
	public String getServer() {
		
		return this.comboServer.getText();
	}
	
	public Button getOkButton() {
		
		return this.btnSave;
	}
	
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public void setProgressStatus(int percentage) {
		
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		//super.okPressed();
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
