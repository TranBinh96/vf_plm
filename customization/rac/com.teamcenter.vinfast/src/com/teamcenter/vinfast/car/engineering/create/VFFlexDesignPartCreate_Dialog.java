package com.teamcenter.vinfast.car.engineering.create;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VFFlexDesignPartCreate_Dialog extends TitleAreaDialog {
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
	private Label lblId_6;
	private Label lblId_7;
	private Label lblId_8;
	public Text txtID;
	public Text txtName;
	public Text txtDescription;
	public Combo cbPartMakeBuy;
	public Combo cbPartCategory;
	public Combo cbDonorVehicle;
	public Combo cbUoM;
	public Combo cbLongShortLead;
	public Combo cbSupplierType;
	public Button rbtManufTrue;
	public Button rbtManufFalse;
	private Label lblId_10;
	public Combo cbPartTraceability;
	public List lstCUVType;
	private Label lblId_9;
	
	public VFFlexDesignPartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
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
				lblId_3 = new Label(container, SWT.NONE);
				lblId_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_3.setText("Part Category: (*)");
			}
			{
				cbPartCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_4 = new Label(container, SWT.NONE);
				lblId_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_4.setText("Donor Vehicle: (*)");
			}
			{
				cbDonorVehicle = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_10 = new Label(container, SWT.NONE);
				lblId_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_10.setText("Part Traceability Indicator:");
			}
			{
				cbPartTraceability = new Combo(container, SWT.READ_ONLY);
				cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_5 = new Label(container, SWT.NONE);
				lblId_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_5.setText("Unit of Measure:");
			}
			{
				cbUoM = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_6 = new Label(container, SWT.NONE);
				lblId_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_6.setText("Long Or Short lead:");
			}
			{
				cbLongShortLead = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbLongShortLead.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbLongShortLead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_7 = new Label(container, SWT.NONE);
				lblId_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_7.setText("Supplier Type:");
			}
			{
				cbSupplierType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_8 = new Label(container, SWT.NONE);
				lblId_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_8.setText("Manufacturing Component:");
			}
			{
				rbtManufTrue = new Button(container, SWT.RADIO);
				rbtManufTrue.setText("True");
			}
			{
				rbtManufFalse = new Button(container, SWT.RADIO);
				rbtManufFalse.setSelection(true);
				rbtManufFalse.setText("False");
			}
			{
				lblId_9 = new Label(container, SWT.NONE);
				lblId_9.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 2));
				lblId_9.setText("CUV Vehicle Type:");
			}
			{
				lstCUVType = new List(container, SWT.BORDER);
				GridData gd_lstCUVType = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_lstCUVType.heightHint = 50;
				lstCUVType.setLayoutData(gd_lstCUVType);
			}
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
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
		newShell.setText("Create VF Flex Part");
	}
}
