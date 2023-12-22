package com.teamcenter.vinfast.aftersale.create;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.bom.StructureManagementService;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineInfo;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineResponse;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BOMLinesOutput;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.ItemLineInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.lov.VFLovService;
import com.teamcenter.vinfast.utils.lov.VFLovValues;
import com.vinfast.sc.utilities.PropertyDefines;

public class VFHandlerServiceStructure extends AbstractHandler {

	LinkedHashMap<String, ArrayList<String>> level0level1 = null;
	LinkedHashMap<String, ArrayList<String>> level1level2 = null;
	HashMap<String, TCComponent> bomLines = new HashMap<String, TCComponent>();
	String donor = null;;
	String category = null;
	String realNameDonor = null;
	TCComponent C3DTemplate;
	DataManagementService coreDMService;
	Shell dialogShell;
	ProgressBar progressBar;
	int total = 1;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = HandlerUtil.getCurrentSelection( event );
		InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
		TCSession session = (TCSession)selectedObjects[0].getSession();
		coreDMService = DataManagementService.getService(session);
		dialogShell = new Shell();
		VFDialogServiceStructure serviceDialog =  new VFDialogServiceStructure(dialogShell);
		serviceDialog.create();
		VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_SVC_CHAPTER, new String[] {PropertyDefines.PROP_VEH_CATEGORY});
		VFLovService.setLovValue(serviceDialog.cb_Veh, lovValues.getLOVValue(PropertyDefines.TYPE_SVC_CHAPTER, PropertyDefines.PROP_VEH_CATEGORY));
		progressBar = serviceDialog.progressBar;
		serviceDialog.cb_Donor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				// TODO Auto-generated method stub
				donor = serviceDialog.cb_Donor.getText();
				realNameDonor = serviceDialog.cb_Donor.getData(donor).toString();
				if(donor != null && category != null) {
					serviceDialog.bt_Create.setEnabled(true);
				}else {
					serviceDialog.bt_Create.setEnabled(false);
				}
			}
		});
		serviceDialog.cb_Veh.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				// TODO Auto-generated method stub
				category = serviceDialog.cb_Veh.getText();
				VFLovValues filterValues = VFLovService.loadFilterLOVValues(session, PropertyDefines.TYPE_SVC_CHAPTER, PropertyDefines.PROP_VEH_CATEGORY, category);
				VFLovService.setLovValue(serviceDialog.cb_Donor, filterValues.getFilterLOVValue());
				if(category != null && donor != null) {
					serviceDialog.bt_Create.setEnabled(true);
				}else {
					serviceDialog.bt_Create.setEnabled(false);
				}
			}
		});
		serviceDialog.bt_Create.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				progressBar.setVisible(true);
				serviceDialog.bt_Create.setEnabled(false);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							total = total + 3;
							TCComponent[] SBOM = Utils.executeSavedQuery("Item...", new String[] {donor+"_SBOM",PropertyDefines.TYPE_SVC_CHAPTER}, new String[] {"Name","Type"});
							if(SBOM.length > 0) {
								MessageBox.post(dialogShell,"Service BOM already exists in system for \""+donor+"\"", "Information", MessageBox.INFORMATION);
								progressBar.setVisible(false);
								serviceDialog.bt_Create.setEnabled(true);
							}else {
								boolean result = readExcelTemplate(serviceDialog.cb_Veh.getText());
								progressBar.setSelection(getProgress(1, total));
								if(result == true) {
									TCComponent[] values = Utils.executeSavedQuery("Item...", new String[] {"Service Chapter and Catalog Template","Cortona3D Item Type"}, new String[] {"Name","Type"});
									if(values != null) {
										C3DTemplate = values[0];
										progressBar.setSelection(getProgress(2, total));
										TCComponentItem item = CreateServiceChapterStructure(session);
										if(item != null) {
											session.getUser().getHomeFolder().add("contents", new TCComponent[] {item});
											progressBar.setSelection(100);
											MessageBox.post(item.getProperty("item_id")+" successfully created and attached to Home folder.", "Success", MessageBox.INFORMATION);
											dialogShell.dispose();
										}
									}else {
										MessageBox.post(dialogShell,"Error loading Cortona Catalog Template. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
										serviceDialog.progressBar.setVisible(false);
										serviceDialog.bt_Create.setEnabled(true);
									}
								}
							}
						} catch (TCException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		serviceDialog.open();
		return event;
	}

	private boolean readExcelTemplate(String vehicle) {

		try {
			String fileName = "VF_RECIPE_MBOM_SHOPS";
			TCComponent[] dataset = Utils.executeSavedQuery("Dataset Name", new String[] {fileName},new String[] {"Dataset Name"});
			if(dataset.length == 0) {
				MessageBox.post("Error finding Structure template in TC. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return false;
			}
			TCComponentTcFile[] namedRefs = ((TCComponentDataset)dataset[0]).getTcFiles();
			if(namedRefs == null) {
				MessageBox.post("Structure template corrupted in TC. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return false;
			}

			FileInputStream fileStream = new FileInputStream(namedRefs[0].getFmsFile());  
			XSSFWorkbook workBook = new XSSFWorkbook(fileStream);   
			XSSFSheet sheet = (vehicle.equalsIgnoreCase("Automobile"))?workBook.getSheet("SBOM"):workBook.getSheet("ESBOM"); 
			if(sheet == null) {
				MessageBox.post("Error in reading template in TC. Please contact Teamcenter Admin", "Error", MessageBox.INFORMATION);
				workBook.close();
				return false;
			}else {
				level0level1 = new LinkedHashMap<String, ArrayList<String>>();
				level1level2 = new LinkedHashMap<String, ArrayList<String>>(); 
				int rowNumber = 0;
				Iterator<Row> rowIterator = sheet.iterator();
				String level0Parts = "";
				String level1Parts = "";
				while (rowIterator.hasNext())                 
				{  
					Row row = rowIterator.next();
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}
					String level0 = getCellText(row.getCell(0));
					if(level0.isEmpty() == false && level0Parts.equals(level0) == false) {
						level0Parts = level0;
					}
					String level1 = getCellText(row.getCell(1));
					if(level1.isEmpty() == false && level1Parts.equals(level1) == false) {
						level1Parts = level1;
					}
					String level2 = getCellText(row.getCell(2));

					if(level0Parts.isEmpty() == false && level1.isEmpty() == false) {
						if(level0level1.containsKey(level0Parts)) {
							ArrayList<String> level1Map = level0level1.get(level0Parts);
							level1Map.add(level1);
							level0level1.put(level0Parts, level1Map);
						
						}else {
							ArrayList<String> level2Map = new ArrayList<String>();
							level2Map.add(level1Parts);
							level0level1.put(level0Parts, level2Map);
							total++;
						}
					}
					if(level1Parts.isEmpty() == false && level2.isEmpty() == false) {
						if(level1level2.containsKey(level1Parts)) {
							ArrayList<String> level2Map = level1level2.get(level1Parts);
							level2Map.add(level2);
						}else {
							ArrayList<String> level3Map = new ArrayList<String>();
							level3Map.add(level2);
							level1level2.put(level1Parts, level3Map);
							total++;
						}
					}
				}
				workBook.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private TCComponentItem CreateServiceChapterStructure(TCSession session) {
		TCComponentItem topItem = null;
		int counter = 4;
		try {
			StructureManagementService BOMSMService = StructureManagementService.getService(session);
			LinkedHashMap<String, TCComponent> newItems = createNewItems(coreDMService, new String[] {donor+"_SBOM"});
			if(newItems != null) {
				TCComponentItemRevision topRevision = (TCComponentItemRevision)newItems.get(donor+"_SBOM");
				topItem = topRevision.getItem();
				TCComponentBOMLine TOPLine = Utils.createBOMWindow(session, topRevision);
				if(level0level1 != null) {
					String[] level1Keys = level0level1.keySet().toArray(new String[0]); //level0keys
					if(level1Keys.length !=0) {
						newItems = createNewItems(coreDMService, level1Keys);
						addItemsToBOMLine(BOMSMService, TOPLine, newItems);
						progressBar.setSelection(getProgress(3, total));
						for(String level1Key : level1Keys) {
							ArrayList<String> level1Map = level0level1.get(level1Key);
							if(level1Map != null) {
								String level2Keys[] = level1Map.toArray(new String[0]); //level1keys
								if(level2Keys.length !=0) {
									newItems = createNewItems(coreDMService, level2Keys);
									addItemsToBOMLine(BOMSMService, (TCComponentBOMLine)bomLines.get(level1Key), newItems);
									progressBar.setSelection(getProgress(counter++, total));
									for(String level2Key : level2Keys) {
										ArrayList<String> level3Map = level1level2.get(level2Key);
										if(level3Map != null) {
											String level3Keys[] = level3Map.toArray(new String[0]); //level2keys
											if(level3Keys.length != 0) {
												newItems = createNewItems(coreDMService, level3Keys);
												addItemsToBOMLine(BOMSMService, (TCComponentBOMLine)bomLines.get(level2Key), newItems);
												progressBar.setSelection(getProgress(counter++, total));
											}
										}
									}
								}
							}
						}
					}
					TOPLine.window().save();
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return topItem;
	}
	
	private LinkedHashMap<String, TCComponent> createNewItems(DataManagementService coreDMService, String[] names) {
		LinkedHashMap<String, TCComponent> revision = null;
		try {

			CreateIn[] itemDef = new CreateIn[names.length];
			for(int i= 0; i<names.length; i++) {
				itemDef[i] = new CreateIn();
				itemDef[i].clientId = names[i];
				itemDef[i].data.boName = PropertyDefines.TYPE_SVC_CHAPTER;
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_NAME, names[i]);
				itemDef[i].data.stringProps.put(PropertyDefines.PROP_OBJ_DESC, names[i]);
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_SVC_DONOR_VEH, realNameDonor);
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_MAKE_BUY, "Information");
				itemDef[i].data.stringProps.put(PropertyDefines.ITEM_IS_TRACEABLE, "N");
				itemDef[i].data.tagProps.put(PropertyDefines.PROP_C3D_ITEM_TYPE, C3DTemplate);

				CreateInput revDef = new CreateInput();
				revDef.boName = PropertyDefines.TYPE_SVC_CHAPTER_REVISION;
				revDef.stringProps.put(PropertyDefines.PROP_OBJ_DESC, names[i]);
				itemDef[i].data.compoundCreateInput.put("revision", new CreateInput[] { revDef });
			}

			CreateResponse response = coreDMService.createObjects(itemDef);
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				revision = new LinkedHashMap<String, TCComponent>(); 
				for(CreateOut output : response.output) {
					for (TCComponent rev : output.objects) {
						if (rev.getType().equals(PropertyDefines.TYPE_SVC_CHAPTER_REVISION)) {
							//TCComponentItemRevision revisionTag = (TCComponentItemRevision)rev;
							revision.put(output.clientId, rev);
							//revisionTag.getItem().setProperty(PropertyDefines.ITEM_SVC_DONOR_VEH, realNameDonor);
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

	private String getCellText(Cell cell) {

		if(cell != null) {
			return cell.getStringCellValue();
		}
		return "";
	}

	private void addItemsToBOMLine(StructureManagementService BOMSMService, TCComponentBOMLine parentLine, HashMap<String,TCComponent> revisionsMap) {
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
				for(BOMLinesOutput output : response.itemLines) {
					bomLines.put(output.clientId, output.bomline);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}
}
