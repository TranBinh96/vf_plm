package com.vinfast.sap.bomtracking;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vinfast.car.sap.superbom.BomTrackingModel;
import com.vinfast.car.sap.superbom.BomTrackingRawDataHandler;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class BomTrackingImport extends AbstractHandler{
	UIGetValuesUtility util = null;
	BomTrackingImportDialog importDlg;
	TCSession clientSession;
	BomTrackingRawDataHandler handler = new BomTrackingRawDataHandler();
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentItemRevision changeObject = (TCComponentItemRevision)selectedObjects[0];
			clientSession = changeObject.getSession();
			util = new UIGetValuesUtility();
			TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(clientSession);
			
			if(UIGetValuesUtility.checkAccess(clientSession, groupMember, changeObject) == true){
				
				importDlg = new BomTrackingImportDialog(new Shell());
				importDlg.create();
				importDlg.setTitle("BOM Tracking Import");
				importDlg.setSession(clientSession);
				Button btnTranfer = importDlg.getSaveButton();
				btnTranfer.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						btnTranfer.setEnabled(false);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								importToMCN(changeObject);
								importDlg.getShell().dispose();
							}
						});
					}
				});
				
				Button btnMerge = importDlg.getBtnMerge();
				btnMerge.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								ArrayList<BomTrackingModel> list = handler.merge(importDlg.getSetSelectedLine());
								importDlg.setTableItem(list);
							}
						});
					}
				});
				
				
				ArrayList<BomTrackingModel> list = handler.getBomTrackingData(loadRawTrackingData(changeObject));
				importDlg.setTableItem(list);
				importDlg.open();
			}
			else {
				MessageBox.post("You are not authorized to transfer MCN.","Please check group/role and try again.", "Access...", MessageBox.ERROR);
				return null;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public File loadRawTrackingData(TCComponentItemRevision mcn) {
		String MCN_SAPID;
		File latestTrackingFile = null;
		try {
			MCN_SAPID = mcn.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
			String dataset_name = String.format("%s_%s", MCN, "BOM_TRACKING.txt");
			TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(clientSession, new String[]{"Name","Dataset Type"}, new String[]{dataset_name,"Text"}, "Dataset...");

			if(oldDatasets != null) {
				HashMap<Date, TCComponent> datasetMap =  new HashMap<Date, TCComponent>();
				DataManagementService dmCoreService =  DataManagementService.getService(clientSession);
				dmCoreService.getProperties(oldDatasets, new String[] {"creation_date"});
				for(TCComponent dataset : oldDatasets){
					Date date_creation = dataset.getDateProperty("creation_date");
					datasetMap.put(date_creation, dataset);
				}
				ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet()); 
				Collections.sort(sortedKeys, Collections.reverseOrder()); 
				TCComponentDataset olddataset = (TCComponentDataset)datasetMap.get(sortedKeys.get(0));
				latestTrackingFile = UIGetValuesUtility.downloadDataset(clientSession,System.getProperty("java.io.tmpdir"), olddataset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return latestTrackingFile;
	}
	
	public void importToMCN(TCComponentItemRevision changeObject) {
		ArrayList<BomTrackingModel> list = handler.getRawModels();
		try {
			changeObject.getReferenceListProperty("vf6_detail_changes");
			CreateIn[] createInputs = new CreateIn[list.size()];
			for (int i = 0; i < list.size(); i++) {
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "Vf6_detail_changes";
				createInput.data.stringProps.put("vf6_action", list.get(i).getActionType());
				createInput.data.stringProps.put("vf6_main_group_str", list.get(i).getMainGroup());
				createInput.data.stringProps.put("vf6_sub_group_str", list.get(i).getSubGroup());
				createInput.data.stringProps.put("vf6_bomline_id", list.get(i).getBomlineID());
				createInput.data.stringProps.put("vf6_part_number", list.get(i).getPartNumber());
				createInput.data.doubleProps.put("vf6_quantity", Double.parseDouble(list.get(i).getQuantity()));
				createInput.data.stringProps.put("vf6_replaced_part_number", list.get(i).getReplacedPartNumber());
				createInput.data.stringProps.put("vf6_new_part_number", list.get(i).getNewPartNumber());
				createInputs[i] = createInput;
			}
			DataManagementService dms = DataManagementService.getService(clientSession);
			CreateResponse response = dms.createObjects(createInputs);
			
			//add to mcr
			if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
				List<TCComponent> allRows = new LinkedList<TCComponent>();
				List<TCComponent> newRows = getNewRows(response);
				allRows.addAll(newRows);
				changeObject.setRelated("vf6_detail_changes", allRows.toArray(new TCComponent[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private List<TCComponent> getNewRows(CreateResponse response) {
		List<TCComponent> newRows = new LinkedList<TCComponent>();
		for (CreateOut output : response.output) {
			newRows.addAll(Arrays.asList(output.objects));
		}
		return newRows;
	}

}
