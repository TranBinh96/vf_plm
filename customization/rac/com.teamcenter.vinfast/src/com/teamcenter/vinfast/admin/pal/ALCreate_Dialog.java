package com.teamcenter.vinfast.admin.pal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class ALCreate_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private TCSession session;
	private TCComponentAssignmentList alItem = null;
	private LinkedHashMap<String, ResourceMember> newResourcesAL = null;
	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList = null;
	private LinkedList<TCComponentGroupMember> userSearch = null;
	private TCComponentTaskTemplate workflowSelected;
	private String taskSelected = null;

	private Text txtUserID;
	private Table tblUserSearch;

	private Button btnCreate;
	private Button btnUpdate;
	private Button btnRemoveUser;
	private Button btnAddUser;

	private TableColumn partDetails;
	private TableColumn tblclmnDefault;
	private Tree treeAssignList;
	private Composite composite;
	private Text txtNumeric;
	private Text txtPercent;
	private Label lblNewLabel_2;
	private Button ckbRequired;
	private Composite composite_1;
	private Label lblNewLabel_4;
	private Label lblNewLabel_5;
	private Text txtName;
	private Text txtDescription;
	private Button rbtNumberic;
	private Button rbtPercen;
	private Label lblNewLabel;
	private Button btnUpdateInfo;
	private Composite composite_2;

	public ALCreate_Dialog(Shell parentShell, TCSession session, LinkedHashMap<String, TCComponentTaskTemplate> subTaskList, TCComponentTaskTemplate workflowSelected, TCComponentAssignmentList alItem) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.session = session;
		this.subTaskList = subTaskList;
		this.workflowSelected = workflowSelected;
		this.alItem = alItem;
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
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			composite_1 = new Composite(container, SWT.NONE);
			composite_1.setLayout(new GridLayout(2, false));
			composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

			lblNewLabel_4 = new Label(composite_1, SWT.NONE);
			lblNewLabel_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_4.setText("AL Name: (*)");

			txtName = new Text(composite_1, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblNewLabel_5 = new Label(composite_1, SWT.NONE);
			lblNewLabel_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_5.setText("Description:");

			txtDescription = new Text(composite_1, SWT.BORDER);
			txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			treeAssignList = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
			treeAssignList.setHeaderVisible(true);
			GridData gd_treeAssignList = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
			gd_treeAssignList.widthHint = 516;
			treeAssignList.setLayoutData(gd_treeAssignList);
			treeAssignList.addListener(SWT.MouseUp, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					TreeItem nodeSelected = treeAssignList.getSelection()[0];
					if (nodeSelected != null) {
						if (nodeSelected.getData() instanceof TCComponentGroupMember)
							showUserInfo(nodeSelected, (TCComponentGroupMember) nodeSelected.getData());

						else if (nodeSelected.getData() instanceof Integer) {
							int level = (int) nodeSelected.getData();
							if (level != 1)
								return;

							showTaskInfo(nodeSelected);
						}
					}
				}
			});

			btnAddUser = new Button(container, SWT.NONE);
			GridData gd_btnAddUser = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_btnAddUser.widthHint = 35;
			btnAddUser.setLayoutData(gd_btnAddUser);
			btnAddUser.setText("<");

			btnAddUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					TreeItem nodeSelected = treeAssignList.getSelection()[0];
					if (nodeSelected.getData() instanceof TCComponentGroupMember)
						return;

					int level = (int) nodeSelected.getData();
					if (level != 2)
						return;

					int index = tblUserSearch.getSelectionIndex();
					TCComponentGroupMember member = userSearch.get(index);

					addUser(nodeSelected, member, null);
				}
			});

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

			btnRemoveUser = new Button(container, SWT.NONE);
			GridData gd_btnRemoveUser = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gd_btnRemoveUser.widthHint = 35;
			btnRemoveUser.setLayoutData(gd_btnRemoveUser);
			btnRemoveUser.setText(">");

			tblUserSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblUserSearch.setLinesVisible(true);
			tblUserSearch.setHeaderVisible(true);
			GridData gd_tblUserSearch = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
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

			composite_2 = new Composite(container, SWT.NONE);
			composite_2.setLayout(new GridLayout(2, false));
			composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			lblNewLabel_2 = new Label(composite_2, SWT.NONE);
			lblNewLabel_2.setText("Required:");

			ckbRequired = new Button(composite_2, SWT.CHECK);
			new Label(container, SWT.NONE);
			ckbRequired.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateUserInfo();
				}
			});

			composite = new Composite(container, SWT.BORDER);
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));

			rbtNumberic = new Button(composite, SWT.RADIO);
			rbtNumberic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			rbtNumberic.setText("Numberic");
			rbtNumberic.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					txtNumeric.setEnabled(true);
					txtPercent.setEnabled(false);
				}
			});

			txtNumeric = new Text(composite, SWT.BORDER);
			txtNumeric.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(composite, SWT.NONE);

			rbtPercen = new Button(composite, SWT.RADIO);
			rbtPercen.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			rbtPercen.setText("Percen");
			rbtPercen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					txtNumeric.setEnabled(false);
					txtPercent.setEnabled(true);
				}
			});

			txtPercent = new Text(composite, SWT.BORDER);
			txtPercent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setText("%");
			new Label(composite, SWT.NONE);

			btnUpdateInfo = new Button(composite, SWT.NONE);
			GridData gd_btnUpdate = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
			gd_btnUpdate.widthHint = 90;
			btnUpdateInfo.setLayoutData(gd_btnUpdate);
			btnUpdateInfo.setText("Update");
			btnUpdateInfo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateTaskInfo();
				}
			});
			new Label(container, SWT.NONE);

			btnRemoveUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					removeUser();
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
		newShell.setText("Assignment List");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 800);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnUpdate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
		btnUpdate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to UPDATE new item?");
				messageBox.setText("Assignment List");
				int response = messageBox.open();
				if (response == SWT.YES) {
					if (!checkRequired()) {
						MessageBox.post("Please input all required information", "Info", MessageBox.WARNING);
						return;
					}

					editAL();
				}
			}
		});

		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
		btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to CREATE new item?");
				messageBox.setText("Assignment List");
				int response = messageBox.open();
				if (response == SWT.YES) {
					if (!checkRequired()) {
						MessageBox.post("Please input all required information", "Info", MessageBox.WARNING);
						return;
					}

					createNewAL();

				}
			}
		});

		if (alItem != null) {
			btnUpdate.setVisible(true);
		} else {
			btnUpdate.setVisible(false);
		}
	}

	private void createNewAL() {
		String alName = txtName.getText();
		String[] alDesc = new String[] { txtDescription.getText() };
		try {
			TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
			alType.create(alName, alDesc, workflowSelected, true, newResourcesAL.values().toArray(new ResourceMember[0]));
			MessageBox.post("Create success.", "Info", MessageBox.INFORMATION);
			this.close();
		} catch (Exception e) {
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	private void editAL() {
		String alName = txtName.getText();
		String[] alDesc = new String[] { txtDescription.getText() };
		try {
			alItem.modify(alName, alDesc, workflowSelected, newResourcesAL.values().toArray(new ResourceMember[0]), true);
			MessageBox.post("Edit success.", "Success", MessageBox.INFORMATION);
			this.close();
		} catch (Exception e) {
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	private Boolean checkRequired() {
		if (txtName.getText().isEmpty())
			return false;

		if (newResourcesAL.size() == 0)
			return false;

		return true;
	}

	private void updateData() {
		newResourcesAL = new LinkedHashMap<>();
		ResourceMember[] currentResourcesAL = null;
		try {
			if (alItem != null) {
				currentResourcesAL = alItem.getDetails();
				txtName.setText(alItem.getName());
				txtDescription.setText(alItem.getDescription());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (TCComponentTaskTemplate subTask : subTaskList.values()) {
			String subTaskName = "";
			String subTaskType = "";
			try {
				subTaskName = subTask.getName();
				subTaskType = subTask.getType();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_REVIEW) != 0 && subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) != 0 && subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_DO) != 0
					&& subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0)
				continue;

			TreeItem treeItem = new TreeItem(treeAssignList, SWT.NONE);
			if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_REVIEW) == 0)
				treeItem.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMReviewTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) == 0)
				treeItem.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMAcknowledgeTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_DO) == 0)
				treeItem.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMDoTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_CONDITION) == 0)
				treeItem.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMConditionTask.png"));

			treeItem.setData(1);
			treeItem.setText(subTaskName);

			TreeItem userNode = new TreeItem(treeItem, SWT.NONE);
			userNode.setData(2);
			userNode.setText("Users");

			if (currentResourcesAL != null) {
				for (ResourceMember resourceMember : currentResourcesAL) {
					if (resourceMember.getTaskTemplate() == subTask) {
						for (TCComponent member : resourceMember.getResources()) {
							if (member instanceof TCComponentGroupMember)
								addUser(userNode, (TCComponentGroupMember) member, resourceMember);
						}
						break;
					}
				}
			}
		}

		for (final TreeItem item : treeAssignList.getItems()) {
			item.setExpanded(true);
		}
	}

	private void showTaskInfo(TreeItem nodeSelected) {
		taskSelected = nodeSelected.getText();
		ResourceMember resourceSelected = newResourcesAL.get(taskSelected);

		int revQuorum = resourceSelected.getReviewQuorum();
		if (revQuorum < 0) {
			rbtNumberic.setSelection(false);
			txtNumeric.setText("");
			txtNumeric.setEnabled(false);
			rbtPercen.setSelection(true);
			txtPercent.setText(String.valueOf(Math.abs(revQuorum)));
			txtPercent.setEnabled(true);
		} else {
			rbtNumberic.setSelection(true);
			txtNumeric.setText(String.valueOf(revQuorum));
			txtNumeric.setEnabled(true);
			rbtPercen.setSelection(false);
			txtPercent.setText("");
			txtPercent.setEnabled(false);
		}
	}

	private void updateTaskInfo() {
		int revQuorum = -100;
		if (rbtNumberic.getSelection()) {
			try {
				revQuorum = Integer.parseInt(txtNumeric.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				revQuorum = 0 - Integer.parseInt(txtPercent.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ResourceMember resourceSelected = newResourcesAL.get(taskSelected);
		newResourcesAL.put(taskSelected, ALAssistant_Extension.updateResourceInfo(resourceSelected, subTaskList.get(taskSelected), revQuorum, -100));
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

	// ----------------------------------------------------------- UPDATE USER
	private void showUserInfo(TreeItem nodeSelected, TCComponentGroupMember member) {
		if (nodeSelected.getData() instanceof Integer)
			return;

		String taskName = nodeSelected.getParentItem().getParentItem().getText();
		ResourceMember resourcesTask = newResourcesAL.get(taskName);

		int action = ALAssistant_Extension.getActionOfMember(resourcesTask, member);
		ckbRequired.setSelection(action == ALAssistant_Constant.AL_ACTION_REVIEW_REQUIRED || action == ALAssistant_Constant.AL_ACTION_ACKNOW_REQUIRED);
	}

	private void updateUserInfo() {
		TreeItem nodeSelected = treeAssignList.getSelection()[0];
		if (nodeSelected.getData() instanceof Integer)
			return;

		String taskName = nodeSelected.getParentItem().getParentItem().getText();
		ResourceMember resourceUpdate = ALAssistant_Extension.updateActionForUser(newResourcesAL.get(taskName), subTaskList.get(taskName), (TCComponentGroupMember) nodeSelected.getData(), ckbRequired.getSelection());
		if (resourceUpdate != null)
			newResourcesAL.put(taskName, resourceUpdate);
	}

	private void addUser(TreeItem nodeSelected, TCComponentGroupMember member, ResourceMember resourceMember) {
		String taskName = nodeSelected.getParentItem().getText();

		if (resourceMember == null) {
			if (!newResourcesAL.containsKey(taskName))
				newResourcesAL.put(taskName, ALAssistant_Extension.addNewUserToResourceNull(subTaskList.get(taskName), new LinkedList<TCComponentGroupMember>(Arrays.asList(member))));
			else
				newResourcesAL.put(taskName, ALAssistant_Extension.addNewUserToResourceNotNull(newResourcesAL.get(taskName), subTaskList.get(taskName), new LinkedList<TCComponentGroupMember>(Arrays.asList(member))));
		} else {
			newResourcesAL.put(taskName, resourceMember);
		}

		TreeItem newItem = new TreeItem(nodeSelected, SWT.BORDER);
		newItem.setText(member.toString());
		newItem.setData(member);
		newItem.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));

		nodeSelected.setExpanded(true);
	}

	private void removeUser() {
		TreeItem nodeSelected = treeAssignList.getSelection()[0];
		if (nodeSelected.getData() instanceof Integer)
			return;

		String taskName = nodeSelected.getParentItem().getParentItem().getText();
		ResourceMember resource = ALAssistant_Extension.replaceUserFromResourceNotNull(newResourcesAL.get(taskName), subTaskList.get(taskName), (TCComponentGroupMember) nodeSelected.getData(), null);
		if (resource != null)
			newResourcesAL.put(taskName, resource);
		else
			newResourcesAL.remove(taskName);
		nodeSelected.dispose();
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

	private String[] convertStringToArray(String inputStr) {
		if (inputStr.contains("\r\n")) {
			String[] str = inputStr.split("\r\n");
			if (str.length > 1) {
				return str;
			}
		}

		return new String[] { inputStr };
	}
}
