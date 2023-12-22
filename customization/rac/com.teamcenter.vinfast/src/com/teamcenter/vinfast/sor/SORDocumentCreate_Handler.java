package com.teamcenter.vinfast.sor;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchGroupMember_Dialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SORDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	private SORDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	private String vehicleCategory = "AUTOMOBILE";
	private String prefixName = "VFSR";
	private static String OBJECT_TYPE = "VF4_SOR_doc";
	private TCComponentGroupMember clUSer;
	private TCComponentGroupMember buyerUser;
	private String PURCHASE_GROUP = "3_IN_1_PDU_DCDC_OBC.Direct Pur.Purchase.VINFAST;BATTERY_PACK.Direct Pur.Purchase.VINFAST;BIW-DC.Direct Pur.Purchase.VINFAST;EE.Direct Pur.Purchase.VINFAST;EXT-AFS.Direct Pur.Purchase.VINFAST;INT.Direct Pur.Purchase.VINFAST;PWT-CHS.Direct Pur.Purchase.VINFAST;SEATS.Direct Pur.Purchase.VINFAST;SEATS_AND_RESTRAINTS.Direct Pur.Purchase.VINFAST;STANDARD.Direct Pur.Purchase.VINFAST";

	public SORDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			// ---------------------------- Init Data -------------------------------------
			LinkedHashMap<String, String> marketDataForm = TCExtension.GetLovValueAndDisplay("vf6cp_market", "Vf6_ECRRevision", session);
			clUSer = null;
			buyerUser = null;
			// ---------------------------- Init UI -------------------------------------
			dlg = new SORDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			// -----------------------------------------------------------------------
			StringExtension.UpdateValueTextCombobox(dlg.cbMarket, marketDataForm);
			dlg.cbModelCode.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					if (!dlg.cbModelCode.getText().isEmpty())
						updateCombobox(3);
				}
			});

			dlg.cbModuleName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					checkComboboxToGenID();
				}
			});

			dlg.btnMarketAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					String market = dlg.cbMarket.getText();
					if (!market.isEmpty()) {
						if (checkExistInList(dlg.lstMarket.getItems(), market))
							dlg.lstMarket.add(market);
					}
				}
			});

			dlg.btnMarketRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					int indexMarket = dlg.lstMarket.getSelectionIndex();
					if (indexMarket >= 0)
						dlg.lstMarket.remove(indexMarket);
				}
			});

			dlg.btnSelectReviewer.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
//					SearchUser_Dialog searchDlg = new SearchUser_Dialog(dlg.getShell());
//					searchDlg.open();
//					Button ok = searchDlg.getOKButton();
//
//					ok.addListener(SWT.Selection, new Listener() {
//						public void handleEvent(Event e) {
//							dlg.txtReviewerName.setText("");
//
//							int index = searchDlg.tblSearch.getSelectionIndex();
//							if (searchDlg.itemSearch.get(index) instanceof TCComponentUser) {
//								TCComponentUser reviewerUSer = (TCComponentUser) searchDlg.itemSearch.get(index);
//
//								try {
//									TCComponent user = TCExtension.getPersonFromUserID(reviewerUSer.getUserId(), session);
//									dlg.txtReviewerName.setText(user.getPropertyDisplayableValue("user_name"));
//								} catch (Exception e2) {
//									e2.toString();
//								}
//							}
//
//							searchDlg.getShell().dispose();
//						}
//					});
				}
			});

			dlg.btnSelectApprover.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
//					SearchUser_Dialog searchDlg = new SearchUser_Dialog(dlg.getShell());
//					searchDlg.open();
//					Button ok = searchDlg.getOKButton();
//
//					ok.addListener(SWT.Selection, new Listener() {
//						public void handleEvent(Event e) {
//							dlg.txtApproverName.setText("");
//
//							int index = searchDlg.tblSearch.getSelectionIndex();
//							if (searchDlg.itemSearch.get(index) instanceof TCComponentUser) {
//								TCComponentUser approverUSer = (TCComponentUser) searchDlg.itemSearch.get(index);
//
//								try {
//									TCComponent user = TCExtension.getPersonFromUserID(approverUSer.getUserId(), session);
//									dlg.txtApproverName.setText(user.getPropertyDisplayableValue("user_name"));
//								} catch (Exception e2) {
//									e2.toString();
//								}
//							}
//
//							searchDlg.getShell().dispose();
//						}
//					});
				}
			});

			dlg.btnSelectCL.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), PURCHASE_GROUP, "CL", false);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							dlg.txtPurchasingCommodityManagerName.setText("");
							dlg.txtPurchasingCommodityManagerEmail.setText("");
							dlg.txtPurchasingCommodityManagerPhone.setText("");

							int index = searchDlg.tblSearch.getSelectionIndex();
							if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
								clUSer = (TCComponentGroupMember) searchDlg.itemSearch.get(index);

								try {
									TCComponent user = TCExtension.getPersonFromUserID(clUSer.getUserId(), session);
									dlg.txtPurchasingCommodityManagerName.setText(user.getPropertyDisplayableValue("user_name"));
									dlg.txtPurchasingCommodityManagerEmail.setText(user.getPropertyDisplayableValue("PA9"));
									dlg.txtPurchasingCommodityManagerPhone.setText(user.getPropertyDisplayableValue("PA10"));
								} catch (Exception e2) {
									e2.toString();
								}
							}

							searchDlg.getShell().dispose();

							dlg.txtCommodityBuyerName.setText("");
							dlg.txtCommodityBuyerEmail.setText("");
							dlg.txtCommodityBuyerPhone.setText("");
							buyerUser = null;
						}
					});
				}
			});

			dlg.btnSelectBuyer.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (clUSer == null) {
						MessageBox.post("Please select Purchasing Commodity Manager first.", "", MessageBox.WARNING);
						return;
					}

					String group = "";
					try {
						group = clUSer.getGroup().toString();
					} catch (Exception e) {
						e.printStackTrace();
					}

					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), group.isEmpty() ? PURCHASE_GROUP : group, "Buyer", false);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							dlg.txtCommodityBuyerName.setText("");
							dlg.txtCommodityBuyerEmail.setText("");
							dlg.txtCommodityBuyerPhone.setText("");

							int index = searchDlg.tblSearch.getSelectionIndex();
							if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
								buyerUser = (TCComponentGroupMember) searchDlg.itemSearch.get(index);

								try {
									TCComponent person = TCExtension.getPersonFromUserID(buyerUser.getUserId(), session);
									dlg.txtCommodityBuyerName.setText(person.getPropertyDisplayableValue("user_name"));
									dlg.txtCommodityBuyerEmail.setText(person.getProperty("PA9"));
									dlg.txtCommodityBuyerPhone.setText(person.getProperty("PA10"));
								} catch (Exception e2) {
									e2.toString();
								}
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			// -----------------------------------------------------------------------
			updateCombobox(2);
//			testcase();
			dlg.txtDataManagement.setText("- ECR system must be used for change management and part history.\n- General Tolerances, CAD and CAE methods as per Vinfast Standards (VFDST)");
			dlg.txtCompliance.setText("- Prohibited or Restricted Substances, Emissions & Toxicology Recycling as per VFDST00007601/02/04 / VFDST00002900\n- Intellectual Property / Copyright / Insurance / Warranties as per Vinfast General Term and Condition (GTC)");
			dlg.txtRasic.setText("RASIC & Deliverables as per VDS - Vinfast Development System");
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}

		return null;
	}

	private void testcase() {
		if (!TCExtension.checkPermission(new String[] { "dba" }, session))
			return;

		dlg.lstMarket.setItems("ASEAN");
		dlg.txtProductionVolume.setText("txtProductionVolume");
		dlg.txtBuildPlan.setText("txtBuildPlan");
		dlg.txtDataManagement.setText("txtDataManagement");
		dlg.txtCompliance.setText("txtCompliance");
		dlg.txtRasic.setText("txtRasic");

		dlg.txtCommodityEngineerName.setText("txtCommodityEngineerName");
		dlg.txtCommodityEngineerEmail.setText("txtCommodityEngineerEmail");
		dlg.txtCommodityEngineerPhone.setText("txtCommodityEngineerPhone");

		dlg.txtCommodityEngineerManagerName.setText("txtCommodityEngineerManagerName");
		dlg.txtCommodityEngineerManagerEmail.setText("txtCommodityEngineerManagerEmail");
		dlg.txtCommodityEngineerManagerPhone.setText("txtCommodityEngineerManagerPhone");

		dlg.txtSQESiteEngineerName.setText("txtSQESiteEngineerName");
		dlg.txtSQESiteEngineerEmail.setText("txtSQESiteEngineerEmail");
		dlg.txtSQESiteEngineerPhone.setText("txtSQESiteEngineerPhone");

		dlg.txtSQEManagerName.setText("txtSQEManagerName");
		dlg.txtSQEManagerEmail.setText("txtSQEManagerEmail");
		dlg.txtSQEManagerPhone.setText("txtSQEManagerPhone");

		dlg.txtSCMAnalystName.setText("txtSCMAnalystName");
		dlg.txtSCMAnalystEmail.setText("txtSCMAnalystEmail");
		dlg.txtSCMAnalystPhone.setText("txtSCMAnalystPhone");

		dlg.txtPackagingAnalystName.setText("txtPackagingAnalystName");
		dlg.txtPackagingAnalystEmail.setText("txtPackagingAnalystEmail");
		dlg.txtPackagingAnalystPhone.setText("txtPackagingAnalystPhone");

		dlg.txtSCMManagerName.setText("txtSCMManagerName");
		dlg.txtSCMManagerEmail.setText("txtSCMManagerEmail");
		dlg.txtSCMManagerPhone.setText("txtSCMManagerPhone");
	}

	private void updateCombobox(int level) {
		if (level == 2) {
			dlg.cbModelCode.removeAll();
			dlg.cbModuleName.removeAll();
			try {
				LinkedHashMap<String, String> dataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf3_veh_category", OBJECT_TYPE, new String[] { vehicleCategory, prefixName }, session);
				StringExtension.UpdateValueTextCombobox(dlg.cbModelCode, dataForm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (level == 3) {
			dlg.cbModuleName.removeAll();

			String moduleCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			try {
				LinkedHashMap<String, String> dataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf3_veh_category", OBJECT_TYPE, new String[] { vehicleCategory, prefixName, moduleCode }, session);
				StringExtension.UpdateValueTextCombobox(dlg.cbModuleName, dataForm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		checkComboboxToGenID();
	}

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		try {
			DataManagementService dms = DataManagementService.getService(session);
			String id = dlg.txtID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());
			String reviewerName = dlg.txtReviewerName.getText();
			String approverName = dlg.txtApproverName.getText();
			Calendar changeDescDate = StringExtension.getDatetimeFromWidget(dlg.datChangeDescDate);
			String[] marketDisplay = dlg.lstMarket.getItems();
			Set<String> marketValue = new HashSet<String>();
			if (marketDisplay != null && marketDisplay.length > 0) {
				for (String str : marketDisplay) {
					marketValue.add((String) dlg.cbMarket.getData(str));
				}
			}
			String prodVolume = dlg.txtProductionVolume.getText();
			String buildSchedule = dlg.txtBuildPlan.getText();
			String dataManagement = dlg.txtDataManagement.getText();
			String compliance = dlg.txtCompliance.getText();
			String rasic = dlg.txtRasic.getText();
			String comBuyerName = dlg.txtCommodityBuyerName.getText();
			String comBuyerEmail = dlg.txtCommodityBuyerEmail.getText();
			String comBuyerPhone = dlg.txtCommodityBuyerPhone.getText();
			String purComManagerName = dlg.txtPurchasingCommodityManagerName.getText();
			String purComManagerEmail = dlg.txtPurchasingCommodityManagerEmail.getText();
			String purComManagerPhone = dlg.txtPurchasingCommodityManagerPhone.getText();
			String comEngineerName = dlg.txtCommodityEngineerName.getText();
			String comEngineerEmail = dlg.txtCommodityEngineerEmail.getText();
			String comEngineerPhone = dlg.txtCommodityEngineerPhone.getText();
			String comEngineerManagerName = dlg.txtCommodityEngineerManagerName.getText();
			String comEngineerManagerEmail = dlg.txtCommodityEngineerManagerEmail.getText();
			String comEngineerManagerPhone = dlg.txtCommodityEngineerManagerPhone.getText();
			String sqeSiteEngineerName = dlg.txtSQESiteEngineerName.getText();
			String sqeSiteEngineerEmail = dlg.txtSQESiteEngineerEmail.getText();
			String sqeSiteEngineerPhone = dlg.txtSQESiteEngineerPhone.getText();
			String sqeManagerName = dlg.txtSQEManagerName.getText();
			String sqeManagerEmail = dlg.txtSQEManagerEmail.getText();
			String sqeManagerPhone = dlg.txtSQEManagerPhone.getText();
			String scmAnalystName = dlg.txtSCMAnalystName.getText();
			String scmAnalystEmail = dlg.txtSCMAnalystEmail.getText();
			String scmAnalystPhone = dlg.txtSCMAnalystPhone.getText();
			String packagingAnalystName = dlg.txtPackagingAnalystName.getText();
			String packagingAnalystEmail = dlg.txtPackagingAnalystEmail.getText();
			String packagingAnalystPhone = dlg.txtPackagingAnalystPhone.getText();
			String scmManagerName = dlg.txtSCMManagerName.getText();
			String scmManagerEmail = dlg.txtSCMManagerEmail.getText();
			String scmManagerPhone = dlg.txtSCMManagerPhone.getText();

			boolean isCAERequeried = dlg.ckbIsCAE.getSelection();
			boolean isSaleMarketing = dlg.ckbIsSaleMarketing.getSelection();
			boolean isStylingTrim = dlg.ckbIsStylingColor.getSelection();
			boolean isVehicleArchieture = dlg.ckbIsVehicleArchieture.getSelection();
			boolean isStandardPart = dlg.ckbIsStandardParts.getSelection();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf3_veh_category", vehicleCategory);
			itemDef.data.stringProps.put("vf3_doc_type", prefixName);
			itemDef.data.stringProps.put("vf3_model_code", modelCode);
			itemDef.data.stringProps.put("vf3_module_name", moduleName);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf4_reviewer_name", reviewerName);
			revDef.stringProps.put("vf4_approver_name", approverName);
			revDef.dateProps.put("vf4_change_desription_date", changeDescDate);
			revDef.stringArrayProps.put("vf4_markets", marketValue.toArray(new String[0]));
			revDef.stringProps.put("vf4_production_volume", prodVolume);
			revDef.stringProps.put("vf4_build_plan", buildSchedule);
			revDef.stringProps.put("vf4_Data_management", dataManagement);
			revDef.stringProps.put("vf4_compliance", compliance);
			revDef.stringProps.put("vf4_rasic_and_deliverables", rasic);

			if (buyerUser != null)
				revDef.tagProps.put("vf4_commodity_buyer1", buyerUser);
			revDef.stringProps.put("vf4_commodity_buyer_name", comBuyerName);
			revDef.stringProps.put("vf4_commodit_bbuyer_email", comBuyerEmail);
			revDef.stringProps.put("vf4_commodity_buyer_phone", comBuyerPhone);
			if (clUSer != null)
				revDef.tagProps.put("vf4_pur_commod_manager1", clUSer);
			revDef.stringProps.put("vf4_pur_com_manager_name", purComManagerName);
			revDef.stringProps.put("vf4_pur_com_manager_email", purComManagerEmail);
			revDef.stringProps.put("vf4_pur_com_manager_phone", purComManagerPhone);
			revDef.stringProps.put("vf4_commodity_engineer_name", comEngineerName);
			revDef.stringProps.put("vf4_commodity_eng_email", comEngineerEmail);
			revDef.stringProps.put("vf4_commodity_eng_phone", comEngineerPhone);
			revDef.stringProps.put("vf4_com_eng_manager_name", comEngineerManagerName);
			revDef.stringProps.put("vf4_com_eng_manager_email", comEngineerManagerEmail);
			revDef.stringProps.put("vf4_com_eng_manager_phone", comEngineerManagerPhone);
			revDef.stringProps.put("vf4_sqe_site_engineer_name", sqeSiteEngineerName);
			revDef.stringProps.put("vf4_sqe_site_engineer_email", sqeSiteEngineerEmail);
			revDef.stringProps.put("vf4_sqe_site_engineer_phone", sqeSiteEngineerPhone);
			revDef.stringProps.put("vf4_sqe_manager_name", sqeManagerName);
			revDef.stringProps.put("vf4_sqe_manager_email", sqeManagerEmail);
			revDef.stringProps.put("vf4_sqe_manager_phone", sqeManagerPhone);
			revDef.stringProps.put("vf4_scm_analyst_name", scmAnalystName);
			revDef.stringProps.put("vf4_scm_analyst_email", scmAnalystEmail);
			revDef.stringProps.put("vf4_scm_analyst_phone", scmAnalystPhone);
			revDef.stringProps.put("vf4_packaging_analyst_name", packagingAnalystName);
			revDef.stringProps.put("vf4_packaging_analyst_email", packagingAnalystEmail);
			revDef.stringProps.put("vf4_packaging_analyst_phone", packagingAnalystPhone);
			revDef.stringProps.put("vf4_scm_manager_name", scmManagerName);
			revDef.stringProps.put("vf4_scm_manager_email", scmManagerEmail);
			revDef.stringProps.put("vf4_scm_manager_phone", scmManagerPhone);

			revDef.boolProps.put("vf4_is_req_cae", isCAERequeried);
			revDef.boolProps.put("vf4_is_req_sale_marketing", isSaleMarketing);
			revDef.boolProps.put("vf4_is_req_styling_color", isStylingTrim);
			revDef.boolProps.put("vf4_is_req_vehicle_architec", isVehicleArchieture);
			revDef.boolProps.put("vf4_is_req_standard_part", isStandardPart);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage(TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			TCComponent cfgContext = response.output[0].objects[0];
			Boolean addToFolder = false;
			if (selectedObject != null) {
				String type = selectedObject.getProperty("object_type");
				if (type.compareToIgnoreCase("Folder") == 0) {
					try {
						selectedObject.add("contents", cfgContext);
						addToFolder = true;
						dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder", IMessageProvider.INFORMATION);
						openOnCreate(cfgContext);
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
			}
			if (!addToFolder) {
				try {
					session.getUser().getNewStuffFolder().add("contents", cfgContext);
				} catch (TCException e1) {
					MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
				}
				dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
				openOnCreate(cfgContext);
			}
			resetDialog();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (dlg.cbModelCode.getText().isEmpty() || dlg.cbModuleName.getText().isEmpty())
			return;

		generateNextID((String) dlg.cbModelCode.getData(dlg.cbModelCode.getText()), (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText()));
	}

	private void generateNextID(String modelCode, String moduleName) {
		try {
			String search_Items = "";
			search_Items = prefixName + modelCode + moduleName;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", OBJECT_TYPE + ";VF3_spec_doc");

			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + "0001";
				} else {
					newIDValue = search_Items + "001";
				}
			} else {
				int id = 0;
				String split = "";
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					split = item_search[0].toString().substring(9, 13);
				} else {
					split = item_search[0].toString().substring(10, 13);
				}
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
				} else {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 3);
				}
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.cbModelCode.removeAll();
		dlg.cbModuleName.removeAll();
	}

	private boolean checkRequired() {
		boolean check = true;
		if (dlg.txtID.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblId, true);
		} else {
			setMessage(dlg.lblId, false);
		}

		if (dlg.txtName.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblName, true);
		} else {
			setMessage(dlg.lblName, false);
		}

		if (dlg.cbModelCode.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblModelCode, true);
		} else {
			setMessage(dlg.lblModelCode, false);
		}

		if (dlg.cbModuleName.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblModuleName, true);
		} else {
			setMessage(dlg.lblModuleName, false);
		}

		if (dlg.txtReviewerName.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblReviewerName, true);
		} else {
			setMessage(dlg.lblReviewerName, false);
		}

		if (dlg.txtApproverName.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblApproverName, true);
		} else {
			setMessage(dlg.lblApproverName, false);
		}

		if (dlg.lstMarket.getItemCount() == 0) {
			check = false;
			setMessage(dlg.lblMarket, true);
		} else {
			setMessage(dlg.lblMarket, false);
		}

		if (dlg.txtProductionVolume.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblProductionVolume, true);
		} else {
			setMessage(dlg.lblProductionVolume, false);
		}

		if (dlg.txtBuildPlan.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblBuildPlan, true);
		} else {
			setMessage(dlg.lblBuildPlan, false);
		}

		if (dlg.txtDataManagement.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblDataManagement, true);
		} else {
			setMessage(dlg.lblDataManagement, false);
		}

		if (dlg.txtCompliance.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblCompliance, true);
		} else {
			setMessage(dlg.lblCompliance, false);
		}

		if (dlg.txtRasic.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblRASIC, true);
		} else {
			setMessage(dlg.lblRASIC, false);
		}

		if (buyerUser == null) {
			check = false;
			setMessage(dlg.lblCommodityBuyer, true);
		} else {
			setMessage(dlg.lblCommodityBuyer, false);
		}

		if (clUSer == null) {
			check = false;
			setMessage(dlg.lblPurchasingCommodityManager, true);
		} else {
			setMessage(dlg.lblPurchasingCommodityManager, false);
		}

		if (dlg.txtCommodityEngineerName.getText().isEmpty() || dlg.txtCommodityEngineerEmail.getText().isEmpty() || dlg.txtCommodityEngineerPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblCommodityEngineer, true);
		} else {
			setMessage(dlg.lblCommodityEngineer, false);
		}

		if (dlg.txtCommodityEngineerManagerName.getText().isEmpty() || dlg.txtCommodityEngineerManagerEmail.getText().isEmpty() || dlg.txtCommodityEngineerManagerPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblCommodityEngineerManager, true);
		} else {
			setMessage(dlg.lblCommodityEngineerManager, false);
		}

		if (dlg.txtSQESiteEngineerName.getText().isEmpty() || dlg.txtSQESiteEngineerEmail.getText().isEmpty() || dlg.txtSQESiteEngineerPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblSQESiteEngineer, true);
		} else {
			setMessage(dlg.lblSQESiteEngineer, false);
		}

		if (dlg.txtSQEManagerName.getText().isEmpty() || dlg.txtSQEManagerEmail.getText().isEmpty() || dlg.txtSQEManagerPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblSQEManager, true);
		} else {
			setMessage(dlg.lblSQEManager, false);
		}

		if (dlg.txtSCMAnalystName.getText().isEmpty() || dlg.txtSCMAnalystEmail.getText().isEmpty() || dlg.txtSCMAnalystPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblSCMAnalyst, true);
		} else {
			setMessage(dlg.lblSCMAnalyst, false);
		}

		if (dlg.txtPackagingAnalystName.getText().isEmpty() || dlg.txtPackagingAnalystEmail.getText().isEmpty() || dlg.txtPackagingAnalystPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblPackagingAnalyst, true);
		} else {
			setMessage(dlg.lblPackagingAnalyst, false);
		}

		if (dlg.txtSCMManagerName.getText().isEmpty() || dlg.txtSCMManagerEmail.getText().isEmpty() || dlg.txtSCMManagerPhone.getText().isEmpty()) {
			check = false;
			setMessage(dlg.lblSCMManager, true);
		} else {
			setMessage(dlg.lblSCMManager, false);
		}

		return check;
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

	private void setMessage(Label label, boolean isError) {
		if (isError)
			label.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			label.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}
}
