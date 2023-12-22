package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
//import com.teamcenter.services.rac.structuremanagement.VariantManagementService;
//import com.teamcenter.services.rac.structuremanagement._2013_05.VariantManagement;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.BOMReport_CADModel;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.BOMValidatingFrame;
import com.vf.utils.Query;
import com.vf4.services.rac.custom.ReportDataSourceService;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSInput;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSOutput;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSResponse;

public class BOMReport extends AbstractHandler {
	private static Logger LOGGER;
	private static ArrayList<String> listBOMLineID;
//	private static BOMReportFrame frame = null;
	private static BOMValidatingFrame frame = null;
	private static TCSession session = null;
	private static long startTime = 0;

	private final String TOKEN_STRING = ";";
	private final String VEHICLE_USAGE = "_VehicleUsage";
	private final String PIECE_COST_FORM_TYPE = "Actual Piece Cost Form";
	private final String TARGET_COST_FORM_TYPE = "Target Cost Form";
	private final String FINAL = "Final quote ( PR level cost walk)";
	private final String INTERIM = "Interim quote ( Initial Sourcing or Change Form)";
	private final String BUYER = "Buyer Estimate ( No quote)";
	private final String ENGINEER = "Cost Engineering Estimate ( New added part/ no quote)";

	private final String EXTRA_INFO_MATURITY_LEVEL = "Maturity Level";
	private final String EXTRA_INFO_COSTING = "Costing";
	private final String EXTRA_INFO_VEHICLE_USAGE = "Vehicle Usage";
	private final String EXTRA_INFO_PLANT_INFO = "Plant Information";
	private final String EXTRA_INFO_TGSS_DOC = "TGSS Document Number";
	private final String EXTRA_INFO_IMDS = "IMDS";
	private final String EXTRA_INFO_PFEP = "PFEP";
	private final String EXTRA_INFO_CAD = "CAD";
	private final String EXTRA_SAVED_VARIANTS = "Save Variants";
	private final String EXTRA_HAVE_DRAWING = "Have Drawing";

	private final String BOM_REPORT_SHEET = "Sheet1";
	private final String DATASET_TEMPLATE_NAME = "BOM_REPORT_TEMPLATE";
	private final String REPORT_PREFIX = "BOM_Report_";
	private final String[] VINFAST_BOM_EXPORT_PROPERTY;
	private final String[] VINFAST_BOM_EXPORT_EXTRA_INFO;
	private final String[] VINFAST_BOM_VALIDATION_RULE;
	private final String[] VINFAST_COST_BOM_IGNORE_STATUS_VALUE;

	private final String[] IMDS_PROPS = new String[] { "Level", "BOM Line", "Purchase Level Vinfast", "Supplier Code", "Supplier Name" };

	private static String TEMP_DIR;
	private static SXSSFWorkbook wb;
	LinkedHashMap<String, String> mapAvaPropPreference = new LinkedHashMap<String, String>();
	LinkedHashMap<Integer, String> mapExtraInfoPreference = new LinkedHashMap<Integer, String>();
	LinkedHashMap<String, String> mapRulePreference = new LinkedHashMap<String, String>();
	LinkedHashMap<String, List<String>> mapRuleContidion = new LinkedHashMap<String, List<String>>();
	HashSet<String> availableSavedVariants;
	// this ava use for IMDS report
	private static LinkedList<String> itemIDLvl_1;

	List<String> selectedProps;
	List<String> selectedExtraInfo;
	List<String> selectedRules;

	private static StringBuilder listVehicleUsageNames;
	private static StringBuilder strBOMLineID;

	private static LinkedHashMap<String, PartMaturityLevelObject> mapMaturityLevelData;
	private static LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMasterBOMLineInfo;
	private static LinkedHashMap<String, CostObject> mapCostData;
	private static LinkedHashMap<String, PlantInformationObject> mapPlantInformationData;
	private static LinkedHashMap<String, PFEPObject> mapPFEPData;
	private static LinkedHashMap<String, BOMReport_CADModel> mapCADData;
	private static LinkedHashMap<TCComponentBOMLine, HashSet<String>> mapBomVariants;

	public BOMReport() {
		TEMP_DIR = System.getenv("tmp");
		LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		VINFAST_BOM_EXPORT_PROPERTY = preferenceService.getStringValues("VINFAST_BOM_VIEW_CONFIG");
		VINFAST_BOM_EXPORT_EXTRA_INFO = preferenceService.getStringValues("VINFAST_BOM_EXPORT_EXTRA_INFO");
		VINFAST_BOM_VALIDATION_RULE = preferenceService.getStringValues("VINFAST_BOM_VALIDATION_RULE");
		VINFAST_COST_BOM_IGNORE_STATUS_VALUE = preferenceService.getStringValues("VINFAST_COST_BOM_IGNORE_STATUS");
		List<String> listValueBOMExportPropPre = Arrays.asList(VINFAST_BOM_EXPORT_PROPERTY);
		List<String> listValueBOMExportExtraInfoPre = Arrays.asList(VINFAST_BOM_EXPORT_EXTRA_INFO);
		List<String> listValueBOMValidateRulePre = Arrays.asList(VINFAST_BOM_VALIDATION_RULE);
		for (String singleValue : listValueBOMExportPropPre) {
			String[] strSplit = singleValue.split(",");
			if (strSplit.length != 3) {
				continue;
			}
			mapAvaPropPreference.put(strSplit[1], strSplit[2]);
		}

		for (String singleValue : listValueBOMExportExtraInfoPre) {
			String[] strSplit = singleValue.split(",");
			if (strSplit.length != 2) {
				continue;
			}
			mapExtraInfoPreference.put(Integer.valueOf(strSplit[0]), strSplit[1]);
		}

		for (String singleValue : listValueBOMValidateRulePre) {
			String[] strSplit = singleValue.split(",");
			if (strSplit.length != 2) {
				continue;
			}
			mapRulePreference.put(strSplit[0], strSplit[1]);
		}
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				/* init variable */
				mapMaturityLevelData = new LinkedHashMap<String, PartMaturityLevelObject>();
				listBOMLineID = new ArrayList<String>();
				selectedProps = new ArrayList<String>();
				selectedExtraInfo = new ArrayList<String>();
				selectedRules = new ArrayList<String>();
				mapMasterBOMLineInfo = new LinkedHashMap<Integer, LinkedHashMap<String, String>>();
				mapCostData = new LinkedHashMap<String, CostObject>();
				mapPlantInformationData = new LinkedHashMap<String, PlantInformationObject>();
				mapPFEPData = new LinkedHashMap<String, PFEPObject>();
				mapCADData = new LinkedHashMap<String, BOMReport_CADModel>();
				listVehicleUsageNames = new StringBuilder();
				strBOMLineID = new StringBuilder();
				itemIDLvl_1 = new LinkedList<>();
				mapBomVariants = new LinkedHashMap<TCComponentBOMLine, HashSet<String>>();
				availableSavedVariants = new HashSet<String>();

				AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
				session = (TCSession) app.getSession();
				InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

				if (targetComponents != null) {
					createDialog(targetComponents);
				} else {
					MessageBox.post("Please Select One Bom Line.", "Error", MessageBox.ERROR);
				}

			}
		});
		return null;
	}

	public void createDialog(final InterfaceAIFComponent[] targetComponents) {
		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

//		frame = new BOMReportFrame(mapAvaPropPreference, mapExtraInfoPreference);
		frame = new BOMValidatingFrame(mapAvaPropPreference, mapExtraInfoPreference, mapRulePreference);

		// handle event when select IMDS extra info
		frame.getTableExtraInfo().addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = frame.getTableExtraInfo().rowAtPoint(e.getPoint());

				if (frame.getTableExtraInfo().getValueAt(row, 0).toString().compareToIgnoreCase(EXTRA_INFO_IMDS) == 0 && frame.getTableExtraInfo().getValueAt(row, 1).toString().compareToIgnoreCase("true") == 0) {

					/* IMDS only available for line level 0 or 1 */
					if (!isTopLine((TCComponentBOMLine) targetComponents[0])) {
						frame.dispose();
						MessageBox.post("Please select bomline has level 0 or 1 to export IMDS report.", "", MessageBox.WARNING);
						return;
					}

					TableModel moAvaProps = frame.getTableAvaProp().getModel();
					for (int i = 0; i < moAvaProps.getRowCount(); i++) {
						if (Arrays.asList(IMDS_PROPS).contains(moAvaProps.getValueAt(i, 0))) {
							moAvaProps.setValueAt(true, i, 1);
						}
					}

					TableModel moExtraInfo = frame.getTableExtraInfo().getModel();
					for (int i = 0; i < moExtraInfo.getRowCount(); i++) {
						if (moExtraInfo.getValueAt(i, 0).toString().compareToIgnoreCase(EXTRA_INFO_MATURITY_LEVEL) == 0) {
							moExtraInfo.setValueAt(true, i, 1);
						}
					}
				}
			}
		});

		frame.setIconImage(frame_Icon.getImage());
		frame.getBtnOK().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						// TODO Auto-generated method stub
						frame.getBtnOK().setEnabled(false);
						startTime = System.currentTimeMillis();
						/* Get selected property */
						for (int i = 0; i < frame.getTableAvaProp().getRowCount(); i++) {
							boolean state = (boolean) frame.getTableAvaProp().getValueAt(i, 1);
							if (state) {
								selectedProps.add((String) frame.getTableAvaProp().getValueAt(i, 0));
							}
						}
						/* Get selected extra information */
						for (int i = 0; i < frame.getTableExtraInfo().getRowCount(); i++) {
							boolean state = (boolean) frame.getTableExtraInfo().getValueAt(i, 1);
							if (state) {
								selectedExtraInfo.add((String) frame.getTableExtraInfo().getValueAt(i, 0));
							}
						}

						/* Get selected rule validation */
						for (int i = 0; i < frame.getTableRuleVaidate().getRowCount(); i++) {
							boolean state = (boolean) frame.getTableRuleVaidate().getValueAt(i, 1);
							if (state) {
								selectedRules.add((String) frame.getTableRuleVaidate().getValueAt(i, 0));
								/* get detail rule condition */
								String[] ruleConditions = session.getPreferenceService().getStringValues((String) frame.getTableRuleVaidate().getValueAt(i, 0));
								List<String> condition = Arrays.asList(ruleConditions);
								mapRuleContidion.put((String) frame.getTableRuleVaidate().getValueAt(i, 0), condition);
							}
						}

						/* Return if nothing selected */
						if (selectedProps.isEmpty() && selectedExtraInfo.isEmpty()) {
							MessageBox.post("Please Select Property/Extra Information.", "Error", MessageBox.ERROR);
							return;
						}

						if (targetComponents.length > 1) {
							/* generate report for particular BOMLine which user selected */
							int i = 0;
							for (InterfaceAIFComponent oneLine : targetComponents) {
								collectData(i, (TCComponentBOMLine) oneLine);
								i++;
							}
						} else if (targetComponents.length == 1) {
							/* generate report for whole assembly with root line is selected line */

							LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
							// Extract Saved variants if needed
							if (selectedExtraInfo.contains(EXTRA_SAVED_VARIANTS)) {
								loadSavedVariants((TCComponentBOMLine) targetComponents[0], allBOMLineInWindow);
							} else {
								expandBOMLines(allBOMLineInWindow, (TCComponentBOMLine) targetComponents[0]);
							}

							int i = 0;
							for (TCComponentBOMLine oneLine : allBOMLineInWindow) {
								collectData(i, (TCComponentBOMLine) oneLine);
								i++;
							}
						}

						/* Get costing information */
						if (selectedExtraInfo.contains(EXTRA_INFO_COSTING)) {
							extractTargetCostandEngiEstimatedCost(listBOMLineID);
						}
						/* Get vehicle usage information */
						if (selectedExtraInfo.contains(EXTRA_INFO_VEHICLE_USAGE)) {
							extractVehicleUsage(listBOMLineID);
						}
						/* Get PFEP */
						if (selectedExtraInfo.contains(EXTRA_INFO_PFEP)) {
							extractPFEP();
						}
						// Get CAD
//						if(selectedExtraInfo.contains(EXTRA_INFO_CAD)) {
//							extractCAD();
//						}

						makeFinalDatatoGenereateReport();

						if (selectedRules.size() > 0) {
							validateBOMLine(mapMasterBOMLineInfo, mapRuleContidion);
						}
						excelPublishReport();

						frame.dispose();
						return;
					}
				};
				session.queueOperation(op);
			}
		});
		frame.getBtnCancel().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	@SuppressWarnings("rawtypes")
	private void excelWriteARowReport() {

		// TODO loop propmap and write file
		SXSSFSheet spreadsheet = wb.getSheet(BOM_REPORT_SHEET);
		SXSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);

		String pattern = "0.000";
		CellStyle styleDecimal = wb.createCellStyle(); // Font and alignment
		styleDecimal.setDataFormat(wb.createDataFormat().getFormat(pattern));
		styleDecimal.setAlignment(HorizontalAlignment.LEFT);
		styleDecimal.setVerticalAlignment(VerticalAlignment.CENTER);
		styleDecimal.setBorderTop(BorderStyle.THIN);
		styleDecimal.setBorderBottom(BorderStyle.THIN);
		styleDecimal.setBorderLeft(BorderStyle.THIN);
		styleDecimal.setBorderRight(BorderStyle.THIN);

		/* in case export report IMDS */
		if (selectedExtraInfo.contains(EXTRA_INFO_IMDS)) {
			int rowCounter = 0;
			int counterLvl1 = 0;
			int currCellCounter = 0;
			for (Map.Entry<Integer, LinkedHashMap<String, String>> entry : mapMasterBOMLineInfo.entrySet()) {
				LinkedHashMap<String, String> BOMLineInfo = (LinkedHashMap<String, String>) entry.getValue();
				/* skip root line */
				if (Integer.valueOf(BOMLineInfo.get("Level")) == 0) {
					continue;
				}

				if (Integer.valueOf(BOMLineInfo.get("Level")) == 1) {
					counterLvl1++;
					rowCounter = 1;
					if (counterLvl1 == 1) {
						currCellCounter = 1;
					} else {
						currCellCounter = (selectedProps.size() * (counterLvl1 - 1)) + counterLvl1;
					}
				}

				if (counterLvl1 == 1) {
					SXSSFRow row = spreadsheet.createRow(rowCounter);
					int cellCounter = currCellCounter;
					for (String col : selectedProps) {
						String dispItm = BOMLineInfo.get(col);
						SXSSFCell cell = row.createCell(cellCounter);
						cell.setCellStyle(cellStyle);
						cell.setCellValue(dispItm == null ? "" : dispItm);
						cellCounter++;
					}
					rowCounter++;
				} else {
					SXSSFRow row = spreadsheet.getRow(rowCounter);
					int cellCounter = currCellCounter;
					for (String col : selectedProps) {
						String dispItm = BOMLineInfo.get(col);
						SXSSFCell cell2Update = row.getCell(cellCounter);
						if (cell2Update == null)
							cell2Update = row.createCell(cellCounter);

						if (!isNumber(dispItm)) {
							cell2Update.setCellStyle(cellStyle);
							cell2Update.setCellValue(dispItm == null ? "" : dispItm);
						} else {
							cell2Update.setCellStyle(styleDecimal);
							cell2Update.setCellValue(Double.valueOf(dispItm));
							cell2Update.setCellType(CellType.NUMERIC);
						}
						cellCounter++;
					}
					rowCounter++;
				}
			}
		} else {
			int rowCounter = 1;
			for (Map.Entry entry : mapMasterBOMLineInfo.entrySet()) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> BOMLineInfo = (LinkedHashMap<String, String>) entry.getValue();

				int cellCounter = 0;
				SXSSFRow row = spreadsheet.createRow(rowCounter);
				for (String col : selectedProps) {
					String dispItm = BOMLineInfo.get(col);
					SXSSFCell cell = row.createCell(cellCounter);
					if (!isNumber(dispItm)) {
						cell.setCellStyle(cellStyle);
						cell.setCellValue(dispItm == null ? "" : dispItm);
					} else {
						if (col.compareToIgnoreCase("Revision ID") == 0 || col.compareToIgnoreCase("Item ID") == 0) {
							cell.setCellStyle(cellStyle);
							cell.setCellValue(dispItm == null ? "" : dispItm);
						} else {
							cell.setCellStyle(styleDecimal);
							cell.setCellValue(Double.valueOf(dispItm));
							cell.setCellType(CellType.NUMERIC);
						}
					}
					cellCounter++;
				}
				rowCounter++;
			}
		}
	}

	private void excelPublishReport() {
		try {
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DATASET_TEMPLATE_NAME, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			XSSFWorkbook template = new XSSFWorkbook(fileIn);
			BOMReport.wb = new SXSSFWorkbook(template, 500);

			excelWriteHeaderLine(BOM_REPORT_SHEET);
			excelWriteARowReport();

			Runtime.getRuntime().gc();

			FileOutputStream fos = new FileOutputStream(report);
			BOMReport.wb.write(fos);
			fos.close();
			BOMReport.wb.dispose();
			report.setWritable(false);
			Desktop.getDesktop().open(report);
		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void collectData(int key, TCComponentBOMLine bomline) throws TCException {
		// TODO Auto-generated method stub
		try {
			/* count total line level 1 */
			if (bomline.getIntProperty("bl_level_starting_0") == 1) {
				itemIDLvl_1.add(bomline.getStringProperty("bl_item_item_id"));
			}
			/* default add Item ID to report */
			if (!selectedProps.contains("Item ID")) {
				selectedProps.add("Item ID");
			}
			String bomlineID = bomline.getProperty("bl_item_item_id");
			String posID = bomline.getProperty("VL5_pos_id");

			if (!listBOMLineID.contains(bomlineID)) {

				if (listVehicleUsageNames.length() > 0) {
					listVehicleUsageNames.append(TOKEN_STRING);
				}
				listVehicleUsageNames.append(bomlineID).append(VEHICLE_USAGE);

				if (strBOMLineID.length() > 0) {
					strBOMLineID.append(TOKEN_STRING);
				}
				strBOMLineID.append(bomlineID);

				/* init map cost data for each part */
				CostObject costNew = new CostObject();
				costNew.setPartNum(bomlineID);
				mapCostData.put(bomlineID, costNew);

			}

			LinkedHashMap<String, String> BOMLineInfo = new LinkedHashMap<String, String>();
			for (String selectedProp : selectedProps) {
				String realNameProp = mapAvaPropPreference.get(selectedProp);
				String value = bomline.getProperty(realNameProp);

				if (selectedProp.compareToIgnoreCase("X Vector") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						if (splitStr[8].startsWith("-")) {
							BOMLineInfo.put(selectedProp, splitStr[8].substring(0, 0) + splitStr[8].substring(1));
						} else {
							if (splitStr[8].compareToIgnoreCase("0") != 0) {
								StringBuilder sb = new StringBuilder(splitStr[8]);
								sb.insert(0, "-");
								BOMLineInfo.put(selectedProp, sb.toString());
							} else {
								BOMLineInfo.put(selectedProp, splitStr[8]);
							}
						}
					}
				} else if (selectedProp.compareToIgnoreCase("Y Vector") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						if (splitStr[9].startsWith("-")) {
							BOMLineInfo.put(selectedProp, splitStr[9].substring(0, 0) + splitStr[9].substring(1));
						} else {
							if (splitStr[9].compareToIgnoreCase("0") != 0) {
								StringBuilder sb = new StringBuilder(splitStr[9]);
								sb.insert(0, "-");
								BOMLineInfo.put(selectedProp, sb.toString());
							} else {
								BOMLineInfo.put(selectedProp, splitStr[9]);
							}
						}
					}
				} else if (selectedProp.compareToIgnoreCase("Z Vector") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						if (splitStr[10].startsWith("-")) {
							BOMLineInfo.put(selectedProp, splitStr[10].substring(0, 0) + splitStr[10].substring(1));
						} else {
							if (splitStr[10].compareToIgnoreCase("0") != 0) {
								StringBuilder sb = new StringBuilder(splitStr[10]);
								sb.insert(0, "-");
								BOMLineInfo.put(selectedProp, sb.toString());
							} else {
								BOMLineInfo.put(selectedProp, splitStr[10]);
							}
						}
					}
				} else if (selectedProp.compareToIgnoreCase("X Component") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						double valueD = Double.valueOf(splitStr[12]);
						BOMLineInfo.put(selectedProp, String.valueOf(valueD * 1000));
					}
				} else if (selectedProp.compareToIgnoreCase("Y Component") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						double valueD = Double.valueOf(splitStr[13]);
						BOMLineInfo.put(selectedProp, String.valueOf(valueD * 1000));
					}
				} else if (selectedProp.compareToIgnoreCase("Z Component") == 0) {
					if (!value.isEmpty()) {
						String[] splitStr = value.split(" ");
						double valueD = Double.valueOf(splitStr[14]);
						BOMLineInfo.put(selectedProp, String.valueOf(valueD * 1000));
					}
				} else {
					BOMLineInfo.put(selectedProp, value);
				}

			}
			/* Extract maturity level information */
			if (selectedExtraInfo.contains(EXTRA_INFO_MATURITY_LEVEL)) {
				extractPartMaturityLevel(bomline);
				BOMLineInfo.put("Release Statuses", mapMaturityLevelData.get(bomlineID).getReleaseStatues());
				BOMLineInfo.put("P ERN Number", mapMaturityLevelData.get(bomlineID).getP_ERNNumber());
				BOMLineInfo.put("P ERN Release Date", mapMaturityLevelData.get(bomlineID).getP_ERNReleaseDate());
				BOMLineInfo.put("I ERN Number", mapMaturityLevelData.get(bomlineID).getI_ERNNumber());
				BOMLineInfo.put("I ERN Release Date", mapMaturityLevelData.get(bomlineID).getI_ERNReleaseDate());
				BOMLineInfo.put("PR ERN Number", mapMaturityLevelData.get(bomlineID).getPR_ERNNumber());
				BOMLineInfo.put("PR ERN Release Date", mapMaturityLevelData.get(bomlineID).getPR_ERNReleaseDate());
				BOMLineInfo.put("PCR ECR Number", mapMaturityLevelData.get(bomlineID).getPCR_ECRNumber());
				BOMLineInfo.put("PCR ECR Release Date", mapMaturityLevelData.get(bomlineID).getPCR_ECRReleaseDate());
				BOMLineInfo.put("PCR ECN Number", mapMaturityLevelData.get(bomlineID).getPCR_ECNNumber());
				BOMLineInfo.put("PCR ECN Release Date", mapMaturityLevelData.get(bomlineID).getPCR_ECNReleaseDate());
				BOMLineInfo.put("ICR ECR Number", mapMaturityLevelData.get(bomlineID).getICR_ECRNumber());
				BOMLineInfo.put("ICR ECR Release Date", mapMaturityLevelData.get(bomlineID).getICR_ECRReleaseDate());
				BOMLineInfo.put("ICR ECN Number", mapMaturityLevelData.get(bomlineID).getICR_ECNNumber());
				BOMLineInfo.put("ICR ECN Release Date", mapMaturityLevelData.get(bomlineID).getICR_ECNReleaseDate());
				BOMLineInfo.put("PPR ECR Number", mapMaturityLevelData.get(bomlineID).getPPR_ECRNumber());
				BOMLineInfo.put("PPR ECR Release Date", mapMaturityLevelData.get(bomlineID).getPPR_ECRReleaseDate());
				BOMLineInfo.put("PPR ECN Number", mapMaturityLevelData.get(bomlineID).getPPR_ECNNumber());
				BOMLineInfo.put("PPR ECN Release Date", mapMaturityLevelData.get(bomlineID).getPPR_ECNReleaseDate());
				BOMLineInfo.put("MCN Number", mapMaturityLevelData.get(bomlineID).getMCNNumber());
				BOMLineInfo.put("MCN Release Date", mapMaturityLevelData.get(bomlineID).getMCNReleaseDate());
				BOMLineInfo.put("In Process", mapMaturityLevelData.get(bomlineID).getInProcess());
			}

			/* Extract plant information */
			if (selectedExtraInfo.contains(EXTRA_INFO_PLANT_INFO)) {
				extractManufPlantInformation(bomline);
				BOMLineInfo.put("Plant Code", mapPlantInformationData.get(bomlineID.concat(posID).trim()).getPlantCode());
				BOMLineInfo.put("Department", mapPlantInformationData.get(bomlineID.concat(posID).trim()).getDepartment());
				BOMLineInfo.put("Make/Buy", mapPlantInformationData.get(bomlineID.concat(posID).trim()).getMakeOrBuy());
			}
			/* Extract TGSS document number */
			if (selectedExtraInfo.contains(EXTRA_INFO_TGSS_DOC)) {
				BOMLineInfo.put("TGSS Document Number", getTGSSDocNumber(bomline));
			}
			/* Extract PFEP */
//			if(selectedExtraInfo.contains(EXTRA_INFO_PFEP)) {
//				extractPFEP();
//				if(mapPFEPData.get(bomlineID) != null) {
//					BOMLineInfo.put("P Release Plan Date", Vf4_P_release_plan_date());
//					BOMLineInfo.put("P Release Date Required?", Vf4_isNotRequired_P());
//					BOMLineInfo.put("P Release Comment", Vf4_why_notRequired_P());
//					BOMLineInfo.put("I Release Plan Date", Vf4_I_release_plan_date());
//					BOMLineInfo.put("I Release Date Required?", Vf4_isNotRequired_I());
//					BOMLineInfo.put("I Release Comment", Vf4_why_notRequired_I());
//					BOMLineInfo.put("PR Release Plan Date", Vf4_PR_release_plan_date());
//					BOMLineInfo.put("PR Release Date Required?", Vf4_isNotRequired_PR());
//					BOMLineInfo.put("PR Release Comment", Vf4_why_notRequired_PR());
//					BOMLineInfo.put("Serial Tool", Vf4_serial_tool());
//					BOMLineInfo.put("Serial Tool Planned Implementation", Vf4_serial_tool_plan());
//					BOMLineInfo.put("Serial Tooling Comment", Vf4_serialToolPlan_comment());
//					BOMLineInfo.put("Supplier Serial Tool", Vf4_supplier_serial_tool());
//					BOMLineInfo.put("Prototype Tooling", Vf4_prototype_tooling());
//					BOMLineInfo.put("Prototype Tooling Planned Implementation", Vf4_prototype_tool_plan());
//					BOMLineInfo.put("Prototype Tooling Comment", Vf4_protoToolPlan_comment());
//					BOMLineInfo.put("Supplier Prototype", Vf4_supplier_prototype());
//					BOMLineInfo.put("Soft Tooling", Vf4_soft_tooling());
//					BOMLineInfo.put("Soft Tooling Planned Implementation", Vf4_soft_tool_plan());
//					BOMLineInfo.put("Soft Tooling Comment", Vf4_softToolPlan_comment());
//					BOMLineInfo.put("Supplier Soft Tool", Vf4_supplier_soft_tool());	
//				}
//			}
			// Extract CAD
			if (selectedExtraInfo.contains(EXTRA_INFO_CAD)) {
				BOMReport_CADModel cadItem = extractCAD(bomline);
				if (cadItem != null) {
					BOMLineInfo.put("JT Last Save Date", cadItem.getJT_Last_Save_Date());
					BOMLineInfo.put("CATPart Last Save Date", cadItem.getCADPart_Last_Save_Date());
				} else {
					BOMLineInfo.put("JT Last Save Date", "");
					BOMLineInfo.put("CATPart Last Save Date", "");
				}
			}
			// Extract Have Drawing
			if (selectedExtraInfo.contains(EXTRA_HAVE_DRAWING)) {
				boolean have = extractHaveDrawing(bomline);
				BOMLineInfo.put(EXTRA_HAVE_DRAWING, have ? "v" : "");
			}

			if (selectedExtraInfo.contains(EXTRA_SAVED_VARIANTS)) {
				HashSet<String> variants = mapBomVariants.get(bomline);
				for (String variant : variants) {
					BOMLineInfo.put(variant, "x");
				}
			}

			mapMasterBOMLineInfo.put(key, BOMLineInfo);
			listBOMLineID.add(bomlineID);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Objective The Desciption of the method to explain what the method does
	 * @param the parameters used by the method
	 * @return the value returned by the method
	 * @throws what kind of exception does this method throw
	 */
	private void extractPartMaturityLevel(TCComponentBOMLine bomline) throws TCException {
		String bomlineID = bomline.getProperty("bl_item_item_id");
		/* ignore part which has run through */
		if (listBOMLineID.contains(bomlineID)) {
			return;
		}
		/* get part information */
		String name = bomline.getProperty("bl_item_object_name");
		String makeOrBuy = bomline.getProperty("bl_item_vf4_item_make_buy");
		String ERNNumber = "";
		String ERNReleaseDate = "";
		StringBuilder PCR_ECRNumber = new StringBuilder("");
		StringBuilder PCR_ECRReleaseDate = new StringBuilder("");
		StringBuilder PCR_ECNNumber = new StringBuilder("");
		StringBuilder PCR_ECNReleaseDate = new StringBuilder("");

		StringBuilder ICR_ECRNumber = new StringBuilder("");
		StringBuilder ICR_ECRReleaseDate = new StringBuilder("");
		StringBuilder ICR_ECNNumber = new StringBuilder("");
		StringBuilder ICR_ECNReleaseDate = new StringBuilder("");

		StringBuilder PPR_ECRNumber = new StringBuilder("");
		StringBuilder PPR_ECRReleaseDate = new StringBuilder("");
		StringBuilder PPR_ECNNumber = new StringBuilder("");
		StringBuilder PPR_ECNReleaseDate = new StringBuilder("");

		StringBuilder MCNNumber = new StringBuilder("");
		StringBuilder MCNReleaseDate = new StringBuilder("");

		StringBuilder itemRelStatuses = new StringBuilder("");
		/* create master data which is input for generate report function */
		PartMaturityLevelObject dataReport = new PartMaturityLevelObject();
		dataReport.setPartNumber(bomlineID);
		dataReport.setPartName(name);
		dataReport.setMakeOrBuy(makeOrBuy);
		mapMaturityLevelData.put(bomlineID, dataReport);

		/*
		 * loop all item revision to find latest revision has status P, I, PR
		 */
		TCComponentItem item = bomline.getItem();
		TCComponent[] itemRevs = item.getRelatedComponents("revision_list");
		String inProcessNumber = findAllInProcess(itemRevs);
		for (int i = 0; i < itemRevs.length; ++i) {
			boolean hasPStatus = false;
			boolean hasIStatus = false;
			boolean hasPRStatus = false;
			boolean hasPCRStatus = false;
			boolean hasICRStatus = false;
			boolean hasPPRStatus = false;
			String releaseStatus = itemRevs[i].getProperty("release_status_list");
			String revNumber = itemRevs[i].getProperty("item_revision_id");
			List<String> splitReleaseStatus = Arrays.asList(releaseStatus.split(","));
			if (!splitReleaseStatus.contains("P") && !splitReleaseStatus.contains("I") && !splitReleaseStatus.contains("PR") && !splitReleaseStatus.contains("PCR") && !splitReleaseStatus.contains("ICR") && !splitReleaseStatus.contains("PPR")) {
				continue;
			} else {
				if (splitReleaseStatus.contains("PCR")) {
					hasPCRStatus = true;
				}
				if (splitReleaseStatus.contains("ICR")) {
					hasICRStatus = true;
				}
				if (splitReleaseStatus.contains("PPR")) {
					hasPPRStatus = true;
				}
				if (splitReleaseStatus.contains("P")) {
					hasPStatus = true;
				}
				if (splitReleaseStatus.contains("I")) {
					hasIStatus = true;
				}
				if (splitReleaseStatus.contains("PR")) {
					hasPRStatus = true;
				}

				TCComponent[] ernRevision = Utils.GetPrimaryObjectByRelationName(session, itemRevs[i], "VF4_ERNRevision", "EC_solution_item_rel");
				TCComponent[] ecnRevision = Utils.GetPrimaryObjectByRelationName(session, itemRevs[i], "Vf6_ECNRevision", "CMHasSolutionItem");
				TCComponent[] mcnRevision = Utils.GetPrimaryObjectByRelationName(session, itemRevs[i], "Vf6_MCNRevision", "EC_solution_item_rel");

				for (int j = 0; j < ernRevision.length; j++) {
					if (hasPStatus) {
						ERNNumber = ernRevision[j].getProperty("item_id");
						ERNReleaseDate = ernRevision[j].getProperty("date_released");
						if (Utils.compareDate(mapMaturityLevelData.get(bomlineID).getP_ERNReleaseDate(), ERNReleaseDate) >= 0) {
							mapMaturityLevelData.get(bomlineID).setP_ERNNumber(ERNNumber);
							mapMaturityLevelData.get(bomlineID).setP_ERNReleaseDate(ERNReleaseDate);
						}
					} else if (hasIStatus) {
						ERNNumber = ernRevision[j].getProperty("item_id");
						ERNReleaseDate = ernRevision[j].getProperty("date_released");
						if (Utils.compareDate(mapMaturityLevelData.get(bomlineID).getI_ERNReleaseDate(), ERNReleaseDate) >= 0) {
							mapMaturityLevelData.get(bomlineID).setI_ERNNumber(ERNNumber);
							mapMaturityLevelData.get(bomlineID).setI_ERNReleaseDate(ERNReleaseDate);
						}
					} else if (hasPRStatus) {
						ERNNumber = ernRevision[j].getProperty("item_id");
						ERNReleaseDate = ernRevision[j].getProperty("date_released");
						if (Utils.compareDate(mapMaturityLevelData.get(bomlineID).getPR_ERNReleaseDate(), ERNReleaseDate) >= 0) {
							mapMaturityLevelData.get(bomlineID).setPR_ERNNumber(ERNNumber);
							mapMaturityLevelData.get(bomlineID).setPR_ERNReleaseDate(ERNReleaseDate);
						}
					}
				}

				for (int j = 0; j < ecnRevision.length; j++) {
					String ecnStatus = ecnRevision[j].getProperty("release_status_list");
					if (!ecnStatus.isEmpty() && ecnStatus.compareToIgnoreCase("Approved") == 0) {
						TCComponent[] solutionItemRev = ecnRevision[j].getRelatedComponents("CMHasSolutionItem");
						for (int y = 0; y < solutionItemRev.length; ++y) {
							if (solutionItemRev[y].getProperty("item_id").compareToIgnoreCase(bomlineID) == 0 && solutionItemRev[y].getProperty("item_revision_id").compareToIgnoreCase(revNumber) == 0) {

								/* append all ECN - ECR */
								if ((hasPStatus == true || hasPCRStatus == true) && !PCR_ECNNumber.toString().contains(ecnRevision[j].getProperty("item_id"))) {
									if (PCR_ECNNumber.length() > 0)
										PCR_ECNNumber.append(", ");
									PCR_ECNNumber.append(ecnRevision[j].getProperty("item_id"));
									if (PCR_ECNReleaseDate.length() > 0)
										PCR_ECNReleaseDate.append(", ");
									PCR_ECNReleaseDate.append(ecnRevision[j].getProperty("date_released"));

									TCComponent ecrRev = ecnRevision[j].getRelatedComponent("CMImplements");
									if (PCR_ECRNumber.length() > 0)
										PCR_ECRNumber.append(", ");
									PCR_ECRNumber.append(ecrRev.getProperty("item_id"));
									if (PCR_ECRReleaseDate.length() > 0)
										PCR_ECRReleaseDate.append(", ");
									PCR_ECRReleaseDate.append(ecrRev.getProperty("date_released"));
								}

								if ((hasIStatus == true || hasICRStatus == true) && !ICR_ECNNumber.toString().contains(ecnRevision[j].getProperty("item_id"))) {
									if (ICR_ECNNumber.length() > 0)
										ICR_ECNNumber.append(", ");
									ICR_ECNNumber.append(ecnRevision[j].getProperty("item_id"));
									if (ICR_ECNReleaseDate.length() > 0)
										ICR_ECNReleaseDate.append(", ");
									ICR_ECNReleaseDate.append(ecnRevision[j].getProperty("date_released"));

									TCComponent ecrRev = ecnRevision[j].getRelatedComponent("CMImplements");
									if (ICR_ECRNumber.length() > 0)
										ICR_ECRNumber.append(", ");
									ICR_ECRNumber.append(ecrRev.getProperty("item_id"));
									if (ICR_ECRReleaseDate.length() > 0)
										ICR_ECRReleaseDate.append(", ");
									ICR_ECRReleaseDate.append(ecrRev.getProperty("date_released"));
								}
								if (hasPPRStatus == true && !PPR_ECNNumber.toString().contains(ecnRevision[j].getProperty("item_id"))) {
									if (PPR_ECNNumber.length() > 0)
										PPR_ECNNumber.append(", ");
									PPR_ECNNumber.append(ecnRevision[j].getProperty("item_id"));
									if (PPR_ECNReleaseDate.length() > 0)
										PPR_ECNReleaseDate.append(", ");
									PPR_ECNReleaseDate.append(ecnRevision[j].getProperty("date_released"));

									TCComponent ecrRev = ecnRevision[j].getRelatedComponent("CMImplements");
									if (PPR_ECRNumber.length() > 0)
										PPR_ECRNumber.append(", ");
									PPR_ECRNumber.append(ecrRev.getProperty("item_id"));
									if (PPR_ECRReleaseDate.length() > 0)
										PPR_ECRReleaseDate.append(", ");
									PPR_ECRReleaseDate.append(ecrRev.getProperty("date_released"));
								}

							}
						}
					}
				}
				for (int j = 0; j < mcnRevision.length; j++) {
					String mcnStatus = mcnRevision[j].getProperty("release_status_list");
					if (mcnStatus.compareToIgnoreCase("Approved") == 0 || mcnStatus.compareToIgnoreCase("PPR") == 0) {
						TCComponent[] solutionItemRev = mcnRevision[j].getRelatedComponents("EC_solution_item_rel");
						for (int y = 0; y < solutionItemRev.length; ++y) {
							if (solutionItemRev[y].getProperty("item_id").compareToIgnoreCase(bomlineID) == 0 && solutionItemRev[y].getProperty("item_revision_id").compareToIgnoreCase(revNumber) == 0) {
								/* append all ECN - ECR */
								if (MCNNumber.length() > 0)
									MCNNumber.append(", ");
								MCNNumber.append(mcnRevision[j].getProperty("item_id"));
								if (MCNReleaseDate.length() > 0)
									MCNReleaseDate.append(", ");
								MCNReleaseDate.append(mcnRevision[j].getProperty("date_released"));
							}
						}
					}
				}
			}
			/* add value for release statuses */
			if (hasPStatus && !itemRelStatuses.toString().contains("P")) {
				itemRelStatuses.append("P");
			}
			if (hasIStatus && !itemRelStatuses.toString().contains("I")) {
				if (itemRelStatuses.length() > 0)
					itemRelStatuses.append(", ");
				itemRelStatuses.append("I");
			}
			if (hasPRStatus && !itemRelStatuses.toString().contains("PR")) {
				if (itemRelStatuses.length() > 0)
					itemRelStatuses.append(", ");
				itemRelStatuses.append("PR");
			}
			if (hasPCRStatus && !itemRelStatuses.toString().contains("PCR")) {
				if (itemRelStatuses.length() > 0)
					itemRelStatuses.append(", ");
				itemRelStatuses.append("PCR");
			}
			if (hasICRStatus && !itemRelStatuses.toString().contains("ICR")) {
				if (itemRelStatuses.length() > 0)
					itemRelStatuses.append(", ");
				itemRelStatuses.append("ICR");
			}
			if (hasPPRStatus && !itemRelStatuses.toString().contains("PPR")) {
				if (itemRelStatuses.length() > 0)
					itemRelStatuses.append(", ");
				itemRelStatuses.append("PPR");
			}
		}

		mapMaturityLevelData.get(bomlineID).setReleaseStatues(itemRelStatuses.toString());
		mapMaturityLevelData.get(bomlineID).setPCR_ECNNumber(PCR_ECNNumber.toString());
		mapMaturityLevelData.get(bomlineID).setPCR_ECNReleaseDate(PCR_ECNReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setPCR_ECRNumber(PCR_ECRNumber.toString());
		mapMaturityLevelData.get(bomlineID).setPCR_ECRReleaseDate(PCR_ECRReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setICR_ECNNumber(ICR_ECNNumber.toString());
		mapMaturityLevelData.get(bomlineID).setICR_ECNReleaseDate(ICR_ECNReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setICR_ECRNumber(ICR_ECRNumber.toString());
		mapMaturityLevelData.get(bomlineID).setICR_ECRReleaseDate(ICR_ECRReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setPPR_ECNNumber(PPR_ECNNumber.toString());
		mapMaturityLevelData.get(bomlineID).setPPR_ECNReleaseDate(PPR_ECNReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setPPR_ECRNumber(PPR_ECRNumber.toString());
		mapMaturityLevelData.get(bomlineID).setPPR_ECRReleaseDate(PPR_ECRReleaseDate.toString());

		mapMaturityLevelData.get(bomlineID).setMCNNumber(MCNNumber.toString());
		mapMaturityLevelData.get(bomlineID).setMCNReleaseDate(MCNReleaseDate.toString());
		mapMaturityLevelData.get(bomlineID).setInProcess(inProcessNumber);
		if (bomlineID.contains("_JF")) {
			mapMaturityLevelData.get(bomlineID).setP_ERNNumber("N/A");
			mapMaturityLevelData.get(bomlineID).setP_ERNReleaseDate("N/A");
		}
	}

	private void excelWriteHeaderLine(String sheetName) {
		if (selectedExtraInfo.contains(EXTRA_INFO_MATURITY_LEVEL)) {
			if (selectedExtraInfo.contains(EXTRA_INFO_IMDS)) {
				selectedProps.add("Release Statuses");
			} else {
				selectedProps.add("Release Statuses");
				selectedProps.add("P ERN Number");
				selectedProps.add("P ERN Release Date");
				selectedProps.add("I ERN Number");
				selectedProps.add("I ERN Release Date");
				selectedProps.add("PR ERN Number");
				selectedProps.add("PR ERN Release Date");
				selectedProps.add("PCR ECR Number");
				selectedProps.add("PCR ECR Release Date");
				selectedProps.add("PCR ECN Number");
				selectedProps.add("PCR ECN Release Date");
				selectedProps.add("ICR ECR Number");
				selectedProps.add("ICR ECR Release Date");
				selectedProps.add("ICR ECN Number");
				selectedProps.add("ICR ECN Release Date");
				selectedProps.add("PPR ECR Number");
				selectedProps.add("PPR ECR Release Date");
				selectedProps.add("PPR ECN Number");
				selectedProps.add("PPR ECN Release Date");
				selectedProps.add("MCN Number");
				selectedProps.add("MCN Release Date");
				selectedProps.add("In Process");
			}
		}
		if (selectedExtraInfo.contains(EXTRA_INFO_COSTING)) {
			/* Target cost */
			selectedProps.add("EDD Cost Target");
			selectedProps.add("Tooling Cost Target");
			selectedProps.add("Piece Cost Target");
			/* Actual cost */
			selectedProps.add("Actual Piece Cost Interim Quote");
			selectedProps.add("Actual Currency Interim Quote");
			selectedProps.add("Supplier Packing Amount Interim Quote");
			selectedProps.add("Suplier Logistic Cost Interim Quote");
			selectedProps.add("Total Cost Interim Quote");

			selectedProps.add("Actual Piece Cost Final Quote");
			selectedProps.add("Actual Currency Final Quote");
			selectedProps.add("Supplier Packing Final Quote");
			selectedProps.add("Suplier Logistic Final Quote");
			selectedProps.add("Total Cost Final Quote");

			selectedProps.add("Actual Piece Cost Buyer Estimate");
			selectedProps.add("Actual Currency Buyer Estimate");
			selectedProps.add("Supplier Packing Buyer Estimate");
			selectedProps.add("Suplier Logistic Buyer Estimate");
			selectedProps.add("Total Cost Buyer Estimate");

			selectedProps.add("Actual Piece Cost Cost Engineer Estimation");
			selectedProps.add("Actual Currency Cost Engineer Estimation");
			selectedProps.add("Supplier Packing Cost Engineer Estimation");
			selectedProps.add("Suplier Logistic Cost Engineer Estimation");
			selectedProps.add("Total Cost Cost Engineer Estimation");

			/* Tooling and ED&D cost */
			selectedProps.add("ED&D Cost Value");
			selectedProps.add("ED&D Cost currency");
			selectedProps.add("Proto Tooling Value");
			selectedProps.add("Proto Tooling Currency");
			selectedProps.add("Proto Piece Price");
			selectedProps.add("Proto Piece Price Currency");
			selectedProps.add("Tooling Investment Value");
			selectedProps.add("Tooling Investment Currency Status");
			selectedProps.add("Refurbishment Tooling");
			selectedProps.add("Refurbishment Tooling Currency");
			selectedProps.add("Facility Investment Amount");
			selectedProps.add("Facility Investment Currency");
			selectedProps.add("Miscellanenous Cost Value");
			selectedProps.add("Miscellanenous Cost Currency");
			selectedProps.add("Industrialize Cost Value");
			selectedProps.add("Industrialize Cost Currency");

		}
		if (selectedExtraInfo.contains(EXTRA_INFO_VEHICLE_USAGE)) {
			/* Vehicle usage */
			selectedProps.add("Vietnam/ASEAN LHD Base BEV");
			selectedProps.add("Vietnam/ASEAN LHD Middle BEV");
			selectedProps.add("Vietnam/ASEAN LHD High BEV");
			selectedProps.add("Vietnam/ASEAN LHD Base ICE");
			selectedProps.add("Vietnam/ASEAN LHD Middle ICE");
			selectedProps.add("Vietnam/ASEAN LHD High ICE");
			selectedProps.add("US Base BEV");
			selectedProps.add("US High BEV");
			selectedProps.add("Common Part");
			selectedProps.add("Platform Part");
		}
		if (selectedExtraInfo.contains(EXTRA_INFO_PLANT_INFO)) {
			/* Vehicle usage */
			selectedProps.add("Plant Code");
			selectedProps.add("Department");
			selectedProps.add("Make/Buy");
		}

		if (selectedExtraInfo.contains(EXTRA_INFO_TGSS_DOC)) {
			selectedProps.add("TGSS Document Number");
		}

		if (selectedExtraInfo.contains(EXTRA_INFO_PFEP)) {
			selectedProps.add("P Release Plan Date");
			selectedProps.add("P Release Date Required?");
			selectedProps.add("P Release Comment");
			selectedProps.add("I Release Plan Date");
			selectedProps.add("I Release Date Required?");
			selectedProps.add("I Release Comment");
			selectedProps.add("PR Release Plan Date");
			selectedProps.add("PR Release Date Required?");
			selectedProps.add("PR Release Comment");
			selectedProps.add("Serial Tool");
			selectedProps.add("Serial Tool Planned Implementation");
			selectedProps.add("Serial Tooling Comment");
			selectedProps.add("Supplier Serial Tool");
			selectedProps.add("Prototype Tooling");
			selectedProps.add("Prototype Tooling Planned Implementation");
			selectedProps.add("Prototype Tooling Comment");
			selectedProps.add("Supplier Prototype");
			selectedProps.add("Soft Tooling");
			selectedProps.add("Soft Tooling Planned Implementation");
			selectedProps.add("Soft Tooling Comment");
			selectedProps.add("Supplier Soft Tool");
		}

		if (selectedExtraInfo.contains(EXTRA_INFO_CAD)) {
			selectedProps.add("JT Last Save Date");
			selectedProps.add("CATPart Last Save Date");
		}

		if (selectedExtraInfo.contains(EXTRA_HAVE_DRAWING)) {
			selectedProps.add(EXTRA_HAVE_DRAWING);
		}

		for (String str : selectedRules) {
			selectedProps.add(str);
		}

		if (selectedExtraInfo.contains(EXTRA_SAVED_VARIANTS)) {
			for (String variant : availableSavedVariants) {
				selectedProps.add(variant);
			}
		}

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

		/* in case IMDS report, the information will be split by each module */
		if (selectedExtraInfo.contains(EXTRA_INFO_IMDS)) {
			for (int i = 0; i < itemIDLvl_1.size(); i++) {
				for (String value : selectedProps) {
					if (selectedProps.get(0).compareToIgnoreCase(value) == 0) {
						CellStyle headerCellStyleYellow = wb.createCellStyle();
						headerCellStyleYellow.setFont(headerFont);
						headerCellStyleYellow.setAlignment(HorizontalAlignment.CENTER);
						headerCellStyleYellow.setVerticalAlignment(VerticalAlignment.CENTER);
						headerCellStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						headerCellStyleYellow.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
						headerCellStyleYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

						SXSSFCell cell = headerRow.createCell(counter);
						cell.setCellValue(itemIDLvl_1.get(i).substring(0, 3));
						cell.setCellStyle(headerCellStyleYellow);
						counter++;
					}
					getHeaderCell(counter, headerRow, value, headerCellStyle);
					counter++;
				}
			}
		} else {
			for (String value : selectedProps) {
				getHeaderCell(counter, headerRow, value, headerCellStyle);
				counter++;
			}
		}
		wb.getSheet(sheetName).createFreezePane(0, 1);
		/* auto size column */
		wb.getSheet(sheetName).trackAllColumnsForAutoSizing();
		for (int kz = 0; kz < selectedProps.size(); kz++) {
			wb.getSheet(sheetName).autoSizeColumn(kz);
		}
	}

	private SXSSFCell getHeaderCell(int index, SXSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		SXSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
		return cell;
	}

	private void extractTargetCostandEngiEstimatedCost(ArrayList<String> listUniqueBomLine) {

		/* query all cost item */
		LinkedHashMap<String, String> queryCostItem = new LinkedHashMap<String, String>();
		queryCostItem.put("Item ID", strBOMLineID.toString());
		queryCostItem.put("Type", "VF4_Cost");
		TCComponent[] costItems = query("___Admin Item...", queryCostItem);

		LinkedHashSet<String> listValidCostItem = new LinkedHashSet<String>();

		if (costItems != null) {
			DataManagementService.getService(session).refreshObjects(costItems);
			for (int i = 0; i < costItems.length; ++i) {
				try {
					TCComponentItem aCostItem = (TCComponentItem) costItems[i];
					String partNum = "";
					if (aCostItem.getProperty("item_id") != null) {
						partNum = aCostItem.getProperty("item_id");
					}
					TCComponent[] costRevsCompo = aCostItem.getRelatedComponents("revision_list");
					if (costRevsCompo != null) {
						for (int k = 0; k < costRevsCompo.length; ++k) {
							TCComponentItemRevision aCostRev = (TCComponentItemRevision) costRevsCompo[k];
							TCComponent[] costCompo = aCostRev.getRelatedComponents("VF4_SourcingCostFormRela");
							String creationDate = "";
							String EDDCostTarget = "0";
							String toolingCostTarget = "0";
							String pieceCostTarget = "0";
							String facilityInvestAmount = "0";
							String facilityInvestCurr = "";
							String miscellaneousCost = "0";
							String miscellaneousCurr = "";
							String pieceCostValueStatus = "0";
							String pieceCostCurr = "";
							String protoPiecePrice = "0";
							String protoPiecePriceCurr = "";
							String protoToolingValue = "0";
							String protoToolingCurr = "";
							String qualityFinance = "";
							String refurToolingValue = "0";
							String refurToolingCurr = "";
							String supplierLogicCost = "0";
							String supplierPackageAmount = "0";
							String toolingInvestValue = "0";
							String toolingInvestCurr = "";
							String totalCost = "0";
							String totalCostCurr = "";
							String EdndCost = "0";
							String EdndCurr = "";
							String releaseStatus = "";
							String industrializeCost = "0";
							String industrializeCurr = "";
							String RNOReferNumber = "";
							String costValid = "";
							String vehicleUsageValid = "false";
							boolean hasRightAccess = false;
							if (aCostRev.getProperty("creation_date") != null) {
								creationDate = aCostRev.getProperty("creation_date");
							}
							if (aCostRev.getProperty("release_status_list") != null) {
								releaseStatus = aCostRev.getProperty("release_status_list");
							}

							/* ignore base line revision */
							String[] partStatus = releaseStatus.split(",");
							List<String> ignoreStatus = Arrays.asList(VINFAST_COST_BOM_IGNORE_STATUS_VALUE);
							boolean isIgnore = false;
							if (partStatus != null && partStatus.length > 0) {
								for (String str : ignoreStatus) {
									if (Arrays.asList(partStatus).contains(str)) {
										LOGGER.info("[BOMCostReport] Ignore revision: " + partNum + "/" + (k + 1));
										isIgnore = true;
									}
								}
							}
							if (isIgnore) {
								continue;
							}
							/* ignore base line revision */

							if (costCompo.length != 0) {
								for (int x = 0; x < costCompo.length; ++x) {
									TCComponentForm costForm = (TCComponentForm) costCompo[x];
									String formType = costForm.getProperty("object_type");
									if (formType.compareToIgnoreCase(TARGET_COST_FORM_TYPE) == 0) {
										/* get target cost information */
										String[] requestProp = { "vf4_ednd_cost_value_target", "vf4_tooling_invest_target", "vf4_piece_cost_value_target" };
										String[] responseProp = costForm.getProperties(requestProp);
										if (!responseProp[0].equals("")) {
											EDDCostTarget = responseProp[0];
											costValid = "true";
										}
										if (!responseProp[1].equals("")) {
											toolingCostTarget = responseProp[1];
											costValid = "true";
										}
										if (!responseProp[2].equals("")) {
											pieceCostTarget = responseProp[2];
											costValid = "true";
										}
									} else if (formType.compareToIgnoreCase(PIECE_COST_FORM_TYPE) == 0) {
										/* get piece cost information */
										String[] requestProp = { "vf4_facility_invest_amount", "vf4_facility_invest_curr", "vf4_miscellaneous_cost", "vf4_miscellaneous_cost_curr", "vf4_piece_cost_curr", "vf4_piece_cost_value_status", "vf4_proto_piece_price", "vf4_proto_piece_price_curr", "vf4_proto_tooling_curr", "vf4_proto_tooling_value", "vf4_quality_of_finance", "vf4_refurbish_tooling_curr", "vf4_refurbish_tooling_value", "vf4_rno_refer_number", "vf4_supplier_logisis_cost",
												"vf4_supplier_package_amount", "vf4_tooling_invest_value", "vf4_tooling_invtest_curr", "vf4_total_cost_manually", "vf4_total_cost_status_curr", "vf4Ednd_curr", "vf4EdndCost", "vf4_industrialization_cost", "vf4_industrialization_curr" };
										String[] responseProp = costForm.getProperties(requestProp);
										if (!responseProp[0].equals("")) {
											facilityInvestAmount = responseProp[0];
										}
										if (!responseProp[1].equals("")) {
											facilityInvestCurr = responseProp[1];
										}
										if (!responseProp[2].equals("")) {
											miscellaneousCost = responseProp[2];
										}
										if (!responseProp[3].equals("")) {
											miscellaneousCurr = responseProp[3];
										}
										if (!responseProp[4].equals("")) {
											pieceCostCurr = responseProp[4];
										}
										if (!responseProp[5].equals("")) {
											pieceCostValueStatus = responseProp[5];
										}
										if (!responseProp[6].equals("")) {
											protoPiecePrice = responseProp[6];
										}
										if (!responseProp[7].equals("")) {
											protoPiecePriceCurr = responseProp[7];
										}
										if (!responseProp[8].equals("")) {
											protoToolingCurr = responseProp[8];
										}
										if (!responseProp[9].equals("")) {
											protoToolingValue = responseProp[9];
										}
										if (!responseProp[10].equals("")) {
											qualityFinance = responseProp[10];
											costValid = "true";
										}
										if (!responseProp[11].equals("")) {
											refurToolingCurr = responseProp[11];
										}
										if (!responseProp[12].equals("")) {
											refurToolingValue = responseProp[12];
										}
										if (!responseProp[13].equals("")) {
											RNOReferNumber = responseProp[13];
										}
										if (!responseProp[14].equals("")) {
											supplierLogicCost = responseProp[14];
										}
										if (!responseProp[15].equals("")) {
											supplierPackageAmount = responseProp[15];
										}
										if (!responseProp[16].equals("")) {
											toolingInvestValue = responseProp[16];
										}
										if (!responseProp[17].equals("")) {
											toolingInvestCurr = responseProp[17];
										}
										if (!responseProp[18].equals("")) {
											totalCost = responseProp[18];
										}
										if (!responseProp[19].equals("")) {
											totalCostCurr = responseProp[19];
										}
										if (!responseProp[20].equals("")) {
											EdndCurr = responseProp[20];
										}
										if (!responseProp[21].equals("")) {
											EdndCost = responseProp[21];
										}
										if (!responseProp[22].equals("")) {
											industrializeCost = responseProp[22];
										}
										if (!responseProp[23].equals("")) {
											industrializeCurr = responseProp[23];
										}
									}
								}
								hasRightAccess = true;
							}
							/* store cost data to temp memory */

							CostObject costExisted = mapCostData.get(partNum);
							if (costExisted == null) {
								CostObject costNew = new CostObject();

								costNew.setEDDCostTarget(EDDCostTarget);
								costNew.setToolingCostTarget(toolingCostTarget);
								costNew.setPieceCostTarget(pieceCostTarget);

								/*
								 * update latest tooling cost to mapping
								 */
								costNew.setFacilityInvestAmount(facilityInvestAmount);
								costNew.setFacilityInvestCurr(facilityInvestCurr);
								costNew.setMiscellaneousCost(miscellaneousCost);
								costNew.setMiscellaneousCurr(miscellaneousCurr);
								costNew.setProtoPiecePrice(protoPiecePrice);
								costNew.setProtoPiecePriceCurr(protoPiecePriceCurr);
								costNew.setProtoToolingCurr(protoToolingCurr);
								costNew.setProtoToolingValue(protoToolingValue);
								costNew.setRefurToolingCurr(refurToolingCurr);
								costNew.setRefurToolingValue(refurToolingValue);
								costNew.setToolingInvestValue(toolingInvestValue);
								costNew.setToolingInvestCurr(toolingInvestCurr);
								costNew.setEdndCost(EdndCost);
								costNew.setEdndCurr(EdndCurr);
								costNew.setIndustrializeCost(industrializeCost);
								costNew.setIndustrializeCurr(industrializeCurr);
								costNew.setCostValid(costValid);

								if (qualityFinance.compareToIgnoreCase(INTERIM) == 0 || qualityFinance.compareToIgnoreCase("") == 0) {
									/* update interim actual cost */
									costNew.setPieceCostCurr(pieceCostCurr);
									costNew.setPieceCostValueStatus(pieceCostValueStatus);
									costNew.setSupplierLogicCost(supplierLogicCost);
									costNew.setSupplierPackageAmount(supplierPackageAmount);
									costNew.setTotalCost(totalCost);
									costNew.setTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(FINAL) == 0) {
									/* update final actual cost */
									costNew.setFinalPieceCostCurr(pieceCostCurr);
									costNew.setFinalPieceCostValueStatus(pieceCostValueStatus);
									costNew.setFinalSupplierLogicCost(supplierLogicCost);
									costNew.setFinalSupplierPackageAmount(supplierPackageAmount);
									costNew.setFinalTotalCost(totalCost);
									costNew.setFinalTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(BUYER) == 0) {
									/* update buyer actual cost */
									costNew.setBuyerPieceCostCurr(pieceCostCurr);
									costNew.setBuyerPieceCostValueStatus(pieceCostValueStatus);
									costNew.setBuyerSupplierLogicCost(supplierLogicCost);
									costNew.setBuyerSupplierPackageAmount(supplierPackageAmount);
									costNew.setBuyerTotalCost(totalCost);
									costNew.setBuyerTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(ENGINEER) == 0) {
									/* update engineer actual cost */
									costNew.setEngineerPieceCostCurr(pieceCostCurr);
									costNew.setEngineerPieceCostValueStatus(pieceCostValueStatus);
									costNew.setEngineerSupplierLogicCost(supplierLogicCost);
									costNew.setEngineerSupplierPackageAmount(supplierPackageAmount);
									costNew.setEngineerTotalCost(totalCost);
									costNew.setEngineerTotalCostCurr(totalCostCurr);
								}
								costNew.setRevisionCreationDate(creationDate);
								costNew.setRightAccess(hasRightAccess);
								mapCostData.put(partNum, costNew);
							} else {
								/*
								 * update latest target cost to mapping
								 */
								costExisted.setEDDCostTarget(EDDCostTarget);
								costExisted.setToolingCostTarget(toolingCostTarget);
								costExisted.setPieceCostTarget(pieceCostTarget);
								costExisted.setCostValid(costValid);
								/*
								 * update latest tooling cost to mapping
								 */
								costExisted.setFacilityInvestAmount(facilityInvestAmount);
								costExisted.setFacilityInvestCurr(facilityInvestCurr);
								costExisted.setMiscellaneousCost(miscellaneousCost);
								costExisted.setMiscellaneousCurr(miscellaneousCurr);
								costExisted.setProtoPiecePrice(protoPiecePrice);
								costExisted.setProtoPiecePriceCurr(protoPiecePriceCurr);
								costExisted.setProtoToolingCurr(protoToolingCurr);
								costExisted.setProtoToolingValue(protoToolingValue);
								costExisted.setRefurToolingCurr(refurToolingCurr);
								costExisted.setRefurToolingValue(refurToolingValue);
								costExisted.setToolingInvestValue(toolingInvestValue);
								costExisted.setToolingInvestCurr(toolingInvestCurr);
								costExisted.setEdndCost(EdndCost);
								costExisted.setEdndCurr(EdndCurr);
								costExisted.setIndustrializeCost(industrializeCost);
								costExisted.setIndustrializeCurr(industrializeCurr);
								if (qualityFinance.compareToIgnoreCase(INTERIM) == 0 || qualityFinance.compareToIgnoreCase("") == 0) {
									/* update interim actual cost */
									costExisted.setPieceCostCurr(pieceCostCurr);
									costExisted.setPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setSupplierLogicCost(supplierLogicCost);
									costExisted.setSupplierPackageAmount(supplierPackageAmount);
									costExisted.setTotalCost(totalCost);
									costExisted.setTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(FINAL) == 0) {
									/* update final actual cost */
									costExisted.setFinalPieceCostCurr(pieceCostCurr);
									costExisted.setFinalPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setFinalSupplierLogicCost(supplierLogicCost);
									costExisted.setFinalSupplierPackageAmount(supplierPackageAmount);
									costExisted.setFinalTotalCost(totalCost);
									costExisted.setFinalTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(BUYER) == 0) {
									/* update buyer actual cost */
									costExisted.setBuyerPieceCostCurr(pieceCostCurr);
									costExisted.setBuyerPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setBuyerSupplierLogicCost(supplierLogicCost);
									costExisted.setBuyerSupplierPackageAmount(supplierPackageAmount);
									costExisted.setBuyerTotalCost(totalCost);
									costExisted.setBuyerTotalCostCurr(totalCostCurr);
								}
								if (qualityFinance.compareToIgnoreCase(ENGINEER) == 0) {
									/* update engineer actual cost */
									costExisted.setEngineerPieceCostCurr(pieceCostCurr);
									costExisted.setEngineerPieceCostValueStatus(pieceCostValueStatus);
									costExisted.setEngineerSupplierLogicCost(supplierLogicCost);
									costExisted.setEngineerSupplierPackageAmount(supplierPackageAmount);
									costExisted.setEngineerTotalCost(totalCost);
									costExisted.setEngineerTotalCostCurr(totalCostCurr);
								}
								costExisted.setRevisionCreationDate(creationDate);
								costExisted.setRightAccess(hasRightAccess);
							}
							/* 18Jun2020-add break to show only latest cost revision */
							// break;
						}
					}
					listValidCostItem.add(partNum);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			/* validate object cost has been created */
			for (Map.Entry<String, CostObject> entryMaster : mapCostData.entrySet()) {
				CostObject costData = (CostObject) entryMaster.getValue();
				String partNum = entryMaster.getKey();
				if (!costData.isRightAccess()) {
					if (listValidCostItem.contains(partNum)) {
						/* init not found cost object */
						costData = new CostObject("NO ACCESS");
						mapCostData.put(partNum, costData);

					} else {
						/* init no acess cost object */
						costData = new CostObject("NOT FOUND");
						mapCostData.put(partNum, costData);
					}
				}
			}

		}
	}

	private TCComponent[] query(String queryName, LinkedHashMap<String, String> queryInput) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[queryInput.size()];
			qc.values = new String[queryInput.size()];
			int i = 0;
			for (Entry<String, String> pair : queryInput.entrySet()) {
				qc.entries[i] = pair.getKey();
				qc.values[i] = pair.getValue();
				i++;
			}
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			} else {
				System.out.println("NO Cost Object FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objects;
	}

	private void validateBOMLine(LinkedHashMap<Integer, LinkedHashMap<String, String>> mapBOMLineInfo, LinkedHashMap<String, List<String>> mapRuleCondition) {
		for (Map.Entry<Integer, LinkedHashMap<String, String>> entryBOMLineInfo : mapBOMLineInfo.entrySet()) {
			LinkedHashMap<String, String> aBOMLine = entryBOMLineInfo.getValue();
			for (Map.Entry<String, List<String>> entryRuleCondi : mapRuleCondition.entrySet()) {
				String ruleName = entryRuleCondi.getKey();
				System.out.println("[BOMREPORT] Validating rule: " + ruleName);
				List<String> rules = entryRuleCondi.getValue();
				for (String str : rules) {
					String result = "Passed";
					String[] conditions = str.split(",");
					for (int i = 0; i < conditions.length; i++) {
						ArrayList<String> pieceCondition = splitCondition(conditions[i]);
						String criteria = pieceCondition.get(0);
						String operation = pieceCondition.get(1);
						String expected = pieceCondition.get(2);
						if (expected.compareToIgnoreCase("null") == 0) {
							expected = "";
						}
						if (operation.compareTo("==") == 0 && aBOMLine.get(criteria).compareToIgnoreCase(expected) != 0) {
							result = "Failed";
							break;
						}
						if (operation.compareTo("!=") == 0 && aBOMLine.get(criteria).compareToIgnoreCase(expected) == 0) {
							result = "Failed";
							break;
						}
					}
					aBOMLine.put(ruleName, result);
				}
			}
		}

	}

	private void extractVehicleUsage(ArrayList<String> listUniqueBomLine) {
		/* query all vehicle usage form */
		LinkedHashMap<String, String> queryVehicleUsage = new LinkedHashMap<String, String>();
		queryVehicleUsage.put("Name", listVehicleUsageNames.toString());
		queryVehicleUsage.put("Type", "VF4_VehicleUsageForm");
		TCComponent[] vehicleUsage = query("General...", queryVehicleUsage);

		if (vehicleUsage != null) {
			DataManagementService.getService(session).refreshObjects(vehicleUsage);
			for (int j = 0; j != vehicleUsage.length; ++j) {
				TCComponentForm vehiForm = (TCComponentForm) vehicleUsage[j];
				try {
					// vehiForm.refresh();
					String formName = vehiForm.getProperty("object_name");
					String partNum = "";
					String VNASEANBaseICE = "";
					String VNASEANMiddleICE = "";
					String VNASEANHighICE = "";
					String VNASEANBaseBEV = "";
					String VNASEANMiddleBEV = "";
					String VNASEANHighBEV = "";
					String USHighBEV = "";
					String USBaseBev = "";
					String commonPart = "";
					String platformPart = "";
					String vehicleUsageValid = "";
					if (formName != null) {
						AIFComponentContext[] aifPrimary = vehiForm.getPrimary();
						if (aifPrimary.length > 1) {
							System.out.println("[TargetCostReport] Vehicle usage form attached to 2 part: " + formName);
							continue;
						}
						if (aifPrimary.length == 0) {
							System.out.println("[TargetCostReport] Vehicle usage form doesn't attach to any part: " + formName);
							continue;
						}
						TCComponent primaryComp = (TCComponent) aifPrimary[0].getComponent();
						partNum = primaryComp.getProperty("item_id");

						if (vehiForm.getProperty("vf4_vn_asean_base_bev") != null && vehiForm.getProperty("vf4_vn_asean_base_bev").compareToIgnoreCase("true") == 0) {
							VNASEANBaseBEV = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_vn_asean_middle_bev") != null && vehiForm.getProperty("vf4_vn_asean_middle_bev").compareToIgnoreCase("true") == 0) {
							VNASEANMiddleBEV = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_vn_asean_high_bev") != null && vehiForm.getProperty("vf4_vn_asean_high_bev").compareToIgnoreCase("true") == 0) {
							VNASEANHighBEV = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_vn_asean_base_ice") != null && vehiForm.getProperty("vf4_vn_asean_base_ice").compareToIgnoreCase("true") == 0) {
							VNASEANBaseICE = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_vn_asean_middle_ice") != null && vehiForm.getProperty("vf4_vn_asean_middle_ice").compareToIgnoreCase("true") == 0) {
							VNASEANMiddleICE = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_vn_asean_high_ice") != null && vehiForm.getProperty("vf4_vn_asean_high_ice").compareToIgnoreCase("true") == 0) {
							VNASEANHighICE = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_us_high_bev") != null && vehiForm.getProperty("vf4_us_high_bev").compareToIgnoreCase("true") == 0) {
							USHighBEV = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_boolean_1") != null && vehiForm.getProperty("vf4_boolean_1").compareToIgnoreCase("true") == 0) {
							USBaseBev = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_boolean_2") != null && vehiForm.getProperty("vf4_boolean_2").compareToIgnoreCase("true") == 0) {
							commonPart = "x";
							VNASEANBaseBEV = "x";
							VNASEANMiddleBEV = "x";
							VNASEANHighBEV = "x";
							VNASEANBaseICE = "x";
							VNASEANMiddleICE = "x";
							VNASEANHighICE = "x";
							USHighBEV = "x";
							USBaseBev = "x";
							vehicleUsageValid = "true";
						}
						if (vehiForm.getProperty("vf4_platform_part") != null && vehiForm.getProperty("vf4_platform_part").compareToIgnoreCase("true") == 0) {
							platformPart = "x";
							vehicleUsageValid = "true";
						}
						for (Map.Entry<String, CostObject> entry : mapCostData.entrySet()) {
							CostObject obj = (CostObject) entry.getValue();
							if (entry.getKey().compareToIgnoreCase(partNum) == 0) {
								obj.setVNASEANBaseBEV(VNASEANBaseBEV);
								obj.setVNASEANHighBEV(VNASEANHighBEV);
								obj.setVNASEANMiddleBEV(VNASEANMiddleBEV);
								obj.setVNASEANBaseICE(VNASEANBaseICE);
								obj.setVNASEANHighICE(VNASEANHighICE);
								obj.setVNASEANMiddleICE(VNASEANMiddleICE);
								obj.setUSBaseBev(USBaseBev);
								obj.setUSHighBEV(USHighBEV);
								obj.setCommonPart(commonPart);
								obj.setPlatformPart(platformPart);
								obj.setVehicleUsageValid(vehicleUsageValid);
							}
						}
					} else {
						continue;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void extractPFEP() {
		/* query all vehicle usage form */
		LinkedHashMap<String, String> queryPFEP = new LinkedHashMap<String, String>();
		queryPFEP.put("ID", strBOMLineID.toString());
		TCComponent[] pfepItems = query("PFEP Form", queryPFEP);

		if (pfepItems != null) {
			DataManagementService.getService(session).refreshObjects(pfepItems);
			for (int j = 0; j != pfepItems.length; ++j) {
				try {
					TCComponentForm pfepItem = (TCComponentForm) pfepItems[j];
					String partNum = "";
					if (pfepItem.getProperty("vf4_part_number") != null) {
						partNum = pfepItem.getProperty("vf4_part_number");
					}

					String vf4_P_release_plan_date = "";
					String vf4_isNotRequired_P = "";
					String vf4_why_notRequired_P = "";
					String vf4_I_release_plan_date = "";
					String vf4_isNotRequired_I = "";
					String vf4_why_notRequired_I = "";
					String vf4_PR_release_plan_date = "";
					String vf4_isNotRequired_PR = "";
					String vf4_why_notRequired_PR = "";
					String vf4_serial_tool = "";
					String vf4_serial_tool_plan = "";
					String vf4_serialToolPlan_comment = "";
					String vf4_supplier_serial_tool = "";
					String vf4_prototype_tooling = "";
					String vf4_prototype_tool_plan = "";
					String vf4_protoToolPlan_comment = "";
					String vf4_supplier_prototype = "";
					String vf4_soft_tooling = "";
					String vf4_soft_tool_plan = "";
					String vf4_softToolPlan_comment = "";
					String vf4_supplier_soft_tool = "";

					String formName = pfepItem.getProperty("object_name");

					if (formName != null) {
						String[] requestProp = { "vf4_P_release_plan_date", "vf4_isNotRequired_P", "vf4_why_notRequired_P", "vf4_I_release_plan_date", "vf4_isNotRequired_I", "vf4_why_notRequired_I", "vf4_PR_release_plan_date", "vf4_isNotRequired_PR", "vf4_why_notRequired_PR", "vf4_serial_tool", "vf4_serial_tool_plan", "vf4_serialToolPlan_comment", "vf4_supplier_serial_tool", "vf4_prototype_tooling", "vf4_prototype_tool_plan", "vf4_protoToolPlan_comment", "vf4_supplier_prototype",
								"vf4_soft_tooling", "vf4_soft_tool_plan", "vf4_softToolPlan_comment", "vf4_supplier_soft_tool", };
						String[] responseProp = pfepItem.getProperties(requestProp);

						vf4_P_release_plan_date = responseProp[0];
						vf4_isNotRequired_P = responseProp[1];
						vf4_why_notRequired_P = responseProp[2];
						vf4_I_release_plan_date = responseProp[3];
						vf4_isNotRequired_I = responseProp[4];
						vf4_why_notRequired_I = responseProp[5];
						vf4_PR_release_plan_date = responseProp[6];
						vf4_isNotRequired_PR = responseProp[7];
						vf4_why_notRequired_PR = responseProp[8];
						vf4_serial_tool = responseProp[9];
						vf4_serial_tool_plan = responseProp[10];
						vf4_serialToolPlan_comment = responseProp[11];
						vf4_supplier_serial_tool = responseProp[12];
						vf4_prototype_tooling = responseProp[13];
						vf4_prototype_tool_plan = responseProp[14];
						vf4_protoToolPlan_comment = responseProp[15];
						vf4_supplier_prototype = responseProp[16];
						vf4_soft_tooling = responseProp[17];
						vf4_soft_tool_plan = responseProp[18];
						vf4_softToolPlan_comment = responseProp[19];
						vf4_supplier_soft_tool = responseProp[20];

						PFEPObject obj = new PFEPObject();
						obj.setVf4_P_release_plan_date(vf4_P_release_plan_date);
						obj.setVf4_isNotRequired_P(vf4_isNotRequired_P);
						obj.setVf4_why_notRequired_P(vf4_why_notRequired_P);
						obj.setVf4_I_release_plan_date(vf4_I_release_plan_date);
						obj.setVf4_isNotRequired_I(vf4_isNotRequired_I);
						obj.setVf4_why_notRequired_I(vf4_why_notRequired_I);
						obj.setVf4_PR_release_plan_date(vf4_PR_release_plan_date);
						obj.setVf4_isNotRequired_PR(vf4_isNotRequired_PR);
						obj.setVf4_why_notRequired_PR(vf4_why_notRequired_PR);
						obj.setVf4_serial_tool(vf4_serial_tool);
						obj.setVf4_serial_tool_plan(vf4_serial_tool_plan);
						obj.setVf4_serialToolPlan_comment(vf4_serialToolPlan_comment);
						obj.setVf4_supplier_serial_tool(vf4_supplier_serial_tool);
						obj.setVf4_prototype_tooling(vf4_prototype_tooling);
						obj.setVf4_prototype_tool_plan(vf4_prototype_tool_plan);
						obj.setVf4_protoToolPlan_comment(vf4_protoToolPlan_comment);
						obj.setVf4_supplier_prototype(vf4_supplier_prototype);
						obj.setVf4_soft_tooling(vf4_soft_tooling);
						obj.setVf4_soft_tool_plan(vf4_soft_tool_plan);
						obj.setVf4_softToolPlan_comment(vf4_softToolPlan_comment);
						obj.setVf4_supplier_soft_tool(vf4_supplier_soft_tool);
						mapPFEPData.put(partNum.trim(), obj);

						LinkedHashMap<String, String> BOMLineInfo = mapMasterBOMLineInfo.get(partNum.trim());
						if (BOMLineInfo != null) {
							BOMLineInfo.put("P Release Plan Date", vf4_P_release_plan_date);
							BOMLineInfo.put("P Release Date Required?", vf4_isNotRequired_P);
							BOMLineInfo.put("P Release Comment", vf4_why_notRequired_P);
							BOMLineInfo.put("I Release Plan Date", vf4_I_release_plan_date);
							BOMLineInfo.put("I Release Date Required?", vf4_isNotRequired_I);
							BOMLineInfo.put("I Release Comment", vf4_why_notRequired_I);
							BOMLineInfo.put("PR Release Plan Date", vf4_PR_release_plan_date);
							BOMLineInfo.put("PR Release Date Required?", vf4_isNotRequired_PR);
							BOMLineInfo.put("PR Release Comment", vf4_why_notRequired_PR);
							BOMLineInfo.put("Serial Tool", vf4_serial_tool);
							BOMLineInfo.put("Serial Tool Planned Implementation", vf4_serial_tool_plan);
							BOMLineInfo.put("Serial Tooling Comment", vf4_serialToolPlan_comment);
							BOMLineInfo.put("Supplier Serial Tool", vf4_supplier_serial_tool);
							BOMLineInfo.put("Prototype Tooling", vf4_prototype_tooling);
							BOMLineInfo.put("Prototype Tooling Planned Implementation", vf4_prototype_tool_plan);
							BOMLineInfo.put("Prototype Tooling Comment", vf4_protoToolPlan_comment);
							BOMLineInfo.put("Supplier Prototype", vf4_supplier_prototype);
							BOMLineInfo.put("Soft Tooling", vf4_soft_tooling);
							BOMLineInfo.put("Soft Tooling Planned Implementation", vf4_soft_tool_plan);
							BOMLineInfo.put("Soft Tooling Comment", vf4_softToolPlan_comment);
							BOMLineInfo.put("Supplier Soft Tool", vf4_supplier_soft_tool);
						}
					} else {
						continue;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean extractHaveDrawing(TCComponentBOMLine bomline) {
		try {
			TCComponentItemRevision item = bomline.getItemRevision();

			TCComponent[] spec = item.getRelatedComponents("IMAN_specification");
			if (spec.length > 0) {
				for (TCComponent comp : spec) {
					if (comp.getTypeComponent().isTypeOf("Dataset") && comp.getDisplayType().compareToIgnoreCase("CATDrawing") == 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private BOMReport_CADModel extractCAD(TCComponentBOMLine bomline) {
		BOMReport_CADModel newItem = new BOMReport_CADModel();

		if (bomline != null) {
			try {
				Boolean stopable = true;
				TCComponentItemRevision item = bomline.getItemRevision();

				TCComponent[] spec = item.getRelatedComponents("IMAN_specification");
				if (spec.length > 0) {
					stopable = false;
					for (TCComponent comp : spec) {
						if (comp.getTypeComponent().isTypeOf("Dataset") && comp.getDisplayType().compareToIgnoreCase("CATPart") == 0) {
							TCComponentDataset dataset = (TCComponentDataset) comp;
							TCComponent[] namedRefs = dataset.getNamedReferences();
							for (TCComponent namedItem : namedRefs) {
								if (namedItem instanceof TCComponentTcFile) {
									TCComponentTcFile file = (TCComponentTcFile) namedItem;
									String last_Mod = file.getProperty("last_mod_date");
									newItem.setCADPart_Last_Save_Date(last_Mod);
									stopable = true;
									break;
								}
							}
							if (stopable)
								break;
						}
					}
				}

				TCComponent[] render = item.getRelatedComponents("IMAN_Rendering");
				if (render.length > 0) {
					stopable = false;
					for (TCComponent comp : render) {
						if (comp.getTypeComponent().isTypeOf("Dataset") && comp.getDisplayType().compareToIgnoreCase("Direct Model") == 0) {
							TCComponentDataset dataset = (TCComponentDataset) comp;
							TCComponent[] namedRefs = dataset.getNamedReferences();
							for (TCComponent namedItem : namedRefs) {
								if (namedItem instanceof TCComponentTcFile) {
									TCComponentTcFile file = (TCComponentTcFile) namedItem;
									String last_Mod = file.getProperty("last_mod_date");
									newItem.setJT_Last_Save_Date(last_Mod);
									stopable = true;
									break;
								}
							}
							if (stopable)
								break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return newItem;
	}

	private void extractManufPlantInformation(TCComponentBOMLine bomline) throws TCException, NotLoadedException {
		String bomlineID = bomline.getProperty("bl_item_item_id");
		/* ignore part which has checked */
//		if (listBOMLineID.contains(bomlineID)) {
//			return;
//		}
		PlantInformationObject dataReport = new PlantInformationObject();
		String bomLinePosID = bomline.getProperty("VL5_pos_id");
		mapPlantInformationData.put(bomlineID.concat(bomLinePosID).trim(), dataReport);
		TCComponentItem item = bomline.getItem();
		TCComponent[] plantForm = item.getRelatedComponents("VF4_plant_form_relation");
		if (plantForm.length <= 0) {
			LOGGER.error("[ERROR] Part NOT have plant form: " + bomlineID);
			return;
		}
		ModelObject[] plantTable = plantForm[0].getRelatedComponents("vf4_plant_table_row");
		StringBuilder plantCode = new StringBuilder();
		StringBuilder department = new StringBuilder();
		StringBuilder makeOrBuy = new StringBuilder();
		for (int i = 0; i < plantTable.length; ++i) {
			String posIDInPlantTable = plantTable[i].getPropertyDisplayableValue("vf4_pos_id");
			if (bomLinePosID.compareToIgnoreCase(posIDInPlantTable.toString()) == 0) {
				if (plantCode.length() > 0)
					plantCode.append("\n");
				plantCode.append(plantTable[i].getPropertyDisplayableValue("vf4_plant_code"));
				dataReport.setPlantCode(plantCode.toString());

				if (department.length() > 0)
					department.append("\n");
				department.append(plantTable[i].getPropertyDisplayableValue("vf4_department"));
				dataReport.setDepartment(department.toString());

				if (makeOrBuy.length() > 0)
					makeOrBuy.append("\n");
				makeOrBuy = makeOrBuy.append(plantTable[i].getPropertyDisplayableValue("vf4_make_or_buy"));
				dataReport.setMakeOrBuy(makeOrBuy.toString());

				break;
			}
		}
	}

	private void makeFinalDatatoGenereateReport() {
		/* mapping cost-vehicle usage */
		for (int i = 0; i < mapMasterBOMLineInfo.size(); i++) {
			LinkedHashMap<String, String> mapBomLineData = mapMasterBOMLineInfo.get(i);
			CostObject costVehiUsageData = mapCostData.get(mapBomLineData.get("Item ID"));

			/* target cost */
			mapBomLineData.put("EDD Cost Target", costVehiUsageData.getEDDCostTarget());
			mapBomLineData.put("Tooling Cost Target", costVehiUsageData.getToolingCostTarget());
			mapBomLineData.put("Piece Cost Target", costVehiUsageData.getPieceCostTarget());
			mapBomLineData.put("Cost", costVehiUsageData.getCostValid());

			/* interim cost */
			mapBomLineData.put("Actual Piece Cost Interim Quote", costVehiUsageData.getPieceCostValueStatus());
			mapBomLineData.put("Actual Currency Interim Quote", costVehiUsageData.getPieceCostCurr());
			mapBomLineData.put("Supplier Packing Amount Interim Quote", costVehiUsageData.getSupplierPackageAmount());
			mapBomLineData.put("Suplier Logistic Cost Interim Quote", costVehiUsageData.getSupplierLogicCost());
			mapBomLineData.put("Total Cost Interim Quote", costVehiUsageData.getTotalCost());

			/* final cost */
			mapBomLineData.put("Actual Piece Cost Final Quote", costVehiUsageData.getFinalPieceCostValueStatus());
			mapBomLineData.put("Actual Currency Final Quote", costVehiUsageData.getFinalPieceCostCurr());
			mapBomLineData.put("Supplier Packing Final Quote", costVehiUsageData.getFinalSupplierPackageAmount());
			mapBomLineData.put("Suplier Logistic Final Quote", costVehiUsageData.getFinalSupplierLogicCost());
			mapBomLineData.put("Total Cost Final Quote", costVehiUsageData.getFinalTotalCost());

			/* buyer cost */
			mapBomLineData.put("Actual Piece Cost Buyer Estimate", costVehiUsageData.getBuyerPieceCostValueStatus());
			mapBomLineData.put("Actual Currency Buyer Estimate", costVehiUsageData.getBuyerPieceCostCurr());
			mapBomLineData.put("Supplier Packing Buyer Estimate", costVehiUsageData.getBuyerSupplierPackageAmount());
			mapBomLineData.put("Suplier Logistic Buyer Estimate", costVehiUsageData.getBuyerSupplierLogicCost());
			mapBomLineData.put("Total Cost Buyer Estimate", costVehiUsageData.getBuyerTotalCost());

			/* engineer cost */
			mapBomLineData.put("Actual Piece Cost Cost Engineer Estimation", costVehiUsageData.getEngineerPieceCostValueStatus());
			mapBomLineData.put("Actual Currency Cost Engineer Estimation", costVehiUsageData.getEngineerPieceCostCurr());
			mapBomLineData.put("Supplier Packing Cost Engineer Estimation", costVehiUsageData.getEngineerSupplierPackageAmount());
			mapBomLineData.put("Suplier Logistic Cost Engineer Estimation", costVehiUsageData.getEngineerSupplierLogicCost());
			mapBomLineData.put("Total Cost Cost Engineer Estimation", costVehiUsageData.getEngineerTotalCost());

			/* tooling and ednd cost */
			mapBomLineData.put("ED&D Cost Value", costVehiUsageData.getEdndCost());
			mapBomLineData.put("ED&D Cost currency", costVehiUsageData.getEdndCurr());
			mapBomLineData.put("Proto Tooling Value", costVehiUsageData.getProtoToolingValue());
			mapBomLineData.put("Proto Tooling Currency", costVehiUsageData.getProtoToolingCurr());
			mapBomLineData.put("Proto Piece Price", costVehiUsageData.getProtoPiecePrice());
			mapBomLineData.put("Proto Piece Price Currency", costVehiUsageData.getProtoPiecePriceCurr());
			mapBomLineData.put("Tooling Investment Value", costVehiUsageData.getToolingInvestValue());
			mapBomLineData.put("Tooling Investment Currency Status", costVehiUsageData.getToolingInvestCurr());
			mapBomLineData.put("Refurbishment Tooling", costVehiUsageData.getRefurToolingValue());
			mapBomLineData.put("Refurbishment Tooling Currency", costVehiUsageData.getRefurToolingValue());
			mapBomLineData.put("Facility Investment Amount", costVehiUsageData.getFacilityInvestAmount());
			mapBomLineData.put("Facility Investment Currency", costVehiUsageData.getFacilityInvestCurr());
			mapBomLineData.put("Miscellanenous Cost Value", costVehiUsageData.getMiscellaneousCost());
			mapBomLineData.put("Miscellanenous Cost Currency", costVehiUsageData.getMiscellaneousCurr());
			mapBomLineData.put("Industrialize Cost Value", costVehiUsageData.getIndustrializeCost());
			mapBomLineData.put("Industrialize Cost Currency", costVehiUsageData.getIndustrializeCurr());

			/* vehicle usage */
			mapBomLineData.put("Vietnam/ASEAN LHD Base BEV", costVehiUsageData.getVNASEANBaseBEV());
			mapBomLineData.put("Vietnam/ASEAN LHD Middle BEV", costVehiUsageData.getVNASEANMiddleBEV());
			mapBomLineData.put("Vietnam/ASEAN LHD High BEV", costVehiUsageData.getVNASEANHighBEV());
			mapBomLineData.put("Vietnam/ASEAN LHD Base ICE", costVehiUsageData.getVNASEANBaseICE());
			mapBomLineData.put("Vietnam/ASEAN LHD Middle ICE", costVehiUsageData.getVNASEANMiddleICE());
			mapBomLineData.put("Vietnam/ASEAN LHD High ICE", costVehiUsageData.getVNASEANHighICE());
			mapBomLineData.put("US Base BEV", costVehiUsageData.getUSBaseBev());
			mapBomLineData.put("US High BEV", costVehiUsageData.getUSHighBEV());
			mapBomLineData.put("Common Part", costVehiUsageData.getCommonPart());
			mapBomLineData.put("Platform Part", costVehiUsageData.getPlatformPart());
			mapBomLineData.put("Vehicle Usage", costVehiUsageData.getVehicleUsageValid());
		}
	}

	private ArrayList<String> splitCondition(String input) {
		ArrayList<String> output = new ArrayList<String>();
		String leftValue = "";
		String operation = "";
		String rightValue = "";
		int idxOfNextWord = 0;
		if (input.indexOf("==") != -1) {
			leftValue = input.substring(idxOfNextWord, input.indexOf("=="));
			rightValue = input.substring(input.indexOf("==") + 2);
			operation = "==";
		} else if (input.indexOf("!=") != -1) {
			leftValue = input.substring(idxOfNextWord, input.indexOf("!="));
			rightValue = input.substring(input.indexOf("!=") + 2);
			operation = "!=";
		} else {
			System.out.println("[BOMREPORT] Invalid condition: " + input);
		}
		output.add(leftValue);
		output.add(operation);
		output.add(rightValue);
		return output;
	}

//	private static void expandBOMLines(LinkedList<TCComponentBOMLine> outBomLines, TCComponentBOMLine rootLine) throws TCException {
//		outBomLines.add(rootLine);
//		if (rootLine.getChildrenCount() > 0) {
//			AIFComponentContext[] aifChilLines = rootLine.getChildren();
//			for (AIFComponentContext aifChilLine : aifChilLines) {
//				expandBOMLines(outBomLines, (TCComponentBOMLine) aifChilLine.getComponent());
//			}
//
//		}
//	}

	private static void expandBOMLines(LinkedList<TCComponentBOMLine> outBomLines, final TCComponentBOMLine rootLine) {
		outBomLines.add(rootLine);
		StructureManagementService structureService = StructureManagementService.getService(session);
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];
		ExpandPSOneLevelResponse levelResp = structureService.expandPSOneLevel(levelInfo, levelPref);
		System.out.println(String.format("%s = %s", "ExpandTime", String.valueOf(count++)));
		if (levelResp.output.length > 0) {
			for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					expandBOMLines(outBomLines, psData.bomLine);
				}
			}
		}
	}

	private static String findAllInProcess(TCComponent[] itemRevs) {
		StringBuilder output = new StringBuilder();
		try {
			for (TCComponent itemRev : itemRevs) {
				TCComponent[] fnd0AllWOrkflows = itemRev.getRelatedComponents("fnd0AllWorkflows");
				for (TCComponent aWf : fnd0AllWOrkflows) {
					String endDate = aWf.getPropertyDisplayableValue("fnd0EndDate");
					if (endDate.isEmpty() || endDate == null) {
						ArrayList<TCComponent> rootTargetECR = Utils.GetSecondaryObjectByRelationName(session, aWf, new String[] { "Vf6_ECRRevision" }, "Fnd0EPMTarget");
						if (rootTargetECR != null && rootTargetECR.size() > 0) {
							for (int i = 0; i < rootTargetECR.size(); ++i) {
								String id = rootTargetECR.get(i).getProperty("item_id");
								if (output.length() > 0)
									output.append(";");
								output.append(id);
							}
						}
						ArrayList<TCComponent> rootTargetECN = Utils.GetSecondaryObjectByRelationName(session, aWf, new String[] { "Vf6_ECRRevision" }, "Fnd0EPMTarget");
						if (rootTargetECN != null && rootTargetECN.size() > 0) {
							for (int i = 0; i < rootTargetECN.size(); ++i) {
								String id = rootTargetECN.get(i).getProperty("item_id");
								if (output.length() > 0)
									output.append(";");
								output.append(id);
							}
						}
						ArrayList<TCComponent> rootTargetERN = Utils.GetSecondaryObjectByRelationName(session, aWf, new String[] { "VF4_ERNRevision" }, "Fnd0EPMTarget");
						if (rootTargetERN != null && rootTargetERN.size() > 0) {
							for (int i = 0; i < rootTargetERN.size(); ++i) {
								String id = rootTargetERN.get(i).getProperty("item_id");
								if (output.length() > 0)
									output.append(";");
								output.append(id);
							}
						}
					}
				}
			}
		} catch (TCException | NotLoadedException e) {
			LOGGER.error("[ERROR] Exception: " + e.toString());
		}
		return output.toString();
	}

	private String getTGSSDocNumber(TCComponentBOMLine bomLine) {
		String output = "";
		try {
			TCComponentItemRevision partRev = bomLine.getItemRevision();
			output = partRev.getPropertyDisplayableValue("vf4_tgss_doc_number");
		} catch (TCException | NotLoadedException e) {
			e.printStackTrace();
			return "";
		}
		return output;
	}

	/* return true if level is 0 or 1 */
	private boolean isTopLine(TCComponentBOMLine bomline) {
		try {
			if (bomline.getIntProperty("bl_level_starting_0") < 2) {
				return true;
			}
		} catch (NumberFormatException | TCException e) {
			e.printStackTrace();
			MessageBox.post("Exception: " + e.toString(), "", MessageBox.ERROR);
			return false;
		}
		return false;
	}

	private boolean isNumber(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}

	}

	private void loadSavedVariants(TCComponentBOMLine bopline, LinkedList<TCComponentBOMLine> allBOMLineInWindow) throws Exception {
		availableSavedVariants = new HashSet<String>();
		ReportDataSourceService rs = ReportDataSourceService.getService(session);
		Get150BomWithVariantsDSInput input = new Get150BomWithVariantsDSInput();
		TCComponentBOMLine parent = bopline;
		TCComponentBOMLine topBop = parent;
		// finding topline
		while (parent != null) {
			try {
				topBop = parent;
				parent = (TCComponentBOMLine) parent.getReferenceProperty("bl_parent");
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		String type = null;

		try {
			TCComponentItem item = topBop.getItem();
			DataManagementService.getService(session).getProperties(new TCComponent[] { item }, new String[] { "object_type" });
			type = item.getType();
		} catch (TCException e2) {
			e2.printStackTrace();
		}

		try {
			input.topItemId = topBop.getProperty("bl_item_item_id");
		} catch (TCException e1) {
			MessageBox.post("Can't find Top BOP. Please contact admin for details ", "Error", MessageBox.ERROR);
			e1.printStackTrace();
		}
		input.toplineTag = bopline;
		input.topItemType = type;
		Get150BomWithVariantsDSResponse response = rs.get150BomWithVariantsDS(new Get150BomWithVariantsDSInput[] { input });
		if (response.serviceData.sizeOfPartialErrors() == 0) {
			Get150BomWithVariantsDSOutput[] outputs = response.outputs;
			if (outputs != null) {
				// get all variants
				if (outputs.length > 0) {
					TCComponent[] allVariantRules = outputs[0].variantRule;
					if (allVariantRules != null && allVariantRules.length != 0) {
						for (TCComponent variantRule : allVariantRules) {
							availableSavedVariants.add(variantRule.toString());
						}
					}
				}

				// get all bomline & there variant infos
				for (Get150BomWithVariantsDSOutput output : outputs) {
					TCComponent[] variantRules = output.variantRule;
					TCComponentBOMLine bopLine = (TCComponentBOMLine) output.bomlineTag;

					HashSet<String> setRule = new HashSet<String>();
					if (bopLine != null) {
						try {
							for (TCComponent variantRule : variantRules) {
								setRule.add(variantRule.toString());
							}
							allBOMLineInWindow.add(bopLine);
							mapBomVariants.put(bopLine, setRule);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private static int count = 0;

}
