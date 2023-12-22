package com.teamcenter.vinfast.report.process;

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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class EScooterProcessStatusReport_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Label lblModelCode;
	public Label lblModuleName;
	public Combo cbProcess;
	public Button btnAccept;
	public Label lblAddingNumber;
	public Button btnRemoveModel;
	public Button btnAddModel;
	public List lstModel;
	public Combo cbModel;
	public Button ckbCreateAfter;
	public Button ckbCreateBefore;
	public Button ckbModifyBefore;
	public Button ckbModifyAfter;
	public DateTime datCreateBefore;
	public DateTime datCreateAfter;
	public DateTime datModifyBefore;
	public DateTime datModifyAfter;
	public Text txtProcessName;
	public Button ckbRunningProcess;

	public EScooterProcessStatusReport_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setMessage("Config to filter report.");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("EScooter Process Status");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblVehicleCategory = new Label(container, SWT.NONE);
			lblVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblVehicleCategory.setText("Process:");

			cbProcess = new Combo(container, SWT.NONE);
			cbProcess.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

			Label lblId = new Label(container, SWT.NONE);
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("Create Before:");
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			ckbCreateBefore = new Button(container, SWT.CHECK);

			datCreateBefore = new DateTime(container, SWT.BORDER);
			datCreateBefore.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel.setText("Create After:");

			ckbCreateAfter = new Button(container, SWT.CHECK);

			datCreateAfter = new DateTime(container, SWT.BORDER);
			datCreateAfter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setText("Modify Before:");

			ckbModifyBefore = new Button(container, SWT.CHECK);

			datModifyBefore = new DateTime(container, SWT.BORDER);
			datModifyBefore.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			Label lblPrefixName = new Label(container, SWT.NONE);
			lblPrefixName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPrefixName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPrefixName.setText("Modify After:");

			ckbModifyAfter = new Button(container, SWT.CHECK);

			datModifyAfter = new DateTime(container, SWT.BORDER);
			datModifyAfter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			lblModelCode = new Label(container, SWT.NONE);
			lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModelCode.setText("Process Name:");

			txtProcessName = new Text(container, SWT.BORDER);
			txtProcessName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			lblModuleName = new Label(container, SWT.NONE);
			lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModuleName.setText("Running Process:");

			ckbRunningProcess = new Button(container, SWT.CHECK);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);

			lblAddingNumber = new Label(container, SWT.NONE);
			lblAddingNumber.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblAddingNumber.setText("Model:");
			lblAddingNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			lstModel = new List(container, SWT.BORDER | SWT.H_SCROLL);
			lstModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			btnRemoveModel = new Button(container, SWT.FLAT);
			btnRemoveModel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnRemoveModel.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			new Label(container, SWT.NONE);

			cbModel = new Combo(container, SWT.READ_ONLY);
			cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			btnAddModel = new Button(container, SWT.FLAT);
			btnAddModel.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
		btnAccept.setText("Export");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 576);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Export Report");
	}
}