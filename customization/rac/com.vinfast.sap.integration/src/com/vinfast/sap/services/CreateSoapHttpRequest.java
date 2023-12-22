package com.vinfast.sap.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.vinfast.sap.util.UIGetValuesUtility;

public class CreateSoapHttpRequest {

	public static String[] sendRequest(String serverURL, HashMap<String, String> data, String serviceHeader, String URL, String nameSpace, String fileName, String logFolder, String authentication) throws Exception {

		String[] responseMsg = null;
		HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1)
				.proxy(ProxySelector.getDefault())
				.build();

		StringBuilder requestData = buildFormDataFromMap(URL, serviceHeader, nameSpace, data);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(serverURL))
				.POST(HttpRequest.BodyPublishers.ofString(requestData.toString(), StandardCharsets.UTF_8))
				.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((authentication).getBytes()))
				.header("SOAPAction", "http://sap.com/xi/WebService/soap1.1")
				.header("Content-Type","text/xml; charset=utf-8")
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		String file_path = logFolder + "//" + fileName + "_" + UIGetValuesUtility.getSequenceID() + ".xml";
		File openfile = new File(file_path);

		try {
			if(openfile.exists()) {
				openfile.delete();
				BufferedWriter out = new BufferedWriter(new FileWriter(openfile));
				out.write(requestData.toString());
				out.write("\n\n\n");
				out.write(response.body());
				out.close();
			}
			else {
				BufferedWriter out = new BufferedWriter(new FileWriter(openfile));
				out.write(requestData.toString());
				out.write("\n\n\n");
				out.write(response.body());
				out.close();
			}
		} catch (IOException e) {
		}
		responseMsg = UIGetValuesUtility.getResponseError(response.body());
		return responseMsg;

	}

	private static StringBuilder buildFormDataFromMap(String URL, String serviceHeader, String nameSpace, HashMap<String, String> data) {

		StringBuilder builder = new StringBuilder();
		builder.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:"+URL+"=\""+nameSpace+"\"><SOAP-ENV:Header/><SOAP-ENV:Body><"+URL+":"+serviceHeader+"><DATA>");
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if(entry.getValue().isEmpty() || entry.getValue().isBlank()) {
				builder.append("<"+entry.getKey().toString()+"/>");
			}else {
				builder.append("<"+entry.getKey().toString()+">");
				if(entry.getKey().equals("OPTION")) {
					if(entry.getValue().contains("!=")) {
						builder.append(entry.getValue().replaceAll("!=", "&lt;&gt;"));
					}else {
						builder.append(entry.getValue().toString());
					}
				}else {
					builder.append(entry.getValue().toString());
				}
				builder.append("</"+entry.getKey().toString()+">");
			}
		}
		builder.append("</DATA></"+URL+":"+serviceHeader+"></SOAP-ENV:Body></SOAP-ENV:Envelope>");
		return builder;
	}
}
