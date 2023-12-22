package com.teamcenter.vinfast.aftersale.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing.StructureSearchService;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.AttributeExpression;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.MFGSearchCriteria;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.OccurrenceNoteExpression;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.SavedQueryExpression;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.SearchExpressionSet;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.StructureSearchResultResponse;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.teamcenter.services.rac.manufacturing._2012_02.DataManagement.ConnectObjectResponse;
import com.teamcenter.services.rac.manufacturing._2012_02.DataManagement.ConnectObjectsInputData;
import com.teamcenter.services.rac.manufacturing._2012_02.DataManagement.GeneralInfo;
import com.teamcenter.services.rac.manufacturing._2012_02.DataManagement.SourceInfo;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;
import com.vinfast.sc.utilities.PropertyDefines;
import com.vinfast.sc.utilities.Utilities;

public class ServiceStructureUpdate_Handler extends AbstractHandler {

	TCSession session;
	HashMap<String, String[]> templateToLoad = null;
	CreateUpdateSBOMDialog dialog;
	// private static final int COL_MAINGROUP = 0;
	// private static final int COL_SUBGROUP = 1;
	// private static final int COL_ID = 2;
	// private static final int COL_MODGROUP = 7;
	// private static final int COL_MAINMOD = 8;
	// private static final int COL_MODNAME = 9;
	// private static final int COL_QUANTITY = 10;
	private static final String BL_OBJECT_NAME = "bl_rev_object_name";
	private static final String RL_IMAN_METarget = "IMAN_METarget";
	TCComponentBOMLine searchScope = null;
	DataManagementService coreDMservice = null;
	static StructureSearchService strutService = null;
	StructureManagementService CADSMService = null;
	com.teamcenter.services.rac.bom.StructureManagementService BOMSMService = null;
	static com.teamcenter.services.rac.manufacturing.DataManagementService manufDMService = null;
	int counter = 1;
	int progress = 20;
	int total = 0;
	String shopName;

	public void loadServices(TCSession session) {
		this.session = session;
		manufDMService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		coreDMservice = DataManagementService.getService(session);
		strutService = StructureSearchService.getService(session);
		CADSMService = StructureManagementService.getService(session);
		BOMSMService = com.teamcenter.services.rac.bom.StructureManagementService.getService(session);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentBOMLine selectedTarget = (TCComponentBOMLine) selectedObjects[0];
			session = selectedTarget.getSession();
			shopName = selectedTarget.getProperty(BL_OBJECT_NAME);
			dialog = new CreateUpdateSBOMDialog(new Shell());
			dialog.create();
			dialog.bt_Update.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					try {
						String fileName = "VF_RECIPE_MBOM_SHOPS";
						LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
						inputQuery.put("Dataset Name", fileName);
						TCComponent[] dataset = Query.queryItem(session, inputQuery, "Dataset Name");
						if (dataset == null) {
							MessageBox.post(dialog.getShell(), "Error loading template. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
							return;
						}
						TCComponentTcFile[] namedRefs = ((TCComponentDataset) dataset[0]).getTcFiles();
						if (namedRefs == null) {
							MessageBox.post(dialog.getShell(), "Structure template corrupted. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
							return;
						}
						dialog.setMessage("SBOM Update started. Please wait...", IMessageProvider.INFORMATION);
						dialog.progressBar.setVisible(true);
						dialog.logText.setVisible(true);
						dialog.getShell().setSize(new Point(400, 500));
						dialog.getShell().redraw();
						dialog.progressBar.setMaximum(100);
						dialog.progressBar.setSelection(5);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									manufDMService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
									coreDMservice = DataManagementService.getService(session);
									strutService = StructureSearchService.getService(session);
									CADSMService = StructureManagementService.getService(session);
									BOMSMService = com.teamcenter.services.rac.bom.StructureManagementService.getService(session);
									writeMessage("Fetching EBOM Context linked to SBOM");
									searchScope = getLinkedEBOM(selectedTarget);
									dialog.progressBar.setSelection(10);
									writeMessage("\nTraversing SBOM Structure...");
									TreeNode SBOMData = getMBOMStructureLevel3(selectedTarget);
									writeMessage("\nLoading SBOM Shop Template...");
									dialog.progressBar.setSelection(15);
									FileDataCollector updatedSBOM = new FileDataCollector(namedRefs[0].getFmsFile(), "SBOM");
									writeMessage("\nStarted creating subgroups...");
									compareAndUpdateSBOM(SBOMData, updatedSBOM);
									dialog.progressBar.setSelection(100);
									MessageBox.post(dialog.getShell(), "Updated completed Succesfully. Please verify data", "Success...", MessageBox.INFORMATION);
									dialog.getShell().dispose();
								} catch (TCException e) {
									e.printStackTrace();
								}
							}
						});
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
			});

			dialog.cb_Rule.setItems(getRevisionRule(session));
			dialog.cb_Rule.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					enableUpdate();
				}
			});
			dialog.open();
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public TCComponentBOMLine getLinkedEBOM(TCComponentBOMLine selectedTarget) {
		TCComponentBOMLine ebomBOMLineTag = null;
		try {

			TCComponentBOMLine topLine = selectedTarget.window().getTopBOMLine();
			TCComponentBOMView BOMview = topLine.getBOMView();
			AIFComponentContext[] eboms = BOMview.getRelated(RL_IMAN_METarget);
			TCComponentBOMView ebomTag = (TCComponentBOMView) eboms[0].getComponent();
			OpenContextInfo[] inf0 = createContextViews(session, ebomTag, "VINFAST_WORKING_RULE");
			ebomBOMLineTag = (TCComponentBOMLine) inf0[0].context;
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ebomBOMLineTag;
	}

	public String[] getRevisionRule(TCSession session) {
		String[] revRule = null;
		try {
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;
			if (RuleInfo != null) {
				revRule = new String[RuleInfo.length];
				int counter = 0;
				for (RevisionRuleInfo rule : RuleInfo) {
					revRule[counter] = rule.revRule.getProperty("object_name");
					counter++;
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return revRule;
	}

	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}

	public TreeNode getMBOMStructureLevel3(TCComponentBOMLine selectedTarget) {
		TreeNode rootNode = null;
		try {
			String name = selectedTarget.getProperty(BL_OBJECT_NAME);
			rootNode = new TreeNode(name);
			rootNode.setNodeTag(selectedTarget);
			traveseStructure(rootNode, selectedTarget);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootNode;
	}

	private void traveseStructure(TreeNode parentNode, TCComponentBOMLine selectedTarget) {

		try {
			ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
			ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
			levelInfo.parentBomLines = new TCComponentBOMLine[] { selectedTarget };
			levelInfo.excludeFilter = "None";
			levelPref.expItemRev = false;
			levelPref.info = new RelationAndTypesFilter[0];
			ExpandPSOneLevelResponse levelResp = CADSMService.expandPSOneLevel(levelInfo, levelPref);
			if (levelResp.output.length > 0) {
				for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
					for (ExpandPSData psData : levelOut.children) {
						TCComponentBOMLine level1Child = psData.bomLine;
						int level = level1Child.getIntProperty(PropertyDefines.BOM_LEVEL);
						String name = level1Child.getProperty(BL_OBJECT_NAME);
						TreeNode node = new TreeNode(name);
						node.setNodeTag(level1Child);
						parentNode.addChild(node);
						if (level1Child.hasChildren() == true && level < 3) {
							traveseStructure(node, level1Child);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void enableUpdate() {

		if (dialog.cb_Rule.getText().isEmpty()) {
			dialog.bt_Update.setEnabled(false);
		} else {
			dialog.setMessage("Click on \"Update\" to update SBOM", IMessageProvider.INFORMATION);
			dialog.bt_Update.setEnabled(true);
		}
	}

	public void compareAndUpdateSBOM(TreeNode existingSBOM, FileDataCollector latestSBOM) {
		// Lines created
		dialog.progressBar.setSelection(15);
		String parent = existingSBOM.getValue();
		if (existingSBOM.hasChilds()) {
			ArrayList<TreeNode> childs = existingSBOM.getChildren();
			for (TreeNode node : childs) {
				String child = node.getValue();
				if (node.hasChilds()) {
					compareAndUpdateSBOM(node, latestSBOM);
				} else {
					ArrayList<DataMap> map = latestSBOM.getDataMap(parent, child);
					if (map != null) {
						writeMessage("\n Line :" + child);
						TCComponent BOMLine = node.getNodeTag();
						updateSubGroupRelated((TCComponentBOMLine) BOMLine, map);
					}
				}
			}
		}
	}

	private void writeMessage(String message) {
		dialog.logText.append(message);
	}

	public void updateSubGroupRelated(TCComponentBOMLine tagLine, ArrayList<DataMap> dataMap) {

		for (DataMap map : dataMap) {

			String moduleName = map.getModuleName();
			if (moduleName.isEmpty() == false) {
				TCComponent[] recipeLines = SearchStruture(map, searchScope);
				if (recipeLines != null) {
					pasteinSBOMFromEBOM(tagLine, recipeLines);
				}
			}
		}
	}

	public TCComponent[] SearchStruture(DataMap dataMap, TCComponent searchScope) {
		TCComponent[] foundObjects = null;

		try {

			String mainModuleGroup = dataMap.getModuleGroup();
			String mainModule = dataMap.getMainModule();
			String moduleName = dataMap.getModuleName();

			OccurrenceNoteExpression[] noteExp = new OccurrenceNoteExpression[3];
			noteExp[0] = new OccurrenceNoteExpression();
			noteExp[0].noteType = "VL5_module_group";
			noteExp[0].queryOperator = "Equal";
			noteExp[0].values = new String[] { mainModuleGroup };

			noteExp[1] = new OccurrenceNoteExpression();
			noteExp[1].noteType = "VL5_main_module";
			noteExp[1].queryOperator = "Equal";
			noteExp[1].values = new String[] { mainModule };

			noteExp[2] = new OccurrenceNoteExpression();
			noteExp[2].noteType = "VL5_module_name";
			noteExp[2].queryOperator = "Equal";
			noteExp[2].values = new String[] { moduleName };

			TCComponent query = Utilities.findSavedQuery(session, "__hasIsAftersalesRelevant");
			SavedQueryExpression savedQuery = new SavedQueryExpression();
			savedQuery.savedQuery = query;
			savedQuery.entries = new String[] { "vf4_itm_after_sale_relevant" };
			savedQuery.values = new String[] { "TRUE" };

			SearchExpressionSet searchSet = new SearchExpressionSet();
			searchSet.itemAndRevisionAttributeExpressions = new AttributeExpression[] {};
			searchSet.doTrushapeRefinement = false;
			searchSet.returnScopedSubTreesHit = false;
			searchSet.savedQueryExpressions = new SavedQueryExpression[] { savedQuery };
			searchSet.occurrenceNoteExpressions = noteExp;

			MFGSearchCriteria mfgCriteria = new MFGSearchCriteria();

			ArrayList<TCComponent> searchParts = new ArrayList<TCComponent>();

			StructureSearchResultResponse startResponse = strutService.startSearch(new TCComponent[] { searchScope }, searchSet, mfgCriteria);

			TCComponent cursor = startResponse.searchCursor;

			cursor = continueSearch(strutService, cursor, searchParts);

			strutService.stopSearch(cursor);

			if (!searchParts.isEmpty()) {
				foundObjects = new TCComponent[searchParts.size()];
				foundObjects = searchParts.toArray(foundObjects);
			}
		} catch (ServiceException e1) {
			e1.printStackTrace();
		}

		return foundObjects;
	}

	public static TCComponentRevisionRule getRevisionRule(String ruleObj, TCSession session) {

		TCComponentRevisionRule revRule = null;
		try {
			StructureManagementService structureService = StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules = structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;
			for (RevisionRuleInfo rule : RuleInfo) {
				if (rule.revRule.getProperty("object_name").equals(ruleObj)) {
					revRule = rule.revRule;
					break;
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return revRule;
	}

	public static TCComponent continueSearch(StructureSearchService strutService, TCComponent cursor, ArrayList<TCComponent> searchParts) {

		StructureSearchResultResponse nextResponse;
		TCComponent nextcursor = null;
		try {
			nextResponse = strutService.nextSearch(cursor);
			nextcursor = nextResponse.searchCursor;
			TCComponent[] objects = nextResponse.objects;
			if (nextResponse.finished == false) {
				if (objects.length > 0) {
					for (TCComponent object : objects) {
						searchParts.add(object);
					}
				}
				continueSearch(strutService, nextcursor, searchParts);
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return nextcursor;
	}

	public static OpenContextInfo[] createContextViews(TCSession session, TCComponent object, String rule) {

		OpenContextInfo[] info = null;
		// DRYRUN __ADMIN_Only_Approved
		TCComponentRevisionRule RevisionRule = getRevisionRule(rule, session);
		com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput input = new com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput();
		input.tagProps.put("RevisionRule", RevisionRule);

		OpenContextInput contextInput = new OpenContextInput();
		contextInput.object = object;
		contextInput.openAssociatedContexts = true;
		contextInput.openViews = true;
		contextInput.contextSettings = input;

		OpenContextsResponse response = manufDMService.openContexts(new OpenContextInput[] { contextInput });
		ServiceData sd = response.serviceData;

		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 1; inx < errorValue.length; inx++) {
				System.out.println(errorValue[inx].getMessage());
			}
		} else {
			ContextGroup[] groups = response.output;
			info = groups[0].contexts;
		}
		return info;
	}

	public void markAsEndItem(TCComponent[] objectLines) {
		coreDMservice.getProperties(objectLines, new String[] { PropertyDefines.BOM_VL5_PUR_LEVEL, PropertyDefines.BOM_VF3_PUR_LEVEL });
		for (TCComponent object : objectLines) {
			TCComponentBOMLine bomline = (TCComponentBOMLine) object;
			try {
				String purchaseLevel = bomline.getPropertyDisplayableValue(PropertyDefines.BOM_VL5_PUR_LEVEL);
				if (bomline.hasChildren() && (purchaseLevel.equals(PropertyDefines.PUR_LEVEL_P) || purchaseLevel.equals(PropertyDefines.PUR_LEVEL_H) || purchaseLevel.equals(PropertyDefines.PUR_LEVEL_AXS))) {
					bomline.setProperty("Fnd0EndItem", "1");
					coreDMservice.refreshObjects2(new TCComponent[] { bomline }, false);
				}
			} catch (NotLoadedException e) {
				e.printStackTrace();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	public TCComponent[] pasteinSBOMFromEBOM(TCComponentBOMLine target, TCComponent[] source) {

		Map<String, Boolean> boolValue = new HashMap<String, Boolean>();
		boolValue.put("occTypeFromPreferenceFlag", true);

		GeneralInfo gInfo = new GeneralInfo();
		gInfo.boolProps = boolValue;

		SourceInfo info = new SourceInfo();
		info.sourceObjects = source;
		info.relationName = "";
		info.relationType = "";
		info.additionalInfo = gInfo;

		ConnectObjectsInputData copyData = new ConnectObjectsInputData();
		copyData.targetObjects = new TCComponent[] { target };
		copyData.sourceInfo = info;

		ConnectObjectResponse response = manufDMService.connectObjects(new ConnectObjectsInputData[] { copyData });

		if (response.serviceData.sizeOfPartialErrors() > 0) {
			System.out.println("Error adding line");
		} else {
			return response.newObjects;
		}

		return null;
	}

	public int status() {
		float value = (float) counter / (float) total;
		int status = (int) (value * 80);
		return 20 + status;
	}
}
