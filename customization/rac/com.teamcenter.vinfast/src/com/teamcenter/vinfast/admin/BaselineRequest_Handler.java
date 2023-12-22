package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.bom.StructureManagementService;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BaselineInput;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BaselineResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.teamcenter.services.rac.core.ReservationService;

public class BaselineRequest_Handler extends AbstractHandler {
	private TCSession session;
	private BaselineRequest_Dialog dlg;

	private TCComponentBOMLine selectedObject;
	private LinkedHashMap<String, LinkedHashMap<String, String>> reportMap = null;
	private LinkedList<PackingItem> packageList = null;
	private String[] reportHeader = { "Package Number", "Part Number", "Revision", "Make/Buy", "Donor Vehicle", "Part Category", "Item Checkout", "Item Release Status" };

	private static String[] GROUP_PERMISSION = new String[] { "dba" };

	public BaselineRequest_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkUserHasGroup(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized to baseline.", "Please change to group: " + String.join(", ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp[0] instanceof TCComponentBOMLine) {
				selectedObject = (TCComponentBOMLine) targetComp[0];
			} else {
				MessageBox.post("Select a bomline.", "", MessageBox.ERROR);
				return null;
			}
			reportMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			packageList = new LinkedList<PackingItem>();

//			TCComponentBOMLine topLine = (TCComponentBOMLine) selectedObject;
//			if (topLine.getBOMViewRevision().isCheckedOut()) {
//				MessageBox.post("BOMView Need to be checked in", "Error", MessageBox.ERROR);
//				return null;
//			}

			dlg = new BaselineRequest_Dialog(new Shell());
			dlg.create();

			// Init Data
			String[] revRulesDataForm = TCExtension.GetRevisionRules(session);
			String[] baselineTemplateDataForm = TCExtension.GetPreferenceValues("Baseline_release_procedures", session);
			// Init UI

			dlg.cbRevisionRule.setItems(revRulesDataForm);
			dlg.cbBaselineTemplate.setItems(baselineTemplateDataForm);
			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						if (!validateRequired()) {
							dlg.setMessage("Please input all required information.", IMessageProvider.WARNING);
							return;
						}
						packingProcess();
					} catch (Exception e2) {

					}
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void packingProcess() {
		LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
		allBOMLineInWindow = TCExtension.expandAllBOMLines((TCComponentBOMLine) selectedObject, session);

		int numberEachPackage = 10;
		String numberPackage = dlg.txtPackageQuantity.getText();
		if (StringExtension.isInteger(numberPackage, 10)) {
			numberEachPackage = Integer.parseInt(numberPackage);
		}
		int i = 0, j = 0;
		TCComponentBOMLine topBom = null;
		HashSet<String> uniqueID = new HashSet<>();
		String partNumber = "";
		String packageNumber = "";
		try {
			for (TCComponentBOMLine oneLine : allBOMLineInWindow) {
				partNumber = oneLine.getPropertyDisplayableValue("bl_item_item_id");
				if (uniqueID.contains(partNumber))
					continue;

				uniqueID.add(partNumber);

				if (i % numberEachPackage == 0) {
					TCComponentItemRevision packageItemRev = createPackage();
					packageNumber = packageItemRev.getItem().getPropertyDisplayableValue("item_id");
					packageList.add(new PackingItem(packageNumber, packageItemRev));

					if (packageItemRev != null) {
						boolean createBomView = createBomViewRevision(packageItemRev, true);
						if (createBomView) {
							topBom = getTopBom(packageItemRev);
						}
					}
				}

				boolean[] isChecks = validation(oneLine, packageNumber, j);
				if (isChecks != null && isChecks.length > 1) {
					if (isChecks[0]) {
						if (topBom != null) {
							addToBOMLine(oneLine, topBom);
							if (!isChecks[1]) {
								for (PackingItem packing : packageList) {
									if (packing.partID.compareToIgnoreCase(packageNumber) == 0) {
										packing.isBaseline = false;
										break;
									}
								}
							}
						}
						i++;
					}
				}
				j++;
			}
		} catch (Exception e) {

		}
		if (dlg.ckbBaseline.getSelection()) {
			baselineProcess();
		}
		exportExcelReport();
	}

	private void baselineProcess() {
		String releaseProcess = dlg.cbBaselineTemplate.getText();
		TCComponentRevisionRule revRule = TCExtension.GetRevisionRule(dlg.cbRevisionRule.getText(), session);
		String desc = dlg.txtDesc.getText();

		for (PackingItem packing : packageList) {
			try {
				if (packing.isBaseline)
					createBaseline(packing.object, releaseProcess, revRule, desc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private TCComponentItemRevision createBaseline(TCComponentItemRevision itemRevision, String releaseProcess, TCComponentRevisionRule revRule, String desc) throws NotLoadedException {
		BaselineInput input = new BaselineInput();
		input.clientID = itemRevision.getUid();
		input.baselineJobName = "Baseline_" + itemRevision.getPropertyDisplayableValue("item_id") + "/" + itemRevision.getPropertyDisplayableValue("current_revision_id");
		input.baselineJobDescription = desc;
		input.itemRev = itemRevision;
		input.dryrun = false;
		input.precise = true;
		input.releaseProcess = releaseProcess;
		input.revRule = revRule;
		input.viewType = "view";

		StructureManagementService service = StructureManagementService.getService(session);
		BaselineResponse response = service.createBaseline(new BaselineInput[] { input });
		if (response.output.length > 0) {
			dlg.setMessage("Baseline success", IMessageProvider.INFORMATION);
			return response.output[0].baselineItemRev;
		} else {
			dlg.setMessage(Utils.HanlderServiceData(response.serviceData), IMessageProvider.ERROR);
			return null;
		}
	}

	private void exportExcelReport() {
		try {
			@SuppressWarnings("resource")
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
			for (Map.Entry<String, LinkedHashMap<String, String>> entry : reportMap.entrySet()) {
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

	private TCComponentItemRevision createPackage() {
		TCComponentItemRevision cfgContext = null;

		try {
			String objectType = "VF4_Study_Part";
			DataManagementService dms = DataManagementService.getService(session);
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = objectType;
			nextID.pattern = "NNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", "Part package");

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals(objectType + "Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
				if (cfgContext != null) {
//					try {
//						session.getUser().getNewStuffFolder().add("contents", cfgContext);
//					} catch (TCException e1) {
//						MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
//					}
//					dlg.setMessage(
//							"Created successfully, new item ( " + ids[0] + " ) has been copied to your Newstuff folder",
//							IMessageProvider.INFORMATION);
				} else {
//					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
//				ServiceData serviceData = response.serviceData;
//				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
//					for (String msg : serviceData.getPartialError(i).getMessages()) {
//						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfgContext;
	}

	private TCComponentBOMLine getTopBom(TCComponentItemRevision itemRevision) {
		OpenContextInfo[] createdBOMViews = TCExtension.CreateContextViews(itemRevision, session);
		if (createdBOMViews != null) {
			if (createdBOMViews.length > 0) {
				if (createdBOMViews[0].context.getType().equals("BOMLine")) {
					return (TCComponentBOMLine) createdBOMViews[0].context;
				}
			}
		}
		return null;
	}

	private boolean createBomViewRevision(TCComponentItemRevision revision, Boolean precise) {
		try {
			CreateOrUpdateRelativeStructureInfo2[] info = new CreateOrUpdateRelativeStructureInfo2[1];
			info[0] = new CreateOrUpdateRelativeStructureInfo2();
			info[0].lastModifiedOfBVR = Calendar.getInstance();
			info[0].parent = revision;
			info[0].precise = true;

			CreateOrUpdateRelativeStructurePref2 prefs = new CreateOrUpdateRelativeStructurePref2();
			prefs.cadOccIdAttrName = null;
			prefs.itemTypes = null;
			prefs.overwriteForLastModDate = true;

			StructureManagement smService = com.teamcenter.services.rac.cad.StructureManagementService.getService(session);

			CreateOrUpdateRelativeStructureResponse response = smService.createOrUpdateRelativeStructure(info, "PR4D_cad", true, prefs);
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void addToBOMLine(TCComponentBOMLine newRev, TCComponentBOMLine topBom) {
		try {
			topBom.add(newRev.getItem(), newRev.getItemRevision(), null, false);
			topBom.window().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private boolean[] validation(TCComponentBOMLine itemRev, String packageNumber, int no) throws Exception {
		boolean isAddToPackage = false;
		boolean isBaseline = true;
		String revStatus = itemRev.getPropertyDisplayableValue("awb0RevisionRelStatusList");
		if (revStatus.isEmpty() || revStatus.compareToIgnoreCase("E") == 0) {
			isAddToPackage = true;
			LinkedHashMap<String, String> reportItemMap = new LinkedHashMap<String, String>();
			reportItemMap.put("Part Number", itemRev.getPropertyDisplayableValue("bl_item_item_id"));
			reportItemMap.put("Revision", itemRev.getPropertyDisplayableValue("bl_rev_item_revision_id"));
			if (itemRev.getPropertyDisplayableValue("bl_item_vf4_item_make_buy").isEmpty()) {
				reportItemMap.put("Make/Buy", "x");
				isBaseline = false;
			} else {
				reportItemMap.put("Make/Buy", "");
			}
			if (itemRev.getPropertyDisplayableValue("vf4_DsnRev_donor_veh_type").isEmpty()) {
				reportItemMap.put("Donor Vehicle", "x");
				isBaseline = false;
			} else {
				reportItemMap.put("Donor Vehicle", "");
			}
			if (itemRev.getPropertyDisplayableValue("bl_item_vf4_part_category").isEmpty()) {
				reportItemMap.put("Part Category", "x");
				isBaseline = false;
			} else {
				reportItemMap.put("Part Category", "");
			}
			if (itemRev.getItem().isCheckedOut()) {
				if (checkInObject(new TCComponent[] { itemRev.getItem() })) {
					reportItemMap.put("Item Checkout", "");
				} else {
					reportItemMap.put("Item Checkout", "x");
					isBaseline = false;
				}
			} else {
				reportItemMap.put("Item Checkout", "");
			}
			String itemStatus = itemRev.getItem().getPropertyDisplayableValue("release_status_list");
			if (itemStatus.isEmpty()) {
				reportItemMap.put("Item Release Status", "");
			} else {
				reportItemMap.put("Item Release Status", "x");
				isBaseline = false;
			}
			reportItemMap.put("Package Number", packageNumber);
			reportMap.put(String.valueOf(no), reportItemMap);
		}
		return (new boolean[] { isAddToPackage, isBaseline });
	}

	private Boolean validateRequired() {
		if (dlg.txtPackageQuantity.getText().isEmpty())
			return false;
		if (dlg.ckbBaseline.getSelection()) {
			if (dlg.cbBaselineTemplate.getText().isEmpty())
				return false;
			if (dlg.cbRevisionRule.getText().isEmpty())
				return false;
		}
//		if(dlg.tblPartNumber.getItemCount() <= 0) return false;
		return true;
	}

	private void resetDialog() {
		dlg.txtDesc.setText("");
		dlg.cbBaselineTemplate.deselectAll();
		dlg.cbRevisionRule.deselectAll();
	}

	private boolean checkInObject(TCComponent[] modObjArr) {
		ReservationService resServ = ReservationService.getService(session);
		ServiceData data = resServ.checkin(modObjArr);
		if (data.sizeOfPartialErrors() > 0) {
			return false;
		}
		return true;
	}

	class PackingItem {
		public String partID;
		public TCComponentItemRevision object;
		public boolean isBaseline;

		public PackingItem(String _partID, TCComponentItemRevision _object) {
			partID = _partID;
			object = _object;
			isBaseline = true;
		}
	}
}
