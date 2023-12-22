package com.vinfast.sap.util;

import java.util.HashMap;

public class EnumTypesUtil {

	public EnumTypesUtil() {

	}
	
	public String SAPCodes(String value) {
		
		HashMap<String, String> codes = new HashMap<String, String>();
		
		codes.put("RAW MATERIAL", "ZRAW");
		codes.put("SEMI-FINISHED", "ZSFG");
		codes.put("FINISHED", "ZFG");
		codes.put("VEHICLE", "ZVEH");
		codes.put("SOFTWARE", "ZSOF");
		
		codes.put("Make", "E");
		codes.put("Purchase", "F");
		codes.put("Finished Goods", "S");
		
		codes.put("Approved", "A");
		codes.put("Not Approved", "N");
		codes.put("", "N");
		
		codes.put("Traceable", "Y");
		codes.put("Not Traceable", "N");
		codes.put("", "N");
		
		codes.put("Availability Rule", "AV");
		codes.put("Inclusion Rule", "IN");
		codes.put("Default Rule", "DE");
		codes.put("Package Option Value", "IN");
		
		return codes.get(value);
	}

	public String getMaterialType(String value){

		HashMap<String, String> material_Type = new HashMap<String, String>();
		material_Type.put("RAW MATERIAL", "ZRAW");
		material_Type.put("SEMI-FINISHED", "ZSFG");
		material_Type.put("FINISHED", "ZFG");
		material_Type.put("VEHICLE", "ZVEH");
		material_Type.put("SOFTWARE", "ZSOF");
		material_Type.put("", "");

		return material_Type.get(value);
	}
	
	public String getMakeBuy(String value){

		HashMap<String, String> makebuyCode = new HashMap<String, String>();
		makebuyCode.put("Make", "E");
		makebuyCode.put("Purchase", "F");
		makebuyCode.put("Finished Goods", "S");

		return makebuyCode.get(value);
	}

	public String getPPAP(String value){

		HashMap<String, String> ppapCode = new HashMap<String, String>();
		ppapCode.put("Approved", "A");
		ppapCode.put("Not Approved", "N");
		ppapCode.put("", "A");

		return ppapCode.get(value);
	}
	
	public String getTraceable(String value){

		HashMap<String, String> trace_code = new HashMap<String, String>();
		trace_code.put("Traceable", "Y");
		trace_code.put("Not Traceable", "N");
		trace_code.put("", "N");

		return trace_code.get(value);
	}
	
	public String getRuleType(String value){

		HashMap<String, String> trace_code = new HashMap<String, String>();
		trace_code.put("Availability Rule", "AV");
		trace_code.put("Inclusion Rule", "IN");
		trace_code.put("Default Rule", "DE");
		trace_code.put("Package Option Value", "IN");

		return trace_code.get(value);
	}

}
