package com.vinfast.sap.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	public static String BUFFERRESPONSE_DETAILS = "DETAILS";
	public static String BUFFERRESPONSE_HEADER = "HEADER";
	public static String BUFFERRESPONSE_PRINT = "PRINT";

	public Logger() {

	}

	public static void writeLog(String msg, String path) {
		BufferedWriter bw = null;
		try {
			File logFile = new File(path + "//MCN_SAP_log.txt");
			if (logFile.exists() && !logFile.isDirectory()) {
				bw = new BufferedWriter(new FileWriter(logFile, true));
			} else {
				logFile.createNewFile();
				bw = new BufferedWriter(new FileWriter(logFile, true));
			}
			bw.write(msg);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void bufferResponse(String argument, String[] values, StringBuilder strBuilder) {
		switch (argument) {
		case "APPEND":
			for (String info : values) {
				strBuilder.append(info);
			}
			break;
		case "DETAILS":
			strBuilder.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
			strBuilder.append("<tr>");
			for (String header : values) {
				strBuilder.append("<th><b>" + header + "</b></th>");
			}

			strBuilder.append("</tr></table>");
			break;
		case "HEADER":
			strBuilder.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");
			for (String header : values) {
				strBuilder.append("<th><b>" + header + "</b></th>");
			}

			strBuilder.append("</tr>");
			break;
		case "WARN":
			if (values[values.length - 1].equalsIgnoreCase("Error")) {
				strBuilder.append("<tr>");
				for (String header : values) {
					strBuilder.append("<td align=\"center\"><font color=\"Maroon\">" + header + "</font></td>");
				}
				strBuilder.append("</tr>");
			} else {
				strBuilder.append("<tr>");
				for (String header : values) {
					strBuilder.append("<td align=\"center\">" + header + "</td>");
				}
				strBuilder.append("</tr>");
			}
			break;
		case "PRINT":
			if (values[values.length - 1].equalsIgnoreCase("Error")) {
				strBuilder.append("<tr>");
				for (String header : values) {
					strBuilder.append("<td align=\"center\"><font color=\"red\">" + header + "</font></td>");
				}
				strBuilder.append("</tr>");
			} else {
				strBuilder.append("<tr>");
				for (String header : values) {
					strBuilder.append("<td align=\"center\">" + header + "</td>");
				}
				strBuilder.append("</tr>");
			}
			break;
		}
	}

	public static String previousTransaction(String logPath, String fileName) {
		String oldData = "";
		File logFile = null;
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
			logFile = new File(logPath + "//VF_" + fileName + "_" + current_date + ".html");

			if (logFile.exists() && !logFile.isDirectory()) {
				String text = new String(Files.readAllBytes(Paths.get(logPath + "//VF_" + fileName + "_" + current_date + ".html")), StandardCharsets.UTF_8);
				oldData = text.substring(12, text.indexOf("</body>"));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return oldData;
	}

	public static File writeBufferResponse(String msg, String logPath, String fileName) {
		File logFile = null;
		BufferedWriter bw = null;
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
			logFile = new File(logPath + "//VF_" + fileName + "_" + current_date + ".html");

			if (logFile.exists() && !logFile.isDirectory()) {
				bw = new BufferedWriter(new FileWriter(logFile, false));
				bw.write(msg);
			} else {
				logFile.createNewFile();
				bw = new BufferedWriter(new FileWriter(logFile, false));
				bw.write(msg);
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logFile;
	}

	public static File writeResponse(String[] msg, String logPath, String fileName) {
		File logFile = null;
		BufferedWriter bw = null;
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
			logFile = new File(logPath + "//VF_" + fileName + "_" + current_date + ".html");

			if (logFile.exists() && !logFile.isDirectory()) {
				if (msg[0].equalsIgnoreCase("CLEAN")) {
					bw = new BufferedWriter(new FileWriter(logFile, false));
					bw.write("");
				} else {
					bw = new BufferedWriter(new FileWriter(logFile, true));
				}
			} else {
				logFile.createNewFile();
				bw = new BufferedWriter(new FileWriter(logFile, true));
			}

			if (msg[0].equals("") || msg[0].equalsIgnoreCase("CLEAN")) {
				bw.close();
				return logFile;
			}
			if (msg.length == 6) {
				if (msg[0].equalsIgnoreCase("HEADER")) {
					bw.write("<html><body><table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\">" + "<tr style=\"background-color:#ccffff;\"><th><b>" + msg[1] + "</b></th><th colspan=\"2\"><b>" + msg[2] + "</b></th><th><b>" + msg[3] + "</b></th><th><b>" + msg[4] + "</b></th><th><b>" + msg[5] + "</b></th></tr>");
				} else if (msg[0].equalsIgnoreCase("FOOTER")) {
					bw.write("</table></body></html>");
				} else if (msg[0].equalsIgnoreCase("-")) {
					bw.write("<tr><td colspan=\"6\" align=\"center\"><b>" + msg[2] + "</b></td></tr>");
				} else if (msg[5].equalsIgnoreCase("Error")) {
					bw.write("<tr><td align=\"center\"><font color=\"red\">" + msg[0] + "</font></td><td align=\"center\"><font color=\"red\">" + msg[1] + "</font></td><td align=\"center\"><font color=\"red\">" + msg[2] + "</font></td><td><font color=\"red\">" + msg[3] + "</font></td><td align=\"center\"><font color=\"red\">" + msg[4] + "</font></td><td align=\"center\"><font color=\"red\">" + msg[5] + "</font></td></tr>");
				} else {
					bw.write("<tr><td align=\"center\">" + msg[0] + "</td><td align=\"center\">" + msg[1] + "</td><td align=\"center\">" + msg[2] + "</td><td>" + msg[3] + "</td><td align=\"center\">" + msg[4] + "</td><td align=\"center\">" + msg[5] + "</td></tr>");
				}
			} else {
				if (msg[0].equalsIgnoreCase("HEADER")) {
					bw.write("<html><body><table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\">" + "<tr style=\"background-color:#ccffff;\"><th><b>" + msg[1] + "</b></th><th><b>" + msg[2] + "</b></th><th><b>Result</b></th></tr>");
				} else if (msg[0].equalsIgnoreCase("FOOTER")) {
					bw.write("</table></body></html>");
				} else if (msg[2].equalsIgnoreCase("Error")) {
					bw.write("<tr><td align=\"center\"><font color=\"red\">" + msg[0] + "</font></td><td><font color=\"red\">" + msg[1] + "</font></td><td align=\"center\"><font color=\"red\">" + msg[2] + "</font></td></tr>");
				} else {
					bw.write("<tr><td align=\"center\">" + msg[0] + "</td><td>" + msg[1] + "</td><td align=\"center\">" + msg[2] + "</td></tr>");
				}
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logFile;
	}

	public static String commonInfo(String userName) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<html><body>");
		String[] printValues = new String[] { "User :" + userName, "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		bufferResponse(BUFFERRESPONSE_DETAILS, printValues, strBuilder);

		return strBuilder.toString();
	}
}
