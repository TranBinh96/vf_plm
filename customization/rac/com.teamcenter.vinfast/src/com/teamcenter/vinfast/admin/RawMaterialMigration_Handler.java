package com.teamcenter.vinfast.admin;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.model.RawMaterialMigrationModel;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

public class RawMaterialMigration_Handler extends AbstractHandler {
	private TCSession session;
	private RawMaterialMigration_Dialog dlg;

	private static String[] GROUP_PERMISSION = { "dba" };
	private LinkedHashMap<String, RawMaterialMigrationModel> blankPartList = null;
	private LinkedHashMap<String, RawMaterialMigrationModel> coilPartList = null;
	private LinkedHashMap<String, RawMaterialMigrationModel> coil2PartList = null;

	private DataManagementService dms = null;
	private TCComponentGroup buyerGroup = null;
	private TCComponentUser buyerUser = null;
	private TCComponentGroupMember clUser = null;
//	private TCComponentUser clUser = null;

	public RawMaterialMigration_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dms = DataManagementService.getService(session);
		if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
			MessageBox.post("You are not authorized.", "Please change to group: " + String.join(";", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}

		dlg = new RawMaterialMigration_Dialog(new Shell());
		dlg.create();

		// init data
		blankPartList = new LinkedHashMap<String, RawMaterialMigrationModel>();
		coilPartList = new LinkedHashMap<String, RawMaterialMigrationModel>();
		coil2PartList = new LinkedHashMap<String, RawMaterialMigrationModel>();
		getGroupMember();
		// init UI
		dlg.btnOpenFile.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExt = { "*.xlsx", "*.xls" };
				fileDialog.setFilterExtensions(filterExt);
				readExcelFile1(fileDialog.open());
			}
		});

		dlg.btnCreate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				migrationData2();
			}
		});

		dlg.open();
		return null;
	}

	private void readExcelFile2(String path) {
		StringBuilder strReport = new StringBuilder();
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					if (rowCount > 1) {
						try {
							TCComponentItemRevision coil2Part = null;
							String coil2Number = row.getCell(0).getStringCellValue();
							String coil2Name = row.getCell(1).getStringCellValue();
							if (!coil2Number.isEmpty()) {
								RawMaterialMigrationModel item = null;
								if (coil2PartList.containsKey(coil2Number)) {
									item = coil2PartList.get(coil2Number);
									coil2Part = item.getPartRevision();
								} else {
									coil2Part = getMEPart(coil2Number);
									if (coil2Part != null) {
										item = new RawMaterialMigrationModel(session, false);
										boolean valid = item.setPartNumber(coil2Part, coil2Name);
										item.setPartPurchase(row.getCell(2).getStringCellValue());
										coil2PartList.put(coil2Number, item);
										strReport.append("<tr><td>" + coil2Number + "</td><td>" + (valid ? "OK" : "NG") + "</td></tr>");
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (wb != null)
					wb.close();
				updateReport(strReport.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readExcelFile1(String path) {
		StringBuilder strReport = new StringBuilder();
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					if (rowCount > 1) {
						try {
							TCComponentItemRevision blankPart = null;
							String blankNumber = row.getCell(0).getStringCellValue();
							if (!blankNumber.isEmpty()) {
								RawMaterialMigrationModel item = null;
								if (blankPartList.containsKey(blankNumber)) {
									item = blankPartList.get(blankNumber);
									blankPart = item.getPartRevision();
								} else {
									blankPart = getMEPart(blankNumber);
									if (blankPart != null) {
										item = new RawMaterialMigrationModel(session, true);
										boolean valid = item.setPartNumber(blankPart);
										blankPartList.put(blankNumber, item);
										strReport.append("<tr><td>" + blankNumber + "</td><td>" + (valid ? "OK" : "NG") + "</td></tr>");
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (wb != null)
					wb.close();
				updateReport(strReport.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readExcelFile(String path) {
		StringBuilder strReport = new StringBuilder();
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					if (rowCount > 1) {
						try {
							TCComponentItemRevision hPart = null;
							String hPartNumber = row.getCell(0).getStringCellValue();
							hPart = getHPart(hPartNumber, row.getCell(2).getStringCellValue());

							TCComponentItemRevision blankPart = null;
							String blankNumber = row.getCell(4).getStringCellValue();
							if (!blankNumber.isEmpty()) {
								RawMaterialMigrationModel item = null;
								if (blankPartList.containsKey(blankNumber)) {
									item = blankPartList.get(blankNumber);
									blankPart = item.getPartRevision();
								} else {
									blankPart = getMEPart(blankNumber);
									if (blankPart != null) {
										item = new RawMaterialMigrationModel(session, true);
										boolean valid = item.setPartNumber(blankPart);
										item.setPartPurchase(row.getCell(11).getStringCellValue());
//										item.setPartMaterial(row.getCell(6).getStringCellValue());
//										item.setPartCoating(row.getCell(7).getStringCellValue());
//										item.setPartThickness(row.getCell(8).getNumericCellValue());
//										item.setPartWidth(row.getCell(9).getNumericCellValue());
//										item.setPartLength(row.getCell(10).getNumericCellValue());
										blankPartList.put(blankNumber, item);
										strReport.append("<tr><td>" + blankNumber + "</td><td>" + (valid ? "OK" : "NG") + "</td></tr>");
									}
								}

								if (item != null) {
									item.addRelationHPartItem(hPartNumber, hPart);
								}
							}

							TCComponentItemRevision coilPart = null;
							String coilNumber = row.getCell(12).getStringCellValue();
							if (!coilNumber.isEmpty()) {
								RawMaterialMigrationModel item = null;
								if (coilPartList.containsKey(coilNumber)) {
									item = coilPartList.get(coilNumber);
									coilPart = item.getPartRevision();
								} else {
									coilPart = getMEPart(coilNumber);
									if (coilPart != null) {
										item = new RawMaterialMigrationModel(session, false);
										boolean valid = item.setPartNumber(coilPart);
										item.setPartPurchase(row.getCell(18).getStringCellValue());
//										item.setPartMaterial(row.getCell(14).getStringCellValue());
//										item.setPartCoating(row.getCell(15).getStringCellValue());
//										item.setPartThickness(row.getCell(16).getNumericCellValue());
//										item.setPartWidth(row.getCell(17).getNumericCellValue());
										coilPartList.put(coilNumber, item);
										strReport.append("<tr><td>" + coilNumber + "</td><td>" + (valid ? "OK" : "NG") + "</td></tr>");
									}
								}

								if (item != null) {
									if (blankPart != null)
										item.addRelationBlankItem(blankNumber, blankPart);
									else
										item.addRelationHPartItem(hPartNumber, hPart);
								}
							}

							TCComponentItemRevision coil2Part = null;
							String coil2Number = row.getCell(20).getStringCellValue();
							if (!coil2Number.isEmpty()) {
								RawMaterialMigrationModel item = null;
								if (coil2PartList.containsKey(coil2Number)) {
									item = coil2PartList.get(coil2Number);
									coil2Part = item.getPartRevision();
								} else {
									coil2Part = getMEPart(coil2Number);
									if (coil2Part != null) {
										item = new RawMaterialMigrationModel(session, false);
										boolean valid = item.setPartNumber(coil2Part);
										item.setPartPurchase(row.getCell(22).getStringCellValue());
//										item.setPartMaterial(row.getCell(14).getStringCellValue());
//										item.setPartCoating(row.getCell(15).getStringCellValue());
//										item.setPartThickness(row.getCell(16).getNumericCellValue());
//										item.setPartWidth(row.getCell(17).getNumericCellValue());
										coil2PartList.put(coil2Number, item);
										strReport.append("<tr><td>" + coil2Number + "</td><td>" + (valid ? "OK" : "NG") + "</td></tr>");
									}
								}

								if (item != null) {
									if (coilPart != null) {
										item.addRelationCoilItem(coilNumber, coilPart);
									} else {
										if (item != null) {
											if (blankPart != null)
												item.addRelationBlankItem(blankNumber, blankPart);
											else
												item.addRelationHPartItem(hPartNumber, hPart);
										}
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (wb != null)
					wb.close();
				updateReport(strReport.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void migrationData() {
		StringBuilder strReport = new StringBuilder();
		strReport.append("<tr><td>Part Number</td><td>Update Attribute</td><td>Create Source Part</td><td>Set Relation</td></tr>");
		for (Map.Entry<String, RawMaterialMigrationModel> entry : blankPartList.entrySet()) {
			RawMaterialMigrationModel rawMaterial = entry.getValue();
			TCComponentItemRevision blankPart = rawMaterial.getPartRevision();

			boolean updatePropertyCheck = true;
			boolean createSourcePartCheck = true;
			boolean setRelationCheck = true;

			try {
				Map<String, String> input = new LinkedHashMap<String, String>();
				if (blankPart.getItem().getPropertyDisplayableValue("vf4_me_part_type").isEmpty())
					input.put("vf4_me_part_type", "BLN");
				input.put("vf4_COI_material", rawMaterial.getPartMaterial());
				input.put("vf4_coating", rawMaterial.getPartCoating());
				input.put("vf4_thickness", String.valueOf(rawMaterial.getPartThickness()));
				input.put("vf4_width", String.valueOf(rawMaterial.getPartWidth()));
				input.put("vf4_length", String.valueOf(rawMaterial.getPartLength()));
				input.put("vf4_is_purchase_ME_part", rawMaterial.isPartPurchase() ? "1" : "0");

				blankPart.getItem().setProperties(input);
			} catch (Exception e) {
				e.printStackTrace();
				updatePropertyCheck = false;
			}

			if (rawMaterial.isPartPurchase())
				createSourcePartCheck = createSourcePart(blankPart);

			try {
				if (rawMaterial.getRelationList() != null && rawMaterial.getRelationList().size() > 0) {
					Set<TCComponent> partList = new HashSet<TCComponent>();
					for (Map.Entry<String, TCComponentItemRevision> part : rawMaterial.getRelationList().entrySet()) {
						partList.add(part.getValue());
					}
					blankPart.setRelated("VF4_H_Part_Relation", partList.toArray(new TCComponent[0]));
				}
			} catch (Exception e) {
				e.printStackTrace();
				setRelationCheck = false;
			}

			strReport.append("<tr><td>" + rawMaterial.getPartNumber() + "</td><td>" + (updatePropertyCheck ? "OK" : "NG") + "</td><td>" + (createSourcePartCheck ? "OK" : "NG") + "</td><td>" + (setRelationCheck ? "OK" : "NG") + "</td></tr>");
		}

		for (Map.Entry<String, RawMaterialMigrationModel> entry : coilPartList.entrySet()) {
			RawMaterialMigrationModel rawMaterial = entry.getValue();
			TCComponentItemRevision coilPart = rawMaterial.getPartRevision();

			boolean updatePropertyCheck = true;
			boolean createSourcePartCheck = true;
			boolean setRelationCheck = true;

			try {
				Map<String, String> input = new LinkedHashMap<String, String>();
				if (coilPart.getItem().getPropertyDisplayableValue("vf4_me_part_type").isEmpty())
					input.put("vf4_me_part_type", "COI");
				input.put("vf4_COI_material", rawMaterial.getPartMaterial());
				input.put("vf4_coating", rawMaterial.getPartCoating());
				input.put("vf4_thickness", String.valueOf(rawMaterial.getPartThickness()));
				input.put("vf4_width", String.valueOf(rawMaterial.getPartWidth()));
				input.put("vf4_is_purchase_ME_part", rawMaterial.isPartPurchase() ? "1" : "0");

				coilPart.getItem().setProperties(input);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (rawMaterial.isPartPurchase())
				createSourcePartCheck = createSourcePart(coilPart);

			try {
				if (rawMaterial.getRelationList() != null && rawMaterial.getRelationList().size() > 0) {
					Set<TCComponent> partList = new HashSet<TCComponent>();
					for (Map.Entry<String, TCComponentItemRevision> part : rawMaterial.getRelationList().entrySet()) {
						partList.add(part.getValue());
					}
					coilPart.setRelated("VF4_blank_part_relation", partList.toArray(new TCComponent[0]));
				}
			} catch (Exception e) {
				e.printStackTrace();
				setRelationCheck = false;
			}

			strReport.append("<tr><td>" + rawMaterial.getPartNumber() + "</td><td>" + (updatePropertyCheck ? "OK" : "NG") + "</td><td>" + (createSourcePartCheck ? "OK" : "NG") + "</td><td>" + (setRelationCheck ? "OK" : "NG") + "</td></tr>");
		}
		dlg.setMessage("Migration success.", IMessageProvider.INFORMATION);
		updateReport(strReport.toString());
	}

	private Set<TCComponent> getArrayFromLinklist(LinkedHashMap<String, TCComponentItemRevision> relationList) {
		Set<TCComponent> partList = new HashSet<TCComponent>();
		for (Map.Entry<String, TCComponentItemRevision> part : relationList.entrySet()) {
			partList.add(part.getValue());
		}

		return partList;
	}

	private void migrationData1() {
		StringBuilder strReport = new StringBuilder();
		strReport.append("<tr><td>Part Number</td><td>Update Attribute</td><td>Create Source Part</td><td>Set Relation</td></tr>");

		relationItem(strReport, blankPartList);
		relationItem(strReport, coilPartList);
		relationItem(strReport, coil2PartList);

		dlg.setMessage("Migration success.", IMessageProvider.INFORMATION);
		updateReport(strReport.toString());
	}

	private void migrationData2() {
		for (Map.Entry<String, RawMaterialMigrationModel> entry : blankPartList.entrySet()) {
			RawMaterialMigrationModel rawMaterial = entry.getValue();
			TCComponentItemRevision coilPart = rawMaterial.getPartRevision();

			try {
				Map<String, String> input = new LinkedHashMap<String, String>();
				if (coilPart.getItem().getPropertyDisplayableValue("vf4_me_part_type").isEmpty())
					input.put("vf4_me_part_type", "COI");
				input.put("vf4_COI_material", rawMaterial.getPartMaterial());
				input.put("vf4_coating", rawMaterial.getPartCoating());
				input.put("vf4_thickness", String.valueOf(rawMaterial.getPartThickness()));
				input.put("vf4_width", String.valueOf(rawMaterial.getPartWidth()));

				coilPart.getItem().setProperties(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void relationItem(StringBuilder strReport, LinkedHashMap<String, RawMaterialMigrationModel> partList) {
		for (Map.Entry<String, RawMaterialMigrationModel> entry : partList.entrySet()) {
			RawMaterialMigrationModel rawMaterial = entry.getValue();
			TCComponentItemRevision blankPart = rawMaterial.getPartRevision();

			boolean setRelationCheck = true;

			try {
				if (rawMaterial.getRelationHPartList() != null && rawMaterial.getRelationHPartList().size() > 0) {
					blankPart.setRelated("VF4_H_Part_Relation", getArrayFromLinklist(rawMaterial.getRelationHPartList()).toArray(new TCComponent[0]));
				}

				if (rawMaterial.getRelationBlankList() != null && rawMaterial.getRelationBlankList().size() > 0) {
					blankPart.setRelated("VF4_blank_part_relation", getArrayFromLinklist(rawMaterial.getRelationBlankList()).toArray(new TCComponent[0]));
				}

				if (rawMaterial.getRelationCoilList() != null && rawMaterial.getRelationCoilList().size() > 0) {
					blankPart.setRelated("VF4_blank_part_relation", getArrayFromLinklist(rawMaterial.getRelationCoilList()).toArray(new TCComponent[0]));
				}
			} catch (Exception e) {
				e.printStackTrace();
				setRelationCheck = false;
			}

			strReport.append("<tr><td>" + rawMaterial.getPartNumber() + "</td><td>OK</td><td>OK</td><td>" + (setRelationCheck ? "OK" : "NG") + "</td></tr>");
		}
	}

	private TCComponentItemRevision getMEPart(String partNumber) {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("ID", partNumber);
		inputQuery.put("Type", "ME Part Revision");

		TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Matured Revision");
		if (item_search == null || item_search.length == 0)
			return null;
		return (TCComponentItemRevision) item_search[0];
	}

	private TCComponentItemRevision getHPart(String partNumber, String partRevision) {
		if (!partRevision.isEmpty()) {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", partNumber);
			inputQuery.put("Revision", partRevision);
			inputQuery.put("Type", "VF Design Revision");
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Item Revision...");
			if (item_search != null && item_search.length > 0)
				return (TCComponentItemRevision) item_search[0];
		} else {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("ID", partNumber);
			inputQuery.put("Type", "VF Design Revision");
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Matured Revision");
			if (item_search != null && item_search.length > 0)
				return (TCComponentItemRevision) item_search[0];
		}
		return null;
	}

	private boolean createSourcePart(TCComponentItemRevision partRev) {
		try {
			AIFComponentContext[] costFormAifs = partRev.getRelated("VF4_Sourcing_Reference");
			if (costFormAifs != null && costFormAifs.length > 1)
				return true;

			TCComponentItem partItem = partRev.getItem();
			String partId = partRev.getPropertyDisplayableValue("item_id");
			String partName = partRev.getPropertyDisplayableValue("object_name");
			String sourcingProgram = partRev.getPropertyDisplayableValue("object_name");
			String donorVehicle = partItem.getPropertyDisplayableValue("vf4_donor_veh");
			String purchaseLevel = PropertyDefines.PUR_LEVEL_P;
			String changeIndex = "NEW";
			String uom = partRev.getPropertyDisplayableValue("uom_tag");

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_Car_LineItm";
			itemDef.data.stringProps.put("object_name", partName);

			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF4_Car_LineItmRevision";
			itemRevisionDef.stringProps.put("object_name", partName);
			itemRevisionDef.stringProps.put("vf4_bom_vfPartNumber", partId);
//			itemRevisionDef.stringProps.put("vf4_platform_module", sourcingProgram);
			itemRevisionDef.tagProps.put("vf4_cl_2", clUser);

			itemRevisionDef.stringProps.put("vf4_purchasing_level", purchaseLevel);
			itemRevisionDef.stringProps.put("vf4_bom_change_index", changeIndex);
			itemRevisionDef.stringProps.put("vf4_bl_unit_of_measure", uom);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post(TCExtension.hanlderServiceData(response.serviceData), "Error", MessageBox.ERROR);
				System.out.println(TCExtension.hanlderServiceData(response.serviceData));
				return false;
			}
			TCComponentItemRevision sourceRev = (TCComponentItemRevision) response.output[0].objects[2];
			changeOwner(sourceRev);
			// attach cost object to part
			partRev.setRelated("VF4_Sourcing_Reference", new TCComponent[] { sourceRev });
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void getGroupMember() {
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Id", "hoangvh3;thaohtp8");
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Admin - User Memberships");

			if (item_search != null && item_search.length > 0) {
				for (TCComponent tcComponent : item_search) {
					TCComponentGroupMember groupMember = (TCComponentGroupMember) tcComponent;
					String group = groupMember.getGroup().toString();
					String role = groupMember.getRole().toString();
					String user = groupMember.getUser().toString();
					if (group.compareTo("Indirect Pur.Purchase.VINFAST") == 0) {
						if (role.compareTo("CL") == 0 && user.contains("hoangvh3")) {
							clUser = groupMember;
						}
						if (role.compareTo("Buyer") == 0 && user.contains("thaohtp8")) {
							buyerGroup = groupMember.getGroup();
							buyerUser = groupMember.getUser();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void changeOwner(TCComponentItemRevision item) throws TCException {
		try {
			item.changeOwner(buyerUser, buyerGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateReport(String value) {
		StringBuffer reportText = new StringBuffer();
		reportText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		reportText.append(StringExtension.htmlTableCss);
		reportText.append("<body style=\"margin: 0px;\">");
		reportText.append("<table>");

		reportText.append(value);

		reportText.append("</table>");
		reportText.append("</body></html>");
		dlg.browser.setText(reportText.toString());
	}
}
