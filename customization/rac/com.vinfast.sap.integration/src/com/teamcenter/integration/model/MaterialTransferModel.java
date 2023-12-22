package com.teamcenter.integration.model;

import java.util.LinkedHashMap;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class MaterialTransferModel {
	public enum PREPARE_STATUS {
		VALIDATE, ALREADY_TRANSFER, NOT_VALIDATE, NOT_YET_RELEASE
	}

	private TCSession session;
	private DataManagementService dmService;
	private TCComponentItemRevision materialRevObject = null;
	private TCComponentItem materialItemObject = null;
	private String materialNumber = "";
	private String mcnNumber = "";
	private String plantCode = "";
	private String revisionNumber = "";
	private String description = "";
	private String materialType = "";
	private String sequence = "";
	private String makeBuy = "";
	private String functionClass = "";
	private String approvalClass = "";
	private String oldMaterialNumber = "";
	private String partType = "";
	private String gmPart = "";
	private String broadCode = "";
	private String uom = "";
	private String traceablePart = "";
	private String functional = "";
	private String vnDescription = "";
	private TCComponent plantForm = null;
	private boolean isFRS = false;

	//
	private String message = "";
	private String transferResult = "";
	private boolean isCheckAlreadyTransfer = false;
	private boolean alreadyTransfer = false;

	private PREPARE_STATUS prepareStatus = PREPARE_STATUS.VALIDATE;

	public MaterialTransferModel() {

	}

	public MaterialTransferModel(TCSession session, DataManagementService dmService) {
		this.session = session;
		this.dmService = dmService;
	}

	public TCComponentItemRevision getMaterialRevObject() {
		return materialRevObject;
	}

	public void setMaterialItemObject(TCComponentItem materialItemObject) {
		this.materialItemObject = materialItemObject;
		if (materialItemObject != null) {
			try {
				materialNumber = generateMaterialId(materialItemObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String generateMaterialId(TCComponent materialItemObject) throws NotLoadedException, TCException {
		String materialNumber = generateMaterialID(materialItemObject);
		String materialObjectType = materialItemObject.getType();
		if (materialObjectType.startsWith("VF3_FRS")) {
			isFRS = true;
		}

		return materialNumber;
	}

	public static String generateMaterialID(TCComponent materialItemObject) throws NotLoadedException, TCException {
		String materialNumber = materialItemObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
		String materialObjectType = materialItemObject.getType();
		if (materialObjectType.startsWith("VF3_FRS")) {
			TCComponent materialItemRev = materialItemObject;
			if (materialItemObject instanceof TCComponentItem) {
				materialItemRev = ((TCComponentItem) materialItemObject).getLatestItemRevision();
			}

			String versionNumber = materialItemRev.getProperty("vf4_version");
			if (versionNumber.isEmpty() == false) {
				double versionDouble = Double.parseDouble(versionNumber);
				versionNumber = String.valueOf((int) versionDouble);
			}
			String majorVersion = materialItemRev.getProperty("vf4_major_version");
			String minorVersion = materialItemRev.getProperty("vf4_minor_version");
			String hotfixVersion = materialItemRev.getProperty("vf3_hotfix_number").trim();
			if (hotfixVersion.isEmpty()) {
				hotfixVersion = materialItemRev.getProperty("vf3_ref_num").trim();
			}
			if (hotfixVersion.isEmpty() == false) {
				try {
					Integer.parseInt(hotfixVersion);
				} catch (Exception ex) {
					hotfixVersion = "";
				}
			}
			if (hotfixVersion.isEmpty())
				hotfixVersion = "0";
			materialNumber = "FRS_DUMMY" + versionNumber + "." + majorVersion + "." + minorVersion + "." + hotfixVersion;
		}

		return materialNumber;
	}

	public void setMaterialRevObject(TCComponentItemRevision materialRevObject) {
		this.materialRevObject = materialRevObject;
		if (materialRevObject != null) {
			try {
				materialNumber = generateMaterialId(materialRevObject);
				revisionNumber = materialRevObject.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
				if (isCheckAlreadyTransfer && !materialRevObject.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).isEmpty()) {
					prepareStatus = PREPARE_STATUS.ALREADY_TRANSFER;
					alreadyTransfer = true;
				}
				materialItemObject = materialRevObject.getItem();
				description = materialRevObject.getPropertyDisplayableValue(PropertyDefines.ITEM_NAME);
				materialType = isFRS ? "ZSOF" : materialItemObject.getTCProperty(PropertyDefines.ITEM_MATERIAL_TYPE).getStringValue();
				uom = materialItemObject.getPropertyDisplayableValue(PropertyDefines.ITEM_UOM);
				if (uom.equals("each"))
					uom = "EA";
				if (materialType.isEmpty()) {
					String makeBuyItem = materialItemObject.getPropertyDisplayableValue(PropertyDefines.ITEM_MAKE_BUY);
					if (makeBuyItem.compareToIgnoreCase("Make") == 0)
						materialType = "ZSFG";
					else if (makeBuyItem.compareToIgnoreCase("Buy") == 0)
						materialType = "ZRAW";
				}
				plantForm = (isFRS ? null : UIGetValuesUtility.getPlantForm(materialItemObject, plantCode));
				if (plantForm != null) {
					makeBuy = UIGetValuesUtility.getPlantMakeBuy(materialItemObject, plantCode);
					functionClass = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_FUNC_CODE);
					approvalClass = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_APPOV_CODE);
					gmPart = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_GM_PARTNO);
					oldMaterialNumber = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_OLD_PARTNO);
					partType = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_PART_CATEGORY);
					traceablePart = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.ITEM_IS_TRACEABLE);
					if (traceablePart.isEmpty())
						traceablePart = "N";
					broadCode = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.REV_BROADCAST);
					vnDescription = TCExtension.GetPropertyRealValue(materialItemObject, PropertyDefines.REV_VIET_DESCRIPTION);
					String objectType = materialItemObject.getTCProperty(PropertyDefines.ITEM_TYPE).getStringValue();
					if (objectType.compareToIgnoreCase(PropertyDefines.TYPE_DESIGN_REVISION) == 0 || objectType.compareToIgnoreCase(PropertyDefines.TYPE_BP_DESIGN_REVISION) == 0 || objectType.compareToIgnoreCase(PropertyDefines.TYPE_MFG_PART_REVISION) == 0)
						functional = getDonorVehicle(materialRevObject, objectType);
				} else if (isFRS) {
					plantCode = "3001";
					makeBuy = "F";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public LinkedHashMap<String, String> getTransferData() {
		return new LinkedHashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put("MATERIALNUMBER", materialNumber);
				put("MCN", mcnNumber);
				put("ACTION", PropertyDefines.ACTION_ADD);
				put("SAP_PLANT", plantCode);
				put("REVISIONNUMBER", revisionNumber);
				put("DESCRIPTION", description);
				put("MATERIALTYPE", materialType);
				put("SEQUENCE", UIGetValuesUtility.getSequenceID());
				put("MAKEBUY", makeBuy);
				put("FUNCTIONALCLASS", functionClass);
				put("APPROVALCLASS", approvalClass);
				put("OLDMATERIALNUMBER", oldMaterialNumber);
				put("PART_TYPE", partType);
				put("GM_PART", gmPart);
				put("BRDCODE", broadCode);
				put("UOM", uom);
				put("TRACEABLEPART", traceablePart);
				put("FUNCTIONAL", functional);
				if (!vnDescription.isEmpty())
					put("DESCRIPTIONVIETNAMESE", vnDescription);
			}
		};
	}

	public String getMaterialNumber() {
		return materialNumber;
	}

	public String getMcnNumber() {
		return mcnNumber;
	}

	public void setMcnNumber(String mcnNumber) {
		this.mcnNumber = mcnNumber;
	}

	public String getPlantCode() {
		return plantCode;
	}

	public void setPlantCode(String plantCode) {
		this.plantCode = plantCode;
	}

	public String getRevisionNumber() {
		return revisionNumber;
	}

	public String getDescription() {
		return description;
	}

	public String getMaterialType() {
		return materialType;
	}

	public String getSequence() {
		return sequence;
	}

	public String getMakeBuy() {
		return makeBuy;
	}

	public String getFunctionClass() {
		return functionClass;
	}

	public String getApprovalClass() {
		return approvalClass;
	}

	public String getOldMaterialNumber() {
		return oldMaterialNumber;
	}

	public String getPartType() {
		return partType;
	}

	public String getGmPart() {
		return gmPart;
	}

	public String getBroadCode() {
		return broadCode;
	}

	public String getUom() {
		return uom;
	}

	public String getTraceablePart() {
		return traceablePart;
	}

	public String getFunctional() {
		return functional;
	}

	public String getVnDescription() {
		return vnDescription;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransferResult() {
		return transferResult;
	}

	public void setTransferResult(String transferResult) {
		this.transferResult = transferResult;
	}

	public boolean isAlreadyTransfer() {
		return alreadyTransfer;
	}

	public TCComponent getPlantForm() {
		return plantForm;
	}

	public boolean isValidate() {
		return message.isEmpty();
	}

	public TCComponentItem getMaterialItemObject() {
		return materialItemObject;
	}

	public PREPARE_STATUS getPrepareStatus() {
		return prepareStatus;
	}

	public void setMakeBuy(String makebuy) {
		this.makeBuy = makebuy;
	}

	public void setPrepareStatus(PREPARE_STATUS prepareStatus) {
		this.prepareStatus = prepareStatus;
	}

	private String getDonorVehicle(TCComponentItemRevision material, String objectType) {
		String donor = "";
		try {
			// 1.get vf4_donor_veh value of material
			String materialVehProg = "";
			if (objectType.equals(PropertyDefines.TYPE_DESIGN_REVISION) || objectType.equals(PropertyDefines.TYPE_BP_DESIGN_REVISION)) {
				dmService.getProperties(new TCComponent[] { material.getItem() }, new String[] { PropertyDefines.ITEM_DONOR_VEHICLE2 });
				materialVehProg = material.getItem().getProperty(PropertyDefines.ITEM_DONOR_VEHICLE2);
			} else if (objectType.equals(PropertyDefines.TYPE_MFG_PART_REVISION)) {
				dmService.getProperties(new TCComponent[] { material.getItem() }, new String[] { PropertyDefines.ITEM_DONOR_VEHICLE });
				materialVehProg = material.getItem().getProperty(PropertyDefines.ITEM_DONOR_VEHICLE);
			}

			if (materialVehProg.trim().isEmpty())
				return "";

			// 2.search configurator context having same vf4_donor_veh with material && is
			// latest configurator context transfered to sap.
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
			queryInput.put("Vehicle Program", materialVehProg);
			queryInput.put("Is Latest ERP Transferred", "TRUE");
			TCComponent[] contexts = UIGetValuesUtility.query("Admin - Configurator Context", queryInput, session);

			if (contexts == null)
				return "";

			if (contexts.length == 1) {
				TCComponent context = contexts[0];
				donor = context.getProperty(PropertyDefines.CONTEXT_CURRENT_ID);
			} else {
				for (TCComponent context : contexts) {
					String id = context.getProperty(PropertyDefines.CONTEXT_CURRENT_ID);
					System.out.println("Context: " + id);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
			return "";
		}

		return donor;
	}

	public boolean isFRS() {
		return isFRS;
	}
}
