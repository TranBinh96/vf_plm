package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import org.jdesktop.swingx.JXDatePicker;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

public class ProcessStatusReportFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -775551190708280736L;
	private JPanel wrapper;
	public JLabel lblChooseProcess;
	public JLabel lblCreationBefore;
	public JLabel lblCreationAfter;
	public JLabel lblModuleGroupECR;
	public JButton btnLeft;
	public JButton btnRight;
	public JComboBox<String> cbProcess;
	public JComboBox<String> cbModuleGroupECR;
	public JXDatePicker creationBefore;
	public JXDatePicker creationAfter;
	public JLabel lblProcessName;
	public JTextField txtprocessName;
	public JLabel lblModifyBefore;
	public JLabel lblModifyAfter;
	public JXDatePicker modifyAfter;
	public JXDatePicker modifyBefore;
	public JCheckBox chckbxNewCheckBox;
	public JLabel lblRunningProcess;
	public JPanel panel_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProcessStatusReportFrame frame = new ProcessStatusReportFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ProcessStatusReportFrame() {
		setTitle("Process Status Report");
		setBounds(100, 100, 525, 312);
		wrapper = new JPanel();
		wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(wrapper);
		wrapper.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		wrapper.add(panel_1, BorderLayout.SOUTH);
		
		btnLeft = new JButton("OK");
		btnLeft.setPreferredSize(new Dimension(150, 40));
		panel_1.add(btnLeft);
		
		btnRight = new JButton("Cancel");
		btnRight.setPreferredSize(new Dimension(150, 40));
		panel_1.add(btnRight);
		
		panel_2 = new JPanel();
		wrapper.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		
		lblChooseProcess = new JLabel("Choose Process:");
		panel_2.add(lblChooseProcess);
		
		cbProcess = new JComboBox<String>();
		panel_2.add(cbProcess);

		lblModuleGroupECR = new JLabel("Module Group:");
		panel_2.add(lblModuleGroupECR);
		
		cbModuleGroupECR = new JComboBox<String>();
		panel_2.add(cbModuleGroupECR);
		
		lblCreationBefore = new JLabel("Create Before:");
		panel_2.add(lblCreationBefore);
		
		creationBefore = new JXDatePicker();
		creationBefore.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		panel_2.add(creationBefore);
		
		lblCreationAfter = new JLabel("Create After:");
		panel_2.add(lblCreationAfter);
		
		creationAfter = new JXDatePicker();
		creationAfter.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		panel_2.add(creationAfter);
		
		lblModifyBefore = new JLabel("Modify Before:");
		panel_2.add(lblModifyBefore);
		
		modifyBefore = new JXDatePicker();
		modifyBefore.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		panel_2.add(modifyBefore);
		
		lblModifyAfter = new JLabel("Modify After:");
		panel_2.add(lblModifyAfter);
		
		modifyAfter = new JXDatePicker();
		modifyAfter.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		panel_2.add(modifyAfter);
		
		lblProcessName = new JLabel("Process Name:");
		panel_2.add(lblProcessName);
		
		txtprocessName = new JTextField();
		panel_2.add(txtprocessName);
		
		lblRunningProcess = new JLabel("Running Process:");
		panel_2.add(lblRunningProcess);
		
		chckbxNewCheckBox = new JCheckBox("");
		panel_2.add(chckbxNewCheckBox);
		
	}
}
