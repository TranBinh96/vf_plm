package com.vinfast.scooter.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.ScooterSuperBomBopDataSend;
import com.vinfast.integration.model.SuperScooterReport;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SuperScooterDataHelper {
	HashMap<String,HashMap<String,String>> prDataMap = new HashMap<String,HashMap<String,String>>();
	HashMap<String,HashMap<String,String>> solBOMDataMap = new HashMap<String,HashMap<String,String>>();
	HashMap<String, ArrayList<HashMap<String,String>>> solBOPDataMap = new HashMap<String, ArrayList<HashMap<String,String>>>();
	
	int count  = 0;
	public HashMap<String, HashMap<String, String>> getPrDataMap() {
		return prDataMap;
	}

	public void setPrDataMap(HashMap<String, HashMap<String, String>> prDataMap) {
		this.prDataMap = prDataMap;
	}

	public HashMap<String, HashMap<String, String>> getSolBOMDataMap() {
		return solBOMDataMap;
	}

	public void setSolBOMDataMap(HashMap<String, HashMap<String, String>> solBOMDataMap) {
		this.solBOMDataMap = solBOMDataMap;
	}

	public HashMap<String, ArrayList<HashMap<String, String>>> getSolBOPDataMap() {
		return solBOPDataMap;
	}

	public void setSolBOPDataMap(HashMap<String, ArrayList<HashMap<String, String>>> solBOPDataMap) {
		this.solBOPDataMap = solBOPDataMap;
	}
	
	ArrayList<ScooterSuperBomBopDataSend> processImpactedData(String itemID, ArrayList<SuperScooterReport> reportList, TCComponentItemRevision item) {
		ArrayList<ScooterSuperBomBopDataSend> listData2Send = new ArrayList<ScooterSuperBomBopDataSend>();
		Set<String> deletedParts = SuperScooterTransfer.delPart(prDataMap, solBOMDataMap);
		Set<String> addedParts = SuperScooterTransfer.addPart(prDataMap, solBOMDataMap);
		HashMap<String, HashMap<String, String>> commonPartsMap = SuperScooterTransfer.commonPart(prDataMap, solBOMDataMap);
		Set<String> commonParts = commonPartsMap.keySet();

		if(deletedParts.isEmpty() == false){
			Iterator<String> iterator = deletedParts.iterator();
			while(iterator.hasNext()) {
				ScooterSuperBomBopDataSend data2Send = new ScooterSuperBomBopDataSend();
				String delKey = iterator.next();
				HashMap<String, String> bomMap = prDataMap.get(delKey);
				SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++), itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"), "Delete part in BOM" ,bomMap.get("ACTION"),"Ready");
				rp.setType(UpdateType.UPDATE_BODY_INFO);
				reportList.add(rp);
				data2Send.setBomData(bomMap);
				data2Send.setItem(item);
				listData2Send.add(data2Send);
			}
		}

		if(addedParts.isEmpty() == false){

			Iterator<String> iterator = addedParts.iterator();

			while(iterator.hasNext()) {
				ScooterSuperBomBopDataSend data2Send = new ScooterSuperBomBopDataSend();
				String addKey = iterator.next();
				HashMap<String, String> bomMap = solBOMDataMap.get(addKey);
				SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++), itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"), "Add new part in BOM" ,bomMap.get("ACTION"),"Ready");
				rp.setType(UpdateType.UPDATE_BODY_INFO);
				reportList.add(rp);
				ArrayList<HashMap<String, String>> bopMap = solBOPDataMap.get(addKey);
				data2Send.setBomData(bomMap);
				for(HashMap<String, String> BOP : bopMap) {
					SuperScooterReport rp2 = new SuperScooterReport(Integer.toString(count++),itemID,BOP.get("BOPID"),BOP.get("BOMLINEID"),"Add new part in BOP", BOP.get("ACTION"),"Ready");
					rp2.setType(UpdateType.UPDATE_BODY_INFO);
					reportList.add(rp2);
					data2Send.setSingleBopData(BOP);
				}
				data2Send.setItem(item);
				listData2Send.add(data2Send);
			}
		}

		if(commonParts.isEmpty() == false){

			Iterator<String> iterator = commonParts.iterator();

			while(iterator.hasNext()) {
				ScooterSuperBomBopDataSend data2Send = new ScooterSuperBomBopDataSend();
				String commKey = iterator.next();
				HashMap<String, String> bomMap = solBOMDataMap.get(commKey);
				bomMap.replace("ACTION", bomMap.get("ACTION"), "C");
				SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++), itemID,bomMap.get("PARTNO"),bomMap.get("BOMLINEID"), "Change part in BOM" ,bomMap.get("ACTION"),"Ready");
				rp.setType(UpdateType.UPDATE_BODY_INFO);
				reportList.add(rp);
				data2Send.setSingleBopData(bomMap);
				data2Send.setItem(item);
				listData2Send.add(data2Send);
			}
		}
		return listData2Send;
	}
	
	ArrayList<ScooterSuperBomBopDataSend> getDeletedOperationNoPart(MCNInformation mcn, TCComponent[] problemItems, ArrayList<SuperScooterReport> reportList) throws Exception {
		ArrayList<ScooterSuperBomBopDataSend> data2SendList = new ArrayList<ScooterSuperBomBopDataSend>();
		if(problemItems != null) {

			for(TCComponent problemItem : problemItems){

				TCComponentItemRevision problem = (TCComponentItemRevision) problemItem;

				CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(TCHelper.getInstance().session, problem);

				TCComponentBOMWindow operationWindow = output[0].bomWindow;
				TCComponentBOMLine operationLine = output[0].bomLine;

				if(UIGetValuesUtility.hasMaterials(operationLine) == false){

					TCHelper.getInstance().dmService.getProperties(new TCComponent[] {operationLine},new String[] {"bl_item_item_id","bl_rev_item_revision_id","VF4_bomline_id","vf4_operation_type","bl_rev_vf3_transfer_to_sap","bl_rev_vf5_line_supply_method"});

					String ID = operationLine.getProperty("bl_item_item_id");
					String RevID = operationLine.getProperty("bl_rev_item_revision_id");
					String HT = "";
					if(mcn.getMaterialCode().equals("")) {
						HT = mcn.getPlatForm()+"_"+mcn.getModelYear();
					}else {
						HT = mcn.getMaterialCode();
					}

					if(HT.equals("")) {
						SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),"OWP",ID+"/"+RevID,"","Header Part information missing on operation. Fill in Bomline","-","Error");
						rp.setType(UpdateType.UPDATE_BODY_ERROR);
						reportList.add(rp);
					}else {

						String WS = operationLine.getProperty("bl_rev_vf3_transfer_to_sap");
						String[] split = WS.split(",");

						String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
						if(LSM.equals("")) {
							LSM = "JIS";
						}

						String MES = operationLine.getProperty("vf4_operation_type");
						if(MES.equals("") || MES.equals("NA")) {
							MES = "N";
						}else {
							MES = "Y";
						}

						for(String workstation : split) {
							HashMap<String, String> BOP_Values = new HashMap<String, String>();
							BOP_Values.put("SAPPLANT", mcn.getSapID());
							BOP_Values.put("TOPLEVELPART", HT);
							BOP_Values.put("HEADERPART", HT);
							BOP_Values.put("BOMLINEID", "");
							BOP_Values.put("ACTION", "D");
							BOP_Values.put("WORKSTATION", workstation);
							BOP_Values.put("LINESUPPLYMETHOD",LSM);
							BOP_Values.put("BOPID",ID);
							BOP_Values.put("MESBOPINDICATOR",MES);
							BOP_Values.put("REVISION",RevID);
							BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
							ScooterSuperBomBopDataSend data2Send = new ScooterSuperBomBopDataSend();
							data2Send.setSingleBopData(BOP_Values);
							data2Send.setItem(problem);
							data2SendList.add(data2Send);
							SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),"OWP",BOP_Values.get("BOPID")+"/"+BOP_Values.get("REVISION"),"", "Delete Operation no part",BOP_Values.get("ACTION"),"Ready");
							rp.setType(UpdateType.UPDATE_BODY_INFO);
							reportList.add(rp);
						}
					}
				}
				operationWindow.close();
			}
		}
		return data2SendList;
	}
	
	ArrayList<ScooterSuperBomBopDataSend> getAddedOperationNoPart(MCNInformation mcn, TCComponent[] solutionItems, ArrayList<SuperScooterReport> reportList, TCComponent traverseStructure) throws Exception {
		ArrayList<ScooterSuperBomBopDataSend> data2SendList = new ArrayList<ScooterSuperBomBopDataSend>();
		if(solutionItems != null) {

			for (TCComponent solutionItem : solutionItems)
			{

				TCComponentItemRevision operations = (TCComponentItemRevision)solutionItem;

				String ID = operations.getProperty("item_id");

				ArrayList<TCComponent> operationsList = UIGetValuesUtility.searchPartsInStruture(TCHelper.getInstance().session, new String[] {ID}, traverseStructure);

				if(operationsList.isEmpty() == false) {

					TCComponentBOMLine operationLine = (TCComponentBOMLine)operationsList.get(0);

					if(UIGetValuesUtility.hasMaterials(operationLine) == false){

						TCHelper.getInstance().dmService.getProperties(new TCComponent[] {operationLine},new String[] {"bl_item_item_id","bl_rev_item_revision_id","VF4_bomline_id","vf4_operation_type","bl_rev_vf3_transfer_to_sap","bl_rev_vf5_line_supply_method"});

						String RevID = operationLine.getProperty("bl_rev_item_revision_id");
						String HT = mcn.getPlatForm()+"_"+mcn.getModelYear();

						if(HT.equals("")) {							
							SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),"OWP",ID+"/"+RevID,"","Header Part information missing on operation. Fill in Bomline","-","Error");
							rp.setType(UpdateType.UPDATE_BODY_ERROR);
							reportList.add(rp);
						}else {

							String LSM = operationLine.getProperty("bl_rev_vf5_line_supply_method");
							if(LSM.equals("")) {
								LSM = "JIS";
							}

							String MES = operationLine.getProperty("vf4_operation_type");
							if(MES.equals("") || MES.equals("NA")) {
								MES = "N";
							}else {
								MES = "Y";
							}

							for(TCComponent operation : operationsList) {

								HashMap<String, String> BOP_Values = new HashMap<String, String>();
								BOP_Values.put("SAPPLANT", mcn.getSapID());
								BOP_Values.put("TOPLEVELPART", HT);
								BOP_Values.put("HEADERPART", HT);
								BOP_Values.put("BOMLINEID", "");
								BOP_Values.put("ACTION", "A");
								BOP_Values.put("WORKSTATION", UIGetValuesUtility.getWorkStationID((TCComponentBOMLine)operation, "bl_rev_object_name"));
								BOP_Values.put("LINESUPPLYMETHOD",LSM);
								BOP_Values.put("BOPID",ID);
								BOP_Values.put("MESBOPINDICATOR",MES);
								BOP_Values.put("REVISION",RevID);
								BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
								ScooterSuperBomBopDataSend data2Send = new ScooterSuperBomBopDataSend();
								data2Send.setSingleBopData(BOP_Values);
								data2Send.setItem(operations);
								data2SendList.add(data2Send);
								SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++),"OWP",String.format("%s/%s",BOP_Values.get("BOPID"),BOP_Values.get("REVISION")),"", "Add Operation no part",BOP_Values.get("ACTION"),"Ready");
								rp.setType(UpdateType.UPDATE_BODY_INFO);
								reportList.add(rp);
							}
						}
					}
				}
			}
		}
		return data2SendList;
	}
}
