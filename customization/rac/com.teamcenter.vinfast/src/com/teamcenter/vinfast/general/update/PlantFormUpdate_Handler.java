package com.teamcenter.vinfast.general.update;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.PlantFormConfigModel;
import com.teamcenter.vinfast.model.UpdatePlantFormModel;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class PlantFormUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private PlantFormUpdate_Dialog dlg;
	private List<UpdatePlantFormModel> partList;
	private List<String> taskNameList;
	private LinkedHashMap<String, PlantFormConfigModel> objectList;
	private LinkedHashMap<TCComponentTask, LinkedList<UpdatePlantFormModel>> taskList;

	public PlantFormUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

			taskList = new LinkedHashMap<TCComponentTask, LinkedList<UpdatePlantFormModel>>();
			taskNameList = new LinkedList<String>();
			objectList = new LinkedHashMap<String, PlantFormConfigModel>();
			String[] setting = TCExtension.GetPreferenceValues("VINF_PLANTFORMUPDATE_SETTING", session);
			if (setting != null) {
				if (setting.length >= 2) {
					String[] taskArray = setting[0].split(";");
					for (String task : taskArray) {
						if (!task.isEmpty())
							taskNameList.add(task);
					}
					String[] objectArray = setting[1].split(";");
					for (String object : objectArray) {
						if (!object.isEmpty()) {
							String[] str = object.split(",");
							if (str.length > 1) {
								objectList.put(str[0], new PlantFormConfigModel(str[0], str[1], session));
							}
						}
					}
				}
			}

			partList = new LinkedList<UpdatePlantFormModel>();
			for (InterfaceAIFComponent item : targetComp) {
				if (item instanceof TCComponentTask) {
					TCComponentTask task = (TCComponentTask) item;
					TCComponentTask rooTask = task.getRoot();
					if (validationTask(rooTask.getName())) {
						LinkedList<UpdatePlantFormModel> objectTempList = new LinkedList<UpdatePlantFormModel>();
						if (taskList.containsKey(task)) {
							objectTempList = taskList.get(task);
						} else {
							taskList.put(task, objectTempList);
						}
						TCComponent[] targetItems = task.getRelatedComponents("root_target_attachments");
						for (TCComponent targetItem : targetItems) {
							if (targetItem instanceof TCComponentItem) {
								if (validationObjectType(targetItem.getType())) {
									if (!checkPartExist(targetItem.getPropertyDisplayableValue("item_id"))) {
										UpdatePlantFormModel newUpdatePlantForm = new UpdatePlantFormModel((TCComponentItem) targetItem, objectList);
										partList.add(newUpdatePlantForm);
										objectTempList.add(newUpdatePlantForm);
									}
								}
							}
						}
					}
				} else if (item instanceof TCComponentItem) {
					TCComponentItem objectItem = (TCComponentItem) item;
					if (validationObjectType(objectItem.getType())) {
						if (!checkPartExist(objectItem.getPropertyDisplayableValue("item_id"))) {
							partList.add(new UpdatePlantFormModel((TCComponentItem) objectItem, objectList));
						}
					}
				}
			}

			// Init data
			String[] plantCodeDataForm = TCExtension.GetLovValues("vf4_plant", "VF4_plant_form", session);
			LinkedHashMap<String, String> procurementTypeDataForm = TCExtension.GetLovValueAndDisplay("vf4_make_buy", "VF4_plant_form", session);

			// Init UI
			dlg = new PlantFormUpdate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Update Plant Form");
			dlg.setMessage("Define business object create information");

			dlg.cbPlantCode.setItems(plantCodeDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbProcumentType, procurementTypeDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						updatePlantForm();
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

	private Boolean checkRequired() {
		if (dlg.cbPlantCode.getText().isEmpty() || dlg.cbProcumentType.getText().isEmpty())
			return false;
		return true;
	}

	private void updateTable(boolean first) {
		dlg.tblItem.removeAll();

		int i = 0;
		for (UpdatePlantFormModel item : partList) {
			i++;
			TableItem row = new TableItem(dlg.tblItem, SWT.NONE);
			row.setText(new String[] { "", String.valueOf(i), item.getPartNumber(), item.getReleaseStatus(), item.getPartName(), item.getPlantForm(), item.getPartMakeBuy(), item.getTaskStatus() });
			if (item.getTaskStatus().compareToIgnoreCase("Success") == 0)
				row.setBackground(7, SWTResourceManager.getColor(SWT.COLOR_GREEN));
			if (first)
				row.setChecked(true);
		}

		dlg.tblItem.redraw();
	}

	private void updatePlantForm() throws TCException, NotLoadedException, Exception {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		String plantCodeUpdate = dlg.cbPlantCode.getText();
		String procuTypeUpdate = (String) dlg.cbProcumentType.getData(dlg.cbProcumentType.getText());
		int i = 0;
		for (UpdatePlantFormModel item : partList) {
			item.setTaskStatus("Success");
			TableItem tableItem = dlg.tblItem.getItem(i++);
			if (tableItem.getChecked()) {
				PlantFormConfigModel plantformConfig = objectList.get(item.getObjectItem().getType());
				String relationName = plantformConfig.getRelationName();
				if (relationName.isEmpty())
					return;
				TCComponent[] forms = item.getObjectItem().getRelatedComponents(relationName);
				// validation
				boolean checkValid = true;
				List<TCComponent> formList = new LinkedList<TCComponent>();
				TCComponent formNull = null;
				for (TCComponent form : forms) {
					formList.add(form);
					String plantCode = form.getPropertyDisplayableValue(plantformConfig.getPlantProperty());
					String procuType = TCExtension.GetPropertyRealValue(form, plantformConfig.getProcumentProperty());

					if (plantCode.compareToIgnoreCase(plantCodeUpdate) == 0) {
//						checkValid = false;
						formNull = form;
					}

					if (plantCode.isEmpty() || procuType.isEmpty()) {
						formNull = form;
					}
				}
				if (checkValid) {
					if (formNull != null) {
						try {
							formNull.setStringProperty(plantformConfig.getPlantProperty(), plantCodeUpdate);
							formNull.setStringProperty(plantformConfig.getProcumentProperty(), procuTypeUpdate);
						} catch (Exception e) {
							item.setTaskStatus(e.toString());
						}
					} else {
						try {
							TCComponent newForm = createForm(item.getObjectItem(), plantCodeUpdate, procuTypeUpdate);
							if (newForm != null) {
								formList.add(newForm);
								item.getObjectItem().add(relationName, new TCComponent[] { newForm });
							}
						} catch (Exception e) {
							item.setTaskStatus(e.toString());
						}
					}
				} else {
					item.setTaskStatus("Plant code existed");
				}
			} else {
				item.setTaskStatus("");
			}
		}

		dlg.setMessage("Update plant form success", IMessageProvider.INFORMATION);
		updateTable(false);
		completeTasks();
	}

	private void completeTasks() {
		if (dlg.ckbFinishProcess.getSelection()) {
			for (Map.Entry<TCComponentTask, LinkedList<UpdatePlantFormModel>> taskItem : taskList.entrySet()) {
				boolean finishValid = true;
				for (UpdatePlantFormModel updatePlantFormItem : taskItem.getValue()) {
					if (updatePlantFormItem.getTaskStatus().compareTo("Success") != 0) {
						finishValid = false;
						break;
					}
				}
				if (finishValid) {
					try {
						completeTask(taskItem.getKey());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private TCComponent createForm(TCComponentItem item, String plantCode, String procuType) throws Exception {
		PlantFormConfigModel plantformConfig = objectList.get(item.getType());
		// create form
		String formName = item.getPropertyDisplayableValue("object_name");
		DataManagementService dms = DataManagementService.getService(session);
		CreateIn in = new CreateIn();
		in.data.boName = plantformConfig.getPlantFormObjectType();
		in.data.stringProps.put("object_name", formName);
		in.data.stringProps.put(plantformConfig.getPlantProperty(), plantCode);
		in.data.stringProps.put(plantformConfig.getProcumentProperty(), procuType);
		CreateIn[] ins = new CreateIn[] { in };
		CreateResponse res = dms.createObjects(ins);
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			// change ownerships
			TCComponent form = res.output[0].objects[0];
			TCComponentGroup partRevOwningGroup = (TCComponentGroup) (((TCComponent) item).getReferenceProperty("owning_group"));
			TCComponentUser partRevOwningUser = (TCComponentUser) (((TCComponent) item).getReferenceProperty("owning_user"));
			form.changeOwner(partRevOwningUser, partRevOwningGroup);
			return form;
		} else {
			String errorMessage = Utils.getErrorMessagesFromSOA(res.serviceData);
			throw new Exception(errorMessage);
		}
	}

	private boolean validationTask(String _task) {
		for (String task : taskNameList) {
			if (_task.compareToIgnoreCase(task) == 0)
				return true;
		}
		return false;
	}

	private boolean validationObjectType(String _object) {
		for (Map.Entry<String, PlantFormConfigModel> object : objectList.entrySet()) {
			if (_object.compareToIgnoreCase(object.getKey()) == 0)
				return true;
		}
		return false;
	}

	private boolean checkPartExist(String partNumber) {
		for (UpdatePlantFormModel part : partList) {
			if (partNumber.compareToIgnoreCase(part.getPartName()) == 0)
				return true;
		}
		return false;
	}

	private void completeTask(TCComponentTask selectSignOffTask) throws Exception {
		if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
			Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
			paii.clientId = "complete" + selectSignOffTask.getUid();
			paii.action = "SOA_EPM_complete_action";
			paii.actionableObject = selectSignOffTask;
			paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
			paii.supportingValue = "SOA_EPM_completed";

			ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
			if (sd.sizeOfPartialErrors() > 0) {
				System.out.println("[assignPerformer]: " + TCExtension.hanlderServiceData(sd));
			}
		}
	}
}
