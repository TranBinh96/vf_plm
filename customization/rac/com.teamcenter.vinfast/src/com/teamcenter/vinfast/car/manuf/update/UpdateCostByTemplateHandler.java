package com.teamcenter.vinfast.car.manuf.update;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.CostUtils;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.Utilities;

import vfplm.soa.common.service.VFUtility;


public class UpdateCostByTemplateHandler extends AbstractHandler {
	private TCSession session;
	private TCComponentTask[] targetTask = null;
	private UpdateCostByTemplateDialog dialog = null;
	private LinkedHashMap<String, LinkedHashMap<String, String>> mapId2CostDataBefore = null;
	private LinkedHashMap<String, LinkedHashMap<String, String>> mapId2CostDataAfter = null;
	private LinkedHashMap<String, String> mapDisplayProp2Prop = null;
	public UpdateCostByTemplateHandler() {
		super();
		// ---------------------------- Init member variable-------------------------------------
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.dialog = new UpdateCostByTemplateDialog(new Shell());
		this.mapId2CostDataBefore = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		this.mapId2CostDataAfter = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		
		this.mapDisplayProp2Prop = new LinkedHashMap<String, String>();
		this.mapDisplayProp2Prop.put("Part Number", "");
		this.mapDisplayProp2Prop.put("Piece Cost Currency Status", "vf4_piece_cost_curr");
		this.mapDisplayProp2Prop.put("Piece Cost Value Status", "vf4_piece_cost_value_status");
		this.mapDisplayProp2Prop.put("Supplier Packaging Amount", "vf4_supplier_package_amount");
		this.mapDisplayProp2Prop.put("Supplier Logistics Cost", "vf4_supplier_logisis_cost");
		this.mapDisplayProp2Prop.put("Manufacturing Cost", "vf4_miscellaneous_cost");
		this.mapDisplayProp2Prop.put("Tax Absolute", "vf4_prd_tax_absolute");
		this.mapDisplayProp2Prop.put("Tooling Investment Currency Status", "vf4_tooling_invtest_curr");
		this.mapDisplayProp2Prop.put("Tooling Investment Value Status", "vf4_tooling_invest_value");
		this.mapDisplayProp2Prop.put("ED&D Cost", "vf4EdndCost");
		this.mapDisplayProp2Prop.put("ED&D Cost Currency", "vf4Ednd_curr");

		this.targetTask = getTargetTask(AIFUtility.getCurrentApplication().getTargetComponents());
		// ---------------------------- Init member variable-------------------------------------
	}
	
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		UpdateCostByTemplateHandler handler = new UpdateCostByTemplateHandler();
		for(TCComponentTask task : handler.targetTask) {
			try {
				String taskName = task.getName();
				String wfName = task.getRoot().getName();
				if(!handler.isCorrectTask(taskName)) {
					MessageBox.post("This function apply for only workflow process Manufacturing Change Cost Request", "WARNING", MessageBox.WARNING);
					return null;
				}
				if(!handler.isCorrectWorkflow(wfName)) {
					MessageBox.post("This function apply for only workflow process Manufacturing Change Cost Request", "WARNING", MessageBox.WARNING);
					return null;
				}
				handler.setMapId2CostDataBefore(task);
			} catch (TCException e) {
				MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
				return null;
			}
		}
		handler.dialog.create();
		handler.dialog.btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(handler.mapId2CostDataAfter != null && handler.mapId2CostDataAfter.size() > 0) {
					for(TCComponentTask task : handler.targetTask) {
						boolean updateOK = handler.isUpdateCostOK(task);
						if(!updateOK) {
							break;
						}else{
							//TODO complete task
							if(handler.dialog.btnCompleteTsk.getSelection()) {
								Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
								paii.clientId = "complete" + task.getUid();
								paii.action = "SOA_EPM_complete_action";
								paii.actionableObject = task;
								paii.propertyNameValues.put("comments", new String[]{"Auto Completed"});
								paii.supportingValue = "SOA_EPM_completed";
								
								ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[]{paii});
								if(sd.sizeOfPartialErrors() > 0) {
									MessageBox.post(Utils.HanlderServiceData(sd), "ERROR", MessageBox.ERROR);
									return;
								}
							}
						}
					}
				}
				handler.dialog.close();
			}
		});
		
		handler.dialog.btnAttachment.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
					FileDialog fd = new FileDialog(handler.dialog.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "*.csv" };
					fd.setFilterExtensions(filterExt);
					String selected = fd.open();
					if (selected != null) {
						handler.dialog.textAttachment.setText(selected);
					}
					// TODO call function extract data from file
					if (!selected.isEmpty()) {
						handler.mapId2CostDataAfter = readCsv(selected);
						if(handler.mapId2CostDataAfter != null && handler.mapId2CostDataAfter.size() > 0) {
							handler.dialog.btnOK.setEnabled(true);
						}
					}
			}
		});
		
		handler.dialog.btnDownloadTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*write all cost data into csv file*/
				File f = handler.writeCSVFile();
				if(f != null) {
					try {
						Desktop.getDesktop().open(f);
//						ProcessBuilder pb = new ProcessBuilder("Notepad.exe", f.getAbsolutePath());
//						pb.start();
					} catch (IOException e) {
						MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
						return;
					}
				}
			}
		});

		handler.dialog.open();
		return null;
	}
	
	private TCComponentTask[] getTargetTask(InterfaceAIFComponent[] targetComp) {
		TCComponentTask[] targetTask = new TCComponentTask[targetComp.length];
		int i = 0;
		for(InterfaceAIFComponent selected : targetComp) {
			if (selected instanceof TCComponentTask) {
				targetTask[i] = ((TCComponentTask)selected);
			}
			i++;
		}
		return targetTask;
	}
	
	private boolean isUpdateCostOK(TCComponentTask task) {
		TCComponent[] arrCostForm;
		try {
			arrCostForm = task.getRelatedComponents("root_target_attachments");
			for(TCComponent costForm : arrCostForm) {
				String formName = costForm.getPropertyDisplayableValue("object_name");
				String partNum = formName.split("_")[0];
				TCAccessControlService acl = session.getTCAccessControlService();
				boolean canWrite2Form = (acl.checkPrivilege(costForm, "WRITE") && acl.checkPrivilege(costForm, "WRITE"));
				if(canWrite2Form == false) {
					MessageBox.post("You don't have write access on the form " + formName, "ERROR", MessageBox.ERROR);
					return false;
				}
				LinkedHashMap<String, String> proDisName2value = this.mapId2CostDataAfter.get(partNum);
				if(proDisName2value != null) {
					HashMap<String, DataManagementService.VecStruct> propertyMap = new HashMap<String, DataManagementService.VecStruct>();
					for(Entry<String, String> entry : proDisName2value.entrySet()) {
						String propDisplayName = entry.getKey();
						String value = entry.getValue();
						String propRealName = this.mapDisplayProp2Prop.get(propDisplayName);
						if(propRealName != null && !propRealName.isEmpty()) {
							TCComponentType objType = costForm.getTypeComponent();
							int propType = objType.getPropDesc(propRealName).getType();
							switch (propType) {
							case PropertyDescription.CLIENT_PROP_TYPE_date:
								if (!value.isEmpty()) {
									Calendar cal = Calendar.getInstance();
									SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
									cal.setTime(formatter.parse(value));
									String[] propertyDateValues = { Utils.CalendarToTcDateString(cal) };
									DataManagementService.VecStruct vecStructDate = new DataManagementService.VecStruct();
									vecStructDate.stringVec = propertyDateValues;
									propertyMap.put(propRealName, vecStructDate);
								}else {
									DataManagementService.VecStruct vecStructDate = new DataManagementService.VecStruct();
									String[] propertyDateValues = { "" };
									vecStructDate.stringVec = propertyDateValues;
									propertyMap.put(propRealName, vecStructDate);
								}
								break;
							case PropertyDescription.CLIENT_PROP_TYPE_string:
								String[] propertyStringValues = { value };
								DataManagementService.VecStruct vecStructString = new DataManagementService.VecStruct();
								vecStructString.stringVec = propertyStringValues;
								propertyMap.put(propRealName, vecStructString);
								break;
							case PropertyDescription.CLIENT_PROP_TYPE_double:
								String[] propertyDoubleValues = { value };
								DataManagementService.VecStruct vecStructDouble = new DataManagementService.VecStruct();
								vecStructDouble.stringVec = propertyDoubleValues;
								propertyMap.put(propRealName, vecStructDouble);
							default:
								break;
							}
						}
					}
					//hardcode to set default value vf4_quality_of_finance
					String[] propertyStringValues = { "Cost Engineering Estimate" };
					DataManagementService.VecStruct vecStructString = new DataManagementService.VecStruct();
					vecStructString.stringVec = propertyStringValues;
					propertyMap.put("vf4_quality_of_finance", vecStructString);
					
					ServiceData serviceData = DataManagementService.getService(session).setProperties(new TCComponent[] { costForm }, propertyMap);
					if (serviceData.sizeOfPartialErrors() > 0) {
						MessageBox.post("Exception: " + Utils.HanlderServiceData(serviceData), "ERROR", MessageBox.ERROR);
						return false;
					}
				}
			}
		} catch (TCException | NotLoadedException | ParseException e) {
			MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
			return false;
		}
		return true;
	}
	
	private void setMapId2CostDataBefore(TCComponentTask task) {
		TCComponent[] arrCostForm;
		try {
			arrCostForm = task.getRelatedComponents("root_target_attachments");
			for(TCComponent costForm : arrCostForm) {
				String formName = costForm.getPropertyDisplayableValue("object_name");
				String partNum = formName.split("_")[0];
				LinkedHashMap<String, String> proDisName2value = new LinkedHashMap<String, String>();
				for(Entry<String, String> entry : this.mapDisplayProp2Prop.entrySet()) {
					String propDisplayName = entry.getKey();
					String propRealName = entry.getValue();
					if(!propRealName.isEmpty()) {
						String value = costForm.getPropertyDisplayableValue(propRealName);
						proDisName2value.put(propDisplayName, value);
					}else {
						proDisName2value.put(propDisplayName, partNum);
					}
				}
				this.mapId2CostDataBefore.put(partNum, proDisName2value);
			}
		} catch (TCException | NotLoadedException e) {
			MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
		}
	}
	
	private boolean isCorrectTask(String taskName) {
		if(taskName.compareTo("Update Cost") == 0) {
			return true;
		}
		return false;
	}
	
	private boolean isCorrectWorkflow(String wfName) {
		if(wfName.compareTo("Manufacturing Change Cost Request") == 0) {
			return true;
		}
		return false;
	}

	private File writeCSVFile(){
		List<String[]> csvData = createCSVData();
		File output = null;
        // default all fields are enclosed in double quotes
        // default separator is a comma
		final String tempFolder = System.getenv("tmp");
		String pathFile = tempFolder + "\\" + "data.csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(pathFile))) {
            writer.writeAll(csvData);
            output = new File(pathFile);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return output;
	}
	
	private List<String[]> createCSVData(){
		List<String[]> output = new LinkedList<String[]>();
		String[] header = this.mapDisplayProp2Prop.keySet().toArray(new String[this.mapDisplayProp2Prop.keySet().size()]);
		output.add(header);
		if(this.mapId2CostDataBefore != null && this.mapId2CostDataBefore.size() > 0) {
			for(Map.Entry<String, LinkedHashMap<String, String>> entry : this.mapId2CostDataBefore.entrySet()) {
				LinkedHashMap<String, String> pairCostData = entry.getValue();
				String[] value = pairCostData.values().toArray(new String[pairCostData.values().size()]);
				output.add(value);
			}
		}
		return output;
	}
	
	private LinkedHashMap<String, LinkedHashMap<String, String>> readCsv(String csvFile) {
		LinkedHashMap<String, LinkedHashMap<String, String>> output = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "ISO_8859_1")));
			String[] tempArr;
			String[] header = null;
			String partNum = "";
			int counter = 0;
			while ((tempArr = csvReader.readNext()) != null) {
				counter++;
				LinkedHashMap<String, String> prop2Value = new LinkedHashMap<String, String>();
				if (counter == 1) {
					header = tempArr;
					for (String aHeader : header) {
						if(!this.mapDisplayProp2Prop.containsKey(aHeader)) {
							MessageBox.post("Invalid column - " + aHeader, "ERROR", MessageBox.ERROR);
							return output;
						}
					}
					continue;
				} else {
					partNum = tempArr[0];
					for(int i = 0; i < header.length; i++) {
						String value = tempArr[i];
						String aHeader = header[i];
						prop2Value.put(aHeader, value);
					}
				}
				output.put(partNum, prop2Value);
			}
			csvReader.close();
		} catch (IOException ioe) {
			MessageBox.post(ioe.toString(), "ERROR", MessageBox.ERROR);
			return output;
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
				return output;
			}
		}
		return output;
	}
}