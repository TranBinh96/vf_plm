package com.teamcenter.vinfast.car.engineering.workflow;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class MPPartApproval_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	private Label lblProgram;
	public Combo cbProgram;
	private Label lblModule;
	public Combo cbModule;
	private Label label;
	public Browser brsALs;
	private TabFolder tabFolder;
	private TabItem tbtmNewItem;
	private TabItem tbtmNewItem_1;
	public Browser brsParts;
	private Text txtMessage;

	public MPPartApproval_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		area = (Composite) super.createDialogArea(parent);
		area.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginWidth = 5;
		try {
			container = new Composite(area, SWT.NONE);
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				txtMessage = new Text(container, SWT.BORDER);
				txtMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtMessage.setEditable(false);
				GridData gd_txtMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_txtMessage.heightHint = 65;
				txtMessage.setLayoutData(gd_txtMessage);
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
			}
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Program: (*)");
			}
			{
				cbProgram = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblModule = new Label(container, SWT.NONE);
				lblModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModule.setText("Module: (*)");
			}
			{
				cbModule = new Combo(container, SWT.READ_ONLY);
				cbModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("Parts List");
					{
						brsParts = new Browser(tabFolder, SWT.NONE);
						tbtmNewItem.setControl(brsParts);
					}
				}
				{
					tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem_1.setText("Assignment List");
					{
						brsALs = new Browser(tabFolder, SWT.NONE);
						tbtmNewItem_1.setControl(brsALs);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Trigger", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 350);
		super.configureShell(newShell);
		newShell.setText("MP Part Release");
	}

	public void setMessage(String value, Boolean error) {
		txtMessage.setForeground(SWTResourceManager.getColor(error ? SWT.COLOR_RED : SWT.COLOR_BLACK));
		txtMessage.setText(value);
	}
}
