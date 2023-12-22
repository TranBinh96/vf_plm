package com.teamcenter.vinfast.report.eecomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

public class EEComponentBomReport_Dialog extends JFrame {
	private static final long serialVersionUID = -780840355582431947L;
	private JPanel wrapper;
	public JButton btnCreate;
	public JButton btnCancel;
	public JLabel title;
	public JRadioButton rbtNoExpand;
	public JRadioButton rbtExpand;
	private JPanel panel;
	public JProgressBar progressBar;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EEComponentBomReport_Dialog frame = new EEComponentBomReport_Dialog();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public EEComponentBomReport_Dialog() {
		setResizable(false);
		setTitle("EEBOM Report");
		setBounds(100, 100, 300, 200);
		wrapper = new JPanel();
		wrapper.setBackground(Color.WHITE);
		wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(wrapper);
		wrapper.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_1.setBackground(Color.WHITE);
		wrapper.add(panel_1, BorderLayout.SOUTH);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBackground(Color.WHITE);
		btnCancel.setPreferredSize(new Dimension(100, 30));
		panel_1.add(btnCancel);

		btnCreate = new JButton("Export");
		btnCreate.setBackground(Color.WHITE);
		btnCreate.setPreferredSize(new Dimension(100, 30));
		panel_1.add(btnCreate);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBackground(Color.WHITE);
		wrapper.add(panel_2, BorderLayout.NORTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] {30, 30};
		gbl_panel_2.rowHeights = new int[] {25, 25};
		gbl_panel_2.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0};
		panel_2.setLayout(gbl_panel_2);
		
		title = new JLabel("");
		GridBagConstraints gbc_title = new GridBagConstraints();
		gbc_title.gridwidth = 2;
		gbc_title.fill = GridBagConstraints.BOTH;
		gbc_title.insets = new Insets(0, 0, 5, 0);
		gbc_title.gridx = 0;
		gbc_title.gridy = 0;
		panel_2.add(title, gbc_title);
		
		rbtNoExpand = new JRadioButton("Packed");
		rbtNoExpand.setBackground(Color.WHITE);
		GridBagConstraints gbc_rbtNoExpand = new GridBagConstraints();
		gbc_rbtNoExpand.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbtNoExpand.insets = new Insets(0, 0, 5, 5);
		gbc_rbtNoExpand.gridx = 0;
		gbc_rbtNoExpand.gridy = 1;
		panel_2.add(rbtNoExpand, gbc_rbtNoExpand);
		
		rbtExpand = new JRadioButton("Unpacked");
		rbtExpand.setBackground(Color.WHITE);
		GridBagConstraints gbc_rbtExpand = new GridBagConstraints();
		gbc_rbtExpand.insets = new Insets(0, 0, 5, 0);
		gbc_rbtExpand.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbtExpand.gridx = 1;
		gbc_rbtExpand.gridy = 1;
		panel_2.add(rbtExpand, gbc_rbtExpand);
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		wrapper.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {30};
		gbl_panel.rowHeights = new int[] {14};
		gbl_panel.columnWeights = new double[]{0.0, 1.0};
		gbl_panel.rowWeights = new double[]{0.0};
		panel.setLayout(gbl_panel);
		
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(100, 25));
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.gridwidth = 2;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.anchor = GridBagConstraints.NORTH;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 0;
		panel.add(progressBar, gbc_progressBar);
	}
}
