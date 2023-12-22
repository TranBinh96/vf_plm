package com.teamcenter.vinfast.subdialog;

import java.util.LinkedHashMap;
import java.util.LinkedList;

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

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.Query;

public class SearchECNRev_Dialog extends Dialog {
	private TCSession session = null;
	public Table tblSearch;
	private Button ok;

	private Text txtECNNumber;
	private Text txtRevision;

	private String objectType = "";

	public LinkedList<TCComponent> itemSearch = null;

	public SearchECNRev_Dialog(Shell parent, String objectType) {
		super(parent);
		setBlockOnOpen(false);
		this.objectType = objectType;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		itemSearch = new LinkedList<TCComponent>();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(3, false);
		container.setLayout(gl_shell);

		Label lblPartNumber = new Label(container, SWT.NONE);
		lblPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblPartNumber.setText("Item Number:");

		txtECNNumber = new Text(container, SWT.BORDER);
		txtECNNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtECNNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtECNNumber.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
		btnSearch.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				loadObjects();
			}
		});

		Label lblRevision = new Label(container, SWT.NONE);
		GridData gd_lblRevision = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblRevision.widthHint = 68;
		lblRevision.setLayoutData(gd_lblRevision);
		lblRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblRevision.setText("Revision:");

		txtRevision = new Text(container, SWT.BORDER);
		txtRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		GridData gd_txtRevision = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtRevision.widthHint = 20;
		txtRevision.setLayoutData(gd_txtRevision);

		txtRevision.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event evt) {
				tblSearch.deselectAll();
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					loadObjects();
				}
				ok.setEnabled(false);
			}
		});
		new Label(container, SWT.NONE);

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_tblSearch = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_tblSearch.widthHint = 406;
		tblSearch.setLayoutData(gd_tblSearch);
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);
		TableColumn partDetails = new TableColumn(tblSearch, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(106);
		partDetails.setText("Item Number");

		TableColumn partType = new TableColumn(tblSearch, SWT.NONE);
		partType.setResizable(true);
		partType.setWidth(60);
		partType.setText("Revision");

		TableColumn tblclmnEcrName = new TableColumn(tblSearch, SWT.NONE);
		tblclmnEcrName.setWidth(235);
		tblclmnEcrName.setText("Name");
		tblclmnEcrName.setResizable(true);

		txtECNNumber.addListener(SWT.KeyUp, new Listener() {
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
		String partNumber = txtECNNumber.getText();
		String revision = txtRevision.getText();
		TCComponent[] objects = null;
		if (partNumber.isEmpty() && revision.isEmpty()) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return;
		}
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		if (!partNumber.isEmpty())
			queryInput.put("Item ID", partNumber);
		if (!revision.isEmpty())
			queryInput.put("Revision", revision);
		queryInput.put("Type", objectType);

		objects = Query.queryItem(session, queryInput, "Item Revision...");

		if (objects != null) {
			tblSearch.removeAll();
			for (TCComponent obj : objects) {
				try {
					String[] propValues = obj.getProperties(new String[] { "item_id", "item_revision_id", "object_name" });
					itemSearch.add(obj);
					setTableRow(propValues);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			tblSearch.redraw();
		} else {
			MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...", MessageBox.ERROR);
			return;
		}
	}

	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblSearch, SWT.NONE);
		row.setText(values);
	}
}
