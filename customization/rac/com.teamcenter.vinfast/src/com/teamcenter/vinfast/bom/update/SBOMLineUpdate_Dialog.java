package com.teamcenter.vinfast.bom.update;

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

import com.teamcenter.rac.kernel.TCException;

public class SBOMLineUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnSupersession;
	public Button btnEffectiveDate;
	public Button btnValidtillDate;
	public Button btnMarket;
	public Button btn1stVINNewPart;
	public Button btnRemarks;
	public Table tblBom;
	public Button ckbCheckAll;

	public Button btnAftersalesCritical;
	public Button btnEpcVariant;
	public Button btnCreate;

	public SBOMLineUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Update SBOM Information");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Composite composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			ckbCheckAll = new Button(composite, SWT.CHECK);
			ckbCheckAll.setBounds(10, 5, 13, 16);

			btnSupersession = new Button(composite, SWT.NONE);
			btnSupersession.setBounds(38, 0, 100, 25);
			btnSupersession.setText("Supersession");

			btnEffectiveDate = new Button(composite, SWT.NONE);
			btnEffectiveDate.setBounds(143, 0, 100, 25);
			btnEffectiveDate.setText("Effective Date");

			btnValidtillDate = new Button(composite, SWT.NONE);
			btnValidtillDate.setBounds(248, 0, 100, 25);
			btnValidtillDate.setText("Valid till Date");

			btnMarket = new Button(composite, SWT.NONE);
			btnMarket.setBounds(353, 0, 100, 25);
			btnMarket.setText("Market");

			btn1stVINNewPart = new Button(composite, SWT.NONE);
			btn1stVINNewPart.setBounds(458, 0, 100, 25);
			btn1stVINNewPart.setText("1st VIN New Part");

			btnRemarks = new Button(composite, SWT.NONE);
			btnRemarks.setText("Remarks");
			btnRemarks.setBounds(776, 0, 100, 25);

			btnAftersalesCritical = new Button(composite, SWT.NONE);
			btnAftersalesCritical.setText("Aftersales");
			btnAftersalesCritical.setBounds(564, -1, 100, 25);

			btnEpcVariant = new Button(composite, SWT.NONE);
			btnEpcVariant.setText("EPC Variant");
			btnEpcVariant.setBounds(670, -1, 100, 25);

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

			TableColumn tblclmnNewColumn = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn.setWidth(100);
			tblclmnNewColumn.setText("Part No");

			TableColumn tblclmnNewRevision = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewRevision.setWidth(55);
			tblclmnNewRevision.setText("Rev");

			TableColumn tblclmnPartName = new TableColumn(tblBom, SWT.NONE);
			tblclmnPartName.setWidth(100);
			tblclmnPartName.setText("Part Name");

			TableColumn tblclmnPosid = new TableColumn(tblBom, SWT.NONE);
			tblclmnPosid.setWidth(69);
			tblclmnPosid.setText("POSID");

			TableColumn tblclmnLevel = new TableColumn(tblBom, SWT.NONE);
			tblclmnLevel.setWidth(40);
			tblclmnLevel.setText("Level");

			TableColumn tblclmnQuantity = new TableColumn(tblBom, SWT.NONE);
			tblclmnQuantity.setWidth(50);
			tblclmnQuantity.setText("Qty");

			TableColumn tblclmnOriginalPart = new TableColumn(tblBom, SWT.NONE);
			tblclmnOriginalPart.setWidth(86);
			tblclmnOriginalPart.setText("Original Part");

			TableColumn tblclmnVariantFormula = new TableColumn(tblBom, SWT.NONE);
			tblclmnVariantFormula.setWidth(100);
			tblclmnVariantFormula.setText("Supersession");

			TableColumn tblclmnTorqueInfo = new TableColumn(tblBom, SWT.NONE);
			tblclmnTorqueInfo.setWidth(100);
			tblclmnTorqueInfo.setText("Effective Date");

			TableColumn tblclmnWeightg = new TableColumn(tblBom, SWT.NONE);
			tblclmnWeightg.setWidth(100);
			tblclmnWeightg.setText("Valid till Date");

			TableColumn tblclmnChangeDesc = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangeDesc.setWidth(100);
			tblclmnChangeDesc.setText("Market");

			TableColumn tblclmndDataAffected = new TableColumn(tblBom, SWT.NONE);
			tblclmndDataAffected.setWidth(60);
			tblclmndDataAffected.setText("1st VIN New Part");

			TableColumn tblclmnNewColumn_3 = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn_3.setWidth(100);
			tblclmnNewColumn_3.setText("Aftersales Critical");

			TableColumn tblclmnNewColumn_2 = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn_2.setWidth(100);
			tblclmnNewColumn_2.setText("EPC Variant");

			TableColumn tblclmnMaterial = new TableColumn(tblBom, SWT.NONE);
			tblclmnMaterial.setWidth(100);
			tblclmnMaterial.setText("Remarks");
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
		newShell.setText("ServiceBOM...");
	}
}
