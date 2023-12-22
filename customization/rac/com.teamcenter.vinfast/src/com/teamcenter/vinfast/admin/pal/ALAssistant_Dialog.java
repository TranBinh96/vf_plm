package com.teamcenter.vinfast.admin.pal;

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

public class ALAssistant_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	public Label lblTotalResult;
	private Composite composite;
	public Table tblAssigner;
	public Text txtALDesc;
	public Text txtALName;

	public Combo cbWorkflow;
	public Combo cbTask;

	public Button btnSearch;
	public Button btnAdd;
	public Button btnRemove;
	public Button btnReplace;
	public Button btnCopy;
	public Button ckbCheckAll;
	public Button btnEditAL;
	public Button btnDeleteAL;
	public Button btnExport;
	public Button btnExportAlByUser;
	public Button btnNewAl;
	public Button btnImport;
	public Button btnUpdateAlName;

	public ALAssistant_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
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
			GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_composite.heightHint = 63;
			composite.setLayoutData(gd_composite);

			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setText("Workflow");
			lblNewLabel.setBounds(10, 10, 55, 15);

			cbWorkflow = new Combo(composite, SWT.NONE);
			cbWorkflow.setToolTipText("Workflow");
			cbWorkflow.setBounds(10, 30, 200, 23);

			Label lblAlName = new Label(composite, SWT.NONE);
			lblAlName.setText("AL Name");
			lblAlName.setBounds(216, 10, 55, 15);

			txtALName = new Text(composite, SWT.BORDER);
			txtALName.setToolTipText("Assignment List Name");
			txtALName.setBounds(216, 31, 140, 23);

			Label lblAlName_2 = new Label(composite, SWT.NONE);
			lblAlName_2.setText("Task");
			lblAlName_2.setBounds(508, 10, 55, 15);

			cbTask = new Combo(composite, SWT.NONE);
			cbTask.setToolTipText("Workflow");
			cbTask.setBounds(508, 30, 200, 23);

			btnSearch = new Button(composite, SWT.NONE);
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnSearch.setBounds(714, 28, 25, 25);

			Label lblAlDesc = new Label(composite, SWT.NONE);
			lblAlDesc.setText("AL Desc");
			lblAlDesc.setBounds(362, 9, 55, 15);

			txtALDesc = new Text(composite, SWT.BORDER);
			txtALDesc.setToolTipText("Assignment List Name");
			txtALDesc.setBounds(362, 30, 140, 23);

			Composite composite_1 = new Composite(container, SWT.NONE);
			composite_1.setLayout(new GridLayout(12, false));
			composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			ckbCheckAll = new Button(composite_1, SWT.CHECK);

			btnAdd = new Button(composite_1, SWT.NONE);
			btnAdd.setText("Add User");

			btnRemove = new Button(composite_1, SWT.NONE);
			btnRemove.setText("Remove User");

			btnReplace = new Button(composite_1, SWT.NONE);
			btnReplace.setText("Replace User");

			btnCopy = new Button(composite_1, SWT.NONE);
			btnCopy.setText("Copy User");

			btnEditAL = new Button(composite_1, SWT.NONE);
			btnEditAL.setText("Edit AL");

			btnExport = new Button(composite_1, SWT.NONE);
			btnExport.setText("Export AL");

			btnDeleteAL = new Button(composite_1, SWT.NONE);
			btnDeleteAL.setText("Delete AL");

			btnExportAlByUser = new Button(composite_1, SWT.NONE);
			btnExportAlByUser.setText("Export AL By User");

			btnNewAl = new Button(composite_1, SWT.NONE);
			btnNewAl.setText("New AL");

			btnImport = new Button(composite_1, SWT.NONE);
			btnImport.setText("Import AL");

			btnUpdateAlName = new Button(composite_1, SWT.NONE);
			btnUpdateAlName.setText("Update AL Name");

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
			tblclmnNewColumn.setWidth(150);
			tblclmnNewColumn.setText("AL Name");

			TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
			tblclmnNewColumn_1.setWidth(150);
			tblclmnNewColumn_1.setText("AL Desc");

			TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
			TableColumn tblclmnNewColumn_4 = tableViewerColumn_4.getColumn();
			tblclmnNewColumn_4.setWidth(200);
			tblclmnNewColumn_4.setText("User");

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
		newShell.setText("AL Assistant");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

	}
}
