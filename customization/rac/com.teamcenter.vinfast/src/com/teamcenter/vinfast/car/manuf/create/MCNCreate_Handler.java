package com.teamcenter.vinfast.car.manuf.create;

import java.util.LinkedHashMap;

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
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.AddParticipantInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.AddParticipantOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ParticipantInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.ICCTGroupMember;
import com.teamcenter.vinfast.subdialog.SearchECNRev_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

public class MCNCreate_Handler extends AbstractHandler {
	private TCSession session;
	private MCNCreate_Dialog dlg;
	private static String OBJECT_TYPE = "Vf6_MCN";
	private TCComponent selectedObject = null;
	private TCComponent implementItemRev = null;

	public MCNCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			// Init data
			String[] changeRequestTypeDataForm = TCExtension.GetLovValues("vf6_change_request_type", "Vf6_MCN", session);
			LinkedHashMap<String, String> programDataForm = TCExtension.GetLovValueAndDisplay("vf6_program_model_name", "Vf6_MCN", session);
			removeValueFromLinkedList(programDataForm, "VF35");
			removeValueFromLinkedList(programDataForm, "C-SUV");
			removeValueFromLinkedList(programDataForm, "VF36");
			removeValueFromLinkedList(programDataForm, "D-SUV");

			LinkedHashMap<String, String> shopDataForm = TCExtension.GetLovValueAndDescription("vf6_shop", "Vf6_MCN", session);
			String[] changeTypeDataForm = TCExtension.GetLovValues("vf6_change_type", "Vf6_MCN", session);
			String[] modelYearDataForm = TCExtension.GetLovValues("vf6_model_year", "Vf6_MCN", session);

			// Init UI
			dlg = new MCNCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, programDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbShop, shopDataForm);

			dlg.cbChangeType.setItems(changeTypeDataForm);

			dlg.cbYear.setItems(modelYearDataForm);

			dlg.btnSearch.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String changeRequestType = dlg.cbChangeRequestType.getText();
					if (changeRequestType.compareTo("ECR/ECN") == 0 || changeRequestType.compareTo("MCR") == 0) {
						String objectType = "";
						if (changeRequestType.compareTo("ECR/ECN") == 0)
							objectType = "Engineering Change Request Revision;Engineering Change Notice Revision;Engineering Change Notice - Electronics & Electricity Revision;Engineering Change Notice Revision - Escooter;Engineering Change Notice Revision - Escooter Emotor";
						else
							objectType = "MCR Revision";

						SearchECNRev_Dialog searchDlg = new SearchECNRev_Dialog(dlg.getShell(), objectType);
						searchDlg.open();
						Button ok = searchDlg.getOKButton();

						ok.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								int index = searchDlg.tblSearch.getSelectionIndex();
								try {
									TCComponent temp = searchDlg.itemSearch.get(index);
									String objType = temp.getType();
									if (objType.compareTo("Vf6_ECRRevision") == 0) {
										TCComponent[] ecnItems = temp.getRelatedComponents("CMImplementedBy");
										for (TCComponent ecnItem : ecnItems) {
											if (ecnItem.getType().compareTo("Vf6_ECNRevision") == 0) {
												implementItemRev = ecnItem;
												break;
											}
										}
									} else {
										implementItemRev = searchDlg.itemSearch.get(index);
									}
									dlg.txtImplements.setText(implementItemRev.getPropertyDisplayableValue("object_string"));
								} catch (Exception ex) {
									ex.printStackTrace();
								}

								searchDlg.getShell().dispose();
							}
						});
					}
				}
			});

			dlg.cbChangeRequestType.setItems(changeRequestTypeDataForm);
			dlg.cbChangeRequestType.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateUI();
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					createNewItem();
				}
			});
			updateUI();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void addParticipiant(TCSession session, TCComponent assignee, TCComponentItemRevision revision,  String participantType) {
		
		ParticipantInfo partInfo =  new ParticipantInfo();
		partInfo.assignee = assignee;
		partInfo.participantType = participantType;
		
		AddParticipantInfo addInfo = new AddParticipantInfo();
		addInfo.itemRev = revision;
		addInfo.participantInfo = new ParticipantInfo[] {partInfo};
		
		
		AddParticipantOutput output = DataManagementService.getService(session).addParticipants(new AddParticipantInfo[] {addInfo});
		if(output.serviceData.sizeOfPartialErrors() > 0) {
			try {
				dlg.setMessage("MCN:"+revision.getPropertyDisplayableValue("item_id")+" Created. But failed to add participant.", IMessageProvider.ERROR);
			} catch (NotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updateUI() {
		dlg.txtDCRNumber.setText("");
//		dlg.txtDCRNumber.setEnabled(false);
		dlg.txtImplements.setText("");
//		dlg.txtImplements.setEnabled(false);
		implementItemRev = null;
//		dlg.btnSearch.setEnabled(false);

		String changeRequestType = dlg.cbChangeRequestType.getText();
		if (changeRequestType.compareTo("ECR/ECN") == 0) {
			dlg.lblECNMCR.setText("ECN: (*)");
			dlg.lblDCROther.setText("");
			dlg.txtImplements.setVisible(true);
			dlg.txtDCRNumber.setVisible(false);
			dlg.txtDCRNumber.setText("");
			dlg.btnSearch.setVisible(true);
		} else if (changeRequestType.compareTo("MCR") == 0) {
			dlg.lblECNMCR.setText("MCR: (*)");
			dlg.lblDCROther.setText("");
			dlg.txtImplements.setVisible(true);
			dlg.txtDCRNumber.setVisible(false);
			dlg.txtDCRNumber.setText("");
			dlg.btnSearch.setVisible(true);
		} else if (changeRequestType.compareTo("DCR") == 0) {
			dlg.lblECNMCR.setText("");
			dlg.lblDCROther.setText("DCR: (*)");
			dlg.txtImplements.setVisible(false);
			dlg.txtDCRNumber.setVisible(true);
			dlg.txtImplements.setText("");
			implementItemRev = null;
			dlg.btnSearch.setVisible(false);
		} else if (changeRequestType.compareTo("Other") == 0) {
			dlg.lblECNMCR.setText("");
			dlg.lblDCROther.setText("Other: (*)");
			dlg.txtImplements.setVisible(false);
			dlg.txtDCRNumber.setVisible(true);
			dlg.txtImplements.setText("");
			implementItemRev = null;
			dlg.btnSearch.setVisible(false);
		}
	}

	private boolean checkRequired() {
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDescription.getText().isEmpty())
			return false;
		if (dlg.cbChangeRequestType.getText().isEmpty())
			return false;

		String changeRequestType = dlg.cbChangeRequestType.getText();
		if (changeRequestType.compareTo("ECR/ECN") == 0 || changeRequestType.compareTo("MCR") == 0) {
			if (dlg.txtImplements.getText().isEmpty())
				return false;
		} else if (changeRequestType.compareTo("DCR") == 0 || changeRequestType.compareTo("Other") == 0) {
			if (dlg.txtDCRNumber.getText().isEmpty())
				return false;
		}

		return true;
	}

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		DataManagementService dms = DataManagementService.getService(session);
		// item
		String name = dlg.txtName.getText();
		String description = dlg.txtDescription.getText();
		String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());

		// vf6_vehicle_group
		// vf6_shop, vf6_sap_plant
		// vf6_change_type
		// vf6_model_year
		// vf6_effective_proposal

		String shop = (String) dlg.cbShop.getData(dlg.cbShop.getText());
		String plant = getPlantFromShop(shop);
		String changeType = dlg.cbChangeType.getText();
		String year = dlg.cbYear.getText();
		String effectiveProposal = dlg.txtEffective.getText();
		String dcrNumber = dlg.txtDCRNumber.getText();
		String changeRequestType = dlg.cbChangeRequestType.getText();

		if (changeRequestType.compareTo("DCR") == 0) {
			if (dcrNumber.length() <= 3) {
				dlg.setMessage("Please input DCR number with prefix DCR.", IMessageProvider.WARNING);
				return;
			} else {
				if (dcrNumber.substring(0, 3).toUpperCase().compareTo("DCR") != 0) {
					dlg.setMessage("Please input DCR number with prefix DCR.", IMessageProvider.WARNING);
					return;
				}
			}
		}

		try {
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf6_program_model_name", program);
			itemDef.data.stringProps.put("vf6_shop", shop);
			itemDef.data.stringProps.put("vf6_sap_plant", plant);
			itemDef.data.stringProps.put("vf6_change_type", changeType);
			itemDef.data.stringProps.put("vf6_model_year", year);
			itemDef.data.stringProps.put("vf6_effective_proposal", effectiveProposal);
			itemDef.data.stringProps.put("vf6_change_request_type", changeRequestType);

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			itemRevisionDef.stringProps.put("vf6_Released_DCR_Number", dcrNumber);

			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "Vf6_ecr_id";
			idGen.stringProps.put("vf6_vehicle_type", program);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision mcnItemRevision = (TCComponentItemRevision) response.output[0].objects[2];
				if (cfgContext != null) {
					if (changeRequestType.compareTo("ECR/ECN") == 0 || changeRequestType.compareTo("MCR") == 0) {
						try {
							String type = implementItemRev.getType();
							if (type.compareTo("VF4_ChangeReq_EERevision") == 0 || type.compareTo("VF4_ChangeReq_ESRevision") == 0 || type.compareTo("VF4ChangeESmotorRevision") == 0) {
								mcnItemRevision.add("IMAN_requirement", new TCComponent[] { implementItemRev });
							} else {
								mcnItemRevision.add("CMImplements", new TCComponent[] { implementItemRev });
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					addParticipiant(session, TCExtension.getCurrentGroupMember(session), mcnItemRevision, "Analyst");
					addParticipiant(session, TCExtension.getCurrentGroupMember(session), mcnItemRevision, "ChangeSpecialist1");

					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item " + mcnItemRevision.getPropertyDisplayableValue("item_id") + " has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item " + mcnItemRevision.getPropertyDisplayableValue("item_id") + " has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getPlantFromShop(String shop) {
		try {
			return TCExtension.getDescriptionFromLOVValue(shop, "vf6_shop", "Vf6_MCN", session);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.cbProgram.deselectAll();
		dlg.cbShop.deselectAll();
		dlg.cbChangeType.deselectAll();
		dlg.cbYear.deselectAll();

		dlg.txtImplements.setText("");
		implementItemRev = null;

		dlg.txtDCRNumber.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.txtEffective.setText("");
		dlg.txtDCRNumber.setText("");
	}

	private void removeValueFromLinkedList(LinkedHashMap<String, String> linkedList, String key) {
		if (linkedList.containsKey(key))
			linkedList.remove(key);
	}

}
