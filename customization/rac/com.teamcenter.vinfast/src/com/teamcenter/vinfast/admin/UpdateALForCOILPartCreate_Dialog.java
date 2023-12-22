package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.List;

public class UpdateALForCOILPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	public Combo cbWorkprocess;
	private Label lblNewLabel_1;
	public Text txtNewValue;
	public List lstValue;
	
	public UpdateALForCOILPartCreate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Import ALs for Workprocess");
		setMessage("Define business object create information");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Preference:");
			
			cbWorkprocess = new Combo(container, SWT.NONE | SWT.READ_ONLY);
			cbWorkprocess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblNewLabel_1 = new Label(container, SWT.NONE);
			lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblNewLabel_1.setText("New value:");
			
			txtNewValue = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtNewValue = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_txtNewValue.heightHint = 62;
			txtNewValue.setLayoutData(gd_txtNewValue);
			
			lstValue = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			lstValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
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
