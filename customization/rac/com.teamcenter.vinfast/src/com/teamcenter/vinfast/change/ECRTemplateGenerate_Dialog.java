package com.teamcenter.vinfast.change;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.List;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ECRTemplateGenerate_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	private Label lblSupplier;
	private Label lblVehicleCostDelta;
	private Label lblPieceCostDelta;
	private Label lblToolingCostDelta;
	private Label lblEddCostDelta;
	private Label lblLeadtimeImpact;
	private Label lblPtosopTimeImpact;
	private Label lblHardTkoorderMaterial;
	private Label lblHardTkocutSteel;
	private Label lblProblem;
	public Spinner txtVehicleCost;
	public Spinner txtPieceCost;
	public Spinner txtToolingCost;
	public Spinner txtEDDCost;
	public Combo cbTKO01;
	public Combo cbTKO02;
	public Text txtLeadtime;
	public Text txtPTOSOPTime;
	public Text txtProblem;
	private Label lblRootCause;
	private Label lblSolution;
	public Text txtRootCause;
	public Text txtSolution;
	private TabFolder tabFolder;
	private TabItem tbtmImpact;
	private Composite composite;
	public Text txtSupplier;
	private TabItem tbtmNewItem;
	private Composite composite_1;
	private Label lblEcrno;
	private Label lblEcrTriggeredDate;
	private Label lblEcrContactEmail;
	public Text txtECRNo;
	public Text txtContactEmail;
	private Label lblProgram;
	private Label lblModuleGroup;
	public Combo cbProgram;
	public Combo cbModuleGroup;
	private Label lblSilNo;
	private Label lblDcrNo;
	private Label lblImplementationDate;
	private Label lblCoordinatedChange;
	private Label lblNewLabel;
	private Label lblVehicleType;
	public Button ckbMarket_VN;
	public Button ckbMarket_SG;
	public Button ckbMarket_UK;
	public Button ckbMarket_AU;
	public Button ckbMarket_THA;
	public Button ckbMarket_CA;
	public Button ckbMarket_EU;
	public Button ckbMarket_US;
	public Button ckbVehicleType_ICE;
	public Button ckbVehicleType_BEV;
	private Label lblDrivingPostition;
	public Button rbtSeat_5;
	public Button rbtSeat_6;
	public Button rbtSeat_7;
	private Label lblVariant;
	private Label lblDrivingPosition;
	public Button ckbDrivingPostion_LHD;
	public Button ckbDrivingPostion_RHD;
	private TabItem tbtmNewItem_1;
	private Composite composite_2;
	private Label lblNewLabel_1;
	public Button ckbModule_BIW;
	public Button ckbModule_DAC;
	public Button ckbModule_INT;
	public Button ckbModule_EXT;
	public Button ckbModule_EAE;
	public Button ckbModule_CHA;
	public Button ckbModule_POW;
	public Button ckbModule_BAT;
	public Button ckbModule_EMT;
	public Button ckbModule_BMW;
	private Label lblNewLabel_2;
	public Button ckbFunction_DirectPurchasing;
	public Button ckbFunction_IndirectPurchasing;
	public Button ckbFunction_Homolo;
	public Button ckbFunction_AfterSale;
	public Button ckbFunction_Styling;
	public Button ckbFunction_Safety;
	public Button ckbFunction_Standard;
	public Text txtSILNo;
	public Text txtDCRNo;
	public DateTime datImplementationDate;
	public Text txtCoordinatedChange;
	public Button btnVariantRemove;
	public Button btnVariantAdd;
	public Combo cbVariant;
	public List lstVariant;
	public DateTime datTriggerDate;
	public Button btnSearchECR;
	private TabItem tbtmNewItem_2;
	private Composite composite_3;
	public Table tblBom;
	public Button btnAddBomline;
	public Button btnRemoveBomline;
	private TableColumn tblclmnPosid;
	private TableColumn tblclmnChangeType;
	private TableColumn tblclmnDonorVehicle;
	private TableColumn tblclmnLevel;
	private TableColumn tblclmnPurchaseLevel;
	private TableColumn tblclmnQuantity;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnPartName;
	private TableColumn tblclmnNewRevision;
	private TableColumn tblclmnOldRevision;
	private TableColumn tblclmnOriginalPart;
	
	public ECRTemplateGenerate_Dialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				{
					tbtmImpact = new TabItem(tabFolder, SWT.NONE);
					tbtmImpact.setText("Impact");
					{
						composite = new Composite(tabFolder, SWT.NONE);
						tbtmImpact.setControl(composite);
						composite.setLayout(new GridLayout(2, false));
						{
							lblSupplier = new Label(composite, SWT.NONE);
							lblSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblSupplier.setText("Supplier:");
						}
						{
							txtSupplier = new Text(composite, SWT.BORDER);
							txtSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblVehicleCostDelta = new Label(composite, SWT.NONE);
							lblVehicleCostDelta.setText("Vehicle cost delta:");
						}
						{
							txtVehicleCost = new Spinner(composite, SWT.BORDER);
							txtVehicleCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblPieceCostDelta = new Label(composite, SWT.NONE);
							lblPieceCostDelta.setText("Piece cost delta:");
						}
						{
							txtPieceCost = new Spinner(composite, SWT.BORDER);
							txtPieceCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblToolingCostDelta = new Label(composite, SWT.NONE);
							lblToolingCostDelta.setText("Tooling cost delta:");
						}
						{
							txtToolingCost = new Spinner(composite, SWT.BORDER);
							txtToolingCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblEddCostDelta = new Label(composite, SWT.NONE);
							lblEddCostDelta.setText("ED&D cost delta:");
						}
						{
							txtEDDCost = new Spinner(composite, SWT.BORDER);
							txtEDDCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblLeadtimeImpact = new Label(composite, SWT.NONE);
							lblLeadtimeImpact.setText("Leadtime impact:");
						}
						{
							txtLeadtime = new Text(composite, SWT.BORDER);
							txtLeadtime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblPtosopTimeImpact = new Label(composite, SWT.NONE);
							lblPtosopTimeImpact.setText("PTO/SOP Time impact:");
						}
						{
							txtPTOSOPTime = new Text(composite, SWT.BORDER);
							txtPTOSOPTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblHardTkoorderMaterial = new Label(composite, SWT.NONE);
							lblHardTkoorderMaterial.setText("Hard TKO-01/Order material:");
						}
						{
							cbTKO01 = new Combo(composite, SWT.READ_ONLY);
							cbTKO01.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblHardTkocutSteel = new Label(composite, SWT.NONE);
							lblHardTkocutSteel.setText("Hard TKO-02/Cut steel:");
						}
						{
							cbTKO02 = new Combo(composite, SWT.READ_ONLY);
							cbTKO02.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblProblem = new Label(composite, SWT.NONE);
							lblProblem.setText("Problem:");
						}
						{
							txtProblem = new Text(composite, SWT.BORDER);
							GridData gd_txtProblem = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_txtProblem.heightHint = 40;
							txtProblem.setLayoutData(gd_txtProblem);
						}
						{
							lblRootCause = new Label(composite, SWT.NONE);
							lblRootCause.setText("Root cause:");
						}
						{
							txtRootCause = new Text(composite, SWT.BORDER);
							GridData gd_txtRootCause = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_txtRootCause.heightHint = 40;
							txtRootCause.setLayoutData(gd_txtRootCause);
						}
						{
							lblSolution = new Label(composite, SWT.NONE);
							lblSolution.setText("Solution:");
						}
						{
							txtSolution = new Text(composite, SWT.BORDER);
							GridData gd_txtSolution = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_txtSolution.heightHint = 40;
							txtSolution.setLayoutData(gd_txtSolution);
						}
					}
				}
				{
					tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem.setText("ECR Information");
					{
						composite_1 = new Composite(tabFolder, SWT.NONE);
						tbtmNewItem.setControl(composite_1);
						composite_1.setLayout(new GridLayout(10, false));
						{
							lblEcrno = new Label(composite_1, SWT.NONE);
							lblEcrno.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblEcrno.setText("ECRNo:");
						}
						{
							txtECRNo = new Text(composite_1, SWT.BORDER);
							txtECRNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1));
						}
						{
							btnSearchECR = new Button(composite_1, SWT.NONE);
							btnSearchECR.setToolTipText("");
							btnSearchECR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/ecr_item.png"));
						}
						{
							lblEcrTriggeredDate = new Label(composite_1, SWT.NONE);
							lblEcrTriggeredDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblEcrTriggeredDate.setText("ECR Triggered Date:");
						}
						{
							datTriggerDate = new DateTime(composite_1, SWT.BORDER);
							datTriggerDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 9, 1));
						}
						{
							lblEcrContactEmail = new Label(composite_1, SWT.NONE);
							lblEcrContactEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblEcrContactEmail.setText("ECR Contact Email:");
						}
						{
							txtContactEmail = new Text(composite_1, SWT.BORDER);
							txtContactEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblProgram = new Label(composite_1, SWT.NONE);
							lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblProgram.setText("Program:");
						}
						{
							cbProgram = new Combo(composite_1, SWT.READ_ONLY);
							cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblModuleGroup = new Label(composite_1, SWT.NONE);
							lblModuleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblModuleGroup.setText("Module Group:");
						}
						{
							cbModuleGroup = new Combo(composite_1, SWT.READ_ONLY);
							cbModuleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblSilNo = new Label(composite_1, SWT.NONE);
							lblSilNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblSilNo.setText("SIL No:");
						}
						{
							txtSILNo = new Text(composite_1, SWT.BORDER);
							txtSILNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblDcrNo = new Label(composite_1, SWT.NONE);
							lblDcrNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblDcrNo.setText("DCR No:");
						}
						{
							txtDCRNo = new Text(composite_1, SWT.BORDER);
							txtDCRNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblImplementationDate = new Label(composite_1, SWT.NONE);
							lblImplementationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblImplementationDate.setText("Implementation Date:");
						}
						{
							datImplementationDate = new DateTime(composite_1, SWT.BORDER);
							datImplementationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 9, 1));
						}
						{
							lblCoordinatedChange = new Label(composite_1, SWT.NONE);
							lblCoordinatedChange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblCoordinatedChange.setText("Coordinated Change:");
						}
						{
							txtCoordinatedChange = new Text(composite_1, SWT.BORDER);
							txtCoordinatedChange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 9, 1));
						}
						{
							lblNewLabel = new Label(composite_1, SWT.NONE);
							lblNewLabel.setText("Market:");
						}
						{
							ckbMarket_VN = new Button(composite_1, SWT.CHECK);
							ckbMarket_VN.setText("VN");
						}
						{
							ckbMarket_US = new Button(composite_1, SWT.CHECK);
							ckbMarket_US.setText("US");
						}
						{
							ckbMarket_EU = new Button(composite_1, SWT.CHECK);
							ckbMarket_EU.setText("EU");
						}
						{
							ckbMarket_CA = new Button(composite_1, SWT.CHECK);
							ckbMarket_CA.setText("CA");
						}
						{
							ckbMarket_THA = new Button(composite_1, SWT.CHECK);
							ckbMarket_THA.setText("THA");
						}
						{
							ckbMarket_SG = new Button(composite_1, SWT.CHECK);
							ckbMarket_SG.setText("SG");
						}
						{
							ckbMarket_UK = new Button(composite_1, SWT.CHECK);
							ckbMarket_UK.setText("UK");
						}
						{
							ckbMarket_AU = new Button(composite_1, SWT.CHECK);
							ckbMarket_AU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							ckbMarket_AU.setText("AU");
						}
						new Label(composite_1, SWT.NONE);
						{
							lblVehicleType = new Label(composite_1, SWT.NONE);
							lblVehicleType.setText("Vehicle Type:");
						}
						{
							ckbVehicleType_ICE = new Button(composite_1, SWT.CHECK);
							ckbVehicleType_ICE.setText("ICE");
						}
						{
							ckbVehicleType_BEV = new Button(composite_1, SWT.CHECK);
							ckbVehicleType_BEV.setText("BEV");
						}
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						{
							lblDrivingPosition = new Label(composite_1, SWT.NONE);
							lblDrivingPosition.setText("Driving Position:");
						}
						{
							ckbDrivingPostion_LHD = new Button(composite_1, SWT.CHECK);
							ckbDrivingPostion_LHD.setText("LHD");
						}
						{
							ckbDrivingPostion_RHD = new Button(composite_1, SWT.CHECK);
							ckbDrivingPostion_RHD.setText("RHD");
						}
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						{
							lblDrivingPostition = new Label(composite_1, SWT.NONE);
							lblDrivingPostition.setText("Seat Configuration:");
						}
						{
							rbtSeat_5 = new Button(composite_1, SWT.RADIO);
							rbtSeat_5.setText("5");
						}
						{
							rbtSeat_6 = new Button(composite_1, SWT.RADIO);
							rbtSeat_6.setText("6");
						}
						{
							rbtSeat_7 = new Button(composite_1, SWT.RADIO);
							rbtSeat_7.setText("7");
						}
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						new Label(composite_1, SWT.NONE);
						{
							lblVariant = new Label(composite_1, SWT.NONE);
							lblVariant.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
							lblVariant.setText("Variant:");
						}
						{
							lstVariant = new List(composite_1, SWT.BORDER);
							GridData gd_lstVariant = new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1);
							gd_lstVariant.heightHint = 52;
							lstVariant.setLayoutData(gd_lstVariant);
						}
						{
							btnVariantRemove = new Button(composite_1, SWT.NONE);
							btnVariantRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
							btnVariantRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
						}
						new Label(composite_1, SWT.NONE);
						{
							cbVariant = new Combo(composite_1, SWT.READ_ONLY);
							cbVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1));
						}
						{
							btnVariantAdd = new Button(composite_1, SWT.NONE);
							btnVariantAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							btnVariantAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
						}
					}
				}
				{
					tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem_1.setText("Affection");
					{
						composite_2 = new Composite(tabFolder, SWT.NONE);
						tbtmNewItem_1.setControl(composite_2);
						composite_2.setLayout(new GridLayout(2, false));
						{
							lblNewLabel_1 = new Label(composite_2, SWT.NONE);
							lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblNewLabel_1.setText("Module:");
						}
						{
							ckbModule_BIW = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_BIW.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							ckbModule_BIW.setAlignment(SWT.LEFT);
							ckbModule_BIW.setText("BODY IN WHITE");
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_DAC = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_DAC.setText("DOOR & CLOSURES");
							ckbModule_DAC.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_INT = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_INT.setText("INTERIOR");
							ckbModule_INT.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_EXT = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_EXT.setText("EXTERIOR");
							ckbModule_EXT.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_EAE = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_EAE.setText("ELECTRIC ELECTRONICS");
							ckbModule_EAE.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_CHA = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_CHA.setText("CHASSIS");
							ckbModule_CHA.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_POW = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_POW.setText("POWER TRAIN");
							ckbModule_POW.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_BAT = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_BAT.setText("BATTERY PACK");
							ckbModule_BAT.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_EMT = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_EMT.setText("E-MOTOR");
							ckbModule_EMT.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbModule_BMW = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbModule_BMW.setText("BMW ENGINE");
							ckbModule_BMW.setAlignment(SWT.LEFT);
						}
						{
							lblNewLabel_2 = new Label(composite_2, SWT.NONE);
							lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblNewLabel_2.setText("Function:");
						}
						{
							ckbFunction_DirectPurchasing = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_DirectPurchasing.setText("DIRECT PURCHASING");
							ckbFunction_DirectPurchasing.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_IndirectPurchasing = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_IndirectPurchasing.setText("INDIRECT PURCHASING ( H ) Part");
							ckbFunction_IndirectPurchasing.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_Homolo = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_Homolo.setText("HOMOLOGATION");
							ckbFunction_Homolo.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_AfterSale = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_AfterSale.setText("AFTERSALES - SERVICE");
							ckbFunction_AfterSale.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_Styling = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_Styling.setText("STYLING");
							ckbFunction_Styling.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_Safety = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_Safety.setText("FUNCTIONAL SAFETY");
							ckbFunction_Safety.setAlignment(SWT.LEFT);
						}
						new Label(composite_2, SWT.NONE);
						{
							ckbFunction_Standard = new Button(composite_2, SWT.CHECK | SWT.RIGHT);
							ckbFunction_Standard.setText("STANDARD PART");
							ckbFunction_Standard.setAlignment(SWT.LEFT);
						}
					}
				}
				{
					tbtmNewItem_2 = new TabItem(tabFolder, SWT.NONE);
					tbtmNewItem_2.setText("Bom Info");
					{
						composite_3 = new Composite(tabFolder, SWT.NONE);
						tbtmNewItem_2.setControl(composite_3);
						composite_3.setLayout(new GridLayout(2, false));
						{
							btnAddBomline = new Button(composite_3, SWT.NONE);
							btnAddBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
						}
						{
							btnRemoveBomline = new Button(composite_3, SWT.NONE);
							btnRemoveBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
						}
						{
							tblBom = new Table(composite_3, SWT.BORDER | SWT.FULL_SELECTION);
							tblBom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
							tblBom.setHeaderVisible(true);
							tblBom.setLinesVisible(true);
							{
								tblclmnChangeType = new TableColumn(tblBom, SWT.NONE);
								tblclmnChangeType.setWidth(80);
								tblclmnChangeType.setText("Change Type");
							}
							{
								tblclmnPosid = new TableColumn(tblBom, SWT.NONE);
								tblclmnPosid.setWidth(69);
								tblclmnPosid.setText("POSID");
							}
							{
								tblclmnLevel = new TableColumn(tblBom, SWT.NONE);
								tblclmnLevel.setWidth(55);
								tblclmnLevel.setText("Level");
							}
							{
								tblclmnNewColumn = new TableColumn(tblBom, SWT.NONE);
								tblclmnNewColumn.setWidth(100);
								tblclmnNewColumn.setText("Part No");
							}
							{
								tblclmnPartName = new TableColumn(tblBom, SWT.NONE);
								tblclmnPartName.setWidth(100);
								tblclmnPartName.setText("Part Name");
							}
							{
								tblclmnOriginalPart = new TableColumn(tblBom, SWT.NONE);
								tblclmnOriginalPart.setWidth(86);
								tblclmnOriginalPart.setText("Original Part");
							}
							{
								tblclmnOldRevision = new TableColumn(tblBom, SWT.NONE);
								tblclmnOldRevision.setWidth(55);
								tblclmnOldRevision.setText("Old Rev");
							}
							{
								tblclmnNewRevision = new TableColumn(tblBom, SWT.NONE);
								tblclmnNewRevision.setWidth(59);
								tblclmnNewRevision.setText("New Rev");
							}
							{
								tblclmnDonorVehicle = new TableColumn(tblBom, SWT.NONE);
								tblclmnDonorVehicle.setWidth(100);
								tblclmnDonorVehicle.setText("Donor Vehicle");
							}
							{
								tblclmnPurchaseLevel = new TableColumn(tblBom, SWT.NONE);
								tblclmnPurchaseLevel.setWidth(100);
								tblclmnPurchaseLevel.setText("Purchase Level");
							}
							{
								tblclmnQuantity = new TableColumn(tblBom, SWT.NONE);
								tblclmnQuantity.setWidth(100);
								tblclmnQuantity.setText("Quantity");
							}
						}
					}
				}
			}
		} 
		catch (Exception e) {
			
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Export", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(600, 400);
		super.configureShell(newShell);
		newShell.setText("ECR Information Template Generate");
	}
}
