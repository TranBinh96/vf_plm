package com.teamcenter.vinfast.impactedprogram;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ProgressBar;

public class ImpactedProgram_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnSaveExcel;
	
	public Combo cbRevisionRule;
	private Label lblView;
	public Button rbtProgramView;
	public Button rbtPartView;
	public Table tblMaster;
	public TableColumn tblclmnColumn1;
	public TableColumn tblclmnColumn2;
	
	public ImpactedProgram_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.verticalSpacing = 5;
		gridLayout.horizontalSpacing = 5;
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblCategory = new Label(container, SWT.NONE);
			lblCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblCategory.setText("Revision rule:");
			
			cbRevisionRule = new Combo(container, SWT.READ_ONLY);
			cbRevisionRule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblView = new Label(container, SWT.NONE);
			lblView.setText("View:");
			
			rbtProgramView = new Button(container, SWT.RADIO);
			rbtProgramView.setText("Program");
			
			rbtPartView = new Button(container, SWT.RADIO);
			rbtPartView.setText("Part");
			
			tblMaster = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblMaster.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			tblMaster.setHeaderVisible(true);
			tblMaster.setLinesVisible(true);
			
			tblclmnColumn1 = new TableColumn(tblMaster, SWT.NONE);
			tblclmnColumn1.setWidth(200);
			tblclmnColumn1.setText("New Column");
			
			tblclmnColumn2 = new TableColumn(tblMaster, SWT.NONE);
			tblclmnColumn2.setWidth(200);
			tblclmnColumn2.setText("New Column");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSaveExcel = createButton(parent, IDialogConstants.CLOSE_ID, "Save to Excel", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Impacted Program");
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}
}
