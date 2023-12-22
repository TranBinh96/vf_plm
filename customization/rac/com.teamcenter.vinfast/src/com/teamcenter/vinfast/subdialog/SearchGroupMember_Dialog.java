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
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.Query;

public class SearchGroupMember_Dialog extends Dialog {
	private TCSession session = null;
	public Table tblSearch;
	private Button btnAccept;

	private Text txtUserID;

	public LinkedList<TCComponent> itemSearch = null;
	private String group = "";
	private String role = "";
	private Boolean isInActive = null;

	public SearchGroupMember_Dialog(Shell parent, String group, String role, Boolean isInActive) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		this.group = group;
		this.role = role;
		this.isInActive = isInActive;
		setBlockOnOpen(false);
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
		lblPartNumber.setText("User ID:");

		txtUserID = new Text(container, SWT.BORDER);
		txtUserID.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtUserID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtUserID.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
		btnSearch.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				loadObjects();
			}
		});

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_tblSearch = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_tblSearch.widthHint = 406;
		tblSearch.setLayoutData(gd_tblSearch);
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);

		TableColumn tblclmnStatus = new TableColumn(tblSearch, SWT.NONE);
		tblclmnStatus.setWidth(50);
		tblclmnStatus.setText("Status");
		tblclmnStatus.setResizable(true);

		TableColumn tblclmnGroupDault = new TableColumn(tblSearch, SWT.NONE);
		tblclmnGroupDault.setWidth(90);
		tblclmnGroupDault.setText("Group Default");
		tblclmnGroupDault.setResizable(true);
		TableColumn partDetails = new TableColumn(tblSearch, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(90);
		partDetails.setText("User");

		TableColumn partType = new TableColumn(tblSearch, SWT.NONE);
		partType.setResizable(true);
		partType.setWidth(200);
		partType.setText("Group");

		TableColumn tblclmnNewColumn = new TableColumn(tblSearch, SWT.NONE);
		tblclmnNewColumn.setWidth(150);
		tblclmnNewColumn.setText("Role");

		TableColumn tblclmnNewColumn_1 = new TableColumn(tblSearch, SWT.NONE);
		tblclmnNewColumn_1.setWidth(180);
		tblclmnNewColumn_1.setText("Name");

		txtUserID.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event evt) {
				tblSearch.deselectAll();
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					loadObjects();
				}
				btnAccept.setEnabled(false);
			}
		});

		tblSearch.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				int count = tblSearch.getSelectionCount();
				if (count == 0) {
					btnAccept.setEnabled(false);
				} else {
					btnAccept.setEnabled(true);
				}
				tblSearch.forceFocus();
			}
		});

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.OK_ID, "Accept", false);
		btnAccept.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Search Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 400);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	@Override
	protected void okPressed() {
	}

	public void loadObjects() {
		String userID = txtUserID.getText();
		TCComponent[] objects = null;
		if (userID.isEmpty()) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return;
		}
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		queryInput.put("Id", userID);
		if (!group.isEmpty())
			queryInput.put("Group", group);

		if (!role.isEmpty())
			queryInput.put("Role", role);

		if (isInActive != null)
			queryInput.put("Group Member Status", isInActive ? "TRUE" : "FALSE");

		objects = Query.queryItem(session, queryInput, "__TNH_FindGroupMem");

		if (objects != null) {
			tblSearch.removeAll();
			for (TCComponent obj : objects) {
				try {
					if (obj instanceof TCComponentGroupMember) {
						TCComponentGroupMember member = (TCComponentGroupMember) obj;
						itemSearch.add(member);
						String[] propValues = member.getProperties(new String[] { "status", "default_group", "user", "group", "role", "object_string" });
						if (propValues[0].compareTo("True") == 0)
							propValues[0] = "In-Active";
						else
							propValues[0] = "Active";

						TCComponentUser user = member.getUser();
						if (user.getProperty("login_group").compareTo(propValues[3]) == 0) {
							propValues[1] = "[v]";
						}
						setTableRow(propValues);
					}
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
