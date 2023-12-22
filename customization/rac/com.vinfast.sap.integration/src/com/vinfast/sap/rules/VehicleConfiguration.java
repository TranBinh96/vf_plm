package com.vinfast.sap.rules;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCfg0ConfiguratorPerspective;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.configurator.ConfigManager;
import com.vinfast.sap.dialogs.ConfiguratorDialog;
import com.vinfast.sap.rules.RulesTransfer.ActionType;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.MCNValidator;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.sap.util.VFResponse;
import com.vinfast.url.SAPURL;

public class VehicleConfiguration extends AbstractHandler {
	private DataManagementService dmService = null;
	private RulesTransfer ruleTransfer = null;
	private HashMap<String, HashSet<String>> preIncludeRules = null;
	private String auth = PropertyDefines.SERVER_SAP_VF_AUTH;
	private ProgressMonitorDialog progressDlg = null;
	private ArrayList<LinkedHashMap<String, String>> loadValues = null;
	private boolean hasError;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) selectedObjects[0];
			TCSession session = changeRevision.getSession();
			dmService =  DataManagementService.getService(session);
			VFResponse response = MCNValidator.validate(selectedObjects);
			if(response.hasError() == true) {
				MessageBox.post(response.getErrorMessage(), "Error", MessageBox.ERROR);
				return null;
			}else {

				String company = UIGetValuesUtility.getCompanyCode(event);
				ConfigManager configManager = new ConfigManager(company).loadChangeAttachments(session, changeRevision);

				if (configManager.getModel().isEmpty() || configManager.getYear().isEmpty()) {
					MessageBox.post("Error to get Plaform and Model Year. Contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
					return false;
				}

				ruleTransfer = new RulesTransfer();
				preIncludeRules = new HashMap<String, HashSet<String>>();

				ConfiguratorDialog configDialog = new ConfiguratorDialog(new Shell());
				configDialog.create();
				configDialog.setTitle("Rules Transfer");
				configDialog.setModel(configManager.getModel());
				configDialog.setYear(configManager.getYear());
				configDialog.setMCN(configManager.getMCN());
				configDialog.setPlant(configManager.getPlant());
				configDialog.setServer("PRODUCTION");
				configDialog.getPrepareButton().addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								loadValues = loadDataTransfer(configDialog, configManager);
								if(loadValues.isEmpty()) {
									MessageBox.post("Failed to load Rules. Please contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
								}else {
									StringBuilder strBuilder = writeToReport(configManager);
									File logFile = Logger.writeBufferResponse(strBuilder.toString(), UIGetValuesUtility.createLogFolder("MCN" + configManager.getMCN()), "RULES");
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											try {
												StringViewerDialog viewdialog = new StringViewerDialog(logFile);
												viewdialog.setTitle("Transfer Status");
												viewdialog.setSize(600, 400);
												viewdialog.setLocationRelativeTo(null);
												viewdialog.setVisible(true);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									});
									configDialog.getOkButton().setEnabled(true);
									
								}
							}
						});
					}
				});
				configDialog.getOkButton().addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								String server = configDialog.getServer();
								File logFile = transferData(configDialog,configManager, loadValues);
								if (logFile != null && server.equals("PRODUCTION") && hasError == false) {
									TCComponentDataset newHTMLDataset = UIGetValuesUtility.createDataset(dmService, configManager.getChangeObject(), "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
									if (newHTMLDataset != null) {
										UIGetValuesUtility.uploadNamedReference(session, newHTMLDataset, logFile, "HTML", true, true);
									}

									String dataset_name = String.format("%s_%s_IncludeRules", configManager.getModel(), configManager.getYear());
									File includeRulesStructure = ruleTransfer.uploadCurrentIncludeRules(session, preIncludeRules, dataset_name);
									TCComponentDataset newTextDataset = UIGetValuesUtility.createDataset(dmService, null, null, dataset_name, dataset_name, "Text", "TextEditor");

									if (newTextDataset == null) {
										MessageBox.post("Failed to create report. Please contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
									} else {
										UIGetValuesUtility.uploadNamedReference(session, newTextDataset, includeRulesStructure, "Text", true, true);
									}									
								}
								configDialog.close();
							}
						});
					}
				});
				configDialog.getOkButton().setEnabled(false);
				configDialog.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected ArrayList<LinkedHashMap<String, String>> loadDataTransfer(ConfiguratorDialog configDialog, ConfigManager configManager) {
		ArrayList<LinkedHashMap<String, String>> ruleValues = new ArrayList<LinkedHashMap<String, String>>();
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(configDialog.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Preparing Data...", IProgressMonitor.UNKNOWN);
					try {
						String platFormCode = configManager.getModel();
						String modelYear = configManager.getYear();
						String dataset_name = String.format("%s_%s_IncludeRules", platFormCode, modelYear);
						monitor.subTask("Loading previous rules...");
						preIncludeRules = loadPreIncludeRules(configManager, dataset_name);
						monitor.subTask("Loading Problem Rules...");
						ruleValues.addAll(getRules(configManager, configManager.getProblemItems(), ActionType.ACTION_DELETE));
						monitor.subTask("Loading Solution Rules...");
						ruleValues.addAll(getRules(configManager, configManager.getSolutionItems(), ActionType.ACTION_ADD));
					}catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ruleValues;
	}

	private File transferData(ConfiguratorDialog dialog, ConfigManager configManager ,ArrayList<LinkedHashMap<String, String>> loadValues) {
		hasError = false;
		String server = dialog.getServer();
		SAPURL SAPConnect = new SAPURL();
		StringBuilder strBuilder = new StringBuilder();
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + configManager.getMCN());
		try {
			if (progressDlg == null)
				progressDlg = new ProgressMonitorDialog(dialog.getShell());
			progressDlg.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Transferring Data...", IProgressMonitor.UNKNOWN);
					try {
						monitor.subTask("Sending to SAP...");
						String SERVER_IP = UIGetValuesUtility.SAPSERVERIP(server);
						int count = 1;
						strBuilder.append("<html><body>");
						String[] printValues = new String[] { "Model : " + configManager.getModel() + "_" + configManager.getYear(), "MCN :" + configManager.getMCN(), "User : " + configManager.getSession().getUserName(), "Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
						Logger.bufferResponse("DETAILS", printValues, strBuilder);
						printValues = new String[] { "S.No", "Rules", "Message", "Type", "Action", "Result" };
						Logger.bufferResponse("HEADER", printValues, strBuilder);
						try {
							for (LinkedHashMap<String, String> dataMap : loadValues) {
								String filename = dataMap.get("RULETYPE") + "_" + dataMap.get("RULEID") + "_" + dataMap.get("OPTIONID");
								String[] message = CreateSoapHttpRequest.sendRequest(SAPConnect.rulesWebserviceURL(SERVER_IP), dataMap, SAPURL.RUL_HEADER, SAPURL.RUL_TAG, SAPURL.RUL_NAMESPACE, "I_" + filename, logFolder, auth);
								if (message[0].equals("S") || message[1].contains("Record is exist in staging table")) {
									printValues = new String[] { Integer.toString(count), filename, "ok", dataMap.get("RULETYPE"), dataMap.get("ACTION"), "Success" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
								}else {
									hasError = true;
									printValues = new String[] { Integer.toString(count), filename, "fail", dataMap.get("RULETYPE"), dataMap.get("ACTION"), "Error" };
									Logger.bufferResponse("PRINT", printValues, strBuilder);
								}
								count++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						strBuilder.append("</table>");
						String data = Logger.previousTransaction(logFolder, "RULES");

						if (!data.equals("")) {
							strBuilder.append(data);
							strBuilder.append("<br>");
						}

						strBuilder.append("</body></html>");
					}catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		

		File logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "RULES");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StringViewerDialog viewdialog = new StringViewerDialog(Logger.writeBufferResponse(strBuilder.toString(), logFolder, "RULES"));
					viewdialog.setTitle("Transfer Status");
					viewdialog.setSize(600, 400);
					viewdialog.setLocationRelativeTo(null);
					viewdialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return logFile;
	}

	private StringBuilder writeToReport(ConfigManager configManager) {

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<html><body>");
		String[] printValues = new String[] { "Model : " + configManager.getModel() + "_" + configManager.getYear(), "MCN :" + configManager.getMCN(), "User : " + configManager.getSession().getUserName(), "Time : " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		Logger.bufferResponse("DETAILS", printValues, strBuilder);
		printValues = new String[] { "S.No", "Rules", "Message", "Type", "Action", "Result" };
		Logger.bufferResponse("HEADER", printValues, strBuilder);
		try {
			int count = 1;
			for (LinkedHashMap<String, String> dataMap : loadValues) {
				String filename = dataMap.get("RULETYPE") + "_" + dataMap.get("RULEID") + "_" + dataMap.get("OPTIONID");
				printValues = new String[] { Integer.toString(count), filename, "Record will be sent to SAP", dataMap.get("RULETYPE"), dataMap.get("ACTION"), "Ok" };
				Logger.bufferResponse("PRINT", printValues, strBuilder);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		strBuilder.append("</table>");
		strBuilder.append("</body></html>");

		return strBuilder;
	}

	private ArrayList<LinkedHashMap<String, String>> getRules(ConfigManager configManager ,TCComponent[] items, ActionType action) throws Exception {
		ArrayList<LinkedHashMap<String, String>> ruleValues = new ArrayList<LinkedHashMap<String, String>>();
		if (items != null && items.length > 0) {
			for (TCComponent rule : items) {
				String ruleType = rule.getPropertyDisplayableValue("object_type");
				if (ruleType.equals("Configurator Context")) {
					TCComponentCfg0ConfiguratorPerspective contextPerspective = (TCComponentCfg0ConfiguratorPerspective) configManager.getTopLevelItemRevision().getRelatedComponent("cfg0ConfigPerspective");

					TCComponent[] AVRules = contextPerspective.getRelatedComponents("cfg0AvailabilityRules");

					TCComponent[] DFRules = contextPerspective.getRelatedComponents("cfg0DefaultRules");

					TCComponent[] INRules = contextPerspective.getRelatedComponents("cfg0IncludeRules");

					if (AVRules != null) {
						for (TCComponent AVRule : AVRules) {
							ruleValues.add(ruleTransfer.getAvailabilityRules(action, configManager, AVRule));
						}
					}

					if (DFRules != null) {
						for (TCComponent DFRule : DFRules) {
							ruleValues.add(ruleTransfer.getDefaultRules(action, configManager, DFRule));
						}
					}

					if (INRules != null) {
						for (TCComponent INRule : INRules) {
							ruleValues.addAll(ruleTransfer.getInclusionRules(configManager, INRule, preIncludeRules));
						}
					}
				} else if (ruleType.equals("Default Rule")) {
					ruleValues.add(ruleTransfer.getDefaultRules(action, configManager, rule));
				} else if (ruleType.equals("Availability Rule")) {
					ruleValues.add(ruleTransfer.getAvailabilityRules(action, configManager, rule));
				} else if (ruleType.equals("Inclusion Rule")) {
					ruleValues.addAll(ruleTransfer.getInclusionRules(configManager, rule, preIncludeRules));
				}
			}
		}
		return ruleValues;
	}

	private HashMap<String, HashSet<String>> loadPreIncludeRules(ConfigManager configManager , String dataset_name) {
		HashMap<String, HashSet<String>> preIncludeRules = new HashMap<String, HashSet<String>>();
		File oldFile = null;
		TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(configManager.getSession(), new String[] { "Name", "Dataset Type" }, new String[] { dataset_name, "Text" }, "Dataset...");
		if (oldDatasets != null) {
			HashMap<Date, TCComponent> datasetMap = new HashMap<Date, TCComponent>();
			dmService.getProperties(oldDatasets, new String[] { "creation_date" });
			for (TCComponent dataset : oldDatasets) {
				try {
					Date date_creation = dataset.getDateProperty("creation_date");
					datasetMap.put(date_creation, dataset);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet());
			Collections.sort(sortedKeys, Collections.reverseOrder());
			TCComponentDataset olddataset = (TCComponentDataset) datasetMap.get(sortedKeys.get(0));
			oldFile = UIGetValuesUtility.downloadDataset(configManager.getSession(), System.getProperty("java.io.tmpdir"), olddataset);
			if (oldFile != null) {
				preIncludeRules = ruleTransfer.previousIncludeRules(configManager.getSession(), oldFile);
			}
		}
		return preIncludeRules;
	}

}