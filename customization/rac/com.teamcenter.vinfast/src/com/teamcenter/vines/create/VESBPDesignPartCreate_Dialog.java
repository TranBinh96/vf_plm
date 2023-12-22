package com.teamcenter.vines.create;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VESBPDesignPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	public Button ckbOpenOnCreate;
	public Label lblID;
	public Label lblName;
	public Label lblDesc;
	public Label lblPartMakeBuy;
	public Label lblPartCategory;
	public Label lblDonorVehicle;
	public Label lblUoM;
	public Label lblLongOrShortLead;
	public Label lblSupplierType;
	public Label lblManufComponent;
	public Text txtID;
	public Text txtSuffID;
	public Text txtName;
	public Text txtDescription;
	public Combo cbPartMakeBuy;
	public Combo cbPartCategory;
	public Combo cbDonorVehicle;
	public Combo cbUoM;
	public Combo cbLongShortLead;
	public Combo cbSupplierType;
	public Button rbtManufTrue;
	public Button rbtManufFalse;
	public Label lblPartTrace;
	public Combo cbPartTraceability;
	public List lstVehicleType;
	public Button btnRemoveCUVType;
	public Combo cbCUVType;
	public Button btnAddCUVType;
	public Label lblVehicleType;
	public TabFolder tabFolder;
	private TabItem tbtmNewPartItem;
	private TabItem tbtmNewItem;
	private Composite composite;
	public Label lblPurchaseLevel;
	public Label lblModuleGroupEnglish;
	public Label lblMainModuleEnglish;
	public Label lblModuleName;
	public Combo cbBOMPurchaseLevel;
	public Combo cbBOMModuleGroupEnglish;
	public Combo cbBOMMainModuleEnglish;
	public Combo cbBOMModuleName;
	public Label lblIsAfterSale;
	public Button rbtIsAfterSaleTrue;
	public Button rbtIsAfterSaleFalse;
	private Composite composite_1;
	public Label lblIsChangePart;
	public Combo cbIsModifiedPart;
	public Text txtOldPartNumber;
	public Label lblOriginalBasePart;
	public Label lblModuleGrp;
	public Label lblType;
	public Combo cbType;
	public Combo cbVehicleType;
	public Button btnVehicleTypeAdd;
	public Button btnVehicleTypeRemove;
	public Label lblBatteryPack;
	public Label lblModel;
	public Combo cbBatteryPack;
	public Combo cbModel;
	public Label lblGroupName;
	public Combo cbGroupName;

	public VESBPDesignPartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {

			tabFolder = new TabFolder(area, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
			{

				tbtmNewPartItem = new TabItem(tabFolder, SWT.NONE);
				tbtmNewPartItem.setText("Part Attributes");
				{
					container = new Composite(tabFolder, SWT.NONE);
					tbtmNewPartItem.setControl(container);
					GridLayout gl_container = new GridLayout(4, false);

					container.setLayout(gl_container);
					container.setLayoutData(new GridData(GridData.FILL_BOTH));
					{
						lblType = new Label(container, SWT.NONE);
						lblType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblType.setText("Type: (*)");
					}
					{
						cbType = new Combo(container, SWT.READ_ONLY);
						cbType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblDonorVehicle = new Label(container, SWT.NONE);
						lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblDonorVehicle.setText("Donor Vehicle: (*)");
					}
					{
						cbDonorVehicle = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblBatteryPack = new Label(container, SWT.NONE);
						lblBatteryPack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblBatteryPack.setText("Battery Pack: (*)");
					}
					{
						cbBatteryPack = new Combo(container, SWT.READ_ONLY);
						cbBatteryPack.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBatteryPack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblModel = new Label(container, SWT.NONE);
						lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModel.setText("Model: (*)");
					}
					{
						cbModel = new Combo(container, SWT.READ_ONLY);
						cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblGroupName = new Label(container, SWT.NONE);
						lblGroupName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblGroupName.setText("Group Name: (*)");
					}
					{
						cbGroupName = new Combo(container, SWT.READ_ONLY);
						cbGroupName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbGroupName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblIsChangePart = new Label(container, SWT.NONE);
						lblIsChangePart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblIsChangePart.setText("Is Changed Part After PR Released: (*)");
					}
					{
						cbIsModifiedPart = new Combo(container, SWT.READ_ONLY);
						cbIsModifiedPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbIsModifiedPart = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
						gd_cbIsModifiedPart.widthHint = 198;
						cbIsModifiedPart.setLayoutData(gd_cbIsModifiedPart);
					}
					new Label(container, SWT.NONE);
					{
						lblOriginalBasePart = new Label(container, SWT.NONE);
						lblOriginalBasePart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblOriginalBasePart.setText("Original Base Part Number: (*)");
					}
					{
						txtOldPartNumber = new Text(container, SWT.BORDER);
						txtOldPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
						txtOldPartNumber.setEnabled(false);
					}
					new Label(container, SWT.NONE);
					{
						lblID = new Label(container, SWT.NONE);
						lblID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblID.setText("ID: (*)");
					}
					{
						txtID = new Text(container, SWT.BORDER);
						txtID.setEditable(false);
						txtID.setEnabled(false);
						txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtID.setTextLimit(11);
						txtID.setToolTipText("Please enter 11 Digit Part Number");
					}
					{
						txtSuffID = new Text(container, SWT.BORDER);
						txtSuffID.setEnabled(false);
						GridData gd_txtSuffID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
						gd_txtSuffID.widthHint = 1;
						txtSuffID.setLayoutData(gd_txtSuffID);
						txtSuffID.setToolTipText("Suffix ID is Auto-Generated based on \"Donor Vehicle\" selection.");
					}
					new Label(container, SWT.NONE);
					{
						lblName = new Label(container, SWT.NONE);
						lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblName.setText("Name: (*)");
					}
					{
						txtName = new Text(container, SWT.BORDER);
						txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblDesc = new Label(container, SWT.NONE);
						lblDesc.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
						lblDesc.setText("Description:");
					}
					{
						txtDescription = new Text(container, SWT.BORDER);
						GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
						gd_txtDescription.heightHint = 40;
						txtDescription.setLayoutData(gd_txtDescription);
					}
					new Label(container, SWT.NONE);
					{
						lblPartMakeBuy = new Label(container, SWT.NONE);
						lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPartMakeBuy.setText("Part Make/Buy: (*)");
					}
					{
						cbPartMakeBuy = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblPartCategory = new Label(container, SWT.NONE);
						lblPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPartCategory.setText("Part Category: (*)");
					}
					{
						cbPartCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblPartTrace = new Label(container, SWT.NONE);
						lblPartTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPartTrace.setText("Part Traceability Indicator:");
					}
					{
						cbPartTraceability = new Combo(container, SWT.READ_ONLY);
						cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblIsAfterSale = new Label(container, SWT.NONE);
						lblIsAfterSale.setText("Is After Sale Revelant:");
					}
					{
						composite_1 = new Composite(container, SWT.NONE);
						GridData gd_composite_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
						gd_composite_1.heightHint = 25;
						composite_1.setLayoutData(gd_composite_1);
						{
							rbtIsAfterSaleTrue = new Button(composite_1, SWT.RADIO);
							rbtIsAfterSaleTrue.setBounds(5, 5, 60, 16);
							rbtIsAfterSaleTrue.setText("True");
						}
						{
							rbtIsAfterSaleFalse = new Button(composite_1, SWT.RADIO);
							rbtIsAfterSaleFalse.setSelection(true);
							rbtIsAfterSaleFalse.setBounds(65, 5, 60, 16);
							rbtIsAfterSaleFalse.setText("False");
							// rbtIsAfterSaleFalse.setSelection(true);
						}
					}
					new Label(container, SWT.NONE);
					{
						lblUoM = new Label(container, SWT.NONE);
						lblUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblUoM.setText("Unit of Measure: (*)");
					}
					{
						cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblLongOrShortLead = new Label(container, SWT.NONE);
						lblLongOrShortLead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblLongOrShortLead.setText("Long Or Short lead:");
					}
					{
						cbLongShortLead = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbLongShortLead.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbLongShortLead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblSupplierType = new Label(container, SWT.NONE);
						lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblSupplierType.setText("Supplier Type:");
					}
					{
						cbSupplierType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
						cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(container, SWT.NONE);
					{
						lblManufComponent = new Label(container, SWT.NONE);
						lblManufComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblManufComponent.setText("Manufacturing Component:");
					}

					Composite composite_2 = new Composite(container, SWT.NONE);
					GridData gd_composite_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
					gd_composite_2.heightHint = 25;
					composite_2.setLayoutData(gd_composite_2);
					{
						rbtManufTrue = new Button(composite_2, SWT.RADIO);
						rbtManufTrue.setBounds(5, 5, 60, 16);
						rbtManufTrue.setText("True");
					}
					{
						rbtManufFalse = new Button(composite_2, SWT.RADIO);
						rbtManufFalse.setBounds(65, 5, 60, 16);
						// rbtManufFalse.setSelection(true);
						rbtManufFalse.setText("False");
					}
					new Label(container, SWT.NONE);
					{
						lblVehicleType = new Label(container, SWT.NONE);
						lblVehicleType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
						lblVehicleType.setText("Vehicle Type:");
					}
					{
						lstVehicleType = new List(container, SWT.BORDER | SWT.V_SCROLL);
						GridData gd_lstVehicleType = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
						gd_lstVehicleType.heightHint = 50;
						lstVehicleType.setLayoutData(gd_lstVehicleType);
						lstVehicleType.setToolTipText("Hold CTRL button to select/deselect multiple value");
					}

					btnVehicleTypeRemove = new Button(container, SWT.NONE);
					btnVehicleTypeRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					btnVehicleTypeRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
					new Label(container, SWT.NONE);
					{
						cbVehicleType = new Combo(container, SWT.READ_ONLY);
						cbVehicleType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbVehicleType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					{
						btnVehicleTypeAdd = new Button(container, SWT.NONE);
						btnVehicleTypeAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
					}
				}
				tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
				tbtmNewItem.setText("BOM Attributes");
				{
					composite = new Composite(tabFolder, SWT.NONE);
					tbtmNewItem.setControl(composite);
					GridLayout gl_container1 = new GridLayout(2, false);
					composite.setLayout(gl_container1);
					composite.setLayoutData(new GridData(GridData.FILL_BOTH));
					{
						lblPurchaseLevel = new Label(composite, SWT.NONE);
						lblPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPurchaseLevel.setText("Purchase Level: (*)");
					}
					{
						cbBOMPurchaseLevel = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbBOMPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBOMPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						lblModuleGroupEnglish = new Label(composite, SWT.NONE);
						lblModuleGroupEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModuleGroupEnglish.setText("Module Group English: (*)");
					}
					{
						cbBOMModuleGroupEnglish = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbBOMModuleGroupEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBOMModuleGroupEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						lblMainModuleEnglish = new Label(composite, SWT.NONE);
						lblMainModuleEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblMainModuleEnglish.setText("Main Module English: (*)");
					}
					{
						cbBOMMainModuleEnglish = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbBOMMainModuleEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBOMMainModuleEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						lblModuleName = new Label(composite, SWT.NONE);
						lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModuleName.setText("Module Name: (*)");
					}
					{
						cbBOMModuleName = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbBOMModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBOMModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
				}
			}

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
		return new Point(600, 850);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("VINES BP Design Part");
	}
}
