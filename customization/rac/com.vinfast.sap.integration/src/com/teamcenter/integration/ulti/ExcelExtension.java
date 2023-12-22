package com.teamcenter.integration.ulti;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExtension {
	public static void exportExcel(LinkedHashMap<String, String> dataList, String fileName) {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet spreadsheet = wb.createSheet("Data");

		int i = 0;
		for (Map.Entry<String, String> row : dataList.entrySet()) {
			XSSFRow bodyRow = spreadsheet.createRow(++i);

			XSSFCell cell1 = bodyRow.createCell(0);
			cell1.setCellValue(row.getKey());

			XSSFCell cell2 = bodyRow.createCell(1);
			cell2.setCellValue(row.getValue());
		}

		try {
			File file = new File(System.getenv("tmp") + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			wb.close();
			fos.close();
			Desktop.getDesktop().open(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static XSSFCell getCellByAddress(String address, XSSFSheet worksheet) {
		CellReference cr = new CellReference(address);
		XSSFRow row = worksheet.getRow(cr.getRow());
		XSSFCell cell = row.getCell(cr.getCol());
		return cell;
	}
}
