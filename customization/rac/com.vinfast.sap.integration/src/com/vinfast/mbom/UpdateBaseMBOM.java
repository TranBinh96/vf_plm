package com.vinfast.mbom;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;
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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineInfo;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineResponse;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BOMLinesOutput;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.ItemLineInfo;
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
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
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
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;


public class UpdateBaseMBOM extends AbstractHandler {

	TCSession session;
	HashMap<String,String[]> templateToLoad = null;
	UpdateMBOMDialog dialog;
	private static final int COL_MAINGROUP = 0;
	private static final int COL_SUBGROUP = 1;
	private static final int COL_ID = 2;
	private static final int COL_MODGROUP = 3;
	private static final int COL_MAINMOD = 4;
	private static final int COL_MODNAME = 5;
	private static final int COL_QUANTITY = 6;
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
		// TODO Auto-generated constructor stub
		this.session = session;
		manufDMService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
		coreDMservice = DataManagementService.getService(session);
		strutService = StructureSearchService.getService(session);
		CADSMService = StructureManagementService.getService(session);
		BOMSMService = com.teamcenter.services.rac.bom.StructureManagementService.getService(session);
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		String commandName = "";
		try {
			IParameter cmdParamIsMEPart = event.getCommand().getParameter("com.teamcenter.vinfast.commands.manufacturing.shop");
			commandName = (cmdParamIsMEPart != null && cmdParamIsMEPart.getName() != null) ? cmdParamIsMEPart.getName() : "";
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentBOMLine selectedTarget = (TCComponentBOMLine)selectedObjects[0];
			session = selectedTarget.getSession();
			shopName = selectedTarget.getProperty(BL_OBJECT_NAME);
			dialog = new UpdateMBOMDialog(new Shell());
			if(!shopName.equals(commandName)) {
				MessageBox.post(dialog.getShell(), "Selected Shop is :"+shopName+" and selected option is :"+commandName, "Wrong Selection...", MessageBox.ERROR);
				return null;
			}
			dialog.create();
			dialog.bt_Update.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					try {
						String fileName = "VF_RECIPE_MBOM_SHOPS";
						TCComponent[] dataset = UIGetValuesUtility.quickSearch(session, new String[] {"Dataset Name"}, new String[] {fileName}, "Dataset Name");
						if(dataset == null) {
							MessageBox.post(dialog.getShell(), "Error loading template. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
							return;
						}
						TCComponentTcFile[] namedRefs = ((TCComponentDataset)dataset[0]).getTcFiles();
						if(namedRefs == null) {
							MessageBox.post(dialog.getShell(), "Structure template corrupted. Contact Teamcenter Admin", "Error", MessageBox.ERROR);
							return;
						}
						dialog.setMessage("MBOM Update started. Please wait...", IMessageProvider.INFORMATION);
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
									writeMessage("Fetching EBOM Context linked to MBOM");
									searchScope = getLinkedEBOM(selectedTarget);
									dialog.progressBar.setSelection(10);
									writeMessage("\nTraversing MBOM Structure...");
									FileDataCollector MBOMData = getMBOMStructureLevel3(selectedTarget);
									writeMessage("\nLoading MBOM Shop Template...");
									dialog.progressBar.setSelection(15);
									FileDataCollector updatedMBOM = readTCFile(namedRefs[0].getFmsFile(),shopName);
									writeMessage("\nStarted creating subgroups...");
									compareAndUpdateMBOM(selectedTarget, MBOMData, updatedMBOM);
									dialog.progressBar.setSelection(100);
									MessageBox.post(dialog.getShell(), "Updated completed Succesfully. Please verify data", "Success...", MessageBox.INFORMATION);
									dialog.getShell().dispose();
								} catch (TCException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					} catch (TCException e1) {
						// TODO Auto-generated catch block
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
			dialog.cb_Variant.setItems(new String[] {"Base","Premium"});
			dialog.cb_Variant.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					enableUpdate();
				}
			});
			dialog.open();
		} catch (NotDefinedException e) {
			MessageBox.post("Exeption: " + e.toString() + ".\n","ERROR",MessageBox.ERROR);
			return null;
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public TCComponentBOMLine getLinkedEBOM(TCComponentBOMLine selectedTarget) {
		TCComponentBOMLine ebomBOMLineTag= null;
		try {

			TCComponentBOMLine topLine = selectedTarget.window().getTopBOMLine();
			TCComponentBOMView BOMview = topLine.getBOMView();
			AIFComponentContext[] eboms = BOMview.getRelated(RL_IMAN_METarget);
			TCComponentBOMView ebomTag = (TCComponentBOMView) eboms[0].getComponent();
			OpenContextInfo[] inf0 = createContextViews(session, ebomTag, "VINFAST_WORKING_RULE");
			ebomBOMLineTag = (TCComponentBOMLine)inf0[0].context;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			if(RuleInfo != null) {
				revRule = new String[RuleInfo.length];
				int counter = 0;
				for (RevisionRuleInfo rule : RuleInfo) {
					revRule[counter] = rule.revRule.getProperty("object_name");
					counter++;
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

	public int getProgress(int current, int total) {

		float value = (float)current/(float)total;

		int percent = (int) (value*100);

		return percent;

	}

	public FileDataCollector getMBOMStructureLevel3(TCComponentBOMLine selectedTarget) {

		FileDataCollector MBOMData = null;
		try {
			ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
			ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
			levelInfo.parentBomLines = new TCComponentBOMLine[] { selectedTarget };
			levelInfo.excludeFilter = "None";
			levelPref.expItemRev = false;
			levelPref.info = new RelationAndTypesFilter[0];
			ExpandPSOneLevelResponse levelResp = CADSMService.expandPSOneLevel(levelInfo, levelPref);
			if (levelResp.output.length > 0) {
				MBOMData = new FileDataCollector();
				for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
					for (ExpandPSData psData: levelOut.children) {
						TCComponentBOMLine level1Child = psData.bomLine;
						String mainGroup = level1Child.getProperty(BL_OBJECT_NAME);
						MBOMData.setSubGroup(mainGroup, null);
						MBOMData.setBOMLine(mainGroup,"",level1Child);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return MBOMData;
	}

	private void enableUpdate() {

		if(dialog.cb_Variant.getText().isEmpty() || dialog.cb_Rule.getText().isEmpty()) {
			dialog.bt_Update.setEnabled(false);
		}else {
			dialog.setMessage("Click on \"Update\" to update MBOM", IMessageProvider.INFORMATION);
			dialog.bt_Update.setEnabled(true);
		}
	}

	public FileDataCollector readTCFile(File filePath, String sheet_Name) {
		FileDataCollector MBOMUpdateData = null;
		try  
		{  
			int rowNumber = 0;
			MBOMUpdateData = new FileDataCollector();
			FileInputStream fis = new FileInputStream(filePath);   //obtaining bytes from the file  
			//creating Workbook instance that refers to .xlsx file  
			XSSFWorkbook wb = new XSSFWorkbook(fis);   
			XSSFSheet sheet = wb.getSheet(sheet_Name);   //creating a Sheet object to retrieve object 
			if(sheet == null) {
				wb.close();
				MessageBox.post("Error in loading template. Please contact Teamcenter Admin", "Error", MessageBox.INFORMATION);
				return null;
			}
			Iterator<Row> rowItr = sheet.iterator();    //iterating over excel file  
			String mainGroup = "";
			String subGroup = "";
			while (rowItr.hasNext())                 
			{  
				Row row = rowItr.next();
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				DataMap map = new DataMap();
				String MG = getCellText(row.getCell(COL_MAINGROUP));
				if(MG.isEmpty()==false && mainGroup.equals(MG) == false) {
					mainGroup = MG;
				}
				
				String SG = getCellText(row.getCell(COL_SUBGROUP));
				if(SG.isEmpty()==false && subGroup.equals(SG) == false) {
					subGroup = SG;
				}
				String ID = getCellText(row.getCell(COL_ID));
				map.setID(ID);
				String MMG = getCellText(row.getCell(COL_MODGROUP));
				map.setModuleGroup(MMG);
				String MM = getCellText(row.getCell(COL_MAINMOD));
				if(MM.isEmpty()==false && subGroup.equals(MM) == false) {
					subGroup = MM;
				}
				map.setMainModule(MM);
				String MN = getCellText(row.getCell(COL_MODNAME));
				if(MN.isEmpty()==false && subGroup.equals(MN) == false) {
					subGroup = subGroup+"_"+MN;
				}
				map.setModuleName(MN);
				String QTY = getCellText(row.getCell(COL_QUANTITY));
				if(QTY.equals("")) {
					QTY = "1";
				}
				map.setLines(QTY);

				if(SG.isEmpty() == false || MN.isEmpty()== false) {
					MBOMUpdateData.setSubGroup(mainGroup, subGroup.replace("/", "_"));
					total++;
				}
				if(ID.isEmpty()) {
					MBOMUpdateData.setSubGroupData(mainGroup, subGroup.replace("/", "_"), map);
				}else {
					MBOMUpdateData.setSubGroupConsumables(mainGroup, subGroup.replace("/", "_"), ID);
				}
			}
			wb.close();
		}  
		catch(Exception e)  
		{  
			e.printStackTrace();  
		}
		return MBOMUpdateData;  

	}
	
	private String getCellText(Cell cell) {
		
		if(cell != null) {
			return cell.getStringCellValue();
		}
		return "";
	}

	public void compareAndUpdateMBOM(TCComponentBOMLine selectedTarget, FileDataCollector existingMBOM, FileDataCollector latestMBOM) {

		//Lines created
		ArrayList<String> existingLineKeys = existingMBOM.getShopLines(); //get old MG
		ArrayList<String> linesToCreate = new ArrayList<String>();
		linesToCreate.addAll(existingLineKeys);
		for(String line : latestMBOM.getShopLines()) {
			if(existingLineKeys.contains(line)) {
				linesToCreate.remove(line);
			}
		}
		System.out.println("Total Lines to create :-"+linesToCreate.size());
		if(!linesToCreate.isEmpty()) {
			HashMap<String, TCComponent> newLinesCreated = createNewItems(linesToCreate.toArray(new String[0]));
			if(newLinesCreated != null) {
				HashMap<String, TCComponent> newLinesBOMLine = addItemsToBOMLine(selectedTarget, newLinesCreated);
				for(String line : newLinesBOMLine.keySet()) {
					existingMBOM.setSubGroup(line, null);
					existingMBOM.setBOMLine(line,"", (TCComponentBOMLine)newLinesBOMLine.get(line));
				}
			}
		}else {
			dialog.progressBar.setSelection(20);
			for(String line : latestMBOM.getShopLines()) {
				HashMap<String, ArrayList<DataMap>> subGroups = latestMBOM.get(line);
				HashMap<String, TCComponent> newSubGroupsCreated = createNewItems(subGroups.keySet().toArray(new String[0]));
				if(newSubGroupsCreated != null) {
					HashMap<String, TCComponent> newSubGroupsBOMLine = addItemsToBOMLine(existingMBOM.getBOMLine(line, ""), newSubGroupsCreated);
					if(newSubGroupsBOMLine != null) {
						for(String subGroup : subGroups.keySet()) {
							counter++;
							dialog.progressBar.setSelection(status());
							writeMessage("Pull data for "+subGroup+" from EBOM to MBOM.....");
							existingMBOM.setSubGroup(line, subGroup);
							existingMBOM.setBOMLine(line, subGroup, (TCComponentBOMLine)newSubGroupsBOMLine.get(subGroup));//Updae BOMLine in existing map
							ArrayList<String> consumablesIDs = latestMBOM.getSubGroupConsumables(line, subGroup);
							if(consumablesIDs != null) {
								String queryList = "";
								for(String conID : consumablesIDs) {
									if(queryList.equals("")) {
										queryList = conID;
									}else {
										queryList = queryList+";"+conID;
									}
								}
								TCComponent[] items = UIGetValuesUtility.quickSearch(session, new String[] {"Item ID","Type"}, new String[] {queryList,"VF3_manuf_part"}, "Item...");
								if(items != null) {
									addItemsToBOMLine((TCComponentBOMLine)newSubGroupsBOMLine.get(subGroup), items);
								}
							}
							if(subGroups.get(subGroup) != null) {
								updateSubGroupRelated(subGroup, subGroups, existingMBOM.getBOMLine(line, subGroup));//Update Map
							}
						}
					}
				}
			}
		}
	}

	private HashMap<String, TCComponent> createNewItems(String[] names) {
		HashMap<String, TCComponent> revision = null;
		try {

			CreateIn[] itemDef = new CreateIn[names.length];
			for(int i= 0; i<names.length; i++) {
				itemDef[i] = new CreateIn();
				itemDef[i].clientId = names[i];
				itemDef[i].data.boName = "VF3_vfitem";
				itemDef[i].data.stringProps.put("object_name", names[i]);
				itemDef[i].data.stringProps.put("object_desc", names[i]);

				CreateInput revDef = new CreateInput();
				revDef.boName = "VF3_vfitemRevision";
				revDef.stringProps.put("object_desc", names[i]);
				itemDef[i].data.compoundCreateInput.put("revision", new CreateInput[] { revDef });
			}

			CreateResponse response = coreDMservice.createObjects(itemDef);
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				revision = new HashMap<String, TCComponent>(); 
				for(CreateOut output : response.output) {
					for (TCComponent rev : output.objects) {
						if (rev.getType().equals("VF3_vfitemRevision")) {
							revision.put(output.clientId, rev);
						}
					}
				}
			} else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						writeMessage("Exception: " + msg);
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return revision;
	}

	private void writeMessage(String message) {
		dialog.logText.append(message);
	}
	
	public void updateSubGroupRelated(String subGroupKeys, HashMap<String, ArrayList<DataMap>> subGroups, TCComponentBOMLine subGroupTagLine) {

		ArrayList<DataMap> dataMap = subGroups.get(subGroupKeys);

		for(DataMap map : dataMap) {

			String ID = map.getID();
			String moduleName = map.getModuleName();
			if(ID.isEmpty() == true && moduleName.isEmpty() == false) {
				TCComponent[] recipeLines = SearchStruture(map, searchScope);
				if(recipeLines != null) {
					TCComponent[] newObjectLines = pasteinMBOMFromEBOM(subGroupTagLine, recipeLines);
					markAsEndItem(newObjectLines);
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

			OccurrenceNoteExpression[] noteExp = new OccurrenceNoteExpression[4];
			noteExp[0] =  new OccurrenceNoteExpression();
			noteExp[0].noteType = "VL5_module_group";
			noteExp[0].queryOperator = "Equal";
			noteExp[0].values = new String[] {mainModuleGroup};

			noteExp[1] =  new OccurrenceNoteExpression();
			noteExp[1].noteType = "VL5_main_module";
			noteExp[1].queryOperator = "Equal";
			noteExp[1].values = new String[] {mainModule};

			noteExp[2] =  new OccurrenceNoteExpression();
			noteExp[2].noteType = "VL5_module_name";
			noteExp[2].queryOperator = "Equal";
			noteExp[2].values = new String[] {moduleName};

			noteExp[3] =  new OccurrenceNoteExpression();
			noteExp[3].noteType = "VL5_purchase_lvl_vf";
			noteExp[3].queryOperator = "Equal";
			noteExp[3].values = new String[] {"P"};

			SearchExpressionSet searchSet =  new SearchExpressionSet();
			searchSet.itemAndRevisionAttributeExpressions = new AttributeExpression[] {};
			searchSet.doTrushapeRefinement = false;
			searchSet.returnScopedSubTreesHit = false;
			searchSet.savedQueryExpressions =  new SavedQueryExpression[] {};
			searchSet.occurrenceNoteExpressions = noteExp;

			MFGSearchCriteria mfgCriteria = new MFGSearchCriteria();

			ArrayList<TCComponent> searchParts = new ArrayList<TCComponent>();

			StructureSearchResultResponse startResponse = strutService.startSearch(new TCComponent[] {searchScope}, searchSet, mfgCriteria);

			TCComponent cursor = startResponse.searchCursor;

			cursor = continueSearch(strutService, cursor, searchParts);

			strutService.stopSearch(cursor);

			if(!searchParts.isEmpty()) {

				foundObjects = new TCComponent[searchParts.size()]; 
				foundObjects = searchParts.toArray(foundObjects); 
			}

		} catch (ServiceException e1) {
			//TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
		
		coreDMservice.getProperties(objectLines, new String[] {PropertyDefines.BOM_VL5_PUR_LEVEL, PropertyDefines.BOM_VF3_PUR_LEVEL});
		for(TCComponent BOMline : objectLines) {
			
			TCComponentBOMLine BL = (TCComponentBOMLine) BOMline;
			try {
				if(BL.hasChildren() && (BL.getPropertyDisplayableValue(PropertyDefines.BOM_VL5_PUR_LEVEL).equals("P") || BL.getPropertyDisplayableValue(PropertyDefines.BOM_VL5_PUR_LEVEL).equals("H"))){
					
					BL.setProperty("Fnd0EndItem", "1");
					coreDMservice.refreshObjects2(new TCComponent[] {BL}, false);
				}
			} catch (NotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public TCComponent[] pasteinMBOMFromEBOM(TCComponentBOMLine target, TCComponent[] source) {

		Map<String,Boolean> boolValue = new HashMap<String,Boolean>();
		boolValue.put("occTypeFromPreferenceFlag", true);

		GeneralInfo gInfo = new GeneralInfo();
		gInfo.boolProps = boolValue;

		SourceInfo info =  new SourceInfo();
		info.sourceObjects = source;
		info.relationName = "";
		info.relationType = "";
		info.additionalInfo = gInfo;

		ConnectObjectsInputData copyData = new ConnectObjectsInputData();
		copyData.targetObjects = new TCComponent[] {target};
		copyData.sourceInfo = info;

		ConnectObjectResponse response = manufDMService.connectObjects(new ConnectObjectsInputData[] {copyData});
		
		if(response.serviceData.sizeOfPartialErrors() > 0) {
			System.out.println("Error adding line");
		}else {
			return response.newObjects;
		}
		
		return null;
	}

	private HashMap<String,TCComponent> addItemsToBOMLine(TCComponentBOMLine parentLine, HashMap<String,TCComponent> revisionsMap) {

		HashMap<String,TCComponent> shopLines = null;
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

			}else {
				shopLines = new HashMap<String,TCComponent>();
				for(BOMLinesOutput output : response.itemLines) {
					shopLines.put(output.clientId, output.bomline);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shopLines;
	}
	private void addItemsToBOMLine(TCComponentBOMLine parentLine, TCComponent[] itemMap) {

		try {
			
			int i = 0;
			AddOrUpdateChildrenToParentLineInfo[] childInfo =  new AddOrUpdateChildrenToParentLineInfo[itemMap.length];
			for(TCComponent itemTag : itemMap) {
				TCComponentItem item = (TCComponentItem) itemTag;
				ItemLineInfo itemInfo = new ItemLineInfo();
				itemInfo.clientId = item.getUid();
				itemInfo.item = item;
				itemInfo.itemRev = item.getLatestItemRevision();

				childInfo[i] = new AddOrUpdateChildrenToParentLineInfo();
				childInfo[i].items = new ItemLineInfo[] {itemInfo};
				childInfo[i].parentLine = parentLine;
				i++;
			}

			AddOrUpdateChildrenToParentLineResponse response = BOMSMService.addOrUpdateChildrenToParentLine(childInfo);
			if(response.serviceData.sizeOfPartialErrors() > 0) {

			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int status() {
		float value = (float)counter/(float)total;
		int status = (int) (value*80);
		return 20+status;
	}
}
