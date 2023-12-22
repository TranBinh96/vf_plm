package com.teamcenter.vinfast.model;

import java.util.LinkedList;

import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;

public class UpdateALForWorkprocess_Model {
	private LinkedList<String> conditionList;
	private LinkedList<String> gateList;
	private TCSession session;
	
	public UpdateALForWorkprocess_Model(TCSession _session) {
		conditionList = new LinkedList<String>();
		gateList = new LinkedList<String>();
		session = _session;
	}
	
	public void AddCondition(String input) {
		conditionList.add(input);
	}
	
	public String AddGate(String input) {
		if(input.isEmpty()) {
			gateList.add(input);
		}
		else {
			if (input.contains("\n")) {
				String[] str = input.split("\n");
				String result = "";
				for (String value : str) {
					if(TCExtension.GetGroupMemberByUserID(value, session) == null) {
						return value;	
					}
					if(!result.isEmpty()) {
						result += ",";
					}
					result += value;
				}
				gateList.add(result);
			}
			else {
				if(TCExtension.GetGroupMemberByUserID(input, session) != null) {
					gateList.add(input);
				}
				else {
					return input;
				}
			}
		}
		return "";
	}
	
	public String GetConditions() {
		String output = "";
		int i = 0;
		for (String string : conditionList) {
			if(i > 0) {
				output += ";";
			}
			output += string;
			i++;
		}
		return output;
	}
	
	public String GetGates() {
		String output = "";
		int i = 0;
		for (String string : gateList) {
			if(i > 0) {
				output += ";";
			}
			output += string;
			i++;
		}
		return output;
	}
}
