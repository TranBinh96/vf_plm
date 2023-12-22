package com.teamcenter.vinfast.handlers;

public class CostObject  implements Cloneable{
	private String partNum;
	private String partName;
	private String procurementLvl;
	private String designAssumption;
	private String sorNum;
	private String sorName;
	private String moduleName;
	private String CL;
	private String buyer;
	private String pieceCostTarget;
	private String pieceCostTargetCurr;
	private String EDDCostTarget;
	private String EDDCostTargetCurr;
	private String toolingCostTarget;
	private String toolingCostTargetCurr;
	private String owner;
	private String VNASEANBaseICE;
	private String VNASEANMiddleICE;
	private String VNASEANHighICE;
	private String VNASEANBaseBEV;
	private String VNASEANMiddleBEV;
	private String VNASEANHighBEV;
	private String USHighBEV;
	private String USBaseBev;
	private String commonPart;
	private String platformPart;
	private String bomLevel;
	private boolean rightAccess;
	private boolean Found;
	private String supplierType;
	private double weight;
	private String material;
	private String targetCostDateModify;
	private String targetCostCreationDate;
	private String mainModuleGrp;
	private String moduleGrp;
	private double estimatedWeight;
	private double measuredWeight;
	private String costValid;
	private String vehicleUsageValid;
	/*interim piece cost*/
	private String pieceCostDateModify;
	private String pieceCostValueStatus;
	private String pieceCostCurr;
	private String supplierLogicCost;
	private String supplierPackageAmount;
	private String totalCost;
	private String totalCostCurr;
	
	/*final piece cost*/
	private String finalPieceCostDateModify;
	private String finalPieceCostValueStatus;
	private String finalPieceCostCurr;
	private String finalSupplierLogicCost;
	private String finalSupplierPackageAmount;
	private String finalTotalCost;
	private String finalTotalCostCurr;
	
	/*buyer estimate piece cost*/
	private String buyerPieceCostDateModify;
	private String buyerPieceCostValueStatus;
	private String buyerPieceCostCurr;
	private String buyerSupplierLogicCost;
	private String buyerSupplierPackageAmount;
	private String buyerTotalCost;
	private String buyerTotalCostCurr;
	
	/*engineer estimate piece cost*/
	private String engineerPieceCostDateModify;
	private String engineerPieceCostValueStatus;
	private String engineerPieceCostCurr;
	private String engineerSupplierLogicCost;
	private String engineerSupplierPackageAmount;
	private String engineerTotalCost;
	private String engineerTotalCostCurr;
	
	/*tooling cost*/
	private String EdndCurr;
	private String EdndCost;
	private String protoToolingCurr;
	private String protoToolingValue;
	private String protoPiecePrice;
	private String protoPiecePriceCurr;
	private String toolingInvestValue;
	private String toolingInvestCurr;
	private String refurToolingCurr;
	private String refurToolingValue;
	private String facilityInvestAmount;
	private String facilityInvestCurr;
	private String miscellaneousCost;
	private String miscellaneousCurr;
	private String qualityFinance;
	private String RNOReferNumber;
	private String industrializeCurr;
	private String industrializeCost;
	private String revisionCreationDate;
	
	public CostObject() {
		this.partNum 			= "";
		this.partName 			= "";
		this.procurementLvl 	= "";
		this.designAssumption 	= "";
		this.sorNum 			= "";
		this.sorName 			= "";
		this.moduleName 		= "";
		this.CL					= "";
		this.buyer				= "";
		this.pieceCostTarget	= "0";
		this.pieceCostTargetCurr = "";
		this.EDDCostTarget		= "0";
		this.EDDCostTargetCurr	= "";
		this.toolingCostTarget	= "0";
		this.toolingCostTargetCurr = "";
		this.VNASEANBaseBEV		= "";
		this.VNASEANBaseICE		= "";
		this.VNASEANHighBEV		= "";
		this.VNASEANHighICE		= "";
		this.VNASEANMiddleBEV	= "";
		this.VNASEANMiddleICE	= "";
		this.platformPart		= "";
		this.USBaseBev			= "";
		this.USHighBEV			= "";
		this.commonPart		= "";
		this.bomLevel			= "";
		this.owner				= "";
		this.rightAccess		= false;
		this.Found				= false;
		this.targetCostDateModify	= "";
		this.targetCostCreationDate = "";
		this.costValid			= "";
		this.vehicleUsageValid	= "";
		//VF4_PieceCostForm
		this.pieceCostDateModify	= "";
		this.facilityInvestAmount 	= "0";
		this.facilityInvestCurr		= "";
		this.miscellaneousCost		= "0";
		this.miscellaneousCurr		= "";
		this.pieceCostValueStatus	= "0";
		this.pieceCostCurr			= "";
		this.protoPiecePrice		= "0";
		this.protoPiecePriceCurr	= "";
		this.protoToolingCurr		= "";
		this.protoToolingValue		= "0";
		this.qualityFinance			= "";
		this.refurToolingCurr		= "";
		this.refurToolingValue		= "0";
		this.RNOReferNumber			= "";
		this.supplierLogicCost		= "0";
		this.supplierPackageAmount	= "0";
		this.toolingInvestValue		= "0";
		this.toolingInvestCurr		= "";
		this.totalCost				= "0";
		this.totalCostCurr			= "";
		this.EdndCurr				= "";
		this.EdndCost				= "0";
		this.mainModuleGrp			= "";
		this.moduleGrp				= "";
		this.finalPieceCostDateModify = "";
		this.finalPieceCostValueStatus = "0";
		this.finalPieceCostCurr = "";
		this.finalSupplierLogicCost = "0";
		this.finalSupplierPackageAmount = "0";
		this.finalTotalCost = "0";
		this.finalTotalCostCurr = "";
		
		/*buyer estimate piece cost*/
		this.buyerPieceCostDateModify = "";
		this.buyerPieceCostValueStatus = "0";
		this.buyerPieceCostCurr = "";
		this.buyerSupplierLogicCost = "0";
		this.buyerSupplierPackageAmount = "0";
		this.buyerTotalCost = "0";
		this.buyerTotalCostCurr = "";
		
		/*engineer estimate piece cost*/
		this.engineerPieceCostDateModify = "";
		this.engineerPieceCostValueStatus = "0";
		this.engineerPieceCostCurr = "";
		this.engineerSupplierLogicCost = "0";
		this.engineerSupplierPackageAmount = "0";
		this.engineerTotalCost = "0";
		this.engineerTotalCostCurr = "";
		this.industrializeCost = "0";
		this.industrializeCurr = "";
		
		this.revisionCreationDate = "";
		this.material = "";
	}
	
	public CostObject(String label) {
		this.partNum 			= "";
		this.partName 			= "";
		this.procurementLvl 	= "";
		this.designAssumption 	= "";
		this.sorNum 			= "";
		this.sorName 			= "";
		this.moduleName 		= "";
		this.CL					= "";
		this.buyer				= "";
		this.pieceCostTarget		= label;
		this.pieceCostTargetCurr 	= label;
		this.EDDCostTarget			= label;
		this.EDDCostTargetCurr		= label;
		this.toolingCostTarget		= label;
		this.toolingCostTargetCurr 	= label;
		this.VNASEANBaseBEV		= "";
		this.VNASEANBaseICE		= "";
		this.VNASEANHighBEV		= "";
		this.VNASEANHighICE		= "";
		this.VNASEANMiddleBEV	= "";
		this.VNASEANMiddleICE	= "";
		this.platformPart		= "";
		this.USBaseBev			= "";
		this.USHighBEV			= "";
		this.commonPart		= "";
		this.bomLevel			= "";
		this.owner				= "";
		this.rightAccess		= false;
		this.Found				= false;
		this.targetCostDateModify	= "";
		this.targetCostCreationDate = "";
		//VF4_PieceCostForm
		this.pieceCostDateModify	= "";
		this.facilityInvestAmount 	= label;
		this.facilityInvestCurr		= label;
		this.miscellaneousCost		= label;
		this.miscellaneousCurr		= label;
		this.pieceCostValueStatus	= label;
		this.pieceCostCurr			= label;
		this.protoPiecePrice		= label;
		this.protoPiecePriceCurr	= label;
		this.protoToolingCurr		= label;
		this.protoToolingValue		= label;
		this.qualityFinance			= label;
		this.refurToolingCurr		= label;
		this.refurToolingValue		= label;
		this.RNOReferNumber			= label;
		this.supplierLogicCost		= label;
		this.supplierPackageAmount	= label;
		this.toolingInvestValue		= label;
		this.toolingInvestCurr		= label;
		this.totalCost				= label;
		this.totalCostCurr			= label;
		this.EdndCurr				= label;
		this.EdndCost				= label;
		this.mainModuleGrp			= "";
		this.moduleGrp				= "";
		this.finalPieceCostDateModify = "";
		this.finalPieceCostValueStatus 	= label;
		this.finalPieceCostCurr 		= label;
		this.finalSupplierLogicCost 	= label;
		this.finalSupplierPackageAmount = label;
		this.finalTotalCost 			= label;
		this.finalTotalCostCurr			= label;
	
		/*buyer estimate piece cost*/
		this.buyerPieceCostDateModify = "";
		this.buyerPieceCostValueStatus 	= label;
		this.buyerPieceCostCurr 		= label;
		this.buyerSupplierLogicCost 	= label;
		this.buyerSupplierPackageAmount = label;
		this.buyerTotalCost 			= label;
		this.buyerTotalCostCurr 		= label;
		
		/*engineer estimate piece cost*/
		this.engineerPieceCostDateModify = "";
		this.engineerPieceCostValueStatus 	= label;
		this.engineerPieceCostCurr 			= label;
		this.engineerSupplierLogicCost 		= label;
		this.engineerSupplierPackageAmount 	= label;
		this.engineerTotalCost 				= label;
		this.engineerTotalCostCurr 			= label;
		this.industrializeCost 				= label;
		this.industrializeCurr 				= label;
		this.costValid = "";
		this.vehicleUsageValid	= "";
		this.revisionCreationDate = "";
		this.material = "";
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException{
		return super.clone();
		
	}
	
	public String getVehicleUsageValid() {
		return vehicleUsageValid;
	}

	public void setVehicleUsageValid(String vehicleUsageValid) {
		this.vehicleUsageValid = vehicleUsageValid;
	}

	public String getCostValid() {
		return costValid;
	}

	public void setCostValid(String costValid) {
		this.costValid = costValid;
	}

	public String getIndustrializeCurr() {
		return industrializeCurr;
	}

	public void setIndustrializeCurr(String industrializeCurr) {
		this.industrializeCurr = industrializeCurr;
	}

	public String getIndustrializeCost() {
		return industrializeCost;
	}

	public void setIndustrializeCost(String industrializeCost) {
		this.industrializeCost = industrializeCost;
	}

	public String getPlatformPart() {
		return platformPart;
	}

	public void setPlatformPart(String platformPart) {
		this.platformPart = platformPart;
	}

	public double getEstimatedWeight() {
		return estimatedWeight;
	}

	public void setEstimatedWeight(double estimatedWeight) {
		this.estimatedWeight = estimatedWeight;
	}

	public double getMeasuredWeight() {
		return measuredWeight;
	}

	public void setMeasuredWeight(double measuredWeight) {
		this.measuredWeight = measuredWeight;
	}

	public String getMainModuleGrp() {
		return mainModuleGrp;
	}

	public void setMainModuleGrp(String mainModuleGrp) {
		this.mainModuleGrp = mainModuleGrp;
	}

	public String getModuleGrp() {
		return moduleGrp;
	}

	public void setModuleGrp(String moduleGrp) {
		this.moduleGrp = moduleGrp;
	}

	public String getRevisionCreationDate() {
		return revisionCreationDate;
	}

	public void setRevisionCreationDate(String revisionCreationDate) {
		this.revisionCreationDate = revisionCreationDate;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getFinalPieceCostDateModify() {
		return finalPieceCostDateModify;
	}

	public void setFinalPieceCostDateModify(String finalPieceCostDateModify) {
		this.finalPieceCostDateModify = finalPieceCostDateModify;
	}

	public String getFinalPieceCostValueStatus() {
		return finalPieceCostValueStatus;
	}

	public void setFinalPieceCostValueStatus(String finalPieceCostValueStatus) {
		this.finalPieceCostValueStatus = finalPieceCostValueStatus;
	}

	public String getFinalPieceCostCurr() {
		return finalPieceCostCurr;
	}

	public void setFinalPieceCostCurr(String finalPieceCostCurr) {
		this.finalPieceCostCurr = finalPieceCostCurr;
	}

	public String getFinalSupplierLogicCost() {
		return finalSupplierLogicCost;
	}

	public void setFinalSupplierLogicCost(String finalSupplierLogicCost) {
		this.finalSupplierLogicCost = finalSupplierLogicCost;
	}

	public String getFinalSupplierPackageAmount() {
		return finalSupplierPackageAmount;
	}

	public void setFinalSupplierPackageAmount(String finalSupplierPackageAmount) {
		this.finalSupplierPackageAmount = finalSupplierPackageAmount;
	}

	public String getFinalTotalCost() {
		return finalTotalCost;
	}

	public void setFinalTotalCost(String finalTotalCost) {
		this.finalTotalCost = finalTotalCost;
	}

	public String getFinalTotalCostCurr() {
		return finalTotalCostCurr;
	}

	public void setFinalTotalCostCurr(String finalTotalCostCurr) {
		this.finalTotalCostCurr = finalTotalCostCurr;
	}

	public String getBuyerPieceCostDateModify() {
		return buyerPieceCostDateModify;
	}

	public void setBuyerPieceCostDateModify(String buyerPieceCostDateModify) {
		this.buyerPieceCostDateModify = buyerPieceCostDateModify;
	}

	public String getBuyerPieceCostValueStatus() {
		return buyerPieceCostValueStatus;
	}

	public void setBuyerPieceCostValueStatus(String buyerPieceCostValueStatus) {
		this.buyerPieceCostValueStatus = buyerPieceCostValueStatus;
	}

	public String getBuyerPieceCostCurr() {
		return buyerPieceCostCurr;
	}

	public void setBuyerPieceCostCurr(String buyerPieceCostCurr) {
		this.buyerPieceCostCurr = buyerPieceCostCurr;
	}

	public String getBuyerSupplierLogicCost() {
		return buyerSupplierLogicCost;
	}

	public void setBuyerSupplierLogicCost(String buyerSupplierLogicCost) {
		this.buyerSupplierLogicCost = buyerSupplierLogicCost;
	}

	public String getBuyerSupplierPackageAmount() {
		return buyerSupplierPackageAmount;
	}

	public void setBuyerSupplierPackageAmount(String buyerSupplierPackageAmount) {
		this.buyerSupplierPackageAmount = buyerSupplierPackageAmount;
	}

	public String getBuyerTotalCost() {
		return buyerTotalCost;
	}

	public void setBuyerTotalCost(String buyerTotalCost) {
		this.buyerTotalCost = buyerTotalCost;
	}

	public String getBuyerTotalCostCurr() {
		return buyerTotalCostCurr;
	}

	public void setBuyerTotalCostCurr(String buyerTotalCostCurr) {
		this.buyerTotalCostCurr = buyerTotalCostCurr;
	}

	public String getEngineerPieceCostDateModify() {
		return engineerPieceCostDateModify;
	}

	public void setEngineerPieceCostDateModify(String engineerPieceCostDateModify) {
		this.engineerPieceCostDateModify = engineerPieceCostDateModify;
	}

	public String getEngineerPieceCostValueStatus() {
		return engineerPieceCostValueStatus;
	}

	public void setEngineerPieceCostValueStatus(String engineerPieceCostValueStatus) {
		this.engineerPieceCostValueStatus = engineerPieceCostValueStatus;
	}

	public String getEngineerPieceCostCurr() {
		return engineerPieceCostCurr;
	}

	public void setEngineerPieceCostCurr(String engineerPieceCostCurr) {
		this.engineerPieceCostCurr = engineerPieceCostCurr;
	}

	public String getEngineerSupplierLogicCost() {
		return engineerSupplierLogicCost;
	}

	public void setEngineerSupplierLogicCost(String engineerSupplierLogicCost) {
		this.engineerSupplierLogicCost = engineerSupplierLogicCost;
	}

	public String getEngineerSupplierPackageAmount() {
		return engineerSupplierPackageAmount;
	}

	public void setEngineerSupplierPackageAmount(String engineerSupplierPackageAmount) {
		this.engineerSupplierPackageAmount = engineerSupplierPackageAmount;
	}

	public String getEngineerTotalCost() {
		return engineerTotalCost;
	}

	public void setEngineerTotalCost(String engineerTotalCost) {
		this.engineerTotalCost = engineerTotalCost;
	}

	public String getEngineerTotalCostCurr() {
		return engineerTotalCostCurr;
	}

	public void setEngineerTotalCostCurr(String engineerTotalCostCurr) {
		this.engineerTotalCostCurr = engineerTotalCostCurr;
	}

	public String getTargetCostCreationDate() {
		return targetCostCreationDate;
	}

	public void setTargetCostCreationDate(String targetCostCreationDate) {
		this.targetCostCreationDate = targetCostCreationDate;
	}

	public String getProtoPiecePrice() {
		return protoPiecePrice;
	}

	public void setProtoPiecePrice(String protoPiecePrice) {
		this.protoPiecePrice = protoPiecePrice;
	}

	public String getProtoPiecePriceCurr() {
		return protoPiecePriceCurr;
	}

	public void setProtoPiecePriceCurr(String protoPiecePriceCurr) {
		this.protoPiecePriceCurr = protoPiecePriceCurr;
	}

	public String getProtoToolingCurr() {
		return protoToolingCurr;
	}

	public void setProtoToolingCurr(String protoToolingCurr) {
		this.protoToolingCurr = protoToolingCurr;
	}

	public String getProtoToolingValue() {
		return protoToolingValue;
	}

	public void setProtoToolingValue(String protoToolingValue) {
		this.protoToolingValue = protoToolingValue;
	}

	public String getFacilityInvestAmount() {
		return facilityInvestAmount;
	}

	public void setFacilityInvestAmount(String facilityInvestAmount) {
		this.facilityInvestAmount = facilityInvestAmount;
	}

	public String getFacilityInvestCurr() {
		return facilityInvestCurr;
	}

	public void setFacilityInvestCurr(String facilityInvestCurr) {
		this.facilityInvestCurr = facilityInvestCurr;
	}

	public String getMiscellaneousCost() {
		return miscellaneousCost;
	}

	public void setMiscellaneousCost(String miscellaneousCost) {
		this.miscellaneousCost = miscellaneousCost;
	}

	public String getMiscellaneousCurr() {
		return miscellaneousCurr;
	}

	public void setMiscellaneousCurr(String miscellaneousCurr) {
		this.miscellaneousCurr = miscellaneousCurr;
	}

	public String getPieceCostValueStatus() {
		return pieceCostValueStatus;
	}

	public void setPieceCostValueStatus(String pieceCostValueStatus) {
		this.pieceCostValueStatus = pieceCostValueStatus;
	}

	public String getPieceCostCurr() {
		return pieceCostCurr;
	}

	public void setPieceCostCurr(String pieceCostCurr) {
		this.pieceCostCurr = pieceCostCurr;
	}

	public String getQualityFinance() {
		return qualityFinance;
	}

	public void setQualityFinance(String qualityFinance) {
		this.qualityFinance = qualityFinance;
	}

	public String getRefurToolingCurr() {
		return refurToolingCurr;
	}

	public void setRefurToolingCurr(String refurToolingCurr) {
		this.refurToolingCurr = refurToolingCurr;
	}

	public String getRefurToolingValue() {
		return refurToolingValue;
	}

	public void setRefurToolingValue(String refurToolingValue) {
		this.refurToolingValue = refurToolingValue;
	}

	public String getRNOReferNumber() {
		return RNOReferNumber;
	}

	public void setRNOReferNumber(String rNOReferNumber) {
		RNOReferNumber = rNOReferNumber;
	}

	public String getSupplierLogicCost() {
		return supplierLogicCost;
	}

	public void setSupplierLogicCost(String supplierLogicCost) {
		this.supplierLogicCost = supplierLogicCost;
	}

	public String getSupplierPackageAmount() {
		return supplierPackageAmount;
	}

	public void setSupplierPackageAmount(String supplierPackageAmount) {
		this.supplierPackageAmount = supplierPackageAmount;
	}

	public String getToolingInvestValue() {
		return toolingInvestValue;
	}

	public void setToolingInvestValue(String toolingInvestValue) {
		this.toolingInvestValue = toolingInvestValue;
	}

	public String getToolingInvestCurr() {
		return toolingInvestCurr;
	}

	public void setToolingInvestCurr(String toolingInvestCurr) {
		this.toolingInvestCurr = toolingInvestCurr;
	}

	public String getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(String totalCost) {
		this.totalCost = totalCost;
	}

	public String getTotalCostCurr() {
		return totalCostCurr;
	}

	public void setTotalCostCurr(String totalCostCurr) {
		this.totalCostCurr = totalCostCurr;
	}

	public String getEdndCurr() {
		return EdndCurr;
	}

	public void setEdndCurr(String edndCurr) {
		EdndCurr = edndCurr;
	}

	public String getEdndCost() {
		return EdndCost;
	}

	public void setEdndCost(String edndCost) {
		EdndCost = edndCost;
	}

	public double getWeight() {
		return weight;
	}


	public String getTargetCostDateModify() {
		return targetCostDateModify;
	}

	public void setTargetCostDateModify(String targetCostDateModify) {
		this.targetCostDateModify = targetCostDateModify;
	}

	public String getPieceCostDateModify() {
		return pieceCostDateModify;
	}

	public void setPieceCostDateModify(String pieceCostDateModify) {
		this.pieceCostDateModify = pieceCostDateModify;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isFound() {
		return Found;
	}


	public void setFound(boolean notFound) {
		this.Found = notFound;
	}


	public String getSupplierType() {
		return supplierType;
	}

	public void setSupplierType(String supplierType) {
		this.supplierType = supplierType;
	}
	
	public boolean isRightAccess() {
		return rightAccess;
	}

	public void setRightAccess(boolean rightAccess) {
		this.rightAccess = rightAccess;
	}

	public String getBomLevel() {
		return bomLevel;
	}

	public void setBomLevel(String bomLevel) {
		this.bomLevel = bomLevel;
	}

	public String getVNASEANBaseICE() {
		return VNASEANBaseICE;
	}

	public void setVNASEANBaseICE(String vNASEANBaseICE) {
		VNASEANBaseICE = vNASEANBaseICE;
	}

	public String getVNASEANMiddleICE() {
		return VNASEANMiddleICE;
	}

	public void setVNASEANMiddleICE(String vNASEANMiddleICE) {
		VNASEANMiddleICE = vNASEANMiddleICE;
	}

	public String getVNASEANHighICE() {
		return VNASEANHighICE;
	}

	public void setVNASEANHighICE(String vNASEANHigICE) {
		VNASEANHighICE = vNASEANHigICE;
	}

	public String getVNASEANBaseBEV() {
		return VNASEANBaseBEV;
	}

	public void setVNASEANBaseBEV(String vNASEANBaseBEV) {
		VNASEANBaseBEV = vNASEANBaseBEV;
	}

	public String getVNASEANMiddleBEV() {
		return VNASEANMiddleBEV;
	}

	public void setVNASEANMiddleBEV(String vNASEANMiddleBEV) {
		VNASEANMiddleBEV = vNASEANMiddleBEV;
	}

	public String getVNASEANHighBEV() {
		return VNASEANHighBEV;
	}

	public void setVNASEANHighBEV(String vNASEANHighBEV) {
		VNASEANHighBEV = vNASEANHighBEV;
	}

	public String getUSHighBEV() {
		return USHighBEV;
	}

	public void setUSHighBEV(String uSHighBEV) {
		USHighBEV = uSHighBEV;
	}

	public String getUSBaseBev() {
		return USBaseBev;
	}

	public void setUSBaseBev(String uSBaseBev) {
		USBaseBev = uSBaseBev;
	}

	public String getCommonPart() {
		return commonPart;
	}

	public void setCommonPart(String commonPart) {
		this.commonPart = commonPart;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getPartNum() {
		return partNum;
	}
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	public String getPartName() {
		return partName;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	public String getProcurementLvl() {
		return procurementLvl;
	}
	public void setProcurementLvl(String procurementLvl) {
		this.procurementLvl = procurementLvl;
	}
	public String getDesignAssumption() {
		return designAssumption;
	}
	public void setDesignAssumption(String designAssumption) {
		this.designAssumption = designAssumption;
	}
	public String getSorNum() {
		return sorNum;
	}
	public void setSorNum(String sorNum) {
		this.sorNum = sorNum;
	}
	public String getSorName() {
		return sorName;
	}
	public void setSorName(String sorName) {
		this.sorName = sorName;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getCL() {
		return CL;
	}
	public void setCL(String cL) {
		this.CL = cL;
	}
	public String getBuyer() {
		return buyer;
	}
	public void setBuyer(String buyer) {
		this.buyer = buyer;
	}
	public String getPieceCostTarget() {
		return pieceCostTarget;
	}
	public void setPieceCostTarget(String pieceCostTarget) {
		this.pieceCostTarget = pieceCostTarget;
	}
	public String getPieceCostTargetCurr() {
		return pieceCostTargetCurr;
	}
	public void setPieceCostTargetCurr(String pieceCOstTargetCurr) {
		this.pieceCostTargetCurr = pieceCOstTargetCurr;
	}
	public String getEDDCostTarget() {
		return EDDCostTarget;
	}
	public void setEDDCostTarget(String eDDCostTarget) {
		EDDCostTarget = eDDCostTarget;
	}
	public String getEDDCostTargetCurr() {
		return EDDCostTargetCurr;
	}
	public void setEDDCostTargetCurr(String eDDCostTargetCurr) {
		EDDCostTargetCurr = eDDCostTargetCurr;
	}
	public String getToolingCostTarget() {
		return toolingCostTarget;
	}
	public void setToolingCostTarget(String toolingCostTarget) {
		this.toolingCostTarget = toolingCostTarget;
	}
	public String getToolingCostTargetCurr() {
		return toolingCostTargetCurr;
	}
	public void setToolingCostTargetCurr(String toolingCostTargetCurr) {
		this.toolingCostTargetCurr = toolingCostTargetCurr;
	}
}
