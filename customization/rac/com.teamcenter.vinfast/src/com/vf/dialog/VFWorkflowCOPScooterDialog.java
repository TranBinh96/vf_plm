package com.vf.dialog;

import java.util.LinkedList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soaictstubs.stringSeq_tHolder;
import com.vf.utils.TCExtension;
//import com.teamcenter.vinfast.model.*;

public class VFWorkflowCOPScooterDialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	private Button btnCreate;
	private TCSession session;
	
	
	private Label lblNewLabel;
	private Combo cbProgram;
	private Label lblWorkflow;
	private Label lblModule;
	private Label lblEcr;
	private Combo cbWorkflow;
	private Combo cbModule;
	private Combo cbCombineWith;
	
	private String ObjectType = "Vf6_ECRRevision";
	
	private final static String VF_PROGRAM_NAMES = "VF_Process_Scooter_Program_Names";
	private final static String VF_PROGRAM_ASSIGNMENT = "VF_Process_Scooter_Program_Assignment";
	private final static String VF_PROGRAM_WORKFLOW = "VF_Program_Type_Workflow";
	
	public VFWorkflowCOPScooterDialog(Shell parentShell, TCSession session) throws TCException {
		super(parentShell);
		this.session = session;
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			//-------------------------------------------init
//			LinkedList<WPProgramModel> ProgramList = new LinkedList<WPProgramModel>();
//			String[] programDataForm = TCExtension.GetPreferenceValues(VF_PROGRAM_NAMES, session);
//			String[] programAssignDataForm = TCExtension.GetPreferenceValues(VF_PROGRAM_ASSIGNMENT, session);
//			String[] programWorkflowDataForm = TCExtension.GetPreferenceValues(VF_PROGRAM_WORKFLOW, session);
//			
//			for (String program : programDataForm) {
//				WPProgramModel newProgram = new WPProgramModel();
//				newProgram.ProgramName = program;
//			}
//			
//			for (String programAssign : programAssignDataForm) {
//				String[] strSplit = programAssign.split("==");
//				if(strSplit.length >= 3) {
//					String programName = strSplit[0];
//					String taskName = strSplit[1];
//					String userID = strSplit[2];
//					Boolean check = true;
//					for (WPProgramModel program : ProgramList) {
//						if(program.ProgramName.compareToIgnoreCase(programName) == 0) {
//							WPTaskModel task = program.AddTask(taskName);
//							task.AddUser(userID, "", "", "");
//							check = false;
//							break;
//						}
//					}
//					if(check) {
//						WPProgramModel newProgram = new WPProgramModel();
//						newProgram.ProgramName = programName;
//						ProgramList.add(newProgram);
//						WPTaskModel task = newProgram.AddTask(taskName);
//						task.AddUser(userID, "", "", "");
//					}
//				}
//			}
			//-------------------------------------------
//			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Trigger DFMEA Approval Process");
			
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("Program:");
			}
			{
				cbProgram = new Combo(container, SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				GridData gd_cbProgram = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_cbProgram.widthHint = 395;
				cbProgram.setLayoutData(gd_cbProgram);
//				cbProgram.setItems(programDataForm);
			}
			{
				lblWorkflow = new Label(container, SWT.NONE);
				lblWorkflow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblWorkflow.setText("Workflow:");
				lblWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbWorkflow = new Combo(container, SWT.READ_ONLY);
				cbWorkflow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbWorkflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			}
			{
				lblModule = new Label(container, SWT.NONE);
				lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModule.setText("Module:");
				lblModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbModule = new Combo(container, SWT.READ_ONLY);
				cbModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				cbCombineWith = new Combo(container, SWT.READ_ONLY);
				cbCombineWith.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCombineWith.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblEcr = new Label(container, SWT.NONE);
				lblEcr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblEcr.setText("ECR:");
				lblEcr.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}
	
	public void setProgressStatus(int percentage) {
//		display = new Display();
//		new LongRunningOperation(display, progressBar).start();
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Start", true);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(480, 320);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("DFMEA Process");
	}
}
