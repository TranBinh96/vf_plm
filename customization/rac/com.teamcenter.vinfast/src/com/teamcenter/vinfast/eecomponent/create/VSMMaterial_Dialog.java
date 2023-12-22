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

public class VSMMaterial_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
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
	public Label lblMaterialCategory;
	public Combo cbMaterialCategory;
	private Label lblComponentCategory;
	public Combo cbComponentCategory;
	private Label lblDonorVehicle;
	public Combo cbDonorVehicle;

	public VSMMaterial_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Create VSM Material");
			setMessage("Define business object create information");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblComponentCategory = new Label(container, SWT.NONE);
				lblComponentCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblComponentCategory.setText("Component Category: (*)");
			}
			{
				cbComponentCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbComponentCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbComponentCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
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
			{
				lblMaterialCategory = new Label(container, SWT.NONE);
				lblMaterialCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblMaterialCategory.setText("Material Category: (*)");
			}
			{
				cbMaterialCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbMaterialCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMaterialCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
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
		return new Point(480, 450);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create VSM Material");
	}
}
