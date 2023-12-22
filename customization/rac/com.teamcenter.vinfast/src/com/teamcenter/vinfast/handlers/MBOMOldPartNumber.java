package com.teamcenter.vinfast.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.vinfast.handlers.MBOMOldPartSWTDialog.OldPartNumberPostAction;
import com.teamcenter.vinfast.utils.CostUtils;
import com.teamcenter.vinfast.utils.Utils;

public class MBOMOldPartNumber extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Shell shell = new Shell();
		List<MBOMOldPartSWTDialog.OldPartNumberPostAction> postActions = new LinkedList<MBOMOldPartSWTDialog.OldPartNumberPostAction>();
		postActions.add(new OldPartNumberPostAction() {
			@Override
			public void run(List<TCComponent> oldItems, TCComponent item) throws Exception {
				if (oldItems.size() > 1) {
					// more than one items ==> warning
					String attrName = "Traceability Indicator, Part Make Buy";
					throw new Exception("Cannot copy \"" + attrName + "\" to the new part as there are multiple old part numbers!");
				} else if (oldItems.size() == 1) {
					String traceable = oldItems.get(0).getStringProperty("vf4_item_is_traceable");
					item.setProperty("vf4_item_is_traceable", traceable);

					String isTransfer = item.getPropertyDisplayableValue("vf4_is_transferred_erp");
					if (isTransfer.compareToIgnoreCase(String.valueOf(Boolean.TRUE)) != 0) {
						// String partMakeBuy =
						// oldItems.get(0).getPropertyDisplayableValue("vf4_item_make_buy");
						// item.setProperty("vf4_item_make_buy", partMakeBuy);
					} else {
						throw new Exception("Cannot copy Part Make Buy from old part as new part is transferred to downstream system.");
					}
				}
			}

			@Override
			public String getName() {
				return "Copy Part Tracebility";
			}
		});

		postActions.add(new OldPartNumberPostAction() {
			@Override
			public void run(List<TCComponent> oldItems, TCComponent item) throws Exception {
				String partNumber = item.getProperty("item_id");
				if (oldItems.size() > 1) {
					// more than one items ==> warning
					throw new Exception("Cannot copy cost forms to the new part as there are multiple old part numbers!");
				} else if (oldItems.size() == 1) {
					try {
						TCComponentItemRevision oldPartRev = getPartRevFrom(oldItems.get(0));
						TCComponentItemRevision newPartRev = getPartRevFrom(item);

						TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();

						CostUtils oldCostUtil = new CostUtils(session, oldPartRev);
						CostUtils newCostUtil = new CostUtils(session, newPartRev);

						// TCComponent oldTargetCost =
						// oldCostUtil.getOrCreateOrSearchAndRelateTargetCost(false);
						TCComponentItemRevision oldCostRevTmp = oldCostUtil.getCostRevOrCreateOrSearchThenRelateToPartIfFound(false);
						StringBuffer errMsg = new StringBuffer();
						if (oldCostRevTmp != null) {
							newCostUtil.getOrCreateOrSearchAndRelateTargetCost(true);

							TCComponent newCostRev = newCostUtil.getOrSearchCostAndRemoveWrongCosts();
							TCComponent oldCostRev = oldCostUtil.getOrSearchCostAndRemoveWrongCosts();
							newCostRev.setRelated("IMAN_snapshot", new TCComponent[] { oldCostRev });
							item.setRelated("EC_reference_item_rel", oldItems.toArray(new TCComponent[0]));
							// trigger process with newTargetCost

							WorkflowService wfsrv = WorkflowService.getService(session);
							ContextData triggerProcessData = new ContextData();
							triggerProcessData.attachmentTypes = new int[] { 1 };
							triggerProcessData.attachments = new String[] { newPartRev.getUid() };
							triggerProcessData.processTemplate = "VF_IT_Trigger_Copy_Old_Part_Info";
							InstanceInfo copyingPartProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", triggerProcessData);
							if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
								System.out.println("[vf]" + item.getObjectString() + "OKAY");
							} else {
								String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
								errMsg.append("\nCannot proceed copying old part info of \"" + item.toString() + "\" as below error.\n" + soaErr);
							}

//							ContextData copyingPartProcessData = new ContextData();
//							copyingPartProcessData.attachmentTypes = new int [] { 1, 1 };
//							copyingPartProcessData.attachments = new String[] { newCostRev.getUid(), item.getUid() };
//							copyingPartProcessData.processTemplate = "VF_IT_Copy_Data_Old_Part_to_New_Part";
//							InstanceInfo copyingPartProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", copyingPartProcessData );
//							if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
//								System.out.println("[vf]" + item.getObjectString() + "OKAY");
//							}else {
//								String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
//								errMsg.append("\nCannot copy old part info of \"" + item.toString() + "\" as below error.\n" + soaErr);
//							}
//							
//							ContextData copyingRevProcessData = new ContextData();
//							copyingRevProcessData.attachmentTypes = new int [] { 1, 3 };
//							copyingRevProcessData.attachments = new String[] { newPartRev.getUid(), oldPartRev.getUid() };
//							copyingRevProcessData.processTemplate = "VF_IT_Copy_Data_Old_Part_Rev_to_New_Part_Rev";
//							InstanceInfo copyingRevProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", copyingRevProcessData );
//							if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
//								System.out.println("[vf]" + item.getObjectString() + "OKAY");
//							}else {
//								String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
//								errMsg.append("\nCannot copy old part revision info of \"" + item.toString() + "\" as below error.\n" + soaErr);
//							}

							if (!errMsg.toString().isEmpty()) {
								throw new Exception(errMsg.toString());
							}
						}
					} catch (Exception ex) {
						TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
						try {
							String tmpdir = System.getProperty("java.io.tmpdir");
							File tempParamFile = new File(tmpdir + "\\" + java.util.UUID.randomUUID() + ".txt");
							BufferedWriter writer = new BufferedWriter(new FileWriter(tempParamFile));

							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							ex.printStackTrace(pw);
							pw.flush();
							writer.write(ex.getMessage());
							writer.write(System.lineSeparator());
							writer.write(sw.toString());
							writer.write(System.lineSeparator());
							writer.close();
							Utils.createDataset(session, "VF_TRIGGER_COPY_OLD_PART_INFO_FAIL_" + partNumber, "Text", "DBA_Created\n", tempParamFile.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}

						ex.printStackTrace();
					}
				}
			}

			private TCComponentItemRevision getPartRevFrom(TCComponent item) throws TCException {
				TCComponentItemRevision[] workingRevs = ((TCComponentItem) item).getWorkingItemRevisions();
				TCComponentItemRevision latestRev = ((TCComponentItem) item).getLatestItemRevision();
				if (workingRevs.length > 0)
					return workingRevs[0];

				return latestRev;
			}

			@Override
			public String getName() {
				return "Copy Cost Forms";
			}
		});

		MBOMOldPartSWTDialog dialog = new MBOMOldPartSWTDialog(shell, SWT.SHELL_TRIM, postActions);
		centerToScreen(shell);
		dialog.open();
		return null;
	}

	public void centerToScreen(Shell shell) {
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
	}
}
