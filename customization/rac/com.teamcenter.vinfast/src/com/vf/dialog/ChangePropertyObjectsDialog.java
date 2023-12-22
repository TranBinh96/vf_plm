package com.vf.dialog;

import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.common.lov.view.components.LOVDisplayer;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.vinfast.handlers.ModuleGroupInputAutomation;
import com.teamcenter.vinfast.utils.Utils;

public class ChangePropertyObjectsDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String PART_ID_PREFIX_AND_MODULE_GROUP = "PART_ID_PREFIX_AND_MODULE_GROUP";
	
	LOVDisplayer cbxLovAttribute;
	TCPropertyDescriptor lovDescriptor;
	TCComponent []objects;
	TCSession session;
	Utils utils;
	String objectAttrName;
	TCAccessControlService acl;
	String attributeDisplayName;
	String dialogName;
	List<String> errorMessages;
	int attributeType = -1;
	TCComponentBOMLine[] bomlines;
	
	private static final Logger logger = Logger.getLogger(ModuleGroupInputAutomation.class);
	
	public ChangePropertyObjectsDialog(String title, TCComponentBOMLine[] bomlines, List<TCComponent> objects, String objectType, String attributeName,
			TCSession sessionIn) throws TCException {
		this.objects = objects.toArray(new TCComponent[0]);
		this.bomlines = bomlines;
		session = sessionIn;
		utils = new Utils();
		
		TCPropertyDescriptor propDescriptor = session.getTypeComponent(objectType).getPropertyDescriptor(attributeName);
		attributeType = propDescriptor.getType();
		if (propDescriptor.getType() == TCPropertyDescriptor.CLIENT_PROP_TYPE_bool) {
			//TODO: find a way to get LOV from LOV name.
			lovDescriptor = session.getTypeComponent("Vf6_ECN").getPropertyDescriptor("vf6_vehicle_weight_affected");
		} else {
			//TODO: handle other cases
		}
		
		attributeDisplayName = propDescriptor.getDisplayName();
		objectAttrName = attributeName;
		
		dialogName = title;
		acl = session.getTCAccessControlService();
		setTitle(title);
		errorMessages = new LinkedList<String>();
	}

	public void createAndShowGUI() throws Exception {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBackground(SystemColor.inactiveCaptionBorder);

		Dimension fixedSize = new Dimension(326, 136);
		this.setPreferredSize(fixedSize);
		this.setMinimumSize(fixedSize);
		this.setMaximumSize(fixedSize);
		mainPanel.setPreferredSize(new Dimension(320, 112));
		this.setResizable(false);
		
		JPanel overridePanel = new JPanel();
		overridePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
		overridePanel.setBounds(5, 5, 310, 92);
		mainPanel.add(overridePanel);
		overridePanel.setLayout(null);
		
//		JLabel lblModuleGroupEnglish = new JLabel("Override " + attributeDisplayName);
//		lblModuleGroupEnglish.setFont(new Font("Tahoma", Font.PLAIN, 13));
//		lblModuleGroupEnglish.setBounds(12, 13, 264, 16);
//		overridePanel.add(lblModuleGroupEnglish);
		overridePanel.setBackground(SystemColor.inactiveCaptionBorder);
		
		final iButton btnStart = new iButton("OK");
		btnStart.setBounds(107, 54, 90, 30);
		btnStart.setVerticalTextPosition(SwingConstants.CENTER);
		btnStart.setHorizontalTextPosition(SwingConstants.CENTER);
		overridePanel.add(btnStart);
		
		iButton btnCancel = new iButton("Cancel");
		btnCancel.setBounds(209, 54, 90, 30);
		btnCancel.setVerticalTextPosition(SwingConstants.CENTER);
		btnCancel.setHorizontalTextPosition(SwingConstants.CENTER);
		overridePanel.add(btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
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
		btnStart.setEnabled(false);
		
		cbxLovAttribute = new LOVDisplayer(lovDescriptor);
		cbxLovAttribute.setBounds(12, 13, 287, 28);
		overridePanel.add(cbxLovAttribute);
		cbxLovAttribute.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String selectedDisplayVal = cbxLovAttribute.getSelectedDisplayValue();
				btnStart.setEnabled(selectedDisplayVal != null && selectedDisplayVal.isEmpty() == false);
			}
		});

		
		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void showConfirmDialog() throws TCException {
		this.setAlwaysOnTop(false);
		if (errorMessages.isEmpty()) {			
			for (TCComponentBOMLine bomline : bomlines) {
				bomline.refresh();
			}
			MessageBox.post("The update is completed successfully.", "Information", MessageBox.INFORMATION);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					StringBuffer html = new StringBuffer();
					html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
					html.append("<tr><td><b>NOT Updated Part</b></td></tr>");
					for (String errMsg : errorMessages) {
						String line = errMsg;
						html.append("<tr><td>").append(line).append("</td>").append("</tr>");
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
				for (TCComponent object : objects) {
					if (acl.checkPrivilege(object, "WRITE")) {
						try {
							if (attributeType == TCPropertyDescriptor.CLIENT_PROP_TYPE_string) {
								object.setStringProperty(objectAttrName, willFillValue);								
							} else if (attributeType == TCPropertyDescriptor.CLIENT_PROP_TYPE_bool) {
								object.setLogicalProperty(objectAttrName, willFillValue.toLowerCase().contains("yes") || willFillValue.toLowerCase().contains("true") || willFillValue.contains("1"));
							} else {
								// TODO: handle other cases.
							}
						} catch (Exception ex) {
							logger.error(ex);
							errorMessages.add(generatedErrorMessageLine(object));
						}
					} else {
						errorMessages.add(generatedErrorMessageLine(object));
					}
				}
			}
		};
		session.queueOperationAndWait(operation);
		showConfirmDialog();
		setVisible(false);
	}

	private String generatedErrorMessageLine(TCComponent bomline) throws TCException {
		return bomline.getProperty("object_string");
	}
}
