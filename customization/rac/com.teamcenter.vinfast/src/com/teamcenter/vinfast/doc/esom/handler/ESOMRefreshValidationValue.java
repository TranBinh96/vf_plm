package com.teamcenter.vinfast.doc.esom.handler;

import java.math.BigInteger;
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

public class ESOMRefreshValidationValue extends AbstractHandler {
	
	public class ValidationData{
		public String validationType;
		public int meetTarget;
		public int minorAndInsign;
		public int moderate;
		public int criticalAndMajor;
	}

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
				
				List<TCComponent> lstTblEcr = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_ecr_info")));
				List<TCComponent> lstTblDvpr = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_dvpr_info")));
				List<TCComponent> lstTblSils = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_sil_info")));
				List<TCComponent> lstTblValidation = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_validation_info")));
				List<ValidationData> valiData = new LinkedList<ESOMRefreshValidationValue.ValidationData>();
				//remove all current row
				lstTblValidation.clear();
				
				//recalculate ecr info
				ValidationData ecrInfo = new ValidationData();
				ecrInfo.validationType = "Open ECRs";
				for(int i = 0; i < lstTblEcr.size(); i++) {
					TCComponent ecrRow = lstTblEcr.get(i);
					String riskAssess = ecrRow.getProperty("vf3_risk_assessment");
					switch (riskAssess) {
					case "MEET TARGETS":
						ecrInfo.meetTarget += 1;
						break;
					case "INSIGNIFICANT":
						ecrInfo.minorAndInsign += 1;
						break;
					case "MINOR":
						ecrInfo.minorAndInsign += 1;
						break;
					case "MODERATE":
						ecrInfo.moderate += 1;
						break;
					case "MAJOR":
						ecrInfo.criticalAndMajor += 1;
						break;
					case "CRITICAL":
						ecrInfo.criticalAndMajor += 1;
						break;
					default:
						break;
					}
				}
				
				//recalculate sil info
				ValidationData silInfo = new ValidationData();
				silInfo.validationType = "Open SILs";
				for(int i = 0; i < lstTblSils.size(); i++) {
					TCComponent silRow = lstTblSils.get(i);
					String riskAssess = silRow.getProperty("vf3_risk_assessment");
					switch (riskAssess) {
					case "MEET TARGETS":
						silInfo.meetTarget += 1;
						break;
					case "INSIGNIFICANT":
						silInfo.minorAndInsign += 1;
						break;
					case "MINOR":
						silInfo.minorAndInsign += 1;
						break;
					case "MODERATE":
						silInfo.moderate += 1;
						break;
					case "MAJOR":
						silInfo.criticalAndMajor += 1;
						break;
					case "CRITICAL":
						silInfo.criticalAndMajor += 1;
						break;
					default:
						break;
					}
				}
				
				//recalculate dvpr info
				ValidationData dvprInfo = new ValidationData();
				dvprInfo.validationType = "Validation Completed";
				for(int i = 0; i < lstTblDvpr.size(); i++) {
					TCComponent dvprRow = lstTblDvpr.get(i);
					String riskAssess = dvprRow.getProperty("vf3_risk_assessment");
					switch (riskAssess) {
					case "MEET TARGETS":
						dvprInfo.meetTarget += 1;
						break;
					case "INSIGNIFICANT":
						dvprInfo.minorAndInsign += 1;
						break;
					case "MINOR":
						dvprInfo.minorAndInsign += 1;
						break;
					case "MODERATE":
						dvprInfo.moderate += 1;
						break;
					case "MAJOR":
						dvprInfo.criticalAndMajor += 1;
						break;
					case "CRITICAL":
						dvprInfo.criticalAndMajor += 1;
						break;
					default:
						break;
					}
				}
				valiData.add(dvprInfo);
				valiData.add(ecrInfo);
				valiData.add(silInfo);
				//for each of ECR item create table row
				List<TCComponent> newRows = new LinkedList<TCComponent>();
				if (valiData.size() > 0) {
					CreateIn[] createInputs = new CreateIn[valiData.size()];
					for (int i = 0; i < valiData.size(); i++) {
						CreateIn createInput = new CreateIn();
						createInput.data.boName = "VF3_ESOM_Validation";
						createInput.data.stringProps.put("vf3_validation_type", valiData.get(i).validationType);
						createInput.data.intProps.put("vf3_meet_targets", BigInteger.valueOf(valiData.get(i).meetTarget));
						createInput.data.intProps.put("vf3_critical_major", BigInteger.valueOf(valiData.get(i).criticalAndMajor));
						createInput.data.intProps.put("vf3_minor_insignificant", BigInteger.valueOf(valiData.get(i).minorAndInsign)); 
						createInput.data.intProps.put("vf3_moderate", BigInteger.valueOf(valiData.get(i).moderate)); 
						createInputs[i] = createInput;
					}
					DataManagementService dms = DataManagementService.getService(session);
					CreateResponse response = dms.createObjects(createInputs);
					if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
						newRows = getNewRows(response);
					}
				}
				//add to esom
				if (newRows.size() > 0) {
					lstTblValidation.addAll(newRows);
				}
				esomRev.setRelated("vf3_validation_info", lstTblValidation.toArray(new TCComponent[0]));
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
