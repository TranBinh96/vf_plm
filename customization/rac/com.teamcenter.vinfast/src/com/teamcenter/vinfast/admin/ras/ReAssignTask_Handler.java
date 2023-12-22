package com.teamcenter.vinfast.admin.ras;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ReAssignTask_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private ReAssignTask_Dialog dlg;
	private LinkedHashMap<String, TCComponentTaskTemplate> workflowMapping;
	private LinkedHashMap<String, LinkedHashMap<String, String>> taskMapping;
	private List<TCComponentTask> itemList;
	private TCComponentGroupMember oldMember = null;

	private static String[] GROUP_PERMISSION = { "dba" };
	private static String[] TASKTYPE = { ReAssignTask_Constant.TASKTYPE_DO, ReAssignTask_Constant.TASKTYPE_CONDITION, ReAssignTask_Constant.TASKTYPE_PERFORMSIGNOFF };
	private static String[] TASKTEMPLATETYPE = { ALAssistant_Constant.TASKTEMPLATE_DO, ALAssistant_Constant.TASKTEMPLATE_CONDITION, ALAssistant_Constant.TASKTEMPLATE_REVIEW, ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE };

	private TCComponentTaskTemplate workflowSelected = null;

	public ReAssignTask_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			selectedTask(AIFUtility.getCurrentApplication().getTargetComponents());

			workflowMapping = new LinkedHashMap<String, TCComponentTaskTemplate>();
			taskMapping = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			loadAllWork();

			dlg = new ReAssignTask_Dialog(new Shell());
			dlg.create();

			dlg.cbTaskType.setItems(TASKTYPE);
			dlg.cbTaskType.select(0);
			dlg.txtProcessName.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						search();
					}
				}
			});

			dlg.cbWorkflow.setItems(workflowMapping.keySet().toArray(new String[0]));
			dlg.cbWorkflow.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					dlg.cbTask.removeAll();

					String workflow = dlg.cbWorkflow.getText();
					if (workflow.isEmpty())
						return;

					if (taskMapping.containsKey(workflow)) {
						StringExtension.UpdateValueTextCombobox(dlg.cbTask, taskMapping.get(workflow));
					} else {
						workflowSelected = workflowMapping.get(dlg.cbWorkflow.getText());
						try {
							if (workflowSelected != null) {
								TCComponent[] subTasks = workflowSelected.getSubtaskDefinitions();
								if (subTasks != null && subTasks.length > 0) {
									LinkedHashMap<String, String> taskList = new LinkedHashMap<String, String>();
									for (int i = 0; i < subTasks.length; i++) {
										if (subTasks[i] instanceof TCComponentTaskTemplate) {
											if (checkTaskTemplateType(subTasks[i].getType())) {
												String taskName = ((TCComponentTaskTemplate) subTasks[i]).getName();
												taskList.put(String.valueOf(i), taskName);
											}
										}
									}
									taskMapping.put(workflow, taskList);
									StringExtension.UpdateValueTextCombobox(dlg.cbTask, taskList);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});

			dlg.btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					search();
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectAll();
				}
			});

			dlg.btnAdd.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentTask> taskList = new HashSet<TCComponentTask>();
					int i = 0;
					for (TCComponentTask item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							taskList.add(item);
						}
					}

					ReAssignUserUpdate_Dialog updateDlg = new ReAssignUserUpdate_Dialog(dlg.getShell(), session, taskList, "ADD");
					updateDlg.open();
				}
			});

			dlg.btnRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentTask> taskList = new HashSet<TCComponentTask>();
					int i = 0;
					for (TCComponentTask item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							taskList.add(item);
						}
					}

					ReAssignUserUpdate_Dialog updateDlg = new ReAssignUserUpdate_Dialog(dlg.getShell(), session, taskList, "REMOVE");
					updateDlg.open();
				}
			});

			dlg.btnReplace.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentTask> taskList = new HashSet<TCComponentTask>();
					int i = 0;
					for (TCComponentTask item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							taskList.add(item);
						}
					}

					ReAssignUserReplace_Dialog updateDlg = new ReAssignUserReplace_Dialog(dlg.getShell(), session, taskList);
					updateDlg.open();
				}
			});

//			dlg.btnExport.addListener(SWT.Selection, new Listener() {
//				@Override
//				public void handleEvent(Event arg0) {
//					exportAL();
//				}
//			});
//
//			dlg.btnExportAlByUser.addListener(SWT.Selection, new Listener() {
//				@Override
//				public void handleEvent(Event arg0) {
//					exportALByUser();
//				}
//			});
//
//			dlg.btnEditAL.addListener(SWT.Selection, new Listener() {
//				@Override
//				public void handleEvent(Event arg0) {
//					LinkedList<TCComponentAssignmentList> alList = new LinkedList<TCComponentAssignmentList>();
//					int i = 0;
//					for (ReplaceGroupMemberModel item : itemList) {
//						TableItem tableItem = dlg.tblAssigner.getItem(i++);
//						if (tableItem.getChecked()) {
//							alList.add(item.getAssignmentList());
//						}
//					}
//
//					if (alList.size() != 1) {
//						MessageBox.post("Select one AL to edit.", "Warning", MessageBox.WARNING);
//						return;
//					}
//
//					ALCreate_Dialog copyDlg = new ALCreate_Dialog(dlg.getShell(), session, subTaskList, workflowSelected, alList.get(0));
//					copyDlg.open();
//				}
//			});

			updateTable();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean checkTaskTemplateType(String value) {
		for (String type : TASKTEMPLATETYPE) {
			if (type.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	private void updateTable() {
		dlg.tblAssigner.removeAll();
		dlg.lblTotalResult.setText("0 item(s)");

		if (itemList != null && itemList.size() > 0) {
			for (TCComponentTask item : itemList) {
				TableItem tableItem = new TableItem(dlg.tblAssigner, SWT.NONE);
				try {
					tableItem.setText(new String[] { "", item.getName(), item.getTaskType(), item.getProcess().getName() });
				} catch (Exception e) {
					e.printStackTrace();
				}
				tableItem.setChecked(true);
			}
		}

		dlg.tblAssigner.redraw();
		if (itemList != null) {
			dlg.lblTotalResult.setText(String.valueOf(itemList.size()) + " item(s)");
		}
	}

	private void exportALByUser() {
		try {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("User ID", "*");
			queryInput.put("Status", "0");

			TCComponent[] searchObjects = Query.queryItem(session, queryInput, "__employeeInfo");
			if (searchObjects != null && searchObjects.length > 0) {
				SXSSFWorkbook wb = new SXSSFWorkbook();
				SXSSFSheet spreadsheet = wb.createSheet("Data");

				int i = 0;
				int j = 0;
				String[] headerTitle = { "Workflow", "AL Name", "AL Desc", "User ID", "User Group", "User Role" };
				SXSSFRow headerRow = spreadsheet.createRow(i++);
				for (String header : headerTitle) {
					SXSSFCell cellHeader = headerRow.createCell(j++);
					cellHeader.setCellValue(header);
				}

				TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
				for (TCComponent searchObject : searchObjects) {
					if (searchObject instanceof TCComponentUser) {
						TCComponentUser item = (TCComponentUser) searchObject;
						TCComponentGroupMember[] groupMember = item.getGroupMembers();
						if (groupMember.length > 0) {
							for (TCComponentGroupMember member : groupMember) {
								TCComponentAssignmentList[] assListComps = alType.findByResource(member);
								if (assListComps != null && assListComps.length > 0) {
									for (TCComponentAssignmentList al : assListComps) {
										SXSSFRow bodyRow = spreadsheet.createRow(i++);
										// WF
										SXSSFCell cellBody1 = bodyRow.createCell(0);
										cellBody1.setCellValue(al.getProcessTemplate().toString());
										// AL Name
										SXSSFCell cellBody2 = bodyRow.createCell(1);
										cellBody2.setCellValue(al.getName());
										// AL Desc
										SXSSFCell cellBody3 = bodyRow.createCell(2);
										cellBody3.setCellValue(al.getDescription());
										// User ID
										SXSSFCell cellBody4 = bodyRow.createCell(3);
										cellBody4.setCellValue(member.getUserId());
										// User Group
										SXSSFCell cellBody5 = bodyRow.createCell(4);
										cellBody5.setCellValue(member.getGroup().toString());
										// User Role
										SXSSFCell cellBody6 = bodyRow.createCell(5);
										cellBody6.setCellValue(member.getRole().toString());
									}
								}
							}
						}
					}
				}

				File file = new File(System.getenv("tmp") + "AL_Export.xlsx");
				FileOutputStream fos = new FileOutputStream(file);
				wb.write(fos);
				fos.close();
				Desktop.getDesktop().open(file);
				wb.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void selectedTask(InterfaceAIFComponent[] targetComp) {
		itemList = new LinkedList<>();
		for (InterfaceAIFComponent target : targetComp) {
			if (target instanceof TCComponentTask) {
				itemList.add((TCComponentTask) target);
			}
		}
	}

	private void search() {
		itemList = new LinkedList<>();

		try {
			String workflowName = dlg.cbWorkflow.getText();
			String processName = dlg.txtProcessName.getText();
			String taskType = dlg.cbTaskType.getText();
			String taskName = dlg.cbTask.getText();
			String userID = dlg.txtUserID.getText();

			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Workflow Name", workflowName);

			if (!dlg.cbTaskType.getText().isEmpty())
				queryInput.put("Task Type", taskType);

			if (!dlg.cbTask.getText().isEmpty())
				queryInput.put("Task Name", taskName);

			if (!dlg.txtProcessName.getText().isEmpty())
				queryInput.put("Process Name", processName);

			if (taskType.isEmpty()) {

			} else {
				TCComponent[] queryOutput = null;
				if (taskType.compareTo(ReAssignTask_Constant.TASKTYPE_PERFORMSIGNOFF) == 0) {
					queryOutput = TCExtension.queryItem(session, queryInput, "__VINF_FindReviewAcknowledgeTask");
					if (queryOutput != null && queryInput.size() > 0) {
						dmService.getProperties(queryOutput, new String[] { "awp0Reviewers" });
						for (TCComponent item : queryOutput) {
							if (item instanceof TCComponentTask) {
								if (userID.isEmpty()) {
									itemList.add((TCComponentTask) item);
									continue;
								}

								boolean check = false;
								TCComponent[] reviewers = ((TCComponentTask) item).getRelatedComponents("awp0Reviewers");
								if (reviewers != null && reviewers.length > 0) {
									for (TCComponent reviewer : reviewers) {
										if (reviewer instanceof TCComponentGroupMember) {
											if (((TCComponentGroupMember) reviewer).getUserId().compareTo(userID) == 0)
												check = true;
										}
									}
								}
								if (check)
									itemList.add((TCComponentTask) item);
							}
						}
					}
				} else {
					queryInput.put("Responsible Party", dlg.txtUserID.getText().isEmpty() ? "*" : dlg.txtUserID.getText());
					queryOutput = TCExtension.queryItem(session, queryInput, "__VINF_FindDoConTask");
					if (queryOutput != null && queryInput.size() > 0) {
						for (TCComponent item : queryOutput) {
							if (item instanceof TCComponentTask) {
								itemList.add((TCComponentTask) item);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateTable();
	}

	private void selectAll() {
		for (TableItem item : dlg.tblAssigner.getItems()) {
			item.setChecked(dlg.ckbCheckAll.getSelection());
		}
	}

	private void loadAllWork() {
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("Template Name", "*");
				}
			};
			TCComponent[] itemSearch = Query.queryItem(session, inputQuery, "__Workflow Template");
			if (itemSearch != null && itemSearch.length > 0) {
				for (TCComponent item : itemSearch) {
					if (item instanceof TCComponentTaskTemplate) {
						TCComponentTaskTemplate workflow = (TCComponentTaskTemplate) item;
						workflowMapping.put(workflow.getName(), workflow);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
