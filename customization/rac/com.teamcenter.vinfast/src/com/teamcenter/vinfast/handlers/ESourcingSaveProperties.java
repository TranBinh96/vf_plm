package com.teamcenter.vinfast.handlers;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ServiceData;

public class ESourcingSaveProperties {

	TCComponentItemRevision itemRevision = null;
	TCSession session = null;
	Hashtable<String, String> savePropertyHash = null;
	Hashtable<String, Date> datePropertyHash = null;


	ESourcingSaveProperties(Hashtable<String, String> propertyHash,Hashtable<String, Date> datePropertyHash , TCSession theSession, TCComponentItemRevision itemRev){

		this.session = theSession;
		this.itemRevision = itemRev;
		this.savePropertyHash = propertyHash;
		this.datePropertyHash = datePropertyHash ;

		saveOperation();
	}

	public void saveOperation()
	{

		try
		{
			DataManagementService dmService = DataManagementService.getService(this.session);

			HashMap<String, Object> propertyMap = new HashMap<>();
			for(Entry<String, String> propEntry : savePropertyHash.entrySet()){
				System.out.println(propEntry.getKey() + " = " + propEntry.getValue());
				String[] propertyValues = { propEntry.getValue() };
				DataManagement.VecStruct vecStruct = new DataManagement.VecStruct();
				vecStruct.stringVec = propertyValues;
				propertyMap.put(propEntry.getKey(), vecStruct);
			}

			for(Entry<String, Date> propEntry : datePropertyHash.entrySet()){
				System.out.println(propEntry.getKey() + " = " + propEntry.getValue().toString());
				String[] propertyValues = { com.teamcenter.soa.client.model.Property.toDateString(propEntry.getValue()) };
				DataManagement.VecStruct vecStruct = new DataManagement.VecStruct();
				vecStruct.stringVec = propertyValues;
				propertyMap.put(propEntry.getKey(), vecStruct);
			}		 				

//			ServiceData serviceData = dmService.setProperties(new TCComponent[] { this.itemRevision }, propertyMap);
//			
//			if (serviceData.sizeOfPartialErrors() > 0)
//			{
//				ErrorStack errorStack = serviceData.getPartialError(serviceData.sizeOfPartialErrors()-1) ;
//				ErrorValue[] errorValue = errorStack.getErrorValues() ;
//
//				for (int inx = 0 ; inx < errorValue.length ; inx ++)
//				{
//					System.out.println("ERROR IN UPDATING IR " + errorValue[inx].getMessage() + "\n" );
//				}
//			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}



