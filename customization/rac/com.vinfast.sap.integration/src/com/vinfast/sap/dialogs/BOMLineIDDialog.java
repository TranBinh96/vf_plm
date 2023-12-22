package com.vinfast.sap.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCSession;

public class BOMLineIDDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JCheckBox childLines = null;
	public JCheckBox resetLines = null;
	public JCheckBox selectedLines = null;
	public JButton JB_Save = null;
	public JButton JB_Cancel = null;
	public TCTable bomlineTable = null;

	public InterfaceAIFComponent[] showTableLines = null;

	public void createAndShowGUI(TCSession session,InterfaceAIFComponent[] Lines ) throws Exception {

		showTableLines = Lines;
		setTitle("BOMLine ID Dialog");
		JPanel panel = new JPanel();
		childLines = new JCheckBox("Child Line(s)");
		resetLines = new JCheckBox("Reset Line(s)");  
		selectedLines = new JCheckBox("Selected Line(s)");  

		JSeparator separator =  new JSeparator();

		JB_Save =  new JButton("Generate");
		JB_Cancel =  new JButton("Close");

		ImageIcon  frame_Icon = new ImageIcon(getClass().getResource("/icons/Bomline_16.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(450, 400));


		bomlineTable =  new TCTable();
		JScrollPane scrollPane = new JScrollPane(bomlineTable);
		scrollPane.setBounds(5, 5, 430, 300);
		bomlineTable.setEditable(false);
		bomlineTable.setColumnSelectionAllowed(false);
		String[] tableAttr = new String[] {"object_string","VF4_bomline_id"}; 
		bomlineTable.setColumnNames(session, tableAttr,"BOMLine");
		bomlineTable.getColumnModel().getColumn(0).setPreferredWidth(347);
		bomlineTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		bomlineTable.getColumnModel().getColumn(0).setHeaderValue("BOM Line");
		bomlineTable.getColumnModel().getColumn(1).setHeaderValue("BOMLineID");

		for(InterfaceAIFComponent line : Lines) {
			bomlineTable.addRows(line);
		}
		resetLines.setBounds(70, 310, 130, 25);
		resetLines.setBackground(Color.white);

		selectedLines.setBounds(200, 310, 130, 25);
		selectedLines.setBackground(Color.white);
		selectedLines.setSelected(true);

		childLines.setBounds(340, 310, 100, 25);
		childLines.setBackground(Color.white);

		separator.setBounds(5, 340, 440, 25);

		JB_Save.setIcon(save_Icon);
		JB_Save.setBounds(100, 350, 110, 25);

		JB_Cancel.setIcon(cancel_Icon);
		JB_Cancel.setBounds(220, 350, 90, 25);
		JB_Cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		});

		panel.add(resetLines);
		panel.add(selectedLines);
		panel.add(childLines);
		panel.add(scrollPane);
		panel.add(separator);
		panel.add(JB_Save);
		panel.add(JB_Cancel);


		bomlineTable.revalidate();
		bomlineTable.repaint();
		getContentPane().add(panel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setAlwaysOnTop(true);
	}

}
