//package com.vinfast.sap.services;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import javax.xml.soap.SOAPConnection;
//import javax.xml.soap.SOAPConnectionFactory;
//import javax.xml.soap.SOAPMessage;
//
//import com.vinfast.sap.util.UIGetValuesUtility;
//
//public class InitiateWebService {
//
//	public InitiateWebService() {
//
//	}
//	
//
//	public CreateSOAPRequest createSOAPRequest() {
//		CreateSOAPRequest soapRequestClass = new CreateSOAPRequest() ;
//		return soapRequestClass;
//	}
//	
//	public String[] callWebService(SOAPMessage soapRequest, String webURL) {
//		String[] message = null;
//		SOAPMessage soapResponse = null ;
//		try {
//			// Create SOAP Connection
//			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
//			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
//
//			// Send SOAP Message to SOAP Server
//			soapResponse = soapConnection.call(soapRequest, webURL);
//			soapResponse.writeTo(System.out);
//			soapConnection.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			//log.writeLog("RESULT:| "+ex.getMessage());
//		}
//
//		return message ;
//	}
//
//	public String[] callWebService(SOAPMessage soapRequest, String webURL, String ID, String logFolder) {
//		String[] message = null;
//		SOAPMessage soapResponse = null ;
//		FileWriter  writer = null;
//		try {
//			String file_path = logFolder+"//"+ID+"_"+UIGetValuesUtility.getSequenceID()+".xml";
//			System.out.println(file_path);
//			if (file_path.contains("\n")) {
//				file_path = file_path.replace("\n", "");
//			}
//			File openfile = new File(file_path);
//
//			if (openfile.exists()) {
//				openfile.delete();
//				writer = new FileWriter(openfile, true);
//			} else {
//				writer = new FileWriter(openfile, true);
//			}
//
//			// Create SOAP Connection
//			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
//			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
//
//			// Send SOAP Message to SOAP Server
//			soapResponse = soapConnection.call(soapRequest, webURL);
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			soapResponse.writeTo(out);
//			String strMsg = new String(out.toByteArray());
//			writer.write(strMsg);
//			message = UIGetValuesUtility.getResponseError(strMsg);
//			writer.write("\n");
//			writer.close();
//			soapConnection.close();
//		} catch (Exception ex) {
//			try {
//				ex.printStackTrace();
//				if (writer != null) {
//					writer.close();
//				}
//			
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ex.printStackTrace();
//		}
//
//		return message ;
//	}
//
//	public String[] callWebService1(SOAPMessage soapRequest, String webURL, String ID, String logFolder) {
//		String[] message = null;
//		FileWriter  writer = null;
//		try {
//			String file_path = logFolder + "//" + ID + "_" + UIGetValuesUtility.getSequenceID() + ".xml";
//			System.out.println(file_path);
//			if (file_path.contains("\n")) {
//				file_path = file_path.replace("\n", "");
//			}
//			File openfile = new File(file_path);
//
//			if(openfile.exists()) {
//				openfile.delete();
//				writer = new FileWriter(openfile, true);
//			} else {
//				writer = new FileWriter(openfile, true);
//			}
//		} catch (Exception ex) {
//			try {
//				ex.printStackTrace();
//				if (writer != null) {
//					writer.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			ex.printStackTrace();
//		}
//
//		return message ;
//	}
//}
