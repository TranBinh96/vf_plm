package com.teamcenter.vinfast.car.engineering.specdoc;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.util.MessageBox;

public class DVPRDocumentTestReportImport_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnUpdate;
	public Browser brwTable;
	private Label lblNewLabel;
	public Text txtFile;
	public Button btnOpenFile;

	public DVPRDocumentTestReportImport_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		parent.setFocus();

		try {
			container = new Composite(area, SWT.NONE);
			container.setLayout(new GridLayout(3, false));
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			setTitle("Import Test Report");
			setMessage("Select file to import.", IMessageProvider.INFORMATION);

			lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Excel file:");

			txtFile = new Text(container, SWT.BORDER);
			txtFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnOpenFile = new Button(container, SWT.NONE);
			btnOpenFile.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/importobjects_16.png"));

			brwTable = new Browser(container, SWT.NONE);
			brwTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Assignment List");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 800);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnUpdate = createButton(parent, IDialogConstants.CLOSE_ID, "Import", false);
	}
}