package com.teamcenter.rac.jes;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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

public class JESMigrationByLine_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	public Table tblShop;
	private CheckboxTableViewer checkboxTableViewer;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnChangeType;

	public JESMigrationByLine_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("JES Migration");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
				checkboxTableViewer.setAllGrayed(true);
				checkboxTableViewer.setAllChecked(false);
				tblShop = checkboxTableViewer.getTable();
				tblShop.setLinesVisible(true);
				tblShop.setHeaderVisible(true);
				tblShop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				{
					tblclmnNewColumn = new TableColumn(tblShop, SWT.NONE);
					tblclmnNewColumn.setWidth(30);
				}
				{
					tblclmnChangeType = new TableColumn(tblShop, SWT.NONE);
					tblclmnChangeType.setWidth(90);
					tblclmnChangeType.setText("Line");
				}
			}
		} catch (Exception e) {

		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Export", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 800);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Export Report");
	}
}
