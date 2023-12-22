package com.teamcenter.vinfast.general.create;

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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;

public class MEPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Button ckbOpenOnCreate;

	public Text txtID;
	public Text txtName;
	public Text txtDesc;
	public Combo cbModel;
	public Combo cbCategory;
	public Combo cbPartMakeBuy;
	public Combo cbUOM;
	public Combo cbVehicleLine;
	public Text txtPartReference;
	public Text txtPartNameVietnamese;
	public Label lblEsModelvehicleLine;
	public Label lblPartReference;
	public Label lblPartNameVietnamese;
	public Composite comScooter;
	public Composite comCoil;
	private Label lblNewLabel;
	public Combo cbCoilMaterial;
	private Label lblThickness;
	public Spinner txtCoilThickness;
	private Label lblThickness_1;
	private Label lblWidth;
	public Spinner txtCoilWidth;
	private Label lblThickness_2;
	private Label lblIsMotherCoil;
	public Button ckbIsCoilPurchase;
	public CTabFolder tabFolder;
	public CTabItem tbtmScooter;
	public CTabItem tbtmCoil;
	private Label lblMaterial;
	public Combo cbCoilCoating;
	public Label lblNameWarning;
	public Button btnAutoFill;
	private Label lblPartTraceabilityIndicator;
	private Label lblUnitOfMeasure_2;
	private Label lblUnitOfMeasure_3;
	public Combo cbPartTraceability;
	public Combo cbPartCategory;
	public Button rbtIsAfterSaleTrue;
	public Button rbtIsAfterSaleFalse;
	public Label lblSupplierType;
	public Combo cbSupplierType;
	private Label lblLink;
	public Text txtCoilLinkPart;
	public CTabItem tbtmBlank;
	public Composite comBlank;
	public Combo cbBlankMaterial;
	private Label lblNewLabel_1;
	public Combo cbBlankCoating;
	private Label lblNewLabel_2;
	private Label lblNewLabel_3;
	public Spinner txtBlankThickness;
	private Label lblNewLabel_4;
	private Label lblNewLabel_5;
	public Spinner txtBlankWidth;
	private Label lblNewLabel_6;
	private Label lblNewLabel_7;
	public Spinner txtBlankLength;
	private Label lblNewLabel_8;
	private Label lblNewLabel_9;
	public Text txtBlackLinkPart;
	private Label lblIsMotherCoil_1;
	public Button ckbIsBlankPurchase;

	public MEPartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create ME Part");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			// init UI
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblModel = new Label(container, SWT.NONE);
			lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModel.setText("Model: (*)");

			cbModel = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			Label lblSubCategory = new Label(container, SWT.NONE);
			lblSubCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblSubCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblSubCategory.setText("Sub Category: (*)");

			cbCategory = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			Label lblId = new Label(container, SWT.NONE);
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID: (*)");

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			txtID.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			btnAutoFill = new Button(container, SWT.NONE);
			btnAutoFill.setText("Validate");
			new Label(container, SWT.NONE);
			
			lblNameWarning = new Label(container, SWT.NONE);
			lblNameWarning.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			lblNameWarning.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.ITALIC));
			lblNameWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblDescription.setText("Description: (*)");

			txtDesc = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			gd_txtDesc.heightHint = 38;
			txtDesc.setLayoutData(gd_txtDesc);

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setText("Part Make/Buy: (*)");

			cbPartMakeBuy = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			Label lblUnitOfMeasure = new Label(container, SWT.NONE);
			lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUnitOfMeasure.setText("Unit Of Measure: (*)");

			cbUOM = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			
			lblPartTraceabilityIndicator = new Label(container, SWT.NONE);
			lblPartTraceabilityIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartTraceabilityIndicator.setText("Part Traceability Indicator:");
			lblPartTraceabilityIndicator.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartTraceabilityIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			cbPartTraceability = new Combo(container, SWT.READ_ONLY);
			cbPartTraceability.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			
			lblUnitOfMeasure_2 = new Label(container, SWT.NONE);
			lblUnitOfMeasure_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUnitOfMeasure_2.setText("Is After Sale Revelant:");
			lblUnitOfMeasure_2.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblUnitOfMeasure_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			rbtIsAfterSaleTrue = new Button(container, SWT.RADIO);
			rbtIsAfterSaleTrue.setEnabled(false);
			rbtIsAfterSaleTrue.setText("True");
			
			rbtIsAfterSaleFalse = new Button(container, SWT.RADIO);
			rbtIsAfterSaleFalse.setEnabled(false);
			rbtIsAfterSaleFalse.setText("False");
			new Label(container, SWT.NONE);
			
			lblUnitOfMeasure_3 = new Label(container, SWT.NONE);
			lblUnitOfMeasure_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUnitOfMeasure_3.setText("Part Category: (*)");
			lblUnitOfMeasure_3.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblUnitOfMeasure_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			cbPartCategory = new Combo(container, SWT.READ_ONLY);
			cbPartCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			
			lblSupplierType = new Label(container, SWT.NONE);
			lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSupplierType.setText("Supplier Type: (*)");
			lblSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			cbSupplierType = new Combo(container, SWT.READ_ONLY);
			cbSupplierType.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			
			tabFolder = new CTabFolder(container, SWT.BORDER | SWT.FLAT);
			tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
			tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
			
			tbtmScooter = new CTabItem(tabFolder, SWT.NONE);
			tbtmScooter.setText("Scooter");
			
			comScooter = new Composite(tabFolder, SWT.NONE);
			tbtmScooter.setControl(comScooter);
			comScooter.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			comScooter.setLayout(new GridLayout(2, false));
			
			lblEsModelvehicleLine = new Label(comScooter, SWT.NONE);
			lblEsModelvehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblEsModelvehicleLine.setText("ES Model/Vehicle Line: (*)");
			lblEsModelvehicleLine.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblEsModelvehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			cbVehicleLine = new Combo(comScooter, SWT.READ_ONLY);
			cbVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbVehicleLine.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbVehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			lblPartReference = new Label(comScooter, SWT.NONE);
			lblPartReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartReference.setText("Part Reference:");
			lblPartReference.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartReference.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtPartReference = new Text(comScooter, SWT.BORDER);
			txtPartReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			txtPartReference.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtPartReference.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			lblPartNameVietnamese = new Label(comScooter, SWT.NONE);
			lblPartNameVietnamese.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblPartNameVietnamese.setText("Part Name Vietnamese:");
			lblPartNameVietnamese.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartNameVietnamese.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtPartNameVietnamese = new Text(comScooter, SWT.BORDER);
			txtPartNameVietnamese.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			txtPartNameVietnamese.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtPartNameVietnamese.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			tbtmCoil = new CTabItem(tabFolder, SWT.NONE);
			tbtmCoil.setText("Coil");
			
			comCoil = new Composite(tabFolder, SWT.NONE);
			tbtmCoil.setControl(comCoil);
			comCoil.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			comCoil.setLayout(new GridLayout(3, false));
			
			lblNewLabel = new Label(comCoil, SWT.NONE);
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Material: (*)");
			
			cbCoilMaterial = new Combo(comCoil, SWT.NONE | SWT.READ_ONLY);
			cbCoilMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCoilMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblMaterial = new Label(comCoil, SWT.NONE);
			lblMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMaterial.setText("Coating:");
			lblMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			cbCoilCoating = new Combo(comCoil, SWT.READ_ONLY);
			cbCoilCoating.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCoilCoating.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblThickness = new Label(comCoil, SWT.NONE);
			lblThickness.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblThickness.setText("Thickness: (*)");
			
			txtCoilThickness = new Spinner(comCoil, SWT.BORDER);
			txtCoilThickness.setIncrement(10);
			txtCoilThickness.setDigits(2);
			txtCoilThickness.setMaximum(1000000);
			txtCoilThickness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblThickness_1 = new Label(comCoil, SWT.NONE);
			lblThickness_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblThickness_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblThickness_1.setText("(mm)");
			
			lblWidth = new Label(comCoil, SWT.NONE);
			lblWidth.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblWidth.setText("Width: (*)");
			
			txtCoilWidth = new Spinner(comCoil, SWT.BORDER);
			txtCoilWidth.setMaximum(1000000);
			txtCoilWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			lblThickness_2 = new Label(comCoil, SWT.NONE);
			lblThickness_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblThickness_2.setText("(mm)");
			
			lblIsMotherCoil = new Label(comCoil, SWT.NONE);
			lblIsMotherCoil.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblIsMotherCoil.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblIsMotherCoil.setText("Is Purchase Part:");
			
			ckbIsCoilPurchase = new Button(comCoil, SWT.CHECK);
			ckbIsCoilPurchase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
			
			lblLink = new Label(comCoil, SWT.NONE);
			lblLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblLink.setText("Link:");
			lblLink.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtCoilLinkPart = new Text(comCoil, SWT.BORDER);
			txtCoilLinkPart.setEditable(false);
			txtCoilLinkPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			tbtmBlank = new CTabItem(tabFolder, SWT.NONE);
			tbtmBlank.setText("Blank");
			
			comBlank = new Composite(tabFolder, SWT.NONE);
			comBlank.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			tbtmBlank.setControl(comBlank);
			comBlank.setLayout(new GridLayout(3, false));
			
			lblNewLabel_1 = new Label(comBlank, SWT.NONE);
			lblNewLabel_1.setText("Material: (*)");
			lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			cbBlankMaterial = new Combo(comBlank, SWT.READ_ONLY);
			cbBlankMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBlankMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblNewLabel_2 = new Label(comBlank, SWT.NONE);
			lblNewLabel_2.setText("Coating:");
			lblNewLabel_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			cbBlankCoating = new Combo(comBlank, SWT.READ_ONLY);
			cbBlankCoating.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBlankCoating.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblNewLabel_3 = new Label(comBlank, SWT.NONE);
			lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_3.setText("Thickness: (*)");
			lblNewLabel_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtBlankThickness = new Spinner(comBlank, SWT.BORDER);
			txtBlankThickness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtBlankThickness.setMaximum(1000000);
			txtBlankThickness.setIncrement(10);
			txtBlankThickness.setDigits(2);
			
			lblNewLabel_4 = new Label(comBlank, SWT.NONE);
			lblNewLabel_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_4.setText("(mm)");
			lblNewLabel_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			lblNewLabel_6 = new Label(comBlank, SWT.NONE);
			lblNewLabel_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_6.setText("Width: (*)");
			lblNewLabel_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtBlankWidth = new Spinner(comBlank, SWT.BORDER);
			txtBlankWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			txtBlankWidth.setMaximum(1000000);
			
			lblNewLabel_5 = new Label(comBlank, SWT.NONE);
			lblNewLabel_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_5.setText("(mm)");
			lblNewLabel_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			lblNewLabel_7 = new Label(comBlank, SWT.NONE);
			lblNewLabel_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_7.setText("Length: (*)");
			lblNewLabel_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtBlankLength = new Spinner(comBlank, SWT.BORDER);
			txtBlankLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			txtBlankLength.setMaximum(1000000);
			
			lblNewLabel_8 = new Label(comBlank, SWT.NONE);
			lblNewLabel_8.setText("(mm)");
			lblNewLabel_8.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			lblIsMotherCoil_1 = new Label(comBlank, SWT.NONE);
			lblIsMotherCoil_1.setText("Is Purchase Part:");
			lblIsMotherCoil_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			ckbIsBlankPurchase = new Button(comBlank, SWT.CHECK);
			ckbIsBlankPurchase.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			
			lblNewLabel_9 = new Label(comBlank, SWT.NONE);
			lblNewLabel_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_9.setText("Link:");
			lblNewLabel_9.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			txtBlackLinkPart = new Text(comBlank, SWT.BORDER);
			txtBlackLinkPart.setEditable(false);
			txtBlackLinkPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
        ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 750);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("ME Part");
	}
}
