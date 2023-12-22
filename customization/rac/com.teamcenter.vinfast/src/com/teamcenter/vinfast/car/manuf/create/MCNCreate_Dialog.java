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
import org.eclipse.wb.swt.ResourceManager;

public class MCNCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button ckbOpenOnCreate;
	public Button btnCreate;
	public Button cbbJESSOS;
	public Text txtDescription;
	public Combo cbChangeType;
	public Combo cbProgram;
	public Combo cbYear;
	public Text txtName;
	public Text txtEffective;
	public Combo cbShop;
	public Text txtDCRNumber;
	public Text txtImplements;
	public Button btnSearch;
	private Label lblId;
	public Combo cbChangeRequestType;
	public Label lblECNMCR;
	public Label lblDCROther;

	public MCNCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create Manufacturing Change Notice");
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				Label lblId_3 = new Label(container, SWT.NONE);
				lblId_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_3.setText("Synopsis: (*)");
			}
			{
				txtName = new Text(container, SWT.BORDER);
				txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblId_1 = new Label(container, SWT.NONE);
				lblId_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
				lblId_1.setText("Description: (*)");
			}
			{
				txtDescription = new Text(container, SWT.BORDER);
				GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_txtDescription.heightHint = 54;
				txtDescription.setLayoutData(gd_txtDescription);
			}
			{
				Label lblId_4 = new Label(container, SWT.NONE);
				lblId_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_4.setText("Program/Model Name: (*)");
			}
			{
				cbProgram = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblName = new Label(container, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblName.setText("Shop: (*)");
			}
			{
				cbShop = new Combo(container, SWT.READ_ONLY);
				cbShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblId_2 = new Label(container, SWT.NONE);
				lblId_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_2.setText("Change Type:");
			}
			{
				cbChangeType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbChangeType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbChangeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblId_5 = new Label(container, SWT.NONE);
				lblId_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_5.setText("Model Year:");
			}
			{
				cbYear = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblId_6 = new Label(container, SWT.NONE);
				lblId_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId_6.setText("Effective Proposal:");
			}
			{
				txtEffective = new Text(container, SWT.BORDER);
				txtEffective.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblId = new Label(container, SWT.NONE);
				lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblId.setText("Change Request Type: (*)");
			}
			{
				cbChangeRequestType = new Combo(container, SWT.READ_ONLY);
				cbChangeRequestType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbChangeRequestType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblECNMCR = new Label(container, SWT.NONE);
				lblECNMCR.setText("ECN/MCR:");
				lblECNMCR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				txtImplements = new Text(container, SWT.BORDER);
				txtImplements.setEditable(false);
				txtImplements.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSearch = new Button(container, SWT.NONE);
				btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			}
			{
				lblDCROther = new Label(container, SWT.NONE);
				lblDCROther.setText("Released DCR Number/Other:");
				lblDCROther.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				txtDCRNumber = new Text(container, SWT.BORDER);
				txtDCRNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				Label lblJESSOS = new Label(container, SWT.NONE);
				lblJESSOS.setText("Is JES/SOS impacted?");
				lblJESSOS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				cbbJESSOS = new Button(container, SWT.CHECK);
				cbbJESSOS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
		newShell.setText("Manufacturing Change Notice");
	}
}
