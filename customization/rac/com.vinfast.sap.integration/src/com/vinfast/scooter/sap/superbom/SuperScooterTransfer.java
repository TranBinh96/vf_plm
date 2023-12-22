package com.vinfast.scooter.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.SuperScooterReport;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.dialogs.BOMBOPDialog;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SuperScooterTransfer {

	public static HashMap<String, HashMap<String,String>> traverseProblemItems(BOMBOPDialog transferDlg, BOMManager bomManager, TCComponentBOMLine problemItem,String subgroup){

		boolean toProcess = true;
		new UIGetValuesUtility();
		HashMap<String, HashMap<String,String>> bomDataMap = null;
		new Logger();
		try {

			bomDataMap = new HashMap<String, HashMap<String,String>>();
			HashMap<String, String> bomValuesMap = new HashMap<String, String>();
			bomValuesMap.put("PLATFORM", bomManager.getModel());
			bomValuesMap.put("MODELYEAR", bomManager.getYear());
			bomValuesMap.put("PLANT", bomManager.getPlant());
			bomValuesMap.put("PARTNO", problemItem.getProperty("bl_item_item_id"));
			bomValuesMap.put("MCN", bomManager.getMCN());
			bomValuesMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			bomValuesMap.put("MATERIAL", bomManager.getMaterialCode());
			bomValuesMap.put("ACTION", "D");

			String bomLineID = problemItem.getProperty("VF4_bomline_id").trim();

			if(bomLineID.length() != 0 ){
				for(int i=bomLineID.length(); i<4 ;i++){
					bomLineID = "0"+bomLineID;
				}
				bomValuesMap.put("BOMLINEID", bomLineID);
			}else {
				String[] printValues = new String[] {Integer.toString(bomManager.getSerialNo()),subgroup,problemItem.getProperty("bl_item_item_id"),"-","BOMLine ID is empty. Please fill BOMLine ID",bomValuesMap.get("ACTION"),"Error"};
				bomManager.printReport("PRINT", printValues);
				bomManager.incrementSerialNo();
				toProcess = false;
			}

			String qty = problemItem.getProperty("bl_quantity");

			if(bomLineID.length() == 0 ){

				String[] printValues = new String[] {Integer.toString(bomManager.getSerialNo()),subgroup,problemItem.getProperty("bl_item_item_id"),"-","Quantity is empty. Please fill Quantity",bomValuesMap.get("ACTION"),"Error"};
				bomManager.printReport("PRINT", printValues);
				bomManager.incrementSerialNo();
				toProcess = false;
			}else {
				bomValuesMap.put("QUANTITY", qty);
			}

			String formula =  problemItem.getProperty("bl_formula");

			if(formula.equals("") ==  false){

				formula = formula.replaceAll("\\&", "AND");

				formula = formula.replaceAll("\\|", "OR");

				formula = formula.substring(0, formula.length());

				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(formula);

				while(m.find()) {
					
					if(bomManager.getMaterialCode().equals("")) {
						formula = formula.replace(m.group(1),bomManager.getModel()+"_"+bomManager.getYear());
					}else {
						
						formula = formula.replace(m.group(1),bomManager.getMaterialCode());
					}

				}

				bomValuesMap.put("OPTION", formula);
			}else {
				bomValuesMap.put("OPTION", "");
			}

			bomDataMap.put(bomValuesMap.get("PARTNO")+"~"+bomValuesMap.get("BOMLINEID"), bomValuesMap);

		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if(toProcess ==  false){
			return null;
		}
		return bomDataMap;

	}

	public static HashMap<String, HashMap<String,String>> traverseSolutionItems(BOMBOPDialog transferDlg, BOMManager bomManager, TCComponentBOMLine solutionItem,String subgroup){

		boolean toProcess = true;
		new UIGetValuesUtility();
		HashMap<String, HashMap<String,String>> bomDataMap = null;
		new Logger();
		try {

			bomDataMap = new HashMap<String, HashMap<String,String>>();

			HashMap<String, String> bomValuesMap = new HashMap<String, String>();
			bomValuesMap.put("PLATFORM", bomManager.getModel());
			bomValuesMap.put("MODELYEAR", bomManager.getYear());
			bomValuesMap.put("PLANT", bomManager.getPlant());
			bomValuesMap.put("PARTNO", solutionItem.getProperty("bl_item_item_id"));
			bomValuesMap.put("MCN", bomManager.getMCN());
			bomValuesMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			bomValuesMap.put("MATERIAL", bomManager.getMaterialCode());
			bomValuesMap.put("ACTION", "A");

			String bomLineID = solutionItem.getProperty("VF4_bomline_id").trim();

			if(bomLineID.length() != 0 ){
				for(int i=bomLineID.length(); i<4 ;i++){
					bomLineID = "0"+bomLineID;
				}
				bomValuesMap.put("BOMLINEID", bomLineID);
			}else {
				String[] printValues = new String[] {Integer.toString(bomManager.getSerialNo()),subgroup,solutionItem.getProperty("bl_item_item_id"),"-","BOMLine ID is empty. Please fill BOMLine ID",bomValuesMap.get("ACTION"),"Error"};
				bomManager.printReport("PRINT", printValues);
				toProcess = false;
				bomManager.incrementSerialNo();
			}

			String qty = solutionItem.getProperty("bl_quantity");

			if(bomLineID.length() == 0 ){
				String[] printValues = new String[] {Integer.toString(bomManager.getSerialNo()),subgroup,solutionItem.getProperty("bl_item_item_id"),"-","Quantity is empty. Please fill Quantity",bomValuesMap.get("ACTION"),"Error"};
				bomManager.printReport("PRINT", printValues);
				toProcess = false;
				bomManager.incrementSerialNo();
			}else {
				bomValuesMap.put("QUANTITY", qty);
			}

			String formula =  solutionItem.getProperty("bl_formula");

			if(formula.equals("") ==  false){

				formula = formula.replaceAll("\\&", "AND");

				formula = formula.replaceAll("\\|", "OR");

				//formula = formula.replaceAll("!=", "<>;");

				formula = formula.substring(0, formula.length());

				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(formula);

				while(m.find()) {
					
					String replaceCode = "";
					
					if(bomManager.getMaterialCode().equals("")) {
						
						replaceCode = bomManager.getModel()+"_"+bomManager.getYear();
						
					}else {
						replaceCode = bomManager.getMaterialCode();
					}
					
					formula = formula.replace(m.group(1),replaceCode);
				}

				bomValuesMap.put("OPTION", formula);
			}else {
				bomValuesMap.put("OPTION", "");
			}

			bomDataMap.put(bomValuesMap.get("PARTNO")+"~"+bomValuesMap.get("BOMLINEID"), bomValuesMap);

		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(toProcess ==  false){
			return null;
		}
		return bomDataMap;

	}

	@SuppressWarnings("unchecked")
	public static Set<String> addPart(HashMap<String,HashMap<String,String>> old_items, HashMap<String,HashMap<String,String>> new_items){

		HashMap<String,String> old_BOM = (HashMap<String,String>)old_items.clone();
		HashMap<String,String>  new_BOM = (HashMap<String,String>)new_items.clone();

		Set<String> addoldBOM = old_BOM.keySet();
		Set<String> addNewBOM = new_BOM.keySet();

		addNewBOM.removeAll(addoldBOM);

		return addNewBOM;
	}

	@SuppressWarnings("unchecked")
	public static Set<String> delPart(HashMap<String,HashMap<String,String>> old_items, HashMap<String,HashMap<String,String>> new_items){

		HashMap<String,String> old_BOM = (HashMap<String,String>)old_items.clone();
		HashMap<String,String>  new_BOM = (HashMap<String,String>)new_items.clone();

		Set<String> addoldBOM = old_BOM.keySet();
		Set<String> addNewBOM = new_BOM.keySet();

		addoldBOM.removeAll(addNewBOM);

		return addoldBOM;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, HashMap<String, String>> commonPart(HashMap<String,HashMap<String,String>> old_items, HashMap<String,HashMap<String,String>> new_items){

		HashMap<String,HashMap<String,String>> addSTBOM = (HashMap<String,HashMap<String,String>>) old_items.clone();
		HashMap<String,HashMap<String,String>> addSTPart = (HashMap<String,HashMap<String,String>>)new_items.clone();
		HashMap<String,HashMap<String,String>> common_Parts = new HashMap<String,HashMap<String,String>>();
		Set<String> addBOM = addSTBOM.keySet();
		Set<String> addST = addSTPart.keySet();

		addBOM.retainAll(addST);

		for ( String key : addSTBOM.keySet()) {
			try {

				HashMap<String, String> solutionItem = addSTBOM.get(key);
				HashMap<String, String> problemItem = addSTPart.get(key);

				String solQty = solutionItem.get("QUANTITY");
				String prbQty = problemItem.get("QUANTITY");

				String solOption = solutionItem.get("OPTION");
				String prbOption = problemItem.get("OPTION");

				float solq = Float.parseFloat(solQty);
				float prbq = Float.parseFloat(prbQty);

				if((solq == prbq && solOption.equals(prbOption)) == false){
					common_Parts.put(key, solutionItem);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return common_Parts;
	}

	public static HashMap<String, HashMap<String,String>> traverseProblemItems2(MCNInformation mcn, TCComponentBOMLine problemItem,String subgroup, int count, ArrayList<SuperScooterReport> reportList){

		boolean toProcess = true;
		HashMap<String, HashMap<String,String>> bomDataMap = null;
		try {

			bomDataMap = new HashMap<String, HashMap<String,String>>();
			HashMap<String, String> bomValuesMap = new HashMap<String, String>();
			bomValuesMap.put("PLATFORM", mcn.getPlatForm());
			bomValuesMap.put("MODELYEAR", mcn.getModelYear());
			bomValuesMap.put("PLANT", mcn.getPlant());
			bomValuesMap.put("PARTNO", problemItem.getProperty("bl_item_item_id"));
			bomValuesMap.put("MCN", mcn.getMcnID());
			bomValuesMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			bomValuesMap.put("MATERIAL", mcn.getMaterialCode());
			bomValuesMap.put("ACTION", "D");

			String bomLineID = problemItem.getProperty("VF4_bomline_id").trim();

			if(bomLineID.length() != 0 ){
				for(int i=bomLineID.length(); i<4 ;i++){
					bomLineID = "0"+bomLineID;
				}
				bomValuesMap.put("BOMLINEID", bomLineID);
			}else {
				SuperScooterReport rp = new SuperScooterReport();
				rp.setNo(Integer.toString(count++));
				rp.setSubGroup(subgroup);
				rp.setRecord(problemItem.getProperty("bl_item_item_id"));
				rp.setBomlineId("-");
				rp.setMessage("BOMLine ID is empty. Please fill BOMLine ID");
				rp.setAction("-");
				rp.setType(UpdateType.UPDATE_BODY_ERROR);
				reportList.add(rp);
				toProcess = false;
				count++;
			}

			String qty = problemItem.getProperty("bl_quantity");

			if(bomLineID.length() == 0 ){

				bomValuesMap.put("QUANTITY", "1");
			}else {
				bomValuesMap.put("QUANTITY", qty);
			}

			String formula =  problemItem.getProperty("bl_formula");

			if(formula.equals("") ==  false){

				formula = formula.replaceAll("\\&", "AND");

				formula = formula.replaceAll("\\|", "OR");

				//formula = formula.replaceAll("!=", "<>;");

				formula = formula.substring(0, formula.length());

				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(formula);

				while(m.find()) {
					
					if(mcn.getMaterialCode().equals("")) {
						formula = formula.replace(m.group(1),mcn.getPlatForm()+"_"+mcn.getModelYear());
					}else {
						
						formula = formula.replace(m.group(1),mcn.getMaterialCode());
					}

				}

				bomValuesMap.put("OPTION", formula);
			}else {
				bomValuesMap.put("OPTION", "");
			}

			bomDataMap.put(bomValuesMap.get("PARTNO")+"~"+bomValuesMap.get("BOMLINEID"), bomValuesMap);

		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if(toProcess ==  false){
			return null;
		}
		return bomDataMap;

	}

	public static HashMap<String, HashMap<String,String>> traverseSolutionItems2(MCNInformation mcn, TCComponentBOMLine solutionItem,String subgroup, int count,ArrayList<SuperScooterReport> reportList){

		boolean toProcess = true;
		HashMap<String, HashMap<String,String>> bomDataMap = null;
		try {

			bomDataMap = new HashMap<String, HashMap<String,String>>();

			HashMap<String, String> bomValuesMap = new HashMap<String, String>();
			bomValuesMap.put("PLATFORM", mcn.getPlatForm());
			bomValuesMap.put("MODELYEAR", mcn.getModelYear());
			bomValuesMap.put("PLANT", mcn.getPlant());
			bomValuesMap.put("PARTNO", solutionItem.getProperty("bl_item_item_id"));
			bomValuesMap.put("MCN", mcn.getMcnID());
			bomValuesMap.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			bomValuesMap.put("MATERIAL", mcn.getMaterialCode());
			bomValuesMap.put("ACTION", "A");

			String bomLineID = solutionItem.getProperty("VF4_bomline_id");

			if(bomLineID.length() != 0 ){
				for(int i=bomLineID.length(); i<4 ;i++){
					bomLineID = "0"+bomLineID;
				}
				bomValuesMap.put("BOMLINEID", bomLineID);
			}else {
				SuperScooterReport rp = new SuperScooterReport();
				rp.setNo(Integer.toString(count++));
				rp.setSubGroup(subgroup);
				rp.setRecord(solutionItem.getProperty("bl_item_item_id"));
				rp.setBomlineId("-");
				rp.setMessage("BOMLine ID is empty. Please fill BOMLine ID");
				rp.setAction("-");
				rp.setType(UpdateType.UPDATE_BODY_ERROR);
				reportList.add(rp);
				toProcess = false;
				count++;
			}

			String qty = solutionItem.getProperty("bl_quantity");

			if(bomLineID.length() == 0 ){

				bomValuesMap.put("QUANTITY", "1");
			}else {
				bomValuesMap.put("QUANTITY", qty);
			}

			String formula =  solutionItem.getProperty("bl_formula");

			if(formula.equals("") ==  false){

				formula = formula.replaceAll("\\&", "AND");

				formula = formula.replaceAll("\\|", "OR");

				//formula = formula.replaceAll("!=", "<>;");

				formula = formula.substring(0, formula.length());

				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(formula);

				while(m.find()) {
					
					String replaceCode = "";
					
					if(mcn.getMaterialCode().equals("")) {
						
						replaceCode = mcn.getPlatForm()+"_"+mcn.getModelType();
						
					}else {
						replaceCode = mcn.getMaterialCode();
					}
					
					formula = formula.replace(m.group(1),replaceCode);
				}

				bomValuesMap.put("OPTION", formula);
			}else {
				bomValuesMap.put("OPTION", "");
			}

			bomDataMap.put(bomValuesMap.get("PARTNO")+"~"+bomValuesMap.get("BOMLINEID"), bomValuesMap);

		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(toProcess ==  false){
			return null;
		}
		return bomDataMap;

	}

}
