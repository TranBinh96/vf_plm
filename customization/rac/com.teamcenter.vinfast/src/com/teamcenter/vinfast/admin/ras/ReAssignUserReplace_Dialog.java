package com.teamcenter.vinfast.admin.ras;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

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
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class ReAssignUserReplace_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private Text txtUserID;
	private Table tblUserSearch;
	private Table tblTask;

	private Button btnAccept;
	private Button btnChooseOldUser;
	private Button btnChooseNewUser;
	private Button btnRemoveNewUser;

	private TCSession session;

	private Set<TCComponentTask> taskList = null;
	private TCComponentGroupMember oldUser = null;
	private TCComponentGroupMember newUser = null;
	private LinkedList<TCComponentGroupMember> userSearch = null;
	private Text txtOldUser;
	private Text txtNewUser;
	private TableColumn partDetails;
	private TableColumn tblclmnDefault;
	private TableColumn tblclmnNewColumn;

	public ReAssignUserReplace_Dialog(Shell parentShell, TCSession session, Set<TCComponentTask> taskList) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.session = session;
		this.taskList = taskList;
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
			GridLayout gl_container = new GridLayout(5, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			txtUserID = new Text(container, SWT.BORDER);
			txtUserID.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			GridData gd_txtUserID = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
			gd_txtUserID.widthHint = 474;
			txtUserID.setLayoutData(gd_txtUserID);
			txtUserID.setFocus();
			txtUserID.setMessage("UserID");
			txtUserID.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						seachUser();
					}
				}
			});

			tblUserSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblUserSearch.setLinesVisible(true);
			tblUserSearch.setHeaderVisible(true);
			GridData gd_tblUserSearch = new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1);
			gd_tblUserSearch.widthHint = 399;
			gd_tblUserSearch.heightHint = 250;
			tblUserSearch.setLayoutData(gd_tblUserSearch);

			partDetails = new TableColumn(tblUserSearch, SWT.NONE);
			partDetails.setWidth(45);
			partDetails.setText("Status");
			partDetails.setResizable(true);

			tblclmnDefault = new TableColumn(tblUserSearch, SWT.NONE);
			tblclmnDefault.setWidth(50);
			tblclmnDefault.setText("Default");
			tblclmnDefault.setResizable(true);

			TableColumn partDetails_1 = new TableColumn(tblUserSearch, SWT.NONE);
			partDetails_1.setWidth(90);
			partDetails_1.setText("User");
			partDetails_1.setResizable(true);

			TableColumn partType_1 = new TableColumn(tblUserSearch, SWT.NONE);
			partType_1.setWidth(80);
			partType_1.setText("Group");
			partType_1.setResizable(true);

			TableColumn tblclmnNewColumn_2 = new TableColumn(tblUserSearch, SWT.NONE);
			tblclmnNewColumn_2.setWidth(200);
			tblclmnNewColumn_2.setText("Role");

			TableColumn tblclmnNewColumn_1_1 = new TableColumn(tblUserSearch, SWT.NONE);
			tblclmnNewColumn_1_1.setWidth(450);
			tblclmnNewColumn_1_1.setText("Name");

			btnChooseOldUser = new Button(container, SWT.NONE);
			btnChooseOldUser.setText("Choose Old User");
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);

			btnChooseNewUser = new Button(container, SWT.NONE);
			btnChooseNewUser.setText("Choose New User");

			btnRemoveNewUser = new Button(container, SWT.NONE);
			btnRemoveNewUser.setText("Remove New User");

			txtOldUser = new Text(container, SWT.BORDER);
			txtOldUser.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtOldUser.setEditable(false);
			txtOldUser.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			GridData gd_txtOldUser = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_txtOldUser.widthHint = 13;
			txtOldUser.setLayoutData(gd_txtOldUser);
			new Label(container, SWT.NONE);

			txtNewUser = new Text(container, SWT.BORDER);
			txtNewUser.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtNewUser.setEditable(false);
			txtNewUser.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtNewUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			tblTask = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblTask.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
			tblTask.setHeaderVisible(true);
			tblTask.setLinesVisible(true);

			TableColumn tblclmnAlName = new TableColumn(tblTask, SWT.NONE);
			tblclmnAlName.setWidth(180);
			tblclmnAlName.setText("Task Name");

			TableColumn tblclmnAlDesc = new TableColumn(tblTask, SWT.NONE);
			tblclmnAlDesc.setWidth(150);
			tblclmnAlDesc.setText("Task Type");

			tblclmnNewColumn = new TableColumn(tblTask, SWT.NONE);
			tblclmnNewColumn.setWidth(520);
			tblclmnNewColumn.setText("Process Name");

			btnChooseOldUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int itemIndex = tblUserSearch.getSelectionIndex();
					TCComponentGroupMember item = userSearch.get(itemIndex);
					try {
						oldUser = item;
						txtOldUser.setText(item.getProperty("object_string"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			btnChooseNewUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int itemIndex = tblUserSearch.getSelectionIndex();
					TCComponentGroupMember item = userSearch.get(itemIndex);
					try {
						newUser = item;
						txtNewUser.setText(item.getProperty("object_string"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			btnRemoveNewUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					newUser = null;
					txtNewUser.setText("");
				}
			});

			updateData();
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("User Update");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
		btnAccept.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to REPLACE ?");
				messageBox.setText("REPLACE user group member");
				int response = messageBox.open();
				if (response == SWT.YES) {
					replaceUserMember();
				}
			}
		});
	}

	private void seachUser() {
		userSearch = new LinkedList<TCComponentGroupMember>();
		tblUserSearch.deselectAll();
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("Id", txtUserID.getText());
			}
		};

		TCComponent[] objectSearch = Query.queryItem(session, queryInput, "Admin - User Memberships");
		if (objectSearch != null) {
			tblUserSearch.removeAll();
			DataManagementService dmService = DataManagementService.getService(session);
			dmService.getProperties(objectSearch, new String[] { "object_string", "object_type" });
			for (TCComponent obj : objectSearch) {
				try {
					if (obj instanceof TCComponentGroupMember) {
						TCComponentGroupMember member = (TCComponentGroupMember) obj;
						userSearch.add(member);
						String[] propValues = member.getProperties(new String[] { "status", "login_group", "user", "group", "role", "object_string" });
						if (propValues[0].compareTo("True") == 0)
							propValues[0] = "In-Active";
						else
							propValues[0] = "Active";

						TCComponentUser user = member.getUser();
						if (user.getProperty("login_group").compareTo(propValues[3]) == 0) {
							propValues[1] = "[v]";
						}
						TableItem row = new TableItem(this.tblUserSearch, SWT.NONE);
						row.setText(propValues);
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			tblUserSearch.redraw();
		}
	}

	public void updateData() {
		try {
			for (TCComponentTask task : taskList) {
				TableItem row = new TableItem(this.tblTask, SWT.NONE);
				row.setText(new String[] { task.getName(), task.getTaskType(), task.getProcess().getName() });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////
	//////////////////////// REPLACE USER ////////////////////
	//////////////////////////////////////////////////////////

	private void replaceUserMember() {
		if (oldUser == null || newUser == null) {
			MessageBox.post("Old User and New User must not null.", "Warning", MessageBox.WARNING);
			return;
		}

		for (TCComponentTask task : taskList) {
			try {
				if (task.getTaskType().compareTo(ReAssignTask_Constant.TASKTYPE_PERFORMSIGNOFF) == 0) {
					ReAssignTask_Extension.replaceReviewer(task, oldUser, newUser, session);
				} else {
					ReAssignTask_Extension.replaceResponsibleParty(task, oldUser, newUser);
				}
			} catch (Exception e) {
				MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
		}
		MessageBox.post("REPLACE success.", "Success", MessageBox.INFORMATION);
		this.close();
	}
}
