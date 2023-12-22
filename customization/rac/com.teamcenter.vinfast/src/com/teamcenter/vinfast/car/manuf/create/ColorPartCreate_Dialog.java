package com.teamcenter.vinfast.car.manuf.create;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class ColorPartCreate_Dialog extends TitleAreaDialog {
	private Composite container;
	private Composite composite;

	public Button btnAccept;
	public Button ckbOpenOnCreate;
	public Button btnSearch;
	public Combo cbCategory;
	public Combo cbBaseColor;
	public Combo cbSubColor;
	public Combo cbColorSpec;
	public Combo cbGloss;
	public Combo cbGrain;
	public Text txtID;
	public Text txtColorCode;
	public Combo cbUOM;
	public Text txtName;
	public Text txtDescription;
	public Combo cbPartMakeBuy;

	public Label lblPartTrace;
	public Label lblIsAfterSale;
	public Combo cbPartTraceability;
	public Label lblCategory;
	public Label lblBaseColor;
	public Label lblSubColor;
	public Label lblColorSpecification;
	public Label lblGloss;
	public Label lblGrain;
	public Label lblId;
	public Label lblName;
	public Label lblDescription;
	public Label lblPartMakebuy;
	public Label lblUom;
	public Button rbtIsAfterSaleTrue;
	public Button rbtIsAfterSaleFalse;
	public Label lblBasePart;
	public Text txtBasePart;

	public ColorPartCreate_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Color Part Create");
		setMessage("Please fill values to create color part", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblBasePart = new Label(container, SWT.NONE);
			lblBasePart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBasePart.setText("Base Part:");

			txtBasePart = new Text(container, SWT.BORDER);
			txtBasePart.setEnabled(false);
			txtBasePart.setEditable(false);
			txtBasePart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			btnSearch = new Button(container, SWT.PUSH);
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gd_btnSearch.widthHint = 26;
			btnSearch.setLayoutData(gd_btnSearch);

			lblCategory = new Label(container, SWT.NONE);
			lblCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblCategory.setText("Category:");
			cbCategory = new Combo(container, SWT.READ_ONLY);
			cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblBaseColor = new Label(container, SWT.NONE);
			lblBaseColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBaseColor.setText("Base Color:");
			cbBaseColor = new Combo(container, SWT.READ_ONLY);
			cbBaseColor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBaseColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblSubColor = new Label(container, SWT.NONE);
			lblSubColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubColor.setText("Sub Color:");
			cbSubColor = new Combo(container, SWT.READ_ONLY);
			cbSubColor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSubColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblColorSpecification = new Label(container, SWT.NONE);
			lblColorSpecification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblColorSpecification.setText("Color Specification:");
			cbColorSpec = new Combo(container, SWT.READ_ONLY);
			cbColorSpec.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbColorSpec.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblGloss = new Label(container, SWT.NONE);
			lblGloss.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblGloss.setText("Gloss:");
			cbGloss = new Combo(container, SWT.READ_ONLY);
			cbGloss.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbGloss.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblGrain = new Label(container, SWT.NONE);
			lblGrain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblGrain.setText("Grain:");
			cbGrain = new Combo(container, SWT.READ_ONLY);
			cbGrain.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbGrain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblId = new Label(container, SWT.NONE);
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID: (*)");
			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			GridData gd_txtID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtID.widthHint = 160;
			txtID.setLayoutData(gd_txtID);

			txtColorCode = new Text(container, SWT.BORDER);
			txtColorCode.setEnabled(false);
			txtColorCode.setEditable(false);
			txtColorCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);

			lblName = new Label(container, SWT.NONE);
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");
			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblDescription.setText("Description: (*)");
			txtDescription = new Text(container, SWT.BORDER | SWT.WRAP);
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			gd_txtDescription.heightHint = 50;
			txtDescription.setLayoutData(gd_txtDescription);

			lblPartMakebuy = new Label(container, SWT.NONE);
			lblPartMakebuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakebuy.setText("Part Make/Buy: (*)");
			cbPartMakeBuy = new Combo(container, SWT.READ_ONLY);
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblUom = new Label(container, SWT.NONE);
			lblUom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUom.setText("UOM: (*)");
			cbUOM = new Combo(container, SWT.READ_ONLY);
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblPartTrace = new Label(container, SWT.NONE);
			lblPartTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblPartTrace.setText("Part Traceability Indicator:");

			cbPartTraceability = new Combo(container, SWT.READ_ONLY);
			cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblIsAfterSale = new Label(container, SWT.NONE);
			lblIsAfterSale.setText("Is After Sale Revelant:");

			composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

			rbtIsAfterSaleTrue = new Button(composite, SWT.RADIO);
			rbtIsAfterSaleTrue.setText("True");
			rbtIsAfterSaleTrue.setBounds(5, 5, 60, 16);

			rbtIsAfterSaleFalse = new Button(composite, SWT.RADIO);
			rbtIsAfterSaleFalse.setText("False");
			rbtIsAfterSaleFalse.setBounds(65, 5, 60, 16);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Color Part");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 616);
	}
}
