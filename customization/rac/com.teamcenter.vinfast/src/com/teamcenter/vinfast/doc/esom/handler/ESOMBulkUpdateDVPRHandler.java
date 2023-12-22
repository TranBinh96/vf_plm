package com.teamcenter.vinfast.doc.esom.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.opencsv.CSVReader;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.PropDescriptorService;
import com.teamcenter.services.rac.core._2007_06.PropDescriptor.PropDescInfo;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDesc;
import com.teamcenter.services.rac.core._2011_06.PropDescriptor.PropDescOutput2;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.doc.esom.view.ESOMFileBrowserDialog;
import com.teamcenter.vinfast.utils.Utils;

public class ESOMBulkUpdateDVPRHandler extends AbstractHandler {
	private TCSession session;
	private LinkedHashMap<String, LinkedHashMap<String, String>> masterData = new LinkedHashMap<String, LinkedHashMap<String, String>>();
	private List<String> propRealName = new ArrayList<String>();

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		masterData.clear();
		propRealName.clear();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponent esomRev = (TCComponent) targetComp[0];
		if (targetComp.length > 1 || esomRev.getTypeObject().toString().compareToIgnoreCase("VF3_AT_ESOM_DocRevision") != 0) {
			MessageBox.post("Please select only one ESOM revision", "WARNING", MessageBox.WARNING);
			return null;
		}
		//check access write
		TCAccessControlService acl = session.getTCAccessControlService();
		boolean hasWriteAccess = false;
		try {
			hasWriteAccess = acl.checkPrivilege(esomRev, "WRITE");
		} catch (TCException e2) {
			MessageBox.post("Exception: " + e2.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		if(!hasWriteAccess) {
			MessageBox.post("You don't have access write on this ESOM revision", "WARNING", MessageBox.WARNING);
			return null;
		}
		

			ESOMFileBrowserDialog dialog = new ESOMFileBrowserDialog(new Shell());
			dialog.create();
			dialog.setMessage("File format:\n"
					+ "DVPR Number, Target Met, Percentage Completed, Risk Assessment, Comment");

			dialog.btnAttachment.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(dialog.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "*.csv" };
					fd.setFilterExtensions(filterExt);
					String selected = fd.open();
					if (selected != null)
						dialog.textAttachment.setText(selected);

					// TODO call function extract data from file
					if (!selected.isEmpty()) {
						int lineErr = readCsv(selected);
						switch (lineErr) {
						case 0:
							dialog.setMessage("Read file succesfully");
							dialog.btnOK.setEnabled(true);
							break;
						case -1:
							dialog.setMessage("Internal error while reading csv file");
							break;
						default:
							dialog.setMessage("Line number " + lineErr + " wrong format");
							break;
						}
					}
				}
			});
			
			dialog.btnOK.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// TODO call function expand bom and update
					dialog.setMessage("Updating...");
					if (masterData.size() > 0) {
						try {
							/* get descriptor for list properties */
							PropDescInfo propDescInfo = new PropDescInfo();
							propDescInfo.typeName = "VF3_ESOM_DVPR";
							String[] propNames = propRealName.toArray(new String[propRealName.size()]);
							propDescInfo.propNames = propNames;
							
							PropDescriptorService propDescService = PropDescriptorService.getService(session);
							PropDescriptor.AttachedPropDescsResponse res = propDescService.getAttachedPropDescs2(new PropDescInfo[] { propDescInfo });
							if (res.serviceData.sizeOfPartialErrors() > 0) {
								MessageBox.post("Exception: " + Utils.HanlderServiceData(res.serviceData), "ERROR", MessageBox.ERROR);
								return;
							}
							Collection<PropDescOutput2[]> propDesc = (Collection<PropDescOutput2[]>) res.inputTypeNameToPropDescOutput.values();
							LinkedHashMap<String, PropDesc> mapPropDesc = new LinkedHashMap<String, PropDesc>();
							PropDescOutput2[] aPropDesc2 = propDesc.iterator().next();
							for (int i = 0; i < aPropDesc2.length; i++) {
								mapPropDesc.put(aPropDesc2[i].propName, aPropDesc2[i].propertyDesc);
							}
							//loop each element to update data
							List<TCComponent> currdvprTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_dvpr_info")));
							for(int i = 0; i < currdvprTable.size(); i++) {
								TCComponent dvprDetailInfo = currdvprTable.get(i);
								TCComponent dvprItem = dvprDetailInfo.getReferenceProperty("vf3_dvpr_item");
								String dvprNum = dvprItem.getPropertyDisplayableValue("item_id");
								if(masterData.containsKey(dvprNum)) {
									LinkedHashMap<String, String> prop2Value = masterData.get(dvprNum);
									HashMap<String, DataManagementService.VecStruct> propertyMap = new HashMap<String, DataManagementService.VecStruct>();
									for (Entry<String, String> entry : prop2Value.entrySet()) {
										String prop = entry.getKey();
										String value = entry.getValue();
										
										if(mapPropDesc.get(prop).isModifiable == false) {
											continue;
										}
										TCComponentType objType = dvprDetailInfo.getTypeComponent();
										int propType = objType.getPropDesc(prop).getType();
										switch (propType) {
										case PropertyDescription.CLIENT_PROP_TYPE_date:
											if (!value.isEmpty()) {
												Calendar cal = Calendar.getInstance();
												SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
												cal.setTime(formatter.parse(value));
												String[] propertyDateValues = { Utils.CalendarToTcDateString(cal) };
												DataManagementService.VecStruct vecStructDate = new DataManagementService.VecStruct();
												vecStructDate.stringVec = propertyDateValues;
												propertyMap.put(prop, vecStructDate);
											}else {
												DataManagementService.VecStruct vecStructDate = new DataManagementService.VecStruct();
												String[] propertyDateValues = { "" };
												vecStructDate.stringVec = propertyDateValues;
												propertyMap.put(prop, vecStructDate);
											}
											break;
										case PropertyDescription.CLIENT_PROP_TYPE_string:
											String[] propertyStringValues = { value };
											DataManagementService.VecStruct vecStructString = new DataManagementService.VecStruct();
											vecStructString.stringVec = propertyStringValues;
											propertyMap.put(prop, vecStructString);
											break;
										case PropertyDescription.CLIENT_PROP_TYPE_double:
//											if (entry.getValue().isEmpty()) {
//												LOGGER.warn("[updateSourcingPart] BLANK value - propery: " + entry.getKey());
//												continue;
//											}
//											if (!VFUtility.isNumeric(entry.getValue())) {
//												LOGGER.error("[updateSourcingPart] INVALID value - property: " + entry.getKey() + "|value: " + entry.getValue() + "|Part: " + partID);
//												continue;
//											}
											String[] propertyDoubleValues = { value };
											DataManagementService.VecStruct vecStructDouble = new DataManagementService.VecStruct();
											vecStructDouble.stringVec = propertyDoubleValues;
											propertyMap.put(prop, vecStructDouble);
										default:
											break;
										}
										
										ServiceData serviceData = DataManagementService.getService(session).setProperties(new TCComponent[] { dvprDetailInfo }, propertyMap);
										if (serviceData.sizeOfPartialErrors() > 0) {
											MessageBox.post("Exception: " + Utils.HanlderServiceData(res.serviceData), "ERROR", MessageBox.ERROR);
											return;
										}
									}
								}
							}
						}catch (ServiceException | TCException | NotLoadedException | ParseException e1) {
							MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
							e1.printStackTrace();
						}
					}
					dialog.setMessage("Update succesfully!");
					dialog.btnOK.setEnabled(false);
				}
			});
			
			dialog.open();

		return null;
	}

	public int readCsv(String csvFile) {
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "ISO_8859_1")));
			String[] tempArr;
			int counter = 1;
			while ((tempArr = csvReader.readNext()) != null) {
				LinkedHashMap<String, String> prop2Value = new LinkedHashMap<String, String>();
				if (counter == 1) {
					if (tempArr[0].compareToIgnoreCase("DVPR Number") != 0) {
						return counter;
					} else {
						for(String displayName : tempArr) {
							if(displayName.trim().compareToIgnoreCase("Risk Assessment") == 0) {
								propRealName.add("vf3_risk_assessment");
							}else if(displayName.trim().compareToIgnoreCase("Comment") == 0) {
								propRealName.add("vf3_comment");
							}else if(displayName.trim().compareToIgnoreCase("DVPR Number") == 0) {
								propRealName.add("vf3_dvpr_item");
							}else if(displayName.trim().compareToIgnoreCase("Target met") == 0) {
								propRealName.add("vf3_target_met");
							}else if(displayName.trim().compareToIgnoreCase("Percentage Completed") == 0) {
								propRealName.add("vf3_percentage_completed");
							}
						}
					}
				} else {
					if (tempArr[0].isEmpty()) {
						return counter;
					} else if (tempArr.length != propRealName.size()) {
						return counter;
					} else {
						for (int i = 0; i < tempArr.length; i++) {
							if (propRealName.get(i).compareToIgnoreCase("vf3_risk_assessment") == 0) {
								prop2Value.put("vf3_risk_assessment", tempArr[i].trim());
							} else if (propRealName.get(i).compareToIgnoreCase("vf3_comment") == 0) {
								prop2Value.put("vf3_comment", tempArr[i].trim());
							} else if (propRealName.get(i).compareToIgnoreCase("vf3_target_met") == 0) {
								prop2Value.put("vf3_target_met", tempArr[i].trim());
							} else if (propRealName.get(i).compareToIgnoreCase("vf3_percentage_completed") == 0) {
								prop2Value.put("vf3_percentage_completed", tempArr[i].trim());
							}
						}
						masterData.put(tempArr[0], prop2Value);
					}
				}
				counter++;
			}
			csvReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -1;
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return 0;
	}

}
