package com.teamcenter.vinfast.escooter.engineering.workflow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Tree;

public class EScooterSORProcessTrigger_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	private Label lblNewLabel;
	private Label lblProgram;
	public Combo cbProgram;
	private Label lblModule;
	public Text txtID;
	public Combo cbModule;
	private Label label;
	public Label lblMessage;
	private Label lblWorkflow;
	public Text txtWorkflow;
	public TabFolder tabFolder;
	private TabItem tbtmValidate;
	public Browser brwValidate;

	public Button btnAccept;
	public Button btnValidate;
	private TabItem tbtmBomInfo;
	private Composite composite_1;
	public Table tblBom;
	private CheckboxTableViewer checkboxTableViewer_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnPosid_1;
	private TableColumn tblclmnDonorVehicle_1;
	private TableColumn tblclmnLevel_1;
	private TableColumn tblclmnSteering_1;
	private TableColumn tblclmnQuantity_1;
	private TableColumn tblclmnPurchaseLevel_1;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnPartName_1;
	public Button btnAddBomInfo;
	public Button btnRemoveBomInfo;
	public Button btnUpdateBomInfo;
	public Button ckbCheckAll;
	private TabItem tbtmNewItem;
	public Tree treeAssignList;
	public Label lblAllowedObjectType;
	private TableColumn tblclmnDonorVehicle;

	public EScooterSORProcessTrigger_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
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
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
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
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("SOR Doc: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setEnabled(false);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
				{
					tbtmBomInfo = new TabItem(tabFolder, SWT.NONE);
					tbtmBomInfo.setText("Bom Info");
					{
						composite_1 = new Composite(tabFolder, SWT.NONE);
						tbtmBomInfo.setControl(composite_1);
						composite_1.setLayout(new GridLayout(4, false));
						new Label(composite_1, SWT.NONE);
						{
							lblAllowedObjectType = new Label(composite_1, SWT.NONE);
							lblAllowedObjectType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
							lblAllowedObjectType.setText("Allowed Object Type:");
							lblAllowedObjectType.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
							lblAllowedObjectType.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
						}
						{
							ckbCheckAll = new Button(composite_1, SWT.CHECK);
						}
						{
							btnAddBomInfo = new Button(composite_1, SWT.NONE);
							GridData gd_btnAddBomInfo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
							gd_btnAddBomInfo.widthHint = 80;
							btnAddBomInfo.setLayoutData(gd_btnAddBomInfo);
							btnAddBomInfo.setText("Add");
						}
						{
							btnRemoveBomInfo = new Button(composite_1, SWT.NONE);
							GridData gd_btnRemoveBomInfo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
							gd_btnRemoveBomInfo.widthHint = 80;
							btnRemoveBomInfo.setLayoutData(gd_btnRemoveBomInfo);
							btnRemoveBomInfo.setText("Remove");
						}
						{
							btnUpdateBomInfo = new Button(composite_1, SWT.NONE);
							GridData gd_btnUpdateBomInfo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
							gd_btnUpdateBomInfo.widthHint = 80;
							btnUpdateBomInfo.setLayoutData(gd_btnUpdateBomInfo);
							btnUpdateBomInfo.setText("Update");
						}
						{
							checkboxTableViewer_1 = CheckboxTableViewer.newCheckList(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.MULTI);
							checkboxTableViewer_1.setAllGrayed(false);
							checkboxTableViewer_1.setAllChecked(false);
							tblBom = checkboxTableViewer_1.getTable();
							tblBom.setTouchEnabled(true);
							tblBom.setLinesVisible(true);
							tblBom.setHeaderVisible(true);
							tblBom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
							{
								tblclmnNewColumn_2 = new TableColumn(tblBom, SWT.NONE);
								tblclmnNewColumn_2.setWidth(30);
							}
							{
								tblclmnPosid_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnPosid_1.setWidth(69);
								tblclmnPosid_1.setText("POSID");
							}
							{
								tblclmnNewColumn_3 = new TableColumn(tblBom, SWT.NONE);
								tblclmnNewColumn_3.setWidth(100);
								tblclmnNewColumn_3.setText("Part No");
							}
							{
								tblclmnPartName_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnPartName_1.setWidth(100);
								tblclmnPartName_1.setText("Part Name");
							}
							{
								tblclmnDonorVehicle_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnDonorVehicle_1.setWidth(55);
								tblclmnDonorVehicle_1.setText("UOM");
							}
							{
								tblclmnDonorVehicle = new TableColumn(tblBom, SWT.NONE);
								tblclmnDonorVehicle.setWidth(65);
								tblclmnDonorVehicle.setText("Make Buy");
							}
							{
								tblclmnPurchaseLevel_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnPurchaseLevel_1.setWidth(80);
								tblclmnPurchaseLevel_1.setText("Purchase Level");
							}
							{
								tblclmnSteering_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnSteering_1.setWidth(70);
								tblclmnSteering_1.setText("Module Group");
							}
							{
								tblclmnLevel_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnLevel_1.setWidth(40);
								tblclmnLevel_1.setText("Main Module");
							}
							{
								tblclmnQuantity_1 = new TableColumn(tblBom, SWT.NONE);
								tblclmnQuantity_1.setWidth(70);
								tblclmnQuantity_1.setText("Module Name");
							}
						}
					}
				}
				{
					tbtmValidate = new TabItem(tabFolder, SWT.NONE);
					tbtmValidate.setText("Validate");
					{
						brwValidate = new Browser(tabFolder, SWT.NONE);
						tbtmValidate.setControl(brwValidate);
					}
				}
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("Assignment List");
					{
						treeAssignList = new Tree(tabFolder, SWT.BORDER);
						tbtmNewItem.setControl(treeAssignList);
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
		btnValidate = createButton(parent, IDialogConstants.CLOSE_ID, "Verify", false);
		btnValidate.setText("Validate");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 800);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 350);
		super.configureShell(newShell);
		newShell.setText("Escooter SOR Process");
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError)
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}
}
