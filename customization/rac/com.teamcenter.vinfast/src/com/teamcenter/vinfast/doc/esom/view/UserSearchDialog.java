package com.teamcenter.vinfast.doc.esom.view;

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
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sc.utilities.Utilities;

public class UserSearchDialog extends AbstractSWTDialog{
	private Table tableUser;
	private Button ok;
	public TableItem[] searchItems;
	private LinkedHashMap<String, TCComponent> mapTcCompo = null;
	
	public UserSearchDialog(Shell parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		setBlockOnOpen(false);
		mapTcCompo = new LinkedHashMap<String, TCComponent>();
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
		lblUserId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUserId.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblUserId.setText("User ID:");

		Text textID = new Text(container, SWT.BORDER);
		textID.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		textID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textID.setFocus();

		Label lblEmail = new Label(container, SWT.NONE);
		GridData gd_lblRevision = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblRevision.widthHint = 68;
		lblEmail.setLayoutData(gd_lblRevision);
		lblEmail.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblEmail.setText("Email:");

		Text txtEmail = new Text(container, SWT.BORDER);
		txtEmail.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_1.widthHint = 20;
		txtEmail.setLayoutData(gd_text_1);
		
		tableUser = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		tableUser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tableUser.setHeaderVisible(true);
		tableUser.setLinesVisible(true);
		TableColumn partDetails = new TableColumn(tableUser, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(250);
		partDetails.setText("User");
		
		TableColumn partType = new TableColumn(tableUser, SWT.NONE);
		partType.setResizable(true);
		partType.setWidth(150);
		partType.setText("Email");
		
		textID.addListener(SWT.KeyUp, new Listener() {

			public void handleEvent(Event evt) {
				tableUser.deselectAll();
				if(evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					//do something here....
					String ID = textID.getText();
					String mail = txtEmail.getText();
					loadObjects(session, ID, mail);
				}
				 ok.setEnabled(false);
			}
		});
		
		txtEmail.addListener(SWT.KeyUp, new Listener() {

			public void handleEvent(Event evt) {
				tableUser.deselectAll();
				if(evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					//do something here....
					String ID = textID.getText();
					String mail = txtEmail.getText();
					loadObjects(session,ID, mail);
				}
				 ok.setEnabled(false);
			}
		});
		
		tableUser.addListener(SWT.MouseUp, new Listener() {
		      public void handleEvent(Event e) {
		    	  int count = tableUser.getSelectionCount();
		    	  if(count == 0) {
		    		  ok.setEnabled(false);
		    	  }
		    	  else {
		    		  ok.setEnabled(true);
		    	  }
		    	  tableUser.forceFocus();
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
		newShell.setText("Search Dialog...");
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
		return this.tableUser;
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
	}
	
	public TableItem[] getSelectedTableItems() {
		
		return this.searchItems;
	}
	
	public void loadObjects(TCSession session, String userId, String mail) {
		TCComponent[] objects = null;

		if(userId.equals("") && mail.equals("")) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return; 
		}
		else {
			if(userId.equals("")) {
				userId = "*";
			}
			if(mail.equals("")) {
				mail = "*";
			}

			String[] entries = new String[] {"User ID", "Email"};
			String[] values = new String[] {userId, mail};
			objects = new Utilities().searchObjects(session, entries, values, "__TNH_Find_User");

			if(objects != null) {
				
				tableUser.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] {"object_string"});
				
				for(TCComponent obj : objects) {
					
					try {
						String objStr = obj.getProperty("object_string");
						TCComponentUser user = (TCComponentUser)obj;
						TCComponent personCompo = user.getRelatedComponent("person");
						String email = personCompo.getProperty("PA9");
						mapTcCompo.put(objStr, obj);
						setTableRow(new String[] {objStr, email});
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				tableUser.redraw();
			}
			else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...", MessageBox.ERROR);
				return; 
			}
		}
	}
	
	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tableUser, SWT.NONE);
		row.setText(values);
	}
	
	public LinkedHashMap<String, TCComponent> getMapTcCompo() {
		return mapTcCompo;
	}
}
