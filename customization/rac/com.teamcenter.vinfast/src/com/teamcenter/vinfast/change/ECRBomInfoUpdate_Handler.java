package com.teamcenter.vinfast.change;

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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.MassUpdateComboboxValue_Dialog;
import com.vf.utils.APIExtension;
import com.vf.utils.TCExtension;

public class ECRBomInfoUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private ECRBomInfoUpdate_Dialog dlg;
	private TCComponent selectedObject = null;
	private Set<String> ecrImpactedProgram = null;
	private LinkedHashMap<String, String> vehicleGroupMapping = null;

	private Color warningColor = new Color(null, 255, 73, 97);
	private Color normalColor = new Color(null, 255, 255, 255);
	private LinkedHashMap<String, String> statusReleaseDataForm = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("P", "P Release");
			put("I", "I Release");
			put("PR", "PR Release");
		}
	};

	private String[] maturityLevelDataForm = { "Not released", "P", "I", "PR", "PPR" };
	private String[] changeTypeDataForm = { "NEW", "ADD", "CHANGE", "SWAP", "REMOVE" };
	private String[] steeringDataForm = { "All", "LHD", "RHD" };
	private String[] exchangeabilityDataForm = { "Fully interchangeable-stock OK to use for any vehicle", "New for old only-stock OK to service old vehicles", "New for old only-do not use stock", "Not interchangeable-stock OK to service old vehicles", "Not interchangeable-do not use stock" };
	private LinkedHashMap<String, String> bomInfoTemplate = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("", "");
			put("vf6_change_type", "");
			put("vf6_posid", "VL5_pos_id");
			put("vf6_donor_vehicle", "vf4_DsnRev_donor_veh_type");
			put("vf6_structure_level", "bl_level_starting_0");
			put("vf6_steering", "");
			put("vf6_quantity2", "bl_quantity");
			put("vf6_maturity_level", "bl_item_vf4cp_maturity_level");
			put("vf6_purchase_level", "VL5_purchase_lvl_vf");
			put("vf6_part_number", "awb0BomLineItemId");
			put("vf6_old_version", "");
			put("vf6_frozen_revision", "");
			put("vf6_new_revision", "bl_rev_item_revision_id");
			put("vf6_part_name", "bl_item_object_name");
			put("vf6_original_base_part", "bl_item_vf4_orginal_part_number");
			put("vf6_variant_formula", "bl_formula");
			put("vf6_torque_information", "VL5_torque_inf");
			put("vf6_weight", "bl_rev_vf4_cad_weight");
			put("vf6_change_description", "");
			put("vf6_3d_data_affected", "");
			put("vf6_material", "vf4_catia_material");
			put("vf6_cad_coating", "bl_rev_vf4_cad_coating");
			put("vf6_specbook", "bl_rev_vl5_specbook_num");
			put("vf6_supplier", "");
			put("vf6_supplier_contact", "");
			put("vf6_is_aftersale_relevaant", "bl_item_vf4_itm_after_sale_relevant");
			put("vf6_exchangeability", "");
			put("vf6_part_tracebility", "bl_item_vf4_item_is_traceable");
			put("vf6_cop_impacted_program", "");
		}
	};

	private LinkedHashMap<String, Integer> bomInfoTemplateRecover = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("vf6_change_type", 11);
			put("vf6_posid", 0);
			put("vf6_donor_vehicle", 3);
			put("vf6_structure_level", 5);
			put("vf6_steering", 6);
			put("vf6_quantity2", 7);
			put("vf6_maturity_level", 8);
			put("vf6_purchase_level", 10);
			put("vf6_part_number", 13);
			put("vf6_old_version", 16);
			put("vf6_frozen_revision", 17);
			put("vf6_new_revision", 18);
			put("vf6_part_name", 19);
			put("vf6_original_base_part", 27);
			put("vf6_variant_formula", 31);
			put("vf6_torque_information", 36);
			put("vf6_weight", 41);
			put("vf6_change_description", 43);
			put("vf6_3d_data_affected", 54);
			put("vf6_material", 55);
			put("vf6_cad_coating", 58);
			put("vf6_specbook", 63);
			put("vf6_supplier", 67);
			put("vf6_supplier_contact", 71);
			put("vf6_is_aftersale_relevaant", 75);
			put("vf6_exchangeability", 77);
			put("vf6_part_tracebility", 81);
		}
	};

	private static String[] GROUP_PERMISSION = { "dba" };

	public ECRBomInfoUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			ecrImpactedProgram = new HashSet<String>();

			vehicleGroupMapping = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", "VF4_Design", session);

			dlg = new ECRBomInfoUpdate_Dialog(new Shell());
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

//			dlg.ckbCombineRelease.addListener(SWT.Selection, new Listener() {
//				public void handleEvent(Event arg0) {
//					dlg.cbCombineRelease.setEnabled(dlg.ckbCombineRelease.getSelection());
//				}
//			});

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
							updateImpactedProgramECRRev();
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
					if (bomTemp.getKey().compareTo("vf6_part_number") == 0) {
						partNumber = value;
					} else if (bomTemp.getKey().compareTo("vf6_change_type") == 0) {
						changeType = item.getText(i);
					} else if (bomTemp.getKey().compareTo("vf6_maturity_level") == 0) {
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

		if (property.compareTo("vf6_maturity_level") == 0)
			return false;
		if (property.compareTo("vf6_purchase_level") == 0)
			return false;
		if (partNumber.length() > 2 && partNumber.substring(0, 3).compareToIgnoreCase("STD") == 0) {
			if (property.compareTo("vf6_torque_information") == 0)
				return false;
		}
		if (changeType.compareToIgnoreCase("SWAP") == 0) {
			if (property.compareTo("vf6_original_base_part") == 0)
				return false;

			if (maturityLevel.compareTo("PR") == 0 || maturityLevel.compareTo("PPR") == 0) {
				if (property.compareTo("vf6_exchangeability") == 0)
					return false;
			}
		}
		if (indexCombineStatus > -1) {
			int indexIStatus = TCExtension.getReleaseStatusIndex("I");
			if (indexCombineStatus >= indexIStatus) {
				if (property.compareTo("vf6_is_aftersale_relevaant") == 0)
					if (!partNumber.contains("_JF"))
						return false;
			}
			int indexPRStatus = TCExtension.getReleaseStatusIndex("PR");
			if (indexCombineStatus >= indexPRStatus) {
				if (property.compareTo("vf6_part_tracebility") == 0)
					return false;
			}
		}
		if (changeType.compareToIgnoreCase("REMOVE") != 0)
			if (property.compareTo("vf6_posid") == 0)
				return false;
		if (property.compareTo("vf6_donor_vehicle") == 0)
			return false;
		if (property.compareTo("vf6_quantity2") == 0)
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

		ECRBomInfoEdit_Dialog addDlg = new ECRBomInfoEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, bomInfo);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put("vf6_change_type", addDlg.cbChangeType.getText());
						put("vf6_posid", addDlg.txtPOSID.getText());
						put("vf6_donor_vehicle", addDlg.cbDonorVehicle.getText());
						put("vf6_structure_level", addDlg.txtLevel.getText());
						put("vf6_steering", addDlg.cbSteering.getText());
						put("vf6_quantity2", addDlg.txtQuantity.getText());
						put("vf6_maturity_level", addDlg.cbMaturityLevel.getText());
						put("vf6_purchase_level", addDlg.cbPurchaseLevel.getText());
						put("vf6_part_number", addDlg.txtPartNo.getText());
						put("vf6_old_version", addDlg.txtOldRev.getText());
						put("vf6_frozen_revision", addDlg.txtFrozenRev.getText());
						put("vf6_new_revision", addDlg.txtNewRev.getText());
						put("vf6_part_name", addDlg.txtPartName.getText());
						put("vf6_original_base_part", addDlg.txtOriginalPart.getText());
						put("vf6_variant_formula", addDlg.txtVariantFormula.getText());
						put("vf6_torque_information", addDlg.txtTorqueInfo.getText());
						put("vf6_weight", addDlg.txtWeight.getText());
						put("vf6_change_description", addDlg.txtChangeDesc.getText());
						put("vf6_3d_data_affected", addDlg.ckb3DData.getSelection() ? "Yes" : "");
						put("vf6_material", addDlg.txtMaterial.getText());
						put("vf6_cad_coating", addDlg.txtCoating.getText());
						put("vf6_specbook", addDlg.txtSpecbook.getText());
						put("vf6_supplier", addDlg.txtSupplier.getText());
						put("vf6_supplier_contact", addDlg.txtSupplierContact.getText());
						put("vf6_is_aftersale_relevaant", addDlg.cbAftersaleRelevant.getText());
						put("vf6_exchangeability", addDlg.cbExchangeability.getText());
						put("vf6_part_tracebility", addDlg.cbPartTraceability.getText());
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
						put("vf6_change_type", addDlg.cbChangeType.getText());
						put("vf6_posid", addDlg.txtPOSID.getText());
						put("vf6_donor_vehicle", addDlg.cbDonorVehicle.getText());
						put("vf6_structure_level", addDlg.txtLevel.getText());
						put("vf6_steering", addDlg.cbSteering.getText());
						put("vf6_quantity2", addDlg.txtQuantity.getText());
						put("vf6_maturity_level", addDlg.cbMaturityLevel.getText());
						put("vf6_purchase_level", addDlg.cbPurchaseLevel.getText());
						put("vf6_part_number", addDlg.txtPartNo.getText());
						put("vf6_old_version", addDlg.txtOldRev.getText());
						put("vf6_frozen_revision", addDlg.txtFrozenRev.getText());
						put("vf6_new_revision", addDlg.txtNewRev.getText());
						put("vf6_part_name", addDlg.txtPartName.getText());
						put("vf6_original_base_part", addDlg.txtOriginalPart.getText());
						put("vf6_variant_formula", addDlg.txtVariantFormula.getText());
						put("vf6_torque_information", addDlg.txtTorqueInfo.getText());
						put("vf6_weight", addDlg.txtWeight.getText());
						put("vf6_change_description", addDlg.txtChangeDesc.getText());
						put("vf6_3d_data_affected", addDlg.ckb3DData.getSelection() ? "True" : "False");
						put("vf6_material", addDlg.txtMaterial.getText());
						put("vf6_cad_coating", addDlg.txtCoating.getText());
						put("vf6_specbook", addDlg.txtSpecbook.getText());
						put("vf6_supplier", addDlg.txtSupplier.getText());
						put("vf6_supplier_contact", addDlg.txtSupplierContact.getText());
						put("vf6_is_aftersale_relevaant", addDlg.cbAftersaleRelevant.getText());
						put("vf6_exchangeability", addDlg.cbExchangeability.getText());
						put("vf6_part_tracebility", addDlg.cbPartTraceability.getText());
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
							tableItem.setText(getPosBomInfoTemplate("vf6_change_type"), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Steering") == 0)
							tableItem.setText(getPosBomInfoTemplate("vf6_steering"), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Exchangeability") == 0)
							tableItem.setText(getPosBomInfoTemplate("vf6_exchangeability"), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Maturity Level") == 0)
							tableItem.setText(getPosBomInfoTemplate("vf6_maturity_level"), massUpdateDlg.valueSelected);
					}
				}
			}
		});
	}

	private void updateImpactedProgramECRRev() {
		try {
			Set<String> programConvertList = convertProgram(ecrImpactedProgram);

			selectedObject.setProperty("vf6_cop_impacted_program", String.join(", ", programConvertList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createBOMInformation() throws Exception {
		DataManagementService dms = DataManagementService.getService(session);
		TCComponent[] forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
		TCComponent ecrForm = null;
		if (forms.length > 0) {
			ecrForm = forms[0];
		} else {
			ecrForm = creatEcrFormAndAttachToECR(selectedObject);
		}

		Set<String> partIDList = new HashSet<String>();
		List<Map<String, String>> bomAttributesAndValsList = new LinkedList<Map<String, String>>();
		for (TableItem tableItem : dlg.tblBom.getItems()) {
			int i = 0;
			Map<String, String> mapItem = new HashMap<String, String>();
			for (Map.Entry<String, String> bomTemp : bomInfoTemplate.entrySet()) {
				if (!bomTemp.getKey().isEmpty())
					mapItem.put(bomTemp.getKey(), tableItem.getText(i));
				if (bomTemp.getKey().compareTo("vf6_part_number") == 0)
					partIDList.add(tableItem.getText(i));
				if (bomTemp.getKey().compareTo("vf6_original_base_part") == 0) {
					if (!tableItem.getText(i).isEmpty())
						partIDList.add(tableItem.getText(i));
				}
				i++;
			}
			bomAttributesAndValsList.add(mapItem);
		}

//		getCOPImpactedProgram(partIDList, bomAttributesAndValsList);

		if (bomAttributesAndValsList.size() > 0) {
			CreateIn[] createInputs = new CreateIn[bomAttributesAndValsList.size()];
			for (int bomListIndex = 0; bomListIndex < bomAttributesAndValsList.size(); bomListIndex++) {
				Map<String, String> bomAttributesAndVals = bomAttributesAndValsList.get(bomListIndex);
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "Vf6_bom_infomation";
				createInput.data.stringProps.putAll(bomAttributesAndVals);

				createInputs[bomListIndex] = createInput;
			}
			CreateResponse response = dms.createObjects(createInputs);
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
							String quantityStr = bomAttributesAndVals.get("vf6_quantity2");
							double quantityDouble = Double.parseDouble(quantityStr);
							int quantity = (int) Math.floor(quantityDouble);
							quantityStr = Integer.toString(quantity);
							bomAttributesAndVals.put("vf6_quantity2", quantityStr);
							row.setProperties(bomAttributesAndVals);
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw ex;
					}
				}
			}
			ecrForm.setRelated("vf6_bom_information", rows.toArray(new TCComponent[0]));
			ecrForm.refresh();
		} else {
			ecrForm.setRelated("vf6_bom_information", new TCComponent[0]);
		}
	}

	private void updateCurrentBomline() {
		try {
			TCComponent[] forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
			if (forms.length > 0) {
				TCComponent[] bomInfo = forms[0].getRelatedComponents("vf6_bom_information");
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
							if (bomInfoTemp.getKey().compareTo("vf6_frozen_revision") == 0) {
								value = getFrozenVersion(bomline.getItemRevision());
							} else if (bomInfoTemp.getKey().compareTo("vf6_maturity_level") == 0) {
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
							} else if (bomInfoTemp.getKey().compareTo("vf6_supplier") == 0) {
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

	private TCComponent creatEcrFormAndAttachToECR(TCComponent ecrRev) throws Exception {
		Map<String, String> stringProps = new HashMap<String, String>();
		String ecrRevName = ecrRev.getProperty("object_name");
		stringProps.put("object_name", ecrRevName);
		CreateIn input = new CreateIn();
		input.data.boName = ECRInputManagement.ECR_FORM_TYPE;
		input.data.stringProps = stringProps;

		CreateResponse res = dmService.createObjects(new CreateIn[] { input });

		TCComponent newECRForm = null;
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			newECRForm = res.output[0].objects[0];
			ecrRev.setRelated(ECRInputManagement.ECR_FORM_RELATION, new TCComponent[] { newECRForm });
		} else {
			String errorMsg = TCExtension.hanlderServiceData(res.serviceData);
			throw new Exception("Cannot create ECR form with below errors.\n" + errorMsg);
		}

		return newECRForm;
	}

	// ---------------------- COP
	private void getCOPImpactedProgram(Set<String> partIDList, List<Map<String, String>> bomAttributesAndValsList) {
		try {
			if (partIDList != null && partIDList.size() > 0) {
				Set<String> uidList = new HashSet<String>();
				LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
				inputQuery.put("Item ID", String.join(";", partIDList));
				inputQuery.put("Type", "VF Design");

				TCComponent[] itemResults = TCExtension.queryItem(session, inputQuery, "Item...");
				if (itemResults != null) {
					for (TCComponent itemResult : itemResults) {
						TCComponentItem part = null;
						if (itemResult instanceof TCComponentItem) {
							part = (TCComponentItem) itemResult;
							String programValue = selectedObject.getPropertyDisplayableValue("vf6_vehicle_group");
							String donorVehicle = part.getPropertyDisplayableValue("vf4_donor_vehicle");
							if (!donorVehicle.isEmpty()) {
								if (programValue.compareTo("EBUS10") == 0)
									programValue = "Ebus10";
								else if (programValue.compareTo("EBUS12") == 0)
									programValue = "Ebus12";
								else if (programValue.compareTo("S&S") == 0)
									programValue = "SnS";
								else if (programValue.compareTo("VF35") == 0 || programValue.compareTo("VF35/VFe35") == 0 || programValue.compareTo("VFe35") == 0)
									programValue = "VFe35/VF35";
								else if (programValue.compareTo("VF36") == 0 || programValue.compareTo("VF36/VFe36") == 0 || programValue.compareTo("VFe36") == 0)
									programValue = "VFe36/VF36";
								else if (programValue.compareTo("VFe34S") == 0)
									programValue = "VF7";

								if (programValue.compareTo(donorVehicle) == 0)
									uidList.add(part.getUid());
							}
						}
					}
				}

				Map<String, Set<String>> impactedProgramMap = new HashMap<String, Set<String>>();
				if (uidList != null && uidList.size() > 0) {
					impactedProgramMap = APIExtension.getImpactedProgram(uidList);
				}

				for (Map<String, String> bomAttr : bomAttributesAndValsList) {
					String partNumber = bomAttr.get("vf6_part_number");
					Set<String> impactedProgram = impactedProgramMap.get(partNumber);
					if (impactedProgram != null) {
						bomAttr.put("vf6_cop_impacted_program", String.join(", ", convertProgram(impactedProgram)));
						ecrImpactedProgram.addAll(impactedProgram);
					} else {
						bomAttr.put("vf6_cop_impacted_program", "");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Set<String> convertProgram(Set<String> ecrImpactedProgram) {
		Set<String> programConvertList = new HashSet<String>();
		for (String program : ecrImpactedProgram) {
			if (vehicleGroupMapping.containsKey(program))
				programConvertList.add(vehicleGroupMapping.get(program));
		}
		return programConvertList;
	}
}
