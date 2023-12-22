package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

import com.teamcenter.rac.kernel.TCSession;

public class DVPRDocumentTestReportEdit_Dialog extends Dialog {
	public Button btnAccept;
	private Label lblNewLabel;

	public String valueSelected = "";
	private Label lblDonorVehicle;
	private Label lblValue_2;
	private Label lblValue_3;
	private Label lblValue_4;

	public Text txtTestName;
	public Text txtComment;
	public Text txtVFTE;
	public Combo cbTestResult;
	public Combo cbProgress;

	private String[] progressDataForm = { "0%", "50%", "90%", "100%" };
	private String[] testResultDataForm = { "Green", "Yellow", "Red" };
	private ScrolledComposite scrolledComposite;
	private Composite composite;

	public DVPRDocumentTestReportEdit_Dialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(2, false);
		container.setLayout(gl_shell);

		scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("VFTE:");

		txtVFTE = new Text(composite, SWT.BORDER);
		txtVFTE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_2 = new Label(composite, SWT.NONE);
		lblValue_2.setText("Test Name:");
		lblValue_2.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtTestName = new Text(composite, SWT.BORDER);
		txtTestName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblDonorVehicle = new Label(composite, SWT.NONE);
		lblDonorVehicle.setText("Progress:");
		lblDonorVehicle.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbProgress = new Combo(composite, SWT.READ_ONLY);
		cbProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbProgress.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbProgress.setItems(progressDataForm);

		lblValue_3 = new Label(composite, SWT.NONE);
		lblValue_3.setText("Test Result:");
		lblValue_3.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbTestResult = new Combo(composite, SWT.READ_ONLY);
		cbTestResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbTestResult.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbTestResult.setItems(testResultDataForm);

		lblValue_4 = new Label(composite, SWT.NONE);
		lblValue_4.setText("Comment:");
		lblValue_4.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtComment = new Text(composite, SWT.BORDER);
		txtComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return container;
	}

	public void initData(TCSession session, LinkedHashMap<String, String> bomInfo) {
		try {
			if (bomInfo != null) {
				txtVFTE.setText(bomInfo.get(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM));
				txtTestName.setText(bomInfo.get(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME));
				cbProgress.setText(bomInfo.get(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS));
				cbTestResult.setText(bomInfo.get(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT));
				txtComment.setText(bomInfo.get(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT));
				disableUI();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disableUI() {
		txtVFTE.setEnabled(false);
		txtTestName.setEnabled(false);
	}

	public void clearUI() {
		txtVFTE.setText("");
		txtTestName.setText("");
		cbProgress.deselectAll();
		cbTestResult.deselectAll();
		txtComment.setText("");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update Test Report");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 350);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
