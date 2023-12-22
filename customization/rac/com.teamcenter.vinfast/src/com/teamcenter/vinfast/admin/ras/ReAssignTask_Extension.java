package com.teamcenter.vinfast.admin.ras;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.RemoveSignoffsInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.client.model.ServiceData;

public class ReAssignTask_Extension {
	public static void replaceResponsibleParty(TCComponentTask task, TCComponentGroupMember oldUser, TCComponentGroupMember newUser) {
		try {
			TCComponent responParty = task.getResponsibleParty();
			if (responParty instanceof TCComponentGroupMember) {
				if (responParty == oldUser)
					task.setResponsibleParty(newUser.getUser());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addResponsibleParty(TCComponentTask task, TCComponentGroupMember newUser) {
		try {
			task.setResponsibleParty(newUser.getUser());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addReviewer(TCComponentTask targetTask, LinkedList<TCComponentGroupMember> userList, TCSession session) {
		try {
			TCComponentTask parentTask = targetTask.getParent();
			TCComponentTask[] subTasks = parentTask.getSubtasks();
			TCComponentTask selectSignOffTask = null;
			for (TCComponentTask subTask : subTasks) {
				if (subTask.getTaskType().compareTo("EPMSelectSignoffTask") == 0)
					selectSignOffTask = subTask;
			}
			if (selectSignOffTask != null) {
				if (userList.size() > 0) {
					CreateSignoffs createSignoff = new CreateSignoffs();
					createSignoff.task = selectSignOffTask;
					createSignoff.signoffInfo = new CreateSignoffInfo[userList.size()];
					int j = 0;
					for (TCComponentGroupMember user : userList) {
						createSignoff.signoffInfo[j] = new CreateSignoffInfo();
						createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
						createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
						createSignoff.signoffInfo[j].signoffMember = user;
						j++;
					}

					ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
					if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
						System.out.println("Cannot add new user");
					}

					DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
					propInfo.object = selectSignOffTask;
					propInfo.timestamp = Calendar.getInstance();
					propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
					propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
					propInfo.vecNameVal[0].name = "task_result";
					propInfo.vecNameVal[0].values = new String[] { "Completed" };
					DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
					if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
						System.out.println("Cannot assign task to user.");
					}

					if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
						paii.clientId = "complete" + selectSignOffTask.getUid();
						paii.action = "SOA_EPM_complete_action";
						paii.actionableObject = selectSignOffTask;
						paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
						paii.supportingValue = "SOA_EPM_completed";

						ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
						if (sd.sizeOfPartialErrors() > 0) {
							System.out.println("Cannot assign task to user.");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeReviewer(TCComponentTask targetTask, LinkedList<TCComponentGroupMember> userList, TCSession session) {
		try {
			TCComponentTask parentTask = targetTask.getParent();
			TCComponentTask[] subTasks = parentTask.getSubtasks();
			TCComponentTask selectSignOffTask = null;
			for (TCComponentTask subTask : subTasks) {
				if (subTask.getTaskType().compareTo("EPMSelectSignoffTask") == 0)
					selectSignOffTask = subTask;
			}
			if (selectSignOffTask != null) {
				if (userList.size() > 0) {
					Set<Integer> removeSignOffIndex = new HashSet<>();
					TCComponentSignoff[] signOffItems = selectSignOffTask.getValidSignoffs();
					int i = 0;
					for (TCComponentSignoff signOffItem : signOffItems) {
						TCComponentGroupMember groupMember = signOffItem.getGroupMember();
						if (userList.contains(groupMember))
							removeSignOffIndex.add(i);

						i++;
					}

					if (removeSignOffIndex.size() >= 0) {
						for (Integer removeIndex : removeSignOffIndex) {
							RemoveSignoffsInfo removeInfo = new RemoveSignoffsInfo();
							removeInfo.removeSignoffObjs = new TCComponentSignoff[] { signOffItems[removeIndex] };
							removeInfo.task = selectSignOffTask;
							ServiceData removeSgOffRes = WorkflowService.getService(session).removeSignoffs(new RemoveSignoffsInfo[] { removeInfo });
							if (removeSgOffRes.sizeOfPartialErrors() > 0) {
								System.out.println("Cannot remove old user: " + signOffItems[removeIndex].getGroupMember().toString());
							}
						}

						DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
						propInfo.object = selectSignOffTask;
						propInfo.timestamp = Calendar.getInstance();
						propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
						propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
						propInfo.vecNameVal[0].name = "task_result";
						propInfo.vecNameVal[0].values = new String[] { "Completed" };
						DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
						if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
							System.out.println("Cannot assign task to user.");
						}

						if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
							paii.clientId = "complete" + selectSignOffTask.getUid();
							paii.action = "SOA_EPM_complete_action";
							paii.actionableObject = selectSignOffTask;
							paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
							paii.supportingValue = "SOA_EPM_completed";

							ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
							if (sd.sizeOfPartialErrors() > 0) {
								System.out.println("Cannot assign task to user.");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void replaceReviewer(TCComponentTask targetTask, TCComponentGroupMember oldUser, TCComponentGroupMember newUser, TCSession session) {
		try {
			if (oldUser == null || newUser == null)
				return;

			TCComponentTask parentTask = targetTask.getParent();
			TCComponentTask[] subTasks = parentTask.getSubtasks();
			TCComponentTask selectSignOffTask = null;
			for (TCComponentTask subTask : subTasks) {
				if (subTask.getTaskType().compareTo("EPMSelectSignoffTask") == 0)
					selectSignOffTask = subTask;
			}
			if (selectSignOffTask != null) {
				int removeSignOffIndex = -1;
				TCComponentSignoff[] signOffItems = selectSignOffTask.getValidSignoffs();
				int i = 0;
				for (TCComponentSignoff signOffItem : signOffItems) {
					TCComponentGroupMember groupMember = signOffItem.getGroupMember();
					if (groupMember == oldUser) {
						removeSignOffIndex = i;
						signOffItem.setGroupMember(newUser);
					}
					i++;
				}

//				if (removeSignOffIndex >= 0) {
//					RemoveSignoffsInfo removeInfo = new RemoveSignoffsInfo();
//					removeInfo.removeSignoffObjs = new TCComponentSignoff[] { signOffItems[removeSignOffIndex] };
//					removeInfo.task = selectSignOffTask;
//					ServiceData removeSgOffRes = WorkflowService.getService(session).removeSignoffs(new RemoveSignoffsInfo[] { removeInfo });
//					if (removeSgOffRes.sizeOfPartialErrors() > 0) {
//						System.out.println("Cannot remove old user: " + oldUser.toString());
//					}
//
//					CreateSignoffs createSignoff = new CreateSignoffs();
//					createSignoff.task = selectSignOffTask;
//					createSignoff.signoffInfo = new CreateSignoffInfo[1];
//					createSignoff.signoffInfo[0] = new CreateSignoffInfo();
//					createSignoff.signoffInfo[0].originType = "SOA_EPM_ORIGIN_UNDEFINED";
//					createSignoff.signoffInfo[0].signoffAction = "SOA_EPM_Review";
//					createSignoff.signoffInfo[0].signoffMember = newUser;
//
//					ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
//					if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
//						System.out.println("Cannot add new user");
//					}
//
//					DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
//					propInfo.object = selectSignOffTask;
//					propInfo.timestamp = Calendar.getInstance();
//					propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
//					propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
//					propInfo.vecNameVal[0].name = "task_result";
//					propInfo.vecNameVal[0].values = new String[] { "Completed" };
//					DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
//					if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
//						System.out.println("Cannot assign task to user.");
//					}
//
//					if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
//						paii.clientId = "complete" + selectSignOffTask.getUid();
//						paii.action = "SOA_EPM_complete_action";
//						paii.actionableObject = selectSignOffTask;
//						paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
//						paii.supportingValue = "SOA_EPM_completed";
//
//						ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
//						if (sd.sizeOfPartialErrors() > 0) {
//							System.out.println("Cannot assign task to user.");
//						}
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
