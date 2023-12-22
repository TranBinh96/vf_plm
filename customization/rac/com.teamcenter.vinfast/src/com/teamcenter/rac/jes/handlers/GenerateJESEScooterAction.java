package com.teamcenter.rac.jes.handlers;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachment;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsInput;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsResponse;
import com.vf.utils.ImageValidator;
import com.vf.utils.JES_EScooter_Logic;
import com.vf.utils.JES_SafetyGetter;
import com.vf.utils.JES_SymbolGetter;
import com.vf.utils.Query;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GenerateJESEScooterAction extends AbstractHandler {
	/**
	 * The constructor.
	 */
	TCSession session = null;
	BufferedWriter output = null;
	FileReader fr = null;
	String rootBOP_name = "";
	TCComponent rootBOP;
	JES_EScooter_Logic logic = new JES_EScooter_Logic();

	final String TEMP_DIR;

	public GenerateJESEScooterAction() {
		TEMP_DIR = System.getenv("tmp");
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	private boolean validateSelection(InterfaceAIFComponent[] aifComp) throws TCException {
		if (aifComp.length != 1) {
			return false;
		}

		if (!(aifComp[0] instanceof TCComponentBOMLine)) {
			return false;
		}
		
		if (((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("MEOP") == false) {
			return false;
		}

		return true;
	}

	/**
	 * the command has been executed, so extract extract the needed information from
	 * the application context.
	 */

	private ArrayList<TCComponent> whereUsedMEOP(TCComponent itemRev, String Returnobjectype) {
		ArrayList<TCComponent> usedMEOP = new ArrayList<TCComponent>();
		TCComponent[] usedParts = null;
		try {
			usedParts = itemRev.whereUsed(TCComponent.WHERE_USED_ALL);
			for (int i = 0; i < usedParts.length; i++) {

				if (usedParts[i].getTypeComponent().getType().equals(Returnobjectype)) {
					usedMEOP.add(usedParts[i]);
				}
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return usedMEOP;
	}

	public Object execute(ExecutionEvent event) {

		try {
			@SuppressWarnings("unused")
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

			InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();

			boolean is_valid = validateSelection(aifComp) ;
			if (!is_valid)
			{
				MessageBox.post("Selected a wrong object to view JES!\nPlease select an Operation.", "Error", MessageBox.ERROR);
				return null;
			}

			// Assume that selected item is BOMLine
			boolean isValidSelectedObject = false;
			TCComponent opRev = null;
			TCComponentBOMLine operationLine = (TCComponentBOMLine) aifComp[0];
			getparent(operationLine);
			opRev = operationLine.getItemRevision();

			//if (rootBOP_name != null) {
				isValidSelectedObject = true;
				String opeationName = opRev .getPropertyDisplayableValue("object_name");
				opeationName = opeationName.replace(":", "_").replace("/", "_").replace("\\", "_").replace("*", "_")
						.replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");

				generateReport(aifComp[0], TEMP_DIR + "\\" + opeationName, true);
			//}

			if (isValidSelectedObject == false) {
				MessageBox.post("Please open the whole BOP before viewing JES/SOS report to get full information!", "Info",
						MessageBox.WARNING);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post("There are some errors occured, please contact your administrator.", "Error",
					MessageBox.ERROR);
		}
		return null;
	}

	public void generateReport(InterfaceAIFComponent aifComp, String outputFilePathWithoutExtension,
			boolean openAfterDownload) throws Exception {
		TCComponentBOMLine operationLine = (TCComponentBOMLine) aifComp;

		if (rootBOP == null) {
			getparent(operationLine);
		}

		// log_nguyen prepare data
		List<TCComponentFolder> activityComps = new LinkedList<TCComponentFolder>();
		Hashtable<String, String> OperationList = new Hashtable<String, String>();
		Vector<Hashtable<String, String>> equipmentVec = new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> materialVec = new Vector<Hashtable<String, String>>();

		TCComponentItemRevision operationRev = operationLine.getItemRevision();
		prepareData(OperationList, activityComps, equipmentVec, materialVec, operationLine);
		File operationImage = getOperationImage(operationRev);

		if (materialVec.size() < logic.JES_TEMPLATE_MAX_MATERIAL_VEC_SIZE) {
			// log_nguyen populate report
			populateReport(OperationList, materialVec, equipmentVec, operationRev, activityComps, operationImage,
					outputFilePathWithoutExtension, openAfterDownload);
		} else {
			MessageBox.post("Material count must be less than template rows (i.e 40). Please contact system admin.",
					"Error", MessageBox.ERROR);
		}
	}

	private List<TCComponentFolder> prepareData(Hashtable<String, String> OperationList,
			List<TCComponentFolder> activityComps, Vector<Hashtable<String, String>> equipmentVec,
			Vector<Hashtable<String, String>> materialVec, TCComponentBOMLine operationLine) throws Exception {

		TCComponent operationRev = operationLine.getItemRevision();
		String operationRevNumber = operationRev.getProperty("item_revision_id");
		String operationName = operationRev.getProperty("object_name");// + " / " +
																				// operationRev.getProperty("vf5_viet_description");
		String OperationID = operationRev.getProperty("item_id");

		String vietnameseDesc = operationRev.getProperty("vf5_viet_description");
		if (vietnameseDesc != null && vietnameseDesc.length() > 1) {
			operationName = vietnameseDesc;
		}

//		AIFComponentContext[] context = operationLine.getRelated("bl_me_activity_lines") ;
//		TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine)context[0].getComponent() ;
//		String Time = activityLine.getProperty("al_activity_work_time");

		TCComponent jesRev = operationRev.getRelatedComponent(JES_EScooter_Logic.OPREV_JESREV_RELATION);
		AIFComponentContext[] contents = jesRev.getChildren(JES_EScooter_Logic.JESREV_ACTIVITY_PROP);

		double jesTime = 0;
		for (AIFComponentContext content : contents) {
			TCComponentFolder jesActivity = (TCComponentFolder) content.getComponent();
			activityComps.add(jesActivity);
			try {
				double stepTime = jesActivity.getDoubleProperty("vf4_time");
				if (stepTime > 0)
					jesTime += stepTime;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		DecimalFormat df = new DecimalFormat("#.##");
		String analysisTime = df.format(jesTime);

		String stationObjectStr = operationLine.getProperty("bl_formatted_parent_name");

		String Station = "";
		String StationName = "";
		try {
			String[] tempStr = stationObjectStr.split("/");
			Station = tempStr[0];
		} catch (Exception e) {
			Station = stationObjectStr;
		}

		ArrayList<TCComponent> targetWorkstations = whereUsedMEOP(operationRev, "Mfg0MEProcStatnRevision");

		if (targetWorkstations.size() != 0) {
			for (TCComponent targetWorkstation : targetWorkstations) {

				if (targetWorkstation.getProperty("item_id").equals(Station)) {
					StationName = targetWorkstation.getProperty("object_name");
					//StationName += targetWorkstation.getProperty("object_name") + ",";
					break;
				}
			}
			//StationName = StationName.substring(0, StationName.lastIndexOf(","));
		}

		String createdTime = "";
		String modifiedTime = "";
		String modifiedBy = "";

		Date createdDate = jesRev.getDateProperty("creation_date");
		Date jesRevModifiedDate = jesRev.getDateProperty("last_mod_date");
		Date actModifiedDate = jesRev.getDateProperty("last_mod_date");
		Date lastActModifiedDate = actModifiedDate;
//		TCComponent lastAct = rootActivityComp;
//		for ( TCComponentMEActivity actComp : activityComps) {
//			Date actCompDate = actComp.getDateProperty("last_mod_date");
//			if (lastActModifiedDate.compareTo(actCompDate) < 0) {
//				lastActModifiedDate = actCompDate;
//				lastAct = actComp;
//			}
//		}

//		Date lastModifiedDate = null;
//		String lastModifiedUser = null;
		Date lastModifiedDate = jesRevModifiedDate;
		String lastModifiedUser = jesRev.getProperty("last_mod_user");
		;

		SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy");
//		if (lastActModifiedDate.compareTo(opRevModifiedDate) > 0) {
//			lastModifiedDate = lastActModifiedDate;
//			lastModifiedUser = lastAct.getProperty("last_mod_user");
//		} else {
//			lastModifiedDate = opRevModifiedDate;
//			lastModifiedUser = operationRev.getProperty("last_mod_user");
//		}
		modifiedTime = dtf.format(lastModifiedDate);
		modifiedBy = lastModifiedUser;
		createdTime = dtf.format(createdDate);

		OperationList.put("ID", OperationID);
		OperationList.put("Name", operationName);
		String jesStage = jesRev.getPropertyDisplayableValue("vf4_jes_stage");
		OperationList.put("Stage", jesStage);
		OperationList.put("Station", StationName);
		OperationList.put("Time", analysisTime);
		String jesRevID = jesRev.getPropertyDisplayableValue("item_revision_id");
		OperationList.put("JesRevID", jesRevID);
		OperationList.put("TimeCreated", createdTime);
		OperationList.put("TimeMofified", modifiedTime);
		OperationList.put("ModifiedBy", modifiedBy);

		
		return activityComps;
	}

	private File getOperationImage(TCComponentItemRevision operationRev) throws Exception {
		File operationImage = null;
		TCComponent jesRev = operationRev.getRelatedComponent("VF4_JES_Relation");
		AIFComponentContext[] attachedObjects = jesRev.getRelated(JES_EScooter_Logic.JESREV_PICTURE_RELATION);
		ImageValidator imgValidator = new ImageValidator();
		for (AIFComponentContext attachedObject : attachedObjects) {
			TCComponent comp = (TCComponent) attachedObject.getComponent();
			if (comp.getTypeComponent().isTypeOf("Dataset")) {
				TCComponentDataset dataset = (TCComponentDataset) comp;
				TCComponent[] namedRefs = dataset.getNamedReferences();
				for (int i = 0; i < dataset.getNamedReferences().length; i++) {
					TCComponentTcFile tcFile = (TCComponentTcFile) namedRefs[i];
					if (imgValidator.validate(tcFile.toString())) {
						operationImage = tcFile.getFile(TEMP_DIR);
						return operationImage;
					}
				}
			}
		}
		return null;
	}

	private TCComponent getparent(TCComponentBOMLine bom_line_parent2) {
		try {
			TCComponent parent = bom_line_parent2.getReferenceProperty("bl_parent");
			TCComponentBOMLine bom_line_parent = (TCComponentBOMLine) parent;
			if (bom_line_parent != null) {
				if (bom_line_parent.getItem().getType().equals("Mfg0MEPlantBOP") && bom_line_parent.getProperty("bl_level_starting_0").equals("0")) {
//					String rootBOPName = bom_line_parent.getProperty("bl_item_object_name");
//					rootBOP_name = rootBOPName != null && rootBOPName.split("_").length >= 3 ? rootBOPName.split("_")[2] : "";
//					rootBOP = bom_line_parent;
					rootBOP_name = bom_line_parent.getProperty("bl_item_object_name");
				} else {
					getparent(bom_line_parent);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private void populateReport(Hashtable<String, String> operationList, Vector<Hashtable<String, String>> materialVec,
			Vector<Hashtable<String, String>> equipmentVec, TCComponentItemRevision operationRev,
			List<TCComponentFolder> activityComps, File operationImage, String outputFilePathWithoutExtension,
			boolean openAfterDownload) {

		InputStream fileOut = null;
		XSSFWorkbook workbook = null;

		try {
			// NGUYEN
			// log_nguyen search jes template
			File updatefile = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET,
					Query.QUERY_JES_ENTRY_DATASET_NAME, logic.JES_DATASET_TEMPLATE, outputFilePathWithoutExtension);

			fileOut = new FileInputStream(updatefile);
			workbook = new XSSFWorkbook(fileOut);
			XSSFSheet worksheet = workbook.getSheetAt(0);

			// log_nguyen fill data

			// HEADER ROW
			updatefile.setWritable(true);
			if (worksheet.getRow(1) == null) {
			} else {
			}

			// DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

			// ----Created Time Row
			XSSFRow createdTimeRow = null;
			if (worksheet.getRow(1) == null) {
				createdTimeRow = worksheet.createRow(1);
			} else {
				createdTimeRow = worksheet.getRow(1);
			}

			fillHeaderInfo(operationList, workbook, worksheet);

			TCComponent jesRev = operationRev.getRelatedComponent(JES_EScooter_Logic.OPREV_JESREV_RELATION);
			drawSafetyPictures(workbook, jesRev);
			
			if (operationImage != null) {
				drawPicture(workbook, logic.PICTURE_COL_TOP_INDEX, logic.PICTURE_ROW_TOP_INDEX,
						logic.PICTURE_COL_BOTTOM_INDEX, logic.PICTURE_ROW_BOTTOM_INDEX, operationImage);
			}

			fillDetailSteps(activityComps, worksheet);

			fillJESRevisionInfo(operationList, worksheet);

//			if (hasSecondPage) {
//				worksheet.setAutobreaks(false);
//				int secondHeaderRowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + logic.JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE;
//				worksheet.setRowBreak(secondHeaderRowIndex - 1);
//				writeHeader(workbook, worksheet, secondHeaderRowIndex);
//			}

			fileOut.close();
			// FileOutputStream output_file = new FileOutputStream(updatefile) ;
			// NGUYEN create temp path
			// File outFile = new File("d:\\testmy.xlsx");
			FileOutputStream output_file = new FileOutputStream(updatefile);
			workbook.write(output_file);
			output_file.close();
			updatefile.setWritable(false);
			// MessageBox.post("Updated Successfully.", "Success", MessageBox.INFORMATION);
			if (openAfterDownload) {
				Desktop.getDesktop().open(updatefile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post("Please contact Admin. Detail error message is as below.\n" + ex.getMessage(), "ERROR",
					MessageBox.ERROR);
		}
	}

	private void fillJESRevisionInfo(Hashtable<String, String> operationList, XSSFSheet worksheet) {
		XSSFRow operationRow = null;
		if (worksheet.getRow(13) == null) {
			operationRow = worksheet.createRow(13);
		} else {
			operationRow = worksheet.getRow(13);
		}

		XSSFRow jobElementRow = null;
		if (worksheet.getRow(14) == null) {
			jobElementRow = worksheet.createRow(13);
		} else {
			jobElementRow = worksheet.getRow(13);
		}

		XSSFRow stageRow = worksheet.getRow(13);
		XSSFCell stageCell = stageRow.getCell(CellReference.convertColStringToIndex("AD"));
		String jesStage = operationList.get("Stage");
		stageCell.setCellValue(jesStage);
		
		XSSFRow modelRow = worksheet.getRow(5);
		XSSFCell modelCell = modelRow.getCell(CellReference.convertColStringToIndex("AH"));
		modelCell.setCellValue(rootBOP_name);

		XSSFRow modelRowVN = worksheet.getRow(6);
		XSSFCell modelCellVN = modelRowVN.getCell(CellReference.convertColStringToIndex("AH"));
		modelCellVN.setCellValue(rootBOP_name);

		XSSFCell nameCell = jobElementRow.getCell(CellReference.convertColStringToIndex("A"));
		nameCell.setCellValue(operationList.get("Name").toUpperCase());
		// nameCell.setCellStyle(style);

		XSSFCell IDCell = operationRow.getCell(CellReference.convertColStringToIndex("L"));
		IDCell.setCellValue(operationList.get("ID").toUpperCase());
		// IDCell.setCellStyle(style);

		XSSFCell timeCell = operationRow.getCell(CellReference.convertColStringToIndex("S"));
		timeCell.setCellValue(operationList.get("Time").toUpperCase());
		// timeCell.setCellStyle(style);

		XSSFCell stationCell = operationRow.getCell(CellReference.convertColStringToIndex("V"));
		stationCell.setCellValue(operationList.get("Station").toUpperCase());
	}

	private void drawSafetyPictures(XSSFWorkbook workbook, TCComponent jesRev) throws Exception {
		String[] allSafetySymbols = null;
		LOVService lovService = LOVService.getService(session);
		LOVAttachmentsInput lovInput = new LOVAttachmentsInput();

		String jesSafetyStr = jesRev.getProperty("vf4_safety");
		if (jesSafetyStr != null) {
			String[] saftySymbolsArr = jesSafetyStr.split(",");
			List<String> saftySymbols = Arrays.asList(saftySymbolsArr);
			try {
				lovInput.objects = new TCComponent[] { jesRev };
				lovInput.properties = new String[] { "vf4_safety" };

				String[] lov = null;
				LOVAttachmentsResponse response = lovService.getLOVAttachments(new LOVAttachmentsInput[] { lovInput });
				Map<TCComponent, LOVAttachment[]> lovValues = response.lovAttachments;
				Set<TCComponent> keys = lovValues.keySet();
				for (TCComponent key : keys) {
					System.out.println(key);
					LOVAttachment[] lovs = lovValues.get(key);
					TCComponentListOfValues listOfValues = lovs[0].lov;
					ListOfValuesInfo valuesInfo = listOfValues.getListOfValues();
					allSafetySymbols = valuesInfo.getStringListOfValues();
				}
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// TODO Auto-generated catch block
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allSafetySymbols = new String[] { "Glasses", "Hard Face Mask"};
			String[] columnsFirst = new String[] { "AQ", "AU", "BA", "BG", "BK", "BN", "BQ", "BU" };
			String[] columnsSecond = new String[] { "AR", "AV", "BB", "BH", "BL", "BO", "BR", "BV" };
			int jesSafetyRow = 15;
			JES_SafetyGetter safetyGetter = new JES_SafetyGetter();
			for (int i = 0; i < allSafetySymbols.length; i++) {
				boolean isTicked = saftySymbols.contains(allSafetySymbols[i]);
				File pict = safetyGetter.getImage(session, allSafetySymbols[i], isTicked);
				drawPicture(workbook, columnsFirst[i], jesSafetyRow, columnsSecond[i], jesSafetyRow + 1, pict);
			}
		}
	}

	private void fillHeaderInfo(Hashtable<String, String> operationList, XSSFWorkbook workbook, XSSFSheet worksheet) {
		// -------Modified Date ROW
		XSSFRow headerRow1 = null;
		if (worksheet.getRow(1) == null) {
			headerRow1 = worksheet.createRow(1);
		} else {
			headerRow1 = worksheet.getRow(1);
		}

		XSSFRow headerRow2 = null;
		if (worksheet.getRow(2) == null) {
			headerRow2 = worksheet.createRow(2);
		} else {
			headerRow2 = worksheet.getRow(2);
		}

		XSSFCell datePreparedEN = null;
		datePreparedEN = headerRow1.getCell(CellReference.convertColStringToIndex("BG"));
		if (datePreparedEN == null) {
			datePreparedEN = headerRow1.createCell(CellReference.convertColStringToIndex("BG"));
		}
		datePreparedEN.setCellValue(operationList.get("TimeMofified"));
		XSSFCell datePreparedVN = null;
		datePreparedVN = headerRow2.getCell(CellReference.convertColStringToIndex("BG"));
		if (datePreparedVN == null) {
			datePreparedVN = headerRow2.createCell(CellReference.convertColStringToIndex("BG"));
		}
		datePreparedVN.setCellValue(operationList.get("TimeMofified"));

		XSSFCell prepareByEN = null;
		prepareByEN = headerRow1.getCell(CellReference.convertColStringToIndex("BP"));
		if (prepareByEN == null) {
			prepareByEN = headerRow1.createCell(CellReference.convertColStringToIndex("BP"));
		}
		prepareByEN.setCellValue(operationList.get("ModifiedBy"));
		XSSFCell prepareByVN = null;
		prepareByVN = headerRow2.getCell(CellReference.convertColStringToIndex("BP"));
		if (prepareByVN == null) {
			prepareByVN = headerRow2.createCell(CellReference.convertColStringToIndex("BP"));
		}
		prepareByVN.setCellValue(operationList.get("ModifiedBy"));

		XSSFCell jesRevIdEN = null;
		jesRevIdEN = headerRow1.getCell(CellReference.convertColStringToIndex("BW"));
		if (jesRevIdEN == null) {
			jesRevIdEN = headerRow1.createCell(CellReference.convertColStringToIndex("BW"));
		}
		jesRevIdEN.setCellValue(operationList.get("JesRevID"));
		XSSFCell jesRevIdVN = null;
		jesRevIdVN = headerRow2.getCell(CellReference.convertColStringToIndex("BW"));
		if (jesRevIdVN == null) {
			jesRevIdVN = headerRow2.createCell(CellReference.convertColStringToIndex("BW"));
		}
		jesRevIdVN.setCellValue(operationList.get("JesRevID"));

		// thanhpn6 add revision number
		// updateRevNumber(workbook, worksheet, operationList.get("RevisionID"));
		// thanhpn6 finish add revision number
	}

	private XSSFCellStyle createStepCellStyle(XSSFWorkbook workbook) {
		XSSFCellStyle border = workbook.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		cellStyle.cloneStyleFrom(border);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void fillDetailSteps(List<TCComponentFolder> activityComps, XSSFSheet worksheet) throws Exception {
		int detailStepRowIndex = logic.DETAIL_STEPS_ROW_INDEX;
		int i = 1;
		for (TCComponentFolder activityComp : activityComps) {
			XSSFRow detailStepsRow = null;
			if (worksheet.getRow(detailStepRowIndex) == null) {
				detailStepsRow = worksheet.createRow(detailStepRowIndex);
			} else {
				detailStepsRow = worksheet.getRow(detailStepRowIndex);
			}

			XSSFCell detailStepsCell_No = detailStepsRow
					.getCell(CellReference.convertColStringToIndex(logic.DETAIL_STEPS_COL_INDEX_NO));
			XSSFCell detailStepsCell_Desc = detailStepsRow
					.getCell(CellReference.convertColStringToIndex(logic.DETAIL_STEPS_COL_INDEX_DESC));
			XSSFCell detailStepsCell_Detail = detailStepsRow.getCell(CellReference.convertColStringToIndex("AL"));
			XSSFCell detailStepsCell_ReqSpec = detailStepsRow.getCell(CellReference.convertColStringToIndex("BM"));
			XSSFCell detailStepsCell_Material = detailStepsRow.getCell(CellReference.convertColStringToIndex("BP"));
			XSSFCell detailStepsCell_Time = detailStepsRow.getCell(CellReference.convertColStringToIndex("BU"));
			XSSFCell detailStepsCell_Tool = detailStepsRow.getCell(CellReference.convertColStringToIndex("BH"));
			// String stepNo = activityComp.getProperty(logic.DETAIL_PROP_NAME_STEP_NO);
			String stepNo = String.valueOf(i);
			String stepDesc = activityComp.getProperty("vf4_step_desc_en");
			String stepDetail = activityComp.getProperty("vf4_detail_step_desc");
			String stepRequirementSpec = activityComp.getProperty("vf4_step_reference");
			String stepMaterial = activityComp.getProperty("vf4_material");
			String stepTime = activityComp.getProperty("vf4_time");
			if (stepTime != null && stepTime.isEmpty() == false) {
				DecimalFormat df = new DecimalFormat("#.##");
				String analysisTime = df.format(Double.valueOf(stepTime));
				stepTime = analysisTime;
			}

			String stepTool = activityComp.getProperty("vf4_tool");
			String stepSymbolStr = activityComp.getProperty("vf4_detail_step_symbols");
			String[] stepSymbols = stepSymbolStr != null ? stepSymbolStr.split(",") : new String[0];
			System.out.println("");
			detailStepsCell_No.setCellValue(stepNo);
			detailStepsCell_Desc.setCellValue(stepDesc);
			detailStepsCell_Detail.setCellValue(stepDetail);
			detailStepsCell_ReqSpec.setCellValue(stepRequirementSpec);
			detailStepsCell_Material.setCellValue(stepMaterial);
			detailStepsCell_Time.setCellValue(stepTime);
			detailStepsCell_Tool.setCellValue(stepTool);
			JES_SymbolGetter symbolGetter = new JES_SymbolGetter();

			int stepSymbolsIndex = 0;
			if (stepSymbolsIndex < stepSymbols.length) {
				File symbolImage = symbolGetter.getImage(session, stepSymbols[stepSymbolsIndex++]);
				if (symbolImage != null) {
					drawPicture(worksheet.getWorkbook(), "U", detailStepRowIndex, "V", detailStepRowIndex + 1,
							symbolImage);
				} else {
					// "CANNOT find symbol images or it's empty.
				}
			}

			if (stepSymbolsIndex < stepSymbols.length) {
				File symbolImage = symbolGetter.getImage(session, stepSymbols[stepSymbolsIndex++]);
				if (symbolImage != null) {
					drawPicture(worksheet.getWorkbook(), "T", detailStepRowIndex, "U", detailStepRowIndex + 1,
							symbolImage);
				} else {
					// "CANNOT find symbol images or it's empty.
				}
			}

			detailStepRowIndex++;
			if (stepSymbolsIndex < stepSymbols.length) {
				File symbolImage = symbolGetter.getImage(session, stepSymbols[stepSymbolsIndex++]);
				if (symbolImage != null) {
					drawPicture(worksheet.getWorkbook(), "U", detailStepRowIndex, "V", detailStepRowIndex + 1,
							symbolImage);
				} else {
					// "CANNOT find symbol images or it's empty.
				}
			}
			if (stepSymbolsIndex < stepSymbols.length) {
				File symbolImage = symbolGetter.getImage(session, stepSymbols[stepSymbolsIndex++]);
				if (symbolImage != null) {
					drawPicture(worksheet.getWorkbook(), "T", detailStepRowIndex, "U", detailStepRowIndex + 1,
							symbolImage);
				} else {
					// "CANNOT find symbol images or it's empty.
				}
			}
			detailStepRowIndex++;

			i++;
		}
	}

	private void fillPictureDetailSteps(XSSFWorkbook workbook, int detailStepRowIndex, String stepSymbol) {
		// TODO Auto-generated method stub
		List<XSSFPictureData> allPictures = workbook.getAllPictures();
		for (XSSFPictureData picture : allPictures) {

			System.out.println(picture.toString());
		}
	}

	private void writeHeader(XSSFWorkbook workbook, XSSFSheet sheet, int headerRowIndex) {
		XSSFRow row = sheet.createRow(headerRowIndex);
		// Create Style border
		XSSFCellStyle Header = logic.createCellStyleForPartDataHeader(workbook);
		// Step
		XSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(Header);
		cell.setCellValue("STEP");
		// Part number
		for (int i = 1; i <= 3; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("PART N#");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 1, 3));
		// Partname
		for (int i = 4; i <= 8; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("PART NAME");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 4, 8));
		// Qty
		cell = row.createCell(9, CellType.NUMERIC);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellStyle(Header);
		cell.setCellValue("QTY");
		// TORQUE
		for (int i = 10; i <= 13; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("TORQUE");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 10, 13));
		// Production tool
		for (int i = 14; i <= 18; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("TOOLS/CONSUMABLES");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 14, 18));

		// Backup tool
		for (int i = 19; i <= 22; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("BACKUP TOOL");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 19, 22));

	}

	public File updateJES(TCComponentItemRevision revObj) {

		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("IMAN_specification");
			for (int i = 0; i < dataset.length; i++) {

				if (dataset[i].getType().equals("MSExcelX")) {
					excel = (TCComponentDataset) dataset[i];
					break;
				}
			}
			if (excel != null) {
				TCComponent[] namedRef = excel.getNamedReferences();
				TCComponentTcFile file = (TCComponentTcFile) namedRef[0];
				excelFile = file.getFmsFile();
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return excelFile;
	}

	private void updateRevNumber(XSSFWorkbook workbook, XSSFSheet worksheet, String revNum) {
		if (!(isMergedCell(worksheet, 2, 3, CellReference.convertColStringToIndex("N"),
				CellReference.convertColStringToIndex("N")))) {
			// set border first time
			String setBorderRange = "$N$2:$N$4";
			setBordersToMergedCells(worksheet, CellRangeAddress.valueOf(setBorderRange));

			setMerge(worksheet, 2, 3, CellReference.convertColStringToIndex("N"),
					CellReference.convertColStringToIndex("N"));
			XSSFRow revRow = null;
			XSSFRow titleRow = null;
			XSSFCellStyle cellStyle = createCellStyle(workbook);
			if (worksheet.getRow(1) == null) {
				titleRow = worksheet.createRow(1);
			} else {
				titleRow = worksheet.getRow(1);
			}
			XSSFCell revTitleCell = null;
			revTitleCell = titleRow.getCell(CellReference.convertColStringToIndex("N"));
			revTitleCell.setCellValue("REV");
			revTitleCell.setCellStyle(cellStyle);
			if (worksheet.getRow(2) == null) {
				revRow = worksheet.createRow(2);
			} else {
				revRow = worksheet.getRow(2);
			}
			XSSFCell revValueCell = null;
			revValueCell = revRow.getCell(CellReference.convertColStringToIndex("N"));
			if (revValueCell == null) {
				revValueCell = revRow.createCell(CellReference.convertColStringToIndex("N"));
			}
			revValueCell.setCellValue(revNum);
			revValueCell.setCellStyle(cellStyle);
		} else {
			XSSFRow revRow = null;
			if (worksheet.getRow(2) == null) {
				revRow = worksheet.createRow(2);
			} else {
				revRow = worksheet.getRow(2);
			}
			XSSFCell revCell = null;
			revCell = revRow.getCell(CellReference.convertColStringToIndex("N"));
			if (revCell == null) {
				revCell = revRow.createCell(CellReference.convertColStringToIndex("N"));
			}
			revCell.setCellValue(revNum);
		}
	}

	private boolean isMergedCell(XSSFSheet worksheet, int numRow, int untilRow, int numCol, int untilCol) {
		System.out.println("num merged reagons: " + worksheet.getNumMergedRegions());
		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			CellRangeAddress merge = worksheet.getMergedRegion(i);
			System.out.println("Number:" + i + " " + merge.getFirstRow() + " " + merge.getLastRow() + " "
					+ merge.getFirstColumn() + " " + merge.getLastColumn());
			if (numRow == merge.getFirstRow() && untilRow == merge.getLastRow() && numCol == merge.getFirstColumn()
					&& untilCol == merge.getLastColumn()) {
				return true;
			}
		}
		return false;
	}

	protected void setMerge(XSSFSheet sheet, int numRow, int untilRow, int numCol, int untilCol) {
		CellRangeAddress cellMerge = new CellRangeAddress(numRow, untilRow, numCol, untilCol);
		sheet.addMergedRegion(cellMerge);
	}

	protected void setBordersToMergedCells(XSSFSheet sheet, CellRangeAddress rangeAddress) {
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
	}

	private XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();

		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Calibri");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);

		// need set border twice time to make sure all borders were set
		cellStyle.setBorderTop(BorderStyle.MEDIUM);
		cellStyle.setBorderRight(BorderStyle.MEDIUM);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		cellStyle.setBorderLeft(BorderStyle.MEDIUM);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void drawPicture(XSSFWorkbook workbook, String col1, int row1, String col2, int row2, File pict)
			throws FileNotFoundException, IOException {
		InputStream InputPic = new FileInputStream(pict);
		byte[] bytes = IOUtils.toByteArray(InputPic);
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG | Workbook.PICTURE_TYPE_PNG);
		InputPic.close();
		CreationHelper helper = workbook.getCreationHelper();
		Drawing<?> drawing = workbook.getSheetAt(0).createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(CellReference.convertColStringToIndex(col1));
		anchor.setRow1(row1);
		anchor.setCol2(CellReference.convertColStringToIndex(col2));
		anchor.setRow2(row2);
		drawing.createPicture(anchor, pictureIdx);
	}
}
