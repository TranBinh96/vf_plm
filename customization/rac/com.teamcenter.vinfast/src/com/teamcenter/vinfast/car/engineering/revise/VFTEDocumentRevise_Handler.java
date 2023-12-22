package com.teamcenter.vinfast.car.engineering.revise;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VFTEDocumentRevise_Handler extends AbstractHandler {
	private TCSession session;
	private VFTEDocumentRevise_Dialog dlg;
	private DataManagementService dmService;
	private TCComponentItemRevision selectedObject = null;
	private String OBJECT_TYPE = "VF3_AT_VFTE_DocRevision";
	private String SPECIFICATION_RELATION_NAME = "IMAN_specification";

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

		if (validObjectSelect(targetComp)) {
			MessageBox.post("Please Select VFTE Document Revision.", "Error", MessageBox.ERROR);
			return null;
		}
		try {

			dlg = new VFTEDocumentRevise_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("VFTE Document Revise");
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			dlg.ckbTargetReviseDate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateTargetReviseDateUI();
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			fillDefaultData();

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void fillDefaultData() {
		Date today = new Date();
		Calendar targetReleaseDate = Calendar.getInstance();
		targetReleaseDate.setTime(today);
		targetReleaseDate.add(Calendar.DAY_OF_YEAR, 45);
		dlg.datTargetReleaseDate.setDate(targetReleaseDate.get(Calendar.YEAR), targetReleaseDate.get(Calendar.MONTH), targetReleaseDate.get(Calendar.DAY_OF_MONTH));
		dlg.datTargetReviseDate.setDate(targetReleaseDate.get(Calendar.YEAR), targetReleaseDate.get(Calendar.MONTH), targetReleaseDate.get(Calendar.DAY_OF_MONTH));
		updateTargetReviseDateUI();
		try {
			String id = selectedObject.getPropertyDisplayableValue("item_id");
			String name = selectedObject.getPropertyDisplayableValue("object_name");
			TCComponentItemRevision latestItemRevision = TCExtension.getLatestItemRevision(selectedObject.getItem());
			String latestRevision = latestItemRevision.getPropertyDisplayableValue("current_revision_id");
			String newRevision = StringExtension.ConvertNumberToString(Integer.parseInt(latestRevision) + 1, 2);

			dlg.txtID.setText(id);
			dlg.txtName.setText(name);
			dlg.txtRevision.setText(newRevision);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		try {
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			Calendar targetReleaseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReleaseDate);
			Calendar targetReviseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReviseDate);

			ReviseInfo reviseInfo = new ReviseInfo();
			reviseInfo.baseItemRevision = selectedObject;
			reviseInfo.description = description;
			reviseInfo.name = name;
			reviseInfo.clientId = selectedObject.getUid();

			ReviseResponse2 response = dmService.revise2(new ReviseInfo[] { reviseInfo });
			Map.Entry<String, ReviseOutput> entry = response.reviseOutputMap.entrySet().iterator().next();
			TCComponentItemRevision newItemRev = entry.getValue().newItemRev;
			if (newItemRev == null) {
				if (response.serviceData.sizeOfPartialErrors() > 0)
					throw new Exception(TCExtension.hanlderServiceData(response.serviceData));
				else
					throw new Exception("");
			}

			newItemRev.setDateProperty("vf4_target_release_date", targetReleaseDate.getTime());
			if (dlg.ckbTargetReviseDate.getSelection())
				newItemRev.setDateProperty("vf4_target_revise_date", targetReviseDate.getTime());

			TCComponent[] files = selectedObject.getRelatedComponents(SPECIFICATION_RELATION_NAME);
			if (files != null) {
				LinkedList<TCComponent> pdfFileList = new LinkedList<TCComponent>();
				for (TCComponent pdfFile : files) {
					if (pdfFile instanceof TCComponentDataset && pdfFile.getPropertyDisplayableValue("object_type").compareTo("PDF") == 0)
						pdfFileList.add(pdfFile);
				}

				if (pdfFileList.size() > 0) {
					TCComponent[] currSpecItems = newItemRev.getRelatedComponents(SPECIFICATION_RELATION_NAME);
					if (currSpecItems != null && currSpecItems.length > 0) {
						newItemRev.remove(SPECIFICATION_RELATION_NAME, currSpecItems);
					}
					newItemRev.add(SPECIFICATION_RELATION_NAME, pdfFileList.toArray(new TCComponent[0]));
				}
			}

			dlg.setMessage("Created successfully.", IMessageProvider.INFORMATION);
			dlg.btnAccept.setEnabled(false);
		} catch (Exception e) {
			dlg.setMessage("Revise unsuccessfull. Exception: " + e.getMessage());
		}
	}

	private Boolean checkRequired() {
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtRevision.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;

		return true;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;

		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentItemRevision)
			selectedObject = (TCComponentItemRevision) targetComponents[0];

		if (selectedObject == null)
			return true;

		if (selectedObject.getType().compareTo(OBJECT_TYPE) != 0)
			return true;

		return false;
	}

	private void updateTargetReviseDateUI() {
		dlg.datTargetReviseDate.setVisible(dlg.ckbTargetReviseDate.getSelection());
	}
}
