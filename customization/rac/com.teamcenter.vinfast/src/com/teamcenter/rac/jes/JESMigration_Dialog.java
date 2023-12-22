package com.teamcenter.rac.jes;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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

public class JESMigration_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	private Label lblProgram;
	public Combo cbProgram;
	public Table tblShop;
	private CheckboxTableViewer checkboxTableViewer;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnChangeType;
	private TableColumn tblclmnPosid;
	private TableColumn tblclmnNewColumn1;
	public Button ckbCheckAll;
	private Label lblRevisionRule;
	public Combo cbRevisionRule;

	public JESMigration_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Super BOM/BOP");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Program:");
			}
			{
				cbProgram = new Combo(container, SWT.READ_ONLY);
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblRevisionRule = new Label(container, SWT.NONE);
				lblRevisionRule.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblRevisionRule.setText("Revision Rule:");
			}
			{
				cbRevisionRule = new Combo(container, SWT.READ_ONLY);
				cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				ckbCheckAll = new Button(container, SWT.CHECK);
			}
			new Label(container, SWT.NONE);
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
					tblclmnChangeType.setText("Shop");
				}
				{
					tblclmnPosid = new TableColumn(tblShop, SWT.NONE);
					tblclmnPosid.setWidth(87);
					tblclmnPosid.setText("ID");
				}
				{
					tblclmnNewColumn1 = new TableColumn(tblShop, SWT.NONE);
					tblclmnNewColumn1.setWidth(245);
					tblclmnNewColumn1.setText("Desc");
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
