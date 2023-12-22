package com.teamcenter.vinfast.report.bomcomparision;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.Query;

public class BOMComparisonHandler extends AbstractHandler {
	private TCSession session;
	private BOMComparisonDialog dialog = null;
	private LinkedHashMap<String, TCComponentRevisionRule> revRule = null;
	private TCComponentItemRevision oldRevision = null;
	private TCComponentItemRevision newRevision = null;
	private static Logger LOGGER = null;
	private static Map<String,String> prop2Compare = null;
	private static SXSSFWorkbook wb;
	private static String TEMP_DIR;
	
	public static class Diff {
		String changeType = "";
		String desc = "";
	}
	
	
	public BOMComparisonHandler() {
		super();
		// ---------------------------- Init member variable-------------------------------------
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.dialog = new BOMComparisonDialog(new Shell());
		this.revRule = Utils.getRevisionRule2(session);
		prop2Compare = new LinkedHashMap<>();
		prop2Compare.put("bl_level_starting_0","Level");
		prop2Compare.put("bl_item_item_id","Item Id");
		prop2Compare.put("bl_rev_item_revision_id","Revision");
		prop2Compare.put("bl_item_object_name","Item Name");
		prop2Compare.put("vf4_DsnRev_donor_veh_type","Donor Vehicle");
		prop2Compare.put("VL5_torque_inf","Torque Information");
		prop2Compare.put("VL5_purchase_lvl_vf","Purchase Level Vinfast");
		prop2Compare.put("bl_item_vf4_orginal_part_number","Original Base Part Number");
		prop2Compare.put("VL5_pos_id","POS ID");
		prop2Compare.put("bl_formula","Variant Formula");
		prop2Compare.put("VL5_mounting_comment","Mounting Comment");
		prop2Compare.put("vf4_catia_material","Mounting Comment");
		prop2Compare.put("bl_item_vf4_item_make_buy","Part Make/Buy");
		prop2Compare.put("bl_quantity","Quantity");
		prop2Compare.put("bl_item_uom_tag","Default Unit of Measure");
		prop2Compare.put("bl_item_vl5_color_code","Color Code");
		prop2Compare.put("bl_item_vf4_itm_after_sale_relevant","Is After Sale Relevant");
		prop2Compare.put("bl_rev_vf4_cad_thickness","CAD Thickness");
		prop2Compare.put("bl_rev_vf4_cad_weight","CAD Weight");
		prop2Compare.put("bl_item_vf4_item_is_traceable","Part Traceability Indicator");
		prop2Compare.put("Change Type","Change Type");
		prop2Compare.put("VL5_module_group","Module Group English");
		prop2Compare.put("VL5_main_module","Main Module English");
		prop2Compare.put("VL5_module_name","Module Name");
		prop2Compare.put("bl_rev_vl5_colored_part","Colored Part");
		prop2Compare.put("Description","Description");
		TEMP_DIR 			= System.getenv("tmp");
		LOGGER = Logger.getLogger(this.getClass());
		// ---------------------------- Init member variable-------------------------------------
	}
	
	public TCComponentItemRevision getOldRevision() {
		return oldRevision;
	}

	public void setOldRevision(TCComponentItemRevision oldRevision) {
		this.oldRevision = oldRevision;
	}

	public TCComponentItemRevision getNewRevision() {
		return newRevision;
	}

	public void setNewRevision(TCComponentItemRevision newRevision) {
		this.newRevision = newRevision;
	}

	public BOMComparisonDialog getDialog() {
		return dialog;
	}
	
	public LinkedHashMap<String, TCComponentRevisionRule> getRevRule() {
		return revRule;
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		BOMComparisonHandler handler = new BOMComparisonHandler();
		handler.getDialog().create();
		if(handler.getRevRule() != null && handler.getRevRule().size() > 0) {
			handler.getDialog().getCbOldRevRule().setItems(getRevRule().keySet().toArray(new String[getRevRule().keySet().size()]));
			handler.getDialog().getCbNewRevRule().setItems(getRevRule().keySet().toArray(new String[getRevRule().keySet().size()]));
		}
		
		handler.getDialog().setCbNewRevRuleText("VINFAST_WORKING_RULE");
		handler.getDialog().setCbOldRevRuleText("VINFAST_REVIEW_RULE");
		
		handler.getDialog().getBtnAddOldBom().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					SearchDesignRevDialog searchDlg = new SearchDesignRevDialog(handler.getDialog().getShell(), handler.getDialog().getShell().getStyle());
					searchDlg.open();
					Button btnOK = searchDlg.getOKButton();

					btnOK.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = searchDlg.getSearchTable();
							TableItem[] items = partTable.getSelection();
							if(searchDlg.getMapDesignRevCompo().containsKey(items[0].getText())) {
								handler.getDialog().getTxtOldBomRev().setText(items[0].getText());
								handler.setOldRevision((TCComponentItemRevision) searchDlg.getMapDesignRevCompo().get(items[0].getText()));
							}
							searchDlg.getShell().dispose();
						}
					});
					
				} catch (Exception e1) {
					MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
					LOGGER.error(e1.toString());
				}
			}
		});
		
		handler.getDialog().getBtnAddNewBom().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					SearchDesignRevDialog searchDlg = new SearchDesignRevDialog(handler.getDialog().getShell(), handler.getDialog().getShell().getStyle());
					searchDlg.open();
					Button btnOK = searchDlg.getOKButton();

					btnOK.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = searchDlg.getSearchTable();
							TableItem[] items = partTable.getSelection();
							if(searchDlg.getMapDesignRevCompo().containsKey(items[0].getText())) {
								handler.getDialog().getTxtNewBomRev().setText(items[0].getText());
								handler.setNewRevision((TCComponentItemRevision) searchDlg.getMapDesignRevCompo().get(items[0].getText()));
							}
							searchDlg.getShell().dispose();
						}
					});
					
				} catch (Exception e1) {
					MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
					LOGGER.error(e1.toString());
				}
			}
		});
		
		handler.getDialog().getBtnOK().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(handler.getDialog().getTxtNewBomRev().getText().isEmpty() ||
						handler.getDialog().getTxtOldBomRev().getText().isEmpty() ||
						handler.getDialog().getCbNewRevRule().getText().isEmpty() ||
						handler.getDialog().getCbOldRevRule().getText().isEmpty()) {
					MessageBox.post("Please input all information !!!", "WARNING", MessageBox.WARNING);
					return;
				}
				
				String oldId = handler.getDialog().getTxtOldBomRev().getText().split("/")[0];
				String newId = handler.getDialog().getTxtNewBomRev().getText().split("/")[0];
				if(oldId.compareToIgnoreCase(newId) != 0) {
					MessageBox.post("Old BOM ID and New BOM ID must be same !!!", "WARNING", MessageBox.WARNING);
					return;
				}
				//TODO call function expand bom
				TCComponentBOMLine oldRootLine = handler.getRootLine(handler.getOldRevision(), handler.getDialog().getCbOldRevRule().getText());
				TCComponentBOMLine newRootLine = handler.getRootLine(handler.getNewRevision(), handler.getDialog().getCbNewRevRule().getText());
				
				List<TCComponentBOMLine> oldBom = new LinkedList<TCComponentBOMLine>();
				List<TCComponentBOMLine> newBom = new LinkedList<TCComponentBOMLine>();
				try {
					
					// Create a new ProgressMonitorDialog
					ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(handler.getDialog().getShell());
					progressDialog.open();
					progressDialog.getProgressMonitor().beginTask("Processing...", 1000000);
					// Run the function in a background thread
					Thread backgroundThread = new Thread(() -> {
					    try {
					        handler.expandBOM(oldRootLine, oldBom);
					        Display.getDefault().asyncExec(() -> progressDialog.getProgressMonitor().worked(200000));
					        
							handler.expandBOM(newRootLine, newBom);
							Display.getDefault().asyncExec(() -> progressDialog.getProgressMonitor().worked(200000));
							
							LinkedHashMap<TCComponentBOMLine, Diff> compareRes = handler.compareBOM(oldBom, newBom);
							Display.getDefault().asyncExec(() -> progressDialog.getProgressMonitor().worked(400000));
							
							excelPublishReport(compareRes);
							Display.getDefault().asyncExec(() -> progressDialog.getProgressMonitor().worked(200000));
							
					    } catch (Exception e) {
					        e.printStackTrace();
					    }
					});
					backgroundThread.start();
					backgroundThread.join();
					progressDialog.close();
					
//					handler.expandBOM(oldRootLine, oldBom);
//					handler.expandBOM(newRootLine, newBom);
//					LinkedHashMap<TCComponentBOMLine, Diff> compareRes = handler.compareBOM(oldBom, newBom);
//					excelPublishReport(compareRes);
					handler.dialog.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		handler.dialog.open();
		return null;
	}
	
	private TCComponentBOMLine getRootLine(TCComponentItemRevision revision, String ruleName) {
		TCComponentRevisionRule revRule = this.revRule.get(ruleName);
		TCComponentBOMLine rootLine = Utils.createBOMWindow(session, revision, revRule);
		return rootLine;
	}
	
	private void expandBOM(TCComponentBOMLine parentLine, List<TCComponentBOMLine> bomlines) throws Exception{
		TcResponseHelper response = TcBOMService.expand(session, parentLine);
		TCComponentBOMLine[] childrenLines =  Arrays.asList(response.getReturnedObjects()).toArray(new TCComponentBOMLine[0]);
		bomlines.add(parentLine);
		if(childrenLines != null && childrenLines.length > 0) {
			for(TCComponentBOMLine childLine : childrenLines) {
				if(childLine.getChildrenCount() > 0) {
					expandBOM(childLine, bomlines);
				} else {
					bomlines.add(childLine);
				}
			}
		}
	}
	
	private LinkedHashMap<TCComponentBOMLine, Diff> compareBOM(List<TCComponentBOMLine> oldBOM, List<TCComponentBOMLine> newBOM) throws NotLoadedException, TCException {
		LinkedHashMap<TCComponentBOMLine, Diff> compareRes = new LinkedHashMap<TCComponentBOMLine, Diff>();
		LinkedHashMap<String, List<TCComponentBOMLine>> parent2ChildOldBom = new LinkedHashMap<String, List<TCComponentBOMLine>>();
		LinkedHashMap<String, List<TCComponentBOMLine>> parent2ChildNewBom = new LinkedHashMap<String, List<TCComponentBOMLine>>();
		LinkedHashMap<String, Integer> parentIdNewChildIdandQuantity = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> parentIdOldChildIdandQuantity = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> parentIdOldChildIdFindNoAndQuantity = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> parentIdNewChildIdFindNoAndQuantity = new LinkedHashMap<String, Integer>();
		
		LinkedHashMap<String, List<TCComponentBOMLine>> parent2SameChildOldBom = new LinkedHashMap<String, List<TCComponentBOMLine>>();
		LinkedHashMap<String, List<TCComponentBOMLine>> parent2SameChildNewBom = new LinkedHashMap<String, List<TCComponentBOMLine>>();
		
		//key1: parentId+childId, key2: index
//		MultiKeyMap<String, TCComponentBOMLine> parent2OldChildIndex = new MultiKeyMap();
//		MultiKeyMap<String, TCComponentBOMLine> parent2NewChildIndex = new MultiKeyMap();
		//create mapping bl_clone_stable_occurrence_id-newBOM
		LinkedHashMap<String, TCComponentBOMLine> occId2NewBom = new LinkedHashMap<String, TCComponentBOMLine>();
		for(TCComponentBOMLine aLine : newBOM) {
			String occId = aLine.getPropertyDisplayableValue("bl_clone_stable_occurrence_id");
			String itemId = aLine.getPropertyDisplayableValue("bl_item_item_id");
			//create map parent2ChildOldBom
			List<TCComponentBOMLine> lstChildBomLine = new ArrayList<TCComponentBOMLine>();
			if(!parent2ChildNewBom.containsKey(itemId) && aLine.getChildrenCount() > 0) {
				AIFComponentContext[] childLine = aLine.getChildren();
				for(AIFComponentContext aifCon : childLine) {
					TCComponentBOMLine bl = (TCComponentBOMLine)aifCon.getComponent();
					lstChildBomLine.add(bl);
					String childItemId = bl.getPropertyDisplayableValue("bl_item_item_id");
					String findNo = bl.getPropertyDisplayableValue("bl_sequence_no");
					StringBuilder itemIdChildId = new StringBuilder().append(itemId).append(childItemId);
					StringBuilder itemIdChildIdFindno = new StringBuilder().append(itemId).append(childItemId).append(findNo);
					
					if(!parentIdNewChildIdandQuantity.containsKey(itemIdChildId.toString())) {
						parentIdNewChildIdandQuantity.put(itemIdChildId.toString(), 1);
					}else {
						parentIdNewChildIdandQuantity.put(itemIdChildId.toString(), parentIdNewChildIdandQuantity.get(itemIdChildId.toString()) + 1);
					}
					
					if(!parentIdNewChildIdFindNoAndQuantity.containsKey(itemIdChildIdFindno.toString())) {
						parentIdNewChildIdFindNoAndQuantity.put(itemIdChildIdFindno.toString(), 1);
					}else {
						parentIdNewChildIdFindNoAndQuantity.put(itemIdChildIdFindno.toString(), parentIdNewChildIdFindNoAndQuantity.get(itemIdChildIdFindno.toString()) + 1);
					}
					
					if(!parent2SameChildNewBom.containsKey(itemIdChildId.toString())) {
						List<TCComponentBOMLine> lstSameChildBomLine = new ArrayList<TCComponentBOMLine>();
						lstSameChildBomLine.add(bl);
						parent2SameChildNewBom.put(itemIdChildId.toString(), lstSameChildBomLine);
					}else {
						List<TCComponentBOMLine> lstSameChildBomLine = parent2SameChildNewBom.get(itemIdChildId.toString());
						lstSameChildBomLine.add(bl);
						parent2SameChildNewBom.put(itemIdChildId.toString(), lstSameChildBomLine);
					}
				}
			}
			occId2NewBom.put(occId + itemId, aLine);
			parent2ChildNewBom.put(itemId, lstChildBomLine);
		}
		
		//create mapping bl_clone_stable_occurrence_id-oldBOM
		String oldRootLineRevNum = "";
		LinkedHashMap<String, TCComponentBOMLine> occId2OldBom = new LinkedHashMap<String, TCComponentBOMLine>();
		for(TCComponentBOMLine aLine : oldBOM) {
			String occId = aLine.getPropertyDisplayableValue("bl_clone_stable_occurrence_id");
			if(occId.isEmpty()) {
				oldRootLineRevNum = aLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
			}
			String itemId = aLine.getPropertyDisplayableValue("bl_item_item_id");
			//create map parent2ChildOldBom
			List<TCComponentBOMLine> lstChildBomLine = new ArrayList<TCComponentBOMLine>();
			if(!parent2ChildOldBom.containsKey(itemId) && aLine.getChildrenCount() > 0) {
				AIFComponentContext[] childLine = aLine.getChildren();
				for(AIFComponentContext aifCon : childLine) {
					TCComponentBOMLine bl = (TCComponentBOMLine)aifCon.getComponent();
					lstChildBomLine.add(bl);
					String childItemId = bl.getPropertyDisplayableValue("bl_item_item_id");
					String findNo = bl.getPropertyDisplayableValue("bl_sequence_no");
					
					StringBuilder itemIdChildId = new StringBuilder();
					itemIdChildId.append(itemId).append(childItemId);
					
					StringBuilder itemIdChildIdFindno = new StringBuilder();
					itemIdChildIdFindno.append(itemId).append(childItemId).append(findNo);
					
					if(!parentIdOldChildIdandQuantity.containsKey(itemIdChildId.toString())) {
						parentIdOldChildIdandQuantity.put(itemIdChildId.toString(), 1);
					}else {
						parentIdOldChildIdandQuantity.put(itemIdChildId.toString(), parentIdOldChildIdandQuantity.get(itemIdChildId.toString()) + 1);
					}
					
					if(!parentIdOldChildIdFindNoAndQuantity.containsKey(itemIdChildIdFindno.toString())) {
						parentIdOldChildIdFindNoAndQuantity.put(itemIdChildIdFindno.toString(), 1);
					}else {
						parentIdOldChildIdFindNoAndQuantity.put(itemIdChildIdFindno.toString(), parentIdOldChildIdFindNoAndQuantity.get(itemIdChildIdFindno.toString()) + 1);
					}
					
					if(!parent2SameChildOldBom.containsKey(itemIdChildId.toString())) {
						List<TCComponentBOMLine> lstSameChildBomLine = new ArrayList<TCComponentBOMLine>();
						lstSameChildBomLine.add(bl);
						parent2SameChildOldBom.put(itemIdChildId.toString(), lstSameChildBomLine);
					}else {
						List<TCComponentBOMLine> lstSameChildBomLine = parent2SameChildOldBom.get(itemIdChildId.toString());
						lstSameChildBomLine.add(bl);
						parent2SameChildOldBom.put(itemIdChildId.toString(), lstSameChildBomLine);
					}
				}
			}
			
			occId2OldBom.put(occId + itemId, aLine);
			parent2ChildOldBom.put(itemId, lstChildBomLine);
		}
		
		
		//TODO check remove case
		LinkedHashMap<String, List<TCComponentBOMLine>> partId2RemovedLine = new LinkedHashMap<String, List<TCComponentBOMLine>>();
		for(TCComponentBOMLine aLine : oldBOM) {
			String oldOccId = aLine.getPropertyDisplayableValue("bl_clone_stable_occurrence_id");
			String itemId = aLine.getPropertyDisplayableValue("bl_item_item_id");
			if(!oldOccId.isEmpty() && !occId2NewBom.containsKey(oldOccId + itemId)) {
				String parentId = aLine.parent().getPropertyDisplayableValue("bl_item_item_id");
				List<TCComponentBOMLine> removedLine = new ArrayList<TCComponentBOMLine>(); 
				if(partId2RemovedLine.containsKey(parentId)) {
					removedLine = partId2RemovedLine.get(parentId);
				}
				removedLine.add(aLine);
				partId2RemovedLine.put(parentId, removedLine);
			}
		}
		
		String newRootLineRevNum = "";
		for(TCComponentBOMLine aLine : newBOM) {
			String newOccId = aLine.getPropertyDisplayableValue("bl_clone_stable_occurrence_id");
			String partId = aLine.getPropertyDisplayableValue("bl_item_item_id");
//			if(partId.compareToIgnoreCase("STD90003132") == 0) {
//				System.out.println("print");
//			}
			String type = aLine.getItemRevision().getType();
			Diff diff = new Diff();
			if(newOccId.isEmpty()) {
				//root line
				newRootLineRevNum = aLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
				if(newRootLineRevNum.compareToIgnoreCase(oldRootLineRevNum) != 0) {
					diff.changeType = "CHANGE";
					diff.desc = "Change revision " +"[" + oldRootLineRevNum + "]" + " ==> " + "[" + newRootLineRevNum + "]";
				}
				compareRes.put(aLine, diff);
			}else {
				if(occId2OldBom.containsKey(newOccId + partId)) {
					//same bomline => compare property to detect change
					TCComponentBOMLine oldBOMLine = occId2OldBom.get(newOccId + partId);
					bomLineCompareProp(aLine, diff, oldBOMLine);
					if(!diff.desc.isEmpty()) {
						diff.changeType = "CHANGE";
					}
					compareRes.put(aLine, diff);
				} else {
					// ADD, NEW, SWAP
					if (isRevisionChange(parent2ChildOldBom, aLine, diff)) {
						// TODO check case Revise new revision
						compareRes.put(aLine, diff);
					} else if (isSwapCase(parent2ChildOldBom, diff, aLine)) {
						// case SWAP
						compareRes.put(aLine, diff);
					} else {
						//compare total line
						String parentId = aLine.parent().getPropertyDisplayableValue("bl_item_item_id");
						StringBuilder parentIdPartId = new StringBuilder().append(parentId).append(partId);
						int oldQuantity = parentIdOldChildIdandQuantity.get(parentIdPartId.toString()) == null ? 0 : parentIdOldChildIdandQuantity.get(parentIdPartId.toString());
						int newQuantity = parentIdNewChildIdandQuantity.get(parentIdPartId.toString()) == null ? 0 : parentIdNewChildIdandQuantity.get(parentIdPartId.toString());
						
						if(oldQuantity == newQuantity) {
							//CHANGE case
							int lineIndex = 0;
							List<TCComponentBOMLine> newLineInParent = parent2SameChildNewBom.get(parentIdPartId.toString());
							for(TCComponentBOMLine line : newLineInParent) {
								if(line.equals(aLine)) {
									List<TCComponentBOMLine> oldLineInParent = parent2SameChildOldBom.get(parentIdPartId.toString());
									bomLineCompareProp(aLine, diff, oldLineInParent.get(lineIndex));
									if(!diff.desc.isEmpty()) {
										diff.changeType = "CHANGE";
									}
									compareRes.put(aLine, diff);
									break;
								}
								lineIndex++;
							}
						}else if(newQuantity - oldQuantity > 0){
							//ADD or NEW
							List<TCComponentBOMLine> newLineInParent = parent2SameChildNewBom.get(parentIdPartId.toString());
							if(newLineInParent.indexOf(aLine) >= oldQuantity) {
								//if part has status from SCR to greater => Add
								LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
								queryInput.put("ID", partId);
								queryInput.put("Type", type);
								TCComponent[] arrCompo = Query.queryItem(session, queryInput, "__TNH_isAddPart");
								if(arrCompo != null && arrCompo.length > 0) {
									// case ADD
									diff.changeType = "ADD";
									compareRes.put(aLine, diff);
								}else {
									// case NEW
									//if part has no status or S => NEW
									diff.changeType = "NEW";
									compareRes.put(aLine, diff);
								}
							}
						}
					}
				}
			}
			
			//TODO add remove case
			LinkedHashMap<String, Integer> mapRemovedLineProccessed = new LinkedHashMap<String, Integer>();
			if(partId2RemovedLine.containsKey(partId)) {
				List<TCComponentBOMLine> removedLines = partId2RemovedLine.get(partId);
				List<TCComponentBOMLine> newChildLines = parent2ChildNewBom.get(partId);
				boolean isSwapCase = false;
				boolean isChangeCase = false;
				boolean isChangeQuantity = false;
				for(TCComponentBOMLine removedLine : removedLines) {
					String removedPartNum = removedLine.getPropertyDisplayableValue("bl_item_item_id");
					int quantityInOldBOM = parentIdOldChildIdandQuantity.get(partId + removedPartNum) == null ? 0 : parentIdOldChildIdandQuantity.get(partId + removedPartNum);
					int quantityInNewBOM = parentIdNewChildIdandQuantity.get(partId + removedPartNum) == null ? 0 : parentIdNewChildIdandQuantity.get(partId + removedPartNum);
					int totalRemovedLine = quantityInOldBOM - quantityInNewBOM;
					for(TCComponentBOMLine newChildLine : newChildLines) {
						String oldPartNumofNewPart = newChildLine.getPropertyDisplayableValue("bl_item_vf4_orginal_part_number");
						String newPartNum = newChildLine.getPropertyDisplayableValue("bl_item_item_id");
						if(removedPartNum.compareToIgnoreCase(oldPartNumofNewPart) == 0) {
							isSwapCase = true;
							break;
						}
						
						if(removedPartNum.compareToIgnoreCase(newPartNum) == 0) {
							if(totalRemovedLine == 0) {
								isChangeCase = true;
								break;
							}else {
								int numberLineProcessed = mapRemovedLineProccessed.get(partId + removedPartNum) == null ? 0 : mapRemovedLineProccessed.get(partId + removedPartNum);
								if(numberLineProcessed < totalRemovedLine) {
									Diff diffRemoved = new Diff();
									diffRemoved.changeType = "REMOVE";
									compareRes.put(removedLine, diffRemoved);
									
									mapRemovedLineProccessed.put(partId + removedPartNum, numberLineProcessed + 1);
								}
								isChangeQuantity = true;
								break;
							}
						}
					}
					if(isSwapCase || isChangeCase || isChangeQuantity) {
						//ignore if is SWAP case
						isSwapCase = false;
						isChangeCase = false;
						isChangeQuantity = false;
						continue;
					}
					Diff diffRemoved = new Diff();
					diffRemoved.changeType = "REMOVE";
					compareRes.put(removedLine, diffRemoved);
				}
			}
		}
		return compareRes;
	}

	private void bomLineCompareProp(TCComponentBOMLine currLine, Diff diff, TCComponentBOMLine oldBOMLine)
			throws NotLoadedException {
		//Set<String> propCompare = prop2Compare.keySet();
		
		for(Entry<String, String> propCompare : prop2Compare.entrySet()){	            
			String propName = propCompare.getKey();
			if(propName.compareToIgnoreCase("bl_level_starting_0") != 0 &&
					propName.compareToIgnoreCase("bl_item_item_id") != 0 &&
					propName.compareToIgnoreCase("Change Type") != 0 &&
					propName.compareToIgnoreCase("Description") != 0) {
				String newValue = currLine.getPropertyDisplayableValue(propName) == null ? "" : currLine.getPropertyDisplayableValue(propName);
				String oldValue = oldBOMLine.getPropertyDisplayableValue(propName) == null ? "" : oldBOMLine.getPropertyDisplayableValue(propName);
				if(newValue.compareToIgnoreCase(oldValue) != 0) {
					if(!diff.desc.isEmpty()) {
						StringBuilder str = new StringBuilder();
						str.append(diff.desc);
						str.append("\n");
						str.append("Change " + propName + " [" + oldValue + "]" + " ==> " + "[" + newValue + "]");
						diff.desc =  str.toString();
					}else {
						diff.desc = "Change " + propName + " [" + oldValue + "]" + " ==> " + "[" + newValue + "]";
					}
				}
			}
        }
	}

	private boolean isSwapCase(LinkedHashMap<String, List<TCComponentBOMLine>> parent2ChildOldBom, Diff diff,
			TCComponentBOMLine currLine) throws NotLoadedException, TCException {
		boolean isSwapCase = false;
		String oldPartNum = currLine.getPropertyDisplayableValue("bl_item_vf4_orginal_part_number");
		String partNum = currLine.getPropertyDisplayableValue("bl_item_item_id");
		TCComponentBOMLine parentLine = currLine.parent();
		List<TCComponentBOMLine> oldChildLines = parent2ChildOldBom.get(parentLine.getPropertyDisplayableValue("bl_item_item_id"));
		if(oldChildLines != null && oldChildLines.size() > 0) {
			for(TCComponentBOMLine oldChild : oldChildLines) {
				if(oldChild.getPropertyDisplayableValue("bl_item_item_id").compareToIgnoreCase(oldPartNum) == 0) {
					diff.changeType = "SWAP";
					diff.desc = "Swap " + "[" + oldPartNum + "]" + " ==> " + "[" + partNum + "]";
					isSwapCase = true;
					bomLineCompareProp(currLine, diff, oldChild);
					
					break;
				}
			}
		}
		return isSwapCase;
	}

	private boolean isRevisionChange(LinkedHashMap<String, List<TCComponentBOMLine>> parent2ChildOldBom, TCComponentBOMLine aLine,
			Diff diff) throws TCException, NotLoadedException {
		boolean isRevisionChange = false;
		TCComponentBOMLine parentLine = aLine.parent();
		String newRevNumber = aLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
		String newPartId = aLine.getPropertyDisplayableValue("bl_item_item_id");
		String parentId = parentLine.getPropertyDisplayableValue("bl_item_item_id");
		if(parent2ChildOldBom.containsKey(parentId)) {
			List<TCComponentBOMLine> oldChildLine = parent2ChildOldBom.get(parentId);
			for(TCComponentBOMLine oldLine : oldChildLine) {
				String oldId = oldLine.getPropertyDisplayableValue("bl_item_item_id");
				if(oldId.compareToIgnoreCase(newPartId) == 0) {
					String oldRevNum = oldLine.getPropertyDisplayableValue("bl_rev_item_revision_id");
					if(oldRevNum.compareToIgnoreCase(newRevNumber) != 0) {
						diff.changeType = "CHANGE";
						bomLineCompareProp(aLine, diff, oldLine);
						isRevisionChange = true; 
					}
					break;
				}
			}
		}
		return isRevisionChange;
	}
	
	private void excelPublishReport(LinkedHashMap<TCComponentBOMLine, Diff> compareRes) {
		try {
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, "BOM_REPORT_TEMPLATE", TEMP_DIR, "BOM_Comparison", 0);

			InputStream fileIn = new FileInputStream(report);
			XSSFWorkbook template = new XSSFWorkbook(fileIn);
			BOMComparisonHandler.wb = new SXSSFWorkbook(template, 500);
			
			excelWriteHeaderLine("Sheet1");
			excelWriteARowReport(compareRes);
			
			Runtime.getRuntime().gc();
			
			FileOutputStream fos = new FileOutputStream(report);
			BOMComparisonHandler.wb.write(fos);
			fos.close();
			BOMComparisonHandler.wb.dispose();
			report.setWritable(false);
			Desktop.getDesktop().open(report);
		} catch (IOException | TCException | NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private CellStyle getCellStyle (boolean isNumber, String changeType, String indent) {
		SXSSFSheet spreadsheet = wb.getSheet("Sheet1");
		SXSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle cellStyle = wb.createCellStyle();
		if(isNumber) {
			String pattern = "0.000";
			cellStyle.setDataFormat(wb.createDataFormat().getFormat(pattern));
		}
		if(!indent.isEmpty()) {
			cellStyle.setIndention(Short.valueOf(indent));;
		}
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		
		switch (changeType) {
		case "CHANGE":
			Color color = new Color(252, 213, 180);
	        XSSFColor xssfColor = new XSSFColor(color, new DefaultIndexedColorMap());
	        xssfColor.setTheme(2);
	        xssfColor.setTint(0.4);
			cellStyle.setFillForegroundColor(xssfColor.getIndex());
			cellStyle.setFillBackgroundColor(xssfColor.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "SWAP":
			cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "NEW":
			cellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "ADD":
			cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "REMOVE":
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		default:
			break;
		}
		
		return cellStyle;
	}
	
	private XSSFCellStyle getCellStyle2 (boolean isNumber, String changeType, String indent) {
		SXSSFSheet spreadsheet = wb.getSheet("Sheet1");
		SXSSFWorkbook wb = spreadsheet.getWorkbook();
		XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
		if(isNumber) {
			String pattern = "0.000";
			cellStyle.setDataFormat(wb.createDataFormat().getFormat(pattern));
		}
		if(!indent.isEmpty()) {
			cellStyle.setIndention(Short.valueOf(indent));;
		}
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		
		switch (changeType) {
		case "CHANGE":
			//Color color = new Color(244, 176, 132);
			Color color = new Color(255,239,213);
	        XSSFColor xssfColor = new XSSFColor(color, new DefaultIndexedColorMap());
			cellStyle.setFillForegroundColor(xssfColor);
			cellStyle.setFillBackgroundColor(xssfColor);
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "SWAP":
			cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "NEW":
			cellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "ADD":
			cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case "REMOVE":
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			cellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		default:
			break;
		}
		
		return cellStyle;
	}
	
	private void excelWriteARowReport(LinkedHashMap<TCComponentBOMLine, Diff> CompareRes) throws NotLoadedException, TCException {

		// TODO loop propmap and write file
		SXSSFSheet spreadsheet = wb.getSheet("Sheet1");

		int rowCounter = 1;
		for (Map.Entry<TCComponentBOMLine, Diff> entry : CompareRes.entrySet()) {
			TCComponentBOMLine BOMLineInfo = entry.getKey();
			String changeType = entry.getValue().changeType;
			String desc = entry.getValue().desc;
			String indent = "";
			int cellCounter = 0;
			SXSSFRow row = spreadsheet.createRow(rowCounter);
			//Set<String>propCompare = prop2Compare.keySet();
			
			for(Entry<String, String> propCompare : prop2Compare.entrySet()){	            
				String col = propCompare.getKey();
				String dispItm = "";
				if(col.compareTo("Change Type") == 0) {
					dispItm = changeType;
				}else if(col.compareTo("Description") == 0) {
					dispItm = desc;
				}else {
					dispItm = BOMLineInfo.getPropertyDisplayableValue(col);
				}
				
				//set indent for level column
				indent = col.compareToIgnoreCase("bl_level_starting_0") == 0 ? dispItm : "";
				SXSSFCell cell = row.createCell(cellCounter);
				if (!isNumber(dispItm)) {
					cell.setCellStyle(getCellStyle2(false, changeType, indent));
					cell.setCellValue(dispItm == null ? "" : dispItm);
				} else {
					if (col.compareToIgnoreCase("bl_rev_item_revision_id") == 0 || col.compareToIgnoreCase("bl_item_item_id") == 0 || col.compareToIgnoreCase("bl_level_starting_0") == 0) {
						cell.setCellStyle(getCellStyle2(false, changeType, indent));
						cell.setCellValue(dispItm == null ? "" : dispItm);
					} else {
						cell.setCellStyle(getCellStyle2(true, changeType, indent));
						cell.setCellValue(Double.valueOf(dispItm));
						cell.setCellType(CellType.NUMERIC);
					}
				}
				cellCounter++;
			}
			rowCounter++;				
		}
	}
	
	private void excelWriteHeaderLine(String sheetName) {
		
		SXSSFSheet spreadsheet = wb.getSheet(sheetName);
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		headerCellStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		SXSSFRow headerRow = spreadsheet.createRow(0);

		int counter = 0;
		for(Entry<String, String> propCompare : prop2Compare.entrySet()){	            
			getHeaderCell(counter, headerRow, propCompare.getValue(), headerCellStyle);
			counter++;
        }
					
			
		wb.getSheet(sheetName).createFreezePane(0, 1);
		/*auto size column*/
//		wb.getSheet(sheetName).trackAllColumnsForAutoSizing();
//		for (int kz = 0; kz < prop2Compare.size(); kz++) {
//			wb.getSheet(sheetName).autoSizeColumn(kz);
//		}
	}
	
	private SXSSFCell getHeaderCell(int index, SXSSFRow headerRow, String displayValue, CellStyle headerCellStyle){
		 SXSSFCell cell = headerRow.createCell(index);
		 cell.setCellValue(displayValue);
		 cell.setCellStyle(headerCellStyle);
		 return cell;
	}
	
	private boolean isNumber(String input) {
		 try {
	            Double.parseDouble(input);
	            return true;
	        }
	        catch(NumberFormatException e) {
	            return false;
	        }
		 	catch(NullPointerException e) {
		 		return false;
		 	}
		 	
	}
}