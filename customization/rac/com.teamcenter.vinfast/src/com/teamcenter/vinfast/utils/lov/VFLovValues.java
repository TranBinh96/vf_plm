package com.teamcenter.vinfast.utils.lov;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDesc;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDescOutput2;

public class VFLovValues {

	private LinkedHashMap<String,LinkedHashMap<String,LinkedHashMap<Object, String>>> lovValuesMap =  new LinkedHashMap<String,LinkedHashMap<String,LinkedHashMap<Object, String>>>();
	private LinkedHashMap<Object, String> lovFilterValuesMap = new LinkedHashMap<Object, String>();

	public VFLovValues(Map<String,PropDescOutput2[]> output) {

		try {
			for(String objectType : output.keySet()) {
				LinkedHashMap<String,LinkedHashMap<Object, String>> objectMap = null;
				if(lovValuesMap.containsKey(objectType)) {
					objectMap = lovValuesMap.get(objectType);
				}else {
					objectMap =  new LinkedHashMap<String,LinkedHashMap<Object, String>>();
					lovValuesMap.put(objectType, objectMap);
				}
				PropDescOutput2[] outputValues = output.get(objectType);
				for(PropDescOutput2 outputValue : outputValues) {
					LinkedHashMap<Object, String> propMap = null;
					PropDesc propDescription = outputValue.propertyDesc;
					TCComponentListOfValues listOfValues = propDescription.lov;
					ListOfValuesInfo info = listOfValues.getListOfValues();
					Object[] objects = info.getListOfValues();
					if(objects.length != 0) {
						propMap =  new LinkedHashMap<Object, String>();
						for(int i=0; i<objects.length;i++) {
							propMap.put(objects[i], info.getDisplayableValue(objects[i]));
						}
					}
					objectMap.put(outputValue.propName, propMap);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VFLovValues(TCComponentListOfValues listOfValues) {
		try {
			ListOfValuesInfo info = listOfValues.getListOfValues();
			Object[] objects = info.getListOfValues();
			if (objects.length != 0) {
				byte b;
				int i;
				Object[] arrayOfObject;
				for (i = (arrayOfObject = objects).length, b = 0; b < i; ) {
					Object object = arrayOfObject[b];
					lovFilterValuesMap.put(object, info.getDisplayableValue(object));
					b++;
				} 
			} 
		} catch (TCException e) {
			e.printStackTrace();
		} 
	}

	public LinkedHashMap<Object, String> getLOVValue(String objectType, String property) {

		LinkedHashMap<String, LinkedHashMap<Object, String>> propMap = lovValuesMap.get(objectType);
		return propMap.get(property);
	}

	public HashMap<Object, String> getFilterLOVValue() {
		return lovFilterValuesMap;
	}
}
