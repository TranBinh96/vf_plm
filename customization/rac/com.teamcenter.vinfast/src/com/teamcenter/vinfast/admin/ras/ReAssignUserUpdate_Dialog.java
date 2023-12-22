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

public class ReAssignUserUpdate_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private Text txtUserID;
	private Table tblUserSearch;
	private Table tblUserSelect;
	private Table tblTask;

	private Button btnAccept;
	private Button btnRemoveUser;
	private Button btnAddUser;

	private TCSession session;

	private Set<TCComponentTask> taskList = null;
	private LinkedList<TCComponentGroupMember> userLists = null;
	private LinkedList<TCComponentGroupMember> userSearch = null;
	private String updateType;
	private TableColumn partDetails_1_2;
	private TableColumn partDetails;
	private TableColumn tblclmnDefault;
	private TableColumn partDetails_1_3;
	private TableColumn tblclmnNewColumn;

	public ReAssignUserUpdate_Dialog(Shell parentShell, TCSession session, Set<TCComponentTask> taskList, String updateType) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.session = session;
		this.taskList = taskList;
		this.updateType = updateType;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		userLists = new LinkedList<TCComponentGroupMember>();

		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		parent.setFocus();

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			txtUserID = new Text(container, SWT.BORDER);
			txtUserID.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			GridData gd_txtUserID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtUserID.widthHint = 474;
			txtUserID.setLayoutData(gd_txtUserID);
			txtUserID.setMessage("UserID");
			txtUserID.setFocus();
			txtUserID.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						seachUser();
					}
				}
			});

			btnAddUser = new Button(container, SWT.NONE);
			GridData gd_btnAddUser = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_btnAddUser.widthHint = 35;
			btnAddUser.setLayoutData(gd_btnAddUser);
			btnAddUser.setText(">");

			btnAddUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int[] items = tblUserSearch.getSelectionIndices();
					for (int index = 0; index < items.length; index++) {
						TCComponentGroupMember member = userSearch.get(items[index]);
						if (!checkUserDuplicate(member, userLists)) {
							try {
								userLists.add(member);
								TableItem setItem = new TableItem(tblUserSelect, SWT.NONE);
								String[] propValues = member.getProperties(new String[] { "status", "default_group", "user", "group", "role", "object_string" });
								if (propValues[0].compareTo("True") == 0)
									propValues[0] = "In-Active";
								else
									propValues[0] = "Active";

								TCComponentUser user = member.getUser();
								if (user.getProperty("login_group").compareTo(propValues[3]) == 0) {
									propValues[1] = "[v]";
								}
								setItem.setText(propValues);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			});

			tblUserSelect = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblUserSelect.setLinesVisible(true);
			tblUserSelect.setHeaderVisible(true);
			GridData gd_tblUserSelect = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
			gd_tblUserSelect.heightHint = 250;
			tblUserSelect.setLayoutData(gd_tblUserSelect);

			partDetails_1_2 = new TableColumn(tblUserSelect, SWT.NONE);
			partDetails_1_2.setWidth(45);
			partDetails_1_2.setText("Status");
			partDetails_1_2.setResizable(true);

			partDetails_1_3 = new TableColumn(tblUserSelect, SWT.NONE);
			partDetails_1_3.setWidth(50);
			partDetails_1_3.setText("Default");
			partDetails_1_3.setResizable(true);

			TableColumn partDetails_1_1 = new TableColumn(tblUserSelect, SWT.NONE);
			partDetails_1_1.setWidth(90);
			partDetails_1_1.setText("User");
			partDetails_1_1.setResizable(true);

			TableColumn partType_1_1 = new TableColumn(tblUserSelect, SWT.NONE);
			partType_1_1.setWidth(80);
			partType_1_1.setText("Group");
			partType_1_1.setResizable(true);

			TableColumn tblclmnNewColumn_2_1 = new TableColumn(tblUserSelect, SWT.NONE);
			tblclmnNewColumn_2_1.setWidth(200);
			tblclmnNewColumn_2_1.setText("Role");

			TableColumn tblclmnNewColumn_1_1_1 = new TableColumn(tblUserSelect, SWT.NONE);
			tblclmnNewColumn_1_1_1.setWidth(150);
			tblclmnNewColumn_1_1_1.setText("Name");

			tblUserSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblUserSearch.setLinesVisible(true);
			tblUserSearch.setHeaderVisible(true);
			GridData gd_tblUserSearch = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_tblUserSearch.widthHint = 423;
			gd_tblUserSearch.heightHint = 120;
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
			tblclmnNewColumn_1_1.setWidth(150);
			tblclmnNewColumn_1_1.setText("Name");

			btnRemoveUser = new Button(container, SWT.NONE);
			GridData gd_btnRemoveUser = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gd_btnRemoveUser.widthHint = 35;
			btnRemoveUser.setLayoutData(gd_btnRemoveUser);
			btnRemoveUser.setText("<");

			btnRemoveUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int[] items = tblUserSelect.getSelectionIndices();
					if (items.length > 0) {
						LinkedList<TCComponentGroupMember> removeUser = new LinkedList<TCComponentGroupMember>();
						for (int index : items) {
							removeUser.add(userLists.get(index));
						}
						for (TCComponentGroupMember user : removeUser) {
							userLists.remove(user);
						}
						tblUserSelect.remove(items);
					}
				}
			});

			tblTask = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblTask.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
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
		return new Point(900, 800);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
		btnAccept.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to " + updateType + " ?");
				messageBox.setText(updateType + " user group member");
				int response = messageBox.open();
				if (response == SWT.YES) {
					if (updateType.compareToIgnoreCase("ADD") == 0)
						addUserMember();
					else if (updateType.compareToIgnoreCase("REMOVE") == 0)
						removeUserMember();
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
						String[] propValues = member.getProperties(new String[] { "status", "default_group", "user", "group", "role", "object_string" });
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
	//////////////////////// ADD NEW USER ////////////////////
	//////////////////////////////////////////////////////////
	private void addUserMember() {
		if (userLists == null || userLists.size() == 0) {
			MessageBox.post("Please choose user.", "Warning", MessageBox.WARNING);
			return;
		}

		for (TCComponentTask task : taskList) {
			try {
				if (task.getTaskType().compareTo(ReAssignTask_Constant.TASKTYPE_PERFORMSIGNOFF) == 0) {
					ReAssignTask_Extension.addReviewer(task, userLists, session);
				} else {
					ReAssignTask_Extension.addResponsibleParty(task, userLists.get(0));
				}
			} catch (Exception e) {
				MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
		}

		MessageBox.post(updateType + " success.", "Success", MessageBox.INFORMATION);
		this.close();
	}

	//////////////////////////////////////////////////////////
	//////////////////////// REMOVE USER /////////////////////
	//////////////////////////////////////////////////////////
	private void removeUserMember() {
		if (userLists == null || userLists.size() == 0) {
			MessageBox.post("Please choose user.", "Warning", MessageBox.WARNING);
			return;
		}

		for (TCComponentTask task : taskList) {
			try {
				ReAssignTask_Extension.removeReviewer(task, userLists, session);
			} catch (Exception e) {
				MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
		}

		MessageBox.post(updateType + " success.", "Success", MessageBox.INFORMATION);
		this.close();
	}

	private boolean checkUserDuplicate(TCComponentGroupMember item, LinkedList<TCComponentGroupMember> source) {
		if (source.size() > 0) {
			for (TCComponent part : source) {
				if (part == item)
					return true;
			}
		}

		return false;
	}
}
