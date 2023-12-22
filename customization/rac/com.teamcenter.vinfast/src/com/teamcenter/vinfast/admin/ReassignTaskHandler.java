package com.teamcenter.vinfast.admin;

import java.util.Calendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.RemoveSignoffsInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class ReassignTaskHandler extends AbstractHandler {
	private TCSession session;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		if (session.getGroup().toString().compareToIgnoreCase("dba") != 0) {
			MessageBox.post("You don't have permission to use this command", "WARNING", MessageBox.WARNING);
			return null;
		}
		if (!session.hasBypass()) {
			try {
				session.enableBypass(true);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		Shell shell = new Shell();
		ReassignTaskDialog dlg = new ReassignTaskDialog(shell, shell.getStyle());
		dlg.open();
		Button btnOK = dlg.getOKButton();
		btnOK.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				Table partTable = dlg.getSearchTable();
				TableItem[] items = partTable.getSelection();
				TCComponent grpMemberCompo = dlg.getMapGrpMember().get(items[0].getText(3));
				String newUserId = "";
				String newGroup = "";
				String newRole = "";
				try {
					newUserId = ((TCComponentGroupMember) grpMemberCompo).getUserId();
					newGroup = ((TCComponentGroupMember) grpMemberCompo).getGroup().toString();
					newRole = ((TCComponentGroupMember) grpMemberCompo).getRole().toString();
				} catch (TCException e1) {
					MessageBox.post(e1.toString(), "ERROR", MessageBox.ERROR);
					e1.printStackTrace();
					return;
				}
				InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
				for (InterfaceAIFComponent comp : targetComp) {
					TCComponentTask targetTsk = (TCComponentTask) comp;
					if (targetTsk.getType().compareToIgnoreCase("EPMDotask") == 0 || targetTsk.getType().compareToIgnoreCase("EPMConditionTask") == 0) {
						// TODO reassign Do Task or Condition Task
						try {
							targetTsk.setResponsibleParty(((TCComponentGroupMember) grpMemberCompo).getUser());
						} catch (TCException e) {
							e.printStackTrace();
						}
					} else if (targetTsk.getType().compareToIgnoreCase("EPMPerformSignoffTask") == 0) {
						// TODO reassign Review Task or Acknowledge Task
						try {
							TCComponentSignoff[] signoffObj = targetTsk.getValidSignoffs();
							int szSignoff = signoffObj.length;
							for (int i = 0; i < szSignoff; i++) {
								TCComponentGroupMember grpMem = signoffObj[i].getGroupMember();
								String userId = grpMem.getUserId();
								String group = grpMem.getGroup().toString();
								String role = grpMem.getRole().toString();

								if (dlg.txtReassignFrom.getText().compareToIgnoreCase(userId) == 0) {
									if (userId.compareToIgnoreCase(newUserId) != 0 || group.compareToIgnoreCase(newGroup) != 0 || role.compareToIgnoreCase(newRole) != 0) {
										String taskRes = targetTsk.getPropertyDisplayableValue("task_result");
										String taskStatus = targetTsk.getPropertyDisplayableValue("task_state");
										if (taskRes.compareToIgnoreCase("Unset") == 0 && taskStatus.compareToIgnoreCase("Started") != 0) {
											TCComponentTask reviewTask = targetTsk.getParent();
											TCComponentTask selectSignOffTsk = reviewTask.getSubtasks()[0];
											String taskType = selectSignOffTsk.getTaskType();
											if (taskType.compareToIgnoreCase("EPMSelectSignoffTask") != 0) {
												selectSignOffTsk = reviewTask.getSubtasks()[1];
											}
											// in this case we need to use other way to re-assign task
											CreateSignoffs createSignoff = new CreateSignoffs();
											createSignoff.task = selectSignOffTsk;
											createSignoff.signoffInfo = new CreateSignoffInfo[1];
											createSignoff.signoffInfo[0] = new CreateSignoffInfo();
											createSignoff.signoffInfo[0].originType = "SOA_EPM_ORIGIN_UNDEFINED";
											createSignoff.signoffInfo[0].signoffAction = "SOA_EPM_Review";
											createSignoff.signoffInfo[0].signoffMember = grpMemberCompo;

											ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
											if (addSignoffsResponse.sizeOfPartialErrors() <= 0) {
												// TODO remove current signoff
												RemoveSignoffsInfo removeInfo = new RemoveSignoffsInfo();
												removeInfo.removeSignoffObjs = new TCComponentSignoff[] { signoffObj[i] };
												removeInfo.task = selectSignOffTsk;
												ServiceData removeSgOffRes = WorkflowService.getService(session).removeSignoffs(new RemoveSignoffsInfo[] { removeInfo });
												if (removeSgOffRes.sizeOfPartialErrors() > 0) {
													System.out.println("Cannot remove old user: " + userId);
												}
											}

											// Set adhoc done
											DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
											propInfo.object = selectSignOffTsk;
											propInfo.timestamp = Calendar.getInstance();
											propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
											propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
											propInfo.vecNameVal[0].name = "task_result";
											propInfo.vecNameVal[0].values = new String[] { "Completed" };
											DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
											if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
												System.out.println("Cannot assign task to user: " + newUserId);
											}

											// Complete selection signoff task if it started
											if (selectSignOffTsk.getTaskState().compareToIgnoreCase("Started") == 0) {
												try {
													Thread.sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
												Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
												paii.clientId = "complete" + selectSignOffTsk.getUid();
												paii.action = "SOA_EPM_complete_action";
												paii.actionableObject = selectSignOffTsk;
												paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
												paii.supportingValue = "SOA_EPM_completed";

												ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
												if (sd.sizeOfPartialErrors() > 0) {
													System.out.println("Cannot assign task to user: " + newUserId);
												}
											}

										} else {
											signoffObj[i].delegate(grpMemberCompo);
										}
									}
								}
							}
						} catch (TCException | NotLoadedException | ServiceException e) {
							e.printStackTrace();
						}
					}
				}
				dlg.getShell().dispose();
			}
		});
		return null;
	}
}
