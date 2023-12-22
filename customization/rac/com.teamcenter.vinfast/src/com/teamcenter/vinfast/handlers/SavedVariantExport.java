package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.GroupLayout.Alignment;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.MaterialFinalCodeInfo;

public class SavedVariantExport extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub

		String[] properties = new String[] {"cfg0ConfigPerspective","object_name","object_desc","cfg0VariantRuleText"};

		ISelection selection = HandlerUtil.getCurrentSelection( event );

		try {

			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

			if(selectedObjects.length != 0) {

				TCSession session = (TCSession) AIFUtility.getDefaultSession();

				DataManagementService dmCoreService = DataManagementService.getService(session);

				TCComponentSavedVariantRule[] savedVariantRule = new TCComponentSavedVariantRule[selectedObjects.length];

				for(int i =0; i<selectedObjects.length; i++) {

					savedVariantRule[i] =  (TCComponentSavedVariantRule) selectedObjects[i];
				}

				dmCoreService.getProperties(savedVariantRule, properties);

				ArrayList<MaterialFinalCodeInfo> allSelectedRulesMap = null;

				if(savedVariantRule.length != 0) {

					allSelectedRulesMap =  new ArrayList<MaterialFinalCodeInfo>();

					for(TCComponentSavedVariantRule variantRule : savedVariantRule) {

						MaterialFinalCodeInfo finalCodeMap = new MaterialFinalCodeInfo();

						String materialCode = variantRule.getPropertyDisplayableValue("object_name");

						finalCodeMap.setMaterialCode(materialCode);
						
						String name = variantRule.getPropertyDisplayableValue("object_desc");

						finalCodeMap.setName(name);

						TCComponent contextPerspective = variantRule.getRelatedComponent("cfg0ConfigPerspective") ;

						dmCoreService.getProperties(new TCComponent[] {contextPerspective}, new String[] {"cfg0ProductItems"});

						String context = contextPerspective.getPropertyDisplayableValue("cfg0ProductItems");

						context = context.substring(0, 9);

						if(context.contains("-")) {
							context = context.replace("-", "_");
						}

						finalCodeMap.setContext(context);

						String variantRuleText = variantRule.getPropertyDisplayableValue("cfg0VariantRuleText");
						String[] familyFeature = variantRuleText.split(" AND ");

						ArrayList<String> allFeatures  = null;

						if(familyFeature.length != 0) {

							allFeatures  = new ArrayList<String>();

							for(String ruleText : familyFeature) {

								allFeatures.add(ruleText);
							}
						}

						finalCodeMap.setFamilyFeature(allFeatures);

						allSelectedRulesMap.add(finalCodeMap);
					}

					SimpleDateFormat sf =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String date = sf.format(Calendar.getInstance().getTime());
					date = date.replace("-", "");
					date = date.replace(" ", "");
					date = date.replace(":", "");

					File outputPath = printToExcel(allSelectedRulesMap, System.getProperty("java.io.tmpdir"), "FinalMaterialCode_"+date);

					try {
						Desktop.getDesktop().open(outputPath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}

	public File printToExcel(ArrayList<MaterialFinalCodeInfo> allSelectedRulesMap, String filePath, String fileName){

		File outputFile = null;
		try {
			//Create blank workbook
			XSSFWorkbook workbook = new XSSFWorkbook();

			//Create a blank sheet
			XSSFSheet spreadsheet = workbook.createSheet(fileName);
			spreadsheet.setColumnWidth(0, 5000);
			spreadsheet.setColumnWidth(1, 10000);
			spreadsheet.setColumnWidth(2, 5000);
			spreadsheet.setColumnWidth(3, 5000);
			spreadsheet.setColumnWidth(4, 5000);
			//Create row object
			XSSFRow row;

			//This data needs to be written (Object[])
			CellStyle backgroundStyle = workbook.createCellStyle();

			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setBold(true);
			font.setFontName("Arial");

			backgroundStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			backgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			backgroundStyle.setAlignment(HorizontalAlignment.CENTER);
			backgroundStyle.setFont(font);

			Map < String, String[] > headerinfo = new TreeMap < String, String[] >();
			headerinfo.put( "1", new String[] {"MATERIAL CODE","NAME", "CONTEXT", "FAMILY CODE", "FEATURE CODE"});

			//Iterate over data and write to sheet
			Set < String > keyid = headerinfo.keySet();
			int rowid = 0;

			for (String key : keyid) {

				row = spreadsheet.createRow(rowid++);
				String [] objectArr = headerinfo.get(key);
				int cellid = 0;

				for (String obj : objectArr){
					Cell cell = row.createCell(cellid++);
					cell.setCellValue(new XSSFRichTextString(obj));
					cell.setCellStyle(backgroundStyle);
				}
			}
			//Header FINISH

			int BOMrowcount = 1;

			for (MaterialFinalCodeInfo finalCodeInfo : allSelectedRulesMap) {

				String materialCode = finalCodeInfo.getMaterialCode();
				String context = finalCodeInfo.getContext();
				String name = finalCodeInfo.getName();
				ArrayList<String> ruleText = finalCodeInfo.getFamilyFeature();

				int startRow = BOMrowcount;

				for (String text : ruleText){

					XSSFRow rowCell = null;
					if(startRow == BOMrowcount) {


						CellStyle cellStyle = workbook.createCellStyle();
						cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
						rowCell = spreadsheet.createRow(BOMrowcount++);
						Cell cell0 = rowCell.createCell(0);
						cell0.setCellStyle(cellStyle);
						cell0.setCellValue(materialCode);
						Cell cell1 = rowCell.createCell(1);
						cell1.setCellStyle(cellStyle);
						cell1.setCellValue(name);
						Cell cell2 = rowCell.createCell(2);
						cell2.setCellStyle(cellStyle);
						cell2.setCellValue(context);

					}else {

						rowCell = spreadsheet.createRow(BOMrowcount++);
						rowCell.createCell(0);
						rowCell.createCell(1);
						rowCell.createCell(2);
					}

					text = text.substring(text.indexOf("]")+1, text.length());
					String[] split = text.split("=");
					rowCell.createCell(3).setCellValue(split[0].trim());
					rowCell.createCell(4).setCellValue(split[1].trim());
				}

				int endRow = BOMrowcount-1;
				spreadsheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
				spreadsheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1, 1));
				spreadsheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 2, 2));
			}

			//Write the workbook in file system
			outputFile = new File(filePath+"//"+fileName+".xlsx");
			FileOutputStream out = new FileOutputStream(outputFile);
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

		return outputFile;
	}

}
