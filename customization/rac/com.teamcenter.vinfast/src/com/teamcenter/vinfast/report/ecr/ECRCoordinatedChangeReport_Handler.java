package com.teamcenter.vinfast.report.ecr;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.ExcelExtension;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ECRCoordinatedChangeReport_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private ECRCoordinatedChangeReport_Dialog dlg;
	private LinkedHashMap<String, String> propertyList = new LinkedHashMap<String, String>() {
		{
			put("ECR Number", "item_id");
			put("ECR Title", "object_name");
			put("Program", "vf6_vehicle_group");
			put("Module", "vf6_module_group_comp");
			put("Release Status", "release_status_list");
			put("Date Release", "date_released");
		}
	};

	private final String REPORT_PREFIX = "ECR-Coordinated-Change";

	public ECRCoordinatedChangeReport_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			String[] vehicleGroupDataForm = TCExtension.GetLovValues("vf6_vehicle_group", "Vf6_ECR", session);
			String[] moduleGroupDataForm = TCExtension.GetLovValues("vf6_module_group", "Vf6_ECR", session);

			dlg = new ECRCoordinatedChangeReport_Dialog(new Shell());
			dlg.create();

			dlg.cbProgram.setItems(vehicleGroupDataForm);
			dlg.btnProgramAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("PROGRAM");
				}
			});

			dlg.btnProgramRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("PROGRAM");
				}
			});

			dlg.cbModule.setItems(moduleGroupDataForm);
			dlg.btnModuleAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("MODULE");
				}
			});

			dlg.btnModuleRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("MODULE");
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					exportReport();
				}
			});

			dlg.open();
		} catch (Exception e) {

		}
		return null;
	}

	private void exportReport() {
		try {
			List<String> programFilter = null;
			if (dlg.lstProgram.getItemCount() > 0)
				programFilter = Arrays.asList(dlg.cbProgram.getItems());

			List<String> moduleFilter = null;
			if (dlg.lstModule.getItemCount() > 0)
				moduleFilter = Arrays.asList(dlg.cbModule.getItems());

			LinkedList<LinkedHashMap<String, String>> dataList = new LinkedList<>();
			TCComponent[] queryOutput = TCExtension.queryItem(session, null, "VF_ECR_With_Coordinated_Changes");
			if (queryOutput != null) {
				dmService.getProperties(queryOutput, propertyList.values().toArray(new String[0]));
				for (TCComponent queryItem : queryOutput) {
					LinkedHashMap<String, String> parentInfo = new LinkedHashMap<>();
					for (Map.Entry<String, String> property : propertyList.entrySet()) {
						parentInfo.put(property.getKey(), queryItem.getPropertyDisplayableValue(property.getValue()));
					}
					String ccrNumber = parentInfo.get("ECR Number").replace("ECR", "CCR");
					String program = parentInfo.get("Program");
					String module = parentInfo.get("Module");

					if (programFilter != null && !programFilter.contains(program))
						continue;

					if (moduleFilter != null && !moduleFilter.contains(module))
						continue;

					parentInfo.put("CCR", ccrNumber);
					dataList.add(parentInfo);

					TCComponent[] coordinatedChanges = queryItem.getRelatedComponents("Vf6_CoordinatedChange");
					if (coordinatedChanges != null) {
						for (TCComponent coordinatedItem : coordinatedChanges) {
							TCComponentItemRevision ecrRevision = null;
							if (coordinatedItem instanceof TCComponentItem)
								ecrRevision = getECRRevision((TCComponentItem) coordinatedItem);
							else if (coordinatedItem instanceof TCComponentItemRevision)
								ecrRevision = getECRRevision(((TCComponentItemRevision) coordinatedItem).getItem());

							if (ecrRevision != null) {
								LinkedHashMap<String, String> childInfo = new LinkedHashMap<>();
								childInfo.put("CCR", ccrNumber);
								for (Map.Entry<String, String> property : propertyList.entrySet()) {
									childInfo.put(property.getKey(), ecrRevision.getPropertyDisplayableValue(property.getValue()));
								}
								dataList.add(childInfo);
							}
						}
					}
				}
			}

			excelPublishReport(dataList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TCComponentItemRevision getECRRevision(TCComponentItem ecrItem) {
		TCComponentItemRevision ecrRevision = null;
		try {
			String partNumber = ecrItem.getPropertyDisplayableValue("item_id");
			if (partNumber.compareTo("ECR40000531") == 0) {
				System.out.println(partNumber);
			}
			TCComponentItemRevision[] ecrItemRevisions = ecrItem.getReleasedItemRevisions();
			if (ecrItemRevisions != null && ecrItemRevisions.length > 0) {
				for (TCComponentItemRevision ecrItemRevision : ecrItemRevisions) {
					String releaseStatus = ecrItemRevision.getPropertyDisplayableValue("release_status_list");
					if (releaseStatus.contains("Approved")) {
						ecrRevision = ecrItemRevision;
					} else {
						if (ecrRevision == null) {
							ecrRevision = ecrItemRevision;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ecrRevision;
	}

	private void excelPublishReport(LinkedList<LinkedHashMap<String, String>> dataList) {
		try {
			@SuppressWarnings("resource")
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet spreadsheet = wb.createSheet("DOC");

			// Header
			CellStyle headerCellStyle = wb.createCellStyle();

			HSSFRow headerRow = spreadsheet.createRow(0);
			ExcelExtension.setCell(0, headerRow, "CCR", headerCellStyle);
			int cellCounter = 1;
			for (String value : propertyList.keySet()) {
				ExcelExtension.setCell(cellCounter, headerRow, value, headerCellStyle);
				cellCounter++;
			}
			// Body
			CellStyle bodyCellStyle = wb.createCellStyle();

			int rowCounter = 1;
			for (LinkedHashMap<String, String> lineInfo : dataList) {
				HSSFRow bodyRow = spreadsheet.createRow(rowCounter);
				ExcelExtension.setCell(0, bodyRow, lineInfo.get("CCR"), bodyCellStyle);
				cellCounter = 1;
				for (String col : propertyList.keySet()) {
					String dispItm = lineInfo.get(col);
					if (dispItm == null) {
						dispItm = "";
					}
					ExcelExtension.setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
					cellCounter++;
				}
				rowCounter++;
			}

			String fileName = REPORT_PREFIX + "_" + StringExtension.getTimeStamp() + ".xls";
			File file = new File(System.getenv("tmp") + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			MessageBox.post("The report export successfully.", "Information", MessageBox.INFORMATION);
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addValue(String type) {
		switch (type) {
		case "PROGRAM":
			String program = dlg.cbProgram.getText();
			if (!program.isEmpty()) {
				if (checkExistInList(dlg.lstProgram.getItems(), program))
					dlg.lstProgram.add(program);
			}
			break;
		case "MODULE":
			String module = dlg.cbModule.getText();
			if (!module.isEmpty()) {
				if (checkExistInList(dlg.lstModule.getItems(), module))
					dlg.lstModule.add(module);
			}
			break;
		}
	}

	private void removeValue(String type) {
		switch (type) {
		case "PROGRAM":
			int indexProgram = dlg.lstProgram.getSelectionIndex();
			if (indexProgram >= 0)
				dlg.lstProgram.remove(indexProgram);
			break;
		case "MODULE":
			int indexModule = dlg.lstModule.getSelectionIndex();
			if (indexModule >= 0)
				dlg.lstModule.remove(indexModule);
			break;
		}
	}

	private boolean checkExistInList(String[] origin, String target) {
		if (origin == null || origin.length == 0)
			return true;

		for (String item : origin) {
			if (item.compareTo(target) == 0)
				return false;
		}
		return true;
	}
}
