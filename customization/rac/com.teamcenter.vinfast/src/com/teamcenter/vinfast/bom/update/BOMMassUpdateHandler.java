package com.teamcenter.vinfast.bom.update;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.opencsv.CSVReader;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
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
import com.teamcenter.vinfast.model.BOMMassUpdateModel;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BOMMassUpdateHandler extends AbstractHandler {
	private TCSession session;
	private static DataManagementService dmService = null;
	private BOMMassUpdateDialog dlg;
	private TCComponentBOMLine selectObject = null;
	private LinkedHashMap<String, BOMMassUpdateModel> updateList = null;
	private LinkedList<BOMMassUpdateModel> wrongList = null;
	private HashMap<String, HashMap<String, ArrayList<String>>> moduleValidate = null;
	private List<String> propRealName = new ArrayList<String>();
	private String[] group = { "dba" };

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);

		if (!TCExtension.checkPermission(group, session)) {
			MessageBox.post("You don't have permission to use this command.", "WARNING", MessageBox.WARNING);
			return null;
		}

		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 1) {
			MessageBox.post("Please select one root bomline", "WARNING", MessageBox.WARNING);
			return null;
		}

		selectObject = (TCComponentBOMLine) targetComp[0];
		getModuleMapping();

		MessageBox.post("Please expand all EBOM first. Otherwise, it may take a long time", "INFO", MessageBox.WARNING);

		dlg = new BOMMassUpdateDialog(new Shell());
		dlg.create();

		dlg.btnAttachment.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog fd = new FileDialog(dlg.getShell(), SWT.SELECTED);
				fd.setFilterPath("C:/");
				String[] filterExt = { "*.csv" };
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if (selected != null)
					dlg.textAttachment.setText(selected);

				if (!selected.isEmpty()) {
					if (readCsv(selected)) {
						dlg.setMessage("Read file succesfully");
						refreshTable(false);
					} else {
						dlg.setMessage("Internal error while reading csv file");
					}
				}
			}
		});

		dlg.btnOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dlg.setMessage("Updating...");

				massUpdate();
				refreshTable(true);

				dlg.setMessage("Update succesfully. Please click on Save button to finish");
				dlg.btnOK.setEnabled(false);
			}
		});

		dlg.open();

		return null;
	}

	private void massUpdate() {
		if (updateList.size() == 0)
			return;

		try {
			PropDescInfo propDescInfo = new PropDescInfo();
			propDescInfo.typeName = "BOMLine";
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

			LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
			expandBOMLines(allBOMLineInWindow, selectObject);

			Set<String> propertyAdding = new HashSet<>(Arrays.asList(new String[] { "bl_item_item_id", "VL5_pos_id" }));
			propertyAdding.addAll(propRealName);
			dmService.getProperties(allBOMLineInWindow.toArray(new TCComponentBOMLine[0]), propertyAdding.toArray(new String[0]));

			for (TCComponentBOMLine oneLine : allBOMLineInWindow) {
				String itemId = oneLine.getPropertyDisplayableValue("bl_item_item_id");
				String posId = oneLine.getPropertyDisplayableValue("VL5_pos_id");
				String key = itemId + posId;
				if (updateList.containsKey(key)) {
					BOMMassUpdateModel bomUpdate = updateList.get(key);
					LinkedHashMap<String, String> prop2Value = bomUpdate.getUpdateProperties();
					HashMap<String, DataManagementService.VecStruct> propertyMap = new HashMap<String, DataManagementService.VecStruct>();
					for (Entry<String, String> entry : prop2Value.entrySet()) {
						String prop = entry.getKey();
						String value = entry.getValue();

						if (!mapPropDesc.get(prop).isModifiable) {
							bomUpdate.setUpdateMessage("Property: " + prop + " can not modify. \n");
							continue;
						}

						TCComponentType objType = oneLine.getTypeComponent();
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
							} else {
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
//									if (entry.getValue().isEmpty()) {
//										LOGGER.warn("[updateSourcingPart] BLANK value - propery: " + entry.getKey());
//										continue;
//									}
//									if (!VFUtility.isNumeric(entry.getValue())) {
//										LOGGER.error("[updateSourcingPart] INVALID value - property: " + entry.getKey() + "|value: " + entry.getValue() + "|Part: " + partID);
//										continue;
//									}
							String[] propertyDoubleValues = { value };
							DataManagementService.VecStruct vecStructDouble = new DataManagementService.VecStruct();
							vecStructDouble.stringVec = propertyDoubleValues;
							propertyMap.put(prop, vecStructDouble);
						default:
							break;
						}

						ServiceData serviceData = dmService.setProperties(new TCComponentBOMLine[] { oneLine }, propertyMap);
						if (serviceData.sizeOfPartialErrors() > 0) {
							bomUpdate.setUpdateMessage("Exception: " + TCExtension.hanlderServiceData(serviceData) + "\n");
						}
					}
				}
			}
		} catch (TCException | NotLoadedException | ServiceException | ParseException e1) {
			MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
			e1.printStackTrace();
		}
	}

	private boolean readCsv(String csvFile) {
		List<String> propRealName = new ArrayList<String>();
		CSVReader csvReader = null;
		try {
			updateList = new LinkedHashMap<>();
			wrongList = new LinkedList<>();
			csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "ISO_8859_1")));
			String[] tempArr;
			int counter = 1;
			while ((tempArr = csvReader.readNext()) != null) {
				if (counter == 1) {
					if (tempArr[0].compareToIgnoreCase("bl_item_item_id") != 0 || tempArr[1].compareToIgnoreCase("VL5_pos_id") != 0) {
						return false;
					} else {
						propRealName = Arrays.asList(tempArr);
					}
				} else {
					BOMMassUpdateModel newItem = new BOMMassUpdateModel();
					if (tempArr[0].isEmpty() || tempArr[1].isEmpty()) {
						newItem.setLineNo(counter);
						newItem.setValidateMessage("Part Number and POSID must not null.");
						wrongList.add(newItem);
					} else if (tempArr.length != propRealName.size()) {
						newItem.setLineNo(counter);
						newItem.setValidateMessage("Wrong format.");
						wrongList.add(newItem);
					} else {
						LinkedHashMap<String, String> prop2Value = new LinkedHashMap<String, String>();
						for (int i = 0; i < tempArr.length; i++) {
							if (i > 1) {
								prop2Value.put(propRealName.get(i), tempArr[i]);
							}
						}

						if (isModuleInfoValid(prop2Value)) {
							newItem.setLineNo(counter);
							newItem.setPartNumber(tempArr[0]);
							newItem.setPosID(tempArr[1]);
							newItem.setUpdateProperties(prop2Value);
							updateList.put(tempArr[0] + tempArr[1], newItem);
						} else {
							newItem.setValidateMessage("Module not valid.");
							wrongList.add(newItem);
						}
					}
				}
				counter++;
			}
			csvReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private void refreshTable(Boolean resultProcess) {
		if (resultProcess) {
			// ----------------------------------------------
			StringBuilder updateText = new StringBuilder();
			updateText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
			updateText.append(StringExtension.htmlTableCss);
			updateText.append("<body style=\"margin: 0px;\">");
			updateText.append("<table>");
			LinkedHashMap<String, String> header1 = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("No", "5");
					put("Line", "10");
					put("Message", "85");
				}
			};
			updateText.append(StringExtension.genTableHeader(header1));
			//
			int i = 0;
			for (BOMMassUpdateModel wrongItem : wrongList) {
				updateText.append("<tr style='font-size: 12px; text-align: center'>");
				updateText.append("<td>" + String.valueOf(++i) + "</td>");
				updateText.append("<td>" + wrongItem.getLineNo() + "</td>");
				if (wrongItem.getUpdateMessage().isEmpty())
					updateText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Update success.") + "</td>");
				else
					updateText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(wrongItem.getValidateMessage()) + "</td>");
				updateText.append("</tr>");
			}
			//
			updateText.append("</table>");
			updateText.append("</body></html>");
			dlg.brwUpdate.setText(updateText.toString());
		} else {
			StringBuilder wrongText = new StringBuilder();
			wrongText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
			wrongText.append(StringExtension.htmlTableCss);
			wrongText.append("<body style=\"margin: 0px;\">");
			wrongText.append("<table>");
			LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("No", "5");
					put("Line", "10");
					put("Message", "85");
				}
			};
			wrongText.append(StringExtension.genTableHeader(header));
			//
			int i = 0;
			for (BOMMassUpdateModel wrongItem : wrongList) {
				wrongText.append("<tr style='font-size: 12px; text-align: center'>");
				wrongText.append("<td>" + String.valueOf(++i) + "</td>");
				wrongText.append("<td>" + wrongItem.getLineNo() + "</td>");
				wrongText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(wrongItem.getValidateMessage()) + "</td>");
				wrongText.append("</tr>");
			}
			//
			wrongText.append("</table>");
			wrongText.append("</body></html>");
			dlg.brwWrong.setText(wrongText.toString());

			// ----------------------------------------------
			StringBuilder updateText = new StringBuilder();
			updateText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
			updateText.append(StringExtension.htmlTableCss);
			updateText.append("<body style=\"margin: 0px;\">");
			updateText.append("<table>");
			LinkedHashMap<String, String> header1 = new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("No", "5");
					put("Line", "10");
					put("Message", "85");
				}
			};
			updateText.append(StringExtension.genTableHeader(header1));
			//
			i = 0;
			for (BOMMassUpdateModel wrongItem : wrongList) {
				updateText.append("<tr style='font-size: 12px; text-align: center'>");
				updateText.append("<td>" + String.valueOf(++i) + "</td>");
				updateText.append("<td>" + wrongItem.getLineNo() + "</td>");
				updateText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Ready to update.") + "</td>");
				updateText.append("</tr>");
			}
			//
			updateText.append("</table>");
			updateText.append("</body></html>");
			dlg.brwUpdate.setText(updateText.toString());
		}
	}

	private void expandBOMLines(LinkedList<TCComponentBOMLine> outBomLines, TCComponentBOMLine rootLine) throws TCException {
		outBomLines.add(rootLine);
		if (rootLine.getChildrenCount() > 0) {
			AIFComponentContext[] aifChilLines = rootLine.getChildren();
			for (AIFComponentContext aifChilLine : aifChilLines) {
				expandBOMLines(outBomLines, (TCComponentBOMLine) aifChilLine.getComponent());
			}
		}
	}

	private void getModuleMapping() {
		moduleValidate = new LinkedHashMap<>();
		String[] values = TCExtension.GetPreferenceValues("VF_PART_CREATE_MODULE_VALIDATE", session);
		if (values != null) {
			for (String value : values) {
				String[] str = value.split(";");
				if (str.length > 2) {
					String module1 = str[0];
					String module2 = str[1];
					String module3 = str[2];

					if (moduleValidate.containsKey(module1)) {
						HashMap<String, ArrayList<String>> oldModule1 = moduleValidate.get(module1);
						if (oldModule1.containsKey(module2)) {
							ArrayList<String> oldModule2 = oldModule1.get(module2);
							oldModule2.add(module3);
						} else {
							ArrayList<String> newModule3 = new ArrayList<>();
							newModule3.add(module3);
							oldModule1.put(module2, newModule3);
						}
					} else {
						ArrayList<String> newModule3 = new ArrayList<>();
						newModule3.add(module3);
						HashMap<String, ArrayList<String>> newModule2 = new LinkedHashMap<>();
						newModule2.put(module2, newModule3);
						moduleValidate.put(module1, newModule2);
					}
				}
			}
		}
	}

	private boolean isModuleInfoValid(LinkedHashMap<String, String> prop2Value) {
		String moduleGrp = null;
		String mainModule = null;
		String moduleName = null;

		for (Map.Entry<String, String> entry : prop2Value.entrySet()) {
			if (entry.getKey().compareTo("VL5_module_group") == 0)
				moduleGrp = entry.getValue();

			if (entry.getKey().compareTo("VL5_module_name") == 0)
				moduleName = entry.getValue();

			if (entry.getKey().compareTo("VL5_main_module") == 0)
				mainModule = entry.getValue();
		}

		if (moduleGrp == null || mainModule == null || moduleName == null)
			return true;

		if (moduleGrp.isEmpty() || moduleName.isEmpty() || mainModule.isEmpty())
			return false;

		if (!moduleValidate.containsKey(moduleGrp))
			return false;

		HashMap<String, ArrayList<String>> subModLst = moduleValidate.get(moduleGrp);
		if (subModLst == null || !subModLst.containsKey(mainModule))
			return false;

		ArrayList<String> modLst = subModLst.get(mainModule);
		if (modLst == null || !modLst.contains(moduleName))
			return false;

		return true;
	}
}
