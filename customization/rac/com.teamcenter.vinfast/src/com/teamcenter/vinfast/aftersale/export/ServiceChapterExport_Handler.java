package com.teamcenter.vinfast.aftersale.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ServiceChapterExport_Handler extends AbstractHandler {
	private static TCSession session = null;
	private static DataManagementService dmService = null;
	private ServiceChapterExport_Dialog dlg = null;

	private TCComponentBOMLine selectObject = null;
	private static CloudBlobContainer blobContainer;

	private final String REPORT_PREFIX = "Service_Chapter_";
	private final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=vfstpblob;AccountKey=T5k21GE1dYFv1vfy1R9MTrsmVwh2IpaD3g8J2rSDraZYrEKNceyBo+C1MXsyfmOKqWs28610kuRP+ASte03FKA==;EndpointSuffix=core.windows.net;";

	private LinkedList<TCComponentBOMLine> parentBom;

	private static String TEMP_DIR;

	private static LinkedHashMap<String, LinkedHashMap<String, String>> mapMasterBOMLineInfo = null;
	private HashMap<String, TCComponentTcFile> filesMap = null;

	private LinkedHashMap<String, String> BOM_PROP = null;
	private String modelYear = "";

	public ServiceChapterExport_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please Select Service Chapter to export.", "Error", MessageBox.ERROR);
			return null;
		}

		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
		TEMP_DIR = System.getenv("tmp");

		// init data
		filesMap = new HashMap<String, TCComponentTcFile>();
		mapMasterBOMLineInfo = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		parentBom = new LinkedList<TCComponentBOMLine>();
		BOM_PROP = new LinkedHashMap<String, String>();

		for (String singleValue : TCExtension.GetPreferenceValues("VINFAST_SBOM_VIEW_CONFIG", session)) {
			String[] strSplit = singleValue.split("==");
			if (strSplit.length == 2) {
				BOM_PROP.put(strSplit[0], strSplit[1]);
			} else {
				BOM_PROP.put(strSplit[0], "");
			}
		}
		String contextID = "";
		getParent(selectObject);
		if (parentBom.size() > 0) {
			try {
				TCComponentItem objectItem = parentBom.getLast().getItem();
				contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				TCComponentItem objectItem = selectObject.getItem();
				contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// init UI
		try {
			dlg = new ServiceChapterExport_Dialog(new Shell());
			dlg.create();

			dlg.txtModel.setText(contextID.split("-")[0]);
			dlg.txtID.setText(selectObject.getPropertyDisplayableValue("bl_indented_title"));
			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					exportData();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void connectAzureStorage() {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(CONNECTION_STRING);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			String path = "ecn";
			if (!dlg.txtModel.getText().isEmpty())
				path += "/" + dlg.txtModel.getText();
			blobContainer = blobClient.getContainerReference(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exportData() {
		TEMP_DIR = System.getenv("tmp") + "/" + System.currentTimeMillis() + "/";
		File theDir = new File(TEMP_DIR);
		theDir.mkdirs();

		LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
		expandBOMLines(allBOMLineInWindow, selectObject);
		try {
			for (int j = parentBom.size() - 1; j >= 0; j--) {
				collectData((TCComponentBOMLine) parentBom.get(j));
			}

			Set<String> propertyAdding = new HashSet<>(
					Arrays.asList(new String[] { "bl_item_vf4_itm_after_sale_relevant", "bl_item_item_id", "bl_rev_item_revision_id", "bl_sequence_no", "bl_parent", "bl_item_vl5_color_code", "bl_quantity", "bl_item_object_type", "bl_revision", "VF4_effective_date", "VF4_valid_till_date" }));
			propertyAdding.addAll(BOM_PROP.keySet());
			dmService.getProperties(allBOMLineInWindow.toArray(new TCComponentBOMLine[0]), propertyAdding.toArray(new String[0]));
			Date today = new Date();
			int levelVFSC = 0;
			DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			for (TCComponentBOMLine bomline : allBOMLineInWindow) {
				int level = Integer.parseInt(bomline.getPropertyDisplayableValue("bl_level_starting_0"));
				String relevant = bomline.getPropertyDisplayableValue("bl_item_vf4_itm_after_sale_relevant");
				String objectType = bomline.getPropertyDisplayableValue("bl_item_object_type");
				Date effectiveDate = null;
				try {
					effectiveDate = df.parse(bomline.getPropertyDisplayableValue("VF4_effective_date"));
				} catch (Exception ignored) {
				}
				Date validTilDate = null;
				try {
					validTilDate = df.parse(bomline.getPropertyDisplayableValue("VF4_valid_till_date"));
				} catch (Exception ignored) {
				}

				boolean check = true;

				if (objectType.compareToIgnoreCase("Service Chapter") == 0) {
					levelVFSC = level;
				} else {
					if (level > levelVFSC + 1)
						continue;

					if (relevant.compareTo("True") != 0)
						continue;

					check = false;
					if (effectiveDate == null && validTilDate == null) {
						check = true;
					} else {
						if (effectiveDate == null) {
							if (validTilDate.after(today))
								check = true;
						} else if (validTilDate == null) {
							if (effectiveDate.after(today))
								check = true;
						} else {
							if (effectiveDate.after(today) && validTilDate.after(today))
								check = true;
						}
					}
				}

				if (!check)
					continue;

				String bomlineID = bomline.getProperty("bl_item_item_id");
				String revid = bomline.getPropertyDisplayableValue("bl_rev_item_revision_id");
				String seqNo = bomline.getPropertyDisplayableValue("bl_sequence_no");
				String parentPart = bomline.getPropertyDisplayableValue("bl_parent");

				LinkedHashMap<String, String> objectItem = mapMasterBOMLineInfo.get(bomlineID + "-" + revid + "-" + seqNo + "-" + parentPart);
				if (objectItem == null) {
					LinkedHashMap<String, String> bomLineInfo = new LinkedHashMap<String, String>();
					for (Map.Entry<String, String> entry : BOM_PROP.entrySet()) {
						if (!entry.getValue().isEmpty()) {
							bomLineInfo.put(entry.getKey(), bomline.getPropertyDisplayableValue(entry.getValue()));
						} else {
							String keyString = entry.getKey();
							if (keyString.compareToIgnoreCase("Year") == 0) {
								bomLineInfo.put(keyString, modelYear);
							} else if (keyString.compareToIgnoreCase("Color Information") == 0) {
								bomLineInfo.put(keyString, getColorInformation(bomline.getStringProperty("bl_item_item_id"), bomline.getPropertyDisplayableValue("bl_item_vl5_color_code")));
							} else if (keyString.compareToIgnoreCase("Type") == 0) {
								if (objectType.compareToIgnoreCase("Service Chapter") != 0) {
									bomLineInfo.put(keyString, "Service Part");
								} else {
									bomLineInfo.put(keyString, "Service Chapter");
								}
							} else {
								bomLineInfo.put(entry.getKey(), "");
							}
						}
					}

					if (!filesMap.containsKey(bomlineID)) {
						TCComponentTcFile file = getImage(bomline.getItemRevision());
						if (file != null) {
							bomLineInfo.put("Figure No", bomlineID);
							filesMap.put(bomlineID, file);
						}
					}

					mapMasterBOMLineInfo.put(bomlineID + "-" + revid + "-" + seqNo + "-" + parentPart, bomLineInfo);
				} else {
					String quantity = objectItem.get("Qty");
					if (quantity.isEmpty()) {
						objectItem.put("Qty", bomline.getPropertyDisplayableValue("bl_quantity"));
					} else {
						try {
							objectItem.put("Qty", String.valueOf(Double.parseDouble(quantity) + Double.parseDouble(bomline.getPropertyDisplayableValue("bl_quantity"))));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		excelPublishReport();
	}

	private void collectData(TCComponentBOMLine bomline) {
		try {
			String relevant = bomline.getPropertyDisplayableValue("bl_item_vf4_itm_after_sale_relevant");
			String objectType = bomline.getPropertyDisplayableValue("bl_item_object_type");
			if (objectType.compareToIgnoreCase("Service Chapter") != 0) {
				if (relevant.compareTo("True") != 0)
					return;
			}

			String bomlineID = bomline.getProperty("bl_item_item_id");
			String revid = bomline.getPropertyDisplayableValue("bl_rev_item_revision_id");
			String seqNo = bomline.getPropertyDisplayableValue("bl_sequence_no");
			String parentPart = bomline.getPropertyDisplayableValue("bl_parent");

			LinkedHashMap<String, String> objectItem = mapMasterBOMLineInfo.get(bomlineID + "-" + revid + "-" + seqNo + "-" + parentPart);
			if (objectItem == null) {
				LinkedHashMap<String, String> bomLineInfo = new LinkedHashMap<String, String>();
				for (Map.Entry<String, String> entry : BOM_PROP.entrySet()) {
					if (!entry.getValue().isEmpty()) {
						bomLineInfo.put(entry.getKey(), bomline.getPropertyDisplayableValue(entry.getValue()));
					} else {
						String keyString = entry.getKey();
						if (keyString.compareToIgnoreCase("Year") == 0) {
							bomLineInfo.put(keyString, modelYear);
						} else if (keyString.compareToIgnoreCase("Color Information") == 0) {
							bomLineInfo.put(keyString, getColorInformation(bomline.getStringProperty("bl_item_item_id"), bomline.getPropertyDisplayableValue("bl_item_vl5_color_code")));
						} else if (keyString.compareToIgnoreCase("Type") == 0) {
							if (objectType.compareToIgnoreCase("Service Chapter") != 0) {
								bomLineInfo.put(keyString, "Service Part");
							} else {
								bomLineInfo.put(keyString, "Service Chapter");
							}
						} else {
							bomLineInfo.put(entry.getKey(), "");
						}
					}
				}

				if (!filesMap.containsKey(bomlineID)) {
					TCComponentTcFile file = getImage(bomline.getItemRevision());
					if (file != null) {
						bomLineInfo.put("Figure No", bomlineID);
						filesMap.put(bomlineID, file);
					}
				}

				mapMasterBOMLineInfo.put(bomlineID + "-" + revid + "-" + seqNo + "-" + parentPart, bomLineInfo);
			} else {
				String quantity = objectItem.get("Qty");
				if (quantity.isEmpty()) {
					objectItem.put("Qty", bomline.getPropertyDisplayableValue("bl_quantity"));
				} else {
					try {
						objectItem.put("Qty", String.valueOf(Double.parseDouble(quantity) + Double.parseDouble(bomline.getPropertyDisplayableValue("bl_quantity"))));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void excelPublishReport() {
		try {
			@SuppressWarnings("resource")

			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet spreadsheetBOM = wb.createSheet("SBOM");
			HSSFSheet spreadsheetFR = wb.createSheet("FR");
			HSSFSheet spreadsheetDE = wb.createSheet("DE");
			HSSFSheet spreadsheetNL = wb.createSheet("NL");

			CellStyle headerCellStyle = wb.createCellStyle();

			HSSFRow headerRowBOM = spreadsheetBOM.createRow(0);
			int cellCounter = 0;
			for (Map.Entry<String, String> entry : BOM_PROP.entrySet()) {
				if (entry.getKey().compareTo("Part Name French") == 0 || entry.getKey().compareTo("Part Name German") == 0 || entry.getKey().compareTo("Part Name Dutch") == 0)
					continue;

				setCell(cellCounter, headerRowBOM, entry.getKey(), headerCellStyle);
				cellCounter++;
			}

			HSSFRow headerRowFR = spreadsheetFR.createRow(0);
			setCell(0, headerRowFR, "Item ID", headerCellStyle);
			setCell(1, headerRowFR, "Part Name", headerCellStyle);
			setCell(2, headerRowFR, "Note", headerCellStyle);

			HSSFRow headerRowDE = spreadsheetDE.createRow(0);
			setCell(0, headerRowDE, "Item ID", headerCellStyle);
			setCell(1, headerRowDE, "Part Name", headerCellStyle);
			setCell(2, headerRowDE, "Note", headerCellStyle);

			HSSFRow headerRowNL = spreadsheetNL.createRow(0);
			setCell(0, headerRowNL, "Item ID", headerCellStyle);
			setCell(1, headerRowNL, "Part Name", headerCellStyle);
			setCell(2, headerRowNL, "Note", headerCellStyle);

			CellStyle bodyCellStyle = wb.createCellStyle();

			int rowCounter = 1;
			for (Map.Entry<String, LinkedHashMap<String, String>> entry : mapMasterBOMLineInfo.entrySet()) {
				LinkedHashMap<String, String> bomLineInfo = (LinkedHashMap<String, String>) entry.getValue();
				HSSFRow bodyRowBOM = spreadsheetBOM.createRow(rowCounter);
				cellCounter = 0;
				for (Map.Entry<String, String> bomLine : bomLineInfo.entrySet()) {
					if (bomLine.getKey().compareTo("Part Name French") == 0 || bomLine.getKey().compareTo("Part Name German") == 0 || bomLine.getKey().compareTo("Part Name Dutch") == 0)
						continue;

					String dispItm = bomLine.getValue();
					setCell(cellCounter, bodyRowBOM, dispItm, bodyCellStyle);
					cellCounter++;
				}

				HSSFRow bodyRowFR = spreadsheetFR.createRow(rowCounter);
				setCell(0, bodyRowFR, bomLineInfo.get("Item ID"), bodyCellStyle);
				setCell(1, bodyRowFR, bomLineInfo.get("Part Name French"), bodyCellStyle);
				setCell(2, bodyRowFR, "", bodyCellStyle);

				HSSFRow bodyRowDE = spreadsheetDE.createRow(rowCounter);
				setCell(0, bodyRowDE, bomLineInfo.get("Item ID"), bodyCellStyle);
				setCell(1, bodyRowDE, bomLineInfo.get("Part Name German"), bodyCellStyle);
				setCell(2, bodyRowDE, "", bodyCellStyle);

				HSSFRow bodyRowNL = spreadsheetNL.createRow(rowCounter);
				setCell(0, bodyRowNL, bomLineInfo.get("Item ID"), bodyCellStyle);
				setCell(1, bodyRowNL, bomLineInfo.get("Part Name Dutch"), bodyCellStyle);
				setCell(2, bodyRowNL, "", bodyCellStyle);

				rowCounter++;
			}

			String topBOM = "";
			try {
				topBOM = selectObject.getPropertyDisplayableValue("bl_item_item_id");
			} catch (Exception e) {
				e.printStackTrace();
			}

			String fileName = REPORT_PREFIX + topBOM + "_" + StringExtension.getTimeStamp() + ".xls";
			File file = new File(TEMP_DIR + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
//			Desktop.getDesktop().open(file);
			connectAzureStorage();
			uploadToCloud(file, fileName);
			processImageFile();
			dlg.setMessage("The upload is completed successfully.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			dlg.setMessage("The upload unsuccess. Exception: " + e.toString(), IMessageProvider.ERROR);
		}
	}

	private TCComponentTcFile getImage(TCComponent object) throws Exception {
		TCComponent[] attachedObjects = object.getRelatedComponents("IMAN_specification");
		for (TCComponent comp : attachedObjects) {
			if (comp.getType().compareTo("Image") == 0) {
				TCComponentDataset dataset = (TCComponentDataset) comp;
				TCComponent[] namedRefs = dataset.getNamedReferences();
				for (int i = 0; i < dataset.getNamedReferences().length; i++) {
					TCComponentTcFile tcFile = (TCComponentTcFile) namedRefs[i];
					if (tcFile.toString().toLowerCase().contains(".png")) {
						return tcFile;
					}
				}
			}
		}
		return null;
	}

	private void processImageFile() {
		for (Map.Entry<String, TCComponentTcFile> value : filesMap.entrySet()) {
			try {
				File file = value.getValue().getFile(TEMP_DIR, value.getKey());
				uploadToCloud(file, value.getKey() + ".png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean uploadToCloud(File file, String _FileName) {
		try {
			String fileName = "";
			fileName = _FileName.isEmpty() ? file.getName() : _FileName;
			CloudBlockBlob blob = blobContainer.getBlockBlobReference(fileName);
			blob.upload(new FileInputStream(file), file.length());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void setCell(int index, HSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		HSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	private String getColorInformation(String objectItemId, String colorCode) {
		String color = "";
		String input = "";
		if (!objectItemId.isEmpty()) {
			if (objectItemId.substring(0, 3).compareToIgnoreCase("SVC") == 0) {
				input = colorCode;
			} else {
				if (objectItemId.length() >= 14) {
					input = objectItemId.substring(11, 13);
				}
			}
		}
		if (!input.isEmpty()) {
			if (input.length() > 2)
				input = input.substring(0, 2);
			switch (input) {
			case "BK":
			case "BA":
				color = "BLACK/Ã„ï¿½EN";
				break;
			case "BL":
			case "BU":
				color = "BLUE/XANH NÃ†Â¯Ã¡Â»Å¡C BIÃ¡Â»â€šN";
				break;
			case "BR":
				color = "BROWN/NÃƒâ€šU";
				break;
			case "GN":
				color = "GREEN/XANH LÃƒï¿½ CÃƒâ€šY";
				break;
			case "GY":
				color = "GREY/XÃƒï¿½M";
				break;
			case "YL":
				color = "YELLOW/VÃƒâ‚¬NG";
				break;
			case "OR":
				color = "ORANGE/CAM";
				break;
			case "RE":
				color = "RED/Ã„ï¿½Ã¡Â»Å½";
				break;
			case "SI":
				color = "SILVER/BÃ¡ÂºÂ C";
				break;
			case "WH":
				color = "WHITE/TRÃ¡ÂºÂ®NG";
				break;
			case "PR":
				color = "PURPLE/TÃƒï¿½M";
				break;
			case "BG":
				color = "BEIGE/XÃƒï¿½M TRO";
				break;
			case "CO":
				color = "COPPER/Ã„ï¿½Ã¡Â»â€™NG";
				break;
			case "SP":
				color = "V8 SPECIAL/V8 SPECIAL";
				break;
			case "XB":
				color = "BI-COLORS/BI-COLORS";
				break;
			case "DC":
				color = "CHROME/CHROME";
				break;
			case "DH":
				color = "HYDROGRAPHY/HYDROGRAPHY";
				break;
			case "DG":
				color = "WOOD/GÃ¡Â»â€“";
				break;
			case "DA":
				color = "ALUMINUM/NHÃƒâ€�M";
				break;
			case "DF":
				color = "CARBON FIBER/SÃ¡Â»Â¢I CARBON";
				break;
			}
		}
		return color;
	}

	private void getParent(TCComponentBOMLine bom_line_parent2) {
		try {
			TCComponent parent = bom_line_parent2.getReferenceProperty("bl_parent");
			TCComponentBOMLine bom_line_parent = (TCComponentBOMLine) parent;
			if (bom_line_parent != null) {
				if (bom_line_parent.getProperty("bl_level_starting_0").equals("0")) {
					parentBom.add(bom_line_parent);
					getModelYear(bom_line_parent);
				} else {
					parentBom.add(bom_line_parent);
					getParent(bom_line_parent);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void getModelYear(TCComponentBOMLine topBom) {
		try {
			TCComponentItem itemObj = topBom.getItem();
			String configurationContext = itemObj.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
			if (configurationContext.contains("-")) {
				String[] str = configurationContext.split("-");
				if (str[0].contains("_")) {
					String[] str1 = str[0].split("_");
					if (str1.length > 1) {
						modelYear = str1[1];
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length != 1)
			return true;

		if (targetComponents[0] instanceof TCComponentBOMLine) {
			selectObject = (TCComponentBOMLine) targetComponents[0];
			String type = "";
			try {
				type = selectObject.getPropertyDisplayableValue("bl_item_object_type");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (type.compareToIgnoreCase("Service Chapter") == 0)
				return false;
		}
		return true;
	}

	private static void expandBOMLines(LinkedList<TCComponentBOMLine> outBomLines, TCComponentBOMLine rootLine) {
		try {
			outBomLines.add(rootLine);
			if (rootLine.getChildrenCount() > 0) {
				AIFComponentContext[] aifChilLines = rootLine.getChildren();
				for (AIFComponentContext aifChilLine : aifChilLines) {
					expandBOMLines(outBomLines, (TCComponentBOMLine) aifChilLine.getComponent());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
