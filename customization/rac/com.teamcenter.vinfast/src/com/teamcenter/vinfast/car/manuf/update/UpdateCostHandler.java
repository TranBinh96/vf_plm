package com.teamcenter.vinfast.car.manuf.update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
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
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.CostUtils;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

import vfplm.soa.common.service.VFUtility;

public class UpdateCostHandler extends AbstractHandler {
	private TCSession session = null;
	private DataManagementService dmService = null;
	private UpdateCostDialog dlg;
	private LinkedList<UpdateCostModel> requestList = null;
	private static String PROCESS_NAME = "Manufacturing Change Cost Request";
	private static String AL_NAME = "Manufacturing Change Cost Request";

	public UpdateCostHandler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// ---------------------------- Init -------------------------------------
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

		try {
			dlg = new UpdateCostDialog(new Shell());
			dlg.create();
			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createRequest();
				}
			});

			validateSelectedObject(targetComp);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void validateSelectedObject(InterfaceAIFComponent[] targetComp) {
		boolean validSuccess = true;
		String[] whiteLstObj = TCExtension.GetPreferenceValues("VINF_COST_AVAILABLE_OBJECT_TYPES", session);
		requestList = new LinkedList<>();

		for (InterfaceAIFComponent selected : targetComp) {
			TCComponentItemRevision newItem = null;
			if (selected instanceof TCComponentBOMLine) {
				try {
					newItem = ((TCComponentBOMLine) selected).getItemRevision();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (selected instanceof TCComponentItemRevision) {
				newItem = (TCComponentItemRevision) selected;
			}
			if (newItem != null) {
				UpdateCostModel requestItem = new UpdateCostModel();
				requestItem.setObjectItem(newItem);
				if (!Arrays.asList(whiteLstObj).contains(newItem.getTypeObject().toString())) {
					requestItem.setValidMessage("Object type not valid.");
					validSuccess = false;
				}

				requestList.add(requestItem);
			}
		}

		dlg.btnCreate.setEnabled(validSuccess);

		refreshReport(false);
	}

	private void refreshReport(boolean processed) {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("No", "5");
				put("Part Number", "20");
				put("Object Type", "20");
				put("Status", "55");
			}
		};
		int i = 0;
		validationResultText.append(StringExtension.genTableHeader(header));
		for (UpdateCostModel requestItem : requestList) {
			validationResultText.append("<tr>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + requestItem.getItemID() + "/" + requestItem.getRevisionID() + "</td>");
			validationResultText.append("<td>" + requestItem.getObjectType() + "</td>");
			if (!processed) {
				if (requestItem.isReadyProcess())
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Ready to send request.") + "</td>");
				else
					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(requestItem.getValidMessage()) + "</td>");
			} else {

			}
			validationResultText.append("</tr>");
		}
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwValidate.setText(validationResultText.toString());
	}

	private void createRequest() {
		if (dlg.txtReason.getText().isEmpty()) {
			dlg.setMessage("Please input REASON", IMessageProvider.WARNING);
			return;
		}

		boolean predataSuccess = true;
		LinkedList<TCComponent> mfgRequestForms = new LinkedList<>();
		LinkedList<TCComponent> actualPieceForms = new LinkedList<>();
		boolean isCreateCost = (session.getGroup().toString().contains("Car Program.VINFAST") == false);
		for (UpdateCostModel requestItem : requestList) {
			TCComponentItemRevision designPart = requestItem.getObjectItem();
			try {
				CostUtils costUtil = new CostUtils(session, designPart);
				TCComponent actualPieceCostForm = costUtil.getOrCreateOrSearchAndRelatePieceCost(isCreateCost);
				if (actualPieceCostForm == null)
					throw new Exception("Actual Piece Cost Form not exist.");

				TCComponent mfgRequestForm = createPieceCostForm(designPart);
				if (mfgRequestForm == null)
					throw new Exception("Actual Piece Cost Form Temp not exist.");

				copyCostAttributes(actualPieceCostForm, mfgRequestForm);
				mfgRequestForm.setProperty("vf4_comment", dlg.txtReason.getText());

				mfgRequestForms.add(mfgRequestForm);
				actualPieceForms.add(actualPieceCostForm);
			} catch (Exception e) {
				e.printStackTrace();
				requestItem.setProcessMessage(e.getMessage());
				predataSuccess = false;
			}
		}

		if (!predataSuccess) {
			if (mfgRequestForms != null && mfgRequestForms.size() > 0)
				dmService.deleteObjects(mfgRequestForms.toArray(new TCComponent[0]));

			dlg.setMessage("Prepare data unsuccessfully.", IMessageProvider.ERROR);
			return;
		}
		String triggerResult = submitWorkflow(mfgRequestForms, actualPieceForms);
		if (triggerResult.isEmpty()) {
			dlg.getShell().dispose();
			MessageBox.post("Process has been triggererd successfully", "INFO", MessageBox.INFORMATION);
		} else {
			dlg.setMessage("Create request unsuccessfull. Exception: " + triggerResult, IMessageProvider.ERROR);
		}
	}

	private TCComponent createPieceCostForm(ModelObject partRev) throws Exception {
		String formName = partRev.getPropertyDisplayableValue("item_id") + "_MFGRequestUpdateCost";

		CreateIn in = new CreateIn();
		in.data.boName = "VF4_PieceCostForm";
		in.data.stringProps.put("object_name", formName);
		CreateIn[] ins = new CreateIn[] { in };
		CreateResponse res = dmService.createObjects(ins);

		if (res.serviceData.sizeOfPartialErrors() > 0)
			throw new Exception("Create MFG Request Form unsuccessfully. Exception: " + TCExtension.hanlderServiceData(res.serviceData));

		return res.output[0].objects[0];
	}

	private String copyCostAttributes(TCComponent oldForm, TCComponent newForm) throws NotLoadedException {
		Hashtable<String, PropertyDescription> propeDescMap = newForm.getTypeObject().getPropDescs();
		dmService.refreshObjects(new TCComponent[] { oldForm, newForm });
		dmService.getProperties(new TCComponent[] { oldForm, newForm }, propeDescMap.keySet().toArray(new String[0]));

		Map<String, VecStruct> nameValMap = new HashMap<String, VecStruct>();

		for (Entry<String, PropertyDescription> propDescEntry : propeDescMap.entrySet()) {
			String propName = propDescEntry.getKey();
			PropertyDescription propDesc = propDescEntry.getValue();
			int propType = propDesc.getType();
			String currentTargetValue = newForm.getPropertyDisplayableValue(propName);

			if (propName.startsWith("vf4") && currentTargetValue.isEmpty()
					&& (propType == PropertyDescription.CLIENT_PROP_TYPE_string || propType == PropertyDescription.CLIENT_PROP_TYPE_bool || propType == PropertyDescription.CLIENT_PROP_TYPE_int || (propType == PropertyDescription.CLIENT_PROP_TYPE_double))) {
				Property propObj = oldForm.getPropertyObject(propName);
				String valToCopy = (propObj.getPropertyDescription().getType() == PropertyDescription.CLIENT_PROP_TYPE_string) ? propObj.getStringValue() : oldForm.getPropertyDisplayableValue(propName);
				VecStruct valVec = new VecStruct();
				valVec.stringVec = new String[] { (propType == PropertyDescription.CLIENT_PROP_TYPE_bool) ? convertToSoaBool(valToCopy) : valToCopy };
				if (propDesc.isModifiable()) {
					if (valToCopy.isEmpty() == false) {
						nameValMap.put(propName, valVec);
					}
				} else {
					System.out.println("[copyCostAttributes] WARNING: " + propName + " is NOT modifiable.");
				}
			}
		}

		if (nameValMap.isEmpty() == false) {
			ServiceData serviceData = DataManagementService.getService(session).setProperties(new TCComponent[] { newForm }, nameValMap);
			if (serviceData.sizeOfPartialErrors() > 0) {
				return VFUtility.HanlderServiceData(serviceData);
			}
		}
		return "";
	}

	private static String convertToSoaBool(String valToCopy) {
		return (valToCopy.toLowerCase().contains("true") ? "1" : (valToCopy.toLowerCase().contains("false") ? "0" : ""));
	}

	private String submitWorkflow(LinkedList<TCComponent> targets, LinkedList<TCComponent> references) {
		String firstPartNumber = "";
		try {
			firstPartNumber = targets.get(0).getPropertyDisplayableValue("item_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String triggerMessage = "";
		String processName = "MFG Request Update Cost - " + firstPartNumber;
		String processDesc = "MFG Request Update Cost - " + firstPartNumber;
		LinkedList<String> targetUids = new LinkedList<>();
		for (TCComponent targetItem : targets) {
			targetUids.add(targetItem.getUid());
		}

		LinkedList<String> referUids = new LinkedList<>();
		for (TCComponent referenceItem : references) {
			referUids.add(referenceItem.getUid());
		}

		int[] targetAttachTypes = new int[targetUids.size()];
		int[] referAttachTypes = new int[references.size()];
		Arrays.fill(targetAttachTypes, 1);
		Arrays.fill(referAttachTypes, 3);

		try {
			TriggerProcess triggerCOP = new TriggerProcess(session);
			triggerCOP.setWorkProcessTemplace(PROCESS_NAME);
			triggerCOP.setWorkProcessName(processName);
			triggerCOP.setWorkProcessDesc(processDesc);
			triggerCOP.setAlName(AL_NAME);
			triggerCOP.setWorkProcessAttachment(ArrayUtils.addAll(targetUids.toArray(new String[0]), referUids.toArray(new String[0])));
			triggerCOP.setWorkProcessAttachmentType(ArrayUtils.addAll(targetAttachTypes, referAttachTypes));
			triggerMessage = triggerCOP.run();
		} catch (Exception e) {
			e.printStackTrace();
			triggerMessage = e.getMessage();
		}

		return triggerMessage;
	}
}