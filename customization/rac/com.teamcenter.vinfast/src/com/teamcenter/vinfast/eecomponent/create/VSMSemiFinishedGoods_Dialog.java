package com.teamcenter.vinfast.eecomponent.create;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.wb.swt.SWTResourceManager;

public class VSMSemiFinishedGoods_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	private Label lblProduct;
	private Label lblProductCategory;
	public Combo cbProduct;
	public Combo cbProductCategory;
	private Label lblNewLabel;
	private Label lblName;
	private Label lblPartMakebuy;
	public Text txtID;
	public Label lblDescription;
	public Text txtDescription;
	private Label lblPartMakebuy_2;
	public Text txtName;
	public Combo cbUoM;
	public Combo cbPartMakeBuy;
	private Label lblPartMakebuy_1;
	public Combo cbDonorVehicle;

	public VSMSemiFinishedGoods_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Create VSM Semi-Finished Goods");
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
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
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
				gd_txtDescription.heightHint = 48;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				lblPartMakebuy = new Label(container, SWT.NONE);
				lblPartMakebuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartMakebuy.setText("UoM:");
			}
			{
				cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblPartMakebuy_2 = new Label(container, SWT.NONE);
				lblPartMakebuy_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartMakebuy_2.setText("Part Make/Buy: (*)");
			}
			{
				cbPartMakeBuy = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblPartMakebuy_1 = new Label(container, SWT.NONE);
				lblPartMakebuy_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartMakebuy_1.setText("Donor Program: (*)");
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
		return new Point(480, 470);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create VSM Semi-Finished Goods");
	}
}
