package com.teamcenter.vinfast.car.engineering.workflow;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Tree;

public class CMFSProcessTrigger_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	private Label lblProgram;
	public Text txtProgram;
	private Label lblMessage;
	private Label label;
	public TabFolder tabFolder;
	private TabItem tbtmNewItem;
	private TabItem tbtmNewItem_1;
	public Browser brwValidate;
	public Tree treeAssignList;
	private Label lblModule;
	public Text txtModule;

	public CMFSProcessTrigger_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(2, false);
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		{
			lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL);
			GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
			gd_lblMessage.heightHint = 50;
			lblMessage.setLayoutData(gd_lblMessage);
			lblMessage.setText("Trigger ESOM Approval Process.");
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			lblMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}
		{
			label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		}
		{
			lblProgram = new Label(container, SWT.NONE);
			lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblProgram.setText("Program:");
		}
		{
			txtProgram = new Text(container, SWT.BORDER);
			txtProgram.setEnabled(false);
			txtProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		{
			lblModule = new Label(container, SWT.NONE);
			lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModule.setText("Module:");
			lblModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}
		{
			txtModule = new Text(container, SWT.BORDER);
			txtModule.setEnabled(false);
			txtModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		{
			tabFolder = new TabFolder(container, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
			{
				tbtmNewItem = new TabItem(tabFolder, 0);
				tbtmNewItem.setText("Validate");
				{
					brwValidate = new Browser(tabFolder, SWT.BORDER);
					tbtmNewItem.setControl(brwValidate);
				}
			}
			{
				tbtmNewItem_1 = new TabItem(tabFolder, 0);
				tbtmNewItem_1.setText("Assignment List");
				{
					treeAssignList = new Tree(tabFolder, SWT.BORDER);
					tbtmNewItem_1.setControl(treeAssignList);
				}
			}
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Submit", false);
		btnCreate.setText("Trigger");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(450, 400);
		super.configureShell(newShell);
		newShell.setText("CMF Process");
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
