package com.teamcenter.vines.ecr;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.change.ECRBomInfoEdit_Dialog;
import com.teamcenter.vinfast.subdialog.MassUpdateComboboxValue_Dialog;
import com.vf.utils.TCExtension;

public class VESECRBomInfoUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private VESECRBomInfoUpdate_Dialog dlg;
	private TCComponent selectedObject = null;

	private Color warningColor = new Color(null, 255, 73, 97);
	private Color normalColor = new Color(null, 255, 255, 255);
	private String[] maturityLevelDataForm = { "Not released", "P", "I", "PR", "PPR" };
	private String[] changeTypeDataForm = { "NEW", "ADD", "CHANGE", "SWAP", "REMOVE" };
	private String[] steeringDataForm = { "All", "LHD", "RHD" };
	private String[] exchangeabilityDataForm = { "Fully interchangeable-stock OK to use for any vehicle", "New for old only-stock OK to service old vehicles", "New for old only-do not use stock", "Not interchangeable-stock OK to service old vehicles", "Not interchangeable-do not use stock" };
	private LinkedHashMap<String, String> bomInfoTemplate = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("", "");
			put(VESECRNameDefine.BOMINFO_CHANGE_TYPE, "");
			put(VESECRNameDefine.BOMINFO_POSID, "VL5_pos_id");
			put(VESECRNameDefine.BOMINFO_DONOR_VEHICLE, "vf4_DsnRev_donor_veh_type");
			put(VESECRNameDefine.BOMINFO_STRUCTURE_LEVEL, "bl_level_starting_0");
			put(VESECRNameDefine.BOMINFO_STEERING, "");
			put(VESECRNameDefine.BOMINFO_QUANTITY, "bl_quantity");
			put(VESECRNameDefine.BOMINFO_MATURITY_LEVEL, "bl_item_vf4cp_maturity_level");
			put(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL, "VL5_purchase_lvl_vf");
			put(VESECRNameDefine.BOMINFO_PART_NUMBER, "awb0BomLineItemId");
			put(VESECRNameDefine.BOMINFO_OLD_VERSION, "");
			put(VESECRNameDefine.BOMINFO_FROZEN_REVISION, "");
			put(VESECRNameDefine.BOMINFO_NEW_VERSION, "bl_rev_item_revision_id");
			put(VESECRNameDefine.BOMINFO_PART_NAME, "bl_item_object_name");
			put(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART, "bl_item_vf4_orginal_part_number");
			put(VESECRNameDefine.BOMINFO_VARIANT_FORMULA, "bl_formula");
			put(VESECRNameDefine.BOMINFO_TORQUE_INFO, "VL5_torque_inf");
			put(VESECRNameDefine.BOMINFO_WEIGHT, "bl_rev_vf4_cad_weight");
			put(VESECRNameDefine.BOMINFO_CHANGE_DESC, "");
			put(VESECRNameDefine.BOMINFO_3D_DATA_AFFECTED, "");
			put(VESECRNameDefine.BOMINFO_MATERIAL, "vf4_catia_material");
			put(VESECRNameDefine.BOMINFO_CAD_COATING, "bl_rev_vf4_cad_coating");
			put(VESECRNameDefine.BOMINFO_SPEC_BOOK, "bl_rev_vl5_specbook_num");
			put(VESECRNameDefine.BOMINFO_SUPPLIER, "");
			put(VESECRNameDefine.BOMINFO_SUPPLIER_CONTACT, "");
			put(VESECRNameDefine.BOMINFO_IS_AFTERSALE, "bl_item_vf4_itm_after_sale_relevant");
			put(VESECRNameDefine.BOMINFO_EXCHANGEABILITY, "");
			put(VESECRNameDefine.BOMINFO_PART_TRACE, "bl_item_vf4_item_is_traceable");
			put(VESECRNameDefine.BOMINFO_COP_IMPACTED_PROGRAM, "");
		}
	};

	private LinkedHashMap<String, Integer> bomInfoTemplateRecover = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put(VESECRNameDefine.BOMINFO_CHANGE_TYPE, 11);
			put(VESECRNameDefine.BOMINFO_POSID, 0);
			put(VESECRNameDefine.BOMINFO_DONOR_VEHICLE, 3);
			put(VESECRNameDefine.BOMINFO_STRUCTURE_LEVEL, 5);
			put(VESECRNameDefine.BOMINFO_STEERING, 6);
			put(VESECRNameDefine.BOMINFO_QUANTITY, 7);
			put(VESECRNameDefine.BOMINFO_MATURITY_LEVEL, 8);
			put(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL, 10);
			put(VESECRNameDefine.BOMINFO_PART_NUMBER, 13);
			put(VESECRNameDefine.BOMINFO_OLD_VERSION, 16);
			put(VESECRNameDefine.BOMINFO_FROZEN_REVISION, 17);
			put(VESECRNameDefine.BOMINFO_NEW_VERSION, 18);
			put(VESECRNameDefine.BOMINFO_PART_NAME, 19);
			put(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART, 27);
			put(VESECRNameDefine.BOMINFO_VARIANT_FORMULA, 31);
			put(VESECRNameDefine.BOMINFO_TORQUE_INFO, 36);
			put(VESECRNameDefine.BOMINFO_WEIGHT, 41);
			put(VESECRNameDefine.BOMINFO_CHANGE_DESC, 43);
			put(VESECRNameDefine.BOMINFO_3D_DATA_AFFECTED, 54);
			put(VESECRNameDefine.BOMINFO_MATERIAL, 55);
			put(VESECRNameDefine.BOMINFO_CAD_COATING, 58);
			put(VESECRNameDefine.BOMINFO_SPEC_BOOK, 63);
			put(VESECRNameDefine.BOMINFO_SUPPLIER, 67);
			put(VESECRNameDefine.BOMINFO_SUPPLIER_CONTACT, 71);
			put(VESECRNameDefine.BOMINFO_IS_AFTERSALE, 75);
			put(VESECRNameDefine.BOMINFO_EXCHANGEABILITY, 77);
			put(VESECRNameDefine.BOMINFO_PART_TRACE, 81);
		}
	};

	private static String[] GROUP_PERMISSION = { "dba" };

	public VESECRBomInfoUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			dlg = new VESECRBomInfoUpdate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Update Bom Information - " + selectedObject.toString());

			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				dlg.btnRecover.setVisible(false);
			}

			dlg.btnRecover.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
					fileDialog.setText("Open");
					fileDialog.setFilterPath("C:/");
					String[] filterExt = { "*.xlsx", "*.xls" };
					fileDialog.setFilterExtensions(filterExt);
					recoverBomInfo(fileDialog.open());
				}
			});

			dlg.btnUp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					switchLine(true);
				}
			});

			dlg.btnDown.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					switchLine(false);
				}
			});

			dlg.btnAddBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						addBomline();
					} catch (Exception ex) {
						dlg.setMessage(ex.toString(), IMessageProvider.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnRemoveBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						removeBomline(false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnChangeTypeUpdateAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Change Type", changeTypeDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnSteeringUpdateAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Steering", steeringDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnExchangeabilityUpdateAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Exchangeability", exchangeabilityDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnMaturityLevel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Maturity Level", maturityLevelDataForm);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectAll();
				}
			});

			dlg.btnAddRemovePart.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addRemovePartBomline();
				}
			});

			dlg.tblBom.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (dlg.tblBom.getSelectionIndex() >= 0)
						editBomlineInfo();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						if (validate()) {
							createBOMInformation();
							removeBomline(true);
							selectedObject.refresh();
							dlg.setMessage("Update success.", IMessageProvider.INFORMATION);
						} else {
							dlg.setMessage("BOM Info not valid.", IMessageProvider.ERROR);
							dlg.tblBom.deselectAll();
						}
					} catch (Exception ex) {
						dlg.setMessage(ex.toString());
						ex.printStackTrace();
					}
				}
			});

			updateCurrentBomline();
			selectAll();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void recoverBomInfo(String path) {
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				@SuppressWarnings("resource")
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowCount = 0;
				for (Row row : sheet) {
					rowCount++;
					if (rowCount > 1) {
						LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
						for (Map.Entry<String, Integer> bomInfoTemp : bomInfoTemplateRecover.entrySet()) {
							try {
								String value = row.getCell(bomInfoTemp.getValue()).getStringCellValue();
								output.put(bomInfoTemp.getKey(), value);
							} catch (Exception e) {
								System.out.print(bomInfoTemp.getValue());
							}
						}
						addTableRow(output, -1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void switchLine(boolean isUp) {
		int minIndex = dlg.tblBom.getItemCount() - 1;
		LinkedList<LinkedHashMap<String, String>> rowChooseList = new LinkedList<LinkedHashMap<String, String>>();

		int[] selectIndexs = dlg.tblBom.getSelectionIndices();

		Integer[] selectIndexsSort = new Integer[selectIndexs.length];
		int i = 0;
		for (int integer : selectIndexs) {
			selectIndexsSort[i++] = integer;
		}

		Arrays.sort(selectIndexsSort, Collections.reverseOrder());

		for (int selectIndex : selectIndexsSort) {
			TableItem bomlineSelect = dlg.tblBom.getItem(selectIndex);
			LinkedHashMap<String, String> bomInfo = new LinkedHashMap<String, String>();
			i = 0;
			for (Map.Entry<String, String> bom : bomInfoTemplate.entrySet())
				bomInfo.put(bom.getKey(), bomlineSelect.getText(i++));

			rowChooseList.add(bomInfo);
			if (selectIndex < minIndex)
				minIndex = selectIndex;
		}

		if (isUp) {
			if (minIndex > 0)
				minIndex--;
		} else {
			if (minIndex < dlg.tblBom.getItemCount() - selectIndexsSort.length)
				minIndex++;
		}

		for (Integer integer : selectIndexsSort) {
			dlg.tblBom.remove(integer);
		}

		i = 0;
		int minIndex1 = minIndex;
		int[] reSelectIndexs = new int[rowChooseList.size()];
		for (LinkedHashMap<String, String> rowSelect : rowChooseList) {
			addTableRow(rowSelect, minIndex);
			reSelectIndexs[i++] = minIndex1++;
		}

		dlg.tblBom.setFocus();
		dlg.tblBom.setSelection(reSelectIndexs);
	}

	private boolean validate() {
		boolean check = true;
		int indexCombineStatus = -1;
		int indexNewPartStatus = -1;
		try {
			String newPartStatus = selectedObject.getPropertyDisplayableValue("vf6_new_parts_status");
			if (!newPartStatus.isEmpty()) {
				indexNewPartStatus = TCExtension.getReleaseStatusIndex(newPartStatus);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (TableItem item : dlg.tblBom.getItems()) {
			int i = 0;
			String partNumber = "";
			String changeType = "";
			String maturityLevel = "";
			for (Map.Entry<String, String> bomTemp : bomInfoTemplate.entrySet()) {
				if (!bomTemp.getKey().isEmpty()) {
					String value = "";
					if (bomTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_PART_NUMBER) == 0) {
						partNumber = value;
					} else if (bomTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_CHANGE_TYPE) == 0) {
						changeType = item.getText(i);
					} else if (bomTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_MATURITY_LEVEL) == 0) {
						maturityLevel = item.getText(i);
					}

					value = item.getText(i);

					if (!checkValueMandatory(bomTemp.getKey(), value, partNumber, changeType, indexNewPartStatus != -1 ? indexNewPartStatus : indexCombineStatus, maturityLevel)) {
						check = false;
						item.setBackground(i, warningColor);
					} else {
						item.setBackground(i, normalColor);
					}
				}
				i++;
			}
		}
		return check;
	}

	private boolean checkValueMandatory(String property, String value, String partNumber, String changeType, int indexCombineStatus, String maturityLevel) {
		if (!value.isEmpty())
			return true;

		if (property.compareTo(VESECRNameDefine.BOMINFO_MATURITY_LEVEL) == 0)
			return false;
		if (property.compareTo(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL) == 0)
			return false;
		if (partNumber.length() > 2 && partNumber.substring(0, 3).compareToIgnoreCase("STD") == 0) {
			if (property.compareTo(VESECRNameDefine.BOMINFO_TORQUE_INFO) == 0)
				return false;
		}
		if (changeType.compareToIgnoreCase("SWAP") == 0) {
			if (property.compareTo(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART) == 0)
				return false;

			if (maturityLevel.compareTo("PR") == 0 || maturityLevel.compareTo("PPR") == 0) {
				if (property.compareTo(VESECRNameDefine.BOMINFO_EXCHANGEABILITY) == 0)
					return false;
			}
		}
		if (indexCombineStatus > -1) {
			int indexIStatus = TCExtension.getReleaseStatusIndex("I");
			if (indexCombineStatus >= indexIStatus) {
				if (property.compareTo(VESECRNameDefine.BOMINFO_IS_AFTERSALE) == 0)
					if (!partNumber.contains("_JF"))
						return false;
			}
			int indexPRStatus = TCExtension.getReleaseStatusIndex("PR");
			if (indexCombineStatus >= indexPRStatus) {
				if (property.compareTo(VESECRNameDefine.BOMINFO_PART_TRACE) == 0)
					return false;
			}
		}
		if (changeType.compareToIgnoreCase("REMOVE") != 0)
			if (property.compareTo(VESECRNameDefine.BOMINFO_POSID) == 0)
				return false;
		if (property.compareTo(VESECRNameDefine.BOMINFO_QUANTITY) == 0)
			return false;

		return true;
	}

	private void editBomlineInfo() {
		int editIndex = dlg.tblBom.getSelectionIndex();
		TableItem bomlineSelect = dlg.tblBom.getItem(editIndex);
		LinkedHashMap<String, String> bomInfo = new LinkedHashMap<String, String>();
		int i = 0;
		for (Map.Entry<String, String> bom : bomInfoTemplate.entrySet()) {
			bomInfo.put(bom.getKey(), bomlineSelect.getText(i));
			i++;
		}

		VESECRBomInfoEdit_Dialog addDlg = new VESECRBomInfoEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, bomInfo);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put(VESECRNameDefine.BOMINFO_CHANGE_TYPE, addDlg.cbChangeType.getText());
						put(VESECRNameDefine.BOMINFO_POSID, addDlg.txtPOSID.getText());
						put(VESECRNameDefine.BOMINFO_DONOR_VEHICLE, addDlg.cbDonorVehicle.getText());
						put(VESECRNameDefine.BOMINFO_STRUCTURE_LEVEL, addDlg.txtLevel.getText());
						put(VESECRNameDefine.BOMINFO_STEERING, addDlg.cbSteering.getText());
						put(VESECRNameDefine.BOMINFO_QUANTITY, addDlg.txtQuantity.getText());
						put(VESECRNameDefine.BOMINFO_MATURITY_LEVEL, addDlg.cbMaturityLevel.getText());
						put(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL, addDlg.cbPurchaseLevel.getText());
						put(VESECRNameDefine.BOMINFO_PART_NUMBER, addDlg.txtPartNo.getText());
						put(VESECRNameDefine.BOMINFO_OLD_VERSION, addDlg.txtOldRev.getText());
						put(VESECRNameDefine.BOMINFO_FROZEN_REVISION, addDlg.txtFrozenRev.getText());
						put(VESECRNameDefine.BOMINFO_NEW_VERSION, addDlg.txtNewRev.getText());
						put(VESECRNameDefine.BOMINFO_PART_NAME, addDlg.txtPartName.getText());
						put(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART, addDlg.txtOriginalPart.getText());
						put(VESECRNameDefine.BOMINFO_VARIANT_FORMULA, addDlg.txtVariantFormula.getText());
						put(VESECRNameDefine.BOMINFO_TORQUE_INFO, addDlg.txtTorqueInfo.getText());
						put(VESECRNameDefine.BOMINFO_WEIGHT, addDlg.txtWeight.getText());
						put(VESECRNameDefine.BOMINFO_CHANGE_DESC, addDlg.txtChangeDesc.getText());
						put(VESECRNameDefine.BOMINFO_3D_DATA_AFFECTED, addDlg.ckb3DData.getSelection() ? "Yes" : "");
						put(VESECRNameDefine.BOMINFO_MATERIAL, addDlg.txtMaterial.getText());
						put(VESECRNameDefine.BOMINFO_CAD_COATING, addDlg.txtCoating.getText());
						put(VESECRNameDefine.BOMINFO_SPEC_BOOK, addDlg.txtSpecbook.getText());
						put(VESECRNameDefine.BOMINFO_SUPPLIER, addDlg.txtSupplier.getText());
						put(VESECRNameDefine.BOMINFO_SUPPLIER_CONTACT, addDlg.txtSupplierContact.getText());
						put(VESECRNameDefine.BOMINFO_IS_AFTERSALE, addDlg.cbAftersaleRelevant.getText());
						put(VESECRNameDefine.BOMINFO_EXCHANGEABILITY, addDlg.cbExchangeability.getText());
						put(VESECRNameDefine.BOMINFO_PART_TRACE, addDlg.cbPartTraceability.getText());
					}
				};
				addDlg.close();
				editTableRow(bomline, editIndex);
			}
		});
	}

	private void addRemovePartBomline() {
		ECRBomInfoEdit_Dialog addDlg = new ECRBomInfoEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, null);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int addIndex = -1;
				for (int i = 0; i < dlg.tblBom.getItemCount(); i++) {
					TableItem rowItem = dlg.tblBom.getItem(i);
					if (rowItem.getChecked())
						addIndex = i;
				}
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put(VESECRNameDefine.BOMINFO_CHANGE_TYPE, addDlg.cbChangeType.getText());
						put(VESECRNameDefine.BOMINFO_POSID, addDlg.txtPOSID.getText());
						put(VESECRNameDefine.BOMINFO_DONOR_VEHICLE, addDlg.cbDonorVehicle.getText());
						put(VESECRNameDefine.BOMINFO_STRUCTURE_LEVEL, addDlg.txtLevel.getText());
						put(VESECRNameDefine.BOMINFO_STEERING, addDlg.cbSteering.getText());
						put(VESECRNameDefine.BOMINFO_QUANTITY, addDlg.txtQuantity.getText());
						put(VESECRNameDefine.BOMINFO_MATURITY_LEVEL, addDlg.cbMaturityLevel.getText());
						put(VESECRNameDefine.BOMINFO_PURCHASE_LEVEL, addDlg.cbPurchaseLevel.getText());
						put(VESECRNameDefine.BOMINFO_PART_NUMBER, addDlg.txtPartNo.getText());
						put(VESECRNameDefine.BOMINFO_OLD_VERSION, addDlg.txtOldRev.getText());
						put(VESECRNameDefine.BOMINFO_FROZEN_REVISION, addDlg.txtFrozenRev.getText());
						put(VESECRNameDefine.BOMINFO_NEW_VERSION, addDlg.txtNewRev.getText());
						put(VESECRNameDefine.BOMINFO_PART_NAME, addDlg.txtPartName.getText());
						put(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART, addDlg.txtOriginalPart.getText());
						put(VESECRNameDefine.BOMINFO_VARIANT_FORMULA, addDlg.txtVariantFormula.getText());
						put(VESECRNameDefine.BOMINFO_TORQUE_INFO, addDlg.txtTorqueInfo.getText());
						put(VESECRNameDefine.BOMINFO_WEIGHT, addDlg.txtWeight.getText());
						put(VESECRNameDefine.BOMINFO_CHANGE_DESC, addDlg.txtChangeDesc.getText());
						put(VESECRNameDefine.BOMINFO_3D_DATA_AFFECTED, addDlg.ckb3DData.getSelection() ? "True" : "False");
						put(VESECRNameDefine.BOMINFO_MATERIAL, addDlg.txtMaterial.getText());
						put(VESECRNameDefine.BOMINFO_CAD_COATING, addDlg.txtCoating.getText());
						put(VESECRNameDefine.BOMINFO_SPEC_BOOK, addDlg.txtSpecbook.getText());
						put(VESECRNameDefine.BOMINFO_SUPPLIER, addDlg.txtSupplier.getText());
						put(VESECRNameDefine.BOMINFO_SUPPLIER_CONTACT, addDlg.txtSupplierContact.getText());
						put(VESECRNameDefine.BOMINFO_IS_AFTERSALE, addDlg.cbAftersaleRelevant.getText());
						put(VESECRNameDefine.BOMINFO_EXCHANGEABILITY, addDlg.cbExchangeability.getText());
						put(VESECRNameDefine.BOMINFO_PART_TRACE, addDlg.cbPartTraceability.getText());
					}
				};
				addDlg.clearUI();
				addTableRow(bomline, ++addIndex);
			}
		});
	}

	private void selectAll() {
		for (TableItem item : dlg.tblBom.getItems()) {
			item.setChecked(dlg.ckbCheckAll.getSelection());
		}
	}

	private void massUpdate(String title, String[] values) {
		MassUpdateComboboxValue_Dialog massUpdateDlg = new MassUpdateComboboxValue_Dialog(dlg.getShell());
		massUpdateDlg.create();
		massUpdateDlg.lblValue.setText(title);
		massUpdateDlg.cbValue.setItems(values);
		massUpdateDlg.open();
		Button okButton = massUpdateDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				for (TableItem tableItem : dlg.tblBom.getItems()) {
					if (tableItem.getChecked()) {
						if (title.compareToIgnoreCase("Change Type") == 0)
							tableItem.setText(getPosBomInfoTemplate(VESECRNameDefine.BOMINFO_CHANGE_TYPE), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Steering") == 0)
							tableItem.setText(getPosBomInfoTemplate(VESECRNameDefine.BOMINFO_STEERING), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Exchangeability") == 0)
							tableItem.setText(getPosBomInfoTemplate(VESECRNameDefine.BOMINFO_EXCHANGEABILITY), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Maturity Level") == 0)
							tableItem.setText(getPosBomInfoTemplate(VESECRNameDefine.BOMINFO_MATURITY_LEVEL), massUpdateDlg.valueSelected);
					}
				}
			}
		});
	}

	private void createBOMInformation() throws Exception {
		Set<String> partIDList = new HashSet<String>();
		List<Map<String, String>> bomAttributesAndValsList = new LinkedList<Map<String, String>>();
		for (TableItem tableItem : dlg.tblBom.getItems()) {
			int i = 0;
			Map<String, String> mapItem = new HashMap<String, String>();
			for (Map.Entry<String, String> bomTemp : bomInfoTemplate.entrySet()) {
				if (!bomTemp.getKey().isEmpty())
					mapItem.put(bomTemp.getKey(), tableItem.getText(i));
				if (bomTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_PART_NUMBER) == 0)
					partIDList.add(tableItem.getText(i));
				if (bomTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_ORIGINAL_BASE_PART) == 0) {
					if (!tableItem.getText(i).isEmpty())
						partIDList.add(tableItem.getText(i));
				}
				i++;
			}
			bomAttributesAndValsList.add(mapItem);
		}

		if (bomAttributesAndValsList.size() > 0) {
			CreateIn[] createInputs = new CreateIn[bomAttributesAndValsList.size()];
			for (int bomListIndex = 0; bomListIndex < bomAttributesAndValsList.size(); bomListIndex++) {
				Map<String, String> bomAttributesAndVals = bomAttributesAndValsList.get(bomListIndex);
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "VF4_bom_infomation";
				createInput.data.stringProps.putAll(bomAttributesAndVals);

				createInputs[bomListIndex] = createInput;
			}
			CreateResponse response = dmService.createObjects(createInputs);
			if (response.serviceData.sizeOfPartialErrors() > 0)
				throw new Exception(TCExtension.hanlderServiceData(response.serviceData));

			List<TCComponent> rows = new LinkedList<TCComponent>();
			for (CreateOut output : response.output) {
				rows.addAll(Arrays.asList(output.objects));
			}

			int bomListIndex = 0;
			for (TCComponent row : rows) {
				Map<String, String> bomAttributesAndVals = bomAttributesAndValsList.get(bomListIndex++);

				try {
					row.setProperties(bomAttributesAndVals);
				} catch (TCException ex) {
					try {
						if (ex.getErrorCode() == 38009) {
							String quantityStr = bomAttributesAndVals.get(VESECRNameDefine.BOMINFO_QUANTITY);
							double quantityDouble = Double.parseDouble(quantityStr);
							int quantity = (int) Math.floor(quantityDouble);
							quantityStr = Integer.toString(quantity);
							bomAttributesAndVals.put(VESECRNameDefine.BOMINFO_QUANTITY, quantityStr);
							row.setProperties(bomAttributesAndVals);
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw ex;
					}
				}
			}
			selectedObject.setRelated("vf4_bom_infomation", rows.toArray(new TCComponent[0]));
		} else {
			selectedObject.setRelated("vf4_bom_infomation", new TCComponent[0]);
		}
		selectedObject.refresh();
	}

	private void updateCurrentBomline() {
		try {
			TCComponent[] bomInfo = selectedObject.getRelatedComponents("vf4_bom_infomation");
			if (bomInfo.length > 0) {
				for (TCComponent bomline : bomInfo) {
					LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
					for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
						if (!bomInfoTemp.getKey().isEmpty()) {
							String value = bomline.getPropertyDisplayableValue(bomInfoTemp.getKey());
							output.put(bomInfoTemp.getKey(), value);
						}
					}
					addTableRow(output, -1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addBomline() throws Exception {
		int addIndex = dlg.tblBom.getItemCount() - 1;
		for (int i = 0; i < dlg.tblBom.getItemCount(); i++) {
			TableItem rowItem = dlg.tblBom.getItem(i);
			if (rowItem.getChecked())
				addIndex = i;
		}
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 0) {
			for (InterfaceAIFComponent comp : targetComp) {
				if (comp instanceof TCComponentBOMLine) {
					LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
					TCComponentBOMLine bomline = (TCComponentBOMLine) comp;
					for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
						String value = "";
						if (!bomInfoTemp.getKey().isEmpty()) {
							if (bomInfoTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_FROZEN_REVISION) == 0) {
								value = getFrozenVersion(bomline.getItemRevision());
							} else if (bomInfoTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_MATURITY_LEVEL) == 0) {
								value = bomline.getPropertyDisplayableValue(bomInfoTemp.getValue());
								if (value.isEmpty())
									value = "Not released";
								else {
									int indexStatus = -1;
									if (value.contains(";")) {
										for (String item : value.split(";")) {
											int index = TCExtension.getReleaseStatusIndex(item);
											if (index > indexStatus) {
												indexStatus = index;
											}
										}
									} else {
										int index = TCExtension.getReleaseStatusIndex(value);
										if (index < indexStatus) {
											indexStatus = index;
										}
									}
									if (indexStatus > -1) {
										value = TCExtension.releaseStatusList[indexStatus];
									} else {
										value = "Not released";
									}
								}
							} else if (bomInfoTemp.getKey().compareTo(VESECRNameDefine.BOMINFO_SUPPLIER) == 0) {
								value = bomline.getPropertyDisplayableValue("bl_item_vf4cp_itm_supplier_code") + " - " + bomline.getPropertyDisplayableValue("bl_item_vf4cp_itm_supplier_name");
							} else {
								if (!bomInfoTemp.getValue().isEmpty()) {
									value = bomline.getPropertyDisplayableValue(bomInfoTemp.getValue());
								}
							}
						}
						output.put(bomInfoTemp.getKey(), value);
					}
					addTableRow(output, ++addIndex);
				}
			}
		}
	}

	private void removeBomline(boolean removeAll) {
		if (removeAll) {
			dlg.tblBom.removeAll();
		} else {
			int totalDel = 0;
			for (TableItem tableItem : dlg.tblBom.getItems()) {
				if (tableItem.getChecked()) {
					totalDel++;
				}
			}
			for (int j = 0; j < totalDel; j++) {
				int i = 0;
				for (TableItem tableItem : dlg.tblBom.getItems()) {
					if (tableItem.getChecked()) {
						totalDel++;
						dlg.tblBom.remove(i);
						break;
					}
					i++;
				}
			}
		}
		dlg.tblBom.setFocus();
		dlg.tblBom.selectAll();
	}

	private int getPosBomInfoTemplate(String value) {
		if (bomInfoTemplate.containsKey(value)) {
			return new ArrayList<String>(bomInfoTemplate.keySet()).indexOf(value);
		}
		return 0;
	}

	private String getFrozenVersion(TCComponentItemRevision partRevision) {
		if (partRevision == null)
			return "";
		try {
			TCComponentItemRevision[] baselineRevList = ((TCComponentItemRevision) partRevision).listBaselineRevs("");
			if (baselineRevList != null && baselineRevList.length > 0) {
				String max = "000";
				for (TCComponentItemRevision baselineRev : baselineRevList) {
					String revision = baselineRev.getPropertyDisplayableValue("current_revision_id");
					if (revision.contains(".")) {
						String[] str = revision.split("\\.");
						if (Integer.parseInt(max) < Integer.parseInt(str[1])) {
							max = str[1];
						}
					}
				}
				if (max.compareTo("000") != 0)
					return max;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addTableRow(LinkedHashMap<String, String> bomline, int addIndex) {
		List<String> tableValue = new LinkedList<String>();
		for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomline.get(bomInfoTemp.getKey());
			}
			tableValue.add(value);
		}

		TableItem item = null;
		if (addIndex < 0)
			item = new TableItem(dlg.tblBom, SWT.NONE);
		else
			item = new TableItem(dlg.tblBom, SWT.NONE, addIndex);
		item.setText(tableValue.toArray(new String[0]));
	}

	private void editTableRow(LinkedHashMap<String, String> bomline, int editIndex) {
		List<String> tableValue = new LinkedList<String>();
		for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
			String value = "";
			if (!bomInfoTemp.getKey().isEmpty()) {
				value = bomline.get(bomInfoTemp.getKey());
			}
			tableValue.add(value);
		}

		TableItem item = dlg.tblBom.getItem(editIndex);
		item.setText(tableValue.toArray(new String[0]));
	}
}
