package com.teamcenter.vinfast.car.engineering.specdoc;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;

public class DVPRDocumentTestReportExport_Handler extends AbstractHandler {
	private TCComponent selectedObject = null;

	public DVPRDocumentTestReportExport_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			String objectType = selectedObject.getPropertyDisplayableValue("object_type");
			if (objectType.compareTo("DVPR Document Revision") != 0) {
				MessageBox.post("Please select one DVPR Document Revision.", "Warning", MessageBox.WARNING);
				return null;
			}

			String[] headers = new String[] { "VFTE", "Test Name", "Progress", "Test Result", "Comment" };
			SXSSFWorkbook wb = new SXSSFWorkbook();
			SXSSFSheet spreadsheet = wb.createSheet("Data");

			int i = 0;
			int j = 0;
			SXSSFRow headerRow = spreadsheet.createRow(i++);
			for (String header : headers) {
				SXSSFCell cell1 = headerRow.createCell(j++);
				cell1.setCellValue(header);
			}

			TCComponent[] testReportRow = selectedObject.getReferenceListProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
			if (testReportRow != null) {
				for (TCComponent report : testReportRow) {
					SXSSFRow bodyRow = spreadsheet.createRow(i++);

					SXSSFCell cellBody = bodyRow.createCell(0);
					cellBody.setCellValue(report.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM));

					SXSSFCell cellBody1 = bodyRow.createCell(1);
					cellBody1.setCellValue(report.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME));

					SXSSFCell cellBody2 = bodyRow.createCell(2);
					cellBody2.setCellValue(report.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS));

					SXSSFCell cellBody3 = bodyRow.createCell(3);
					cellBody3.setCellValue(report.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT));

					SXSSFCell cellBody4 = bodyRow.createCell(4);
					cellBody4.setCellValue(report.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT));
				}
			}

			File file = new File(System.getenv("tmp") + selectedObject.getPropertyDisplayableValue("item_id") + "_" + "Test Report List_" + StringExtension.getTimeStamp() + ".xlsx");
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			Desktop.getDesktop().open(file);
			wb.close();
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}
}