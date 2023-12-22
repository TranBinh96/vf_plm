package com.teamcenter.vinfast.swbom.view;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.apache.poi.ss.usermodel.CellStyle;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.BOMVariantService;
import com.vf.utils.ExcelExtension;
import com.vf.utils.StringExtension;

public class FRSExport_Handler extends AbstractHandler {
	private TCSession session;
	private FRSExport_Dialog dlg;
	private TCComponentBOMLine rootLine = null;
	private ProgressMonitorDialog progressMonitorDialog = null;

	private LinkedHashMap<String, String> didNoMapping = new LinkedHashMap<String, String>() {
		{
			put("HW", "F191");
			put("SW", "F188");
			put("CAL", "F102");
			put("SW_APP", "F104");
			put("SW_BASIC", "F105");
			put("FONT", "F109");
			put("HMI", "F106");
			put("TBOX", "F164");
			put("SW2-10", "F198");
			put("Bootloader", "F101");
		}
	};

	private LinkedHashMap<String, String> didRevMapping = new LinkedHashMap<String, String>() {
		{
			put("HW", "F141");
			put("SW", "F148");
			put("CAL", "F142");
			put("SW_APP", "F144");
			put("SW_BASIC", "F145");
			put("FONT", "F149");
			put("HMI", "F146");
			put("TBOX", "F149");
			put("SW2-10", "F158");
			put("Bootloader", "");
		}
	};

	private DataManagementService dmService;

	public FRSExport_Handler() {
		super();
	}

	public class ECUModel {
		public String ecuName = "";
		public String purchasePartNumber = "";
		public String purchasePartName = "";
		public String purchasePartRev = "";
		public LinkedList<LinkedHashMap<String, String>> partList = new LinkedList<LinkedHashMap<String, String>>();
		public LinkedList<LinkedHashMap<String, String>> bootloaderList = new LinkedList<LinkedHashMap<String, String>>();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp.length != 1) {
				return null;
			}

			if (targetComp[0] instanceof TCComponentBOMLine)
				rootLine = (TCComponentBOMLine) targetComp[0];
			else
				return null;

			dlg = new FRSExport_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("SWBOM Export.", IMessageProvider.INFORMATION);
			dlg.txtFRSNumber.setText(rootLine.getItem().getPropertyDisplayableValue("object_string"));
			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					processAccept();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void processAccept() {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Export FRS...", IProgressMonitor.UNKNOWN);
					try {
						exportFRSNewTempalte();
						dlg.setMessage("Export success", IMessageProvider.INFORMATION);
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exportFRSNewTempalte() {
		try {
			TCComponentItem item = rootLine.getItem();
			String contextID = item.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
			LinkedList<ECUModel> ecuModelList = new LinkedList<ECUModel>();
			TCComponentItem objectItem = (TCComponentItem) rootLine.getItem();
			TCComponentItemRevision objectItemRev = (TCComponentItemRevision) rootLine.getItemRevision();

			BOMVariantService variantService = new BOMVariantService(session, contextID.split("-")[0]);

			String frsNumber = objectItem.getPropertyDisplayableValue("item_id");
			String frsName = objectItem.getPropertyDisplayableValue("object_name");

			LinkedList<TCComponentBOMLine> ecuList = expandBOMLines(rootLine);
			dmService.getProperties(ecuList.toArray(new TCComponent[0]), new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "bl_rev_object_name", "bl_formula" });

			int currentLevel = 0;
			String formulaText;
			for (TCComponentBOMLine ecuItem : ecuList) {
				ECUModel newItem = new ECUModel();

				newItem.ecuName = ecuItem.getPropertyDisplayableValue("bl_rev_object_name");

				currentLevel = 1;
				formulaText = ecuItem.getPropertyDisplayableValue("bl_formula");
				variantService.getVariantList(currentLevel, formulaText);

				LinkedList<TCComponentBOMLine> partList = expandBOMLines(ecuItem);
				dmService.getProperties(partList.toArray(new TCComponent[0]), new String[] { "bl_item_item_id", "bl_rev_item_revision_id", "bl_rev_object_name", "VL5_purchase_lvl_vf", "vf4_software_part_type", "VF4_bootloader", "VF4_remarks", "VF4_supplier_rev" });
				for (TCComponentBOMLine partItem : partList) {
					String swPartType = partItem.getPropertyDisplayableValue("vf4_software_part_type");
					String supplierRev = partItem.getPropertyDisplayableValue("VF4_supplier_rev");
					if (swPartType.compareTo("PURCHASE") == 0) {
						newItem.purchasePartNumber = partItem.getPropertyDisplayableValue("bl_item_item_id");
						newItem.purchasePartName = partItem.getPropertyDisplayableValue("bl_rev_object_name");
						if (!supplierRev.isEmpty())
							newItem.purchasePartRev = supplierRev;
						else
							newItem.purchasePartRev = partItem.getPropertyDisplayableValue("bl_rev_item_revision_id");
					} else {
						LinkedHashMap<String, String> newPartItem = new LinkedHashMap<String, String>();
						newPartItem.put("PartNumber", partItem.getPropertyDisplayableValue("bl_item_item_id"));
						newPartItem.put("PartName", partItem.getPropertyDisplayableValue("bl_rev_object_name"));
						if (!supplierRev.isEmpty())
							newPartItem.put("PartRev", supplierRev);
						else
							newPartItem.put("PartRev", partItem.getPropertyDisplayableValue("bl_rev_item_revision_id"));

						newPartItem.put("PurchaseLevel", partItem.getPropertyDisplayableValue("VL5_purchase_lvl_vf"));

						newPartItem.put("SWPartType", swPartType);
						newPartItem.put("DIDNo", didNoMapping.getOrDefault(swPartType, ""));
						newPartItem.put("DIDRev", didRevMapping.getOrDefault(swPartType, ""));

						newPartItem.put("Remarks", partItem.getPropertyDisplayableValue("VF4_remarks"));

						currentLevel = 2;
						formulaText = partItem.getPropertyDisplayableValue("bl_formula");
						Set<String> variantList = variantService.getVariantList(currentLevel, formulaText);
						newPartItem.put("Variant", variantList == null ? "" : String.join(";", variantList));

						newItem.partList.add(newPartItem);

						if (swPartType.compareTo("HW") == 0) {
							String bootLoader = partItem.getPropertyDisplayableValue("VF4_bootloader");
							if (!bootLoader.isEmpty()) {
								LinkedHashMap<String, String> newPartBootloader = new LinkedHashMap<String, String>();
								newPartBootloader.put("PartNumber", bootLoader);
								newPartBootloader.put("PartName", "");
								newPartBootloader.put("PartRev", "");
								newPartBootloader.put("PurchaseLevel", "");
								newPartBootloader.put("SWPartType", "Bootloader");
								newPartBootloader.put("DIDNo", didNoMapping.getOrDefault("Bootloader", ""));
								newPartBootloader.put("DIDRev", didRevMapping.getOrDefault("Bootloader", ""));
								newPartBootloader.put("Remarks", "");
								newPartBootloader.put("Variant", variantList == null ? "" : String.join(";", variantList));

								newItem.bootloaderList.add(newPartBootloader);
							}
						}
					}
				}

				ecuModelList.add(newItem);
			}

			exportToExcel(ecuModelList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exportToExcel(LinkedList<ECUModel> ecuModelList) {
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet spreadsheet = wb.createSheet("Data");

			int cellNumber = 0;
			int rowNumber = 0;

			CellStyle defaultCellStyle = ExcelExtension.getDefaultCellStyle(wb);

			String[] headers = new String[] { "ECU Name", "PN Type", "VF PN with Revision", "DID No", "VF PN", "Rev Did", "Rev", "Purchase Level", "Purchase Part Number with Rev", "Purchase Partnumber after Reflashing", "Remark", "ECO", "PLUS" };
			XSSFRow headerRow = spreadsheet.createRow(rowNumber++);
			for (String header : headers) {
				XSSFCell cell = headerRow.createCell(cellNumber++);
				cell.setCellStyle(defaultCellStyle);
				cell.setCellValue(header);
			}

			for (ECUModel ecuItem : ecuModelList) {
				for (LinkedHashMap<String, String> partItem : ecuItem.partList) {
					XSSFRow bodyRow = spreadsheet.createRow(rowNumber++);

					XSSFCell cell = bodyRow.createCell(0);
					cell.setCellStyle(defaultCellStyle);
					cell.setCellValue(ecuItem.ecuName);

					XSSFCell cell1 = bodyRow.createCell(1);
					cell1.setCellStyle(defaultCellStyle);
					cell1.setCellValue(partItem.get("SWPartType"));

					XSSFCell cell2 = bodyRow.createCell(2);
					cell2.setCellStyle(defaultCellStyle);
					cell2.setCellValue(partItem.get("PartNumber") + "-" + partItem.get("PartRev"));

					XSSFCell cell3 = bodyRow.createCell(3);
					cell3.setCellStyle(defaultCellStyle);
					cell3.setCellValue(partItem.get("DIDNo"));

					XSSFCell cell4 = bodyRow.createCell(4);
					cell4.setCellStyle(defaultCellStyle);
					cell4.setCellValue(partItem.get("PartNumber"));

					XSSFCell cell5 = bodyRow.createCell(5);
					cell5.setCellStyle(defaultCellStyle);
					cell5.setCellValue(partItem.get("DIDRev"));

					XSSFCell cell6 = bodyRow.createCell(6);
					cell6.setCellStyle(defaultCellStyle);
					cell6.setCellValue(partItem.get("PartRev"));

					XSSFCell cell7 = bodyRow.createCell(7);
					cell7.setCellStyle(defaultCellStyle);
					cell7.setCellValue(partItem.get("PurchaseLevel"));

					XSSFCell cell8 = bodyRow.createCell(8);
					cell8.setCellStyle(defaultCellStyle);
					cell8.setCellValue(ecuItem.purchasePartNumber + "-" + ecuItem.purchasePartRev);

					XSSFCell cell9 = bodyRow.createCell(9);
					cell9.setCellStyle(defaultCellStyle);
					cell9.setCellValue("");

					XSSFCell cell10 = bodyRow.createCell(10);
					cell10.setCellStyle(defaultCellStyle);
					cell10.setCellValue(partItem.get("Remarks"));

					String variant = partItem.get("Variant");
					XSSFCell cell11 = bodyRow.createCell(11);
					cell11.setCellStyle(defaultCellStyle);
					cell11.setCellValue(variant.toUpperCase().contains("_ECO_") ? "X" : "O");

					XSSFCell cell12 = bodyRow.createCell(12);
					cell12.setCellStyle(defaultCellStyle);
					cell12.setCellValue(variant.toUpperCase().contains("_PLUS_") ? "X" : "O");
				}

				for (LinkedHashMap<String, String> partItem : ecuItem.bootloaderList) {
					XSSFRow bodyRow = spreadsheet.createRow(rowNumber++);

					XSSFCell cell = bodyRow.createCell(0);
					cell.setCellStyle(defaultCellStyle);
					cell.setCellValue(ecuItem.ecuName);

					XSSFCell cell1 = bodyRow.createCell(1);
					cell1.setCellStyle(defaultCellStyle);
					cell1.setCellValue(partItem.get("SWPartType"));

					XSSFCell cell2 = bodyRow.createCell(2);
					cell2.setCellStyle(defaultCellStyle);
					cell2.setCellValue(partItem.get("PartNumber"));

					XSSFCell cell3 = bodyRow.createCell(3);
					cell3.setCellStyle(defaultCellStyle);
					cell3.setCellValue(partItem.get("DIDNo"));

					XSSFCell cell4 = bodyRow.createCell(4);
					cell4.setCellStyle(defaultCellStyle);
					cell4.setCellValue(partItem.get("PartNumber"));

					XSSFCell cell5 = bodyRow.createCell(5);
					cell5.setCellStyle(defaultCellStyle);
					cell5.setCellValue(partItem.get("DIDRev"));

					XSSFCell cell6 = bodyRow.createCell(6);
					cell6.setCellStyle(defaultCellStyle);
					cell6.setCellValue(partItem.get("PartRev"));

					XSSFCell cell7 = bodyRow.createCell(7);
					cell7.setCellStyle(defaultCellStyle);
					cell7.setCellValue(partItem.get("PurchaseLevel"));

					XSSFCell cell8 = bodyRow.createCell(8);
					cell8.setCellStyle(defaultCellStyle);
					cell8.setCellValue(ecuItem.purchasePartNumber + "-" + ecuItem.purchasePartRev);

					XSSFCell cell9 = bodyRow.createCell(9);
					cell9.setCellStyle(defaultCellStyle);
					cell9.setCellValue("");

					XSSFCell cell10 = bodyRow.createCell(10);
					cell10.setCellStyle(defaultCellStyle);
					cell10.setCellValue(partItem.get("Remarks"));

					String variant = partItem.get("Variant");
					XSSFCell cell11 = bodyRow.createCell(11);
					cell11.setCellStyle(defaultCellStyle);
					cell11.setCellValue(variant.toUpperCase().contains("_ECO_") ? "X" : "O");

					XSSFCell cell12 = bodyRow.createCell(12);
					cell12.setCellStyle(defaultCellStyle);
					cell12.setCellValue(variant.toUpperCase().contains("_PLUS_") ? "X" : "O");
				}
			}

			String fileName = "FRS" + "_" + StringExtension.getTimeStamp() + ".xlsx";
			File file = new File(System.getenv("tmp") + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			Desktop.getDesktop().open(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void exportFRS() {
//		try {
//			TCComponentItem objectItem = (TCComponentItem) rootLine.getItem();
//			TCComponentItemRevision objectItemRev = (TCComponentItemRevision) rootLine.getItemRevision();
//
//			String frsNumber = rootLine.getPropertyDisplayableValue("awb0BomLineItemId");
//			String model = "";
//			String year = "";
//			String contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
//			String modelAndYear = contextID.split("-").length > 0 ? contextID.split("-")[0] : "";
//			String[] modelAndYearWords = modelAndYear.split("_");
//			if (modelAndYearWords.length >= 2) {
//				model = modelAndYearWords[0];
//				year = modelAndYearWords[1];
//			}
//			String[] region = null;
//			String frsVersion = "";
//			if (objectItemRev.getType().compareToIgnoreCase("VF3_FRSRevision") == 0) {
//				region = TCExtension.getPropertyRealValues(objectItemRev, "vf4_market_arr");
//				String version = objectItemRev.getPropertyDisplayableValue("vf4_version");
//				String majorVersion = objectItemRev.getPropertyDisplayableValue("vf4_major_version");
//				String minorVersion = objectItemRev.getPropertyDisplayableValue("vf4_minor_version");
//				String hotfix = objectItemRev.getPropertyDisplayableValue("vf3_hotfix_number");
//				frsVersion = String.format("%,.0f", Double.parseDouble(version)) + "." + majorVersion + "." + minorVersion + "." + hotfix;
//			} else {
//				frsVersion = objectItemRev.getPropertyDisplayableValue("object_desc");
//			}
//			String releaseDate = "";
//			String lifeCycle = objectItemRev.getPropertyDisplayableValue("vf4_lifecycle");
//			String technicianInfo = "";
//			String customerInfo = "";
//
//			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
//			Document document = documentBuilder.newDocument();
//
//			Element frsRootEle = document.createElement("frs");
//			document.appendChild(frsRootEle);
//
//			Element frsNumberEle = document.createElement("frsNumber");
//			frsRootEle.appendChild(frsNumberEle);
//			Text frsNumberTextNode = document.createTextNode("FRS" + frsVersion);
//			frsNumberEle.appendChild(frsNumberTextNode);
//
//			Element frsNameEle = document.createElement("frsTCID");
//			frsRootEle.appendChild(frsNameEle);
//			Text frsNameEleTextNode = document.createTextNode(frsNumber);
//			frsNameEle.appendChild(frsNameEleTextNode);
//
//			Element modelEle = document.createElement("model");
//			frsRootEle.appendChild(modelEle);
//			Text modelTextNode = document.createTextNode(model);
//			modelEle.appendChild(modelTextNode);
//
//			Element yearEle = document.createElement("year");
//			frsRootEle.appendChild(yearEle);
//			Text yearTextNode = document.createTextNode(year);
//			yearEle.appendChild(yearTextNode);
//
//			Element marketEle = document.createElement("market");
//			frsRootEle.appendChild(marketEle);
//			Text marketTextNode = document.createTextNode(region != null ? String.join(",", region) : "");
//			marketEle.appendChild(marketTextNode);
//
//			Element lifeCycleEle = document.createElement("lifecycle");
//			frsRootEle.appendChild(lifeCycleEle);
//			Text lifeCycleTextNode = document.createTextNode(lifeCycle);
//			lifeCycleEle.appendChild(lifeCycleTextNode);
//
//			Element techInfoEle = document.createElement("technicianInfo");
//			frsRootEle.appendChild(techInfoEle);
//			Text techInfoTextNode = document.createTextNode(technicianInfo);
//			techInfoEle.appendChild(techInfoTextNode);
//
//			Element cusInfoEle = document.createElement("customerInfo");
//			frsRootEle.appendChild(cusInfoEle);
//			Text cusInfoTextNode = document.createTextNode(customerInfo);
//			cusInfoEle.appendChild(cusInfoTextNode);
//
//			Element releaseDateEle = document.createElement("releaseDate");
//			frsRootEle.appendChild(releaseDateEle);
//			Text releaseDateTextNode = document.createTextNode(releaseDate);
//			releaseDateEle.appendChild(releaseDateTextNode);
//
//			Element ecuListEle = document.createElement("ecuList");
//			frsRootEle.appendChild(ecuListEle);
//			Element currentEcuPartListEle = null;
//
//			String ECU_ID = "";
//			String Part_Type = "";
//
//			LinkedList<TCComponentBOMLine> childs = expandBOMLines(rootLine);
//			for (TCComponentBOMLine line : childs) {
//				LinkedHashMap<String, Set<String>> filesMap = new LinkedHashMap<>();
//				TCComponentItem item = (TCComponentItem) line.getItem();
//				TCComponentItemRevision itemRev = (TCComponentItemRevision) line.getItemRevision();
//
//				Element ecuEle = document.createElement("ecu");
//				ecuListEle.appendChild(ecuEle);
//
//				String ecuId = "";
//				try {
//					ecuId = item.getPropertyDisplayableValue("vf4_ECU_type");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (ecuId.isEmpty())
//					ecuId = line.getPropertyDisplayableValue("bl_rev_object_name");
//
//				Element ecuIdEle = document.createElement("ecuId");
//				ecuEle.appendChild(ecuIdEle);
//				Text ecuIdTextNode = document.createTextNode(ecuId);
//				ecuIdEle.appendChild(ecuIdTextNode);
//				ECU_ID = ecuId;
//
//				String orderNumber = line.getPropertyDisplayableValue("bl_sequence_no");
//				Element orderNumberEle = document.createElement("orderNumber");
//				ecuEle.appendChild(orderNumberEle);
//				Text orderNumberTextNode = document.createTextNode(orderNumber);
//				orderNumberEle.appendChild(orderNumberTextNode);
//
//				Element ecuPartListEle = document.createElement("ecuPartList");
//				ecuEle.appendChild(ecuPartListEle);
//				currentEcuPartListEle = ecuPartListEle;
//
//				if (itemRev != null) {
//					TCComponent[] spec = itemRev.getRelatedComponents("IMAN_specification");
//					for (TCComponent comp : spec) {
//						if (comp.getType().compareToIgnoreCase("Dataset") == 0) {
//							TCComponentDataset dataset = (TCComponentDataset) comp;
//							TCComponent[] namedRefs = dataset.getNamedReferences();
//							for (TCComponent nameRef : namedRefs) {
//								if (nameRef instanceof TCComponentTcFile) {
//									String fileName = ((TCComponentTcFile) nameRef).getProperty("original_file_name");
//									String partNumber = "";
//									if (!fileName.contains("-")) {
//										continue;
//									} else {
//										partNumber = fileName.split("-")[0];
//									}
//
//									if (filesMap.containsKey(partNumber)) {
//										filesMap.get(partNumber).add(fileName);
//									} else {
//										Set<String> list = new HashSet<>();
//										list.add(fileName);
//										filesMap.put(fileName, list);
//									}
//								}
//							}
//						}
//					}
//				}
//				LinkedList<TCComponentBOMLine> bomLineChild = expandBOMLines(line);
//				for (TCComponentBOMLine childLine : bomLineChild) {
//					Element ecuPartEle = document.createElement("ecuPart");
//					currentEcuPartListEle.appendChild(ecuPartEle);
//
//					String partType = childLine.getPropertyDisplayableValue("vf4_software_part_type");
//					String variant = childLine.getPropertyDisplayableValue("bl_formula");
//					String traceability = "";
//					String partNumber = childLine.getPropertyDisplayableValue("bl_item_item_id");
//
//					Element partTypeEle = document.createElement("partType");
//					Text partTypeTextNode = document.createTextNode(partType);
//					partTypeEle.appendChild(partTypeTextNode);
//					ecuPartEle.appendChild(partTypeEle);
//					Part_Type = partType;
//
//					Element variantEle = document.createElement("variant");
//					Text variantTextNode = document.createTextNode(variant);
//					variantEle.appendChild(variantTextNode);
//					ecuPartEle.appendChild(variantEle);
//
//					Element traceabilityEle = document.createElement("traceability");
//					Text traceabilityTextNode = document.createTextNode(traceability);
//					traceabilityEle.appendChild(traceabilityTextNode);
//					ecuPartEle.appendChild(traceabilityEle);
//
//					Element partNumberEle = document.createElement("partNumber");
//					Text partNumberTextNode = document.createTextNode(partNumber);
//					partNumberEle.appendChild(partNumberTextNode);
//					ecuPartEle.appendChild(partNumberEle);
//
//					Element filesEle = document.createElement("files");
//					ecuPartEle.appendChild(filesEle);
//
//					String revision = childLine.getPropertyDisplayableValue("VF4_supplier_rev").trim().replaceAll("\r", "").replaceAll("\n", "");
//					if (revision.isEmpty())
//						revision = childLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
//
//					if (filesMap.size() > 0 && filesMap.containsKey(partNumber)) {
//						Set<String> datasets = filesMap.get(partNumber);
//						for (String fileName : datasets) {
//							if (fileName.trim().length() > 0) {
//								Element fileEle = document.createElement("file");
//								filesEle.appendChild(fileEle);
//
//								Element revisionEle = document.createElement("revision");
//								Text revisionTextNode = document.createTextNode(revision);
//								revisionEle.appendChild(revisionTextNode);
//								fileEle.appendChild(revisionEle);
//
//								Element fileNameEle = document.createElement("fileName");
//								Text fileNameTextNode = document.createTextNode(fileName);
//								fileNameEle.appendChild(fileNameTextNode);
//								fileEle.appendChild(fileNameEle);
//							}
//						}
//					} else {
//						Element fileEle = document.createElement("file");
//						filesEle.appendChild(fileEle);
//
//						Element revisionEle = document.createElement("revision");
//						Text revisionTextNode = document.createTextNode(revision);
//						revisionEle.appendChild(revisionTextNode);
//						fileEle.appendChild(revisionEle);
//
//						Element fileNameEle = document.createElement("fileName");
//						Text fileNameTextNode = document.createTextNode("");
//						fileNameEle.appendChild(fileNameTextNode);
//						fileEle.appendChild(fileNameEle);
//					}
//				}
//			}
//
//			String xmlFileName = "frs_" + frsNumber + "_" + java.util.UUID.randomUUID() + ".xml";
//			String xmlFilePath = "C:\\temp\\" + xmlFileName;
//			TransformerFactory transformerFactory = TransformerFactory.newInstance();
//			Transformer transformer = transformerFactory.newTransformer();
//			DOMSource domSource = new DOMSource(document);
//			StreamResult streamResult = new StreamResult(new File(xmlFilePath));
//			transformer.transform(domSource, streamResult);
//
//			dlg.setMessage("Export success to C:\\temp folder.", IMessageProvider.INFORMATION);
//		} catch (Exception e) {
//			e.printStackTrace();
//			dlg.setMessage("Export unsuccessfull. Exception: " + e.getMessage(), IMessageProvider.ERROR);
//		}
//	}

	private LinkedList<TCComponentBOMLine> expandBOMLines(TCComponentBOMLine rootLine) {
		LinkedList<TCComponentBOMLine> output = new LinkedList<TCComponentBOMLine>();
		StructureManagementService structureService = StructureManagementService.getService(session);
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];
		ExpandPSOneLevelResponse levelResp = structureService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0) {
			for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					output.add(psData.bomLine);
				}
			}
		}
		return output;
	}
}
