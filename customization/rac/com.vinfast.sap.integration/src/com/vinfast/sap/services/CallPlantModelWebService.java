package com.vinfast.sap.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import com.vinfast.sap.util.UIGetValuesUtility;

public class CallPlantModelWebService {
	
	public CallPlantModelWebService(){
		
	}
	
	public int callService(StringBuffer dataString, String WS, String IP, String logFolder)
	{
		int code = 0;
		try {
			String url_value = "http://"+IP+"/ImportPlant";
			URL url = new URL(url_value);
			
			try
			{
				new UIGetValuesUtility();
				String sequenceID = UIGetValuesUtility.getSequenceID() ;
				BufferedWriter writer = new BufferedWriter(new FileWriter(logFolder+"\\"+WS+"_"+sequenceID+".html"));
				writer.write(dataString.toString());

				writer.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/soap+xml");
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.setRequestProperty("Host", IP);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1 (java 1.5)");
			
			String encoded = Base64.getEncoder().encodeToString(dataString.toString().getBytes());
			
			String input = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><a:Action>http://tempuri.org/IHttpsServer/SetData</a:Action>"
					+ "<a:To>http://"+IP+"/ImportPlant</a:To></s:Header><s:Body><SetData xmlns=\"http://tempuri.org/\"><data>" + encoded + "</data>"
					+ "<enc>UTF8</enc></SetData></s:Body></s:Envelope>" ;

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			code = conn.getResponseCode();
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}

}
