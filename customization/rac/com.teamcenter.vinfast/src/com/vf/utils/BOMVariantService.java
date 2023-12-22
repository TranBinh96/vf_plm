package com.vf.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.vinfast.model.FeatureModel;

public class BOMVariantService {
	private String configurator;
	private TCSession session;
	private DataManagementService dmService;
	private static LinkedHashMap<String, LinkedHashMap<String, Set<String>>> variantRuleMatrix = null;
	private static Set<String> allFamilyOfContextMatrix = null;
	private ScriptEngine engine = null;
	private LinkedList<Set<String>> postOfVariant;

	public BOMVariantService(TCSession session, String configurator) {
		this.configurator = configurator;
		this.session = session;
		dmService = DataManagementService.getService(session);
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("JavaScript");

		loadVariantRuleMatrix();

		postOfVariant.add(variantRuleMatrix.keySet());
	}

	public Set<String> getVariantList(int currentLevel, String formulaText) {
		Set<String> dataReturn = new HashSet<String>();
		Set<String> currentVariantCheck = postOfVariant.get(currentLevel - 1);
		if (!formulaText.isEmpty()) {
			if (currentVariantCheck == null)
				currentVariantCheck = variantRuleMatrix.keySet();

			dataReturn = variantCheckExist(formulaText, currentVariantCheck);
		} else {
			dataReturn = currentVariantCheck;
		}

		if (postOfVariant.size() <= currentLevel)
			postOfVariant.add(dataReturn);
		else
			postOfVariant.set(currentLevel, dataReturn);
		
		return dataReturn;
	}

	private void loadVariantRuleMatrix() {
		postOfVariant = new LinkedList<Set<String>>();
		variantRuleMatrix = new LinkedHashMap<>();
		allFamilyOfContextMatrix = new HashSet<>();

		LinkedHashMap<String, TCComponent> variantRuleList = new LinkedHashMap<>();

		try {
			LinkedHashMap<String, String> queryInput1 = new LinkedHashMap<>();
			queryInput1.put("ID", configurator);
			TCComponent[] queryOutput1 = TCExtension.queryItem(session, queryInput1, "__Cfg0OptionValuesFromValueIDs");
			if (queryOutput1.length > 0) {
				dmService.getProperties(queryOutput1, new String[] { "current_name", "cfg0ObjectId" });
				for (TCComponent object : queryOutput1) {
					allFamilyOfContextMatrix.add(object.getPropertyDisplayableValue("current_name"));
				}
			}

			LinkedHashMap<String, String> queryInput2 = new LinkedHashMap<>();
			queryInput2.put("Item ID", configurator);
			queryInput2.put("Type", "Configurator Context");
			TCComponent[] queryOutput2 = TCExtension.queryItem(session, queryInput2, "Item...");
			if (queryOutput2.length == 0)
				return;

			dmService.getProperties(queryOutput2, new String[] { "IMAN_reference" });
			TCComponent[] variantList = queryOutput2[0].getReferenceListProperty("IMAN_reference");
			if (variantList == null || variantList.length == 0)
				return;

			dmService.getProperties(variantList, new String[] { "current_name", "cfg0VariantRuleText" });
			for (TCComponent variantRule : variantList) {
				String variantRuleName = variantRule.getPropertyDisplayableValue("current_name");
				variantRuleList.put(variantRuleName, variantRule);
			}

			for (Map.Entry<String, TCComponent> variant : variantRuleList.entrySet()) {
				LinkedHashMap<String, Set<String>> familyList = new LinkedHashMap<>();

				String ruleText = variant.getValue().getPropertyDisplayableValue("cfg0VariantRuleText").replace(" ", "");
				if (!ruleText.isEmpty()) {
					if (ruleText.contains("Any"))
						ruleText = convertAnyToFamilies(ruleText);

					Set<String> familyTempList = new HashSet<>();
					if (ruleText.contains("AND")) {
						familyTempList.addAll(Arrays.asList(ruleText.split("AND")));
					} else {
						familyTempList.add(ruleText);
					}

					for (String family : familyTempList) {
						Set<String> featureTempList = new HashSet<>();
						if (family.contains("OR")) {
							featureTempList.addAll(Arrays.asList(family.split("OR")));
						} else {
							featureTempList.add(family);
						}

						for (String feature : featureTempList) {
							FeatureModel model = getFeature(feature);
							if (model != null) {
								if (familyList.containsKey(model.getFamilyName())) {
									familyList.get(model.getFamilyName()).add(model.getLogical() + model.getFeatureName());
								} else {
									familyList.put(model.getFamilyName(), new HashSet<>(List.of(model.getLogical() + model.getFeatureName())));
								}
							}
						}
					}
				}

				variantRuleMatrix.put(variant.getKey(), familyList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String convertAnyToFamilies(String input) {
		try {
			FeatureModel model = getFeature(input);
			if (model != null) {
				LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
				queryInput.put("Name", model.getFamilyName());
				TCComponent[] objectQuery = TCExtension.queryItem(session, queryInput, "__Cfg0OptionValuesFromOptionFamilyNames");
				if (objectQuery.length > 0) {
					dmService.getProperties(objectQuery, new String[] { "cfg0ObjectId" });
					LinkedList<String> featureList = new LinkedList<>();
					for (TCComponent feature : objectQuery) {
						featureList.add(feature.getPropertyDisplayableValue("cfg0ObjectId").trim());
					}
					LinkedList<String> features = new LinkedList<>();
					for (String feature : featureList) {
						features.add(model.getPrefix() + model.getFamilyName() + model.getLogical() + feature);
					}

					StringBuilder output = new StringBuilder();
					if (features.size() > 1) {
						output.append("(");
						output.append(String.join((model.getLogical().compareTo("=") == 0 ? "OR" : "AND"), features));
						output.append(")");
					} else {
						output.append(features.get(0));
					}
					return output.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

	private FeatureModel getFeature(String input) {
		// [Teamcenter]B15 = B151
		input = input.replace("(", "").replace(")", "");
		String logical = input.contains("!=") ? "!=" : "=";
		if (input.contains(logical)) {
			String[] str = input.split(logical);
			if (str.length > 1) {
				if (str[0].contains("]")) {
					String[] str1 = str[0].split("]");
					if (str1.length > 1) {
						FeatureModel returnModel = new FeatureModel();
						returnModel.setPrefix(str1[0].replace(" ", "") + "]");
						returnModel.setFamilyName(str1[1].replace(" ", ""));
						returnModel.setFeatureName(str[1].replace(" ", ""));
						returnModel.setLogical(logical);
						return returnModel;
					}
				}
			}
		}

		return null;
	}

	private Set<String> variantCheckExist(String formulaText, Set<String> variantList) {
		Set<String> variantProcess = new HashSet<>();
		String processedFormulaText = preProcessFormulaText(formulaText);
		String processedFormulaTextClone = processedFormulaText.replace("(", "").replace(")", "").replace("AND", ";").replace("OR", ";");
		LinkedList<String> featureList = new LinkedList<>();
		if (processedFormulaTextClone.contains(";")) {
			Collections.addAll(featureList, processedFormulaTextClone.split(";"));
		} else {
			featureList.add(processedFormulaTextClone);
		}

		for (String variant : variantList) {
			if (variantRuleMatrix.containsKey(variant)) {
				LinkedHashMap<String, Set<String>> matrix = variantRuleMatrix.get(variant);
				String output = processedFormulaText;
				for (String feature : featureList) {
					if (!feature.trim().isEmpty()) {
						boolean check = featureCheckExist(feature, matrix);
						output = output.replace(feature.trim(), String.valueOf(check));
					}
				}

				if (evaluateBooleanString(output.replace("AND", "&&").replace("OR", "||").replace("NOT", "!"), formulaText))
					variantProcess.add(variant);
			}
		}

		return variantProcess;
	}

	private String preProcessFormulaText(String formulaText) {
		String output = formulaText;
		if (formulaText.contains("Any")) {
			output = formulaText.replace("(", "").replace(")", "").replace("AND", ";").replace("OR", ";");
			LinkedList<String> featureList = new LinkedList<>();
			if (output.contains(";")) {
				Collections.addAll(featureList, output.split(";"));
			} else {
				featureList.add(output);
			}

			for (String feature : featureList) {
				if (feature.contains("Any")) {
					String abc = convertAnyToFamilies(feature);
					formulaText = formulaText.replace(feature.trim(), abc);
				}
			}
		}

		return formulaText;
	}

	private boolean evaluateBooleanString(String boolString, String partNumber) {
		try {
			Object result = engine.eval(boolString);
			return (Boolean) result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean featureCheckExist(String feature, LinkedHashMap<String, Set<String>> matrix) {
		FeatureModel model = getFeature(feature);
		if (model != null) {
			Set<String> featureList = matrix.get(model.getFamilyName());
			if (featureList == null)
				return allFamilyOfContextMatrix.contains(model.getFeatureName());

			if (model.getLogical().compareTo("=") == 0)
				return featureList.contains(model.getLogical() + model.getFeatureName());
			else {
				if (featureList.contains("=" + model.getFeatureName())) {
					return featureList.size() != 1;
				}
				return true;
			}
		}
		return false;
	}
}
