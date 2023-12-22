package com.teamcenter.vinfast.handlers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.vf.utils.Query;

public class MBOMPartSearchDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String queryType;
	private static String selectedItemID;

	public void createAndShowGUI(TCSession session, Table table, List<String> idList, String _type) throws Exception {
		queryType = _type;
		setTitle("Search Dialog");
		InterfaceAIFComponent object = AIFUtility.getCurrentApplication().getTargetComponent();
		selectedItemID = object.getProperty("item_id");
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(400, 330));

		JLabel idSearch = new JLabel("Part Number:");
		idSearch.setBounds(10, 20, 110, 25);
		panel.add(idSearch);

		iTextField idBox = new iTextField();
		idBox.setBounds(120, 20, 160, 25);
		panel.add(idBox);
		idBox.setToolTipText("Enter specific ID. Only 100 objects will be displayed based on search input.");

		// Commented by Rafi 05-Aug-2022 - Ticket 228865
		// We no longer search with revision, only search with Item ID

		/*
		 * JLabel revSearch = new JLabel("Revision:"); revSearch.setBounds(10, 50, 110,
		 * 25); panel.add(revSearch);
		 * 
		 * iTextField revBox = new iTextField(); revBox.setBounds(120, 50, 160, 25);
		 * panel.add(revBox); revBox.setEnabled(false);
		 */

		TCTable bomlineTable = new TCTable();
		JScrollPane scrollPane = new JScrollPane(bomlineTable);
		scrollPane.setBounds(10, 50, 380, 230);
		bomlineTable.setEditable(false);
		bomlineTable.setColumnSelectionAllowed(false);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		bomlineTable.setRowSelectionAllowed(true);
		bomlineTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		ListSelectionModel selectionModel = bomlineTable.getSelectionModel();

		iButton search = new iButton(new ImageIcon(getClass().getResource("/icons/search.png")));
		search.setBounds(290, 20, 25, 25);
		panel.add(search);
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction(session, idBox, bomlineTable);
			}
		});
		search.setEnabled(false);
		search.setToolTipText("Only 100 objects will be shown.");

		Icon okIcon = new ImageIcon(getClass().getResource("/icons/validation_16.png"));
		iButton okButton = new iButton(okIcon);
		okButton.setText("Ok");
		okButton.setBounds(270, 300, 80, 25);
		panel.add(okButton);
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = bomlineTable.getSelectedRows();
				if (rows.length != 0) {
					for (int i = 0; i < rows.length; i++) {
						if (rows[i] != -1) {
							AIFComponentContext obj = bomlineTable.dataModel.getRow(rows[i]);
							final TCComponentItem item = (TCComponentItem) obj.getComponent();
							try {
								String id = item.getProperty("item_id");
								if (idList.contains(id) == true) {
									MessageBox.post("Part Number already added. Please choose different Part Number.", "Duplicate", MessageBox.ERROR);
									return;
								} else if (selectedItemID.equalsIgnoreCase(id)) {
									MessageBox.post("Part Number cannot be same as selected object. Please choose different Part Number.", "Duplicate", MessageBox.ERROR);
									return;
								} else {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											TableItem tmpTable = new TableItem(table, SWT.NONE);
											tmpTable.setText(id);
											tmpTable.setData(item);
											idList.add(id);
										}
									});

									disposeDialog();
								}
							} catch (TCException exception) {
								exception.printStackTrace();
							}
						}
					}
				}
			}
		});

		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String strSource = e.getSource().toString();
				int start = strSource.indexOf("{") + 1, stop = strSource.length() - 1;
				String valid = strSource.substring(start, stop);
				if (valid.length() != 0) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		});

		idBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (idBox.getText().length() != 0) {
					search.setEnabled(true);
				} else {
					search.setEnabled(false);
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchAction(session, idBox, bomlineTable);
				}
			}
		});

		panel.add(scrollPane);
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public TCComponent[] searchObjects(TCSession session, String[] entires, String[] values, String query_name) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput findQuery = new FindSavedQueriesCriteriaInput();
			findQuery.queryNames = new String[] { query_name };
			findQuery.queryDescs = new String[] { "" };
			findQuery.queryType = 0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] { findQuery });
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput savedQuery = new SavedQueryInput();

			savedQuery.query = (TCComponentQuery) result[0];
			savedQuery.entries = entires;
			savedQuery.values = values;
			savedQuery.maxNumToReturn = 100;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(new SavedQueryInput[] { savedQuery });

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return objects;
	}

	public void searchAction(TCSession session, iTextField iDField, TCTable table) {
		TCComponent[] objects = null;
		String ID = iDField.getText();

		if (ID.equals("")) {
			MessageBox.post("Search fields cannot be empty.", "Invalid...", MessageBox.ERROR);
			return;
		}

		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Item ID", ID);
		inputQuery.put("Type", queryType);
		objects = Query.queryItem(session, inputQuery, "Item...");

		if (objects != null) {
			table.clear();
			String[] tableAttr = new String[] { "object_string", "object_type" };
			table.setColumnNames(session, tableAttr, "ItemRevision");
			table.getColumnModel().getColumn(0).setPreferredWidth(270);
			table.getColumnModel().getColumn(1).setPreferredWidth(110);
			table.getColumnModel().getColumn(0).setHeaderValue("Object");
			table.getColumnModel().getColumn(1).setHeaderValue("Type");

			for (TCComponent line : objects) {

				TCComponent item = (TCComponent) line;
				table.addRows(item);

			}
			table.revalidate();
			table.repaint();
		} else {
			MessageBox.post("No objects found with search criteria.", "Error...", MessageBox.ERROR);
		}
	}
}
