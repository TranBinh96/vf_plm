package com.teamcenter.vines.ecr;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VESECRCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	private Composite composite;
	private Composite composite_1;
	private ScrolledComposite scrolledComposite;
	private ScrolledComposite scrolledComposite_1;

	public Button btnCreate;
	public Button ckbOpenOnCreate;
	public Button btnMarketRemove;
	public Button btnMarketAdd;
	public Button btnVariantRemove;
	public Button btnVariantAdd;
	public Button ckbQualityEngineering;
	public Button ckbFunctionSafety;
	public Button ckbHomologation;
	public Button ckbAfterSale;
	public Button ckbManufacturingQuality;
	public Button btnECRRemove;
	public Button btnECRAdd;

	private Group grpSummary;
	private Group grpImpact;

	public Label lblOtherCost;
	public Label lblVehicleCost;
	public Label lblPieceCostDelta;
	public Label lblToolingCost;
	public Label lblEddCostDelta;
	public Label lblLeadtimeImpact;
	public Label lblBuildPhaseTime;
	public Label lblProblem;
	public Label lblRootCause;
	public Label lblSolution;
	public Label lblName;
	public Label lblEcrContact;
	public Label lblEcrContactEmail;
	public Label label_4;
	public Label label_5;
	public Label lblVehicleGroup;
	public Label lblDescription;
	public Label lblImpactedModule;
	public Label lblDFMEA;
	public Label lblModuleGroup;
	public Label lblImplementationDate;
	public Label lblId_6;
	public Label lblId_7;
	public Label lblId_8;
	public Label lblChangeReason;
	public Label lblSupplier;
	public Label lblMarket;
	public Label lblDriving;
	public Label lblVariant;
	public Label lblSILNo;
	public Label lblDCRNo;
	public Label lblECRCategory;
	public Label lblCoordinatedChage;
	public Label lblId_27;
	public Label lblDecisionApproval;
	public Label lblId_29;
	public Label lblId_31;
	public Label lblSpecBook;
	public Label lblPFMEA;
	public Label lblWeight;
	public Label lblId_36;

	public Text txtVehicleCost;
	public Text txtPieceCost;
	public Text txtToolingCost;
	public Text txtEDDCost;
	public Text txtOtherCost;
	public Text txtLeadtime;
	public Text txtMRPLateTime;
	public Text txtCurrentWeight;
	public Text txtNewWeight;

	public Text txtProblem;
	public Text txtRootCause;
	public Text txtSolution;
	public Text txtDecisionComment;
	public Text txtSupplier;
	public Text txtName;
	public Text txtECRContactPerson;
	public Text txtECRContactEmail;
	public Text txtDescription;
	public Text txtSILNo;
	public Text txtDCRNo;
	public Text txtCurrentMaterial;
	public Text txtNewMaterial;

	public Combo cbMarket;
	public Combo cbDriving;
	public Combo cbVariant;
	public Combo cbDecisionApproval;
	public Combo cbProgramGroup;
	public Combo cbModuleGroup;
	public Combo cbImpactedModule;
	public Combo cbECRCategory;
	public Combo cbImplementationDate;
	public Combo cbChangeReason;
	public Combo cbNewPartStatus;
	public Combo cbExchangeNewPart;
	public Combo cbExchangeOldPart;

	public List lstVariant;
	public List lstMarket;
	public List lstCoordinatedChage;

	public DateTime datApprovalDate;
	public DateTime datTargetReleaseDate;
	public Label lblDVP;
	public Label lblECRCategory2;
	public Combo cbECRCategory2;
	public Button ckbSupplierQualityManager;
	public Button ckbTestingValidation;
	public Button ckbDMU;
	public Button ckbCAE;
	public Button ckbManufacturing;
	public Button ckbDCEO;
	public List lstECRCategory2;
	public Button btnECRCategory2Remove;
	public Button btnECRCategory2Add;
	public Label lblSunkCost;
	public Text txtSunkCost;
	public Label lblPlantequipmentCost;
	public Text txtPlantEquipmentCost;
	public Label lblLogisticCost;
	public Text txtLogisticCost;
	public Label lblTestingValidationCost;
	public Text txtTestingValidationCost;
	private Group grpInitCost;
	public Combo cbVehicleCostRequired;
	public Combo cbPieceCostRequired;
	public Combo cbToolingCostRequired;
	public Combo cbEDDCostRequired;
	public Combo cbSunkCostRequired;
	public Combo cbPlantEquipmentCostRequired;
	public Combo cbLogisticCostRequired;
	public Combo cbTestingValidationCostRequired;
	public Combo cbOtherCostRequired;
	private Group grpDocumentation;
	private Group grpChangeDescription;
	public Combo cbSILNoRequired;
	public Combo cbDCRNoRequired;
	public Combo cbCoordinatedChageRequired;
	public Combo cbWeightRequired;
	public Text txtPFMEA;
	public Text txtSpecBook;
	public Text txtDFMEA;
	public Text txtDVP;
	public Combo cbPFMEARequired;
	public Combo cbSpecBookRequired;
	public Combo cbDFMEARequired;
	public Combo cbDVPRequired;
	public Label lblId_9;
	public Label lblId_11;
	public Label lblId_12;
	public Label lblId_13;
	public Combo cbLeadtimeRequired;
	public Combo cbBuildPhaseTimeRequired;
	public Combo cbImpactedModuleRequired;
	public Label lblCOPImpact;
	public Combo cbCOPImpactRequired;
	public List lstCOPImpact;
	public Combo cbCOPImpact;
	public Button btnCOPImpactRemove;
	public Button btnCOPImpactAdd;
	public Button ckbTargetReleaseDate;
	public Button ckbApprovalDate;
	public List lstImpactedModule;
	public Button btnImpactedModuleAdd;
	public Button btnImpactedModuleRemove;
	public Combo cbBuildPhaseTime;
	public List lstImplementationDate;
	public Button btnImplementationDateRemove;
	public Button btnImplementationDateAdd;
	public Label lblIsSendDataToSupplier;
	public Combo cbIsSendDataToSupplier;
	public Label lblMrpTime;
	public Text txtSendDataReason;
	public Label lblDMUCheckFile;
	public Combo cbHaveDMUCheckRequired;
	public Text txtDMUCheckFile;
	public Button btnDMUUpload;
	public Button ckbMGL;
	public Button ckbHTKO2;
	public Button ckbHTKO1;
	public Button ckbDirectPurchase;
	public Button ckbIndirectPurchase;
	public Button ckbCostEngineering;
	public Button ckbSystemArchitectApproval;
	private Label lblNewLabel_1;
	private Label lblNewLabel_2;
	public Label lblReworkCost;
	public Combo cbReworkCostRequired;
	public Text txtReworkCost;
	public Label lblBudgetCost;
	public Combo cbBudgetCostRequired;
	public Text txtBudgetCost;
	public Label lblToolingImpact;
	public Combo cbToolingImpactRequired;
	public Text txtToolingImpact;
	public Label label;
	public Label lblCustomerImpact;
	public Combo cbCustomerImpactRequired;
	public Text txtCustomerImpact;

	public VESECRCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Create Engineering Change Request");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 684;
			container.setLayoutData(gd_container);
			grpSummary = new Group(container, SWT.NONE);

			VerifyListener doubleVerify = new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					Text text = (Text) e.getSource();
					final String oldS = text.getText();
					String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
					try {
						if (!newS.isEmpty() && newS.compareTo("-") != 0) {
							Double.parseDouble(newS);
						}
					} catch (NumberFormatException ex) {
						e.doit = false;
					}
				}
			};

			GridData gd_grpSummary = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
			gd_grpSummary.heightHint = 439;
			gd_grpSummary.widthHint = 808;
			grpSummary.setLayoutData(gd_grpSummary);
			grpSummary.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			grpSummary.setText("SUMMARY");
			GridLayout gl_grpSummary = new GridLayout(1, false);
			gl_grpSummary.horizontalSpacing = 1;
			grpSummary.setLayout(gl_grpSummary);
			{
				scrolledComposite = new ScrolledComposite(grpSummary, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
				gd_scrolledComposite.heightHint = 465;
				scrolledComposite.setLayoutData(gd_scrolledComposite);
				scrolledComposite.setExpandHorizontal(true);
				scrolledComposite.setExpandVertical(true);
				{
					composite = new Composite(scrolledComposite, SWT.NONE);
					composite.setLayout(new GridLayout(9, false));
					{
						lblVehicleGroup = new Label(composite, SWT.NONE);
						lblVehicleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblVehicleGroup.setText("Product Group: (*)");
					}
					{
						cbProgramGroup = new Combo(composite, SWT.READ_ONLY);
						cbProgramGroup.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbProgramGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbProgramGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblEcrContact = new Label(composite, SWT.NONE);
						lblEcrContact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblEcrContact.setText("ECR Contact Person:");
					}
					{
						txtECRContactPerson = new Text(composite, SWT.BORDER);
						txtECRContactPerson.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblName = new Label(composite, SWT.NONE);
						lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblName.setText("ECR Title: (*)");
					}
					{
						txtName = new Text(composite, SWT.BORDER);
						txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblEcrContactEmail = new Label(composite, SWT.NONE);
						lblEcrContactEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblEcrContactEmail.setText("ECR Contact Email: (*)");
					}
					{
						txtECRContactEmail = new Text(composite, SWT.BORDER);
						txtECRContactEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblDescription = new Label(composite, SWT.NONE);
						lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
						lblDescription.setText("Description: (*)");
					}
					{
						txtDescription = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
						GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, false, false, 7, 1);
						gd_txtDescription.heightHint = 35;
						txtDescription.setLayoutData(gd_txtDescription);
					}
					new Label(composite, SWT.NONE);
					{
						lblModuleGroup = new Label(composite, SWT.NONE);
						lblModuleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModuleGroup.setText("Module Group: (*)");
					}
					{
						cbModuleGroup = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbModuleGroup.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbModuleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
						cbModuleGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					new Label(composite, SWT.NONE);
					{
						lblECRCategory = new Label(composite, SWT.NONE);
						lblECRCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblECRCategory.setText("ECR Category: (*)");
					}
					{
						cbECRCategory = new Combo(composite, SWT.READ_ONLY);
						cbECRCategory.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbECRCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbECRCategory.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblImpactedModule = new Label(composite, SWT.NONE);
						lblImpactedModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
						lblImpactedModule.setText("Impacted Other Module: (*)");
					}
					{
						cbImpactedModuleRequired = new Combo(composite, SWT.READ_ONLY);
						cbImpactedModuleRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbImpactedModuleRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbCoordinatedChangeModuleRequired = new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1);
						gd_cbCoordinatedChangeModuleRequired.widthHint = 30;
						cbImpactedModuleRequired.setLayoutData(gd_cbCoordinatedChangeModuleRequired);
					}
					{
						lstImpactedModule = new List(composite, SWT.BORDER);
						GridData gd_lstImpactedModule = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_lstImpactedModule.heightHint = 40;
						lstImpactedModule.setLayoutData(gd_lstImpactedModule);
					}
					{
						btnImpactedModuleRemove = new Button(composite, SWT.NONE);
						btnImpactedModuleRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnImpactedModuleRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
						btnImpactedModuleRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					}
					{
						lblECRCategory2 = new Label(composite, SWT.NONE);
						lblECRCategory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
						lblECRCategory2.setText("ECR Category Level 2: (*)");
					}
					{
						lstECRCategory2 = new List(composite, SWT.BORDER);
						GridData gd_lstECRCategory2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
						gd_lstECRCategory2.heightHint = 40;
						lstECRCategory2.setLayoutData(gd_lstECRCategory2);
					}
					{
						btnECRCategory2Remove = new Button(composite, SWT.NONE);
						btnECRCategory2Remove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnECRCategory2Remove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
						btnECRCategory2Remove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					}
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					{
						cbImpactedModule = new Combo(composite, SWT.READ_ONLY);
						cbImpactedModule.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbImpactedModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						cbImpactedModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					{
						btnImpactedModuleAdd = new Button(composite, SWT.NONE);
						btnImpactedModuleAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnImpactedModuleAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
					}
					{
						cbECRCategory2 = new Combo(composite, SWT.READ_ONLY);
						cbECRCategory2.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbECRCategory2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbECRCategory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					{
						btnECRCategory2Add = new Button(composite, SWT.NONE);
						btnECRCategory2Add.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnECRCategory2Add.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
					}
					{
						lblImplementationDate = new Label(composite, SWT.NONE);
						lblImplementationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblImplementationDate.setText("Implementation Date: (*)");
					}
					{
						lstImplementationDate = new List(composite, SWT.BORDER);
						GridData gd_lstImplementationDate = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
						gd_lstImplementationDate.heightHint = 40;
						lstImplementationDate.setLayoutData(gd_lstImplementationDate);
					}
					{
						btnImplementationDateRemove = new Button(composite, SWT.NONE);
						btnImplementationDateRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnImplementationDateRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
						btnImplementationDateRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					}
					{
						lblChangeReason = new Label(composite, SWT.NONE);
						lblChangeReason.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
						lblChangeReason.setText("Change Reason: (*)");
					}
					{
						cbChangeReason = new Combo(composite, SWT.READ_ONLY);
						cbChangeReason.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbChangeReason.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));
						cbChangeReason.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					{
						cbImplementationDate = new Combo(composite, SWT.READ_ONLY);
						cbImplementationDate.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbImplementationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
						cbImplementationDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					{
						btnImplementationDateAdd = new Button(composite, SWT.NONE);
						btnImplementationDateAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnImplementationDateAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
					}
					{
						lblId_31 = new Label(composite, SWT.NONE);
						lblId_31.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblId_31.setText("New Part Status:");
					}
					{
						cbNewPartStatus = new Combo(composite, SWT.READ_ONLY);
						cbNewPartStatus.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbNewPartStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbNewPartStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblId_6 = new Label(composite, SWT.NONE);
						lblId_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblId_6.setText("Target Release Date:");
					}
					{
						ckbTargetReleaseDate = new Button(composite, SWT.CHECK);
						ckbTargetReleaseDate.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						ckbTargetReleaseDate.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								updateDateTimeControl();
							}
						});
					}
					{
						datTargetReleaseDate = new DateTime(composite, SWT.BORDER);
						datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblDCRNo = new Label(composite, SWT.NONE);
						lblDCRNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblDCRNo.setText("DCR No: (*)");
					}
					{
						cbDCRNoRequired = new Combo(composite, SWT.READ_ONLY);
						cbDCRNoRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbDCRNoRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbDCRNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbDCRNo.widthHint = 30;
						cbDCRNoRequired.setLayoutData(gd_cbDCRNo);
					}
					{
						txtDCRNo = new Text(composite, SWT.BORDER);
						txtDCRNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblId_7 = new Label(composite, SWT.NONE);
						lblId_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblId_7.setText("Exchangeability New Part:");
					}
					{
						cbExchangeNewPart = new Combo(composite, SWT.NONE);
						cbExchangeNewPart.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbExchangeNewPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
						cbExchangeNewPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					new Label(composite, SWT.NONE);
					{
						lblSILNo = new Label(composite, SWT.NONE);
						lblSILNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblSILNo.setText("SIL/IMS No: (*)");
					}
					{
						cbSILNoRequired = new Combo(composite, SWT.READ_ONLY);
						cbSILNoRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbSILNoRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbSILNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbSILNo.widthHint = 30;
						cbSILNoRequired.setLayoutData(gd_cbSILNo);
					}
					{
						txtSILNo = new Text(composite, SWT.BORDER);
						txtSILNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblId_8 = new Label(composite, SWT.NONE);
						lblId_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblId_8.setText("Exchangeability Old Part:");
					}
					{
						cbExchangeOldPart = new Combo(composite, SWT.NONE);
						cbExchangeOldPart.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbExchangeOldPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
						cbExchangeOldPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					new Label(composite, SWT.NONE);
					{
						lblCoordinatedChage = new Label(composite, SWT.NONE);
						lblCoordinatedChage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblCoordinatedChage.setText("Coordinated Change: (*)");
					}
					{
						cbCoordinatedChageRequired = new Combo(composite, SWT.READ_ONLY);
						cbCoordinatedChageRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbCoordinatedChageRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbCoordinatedChage = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbCoordinatedChage.widthHint = 30;
						cbCoordinatedChageRequired.setLayoutData(gd_cbCoordinatedChage);
					}
					{
						lstCoordinatedChage = new List(composite, SWT.BORDER);
						GridData gd_lstCoordinatedChage = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2);
						gd_lstCoordinatedChage.heightHint = 45;
						lstCoordinatedChage.setLayoutData(gd_lstCoordinatedChage);
					}
					{
						btnECRRemove = new Button(composite, SWT.NONE);
						btnECRRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnECRRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					}
					{
						lblDMUCheckFile = new Label(composite, SWT.NONE);
						lblDMUCheckFile.setText("Have DMU Check Result: (*)");
					}
					{
						cbHaveDMUCheckRequired = new Combo(composite, SWT.READ_ONLY);
						cbHaveDMUCheckRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbHaveDMUCheckRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbHaveDMUCheckRequired = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
						gd_cbHaveDMUCheckRequired.widthHint = 30;
						cbHaveDMUCheckRequired.setLayoutData(gd_cbHaveDMUCheckRequired);
					}
					{
						txtDMUCheckFile = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
						txtDMUCheckFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						txtDMUCheckFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						btnDMUUpload = new Button(composite, SWT.NONE);
						btnDMUUpload.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/importobjects_16.png"));
						btnDMUUpload.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
					}
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					{
						btnECRAdd = new Button(composite, SWT.NONE);
						btnECRAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						btnECRAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/ecr_item.png"));
					}
					{
						lblIsSendDataToSupplier = new Label(composite, SWT.NONE);
						lblIsSendDataToSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblIsSendDataToSupplier.setText("Is Send Data To Supplier: (*)");
					}
					{
						cbIsSendDataToSupplier = new Combo(composite, SWT.READ_ONLY);
						cbIsSendDataToSupplier.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbIsSendDataToSupplier.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbIsSendDataToSupplier = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
						gd_cbIsSendDataToSupplier.widthHint = 30;
						cbIsSendDataToSupplier.setLayoutData(gd_cbIsSendDataToSupplier);
					}
					{
						txtSendDataReason = new Text(composite, SWT.BORDER);
						txtSendDataReason.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
					new Label(composite, SWT.NONE);
				}
				scrolledComposite.setContent(composite);
				scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}

			Group grpPrecab = new Group(container, SWT.NONE);
			GridData gd_grpPrecab = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd_grpPrecab.widthHint = 407;
			grpPrecab.setLayoutData(gd_grpPrecab);
			grpPrecab.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
			grpPrecab.setText("PRECAB");
			grpPrecab.setLayout(new GridLayout(4, false));
			{
				lblMarket = new Label(grpPrecab, SWT.NONE);
				lblMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
				lblMarket.setText("Market: (*)");
			}

			lstMarket = new List(grpPrecab, SWT.BORDER);
			GridData gd_lstMarket = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_lstMarket.heightHint = 40;
			lstMarket.setLayoutData(gd_lstMarket);
			{
				btnMarketRemove = new Button(grpPrecab, SWT.NONE);
				btnMarketRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				btnMarketRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnMarketRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			{
				cbMarket = new Combo(grpPrecab, SWT.READ_ONLY);
				cbMarket.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				btnMarketAdd = new Button(grpPrecab, SWT.NONE);
				btnMarketAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				btnMarketAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblDriving = new Label(grpPrecab, SWT.NONE);
				lblDriving.setText("LHD/RHD: (*)");
			}
			{
				cbDriving = new Combo(grpPrecab, SWT.READ_ONLY);
				cbDriving.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbDriving.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDriving.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblVariant = new Label(grpPrecab, SWT.NONE);
				lblVariant.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
				lblVariant.setText("Variant:");
			}
			{
				lstVariant = new List(grpPrecab, SWT.BORDER);
				GridData gd_lstVariant = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_lstVariant.heightHint = 40;
				lstVariant.setLayoutData(gd_lstVariant);
			}
			{
				btnVariantRemove = new Button(grpPrecab, SWT.NONE);
				btnVariantRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				btnVariantRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnVariantRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			{
				cbVariant = new Combo(grpPrecab, SWT.READ_ONLY);
				cbVariant.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbVariant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				btnVariantAdd = new Button(grpPrecab, SWT.NONE);
				btnVariantAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				btnVariantAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblDecisionApproval = new Label(grpPrecab, SWT.NONE);
				lblDecisionApproval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDecisionApproval.setText("Pre-cab decision:");
			}
			{
				cbDecisionApproval = new Combo(grpPrecab, SWT.NONE);
				cbDecisionApproval.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbDecisionApproval.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDecisionApproval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblId_27 = new Label(grpPrecab, SWT.NONE);
				lblId_27.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_27.setText("Decision Comment:");
			}
			{
				txtDecisionComment = new Text(grpPrecab, SWT.BORDER);
				txtDecisionComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblId_29 = new Label(grpPrecab, SWT.NONE);
				lblId_29.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_29.setText("Approval Date:");
			}
			{
				ckbApprovalDate = new Button(grpPrecab, SWT.CHECK);
				ckbApprovalDate.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				ckbApprovalDate.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateDateTimeControl();
					}
				});
			}
			{
				datApprovalDate = new DateTime(grpPrecab, SWT.BORDER);
				datApprovalDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				grpImpact = new Group(container, SWT.NONE);
				GridData gd_grpImpact = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3);
				gd_grpImpact.widthHint = 497;
				grpImpact.setLayoutData(gd_grpImpact);
				grpImpact.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
				grpImpact.setLayout(new GridLayout(1, false));
				grpImpact.setText("IMPACT");
				{
					scrolledComposite_1 = new ScrolledComposite(grpImpact, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
					GridData gd_scrolledComposite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_scrolledComposite_1.heightHint = 68;
					scrolledComposite_1.setLayoutData(gd_scrolledComposite_1);
					scrolledComposite_1.setExpandHorizontal(true);
					scrolledComposite_1.setExpandVertical(true);
					{
						composite_1 = new Composite(scrolledComposite_1, SWT.NONE);
						composite_1.setLayout(new GridLayout(4, false));
						{
							lblSupplier = new Label(composite_1, SWT.NONE);
							lblSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblSupplier.setText("Supplier: (*)");
						}
						{
							txtSupplier = new Text(composite_1, SWT.BORDER);
							txtSupplier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
						}
						{
							lblCOPImpact = new Label(composite_1, SWT.NONE);
							lblCOPImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
							lblCOPImpact.setText("COP Impact: (*)");
						}
						{
							cbCOPImpactRequired = new Combo(composite_1, SWT.READ_ONLY);
							cbCOPImpactRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbCOPImpactRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							GridData gd_cbCOPImpact = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
							gd_cbCOPImpact.widthHint = 30;
							cbCOPImpactRequired.setLayoutData(gd_cbCOPImpact);
						}
						{
							lstCOPImpact = new List(composite_1, SWT.BORDER);
							GridData gd_lstCOPImpact = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_lstCOPImpact.heightHint = 40;
							lstCOPImpact.setLayoutData(gd_lstCOPImpact);
						}
						{
							btnCOPImpactRemove = new Button(composite_1, SWT.NONE);
							btnCOPImpactRemove.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							btnCOPImpactRemove.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
							btnCOPImpactRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
						}
						new Label(composite_1, SWT.NONE);
						{
							cbCOPImpact = new Combo(composite_1, SWT.READ_ONLY);
							cbCOPImpact.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbCOPImpact.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbCOPImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							btnCOPImpactAdd = new Button(composite_1, SWT.NONE);
							btnCOPImpactAdd.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							btnCOPImpactAdd.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
							btnCOPImpactAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
						}
						{
							lblLeadtimeImpact = new Label(composite_1, SWT.NONE);
							lblLeadtimeImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblLeadtimeImpact.setText("Leadtime impact: (*)");
						}
						{
							cbLeadtimeRequired = new Combo(composite_1, SWT.READ_ONLY);
							cbLeadtimeRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbLeadtimeRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							GridData gd_cbLeadtime = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_cbLeadtime.widthHint = 30;
							cbLeadtimeRequired.setLayoutData(gd_cbLeadtime);
						}
						{
							txtLeadtime = new Text(composite_1, SWT.BORDER);
							txtLeadtime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							txtLeadtime.addVerifyListener(doubleVerify);
						}
						{
							label_4 = new Label(composite_1, SWT.NONE);
							label_4.setText("(week)");
						}
						{
							lblBuildPhaseTime = new Label(composite_1, SWT.NONE);
							lblBuildPhaseTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblBuildPhaseTime.setText("Build Phase Time Impact: (*)");
						}
						{
							cbBuildPhaseTimeRequired = new Combo(composite_1, SWT.READ_ONLY);
							cbBuildPhaseTimeRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbBuildPhaseTimeRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							GridData gd_cbPTOSOPtime = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
							gd_cbPTOSOPtime.widthHint = 30;
							cbBuildPhaseTimeRequired.setLayoutData(gd_cbPTOSOPtime);
						}
						{
							cbBuildPhaseTime = new Combo(composite_1, SWT.READ_ONLY);
							cbBuildPhaseTime.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbBuildPhaseTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbBuildPhaseTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						new Label(composite_1, SWT.NONE);
						{
							lblMrpTime = new Label(composite_1, SWT.NONE);
							lblMrpTime.setText("Late MRP Time:");
						}
						new Label(composite_1, SWT.NONE);
						{
							txtMRPLateTime = new Text(composite_1, SWT.BORDER);
							txtMRPLateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							txtMRPLateTime.addVerifyListener(doubleVerify);
						}
						{
							label_5 = new Label(composite_1, SWT.NONE);
							label_5.setText("(week)");
						}
						{
							lblToolingImpact = new Label(composite_1, SWT.NONE);
							lblToolingImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblToolingImpact.setText("Tooling Impact: (*)");
						}
						{
							cbToolingImpactRequired = new Combo(composite_1, SWT.READ_ONLY);
							cbToolingImpactRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbToolingImpactRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbToolingImpactRequired.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							txtToolingImpact = new Text(composite_1, SWT.BORDER);
							txtToolingImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							txtToolingImpact.addVerifyListener(doubleVerify);
						}
						{
							label = new Label(composite_1, SWT.NONE);
							label.setText("(week)");
						}
						{
							lblCustomerImpact = new Label(composite_1, SWT.NONE);
							lblCustomerImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblCustomerImpact.setText("Customer Impact: (*)");
						}
						{
							cbCustomerImpactRequired = new Combo(composite_1, SWT.READ_ONLY);
							cbCustomerImpactRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
							cbCustomerImpactRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbCustomerImpactRequired.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							txtCustomerImpact = new Text(composite_1, SWT.BORDER);
							txtCustomerImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						new Label(composite_1, SWT.NONE);
						{
							lblProblem = new Label(composite_1, SWT.NONE);
							lblProblem.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
							lblProblem.setText("Problem: (*)");
						}
						{
							txtProblem = new Text(composite_1, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
							GridData gd_txtProblem = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
							gd_txtProblem.heightHint = 70;
							txtProblem.setLayoutData(gd_txtProblem);
						}
						{
							lblRootCause = new Label(composite_1, SWT.NONE);
							lblRootCause.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
							lblRootCause.setText("Root cause: (*)");
						}
						{
							txtRootCause = new Text(composite_1, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
							GridData gd_txtRootCause = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
							gd_txtRootCause.heightHint = 70;
							txtRootCause.setLayoutData(gd_txtRootCause);
						}
						{
							lblSolution = new Label(composite_1, SWT.NONE);
							lblSolution.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
							lblSolution.setText("Solution: (*)");
						}
						{
							txtSolution = new Text(composite_1, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
							GridData gd_txtSolution = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
							gd_txtSolution.heightHint = 70;
							txtSolution.setLayoutData(gd_txtSolution);
						}
					}
					scrolledComposite_1.setContent(composite_1);
					scrolledComposite_1.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
			{
				grpChangeDescription = new Group(container, SWT.NONE);
				grpChangeDescription.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_MAGENTA));
				grpChangeDescription.setText("CHANGE DESCRIPTION");
				grpChangeDescription.setLayout(new GridLayout(5, false));
				grpChangeDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
				{
					lblId_36 = new Label(grpChangeDescription, SWT.NONE);
					lblId_36.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
					lblId_36.setText("Material:");
				}
				{
					lblId_9 = new Label(grpChangeDescription, SWT.NONE);
					lblId_9.setText("Current");
					lblId_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				}
				{
					txtCurrentMaterial = new Text(grpChangeDescription, SWT.BORDER);
					txtCurrentMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				}
				{
					lblId_11 = new Label(grpChangeDescription, SWT.NONE);
					lblId_11.setText("New");
					lblId_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				}
				{
					txtNewMaterial = new Text(grpChangeDescription, SWT.BORDER);
					txtNewMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				}
				{
					lblWeight = new Label(grpChangeDescription, SWT.NONE);
					lblWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
					lblWeight.setText("Weight: (*)");
				}
				{
					cbWeightRequired = new Combo(grpChangeDescription, SWT.READ_ONLY);
					cbWeightRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
					cbWeightRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					GridData gd_cbWeight = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2);
					gd_cbWeight.widthHint = 30;
					cbWeightRequired.setLayoutData(gd_cbWeight);
				}
				{
					lblId_12 = new Label(grpChangeDescription, SWT.NONE);
					lblId_12.setText("Current");
					lblId_12.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				}
				txtCurrentWeight = new Text(grpChangeDescription, SWT.BORDER);
				txtCurrentWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				txtCurrentWeight.addVerifyListener(doubleVerify);
				{
					lblNewLabel_1 = new Label(grpChangeDescription, SWT.NONE);
					lblNewLabel_1.setText("(kg)");
				}
				{
					lblId_13 = new Label(grpChangeDescription, SWT.NONE);
					lblId_13.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					lblId_13.setText("New");
				}
				txtNewWeight = new Text(grpChangeDescription, SWT.BORDER);
				txtNewWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				{
					lblNewLabel_2 = new Label(grpChangeDescription, SWT.NONE);
					lblNewLabel_2.setText("(kg)");
				}
				txtNewWeight.addVerifyListener(doubleVerify);
			}
			{
				grpDocumentation = new Group(container, SWT.NONE);
				grpDocumentation.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_CYAN));
				grpDocumentation.setText("REVIEW GATE");
				grpDocumentation.setLayout(new GridLayout(3, false));
				grpDocumentation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
				{
					ckbDMU = new Button(grpDocumentation, SWT.CHECK);
					ckbDMU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					ckbDMU.setText("DMU");
				}
				{
					ckbSystemArchitectApproval = new Button(grpDocumentation, SWT.CHECK);
					ckbSystemArchitectApproval.setText("System Architect Approval");
				}
				{
					ckbMGL = new Button(grpDocumentation, SWT.CHECK);
					ckbMGL.setText("MGL");
				}
				{
					ckbQualityEngineering = new Button(grpDocumentation, SWT.CHECK);
					ckbQualityEngineering.setText("Quality Engineering");
					ckbQualityEngineering.setTouchEnabled(true);
				}
				{
					ckbManufacturingQuality = new Button(grpDocumentation, SWT.CHECK);
					ckbManufacturingQuality.setText("Manufacturing Quality");
				}
				{
					ckbManufacturing = new Button(grpDocumentation, SWT.CHECK);
					ckbManufacturing.setText("Manufacturing");
				}
				{
					ckbTestingValidation = new Button(grpDocumentation, SWT.CHECK);
					ckbTestingValidation.setText("Testing and Validation");
				}
				{
					ckbCAE = new Button(grpDocumentation, SWT.CHECK);
					ckbCAE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					ckbCAE.setText("CAE");
				}
				{
					ckbHomologation = new Button(grpDocumentation, SWT.CHECK);
					ckbHomologation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					ckbHomologation.setText("Homologation");
				}
				{
					ckbAfterSale = new Button(grpDocumentation, SWT.CHECK);
					ckbAfterSale.setText("After Sale/Service");
				}
				{
					ckbFunctionSafety = new Button(grpDocumentation, SWT.CHECK);
					ckbFunctionSafety.setText("Function Safety");
				}
				{
					ckbCostEngineering = new Button(grpDocumentation, SWT.CHECK);
					ckbCostEngineering.setText("Cost Engineering");
				}
				{
					ckbDirectPurchase = new Button(grpDocumentation, SWT.CHECK);
					ckbDirectPurchase.setText("Direct Purchase");
				}
				{
					ckbIndirectPurchase = new Button(grpDocumentation, SWT.CHECK);
					ckbIndirectPurchase.setText("In-Direct Purchase");
				}
				{
					ckbSupplierQualityManager = new Button(grpDocumentation, SWT.CHECK);
					ckbSupplierQualityManager.setText("Supplier Quality Manager");
				}
				{
					ckbHTKO1 = new Button(grpDocumentation, SWT.CHECK);
					ckbHTKO1.setText("HTKO1");
				}
				{
					ckbHTKO2 = new Button(grpDocumentation, SWT.CHECK);
					ckbHTKO2.setText("HTKO2");
				}
				{
					ckbDCEO = new Button(grpDocumentation, SWT.CHECK);
					ckbDCEO.setText("DCEO RD");
				}
			}

			Group grpReviewGate = new Group(container, SWT.NONE);
			grpReviewGate.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
			grpReviewGate.setText("DOCUMENTATION");
			grpReviewGate.setLayout(new GridLayout(3, false));
			grpReviewGate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			{
				lblSpecBook = new Label(grpReviewGate, SWT.NONE);
				lblSpecBook.setText("Spec Book: (*)");
			}
			{
				cbSpecBookRequired = new Combo(grpReviewGate, SWT.READ_ONLY);
				GridData gd_cbSpecBookRequired = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_cbSpecBookRequired.widthHint = 30;
				cbSpecBookRequired.setLayoutData(gd_cbSpecBookRequired);
				cbSpecBookRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbSpecBookRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtSpecBook = new Text(grpReviewGate, SWT.BORDER);
				txtSpecBook.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblPFMEA = new Label(grpReviewGate, SWT.NONE);
				lblPFMEA.setText("PFMEA: (*)");
			}
			{
				cbPFMEARequired = new Combo(grpReviewGate, SWT.READ_ONLY);
				GridData gd_cbPFMEARequired = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_cbPFMEARequired.widthHint = 30;
				cbPFMEARequired.setLayoutData(gd_cbPFMEARequired);
				cbPFMEARequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbPFMEARequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtPFMEA = new Text(grpReviewGate, SWT.BORDER);
				txtPFMEA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				lblDFMEA = new Label(grpReviewGate, SWT.NONE);
				lblDFMEA.setText("DFMEA: (*)");
			}
			{
				cbDFMEARequired = new Combo(grpReviewGate, SWT.READ_ONLY);
				GridData gd_cbDFMEARequired = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_cbDFMEARequired.widthHint = 30;
				cbDFMEARequired.setLayoutData(gd_cbDFMEARequired);
				cbDFMEARequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbDFMEARequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtDFMEA = new Text(grpReviewGate, SWT.BORDER);
				txtDFMEA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				lblDVP = new Label(grpReviewGate, SWT.NONE);
				lblDVP.setText("DVP: (*)");
			}
			{
				cbDVPRequired = new Combo(grpReviewGate, SWT.READ_ONLY);
				GridData gd_cbDVPRequired = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_cbDVPRequired.widthHint = 30;
				cbDVPRequired.setLayoutData(gd_cbDVPRequired);
				cbDVPRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
				cbDVPRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				txtDVP = new Text(grpReviewGate, SWT.BORDER);
				txtDVP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				grpInitCost = new Group(container, SWT.NONE);
				grpInitCost.setForeground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
				grpInitCost.setText("INIT COST (currency: $)");
				grpInitCost.setLayout(new GridLayout(6, false));
				grpInitCost.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
				{
					lblVehicleCost = new Label(grpInitCost, SWT.NONE);
					lblVehicleCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					lblVehicleCost.setText("Product Cost: (*)");
				}
				{
					cbVehicleCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
					cbVehicleCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
					cbVehicleCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					GridData gd_cbVehicleCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
					gd_cbVehicleCost.widthHint = 30;
					cbVehicleCostRequired.setLayoutData(gd_cbVehicleCost);
				}
				{
					txtVehicleCost = new Text(grpInitCost, SWT.BORDER);
					txtVehicleCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					{
						lblPieceCostDelta = new Label(grpInitCost, SWT.NONE);
						lblPieceCostDelta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPieceCostDelta.setText("Piece Cost: (*)");
					}
					{
						cbPieceCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbPieceCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbPieceCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbPieceCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbPieceCost.widthHint = 30;
						cbPieceCostRequired.setLayoutData(gd_cbPieceCost);
					}
					{
						txtPieceCost = new Text(grpInitCost, SWT.BORDER);
						txtPieceCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtPieceCost.addVerifyListener(doubleVerify);
					}
					{
						lblToolingCost = new Label(grpInitCost, SWT.NONE);
						lblToolingCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblToolingCost.setText("Tooling Cost: (*)");
					}
					{
						cbToolingCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbToolingCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbToolingCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbToolingCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbToolingCost.widthHint = 30;
						cbToolingCostRequired.setLayoutData(gd_cbToolingCost);
					}
					{
						txtToolingCost = new Text(grpInitCost, SWT.BORDER);
						txtToolingCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtToolingCost.addVerifyListener(doubleVerify);
					}
					{
						lblEddCostDelta = new Label(grpInitCost, SWT.NONE);
						lblEddCostDelta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblEddCostDelta.setText("ED&D Cost: (*)");
					}
					{
						cbEDDCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbEDDCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbEDDCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbEDDCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbEDDCost.widthHint = 30;
						cbEDDCostRequired.setLayoutData(gd_cbEDDCost);
					}
					{
						txtEDDCost = new Text(grpInitCost, SWT.BORDER);
						txtEDDCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtEDDCost.addVerifyListener(doubleVerify);
					}
					{
						lblSunkCost = new Label(grpInitCost, SWT.NONE);
						lblSunkCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblSunkCost.setText("Sunk Cost: (*)");
					}
					{
						cbSunkCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbSunkCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbSunkCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbSunkCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbSunkCost.widthHint = 30;
						cbSunkCostRequired.setLayoutData(gd_cbSunkCost);
					}
					{
						txtSunkCost = new Text(grpInitCost, SWT.BORDER);
						txtSunkCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtSunkCost.addVerifyListener(doubleVerify);
					}
					{
						lblPlantequipmentCost = new Label(grpInitCost, SWT.NONE);
						lblPlantequipmentCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblPlantequipmentCost.setText("Plant Equipment Cost: (*)");
					}
					{
						cbPlantEquipmentCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbPlantEquipmentCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbPlantEquipmentCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbPlantEquipmentCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbPlantEquipmentCost.widthHint = 30;
						cbPlantEquipmentCostRequired.setLayoutData(gd_cbPlantEquipmentCost);
					}
					{
						txtPlantEquipmentCost = new Text(grpInitCost, SWT.BORDER);
						txtPlantEquipmentCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtPlantEquipmentCost.addVerifyListener(doubleVerify);
					}
					{
						lblLogisticCost = new Label(grpInitCost, SWT.NONE);
						lblLogisticCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblLogisticCost.setText("Logistic Cost: (*)");
					}
					{
						cbLogisticCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbLogisticCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbLogisticCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbScrapCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbScrapCost.widthHint = 30;
						cbLogisticCostRequired.setLayoutData(gd_cbScrapCost);
					}
					{
						txtLogisticCost = new Text(grpInitCost, SWT.BORDER);
						txtLogisticCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtLogisticCost.addVerifyListener(doubleVerify);
					}
					{
						lblTestingValidationCost = new Label(grpInitCost, SWT.NONE);
						lblTestingValidationCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblTestingValidationCost.setText("Testing Validation Cost: (*)");
					}
					{
						cbTestingValidationCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbTestingValidationCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbTestingValidationCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbTestingValidationCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbTestingValidationCost.widthHint = 30;
						cbTestingValidationCostRequired.setLayoutData(gd_cbTestingValidationCost);
					}
					{
						txtTestingValidationCost = new Text(grpInitCost, SWT.BORDER);
						txtTestingValidationCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtTestingValidationCost.addVerifyListener(doubleVerify);
					}
					{
						lblReworkCost = new Label(grpInitCost, SWT.NONE);
						lblReworkCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblReworkCost.setText("Rework Cost: (*)");
					}
					{
						cbReworkCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbReworkCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbReworkCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbReworkCostRequired.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						txtReworkCost = new Text(grpInitCost, SWT.BORDER);
						txtReworkCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtReworkCost.addVerifyListener(doubleVerify);
					}
					{
						lblBudgetCost = new Label(grpInitCost, SWT.NONE);
						lblBudgetCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblBudgetCost.setText("Budget Cost: (*)");
					}
					{
						cbBudgetCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbBudgetCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbBudgetCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbBudgetCostRequired.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						txtBudgetCost = new Text(grpInitCost, SWT.BORDER);
						txtBudgetCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtBudgetCost.addVerifyListener(doubleVerify);
					}
					{
						lblOtherCost = new Label(grpInitCost, SWT.NONE);
						lblOtherCost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 4, 1));
						lblOtherCost.setText("Other Cost (tax, advance payment...): (*)");
					}
					{
						cbOtherCostRequired = new Combo(grpInitCost, SWT.READ_ONLY);
						cbOtherCostRequired.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
						cbOtherCostRequired.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						GridData gd_cbOtherCost = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd_cbOtherCost.widthHint = 30;
						cbOtherCostRequired.setLayoutData(gd_cbOtherCost);
					}
					{
						txtOtherCost = new Text(grpInitCost, SWT.BORDER);
						txtOtherCost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						txtOtherCost.addVerifyListener(doubleVerify);
					}
					txtVehicleCost.addVerifyListener(doubleVerify);
				}
			}

			updateDateTimeControl();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	private void updateDateTimeControl() {
		datTargetReleaseDate.setVisible(ckbTargetReleaseDate.getSelection());
		datApprovalDate.setVisible(ckbApprovalDate.getSelection());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1500, 900);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("VINES Engineering Change Request...");
	}
}
