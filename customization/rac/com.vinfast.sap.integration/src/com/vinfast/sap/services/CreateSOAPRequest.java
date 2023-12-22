//package com.vinfast.sap.services;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import javax.xml.soap.MessageFactory;
//import javax.xml.soap.MimeHeaders;
//import javax.xml.soap.SOAPBody;
//import javax.xml.soap.SOAPElement;
//import javax.xml.soap.SOAPEnvelope;
//import javax.xml.soap.SOAPMessage;
//import javax.xml.soap.SOAPPart;
//
//import com.vinfast.sap.util.UIGetValuesUtility;
//
//import java.util.Base64;
//
//public class CreateSOAPRequest {
//
//	static FileWriter  writer = null;
//
//	public CreateSOAPRequest()
//	{
//
//	}
//
//	public SOAPMessage createRequest(HashMap<String, String> dataMap, String serviceHeader, String URL, String nameSpace, String ID, String logFolder, String userAndPassWord) {
//
//		SOAPMessage soapMessage = null ;
//		try {
//			String file_path = logFolder + "//" + ID + "_" + UIGetValuesUtility.getSequenceID() + ".xml";
//			File openfile = new File(file_path);
//
//			if(openfile.exists()) {
//				openfile.delete();
//			}
//
//			MessageFactory messageFactory = MessageFactory.newInstance();
//			soapMessage = messageFactory.createMessage();
//
//			SOAPEnvelope envelope = createSOAPEnvelope(soapMessage, URL, nameSpace) ;
//
//			createBody(envelope, dataMap, serviceHeader, URL);
//
//			MimeHeaders headers = soapMessage.getMimeHeaders();
//
//			String authStringEnc =  Base64.getEncoder().encodeToString(userAndPassWord.getBytes());
//
//			headers.addHeader("Authorization", "Basic " + authStringEnc) ;
//
//			soapMessage.saveChanges();
//
//			OutputStream outputStream = new FileOutputStream(openfile);
//			soapMessage.writeTo(outputStream);
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
//			outputStreamWriter.flush();
//			outputStreamWriter.close();
//
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//			Logger.writeLog("INFO:| "+ex.getMessage(),logFolder);
//		}
//
//		return soapMessage;
//	}
//
//	public SOAPEnvelope createBody(SOAPEnvelope env , HashMap<String, String> dataMap, String serviceHeader, String URL)
//	{
//		SOAPEnvelope envelope = env;
//		try
//		{
//			SOAPBody soapBody = envelope.getBody();
//
//			Iterator<Entry<String, String>>  it = dataMap.entrySet().iterator();
//			SOAPElement soapBodyElem = soapBody.addChildElement(serviceHeader , URL);
//			SOAPElement soapDataElem = soapBodyElem.addChildElement("DATA") ;
//
//			while (it.hasNext()) {
//
//				Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
//				SOAPElement temp = null ;
//
//				if(pair.getKey().toString().equals("FUNCTIONAL")) {
//
//					temp = soapDataElem.addChildElement(pair.getKey().toString());
//					String[] splitContext = pair.getValue().toString().split(",");
//					for(String context : splitContext) {
//						SOAPElement tempchild = temp.addChildElement("VALUE");
//						tempchild.addTextNode(context);
//					}
//
//				}else {
//
//					temp = soapDataElem.addChildElement(pair.getKey().toString());
//					temp.addTextNode(pair.getValue().toString());
//				}
//
//			}
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//		}
//
//		return envelope;
//	}
//
//	public SOAPEnvelope createSOAPEnvelope(SOAPMessage soapMessage, String URL, String nameSpace) {
//		SOAPEnvelope envelope = null;
//		try {
//			SOAPPart soapPart = soapMessage.getSOAPPart();
//			envelope = soapPart.getEnvelope();
//			envelope.addNamespaceDeclaration(URL, nameSpace);
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//		}
//		return envelope ;
//	}
//}
