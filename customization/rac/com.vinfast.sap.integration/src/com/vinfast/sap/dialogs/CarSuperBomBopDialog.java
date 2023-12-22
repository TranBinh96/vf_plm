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
import org.eclipse.wb.swt.ResourceManager;

public class CarSuperBomBopDialog extends TitleAreaDialog{
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
	public Text txtSWBOM;
	private Label lbSW;
	public Button btnUpdateSWBOM;
	private boolean enableSWBOMFeature = true;
	
	public CarSuperBomBopDialog(Shell parent, boolean isSWBOM) {
		super(parent);
		setHelpAvailable(false);
		enableSWBOMFeature = isSWBOM;
		if (enableSWBOMFeature) {
			setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.ON_TOP);
		} else {
			setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Prepare Data then execute the Transfer",IMessageProvider.INFORMATION);
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
		txtModel.setEditable(false);
		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblYear = new Label(container, SWT.NONE);
		lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblYear.setText("YEAR:");
		
		txtYear = new Text(container, SWT.BORDER);
		txtYear.setEditable(false);
		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPlant = new Label(container, SWT.NONE);
		lblPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPlant.setText("PLANT:");
		
		txtPlant = new Text(container, SWT.BORDER);
		txtPlant.setEditable(false);
		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblShop.setText("SHOP:");
		
		txtShop = new Text(container, SWT.BORDER);
		txtShop.setEditable(false);
		txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMcn = new Label(container, SWT.NONE);
		lblMcn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMcn.setText("MCN:");
		
		txtMCN = new Text(container, SWT.BORDER);
		txtMCN.setEditable(false);
		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblServer.setText("SERVER:");
		
		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbServer.setItems(new String[] {"PRODUCTION","QA","DEV", "DEV-TEST"});
		
		lbSW = new Label(container, SWT.NONE);
		lbSW.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lbSW.setText("SWBOM:");
		
		txtSWBOM = new Text(container, SWT.BORDER);
		txtSWBOM.setEditable(false);
		txtSWBOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnUpdateSWBOM = new Button(container, SWT.NONE);
		btnUpdateSWBOM.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
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
		return new Point(500, 300);
	}
	
	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnTransfer = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnPrepare = createButton(parent, IDialogConstants.CLOSE_ID, "Prepare Data", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
	
	public void setCounter(int counter) {
		this.counter = counter;
	}
}
