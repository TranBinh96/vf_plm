package com.teamcenter.vinfast.subdialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.swt.widgets.Display;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentQuery;
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

public class RequestTranferMMSearchDialg extends AbstractAIFDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Runnable postAction;

	public void createAndShowGUI(TCSession session, Set<TCComponent> outputItems, Runnable postAction) throws Exception {
		outputItems.clear();
		this.postAction = postAction;
		
		setTitle("Search Dialog");
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(520, 330));

		JLabel idSearch =  new JLabel("Part Number:");
		idSearch.setBounds(10, 20, 110, 25);
		panel.add(idSearch);

		iTextField idBox = new iTextField();
		idBox.setBounds(120, 20, 160, 25);
		panel.add(idBox);
		idBox.setToolTipText("Enter specific ID. Only 100 objects will be displayed based on search input.");
		
		JLabel revSearch =  new JLabel("Revision:");
		revSearch.setBounds(10, 50, 110, 25);
		panel.add(revSearch);

		iTextField revBox = new iTextField();
		revBox.setBounds(120, 50, 160, 25);
		panel.add(revBox);
		revBox.setEnabled(false);
		

		TCTable bomlineTable =  new TCTable();
		JScrollPane scrollPane = new JScrollPane(bomlineTable);
		scrollPane.setBounds(10, 80, 500, 200);
		bomlineTable.setEditable(false);
		bomlineTable.setColumnSelectionAllowed(false);
		bomlineTable.setRowSelectionAllowed(true);
		bomlineTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		ListSelectionModel selectionModel = bomlineTable.getSelectionModel();

		iButton search = new iButton(new ImageIcon(getClass().getResource("/icons/search.png")));
		search.setBounds(290, 20, 25, 25);
		panel.add(search);
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				searchAction(session, idBox, revBox, bomlineTable);
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

				if(rows.length != 0) {

					for(int i=0 ; i<rows.length ; i++) {

						if (rows[i] != -1) {
							AIFComponentContext obj = bomlineTable.dataModel.getRow(rows[i]);
							final TCComponentItem item = (TCComponentItem) obj.getComponent();
							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									outputItems.add(item);
								}
							});
							
							disposeDialog();
						}
					}
					Display.getDefault().asyncExec(postAction);
				}
			}
		});
		
		selectionModel.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {

		        String strSource= e.getSource().toString();
		        int start = strSource.indexOf("{")+1,stop  = strSource.length()-1;
		        String valid = strSource.substring(start, stop);
		        if(valid.length() !=0) {
		        	okButton.setEnabled(true);
		        }else {
		        	okButton.setEnabled(false);
		        }
		    }
		});
		
		idBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				
				if(idBox.getText().length() !=0) {
					revBox.setEnabled(true);
					search.setEnabled(true);
				}else {
					revBox.setText("");
					revBox.setEnabled(false);
					search.setEnabled(false);
				}
				
				if(e.getKeyCode() == KeyEvent.VK_ENTER){

					searchAction(session, idBox, revBox, bomlineTable);
				}
			}
		});

		revBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				if(e.getKeyCode() == KeyEvent.VK_ENTER){

					searchAction(session, idBox, revBox, bomlineTable);
				}
			}

		});

		panel.add(scrollPane);
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public TCComponent[] searchObjects(TCSession session, String[] entires,String[] values, String query_name )
	{
		TCComponent[] objects = null ;
		try
		{
			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput findQuery = new  FindSavedQueriesCriteriaInput();
			findQuery.queryNames= new String[] {query_name};
			findQuery.queryDescs= new String[] {""};
			findQuery.queryType=0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] {findQuery});
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput savedQuery = new SavedQueryInput();

			savedQuery.query = (TCComponentQuery) result[0];
			savedQuery.entries = entires;
			savedQuery.values = values;
			savedQuery.maxNumToReturn = 100;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(new SavedQueryInput[] {savedQuery});

			SavedQueryResults[] results = responce.arrayOfResults;

			if(results[0].numOfObjects!=0){

				objects = results[0].objects ;
			}
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return objects ;
	}

	public void searchAction(TCSession session, iTextField iDField, iTextField revField, TCTable table) {

		TCComponent[] objects = null;
		String ID = iDField.getText();
		String Name = revField.getText();

		if(ID.equals("") && Name.equals("")) {

			MessageBox.post("Search fields cannot be empty.","Invalid...", MessageBox.ERROR);

		}else {

			if(ID.equals("")) {
				ID = "*";
			}
			if(Name.equals("")) {
				Name = "*";
			}

			String[] entries = new String[] {"Item ID"};
			String[] values = new String[] {ID};
			objects = searchObjects(session, entries, values, "__Admin_Old_Part_Number");

			if(objects != null) {

				int rowCount = table.getRowCount();
				String[] tableAttr = new String[] {"object_string","object_type", "vf4_is_transferred_erp"}; 
				table.setColumnNames(session, tableAttr,"ItemRevision");
				table.getColumnModel().getColumn(0).setPreferredWidth(250);
				table.getColumnModel().getColumn(1).setPreferredWidth(100);
				table.getColumnModel().getColumn(2).setPreferredWidth(130);
				table.getColumnModel().getColumn(0).setHeaderValue("Object");
				table.getColumnModel().getColumn(1).setHeaderValue("Type");
				table.getColumnModel().getColumn(2).setHeaderValue("Is Transferred EPR");

				for(TCComponent line : objects) {

					TCComponent item = (TCComponent)line;
					table.addRows(item);

				}
				table.revalidate();
				table.repaint();

			}else {

				MessageBox.post("No objects found with search criteria.", "Error...", MessageBox.ERROR);
			}
		}
	}
}
