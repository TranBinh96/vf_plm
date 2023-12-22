package com.vinfast.sap.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.configurator.ConfigManager;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.services.ReadXMLInputFile;
import com.vinfast.sap.util.EnumTypesUtil;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class RulesTransfer {
	public enum ActionType {
		ACTION_ADD, ACTION_DELETE
	}

	public LinkedHashMap<String, String> getAvailabilityRules(ActionType action, ConfigManager configManager, TCComponent AVRule) {
		LinkedHashMap<String, String> dataMap = null;
		try {
			String ruleID = AVRule.getPropertyDisplayableValue("cfg0ObjectId");
			String subCondition = AVRule.getPropertyDisplayableValue("cfg0SubjectCondition");
			String appCondition = AVRule.getPropertyDisplayableValue("cfg0ApplicabilityCondition");

			if (subCondition != null & appCondition != null) {
				dataMap = new LinkedHashMap<String, String>();
				dataMap.put("PLATFORM", configManager.getModel());
				dataMap.put("MODELYEAR", configManager.getYear());
				dataMap.put("PLANT", configManager.getPlant());
				dataMap.put("MCN", configManager.getMCN());
				if (action == ActionType.ACTION_ADD) {
					dataMap.put("ACTION", "A");
				} else {
					dataMap.put("ACTION", "D");
				}
				dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
				dataMap.put("RULETYPE", "AV");
				dataMap.put("RULEID", ruleID);
				Pattern p3 = Pattern.compile("\\[(.*?)\\]");
				Matcher m3 = p3.matcher(appCondition);
				while (m3.find()) {
					appCondition = appCondition.replace(m3.group(1), configManager.getModel() + "_" + configManager.getYear());
				}
				dataMap.put("SELECTIONRULES", appCondition);
				dataMap.put("FAMILYID", getFamilyOption(subCondition, 0).trim());
				dataMap.put("OPTIONID", getFamilyOption(subCondition, 1).trim());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return dataMap;
	}

	public LinkedHashMap<String, String> getDefaultRules(ActionType action, ConfigManager configManager, TCComponent DERule) {
		LinkedHashMap<String, String> dataMap = null;
		try {
			String ruleID = DERule.getPropertyDisplayableValue("cfg0ObjectId");
			String subCondition = DERule.getPropertyDisplayableValue("cfg0SubjectCondition");
			String appCondition = DERule.getPropertyDisplayableValue("cfg0ApplicabilityCondition");

			if (subCondition != null & appCondition != null) {
				dataMap = new LinkedHashMap<String, String>();
				dataMap.put("PLATFORM", configManager.getModel());
				dataMap.put("MODELYEAR", configManager.getYear());
				dataMap.put("PLANT", configManager.getPlant());
				dataMap.put("MCN", configManager.getMCN());
				if (action == ActionType.ACTION_ADD) {
					dataMap.put("ACTION", "A");
				} else {
					dataMap.put("ACTION", "D");
				}
				dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
				dataMap.put("RULETYPE", "DE");
				dataMap.put("RULEID", ruleID);
				Pattern p3 = Pattern.compile("\\[(.*?)\\]");
				Matcher m3 = p3.matcher(appCondition);
				while (m3.find()) {
					appCondition = appCondition.replace(m3.group(1), configManager.getModel() + "_" + configManager.getYear());
				}
				dataMap.put("SELECTIONRULES", appCondition);
				dataMap.put("FAMILYID", getFamilyOption(subCondition, 0).trim());
				dataMap.put("OPTIONID", getFamilyOption(subCondition, 1).trim());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return dataMap;
	}

	public ArrayList<LinkedHashMap<String, String>> getInclusionRules(ConfigManager configManager, TCComponent INRule, HashMap<String, HashSet<String>> preIncludeRuleMap) {
		ArrayList<LinkedHashMap<String, String>> allDataMap = new ArrayList<LinkedHashMap<String, String>>();
		HashMap<String, HashSet<String>> curIncludeRulesMap = new HashMap<String, HashSet<String>>();
		LinkedHashMap<String, String> dataMap = null;
		try {
			String ruleID = INRule.getPropertyDisplayableValue("cfg0ObjectId");
			String subCondition = INRule.getPropertyDisplayableValue("cfg0SubjectCondition").replace(" ", "");
			String appCondition = INRule.getPropertyDisplayableValue("cfg0ApplicabilityCondition");
			String key = String.format("%s~%s", ruleID, appCondition.replace(" ", ""));

			if (!preIncludeRuleMap.containsKey(key)) {
				// 100% new
				allDataMap = getNewIncludeRuleData(configManager, INRule, preIncludeRuleMap);
			} else {
				// something new & something old
				HashSet<String> oldRules = preIncludeRuleMap.get(key);
				HashSet<String> curRules = new HashSet<String>();
				String[] subject = subCondition.split("AND");
				for (String family : subject) {
					dataMap = new LinkedHashMap<String, String>();
					dataMap.put("PLATFORM", configManager.getModel());
					dataMap.put("MODELYEAR", configManager.getYear());
					dataMap.put("PLANT", configManager.getPlant());
					dataMap.put("MCN", configManager.getMCN());
					dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
					dataMap.put("RULETYPE", "IN");
					dataMap.put("RULEID", ruleID);
					Pattern p3 = Pattern.compile("\\[(.*?)\\]");
					Matcher m3 = p3.matcher(appCondition);
					while (m3.find()) {
						appCondition = appCondition.replace(m3.group(1), configManager.getModel() + "_" + configManager.getYear());
					}
					dataMap.put("SELECTIONRULES", appCondition);
					dataMap.put("FAMILYID", getFamilyOption(family, 0).trim());
					dataMap.put("OPTIONID", getFamilyOption(family, 1).trim());
					if (!oldRules.contains(family)) {
						dataMap.put("ACTION", "A");
						allDataMap.add(dataMap);
					}
					curRules.add(family);
				}
				curIncludeRulesMap.put(key, curRules);
				oldRules.removeAll(curRules);
				for (String del : oldRules) {
					dataMap = new LinkedHashMap<String, String>();
					dataMap.put("PLATFORM", configManager.getModel());
					dataMap.put("MODELYEAR", configManager.getYear());
					dataMap.put("PLANT", configManager.getPlant());
					dataMap.put("MCN", configManager.getMCN());
					dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
					dataMap.put("RULETYPE", "IN");
					dataMap.put("RULEID", ruleID);
					dataMap.put("SELECTIONRULES", appCondition);
					String family = del;
					dataMap.put("FAMILYID", getFamilyOption(family, 0).trim());
					dataMap.put("OPTIONID", getFamilyOption(family, 1).trim());
					dataMap.put("ACTION", "D");
					allDataMap.add(dataMap);
				}
				preIncludeRuleMap.put(key, curRules);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return allDataMap;

	}

	public ArrayList<LinkedHashMap<String, String>> getNewIncludeRuleData(ConfigManager configManager, TCComponent INRule, HashMap<String, HashSet<String>> preIncludeRuleMap) {
		ArrayList<LinkedHashMap<String, String>> allDataMap = null;
		HashSet<String> rules = new HashSet<String>();
		LinkedHashMap<String, String> dataMap = null;
		try {
			String ruleID = INRule.getPropertyDisplayableValue("cfg0ObjectId");
			String subCondition = INRule.getPropertyDisplayableValue("cfg0SubjectCondition").replace(" ", "");
			String appCondition = INRule.getPropertyDisplayableValue("cfg0ApplicabilityCondition");
			String key = String.format("%s~%s", ruleID, appCondition.replace(" ", ""));
			if (subCondition != null & appCondition != null) {
				allDataMap = new ArrayList<LinkedHashMap<String, String>>();
				String[] subject = subCondition.split("AND");
				for (String family : subject) {
					dataMap = new LinkedHashMap<String, String>();
					dataMap.put("PLATFORM", configManager.getModel());
					dataMap.put("MODELYEAR", configManager.getYear());
					dataMap.put("PLANT", configManager.getPlant());
					dataMap.put("MCN", configManager.getMCN());
					dataMap.put("ACTION", "A");
					dataMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
					dataMap.put("RULETYPE", "IN");
					dataMap.put("RULEID", ruleID);
					Pattern p3 = Pattern.compile("\\[(.*?)\\]");
					Matcher m3 = p3.matcher(appCondition);
					while (m3.find()) {
						appCondition = appCondition.replace(m3.group(1),
								configManager.getModel() + "_" + configManager.getYear());
					}
					dataMap.put("SELECTIONRULES", appCondition);
					dataMap.put("FAMILYID", getFamilyOption(family, 0).trim());
					dataMap.put("OPTIONID", getFamilyOption(family, 1).trim());
					allDataMap.add(dataMap);
					rules.add(family);
				}
				preIncludeRuleMap.put(key, rules);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return allDataMap;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, HashMap<String, String>> transferRules(int count, TCComponent rule,
			HashMap<String, String> dataMap, StringBuilder strBuilder) {

		new Logger();
		ReadXMLInputFile readFile = new ReadXMLInputFile();
		new UIGetValuesUtility();
		boolean toProcess = true;
		boolean isInclusion = false;

		HashMap<String, HashMap<String, String>> ruleMap = new HashMap<String, HashMap<String, String>>();

		try {
			String fileName = readFile.getXML(rule);

			if (fileName == null) {

				MessageBox.post("Parser file name unknown. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;

			} else {

				InputStream traverse_File = readFile.loadXML(fileName);

				if (traverse_File == null) {

					MessageBox.post("Property file parser error. Please contact Teamcenter Admin", "Error",
							MessageBox.ERROR);
					return null;
				}
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(traverse_File);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName(SAPURL.RUL_HEADER_TAG);

				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element tagElement = (Element) nNode;
						NodeList dataList = tagElement.getChildNodes();

						for (int jnx = 0; jnx < dataList.getLength(); jnx++) {
							Node dataNode = dataList.item(jnx);

							if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
								Element dataElement = (Element) dataNode;

								NodeList attributes = dataElement.getChildNodes();

								for (int knx = 0; knx < attributes.getLength(); knx++) {

									Node attribute = attributes.item(knx);

									if (attribute.getNodeType() == Node.ELEMENT_NODE) {
										Element attributeElement = (Element) attribute;

										String sap_tag = attributeElement.getAttribute("erp_tag");
										String plm_tag = attributeElement.getAttribute("plm_tag");
										String relation = attributeElement.getAttribute("relation");
										String is_mandatory = attributeElement.getAttribute("mandatory");

										String propTagValue = "";

										if (relation.equalsIgnoreCase("HASHMAP")) {

											propTagValue = dataMap.get(plm_tag);

										} else if (relation.equalsIgnoreCase("AUTOMATIC")) {

											propTagValue = UIGetValuesUtility.getSequenceID();
										} else {

											propTagValue = rule.getProperty(plm_tag);

											if (sap_tag.equals("SELECTIONRULES")) {

												Pattern p3 = Pattern.compile("\\[(.*?)\\]");
												Matcher m3 = p3.matcher(propTagValue);
												while (m3.find()) {
													propTagValue = propTagValue.replace(m3.group(1),
															dataMap.get("PLATFORM") + "_" + dataMap.get("MODELYEAR"));
												}
											} else if (sap_tag.equals("RULETYPE")) {
												propTagValue = new EnumTypesUtil().getRuleType(propTagValue);

												if (propTagValue.equals("IN")) {
													isInclusion = true;
												}
											} else if (sap_tag.equals("FAMILYID")) {
												String familyID = getFamilyOption(propTagValue, 0);
												propTagValue = familyID;
											} else if (sap_tag.equals("OPTIONID")) {
												String optionID = getFamilyOption(propTagValue, 1);

												if (optionID.equals("I20")) {
													optionID = "J21";
												}
												if (optionID.equals("WML")) {
													optionID = "WMU";
												}
												propTagValue = optionID;
											} else {

											}
										}

										if (dataMap.containsKey(sap_tag) == false) {

											dataMap.put(sap_tag, propTagValue.toUpperCase());
										}
										if (is_mandatory.equals("Yes") && propTagValue.equals("")) {

											String[] printValues = new String[] { Integer.toString(count),
													dataMap.get("PARTNO"),
													sap_tag + " value is empty in mandatory field.", "-", "-",
													"Error" };
											Logger.bufferResponse("PRINT", printValues, strBuilder);
											count++;
											toProcess = false;
											break;

										}
									}
								}
							}
						}
					}
				}

				if (isInclusion == true) {
					String familySubject = rule.getProperty("cfg0SubjectCondition");
					HashMap<String, String> InclusionMap = new HashMap<String, String>();
					if (!familySubject.equals("")) {
						String[] subject = familySubject.split("AND");
						for (int i = 0; i < subject.length; i++) {
							InclusionMap = (HashMap<String, String>) dataMap.clone();
							String familyID = getFamilyOption(subject[i], 0);
							String optionID = getFamilyOption(subject[i], 1);
							if (optionID.equals("I20")) {
								optionID = "J21";
							}
							if (optionID.equals("WML")) {
								optionID = "WMU";
							}
							fileName = InclusionMap.get("RULEID") + "_" + familyID + "_" + optionID;
							InclusionMap.put("FAMILYID", familyID);
							InclusionMap.put("OPTIONID", optionID);
							ruleMap.put(fileName, InclusionMap);
						}
					}
				} else {
					ruleMap.put(dataMap.get("RULEID") + "_" + dataMap.get("FAMILYID") + "_" + dataMap.get("OPTIONID"),
							dataMap);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (toProcess == false) {
			return null;
		}

		return ruleMap;

	}

	public String getPackageSelectionRule(TCComponent[] literalPackage, String plantModel, String ID) {

		String selection = "";
		try {
			for (int i = 0; i < literalPackage.length; i++) {

				String literal_ID = literalPackage[i].getProperty("cfg0ObjectId");

				if (selection.equals("")) {
					selection = plantModel + ID + " = " + literal_ID;
				} else {
					selection = selection + " AND " + plantModel + ID + " = " + literal_ID;
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return selection;
	}

	public String getFamilyOption(String context, int index) {

		String value = "";
		if (context.contains(" OR ")) {
			String[] split_values = context.split("\\]");
			if (split_values.length == 3) {
				String[] code = split_values[1].split("=");
				if (code.length != 0 && index == 1) {
					String[] code_value = code[1].split(" ");
					value = code_value[1].trim().toUpperCase();
				} else {
					value = code[index].trim().toUpperCase();
				}
			}
		} else {
			String[] split_values = context.split("\\]");
			if (split_values.length == 2) {
				String[] code = split_values[1].split("=");
				if (code.length != 0) {
					value = code[index].trim().toUpperCase();
				}
			}
		}
		return value;
	}

	public HashMap<String, HashSet<String>> previousIncludeRules(TCSession session, File oldBOM) {

		HashMap<String, HashSet<String>> previousIncludeRules = new HashMap<String, HashSet<String>>();
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new FileReader(oldBOM));
			if (bufferReader.ready()) {
				String sCurrentLine = "";
				previousIncludeRules = new HashMap<String, HashSet<String>>();

				while ((sCurrentLine = bufferReader.readLine()) != null) {
					String[] key_values = sCurrentLine.split("===", 2);
					if (key_values.length == 2) {
						String ruleId = key_values[0];
						String[] rules = key_values[1].split("~");
						HashSet<String> setRules = new HashSet<String>();
						for (String rule : rules) {
							setRules.add(rule.replace(" ", ""));
						}
						previousIncludeRules.put(ruleId, setRules);
					} else {
						return null;
					}
				}
			}
			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (Throwable e) {
					bufferReader = null;
				}
			}
		}
		return previousIncludeRules;
	}

	public File uploadCurrentIncludeRules(TCSession session, HashMap<String, HashSet<String>> includeRulesMap,
			String fileName) {
		// for the first time we don't have any dataset to download
		String path = "C:\\temp\\" + fileName + ".txt";
		File newFile = new File(path);
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(newFile));
			for (Entry<String, HashSet<String>> mapKey : includeRulesMap.entrySet()) {

				String key = mapKey.getKey();
				String value = "";
				for (String rule : mapKey.getValue()) {
					if (value.isEmpty()) {
						value = rule.replace(" ", "");
					} else {
						value = value + "~" + rule.replace(" ", "");
					}

				}
				output.write(key + "===" + value);
				output.newLine();
			}
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;
	}
}
