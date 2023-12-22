package com.teamcenter.vinfast.productconfigurator;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.List;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ECUFeatureCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	
	private Label lblProgram;
	public Combo cbFamilyGroup;
	private Label lblWorkflow;
	public Combo cbFamily;
	private Label lblMarket;
	private Label lblWorkflow_2;
	private Label lblWorkflow_3;
	private Label lblWorkflow_4;
	public List lstMarket;
	public Button btnRemoveMarket;
	public Button btnAddMarket;
	public Combo cbMarket;
	public List lstModel;
	public Button btnRemoveModel;
	public Combo cbModel;
	public Button btnAddModel;
	public Spinner txtVehicleVersion;
	public Text txtOptionDependency;
	
	public ECUFeatureCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("ECU Feature");
			setMessage("Define business object create information");
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			gl_container.horizontalSpacing = 2;
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Family Group:");
			}
			{
				cbFamilyGroup = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbFamilyGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbFamilyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblWorkflow = new Label(container, SWT.NONE);
				lblWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblWorkflow.setText("Feature: (*)");
			}
			{
				cbFamily = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbFamily.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbFamily.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblMarket = new Label(container, SWT.NONE);
				lblMarket.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));
				lblMarket.setText("Market:");
			}
			{
				lstMarket = new List(container, SWT.BORDER);
				lstMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				btnRemoveMarket = new Button(container, SWT.NONE);
				btnRemoveMarket.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnRemoveMarket.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			{
				cbMarket = new Combo(container, SWT.READ_ONLY);
				cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnAddMarket = new Button(container, SWT.NONE);
				btnAddMarket.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblWorkflow_2 = new Label(container, SWT.NONE);
				lblWorkflow_2.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));
				lblWorkflow_2.setText("Model:");
			}
			{
				lstModel = new List(container, SWT.BORDER);
				lstModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				btnRemoveModel = new Button(container, SWT.NONE);
				btnRemoveModel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				btnRemoveModel.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			}
			{
				cbModel = new Combo(container, SWT.READ_ONLY);
				cbModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnAddModel = new Button(container, SWT.NONE);
				btnAddModel.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				lblWorkflow_3 = new Label(container, SWT.NONE);
				lblWorkflow_3.setText("Minimum Vehicle Version:");
			}
			{
				txtVehicleVersion = new Spinner(container, SWT.BORDER);
				txtVehicleVersion.setMaximum(100000);
				txtVehicleVersion.setPageIncrement(1);
				txtVehicleVersion.setDigits(2);
				txtVehicleVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
			}
			{
				lblWorkflow_4 = new Label(container, SWT.NONE);
				lblWorkflow_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblWorkflow_4.setText("Option dependency expression:");
			}
			{
				txtOptionDependency = new Text(container, SWT.BORDER);
				txtOptionDependency.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Add", false);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 550);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 250);
		super.configureShell(newShell);
		newShell.setText("ECU Feature");
	}
}
