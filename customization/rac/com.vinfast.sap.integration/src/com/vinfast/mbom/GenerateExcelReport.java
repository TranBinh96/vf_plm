package com.vinfast.mbom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.FileManagementUtility;

public class GenerateExcelReport {
	private FileManagementUtility fileUtility = null;
	private XSSFWorkbook workbook = null;
	private XSSFSheet spreadsheet;
	private int lineNumber = 0;
	
	public GenerateExcelReport() {
		// TODO Auto-generated constructor stub
		create();
	}
	
	private void create() {
		workbook = new XSSFWorkbook();
		spreadsheet = workbook.createSheet("Data");
		spreadsheet.createFreezePane(0, 1);
	}

	public void printHeaderData(String[] columnData) {
		
		XSSFFont headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());

		XSSFCellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.BIG_SPOTS);

		XSSFRow row = spreadsheet.createRow(lineNumber++);
		for(int col = 0; col<columnData.length; col++) {
			XSSFCell columnShop = row.createCell(col);
			columnShop.setCellValue(columnData[col]);
			columnShop.setCellStyle(headerCellStyle);
			//spreadsheet.autoSizeColumn(col);
		}
	}

	public void printRowData(String[] columnData) {

		XSSFRow row = spreadsheet.createRow(lineNumber++);
		for(int col = 0; col<columnData.length; col++) {
			XSSFCell columnShop = row.createCell(col);
			if(col==4) {
				if(columnData[col].equals("No Preview") || columnData[col].equals("")) {
					columnShop.setCellValue(columnData[col]);
				}else{
					row.setHeight((short)1500);
					insertImage(columnData[col], row.getRowNum(), col);
				}
			}else {
				columnShop.setCellValue(columnData[col]);
			}
		}
	}
	
	private void insertImage(String imagePath, int row, int col) {

		try {
			if(!imagePath.isEmpty()) {
				FileInputStream stream = new FileInputStream(getFileManagementUtility().getFile(imagePath) );
				XSSFDrawing drawing = spreadsheet.createDrawingPatriarch();
				XSSFCreationHelper helper = workbook.getCreationHelper();
				XSSFClientAnchor anchor = helper.createClientAnchor();
				anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
				int pictureIndex = workbook.addPicture(IOUtils.toByteArray(stream), XSSFWorkbook.PICTURE_TYPE_BMP);
				stream.close();
				spreadsheet.autoSizeColumn(col);
				anchor.setCol1( col );
				anchor.setCol2( col+1 );
				anchor.setRow1( row ); // same row is okay
				anchor.setRow2( row+1 );
				drawing.createPicture( anchor, pictureIndex );
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FileManagementUtility getFileManagementUtility() {
		TCSession session = (TCSession)AIFUtility.getDefaultSession();
		if(fileUtility == null) {
			fileUtility = new FileManagementUtility(session.getSoaConnection());
			return fileUtility;
		}else {
			return fileUtility;
		}
	}
	public void printToExcel(String filePathName) {
		//"C:/Temp/"+shopName+".xlsx"
		try {
			
			FileOutputStream out = new FileOutputStream(new File(filePathName));
			workbook.write(out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void autoFitColumnText(int[] columns) {
		for(int col = 0; col<columns.length; col++) {
			spreadsheet.autoSizeColumn(columns[col]);
		}
	}
}
