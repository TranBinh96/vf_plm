package com.vf.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.UserList;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.vf.utils.Query;

public class VFSearchSORDocDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TCTable bomlineTable = null;

	public VFSearchSORDocDialog() {
		bomlineTable = new TCTable();
	}

	public void createAndShowGUI(TCSession session, TCTable selectedObj) throws Exception {

		setTitle("SOR Document Search Dialog");
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(590, 450));
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		JLabel l_SORNumber = new JLabel("SOR Number:");
		l_SORNumber.setBounds(10, 20, 90, 25);
		panel.add(l_SORNumber);

		iTextField t_SORNumber = new iTextField();
		t_SORNumber.setBounds(110, 20, 200, 25);
		panel.add(t_SORNumber);

		JLabel j_SORName = new JLabel("SOR Name:");
		j_SORName.setBounds(10, 50, 90, 25);
		panel.add(j_SORName);

		iTextField t_SORName = new iTextField();
		t_SORName.setBounds(110, 50, 200, 25);
		panel.add(t_SORName);

		JLabel l_OwningUser = new JLabel("Owing User:");
		l_OwningUser.setBounds(10, 80, 90, 25);
		panel.add(l_OwningUser);

		iComboBox cb_OwingUser = new iComboBox();
		cb_OwingUser.setBounds(110, 80, 200, 25);

		ArrayList<String> listUserNameOS = new ArrayList<>();
		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
		UserList allUser = userType.getUserListByUser("*");

		if (allUser != null && allUser.getUserIds().length > 0) {
			for (int i = 0; i < allUser.getUserIds().length; i++) {
				String userName = allUser.getUserNames()[i];
				String userID = allUser.getUserIds()[i];
				String OSName = userName + " (" + userID + ")";
				listUserNameOS.add(OSName);
			}
		}

		cb_OwingUser.setItems(listUserNameOS);
		cb_OwingUser.setSelectedItem(session.getUser().toString());
		panel.add(cb_OwingUser);

		JScrollPane scrollPane = new JScrollPane(bomlineTable);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 110, 570, 300);

		bomlineTable.setRowSelectionAllowed(true);
		bomlineTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		bomlineTable.setEditable(false);
		bomlineTable.setColumnSelectionAllowed(false);
		bomlineTable.getTableHeader().setResizingAllowed(true);
		t_SORNumber.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handlerVK_ENTER(session, t_SORNumber, t_SORName, cb_OwingUser, e);
				}
			}
		});

		t_SORName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handlerVK_ENTER(session, t_SORNumber, t_SORName, cb_OwingUser, e);
			}
		});

		cb_OwingUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handlerVK_ENTER(session, t_SORNumber, t_SORName, cb_OwingUser, e);
				}
			}

		});

		iButton search = new iButton(new ImageIcon(getClass().getResource("/icons/search.png")));
		search.setBounds(310, 20, 20, 25);
		panel.add(search);
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WaitDialog wait = new WaitDialog();
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						getSORDoc(session, t_SORNumber, t_SORName, cb_OwingUser, wait);
						wait.close();
						return null;
					}
				};
				mySwingWorker.execute();
				wait.makeWait("Loading...", e);
			}
		});

		Icon okIcon = new ImageIcon(getClass().getResource("/icons/validation_16.png"));
		iButton okButton = new iButton(okIcon);
		okButton.setText("OK");
		okButton.setBounds(400, 420, 80, 25);
		panel.add(okButton);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int[] rows = bomlineTable.getSelectedRows();

				for (int row : rows) {
					if (row != -1) {
						// do some action if appropriate column
						AIFComponentContext obj = bomlineTable.dataModel.getRow(row);
						selectedObj.dataModel.removeAllRows();
						selectedObj.addRows(obj);
						selectedObj.repaint();
						selectedObj.revalidate();
					}
				}
				disposeDialog();
			}
		});
		panel.add(scrollPane);
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void buildTable(TCComponent[] objectRow, LinkedHashMap<String, String> columns, TCSession session, TCTable table) {
		String[] attRealName = columns.keySet().toArray(new String[columns.size()]);
		String[] attDisplayName = columns.values().toArray(new String[columns.size()]);

		table.setColumnNames(session, attRealName, "ItemRevision");

		for (int i = 0; i < columns.size(); ++i) {
			table.getColumnModel().getColumn(i).setPreferredWidth(250);
			table.getColumnModel().getColumn(i).setHeaderValue(attDisplayName[i]);
		}
		for (TCComponent row : objectRow) {
			try {
				if (!isItemRevReleased(row)) {
					table.addRows(row);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isItemRevReleased(TCComponent comp) throws TCException {
		String releaseStatuses = comp.getProperty("release_status_list");
		if (releaseStatuses != null && releaseStatuses.length() > 0) {
			return true;
		}
		return false;
	}

	private void getSORDoc(TCSession session, iTextField t_SORNumber, iTextField t_SORName, iComboBox cb_OwingUser, WaitDialog wait) {
		TCComponent[] objects = null;
		String id = t_SORNumber.getText();
		String name = t_SORName.getText();
		String owningUser = cb_OwingUser.getSelectedItem().toString();
		if (id.equals("") && name.equals("") && owningUser.equals("")) {
			setAlwaysOnTop(false);
			wait.close();
			JOptionPane.showMessageDialog(new JFrame(), "Search fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			if (id.equals("")) {
				id = "*";
			}
			if (name.equals("")) {
				name = "*";
			}
			if (owningUser.equals("")) {
				owningUser = "*";
			}
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Item ID", id);
			queryInput.put("Name", name);
			queryInput.put("Owning User", owningUser);
			queryInput.put("Type", "VF3_spec_docRevision;VF3_VES_BSOR_docRevision;VF3_VES_CSOR_docRevision;VF4_SOR_docRevision");
			objects = Query.queryItem(session, queryInput, "Item Revision...");

			if (objects != null) {
				bomlineTable.dataModel.removeAllRows();
				LinkedHashMap<String, String> mapColumn = new LinkedHashMap<>();
				mapColumn.put("object_string", "Name");
				mapColumn.put("owning_user", "Owning User");
				mapColumn.put("object_type", "Type");

				buildTable(objects, mapColumn, session, bomlineTable);
				bomlineTable.revalidate();
				bomlineTable.repaint();

			} else {
				setAlwaysOnTop(false);
				JOptionPane.showMessageDialog(new JFrame(), "No objects found with search criteria.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void handlerVK_ENTER(TCSession session, iTextField t_SORNumber, iTextField t_SORName, iComboBox cb_OwingUser, KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			WaitDialog wait = new WaitDialog();
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					getSORDoc(session, t_SORNumber, t_SORName, cb_OwingUser, wait);
					wait.close();
					return null;
				}
			};
			mySwingWorker.execute();
			Window win = SwingUtilities.getWindowAncestor((Component) e.getSource());
			wait.makeWait("Loading...", win);
		}
	}
}
