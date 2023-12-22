package com.teamcenter.vinfast.eecomponent.create;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class EECompDocumentCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Text txtID;
	public Text txtName;
	public Text txtDescription;
	public Label lblModelCode;
	public Combo cbProgramName;
	public Combo cbDocumentType;
	public Button btnCreate;
	public Button ckbOpenOnCreate;

	public EECompDocumentCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Create EE Component Document");
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblId = new Label(container, SWT.NONE);
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID: (*)");
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setText("Description:");

			txtDescription = new Text(container, SWT.BORDER);
			txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

			Label lblPrefixName = new Label(container, SWT.NONE);
			lblPrefixName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPrefixName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPrefixName.setText("Program Name: (*)");

			cbProgramName = new Combo(container, SWT.READ_ONLY);
			cbProgramName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

			lblModelCode = new Label(container, SWT.NONE);
			lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModelCode.setText("Document Type: (*)");

			cbDocumentType = new Combo(container, SWT.READ_ONLY);
			cbDocumentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
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
		return new Point(480, 430);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create EE Component Document");
	}
}
