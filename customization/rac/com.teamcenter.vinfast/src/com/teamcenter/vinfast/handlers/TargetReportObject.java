package com.teamcenter.vinfast.handlers;

public class TargetReportObject {
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
	private String platformPart;
	private String bomLevel;
	private boolean rightAccess;
	private boolean notFound;
	private String supplierType;
	private double weight;
	private String targetCostDateModify;
	private String pieceCostDateModify;

	public TargetReportObject() {
		this.partNum 			= "";
		this.partName 			= "";
		this.procurementLvl 	= "";
		this.designAssumption 	= "";
		this.sorNum 			= "";
		this.sorName 			= "";
		this.moduleName 		= "";
		this.CL					= "";
		this.buyer				= "";
		this.pieceCostTarget	= "";
		this.pieceCostTargetCurr = "";
		this.EDDCostTarget		= "";
		this.EDDCostTargetCurr	= "";
		this.toolingCostTarget	= "";
		this.toolingCostTargetCurr = "";
		this.VNASEANBaseBEV		= "";
		this.VNASEANBaseICE		= "";
		this.VNASEANHighBEV		= "";
		this.VNASEANHighICE		= "";
		this.VNASEANMiddleBEV	= "";
		this.VNASEANMiddleICE	= "";
		this.USBaseBev			= "";
		this.USHighBEV			= "";
		this.platformPart		= "";
		this.bomLevel			= "";
		this.owner				= "";
		this.rightAccess		= false;
		this.notFound			= false;
		this.targetCostDateModify	= "";
		this.pieceCostDateModify	= "";
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

	public boolean isNotFound() {
		return notFound;
	}


	public void setNotFound(boolean notFound) {
		this.notFound = notFound;
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

	public String getPlatformPart() {
		return platformPart;
	}

	public void setPlatformPart(String platformPart) {
		this.platformPart = platformPart;
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
