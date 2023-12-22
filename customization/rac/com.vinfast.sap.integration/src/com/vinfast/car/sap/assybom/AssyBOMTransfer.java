package com.vinfast.car.sap.assybom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class AssyBOMTransfer {
	
	
	public HashMap<TCComponent, HashMap<String, BOMBOPData>> loadBOMBOPData(BOMManager BOMManager, HashMap<TCComponent, ArrayList<BOMBOPData>> dataMap){

		HashMap<TCComponent, HashMap<String, BOMBOPData>> allGroupDataMap = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();

		try {

			DataManagementService dataManagementService = BOMManager.getDataManagementService();
			String[] bomProperties = new String[7];
			bomProperties[0] = PropertyDefines.BOM_ITEM_ID;
			bomProperties[1] = PropertyDefines.BOM_BOM_ID;
			bomProperties[2] = PropertyDefines.BOM_QUANTITY;
			bomProperties[3] = PropertyDefines.BOM_FORMULA;
			bomProperties[4] = PropertyDefines.BOM_MANUF_CODE;
			bomProperties[5] = PropertyDefines.BOM_PARENT;
			bomProperties[6] = PropertyDefines.BOM_DESIGNATOR;

			String[] bopProperties = new String[3];
			bopProperties[0] = PropertyDefines.BOM_FAMILY_ADDR;
			bopProperties[1] = PropertyDefines.BOM_LRHAND;
			bopProperties[2] = PropertyDefines.BOM_PARENT;

			for(TCComponent BOMLine : dataMap.keySet()) {
				
				TCComponentBOMLine BOMLineObject = (TCComponentBOMLine) BOMLine; 
				
				TCComponentItemRevision BOMLineRevision = BOMLineObject.getItemRevision();

				HashMap<String, BOMBOPData> subGroupDataMap = null;
				
				ArrayList<BOMBOPData> subGroupData = dataMap.get(BOMLineObject);
				
				if(subGroupData.isEmpty()) {
					
					if(BOMLineRevision.getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {

						BOMManager.setOperationNoPart(BOMLineObject);
						
					}else {
						
						subGroupDataMap = new HashMap<String, BOMBOPData>();
						allGroupDataMap.put(BOMLineObject, subGroupDataMap);
					}
				}else {
					
					String parentID = "";
					subGroupDataMap = new HashMap<String, BOMBOPData>();
					
					if(BOMLineRevision.getType().equals(PropertyDefines.TYPE_OPERATION_REVISION) == false) {

						parentID =  BOMLineObject.getProperty(PropertyDefines.BOM_ITEM_ID);
					}
					
					for(BOMBOPData data : subGroupData) {

						TCComponent bomLine = data.getMBOMLine();
						TCComponent[] bopLines = data.getMBOPLine();
						dataManagementService.getProperties(new TCComponent[] {bomLine}, bomProperties);
						dataManagementService.getProperties(bopLines, bopProperties);

						String plant = BOMManager.getPlant();
						data.setPlantCode(plant);
						
						if(parentID.equals("")) {

							TCComponentBOMLine parentLine = (TCComponentBOMLine) bomLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
							parentID = parentLine.getProperty(PropertyDefines.BOM_ITEM_ID);
							data.setParentPart(parentID);
						
						}else {
							data.setParentPart(parentID);
						}
						
						String childID = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID).trim();
						data.setChildPart(childID);
						
						String bomlineid = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID).trim();
						if(bomlineid.length() != 0 ){
							for(int i= bomlineid.length(); i<4 ;i++){
								bomlineid = "0"+bomlineid;
							}
						}
						data.setBOMLineID(bomlineid);

						String quantity = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY).trim();
						if(quantity.length() == 0 ){
							quantity = "1.000";
						}
						data.setQuanity(quantity);

						String vfDesignator = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_DESIGNATOR).trim();
						data.setVfDesignator(vfDesignator);
						
						String mcn =  BOMManager.getMCN();
						data.setMCN(mcn);

						String sequence = UIGetValuesUtility.getSequenceID();
						data.setSequenceTime(sequence);

						data.setAction("A");
						
						if(bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_MANUF_CODE).equals("Parent Part") == true){

							TCComponentBOMLine parent = (TCComponentBOMLine) bomLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
							data.setHeaderPart(parent.getProperty(PropertyDefines.BOM_ITEM_ID));
							parent = (TCComponentBOMLine) parent.getReferenceProperty(PropertyDefines.BOM_PARENT);
							data.setTopLevelPart(parent.getProperty(PropertyDefines.BOM_ITEM_ID));
							data.setPhantom("YES");

						}else{
							
							data.setHeaderPart(parentID);
							data.setTopLevelPart(parentID);
							data.setPhantom("NO");
						}

						for(TCComponent bopLine : bopLines) {

							String family_addr = bopLine.getPropertyDisplayableValue(PropertyDefines.BOM_FAMILY_ADDR).trim();
							data.setFamilyAddress(family_addr);

							String l_r_hand = bopLine.getPropertyDisplayableValue(PropertyDefines.BOM_LRHAND).trim();
							data.setLeftRightHand(l_r_hand);

							TCComponentBOMLine operation  = (TCComponentBOMLine)bopLine.getReferenceProperty(PropertyDefines.BOM_PARENT);

							if(operation.getItemRevision().getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {

								String[] operationProperties = new String[5];
								operationProperties[0] = PropertyDefines.BOM_NAME;
								operationProperties[1] = PropertyDefines.BOM_ITEM_ID;
								operationProperties[2] = PropertyDefines.BOM_LINE_SUP_METHOD;
								operationProperties[3] = PropertyDefines.BOM_OPERATION_TYPE;
								operationProperties[4] = PropertyDefines.BOM_REV_ID;
								dataManagementService.getProperties(new TCComponent[] {operation}, operationProperties);

								String workstation =  UIGetValuesUtility.getWorkStationID(operation, PropertyDefines.BOM_NAME);
								data.setWorkStation(workstation);

								String linesupplymethod =  operation.getPropertyDisplayableValue(PropertyDefines.BOM_LINE_SUP_METHOD);
								if(linesupplymethod.equals("")) {

									linesupplymethod = "JIS";
								}
								data.setLineSupplyMethod(linesupplymethod);

								data.setBOPID(operation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));

								String mesbopindicator =  operation.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
								if (mesbopindicator.equals("") || mesbopindicator.equalsIgnoreCase("NA"))
								{
									mesbopindicator =  "N";
								}else{
									mesbopindicator =  "Y";
								}

								data.setMESIndicator(mesbopindicator);
								data.setBOPRevision(operation.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID));

							}
						}

						subGroupDataMap.put(data.getParentPart()+"~"+data.getChildPart()+"~"+data.getBOMLineID(), data);
					}
					
					allGroupDataMap.put(BOMLineRevision, subGroupDataMap);
				}
			}
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return allGroupDataMap;
	}

	public BOMBOPData loadBOPData(BOMManager BOMManager, TCComponentBOMLine bomLine, boolean isDelete){

		new UIGetValuesUtility();
		DataManagementService dataManagementService = BOMManager.getDataManagementService();
		BOMBOPData BOPData =  new BOMBOPData();
		try {
			
			String plant = BOMManager.getPlant();
			BOPData.setPlantCode(plant);
			
			String parent = bomLine.getProperty(PropertyDefines.BOM_REF_COMP);
			if(parent.equals("")) {
				
				String BOPID = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
				String BOPRevID = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
				BOMManager.printReport("PRINT", new String[]{Integer.toString(BOMManager.getSerialNo()),"OWP",BOPID+"/"+BOPRevID,"-","Header Parent is empty. Please fill in reference component","-","Error"});
				BOMManager.setError(true);
				return null;
				
			}else {
				
				BOPData.setHeaderPart(parent);
				BOPData.setTopLevelPart(parent);
			}
			
			BOPData.setBOMLineID("");
			
			String mcn =  BOMManager.getMCN();
			BOPData.setMCN(mcn);

			String sequence = UIGetValuesUtility.getSequenceID();
			BOPData.setSequenceTime(sequence);

			if(isDelete) {
				
				BOPData.setAction("D");
			}else {
				
				BOPData.setAction("A");
			}

			if(bomLine.getItemRevision().getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {

				String[] operationProperties = new String[5];
				operationProperties[0] = PropertyDefines.BOM_NAME;
				operationProperties[1] = PropertyDefines.BOM_ITEM_ID;
				operationProperties[2] = PropertyDefines.BOM_LINE_SUP_METHOD;
				operationProperties[3] = PropertyDefines.BOM_OPERATION_TYPE;
				operationProperties[4] = PropertyDefines.BOM_REV_ID;
				dataManagementService.getProperties(new TCComponent[] {bomLine}, operationProperties);

				if(isDelete) {
					
					String WSID = bomLine.getItemRevision().getProperty(PropertyDefines.REV_TO_SAP);
					if(WSID.equals("")) {
						
						String BOPID = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						String BOPRevID = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
						BOMManager.printReport("PRINT", new String[]{Integer.toString(BOMManager.getSerialNo()),"OWP",BOPID+"/"+BOPRevID,"-","WorkStation ID is wrong","-","Error"});
						BOMManager.setError(true);
						return null;
					}else {
						BOPData.setWorkStation(WSID);
					}
					
				}else {
					
					String workstation =  UIGetValuesUtility.getWorkStationID(bomLine, PropertyDefines.BOM_NAME);
					BOPData.setWorkStation(workstation);
				}
				
				String linesupplymethod =  bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_LINE_SUP_METHOD);
				if(linesupplymethod.equals("")) {

					linesupplymethod = "JIS";
				}
				BOPData.setLineSupplyMethod(linesupplymethod);

				BOPData.setBOPID(bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));

				String mesbopindicator =  bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
				if (mesbopindicator.equals("") || mesbopindicator.equalsIgnoreCase("NA") || mesbopindicator.equalsIgnoreCase("Automatic Consumption"))
				{
					mesbopindicator =  "N";
				}else{
					mesbopindicator =  "Y";
				}

				BOPData.setMESIndicator(mesbopindicator);
				BOPData.setBOPRevision(bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID));
			}

		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BOPData;
	}

	public TCComponent[] traverseSubAssembly(TCComponentBOMLine BOMLine,int count, StringBuilder strBuilder){

		new UIGetValuesUtility();
		new Logger();
		ArrayList<TCComponentBOMLine> sendToSAP =  new ArrayList<TCComponentBOMLine>();
		ArrayList<String> duplicateBOMLine = new ArrayList<String>();
		TCComponent[] bomlinesList = null;
		String[] reqSubAssyProperties = {"bl_rev_item_id","VF4_bomline_id","bl_item_object_type","VF3_purchase_lvl_vf","VF4_manuf_code"}; 
		boolean bomlineIDExists = false;
		try {


			ExpandPSData[] childBOMLines = UIGetValuesUtility.traverseSingleLevelBOM(BOMLine.getSession(), BOMLine);
			
			

			for(ExpandPSData childBomLine : childBOMLines) {

				String[] bomlinePropValues = childBomLine.bomLine.getProperties(reqSubAssyProperties);

				if (UIGetValuesUtility.isToProcessAssembly(bomlinePropValues[2].trim(),bomlinePropValues[4].trim())== true)
				{
					if(!duplicateBOMLine.contains(bomlinePropValues[1])){

						duplicateBOMLine.add(bomlinePropValues[1]);
						sendToSAP.add(childBomLine.bomLine);

					}else{

						String ID = childBomLine.bomLine.getProperty("bl_rev_item_id");
						Logger.bufferResponse("PRINT", new String[] {Integer.toString(count),ID,bomlinePropValues[1],"Duplicate BOMline ID in Sub Group.","-","Error"}, strBuilder);
						count++;
						bomlineIDExists = true;
					}

				}

			}

			if(bomlineIDExists == false && sendToSAP.size() != 0){
				bomlinesList = new TCComponent[sendToSAP.size()];
				for(int i = 0; i<sendToSAP.size();i++){
					bomlinesList[i] = sendToSAP.get(i);
				}
			}
			if(bomlineIDExists == true){
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {

						try {

							new Logger();
							StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(strBuilder.toString(), "", "BOM"));
							viewdialog.setTitle("Transfer Status");
							viewdialog.setSize(600, 400);
							viewdialog.setLocationRelativeTo(null);
							viewdialog.setVisible(true);

						} catch (Exception e) {

							e.printStackTrace();
						}

					}
				});
				return null;
			}
		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bomlinesList;
	}

	public LinkedHashMap<String, String> generateBOMWebServiceValue(String key, LinkedHashMap<String, LinkedHashMap<String,String>> bomLinesArray,String MCN_SAPID, String action, String seq_ID){

		LinkedHashMap<String, String> newbomValues = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> newbom = bomLinesArray.get(key);

		newbomValues.put("PARENTPART", newbom.get("PARENTPART"));
		newbomValues.put("CHILDPART", newbom.get("CHILDPART"));
		newbomValues.put("BOMLINEID", newbom.get("BOMLINEID"));

		newbomValues.put("PLANTCODE", newbom.get("PLANTCODE"));
		newbomValues.put("QUANTITY", newbom.get("QUANTITY"));
		newbomValues.put("ACTION", action);
		newbomValues.put("SEQUENCE", seq_ID);
		newbomValues.put("MCN", MCN_SAPID);
		newbomValues.put("PHANTOM", newbom.get("PHANTOM"));

		return newbomValues;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String, String>> commonPart(LinkedHashMap<String,LinkedHashMap<String,String>> old_items, LinkedHashMap<String,LinkedHashMap<String,String>> new_items){

		LinkedHashMap<String,LinkedHashMap<String, String>> addSTBOM = (LinkedHashMap<String,LinkedHashMap<String, String>>) old_items.clone();
		LinkedHashMap<String,LinkedHashMap<String, String>> addSTPart = (LinkedHashMap<String,LinkedHashMap<String, String>>)new_items.clone();
		LinkedHashMap<String,LinkedHashMap<String, String>> common_Parts = new LinkedHashMap<String,LinkedHashMap<String, String>>();
		Set<String> addBOM = addSTBOM.keySet();
		Set<String> addST = addSTPart.keySet();

		addBOM.retainAll(addST);

		for ( String key : addSTBOM.keySet()) {
			try {

				LinkedHashMap<String, String> solutionItem = addSTBOM.get(key);
				LinkedHashMap<String, String> problemItem = addSTPart.get(key);

				String solQty = solutionItem.get("QUANTITY");
				String prbQty = problemItem.get("QUANTITY");

				float solq = Float.parseFloat(solQty);
				float prbq = Float.parseFloat(prbQty);

				if((solq == prbq) == false){
					common_Parts.put(key, solutionItem);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return common_Parts;
	}

	public LinkedHashMap<String, String> generateBOPWebServiceValue(String key, LinkedHashMap<String, LinkedHashMap<String, String>> bomLinesArray, String action, String seq_ID){

		LinkedHashMap<String, String> newbomValues = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> newbom = bomLinesArray.get(key);

		newbomValues.put("BOMLINEID", newbom.get("BOMLINEID"));
		newbomValues.put("SAPPLANT", newbom.get("SAPPLANT"));
		newbomValues.put("TOPLEVELPART", newbom.get("TOPLEVELPART"));
		newbomValues.put("HEADERPART", newbom.get("HEADERPART"));
		newbomValues.put("LINESUPPLYMETHOD", newbom.get("LINESUPPLYMETHOD"));
		newbomValues.put("BOPID", newbom.get("BOPID"));
		newbomValues.put("WORKSTATION", newbom.get("WORKSTATION"));
		newbomValues.put("MESBOPINDICATOR", newbom.get("MESBOPINDICATOR"));
		newbomValues.put("REVISION", newbom.get("REVISION"));
		newbomValues.put("PHANTOM", newbom.get("PHANTOM"));
		newbomValues.put("ACTION", action);
		newbomValues.put("SEQUENCE", seq_ID);

		return newbomValues;
	}

	public String getBOMLineCurrentValues(HashMap<String, String> bomLinesArray){

		String parentPartValue = bomLinesArray.get("PARENTPART");
		String childPartValue = bomLinesArray.get("CHILDPART");
		String bomlineValue = bomLinesArray.get("BOMLINEID");
		return "ENGINE_SHOP~"+parentPartValue+"~"+childPartValue+"~"+bomlineValue;
	}
}
