package com.teamcenter.vines.ecr;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import com.teamcenter.vinfast.subdialog.SearchItem_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VESECRCreate_Handler extends AbstractHandler {
	private TCSession session;
	private VESECRCreate_Dialog dlg;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private static String OBJECT_TYPE = "VF4_VinES_ECR";
	private TCComponent selectedObject = null;
	private LinkedList<TCComponent> ecrRevList = null;

	public VESECRCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			// Load data form
			LinkedHashMap<String, String> vehicleGroupDataForm = TCExtension.GetLovValueAndDisplay("vf4_vehicle_group", OBJECT_TYPE, session);

			String[] moduleGroupDataForm = TCExtension.GetLovValues("vf4_module_group", OBJECT_TYPE, session);
			String[] changeReasonDataForm = TCExtension.GetLovValues("vf4_change_reason", OBJECT_TYPE, session);
			String[] implementationDateDataForm = TCExtension.GetLovValues("vf4_implementation_date_arr", OBJECT_TYPE, session);

			String[] impactedModuleGroupDataForm = TCExtension.GetLovValues("vf4_impacted_module_arr", OBJECT_TYPE, session);

			LinkedHashMap<String, String> newPartStatusDataForm = TCExtension.GetLovValueAndDisplay("vf4_new_parts_status", OBJECT_TYPE + "Revision", session);
			newPartStatusDataForm.put("", "");
			String[] exchangeNewPartDataForm = TCExtension.GetLovValues("vf4_es_exchange_newpart", OBJECT_TYPE + "Revision", session);
			String[] exchangeOldPartDataForm = TCExtension.GetLovValues("vf4_es_exchange_oldpart", OBJECT_TYPE + "Revision", session);

			LinkedHashMap<String, String> marketDataForm = TCExtension.GetLovValueAndDisplay("vf4_market", OBJECT_TYPE + "Revision", session);
			String[] lhdrhdDataForm = TCExtension.GetLovValues("vf4_lhd_rhd", OBJECT_TYPE + "Revision", session);
			String[] variantDataForm = TCExtension.GetLovValues("vf4_variant", OBJECT_TYPE + "Revision", session);
			String[] decisionApprovalDataForm = TCExtension.GetLovValues("vf4_pre_decision_approval", OBJECT_TYPE + "Revision", session);
			LinkedHashMap<String, String> ecrCategoryDataForm = TCExtension.GetLovValueAndDescription("vf4_ecr_category", OBJECT_TYPE + "Revision", session);
			String[] requiredDataForm = new String[] { "Yes", "No" };
			String[] buildPhaseTimeDataForm = TCExtension.GetLovValues("vf4_build_time_impact", OBJECT_TYPE + "Revision", session);

			// Init UI
			dlg = new VESECRCreate_Dialog(new Shell());
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

			dlg.cbLogisticCostRequired.setItems(requiredDataForm);
			dlg.cbLogisticCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbLogisticCostRequired");
				}
			});

			dlg.cbTestingValidationCostRequired.setItems(requiredDataForm);
			dlg.cbTestingValidationCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbTestingValidationCost");
				}
			});

			dlg.cbReworkCostRequired.setItems(requiredDataForm);
			dlg.cbReworkCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbReworkCostRequired");
				}
			});

			dlg.cbBudgetCostRequired.setItems(requiredDataForm);
			dlg.cbBudgetCostRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbBudgetCostRequired");
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

			dlg.cbToolingImpactRequired.setItems(requiredDataForm);
			dlg.cbToolingImpactRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbToolingImpactRequired");
				}
			});

			dlg.cbCustomerImpactRequired.setItems(requiredDataForm);
			dlg.cbCustomerImpactRequired.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateRequired("cbCustomerImpactRequired");
				}
			});

			// ---------------------------------------------------
			dlg.cbECRCategory.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.cbECRCategory.getText().isEmpty()) {
						dlg.cbECRCategory2.removeAll();
						dlg.lstECRCategory2.removeAll();

						String ecrCategory = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
						try {
							LinkedHashMap<String, String> dataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf4_ecr_category", OBJECT_TYPE + "Revision", new String[] { ecrCategory }, session);
							StringExtension.UpdateValueTextCombobox(dlg.cbECRCategory2, dataForm);
						} catch (Exception e) {
							e.printStackTrace();
						}

						updateReviewGate();
					}
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbProgramGroup, vehicleGroupDataForm);

			dlg.cbModuleGroup.setItems(moduleGroupDataForm);
			dlg.cbImpactedModule.setItems(impactedModuleGroupDataForm);
			dlg.cbImplementationDate.setItems(implementationDateDataForm);

			dlg.btnDMUUpload.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(dlg.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "Excel Files|*.xls;*.xlsx;*.xlsm" };
					fd.setFilterExtensions(filterExt);
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

			dlg.btnECRAdd.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchItem_Dialog searchDlg = new SearchItem_Dialog(dlg.getShell(), "VinES ECR");
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							TCComponent ecrItem = searchDlg.itemSearch.get(index);
							try {
								if (!ecrRevList.contains(ecrItem)) {
									ecrRevList.add(ecrItem);
									dlg.lstCoordinatedChage.add(ecrItem.getPropertyDisplayableValue("item_id"));
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
					int indexECRRev = dlg.lstCoordinatedChage.getSelectionIndex();
					if (indexECRRev >= 0) {
						dlg.lstCoordinatedChage.remove(indexECRRev);
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
		dlg.cbProgramGroup.deselectAll();
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
		dlg.txtSILNo.setText("");
		dlg.cbCoordinatedChageRequired.deselectAll();
		dlg.lstCoordinatedChage.removeAll();
		ecrRevList = new LinkedList<>();
		dlg.cbImpactedModuleRequired.deselectAll();
		dlg.cbIsSendDataToSupplier.setText("No");

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
		dlg.cbLogisticCostRequired.deselectAll();
		dlg.txtLogisticCost.setText("");
		dlg.cbTestingValidationCostRequired.deselectAll();
		dlg.txtTestingValidationCost.setText("");
		dlg.cbOtherCostRequired.deselectAll();
		dlg.txtOtherCost.setText("");
		dlg.cbReworkCostRequired.deselectAll();
		dlg.txtReworkCost.setText("");
		dlg.cbBudgetCostRequired.deselectAll();
		dlg.txtBudgetCost.setText("");

		// PRECAB
		dlg.lstMarket.removeAll();
		dlg.cbMarket.deselectAll();
		dlg.cbDriving.deselectAll();
		dlg.lstVariant.removeAll();
		dlg.cbVariant.deselectAll();
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
		dlg.cbToolingImpactRequired.deselectAll();
		dlg.txtToolingImpact.setText("");
		dlg.cbCustomerImpactRequired.deselectAll();
		dlg.txtCustomerImpact.setText("");
		dlg.txtProblem.setText("");
		dlg.txtRootCause.setText("");
		dlg.txtSolution.setText("");
	}

	private void testCase() {
		if (!TCExtension.checkPermission(new String[] { "dba" }, session))
			return;

		dlg.cbProgramGroup.setText("ESS");
		dlg.txtName.setText("txtName");
		dlg.txtECRContactPerson.setText("txtECRContactPerson");
		dlg.txtECRContactEmail.setText("txtECRContactEmail");
		dlg.txtDescription.setText("txtDescription");

		dlg.cbModuleGroup.setText("EE");
		dlg.lstImpactedModule.setItems("ESS");
		dlg.cbECRCategory.setText("Others - Not belong to all category above");
		dlg.lstECRCategory2.setItems("Others");
		dlg.lstImplementationDate.setItems("IMMEDIATE CHANGE");
		dlg.cbChangeReason.setText("Cost Saving");
		dlg.cbNewPartStatus.setText("I");
		dlg.cbExchangeNewPart.setText("New parts must not be used on old models");
		dlg.cbExchangeOldPart.setText("Old parts must not be used on new models");
		dlg.cbSILNoRequired.setText("Yes");
		dlg.txtSILNo.setText("txtSILNo");
		dlg.cbDCRNoRequired.setText("Yes");
		dlg.txtDCRNo.setText("txtDCRNo");
		dlg.cbImpactedModuleRequired.setText("Yes");
		dlg.cbCoordinatedChageRequired.setText("No");
		dlg.cbHaveDMUCheckRequired.setText("No");
		dlg.cbIsSendDataToSupplier.setText("Yes");

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
		dlg.cbLogisticCostRequired.setText("Yes");
		dlg.txtLogisticCost.setText("7");
		dlg.cbTestingValidationCostRequired.setText("Yes");
		dlg.txtTestingValidationCost.setText("8");
		dlg.cbOtherCostRequired.setText("Yes");
		dlg.txtOtherCost.setText("9");
		dlg.cbReworkCostRequired.setText("Yes");
		dlg.txtReworkCost.setText("10");
		dlg.cbBudgetCostRequired.setText("Yes");
		dlg.txtBudgetCost.setText("11");

		dlg.lstMarket.setItems("Vietnam");
		dlg.cbDriving.setText("ALL");
		dlg.lstVariant.setItems("VF - BUS");
		dlg.cbDecisionApproval.setText("APPROVED");
		dlg.txtDecisionComment.setText("txtDecisionComment");

		dlg.txtSupplier.setText("txtSupplier");
		dlg.cbCOPImpactRequired.setText("Yes");
		dlg.lstCOPImpact.setItems("VFe36");
		dlg.cbLeadtimeRequired.setText("Yes");
		dlg.txtLeadtime.setText("1");
		dlg.cbBuildPhaseTimeRequired.setText("No");
		dlg.cbBuildPhaseTime.setText("LS");
		dlg.txtMRPLateTime.setText("1");
		dlg.cbToolingImpactRequired.setText("Yes");
		dlg.txtToolingImpact.setText("1");
		dlg.cbCustomerImpactRequired.setText("Yes");
		dlg.txtCustomerImpact.setText("txtCustomerImpact");
		dlg.txtProblem.setText("txtProblem");
		dlg.txtRootCause.setText("txtRootCause");
		dlg.txtSolution.setText("txtSolution");
	}

	private void updateReviewGate() {
		String ecrCategorty = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
		if (ecrCategorty.isEmpty())
			return;

		if (ecrCategorty.compareTo("Gate Release") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, true);
			updateCheckbox(dlg.ckbCAE, true);
			updateCheckbox(dlg.ckbHomologation, true);
			updateCheckbox(dlg.ckbAfterSale, true);
			updateCheckbox(dlg.ckbFunctionSafety, true);
			updateCheckbox(dlg.ckbCostEngineering, true);
			updateCheckbox(dlg.ckbDirectPurchase, true);
			updateCheckbox(dlg.ckbSupplierQualityManager, true);
			updateCheckbox(dlg.ckbHTKO1, null);
			updateCheckbox(dlg.ckbHTKO2, null);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("Sample Release") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, false);
			updateCheckbox(dlg.ckbCAE, false);
			updateCheckbox(dlg.ckbHomologation, false);
			updateCheckbox(dlg.ckbAfterSale, false);
			updateCheckbox(dlg.ckbFunctionSafety, false);
			updateCheckbox(dlg.ckbCostEngineering, false);
			updateCheckbox(dlg.ckbDirectPurchase, null);
			updateCheckbox(dlg.ckbSupplierQualityManager, false);
			updateCheckbox(dlg.ckbHTKO1, false);
			updateCheckbox(dlg.ckbHTKO2, false);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("Design Change") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, true);
			updateCheckbox(dlg.ckbCAE, true);
			updateCheckbox(dlg.ckbHomologation, true);
			updateCheckbox(dlg.ckbAfterSale, true);
			updateCheckbox(dlg.ckbFunctionSafety, true);
			updateCheckbox(dlg.ckbCostEngineering, true);
			updateCheckbox(dlg.ckbDirectPurchase, true);
			updateCheckbox(dlg.ckbSupplierQualityManager, true);
			updateCheckbox(dlg.ckbHTKO1, null);
			updateCheckbox(dlg.ckbHTKO2, null);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("Document For Pack") == 0) {
			updateCheckbox(dlg.ckbDMU, null);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, true);
			updateCheckbox(dlg.ckbCAE, false);
			updateCheckbox(dlg.ckbHomologation, true);
			updateCheckbox(dlg.ckbAfterSale, false);
			updateCheckbox(dlg.ckbFunctionSafety, true);
			updateCheckbox(dlg.ckbCostEngineering, false);
			updateCheckbox(dlg.ckbDirectPurchase, false);
			updateCheckbox(dlg.ckbSupplierQualityManager, true);
			updateCheckbox(dlg.ckbHTKO1, false);
			updateCheckbox(dlg.ckbHTKO2, false);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("Document For Component") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, false);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, false);
			updateCheckbox(dlg.ckbManufacturingQuality, false);
			updateCheckbox(dlg.ckbManufacturing, false);
			updateCheckbox(dlg.ckbTestingValidation, null);
			updateCheckbox(dlg.ckbCAE, false);
			updateCheckbox(dlg.ckbHomologation, null);
			updateCheckbox(dlg.ckbAfterSale, false);
			updateCheckbox(dlg.ckbFunctionSafety, null);
			updateCheckbox(dlg.ckbCostEngineering, false);
			updateCheckbox(dlg.ckbDirectPurchase, false);
			updateCheckbox(dlg.ckbSupplierQualityManager, null);
			updateCheckbox(dlg.ckbHTKO1, false);
			updateCheckbox(dlg.ckbHTKO2, false);
			updateCheckbox(dlg.ckbDCEO, null);
		} else if (ecrCategorty.compareTo("Software") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, true);
			updateCheckbox(dlg.ckbCAE, true);
			updateCheckbox(dlg.ckbHomologation, true);
			updateCheckbox(dlg.ckbAfterSale, true);
			updateCheckbox(dlg.ckbFunctionSafety, true);
			updateCheckbox(dlg.ckbCostEngineering, true);
			updateCheckbox(dlg.ckbDirectPurchase, true);
			updateCheckbox(dlg.ckbSupplierQualityManager, true);
			updateCheckbox(dlg.ckbHTKO1, null);
			updateCheckbox(dlg.ckbHTKO2, null);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("M/C Release") == 0) {
			updateCheckbox(dlg.ckbDMU, true);
			updateCheckbox(dlg.ckbSystemArchitectApproval, true);
			updateCheckbox(dlg.ckbMGL, true);
			updateCheckbox(dlg.ckbQualityEngineering, true);
			updateCheckbox(dlg.ckbManufacturingQuality, true);
			updateCheckbox(dlg.ckbManufacturing, true);
			updateCheckbox(dlg.ckbTestingValidation, false);
			updateCheckbox(dlg.ckbCAE, false);
			updateCheckbox(dlg.ckbHomologation, false);
			updateCheckbox(dlg.ckbAfterSale, false);
			updateCheckbox(dlg.ckbFunctionSafety, false);
			updateCheckbox(dlg.ckbCostEngineering, false);
			updateCheckbox(dlg.ckbDirectPurchase, true);
			updateCheckbox(dlg.ckbSupplierQualityManager, false);
			updateCheckbox(dlg.ckbHTKO1, false);
			updateCheckbox(dlg.ckbHTKO2, false);
			updateCheckbox(dlg.ckbDCEO, true);
		} else if (ecrCategorty.compareTo("Others") == 0) {
			updateCheckbox(dlg.ckbDMU, null);
			updateCheckbox(dlg.ckbSystemArchitectApproval, null);
			updateCheckbox(dlg.ckbMGL, null);
			updateCheckbox(dlg.ckbQualityEngineering, null);
			updateCheckbox(dlg.ckbManufacturingQuality, null);
			updateCheckbox(dlg.ckbManufacturing, null);
			updateCheckbox(dlg.ckbTestingValidation, null);
			updateCheckbox(dlg.ckbCAE, null);
			updateCheckbox(dlg.ckbHomologation, null);
			updateCheckbox(dlg.ckbAfterSale, null);
			updateCheckbox(dlg.ckbFunctionSafety, null);
			updateCheckbox(dlg.ckbCostEngineering, null);
			updateCheckbox(dlg.ckbDirectPurchase, null);
			updateCheckbox(dlg.ckbSupplierQualityManager, null);
			updateCheckbox(dlg.ckbHTKO1, null);
			updateCheckbox(dlg.ckbHTKO2, null);
			updateCheckbox(dlg.ckbDCEO, null);
		}
	}

	private void updateRequired(String type) {
		if (type.compareToIgnoreCase("cbSILNo") == 0) {
			dlg.txtSILNo.setEnabled(dlg.cbSILNoRequired.getText().contains("Yes"));
			dlg.txtSILNo.setText("");
		} else if (type.compareToIgnoreCase("cbDCRNo") == 0) {
			dlg.txtDCRNo.setEnabled(dlg.cbDCRNoRequired.getText().contains("Yes"));
			dlg.txtDCRNo.setText("");
		} else if (type.compareToIgnoreCase("cbCoordinatedChage") == 0) {
			dlg.btnECRAdd.setEnabled(dlg.cbCoordinatedChageRequired.getText().contains("Yes"));
			dlg.btnECRRemove.setEnabled(dlg.cbCoordinatedChageRequired.getText().contains("Yes"));
			dlg.lstCoordinatedChage.removeAll();
		} else if (type.compareToIgnoreCase("cbVehicleCost") == 0) {
			dlg.txtVehicleCost.setEnabled(dlg.cbVehicleCostRequired.getText().contains("Yes"));
			dlg.txtVehicleCost.setText("");
		} else if (type.compareToIgnoreCase("cbToolingCost") == 0) {
			dlg.txtToolingCost.setEnabled(dlg.cbToolingCostRequired.getText().contains("Yes"));
			dlg.txtToolingCost.setText("");
		} else if (type.compareToIgnoreCase("cbSunkCost") == 0) {
			dlg.txtSunkCost.setEnabled(dlg.cbSunkCostRequired.getText().contains("Yes"));
			dlg.txtSunkCost.setText("");
		} else if (type.compareToIgnoreCase("cbLogisticCostRequired") == 0) {
			dlg.txtLogisticCost.setEnabled(dlg.cbLogisticCostRequired.getText().contains("Yes"));
			dlg.txtLogisticCost.setText("");
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
		} else if (type.compareToIgnoreCase("cbReworkCostRequired") == 0) {
			dlg.txtReworkCost.setEnabled(dlg.cbReworkCostRequired.getText().contains("Yes"));
			dlg.txtReworkCost.setText("");
		} else if (type.compareToIgnoreCase("cbBudgetCostRequired") == 0) {
			dlg.txtBudgetCost.setEnabled(dlg.cbBudgetCostRequired.getText().contains("Yes"));
			dlg.txtBudgetCost.setText("");
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
			dlg.cbBuildPhaseTime.deselectAll();
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
		} else if (type.compareToIgnoreCase("cbToolingImpactRequired") == 0) {
			dlg.txtToolingImpact.setEnabled(dlg.cbToolingImpactRequired.getText().contains("Yes"));
			dlg.txtToolingImpact.setText("");
		} else if (type.compareToIgnoreCase("cbCustomerImpactRequired") == 0) {
			dlg.txtCustomerImpact.setEnabled(dlg.cbCustomerImpactRequired.getText().contains("Yes"));
			dlg.txtCustomerImpact.setText("");
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
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		DataManagementService dms = DataManagementService.getService(session);
		// item

		// SUMMARY
		String programGroup = (String) dlg.cbProgramGroup.getData(dlg.cbProgramGroup.getText());
		String ecrContactPerson = dlg.txtECRContactPerson.getText();
		String name = dlg.txtName.getText();
		String ecrContactEmail = dlg.txtECRContactEmail.getText();
		String description = dlg.txtDescription.getText();
		String moduleGroup = dlg.cbModuleGroup.getText();
		String ecrCategory = (String) dlg.cbECRCategory.getData(dlg.cbECRCategory.getText());
		boolean isImpactedOtherModuleRequired = dlg.cbImpactedModuleRequired.getText().contains("Yes");
		String[] impactedModule = dlg.lstImpactedModule.getItems();
		String[] ecrCategory2Display = dlg.lstECRCategory2.getItems();
		Set<String> ecrCategory2Value = new HashSet<String>();
		if (ecrCategory2Display != null && ecrCategory2Display.length > 0) {
			for (String str : ecrCategory2Display) {
				ecrCategory2Value.add((String) dlg.cbECRCategory2.getData(str));
			}
		}
		String[] implementationDate = dlg.lstImplementationDate.getItems();
		String changeReason = dlg.cbChangeReason.getText();
		String newPartStatus = (String) dlg.cbNewPartStatus.getData(dlg.cbNewPartStatus.getText());
		Calendar targetReleaseDate = null;
		if (dlg.ckbTargetReleaseDate.getSelection())
			targetReleaseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReleaseDate);
		boolean isDCRNoRequired = dlg.cbDCRNoRequired.getText().contains("Yes");
		String dcrNo = dlg.txtDCRNo.getText();
		String exchangeNewPart = dlg.cbExchangeNewPart.getText();
		boolean isSILNoRequired = dlg.cbSILNoRequired.getText().contains("Yes");
		String silNo = dlg.txtSILNo.getText();
		String exchangeOldPart = dlg.cbExchangeOldPart.getText();
		boolean isCoordinatedChangeRequired = dlg.cbCoordinatedChageRequired.getText().contains("Yes");
		boolean haveDMUCheck = dlg.cbHaveDMUCheckRequired.getText().contains("Yes");
		boolean isSendDataToSupplier = dlg.cbIsSendDataToSupplier.getText().contains("Yes");
		String sendDataReason = dlg.txtSendDataReason.getText();
		// CHANGE DESCRIPTION
		String currentMaterial = dlg.txtCurrentMaterial.getText();
		String newMaterial = dlg.txtNewMaterial.getText();
		boolean isWeightRequired = dlg.cbWeightRequired.getText().contains("Yes");
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
		// DOCUMENTION
		boolean isPFMEARequired = dlg.cbPFMEARequired.getText().contains("Yes");
		String pfmea = dlg.txtPFMEA.getText();
		boolean isSpecBookRequired = dlg.cbSpecBookRequired.getText().contains("Yes");
		String specBook = dlg.txtSpecBook.getText();
		boolean isDFMEARequired = dlg.cbDFMEARequired.getText().contains("Yes");
		String dfmea = dlg.txtDFMEA.getText();
		boolean isDVPRequired = dlg.cbDVPRequired.getText().contains("Yes");
		String dvp = dlg.txtDVP.getText();
		// REVIEW GATE
		boolean isDMU = dlg.ckbDMU.getSelection();
		boolean isSystemArchitectApproval = dlg.ckbSystemArchitectApproval.getSelection();
		boolean isMGL = dlg.ckbMGL.getSelection();
		boolean isQualityEngineering = dlg.ckbQualityEngineering.getSelection();
		boolean isManufacturingQuality = dlg.ckbManufacturingQuality.getSelection();
		boolean isManufacturing = dlg.ckbManufacturing.getSelection();
		boolean isTestingValidation = dlg.ckbTestingValidation.getSelection();
		boolean isCAE = dlg.ckbCAE.getSelection();
		boolean isHomo = dlg.ckbHomologation.getSelection();
		boolean isAFS = dlg.ckbAfterSale.getSelection();
		boolean isFunctionSafety = dlg.ckbFunctionSafety.getSelection();
		boolean isCostEngineering = dlg.ckbCostEngineering.getSelection();
		boolean isDirectPurchase = dlg.ckbDirectPurchase.getSelection();
		boolean isINDirectPurchase = dlg.ckbIndirectPurchase.getSelection();
		boolean isSupplierQualityManager = dlg.ckbSupplierQualityManager.getSelection();
		boolean isHTKO1 = dlg.ckbHTKO1.getSelection();
		boolean isHTKO2 = dlg.ckbHTKO2.getSelection();
		boolean isDCEO = dlg.ckbDCEO.getSelection();
		// INIT COST
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

		boolean logisticCostRequired = dlg.cbLogisticCostRequired.getText().contains("Yes");
		Double logisticCost = null;
		try {
			logisticCost = Double.parseDouble(dlg.txtLogisticCost.getText());
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

		boolean reworkCostRequired = dlg.cbReworkCostRequired.getText().contains("Yes");
		Double reworkCost = null;
		try {
			reworkCost = Double.parseDouble(dlg.txtReworkCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean budgetCostRequired = dlg.cbBudgetCostRequired.getText().contains("Yes");
		Double budgetCost = null;
		try {
			budgetCost = Double.parseDouble(dlg.txtBudgetCost.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// PRE-CAB
		String[] marketDisplay = dlg.lstMarket.getItems();
		Set<String> marketValue = new HashSet<String>();
		if (marketDisplay != null && marketDisplay.length > 0) {
			for (String str : marketDisplay) {
				marketValue.add((String) dlg.cbMarket.getData(str));
			}
		}
		String lhdrhd = dlg.cbDriving.getText();
		String[] variant = dlg.lstVariant.getItems();
		String decisionApproval = dlg.cbDecisionApproval.getText();
		String decisionComment = dlg.txtDecisionComment.getText();
		Calendar approvalDate = null;
		if (dlg.ckbApprovalDate.getSelection())
			approvalDate = StringExtension.getDatetimeFromWidget(dlg.datApprovalDate);
		// IMPACT
		String supplier = dlg.txtSupplier.getText();
		boolean isCopImpactRequired = dlg.cbCOPImpactRequired.getText().contains("Yes");
		String[] copImpactDisplay = dlg.lstCOPImpact.getItems();
		Set<String> copImpact = new HashSet<String>();
		if (copImpactDisplay != null && copImpactDisplay.length > 0) {
			for (String str : copImpactDisplay) {
				copImpact.add((String) dlg.cbCOPImpact.getData(str));
			}
		}

		boolean leadTimeImpactRequired = dlg.cbLeadtimeRequired.getText().contains("Yes");
		Double leadTimeImpact = null;
		try {
			leadTimeImpact = Double.parseDouble(dlg.txtLeadtime.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isBuildPhaseTimeRequired = dlg.cbBuildPhaseTimeRequired.getText().contains("Yes");
		String buildPhaseTimeImpact = dlg.cbBuildPhaseTime.getText();
		Double mrpLateTime = null;
		try {
			mrpLateTime = Double.parseDouble(dlg.txtMRPLateTime.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isToolingImpactRequired = dlg.cbToolingImpactRequired.getText().contains("Yes");
		Double toolingImpact = null;
		try {
			toolingImpact = Double.parseDouble(dlg.txtToolingImpact.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isCustomerImpactRequired = dlg.cbCustomerImpactRequired.getText().contains("Yes");
		String customerImpact = dlg.txtCustomerImpact.getText();

		String problem = dlg.txtProblem.getText();
		String rootCause = dlg.txtRootCause.getText();
		String solution = dlg.txtSolution.getText();

		try {
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);

			itemDef.data.stringProps.put("vf4_vehicle_group", programGroup);
			itemRevisionDef.stringProps.put("vf4_ecr_contact_person", ecrContactPerson);
			itemDef.data.stringProps.put("object_name", name);
			itemRevisionDef.stringProps.put("vf4_ecr_contact_email", ecrContactEmail);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_module_group", moduleGroup);
			itemRevisionDef.stringProps.put("vf4_ecr_category", ecrCategory);
			itemRevisionDef.boolProps.put("vf4_is_req_impact_module", isImpactedOtherModuleRequired);
			itemDef.data.stringArrayProps.put("vf4_impacted_module_arr", impactedModule);
			itemRevisionDef.stringArrayProps.put("vf4_ecr_category2", ecrCategory2Value.toArray(new String[0]));
			itemDef.data.stringArrayProps.put("vf4_implementation_date_arr", implementationDate);
			itemDef.data.stringProps.put("vf4_change_reason", changeReason);
			itemRevisionDef.stringProps.put("vf4_new_parts_status", newPartStatus);
			if (targetReleaseDate != null)
				itemDef.data.dateProps.put("vf4_target_release_date", targetReleaseDate);
			itemRevisionDef.boolProps.put("vf4_is_dcr_required", isDCRNoRequired);
			itemRevisionDef.stringProps.put("vf4_dcr_no", dcrNo);
			itemRevisionDef.stringProps.put("vf4_es_exchange_newpart", exchangeNewPart);
			itemRevisionDef.boolProps.put("vf4_is_sil_no_required", isSILNoRequired);
			itemRevisionDef.stringProps.put("vf4_sil_no", silNo);
			itemRevisionDef.stringProps.put("vf4_es_exchange_oldpart", exchangeOldPart);
			itemRevisionDef.boolProps.put("vf4_is_coord_change_require", isCoordinatedChangeRequired);
			itemRevisionDef.boolProps.put("vf4_have_DMU_check_result", haveDMUCheck);
			itemRevisionDef.boolProps.put("vf4_is_send_data_to_supp", isSendDataToSupplier);
			itemRevisionDef.stringProps.put("vf4_reason_not_send_data", sendDataReason);
			// CHANGE DESCRIPTION
			itemRevisionDef.stringProps.put("vf4_current_material", currentMaterial);
			itemRevisionDef.stringProps.put("vf4_new_material", newMaterial);
			itemRevisionDef.boolProps.put("vf4_is_input_weight_require", isWeightRequired);
			if (currentWeight != null)
				itemRevisionDef.doubleProps.put("vf4_current_weight", currentWeight);
			if (newWeight != null)
				itemRevisionDef.doubleProps.put("vf4_new_weight", newWeight);
			// DOCUMENTATION
			itemRevisionDef.boolProps.put("vf4_have_DFMEA", isDFMEARequired);
			itemRevisionDef.stringProps.put("vf4_DFMEA_doc", dfmea);
			itemRevisionDef.boolProps.put("vf4_have_PFMEA", isPFMEARequired);
			itemRevisionDef.stringProps.put("vf4_PFMEA_doc", pfmea);
			itemRevisionDef.boolProps.put("vf4_have_spec_book", isSpecBookRequired);
			itemRevisionDef.stringProps.put("vf4_spec_doc", specBook);
			itemRevisionDef.boolProps.put("vf4_have_DVP", isDVPRequired);
			itemRevisionDef.stringProps.put("vf4_DVP_doc", dvp);
			// REVIEW GATE
			itemRevisionDef.boolProps.put("vf4_is_DMU_required", isDMU);
			itemRevisionDef.boolProps.put("vf4_gr_system_arch", isSystemArchitectApproval);
			itemRevisionDef.boolProps.put("vf4_gr_module_group", isMGL);
			itemRevisionDef.boolProps.put("vf4_gr_qual_eng", isQualityEngineering);
			itemRevisionDef.boolProps.put("vf4_gr_manu_quality", isManufacturingQuality);
			itemRevisionDef.boolProps.put("vf4_gr_manufacturing", isManufacturing);
			itemRevisionDef.boolProps.put("vf4_is_test_n_vali_review", isTestingValidation);
			itemRevisionDef.boolProps.put("vf4_is_cae_required", isCAE);
			itemRevisionDef.boolProps.put("vf4_is_homo_required", isHomo);
			itemRevisionDef.boolProps.put("vf4_is_aftersale_required", isAFS);
			itemRevisionDef.boolProps.put("vf4_is_functional_safety", isFunctionSafety);
			itemRevisionDef.boolProps.put("vf4_is_cost_eng", isCostEngineering);
			itemRevisionDef.boolProps.put("vf4_gr_direct_pur", isDirectPurchase);
			itemRevisionDef.boolProps.put("vf4_gr_indirect_pur", isINDirectPurchase);
			itemRevisionDef.boolProps.put("vf4_is_supplier_qual_manage", isSupplierQualityManager);
			itemRevisionDef.boolProps.put("vf4_is_HTKO1_required", isHTKO1);
			itemRevisionDef.boolProps.put("vf4_is_HTKO2_required", isHTKO2);
			itemRevisionDef.boolProps.put("vf4_dceo_rnd_approve", isDCEO);
			// INIT COST
			itemRevisionDef.boolProps.put("vf4_is_veh_cost_required", vehicleCostRequired);
			if (vehicleCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_veh_cost_delta", vehicleCost);

			itemRevisionDef.boolProps.put("vf4_is_piece_cost_require", pieceCostRequired);
			if (pieceCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_piece_cost_delta", pieceCost);

			itemRevisionDef.boolProps.put("vf4is_tooling_cost_require", toolingCostRequired);
			if (toolingCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_tooling_cost_delta", toolingCost);

			itemRevisionDef.boolProps.put("vf4_is_edd_cost_Required", eddCostRequired);
			if (eddCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_edd_cost_delta", eddCost);

			itemRevisionDef.boolProps.put("vf4v_is_other_cost_required", otherCostRequired);
			if (otherCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_other_cost", otherCost);

			itemRevisionDef.boolProps.put("vf4_is_sunk_cost_require", sunkCostRequired);
			if (sunkCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_sunk_cost", sunkCost);

			itemRevisionDef.boolProps.put("vf4_is_logistic_cost_requir", logisticCostRequired);
			if (logisticCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_logistic_cost", logisticCost);

			itemRevisionDef.boolProps.put("vf4_is_test_n_vali_required", testingValidationCostRequired);
			if (testingValidationCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_testing_validate_c", testingValidationCost);

			itemRevisionDef.boolProps.put("vf4_is_plant_equipment_cost", plantEquipmentCostRequired);
			if (plantEquipmentCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_plant_equip_cost", plantEquipmentCost);

			itemRevisionDef.boolProps.put("vf4_is_rework_cost_require", reworkCostRequired);
			if (reworkCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_rework_cost", reworkCost);

			itemRevisionDef.boolProps.put("vf4_is_budget_cost_required", budgetCostRequired);
			if (budgetCost != null)
				itemRevisionDef.doubleProps.put("vf4_init_budget_cost", budgetCost);
			// PRE-CAB
			itemRevisionDef.stringArrayProps.put("vf4_market", marketValue.toArray(new String[0]));
			itemRevisionDef.stringProps.put("vf4_lhd_rhd", lhdrhd);
			itemRevisionDef.stringArrayProps.put("vf4_variant", variant);
			itemRevisionDef.stringProps.put("vf4_pre_decision_approval", decisionApproval);
			itemRevisionDef.stringProps.put("vf4_precab_decision_comment", decisionComment);
			if (approvalDate != null)
				itemRevisionDef.dateProps.put("vf4_precab_approval_date", approvalDate);
			// IMPACT
			itemRevisionDef.stringProps.put(VESECRNameDefine.BOMINFO_SUPPLIER, supplier);
			itemRevisionDef.boolProps.put("vf4_is_cop_impact_require", isCopImpactRequired);
			itemRevisionDef.stringArrayProps.put("vf4_cop_impacted_prog_user", copImpact.toArray(new String[0]));
			itemRevisionDef.boolProps.put("vf4_is_leadtime_impact_req", leadTimeImpactRequired);
			if (leadTimeImpact != null)
				itemRevisionDef.doubleProps.put("vf4_leadtime_impact1", leadTimeImpact);
			itemRevisionDef.boolProps.put("vf4_is_build_time_imp_requi", isBuildPhaseTimeRequired);
			itemRevisionDef.stringArrayProps.put("vf4_build_time_impact", new String[] { buildPhaseTimeImpact });
			if (mrpLateTime != null)
				itemRevisionDef.doubleProps.put("vf4_mrp_late_time", mrpLateTime);
			itemRevisionDef.boolProps.put("vf4_is_tooling_impac_req", isToolingImpactRequired);
			itemRevisionDef.doubleProps.put("vf4_tooling_impact", toolingImpact);
			itemRevisionDef.boolProps.put("vf4_is_customer_impact_req", isCustomerImpactRequired);
			itemRevisionDef.stringProps.put("vf4_customer_impact1", customerImpact);
			itemRevisionDef.stringProps.put("vf4_problem", problem);
			itemRevisionDef.stringProps.put("vf4_root_cause", rootCause);
			itemRevisionDef.stringProps.put("vf4_solution", solution);
			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateInput idGen = new CreateInput();
			idGen.boName = "VF4_VinES_ECR_ID";
			idGen.stringProps.put("vf4_vehicle_type", programGroup);
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

	private void addCoordinatedChange(TCComponent parentItem) {
		if (ecrRevList == null || ecrRevList.size() == 0)
			return;
		try {
			parentItem.setRelated("VF4_CoordinatedChange", ecrRevList.toArray(new TCComponent[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkRequired() {
		boolean check = true;
		// SUMMARY
		if (dlg.cbProgramGroup.getText().isEmpty()) {
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
			if (dlg.cbSILNoRequired.getText().contains("Yes") && dlg.txtSILNo.getText().isEmpty()) {
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
			if (dlg.cbCoordinatedChageRequired.getText().contains("Yes") && dlg.lstCoordinatedChage.getItemCount() == 0) {
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
			warningLabel(dlg.lblIsSendDataToSupplier, false);
		}

		if (dlg.cbHaveDMUCheckRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblDMUCheckFile, true);
		} else {
			warningLabel(dlg.lblDMUCheckFile, false);
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

		if (dlg.cbLogisticCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblLogisticCost, true);
		} else {
			if (dlg.cbLogisticCostRequired.getText().contains("Yes") && dlg.txtLogisticCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblLogisticCost, true);
			} else {
				warningLabel(dlg.lblLogisticCost, false);
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

		if (dlg.cbReworkCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblReworkCost, true);
		} else {
			if (dlg.cbReworkCostRequired.getText().contains("Yes") && dlg.txtReworkCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblReworkCost, true);
			} else {
				warningLabel(dlg.lblReworkCost, false);
			}
		}

		if (dlg.cbBudgetCostRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblBudgetCost, true);
		} else {
			if (dlg.cbBudgetCostRequired.getText().contains("Yes") && dlg.txtBudgetCost.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblBudgetCost, true);
			} else {
				warningLabel(dlg.lblBudgetCost, false);
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
			if (dlg.cbBuildPhaseTimeRequired.getText().contains("Yes") && dlg.cbBuildPhaseTime.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblBuildPhaseTime, true);
			} else {
				warningLabel(dlg.lblBuildPhaseTime, false);
			}
		}

		if (dlg.cbToolingImpactRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblToolingImpact, true);
		} else {
			if (dlg.cbToolingImpactRequired.getText().contains("Yes") && dlg.txtToolingImpact.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblToolingImpact, true);
			} else {
				warningLabel(dlg.lblToolingImpact, false);
			}
		}

		if (dlg.cbCustomerImpactRequired.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblCustomerImpact, true);
		} else {
			if (dlg.cbCustomerImpactRequired.getText().contains("Yes") && dlg.txtCustomerImpact.getText().isEmpty()) {
				check = false;
				warningLabel(dlg.lblCustomerImpact, true);
			} else {
				warningLabel(dlg.lblCustomerImpact, false);
			}
		}

		if (dlg.txtProblem.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblProblem, true);
		} else {
			warningLabel(dlg.lblProblem, false);
		}

		if (dlg.txtRootCause.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblRootCause, true);
		} else {
			warningLabel(dlg.lblRootCause, false);
		}

		if (dlg.txtSolution.getText().isEmpty()) {
			check = false;
			warningLabel(dlg.lblSolution, true);
		} else {
			warningLabel(dlg.lblSolution, false);
		}

		return check;
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
		if (linkedList != null) {
			if (linkedList.containsKey(key))
				linkedList.remove(key);
		}
	}

	private void createDataset(TCComponent itemRev) {
		String filePath = dlg.txtDMUCheckFile.getText();
		String datasetType = "MSExcelX";

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

	private void updateCheckbox(Button ckb, Boolean check) {
		if (check == null) {
			ckb.setEnabled(true);
			ckb.setSelection(false);
		} else {
			ckb.setEnabled(false);
			ckb.setSelection(check);
		}
	}
}
