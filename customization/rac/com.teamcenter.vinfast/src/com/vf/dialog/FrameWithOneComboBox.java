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


public class FrameWithOneComboBox extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8898580456647981330L;
	private JPanel contentPane;
	public JLabel label1;
	public JComboBox comboBox1;
	public JButton btnLeft;
	public JButton btnRight;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameWithOneComboBox frame = new FrameWithOneComboBox();
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
	public FrameWithOneComboBox() {
		setTitle("New Frame With One Combo Box");
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
		
		label1 = new JLabel("Choose CL:");
		sl_panel_1.putConstraint(SpringLayout.NORTH, label1, 10, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, label1, 10, SpringLayout.WEST, panel_1);
		panel_1.add(label1);
		
		comboBox1 = new JComboBox();
		sl_panel_1.putConstraint(SpringLayout.NORTH, comboBox1, 7, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, comboBox1, 6, SpringLayout.EAST, label1);
		sl_panel_1.putConstraint(SpringLayout.EAST, comboBox1, -10, SpringLayout.EAST, panel_1);
		panel_1.add(comboBox1);
	}

}
