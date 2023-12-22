package com.teamcenter.vinfast.car.engineering.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.RawMaterialLinkModel;
import com.vf.utils.TCExtension;

public class RawMaterialCost_Handler extends AbstractHandler {
	private TCSession session;
	private RawMaterialCost_Dialog dlg;
	private LinkedList<TCComponentItemRevision> hPartList = null;
	private LinkedList<RawMaterialLinkModel> rawMaterialList = null;

	private static String[] GROUP_PERMISSION = { "dba" };

	public RawMaterialCost_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + String.join(";", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			// get data
			hPartList = new LinkedList<TCComponentItemRevision>();
			for (InterfaceAIFComponent target : targetComp) {
				if (target instanceof TCComponentItemRevision) {
					hPartList.add((TCComponentItemRevision) target);
				} else if (target instanceof TCComponentBOMLine) {
					hPartList.add(((TCComponentBOMLine) target).getItemRevision());
				}
			}
			getData();

			// Init UI
			dlg = new RawMaterialCost_Dialog(new Shell());
			dlg.create();
			refreshTable();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getData() throws Exception {
		Set<String> costList = new HashSet<String>();
		rawMaterialList = new LinkedList<RawMaterialLinkModel>();
		for (TCComponentItemRevision hPart : hPartList) {
			RawMaterialLinkModel newRawMaterial = new RawMaterialLinkModel();
			newRawMaterial.sethPartNumber(hPart.getPropertyDisplayableValue("item_id"));
			newRawMaterial.sethPName(hPart.getPropertyDisplayableValue("object_name"));
			costList.add(hPart.getPropertyDisplayableValue("item_id"));

			List<TCComponent> mePartList = TCExtension.getReferenced(hPart, "VF4_H_Part_Relation", session, "");
			if (mePartList != null && mePartList.size() > 0) {
				if (mePartList.get(0) instanceof TCComponentItemRevision) {
					TCComponentItemRevision mePartRev = (TCComponentItemRevision) mePartList.get(0);
					TCComponentItem mePartItem = mePartRev.getItem();
					String mePartType = mePartItem.getPropertyDisplayableValue("vf4_me_part_type");
					if (mePartType.compareTo("BLN") == 0) {
						linkToBlank(newRawMaterial, mePartRev);

						List<TCComponent> coilPart = TCExtension.getReferenced(mePartRev, "VF4_blank_part_relation", session, "");
						if (coilPart != null && coilPart.size() > 0) {
							if (coilPart.get(0) instanceof TCComponentItemRevision) {
								TCComponentItemRevision coilItemRev = (TCComponentItemRevision) coilPart.get(0);
								linkToCoil1(newRawMaterial, coilItemRev);
							}
						}
					} else if (mePartType.compareTo("COI") == 0) {
						linkToCoil1(newRawMaterial, mePartRev);
					}
				}
			}
			rawMaterialList.add(newRawMaterial);
		}

//		LinkedHashMap<String, VFPartCost> cost = TCExtension.extractPartCostInfo(costList.toArray(new String[0]), session);
//		if (cost != null) {
//			for (Map.Entry<String, VFPartCost> costItem : cost.entrySet()) {
//				for (RawMaterialLinkModel rawMaterialItem : rawMaterialList) {
//					if (costItem.getKey().compareToIgnoreCase(rawMaterialItem.getCoilPartNumber()) == 0) {
//						rawMaterialItem.setPartCost(costItem.getValue().partCost);
//					}
//				}
//			}
//		}
	}

	private void linkToBlank(RawMaterialLinkModel newRawMaterial, TCComponentItemRevision mePartRev) throws Exception {
		TCComponentItem blankItem = mePartRev.getItem();
		newRawMaterial.setBlankPartNumber(blankItem.getPropertyDisplayableValue("item_id"));
		newRawMaterial.setBlankPartName(blankItem.getPropertyDisplayableValue("object_name"));
		newRawMaterial.setBlankMaterial(blankItem.getPropertyDisplayableValue("vf4_COI_material"));
		newRawMaterial.setBlankCoating(blankItem.getPropertyDisplayableValue("vf4_coating"));
		newRawMaterial.setBlankThickness(blankItem.getPropertyDisplayableValue("vf4_thickness"));
		newRawMaterial.setBlankWidth(blankItem.getPropertyDisplayableValue("vf4_width"));
		newRawMaterial.setBlankLength(blankItem.getPropertyDisplayableValue("vf4_length"));

		List<TCComponent> coilPart = TCExtension.getReferenced(mePartRev, "VF4_blank_part_relation", session, "");
		if (coilPart != null && coilPart.size() > 0) {
			if (coilPart.get(0) instanceof TCComponentItemRevision) {
				TCComponentItemRevision coilItemRev = (TCComponentItemRevision) coilPart.get(0);
				linkToCoil1(newRawMaterial, coilItemRev);
			}
		}
	}

	private void linkToCoil1(RawMaterialLinkModel newRawMaterial, TCComponentItemRevision mePartRev) throws Exception {
		TCComponentItem coilItem = mePartRev.getItem();
		newRawMaterial.setCoilPartNumber(coilItem.getPropertyDisplayableValue("item_id"));
		newRawMaterial.setCoilPartName(coilItem.getPropertyDisplayableValue("object_name"));
		newRawMaterial.setCoilMaterial(coilItem.getPropertyDisplayableValue("vf4_COI_material"));
		newRawMaterial.setCoilCoating(coilItem.getPropertyDisplayableValue("vf4_coating"));
		newRawMaterial.setCoilThickness(coilItem.getPropertyDisplayableValue("vf4_thickness"));
		newRawMaterial.setCoilWidth(coilItem.getPropertyDisplayableValue("vf4_width"));

		List<TCComponent> coilPart = TCExtension.getReferenced(mePartRev, "VF4_blank_part_relation", session, "");
		if (coilPart != null && coilPart.size() > 0) {
			if (coilPart.get(0) instanceof TCComponentItemRevision) {
				TCComponentItemRevision coilItemRev = (TCComponentItemRevision) coilPart.get(0);
				linkToCoil2(newRawMaterial, coilItemRev);
			}
		}
	}

	private void linkToCoil2(RawMaterialLinkModel newRawMaterial, TCComponentItemRevision mePartRev) throws Exception {
		TCComponentItem coilItem = mePartRev.getItem();
		newRawMaterial.setCoil2PartNumber(coilItem.getPropertyDisplayableValue("item_id"));
		newRawMaterial.setCoil2PartName(coilItem.getPropertyDisplayableValue("object_name"));
		newRawMaterial.setCoil2Material(coilItem.getPropertyDisplayableValue("vf4_COI_material"));
		newRawMaterial.setCoil2Coating(coilItem.getPropertyDisplayableValue("vf4_coating"));
		newRawMaterial.setCoil2Thickness(coilItem.getPropertyDisplayableValue("vf4_thickness"));
		newRawMaterial.setCoil2Width(coilItem.getPropertyDisplayableValue("vf4_width"));
	}

	private void refreshTable() {
		int i = 0;
		for (RawMaterialLinkModel item : rawMaterialList) {
			TableItem tableItem = new TableItem(dlg.tblMaster, SWT.NONE);
			tableItem.setText(new String[] { String.valueOf(++i), item.gethPartNumber(), item.gethPName(), item.getBlankPartNumber(), item.getBlankPartName(), item.getBlankMaterial(), item.getBlankCoating(), item.getBlankThickness(), item.getBlankWidth(), item.getBlankLength(),
					item.getCoilPartNumber(), item.getCoilPartName(), item.getCoilMaterial(), item.getCoilCoating(), item.getCoilThickness(), item.getCoilWidth(), item.getCoil2PartNumber(), item.getCoil2PartName(), item.getCoil2Material(), item.getCoil2Coating(), item.getCoil2Thickness(),
					item.getCoil2Width(), item.getPieceCost(), item.getToolingCost(), item.getEdndCost() });
			tableItem.setChecked(true);
		}
		dlg.lblTotalResult.setText(String.valueOf(i) + " item(s)");
	}
}
