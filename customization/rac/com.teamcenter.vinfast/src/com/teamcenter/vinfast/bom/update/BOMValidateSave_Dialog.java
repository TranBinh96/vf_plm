package com.teamcenter.vinfast.bom.update;

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

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Label;

public class BOMValidateSave_Dialog extends TitleAreaDialog {
	private Composite area;
	public TCSession session = null;
	public Button btnOK;
	public Browser browserReport;
	public Table tblUpdate;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnNewColumn_4;
	private TableColumn tblclmnNewColumn_5;

	public BOMValidateSave_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		area = (Composite) super.createDialogArea(parent);
		setTitle("BOM Attribute Massupdate");
		setMessage("Define business object create information");
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 3;
		gridLayout.marginHeight = 3;
		
		browserReport = new Browser(area, SWT.NONE);
		browserReport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		tblUpdate = new Table(area, SWT.BORDER | SWT.FULL_SELECTION);
//		tblUpdate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		tblUpdate.setHeaderVisible(true);
//		tblUpdate.setLinesVisible(true);
//		{
//			tblclmnNewColumn = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn.setWidth(150);
//			tblclmnNewColumn.setText("Bomline");
//			
//			tblclmnNewColumn_5 = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn_5.setWidth(90);
//			tblclmnNewColumn_5.setText("Part Make/Buy");
//			tblclmnNewColumn_1 = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn_1.setWidth(90);
//			tblclmnNewColumn_1.setText("Purchase Level");
//			tblclmnNewColumn_2 = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn_2.setWidth(172);
//			tblclmnNewColumn_2.setText("Module Group English");
//			tblclmnNewColumn_3 = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn_3.setWidth(187);
//			tblclmnNewColumn_3.setText("Main Module English");
//			tblclmnNewColumn_4 = new TableColumn(tblUpdate, SWT.NONE);
//			tblclmnNewColumn_4.setWidth(152);
//			tblclmnNewColumn_4.setText("Module Name");
//		}

		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
		gd_table.widthHint = 150;
		gd_table.heightHint = 150;

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("BOM Validation Error");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	@Override
	protected void okPressed() {
		return;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnOK = createButton(parent, IDialogConstants.OK_ID, "Save", true);
	}
}
