package com.teamcenter.vinfast.handlers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentACLType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentRoleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class ESourcingEditDialog {

	TCSession session = null ;
	InterfaceAIFComponent[] interfaceAIFComp = null ;
	int tableCol_count =0;
	JTable table = null;
	Date tempDate = null ;
	String pattern = "dd-MMM-yyyy";
	SimpleDateFormat format = new SimpleDateFormat(pattern);
	String tempValue= null;
	TCPreferenceService prefService = null;
	JFrame frame = null;
	Hashtable<String , TCComponentItemRevision> src_items = null ;
	JButton editButton = null;
	int rowindex = 0;
	int colindex = 0;
	Object copyCol = "";

	public ESourcingEditDialog(TCSession theSession, InterfaceAIFComponent[] targetComp) {
		// TODO Auto-generated constructor stub

		session = theSession ;
		interfaceAIFComp  = targetComp ;

	}

	public void createDialog()
	{
		try
		{
			createAndShowGUI();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void createAndShowGUI() throws Exception {
		// Figure out what directory to display
		prefService =  session.getPreferenceService();

		Vector<Object> staticVector = createTableVector(interfaceAIFComp);

		// Create a TableModel object to represent the contents of the directory
		VFTableModel tableModel = new VFTableModel(staticVector);

		// Create a JTable and tell it to display our model
		table = new JTable(tableModel);
		table.setRowHeight(table.getRowHeight()+3);

		/*table.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent me)
			{

				int row = table.rowAtPoint(me.getPoint());
				int col = table.columnAtPoint(me.getPoint());

				table.clearSelection();
				table.addRowSelectionInterval(row,row);
				table.addColumnSelectionInterval(col,col);
				if (col < 7){
					return;
				}
				if(SwingUtilities.isRightMouseButton(me) == true)
				{

					if (me.getComponent() instanceof JTable ) {
						JPopupMenu popup = rightClickOperation(row,col);
						//table.setComponentPopupMenu(popup);
						popup.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			}
		});*/

		//Make header names to center
		TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(JLabel.CENTER);

		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("SansSerif", Font.BOLD, 12));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(true);

		final TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
		header.setDefaultRenderer(new TableCellRenderer() {

			private JLabel lbl;

			@Override
			public Component getTableCellRendererComponent(
					JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				lbl = (JLabel) hr.getTableCellRendererComponent(table, value, true, true, row, col);

				if (session.getCurrentRole().toString().equals("CL"))
				{
					if (col ==16||col ==17||col ==22||col ==23||col ==24||col ==27||col ==29||col ==31||
							col ==33||col ==35||col ==37||col ==40||col ==56||col ==58||col ==59||col ==60||col ==61) {
						lbl.setForeground(Color.RED);
						//lbl.setBackground(Color.white);
					} else {
						lbl.setForeground(Color.BLACK);
						//lbl.setBackground(Color.white);
					}
				}
				else
				{
					//15,18,19,20,25,26,28,30,32,34,36,38,39,41 to 55,57,
					
					if (col ==15||col ==18||col ==19||col ==20||col ==25||col ==26||col ==28||col ==30||col ==32
							||col ==34||col ==36||col ==38||col ==39||col ==41||col ==42||col ==43||col ==44||col ==45
							||col ==46||col ==47||col ==48||col ==49||col ==50||col ==51||col ==52||col ==53||col ==54
							||col ==55||col ==57) {
						lbl.setForeground(Color.RED);
						//lbl.setBackground(Color.white);
					} else {
						lbl.setForeground(Color.BLACK);
						//lbl.setBackground(Color.white);
					}
				}
				return lbl;
			}
		});

		table.setDefaultRenderer(Object.class, new TableCellRenderer()
		{
			private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (row % 2 == 0) {
					c.setBackground(Color.WHITE);//.decode("#99CCFF"));
				} else {
					c.setBackground(Color.decode("#E0E0E0"));
				}
				c.setForeground(Color.BLACK);
				return c;
			}

		});

		new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) ;
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_);
		TableColumn column = null;
		tableCol_count = table.getColumnModel().getColumnCount();

		for (int i = 0; i < tableCol_count; i++) {

			if(i==0){
				column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(30);
			}else if(i==1){
				column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(120);
			}else if(i==2){
				column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(180);
			}else{
				column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(150);
			}
		}  
		// Put the JTable in a JScrollPane to handle scrolling
		JScrollPane tableScrollPane = new JScrollPane(table);
		//tableScrollPane.ver
		cellEditor();
		tableScrollPane.setPreferredSize(new Dimension(1024, 500));

		// Create an action listener to display the given directory
		Icon collapse_Icon = new ImageIcon(getClass().getResource("/icons/Collapse_16.png"));
		Icon expand_Icon = new ImageIcon(getClass().getResource("/icons/Expand_16.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon edit_Icon = new ImageIcon(getClass().getResource("/icons/edit_16.png"));
		ImageIcon  frame_Icon = new ImageIcon(getClass().getResource("/icons/LineObj_16.png"));

		JButton expandButton = new JButton(expand_Icon);
		expandButton.setPreferredSize(new Dimension(25, 25));
		expandButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showHideColumn(true);
			}

		});

		JButton collpaseButton = new JButton(collapse_Icon);
		collpaseButton.setPreferredSize(new Dimension(25, 25));
		collpaseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showHideColumn(false);
			}

		});

		editButton = new JButton(edit_Icon);
		editButton.setText("Edit");
		editButton.setPreferredSize(new Dimension(80, 25));
		editButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				table.setEnabled(true);
				editButton.setEnabled(false);

			}
		});

		JButton saveButton = new JButton(save_Icon);
		saveButton.setText("Save");
		saveButton.setPreferredSize(new Dimension(80, 25));
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				if(vaildCheck())
				{
					saveLineItem();
					JOptionPane.showMessageDialog(null,"Source Part(s) updated Successfully");
					frame.dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null,"Field is empty");                                                                            
				}
			}
		});

		JPanel ctrlPane = new JPanel(); 
		ctrlPane.add(expandButton);
		ctrlPane.add(collpaseButton);
		ctrlPane.add(editButton);
		ctrlPane.add(saveButton);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ctrlPane, tableScrollPane);
		splitPane.setDividerLocation(30);
		splitPane.setEnabled(false);

		// Display it all in a scrolling window and make the window appear
		frame = new JFrame("Source Part");
		frame.setIconImage(frame_Icon.getImage());
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(splitPane);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setEnabled(false);
		//frame.add(new JScrollPane(table));
		frame.setSize(800, 500);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public ArrayList<TCComponentItemRevision> filterLineItemRevisions(InterfaceAIFComponent[] targetComp){

		ArrayList<TCComponentItemRevision> lineItems = new ArrayList<TCComponentItemRevision>();
		for(int i=0; i<targetComp.length ; i++){
			if(targetComp[i].getType().equals("VF4_line_itemRevision")){

				TCComponentItemRevision lineItem = (TCComponentItemRevision)targetComp[i];
				lineItems.add(lineItem);
			}
		}
		return lineItems;
	}

	public void showHideColumn(boolean visible){

		int width=0;
		int namewidth=0;
		if(visible == true){
			width = 150;
			namewidth=180;
		}
		for(int i=2;i<15;i++){

			if(i==2){
				table.getColumnModel().getColumn(i).setMinWidth(namewidth);
				table.getColumnModel().getColumn(i).setMaxWidth(namewidth);
				table.getColumnModel().getColumn(i).setWidth(namewidth);
			}else{
				table.getColumnModel().getColumn(i).setMinWidth(width);
				table.getColumnModel().getColumn(i).setMaxWidth(width);
				table.getColumnModel().getColumn(i).setWidth(width);
			}
		}
	}

	public void cellEditor(){

		String[] regions = prefService.getStringValues("VF_Sourcing_Region");
		String[] buyer_users = {};
		String[] CL_users = {};
		//String user = session.getUser().toString();
		try {

			TCComponentGroupType groupType =  (TCComponentGroupType)session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType)session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
			TCComponentGroup group = groupType.find("Purchase");

			TCComponentRole role = roleType.find("Buyer");
			TCComponentRole seniorrole = roleType.find("Senior Buyer");
			TCComponentGroupMember[] buyer_groupMem = GMType.findByRole(role, group);
			TCComponentGroupMember[] seniorbuyer_groupMem = GMType.findByRole(seniorrole, group);

			TCComponentRole CL_role = roleType.find("CL");
			TCComponentRole purchaseCL_role = roleType.find("Purchasing Coordinator");
			TCComponentGroupMember[] CL_groupMem = GMType.findByRole(CL_role, group);
			TCComponentGroupMember[] purchaseCL_groupMem = GMType.findByRole(purchaseCL_role, group);
			buyer_users =  new String[buyer_groupMem.length+seniorbuyer_groupMem.length];
			int buyers_count = 0;
			for(int i=0;i<buyer_groupMem.length;i++){

				buyer_users[i]= buyer_groupMem[i].getUser().toString();
				buyers_count++;
			}
			for(int i=0;i<seniorbuyer_groupMem.length;i++){

				buyer_users[buyers_count]= seniorbuyer_groupMem[i].getUser().toString();
				buyers_count++;
			}

			CL_users =  new String[CL_groupMem.length+purchaseCL_groupMem.length];
			int cl_count = 0;
			for(int j=0;j<CL_groupMem.length;j++){

				CL_users[j]= CL_groupMem[j].getUser().toString();
				cl_count++;
			}
			for(int i=0;i<purchaseCL_groupMem.length;i++){

				CL_users[cl_count]= purchaseCL_groupMem[i].getUser().toString();
				cl_count++;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		TableColumn regionColumn = table.getColumnModel().getColumn(20);
		JComboBox<String> regionComboBox = new JComboBox<String>(regions);
		regionColumn.setCellEditor(new DefaultCellEditor(regionComboBox));

		TableColumn clColumn = table.getColumnModel().getColumn(21);
		JComboBox<String> clComboBox = new JComboBox<String>(CL_users);
		clColumn.setCellEditor(new DefaultCellEditor(clComboBox));

		TableColumn buyerColumn = table.getColumnModel().getColumn(22);
		JComboBox<String> buyerComboBox = new JComboBox<String>(buyer_users);
		buyerColumn.setCellEditor(new DefaultCellEditor(buyerComboBox));

		TableColumn clMColumn = table.getColumnModel().getColumn(23);
		JComboBox<String> clMComboBox = new JComboBox<String>(buyer_users);
		clMColumn.setCellEditor(new DefaultCellEditor(clMComboBox));

		String[] yes_No = {"","Yes","No"};
		String[] piece_status = {"","Final","Interim","Buyer Estimate","Cost Engineering Estimate"};

		TableColumn techColumn = table.getColumnModel().getColumn(26);
		JComboBox<String> techComboBox = new JComboBox<String>(yes_No);
		techColumn.setCellEditor(new DefaultCellEditor(techComboBox));
		
		TableColumn PieceStatusColumn = table.getColumnModel().getColumn(53);
		JComboBox<String> PieceStatusComboBox = new JComboBox<String>(piece_status);
		PieceStatusColumn.setCellEditor(new DefaultCellEditor(PieceStatusComboBox));

		TableColumn PTODemanColumn = table.getColumnModel().getColumn(58);
		JComboBox<String> PTODemandComboBox = new JComboBox<String>(yes_No);
		PTODemanColumn.setCellEditor(new DefaultCellEditor(PTODemandComboBox));
		
		TableColumn PTOColumn = table.getColumnModel().getColumn(59);
		JComboBox<String> PTOComboBox = new JComboBox<String>(yes_No);
		PTOColumn.setCellEditor(new DefaultCellEditor(PTOComboBox));
		
		TableColumn POColumn = table.getColumnModel().getColumn(61);
		JComboBox<String> POComboBox = new JComboBox<String>(yes_No);
		POColumn.setCellEditor(new DefaultCellEditor(POComboBox));


	}

	public boolean vaildCheck()
	{
		if(table.getCellEditor()!=null){
			table.getCellEditor().stopCellEditing();
		}
		for(int i=0;i< table.getRowCount();i++)
		{
			for (int j=0;j<table.getColumnCount();j++)
			{
				String om=table.getValueAt(i,21).toString();
				System.out.println("\n Value ="+om+ " and Length :"+om.length());
				if(om.trim().length()==0)
				{
					return false;
				}
			}
		}
		return true;
	}

	public void saveLineItem(){

		Hashtable<String , String> data_table = null ;
		Hashtable<String , Date> date_table = null ;

		for (int row = 0 ; row < table.getRowCount() ; row ++ )
		{
			data_table = new Hashtable<String, String>() ;
			date_table = new Hashtable<String, Date>() ;
			String part_ID = "";
			String pos_ID = "";
			for (int col = 0 ; col < table.getColumnCount() ; col ++)
			{
				switch (col) {

				case 1:
					/*if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_bom_vfPartNumber", table.getValueAt(row, col).toString()) ;*/
					part_ID = table.getValueAt(row, col).toString();
					break ;
				/*case 2:
					data_table.put("object_name", table.getValueAt(row, col).toString()) ;
					break ;
				case 3:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_bom_vehicle_type", table.getValueAt(row, col).toString()) ;
					break ;*/
				case 4:
					/*if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_bom_posID", table.getValueAt(row, col).toString()) ;*/
					pos_ID = table.getValueAt(row, col).toString();
					break ;
				/*case 5:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_bom_quantity", table.getValueAt(row, col).toString()) ;
					break ;
				case 6:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("uom_tag", table.getValueAt(row, col).toString()) ;
					break ;
				case 7:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_main_module_bom", table.getValueAt(row, col).toString()) ;
					break ;
				case 8:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_module_group_bom", table.getValueAt(row, col).toString()) ;
					break ;
				case 9:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_module_name", table.getValueAt(row, col).toString()) ;
					break ;
				case 10:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_reference_number_bom", table.getValueAt(row, col).toString()) ;
					break ;
				case 11:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_reference_number_bom", table.getValueAt(row, col).toString()) ;
					break ;
				case 12:
					data_table.put("vf4_purchasing_level", table.getValueAt(row, col).toString()) ;
					break ;
				case 13:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_reference_number_bom", table.getValueAt(row, col).toString()) ;
					break ;
				case 14:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						break ;
					}
					data_table.put("vf4_reference_number_bom", table.getValueAt(row, col).toString()) ;
					break ;*/
				case 15:
					
					data_table.put("vf4_alt_pot_supplier_h2p", table.getValueAt(row, col).toString()) ;
					break ;
				case 16:
					
					data_table.put("vf4_bmw_supplier_code", table.getValueAt(row, col).toString()) ;
					break ;
				case 17:
					
					data_table.put("vf4_bmw_supplier", table.getValueAt(row, col).toString()) ;
					break ;
				case 18:
					
					data_table.put("vf4_bmw_nominated_supplier", table.getValueAt(row, col).toString()) ;
					break ;
				case 19:
					
					data_table.put("vf4_supplier_country", table.getValueAt(row, col).toString()) ;
					break ;
				case 20:
					
					data_table.put("vf4_manuf_location", table.getValueAt(row, col).toString()) ;
					break ;
				case 21:
					
					data_table.put("vf4_cl", table.getValueAt(row, col).toString()) ;
					break ;
				case 22:
					
					data_table.put("vf4_buyer", table.getValueAt(row, col).toString()) ;
					break ;
				case 23:
					
					data_table.put("vf4_cl_by_module", table.getValueAt(row, col).toString()) ;
					break ;
				case 24:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_clPlan_nda_signed", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_clPlan_nda_signed", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 25:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_buyerPlan_NDASIGNED", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_buyerPlan_NDASIGNED", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 26:
					
					data_table.put("vf4_technical_pack_MSE", table.getValueAt(row, col).toString()) ;
					break ;
				case 27:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_plan_rfi", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_plan_rfi", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 28:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_rfi", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_rfi", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 29:
					
					data_table.put("vf4_rfi", table.getValueAt(row, col).toString()) ;
					break ;
				case 30:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_quote_received", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_quote_received", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 31:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_cl_plan_SoB", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_cl_plan_SoB", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 32:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_buyer_actual_SoB", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_buyer_actual_SoB", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 33:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_cl_plan_feasibility", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_cl_plan_feasibility", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 34:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_buyer_actual_feasibilit", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_buyer_actual_feasibilit", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 35:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_sourcing_recommendation", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_sourcing_recommendation", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 36:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_sourcing_recommendAppr", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_sourcing_recommendAppr", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 37:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_plan_award_supplier", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_plan_award_supplier", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 38:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_award_supplier", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_award_supplier", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 39:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_rfq", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_rfq", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 40:
					data_table.put("vf4_system_rfi_rfq", table.getValueAt(row, col).toString()) ;
					break ;
				case 41:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_quote_received", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_quote_received", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 42:
					data_table.put("vf4_buyer_quote", table.getValueAt(row, col).toString()) ;
					break ;
				case 43:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_eddorder", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_eddorder", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 44:
					data_table.put("vf4_eddorder_currency", table.getValueAt(row, col).toString()) ;
					break ;
				case 45:
					data_table.put("vf4_eddorder_value", table.getValueAt(row, col).toString()) ;
					break ;
				case 46:
					data_table.put("vf4_eddorder", table.getValueAt(row, col).toString()) ;
					break ;
				case 47:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_tooling_order", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_tooling_order", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 48:
					data_table.put("vf4_tooling_currency", table.getValueAt(row, col).toString()) ;
					break ;
				case 49:
					data_table.put("vf4_tooling_order", table.getValueAt(row, col).toString()) ;
					break ;
				case 50:
					data_table.put("vf4_toolingorder", table.getValueAt(row, col).toString()) ;
					break ;
				case 51:
					data_table.put("vf4_piece_cost", table.getValueAt(row, col).toString()) ;
					break ;
				case 52:
					data_table.put("vf4_piece_currency", table.getValueAt(row, col).toString()) ;
					break ;
				case 53:
					data_table.put("vf4_piece_cost_status", table.getValueAt(row, col).toString()) ;
					break ;
				case 54:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_plan_contract_signed", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_plan_contract_signed", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 55:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_actual_contract_signed", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_actual_contract_signed", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 56:
					data_table.put("vf4_contract_signed", table.getValueAt(row, col).toString()) ;
					break ;
				case 57:
					data_table.put("vf4_preproduction_demand", table.getValueAt(row, col).toString()) ;
					break ;
				case 58:
					data_table.put("vf4_pto_onward_demand", table.getValueAt(row, col).toString()) ;
					break ;
				case 59:
					if (table.getValueAt(row, col).toString().equals(""))
					{
						data_table.put("vf4_pto", table.getValueAt(row, col).toString()) ;
					}else{
						
						date_table.put("vf4_pto", convertStrToDate(table.getValueAt(row, col).toString())) ;
					}
					break ;
				case 60:
					data_table.put("vf4_purchasing_comments", table.getValueAt(row, col).toString()) ;
					break ;
				case 61:
					data_table.put("vf4po_release", table.getValueAt(row, col).toString()) ;
					break ;
				}

			}
			TCComponentItemRevision rev = src_items.get(part_ID+"@"+pos_ID);
			new ESourcingSaveProperties(data_table,date_table , session, rev);
		}

	}

	public Vector<Object> createTableVector(InterfaceAIFComponent[] targetComp)
	{
		Vector<Object> tempVector = new Vector<Object>();
		src_items = new Hashtable<String , TCComponentItemRevision>() ;
		String[] prop_names = {"object_name","vf4_bom_vehicle_type","vf4_bom_posID","vf4_bom_quantity","vf4_bom_vfPartNumber","vf4_bom_change_index",
				"vf4_purchasing_level","vf4_structure_level_vf","vf4_engineering_part_owner","vf4_main_module_bom",
				"vf4_module_group_bom","vf4_module_name","vf4_reference_number_bom","vf4_alt_pot_supplier_h2p","vf4_bmw_supplier_code",
				"vf4_bmw_supplier","vf4_bmw_nominated_supplier","vf4_supplier_country","vf4_manuf_location","vf4_cl","vf4_buyer",
				"vf4_cl_by_module","vf4_clPlan_nda_signed","vf4_buyerPlan_NDASIGNED","vf4_technical_pack_MSE","vf4_plan_rfi",
				"vf4_actual_rfi","vf4_rfi","vf4_quote_received","vf4_cl_plan_SoB","vf4_buyer_actual_SoB","vf4_cl_plan_feasibility",
				"vf4_buyer_actual_feasibilit","vf4_sourcing_recommendation","vf4_sourcing_recommendAppr","vf4_plan_award_supplier",
				"vf4_actual_award_supplier","vf4_actual_rfq","vf4_system_rfi_rfq","vf4_actual_quote_received","vf4_buyer_quote",
				"vf4_actual_eddorder","vf4_eddorder_currency","vf4_eddorder_value","vf4_eddorder","vf4_actual_tooling_order","vf4_tooling_currency",
				"vf4_tooling_order","vf4_toolingorder","vf4_piece_cost","vf4_piece_currency","vf4_piece_cost_status","vf4_plan_contract_signed","vf4_actual_contract_signed","vf4_contract_signed",
				"vf4_preproduction_demand","vf4_pto_onward_demand","vf4_pto","vf4_purchasing_comments","vf4po_release"};
		try
		{
			for (int inx = 0 ; inx < targetComp.length ; inx ++)
			{

				TCComponentItemRevision targetRev = (TCComponentItemRevision) targetComp[inx] ;
				
				TCComponentItem item = targetRev.getItem();
				String releaseStatusList = "" ;
				releaseStatusList = targetRev.getProperty("release_status_list") ;
				if(targetComp[inx].getType().equals("VF4_line_itemRevision") && releaseStatusList.length() == 0){

					String[] props = targetRev.getProperties(prop_names);
					String UOM=item.getProperty("uom_tag");
					String name = props[0];
					String vehicle_type = props[1];
					String posID = props[2];
					String quantity = props[3];
					String vfPartNumber = props[4];
					String change_index = props[5];
					String purchase_level = props[6];
					String structure_level = props[7];
					String part_owner = props[8];
					String main_module = props[9];
					String module_group = props[10];
					String module_name = props[11];
					String BWMRefNo = props[12];
					String potential_supplier = props[13];
					String bmwSupplierCode= props[14];
					String bmwSupplier= props[15];
					String bmwNominatedSupplier= props[16];
					String supplierRegion= props[17];
					String manufacturingLocation= props[18];
					String cl= props[19];
					String buyer= props[20];
					String cl_byModule= props[21];
					String clPlanNdasignedString=convertDateFormat(props[22]);
					String buyerPlanNdasignedString=convertDateFormat(props[23]);
					String technical_package_review = props[24];
					
					if(technical_package_review.equals("")){
						
						technical_package_review = "No";
					}
					
					String clPlanRfi = convertDateFormat(props[25]);
					String buyerActualRfi = convertDateFormat(props[26]);
					String rfi = props[27];
					String quoteReceived = convertDateFormat(props[28]);
					String cl_plan_SoB =convertDateFormat(props[29]);
					String buyer_actual_SoB =convertDateFormat(props[30]);
					String cl_plan_feasibility_confirm = convertDateFormat(props[31]);
					String buyer_actual_feasibility_confirm = convertDateFormat(props[32]);
					String cl_sourcing_recommendation_sent = convertDateFormat(props[33]);
					String buyer_sourcing_recommendation_approved = convertDateFormat(props[34]);
					String cl_plan_supplier_award = convertDateFormat(props[35]);
					String buyer_actual_supplier_award = convertDateFormat(props[36]);
					String buyer_actual_rfq = convertDateFormat(props[37]);
					String system_rfi_rfq = props[38];
					String buyer_actual_quote_received = convertDateFormat(props[39]);
					String buyer_quote = props[40];
					String buyer_actual_eddorder = convertDateFormat(props[41]);
					String eddorder_currency = props[42];
					String eddorder_value = props[43];
					String eddorder = props[44];
					String buyer_actual_tooling_order = convertDateFormat(props[45]);
					String tooling_order_currency = props[46];
					String tooling_order_value = props[47];
					String tooling = props[48];
					String piece_cost_value = props[49];
					String piece_currency_value = props[50];
					String piece_cost_status = props[51];
					String cl_plan_contract_signed = convertDateFormat(props[52]);
					String buyer_actual_contract_signed = convertDateFormat(props[53]);
					String contract_signed = props[54];
					String buyer_preproduction_demand_secured = props[55];
					String buyer_pto_onward_demand = props[56];
					String PTO1 = convertDateFormat(props[57]);
					String purchasing_comments = props[58];
					String po_release = props[59];

					tempVector.addElement(new Data(false, vfPartNumber , name,vehicle_type,posID,quantity,UOM,change_index,purchase_level,
							structure_level,part_owner,main_module,module_group,module_name,BWMRefNo,potential_supplier,bmwSupplierCode,bmwSupplier,
							bmwNominatedSupplier,supplierRegion, manufacturingLocation,	cl,buyer,cl_byModule,clPlanNdasignedString,
							buyerPlanNdasignedString,technical_package_review, clPlanRfi,buyerActualRfi,rfi,quoteReceived,cl_plan_SoB,
							buyer_actual_SoB, cl_plan_feasibility_confirm , buyer_actual_feasibility_confirm , cl_sourcing_recommendation_sent,
							buyer_sourcing_recommendation_approved,cl_plan_supplier_award,buyer_actual_supplier_award,buyer_actual_rfq,system_rfi_rfq,
							buyer_actual_quote_received,buyer_quote,buyer_actual_eddorder,eddorder_currency,eddorder_value,eddorder, buyer_actual_tooling_order,
							tooling_order_currency,tooling_order_value,tooling,piece_cost_value,piece_currency_value,piece_cost_status,
							cl_plan_contract_signed, buyer_actual_contract_signed ,contract_signed,buyer_preproduction_demand_secured,buyer_pto_onward_demand ,
							PTO1 ,purchasing_comments,po_release));

					src_items.put(vfPartNumber+"@"+posID, targetRev);


				}
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return tempVector;
	}


	private String convertDateFormat(String dateProperty) {
		// TODO Auto-generated method stub
		String strDate = "";
		if(!dateProperty.equals("")){
			try {
				strDate = format.format(format.parse(dateProperty));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return strDate;
	}

	private Date convertStrToDate(String dateProperty) {
		// TODO Auto-generated method stub
		Date D_Date = null;
		if(dateProperty!=null && !dateProperty.equals("") ){
			try {
				D_Date = format.parse(dateProperty);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return D_Date;
	}


	//INNER CLASS
	class Data
	{
		private boolean sum;
		private String name = "";
		private String vehicle_type = "";
		private String posID = "";
		private String quantity = "";
		private String UOM = "";
		private String vfPartNumber = "";
		private String change_index = "";
		private String purchase_level = "" ;
		private String structure_level = "";
		private String part_owner = "";
		private String main_Module = "";
		private String module_Group = "";
		private String module_Name = "";
		private String BMW_No = "";
		private String potential_supplier  = "";
		private String bmwSupplierCode = "";
		private String bmwSupplier = "";
		private String bmwNominatedSupplier = "";
		private String supplierRegion = "";
		private String manufacturingLocation = "";
		private String cl = "";
		private String buyer = "";
		private String cl_byModule = "";
		private String clPlanNdasignedString = "";
		private String buyerPlanNdasignedString = "";
		private String technical_package_review  = "";
		private String clPlanRfi = "";
		private String buyerActualRfi = "";
		private String rfi = "";
		private String quoteReceived  = "";
		private String cl_plan_SoB  = "";
		private String buyer_actual_SoB = "" ;
		private String cl_plan_feasibility_confirm = "";
		private String buyer_actual_feasibility_confirm  = "";
		private String cl_sourcing_recommendation_sent  = "";
		private String buyer_sourcing_recommendation_approved = "";
		private String cl_plan_supplier_award  = "";
		private String buyer_actual_supplier_award  = "";
		private String buyer_actual_rfq  = "";
		private String system_rfi_rfq  = "";
		private String buyer_actual_quote_received  = "";
		private String buyer_quote  = "";
		private String buyer_actual_eddorder = "" ;
		private String eddorder_currency = "" ;
		private String eddorder_value = "" ;
		private String buyer_actual_tooling_order = "" ;
		private String eddorder = "" ;
		private String tooling_order_currency = "" ;
		private String tooling_order_value = "" ;
		private String tooling_order = "" ;
		private String piece_cost = "" ;
		private String piece_cost_currency = "" ;
		private String piece_cost_status = "";
		private String cl_plan_contract_signed = "";
		private String buyer_actual_contract_signed  = "";
		private String contract_signed  = "";
		private String buyer_preproduction_demand_secured = "" ;
		private String buyer_pto_onward_demand = "" ;
		private String PTO1  = "";
		private String purchasing_comments  = "";
		private String po_release = "";


		public Data()
		{
		}

		public Data(
				Boolean sum, String vfPartNumber , String name, String vehicle_type, String posID, String quantity, String UOM,String change_index,String purchase_level_v,String structure_level,String part_owner, String main_module, String module_group, String module_name,
				String BMW_ref, String potential_supplier, String bmwSupplierCode,String bmwSupplier , String bmwNominatedSupplier , String supplierRegion ,String manufacturingLocation, String cl,
				String buyer , String cl_byModule, String clPlanNdasignedString, String buyerPlanNdasignedString , String technical_package_review ,String clPlanRfi ,String buyerActualRfi, String rfi ,
				String quoteReceived ,String cl_plan_SoB , String buyer_actual_SoB , String cl_plan_feasibility_confirm , String buyer_actual_feasibility_confirm , String cl_sourcing_recommendation_sent ,
				String buyer_sourcing_recommendation_approved , String cl_plan_supplier_award , String buyer_actual_supplier_award , String buyer_actual_rfq , String system_rfi_rfq ,
				String buyer_actual_quote_received , String buyer_quote , String buyer_actual_eddorder , String eddorder_currency , String eddorder_value , String eddorder, String buyer_actual_tooling_order ,
				String tooling_order_currency , String tooling_order_value ,String tooling_order,String piece_cost_v,String piece_cost_currency_v,String piece_cost_status,String cl_plan_contract_signed , 
				String buyer_actual_contract_signed , String  contract_signed , String buyer_preproduction_demand_secured , String buyer_pto_onward_demand ,  String PTO1 , String purchasing_comments, String po_release
				)
		{
			this.sum = sum;
			this.name = name;
			this.vehicle_type = vehicle_type;
			this.posID = posID;
			this.quantity = quantity;
			this.UOM = UOM;
			this.vfPartNumber = vfPartNumber;
			this.change_index = change_index;
			this.purchase_level = purchase_level_v ;
			this.structure_level = structure_level;
			this.part_owner = part_owner;
			this.main_Module = main_module;
			this.module_Group = module_group;
			this.module_Name = module_name;
			this.BMW_No = BMW_ref;
			this.potential_supplier = potential_supplier;
			this.bmwSupplierCode = bmwSupplierCode;
			this.bmwSupplier = bmwSupplier;
			this.bmwNominatedSupplier = bmwNominatedSupplier ;
			this.supplierRegion = supplierRegion;
			this.manufacturingLocation = manufacturingLocation;
			this.cl = cl ;
			this.buyer = buyer;
			this.cl_byModule = cl_byModule;
			this.clPlanNdasignedString = clPlanNdasignedString ;
			this.buyerPlanNdasignedString = buyerPlanNdasignedString ;
			this.technical_package_review = technical_package_review ;
			this.clPlanRfi = clPlanRfi ;
			this.buyerActualRfi = buyerActualRfi ;
			this.rfi = rfi ;
			this.quoteReceived = quoteReceived ;
			this.cl_plan_SoB = cl_plan_SoB ;
			this.buyer_actual_SoB = buyer_actual_SoB ;
			this.cl_plan_feasibility_confirm = cl_plan_feasibility_confirm ;
			this.buyer_actual_feasibility_confirm = buyer_actual_feasibility_confirm ;
			this.cl_sourcing_recommendation_sent = cl_sourcing_recommendation_sent ;
			this.buyer_sourcing_recommendation_approved = buyer_sourcing_recommendation_approved ;
			this.cl_plan_supplier_award = cl_plan_supplier_award ;
			this.buyer_actual_supplier_award = buyer_actual_supplier_award ;
			this.buyer_actual_rfq = buyer_actual_rfq ;
			this.system_rfi_rfq = system_rfi_rfq ;
			this.buyer_actual_quote_received = buyer_actual_quote_received ;
			this.buyer_quote = buyer_quote ;
			this.buyer_actual_eddorder = buyer_actual_eddorder ;
			this.eddorder_currency = eddorder_currency ;
			this.eddorder_value = eddorder_value ;
			this.eddorder = eddorder;
			this.buyer_actual_tooling_order = buyer_actual_tooling_order ;
			this.tooling_order_currency = tooling_order_currency ;
			this.tooling_order_value = tooling_order_value ;
			this.tooling_order = tooling_order;
			this.piece_cost = piece_cost_v ;
			this.piece_cost_currency = piece_cost_currency_v ;
			this.piece_cost_status = piece_cost_status;
			this.cl_plan_contract_signed = cl_plan_contract_signed ;
			this.buyer_actual_contract_signed = buyer_actual_contract_signed ;
			this.contract_signed = contract_signed ;
			this.buyer_preproduction_demand_secured = buyer_preproduction_demand_secured ;
			this.buyer_pto_onward_demand = buyer_pto_onward_demand ;
			this.PTO1 = PTO1 ;
			this.purchasing_comments = purchasing_comments ;
			this.po_release = po_release;

		}

		public boolean getSelectedRow()
		{
			return this.sum ;
		}
		public String getName()
		{
			return this.name ;
		}
		public String getvehicleType()
		{
			return this.vehicle_type ;
		}
		public String getposID()
		{
			return this.posID ;
		}
		public String getQuantity()
		{
			return this.quantity ;
		}
		public String getUOM()
		{
			return this.UOM ;
		}
		public String getvfPartNumber()
		{
			return this.vfPartNumber ;
		}
		public String getChangeIndex()
		{
			return this.change_index ;
		}
		public String getPurchaseLevel()
		{
			return this.purchase_level ;
		}
		public String getStructureLevel()
		{
			return this.structure_level ;
		}
		public String getPartOwner()
		{
			return this.part_owner ;
		}
		public String getMainModule()
		{
			return this.main_Module ;
		}
		public String getModuleGroup()
		{
			return this.module_Group ;
		}
		public String getModuleName()
		{
			return this.module_Name ;
		}
		public String getBMWRefNo()
		{
			return this.BMW_No ;
		}
		public String getAltSupplier()
		{
			return this.potential_supplier ;
		}
		public String getBMWSupplierCode()
		{
			return this.bmwSupplierCode ;
		}
		public String getBMWSupplier()
		{
			return this.bmwSupplier ;
		}
		public String getBMWNominatedSupplier()
		{
			return this.bmwNominatedSupplier ;
		}
		public String getSupplierRegion()
		{
			return this.supplierRegion ;
		}
		public String getManufacturingLoc()
		{
			return this.manufacturingLocation ;
		}
		public String getcl()
		{
			return this.cl ;
		}
		public String getBuyer()
		{
			return this.buyer ;
		}
		public String getclByModule()
		{
			return this.cl_byModule ;
		}
		public String getPlanNDASigned()
		{
			return this.clPlanNdasignedString ;
		}
		public String getActualNDASigned()
		{
			return this.buyerPlanNdasignedString ;
		}
		public String gettechReview()
		{
			return this.technical_package_review ;
		}
		public String getPlanRFI()
		{
			return this.clPlanRfi ;
		}
		public String getActualRFI()
		{
			return this.buyerActualRfi ;
		}
		public String getRfi()
		{
			return this.rfi ;
		}
		public String getQuoteReceived()
		{
			return this.quoteReceived ;
		}
		public String getPlanSoB()
		{
			return this.cl_plan_SoB ;
		}
		public String getActualSoB()
		{
			return this.buyer_actual_SoB ;
		}
		public String getPlanFeasibility()
		{
			return this.cl_plan_feasibility_confirm ;
		}
		public String getActualFeasibility()
		{
			return this.buyer_actual_feasibility_confirm ;
		}
		public String getSourcingRecommendationSent()
		{
			return this.cl_sourcing_recommendation_sent ;
		}
		public String getSourcingRecommendationApproved()
		{
			return this.buyer_sourcing_recommendation_approved ;
		}
		public String getPlanSupplierAward()
		{
			return this.cl_plan_supplier_award ;
		}
		public String getActualSupplierAward()
		{
			return this.buyer_actual_supplier_award ;
		}
		public String getActualRFQ()
		{
			return this.buyer_actual_rfq ;
		}
		public String getSystemRfi_Rfq()
		{
			return this.system_rfi_rfq ;
		}
		public String getActualQuoteReceived()
		{
			return this.buyer_actual_quote_received ;
		}
		public String getQuote()
		{
			return this.buyer_quote ;
		}
		public String geteddOrderCurrency()
		{
			return this.eddorder_currency ;
		}
		public String getActualEDDorder()
		{
			return this.buyer_actual_eddorder ;
		}
		public String geteddOrderValue()
		{
			return this.eddorder_value ;
		}
		public String geteddOrder()
		{
			return this.eddorder ;
		}
		public String getActualToolingOrder()
		{
			return this.buyer_actual_tooling_order ;
		}
		public String getTCCurrency()
		{
			return this.tooling_order_currency ;
		}
		public String getTOValue()
		{
			return this.tooling_order_value ;
		}
		public String getTO()
		{
			return this.tooling_order;
		}
		public String getPieceCost()
		{
			return this.piece_cost ;
		}
		public String getPieceCurr()
		{
			return this.piece_cost_currency ;
		}
		public String getPieceStatus()
		{
			return this.piece_cost_status ;
		}
		public String getPlanContractSigned()
		{
			return this.cl_plan_contract_signed ;
		}
		public String getActualContractSigned()
		{
			return this.buyer_actual_contract_signed ;
		}
		public String getContractSigned()
		{
			return this.contract_signed ;
		}
		public String getpreProductionDemand()
		{
			return this.buyer_preproduction_demand_secured ;
		}
		public String getPTODemand()
		{
			return this.buyer_pto_onward_demand;
		}
		public String getPTO()
		{
			return this.PTO1 ;
		}
		public String getPurchasingComments()
		{
			return this.purchasing_comments ;
		}
		public String getPORelease()
		{
			return this.po_release ;
		}


		// SET ROW

		public void setSelectedRow(boolean sum)
		{
			this.sum = sum ;
		}
		public void setName(String name)
		{
			this.name = name ;
		}
		public void setvehicleType(String vehicle_type)
		{
			this.vehicle_type = vehicle_type;
		}
		public void setposID(String posID)
		{
			this.posID = posID;
		}
		public void setQuantity(String quantity)
		{
			this.quantity = quantity;
		}
		public void setUOM(String UOM)
		{
			this.UOM = UOM;
		}
		public void setvfPartNumber(String vfPartNumber)
		{
			this.vfPartNumber = vfPartNumber;
		}
		public void setChangeIndex(String change_index)
		{
			this.change_index = change_index;
		}
		public void setPurchaseLevel(String purchase_level)
		{
			this.purchase_level = purchase_level;
		}
		public void setStructureLevel(String structure_level)
		{
			this.structure_level = structure_level;
		}
		public void setPartOwner(String part_owner)
		{
			this.part_owner = part_owner;
		}
		public void setMainModule(String main_module)
		{
			this.main_Module = main_module;
		}
		public void setModuleGroup(String module_group)
		{
			this.module_Group = module_group ;
		}
		public void setModuleName(String module_name)
		{
			this.module_Name = module_name;
		}
		public void setBMWRefNo(String bmwrefNO)
		{
			this.BMW_No = bmwrefNO ;
		}
		public void setAltSupplier(String potential_supplier)
		{
			this.potential_supplier = potential_supplier;
		}
		public void setBMWSupplierCode(String bmwSupplierCode)
		{
			this.bmwSupplierCode = bmwSupplierCode;
		}
		public void setBMWSupplier(String bmwSupplier)
		{
			this.bmwSupplier = bmwSupplier;
		}
		public void setBMWNominatedSupplier(String bmwNominatedSupplier)
		{
			this.bmwNominatedSupplier = bmwNominatedSupplier;
		}
		public void setSupplierRegion(String supplierRegion)
		{
			this.supplierRegion = supplierRegion;
		}
		public void setManufacturingLoc(String manufacturingLocation)
		{
			this.manufacturingLocation = manufacturingLocation;
		}
		public void setcl(String cl)
		{
			this.cl = cl;
		}
		public void setBuyer(String buyer)
		{
			this.buyer = buyer;
		}
		public void setclByModule(String cl_byModule)
		{
			this.cl_byModule = cl_byModule;
		}
		public void setPlanNDASigned(String clPlanNdasignedString)
		{
			this.clPlanNdasignedString = clPlanNdasignedString;
		}
		public void setActualNDASigned(String buyerPlanNdasignedString)
		{
			this.buyerPlanNdasignedString = buyerPlanNdasignedString ;
		}
		public void settechReview(String technical_package_review)
		{
			this.technical_package_review = technical_package_review ;
		}
		public void setPlanRFI(String clPlanRfi)
		{
			this.clPlanRfi = clPlanRfi ;
		}
		public void setActualRFI(String buyerActualRfi)
		{
			this.buyerActualRfi = buyerActualRfi;
		}
		public void setRfi(String rfi)
		{
			this.rfi = rfi;
		}
		public void setQuoteReceived(String quoteReceived)
		{
			this.quoteReceived = quoteReceived;
		}
		public void setPlanSoB(String cl_plan_SoB)
		{
			this.cl_plan_SoB = cl_plan_SoB;
		}
		public void setActualSoB(String buyer_actual_SoB)
		{
			this.buyer_actual_SoB = buyer_actual_SoB ;
		}
		public void setPlanFeasibility(String cl_plan_feasibility_confirm)
		{
			this.cl_plan_feasibility_confirm = cl_plan_feasibility_confirm;
		}
		public void setActualFeasibility(String buyer_actual_feasibility_confirm)
		{
			this.buyer_actual_feasibility_confirm = buyer_actual_feasibility_confirm;
		}
		public void setSourcingRecommendationSent(String cl_sourcing_recommendation_sent)
		{
			this.cl_sourcing_recommendation_sent = cl_sourcing_recommendation_sent ;
		}
		public void setSourcingRecommendationApproved(String buyer_sourcing_recommendation_approved)
		{
			this.buyer_sourcing_recommendation_approved = buyer_sourcing_recommendation_approved;
		}
		public void setPlanSupplierAward(String cl_plan_supplier_award)
		{
			this.cl_plan_supplier_award = cl_plan_supplier_award ;
		}
		public void setActualSupplierAward(String buyer_actual_supplier_award)
		{
			this.buyer_actual_supplier_award = buyer_actual_supplier_award;
		}
		public void setActualRFQ(String buyer_actual_rfq)
		{
			this.buyer_actual_rfq = buyer_actual_rfq;
		}
		public void setSystemRfi_Rfq(String system_rfi_rfq)
		{
			this.system_rfi_rfq = system_rfi_rfq;
		}
		public void setActualQuoteReceived(String buyer_actual_quote_received)
		{
			this.buyer_actual_quote_received = buyer_actual_quote_received;
		}
		public void setQuote(String buyer_quote)
		{
			this.buyer_quote = buyer_quote;
		}
		public void seteddOrderCurrency(String eddorder_currency)
		{
			this.eddorder_currency = eddorder_currency;
		}
		public void setActualEDDorder(String buyer_actual_eddorder)
		{
			this.buyer_actual_eddorder = buyer_actual_eddorder;
		}
		public void seteddOrderValue(String eddorder_value)
		{
			this.eddorder_value = eddorder_value;
		}
		public void seteddOrder(String eddorder)
		{
			this.eddorder = eddorder ;
		}
		public void setActualToolingOrder(String buyer_actual_tooling_order)
		{
			this.buyer_actual_tooling_order = buyer_actual_tooling_order;
		}
		public void setTCCurrency(String tooling_order_currency)
		{
			this.tooling_order_currency = tooling_order_currency;
		}
		public void setTOValue(String tooling_order_value)
		{
			this.tooling_order_value = tooling_order_value;
		}
		public void setTO(String tooling_order)
		{
			this.tooling_order = tooling_order;
		}
		public void setPieceCost(String piece_cost_v)
		{
			this.piece_cost = piece_cost_v;
		}
		public void setPieceCurr(String piece_cost_currency_v)
		{
			this.piece_cost_currency = piece_cost_currency_v;
		}
		public void setPieceStatus(String piece_cost_status)
		{
			this.piece_cost_status = piece_cost_status;
		}
		public void setPlanContractSigned(String cl_plan_contract_signed)
		{
			this.cl_plan_contract_signed  = cl_plan_contract_signed ;
		}
		public void setActualContractSigned(String buyer_actual_contract_signed)
		{
			this.buyer_actual_contract_signed =  buyer_actual_contract_signed ;
		}
		public void setContractSigned(String contract_signed)
		{
			this.contract_signed = contract_signed;
		}
		public void setpreProductionDemand(String buyer_preproduction_demand_secured)
		{
			this.buyer_preproduction_demand_secured = buyer_preproduction_demand_secured;
		}
		public void setPTODemand(String buyer_pto_onward_demand)
		{
			this.buyer_pto_onward_demand = buyer_pto_onward_demand;
		}
		public void setPTO(String PTO1)
		{
			this.PTO1 = PTO1;
		}
		public void setPurchasingComments(String purchasing_comments)
		{
			this.purchasing_comments = purchasing_comments ;
		}
		public void setPORelease(String po_release)
		{
			this.po_release = po_release;
		}
	}


	class VFTableModel extends AbstractTableModel
	{
		public String[] column_names = { "","VF PART NUMBER", "DESCRIPTION", "VEHICLE TYPE", "POS ID", "QUANTITY", "UOM","CHANGE INDEX","PURCHASE LEVEL VF","STRUCTURE LEVEL VF","ENGINEER PART OWNER",
				"MAIN MODULE", "MODULE GROUP","MODULE NAME","BMW REFERENCE NO","ALT/POT SUPPLIER", "BMW SUPPLIER CODE", "BMW SUPPLIER","AWARDED SUPPLIER" , "SUPPLIER COUNTRY",
				"MANUFACTURING COUNTRY" ,"CL" , "BUYER" ,"MODULE OWNER" , "PLAN NDA" , "ACTUAL NDA" , "TECHNICAL PACKAGE REVIEW" , "PLAN RFI" ,
				"ACTUAL RFI", "RFI" , "QUOTE RECEIVED" , "PLAN SoB" , "ACTUAL SoB" , "PLAN FEASIBILITY CONFIRMED", "ACTUAL FEASIBILITY CONFIRMED",
				"SOURCING REC SENT" , "SOURCING REC APPROVED" , "PLAN SUPPLIER AWARD" , "ACTUAL SUPPLIER AWARD" ,"RFQ ACTUAL", "RFI#", "QUOTE RECEIVED",
				"QUOTE#","ACTUAL ED&D ORDER" , "CURRENCY" , "VALUE" ,"ED&D ORDER#", "ACTUAL TOOLING ORDER", "CURRENCY", "VALUE","TOOLING ORDER#","PIECE COST","PIECE CURRENCY","PIECE COST STATUS",
				"PLAN PART CONTRACT SIGNED" , "ACTUAL PART CONTRACT SIGNED" , "PART CONTRACT SIGNED#" ,"PRE-PROD DEMAND SECURED", "PTO ONWARD DEMAND SECURED",  "PTO" , "PURCHASING COMMENTS","PO RELEASED"};

		public Class[] column_types = { Boolean.class, String.class, String.class, String.class, String.class, String.class,
				String.class, String.class, String.class,String.class, String.class, String.class, String.class, String.class,
				String.class, String.class, String.class, String.class, String.class, String.class, String.class , String.class,
				String.class, String.class ,String.class , String.class , String.class, String.class , String.class ,  String.class,
				String.class , String.class , String.class , String.class , String.class , String.class , String.class ,String.class ,
				String.class ,String.class ,String.class ,String.class ,String.class, String.class, String.class ,String.class,
				String.class ,String.class, String.class,String.class,String.class,String.class, String.class, String.class , 
				String.class , String.class ,String.class,String.class,String.class,String.class,String.class,String.class };

		public final Object[] longValues = { "","","","","","","","","","","","","","","","","","","","","","","","","","","",
				"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};

		Vector<Object> m_macDataVector;

		public VFTableModel(Vector<Object> staticVector) {
			// TODO Auto-generated constructor stub
			this.m_macDataVector = staticVector;
		}
		public int getColumnCount() {
			return this.column_names.length;
		}
		public int getRowCount() {
			return this.m_macDataVector.size();
		}

		public String getColumnName(int col) {
			return this.column_names[col];
		}

		public Class getColumnClass(int col) {
			return this.column_types[col];
		}
		public void removeRow(int row) {
			m_macDataVector.removeElementAt(row);
			fireTableRowsDeleted(row, row);
		}

		public Object getValueAt(int row, int col) {

			ESourcingEditDialog.Data macData = (ESourcingEditDialog.Data)this.m_macDataVector.elementAt(row);

			switch (col) {

			case 0:
				return macData.getSelectedRow();
			case 1:
				return macData.getvfPartNumber();
			case 2:
				return macData.getName();
			case 3:
				return macData.getvehicleType();
			case 4:
				return macData.getposID();
			case 5:
				return macData.getQuantity();
			case 6:
				return macData.getUOM();
			case 7:
				return macData.getChangeIndex();
			case 8:
				return macData.getPurchaseLevel();
			case 9:
				return macData.getStructureLevel();
			case 10:
				return macData.getPartOwner();
			case 11:
				return macData.getMainModule();
			case 12:
				return macData.getModuleGroup();
			case 13:
				return macData.getModuleName();
			case 14:
				return macData.getBMWRefNo();
			case 15:
				return macData.getAltSupplier();
			case 16:
				return macData.getBMWSupplierCode();
			case 17:
				return macData.getBMWSupplier();
			case 18:
				return macData.getBMWNominatedSupplier();
			case 19:
				return macData.getSupplierRegion();
			case 20:
				return macData.getManufacturingLoc();
			case 21:
				return macData.getcl();
			case 22:
				return macData.getBuyer();
			case 23:
				return macData.getclByModule();
			case 24:
				return macData.getPlanNDASigned();
			case 25:
				return macData.getActualNDASigned();
			case 26:
				return macData.gettechReview();
			case 27:
				return macData.getPlanRFI();
			case 28:
				return macData.getActualRFI();
			case 29:
				return macData.getRfi();
			case 30:
				return macData.getQuoteReceived();
			case 31:
				return macData.getPlanSoB();
			case 32:
				return macData.getActualSoB();
			case 33:
				return macData.getPlanFeasibility();
			case 34:
				return macData.getActualFeasibility();
			case 35:
				return macData.getSourcingRecommendationSent();
			case 36:
				return macData.getSourcingRecommendationApproved();
			case 37:
				return macData.getPlanSupplierAward();
			case 38:
				return macData.getActualSupplierAward();
			case 39:
				return macData.getActualRFQ();
			case 40:
				return macData.getSystemRfi_Rfq();
			case 41:
				return macData.getActualQuoteReceived();
			case 42:
				return macData.getQuote();
			case 43:
				return macData.getActualEDDorder();
			case 44:
				return macData.geteddOrderCurrency();
			case 45:
				return macData.geteddOrderValue();
			case 46:
				return macData.geteddOrder();
			case 47:
				return macData.getActualToolingOrder();
			case 48:
				return macData.getTCCurrency();
			case 49:
				return macData.getTOValue();
			case 50:
				return macData.getTO();
			case 51:
				return macData.getPieceCost();
			case 52:
				return macData.getPieceCurr();
			case 53:
				return macData.getPieceStatus();
			case 54:
				return macData.getPlanContractSigned();
			case 55:
				return macData.getActualContractSigned();
			case 56:
				return macData.getContractSigned();
			case 57:
				return macData.getpreProductionDemand();
			case 58:
				return macData.getPTODemand();
			case 59:
				return macData.getPTO();
			case 60:
				return macData.getPurchasingComments();
			case 61:
				return macData.getPORelease();

			}
			return new String();
		}

		public boolean isCellEditable(int row, int col)
		{
			if (session.getCurrentRole().toString().equals("CL"))
			{
				//Make Editable
				return col ==0||col ==16||col ==17||col ==22||col ==23||col ==24||col ==27||col ==29||col ==31||
						col ==33||col ==35||col ==37||col ==40||col ==54||col ==56||col ==59||col ==60||col ==61? true : false;
			}
			else
			{
				//15,16,17,18,19,20,25,26,28,29,30,32,34,36,38,39,40,41 to 55,57,58,59,60,61
				return col ==15||col ==16||col ==17||col ==18||col ==19||col ==20||col ==25||col ==26||col ==28||col ==30||col ==32
						||col ==34||col ==36||col ==38||col ==39||col ==41||col ==42||col ==43||col ==44||col ==45
						||col ==46||col ==47||col ==48||col ==49||col ==50||col ==51||col ==52||col ==53||col ==55
						||col ==56||col ==57||col ==58||col ==59||col ==60||col ==61? true : false;
			}
		}

		public void setValueAt(Object value, int row, int col) {

			ESourcingEditDialog.Data macData = (ESourcingEditDialog.Data)this.m_macDataVector.elementAt(row);

			switch (col) {

			case 0:
				macData.setSelectedRow((boolean)value);
				break ;
			case 1:
				macData.setvfPartNumber((String)value);
				break ;
			case 2:
				macData.setName((String)value);
				break ;
			case 3:
				macData.setvehicleType((String)value);
				break ;
			case 4:
				macData.setposID((String)value);
				break ;
			case 5:
				macData.setQuantity((String)value);
				break ;
			case 6:
				macData.setUOM((String)value);
				break ;
			case 7:
				macData.setChangeIndex((String)value);
				break ;
			case 8:
				macData.setPurchaseLevel((String)value);
				break ;
			case 9:
				macData.setStructureLevel((String)value);
				break ;
			case 10:
				macData.setPartOwner((String)value);
				break ;
			case 11:
				macData.setMainModule((String)value);
				break ;
			case 12:
				macData.setModuleGroup((String)value);
				break ;
			case 13:
				macData.setModuleName((String)value);
				break ;
			case 14:
				macData.setBMWRefNo((String)value);
				break ;
			case 15:
				macData.setAltSupplier((String)value);
				break ;
			case 16:
				macData.setBMWSupplierCode((String)value);
				break ;
			case 17:
				macData.setBMWSupplier((String)value);
				break ;
			case 18:
				macData.setBMWNominatedSupplier((String)value);
				break ;
			case 19:
				macData.setSupplierRegion((String)value);
				break ;
			case 20:
				macData.setManufacturingLoc((String)value);
				break ;
			case 21:
				macData.setcl((String)value);
				break ;
			case 22:
				macData.setBuyer((String)value);
				break ;
			case 23:
				macData.setclByModule((String)value);
				break ;
			case 24:

				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanNDASigned("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanNDASigned("");
					}else{
						macData.setPlanNDASigned((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 25:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualNDASigned("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualNDASigned("");
					}else{
						macData.setActualNDASigned((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 26:
				macData.settechReview((String)value);
				break ;
			case 27:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanRFI("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanRFI("");
					}else{
						macData.setPlanRFI((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 28:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualRFI("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualRFI("");
					}else{
						macData.setActualRFI((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 29:
				macData.setRfi((String)value);
				break ;
			case 30:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setQuoteReceived("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setQuoteReceived("");
					}else{
						macData.setQuoteReceived((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 31:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanSoB("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanSoB("");
					}else{
						macData.setPlanSoB((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 32:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualSoB("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualSoB("");
					}else{
						macData.setActualSoB((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 33:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanFeasibility("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanFeasibility("");
					}else{
						macData.setPlanFeasibility((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 34:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualFeasibility("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualFeasibility("");
					}else{
						macData.setActualFeasibility((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 35:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setSourcingRecommendationSent("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setSourcingRecommendationSent("");
					}else{
						macData.setSourcingRecommendationSent((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 36:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setSourcingRecommendationApproved("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setSourcingRecommendationApproved("");
					}else{
						macData.setSourcingRecommendationApproved((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 37:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanSupplierAward("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanSupplierAward("");
					}else{
						macData.setPlanSupplierAward((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 38:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualSupplierAward("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualSupplierAward("");
					}else{
						macData.setActualSupplierAward((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 39:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualRFQ("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualRFQ("");
					}else{
						macData.setActualRFQ((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 40:
				macData.setSystemRfi_Rfq((String)value);
				break ;
			case 41:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualQuoteReceived("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualQuoteReceived("");
					}else{
						macData.setActualQuoteReceived((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 42:
				macData.setQuote((String)value);
				break ;
			case 43:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualEDDorder("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualEDDorder("");
					}else{
						macData.setActualEDDorder((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 44:
				macData.seteddOrderCurrency((String)value);
				break ;
			case 45:
				macData.seteddOrderValue((String)value);
				break ;
			case 46:
				macData.seteddOrder((String)value);
				break ;
			case 47:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualToolingOrder("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualToolingOrder("");
					}else{
						macData.setActualToolingOrder((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 48:
				macData.setTCCurrency((String)value);
				break ;
			case 49:
				macData.setTOValue((String)value);
				break ;
			case 50:
				macData.setTO((String)value);
				break ;
			case 51:
				macData.setPieceCost((String)value);
				break ;
			case 52:
				macData.setPieceCurr((String)value);
				break ;
			case 53:
				macData.setPieceStatus((String)value);
				break ;
			case 54:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPlanContractSigned("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPlanContractSigned("");
					}else{
						macData.setPlanContractSigned((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 55:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setActualContractSigned("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setActualContractSigned("");
					}else{
						macData.setActualContractSigned((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 56:
				macData.setContractSigned((String)value);
				break ;
			case 57:
				macData.setpreProductionDemand((String)value);
				break ;
			case 58:
				macData.setPTODemand((String)value);
				break ;
		
			case 59:
				tempValue = ((String)value);
				try {
					if(tempValue.equals("")){
						macData.setPTO("");
						break;
					}
					Date date = format.parse(tempValue);
					if (!format.format(date).equals(tempValue)) {
						JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
						macData.setPTO("");
					}else{
						macData.setPTO((String)value);
					}
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(null, tempValue + " is not a valid format for " + pattern, "Invalid Pattern", 1);
					ex.printStackTrace();
				}
				break ;
			case 60:
				macData.setPurchasingComments((String)value);
				break ;
			case 61:
				macData.setPORelease((String)value);
				break ;

			}

			fireTableCellUpdated(row, col);
		}
	}

	public JPopupMenu rightClickOperation(int row,int col)
	{
		rowindex = row;
		colindex = col;
		Icon copy_Icon = new ImageIcon(getClass().getResource("/icons/copy_16.png"));
		Icon paste_Icon = new ImageIcon(getClass().getResource("/icons/paste_16.png"));
		JPopupMenu popup=new JPopupMenu();
		JMenuItem copy = new JMenuItem("Copy");
		copy.setIcon(copy_Icon);
		JMenuItem paste = new JMenuItem("Paste");
		paste.setIcon(paste_Icon);
		JMenuItem pasteAll = new JMenuItem("Paste All");
		pasteAll.setIcon(paste_Icon);
		table.setComponentPopupMenu(popup);
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				copyCol = table.getModel().getValueAt(rowindex, colindex);

			}
		});
		paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				table.getModel().setValueAt(copyCol, rowindex, colindex);
				//JOptionPane.showMessageDialog(null, "Paste scucessfuly");
			}
		});
		pasteAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				for(int i=0;i<table.getRowCount();i++){
					table.getModel().setValueAt(copyCol, i, colindex);
				}
			}
		});
		table.clearSelection();
		popup.add(copy);
		popup.add(paste);
		popup.add(pasteAll);
		return popup;
	}

}

