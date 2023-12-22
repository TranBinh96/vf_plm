package com.teamcenter.vinfast.utils.lov;

import java.util.HashMap;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;

import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.PropDescriptorService;
import com.teamcenter.services.rac.core._2007_06.PropDescriptor.PropDescInfo;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.AttachedPropDescsResponse;

public class VFLovService {

	private static PropDescriptorService propService = null;
	private static VFLovValues lovValues = null;
	
	public static VFLovValues loadLOVValues(TCSession session, String object, String[] properties) {
		VFLovValues lovValues = null;
		try {
			propService = getPropDescService(session);
			PropDescInfo[] infos =  new PropDescInfo[1];
			infos[0] = new PropDescInfo();
			infos[0].typeName = object;
			infos[0].propNames= properties;
			AttachedPropDescsResponse response = propService.getAttachedPropDescs2(infos);
			if(response.serviceData.sizeOfPartialErrors() > 0 == false) {
				lovValues = new VFLovValues(response.inputTypeNameToPropDescOutput);
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lovValues;
	}
	
	public static VFLovValues loadFilterLOVValues(TCSession session, String objectType, String property, String dependentValue) {
	    try {
	      TCPropertyDescriptor propertyDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(property);
	      if (propertyDescriptor.hasLOVAttached()) {
	        TCComponentListOfValues attachedLOV = propertyDescriptor.getLOV();
	        TCComponentListOfValues parentInfo = attachedLOV.getListOfFilterOfValue(dependentValue);
	        lovValues = new VFLovValues(parentInfo);
	      } 
	    } catch (TCException e) {
	      e.printStackTrace();
	    } 
	    return lovValues;
	  }

	private static PropDescriptorService getPropDescService(TCSession session) {

		if(propService == null) {
			return PropDescriptorService.getService(session);
		}else {
			return propService;
		}
	}
	
	public static void setLovValue(Combo field, HashMap<Object, String> lovValues) {
		field.removeAll();
		for(Object key : lovValues.keySet()) {
			field.add(lovValues.get(key));
			field.setData(lovValues.get(key), key);
		}
	}
	public static void setLovValue(List field, HashMap<Object, String> lovValues) {
		field.removeAll();
		for(Object key : lovValues.keySet()) {
			field.add(lovValues.get(key));
			field.setData(lovValues.get(key), key);
		}
	}
}
