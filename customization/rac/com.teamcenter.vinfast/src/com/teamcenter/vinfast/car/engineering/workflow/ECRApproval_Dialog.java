package com.teamcenter.vinfast.car.engineering.workflow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class ECRApproval_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	private Label lblNewLabel;
	private Label lblProgram;
	public Combo cbProgram;
	private Label lblModule;
	public Label lblCombine;
	public Combo cbCombine;
	public Text txtID;
	public Combo cbModule;
	private Label label;
	public Label lblMessage;
	private Label lblWorkflow;
	public Text txtWorkflow;
	public TabFolder tabFolder;
	private TabItem tbtmNewItem;
	public Browser brwValidate;
	public TabItem tbtmCop;
	public Browser brwCOP;

	public Button btnAccept;
	public Button btnValidate;

	public ECRApproval_Dialog(Shell parentShell) throws TCException {
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
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL | SWT.V_SCROLL);
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
				gd_lblMessage.heightHint = 65;
				lblMessage.setLayoutData(gd_lblMessage);
				lblMessage.setText("Trigger ECR/ECN Process");
				lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				lblMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
			}
			{
				lblWorkflow = new Label(container, SWT.NONE);
				lblWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblWorkflow.setText("Workflow:");
				lblWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtWorkflow = new Text(container, SWT.BORDER);
				txtWorkflow.setEnabled(false);
				txtWorkflow.setEditable(false);
				txtWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
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
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
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
				lblCombine = new Label(container, SWT.NONE);
				lblCombine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCombine.setText("Combine with:");
			}
			{
				cbCombine = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbCombine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ECR:");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setEnabled(false);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("Validate");
					{
						brwValidate = new Browser(tabFolder, SWT.NONE);
						tbtmNewItem.setControl(brwValidate);
					}
				}
				{
					tbtmCop = new TabItem(tabFolder, SWT.NONE);
					tbtmCop.setText("COP");
					{
						brwCOP = new Browser(tabFolder, SWT.NONE);
						tbtmCop.setControl(brwCOP);
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
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Trigger", false);
		btnValidate = createButton(parent, IDialogConstants.CLOSE_ID, "Validate", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(700, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 350);
		super.configureShell(newShell);
		newShell.setText("ECR/ECN Process");
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError)
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}
}
