package com.teamcenter.vinfast.car.manuf.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class UpdateCostDialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Label lblReason;
	public Text txtReason;
	public Browser brwValidate;

	public UpdateCostDialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Input reason to submit request");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblReason = new Label(container, SWT.NONE);
			lblReason.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
//			lblReason.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			lblReason.setText("Reason: (*)");
			lblReason.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtReason = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			txtReason.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDesignPart = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesignPart.heightHint = 86;
			txtReason.setLayoutData(gd_txtDesignPart);

			brwValidate = new Browser(container, SWT.NONE);
			brwValidate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Request", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Request Update MFG Cost");
	}
}
