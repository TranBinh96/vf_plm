package com.teamcenter.vinfast.report.mhu;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.dialog.MHUReportDialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;

public class MHUExport_Handler extends AbstractHandler {
	private int[] colorArrayR = {189, 252, 255, 180, 198};
	private int[] colorArrayG = {215, 228, 230, 198, 224};
	private int[] colorArrayB = {238, 214, 153, 231, 180};
	
	private static MHUReportDialog frame = null;
	private static TCSession session = null;
	private static long startTime = 0;

	private final String REPORT_PREFIX = "MHUBOM_";
	
	private String TopBom = "";

	private static String TEMP_DIR;
	
	LinkedHashMap<String, String> mapAvaPropPreference 		= new LinkedHashMap<String, String>();
	
	private List<String> selectedProps								= new ArrayList<String>();
	
	private static LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMasterBOMLineInfo;
	
	private static int sourcingNumberCol = 0;
	
	public MHUExport_Handler() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TEMP_DIR = System.getenv("tmp");
		
		mapAvaPropPreference.put("Item ID", "current_id");
		mapAvaPropPreference.put("Part Name", "current_name");
		mapAvaPropPreference.put("Revision", "current_revision_id");
		mapAvaPropPreference.put("Sourcing List", "");
		selectedProps.add("Item ID");
		selectedProps.add("Part Name");
		selectedProps.add("Revision");
		selectedProps.add("Sourcing List");
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
		
		if (targetComponents != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					mapMasterBOMLineInfo = new LinkedHashMap<Integer, LinkedHashMap<String,String>>();
					createDialog(targetComponents);
				}
			});
		} 
		else {
			MessageBox.post("Please Select One Bom Line.", "Error", MessageBox.ERROR);
		}

		return null;
	}
	
	public void createDialog(final InterfaceAIFComponent[] targetComponents) {
		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));
		
		frame = new MHUReportDialog();
		frame.setTitle("MHUBOM Report");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(500, 200));
		frame.label1.setText("<html>Export all EC Part Revision. This action will take few minutes.</html>");
		frame.label1.setHorizontalAlignment(SwingConstants.CENTER);
		frame.btnLeft.setIcon(ok_Icon);
		frame.btnLeft.setText("Continue");

		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");
		
//		frame.progressBar.setVisible(false);
		frame.rdbtnShowAllMPN.setVisible(false);
		frame.rdbtnShowRefMPN.setVisible(false);
		
		frame.btnLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Job job = new Job("Creating Report") {
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						IStatus status = new IStatus() {
							@Override
							public boolean matches(int arg0) {
								return false;
							}
							@Override
							public boolean isOK() {
								return false;
							}
							@Override
							public boolean isMultiStatus() {
								return false;
							}
							@Override
							public int getSeverity() {
								return 0;
							}
							@Override
							public String getPlugin() {
								return null;
							}
							@Override
							public String getMessage() {
								return null;
							}
							@Override
							public Throwable getException() {
								return null;
							}
							@Override
							public int getCode() {
								return 0;
							}
							@Override
							public IStatus[] getChildren() {
								return null;
							}
						};
						
						frame.progressBar.setIndeterminate(true);
						
						frame.btnLeft.setEnabled(false);
						startTime = System.currentTimeMillis();
						TEMP_DIR = System.getenv("tmp") + "/" + startTime;
						File theDir = new File(TEMP_DIR);
						theDir.mkdirs();
						
						GetAllECPart();
						
						excelPublishReport();

						return status;
					}
				};
				session.queueOperation(job);
			}
		});
		frame.btnRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void GetAllECPart() {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("ID", "*");
		TCComponent[] item_search = Query.queryItem(session, inputQuery, "EC Part Revision");

		if(item_search != null || item_search.length > 0){
			int i = 0;
			for(TCComponent oneLine : item_search) {
				collectData(i, (TCComponent) oneLine);
				i++;
			}
		}
	}
	
	private void collectData(int key, TCComponent bomline) {
		try {
			String check = "";
			String objectType = bomline.getType();
			if(objectType.compareToIgnoreCase("VF4_EC_PartRevision") != 0) {
				return;
			}
			
			bomline.getRelatedComponents();
			
			LinkedHashMap<String, String> BOMLineInfo = new LinkedHashMap<String, String>();
			String refeMPN = "";
			for (Map.Entry<String, String> entry: mapAvaPropPreference.entrySet()) {
				String value = "";
				if(!entry.getValue().isEmpty()) {
					value = bomline.getProperty(entry.getValue());
					if(entry.getKey().compareToIgnoreCase("Ref MPN") == 0) 
						refeMPN = value;
				}
				BOMLineInfo.put(entry.getKey(), value);
			}
			TCComponent[] objectChildComponents = bomline.getRelatedComponents("VF4_EC_Supplier_Relation");
			
			String sourcingInfo = "";
			int _sourcingNumber = 0;
			
			for (TCComponent tcComponent : objectChildComponents) {
				if(tcComponent.getType().compareToIgnoreCase("VF4_EC_Supp_PartRevision") == 0) {
					_sourcingNumber++;
					if(!sourcingInfo.isEmpty()) {
						sourcingInfo += ";";
					}
					sourcingInfo += tcComponent.getPropertyDisplayableValue("object_name") + ";" +
									tcComponent.getPropertyDisplayableValue("vf4_manufacturer") + ";" +
									tcComponent.getPropertyDisplayableValue("vf4_supplier_name");
				}
			}

			if(_sourcingNumber > sourcingNumberCol)
				sourcingNumberCol = _sourcingNumber;
			
			BOMLineInfo.put("Sourcing List", sourcingInfo);
			BOMLineInfo.put("Check", check);
			
			mapMasterBOMLineInfo.put(key, BOMLineInfo);
		} 
		catch (TCException e) {
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void excelPublishReport() {
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet spreadsheet = wb.createSheet("BOM");
			
			//Top Header
			XSSFCellStyle topHeaderCellStyle = wb.createCellStyle();
			topHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			topHeaderCellStyle.setBorderTop(BorderStyle.THIN);
			topHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
			topHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
			topHeaderCellStyle.setBorderRight(BorderStyle.THIN);
			topHeaderCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(146, 208, 80), new DefaultIndexedColorMap()));
		    
			XSSFRow topHeaderRow = spreadsheet.createRow(0);
			int cellCounter = 0;
			setCell(1, topHeaderRow, "BOM", topHeaderCellStyle);
			spreadsheet.addMergedRegion(new CellRangeAddress(0, 0, 1, selectedProps.size() - 2));
			for (int i = 0; i < sourcingNumberCol; i++) {
				XSSFCellStyle secondHeaderCellStyle = wb.createCellStyle();
				secondHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				secondHeaderCellStyle.setBorderTop(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderRight(BorderStyle.THIN);
				secondHeaderCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(colorArrayR[i % 5], colorArrayG[i % 5], colorArrayB[i % 5]), new DefaultIndexedColorMap()));
				
				setCell(selectedProps.size() - 1 + i * 3, topHeaderRow, "SOURCING " + (i + 1), secondHeaderCellStyle);
				spreadsheet.addMergedRegion(new CellRangeAddress(0, 0, selectedProps.size() - 1 + i * 3, selectedProps.size() - 1 + i * 3 + 2));
			}
			
			//Header
			CellStyle headerCellStyle = wb.createCellStyle();
			headerCellStyle.setBorderTop(BorderStyle.THIN);
			headerCellStyle.setBorderBottom(BorderStyle.THIN);
			headerCellStyle.setBorderLeft(BorderStyle.THIN);
			headerCellStyle.setBorderRight(BorderStyle.THIN);
			
			XSSFRow headerRow = spreadsheet.createRow(1);
			cellCounter = 0;
			for (String value : selectedProps) {
				if(value.compareToIgnoreCase("Sourcing List") == 0) {
					for (int i = 0; i < sourcingNumberCol; i++) {
						setCell(cellCounter, headerRow, "MPN", headerCellStyle); cellCounter++;
						setCell(cellCounter, headerRow, "Manufacturer", headerCellStyle); cellCounter++;
						setCell(cellCounter, headerRow, "Supplier Name", headerCellStyle); cellCounter++;
					}
				}
				else {
					setCell(cellCounter, headerRow, value, headerCellStyle); cellCounter++;
				}
			}
			
			//Body
			XSSFCellStyle cellStyleOrange = wb.createCellStyle();
			cellStyleOrange.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyleOrange.setBorderTop(BorderStyle.THIN);
			cellStyleOrange.setBorderBottom(BorderStyle.THIN);
			cellStyleOrange.setBorderLeft(BorderStyle.THIN);
			cellStyleOrange.setBorderRight(BorderStyle.THIN);
			cellStyleOrange.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), new DefaultIndexedColorMap()));
			
			CellStyle bodyCellStyle = wb.createCellStyle();
			bodyCellStyle.setBorderTop(BorderStyle.THIN);
			bodyCellStyle.setBorderBottom(BorderStyle.THIN);
			bodyCellStyle.setBorderLeft(BorderStyle.THIN);
			bodyCellStyle.setBorderRight(BorderStyle.THIN);
			
			int rowCounter = 2;
			for(Map.Entry entry : mapMasterBOMLineInfo.entrySet()) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> BOMLineInfo = (LinkedHashMap<String, String>)entry.getValue();
				
				cellCounter = 0;
				XSSFRow bodyRow = spreadsheet.createRow(rowCounter);
				for (String col : selectedProps) {
					String dispItm = BOMLineInfo.get(col);
					if(col.compareToIgnoreCase("Sourcing List") == 0) {
						if(dispItm == null) {
							for (int i = 0; i < sourcingNumberCol; i++) {
								setCell(cellCounter, bodyRow, "", bodyCellStyle); cellCounter++;
								setCell(cellCounter, bodyRow, "", bodyCellStyle); cellCounter++;
								setCell(cellCounter, bodyRow, "", bodyCellStyle); cellCounter++;
							}
						}
						else {
							String[] str = dispItm.split(";");
							String value = ""; 
							for (int i = 0; i < sourcingNumberCol; i++) {
								value = ""; try { value = str[0 + i * 3]; } catch (Exception e) { }
								setCell(cellCounter, bodyRow, value, bodyCellStyle); cellCounter++;
								value = ""; try { value = str[1 + i * 3]; } catch (Exception e) { }
								setCell(cellCounter, bodyRow, value, bodyCellStyle); cellCounter++;
								value = ""; try { value = str[2 + i * 3]; } catch (Exception e) { }
								setCell(cellCounter, bodyRow, value, bodyCellStyle); cellCounter++;
							}
						}
					}
					else {
						if(dispItm == null) {
							dispItm = "";
						}
						boolean errorCell = false;
						if(col.compareToIgnoreCase("Ref MPN") == 0) {
							if(BOMLineInfo.get("Check").compareToIgnoreCase("0") == 0) {
								errorCell = true;
							}
						}
						if(errorCell) setCell(cellCounter, bodyRow, dispItm, cellStyleOrange); 
						else setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
						cellCounter++;
					}
				}
				rowCounter++;
			}
			
			for (int kz = 0; kz < selectedProps.size() + sourcingNumberCol * 3; kz++) {
				spreadsheet.autoSizeColumn(kz);
			}
			
			String fileName = REPORT_PREFIX + TopBom + "_" + StringExtension.getTimeStamp() + ".xlsx";
			File file = new File(TEMP_DIR + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			frame.progressBar.setIndeterminate(false);
			Desktop.getDesktop().open(file);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setCell(int index, XSSFRow headerRow, String displayValue, CellStyle headerCellStyle){
		XSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}
	
	private String currentTime() {
		SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		return formatter.format(date);
	}
}


