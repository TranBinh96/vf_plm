package com.teamcenter.vinfast.swbom.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VFFRSCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	public Button ckbOpenOnCreate;
	private Label lblName;
	private Label lblId_1;
	private Label lblId_2;
	private Label lblId_3;
	private Label lblId_5;
	private Label lblId_7;
	public Text txtName;
	public Text txtDescription;
	public Combo cbMarket;
	public Combo cbProgram;
	public Combo cbModel;
	private Label lblId_4;
	public Combo cbLifecycle;
	public List lstMarket;
	public Button btnMarketRemove;
	public Button btnMarketAdd;
	public List lstModel;
	public Button btnModelRemove;
	public Button btnModelAdd;
	public Text txtVersion;
	private Label lblId_6;
	public Combo cbFUSA;
	private Label lblId_8;
	private Label lblId_9;
	private Label lblId_10;
	public Text txtMajorVersion;
	public Text txtMinorVersion;
	public Text txtHotfixNumber;
	private Label lblId_11;
	public Text txtReferenceNumber;

	public VFFRSCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);

			VerifyListener integerVerify = new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					Text text = (Text) e.getSource();
					final String oldS = text.getText();
					String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
					try {
						if (!newS.isEmpty() && newS.compareTo("-") != 0) {
							Integer.parseInt(newS);
						}
					} catch (NumberFormatException ex) {
						e.doit = false;
					}
				}
			};

			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER | SWT.READ_ONLY);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_1 = new Label(container, SWT.NONE);
				lblId_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_1.setText("Description:");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_txtDescription.heightHint = 54;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblId_2 = new Label(container, SWT.NONE);
				lblId_2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_2.setText("Market: (*)");
			}
			{
				lstMarket = new List(container, SWT.BORDER);
				lstMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				btnMarketRemove = new Button(container, SWT.NONE);
				btnMarketRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnMarketRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			new Label(container, SWT.NONE);
			{
				cbMarket = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnMarketAdd = new Button(container, SWT.NONE);
				btnMarketAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblId_3 = new Label(container, SWT.NONE);
				lblId_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_3.setText("Program: (*)");
			}
			{
				cbProgram = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_5 = new Label(container, SWT.NONE);
				lblId_5.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_5.setText("Model: (*)");
			}
			{
				lstModel = new List(container, SWT.BORDER);
				lstModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				btnModelRemove = new Button(container, SWT.NONE);
				btnModelRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnModelRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			new Label(container, SWT.NONE);
			{
				cbModel = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnModelAdd = new Button(container, SWT.NONE);
				btnModelAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblId_7 = new Label(container, SWT.NONE);
				lblId_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_7.setText("Version: (*)");
			}
			{
				txtVersion = new Text(container, SWT.BORDER);
				txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				txtVersion.addVerifyListener(integerVerify);
			}
			{
				lblId_8 = new Label(container, SWT.NONE);
				lblId_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_8.setText("Major Version: (*)");
			}
			{
				txtMajorVersion = new Text(container, SWT.BORDER);
				txtMajorVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				txtMajorVersion.addVerifyListener(integerVerify);
			}
			{
				lblId_9 = new Label(container, SWT.NONE);
				lblId_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_9.setText("Minor Version: (*)");
			}
			{
				txtMinorVersion = new Text(container, SWT.BORDER);
				txtMinorVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				txtMinorVersion.addVerifyListener(integerVerify);
			}
			{
				lblId_10 = new Label(container, SWT.NONE);
				lblId_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_10.setText("Hotfix Number:");
			}
			{
				txtHotfixNumber = new Text(container, SWT.BORDER);
				txtHotfixNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				txtHotfixNumber.addVerifyListener(integerVerify);
			}
			{
				lblId_11 = new Label(container, SWT.NONE);
				lblId_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_11.setText("Reference Number:");
			}
			{
				txtReferenceNumber = new Text(container, SWT.BORDER);
				txtReferenceNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_4 = new Label(container, SWT.NONE);
				lblId_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_4.setText("Lifecycle:");
			}
			{
				cbLifecycle = new Combo(container, SWT.READ_ONLY);
				cbLifecycle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbLifecycle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_6 = new Label(container, SWT.NONE);
				lblId_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_6.setText("FUSA:");
			}
			{
				cbFUSA = new Combo(container, SWT.READ_ONLY);
				cbFUSA.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbFUSA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 800);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create VF FRS");
	}
}
