package com.teamcenter.vinfast.car.engineering.specdoc;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.Label;

public class DVPRDocumentTestReportUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnProgressUpdateAll;
	public Button btnTestResultUpdateAll;
	public Table tblBom;
	public Button ckbCheckAll;

	public Button btnCreate;

	public DVPRDocumentTestReportUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Update Test Report");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Composite composite = new Composite(container, SWT.NONE);
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			ckbCheckAll = new Button(composite, SWT.CHECK);

			btnProgressUpdateAll = new Button(composite, SWT.NONE);
			btnProgressUpdateAll.setText("Progress");

			btnTestResultUpdateAll = new Button(composite, SWT.NONE);
			btnTestResultUpdateAll.setText("Test Result");

			CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.MULTI);
			checkboxTableViewer.setAllChecked(false);
			checkboxTableViewer.setAllGrayed(false);
			tblBom = checkboxTableViewer.getTable();
			tblBom.setTouchEnabled(true);
			tblBom.setLinesVisible(true);
			tblBom.setHeaderVisible(true);
			tblBom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			TableColumn tblclmnNewColumn_1 = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn_1.setWidth(30);

			TableColumn tblclmnChangeType = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangeType.setWidth(120);
			tblclmnChangeType.setText("VFTE");

			TableColumn tblclmnPosid = new TableColumn(tblBom, SWT.NONE);
			tblclmnPosid.setWidth(300);
			tblclmnPosid.setText("Test Name");

			TableColumn tblclmnDonorVehicle = new TableColumn(tblBom, SWT.NONE);
			tblclmnDonorVehicle.setWidth(100);
			tblclmnDonorVehicle.setText("Progress");

			TableColumn tblclmnLevel = new TableColumn(tblBom, SWT.NONE);
			tblclmnLevel.setWidth(100);
			tblclmnLevel.setText("Test Result");

			TableColumn tblclmnSteering = new TableColumn(tblBom, SWT.NONE);
			tblclmnSteering.setWidth(300);
			tblclmnSteering.setText("Comment");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1038, 1000);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Test Report");
	}
}
