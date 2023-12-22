package com.teamcenter.vinfast.admin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.model.ReplaceGroupMemberModel;
import com.vf.utils.TCExtension;

public class TableObjectMigrate_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService;
	private TCComponent selectedObject;
	private TCComponent selectedObject1;

	private TableObjectMigrate_Dialog dlg;
	private LinkedHashMap<String, TCComponentTaskTemplate> workflowDataForm;
	private List<ReplaceGroupMemberModel> itemList;
	private TCComponentGroupMember oldMember = null;

	private String TABLE_RELATION = "vf4_SOR_part_info";
	private String TABLE_ROW = "vf4_SOR_part_info";

	private static String[] GROUP_PERMISSION = { "dba" };
	private static String[] TASKTYPE = { "EPMTaskTemplate", "EPMReviewTaskTemplate", "EPMConditionTaskTemplate", "EPMAcknowledgeTaskTemplate" };

	public TableObjectMigrate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			selectedObject1 = (TCComponent) targetComp[1];
			// get workflow list
			workflowDataForm = new LinkedHashMap<String, TCComponentTaskTemplate>();

			try {
				TCComponentTaskTemplateType tCComponentTaskTemplateType = (TCComponentTaskTemplateType) session.getTypeComponent("EPMTaskTemplate");
				if (tCComponentTaskTemplateType != null) {
					TCComponentTaskTemplate[] workflowList = tCComponentTaskTemplateType.getProcessTemplates(false, false, null, null, null);
					for (TCComponentTaskTemplate workflow : workflowList) {
						workflowDataForm.put(workflow.getName(), workflow);
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}

			dlg = new TableObjectMigrate_Dialog(new Shell());
			dlg.create();
			// Init UI
			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					moveTable(selectedObject, selectedObject1);
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void moveTable(TCComponent sourceItem, TCComponent targetItem) {
		try {
			TCComponent[] bomInfos = sourceItem.getReferenceListProperty("vf4_SOR_part_info");
			if (bomInfos.length > 0) {
				List<TCComponent> rows = new LinkedList<TCComponent>();

				LinkedList<CreateIn> createInputs = new LinkedList<CreateIn>();

				int i = 0;
				for (TCComponent bomline : bomInfos) {
					CreateIn createInput = new CreateIn();
					createInput.data.boName = TABLE_ROW;

					TCComponent part = null;
					TCComponent UOMTag = null;
					String uom = bomline.getProperty("vf4_main_module");

					try {
						part = (TCComponent) bomline.getPropertyObject("vf4_design_part").getModelObjectValue();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!uom.isEmpty()) {
						UOMTag = TCExtension.GetUOMItem(uom);
					}

					createInput.data.tagProps.put("vf4_design_part", part);
					createInput.data.tagProps.put("vf4_uom", UOMTag);
					createInput.data.stringProps.put("vf4_main_module", bomline.getProperty("vf4_main_module"));
					createInput.data.stringProps.put("vf4_module_group", bomline.getProperty("vf4_module_group"));
					createInput.data.stringProps.put("vf4_module_name", bomline.getProperty("vf4_module_name"));
					createInput.data.stringProps.put("vf4_purchase_level", bomline.getProperty("vf4_purchase_level"));

					createInputs.add(createInput);
					i++;
				}

				// create table row
				CreateResponse response = dmService.createObjects(createInputs.toArray(new CreateIn[createInputs.size()]));
				List<TCComponent> newSORTable = new LinkedList<TCComponent>();
				if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
					for (CreateOut output : response.output) {
						newSORTable.addAll(Arrays.asList(output.objects));
					}

					targetItem.setRelated("vf4_SOR_part_info", newSORTable.toArray(new TCComponent[0]));
					targetItem.refresh();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TCComponent creatEcrFormAndAttachToECR(TCComponent ecrRev) throws Exception {
		Map<String, String> stringProps = new HashMap<String, String>();
		String ecrRevName = ecrRev.getProperty("object_name");
		stringProps.put("object_name", ecrRevName);
		CreateIn input = new CreateIn();
		input.data.boName = TABLE_ROW;
		input.data.stringProps = stringProps;

		CreateResponse res = dmService.createObjects(new CreateIn[] { input });

		TCComponent newECRForm = null;
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			newECRForm = res.output[0].objects[0];
			ecrRev.setRelated(TABLE_RELATION, new TCComponent[] { newECRForm });
		} else {
			String errorMsg = TCExtension.hanlderServiceData(res.serviceData);
			throw new Exception("Cannot create ECR form with below errors.\n" + errorMsg);
		}

		return newECRForm;
	}
}
