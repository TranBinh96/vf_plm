package com.teamcenter.vinfast.swbom.view;

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
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.util.MessageBox;

public class FRSExport_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnAccept;
	private Label lblNewLabel;
	public Text txtFRSNumber;

	public FRSExport_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("FRS");
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("FRS Number:");
			}
			{
				txtFRSNumber = new Text(container, SWT.BORDER | SWT.READ_ONLY);
				txtFRSNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Export");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(440, 369);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Export", false);
	}
}
