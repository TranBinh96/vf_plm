package com.teamcenter.vinfast.change;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Dialog;
import org.eclipse.swt.widgets.Shell;

public class ECRExportExcelFile extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject;
	private ALAssistant_Dialog dlg;
	private String nameObject ="";
	private ProgressMonitorDialog progressMonitorDialog;
	
	public ECRExportExcelFile() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dlg = new ALAssistant_Dialog(new Shell());
		dlg.create();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		selectedObject = (TCComponentItemRevision) targetComp[0];	
		prepareData();
		return null;
	}
	
	private void prepareData() {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
				 progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
					 @Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Download BOM Information...", IProgressMonitor.UNKNOWN);
							try {
								
								monitor.subTask("Data Export...");
								exportExcelTemplate();								
								
							}catch (Exception e) {
								e.printStackTrace();
							}							
							monitor.done();
							if (monitor.isCanceled())
								throw new InterruptedException("The long running export was cancelled");
					}				
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean exportExcelTemplate() throws NotLoadedException, IOException, TCException {
		// TODO Auto-generated method stub
		
		boolean status = false;
		nameObject = selectedObject.getProperty("item_id")+"_"+selectedObject.getProperty("item_revision_id")+" ";	
		File temp = File.createTempFile(nameObject, ".xlsx");
	    temp.deleteOnExit();
        String[] splits = temp.getAbsolutePath().split(" ");
        String fileSave = splits[0]+".xlsx";
        
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("ECR Summary");	
		sheet.setColumnWidth(0, 3855);
        sheet.setColumnWidth(1, 3555);
        sheet.setColumnWidth(2, 3855);
        sheet.setColumnWidth(3, 2855);
        sheet.setColumnWidth(4, 4855);
        sheet.setColumnWidth(5, 4055);
        sheet.setColumnWidth(6, 3855);
        sheet.setColumnWidth(7, 3855);
        sheet.setColumnWidth(8, 4855);
        sheet.setColumnWidth(9, 4155);
        sheet.setColumnWidth(10, 4155);
        sheet.setColumnWidth(11, 7681);
        sheet.setColumnWidth(12, 6648);
        sheet.setColumnWidth(13, 4155);
        sheet.setColumnWidth(14, 4155);
        sheet.setColumnWidth(15, 4155);
        sheet.setColumnWidth(16, 10066);
        sheet.setColumnWidth(17, 4235);
        sheet.setColumnWidth(18, 4235);
        sheet.setColumnWidth(19, 4235);
        sheet.setColumnWidth(20, 10221);
        sheet.setColumnWidth(21, 4735);
        sheet.setColumnWidth(22, 4235);
        sheet.setColumnWidth(23, 8888);
        sheet.setColumnWidth(24, 3855);
        sheet.setColumnWidth(25, 6855);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        XSSFCellStyle center = workbook.createCellStyle();
        /* Multiple Styles to Excel Cell POI */
        center.setFont(font);
        
        XSSFRow row = null;
        Cell cell = null;
        row =  sheet.createRow((short)0);
        row.setHeight((short)300);
        cell = row.createCell(0,CellType.STRING);
        cell.setCellValue("POSID");
        cell.setCellStyle(center);
        cell = row.createCell(1,CellType.STRING);
        cell.setCellValue("Donor Vehicle");
        cell.setCellStyle(center);
        cell = row.createCell(2,CellType.STRING);
        cell.setCellValue("Structure Level");
        cell.setCellStyle(center);
        cell = row.createCell(3,CellType.STRING);
        cell.setCellValue("Steering");
        cell.setCellStyle(center);
        cell = row.createCell(4,CellType.STRING);
        cell.setCellValue("Quantity");
        cell.setCellStyle(center);
        cell = row.createCell(5,CellType.STRING);
        cell.setCellValue("Maturity Level");
        cell.setCellStyle(center);
        cell = row.createCell(6,CellType.STRING);
        cell.setCellValue("Purchase Level");
        cell.setCellStyle(center);
        cell = row.createCell(7,CellType.STRING);
        cell.setCellValue("Change Type");
        cell.setCellStyle(center);
        cell = row.createCell(8,CellType.STRING);
        cell.setCellValue("Part Number");
        cell.setCellStyle(center);
        cell = row.createCell(9,CellType.STRING);
        cell.setCellValue("Old Version");
        cell.setCellStyle(center);
        cell = row.createCell(10,CellType.STRING);
        cell.setCellValue("Frozen Revision");
        cell.setCellStyle(center);
        cell = row.createCell(11,CellType.STRING);
        cell.setCellValue("Part Name");
        cell.setCellStyle(center);
        cell = row.createCell(12,CellType.STRING);
        cell.setCellValue("Original Base Part Number");
        cell.setCellStyle(center);
        cell = row.createCell(13,CellType.STRING);
        cell.setCellValue("Variant Formula");
        cell.setCellStyle(center);
        cell = row.createCell(14,CellType.STRING);
        cell.setCellValue("Torque Information");
        cell.setCellStyle(center);
        cell = row.createCell(15,CellType.STRING);
        cell.setCellValue("Weight (g)");
        cell.setCellStyle(center);
        cell = row.createCell(16,CellType.STRING);
        cell.setCellValue("Change Description");
        cell.setCellStyle(center);
        cell = row.createCell(17,CellType.STRING);
        cell.setCellValue("3D Data Affected");
        cell.setCellStyle(center);
        cell = row.createCell(18,CellType.STRING);
        cell.setCellValue("Material");
        cell.setCellStyle(center);
        cell = row.createCell(19,CellType.STRING);
        cell.setCellValue("Specbook");
        cell.setCellStyle(center);
        cell = row.createCell(20,CellType.STRING);
        cell.setCellValue("Supplier");
        cell.setCellStyle(center);
        cell = row.createCell(21,CellType.STRING);
        cell.setCellValue("Supplier Contact");
        cell.setCellStyle(center);
        cell = row.createCell(22,CellType.STRING);
        cell.setCellValue("Is Aftersale Relevaant");
        cell.setCellStyle(center);
        cell = row.createCell(23,CellType.STRING);
        cell.setCellValue("Exchangeability");
        cell.setCellStyle(center);
        cell = row.createCell(24,CellType.STRING);
        cell.setCellValue("Part Tracebility");
        cell.setCellStyle(center);
        cell = row.createCell(25,CellType.STRING);
        cell.setCellValue("COP Impacted Program");
        cell.setCellStyle(center);
		 
		TCComponent[] forms;
		try {
			forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
			if (forms.length > 0) {
				TCComponent[] bomInfo = forms[0].getRelatedComponents("vf6_bom_information");
				if (bomInfo.length > 0) {
					int i = 0;
					for (TCComponent bomline : bomInfo) {
						row = sheet.createRow((short)1+i);
						row.setHeight((short)300);
						row.createCell(0).setCellValue(bomline.getPropertyDisplayableValue("vf6_posid"));	
						row.createCell(1).setCellValue(bomline.getPropertyDisplayableValue("vf6_donor_vehicle"));
						row.createCell(2).setCellValue(bomline.getPropertyDisplayableValue("vf6_structure_level"));
						row.createCell(3).setCellValue(bomline.getPropertyDisplayableValue("vf6_steering"));
						row.createCell(4).setCellValue(bomline.getPropertyDisplayableValue("vf6_quantity2"));
						row.createCell(5).setCellValue(bomline.getPropertyDisplayableValue("vf6_maturity_level"));
						row.createCell(6).setCellValue(bomline.getPropertyDisplayableValue("vf6_purchase_level"));
						row.createCell(7).setCellValue(bomline.getPropertyDisplayableValue("vf6_change_type"));
						row.createCell(8).setCellValue(bomline.getPropertyDisplayableValue("vf6_part_number"));
						row.createCell(9).setCellValue(bomline.getPropertyDisplayableValue("vf6_old_version"));	
						row.createCell(10).setCellValue(bomline.getPropertyDisplayableValue("vf6_frozen_revision"));	
						row.createCell(11).setCellValue(bomline.getPropertyDisplayableValue("vf6_part_name"));	
						row.createCell(12).setCellValue(bomline.getPropertyDisplayableValue("vf6_original_base_part"));	
						row.createCell(13).setCellValue(bomline.getPropertyDisplayableValue("vf6_variant_formula"));	
						row.createCell(14).setCellValue(bomline.getPropertyDisplayableValue("vf6_torque_information"));	
						row.createCell(15).setCellValue(bomline.getPropertyDisplayableValue("vf6_weight"));	
						row.createCell(16).setCellValue(bomline.getPropertyDisplayableValue("vf6_change_description"));	
						row.createCell(17).setCellValue(bomline.getPropertyDisplayableValue("vf6_3d_data_affected"));	
						row.createCell(18).setCellValue(bomline.getPropertyDisplayableValue("vf6_material"));	
						row.createCell(19).setCellValue(bomline.getPropertyDisplayableValue("vf6_specbook"));	
						row.createCell(20).setCellValue(bomline.getPropertyDisplayableValue("vf6_supplier"));	
						row.createCell(21).setCellValue(bomline.getPropertyDisplayableValue("vf6_supplier_contact"));	
						row.createCell(22).setCellValue(bomline.getPropertyDisplayableValue("vf6_is_aftersale_relevaant"));	
						row.createCell(23).setCellValue(bomline.getPropertyDisplayableValue("vf6_exchangeability"));	
						row.createCell(24).setCellValue(bomline.getPropertyDisplayableValue("vf6_part_tracebility"));	
						row.createCell(25).setCellValue(bomline.getPropertyDisplayableValue("vf6_cop_impacted_program"));	
						i++;
					}					
					
				}
				File excelFile = new File(fileSave);
				java.io.FileOutputStream out  = new java.io.FileOutputStream(excelFile);
				workbook.write(out);
				out.close();
				status = true;
				Desktop.getDesktop().open(excelFile);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return status;
	}
}
