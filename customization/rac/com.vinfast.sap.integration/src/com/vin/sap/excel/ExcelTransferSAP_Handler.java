package com.vin.sap.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.integration.model.OrganizationInformationFactory;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class ExcelTransferSAP_Handler extends AbstractHandler {
	private TCSession session;
	private OrganizationInformationAbstract serverInfo = null;
	private TCComponent selectedObject = null;
	private ExcelTransferSAP_Dialog dlg;
	private LinkedHashMap<String, List<String>> formTransfer = null;
	private static String[] GROUP_PERMISSION = { "dba" };

	public ExcelTransferSAP_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.",
						"Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select 1 MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			String mcnNumber = selectedObject.getPropertyDisplayableValue("item_id");

			// init Data
			formTransfer = new LinkedHashMap<String, List<String>>();
			String[] formPreference = TCExtension.GetPreferenceValues("EXCEL_SAP_FORMAT_TRANSFER", session);
			if (formPreference != null) {
				for (String form : formPreference) {
					if (form.contains("=")) {
						String[] str = form.split("=");
						String formValue = str[0];
						List<String> headerColumnValue = new LinkedList<String>();
						if (str[1].contains(",")) {
							headerColumnValue = Arrays.asList(str[1].split(","));
						} else {
							headerColumnValue.add(str[1]);
						}
						formTransfer.put(formValue, headerColumnValue);
					}
				}
			}
			String company = "";
			String command = event.getCommand().toString();
			if (command.contains(PropertyDefines.VIN_ES)) {
				company = PropertyDefines.VIN_ES.toUpperCase();
			} else if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
				company = PropertyDefines.VIN_FAST_ELECTRIC.toUpperCase();
			} else {
				company = PropertyDefines.VIN_FAST.toUpperCase();
			}
			// init UI
			dlg = new ExcelTransferSAP_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Excel BOM/BOP Transfer for " + company);

			dlg.cbFormat.setItems(formTransfer.keySet().toArray(new String[0]));
			dlg.cbServer.setText("PRODUCTION");
			dlg.txtMCN.setText(mcnNumber);

			String logFolder = UIGetValuesUtility.createLogFolder(mcnNumber);
			dlg.btnSave.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					String serverType = dlg.cbServer.getText();
					serverInfo = OrganizationInformationFactory
							.generateOrganizationInformation(event.getCommand().toString(), serverType, session);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferByCode(mcnNumber, logFolder);
							dlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnDownLoadForm.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					try {
						String format = dlg.cbFormat.getText();
						String fileName = String.format("%s\\%s.xlsx", logFolder, format);
						downloadFormat(fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			dlg.btnUpLoadForm.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					FileDialog dialog = new FileDialog(dlg.getShell(), SWT.OPEN);
					String path = dialog.open();
					dlg.txtLink.setText(path);
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<ArrayList<String>> readExcel(String excelFilePath, List<String> columns)
			throws IOException {
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		InputStream inputStream = new FileInputStream(new File(excelFilePath));
		Workbook workbook = getWorkbook(inputStream, excelFilePath);
		Sheet sheet = workbook.getSheetAt(0);
		XSSFRow row;
		Iterator<Row> rows = sheet.rowIterator();
		while (rows.hasNext()) {
			row = (XSSFRow) rows.next();
			ArrayList<String> rowData = new ArrayList<String>();
			for (int cn = 0; cn < columns.size(); cn++) {
				XSSFCell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				rowData.add(String.format("%s", (String) getCellValue(cell)));
			}
			list.add(rowData);
		}
		inputStream.close();
		return list;
	}

	private static Workbook getWorkbook(InputStream inputStream, String excelFilePath) throws IOException {
		Workbook workbook = null;
		if (excelFilePath.endsWith("xlsx")) {
			workbook = new XSSFWorkbook(inputStream);
		} else if (excelFilePath.endsWith("xls")) {
			workbook = new HSSFWorkbook(inputStream);
		} else {
			throw new IllegalArgumentException("The specified file is not Excel file");
		}

		return workbook;
	}

	private static Object getCellValue(Cell cell) {
		cell.setCellType(CellType.STRING);
		Object cellValue = null;
		cellValue = cell.getStringCellValue();
		return cellValue;
	}

	public void transferByCode(String mcnNumber, String logFolder) {
		String fileName = dlg.txtLink.getText();

		SAPURL sapConnect = new SAPURL();
		List<String> format = formTransfer.get(dlg.cbFormat.getText());
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		try {
			ArrayList<ArrayList<String>> listInput = readExcel(fileName, format);
			for (ArrayList<String> row : listInput) {
				HashMap<String, String> xml = new HashMap<String, String>();
				int i = 0;
				for (String col : row) {
					if (format.get(i).equals("BOMLINEID") && !col.isEmpty()) {
						while (col.length() < 4) {
							col = "0" + col;
						}
					}

					if (format.get(i).equals("REVISION")) {
						while (col.length() < 2) {
							col = "0" + col;
						}
					}

					if (format.get(i).equals("BOPID")) {
						while (col.length() < 6) {
							col = "0" + col;
						}
					}

					xml.put(format.get(i++), col.trim());
				}
				if (xml.get("ACTION").length() == 1) {
					xml.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
					list.add(xml);
				}
			}

			String serviceHeader = "";
			String URL = "";
			String nameSpace = "";
			String service = "";
			String formFormat = dlg.cbFormat.getText();
			switch (formFormat) {
			case "CAR_UNIT_BOM":
				serviceHeader = SAPURL.ASSY_BOM_HEADER;
				URL = SAPURL.ASSY_BOM_TAG;
				nameSpace = SAPURL.ASSY_BOM_NAMESPACE;
				service = sapConnect.assybomWebserviceURL(serverInfo.getServerIP());
				break;
			case "CAR_UNIT_BOP":
				serviceHeader = SAPURL.ASSY_BOP_HEADER;
				URL = SAPURL.ASSY_BOP_TAG;
				nameSpace = SAPURL.ASSY_BOP_NAMESPACE;
				service = sapConnect.assybopWebserviceURL(serverInfo.getServerIP());
				break;
			case "CAR_SUPER_BOM":
				serviceHeader = SAPURL.SUP_BOM_HEADER;
				URL = SAPURL.SUP_BOM_TAG;
				nameSpace = SAPURL.SUP_BOM_NAMESPACE;
				service = sapConnect.superbomWebserviceURL(serverInfo.getServerIP());
				break;
			case "CAR_SUPER_BOP":
				serviceHeader = SAPURL.SUP_BOP_HEADER;
				URL = SAPURL.SUP_BOP_TAG;
				nameSpace = SAPURL.SUP_BOP_NAMESPACE;
				service = sapConnect.superbopWebserviceURL(serverInfo.getServerIP());
				break;
			default:
				break;
			}

			int count = 1;
			for (HashMap<String, String> BOP_Values : list) {
				String fileOut = String.valueOf(count);
				try {
					CreateSoapHttpRequest.sendRequest(service, BOP_Values, serviceHeader, URL, nameSpace,
							"I_EXCEL_" + BOP_Values.get("ACTION") + "_" + fileOut, logFolder, serverInfo.getAuth());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*ins.callWebService(bopSoapRequest, service,
						"O_EXCEL_" + BOP_Values.get("ACTION") + "_" + fileOut, logFolder);*/

//				MessageBox.post(msg.toString(), "Info", MessageBox.INFORMATION);
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloadFormat(String fileName) throws IOException {
		List<String> headerColumns = formTransfer.get(dlg.cbFormat.getText());
		// workbook object
		XSSFWorkbook workbook = new XSSFWorkbook();
		// spreadsheet object
		XSSFSheet spreadsheet = workbook.createSheet(" Student Data ");
		// creating a row object
		XSSFRow row;
		// This data needs to be written (Object[])
		Map<String, Object[]> formatData = new TreeMap<String, Object[]>();
		formatData.put("1", headerColumns.toArray(new String[0]));

		Set<String> keyid = formatData.keySet();
		int rowid = 0;
		// writing the data into the sheets...
		for (String key : keyid) {
			row = spreadsheet.createRow(rowid++);
			Object[] objectArr = formatData.get(key);
			int cellid = 0;

			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				cell.setCellValue((String) obj);
			}
		}

		FileOutputStream out = new FileOutputStream(new File(fileName));

		workbook.write(out);
		out.close();
		workbook.close();
		MessageBox.post(String.format("Downloaded format successfully: %s", fileName), "Info", MessageBox.INFORMATION);
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		selectedObject = (TCComponent) targetComponents[0];
		return false;
	}
}
