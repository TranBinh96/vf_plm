package com.vinfast.bom.familygroup;


import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class ValidateFGReportDialog extends TitleAreaDialog {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private Combo cbTopPart;
	private Combo cbPrg;
	private Combo cbVariant;
	private Combo cbRevisionRule;
	private Button btnOk;
	private Composite container;
	private Composite container2;
	private Composite area;
	private ProgressBar  progressBar;
	private static final Logger logger = Logger.getLogger(ValidateFGReportDialog.class);

	public ValidateFGReportDialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	public Button getBtnOk() {
		return btnOk;
	}

	public void setBtnOk(Button btnSave) {
		this.btnOk = btnSave;
	}


	public Combo getCbTopPart() {
		return cbTopPart;
	}


	public Combo getCbPrg() {
		return cbPrg;
	}



	public Combo getCbVariant() {
		return cbVariant;
	}
	
	public Combo getCbRevisionRule() {
		return cbRevisionRule;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.BORDER);
			GridLayout glContainer = new GridLayout(1, true);
			glContainer.marginRight = 10;
			glContainer.marginLeft = 10;
			container.setLayout(glContainer);
			GridData gdContainer = new GridData(GridData.FILL_BOTH);
			gdContainer.heightHint = 200;
			gdContainer.widthHint = 500;
			container.setLayoutData(gdContainer);
			
			Label lblPrg = new Label(container, SWT.NONE);
			lblPrg.setText("Program: ");
			cbPrg = new Combo(container, SWT.READ_ONLY);
			cbPrg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			Label lblTopPart = new Label(container, SWT.NONE);
			lblTopPart.setText("Top Part: ");
			cbTopPart = new Combo(container, SWT.READ_ONLY);
			cbTopPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label lblVariant = new Label(container, SWT.NONE);
			lblVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVariant.setText("Variant: ");
			cbVariant = new Combo(container, SWT.READ_ONLY);
			cbVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			Label lblRevisionRule = new Label(container, SWT.NONE);
			lblRevisionRule.setText("Revision Rule: ");
			cbRevisionRule = new Combo(container, SWT.READ_ONLY);
			cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	private void createProgressBar(Composite shell) {
		Composite statusBar = new Composite(area, SWT.BORDER);
	    statusBar.setLayout(new GridLayout(2, false));
	    statusBar.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

	    progressBar = new ProgressBar(statusBar, SWT.SMOOTH);
	    progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    progressBar.setMaximum(100);

	    Label status = new Label(statusBar, SWT.NONE);
	    status.setText("Some status message");
	    status.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		btnOk = createButton(container, IDialogConstants.OK_ID, "Validate", true);
		btnOk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Validate Family Group");
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		//super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(602, 410);
	}
	
	public void showValidationResult(String htmlTxt) {
		container2 = new Composite(area, SWT.BORDER);
		GridLayout glContainer = new GridLayout(2, true);
		glContainer.marginRight = 10;
		glContainer.marginLeft = 10;
		container2.setLayout(glContainer);
		GridData gdContainer = new GridData(GridData.FILL_BOTH);
		gdContainer.widthHint = 680;
		container2.setLayoutData(gdContainer);
		
		Browser browser = new Browser(container2, SWT.NONE);
		browser.setText(htmlTxt);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}
	
	public void showProgressBar()
	{
	    Composite statusBar = new Composite(area, SWT.BORDER);
	    statusBar.setLayout(new GridLayout(2, false));
	    statusBar.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

	    progressBar = new ProgressBar(statusBar, SWT.SMOOTH);
	    progressBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
	    progressBar.setMaximum(100);

	    Label status = new Label(statusBar, SWT.NONE);
	    status.setText("Some status message");
	    status.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
	    this.getShell().setSize(602, 411);
	}
	
}
