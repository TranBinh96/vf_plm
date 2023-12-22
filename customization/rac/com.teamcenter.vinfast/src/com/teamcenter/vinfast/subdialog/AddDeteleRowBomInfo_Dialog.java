package com.teamcenter.vinfast.subdialog;

import java.util.LinkedHashMap;

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

import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;

import org.eclipse.swt.widgets.Text;

public class AddDeteleRowBomInfo_Dialog extends Dialog {
	public Button btnAccept;
	private Label lblNewLabel;
	private TCSession session;

	public String valueSelected = "";
	private Label lblDonorVehicle;
	private Label lblValue_2;
	private Label lblValue_3;
	private Label lblValue_4;
	private Label lblValue_5;
	private Label lblValue_6;
	private Label lblValue_7;
	private Label lblValue_8;
	private Label lblValue_9;
	private Label lblValue_10;
	private Label lblValue_1;
	private Label lblValue_11;
	private Label lblValue_12;
	private Label lblValue_13;
	private Label lblValue_14;
	private Label lblValue_15;
	private Label lblValue_16;
	private Label lblValue_17;
	private Label lblValue_18;
	private Label lblValue_19;
	private Label lblValue_20;
	private Label lblValue_21;
	private Label lblValue_22;
	public Combo cbSteering;
	public Text txtLevel;
	public Text txtQuantity;
	public Combo cbMaturityLevel;
	public Text txtPartNo;
	public Text txtOldRev;
	public Text txtFrozenRev;
	public Text txtNewRev;
	public Text txtPartName;
	public Text txtOriginalPart;
	public Text txtVariantFormula;
	public Text txtTorqueInfo;
	public Text txtWeight;
	public Text txtChangeDesc;
	public Button ckb3DData;
	public Text txtMaterial;
	public Text txtCoating;
	public Text txtSpecbook;
	public Combo cbExchangeability;
	public Combo cbAftersaleRelevant;
	public Combo cbPartTraceability;
	public Text txtPOSID;
	public Combo cbDonorVehicle;
	public Combo cbPurchaseLevel;

	private String[] maturityLevelDataForm = { "Not released", "P", "I", "PR", "PPR" };
	private String[] steeringDataForm = { "All", "LHD", "RHD" };
	private String[] exchangeabilityDataForm = { "Fully interchangeable-stock OK to use for any vehicle", "New for old only-stock OK to service old vehicles", "New for old only-do not use stock", "Not interchangeable-stock OK to service old vehicles", "Not interchangeable-do not use stock" };

	public AddDeteleRowBomInfo_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(2, false);
		container.setLayout(gl_shell);

		lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("POSID:");

		txtPOSID = new Text(container, SWT.BORDER);
		txtPOSID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblDonorVehicle = new Label(container, SWT.NONE);
		lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDonorVehicle.setText("Donor Vehicle:");
		lblDonorVehicle.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbDonorVehicle = new Combo(container, SWT.READ_ONLY);
		cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_2 = new Label(container, SWT.NONE);
		lblValue_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_2.setText("Level:");
		lblValue_2.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtLevel = new Text(container, SWT.BORDER);
		txtLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_3 = new Label(container, SWT.NONE);
		lblValue_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_3.setText("Steering:");
		lblValue_3.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbSteering = new Combo(container, SWT.READ_ONLY);
		cbSteering.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbSteering.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_4 = new Label(container, SWT.NONE);
		lblValue_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_4.setText("Quantity:");
		lblValue_4.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtQuantity = new Text(container, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_5 = new Label(container, SWT.NONE);
		lblValue_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_5.setText("Maturity Level:");
		lblValue_5.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbMaturityLevel = new Combo(container, SWT.READ_ONLY);
		cbMaturityLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbMaturityLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_6 = new Label(container, SWT.NONE);
		lblValue_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_6.setText("Purchase Level:");
		lblValue_6.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbPurchaseLevel = new Combo(container, SWT.READ_ONLY);
		cbPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_7 = new Label(container, SWT.NONE);
		lblValue_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_7.setText("Part No:");
		lblValue_7.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartNo = new Text(container, SWT.BORDER);
		txtPartNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_8 = new Label(container, SWT.NONE);
		lblValue_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_8.setText("Old Rev:");
		lblValue_8.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldRev = new Text(container, SWT.BORDER);
		txtOldRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_9 = new Label(container, SWT.NONE);
		lblValue_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_9.setText("Frozen Rev:");
		lblValue_9.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtFrozenRev = new Text(container, SWT.BORDER);
		txtFrozenRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_10 = new Label(container, SWT.NONE);
		lblValue_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_10.setText("New Rev:");
		lblValue_10.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtNewRev = new Text(container, SWT.BORDER);
		txtNewRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_1 = new Label(container, SWT.NONE);
		lblValue_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_1.setText("Part Name:");
		lblValue_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartName = new Text(container, SWT.BORDER);
		txtPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_11 = new Label(container, SWT.NONE);
		lblValue_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_11.setText("Original Part:");
		lblValue_11.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOriginalPart = new Text(container, SWT.BORDER);
		txtOriginalPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_12 = new Label(container, SWT.NONE);
		lblValue_12.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_12.setText("Variant Formula:");
		lblValue_12.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtVariantFormula = new Text(container, SWT.BORDER);
		txtVariantFormula.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_13 = new Label(container, SWT.NONE);
		lblValue_13.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_13.setText("Torque Info:");
		lblValue_13.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtTorqueInfo = new Text(container, SWT.BORDER);
		txtTorqueInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_14 = new Label(container, SWT.NONE);
		lblValue_14.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_14.setText("Weight (g):");
		lblValue_14.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtWeight = new Text(container, SWT.BORDER);
		txtWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_15 = new Label(container, SWT.NONE);
		lblValue_15.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_15.setText("Change Description:");
		lblValue_15.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtChangeDesc = new Text(container, SWT.BORDER);
		txtChangeDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_16 = new Label(container, SWT.NONE);
		lblValue_16.setText("3D Data:");
		lblValue_16.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		ckb3DData = new Button(container, SWT.CHECK);

		lblValue_17 = new Label(container, SWT.NONE);
		lblValue_17.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_17.setText("Material:");
		lblValue_17.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtMaterial = new Text(container, SWT.BORDER);
		txtMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_18 = new Label(container, SWT.NONE);
		lblValue_18.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_18.setText("Coating:");
		lblValue_18.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtCoating = new Text(container, SWT.BORDER);
		txtCoating.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_19 = new Label(container, SWT.NONE);
		lblValue_19.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_19.setText("Specbook:");
		lblValue_19.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtSpecbook = new Text(container, SWT.BORDER);
		txtSpecbook.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_20 = new Label(container, SWT.NONE);
		lblValue_20.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_20.setText("Aftersale Relevant:");
		lblValue_20.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbAftersaleRelevant = new Combo(container, SWT.READ_ONLY);
		cbAftersaleRelevant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbAftersaleRelevant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_21 = new Label(container, SWT.NONE);
		lblValue_21.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_21.setText("Exchangeability:");
		lblValue_21.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbExchangeability = new Combo(container, SWT.READ_ONLY);
		cbExchangeability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbExchangeability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_22 = new Label(container, SWT.NONE);
		lblValue_22.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_22.setText("Part Traceability:");
		lblValue_22.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbPartTraceability = new Combo(container, SWT.READ_ONLY);
		cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return container;
	}

	public void initData(TCSession session) {
		this.session = session;
		try {
			cbSteering.setItems(steeringDataForm);
			cbMaturityLevel.setItems(maturityLevelDataForm);
			cbExchangeability.setItems(exchangeabilityDataForm);
			String[] partTraceabilityDataForm = TCExtension.GetLovValues("vf4_item_is_traceable", "VF4_Design", session);
			cbPartTraceability.setItems(partTraceabilityDataForm);
			String[] donorVehicleDataForm = TCExtension.GetLovValues("vf4_donor_vehicle", "VF4_Design", session);
			cbDonorVehicle.setItems(donorVehicleDataForm);
			String[] afterSaleRevDataForm = { "True", "False" };
			cbAftersaleRelevant.setItems(afterSaleRevDataForm);
			String[] purchaseLevelDataForm = TCExtension.GetLovValues("Vl5_purchase_lvl_vf");
			cbPurchaseLevel.setItems(purchaseLevelDataForm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clearUI() {
		txtPOSID.setText("");
		cbDonorVehicle.deselectAll();
		txtLevel.setText("");
		cbSteering.deselectAll();
		txtQuantity.setText("");
		cbMaturityLevel.deselectAll();
		cbPurchaseLevel.deselectAll();
		txtPartNo.setText("");
		txtOldRev.setText("");
		txtFrozenRev.setText("");
		txtNewRev.setText("");
		txtPartName.setText("");
		txtOriginalPart.setText("");
		txtVariantFormula.setText("");
		txtTorqueInfo.setText("");
		txtWeight.setText("");
		txtChangeDesc.setText("");
		
		txtMaterial.setText("");
		txtCoating.setText("");
		txtSpecbook.setText("");
		cbAftersaleRelevant.deselectAll();
		cbExchangeability.deselectAll();
		cbPartTraceability.deselectAll();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Add", true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Remove bomline...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(460, 743);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
