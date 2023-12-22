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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SCNCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button ckbOpenOnCreate;
	public Button btnCreate;
	public Label lblDonorVehicle;
	public Combo cbVehicleProgram;
	public Text txtName;
	public Text txtDescription;
	public Button ckbSORRequired;
	public Button ckbIllustrationUpdateRequired;
	public Button ckbHPartsRequired;
	public Button ckbTranslationRequired;
	public Button ckbMarketSpecificPurchaseRequired;
	public Button btnECNRemove;
	public List lstECN;
	public Button btnECNSearch;
	public Text txtPrefixName;

	public SCNCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create SCN");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblMaterial = new Label(container, SWT.NONE);
			lblMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMaterial.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblMaterial.setText("Vehicle Program: (*)");

			cbVehicleProgram = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbVehicleProgram.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbVehicleProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_cbVehicleProgram = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			gd_cbVehicleProgram.widthHint = 302;
			cbVehicleProgram.setLayoutData(gd_cbVehicleProgram);

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblName.setText("SCN Title: (*)");

			txtPrefixName = new Text(container, SWT.BORDER);
			txtPrefixName.setEditable(false);
			txtPrefixName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtPrefixName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtPrefixName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			Label lblReferenceNumber = new Label(container, SWT.NONE);
			lblReferenceNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblReferenceNumber.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblReferenceNumber.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblReferenceNumber.setText("Description:");

			txtDescription = new Text(container, SWT.BORDER);
			txtDescription.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			gd_txtDescription.heightHint = 47;
			txtDescription.setLayoutData(gd_txtDescription);

			Label lblColorCode = new Label(container, SWT.NONE);
			lblColorCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblColorCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblColorCode.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblColorCode.setText("Implements:");

			lstECN = new List(container, SWT.BORDER);
			GridData gd_lstECN = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2);
			gd_lstECN.heightHint = 55;
			lstECN.setLayoutData(gd_lstECN);

			btnECNSearch = new Button(container, SWT.NONE);
			btnECNSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			new Label(container, SWT.NONE);

			btnECNRemove = new Button(container, SWT.NONE);
			btnECNRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnECNRemove.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPartMakeBuy.setText("SOR required: (*)");

			ckbSORRequired = new Button(container, SWT.CHECK);
			ckbSORRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			new Label(container, SWT.NONE);

			Label lblDes = new Label(container, SWT.NONE);
			lblDes.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDes.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDes.setText("Illustration update required: (*)");

			ckbIllustrationUpdateRequired = new Button(container, SWT.CHECK);
			ckbIllustrationUpdateRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			new Label(container, SWT.NONE);

			lblDonorVehicle = new Label(container, SWT.NONE);
			lblDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDonorVehicle.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDonorVehicle.setText("H Parts required: (*)");

			ckbHPartsRequired = new Button(container, SWT.CHECK);
			ckbHPartsRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			new Label(container, SWT.NONE);

			Label lblUoM = new Label(container, SWT.NONE);
			lblUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUoM.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblUoM.setText("Translation required: (*)");

			ckbTranslationRequired = new Button(container, SWT.CHECK);
			ckbTranslationRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			new Label(container, SWT.NONE);

			Label lblPurLevel = new Label(container, SWT.NONE);
			lblPurLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPurLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPurLevel.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPurLevel.setText("Market specific Purchase required: (*)");

			ckbMarketSpecificPurchaseRequired = new Button(container, SWT.CHECK);
			ckbMarketSpecificPurchaseRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			new Label(container, SWT.NONE);
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
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
		return new Point(500, 580);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Aftersale Change Notice");
	}
}
