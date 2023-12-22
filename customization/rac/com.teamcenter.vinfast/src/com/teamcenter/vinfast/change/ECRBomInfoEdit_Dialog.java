package com.teamcenter.vinfast.change;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;
import org.eclipse.swt.custom.ScrolledComposite;

public class ECRBomInfoEdit_Dialog extends Dialog {
	public Button btnAccept;
	private Label lblNewLabel;

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
	private Label lblValue;
	private Label lblSupplierContact;
	private Label lblChangeType;

	public Text txtLevel;
	public Text txtQuantity;
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
	public Text txtMaterial;
	public Text txtCoating;
	public Text txtSpecbook;
	public Text txtPOSID;
	public Text txtSupplier;
	public Text txtSupplierContact;
	public Combo cbExchangeability;
	public Combo cbAftersaleRelevant;
	public Combo cbPartTraceability;
	public Combo cbSteering;
	public Combo cbMaturityLevel;
	public Combo cbDonorVehicle;
	public Combo cbPurchaseLevel;
	public Combo cbChangeType;
	public Button ckb3DData;

	private String[] changeTypeDataForm = { "NEW", "ADD", "CHANGE", "SWAP", "REMOVE" };
	private String[] maturityLevelDataForm = { "Not released", "P", "I", "PR", "PPR" };
	private String[] steeringDataForm = { "All", "LHD", "RHD" };
	private String[] exchangeabilityDataForm = { "Fully interchangeable-stock OK to use for any vehicle", "New for old only-stock OK to service old vehicles", "New for old only-do not use stock", "Not interchangeable-stock OK to service old vehicles", "Not interchangeable-do not use stock" };
	private ScrolledComposite scrolledComposite;
	private Composite composite;

	public ECRBomInfoEdit_Dialog(Shell parent) {
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

		lblChangeType = new Label(composite, SWT.NONE);
		lblChangeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblChangeType.setText("Change Type:");

		cbChangeType = new Combo(composite, SWT.NONE);
		cbChangeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbChangeType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbChangeType.setItems(changeTypeDataForm);

		lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("POSID:");

		txtPOSID = new Text(composite, SWT.BORDER);
		txtPOSID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblDonorVehicle = new Label(composite, SWT.NONE);
		lblDonorVehicle.setText("Donor Vehicle:");
		lblDonorVehicle.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbDonorVehicle = new Combo(composite, SWT.NONE);
		cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		lblValue_2 = new Label(composite, SWT.NONE);
		lblValue_2.setText("Level:");
		lblValue_2.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtLevel = new Text(composite, SWT.BORDER);
		txtLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_3 = new Label(composite, SWT.NONE);
		lblValue_3.setText("Steering:");
		lblValue_3.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbSteering = new Combo(composite, SWT.NONE);
		cbSteering.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbSteering.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbSteering.setItems(steeringDataForm);

		lblValue_4 = new Label(composite, SWT.NONE);
		lblValue_4.setText("Quantity:");
		lblValue_4.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtQuantity = new Text(composite, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_5 = new Label(composite, SWT.NONE);
		lblValue_5.setText("Maturity Level:");
		lblValue_5.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbMaturityLevel = new Combo(composite, SWT.NONE);
		cbMaturityLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbMaturityLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbMaturityLevel.setItems(maturityLevelDataForm);

		lblValue_6 = new Label(composite, SWT.NONE);
		lblValue_6.setText("Purchase Level:");
		lblValue_6.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbPurchaseLevel = new Combo(composite, SWT.NONE);
		cbPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		lblValue_7 = new Label(composite, SWT.NONE);
		lblValue_7.setText("Part No:");
		lblValue_7.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartNo = new Text(composite, SWT.BORDER);
		txtPartNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_8 = new Label(composite, SWT.NONE);
		lblValue_8.setText("Old Rev:");
		lblValue_8.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldRev = new Text(composite, SWT.BORDER);
		txtOldRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_9 = new Label(composite, SWT.NONE);
		lblValue_9.setText("Frozen Rev:");
		lblValue_9.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtFrozenRev = new Text(composite, SWT.BORDER);
		txtFrozenRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_10 = new Label(composite, SWT.NONE);
		lblValue_10.setText("New Rev:");
		lblValue_10.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtNewRev = new Text(composite, SWT.BORDER);
		txtNewRev.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_1 = new Label(composite, SWT.NONE);
		lblValue_1.setText("Part Name:");
		lblValue_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartName = new Text(composite, SWT.BORDER);
		txtPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_11 = new Label(composite, SWT.NONE);
		lblValue_11.setText("Original Part:");
		lblValue_11.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOriginalPart = new Text(composite, SWT.BORDER);
		txtOriginalPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_12 = new Label(composite, SWT.NONE);
		lblValue_12.setText("Variant Formula:");
		lblValue_12.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtVariantFormula = new Text(composite, SWT.BORDER);
		txtVariantFormula.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_13 = new Label(composite, SWT.NONE);
		lblValue_13.setText("Torque Info:");
		lblValue_13.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtTorqueInfo = new Text(composite, SWT.BORDER);
		txtTorqueInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_14 = new Label(composite, SWT.NONE);
		lblValue_14.setText("Weight (g):");
		lblValue_14.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtWeight = new Text(composite, SWT.BORDER);
		txtWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_15 = new Label(composite, SWT.NONE);
		lblValue_15.setText("Change Description:");
		lblValue_15.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtChangeDesc = new Text(composite, SWT.BORDER);
		txtChangeDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_16 = new Label(composite, SWT.NONE);
		lblValue_16.setText("3D Data:");
		lblValue_16.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		ckb3DData = new Button(composite, SWT.CHECK);

		lblValue_17 = new Label(composite, SWT.NONE);
		lblValue_17.setText("Material:");
		lblValue_17.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtMaterial = new Text(composite, SWT.BORDER);
		txtMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_18 = new Label(composite, SWT.NONE);
		lblValue_18.setText("Coating:");
		lblValue_18.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtCoating = new Text(composite, SWT.BORDER);
		txtCoating.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_19 = new Label(composite, SWT.NONE);
		lblValue_19.setText("Specbook:");
		lblValue_19.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtSpecbook = new Text(composite, SWT.BORDER);
		txtSpecbook.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue = new Label(composite, SWT.NONE);
		lblValue.setText("Supplier:");
		lblValue.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtSupplier = new Text(composite, SWT.BORDER);
		txtSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblSupplierContact = new Label(composite, SWT.NONE);
		lblSupplierContact.setText("Supplier Contact:");
		lblSupplierContact.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtSupplierContact = new Text(composite, SWT.BORDER);
		txtSupplierContact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_20 = new Label(composite, SWT.NONE);
		lblValue_20.setText("Aftersale Relevant:");
		lblValue_20.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbAftersaleRelevant = new Combo(composite, SWT.READ_ONLY);
		cbAftersaleRelevant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbAftersaleRelevant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		lblValue_21 = new Label(composite, SWT.NONE);
		lblValue_21.setText("Exchangeability:");
		lblValue_21.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbExchangeability = new Combo(composite, SWT.NONE);
		cbExchangeability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbExchangeability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbExchangeability.setItems(exchangeabilityDataForm);

		lblValue_22 = new Label(composite, SWT.NONE);
		lblValue_22.setText("Part Traceability:");
		lblValue_22.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbPartTraceability = new Combo(composite, SWT.READ_ONLY);
		cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return container;
	}

	public void initData(TCSession session, LinkedHashMap<String, String> bomInfo) {
		try {
			String[] partTraceabilityDataForm = TCExtension.GetLovValues("vf4_item_is_traceable", "VF4_Design", session);
			cbPartTraceability.setItems(partTraceabilityDataForm);
			String[] donorVehicleDataForm = TCExtension.GetLovValues("vf4_donor_vehicle", "VF4_Design", session);
			cbDonorVehicle.setItems(donorVehicleDataForm);
			String[] afterSaleRevDataForm = { "True", "False" };
			cbAftersaleRelevant.setItems(afterSaleRevDataForm);
			String[] purchaseLevelDataForm = TCExtension.GetLovValues("Vl5_purchase_lvl_vf");
			cbPurchaseLevel.setItems(purchaseLevelDataForm);
			if (bomInfo != null) {
				cbChangeType.setText(bomInfo.get("vf6_change_type"));
				txtPOSID.setText(bomInfo.get("vf6_posid"));
				cbDonorVehicle.setText(bomInfo.get("vf6_donor_vehicle"));
				txtLevel.setText(bomInfo.get("vf6_structure_level"));
				cbSteering.setText(bomInfo.get("vf6_steering"));
				txtQuantity.setText(bomInfo.get("vf6_quantity2"));
				cbMaturityLevel.setText(bomInfo.get("vf6_maturity_level"));
				cbPurchaseLevel.setText(bomInfo.get("vf6_purchase_level"));
				txtPartNo.setText(bomInfo.get("vf6_part_number"));
				txtOldRev.setText(bomInfo.get("vf6_old_version"));
				txtFrozenRev.setText(bomInfo.get("vf6_frozen_revision"));
				txtNewRev.setText(bomInfo.get("vf6_new_revision"));
				txtPartName.setText(bomInfo.get("vf6_part_name"));
				txtOriginalPart.setText(bomInfo.get("vf6_original_base_part"));
				txtVariantFormula.setText(bomInfo.get("vf6_variant_formula"));
				txtTorqueInfo.setText(bomInfo.get("vf6_torque_information"));
				txtWeight.setText(bomInfo.get("vf6_weight"));
				txtChangeDesc.setText(bomInfo.get("vf6_change_description"));
				ckb3DData.setSelection(bomInfo.get("vf6_3d_data_affected").compareTo("Yes") == 0);
				txtMaterial.setText(bomInfo.get("vf6_material"));
				txtCoating.setText(bomInfo.get("vf6_cad_coating"));
				txtSpecbook.setText(bomInfo.get("vf6_specbook"));
				txtSupplier.setText(bomInfo.get("vf6_supplier"));
				txtSupplierContact.setText(bomInfo.get("vf6_supplier_contact"));
				cbAftersaleRelevant.setText(bomInfo.get("vf6_is_aftersale_relevaant"));
				cbExchangeability.setText(bomInfo.get("vf6_exchangeability"));
				cbPartTraceability.setText(bomInfo.get("vf6_part_tracebility"));
				disableUI();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disableUI() {
//		cbChangeType.setEnabled(false);
		txtPOSID.setEnabled(false);
		cbDonorVehicle.setEnabled(false);
		txtLevel.setEnabled(false);
//		cbSteering.setEnabled(false);
		txtQuantity.setEnabled(false);
//		cbMaturityLevel.setEnabled(false);
		cbPurchaseLevel.setEnabled(false);
		txtPartNo.setEnabled(false);
//		txtOldRev.setEnabled(false);
//		txtFrozenRev.setEnabled(false);
		txtNewRev.setEnabled(false);
		txtPartName.setEnabled(false);
		txtOriginalPart.setEnabled(false);
		txtVariantFormula.setEnabled(false);
		txtTorqueInfo.setEnabled(false);
		txtWeight.setEnabled(false);
//		txtChangeDesc.setEnabled(false);
//		ckb3DData.setEnabled(false);
		txtMaterial.setEnabled(false);
		txtCoating.setEnabled(false);
		txtSpecbook.setEnabled(false);
		txtSupplier.setEnabled(false);
//		txtSupplierContact.setEnabled(false);

		if (cbAftersaleRelevant.getText().isEmpty())
			cbAftersaleRelevant.setEnabled(false);
//		cbExchangeability.setEnabled(false);

		if (cbPartTraceability.getText().isEmpty())
			cbPartTraceability.setEnabled(false);
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
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update Bom Info...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 800);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
