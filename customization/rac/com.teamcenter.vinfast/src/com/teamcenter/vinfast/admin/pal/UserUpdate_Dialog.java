package com.teamcenter.vinfast.admin.pal;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class UserUpdate_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private Text txtUserID;
	private Table tblUserSearch;
	private Table tblUserSelect;
	private Table tblAL;

	private Button btnAccept;
	private Button btnRemoveUser;
	private Button btnAddTask;
	private Button btnRemoveTask;
	private Button btnAddUser;

	private List lstTask;
	private List lstTaskSelect;

	private TCSession session;

	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList = null;
	private LinkedList<String> taskList = null;
	private Set<TCComponentAssignmentList> alList = null;
	private LinkedList<TCComponentGroupMember> userLists = null;
	private LinkedList<TCComponentGroupMember> userSearch = null;
	private String updateType;
	private TableColumn partDetails_1_2;
	private TableColumn partDetails;
	private TableColumn tblclmnDefault;
	private TableColumn partDetails_1_3;

	public UserUpdate_Dialog(Shell parentShell, TCSession session, LinkedHashMap<String, TCComponentTaskTemplate> subTaskList, Set<TCComponentAssignmentList> alList, LinkedList<String> taskList, String updateType) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.session = session;
		this.subTaskList = subTaskList;
		this.alList = alList;
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

			lstTask = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_lstTask = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
			gd_lstTask.widthHint = 355;
			gd_lstTask.heightHint = 200;
			lstTask.setLayoutData(gd_lstTask);

			btnAddTask = new Button(container, SWT.NONE);
			GridData gd_btnAddTask = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gd_btnAddTask.widthHint = 35;
			btnAddTask.setLayoutData(gd_btnAddTask);
			btnAddTask.setText(">");

			btnAddTask.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int[] items = lstTask.getSelectionIndices();
					for (int index = 0; index < items.length; index++) {
						String item = lstTask.getItem(items[index]);
						boolean check = false;
						for (String task : lstTaskSelect.getItems()) {
							if (task.compareTo(item) == 0) {
								check = true;
								break;
							}
						}

						if (!check) {
							lstTaskSelect.add(item);
						}
					}
				}
			});

			lstTaskSelect = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_lstTaskSelect = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
			gd_lstTaskSelect.heightHint = 171;
			lstTaskSelect.setLayoutData(gd_lstTaskSelect);

			btnRemoveTask = new Button(container, SWT.NONE);
			GridData gd_btnRemoveTask = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gd_btnRemoveTask.widthHint = 35;
			btnRemoveTask.setLayoutData(gd_btnRemoveTask);
			btnRemoveTask.setText("<");

			btnRemoveTask.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int[] items = lstTaskSelect.getSelectionIndices();
					if (items.length > 0) {
						LinkedList<String> removeTask = new LinkedList<String>();
						for (int index : items) {
							removeTask.add(lstTaskSelect.getItem(index));
						}
						lstTaskSelect.remove(items);
					}
				}
			});

			tblAL = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblAL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			tblAL.setHeaderVisible(true);
			tblAL.setLinesVisible(true);

			TableColumn tblclmnAlName = new TableColumn(tblAL, SWT.NONE);
			tblclmnAlName.setWidth(241);
			tblclmnAlName.setText("AL Name");

			TableColumn tblclmnAlDesc = new TableColumn(tblAL, SWT.NONE);
			tblclmnAlDesc.setWidth(422);
			tblclmnAlDesc.setText("Al Desc");

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
		for (TCComponentAssignmentList al : alList) {
			TableItem row = new TableItem(this.tblAL, SWT.NONE);
			row.setText(new String[] { al.getName(), al.getDescription() });
		}
		for (String task : taskList) {
			lstTask.add(task);
		}
	}

	//////////////////////////////////////////////////////////
	//////////////////////// ADD NEW USER ////////////////////
	//////////////////////////////////////////////////////////
	private void addUserMember() {
		if (userLists == null || userLists.size() == 0 || lstTaskSelect.getItemCount() == 0) {
			MessageBox.post("Please choose user and task.", "Warning", MessageBox.WARNING);
			return;
		}

		for (TCComponentAssignmentList al : alList) {
			try {
				String alName = al.getName();
				String[] alDescription = new String[] { al.getDescription() };
				TCComponent[] tasks = al.getRelatedComponents("task_templates");
				TCComponentTaskTemplate taskTemplate = al.getProcessTemplate();
				ResourceMember[] currentResourcesAL = al.getDetails();
				LinkedList<ResourceMember> newResourcesAL = new LinkedList<ResourceMember>();

				if (currentResourcesAL != null && currentResourcesAL.length > 0) {
					for (ResourceMember resourceMember : currentResourcesAL) {
						newResourcesAL.add(resourceMember);
					}
				}

				for (String taskName : lstTaskSelect.getItems()) {
					int index = -1;
					for (int i = 0; i < tasks.length; i++) {
						if (tasks[i].toString().compareTo(taskName) == 0) {
							index = i;
							break;
						}
					}
					if (index >= 0) {
						TCComponentTaskTemplate selectTask = (TCComponentTaskTemplate) tasks[index];
						newResourcesAL.set(index, ALAssistant_Extension.addNewUserToResourceNotNull(currentResourcesAL[index], selectTask, userLists));
					} else {
						if (subTaskList != null && subTaskList.size() > 0) {
							TCComponentTaskTemplate subTask = subTaskList.get(taskName);
							if (subTask != null)
								newResourcesAL.add(ALAssistant_Extension.addNewUserToResourceNull(subTask, userLists));
						}
					}
				}

				al.modify(alName, alDescription, taskTemplate, newResourcesAL.toArray(new ResourceMember[0]), true);
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
		if (lstTaskSelect.getItemCount() == 0) {
			MessageBox.post("Please choose task.", "Warning", MessageBox.WARNING);
			return;
		}

		for (TCComponentAssignmentList al : alList) {
			try {
				String alName = al.getName();
				String[] alDescription = new String[] { al.getDescription() };
				TCComponent[] tasks = al.getRelatedComponents("task_templates");
				TCComponentTaskTemplate taskTemplate = al.getProcessTemplate();
				ResourceMember[] currentResourcesAL = al.getDetails();
				LinkedList<ResourceMember> newResourcesAL = new LinkedList<ResourceMember>();

				if (currentResourcesAL != null && currentResourcesAL.length > 0) {
					for (ResourceMember resourceMember : currentResourcesAL) {
						newResourcesAL.add(resourceMember);
					}
				}

				for (String taskName : lstTaskSelect.getItems()) {
					int index = -1;
					for (int i = 0; i < tasks.length; i++) {
						if (tasks[i].toString().compareTo(taskName) == 0) {
							index = i;
							break;
						}
					}
					if (index >= 0) {
						ResourceMember newResources = removeAllUserFromResourceNotNull(index, currentResourcesAL[index], (TCComponentTaskTemplate) tasks[index]);
						if (newResources != null)
							newResourcesAL.set(index, newResources);
						else
							newResourcesAL.remove(index);
					}
				}
				al.modify(alName, alDescription, taskTemplate, newResourcesAL.toArray(new ResourceMember[0]), true);
			} catch (Exception e) {
				MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
		}

		MessageBox.post(updateType + " success.", "Success", MessageBox.INFORMATION);
		this.close();
	}

	private ResourceMember removeAllUserFromResourceNotNull(int index, ResourceMember resourcesTask, TCComponentTaskTemplate taskTemplate) {
		int i = -100;
		int j = -100;
		int k = 0;

		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();

		LinkedList<Integer> newAction = new LinkedList<Integer>();

		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();

		if (userLists == null || userLists.size() == 0)
			return null;

		String taskType = taskTemplate.getType();
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0 || ALAssistant_Extension.validateUpdateConditionTask(newMembers, userLists.toArray(new TCComponent[0]))) {
			int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
			for (TCComponentGroupMember member : userLists) {
				newProfile.add(null);
				newAction.add(actionValue);
				newMembers.add(member);
			}
		}

		ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
		return newResource;
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
