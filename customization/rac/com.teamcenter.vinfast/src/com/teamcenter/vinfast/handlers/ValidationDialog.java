package com.teamcenter.vinfast.handlers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.AssignmentLists;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesInputInfo;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesOutput;
import com.teamcenter.services.rac.workflow._2013_05.Workflow.GetWorkflowTemplatesResponse;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.ValidationResult;

public class ValidationDialog extends AbstractAIFDialog {

	private static final String DEFAULT_PROGRAM = "EBUS";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TCSession sessionDialog = null;
	private HashMap<String, TCComponentBOMLine> selectedLines = null;
	private String workflow = "";
	private String[] assignmentListValues = null;
	private iComboBox icb_module = null;
	private iComboBox icb_program = null;
	private String validationResultText = "";
	private Utils utility;
	private JEditorPane jEditorPane;
	private final Logger logger;

	private static boolean isOpen = false;

	public ValidationDialog() {
		super();
		logger = Logger.getLogger(this.getClass());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void createAndShowGUI(TCSession session, HashMap<String, TCComponentBOMLine> selectedBomline,
			Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final String programValue) throws Exception {
		if (isOpen == true) {
			return;
		}

		isOpen = true;
		this.setResizable(false);
		//this.setAlwaysOnTop(true);
		sessionDialog = session;
		selectedLines = selectedBomline;
		utility = new Utils();
		String[] workflows = Utils.getPreferenceValues(session, "VF_Program_Workflow");
		String[] prgm_values = Utils.getPreferenceValues(session, "VF_Program_Names");
		assignmentListValues = Utils.getPreferenceValues(session, "VF_Module_AssignmentList");
		
		if (workflows == null || prgm_values == null || assignmentListValues == null) {
			logger.error("VF_Program_Workflow or VF_Program_Names or VF_Module_AssignmentList preferences are not setup!");
			return;
		}

		setTitle("Sourcing Dialog");

		JPanel sourcingPanel = new JPanel();
		sourcingPanel.setLayout(null);
		sourcingPanel.setBackground(Color.white);

		sourcingPanel.setPreferredSize(new Dimension(660, 650));

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/sourcing_16.png"));
		Icon start_Icon = new ImageIcon(getClass().getResource("/icons/server_busystate_16.png"));

		JLabel jl_program = new JLabel("Program:");
		jl_program.setBounds(15, 20, 95, 25);
		sourcingPanel.add(jl_program);

		// Values from preference

		icb_program = new iComboBox(prgm_values);
		icb_program.setBounds(80, 20, 200, 25);
		if (!programValue.equals("")) {
			icb_program.setSelectedItem(programValue);
		} else {
			icb_program.setSelectedItem(DEFAULT_PROGRAM);
		}
		icb_program.setMandatory(true);
		icb_program.setEnabled(false);
		sourcingPanel.add(icb_program);

		JLabel jl_module = new JLabel("Module:");
		jl_module.setBounds(300, 20, 100, 25);
		sourcingPanel.add(jl_module);

		icb_module = new iComboBox();
		icb_module.setBounds(360, 20, 200, 25);

		icb_module.setMandatory(true);
		icb_module.setEnabled(false);
		sourcingPanel.add(icb_module);

		JLabel jl_workflow = new JLabel("Workflow:");
		jl_workflow.setBounds(20, 55, 100, 25);
		sourcingPanel.add(jl_workflow);

		iTextField itf_workflow = new iTextField();
		itf_workflow.setBounds(90, 55, 370, 25);
		workflow = getWorkflow(workflows, icb_program.getSelectedItem().toString());
		itf_workflow.setRequired(true);
		itf_workflow.setEditable(false);
		itf_workflow.setText(workflow);
		sourcingPanel.add(itf_workflow);

		iButton ib_Start = new iButton("Start");
		ib_Start.setIcon(start_Icon);
		ib_Start.setBounds(470, 55, 90, 25);
		sourcingPanel.add(ib_Start);

		boolean checkAllPassed = true;

		for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : partsAndValidationResults
				.entrySet()) {
			List<ValidationResult> validationResults = partsAndValidationResult.getValue();
			validationResultText += ValidationResult.createHtmlText(partsAndValidationResult) + "</br></br>";
			boolean checkPassed = ValidationResult.checkValidationResults(validationResults);
			if (checkPassed == false) {
				checkAllPassed = false;
			}
		}

		if (checkAllPassed == true) {
			TCComponentBOMLine bomLine = selectedBomline.get(selectedBomline.keySet().toArray()[0]);
			String[] lovValues = utility.getLovValues(session, "VL5_module_group", bomLine);
			String lovValue = bomLine.getProperty("VL5_module_group");
			icb_module.setEnabled(true);
			icb_module.addItems(lovValues);
			icb_module.setSelectedItem(lovValue);
			//validationResultText = "Validation Result:- SUCCESS";
			//validationResultText = validationResultText + "\n" + "Click on \"START\" to Start sourcing.";
		} else {

			ib_Start.setEnabled(false);
		}

		ib_Start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String assignment_List = "";
				String moduleSeleted = icb_program.getSelectedItem().toString() + "_"
						+ icb_module.getSelectedItem().toString();
				if (!moduleSeleted.equals("")) {
					assignment_List = getAssignment(assignmentListValues, moduleSeleted);
				}
				boolean isSuccess = submitToWorkflow(sessionDialog, selectedLines, workflow, assignment_List, programValue);
				if (isSuccess == true) {
					MessageBox.post("Sourcing initiated.", "Success", MessageBox.INFORMATION);
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							dispose();
						}
					});
				}
			}

		});

		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Validation Report"));
		scrollPane.setBounds(20, 90, 620, 550);

		HTMLEditorKit kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);

		// add some styles to the html
//	        StyleSheet styleSheet = kit.getStyleSheet();
//	        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");

		Document doc = kit.createDefaultDocument();
		jEditorPane.setDocument(doc);
		jEditorPane.setText(validationResultText);

		sourcingPanel.add(scrollPane);

		getContentPane().add(sourcingPanel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private String getWorkflow(String[] programsWF, String program) {

		String workflow = "";

		for (String pgrmWF : programsWF) {

			String[] pgrm = pgrmWF.split("=");
			if (program.equals(pgrm[0])) {
				workflow = pgrm[1];
			}
		}
		return workflow;
	}

	private String getAssignment(String[] assgn_Lists, String module) {

		String assignment_ListValue = "";

		for (String assignment : assgn_Lists) {

			String[] assignmentModule = assignment.split("=");
			if (module.equals(assignmentModule[0])) {
				assignment_ListValue = assignmentModule[1];
			}
		}
		return assignment_ListValue;
	}

	private boolean submitToWorkflow(TCSession session, HashMap<String, TCComponentBOMLine> selectedBomline,
			String workflow, String assignmentList, final String programValue) {

		final boolean[] isSuccess = {false};
		String group = session.getCurrentGroup().toString();
		final WorkflowService wfService = WorkflowService.getService(session);

		String submitToWorkflow = "";
		int count = 0;
		int lineCount = selectedBomline.size();
		try {
			String[] keys = new String[lineCount];
			TCComponent[] values = new TCComponent[lineCount];

			for (Map.Entry<String, TCComponentBOMLine> entry : selectedBomline.entrySet()) {
				keys[count] = entry.getKey();
				values[count] = entry.getValue();
				count++;
			}

			GetWorkflowTemplatesInputInfo templateInfo = new GetWorkflowTemplatesInputInfo();
			templateInfo.targetObjects = values;
			templateInfo.group = group;

			GetWorkflowTemplatesResponse response = wfService
					.getWorkflowTemplates(new GetWorkflowTemplatesInputInfo[] { templateInfo });
			GetWorkflowTemplatesOutput[] template = response.templatesOutput;

			TCComponentTaskTemplate[] allTempletes = template[0].workflowTemplates;

			for (TCComponentTaskTemplate templateWF : allTempletes) {

				String name = templateWF.getName();

				if (workflow.equals(name)) {
					submitToWorkflow = templateWF.getName();
					break;
				}

			}

			if (!submitToWorkflow.equals("")) {

				final AssignmentLists lists = wfService.getAssignmentLists(new String[] { assignmentList });
				TCComponentAssignmentList[] list = lists.assignedLists;
				int[] attachmentTypes = new int[keys.length];
				Arrays.fill(attachmentTypes, 1);
				final TCComponentBOMLine bl = (TCComponentBOMLine) values[0];
				final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data = new com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData();
				data.attachmentCount = lineCount;
				data.attachments = keys;
				data.processTemplate = submitToWorkflow;
				data.attachmentTypes = attachmentTypes;
				data.processAssignmentList = list[0].getName();
				
				
				final List<String> validators = new LinkedList<String>();
				final StringBuffer partsErrorMsg = new StringBuffer();
				session.queueOperationAndWait(new Job("Trigering workflow") {
					
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						InstanceInfo intInfo = wfService.createInstance(true, null, programValue + " - " + bl.toString(), null, programValue + " - " + bl.toString(), data);
						ServiceData output = intInfo.serviceData;
						
						if (output.sizeOfPartialErrors() == 0) {
							isSuccess[0] = true;
							logger.error("(NOT ERROR): Successfully submitted to workflow.");
						} else {
							ErrorStack error = output.getPartialError(0);
							ErrorValue[] errorValues = error.getErrorValues();
							for (ErrorValue value : errorValues) {
								String message = value.getMessage();
								validators.add(message);
							}
						}
						return Status.OK_STATUS;
					}
				} );
				
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
		} catch (TCException | ServiceException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return isSuccess[0];
	}

	@Override
	public void dispose() {
		isOpen = false;
		super.dispose();
	}

}
