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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class UserCopy_Dialog extends Dialog {
	private Composite area;
	private Composite container;

	private Button btnAccept;

	private Table tblAL;

	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList = null;
	private LinkedList<String> taskList = null;
	private Set<TCComponentAssignmentList> alList = null;
	private Label lblTargetTask;
	private Combo cbSourceTask;
	private Combo cbTargetTask;

	public UserCopy_Dialog(Shell parentShell, LinkedHashMap<String, TCComponentTaskTemplate> subTaskList, Set<TCComponentAssignmentList> alList, LinkedList<String> taskList) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.subTaskList = subTaskList;
		this.alList = alList;
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
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setText("Source Task:");
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

			lblTargetTask = new Label(container, SWT.NONE);
			lblTargetTask.setText("Target Task:");
			lblTargetTask.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

			cbSourceTask = new Combo(container, SWT.NONE);
			cbSourceTask.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			cbTargetTask = new Combo(container, SWT.NONE);
			cbTargetTask.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			tblAL = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblAL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			tblAL.setHeaderVisible(true);
			tblAL.setLinesVisible(true);

			TableColumn tblclmnAlName = new TableColumn(tblAL, SWT.NONE);
			tblclmnAlName.setWidth(241);
			tblclmnAlName.setText("AL Name");

			TableColumn tblclmnAlDesc = new TableColumn(tblAL, SWT.NONE);
			tblclmnAlDesc.setWidth(422);
			tblclmnAlDesc.setText("Al Desc");
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);

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
				messageBox.setMessage("Do you really want to COPY?");
				messageBox.setText("COPY user group member");
				int response = messageBox.open();
				if (response == SWT.YES) {
					copyUserMember();
				}
			}
		});
	}

	private void copyUserMember() {
		if (cbSourceTask.getText().isEmpty() || cbTargetTask.getText().isEmpty()) {
			MessageBox.post("Please choose Source Task and Target Task.", "Warning", MessageBox.WARNING);
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

				String sourceTaskName = cbSourceTask.getText();
				String targetTaskName = cbTargetTask.getText();
				int indexSource = -1;
				int indexTarget = -1;

				for (int i = 0; i < tasks.length; i++) {
					if (tasks[i].toString().compareTo(sourceTaskName) == 0) {
						indexSource = i;
					}

					if (tasks[i].toString().compareTo(targetTaskName) == 0) {
						indexTarget = i;
					}
				}

				if (indexSource == -1) {
					MessageBox.post("Source task no have user.", "Warning", MessageBox.WARNING);
					return;
				}

				if (indexTarget >= 0) {
					newResourcesAL.set(indexTarget, copyUserToResourceNotNull(indexTarget, currentResourcesAL[indexTarget], (TCComponentTaskTemplate) tasks[indexTarget], currentResourcesAL[indexSource]));
				} else {
					if (subTaskList != null && subTaskList.size() > 0) {
						for (TCComponent subTask : subTaskList.values()) {
							if (subTask instanceof TCComponentTaskTemplate) {
								if (((TCComponentTaskTemplate) subTask).getName().compareTo(targetTaskName) == 0) {
									ResourceMember resource = copyUserToResourceNull((TCComponentTaskTemplate) subTask, currentResourcesAL[indexSource]);
									if (resource != null)
										newResourcesAL.add(resource);
									break;
								}
							}
						}
					}
				}

				al.modify(alName, alDescription, taskTemplate, newResourcesAL.toArray(new ResourceMember[0]), true);
			} catch (TCException e) {
				MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
				e.printStackTrace();
			}
		}

		MessageBox.post("COPY success.", "Success", MessageBox.INFORMATION);
		this.close();
	}

	private ResourceMember copyUserToResourceNotNull(int index, ResourceMember targetTask, TCComponentTaskTemplate taskTemplate, ResourceMember sourceTask) {
		TCComponentProfile[] currentProfile = targetTask.getProfiles();
		Integer[] currentActions = targetTask.getActions();
		TCComponent[] currentMembers = targetTask.getResources();
		int i = -100;
		int j = -100;
		int k = 0;

		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();
		if (currentProfile != null && currentProfile.length > 0) {
			for (TCComponentProfile profile : currentProfile) {
				newProfile.add(profile);
			}
		}

		LinkedList<Integer> newAction = new LinkedList<Integer>();
		if (currentActions != null && currentActions.length > 0) {
			for (Integer action : currentActions) {
				newAction.add(action);
			}
		}

		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();
		if (currentMembers != null && currentMembers.length > 0) {
			for (TCComponent member : currentMembers) {
				newMembers.add(member);
			}
		}

		String taskType = taskTemplate.getType();
		TCComponent[] sourceMember = sourceTask.getResources();
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0 || ALAssistant_Extension.validateUpdateConditionTask(newMembers, sourceMember)) {
			int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
			for (TCComponent member : sourceMember) {
				newProfile.add(null);
				newAction.add(actionValue);
				newMembers.add(member);
			}
		}
		ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
		return newResource;
	}

	private ResourceMember copyUserToResourceNull(TCComponentTaskTemplate taskTemplate, ResourceMember sourceTask) {
		int i = -100;
		int j = -100;
		int k = 0;
		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();
		LinkedList<Integer> newAction = new LinkedList<Integer>();
		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();

		String taskType = taskTemplate.getType();
		TCComponent[] sourceMember = sourceTask.getResources();
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0 || ALAssistant_Extension.validateUpdateConditionTask(newMembers, sourceMember)) {
			int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
			for (TCComponent member : sourceMember) {
				newProfile.add(null);
				newAction.add(actionValue);
				newMembers.add(member);
			}
			ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
			return newResource;
		} else {
			return null;
		}
	}

	public void updateData() {
		for (TCComponentAssignmentList al : alList) {
			TableItem row = new TableItem(this.tblAL, SWT.NONE);
			row.setText(new String[] { al.getName(), al.getDescription() });
		}

		cbSourceTask.setItems(taskList.toArray(new String[0]));
		cbTargetTask.setItems(taskList.toArray(new String[0]));
	}
}
