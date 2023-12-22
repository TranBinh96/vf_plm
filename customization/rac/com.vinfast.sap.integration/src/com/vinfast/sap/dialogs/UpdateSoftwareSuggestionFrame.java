package com.vinfast.sap.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.teamcenter.rac.kernel.TCComponent;


public class UpdateSoftwareSuggestionFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private JPanel wrapper;
	public JButton btnExec;
	public DefaultListModel<String> modelList = new DefaultListModel<String>();
	private JLabel lblNewLabel;
	public JButton btnClose;
	private JTextField txtSUbGroupID;


	private JLabel lblSwBom;
	private JTextField txtSWBOMID;
	public JButton btnUpdateSubGroup;
	public JButton btnUpdateSWBOM;
	public TCComponent SWBOM;
	public TCComponent subGroup;
	private JProgressBar progressBar;
	
	public TCComponent getSWBOM() {
		return SWBOM;
	}
	public JTextField getTxtSUbGroupID() {
		return txtSUbGroupID;
	}

	public JTextField getTxtSWBOMID() {
		return txtSWBOMID;
	}
	public void setSWBOM(TCComponent sWBOM) {
		SWBOM = sWBOM;
	}

	public TCComponent getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(TCComponent subGroup) {
		this.subGroup = subGroup;
	}

	public boolean isFilledInput() {
		return !this.txtSUbGroupID.getText().isEmpty() && !this.txtSWBOMID.getText().isEmpty();
	}
	
	public void setSWBOMID(String id) {
		this.txtSWBOMID.setText(id);
	}
	
	public void setSubGroupID(String id) {
		this.txtSUbGroupID.setText(id);
	}

	public UpdateSoftwareSuggestionFrame() {
		setResizable(false);
		setTitle("MBOM & SWBOM Software info matching status");
		setBounds(100, 100, 527, 206);
		wrapper = new JPanel();
		wrapper.setBackground(Color.WHITE);
		wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(wrapper);
		wrapper.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Setting", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_2.setBackground(Color.WHITE);
		wrapper.add(panel_2, BorderLayout.NORTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] {30, 30, 0};
		gbl_panel_2.rowHeights = new int[] {22, 30};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_panel_2.rowWeights = new double[]{0.0};
		panel_2.setLayout(gbl_panel_2);
		
		lblNewLabel = new JLabel("Sub Group:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_2.add(lblNewLabel, gbc_lblNewLabel);
		
		txtSUbGroupID = new JTextField();
		GridBagConstraints gbc_txtSUbGroupID = new GridBagConstraints();
		gbc_txtSUbGroupID.insets = new Insets(0, 0, 5, 5);
		gbc_txtSUbGroupID.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSUbGroupID.gridx = 1;
		gbc_txtSUbGroupID.gridy = 0;
		panel_2.add(txtSUbGroupID, gbc_txtSUbGroupID);
		txtSUbGroupID.setColumns(10);
		
		btnUpdateSubGroup = new JButton("Update");
		GridBagConstraints gbc_btnUpdateSubGroup = new GridBagConstraints();
		gbc_btnUpdateSubGroup.insets = new Insets(0, 0, 5, 0);
		gbc_btnUpdateSubGroup.gridx = 2;
		gbc_btnUpdateSubGroup.gridy = 0;
		panel_2.add(btnUpdateSubGroup, gbc_btnUpdateSubGroup);
		
		lblSwBom = new JLabel("SW BOM");
		GridBagConstraints gbc_lblSwBom = new GridBagConstraints();
		gbc_lblSwBom.anchor = GridBagConstraints.EAST;
		gbc_lblSwBom.insets = new Insets(0, 0, 0, 5);
		gbc_lblSwBom.gridx = 0;
		gbc_lblSwBom.gridy = 1;
		panel_2.add(lblSwBom, gbc_lblSwBom);
		
		txtSWBOMID = new JTextField();
		GridBagConstraints gbc_txtSWBOMID = new GridBagConstraints();
		gbc_txtSWBOMID.insets = new Insets(0, 0, 0, 5);
		gbc_txtSWBOMID.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSWBOMID.gridx = 1;
		gbc_txtSWBOMID.gridy = 1;
		panel_2.add(txtSWBOMID, gbc_txtSWBOMID);
		txtSWBOMID.setColumns(10);
		
		btnUpdateSWBOM = new JButton("Update");
		GridBagConstraints gbc_btnUpdateSWBOM = new GridBagConstraints();
		gbc_btnUpdateSWBOM.gridx = 2;
		gbc_btnUpdateSWBOM.gridy = 1;
		panel_2.add(btnUpdateSWBOM, gbc_btnUpdateSWBOM);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_1.setBackground(Color.WHITE);
		wrapper.add(panel_1, BorderLayout.SOUTH);
		
		btnClose = new JButton("Cancel");
		btnClose.setPreferredSize(new Dimension(100, 30));
		panel_1.add(btnClose);
		
		btnExec = new JButton("Validate");
		btnExec.setBackground(Color.WHITE);
		btnExec.setPreferredSize(new Dimension(100, 30));
		panel_1.add(btnExec);
		
		progressBar = new JProgressBar(0,100);
		progressBar.setStringPainted(true);
		wrapper.add(progressBar, BorderLayout.CENTER);
	}
	
	public void setProcessPercent(int percent, String detail) {
		this.progressBar.setValue(percent);
		this.progressBar.setString(String.format("%s - %s",Integer.toString(percent)+"%", detail));
	}
	

}
