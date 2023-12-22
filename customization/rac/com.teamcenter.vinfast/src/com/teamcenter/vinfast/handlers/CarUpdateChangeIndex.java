package com.teamcenter.vinfast.handlers;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.EScooterUpdateChangeIndexFrame;
import com.vf.utils.Query;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.*;

public class CarUpdateChangeIndex extends AbstractHandler{
	private final Logger LOGGER;
	private static EScooterUpdateChangeIndexFrame frame 		= null;
	private static TCSession session 							= null;
	private static TCComponentGroup group 						= null;
	private static TCComponentRole role 						= null;
	private static TCComponentUser user 						= null;
	private static String selectedChangeIndexValue 				= null;
	private static SXSSFWorkbook wb;
	private final String DATASET_TEMPLATE_NAME 	= "BOM_REPORT_TEMPLATE";
	private final String REPORT_PREFIX 			= "Change_Report_";
	private static String TEMP_DIR				= "";
	private static long startTime 				= 0;
	private static String targetProperty		= "VL5_change_index";
	private static String dummyProperty         = "VF3_location1";
	private static String LEVEL_HEADER 			= "Level";
	private static String PART_NUMBER_HEADER 	= "Part Number";
	private static String DONOR_VEHICLE_HEADER	= "Donor Vehicle";
	private static String OLD_VALUE_HEADER		= "Old Change Index";
	private static String NEW_VALUE_HEADER		= "New Change Index";
	private static String STATUS_HEADER			= "Status";
	private static String NOTE_HEADER			= "Note";
	private static List<String> listHeaders = new ArrayList<String>();
	
	private static LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMasterBOMLineInfo;
	public CarUpdateChangeIndex() {
		TEMP_DIR 			= System.getenv("tmp");
		this.LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		/*init header list*/
		listHeaders.add(LEVEL_HEADER);
		listHeaders.add(PART_NUMBER_HEADER);
		listHeaders.add(DONOR_VEHICLE_HEADER);
		listHeaders.add(OLD_VALUE_HEADER);
		listHeaders.add(NEW_VALUE_HEADER);
		listHeaders.add(STATUS_HEADER);
		listHeaders.add(NOTE_HEADER);
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					group = session.getCurrentGroup();
					role = session.getCurrentRole();
					user = session.getUser();
					mapMasterBOMLineInfo = new LinkedHashMap<Integer, LinkedHashMap<String, String>>();

					InterfaceAIFComponent[] bomLine = AIFUtility.getCurrentApplication().getTargetComponents();
					if (bomLine != null) {
						String[] changeIndexValues = Utils.getLovValues(session, targetProperty, (TCComponentBOMLine) bomLine[0]);
						createDialog(new ArrayList<>(Arrays.asList(changeIndexValues)), bomLine);
					} else {
						MessageBox.post("Please Select Bom Line.", "Error", MessageBox.ERROR);
					}

			}
		});
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public void createDialog(ArrayList<String> listValues, final InterfaceAIFComponent[] selectedBOMLines) {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new EScooterUpdateChangeIndexFrame();
		frame.setTitle("Update Change Index");
		frame.setIconImage(frame_Icon.getImage());
		
		for (String aProcess : listValues) {
			frame.comboBox1.addItem(aProcess);
		}
		/*default add blank value*/
		frame.comboBox1.addItem("");
		frame.comboBox1.setSelectedIndex(listValues.size());
		frame.btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedChangeIndexValue = frame.comboBox1.getSelectedItem().toString();
				if (selectedChangeIndexValue == null) {
					MessageBox.post("Please select a value.", "Error", MessageBox.ERROR);
					return;
				}
//				// TODO call function query data
				AbstractAIFOperation op = new AbstractAIFOperation() {
					
					@Override
					public void executeOperation() throws Exception {
						frame.btnOK.setEnabled(false);
						startTime = System.currentTimeMillis();
						int i = 0;
						for (InterfaceAIFComponent oneLine : selectedBOMLines) {
							executeUpdateChangeIndex(i, (TCComponentBOMLine) oneLine);
							i++;
						}
						frame.dispose();
						
						/*use timer to wait application refresh*/
						Timer timer = new Timer(2000, new ActionListener() {
						    @Override
						    public void actionPerformed(ActionEvent arg0) {            
						        MessageBox.post("The action has been completed. A report will be downloaded after you click the OK button. Please verify the updated values and Click Save button to commit the changes", "Information", MessageBox.INFORMATION);
						        publishReport();
						    }
						});
						timer.setRepeats(false);
						timer.start();
					}
				};
				session.queueOperation(op);
			}

		});
		
		frame.btnPopulateDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				// TODO call function query data
				AbstractAIFOperation op = new AbstractAIFOperation() {
					@Override
					public void executeOperation() throws Exception {
						startTime = System.currentTimeMillis();
						boolean isContinue = true;
						int i = 0;
						for (InterfaceAIFComponent oneLine : selectedBOMLines) {
							isContinue = executeUpdateDefaultValue(i, (TCComponentBOMLine) oneLine);
							if(!isContinue) {
								break;
							}
							i++;
						}
						frame.dispose();
						
						/*use timer to wait application refresh*/
						if (isContinue) {
							Timer timer = new Timer(2000, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									MessageBox.post(
											"The action has been completed. A report will be downloaded after you click the OK button. Please verify the updated values and Click Save button to commit the changes",
											"Information", MessageBox.INFORMATION);
									publishReport();
								}
							});
							timer.setRepeats(false);
							timer.start();
						}
					}
				};
				session.queueOperation(op);
			}
		});
		
		frame.btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void executeUpdateChangeIndex(int counting, TCComponentBOMLine bomline) {
		boolean result = true;
		LinkedHashMap<String, String> BOMLineInfo = new LinkedHashMap<String, String>();
		String oldChangeIndex 	= "";
		String bomlineLvl 		= "";
		String note 			= "";
		String partNumber		= "";
		try {
			String itemType = bomline.getProperty("bl_item_object_type");
			bomlineLvl = bomline.getProperty("bl_level_starting_0");
			partNumber = bomline.getProperty("bl_item_item_id");
			if(itemType.compareToIgnoreCase("VF Design") == 0 || itemType.compareToIgnoreCase("VF BP Design") == 0) {
				oldChangeIndex = bomline.getProperty(targetProperty);
				bomline.setStringProperty(targetProperty, selectedChangeIndexValue);
			}else {
				result = false;
				note = "Wrong data type";
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = false;
			note = e.toString();
			e.printStackTrace();
		}
		BOMLineInfo.put(LEVEL_HEADER, bomlineLvl);
		BOMLineInfo.put(PART_NUMBER_HEADER, partNumber);
		BOMLineInfo.put(OLD_VALUE_HEADER, oldChangeIndex);
		BOMLineInfo.put(NEW_VALUE_HEADER, selectedChangeIndexValue);
		if(result) {
			BOMLineInfo.put(STATUS_HEADER, "Successed");
		}else {
			BOMLineInfo.put(STATUS_HEADER, "Failed");
			BOMLineInfo.put(NOTE_HEADER	, note);
		}
		mapMasterBOMLineInfo.put(counting, BOMLineInfo);
	}
	
	private boolean executeUpdateDefaultValue(int counting, TCComponentBOMLine bomline) {
		boolean result = true;
		LinkedHashMap<String, String> BOMLineInfo = new LinkedHashMap<String, String>();
		String oldChangeIndex 	= "";
		String makeBuy			= "";
		String bomlineLvl 		= "";
		String note 			= "";
		String partNumber		= "";
		String newChangeIndex   = "";
		String donorVeh 		= "";
		String parentDonorVeh	= "";
		String parentChangeIndex = "";
		String partCategory		= "";
		String parentNumberID = "";
		try {
			TCComponentBOMLine parentLine = (TCComponentBOMLine)bomline.getReferenceProperty("bl_parent");
			if(parentLine == null) {
				MessageBox.post("Cannot input change index to root line", "Waring", MessageBox.WARNING);
				return false;
			}
			String itemType = bomline.getProperty("bl_item_object_type");
			bomlineLvl = bomline.getProperty("bl_level_starting_0");
			partNumber = bomline.getProperty("bl_item_item_id");
			makeBuy = bomline.getItemRevision().getProperty("vl5_make_buy");
			donorVeh = bomline.getItem().getProperty("vf4_donor_vehicle");
			partCategory = bomline.getItem().getProperty("vf4_part_category");
			parentChangeIndex = parentLine.getProperty(targetProperty);
			oldChangeIndex = bomline.getProperty(targetProperty);
			parentDonorVeh = parentLine.getItem().getProperty("vf4_donor_vehicle");
			parentNumberID = parentLine.getProperty("bl_item_item_id");
			
			BOMLineInfo.put(LEVEL_HEADER, bomlineLvl);
			BOMLineInfo.put(PART_NUMBER_HEADER, partNumber);
			BOMLineInfo.put(DONOR_VEHICLE_HEADER, donorVeh);
			BOMLineInfo.put(OLD_VALUE_HEADER, oldChangeIndex);
			
			if(itemType.compareToIgnoreCase("VF Design") != 0 && itemType.compareToIgnoreCase("VF BP Design") != 0) {
				String msg = "Invalid object type: " + itemType;
				BOMLineInfo.put(STATUS_HEADER, "Failed");
				BOMLineInfo.put(NOTE_HEADER	, msg);
				mapMasterBOMLineInfo.put(counting, BOMLineInfo);
//				MessageBox.post(msg, "Warning", MessageBox.WARNING);
				return true;
			}
			
			if(parentDonorVeh.isEmpty()) {
				String msg = "Please input Donor Vehicle for parent part: " + parentNumberID;
				BOMLineInfo.put(STATUS_HEADER, "Failed");
				BOMLineInfo.put(NOTE_HEADER	, msg);
				mapMasterBOMLineInfo.put(counting, BOMLineInfo);
//				MessageBox.post(msg, "Warning", MessageBox.WARNING);
				return true;
			}
			if(donorVeh.isEmpty()) {
				String msg = "Please input Donor Vehicle for: " + partNumber;
				BOMLineInfo.put(STATUS_HEADER, "Failed");
				BOMLineInfo.put(NOTE_HEADER	, msg);
				mapMasterBOMLineInfo.put(counting, BOMLineInfo);
//				MessageBox.post(msg, "Warning", MessageBox.WARNING);
				return true;
			}
			if(parentChangeIndex.isEmpty()) {
				String msg = "Please input Change Index for parent line: " + partNumber;
				BOMLineInfo.put(STATUS_HEADER, "Failed");
				BOMLineInfo.put(NOTE_HEADER	, msg);
				mapMasterBOMLineInfo.put(counting, BOMLineInfo);
//				MessageBox.post(msg, "Warning", MessageBox.WARNING);
				return true;
			}
			if(Integer.valueOf(bomlineLvl) <= 1) {
				String msg = "Cannot input Change Index for bom line level 0 or 1";
				BOMLineInfo.put(STATUS_HEADER, "Failed");
				BOMLineInfo.put(NOTE_HEADER	, msg);
				mapMasterBOMLineInfo.put(counting, BOMLineInfo);
//				MessageBox.post(msg, "Warning", MessageBox.WARNING);
				return true;
			}
			if(!partCategory.isEmpty() && partCategory.compareToIgnoreCase("NONE") != 0) {
				//Valid Part Category => Change Index  = Part Category
				newChangeIndex = partCategory;
				bomline.setStringProperty(targetProperty, newChangeIndex);
			}else {
				if(donorVeh.compareToIgnoreCase(parentDonorVeh) == 0 && parentChangeIndex.compareToIgnoreCase("COP") == 0) {
					newChangeIndex = "COP";
					bomline.setStringProperty(targetProperty, newChangeIndex);
				}else if(donorVeh.compareToIgnoreCase(parentDonorVeh) == 0 && parentChangeIndex.compareToIgnoreCase("COP") != 0) {
					newChangeIndex = "NEW";
					bomline.setStringProperty(targetProperty, newChangeIndex);
				}else if(donorVeh.compareToIgnoreCase(parentDonorVeh) != 0 && parentChangeIndex.compareToIgnoreCase("COP") == 0) {
					newChangeIndex = "NEW";
					bomline.setStringProperty(targetProperty, newChangeIndex);
				}else if(donorVeh.compareToIgnoreCase(parentDonorVeh) != 0 && parentChangeIndex.compareToIgnoreCase("COP") != 0) {
					newChangeIndex = "COP";
					bomline.setStringProperty(targetProperty, newChangeIndex);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = false;
			note = e.toString();
			e.printStackTrace();
		}
//		BOMLineInfo.put(LEVEL_HEADER, bomlineLvl);
//		BOMLineInfo.put(PART_NUMBER_HEADER, partNumber);
//		BOMLineInfo.put(OLD_VALUE_HEADER, oldChangeIndex);
//		BOMLineInfo.put(NEW_VALUE_HEADER, newChangeIndex);
		if(result) {
			BOMLineInfo.put(STATUS_HEADER, "Successed");
			BOMLineInfo.put(NEW_VALUE_HEADER, newChangeIndex);
		}else {
			BOMLineInfo.put(STATUS_HEADER, "Failed");
			BOMLineInfo.put(NOTE_HEADER	, note);
		}
		mapMasterBOMLineInfo.put(counting, BOMLineInfo);
		return true;
	}

	
	public String removeDupicateWord(String input, String token) {
		final String[] strWords = input.split(token);
		final Set<String> setOfWords = new LinkedHashSet<String>(Arrays.asList(strWords));
		final StringBuilder builder = new StringBuilder();
		int index = 0;

		for (String s : setOfWords) {

			if (index > 0)
				builder.append(token);

			builder.append(s);
			index++;
		}
		String output = builder.toString();
		return output;
	}
	
	private void publishReport() {
		try {
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DATASET_TEMPLATE_NAME, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			XSSFWorkbook template = new XSSFWorkbook(fileIn);
			CarUpdateChangeIndex.wb = new SXSSFWorkbook(template, 500);
			
			writeHeaderLine("Sheet1");
			writeARowReport();
			wb.getSheet("Sheet1").trackAllColumnsForAutoSizing();
			for (int kz = 0; kz < listHeaders.size(); kz++) {
				wb.getSheet("Sheet1").autoSizeColumn(kz);
			}
			Runtime.getRuntime().gc();
			
			FileOutputStream fos = new FileOutputStream(report);
			CarUpdateChangeIndex.wb.write(fos);
			fos.close();
			CarUpdateChangeIndex.wb.dispose();
			report.setWritable(false);
			Desktop.getDesktop().open(report);
		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void writeARowReport() {

		// TODO loop propmap and write file
		SXSSFSheet spreadsheet = wb.getSheet("Sheet1");
		SXSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		
		int rowCounter = 1;
		for(Map.Entry entry : mapMasterBOMLineInfo.entrySet()) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, String> BOMLineInfo = (LinkedHashMap<String, String>)entry.getValue();
			
			int cellCounter = 0;
			SXSSFRow row = spreadsheet.createRow(rowCounter);
			for (String col : listHeaders) {
				String dispItm = BOMLineInfo.get(col);
				SXSSFCell cell = row.createCell(cellCounter);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(dispItm == null ? "" : dispItm);
				
				cellCounter++;
			}
			rowCounter++;
		}
	}
	
	public void writeHeaderLine(String sheetName) {
		
		SXSSFSheet spreadsheet = wb.getSheet(sheetName);
		
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		headerCellStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		SXSSFRow headerRow = spreadsheet.createRow(0);

		int counter = 0;

		for (String temp : listHeaders) {
			getHeaderCell(counter, headerRow, temp, headerCellStyle);
			counter++;
		}
		spreadsheet.setAutoFilter(new CellRangeAddress(0, 0, 0, listHeaders.size() - 1));
		spreadsheet.createFreezePane(0, 1);
	}
	
	private SXSSFCell getHeaderCell(int id, SXSSFRow headerRow, String displayValue, CellStyle headerCellStyle)
	{
		 SXSSFCell cell = headerRow.createCell(id);
		 cell.setCellValue(displayValue);
		 cell.setCellStyle(headerCellStyle);
		 return cell;
	}
}
