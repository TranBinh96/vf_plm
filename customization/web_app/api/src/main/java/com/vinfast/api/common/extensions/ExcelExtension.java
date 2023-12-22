package com.vinfast.api.common.extensions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class ExcelExtension {
    public static void createHeader(int rowNum, String[] headerList, HSSFSheet sheet) {
        HSSFRow rowHeader = sheet.createRow(rowNum);
        int cellIndex = 0;
        for (String header: headerList) {
            HSSFCell cel = rowHeader.createCell(cellIndex++);
            cel.setCellValue(header);
        }
    }
}
