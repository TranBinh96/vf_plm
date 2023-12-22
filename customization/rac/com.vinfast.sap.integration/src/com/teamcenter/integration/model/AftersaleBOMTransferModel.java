package com.teamcenter.integration.model;

import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.services.rac.core.DataManagementService;

public class AftersaleBOMTransferModel {
	private String level = "";
	private String itemID = "";	
	private String materialClass = "";	
	private String seqNumber = "";
	private String uniqueID = "";	
	private String partNameVIE = "";
	private String semiFinishedGood = "";
	private String partNameENG = "";
	private String colorCode = "";
	private String qty = "";
	private String uoM = "";
	private String figureNo = "";
	private String revNumber = "";
	private String year = "";
	private String epcVariant = "";
	private String orgPartNumber = "";
	private String suppressionCode = "";
	private String suppressionDesc = "";
	private String effectiveDate = "";
	private String validTillDate = "";
	private String firstVinNewPart = "";
	private String remarks = "";
	private String itemName = "";
	private String purchaseLvlVin= "";
	private String afterSaleRelevant ="";
	private String aftersalesCritical ="";
	private String afsPartVolume  ="";
	private String market ="";	
	private String releaseStatuses ="";
	private String moduleGroupEnglish ="";
	private String mainModuleEnglish ="";
	private String moduleName ="";
	private String supplierName ="";
	private String sorName ="";
	private String sorNumber ="";
	private String partMakeBuy ="";
	private String materialType ="";
	private String posID ="";
	private String coloredPart ="";
	private String torqueInformation ="";
	private boolean isValidate;
	private boolean isNoNeedTransfer;	
	private String message = "";

	public AftersaleBOMTransferModel(TCComponentBOMLine bomline, LinkedHashMap<String, String> superSessionCodeList, DataManagementService dmService) {
		if (bomline != null) {
			try {
				dmService.getProperties(new TCComponent[] { bomline },
						new String[] { "bl_level_starting_0","bl_sequence_no", "bl_clone_stable_occurrence_id", "bl_item_item_id","bl_item_vf4_item_name_vi", 
										"bl_rev_object_name", "bl_quantity", "bl_uom", "bl_rev_item_revision_id", "VF4_EPC_Variant", "bl_item_vf4_original_part_number",
										"bl_item_object_name","VL5_purchase_lvl_vf","bl_item_vf4_itm_after_sale_relevant","VF4_LNT_af_critical",
										"VF4_AFS_Part_Volumne","bl_item_vf4_orginal_part_number","VF4_supersessionCode","VF4_market","bl_rev_release_statuses",
										"VL5_module_group","VL5_main_module","VL5_module_name","bl_item_vf4_itm_supplier_name","bl_rev_vf4_sor_name_rev","VF4_remarks",
										"bl_rev_vf4_sor_number_rev","bl_item_vf4_item_make_buy","bl_item_vf4_itm_material_type","VL5_pos_id",
										"bl_rev_vl5_colored_part","bl_item_vl5_color_code","VL5_torque_inf" });
				
				materialClass = bomline.getPropertyDisplayableValue("");			
				level = bomline.getPropertyDisplayableValue("bl_level_starting_0");
				seqNumber = bomline.getPropertyDisplayableValue("bl_sequence_no");
				uniqueID = bomline.getPropertyDisplayableValue("bl_clone_stable_occurrence_id");
				itemID = bomline.getPropertyDisplayableValue("bl_item_item_id");
				partNameVIE = bomline.getPropertyDisplayableValue("bl_item_vf4_item_name_vi");
				semiFinishedGood = "";
				partNameENG = bomline.getPropertyDisplayableValue("bl_rev_object_name");
				colorCode = bomline.getPropertyDisplayableValue("");
				qty = bomline.getPropertyDisplayableValue("bl_quantity");
				uoM = bomline.getPropertyDisplayableValue("bl_uom");
				if (uoM.isEmpty() || uoM.compareToIgnoreCase("each") == 0)
					uoM = "EA";
				figureNo = "";
				revNumber = bomline.getPropertyDisplayableValue("bl_rev_item_revision_id");
				epcVariant = bomline.getPropertyDisplayableValue("VF4_EPC_Variant");
				orgPartNumber = bomline.getPropertyDisplayableValue("bl_item_vf4_orginal_part_number");
				suppressionDesc = bomline.getPropertyDisplayableValue("VF4_supersessionCode");
				if (!suppressionDesc.isEmpty() && superSessionCodeList != null) {
					suppressionCode = superSessionCodeList.getOrDefault(suppressionDesc, "");
				}
				effectiveDate = covertDate(bomline.getPropertyDisplayableValue("VF4_effective_date"));
				validTillDate = covertDate(bomline.getPropertyDisplayableValue("VF4_valid_till_date"));
				itemName = bomline.getPropertyDisplayableValue("bl_item_object_name");
				purchaseLvlVin= bomline.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
				afterSaleRelevant = bomline.getPropertyDisplayableValue("bl_item_vf4_itm_after_sale_relevant");
				aftersalesCritical = bomline.getPropertyDisplayableValue("VF4_LNT_af_critical");
				afsPartVolume  = bomline.getPropertyDisplayableValue("VF4_AFS_Part_Volumne");
				/* superSessionDescription = bomline.getPropertyDisplayableValue(""); */
				market = bomline.getPropertyDisplayableValue("VF4_market");
				releaseStatuses = bomline.getPropertyDisplayableValue("bl_rev_release_statuses");
				moduleGroupEnglish = bomline.getPropertyDisplayableValue("VL5_module_group");
				mainModuleEnglish = bomline.getPropertyDisplayableValue("VL5_main_module");
				moduleName = bomline.getPropertyDisplayableValue("VL5_module_name");
				supplierName = bomline.getPropertyDisplayableValue("bl_item_vf4_itm_supplier_name");
				sorName = bomline.getPropertyDisplayableValue("bl_rev_vf4_sor_name_rev");
				sorNumber = bomline.getPropertyDisplayableValue("bl_rev_vf4_sor_number_rev");
				partMakeBuy = bomline.getPropertyDisplayableValue("bl_item_vf4_item_make_buy");
				materialType = bomline.getPropertyDisplayableValue("bl_item_vf4_itm_material_type");
				//defaultUnitMeasure = bomline.getPropertyDisplayableValue("bl_item_uom_tag");
				posID = bomline.getPropertyDisplayableValue("VL5_pos_id");
				//variantFormula = bomline.getPropertyDisplayableValue("bl_formula");
				colorCode = bomline.getPropertyDisplayableValue("bl_item_vl5_color_code");
				coloredPart = bomline.getPropertyDisplayableValue("bl_rev_vl5_colored_part");				
				torqueInformation = bomline.getPropertyDisplayableValue("VL5_torque_inf");		
				remarks = bomline.getPropertyDisplayableValue("VF4_remarks");
//				firstVinNewPart = bomline.getPropertyDisplayableValue("");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getMaterialClass() {
		return materialClass;
	}

	public void setMaterialClass(String materialClass) {
		this.materialClass = materialClass;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(String seqNumber) {
		this.seqNumber = seqNumber;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getPartNameVIE() {
		return partNameVIE;
	}

	public void setPartNameVIE(String partNameVIE) {
		this.partNameVIE = partNameVIE;
	}

	public String getSemiFinishedGood() {
		return semiFinishedGood;
	}

	public void setSemiFinishedGood(String semiFinishedGood) {
		this.semiFinishedGood = semiFinishedGood;
	}

	public String getPartNameENG() {
		return partNameENG;
	}

	public void setPartNameENG(String partNameENG) {
		this.partNameENG = partNameENG;
	}

	public String getColorCode() {
		return colorCode;
	}

	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public String getUoM() {
		return uoM;
	}

	public void setUoM(String uoM) {
		this.uoM = uoM;
	}

	public String getFigureNo() {
		return figureNo;
	}

	public void setFigureNo(String figureNo) {
		this.figureNo = figureNo;
	}

	public String getRevNumber() {
		return revNumber;
	}

	public void setRevNumber(String revNumber) {
		this.revNumber = revNumber;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getEpcVariant() {
		return epcVariant;
	}

	public void setEpcVariant(String epcVariant) {
		this.epcVariant = epcVariant;
	}

	public String getOrgPartNumber() {
		return orgPartNumber;
	}

	public void setOrgPartNumber(String orgPartNumber) {
		this.orgPartNumber = orgPartNumber;
	}

	public String getSuppressionCode() {
		return suppressionCode;
	}

	public void setSuppressionCode(String suppressionCode) {
		this.suppressionCode = suppressionCode;
	}

	public String getSuppressionDesc() {
		return suppressionDesc;
	}

	public void setSuppressionDesc(String suppressionDesc) {
		this.suppressionDesc = suppressionDesc;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getValidTillDate() {
		return validTillDate;
	}

	public void setValidTillDate(String validTillDate) {
		this.validTillDate = validTillDate;
	}

	public String getFirstVinNewPart() {
		return firstVinNewPart;
	}

	public void setFirstVinNewPart(String firstVinNewPart) {
		this.firstVinNewPart = firstVinNewPart;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public boolean isValidate() {
		return isValidate;
	}

	public void setValidate(boolean isValidate) {
		this.isValidate = isValidate;
	}

	public boolean isNoNeedTransfer() {
		return isNoNeedTransfer;
	}

	public void setNoNeedTransfer(boolean isNoNeedTransfer) {
		this.isNoNeedTransfer = isNoNeedTransfer;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getPurchaseLvlVin() {
		return purchaseLvlVin;
	}

	public void setPurchaseLvlVin(String purchaseLvlVin) {
		this.purchaseLvlVin = purchaseLvlVin;
	}

	public String getAfterSaleRelevant() {
		return afterSaleRelevant;
	}

	public void setAfterSaleRelevant(String afterSaleRelevant) {
		this.afterSaleRelevant = afterSaleRelevant;
	}

	public String getAftersalesCritical() {
		return aftersalesCritical;
	}

	public void setAftersalesCritical(String aftersalesCritical) {
		this.aftersalesCritical = aftersalesCritical;
	}

	public String getAfsPartVolume() {
		return afsPartVolume;
	}

	public void setAfsPartVolume(String afsPartVolume) {
		this.afsPartVolume = afsPartVolume;
	}


	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getReleaseStatuses() {
		return releaseStatuses;
	}

	public void setReleaseStatuses(String releaseStatuses) {
		this.releaseStatuses = releaseStatuses;
	}

	public String getModuleGroupEnglish() {
		return moduleGroupEnglish;
	}

	public void setModuleGroupEnglish(String moduleGroupEnglish) {
		this.moduleGroupEnglish = moduleGroupEnglish;
	}

	public String getMainModuleEnglish() {
		return mainModuleEnglish;
	}

	public void setMainModuleEnglish(String mainModuleEnglish) {
		this.mainModuleEnglish = mainModuleEnglish;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getSorName() {
		return sorName;
	}

	public void setSorName(String sorName) {
		this.sorName = sorName;
	}

	public String getSorNumber() {
		return sorNumber;
	}

	public void setSorNumber(String sorNumber) {
		this.sorNumber = sorNumber;
	}

	public String getPartMakeBuy() {
		return partMakeBuy;
	}

	public void setPartMakeBuy(String partMakeBuy) {
		this.partMakeBuy = partMakeBuy;
	}

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}


	public String getPosID() {
		return posID;
	}

	public void setPosID(String posID) {
		this.posID = posID;
	}


	public String getColoredPart() {
		return coloredPart;
	}

	public void setColoredPart(String coloredPart) {
		this.coloredPart = coloredPart;
	}

	public String getTorqueInformation() {
		return torqueInformation;
	}

	public void setTorqueInformation(String torqueInformation) {
		this.torqueInformation = torqueInformation;
	}
	public String getTransferData() {
		StringBuilder output = new StringBuilder();

		output.append("<DATA>");
		createChildNode("MaterialClass", materialClass, output);
		createChildNode("Level", level, output);
		createChildNode("SeqNumber", seqNumber, output);
		createChildNode("UniqueID", uniqueID, output);
		createChildNode("ItemID", itemID, output);
		createChildNode("PartNameVIE", materialClass, output);
		createChildNode("SemiFinishedGood", semiFinishedGood, output);
		createChildNode("PartNameENG", partNameENG, output);
		createChildNode("ColorCode", colorCode, output);
		createChildNode("Qty", qty, output);
		createChildNode("UoM", uoM, output);
		createChildNode("FigureNo", figureNo, output);
		createChildNode("RevNumber", revNumber, output);
		createChildNode("Year", year, output);
		createChildNode("EPCVariant", epcVariant, output);
		createChildNode("OrgPartNumber", orgPartNumber, output);
		createChildNode("SuppressionCode", suppressionCode, output);
		createChildNode("SuppressionDesc", suppressionDesc, output);
		createChildNode("EffectiveDate", effectiveDate, output);
		createChildNode("ValidTillDate", validTillDate, output);
		createChildNode("FirstVinNewPart", firstVinNewPart, output);
		createChildNode("Remarks", remarks, output);
		createChildNode("AftersalesCritical",aftersalesCritical , output);		
		createChildNode("AFSPartVolume",afsPartVolume , output);
		createChildNode("Market",market, output);
		createChildNode("PurchaseLevelVinFast",purchaseLvlVin, output);
		createChildNode("IsAfterSaleRelevant",afterSaleRelevant, output);
		createChildNode("ReleaseStatus",releaseStatuses, output);
		createChildNode("ModuleGroupEnglish",moduleGroupEnglish , output);
		createChildNode("MainModuleEnglish",mainModuleEnglish, output);
		createChildNode("ModuleName",moduleName, output);
		createChildNode("SupplierName",supplierName, output);
		createChildNode("SORName",sorName, output);
		createChildNode("SORNumber",sorNumber, output);
		createChildNode("PartMakeBuy",partMakeBuy, output);
		createChildNode("MaterialType",materialType, output);		
		createChildNode("POSID",posID, output);
		createChildNode("ColoredPart",coloredPart, output);
		createChildNode("TorqueInformation",torqueInformation, output);
		output.append("</DATA>");
		return output.toString().replaceAll("&", "");
	}

	private void createChildNode(String key, String value, StringBuilder output) {
		if (value.isEmpty())
			output.append("<" + key + "/>");
		else
			output.append("<" + key + ">" + value + "</" + key + ">");
	}
	private String covertDate(String date) {

        String strdate [] =date.split("-");
        if(strdate[0].length()==1)
        	strdate[0] = "0"+strdate[0];
        return strdate[0]+"-"+strdate[1]+"-"+strdate[2];
	}
}
