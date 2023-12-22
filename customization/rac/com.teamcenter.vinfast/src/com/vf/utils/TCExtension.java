package com.vf.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.commands.open.OpenCommand;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.administration.IRMService;
import com.teamcenter.services.rac.administration.PreferenceManagementService;
import com.teamcenter.services.rac.administration._2006_03.IRM;
import com.teamcenter.services.rac.administration._2006_03.IRM.CheckAccessorPrivilegesResponse;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.PreferenceDefinition;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.PreferenceValue;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.SetPreferences2In;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleConfigInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.rac.core._2006_03.Session.GetGroupMembershipResponse;
import com.teamcenter.services.rac.core._2006_03.Session.GetSessionGroupMemberResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationship;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.AssignmentLists;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesInputInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesOutput;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesResponse;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.escooter.sorprocess.model.AssigmentListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.vf4.services.rac.custom.IntegrationService;
import com.vf4.services.rac.custom.ReportDataSourceService;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCostCalcResponse;
import com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPInput;
import com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput;

public class TCExtension {

	public static String[] releaseStatusList = { "Sourcing", "SCR", "P", "PCR", "I", "ICR", "PR", "PPR" };

	public static int getReleaseStatusIndex(String status) {
		int i = 0;
		for (String releaseStatus : releaseStatusList) {
			if (status.compareTo(releaseStatus) == 0)
				return i;
			i++;
		}
		return -1;
	}

	public static String[] GetLOV(String property, String objectType, String chooseValue, TCSession session) throws TCException {
		String[] output = null;
		Object[][] lovValues = getLovDisplayAndTrueValues(property, objectType, session);
		for (Object[] lovVals : lovValues) {
			if (chooseValue.equals(lovVals[1])) {
				TCComponentListOfValues childLov = (TCComponentListOfValues) lovVals[2];
				ListOfValuesInfo lovInfo = childLov.getListOfValues();
				output = lovInfo.getLOVDisplayValues();
			}
		}
		return output;
	}

	public static Object[][] getLovDisplayAndTrueValues(String property, String objectType, TCSession session) throws TCException {
		Object[][] value = null;
		try {
			TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
			TCComponentListOfValues tmpcom = propertyDescriptor.getLOV();

			ListOfValuesInfo lovInfo = tmpcom.getListOfValues();
			String[] displayValues = lovInfo.getLOVDisplayValues();
			Object[] values = lovInfo.getListOfValues();
			TCComponentListOfValues[] listVals = lovInfo.getListOfFilters();
			int valueslength = displayValues.length;
			value = new Object[valueslength][3];
			for (int i = 0; i < valueslength; i++) {
				value[i][0] = displayValues[i];
				value[i][1] = values[i].toString();
				value[i][2] = listVals[i];
			}
			return value;
		} catch (TCException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static String[] GetLovValues(String property, String objectType, TCSession session) {
		try {
			TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] output = valuesInfo.getLOVDisplayValues();
			Arrays.sort(output, String.CASE_INSENSITIVE_ORDER);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String[] GetLovValues(String lovName) throws TCException {
		String[] output = {};
		TCComponentListOfValues color_spec = TCComponentListOfValuesType.findLOVByName(lovName);
		color_spec.clearCache();
		ListOfValuesInfo color_spec_lov = color_spec.getListOfValues();
		output = color_spec_lov.getStringListOfValues();
		Arrays.sort(output, String.CASE_INSENSITIVE_ORDER);
		return output;
	}

	public static LinkedHashMap<String, String> GetLovValueAndDisplayInterdependent(String property, String objectType, String[] parentValue, TCSession session) {
		try {
			LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
			TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
			if (propertyDescriptor.hasLOVAttached()) {
				TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
				ListOfValuesInfo parentInfo = attachedLOV.getListOfValues();

				GetLOVByLevel(0, parentValue, parentInfo, output);

				return output;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static LinkedHashMap<String, String> GetLOVByLevel(int level, String[] parentValue, ListOfValuesInfo parentInfo, LinkedHashMap<String, String> output) throws TCException {
		String[] parentValues = parentInfo.getStringListOfValues();
		TCComponentListOfValues[] listVals = parentInfo.getListOfFilters();
		if (parentValues.length > 0) {
			for (int i = 0; i < parentValues.length; i++) {
				if (parentValues[i].compareToIgnoreCase(parentValue[level]) == 0) {
					ListOfValuesInfo childInfo = listVals[i].getListOfValues();
					if (level == parentValue.length - 1) {
						String[] childValues = childInfo.getStringListOfValues();
						String[] childTexts = childInfo.getLOVDisplayValues();
						int j = 0;
						for (String value : childValues) {
							output.put(value, childTexts[j]);
							j++;
						}
						return output;
					} else {
						level++;
						return GetLOVByLevel(level, parentValue, childInfo, output);
					}
				}
			}
		}
		return output;
	}

	public static LinkedHashMap<String, String> SortingLOV(LinkedHashMap<String, String> output) {
		List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(output.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> a, Entry<String, String> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		LinkedHashMap<String, String> sortedMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static TCComponent[] getALByWorkflow(String workflowName, TCSession session) {
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Template Name", workflowName);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "__Workflow Template");

			if (item_search != null && item_search.length > 0) {
				TCComponentTaskTemplate workflow = (TCComponentTaskTemplate) item_search[0];
				return workflow.getRelatedComponents("assignment_lists");
			}

//			TCComponentTaskTemplateType tCComponentTaskTemplateType = (TCComponentTaskTemplateType) session.getTypeComponent("EPMTaskTemplate");
//			if (tCComponentTaskTemplateType != null) {
//				TCComponentTaskTemplate[] workflowList = tCComponentTaskTemplateType.getProcessTemplates(false, false, null, null, null);
//				for (TCComponentTaskTemplate workflow : workflowList) {
//					if (workflow.getName().compareTo(workflowName) == 0) {
//						TCComponent[] list = workflow.getRelatedComponents("assignment_lists");
//						return workflow.getRelatedComponents("assignment_lists");
//					}
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static TCComponentAssignmentList GetALByName(String aclName, TCSession session) throws Exception {
		WorkflowService wfService = WorkflowService.getService(session);
		AssignmentLists lists = wfService.getAssignmentLists(new String[] { aclName });
		TCComponentAssignmentList[] list = lists.assignedLists;
		if (list != null && list.length > 0)
			return list[0];

		return null;
	}

	public static LinkedHashMap<String, String> GetLovValueAndDisplay(String property, String objectType, TCSession session) throws TCException {
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

			return SortingLOV(output);
		} else {
			return null;
		}
	}

	public static LinkedHashMap<String, String> GetLovValueAndDisplay(String lovName) throws TCException {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		TCComponentListOfValues attachedLOV = TCComponentListOfValuesType.findLOVByName(lovName);
		attachedLOV.refresh();
		ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();

		String[] realVal = valuesInfo.getStringListOfValues();
		String[] disval = valuesInfo.getLOVDisplayValues();
		int i = 0;
		for (String value : realVal) {
			output.put(value, disval[i]);
			i++;
		}

		return SortingLOV(output);
	}

	public static LinkedHashMap<String, String> GetLovValueAndDescription(String property, String objectType, TCSession session) throws TCException {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] realVal = valuesInfo.getStringListOfValues();
			String[] disval = valuesInfo.getDescriptions();
			int i = 0;
			for (String value : realVal) {
				output.put(value, value + " - " + disval[i]);
				i++;
			}

			return SortingLOV(output);
		} else {
			return null;
		}
	}

	public static LinkedHashMap<String, String> getLovValueAndDescription(String property, String objectType, TCSession session) throws TCException {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] realVal = valuesInfo.getStringListOfValues();
			String[] disval = valuesInfo.getDescriptions();
			int i = 0;
			for (String value : realVal) {
				output.put(value, disval[i]);
				i++;
			}

			return SortingLOV(output);
		} else {
			return null;
		}
	}

	public static Map GetLOVList(String property, String objectType, TCSession session) throws TCException {
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
		ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
		Map output = new HashMap<String, String>();
		String[] valueList = valuesInfo.getLOVDisplayValues();
		String[] textList = valuesInfo.getStringListOfValues();

		for (int i = 0; i < valueList.length; i++) {
			output.put(valueList[i], textList[i]);
		}

		TreeMap<String, String> sorted = new TreeMap<>(output);

//		Arrays.sort(output);
		return sorted;
	}

	public static String[] GetPreferenceValues(String prefName, TCSession session) {
		String[] IP_values = null;
		try {
			PreferenceManagementService preferenceService = PreferenceManagementService.getService(session);
			GetPreferencesResponse prefReponse = preferenceService.refreshPreferences2(new String[] { prefName }, false);
			CompletePreference[] pref = prefReponse.response;

			if (pref.length != 0) {
				PreferenceValue values = pref[0].values;
				IP_values = values.values;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return IP_values;
	}

	public static String GetPreferenceDescription(String prefName, TCSession session) {
		String data_output = "";
		PreferenceManagementService preferenceService = PreferenceManagementService.getService(session);
		GetPreferencesResponse prefReponse = preferenceService.refreshPreferences2(new String[] { prefName }, false);
		CompletePreference[] pref = prefReponse.response;

		if (pref.length != 0) {
			PreferenceDefinition definition = pref[0].definition;
			data_output = definition.description;
		}
		return data_output;
	}

	public static CompletePreference getPreference(String prefName, TCSession session) {
		PreferenceManagementService preferenceService = PreferenceManagementService.getService(session);
		GetPreferencesResponse prefReponse = preferenceService.refreshPreferences2(new String[] { prefName }, false);
		CompletePreference[] pref = prefReponse.response;

		if (pref.length != 0)
			return pref[0];
		return null;
	}

	public static String[] GetUOMList(TCSession session) throws TCException {
		String[] UOM = null;
		UOM = GetLovValues("uom_tag", "Item", session);

		ArrayList<String> UOMList = new ArrayList<String>();
		for (String string : UOM) {
			boolean check = true;
			for (String string2 : UOMList) {
				if (string.toUpperCase().compareToIgnoreCase(string2.toUpperCase()) == 0) {
					check = false;
					break;
				}
			}
			if (check) {
				UOMList.add(string);
			}
		}
		UOM = new String[UOMList.size()];
		UOMList.toArray(UOM);
		return UOM;
	}

	public static TCComponent GetUOMItem(String uom) throws TCException {
		TCComponent UOMItem = null;
		String UOMValue = uom;
		TCComponent[] uom_value_tags = GetUOMLOVValues();

		if (!UOMValue.equals("") && uom_value_tags.length != 0) {
			for (int i = 0; i < uom_value_tags.length; i++) {
				TCComponentUnitOfMeasure unit = (TCComponentUnitOfMeasure) uom_value_tags[i];
				if (unit.getProperty("symbol").equals(UOMValue)) {
					UOMItem = uom_value_tags[i];
					break;
				}
			}
		}
		return UOMItem;
	}

	public static TCComponent[] GetUOMLOVValues() throws TCException {
		TCComponent[] lovValues = null;
		try {
			TCComponentListOfValues spec = TCComponentListOfValuesType.findLOVByName("Unit of Measures");
			ListOfValuesInfo spec_lov = spec.getListOfValues();
			lovValues = spec_lov.getTagListOfValues();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return lovValues;
	}

	public static TCComponentGroupMember getCurrentGroupMember(TCSession session) {
		TCComponentGroupMember groupMember = null;
		try {
			SessionService SService = SessionService.getService(session);
			GetSessionGroupMemberResponse response = SService.getSessionGroupMember();
			ServiceData sd = response.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
				ErrorValue[] errorValue = errorStack.getErrorValues();
				for (int inx = 1; inx < errorValue.length; inx++) {
					System.out.println(errorValue[inx].getMessage());
				}

			} else {
				groupMember = response.groupMember;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return groupMember;
	}

	public static boolean checkAccessPermission(TCComponentGroupMember groupMember, TCComponent object, TCSession session) {
		boolean result = false;
		try {
			IRMService irmService = IRMService.getService(session);
			CheckAccessorPrivilegesResponse response = irmService.checkAccessorsPrivileges(groupMember, new TCComponent[] { object }, new String[] { "WRITE" });
			ServiceData sd = response.serviceData;

			if (sd.sizeOfPartialErrors() > 0) {
				return result;
			} else {
				IRM.PrivilegeReport[] report = response.privilegeReports;
				IRM.Privilege[] privilege = report[0].privilegeInfos;
				result = privilege[0].verdict;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static LinkedList<TCComponentBOMLine> expandAllBOMLines(final TCComponentBOMLine rootLine, TCSession session) {
		LinkedList<TCComponentBOMLine> outBomLines = new LinkedList<TCComponentBOMLine>();
		StructureManagementService structureService = StructureManagementService.getService(session);
		ExpandPSAllLevelsInfo levelInfo = new ExpandPSAllLevelsInfo();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";

		ExpandPSAllLevelsPref levelPref = new ExpandPSAllLevelsPref();
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];

		ExpandPSAllLevelsResponse levelResp = structureService.expandPSAllLevels(levelInfo, levelPref);

		if (levelResp.output.length > 0) {
			for (ExpandPSAllLevelsOutput levelOut : levelResp.output) {
				outBomLines.add(levelOut.parent.bomLine);
			}
		}

		return outBomLines;
	}

	public static TCComponentGroupMember GetGroupMemberByUserID(String userID, TCSession session) {
		TCComponentGroupMember groupMember = null;

		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Id", userID);
		inputQuery.put("Group Member Status", "FALSE");
		TCComponent[] item_search = Query.queryItem(session, inputQuery, "Admin - User Memberships");

		if (item_search != null && item_search.length > 0) {
			groupMember = (TCComponentGroupMember) item_search[0];
		}

		return groupMember;
	}

	public static String[] GetRevisionRules(TCSession session) {
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

	public static TCComponentRevisionRule GetRevisionRule(String ruleObj, TCSession session) {
		TCComponentRevisionRule revRule = null;
		try {
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			for (RevisionRuleInfo rule : RuleInfo) {
				if (rule.revRule.getProperty("object_name").equals(ruleObj)) {
					revRule = rule.revRule;
					break;
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return revRule;
	}

	public static TCComponentTaskTemplate GetWorkflowByName(String name, TCSession session) {
		GetWorkflowTemplatesInputInfo templateInfo = new GetWorkflowTemplatesInputInfo();
		WorkflowService wfService = WorkflowService.getService(session);
		GetWorkflowTemplatesResponse response = wfService.getWorkflowTemplates(new GetWorkflowTemplatesInputInfo[] { templateInfo });
		GetWorkflowTemplatesOutput[] template = response.templatesOutput;

		if (template != null && template.length > 0) {
			TCComponentTaskTemplate[] templates = template[0].workflowTemplates;
			for (TCComponentTaskTemplate wftemplate : templates) {
				try {
					if (wftemplate.getName().compareToIgnoreCase(name) == 0) {
						return wftemplate;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public static List<TCComponent> getReferenced(TCComponent selectedObject, String relationName, TCSession session, String objectType) {
		List<TCComponent> resultList = new LinkedList<TCComponent>();
		DataManagementService dm = DataManagementService.getService(session);
		TCComponent[] list = new TCComponent[1];
		list[0] = selectedObject;
		WhereReferencedResponse response1 = dm.whereReferenced(list, 1);
		if (response1 != null) {
			for (WhereReferencedOutput output : response1.output) {
				WhereReferencedInfo[] refs = output.info;
				if (!relationName.isEmpty()) {
					for (WhereReferencedInfo info : refs) {
						if (info.relation.compareTo(relationName) == 0) {
							if (objectType.isEmpty()) {
								resultList.add(info.referencer);
							} else {
								String type = info.referencer.getType();
								if (type.compareTo(objectType) == 0)
									resultList.add(info.referencer);
							}
						}
					}
				} else {
					for (WhereReferencedInfo info : refs) {
						if (objectType.isEmpty()) {
							resultList.add(info.referencer);
						} else {
							String type = info.referencer.getType();
							if (type.compareTo(objectType) == 0)
								resultList.add(info.referencer);
						}
					}
				}
			}
		}
		return resultList;
	}

	public static ServiceData TriggerProcess(String[] partUID, String processTemplate, String assignmentList, String nameProcess, TCSession session) {
		int[] attachmentTypes = new int[partUID.length];
		Arrays.fill(attachmentTypes, 1);

		WorkflowService wfService = WorkflowService.getService(session);
		final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data = new com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData();
		data.attachmentCount = partUID.length;
		data.attachments = partUID;
		data.processTemplate = processTemplate;
		data.attachmentTypes = attachmentTypes;
		if (!assignmentList.isEmpty())
			data.processAssignmentList = assignmentList;

		InstanceInfo intInfo = wfService.createInstance(true, null, nameProcess, null, "", data);
		return intInfo.serviceData;
	}

	public static boolean AssignPerformer(TCComponentProcess process, String taskName, List<TCComponentGroupMember> performerList, TCSession session) throws TCException, ServiceException {
		TCComponentTask rootTask = process.getRootTask();
		TCComponentTask[] subtasks = rootTask.getSubtasks();
		for (int i = 0; i < subtasks.length; i++) {
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 && subtasks[i].getProperty("object_name").compareToIgnoreCase(taskName) == 0) {

				TCComponentTask selectSignOffTask = subtasks[i].getSubtasks()[0];
				if (selectSignOffTask.getTaskType().compareToIgnoreCase("EPMSelectSignoffTask") != 0) {
					selectSignOffTask = subtasks[i].getSubtasks()[1];
				}

				CreateSignoffs createSignoff = new CreateSignoffs();
				createSignoff.task = selectSignOffTask;
				createSignoff.signoffInfo = new CreateSignoffInfo[performerList.size()];
				int j = 0;
				for (TCComponentGroupMember tcGroupMember : performerList) {
					createSignoff.signoffInfo[j] = new CreateSignoffInfo();
					createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
					createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
					createSignoff.signoffInfo[j].signoffMember = tcGroupMember;
					j++;
				}

				ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
				if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
					return false;
				}

				// Set adhoc done
				DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
				propInfo.object = selectSignOffTask;
				propInfo.timestamp = Calendar.getInstance();
				propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
				propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
				propInfo.vecNameVal[0].name = "task_result";
				propInfo.vecNameVal[0].values = new String[] { "Completed" };
				DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
				if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
					return false;
				}

				// Complete selection signoff task if it started
				if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
					try {
						Thread.sleep(1000);
						process.refresh();
						subtasks[i].refresh();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
					paii.clientId = "complete" + selectSignOffTask.getUid();
					paii.action = "SOA_EPM_complete_action";
					paii.actionableObject = selectSignOffTask;
					paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
					paii.supportingValue = "SOA_EPM_completed";

					ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
					if (sd.sizeOfPartialErrors() > 0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static OpenContextInfo[] CreateContextViews(TCComponent object, TCSession session) {
		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponentRevisionRule RevisionRule = GetRevisionRule("Working; Any Status", session);
		CreateInput input = new CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;

		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 1; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}
		} else {
			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;
	}

	public static String GetPropertyRealValue(TCComponent item, String propertyName) {
		try {
			TCProperty moduleGroupProperty = item.getTCProperty(propertyName);
			if (moduleGroupProperty != null)
				return moduleGroupProperty.getStringValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String[] getPropertyRealValues(TCComponent item, String propertyName) throws TCException {
		TCProperty moduleGroupProperty = item.getTCProperty(propertyName);
		if (moduleGroupProperty != null)
			return moduleGroupProperty.getStringArrayValue();
		return null;
	}

	public static String GetUOMValue(String value, String[] uomList) {
		if (value.isEmpty())
			return "";
		if (value.compareToIgnoreCase("each") == 0)
			return "EA";
		for (String item : uomList) {
			if (value.compareToIgnoreCase(item) == 0)
				return value;
		}

		for (String item : uomList) {
			if (value.toUpperCase().compareToIgnoreCase(item) == 0)
				return value.toUpperCase();
		}

		return "";
	}

	public static boolean checkPermission(String[] groupNames, TCSession session) {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			for (String groupName : groupNames) {
				if (group.toString().compareToIgnoreCase(groupName) == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean checkPermissionAccess(TCComponent item, String permission, TCSession session) {
		try {
			TCAccessControlService acl = session.getTCAccessControlService();
			return acl.checkPrivilege(item, permission);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void openComponent(TCComponent newComp) throws Exception {
		AIFDesktop desktop = AIFDesktop.getActiveDesktop();
		Registry registry = Registry.getRegistry("com.teamcenter.rac.common.actions.actions");
		OpenCommand openCommand = (OpenCommand) registry.newInstanceForEx("openCommand", new Object[] { desktop, newComp });
		openCommand.executeModeless();
	}

	public static List<TCComponent> getRelatedComponents(DataManagementService dmService, TCComponent primaryObject, String relation) {
		List<TCComponent> relatedObj = new LinkedList<TCComponent>();
		com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter filter = new com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter();
//		filter.otherSideObjectTypes = objectType;
		filter.relationTypeName = relation;

		ExpandGRMRelationsPref2 relationPref = new ExpandGRMRelationsPref2();
		relationPref.expItemRev = false;
		relationPref.returnRelations = false;
		relationPref.info = new com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter[] { filter };

		ExpandGRMRelationsResponse2 response = dmService.expandGRMRelationsForPrimary(new TCComponent[] { primaryObject }, relationPref);
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			System.out.println(TCExtension.hanlderServiceData(response.serviceData));
			return null;
		}
		ExpandGRMRelationsOutput2[] output = response.output;
		ExpandGRMRelationsData2[] data = output[0].relationshipData;
		ExpandGRMRelationship[] relations = data[0].relationshipObjects;

		if (relations.length != 0) {
			for (ExpandGRMRelationship relationObj : relations) {
				relatedObj.add(relationObj.otherSideObject);
			}
		}
		return relatedObj;
	}

	public static LinkedHashMap<String, VFPartCost> extractPartCostInfo(String[] partList, TCSession session) {
		LinkedHashMap<String, VFPartCost> partCostMap = new LinkedHashMap<String, VFPartCost>();
		ReportDataSourceService reportService = ReportDataSourceService.getService(session);
		// testing
		String test = "BAT11002002";
		// testing
		// String[] calcCostInput = this.listPartIDandMakeBuy.toArray(new
		// String[this.listPartIDandMakeBuy.size()]);
		// VFPartCostCalcResponse response =
		// reportService.calculatePartsCost(calcCostInput , "_", "_");
		VFPartCostCalcResponse response = reportService.calculatePartsCost(partList, "_", "_");
		for (Map.Entry<String, VFPartCost> entry : response.result.entrySet()) {
			VFPartCost costInfo = entry.getValue();
			String partID = entry.getKey();
			partCostMap.put(partID, costInfo);
		}
		return partCostMap;
	}

	public static TCComponentBOMLine getTopBom(TCComponentBOMLine bomLine) {
		TCComponentBOMLine top = null;
		try {
			TCComponent parent = bomLine.getReferenceProperty("bl_parent");
			if (parent instanceof TCComponentBOPLine) {
				TCComponentBOMLine bop_line_parent = (TCComponentBOMLine) parent;
				if (bop_line_parent != null) {
					String level = bop_line_parent.getPropertyDisplayableValue("bl_level_starting_0");
					if (level.equals("0")) {
						top = bop_line_parent;
					} else {
						return getTopBom(bop_line_parent);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return top;
	}

	public static TCComponentBOMLine getParentBom(TCComponentBOMLine bomLine, int levelFind) {
		TCComponentBOMLine top = null;
		try {
			TCComponent parent = bomLine.getReferenceProperty("bl_parent");
			if (parent instanceof TCComponentBOPLine) {
				TCComponentBOMLine bop_line_parent = (TCComponentBOMLine) parent;
				if (bop_line_parent != null) {
					String level = bop_line_parent.getPropertyDisplayableValue("bl_level_starting_0");
					if (level.compareTo(String.valueOf(levelFind)) == 0) {
						top = bop_line_parent;
					} else {
						return getParentBom(bop_line_parent, levelFind);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return top;
	}

	public static String hanlderServiceData(ServiceData sd) {
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

	public static String hanlderServiceData(com.teamcenter.soa.client.model.ServiceData sd) {
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

	public static File downloadDataset(String outputPath, TCComponentDataset dataset, String type, String fileName, TCSession session) {
		File downloadFile = null;
		try {
			FileManagementService fmService = FileManagementService.getService(session);
			FileManagementUtility fileUtility = new FileManagementUtility(session.getSoaConnection());
			String datasetType = dataset.getType();
			if (datasetType.equals(type)) {
				TCComponentTcFile[] files = dataset.getTcFiles();
				if (files.length != 0) {
					FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);
					Map<TCComponentTcFile, String> map = ticketResp.tickets;
					for (TCComponentTcFile tcFile : map.keySet()) {
						if (fileName.isEmpty())
							fileName = tcFile.getProperty("original_file_name");
						String ticket = map.get(tcFile);
						downloadFile = fileUtility.getTransientFile(ticket, outputPath + fileName);
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return downloadFile;
	}

	public static TCComponentDataset createDataset(String datasetName, String datasetType, File fileToUpload, TCSession session) throws TCException {
		NamedReferenceContext namedReferenceContext[] = null;
		String namedRef = null;
		TCComponentDataset createdDataset = null;

		TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
		TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
		namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
		namedRef = namedReferenceContext[0].getNamedReference();
		String[] type = { namedRef };
		TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
		// Create the Dataset with given name and description. create( datasetName,
		// datasetDesc, dataset_id, dataset_rev, datasetType, toolUsed );
		createdDataset = datasetTypeComponent.create(datasetName, "", datasetDefinition.toString());
		createdDataset.refresh();
		String fileName = fileToUpload.getAbsolutePath();
		// import selected file on newly created dataset with the Specification relation
		createdDataset.setFiles(new String[] { fileName }, type);
		// attach dataset to target element with IMAN_specification */
		return createdDataset;
	}

	public static LinkedHashMap<String, String> getAttributeMap(String objectType, TCSession session) throws TCException, NotLoadedException {
		LinkedHashMap<String, String> attributeMap = new LinkedHashMap<String, String>();
		Connection connection = session.getSoaConnection();
		ClientMetaModel metaModel = connection.getClientMetaModel();
		List<Type> types = metaModel.getTypes(new String[] { objectType }, connection);

		for (Type type : types) {
			Hashtable<String, PropertyDescription> propDescs = type.getPropDescs();
			for (String propName : new TreeSet<String>(propDescs.keySet())) {
				PropertyDescription propDesc = propDescs.get(propName);
				attributeMap.put(propName, propDesc.getUiName());
			}
		}
		return attributeMap;
	}

	public static boolean checkUserHasGroup(String[] groupNames, TCSession session) {
		try {
			SessionService sessionServvice = SessionService.getService(session);
			GetGroupMembershipResponse groupMembership = sessionServvice.getGroupMembership();
			ServiceData sd = groupMembership.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				System.out.println(hanlderServiceData(sd));
			} else {
				for (TCComponentGroupMember groupMember : groupMembership.groupMembers) {
					TCComponentGroup group = groupMember.getGroup();
					for (String groupName : groupNames) {
						if (group.toString().compareToIgnoreCase(groupName) == 0) {
							return true;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean checkUserHasRole(String[] roleNames, TCSession session) {
		try {
			SessionService sessionServvice = SessionService.getService(session);
			GetGroupMembershipResponse groupMembership = sessionServvice.getGroupMembership();
			ServiceData sd = groupMembership.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				System.out.println(hanlderServiceData(sd));
			} else {
				for (TCComponentGroupMember groupMember : groupMembership.groupMembers) {
					TCComponentRole role = groupMember.getRole();
					for (String roleName : roleNames) {
						if (role.toString().compareToIgnoreCase(roleName) == 0) {
							return true;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void setStringArrayProperty(TCComponent component, String propertyName, String[] arrayValue) throws Exception {
		TCProperty property = component.getTCProperty(propertyName);
		if (property != null) {
			property.setStringValueArray(arrayValue);

		}
	}

	public static String getDescriptionFromLOVValue(String valueSearch, String property, String objectType, TCSession session) throws TCException {
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
			String[] realVal = valuesInfo.getStringListOfValues();
			String[] disval = valuesInfo.getDescriptions();
			int i = 0;
			for (String value : realVal) {
				if (value.compareTo(valueSearch) == 0) {
					return disval[i];
				}
				i++;
			}
		}

		return "";
	}

	public static String updatePreference(TCSession session, String preferenceName, String[] preferenceValue) {
		SetPreferences2In prefeItem = new SetPreferences2In();
		prefeItem.preferenceName = preferenceName;
		prefeItem.values = preferenceValue;
		PreferenceManagementService prefService = PreferenceManagementService.getService(session);
		ServiceData response = prefService.setPreferences2(new SetPreferences2In[] { prefeItem });
		if (response.sizeOfPartialErrors() > 0) {
			return hanlderServiceData(response);
		}

		return "";
	}

	public static TCComponentBOMLine openBOMStructure(TCComponentItemRevision itemRev, TCComponentRevisionRule revisionRule, TCSession session) throws Exception {
		CreateBOMWindowsInfo bomWinInfo150 = new CreateBOMWindowsInfo();
		bomWinInfo150.item = itemRev.getItem();
		bomWinInfo150.itemRev = itemRev;
//        bomWinInfo150.bomView = (PSBOMView) bomViews[0];
		bomWinInfo150.revRuleConfigInfo = new RevisionRuleConfigInfo();
		bomWinInfo150.revRuleConfigInfo.revRule = revisionRule;

		CreateBOMWindowsResponse response = StructureManagementService.getService(session).createBOMWindows(new CreateBOMWindowsInfo[] { bomWinInfo150 });
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			return null;
		}

		return response.output[0].bomLine;
	}

	public static TCComponentRevisionRule getRevisionRule(String revisionRuleName, TCSession session) {
		try {
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			for (RevisionRuleInfo rule : RuleInfo) {
				if (rule.revRule.getProperty("object_name").compareTo(revisionRuleName) == 0) {
					return rule.revRule;
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static LinkedList<TCComponentBOMLine> arrangementBomLine(LinkedList<TCComponentBOMLine> blExpand) throws Exception {
		Integer[] posOfLevel = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		LinkedList<TCComponentBOMLine> sortingExpand = new LinkedList<TCComponentBOMLine>();
		int targetLevel = 0;
		for (TCComponentBOMLine tcBOMLine : blExpand) {
			Integer currentLevel = Integer.parseInt(tcBOMLine.getPropertyDisplayableValue("bl_level_starting_0").toString());

			if (currentLevel < targetLevel) {
				sortingExpand.add(posOfLevel[currentLevel], tcBOMLine);
			} else if (currentLevel > targetLevel) {
				sortingExpand.add(tcBOMLine);
				for (int j = currentLevel; j > targetLevel; j--) {
					posOfLevel[j - 1] = sortingExpand.size() - 1;
				}
			} else {
				sortingExpand.add(tcBOMLine);
			}

			targetLevel = currentLevel;
		}

		return sortingExpand;
	}

	public static TCComponent[] queryItem(TCSession session, LinkedHashMap<String, String> inputQuery, String queryName) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = new String[] { queryName };
			qurey.queryDescs = new String[] { "" };
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			qc.query = (TCComponentQuery) result[0];

			if (inputQuery != null) {
				qc.entries = new String[inputQuery.size()];
				qc.values = new String[inputQuery.size()];
				int i = 0;
				for (Map.Entry<String, String> entry : inputQuery.entrySet()) {
					qc.entries[i] = entry.getKey();
					qc.values[i] = entry.getValue();
					i++;
				}
			}

			SavedQueryInput qc_v[] = new SavedQueryInput[1];
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);
			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			} else {
				System.out.println("[WARNING] NO OBJECT FOUND");
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return objects;
	}

	public static OpenContextInfo[] createContextViews(TCSession session, TCComponent object) {
		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponentRevisionRule RevisionRule = getRevisionRule("Working; Any Status", session);
		CreateInput input = new CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;
		if (sd.sizeOfPartialErrors() > 0) {
			SoaUtil.buildErrorMessage(sd);
		} else {
			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;
	}

	public static OpenContextInfo[] createContextViews(TCSession session, TCComponent object, String rule) {
		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponentRevisionRule RevisionRule = getRevisionRule(rule, session);
		CreateInput input = new CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;

		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 1; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}
		} else {
			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;
	}

	public static void closeContext(TCSession session, TCComponent window) {
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		dmService.closeContexts(new TCComponent[] { window });
	}

	public static TCComponent[] searchStruture(TCSession session, String searchObjects, TCComponent searchScope) {
		StructureSearchInMPPInput input = new StructureSearchInMPPInput();
		input.queryName = "Item...";
		input.queryCriteria = new String[] { "Item ID" };
		input.queryCriteriaValues = new String[] { searchObjects };
		input.searchScope = (TCComponentBOMLine) searchScope;
		IntegrationService sv = IntegrationService.getService(session);
		StructureSearchInMPPOutput output = sv.structureSearchInMPP(input);
		if (output.errorString.isEmpty()) {
			return output.foundLines;
		}
		return null;
	}

	public static void createFolder(String path) {
		File temp_Folder_U = new File(path);
		if (!temp_Folder_U.exists() && !temp_Folder_U.isDirectory()) {
			if (temp_Folder_U.mkdir()) {
				System.out.println("Create log directory success!");
			} else {
				System.out.println("Failed to create log directory!");
			}
		}
	}

	public static String generateItemID(String objectType, String pattern, DataManagementService dmService) {
		try {
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = objectType;
			nextID.pattern = pattern;

			GetNextIdsResponse IDReponse = dmService.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;
			return ids[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static LinkedHashMap<String, LinkedHashMap<String, Set<TCComponentGroupMember>>> getScooterReviewerList(TCSession session) {
		LinkedHashMap<String, LinkedHashMap<String, Set<TCComponentGroupMember>>> reviewerMapping = new LinkedHashMap<String, LinkedHashMap<String, Set<TCComponentGroupMember>>>();
		String[] preValues = TCExtension.GetPreferenceValues("VF_ESCOOTER_PROCESS_ASSIGNMENT_LIST", session);
		if (preValues != null) {
			int i = 0;
			LinkedList<String> gateList = new LinkedList<String>();
			for (String preValue : preValues) {
				if (i == 0) {
					for (String gate : preValue.split(";")) {
						if (!gate.isEmpty())
							gateList.add(gate);
					}
				} else {
					String[] str = preValue.split("=");
					if (str.length >= 2) {
						String program = str[0];
						String reviewerAllGate = str[1];
						LinkedHashMap<String, Set<TCComponentGroupMember>> gateMapping = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
						int j = 0;
						for (String reviewerEachGate : reviewerAllGate.split(";")) {
							if (!reviewerEachGate.isEmpty()) {
								Set<TCComponentGroupMember> reviewerList = new HashSet<TCComponentGroupMember>();
								for (String reviewer : reviewerEachGate.split(",")) {
									TCComponentGroupMember member = getGroupMember(reviewer, session);
									if (member != null) {
										reviewerList.add(member);
									}
								}
								gateMapping.put(gateList.get(j), reviewerList);
							}
							j++;
						}

						reviewerMapping.put(program, gateMapping);
					}
				}
				i++;
			}
		}

		return reviewerMapping;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, ALModel>> getProgramMapping(String workflowName, Set<String> programAvailable, TCSession session) {
		LinkedHashMap<String, LinkedHashMap<String, ALModel>> programMapping = new LinkedHashMap<>();

		try {
			Set<TCComponent> taskList = getSubTaskList(workflowName, session);

			TCComponent[] alList = getALByWorkflow(workflowName, session);
			DataManagementService dmsService = DataManagementService.getService(session);
			dmsService.getProperties(alList, new String[] { "object_string", "list_desc", "resources", "task_templates" });
			for (TCComponent alItem : alList) {
				if (alItem instanceof TCComponentAssignmentList) {
					ALModel alModelItem = new ALModel((TCComponentAssignmentList) alItem, taskList);
					String alName = alModelItem.getALName();
					String alNameProcess = alName.replace(workflowName + "_", "");
					String alDesc = alModelItem.getALDesc();
					String[] str = alNameProcess.split("_");
					if (str.length >= 2) {
						String program = str[0];
						String module = alNameProcess.replace(program + "_", "");

						if (!programAvailable.contains(program)) {
							if (!alDesc.isEmpty()) {
								String[] programList = alDesc.split(";");
								for (String programTemp : programList) {
									if (programMapping.containsKey(programTemp)) {
										if (!programMapping.get(programTemp).containsKey(module)) {
											programMapping.get(programTemp).put(module, alModelItem);
										}
									} else {
										LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
										moduleMapping.put(module, alModelItem);
										programMapping.put(programTemp, moduleMapping);
									}
								}
							}
						} else {
							if (programMapping.containsKey(program)) {
								programMapping.get(program).put(module, alModelItem);
							} else {
								LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
								moduleMapping.put(module, alModelItem);
								programMapping.put(program, moduleMapping);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return programMapping;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, ALModel>> getScooterProgramMapping(String workflowName, Set<String> programAvailable, Boolean getPreference, TCSession session) {
		LinkedHashMap<String, LinkedHashMap<String, ALModel>> programMapping = new LinkedHashMap<>();

		try {
			Set<TCComponent> taskList = getSubTaskList(workflowName, session);
			TCComponent[] alList = getALByWorkflow(workflowName, session);
			DataManagementService dmsService = DataManagementService.getService(session);
			dmsService.getProperties(alList, new String[] { "object_string", "list_desc", "resources", "task_templates" });
			for (TCComponent alItem : alList) {
				if (alItem instanceof TCComponentAssignmentList) {
					String alName = alItem.getProperty("object_string");
					String alNameProcess = alName.replace(workflowName + "_", "");
					String alDesc = alItem.getProperty("list_desc");
					String[] str = alNameProcess.split("_");
					if (str.length >= 2) {
						String program = str[0];
						String module = alNameProcess.replace(program + "_", "");

						if (programAvailable == null) {
							if (program.compareTo("Common") == 0) {
								if (!alDesc.isEmpty()) {
									String[] programList = alDesc.split(";");
									for (String programTemp : programList) {
										if (programMapping.containsKey(programTemp)) {
											if (!programMapping.get(programTemp).containsKey(module)) {
												programMapping.get(programTemp).put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
											}
										} else {
											LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
											moduleMapping.put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
											programMapping.put(programTemp, moduleMapping);
										}
									}
								}
							} else {
								if (programMapping.containsKey(program)) {
									programMapping.get(program).put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
								} else {
									LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
									moduleMapping.put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
									programMapping.put(program, moduleMapping);
								}
							}
						} else {
							if (!programAvailable.contains(program)) {
								if (!alDesc.isEmpty()) {
									String[] programList = alDesc.split(";");
									for (String programTemp : programList) {
										if (programMapping.containsKey(programTemp)) {
											if (!programMapping.get(programTemp).containsKey(module)) {
												programMapping.get(programTemp).put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
											}
										} else {
											LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
											moduleMapping.put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
											programMapping.put(programTemp, moduleMapping);
										}
									}
								}
							} else {
								if (programMapping.containsKey(program)) {
									programMapping.get(program).put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
								} else {
									LinkedHashMap<String, ALModel> moduleMapping = new LinkedHashMap<String, ALModel>();
									moduleMapping.put(module, new ALModel((TCComponentAssignmentList) alItem, taskList));
									programMapping.put(program, moduleMapping);
								}
							}
						}
					}
				}
			}

			if (getPreference) {
				LinkedHashMap<String, LinkedHashMap<String, Set<TCComponentGroupMember>>> reviewerMapping = getScooterReviewerList(session);
				for (Map.Entry<String, LinkedHashMap<String, Set<TCComponentGroupMember>>> reviewerMappingEntrySet : reviewerMapping.entrySet()) {
					String program = reviewerMappingEntrySet.getKey();
					LinkedHashMap<String, Set<TCComponentGroupMember>> reviewerEachGate = reviewerMappingEntrySet.getValue();

					if (reviewerEachGate.size() > 0) {
						if (programMapping.containsKey(program)) {
							for (Map.Entry<String, Set<TCComponentGroupMember>> reviewerEachGateEntrySet : reviewerEachGate.entrySet()) {
								String gate = reviewerEachGateEntrySet.getKey();
								Set<TCComponentGroupMember> reviewer = reviewerEachGateEntrySet.getValue();
								if (reviewer.size() > 0) {
									LinkedHashMap<String, ALModel> moduleMapping = programMapping.get(program);
									for (ALModel module : moduleMapping.values()) {
										module.addReviewer(gate, reviewer);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return programMapping;
	}

	public static TCComponentGroupMember getGroupMember(String groupMember, TCSession session) {
		String[] str = groupMember.split("/");
		if (str.length < 3)
			return null;

		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		queryInput.put("Group", str[0]);
		queryInput.put("Role", str[1]);
		queryInput.put("Id", str[2]);
		queryInput.put("Group Member Status", "FALSE");
		TCComponent[] queryOutput = TCExtension.queryItem(session, queryInput, "__TNH_FindGroupMem");
		if (queryOutput != null && queryOutput.length > 0)
			return (TCComponentGroupMember) queryOutput[0];

		return null;
	}

	public static Set<TCComponent> getSubTaskList(String workflowName, TCSession session) {
		Set<TCComponent> taskList = new HashSet<TCComponent>();
		try {
			TCComponentTaskTemplate workflow = GetWorkflowByName(workflowName, session);
			if (workflow != null) {
				TCComponent[] subTasks = workflow.getSubtaskDefinitions();
				if (subTasks != null && subTasks.length > 0) {
					for (TCComponent subTask : subTasks) {
						if (subTask instanceof TCComponentTaskTemplate) {
							taskList.add(subTask);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return taskList;
	}

	public static TCComponentItemRevision getLatestItemRevision(TCComponentItem item) {
		try {
			TCComponentItemRevision lastestRevision = null;
			TCComponentItemRevision[] releaseItemRevisions = item.getReleasedItemRevisions();
			if (releaseItemRevisions != null) {
				for (TCComponentItemRevision itemRevision : releaseItemRevisions) {
					if (itemRevision.getPropertyDisplayableValue("item_revision_id").contains("."))
						continue;
					if (lastestRevision == null) {
						lastestRevision = itemRevision;
					} else {
						String revNumberLatest = lastestRevision.getPropertyDisplayableValue("item_revision_id");
						String revNumber = itemRevision.getPropertyDisplayableValue("item_revision_id");
						if (StringExtension.isInteger(revNumber, 10) && StringExtension.isInteger(revNumberLatest, 10)) {
							if (Integer.parseInt(revNumber) > Integer.parseInt(revNumberLatest)) {
								lastestRevision = itemRevision;
							}
						}
					}
				}
			}
			TCComponentItemRevision[] workItemRevisions = item.getWorkingItemRevisions();
			if (workItemRevisions != null) {
				for (TCComponentItemRevision itemRevision : workItemRevisions) {
					if (itemRevision.getPropertyDisplayableValue("item_revision_id").contains("."))
						continue;
					if (lastestRevision == null) {
						lastestRevision = itemRevision;
					} else {
						String revNumberLatest = lastestRevision.getPropertyDisplayableValue("item_revision_id");
						String revNumber = itemRevision.getPropertyDisplayableValue("item_revision_id");
						if (StringExtension.isInteger(revNumber, 10) && StringExtension.isInteger(revNumberLatest, 10)) {
							if (Integer.parseInt(revNumber) > Integer.parseInt(revNumberLatest)) {
								lastestRevision = itemRevision;
							}
						}
					}
				}
			}

			return lastestRevision;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponent getPersonFromUserID(String userID, TCSession session) {
		if (!userID.isEmpty()) {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("User ID", userID);

			TCComponent[] queryOutput = queryItem(session, queryInput, "__TNH_Find_User");
			if (queryOutput != null && queryOutput.length > 0) {
				try {
					return queryOutput[0].getRelatedComponent("person");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
