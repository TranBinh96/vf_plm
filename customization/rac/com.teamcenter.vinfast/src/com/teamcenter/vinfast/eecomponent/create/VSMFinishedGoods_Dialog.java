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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.SWTResourceManager;

public class VSMFinishedGoods_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	private Label lblNewLabel;
	public Text txtID;
	private Label lblName;
	public Text txtName;
	private Label lblDescription;
	public Text txtDescription;
	private Label lblUom;
	public Combo cbUoM;
	private Label lblPartMakebuy;
	public Combo cbPartMakeBuy;
	private Label lblProduct;
	private Label lblProductCategory;
	private Label lblProductType;
	private Label lblModelName;
	private Label lblMarket;
	public Combo cbProduct;
	public Combo cbProductCategory;
	public Combo cbVehicleType;
	public Combo cbModel;
	public Combo cbMarket;
	private Label lblDonorVehicle;
	public Combo cbDonorVehicle;

	public VSMFinishedGoods_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Create VSM Finished Goods");
			setMessage("Define business object create information");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblProduct = new Label(container, SWT.NONE);
				lblProduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProduct.setText("Product: (*)");
			}
			{
				cbProduct = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProduct.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblProductType = new Label(container, SWT.NONE);
				lblProductType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProductType.setText("Vehicle Type: (*)");
			}
			{
				cbVehicleType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbVehicleType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbVehicleType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblProductCategory = new Label(container, SWT.NONE);
				lblProductCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProductCategory.setText("Product Category: (*)");
			}
			{
				cbProductCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProductCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProductCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblModelName = new Label(container, SWT.NONE);
				lblModelName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModelName.setText("Model: (*)");
			}
			{
				cbModel = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblMarket = new Label(container, SWT.NONE);
				lblMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblMarket.setText("Market: (*)");
			}
			{
				cbMarket = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblDescription = new Label(container, SWT.NONE);
				lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDescription.setText("Description:");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_txtDescription.heightHint = 52;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblUom = new Label(container, SWT.NONE);
				lblUom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblUom.setText("UoM:");
			}
			{
				cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
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
			{
				lblDonorVehicle = new Label(container, SWT.NONE);
				lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDonorVehicle.setText("Donor Program: (*)");
			}
			{
				cbDonorVehicle = new Combo(container, SWT.READ_ONLY);
				cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			new Label(container, SWT.NONE);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create VSM Finished Goods");
	}
}
