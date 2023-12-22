package com.vinfast.mbom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.administration.PreferenceManagementService;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.PreferenceValue;
import com.teamcenter.services.rac.bom.StructureManagementService;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineInfo;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineResponse;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BOMLinesOutput;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.ItemLineInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.PropDescriptorService;
import com.teamcenter.services.rac.core._2007_06.PropDescriptor.PropDescInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.AttachedPropDescsResponse;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDesc;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDescOutput2;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.vinfast.sap.util.PropertyDefines;


public class CreateBaseMBOM extends AbstractHandler {

	TCSession session;
	HashMap<String,String[]> templateToLoad = null;
	CreateBaseMBOMDialog dialog;
	DataManagementService coreDMService = null;
	StructureManagementService BOMSMService= null;
	String donorVehicle = null;
	String plant = null;
	String mbomType = null;
	Shell shell = null;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		try {

			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			if(selectedObjects.length > 1) {
				MessageBox.post("Please select only EBOM TopLine to create MBOM", "Error", MessageBox.ERROR);
				return null;
			}
			TCComponentBOMLine selectedTarget = (TCComponentBOMLine)selectedObjects[0];
			session = selectedTarget.getSession();
			dialog = new CreateBaseMBOMDialog(shell = new Shell());
			if(selectedTarget.parent() != null) {
				MessageBox.post("Please select EBOM TopLine to create MBOM", "Error", MessageBox.ERROR);
				return null;
			}
			dialog.create();
			dialog.bt_Create.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {

					coreDMService = DataManagementService.getService(session);
					BOMSMService = StructureManagementService.getService(session);
					donorVehicle = dialog.cb_Donor.getText();
					plant = dialog.cb_Plant.getText();
					mbomType = dialog.cb_Type.getText();
					shell.dispose();
					session.queueOperation( new AbstractAIFOperation( "Creating MBOM..." )
					{
						@Override
						public void executeOperation() throws Exception
						{
							TCComponentItemRevision revision = createMBOM(selectedTarget);
							if(revision != null) {
								TCComponentBOMLine topMBOMLine = createContextViews(revision);
								String[] shops = templateToLoad.keySet().toArray(new String[0]);
								HashMap<String,TCComponent> shopList = createNewItems(shops);
								HashMap<String,TCComponent> shopLines = addItemsToBOMLine(topMBOMLine, shopList);
								for(String shop : templateToLoad.keySet()) {
									String[] lines = templateToLoad.get(shop);
									HashMap<String,TCComponent> lineList = createNewItems(lines);
									addItemsToBOMLine((TCComponentBOMLine)shopLines.get(shop), lineList);
								}
								topMBOMLine.window().save();
								linkEBOMAndMBOM(topMBOMLine.getBOMView(), selectedTarget.getBOMView());
								closeContext(session, topMBOMLine);
								MessageBox.post("MBOM "+revision.getProperty(PropertyDefines.ITEM_OBJECT_STR)+" created successfully and open on View", "MBOM Created", MessageBox.INFORMATION);
								OpenCommand open = new OpenCommand(AIFUtility.getAIFPortal(), revision);
								open.executeModal();
							}
						}
					} );
				}
			});
			PropDescriptorService propService = PropDescriptorService.getService(session);
			//Combo Values
			String[] donorLOV = loadLovAttachments(propService, PropertyDefines.TYPE_DESIGN,PropertyDefines.ITEM_DONOR_VEHICLE2);
			if(donorLOV != null) {
				dialog.cb_Donor.setItems(donorLOV);
			}else {
				MessageBox.post(dialog.getShell(), "\"Donor Vehicle\" lov values failed to load. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;
			}
			dialog.cb_Donor.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					enableCreate();
				}
			});
			String[] plantLOV = loadLovAttachments(propService, PropertyDefines.TYPE_CN_ITEM,PropertyDefines.ECM_PLANT);
			if(plantLOV != null) {
				dialog.cb_Plant.setItems(plantLOV);
			}else {
				MessageBox.post(dialog.getShell(), "\"Plant\" lov values failed to load. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;
			}
			dialog.cb_Plant.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					enableCreate();
				}
			});

			PreferenceManagementService prefService = PreferenceManagementService.getService(session);
			String[] mbomTypes = getPreferenceValues(prefService, PropertyDefines.PREF_TOTAL_SHOPS_LIST);
			if(mbomTypes != null) {
				dialog.cb_Type.setItems(mbomTypes);
			}else {
				MessageBox.post(dialog.getShell(), "Identifing Shop structure preference failed. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;
			}
			String[] structureMapping = getPreferenceValues(prefService, PropertyDefines.PREF_MBOM_TEMPLATE);
			if(structureMapping == null) {
				MessageBox.post(dialog.getShell(), "Identifing template preference failed. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;
			}
			dialog.cb_Type.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					templateToLoad = loadAssemblyTemplate(dialog.cb_Type.getText().split("-")[0], structureMapping);
					enableCreate();
				}
			});
			dialog.open();
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	private void enableCreate() {

		if(dialog.cb_Donor.getText().isEmpty() || dialog.cb_Plant.getText().isEmpty() || dialog.cb_Type.getText().isEmpty()) {
			dialog.bt_Create.setEnabled(false);
		}else {
			dialog.bt_Create.setEnabled(true);
		}
	}
	private HashMap<String,String[]> loadAssemblyTemplate(String selectedMBOMType, String[] templateTypes) {
		HashMap<String,String[]> shopAndLine = new HashMap<String, String[]>();
		for(String template : templateTypes) {
			String[] shopLines = template.split("=");
			if(shopLines[0].equals(selectedMBOMType)) {
				String shopName = shopLines[1].split(":")[0];
				String[] lineNames = shopLines[1].split(":")[1].split(",");
				shopAndLine.put(shopName, lineNames);
			}
		}
		return shopAndLine;
	}

	private String[] loadLovAttachments(PropDescriptorService propService, String object, String property) {
		String[] lovAttachmentValues = null;
		try {
			PropDescInfo[] input = new PropDescInfo[1];
			input[0] = new PropDescInfo();
			input[0].typeName = object;
			input[0].propNames = new String[] {property};
			AttachedPropDescsResponse response = propService.getAttachedPropDescs2(input);
			Map<String,PropDescOutput2[]> output = response.inputTypeNameToPropDescOutput;
			for(String prop : output.keySet()) {
				PropDescOutput2[] values = output.get(prop);
				PropDesc desc = values[0].propertyDesc;
				TCComponentListOfValues lovvalues = desc.lov;
				return lovvalues.getListOfValues().getLOVDisplayValues();
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lovAttachmentValues;
	}

	private String[] getPreferenceValues(PreferenceManagementService prefService, String prefName) {
		String[] values= null;
		GetPreferencesResponse reponse = prefService.getPreferences(new String[] {prefName}, false);
		if(reponse.data.sizeOfPartialErrors() == 0) {
			CompletePreference[] prefValues = reponse.response;
			PreferenceValue valuesTag = prefValues[0].values;
			return valuesTag.values;
		}
		return values;
	}

	private TCComponentItemRevision createMBOM(TCComponentBOMLine target) {
		TCComponentItemRevision revision = null;
		try {
			if(target.parent() == null) {

				TCComponent[] context = target.getItem().getReferenceListProperty(PropertyDefines.REL_VARIANT_CONFIG);
				TCComponent configContext = context[0];
				String contextID = configContext.getProperty(PropertyDefines.ITEM_ID);
				HashMap<String, TCComponent> revisionMap = createNewItems(new String[] {contextID+"_"+plant+"_MBOM_"+donorVehicle.replace("/", "_")});
				if(revisionMap != null) {
					revision = (TCComponentItemRevision)revisionMap.get(contextID+"_"+plant+"_MBOM_"+donorVehicle.replace("/", "_"));
					revision.getItem().add(PropertyDefines.REL_VARIANT_CONFIG, configContext);
				}
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return revision;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private HashMap<String, TCComponent> createNewItems(String[] names) {
		HashMap<String, TCComponent> revision = null;
		try {

			CreateIn[] itemDef = new CreateIn[names.length];
			for(int i= 0; i<names.length; i++) {
				itemDef[i] = new CreateIn();
				itemDef[i].clientId = names[i];
				itemDef[i].data.boName = PropertyDefines.TYPE_VF_ITEM;
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_NAME, names[i]);
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_DESC, names[i]);

				CreateInput revDef = new CreateInput();
				revDef.boName = PropertyDefines.TYPE_VF_ITEM_REVISION;
				revDef.stringProps.put(PropertyDefines.ITEM_DESC, names[i]);
				itemDef[i].data.compoundCreateInput.put("revision", new CreateInput[] { revDef });
			}

			CreateResponse response = coreDMService.createObjects(itemDef);
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				revision = new HashMap<String, TCComponent>(); 
				for(CreateOut output : response.output) {
					for (TCComponent rev : output.objects) {
						if (rev.getType().equals(PropertyDefines.TYPE_VF_ITEM_REVISION)) {
							revision.put(output.clientId, rev);
						}
					}
				}
			} else {
				ServiceData serviceData = response.serviceData;
				if(serviceData.sizeOfPartialErrors()>0) {
					MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return revision;
	}

	public void linkEBOMAndMBOM(TCComponentBOMView MBOM, TCComponentBOMView EBOM) {
		try {
			MBOM.setRelated(PropertyDefines.REL_IMAN_METarget, new TCComponent[] {EBOM});
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TCComponentBOMLine createContextViews(TCComponent object) {

		TCComponentBOMLine info = null;
		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		TCComponentRevisionRule RevisionRule = getRevisionRule(PropertyDefines.REVISION_RULE_WORKING, session);
		com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput input = new com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = false;
		contextInput.openViews = false;
		contextInput.contextSettings = input;

		OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData serviceData = response.serviceData;
		if (serviceData.sizeOfPartialErrors() > 0) {
			MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);
			return null;
		} else {
			ContextGroup[] groups = response.output;
			for(OpenContextInfo views : groups[0].contexts) {
				if(views.context.getType().equals(PropertyDefines.TYPE_BOMLINE)) {
					info = (TCComponentBOMLine) views.context;
				}
			}
		}
		return info;
	}

	public static TCComponentRevisionRule getRevisionRule(String ruleObj, TCSession session) {
		TCComponentRevisionRule revRule = null;
		try {
			com.teamcenter.services.rac.cad.StructureManagementService structureService = com.teamcenter.services.rac.cad.StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;
			for (RevisionRuleInfo rule : RuleInfo) {
				if (rule.revRule.getProperty(PropertyDefines.ITEM_NAME).equals(ruleObj)) {
					revRule = rule.revRule;
					break;
				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return revRule;
	}

	public void closeContext(TCSession session, TCComponent window) {

		com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		dmService.closeContexts(new TCComponent[] { window });
	}

	private HashMap<String,TCComponent> addItemsToBOMLine(TCComponentBOMLine parentLine, HashMap<String,TCComponent> revisionsMap) {
		HashMap<String,TCComponent> shopLines = revisionsMap;
		try {
			int i = 0;
			AddOrUpdateChildrenToParentLineInfo[] childInfo =  new AddOrUpdateChildrenToParentLineInfo[revisionsMap.size()];
			for(String shop : revisionsMap.keySet()) {
				TCComponentItemRevision revision = (TCComponentItemRevision)revisionsMap.get(shop);
				ItemLineInfo itemInfo = new ItemLineInfo();
				itemInfo.clientId = shop;
				itemInfo.item = revision.getItem();
				itemInfo.itemRev = revision;

				childInfo[i] = new AddOrUpdateChildrenToParentLineInfo();
				childInfo[i].items = new ItemLineInfo[] {itemInfo};
				childInfo[i].parentLine = parentLine;
				i++;
			}
			AddOrUpdateChildrenToParentLineResponse response = BOMSMService.addOrUpdateChildrenToParentLine(childInfo);
			if(response.serviceData.sizeOfPartialErrors()>0) {
				MessageBox.post(SoaUtil.buildErrorMessage(response.serviceData), "Error", MessageBox.ERROR);
			}else {
				shopLines.clear();
				for(BOMLinesOutput output : response.itemLines) {
					revisionsMap.put(output.clientId, output.bomline);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shopLines;
	}
}
