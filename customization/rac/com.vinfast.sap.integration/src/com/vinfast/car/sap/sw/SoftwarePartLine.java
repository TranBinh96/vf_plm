package com.vinfast.car.sap.sw;

public class SoftwarePartLine implements Cloneable{

	

	
	public SoftwarePartLine() {
		super();
	}
	
    @Override
	public SoftwarePartLine clone() throws CloneNotSupportedException {
        return (SoftwarePartLine) super.clone();
    }
    
	public enum ACTION{
		ACTION_ADD,
		ACTION_DELETE,
		ACTION_UPDATE_REVISION,
		ACTION_UPDATE_VARIANT,
		ACTION_NONE
	};
	
	private String id;
	private String revID;
	private String variantFormula;
	private String swPartType;
	private String information = "";
	private String ecu = "";
	private ACTION action;
	
	public ACTION getAction() {
		return action;
	}

	public String getEcu() {
		return ecu;
	}

	public void setEcu(String ecu) {
		this.ecu = ecu;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRevID() {
		return revID;
	}
	public void setRevID(String revID) {
		this.revID = revID;
	}
	public String getVariantFormula() {
		return variantFormula;
	}
	public void setVariantFormula(String variantFormula) {
		this.variantFormula = variantFormula;
	}
	public String getSwPartType() {
		return swPartType;
	}
	public void setSwPartType(String swPartType) {
		this.swPartType = swPartType;
	}
	
	public String getKey() {
		return this.id;
	}
	
    public void setAction(ACTION action) {
    	this.action = action;
    	switch(action) {
    	case ACTION_ADD:
    		information += "New part added in SW BOM.\n";
    		break;
    	case ACTION_DELETE:
    		information += "Part deleted in SW BOM.\n";
    		break;
    	case ACTION_UPDATE_REVISION:
    		information += "Part updated revision in SW BOM.\n";
    		break;
    	case ACTION_UPDATE_VARIANT:
    		information += "Part updated variant formula in SW BOM.\n";
    		break;
		default:
			break;
    	}
    }
	
	public boolean isValidSwPartLine() {
		if(swPartType.equals("PURCHASE") || swPartType.equals("In House")) {
			return false;
		}
		return true;
	}

}
