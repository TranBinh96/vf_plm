package com.teamcenter.vinfast.admin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.SyncCostImpactModel;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class SyncCostImpactToST_Handler extends AbstractHandler {
	private TCSession session;
	private SyncCostImpactToST_Dialog dlg;
	private static LinkedList<String> partHasCost;
	private InterfaceAIFComponent[] targetComp;
	private int totalSelect = 0;
	private LinkedList<SyncCostImpactModel> partValidList;
	private DateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
	
	private static String GROUP_PERMISSION = "dba";

	public SyncCostImpactToST_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		if(!CheckPermission()) {
			MessageBox.post("You are not authorized to Sync Cost Impact to ST.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
			return null;
		}
		
		try {
			targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp != null) {
				totalSelect = targetComp.length;
			}
			partValidList = new LinkedList<SyncCostImpactModel>();

			// Init data

			// Init UI
			dlg = new SyncCostImpactToST_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Sync Cost Impact To ST");
			dlg.setMessage(totalSelect + " item(s) selected", IMessageProvider.INFORMATION);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						sycnCostImpact();
					} catch (Exception e2) {
						dlg.setMessage(e2.toString(), IMessageProvider.ERROR);
					}
				}
			});
			updateTable(true);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void sycnCostImpact() {
		for (InterfaceAIFComponent item : targetComp) {
			LinkedList<SyncCostImpactModel> successList = new LinkedList<SyncCostImpactModel>();
			LinkedList<SyncCostImpactModel> unsuccessList = new LinkedList<SyncCostImpactModel>();
			String ecrNumber = "";
			try {
				String error = validationECR(item);
				if (error.isEmpty()) {
					partHasCost = new LinkedList<String>();
					TCComponentItemRevision ecr = (TCComponentItemRevision) item;
					TCComponent[] purchasingCostForms = getRelatedObjects(ecr, new String[] { "Vf6_purchasing" },
							"Vf6_change_forms");
					TCComponent[] manufacturingCostForms = getRelatedObjects(ecr, new String[] { "Vf6_manufacturing" },
							"Vf6_change_forms");

					Map<String, TCComponent> partIdsAndPurchasingForms = preparePartIdsAndCostFormsMap(
							purchasingCostForms);
					Map<String, TCComponent> partIdsAndManufacturingForms = preparePartIdsAndCostFormsMap(
							manufacturingCostForms);
					Map<String, SyncCostImpactModel> partIdsAndEcrStCosts = preparePartIdsAndSyncCostImpactModelMap(
							partIdsAndPurchasingForms, partIdsAndManufacturingForms);

					for (Map.Entry<String, SyncCostImpactModel> entry : partIdsAndEcrStCosts.entrySet()) {
						String partId = entry.getKey();
						String revNum = "";
						String idNum = "";
						if (partId.contains("/")) {
							idNum = partId.split("/")[0];
							revNum = partId.split("/")[1];
						}
						SyncCostImpactModel ecrCost = entry.getValue();
						ecrNumber = ecr.getPropertyDisplayableValue("item_id");
						ecrCost.setEcrNumber(ecrNumber);
						try {
							Date dateRelease = format.parse(ecr.getPropertyDisplayableValue("date_released"));
							ecrCost.setEcrRelDate(dateRelease);
						} catch (Exception e) {
						}

						ecrCost.setPartNumber(idNum);
						ecrCost.setRevisionNumber(revNum);
						partValidList.add(ecrCost);
						updateVF4_ST_ECRLatestCost(ecrCost);
						if (ecrCost.getTaskStatus().compareToIgnoreCase("Success") == 0)
							successList.add(ecrCost);
						else
							unsuccessList.add(ecrCost);
					}
				}
				if (successList.size() > 0)
					createDataset(ecrNumber, successList, "success");
				if (unsuccessList.size() > 0)
					createDataset(ecrNumber, unsuccessList, "unsuccess");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		updateTable(false);
		dlg.btnCreate.setEnabled(false);
	}

	private void updateTable(boolean first) {
		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<html style=\"border: 1px solid black; padding: 0px;\">");
		htmlText.append(
				"<style> table, tr, td {border: 1px solid;}table {width: 100%;border-collapse: collapse;table-layout: fixed;}td {height: 20px;word-wrap:break-word;}</style>");
		htmlText.append("<body style=\"margin: 0px;\">");
		htmlText.append("<table>");
		htmlText.append("<tr style=\"background-color: #1E9FF2; color: white; height: 30px; text-align: center;\">");
		htmlText.append("<td style=\"width: 15%;\">Part Number</td>");
		htmlText.append("<td style=\"width: 10%;\">Rev</td>");
		htmlText.append("<td style=\"width: 15%;\">ECR Number</td>");
		htmlText.append("<td style=\"width: 20%;\">ECR Release Date</td>");
		htmlText.append("<td style=\"width: 40%;\">Status</td>");
		htmlText.append("</tr>");
		//
		if (first) {
			htmlText.append("<tr>");
			htmlText.append("<td></td>");
			htmlText.append("<td></td>");
			htmlText.append("<td></td>");
			htmlText.append("<td></td>");
			htmlText.append("<td></td>");
			htmlText.append("</tr>");
		} else {
			for (SyncCostImpactModel syncCostItem : partValidList) {
				htmlText.append("<tr>");
				htmlText.append("<td>" + syncCostItem.getPartNumber() + "</td>");
				htmlText.append("<td>" + syncCostItem.getRevisionNumber() + "</td>");
				htmlText.append("<td>" + syncCostItem.getEcrNumber() + "</td>");
				htmlText.append("<td>" + syncCostItem.getEcrRelDate() + "</td>");
				htmlText.append("<td>" + syncCostItem.getTaskStatus() + "</td>");
				htmlText.append("</tr>");
			}
		}
		//
		htmlText.append("</table></body></html>");

		dlg.browser.setText(htmlText.toString());
	}

	private String validationECR(InterfaceAIFComponent item) throws NotLoadedException, TCException {
		if (item instanceof TCComponentItemRevision) {
			TCComponentItemRevision ecr = (TCComponentItemRevision) item;
			if (ecr.getType().compareToIgnoreCase("Vf6_ECRRevision") == 0) {
				// check approval
				String ecrStatus = ecr.getPropertyDisplayableValue("release_status_list");
				if (!ecrStatus.contains("Approved")) {
					return "ECR not have Approved status";
				}

				TCComponent[] partRevList = getRelatedObjects(ecr,
						new String[] { "VF3_manuf_partRevision", "VF4_DesignRevision", "VF4_BP_DesignRevision" },
						"Cm0HasProposal");
				if (partRevList == null || partRevList.length == 0) {
					return "NO Proposal item exists";
				}
			} else {
				return "Not an ECR";
			}
		} else {
			return "Not an ECR";
		}

		return "";
	}

	//
	private Map<String, TCComponent> preparePartIdsAndCostFormsMap(TCComponent[] costForms) throws NotLoadedException {
		Map<String, TCComponent> partIdsAndCostForms = new HashMap<String, TCComponent>();

		for (TCComponent costForm : costForms) {
			String formName = costForm.getPropertyDisplayableValue("object_name");
			// Purchasing Cost - QQQ65432174/01
			if (formName.contains(" - ") && formName.contains("/")) {
				String partId = formName.split(" - ")[1];
				if (!partHasCost.contains(partId)) {
					partHasCost.add(partId.trim());
				}
				partIdsAndCostForms.put(partId, costForm);
			} else {
//				LOGGER.info("[preparePartIdsAndCostFormsMap] - Invalid form name: " + formName);
			}
		}
		return partIdsAndCostForms;
	}

	private TCComponent[] getRelatedObjects(TCComponent primaryObj, String[] secondaryObjTypes, String relationName) {
		try {
			TCComponent[] partRevList = primaryObj.getRelatedComponents(relationName);
			if (partRevList != null && partRevList.length > 0) {
				List<TCComponent> outputList = new LinkedList<TCComponent>();
				for (TCComponent item : partRevList) {
					if (secondaryObjTypes.length > 0) {
						boolean check = false;
						for (String objectType : secondaryObjTypes) {
							if (item.getType().compareToIgnoreCase(objectType) == 0) {
								check = true;
								break;
							}
						}
						if (check) {
							outputList.add(item);
						}
					} else {
						outputList.add(item);
					}
				}
				return outputList.toArray(new TCComponent[outputList.size()]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private Map<String, SyncCostImpactModel> preparePartIdsAndSyncCostImpactModelMap(
			Map<String, TCComponent> partIdsAndPurchasingForms, Map<String, TCComponent> partIdsAndManufacturingForms)
			throws NotLoadedException {

		Map<String, SyncCostImpactModel> partIdsAndVFSyncCostImpactModels = new HashMap<String, SyncCostImpactModel>();

		for (String partID : partHasCost) {
			TCComponent purchasingForm = null;
			TCComponent manufacturingForm = null;
			SyncCostImpactModel purchasingCost = null;
			SyncCostImpactModel manufacturingCost = null;

			if (partIdsAndPurchasingForms.containsKey(partID)) {
				purchasingForm = partIdsAndPurchasingForms.get(partID);
			}

			if (purchasingForm != null) {
				purchasingCost = getPurchasingCost(purchasingForm);
			} else {
				purchasingCost = new SyncCostImpactModel(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
			}

			if (partIdsAndManufacturingForms.containsKey(partID)) {
				manufacturingForm = partIdsAndManufacturingForms.get(partID);
			}
			if (manufacturingForm != null) {
				manufacturingCost = getManufacturingCost(manufacturingForm);
			} else {
				manufacturingCost = new SyncCostImpactModel(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
			}

			SyncCostImpactModel VFSyncCostImpactModel = plusStEcrCost(purchasingCost, manufacturingCost);
			partIdsAndVFSyncCostImpactModels.put(partID, VFSyncCostImpactModel);
		}

		return partIdsAndVFSyncCostImpactModels;
	}

	private SyncCostImpactModel getPurchasingCost(TCComponent purchasingForm) throws NotLoadedException {
		String packingCostStr = purchasingForm.getPropertyDisplayableValue("vf6_pur_packing_cost");
		String logisticCostStr = purchasingForm.getPropertyDisplayableValue("vf6_pur_logistic_cost");
		String edndCostStr = purchasingForm.getPropertyDisplayableValue("vf6_supplier_eng_costs");
		String toolingCostStr = purchasingForm.getPropertyDisplayableValue("vf6_tooling_costs");
		String pieceCostStr = purchasingForm.getPropertyDisplayableValue("vf6_material_costs");

		Double packing = packingCostStr.isEmpty() ? Double.NaN : Double.valueOf(packingCostStr);
		Double logistic = logisticCostStr.isEmpty() ? Double.NaN : Double.valueOf(logisticCostStr);
		Double ednd = edndCostStr.isEmpty() ? Double.NaN : Double.valueOf(edndCostStr);
		Double tooling = toolingCostStr.isEmpty() ? Double.NaN : Double.valueOf(toolingCostStr);
		Double piece = pieceCostStr.isEmpty() ? Double.NaN : Double.valueOf(pieceCostStr);

		SyncCostImpactModel VFSyncCostImpactModel = new SyncCostImpactModel(ednd, tooling, piece, logistic, packing);

		return VFSyncCostImpactModel;
	}

	private SyncCostImpactModel getManufacturingCost(ModelObject manufacturing) throws NotLoadedException {
		String packingCostStr = manufacturing.getPropertyDisplayableValue("vf6_manuf_packing_cost");
		String logisticCostStr = manufacturing.getPropertyDisplayableValue("vf6_manuf_logistic_cost");
		String edndCostStr = manufacturing.getPropertyDisplayableValue("vf6_manu_supplier_eng_costs");
		String toolingCostStr = manufacturing.getPropertyDisplayableValue("vf6_manu_tooling_costs");
		String pieceCostStr = manufacturing.getPropertyDisplayableValue("vf6_manu_piece_costs");

		Double packing = packingCostStr.isEmpty() ? Double.NaN : Double.valueOf(packingCostStr);
		Double logistic = logisticCostStr.isEmpty() ? Double.NaN : Double.valueOf(logisticCostStr);
		Double ednd = edndCostStr.isEmpty() ? Double.NaN : Double.valueOf(edndCostStr);
		Double tooling = toolingCostStr.isEmpty() ? Double.NaN : Double.valueOf(toolingCostStr);
		Double piece = pieceCostStr.isEmpty() ? Double.NaN : Double.valueOf(pieceCostStr);

		SyncCostImpactModel VFSyncCostImpactModel = new SyncCostImpactModel(ednd, tooling, piece, logistic, packing);

		return VFSyncCostImpactModel;
	}

	private SyncCostImpactModel plusStEcrCost(SyncCostImpactModel purchasingCost,
			SyncCostImpactModel manufacturingCost) {
		Double packing = Double.NaN;
		Double logistic = Double.NaN;
		Double tooling = Double.NaN;
		Double ednd = Double.NaN;
		Double piece = Double.NaN;

		SyncCostImpactModel stEcrCost = new SyncCostImpactModel(ednd, tooling, piece, logistic, packing);

		if (manufacturingCost.hasCost() && purchasingCost.hasCost()) {
			SyncCostImpactModel manufacturingCostWithoutNaN = removeNaN(manufacturingCost);
			SyncCostImpactModel purchasingCostWithoutNaN = removeNaN(purchasingCost);

			if (manufacturingCost.getEdnd().isNaN() == false || purchasingCost.getEdnd().isNaN() == false)
				ednd = manufacturingCostWithoutNaN.getEdnd() + purchasingCostWithoutNaN.getEdnd();
			if (manufacturingCost.getLogistic().isNaN() == false || purchasingCost.getLogistic().isNaN() == false)
				logistic = manufacturingCostWithoutNaN.getLogistic() + purchasingCostWithoutNaN.getLogistic();
			if (manufacturingCost.getTooling().isNaN() == false || purchasingCost.getTooling().isNaN() == false)
				tooling = manufacturingCostWithoutNaN.getTooling() + purchasingCostWithoutNaN.getTooling();
			if (manufacturingCost.getPacking().isNaN() == false || purchasingCost.getPacking().isNaN() == false)
				packing = manufacturingCostWithoutNaN.getPacking() + purchasingCostWithoutNaN.getPacking();
			if (manufacturingCost.getPiece().isNaN() == false || purchasingCost.getPiece().isNaN() == false)
				piece = manufacturingCostWithoutNaN.getPiece() + purchasingCostWithoutNaN.getPiece();

			stEcrCost = new SyncCostImpactModel(ednd, tooling, piece, logistic, packing);
		} else if (manufacturingCost.hasCost()) {
			stEcrCost = manufacturingCost;
		} else if (purchasingCost.hasCost()) {
			stEcrCost = purchasingCost;
		}

		return stEcrCost;
	}

	private SyncCostImpactModel removeNaN(SyncCostImpactModel formCost) {
		Double packing = formCost.getPacking().isNaN() ? 0 : formCost.getPacking().doubleValue();
		Double logistic = formCost.getLogistic().isNaN() ? 0 : formCost.getLogistic().doubleValue();
		Double tooling = formCost.getTooling().isNaN() ? 0 : formCost.getTooling().doubleValue();
		Double ednd = formCost.getEdnd().isNaN() ? 0 : formCost.getEdnd().doubleValue();
		Double piece = formCost.getPiece().isNaN() ? 0 : formCost.getPiece().doubleValue();

		SyncCostImpactModel cost = new SyncCostImpactModel(ednd, tooling, piece, logistic, packing);
		return cost;
	}

	private void updateVF4_ST_ECRLatestCost(SyncCostImpactModel po) {
		String partNumber = po.getPartNumber();
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
		queryInput.put("VF Part Number", partNumber);
		try {
			TCComponent[] stParts = Query.queryItem(session, queryInput, "Source Part");
			if (stParts != null && stParts.length > 0) {
				Map<String, List<TCComponent>> partIdsAndEcrStForms = preparePartIdsAndStEcrFormsMap(po, stParts);

				if (partIdsAndEcrStForms.values().size() <= 0) {
					po.setTaskStatus("Form VF4_ST_ECRLATESTCOST not found");
					return;
				}

				for (Entry<String, List<TCComponent>> partIdAndEcrStForms : partIdsAndEcrStForms.entrySet()) {
					List<TCComponent> stEcrCostForms = partIdAndEcrStForms.getValue();

					String errorLog = executeUpdate(po, stEcrCostForms);
					if (!errorLog.isEmpty()) {
						po.setTaskStatus(errorLog);
						return;
					}
				}
				po.setTaskStatus("Success");
			} else {
				po.setTaskStatus("ST not found");
			}
		} catch (Exception e) {
			po.setTaskStatus(e.toString());
		}
	}

	private Map<String, List<TCComponent>> preparePartIdsAndStEcrFormsMap(SyncCostImpactModel po, TCComponent[] mo)
			throws Exception {
		Map<String, List<TCComponent>> partIdsAndSourceParts = new HashMap<String, List<TCComponent>>();
		Date ecrRelDate = po.getEcrRelDate();
		for (int i = 0; i < mo.length; i++) {
			String partId = mo[i].getPropertyDisplayableValue("vf4_bom_vfPartNumber");
			
			TCComponent[] stEcrFormsTmp = getRelatedObjects(mo[i], new String[] { "VF4_ST_ECRLatestCost" }, "VF4_ST_ECRLatestCost_Rela");
			if (stEcrFormsTmp != null && stEcrFormsTmp.length > 0) {
				TCComponent stEcrForm = stEcrFormsTmp[0];
				List<TCComponent> listEcrStForm = partIdsAndSourceParts.get(partId);
				if (listEcrStForm == null)
					listEcrStForm = new LinkedList<TCComponent>();
				listEcrStForm.add(stEcrForm);
				partIdsAndSourceParts.put(partId, listEcrStForm);
			} else {
//				LOGGER.error("[preparePartIdsAndStEcrFormsMap] Not found VF4_ST_ECRLatestCost form: " + partId + "|uid: " + mo[i].getUid());
			}
		}
		return partIdsAndSourceParts;
	}

	private String executeUpdate(SyncCostImpactModel cost, List<TCComponent> stEcrCostForms) throws NotLoadedException {
		Map<String, VecStruct> nameValMap = new HashMap<String, VecStruct>();
		String err = "";
		if (cost.hasCost()) {
			if (cost.getLogistic() != null)
				putPropNameValVec(nameValMap, "vf4_logistics_costs", Property.toDoubleString(cost.getLogistic().doubleValue()));

			if (cost.getEdnd() != null)
				putPropNameValVec(nameValMap, "vf4_ednd_costs", Property.toDoubleString(cost.getEdnd().doubleValue()));

			if (cost.getPacking() != null)
				putPropNameValVec(nameValMap, "vf4_packing_costs", Property.toDoubleString(cost.getPacking().doubleValue()));

			if (cost.getPiece() != null)
				putPropNameValVec(nameValMap, "vf4_piece_costs", Property.toDoubleString(cost.getPiece().doubleValue()));

			if (cost.getTooling() != null)
				putPropNameValVec(nameValMap, "vf4_tooling_costs", Property.toDoubleString(cost.getTooling().doubleValue()));

			putPropNameValVec(nameValMap, "vf4_ecr_latest_rel_date", Property.toDateString(cost.getEcrRelDate()));
			putPropNameValVec(nameValMap, "vf4_ecr_num_latest_rel", cost.getEcrNumber());

			DataManagementService dms = DataManagementService.getService(session);
			ServiceData serviceData = dms.setProperties(stEcrCostForms.toArray(new TCComponent[stEcrCostForms.size()]), nameValMap);
			if (serviceData.sizeOfPartialErrors() > 0) {
				String error = "Exception: ";
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						error += msg + ". ";
					}
				}
			}
			dms.refreshObjects(stEcrCostForms.toArray(new TCComponent[stEcrCostForms.size()]));

			// TODO calculate Total Piece Cost
			for (TCComponent mo : stEcrCostForms) {
				String logisCost = mo.getPropertyDisplayableValue("vf4_logistics_costs");
				String pkgCost = mo.getPropertyDisplayableValue("vf4_packing_costs");
				String pieceCost = mo.getPropertyDisplayableValue("vf4_piece_costs");

				double d_pieceCost = 0;
				double d_logisticsCost = 0;
				double d_supplierPkgAmount = 0;
				try {
					if (!pieceCost.isEmpty()) {
						d_pieceCost = Double.valueOf(pieceCost);
					}
					if (!logisCost.isEmpty()) {
						d_logisticsCost = Double.valueOf(logisCost);
					}
					if (!pkgCost.isEmpty()) {
						d_supplierPkgAmount = Double.valueOf(pkgCost);
					}
				} catch (Exception e) {
//					LOGGER.error("[executeUpdate] Exception: " + e.toString());
				}
				HashMap<String, VecStruct> propertyMap = new HashMap<>();
				String[] propertyDoubleValues = { String.valueOf(d_pieceCost + d_logisticsCost + d_supplierPkgAmount) };
				VecStruct vecStructDouble = new VecStruct();
				vecStructDouble.stringVec = propertyDoubleValues;
				propertyMap.put("vf4_total_piece_cost", vecStructDouble);

				ServiceData serviceData1 = dms.setProperties(new TCComponent[] { mo }, propertyMap);
				if (serviceData1.sizeOfPartialErrors() > 0) {
					String error = "Exception: ";
					for (int i = 0; i < serviceData1.sizeOfPartialErrors(); i++) {
						for (String msg : serviceData1.getPartialError(i).getMessages()) {
							error += msg + ". ";
						}
					}
				}
			}
		}
		return err;
	}

	private void putPropNameValVec(Map<String, VecStruct> nameValMap, String propName, Object val) {
		VecStruct valVec = new VecStruct();
		valVec.stringVec = new String[] { val.toString() };
		nameValMap.put(propName, valVec);
	}

	private void createDataset(String ecrNumber, List<SyncCostImpactModel> dataList, String status) throws TCException, IOException {
		File tempParamFile = new File(System.getenv("tmp") + "\\" + String.valueOf(System.currentTimeMillis()) + ".txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempParamFile));
		StringBuffer header = new StringBuffer();
		header.append("Part Number;Revision Number;ECR Number;Released Date;Piece Costs;Packing Costs;Logistics Costs;Ddd Costs;Tooling Costs;Status");
		writer.write(header.toString() + "\n");
		for (SyncCostImpactModel item : dataList) {
			StringBuffer line = new StringBuffer();
			line.append(item.getPartNumber() + ";" + item.getRevisionNumber() + ";" + item.getEcrNumber() + ";"
					+ item.getEcrRelDate() + ";" + item.getPiece() + ";" + item.getPacking() + ";" + item.getLogistic()
					+ ";" + item.getEdnd() + ";" + item.getTooling() + ";" + item.getTaskStatus());
			writer.write(line.toString() + "\n");
		}
		writer.close();
		String datasetName = ecrNumber + "_" + status + "_" + String.valueOf(System.currentTimeMillis());
		Utils.createDataset(session, datasetName, "Text", "DBA_Created", tempParamFile.getAbsolutePath());
	}
	
	private boolean CheckPermission() {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			if(group.toString().compareToIgnoreCase(GROUP_PERMISSION) == 0) return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
}
