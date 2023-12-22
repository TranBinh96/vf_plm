package com.teamcenter.vinfast.change;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchIMSNumber_Dialog;
import com.teamcenter.vinfast.subdialog.SearchItem_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ECRCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ECRCreate_Dialog dlg;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private static String OBJECT_TYPE = "Vf6_ECR";
	private TCComponent selectedObject = null;
	private LinkedHashMap<String, String> ecrMappingID = null;
	private LinkedList<TCComponent> ecrRevList = null;
	private String vehicleType = "";

	public ECRCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			String abcString = event.getCommand().toString();
			boolean isVES = event.getCommand().toString().contains("batcell");

			ecrMappingID = new LinkedHashMap<String, String>();

			if (event.getCommand().toString().contains("vines")) {
				vehicleType = "VES";
			} else if (event.getCommand().toString().contains("eecomponent")) {
				vehicleType = "EE";
			} else {
				vehicleType = "CAR";
			}
			// Load data form

			LinkedHashMap<String, String> vehicleGroupValueAndDesc = TCExtension.getLovValueAndDescription("vf6_vehicle_group", "Vf6_ECR", session);
			LinkedHashMap<String, String> vehicleGroupDataForm = TCExtension.GetLovValueAndDisplay("vf6_vehicle_group", "Vf6_ECR", session);
			LinkedHashMap<String, String> moduleGroupValueAndDesc = TCExtension.getLovValueAndDescription("vf6_module_group", "Vf6_ECR", session);
			LinkedList<String> moduleGroupDataForm = new LinkedList<String>();

			if (isVES) {
				for (Map.Entry<String, String> entry : vehicleGroupValueAndDesc.entrySet()) {
					if (entry.getValue().compareToIgnoreCase("BP") != 0 && entry.getValue().compareToIgnoreCase("CELL") != 0) {
						removeValueFromLinkedList(vehicleGroupDataForm, entry.getKey());
					}
				}

				for (Map.Entry<String, String> entry : moduleGroupValueAndDesc.entrySet()) {
					if (entry.getValue().compareToIgnoreCase("BP") == 0 || entry.getValue().compareToIgnoreCase("CELL") == 0) {
						moduleGroupDataForm.add(entry.getKey());
					}
				}
			} else {
				for (Map.Entry<String, String> entry : vehicleGroupValueAndDesc.entrySet()) {
					String key = entry.getKey();
					if (entry.getValue().compareToIgnoreCase("CAR") != 0) {
						removeValueFromLinkedList(vehicleGroupDataForm, key);
					} else {
						if (key.compareTo("VF35") == 0 || key.compareTo("C-SUV") == 0 || key.compareTo("VF36") == 0 || key.compareTo("D-SUV") == 0)
							removeValueFromLinkedList(vehicleGroupDataForm, key);
					}
				}

				for (Map.Entry<String, String> entry : moduleGroupValueAndDesc.entrySet()) {
					if (entry.getValue().compareToIgnoreCase("CAR") == 0) {
						moduleGroupDataForm.add(entry.getKey());
					}
				}
			}

			removeValueFromLinkedList(vehicleGroupDataForm, "VF35");
			removeValueFromLinkedList(vehicleGroupDataForm, "C-SUV");
			removeValueFromLinkedList(vehicleGroupDataForm, "VF36");
			removeValueFromLinkedList(vehicleGroupDataForm, "D-SUV");
			removeValueFromLinkedList(vehicleGroupDataForm, "Car");

			String[] changeReasonDataForm = TCExtension.GetLovValues("vf6_change_reason", "Vf6_ECR", session);
			String[] implementationDateDataForm = TCExtension.GetLovValues("vf6_implementation_date_arr", "Vf6_ECR", session);

			String[] impactedModuleGroupDataForm = TCExtension.GetLovValues("vf6_impacted_module_arr", "Vf6_ECR", session);

			LinkedHashMap<String, String> newPartStatusDataForm = TCExtension.GetLovValueAndDisplay("vf6_new_parts_status", "Vf6_ECRRevision", session);
			newPartStatusDataForm.put("", "");
			String[] exchangeNewPartDataForm = TCExtension.GetLovValues("vf6_es_exchange_newpart", "Vf6_ECRRevision", session);
			String[] exchangeOldPartDataForm = TCExtension.GetLovValues("vf6_es_exchange_oldpart", "Vf6_ECRRevision", session);
			String[] ecrMappingPreference = TCExtension.GetPreferenceValues("VF_ECR_GENERAL_ID", session);
			if (ecrMappingPreference != null && ecrMappingPreference.length > 0) {
				for (String value : ecrMappingPreference) {
					if (value.contains("=")) {
						String[] str = value.split("=");
						ecrMappingID.put(str[0], str[1]);
					}

				}
			}
			LinkedHashMap<String, String> marketDataForm = TCExtension.GetLovValueAndDisplay("vf6cp_market", "Vf6_ECRRevision", session);
			String[] lhdrhdDataForm = TCExtension.GetLovValues("vf6cp_lhd_rhd", "Vf6_ECRRevision", session);
			String[] variantDataForm = TCExtension.GetLovValues("vf6cp_variant", "Vf6_ECRRevision", session);
			String[] seatDataForm = TCExtension.GetLovValues("vf6cp_seat_configuration", "Vf6_ECRRevision", session);
			String[] premiumVariantDataForm = TCExtension.GetLovValues("vf6cp_base_or_premium", "Vf6_ECRRevision", session);
			String[] decisionApprovalDataForm = TCExtension.GetLovValues("vf6cp_pre_decision_approval", "Vf6_ECRRevision", session);
			LinkedHashMap<String, String> ecrCategoryDataForm = TCExtension.GetLovValueAndDescription("vf6_ecr_category", "Vf6_ECRRevision", session);
			String[] requiredDataForm = new String[] { "Yes", "No" };
			String[] buildPhaseTimeDataForm = TCExtension.GetLovValues("vf6_build_time_impact", "Vf6_ECRRevision", session);

			// Init UI
			dlg = new ECRCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbCOPImpact, vehicleGroupDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbECRCategory, ecrCategoryDataForm);
			dlg.cbChangeReason.setItems(changeReasonDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbNewPartStatus, newPartStatusDataForm);
			dlg.cbExchangeNewPart.setItems(exchangeNewPartDataForm);
			dlg.cbExchangeOldPart.setItems(exchangeOldPartDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbMarket, marketDataForm);
			dlg.cbDriving.setItems(lhdrhdDataForm);
			dlg.cbVariant.setItems(variantDataForm);
			dlg.cbSeat.setItems(seatDataForm);
			dlg.cbPremiumVariant.setItems(premiumVariantDataForm);
			dlg.cbDecisionApproval.setItems(decisionApprovalDataForm);

			dlg.cbModuleGroup.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String moduleGroup = dlg.cbModuleGroup.getText();
					if (!moduleGroup.isEmpty()) {
						dlg.lstImpactedModule.removeAll();
						Set<String> impactedModuleGroupClone = new HashSet<>();
						for (String item : impactedModuleGroupDataForm) {
							if (item.compareTo(moduleGroup) == 0)
								continue;

							impactedModuleGroupClone.add(item);
						}
						dlg.cbImpactedModule.setItems(impactedModuleGroupClone.toArray(new String[0]));
					}
				}
			});
			// ---------------------------------------------------
			dlg.cbHaveDMUCheckRequired.setItems(requiredDataForm);
			dlg.cbHaveDMUCheckRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbHaveDMUCheckRequired");
				}
			});

			dlg.cbSILNoRequired.setItems(requiredDataForm);
			dlg.cbSILNoRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbSILNo");
				}
			});

			dlg.cbDCRNoRequired.setItems(requiredDataForm);
			dlg.cbDCRNoRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbDCRNo");
				}
			});

			dlg.cbCoordinatedChageRequired.setItems(requiredDataForm);
			dlg.cbCoordinatedChageRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbCoordinatedChage");
				}
			});

			dlg.cbVehicleCostRequired.setItems(requiredDataForm);
			dlg.cbVehicleCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbVehicleCost");
				}
			});

			dlg.cbPieceCostRequired.setItems(requiredDataForm);
			dlg.cbPieceCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbPieceCost");
				}
			});

			dlg.cbToolingCostRequired.setItems(requiredDataForm);
			dlg.cbToolingCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbToolingCost");
				}
			});

			dlg.cbEDDCostRequired.setItems(requiredDataForm);
			dlg.cbEDDCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbEDDCost");
				}
			});

			dlg.cbSunkCostRequired.setItems(requiredDataForm);
			dlg.cbSunkCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbSunkCost");
				}
			});

			dlg.cbPlantEquipmentCostRequired.setItems(requiredDataForm);
			dlg.cbPlantEquipmentCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbPlantEquipmentCost");
				}
			});

			dlg.cbScrapCostRequired.setItems(requiredDataForm);
			dlg.cbScrapCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbScrapCost");
				}
			});

			dlg.cbTestingValidationCostRequired.setItems(requiredDataForm);
			dlg.cbTestingValidationCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbTestingValidationCost");
				}
			});

			dlg.cbOtherCostRequired.setItems(requiredDataForm);
			dlg.cbOtherCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbOtherCost");
				}
			});

			dlg.cbWeightRequired.setItems(requiredDataForm);
			dlg.cbWeightRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbWeight");
				}
			});

			dlg.cbPFMEARequired.setItems(requiredDataForm);
			dlg.cbPFMEARequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbSOR");
				}
			});

			dlg.cbSpecBookRequired.setItems(requiredDataForm);
			dlg.cbSpecBookRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbSpecBook");
				}
			});

			dlg.cbDFMEARequired.setItems(requiredDataForm);
			dlg.cbDFMEARequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbDFMEA");
				}
			});

			dlg.cbDVPRequired.setItems(requiredDataForm);
			dlg.cbDVPRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbDVP");
				}
			});

			dlg.cbLeadtimeRequired.setItems(requiredDataForm);
			dlg.cbLeadtimeRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbLeadtime");
				}
			});

			dlg.cbBuildPhaseTimeRequired.setItems(requiredDataForm);
			dlg.cbBuildPhaseTimeRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbBuildPhaseTimeRequired");
				}
			});

			dlg.cbImpactedModuleRequired.setItems(requiredDataForm);
			dlg.cbImpactedModuleRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbImpactedModuleRequired");
				}
			});

			dlg.cbCOPImpactRequired.setItems(requiredDataForm);
			dlg.cbCOPImpactRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbCOPImpact");
				}
			});

			dlg.cbIsSendDataToSupplier.setItems(requiredDataForm);
			dlg.cbIsSendDataToSupplier.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbIsSendDataToSupplier");
				}
			});

			dlg.cbBuildPhaseTime.setItems(buildPhaseTimeDataForm);

			// ---------------------------------------------------
			dlg.cbECRCategory.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.cbECRCategory.getText().isEmpty()) {
						dlg.cbECRCategory2.removeAll();
						dlg.lstECRCategory2.removeAll();

						String ecrCategory = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
						try {
							LinkedHashMap<String, String> dataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf6_ecr_category", "Vf6_ECRRevision", new String[] { ecrCategory }, session);
							StringExtension.UpdateValueTextCombobox(dlg.cbECRCategory2, dataForm);
						} catch (Exception e) {
							e.printStackTrace();
						}

						updateReviewGate();
					}
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleGroup, vehicleGroupDataForm);

			dlg.cbModuleGroup.setItems(moduleGroupDataForm.toArray(new String[0]));
			dlg.cbImpactedModule.setItems(impactedModuleGroupDataForm);
			dlg.cbImplementationDate.setItems(implementationDateDataForm);

			dlg.btnDMUUpload.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(dlg.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					if (dlg.cbHaveDMUCheckRequired.getText().contains("No")) {
						fd.setFilterExtensions(new String[] { "*.msg;*.pdf" });
					} else {
						fd.setFilterExtensions(new String[] { "Excel Files|*.xls;*.xlsx" });
					}
					String selected = fd.open();
					if (selected != null)
						dlg.txtDMUCheckFile.setText(selected);
				}
			});

			dlg.btnECRCategory2Add.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("ECR_CATEGORY_2");
				}
			});

			dlg.btnECRCategory2Remove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("ECR_CATEGORY_2");
				}
			});

			dlg.btnMarketAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("MARKET");
				}
			});

			dlg.btnMarketRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("MARKET");
				}
			});

			dlg.btnVariantAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("VARIANT");
				}
			});

			dlg.btnVariantRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("VARIANT");
				}
			});

			dlg.btnCOPImpactAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("COP_IMPACT");
				}
			});

			dlg.btnCOPImpactRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("COP_IMPACT");
				}
			});

			dlg.btnImpactedModuleAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("IMPACTED_MODULE");
				}
			});

			dlg.btnImpactedModuleRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("IMPACTED_MODULE");
				}
			});

			dlg.btnImplementationDateAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("IMPLEMENTATION_DATE");
				}
			});

			dlg.btnImplementationDateRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("IMPLEMENTATION_DATE");
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					showProgressDialog(true);
					try {
						createNewItem();
					} catch (Exception e) {
						e.printStackTrace();
					}
					showProgressDialog(false);
				}
			});

			dlg.btnIMSAdd.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchIMSNumber_Dialog searchDlg = new SearchIMSNumber_Dialog(dlg.getShell());
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							for (TableItem tableItem : searchDlg.tblSearch.getSelection()) {
								TableItem newRow = new TableItem(dlg.tblSIL, SWT.NONE);
								newRow.setText(new String[] { tableItem.getText(0), tableItem.getText(1) });
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnIMSRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int indexSIL = dlg.tblSIL.getSelectionIndex();
					if (indexSIL >= 0) {
						dlg.tblSIL.remove(indexSIL);
					}
				}
			});

			dlg.btnECRAdd.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchItem_Dialog searchDlg = new SearchItem_Dialog(dlg.getShell(), "Engineering Change Request");
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							TCComponent ecrItem = searchDlg.itemSearch.get(index);
							try {
								if (!ecrRevList.contains(ecrItem)) {
									ecrRevList.add(ecrItem);
									TableItem newRow = new TableItem(dlg.tblCoordinatedChange, SWT.NONE);
									newRow.setText(new String[] { ecrItem.getPropertyDisplayableValue("item_id"), ecrItem.getPropertyDisplayableValue("object_name"), ecrItem.getPropertyDisplayableValue("vf6_implementation_date_arr") });
								}
							} catch (Exception e2) {
								e2.printStackTrace();
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnECRRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int indexECRRev = dlg.tblCoordinatedChange.getSelectionIndex();
					if (indexECRRev >= 0) {
						dlg.tblCoordinatedChange.remove(indexECRRev);
						ecrRevList.remove(indexECRRev);
					}
				}
			});

			dlg.getShell().setMaximized(true);

			initUIDefault();

//			testCase();

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void initUIDefault() {
		// SUMMARY
		dlg.cbVehicleGroup.deselectAll();
		dlg.txtName.setText("");
		dlg.txtECRContactPerson.setText("");
		dlg.txtECRContactEmail.setText("");
		dlg.txtDescription.setText("");
		dlg.cbModuleGroup.deselectAll();
		dlg.cbECRCategory.deselectAll();
		dlg.cbECRCategory2.removeAll();
		dlg.lstECRCategory2.removeAll();
		dlg.lstImplementationDate.removeAll();
		dlg.cbImplementationDate.deselectAll();
		dlg.cbChangeReason.deselectAll();
		dlg.cbNewPartStatus.deselectAll();
		dlg.ckbTargetReleaseDate.setSelection(false);
		dlg.cbDCRNoRequired.deselectAll();
		dlg.txtDCRNo.setText("");
		dlg.cbExchangeNewPart.deselectAll();
		dlg.cbExchangeOldPart.deselectAll();
		dlg.cbSILNoRequired.deselectAll();
		dlg.tblSIL.removeAll();
		dlg.cbCoordinatedChageRequired.deselectAll();
		dlg.tblCoordinatedChange.removeAll();
		ecrRevList = new LinkedList<>();
		dlg.cbImpactedModuleRequired.deselectAll();
		dlg.cbIsSendDataToSupplier.deselectAll();
		dlg.txtSendDataReason.setText("");
		dlg.cbHaveDMUCheckRequired.deselectAll();
		dlg.txtDMUCheckFile.setText("");

		// CHANGE DESC
		dlg.txtCurrentMaterial.setText("");
		dlg.txtNewMaterial.setText("");
		dlg.cbWeightRequired.deselectAll();
		dlg.txtCurrentWeight.setText("");
		dlg.txtNewWeight.setText("");

		// DOCUMENTATION
		dlg.cbPFMEARequired.deselectAll();
		dlg.txtPFMEA.setText("");
		dlg.cbSpecBookRequired.deselectAll();
		dlg.txtSpecBook.setText("");
		dlg.cbDFMEARequired.deselectAll();
		dlg.txtDFMEA.setText("");
		dlg.cbDVPRequired.deselectAll();
		dlg.txtDVP.setText("");

		// REVIEW GATE
		dlg.ckbVehicleArchitecture.setEnabled(false);
		dlg.ckbHomologation.setEnabled(false);
		dlg.ckbCAE.setEnabled(false);
		dlg.ckbAfterSale.setEnabled(false);
		dlg.ckbStandardPart.setEnabled(false);
		dlg.ckbFunctionSafety.setEnabled(false);
		dlg.ckbColorTrim.setEnabled(false);
		dlg.ckbSupplierQualityManager.setEnabled(false);
		dlg.ckbSaleProductMarketing.setEnabled(false);

		// INIT COST
		dlg.cbVehicleCostRequired.deselectAll();
		dlg.txtVehicleCost.setText("");
		dlg.cbPieceCostRequired.deselectAll();
		dlg.txtPieceCost.setText("");
		dlg.cbToolingCostRequired.deselectAll();
		dlg.txtToolingCost.setText("");
		dlg.cbEDDCostRequired.deselectAll();
		dlg.txtEDDCost.setText("");
		dlg.cbSunkCostRequired.deselectAll();
		dlg.txtSunkCost.setText("");
		dlg.cbPlantEquipmentCostRequired.deselectAll();
		dlg.txtPlantEquipmentCost.setText("");
		dlg.cbScrapCostRequired.deselectAll();
		dlg.txtScrapCost.setText("");
		dlg.cbTestingValidationCostRequired.deselectAll();
		dlg.txtTestingValidationCost.setText("");
		dlg.cbOtherCostRequired.deselectAll();
		dlg.txtOtherCost.setText("");

		// PRECAB
		dlg.lstMarket.removeAll();
		dlg.cbMarket.deselectAll();
		dlg.cbDriving.deselectAll();
		dlg.lstVariant.removeAll();
		dlg.cbVariant.deselectAll();
		dlg.cbPremiumVariant.deselectAll();
		dlg.cbSeat.deselectAll();
		dlg.cbDecisionApproval.deselectAll();
		dlg.txtDecisionComment.setText("");
		dlg.ckbApprovalDate.setSelection(false);

		// IMPACT
		dlg.txtSupplier.setText("");
		dlg.lstCOPImpact.removeAll();
		dlg.cbCOPImpactRequired.deselectAll();
		dlg.cbLeadtimeRequired.deselectAll();
		dlg.txtLeadtime.setText("");
		dlg.cbBuildPhaseTimeRequired.deselectAll();
		dlg.txtMRPLateTime.setText("");
		dlg.txtProblem.setText("");
		dlg.txtRootCause.setText("");
		dlg.txtSolution.setText("");
	}

	private void testCase() {
		if (!TCExtension.checkPermission(new String[] { "dba" }, session))
			return;

		dlg.cbVehicleGroup.setText("VFe36");
		dlg.txtName.setText("txtName");
		dlg.txtECRContactPerson.setText("txtECRContactPerson");
		dlg.txtECRContactEmail.setText("txtECRContactEmail");
		dlg.txtDescription.setText("txtDescription");

		dlg.cbModuleGroup.setText("Chassis");
		dlg.lstImpactedModule.setItems("Chassis");
		dlg.cbECRCategory.setText("Others - Not belong to all category above");
		dlg.lstECRCategory2.setItems("Others");
		dlg.lstImplementationDate.setItems("EP");
		dlg.cbChangeReason.setText("Cost Saving");
		dlg.cbNewPartStatus.setText("I");
		dlg.cbExchangeNewPart.setText("New parts must not be used on old models");
		dlg.cbExchangeOldPart.setText("Old parts must not be used on new models");
		dlg.cbSILNoRequired.setText("Yes");
		dlg.cbDCRNoRequired.setText("Yes");
		dlg.txtDCRNo.setText("txtDCRNo");
		dlg.cbImpactedModuleRequired.setText("Yes");
		dlg.cbCoordinatedChageRequired.setText("No");

		dlg.txtCurrentMaterial.setText("txtCurrentMaterial");
		dlg.txtNewMaterial.setText("txtNewMaterial");
		dlg.cbWeightRequired.setText("Yes");
		dlg.txtCurrentWeight.setText("1");
		dlg.txtNewWeight.setText("2");

		dlg.cbPFMEARequired.setText("Yes");
		dlg.txtPFMEA.setText("txtSOR");
		dlg.cbSpecBookRequired.setText("Yes");
		dlg.txtSpecBook.setText("txtSpecBook");
		dlg.cbDFMEARequired.setText("Yes");
		dlg.txtDFMEA.setText("txtDFMEA");
		dlg.cbDVPRequired.setText("Yes");
		dlg.txtDVP.setText("txtDVP");

		dlg.cbVehicleCostRequired.setText("Yes");
		dlg.txtVehicleCost.setText("1");
		dlg.cbPieceCostRequired.setText("Yes");
		dlg.txtPieceCost.setText("2");
		dlg.cbToolingCostRequired.setText("Yes");
		dlg.txtToolingCost.setText("3");
		dlg.cbEDDCostRequired.setText("Yes");
		dlg.txtEDDCost.setText("4");
		dlg.cbSunkCostRequired.setText("Yes");
		dlg.txtSunkCost.setText("5");
		dlg.cbPlantEquipmentCostRequired.setText("Yes");
		dlg.txtPlantEquipmentCost.setText("6");
		dlg.cbScrapCostRequired.setText("Yes");
		dlg.txtScrapCost.setText("7");
		dlg.cbTestingValidationCostRequired.setText("Yes");
		dlg.txtTestingValidationCost.setText("8");
		dlg.cbOtherCostRequired.setText("Yes");
		dlg.txtOtherCost.setText("9");

		dlg.lstMarket.setItems("Vietnam");
		dlg.cbDriving.setText("ALL");
		dlg.lstVariant.setItems("ECO");
		dlg.cbPremiumVariant.setText("Yes");
		dlg.cbSeat.setText("5");
		dlg.cbDecisionApproval.setText("APPROVED");
		dlg.txtDecisionComment.setText("txtDecisionComment");

		dlg.txtSupplier.setText("txtSupplier");
		dlg.cbCOPImpactRequired.setText("Yes");
		dlg.lstCOPImpact.setItems("VFe36");
		dlg.cbLeadtimeRequired.setText("Yes");
		dlg.txtLeadtime.setText("1");
		dlg.cbBuildPhaseTimeRequired.setText("Yes");
		dlg.cbBuildPhaseTime.setText("LS");
		dlg.txtMRPLateTime.setText("1");
		dlg.txtProblem.setText("txtProblem");
		dlg.txtRootCause.setText("txtRootCause");
		dlg.txtSolution.setText("txtSolution");
	}

	private void updateReviewGate() {
		String ecrCategorty = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
		if (ecrCategorty.isEmpty())
			return;

		if (ecrCategorty.compareTo("Aftersales") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(true);
			dlg.ckbAfterSale.setSelection(true);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Commercial & Procurement relevant") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(false);
			dlg.ckbHomologation.setSelection(false);
			dlg.ckbCAE.setSelection(false);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Data BOM") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(false);
			dlg.ckbCAE.setSelection(false);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(true);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(true);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Design change") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(true);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(true);
			dlg.ckbFunctionSafety.setSelection(true);
			dlg.ckbColorTrim.setSelection(true);
			dlg.ckbSupplierQualityManager.setSelection(true);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Document") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(false);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(true);
			dlg.ckbAfterSale.setSelection(true);
			dlg.ckbStandardPart.setSelection(true);
			dlg.ckbFunctionSafety.setSelection(true);
			dlg.ckbColorTrim.setSelection(true);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Gate release") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(true);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(true);
			dlg.ckbFunctionSafety.setSelection(true);
			dlg.ckbColorTrim.setSelection(true);
			dlg.ckbSupplierQualityManager.setSelection(true);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Others") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(false);
			dlg.ckbCAE.setSelection(false);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Software") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(false);
			dlg.ckbAfterSale.setSelection(true);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(true);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		} else if (ecrCategorty.compareTo("Product strategy") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(false);
			dlg.ckbHomologation.setSelection(true);
			dlg.ckbCAE.setSelection(true);
			dlg.ckbAfterSale.setSelection(true);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(true);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(true);
		} else if (ecrCategorty.compareTo("M/C Release") == 0) {
			dlg.ckbVehicleArchitecture.setSelection(true);
			dlg.ckbHomologation.setSelection(false);
			dlg.ckbCAE.setSelection(false);
			dlg.ckbAfterSale.setSelection(false);
			dlg.ckbStandardPart.setSelection(false);
			dlg.ckbFunctionSafety.setSelection(false);
			dlg.ckbColorTrim.setSelection(false);
			dlg.ckbSupplierQualityManager.setSelection(false);
			dlg.ckbSaleProductMarketing.setSelection(false);
		}
	}

	private void updateRequired(String type) {
		if (type.compareToIgnoreCase("cbSILNo") == 0) {
			dlg.btnIMSAdd.setEnabled(dlg.cbSILNoRequired.getText().contains("Yes"));
			dlg.btnIMSRemove.setEnabled(dlg.cbSILNoRequired.getText().contains("Yes"));
			dlg.tblSIL.removeAll();
		} else if (type.compareToIgnoreCase("cbDCRNo") == 0) {
			dlg.txtDCRNo.setEnabled(dlg.cbDCRNoRequired.getText().contains("Yes"));
			dlg.txtDCRNo.setText("");
		} else if (type.compareToIgnoreCase("cbCoordinatedChage") == 0) {
			dlg.btnECRAdd.setEnabled(dlg.cbCoordinatedChageRequired.getText().contains("Yes"));
			dlg.btnECRRemove.setEnabled(dlg.cbCoordinatedChageRequired.getText().contains("Yes"));
			ecrRevList = new LinkedList<>();
			dlg.tblCoordinatedChange.removeAll();
		} else if (type.compareToIgnoreCase("cbVehicleCost") == 0) {
			dlg.txtVehicleCost.setEnabled(dlg.cbVehicleCostRequired.getText().contains("Yes"));
			dlg.txtVehicleCost.setText("");
		} else if (type.compareToIgnoreCase("cbToolingCost") == 0) {
			dlg.txtToolingCost.setEnabled(dlg.cbToolingCostRequired.getText().contains("Yes"));
			dlg.txtToolingCost.setText("");
		} else if (type.compareToIgnoreCase("cbSunkCost") == 0) {
			dlg.txtSunkCost.setEnabled(dlg.cbSunkCostRequired.getText().contains("Yes"));
			dlg.txtSunkCost.setText("");
		} else if (type.compareToIgnoreCase("cbScrapCost") == 0) {
			dlg.txtScrapCost.setEnabled(dlg.cbScrapCostRequired.getText().contains("Yes"));
			dlg.txtScrapCost.setText("");
		} else if (type.compareToIgnoreCase("cbOtherCost") == 0) {
			dlg.txtOtherCost.setEnabled(dlg.cbOtherCostRequired.getText().contains("Yes"));
			dlg.txtOtherCost.setText("");
		} else if (type.compareToIgnoreCase("cbPieceCost") == 0) {
			dlg.txtPieceCost.setEnabled(dlg.cbPieceCostRequired.getText().contains("Yes"));
			dlg.txtPieceCost.setText("");
		} else if (type.compareToIgnoreCase("cbEDDCost") == 0) {
			dlg.txtEDDCost.setEnabled(dlg.cbEDDCostRequired.getText().contains("Yes"));
			dlg.txtEDDCost.setText("");
		} else if (type.compareToIgnoreCase("cbPlantEquipmentCost") == 0) {
			dlg.txtPlantEquipmentCost.setEnabled(dlg.cbPlantEquipmentCostRequired.getText().contains("Yes"));
			dlg.txtPlantEquipmentCost.setText("");
		} else if (type.compareToIgnoreCase("cbTestingValidationCost") == 0) {
			dlg.txtTestingValidationCost.setEnabled(dlg.cbTestingValidationCostRequired.getText().contains("Yes"));
			dlg.txtTestingValidationCost.setText("");
		} else if (type.compareToIgnoreCase("cbWeight") == 0) {
			dlg.txtCurrentWeight.setEnabled(dlg.cbWeightRequired.getText().contains("Yes"));
			dlg.txtNewWeight.setEnabled(dlg.cbWeightRequired.getText().contains("Yes"));
			dlg.txtCurrentWeight.setText("");
			dlg.txtNewWeight.setText("");
		} else if (type.compareToIgnoreCase("cbSOR") == 0) {
			dlg.txtPFMEA.setEnabled(dlg.cbPFMEARequired.getText().contains("Yes"));
			dlg.txtPFMEA.setText("");
		} else if (type.compareToIgnoreCase("cbSpecBook") == 0) {
			dlg.txtSpecBook.setEnabled(dlg.cbSpecBookRequired.getText().contains("Yes"));
			dlg.txtSpecBook.setText("");
		} else if (type.compareToIgnoreCase("cbDFMEA") == 0) {
			dlg.txtDFMEA.setEnabled(dlg.cbDFMEARequired.getText().contains("Yes"));
			dlg.txtDFMEA.setText("");
		} else if (type.compareToIgnoreCase("cbDVP") == 0) {
			dlg.txtDVP.setEnabled(dlg.cbDVPRequired.getText().contains("Yes"));
			dlg.txtDVP.setText("");
		} else if (type.compareToIgnoreCase("cbLeadtime") == 0) {
			dlg.txtLeadtime.setEnabled(dlg.cbLeadtimeRequired.getText().contains("Yes"));
			dlg.txtLeadtime.setText("");
		} else if (type.compareToIgnoreCase("cbBuildPhaseTimeRequired") == 0) {
			dlg.cbBuildPhaseTime.setEnabled(dlg.cbBuildPhaseTimeRequired.getText().contains("Yes"));
			dlg.txtMRPLateTime.setEnabled(dlg.cbBuildPhaseTimeRequired.getText().contains("Yes"));
			dlg.txtMRPLateTime.setText("");
		} else if (type.compareToIgnoreCase("cbCOPImpact") == 0) {
			dlg.btnCOPImpactAdd.setEnabled(dlg.cbCOPImpactRequired.getText().contains("Yes"));
			dlg.btnCOPImpactRemove.setEnabled(dlg.cbCOPImpactRequired.getText().contains("Yes"));
			dlg.lstCOPImpact.removeAll();
		} else if (type.compareToIgnoreCase("cbImpactedModuleRequired") == 0) {
			dlg.btnImpactedModuleAdd.setEnabled(dlg.cbImpactedModuleRequired.getText().contains("Yes"));
			dlg.btnImpactedModuleRemove.setEnabled(dlg.cbImpactedModuleRequired.getText().contains("Yes"));
			dlg.lstImpactedModule.removeAll();
		} else if (type.compareToIgnoreCase("cbIsSendDataToSupplier") == 0) {
			dlg.txtSendDataReason.setEnabled(dlg.cbIsSendDataToSupplier.getText().contains("No"));
			dlg.txtSendDataReason.setText("");
		} else if (type.compareToIgnoreCase("cbHaveDMUCheckRequired") == 0) {
			dlg.txtDMUCheckFile.setText("");
		}
	}

	private void addValue(String type) {
		switch (type) {
		case "MARKET":
			String market = dlg.cbMarket.getText();
			if (!market.isEmpty()) {
				if (checkExistInList(dlg.lstMarket.getItems(), market))
					dlg.lstMarket.add(market);
			}
			break;
		case "VARIANT":
			String variant = dlg.cbVariant.getText();
			if (!variant.isEmpty()) {
				if (checkExistInList(dlg.lstVariant.getItems(), variant))
					dlg.lstVariant.add(variant);
			}
			break;
		case "ECR_CATEGORY_2":
			String category = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
			String category2 = dlg.cbECRCategory2.getText();
			if (!category2.isEmpty()) {
				if (category.compareTo("Gate release") == 0) {
					if (dlg.lstECRCategory2.getItemCount() > 0)
						return;
				}

				if (checkExistInList(dlg.lstECRCategory2.getItems(), category2))
					dlg.lstECRCategory2.add(category2);
			}
			break;
		case "COP_IMPACT":
			String copImpact = dlg.cbCOPImpact.getText();
			if (!copImpact.isEmpty()) {
				if (checkExistInList(dlg.lstCOPImpact.getItems(), copImpact))
					dlg.lstCOPImpact.add(copImpact);
			}
			break;
		case "IMPACTED_MODULE":
			String impactedModule = dlg.cbImpactedModule.getText();
			if (!impactedModule.isEmpty()) {
				if (checkExistInList(dlg.lstImpactedModule.getItems(), impactedModule))
					dlg.lstImpactedModule.add(impactedModule);
			}
		case "IMPLEMENTATION_DATE":
			String implementationDate = dlg.cbImplementationDate.getText();
			if (!implementationDate.isEmpty()) {
				if (checkExistInList(dlg.lstImplementationDate.getItems(), implementationDate))
					dlg.lstImplementationDate.add(implementationDate);
			}
			break;
		}
	}

	private void removeValue(String type) {
		switch (type) {
		case "MARKET":
			int indexMarket = dlg.lstMarket.getSelectionIndex();
			if (indexMarket >= 0)
				dlg.lstMarket.remove(indexMarket);
			break;
		case "VARIANT":
			int indexVariant = dlg.lstVariant.getSelectionIndex();
			if (indexVariant >= 0)
				dlg.lstVariant.remove(indexVariant);
			break;
		case "ECR_CATEGORY_2":
			int indexECRCategory2 = dlg.lstECRCategory2.getSelectionIndex();
			if (indexECRCategory2 >= 0)
				dlg.lstECRCategory2.remove(indexECRCategory2);
			break;
		case "COP_IMPACT":
			int indexCOPImpact = dlg.lstCOPImpact.getSelectionIndex();
			if (indexCOPImpact >= 0)
				dlg.lstCOPImpact.remove(indexCOPImpact);
			break;
		case "IMPACTED_MODULE":
			int indexImpactModule = dlg.lstImpactedModule.getSelectionIndex();
			if (indexImpactModule >= 0)
				dlg.lstImpactedModule.remove(indexImpactModule);
			break;
		case "IMPLEMENTATION_DATE":
			int indexImplementationDate = dlg.lstImplementationDate.getSelectionIndex();
			if (indexImplementationDate >= 0)
				dlg.lstImplementationDate.remove(indexImplementationDate);
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

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information.", IMessageProvider.WARNING);
			return;
		}

		if (!checkCoordinatedChange()) {
			dlg.setMessage("Implementation Date of Coordinated Change not matching.", IMessageProvider.WARNING);
			return;
		}

		if (!checkSILSeverity()) {
			dlg.setMessage("With SIL Severity is PS/PL/W/A please select \"IMMEDIATE CHANGE\" value for Implementation Date.", IMessageProvider.WARNING);
			return;
		}

		DataManagementService dms = DataManagementService.getService(session);
		String name = dlg.txtName.getText();
		String description = dlg.txtDescription.getText();

		String vehicleGroup = (String) dlg.cbVehicleGroup.getData(dlg.cbVehicleGroup.getText());
		String moduleGroup = dlg.cbModuleGroup.getText();
		String[] impactedModule = dlg.lstImpactedModule.getItems();
		String changeReason = dlg.cbChangeReason.getText();
		String[] implementationDate = dlg.lstImplementationDate.getItems();

		Calendar targetReleaseDate = null;
		if (dlg.ckbTargetReleaseDate.getSelection())
			targetReleaseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReleaseDate);

		String ecrCategory = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
		String[] ecrCategory2Display = dlg.lstECRCategory2.getItems();
		Set<String> ecrCategory2Value = new HashSet<String>();
		if (ecrCategory2Display != null && ecrCategory2Display.length > 0) {
			for (String str : ecrCategory2Display) {
				ecrCategory2Value.add((String) dlg.cbECRCategory2.getData(str));
			}
		}
		String newPartStatus = (String) dlg.cbNewPartStatus.getData(dlg.cbNewPartStatus.getText());
		String exchangeNewPart = dlg.cbExchangeNewPart.getText();
		String exchangeOldPart = dlg.cbExchangeOldPart.getText();

		boolean isCoordinatedChangeRequired = dlg.cbCoordinatedChageRequired.getText().contains("Yes");

		boolean isSendDataToSupplier = dlg.cbIsSendDataToSupplier.getText().contains("Yes");
		String sendDataReason = dlg.txtSendDataReason.getText();

		boolean haveDMUCheck = dlg.cbHaveDMUCheckRequired.getText().contains("Yes");

		boolean isINPurchase = dlg.ckbIDP.getSelection();
		boolean isVehicleArchitecture = dlg.ckbVehicleArchitecture.getSelection();
		boolean isHomo = dlg.ckbHomologation.getSelection();
		boolean isCAE = dlg.ckbCAE.getSelection();
		boolean isAFS = dlg.ckbAfterSale.getSelection();
		boolean isStandardPart = dlg.ckbStandardPart.getSelection();
		boolean isSafety = dlg.ckbFunctionSafety.getSelection();
		boolean isColorTrim = dlg.ckbColorTrim.getSelection();
		boolean isSupplierQualityManager = dlg.ckbSupplierQualityManager.getSelection();
		boolean isSaleProductMarketing = dlg.ckbSaleProductMarketing.getSelection();
		boolean isVehicleTVManager = dlg.ckbVehicleTVManager.getSelection();
		boolean isComponentTestingManager = dlg.ckbComponentTestingManager.getSelection();
		boolean isEETVManager = dlg.ckbEETVManager.getSelection();
		boolean isHTKO1 = dlg.ckbHTKO1.getSelection();
		boolean isHTKO2 = dlg.ckbHTKO2.getSelection();

		boolean isPFMEARequired = dlg.cbPFMEARequired.getText().contains("Yes");
		String pfmea = dlg.txtPFMEA.getText();
		boolean isSpecBookRequired = dlg.cbSpecBookRequired.getText().contains("Yes");
		String specBook = dlg.txtSpecBook.getText();
		boolean isDFMEARequired = dlg.cbDFMEARequired.getText().contains("Yes");
		String dfmea = dlg.txtDFMEA.getText();
		boolean isDVPRequired = dlg.cbDVPRequired.getText().contains("Yes");
		String dvp = dlg.txtDVP.getText();

		boolean vehicleCostRequired = dlg.cbVehicleCostRequired.getText().contains("Yes");
		Double vehicleCost = null;
		try {
			vehicleCost = Double.parseDouble(dlg.txtVehicleCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean pieceCostRequired = dlg.cbPieceCostRequired.getText().contains("Yes");
		Double pieceCost = null;
		try {
			pieceCost = Double.parseDouble(dlg.txtPieceCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean toolingCostRequired = dlg.cbToolingCostRequired.getText().contains("Yes");
		Double toolingCost = null;
		try {
			toolingCost = Double.parseDouble(dlg.txtToolingCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean eddCostRequired = dlg.cbEDDCostRequired.getText().contains("Yes");
		Double eddCost = null;
		try {
			eddCost = Double.parseDouble(dlg.txtEDDCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean otherCostRequired = dlg.cbOtherCostRequired.getText().contains("Yes");
		Double otherCost = null;
		try {
			otherCost = Double.parseDouble(dlg.txtOtherCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean sunkCostRequired = dlg.cbSunkCostRequired.getText().contains("Yes");
		Double sunkCost = null;
		try {
			sunkCost = Double.parseDouble(dlg.txtSunkCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean scrapCostRequired = dlg.cbScrapCostRequired.getText().contains("Yes");
		Double scrapCost = null;
		try {
			scrapCost = Double.parseDouble(dlg.txtScrapCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean testingValidationCostRequired = dlg.cbTestingValidationCostRequired.getText().contains("Yes");
		Double testingValidationCost = null;
		try {
			testingValidationCost = Double.parseDouble(dlg.txtTestingValidationCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean plantEquipmentCostRequired = dlg.cbPlantEquipmentCostRequired.getText().contains("Yes");
		Double plantEquipmentCost = null;
		try {
			plantEquipmentCost = Double.parseDouble(dlg.txtPlantEquipmentCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isCoordinatedChangeModuleRequired = dlg.cbImpactedModuleRequired.getText().contains("Yes");

		boolean isCopImpactRequired = dlg.cbCOPImpactRequired.getText().contains("Yes");

		String[] copImpactDisplay = dlg.lstCOPImpact.getItems();
		Set<String> copImpact = new HashSet<String>();
		if (copImpactDisplay != null && copImpactDisplay.length > 0) {
			for (String str : copImpactDisplay) {
				copImpact.add((String) dlg.cbCOPImpact.getData(str));
			}
		}

		boolean isBuildPhaseTimeImpactRequired = dlg.cbBuildPhaseTimeRequired.getText().contains("Yes");
		String buildPhaseTimeImpact = dlg.cbBuildPhaseTime.getText();

		Set<String> silSeverity = new HashSet<String>();
		for (TableItem tableItem : dlg.tblSIL.getItems()) {
			String silSeverityTemp = tableItem.getText(1);
			if (!silSeverityTemp.isEmpty())
				silSeverity.add(silSeverityTemp);
		}

		try {
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);

			itemDef.data.stringProps.put("vf6_vehicle_group", vehicleGroup);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf6_module_group", moduleGroup);
			itemRevisionDef.stringProps.put("vf6_ecr_category", ecrCategory);
			itemDef.data.stringArrayProps.put("vf6_impacted_module_arr", impactedModule);
			itemRevisionDef.stringArrayProps.put("vf6_ecr_category_lv2", ecrCategory2Value.toArray(new String[0]));
			if (targetReleaseDate != null)
				itemDef.data.dateProps.put("vf6_target_release_date", targetReleaseDate);
			itemDef.data.stringProps.put("vf6_change_reason", changeReason);
			itemDef.data.stringArrayProps.put("vf6_implementation_date_arr", implementationDate);

			itemRevisionDef.stringProps.put("vf6_new_parts_status", newPartStatus);
			itemRevisionDef.stringProps.put("vf6_es_exchange_newpart", exchangeNewPart);
			itemRevisionDef.stringProps.put("vf6_es_exchange_oldpart", exchangeOldPart);
			itemRevisionDef.boolProps.put("vf6_is_coord_change_require", isCoordinatedChangeRequired);
			itemRevisionDef.boolProps.put("vf6_is_coor_module_require", isCoordinatedChangeModuleRequired);
			itemRevisionDef.boolProps.put("vf6_is_send_data_to_supp", isSendDataToSupplier);
			itemRevisionDef.stringProps.put("vf6_reason_not_send_data", sendDataReason);
			itemRevisionDef.boolProps.put("vf6_have_DMU_check_result", haveDMUCheck);
			if (silSeverity.size() > 0)
				itemRevisionDef.stringProps.put("vf6_sil_severity", String.join(",", silSeverity));
			itemRevisionDef.boolProps.put("vf6_is_ee_component", vehicleType.compareTo("EE") == 0);

			// DOCUMENTATION
			itemRevisionDef.boolProps.put("vf6_have_DFMEA", isDFMEARequired);
			itemRevisionDef.stringProps.put("vf6_DFMEA_doc", dfmea);
			itemRevisionDef.boolProps.put("vf6_have_PFMEA", isPFMEARequired);
			itemRevisionDef.stringProps.put("vf6_PFMEA_doc", pfmea);
			itemRevisionDef.boolProps.put("vf6_have_spec_book", isSpecBookRequired);
			itemRevisionDef.stringProps.put("vf6_spec_doc", specBook);
			itemRevisionDef.boolProps.put("vf6_have_DVP", isDVPRequired);
			itemRevisionDef.stringProps.put("vf6_DVP_doc", dvp);

			// REVIEW GATE
			itemRevisionDef.boolProps.put("vf6_is_indirectPur_required", isINPurchase);
			itemRevisionDef.boolProps.put("vf6_is_DMU_required", isVehicleArchitecture);
			itemRevisionDef.boolProps.put("vf6_is_homo_required", isHomo);
			itemRevisionDef.boolProps.put("vf6_is_cae_required", isCAE);
			itemRevisionDef.boolProps.put("vf6_is_aftersale_required", isAFS);
			itemRevisionDef.boolProps.put("vf6_is_standard_part", isStandardPart);
			itemRevisionDef.boolProps.put("vf6_is_functional_safety", isSafety);
			itemRevisionDef.boolProps.put("vf6_is_color_trim_required", isColorTrim);
			itemRevisionDef.boolProps.put("vf6_is_sale_and_marketin", isSaleProductMarketing);
			itemRevisionDef.boolProps.put("vf6_is_supplier_qual_manage", isSupplierQualityManager);
			itemRevisionDef.boolProps.put("vf6_is_vehicle_tv_manager", isVehicleTVManager);
			itemRevisionDef.boolProps.put("vf6_is_compon_test_required", isComponentTestingManager);
			itemRevisionDef.boolProps.put("vf6_is_ee_tv_manager_requir", isEETVManager);
			itemRevisionDef.boolProps.put("vf6_is_htko1_required", isHTKO1);
			itemRevisionDef.boolProps.put("vf6_is_htko2_required", isHTKO2);

			// INIT COST
			itemRevisionDef.boolProps.put("vf6_is_veh_cost_required", vehicleCostRequired);
			if (vehicleCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_veh_cost_delta", vehicleCost);

			itemRevisionDef.boolProps.put("vf6_is_piece_cost_require", pieceCostRequired);
			if (pieceCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_piece_cost_delta", pieceCost);

			itemRevisionDef.boolProps.put("vf6_is_tooling_cost_require", toolingCostRequired);
			if (toolingCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_tooling_cost_delta", toolingCost);

			itemRevisionDef.boolProps.put("vf6_is_edd_cost_Required", eddCostRequired);
			if (eddCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_edd_cost_delta", eddCost);

			itemRevisionDef.boolProps.put("vf6_is_other_cost_required", otherCostRequired);
			if (otherCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_other_cost", otherCost);

			itemRevisionDef.boolProps.put("vf6_is_sunk_cost_require", sunkCostRequired);
			if (sunkCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_sunk_cost", sunkCost);

			itemRevisionDef.boolProps.put("vf6_is_scrap_cost_required", scrapCostRequired);
			if (scrapCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_scrap_cost", scrapCost);

			itemRevisionDef.boolProps.put("vf6_is_test_n_vali_required", testingValidationCostRequired);
			if (testingValidationCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_testing_validate_c", testingValidationCost);

			itemRevisionDef.boolProps.put("vf6_is_plant_equipment_cost", plantEquipmentCostRequired);
			if (plantEquipmentCost != null)
				itemRevisionDef.doubleProps.put("vf6_init_plant_equip_cost", plantEquipmentCost);

			// IMPACT
			itemRevisionDef.boolProps.put("vf6_is_cop_impact_require", isCopImpactRequired);
			itemRevisionDef.stringArrayProps.put("vf6_cop_impacted_prog_user", copImpact.toArray(new String[0]));

			itemRevisionDef.boolProps.put("vf6_is_pto_sop_timeImpact", isBuildPhaseTimeImpactRequired);
			itemRevisionDef.stringArrayProps.put("vf6_build_time_impact", new String[] { buildPhaseTimeImpact });

			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "Vf6_ecr_id";
			idGen.stringProps.put("vf6_vehicle_type", vehicleGroup);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
				if (cfgContext != null) {
					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					createECRSumary(itemRev);
					createDataset(itemRev);
					addCoordinatedChange(itemRev);
					initUIDefault();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addCoordinatedChange(TCComponentItemRevision parentItem) {
		if (ecrRevList == null || ecrRevList.size() == 0)
			return;
		try {
			String ecrNumber = parentItem.getPropertyDisplayableValue("item_id");
			parentItem.setRelated("Vf6_CoordinatedChange", ecrRevList.toArray(new TCComponent[0]));
			String ccrNumber = ecrNumber.replace("ECR", "CCR");
			parentItem.getItem().setProperty("vf4_part_reference", ccrNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createECRSumary(TCComponent parentItem) throws Exception {
		boolean silNoRequired = dlg.cbSILNoRequired.getText().contains("Yes");
		Set<String> silNoList = new HashSet<String>();
		for (TableItem tableItem : dlg.tblSIL.getItems()) {
			String silNoTemp = tableItem.getText(0);
			if (!silNoTemp.isEmpty())
				silNoList.add(silNoTemp);
		}

		String silNo = String.join(",", silNoList);
		boolean dcrNoRequired = dlg.cbDCRNoRequired.getText().contains("Yes");
		String dcrNo = dlg.txtDCRNo.getText();

		String supplier = dlg.txtSupplier.getText();

		boolean leadTimeImpactRequired = dlg.cbLeadtimeRequired.getText().contains("Yes");
		Double leadTimeImpact = null;
		try {
			leadTimeImpact = Double.parseDouble(dlg.txtLeadtime.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean buildPhaseTimeRequired = dlg.cbBuildPhaseTimeRequired.getText().contains("Yes");
		Double mrpLateTime = null;
		try {
			mrpLateTime = Double.parseDouble(dlg.txtMRPLateTime.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String problem = dlg.txtProblem.getText();
		String rootCause = dlg.txtRootCause.getText();
		String solution = dlg.txtSolution.getText();
		String[] marketDisplay = dlg.lstMarket.getItems();
		Set<String> marketValue = new HashSet<String>();
		if (marketDisplay != null && marketDisplay.length > 0) {
			for (String str : marketDisplay) {
				marketValue.add((String) dlg.cbMarket.getData(str));
			}
		}
		String lhdrhd = dlg.cbDriving.getText();
		String[] variant = dlg.lstVariant.getItems();
		String seatConfi = dlg.cbSeat.getText();
		String decisionApproval = dlg.cbDecisionApproval.getText();
		String decisionComment = dlg.txtDecisionComment.getText();
		Calendar approvalDate = null;
		if (dlg.ckbApprovalDate.getSelection())
			approvalDate = StringExtension.getDatetimeFromWidget(dlg.datApprovalDate);

		String premiumVariant = dlg.cbPremiumVariant.getText();
		String contactPerson = dlg.txtECRContactPerson.getText();
		String contactEmail = dlg.txtECRContactEmail.getText();
		String currentMaterial = dlg.txtCurrentMaterial.getText();
		String newMaterial = dlg.txtNewMaterial.getText();
		Double currentWeight = null;
		try {
			currentWeight = Double.parseDouble(dlg.txtCurrentWeight.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Double newWeight = null;
		try {
			newWeight = Double.parseDouble(dlg.txtNewWeight.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		TCComponent[] forms = parentItem.getRelatedComponents("Vf6_ecr_info_relation");
		if (forms.length > 0) {
			forms[0].setStringProperty("", "");

			forms[0].setLogicalProperty("vf6_is_sil_no_required", silNoRequired);
			forms[0].setStringProperty("vf6_sil_no", silNo);
			forms[0].setLogicalProperty("vf6_is_dcr_required", dcrNoRequired);
			forms[0].setStringProperty("vf6_dcr_no", dcrNo);
			forms[0].setStringProperty("vf6_supplier", supplier);
			forms[0].setLogicalProperty("vf6_is_leadtime_impact_req", leadTimeImpactRequired);
			if (leadTimeImpact != null)
				forms[0].setDoubleProperty("vf6_leadtime_impact1", leadTimeImpact);

			forms[0].setLogicalProperty("vf6_is_pto_sop_timeImpact", buildPhaseTimeRequired);
			if (mrpLateTime != null)
				forms[0].setDoubleProperty("vf6_pto_sop_time_impact1", mrpLateTime);
			forms[0].setStringProperty("vf6_problem", problem);
			forms[0].setStringProperty("vf6_root_cause", rootCause);
			forms[0].setStringProperty("vf6_solution", solution);
			TCExtension.setStringArrayProperty(forms[0], "vf6_market", marketValue.toArray(new String[0]));
			forms[0].setStringProperty("vf6_lhd_rhd", lhdrhd);
			TCExtension.setStringArrayProperty(forms[0], "vf6_variant", variant);
			TCExtension.setStringArrayProperty(forms[0], "vf6_seat_configuration", new String[] { seatConfi });
			forms[0].setStringProperty("vf6_pre_decision_approval", decisionApproval);
			forms[0].setStringProperty("vf6_precab_decision_comment", decisionComment);
			if (approvalDate != null)
				forms[0].setDateProperty("vf6_precab_approval_date", approvalDate.getTime());
			forms[0].setStringProperty("vf6_base_or_premium", premiumVariant);
			forms[0].setStringProperty("vf6_ecr_contact_person", contactPerson);
			forms[0].setStringProperty("vf6_ecr_contact_email", contactEmail);

			// CHANGE DESC
			forms[0].setStringProperty("vf6_current_material", currentMaterial);
			forms[0].setStringProperty("vf6_new_material", newMaterial);
			if (currentWeight != null)
				forms[0].setDoubleProperty("vf6_current_weight", currentWeight);
			if (newWeight != null)
				forms[0].setDoubleProperty("vf6_new_weight", newWeight);
		}
	}

	private boolean checkRequired() {
		boolean check = true;
		// SUMMARY
		if (dlg.cbVehicleGroup.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblVehicleGroup, true);
		} else {
			warningLabel(dlg.lblVehicleGroup, false);
		}

		if (dlg.txtName.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblName, true);
		} else {
			warningLabel(dlg.lblName, false);
		}

		if (dlg.txtECRContactEmail.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblEcrContactEmail, true);
		} else {
			warningLabel(dlg.lblEcrContactEmail, false);
		}

		if (dlg.txtDescription.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDescription, true);
		} else {
			warningLabel(dlg.lblDescription, false);
		}

		if (dlg.cbModuleGroup.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblModuleGroup, true);
		} else {
			warningLabel(dlg.lblModuleGroup, false);
		}

		if (dlg.cbECRCategory.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblECRCategory, true);
		} else {
			warningLabel(dlg.lblECRCategory, false);
		}

		if (dlg.cbImpactedModuleRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblImpactedModule, true);
		} else {
			if (dlg.cbImpactedModuleRequired.getText().contains("Yes") && dlg.lstImpactedModule.getItemCount() == 0) {
				check = false;
				warningLabel(dlg.lblImpactedModule, true);
			} else {
				warningLabel(dlg.lblImpactedModule, false);
			}
		}

		if (dlg.lstECRCategory2.getItemCount() == 0) {
			check = false;
			warningLabel(dlg.lblECRCategory2, true);
		} else {
			warningLabel(dlg.lblECRCategory2, false);
		}

		if (dlg.lstImplementationDate.getItemCount() == 0) {
			check = false;
			warningLabel(dlg.lblImplementationDate, true);
		} else {
			warningLabel(dlg.lblImplementationDate, false);
		}

		if (dlg.cbChangeReason.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblChangeReason, true);
		} else {
			warningLabel(dlg.lblChangeReason, false);
		}

		if (dlg.cbDCRNoRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDCRNo, true);
		} else {
			if (dlg.cbDCRNoRequired.getText().contains("Yes") && dlg.txtDCRNo.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblDCRNo, true);
			} else {
				warningLabel(dlg.lblDCRNo, false);
			}
		}

		if (dlg.cbSILNoRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSILNo, true);
		} else {
			if (dlg.cbSILNoRequired.getText().contains("Yes") && dlg.tblSIL.getItemCount() == 0) {
				check = false;
				warningLabel(dlg.lblSILNo, true);
			} else {
				warningLabel(dlg.lblSILNo, false);
			}
		}

		if (dlg.cbCoordinatedChageRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblCoordinatedChage, true);
		} else {
			if (dlg.cbCoordinatedChageRequired.getText().contains("Yes") && (ecrRevList == null || ecrRevList.size() == 0)) {
				check = false;
				warningLabel(dlg.lblCoordinatedChage, true);
			} else {
				warningLabel(dlg.lblCoordinatedChage, false);
			}
		}

		if (dlg.cbIsSendDataToSupplier.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblIsSendDataToSupplier, true);
		} else {
			if (dlg.cbIsSendDataToSupplier.getText().contains("No") && dlg.txtSendDataReason.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblIsSendDataToSupplier, true);
			} else {
				warningLabel(dlg.lblIsSendDataToSupplier, false);
			}
		}

		if (dlg.cbHaveDMUCheckRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDMUCheckFile, true);
		} else {
			if (dlg.txtDMUCheckFile.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblDMUCheckFile, true);
			} else {
				warningLabel(dlg.lblDMUCheckFile, false);
			}
		}

		// CHANGE DESC
		if (dlg.cbWeightRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblWeight, true);
		} else {
			if (dlg.cbWeightRequired.getText().contains("Yes") && (dlg.txtCurrentWeight.getText().isEmpty() || dlg.txtNewWeight.getText().isEmpty())) {
				check = false;
				warningLabel(dlg.lblWeight, true);
			} else {
				warningLabel(dlg.lblWeight, false);
			}
		}

		// DOCUMENTATION
		if (dlg.cbPFMEARequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblPFMEA, true);
		} else {
			if (dlg.cbPFMEARequired.getText().contains("Yes") && dlg.txtPFMEA.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblPFMEA, true);
			} else {
				warningLabel(dlg.lblPFMEA, false);
			}
		}

		if (dlg.cbSpecBookRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSpecBook, true);
		} else {
			if (dlg.cbSpecBookRequired.getText().contains("Yes") && dlg.txtSpecBook.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblSpecBook, true);
			} else {
				warningLabel(dlg.lblSpecBook, false);
			}
		}

		if (dlg.cbDFMEARequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDFMEA, true);
		} else {
			if (dlg.cbDFMEARequired.getText().contains("Yes") && dlg.txtDFMEA.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblDFMEA, true);
			} else {
				warningLabel(dlg.lblDFMEA, false);
			}
		}

		if (dlg.cbDVPRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDVP, true);
		} else {
			if (dlg.cbDVPRequired.getText().contains("Yes") && dlg.txtDVP.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblDVP, true);
			} else {
				warningLabel(dlg.lblDVP, false);
			}
		}

		// INIT COST
		if (dlg.cbVehicleCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblVehicleCost, true);
		} else {
			if (dlg.cbVehicleCostRequired.getText().contains("Yes") && dlg.txtVehicleCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblVehicleCost, true);
			} else {
				warningLabel(dlg.lblVehicleCost, false);
			}
		}

		if (dlg.cbPieceCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblPieceCostDelta, true);
		} else {
			if (dlg.cbPieceCostRequired.getText().contains("Yes") && dlg.txtPieceCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblPieceCostDelta, true);
			} else {
				warningLabel(dlg.lblPieceCostDelta, false);
			}
		}

		if (dlg.cbToolingCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblToolingCost, true);
		} else {
			if (dlg.cbToolingCostRequired.getText().contains("Yes") && dlg.txtToolingCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblToolingCost, true);
			} else {
				warningLabel(dlg.lblToolingCost, false);
			}
		}

		if (dlg.cbEDDCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblEddCostDelta, true);
		} else {
			if (dlg.cbEDDCostRequired.getText().contains("Yes") && dlg.txtEDDCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblEddCostDelta, true);
			} else {
				warningLabel(dlg.lblEddCostDelta, false);
			}
		}

		if (dlg.cbSunkCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSunkCost, true);
		} else {
			if (dlg.cbSunkCostRequired.getText().contains("Yes") && dlg.txtSunkCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblSunkCost, true);
			} else {
				warningLabel(dlg.lblSunkCost, false);
			}
		}

		if (dlg.cbPlantEquipmentCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblPlantequipmentCost, true);
		} else {
			if (dlg.cbPlantEquipmentCostRequired.getText().contains("Yes") && dlg.txtPlantEquipmentCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblPlantequipmentCost, true);
			} else {
				warningLabel(dlg.lblPlantequipmentCost, false);
			}
		}

		if (dlg.cbScrapCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblScrapCost, true);
		} else {
			if (dlg.cbScrapCostRequired.getText().contains("Yes") && dlg.txtScrapCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblScrapCost, true);
			} else {
				warningLabel(dlg.lblScrapCost, false);
			}
		}

		if (dlg.cbTestingValidationCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblTestingValidationCost, true);
		} else {
			if (dlg.cbTestingValidationCostRequired.getText().contains("Yes") && dlg.txtTestingValidationCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblTestingValidationCost, true);
			} else {
				warningLabel(dlg.lblTestingValidationCost, false);
			}
		}

		if (dlg.cbOtherCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblOtherCost, true);
		} else {
			if (dlg.cbOtherCostRequired.getText().contains("Yes") && dlg.txtOtherCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblOtherCost, true);
			} else {
				warningLabel(dlg.lblOtherCost, false);
			}
		}

		// PRECAB
		if (dlg.lstMarket.getItemCount() == 0) {
			check = false;
			warningLabel(dlg.lblMarket, true);
		} else {
			warningLabel(dlg.lblMarket, false);
		}
		if (dlg.cbDriving.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDriving, true);
		} else {
			warningLabel(dlg.lblDriving, false);
		}
		if (dlg.lstVariant.getItemCount() == 0) {
			check = false;
			warningLabel(dlg.lblVariant, true);
		} else {
			warningLabel(dlg.lblVariant, false);
		}
		if (dlg.cbPremiumVariant.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblPremiumVariant, true);
		} else {
			warningLabel(dlg.lblPremiumVariant, false);
		}

		// IMPACT
		if (dlg.txtSupplier.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSupplier, true);
		} else {
			warningLabel(dlg.lblSupplier, false);
		}

		if (dlg.cbCOPImpactRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblCOPImpact, true);
		} else {
			if (dlg.cbCOPImpactRequired.getText().contains("Yes") && dlg.lstCOPImpact.getItemCount() == 0) {
				check = false;
				warningLabel(dlg.lblCOPImpact, true);
			} else {
				warningLabel(dlg.lblCOPImpact, false);
			}
		}

		if (dlg.cbLeadtimeRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblLeadtimeImpact, true);
		} else {
			if (dlg.cbLeadtimeRequired.getText().contains("Yes") && dlg.txtLeadtime.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblLeadtimeImpact, true);
			} else {
				warningLabel(dlg.lblLeadtimeImpact, false);
			}
		}

		if (dlg.cbBuildPhaseTimeRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblBuildPhaseTime, true);
		} else {
			if (dlg.cbBuildPhaseTimeRequired.getText().contains("Yes")) {
				if (dlg.cbBuildPhaseTime.getText().isEmpty()) {
					check = false;
					warningLabel(dlg.lblBuildPhaseTime, true);
				} else {
					warningLabel(dlg.lblBuildPhaseTime, false);
				}
			}
		}

		if (dlg.txtProblem.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblProblem, true);
		}

		if (dlg.txtRootCause.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblRootCause, true);
		}

		if (dlg.txtSolution.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSolution, true);
		}

		return check;
	}

	private boolean checkCoordinatedChange() {
		boolean isValid = true;
		if (dlg.cbCoordinatedChageRequired.getText().compareTo("Yes") == 0) {
			List<String> implementationDate = Arrays.asList(dlg.lstImplementationDate.getItems());
			isValid = false;
			for (TCComponent ecrItem : ecrRevList) {
				try {
					String implement = ecrItem.getPropertyDisplayableValue("vf6_implementation_date_arr");
					String[] str = implement.split(",");
					for (String imp : str) {
						if (implementationDate.contains(imp.stripLeading().stripTrailing())) {
							isValid = true;
						}
//						if (!implementationDate.contains(imp.stripLeading().stripTrailing()))
//							return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return isValid;
	}

	private boolean checkSILSeverity() {
		for (TableItem tableItem : dlg.tblSIL.getItems()) {
			String silSeverity = tableItem.getText(1);
			if (!silSeverity.isEmpty()) {
				if (silSeverity.compareTo("PS") == 0 || silSeverity.compareTo("PL") == 0 || silSeverity.compareTo("W") == 0 || silSeverity.compareTo("A") == 0) {
					boolean check = false;
					for (String implement : dlg.lstImplementationDate.getItems()) {
						if (implement.compareTo("IMMEDIATE CHANGE") == 0)
							check = true;
					}

					if (!check)
						return false;
				}
			}
		}

		return true;
	}

	private void warningLabel(Label target, boolean warning) {
		if (warning)
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showProgressDialog(boolean isShow) {
		if (isShow) {
			if (progressMonitorDialog == null) {
				progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
				progressMonitorDialog.open();
			}
		} else {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
		}
	}

	private void removeValueFromLinkedList(LinkedHashMap<String, String> linkedList, String key) {
		if (linkedList.containsKey(key))
			linkedList.remove(key);
	}

	private void createDataset(TCComponent itemRev) {
		String filePath = dlg.txtDMUCheckFile.getText();
		String datasetType = "";
		if (dlg.cbHaveDMUCheckRequired.getText().contains("No")) {
			if (filePath.endsWith(".pdf")) {
				datasetType = "PDF";
			} else {
				datasetType = "Outlook";
			}
		} else {
			datasetType = "MSExcelX";
		}

		NamedReferenceContext namedReferenceContext[] = null;
		String namedRef = null;

		try {
			String fileName = "DMU Check";
			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
			namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			TCComponentDataset newDataset = datasetTypeComponent.create(fileName, fileName, datasetDefinition.toString());
			newDataset.refresh();
			newDataset.setFiles(new String[] { filePath }, type);

			itemRev.add("Vf6_DMU_Relation", newDataset);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

//	private String genCCR() {
//		String ccrNumber = "";
//		try {
//			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
//			queryInput.put("Item ID", "CCR_COUNTER");
//
//			TCComponent[] queryOutput = TCExtension.queryItem(session, queryInput, "Item...");
//			if (queryInput != null && queryInput.size() > 0) {
//				TCComponent ccrCounterItem = queryOutput[0];
//				if (ccrCounterItem.isCheckedOut())
//					return "";
//
//				String counterString = ccrCounterItem.getPropertyDisplayableValue("object_desc");
//				if (!counterString.isEmpty()) {
//					int counter = Integer.parseInt(counterString.replace("CCR", ""));
//					ccrNumber = "CCR" + StringExtension.ConvertNumberToString(++counter, 8);
//				} else {
//					ccrNumber = "CCR" + "00000001";
//				}
//
//				if (ccrCounterItem.okToCheckout()) {
//					ICCTReservationService reserveService = new ICCTReservationService(session.getSoaConnection());
//					reserveService.reserve(new String[] { ccrCounterItem.getUid() }, "", "", (short) 2);
//
//					ccrCounterItem.setProperty("object_desc", ccrNumber);
//
//					reserveService.unReserve(new String[] { ccrCounterItem.getUid() }, false);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return ccrNumber;
//	}

}
