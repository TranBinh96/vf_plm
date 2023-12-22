package com.teamcenter.vinfast.escooter.engineering.create;

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

import com.teamcenter.rac.util.MessageBox;

public class ScooterPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnHelp;
	public Button btnCreate;
	public Button ckbOpenOnCreate;
	public Text txtID;
	public Text txtName;
	public Text txtVietnamName;
	public Text txtDescription;
	public Combo cbMaterialGroup;
	public Combo cbMakeBuy;
	public Combo cbUoM;
	public Combo cbVehicleLine;
	public Combo cbPartCategory;
	public Text txtPartReference;
	public Label lblQuantity;
	public Text txtQuantity;
	private Label lblCategory;
	public Combo cbCategory;
	public Label lblID;
	public Label lblMaterialGroup;
	public Label lblDescription;
	public Label lblPartMakebuy;
	public Label lblUoM;
	public Label lblEsModelvehicleLine;
	public Label lblIsAfterSale;
	public Label lblPartNameVietnamese;
	public Label lblPartCategory;
	public Label lblPartReference;
	public Label lblName;
	private Composite composite;
	public Button rbtYes;
	public Button rbtNo;

	public ScooterPartCreate_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		area = (Composite) super.createDialogArea(parent);
		try {
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Create Scooter Part");

			container = new Composite(area, SWT.NONE);
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblID = new Label(container, SWT.NONE);
			lblID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblID.setText("ID: (*)");

			txtID = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblName = new Label(container, SWT.NONE);
			lblName.setText("Name: (*)");
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblMaterialGroup = new Label(container, SWT.NONE);
			lblMaterialGroup.setText("Material Group: (*)");
			lblMaterialGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbMaterialGroup = new Combo(container, SWT.READ_ONLY);
			cbMaterialGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbMaterialGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblDescription = new Label(container, SWT.NONE);
			lblDescription.setText("Description:");
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			txtDescription.setToolTipText("Mandatory field: Please fill to enable Save button");
			txtDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDescription.heightHint = 54;
			txtDescription.setLayoutData(gd_txtDescription);

			lblPartMakebuy = new Label(container, SWT.NONE);
			lblPartMakebuy.setText("Part Make/Buy: (*)");
			lblPartMakebuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbMakeBuy = new Combo(container, SWT.READ_ONLY);
			cbMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblUoM = new Label(container, SWT.NONE);
			lblUoM.setText("Unit Of Measure: (*)");
			lblUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbUoM = new Combo(container, SWT.READ_ONLY);
			cbUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblEsModelvehicleLine = new Label(container, SWT.NONE);
			lblEsModelvehicleLine.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblEsModelvehicleLine.setText("EScooter Model/ Vehicle Line: (*)");
			lblEsModelvehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbVehicleLine = new Combo(container, SWT.READ_ONLY);
			cbVehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblCategory = new Label(container, SWT.NONE);
			lblCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblCategory.setText("Category:");
			lblCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbCategory = new Combo(container, SWT.READ_ONLY);
			cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblIsAfterSale = new Label(container, SWT.NONE);
			lblIsAfterSale.setText("Is After Sale Relevant:");
			lblIsAfterSale.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			rbtYes = new Button(composite, SWT.RADIO);
			rbtYes.setText("True");
			rbtYes.setBounds(5, 5, 60, 16);

			rbtNo = new Button(composite, SWT.RADIO);
			rbtNo.setText("False");
			rbtNo.setBounds(65, 5, 60, 16);

			lblPartReference = new Label(container, SWT.NONE);
			lblPartReference.setText("Part Reference:");
			lblPartReference.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtPartReference = new Text(container, SWT.BORDER);
			txtPartReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblPartCategory = new Label(container, SWT.NONE);
			lblPartCategory.setText("Part Category: (*)");
			lblPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbPartCategory = new Combo(container, SWT.READ_ONLY);
			cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblPartNameVietnamese = new Label(container, SWT.NONE);
			lblPartNameVietnamese.setText("EScooter Part Name Vietnamese:");
			lblPartNameVietnamese.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtVietnamName = new Text(container, SWT.BORDER);
			txtVietnamName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblQuantity = new Label(container, SWT.NONE);
			lblQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblQuantity.setText("Quantity:");
			lblQuantity.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtQuantity = new Text(container, SWT.BORDER);
			txtQuantity.setText("1");
			txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
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
		return new Point(480, 620);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create Scooter Part");
	}
}
