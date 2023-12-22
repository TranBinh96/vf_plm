package com.teamcenter.integration.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class BOMBOPReport_Dialog extends JFrame {
	private static final long serialVersionUID = -780840355582431947L;
	private JPanel wrapper;
	public JButton btnCreate;
	public JButton btnCancel;
	public JLabel title;
	private JPanel panel;
	public JProgressBar progressBar;
	private JLabel lblNewLabel;
	public JLabel lblShopName;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					BOMBOPReport_Dialog frame = new BOMBOPReport_Dialog();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public BOMBOPReport_Dialog() {
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
		
		lblNewLabel = new JLabel("Shop:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel_2.add(lblNewLabel, gbc_lblNewLabel);
		
		lblShopName = new JLabel("New label");
		GridBagConstraints gbc_lblShopName = new GridBagConstraints();
		gbc_lblShopName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblShopName.gridx = 1;
		gbc_lblShopName.gridy = 1;
		panel_2.add(lblShopName, gbc_lblShopName);
		
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
