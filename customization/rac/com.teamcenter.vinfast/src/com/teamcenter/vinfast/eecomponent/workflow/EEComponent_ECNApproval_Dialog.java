package com.teamcenter.vinfast.eecomponent.workflow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.browser.Browser;

public class EEComponent_ECNApproval_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	public Combo cbVehicleCategory;
	public Combo cbModule;
	public Combo cbWorkflow;
	public Combo cbCombine;
	public Combo cbCostImpact;

	public Text txtID;

	public Button btnAccept;
	public Label lblMessage;
	private Label label;

	public EEComponent_ECNApproval_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginWidth = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblMessage = new Label(container, SWT.WRAP | SWT.HORIZONTAL);
				GridData gd_lblMessage = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
				gd_lblMessage.heightHint = 50;
				lblMessage.setLayoutData(gd_lblMessage);
				lblMessage.setText("Trigger ECN Process.");
				lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				lblMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
			}
			{
				Label lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ECN:");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				Label lblProgram = new Label(container, SWT.NONE);
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Vehicle Category: (*)");
			}
			{
				cbVehicleCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				Label lblWorkflow = new Label(container, SWT.NONE);
				lblWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblWorkflow.setText("Module: (*)");
			}
			{
				cbModule = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				Label lblModule = new Label(container, SWT.NONE);
				lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModule.setText("Workflow: (*)");
			}
			{
				cbWorkflow = new Combo(container, SWT.READ_ONLY);
				cbWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				Label lblCombine = new Label(container, SWT.NONE);
				lblCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCombine.setText("Combine with:");
			}
			{
				cbCombine = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbCombine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				Label lblCostImpact = new Label(container, SWT.NONE);
				lblCostImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCostImpact.setText("Cost Impact: (*)");
			}
			{
				cbCostImpact = new Combo(container, SWT.READ_ONLY);
				cbCostImpact.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCostImpact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Trigger", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 430);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("EE Component ECN Process");
	}

	public void setMessage(String mes, boolean isError) {
		lblMessage.setText(mes);
		if (isError) {
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		} else {
			lblMessage.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		}
	}
}
