package com.teamcenter.vinfast.report.bomcomparision;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class BOMComparisonDialog extends Dialog {
	private Text txtOldBomRev;
	private Combo cbOldRevRule;
	private Button btnAddOldBom;
	
	private Text txtNewBomRev;
	private Combo cbNewRevRule;
	private Button btnAddNewBom;
	private Button btnOK;
	private ProgressBar progressBar						   = null;
	private Composite container							   = null;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public BOMComparisonDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	public Text getTxtOldBomRev() {
		return txtOldBomRev;
	}

	public void setTxtOldBomRev(Text txtOldBomRev) {
		this.txtOldBomRev = txtOldBomRev;
	}

	public Combo getCbOldRevRule() {
		return cbOldRevRule;
	}

	public void setCbOldRevRule(Combo cbOldRevRule) {
		this.cbOldRevRule = cbOldRevRule;
	}
	
	public void setCbOldRevRuleText(String cbOldRevRule) {
		this.cbOldRevRule.setText(cbOldRevRule);
	}

	public Button getBtnAddOldBom() {
		return btnAddOldBom;
	}

	public void setBtnAddOldBom(Button btnAddOldBom) {
		this.btnAddOldBom = btnAddOldBom;
	}

	public Text getTxtNewBomRev() {
		return txtNewBomRev;
	}

	public void setTxtNewBomRev(Text txtNewBomRev) {
		this.txtNewBomRev = txtNewBomRev;
	}

	public Combo getCbNewRevRule() {
		return cbNewRevRule;
	}

	public void setCbNewRevRule(Combo cbNewRevRule) {
		this.cbNewRevRule = cbNewRevRule;
	}
	
	public void setCbNewRevRuleText(String cbNewRevRule) {
		this.cbNewRevRule.setText(cbNewRevRule);
	}

	public Button getBtnAddNewBom() {
		return btnAddNewBom;
	}

	public void setBtnAddNewBom(Button btnAddNewBom) {
		this.btnAddNewBom = btnAddNewBom;
	}

	public Button getBtnOK() {
		return btnOK;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		this.getShell().setText("BOM Comparison Report");
		container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginLeft = 10;
		/*=====================Left panel layout===========================*/
		Composite leftCompo = new Composite(container, SWT.BORDER);
		GridData gdLeft = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gdLeft.widthHint = 370;
		leftCompo.setLayoutData(gdLeft);
		leftCompo.setLayout(new GridLayout(3, false));
		new Label(leftCompo, SWT.NONE);
		
		Label lblOldBom = new Label(leftCompo, SWT.NONE);
		lblOldBom.setFont(SWTResourceManager.getFont("Segoe UI", 20, SWT.BOLD));
		lblOldBom.setText("OLD BOM");
		new Label(leftCompo, SWT.NONE);
		
		Label lblOldRevRule = new Label(leftCompo, SWT.NONE);
		lblOldRevRule.setText("Revision Rule");
		
		cbOldRevRule = new Combo(leftCompo, SWT.NONE);
		GridData gd_cbOldRevRule = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_cbOldRevRule.widthHint = 247;
		cbOldRevRule.setLayoutData(gd_cbOldRevRule);
		new Label(leftCompo, SWT.NONE);
		
		Label lblOldBomRev = new Label(leftCompo, SWT.NONE);
		lblOldBomRev.setText("BOM Revision");
		
		txtOldBomRev = new Text(leftCompo, SWT.BORDER);
		txtOldBomRev.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		txtOldBomRev.setEnabled(false);
		btnAddOldBom = new Button(leftCompo, SWT.NONE);
		btnAddOldBom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddOldBom.setText("Add");

		/*=====================Right panel layout===========================*/
		Composite rightCompo = new Composite(container, SWT.BORDER);
		GridData gdRight = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gdRight.widthHint = 370;
		rightCompo.setLayoutData(gdRight);
		rightCompo.setLayout(new GridLayout(3, false));
		new Label(rightCompo, SWT.NONE);
		
		Label lblNewBom = new Label(rightCompo, SWT.NONE);
		lblNewBom.setFont(SWTResourceManager.getFont("Segoe UI", 20, SWT.BOLD));
		lblNewBom.setText("NEW BOM");
		new Label(rightCompo, SWT.NONE);
		
		Label lblNewRevRule = new Label(rightCompo, SWT.NONE);
		lblNewRevRule.setText("Revision Rule");
		
		cbNewRevRule = new Combo(rightCompo, SWT.NONE);
		cbNewRevRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(rightCompo, SWT.NONE);
		
		Label lblNewBomRev = new Label(rightCompo, SWT.NONE);
		lblNewBomRev.setText("BOM Revision");
		
		txtNewBomRev = new Text(rightCompo, SWT.BORDER);
		txtNewBomRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtNewBomRev.setEnabled(false);
		btnAddNewBom = new Button(rightCompo, SWT.NONE);
		btnAddNewBom.setText("Add");
		
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnOK = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(838, 265);
	}

	public Composite getContainer() {
		return container;
	}

	public void setContainer(Composite container) {
		this.container = container;
	}

	public void createProgressBar() {
		Composite statusBar = new Composite(this.container, SWT.BORDER);
		GridData gd_statusBar = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gd_statusBar.widthHint = 638;
		gd_statusBar.heightHint = 34;
		statusBar.setLayoutData(gd_statusBar);

		progressBar = new ProgressBar(statusBar, SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
		progressBar.setMaximum(100);
	}
	
	
	public void updateProgressBar()
	{
	    new Thread(new Runnable()
	    {
	        private int                 progress    = 0;
	        private static final int    INCREMENT   = 10;

	        @Override
	        public void run()
	        {
	            while (!progressBar.isDisposed())
	            {
	                Display.getDefault().asyncExec(new Runnable()
	                {
	                    @Override
	                    public void run()
	                    {
	                        if (!progressBar.isDisposed())
	                            progressBar.setSelection((progress += INCREMENT) % (progressBar.getMaximum() + INCREMENT));
	                        System.out.println("[updateProgressBar] processing: " + INCREMENT);
	                    }
	                });

	                try
	                {
	                    Thread.sleep(1000);
	                }
	                catch (InterruptedException e)
	                {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }).start();
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

}

