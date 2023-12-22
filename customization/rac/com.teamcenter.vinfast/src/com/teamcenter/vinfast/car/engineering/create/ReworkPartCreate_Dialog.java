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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class ReworkPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	public Button ckbOpenOnCreate;
	private Label lblId;
	private Label lblName;
	private Label lblId_1;
	private Label lblId_2;
	private Label lblId_3;
	private Label lblId_5;
	private Label lblId_7;
	public Text txtID;
	public Text txtName;
	public Text txtDescription;
	public Combo cbPartMakeBuy;
	public Combo cbPartCategory;
	public Combo cbUoM;
	public Combo cbSupplierType;
	public Text txtSuffID;
	private Label lblId_4;
	public Combo cbPartTraceability;
	private Label lblId_6;
	private Composite composite;
	public Button rbtIsAfterSaleTrue;
	public Button rbtIsAfterSaleFalse;

	public ReworkPartCreate_Dialog(Shell parentShell) throws TCException {
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
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId.setText("ID: (*)");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				txtSuffID = new Text(container, SWT.BORDER);
				txtSuffID.setEditable(false);
				GridData gd_txtSuffID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_txtSuffID.widthHint = 7;
				txtSuffID.setLayoutData(gd_txtSuffID);
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
				lblId_4 = new Label(container, SWT.NONE);
				lblId_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblId_4.setText("Part Traceability Indicator:");
			}
			{
				cbPartTraceability = new Combo(container, SWT.READ_ONLY);
				cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId_6 = new Label(container, SWT.NONE);
				lblId_6.setText("Is After Sale Revelant:");
			}
			{
				composite = new Composite(container, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
				{
					rbtIsAfterSaleTrue = new Button(composite, SWT.RADIO);
					rbtIsAfterSaleTrue.setText("True");
					rbtIsAfterSaleTrue.setBounds(5, 5, 60, 16);
				}
				{
					rbtIsAfterSaleFalse = new Button(composite, SWT.RADIO);
					rbtIsAfterSaleFalse.setText("False");
					rbtIsAfterSaleFalse.setBounds(65, 5, 60, 16);
				}
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
		return new Point(480, 593);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create Re-work Part");
	}
}
