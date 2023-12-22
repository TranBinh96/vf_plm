package com.teamcenter.vinfast.car.engineering.update;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.browser.Browser;

public class DesignConverter_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnAccept;
	private Label lblNewLabel;
	public Button btnChange;
	private Label lblNewLabel_1;
	public Text txtFrom;
	public Text txtTo;
	public Browser browser;
	public Text txtPart;
	private Label lblNewLabel_2;

	public DesignConverter_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 5;
		gridLayout.marginWidth = 5;
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel_2 = new Label(container, SWT.NONE);
				lblNewLabel_2.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
				lblNewLabel_2.setText("Part");
			}
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			{
				txtPart = new Text(container, SWT.BORDER);
				txtPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtPart.setEditable(false);
				txtPart.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
				txtPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("From");
			}
			new Label(container, SWT.NONE);
			{
				lblNewLabel_1 = new Label(container, SWT.NONE);
				lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
				lblNewLabel_1.setText("To");
			}
			{
				txtFrom = new Text(container, SWT.BORDER);
				txtFrom.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
				txtFrom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtFrom.setEditable(false);
				txtFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnChange = new Button(container, SWT.NONE);
				GridData gd_btnChange = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_btnChange.heightHint = 30;
				btnChange.setLayoutData(gd_btnChange);
				btnChange.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
				btnChange.setText("Switch");
			}
			{
				txtTo = new Text(container, SWT.BORDER);
				txtTo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
				txtTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtTo.setEditable(false);
				txtTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				browser = new Browser(container, SWT.NONE);
				browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			}

		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 430);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(400, 400);
		super.configureShell(newShell);
		newShell.setText("Design Converter");
	}
}
