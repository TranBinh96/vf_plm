package com.vf.dialog;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JComboBox;


public class AssignToUserForm extends JFrame {

	private JPanel contentPane;
	public JLabel lblChooseCl;
	public JComboBox cbListUser;
	public JButton btnLeft;
	public JButton btnRight;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AssignToUserForm frame = new AssignToUserForm();
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
	public AssignToUserForm() {
		setTitle("Assign to Buyer Dialog");
		setBounds(100, 100, 500, 157);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		
		btnLeft = new JButton("New button");
		btnLeft.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(btnLeft);
		
		btnRight = new JButton("New button");
		panel.add(btnRight);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);
		
		lblChooseCl = new JLabel("Choose CL:");
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblChooseCl, 10, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblChooseCl, 10, SpringLayout.WEST, panel_1);
		panel_1.add(lblChooseCl);
		
		cbListUser = new JComboBox();
		sl_panel_1.putConstraint(SpringLayout.NORTH, cbListUser, 7, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, cbListUser, 6, SpringLayout.EAST, lblChooseCl);
		sl_panel_1.putConstraint(SpringLayout.EAST, cbListUser, -10, SpringLayout.EAST, panel_1);
		panel_1.add(cbListUser);
	}

}
