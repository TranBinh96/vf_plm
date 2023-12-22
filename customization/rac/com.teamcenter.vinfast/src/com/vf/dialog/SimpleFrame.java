package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SimpleFrame extends JFrame{
	private JPanel contentPane;
	public JLabel label1;
	public JButton btnLeft;
	public JButton btnRight;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimpleFrame frame = new SimpleFrame();
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
	public SimpleFrame() {
		setTitle("Target Cost Report");
		setBounds(100, 100, 428, 157);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		
		btnLeft = new JButton("New button");
//		btnLeft.setHorizontalAlignment(SwingConstants.RIGHT);
		btnLeft.setPreferredSize(new Dimension(150, 40));
		panel.add(btnLeft);
		
		btnRight = new JButton("New button");
		btnRight.setPreferredSize(new Dimension(150, 40));
		panel.add(btnRight);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);
		
		label1 = new JLabel("New Label:");
		sl_panel_1.putConstraint(SpringLayout.NORTH, label1, 10, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, label1, 10, SpringLayout.WEST, panel_1);
		panel_1.add(label1);
		
	}
}
