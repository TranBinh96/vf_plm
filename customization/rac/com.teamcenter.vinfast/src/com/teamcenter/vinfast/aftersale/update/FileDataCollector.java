package com.teamcenter.vinfast.aftersale.update;

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
	//private HashMap<String,HashMap<String, ArrayList<DataMap>>> level12Collector = new HashMap<String,HashMap<String, ArrayList<DataMap>>>();
	//private HashMap<String,HashMap<String, ArrayList<DataMap>>> level23Collector = new HashMap<String,HashMap<String, ArrayList<DataMap>>>();
	private HashMap<String,HashMap<String, ArrayList<DataMap>>> dataCollector = new HashMap<String,HashMap<String, ArrayList<DataMap>>>();
	private HashMap<String, TCComponentBOMLine> bomlineTag =  new HashMap<String, TCComponentBOMLine>();
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
		String level1Name = "";
		String level2Name = "";
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
				Iterator<Row> rowIterator = sheet.iterator(); 
				while (rowIterator.hasNext())                 
				{  
					Row row = rowIterator.next();
					if(row.getRowNum() != 0) {
						DataMap map = new DataMap();
						String level1 = getCellText(row.getCell(0));
						if(level1.isEmpty()==false && level1Name.equals(level1) == false) {
							level1Name = level1;
						}
						String level2 = getCellText(row.getCell(1));
						if(level2.isEmpty()==false && level2Name.equals(level2) == false) {
							level2Name = level2;
						}
						String level3Name = getCellText(row.getCell(2));

						String MMG = getCellText(row.getCell(7));
						map.setModuleGroup(MMG);
						
						String MM = getCellText(row.getCell(8));
						map.setMainModule(MM);
						
						String MN = getCellText(row.getCell(9));
						map.setModuleName(MN);

						if((MMG.isEmpty() && MM.isEmpty() && MN.isEmpty()) == false) {

							if((level1Name.isEmpty() == false && level2Name.isEmpty() == false) && level3Name.isEmpty()) {
								setSubGroup(level1Name, level2Name);
								setSubGroupData(level1Name, level2Name, map);
							}
							if(level3Name.isEmpty() == false) {
								setSubGroup(level2Name, level3Name);
								setSubGroupData(level2Name, level3Name, map);
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

	public void setSubGroup(String parent, String child) {

		HashMap<String, ArrayList<DataMap>> childData = dataCollector.get(parent);
		if(childData == null && child != null) {
			childData = new HashMap<String, ArrayList<DataMap>>();
			childData.put(child, null);
		}
		dataCollector.put(parent, childData);
	}
	
	public void setSubGroupData(String parent, String child, DataMap map) {

		HashMap<String, ArrayList<DataMap>> childData = dataCollector.get(parent);
		ArrayList<DataMap> dataMap = childData.get(child);
		if(dataMap == null) {
			dataMap = new ArrayList<DataMap>();
			dataMap.add(map);
		}else {
			dataMap.add(map);
		}
		childData.put(child, dataMap);
		dataCollector.put(parent, childData);
	}
	
	public ArrayList<DataMap> getDataMap(String parent, String child) {
		HashMap<String, ArrayList<DataMap>> childs = dataCollector.get(parent);
		if(childs != null) {
			return childs.get(child);
		}
		return null;
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
