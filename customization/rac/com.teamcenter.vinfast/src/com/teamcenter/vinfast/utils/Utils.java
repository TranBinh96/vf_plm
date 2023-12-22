package com.teamcenter.vinfast.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.newdataset.DatasetInfoContainer;
import com.teamcenter.rac.commands.newdataset.DatasetUtil;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentRoleType;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleConfigInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.cad._2019_06.StructureManagement.CreateWindowsInfo3;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationship;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachment;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsInput;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsResponse;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2006_03.SavedQuery;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.SavedQueryObject;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesInputInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesOutput;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesResponse;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;

public class Utils {
	private TCSession session;
	public static Map<String, TCComponentQuery> queryMap = null;
	public static String DEFAULT_MODULE_GROUP_ATTR_NAME = "DEFAULT";
	public final static String PREFERENCE_VF_PROGRAM_NAMES = "VF_Program_Names";
	private static Logger LOGGER;

	public Utils() {
		LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public static String[] getPreferenceValues(TCSession session, String prefName) {
		String[] values = null;
		TCPreferenceService preService = session.getPreferenceService();
		values = preService.getStringValues(prefName);
		return values;
	}

	public static String[] getLovValues2(TCSession session, String property, String objectType) throws TCException {
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] output = valuesInfo.getStringListOfValues();
			return output;
		} else {
			return null;
		}
	}

	public static LinkedHashMap<String, String> getLovDetailInfo(TCSession session, String property, String objectType) throws TCException {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] realVal = valuesInfo.getStringListOfValues();
			String[] disval = valuesInfo.getLOVDisplayValues();
			int i = 0;
			for (String value : realVal) {
				output.put(value, disval[i]);
				i++;
			}
			return output;
		} else {
			return null;
		}
	}

	public static String[] getLovValues(TCSession session, String property, TCComponent selectedPart) {
		String[] values = null;
		LOVService lovService = LOVService.getService(session);
		LOVAttachmentsInput lovInput = new LOVAttachmentsInput();
		try {
			lovInput.objects = new TCComponent[] { selectedPart };
			lovInput.properties = new String[] { property };

			LOVAttachmentsResponse response = lovService.getLOVAttachments(new LOVAttachmentsInput[] { lovInput });
			Map<TCComponent, LOVAttachment[]> lovValues = response.lovAttachments;
			Set<TCComponent> keys = lovValues.keySet();
			for (TCComponent key : keys) {
				System.out.println(key);
				LOVAttachment[] lovs = lovValues.get(key);
				TCComponentListOfValues listOfValues = lovs[0].lov;
				ListOfValuesInfo valuesInfo = listOfValues.getListOfValues();
				values = valuesInfo.getStringListOfValues();
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return values;
	}

	public void createWorkflowProcess(TCSession session, String wfName, HashMap<String, TCComponentBOMLine> selectedLines) {
		WorkflowService wfService = WorkflowService.getService(session);
		ContextData contextData = new ContextData();
		contextData.attachmentCount = selectedLines.size();
	}

	public boolean checkValidType(InterfaceAIFComponent[] selComps) {
		boolean isValid = true;
		try {
			for (InterfaceAIFComponent selectedPart : selComps) {
				TCComponentBOMLine bomline = (TCComponentBOMLine) selectedPart;
				String objType = bomline.getItem().getType();
				if ((objType.equals("VF Part") || objType.equals("VF4_Design")) == false) {
					isValid = false;
					break;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return isValid;
	}

	public HashMap<String, TCComponentBOMLine> checkValidModule(InterfaceAIFComponent[] selComps, String moduleAttributeName) {
		HashMap<String, TCComponentBOMLine> processList = new HashMap<String, TCComponentBOMLine>();
		ArrayList<String> module = new ArrayList<String>();
		try {
			for (InterfaceAIFComponent selectedPart : selComps) {
				TCComponentBOMLine bomline = (TCComponentBOMLine) selectedPart;
				String UID = bomline.getItemRevision().getUid();

				String lovValue = bomline.getProperty(moduleAttributeName);
				if (module.isEmpty()) {
					module.add(lovValue);
				}

				if (module.contains(lovValue) == true) {
					processList.put(UID, bomline);
				} else {
					return null;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return processList;
	}

	public boolean checkIsEmptyModule(InterfaceAIFComponent[] selComps) {
		boolean isEmpty = false;
		try {
			for (InterfaceAIFComponent selectedPart : selComps) {
				TCComponentBOMLine bomline = (TCComponentBOMLine) selectedPart;
				String lovValue = bomline.getProperty("VL5_module_group");

				if (lovValue.length() == 0) {
					return true;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return isEmpty;
	}

	public boolean checkValidGroup(String[] validGroups, String currentGroup) {
		boolean isvalid = false;
		for (String validGroup : validGroups) {
			if (currentGroup.contains(validGroup) || currentGroup.compareToIgnoreCase(validGroup) == 0) {
				return true;
			}
		}
		return isvalid;
	}

	public static TCComponent[] executeSavedQuery(String queryName, String[] values, String[] parameters) throws Exception {
		if (queryMap == null) {
			queryMap = new HashMap<String, TCComponentQuery>();
		}

		TCComponentQuery query = queryMap.get(queryName);
		if (query == null) {
			SavedQueryService savedQueryService = SavedQueryService.getService((TCSession) AIFUtility.getCurrentApplication().getSession());
			SavedQuery.GetSavedQueriesResponse getSavedQueriesResponse = savedQueryService.getSavedQueries();
			for (SavedQueryObject queryObject : getSavedQueriesResponse.queries) {
				if (queryObject.name.equalsIgnoreCase(queryName)) {
					query = queryObject.query;
					queryMap.put(queryName, query);
					break;
				}
			}
		}

		if (query != null) {
			return query.execute(parameters, values);
		} else {
			throw new Exception("Cannot find query \"" + queryName + "\".");
		}
	}

	public static String getErrorMessagesFromSOA(ServiceData serviceData) {
		int errorNum = serviceData.sizeOfPartialErrors();
		StringBuffer errorMessage = new StringBuffer();
		for (int i = 0; i < errorNum; i++) {
			ErrorStack error = serviceData.getPartialError(i);
			for (String errMsg : error.getMessages()) {
				errorMessage.append(errMsg).append("\n");
			}
		}
		return errorMessage.toString();
	}

	public static TCComponentDataset createDataset(TCSession session, String datasetName, String datasetTypeStr, String datasetDescription, String absolutePath) throws TCException {
		TCComponentDatasetDefinitionType datasetDefinitionType = null;
		TCComponentDatasetDefinition datasetDefinition = null;
		NamedReferenceContext[] contextList = null;

		// get file information
		File file = new File(absolutePath);
		String fileExtension = DatasetInfoContainer.getFileExtension(file);

		// get dataset definitions
		datasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
		datasetDefinition = datasetDefinitionType.find(datasetTypeStr);
		String datasetTypeInternalStr = datasetDefinition.toInternalString();

		// get named reference definitions
		contextList = DatasetUtil.getNamedReferenceContext(datasetDefinition, session, fileExtension);
		String namedRefType = contextList[0].getNamedReference();

		String[] fileNamesArray = new String[] { absolutePath };
		String[] namedRefArray = new String[] { namedRefType };

		TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent(datasetTypeInternalStr);
		TCComponentDataset dataset = datasetType.setFiles(datasetName, datasetDescription, datasetTypeInternalStr, fileNamesArray, namedRefArray);

		dataset.setStringProperty("object_name", datasetName);

		return dataset;
	}

	public Map<String, TCComponent> getAllWorkflowTemplates() {
		GetWorkflowTemplatesInputInfo templateInfo = new GetWorkflowTemplatesInputInfo();
		String[] results = new String[0];
		// templateInfo.targetObjects = values;
		// templateInfo.group = group;
		// templateInfo.getFiltered = true;
		// templateInfo.objectTypes = new String[] { "VF4_DesignRevision" };
		Map<String, TCComponent> templateNamesAndComps = new HashMap<String, TCComponent>();
		WorkflowService wfService = WorkflowService.getService(session);
		GetWorkflowTemplatesResponse response = wfService.getWorkflowTemplates(new GetWorkflowTemplatesInputInfo[] { templateInfo });
		GetWorkflowTemplatesOutput[] template = response.templatesOutput;

		if (template != null && template.length > 0) {
			TCComponentTaskTemplate[] templates = template[0].workflowTemplates;
			results = new String[templates.length];
			int i = 0;
			for (TCComponentTaskTemplate wftemplate : templates) {
				try {
					results[i++] = wftemplate.getName();
					templateNamesAndComps.put(wftemplate.getName(), wftemplate);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}

		return templateNamesAndComps;
	}

	public Map<String, String> getProgramsAndModuleAttributes() {
		TCPreferenceService preferenceService = session.getPreferenceService();
		String[] PROGRAMS_MODULES_ATTRIBUTES_NAME = preferenceService.getStringValues("VF_PROGRAMS_MODULES_ATTRIBUTES_NAME");

		Map<String, String> programsAndModuleAttributes = new HashMap<String, String>();
		for (String programAndModuleAttribute : PROGRAMS_MODULES_ATTRIBUTES_NAME) {
			programAndModuleAttribute = programAndModuleAttribute.trim();
			String key = programAndModuleAttribute.split("=")[0];
			String value = programAndModuleAttribute.split("=")[1];
			programsAndModuleAttributes.put(key, value);
		}
		return programsAndModuleAttributes;
	}

	public static String getProgram(String group, TCSession session) {
		String program = "";
		String[] programs = getPreferenceValues(session, Utils.PREFERENCE_VF_PROGRAM_NAMES);
		for (String pgrm : programs) {

			if (group.contains(pgrm)) {
				program = pgrm;
				break;
			}
		}
		return program;
	}

	public static int compareDate(String current, String newer) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		Date currentDate = null;
		Date newDate = null;
		try {
			currentDate = sdf.parse(current);
		} catch (ParseException e1) {
			System.out.println("[Utils] Invalid currentDate: " + current);
		}
		try {
			newDate = sdf.parse(newer);
		} catch (ParseException e) {
			System.out.println("[Utils] Invalid newDate: " + newer);
		}
		if (currentDate != null && newDate != null) {
			if (currentDate.after(newDate)) {
				return -1;
			}

			if (currentDate.before(newDate)) {
				return 1;
			}

			if (currentDate.equals(newDate)) {
				return 0;
			}
		}
		if (currentDate == null && newDate != null) {
			return 1;
		}
		if (currentDate != null && newDate == null) {
			return -2;
		}
		if (currentDate == null && newDate == null) {
			return 1;
		}
		return 0;
	}

	public static ArrayList<TCComponent> GetSecondaryObjectByRelationName(TCSession connection, TCComponent primaryObj, String[] secondaryObjType, String relationName) {
		DataManagementService dms = DataManagementService.getService(connection);
		ArrayList<TCComponent> secondaryObj = new ArrayList<TCComponent>();
		ExpandGRMRelationsPref2 pref = new ExpandGRMRelationsPref2();
		pref.info = new RelationAndTypesFilter[1];
		pref.info[0] = new RelationAndTypesFilter();
		if (!relationName.isEmpty()) {
			pref.info[0].relationTypeName = relationName;
			pref.info[0].otherSideObjectTypes = secondaryObjType;
		}
		ExpandGRMRelationsResponse2 relationResponse = dms.expandGRMRelationsForPrimary(new TCComponent[] { primaryObj }, pref);
		if (relationResponse.serviceData.sizeOfPartialErrors() > 0) {
			LOGGER.error("[Utils] Exception: " + HanlderServiceData(relationResponse.serviceData));
			return null;
		}
		if (relationResponse.output.length == 0) {
			return null;
		}
		ExpandGRMRelationsOutput2 output = relationResponse.output[0];
		ExpandGRMRelationsData2[] rdata = output.relationshipData;
		for (int j = 0; j < rdata.length; j++) {
			ExpandGRMRelationship[] childs = rdata[j].relationshipObjects;
			for (int i = 0; i < childs.length; i++) {
				TCComponent mo = childs[i].otherSideObject;
				String objectType = mo.getTypeObject().getName();
				if (mo != null && Arrays.asList(secondaryObjType).contains(objectType)) {
					secondaryObj.add(childs[i].otherSideObject);
				}
			}
		}

		return secondaryObj;
	}

	public static TCComponent[] GetPrimaryObjectByRelationName(TCSession connection, TCComponent secondaryObj, String primaryObjType, String relationName) {
		DataManagementService dms = DataManagementService.getService(connection);
		ExpandGRMRelationsPref2 pref = new ExpandGRMRelationsPref2();
		pref.info = new RelationAndTypesFilter[1];
		pref.info[0] = new RelationAndTypesFilter();
		if (!relationName.isEmpty()) {
			pref.info[0].relationTypeName = relationName;
			pref.info[0].otherSideObjectTypes = new String[] { primaryObjType };
		}
		ExpandGRMRelationsResponse2 relationResponse = dms.expandGRMRelationsForSecondary(new TCComponent[] { secondaryObj }, pref);
		if (relationResponse.serviceData.sizeOfPartialErrors() > 0) {
			LOGGER.error("[Utils] Exception: " + HanlderServiceData(relationResponse.serviceData));
			return null;
		}
		if (relationResponse.output.length == 0) {
			return null;
		}

		ExpandGRMRelationsOutput2 output = relationResponse.output[0];
		ExpandGRMRelationsData2[] rdata = output.relationshipData;
		TCComponent[] primaryObj = null;
		for (int j = 0; j < rdata.length; j++) {
			ExpandGRMRelationship[] childs = rdata[j].relationshipObjects;
			primaryObj = new TCComponent[childs.length];
			for (int i = 0; i < childs.length; i++) {
				primaryObj[i] = childs[i].otherSideObject;
			}
		}
		return primaryObj;
	}

	public static String HanlderServiceData(ServiceData sd) {
		StringBuilder strOut = new StringBuilder();
		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (ErrorValue er : errorValue) {
				if (strOut.length() > 0)
					strOut.append(", ");
				strOut.append(er.getMessage());
			}
		}
		return strOut.toString();
	}

	public static void getUserByGroupAndRole(TCSession session, String group, String role, LinkedHashMap<String, TCComponentUser> output) {
		// <UserName, UserObject>
		try {
			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
			TCComponentGroup comGroup = groupType.find(group);
			TCComponentRole comRole = roleType.find(role);
			TCComponentGroupMember[] groupMem = GMType.findByRole(comRole, comGroup);
			for (int j = 0; j < groupMem.length; j++) {
				output.put(groupMem[j].getUser().toString(), groupMem[j].getUser());
			}
		} catch (TCException e) {
			LOGGER.error("[Utils] Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public static void getGroupMember(TCSession session, String group, String role, LinkedHashMap<String, TCComponentGroupMember> output) {
		try {
			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
			TCComponentGroup comGroup = groupType.find(group);
			TCComponentRole comRole = roleType.find(role);
			TCComponentGroupMember[] groupMem = GMType.findByRole(comRole, comGroup);
			for (int j = 0; j < groupMem.length; j++) {
				output.put(groupMem[j].getUser().toString(), groupMem[j]);
			}
		} catch (TCException e) {
			LOGGER.error("[Utils] Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public static TCComponent getFirstGroupMember(String userid) throws Exception {
		TCComponent[] gms = Utils.executeSavedQuery("Admin - User Memberships", new String[] { userid }, new String[] { "Id" });
		TCComponent gm = null;
		if (gms.length > 0) {
			gm = gms[0];
		}

		return gm;
	}

	public static boolean createWorkflowProcess(TCSession session, TCComponent[] targets, String wfTemplate, String description, String subject, String name) {
		String[] targetUIDs;
		if (targets == null || targets.length < 0) {
			return false;
		} else {
			targetUIDs = new String[targets.length];
			for (int i = 0; i < targets.length; i++) {
				targetUIDs[i] = targets[i].getUid();
			}
		}

		ContextData contextData = new ContextData();
		contextData.processTemplate = wfTemplate; // update with your own Process Template.
		contextData.subscribeToEvents = false;
		contextData.subscriptionEventCount = 0;
		contextData.attachmentCount = targets.length;
		contextData.attachments = targetUIDs;
		contextData.attachmentTypes = new int[] { EPM_attachement.target.value() };
		// (1 == 'EPM_target_attachment') - AttachmentTypes are defined in include / epm
		// / epm.h
		WorkflowService wfService = WorkflowService.getService(session);
		InstanceInfo instanceInfo = wfService.createInstance(true, null, name, subject, description, contextData);

		if (instanceInfo.serviceData.sizeOfPartialErrors() > 0) {
			LOGGER.error("[Utils]: " + HanlderServiceData(instanceInfo.serviceData));
			return false;
		} else {
			LOGGER.error("[Utils] New WorkFlow Instance: " + instanceInfo.instanceKey);
			return true;
		}
	}

	public enum EPM_attachement {
		target(1), reference(3), signoff(4), release_status(5), comment(6), instruction(7), interprocess(8), project_task(9);

		private final int value;

		EPM_attachement(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}

	}

	public static LinkedHashMap<String, TCComponentRevisionRule> getRevisionRule2(TCSession session) {
		LinkedHashMap<String, TCComponentRevisionRule> out = new LinkedHashMap<String, TCComponentRevisionRule>();
		try {
			DataManagementService dmService = DataManagementService.getService(session);
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			if (RuleInfo != null) {
				TCComponentRevisionRule[] ruleObj = new TCComponentRevisionRule[RuleInfo.length];
				int count = 0;
				for (RevisionRuleInfo rule : RuleInfo) {
					ruleObj[count] = rule.revRule;
					count++;
				}

				if (ruleObj.length != 0) {
					dmService.getProperties(ruleObj, new String[] { "object_name" });
					int i = 0;
					for (TCComponentRevisionRule rule : ruleObj) {
						String ruleName = rule.getProperty("object_name");
						out.put(ruleName, rule);
					}
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return out;
	}

	public static String[] getRevisionRule(TCSession session) {
		String[] revRule = {};
		try {
			DataManagementService dmService = DataManagementService.getService(session);
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			if (RuleInfo != null) {
				TCComponentRevisionRule[] ruleObj = new TCComponentRevisionRule[RuleInfo.length];
				int count = 0;
				for (RevisionRuleInfo rule : RuleInfo) {
					ruleObj[count] = rule.revRule;
					count++;
				}

				if (ruleObj.length != 0) {
					revRule = new String[ruleObj.length];
					dmService.getProperties(ruleObj, new String[] { "object_name" });
					int i = 0;
					for (TCComponentRevisionRule rule : ruleObj) {
						revRule[i] = rule.getProperty("object_name");
						i++;
					}
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return revRule;

	}

	public static String CalendarToTcDateString(final Calendar date) {
		// Following two formats are supported for date string:
		// yyyy-MM-ddThh:mm:sszz:zz 2005-05-20T14:32:05-08:00
		// yyyy-MM-ddThh:mm:ss.SSSzz:zz 2005-05-20T14:32:05.345-08:00

		// final String format = "%1$tFT%1$tT"; // without millisecons
		final String format = "%1$tFT%1$tT.%1$tL"; // with milliseconds
		final String timeZone = String.format("%1$tz", date);

		return String.format(format + timeZone.substring(0, 3) + ":" + timeZone.substring(3), date);
	}

	public static TCComponentBOMLine createBOMWindow(TCSession session, TCComponentItemRevision revision) {

		TCComponentBOMLine topLine = null;

		try {
			StructureManagementService SMService = StructureManagementService.getService(session);
			CreateWindowsInfo3 info = new CreateWindowsInfo3();
			info.item = revision.getItem();
			info.itemRev = revision;

			CreateBOMWindowsResponse response = SMService.createOrReConfigureBOMWindows(new CreateWindowsInfo3[] { info });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				SoaUtil.buildErrorMessage(response.serviceData);
			} else {
				CreateBOMWindowsOutput[] output = response.output;
				return output[0].bomLine;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return topLine;
	}

	public static TCComponentBOMLine createBOMWindow(TCSession session, TCComponentItemRevision revision, TCComponentRevisionRule revisionRule) {

		TCComponentBOMLine topLine = null;

		try {
			StructureManagementService SMService = StructureManagementService.getService(session);
			CreateWindowsInfo3 info = new CreateWindowsInfo3();
			info.item = revision.getItem();
			info.itemRev = revision;
			RevisionRuleConfigInfo revCfgInfo = new RevisionRuleConfigInfo();
			revCfgInfo.revRule = revisionRule;
			info.revRuleConfigInfo = revCfgInfo;
			CreateBOMWindowsResponse response = SMService.createOrReConfigureBOMWindows(new CreateWindowsInfo3[] { info });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				SoaUtil.buildErrorMessage(response.serviceData);
			} else {
				CreateBOMWindowsOutput[] output = response.output;
				return output[0].bomLine;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return topLine;
	}

}
