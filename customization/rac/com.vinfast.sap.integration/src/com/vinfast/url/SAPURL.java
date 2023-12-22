package com.vinfast.url;

public class SAPURL {
	
	public static final String FAM_NAMESPACE			= "http://plm.com/VehicleFamilyOption";
	public static final String FAM_TAG					= "veh";
	public static final String FAM_HEADER				= "MT_VFO_REQ";
	public static final String FAM_HEADER_TAG			= "veh:MT_VFO_REQ";
	
	public static final String RUL_NAMESPACE			= "http://plm.com/VehicleConfiguration";
	public static final String RUL_TAG					= "veh";
	public static final String RUL_HEADER				= "MT_VEHICONF_PLM_SEND";
	public static final String RUL_HEADER_TAG			= "veh:MT_VEHICONF_PLM_SEND";
	
	public static final String ECN_NAMESPACE			= "http://plm.com/EngineerChange_Car";
	public static final String ECN_TAG					= "eng";
	public static final String ECN_HEADER				= "MT_ECN_PLM_REQ";
	public static final String ECN_HEADER_TAG			= "eng:MT_ECN_PLM_REQ";
	
	public static final String MAT_NAMESPACE			= "http://plm.com/MaterialMaster_Car";
	public static final String MAT_TAG					= "mat";
	public static final String MAT_HEADER				= "MT_MATERIAL_PLM_REQ";
	public static final String MAT_HEADER_TAG			= "mat:MT_MATERIAL_PLM_REQ";
	
	public static final String ASSY_BOM_NAMESPACE		= "http://plm.com/BOMCreate_Car";
	public static final String ASSY_BOM_TAG				= "bom";
	public static final String ASSY_BOM_HEADER			= "MT_BOM_PLM_REQ";
	public static final String ASSY_BOM_HEADER_TAG		= "bom:MT_BOM_PLM_REQ";
	
	public static final String ASSY_BOP_NAMESPACE		= "http://plm.com/AssemblyCompAllocation";
	public static final String ASSY_BOP_TAG				= "ass";
	public static final String ASSY_BOP_HEADER			= "MT_ACA_PLM_SEND";
	public static final String ASSY_BOP_HEADER_TAG		= "ass:MT_ACA_PLM_SEND";
	
	public static final String SUP_BOM_NAMESPACE		= "http://plm.com/VehicleBOM";
	public static final String SUP_BOM_TAG				= "veh";
	public static final String SUP_BOM_HEADER			= "MT_PLM_VEHICLEBOM_SEND";
	public static final String SUP_BOM_HEADER_TAG		= "veh:MT_PLM_VEHICLEBOM_SEND";
	
	public static final String SUP_BOP_NAMESPACE		= "http://plm.com/VehicleCompAllocation";
	public static final String SUP_BOP_TAG				= "veh";
	public static final String SUP_BOP_HEADER			= "MT_VCARequest";
	public static final String SUP_BOP_HEADER_TAG		= "veh:MT_VCARequest";
	
	public static final String ES_BOM_NAMESPACE			= "http://plm.com/EScooterBOM";
	public static final String ES_BOM_TAG				= "esc";
	public static final String ES_BOM_HEADER			= "MT_PLM_ESCOOTERBOM_SEND";
	public static final String ES_BOM_HEADER_TAG		= "esc:MT_PLM_ESCOOTERBOM_SEND";	
	
	public static final String TRACKING_BOM_NAMESPACE	= "http://plm.com/BOM_Tracking";
	public static final String TRACKING_BOM_TAG			= "bom";
	public static final String TRACKING_BOM_HEADER		= "MT_BOM_TRACKING_PLM_REQ";
	public static final String TRACKING_BOM_HEADER_TAG	= "bom:MT_BOM_TRACKING_PLM_REQ";

	public SAPURL(){
		
	}
	
	public String familyWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_VFO_PLM_Synch_Out&interfaceNamespace=http://plm.com/VehicleFamilyOption";
	}
	
	public String rulesWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_VEHICONF_OUT&interfaceNamespace=http://plm.com/VehicleConfiguration";
	}

	public String changeWebserviceURL(String IP) {

		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_EngineerChange_Car_Out&interfaceNamespace=http://plm.com/EngineerChange_Car";
	}
	
	public String materialWebserviceURL(String IP) {

		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_ItemCreate_Car_Out&interfaceNamespace=http://plm.com/MaterialMaster_Car";
	}
	
	public String assybomWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_AssemblyMBOM_Out&interfaceNamespace=http://plm.com/BOMCreate_Car";
	}
	
	public String assybopWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_ACA_OUT&interfaceNamespace=http://plm.com/AssemblyCompAllocation";
	}
	
	public String superbomWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_VehicleBOM_Out&interfaceNamespace=http://plm.com/VehicleBOM";
	}
	
	public String superbopWebserviceURL(String IP) {
		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_VCA_PLM_Synch_Out&interfaceNamespace=http://plm.com/VehicleCompAllocation";
	}
	
	public String esbomWebserviceURL(String IP) {

		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_EScooterBOM_Out&interfaceNamespace=http://plm.com/EScooterBOM";
	}
	
	public String trackingWebserviceURL(String IP) {

		return "http://"+IP+"/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PLM&receiverParty=&receiverService=&interface=SI_BOM_TRACKING_OUT&interfaceNamespace=http://plm.com/BOM_Tracking";
	}

}
