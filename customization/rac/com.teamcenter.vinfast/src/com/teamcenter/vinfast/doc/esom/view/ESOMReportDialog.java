package com.teamcenter.vinfast.doc.esom.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.jdesktop.swingx.JXDatePicker;
import java.awt.GridLayout;
import javax.swing.JCheckBox;

public class ESOMReportDialog extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -775551190708280736L;
	private JPanel wrapper;
	public JLabel lblChooseProgram;
	public JButton btnLeft;
	public JButton btnRight;
	public JComboBox<String> cbProgram;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ESOMReportDialog frame = new ESOMReportDialog();
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
	public ESOMReportDialog() {
		setTitle("ESOM Report");
		setBounds(100, 100, 463, 110);
		wrapper = new JPanel();
		wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(wrapper);
		wrapper.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		wrapper.add(panel_1, BorderLayout.SOUTH);
		
		btnLeft = new JButton("OK");
		btnLeft.setPreferredSize(new Dimension(100, 25));
		panel_1.add(btnLeft);
		
		btnRight = new JButton("Cancel");
		btnRight.setPreferredSize(new Dimension(100, 25));
		panel_1.add(btnRight);
		
		JPanel panel_2 = new JPanel();
		wrapper.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		
		lblChooseProgram = new JLabel("Program:");
		panel_2.add(lblChooseProgram);
		
		cbProgram = new JComboBox<String>();
		panel_2.add(cbProgram);
		
	}
}
