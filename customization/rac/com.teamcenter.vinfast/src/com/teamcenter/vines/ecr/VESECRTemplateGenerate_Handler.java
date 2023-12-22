package com.teamcenter.vines.ecr;

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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.ExcelExtension;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VESECRTemplateGenerate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject;

	public VESECRTemplateGenerate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp[0] instanceof TCComponentItemRevision)
			selectedObject = (TCComponentItemRevision) targetComp[0];

		if (selectedObject == null) {
			MessageBox.post("Please select ECR Revision.", "Export ECR Information", MessageBox.WARNING);
			return null;
		}

		if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
			MessageBox.post("You don't have WRITE access on the ECR.", "Export ECR Information", MessageBox.WARNING);
			return null;
		}

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
				MessageBox.post("Can not get ECR Document Template file. Please contact your administrator for further instructions with below messages.", "Export ECR Information failed", MessageBox.ERROR);

			InputStream outputFile = new FileInputStream(templateFile);
			XSSFWorkbook workbook = new XSSFWorkbook(outputFile);
			templateFile.setWritable(true);

			if (!isNewFile) {
				int ecrSummarySheetIndex = workbook.getSheetIndex("ECR Summary");
				if (ecrSummarySheetIndex >= 0)
					workbook.removeSheetAt(ecrSummarySheetIndex);
			}

			int ecrSummaryTempSheetIndex = workbook.getSheetIndex("ECR Summary Template");
			workbook.cloneSheet(ecrSummaryTempSheetIndex);
			workbook.setSheetName(workbook.getNumberOfSheets() - 1, "ECR Summary");
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				if (i == workbook.getNumberOfSheets() - 1)
					workbook.getSheetAt(i).setSelected(true);
				else
					workbook.getSheetAt(i).setSelected(false);
			}
			workbook.setActiveSheet(workbook.getNumberOfSheets() - 1);

			File operationImage = getECRImage();
			if (operationImage != null) {
				importImage(workbook, "B", 8, "Z", 41, operationImage);
			}

			XSSFSheet worksheet = workbook.getSheet("ECR Summary");
			addWaterMark(worksheet);
			updateCellValue(worksheet, "H3", selectedObject.getPropertyDisplayableValue("item_id") + " - " + selectedObject.getPropertyDisplayableValue("object_name"));

			// -------------------------------- Change Description
			updateCellValue(worksheet, "E7", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_current_weight"))); // Current Weight
			updateCellValue(worksheet, "J7", selectedObject.getPropertyDisplayableValue("vf4_current_material")); // Current Material
			updateCellValue(worksheet, "Q7", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_new_weight"))); // New Weight
			updateCellValue(worksheet, "U7", selectedObject.getPropertyDisplayableValue("vf4_new_material")); // New Material

			// -------------------------------- Impact
			updateCellValue(worksheet, "AD6", selectedObject.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_SUPPLIER));// Supplier
			updateCellValue(worksheet, "AD8", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_veh_cost_delta")));
			updateCellValue(worksheet, "AD9", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_piece_cost_delta")));
			updateCellValue(worksheet, "AD10", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_tooling_cost_delta")));
			updateCellValue(worksheet, "AD11", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_edd_cost_delta")));
			updateCellValue(worksheet, "AD12", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_sunk_cost")));
			updateCellValue(worksheet, "AD13", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_logistic_cost")));
			updateCellValue(worksheet, "AD14", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_rework_cost")));
			updateCellValue(worksheet, "AD15", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_budget_cost")));
			updateCellValue(worksheet, "AD16", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_plant_equip_cost")));
			updateCellValue(worksheet, "AD17", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_testing_validate_c")));
			updateCellValue(worksheet, "AD18", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_init_other_cost")));
			updateCellValue(worksheet, "AD19", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_leadtime_impact1")));// Leadtime impact
			updateCellValue(worksheet, "AD20", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_tooling_impact")));// Tooling impact
			updateCellValue(worksheet, "AD21", selectedObject.getPropertyDisplayableValue("vf4_customer_impact"));// Customer impact
//			updateCellValue(worksheet, "AD22", selectedObject.getPropertyDisplayableValue("vf6_is_htko1_required"));
//			updateCellValue(worksheet, "AD23", selectedObject.getPropertyDisplayableValue("vf6_is_htko2_required"));
			updateCellValue(worksheet, "AI8", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_vehicle_cost_delta")));
			updateCellValue(worksheet, "AI9", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_delta_piece_cost")));
			updateCellValue(worksheet, "AI10", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_delta_tooling_cost")));
			updateCellValue(worksheet, "AI11", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_delta_edd_cost")));
			updateCellValue(worksheet, "AI12", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_sunk_cost")));
			updateCellValue(worksheet, "AI13", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_logistic_cost")));
			updateCellValue(worksheet, "AI16", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_plant_equipment_cost")));
			updateCellValue(worksheet, "AI17", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_testing_validate_cost")));
			updateCellValue(worksheet, "AI18", getDecimal(selectedObject.getPropertyDisplayableValue("vf4_other_cost")));

			updateCellValue(worksheet, "Z25", selectedObject.getPropertyDisplayableValue("vf4_problem"));// Problem
			updateCellValue(worksheet, "Z32", selectedObject.getPropertyDisplayableValue("vf4_root_cause"));// Root cause
			updateCellValue(worksheet, "Z39", selectedObject.getPropertyDisplayableValue("vf4_solution"));// Solution

			// -------------------------------- Precab Information
			updateCellValue(worksheet, "AS6", selectedObject.getPropertyDisplayableValue("vf4_vehicle_group"));// Project/Program
			// Market
			String market = selectedObject.getPropertyDisplayableValue("vf4_market");
			updateCellValue(worksheet, "AS7", market.contains("Vietnam") ? "R" : "£");
			updateCellValue(worksheet, "AT7", market.contains("USA&CAN") ? "R" : "£");
			updateCellValue(worksheet, "AU7", market.contains("Europe") ? "R" : "£");
			updateCellValue(worksheet, "AV7", market.contains("USA&CAN") ? "R" : "£");
			updateCellValue(worksheet, "AW7", market.contains("Thailand") ? "R" : "£");
			updateCellValue(worksheet, "AX7", market.contains("Singapore") ? "R" : "£");
			updateCellValue(worksheet, "AY7", market.contains("United_Kingdom") ? "R" : "£");
			updateCellValue(worksheet, "AZ7", market.contains("Australia") ? "R" : "£");
			updateCellValue(worksheet, "AS10", market.contains("Indonesia") ? "R" : "£");
			updateCellValue(worksheet, "AT10", market.contains("Malaysia") ? "R" : "£");
			// Driving Position
			String lhdrhd = selectedObject.getPropertyDisplayableValue("vf4_lhd_rhd");
			updateCellValue(worksheet, "AS14", lhdrhd.contains("LHD") ? "R" : "£");
			updateCellValue(worksheet, "AW14", lhdrhd.contains("RHD") ? "R" : "£");
			if (lhdrhd.compareToIgnoreCase("All") == 0) {
				updateCellValue(worksheet, "AS14", "R");
				updateCellValue(worksheet, "AW14", "R");
			}
			// Variant
			String variant = selectedObject.getPropertyDisplayableValue("vf4_variant");
			updateCellValue(worksheet, "AS16", variant.isEmpty() ? "" : variant.replace(",", "\n"));

			updateCellValue(worksheet, "AN26", selectedObject.getPropertyDisplayableValue("vf4_pre_decision_approval"));// Pre-cab decision approval
			updateCellValue(worksheet, "AS26", selectedObject.getPropertyDisplayableValue("vf4_precab_decision_comment"));// Pre-cab decision comments
			updateCellValue(worksheet, "AS31", selectedObject.getPropertyDisplayableValue("vf4_precab_approval_date"));// Pre-cab Approval date

			// -------------------------------- ECR Information
			updateCellValue(worksheet, "AS33", selectedObject.getPropertyDisplayableValue("vf4_ecr_category"));// ECR Category
			updateCellValue(worksheet, "AS34", selectedObject.getPropertyDisplayableValue("item_id"));// ECR No
			updateCellValue(worksheet, "AS35", "");// ECR Trigger date
			updateCellValue(worksheet, "AS36", selectedObject.getPropertyDisplayableValue("vf4_ecr_contact_email"));// ECR contact email
			updateCellValue(worksheet, "AS37", selectedObject.getPropertyDisplayableValue("vf4_module_group"));// Module Group
			updateCellValue(worksheet, "AS38", selectedObject.getPropertyDisplayableValue("vf4_sil_no"));// SIL No
			updateCellValue(worksheet, "AS39", selectedObject.getPropertyDisplayableValue("vf4_dcr_no"));// DCR No
			updateCellValue(worksheet, "AS40", selectedObject.getPropertyDisplayableValue("vf4_implementation_date_arr"));// Implementation Date
			updateCellValue(worksheet, "AS41", selectedObject.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_COP_IMPACTED_PROGRAM));// COP Impacted Program
			updateCellValue(worksheet, "AS42", getCoordinatedChange());// Coordinated change

			// -------------------------------- Affection
			// Impacted Module Group
			String impactedModule = selectedObject.getItem().getPropertyDisplayableValue("vf4_impacted_module_arr");
			updateCellValue(worksheet, "BH7", impactedModule.contains("BATTERY PACK") ? "ü" : "");
			updateCellValue(worksheet, "BH8", impactedModule.contains("ME") ? "ü" : "");
			updateCellValue(worksheet, "BH9", impactedModule.contains("MD") ? "ü" : "");
			updateCellValue(worksheet, "BH10", impactedModule.contains("EE") ? "ü" : "");
			updateCellValue(worksheet, "BH11", impactedModule.contains("SW") ? "ü" : "");
			updateCellValue(worksheet, "BH12", impactedModule.contains("ESS") ? "ü" : "");

			// Function (Option)
			updateCellValue(worksheet, "BH24", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_DMU_required")) ? "ü" : "");// DMU
			updateCellValue(worksheet, "BH25", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_system_arch")) ? "ü" : "");// System Architect Approval
			updateCellValue(worksheet, "BH26", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_module_group")) ? "ü" : "");// Module Group Leader
			updateCellValue(worksheet, "BH27", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_qual_eng")) ? "ü" : "");// Quality Engineering
			updateCellValue(worksheet, "BH28", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_manu_quality")) ? "ü" : "");// Manufacturing Quality
			updateCellValue(worksheet, "BH29", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_manufacturing")) ? "ü" : "");// Manufacturing
			updateCellValue(worksheet, "BH30", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_test_n_vali_review")) ? "ü" : "");// Testing & Validation
			updateCellValue(worksheet, "BH31", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_cae_required")) ? "ü" : "");// CAE
			updateCellValue(worksheet, "BH32", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_homo_required")) ? "ü" : "");// Homologation
			updateCellValue(worksheet, "BH33", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_aftersale_required")) ? "ü" : "");// After Sale - Service
			updateCellValue(worksheet, "BH34", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_functional_safety")) ? "ü" : "");// Functional Safety
			updateCellValue(worksheet, "BH35", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_cost_eng")) ? "ü" : "");// Cost Engineering
			updateCellValue(worksheet, "BH36", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_direct_pur")) ? "ü" : "");// Direct Purchase Commodity
			updateCellValue(worksheet, "BH37", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_gr_indirect_pur")) ? "ü" : "");// In-Direct Purchase Commodity
			updateCellValue(worksheet, "BH38", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_supplier_qual_manage")) ? "ü" : "");// Supplier Quality Manager
			updateCellValue(worksheet, "BH39", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_HTKO1_required")) ? "ü" : "");// HTKO1
			updateCellValue(worksheet, "BH40", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_is_HTKO2_required")) ? "ü" : "");// HTKO2
			updateCellValue(worksheet, "BH41", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_dceo_rnd_approve")) ? "ü" : "");// DCEO R&D Approval

			// Documentation
			updateCellValue(worksheet, "BQ7", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_have_spec_book")) ? "ü" : "");// Have Spec Book
			updateCellValue(worksheet, "BQ8", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_have_PFMEA")) ? "ü" : "");// Have PFMEA
			updateCellValue(worksheet, "BQ9", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_have_DFMEA")) ? "ü" : "");// Have DFMEA
			updateCellValue(worksheet, "BQ10", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf4_have_DVP")) ? "ü" : "");// Have DVP

			// -------------------------------- Bom Info
			TCComponent[] bomInfo = selectedObject.getReferenceListProperty("vf4_bom_infomation");
			if (bomInfo.length > 0) {
				int i = 0;
				for (TCComponent bomline : bomInfo) {
					updateCellValue(worksheet, "B" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_POSID));
					updateCellValue(worksheet, "E" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_DONOR_VEHICLE));
					updateCellValue(worksheet, "G" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_STRUCTURE_LEVEL));
					updateCellValue(worksheet, "H" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_STEERING));
					updateCellValue(worksheet, "I" + String.valueOf(i + 48), getDecimal(bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_QUANTITY)));
					updateCellValue(worksheet, "J" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_MATURITY_LEVEL));
					updateCellValue(worksheet, "L" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL));
					updateCellValue(worksheet, "M" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_CHANGE_TYPE));
					updateCellValue(worksheet, "O" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_PART_NUMBER));
					updateCellValue(worksheet, "R" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_OLD_VERSION));
					updateCellValue(worksheet, "S" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_FROZEN_REVISION));
					updateCellValue(worksheet, "T" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_NEW_VERSION));
					updateCellValue(worksheet, "U" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_PART_NAME));
					updateCellValue(worksheet, "AC" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART));
					updateCellValue(worksheet, "AG" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_VARIANT_FORMULA));
					updateCellValue(worksheet, "AL" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_TORQUE_INFO));
					updateCellValue(worksheet, "AQ" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_WEIGHT));
					updateCellValue(worksheet, "AS" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_CHANGE_DESC));
					updateCellValue(worksheet, "BD" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_3D_DATA_AFFECTED));
					updateCellValue(worksheet, "BE" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_MATERIAL));
					updateCellValue(worksheet, "BH" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_CAD_COATING));
					updateCellValue(worksheet, "BM" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_SPEC_BOOK));
					updateCellValue(worksheet, "BQ" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_SUPPLIER));
					updateCellValue(worksheet, "BU" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_SUPPLIER_CONTACT));
					updateCellValue(worksheet, "BY" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_IS_AFTERSALE));
					updateCellValue(worksheet, "CA" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_EXCHANGEABILITY));
					updateCellValue(worksheet, "CE" + String.valueOf(i + 48), bomline.getPropertyDisplayableValue(VESECRNameDefine.BOMINFO_PART_TRACE));
					i++;
				}
			}

			outputFile.close();

			FileOutputStream outputStream = new FileOutputStream(templateFile);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

			if (isNewFile) {
				createDataset(templateFile.getPath());
				templateFile.delete();
			}
			MessageBox.post("ECR Document update success.", "Export ECR Information", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post("ECR Document update unsuccess. Exception: " + e.toString(), "Export ECR Information", MessageBox.ERROR);
		}
	}

	private File downloadTemplateFile() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", "VES_ECRDocument_Template");
		parameter.put("Dataset Type", "MS ExcelX");
		parameter.put("Owning Group", "dba");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0)
			return TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", getNameECRDocument() + ".xlsx", session);

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
			String ecrDocumentName = getNameECRDocument();
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
			String fileName = getNameECRDocument();
			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
			namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			TCComponentDataset newDataset = datasetTypeComponent.create(fileName, fileName, datasetDefinition.toString());
			newDataset.refresh();
			newDataset.setFiles(new String[] { filePath }, type);
			selectedObject.add("CMReferences", newDataset);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private String getNameECRDocument() {
		String output = "ECR_Document_";
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
			TCComponent[] objects = selectedObject.getRelatedComponents("VF4_CoordinatedChange");
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
