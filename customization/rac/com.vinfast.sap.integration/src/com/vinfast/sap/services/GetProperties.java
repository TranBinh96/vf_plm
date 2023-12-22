package com.vinfast.sap.services;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;

public class GetProperties {

	DataManagementService dmService = null ;
	TCSession session = null ;

	public GetProperties(TCSession theSession)
	{
		this.session = theSession ;
		this.dmService = DataManagementService.getService(this.session);
	}
	public TCComponentItemRevision getBOMItemRev(TCComponent sourceBOMLine)
	{
		TCComponentItemRevision targetRev = null ;
		TCComponentBOMLine line = (TCComponentBOMLine)sourceBOMLine ;
		try
		{
			targetRev = line.getItemRevision() ;
			return targetRev ;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return targetRev ;
	}

	public String getObjectPropertyValue(String propertyName , TCComponent Object)
	{
		String prop_Value = "";
		try {
			prop_Value = Object.getProperty(propertyName).toString();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop_Value ;
	}

	//BOM LINE DATA
	public String getBOMLinePropertyValue(String propertyName , TCComponentBOMLine parentBOM)
	{

		String prop_Value = "";
		try {
			TCComponentItemRevision rev = parentBOM.getItemRevision();
			prop_Value = rev.getProperty(propertyName).toString();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop_Value ;
	}

	public String getBOMLineValue(String propertyName , TCComponentBOMLine childBOM)
	{
		String prop_Value = null;
		try {
			//childBOM.loadState();
			//childBOM.refresh();
			childBOM.getAllTCProperties();
			
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop_Value ;
	}

	public TCComponent getTargetComp(String relation , TCComponent Object)
	{
		TCComponent tObject = Object ;
		String arr[] = relation.split(":") ;
		loop:
		for (int inx = 0 ; inx < arr.length ; inx ++)
		{
			String temp1 = arr[inx] ;
			String temp2[] = temp1.split(",") ;
			for (int jnx = 0 ; jnx < temp1.length() ; jnx ++)
			{
				String parseRelation  = temp2[0] ;
				String targetObject = temp2[1] ;

				try
				{
					TCComponent newTarget[] = tObject.getRelatedComponents(parseRelation) ;
					for (int knx = 0 ; knx < newTarget.length ; knx ++)
					{
						if (newTarget[knx].getType().equals(targetObject))
						{
							tObject = newTarget[knx] ;
							continue loop ;
						}
					}
				}
				catch(TCException ex)
				{
					ex.printStackTrace();
				}

			}
		}
		return tObject ;
	}

}
