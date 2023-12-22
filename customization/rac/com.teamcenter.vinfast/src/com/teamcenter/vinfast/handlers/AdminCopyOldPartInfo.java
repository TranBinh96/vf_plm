package com.teamcenter.vinfast.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventResponse;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.vinfast.handlers.MBOMOldPartSWTDialog.OldPartNumberPostAction;
import com.teamcenter.vinfast.utils.CostUtils;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.TCExtension;

public class AdminCopyOldPartInfo extends AbstractHandler  {

	private TCSession session = null;
	private TCComponent rule = null;
	private static String[] GROUP_PERMISSION = { "dba" };
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
			MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}
		InterfaceAIFComponent[] iaifcomps = AIFUtility.getCurrentApplication().getTargetComponents();
		
		StringBuffer sb = new StringBuffer();
		for (InterfaceAIFComponent aifcomp : iaifcomps) {
			TCComponent comp = (TCComponent) aifcomp;
			String failPartID = "";
			try {
				failPartID = comp.getProperty("object_string");
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			PostEventObjectProperties[] postEvtInputs = new PostEventObjectProperties[1];
			postEvtInputs[0] = new PostEventObjectProperties();
			postEvtInputs[0].primaryObject = comp;
			
			PostEventResponse response = null;
			try {
				response = DataManagementService.getService(session).postEvent(postEvtInputs,
						"Fnd0MultiSite_Unpublish");
			} catch (ServiceException e1) {
				sb.append(failPartID).append(";");
				e1.printStackTrace();
			}
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				sb.append(failPartID).append(";");
			} 
		}
		
		System.out.println(sb.toString());
////		StringBuffer err = new StringBuffer();
////		StringBuffer copyItems = new StringBuffer();
////		StringBuffer notCopyItems = new StringBuffer();
////		try {
////			rule = session.getComponentManager().getTCComponent("xENAw$Eo47MsRA");
////			for (InterfaceAIFComponent comp : comps) {
////				TCComponentItem item = (TCComponentItem)comp;
////				String oldPartNumber;
////				
////				oldPartNumber = item.getProperty("vf4_orginal_part_number");
////				
////				if (oldPartNumber != null && oldPartNumber.trim().isEmpty() == false && (!oldPartNumber.contains(";") && !oldPartNumber.contains(","))) {
////					try {
////						copyOldPartInfo(oldPartNumber.trim(), item, copyItems, notCopyItems, session);
////					} catch (Exception e) {
////						err.append("Ex when copying old part info:\n");
////						err.append(e.getMessage());
////						err.append("\n");
////					}
////				} else {
////					notCopyItems.append(item.toString());
////					notCopyItems.append("\n");
////				}
////			}
////		} catch (TCException e) {
////			err.append("Ex when getting old part number:\n");
////			err.append(e.getMessage());
////			err.append("\n");
////		}
////		
////		if (err.toString().isEmpty() == false) {
////			MessageBox.post(err.toString(), "Error", MessageBox.ERROR);
////		} else {
////			SwingUtilities.invokeLater(new Runnable() {
////				
////				@Override
////				public void run() {
////					StringViewerDialog result = new StringViewerDialog(new String[] {"Coppied successfully below parts.\n" + copyItems.toString() + "\n\nNot coppied below parts:\n" + notCopyItems.toString()});
////					result.setVisible(true);
////				}
////			});
//			
//		}
//		
		// loop all targets
		// if instance of design do
		//    get oldPartNUmber
		//    if oldPartNumber more than one or empty then next
		//
		//      call function copy info from oldpartnumber 		
		//    end if
		// end if
		
		return null;
	}

	public void copyOldPartInfo(String oldItemNumber, TCComponent item, StringBuffer copyItems, StringBuffer notCopyItems, TCSession session) throws Exception {
		try {
			TCComponent[] oldItems = searchOldItem(session, oldItemNumber);
			if (oldItems.length > 1 || oldItems.length == 0) {
				notCopyItems.append(item.toString() + "\n");
			} else {
				
				List<MBOMOldPartSWTDialog.OldPartNumberPostAction> postActions = preparePostActions();
				
				String saveValue = oldItemNumber;
				System.out.println("Remove Clicked");
				List<TCComponent> oldItemsList = new LinkedList<TCComponent>();
				oldItemsList.add((TCComponent) oldItems[0]);

				StringBuffer allErrorMsgs = new StringBuffer();
				String currentOriginalPN = null;
				try {
					currentOriginalPN = item.getProperty("vf4_orginal_part_number");
					item.setProperty("vf4_hw_version", "true");
					item.setProperty("vf4_orginal_part_number", saveValue);
					item.setProperty("vf4_hw_version", "");
					item.refresh();
					currentOriginalPN = item.getProperty("vf4_orginal_part_number");
					for (OldPartNumberPostAction postAction : postActions) {
						try {
							postAction.run(oldItemsList, item);
						} catch (Exception e) {
							allErrorMsgs.append(e.getMessage());
							allErrorMsgs.append("\n");
						}
					}
				} catch (Exception e) {
					allErrorMsgs.append(e.getMessage());
					allErrorMsgs.append("\n");
				}

				boolean updateOriginalPNValueSuccessfully = currentOriginalPN.equals(saveValue);

				
				if (updateOriginalPNValueSuccessfully) {
					copyItems.append(item.toString());
					copyItems.append("\n");
				} else {
					notCopyItems.append(item.toString() + "\n");
				}
					
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<MBOMOldPartSWTDialog.OldPartNumberPostAction> preparePostActions() {
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
						//String partMakeBuy = oldItems.get(0).getPropertyDisplayableValue("vf4_item_make_buy");
						//item.setProperty("vf4_item_make_buy", partMakeBuy);
					}
					else {
						throw new Exception("Cannot copy Part Make Buy from old part as new part is trasnferred to downstream system.");
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
						
						//TCComponent oldTargetCost = oldCostUtil.getOrCreateOrSearchAndRelateTargetCost(false);
						TCComponentItemRevision oldCostRevTmp = oldCostUtil.getCostRevOrCreateOrSearchThenRelateToPartIfFound(false);
						StringBuffer errMsg = new StringBuffer();
						if (oldCostRevTmp != null) {
							newCostUtil.getOrCreateOrSearchAndRelateTargetCost(true);
							
							TCComponent newCostRev = newCostUtil.getOrSearchCostAndRemoveWrongCosts();
							TCComponent oldCostRev = oldCostUtil.getOrSearchCostAndRemoveWrongCosts();
							newCostRev.setRelated("IMAN_snapshot", new TCComponent[] { oldCostRev });
							item.setRelated("EC_reference_item_rel", oldItems.toArray(new TCComponent[0]));
							//trigger process with newTargetCost 
							
							WorkflowService wfsrv = WorkflowService.getService(session);
							ContextData triggerProcessData = new ContextData();
							triggerProcessData.attachmentTypes = new int [] { 1 };
							triggerProcessData.attachments = new String[] { newPartRev.getUid()};
							triggerProcessData.processTemplate = "VF_IT_Trigger_Copy_Old_Part_Info";
							InstanceInfo copyingPartProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", triggerProcessData );
							if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
								System.out.println("[vf]" + item.getObjectString() + "OKAY");
							}else {
								String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
								errMsg.append("\nCannot proceed copying old part info of \"" + item.toString() + "\" as below error.\n" + soaErr);
							}
							
//									ContextData copyingPartProcessData = new ContextData();
//									copyingPartProcessData.attachmentTypes = new int [] { 1, 1 };
//									copyingPartProcessData.attachments = new String[] { newCostRev.getUid(), item.getUid() };
//									copyingPartProcessData.processTemplate = "VF_IT_Copy_Data_Old_Part_to_New_Part";
//									InstanceInfo copyingPartProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", copyingPartProcessData );
//									if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
//										System.out.println("[vf]" + item.getObjectString() + "OKAY");
//									}else {
//										String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
//										errMsg.append("\nCannot copy old part info of \"" + item.toString() + "\" as below error.\n" + soaErr);
//									}
//									
//									ContextData copyingRevProcessData = new ContextData();
//									copyingRevProcessData.attachmentTypes = new int [] { 1, 3 };
//									copyingRevProcessData.attachments = new String[] { newPartRev.getUid(), oldPartRev.getUid() };
//									copyingRevProcessData.processTemplate = "VF_IT_Copy_Data_Old_Part_Rev_to_New_Part_Rev";
//									InstanceInfo copyingRevProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call", copyingRevProcessData );
//									if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0 && copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
//										System.out.println("[vf]" + item.getObjectString() + "OKAY");
//									}else {
//										String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
//										errMsg.append("\nCannot copy old part revision info of \"" + item.toString() + "\" as below error.\n" + soaErr);
//									}
							
							if (!errMsg.toString().isEmpty()) {
								throw new Exception(errMsg.toString()); 
							}
						}
						
					} catch (Exception ex) {
						TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession() ;
						try {
							String tmpdir = System.getProperty("java.io.tmpdir");
							File tempParamFile = new File( tmpdir +"\\"+ java.util.UUID.randomUUID() + ".txt");
							BufferedWriter writer = new BufferedWriter(new FileWriter(tempParamFile));
							writer.write(ex.getMessage());
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
				TCComponentItemRevision[] workingRevs = ((TCComponentItem)item).getWorkingItemRevisions();
				TCComponentItemRevision latestRev = ((TCComponentItem)item).getLatestItemRevision();
				if (workingRevs.length > 0)
					return workingRevs[0];
				
				return latestRev;
			}

			@Override
			public String getName() {
				return "Copy Cost Forms";
			}
		});
		return postActions;
	}
	
	private TCComponent[] searchOldItem(TCSession session, String oldPartNumber) {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		String queryName = "Item...";
		inputQuery.put("Type", "VF4_*Design");
		inputQuery.put("Item ID", oldPartNumber);
		TCComponent[] result = com.vf.utils.Query.queryItem(session, inputQuery, queryName );
		return result;
	}

	private TCComponentItemRevision getPartRevFrom(TCComponent item) throws TCException {
		
//		TCComponentItemRevision[] workingRevs = ((TCComponentItem)item).getWorkingItemRevisions();
//		TCComponentItemRevision latestRev = ((TCComponentItem)item).getLatestItemRevision();
		
		TCComponentItemRevision latestRev = ((TCComponentItem)item).getConfiguredItemRevision(rule);
//		if (workingRevs.length > 0)
//			return workingRevs[0];
		
		return latestRev;
	}
}
