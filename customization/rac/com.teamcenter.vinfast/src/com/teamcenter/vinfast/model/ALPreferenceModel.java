package com.teamcenter.vinfast.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;

public class ALPreferenceModel {
	private TCSession session;
	private LinkedList<String> conditionList;
	private LinkedList<String> gateList;
	public LinkedHashMap<String, LinkedHashMap<String, String>> valueList;
	public LinkedHashMap<String, String> alList;
	private boolean byAL = false;

	public ALPreferenceModel(TCSession _session, String preferenceName, boolean al) {
		session = _session;
		conditionList = new LinkedList<String>();
		gateList = new LinkedList<String>();
		valueList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		alList = new LinkedHashMap<String, String>();
		byAL = al;

		if (!preferenceName.isEmpty()) {
			String[] preValues = TCExtension.GetPreferenceValues(preferenceName, session);
			if (preValues != null && preValues.length >= 3) {
				SetConditionList(preValues[1]);
				SetGateList(preValues[2]);
				if (preValues.length > 3) {
					SetValuesList(preValues);
				}
			}
		}
	}

	private void SetConditionList(String input) {
		if (!input.isEmpty()) {
			if (input.contains(";")) {
				String[] values = input.split(";");
				for (String value : values) {
					conditionList.add(value);
				}
			} else {
				conditionList.add(input);
			}
		}
	}

	private void SetGateList(String input) {
		if (!input.isEmpty()) {
			if (input.contains(";")) {
				String[] values = input.split(";");
				for (String value : values) {
					gateList.add(value);
				}
			} else {
				gateList.add(input);
			}
		}
	}

	private void SetValuesList(String[] input) {
		for (int i = 3; i < input.length; i++) {
			String row = input[i];
			if (row.contains("=")) {
				String[] condition_reviewer = row.split("=");
				if (condition_reviewer.length >= 2) {
					valueList.put(condition_reviewer[0], SetReviewerList(condition_reviewer[1]));
					if (byAL)
						alList.put(condition_reviewer[0], condition_reviewer[1]);
				}
			}
		}
	}

	private LinkedHashMap<String, String> SetReviewerList(String input) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();

		if (byAL) {
			try {
				TCComponentAssignmentList rawModel = TCExtension.GetALByName(input, session);
				if (rawModel != null) {
					TCComponent[] resRaw = rawModel.getRelatedComponents("resources");
					TCComponent[] taskRaw = rawModel.getRelatedComponents("task_templates");

					for (String gate : gateList) {
						Set<String> reviewers = new HashSet<String>();
						for (int i = 0; i < taskRaw.length; i++) {
							if (gate.compareTo(taskRaw[i].getPropertyDisplayableValue("object_name")) == 0) {
								TCComponent[] items = resRaw[i].getRelatedComponents("resources");
								if (items != null && items.length > 0) {
									for (TCComponent item : items) {
										reviewers.add(item.toString());
									}
								}
							}
						}
						output.put(gate, String.join(",", reviewers));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (input.contains(";")) {
				String[] reviewerEachGates = input.split(";");
				int i = 0;
				for (String gate : gateList) {
					String reviewers = "";
					if (i <= reviewerEachGates.length) {
						if (i < reviewerEachGates.length)
							reviewers = reviewerEachGates[i];
					}
					output.put(gate, reviewers);
					i++;
				}
			} else {
				String[] reviewerEachGates = { input };
				int i = 0;
				for (String gate : gateList) {
					String reviewers = "";
					if (i <= reviewerEachGates.length) {
						if (i < reviewerEachGates.length)
							reviewers = reviewerEachGates[i];
					}
					output.put(gate, reviewers);
					i++;
				}
			}
		}

		return output;
	}

	private Set<TCComponentGroupMember> GetReviewer(String input) {
		Set<TCComponentGroupMember> output = new HashSet<TCComponentGroupMember>();
		if (!input.isEmpty()) {
			if (input.contains(",")) {
				String[] reviewers = input.split(",");
				for (String reviewer : reviewers) {
					TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(reviewer, session);
					if (member != null) {
						output.add(member);
					}
				}
			} else {
				TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(input, session);
				if (member != null) {
					output.add(member);
				}
			}
		}

		return output;
	}

	@SuppressWarnings("null")
	public LinkedHashMap<String, String> GetALByCondition(String condition) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		output = valueList.get(condition);
		if (output == null) {
			for (String gate : gateList) {
				output.put(gate, "");
			}
		}
		return output;
	}

	public LinkedHashMap<String, Set<TCComponentGroupMember>> GetAssignmentListByCondition(String condition) {
		LinkedHashMap<String, Set<TCComponentGroupMember>> output = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
		LinkedHashMap<String, String> values = valueList.get(condition);
		if (values != null && values.size() > 0) {
			for (Map.Entry<String, String> value : values.entrySet()) {
				Set<TCComponentGroupMember> al = GetReviewer(value.getValue());
				output.put(value.getKey(), al);
			}
		} else {
			for (String gate : gateList) {
				output.put(gate, null);
			}
		}
		return output;
	}

	public String getPALByByCondition(String condition) {
		if (alList.containsKey(condition))
			return alList.get(condition);
		return "";
	}

	public LinkedList<String> getGateList() {
		return gateList;
	}
}
