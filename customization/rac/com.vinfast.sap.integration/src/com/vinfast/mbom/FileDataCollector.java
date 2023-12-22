package com.vinfast.mbom;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class FileDataCollector {

	//HashMap<MG,HashMap<SB,ID>>
	private HashMap<String,HashMap<String, ArrayList<DataMap>>> dataCollector = null;
	private HashMap<String, TCComponentBOMLine> bomlineTag =  null;
	private HashMap<String, ArrayList<String>> consumables = null;
	boolean isLoaded = false;
	TCSession session = null;
	private HashMap<String, String> idImageFile = new HashMap<String, String>();
	private String currentLine = "";
	private String currentGroup = "";
	public FileDataCollector() {

	}

	public FileDataCollector(File file, String sheetName) {
		session = (TCSession)AIFUtility.getCurrentApplication().getSession();
		readTemplate(file, sheetName);
	}
	/**
	 * @apiNote Reads the excel template and collect data
	 * @param file
	 * @param sheetName
	 * @return
	 */
	private void readTemplate(File file, String sheetName) {
		String mainGroup = "";
		String subGroup = "";
		try  
		{  
			FileInputStream fileStream = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(fileStream);   
			XSSFSheet sheet = workbook.getSheet(sheetName);
			if(sheet == null) {
				workbook.close();
				isLoaded = false;
				MessageBox.post("Error in loading template. Please contact Teamcenter Admin", "Error", MessageBox.INFORMATION);
				return;
			}else {
				dataCollector = new HashMap<String,HashMap<String, ArrayList<DataMap>>>();
				bomlineTag =  new HashMap<String, TCComponentBOMLine>();
				consumables = new HashMap<String, ArrayList<String>>();
				Iterator<Row> rowIterator = sheet.iterator(); 
				while (rowIterator.hasNext())                 
				{  
					Row row = rowIterator.next();
					if(row.getRowNum() != 0) {
						DataMap map = new DataMap();
						String MG = getCellText(row.getCell(0));
						if(MG.isEmpty()==false && mainGroup.equals(MG) == false) {
							mainGroup = MG;
						}
						String SG = getCellText(row.getCell(1));
						if(SG.isEmpty()==false && subGroup.equals(SG) == false) {
							subGroup = SG;
						}
						String ID = getCellText(row.getCell(2));
						map.setID(ID);
						String MMG = getCellText(row.getCell(3));
						map.setModuleGroup(MMG);
						String MM = getCellText(row.getCell(4));
						if(MM.isEmpty()==false && subGroup.equals(MM) == false) {
							subGroup = MM;
						}
						map.setMainModule(MM);
						String MN = getCellText(row.getCell(5));
						if(MN.isEmpty()==false && subGroup.equals(MN) == false) {
							subGroup = subGroup+"_"+MN;
						}
						map.setModuleName(MN);
						String QTY = getCellText(row.getCell(6));
						if(QTY.equals("")) {
							QTY = "1";
						}
						map.setLines(QTY);

						if((MMG.isEmpty() && MM.isEmpty() && MN.isEmpty() && ID.isEmpty()) == false) {

							if(SG.isEmpty() == false || MN.isEmpty()== false) {
								setSubGroup(mainGroup, subGroup.replace("/", "_"));
							}
							if(ID.isEmpty()) {
								setSubGroupData(mainGroup, subGroup.replace("/", "_"), map);
							}else {
								setSubGroupConsumables(mainGroup, subGroup.replace("/", "_"), ID);
							}
						}
					}
				}
				isLoaded = true;
			}
			workbook.close();
		}  
		catch(Exception e)  
		{  
			isLoaded = false;
			e.printStackTrace();  
		}
		return;  
	}

	private static String getCellText(Cell cell) {

		if(cell != null) {
			return cell.getStringCellValue();
		}
		return "";
	}

	public void setSubGroup(String mainGroup, String subGroup) {

		HashMap<String, ArrayList<DataMap>> subGroupData = dataCollector.get(mainGroup);
		if(subGroupData == null && subGroup != null) {
			subGroupData = new HashMap<String, ArrayList<DataMap>>();
			subGroupData.put(subGroup, null);
		}
		dataCollector.put(mainGroup, subGroupData);
	}
	public void setSubGroupData(String mainGroup, String subGroup, DataMap map) {

		HashMap<String, ArrayList<DataMap>> subGroupData = dataCollector.get(mainGroup);
		ArrayList<DataMap> dataMap = subGroupData.get(subGroup);
		if(dataMap == null) {
			dataMap = new ArrayList<DataMap>();
			dataMap.add(map);
		}else {
			if(map.getID().isEmpty() == false) {
				DataMap oldMap = dataMap.get(0);
				String id = oldMap.getID();
				if(id.isEmpty()) {
					oldMap.setID(map.getID());
				}else {
					id = id +";"+map.getID();
					oldMap.setID(id);
				}
			}
		}
		subGroupData.put(subGroup, dataMap);
		dataCollector.put(mainGroup, subGroupData);
	}

	public void setBOMLine(String line,String group, TCComponentBOMLine value) {

		String key = line;
		if(group.isEmpty() == false) {
			key = key+"_"+group;
		}
		bomlineTag.put(key, value);
	}

	public TCComponentBOMLine getBOMLine(String line,String group) {
		String key = line;
		if(group.isEmpty() == false) {
			key = key+"_"+group;
		}
		return bomlineTag.get(key);
	}

	public ArrayList<String> getShopLines(){

		ArrayList<String> lines = new ArrayList<String>();
		lines.addAll(dataCollector.keySet());
		return lines;
	}

	public String[] getShopLineGroups(String line) {
		HashMap<String, ArrayList<DataMap>> groups = dataCollector.get(line);
		return groups.keySet().toArray(new String[0]);
	}

	public HashMap<String,ArrayList<DataMap>> get(String key){
		return dataCollector.get(key);
	}

	public ArrayList<String> getSubGroupConsumables(String line,String group) {
		String key = line;
		if(group.isEmpty() == false) {
			key = key+"_"+group;
		}
		ArrayList<String> IDs = consumables.get(key);
		if(IDs != null) {
			return IDs;
		}
		return null;
	}

	public void setSubGroupConsumables(String line,String group,String ID) {
		String key = line;
		if(group.isEmpty() == false) {
			key = key+"_"+group;
		}
		ArrayList<String> value = null;
		if(consumables.containsKey(key)) {
			value =  consumables.get(key);
		}else {
			value =  new ArrayList<String>();

		}
		value.add(ID);
		consumables.put(key, value);
	}

	public void setThumbnail(String ID, String imageFile) {
		if(idImageFile.containsKey(ID) == false) {
			idImageFile.put(ID, imageFile);
		}
	}

	public String getThumbnail(String ID) {
		if(idImageFile.containsKey(ID)) {
			return idImageFile.get(ID);
		}else {
			return null;
		}
	}
	public void setCurrentGroup(String group) {
		currentGroup = group;
	}
	public void setCurrentLine(String line) {
		currentLine = line;
	}
	public String getCurrentGroup() {
		return currentGroup;
	}
	public String getCurrentLine() {
		return currentLine;
	}
}
