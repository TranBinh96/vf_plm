package com.teamcenter.vinfast.car.engineering.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.DateTime;

public class DocumentRevisionAttributeUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnAccept;
	public DateTime datTargetReleaseDate;
	public DateTime datTargetReviseDate;
	public Button btnInfoTargetReleaseDate;
	public Button btnInfoTargetReviseDate;

	public DocumentRevisionAttributeUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Attributes Update");
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblVehicleCategory = new Label(container, SWT.NONE);
			lblVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblVehicleCategory.setText("Target Release Date:");
			{
				datTargetReleaseDate = new DateTime(container, SWT.BORDER);
				datTargetReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoTargetReleaseDate = new Button(container, SWT.NONE);
				btnInfoTargetReleaseDate.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			Label lblTargetReviseDate = new Label(container, SWT.NONE);
			lblTargetReviseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTargetReviseDate.setText("Target Revise Date:");
			lblTargetReviseDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			{
				datTargetReviseDate = new DateTime(container, SWT.BORDER);
				datTargetReviseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				btnInfoTargetReviseDate = new Button(container, SWT.NONE);
				btnInfoTargetReviseDate.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(350, 343);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(300, 400);
		super.configureShell(newShell);
		newShell.setText("Document Revision Attributes");
	}
}