package com.teamcenter.vinfast.sor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.ExcelExtension;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SORDocumentGenerate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponent selectedObject;

	public SORDocumentGenerate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		selectedObject = (TCComponent) targetComp[0];
		exportExcelTemplate();

		return null;
	}

	private void exportExcelTemplate() {
		try {
			boolean isNewFile = false;
			File templateFile = getECRExcelFile();
			if (templateFile == null) {
				isNewFile = true;
				templateFile = downloadTemplateFile();
			}

			if (templateFile == null)
				MessageBox.post("Can not get SOR Document Template file. Please contact your administrator for further instructions with below messages.", "Export SOR Information failed", MessageBox.ERROR);

			InputStream outputFile = new FileInputStream(templateFile);
			XSSFWorkbook workbook = new XSSFWorkbook(outputFile);
			templateFile.setWritable(true);

			if (!isNewFile) {
				int ecrSummarySheetIndex = workbook.getSheetIndex("Smart SOR");
				if (ecrSummarySheetIndex >= 0)
					workbook.removeSheetAt(ecrSummarySheetIndex);
			}

			int ecrSummaryTempSheetIndex = workbook.getSheetIndex("Smart SOR Template");
			workbook.cloneSheet(ecrSummaryTempSheetIndex);
			workbook.setSheetName(workbook.getNumberOfSheets() - 1, "Smart SOR");
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				if (i == workbook.getNumberOfSheets() - 1)
					workbook.getSheetAt(i).setSelected(true);
				else
					workbook.getSheetAt(i).setSelected(false);
			}
			workbook.setActiveSheet(workbook.getNumberOfSheets() - 1);

			XSSFSheet worksheet = workbook.getSheet("Smart SOR");
//			addWaterMark(worksheet);

			// -------------------------------- General
			updateCellValue(worksheet, "D2", selectedObject.getPropertyDisplayableValue("item_id"));
			updateCellValue(worksheet, "D3", selectedObject.getPropertyDisplayableValue("owning_user ")); // Creator name
			updateCellValue(worksheet, "D4", selectedObject.getPropertyDisplayableValue("vf4_reviewer_name")); // Reviewer name
			updateCellValue(worksheet, "D5", selectedObject.getPropertyDisplayableValue("vf4_approver_name")); // Approver name
			updateCellValue(worksheet, "D6", selectedObject.getPropertyDisplayableValue("vf4_change_desription_date")); // Change desc / date
			updateCellValue(worksheet, "D7", selectedObject.getPropertyDisplayableValue("vf4_markets")); //
			updateCellValue(worksheet, "D8", selectedObject.getPropertyDisplayableValue("vf4_production_volume")); //
			updateCellValue(worksheet, "D9", selectedObject.getPropertyDisplayableValue("vf4_build_plan")); //

			// -------------------------------- Contact
			updateCellValue(worksheet, "E11", selectedObject.getPropertyDisplayableValue("vf4_commodity_buyer_name")); //
			updateCellValue(worksheet, "F11", selectedObject.getPropertyDisplayableValue("vf4_commodit_bbuyer_email")); //
			updateCellValue(worksheet, "G11", selectedObject.getPropertyDisplayableValue("vf4_commodity_buyer_phone")); //
			updateCellValue(worksheet, "E12", selectedObject.getPropertyDisplayableValue("vf4_pur_com_manager_name")); //
			updateCellValue(worksheet, "F12", selectedObject.getPropertyDisplayableValue("vf4_pur_com_manager_email")); //
			updateCellValue(worksheet, "G12", selectedObject.getPropertyDisplayableValue("vf4_pur_com_manager_phone")); //
			updateCellValue(worksheet, "E13", selectedObject.getPropertyDisplayableValue("vf4_commodity_engineer_name")); //
			updateCellValue(worksheet, "F13", selectedObject.getPropertyDisplayableValue("vf4_commodity_eng_email")); //
			updateCellValue(worksheet, "G13", selectedObject.getPropertyDisplayableValue("vf4_commodity_eng_phone")); //
			updateCellValue(worksheet, "E14", selectedObject.getPropertyDisplayableValue("vf4_com_eng_manager_name")); //
			updateCellValue(worksheet, "F14", selectedObject.getPropertyDisplayableValue("vf4_com_eng_manager_email")); //
			updateCellValue(worksheet, "G14", selectedObject.getPropertyDisplayableValue("vf4_com_eng_manager_phone")); //
			updateCellValue(worksheet, "E15", selectedObject.getPropertyDisplayableValue("vf4_sqe_site_engineer_name")); //
			updateCellValue(worksheet, "F15", selectedObject.getPropertyDisplayableValue("vf4_sqe_site_engineer_email")); //
			updateCellValue(worksheet, "G15", selectedObject.getPropertyDisplayableValue("vf4_sqe_site_engineer_phone")); //
			updateCellValue(worksheet, "E16", selectedObject.getPropertyDisplayableValue("vf4_sqe_manager_name")); //
			updateCellValue(worksheet, "F16", selectedObject.getPropertyDisplayableValue("vf4_sqe_manager_email")); //
			updateCellValue(worksheet, "G16", selectedObject.getPropertyDisplayableValue("vf4_sqe_manager_phone")); //
			updateCellValue(worksheet, "E17", selectedObject.getPropertyDisplayableValue("vf4_scm_analyst_name")); //
			updateCellValue(worksheet, "F17", selectedObject.getPropertyDisplayableValue("vf4_scm_analyst_email")); //
			updateCellValue(worksheet, "G17", selectedObject.getPropertyDisplayableValue("vf4_scm_analyst_phone")); //
			updateCellValue(worksheet, "E18", selectedObject.getPropertyDisplayableValue("vf4_packaging_analyst_name")); //
			updateCellValue(worksheet, "F18", selectedObject.getPropertyDisplayableValue("vf4_packaging_analyst_email")); //
			updateCellValue(worksheet, "G18", selectedObject.getPropertyDisplayableValue("vf4_packaging_analyst_phone")); //
			updateCellValue(worksheet, "E19", selectedObject.getPropertyDisplayableValue("vf4_scm_manager_name")); //
			updateCellValue(worksheet, "F19", selectedObject.getPropertyDisplayableValue("vf4_scm_manager_email")); //
			updateCellValue(worksheet, "G19", selectedObject.getPropertyDisplayableValue("vf4_scm_manager_phone")); //

			updateCellValue(worksheet, "D20", selectedObject.getPropertyDisplayableValue("vf4_Data_management"));// Project/Program
			updateCellValue(worksheet, "D21", selectedObject.getPropertyDisplayableValue("vf4_compliance"));// Project/Program
			updateCellValue(worksheet, "D22", selectedObject.getPropertyDisplayableValue("vf4_rasic_and_deliverables"));// Project/Program

			outputFile.close();

			FileOutputStream outputStream = new FileOutputStream(templateFile);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

			if (isNewFile) {
				createDataset(templateFile.getPath());
				templateFile.delete();
			}
			MessageBox.post("SOR Document update success.", "Export ECR Information", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post("SOR Document update unsuccess. Exception: " + e.toString(), "Export SOR Information", MessageBox.ERROR);
		}
	}

	private File downloadTemplateFile() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", "SOR_Document_Template");
		parameter.put("Dataset Type", "MS ExcelX");
		parameter.put("Owning Group", "dba");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0)
			return TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", getNameSORDocument() + ".xlsx", session);

		return null;
	}

	private void updateCellValue(XSSFSheet worksheet, String cellAddress, String value) {
		XSSFCell cell = ExcelExtension.getCellByAddress(cellAddress, worksheet);
		cell.setCellValue(value);
	}

	private void updateCellValue(XSSFSheet worksheet, String cellAddress, Double value) {
		if (value == null)
			return;

		XSSFCell cell = ExcelExtension.getCellByAddress(cellAddress, worksheet);
		cell.setCellValue(value);
	}

	private boolean checkBooleanValue(String input) {
		if (input.compareToIgnoreCase("True") == 0 || input.compareToIgnoreCase("1") == 0)
			return true;
		return false;
	}

	private Double getDecimal(String input) {
		if (StringExtension.isDouble(input))
			return Double.parseDouble(input);
		return null;
	}

	private void importImage(XSSFWorkbook workbook, String col1, int row1, String col2, int row2, File pict) {
		try {
			InputStream InputPic = new FileInputStream(pict);
			byte[] bytes = IOUtils.toByteArray(InputPic);
			int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG | Workbook.PICTURE_TYPE_PNG);
			InputPic.close();
			CreationHelper helper = workbook.getCreationHelper();
			Drawing<?> drawing = workbook.getSheet("ECR Summary").getDrawingPatriarch();
			if (drawing == null)
				drawing = workbook.getSheet("ECR Summary").createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(CellReference.convertColStringToIndex(col1));
			anchor.setRow1(row1);
			anchor.setCol2(CellReference.convertColStringToIndex(col2));
			anchor.setRow2(row2);
			Picture pic = drawing.createPicture(anchor, pictureIdx);
			pic.resize(0.99);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getECRImage() throws Exception {
		File operationImage = null;
		AIFComponentContext[] attachedObjects = selectedObject.getRelated("TC_Attaches");
		for (AIFComponentContext attachedObject : attachedObjects) {
			TCComponent comp = (TCComponent) attachedObject.getComponent();
			if (comp.getTypeComponent().isTypeOf("Dataset")) {
				TCComponentDataset dataset = (TCComponentDataset) comp;
				TCComponent[] namedRefs = dataset.getNamedReferences();
				for (int i = 0; i < dataset.getNamedReferences().length; i++) {
					TCComponentTcFile tcFile = (TCComponentTcFile) namedRefs[i];
					if (tcFile.toString().toLowerCase().contains(".jpg") || tcFile.toString().toLowerCase().contains(".png")) {
						operationImage = tcFile.getFile(System.getenv("tmp"));
						return operationImage;
					}
				}
			}
		}
		return null;
	}

	private File getECRExcelFile() {
		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			String ecrDocumentName = getNameSORDocument();
			TCComponent[] datasets = selectedObject.getRelatedComponents("CMReferences");
			for (TCComponent dataset : datasets) {
				if (dataset.getType().equals("MSExcelX") && ecrDocumentName.compareTo(dataset.toString()) == 0) {
					excel = (TCComponentDataset) dataset;
					break;
				}
			}

			if (excel != null) {
				TCComponent[] namedRef = excel.getNamedReferences();
				TCComponentTcFile file = (TCComponentTcFile) namedRef[0];
				excelFile = file.getFmsFile();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return excelFile;
	}

	private void createDataset(String filePath) {
		String datasetType = "MSExcelX";

		NamedReferenceContext namedReferenceContext[] = null;
		String namedRef = null;

		try {
			String fileName = getNameSORDocument();
			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
			namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			TCComponentDataset newDataset = datasetTypeComponent.create(fileName, fileName, datasetDefinition.toString());
			newDataset.refresh();
			newDataset.setFiles(new String[] { filePath }, type);
			selectedObject.add("IMAN_specification", newDataset);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private String getNameSORDocument() {
		String output = "SOR_Document_";
		try {
			output += selectedObject.getProperty("item_id") + "_" + selectedObject.getProperty("item_revision_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	private void addWaterMark(XSSFSheet sheet) throws Exception {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		Header header = sheet.getHeader();
		header.setCenter("&C&KE7E6E6&50\r\r\r\rVinFast\r" + df.format(cal.getTime()));
	}

	private String getCoordinatedChange() {
		Set<String> coordinatedChange = new HashSet<String>();
		try {
			TCComponent[] objects = selectedObject.getRelatedComponents("Vf6_CoordinatedChange");
			if (objects != null) {
				for (TCComponent object : objects) {
					coordinatedChange.add(object.getPropertyDisplayableValue("item_id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.join(";", coordinatedChange);
	}
}