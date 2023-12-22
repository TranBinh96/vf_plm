package com.teamcenter.vinfast.escooter.engineering.workflow;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;

public class EScooterECNProcessTrigger_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	private Label lblNewLabel;
	private Label lblProgram;
	public Combo cbProgram;
	private Label lblWorkflow;
	public Combo cbWorkflow;
	private Label lblModule;
	public Combo cbModule;
	public Label lblCombine;
	public Combo cbCombine;
	private Label lblCostImpact;
	public Combo cbCostImpact;
	public Text txtID;
	private Label lblMessage;
	private Label label;
	private TabFolder tabFolder;
	private TabItem tbtmAssignmentList;
	public Tree treeAssignList;

	public EScooterECNProcessTrigger_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		gridLayout.verticalSpacing = 2;
		area.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		try {
			container = new Composite(area, SWT.NONE);
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL);
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
				gd_lblMessage.heightHint = 65;
				lblMessage.setLayoutData(gd_lblMessage);
				lblMessage.setText("Trigger ECN Process");
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
				lblWorkflow.setText("Workflow: (*)");
			}
			{
				cbWorkflow = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				lblProgram = new Label(container, SWT.NONE);
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
				lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModule.setText("Module: (*)");
			}
			{
				cbModule = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblCombine = new Label(container, SWT.NONE);
				lblCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCombine.setText("Combine with:");
			}
			{
				cbCombine = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbCombine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblCostImpact = new Label(container, SWT.NONE);
				lblCostImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCostImpact.setText("Cost Impact: (*)");
			}
			{
				cbCostImpact = new Combo(container, SWT.READ_ONLY);
				cbCostImpact.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCostImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ECN:");
			}
			{
				txtID = new Text(container, SWT.BORDER | SWT.READ_ONLY);
				txtID.setEnabled(false);
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
				{
					tbtmAssignmentList = new TabItem(tabFolder, SWT.NONE);
					tbtmAssignmentList.setText("Assignment List");
					{
						treeAssignList = new Tree(tabFolder, SWT.BORDER);
						tbtmAssignmentList.setControl(treeAssignList);
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
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Submit", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 800);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("EScooter ECN Process");
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError)
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}
}
