package com.teamcenter.vinfast.aftersale.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ServicePartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate = null;

	public Label lblDonorVehicle;
	public Combo cbMaterialGroup;
	public Combo cbModelGroup;
	public Text txtName;
	public Text txtReferenceNumber;
	public Text txtColorCode;
	public Text txtID;
	public Combo cbPartMakeBuy;
	public Text txtVietnameseDesc;
	public Combo cbDonorVehOrScooterVEHLine;
	public Combo cbUom;
	public Button rbAfsRelTrue;
	public Button rbAfsRelFalse;
	public Combo cbPurchaseLevel;
	public Combo cbSupplierType;
	public Combo cbTraceIndicator;
	private Composite composite;
	public Combo cbSuffID;

	public ServicePartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create Service Part");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblMaterial = new Label(container, SWT.NONE);
			lblMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMaterial.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblMaterial.setText("Material Group: (*)");

			cbMaterialGroup = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbMaterialGroup.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbMaterialGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_cbMaterialGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_cbMaterialGroup.widthHint = 302;
			cbMaterialGroup.setLayoutData(gd_cbMaterialGroup);

			Label lblModel = new Label(container, SWT.NONE);
			lblModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModel.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblModel.setText("Model Group: (*)");

			cbModelGroup = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbModelGroup.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbModelGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbModelGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblID = new Label(container, SWT.NONE);
			lblID.setText("ID: (*)");
			lblID.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtID.widthHint = 295;
			txtID.setLayoutData(gd_txtID);
			
			cbSuffID = new Combo(container, SWT.READ_ONLY);
			cbSuffID.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbSuffID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSuffID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblReferenceNumber = new Label(container, SWT.NONE);
			lblReferenceNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblReferenceNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblReferenceNumber.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblReferenceNumber.setText("Reference Number:");

			txtReferenceNumber = new Text(container, SWT.BORDER);
			txtReferenceNumber.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtReferenceNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtReferenceNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblColorCode = new Label(container, SWT.NONE);
			lblColorCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblColorCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblColorCode.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblColorCode.setText("Color Code:");

			txtColorCode = new Text(container, SWT.BORDER);
			txtColorCode.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtColorCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtColorCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPartMakeBuy.setText("Part Make/Buy: (*)");

			cbPartMakeBuy = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPartMakeBuy.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblDes = new Label(container, SWT.NONE);
			lblDes.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDes.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDes.setText("VN Description:");

			txtVietnameseDesc = new Text(container, SWT.BORDER);
			txtVietnameseDesc.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtVietnameseDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtVietnameseDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblDonorVehicle = new Label(container, SWT.NONE);
			lblDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDonorVehicle.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDonorVehicle.setText("Donor Vehicle: (*)");

			cbDonorVehOrScooterVEHLine = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbDonorVehOrScooterVEHLine.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbDonorVehOrScooterVEHLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbDonorVehOrScooterVEHLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblUoM = new Label(container, SWT.NONE);
			lblUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUoM.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblUoM.setText("UoM:");

			cbUom = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbUom.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbUom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblAfs = new Label(container, SWT.NONE);
			lblAfs.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblAfs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblAfs.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblAfs.setText("Aftersales Relevant:");

			composite = new Composite(container, SWT.NONE);
			GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1);
			gd_composite.heightHint = 25;
			composite.setLayoutData(gd_composite);

			rbAfsRelTrue = new Button(composite, SWT.RADIO);
			rbAfsRelTrue.setSelection(true);
			rbAfsRelTrue.setBounds(5, 5, 58, 16);
			rbAfsRelTrue.setText("True");
			rbAfsRelTrue.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			rbAfsRelTrue.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			rbAfsRelTrue.setEnabled(true);

			rbAfsRelFalse = new Button(composite, SWT.RADIO);
			rbAfsRelFalse.setBounds(70, 5, 66, 16);
			rbAfsRelFalse.setText("False");
			rbAfsRelFalse.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			rbAfsRelFalse.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			rbAfsRelFalse.setEnabled(false);

			Label lblPurLevel = new Label(container, SWT.NONE);
			lblPurLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPurLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPurLevel.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPurLevel.setText("Purchase Level:");

			cbPurchaseLevel = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPurchaseLevel.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblSupplierType = new Label(container, SWT.NONE);
			lblSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSupplierType.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblSupplierType.setText("Supplier Type:");

			cbSupplierType = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbSupplierType.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblTraceIndicator = new Label(container, SWT.NONE);
			lblTraceIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblTraceIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTraceIndicator.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblTraceIndicator.setText("Traceability Indicator:");

			cbTraceIndicator = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbTraceIndicator.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbTraceIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbTraceIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 580);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("New Item");
	}
}
