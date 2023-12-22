package com.teamcenter.integration.report;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;

public class StationReport_Handler extends AbstractHandler {
	private TCSession session;
	private static DataManagementService dmService = null;
	private StationReport_Dialog dlg;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private TCComponentBOMLine selectObject = null;
	private static LinkedList<LinkedHashMap<String, String>> mapMasterBOMLineInfo = null;
	private LinkedHashMap<String, String> BOP_PROP = new LinkedHashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("BOM Line", "bl_indented_title");
			put("Line", "");
			put("Station", "");
			put("Part Number", "awb0BomLineItemId");
			put("Quantity", "bl_quantity");
			put("Part Name", "bl_item_object_name");
			put("UoM", "bl_uom");
			put("Purchase Level", "VL5_purchase_lvl_vf");
			put("Level", "bl_level_starting_0");
			put("Object Type", "bl_item_object_type");
		}
	};

	public StationReport_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			selectObject = (TCComponentBOMLine) targetComponents[0];

			dmService = DataManagementService.getService(session);

			dlg = new StationReport_Dialog(new Shell());
			dlg.create();

			dlg.txtTOP.setText(selectObject.getPropertyDisplayableValue("object_string"));
			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					exportReport();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void exportReport() {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Export Report...", IProgressMonitor.UNKNOWN);
					try {
						mapMasterBOMLineInfo = new LinkedList<>();
						LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
						expandBOMLines(allBOMLineInWindow, selectObject);

						Set<String> propertyAdding = new HashSet<>(Arrays.asList(new String[] { "bl_item_vf4_itm_after_sale_relevant", "bl_item_item_id", "bl_rev_item_revision_id", "bl_sequence_no", "bl_parent", "bl_item_vl5_color_code", "bl_quantity", "bl_item_object_type", "bl_revision" }));
						propertyAdding.addAll(BOP_PROP.values());
						dmService.getProperties(allBOMLineInWindow.toArray(new TCComponentBOMLine[0]), propertyAdding.toArray(new String[0]));
						String station = "";
						for (TCComponentBOMLine bomLine : allBOMLineInWindow) {
							try {
								String objectType = bomLine.getPropertyDisplayableValue("bl_item_object_type");
								String itemName = bomLine.getPropertyDisplayableValue("bl_item_object_name");
								if (objectType.compareTo("Process Station") == 0) {
									station = itemName;
								} else {
									if (objectType.compareTo("Process Line") != 0 && objectType.compareTo("Station") != 0 && objectType.compareTo("Operation") != 0) {
										LinkedHashMap<String, String> bomLineInfo = new LinkedHashMap<String, String>();
										for (Map.Entry<String, String> entry : BOP_PROP.entrySet()) {
											String tmp = "";
											if (entry.getKey().compareTo("Station") == 0)
												tmp = station;
											if (!entry.getValue().isEmpty())
												tmp = bomLine.getPropertyDisplayableValue(entry.getValue());
											bomLineInfo.put(entry.getKey(), tmp);
										}
										mapMasterBOMLineInfo.add(bomLineInfo);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			excelPublishReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void excelPublishReport() {
		try {
			File templateFile = downloadTemplateFile();

			if (templateFile == null) {
				MessageBox.post("Can not get Template file. Please contact your administrator for further instructions with below messages.", "Export ECR Information failed", MessageBox.ERROR);
				return;
			}

			InputStream outputFile = new FileInputStream(templateFile);
			XSSFWorkbook wb = new XSSFWorkbook(outputFile);
			templateFile.setWritable(true);

			XSSFSheet spreadsheet = wb.getSheet("BOM");

			// Body
			int cellCounter = 0;
			XSSFCellStyle bodyCellStyle = wb.createCellStyle();
			int rowCounter = 1;
			for (LinkedHashMap<String, String> bomLineInfo : mapMasterBOMLineInfo) {
				XSSFRow bodyRow = spreadsheet.createRow(rowCounter++);
				cellCounter = 0;
				for (Map.Entry<String, String> bomLine : bomLineInfo.entrySet()) {
					String dispItm = bomLine.getValue();
					if (bomLine.getKey().compareTo("Quantity") == 0) {
						if (StringExtension.isDouble(dispItm))
							setCell(cellCounter, bodyRow, Double.parseDouble(dispItm), bodyCellStyle);
						else
							setCell(cellCounter, bodyRow, "", bodyCellStyle);
					} else {
						setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
					}

					cellCounter++;
				}
			}

			outputFile.close();

//			String fileName = REPORT_PREFIX + topBOM + "_" + StringExtension.getTimeStamp() + ".xls";
//			File file = new File(System.getenv("tmp") + fileName);
			FileOutputStream fos = new FileOutputStream(templateFile);
			wb.write(fos);
			fos.close();
			wb.close();
			Desktop.getDesktop().open(templateFile);
			dlg.setMessage("Report export completed successfully.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			dlg.setMessage("Report export unsuccess. Exception: " + e.toString(), IMessageProvider.ERROR);
		}
	}

	private void setCell(int index, XSSFRow headerRow, String displayValue, XSSFCellStyle headerCellStyle) {
		XSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	private void setCell(int index, XSSFRow headerRow, Double displayValue, XSSFCellStyle headerCellStyle) {
		XSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
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

	private File downloadTemplateFile() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", "VF_Station_Report_Template");
		parameter.put("Dataset Type", "MS ExcelX");
		parameter.put("Owning Group", "dba");

		TCComponent[] item_list = TCExtension.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0)
			return TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", getExcelName() + ".xlsm", session);

		return null;
	}

	private String getExcelName() {
		String output = "Station_Report_";
		try {
			output += TCExtension.getCurrentTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
