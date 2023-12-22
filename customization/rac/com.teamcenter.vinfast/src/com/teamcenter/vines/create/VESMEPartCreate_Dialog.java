package com.teamcenter.vines.create;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VESMEPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Button ckbOpenOnCreate;

	public Text txtID;
	public Text txtName;
	public Text txtDesc;
	public Combo cbModel;
	public Combo cbCategory;
	public Combo cbPartMakeBuy;
	public Combo cbUOM;

	public VESMEPartCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create VES ME Part");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			// init UI
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblModel = new Label(container, SWT.NONE);
			lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModel.setText("Model: (*)");

			cbModel = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbModel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblSubCategory = new Label(container, SWT.NONE);
			lblSubCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblSubCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblSubCategory.setText("Sub Category: (*)");

			cbCategory = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblId = new Label(container, SWT.NONE);
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID: (*)");

			txtID = new Text(container, SWT.BORDER);
			txtID.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblDescription.setText("Description: (*)");

			txtDesc = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesc.heightHint = 58;
			txtDesc.setLayoutData(gd_txtDesc);

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setText("Part Make/Buy: (*)");

			cbPartMakeBuy = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblUnitOfMeasure = new Label(container, SWT.NONE);
			lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUnitOfMeasure.setText("UoM: (*)");

			cbUOM = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
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
		return new Point(450, 550);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("VES ME Part");
	}
}
