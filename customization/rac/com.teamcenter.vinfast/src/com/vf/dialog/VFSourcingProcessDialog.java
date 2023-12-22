package com.vf.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.rac.util.scrollpage.ScrollPagePane;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.TCExtension;
import com.vf.utils.VFAssignmentList;
import com.vf.utils.VFRacDefine.VFProgram;
import com.vinfast.sc.utilities.PropertyDefines;

import vfplm.soa.common.define.Constant;

public class VFSourcingProcessDialog extends VFAbstractProcessDialog {

	public enum WorkflowListMode {
		GET_FROM_REFRERENCE_VF_Program_Workflow, GET_WORKFLOW_LIST_FILTERED_BY_GROUP;
	}

	private static final Logger logger = Logger.getLogger(VFSourcingProcessDialog.class);
	public final static String VF_PROGRAM_NAMES = "VF_Program_Names";
	public final static String VF_SOURCING_PRG_2_USER_GROUP = "VF_SOURCING_PROGRAM_2_USER_GROUP";
	public final static String VF_SOURCING_PRG_2_WF = "VF_SOURCING_PROGRAM_2_WF";
	private final static String MGL = "VF_MODULE_GROUP_LEADER";
	private final static String CL = "VF_COMMUNITY_LEADER";
	private final static String COST_ENG = "VF_COST_ENGINEER";
	private final static String BUYER = "VF_BUYER";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private iComboBox icb_workflow = null;
	private iComboBox icb_module = null;
	private iComboBox icb_mgl = null;
	private iComboBox icb_cl = null;
	private iComboBox icb_chiefEng = null;
	private iComboBox icb_program = null;
	private iComboBox icb_costEng = null;
	private iButton ib_Start;
	private iButton ib_addSOR;
	private String validationResultText = "";
	private JEditorPane jEditorPane;
	private final TCSession session;
	private PostAction postActionWhenStartedSuccessfully;
	private List<TCComponent> listSelectedItem;
	private TCTable tblSelectedObj = null;
	private TCComponent selectedSpecDoc = null;
	private static boolean isOpen = false;
	private LinkedHashMap<String, TCComponentUser> userModuleLeader;
	private LinkedHashMap<String, TCComponentUser> userCL;
	private LinkedHashMap<String, TCComponentUser> userCostEng;
	private LinkedHashMap<String, TCComponentUser> userBuyer;
	private LinkedHashMap<String, TCComponentGroupMember> gmModuleLeader;
	private LinkedHashMap<String, TCComponentGroupMember> gmCL;
	private LinkedHashMap<String, TCComponentGroupMember> gmCostEng;
	private LinkedHashMap<String, TCComponentGroupMember> gmBuyer;
	private LinkedHashMap<String, String> lovVF4SourcingCarPrg = new LinkedHashMap<>();
	private static boolean isValidProgram = false;
	private static boolean isValidWf = true;
	private static boolean isValidModule = false;
	private static boolean isValidMGL = false;
	private static boolean isValidCL = false;
	private static boolean isValidChiefEng = false;
	private static boolean isValidCostEng = false;
	private static String chiefEngineer = "";
	private static String rndDirector = "";
	private static String rndHead = "";
	private static boolean has_P_PartInTarget = false;
	private static String buyerOSName;

	public TCComponentGroupMember getMGCostEngGroupMember(String userID) {
		return gmCostEng.get(userID);
	}

	public TCComponentGroupMember getMGLGroupMember(String userID) {
		return gmModuleLeader.get(userID);
	}

	public TCComponentGroupMember getCLGroupMember(String userID) {
		return gmCL.get(userID);
	}

	public String getSelectedProgram() {
		return this.icb_program.getSelectedItem().toString();
	}

	public String getSelectedModule() {
		return this.icb_module.getSelectedItem().toString();
	}

	public String getSelectedWorkflow() {
		return this.icb_workflow.getSelectedItem().toString();
	}

	public void setModule(String module) {
		for (int i = 0; i < this.icb_module.getItems().length; i++) {
			if (this.icb_module.getItemAt(i).toString().compareToIgnoreCase(module) == 0) {
				this.icb_module.setSelectedIndex(i);
			}
		}
	}

	public String getSelectedCostEng() {
		return this.icb_costEng.getSelectedItem().toString();
	}

	public String getSelectedCL() {
		return this.icb_cl.getSelectedItem().toString();
	}

	public String getSelectedChiefEng() {
		return this.icb_chiefEng.getSelectedItem().toString();
	}

	public void setCL(String cl) {
		for (int i = 0; i < this.icb_cl.getItems().length; i++) {
			if (this.icb_cl.getItemAt(i).toString().compareToIgnoreCase(cl) == 0) {
				this.icb_cl.setSelectedIndex(i);
			}
		}
	}

	public String getSelectedMGL() {
		return this.icb_mgl.getSelectedItem().toString();
	}

	public void setMGL(String mgl) {
		for (int i = 0; i < this.icb_mgl.getItems().length; i++) {
			if (this.icb_mgl.getItemAt(i).toString().compareToIgnoreCase(mgl) == 0) {
				this.icb_mgl.setSelectedIndex(i);
			}
		}
	}

	public void setChiefEng(String chiefEng) {
		for (int i = 0; i < this.icb_chiefEng.getItems().length; i++) {
			if (this.icb_chiefEng.getItemAt(i).toString().compareToIgnoreCase(chiefEng) == 0) {
				this.icb_chiefEng.setSelectedIndex(i);
			}
		}
	}

	public void setCostEng(String costEng) {
		for (int i = 0; i < this.icb_costEng.getItems().length; i++) {
			if (this.icb_costEng.getItemAt(i).toString().compareToIgnoreCase(costEng) == 0) {
				this.icb_costEng.setSelectedIndex(i);
			}
		}
	}

	public VFSourcingProcessDialog(TCSession session, boolean isCOPPartProcess, PostAction postActionWhenStartedSuccessfully) {
		super();
		this.session = session;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.userModuleLeader = new LinkedHashMap<>();
		this.userCL = new LinkedHashMap<>();
		this.userCostEng = new LinkedHashMap<String, TCComponentUser>();
		this.gmModuleLeader = new LinkedHashMap<>();
		this.gmCostEng = new LinkedHashMap<String, TCComponentGroupMember>();
		this.gmCL = new LinkedHashMap<>();
		this.gmCostEng = new LinkedHashMap<String, TCComponentGroupMember>();
		this.userBuyer = new LinkedHashMap<String, TCComponentUser>();
		this.gmBuyer = new LinkedHashMap<String, TCComponentGroupMember>();

		this.postActionWhenStartedSuccessfully = postActionWhenStartedSuccessfully;

		String[] MGLRole = session.getPreferenceService().getStringValues(MGL);
		for (int i = 0; i < MGLRole.length; i++) {
			String group = MGLRole[i].split(";")[0];
			String role = MGLRole[i].split(";")[1];
			Utils.getUserByGroupAndRole(session, group, role, userModuleLeader);
			Utils.getGroupMember(session, group, role, gmModuleLeader);
		}
		String[] CLRole = session.getPreferenceService().getStringValues(CL);
		for (int i = 0; i < CLRole.length; i++) {
			String group = CLRole[i].split(";")[0];
			String role = CLRole[i].split(";")[1];
			Utils.getUserByGroupAndRole(session, group, role, userCL);
			Utils.getGroupMember(session, group, role, gmCL);
		}

		String[] CostEngRole = session.getPreferenceService().getStringValues(COST_ENG);
		for (int i = 0; i < CostEngRole.length; i++) {
			String group = CostEngRole[i].split(";")[0];
			String role = CostEngRole[i].split(";")[1];
			Utils.getUserByGroupAndRole(session, group, role, userCostEng);
			Utils.getGroupMember(session, group, role, gmCostEng);
		}

		String[] buyerRole = session.getPreferenceService().getStringValues(BUYER);
		for (int i = 0; i < buyerRole.length; i++) {
			String group = buyerRole[i].split(";")[0];
			String role = buyerRole[i].split(";")[1];
			Utils.getUserByGroupAndRole(session, group, role, userBuyer);
			Utils.getGroupMember(session, group, role, gmBuyer);
		}

		String[] lovVF4SourcingCarPrg = Utils.getPreferenceValues(session, "LOV_VF4_SOURCING_CAR_PROGRAM");
		if (lovVF4SourcingCarPrg != null) {
			for (int i = 0; i < lovVF4SourcingCarPrg.length; i++) {
				String realName = lovVF4SourcingCarPrg[i].split(";")[0];
				String disName = lovVF4SourcingCarPrg[i].split(";")[1];
				this.lovVF4SourcingCarPrg.put(disName, realName);
			}
		}
	}

	public void createAndShowGUI(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final TCComponent[] arrSelectedItem, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final String dialogTitle) throws Exception {

		if (isOpen == true) {
			return;
		}

		this.listSelectedItem = Arrays.asList(arrSelectedItem);
		isOpen = true;
		this.setResizable(false);
		this.setAlwaysOnTop(true);

		final boolean[] checkAllPassed = new boolean[] { true };

		String[] prgm_values = input.keySet().toArray(new String[0]);
		setTitle(dialogTitle);
		JPanel sourcingPanel = new JPanel();
		sourcingPanel.setLayout(null);
		sourcingPanel.setBackground(Color.white);
		sourcingPanel.setPreferredSize(new Dimension(647, 800));

		Icon start_Icon = new ImageIcon(getClass().getResource("/icons/server_busystate_16.png"));

		JLabel jl_program = new JLabel("Program:");
		jl_program.setBounds(15, 20, 95, 25);
		sourcingPanel.add(jl_program);
		icb_program = new iComboBox(prgm_values);
		icb_program.setBounds(120, 20, 200, 25);
		icb_program.setMandatory(true);
		icb_program.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleProgramSelection(input, checkAllPassed, partsAndValidationResults);
					enableStartButton(checkAllPassed);
				} catch (Exception e1) {
					logger.error(e1);
					setAlwaysOnTop(false);
					MessageBox.post(e1);
				}
			}
		});
		icb_program.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_program.getTextField().getText(), icb_program)) {
					isValidProgram = true;
				} else {
					isValidProgram = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_program);

		JLabel jl_workflow = new JLabel("Workflow:");
		jl_workflow.setBounds(15, 55, 100, 25);
		sourcingPanel.add(jl_workflow);
		icb_workflow = new iComboBox();
		icb_workflow.setBounds(120, 55, 370, 25);
		icb_workflow.setMandatory(true);
		icb_workflow.setEnabled(false);
		sourcingPanel.add(icb_workflow);

		ib_Start = new iButton("Start");
		ib_Start.setIcon(start_Icon);
		ib_Start.setBounds(500, 55, 90, 25);
		ib_Start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handlerStartButton(partsAndValidationResults);
				} catch (TCException | NotLoadedException e1) {
					logger.error(e1);
					setAlwaysOnTop(false);
					MessageBox.post(e1);
				}

			}
		});
		ib_Start.setEnabled(false);
		sourcingPanel.add(ib_Start);

		JLabel jl_module = new JLabel("Module:");
		jl_module.setBounds(15, 93, 100, 25);
		sourcingPanel.add(jl_module);
		icb_module = new iComboBox();
		icb_module.setBounds(120, 93, 200, 25);
		icb_module.setMandatory(true);
		icb_module.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleModuleSelection(input, checkAllPassed);
					enableStartButton(checkAllPassed);
				} catch (TCException e1) {
					logger.error(e1);
					setAlwaysOnTop(false);
					MessageBox.post(e1);
				}
			}
		});
		icb_module.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_module.getTextField().getText(), icb_module)) {
					isValidModule = true;
				} else {
					isValidModule = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_module);

		JLabel jl_mgl = new JLabel("MGL/Manager:");
		jl_mgl.setBounds(15, 133, 100, 25);
		sourcingPanel.add(jl_mgl);
		icb_mgl = new iComboBox(userModuleLeader.values().toArray());
		icb_mgl.setBounds(120, 133, 200, 25);
		icb_mgl.setMandatory(true);
		icb_mgl.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_mgl.getTextField().getText(), icb_mgl)) {
					isValidMGL = true;
				} else {
					isValidMGL = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		icb_mgl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleMGLSelection();
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_mgl);

		JLabel jl_cl = new JLabel("CL:");
		jl_cl.setBounds(15, 173, 100, 25);
		sourcingPanel.add(jl_cl);
		icb_cl = new iComboBox(userCL.values().toArray());
		icb_cl.setBounds(120, 173, 200, 25);
		icb_cl.setMandatory(true);
		icb_cl.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_cl.getTextField().getText(), icb_cl)) {
					isValidCL = true;
				} else {
					isValidCL = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		icb_cl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleCLSelection();
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_cl);

		// cost engineer combobox
		JLabel jl_costEng = new JLabel("Cost Engineer:");
		jl_costEng.setBounds(15, 210, 100, 25);
		sourcingPanel.add(jl_costEng);
		icb_costEng = new iComboBox(userCostEng.values().toArray());
		icb_costEng.setBounds(120, 210, 200, 25);
		icb_costEng.setMandatory(true);
		icb_costEng.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_costEng.getTextField().getText(), icb_costEng)) {
					isValidCostEng = true;
				} else {
					isValidCostEng = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		icb_costEng.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleCostEngSelection();
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_costEng);

		// chief engineer combobox
		JLabel jl_chiefEng = new JLabel("Chief Engineer:");
		jl_chiefEng.setBounds(15, 250, 100, 25);
		sourcingPanel.add(jl_chiefEng);
		icb_chiefEng = new iComboBox(userModuleLeader.values().toArray());
		icb_chiefEng.setBounds(120, 250, 200, 25);
		icb_chiefEng.setMandatory(true);
		icb_chiefEng.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (handleComboboxTyping(icb_chiefEng.getTextField().getText(), icb_chiefEng)) {
					isValidChiefEng = true;
				} else {
					isValidChiefEng = false;
				}
				enableStartButton(checkAllPassed);
			}
		});
		icb_chiefEng.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleChiefEngSelection();
				enableStartButton(checkAllPassed);
			}
		});
		sourcingPanel.add(icb_chiefEng);

		JLabel jl_sor = new JLabel("SOR Doc:");
		jl_sor.setBounds(15, 290, 100, 25);
		sourcingPanel.add(jl_sor);

		tblSelectedObj = new TCTable();
		String[] tableAttr = new String[] { "object_string" };
		tblSelectedObj.setColumnNames(session, tableAttr, "ItemRevision");
		tblSelectedObj.getColumnModel().getColumn(0).setPreferredWidth(370);
		tblSelectedObj.setRowHeight(27);
		tblSelectedObj.setTableHeader(null);
		tblSelectedObj.setRowSelectionAllowed(true);
		tblSelectedObj.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		tblSelectedObj.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (tblSelectedObj.dataModel.getRowCount() > 0) {
					selectedSpecDoc = (TCComponent) tblSelectedObj.dataModel.getRow(0).getComponent();
					updateSolutionItemRelationInSpecDoc();
				}
			}
		});
		ScrollPagePane scrollPaneSelectedObj = new ScrollPagePane(tblSelectedObj);
		scrollPaneSelectedObj.setHorizontalScrollBarPolicy(ScrollPagePane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneSelectedObj.setVerticalScrollBarPolicy(ScrollPagePane.VERTICAL_SCROLLBAR_NEVER);
		scrollPaneSelectedObj.setBounds(120, 290, 370, 25);
		sourcingPanel.add(scrollPaneSelectedObj);

		Icon importIcon = new ImageIcon(getClass().getResource("/icons/add16.png"));
		ib_addSOR = new iButton(importIcon);
		ib_addSOR.setBounds(500, 290, 20, 25);
		ib_addSOR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WaitDialog wait = new WaitDialog();
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						try {
							handleSORSelection();
						} catch (Exception e1) {
							setAlwaysOnTop(false);
							int input = JOptionPane.showOptionDialog(null, e1.toString(), "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
							if (input == JOptionPane.OK_OPTION) {
								setAlwaysOnTop(true);
							}
						}
						wait.close();
						return null;
					}
				};
				mySwingWorker.execute();
				wait.makeWait("Loading...", e);
			}
		});
		sourcingPanel.add(ib_addSOR);

		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Validation Report"));
		scrollPane.setBounds(15, 330, 622, 460);
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
		sourcingPanel.add(scrollPane);

		getContentPane().add(sourcingPanel);
		pack();
		setLocationRelativeTo(null);

		setVisible(true);
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

	private boolean submitToWorkflow(TCComponent[] targets, String workflow, final String assignmentList, final String programValue, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) throws TCException {

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
					return createWorkflowProcess(assignmentList, programValue, isSuccess, bl, data, validators, partsAndValidationResults);
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

	private void handleProgramSelection(final Map<String, Map<String, VFProcessDialogProcessInput>> input, final boolean[] checkAllPassed, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) throws Exception {
		if (getSelectedProgram() != null) {
			has_P_PartInTarget = hasPPartInTarget(getSelectedProgram(), partsAndValidationResults);
			resetDialog();
			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(getSelectedProgram());
			if (workflowAndInput != null) {
				String[] workflowList = workflowAndInput.keySet().toArray(new String[0]);
				icb_workflow.setItems(workflowList);
				icb_workflow.setSelectedIndex(0);
			}
			handleWorkflowSelection(checkAllPassed, input);
			isValidProgram = true;
		} else {
			resetDialog();
			disableDialog();
		}

	}

	private void handleWorkflowSelection(final boolean[] checkAllPassed, final Map<String, Map<String, VFProcessDialogProcessInput>> input) throws Exception {
		String selectedProgram = icb_program.getSelectedItem().toString();
		final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
		String selectedWorkflow = icb_workflow.getSelectedItem().toString();
		VFProcessDialogProcessInput processInput = workflowAndInput.get(selectedWorkflow);
		if (processInput != null) {
			icb_module.setEnabled(true);
			List<VFAssignmentList> moduleItems = processInput.getAssignmentList();
			Collections.sort(moduleItems, (d1, d2) -> {
				return d1.toString().compareTo(d2.toString());
			});
			icb_module.setItems(moduleItems.toArray(new VFAssignmentList[0]));
			String defaultAssignmentList = processInput.getDefaultAssignmentList();
			if (defaultAssignmentList != null && !defaultAssignmentList.isEmpty()) {
				for (VFAssignmentList assList : processInput.getAssignmentList()) {
					if (assList.getName().contains(defaultAssignmentList)) {
						icb_module.setSelectedItem(assList);
						break;
					}
				}
			}

			IValidator validator = workflowAndInput.get(selectedWorkflow).getValidator();
			Map<TCComponent, List<ValidationResult>> partsValRes = new HashMap<TCComponent, List<ValidationResult>>();
			if (validator != null) {
				for (TCComponent target : listSelectedItem) {
					List<ValidationResult> valRes = null;
					if (target instanceof TCComponentBOMLine) {
						valRes = validator.validate((TCComponentBOMLine) target);
					} else {
						valRes = validator.validate((TCComponentItemRevision) target);
					}
					partsValRes.put(target, valRes);
				}
			}
			checkValidationResults(partsValRes, checkAllPassed);
		}
	}

	private IStatus createWorkflowProcess(final String assignmentList, final String programValue, final boolean[] isSuccess, final TCComponent bl, final ContextData data, final List<String> validators, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) {
		// 31-Oct: write part info into table
		boolean isCreatedTable = isCreatedSORTable(session, bl, partsAndValidationResults, programValue);
		if (isCreatedTable == false) {
			MessageBox.post("There is an issue while initialing workflow process, please contact to admin.", "ERROR", MessageBox.ERROR);
			return Status.CANCEL_STATUS;
		}
		InstanceInfo intInfo = WorkflowService.getService(session).createInstance(true, null, programValue + " - " + bl.toString(), null, programValue + " - " + bl.toString(), data);
		ServiceData output = intInfo.serviceData;

		if (output.sizeOfPartialErrors() > 0) {
			validators.add(Utils.HanlderServiceData(output));
			MessageBox.post(Utils.HanlderServiceData(output), "ERROR", MessageBox.ERROR);
			return Status.CANCEL_STATUS;
		} else {
			if (output.getCreatedObject(0) != null) {
				try {

					// TODO create dataset
//					TCComponentDataset dataset = exportBomlineInfo(partsAndValidationResults, output.getCreatedObject(0), this.icb_program.getTextField().getText());

					if (!assignSignoffToProcess((TCComponentProcess) output.getCreatedObject(0))) {
						try {
							MessageBox.post("There is an issue while assigning task to MGL/CL, please contact to admin.", "ERROR", MessageBox.ERROR);
							output.getCreatedObject(0).delete();
							return Status.CANCEL_STATUS;
						} catch (TCException e1) {
							e1.printStackTrace();
							return Status.CANCEL_STATUS;
						}
					}
					if (postActionWhenStartedSuccessfully != null) {
						postActionWhenStartedSuccessfully.execute(output.getCreatedObject(0));
					}
					isSuccess[0] = true;
					logger.info("(NOT ERROR): Successfully submitted to workflow.");
				} catch (Exception e) {
					MessageBox.post("There is an issue while initialing workflow process, please contact to admin.", "ERROR", MessageBox.ERROR);
					try {
						output.getCreatedObject(0).delete();
					} catch (TCException e1) {
						e1.printStackTrace();
						logger.error("[createWorkflowProcess] Exception: " + e1.toString());
					}
					e.printStackTrace();
					logger.error("[createWorkflowProcess] Exception: " + e.toString());
				}
			}
			return Status.OK_STATUS;
		}
	}

	private void handleModuleSelection(final Map<String, Map<String, VFProcessDialogProcessInput>> input, boolean[] checkAllPassed) throws TCException {
		this.icb_chiefEng.setSelectedIndex(-1);
		this.icb_cl.setSelectedIndex(-1);
		this.icb_costEng.setSelectedIndex(-1);
		this.icb_mgl.setSelectedIndex(-1);
		if (getSelectedProgram().compareToIgnoreCase(VFProgram.VF32) == 0) {
			if (getSelectedModule().contains("Premium")) {
				icb_chiefEng.setEnabled(true);
				isValidChiefEng = false;
			} else {
				icb_chiefEng.setEnabled(false);
				isValidChiefEng = true;
			}
		}
		if (getSelectedModule() != null) {
			// TODO get signoff by selected module
			String selectedProgram = getSelectedProgram();
			String selectedModule = getSelectedModule();
			String selectedWorkflow = getSelectedWorkflow();

			final Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(selectedProgram);
			VFProcessDialogProcessInput processInput = workflowAndInput.get(selectedWorkflow);
			TCComponentAssignmentList assignmentList = (TCComponentAssignmentList) processInput.getAssignmentListByDesc(selectedModule);
			TCComponent[] reses = assignmentList.getRelatedComponents("resources");
			TCComponent[] taskTemp = assignmentList.getRelatedComponents("task_templates");
			TCComponent[] resOfTaskMGLAprrove = null;
			TCComponent[] resOfTaskCLAprrove = null;
			TCComponent[] resOfTaskChiefEngAprrove = null;
			TCComponent[] resOfRnDDirectorApproval = null;
			TCComponent[] resOfTaskUpdateTargetCost = null;
			TCComponent[] resOfTaskRnDHeadReview = null;
			TCComponent[] resOfTaskNotifyBuyer = null;

			TCComponentUser mgl = null;
			TCComponentUser cl = null;
			TCComponentUser chiefEng = null;
			TCComponentUser rndDirector = null;
			TCComponentUser costEng = null;
			TCComponentUser rndHeadReviewer = null;
			TCComponentUser buyer = null;

			for (int i = 0; i < taskTemp.length; i++) {
				if (taskTemp[i].toString().compareToIgnoreCase("Module Leader Review") == 0) {
					resOfTaskMGLAprrove = reses[i].getRelatedComponents("resources");
					mgl = ((TCComponentGroupMember) resOfTaskMGLAprrove[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("Chief Engineer Review") == 0) {
					resOfTaskChiefEngAprrove = reses[i].getRelatedComponents("resources");
					chiefEng = ((TCComponentGroupMember) resOfTaskChiefEngAprrove[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("Purchase Review SOR Document") == 0) {
					resOfTaskCLAprrove = reses[i].getRelatedComponents("resources");
					cl = ((TCComponentGroupMember) resOfTaskCLAprrove[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("R&D Director Review") == 0) {
					resOfRnDDirectorApproval = reses[i].getRelatedComponents("resources");
					rndDirector = ((TCComponentGroupMember) resOfRnDDirectorApproval[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("Update Target Cost") == 0) {
					resOfTaskUpdateTargetCost = reses[i].getRelatedComponents("resources");
					costEng = ((TCComponentGroupMember) resOfTaskUpdateTargetCost[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("R&D Head Review") == 0) {
					resOfTaskRnDHeadReview = reses[i].getRelatedComponents("resources");
					rndHeadReviewer = ((TCComponentGroupMember) resOfTaskRnDHeadReview[0]).getUser();
				} else if (taskTemp[i].toString().compareToIgnoreCase("Assign to Buyer") == 0) {
					resOfTaskNotifyBuyer = reses[i].getRelatedComponents("resources");
					buyer = ((TCComponentGroupMember) resOfTaskNotifyBuyer[0]).getUser();
				}
			}
			if (rndHeadReviewer != null) {
				VFSourcingProcessDialog.rndHead = rndHeadReviewer.toString();
			}
			if (rndDirector != null) {
				VFSourcingProcessDialog.rndDirector = rndDirector.toString();
			}
			if (chiefEng != null) {
				setChiefEng(chiefEng.toString());
				if (!getSelectedChiefEng().isEmpty()) {
					isValidChiefEng = true;
				}
			}
			if (mgl != null) {
				setMGL(mgl.toString());
				if (!getSelectedMGL().isEmpty()) {
					isValidMGL = true;
				}
			}
			if (cl != null) {
				setCL(cl.toString());
				if (!getSelectedCL().isEmpty()) {
					isValidCL = true;
				}
			}
			if (buyer != null) {
				buyerOSName = buyer.toString();
			}
			if (costEng != null) {
				if (has_P_PartInTarget) {
					setCostEng(costEng.toString());
					if (!getSelectedCostEng().isEmpty()) {
						isValidCostEng = true;
					}
				} else {
					isValidCostEng = true;
				}
			}
			isValidModule = true;
		}
	}

	private void handlerStartButton(final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) throws TCException, NotLoadedException {
		String selectedProgram = icb_program.getSelectedItem().toString();
		String selectedAssignmentList = icb_module.getItemCount() > 0 ? ((VFAssignmentList) icb_module.getSelectedItem()).getName() : "";
		String selectedWorkflow = icb_workflow.getSelectedItem().toString();
		boolean isSuccess = false;
		/* remove duplicated bomline before add to target */
		ArrayList<TCComponent> targetComps = new ArrayList<>();
		ArrayList<String> uniqueID = new ArrayList<>();
		for (int i = 0; i < this.listSelectedItem.size(); i++) {
			String partID = "";
			TCComponentItemRevision partRev = null;
			if (this.listSelectedItem.get(i) instanceof TCComponentBOMLine) {
				TCComponentBOMLine aBOMLine = (TCComponentBOMLine) this.listSelectedItem.get(i);
				partRev = aBOMLine.getItemRevision();
				partID = aBOMLine.getPropertyDisplayableValue(Constant.VFBOMLINE.ITEM_ID);
			} else {
				partID = ((TCComponentItemRevision) this.listSelectedItem.get(i)).getProperty(Constant.VFWorkspaceObject.ID);
				partRev = (TCComponentItemRevision) this.listSelectedItem.get(i);
			}
			if (!uniqueID.contains(partID)) {
				targetComps.add(partRev);
				uniqueID.add(partID);
			}
		}

		if (selectedSpecDoc == null) {
			this.setAlwaysOnTop(false);
			MessageBox.post("Please select SOR document", "ERROR", MessageBox.ERROR);
			return;
		} else {
			/* thanhpn6-20200905-add sor document into target */
			targetComps.add(0, selectedSpecDoc);
		}

		TCComponent[] arrTarget = new TCComponent[targetComps.size()];
		targetComps.toArray(arrTarget);
		isSuccess = submitToWorkflow(arrTarget, selectedWorkflow, selectedAssignmentList, selectedProgram, partsAndValidationResults);

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

	public String getProgramByGroupmember() {
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

	private void handleSORSelection() throws Exception {
		setAlwaysOnTop(false);
		new VFSearchSORDocDialog().createAndShowGUI(session, tblSelectedObj);
	}

	private void updateSolutionItemRelationInSpecDoc() {
		try {
			TCComponent[] currSolutionItems = this.selectedSpecDoc.getRelatedComponents("EC_solution_item_rel");
			/* step1: remove all current parts */
			if (currSolutionItems != null && currSolutionItems.length > 0) {
				this.selectedSpecDoc.remove("EC_solution_item_rel", currSolutionItems);
			}
			/* step2: add new part */
			if (this.listSelectedItem != null && this.listSelectedItem.size() > 0) {
				ArrayList<TCComponent> arrPartRev = new ArrayList<>();
				ArrayList<String> uniqueID = new ArrayList<>();
				for (int i = 0; i < this.listSelectedItem.size(); i++) {
					TCComponentItemRevision partRev = null;
					String partID = "";
					if (this.listSelectedItem.get(i) instanceof TCComponentBOMLine) {
						TCComponentBOMLine aBOMLine = (TCComponentBOMLine) this.listSelectedItem.get(i);
						partRev = aBOMLine.getItemRevision();
						partID = aBOMLine.getPropertyDisplayableValue("bl_item_item_id");
					} else {
						partID = ((TCComponentItemRevision) this.listSelectedItem.get(i)).getProperty(Constant.VFWorkspaceObject.ID);
						partRev = (TCComponentItemRevision) this.listSelectedItem.get(i);
					}

					if (!uniqueID.contains(partID)) {
						arrPartRev.add(partRev);
						uniqueID.add(partID);
					}
				}
				this.selectedSpecDoc.setRelated("EC_solution_item_rel", arrPartRev.toArray(new TCComponent[arrPartRev.size()]));
				this.selectedSpecDoc.setProperty("vf4_car_program", lovVF4SourcingCarPrg.get(icb_program.getSelectedItem().toString().trim()));
				if (getSelectedModule().contains("Premium")) {
					this.selectedSpecDoc.setProperty("vf4_is_manuf_required", "TRUE");
				}
			}
		} catch (TCException | NotLoadedException e) {
			setVisible(false);
			int input = JOptionPane.showOptionDialog(null, e.toString(), "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, null, null);
			if (input == JOptionPane.OK_OPTION || input == JOptionPane.CLOSED_OPTION) {
				setVisible(true);
				tblSelectedObj.dataModel.removeAllRows();
				tblSelectedObj.revalidate();
				tblSelectedObj.repaint();
			}
		}
	}

	private boolean assignSignoffToProcess(TCComponentProcess process) throws TCException, ServiceException {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TCComponentGroupMember mgl = getMGLGroupMember(getSelectedMGL());
		TCComponentGroupMember cl = getCLGroupMember(getSelectedCL());
		TCComponentGroupMember chiefE = getMGLGroupMember(getSelectedChiefEng());
		TCComponentGroupMember rndDirector = getMGLGroupMember(VFSourcingProcessDialog.rndDirector);
		TCComponentGroupMember rndHead = getMGLGroupMember(VFSourcingProcessDialog.rndHead);
		TCComponentGroupMember costEng = getMGCostEngGroupMember(getSelectedCostEng());
		TCComponentUser costEngUser = userCostEng.get(getSelectedCostEng());
		TCComponentUser buyerUser = userBuyer.get(buyerOSName);
		TCComponentTask rootTask = ((TCComponentProcess) process).getRootTask();
		TCComponentTask[] subtasks = rootTask.getSubtasks();
		for (int i = 0; i < subtasks.length; i++) {
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMDoTask") == 0 && subtasks[i].getName().compareToIgnoreCase("Update Target Cost") == 0) {
				if (has_P_PartInTarget) {
//					subtasks[i].setRelated("responsible_party", new TCComponent[]{costEng});
					subtasks[i].setResponsibleParty(costEngUser);
//					((TCComponentTask) subtasks[i]).insertRelated("responsible_party", new TCComponent[]{null}, 1);
//					subtasks[i].setRelated("fnd0Performer", new TCComponent[] {userCostEng.get("Nathan Jarrod Benson (nathanb)")});
				}
			}

			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMTask") == 0 && subtasks[i].getName().compareToIgnoreCase("Assign to Buyer") == 0 && buyerUser != null) {
				subtasks[i].setResponsibleParty(buyerUser);
			}
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMConditionTask") == 0 && subtasks[i].getName().compareToIgnoreCase("isRequiredCE") == 0) {
				DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
				propInfo.object = subtasks[i];
				propInfo.timestamp = Calendar.getInstance();
				propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
				propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
				propInfo.vecNameVal[0].name = "task_result";
				if (has_P_PartInTarget) {
					propInfo.vecNameVal[0].values = new String[] { "true" };
				} else {
					propInfo.vecNameVal[0].values = new String[] { "false" };
				}
				DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
				if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
					logger.error("[assignSignoffToProcess]: " + Utils.HanlderServiceData(setPropertyResponse.data));
					return false;
				}
			}
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0) {
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
				if (subtasks[i].getName().compareToIgnoreCase("Module Leader Review") == 0) {
					createSignoff.signoffInfo[0].signoffMember = mgl;
				} else if (subtasks[i].getName().compareToIgnoreCase("Purchase Review SOR Document") == 0) {
					createSignoff.signoffInfo[0].signoffMember = cl;
				} else if (subtasks[i].getName().compareToIgnoreCase("Chief Engineer Review") == 0) {
					createSignoff.signoffInfo[0].signoffMember = chiefE;
				} else if (subtasks[i].getName().compareToIgnoreCase("R&D Director Review") == 0) {
					createSignoff.signoffInfo[0].signoffMember = rndDirector;
				} else if (subtasks[i].getName().compareToIgnoreCase("R&D Head Review") == 0) {
					createSignoff.signoffInfo[0].signoffMember = rndHead;
				} else {
					logger.error("[assignSignoffToProcess] There are no signoff member for task: " + subtasks[i].getName());
					return false;
				}
				if (createSignoff.signoffInfo[0].signoffMember != null) {
					ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
					if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
						logger.error("[assignSignoffToProcess]: " + Utils.HanlderServiceData(addSignoffsResponse));
						return false;
					}
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
						process.refresh();
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

	private void handleCostEngSelection() {
		if (getSelectedCostEng() != null && !getSelectedCostEng().isEmpty()) {
			isValidCostEng = true;
		} else {
			isValidCostEng = false;
		}
	}

	private void handleMGLSelection() {
		if (getSelectedMGL() != null && !getSelectedMGL().isEmpty()) {
			isValidMGL = true;
		} else {
			isValidMGL = false;
		}
	}

	private void handleCLSelection() {
		if (getSelectedCL() != null && !getSelectedCL().isEmpty()) {
			isValidCL = true;
		} else {
			isValidCL = false;
		}
	}

	private void handleChiefEngSelection() {
		if (getSelectedChiefEng() != null && !getSelectedChiefEng().isEmpty()) {
			isValidChiefEng = true;
		} else {
			isValidChiefEng = false;
		}
	}

	private boolean handleComboboxTyping(String inputValue, iComboBox combobox) {
		for (int i = 0; i < combobox.getItemCount(); i++) {
			if (inputValue.compareTo(combobox.getItemAt(i).toString()) == 0) {
				return true;
			}
		}
		return false;
	}

	private void enableStartButton(final boolean[] checkAllPassed) {
		if (getSelectedProgram().compareToIgnoreCase(VFProgram.VF33) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.Escooter) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe32) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe33) != 0
				&& getSelectedProgram().compareToIgnoreCase(VFProgram._3IN1) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe34NG) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.EBUS12) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VF31) != 0
				&& !getSelectedModule().contains("Premium")) {
			isValidChiefEng = true;
		}

		if (getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_EBUS) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_SnS) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_V8) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VF31) == 0
				|| getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe35) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe36) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe32) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe33) == 0
				|| getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe34S) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.Escooter) == 0 || has_P_PartInTarget == false) {
			isValidCostEng = true;
		}

		if (checkAllPassed[0] == true && isValidProgram == true && isValidMGL == true && isValidCL == true && isValidModule == true && isValidCostEng == true && isValidChiefEng == true) {
			ib_Start.setEnabled(true);
		} else {
			ib_Start.setEnabled(false);
		}
	}

	private void resetDialog() {
		if (getSelectedProgram().compareToIgnoreCase(VFProgram.VF33) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.Escooter) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe32) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe33) != 0
				&& getSelectedProgram().compareToIgnoreCase(VFProgram._3IN1) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VFe34NG) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.EBUS12) != 0 && getSelectedProgram().compareToIgnoreCase(VFProgram.VF31) != 0
				&& !getSelectedModule().contains("Premium")) {
			icb_chiefEng.setEnabled(false);
		} else {
			icb_chiefEng.setEnabled(true);
		}
		if (getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_EBUS) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe35) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe36) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_SnS) == 0
				|| getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_V8) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VF31) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe32) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe33) == 0
				|| getSelectedProgram().compareToIgnoreCase(VFProgram.AFS_VFe34S) == 0 || getSelectedProgram().compareToIgnoreCase(VFProgram.Escooter) == 0 || has_P_PartInTarget == false) {
			icb_costEng.setEnabled(false);
		} else {
			icb_costEng.setEnabled(true);
		}
		ib_Start.setEnabled(false);
		icb_workflow.removeAllItems();
		icb_module.removeAllItems();
		icb_mgl.setSelectedItem(null);
		icb_cl.setSelectedItem(null);
		icb_chiefEng.setSelectedItem(null);
		icb_costEng.setSelectedItem(null);
		tblSelectedObj.dataModel.removeAllRows();
		tblSelectedObj.repaint();
		tblSelectedObj.revalidate();

		isValidProgram = false;
		isValidCL = false;
		isValidMGL = false;
		isValidModule = false;
		isValidWf = false;
		isValidChiefEng = false;
		isValidCostEng = false;
	}

	private void disableDialog() {
		ib_Start.setEnabled(false);
		icb_workflow.setEnabled(false);
		icb_module.setEnabled(false);
		icb_mgl.setEnabled(false);
		icb_cl.setEnabled(false);
		ib_addSOR.setEnabled(false);
		icb_chiefEng.setEnabled(false);
		icb_costEng.setEnabled(false);
	}

	private TCComponentDataset exportBomlineInfo(final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, TCComponent createdProcess, String program) throws TCException, IOException {
		String rootTaskUid = createdProcess.getReferenceProperty("root_task").getUid();
		if (createdProcess != null) {
			logger.info("Creating dataset for " + rootTaskUid);
			System.out.println("Creating dataset for " + rootTaskUid);
			// create dataset and attach to workflow as references
			// rev_uid1~~ChangeIndex=aaa~~Purchase Level=bbbb
			// rev_uid2~~ChangeIndex=ccc~~Purchase Level=bbbb

			File tempParamFile = new File(System.getenv("tmp") + "\\" + String.valueOf(System.currentTimeMillis()) + ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempParamFile));
			for (TCComponent compo : partsAndValidationResults.keySet()) {
				if (compo instanceof TCComponentBOMLine) {
					if (program.compareToIgnoreCase(VFProgram.AFS_SnS) == 0 || program.compareToIgnoreCase(VFProgram.AFS_V8) == 0) {

						// Check purchase level to update flag hasPPartInTarget
						if (compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPT") == 0
								|| compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPS") == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("BL") == 0) {
							has_P_PartInTarget = true;
						}

						StringBuffer line = new StringBuffer();
						line.append("UID=");
						line.append(((TCComponentBOMLine) compo).getItemRevision().getUid());
						line.append("~~");
						line.append(Constant.VFBOMLINE.CHANGE_INDEX).append("=").append("NEW");
						line.append("~~");
						line.append(Constant.VFBOMLINE.PUR_LEVEL_VF).append("=").append(compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_GROUP_ENGLISH));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MODULE_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_NAME));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MAIN_MODULE));
						line.append("~~");
						line.append(Constant.VFBOMLINE.UOM).append("=").append(compo.getProperty(Constant.VFBOMLINE.UOM));
						writer.write(line.toString() + "\n");
					} else {

						// Check purchase level to update flag hasPPartInTarget
						if (compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPT") == 0
								|| compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPS") == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("BL") == 0) {
							has_P_PartInTarget = true;
						}

						StringBuffer line = new StringBuffer();
						line.append("UID=");
						line.append(((TCComponentBOMLine) compo).getItemRevision().getUid());
						line.append("~~");
						line.append(Constant.VFBOMLINE.CHANGE_INDEX).append("=").append("NEW");
						line.append("~~");
						line.append(Constant.VFBOMLINE.PUR_LEVEL_VF).append("=").append(compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MODULE_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.MODULE_ENGLISH));
						line.append("~~");
						line.append(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH).append("=").append(compo.getProperty(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH));
						line.append("~~");
						line.append(Constant.VFBOMLINE.UOM).append("=").append(compo.getProperty(Constant.VFBOMLINE.UOM));
						writer.write(line.toString() + "\n");
					}
				} else {
					StringBuffer line = new StringBuffer();
					line.append("UID=");
					line.append(((TCComponentItemRevision) compo).getUid());
					line.append("~~");
					line.append(Constant.VFBOMLINE.CHANGE_INDEX).append("=").append("NEW");
					line.append("~~");
					line.append(Constant.VFBOMLINE.PUR_LEVEL_VF).append("=").append(PropertyDefines.PUR_LEVEL_P);
					line.append("~~");
					line.append(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH).append("=").append("");
					line.append("~~");
					line.append(Constant.VFBOMLINE.MODULE_ENGLISH).append("=").append("");
					line.append("~~");
					line.append(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH).append("=").append("");
					line.append("~~");
					line.append(Constant.VFBOMLINE.UOM).append("=").append(((TCComponentItemRevision) compo).getItem().getProperty("uom_tag"));
					writer.write(line.toString() + "\n");
				}
			}
			writer.close();
			TCComponentDataset dataset = Utils.createDataset(session, rootTaskUid, "Text", "DBA_Created", tempParamFile.getAbsolutePath());
			return dataset;
		}
		return null;
	}

	private boolean hasPPartInTarget(String program, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) throws TCException {
		for (TCComponent compo : partsAndValidationResults.keySet()) {
			if (compo instanceof TCComponentBOMLine) {
				if (program.compareToIgnoreCase(VFProgram.AFS_SnS) == 0 || program.compareToIgnoreCase(VFProgram.AFS_V8) == 0) {
					// Check purchase level to update flag hasPPartInTarget
					if (compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPT") == 0
							|| compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPS") == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("BL") == 0) {
						return true;
					}
				} else {
					// Check purchase level to update flag hasPPartInTarget
					if (compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPT") == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPS") == 0
							|| compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("BL") == 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isCreatedSORTable(final TCSession session, final TCComponent sorItemRev, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults, final String program) {
		// check access write
		TCAccessControlService acl = session.getTCAccessControlService();
		boolean hasWriteAccess = false;
		try {
			hasWriteAccess = acl.checkPrivilege(sorItemRev, "WRITE");
			if (!hasWriteAccess) {
				MessageBox.post("You don't have access write on this SOR item", "WARNING", MessageBox.WARNING);
				return false;
			}
			CreateIn[] createInputs = new CreateIn[partsAndValidationResults.keySet().size()];

			int counter = 0;
			for (TCComponent compo : partsAndValidationResults.keySet()) {
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "VF4_SOR_part_info";
				if (compo instanceof TCComponentBOMLine) {
					String uomValue = ((TCComponentBOMLine) compo).getItemRevision().getItem().getPropertyDisplayableValue("uom_tag");
					TCComponent uomObj = TCExtension.GetUOMItem(uomValue);
					createInput.data.tagProps.put("vf4_design_part", ((TCComponentBOMLine) compo).getItemRevision());
					createInput.data.tagProps.put("vf4_uom", (TCComponent) uomObj);
					if (program.compareToIgnoreCase(VFProgram.AFS_SnS) == 0 || program.compareToIgnoreCase(VFProgram.AFS_V8) == 0) {
						// Check purchase level to update flag hasPPartInTarget
						if (compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPT") == 0
								|| compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("DPS") == 0 || compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL).compareToIgnoreCase("BL") == 0) {
							has_P_PartInTarget = true;
						}

//						createInput.data.stringProps.put("vf4_main_module", compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MAIN_MODULE));
//						createInput.data.stringProps.put("vf4_module_group", compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_GROUP_ENGLISH));
//						createInput.data.stringProps.put("vf4_module_name", compo.getProperty(Constant.VFBOMLINE.ATTR_SNS_MODULE_NAME));
						createInput.data.stringProps.put("vf4_purchase_level", compo.getProperty(Constant.VFBOMLINE.ATTR_VF3_PURCHASE_LVL));

					} else {
						// Check purchase level to update flag hasPPartInTarget
						if (compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase(PropertyDefines.PUR_LEVEL_P) == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPT") == 0
								|| compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("DPS") == 0 || compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF).compareToIgnoreCase("BL") == 0) {
							has_P_PartInTarget = true;
						}

						createInput.data.stringProps.put("vf4_main_module", compo.getProperty(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH));
						createInput.data.stringProps.put("vf4_module_group", compo.getProperty(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH));
						createInput.data.stringProps.put("vf4_module_name", compo.getProperty(Constant.VFBOMLINE.MODULE_ENGLISH));
						createInput.data.stringProps.put("vf4_purchase_level", compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF));
					}

					createInputs[counter] = createInput;
					counter++;
				} else {
					String uomValue = ((TCComponentItemRevision) compo).getItem().getPropertyDisplayableValue("uom_tag");
					TCComponent uomObj = TCExtension.GetUOMItem(uomValue);
					createInput.data.tagProps.put("vf4_design_part", compo);
					createInput.data.tagProps.put("vf4_uom", uomObj);
					createInput.data.stringProps.put("vf4_main_module", "");
					createInput.data.stringProps.put("vf4_module_group", "");
					createInput.data.stringProps.put("vf4_module_name", "");
					createInput.data.stringProps.put("vf4_purchase_level", PropertyDefines.PUR_LEVEL_P);

					createInputs[counter] = createInput;
					counter++;
				}
			}

			// create table row
			DataManagementService dms = DataManagementService.getService(session);
			CreateResponse response = dms.createObjects(createInputs);
			List<TCComponent> newSORTable = new LinkedList<TCComponent>();
			if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
				for (CreateOut output : response.output) {
					newSORTable.addAll(Arrays.asList(output.objects));
				}
			} else {
				MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.ERROR);
				return false;
			}

			// add to sor revision
			sorItemRev.setRelated("vf4_SOR_part_info", newSORTable.toArray(new TCComponent[0]));
			sorItemRev.refresh();
			if (sorItemRev.isCheckedOut() == false && sorItemRev.okToCheckout()) {
				CheckoutEditAction checkOutAction = new CheckoutEditAction();
				checkOutAction.run();
			}
		} catch (TCException | ServiceException | NotLoadedException e) {
			e.printStackTrace();
			MessageBox.post("Exception" + e.toString(), "ERROR", MessageBox.ERROR);
			return false;
		}
		return true;
	}

}