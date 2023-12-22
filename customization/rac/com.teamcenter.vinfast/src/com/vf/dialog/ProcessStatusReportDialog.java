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

public class ProcessStatusReportDialog extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -775551190708280736L;
	private JPanel wrapper;
	public JLabel lblChooseProcess;
	public JLabel lblCreationBefore;
	public JLabel lblCreationAfter;
	public JButton btnLeft;
	public JButton btnRight;
	public JComboBox<String> cbProcess;
	public JXDatePicker creationBefore;
	public JXDatePicker creationAfter;
	public JLabel lblProcessName;
	public JTextField txtprocessName;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProcessStatusReportDialog frame = new ProcessStatusReportDialog();
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
	public ProcessStatusReportDialog() {
		setTitle("Process Status Report");
		setBounds(100, 100, 400, 300);
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
		
		JPanel panel_2 = new JPanel();
		wrapper.add(panel_2, BorderLayout.CENTER);
		SpringLayout layout_2 = new SpringLayout();
		panel_2.setLayout(layout_2);
		
		lblChooseProcess = new JLabel("Choose Process:");
		layout_2.putConstraint(SpringLayout.NORTH, lblChooseProcess, 10, SpringLayout.NORTH, panel_2);
		layout_2.putConstraint(SpringLayout.WEST, lblChooseProcess, 10, SpringLayout.WEST, panel_2);
		panel_2.add(lblChooseProcess);
		
		cbProcess = new JComboBox<String>();
		layout_2.putConstraint(SpringLayout.NORTH, cbProcess, 7, SpringLayout.NORTH, panel_2);
		layout_2.putConstraint(SpringLayout.WEST, cbProcess, 6, SpringLayout.EAST, lblChooseProcess);
		layout_2.putConstraint(SpringLayout.EAST, cbProcess, -10, SpringLayout.EAST, panel_2);
		panel_2.add(cbProcess);
		
		lblCreationBefore = new JLabel("Create Before:   ");
		layout_2.putConstraint(SpringLayout.NORTH, lblCreationBefore, 10, SpringLayout.SOUTH, lblChooseProcess);
		layout_2.putConstraint(SpringLayout.WEST, lblCreationBefore, 10, SpringLayout.WEST, panel_2);
		panel_2.add(lblCreationBefore);
		
		creationBefore = new JXDatePicker();
		creationBefore.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		layout_2.putConstraint(SpringLayout.NORTH, creationBefore, 7, SpringLayout.SOUTH, cbProcess);
		layout_2.putConstraint(SpringLayout.WEST, creationBefore, 6, SpringLayout.EAST, lblCreationBefore);
		layout_2.putConstraint(SpringLayout.EAST, creationBefore, -10, SpringLayout.EAST, panel_2);
		panel_2.add(creationBefore);
		
		lblCreationAfter = new JLabel("Create After:      ");
		layout_2.putConstraint(SpringLayout.NORTH, lblCreationAfter, 15, SpringLayout.SOUTH, lblCreationBefore);
		layout_2.putConstraint(SpringLayout.WEST, lblCreationAfter, 10, SpringLayout.WEST, panel_2);
		panel_2.add(lblCreationAfter);
		
		creationAfter = new JXDatePicker();
		creationAfter.setFormats(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
		layout_2.putConstraint(SpringLayout.NORTH, creationAfter, 7, SpringLayout.SOUTH, creationBefore);
		layout_2.putConstraint(SpringLayout.WEST, creationAfter, 6, SpringLayout.EAST, lblCreationAfter);
		layout_2.putConstraint(SpringLayout.EAST, creationAfter, -10, SpringLayout.EAST, panel_2);
		panel_2.add(creationAfter);
		
		lblProcessName = new JLabel("Process Name:");
		layout_2.putConstraint(SpringLayout.NORTH, lblProcessName, 15, SpringLayout.SOUTH, lblCreationAfter);
		layout_2.putConstraint(SpringLayout.WEST, lblProcessName, 10, SpringLayout.WEST, panel_2);
		panel_2.add(lblProcessName);
		
		txtprocessName = new JTextField();
		layout_2.putConstraint(SpringLayout.NORTH, txtprocessName, 7, SpringLayout.SOUTH, creationAfter);
		layout_2.putConstraint(SpringLayout.WEST, txtprocessName, 6, SpringLayout.EAST, lblProcessName);
		layout_2.putConstraint(SpringLayout.EAST, txtprocessName, -10, SpringLayout.EAST, panel_2);
		panel_2.add(txtprocessName);
	}
}
