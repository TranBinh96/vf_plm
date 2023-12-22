package com.vinfast.sap.util;

import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.soa.common.ObjectPropertyPolicy;

public class SetObjectPolicy {


	public void setPropertyPolicy(TCSession session){ 

		SessionService sessionService = SessionService.getService(session);

		ObjectPropertyPolicy policy = new ObjectPropertyPolicy();

		policy.addType("Sc0DesignXChange", new String[]{ "item_id","revision_list","object_string","item_revision"});
		policy.addType("Sc0DesignXChangeRevision", new String[]{"vm0Status","object_name","object_desc","vm0ExchangeLines"});
		policy.addType("User", new String[]{ "default_group","object_string" });
		policy.addType("VF4_Design", new String[]{"item_id","object_name","revision_list"});
		policy.addType("VF4_DesignRevision", new String[]{"item_id","item_revision_id","object_name","object_type"});
		policy.addType("RevisionRule", new String[]{"current_name"});
		policy.addType("Vendor", new String[]{"item_id","object_name","ContactInCompany"});
		policy.addType("CompanyContact", new String[]{"object_name","sc0VendorName","email_address"});
		policy.addType("VF3_vfitem", new String[]{"Smc0HasVariantConfigContext","object_desc","object_type","IMAN_master_form"});
		policy.addType("ItemRevision", new String[]{"item_id","object_type","release_status_list"});
		policy.addType("VF3_vfitemRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("VF3_vfitemMaster", new String[]{"user_data_2"});
		policy.addType("VF4_CostRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("Mfg0MEPlantBOPRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("Mfg0MEProcLineRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("Mfg0MEProcStatnRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("MEOPRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("VF4_ME_ShopRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("MELineRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("MEStationRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("VF4_str_partRevision", new String[]{"item_id","object_name","items_tag","object_type","release_status_list"});
		policy.addType("Cfg0ProductItem", new String[]{"item_id"});
		policy.addType("BOMLine", new String[] {"bl_parent","bl_rev_item_id","bl_item_item_id","VF4_bomline_id","bl_rev_object_type","bl_quantity","VF3_purchase_lvl_vf",
				"VL5_purchase_lvl_vf","bl_formula","VF4_manuf_code","Vf8_FamilyAddress","Vf8_Hand"});
		policy.addType("Mfg0BvrPart",new String[] {"bl_parent"});
		policy.addType("Mfg0BvrOperation",new String[] {"bl_parent","bl_revision","bl_rev_item_revision_id","vf4_operation_type"});
		policy.addType("Mfg0BvrProcessStation",new String[] {"bl_rev_object_name","bl_revision","object_type","bl_parent"});
		policy.addType("Mfg0BvrProcessLine",new String[] {"bl_rev_object_name","bl_revision","object_type","bl_parent"});
		policy.addType("Mfg0BvrPlantBOP",new String[] {"bl_rev_object_name"});
		
		sessionService.setObjectPropertyPolicy(policy); 

	}
	
	public void setContextPolicy(TCSession session){ 

		SessionService sessionService = SessionService.getService(session);

		ObjectPropertyPolicy policy = new ObjectPropertyPolicy();

		policy.addType("Cfg0ConfiguratorPerspective",new String[] {"cfg0Models","cfg0OptionValues"});
		policy.addType("Cfg0ProductModel",new String[] {"cfg0ModelFamily"});
		policy.addType("Cfg0LiteralOptionValue",new String[] {"cfg0OptionFamily"});
		policy.addType("Cfg0LiteralValueFamily",new String[] {"cfg0FamilyGroups"});
		
		sessionService.setObjectPropertyPolicy(policy); 

	}
	
	public void setOperationPolicy(TCSession session){ 

		SessionService sessionService = SessionService.getService(session);

		ObjectPropertyPolicy policy = new ObjectPropertyPolicy();
		
		policy.addType("BOMLine",new String[] {"cfg0Models","cfg0OptionValues"});
		policy.addType("MEOPRevision",new String[] {"cfg0Models","cfg0OptionValues"});
		sessionService.setObjectPropertyPolicy(policy); 

	}
	
	public void setChangePolicy(TCSession session){ 

		SessionService sessionService = SessionService.getService(session);

		ObjectPropertyPolicy policy = new ObjectPropertyPolicy();
		
		policy.addType("Vf6_MCN",new String[] {"item_id","vf6_action","vf6_sap_plant","object_name","vf6_change_reason","vf6_change_type","vf6_coordination_code_cm","vf6_disposal_code","vf6_model_year","vf6_model_year","vf6_effectivity_date","vf6_comments"});
		sessionService.setObjectPropertyPolicy(policy); 

	}
}
