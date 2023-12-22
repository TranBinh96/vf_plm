package com.teamcenter.vinfast.aftersale.create;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchECNRev_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SCNCreate_Handler extends AbstractHandler {
	private TCSession session;
	private SCNCreate_Dialog dlg;
	private TCComponent selectedObject;
	private String OBJECT_TYPE = "VF4_SCN";
	private LinkedList<TCComponent> implementList;

	public SCNCreate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			// init data
			implementList = new LinkedList<>();
			LinkedHashMap<String, String> vehicleProgramDataForm = TCExtension.GetLovValueAndDisplay("vf4_vehicle_programs", "VF4_SCNRevision", session);
			dlg = new SCNCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleProgram, vehicleProgramDataForm);
			dlg.cbVehicleProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String program = dlg.cbVehicleProgram.getText();
					dlg.txtPrefixName.setText("[" + mappingNewProgram(program) + "]");
				}
			});

			dlg.btnECNSearch.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					String objectType = "Engineering Change Request Revision;Engineering Change Notice Revision;Engineering Change Notice - Electronics & Electricity Revision;Engineering Change Notice Revision - Escooter;Engineering Change Notice Revision - Escooter Emotor";
					SearchECNRev_Dialog searchDlg = new SearchECNRev_Dialog(dlg.getShell(), objectType);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							try {
								TCComponent temp = searchDlg.itemSearch.get(index);
								if (!implementList.contains(temp)) {
									implementList.add(temp);
									dlg.lstECN.add(temp.getPropertyDisplayableValue("item_id") + "/" + temp.getPropertyDisplayableValue("item_revision_id"));
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnECNRemove.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					int index = -1;
					try {
						index = dlg.lstECN.getSelectionIndex();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (index >= 0) {
						dlg.lstECN.remove(index);
						implementList.remove(index);
					}
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					createNewObject();
				}
			});
			deriveFromSCR();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void deriveFromSCR() {
		try {
			String objectType = selectedObject.getType();
			if (objectType.compareTo("VF4_SCRRevision") == 0) {
				String name = selectedObject.getPropertyDisplayableValue("object_name");
				String desc = selectedObject.getPropertyDisplayableValue("object_desc");
				String vehicle = selectedObject.getPropertyDisplayableValue("vf4_veh_program");
				String scrNumber = selectedObject.getPropertyDisplayableValue("item_id");
				String scrRev = selectedObject.getPropertyDisplayableValue("item_revision_id");

				String programName = StringExtension.getStringBetweenKeyword(name, "[", "]");
				dlg.txtName.setText(programName.isEmpty() ? name : name.replace("[" + programName + "]", ""));
				dlg.txtDescription.setText(desc);
				dlg.cbVehicleProgram.setText(vehicle);
				implementList.add(selectedObject);
				dlg.lstECN.add(scrNumber + "/" + scrRev);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNewObject() {
		try {
			if (!createValidate()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dmService = DataManagementService.getService(session);

			String vehicleProgram = (String) dlg.cbVehicleProgram.getData(dlg.cbVehicleProgram.getText());
			String name = dlg.txtPrefixName.getText() + dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			boolean sorRequired = dlg.ckbSORRequired.getSelection();
			boolean illustUpdateRequired = dlg.ckbIllustrationUpdateRequired.getSelection();
			boolean hPartRequired = dlg.ckbHPartsRequired.getSelection();
			boolean translationRequired = dlg.ckbTranslationRequired.getSelection();
			boolean marketPurchaseRequired = dlg.ckbMarketSpecificPurchaseRequired.getSelection();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringProps.put("vf4_vehicle_programs", vehicleProgram);
			revDef.boolProps.put("vf4_is_sor_required", sorRequired);
			revDef.boolProps.put("vf4_illustration_required", illustUpdateRequired);
			revDef.boolProps.put("vf4_is_h_parts_required", hPartRequired);
			revDef.boolProps.put("vf4_is_translation_required", translationRequired);
			revDef.boolProps.put("vf4_market_specific_purchas", marketPurchaseRequired);
			revDef.stringProps.put("vf4_priority_pr", "1");
			revDef.stringProps.put("vf4_change_reason_pr", "1");
			revDef.stringProps.put("vf4_classification_pr", "1");
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "VF4_SCN_GenID";
			idGen.stringProps.put("vf4_vehicle_programs", vehicleProgram);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });
			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage("Create unsuccessfully. Exception: " + TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			TCComponent cfgContext = response.output[0].objects[0];
			TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
			if (cfgContext != null) {
				if (dlg.lstECN.getItemCount() > 0) {
					try {
						itemRev.add("CMImplements", implementList.toArray(new TCComponent[0]));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Boolean addToFolder = false;
				if (selectedObject != null) {
					String type = selectedObject.getProperty("object_type");
					if (type.compareToIgnoreCase("Folder") == 0) {
						try {
							selectedObject.add("contents", cfgContext);
							addToFolder = true;
							dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
				}
				if (!addToFolder) {
					try {
						session.getUser().getNewStuffFolder().add("contents", cfgContext);
						dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
						openOnCreate(cfgContext);
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
				resetDialog();
			} else {
				dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean createValidate() {
		if (dlg.cbVehicleProgram.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;

		return true;
	}

	private void resetDialog() {
		dlg.cbVehicleProgram.deselectAll();
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.lstECN.removeAll();
		implementList = new LinkedList<>();
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String mappingNewProgram(String program) {
		switch (program) {
		case "VFe32":
			return "VF5";
		case "VFe33":
			return "VF6";
		case "VFe34S":
			return "VF7";
		case "VFe35":
			return "VF8";
		case "VFe36":
			return "VF9";
		default:
			return program;
		}
	}
}
