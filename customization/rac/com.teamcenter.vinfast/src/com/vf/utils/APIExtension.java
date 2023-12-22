package com.vf.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class APIExtension {

	public static Map<String, Set<String>> getImpactedProgram(Set<String> uidList) {
		Map<String, Set<String>> returnData = new HashMap<String, Set<String>>();
		try {
			URL url = new URL("http://10.128.11.181:8080/hphongapi/engineering/impactedProgram_Get?" + "uid=" + String.join(",", uidList));
//			URL url = new URL("http://localhost:9669/engineering/impactedProgram_Get?" + "uid=" + String.join(",", uidList));
			HttpURLConnection httpConn = null;
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setConnectTimeout(20000);
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP Error code : " + httpConn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
			BufferedReader br = new BufferedReader(in);

			StringBuilder sbt = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sbt.append(output);
			}
			JSONObject json = new JSONObject(sbt.toString());
			String errorCode = json.getString("errorCode");
			if (errorCode.compareToIgnoreCase("00") == 0) {
				JSONArray dataListJson = json.getJSONArray("dataList");
				if (dataListJson != null) {
					for (Object object : dataListJson) {
						if (object instanceof JSONObject) {
							String partNo = ((JSONObject) object).getString("partNo");
							Set<String> programList = new HashSet<String>();
							JSONArray programListJSON = ((JSONObject) object).getJSONArray("programList");
							for (Object program : programListJSON) {
								programList.add(program.toString());
							}
							returnData.put(partNo, programList);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData;
	}

	public static Map<String, String> getIMSInfo(String imsNumber) {
		Map<String, String> returnData = new HashMap<String, String>();
		try {
			URL url = new URL("https://tms.vinfast.vn/rest/api/2/search?jql=key%20in%20(" + imsNumber + ")&fields=customfield_10226");
//			URL url = new URL("https://tms-uat.vinfast.vn/rest/api/2/search?jql=key%20in%20(" + imsNumber + ")&fields=customfield_10611");
			HttpURLConnection httpConn = null;
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setRequestProperty("Authorization", "Basic dmZwbG06VmZAUGxtITIwMjM=");
			httpConn.setConnectTimeout(60000);
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP Error code : " + httpConn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
			BufferedReader br = new BufferedReader(in);

			StringBuilder sbt = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sbt.append(output);
			}
			JSONObject json = new JSONObject(sbt.toString());
			JSONArray issuesArray = json.getJSONArray("issues");
			for (int i = 0; i < issuesArray.length(); i++) {
				JSONObject issue = issuesArray.getJSONObject(i);
				String issueKey = issue.getString("key");
				String customFieldValue = "";

				try {
					JSONObject fields = issue.getJSONObject("fields");
					if (fields.has("customfield_10226")) {
						JSONObject customField = fields.getJSONObject("customfield_10226");
						customFieldValue = customField.getString("value");
					}
				} catch (Exception e1) {
				}

				returnData.put(issueKey, customFieldValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnData;
	}
}
