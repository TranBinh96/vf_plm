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

public class BOMReportFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4159227140635173153L;

	private JPanel panelContent;
	private JButton btnOK;
	private JButton btnCancel;
	private JTable tableExtraInfo;
	private JTable tableAvaProp;
	private List<String> selectedProp;
	private static final int BOOLEAN_COL = 1;
	private static final Object leftTableColumnName[] = { "Property", "" };
	private static final Object rightTableColumnName[] = { "Extra Information", "" };
	private DefaultTableModel moAvaProp = new DefaultTableModel(null, leftTableColumnName) {

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
	private DefaultTableModel moExtraInfo = new DefaultTableModel(null, rightTableColumnName) {

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
					BOMReportFrame frame = new BOMReportFrame(testing1, testing2);
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
	public BOMReportFrame(final LinkedHashMap<String, String> avaiProp, final LinkedHashMap<Integer, String> extraInfo) {
		/* Init variable */
		Set<String> keys = avaiProp.keySet();
        for(String k:keys){
        	  moAvaProp.addRow(new Object[]{ k , false});
        }
        Set<Integer>keys2 = extraInfo.keySet();
        for(Integer k:keys2){
        	  moExtraInfo.addRow(new Object[]{ extraInfo.get(k) , false});
        }
		
        this.selectedProp = new ArrayList<String>();
		this.btnOK = new JButton("OK");
		this.btnCancel = new JButton("Cancel");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 680);
		setTitle("BOM Export");
		setResizable(false);
		this.panelContent = new JPanel();
		this.panelContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.panelContent.setLayout(new BorderLayout(0, 0));
		setContentPane(this.panelContent);

		Border blackline = BorderFactory.createLineBorder(Color.black);

		/* TOP LAYOUT - include text search and button search */
		JPanel panelTop = new JPanel();

		FlowLayout flowLayout = (FlowLayout) panelTop.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(10);
		this.panelContent.add(panelTop, BorderLayout.NORTH);

		JLabel lblDescript = new JLabel(
				"<html>Please select property and extra information which do you want to expose report.<br/>Time to generate report will depend to how many option you choosen</html>");
		lblDescript.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblDescript.setHorizontalAlignment(SwingConstants.CENTER);
		panelTop.add(lblDescript);

		/* BOT LAYOUT - include OK and Cancel button */
		JPanel panelBottom = new JPanel();
		FlowLayout botLayout = (FlowLayout) panelBottom.getLayout();
		botLayout.setVgap(20);
		this.panelContent.add(panelBottom, BorderLayout.SOUTH);

		this.btnOK.setPreferredSize(new Dimension(100, 30));
		panelBottom.add(btnOK);

		this.btnCancel.setPreferredSize(new Dimension(100, 30));
		panelBottom.add(btnCancel);

		/* CENTER LAYOUT */
		JPanel panelCenter = new JPanel();
		this.panelContent.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new GridLayout(0, 2, 0, 0));

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

		JPanel panelRight = new JPanel();
		panelRight.setBorder(blackline);
		panelCenter.add(panelRight);
		panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.Y_AXIS));

		JLabel lblAvailableExtraInformation = new JLabel("Available Extra Information");
		panelRight.add(lblAvailableExtraInformation);
		lblAvailableExtraInformation.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblAvailableExtraInformation.setAlignmentY(Component.TOP_ALIGNMENT);
		lblAvailableExtraInformation.setFont(new Font("Tahoma", Font.PLAIN, 15));

		JPanel panelExtraInfo = new JPanel();
		panelRight.add(panelExtraInfo);
		panelExtraInfo.setLayout(new GridLayout(0, 1, 0, 0));

		JScrollPane scrollPaneRight = new JScrollPane();
		scrollPaneRight.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneRight.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelExtraInfo.add(scrollPaneRight);

		this.tableExtraInfo = new JTable(moExtraInfo);
		tableExtraInfo.setAutoCreateRowSorter(true);
        TableColumn tc1 = tableExtraInfo.getColumnModel().getColumn(BOOLEAN_COL);
        tc1.setHeaderRenderer(new SelectAllHeader(tableExtraInfo, BOOLEAN_COL));
		scrollPaneRight.setViewportView(tableExtraInfo);

		JScrollPane scrollPaneLeft = new JScrollPane();
		scrollPaneLeft.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneLeft.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelAvaProp.add(scrollPaneLeft);

		this.tableAvaProp = new JTable(moAvaProp);
		tableAvaProp.setAutoCreateRowSorter(true);
        TableColumn tc = tableAvaProp.getColumnModel().getColumn(BOOLEAN_COL);
        tc.setHeaderRenderer(new SelectAllHeader(tableAvaProp, BOOLEAN_COL));
		scrollPaneLeft.setViewportView(tableAvaProp);

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
