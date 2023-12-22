package com.teamcenter.integration.ulti;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.commands.open.OpenCommand;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
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
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSAllLevelsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetVariantRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.rac.core._2006_03.Session.GetSessionGroupMemberResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.rac.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.services.rac.structuremanagement.VariantManagementService;
import com.teamcenter.services.rac.structuremanagement._2013_05.VariantManagement.BOMVariantOptionValueEntry;
import com.teamcenter.services.rac.structuremanagement._2019_06.VariantManagement.BOMVariantRuleContents2;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;
import com.vinfast.sap.util.PropertyDefines;

public class TCExtension {
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

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

	public static String[] GetLovValues(String property, String objectType, TCSession session) throws TCException {
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
		ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
		String[] output = valuesInfo.getLOVDisplayValues();
		Arrays.sort(output, String.CASE_INSENSITIVE_ORDER);
		return output;
	}

	public static String[] GetLovValues(String lovName) throws TCException {
		String[] output = {};
		TCComponentListOfValues color_spec = TCComponentListOfValuesType.findLOVByName(lovName);
		ListOfValuesInfo color_spec_lov = color_spec.getListOfValues();
		output = color_spec_lov.getLOVDisplayValues();
		Arrays.sort(output, String.CASE_INSENSITIVE_ORDER);
		return output;
	}

	public static LinkedHashMap<String, String> GetLovValueAndDisplayInterdependent(String property, String objectType, String[] parentValue, TCSession session) throws TCException {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		if (propertyDescriptor.hasLOVAttached()) {
			TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
			ListOfValuesInfo parentInfo = attachedLOV.getListOfValues();

			GetLOVByLevel(0, parentValue, parentInfo, output);

			return SortingLOV(output);
		} else {
			return null;
		}
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

	public static Map<String, String> GetLOVList(String property, String objectType, TCSession session) throws TCException {
		TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
		TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
		ListOfValuesInfo valuesInfo = attachedLOV.getListOfValues();
		Map<String, String> output = new HashMap<String, String>();
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
		PreferenceManagementService preferenceService = PreferenceManagementService.getService(session);
		GetPreferencesResponse prefReponse = preferenceService.refreshPreferences2(new String[] { prefName }, false);
		CompletePreference[] pref = prefReponse.response;

		if (pref.length != 0) {
			PreferenceValue values = pref[0].values;
			IP_values = values.values;
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

	public static LinkedList<TCComponentBOMLine> ExpandAllBOMLines(final TCComponentBOMLine rootLine, TCSession conn) {
		LinkedList<TCComponentBOMLine> outBomLines = new LinkedList<TCComponentBOMLine>();
		outBomLines.add(rootLine);
		StructureManagementService structureService = StructureManagementService.getService(conn);
		ExpandPSAllLevelsInfo levelInfo = new ExpandPSAllLevelsInfo();
		ExpandPSAllLevelsPref levelPref = new ExpandPSAllLevelsPref();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];
		ExpandPSAllLevelsResponse levelResp = structureService.expandPSAllLevels(levelInfo, levelPref);

		if (levelResp.output.length > 0) {
			for (ExpandPSAllLevelsOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					outBomLines.add(psData.bomLine);
				}
			}
		}

		return outBomLines;
	}

	public static LinkedList<TCComponentBOMLine> expandOneLevelBOMLines(StructureManagementService SMService, TCComponentBOMLine bomLine) {
		LinkedList<TCComponentBOMLine> outBomLines = new LinkedList<TCComponentBOMLine>();
		ExpandPSOneLevelInfo oneLevelInfo = new ExpandPSOneLevelInfo();
		oneLevelInfo.parentBomLines = new TCComponentBOMLine[] { bomLine };
		oneLevelInfo.excludeFilter = "None";

		ExpandPSOneLevelPref pref = new ExpandPSOneLevelPref();
		pref.expItemRev = false;
		pref.info = new RelationAndTypesFilter[0];

		ExpandPSOneLevelResponse levelResp = SMService.expandPSOneLevel(oneLevelInfo, pref);
		if (levelResp.output.length > 0) {
			for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					outBomLines.add(psData.bomLine);
				}
			}
		}

		return outBomLines;
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

	public static List<TCComponent> GetReferenced(TCComponent selectedObject, TCSession session) {
		List<TCComponent> resultList = new LinkedList<TCComponent>();
		DataManagementService dm = DataManagementService.getService(session);
		TCComponent[] list = new TCComponent[1];
		list[0] = selectedObject;
		WhereReferencedResponse response1 = dm.whereReferenced(list, 1);
		if (response1 != null) {
			for (WhereReferencedOutput output : response1.output) {
				WhereReferencedInfo[] refs = output.info;
				for (WhereReferencedInfo info : refs) {
					resultList.add(info.referencer);
				}
			}
		}
		return resultList;
	}

	public static OpenContextInfo[] CreateContextViews(TCComponent object, TCSession session) {
		OpenContextInfo[] info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);

		TCComponentRevisionRule RevisionRule = GetRevisionRule("VINFAST_WORKING_RULE", session);
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

	public static String GetPropertyRealValue(TCComponent item, String propertyName) throws TCException {
		TCProperty moduleGroupProperty = item.getTCProperty(propertyName);
		if (moduleGroupProperty != null)
			return moduleGroupProperty.getStringValue();
		return "";
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

	public static boolean checkPermissionAccess(TCComponent item, String permission, TCSession session) throws Exception {
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

	public static TCComponentBOMLine getTopBom(TCComponentBOMLine bomLine) {
		TCComponentBOMLine top = null;
		try {
			TCComponent parent = bomLine.getReferenceProperty("bl_parent");
			if (parent == null)
				return bomLine;
			if (parent instanceof TCComponentBOMLine) {
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

	public static FoundNodesInfo[] findPartInBOP(TCComponent target, HashMap<TCComponent, TCComponent[]> parentChildLines, TCSession session) throws Exception {
		int iterator = 0;
		CoreService structService = CoreService.getService(session);
		FindNodeInContextInputInfo[] findNodeInputInfo = new FindNodeInContextInputInfo[parentChildLines.size()];
		HashMap<String, TCComponent> isList = new HashMap<String, TCComponent>();
		for (TCComponent parentLine : parentChildLines.keySet()) {
			String parentID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
			parentID = parentID.substring(0, parentID.indexOf("-"));
			isList.put(parentID, parentLine);

			TCComponent[] childLines = parentChildLines.get(parentLine);
			String clientID = parentID;
			findNodeInputInfo[iterator] = new FindNodeInContextInputInfo();
			findNodeInputInfo[iterator].clientID = clientID;
			findNodeInputInfo[iterator].context = target;
			findNodeInputInfo[iterator].nodes = childLines;
			findNodeInputInfo[iterator].allContexts = false;
			findNodeInputInfo[iterator].byIdOnly = true;
			findNodeInputInfo[iterator].relationDepth = 0;
			findNodeInputInfo[iterator].relationDirection = 1;
			findNodeInputInfo[iterator].relationTypes = new String[] { "FND_TraceLink" };
			iterator++;
		}

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(findNodeInputInfo);
		ServiceData serviceData = InContextInputResponse.serviceData;
		if (serviceData.sizeOfPartialErrors() == 0) {
			return InContextInputResponse.resultInfo;
		}

		return null;
	}

	public static NodeInfo[] findBOMLineInBOP(TCComponentBOMLine targetBomLine, TCComponentBOMLine[] selected_Objects, TCSession session) throws Exception {
		CoreService structService = CoreService.getService(session);
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		ServiceData sd = InContextInputResponse.serviceData;

		if (sd.sizeOfPartialErrors() == 0) {
			FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;
			return nodeInfo[0].resultNodes;
		}

		return null;
	}

	public static TCComponent[] queryItem(TCSession session, LinkedHashMap<String, String> inputQuery, String queryName) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[inputQuery.size()];
			qc.values = new String[inputQuery.size()];
			int i = 0;
			for (Map.Entry<String, String> entry : inputQuery.entrySet()) {
				qc.entries[i] = entry.getKey();
				qc.values[i] = entry.getValue();
				i++;
			}
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

	public static void applyVariantRule(TCComponentSavedVariantRule variantRule, TCComponentBOMWindow window, TCSession session) {
		try {
			VariantManagementService VMService = VariantManagementService.getService(session);
			BOMVariantRuleContents2 content = new BOMVariantRuleContents2();
			content.svr = variantRule;
			content.variantRule = null;
			content.isSVRModified = false;
			content.bomVariantOptionValueEntry = new BOMVariantOptionValueEntry[] {};

			VMService.applyBOMVariantRules2(window, new BOMVariantRuleContents2[] { content });
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	public static TCComponentVariantRule getSavedVariant(TCComponentItemRevision topLine, String requiredVariantName, TCSession session) {
		TCComponentVariantRule savedVariantRule = null;
		if (topLine == null)
			return null;

		try {
			GetVariantRulesResponse getVarResp = StructureManagementService.getService(session).getVariantRules(new TCComponentItemRevision[] { topLine });
			if (getVarResp.serviceData.sizeOfPartialErrors() > 0) {
				return null;
			}

			TCComponentVariantRule[] varRulesArr = (TCComponentVariantRule[]) getVarResp.inputItemRevToVarRules.get(topLine);
			for (int inx = 0; inx < varRulesArr.length; inx++) {
				String vRuleName = varRulesArr[inx].getPropertyDisplayableValue("object_name");
				if (requiredVariantName.contains(vRuleName)) {
					return varRulesArr[inx];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return savedVariantRule;
	}

	public static void copyToClipboard(String input) {
		StringSelection stringSelection = new StringSelection(input);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
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

	public static String getCurrentTime() {
		return dtf.format(LocalDateTime.now());
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
}
