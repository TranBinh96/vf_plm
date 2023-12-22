package com.teamcenter.rac.jes.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class UpdateJESAction extends AbstractHandler {
	/**
	 * The constructor.
	 */
	TCSession session = null ;
	BufferedWriter output = null;
	FileReader fr = null;
	String shop_name;

	TCComponentItemRevision operationRev = null ;
	public UpdateJESAction() {
	}


	public boolean validateSelection(InterfaceAIFComponent[] aifComp)
	{
		if (aifComp.length != 1)
		{
			return false ;
		}

		if (!(aifComp[0] instanceof TCComponentBOPLine))
		{
			return false ;
		}

		if (!(aifComp[0].getType().equals("Mfg0BvrOperation"))) {
			return false ;
		}

		return true ;
	}
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */

	public ArrayList<TCComponent> whereUsedMEOP(TCComponent itemRev , String Returnobjectype){
		ArrayList<TCComponent> usedMEOP = new ArrayList<TCComponent>();
		TCComponent[]  usedParts = null;
		try {
			usedParts = itemRev.whereUsed(TCComponent.WHERE_USED_ALL);
			for(int i=0;i<usedParts.length;i++){

				if(usedParts[i].getTypeComponent().getType().equals(Returnobjectype)){
					usedMEOP.add(usedParts[i]);
				}
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return usedMEOP;
	}

	TCComponentItem searchWS(String itemID )
	{
		
		TCComponentItem item = null ;
		try
		{
			SavedQueryService QRservices = SavedQueryService.getService(session);

			//System.out.println("#################################got session and service################################################");
			FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
			System.out.println("gor finder query");
			String  name[]={"Item..."};
			String desc[]={""};
			qurey.queryNames=name;
			qurey.queryDescs=desc;
			qurey.queryType=0;

			qry[0]=qurey;

			//System.out.println("got find qryyyyyyyy session and service################################################");

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[]result=responce1.savedQueries;

			SavedQueryInput qc=new SavedQueryInput();
			SavedQueryInput qc_v[]=new SavedQueryInput[1];

			qc.query=(TCComponentQuery) result[0];
			qc.entries= new String[] {"Item ID"};
			qc.values=new String[] {itemID};
			qc_v[0]=qc;

			//System.out.println("########################################got find qryyyyyyyy ################################################");
			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results=responce.arrayOfResults;

			if(results[0].numOfObjects!=0){

				//isPartSourceable =  false ;
				TCComponent[] tcComp = results[0].objects ;
				item = (TCComponentItem)tcComp[0] ;
			}
			else
			{
				System.out.println("NO Object FOUND");
			}
			//System.out.println("##############result"+folder[0].toString());
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return item ;
	}

	public Object execute(ExecutionEvent event){

		try
		{
			@SuppressWarnings("unused")
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			
			Hashtable<String, String> OperationList = new Hashtable<String, String>() ;
			Hashtable<String, String> equipmentList = new Hashtable<String, String>() ;
			Vector<Hashtable<String, String>> equipmentVec = new Vector<Hashtable<String, String>>() ;
			Hashtable<String, String> materialList = new Hashtable<String, String>() ;
			Vector<Hashtable<String, String>> materialVec = new Vector<Hashtable<String, String>>() ;
			
			session = (TCSession) AIFUtility.getCurrentApplication().getSession() ;
			InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents() ;
			
			boolean is_valid = validateSelection(aifComp) ;
			if (!is_valid)
			{
				MessageBox.post("No a Valid Selection. Please select valid object.", "Error", MessageBox.ERROR);
				return null;
			}
			
			//GET OPERATION PROPERTIES
			TCComponentBOPLine operationLine = (TCComponentBOPLine) aifComp[0] ;
			getparent(operationLine);

			operationRev = operationLine.getItemRevision() ;
			String operationRevNumber = operationRev.getProperty("item_revision_id");
			String OperationName = operationLine.getProperty("bl_item_object_name") + " / " + operationRev.getProperty("vf5_viet_description");
			String OperationID = operationLine.getProperty("bl_rev_awp0Item_item_id");

			AIFComponentContext[] context = operationLine.getRelated("bl_me_activity_lines") ;
			TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine)context[0].getComponent() ;
			String Time = activityLine.getProperty("al_activity_duration_time") ;
			double d = Double.parseDouble(Time) ;
			DecimalFormat df = new DecimalFormat("#.##");
			String analysisTime = df.format(d) ;

			String stationObjectStr = operationLine.getProperty("bl_formatted_parent_name") ; 

			String Station = "" ;
			String StationName = "" ;
			try
			{
				String[] tempStr = stationObjectStr.split("/") ;
				Station = tempStr[0] ;
			}
			catch(Exception e)
			{
				Station = stationObjectStr ;
			}

			ArrayList<TCComponent> targetWorkstations = whereUsedMEOP(operationRev , "Mfg0MEProcStatnRevision");

			if (targetWorkstations.size() != 0)
			{
				for(TCComponent targetWorkstation:targetWorkstations){

					if (targetWorkstation.getProperty("item_id").equals(Station))
					{
						StationName = targetWorkstation.getProperty("object_name") ;
						break ;
					}
				}
			}

			OperationList.put("ID", OperationID) ;
			OperationList.put("Name", OperationName) ;
			OperationList.put("Station", StationName) ;
			OperationList.put("Time", analysisTime) ;
			OperationList.put("RevisionID", operationRevNumber);

			System.out.println();
			TCComponent[] allMaterials = operationLine.getRelatedComponents("Mfg0all_material") ;
			TCComponent[] allEquipments = operationLine.getRelatedComponents("Mfg0used_equipment") ;

			for (int inx = 0 ; inx < allMaterials.length ; inx ++){

				materialList = new Hashtable<String, String>() ;

				if (allMaterials[inx].getProperty("bl_quantity").equals(""))
				{
					materialList.put("Quantity","1") ;
				}
				else
				{
					materialList.put("Quantity",allMaterials[inx].getProperty("bl_quantity")) ;

				}
				materialList.put("ID",allMaterials[inx].getProperty("bl_item_item_id")) ;
				materialList.put("Name",allMaterials[inx].getProperty("bl_item_object_name")) ;
				String torque = allMaterials[inx].getProperty("VF3_torque_info") ;
				materialList.put("Torque", torque) ;

				materialVec.add(materialList) ;
			}

			for (int inx = 0 ; inx < allEquipments.length ; inx ++){

				equipmentList = new Hashtable<String, String>() ;
				equipmentList.put("Name",allEquipments[inx].getProperty("bl_item_object_name")) ;
				equipmentList.put("Bit",allEquipments[inx].getProperty("VF4_bit")) ;
				equipmentList.put("Socket",allEquipments[inx].getProperty("VF4_socket")) ;
				equipmentList.put("Extension",allEquipments[inx].getProperty("VF4_extension")) ;
				equipmentVec.add(equipmentList) ;
			}

			if(materialVec.size() < 43){
				populateReport(OperationList , materialVec , equipmentVec , operationRev) ;
				
			}else{
				MessageBox.post("Material count must be less than templete rows (i.e 40). Please contact system admin.", "Error", MessageBox.ERROR);
				return null;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			MessageBox.post("ERROR", "Please contact Admin", MessageBox.ERROR);
		}
		return null;
	}

	public TCComponent getparent(TCComponentBOMLine bom_line_parent2 )
	{
		try {
			
			
			TCComponent parent = bom_line_parent2.getReferenceProperty("bl_parent");
			TCComponentBOMLine bom_line_parent = (TCComponentBOMLine) parent;
			if( bom_line_parent.getProperty("bl_level_starting_0").equals("1"))
			{
				shop_name = bom_line_parent.getProperty("bl_item_object_name");
			}
			else
			{
				getparent(bom_line_parent);
			}
			
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private void populateReport(Hashtable<String, String> operationList,
			Vector<Hashtable<String, String>> materialVec,
			Vector<Hashtable<String, String>> equipmentVec , TCComponentItemRevision revObj) {
		// TODO Auto-generated method stub

		InputStream fileOut = null ;
		XSSFWorkbook workbook = null ;

		try{
			File updatefile = updateJES(revObj);
			fileOut = new FileInputStream(updatefile);
			workbook = new XSSFWorkbook(fileOut) ;
			XSSFSheet worksheet = workbook.getSheetAt(0);
			//HEADER ROW
			updatefile.setWritable(true);
			if (worksheet.getRow(1) == null)
			{
			}
			else
			{
			}

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");  
			LocalDateTime now = LocalDateTime.now();  

			//Modified Date ROW
			XSSFRow modifyDateRow = null ;
			if (worksheet.getRow(3) == null)
			{
				modifyDateRow = worksheet.createRow(3) ;
			}
			else
			{
				modifyDateRow = worksheet.getRow(3);
			}

			XSSFCell modifyDateCell = null ;
			modifyDateCell = modifyDateRow.getCell(CellReference.convertColStringToIndex("S")) ;
			if (modifyDateCell == null)
			{
				modifyDateCell = modifyDateRow.createCell(CellReference.convertColStringToIndex("S")) ;
			}
			modifyDateCell.setCellValue(dtf.format(now));
			
			//thanhpn6 add revision number
			updateRevNumber(workbook, worksheet, operationList.get("RevisionID"));
			//thanhpn6 finish add revision number
			
			//Operation ROW
			XSSFRow operationRow = null ;
			if (worksheet.getRow(10) == null)
			{
				operationRow = worksheet.createRow(10) ;
			}
			else
			{
				operationRow = worksheet.getRow(10);
			}
			
			XSSFRow modelRow = worksheet.getRow(3);
			XSSFCell modelCell  = modelRow.getCell(CellReference.convertColStringToIndex("I"));
			modelCell.setCellValue(shop_name);
			

			XSSFCell nameCell = operationRow.getCell(CellReference.convertColStringToIndex("A")) ;
			nameCell.setCellValue(operationList.get("Name").toUpperCase());
			//nameCell.setCellStyle(style);

			XSSFCell IDCell = operationRow.getCell(CellReference.convertColStringToIndex("G")) ;
			IDCell.setCellValue(operationList.get("ID").toUpperCase());
			//IDCell.setCellStyle(style);

			XSSFCell timeCell = operationRow.getCell(CellReference.convertColStringToIndex("I")) ;
			timeCell.setCellValue(operationList.get("Time").toUpperCase());
			//timeCell.setCellStyle(style);

			XSSFCell stationCell = operationRow.getCell(CellReference.convertColStringToIndex("J")) ;
			stationCell.setCellValue(operationList.get("Station").toUpperCase());
			//stationCell.setCellStyle(style);
			int rowCount = worksheet.getLastRowNum();
			for (int count = 24; count <= rowCount; count++) {

				if (count == 32)
					continue;
				XSSFRow row = worksheet.getRow(count);
				if (row == null)
					continue;

				XSSFCell A = row.getCell(CellReference.convertColStringToIndex("A"));
				if (A == null)
					continue;
				A.setCellValue("");

				XSSFCell B = row.getCell(CellReference.convertColStringToIndex("B"));
				if (B == null)
					continue;
				B.setCellValue("");

				XSSFCell E = row.getCell(CellReference.convertColStringToIndex("E"));
				if (E == null)
					continue;
				E.setCellValue("");

				XSSFCell J = row.getCell(CellReference.convertColStringToIndex("J"));
				if (J == null)
					continue;
				J.setCellValue("");

				XSSFCell K = row.getCell(CellReference.convertColStringToIndex("K"));
				if (K == null)
					continue;
				K.setCellValue("");

			}
			//MATERIAL LIST
			for (int i = 0 ; i < materialVec.size() ; i ++){
				Hashtable<String, String> materialTable = materialVec.get(i) ;
				XSSFRow materialRow = null ;
				if (i < 6)
				{
					materialRow = worksheet.getRow(24 + i);
				}
				else
				{
					materialRow = worksheet.getRow(24 + 3 + i);
				}

				XSSFCell serialCell = materialRow.getCell(CellReference.convertColStringToIndex("A")) ;
				serialCell.setCellValue( i + 1);

				XSSFCell mNameCell = materialRow.getCell(CellReference.convertColStringToIndex("B")) ;
				mNameCell.setCellValue(materialTable.get("ID").toUpperCase());
				//mNameCell.setCellStyle(style);

				XSSFCell mIDCell = materialRow.getCell(CellReference.convertColStringToIndex("E")) ;
				mIDCell.setCellValue(materialTable.get("Name").toUpperCase());
				//mIDCell.setCellStyle(style);

				XSSFCell mTimeCell = materialRow.getCell(CellReference.convertColStringToIndex("J")) ;
				mTimeCell.setCellValue(materialTable.get("Quantity").toUpperCase());
				//mTimeCell.setCellStyle(style);

				XSSFCell mTorqueCell = materialRow.getCell(CellReference.convertColStringToIndex("K")) ;
				mTorqueCell.setCellValue(materialTable.get("Torque").toUpperCase());
				
			}

			//EQUIPMENT LIST
			for (int i = 0 ; i < equipmentVec.size() ; i ++){

				Hashtable<String, String> equipmentTable = equipmentVec.get(i) ;
				XSSFRow equipmentRow = null;
				if (i < 6)
				{
					equipmentRow = worksheet.getRow(24 + i);
				}
				else
				{
					equipmentRow = worksheet.getRow(24 + 3 + i);
				}
				String Name = equipmentTable.get("Name") ;
				String Bit = equipmentTable.get("Bit") ;
				String Socket = equipmentTable.get("Socket") ;
				String Extension = equipmentTable.get("Extension") ;

				String finalString = "" ;
				finalString = Name;
				if ( !Bit.equals("") )
				{
					finalString = finalString + " | B-" + Bit;
				}
				if ( !Socket.equals(""))
				{
					finalString = finalString + " | S- " + Socket ;
				}
				if ( !Extension.equals(""))
				{
					finalString = finalString + " | Ex- " + Extension ;
				}

				//finalString = Name + " | B-" + Bit + " | S-" + Socket  + " |Ex-" + Extension ;  

				XSSFCell eIDCell = equipmentRow.getCell(CellReference.convertColStringToIndex("O")) ;
				eIDCell.setCellValue(finalString.toUpperCase());

			}

			fileOut.close();
			FileOutputStream output_file = new FileOutputStream(updatefile) ;
			workbook.write(output_file);
			output_file.close();
			updatefile.setWritable(false);
			MessageBox.post("Updated Successfully.", "Success", MessageBox.INFORMATION);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			MessageBox.post("ERROR", "Please contact Admin", MessageBox.ERROR);
		}
	}

	public File updateJES(TCComponentItemRevision revObj){

		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("IMAN_specification");
			for(int i=0;i<dataset.length;i++){

				if(dataset[i].getType().equals("MSExcelX")){
					excel = (TCComponentDataset)dataset[i];
					break;
				}
			}
			if(excel!=null){
				TCComponent[] namedRef = excel.getNamedReferences();
				TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
				excelFile = file.getFmsFile();
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return excelFile;
	}
	
	private void updateRevNumber(XSSFWorkbook workbook, XSSFSheet worksheet, String revNum){
		if(!(isMergedCell(worksheet, 2, 3, CellReference.convertColStringToIndex("N"), CellReference.convertColStringToIndex("N")))){
			//set border first time
		    String setBorderRange = "$N$2:$N$4";
		    setBordersToMergedCells(worksheet, CellRangeAddress.valueOf(setBorderRange));
		    
			setMerge(worksheet, 2, 3, CellReference.convertColStringToIndex("N"), CellReference.convertColStringToIndex("N"));
			XSSFRow revRow = null;
			XSSFRow titleRow = null;
			XSSFCellStyle cellStyle = createCellStyle(workbook);
			if (worksheet.getRow(1) == null)
			{
				titleRow = worksheet.createRow(1) ;
			}
			else
			{
				titleRow = worksheet.getRow(1);
			}
			XSSFCell revTitleCell = null;
			revTitleCell = titleRow.getCell(CellReference.convertColStringToIndex("N")) ;
			revTitleCell.setCellValue("REV");
			revTitleCell.setCellStyle(cellStyle);
			if (worksheet.getRow(2) == null)
			{
				revRow = worksheet.createRow(2) ;
			}
			else
			{
				revRow = worksheet.getRow(2);
			}
			XSSFCell revValueCell = null;
			revValueCell = revRow.getCell(CellReference.convertColStringToIndex("N")) ;
			if (revValueCell == null)
			{
				revValueCell = revRow.createCell(CellReference.convertColStringToIndex("N")) ;
			}
			revValueCell.setCellValue(revNum);
			revValueCell.setCellStyle(cellStyle);
		}else{
			XSSFRow revRow = null;
			if (worksheet.getRow(2) == null)
			{
				revRow = worksheet.createRow(2) ;
			}
			else
			{
				revRow = worksheet.getRow(2);
			}
			XSSFCell revCell = null;
			revCell = revRow.getCell(CellReference.convertColStringToIndex("N")) ;
			if (revCell == null)
			{
				revCell = revRow.createCell(CellReference.convertColStringToIndex("N")) ;
			}
			revCell.setCellValue(revNum);
		}
	}
	private boolean isMergedCell(XSSFSheet worksheet, int numRow, int untilRow, int numCol, int untilCol){
		System.out.println("num merged reagons: "+ worksheet.getNumMergedRegions());
		   for(int i = 0; i < worksheet.getNumMergedRegions(); i++) {
		      CellRangeAddress merge = worksheet.getMergedRegion(i);
		      System.out.println("Number:" + i+ " " + merge.getFirstRow() + " " + merge.getLastRow()+  " " + merge.getFirstColumn() + " " + merge.getLastColumn());
		      if(numRow == merge.getFirstRow() && untilRow == merge.getLastRow() && numCol == merge.getFirstColumn() && untilCol == merge.getLastColumn()){
		    	  return true;
		      }
		   }
		return false;
	}
	
	protected void setMerge(XSSFSheet sheet, int numRow, int untilRow, int numCol, int untilCol) {
	    CellRangeAddress cellMerge = new CellRangeAddress(numRow, untilRow, numCol, untilCol);
	    sheet.addMergedRegion(cellMerge);
	}
	
	protected void setBordersToMergedCells(XSSFSheet sheet, CellRangeAddress rangeAddress) {
	    RegionUtil.setBorderTop(BorderStyle.MEDIUM, rangeAddress, sheet);
	    RegionUtil.setBorderLeft(BorderStyle.MEDIUM, rangeAddress, sheet);
	    RegionUtil.setBorderRight(BorderStyle.MEDIUM, rangeAddress, sheet);
	    RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
	}
	
	private XSSFCellStyle createCellStyle(XSSFWorkbook workbook){
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		
		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Calibri");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);
		
		//need set border twice time to make sure all borders were set
		cellStyle.setBorderTop(BorderStyle.MEDIUM);
		cellStyle.setBorderRight(BorderStyle.MEDIUM);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		cellStyle.setBorderLeft(BorderStyle.MEDIUM);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFont(font);
		return cellStyle;
	}
	
}
