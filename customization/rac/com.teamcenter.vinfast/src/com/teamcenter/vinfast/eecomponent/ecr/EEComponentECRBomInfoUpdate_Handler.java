package com.teamcenter.vinfast.eecomponent.ecr;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.teamcenter.vinfast.change.ECRInputManagement;
import com.teamcenter.vinfast.subdialog.MassUpdateComboboxValue_Dialog;
import com.vf.utils.TCExtension;

public class EEComponentECRBomInfoUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private EEComponentECRBomInfoUpdate_Dialog dlg;
	private TCComponent selectedObject = null;

	private Color warningColor = new Color(null, 255, 73, 97);
	private Color normalColor = new Color(null, 255, 255, 255);

	private String[] changeTypeDataForm = { "NEW", "ADD", "CHANGE", "SWAP", "REMOVE" };
	private String[] disposalMaterialDataForm = { "No impact", "Running change", "Rework", "Use for other purpose", "Use at the request of EED" };
	private LinkedHashMap<String, String> bomInfoTemplate = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("", "");
			put(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER, "");
			put(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE, "");
			put(EEComponentECRNameDefine.BOMINFO_CHANGE_POINT, "");
			put(EEComponentECRNameDefine.BOMINFO_DISPOSAL_MATERIAL, "");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER, "awb0BomLineItemId");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME, "bl_item_object_name");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION, "bl_rev_item_revision_id");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY, "bl_quantity");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN, "");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE, "bl_substitute_list");
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR, "VF4_ECPartDesignator");
			put(EEComponentECRNameDefine.BOMINFO_PART_NUMBER, "awb0BomLineItemId");
			put(EEComponentECRNameDefine.BOMINFO_PART_NAME, "bl_item_object_name");
			put(EEComponentECRNameDefine.BOMINFO_PART_REVISION, "bl_rev_item_revision_id");
			put(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY, "bl_quantity");
			put(EEComponentECRNameDefine.BOMINFO_PART_MPN, "");
			put(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE, "bl_substitute_list");
			put(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR, "VF4_ECPartDesignator");
			put(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY, "bl_item_vf4_item_is_traceable");
		}
	};

	private LinkedHashMap<String, Integer> bomInfoTemplateRecover = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE, 11);
			put("vf6_posid", 0);
			put("vf6_donor_vehicle", 3);
			put("vf6_structure_level", 5);
			put("vf6_steering", 6);
			put(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY, 7);
			put("vf6_maturity_level", 8);
			put("vf6_purchase_level", 10);
			put(EEComponentECRNameDefine.BOMINFO_PART_NUMBER, 13);
			put(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION, 16);
			put("vf6_frozen_revision", 17);
			put(EEComponentECRNameDefine.BOMINFO_PART_REVISION, 18);
			put(EEComponentECRNameDefine.BOMINFO_PART_NAME, 19);
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
			put(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY, 81);
		}
	};

	private static String[] GROUP_PERMISSION = { "dba" };

	public EEComponentECRBomInfoUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			dlg = new EEComponentECRBomInfoUpdate_Dialog(new Shell());
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

			dlg.btnAddNewBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						addBomline(true);
					} catch (Exception ex) {
						dlg.setMessage(ex.toString(), IMessageProvider.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnRemoveNewBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						removePart(true);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnAddOldBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						addBomline(false);
					} catch (Exception ex) {
						dlg.setMessage(ex.toString(), IMessageProvider.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnRemoveOldBomline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						removePart(false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnRemoveLine.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeBomline(false);
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

			dlg.btnDisposalMaterial.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						massUpdate("Disposal Material", disposalMaterialDataForm);
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
//						if (validate()) {
						createBOMInformation();
						removeBomline(true);
						selectedObject.refresh();
						dlg.setMessage("Update success.", IMessageProvider.INFORMATION);
//						} else {
//							dlg.setMessage("BOM Info not valid.", IMessageProvider.ERROR);
//							dlg.tblBom.deselectAll();
//						}
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
					if (bomTemp.getKey().compareTo(EEComponentECRNameDefine.BOMINFO_PART_NUMBER) == 0) {
						partNumber = value;
					} else if (bomTemp.getKey().compareTo(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE) == 0) {
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
				if (property.compareTo(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY) == 0)
					return false;
			}
		}
		if (changeType.compareToIgnoreCase("REMOVE") != 0)
			if (property.compareTo("vf6_posid") == 0)
				return false;
		if (property.compareTo("vf6_donor_vehicle") == 0)
			return false;
		if (property.compareTo(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY) == 0)
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

		EEComponentECRBomInfoEdit_Dialog addDlg = new EEComponentECRBomInfoEdit_Dialog(dlg.getShell());
		addDlg.create();
		addDlg.initData(session, bomInfo);
		addDlg.open();
		Button okButton = addDlg.getOKButton();
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedHashMap<String, String> bomline = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER, addDlg.txtTopPart.getText());
						put(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE, addDlg.cbChangeType.getText());
						put(EEComponentECRNameDefine.BOMINFO_CHANGE_POINT, addDlg.txtChangePoint.getText());
						put(EEComponentECRNameDefine.BOMINFO_DISPOSAL_MATERIAL, addDlg.cbDisposalMaterial.getText());

						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER, addDlg.txtOldPartNumber.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME, addDlg.txtOldPartName.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION, addDlg.txtOldPartRevision.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY, addDlg.txtOldPartQuantity.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN, addDlg.txtOldPartMPN.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE, addDlg.txtOldPartSubstitute.getText());
						put(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR, addDlg.txtOldPartDesignator.getText());

						put(EEComponentECRNameDefine.BOMINFO_PART_NUMBER, addDlg.txtPartNumber.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_NAME, addDlg.txtPartName.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_REVISION, addDlg.txtPartRevision.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY, addDlg.txtQuantity.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_MPN, addDlg.txtMPN.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE, addDlg.txtSubstitute.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR, addDlg.txtDesignator.getText());
						put(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY, addDlg.cbPartTraceability.getText());
					}
				};
				addDlg.close();
				editTableRow(bomline, editIndex);
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
							tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_CHANGE_TYPE), massUpdateDlg.valueSelected);
						else if (title.compareToIgnoreCase("Disposal Material") == 0)
							tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_DISPOSAL_MATERIAL), massUpdateDlg.valueSelected);
					}
				}
			}
		});
	}

	private void createBOMInformation() throws Exception {
		DataManagementService dms = DataManagementService.getService(session);
		TCComponent[] forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
		TCComponent ecrForm = null;
		if (forms.length > 0) {
			ecrForm = forms[0];
		} else {
			ecrForm = createEcrFormAndAttachToECR(selectedObject);
		}

		List<Map<String, String>> bomAttributesAndValsList = new LinkedList<Map<String, String>>();
		for (TableItem tableItem : dlg.tblBom.getItems()) {
			int i = 0;
			Map<String, String> mapItem = new HashMap<String, String>();
			for (Map.Entry<String, String> bomTemp : bomInfoTemplate.entrySet()) {
				if (!bomTemp.getKey().isEmpty())
					mapItem.put(bomTemp.getKey(), tableItem.getText(i));
				i++;
			}
			bomAttributesAndValsList.add(mapItem);
		}

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
							String quantityStr = bomAttributesAndVals.get(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY);
							double quantityDouble = Double.parseDouble(quantityStr);
							int quantity = (int) Math.floor(quantityDouble);
							quantityStr = Integer.toString(quantity);
							bomAttributesAndVals.put(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY, quantityStr);
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

	private void addBomline(boolean isNewPart) throws Exception {
		int addIndex = -1;
		for (int i = 0; i < dlg.tblBom.getItemCount(); i++) {
			TableItem rowItem = dlg.tblBom.getItem(i);
			if (rowItem.getChecked())
				addIndex = i;
		}

		InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComps.length > 0) {
			if (addIndex >= 0) {
				if (targetComps[0] instanceof TCComponentBOMLine) {
					LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
					TCComponentBOMLine bomline = (TCComponentBOMLine) targetComps[0];
					TableItem tableItem = dlg.tblBom.getItem(addIndex);
					for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
						output.put(bomInfoTemp.getKey(), tableItem.getText(getPosBomInfoTemplate(bomInfoTemp.getKey())));
					}

					for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
						String key = bomInfoTemp.getKey();
						if (!key.isEmpty()) {
							if (isNewPart) {
								if (key.compareTo(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER) == 0)
									output.put(key, getTopPart(bomline));
								else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_MPN) == 0)
									output.put(key, getMPN(bomline.getItemRevision()));
								else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_NUMBER) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_NAME) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_REVISION) == 0
										|| key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR) == 0
										|| key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY) == 0)
									output.put(key, bomline.getPropertyDisplayableValue(bomInfoTemp.getValue()));
							} else {
								if (key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN) == 0)
									output.put(key, getMPN(bomline.getItemRevision()));
								else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION) == 0
										|| key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR) == 0)
									output.put(key, bomline.getPropertyDisplayableValue(bomInfoTemp.getValue()));
							}
						}
					}
					editTableRow(output, addIndex);
				}
			} else {
				for (InterfaceAIFComponent targetComp : targetComps) {
					if (targetComp instanceof TCComponentBOMLine) {
						LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
						TCComponentBOMLine bomline = (TCComponentBOMLine) targetComp;
						for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
							output.put(bomInfoTemp.getKey(), "");
						}

						for (Map.Entry<String, String> bomInfoTemp : bomInfoTemplate.entrySet()) {
							String key = bomInfoTemp.getKey();
							if (!key.isEmpty()) {
								if (isNewPart) {
									if (key.compareTo(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER) == 0)
										output.put(key, getTopPart(bomline));
									else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_MPN) == 0)
										output.put(key, getMPN(bomline.getItemRevision()));
									else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_NUMBER) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_NAME) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_REVISION) == 0
											|| key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR) == 0
											|| key.compareTo(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY) == 0)
										output.put(key, bomline.getPropertyDisplayableValue(bomInfoTemp.getValue()));
								} else {
									if (key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_MPN) == 0)
										output.put(key, getMPN(bomline.getItemRevision()));
									else if (key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION) == 0
											|| key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE) == 0 || key.compareTo(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR) == 0)
										output.put(key, bomline.getPropertyDisplayableValue(bomInfoTemp.getValue()));
								}
							}
						}
						addTableRow(output, dlg.tblBom.getItemCount());
					}
				}
			}
		}
	}

	private void removePart(boolean isNewPart) {
		for (TableItem tableItem : dlg.tblBom.getSelection()) {
			if (tableItem.getChecked()) {
				if (isNewPart) {
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_TOP_PART_NUMBER), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_NUMBER), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_NAME), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_REVISION), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_QUANTITY), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_MPN), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_SUBSTITUTE), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_DESIGNATOR), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_PART_TRACEBILITY), "");
				} else {
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_NUMBER), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_NAME), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_REVISION), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_QUANTITY), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_SUBSTITUTE), "");
					tableItem.setText(getPosBomInfoTemplate(EEComponentECRNameDefine.BOMINFO_OLD_PART_DESIGNATOR), "");
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

	private TCComponent createEcrFormAndAttachToECR(TCComponent ecrRev) throws Exception {
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

	private String getTopPart(TCComponentBOMLine bomLine) {
		try {
			String level = bomLine.getPropertyDisplayableValue("bl_level_starting_0");
			if (level.compareTo("0") == 0)
				return bomLine.getPropertyDisplayableValue("awb0BomLineItemId") + "/" + bomLine.getPropertyDisplayableValue("bl_rev_item_revision_id");

			return getTopPart(bomLine.parent());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private String getMPN(TCComponentItemRevision itemRev) {
		try {
			TCComponent[] objectChildComponents = itemRev.getRelatedComponents("VF4_EC_Supplier_Relation");
			String subItem = itemRev.getPropertyDisplayableValue("item_id") + " (";
			for (TCComponent tcComp : objectChildComponents) {
				if (tcComp.getType().compareToIgnoreCase("VF4_EC_Supp_PartRevision") == 0) {
					subItem += tcComp.getPropertyDisplayableValue("object_name") + " / " + tcComp.getPropertyDisplayableValue("vf4_manufacturer");
				}
			}
			subItem += ")";
			return subItem;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
