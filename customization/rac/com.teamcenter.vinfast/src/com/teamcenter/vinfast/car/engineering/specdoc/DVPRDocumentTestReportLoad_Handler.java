package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.model.DVPRDocumentTestReportModel;
import com.vf.utils.TCExtension;

public class DVPRDocumentTestReportLoad_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService = null;
	private TCComponent selectedObject = null;

	public DVPRDocumentTestReportLoad_Handler() {
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

			LinkedList<DVPRDocumentTestReportModel> testReportList = new LinkedList<DVPRDocumentTestReportModel>();
			TCComponent[] testReports = selectedObject.getRelatedComponents("VF4_TestReports");
			if (testReports != null) {
				for (TCComponent vfteItem : testReports) {
					if (vfteItem.getPropertyDisplayableValue("object_type").compareTo("VFTE Document") == 0) {
						TCComponentItemRevision vfteRevision = TCExtension.getLatestItemRevision((TCComponentItem) vfteItem);
						String vfte = vfteItem.getPropertyDisplayableValue("item_id") + "/" + vfteRevision.getPropertyDisplayableValue("item_revision_id");
						TCComponent[] files = vfteRevision.getRelatedComponents("IMAN_specification");
						if (files != null) {
							for (TCComponent pdfFile : files) {
								if (pdfFile instanceof TCComponentDataset && pdfFile.getPropertyDisplayableValue("object_type").compareTo("PDF") == 0) {
									DVPRDocumentTestReportModel newTestReport = new DVPRDocumentTestReportModel();
									newTestReport.setVfte(vfte);
									newTestReport.setTestName(pdfFile.getPropertyDisplayableValue("object_name"));
									testReportList.add(newTestReport);
								}
							}
						}
					}
				}
			}

			TCComponent[] testReportRow = selectedObject.getReferenceListProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
			if (testReportRow == null || testReportRow.length == 0) {
				createdTestReportTable(testReportList);
			} else {
				for (DVPRDocumentTestReportModel testReportItem : testReportList) {
					for (TCComponent row : testReportRow) {
						String vfte = row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM);
						String testName = row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME);

						if (testReportItem.getVfte().compareTo(vfte) == 0 && testReportItem.getTestName().compareTo(testName) == 0) {
							testReportItem.setTestResult(row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT));
							testReportItem.setComment(row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT));
							testReportItem.setProgress(row.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS));
						}
					}
				}

				createdTestReportTable(testReportList);
			}
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void createdTestReportTable(LinkedList<DVPRDocumentTestReportModel> targetObjects) {
		try {
			LinkedList<CreateIn> createInputs = new LinkedList<CreateIn>();
			for (DVPRDocumentTestReportModel entry : targetObjects) {
				CreateIn createInput = new CreateIn();
				createInput.data.boName = SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.OBJECT_NAME;

				String vfteItem = entry.getVfte();
				String testName = entry.getTestName();
				String progress = entry.getProgress();
				String testResult = entry.getTestResult();
				String comment = entry.getComment();

				createInput.data.stringProps.put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM, vfteItem);
				createInput.data.stringProps.put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME, testName);
				createInput.data.stringProps.put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS, progress);
				createInput.data.stringProps.put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT, testResult);
				createInput.data.stringProps.put(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT, comment);

				createInputs.add(createInput);
			}

			CreateResponse response = dmService.createObjects(createInputs.toArray(new CreateIn[createInputs.size()]));
			if (response.serviceData.sizeOfPartialErrors() > 0)
				throw new Exception("There are some errors while processing, please contact to your IT Service Desk. " + TCExtension.hanlderServiceData(response.serviceData));

			List<TCComponent> newTestReportTable = new LinkedList<TCComponent>();
			for (CreateOut output : response.output) {
				newTestReportTable.addAll(Arrays.asList(output.objects));
			}

			selectedObject.setRelated(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME, newTestReportTable.toArray(new TCComponent[0]));
			selectedObject.refresh();
			if (selectedObject.isCheckedOut() == false && selectedObject.okToCheckout()) {
				CheckoutEditAction checkOutAction = new CheckoutEditAction();
				checkOutAction.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
