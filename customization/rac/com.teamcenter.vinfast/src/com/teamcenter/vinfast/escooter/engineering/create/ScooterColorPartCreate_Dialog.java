package com.teamcenter.vinfast.escooter.engineering.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;

public class ScooterColorPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button ckbOpenOnCreate;

	public Button btnCreate;
	public Combo cbItemType;
	public Combo cbBaseColor;
	public Combo cbSubColor;
	public Text txtID;
	public Combo cbUOM;
	public Text txtName;
	public Text txtVietnameseDescription;
	public Combo cbVehicleLine;
	public Text txtColorCode;
	public Label lblVehicleLine;
	public Label lblID;
	public Label lblBaseMaterial;
	public Label lblColorMakingMethod;
	public Label lblCharacteristic;
	public Label lblSubColorName;
	public Combo cbBaseMaterial;
	public Combo cbColorMakingMethod;
	public Combo cbCharacteristic;
	public Combo cbSubColorName;
	public Label lblItemType;
	public Label lblBaseColor;
	public Label lblSubColor;
	public Label lblName;
	public Label lblVNDescription;
	public Label lblUom;
	public Label lblMaterialGroup;
	public Combo cbMaterialGroup;

	public ScooterColorPartCreate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("EScooter Color Part Creation");
		setMessage("Please fill values to create EScooter color part", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblItemType = new Label(container, SWT.NONE);
			lblItemType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblItemType.setText("Item Type: (*)");

			cbItemType = new Combo(container, SWT.READ_ONLY);
			cbItemType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbItemType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblBaseColor = new Label(container, SWT.NONE);
			lblBaseColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBaseColor.setText("Base Color: (*)");

			cbBaseColor = new Combo(container, SWT.READ_ONLY);
			cbBaseColor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBaseColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblSubColor = new Label(container, SWT.NONE);
			lblSubColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubColor.setText("Sub Color: (*)");

			cbSubColor = new Combo(container, SWT.READ_ONLY);
			cbSubColor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSubColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblID = new Label(container, SWT.NONE);
			lblID.setText("ID: (*)");
			lblID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			GridData gd_txtID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtID.widthHint = 110;
			txtID.setLayoutData(gd_txtID);

			txtColorCode = new Text(container, SWT.BORDER);
			txtColorCode.setEditable(false);
			txtColorCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblName = new Label(container, SWT.NONE);
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblVNDescription = new Label(container, SWT.NONE);
			lblVNDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVNDescription.setText("EScooter Part Name Vietnamese:");

			txtVietnameseDescription = new Text(container, SWT.BORDER);
			txtVietnameseDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblBaseMaterial = new Label(container, SWT.NONE);
			lblBaseMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBaseMaterial.setText("Base Material:");

			cbBaseMaterial = new Combo(container, SWT.READ_ONLY);
			cbBaseMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBaseMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblColorMakingMethod = new Label(container, SWT.NONE);
			lblColorMakingMethod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblColorMakingMethod.setText("Color Making Method:");

			cbColorMakingMethod = new Combo(container, SWT.READ_ONLY);
			cbColorMakingMethod.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbColorMakingMethod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblCharacteristic = new Label(container, SWT.NONE);
			lblCharacteristic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblCharacteristic.setText("Characteristic:");

			cbCharacteristic = new Combo(container, SWT.READ_ONLY);
			cbCharacteristic.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCharacteristic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblSubColorName = new Label(container, SWT.NONE);
			lblSubColorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubColorName.setText("Sub Color Name:");

			cbSubColorName = new Combo(container, SWT.READ_ONLY);
			cbSubColorName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSubColorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblUom = new Label(container, SWT.NONE);
			lblUom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUom.setText("UOM: (*)");

			cbUOM = new Combo(container, SWT.READ_ONLY);
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lblVehicleLine = new Label(container, SWT.NONE);
			lblVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleLine.setText("EScooter Model/Vehicle Line: (*)");

			cbVehicleLine = new Combo(container, SWT.READ_ONLY);
			cbVehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			lblMaterialGroup = new Label(container, SWT.NONE);
			lblMaterialGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMaterialGroup.setText("Material Group: (*)");
			
			cbMaterialGroup = new Combo(container, SWT.READ_ONLY);
			cbMaterialGroup.setEnabled(false);
			cbMaterialGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbMaterialGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {;
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("EScooter Color Part");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}
}
