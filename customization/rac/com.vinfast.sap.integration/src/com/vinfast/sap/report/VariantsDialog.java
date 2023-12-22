package com.vinfast.sap.report;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.combobox.iComboBox;

public class VariantsDialog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TCSession session = null ;
	TCComponentBOMLine interfaceAIFComp = null ;
	iComboBox category = null;
	TCComponent[] variantRule = null;
	TCComponentBOMLine topLine = null;
	
	public void createAndShowGUI(TCSession session,TCComponentBOMLine Line ) throws Exception {

		topLine = Line;
		setTitle("Variant Export");
		JPanel panel = new JPanel();
		ImageIcon  frame_Icon = new ImageIcon(getClass().getResource("/icons/Export_16.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));
		panel.setLayout(null);
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(370, 130));

		variantRule = getVariants(topLine);

		JLabel label = new JLabel("Choose Variant:");
		label.setBounds(20, 20, 100, 25);
		category = new iComboBox(variantRule);
		category.addItem("MBOM 150%");
		category.setBounds(120, 20, 210, 25);

		JSeparator separator =  new JSeparator();
		separator.setBounds(10, 80, 360, 25);

		JButton JB_Save =  new JButton("Save");
		JB_Save.setIcon(save_Icon);
		JB_Save.setBounds(90, 90, 90, 25);

		JB_Save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedRule = category.getSelectedItem().toString();
				applyVariantsToBOMWindow(topLine,selectedRule);
				dispose();
			}

		});

		JButton JB_Cancel =  new JButton("Cancel");
		JB_Cancel.setIcon(cancel_Icon);
		JB_Cancel.setBounds(190, 90, 90, 25);
		JB_Cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		});

		panel.add(label);
		panel.add(category);
		panel.add(separator);
		panel.add(JB_Save);
		panel.add(JB_Cancel);

		getContentPane().add(panel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public TCComponent[] getVariants(TCComponentBOMLine topLine){

		TCComponentCfg0ProductItem context = (TCComponentCfg0ProductItem)topLine.getConfiguratorContext();
		TCComponent[] variantRules = null;
		try {
			variantRules = context.getReferenceListProperty("IMAN_reference");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return variantRules;
	}

	protected void applyVariantsToBOMWindow(TCComponentBOMLine topLine, String rule)
	{
		//ClassLoader classLoader = getClass().getClassLoader();
		//URL resource = classLoader.getResource("lib/variantexport.jar");
		//File jarFile  = new File(resource.getFile());
		String env = System.getenv("FMS_HOME");
		env  = env.replace("tccs", "portal\\plugins\\variantexport.jar");
		String temp = System.getProperty("java.io.tmpdir");
		String ID = "";
		String name = "";
		String jarexecute = "";
		try {
			ID = topLine.getItemRevision().getUid();
			String split[] = rule.split("-");
			name = split[0];
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*String[] arg = new String[6];
		arg[0] = ID;
		arg[1] = "MBOM";
		arg[2] = name;
		arg[3] = rule;
		arg[4] = temp;
		arg[5] = "true";
		String jarexecute = "-i "+ID+" -BomType MBOM -VehicleType "+name+" -V \""+rule+"\" -ReportTemplate D:\\_Exported\\xBOM\\reportField.cfg -o "+temp+" -EXCLUDE150";
		ImportExport IE =  new ImportExport();
		IE.startOperation(arg);*/

		if(category.getSelectedItem().toString().equals("MBOM 150%")){
			jarexecute = "-i "+ID+" -BomType MBOM -VehicleType "+name+" -V NoVariant -ReportTemplate D:\\_Exported\\xBOM\\reportField.cfg -o "+temp;
		}else{
			jarexecute = "-i "+ID+" -BomType MBOM -VehicleType "+name+" -V \""+rule+"\" -ReportTemplate D:\\_Exported\\xBOM\\reportField.cfg -o "+temp+" -EXCLUDE150";
		}

		try {
			String filepath = temp+"\\"+"variantexport.bat";
			File file = new File(filepath);
			file.createNewFile();
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.println("java -jar "+env+" "+jarexecute);
			writer.println("exit");
			writer.close();
			Process p = Runtime.getRuntime().exec("cmd /c start /W "+file.getPath());
			p.waitFor();
			p.destroy();
			file.delete();
			MessageBox.post("File created:- "+temp,"Information", MessageBox.INFORMATION);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(jarexecute);
	}
}
