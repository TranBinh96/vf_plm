package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.escooter.sorprocess.model.AssigmentListModel;
import com.teamcenter.vinfast.model.ReplaceGroupMemberModel;
import com.teamcenter.vinfast.subdialog.SearchGroupMember_Dialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ALManagement_Handler extends AbstractHandler {
	private TCSession session;
	private ALManagement_Dialog dlg;
	private LinkedHashMap<String, TCComponentTaskTemplate> workflowDataForm;
	private List<ReplaceGroupMemberModel> itemList;
	private TCComponentGroupMember oldMember = null;

	private static String[] GROUP_PERMISSION = { "dba" };
	private static String[] TASKTYPE = { "EPMTaskTemplate", "EPMReviewTaskTemplate", "EPMConditionTaskTemplate", "EPMAcknowledgeTaskTemplate" };

	public ALManagement_Handler() {
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
			workflowDataForm = new LinkedHashMap<String, TCComponentTaskTemplate>();

			try {
				TCComponentTaskTemplateType tCComponentTaskTemplateType = (TCComponentTaskTemplateType) session.getTypeComponent("EPMTaskTemplate");
				if (tCComponentTaskTemplateType != null) {
					TCComponentTaskTemplate[] workflowList = tCComponentTaskTemplateType.getProcessTemplates(false, false, null, null, null);
					for (TCComponentTaskTemplate workflow : workflowList) {
						workflowDataForm.put(workflow.getName(), workflow);
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}

			dlg = new ALManagement_Dialog(new Shell());
			dlg.create();
			// Init UI
			dlg.cbWorkflow.setItems(workflowDataForm.keySet().toArray(new String[0]));

			dlg.cbWorkflow_EachGate.setItems(workflowDataForm.keySet().toArray(new String[0]));
			dlg.cbWorkflow_EachGate.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					dlg.cbTask_EachGate.removeAll();
					TCComponentTaskTemplate workflowSelected = workflowDataForm.get(dlg.cbWorkflow_EachGate.getText());
					try {
						if (workflowSelected != null) {
							TCComponent[] tasks = workflowSelected.getSubtaskDefinitions();
							if (tasks != null && tasks.length > 0) {
								LinkedHashMap<String, String> taskList = new LinkedHashMap<String, String>();
								for (int i = 0; i < tasks.length; i++) {
									if (tasks[i] instanceof TCComponentTaskTemplate) {
										if (checkTaskType(tasks[i].getType())) {
											taskList.put(String.valueOf(i), ((TCComponentTaskTemplate) tasks[i]).getName());
										}
									}
								}
								StringExtension.UpdateValueTextCombobox(dlg.cbTask_EachGate, taskList);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.txtALName.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						searchByUserID();
					}
				}
			});

			dlg.txtUserID.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						searchByUserID();
					}
				}
			});

			dlg.btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchByUserID();
				}
			});

			dlg.btnSearch_EachGate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					search_EachGate();
				}
			});

			dlg.btnReplace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					findNewGroupMember();
				}
			});

			dlg.btnRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(dlg.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setMessage("Do you really want to remove?");
					messageBox.setText("Remove Group Member");
					int response = messageBox.open();
					if (response == SWT.YES) {
						int tabSelectedIndex = dlg.tabFolder.getSelectionIndex();
						if (tabSelectedIndex == 0) {
							replaceGroupMember(null);
						} else {
							removeGroupMember_EachGate();
						}
					}
				}
			});

			dlg.btnExcel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					exportExcel();
				}
			});

			dlg.btnExportAL.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					exportAllALByWorkflow();
				}
			});

			dlg.btnSearch_OldMember.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					findOldGroupMember();
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectAll();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void exportExcel() {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet spreadsheet = wb.createSheet("Data");

		int i = 0;
		int j = 0;
		String[] headers = { "Workflow", "AL Name", "AL Desc", "User" };
		XSSFRow headerRow = spreadsheet.createRow(i++);
		for (String header : headers) {
			XSSFCell cell1 = headerRow.createCell(j++);
			cell1.setCellValue(header);
		}

		for (TableItem row : dlg.tblAssigner.getItems()) {
			XSSFRow bodyRow = spreadsheet.createRow(i++);

			for (int k = 0; k < headers.length; k++) {
				XSSFCell cell1 = bodyRow.createCell(k);
				cell1.setCellValue(row.getText(k + 1));
			}
		}

		try {
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

	private void exportAllALByWorkflow() {
		String workflowName = "";
		int tabSelectedIndex = dlg.tabFolder.getSelectionIndex();
		if (tabSelectedIndex == 0) {
			workflowName = dlg.cbWorkflow.getText();
		} else {
			workflowName = dlg.cbWorkflow_EachGate.getText();
		}
		TCComponentTaskTemplate workflowSelected = workflowDataForm.get(workflowName);
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

					LinkedHashMap<String, LinkedHashMap<String, String>> alMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
					TCComponent[] alList = TCExtension.getALByWorkflow(workflowName, session);
					for (TCComponent al : alList) {
						if (al instanceof TCComponentAssignmentList) {
							AssigmentListModel alItem = new AssigmentListModel((TCComponentAssignmentList) al);
							LinkedHashMap<String, String> resource = new LinkedHashMap<String, String>();
							alMap.put(alItem.getALName(), resource);
							for (String task : taskList) {
								String viewer = "";
								TCComponentGroupMember[] members = alItem.getUserListByTaskName(task);
								if (members != null) {
									for (TCComponentGroupMember member : members) {
										viewer += member.toString() + ";";
									}
								}
								resource.put(task, viewer);
							}
						}
					}

					// export excel
					XSSFWorkbook wb = new XSSFWorkbook();
					XSSFSheet spreadsheet = wb.createSheet("Data");

					int i = 0;
					int j = 0;
					XSSFRow headerRow = spreadsheet.createRow(i++);
					XSSFCell cellHeader = headerRow.createCell(j++);
					cellHeader.setCellValue("AL Name");
					for (String header : taskList) {
						XSSFCell cell1 = headerRow.createCell(j++);
						cell1.setCellValue(header);
					}

					for (Map.Entry<String, LinkedHashMap<String, String>> al : alMap.entrySet()) {
						j = 0;
						XSSFRow bodyRow = spreadsheet.createRow(i++);
						XSSFCell cellBody = bodyRow.createCell(j++);
						cellBody.setCellValue(al.getKey());
						for (Map.Entry<String, String> resource : al.getValue().entrySet()) {
							XSSFCell cell1 = bodyRow.createCell(j++);
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

	private boolean checkTaskType(String value) {
		for (String type : TASKTYPE) {
			if (type.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	private void findOldGroupMember() {
		try {
			SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), "", "", null);
			searchDlg.open();
			Button ok = searchDlg.getOKButton();

			ok.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					dlg.txtUserID_EachGate.setText("");
					int index = searchDlg.tblSearch.getSelectionIndex();
					if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
						oldMember = (TCComponentGroupMember) searchDlg.itemSearch.get(index);
						dlg.txtUserID_EachGate.setText(oldMember.toString());
					}

					searchDlg.getShell().dispose();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findNewGroupMember() {
		try {
			SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), "", "", null);
			searchDlg.open();
			Button ok = searchDlg.getOKButton();

			ok.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					int index = searchDlg.tblSearch.getSelectionIndex();
					TCComponent newMember = searchDlg.itemSearch.get(index);
					int tabSelectedIndex = dlg.tabFolder.getSelectionIndex();
					if (newMember instanceof TCComponentGroupMember) {
						if (tabSelectedIndex == 0) {
							replaceGroupMember((TCComponentGroupMember) newMember);
						} else {
							replaceGroupMember_EachGate((TCComponentGroupMember) newMember);
						}
					}

					searchDlg.getShell().dispose();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void replaceGroupMember(TCComponentGroupMember newMember) {
		int i = 0;
		LinkedHashMap<TCComponentGroupMember, Set<TCComponentAssignmentList>> resultMap = new LinkedHashMap<TCComponentGroupMember, Set<TCComponentAssignmentList>>();
		for (ReplaceGroupMemberModel item : itemList) {
			TableItem tableItem = dlg.tblAssigner.getItem(i++);
			if (tableItem.getChecked()) {
				if (resultMap.containsKey(item.getGroupMember())) {
					resultMap.get(item.getGroupMember()).add(item.getAssignmentList());
				} else {
					Set<TCComponentAssignmentList> newAl = new HashSet<TCComponentAssignmentList>();
					newAl.add(item.getAssignmentList());
					resultMap.put(item.getGroupMember(), newAl);
				}
			}
		}

		try {
			TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
			for (Map.Entry<TCComponentGroupMember, Set<TCComponentAssignmentList>> result : resultMap.entrySet()) {
				alType.replaceResource(result.getValue().toArray(new TCComponentAssignmentList[0]), result.getKey(), newMember);
			}
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			itemList = null;
			updateTable();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
		}
	}

	private void replaceGroupMember_EachGate(TCComponentGroupMember newMember) {
		int i = 0;
		LinkedHashMap<TCComponentAssignmentList, Set<TCComponentGroupMember>> resultMap = new LinkedHashMap<TCComponentAssignmentList, Set<TCComponentGroupMember>>();
		for (ReplaceGroupMemberModel item : itemList) {
			TableItem tableItem = dlg.tblAssigner.getItem(i++);
			if (tableItem.getChecked()) {
				if (resultMap.containsKey(item.getAssignmentList())) {
					resultMap.get(item.getAssignmentList()).add(item.getGroupMember());
				} else {
					Set<TCComponentGroupMember> newAl = new HashSet<TCComponentGroupMember>();
					newAl.add(item.getGroupMember());
					resultMap.put(item.getAssignmentList(), newAl);
				}
			}
		}
		try {
			for (Map.Entry<TCComponentAssignmentList, Set<TCComponentGroupMember>> result : resultMap.entrySet()) {
				TCComponentAssignmentList al = result.getKey();
				String alName = al.getName();
				String[] alDescription = new String[] { al.getDescription() };
				TCComponentTaskTemplate taskTemplate = al.getProcessTemplate();
				for (TCComponentGroupMember oldMember : result.getValue()) {
					al.modify(alName, alDescription, taskTemplate, getSelectedResources(oldMember, newMember, al), true);
				}
			}
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			itemList = null;
			updateTable();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
		}
	}

	private void removeGroupMember_EachGate() {
		int i = 0;
		LinkedHashMap<TCComponentAssignmentList, Set<TCComponentGroupMember>> resultMap = new LinkedHashMap<TCComponentAssignmentList, Set<TCComponentGroupMember>>();
		for (ReplaceGroupMemberModel item : itemList) {
			TableItem tableItem = dlg.tblAssigner.getItem(i++);
			if (tableItem.getChecked()) {
				if (resultMap.containsKey(item.getAssignmentList())) {
					resultMap.get(item.getAssignmentList()).add(item.getGroupMember());
				} else {
					Set<TCComponentGroupMember> newAl = new HashSet<TCComponentGroupMember>();
					newAl.add(item.getGroupMember());
					resultMap.put(item.getAssignmentList(), newAl);
				}
			}
		}
		try {
			for (Map.Entry<TCComponentAssignmentList, Set<TCComponentGroupMember>> result : resultMap.entrySet()) {
				TCComponentAssignmentList al = result.getKey();
				String alName = al.getName();
				String[] alDescription = new String[] { al.getDescription() };
				TCComponentTaskTemplate taskTemplate = al.getProcessTemplate();
				for (TCComponentGroupMember oldMember : result.getValue()) {
					al.modify(alName, alDescription, taskTemplate, getSelectedResources(oldMember, null, al), true);
				}
			}
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			itemList = null;
			updateTable();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
		}
	}

	private ResourceMember[] getSelectedResources(TCComponentGroupMember oldMember, TCComponentGroupMember newMember, TCComponentAssignmentList assignmentList) {
		String taskName = dlg.cbTask_EachGate.getText();
		try {
			ResourceMember[] resources = assignmentList.getDetails();
			TCComponent[] tasks = assignmentList.getRelatedComponents("task_templates");
			int index = -1;
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i].toString().compareTo(taskName) == 0) {
					index = i;
					break;
				}
			}
			if (index > -1) {
				if (newMember != null) {
					ResourceMember currentMember = resources[index];
					TCComponent[] members = currentMember.getResources();
					for (int i = 0; i < members.length; i++) {
						if (members[i] == oldMember) {
							members[i] = newMember;
						}
					}
					TCComponentProfile[] profile = currentMember.getProfiles();
					Integer[] actions = currentMember.getActions();
					int i = -100;
					int j = -100;
					int k = 0;
					ResourceMember newResource = new ResourceMember((TCComponentTaskTemplate) tasks[index], new TCComponent[] { newMember }, profile, actions, i, j, k);
					resources[index] = newResource;
					return resources;
				} else {
					return removeItemFromArray(index, resources);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ResourceMember[] removeItemFromArray(int index, ResourceMember[] input) {
		LinkedList<ResourceMember> output = new LinkedList<ResourceMember>();
		for (int i = 0; i < input.length; i++) {
			if (index == i)
				continue;
			output.add(input[i]);
		}
		return output.toArray(new ResourceMember[0]);
	}

	private void searchByUserID() {
		itemList = new LinkedList<ReplaceGroupMemberModel>();
		String userID = dlg.txtUserID.getText();

		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Id", userID);
		TCComponent[] item_search = Query.queryItem(session, inputQuery, "Admin - User Memberships");

		if (item_search != null && item_search.length > 0) {
			String wfNameSeach = dlg.cbWorkflow.getText();
			String alNameSearch = dlg.txtALName.getText();

			for (TCComponent member : item_search) {
				if (member instanceof TCComponentGroupMember) {
					try {
						TCComponentGroupMember oldMember = (TCComponentGroupMember) member;
						TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
						TCComponentAssignmentList[] assListComps = alType.findByResource(oldMember);
						TCComponentType.cacheTCPropertiesSet(assListComps, new String[] { "shared", "list_name", "list_desc" }, true);

						if (assListComps != null && assListComps.length > 0) {
							for (TCComponentAssignmentList al : assListComps) {
								if (checkContain(al, wfNameSeach, alNameSearch, "")) {
									ReplaceGroupMemberModel newItem = new ReplaceGroupMemberModel();
									newItem.setAssignmentList(al);
									newItem.setGroupMember(oldMember);
									itemList.add(newItem);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			updateTable();
		}
	}

	private void search_EachGate() {
		itemList = new LinkedList<ReplaceGroupMemberModel>();

		String wfNameSeach = dlg.cbWorkflow_EachGate.getText();
		String alNameSearch = dlg.txtALName_EachGate.getText();
		String taskNameSearch = dlg.cbTask_EachGate.getText();

		try {
			TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
			TCComponentAssignmentList[] assListComps = alType.findByResource(oldMember);
			TCComponentType.cacheTCPropertiesSet(assListComps, new String[] { "shared", "list_name", "list_desc" }, true);

			if (assListComps != null && assListComps.length > 0) {
				for (TCComponentAssignmentList al : assListComps) {
					if (checkContain(al, wfNameSeach, alNameSearch, taskNameSearch)) {
						ReplaceGroupMemberModel newItem = new ReplaceGroupMemberModel();
						newItem.setAssignmentList(al);
						newItem.setGroupMember(oldMember);
						itemList.add(newItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateTable();
	}

	private void updateTable() {
		dlg.tblAssigner.removeAll();
		dlg.lblTotalResult.setText("0 item(s)");

		if (itemList != null && itemList.size() > 0) {
			for (ReplaceGroupMemberModel item : itemList) {
				TableItem tableItem = new TableItem(dlg.tblAssigner, SWT.NONE);
				tableItem.setText(new String[] { "", item.getProcessTemplace(), item.getAssignmentListName(), item.getAssignmentListDesc(), item.getUser() });
				tableItem.setChecked(true);
			}
		}

		dlg.tblAssigner.redraw();
		if (itemList != null) {
			dlg.lblTotalResult.setText(String.valueOf(itemList.size()) + " item(s)");
		}
	}

	private boolean checkContain(TCComponentAssignmentList al, String workflowNameSearch, String alNameSearch, String taskNameSearch) {
		try {
			String workflowName = al.getProcessTemplate().toString();
			if (!workflowNameSearch.isEmpty())
				if (workflowName.compareToIgnoreCase(workflowNameSearch) != 0)
					return false;

			String alName = al.getName();
			if (!alNameSearch.isEmpty())
				if (!alName.contains(alNameSearch))
					return false;

			TCComponent[] resources = al.getRelatedComponents("resources");
			TCComponent[] tasks = al.getRelatedComponents("task_templates");
			if (!taskNameSearch.isEmpty()) {
				if (tasks != null && tasks.length > 0) {
					for (int i = 0; i < tasks.length; i++) {
						if (tasks[i] instanceof TCComponentTaskTemplate) {
							try {
								if (((TCComponentTaskTemplate) tasks[i]).getName().compareToIgnoreCase(taskNameSearch) == 0) {
									TCComponent[] users = resources[i].getRelatedComponents("resources");
									for (TCComponent user : users) {
										if (user instanceof TCComponentGroupMember) {
											if (user == oldMember) {
												return true;
											}
										}
									}
									return false;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void selectAll() {
		for (TableItem item : dlg.tblAssigner.getItems()) {
			item.setChecked(dlg.ckbCheckAll.getSelection());
		}
	}
}
