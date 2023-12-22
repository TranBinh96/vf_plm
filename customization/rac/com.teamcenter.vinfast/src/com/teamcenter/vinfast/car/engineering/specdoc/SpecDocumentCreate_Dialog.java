package com.teamcenter.vinfast.car.engineering.specdoc;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SpecDocumentCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	public Composite container;
	public Combo cbPrefixName;
	public Button btnCreate;
	public Button ckbOpenOnCreate;

	public SpecDocumentCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Create Spec Document");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblPrefixName = new Label(container, SWT.NONE);
			lblPrefixName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPrefixName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPrefixName.setText("Prefix Name: (*)");

			cbPrefixName = new Combo(container, SWT.READ_ONLY);
			cbPrefixName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Spec Document");
	}
}
