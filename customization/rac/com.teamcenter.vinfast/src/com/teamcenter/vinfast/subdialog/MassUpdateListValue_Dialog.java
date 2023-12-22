package com.teamcenter.vinfast.subdialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class MassUpdateListValue_Dialog extends Dialog {
	public Button btnAccept;
	public Label lblValue;
	public Combo cbValue;
	private Label lblNewLabel;

	public String valueSelected = "";
	public List lstValue;
	private Button btnRemove;
	private Button btnAdd;

	public MassUpdateListValue_Dialog(Shell parent) {
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

		lstValue = new List(container, SWT.BORDER);
		lstValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnRemove = new Button(container, SWT.NONE);
		btnRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
		btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				removeValue();
			}
		});
		new Label(container, SWT.NONE);

		cbValue = new Combo(container, SWT.READ_ONLY);
		cbValue.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnAdd = new Button(container, SWT.NONE);
		btnAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
		btnAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				addValue();
			}
		});

		return container;
	}

	private void addValue() {
		String value = cbValue.getText();
		if (!value.isEmpty()) {
			if (checkExistInList(lstValue.getItems(), value))
				lstValue.add(value);
		}
	}

	private void removeValue() {
		int indexValue = lstValue.getSelectionIndex();
		if (indexValue >= 0)
			lstValue.remove(indexValue);
	}

	private boolean checkExistInList(String[] origin, String target) {
		if (origin == null || origin.length == 0)
			return true;

		for (String item : origin) {
			if (item.compareTo(target) == 0)
				return false;
		}
		return true;
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
