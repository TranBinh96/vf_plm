package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.ResourceManager;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class UpdateALForWorkprocess_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	public Combo cbWorkprocess;
	public Table tblMessage;
	public TableColumn tblclmnRow;
	public TableColumn tblclmnUserID;
	public Button btnOpenFile;
	
	public UpdateALForWorkprocess_Dialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Import ALs for Workprocess");
		setMessage("Define business object create information");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Work Process:");
			
			cbWorkprocess = new Combo(container, SWT.NONE | SWT.READ_ONLY);
			cbWorkprocess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			btnOpenFile = new Button(container, SWT.NONE);
			GridData gd_btnOpenFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnOpenFile.widthHint = 29;
			btnOpenFile.setLayoutData(gd_btnOpenFile);
			btnOpenFile.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/file_import_16.png"));
			
			tblMessage = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			GridData gd_tblMessage = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
			gd_tblMessage.widthHint = 339;
			tblMessage.setLayoutData(gd_tblMessage);
			tblMessage.setHeaderVisible(true);
			tblMessage.setLinesVisible(true);
			{
				tblclmnRow = new TableColumn(tblMessage, SWT.NONE);
				tblclmnRow.setWidth(50);
				tblclmnRow.setText("Row");
			}
			{
				tblclmnUserID = new TableColumn(tblMessage, SWT.NONE);
				tblclmnUserID.setWidth(300);
				tblclmnUserID.setText("UserID Notvalid");
			}
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Import ALs for Workprocess");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(470, 546);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}

	@Override
	protected void okPressed() {
		
	}
}
