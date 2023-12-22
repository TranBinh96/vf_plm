package com.teamcenter.vinfast.car.engineering.workflow;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Combo;

public class TechnicalDocProcessTrigger_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnValidate;
	public Button btnAccept;
	public Label lblMessage;
	private Label lblProgram;
	public Text txtProgram;
	private Label label;
	private Label lblModule;
	public Text txtModule;
	public TabFolder tabFolder;
	private TabItem tbtmNewItem;
	private TabItem tbtmNewItem_1;
	public Browser brwValidate;
	public Tree treeAssignList;
	private TabItem tbtmVfteList;
	public Table tblVFTE;
	private TableColumn tblclmnNewColumn;
	public Text txtDocType;
	private Label lblDocumentType;
	public Label lblSubModule;
	public Combo cbSubModule;
	public Button ckbRequiredManufacturing;
	public Combo cbShop;

	public TechnicalDocProcessTrigger_Dialog(Shell parentShell) {
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
				lblMessage.setText("Trigger Technical Document Process.");
				lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_label.widthHint = 234;
				label.setLayoutData(gd_label);
			}
			{
				lblDocumentType = new Label(container, SWT.NONE);
				lblDocumentType.setText("Document Type:");
				lblDocumentType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblDocumentType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				txtDocType = new Text(container, SWT.BORDER);
				txtDocType.setEnabled(false);
				txtDocType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtDocType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
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
				lblSubModule = new Label(container, SWT.NONE);
				lblSubModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSubModule.setText("Sub Module:");
				lblSubModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbSubModule = new Combo(container, SWT.READ_ONLY);
				cbSubModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				ckbRequiredManufacturing = new Button(container, SWT.CHECK);
				ckbRequiredManufacturing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				ckbRequiredManufacturing.setText("Required VF Manufacturing");
				ckbRequiredManufacturing.setSelection(true);
			}
			{
				cbShop = new Combo(container, SWT.READ_ONLY);
				cbShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("Validate");
					{
						brwValidate = new Browser(tabFolder, SWT.BORDER);
						tbtmNewItem.setControl(brwValidate);
					}
				}
				{
					tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem_1.setText("Assignment List");
					{
						treeAssignList = new Tree(tabFolder, SWT.BORDER);
						tbtmNewItem_1.setControl(treeAssignList);
					}
				}
				{
					tbtmVfteList = new TabItem(tabFolder, SWT.NONE);
					tbtmVfteList.setText("VFTE List");
					{
						tblVFTE = new Table(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
						tbtmVfteList.setControl(tblVFTE);
						tblVFTE.setHeaderVisible(true);
						tblVFTE.setLinesVisible(true);
						{
							tblclmnNewColumn = new TableColumn(tblVFTE, SWT.NONE);
							tblclmnNewColumn.setWidth(437);
							tblclmnNewColumn.setText("VFTE");
						}
					}
				}
			}

		} catch (Exception e) {

		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Submit", false);
		btnAccept.setText("Trigger");
		
		btnValidate = createButton(parent, IDialogConstants.CLOSE_ID, "Submit", false);
		btnValidate.setText("Validate");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(500, 500);
		super.configureShell(newShell);
		newShell.setText("Technical Document");
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
