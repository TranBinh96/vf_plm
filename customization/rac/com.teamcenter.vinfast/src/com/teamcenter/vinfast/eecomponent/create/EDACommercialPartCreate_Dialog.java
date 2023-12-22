package com.teamcenter.vinfast.eecomponent.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.wb.swt.ResourceManager;

public class EDACommercialPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnAccept;
	private Label lblMaterial;
	public Combo cbMaterial;
	private Label lblId;
	public Text txtID;
	private Label lblName;
	private Label lblDescription;
	private Label lblUom;
	private Label lblPartMakebuy;
	public Combo cbUoM;
	public Combo cbPartMakeBuy;
	public Text txtName;
	public Text txtDescription;
	private Label lblDonorVehicle;
	public Combo cbDonorVehicle;
	private Label lblMainCategoryCom;
	public Combo cbMainCategory;
	private Label lblFootPrint;
	private Label lblSymbolName;
	public Text txtFootPrint;
	public Text txtSymbolName;
	private Label lblVfCode_1;
	public Text txtVendor;
	public Button btnVendorSearch;
	private Label lblVfCode_2;
	public Text txtVendorPartNumber;

	public EDACommercialPartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Create EDA Commercial Part");
			setMessage("Define business object create information");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMaterial = new Label(container, SWT.NONE);
				lblMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblMaterial.setText("Material: (*)");
			}
			{
				cbMaterial = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblDescription = new Label(container, SWT.NONE);
				lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDescription.setText("Description:");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_txtDescription.heightHint = 48;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			new Label(container, SWT.NONE);
			{
				lblUom = new Label(container, SWT.NONE);
				lblUom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblUom.setText("UoM: (*)");
			}
			{
				cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblPartMakebuy = new Label(container, SWT.NONE);
				lblPartMakebuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartMakebuy.setText("Part Make/Buy: (*)");
			}
			{
				cbPartMakeBuy = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblDonorVehicle = new Label(container, SWT.NONE);
				lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDonorVehicle.setText("Donor Sourcing Program: (*)");
			}
			{
				cbDonorVehicle = new Combo(container, SWT.READ_ONLY);
				cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblMainCategoryCom = new Label(container, SWT.NONE);
				lblMainCategoryCom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblMainCategoryCom.setText("Main Category: (*)");
			}
			{
				cbMainCategory = new Combo(container, SWT.READ_ONLY);
				cbMainCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMainCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblFootPrint = new Label(container, SWT.NONE);
				lblFootPrint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblFootPrint.setText("Foot Print: (*)");
			}
			{
				txtFootPrint = new Text(container, SWT.BORDER);
				txtFootPrint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblSymbolName = new Label(container, SWT.NONE);
				lblSymbolName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSymbolName.setText("Symbol Name: (*)");
			}
			{
				txtSymbolName = new Text(container, SWT.BORDER);
				txtSymbolName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblVfCode_2 = new Label(container, SWT.NONE);
				lblVfCode_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblVfCode_2.setText("MPN Main Compound: (*)");
			}
			{
				txtVendorPartNumber = new Text(container, SWT.BORDER);
				txtVendorPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);
			{
				lblVfCode_1 = new Label(container, SWT.NONE);
				lblVfCode_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblVfCode_1.setText("Vendor: (*)");
			}
			{
				txtVendor = new Text(container, SWT.BORDER);
				txtVendor.setEditable(false);
				txtVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnVendorSearch = new Button(container, SWT.NONE);
				btnVendorSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 628);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("EDA Commercial Part");
	}
}
