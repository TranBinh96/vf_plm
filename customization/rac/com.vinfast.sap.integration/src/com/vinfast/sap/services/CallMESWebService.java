package com.vinfast.sap.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONObject;

import com.teamcenter.integration.model.BaseModel;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf4.services.rac.integration.SAPIntegrationService;
import com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInput;
import com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInputs;
import com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESResponse;
import com.vinfast.sap.util.UIGetValuesUtility;

public class CallMESWebService {

	//	private static String userName = "phongvd3";
	//	private static String password = "phongvd3";
	//	private static String userName = "VFS-MESAPP05-VT\\uafadmin";
	//	private static String password = "Vinfast@123";

	public int callService(StringBuffer dataString, String operation, String IP, String logFolder) {
		int code = 0;
		try {
			String url_value = "http://" + IP + "/ImportOperations";

			URL url = new URL(url_value);

			String inputString = dataString.toString().replaceAll("&", "_");

			try {
				new UIGetValuesUtility();
				String sequenceID = UIGetValuesUtility.getSequenceID();
				BufferedWriter writer = new BufferedWriter(new FileWriter(logFolder + "\\" + operation + "_" + sequenceID + ".xml"));
				writer.write(inputString);
				writer.newLine();
				writer.close();
			} catch (Exception e) {
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
			String encoded = Base64.getEncoder().encodeToString(inputString.getBytes());
			String input = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><a:Action>http://tempuri.org/IHttpsServer/SetData</a:Action>" + "<a:To>http://" + IP + "/ImportOperations</a:To></s:Header><s:Body><SetData xmlns=\"http://tempuri.org/\"><data>" + encoded + "</data>" + "<enc>UTF8</enc></SetData></s:Body></s:Envelope>";

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

	public BaseModel callNewService(String token, String inputString, String ip) {
		BaseModel returnModel = new BaseModel();
		HttpURLConnection httpConn = null;
		try {
			if (ip.contains("https"))
				ip += "/sit-svc/application/FBAPIIAPP/odata/ImportOperationDefinitionWS";
			else
				ip = "http://" + ip + "/sit-svc/application/FBAPIIAPP/odata/ImportOperationDefinitionWS";
			URL url = new URL(ip);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestProperty("Authorization", "Bearer " + token);
			OutputStream outStream = httpConn.getOutputStream();
			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
			outStreamWriter.write(inputString);
			outStreamWriter.flush();
			outStreamWriter.close();
			outStream.close();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				new JSONObject(getResponseMessage(new InputStreamReader(httpConn.getInputStream())));
				returnModel.setErrorCode("00");
			} else if (httpConn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
				JSONObject json = new JSONObject(getResponseMessage(new InputStreamReader(httpConn.getErrorStream())));
				returnModel.setErrorCode("01");
				returnModel.setMessage("Transfer not success.");
				try {
					if (json.has("error")) {
						JSONObject error = json.getJSONObject("error");
						if (error.has("message"))
							returnModel.setMessage(error.getString("message"));
					} else if (json.has("Error")) {
						JSONObject error = json.getJSONObject("Error");
						if (error.has("ErrorMessage"))
							returnModel.setMessage(error.getString("ErrorMessage"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				returnModel.setErrorCode("00");
				returnModel.setMessage("Failed : HTTP Error code : " + httpConn.getResponseCode());
			}
		} catch (Exception e) {
			returnModel.setErrorCode("02");
			returnModel.setMessage("Unable to connect to the MES system. Please re-check your connection or contact MES administrator");
			e.printStackTrace();
		} finally {
			if (httpConn != null)
				httpConn.disconnect();
		}
		return returnModel;
	}

	public int callService(String inputString, String IP) {
		int code = 0;

		try {
			String url_value = "http://" + IP + "/ImportOperations";
			URL url = new URL(url_value);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/soap+xml");
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.setRequestProperty("Host", IP);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1 (java 1.5)");
			String encoded = Base64.getEncoder().encodeToString(inputString.getBytes());
			String input = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><a:Action>http://tempuri.org/IHttpsServer/SetData</a:Action>" + "<a:To>http://" + IP + "/ImportOperations</a:To></s:Header><s:Body><SetData xmlns=\"http://tempuri.org/\"><data>" + encoded + "</data>" + "<enc>UTF8</enc></SetData></s:Body></s:Envelope>";

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

	public static LinkedHashMap<String, String> callService(String IP, LinkedHashMap<String, String> xmlDataList) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		try {
			String url_value = "http://" + IP + "/ImportOperations";
			URL url = new URL(url_value);
			for (Map.Entry<String, String> xmlData : xmlDataList.entrySet()) {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/soap+xml");
				conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
				conn.setRequestProperty("Host", IP);
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1 (java 1.5)");
				String encoded = Base64.getEncoder().encodeToString(xmlData.getValue().getBytes());
				String input = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><a:Action>http://tempuri.org/IHttpsServer/SetData</a:Action>" + "<a:To>http://" + IP + "/ImportOperations</a:To></s:Header><s:Body><SetData xmlns=\"http://tempuri.org/\"><data>" + encoded + "</data>" + "<enc>UTF8</enc></SetData></s:Body></s:Envelope>";
				OutputStream os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();
				output.put(xmlData.getKey(), Integer.toString(conn.getResponseCode()));
				conn.disconnect();
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	public int callSOAService(TCSession session, String ip, String xmlData, String opeID) {
		try {
			SAPIntegrationService service = SAPIntegrationService.getService(session);
			TransferOperationToMESInputs inputs = new TransferOperationToMESInputs();
			inputs.mesServerIP = ip;
			inputs.mesServerPort = 80;
			inputs.inputs = new TransferOperationToMESInput[1];
			inputs.inputs[0] = new TransferOperationToMESInput();
			inputs.inputs[0].xml = xmlData;
			inputs.inputs[0].operationID = opeID;

			TransferOperationToMESResponse response = service.transferOperationToMES(inputs);
			if (response.servicedata.sizeOfPartialErrors() == 0) {
				LinkedHashMap<String, String> opeHashMap = new LinkedHashMap<String, String>();
				int i = 0;
				for (String operationID : response.operationIDs) {
					opeHashMap.put(operationID, response.returnCodes[i++]);
				}
				if (response.returnCodes.length > 0) {
					return Integer.parseInt(response.returnCodes[0]);
				}
			} else {
				ServiceData serviceData = response.servicedata;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static LinkedHashMap<String, String> callSOAService(TCSession session, String ip, LinkedHashMap<String, String> xmlDataList) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		try {
			SAPIntegrationService service = SAPIntegrationService.getService(session);
			TransferOperationToMESInputs inputs = new TransferOperationToMESInputs();
			inputs.mesServerIP = ip;
			inputs.mesServerPort = 80;
			LinkedList<TransferOperationToMESInput> inputList = new LinkedList<TransferOperationToMESInput>();
			for (Map.Entry<String, String> xmlData : xmlDataList.entrySet()) {
				TransferOperationToMESInput inputItem = new TransferOperationToMESInput();
				inputItem.operationID = xmlData.getKey();
				inputItem.xml = xmlData.getValue();
				inputList.add(inputItem);
			}
			inputs.inputs = inputList.toArray(new TransferOperationToMESInput[0]);

			TransferOperationToMESResponse response = service.transferOperationToMES(inputs);
			if (response.servicedata.sizeOfPartialErrors() == 0) {
				int i = 0;
				for (String operationID : response.operationIDs) {
					output.put(operationID, response.returnCodes[i++]);
				}
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.servicedata), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	public static String getAccessToken(String ip, String userName, String password) {
		HttpURLConnection httpConn = null;
		try {
			if (ip.contains("https"))
				ip += "/sit-auth/OAuth/Token";
			else
				ip = "http://" + ip + "/sit-auth/OAuth/Token";
			URL url = new URL(ip);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Authorization", "Basic Y2xpZW50aWQ6Y2xpZW50UHN3");
			httpConn.setRequestProperty("Host", ip);
			httpConn.setRequestProperty("Connection", "keep-alive");
			httpConn.setRequestProperty("Content-Length", "74");
			OutputStream outStream = httpConn.getOutputStream();
			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
			outStreamWriter.write("grant_type=password&username=" + userName + "&password=" + password + "&scope=global");
			outStreamWriter.flush();
			outStreamWriter.close();
			outStream.close();

			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP Error code : " + httpConn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			JSONObject json = new JSONObject(sb.toString());

			return json.getString("access_token");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpConn != null)
				httpConn.disconnect();
		}
		return "";
	}

	private String getResponseMessage(Reader in) {
		BufferedReader br = new BufferedReader(in);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
