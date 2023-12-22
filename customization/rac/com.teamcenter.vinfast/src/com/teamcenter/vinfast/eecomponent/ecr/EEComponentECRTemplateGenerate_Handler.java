package com.teamcenter.vinfast.eecomponent.ecr;

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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.poi.openxml4j.util.ZipSecureFile;
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

public class EEComponentECRTemplateGenerate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject;

	public EEComponentECRTemplateGenerate_Handler() {
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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int userConf = JOptionPane.showConfirmDialog(null, "Do you want Generate ECR Document. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (userConf == JOptionPane.YES_OPTION) {
					try {
						exportExcelTemplate();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

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

			ZipSecureFile.setMinInflateRatio(0);
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
			updateCellValue(worksheet, "E7", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_current_weight"))); // Current Weight
			updateCellValue(worksheet, "J7", selectedObject.getPropertyDisplayableValue("vf6cp_current_material")); // Current Material
			updateCellValue(worksheet, "Q7", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_new_weight"))); // New Weight
			updateCellValue(worksheet, "U7", selectedObject.getPropertyDisplayableValue("vf6cp_new_material")); // New Material

			// -------------------------------- Impact
			updateCellValue(worksheet, "AD6", selectedObject.getPropertyDisplayableValue("vf6cp_supplier"));// Supplier
			updateCellValue(worksheet, "AD8", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_veh_cost_delta")));// Vehicle cost delta
			updateCellValue(worksheet, "AD9", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_piece_cost_delta")));// Piece cost delta
			updateCellValue(worksheet, "AD10", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_tooling_cost_delta")));// Tooling cost delta
			updateCellValue(worksheet, "AD11", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_edd_cost_delta")));// ED&D cost delta
			updateCellValue(worksheet, "AD12", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_sunk_cost")));// Sunk Cost
			updateCellValue(worksheet, "AD13", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_scrap_cost")));// Scrap Cost
			updateCellValue(worksheet, "AD14", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_plant_equip_cost")));// Plant & Equipment Cost
			updateCellValue(worksheet, "AD15", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_testing_validate_c")));// Testing & Validation Cost
			updateCellValue(worksheet, "AD16", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_other_cost")));// Other cost
			updateCellValue(worksheet, "AD17", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_leadtime_impact")));// Leadtime impact
			updateCellValue(worksheet, "AD18", selectedObject.getPropertyDisplayableValue("vf6_build_time_impact"));// PTO/SOP Time impact
			updateCellValue(worksheet, "AD19", selectedObject.getPropertyDisplayableValue("vf6_is_htko1_required"));// Hard TKO-01/Order material
			updateCellValue(worksheet, "AD20", selectedObject.getPropertyDisplayableValue("vf6_is_htko2_required"));// Hard TKO-02/Cut steel
			updateCellValue(worksheet, "Z22", selectedObject.getPropertyDisplayableValue("vf6cp_problem"));// Problem
			updateCellValue(worksheet, "Z29", selectedObject.getPropertyDisplayableValue("vf6cp_root_cause"));// Root cause
			updateCellValue(worksheet, "Z36", selectedObject.getPropertyDisplayableValue("vf6cp_solution"));// Solution

			// -------------------------------- Precab Information
			updateCellValue(worksheet, "AS6", selectedObject.getPropertyDisplayableValue("vf6_vehicle_group"));// Project/Program
			// Market
			String market = selectedObject.getPropertyDisplayableValue("vf6cp_market");
			updateCellValue(worksheet, "AS7", market.contains("Vietnam") ? "R" : "£");
			updateCellValue(worksheet, "AT7", market.contains("USA&CAN") ? "R" : "£");
			updateCellValue(worksheet, "AU7", market.contains("Europe") ? "R" : "£");
			updateCellValue(worksheet, "AV7", market.contains("USA&CAN") ? "R" : "£");
			updateCellValue(worksheet, "AW7", market.contains("ASIA") ? "R" : "£");
			updateCellValue(worksheet, "AX7", market.contains("Singapore") ? "R" : "£");
			updateCellValue(worksheet, "AY7", market.contains("United_Kingdom") ? "R" : "£");
			updateCellValue(worksheet, "AZ7", market.contains("Australia") ? "R" : "£");
			updateCellValue(worksheet, "AS10", market.contains("Indonesia") ? "R" : "£");
			updateCellValue(worksheet, "AT10", market.contains("Malaysia") ? "R" : "£");
			updateCellValue(worksheet, "AU10", market.contains("Lao") ? "R" : "£");
			updateCellValue(worksheet, "AV10", market.contains("Cambodia") ? "R" : "£");
			updateCellValue(worksheet, "AW10", market.contains("Israel") ? "R" : "£");
			updateCellValue(worksheet, "AX10", market.contains("Thailand") ? "R" : "£");
			// Driving Position
			String lhdrhd = selectedObject.getPropertyDisplayableValue("vf6cp_lhd_rhd");
			updateCellValue(worksheet, "AS14", lhdrhd.contains("LHD") ? "R" : "£");
			updateCellValue(worksheet, "AW14", lhdrhd.contains("RHD") ? "R" : "£");
			if (lhdrhd.compareToIgnoreCase("All") == 0) {
				updateCellValue(worksheet, "AS14", "R");
				updateCellValue(worksheet, "AW14", "R");
			}
			// Variant
			String variant = selectedObject.getPropertyDisplayableValue("vf6cp_variant");
			updateCellValue(worksheet, "AS16", variant.isEmpty() ? "" : variant.replace(",", "\n"));
			// Seat
			String seatConfi = selectedObject.getPropertyDisplayableValue("vf6cp_seat_configuration");
			updateCellValue(worksheet, "AS20", seatConfi.contains("5") ? "R" : "£");
			updateCellValue(worksheet, "AU20", seatConfi.contains("6") ? "R" : "£");
			updateCellValue(worksheet, "AW20", seatConfi.contains("7") ? "R" : "£");
			updateCellValue(worksheet, "AY20", seatConfi.contains("4") ? "R" : "£");

			updateCellValue(worksheet, "AN23", selectedObject.getPropertyDisplayableValue("vf6cp_pre_decision_approval"));// Pre-cab decision approval
			updateCellValue(worksheet, "AS23", selectedObject.getPropertyDisplayableValue("vf6cp_precab_dec_comment"));// Pre-cab decision comments
			updateCellValue(worksheet, "AS28", selectedObject.getPropertyDisplayableValue("vf6cp_pre_approval_date"));// Pre-cab Approval date

			// -------------------------------- ECR Information
			updateCellValue(worksheet, "AS30", selectedObject.getPropertyDisplayableValue("vf6_ecr_category"));// ECR Category
			updateCellValue(worksheet, "AS31", selectedObject.getPropertyDisplayableValue("item_id"));// ECR No
			updateCellValue(worksheet, "AS32", "");// ECR Trigger date
			updateCellValue(worksheet, "AS33", selectedObject.getPropertyDisplayableValue("vf6cp_ecr_contact_email"));// ECR contact email
			updateCellValue(worksheet, "AS34", selectedObject.getPropertyDisplayableValue("vf6_module_group_comp"));// Module Group
			updateCellValue(worksheet, "AS35", selectedObject.getPropertyDisplayableValue("vf6cp_sil_no"));// SIL No
			updateCellValue(worksheet, "AS36", selectedObject.getPropertyDisplayableValue("vf6cp_dcr_no"));// DCR No
			updateCellValue(worksheet, "AS37", selectedObject.getPropertyDisplayableValue("vf6_implementation_date"));// Implementation Date
//			updateCellValue(worksheet, "AS38", selectedObject.getPropertyDisplayableValue("vf6_cop_impacted_program"));// COP Impacted Program
			updateCellValue(worksheet, "AS39", getCoordinatedChange());// Coordinated change

			// -------------------------------- Affection
			// Impacted Module Group
			String impactedModule = selectedObject.getPropertyDisplayableValue("vf6_impacted_module_comp");
			updateCellValue(worksheet, "BH7", impactedModule.contains("Chassis") ? "ü" : "");
			updateCellValue(worksheet, "BH8", impactedModule.contains("Doors Closures") ? "ü" : "");
			updateCellValue(worksheet, "BH9", impactedModule.contains("Electrics/Electronics") ? "ü" : "");
			updateCellValue(worksheet, "BH10", impactedModule.contains("Interior") ? "ü" : "");
			updateCellValue(worksheet, "BH11", impactedModule.contains("Powertrain") ? "ü" : "");
			updateCellValue(worksheet, "BH12", impactedModule.contains("Vehicle Complete") ? "ü" : "");
			updateCellValue(worksheet, "BH13", impactedModule.contains("Exterior") ? "ü" : "");
			updateCellValue(worksheet, "BH14", impactedModule.contains("Body in White") ? "ü" : "");
			updateCellValue(worksheet, "BH15", impactedModule.contains("Seats and Restraints") ? "ü" : "");
			updateCellValue(worksheet, "BH16", impactedModule.contains("Battery Pack") ? "ü" : "");
			updateCellValue(worksheet, "BH17", impactedModule.contains("E-MOTOR") ? "ü" : "");
			updateCellValue(worksheet, "BH18", impactedModule.contains("3_IN_1_PDU_DCDC_OBC") ? "ü" : "");
			updateCellValue(worksheet, "BH19", impactedModule.contains("ESS") ? "ü" : "");

			// Function (Default)
			updateCellValue(worksheet, "BH21", "ü");// PROJECT MANAGEMENT
			updateCellValue(worksheet, "BH22", "ü");// MGL ENGINEERING
			updateCellValue(worksheet, "BH23", "ü");// MANUFACTURING
			updateCellValue(worksheet, "BH24", "ü");// ENGINERING QUALITY
			updateCellValue(worksheet, "BH25", "ü");// MANUFACTURING QUALITY
			updateCellValue(worksheet, "BH26", "ü");// TESTING AND VALIDATION
			updateCellValue(worksheet, "BH27", "ü");// COST ENGINEER / PRODUCT FINANCE
			updateCellValue(worksheet, "BH28", "ü");// DIRECT PURCHASING
			updateCellValue(worksheet, "BH29", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_indirectPur_required")) ? "ü" : "");// INDIRECT PURCHASING (H) Part

			// Function (Option)
			updateCellValue(worksheet, "BH32", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_DMU_required")) ? "ü" : "");// VEHICLE ARCHITECTURE
			updateCellValue(worksheet, "BH33", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_homo_required")) ? "ü" : "");// HOMOLOGATION
			updateCellValue(worksheet, "BH34", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_cae_required")) ? "ü" : "");// CAE SIMULATION
			updateCellValue(worksheet, "BH35", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_aftersale_required")) ? "ü" : "");// AFTERSALES - SERVICE
			updateCellValue(worksheet, "BH36", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_standard_part")) ? "ü" : "");// STANDARD PART
			updateCellValue(worksheet, "BH37", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_functional_safety")) ? "ü" : "");// FUNCTIONAL SAFETY
			updateCellValue(worksheet, "BH38", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_color_trim_required")) ? "ü" : "");// STYLING and COLOR&TRIM
			updateCellValue(worksheet, "BH39", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_supplier_qual_manage")) ? "ü" : "");// SUPPLIER QUALITY MANAGER
			updateCellValue(worksheet, "BH40", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_sale_and_marketin")) ? "ü" : "");// SALE & PRODUCT MARKETING

			// Documentation
			updateCellValue(worksheet, "BQ7", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_SOR")) ? "ü" : "");// Have SOR
			updateCellValue(worksheet, "BQ8", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_spec_book")) ? "ü" : "");// Have Spec Book
			updateCellValue(worksheet, "BQ9", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_DFMEA")) ? "ü" : "");// Have DFMEA
			updateCellValue(worksheet, "BQ10", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_DVP")) ? "ü" : "");// Have DVP

			// -------------------------------- Bom Info
			TCComponent[] forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
			if (forms.length > 0) {
				TCComponent[] bomInfo = forms[0].getRelatedComponents("vf6_bom_information");
				if (bomInfo.length > 0) {
					int i = 1;
					for (TCComponent bomline : bomInfo) {
						updateCellValue(worksheet, "B" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER));
						updateCellValue(worksheet, "E" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE));
						updateCellValue(worksheet, "G" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER));
						updateCellValue(worksheet, "J" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME));
						updateCellValue(worksheet, "O" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION));
						updateCellValue(worksheet, "P" + String.valueOf(i + 45), getDecimal(bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY)));
						updateCellValue(worksheet, "Q" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN));
						updateCellValue(worksheet, "U" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE));
						updateCellValue(worksheet, "AA" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR));
						updateCellValue(worksheet, "AD" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_NUMBER));
						updateCellValue(worksheet, "AG" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_NAME));
						updateCellValue(worksheet, "AN" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_REVISION));
						updateCellValue(worksheet, "AO" + String.valueOf(i + 45), getDecimal(bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY)));
						updateCellValue(worksheet, "AP" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_MPN));
						updateCellValue(worksheet, "AT" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE));
						updateCellValue(worksheet, "AY" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR));
						updateCellValue(worksheet, "BD" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_CHANGE_POINT));
						updateCellValue(worksheet, "BL" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_DISPOSAL_MATERIAL));
						updateCellValue(worksheet, "BO" + String.valueOf(i + 45), bomline.getPropertyDisplayableValue(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY));
						i++;
					}
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
		parameter.put("Name", "VF_EE_ECR_Document_Template");
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
