package com.teamcenter.vinfast.escooter.sorprocess.view;

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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class NewSORProcessDialog extends Dialog {
	private Composite area;
	private Composite container;

	public Text txtSOR = null;
	public Button btnStart = null;
	public Combo cbProgram = null;
	public Text txtWorkflow = null;
	public Combo cbModule = null;
	public Button btnAddSOR = null;
	public Browser brwValidate = null;
	public Tree treeAssignList = null;
	private boolean isSystemEvent = false;

	private Label lblMessage;
	private Label label;
	private Label lblWorkflow;
	private Label lblProgram;
	private Label lblModule;
	private Label lblNewLabel;
	private TabFolder tabFolder;
	private TabItem tbtmNewItem;
	private TabItem tbtmAL;

	public NewSORProcessDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
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
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL | SWT.V_SCROLL);
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
				gd_lblMessage.heightHint = 65;
				lblMessage.setLayoutData(gd_lblMessage);
				lblMessage.setText("Trigger SOR Process");
				lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				lblMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
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
				txtWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("SOR Doc: (*)");
			}
			{
				txtSOR = new Text(container, SWT.BORDER);
				txtSOR.setEnabled(false);
				txtSOR.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtSOR.setEditable(false);
				txtSOR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnAddSOR = new Button(container, SWT.NONE);
				btnAddSOR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("Validate");
					{
						brwValidate = new Browser(tabFolder, SWT.NONE);
						tbtmNewItem.setControl(brwValidate);
					}
				}
				{
					tbtmAL = new TabItem(tabFolder, SWT.NONE);
					tbtmAL.setText("Assignment List");
					{
						treeAssignList = new Tree(tabFolder, SWT.BORDER);
						tbtmAL.setControl(treeAssignList);
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
		btnStart = createButton(parent, IDialogConstants.CLOSE_ID, "Trigger", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 800);
	}

	public Text getTxtSOR() {
		return txtSOR;
	}

	public void setTxtSOR(Text text) {
		this.txtSOR = text;
	}

	public Button getBtnStart() {
		return btnStart;
	}

	public void setBtnStart(Button btnStart) {
		this.btnStart = btnStart;
	}

	public Combo getCbPrg() {
		return cbProgram;
	}

	public void setCbPrg(Combo cbPrg) {
		this.cbProgram = cbPrg;
	}

	public Text getTxtWf() {
		return txtWorkflow;
	}

	public void setTxtWf(Text cbWf) {
		this.txtWorkflow = cbWf;
	}

	public Combo getCbModule() {
		return cbModule;
	}

	public void setCbModule(Combo cbModule) {
		this.cbModule = cbModule;
	}

	public Button getBtnAddSOR() {
		return btnAddSOR;
	}

	public void setBtnAddSOR(Button btnAddSOR) {
		this.btnAddSOR = btnAddSOR;
	}

	public void resetUI() {
		this.isSystemEvent = true;
		this.txtWorkflow.setText("");
		this.cbModule.removeAll();
		this.treeAssignList.removeAll();
		this.isSystemEvent = false;
	}

	public boolean isSystemEvent() {
		return isSystemEvent;
	}

	public void setSystemEvent(boolean isSystemEvent) {
		this.isSystemEvent = isSystemEvent;
	}

	public Browser getBrwValidateRes() {
		return brwValidate;
	}

	public void setBrwValidateRes(Browser brwValidateRes) {
		this.brwValidate = brwValidateRes;
	}

	public Tree getTreeAssignList() {
		return treeAssignList;
	}

	public void setTreeAssignList(Tree treeAssignList) {
		this.treeAssignList = treeAssignList;
	}

	public Composite getContainer() {
		return container;
	}

	public void setContainer(Composite container) {
		this.container = container;
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError)
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}
}
