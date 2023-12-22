package com.vf.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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

	public static String getCellStringValue(Cell cell) {
		String cellVal = "";

		if (cell.getCellType() == CellType.FORMULA) {
			switch (cell.getCachedFormulaResultType()) {
			case BOOLEAN:
				boolean booleanVal = cell.getBooleanCellValue();
				cellVal = String.valueOf(booleanVal);
				break;
			case NUMERIC:
				double doubleVal = cell.getNumericCellValue();
				cellVal = String.valueOf(doubleVal);

				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date dateVal = cell.getDateCellValue();
					SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
					cellVal = df.format(dateVal);
				}

				break;
			case STRING:
				cellVal = cell.getRichStringCellValue().getString();
				break;
			default:
				break;
			}
		} else {
			cellVal = cell.toString();
		}

		return cellVal;
	}

	public static void setCell(int index, HSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		HSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	public static CellStyle getDefaultCellStyle(XSSFWorkbook workbook) {
		CellStyle defaultCellStyle = workbook.createCellStyle();
		defaultCellStyle.setAlignment(HorizontalAlignment.LEFT);
		defaultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		defaultCellStyle.setBorderTop(BorderStyle.THIN);
		defaultCellStyle.setBorderBottom(BorderStyle.THIN);
		defaultCellStyle.setBorderLeft(BorderStyle.THIN);
		defaultCellStyle.setBorderRight(BorderStyle.THIN);

		return defaultCellStyle;
	}
}
