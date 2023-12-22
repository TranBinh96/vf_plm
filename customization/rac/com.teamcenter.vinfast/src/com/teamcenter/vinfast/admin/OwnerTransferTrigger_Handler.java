package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.OwnerTransferModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class OwnerTransferTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private OwnerTransferTrigger_Dialog dlg;
	private TCComponent selectedObject = null;
	private List<OwnerTransferModel> dataList;
	private LinkedHashMap<String, LinkedHashMap<String, String>> reportList;

	private static String GROUP_PERMISSION = "dba";

	public OwnerTransferTrigger_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			if (!CheckPermission()) {
				MessageBox.post("You are not authorized to baseline.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			dataList = new LinkedList<OwnerTransferModel>();

			// Init data

			// Init UI
			dlg = new OwnerTransferTrigger_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Owner Transfer Trigger");
			dlg.setMessage("Define business object create information");

			dlg.btnOpenFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
					fileDialog.setText("Open");
					fileDialog.setFilterPath("C:/");
					String[] filterExt = { "*.xlsx", "*.xls" };
					fileDialog.setFilterExtensions(filterExt);
					ReadExcelFile(fileDialog.open());
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ActionProcess();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void ReadExcelFile(String path) {
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int cellCount = 0;
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					cellCount = 0;
					if (rowCount > 1) {
						OwnerTransferModel newItem = new OwnerTransferModel(session);
						for (Cell cell : row) {
							cellCount++;
							if (cellCount == 1)
								newItem.setPartID(cell.getStringCellValue());
							else if (cellCount == 2)
								newItem.setUserID(cell.getStringCellValue());
							else
								newItem.setGroupID(cell.getStringCellValue());
						}
						newItem.CheckReviewer();
						newItem.CheckReferenced();
						dataList.add(newItem);
					}
				}
				dlg.setMessage("Import excel file success", IMessageProvider.INFORMATION);
			} catch (Exception e) {

			}
		}
	}

	private void ActionProcess() {
		List<String> itemList = new LinkedList<String>();
		for (OwnerTransferModel data : dataList) {
			if (data.GetReviewer() != null) {
				reportList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
				try {
					LinkedList<TCComponentBOMLine> allTopLineInWindow = new LinkedList<>();
					if (data.GetBOPTop() != null) {
						allTopLineInWindow = TCExtension.expandAllBOMLines(data.GetBOPTop(), session);
						HashSet<String> uniqueID = new HashSet<>();
						String partNumber = "";
						for (TCComponentBOMLine bomLine : allTopLineInWindow) {
							partNumber = bomLine.getPropertyDisplayableValue("bl_item_item_id");
							if (uniqueID.contains(partNumber))
								continue;
							uniqueID.add(partNumber);
							String[] types = bomLine.getItemRevision().getClassNameHierarchy();
//							String[] types = { bomLine.getItemRevision().getClassType() };
							if (types != null) {
								if (ValidTrigger(types)) {
									LinkedHashMap<String, String> reportItem = new LinkedHashMap<String, String>();
									reportItem.put("Part Number", partNumber);
									reportItem.put("Revision", bomLine.getPropertyDisplayableValue("bl_rev_item_revision_id"));
									reportItem.put("Bom Type", "BOP");

									itemList.add(bomLine.getItem().getUid());
									reportList.put(partNumber, reportItem);
								}
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e.toString());
				}

				try {
					LinkedList<TCComponentBOMLine> allTopLineInWindow = new LinkedList<>();
					if (data.GetPlantModelTop() != null) {
						allTopLineInWindow = TCExtension.expandAllBOMLines(data.GetPlantModelTop(), session);
						HashSet<String> uniqueID = new HashSet<>();
						String partNumber = "";
						for (TCComponentBOMLine bomLine : allTopLineInWindow) {
							partNumber = bomLine.getPropertyDisplayableValue("bl_item_item_id");
							if (uniqueID.contains(partNumber))
								continue;
							uniqueID.add(partNumber);
							String[] types = bomLine.getItemRevision().getClassNameHierarchy();
//							String[] types = { bomLine.getItemRevision().getClassType() };
							if (types != null) {
								if (ValidTrigger(types)) {
									LinkedHashMap<String, String> reportItem = new LinkedHashMap<String, String>();
									reportItem.put("Part Number", partNumber);
									reportItem.put("Revision", bomLine.getPropertyDisplayableValue("bl_rev_item_revision_id"));
									reportItem.put("Bom Type", "Plant Model");

									itemList.add(bomLine.getItem().getUid());
									reportList.put(partNumber, reportItem);
								}
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e.toString());
				}
				if (itemList.size() > 0) {
					try {
						List<TCComponentGroupMember> assignList = new LinkedList<TCComponentGroupMember>();
						assignList.add(data.GetReviewer());
						String errorMes = TriggerPart(itemList.toArray(new String[0]), assignList);
						if (errorMes.isEmpty())
							dlg.setMessage("Trigger process success", IMessageProvider.INFORMATION);
						else
							dlg.setMessage(errorMes, IMessageProvider.ERROR);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				ExportExcelReport();
			}
		}
	}

	private String TriggerPart(String[] partUID, List<TCComponentGroupMember> assignList) throws Exception {
		ServiceData process = TCExtension.TriggerProcess(partUID, "VF_IT_Change_Ownership_MBOM_BOP", "", "Trigger by system", session);
		if (process.sizeOfPartialErrors() > 0) {
			return "Trigger process errorr";
		} else {
			if (process.getCreatedObject(0) != null) {
				boolean check = TCExtension.AssignPerformer((TCComponentProcess) process.getCreatedObject(0), "Select Target Owner", assignList, session);
				if (!check) {
					return "Assign error";
				}
			}
		}
		return "";
	}

	private boolean ValidTrigger(String[] types) {
		for (String type : types) {
			if (type.compareToIgnoreCase("MEWorkareaRevision") == 0 || type.compareToIgnoreCase("Mfg0MEPlantBOPRevision") == 0 || type.compareToIgnoreCase("Mfg0MEProcLineRevision") == 0 || type.compareToIgnoreCase("Mfg0MEProcStatnRevision") == 0 || type.compareToIgnoreCase("MEWorkareaRevision") == 0 || type.compareToIgnoreCase("MEOPRevision") == 0) {
				return true;
			}
		}
		return false;
	}

	private void ExportExcelReport() {
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet spreadsheet = wb.createSheet("BOM");

			int cellCounter = 0;
			// Header
			CellStyle headerCellStyle = wb.createCellStyle();
			headerCellStyle.setBorderTop(BorderStyle.THIN);
			headerCellStyle.setBorderBottom(BorderStyle.THIN);
			headerCellStyle.setBorderLeft(BorderStyle.THIN);
			headerCellStyle.setBorderRight(BorderStyle.THIN);

			XSSFRow headerRow = spreadsheet.createRow(0);
			cellCounter = 0;
			String[] reportHeader = { "Part Number", "Revision", "Bom Type", "Error" };
			for (String value : reportHeader) {
				setCell(cellCounter, headerRow, value, headerCellStyle);
				cellCounter++;
			}

			// Body
			XSSFCellStyle cellStyleOrange = wb.createCellStyle();
			cellStyleOrange.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyleOrange.setBorderTop(BorderStyle.THIN);
			cellStyleOrange.setBorderBottom(BorderStyle.THIN);
			cellStyleOrange.setBorderLeft(BorderStyle.THIN);
			cellStyleOrange.setBorderRight(BorderStyle.THIN);
			cellStyleOrange.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), new DefaultIndexedColorMap()));

			CellStyle bodyCellStyle = wb.createCellStyle();
			bodyCellStyle.setBorderTop(BorderStyle.THIN);
			bodyCellStyle.setBorderBottom(BorderStyle.THIN);
			bodyCellStyle.setBorderLeft(BorderStyle.THIN);
			bodyCellStyle.setBorderRight(BorderStyle.THIN);

			String pattern3 = "dd-MMM-yyyy";
			CellStyle styleDate = wb.createCellStyle();
			styleDate.setDataFormat(wb.createDataFormat().getFormat(pattern3));

			int rowCounter = 1;
			for (Map.Entry entry : reportList.entrySet()) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> BOMLineInfo = (LinkedHashMap<String, String>) entry.getValue();

				cellCounter = 0;
				XSSFRow bodyRow = spreadsheet.createRow(rowCounter);
				for (String col : reportHeader) {
					String dispItm = BOMLineInfo.get(col);
					setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
					cellCounter++;
				}
				rowCounter++;
			}

			String fileName = "Baseline process report - " + StringExtension.getTimeStamp() + ".xlsx";
			File file = new File(System.getenv("tmp") + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setCell(int index, XSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		XSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	private boolean CheckPermission() {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			if (group.toString().compareToIgnoreCase(GROUP_PERMISSION) == 0)
				return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
}
