package com.vf.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.rac.util.scrollpage.ScrollPagePane;
import com.teamcenter.rac.workflow.commands.assignmentlist.AssignAllTasksOperation;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.AssignmentLists;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.Resources;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.handlers.StartSourcing;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.Query;
import com.vf.utils.VFAssignmentList;

public class VFProcessDialogScooter extends VFAbstractProcessDialog {

	public enum WorkflowListMode {
		GET_FROM_REFRERENCE_VF_Program_Workflow, GET_WORKFLOW_LIST_FILTERED_BY_GROUP;
	}

	public final static String VF_PROGRAM_NAMES = "VF_Process_Scooter_Program_Names";
	public final static String VF_PROGRAM_ASSIGNMENT = "VF_Process_Scooter_Program_Assignment";
	private static final Logger logger = Logger.getLogger(VFProcessDialogScooter.class);

	private static final long serialVersionUID = 1L;

	private iComboBox cbWorkflow = null;
	private iComboBox cbModule = null;
	private iComboBox cbProgram = null;
	private JList cbProgramCOP = null;
	private JTabbedPane tabPanel;
	private iButton btnStart;
	private iButton btnAddECR;
	private String validationResultText = "";
	private JEditorPane jEditorPane;
	private JEditorPane jEditorPaneCOP;
	private final WorkflowService wfService;
	private final TCSession session;
	private final boolean isCOPPartProcess;
	private final Map<String, List<TCComponent>> reviewTasksAndCOPReviewers;
	private TCComponent createdProcess = null;
	private PostAction postActionWhenStartedSuccessfully;
	private List<TCComponent> listSelectedObjects;
	private List<TCComponent> validationTargets;
	private iComboBox cbCombineWith = null;
	private JLabel lblCombineWith = null;
	private TCTable tblSelectedObj = null;
	private TCComponent rootTarget = null;
	private iComboBox cbCostImpact;

	public TCComponent getRootTarget() {
		return rootTarget;
	}

	public void setRootTarget(TCComponent rootTarget) {
		this.rootTarget = rootTarget;
	}

	private static boolean isOpen = false;

	public VFProcessDialogScooter(TCSession session, boolean isCOPPartProcess, PostAction postActionWhenStartedSuccessfully) {
		super();
		this.session = session;
		wfService = WorkflowService.getService(session);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.isCOPPartProcess = isCOPPartProcess;
		this.reviewTasksAndCOPReviewers = new HashMap<String, List<TCComponent>>();
		this.postActionWhenStartedSuccessfully = postActionWhenStartedSuccessfully;
	}

	public void createAndShowGUI(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final TCComponent[] selectedObjects, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final String dialogTitle) throws Exception {
		if (isOpen == true)
			return;

		this.listSelectedObjects = Arrays.asList(selectedObjects);

		createdProcess = null;
		isOpen = true;
		this.setResizable(false);
		this.setAlwaysOnTop(true);

		final boolean[] checkAllPassed = new boolean[] { true };
		checkValidationResults(partsAndValidationResults, new boolean[] { true });

		String[] prgm_values = input.keySet().toArray(new String[0]);
		prgm_values = Utils.getPreferenceValues(session, VF_PROGRAM_NAMES);
		VFCbxItem[] programCbxItems = makeProgramCbxItems(prgm_values);

		setTitle(dialogTitle);

		Icon start_Icon = new ImageIcon(getClass().getResource("/icons/server_busystate_16.png"));

		JPanel sourcingPanel = new JPanel();
		sourcingPanel.setLayout(null);
		sourcingPanel.setBackground(Color.white);
		sourcingPanel.setPreferredSize(new Dimension(647, 720));

		JLabel jl_program = new JLabel("Program:");
		jl_program.setBounds(15, 20, 95, 25);
		sourcingPanel.add(jl_program);

		cbProgram = new iComboBox(programCbxItems);
		cbProgram.setBounds(110, 20, 200, 25);
		cbProgram.setMandatory(true);
		sourcingPanel.add(cbProgram);
		cbProgram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleProgramSelection(input, checkAllPassed);
			}
		});

		JLabel jl_workflow = new JLabel("Workflow:");
		jl_workflow.setBounds(15, 50, 100, 25);
		sourcingPanel.add(jl_workflow);

		cbWorkflow = new iComboBox();
		cbWorkflow.setBounds(110, 50, 370, 25);
		cbWorkflow.setMandatory(true);
		sourcingPanel.add(cbWorkflow);
		cbWorkflow.addActionListener(new ActionListener() {
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
		});

		btnStart = new iButton("Start");
		btnStart.setIcon(start_Icon);
		btnStart.setBounds(490, 50, 90, 25);
		sourcingPanel.add(btnStart);
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handlerStartButton(selectedObjects, e);
				} catch (NotLoadedException e1) {
					e1.printStackTrace();
				}
			}
		});

		JLabel jl_module = new JLabel("Module:");
		jl_module.setBounds(15, 80, 100, 25);
		sourcingPanel.add(jl_module);

		cbModule = new iComboBox();
		cbModule.setBounds(110, 80, 200, 25);
		cbModule.setMandatory(true);
		sourcingPanel.add(cbModule);

		lblCombineWith = new JLabel("Combine with:");
		lblCombineWith.setBounds(295, 80, 100, 25);
		sourcingPanel.add(lblCombineWith);
		lblCombineWith.setVisible(false);

		cbCombineWith = new iComboBox();
		cbCombineWith.setBounds(391, 80, 169, 25);
		sourcingPanel.add(cbCombineWith);
		cbCombineWith.setItems(initCombineWithCbx());
		cbCombineWith.setVisible(false);
		cbCombineWith.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handlecbCombineWithSelection();
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
		});

		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Validation Report"));
		scrollPane.setBounds(15, 170, 622, 544);
		HTMLEditorKit kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);
		Document doc = kit.createDefaultDocument();
		jEditorPane.setDocument(doc);
		if (validationResultText.length() == 0 && partsAndValidationResults.size() == 0) {
			validationResultText = "No validation required.";
		}
		jEditorPane.setText(validationResultText);

		if (isCOPPartProcess) {
			cbProgramCOP = new JList();
			cbProgramCOP.setEnabled(false);

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
			copPane.add(cbProgramCOP);
			copPane.add(scrollPaneCOP);

			JButton btnNewButton = new JButton("");
			Icon cleanAllImg = new ImageIcon(getClass().getResource("/icons/delete_16.png"));
			btnNewButton.setIcon(cleanAllImg);
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cbProgramCOP.clearSelection();
					cleanCOPViewer();
				}
			});
			btnNewButton.setBounds(9, 7, 16, 16);
			copPane.add(btnNewButton);

			cbProgramCOP.setBorder(new TitledBorder(new LineBorder(new Color(192, 192, 192), 2), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(192, 192, 192)));
			JScrollPane scrollList = new JScrollPane(cbProgramCOP);
			scrollList.setBounds(10, 23, 187, 491);
			copPane.add(scrollList);

			// pp.setBounds(0,0,0,0);

			JLabel jl_ecr = new JLabel("ECN:");
			jl_ecr.setBounds(15, 110, 100, 25);
			sourcingPanel.add(jl_ecr);

			tblSelectedObj = new TCTable();
			String[] tableAttr = new String[] { "object_string" };
			tblSelectedObj.setColumnNames(session, tableAttr, "ItemRevision");
			tblSelectedObj.getColumnModel().getColumn(0).setPreferredWidth(470);
			tblSelectedObj.setRowHeight(27);
			tblSelectedObj.setTableHeader(null);
			tblSelectedObj.setRowSelectionAllowed(true);
			tblSelectedObj.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			if (rootTarget != null)
				tblSelectedObj.addRow(rootTarget);

			JLabel jl_CostImpact = new JLabel("Big Cost Impact:");
			jl_CostImpact.setBounds(15, 140, 100, 25);
			sourcingPanel.add(jl_CostImpact);

			cbCostImpact = new iComboBox();
			cbCostImpact.setBounds(110, 140, 200, 25);
			cbCostImpact.setMandatory(true);
			sourcingPanel.add(cbCostImpact);
			cbCostImpact.setItems(initCostImpact());
			cbCostImpact.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						handleModuleSelection(checkAllPassed);
					} catch (Exception e1) {
						logger.error(e1);
						setAlwaysOnTop(false);
						MessageBox.post(e1);
					}
				}
			});

//			ckbCostImpact = new JCheckBox("");
//			ckbCostImpact.setBounds(110, 140, 25, 25);
//			sourcingPanel.add(ckbCostImpact);

			ScrollPagePane scrollPaneSelectedObj = new ScrollPagePane(tblSelectedObj);
			scrollPaneSelectedObj.setHorizontalScrollBarPolicy(ScrollPagePane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPaneSelectedObj.setVerticalScrollBarPolicy(ScrollPagePane.VERTICAL_SCROLLBAR_NEVER);
			scrollPaneSelectedObj.setBounds(110, 110, 470, 25);
			sourcingPanel.add(scrollPaneSelectedObj);

			tabPanel = new JTabbedPane(JTabbedPane.TOP);
			tabPanel.setBounds(15, 170, 622, 544);
			tabPanel.add("General", scrollPane);
			// tabPanel.add("COP Prorams", copPane);

			sourcingPanel.add(tabPanel);
		} else {
			sourcingPanel.add(scrollPane);
		}

		cbModule.setEnabled(false);
		cbWorkflow.setEnabled(false);
		if (cbProgramCOP != null) {
			cbProgramCOP.setEnabled(false);
		}

		getContentPane().add(sourcingPanel);
		pack();
		setLocationRelativeTo(null);

		String defaultProgram = getProgram();
		if (cbProgram.getItems().length == 1) {
			cbProgram.setSelectedIndex(0);
			handleProgramSelection(input, checkAllPassed);

			if (cbWorkflow.getItems().length == 1) {
				cbWorkflow.setSelectedIndex(0);
				handleWorkflowSelection(checkAllPassed, input);

				if (cbModule.getItems().length == 1) {
					handleModuleSelection(checkAllPassed);
				}
			}
		} else if (defaultProgram.isEmpty() == false) {
			VFCbxItem selecteCbxItem = makeCbxItem(defaultProgram);
			cbProgram.setSelectedItem(selecteCbxItem);
			handleProgramSelection(input, checkAllPassed);

			if (cbWorkflow.getItems() != null && cbWorkflow.getItems().length == 1) {
				cbWorkflow.setSelectedIndex(0);
				handleWorkflowSelection(checkAllPassed, input);

				if (cbModule.getItems().length == 1) {
					handleModuleSelection(checkAllPassed);
				}
			}
		}

		setVisible(true);
	}

	private List<String> initCombineWithCbx() {
		String selectedProgram = cbProgram != null && cbProgram.getSelectedItem() instanceof VFCbxItem ? ((VFCbxItem) (cbProgram.getSelectedItem())).value : "";
		boolean isEscooter = checkIsEScooter(selectedProgram);
		List<String> combineWithList = Arrays.asList(isEscooter ? new String[] { "I Release", "PR Release" } : new String[] { "P Release", "I Release", "PR Release" });
		return combineWithList;
	}

	private List<String> initCostImpact() {
		List<String> combineWithList = Arrays.asList(new String[] { "Big cost impact", "Non/ Low cost impact" });
		return combineWithList;
	}

	private VFCbxItem makeCbxItem(String programName) {
		String displayValue = programName;
		String value = programName;
		if (programName.contains("CUV"))
			displayValue = programName.replace("CUV", "VF35/VFe35");
		else if (programName.contains("SCP"))
			displayValue = programName.replace("SCP", "VF34");
		else if (programName.contains("D-SUV"))
			displayValue = programName.replace("D-SUV", "VF36/VFe36");
		VFCbxItem item = new VFCbxItem(value, displayValue);
		return item;
	}

	private VFCbxItem[] makeProgramCbxItems(String[] prgm_values) {
		List<VFCbxItem> programCbxItems = new LinkedList<VFProcessDialogScooter.VFCbxItem>();
		for (String programName : prgm_values) {
			VFCbxItem programCbxItem = makeCbxItem(programName);
			if (programName.equals("dba") == false || session.getGroup().toString().contains("dba")) {
				programCbxItems.add(programCbxItem);
			}
		}

		return programCbxItems.toArray(new VFCbxItem[0]);
	}

	private void handlecbCombineWithSelection() throws TCException {
		Object selectedCombineWith = cbCombineWith.getSelectedItem();
		if (selectedCombineWith != null && !selectedCombineWith.toString().isEmpty()) {
			for (TCComponent selectedObject : listSelectedObjects) {
				if (selectedObject.getTypeComponent().getUid().contains("ECRRevision")) {
					TCAccessControlService aclSrv = session.getTCAccessControlService();
					boolean noWriteAccessToECR = !aclSrv.checkPrivilege(selectedObject, "WRITE");
					if (noWriteAccessToECR) {
						cbCombineWith.setSelectedItem("");
						setAlwaysOnTop(false);
						MessageBox.post("Current logined user does not have access on the selected object!", "Info", MessageBox.INFORMATION);
						setAlwaysOnTop(true);
					}

					break;
				}
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

	private boolean submitToWorkflow(TCComponent[] targets, String workflow, final String assignmentList, final String programValue) throws TCException {

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
//			String selectedAssignmentList = cbModule.getItemCount() > 0 ? ((VFAssignmentList) cbModule.getSelectedItem()).getName() : "";
//			data.processAssignmentList = selectedAssignmentList;
			if (reviewTasksAndCOPReviewers.size() == 0) {
				data.processAssignmentList = assignmentList;
			}

			final List<String> validators = new LinkedList<String>();
			final StringBuffer partsErrorMsg = new StringBuffer();
			handleWorkflowTrigger(assignmentList, programValue, isSuccess, bl, data, validators);

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
		if (cbProgram.getSelectedItem() != null) {
			btnStart.setEnabled(false);

			cbCombineWith.setVisible(false);
			lblCombineWith.setVisible(false);
			cbWorkflow.removeAllItems();
			cbModule.removeAllItems();
			if (isCOPPartProcess) {
				cbProgramCOP.setListData(new Object[0]);
				cleanCOPViewer();
			}

			String selectedProgram = "Scooter";
			cbCombineWith.setItems(initCombineWithCbx());
			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
			if (workflowAndInput != null) {
				String[] workflowList = workflowAndInput.keySet().toArray(new String[0]);
				cbWorkflow.setEnabled(true);
				cbWorkflow.setItems(workflowList);

//				if (cbWorkflow.getListeners(ActionListener.class).length == 0) {
//					cbWorkflow.addActionListener(new ActionListener() {
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
//					});
//				}
			}
		} else {
			cbWorkflow.setEnabled(false);
			cbWorkflow.removeAllItems();
			cbModule.setEnabled(false);
			cbModule.removeAllItems();
			btnStart.setEnabled(false);
			btnAddECR.setEnabled(false);
			if (isCOPPartProcess) {
				cbProgramCOP.setEnabled(false);
				cbProgramCOP.setListData(new Object[0]);
			}
		}
	}

	private void cleanCOPViewer() {
		jEditorPaneCOP.setText("<body></body>");
		reviewTasksAndCOPReviewers.clear();
	}

	private void handleWorkflowSelection(final boolean[] checkAllPassed, final Map<String, Map<String, VFProcessDialogProcessInput>> input) throws Exception {
		if (cbWorkflow.getSelectedItem() != null) {
			if (isCOPPartProcess) {
				cbProgramCOP.setListData(new Object[0]);
				cleanCOPViewer();
			}
			cbModule.removeAllItems();

			String selectedProgram = "Scooter";
			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
			String selectedWorkflow = cbWorkflow.getSelectedItem().toString();
			boolean isEScooter = checkIsEScooter(selectedProgram);
//			boolean isEcrCombinedWithRelease = (selectedWorkflow.contains("ECR") && (selectedWorkflow.contains("C-SUV") || selectedWorkflow.contains("VinFast") || isEScooter));
			boolean isEcrCombinedWithRelease = (selectedWorkflow.contains("AfterSOB") || selectedWorkflow.contains("BeforeSOB"));

//			String workflow = cbWorkflow.getSelectedItem() != null ? cbWorkflow.getSelectedItem().toString() : "";
//			if(!workflow.isEmpty()) {
//				if(workflow.compareToIgnoreCase("EScooter-ECR-AfterPR") == 0) {
//					isEcrCombinedWithRelease = false;
//				}
//			}

			cbCombineWith.setVisible(isEcrCombinedWithRelease);
			lblCombineWith.setVisible(isEcrCombinedWithRelease);
			cbCombineWith.setText("");

			VFProcessDialogProcessInput processInput = workflowAndInput.get(selectedWorkflow);
			if (processInput != null) {
				cbModule.setEnabled(true);
				List<VFAssignmentList> assigmentLists = getAssignmentList(processInput);
				cbModule.setItems(assigmentLists);
				String defaultAssignmentList = processInput.getDefaultAssignmentList();
				if (defaultAssignmentList != null && !defaultAssignmentList.isEmpty()) {
					for (VFAssignmentList assList : processInput.getAssignmentList()) {
						if (assList.getName().contains(defaultAssignmentList)) {
							cbModule.setSelectedItem(assList);
							btnStart.setEnabled(checkAllPassed[0]);
							break;
						}
					}
				}

				IValidator validator = workflowAndInput.get(selectedWorkflow).getValidator();
				Map<TCComponent, List<ValidationResult>> partsValRes = new HashMap<TCComponent, List<ValidationResult>>();
				if (validator != null && validationTargets != null) {
					for (TCComponent target : validationTargets) {
						List<ValidationResult> valRes = validator.validate((TCComponentBOMLine) target);
						partsValRes.put(target, valRes);
					}
				}
				checkValidationResults(partsValRes, checkAllPassed);

				if (cbModule.getListeners(ActionListener.class).length == 0) {
					ActionListener moduleSelectionListener = new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							handleModuleSelection(checkAllPassed);
						}
					};
					cbModule.addActionListener(moduleSelectionListener);
				}

				if (cbModule.getItemCount() == 0) {
					Object costImpact = cbCostImpact.getSelectedItem();
					if (costImpact != null) {
						if (!costImpact.toString().isEmpty()) {
							btnStart.setEnabled(true);
						}
					}
				} else if (cbModule.getSelectedIndex() < 0) {
					btnStart.setEnabled(false);
				}
			}

		} else {
			btnStart.setEnabled(false);
			cbModule.setEnabled(false);
			cbModule.removeAllItems();
		}
	}

	private void UpdateStartButtonState() {

	}

	private List<VFAssignmentList> getAssignmentList(VFProcessDialogProcessInput processInput) {

		VFAssignmentList[] assignmentListArrays = processInput.getAssignmentList().toArray(new VFAssignmentList[0]);
		List<VFAssignmentList> assignmentLists = new LinkedList<VFAssignmentList>();
		if (cbProgram.getSelectedIndex() > -1) {
			for (VFAssignmentList assignmentList : assignmentListArrays) {
				assignmentLists.add(assignmentList);
			}
		}

		assignmentLists.sort(new Comparator<VFAssignmentList>() {
			@Override
			public int compare(VFAssignmentList o1, VFAssignmentList o2) {
				String o1Desc = o1.getName();
				String o2Desc = o2.getName();
				return o1Desc.compareTo(o2Desc);
			}
		});

		return assignmentLists;
	}

	private IStatus handleWorkflowTrigger(final String assignmentList, final String programValue, final boolean[] isSuccess, final TCComponent bl, final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data, final List<String> validators) throws TCException {

		String remark = handleStartProcessWithErnChangeCombination();

		ServiceData validationWfResponse = null;
		boolean isEScooter = checkIsEScooter(programValue);
		if (remark.isEmpty() == false && bl.getTypeComponent().getUid().contains("ECR") && !isEScooter) {
			validationWfResponse = triggerValidationWorkflow(programValue, bl, remark, assignmentList);
		}

		String copString = makeCOPString();
		String processDesc = remark.isEmpty() ? "" : "CHANGE APPROVAL COMBINED WITH " + remark.toUpperCase();
		processDesc = copString.isEmpty() ? processDesc : (processDesc.isEmpty() ? copString : processDesc + " - " + copString);
		markTarget(bl, "_");
		if (remark.isEmpty() == true || !bl.getTypeComponent().getUid().contains("ECR") || isEScooter || (validationWfResponse.sizeOfPartialErrors() == 0 && validationWfResponse.sizeOfCreatedObjects() > 0)) {
			InstanceInfo intInfo = wfService.createInstance(true, null, programValue + " - " + bl.toString(), null, processDesc, data);
			ServiceData output = intInfo.serviceData;

			if (output.sizeOfCreatedObjects() > 0) {
				createdProcess = output.getCreatedObject(0);
				handleStartProcessWithErnBigCostImpact();
			}
			if (createdProcess != null) {
				try {
					markTarget(bl, "");

					// reviewTasksAndCOPReviewers
					if (assignmentList != null && assignmentList.isEmpty() == false && reviewTasksAndCOPReviewers.size() > 0) {
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

					boolean gotAssignedIssue = !handlePostAssignment(createdProcess);
					if (gotAssignedIssue) {
						MessageBox.post("There is an issue while assigning task for selected program! Please contact to v.itservicedesk@vinfast.vn for support.", "ERROR", MessageBox.ERROR);
					}

					if (postActionWhenStartedSuccessfully != null) {
						postActionWhenStartedSuccessfully.execute(createdProcess);
					}

					createdProcess.getReferenceProperty("root_task").setProperty("comments", programValue + " - " + assignmentList);
					isSuccess[0] = true;
					logger.error("[VF] (NOT ERROR): process created.");
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.error(ex);
				}
			}

			validationWfResponse = output;
		}

		if (validationWfResponse.sizeOfPartialErrors() > 0) {
			isSuccess[0] = false;

			ErrorStack error = validationWfResponse.getPartialError(0);
			ErrorValue[] errorValues = error.getErrorValues();
			for (ErrorValue value : errorValues) {
				String message = value.getMessage();
				validators.add(message);
			}

			if (createdProcess == null) {
				for (int i = 0; i < validationWfResponse.sizeOfUpdatedObjects(); i++) {
					if (validationWfResponse.getUpdatedObject(i).getType().contains("Job")) {
						validationWfResponse.getUpdatedObject(i).delete();
						break;
					}
				}
			}

			if (isCOPPartProcess) {
				tabPanel.setSelectedIndex(0);
			}
		}

		return Status.OK_STATUS;
	}

	private boolean handlePostAssignment(TCComponent createdProcess2) throws Exception {
		// V5 CITY LFP==taskname=haidv7
		// V5 LITE LFP==tn=tuannd26
		// V7 SPORT LF==tn=tuannd26
		// V9 SPORT LFP==tn=tuannd26

		String[] assignmentBasedOnProgramValues = Utils.getPreferenceValues(session, VF_PROGRAM_ASSIGNMENT);
		String taskName = "";
		String selectedProgram = cbProgram.getSelectedItem() instanceof VFCbxItem ? ((VFCbxItem) (cbProgram.getSelectedItem())).displayVal : "";
		// String selectedProgram = "V5 LITE LFP";
		TCComponentGroupMember picGm = null;
		System.out.println("");
		for (String line : assignmentBasedOnProgramValues) {
			String prg = line.split("==")[0];
			taskName = line.split("=")[2];
			String grpRoleUserId = line.split("=")[3];
			String group = grpRoleUserId.split("/")[0];
			String role = grpRoleUserId.split("/")[1];
			String userId = grpRoleUserId.split("/")[2];

			if (selectedProgram.compareToIgnoreCase(prg) == 0) {
				TCComponent[] objects = null;
				LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
				queryInput.put("Id", userId);
				queryInput.put("Group", group);
				queryInput.put("Role", role);
				queryInput.put("Group Member Status", "FALSE");
				objects = Query.queryItem(session, queryInput, "__TNH_FindGroupMem");
				if (objects == null || objects.length == 0) {
					MessageBox.post("Cannot find Chief Engineer: " + userId, "ERROR", MessageBox.ERROR);
					return false;
				}
				picGm = (TCComponentGroupMember) objects[0];
				break;
			}

		}

		TCComponentTask rootTask = ((TCComponentProcess) createdProcess2).getRootTask();
		TCComponentTask[] subtasks = rootTask.getSubtasks();
		TCComponentUser picUser = picGm.getUser();
		for (int i = 0; i < subtasks.length; i++) {
			String subTaskName = subtasks[i].getName();
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMConditionTask") == 0 && subTaskName.compareToIgnoreCase(taskName) == 0) {
				subtasks[i].setResponsibleParty(picUser);
				break;
			} else if (subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 && subTaskName.compareToIgnoreCase(taskName) == 0) {
				TCComponentTask selectSignOffTask = subtasks[i].getSubtasks()[0];
				if (selectSignOffTask.getTaskType().compareToIgnoreCase("EPMSelectSignoffTask") != 0) {
					selectSignOffTask = subtasks[i].getSubtasks()[1];
				}
				System.out.println("[assignSignoffToProcess] Processing: " + selectSignOffTask.getName());

				CreateSignoffs createSignoff = new CreateSignoffs();
				createSignoff.task = selectSignOffTask;
				createSignoff.signoffInfo = new CreateSignoffInfo[1];
				createSignoff.signoffInfo[0] = new CreateSignoffInfo();
				createSignoff.signoffInfo[0].originType = "SOA_EPM_ORIGIN_UNDEFINED";
				createSignoff.signoffInfo[0].signoffAction = "SOA_EPM_Review";
				createSignoff.signoffInfo[0].signoffMember = picGm;
				ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
				if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
					logger.error("[assignSignoffToProcess]: " + Utils.HanlderServiceData(addSignoffsResponse));
					return false;
				}

				// Set adhoc done
				DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
				propInfo.object = selectSignOffTask;
				propInfo.timestamp = Calendar.getInstance();
				propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
				propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
				propInfo.vecNameVal[0].name = "task_result";
				propInfo.vecNameVal[0].values = new String[] { "Completed" };
				DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
				if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
					logger.error("[assignSignoffToProcess]: " + Utils.HanlderServiceData(setPropertyResponse.data));
					return false;
				}

				// Complete selection signoff task if it started
				if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
					try {
						Thread.sleep(1000);
						createdProcess2.refresh();
						subtasks[i].refresh();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
					paii.clientId = "complete" + selectSignOffTask.getUid();
					paii.action = "SOA_EPM_complete_action";
					paii.actionableObject = selectSignOffTask;
					paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
					paii.supportingValue = "SOA_EPM_completed";

					ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
					if (sd.sizeOfPartialErrors() > 0) {
						logger.error("[assignSignoffToProcess]: " + Utils.HanlderServiceData(sd));
						return false;
					}
				}
			}
		}

		return true;
	}

	private boolean checkIsEScooter(final String programValue) {
		return true;
	}

	private void markTarget(TCComponent bl, String markerValue) throws TCException {
		AIFComponentContext[] forms = bl.getRelated("IMAN_master_form_rev");
		if (forms != null && forms.length > 0) {
			TCComponent form = (TCComponent) forms[0].getComponent();
			form.setProperty("object_desc", markerValue);
		}
	}

	private String makeCOPString() {
		StringBuffer selectedCOPPrograms = new StringBuffer();
		if (cbProgramCOP.getSelectedValuesList().size() > 0)
			selectedCOPPrograms.append("COP PROGRAM(S): ");
		for (Object selectedCOPProgramObj : cbProgramCOP.getSelectedValuesList()) {
			String selectedCOPProgram = (selectedCOPProgramObj instanceof VFCbxItem) ? ((VFCbxItem) selectedCOPProgramObj).displayVal : selectedCOPProgramObj.toString();
			selectedCOPPrograms.append(selectedCOPProgram).append(";");
		}

		if (selectedCOPPrograms.lastIndexOf(";") > -1)
			selectedCOPPrograms.deleteCharAt(selectedCOPPrograms.lastIndexOf(";"));

		return selectedCOPPrograms.toString();
	}

	private ServiceData triggerValidationWorkflow(String programValue, TCComponent bl, String remark, String assignmentList) throws TCException {
		String validationProcess = "";
		if (remark.toLowerCase().equals("p release")) {
			validationProcess = "Vinfast Validation P Release";
		} else if (remark.toLowerCase().equals("i release")) {
			validationProcess = "Vinfast Validation I Release";
		} else if (remark.toLowerCase().equals("pr release")) {
			validationProcess = "Vinfast Validation PR Release";
		}

		ContextData data = new ContextData();
		data.processTemplate = validationProcess;
		data.attachmentCount = 1;
		data.attachments = new String[] { bl.getUid() };
		data.attachmentTypes = new int[] { 1 };
		InstanceInfo info = wfService.createInstance(true, null, programValue + " - " + bl.toString(), null, "VF System Process", data);

		return info.serviceData;
	}

	private String handleStartProcessWithErnChangeCombination() throws TCException {
		String remark = "";
		Object selectedCombineWith = cbCombineWith.getSelectedItem();
		String statusRemark = "vf4_combine_release_status";
		for (TCComponent selectedObject : listSelectedObjects) {
			if (selectedObject.getTypeComponent().getUid().contains("ECRRevision")) {
				remark = (selectedCombineWith != null && !selectedCombineWith.toString().isEmpty()) ? selectedCombineWith.toString() : "";
				selectedObject.refresh();
				selectedObject.setProperty(statusRemark, remark);
				selectedObject.refresh();
				break;
			} else if (selectedObject.getTypeComponent().getUid().contains("ECNRevision")) {
				System.out.println("[VF] ECNRevision ...");
				AIFComponentContext[] ecrs = selectedObject.getRelated("CMImplements");
				if (ecrs != null && ecrs.length > 0) {
					TCComponent ecr = ((TCComponent) ecrs[0].getComponent());
					ecr.refresh();
					remark = ecr.getProperty(statusRemark);
					System.out.println("[VF] " + remark);
					break;
				}
			} else if (selectedObject.getTypeComponent().getUid().contains("ChangeReq_ESRevision")) {
				remark = (selectedCombineWith != null && !selectedCombineWith.toString().isEmpty()) ? selectedCombineWith.toString() : "";
				selectedObject.refresh();
				selectedObject.setProperty(statusRemark, remark);
				selectedObject.refresh();
				break;
			}
		}

		return remark;
	}

	private void handleStartProcessWithErnBigCostImpact() throws TCException {
		Object costImpact = cbCostImpact.getSelectedItem();
		if (costImpact != null) {
			Boolean check = false;
			String statusRemark = "vf4_is_big_cost_impact";
			if (costImpact.toString().compareToIgnoreCase("Big cost impact") == 0) {
				check = true;
			}
			for (TCComponent selectedObject : listSelectedObjects) {
				if (selectedObject.getTypeComponent().getUid().contains("ChangeReq_ESRevision")) {
					selectedObject.refresh();
					selectedObject.setLogicalProperty(statusRemark, check);
					selectedObject.refresh();
					break;
				}
			}
		}
	}

	private void handleModuleSelection(boolean[] checkAllPassed) {
		if (cbModule.getSelectedItem() != null) {
			if (checkAllPassed[0] || StartSourcing.IS_BYPASS_SOURCING_VALIDATION) {
				Object costImpact = cbCostImpact.getSelectedItem();
				if (costImpact != null) {
					if (!costImpact.toString().isEmpty()) {
						btnStart.setEnabled(true);
					}
				}
			}

			String selectedProgram = cbProgram.getSelectedItem() instanceof VFCbxItem ? ((VFCbxItem) (cbProgram.getSelectedItem())).displayVal : "";
			if (isCOPPartProcess) {
				cbProgramCOP.setListData(new Object[0]);
				cleanCOPViewer();

				List<Object> copPrograms = new LinkedList<Object>(Arrays.asList(cbProgram.getItems()));
				copPrograms.remove(cbProgram.getSelectedItem());
				cbProgramCOP.setListData(copPrograms.toArray(new Object[0]));

				cbProgramCOP.setEnabled(true);
				cbProgramCOP.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						StringBuffer copDescription = new StringBuffer();
						String workflowName = cbWorkflow.getSelectedItem() != null ? cbWorkflow.getSelectedItem().toString() : "";
						String selectedModule = cbModule.getSelectedItem() != null ? ((VFAssignmentList) cbModule.getSelectedItem()).getName() : "";
						reviewTasksAndCOPReviewers.clear();
						if (workflowName.length() > 0) {
							String[] copAssignmentPrefs = session.getPreferenceService().getStringValues("VINF_COP_ASSIGNMENT");

							for (Object selectedCOPProgramObj : cbProgramCOP.getSelectedValuesList()) {
								String selectedCOPProgram = (selectedCOPProgramObj instanceof VFCbxItem) ? ((VFCbxItem) selectedCOPProgramObj).displayVal : selectedCOPProgramObj.toString();
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

						jEditorPaneCOP.setText(copDescription.toString());
					}
				});
			}
		} else {
			btnStart.setEnabled(false);
			cbProgramCOP.setEnabled(false);
		}
	}

	private void handlerStartButton(final TCComponent[] selectedBOMLines, ActionEvent evt) throws NotLoadedException {
		boolean isValidEcrFor36 = checkValidEcrFor36(selectedBOMLines);
		if (!isValidEcrFor36) {
			setAlwaysOnTop(false);
			MessageBox.post("CANNOT trigger process for VF36/VFe36 as ECR Model Group is different from selected program!", "Warning", MessageBox.WARNING);
			setAlwaysOnTop(true);
		} else {
			final WaitDialog wait = new WaitDialog();
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					try {
						boolean isSuccess = false;
						String selectedProgram = cbProgram.getSelectedItem() instanceof VFCbxItem ? ((VFCbxItem) (cbProgram.getSelectedItem())).displayVal : "";
						String selectedAssignmentList = cbModule.getItemCount() > 0 ? ((VFAssignmentList) cbModule.getSelectedItem()).getName() : "";
						String selectedWorkflow = cbWorkflow.getSelectedItem().toString();

						isSuccess = submitToWorkflow(selectedBOMLines, selectedWorkflow, selectedAssignmentList, selectedProgram);

						if (isSuccess == true) {
							setAlwaysOnTop(false);
							MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									wait.close();
									dispose();
								}
							});
						}

						wait.close();
					} catch (Exception e1) {
						logger.error(e1);
						wait.close();
						setAlwaysOnTop(false);
						MessageBox.post(e1);
					}

					return null;
				}
			};

			setAlwaysOnTop(false);
			mySwingWorker.execute();
			wait.makeWait("Processing ...", evt);
		}
	}

	public void setValidationTargets(TCComponent[] validationTargets) {
		this.validationTargets = Arrays.asList(validationTargets);
	}

	private boolean checkValidEcrFor36(TCComponent[] selectedBOMLines) throws NotLoadedException {
		return true;
	}

	public TCComponent getCreatedProcess() {
		return createdProcess;
	}

	public String getProgram() {
		String group = session.getCurrentGroup().toString();
		String program = "";
		String[] programs = session.getPreferenceService().getStringValues(VFProcessDialogScooter.VF_PROGRAM_NAMES);
		for (String pgrm : programs) {

			if (group.contains(pgrm)) {
				program = pgrm;
				break;
			}
		}
		return program;
	}

	class VFCbxItem {
		public String value;
		public String displayVal;

		public VFCbxItem() {

		}

		public VFCbxItem(String value, String displayVal) {
			this.value = value;
			this.displayVal = displayVal;
		}

		public String toString() {
			return displayVal;
		}
	}
}