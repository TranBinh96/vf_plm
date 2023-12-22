package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.List;

public class PreferenceAssistant_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Text txtALName;
	private Composite composite;
	public Text txtName;
	public Text txtDesc;
	public Text txtValue;
	public List lstPreference;
	public Button btnAccept;

	public PreferenceAssistant_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			txtALName = new Text(container, SWT.BORDER);
			GridData gd_txtALName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_txtALName.widthHint = 181;
			txtALName.setLayoutData(gd_txtALName);
			txtALName.setToolTipText("Assignment List Name");
			txtALName.setMessage("Preference Name");

			composite = new Composite(container, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setText("Name:");

			txtName = new Text(composite, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblNewLabel_1 = new Label(composite, SWT.NONE);
			lblNewLabel_1.setText("Description:");

			txtDesc = new Text(composite, SWT.BORDER | SWT.MULTI);
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesc.heightHint = 55;
			txtDesc.setLayoutData(gd_txtDesc);

			Label lblNewLabel_2 = new Label(composite, SWT.NONE);
			lblNewLabel_2.setText("Value:");

			txtValue = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
			txtValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			lstPreference = new List(container, SWT.BORDER);
			GridData gd_lstPreference = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
			gd_lstPreference.widthHint = 221;
			lstPreference.setLayoutData(gd_lstPreference);
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Preference Assistant");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(986, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}
}
