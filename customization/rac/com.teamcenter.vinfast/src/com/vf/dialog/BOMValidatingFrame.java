package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.JTextField;
import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.Scrollbar;
import java.awt.Panel;

import javax.swing.BoxLayout;

import java.awt.Font;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class BOMValidatingFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4159227140635173153L;

	private JPanel panelContent;
	private JButton btnOK;
	private JButton btnCancel;
	private JTable tableRuleVaidate;
	private JTable tableAvaProp;
	private JTable tableExtraInfo;
	private JScrollPane scrollPaneRight;
	private List<String> selectedProp;
	private static final int BOOLEAN_COL = 1;
	private static final Object objAvailableProp[] = { "Property", "" };
	private static final Object objRuleValidate[] = { "Rule", "" };
	private static final Object objRuleDescript[] = { "Rule Decription", "" };
	private static final Object objExtraInfo[] = {"Extra Information", "" };
	private static String ruleDescription  = "";
	
	private DefaultTableModel moAvaProp = new DefaultTableModel(null, objAvailableProp) {

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == BOOLEAN_COL) {
				return Boolean.class;
			} else {
				return String.class;
			}
		}
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return true;
			}
			return false;
		}
	};
	private DefaultTableModel moExtraInfo = new DefaultTableModel(null, objExtraInfo) {

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == BOOLEAN_COL) {
				return Boolean.class;
			} else {
				return String.class;
			}
		}
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return true;
			}
			return false;
		}
	};
	private DefaultTableModel moRuleValidate = new DefaultTableModel(null, objRuleValidate) {

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == BOOLEAN_COL) {
				return Boolean.class;
			} else {
				return String.class;
			}
		}
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return true;
			}
			return false;
		}
	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Testing code
					LinkedHashMap<String, String> testing1 = new LinkedHashMap<String, String>();
					testing1.put("testing", "testing1");
					testing1.put("testing1234", "testing2");
					LinkedHashMap<Integer, String> testing2 = new LinkedHashMap<Integer, String>();
					LinkedHashMap<Integer, String> testing3 = new LinkedHashMap<Integer, String>();
					testing3.put(1, "extra1");
					testing3.put(2, "extra2");
					BOMValidatingFrame frame = new BOMValidatingFrame(testing1, testing3, testing1);
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
	public BOMValidatingFrame(final LinkedHashMap<String, String> avaiProp, final LinkedHashMap<Integer, String> extraInfo, final LinkedHashMap<String, String> ruleValidate) {
		/* Init variable */
		Set<String> keys = avaiProp.keySet();
		for (String k : keys) {
			moAvaProp.addRow(new Object[] { k, false });
		}
		Set<Integer> keys2 = extraInfo.keySet();
		for (Integer k : keys2) {
			moExtraInfo.addRow(new Object[] { extraInfo.get(k), false });
		}
		Set<String> keys3 = ruleValidate.keySet();
		for (String k : keys3) {
			moRuleValidate.addRow(new Object[] { k, false });
		}
		
		this.scrollPaneRight = new JScrollPane();
        this.selectedProp = new ArrayList<String>();
		this.btnOK = new JButton("OK");
		this.btnCancel = new JButton("Cancel");

		Border blackline = BorderFactory.createLineBorder(Color.black);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 680);
		setMinimumSize(new Dimension(750, 750));
		setTitle("BOM Export");
		setResizable(true);
		this.panelContent = new JPanel();
		this.panelContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.panelContent.setLayout(new BorderLayout(0, 0));
		setContentPane(this.panelContent);


		/* TOP LAYOUT - include text search and button search */
		JPanel panelTop = new JPanel();

		FlowLayout flowLayout = (FlowLayout) panelTop.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(10);
		this.panelContent.add(panelTop, BorderLayout.NORTH);

		JLabel lblDescript = new JLabel(
				"<html>Please select property, extra information, rule validation which do you want to expose report.<br/>Time to generate report will depend to how many option you choosen</html>");
		lblDescript.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblDescript.setHorizontalAlignment(SwingConstants.CENTER);
		panelTop.add(lblDescript);

		/* BOTTLE LAYOUT - include OK and Cancel button */
		JPanel panelBottom = new JPanel();
		FlowLayout botLayout = (FlowLayout) panelBottom.getLayout();
		botLayout.setVgap(20);
		this.panelContent.add(panelBottom, BorderLayout.SOUTH);

		this.btnOK.setPreferredSize(new Dimension(100, 30));
		panelBottom.add(btnOK);

		this.btnCancel.setPreferredSize(new Dimension(100, 30));
		panelBottom.add(btnCancel);
		/* END BOTTLE LAYOUT - include OK and Cancel button */
		
		/* CENTER LAYOUT */
		JPanel panelCenter = new JPanel();
		this.panelContent.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new GridLayout(0, 3, 0, 0));
		
		/* CENTER LAYOUT - left panel */
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(blackline);
		panelCenter.add(panelLeft);
		panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.Y_AXIS));

		JLabel lblAvailableProperty = new JLabel("Available Property");
		panelLeft.add(lblAvailableProperty);
		lblAvailableProperty.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblAvailableProperty.setAlignmentY(Component.TOP_ALIGNMENT);
		lblAvailableProperty.setFont(new Font("Tahoma", Font.PLAIN, 15));

		JPanel panelAvaProp = new JPanel();
		panelLeft.add(panelAvaProp);
		panelAvaProp.setLayout(new GridLayout(0, 1, 0, 0));
		JScrollPane scrollPaneLeftTop = new JScrollPane();
		scrollPaneLeftTop.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneLeftTop.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelAvaProp.add(scrollPaneLeftTop);
		
		this.tableAvaProp = new JTable(moAvaProp);
		tableAvaProp.setAutoCreateRowSorter(true);
        TableColumn tc = tableAvaProp.getColumnModel().getColumn(BOOLEAN_COL);
        tc.setHeaderRenderer(new SelectAllHeader(tableAvaProp, BOOLEAN_COL));
        scrollPaneLeftTop.setViewportView(tableAvaProp);
        
        
        JPanel panelExtraInfo = new JPanel();
        panelExtraInfo.setPreferredSize(new Dimension(100, 100));
        
        JLabel lblExtraInfo = new JLabel("Extra Information");
        lblExtraInfo.setAlignmentY(Component.TOP_ALIGNMENT);
        lblExtraInfo.setToolTipText("");
        lblExtraInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblExtraInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblExtraInfo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		panelLeft.add(lblExtraInfo);
		
		panelExtraInfo.setLayout(new GridLayout(0, 1, 0, 0));
		JScrollPane scrollPaneLeftBot = new JScrollPane();
		scrollPaneLeftBot.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneLeftBot.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelExtraInfo.add(scrollPaneLeftBot);
		
		panelLeft.add(panelExtraInfo);
		
		this.tableExtraInfo = new JTable(moExtraInfo);
		tableExtraInfo.setAutoCreateRowSorter(true);
        TableColumn tc3 = tableExtraInfo.getColumnModel().getColumn(BOOLEAN_COL);
        tc3.setHeaderRenderer(new SelectAllHeader(tableExtraInfo, BOOLEAN_COL));
        scrollPaneLeftBot.setViewportView(tableExtraInfo);
        
		/* CENTER LAYOUT - end left panel */
		
		/* CENTER LAYOUT - middle panel */
		JPanel panelMid = new JPanel();
		panelMid.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
		panelCenter.add(panelMid);
		panelMid.setLayout(new BoxLayout(panelMid, BoxLayout.Y_AXIS));

		JLabel lblRuleValidating = new JLabel("Rule Validating");
		panelMid.add(lblRuleValidating);
		lblRuleValidating.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblRuleValidating.setAlignmentY(Component.TOP_ALIGNMENT);
		lblRuleValidating.setFont(new Font("Tahoma", Font.PLAIN, 15));

		JPanel panelruleValidate = new JPanel();
		panelMid.add(panelruleValidate);
		panelruleValidate.setLayout(new GridLayout(0, 1, 0, 0));
		JScrollPane scrollPaneMid = new JScrollPane();
		scrollPaneMid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneMid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelruleValidate.add(scrollPaneMid);

		this.tableRuleVaidate = new JTable(moRuleValidate);
		tableRuleVaidate.setAutoCreateRowSorter(true);
        TableColumn tc1 = tableRuleVaidate.getColumnModel().getColumn(BOOLEAN_COL);
        tc1.setHeaderRenderer(new SelectAllHeader(tableRuleVaidate, BOOLEAN_COL));
        tableRuleVaidate.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				System.out.println(tableRuleVaidate.getValueAt(tableRuleVaidate.getSelectedRow(), 0).toString());
				System.out.println("clicked....");
			}
        });
        
        tableRuleVaidate.addMouseListener(new java.awt.event.MouseAdapter(){
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				int row = tableRuleVaidate.rowAtPoint(e.getPoint());
				int col = tableRuleVaidate.columnAtPoint(e.getPoint());
				ruleDescription = ruleValidate.get(tableRuleVaidate.getValueAt(row, col).toString());
				System.out.println(" Value in the cell clicked :" + " " + ruleDescription);
				
				JTextPane txtDescription = new JTextPane();
				txtDescription.setText(ruleDescription);
				scrollPaneRight.setViewportView(txtDescription);
			}
		});
        
        scrollPaneMid.setViewportView(tableRuleVaidate);
        /* CENTER LAYOUT - end middle panel */
        
        /* CENTER LAYOUT - right panel */
		JPanel panelRight = new JPanel();
		panelRight.setBorder(blackline);
		panelCenter.add(panelRight);
		panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.Y_AXIS));

		JLabel lblRuleDescription = new JLabel("Rule Description");
		panelRight.add(lblRuleDescription);
		lblRuleDescription.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblRuleDescription.setAlignmentY(Component.TOP_ALIGNMENT);
		lblRuleDescription.setFont(new Font("Tahoma", Font.PLAIN, 15));

		JPanel panelRuleDescript = new JPanel();
		panelRight.add(panelRuleDescript);
		panelRuleDescript.setLayout(new GridLayout(0, 1, 0, 0));
		scrollPaneRight.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneRight.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelRuleDescript.add(scrollPaneRight);
        /* CENTER LAYOUT - end right panel */
		
		/* END CENTER LAYOUT */
	}

	public JTable getTableExtraInfo() {
		return tableExtraInfo;
	}

	public void setTableExtraInfo(JTable tableExtraInfo) {
		this.tableExtraInfo = tableExtraInfo;
	}

	public JTable getTableAvaProp() {
		return tableAvaProp;
	}

	public void setTableAvaProp(JTable tableAvaProp) {
		this.tableAvaProp = tableAvaProp;
	}

	public List<String> getSelectedProp() {
		return selectedProp;
	}

	public void setSelectedProp(List<String> selectedProp) {
		this.selectedProp = selectedProp;
	}

	public JButton getBtnOK() {
		return btnOK;
	}

	public void setBtnOK(JButton btnOK) {
		this.btnOK = btnOK;
	}

	public JButton getBtnCancel() {
		return btnCancel;
	}

	public void setBtnCancel(JButton btnCancel) {
		this.btnCancel = btnCancel;
	}

	public JTable getTableRuleVaidate() {
		return tableRuleVaidate;
	}

	public void setTableRuleVaidate(JTable tableRuleVaidate) {
		this.tableRuleVaidate = tableRuleVaidate;
	}

	/**
	 * A TableCellRenderer that selects all or none of a Boolean column.
	 * 
	 * @param targetColumn
	 *            the Boolean column to manage
	 */
	class SelectAllHeader extends JToggleButton implements TableCellRenderer {

		private static final String ALL = "Select all";
		private static final String NONE = "Select none";
		private JTable table;
		private TableModel tableModel;
		private JTableHeader header;
		private TableColumnModel tcm;
		private int targetColumn;
		private int viewColumn;

		public SelectAllHeader(JTable table, int targetColumn) {
			super(ALL);
			this.table = table;
			this.tableModel = table.getModel();
			if (tableModel.getColumnClass(targetColumn) != Boolean.class) {
				throw new IllegalArgumentException("Boolean column required.");
			}
			this.targetColumn = targetColumn;
			this.header = table.getTableHeader();
			this.tcm = table.getColumnModel();
			this.applyUI();
			this.addItemListener(new ItemHandler());
			header.addMouseListener(new MouseHandler());
			tableModel.addTableModelListener(new ModelHandler());
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}

		private class ItemHandler implements ItemListener {

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean state = e.getStateChange() == ItemEvent.SELECTED;
				setText((state) ? NONE : ALL);
				for(int i = 0; i < table.getRowCount(); i++){
	            	for(int j = 0; j < table.getColumnCount(); j++){
	            		table.getValueAt(i, j);
	            		if(table.getValueAt(i,j) instanceof Boolean){
	            			table.setValueAt(state, i, j);
	            		}
	            	}
	            }
			}
		}

		@Override
		public void updateUI() {
			super.updateUI();
			applyUI();
		}

		private void applyUI() {
			this.setFont(UIManager.getFont("TableHeader.font"));
			this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			this.setBackground(UIManager.getColor("TableHeader.background"));
			this.setForeground(UIManager.getColor("TableHeader.foreground"));
		}

		private class MouseHandler extends MouseAdapter {

			@Override
			public void mouseClicked(MouseEvent e) {
				viewColumn = header.columnAtPoint(e.getPoint());
				int modelColumn = tcm.getColumn(viewColumn).getModelIndex();
				if (modelColumn == targetColumn) {
					doClick();
				}
			}
		}

		private class ModelHandler implements TableModelListener {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (needsToggle()) {
					doClick();
					header.repaint();
				}
			}
		}

		// Return true if this toggle needs to match the model.
		private boolean needsToggle() {
			boolean allTrue = true;
			boolean allFalse = true;
			for (int r = 0; r < tableModel.getRowCount(); r++) {
				boolean b = (Boolean) tableModel.getValueAt(r, targetColumn);
				allTrue &= b;
				allFalse &= !b;
			}
			return allTrue && !isSelected() || allFalse && isSelected();
		}
	}
}
