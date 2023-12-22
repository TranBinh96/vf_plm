package com.teamcenter.vinfast.bom.update;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PartSubstitutesPaste_Dialog extends Dialog {
	private Composite area;
	public TCSession session = null;
	public Button btnOK;
	public Table tblUpdate;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNo;

	public PartSubstitutesPaste_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 3;
		gridLayout.marginHeight = 3;
		try {
			tblUpdate = new Table(area, SWT.BORDER | SWT.FULL_SELECTION);
			tblUpdate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tblUpdate.setHeaderVisible(true);
			tblUpdate.setLinesVisible(true);
			{
				tblclmnNo = new TableColumn(tblUpdate, SWT.NONE);
				tblclmnNo.setWidth(40);
				tblclmnNo.setText("No");
			}
			{
				tblclmnNewColumn = new TableColumn(tblUpdate, SWT.NONE);
				tblclmnNewColumn.setWidth(200);
				tblclmnNewColumn.setText("Bomline");
				tblclmnNewColumn_1 = new TableColumn(tblUpdate, SWT.NONE);
				tblclmnNewColumn_1.setWidth(150);
				tblclmnNewColumn_1.setText("Substitues");
				tblclmnNewColumn_2 = new TableColumn(tblUpdate, SWT.NONE);
				tblclmnNewColumn_2.setWidth(230);
				tblclmnNewColumn_2.setText("Status");
			}

			GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gd_table.widthHint = 150;
			gd_table.heightHint = 150;
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Paste Part with substitues");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(650, 500);
	}

	@Override
	protected void okPressed() {
		return;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

	}
}
