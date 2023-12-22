package com.teamcenter.vinfast.impactedprogram;

public class Constants {
	private static String font = "Calibri";
	private static int fontSize = 11;
	private static String msgBoxTitle = "Impacted Programs";
	private static String noImpactedProgram = "No Impacted Program";
	private static String tblHdrImpactedPrograms = "Impacted Vehicle Programs";
	private static String tblHdrSelectedParts = "Selected Components";
	private static String impactedProgramExcelFileName = "ImpactedPrograms";
	private static String sheetProgramView = "Program View";
	private static String sheetPartView = "Part View";
	private static String btnSave2Excel = "Save to Excel";
	private static String btnAddReport2ECR = "Add Report to ECR";
	
	
	public static String getFont() {
		return font;
	}
	public static void setFont(String font) {
		Constants.font = font;
	}
	public static int getFontSize() {
		return fontSize;
	}
	public static void setFontSize(int fontSize) {
		Constants.fontSize = fontSize;
	}
	public static String getImpactedProgramExcelFileName() {
		return impactedProgramExcelFileName;
	}
	public static void setImpactedProgramExcelFileName(String impactedProgramExcelFileName) {
		Constants.impactedProgramExcelFileName = impactedProgramExcelFileName;
	}
	public static String getNoImpactedProgram() {
		return noImpactedProgram;
	}
	public static void setNoImpactedProgram(String noImpactedProgram) {
		Constants.noImpactedProgram = noImpactedProgram;
	}
	public static String getTblHdrImpactedPrograms() {
		return tblHdrImpactedPrograms;
	}
	public static void setTblHdrImpactedPrograms(String tblHdrImpactedPrograms) {
		Constants.tblHdrImpactedPrograms = tblHdrImpactedPrograms;
	}
	public static String getTblHdrSelectedParts() {
		return tblHdrSelectedParts;
	}
	public static void setTblHdrSelectedParts(String tblHdrSelectedParts) {
		Constants.tblHdrSelectedParts = tblHdrSelectedParts;
	}
	public static String getSheetProgramView() {
		return sheetProgramView;
	}
	public static void setSheetProgramView(String sheetProgramView) {
		Constants.sheetProgramView = sheetProgramView;
	}
	public static String getSheetPartView() {
		return sheetPartView;
	}
	public static void setSheetPartView(String sheetPartView) {
		Constants.sheetPartView = sheetPartView;
	}
	public static String getMsgBoxTitle() {
		return msgBoxTitle;
	}
	public static void setMsgBoxTitle(String msgBoxTitle) {
		Constants.msgBoxTitle = msgBoxTitle;
	}
	public static String getBtnSave2Excel() {
		return btnSave2Excel;
	}
	public static void setBtnSave2Excel(String btnSave2Excel) {
		Constants.btnSave2Excel = btnSave2Excel;
	}
	public static String getBtnAddReport2ECR() {
		return btnAddReport2ECR;
	}
	public static void setBtnAddReport2ECR(String btnAddReport2ECR) {
		Constants.btnAddReport2ECR = btnAddReport2ECR;
	}
}
