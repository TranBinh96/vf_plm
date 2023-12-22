package com.teamcenter.vinfast.car.engineering.specdoc;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.DateTime;

public class ESOMDocumentCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Text txtID;
	public Text txtName;
	public Label lblModelCode;
	public Label lblModuleName;
	public Combo cbModelCode;
	public Combo cbModuleName;
	public Button btnCreate;
	public Label lblSaleable;
	public Combo cbSaleable;
	public Label lblEspMgl;
	public Label lblVfMgl;
	public Label lblDesignPart;
	public Text txtDesignPart;
	public Table tableECRItem;
	public Table tableDVPRItem;
	public Button btnAddECR;
	public Button btnRemoveECR;
	public Button btnAddDVPR;
	public Button btnRemoveDVPR;
	public Text txtEspMgl;
	public Text txtVfMgl;
	public Button btnAddVfMgl;
	public Button btnRemoveVfMgl;
	public Button btnAddEspMgl;
	public Button btnRemoveEspMgl;
	public DateTime datTargetReleaseDate;

	public ESOMDocumentCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Create ESOM Document");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblDesignPart = new Label(container, SWT.NONE);
			lblDesignPart.setText("Design Part: (*)");
			lblDesignPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtDesignPart = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtDesignPart.setEnabled(false);
			txtDesignPart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtDesignPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			Label lblId = new Label(container, SWT.NONE);
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID:");
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

			Label lblName = new Label(container, SWT.NONE);
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

			lblModelCode = new Label(container, SWT.NONE);
			lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModelCode.setText("Model Code: (*)");

			cbModelCode = new Combo(container, SWT.READ_ONLY);
			cbModelCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

			lblModuleName = new Label(container, SWT.NONE);
			lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblModuleName.setText("Module Name: (*)");

			cbModuleName = new Combo(container, SWT.READ_ONLY);
			cbModuleName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

			lblSaleable = new Label(container, SWT.NONE);
			lblSaleable.setText("Saleable: (*)");
			lblSaleable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			cbSaleable = new Combo(container, SWT.READ_ONLY);
			GridData gd_cbSaleable = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			gd_cbSaleable.widthHint = 397;
			cbSaleable.setLayoutData(gd_cbSaleable);
			
			Label lblTargetReleaseDate = new Label(container, SWT.NONE);
			lblTargetReleaseDate.setText("Target Release Date: (*)");
			lblTargetReleaseDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			datTargetReleaseDate = new DateTime(container, SWT.BORDER);
			datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

			lblEspMgl = new Label(container, SWT.NONE);
			lblEspMgl.setText("ESP MGL: (*)");
			lblEspMgl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtEspMgl = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtEspMgl.setEnabled(false);
			txtEspMgl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtEspMgl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			btnRemoveEspMgl = new Button(container, SWT.FLAT);
			btnRemoveEspMgl.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnAddEspMgl = new Button(container, SWT.FLAT);
			btnAddEspMgl.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));

			lblVfMgl = new Label(container, SWT.NONE);
			lblVfMgl.setText("VF MGL: (*)");
			lblVfMgl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtVfMgl = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtVfMgl.setEnabled(false);
			txtVfMgl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtVfMgl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			btnRemoveVfMgl = new Button(container, SWT.FLAT);
			btnRemoveVfMgl.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnAddVfMgl = new Button(container, SWT.FLAT);
			btnAddVfMgl.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));

			Label lblEcrItems = new Label(container, SWT.NONE);
			lblEcrItems.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblEcrItems.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblEcrItems.setText("ECR Items:");

			tableECRItem = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_tableECR = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_tableECR.widthHint = 150;
			gd_tableECR.heightHint = 70;
			tableECRItem.setLayoutData(gd_tableECR);
			tableECRItem.setLinesVisible(true);
			TableColumn ecrNumber = new TableColumn(tableECRItem, SWT.FILL);
			ecrNumber.setResizable(true);
			ecrNumber.setWidth(145);
			ecrNumber.setText("ECR Number");
			TableColumn objType = new TableColumn(tableECRItem, SWT.FILL);
			objType.setResizable(true);
			objType.setWidth(135);
			objType.setText("Object Type");
			btnRemoveECR = new Button(container, SWT.FLAT);
			btnRemoveECR.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnRemoveECR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnAddECR = new Button(container, SWT.FLAT);
			btnAddECR.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnAddECR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));

			Label lbDVPRItems = new Label(container, SWT.NONE);
			lbDVPRItems.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lbDVPRItems.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lbDVPRItems.setText("DVPR Items:");

			tableDVPRItem = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_tableDVPR = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_tableDVPR.widthHint = 150;
			gd_tableDVPR.heightHint = 70;
			tableDVPRItem.setLayoutData(gd_tableDVPR);
			tableDVPRItem.setLinesVisible(true);
			TableColumn dvprNumber = new TableColumn(tableDVPRItem, SWT.FILL);
			dvprNumber.setResizable(true);
			dvprNumber.setWidth(145);
			dvprNumber.setText("DVPR Number");
			TableColumn partType = new TableColumn(tableDVPRItem, SWT.FILL);
			partType.setResizable(true);
			partType.setWidth(135);
			partType.setText("Object Type");
			btnRemoveDVPR = new Button(container, SWT.FLAT);
			btnRemoveDVPR.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnRemoveDVPR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnAddDVPR = new Button(container, SWT.FLAT);
			btnAddDVPR.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnAddDVPR.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));

		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(555, 753);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Spec Document");
	}
}
