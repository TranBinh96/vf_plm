package com.teamcenter.vinfast.change;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.subdialog.SearchECNRev_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ECRBomInfoValidate_Handler extends AbstractHandler {
	private TCSession session;
	private ECRBomInfoValidate_Dialog dlg;
	private TCComponent ecrItemRev = null;
	private ECRInputManagement ecrInputManagement;
	private boolean isUploadClicked;
	private String ecrSummaryTemplate = "";
	private LinkedHashMap<String, String> attributeMap;
	private String[] attributeMandatory;
	private LinkedList<TCComponentBOMLine> parentBomlineList;
	private String formIcon = "<img src=\"data:image/png;base64,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\" style=\"position: absolute;margin: auto;top: 0;left: 0;right: 0;bottom: 0;\" width=\"100\" height=\"100\"/>";

	public ECRBomInfoValidate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		try {
			parentBomlineList = new LinkedList<TCComponentBOMLine>();
			attributeMap = TCExtension.getAttributeMap("Vf6_bom_infomation", session);

			dlg = new ECRBomInfoValidate_Dialog(new Shell());
			dlg.create();

			dlg.browser.setText("<html style=\"padding: 0px;border-width: 1px;border-color: black;border-style: solid;\"><body style=\"margin: 0px; text-align: center;\"><div style=\"height: 100%\">" + formIcon + "</div></body></html>");

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
//						handleUploadClicked();
					} catch (Exception ex) {
						MessageBox.post("Some errors occured, please contact your administrator for further instructions with below messages.\n" + ex.getMessage(), "Upload ECR Information failed", MessageBox.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnAddParent.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						addParent();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			});

			dlg.btnRemoveParent.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						removeParent();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			});

			dlg.btnSearchECR.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchECNRev_Dialog searchDlg = new SearchECNRev_Dialog(dlg.getShell(), "Engineering Change Request Revision");
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							ecrItemRev = searchDlg.itemSearch.get(index);

							try {
								dlg.txtECR.setText(ecrItemRev.getPropertyDisplayableValue("object_string"));
							} catch (Exception e2) {
								e2.printStackTrace();
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					validateBomline();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void addParent() throws NotLoadedException {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 0) {
			for (InterfaceAIFComponent itemTarget : targetComp) {
				if (itemTarget instanceof TCComponentBOMLine) {
					TCComponentBOMLine bomlineSelect = (TCComponentBOMLine) itemTarget;
					boolean check = true;
					for (TCComponentBOMLine item : parentBomlineList) {
						if (item == bomlineSelect)
							check = false;
					}
					if (check) {
						parentBomlineList.add(bomlineSelect);
						dlg.lstParentBomline.add(bomlineSelect.getPropertyDisplayableValue("object_string"));
					}
				}
			}
		}
	}

	private void removeParent() throws NotLoadedException {
		int removeIndex = dlg.lstParentBomline.getSelectionIndex();
		try {
			parentBomlineList.remove(removeIndex);
			dlg.lstParentBomline.remove(removeIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void validateBomline() {
		try {
			LinkedHashMap<String, TCComponentBOMLine> bomlineList = new LinkedHashMap<String, TCComponentBOMLine>();
			for (TCComponentBOMLine parentBom : parentBomlineList) {
				LinkedList<TCComponentBOMLine> expandBom = TCExtension.expandAllBOMLines(parentBom, session);
				if (expandBom != null) {
					for (TCComponentBOMLine line : expandBom) {
						String posID = line.getPropertyDisplayableValue("VF3_pos_ID");
						bomlineList.put(posID, line);
					}
				}
			}

			String[] headers = { "vf6_change_type", "vf6_posid", "vf6_part_number", "vf6_revision", "vf6_new_revision", "vf6_original_base_part", "vf6_purchase_level", "vf6_quantity", "vf6_change_description" };
			if (attributeMap != null) {
				StringBuilder bomInformation = new StringBuilder();
				bomInformation.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
				bomInformation.append(StringExtension.htmlTableCss);
				bomInformation.append("<body style=\"margin: 0px;\">");
				bomInformation.append("<table>");
				bomInformation.append("<tr>");
				bomInformation.append("<td>Status</td>");
				for (String entry : headers) {
					bomInformation.append("<td>" + attributeMap.get(entry) + "</td>");
				}
				bomInformation.append("</tr>");

				TCComponent[] bomlines = ecrItemRev.getRelatedComponents("vf6cp_bom_information");
				if (bomlines != null && bomlines.length > 0) {
					for (TCComponent bomline : bomlines) {
						StringBuilder bomlineBuilder = new StringBuilder();
						bomlineBuilder.append("<tr>");
						bomlineBuilder.append("<td>{status}</td>");

						String changeType = bomline.getPropertyDisplayableValue("vf6_change_type");
						String posID = bomline.getPropertyDisplayableValue("vf6_posid");
						String partNumber = bomline.getPropertyDisplayableValue("vf6_part_number");
						String revision = bomline.getPropertyDisplayableValue("vf6_revision");
						String newRevision = bomline.getPropertyDisplayableValue("vf6_new_revision");
						String originalPart = bomline.getPropertyDisplayableValue("vf6_original_base_part");
						String purchaseLevel = bomline.getPropertyDisplayableValue("vf6_purchase_level");
						String quantity = bomline.getPropertyDisplayableValue("vf6_quantity");
						String changeDesc = bomline.getPropertyDisplayableValue("vf6_change_description");

						bomlineBuilder.append(checkValueNull(changeType, true));
						bomlineBuilder.append(checkValueNull(posID, true));

						String statusMes = "";
						if (changeType.isEmpty()) {
							bomlineBuilder.append(checkValueNull(partNumber, false));
							bomlineBuilder.append(checkValueNull(revision, false));
							bomlineBuilder.append(checkValueNull(newRevision, false));
							bomlineBuilder.append(checkValueNull(originalPart, false));
						} else {
							if (changeType.compareToIgnoreCase("NEW") == 0) {
								if (!bomlineList.containsKey(posID)) {
									statusMes = "Not in BOM";
									bomlineBuilder.append(checkValueNull(partNumber, false));
									bomlineBuilder.append(checkValueNull(revision, false));
								} else {
									String bomPartNumber = bomlineList.get(posID).getPropertyDisplayableValue("awb0BomLineItemId");
									String bomRevision = bomlineList.get(posID).getPropertyDisplayableValue("bl_rev_item_revision_id");
									bomlineBuilder.append(compareBomline(partNumber, bomPartNumber));
									bomlineBuilder.append(compareBomline(revision, bomRevision));
								}
								bomlineBuilder.append(checkValueNull(newRevision, false));
								bomlineBuilder.append(checkValueNull(originalPart, false));
							} else if (changeType.compareToIgnoreCase("CHANGE") == 0) {
								if (!bomlineList.containsKey(posID)) {
									statusMes = "Not in BOM";
									bomlineBuilder.append(checkValueNull(partNumber, false));
									bomlineBuilder.append(checkValueNull(revision, false));
									bomlineBuilder.append(checkValueNull(newRevision, false));
								} else {
									String bomPartNumber = bomlineList.get(posID).getPropertyDisplayableValue("awb0BomLineItemId");
									String bomRevision = bomlineList.get(posID).getPropertyDisplayableValue("bl_rev_item_revision_id");
									bomlineBuilder.append(compareBomline(partNumber, bomPartNumber));
									bomlineBuilder.append(checkValueNull(revision, false));
									bomlineBuilder.append(compareBomline(newRevision, bomRevision));
								}
								bomlineBuilder.append(checkValueNull(originalPart, false));
							} else if (changeType.compareToIgnoreCase("SWAP") == 0) {
								if (!bomlineList.containsKey(posID)) {
									statusMes = "Not in BOM";
									bomlineBuilder.append(checkValueNull(partNumber, false));
									bomlineBuilder.append(checkValueNull(revision, false));
									bomlineBuilder.append(checkValueNull(newRevision, false));
									bomlineBuilder.append(checkValueNull(originalPart, false));
								} else {
									String bomPartNumber = bomlineList.get(posID).getPropertyDisplayableValue("awb0BomLineItemId");
									String bomRevision = bomlineList.get(posID).getPropertyDisplayableValue("bl_rev_item_revision_id");
									String bomOriginPart = bomlineList.get(posID).getPropertyDisplayableValue("bl_rev_item_revision_id");
									bomlineBuilder.append(compareBomline(partNumber, bomPartNumber));
									bomlineBuilder.append(compareBomline(revision, bomRevision));
									bomlineBuilder.append(checkValueNull(newRevision, false));
									bomlineBuilder.append(compareBomline(originalPart, bomOriginPart));
								}
							} else if (changeType.compareToIgnoreCase("REMOVE") == 0) {
								bomlineBuilder.append(checkValueNull(partNumber, false));
								bomlineBuilder.append(checkValueNull(revision, false));
								bomlineBuilder.append(checkValueNull(newRevision, false));
								bomlineBuilder.append(checkValueNull(originalPart, false));
								if (bomlineList.containsKey(posID))
									statusMes = "Still in BOM";
							} else {
								bomlineBuilder.append("<td>" + newRevision + "</td>");
								bomlineBuilder.append("<td>" + originalPart + "</td>");
							}
						}

						bomlineBuilder.append(purchaseLevel.isEmpty() ? "<td style=\"background-color: red;\"></td>" : "<td>" + purchaseLevel + "</td>");
						bomlineBuilder.append(quantity.isEmpty() ? "<td style=\"background-color: red;\"></td>" : "<td>" + quantity + "</td>");
						bomlineBuilder.append("<td>" + changeDesc + "</td>");
						bomlineBuilder.append("</tr>");
						if (!statusMes.isEmpty())
							statusMes = "<span class=\"badge bg-danger\">" + statusMes + "</span>";
						bomInformation.append(bomlineBuilder.toString().replace("{status}", statusMes));
					}
				}
				bomInformation.append("</body></table></html>");
				dlg.browser.setText(bomInformation.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String compareBomline(String formValue, String bomValue) {
		if (formValue.compareTo(bomValue) == 0) {
			return "<td>" + formValue + "</td>";
		} else {
			return "<td>" + formValue + " <span class=\"badge bg-danger\">#</span> " + bomValue + "</td>";
		}
	}

	private String checkValueNull(String formValue, boolean checkNull) {
		if (checkNull) {
			if (formValue.isEmpty())
				return "<td>" + "<span class=\"badge bg-danger\">NULL</span>" + "</td>";
		}
		return "<td>" + formValue + "</td>";
	}
}
