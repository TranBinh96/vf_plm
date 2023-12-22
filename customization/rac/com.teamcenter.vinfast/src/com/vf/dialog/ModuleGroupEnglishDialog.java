package com.vf.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.common.lov.view.components.LOVDisplayer;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLineType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.handlers.ModuleGroupInputAutomation;
import com.teamcenter.vinfast.utils.Utils;

public class ModuleGroupEnglishDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String PART_ID_PREFIX_AND_MODULE_GROUP = "PART_ID_PREFIX_AND_MODULE_GROUP";
	
	LOVDisplayer cbxModuleGroup;
	TCPropertyDescriptor lovDescriptor;
	TCComponentBOMLine []bomlines;
	TCSession session;
	Utils utils;
	String moduleGroupAttrName;
	String moduleGroupDisplayAttrName;
	TCAccessControlService acl;
	List<String> errorMessages;
	TCComponentBOMLine selectedLine;
	String endMessage;
	private static final Logger logger = Logger.getLogger(ModuleGroupInputAutomation.class);
	
	public ModuleGroupEnglishDialog(TCComponentBOMLine selectedLine, TCComponentBOMLine []bomlines, TCSession session) throws TCException {
		this.selectedLine = selectedLine;
		this.bomlines = bomlines;
		this.session = session;
		this.utils = new Utils();
		
		String group = session.getCurrentGroup().toString();
		String program = Utils.getProgram(group, session);
		Map<String, String> moduleGroupAttrNameMap = utils.getProgramsAndModuleAttributes();
		moduleGroupAttrName  = getModuleGroupAttributeName(program, moduleGroupAttrNameMap);
		moduleGroupDisplayAttrName = session.getTypeComponent("BOMLine").getPropDesc(moduleGroupAttrName).getDisplayName();
		
		lovDescriptor = session.getTypeComponent("BOMLine").getPropertyDescriptor(moduleGroupAttrName);
		acl = session.getTCAccessControlService();
		
		errorMessages = new LinkedList<String>();
		endMessage = "Update module group english success.";
	}

	private String getModuleGroupAttributeName(String program, Map<String, String> moduleGroupAttrNameMap) {
		return "VL5_module_group";
	}
	
	public void createAndShowGUI() throws Exception {
		setTitle("Update Module Group English");

		Dimension fixedSize = new Dimension(326, 160);
		this.setPreferredSize(fixedSize);
		this.setMinimumSize(fixedSize);
		this.setMaximumSize(fixedSize);
		this.setResizable(false);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBackground(SystemColor.inactiveCaptionBorder);
		mainPanel.setPreferredSize(new Dimension(320, 191));
		
		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/automation_16x16.png"));
		Icon defaultIcon = new ImageIcon(getClass().getResource("/icons/default_16x16.png"));
		setIconImage(frame_Icon.getImage());

//		JPanel populatePanel = new JPanel();
//		populatePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
//		populatePanel.setBounds(5, 5, 310, 56);
//		populatePanel.setLayout(null);
//		populatePanel.setBackground(SystemColor.inactiveCaptionBorder);
//		
//		mainPanel.add(populatePanel);
		
//		iButton btnPopulateDefault = new iButton("Populate Default Value");
//		btnPopulateDefault.setBounds(12, 13, 289, 30);
//		btnPopulateDefault.setIcon(defaultIcon);
//		btnPopulateDefault.setVerticalTextPosition(SwingConstants.CENTER);
//		btnPopulateDefault.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try {
//					populateDefaultModuleGroup();
//				} catch (TCException e1) {
//					logger.error(e1);
//				}
//			}
//		});
//		
//		populatePanel.add(btnPopulateDefault);
		
		JPanel overridePanel = new JPanel();
		overridePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
		overridePanel.setBounds(5, 5, 310, 120);
		overridePanel.setLayout(null);
		overridePanel.setBackground(SystemColor.inactiveCaptionBorder);
		
		mainPanel.add(overridePanel);
		
		JLabel lblModuleGroupEnglish = new JLabel("Override Module Group English");
		lblModuleGroupEnglish.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblModuleGroupEnglish.setBounds(12, 13, 264, 16);
		
		overridePanel.add(lblModuleGroupEnglish);
		
		final iButton btnStart = new iButton("OK");
		btnStart.setBounds(107, 82, 90, 30);
		btnStart.setVerticalTextPosition(SwingConstants.CENTER);
		btnStart.setHorizontalTextPosition(SwingConstants.CENTER);
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					fillModuleGroup();
				} catch (TCException e1) {
					logger.error(e1);
				}
			}
		});		
		overridePanel.add(btnStart);
		
		iButton btnCancel = new iButton("Cancel");
		btnCancel.setBounds(209, 82, 90, 30);
		btnCancel.setVerticalTextPosition(SwingConstants.CENTER);
		btnCancel.setHorizontalTextPosition(SwingConstants.CENTER);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		overridePanel.add(btnCancel);
		
		cbxModuleGroup = new LOVDisplayer(lovDescriptor);
		cbxModuleGroup.setBounds(12, 41, 287, 28);
		cbxModuleGroup.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String selectedDisplayVal = cbxModuleGroup.getSelectedDisplayValue();
				btnStart.setEnabled(selectedDisplayVal != null && selectedDisplayVal.isEmpty() == false);
			}
		});
		overridePanel.add(cbxModuleGroup);
		
		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void populateDefaultModuleGroup() throws TCException {
		AbstractAIFOperation operation = new AbstractAIFOperation() {
			@Override
			public void executeOperation() throws Exception {
				String moduleGroup = getDefaultModuleGroup(selectedLine);
				if (moduleGroup != null && moduleGroup.length() > 2) {
					for (TCComponentBOMLine bomline : bomlines) {
						String currentValue = bomline.getStringProperty(moduleGroupAttrName);
						if ((currentValue == null || currentValue.isEmpty() == true) && moduleGroup != null && moduleGroup.isEmpty() == false && bomline != selectedLine) {
							if (acl.checkPrivilege(bomline.getCachedParent().getBOMViewRevision(), "WRITE")) {
								try {
									bomline.setStringProperty(moduleGroupAttrName, moduleGroup);
								} catch (TCException ex) {
									logger.error(ex);
									errorMessages.add(generatedErrorMessageLine(bomline, "Fail as \"" + ex.getMessage() + "\""));
								}
							} 
							else {
								errorMessages.add(generatedErrorMessageLine(bomline, "Fail as NO Access Right"));
							}
						} 
						else if (bomline != selectedLine){
							errorMessages.add(generatedErrorMessageLine(bomline, "Not update as " + moduleGroupDisplayAttrName + " already filled \"" + moduleGroup + "\"."));
						}
					}	
				} 
				else {
					endMessage = "Please select a line with NOT empty " + moduleGroupDisplayAttrName + ".";
				}
			}

			private String getDefaultModuleGroup(TCComponentBOMLine selectedLine) throws NotLoadedException {
				String value = selectedLine.getPropertyDisplayableValue(moduleGroupAttrName);
				return value;
			}
		};
		
		session.queueOperationAndWait(operation);
		showConfirmDialog();
		setVisible(false);
	}
	
	private void showConfirmDialog() {
		this.setAlwaysOnTop(false);
		if (errorMessages.isEmpty()) {
			MessageBox.post(endMessage, "Information", MessageBox.INFORMATION);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					StringBuffer html = new StringBuffer();
					html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
					html.append("<tr style=\"background-color:#ccffff;\"><td><b>Level</b></td><td><b>BOMLine</b></td><td><b>Status</b></td></tr>");
					for (String errMsg : errorMessages) {
						String level = errMsg.split("\\|")[1];
						String line = errMsg.split("\\|")[0];
						String status = errMsg.split("\\|")[2];
						html.append("<tr>")
							.append("<td style='text-align:center'>").append(level).append("</td>")
							.append("<td>").append(line).append("</td>")
							.append("<td style='color:red'>").append(status).append("</td>")
							.append("</tr>");
					}
					html.append("</table></body></html>");
					StringViewerDialog dlg = new StringViewerDialog(new String[] {html.toString()});
					dlg.setTitle("Not Updated BOM Lines");
					dlg.setVisible(true);
				}
			});
		}
	}

	private void fillModuleGroup() throws TCException {
		AbstractAIFOperation operation = new AbstractAIFOperation("Auto-filling module group") {
			@Override
			public void executeOperation() throws Exception {
				String moduleGroup = cbxModuleGroup.getSelectedDisplayValue();
				for (TCComponentBOMLine bomline : bomlines) {
					if (bomline.getCachedParent() != null && acl.checkPrivilege(bomline.getCachedParent().getBOMViewRevision(), "WRITE")) {
						try {
							bomline.setStringProperty(moduleGroupAttrName, moduleGroup);
						} catch (Exception ex) {
							logger.error(ex);
							errorMessages.add(generatedErrorMessageLine(bomline, "Fail as \"" + ex.getMessage() + "\""));
						}						
					} else {
						errorMessages.add(generatedErrorMessageLine(bomline, "Fail as NO Access Right"));
					}
				}
			}
		};
		session.queueOperationAndWait(operation);
		showConfirmDialog();
		setVisible(false);
	}
	
	private String generatedErrorMessageLine(TCComponentBOMLine bomline, String note) throws TCException {
		return bomline.getProperty("object_string") + "|" + bomline.getProperty("bl_level_starting_0") + "|" + note;
	}
}
