package com.teamcenter.vinfast.admin;

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

import com.teamcenter.rac.util.MessageBox;

public class CostFormMigrate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Browser browser;
	private Label lblNewLabel;
	public Text txtObjectType;

	public CostFormMigrate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);

		try {
			setMessage("Define business object create information");
			setTitle("Cost Form Migrate");

			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("Object Type: (*)");
			}
			{
				txtObjectType = new Text(container, SWT.BORDER);
				txtObjectType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				browser = new Browser(container, SWT.NONE);
				browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			}
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Cost Form Migrate");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Migrate", true);
	}
}
