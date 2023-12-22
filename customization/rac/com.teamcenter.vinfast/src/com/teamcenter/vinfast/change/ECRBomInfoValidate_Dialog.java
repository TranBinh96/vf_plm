package com.teamcenter.vinfast.change;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

public class ECRBomInfoValidate_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	private Label lblEcr;
	public Text txtECR;
	public Browser browser;
	public Button btnSearchECR;
	public Button btnCancel;
	private Label lblParentBomline;
	public Button btnAddParent;
	public List lstParentBomline;
	public Button btnRemoveParent;
	
	public ECRBomInfoValidate_Dialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblEcr = new Label(container, SWT.NONE);
				lblEcr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblEcr.setText("ECR: ");
			}
			{
				txtECR = new Text(container, SWT.BORDER);
				txtECR.setEnabled(false);
				txtECR.setEditable(false);
				txtECR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSearchECR = new Button(container, SWT.NONE);
				btnSearchECR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/ecr_item.png"));
				btnSearchECR.setToolTipText("");
				GridData gd_btnSearchECR = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
				gd_btnSearchECR.widthHint = 31;
				btnSearchECR.setLayoutData(gd_btnSearchECR);
			}
			{
				lblParentBomline = new Label(container, SWT.NONE);
				lblParentBomline.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblParentBomline.setText("Parent Bomline:");
			}
			{
				lstParentBomline = new List(container, SWT.BORDER);
				GridData gd_lstParentBomline = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2);
				gd_lstParentBomline.heightHint = 51;
				lstParentBomline.setLayoutData(gd_lstParentBomline);
			}
			{
				btnAddParent = new Button(container, SWT.NONE);
				GridData gd_btnAddParent = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
				gd_btnAddParent.widthHint = 32;
				btnAddParent.setLayoutData(gd_btnAddParent);
				btnAddParent.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/select_editable_target_16.png"));
			}
			new Label(container, SWT.NONE);
			{
				btnRemoveParent = new Button(container, SWT.NONE);
				btnRemoveParent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				btnRemoveParent.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			{
				browser = new Browser(container, SWT.NONE);
				browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			}
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Validate", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 400);
		super.configureShell(newShell);
		newShell.setText("ECR Bom Information Validate");
	}
}
