package com.teamcenter.vinfast.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.DeepCopyInfo;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.DeepCopyData;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2006_03.SavedQuery;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.SavedQueryObject;
import com.teamcenter.soa.client.model.ErrorStack;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

public class CostUtils {
	public final String VINF_COST_AVAILABLE_OBJECT_TYPES = "VINF_COST_AVAILABLE_OBJECT_TYPES";
	public final String[] VF4_DESIGN_REVISION;
	public final String VF4_SOURCING_COST_FORM_RELA = "VF4_SourcingCostFormRela";
	public final String VF4_SAP_COST_FORM_RELA = "VF4_SAPCostRelation";
	public final String VF4_COST = "VF4_Cost";
	public final String VF4_COSTING_REFERENCE = "VF4_Costing_Reference2";

	private final TCSession session;
	private DataManagementService dms = null;
	private static final Logger logger = Logger.getLogger(CostUtils.class);
	private final TCComponentItemRevision partRev;
	private final String partId;
	private final TCComponentItem part;
	private static TCComponentQuery tcComponentQuery = null;
	private TCComponentGroup partRevOwningGroup = null;
	private TCComponentUser partRevOwningUser = null;

	public CostUtils(TCSession session, TCComponentItemRevision partRev) throws TCException {
		this.session = session;
		this.partRev = partRev;
		this.dms = DataManagementService.getService(session);
		this.partId = partRev.getProperty("item_id");
		this.part = (TCComponentItem) partRev.getRelatedComponent("items_tag");
		VF4_DESIGN_REVISION = TCExtension.GetPreferenceValues(VINF_COST_AVAILABLE_OBJECT_TYPES, session);
		partRevOwningGroup = (TCComponentGroup) partRev.getReferenceProperty("owning_group");
		partRevOwningUser = (TCComponentUser) partRev.getReferenceProperty("owning_user");

		SavedQueryService savedQueryService = SavedQueryService.getService(session);
		if (tcComponentQuery == null) {
			try {
				SavedQuery.GetSavedQueriesResponse getSavedQueriesResponse = savedQueryService.getSavedQueries();
				for (SavedQueryObject queryObject : getSavedQueriesResponse.queries) {
					if (queryObject.name.equalsIgnoreCase("Working Item Revision...")) {
						tcComponentQuery = queryObject.query;
						break;
					}
				}
			} catch (ServiceException serviceException) {
				logger.error(serviceException);
				serviceException.printStackTrace();
			}
		}
	}

	public TCComponentItemRevision searchCost() {
		TCComponentItemRevision cost = null;
		try {
			if (tcComponentQuery != null) {
				String parameter[] = { "Item ID", "Type" };
				String value[] = { partId, "VF4_CostRevision" };
				TCComponent[] costRevisions = tcComponentQuery.execute(parameter, value);
				if (costRevisions.length == 1) {
					cost = (TCComponentItemRevision) costRevisions[0];
				} else if (costRevisions.length > 1) {
					logger.error("Searching part ID \"" + partId + "\" should return 1 object but more!");
				} else {
					logger.error("Warning: Cannot found cost with ID \"" + partId + "\"!");
				}
			}
		} catch (TCException tcException) {
			logger.error(tcException);
			tcException.printStackTrace();
		}
		return cost;
	}

	public LinkedList<TCComponentForm> searchForm(String name, String type) {
		LinkedList<TCComponentForm> costForm = new LinkedList<TCComponentForm>();
		try {
			LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
			parameter.put("Name", name);
			parameter.put("Type", type);

			TCComponent[] item_list = Query.queryItem(session, parameter, "General...");
			if (item_list != null && item_list.length > 0) {
				for (TCComponent form : item_list) {
					if (form instanceof TCComponentForm) {
						costForm.add((TCComponentForm) form);
					}
				}
			}
		} catch (Exception tcException) {
			logger.error(tcException);
			tcException.printStackTrace();
		}
		return costForm;
	}

	public TCComponent getOrCreateOrSearchAndRelateTargetCost(boolean willCreateIfNotFound) throws TCException, CannotCreateCostException {
		TCComponentItemRevision costRev = getCostRevOrCreateOrSearchThenRelateToPartIfFound(willCreateIfNotFound);

		TCComponentForm targetCost = null;
		if (costRev != null) {
			if (checkCostForms(costRev)) {
				AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
				if (costFormAifs.length == 2) {
					if (costFormAifs[0].getComponent().getType().equals(PropertyDefines.ACTUAL_TARGET_COST_FORM)) {
						targetCost = (TCComponentForm) costFormAifs[0].getComponent();
					} else {
						targetCost = (TCComponentForm) costFormAifs[1].getComponent();
					}
					changeOwner(targetCost);
				}
			} else {
				logger.error("Cost forms are not valid.");
			}
		} else {
			logger.error("Cannot get cost revision.");
		}

		return targetCost;
	}

	private boolean checkCostForms(TCComponentItemRevision costRev) throws TCException {
		AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
		String validIdStr = costRev.getProperty("item_id") + "_";
		boolean validTargetCostAttached = false;
		boolean validPieceCostAttached = false;
		if (costFormAifs != null && costFormAifs.length == 2) {
			for (AIFComponentContext costFormAif : costFormAifs) {
				TCComponent costForm = (TCComponent) costFormAif.getComponent();
				String costFormName = costForm.getProperty("object_name");
				if (costForm.getType().equals(PropertyDefines.ACTUAL_TARGET_COST_FORM) && costFormName.startsWith(validIdStr)) {
					// do no thing
					validTargetCostAttached = true;
				} else if (costForm.getType().equals(PropertyDefines.ACTUAL_PIECE_COST_FORM) && costFormName.startsWith(validIdStr)) {
					// do no thing
					validPieceCostAttached = true;
				}
			}
		}

		return (validTargetCostAttached && validPieceCostAttached);
	}

	public TCComponentItemRevision getCostRevOrCreateOrSearchThenRelateToPartIfFound(boolean willCreateIfNotFound) throws TCException, CannotCreateCostException {
		TCComponentItemRevision costRev = null;

		costRev = getOrSearchCostAndRemoveWrongCosts();
		if (costRev == null) {
			TCComponentGroup partRevOwningGroup = (TCComponentGroup) partRev.getReferenceProperty("owning_group");
			TCComponentGroup currentUserGroup = session.getGroup();
			if (willCreateIfNotFound) {
				String partOwningGroupStr = partRevOwningGroup.toString();
				String program = Utils.getProgram(partOwningGroupStr, session);
				String currentGroupStr = currentUserGroup.toString();
				String currentRoleStr = session.getRole().toString();
				if (currentGroupStr.equals(partOwningGroupStr) == true) {
					costRev = createVFCost();
					if (costRev == null) {
						throw new CannotCreateCostException("Cannot create Cost. Please contact your administrator.");
					}
				} else {
					costRev = handleCreateCostForSupplierParkCase();
				}
			}
		}

		if (costRev != null) {
			try {
				AIFComponentContext[] candidatesAIF = partRev.getRelated(VF4_COSTING_REFERENCE);
				if (candidatesAIF.length != 1) {
					// only set cost to the latest working cost when there is no cost or more than
					// one cost attached.
					setCost(costRev);
				}
			} catch (Exception ex) {
				logger.error("Cannot relate cost " + costRev.getProperty("object_string") + " to part.");
				logger.error(ex);
				ex.printStackTrace();
			}
			TCComponentType.refreshThese(new TCComponent[] { costRev });

			StringBuffer errorMsg = new StringBuffer();
//			AIFComponentContext[] sapFormAifs = costRev.getRelated(VF4_SAP_COST_FORM_RELA);
//			if (sapFormAifs.length == 0) {
//				LinkedList<TCComponentForm> sapFormList = searchForm(partId + PropertyDefines.SAP_COST_FORM_SUFFIX_NAME, PropertyDefines.SAP_COST_FORM);
//				if (sapFormList.size() == 1) {
//					changeOwner(sapFormList.get(0));
//				}
//			} else if (sapFormAifs.length == 1) {
//				if (sapFormAifs[0].getComponent().getType().equals(PropertyDefines.SAP_COST_FORM)) {
//					changeOwner((TCComponent) sapFormAifs[0].getComponent());
//				}
//			}

			AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
			if (costFormAifs.length == 0) {
				// piece cost form
				LinkedList<TCComponentForm> pieceFormList = searchForm(partId + PropertyDefines.ACTUAL_PIECE_COST_FORM_SUFFIX_NAME, PropertyDefines.ACTUAL_PIECE_COST_FORM);
				if (pieceFormList.size() == 0) {
					TCComponent pieceForm = createCostForm(partId + PropertyDefines.ACTUAL_PIECE_COST_FORM_SUFFIX_NAME, PropertyDefines.ACTUAL_PIECE_COST_FORM);
					if (pieceForm != null)
						costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { pieceForm });
				} else if (pieceFormList.size() == 1) {
					changeOwner(pieceFormList.get(0));
					costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { pieceFormList.get(0) });
				} else {
					errorMsg.append("There is a wrong number of piece cost forms \"" + partId + PropertyDefines.ACTUAL_PIECE_COST_FORM_SUFFIX_NAME + "\" instead of 1 form.");
				}
				// target cost form
				LinkedList<TCComponentForm> targetFormList = searchForm(partId + PropertyDefines.ACTUAL_TARGET_COST_FORM_SUFFIX_NAME, PropertyDefines.ACTUAL_TARGET_COST_FORM);
				if (targetFormList.size() == 0) {
					TCComponent targetForm = createCostForm(partId + PropertyDefines.ACTUAL_TARGET_COST_FORM_SUFFIX_NAME, PropertyDefines.ACTUAL_TARGET_COST_FORM);
					if (targetForm != null)
						costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { targetForm });
				} else if (pieceFormList.size() == 1) {
					changeOwner(targetFormList.get(0));
					costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { targetFormList.get(0) });
				} else {
					errorMsg.append("There is a wrong number of target cost forms \"" + partId + PropertyDefines.ACTUAL_TARGET_COST_FORM_SUFFIX_NAME + "\" instead of 1 form.");
				}
			} else if (costFormAifs.length > 2) {
				errorMsg.append("There is a wrong number of cost forms attached to cost \"" + costRev.toString() + "\" instead of 2 forms.");
			} else if (costFormAifs.length == 1) {
				String suffixName = "";
				String costFormType = "";
				if (costFormAifs[0].getComponent().getType().equals(PropertyDefines.ACTUAL_TARGET_COST_FORM)) {
					suffixName = PropertyDefines.ACTUAL_PIECE_COST_FORM_SUFFIX_NAME;
					costFormType = PropertyDefines.ACTUAL_PIECE_COST_FORM;
				} else {
					suffixName = PropertyDefines.ACTUAL_TARGET_COST_FORM_SUFFIX_NAME;
					costFormType = PropertyDefines.ACTUAL_TARGET_COST_FORM;
				}

				LinkedList<TCComponentForm> formList = searchForm(partId + suffixName, costFormType);
				if (formList.size() == 0) {
					TCComponent form = createCostForm(partId + suffixName, costFormType);
					if (form != null)
						costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { form });
				} else if (formList.size() == 1) {
					changeOwner(formList.get(0));
					costRev.add(VF4_SOURCING_COST_FORM_RELA, new TCComponent[] { formList.get(0) });
				} else {
					errorMsg.append("There is a wrong number of forms \"" + partId + suffixName + "\" instead of 1 form.");
				}
			}

			if (errorMsg.toString().trim().isEmpty() == false) {
				errorMsg.insert(0, "Some abnormal conditions found when opening cost.\nPlease contact your IT Helpdesk with below message.\n");
				throw new CannotCreateCostException(errorMsg.toString());
			}
		}

		return costRev;
	}

	private void changeOwner(TCComponent item) {
		try {
			TCComponentGroup itemOwningGroup = (TCComponentGroup) item.getReferenceProperty("owning_group");
			TCComponentUser itemOwningUser = (TCComponentUser) item.getReferenceProperty("owning_user");
			if (itemOwningGroup != partRevOwningGroup || itemOwningUser != partRevOwningUser) {
				item.changeOwner(partRevOwningUser, partRevOwningGroup);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TCComponentItemRevision handleCreateCostForSupplierParkCase() throws CannotCreateCostException, TCException {
		TCComponentItemRevision costRev;
		costRev = createVFCost();
		if (costRev == null) {
			throw new CannotCreateCostException("Cannot create Cost. Please contact your administrator.");
		}
		AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
		for (AIFComponentContext costFormAif : costFormAifs) {
			changeOwner((TCComponent) costFormAif.getComponent());
		}
		changeOwner(costRev);
		changeOwner(costRev.getItem());
		return costRev;
	}

	private void setCost(TCComponentItemRevision costRev) throws TCException {
		partRev.setRelated(VF4_COSTING_REFERENCE, new TCComponent[] { costRev });
	}

	public TCComponentItemRevision getOrSearchCostAndRemoveWrongCosts() throws TCException {
		TCComponentItemRevision costRev = null;
		AIFComponentContext[] candidatesAIF = partRev.getRelated(VF4_COSTING_REFERENCE);

		for (AIFComponentContext candidateAIF : candidatesAIF) {
			TCComponentItemRevision candidate = (TCComponentItemRevision) candidateAIF.getComponent();
			String costId = candidate.getItem().getProperty("item_id");
			if (partId.equals(costId)) {
				// always get latest working cost revision
				// costRev = candidate;
			} else {
				handleWrongCostAttached(candidate);
			}
		}

		if (costRev == null) {
			costRev = searchCost();
			if (costRev != null)
				setCost(costRev);
		}

		return costRev;
	}

	private void handleWrongCostAttached(TCComponentItemRevision wrongCostRev) throws TCException {
		logger.error("Wrong cost attached " + wrongCostRev.toString() + ". It should be " + partId + ". So, cut it!");
		partRev.remove(VF4_COSTING_REFERENCE, wrongCostRev);
	}

	public void searchCostRevAndRelateToPart() throws TCException {
		TCComponentItemRevision costRev = searchCost();
		if (costRev != null) {
			try {
				setCost(costRev);
			} catch (Exception ex) {
				logger.error("Cannot relate cost " + costRev.getProperty("object_string") + " to part.");
				logger.error(ex);
				ex.printStackTrace();
			}
		}
	}

	public TCComponent getOrCreateOrSearchAndRelatePieceCost(boolean willCreateIfNotFound) throws TCException, CannotCreateCostException {
		TCComponentItemRevision costRev = getCostRevOrCreateOrSearchThenRelateToPartIfFound(willCreateIfNotFound);
		TCComponentForm pieceCost = null;
		if (costRev != null) {
			if (checkCostForms(costRev)) {
				AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
				if (costFormAifs.length == 2) {
					if (costFormAifs[0].getComponent().getType().equals(PropertyDefines.ACTUAL_PIECE_COST_FORM)) {
						pieceCost = (TCComponentForm) costFormAifs[0].getComponent();
					} else {
						pieceCost = (TCComponentForm) costFormAifs[1].getComponent();
					}
					changeOwner(pieceCost);
				}
			} else {
				logger.error("Cost forms are not valid!");
			}
		} else {
			logger.error("Cannot get cost revision!");
		}

		return pieceCost;
	}

	public boolean validatePartType() {
		boolean isValid = false;
		String type = partRev.getType();
		for (String validType : VF4_DESIGN_REVISION) {
			if (type.equals(validType)) {
				isValid = true;
			}
		}
		return isValid;
	}

	private TCComponentItemRevision createVFCost() throws CannotCreateCostException, TCException {
		String partId = partRev.getProperty("item_id");
		ItemProperties[] itemProperties = new ItemProperties[1];
		itemProperties[0] = new ItemProperties();
		itemProperties[0].name = partRev.getProperty("object_name");
		itemProperties[0].itemId = partId;
		itemProperties[0].type = VF4_COST;
		CreateItemsResponse createResponse = dms.createItems(itemProperties, null, null);
		TCComponentItemRevision costRev = null;
		if (createResponse.serviceData.sizeOfPartialErrors() == 0) {
			costRev = createResponse.output[0].itemRev;
		} else {
			throw new CannotCreateCostException("Cannot create Cost. Exception: " + TCExtension.hanlderServiceData(createResponse.serviceData));
//			ServiceData serviceData = createResponse.serviceData;
//			for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
//				for (String msg : serviceData.getPartialError(i).getMessages()) {
//					logger.error(msg);
//				}
//			}
//
//			if (costRev == null) {
//				throw new CannotCreateCostException("Cannot create Cost. Please contact your administrator.");
//			}
		}

		// attach cost object to part
		try {
			setCost(costRev);
		} catch (Exception ex) {
			logger.error("Cannot relate create cost " + costRev.getProperty("object_string") + " to part.");
			logger.error(ex);
			ex.printStackTrace();
		}

		AIFComponentContext[] costFormAifs = costRev.getRelated(VF4_SOURCING_COST_FORM_RELA);
		if (costFormAifs.length == 2) {
			TCComponentForm targetCost = null;
			TCComponentForm pieceCost = null;
			if (costFormAifs[0].getComponent().getType().equals(PropertyDefines.ACTUAL_TARGET_COST_FORM)) {
				targetCost = (TCComponentForm) costFormAifs[0].getComponent();
				pieceCost = (TCComponentForm) costFormAifs[1].getComponent();
			} else {
				targetCost = (TCComponentForm) costFormAifs[1].getComponent();
				pieceCost = (TCComponentForm) costFormAifs[0].getComponent();
			}

			String validTargetCostName = generateTargetCostFormName(costRev);
			String validPieceCostName = generatePieceCostFormName(costRev);

			TCAccessControlService acl = session.getTCAccessControlService();
			boolean useCanWriteForms = (acl.checkPrivilege(targetCost, "WRITE") && acl.checkPrivilege(pieceCost, "WRITE"));
			if (useCanWriteForms) {
				targetCost.setProperty("object_name", validTargetCostName);
				pieceCost.setProperty("object_name", validPieceCostName);
				if (this.part.getProperty("vf4_item_make_buy").compareToIgnoreCase("Make") == 0) {
					pieceCost.setProperty("vf4_quality_of_finance", "Cost Engineering Estimate");
				}
			}

		} else {
			logger.error("Cannot get target & piece cost form from cost object.");
		}

		return costRev;
	}

	private String generatePieceCostFormName(TCComponentItemRevision costRev) throws TCException {
		String partId = costRev.getProperty("item_id");
		return partId + PropertyDefines.ACTUAL_PIECE_COST_FORM_SUFFIX_NAME;
	}

	private String generateTargetCostFormName(TCComponentItemRevision costRev) throws TCException {
		String partId = costRev.getProperty("item_id");
		return partId + PropertyDefines.ACTUAL_TARGET_COST_FORM_SUFFIX_NAME;
	}

	private DeepCopyData createDeepCopyRuleForTargetCost() throws TCException, CannotCreateCostException {
		TCComponent targetCost = getOrCreateOrSearchAndRelateTargetCost(false);
		DeepCopyData deepCopyDataForTarget = new DeepCopyData();
		deepCopyDataForTarget.action = DeepCopyInfo.COPY_AS_OBJECT_ACTION;
		deepCopyDataForTarget.copyRelations = true;
		deepCopyDataForTarget.isRequired = true;
		deepCopyDataForTarget.isTargetPrimary = true;
		deepCopyDataForTarget.newName = targetCost.getProperty("object_name");
		deepCopyDataForTarget.relationTypeName = VF4_SOURCING_COST_FORM_RELA;
		deepCopyDataForTarget.otherSideObjectTag = targetCost;
		return deepCopyDataForTarget;
	}

	private DeepCopyData createDeepCopyRuleForPieceCost() throws TCException, CannotCreateCostException {
		TCComponent pieceCost = getOrCreateOrSearchAndRelatePieceCost(false);
		DeepCopyData deepCopyDataForPiece = new DeepCopyData();
		deepCopyDataForPiece.action = DeepCopyInfo.COPY_AS_OBJECT_ACTION;
		deepCopyDataForPiece.copyRelations = true;
		deepCopyDataForPiece.isRequired = true;
		deepCopyDataForPiece.isTargetPrimary = true;
		deepCopyDataForPiece.newName = pieceCost.getProperty("object_name");
		deepCopyDataForPiece.relationTypeName = VF4_SOURCING_COST_FORM_RELA;
		deepCopyDataForPiece.otherSideObjectTag = pieceCost;
		return deepCopyDataForPiece;
	}

	public void reviseCost(String description) throws Exception {
		final TCComponentItemRevision costRev = getOrSearchCostAndRemoveWrongCosts();
		if (costRev != null) {
			final TCComponentItem costItem = costRev.getItem();
			final String itemName = costItem.getProperty("object_name");

			ReviseInfo reviseInfo = new ReviseInfo();
			reviseInfo.baseItemRevision = costRev;
			reviseInfo.description = description;
			reviseInfo.name = itemName;
			reviseInfo.clientId = String.valueOf(System.currentTimeMillis());

			DeepCopyData deepCopyData1 = createDeepCopyRuleForTargetCost();
			DeepCopyData deepCopyData2 = createDeepCopyRuleForPieceCost();

			reviseInfo.deepCopyInfo = new com.teamcenter.services.rac.core._2008_06.DataManagement.DeepCopyData[] { deepCopyData1, deepCopyData2 };

			ReviseInfo[] reviseInfos = new ReviseInfo[] { reviseInfo };
			ReviseResponse2 response = dms.revise2(reviseInfos);
			int errorNum = response.serviceData.sizeOfPartialErrors();
			StringBuffer errorMessage = new StringBuffer();
			for (int i = 0; i < errorNum; i++) {
				ErrorStack error = response.serviceData.getPartialError(i);
				for (String errMsg : error.getMessages()) {
					errorMessage.append(errMsg).append("\n");
				}
			}

			if (errorNum > 1) {
				// there is always one error 214124 as not defining revision id
				throw new Exception(errorMessage.toString());
			}
		} else {
			logger.error("WARNING: Cannot find valid cost!");
		}
	}

	public TCComponent createCostForm(String formName, String formType) {
		try {
			CreateIn in = new CreateIn();
			in.data.boName = formType;
			in.data.stringProps.put("object_name", formName);
			CreateIn[] ins = new CreateIn[] { in };
			CreateResponse res = dms.createObjects(ins);
			if (res.output.length > 0 && res.output[0].objects.length > 0) {
				// change ownerships
				TCComponent form = res.output[0].objects[0];
				changeOwner(form);
				return form;
			} else {
				String errorMessage = Utils.getErrorMessagesFromSOA(res.serviceData);
				throw new Exception(errorMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
