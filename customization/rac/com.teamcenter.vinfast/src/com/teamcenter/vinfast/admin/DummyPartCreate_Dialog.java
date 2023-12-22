package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Text;

public class DummyPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	public Browser brwReport;
	public Text txtProperty;
	private Label lblNewLabel_1;
	public Text txtValue;
	private Label lblPartType;
	public Text txtItemType;
	private Label lblRevType;
	public Text txtRevType;
	
	public DummyPartCreate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Dummy Part Create");
		setMessage("Define business object create information");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			lblPartType = new Label(container, SWT.NONE);
			lblPartType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartType.setText("Item Type:");
			
			txtItemType = new Text(container, SWT.BORDER);
			txtItemType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblRevType = new Label(container, SWT.NONE);
			lblRevType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblRevType.setText("Rev Type:");
			
			txtRevType = new Text(container, SWT.BORDER);
			txtRevType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Attribute:");
			
			txtProperty = new Text(container, SWT.BORDER);
			txtProperty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblNewLabel_1 = new Label(container, SWT.NONE);
			lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_1.setText("Value:");
			
			txtValue = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtValue.heightHint = 104;
			txtValue.setLayoutData(gd_txtValue);
			
			brwReport = new Browser(container, SWT.NONE);
			GridData gd_brwReport = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd_brwReport.widthHint = 410;
			brwReport.setLayoutData(gd_brwReport);
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Import ALs for Workprocess");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(470, 546);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}
}
