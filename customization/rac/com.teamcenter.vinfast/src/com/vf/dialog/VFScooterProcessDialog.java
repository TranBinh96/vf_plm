package com.vf.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.lov.view.components.LOVDisplayer;
import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.rac.workflow.commands.assignmentlist.AssignAllTasksOperation;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.AssignmentLists;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.Resources;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.handlers.StartSourcing;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.Query;
import com.vf.utils.VFAssignmentList;

public class VFScooterProcessDialog extends VFAbstractProcessDialog {

	public enum WorkflowListMode {
		GET_FROM_REFRERENCE_VF_Program_Workflow, GET_WORKFLOW_LIST_FILTERED_BY_GROUP;
	}

	public final static String VF_PROGRAM_NAMES = "VF_Program_Names";
	private static final Logger logger = Logger.getLogger(VFSourcingProcessDialog.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private iComboBox icb_workflow = null;
	private iComboBox icb_module = null;
	private LOVDisplayer icb_model = null;
	private JList icb_programCOP = null;
	private JTabbedPane tabPanel;
	private iButton ib_Start;
	private String validationResultText = "";
	private JEditorPane jEditorPane;
	private JEditorPane jEditorPaneCOP;
	private final WorkflowService wfService;
	private final TCSession session;
	private final boolean isCOPPartProcess;
	private final Map<String, List<TCComponent>> reviewTasksAndCOPReviewers;
	private TCComponent createdProcess = null;
	private PostAction postActionWhenStartedSuccessfully;
	private List<TCComponent> targets;
	private final String selectedProgram = "Scooter";

	private static boolean isOpen = false;

	public VFScooterProcessDialog(TCSession session, boolean isCOPPartProcess, PostAction postActionWhenStartedSuccessfully) {
		super();
		this.session = session;
		wfService = WorkflowService.getService(session);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.isCOPPartProcess = isCOPPartProcess;
		this.reviewTasksAndCOPReviewers = new HashMap<String, List<TCComponent>>();
		this.postActionWhenStartedSuccessfully = postActionWhenStartedSuccessfully;
	}

	public void createAndShowGUI(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final TCComponent[] targets, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final String dialogTitle) throws Exception {

		if (isOpen == true) {
			return;
		}

		this.targets = Arrays.asList(targets);

		createdProcess = null;
		isOpen = true;
		this.setResizable(false);
		// this.setAlwaysOnTop(true);

		final boolean[] checkAllPassed = new boolean[] { true };
		checkValidationResults(partsAndValidationResults, checkAllPassed);

		String[] prgm_values = input.keySet().toArray(new String[0]);

//		String[] workflows = utility.getPreferenceValues(session, "VF_Program_Workflow");
//		String[] prgm_values = utility.getPreferenceValues(session, "VF_Program_Names");
//		String[] programsTypesWorkflows = utility.getPreferenceValues(session, "VF_Program_Type_Workflow");//CUV~VF4_DesignRevision~C-SUV Release (P Status)
//		assignmentListValues = utility.getPreferenceValues(session, "VF_Module_AssignmentList");

//		if (workflows == null || prgm_values == null || assignmentListValues == null) {
//			logger.error("VF_Program_Workflow or VF_Program_Names or VF_Module_AssignmentList preferences are not setup!");
//			return;
//		}

		setTitle(dialogTitle);

		JPanel sourcingPanel = new JPanel();
		sourcingPanel.setLayout(null);
		sourcingPanel.setBackground(Color.white);

		sourcingPanel.setPreferredSize(new Dimension(647, 682));

		Icon start_Icon = new ImageIcon(getClass().getResource("/icons/server_busystate_16.png"));

		JLabel jl_model = new JLabel("Model:");
		jl_model.setBounds(15, 55, 100, 25);
		TCPropertyDescriptor lovDescriptor = session.getTypeComponent("VF3_Scooter_part").getPropertyDescriptor("vf4_es_model_veh_line");
		icb_model = new LOVDisplayer(lovDescriptor);
		icb_model.setBounds(90, 55, 370, 25);
		icb_model.setMandatory(true);
		sourcingPanel.add(jl_model);

		ib_Start = new iButton("Start");
		ib_Start.setIcon(start_Icon);
		ib_Start.setBounds(470, 55, 90, 25);
		sourcingPanel.add(ib_Start);

		JLabel jl_module = new JLabel("Module:");
		jl_module.setBounds(15, 93, 100, 25);
		sourcingPanel.add(jl_module);
		icb_module = new iComboBox();
		icb_module.setBounds(90, 93, 200, 25);
		icb_module.setMandatory(true);
		sourcingPanel.add(icb_module);

		JLabel jl_workflow = new JLabel("Workflow:");
		jl_workflow.setBounds(15, 20, 95, 25);
		sourcingPanel.add(jl_workflow);
		icb_workflow = new iComboBox();
		icb_workflow.setBounds(90, 20, 200, 25);
		icb_workflow.setMandatory(true);
		sourcingPanel.add(icb_workflow);

		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Validation Report"));
		scrollPane.setBounds(15, 131, 622, 544);
		HTMLEditorKit kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);
//	        StyleSheet styleSheet = kit.getStyleSheet();
//	        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
		Document doc = kit.createDefaultDocument();
		jEditorPane.setDocument(doc);
		if (validationResultText.length() == 0 && partsAndValidationResults.size() == 0) {
			validationResultText = "No validation required.";
		}
		jEditorPane.setText(validationResultText);

		if (isCOPPartProcess) {
			icb_programCOP = new JList();
			icb_programCOP.setEnabled(false);

			jEditorPaneCOP = new JEditorPane();
			jEditorPaneCOP.setText("COP Assignment Description");
			jEditorPaneCOP.setEditable(false);
			JScrollPane scrollPaneCOP = new JScrollPane(jEditorPaneCOP);
			scrollPaneCOP.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "COP Assignment"));
			scrollPaneCOP.setBounds(207, 11, 402, 503);
			HTMLEditorKit kitCOP = new HTMLEditorKit();
			jEditorPaneCOP.setEditorKit(kitCOP);
			Document docCOP = kit.createDefaultDocument();
			jEditorPaneCOP.setDocument(docCOP);

			JPanel copPane = new JPanel();
			copPane.setLayout(null);
			copPane.add(icb_programCOP);
			copPane.add(scrollPaneCOP);

			JButton btnNewButton = new JButton("");
			Icon cleanAllImg = new ImageIcon(getClass().getResource("/icons/delete_16.png"));
			btnNewButton.setIcon(cleanAllImg);
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					icb_programCOP.clearSelection();
					cleanCOPViewer();
				}
			});
			btnNewButton.setBounds(9, 7, 16, 16);
			copPane.add(btnNewButton);

			icb_programCOP.setBorder(new TitledBorder(new LineBorder(new Color(192, 192, 192), 2), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(192, 192, 192)));
			JScrollPane scrollList = new JScrollPane(icb_programCOP);
			scrollList.setBounds(10, 23, 187, 491);
			copPane.add(scrollList);

			// pp.setBounds(0,0,0,0);

			tabPanel = new JTabbedPane(JTabbedPane.TOP);
			tabPanel.setBounds(15, 131, 622, 544);
			tabPanel.add("General", scrollPane);
			tabPanel.add("COP Prorams", copPane);

			sourcingPanel.add(tabPanel);
		} else {
			sourcingPanel.add(scrollPane);
		}

		icb_module.setEnabled(false);
		icb_workflow.setEnabled(false);
		if (icb_programCOP != null) {
			icb_programCOP.setEnabled(false);
		}
		ib_Start.setEnabled(false);
		sourcingPanel.add(icb_model);
		if (icb_model.getListeners(ActionListener.class).length == 0) {
			PropertyChangeListener programSelectionListener = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					handleProgramSelection(input, checkAllPassed);
				}
			};
			icb_model.addPropertyChangeListener(programSelectionListener);
		}

		ActionListener ib_StartListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handlerStartButton(targets);
				} catch (Exception e1) {
					logger.error(e1);
					setAlwaysOnTop(false);
					MessageBox.post(e1);
				}
			}

		};
		ib_Start.addActionListener(ib_StartListener);

		getContentPane().add(sourcingPanel);
		pack();
		setLocationRelativeTo(null);

//		String defaultProgram = getProgram();
//		if (icb_model.getItems().length == 1) {
//			icb_model.setSelectedIndex(0);
//			handleProgramSelection(input, checkAllPassed);
//
//			if (icb_workflow.getItems().length == 1) {
//				icb_workflow.setSelectedIndex(0);
//				handleWorkflowSelection(checkAllPassed, input);
//
//				if (icb_module.getItems().length == 1) {
//					handleModuleSelection(checkAllPassed);
//				}
//			}
//		} else if (defaultProgram.isEmpty() == false){
//			icb_model.setSelectedItem(defaultProgram);
//			handleProgramSelection(input, checkAllPassed);
//			
//			if (icb_workflow.getItems() != null && icb_workflow.getItems().length == 1) {
//				icb_workflow.setSelectedIndex(0);
//				handleWorkflowSelection(checkAllPassed, input);
//
//				if (icb_module.getItems().length == 1) {
//					handleModuleSelection(checkAllPassed);
//				}
//			}
//		}

		setProgramToScooter(input, checkAllPassed);

		setVisible(true);
	}

	private void setProgramToScooter(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final boolean[] checkAllPassed) {
		final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
		if (workflowAndInput != null) {
			String[] workflowList = workflowAndInput.keySet().toArray(new String[0]);
			icb_workflow.setEnabled(true);
			icb_workflow.setItems(workflowList);

			if (icb_workflow.getListeners(ActionListener.class).length == 0) {
				ActionListener workflowSelectionListener = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							handleWorkflowSelection(checkAllPassed, input);
						} catch (Exception e1) {
							logger.error(e1);
							setAlwaysOnTop(false);
							MessageBox.post(e1);
						}
					}

				};
				icb_workflow.addActionListener(workflowSelectionListener);
			}

			if (workflowList.length == 1) {
				icb_workflow.setSelectedIndex(0);
			}
		}
	}

	private void checkValidationResults(final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final boolean[] checkAllPassed) throws TCException {
		validationResultText = "";
		checkAllPassed[0] = true;
		for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : partsAndValidationResults.entrySet()) {
			List<ValidationResult> validationResults = partsAndValidationResult.getValue();
			validationResultText += ValidationResult.createHtmlText(partsAndValidationResult) + "</br></br>";
			boolean checkPassed = ValidationResult.checkValidationResults(validationResults);
			if (checkPassed == false) {
				checkAllPassed[0] = false;
			}
		}
		if (jEditorPane != null) {
			jEditorPane.setText(validationResultText);
		}
	}

	private boolean submitToWorkflow(TCComponent[] targets, String workflow, final String assignmentList, final String model) throws TCException {

		final boolean[] isSuccess = { false };

		int count = 0;
		int lineCount = targets.length;

		String[] keys = new String[lineCount];
		TCComponent[] values = new TCComponent[lineCount];

		for (count = 0; count < targets.length; count++) {
			keys[count] = (targets[count] instanceof TCComponentBOMLine) ? ((TCComponentBOMLine) targets[count]).getItemRevision().getUid() : targets[count].getUid();
			values[count] = targets[count];
		}

		if (!workflow.equals("")) {
			int[] attachmentTypes = new int[keys.length];
			Arrays.fill(attachmentTypes, 1);
			final TCComponent bl = (TCComponent) values[0];
			final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data = new com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData();
			data.attachmentCount = lineCount;
			data.attachments = keys;
			data.processTemplate = workflow;
			data.attachmentTypes = attachmentTypes;
			// data.processAssignmentList = list[0].getName();

			final List<String> validators = new LinkedList<String>();
			final StringBuffer partsErrorMsg = new StringBuffer();
			session.queueOperationAndWait(new Job("Trigering workflow") {

				@Override
				protected IStatus run(IProgressMonitor arg0) {
					return handleWorkflowTrigger(assignmentList, isSuccess, bl, data, validators);
				}
			});

			if (isSuccess[0] == false) {
				for (String validator : validators) {
					String errorMsg = validator;
					if (errorMsg.isEmpty() == false) {
						partsErrorMsg.append("-").append(errorMsg);
						partsErrorMsg.append("\n");
					}
				}
				if (partsErrorMsg.length() != 0) {
					jEditorPane.setText(partsErrorMsg.toString());
				}
			}
		}

		return isSuccess[0];
	}

	@Override
	public void dispose() {
		isOpen = false;
		super.dispose();
	}

	private void handleProgramSelection(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final boolean[] checkAllPassed) {
//		if (icb_model.getSelectedItem() != null) {
//			ib_Start.setEnabled(false);
//			icb_workflow.removeAllItems();
//			icb_module.removeAllItems();
//			if (isCOPPartProcess) {
//				icb_programCOP.setListData(new Object[0]);
//				cleanCOPViewer();
//			}
//
//			String selectedProgram = icb_model.getSelectedItem().toString();
//			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
//			if (workflowAndInput != null) {
//				String[] workflowList = workflowAndInput.keySet().toArray(new String[0]);
//				icb_workflow.setEnabled(true);
//				icb_workflow.setItems(workflowList);
//				
//				if (icb_workflow.getListeners(ActionListener.class).length == 0) {
//					ActionListener workflowSelectionListener = new ActionListener() {
//
//						@Override
//						public void actionPerformed(ActionEvent e) {
//							try {
//								handleWorkflowSelection(checkAllPassed, input);
//							} catch (Exception e1) {
//								logger.error(e1);
//								setAlwaysOnTop(false);
//								MessageBox.post(e1);
//							}
//						}
//
//					};
//					icb_workflow.addActionListener(workflowSelectionListener);
//				}
//			}
//		} else {
//			ib_Start.setEnabled(false);
//			icb_workflow.setEnabled(false);
//			icb_workflow.removeAllItems();
//			icb_module.setEnabled(false);
//			icb_module.removeAllItems();
//			if (isCOPPartProcess) {
//				icb_programCOP.setEnabled(false);
//				icb_programCOP.setListData(new Object[0]);
//			}
//		}

		if (icb_model.getSelectedDisplayValue() == null) {
			ib_Start.setEnabled(false);
		}
	}

	private void cleanCOPViewer() {
		jEditorPaneCOP.setText("<body></body>");
		reviewTasksAndCOPReviewers.clear();
	}

	private void handleWorkflowSelection(final boolean[] checkAllPassed, final Map<String, Map<String, VFProcessDialogProcessInput>> input) throws Exception {
		if (icb_workflow.getSelectedItem() != null) {
			if (isCOPPartProcess) {
				icb_programCOP.setListData(new Object[0]);
				cleanCOPViewer();
			}
			icb_module.removeAllItems();

			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
			String selectedWorkflow = icb_workflow.getSelectedItem().toString();
			VFProcessDialogProcessInput processInput = workflowAndInput.get(selectedWorkflow);
			if (processInput != null) {
				icb_module.setEnabled(true);
				icb_module.setItems(processInput.getAssignmentList().toArray(new VFAssignmentList[0]));
				String defaultAssignmentList = processInput.getDefaultAssignmentList();
				if (defaultAssignmentList != null && !defaultAssignmentList.isEmpty()) {
					for (VFAssignmentList assList : processInput.getAssignmentList()) {
						if (assList.getName().contains(defaultAssignmentList)) {
							icb_module.setSelectedItem(assList);
							ib_Start.setEnabled(checkAllPassed[0]);
							break;
						}
					}
				}

				IValidator validator = workflowAndInput.get(selectedWorkflow).getValidator();
				Map<TCComponent, List<ValidationResult>> partsValRes = new HashMap<TCComponent, List<ValidationResult>>();
				if (validator != null) {
					for (TCComponent target : targets) {
						List<ValidationResult> valRes = validator.validate((TCComponentBOMLine) target);
						partsValRes.put(target, valRes);
					}
				}
				checkValidationResults(partsValRes, checkAllPassed);

				if (icb_module.getListeners(ActionListener.class).length == 0) {
					ActionListener moduleSelectionListener = new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							handleModuleSelection(checkAllPassed);
						}
					};
					icb_module.addActionListener(moduleSelectionListener);
				}

				if (icb_module.getItemCount() == 0) {
					ib_Start.setEnabled(true);
				} else if (icb_module.getSelectedIndex() < 0) {
					ib_Start.setEnabled(false);
				}
			}

		} else {
			ib_Start.setEnabled(false);
			icb_module.setEnabled(false);
			icb_module.removeAllItems();
		}
	}

	private IStatus handleWorkflowTrigger(final String assignmentList, final boolean[] isSuccess, final TCComponent bl, final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data, final List<String> validators) {
		InstanceInfo intInfo = wfService.createInstance(true, null, "EScooter - " + bl.toString(), null, "EScooter - " + bl.toString(), data);
		ServiceData output = intInfo.serviceData;

		if (output.sizeOfCreatedObjects() > 0) {
			createdProcess = output.getCreatedObject(0);
		}
		if (createdProcess != null) {
			try {
				// reviewTasksAndCOPReviewers
				if (assignmentList != null && assignmentList.isEmpty() == false) {
					final AssignmentLists lists = wfService.getAssignmentLists(new String[] { assignmentList });
					TCComponentAssignmentList[] list = lists.assignedLists;
					ResourceMember[] resmems = null;
					// newProcess[0]
					if (list != null) {
						AIFComponentContext[] reses = list[0].getRelated("resources");
						AIFComponentContext[] tasks = list[0].getRelated("task_templates");

						if (reses != null && tasks != null && reses.length > 0 && tasks.length == reses.length) {
							Resources[] resourcesTemp = new Resources[tasks.length];
							resmems = new ResourceMember[tasks.length];
							for (int i = 0; i < tasks.length; i++) {
								Resources res = new Resources();
								TCComponent assRes = (TCComponent) reses[i].getComponent();
								res.ackQuorum = Integer.parseInt(assRes.getProperty("ack_quorum"));
								res.revQuorum = Integer.parseInt(assRes.getProperty("rev_quorum"));
								List<String> actionStrs = assRes.getPropertyDisplayableValues("actions");
								int waitforUndecidedUser = Integer.parseInt(assRes.getProperty("wait_for_all_reviewers"));

								AIFComponentContext[] resourceTemp = ((TCComponent) reses[i].getComponent()).getRelated("resources");
								res.templateResources = new TCComponent[resourceTemp.length];
								// res.templateResources = new TCComponent[resourceTemp.length];
								for (int j = 0; j < resourceTemp.length; j++) {
									res.templateResources[j] = (TCComponent) resourceTemp[j].getComponent();
								}

								res.taskTemplate = (TCComponentTaskTemplate) tasks[i].getComponent();
								if (actionStrs.size() == 1) {
									actionStrs = Arrays.asList(actionStrs.get(0).split(","));
								}
								res.actions = new int[actionStrs.size()];
								int k = 0;
								for (String actionStr : actionStrs) {
									res.actions[k++] = Integer.parseInt(actionStr.trim());
								}

								String taskName = tasks[i].getComponent().getProperty("object_name");

								List<TCComponent> copReviewerList = reviewTasksAndCOPReviewers.get(taskName);
								if (copReviewerList != null && copReviewerList.size() > 0) {
									TCComponent[] addingReviewers = copReviewerList.toArray(new TCComponent[0]);
									List<String> userids = new LinkedList<String>();
									List<TCComponent> finalReses = new LinkedList<TCComponent>();
									finalReses.addAll(Arrays.asList(res.templateResources));
									for (TCComponent addingReviewerr : addingReviewers) {
										String userid = addingReviewerr.getProperty("user_name");
										if (userids.contains(userid)) {

										} else {
											finalReses.add(addingReviewerr);
											userids.add(userid);
										}
									}

									// finalReses.addAll(Arrays.asList(addingReviewers));

									int[] totalActions = new int[finalReses.size()];
									for (int j = 0; j < res.templateResources.length; j++) {
										totalActions[j] = res.actions[j];
									}
									for (int j = res.templateResources.length; j < finalReses.size(); j++) {
										totalActions[j] = res.actions[0];
									}

									removedPlaceKeeper(finalReses, "infodba");

									res.templateResources = finalReses.toArray(new TCComponent[0]);
									res.actions = totalActions;
									System.out.println("find group member & add to res.profiles");
								}

								resourcesTemp[i] = res;

								for (TCComponent bb : res.templateResources) {
									System.out.println(bb.toString());
								}
								TCComponentProfile[] dummyProfiles = new TCComponentProfile[res.templateResources.length];
								Integer[] actions = new Integer[res.actions.length];
								for (int j = 0; j < res.actions.length; j++) {
									actions[j] = new Integer(res.actions[j]);
								}

								ResourceMember resourceMember = new ResourceMember((TCComponentTaskTemplate) tasks[i].getComponent(), res.templateResources, dummyProfiles, actions, res.revQuorum, res.ackQuorum, waitforUndecidedUser);
								resmems[i] = resourceMember;
							}

						}

						// Registry registry = Registry.getRegistry(AssignAllTasksOperation.class);
						// registry.setString("failToAssign", "test");
						AssignAllTasksOperation assignAllOperation = new AssignAllTasksOperation(session, AIFDesktop.getActiveDesktop(), (TCComponentProcess) createdProcess, resmems);
						assignAllOperation.executeOperation();

						// session.queueOperation(assignAllOperation);

					}
				}

				if (postActionWhenStartedSuccessfully != null) {
					postActionWhenStartedSuccessfully.execute(createdProcess);
				}

				isSuccess[0] = true;
				logger.error("(NOT ERROR): Successfully submitted to workflow.");
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error(ex);
			}
		}

		if (output.sizeOfPartialErrors() > 0) {
			ErrorStack error = output.getPartialError(0);
			ErrorValue[] errorValues = error.getErrorValues();
			for (ErrorValue value : errorValues) {
				String message = value.getMessage();
				validators.add(message);
			}

			if (isCOPPartProcess) {
				tabPanel.setSelectedIndex(0);
				if (createdProcess != null)
					try {
						createdProcess.delete();
					} catch (TCException e) {
						e.printStackTrace();
						logger.error(e);
					}
			}
		}

		return Status.OK_STATUS;
	}

	private void removedPlaceKeeper(List<TCComponent> finalReses, String placeKeeperUserName) throws NotLoadedException {
		if (finalReses.size() > 1) {
			String assignedUserName = finalReses.get(0).getPropertyDisplayableValue("user_name");
			if (assignedUserName.equals(placeKeeperUserName)) {
				finalReses.remove(0);
			}
		}
	}

	private void handleModuleSelection(boolean[] checkAllPassed) {
		if (icb_module.getSelectedItem() != null) {
			if (checkAllPassed[0] || StartSourcing.IS_BYPASS_SOURCING_VALIDATION) {
				ib_Start.setEnabled(true);
			}

			String selectedProgram = icb_model.getSelectedDisplayValue();
			if (isCOPPartProcess) {
				icb_programCOP.setListData(new Object[0]);
				cleanCOPViewer();

				List<Object> copPrograms = new LinkedList<Object>(Arrays.asList(icb_model.getSelectedDisplayValue()));
				copPrograms.remove(selectedProgram);
				icb_programCOP.setListData(copPrograms.toArray(new Object[0]));
				// icb_programCOP.setItems(copPrograms.toArray());
				icb_programCOP.setEnabled(true);
				icb_programCOP.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						StringBuffer copDescription = new StringBuffer();
						String workflowName = icb_workflow.getSelectedItem() != null ? icb_workflow.getSelectedItem().toString() : "";
						String selectedModule = icb_module.getSelectedItem() != null ? ((VFAssignmentList) icb_module.getSelectedItem()).getName() : "";
						reviewTasksAndCOPReviewers.clear();
						if (workflowName.length() > 0) {
							String[] copAssignmentPrefs = session.getPreferenceService().getStringValues("VINF_COP_ASSIGNMENT");

							for (Object selectedCOPProgramObj : icb_programCOP.getSelectedValuesList()) {
								String selectedCOPProgram = selectedCOPProgramObj.toString();
								String prefix = workflowName + "|" + selectedCOPProgram + "|" + selectedModule;
								String taskUserPairs = "";
								boolean copUserExists = false;
								for (String copAssignmentPref : copAssignmentPrefs) {
									if (copAssignmentPref.startsWith(prefix)) {
										taskUserPairs = copAssignmentPref.split("=")[1];
										copUserExists = true;
									}
								}
								copDescription.append("<p>");
								copDescription.append("<b>").append(selectedCOPProgramObj).append("</b>:<br/>");
								if (copUserExists) {
									String[] tasksAndUsers = taskUserPairs.split("\\|");
									copDescription.append("<ul>");
									for (String taskAndUser : tasksAndUsers) {
										String taskName = taskAndUser.split(":")[0];
										String userIds = taskAndUser.split(":")[1];

										copDescription.append("<li>");
										copDescription.append("<i>");
										copDescription.append(taskName);
										copDescription.append("</i>");
										copDescription.append(": ");
										copDescription.append(userIds);
										copDescription.append("</li>");
										LinkedHashMap<String, String> input = new LinkedHashMap<String, String>();
										input.put("Id", userIds);
										TCComponent[] reviewers = Query.queryItem(session, input, "Admin - User Memberships");

										if (reviewTasksAndCOPReviewers.get(taskName) == null) {
											reviewTasksAndCOPReviewers.put(taskName, new LinkedList<TCComponent>());
										}

										if (reviewers.length == 0) {
											logger.error("WARN: Cannot find reviewer " + userIds.toString());
										} else {
											List<TCComponent> addedReviewers = reviewTasksAndCOPReviewers.get(taskName);
											addedReviewers.addAll(Arrays.asList(reviewers));
										}
									}
									copDescription.append("</ul>");
								} else {
									copDescription.append("<i>");
									copDescription.append("&nbsp;&nbsp;&nbsp;&nbsp;There are no COP users set up.");
									copDescription.append("</i>");
								}
								copDescription.append("</p>");
							}
						}
						// jEditorPaneCOP.setText("");
//						if (copDescription.toString().trim().isEmpty()) {
//							copDescription.append("<body>");
//							copDescription.append("<p>");
//							copDescription.append("There are no ");
//							copDescription.append("</p>");
//							copDescription.append("</body>");
//						}

						jEditorPaneCOP.setText(copDescription.toString());
					}
				});
			}
		} else {
			ib_Start.setEnabled(false);
			icb_programCOP.setEnabled(false);
		}
	}

	private void handlerStartButton(final TCComponent[] targets) throws Exception {
		String selectedModel = icb_model.getSelectedDisplayValue();
		String selectedAssignmentList = icb_module.getItemCount() > 0 ? ((VFAssignmentList) icb_module.getSelectedItem()).getName() : "";
		String selectedWorkflow = icb_workflow.getSelectedItem().toString();

		prepareBonusAssignment(selectedModel);

		boolean isSuccess = submitToWorkflow(targets, selectedWorkflow, selectedAssignmentList, selectedModel);

		if (isSuccess == true) {
			setAlwaysOnTop(false);
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					dispose();
				}
			});
		}
	}

	private void prepareBonusAssignment(String selectedModel) throws Exception {
		// get model
		// get preference
		String VINF_BONUS_ASSIGNMENT_BY_SINGLE_ATTRIBUTE = "VINF_BONUS_ASSIGNMENT_BY_SINGLE_ATTRIBUTE";
		String[] preferences = session.getPreferenceService().getStringValues(VINF_BONUS_ASSIGNMENT_BY_SINGLE_ATTRIBUTE);

		// program|attribute_name|attribute_value|taskname|user1;user2
		reviewTasksAndCOPReviewers.clear();
		if (preferences == null || preferences.length == 0) {
			throw new Exception("Preference " + VINF_BONUS_ASSIGNMENT_BY_SINGLE_ATTRIBUTE + " is not configured or empty!");
		}

		for (String preference : preferences) {
			String[] preferenceWords = preference.split("\\|");
			if (preferenceWords.length != 5) {
				throw new Exception("Wrong prerence \"" + preference + "\" in " + VINF_BONUS_ASSIGNMENT_BY_SINGLE_ATTRIBUTE + ".");
			} else if (preference.startsWith(selectedProgram) && preference.contains("|vf4_es_model_veh_line|") && preference.contains("|" + selectedModel + "|")) {
				String userIds = preferenceWords[4];
				String taskName = preferenceWords[3];
				// parsing preference

				LinkedHashMap<String, String> input = new LinkedHashMap<String, String>();
				input.put("Id", userIds);
				TCComponent[] reviewers = Query.queryItem(session, input, "Admin - User Memberships");

				if (reviewTasksAndCOPReviewers.get(taskName) == null) {
					reviewTasksAndCOPReviewers.put(taskName, new LinkedList<TCComponent>());
				}

				if (reviewers.length == 0) {
					logger.error("WARN: Cannot find reviewer " + userIds.toString());
				} else {
					List<TCComponent> addedReviewers = reviewTasksAndCOPReviewers.get(taskName);
					addedReviewers.addAll(Arrays.asList(reviewers));
				}
			}
		}
	}

	public TCComponent getCreatedProcess() {
		return createdProcess;
	}

	public String getProgram() {
		String group = session.getCurrentGroup().toString();
		String program = "";
		String[] programs = session.getPreferenceService().getStringValues(VFSourcingProcessDialog.VF_PROGRAM_NAMES);
		for (String pgrm : programs) {

			if (group.contains(pgrm)) {
				program = pgrm;
				break;
			}
		}
		return program;
	}
}
