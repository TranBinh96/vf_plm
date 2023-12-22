package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import com.teamcenter.rac.common.lov.view.components.LOVDisplayer;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.util.iButton;

public class EScooterUpdateChangeIndexFrame extends JFrame {

	public iButton btnOK;
	public iButton btnCancel;
	public iButton btnPopulateDefault;
	
	public JComboBox<String> comboBox1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EScooterUpdateChangeIndexFrame frame = new EScooterUpdateChangeIndexFrame();
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
	public EScooterUpdateChangeIndexFrame() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Update Change Index");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBackground(SystemColor.inactiveCaptionBorder);

		Dimension fixedSize = new Dimension(400, 224);
		this.setPreferredSize(fixedSize);
		this.setMinimumSize(fixedSize);
		this.setMaximumSize(fixedSize);
		mainPanel.setPreferredSize(new Dimension(394, 191));
		this.setResizable(false);
		
		JPanel populatePanel = new JPanel();
		populatePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
		populatePanel.setBounds(15, 5, 367, 56);
		mainPanel.add(populatePanel);
		populatePanel.setLayout(null);
		
		btnPopulateDefault = new iButton("Populate Default Value");
		btnPopulateDefault.setBounds(12, 13, 343, 30);
		populatePanel.add(btnPopulateDefault);
		populatePanel.setBackground(SystemColor.inactiveCaptionBorder);
		btnPopulateDefault.setVerticalTextPosition(SwingConstants.CENTER);
		
		JPanel overridePanel = new JPanel();
		overridePanel.setBorder(new LineBorder(SystemColor.activeCaption, 2));
		overridePanel.setBounds(15, 65, 367, 120);
		mainPanel.add(overridePanel);
		overridePanel.setLayout(null);
		
		JLabel lblChangeIndex = new JLabel("Override Change Index Value");
		lblChangeIndex.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblChangeIndex.setBounds(12, 13, 264, 16);
		overridePanel.add(lblChangeIndex);
		overridePanel.setBackground(SystemColor.inactiveCaptionBorder);
		
		btnOK = new iButton("OK");
		btnOK.setBounds(87, 82, 90, 30);
		btnOK.setVerticalTextPosition(SwingConstants.CENTER);
		btnOK.setHorizontalTextPosition(SwingConstants.CENTER);
		overridePanel.add(btnOK);
		
		btnCancel = new iButton("Cancel");
		btnCancel.setBounds(189, 82, 90, 30);
		btnCancel.setVerticalTextPosition(SwingConstants.CENTER);
		btnCancel.setHorizontalTextPosition(SwingConstants.CENTER);
		overridePanel.add(btnCancel);
		
		comboBox1 = new JComboBox<String>();
		comboBox1.setBounds(12, 41, 343, 28);
		overridePanel.add(comboBox1);
		
		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
