package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.model.DVPRDocumentVFTESummaryModel;
import com.vf.utils.TCExtension;

public class DVPRDocumentVFTESummaryLoad_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService = null;
	private TCComponent selectedObject = null;

	private LinkedHashMap<TCComponent, DVPRDocumentVFTESummaryModel> summaryList;

	public DVPRDocumentVFTESummaryLoad_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			String objectType = selectedObject.getType();
			if (objectType.compareTo("VF3_AT_DVPR_docRevision") != 0) {
				MessageBox.post("Please select one DVPR Document Revision.", "Warning", MessageBox.WARNING);
				return null;
			}

			summaryList = new LinkedHashMap<TCComponent, DVPRDocumentVFTESummaryModel>();

			TCComponent[] bomInfo = selectedObject.getRelatedComponents(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
			if (bomInfo.length > 0) {
				for (TCComponent row : bomInfo) {
					TCComponent vfteObject = null;
					String vfte = row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM);
					if (!vfte.isEmpty()) {
						String[] str = vfte.split("/");
						if (str.length >= 2) {
							LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
							queryInput.put("Item ID", str[0]);
							queryInput.put("Revision", str[1]);
							queryInput.put("Type", "AT VFTE Doc Revision");

							TCComponent[] queryOutput = TCExtension.queryItem(session, queryInput, "Item Revision...");
							if (queryOutput != null && queryInput.size() > 0)
								vfteObject = queryOutput[0];
						}
					}

					if (vfteObject == null)
						continue;

					String testResult = row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT);
					if (summaryList.containsKey(vfteObject)) {
						summaryList.get(vfteObject).setTestResult(testResult);
					} else {
						DVPRDocumentVFTESummaryModel newItem = new DVPRDocumentVFTESummaryModel();
						newItem.setTestResult(testResult);
						newItem.setTargetReleaseDate(vfteObject.getPropertyDisplayableValue("vf4_target_revise_date"));

						summaryList.put(vfteObject, newItem);
					}
				}
			}

			if (summaryList.size() > 0) {
				LinkedList<CreateIn> createInputs = new LinkedList<CreateIn>();
				for (Map.Entry<TCComponent, DVPRDocumentVFTESummaryModel> entry : summaryList.entrySet()) {
					DVPRDocumentVFTESummaryModel summaryItem = entry.getValue();

					CreateIn createInput = new CreateIn();
					createInput.data.boName = SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.OBJECT_NAME;

					createInput.data.tagProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_VFTE_ITEM, entry.getKey());
					createInput.data.stringProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_GREEN_NUMBER, String.valueOf(summaryItem.getGreenNumber()));
					createInput.data.stringProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_YELLOW_NUMBER, String.valueOf(summaryItem.getYellowNumber()));
					createInput.data.stringProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_RED_NUMBER, String.valueOf(summaryItem.getRedNumber()));
					createInput.data.stringProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_NEED_REVISE, String.valueOf(summaryItem.getNeedRevision()));
					createInput.data.stringProps.put(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.PROPERTY_RELEASE_DATE, String.valueOf(summaryItem.getTargetReleaseDate()));

					createInputs.add(createInput);
				}

				CreateResponse response = dmService.createObjects(createInputs.toArray(new CreateIn[createInputs.size()]));
				if (response.serviceData.sizeOfPartialErrors() > 0)
					throw new Exception("There are some errors while processing, please contact to your IT Service Desk. " + TCExtension.hanlderServiceData(response.serviceData));

				List<TCComponent> newVFTESummaryTable = new LinkedList<TCComponent>();
				for (CreateOut output : response.output) {
					newVFTESummaryTable.addAll(Arrays.asList(output.objects));
				}

				selectedObject.setRelated(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.RELATION_NAME, newVFTESummaryTable.toArray(new TCComponent[0]));
			} else {
				selectedObject.setRelated(SpecDocNameDefine.DVPR_VFTE_SUMMARY_TABLE.RELATION_NAME, new TCComponent[0]);
			}
			selectedObject.refresh();
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}
}
