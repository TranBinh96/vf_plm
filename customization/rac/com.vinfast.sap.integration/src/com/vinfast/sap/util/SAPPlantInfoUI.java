package com.vinfast.sap.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.combobox.iComboBox;

public class SAPPlantInfoUI extends AbstractAIFDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3819356246443611198L;
	TCSession session = null ;
	TCComponentBOMLine bomline = null ;

	iComboBox JCB_Make_Buy = null;
	iComboBox JCB_PPAP = null;
	iComboBox JCB_Plant = null;
	iComboBox JCB_pur_Ind = null;
	iComboBox jcb_AppCode = null;

	HashMap<String, String[]> approval_Codes = null;

	public SAPPlantInfoUI(){

	}


	public JButton createSAPDialog(HashMap<String, String[]> Codes){
		approval_Codes = Codes;
		setTitle("Material Transfer");
		JPanel panel = new JPanel();
		ImageIcon  frame_Icon = new ImageIcon(getClass().getResource("/icons/Material_16.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(380, 230));
		
		
		JLabel plant_label = new JLabel("Plant");
		plant_label.setBounds(20, 20, 100, 25);
		JCB_Plant = new iComboBox(approval_Codes.keySet().toArray());
		JCB_Plant.setMandatory(true);
		JCB_Plant.setBounds(130, 20, 220, 25);
		JCB_Plant.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				String selected_Plant = JCB_Plant.getSelectedItem().toString();
				
				if(selected_Plant.equals("") == false){
					String[] prop_values = approval_Codes.get(selected_Plant);
					jcb_AppCode.setSelectedItem(prop_values[1]);
					JCB_Make_Buy.setSelectedItem(prop_values[2]);
					JCB_PPAP.setSelectedItem(prop_values[3]);
					JCB_pur_Ind.setSelectedItem(prop_values[4]);
				}

			}

		});
		panel.add(plant_label);
		panel.add(JCB_Plant);
		
		JLabel code = new JLabel("Approval Code");
		code.setBounds(20, 50, 100, 25);

		jcb_AppCode = new iComboBox(new String[]{"Interior","Exterior"});
		jcb_AppCode.setBounds(130, 50, 220, 25);
		jcb_AppCode.setMandatory(true);
		jcb_AppCode.setEditable(false);
		jcb_AppCode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {


			}

		});
		panel.add(code);
		panel.add(jcb_AppCode);

		JLabel make_buy = new JLabel("Make/Buy");
		make_buy.setBounds(20, 80, 100, 25);
		JCB_Make_Buy = new iComboBox();
		JCB_Make_Buy.setMandatory(true);
		JCB_Make_Buy.setEditable(false);
		JCB_Make_Buy.setBounds(130, 80, 220, 25);
		JCB_Make_Buy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {


			}

		});

		panel.add(make_buy);
		panel.add(JCB_Make_Buy);

		JLabel PPAP = new JLabel("PPAP");
		PPAP.setBounds(20, 110, 100, 25);
		JCB_PPAP = new iComboBox();
		JCB_PPAP.setMandatory(true);
		JCB_PPAP.setEditable(false);
		JCB_PPAP.setBounds(130, 110, 220, 25);
		JCB_PPAP.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {


			}

		});
		panel.add(PPAP);
		panel.add(JCB_PPAP);

		

		JLabel pur_indi = new JLabel("Purchase Indicator");
		pur_indi.setBounds(20, 140, 100, 25);
		JCB_pur_Ind = new iComboBox();
		JCB_pur_Ind.setMandatory(true);
		JCB_pur_Ind.setEditable(false);
		JCB_pur_Ind.setBounds(130, 140, 220, 25);
		panel.add(pur_indi);
		panel.add(JCB_pur_Ind);

		JCB_pur_Ind.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {


			}

		});

		JSeparator separator =  new JSeparator();
		separator.setBounds(10, 180, 380, 25);

		JButton JB_Save =  new JButton("Transfer");
		JB_Save.setIcon(save_Icon);
		JB_Save.setBounds(100, 190, 100, 25);

		JButton JB_Cancel =  new JButton("Cancel");
		JB_Cancel.setIcon(cancel_Icon);
		JB_Cancel.setBounds(210, 190, 100, 25);
		JB_Cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		});

		panel.add(separator);
		panel.add(JB_Save);
		panel.add(JB_Cancel);

		getContentPane().add(panel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		return JB_Save;
	}
}
