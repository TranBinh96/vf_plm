package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import net.miginfocom.swing.MigLayout;

public class MHUReportDialog extends JFrame {
	private JPanel contentPane;
	public JButton btnLeft;
	public JButton btnRight;
	public JLabel label1;
	public JRadioButton rdbtnShowRefMPN;
	public JRadioButton rdbtnShowAllMPN;
	public JProgressBar progressBar;

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
	
	public MHUReportDialog() {
		setTitle("MHU Report");
		setBounds(100, 100, 434, 182);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[190px][5px][195px]", "[24px][25px][24px][40px]"));
		
		label1 = new JLabel("New label");
		contentPane.add(label1, "cell 0 0 3 1,grow");
		
		rdbtnShowRefMPN = new JRadioButton("Show Ref MPN");
		contentPane.add(rdbtnShowRefMPN, "cell 0 1,grow");
		
		rdbtnShowAllMPN = new JRadioButton("Show All MPN");
		contentPane.add(rdbtnShowAllMPN, "cell 2 1,grow");
		
		progressBar = new JProgressBar();
		contentPane.add(progressBar, "cell 0 2 3 1,grow");
		
		btnLeft = new JButton("New button");
		contentPane.add(btnLeft, "cell 0 3,grow");
		btnLeft.setPreferredSize(new Dimension(150, 40));
		
		btnRight = new JButton("New button");
		contentPane.add(btnRight, "cell 2 3,grow");
		btnRight.setPreferredSize(new Dimension(150, 40));
		
	}
}
