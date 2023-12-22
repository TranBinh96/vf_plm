package com.teamcenter.vinfast.sor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

public class SORDocumentCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	private Composite composite;
	private ScrolledComposite scrolledComposite;

	public Button btnCreate;
	public Button ckbOpenOnCreate;

	private Group grpSummary;
	public Label lblName;
	public Label lblDescription;
	public Label lblModelCode;
	public Label lblReviewerName;
	public Label lblModuleName;
	public Label lblChangeDesc;
	public Label lblMarket;
	public Label lblCommodityBuyer;
	public Label lblPurchasingCommodityManager;
	public Label lblCommodityEngineerManager;
	public Label lblProductionVolume;
	public Label lblBuildPlan;
	public Label lblApproverName;
	public Label lblDataManagement;
	public Label lblSQEManager;
	public Label lblSQESiteEngineer;
	public Label lblSCMAnalyst;
	public Label lblCommodityEngineer;
	public Text txtName;
	public Text txtDescription;
	public Text txtProductionVolume;
	public Text txtBuildPlan;

	public Combo cbMarket;
	public Combo cbModuleName;
	public List lstMarket;
	public Combo cbModelCode;
	public Label lblId;
	public Text txtID;
	public Text txtReviewerName;
	public Text txtApproverName;
	public DateTime datChangeDescDate;
	public Label lblCompliance;
	public Label lblRASIC;
	public Text txtDataManagement;
	public Text txtCompliance;
	public Text txtRasic;
	public Label lblId_6;
	public Label lblId_7;
	public Label lblId_8;
	public Label lblId_9;
	public Text txtCommodityBuyerName;
	public Text txtCommodityBuyerEmail;
	public Text txtCommodityBuyerPhone;
	public Text txtPurchasingCommodityManagerName;
	public Text txtPurchasingCommodityManagerEmail;
	public Text txtPurchasingCommodityManagerPhone;
	public Text txtCommodityEngineerName;
	public Text txtCommodityEngineerEmail;
	public Text txtCommodityEngineerPhone;
	public Text txtCommodityEngineerManagerName;
	public Text txtCommodityEngineerManagerEmail;
	public Text txtCommodityEngineerManagerPhone;
	public Label lblPackagingAnalyst;
	public Label lblSCMManager;
	public Text txtSQESiteEngineerName;
	public Text txtSQESiteEngineerEmail;
	public Text txtSQESiteEngineerPhone;
	public Text txtSQEManagerName;
	public Text txtSQEManagerEmail;
	public Text txtSQEManagerPhone;
	public Text txtSCMAnalystName;
	public Text txtSCMAnalystEmail;
	public Text txtSCMAnalystPhone;
	public Text txtPackagingAnalystName;
	public Text txtPackagingAnalystEmail;
	public Text txtPackagingAnalystPhone;
	public Text txtSCMManagerName;
	public Text txtSCMManagerEmail;
	public Text txtSCMManagerPhone;
	public Button btnMarketRemove;
	public Button btnMarketAdd;
	private Group grpReview;
	public Button ckbIsCAE;
	public Button ckbIsSaleMarketing;
	public Button ckbIsStylingColor;
	public Button ckbIsStandardParts;
	public Button ckbIsVehicleArchieture;
	public Button btnSelectBuyer;
	public Button btnSelectCL;
	public Button btnSelectReviewer;
	public Button btnSelectApprover;

	public SORDocumentCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Create SOR Document");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
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

			GridData gd_grpSummary = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
			gd_grpSummary.widthHint = 439;
			grpSummary.setLayoutData(gd_grpSummary);
			grpSummary.setForeground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));
			grpSummary.setText("SUMMARY");
			GridLayout gl_grpSummary = new GridLayout(1, false);
			gl_grpSummary.horizontalSpacing = 1;
			grpSummary.setLayout(gl_grpSummary);
			{
				scrolledComposite = new ScrolledComposite(grpSummary, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				scrolledComposite.setExpandHorizontal(true);
				scrolledComposite.setExpandVertical(true);
				{
					composite = new Composite(scrolledComposite, SWT.NONE);
					composite.setLayout(new GridLayout(3, false));
					{
						lblId = new Label(composite, SWT.NONE);
						lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblId.setText("ID: (*)");
					}
					{
						txtID = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
						txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblName = new Label(composite, SWT.NONE);
						lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblName.setText("Name: (*)");
					}
					{
						txtName = new Text(composite, SWT.BORDER);
						txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblDescription = new Label(composite, SWT.NONE);
						lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblDescription.setText("Description:");
					}
					{
						txtDescription = new Text(composite, SWT.BORDER);
						txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblModelCode = new Label(composite, SWT.NONE);
						lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModelCode.setText("Model Code: (*)");
					}
					{
						cbModelCode = new Combo(composite, SWT.READ_ONLY);
						cbModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						cbModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblModuleName = new Label(composite, SWT.NONE);
						lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblModuleName.setText("Module Name: (*)");
					}
					{
						cbModuleName = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
						cbModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						cbModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					new Label(composite, SWT.NONE);
					{
						lblReviewerName = new Label(composite, SWT.NONE);
						lblReviewerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblReviewerName.setText("Reviewer Name: (*)");
					}
					{
						txtReviewerName = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
						txtReviewerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						btnSelectReviewer = new Button(composite, SWT.NONE);
						btnSelectReviewer.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
					}
					{
						lblApproverName = new Label(composite, SWT.NONE);
						lblApproverName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblApproverName.setText("Approver Name: (*)");
					}
					{
						txtApproverName = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
						txtApproverName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					{
						btnSelectApprover = new Button(composite, SWT.NONE);
						btnSelectApprover.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
					}
					{
						lblChangeDesc = new Label(composite, SWT.NONE);
						lblChangeDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblChangeDesc.setText("Change Description/Date: (*)");
					}
					{
						datChangeDescDate = new DateTime(composite, SWT.BORDER);
						datChangeDescDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblMarket = new Label(composite, SWT.NONE);
						lblMarket.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
						lblMarket.setText("Market: (*)");
					}

					lstMarket = new List(composite, SWT.BORDER);
					GridData gd_lstMarket = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
					gd_lstMarket.heightHint = 50;
					lstMarket.setLayoutData(gd_lstMarket);
					{
						btnMarketRemove = new Button(composite, SWT.NONE);
						btnMarketRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
						btnMarketRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
					}
					{
						cbMarket = new Combo(composite, SWT.READ_ONLY);
						cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
					{
						btnMarketAdd = new Button(composite, SWT.NONE);
						btnMarketAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
					}
					{
						lblProductionVolume = new Label(composite, SWT.NONE);
						lblProductionVolume.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblProductionVolume.setText("Production Volume: (*)");
					}
					{
						txtProductionVolume = new Text(composite, SWT.BORDER);
						txtProductionVolume.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblBuildPlan = new Label(composite, SWT.NONE);
						lblBuildPlan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblBuildPlan.setText("Build Plan/Timing Schedule: (*)");
					}
					{
						txtBuildPlan = new Text(composite, SWT.BORDER);
						txtBuildPlan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
					{
						lblDataManagement = new Label(composite, SWT.NONE);
						lblDataManagement.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblDataManagement.setText("Data Management: (*)");
					}
					{
						txtDataManagement = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
						GridData gd_txtDataManagement = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
						gd_txtDataManagement.heightHint = 50;
						txtDataManagement.setLayoutData(gd_txtDataManagement);
					}
					new Label(composite, SWT.NONE);
					{
						lblCompliance = new Label(composite, SWT.NONE);
						lblCompliance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblCompliance.setText("Compliance: (*)");
					}
					{
						txtCompliance = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
						GridData gd_txtCompliance = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
						gd_txtCompliance.heightHint = 50;
						txtCompliance.setLayoutData(gd_txtCompliance);
					}
					new Label(composite, SWT.NONE);
					{
						lblRASIC = new Label(composite, SWT.NONE);
						lblRASIC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						lblRASIC.setText("RASIC and Deliverables: (*)");
					}
					{
						txtRasic = new Text(composite, SWT.BORDER);
						txtRasic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					}
					new Label(composite, SWT.NONE);
				}
				scrolledComposite.setContent(composite);
				scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}

			Group grpPrecab = new Group(container, SWT.NONE);
			GridData gd_grpPrecab = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
			gd_grpPrecab.widthHint = 407;
			grpPrecab.setLayoutData(gd_grpPrecab);
			grpPrecab.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
			grpPrecab.setText("CONTACTS");
			grpPrecab.setLayout(new GridLayout(5, false));
			{
				lblId_6 = new Label(grpPrecab, SWT.NONE);
				lblId_6.setText("Contact");
			}
			{
				lblId_7 = new Label(grpPrecab, SWT.NONE);
				lblId_7.setText("Name");
			}
			{
				lblId_8 = new Label(grpPrecab, SWT.NONE);
				lblId_8.setText("Email");
			}
			{
				lblId_9 = new Label(grpPrecab, SWT.NONE);
				lblId_9.setText("Phone Number");
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblPurchasingCommodityManager = new Label(grpPrecab, SWT.NONE);
				lblPurchasingCommodityManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPurchasingCommodityManager.setText("Purchasing commodity manager: (*)");
			}
			{
				txtPurchasingCommodityManagerName = new Text(grpPrecab, SWT.BORDER);
				txtPurchasingCommodityManagerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtPurchasingCommodityManagerEmail = new Text(grpPrecab, SWT.BORDER);
				txtPurchasingCommodityManagerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtPurchasingCommodityManagerPhone = new Text(grpPrecab, SWT.BORDER);
				txtPurchasingCommodityManagerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSelectCL = new Button(grpPrecab, SWT.NONE);
				btnSelectCL.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
			}
			{
				lblCommodityBuyer = new Label(grpPrecab, SWT.NONE);
				lblCommodityBuyer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCommodityBuyer.setText("Commodity buyer: (*)");
			}
			{
				txtCommodityBuyerName = new Text(grpPrecab, SWT.BORDER);
				txtCommodityBuyerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityBuyerEmail = new Text(grpPrecab, SWT.BORDER);
				txtCommodityBuyerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityBuyerPhone = new Text(grpPrecab, SWT.BORDER);
				txtCommodityBuyerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSelectBuyer = new Button(grpPrecab, SWT.NONE);
				btnSelectBuyer.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
			}
			{
				lblCommodityEngineer = new Label(grpPrecab, SWT.NONE);
				lblCommodityEngineer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCommodityEngineer.setText("Commodity engineer: (*)");
			}
			{
				txtCommodityEngineerName = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityEngineerEmail = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityEngineerPhone = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblCommodityEngineerManager = new Label(grpPrecab, SWT.NONE);
				lblCommodityEngineerManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCommodityEngineerManager.setText("Commodity engineer manager: (*)");
			}
			{
				txtCommodityEngineerManagerName = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerManagerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityEngineerManagerEmail = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerManagerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtCommodityEngineerManagerPhone = new Text(grpPrecab, SWT.BORDER);
				txtCommodityEngineerManagerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblSQESiteEngineer = new Label(grpPrecab, SWT.NONE);
				lblSQESiteEngineer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSQESiteEngineer.setText("SQE site engineer: (*)");
			}
			{
				txtSQESiteEngineerName = new Text(grpPrecab, SWT.BORDER);
				txtSQESiteEngineerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSQESiteEngineerEmail = new Text(grpPrecab, SWT.BORDER);
				txtSQESiteEngineerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSQESiteEngineerPhone = new Text(grpPrecab, SWT.BORDER);
				txtSQESiteEngineerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblSQEManager = new Label(grpPrecab, SWT.NONE);
				lblSQEManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSQEManager.setText("SQE manager: (*)");
			}
			{
				txtSQEManagerName = new Text(grpPrecab, SWT.BORDER);
				txtSQEManagerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSQEManagerEmail = new Text(grpPrecab, SWT.BORDER);
				txtSQEManagerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSQEManagerPhone = new Text(grpPrecab, SWT.BORDER);
				txtSQEManagerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblSCMAnalyst = new Label(grpPrecab, SWT.NONE);
				lblSCMAnalyst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSCMAnalyst.setText("SCM analyst: (*)");
			}
			{
				txtSCMAnalystName = new Text(grpPrecab, SWT.BORDER);
				txtSCMAnalystName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSCMAnalystEmail = new Text(grpPrecab, SWT.BORDER);
				txtSCMAnalystEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSCMAnalystPhone = new Text(grpPrecab, SWT.BORDER);
				txtSCMAnalystPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblPackagingAnalyst = new Label(grpPrecab, SWT.NONE);
				lblPackagingAnalyst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPackagingAnalyst.setText("Packaging analyst: (*)");
			}
			{
				txtPackagingAnalystName = new Text(grpPrecab, SWT.BORDER);
				txtPackagingAnalystName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtPackagingAnalystEmail = new Text(grpPrecab, SWT.BORDER);
				txtPackagingAnalystEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtPackagingAnalystPhone = new Text(grpPrecab, SWT.BORDER);
				txtPackagingAnalystPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				lblSCMManager = new Label(grpPrecab, SWT.NONE);
				lblSCMManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSCMManager.setText("SCM manager: (*)");
			}
			{
				txtSCMManagerName = new Text(grpPrecab, SWT.BORDER);
				txtSCMManagerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSCMManagerEmail = new Text(grpPrecab, SWT.BORDER);
				txtSCMManagerEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSCMManagerPhone = new Text(grpPrecab, SWT.BORDER);
				txtSCMManagerPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(grpPrecab, SWT.NONE);
			{
				grpReview = new Group(container, SWT.NONE);
				grpReview.setLayout(new GridLayout(2, false));
				grpReview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
				grpReview.setText("REVIEW");
				{
					ckbIsCAE = new Button(grpReview, SWT.CHECK);
					ckbIsCAE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					ckbIsCAE.setText("CAE");
				}
				{
					ckbIsSaleMarketing = new Button(grpReview, SWT.CHECK);
					ckbIsSaleMarketing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					ckbIsSaleMarketing.setText("Sale And Marketing");
				}
				{
					ckbIsStylingColor = new Button(grpReview, SWT.CHECK);
					ckbIsStylingColor.setText("Styling Color Trim");
				}
				{
					ckbIsVehicleArchieture = new Button(grpReview, SWT.CHECK);
					ckbIsVehicleArchieture.setText("Vehicle Archieture");
				}
				{
					ckbIsStandardParts = new Button(grpReview, SWT.CHECK);
					ckbIsStandardParts.setText("Standard Parts");
				}
				new Label(grpReview, SWT.NONE);
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
		return new Point(1000, 1000);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Scope Of Requirements");
	}
}
