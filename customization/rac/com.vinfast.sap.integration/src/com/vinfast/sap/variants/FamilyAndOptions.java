package com.vinfast.sap.variants;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.util.PropertyDefines;

public class FamilyAndOptions {

	public static void getModels(DataManagementService dmCoreService, TCComponent modelValue, ArrayList<LinkedHashMap<String,String>> familyOptionValues){

		try
		{
			//SEND MODELS TO SAP
			TCComponent[] modelFamily = modelValue.getRelatedComponents(PropertyDefines.CFG_MODEL_FAMILY);
			if(modelFamily != null) {
				dmCoreService.getProperties(modelFamily, new String[]{PropertyDefines.CFG_OBJECT_ID,PropertyDefines.ITEM_DESC});
				for(TCComponent family : modelFamily) {
					LinkedHashMap<String,String> modelMap = new LinkedHashMap<String,String>();
					modelMap.put("FAMILYGROUP",family.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim());
					modelMap.put("FAMILYGROUPNAME",family.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim()) ;
					modelMap.put("FAMILYID",family.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim()) ;
					modelMap.put("FAMILYNAME",family.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim()) ;
					modelMap.put("OPTIONID",modelValue.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim()) ;
					modelMap.put("OPTIONIDNAME",modelValue.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim());
					familyOptionValues.add(modelMap);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void getOptionValues(DataManagementService dmCoreService, TCComponent optionValue, ArrayList<LinkedHashMap<String,String>> familyOptionValues){
		try
		{
			TCComponent[] OptionFamily = optionValue.getRelatedComponents(PropertyDefines.CFG_OPTION_FAMILY);
			if(OptionFamily != null) {
				dmCoreService.getProperties(OptionFamily, new String[]{PropertyDefines.CFG_OBJECT_ID,PropertyDefines.ITEM_DESC});
				for(TCComponent family : OptionFamily) {
					TCComponent[] familyGroup = family.getRelatedComponents(PropertyDefines.CFG_FAMILY_GROUP);
					if(familyGroup != null) {
						for(TCComponent group : familyGroup) {
							if(FamilyAndOptionsTransfer.isValidFamily(group.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID))) {
								LinkedHashMap<String,String> optionMap = new LinkedHashMap<String,String>();
								optionMap.put("FAMILYGROUP",group.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim());
								optionMap.put("FAMILYGROUPNAME",group.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim()) ;
								optionMap.put("FAMILYID",family.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim()) ;
								optionMap.put("FAMILYNAME",family.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim()) ;
								optionMap.put("OPTIONID",optionValue.getPropertyDisplayableValue(PropertyDefines.CFG_OBJECT_ID).trim()) ;
								optionMap.put("OPTIONIDNAME",optionValue.getPropertyDisplayableValue(PropertyDefines.ITEM_DESC).trim()) ;
								familyOptionValues.add(optionMap);
							}
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}