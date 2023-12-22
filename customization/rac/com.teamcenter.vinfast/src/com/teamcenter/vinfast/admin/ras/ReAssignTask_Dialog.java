package com.teamcenter.vinfast.admin.ras;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.MessageBox;

public class ReAssignTask_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	public Label lblTotalResult;
	private Composite composite;
	public Table tblAssigner;

	public Combo cbWorkflow;

	public Button btnSearch;
	public Button btnAdd;
	public Button btnRemove;
	public Button btnReplace;
	public Button ckbCheckAll;
	public Combo cbTaskType;
	private Label lblAlName_1;
	public Text txtProcessName;
	public Combo cbTask;
	private Label lblAlName_2;
	public Text txtUserID;
	private Label lblAlName_3;

	public ReAssignTask_Dialog(Shell parentShell) {
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
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			composite = new Composite(container, SWT.NONE);
			composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			composite.setLayout(new GridLayout(6, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setText("Workflow");

			Label lblAlName = new Label(composite, SWT.NONE);
			lblAlName.setText("Process Name");

			lblAlName_1 = new Label(composite, SWT.NONE);
			lblAlName_1.setText("Task Type");

			lblAlName_2 = new Label(composite, SWT.NONE);
			lblAlName_2.setText("Task");

			lblAlName_3 = new Label(composite, SWT.NONE);
			lblAlName_3.setText("Reviewer");
			new Label(composite, SWT.NONE);

			cbWorkflow = new Combo(composite, SWT.NONE);
			cbWorkflow.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
			GridData gd_cbWorkflow = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
			gd_cbWorkflow.widthHint = 130;
			cbWorkflow.setLayoutData(gd_cbWorkflow);
			cbWorkflow.setToolTipText("Workflow");

			txtProcessName = new Text(composite, SWT.BORDER);
			txtProcessName.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
			GridData gd_txtProcessName = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_txtProcessName.widthHint = 120;
			txtProcessName.setLayoutData(gd_txtProcessName);

			cbTaskType = new Combo(composite, SWT.READ_ONLY);
			cbTaskType.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
			GridData gd_cbTaskType = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
			gd_cbTaskType.widthHint = 100;
			cbTaskType.setLayoutData(gd_cbTaskType);
			cbTaskType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbTaskType.setToolTipText("Workflow");

			cbTask = new Combo(composite, SWT.NONE);
			cbTask.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
			GridData gd_cbTask = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_cbTask.widthHint = 120;
			cbTask.setLayoutData(gd_cbTask);
			cbTask.setToolTipText("Workflow");

			txtUserID = new Text(composite, SWT.BORDER);
			txtUserID.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
			GridData gd_txtUserID = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gd_txtUserID.widthHint = 100;
			txtUserID.setLayoutData(gd_txtUserID);

			btnSearch = new Button(composite, SWT.NONE);
			btnSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnSearch.setFont(SWTResourceManager.getFont("Segoe UI", 6, SWT.NORMAL));
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));

			Composite composite_1 = new Composite(container, SWT.NONE);
			composite_1.setLayout(new GridLayout(4, false));
			composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			ckbCheckAll = new Button(composite_1, SWT.CHECK);

			btnAdd = new Button(composite_1, SWT.NONE);
			btnAdd.setText("Add User");

			btnRemove = new Button(composite_1, SWT.NONE);
			btnRemove.setText("Remove User");

			btnReplace = new Button(composite_1, SWT.NONE);
			btnReplace.setText("Replace User");

			CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblAssigner = checkboxTableViewer.getTable();
			tblAssigner.setLinesVisible(true);
			tblAssigner.setHeaderVisible(true);
			tblAssigner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn_5 = tableViewerColumn_5.getColumn();
			tblclmnNewColumn_5.setWidth(40);

			TableViewerColumn tableViewerColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
			tblclmnNewColumn.setWidth(200);
			tblclmnNewColumn.setText("Task Name");

			TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn_4 = tableViewerColumn_4.getColumn();
			tblclmnNewColumn_4.setWidth(130);
			tblclmnNewColumn_4.setText("Task Type");

			TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
			tblclmnNewColumn_1.setWidth(600);
			tblclmnNewColumn_1.setText("Process Name");

			lblTotalResult = new Label(container, SWT.NONE);
			lblTotalResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTotalResult.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
			lblTotalResult.setText("0 item(s)");
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Re-assign Task");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(986, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

	}
}
