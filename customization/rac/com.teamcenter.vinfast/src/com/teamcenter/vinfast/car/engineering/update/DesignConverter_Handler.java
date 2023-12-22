package com.teamcenter.vinfast.car.engineering.update;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class DesignConverter_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private DesignConverter_Dialog dlg;
	private static String[] objectTypeAvailable = new String[] { "VF4_DesignRevision", "VF4_BP_DesignRevision" };
	private boolean fromSingleToProduct = true;
	private static String SUCCESS_STATUS = "Success";
	private static String ALREADY_EXISTS_STATUS = "Already exists";

	public DesignConverter_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if (!validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one VF Design, VF BP Design.", "Information", MessageBox.INFORMATION);
			return null;
		}
		try {
			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to convert part.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			dlg = new DesignConverter_Dialog(new Shell());
			dlg.create();

			dlg.txtPart.setText(selectedObject.toString());
			dlg.btnChange.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fromSingleToProduct = !fromSingleToProduct;
					updateSwitchUI();
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					convertPart();
				}
			});

			updateSwitchUI();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateSwitchUI() {
		dlg.txtFrom.setText(fromSingleToProduct ? "Part Design" : "Product Design");
		dlg.txtTo.setText(fromSingleToProduct ? "Product Design" : "Part Design");
	}

	private void convertPart() {
		LinkedList<String> convertResult = new LinkedList<String>();
		if (fromSingleToProduct) {
			convertResult.add("DirectModel;Cut;" + cutObjectFromComponent("IMAN_Rendering", new String[] { "DirectModel" }));
			convertResult.add("catia_model_attributes;Cut;" + cutObjectFromComponent("IMAN_specification", new String[] { "catia_model_attributes" }));
			convertResult.add("CATPart;Cut;" + cutObjectFromComponent("IMAN_specification", new String[] { "CATPart" }));
			convertResult.add("CATCache;Cut;" + cutObjectFromComponent("catia_cache_link", new String[] { "CATCache" }));
			convertResult.add("PSBOMViewRevision;Create;" + createBomViewRevision(true));
			convertResult.add("CATProduct;Create;" + createDatasetObject("CATProduct", "IMAN_specification"));
		} else {
			convertResult.add("CATProduct;Cut;" + cutObjectFromComponent("IMAN_specification", new String[] { "CATProduct" }));
			convertResult.add("PSBOMViewRevision;Delete;" + delObjectFromComponent("structure_revisions", new String[] { "BOMView Revision" }));
			convertResult.add("CATPart;Create;" + createDatasetObject("CATPart", "IMAN_specification"));
		}
		refreshReport(convertResult);
	}

	private void refreshReport(LinkedList<String> convertResult) {
		StringBuilder reportStr = new StringBuilder();
		reportStr.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		reportStr.append(StringExtension.htmlTableCss);
		reportStr.append("<body style=\"margin: 0px;\">");
		reportStr.append("<table>");
		if (convertResult != null) {
			reportStr.append(StringExtension.genTableHeader(new LinkedHashMap<String, String>() {
				{
					put("Object", "30");
					put("Action", "20");
					put("Status", "50");
				}
			}));
			for (String resultItem : convertResult) {
				reportStr.append("<tr>");
				if (resultItem.contains(";")) {
					String[] item = resultItem.split(";");
					if (item.length > 2) {
						reportStr.append("<td><p>" + item[0] + "</p></td>");
						reportStr.append("<td><p>" + item[1] + "</p></td>");
						String status = item[2];
						if (status.compareTo(SUCCESS_STATUS) == 0) {
							reportStr.append("<td>" + StringExtension.genBadgetSuccess(status) + "</td>");
						} else if (status.compareTo(ALREADY_EXISTS_STATUS) == 0) {
							reportStr.append("<td>" + StringExtension.genBadgetDefault(status) + "</td>");
						} else {
							reportStr.append("<td>" + StringExtension.genBadgetFail(status) + "</td>");
						}
					}
				}
				reportStr.append("</tr>");
			}
		}
		reportStr.append("</table>");
		reportStr.append("</body></html>");
		dlg.browser.setText(reportStr.toString());
	}

	private String cutObjectFromComponent(String relation, String[] cutObjectTypes) {
		try {
			TCComponent[] excelComponent = selectedObject.getRelatedComponents(relation);
			for (TCComponent tcComponent : excelComponent) {
				if (Arrays.stream(cutObjectTypes).anyMatch(tcComponent.getType()::equals)) {
					selectedObject.remove(relation, tcComponent);
				}
			}
		} catch (Exception e) {
			return e.toString();
		}
		return SUCCESS_STATUS;
	}

	private String delObjectFromComponent(String relation, String[] cutObjectTypes) {
		try {
			TCComponent[] component = selectedObject.getRelatedComponents(relation);
			for (TCComponent tcComponent : component) {
				String type = tcComponent.getType();
				if (Arrays.stream(cutObjectTypes).anyMatch(tcComponent.getType()::equals)) {
					tcComponent.removeAndDestroy(relation, selectedObject);
				}
			}
		} catch (Exception e) {
			return e.toString();
		}
		return SUCCESS_STATUS;
	}

	private String createDatasetObject(String datasetType, String relation) {
		try {
			selectedObject.refresh();
			TCComponent[] component = selectedObject.getRelatedComponents(relation);
			for (TCComponent tcComponent : component) {
				if (tcComponent.getType().compareTo(datasetType) == 0) {
					return ALREADY_EXISTS_STATUS;
				}
			}

			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			String datasetName = selectedObject.getProperty("item_id") + "/" + selectedObject.getProperty("item_revision_id");
			TCComponentDataset newDataset = datasetTypeComponent.create(datasetName, "", datasetDefinition.toString());
			selectedObject.add(relation, newDataset);
		} catch (Exception e) {
			return e.toString();
		}
		return SUCCESS_STATUS;
	}

	private String createBomViewRevision(Boolean precise) {
		try {
			TCComponent[] component = selectedObject.getRelatedComponents("structure_revisions");
			for (TCComponent tcComponent : component) {
				if (tcComponent.getType().compareTo("BOMView Revision") == 0) {
					return ALREADY_EXISTS_STATUS;
				}
			}

			CreateOrUpdateRelativeStructureInfo2[] info = new CreateOrUpdateRelativeStructureInfo2[1];
			info[0] = new CreateOrUpdateRelativeStructureInfo2();
			info[0].lastModifiedOfBVR = Calendar.getInstance();
			info[0].parent = selectedObject;
			info[0].precise = true;

			CreateOrUpdateRelativeStructurePref2 prefs = new CreateOrUpdateRelativeStructurePref2();
			prefs.cadOccIdAttrName = null;
			prefs.itemTypes = null;
			prefs.overwriteForLastModDate = true;

			StructureManagement smService = com.teamcenter.services.rac.cad.StructureManagementService.getService(session);
			CreateOrUpdateRelativeStructureResponse response = smService.createOrUpdateRelativeStructure(info, "PR4D_cad", true, prefs);
			if (response.serviceData.sizeOfPartialErrors() >= 0) {
				return TCExtension.hanlderServiceData(response.serviceData);
			}
		} catch (Exception e) {
			return e.toString();
		}
		return SUCCESS_STATUS;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		try {
			if (targetComponents == null)
				return true;
			if (targetComponents.length > 1)
				return true;
			if (targetComponents[0] instanceof TCComponentItemRevision) {
				selectedObject = ((TCComponentItemRevision) targetComponents[0]);
			}
			if (selectedObject == null)
				return false;
//			if (!Arrays.stream(objectTypeAvailable).anyMatch(selectedObject.getType()::equals))
//				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
