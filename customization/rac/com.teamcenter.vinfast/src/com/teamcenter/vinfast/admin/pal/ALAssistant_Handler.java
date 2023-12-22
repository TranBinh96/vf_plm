package com.teamcenter.vinfast.admin.pal;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.ReplaceGroupMemberModel;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ALAssistant_Handler extends AbstractHandler {
	private TCSession session;
	private ALAssistant_Dialog dlg;
	private LinkedHashMap<String, TCComponentTaskTemplate> workflowMapping;
	private LinkedHashMap<String, LinkedHashMap<String, String>> taskMapping;
	private List<ReplaceGroupMemberModel> itemList;
	private TCComponentGroupMember oldMember = null;

	private static String[] GROUP_PERMISSION = { "dba" };
	private static String[] TASKTYPE = { ALAssistant_Constant.TASKTEMPLATE_TASK, ALAssistant_Constant.TASKTEMPLATE_REVIEW, ALAssistant_Constant.TASKTEMPLATE_CONDITION, ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE, ALAssistant_Constant.TASKTEMPLATE_DO };

	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList = null;
	private TCComponentTaskTemplate workflowSelected = null;

	public ALAssistant_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			// get workflow list
			workflowMapping = new LinkedHashMap<String, TCComponentTaskTemplate>();
			taskMapping = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			loadAllWork();

			dlg = new ALAssistant_Dialog(new Shell());
			dlg.create();
			// Init UI

			dlg.txtALName.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						search();
					}
				}
			});

			dlg.txtALDesc.addListener(SWT.KeyUp, new Listener() {
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
									subTaskList = new LinkedHashMap<>();
									for (int i = 0; i < subTasks.length; i++) {
										if (subTasks[i] instanceof TCComponentTaskTemplate) {
											if (checkTaskType(subTasks[i].getType())) {
												String taskName = ((TCComponentTaskTemplate) subTasks[i]).getName();
												taskList.put(String.valueOf(i), taskName);
												subTaskList.put(taskName, (TCComponentTaskTemplate) subTasks[i]);
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
					Set<TCComponentAssignmentList> alList = new HashSet<TCComponentAssignmentList>();
					int i = 0;
					for (ReplaceGroupMemberModel item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							alList.add(item.getAssignmentList());
						}
					}

					LinkedList<String> taskList = new LinkedList<String>();
					for (String task : dlg.cbTask.getItems()) {
						taskList.add(task);
					}

					UserUpdate_Dialog updateDlg = new UserUpdate_Dialog(dlg.getShell(), session, subTaskList, alList, taskList, "ADD");
					updateDlg.open();
				}
			});

			dlg.btnRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentAssignmentList> alList = new HashSet<TCComponentAssignmentList>();
					int i = 0;
					for (ReplaceGroupMemberModel item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							alList.add(item.getAssignmentList());
						}
					}

					LinkedList<String> taskList = new LinkedList<String>();
					for (String task : dlg.cbTask.getItems()) {
						taskList.add(task);
					}

					UserUpdate_Dialog updateDlg = new UserUpdate_Dialog(dlg.getShell(), session, subTaskList, alList, taskList, "REMOVE");
					updateDlg.open();
				}
			});

			dlg.btnReplace.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentAssignmentList> alList = new HashSet<TCComponentAssignmentList>();
					int i = 0;
					for (ReplaceGroupMemberModel item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							alList.add(item.getAssignmentList());
						}
					}

					LinkedList<String> taskList = new LinkedList<String>();
					for (String task : dlg.cbTask.getItems()) {
						taskList.add(task);
					}

					UserReplace_Dialog updateDlg = new UserReplace_Dialog(dlg.getShell(), session, alList, taskList);
					updateDlg.open();
				}
			});

			dlg.btnCopy.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					Set<TCComponentAssignmentList> alList = new HashSet<TCComponentAssignmentList>();
					int i = 0;
					for (ReplaceGroupMemberModel item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							alList.add(item.getAssignmentList());
						}
					}

					LinkedList<String> taskList = new LinkedList<String>();
					for (String task : dlg.cbTask.getItems()) {
						taskList.add(task);
					}

					UserCopy_Dialog copyDlg = new UserCopy_Dialog(dlg.getShell(), subTaskList, alList, taskList);
					copyDlg.open();
				}
			});

			dlg.btnExport.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
//					exportALTest();
					exportAL();
				}
			});

			dlg.btnExportAlByUser.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					exportALByUser();
				}
			});

			dlg.btnEditAL.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					LinkedList<TCComponentAssignmentList> alList = new LinkedList<TCComponentAssignmentList>();
					int i = 0;
					for (ReplaceGroupMemberModel item : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(i++);
						if (tableItem.getChecked()) {
							alList.add(item.getAssignmentList());
						}
					}

					if (alList.size() != 1) {
						MessageBox.post("Select one AL to edit.", "Warning", MessageBox.WARNING);
						return;
					}

					ALCreate_Dialog copyDlg = new ALCreate_Dialog(dlg.getShell(), session, subTaskList, workflowSelected, alList.get(0));
					copyDlg.open();
				}
			});

			dlg.btnDeleteAL.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(dlg.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setMessage("Do you really want to DELETE ?");
					messageBox.setText("DELETE assignment list");
					int response = messageBox.open();
					if (response == SWT.YES) {
						LinkedList<TCComponentAssignmentList> alList = new LinkedList<TCComponentAssignmentList>();
						int i = 0;
						for (ReplaceGroupMemberModel item : itemList) {
							TableItem tableItem = dlg.tblAssigner.getItem(i++);
							if (tableItem.getChecked()) {
								alList.add(item.getAssignmentList());
							}
						}

						deleteAL(alList);
					}
				}
			});

			dlg.btnNewAl.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					ALCreate_Dialog updateDlg = new ALCreate_Dialog(dlg.getShell(), session, subTaskList, workflowSelected, null);
					updateDlg.open();
				}
			});

			dlg.btnImport.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					ALImport_Dialog importDlg = new ALImport_Dialog(dlg.getShell(), session, subTaskList, workflowSelected);
					importDlg.open();
				}
			});

			dlg.btnUpdateAlName.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					ALMultiUpdateName_Dialog multiUpdateName = new ALMultiUpdateName_Dialog(dlg.getShell(), session);
					multiUpdateName.open();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean checkTaskType(String value) {
		for (String type : TASKTYPE) {
			if (type.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	private void updateTable() {
		dlg.tblAssigner.removeAll();
		dlg.lblTotalResult.setText("0 item(s)");

		if (itemList != null && itemList.size() > 0) {
			for (ReplaceGroupMemberModel item : itemList) {
				TableItem tableItem = new TableItem(dlg.tblAssigner, SWT.NONE);
				tableItem.setText(new String[] { "", item.getAssignmentListName(), item.getAssignmentListDesc(), item.getUserByTask(dlg.cbTask.getText()) });
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

	private void exportAL() {
		TCComponentTaskTemplate workflowSelected = workflowMapping.get(dlg.cbWorkflow.getText());
		try {
			if (workflowSelected != null) {
				TCComponent[] tasks = workflowSelected.getSubtaskDefinitions();
				if (tasks != null && tasks.length > 0) {
					Set<String> taskList = new HashSet<String>();
					for (int i = 0; i < tasks.length; i++) {
						if (tasks[i] instanceof TCComponentTaskTemplate) {
							if (checkTaskType(tasks[i].getType())) {
								taskList.add(((TCComponentTaskTemplate) tasks[i]).getName());
							}
						}
					}

					LinkedHashMap<ReplaceGroupMemberModel, LinkedHashMap<String, String>> alMap = new LinkedHashMap<ReplaceGroupMemberModel, LinkedHashMap<String, String>>();
					int n = 0;
					for (ReplaceGroupMemberModel alItem : itemList) {
						TableItem tableItem = dlg.tblAssigner.getItem(n++);
						if (tableItem.getChecked()) {
							LinkedHashMap<String, String> resource = new LinkedHashMap<String, String>();
							alMap.put(alItem, resource);
							for (String task : taskList) {
								resource.put(task, alItem.getUserByTask(task));
							}
						}
					}

					// export excel
					SXSSFWorkbook wb = new SXSSFWorkbook();
					SXSSFSheet spreadsheet = wb.createSheet("Data");

					int i = 0;
					int j = 0;
					SXSSFRow headerRow = spreadsheet.createRow(i++);
					SXSSFCell cellHeader = headerRow.createCell(j++);
					cellHeader.setCellValue("AL Name");
					SXSSFCell cellHeader1 = headerRow.createCell(j++);
					cellHeader1.setCellValue("AL Desc");
					for (String header : taskList) {
						SXSSFCell cell1 = headerRow.createCell(j++);
						cell1.setCellValue(header);
					}

					for (Map.Entry<ReplaceGroupMemberModel, LinkedHashMap<String, String>> al : alMap.entrySet()) {
						j = 0;
						SXSSFRow bodyRow = spreadsheet.createRow(i++);
						SXSSFCell cellBody = bodyRow.createCell(j++);
						cellBody.setCellValue(al.getKey().getAssignmentListName());

						SXSSFCell cellBody1 = bodyRow.createCell(j++);
						cellBody1.setCellValue(al.getKey().getAssignmentListDesc());

						for (Map.Entry<String, String> resource : al.getValue().entrySet()) {
							SXSSFCell cell1 = bodyRow.createCell(j++);
							cell1.setCellValue(resource.getValue());
						}
					}

					File file = new File(System.getenv("tmp") + "AL_Export.xlsx");
					FileOutputStream fos = new FileOutputStream(file);
					wb.write(fos);
					fos.close();
					Desktop.getDesktop().open(file);
					wb.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exportALTest(String containText) {
		try {
			TCComponentTaskTemplate workflowSelected = workflowMapping.get(dlg.cbWorkflow.getText());
			TCComponent[] tasks = workflowSelected.getSubtaskDefinitions();
			Set<String> taskList = new HashSet<String>();
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i] instanceof TCComponentTaskTemplate) {
					if (checkTaskType(tasks[i].getType())) {
						taskList.add(((TCComponentTaskTemplate) tasks[i]).getName());
					}
				}
			}

			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Name", "*");

			TCComponent[] queryOut = TCExtension.queryItem(session, queryInput, "___Assignment List");

			SXSSFWorkbook wb = new SXSSFWorkbook();
			SXSSFSheet spreadsheet = wb.createSheet("Data");

			int i = 0;
			int j = 0;
			SXSSFRow headerRow = spreadsheet.createRow(i++);
			SXSSFCell cellHeader = headerRow.createCell(j++);
			cellHeader.setCellValue("AL Name");

			SXSSFCell cellHeader1 = headerRow.createCell(j++);
			cellHeader1.setCellValue("AL Desc");
			for (String header : taskList) {
				SXSSFCell cell1 = headerRow.createCell(j++);
				cell1.setCellValue(header);
			}

			for (TCComponent item1 : queryOut) {
				if (item1 instanceof TCComponentAssignmentList) {
					TCComponentAssignmentList alItem = (TCComponentAssignmentList) item1;
					if (!alItem.getName().contains(containText))
						continue;

					ReplaceGroupMemberModel alProcess = new ReplaceGroupMemberModel();
					alProcess.setAssignmentList(alItem);

					j = 0;
					SXSSFRow bodyRow = spreadsheet.createRow(i++);
					SXSSFCell cellBody = bodyRow.createCell(j++);
					cellBody.setCellValue(alItem.getName());

					SXSSFCell cellBody1 = bodyRow.createCell(j++);
					cellBody1.setCellValue(alItem.getDescription());

					for (String header : taskList) {
						SXSSFCell cell1 = bodyRow.createCell(j++);
						cell1.setCellValue(alProcess.getUserByTask(header));
					}
				}
			}

			File file = new File(System.getenv("tmp") + "AL_Export.xlsx");
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			Desktop.getDesktop().open(file);
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteAL(LinkedList<TCComponentAssignmentList> alList) {
		try {
			for (TCComponentAssignmentList alItem : alList) {
				alItem.delete();
			}
			MessageBox.post("DELETE success.", "Success", MessageBox.INFORMATION);
		} catch (Exception e) {
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	private void search() {
		itemList = new LinkedList<ReplaceGroupMemberModel>();

		try {
			TCComponentTaskTemplate workflowSelected = workflowMapping.get(dlg.cbWorkflow.getText());
			workflowSelected.refresh();
			if (workflowSelected != null) {
				String alNameSearch = dlg.txtALName.getText();
				String alDescSearch = dlg.txtALDesc.getText();
				TCComponent[] alList = workflowSelected.getTCProperty("assignment_lists").getReferenceValueArray();
				if (alList != null && alList.length > 0) {
					TCComponentType.cacheTCPropertiesSet(alList, new String[] { "shared", "list_name", "list_desc", "resources" }, true);
					for (TCComponent al : alList) {
						if (al instanceof TCComponentAssignmentList) {
							TCComponentAssignmentList alItem = (TCComponentAssignmentList) al;
							try {
								alItem.refresh();
								String alName = alItem.getName();
								String alDesc = alItem.getDescription();

								boolean check = true;

								if (!alNameSearch.isEmpty() && !alName.toUpperCase().contains(alNameSearch.toUpperCase()))
									check = false;

								if (!alDescSearch.isEmpty() && !alDesc.toUpperCase().contains(alDescSearch.toUpperCase()))
									check = false;

								if (check) {
									ReplaceGroupMemberModel newItem = new ReplaceGroupMemberModel();
									newItem.setAssignmentList(alItem);
									newItem.setGroupMember(oldMember);
									itemList.add(newItem);
								}
							} catch (Exception e) {
								e.printStackTrace();
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

//			TCComponentTaskTemplateType tCComponentTaskTemplateType = (TCComponentTaskTemplateType) session.getTypeComponent("EPMTaskTemplate");
//			if (tCComponentTaskTemplateType != null) {
//				TCComponentTaskTemplate[] workflowList = tCComponentTaskTemplateType.getProcessTemplates(false, false, null, null, null);
//				for (TCComponentTaskTemplate workflow : workflowList) {
//					workflowMapping.put(workflow.getName(), workflow);
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
