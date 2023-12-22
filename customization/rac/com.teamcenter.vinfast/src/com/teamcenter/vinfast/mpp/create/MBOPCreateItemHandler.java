package com.teamcenter.vinfast.mpp.create;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.dialog.CreateMPPItemsDialog;

public class MBOPCreateItemHandler extends AbstractHandler {
	
	private static String PROP_PROGRAM = "vf4_program_model_name_ar";

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		TCSession session = (TCSession)application.getSession();
		TCComponent selectedObject = (TCComponent) application.getTargetComponent();
		CreateMPPItemsDialog createDialog =  new CreateMPPItemsDialog(new Shell(), session, selectedObject);
		createDialog.create();
		Button create = createDialog.getCreateButton();
		create.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				
				switch(selectedObject.getTypeObject().getClassName()){    
				case "Folder": 
					TCComponentItemRevision revision = createItem(session, createDialog.getDescription(), createDialog.getDescription(), "Mfg0MEPlantBOP", "Mfg0MEPlantBOPRevision");
					if(revision != null) {
						try {
							revision.getTCProperty(PROP_PROGRAM).setStringValueArray(createDialog.getProgramCodeData());
							selectedObject.add("contents", revision.getItem());
							MessageBox.post("Plant BOP:"+revision.getProperty("object_string")+" Created Succesfully and added to selected folder.","Success",  MessageBox.INFORMATION);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.getDetailsMessage(),"Error",  MessageBox.ERROR);
							try {
								revision.getItem().delete();
							} catch (TCException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					break; 
				case "Mfg0BvrPlantBOP":
					revision = createItem(session, createDialog.getName(), createDialog.getDescription(), "Mfg0MEProcArea", "Mfg0MEProcAreaRevision");
					if(revision != null) {
						try {
							TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
							BOMLine.add(revision.getItem(), revision, null, false);
							MessageBox.post("Shop:"+revision.getProperty("object_string")+" Created Succesfully and added to BOMLine.","Success",  MessageBox.INFORMATION);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.getDetailsMessage(),"Error",  MessageBox.ERROR);
							try {
								revision.delete();
							} catch (TCException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					break; 
				case "Mfg0BvrProcessArea":
					revision = createItem(session, createDialog.getName(), createDialog.getDescription(), "Mfg0MEProcLine", "Mfg0MEProcLineRevision");
					if(revision != null) {
						try {
							TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
							BOMLine.add(revision.getItem(), revision, null, false);
							MessageBox.post("Line:"+revision.getProperty("object_string")+ " Created Succesfully and added to BOMLine.","Success",  MessageBox.INFORMATION);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.getDetailsMessage(),"Error",  MessageBox.ERROR);
							try {
								revision.delete();
							} catch (TCException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					break;
				case "Mfg0BvrProcessLine":
					revision = createItem(session, createDialog.getName(), createDialog.getDescription(), "Mfg0MEProcStatn", "Mfg0MEProcStatnRevision");
					if(revision != null) {
						try {
							TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
							BOMLine.add(revision.getItem(), revision, null, false);
							MessageBox.post("Station:"+revision.getProperty("object_string")+ " Created Succesfully and added to BOMLine.","Success",  MessageBox.INFORMATION);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.getDetailsMessage(),"Error",  MessageBox.ERROR);
							try {
								revision.delete();
							} catch (TCException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					break;
				default:     
					revision = createItem(session, createDialog.getName(), createDialog.getDescription(), "MEOP", "MEOPRevision");
					if(revision != null) {
						try {
							TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
							BOMLine.add(revision.getItem(), revision, null, false);
							MessageBox.post("Operation:"+revision.getProperty("object_string")+ " Created Succesfully and added to BOMLine.","Success",  MessageBox.INFORMATION);
						} catch (TCException e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.getDetailsMessage(),"Error",  MessageBox.ERROR);
							try {
								revision.delete();
							} catch (TCException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}   
			}
		});
		createDialog.open();
		
		return null;
		
	}

	private TCComponentItemRevision createItem(TCSession session, String name, String description, String itemType,String revType) {
		TCComponentItemRevision newItemRevision = null;

		DataManagementService dms = DataManagementService.getService(session);

		try {

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = itemType;
			itemDef.data.stringProps.put("object_name", name);
			//itemDef.data.stringProps.put("vf4_program_model_name", program);

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = revType;
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			ServiceData serviceData = response.serviceData;

			if (serviceData.sizeOfPartialErrors() > 0) {

				String errorMsg = SoaUtil.buildErrorMessage(serviceData);
				MessageBox.post(errorMsg, "Error", MessageBox.ERROR);

			} else {

				CreateOut[] createOutResp = response.output;
				TCComponent[] component = createOutResp[0].objects;
				for (TCComponent rev : component) {
					if (rev.getType().equals(revType)) {
						newItemRevision = (TCComponentItemRevision) rev;
						return newItemRevision;
					}
				}
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		}

		return null;
	}
}
