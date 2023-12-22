package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class ALManagement_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnReplace;
	public Button btnRemove;
	public Button btnExcel;
	public Button btnExportAL;
	public Text txtUserID;
	public Table tblAssigner;
	public Combo cbWorkflow;
	public Button btnSearch;
	public Text txtALName;
	public Button ckbCheckAll;
	
	private CheckboxTableViewer checkboxTableViewer;
	private TableColumn tblclmnNewColumn;
	private TableViewerColumn tableViewerColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableViewerColumn tableViewerColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableViewerColumn tableViewerColumn_2;
	private TableColumn tblclmnNewColumn_4;
	private TableViewerColumn tableViewerColumn_4;
	private TableColumn tblclmnNewColumn_5;
	private TableViewerColumn tableViewerColumn_5;
	public TabItem tbtmAllGate;
	public TabItem tbtmEachGate;
	private Composite composite_1;
	public Combo cbWorkflow_EachGate;
	public Text txtUserID_EachGate;
	public Button btnSearch_EachGate;
	public Text txtALName_EachGate;
	private Label lblNewLabel_1;
	private Label lblAlName_1;
	private Label lblUserId_1;
	public Combo cbTask_EachGate;
	private Label lblAlName_2;
	public Button btnSearch_OldMember;
	public Label lblTotalResult;
	public TabFolder tabFolder;
	
	public ALManagement_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			tabFolder = new TabFolder(container, SWT.NONE);
			GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
			gd_tabFolder.heightHint = 61;
			tabFolder.setLayoutData(gd_tabFolder);
			
			tbtmAllGate = new TabItem(tabFolder, SWT.NONE);
			tbtmAllGate.setText("All Gate");
			
			Composite composite = new Composite(tabFolder, SWT.NONE);
			tbtmAllGate.setControl(composite);
			composite.setLayout(null);
			
			Label lblUserId = new Label(composite, SWT.NONE);
			lblUserId.setText("User ID");
			lblUserId.setBounds(10, 6, 55, 15);
			
			txtUserID = new Text(composite, SWT.BORDER);
			txtUserID.setToolTipText("User ID");
			txtUserID.setBounds(10, 27, 100, 23);
			
			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setBounds(116, 6, 55, 15);
			lblNewLabel.setText("Workflow");
			
			cbWorkflow = new Combo(composite, SWT.NONE);
			cbWorkflow.setToolTipText("Workflow");
			cbWorkflow.setBounds(116, 26, 200, 23);
			
			Label lblAlName = new Label(composite, SWT.NONE);
			lblAlName.setText("AL Name");
			lblAlName.setBounds(322, 6, 55, 15);
			
			txtALName = new Text(composite, SWT.BORDER);
			txtALName.setToolTipText("Assignment List Name");
			txtALName.setBounds(322, 27, 140, 23);
			
			btnSearch = new Button(composite, SWT.NONE);
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnSearch.setBounds(468, 25, 25, 25);
			
			tbtmEachGate = new TabItem(tabFolder, SWT.NONE);
			tbtmEachGate.setText("Each Gate");
			
			composite_1 = new Composite(tabFolder, SWT.NONE);
			composite_1.setLayout(null);
			tbtmEachGate.setControl(composite_1);
			
			lblUserId_1 = new Label(composite_1, SWT.NONE);
			lblUserId_1.setText("User ID");
			lblUserId_1.setBounds(10, 6, 55, 15);
			
			txtUserID_EachGate = new Text(composite_1, SWT.BORDER);
			txtUserID_EachGate.setEditable(false);
			txtUserID_EachGate.setToolTipText("User ID");
			txtUserID_EachGate.setBounds(10, 27, 200, 23);
			
			btnSearch_OldMember = new Button(composite_1, SWT.NONE);
			btnSearch_OldMember.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/vendor_16.png"));
			btnSearch_OldMember.setBounds(216, 25, 25, 25);
			
			lblNewLabel_1 = new Label(composite_1, SWT.NONE);
			lblNewLabel_1.setText("Workflow");
			lblNewLabel_1.setBounds(247, 6, 55, 15);
			
			cbWorkflow_EachGate = new Combo(composite_1, SWT.NONE);
			cbWorkflow_EachGate.setToolTipText("Workflow");
			cbWorkflow_EachGate.setBounds(247, 27, 200, 23);
			
			lblAlName_1 = new Label(composite_1, SWT.NONE);
			lblAlName_1.setText("AL Name");
			lblAlName_1.setBounds(453, 6, 55, 15);
			
			txtALName_EachGate = new Text(composite_1, SWT.BORDER);
			txtALName_EachGate.setToolTipText("Assignment List Name");
			txtALName_EachGate.setBounds(453, 27, 140, 23);
			
			lblAlName_2 = new Label(composite_1, SWT.NONE);
			lblAlName_2.setText("Task");
			lblAlName_2.setBounds(599, 6, 55, 15);
			
			cbTask_EachGate = new Combo(composite_1, SWT.NONE);
			cbTask_EachGate.setToolTipText("Workflow");
			cbTask_EachGate.setBounds(599, 27, 200, 23);
			
			btnSearch_EachGate = new Button(composite_1, SWT.NONE);
			btnSearch_EachGate.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnSearch_EachGate.setBounds(805, 25, 25, 25);
			
			ckbCheckAll = new Button(container, SWT.CHECK);
			ckbCheckAll.setText("Select All");
			new Label(container, SWT.NONE);
			checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblAssigner = checkboxTableViewer.getTable();
			tblAssigner.setLinesVisible(true);
			tblAssigner.setHeaderVisible(true);
			GridData gd_tblAssigner = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd_tblAssigner.widthHint = 706;
			tblAssigner.setLayoutData(gd_tblAssigner);
			
			tableViewerColumn_5 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			tblclmnNewColumn_5 = tableViewerColumn_5.getColumn();
			tblclmnNewColumn_5.setWidth(40);
			
			tableViewerColumn_2 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			tblclmnNewColumn_2 = tableViewerColumn_2.getColumn();
			tblclmnNewColumn_2.setWidth(180);
			tblclmnNewColumn_2.setText("Workflow");
			
			tableViewerColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			tblclmnNewColumn = tableViewerColumn.getColumn();
			tblclmnNewColumn.setWidth(150);
			tblclmnNewColumn.setText("AL Name");
			
			tableViewerColumn_1 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
			tblclmnNewColumn_1.setWidth(150);
			tblclmnNewColumn_1.setText("AL Desc");
			
			tableViewerColumn_4 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			tblclmnNewColumn_4 = tableViewerColumn_4.getColumn();
			tblclmnNewColumn_4.setWidth(200);
			tblclmnNewColumn_4.setText("User");
			
			lblTotalResult = new Label(container, SWT.NONE);
			lblTotalResult.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
			lblTotalResult.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			lblTotalResult.setText("0 item(s)");
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Replace Group Member");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnExportAL = createButton(parent, IDialogConstants.CLOSE_ID, "Export All AL", false);
		btnExcel = createButton(parent, IDialogConstants.CLOSE_ID, "Export", false);
		btnReplace = createButton(parent, IDialogConstants.CLOSE_ID, "Replace", false);
		btnRemove = createButton(parent, IDialogConstants.CLOSE_ID, "Remove", false);
	}
}
