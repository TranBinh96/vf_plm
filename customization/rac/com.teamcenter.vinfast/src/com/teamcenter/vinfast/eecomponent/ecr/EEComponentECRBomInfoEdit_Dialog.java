package com.teamcenter.vinfast.eecomponent.ecr;

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

public class EEComponentECRBomInfoEdit_Dialog extends Dialog {
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
	private Label lblValue_22;
	private Label lblChangeType;

	public Text txtOldPartNumber;
	public Text txtOldPartRevision;
	public Text txtOldPartSubstitute;
	public Text txtOldPartDesignator;
	public Text txtPartNumber;
	public Text txtPartRevision;
	public Text txtPartName;
	public Text txtQuantity;
	public Text txtMPN;
	public Text txtSubstitute;
	public Text txtDesignator;
	public Text txtChangePoint;
	public Combo cbPartTraceability;
	public Combo cbDisposalMaterial;
	public Combo cbChangeType;

	private String[] changeTypeDataForm = { "NEW", "ADD", "CHANGE", "SWAP", "REMOVE" };
	private ScrolledComposite scrolledComposite;
	private Composite composite;
	public Text txtOldPartName;
	public Text txtOldPartQuantity;
	public Text txtOldPartMPN;
	private Label lblTopPart;
	public Text txtTopPart;

	public EEComponentECRBomInfoEdit_Dialog(Shell parent) {
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

		lblTopPart = new Label(composite, SWT.NONE);
		lblTopPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblTopPart.setText("Top Part:");

		txtTopPart = new Text(composite, SWT.BORDER);
		txtTopPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblChangeType = new Label(composite, SWT.NONE);
		lblChangeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblChangeType.setText("Change Type:");

		cbChangeType = new Combo(composite, SWT.NONE);
		cbChangeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbChangeType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbChangeType.setItems(changeTypeDataForm);

		lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("Change Point:");

		txtChangePoint = new Text(composite, SWT.BORDER);
		txtChangePoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblDonorVehicle = new Label(composite, SWT.NONE);
		lblDonorVehicle.setText("Disposal Materials");
		lblDonorVehicle.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbDisposalMaterial = new Combo(composite, SWT.NONE);
		cbDisposalMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbDisposalMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		lblValue_2 = new Label(composite, SWT.NONE);
		lblValue_2.setText("Old Part Number:");
		lblValue_2.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartNumber = new Text(composite, SWT.BORDER);
		txtOldPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_3 = new Label(composite, SWT.NONE);
		lblValue_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_3.setText("Old Part Name:");
		lblValue_3.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartName = new Text(composite, SWT.BORDER);
		txtOldPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_4 = new Label(composite, SWT.NONE);
		lblValue_4.setText("Old Part Revision:");
		lblValue_4.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartRevision = new Text(composite, SWT.BORDER);
		txtOldPartRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_5 = new Label(composite, SWT.NONE);
		lblValue_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_5.setText("Old Part Quantity:");
		lblValue_5.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartQuantity = new Text(composite, SWT.BORDER);
		txtOldPartQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_6 = new Label(composite, SWT.NONE);
		lblValue_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_6.setText("Old Part MPN:");
		lblValue_6.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartMPN = new Text(composite, SWT.BORDER);
		txtOldPartMPN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblValue_7 = new Label(composite, SWT.NONE);
		lblValue_7.setText("Old Part Substitute:");
		lblValue_7.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartSubstitute = new Text(composite, SWT.BORDER);
		txtOldPartSubstitute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_8 = new Label(composite, SWT.NONE);
		lblValue_8.setText("Old Part Designator:");
		lblValue_8.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOldPartDesignator = new Text(composite, SWT.BORDER);
		txtOldPartDesignator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_9 = new Label(composite, SWT.NONE);
		lblValue_9.setText("Part Number:");
		lblValue_9.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartNumber = new Text(composite, SWT.BORDER);
		txtPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_1 = new Label(composite, SWT.NONE);
		lblValue_1.setText("Part Name:");
		lblValue_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartName = new Text(composite, SWT.BORDER);
		txtPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_10 = new Label(composite, SWT.NONE);
		lblValue_10.setText("Part Revision:");
		lblValue_10.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartRevision = new Text(composite, SWT.BORDER);
		txtPartRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_11 = new Label(composite, SWT.NONE);
		lblValue_11.setText("Quantity:");
		lblValue_11.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtQuantity = new Text(composite, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_12 = new Label(composite, SWT.NONE);
		lblValue_12.setText("MPN:");
		lblValue_12.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtMPN = new Text(composite, SWT.BORDER);
		txtMPN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_13 = new Label(composite, SWT.NONE);
		lblValue_13.setText("Substitute:");
		lblValue_13.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtSubstitute = new Text(composite, SWT.BORDER);
		txtSubstitute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblValue_14 = new Label(composite, SWT.NONE);
		lblValue_14.setText("Designator:");
		lblValue_14.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtDesignator = new Text(composite, SWT.BORDER);
		txtDesignator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

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
			String[] disposalMaterialDataForm = { "No impact", "Running change", "Rework", "Use for other purpose", "Use at the request of EED" };
			cbDisposalMaterial.setItems(disposalMaterialDataForm);
			if (bomInfo != null) {
				txtTopPart.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER));
				cbChangeType.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE));
				txtChangePoint.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_CHANGE_POINT));
				cbDisposalMaterial.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_DISPOSAL_MATERIAL));

				txtOldPartNumber.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER));
				txtOldPartName.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME));
				txtOldPartRevision.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION));
				txtOldPartQuantity.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY));
				txtOldPartMPN.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN));
				txtOldPartSubstitute.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE));
				txtOldPartDesignator.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR));

				txtPartNumber.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_NUMBER));
				txtPartName.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_NAME));
				txtPartRevision.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_REVISION));
				txtQuantity.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY));
				txtMPN.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_MPN));
				txtSubstitute.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE));
				txtDesignator.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR));
				cbPartTraceability.setText(bomInfo.get(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY));
				disableUI();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disableUI() {
		txtTopPart.setEnabled(false);

		txtOldPartNumber.setEnabled(false);
		txtOldPartName.setEnabled(false);
		txtOldPartRevision.setEnabled(false);
		txtOldPartQuantity.setEnabled(false);
		txtOldPartMPN.setEnabled(false);
		txtOldPartSubstitute.setEnabled(false);
		txtOldPartDesignator.setEnabled(false);

		txtPartNumber.setEnabled(false);
		txtPartName.setEnabled(false);
		txtPartRevision.setEnabled(false);
		txtQuantity.setEnabled(false);
		txtMPN.setEnabled(false);
		txtSubstitute.setEnabled(false);
		txtDesignator.setEnabled(false);
		cbPartTraceability.setEnabled(false);
	}

	public void clearUI() {
		txtChangePoint.setText("");
		cbDisposalMaterial.deselectAll();
		txtOldPartNumber.setText("");
		txtOldPartRevision.setText("");
		txtOldPartSubstitute.setText("");
		txtOldPartDesignator.setText("");
		txtPartNumber.setText("");
		txtPartRevision.setText("");
		txtPartName.setText("");
		txtQuantity.setText("");
		txtMPN.setText("");
		txtSubstitute.setText("");
		txtDesignator.setText("");
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
		return new Point(600, 650);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
