package com.teamcenter.vinfast.car.manuf.update;

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
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class MEPartAttributesUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Label lblPartTraceabilityIndicator;
	public Combo cbPartTraceability;
	public Button btnInfoPartTraceability;
	private Label lblIsAfterSale;
	public Button btnInfoIsAfterSale;
	private Composite composite;
	public Button rbtIsAfterSaleTrue;
	public Button rbtIsAfterSaleFalse;

	public MEPartAttributesUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("ME Part Attributes Update");
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblPartTraceabilityIndicator = new Label(container, SWT.NONE);
				lblPartTraceabilityIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartTraceabilityIndicator.setText("Part Traceability Indicator:");
				lblPartTraceabilityIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbPartTraceability = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoPartTraceability = new Button(container, SWT.NONE);
				btnInfoPartTraceability.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblIsAfterSale = new Label(container, SWT.NONE);
				lblIsAfterSale.setText("Is After Sale Revelant:");
			}
			{
				composite = new Composite(container, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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
			{
				btnInfoIsAfterSale = new Button(container, SWT.NONE);
				btnInfoIsAfterSale.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 250);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("ME Part Attributes Update");
	}
}