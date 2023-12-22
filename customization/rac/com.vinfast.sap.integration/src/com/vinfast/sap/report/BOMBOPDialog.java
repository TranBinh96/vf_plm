package com.vinfast.sap.report;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.iButton;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.vinfast.sap.util.UIGetValuesUtility;

public class BOMBOPDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TCSession session = null ;
	TCComponentBOMLine interfaceAIFComp = null ;
	TCComponentBOMLine topLine = null;

	public void createAndShowGUI(TCSession session) throws Exception {
		UIGetValuesUtility util		 = new UIGetValuesUtility();
		setTitle("BOM BOP Export");
		JPanel panel = new JPanel();
		ImageIcon  frame_Icon = new ImageIcon(getClass().getResource("/icons/Export_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));
		Icon file_Icon = new ImageIcon(getClass().getResource("/icons/foldertype_16.png"));
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(370, 180));


		String[] BOM = {"ID1U-SUV","ID14-SEDAN","IA15-ACAR"};

		JLabel label = new JLabel("Model:");
		label.setBounds(20, 20, 100, 25);

		iComboBox category = new iComboBox(BOM);
		category.setBounds(100, 20, 100, 25);
		category.setMandatory(true);


		JLabel Ylabel = new JLabel("Year:");
		Ylabel.setBounds(210, 20, 50, 25);

		iComboBox Ycategory = new iComboBox(new String[]{"2019","2020"});
		Ycategory.setBounds(250, 20, 70, 25);
		Ycategory.setMandatory(true);
		Ycategory.setEditable(false);
		Ycategory.setSelectedIndex(0);

		String[] shops = {"3001-BODY SHOP","3001-PAINT SHOP","3001-GA SHOP","3001-SA SHOP","3003-PRESS SHOP","3002-ENGINE SHOP"};
		String[] ACARShops = {"3001-BODY SHOP","3001-PAINT SHOP","3001-GA SHOP","3001-SA SHOP"};
		JLabel labelShop = new JLabel("Choose Shop:");
		labelShop.setBounds(20, 50, 100, 25);
		iComboBox shop = new iComboBox(shops);
		shop.setBounds(100, 50, 220, 25);
		shop.setMandatory(true);
		shop.setEnabled(false);

		JLabel labelExport = new JLabel("Export Path:");
		labelExport.setBounds(20, 80, 100, 25);
		iTextField path = new iTextField();
		path.setBounds(100, 80, 190, 25);
		path.setEditable(false);
		path.setRequired(true);

		iButton file =  new iButton(file_Icon);
		file.setBounds(295, 80, 25, 25);
		file.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fileChooser.showDialog(getContentPane(), "Select");
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					path.setText(selectedFile.getAbsolutePath());
				}
			}

		});

		JSeparator separator =  new JSeparator();
		separator.setBounds(10, 120, 350, 25);

		JButton JB_Save =  new JButton("Export");
		JB_Save.setIcon(frame_Icon);
		JB_Save.setBounds(90, 140, 90, 25);

		JB_Save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(category.getSelectedIndex() == -1){
					MessageBox.post("Please select Model and Click on \"Export\".", "Error", MessageBox.ERROR);
					return;
				}
				if(shop.getSelectedIndex() == -1){
					MessageBox.post("Please select Shop and Click on \"Export\".", "Error", MessageBox.ERROR);
					return;
				}
				if(path.getText().length() == 0){
					MessageBox.post("Please file export location and Click on \"Export\".", "Error", MessageBox.ERROR);
					return;
				}

				HashMap<Date, TCComponent> datasetMap =  null;
				String filepath = path.getText();
				String[] platformCode = category.getSelectedItem().toString().split("-");
				String modelYear = Ycategory.getSelectedItem().toString();
				String[] plantModel = shop.getSelectedItem().toString().split("-");
				String fileName = platformCode[0]+"_"+plantModel[1].trim().replace(" ", "_")+"_"+UIGetValuesUtility.getSequenceID();
				
				try{
				
					String dataset_name = plantModel[0].trim()+"_BOM_"+platformCode[0]+"_"+modelYear+"_"+plantModel[1].trim().replace(" ", "_");

					TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(session, new String[]{"Name","Dataset Type"}, new String[]{dataset_name,"Text"}, "Dataset...");

					if(oldDatasets != null){

						datasetMap =  new HashMap<Date, TCComponent>();

						LinkedHashMap<String, String> shopBOMStructure = null;

						for(TCComponent dataset : oldDatasets){

							Date date_creation = dataset.getDateProperty("creation_date");
							datasetMap.put(date_creation, dataset);
						}

						ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet()); 

						Collections.sort(sortedKeys, Collections.reverseOrder()); 

						TCComponentDataset olddataset = (TCComponentDataset)datasetMap.get(sortedKeys.get(0));

						File oldBOMFile = UIGetValuesUtility.downloadDataset(session,System.getProperty("java.io.tmpdir"), olddataset);

						if(oldBOMFile == null){

							MessageBox.post("Error in loading dataset. Please contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
							return;
						}else{

							shopBOMStructure = util.readOldFile(session, oldBOMFile);
							if(shopBOMStructure == null){

								MessageBox.post("Error in parsing dataset. Please contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
								return;
							}else{
								printToExcel(shopBOMStructure, filepath, fileName);
							}
						}
					}
				}
				catch (TCException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dispose();
			}

		});

		JButton JB_Cancel =  new JButton("Cancel");
		JB_Cancel.setIcon(cancel_Icon);
		JB_Cancel.setBounds(190, 140, 90, 25);
		JB_Cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		});

		category.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(category.getSelectedItem().toString().equals("") ==  false){

					shop.setEnabled(true);
					shop.removeAllItems();
					if(category.getSelectedItem().toString().equals("IA15-ACAR")){

						shop.addItems(ACARShops);
						Ycategory.setSelectedIndex(1);
					}else{
						shop.addItems(shops);
						Ycategory.setSelectedIndex(0);
					}

				}else{
					shop.setEnabled(false);
				}

			}

		});

		panel.add(label);
		panel.add(category);
		panel.add(Ylabel);
		panel.add(Ycategory);
		panel.add(labelShop);
		panel.add(shop);
		panel.add(labelExport);
		panel.add(path);
		panel.add(separator);
		panel.add(JB_Save);
		panel.add(JB_Cancel);
		panel.add(file);

		getContentPane().add(panel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void printToExcel(LinkedHashMap<String, String> BOMvalues, String filePath, String fileName){

		//Create blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		//Create a blank sheet
		XSSFSheet spreadsheet = workbook.createSheet(fileName);

		//Create row object
		XSSFRow row;

		//This data needs to be written (Object[])
		CellStyle backgroundStyle = workbook.createCellStyle();

		Font font = workbook.createFont();
	    font.setFontHeightInPoints((short) 12);
	    font.setBold(true);
	    font.setFontName("Arial");
	    
		backgroundStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
		backgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		backgroundStyle.setAlignment(HorizontalAlignment.CENTER);
		backgroundStyle.setFont(font);
	    
		Map < String, String[] > headerinfo = new TreeMap < String, String[] >();
		headerinfo.put( "1", new String[] {"MAIN GROUP", "SUB GROUP", "PART NUMBER", "BOMLINE ID", "PLANT", "QUANTITY", "VARIANT","MODEL", "YEAR", "BOPID", "REVISION", "WORKSTATION", "LINESUPPLYMETHOD", "MES INDICATOR","FAMILY ADDRESS", "L R HAND"});

		//Iterate over data and write to sheet
		Set < String > keyid = headerinfo.keySet();
		int rowid = 0;

		for (String key : keyid) {
			
			row = spreadsheet.createRow(rowid++);
			String [] objectArr = headerinfo.get(key);
			int cellid = 0;

			for (String obj : objectArr){
				XSSFCell cell = row.createCell(cellid++);
				cell.setCellValue(new XSSFRichTextString(obj));
				cell.setCellStyle(backgroundStyle);
			}
		}
		//Header FINISH
		
		Set < String > BOMid = BOMvalues.keySet();
		int BOMrowcount = 1;

		for (String key : BOMid) {
			
			row = spreadsheet.createRow(BOMrowcount++);
			String values = BOMvalues.get(key);
			String[] keyvalues = key.split("~");
			String[] objectArr = sortPatternValues(values.split("~"));
			
			int cellid = 0;
			
			for (String obj : keyvalues){
				XSSFCell cell = row.createCell(cellid++);
				cell.setCellValue(obj);
			}
			
			
			for (String obj : objectArr){
				
				XSSFCell cell = row.createCell(cellid++);
				cell.setCellValue(obj);
			}
		}
		
		//Write the workbook in file system
		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(filePath+"//"+fileName+".xlsx"));
			workbook.write(out);
			workbook.close();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MessageBox.post(fileName+".xlsx written successfully", "Success", MessageBox.INFORMATION);
		
		System.out.println(fileName+".xlsx written successfully");

	}
	
	public String[] sortPatternValues(String[] objectArr){
		
		String[] ouput = new String[12];
		
		if(objectArr.length !=0){
			
			ouput[0] = objectArr[0];
			ouput[1] = objectArr[1];
			ouput[2] = objectArr[2];
			ouput[3] = objectArr[4];
			ouput[4] = objectArr[6];
			ouput[5] = objectArr[7];
			ouput[6] = objectArr[14];
			ouput[7] = objectArr[8];
			ouput[8] = objectArr[9];
			ouput[9] = objectArr[10];
			ouput[10] = objectArr[11];
			ouput[11] = objectArr[12];
			
		}
		
		return ouput;
	}
	}
