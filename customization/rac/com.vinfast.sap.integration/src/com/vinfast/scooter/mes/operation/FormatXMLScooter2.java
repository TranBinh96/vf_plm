package com.vinfast.scooter.mes.operation;

public class FormatXMLScooter2 {
	public static String xmlFormatOperation(MEOPDataAbstract dataMap) {
		StringBuffer dataString = new StringBuffer();
		dataMap.xmlAppendHeader(dataString);
		dataMap.xmlAppendBody(dataString);
		dataMap.xmlAppendFooter(dataString);
		return dataString.toString();
	}

	public static String jsonFormatOperation(MEOPDataAbstract dataMap) {
		StringBuffer dataString = new StringBuffer();
		dataString.append("{\"command\":{\"OperationsList\":[");
		dataMap.jsonAppendBody(dataString);
		dataString.append("]}}");
		return dataString.toString();
	}
}
