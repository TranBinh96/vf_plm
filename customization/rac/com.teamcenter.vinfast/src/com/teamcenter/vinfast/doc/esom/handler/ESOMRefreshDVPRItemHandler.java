package com.teamcenter.vinfast.doc.esom.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.utils.VFNotSupportException;

public class ESOMRefreshDVPRItemHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		//assume this is ESOM Revision
		//check to make sure one 
		InterfaceAIFComponent[] selectedObjects = AIFUtility.getCurrentApplication().getTargetComponents();
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		try {
			if (selectedObjects.length == 1) {
				
				TCComponent esomRev = (TCComponent)selectedObjects[0];
				
				//check access write
				TCAccessControlService acl = session.getTCAccessControlService();
				boolean hasWriteAccess = acl.checkPrivilege(esomRev, "WRITE");
				if(!hasWriteAccess) {
					MessageBox.post("You don't have access write on this ESOM item", "WARNING", MessageBox.WARNING);
					return null;
				}
				
				//get all dvpr item in relation
				AIFComponentContext[] dvprInContext = esomRev.getRelated("VF4_DVPItems");
				List<TCComponent> newDVPRTable = new LinkedList<TCComponent>();
				if(dvprInContext != null && dvprInContext.length > 0) {
					
					//get current dvpr information in table
					List<TCComponent> currDVPRTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_dvpr_info")));
					LinkedHashMap<String, TCComponent> dvprNum2DetailInfo = new LinkedHashMap<String, TCComponent>();
					for(int i = 0; i < currDVPRTable.size(); i++) {
						TCComponent dvprDetailInfo = currDVPRTable.get(i);
						TCComponent dvprItem = dvprDetailInfo.getReferenceProperty("vf3_dvpr_item");
						String dvprNum = dvprItem.getPropertyDisplayableValue("item_id");
						dvprNum2DetailInfo.put(dvprNum, dvprDetailInfo);
					}
					
					//create new dvpr table
					CreateIn[] createInputs = new CreateIn[dvprInContext.length];
					int counter = 0;
					for (AIFComponentContext aDVPRInContext : dvprInContext) {
						//loop each ecr to prepare input data
						TCComponent dvprRev = (TCComponent)aDVPRInContext.getComponent();
						String dvprNumber = dvprRev.getPropertyDisplayableValue("item_id");
						
						CreateIn createInput = new CreateIn();
						createInput.data.boName = "VF3_ESOM_DVPR";
						createInput.data.tagProps.put("vf3_dvpr_item", dvprRev);
						//copy data if is ecr existed in table
						if(dvprNum2DetailInfo.containsKey(dvprNumber)) {
							createInput.data.stringProps.put("vf3_risk_assessment", dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_risk_assessment").isEmpty() ? "" : dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_risk_assessment"));
							createInput.data.stringProps.put("vf3_comment", dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_comment").isEmpty() ? "" : dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_comment"));
							createInput.data.stringProps.put("vf3_target_met", dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_target_met").isEmpty() ? "" : dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_target_met"));
							if(!dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_percentage_completed").isEmpty()) {
								double perComp = Double.valueOf(dvprNum2DetailInfo.get(dvprNumber).getPropertyDisplayableValue("vf3_percentage_completed"));
								createInput.data.doubleProps.put("vf3_percentage_completed", perComp);
							}
						}
						createInputs[counter] = createInput;
						counter++;
					}
					
					//create table row
					DataManagementService dms = DataManagementService.getService(session);
					CreateResponse response = dms.createObjects(createInputs);
					if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
						newDVPRTable = getNewRows(response);
					}else {
						MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.ERROR);
					}
				}
				
				//add to esom
				esomRev.setRelated("vf3_dvpr_info", newDVPRTable.toArray(new TCComponent[0]));
				esomRev.refresh();
				if (esomRev.isCheckedOut() == false && esomRev.okToCheckout()) {
					CheckoutEditAction checkOutAction = new CheckoutEditAction();
					checkOutAction.run();
				}
				
				
			} else {
				throw new VFNotSupportException("This feature only supports single part selection!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.WARNING);
		}
		
		return null;
	}

	private List<TCComponent> getNewRows(CreateResponse response) {
		List<TCComponent> newRows = new LinkedList<TCComponent>();
		for (CreateOut output : response.output) {
			newRows.addAll(Arrays.asList(output.objects));
		}
		return newRows;
	}

}
