package com.vinfast.api.common.extensions;

import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONExtension {
    public static LinkedHashMap<String, String> loadProps(JSONArray jArray) {
        LinkedHashMap<String, String> output = new LinkedHashMap<>();
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jo = jArray.getJSONObject(i);
            output.put(jo.getString("headerName"), jo.getString("realName"));
        }
        return output;
    }

    public static LinkedHashMap<String, String[]> loadPropsPolicy(JSONArray jArray) {
        LinkedHashMap<String, String[]> output = new LinkedHashMap<>();
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jo = jArray.getJSONObject(i);
            String objectType = jo.getString("objectType");
            String properties = jo.getString("property");
            if (properties.contains(",")) {
                output.put(objectType, properties.split(","));
            } else {
                output.put(objectType, new String[]{properties});
            }
        }
        return output;
    }
}