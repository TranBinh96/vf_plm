package com.vf.dialog;

import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Label;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.common.lov.view.components.LOVDisplayer;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.vinfast.handlers.ModuleGroupInputAutomation;
import com.teamcenter.vinfast.utils.Utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class BomlineSingleLovAutomationDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String PART_ID_PREFIX_AND_MODULE_GROUP = "PART_ID_PREFIX_AND_MODULE_GROUP";
	String regex = "[0-9]*\\.?[0-9]*";
	
	LOVDisplayer cbxLovAttribute;
	TCPropertyDescriptor lovDescriptor;
	TCComponentBOMLine []bomlines;
	TCSession session;
	Utils utils;
	String bomlineAttrName;
	TCAccessControlService acl;
	String attributeDisplayName;
	String dialogName;
	List<String> errorMessages;
	iButton btnStart;
	
	NumberFormat percentFormat;
	double quantity = 1;
	
	private static final Logger logger = Logger.getLogger(ModuleGroupInputAutomation.class);
	
	public BomlineSingleLovAutomationDialog(String title, String bomlineAttrName, String iconPath, TCComponentBOMLine []bomlines, TCSession session) throws TCException {
		this.bomlines = bomlines;
		this.session = session;
		this.utils = new Utils();
		
		lovDescriptor = session.getTypeComponent("BOMLine").getPropertyDescriptor(bomlineAttrName);
		attributeDisplayName = lovDescriptor.getDisplayName();
		this.bomlineAttrName = bomlineAttrName;
		this.dialogName = title;
		acl = session.getTCAccessControlService();
				
		setTitle(title);
		
		if (iconPath != null && iconPath.isEmpty() == false) {
			URL icoRes = getClass().getResource(iconPath);
			if (icoRes != null) {
				ImageIcon frame_Icon = new ImageIcon(icoRes);
				setIconImage(frame_Icon.getImage());
			}
		}
		
		errorMessages = new LinkedList<String>();
	}
	
	public void createAndShowGUI() throws Exception {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBackground(SystemColor.inactiveCaptionBorder);
		int width = 450;

		Dimension fixedSize = new Dimension(width, 150);
		this.setPreferredSize(fixedSize);
		this.setMinimumSize(fixedSize);
		this.setMaximumSize(fixedSize);
		mainPanel.setPreferredSize(new Dimension(width - 6, 150));
		this.setResizable(false);
		
		JPanel overridePanel = new JPanel();
		overridePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
		overridePanel.setBounds(5, 5, width - 15, 110);
		mainPanel.add(overridePanel);
		overridePanel.setLayout(null);
		
//		JLabel lblModuleGroupEnglish = new JLabel("Override " + attributeDisplayName);
//		lblModuleGroupEnglish.setFont(new Font("Tahoma", Font.PLAIN, 13));
//		lblModuleGroupEnglish.setBounds(12, 13, 264, 16);
//		overridePanel.add(lblModuleGroupEnglish);
//		overridePanel.setBackground(SystemColor.inactiveCaptionBorder);
		
		btnStart = new iButton("OK");
		btnStart.setBounds(width - 117, 70, 90, 30);
		btnStart.setVerticalTextPosition(SwingConstants.CENTER);
		btnStart.setHorizontalTextPosition(SwingConstants.CENTER);
		overridePanel.add(btnStart);
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					fillAttribute();
				} catch (TCException e1) {
					logger.error(e1);
				}
			}

		});
		
//		iButton btnCancel = new iButton("Cancel");
//		btnCancel.setBounds(290, 54, 90, 30);
//		btnCancel.setVerticalTextPosition(SwingConstants.CENTER);
//		btnCancel.setHorizontalTextPosition(SwingConstants.CENTER);
//		overridePanel.add(btnCancel);
//		btnCancel.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				setVisible(false);
//			}
//		});
		
		cbxLovAttribute = new LOVDisplayer(lovDescriptor);
		cbxLovAttribute.setBounds(12, 13, width - 40, 28);
		overridePanel.add(cbxLovAttribute);
		cbxLovAttribute.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String selectedDisplayVal = cbxLovAttribute.getSelectedDisplayValue();
				btnStart.setEnabled(selectedDisplayVal != null && selectedDisplayVal.isEmpty() == false);
			}
		});	
		
		percentFormat = NumberFormat.getNumberInstance();

		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void showConfirmDialog() {
		this.setAlwaysOnTop(false);
		if (errorMessages.isEmpty()) {
			MessageBox.post("The update is completed successfully.", "Information", MessageBox.INFORMATION);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					StringBuffer html = new StringBuffer();
					html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
					html.append("<tr><td><b>NOT Updated Line</b></td><td><b>Level</b></td></tr>");
					for (String errMsg : errorMessages) {
						String level = errMsg.split("\\|")[1];
						String line = errMsg.split("\\|")[0];
						html.append("<tr><td>").append(line).append("</td>").append("<td>").append(level).append("</td>").append("</tr>");
					}
					html.append("</table></body></html>");
					StringViewerDialog dlg = new StringViewerDialog(new String[] {html.toString()});
					dlg.setTitle("Not Updated Lines");
					dlg.setVisible(true);
				}
			});
		}
	}

	private void fillAttribute() throws TCException {
		AbstractAIFOperation operation = new AbstractAIFOperation("Auto-filling value") {
			@Override
			public void executeOperation() throws Exception {
				String willFillValue = cbxLovAttribute.getSelectedDisplayValue();
				for (TCComponentBOMLine bomline : bomlines) {
					if (bomline.getCachedParent() != null && acl.checkPrivilege(bomline.getCachedParent().getBOMViewRevision(), "WRITE")) {
						try {
							bomline.setStringProperty(bomlineAttrName, willFillValue);
						} catch (Exception ex) {
							logger.error(ex);
							errorMessages.add(generatedErrorMessageLine(bomline));
						}
					} else {
						errorMessages.add(generatedErrorMessageLine(bomline));
					}
				}
			}
		};
		session.queueOperationAndWait(operation);
		showConfirmDialog();
		setVisible(false);
	}

	private String generatedErrorMessageLine(TCComponentBOMLine bomline) throws TCException {
		return bomline.getProperty("object_string") + "|" + bomline.getProperty("bl_level_starting_0");
	}
}
