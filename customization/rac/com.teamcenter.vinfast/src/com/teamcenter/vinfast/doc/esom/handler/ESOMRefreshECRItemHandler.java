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

public class ESOMRefreshECRItemHandler extends AbstractHandler {

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
				
				//get all ecr item in relation
				AIFComponentContext[] ecrInContexts = esomRev.getRelated("VF4_ECR_Items");
				List<TCComponent> newEcrTable = new LinkedList<TCComponent>();
				if(ecrInContexts != null && ecrInContexts.length > 0) {
					
					//get current ecr information in table
					List<TCComponent> currEcrTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_ecr_info")));
					LinkedHashMap<String, TCComponent> ecrNum2DetailInfo = new LinkedHashMap<String, TCComponent>();
					for(int i = 0; i < currEcrTable.size(); i++) {
						TCComponent ecrDetailInfo = currEcrTable.get(i);
						TCComponent ecrItem = ecrDetailInfo.getReferenceProperty("vf3_ecr_item");
						String ecrNum = ecrItem.getPropertyDisplayableValue("item_id");
						ecrNum2DetailInfo.put(ecrNum, ecrDetailInfo);
					}
					
					//create new ecr table
					CreateIn[] createInputs = new CreateIn[ecrInContexts.length];
					int counter = 0;
					for (AIFComponentContext aEcrInContext : ecrInContexts) {
						//loop each ecr to prepare input data
						TCComponent ecrRev = (TCComponent)aEcrInContext.getComponent();
						String ecrNumber = ecrRev.getPropertyDisplayableValue("item_id");
						
						CreateIn createInput = new CreateIn();
						createInput.data.boName = "VF3_ESOM_ECR";
						createInput.data.tagProps.put("vf3_ecr_item", ecrRev);
						//copy data if is ecr existed in table
						if(ecrNum2DetailInfo.containsKey(ecrNumber)) {
							createInput.data.stringProps.put("vf3_risk_assessment", ecrNum2DetailInfo.get(ecrNumber).getPropertyDisplayableValue("vf3_risk_assessment").isEmpty() ? "" : ecrNum2DetailInfo.get(ecrNumber).getPropertyDisplayableValue("vf3_risk_assessment"));
							createInput.data.stringProps.put("vf3_comment", ecrNum2DetailInfo.get(ecrNumber).getPropertyDisplayableValue("vf3_comment").isEmpty() ? "" : ecrNum2DetailInfo.get(ecrNumber).getPropertyDisplayableValue("vf3_comment"));
						}
						createInputs[counter] = createInput;
						counter++;
					}
					
					//create table row
					DataManagementService dms = DataManagementService.getService(session);
					CreateResponse response = dms.createObjects(createInputs);
					if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
						newEcrTable = getNewRows(response);
					}else {
						MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.ERROR);
					}
				}
				
				//add to esom
				esomRev.setRelated("vf3_ecr_info", newEcrTable.toArray(new TCComponent[0]));
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
			MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.ERROR);
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
