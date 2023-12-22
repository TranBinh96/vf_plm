package com.teamcenter.vinfast.general.assign;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2014_06.Workflow.PerformActionInputInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.VFAddSignoffDialog;
import com.vf.utils.TCExtension;

public class VFAddSignoffProfileHandler extends AbstractHandler{
	private static String PREF_LIST = "VF_JES_SOS_LINES_INFO";
	private HashMap<String, String[]> lineUserProfile = null;
	private ProgressMonitorDialog progressDlg = null;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub

		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent selectedObject = AIFUtility.getCurrentApplication().getTargetComponent();

		
		try {
			TCComponentTask task = (TCComponentTask)selectedObject;
			if(task.getState() != TCTaskState.STARTED) {
				MessageBox.post("Select Task is not started. You can assign only after the task is started.", "Error", MessageBox.ERROR);
				return null;
			}
			VFAddSignoffDialog addSignoffDialog = new VFAddSignoffDialog(new Shell(), session);
			addSignoffDialog.create();

			String[] values = TCExtension.GetPreferenceValues(PREF_LIST, session);
			if(values.length != 0) {
				lineUserProfile = new HashMap<String, String[]>();
				for(String linevalue : values) {
					String[] lines = linevalue.split("=");
					if(lines.length==2 && lines[1].isBlank() == false) {
						lineUserProfile.put(lines[0], lines[1].split(";"));
					}

				}
				addSignoffDialog.setLineValues(lineUserProfile.keySet().toArray(new String[0]));
			}
			TCComponent[] targetObjects = task.getRelatedComponents("root_target_attachments");
			for(TCComponent target : targetObjects) {

				if (target instanceof TCComponentChangeItemRevision) {
					TCComponentItemRevision revision = (TCComponentItemRevision)target;
					addSignoffDialog.setShop(revision.getItem().getProperty("vf6_shop"));
				}

			}
			List line = addSignoffDialog.getListControl();
			line.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					addSignoffDialog.removeAllLines();
					String[] selection = line.getSelection();
					for (int i = 0; i < selection.length; i++) {
						addSignoffDialog.setTableValues(lineUserProfile.get(selection[i]));
					}
				}

			});

			Button assignButton = addSignoffDialog.getAssignButton();
			assignButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					assignSignoffProfiles(addSignoffDialog, task);
					addSignoffDialog.close();
				}
			});

			addSignoffDialog.open();
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private void assignSignoffProfiles(VFAddSignoffDialog addSignoffDialog, TCComponentTask task) {

		try {
			TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			ArrayList<String> items = addSignoffDialog.getTableItemsValue();
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(addSignoffDialog.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Assigning Signoff(s)...", IProgressMonitor.UNKNOWN);
					try {
						if(items.size() > 0) {
							ArrayList<TCComponentGroupMember> groupmembers = new ArrayList<TCComponentGroupMember>();
							for(String item : items) {
								TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(item, session);
								groupmembers.add(member);
							}
							if(groupmembers.isEmpty() == false) {
								if(task.getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 ) {
									TCComponentTask[] selectSignOffTasks = task.getSubtasks();
									for(TCComponentTask selectSignOffTask : selectSignOffTasks) {
										if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")) {
											selectSignOffTask = task.getSubtasks()[0];
											addSignoff(session, selectSignOffTask, groupmembers.toArray(new TCComponentGroupMember[0]));
										}
									} 
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void addSignoff(TCSession session, TCComponentTask selectSignOffTask, TCComponentGroupMember[] performerList) {

		try {
			WorkflowService wfService = WorkflowService.getService(session);
			if (performerList == null || performerList.length == 0) return;

			CreateSignoffs createSignoff = new CreateSignoffs();
			createSignoff.task = selectSignOffTask;
			createSignoff.signoffInfo = new CreateSignoffInfo[performerList.length];
			int j = 0;
			for (TCComponentGroupMember tcGroupMember : performerList) {
				createSignoff.signoffInfo[j] = new CreateSignoffInfo();
				createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
				createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
				createSignoff.signoffInfo[j].signoffMember = tcGroupMember;
				j++;
			}
			ServiceData addSignoffsResponse = wfService.addSignoffs(new CreateSignoffs[] { createSignoff });
			addSignoffsResponse.sizeOfPartialErrors();

			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
			propInfo.object = selectSignOffTask;
			propInfo.timestamp = Calendar.getInstance();
			propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
			propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
			propInfo.vecNameVal[0].name = "task_result";
			propInfo.vecNameVal[0].values = new String[]{"Completed"};
			DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[]{propInfo}, new String[0]);
			if(setPropertyResponse.data.sizeOfPartialErrors() > 0) {
				System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(setPropertyResponse.data));

			}
			selectSignOffTask.fireComponentSaveEvent();
			selectSignOffTask.refresh();//selectSignOffTask.getProperty("task_result");
			selectSignOffTask.setProperty("task_result", "Completed");
			selectSignOffTask.fireComponentSaveEvent();
			//Complete selection signoff task if it started
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
				PerformActionInputInfo paii = new PerformActionInputInfo();
				paii.clientId = "complete" + selectSignOffTask.getUid();
				paii.action = "SOA_EPM_complete_action";
				paii.actionableObject = selectSignOffTask;
				paii.propertyNameValues.put("comments", new String[]{"Auto Completed"});
				paii.supportingValue = "SOA_EPM_completed";

				ServiceData sd = wfService.performAction3(new PerformActionInputInfo[]{paii});
				if(sd.sizeOfPartialErrors() > 0) {
					System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(sd));

				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
