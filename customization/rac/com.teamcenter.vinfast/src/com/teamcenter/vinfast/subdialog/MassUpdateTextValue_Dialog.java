package com.teamcenter.vinfast.subdialog;

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
import org.eclipse.wb.swt.SWTResourceManager;

public class MassUpdateTextValue_Dialog extends Dialog {
	public Button btnAccept;
	public Label lblValue;
	private Label lblNewLabel;
	public String valueSelected = "";
	public Text txtValue;

	public MassUpdateTextValue_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(2, false);
		container.setLayout(gl_shell);

		lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		lblValue = new Label(container, SWT.NONE);
		GridData gd_lblValue = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblValue.widthHint = 118;
		lblValue.setLayoutData(gd_lblValue);
		lblValue.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblValue.setText("Change Type: (*)");

		txtValue = new Text(container, SWT.BORDER);
		txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Mass Update Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(460, 180);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
