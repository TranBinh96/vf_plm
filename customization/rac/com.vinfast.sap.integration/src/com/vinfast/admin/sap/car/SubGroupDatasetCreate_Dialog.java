package com.vinfast.admin.sap.car;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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

import com.teamcenter.rac.kernel.TCSession;

public class SubGroupDatasetCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	private Label lblEcr;
	private Label lblShop;
	public Text txtShop;
	public List lstImpactedItems;
	public Browser browser;
	
	public SubGroupDatasetCreate_Dialog(Shell parentShell, TCSession _session) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblShop = new Label(container, SWT.NONE);
				lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblShop.setText("Shop:");
			}
			{
				txtShop = new Text(container, SWT.BORDER);
				txtShop.setEnabled(false);
				txtShop.setEditable(false);
				txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblEcr = new Label(container, SWT.NONE);
				lblEcr.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblEcr.setText("Sub-Group:");
			}
			{
				lstImpactedItems = new List(container, SWT.BORDER);
				lstImpactedItems.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				browser = new Browser(container, SWT.NONE);
				GridData gd_browser = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
				gd_browser.widthHint = 205;
				browser.setLayoutData(gd_browser);
			}
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create Sub-Group dataset");
	}
}
