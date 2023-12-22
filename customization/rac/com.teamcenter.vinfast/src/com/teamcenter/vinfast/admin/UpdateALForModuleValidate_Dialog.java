package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Text;

public class UpdateALForModuleValidate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	public Button btnExcel;
	public Table tblExcelIn;
	public TableColumn tblclmnRow;
	public TableColumn tblclmnUserID;
	public Button btnOpenFile;
	private Label lblNewLabel_1;
	private Label lblNewLabel_2;
	public Table tblLOVIn;
	private TableColumn tblclmnRow_1;
	private TableColumn tblclmnUserID_1;
	public Text txtPreference;
	
	public UpdateALForModuleValidate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Import ALs for Module Group Validate");
		setMessage("Define business object create information");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Preference:");
			
			txtPreference = new Text(container, SWT.BORDER);
			txtPreference.setEditable(false);
			txtPreference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			btnOpenFile = new Button(container, SWT.NONE);
			GridData gd_btnOpenFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnOpenFile.widthHint = 29;
			btnOpenFile.setLayoutData(gd_btnOpenFile);
			btnOpenFile.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/file_import_16.png"));
			
			lblNewLabel_1 = new Label(container, SWT.NONE);
			lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
			lblNewLabel_1.setText("Have In Excel, Not Have In LOV");
			
			tblExcelIn = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			GridData gd_tblExcelIn = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
			gd_tblExcelIn.widthHint = 389;
			tblExcelIn.setLayoutData(gd_tblExcelIn);
			tblExcelIn.setHeaderVisible(true);
			tblExcelIn.setLinesVisible(true);
			{
				tblclmnRow = new TableColumn(tblExcelIn, SWT.NONE);
				tblclmnRow.setWidth(500);
				tblclmnRow.setText("Value");
			}
			{
				tblclmnUserID = new TableColumn(tblExcelIn, SWT.NONE);
				tblclmnUserID.setWidth(200);
				tblclmnUserID.setText("Module Level");
			}
			
			lblNewLabel_2 = new Label(container, SWT.NONE);
			lblNewLabel_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
			lblNewLabel_2.setText("Have In LOV, Not Have In Excel");
			
			tblLOVIn = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblLOVIn.setLinesVisible(true);
			tblLOVIn.setHeaderVisible(true);
			tblLOVIn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			
			tblclmnRow_1 = new TableColumn(tblLOVIn, SWT.NONE);
			tblclmnRow_1.setWidth(500);
			tblclmnRow_1.setText("Value");
			
			tblclmnUserID_1 = new TableColumn(tblLOVIn, SWT.NONE);
			tblclmnUserID_1.setWidth(200);
			tblclmnUserID_1.setText("Module Level");
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Admin dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
		btnExcel = createButton(parent, IDialogConstants.CLOSE_ID, "Export", true);
	}

	@Override
	protected void okPressed() {
		
	}
}
