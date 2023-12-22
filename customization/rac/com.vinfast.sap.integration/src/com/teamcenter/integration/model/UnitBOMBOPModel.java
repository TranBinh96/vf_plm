package com.teamcenter.integration.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.sap.dialogs.UnitTransferDialog;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class UnitBOMBOPModel extends ModelAbstract {
	private TCComponent bomlineItem;
	private String action;
	private HashMap<String, String> bomDataItem;
	private HashMap<String, String> bopDataItem;
	private ArrayList<HashMap<String, String>> owpBopDataItem;

	private String mcn;
	private String plant;
	private String bomlineID;
	private String parentPartID;

	public UnitBOMBOPModel() {
		super(ModelType.DATA_SEND);
	}

	public String getPartNumber() {
		try {
			return bomlineItem.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getPartRev() {
		try {
			return bomlineItem.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getBomlineID() {
		try {
			return bomlineItem.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getAction() {
		return action;
	}

	public void generateBOMItem(TCComponent parent, TCComponent child, String action) {
		try {
			bomlineItem = child;
			this.action = action;
			bomDataItem = new HashMap<String, String>();
			bomDataItem.put("MCN", mcn);
			bomDataItem.put("PLANTCODE", plant);
			bomDataItem.put("ACTION", action);
			bomDataItem.put("PARENTPART", parent.getProperty(PropertyDefines.BOM_ITEM_ID));
			bomDataItem.put("CHILDPART", child.getProperty(PropertyDefines.BOM_ITEM_ID));

			String BOMLineID = child.getProperty(PropertyDefines.BOM_BOM_ID);
			BOMLineID = StringExtension.ConvertNumberToString(Integer.parseInt(BOMLineID), 4);
			bomDataItem.put("BOMLINEID", BOMLineID);

			String quantity = child.getProperty(PropertyDefines.BOM_QUANTITY);
			if (quantity.isEmpty())
				quantity = "1.000";
			bomDataItem.put("QUANTITY", quantity);

			bomDataItem.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			UIGetValuesUtility.updateNodeForSubtitutePart((TCComponentBOMLine) child, bomDataItem);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void generateBOPItem(TCComponent bop, String action) {
		try {
			bomlineItem = bop;
			this.action = action;
			bopDataItem = new HashMap<String, String>();
			bopDataItem.put("SAPPLANT", plant);
			bopDataItem.put("BOMLINEID", bomlineID);
			bopDataItem.put("TOPLEVELPART", parentPartID);
			bopDataItem.put("HEADERPART", parentPartID);
			bopDataItem.put("ACTION", action);
			bopDataItem.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			TCComponentBOMLine operationLine = (TCComponentBOMLine) bop.getReferenceProperty("bl_parent");
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();
			if (operationRevision.getDisplayType().equalsIgnoreCase("Operation Revision")) {
				String operationID = operationLine.getProperty(PropertyDefines.BOM_ITEM_ID);
				String operationRev = operationLine.getProperty(PropertyDefines.BOM_REV_ID);
				String operationType = operationLine.getProperty(PropertyDefines.BOM_OPERATION_TYPE);
				String transferMES = operationLine.getProperty(PropertyDefines.BOM_TRANSFER_TO_MES);

				if (operationType.isEmpty() || operationType.equalsIgnoreCase("NA"))
					operationType = "N";
				else
					operationType = "Y";

				String JIS = operationLine.getProperty(PropertyDefines.BOM_LINE_SUP_METHOD);
				if (JIS.equals("") || JIS.equalsIgnoreCase("NA"))
					JIS = "JIS";

				String workStation = UIGetValuesUtility.getWorkStationID(operationLine, PropertyDefines.BOM_NAME);

				if (transferMES.trim().isEmpty()) {
					setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
					setMessage("Please first transfer operation [" + operationID + "/" + operationRev + "] to MES and then transfer BOM/BOP to SAP.");
				}
				if (workStation.isEmpty() || workStation.length() < 13) {
					setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
					setMessage("WorkStation ID is wrong or incorrect.");
				}

				bopDataItem.put("BOPID", operationID);
				bopDataItem.put("REVISION", operationRev);
				bopDataItem.put("LINESUPPLYMETHOD", JIS);
				bopDataItem.put("MESBOPINDICATOR", operationType);
				bopDataItem.put("WORKSTATION", workStation);
			} else {
				setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				setMessage("BOPLine is not under operation.");
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void generateOWPBOPItem(TCComponent bop, String action) {
		try {
			bomlineItem = bop;
			this.action = action;
			owpBopDataItem = new ArrayList<HashMap<String, String>>();
			TCComponentBOMLine operationLine = (TCComponentBOMLine) bop;
			String itemID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
			String revID = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
			String referenceComp = operationLine.getItemRevision().getProperty(PropertyDefines.BOM_REF_COMP);
			String transferMES = operationLine.getProperty(PropertyDefines.BOM_TRANSFER_TO_MES);

			if (transferMES.isEmpty()) {
				setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				setMessage("Please first transfer operation to MES and then transfer BOM/BOP to SAP.");
				return;
			}

			if (referenceComp.isEmpty()) {
				setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				setMessage("On Operation \"Reference Component\" column Header Part Information is missing.");
				return;
			}

			String lsm = operationLine.getPropertyDisplayableValue("bl_rev_vf5_line_supply_method");
			if (lsm.isEmpty())
				lsm = "JIS";

			String operationType = operationLine.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
			if (operationType.isEmpty() || operationType.equals("NA"))
				operationType = "N";
			else
				operationType = "Y";

			String workStation = UIGetValuesUtility.getWorkStation(new String[] { "vf3_transfer_to_mes", "vf4_user_notes" }, operationLine.getItemRevision());
			if (workStation.isEmpty()) {
				setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
				setMessage("Workstation ID Information is missing.");
				return;
			}

			String[] split = workStation.split(",");
			for (String workstation : split) {
				if (workstation.length() < 13) {
					setPrepareStatus(PREPARE_STATUS.NOT_VALIDATE);
					setMessage("Workstation ID Information is wrong.");
					continue;
				}

				HashMap<String, String> tempBOPMap = new HashMap<String, String>();
				tempBOPMap.put("SAPPLANT", UnitTransferDialog.getPlant());
				tempBOPMap.put("TOPLEVELPART", referenceComp);
				tempBOPMap.put("HEADERPART", referenceComp);
				tempBOPMap.put("BOMLINEID", "");
				tempBOPMap.put("ACTION", action);
				tempBOPMap.put("WORKSTATION", workstation);
				tempBOPMap.put("LINESUPPLYMETHOD", lsm);
				tempBOPMap.put("BOPID", itemID);
				tempBOPMap.put("MESBOPINDICATOR", operationType);
				tempBOPMap.put("REVISION", revID);
				tempBOPMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
				owpBopDataItem.add(tempBOPMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> getBomDataItem() {
		return bomDataItem;
	}

	public HashMap<String, String> getBopDataItem() {
		return bopDataItem;
	}

	public void setMcn(String mcn) {
		this.mcn = mcn;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	public void setBomlineID(String bomlineID) {
		this.bomlineID = bomlineID;
	}

	public void setParentPartID(String parentPartID) {
		this.parentPartID = parentPartID;
	}
}
