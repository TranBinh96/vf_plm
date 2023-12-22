package com.teamcenter.vines.workflow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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

public class VESBPIApproval_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Label lblMessage;
	private Label lblProgram;
	public Text txtDoctype;
	private Label label;
	public Browser brwReviewer;
	private Label lblModelCode;
	public Text txtModelCode;

	public VESBPIApproval_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		area.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL);
				lblMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_lblMessage.widthHint = 333;
				gd_lblMessage.heightHint = 50;
				lblMessage.setLayoutData(gd_lblMessage);
				lblMessage.setText("Trigger BPI Spec Doc Process.");
				lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_label.widthHint = 234;
				label.setLayoutData(gd_label);
			}
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Document Type:");
			}
			{
				txtDoctype = new Text(container, SWT.BORDER);
				txtDoctype.setEnabled(false);
				txtDoctype.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtDoctype.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblModelCode = new Label(container, SWT.NONE);
				lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModelCode.setText("Model Code:");
				lblModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtModelCode = new Text(container, SWT.BORDER);
				txtModelCode.setEnabled(false);
				txtModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				brwReviewer = new Browser(container, SWT.BORDER);
				brwReviewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			}

		} catch (Exception e) {

		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Submit", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(500, 500);
		super.configureShell(newShell);
		newShell.setText("ESOM Process");
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError) {
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		} else {
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		}
	}
}
