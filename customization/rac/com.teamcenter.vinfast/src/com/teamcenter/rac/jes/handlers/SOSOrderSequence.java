package com.teamcenter.rac.jes.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Message;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2014_06.Workflow.PerformActionInputInfo;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soaictstubs.ICCT;
import com.vf.dialog.SOSOrderSequenceDialog;
import com.vf.utils.TCExtension;

public class SOSOrderSequence extends AbstractHandler {
	private TCSession session = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String error = "";
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		InterfaceAIFComponent aifComp = AIFUtility.getCurrentApplication().getTargetComponent();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Shell shell =  new Shell();
		SOSOrderSequenceDialog dialog = new SOSOrderSequenceDialog(shell, session);
		dialog.create();
		TCComponent[] childLinesArray = loadChildLines(aifComp);
		setRowValues(dialog.getTable(), childLinesArray);
		Button btn_Save = dialog.getSaveButton();
		btn_Save.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				HashMap<TCComponent, String> values = new HashMap<>();
				TableItem[] items = dialog.getTable().getItems();
				for(TableItem item : items) {
					TCComponent bomline = (TCComponent) item.getData();
					String findNo = item.getText(1);
					values.put(bomline, findNo);
				}
				if (progressMonitorDialog == null)
					progressMonitorDialog = new ProgressMonitorDialog(dialog.getShell());
				try {
					progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Saving...", IProgressMonitor.UNKNOWN);
							try {
								
								TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine)aifComp;
								TCComponentItemRevision revision =  selectedBOMLine.getItemRevision();
								TCComponentBOMViewRevision bvrRevision =  selectedBOMLine.getBOMViewRevision();
								ServiceData data = TCExtension.TriggerProcess(new String[] {revision.getUid(), bvrRevision.getUid()}, "__UseInJAR_Main_v1", "", "DeleteProcess", session);
								if(data.sizeOfPartialErrors() > 0) {
									error = SoaUtil.buildErrorMessage(data);
								}else {
									TCComponent process = data.getCreatedObject(0);
									TCComponentTask rootTask = ((TCComponentProcess) process).getRootTask();
									TCComponentTask[] subtasks = rootTask.getSubtasks();
									TCComponentTask autoCompleteTask = null;
									for(int i = 0; i < subtasks.length; i++) {
										if(subtasks[i].getTaskType().equals("EPMDoTask")) {
											autoCompleteTask = subtasks[i];
										}
									}
									for(TCComponent bomline : values.keySet()) {
										Map<String, VecStruct> nameValMap = new HashMap<String, VecStruct>();
										VecStruct valVec = new VecStruct();
										valVec.stringVec = new String[] { values.get(bomline) };
										nameValMap.put("bl_sequence_no", valVec);
										ServiceData serviceData = DataManagementService.getService(session).setProperties(new TCComponent[] { bomline }, nameValMap);
										if(serviceData.sizeOfPartialErrors() > 0) {
											error = SoaUtil.buildErrorMessage(data);
											break;
										}
									}
									bvrRevision.refresh();
									PerformActionInputInfo inputInfo =  new PerformActionInputInfo();
									inputInfo.actionableObject = autoCompleteTask;
									inputInfo.action = "SOA_EPM_complete_action";
									inputInfo.supportingValue = "SOA_EPM_completed";
									data = WorkflowService.getService(session).performAction3(new PerformActionInputInfo[] {inputInfo});
									if(data.sizeOfPartialErrors() > 0) {
										error = SoaUtil.buildErrorMessage(data);
									}
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							monitor.done();
							if (monitor.isCanceled())
								throw new InterruptedException("The long running operation was cancelled");
						}
					});
					dialog.close();
					if(error.isEmpty()) {
						MessageBox.post(shell, "Sequence Order changed successfully","Success",  MessageBox.INFORMATION);
					}else {
						MessageBox.post(shell, error, "Error",  MessageBox.ERROR);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
		});
		dialog.open();
		return null;
	}
	
	private TCComponent[] loadChildLines(InterfaceAIFComponent aifComp) {
		TCComponent[] childLinesArray = null;
		TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine)aifComp;
		if(selectedBOMLine.hasChildren()) {
			try {
				AIFComponentContext[] childlines = selectedBOMLine.getChildren("Mfg0allocated_ops");
				if(childlines.length !=0) {
					int i = 0;
					childLinesArray = new TCComponent[childlines.length];
					for(AIFComponentContext childline : childlines) {
						childLinesArray[i] = (TCComponent)childline.getComponent();
						i++;
					}
					DataManagementService.getService(session).getProperties(childLinesArray, new String[] {"bl_me_activity_lines","bl_indented_title","bl_sequence_no"});
				}
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return childLinesArray;
	}
	
	private void setRowValues(Table table, TCComponent[] childLinesArray) {

		try {
			for(TCComponent childline : childLinesArray) {
				TCComponent childLine = (TCComponent)childline;
				String name = getActivityName(childLine);
				if(name == null) {
					name = childLine.getProperty("bl_indented_title");
				}
				String findNo = childLine.getProperty("bl_sequence_no");
				TableItem item =  new TableItem(table, SWT.NONE);
				item.setText(0, name);
				item.setText(1, findNo);
				item.setData(childline);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getActivityName(TCComponent opLine) {

		String name = null;
		try {
			TCComponentBOPLine operationLine = (TCComponentBOPLine) opLine;
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();

			AIFComponentContext[] context = operationLine.getRelated("bl_me_activity_lines");
			if(context.length != 0) {
				TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) context[0].getComponent();

				AIFComponentContext[] rootActivity = activityLine.getRelated("me_cl_source");
				TCComponentMEActivity rootActivityComp = (TCComponentMEActivity) rootActivity[0].getComponent();
				String rootActivityVietDesc = rootActivityComp.getProperty("vf4_detail_step_desc");
				String operationVNDesc = operationRevision.getProperty("vf5_viet_description");
				if (rootActivityVietDesc != null && rootActivityVietDesc.length() > 1) {
					name = rootActivityVietDesc;
				} else if (operationVNDesc != null && operationVNDesc.length() > 1){
					name = operationVNDesc;
				}
			}else {
				name = operationLine.getProperty("bl_indented_title");
			}
			
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return name;
	}

}
