package com.teamcenter.integration.ulti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Combo;

public class StringExtension {
	public static String htmlTableCss = "<style>table{width:100%;border-collapse:collapse;table-layout:fixed;font-size:10px;font-family:sans-serif;}td{height:24px;word-wrap:break-word;border:1px solid gray;padding:2px}tr:first-child td{border-top:none}tr td:first-child{border-left:none}tr td:last-child{border-right:none}p{font-size: 12px;}.badge{border-radius:3px;font-weight:300;line-height:1.3;font-size:100%;padding:0.25em 0.4em;font-size:12px;}.bg-success{background-color:#28D094;color:#fff;}.bg-danger{background-color:#FF4961;color:#fff;}.bg-default{color:#788394;background-color:#f5f6f7;}</style>";

	public static String genTableHeader(LinkedHashMap<String, String> headerList) {
		StringBuilder output = new StringBuilder();
		output.append("<tr style='background-color: #1E9FF2; color: white; text-align: center'>");
		for (Map.Entry<String, String> header : headerList.entrySet()) {
			output.append("<td style=\"width: " + header.getValue() + "%;\"><p>" + header.getKey() + "</p></td>");
		}
		output.append("</tr>");
		return output.toString();
	}

	public static String genTableHeader(String[] headerList) {
		StringBuilder output = new StringBuilder();
		output.append("<tr style=\"background-color: #1E9FF2; color: white;\">");
		for (String header : headerList) {
			output.append("<td><p>" + header + "</p></td>");
		}
		output.append("</tr>");
		return output.toString();
	}

	public static String genBadgetSuccess(String value) {
		return "<span class=\"badge bg-success\">" + value + "</span>";
	}

	public static String genBadgetFail(String value) {
		return "<span class=\"badge bg-danger\">" + value + "</span>";
	}

	public static String genBadgetDefault(String value) {
		return "<span class=\"badge bg-default\">" + value + "</span>";
	}

	public static LinkedHashMap<String, String> GetComboboxValue(String[] inputList, String value) {
		LinkedHashMap<String, String> outputList = new LinkedHashMap<String, String>();

		for (String stringValue : inputList) {
			if (stringValue.contains(value)) {
				String[] str = stringValue.split("=");
				if (str.length > 2) {
					outputList.put(str[0], str[0] + " (" + str[2] + ")");
				} else if (str.length == 2) {
					outputList.put(str[0], str[0]);
				}
			}
		}

		return outputList;
	}

	public static String GetObjectTypeReal(String[] inputList, String value) {
		for (String stringValue : inputList) {
			String[] str = stringValue.split("=");
			if (str.length > 3) {
				if (str[0].compareToIgnoreCase(value) == 0) {
					return str[3];
				}
			}
		}
		return "";
	}

	public static LinkedHashMap<String, String> GetDataForm(String[] inputList) {
		LinkedHashMap<String, String> outputList = new LinkedHashMap<String, String>();

		for (String stringValue : inputList) {
			String[] str = stringValue.split("=");
			switch (str.length) {
			case 3:
				outputList.put(str[0], str[0] + " - " + str[2]);
				break;
			case 2:
				outputList.put(str[0], str[0] + " - " + str[1]);
				break;
			case 1:
				outputList.put(str[0], str[0]);
				break;
			default:
				outputList.put(stringValue, stringValue);
				break;
			}
		}

		return outputList;
	}

//	public static String GetValueFromText(String text) {
//		String[] str = text.split("\\(");
//		if(str.length > 0) {
//			return str[0].trim();
//		}else {
//			return text;
//		}
//	}

	public static String GetTextFromValue(String[] inputList, String value) {
		String output = "";

		for (String stringValue : inputList) {
			if (stringValue.contains(value)) {
				String[] str = stringValue.split("=");
				if (str.length > 2) {
					return str[0] + " (" + str[2] + ")";
				} else if (str.length == 2) {
					return str[0];
				}
			}
		}

		return output;
	}

	public static void UpdateValueTextCombobox(Combo cbItem, LinkedHashMap<String, String> data) {
		cbItem.removeAll();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			cbItem.add(entry.getValue());
			cbItem.setData(entry.getValue(), entry.getKey());
		}
	}

	public static String ConvertNumberToString(int input, int numberLetter) {
		String output = "";
		for (int i = 0; i < numberLetter - String.valueOf(input).length(); i++) {
			output += "0";
		}
		return output + String.valueOf(input);
	}

	public static boolean CheckCapitalCase(String str) {
		char ch;
		boolean lowerCaseFlag = false;
		boolean numberFlag = false;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
//	        if(Character.isDigit(ch)) {
//	            numberFlag = true;
//	        }
			if (Character.isLowerCase(ch)) {
				lowerCaseFlag = true;
			}
			if (numberFlag || lowerCaseFlag)
				return false;
		}
		return true;
	}

	public static boolean checkSpecialChar(String inputString) {
		if (inputString.isEmpty())
			return false;
		Pattern special = Pattern.compile("[!@#$%&*()+=|<>?{}\\[\\]~]");
		Matcher hasSpecial = special.matcher(inputString);
		return hasSpecial.find();
	}

	public static String getTimeStamp() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss-SSS");

		return df.format(cal.getTime());
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	public static String htmlTextSuccess(String input) {
		StringBuilder output = new StringBuilder();
		output.append("<html style=\"padding: 0px;\"><body style=\"margin: 5px;\"><p style=\"font-size: 12px;\">");
		output.append(input);
		output.append("</p></body></html>");

		return output.toString();
	}

	public static String htmlTextError(String input) {
		StringBuilder output = new StringBuilder();
		output.append("<html style=\"padding: 10px;\"><body style=\"margin: 5px;\"><p style=\"color: red;\">");
		output.append(input);
		output.append("</p></body></html>");

		return output.toString();
	}

	public static boolean checkValueExistInList(String value, LinkedList<String> list) {
		for (String item : list) {
			if (item.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	public static boolean checkValueExistInList(String value, Set<String> list) {
		for (String item : list) {
			if (item.compareToIgnoreCase(value) == 0)
				return true;
		}
		return false;
	}

	public static boolean createFolder(String forderPath) {
		return new File(forderPath).mkdirs();
	}

	public static String getFMSHome() {
		try {
			Process p = null;
			Runtime r = Runtime.getRuntime();
			String OS = System.getProperty("os.name").toLowerCase();

			if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1)) {
				p = r.exec("cmd.exe /c set FMS_HOME");
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				int idx = line.indexOf('=');
				return line.substring(idx + 1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}
