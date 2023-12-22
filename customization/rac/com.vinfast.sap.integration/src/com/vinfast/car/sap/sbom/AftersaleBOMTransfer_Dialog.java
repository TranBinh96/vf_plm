package com.vinfast.car.sap.sbom;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class AftersaleBOMTransfer_Dialog extends TitleAreaDialog {
	public Button btnAccept;
	public Browser brwReport;
	private Label label;
	private Label lblModel;
	public Text txtModel;
	private Label lblYear;
	public Text txtYear;
	private Label lblYear_1;
	public Combo cbServer;

	public AftersaleBOMTransfer_Dialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);

		lblModel = new Label(container, SWT.NONE);
		lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModel.setText("MODEL:");

		txtModel = new Text(container, SWT.BORDER);
		txtModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtModel.setEditable(false);
		txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblYear_1 = new Label(container, SWT.NONE);
		lblYear_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblYear_1.setText("SERVER:");

		cbServer = new Combo(container, SWT.READ_ONLY);
		cbServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblYear = new Label(container, SWT.NONE);
		lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblYear.setText("YEAR:");

		txtYear = new Text(container, SWT.BORDER);
		txtYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtYear.setEditable(false);
		txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

		brwReport = new Browser(container, SWT.NONE);
		brwReport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP Transfer");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
	}
}
