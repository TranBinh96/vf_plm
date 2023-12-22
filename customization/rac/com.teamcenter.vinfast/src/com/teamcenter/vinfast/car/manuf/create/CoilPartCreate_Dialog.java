package com.teamcenter.vinfast.car.manuf.create;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.Spinner;

public class CoilPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	
	public Button btnCreate;
	private Label lblId;
	private Label lblName;
	private Label lblId_1;
	private Label lblId_2;
	private Label lblId_3;
	private Label lblId_4;
	private Label lblId_5;
	public Text txtID;
	public Text txtName;
	public Text txtDescription;
	public Combo cbPartMakeBuy;
	public Combo cbSubCategory;
	public Combo cbMaterial;
	public Combo cbUoM;
	private Label lblId_6;
	private Button ckbIsMotherCoil;
	private Label lblId_7;
	private Label lblId_8;
	private Label lblId_9;
	private Label lblNewLabel;
	private Label lblNewLabel_1;
	private Label lblNewLabel_2;
	private Spinner txtWidth;
	private Spinner txtLength;
	private Spinner txtThickness;
	
	public CoilPartCreate_Dialog(Shell parentShell) throws TCException {
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
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblId_3 = new Label(container, SWT.NONE);
				lblId_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_3.setText("Sub Category: (*)");
			}
			{
				cbSubCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbSubCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbSubCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_4 = new Label(container, SWT.NONE);
				lblId_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_4.setText("Material: (*)");
			}
			{
				cbMaterial = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Name: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
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
				lblId_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_2.setText("Part Make/Buy: (*)");
			}
			{
				cbPartMakeBuy = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_5 = new Label(container, SWT.NONE);
				lblId_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_5.setText("Unit of Measure: (*)");
			}
			{
				cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_6 = new Label(container, SWT.NONE);
				lblId_6.setText("Is Mother Coil: (*)");
			}
			{
				ckbIsMotherCoil = new Button(container, SWT.CHECK);
				ckbIsMotherCoil.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			}
			{
				lblId_7 = new Label(container, SWT.NONE);
				lblId_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_7.setText("Width: (*)");
			}
			{
				txtWidth = new Spinner(container, SWT.BORDER);
				txtWidth.setDigits(2);
				GridData gd_txtWidth = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
				gd_txtWidth.widthHint = 258;
				txtWidth.setLayoutData(gd_txtWidth);
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("mm");
			}
			{
				lblId_8 = new Label(container, SWT.NONE);
				lblId_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_8.setText("Length: (*)");
			}
			{
				txtLength = new Spinner(container, SWT.BORDER);
				txtLength.setDigits(2);
				txtLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				lblNewLabel_1 = new Label(container, SWT.NONE);
				lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblNewLabel_1.setText("mm");
			}
			{
				lblId_9 = new Label(container, SWT.NONE);
				lblId_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_9.setText("Thickness: (*)");
			}
			{
				txtThickness = new Spinner(container, SWT.BORDER);
				txtThickness.setDigits(2);
				txtThickness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				lblNewLabel_2 = new Label(container, SWT.NONE);
				lblNewLabel_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblNewLabel_2.setText("mm");
			}
		} 
		catch (Exception e) {
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
		return new Point(480, 593);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Coil Part...");
	}
}
