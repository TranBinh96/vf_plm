package com.teamcenter.vinfast.general.update;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.UpdatePartMakeBuyModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class PartMakeBuyUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private PartMakeBuyUpdate_Dialog dlg;
	private List<UpdatePartMakeBuyModel> partList;

	public PartMakeBuyUpdate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

			partList = new LinkedList<UpdatePartMakeBuyModel>();
			for (InterfaceAIFComponent item : targetComp) {
				TCComponentItem newItem = null;

				if (item instanceof TCComponentItem) {
					newItem = (TCComponentItem) item;
				} else if (item instanceof TCComponentItemRevision) {
					newItem = ((TCComponentItemRevision) item).getItem();
				} else if (item instanceof TCComponentBOMLine) {
					newItem = ((TCComponentBOMLine) item).getItem();
				}

				if (newItem != null && !checkPartExist(newItem)) {
					partList.add(new UpdatePartMakeBuyModel(newItem));
				}
			}
			// Init data
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", "VF4_Design", session);
			// Init UI
			dlg = new PartMakeBuyUpdate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Update Part Make/Buy");
			dlg.setMessage("Define business object create information");

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						updatePartMakeBuy();
					} catch (Exception e2) {
						dlg.setMessage(e2.toString(), IMessageProvider.ERROR);
					}
				}
			});
			updateTable();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Boolean checkRequired() {
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		return true;
	}

	private void updateTable1() {
		dlg.tblItem.removeAll();

		int i = 0;
		for (UpdatePartMakeBuyModel item : partList) {
			i++;
			TableItem row = new TableItem(dlg.tblItem, SWT.NONE);
			row.setText(new String[] { String.valueOf(i), item.getPartNumber(), item.getReleaseStatus(),
					item.getPartName(), item.getPartMakeBuy(), item.getTaskStatus() });
			if (item.getTaskStatus().compareToIgnoreCase("Success") == 0)
				row.setBackground(5, SWTResourceManager.getColor(SWT.COLOR_GREEN));
		}

		dlg.tblItem.redraw();
	}
	
	private void updateTable() {
		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<html style=\"padding: 0px;\">");
		htmlText.append(StringExtension.htmlTableCss);
		htmlText.append("<body style=\"margin: 0px;\"><table>");
		LinkedHashMap<String, String> headerList = new LinkedHashMap<String, String>();
		headerList.put("No", "3");
		headerList.put("Part Number", "10");
		headerList.put("Release Status", "10");
		headerList.put("Part Name", "15");
		headerList.put("Make/Buy", "7");
		headerList.put("Status", "55");
		htmlText.append(StringExtension.genTableHeader(headerList));
		if (partList != null && partList.size() > 0) {
			int i = 0;
			for (UpdatePartMakeBuyModel item : partList) {
				i++;
				htmlText.append("<tr><td><p>" + String.valueOf(i) + "</p></td>" +
						"<td><p>" + item.getPartNumber() + "</p></td>" +
						"<td><p>" + item.getReleaseStatus() + "</p></td>" + 
						"<td><p>" + item.getPartName() + "</p></td>" + 
						"<td><p>" + item.getPartMakeBuy() + "</p></td>" +
						(item.getTaskStatus().compareToIgnoreCase("Success") == 0 ? "<td>" + StringExtension.genBadgetSuccess("Success") + "</td>" : "<td><p>" + item.getTaskStatus() + "</p></td>") + "</tr>");
			}
		}

		htmlText.append("</table></body></html>");
		dlg.brwItem.setText(htmlText.toString());
	}

	private void updatePartMakeBuy() throws TCException, NotLoadedException, Exception {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		String partMakeBuy = dlg.cbPartMakeBuy.getText();
		for (UpdatePartMakeBuyModel item : partList) {
			item.setTaskStatus("Success");

			TCComponentItem part = item.getObjectItem();
			String mess = validatePartUpdate(part);
			if (mess.isEmpty()) {
				try {
					part.setStringProperty("vf4_item_make_buy", partMakeBuy);
					item.setPartMakeBuy(partMakeBuy);
					item.setTaskStatus("Success");
				} catch (Exception e) {
					item.setTaskStatus(e.toString());
				}
			} else {
				item.setTaskStatus(mess);
			}
		}

		updateTable();
		dlg.btnCreate.setEnabled(false);
	}

	private boolean checkPartExist(TCComponentItem item) {
		for (UpdatePartMakeBuyModel part : partList) {
			if (part.getObjectItem() == item)
				return true;
		}
		return false;
	}

	private String validatePartUpdate(TCComponentItem part) throws Exception {
		if (part.getPropertyDisplayableValue("vf4_is_transferred_erp").toLowerCase().equals("true")) {
			return "Part transferred to SAP system. To change the make/buy value, please trigger ECR process. <br>Then, provide ECR approval number for SAP support at supportsap@vingroup.net and Teamcenter support at itservicedesk@vinfast.vn.";
		}

		if (!TCExtension.checkPermissionAccess(part, "WRITE", session)) {
			return "You don't have WRITE access for this part. To change the make/buy value please send email to itservicedesk@vinfast.vn.";
		}

		return "";
	}
}
