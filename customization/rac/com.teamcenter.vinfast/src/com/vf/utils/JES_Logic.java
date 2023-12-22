package com.vf.utils;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

public class JES_Logic {
	public final int DETAIL_STEPS_ROW_INDEX = 15;
	public final String DETAIL_STEPS_COL_INDEX_NO = "K";
	public final String DETAIL_STEPS_COL_INDEX_SYMBOL_START = "L";
	public final String DETAIL_STEPS_COL_INDEX_SYMBOL_END = "M";
	public final String DETAIL_STEPS_COL_INDEX_DESC = "M";
	
	public final String PICTURE_COL_TOP_INDEX = "A";
	public final int PICTURE_ROW_TOP_INDEX = 15;
	public final String PICTURE_COL_BOTTOM_INDEX = "K";
	public final int PICTURE_ROW_BOTTOM_INDEX = 23;
	
	
	public final String OPERATION_PICTURE_RELATION = "TC_Attaches";
	public final String OPERATION_BACKUP_TOOL_PROPERTY_NAME = "vf4_user_notes";
	public final int JES_TEMPLATE_MAX_MATERIAL_VEC_SIZE = 43;
	public final int JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE;
	public final int JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW;
	public final int JES_TEMPLATE_PART_DATA_INDEX_HEADER_ROW;
	
	public final String DETAIL_STEP_PROP_NAME_DESC = "vf4_detail_step_desc";
	public final String DETAIL_STEP_PROP_NAME_DESC_EN = "vf4_step_desc_en";
	public final String DETAIL_STEP_REFERENCE = "vf4_step_reference";
	public final String DETAIL_PROP_NAME_STEP_SYMBOL = "vf4__detail_step_symbol";
	public final String DETAIL_PROP_NAME_STEP_NO = "object_name";
	public final String REPORT_PREFIX = "JES_Report_";

	public final String JES_DATASET_TEMPLATE = "JES_Template.xlsx";// TODO: save in TC preference

	public JES_Logic() {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession() ;
		TCPreferenceService preferenceService = session.getPreferenceService();
		
		
		
		Integer maxPartsInFirstPage = preferenceService.getIntegerValue("VINF_JES_MAX_PARTS_IN_FIRST_PAGE");
		Integer headerPartRowNumber = preferenceService.getIntegerValue("VINF_HEADER_PART_LIST_ROW_INDEX");
		JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE = maxPartsInFirstPage != null ? maxPartsInFirstPage.intValue() : 2;
		JES_TEMPLATE_PART_DATA_INDEX_HEADER_ROW = headerPartRowNumber != null ? headerPartRowNumber.intValue() : 23;
		JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW = JES_TEMPLATE_PART_DATA_INDEX_HEADER_ROW + 1;
	}
	
	public XSSFCellStyle createCellStyleForPartData(XSSFWorkbook workbook) {
		XSSFCellStyle border = workbook.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		//border.setAlignment(HorizontalAlignment.RIGHT);
		// Create Style font
		XSSFCellStyle cell = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		cell.cloneStyleFrom(border);
		cell.setAlignment(HorizontalAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.CENTER);
		//font.setBold(true);
		cell.setFont(font);
		return cell;
	}
	
	public XSSFCellStyle createCellStyleForPartDataHeader(XSSFWorkbook workbook) {
		XSSFCellStyle border = workbook.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		//border.setAlignment(HorizontalAlignment.RIGHT);
		// Create Style font
		XSSFCellStyle Header = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		Header.cloneStyleFrom(border);
		Header.setAlignment(HorizontalAlignment.LEFT);
		Header.setVerticalAlignment(VerticalAlignment.CENTER);
		font.setBold(true);
		Header.setFont(font);
		return Header;
	}
}
