package com.teamcenter.vinfast.subdialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

public class MassUpdateDateTimeValue_Dialog extends Dialog {
	public Button btnAccept;
	public Label lblValue;
	private Label lblNewLabel;

	public String valueSelected = "";
	public DateTime datValue;
	public Button ckbCheck;
	public boolean valueCheck = true;

	public MassUpdateDateTimeValue_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(3, false);
		container.setLayout(gl_shell);

		lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		new Label(container, SWT.NONE);

		lblValue = new Label(container, SWT.NONE);
		GridData gd_lblValue = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gd_lblValue.widthHint = 118;
		lblValue.setLayoutData(gd_lblValue);
		lblValue.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblValue.setText("Change Type: (*)");

		datValue = new DateTime(container, SWT.BORDER);
		datValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		ckbCheck = new Button(container, SWT.CHECK);
		ckbCheck.setSelection(true);
		ckbCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				valueCheck = ckbCheck.getSelection();
				datValue.setVisible(ckbCheck.getSelection());
			}
		});

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
		return new Point(460, 230);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
