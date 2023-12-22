package com.teamcenter.vinfast.subdialog;

import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.APIExtension;

public class SearchIMSNumber_Dialog extends Dialog {
	public Table tblSearch;
	private Button ok;

	private Text txtIMSNumber;

	public LinkedList<TCComponent> itemSearch = null;

	public SearchIMSNumber_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		itemSearch = new LinkedList<TCComponent>();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(3, false);
		container.setLayout(gl_shell);

		Label lblPartNumber = new Label(container, SWT.NONE);
		lblPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblPartNumber.setText("IMS Number:");

		txtIMSNumber = new Text(container, SWT.BORDER);
		txtIMSNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtIMSNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtIMSNumber.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
		btnSearch.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				loadObjects();
			}
		});

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_tblSearch = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_tblSearch.widthHint = 406;
		tblSearch.setLayoutData(gd_tblSearch);
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);
		TableColumn partDetails = new TableColumn(tblSearch, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(106);
		partDetails.setText("IMS Number");

		TableColumn tblclmnEcrName = new TableColumn(tblSearch, SWT.NONE);
		tblclmnEcrName.setWidth(235);
		tblclmnEcrName.setText("Severity");
		tblclmnEcrName.setResizable(true);

		txtIMSNumber.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event evt) {
				tblSearch.deselectAll();
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					loadObjects();
				}
				ok.setEnabled(false);
			}
		});

		tblSearch.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				int count = tblSearch.getSelectionCount();
				if (count == 0) {
					ok.setEnabled(false);
				} else {
					ok.setEnabled(true);
				}
				tblSearch.forceFocus();
			}
		});

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ok = createButton(parent, IDialogConstants.OK_ID, "Accept", false);
		ok.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Search Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 400);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	@Override
	protected void okPressed() {
	}

	public void loadObjects() {
		tblSearch.removeAll();
		String imsNumber = txtIMSNumber.getText();
		if (imsNumber.isEmpty()) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return;
		}

		Map<String, String> queryOutput = APIExtension.getIMSInfo(imsNumber);
		for (Map.Entry<String, String> entry : queryOutput.entrySet()) {
			setTableRow(new String[] { entry.getKey(), entry.getValue() });
		}

		tblSearch.redraw();
	}

	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblSearch, SWT.NONE);
		row.setText(values);
	}

}
