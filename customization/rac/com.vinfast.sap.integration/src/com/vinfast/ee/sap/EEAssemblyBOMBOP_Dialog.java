//package com.vinfast.ee.sap;
//
//import org.eclipse.jface.dialogs.IDialogConstants;
//import org.eclipse.jface.dialogs.IMessageProvider;
//import org.eclipse.jface.dialogs.TitleAreaDialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.ProgressBar;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.wb.swt.SWTResourceManager;
//
//public class EEAssemblyBOMBOP_Dialog extends TitleAreaDialog {
//	public Text txtModel;
//	public Text txtYear;
//	public Text txtMCN;
//	public Text txtPlant;
//	public Text txtShop;
//	public Combo cbServer;
//	public Combo comboSystem;
//	public ProgressBar progressBar;
//	public Button btnSave;
//	public Button btnDryRun;
//	public String materialCode;
//	public boolean isDryrunEnable = false;
//
//	public EEAssemblyBOMBOP_Dialog(Shell parent) {
//		super(parent);
//		setHelpAvailable(false);
//		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
//	}
//
//	@Override
//	protected Control createDialogArea(Composite parent) {
//		setMessage("Dry run to see error report and transfer", IMessageProvider.INFORMATION);
//		Composite area = (Composite) super.createDialogArea(parent);
//		Composite container = new Composite(area, SWT.NONE);
//		container.setLayout(new GridLayout(4, false));
//		GridData gd_container = new GridData(GridData.FILL_BOTH);
//		gd_container.grabExcessVerticalSpace = false;
//		container.setLayoutData(gd_container);
//
//		Label lblModel = new Label(container, SWT.NONE);
//		lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblModel.setText("Model:");
//		
//		txtModel = new Text(container, SWT.BORDER);
//		txtModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		txtModel.setEditable(false);
//		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label lblYear = new Label(container, SWT.NONE);
//		lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblYear.setText("Year:");
//
//		txtYear = new Text(container, SWT.BORDER);
//		txtYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		txtYear.setEditable(false);
//		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label lblPlant = new Label(container, SWT.NONE);
//		lblPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblPlant.setText("Plant:");
//
//		txtPlant = new Text(container, SWT.BORDER);
//		txtPlant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		txtPlant.setEditable(false);
//		txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label lblShop = new Label(container, SWT.NONE);
//		lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblShop.setText("Shop:");
//
//		txtShop = new Text(container, SWT.BORDER);
//		txtShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		txtShop.setEditable(false);
//		txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label lblMcn = new Label(container, SWT.NONE);
//		lblMcn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblMcn.setText("MCN:");
//
//		txtMCN = new Text(container, SWT.BORDER);
//		txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		txtMCN.setEditable(false);
//		txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label lblServer = new Label(container, SWT.NONE);
//		lblServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		lblServer.setText("Server:");
//
//		cbServer = new Combo(container, SWT.READ_ONLY);
//		cbServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });
//
//		progressBar = new ProgressBar(container, SWT.NONE);
//		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1);
//		gd_progressBar.verticalIndent = 5;
//		progressBar.setLayoutData(gd_progressBar);
//
//		return area;
//	}
//
//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//		newShell.setText("SAP Transfer...");
//	}
//
//	@Override
//	protected Point getInitialSize() {
//		return new Point(450, 321);
//	}
//
//	@Override
//	public void setTitle(String newTitle) {
//		super.setTitle(newTitle);
//	}
//
//	@Override
//	protected void createButtonsForButtonBar(Composite parent) {
//		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
//		if (isDryrunEnable)
//			btnDryRun = createButton(parent, 4576, "Test-Transfer", true);
//	}
//
//	public void setProgressStatus(int current, int total) {
//		float value = (float) current / (float) total;
//		int percent = (int) (value * 100);
//		this.progressBar.setSelection(percent);
//		this.progressBar.redraw();
//		try {
//			Thread.sleep(50);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//}
