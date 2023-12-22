package com.teamcenter.vinfast.escooter.sorprocess.view;

import java.util.LinkedHashMap;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class GroupMemberSearchDialog extends AbstractSWTDialog{

	private Table tblSearch;
	private Button ok;
	public TableItem[] searchItems;
	private LinkedHashMap<String, TCComponent> mapGrpMember = null;
	
	public GroupMemberSearchDialog(Shell parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		setBlockOnOpen(false);
		mapGrpMember = new LinkedHashMap<String, TCComponent>();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(4, false);
		gl_shell.horizontalSpacing = 7;
		gl_shell.verticalSpacing = 7;
		gl_shell.marginRight = 10;
		gl_shell.marginLeft = 10;
		gl_shell.marginBottom = 5;
		gl_shell.marginHeight = 10;
		container.setLayout(gl_shell);

		Label lblUserId = new Label(container, SWT.NONE);
		lblUserId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUserId.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblUserId.setText("User ID:");

		Text txtUserId = new Text(container, SWT.BORDER);
		txtUserId.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtUserId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtUserId.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("Search");
		new Label(container, SWT.NONE);

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);

		TableColumn user = new TableColumn(tblSearch, SWT.NONE);
		user.setResizable(true);
		user.setWidth(200);
		user.setText("User");

		TableColumn group = new TableColumn(tblSearch, SWT.NONE);
		group.setResizable(true);
		group.setWidth(200);
		group.setText("Group");

		TableColumn role = new TableColumn(tblSearch, SWT.NONE);
		role.setResizable(true);
		role.setWidth(200);
		role.setText("Role");
		
		TableColumn name = new TableColumn(tblSearch, SWT.NONE);
		name.setResizable(true);
		name.setWidth(200);
		name.setText("Name");
		
		txtUserId.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if(evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					getGrpMember(session, txtUserId);
				}
			}
		});
		
		btnSearch.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				getGrpMember(session, txtUserId);
			}
		});
		
		tblSearch.addListener(SWT.MouseUp, new Listener() {
		      public void handleEvent(Event e) {
		    	  int count = tblSearch.getSelectionCount();
		    	  if(count == 0) {
		    		  ok.setEnabled(false);
		    	  }else {
		    		  ok.setEnabled(true);
		    	  }
		    	  tblSearch.forceFocus();
		      }
		});
		
		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		ok = getButton(IDialogConstants.OK_ID);
		ok.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Assign To...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}
	
	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}
	
	public void setSelectedTableItems(TableItem[] items) {
		this.searchItems = items;
	}
	
	public Table getSearchTable() {
		return this.tblSearch;
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
	}
	
	public TableItem[] getSelectedTableItems() {
		
		return this.searchItems;
	}
	
	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblSearch, SWT.NONE);
		row.setText(values);
	}
	
	public LinkedHashMap<String, TCComponent> getMapGrpMember() {
		return mapGrpMember;
	}

	private void getGrpMember(TCSession session, Text txtUserId) {
		TCComponent[] objects = null;
		String id = txtUserId.getText();
		if (id.equals("")) {
			MessageBox.post("Search fields cannot be empty.","ERROR", MessageBox.ERROR);
		} else {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Id", id);
			objects = Query.queryItem(session, queryInput, "Admin - User Memberships");

			if (objects != null) {

				tblSearch.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] { "user", "group", "role", "object_string"});

				for (TCComponent obj : objects) {
					try {
						String[] propValues = obj.getProperties(new String[] { "user", "group", "role", "object_string" });
						mapGrpMember.put(propValues[3], obj);
						setTableRow(propValues);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MessageBox.post("Exception: " + e.toString(),"ERROR", MessageBox.ERROR);
					}
				}
				tblSearch.redraw();
			} else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try",
						"Query...", MessageBox.ERROR);
				return;
			}
		}
	}
	
}
