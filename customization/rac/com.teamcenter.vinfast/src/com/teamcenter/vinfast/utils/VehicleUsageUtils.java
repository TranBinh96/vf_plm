package com.teamcenter.vinfast.utils;

import org.apache.log4j.Logger;

import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.CreateOrUpdateFormsResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.FormInfo;

public class VehicleUsageUtils {

	private TCComponentItem part;
	private TCComponentItemRevision partRev;
	private boolean willCreateVehicleUsageForm;
	private static final Logger logger = Logger.getLogger(VehicleUsageUtils.class);
	private final TCSession session;
	
	
	public VehicleUsageUtils(TCComponentItem part, TCComponentItemRevision partRev, boolean willCreateVehicleUsageForm, TCSession session) {
		this.part = part;
		this.partRev = partRev;
		this.willCreateVehicleUsageForm = willCreateVehicleUsageForm;
		this.session = session;
	}

	public TCComponent getVehicleUsage() throws Exception {
		TCComponent vehicleForm = null;
		String formType;
		
		if (part.getType().equals("VF4_Design")) {
			formType = "VF4_VehicleUsageForm";
		} else {
			formType = "VF4_EScooterVehUsage";
		}
		
		String itemID = part.getProperty("item_id");
		TCComponent[] vehicleFormsCandiate = part.getRelatedComponents("VF4_VehicleUsageRelation");
		String validUsageFormName = generateVehicleUsageFormName(itemID);
		vehicleForm = getVehicleUsageFormAndRemoveIfInvalid(part, vehicleFormsCandiate, validUsageFormName);
		
		if (vehicleForm == null) {
			vehicleForm = searchOrCreateAndAttachVehicleUsageForm(part, partRev, vehicleForm, itemID, validUsageFormName, formType);
		}

		return vehicleForm;
	}
	
	private TCComponent getVehicleUsageFormAndRemoveIfInvalid(TCComponentItem item,
			TCComponent[] vehicleFormsCandiate, String validUsageFormName) throws TCException {
		TCComponent vehicleForm = null;
		for (TCComponent vehicleFormCandidate : vehicleFormsCandiate) {
			boolean isValidName = vehicleFormCandidate.getProperty("object_name").equals(validUsageFormName);
			if (isValidName) {
				vehicleForm = vehicleFormCandidate;
			} else {
				if (vehicleFormsCandiate.length == 1 && vehicleFormCandidate.getWhereReferencedCount() == 1) {
					vehicleForm = vehicleFormCandidate;
					TCAccessControlService acl = session.getTCAccessControlService();
					boolean useCanWriteForm = acl.checkPrivilege(vehicleForm, "WRITE");
					if (useCanWriteForm) {
						vehicleForm.setProperty("object_name", validUsageFormName);
						vehicleForm.save();
						vehicleForm.refresh();
					}
				} else {
					handleInvalidUsageFormAttached(item, vehicleFormCandidate);
				}
			}
		}
		
		return vehicleForm;
	}

	private TCComponent searchOrCreateAndAttachVehicleUsageForm(TCComponentItem part, TCComponentItemRevision partRev,
			TCComponent vehicleForm, String itemID, String usageFormName, String formType) throws Exception, TCException {
		String parameters[] = { "Name", "Type" };
		String values[] = { usageFormName, formType };
		TCComponent[] vehicleUsageForms = Utils.executeSavedQuery("General...", values, parameters);
		if (vehicleUsageForms.length == 1) {
			vehicleForm = vehicleUsageForms[0];
		} else if (vehicleUsageForms.length > 1) {
			vehicleForm = vehicleUsageForms[0];
//				throw new Exception("No usage form attached to part and found more than one \"" + usageFormName
//						+ "\" usage forms.");
		} else if (willCreateVehicleUsageForm == true) {
			boolean isGrantedToCreate = checkIsGrantedToCreate(partRev);
			if (isGrantedToCreate) {
				vehicleForm = createVehicleUsage(itemID, formType);					
			} else {
				throw new Exception("NO_ACCESS_RIGHT");
			}
		}
		
		if (vehicleForm != null) {
			part.setRelated("VF4_VehicleUsageRelation", new TCComponent[] { vehicleForm });
			part.refresh();
		}
		return vehicleForm;
	}
	
	private boolean checkIsGrantedToCreate(TCComponentItemRevision partRev) throws TCException {
		TCComponentGroup revOwningGroup = (TCComponentGroup) partRev.getReferenceProperty("owning_group");
		TCComponentGroup currentUserGroup = session.getGroup();
		boolean isGrantedToCreate = (currentUserGroup.toString().equals(revOwningGroup.toString()) == true);
		return isGrantedToCreate;
	}

	private void handleInvalidUsageFormAttached(TCComponentItem item, TCComponent wrongVehicleForm) throws TCException {
		logger.error("Wrong form attached " + wrongVehicleForm.toString() + " for part " + item.toString() + ". So, cut it!");
		item.remove("VF4_VehicleUsageRelation", wrongVehicleForm);
	}

	private TCComponent createVehicleUsage(String itemId, String formType) throws Exception {
		DataManagementService dms = DataManagementService.getService(session);
		FormInfo formInfo = new FormInfo();
		formInfo.formType = formType;
		formInfo.description = itemId;
		formInfo.name = generateVehicleUsageFormName(itemId);
		FormInfo[] formInfos = new FormInfo[] { formInfo };
		CreateOrUpdateFormsResponse results = dms.createOrUpdateForms(formInfos);
		if (results != null && results.outputs != null && results.outputs.length == 1) {
			TCComponent returnedForm = results.outputs[0].form;
			returnedForm.save();
			
			return returnedForm;
		} else {
			String errorMessage = Utils.getErrorMessagesFromSOA(results.serviceData);
			throw new Exception("Cannot create usage form!\n" + errorMessage);
		}
	}
	
	private String generateVehicleUsageFormName(String itemId) {
		return itemId + "_VehicleUsage";
	}
}
