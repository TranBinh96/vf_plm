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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
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
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.JES_EScooter_Logic;
import com.vf.utils.JES_SafetyGetter;
import com.vf.utils.Query;

public class GenerateSOSEScooterAction extends AbstractHandler {
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

	public GenerateSOSEScooterAction() {
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
		
		if (((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("Mfg0MEProcStatn") == false) {
			return false;
		}


		return true;
	}

	/**
	 * the command has been executed, so extract extract the needed information from
	 * the application context.
	s */
	public Object execute(ExecutionEvent event) {

		try {
			@SuppressWarnings("unused")
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

			InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();

			boolean is_valid = validateSelection(aifComp) ;
			if (!is_valid)
			{
				MessageBox.post("Selected a wrong object to view SOS!\nPlease select a Station.", "Error", MessageBox.ERROR);
				return null;
			}

			// Assume that selected item is BOMLine
			boolean isValidSelectedObject = false;
			TCComponent stationRev = null;
			TCComponentBOMLine stationLine = (TCComponentBOMLine) aifComp[0];
			getparent(stationLine);
			stationRev = stationLine.getItemRevision();

			//if (rootBOP_name != null) {
				isValidSelectedObject = true;
				String stationName = stationRev .getPropertyDisplayableValue("object_name");
				stationName = stationName.replace(":", "_").replace("/", "_").replace("\\", "_").replace("*", "_")
						.replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");

				generateReport(aifComp[0], TEMP_DIR + "\\" + stationName, true);
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
		TCComponentBOMLine stationLine = (TCComponentBOMLine) aifComp;

		if (rootBOP == null) {
			getparent(stationLine);
		}

		Map<TCComponent, File> opsImage = new HashMap<TCComponent, File>();
		Map<TCComponent, Map<String, String>> jesesData = new HashMap<TCComponent, Map<String, String>>();
		Hashtable<String, String> sosData = new Hashtable<String, String>();

		TCComponentItemRevision stationRev = stationLine.getItemRevision();
		prepareData(sosData, jesesData, stationLine);

		if (jesesData.keySet().size() < logic.SOS_TEMPLATE_MAX_OP_SIZE) {
			populateReport(stationRev, sosData, jesesData, opsImage, outputFilePathWithoutExtension, openAfterDownload);
		} else {
			MessageBox.post("Operation count must be less than " + logic.SOS_TEMPLATE_MAX_OP_SIZE + "in a station. Please contact system admin.",
					"Error", MessageBox.ERROR);
		}
	}

	private void populateReport(TCComponentItemRevision stationRev, Hashtable<String, String> sosData,
			Map<TCComponent, Map<String, String>> jesesData, Map<TCComponent, File> opsImage,
			String outputFilePathWithoutExtension, boolean openAfterDownload) {
		InputStream fileOut = null;
		XSSFWorkbook workbook = null;

		try {
			File updatefile = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET,
					Query.QUERY_JES_ENTRY_DATASET_NAME, logic.SOS_DATASET_TEMPLATE, outputFilePathWithoutExtension);

			fileOut = new FileInputStream(updatefile);
			workbook = new XSSFWorkbook(fileOut);
			XSSFSheet worksheet = workbook.getSheetAt(0);
			fillSOSInfo(sosData, worksheet);//TODO: fill sos  data
			
			fillJESesInfo(stationRev, jesesData, opsImage, worksheet);
			
			
			
			
			fileOut.close();
			FileOutputStream output_file = new FileOutputStream(updatefile);
			workbook.write(output_file);
			output_file.close();
			updatefile.setWritable(false);
			if (openAfterDownload) {
				Desktop.getDesktop().open(updatefile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post("Please contact Admin. Detail error message is as below.\n" + ex.getMessage(), "ERROR",
					MessageBox.ERROR);
		}
	}

	private void fillJESesInfo(TCComponentItemRevision stationRev, Map<TCComponent, Map<String, String>> jesesData,
			Map<TCComponent, File> opsImage, XSSFSheet worksheet) throws Exception {
		int opRow = 16;
		for (Entry<TCComponent, Map<String, String>> jesEntry : jesesData.entrySet()) {
			TCComponent operationRev = jesEntry.getKey();
			Map<String, String> jesData = jesEntry.getValue();
			
			String operationName = jesData.get("operationName");
			String operationID = jesData.get("operationID");
			
			
			worksheet.getRow(opRow).getCell(CellReference.convertColStringToIndex("F")).setCellValue(operationName);
			worksheet.getRow(opRow).getCell(CellReference.convertColStringToIndex("AN")).setCellValue(operationID);
			
			
			TCComponent jesRev = operationRev.getRelatedComponent(JES_EScooter_Logic.OPREV_JESREV_RELATION);
			
			
			if (jesRev != null) {
				String time = jesData.get("workingTime");
				worksheet.getRow(opRow).getCell(CellReference.convertColStringToIndex("AP")).setCellValue(time);
				drawSafetyPictures(worksheet, jesRev, opRow);	
			}
			
			opRow += 2;
		}
	}

	private void fillSOSInfo(Hashtable<String, String> sosData, XSSFSheet worksheet) {
		String vehicleType = sosData.get("vehicleType");
		String shopName = sosData.get("shopName");
		String datePrepared = sosData.get("datePrepared");
		String preparedBy = sosData.get("preparedBy");
		String stationName = sosData.get("stationName");
		String location = sosData.get("stationID");
		
		worksheet.getRow(3).getCell(CellReference.convertColStringToIndex("AM")).setCellValue(vehicleType);
		worksheet.getRow(4).getCell(CellReference.convertColStringToIndex("AM")).setCellValue(vehicleType);
		
		worksheet.getRow(5).getCell(CellReference.convertColStringToIndex("AM")).setCellValue(shopName);
		
		worksheet.getRow(1).getCell(CellReference.convertColStringToIndex("AW")).setCellValue(datePrepared);
		worksheet.getRow(0).getCell(CellReference.convertColStringToIndex("AW")).setCellValue(datePrepared);
		
		
		worksheet.getRow(0).getCell(CellReference.convertColStringToIndex("BB")).setCellValue(preparedBy);
		worksheet.getRow(1).getCell(CellReference.convertColStringToIndex("BB")).setCellValue(preparedBy);
		
		
		worksheet.getRow(13).getCell(CellReference.convertColStringToIndex("F")).setCellValue(stationName);
		worksheet.getRow(13).getCell(CellReference.convertColStringToIndex("AN")).setCellValue(location);
		
		//worksheet.getRow(XXX).getCell(CellReference.convertColStringToIndex("XXX")).setCellValue(vehicleType);
	}

	private void prepareData(Hashtable<String, String> sosData, Map<TCComponent, Map<String, String>> jesData,
			TCComponentBOMLine stationLine) throws NotLoadedException, TCException {
		TCComponent stationRev = stationLine.getItemRevision();
		
		SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy");
		
		// prepare sos data
		String vehicleType = rootBOP_name;
		String shopName = rootBOP_name;
		String datePrepared = dtf.format(new Date());
		String preparedBy = session.getUserName();
		String stationName = stationRev.getPropertyDisplayableValue("object_name");
		String stationID = stationRev.getPropertyDisplayableValue("item_id");
		
		sosData.put("vehicleType", vehicleType);
		sosData.put("shopName", shopName);
		sosData.put("datePrepared", datePrepared);
		sosData.put("preparedBy", preparedBy);
		sosData.put("stationName", stationName);
		sosData.put("stationID", stationID);
		
		//prepare op data
		
		DecimalFormat df = new DecimalFormat("#.##");
		for (AIFComponentContext stationChildAif : stationLine.getChildren()) {
			TCComponent stationChild = (TCComponent) stationChildAif.getComponent();
			if (stationChild.getType().equals("BOMLine") && ((TCComponentBOMLine)stationChild).getItemRevision().getType().equals("MEOPRevision")) {
				TCComponent opRev = ((TCComponentBOMLine)stationChild).getItemRevision();
				TCComponent jesRev = opRev.getRelatedComponent(JES_EScooter_Logic.OPREV_JESREV_RELATION);
				Map<String, String> jesDataMap = new HashMap<String, String>();
				jesDataMap.put("operationName", opRev.getPropertyDisplayableValue("object_name"));
				jesDataMap.put("operationID", opRev.getPropertyDisplayableValue("item_id"));
				
				if (jesRev != null) {
					String workingTimeStr = jesRev.getPropertyDisplayableValue("vf4_jes_working_time");
					jesDataMap.put("workingTime", (workingTimeStr != null && workingTimeStr.length() > 0) ? df.format(Double.valueOf(jesRev.getPropertyDisplayableValue("vf4_jes_working_time"))) : "" );	
				}
				
				jesData.put(opRev, jesDataMap);
			}
		}
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

	private void drawSafetyPictures(XSSFSheet worksheet, TCComponent jesRev, int jesSafetyRow) throws Exception {
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

				LOVAttachmentsResponse response = lovService.getLOVAttachments(new LOVAttachmentsInput[] { lovInput });
				@SuppressWarnings("unchecked")
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
			String[] columnsFirst = new String[] { "BF", "BG" };
			String[] columnsSecond = new String[] { "BG", "BH" };
			
			JES_SafetyGetter safetyGetter = new JES_SafetyGetter();
			for (int i = 0; i < allSafetySymbols.length; i++) {
				boolean isTicked = saftySymbols.contains(allSafetySymbols[i]);
				File pict = safetyGetter.getImage(session, allSafetySymbols[i], isTicked);
				drawPicture(worksheet, columnsFirst[i], jesSafetyRow, columnsSecond[i], jesSafetyRow + 2, pict);
			}
		}
	}

	public File updateJES(TCComponentItemRevision revObj) {

		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("VF4_SOS_Relation");
			if (dataset == null || dataset.length == 0) {
				dataset = revObj.getRelatedComponents("IMAN_specification");
			}
			
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
	
	private void drawPicture(XSSFSheet worksheet, String col1, int row1, String col2, int row2, File pict)
			throws FileNotFoundException, IOException {
		InputStream InputPic = new FileInputStream(pict);
		byte[] bytes = IOUtils.toByteArray(InputPic);
		XSSFWorkbook workbook = worksheet.getWorkbook();
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG | Workbook.PICTURE_TYPE_PNG);
		InputPic.close();
		CreationHelper helper = workbook.getCreationHelper();
		Drawing<?> drawing = worksheet.createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(CellReference.convertColStringToIndex(col1));
		anchor.setRow1(row1);
		anchor.setCol2(CellReference.convertColStringToIndex(col2));
		anchor.setRow2(row2);
		drawing.createPicture(anchor, pictureIdx);
	}

}
