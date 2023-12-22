package com.teamcenter.vinfast.general.create.mepart;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;

public class MEPart_Dialog extends TitleAreaDialog {
	private Composite area;
	public Composite container;
	public Button btnCreate;
	public Button ckbOpenOnCreate;
	private Label lblSubCategory;
	public Combo cbCategory;
	private Label lblType;
	public Combo cbType;

	public MEPart_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create ME Part");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);

		try {
			// init UI
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblType = new Label(container, SWT.NONE);
				lblType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblType.setText("Type: (*)");
				lblType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
				lblType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbType = new Combo(container, SWT.READ_ONLY);
				cbType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
				cbType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblSubCategory = new Label(container, SWT.NONE);
				lblSubCategory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblSubCategory.setText("Sub Category: (*)");
				lblSubCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
				lblSubCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbCategory = new Combo(container, SWT.READ_ONLY);
				cbCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
				cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 800);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("ME Part");
	}
}
