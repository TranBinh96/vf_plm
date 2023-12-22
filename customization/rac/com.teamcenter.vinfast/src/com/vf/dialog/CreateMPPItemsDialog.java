/*
 * #109226		Change Program code to array (list)		15-11-2023
 * 
 * 
 * 
 * 
 */

package com.vf.dialog;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.vinfast.utils.lov.VFLovService;
import com.teamcenter.vinfast.utils.lov.VFLovValues;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;


public class CreateMPPItemsDialog extends TitleAreaDialog {

	private TCSession session = null;
	private static String PROP_PROGRAM =  "vf4_program_model_name_ar";
	private Button btn_Create = null;
	private TCComponent selectedObject = null;
	private Combo cb_Plant = null;
	private List lst_Program = null;
	private Combo cb_ModelYear = null;
	private Text txt_Name = null;
	private Text txt_ShopName = null;
	private Text txt_ShowName = null;
	private Button[] radios = null;
	public CreateMPPItemsDialog(Shell parentShell, TCSession _session, TCComponent _type) {
		super(parentShell);
		session = _session;
		selectedObject = _type;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite area = (Composite) super.createDialogArea(parent);
		switch(selectedObject.getTypeObject().getClassName()){    
		case "Folder":    
			setTitle("Plant BOP Create Information");
			setMessage("Please fill values to create Item", IMessageProvider.INFORMATION); 
			buildTopLinePanel(area);
			break; 
		case "Mfg0BvrPlantBOP":
			setTitle("Shop Create Information");
			setMessage("Please fill values to create shop", IMessageProvider.INFORMATION); 
			buildAreaPanel(area);
			break; 
		case "Mfg0BvrProcessArea":
			setTitle("Line Create Information");
			setMessage("Please fill values to create line", IMessageProvider.INFORMATION); 
			buildLinePanel(area);
			break;
		case "Mfg0BvrProcessLine":
			setTitle("Station Create Information");
			setMessage("Please fill values to create station", IMessageProvider.INFORMATION); 
			buildStationPanel(area);
			break;
		default:     
			setTitle("Operation Create Information");
			setMessage("Please fill values to create operation", IMessageProvider.INFORMATION); 
			buildOperationPanel(area);
		}    

		return area;
	}

	private void buildTopLinePanel(Composite area) {

		try {
			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 100;
			container.setLayoutData(gd_container);
			
			radios = new Button[2];

		    radios[0] = new Button(container, SWT.RADIO);
		    radios[0].setSelection(true);
		    radios[0].setText("Main BOM");
		    radios[0].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		    
		    radios[1] = new Button(container, SWT.RADIO);
		    radios[1].setText("Sub Assembly BOM");
		    radios[1].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			//plant
			Label lb_Plant = new Label(container, SWT.NONE);
			lb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Plant.setText("Plant Code:");

			cb_Plant = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			addRequiredDecoration(cb_Plant);
			cb_Plant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_CHANGE_NOTICE, new String[] {PropertyDefines.ECM_PLANT});
			VFLovService.setLovValue(cb_Plant, lovValues.getLOVValue(PropertyDefines.TYPE_CHANGE_NOTICE, PropertyDefines.ECM_PLANT));

			//Program
			Label lb_Model = new Label(container, SWT.NONE);
			lb_Model.setText("Model Year:");

			cb_ModelYear = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			ControlDecoration programDecal = addRequiredDecoration(cb_ModelYear);
			cb_ModelYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_ModelYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			String[] programCodes = getProgramCodes();
			if(programCodes !=null) {
				cb_ModelYear.setItems(programCodes);
			}

			//Program
			Label lb_Program = new Label(container, SWT.NONE);
			lb_Program.setText("Program:");

			lst_Program = new List(container, SWT.V_SCROLL | SWT.MULTI| SWT.BORDER);
			addRequiredDecoration(lst_Program);
			lst_Program.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd.heightHint = 70;
			lst_Program.setLayoutData(gd);
			lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_ME_PLANTBOP_REVISION, new String[] {PROP_PROGRAM});
			VFLovService.setLovValue(lst_Program, lovValues.getLOVValue(PropertyDefines.TYPE_ME_PLANTBOP_REVISION, PROP_PROGRAM));

			//Program
			Label lb_Name = new Label(container, SWT.NONE);
			lb_Name.setText("Name:");

			txt_Name = new Text(container, SWT.BORDER);
			addRequiredDecoration(txt_Name);
			txt_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_Name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_Name.setTextLimit(110);

			Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
			//Display Name
			Label lb_ShowText = new Label(container, SWT.NONE);
			lb_ShowText.setText("Display Name:");
			lb_ShowText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));


			txt_ShowName = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt_ShowName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txt_ShowName.setEnabled(false);
			//Listeners
			cb_Plant.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					
					fillName();
				}
			});

			lst_Program.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					fillName();
				}
			});

			cb_ModelYear.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					fillName();
				}
			});
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					fillName();
				}
			});
			
			radios[0].addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if(radios[0].getSelection() == true) {
						cb_ModelYear.clearSelection();
						cb_ModelYear.setEnabled(true);
						programDecal.show();
					}
				}
			});
			
			radios[1].addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if(radios[1].getSelection() == true) {
						cb_ModelYear.clearSelection();
						cb_ModelYear.setEnabled(false);
						programDecal.hide();
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildAreaPanel(Composite area) {

		try {
			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 100;
			container.setLayoutData(gd_container);

			//plant
			Label lb_Plant = new Label(container, SWT.NONE);
			lb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Plant.setText("Plant Code:");


			cb_Plant = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			addRequiredDecoration(cb_Plant);
			cb_Plant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_CHANGE_NOTICE, new String[] {PropertyDefines.ECM_PLANT});
			VFLovService.setLovValue(cb_Plant, lovValues.getLOVValue(PropertyDefines.TYPE_CHANGE_NOTICE, PropertyDefines.ECM_PLANT));
			TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
			if(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").length() != 0) {
				cb_Plant.setText(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").substring(0,4));
				cb_Plant.setEnabled(false);
			}

			//Program
			Label lb_Program = new Label(container, SWT.NONE);
			lb_Program.setText("Program:");

			lst_Program = new List(container, SWT.V_SCROLL | SWT.MULTI| SWT.BORDER);
			addRequiredDecoration(lst_Program);
			lst_Program.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd.heightHint = 70;
			lst_Program.setLayoutData(gd);
			lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_ME_PLANTBOP_REVISION, new String[] {PROP_PROGRAM});
			VFLovService.setLovValue(lst_Program, lovValues.getLOVValue(PropertyDefines.TYPE_ME_PLANTBOP_REVISION, PROP_PROGRAM));
			BOMLine = (TCComponentBOMLine) selectedObject;
			if(BOMLine.getItemRevision().getProperty(PROP_PROGRAM).length() != 0) {
				String[] values = BOMLine.getItemRevision().getTCProperty(PROP_PROGRAM).getDisplayableValues().toArray(new String[0]);
				lst_Program.setSelection(values);
				lst_Program.setEnabled(false);
			}

			//Program
			Label lb_Name = new Label(container, SWT.NONE);
			lb_Name.setText("Shop Code:");

			txt_Name = new Text(container, SWT.BORDER);
			addRequiredDecoration(txt_Name);
			txt_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_Name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_Name.setTextLimit(2);
			txt_Name.setToolTipText("Enter 2 digit shop name (Ex: GA,SA,MF,CS....) ");

			Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
			//Display Name
			Label lb_ShowText = new Label(container, SWT.NONE);
			lb_ShowText.setText("Display Name:");
			lb_ShowText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));

			txt_ShowName = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt_ShowName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txt_ShowName.setEnabled(false);
			txt_ShowName.setText(cb_Plant.getText());
			//Listeners
			cb_Plant.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

			lst_Program.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});
			
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildLinePanel(Composite area) {

		try {
			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 100;
			container.setLayoutData(gd_container);

			//plant
			Label lb_Plant = new Label(container, SWT.NONE);
			lb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Plant.setText("Plant Code:");

			cb_Plant = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			cb_Plant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_CHANGE_NOTICE, new String[] {PropertyDefines.ECM_PLANT});
			VFLovService.setLovValue(cb_Plant, lovValues.getLOVValue(PropertyDefines.TYPE_CHANGE_NOTICE, PropertyDefines.ECM_PLANT));
			addRequiredDecoration(cb_Plant);
			//Listeners

			TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
			if(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").length() != 0) {
				cb_Plant.setText(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").substring(0,4));
				cb_Plant.setEnabled(false);
			}

			//Program
			Label lb_Program = new Label(container, SWT.NONE);
			lb_Program.setText("Program:");

			lst_Program = new List(container, SWT.V_SCROLL | SWT.MULTI| SWT.BORDER);
			lst_Program.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd.heightHint = 70;
			lst_Program.setLayoutData(gd);
			addRequiredDecoration(lst_Program);
			lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_ME_PLANTBOP_REVISION, new String[] {PROP_PROGRAM});
			VFLovService.setLovValue(lst_Program, lovValues.getLOVValue(PropertyDefines.TYPE_ME_PLANTBOP_REVISION, PROP_PROGRAM));

			lst_Program.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

			//Program
			Label lb_Name = new Label(container, SWT.NONE);
			lb_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Name.setText("Line Code:");

			txt_ShopName = new Text(container, SWT.BORDER);
			txt_ShopName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_ShopName.setTextLimit(2);
			txt_ShopName.setEnabled(false);
			addRequiredDecoration(txt_ShopName);
			txt_ShopName.setText(selectedObject.getProperty("bl_rev_object_name"));

			txt_Name = new Text(container, SWT.BORDER);
			txt_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_Name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_Name.setTextLimit(2);
			txt_Name.setToolTipText("Enter 2 digit line name (Ex: T1,T2....) ");
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(txt_ShowName.getText()+"_"+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});
			addRequiredDecoration(txt_Name);
			BOMLine = (TCComponentBOMLine) selectedObject;
			TCComponentItemRevision revision = BOMLine.window().getTopBOMLine().getItemRevision();
			if(revision.getProperty(PROP_PROGRAM).length() != 0) {
				String[] values = revision.getTCProperty(PROP_PROGRAM).getStringArrayValue();
				lst_Program.setSelection(values);
				lst_Program.setEnabled(false);
			}

			Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 3, 1));
			//Display Name
			Label lb_ShowText = new Label(container, SWT.NONE);
			lb_ShowText.setText("Display Name:");
			lb_ShowText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 3, 1));

			txt_ShowName = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt_ShowName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			txt_ShowName.setEnabled(false);
			txt_ShowName.setText(cb_Plant.getText()+"_"+selectedObject.getProperty("bl_rev_object_name"));
			//Listeners
			cb_Plant.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText().toUpperCase()+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

			lst_Program.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText().toUpperCase()+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildStationPanel(Composite area) {

		try {
			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 100;
			container.setLayoutData(gd_container);

			//plant
			Label lb_Plant = new Label(container, SWT.NONE);
			lb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Plant.setText("Plant Code:");

			cb_Plant = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			cb_Plant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_CHANGE_NOTICE, new String[] {PropertyDefines.ECM_PLANT});
			VFLovService.setLovValue(cb_Plant, lovValues.getLOVValue(PropertyDefines.TYPE_CHANGE_NOTICE, PropertyDefines.ECM_PLANT));
			addRequiredDecoration(cb_Plant);
			//Listeners

			TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
			if(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").length() != 0) {
				cb_Plant.setText(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").substring(0,4));
				cb_Plant.setEnabled(false);
			}

			//Program
			Label lb_Program = new Label(container, SWT.NONE);
			lb_Program.setText("Program:");

			lst_Program = new List(container, SWT.V_SCROLL | SWT.MULTI| SWT.BORDER);
			lst_Program.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd.heightHint = 70;
			lst_Program.setLayoutData(gd);
			lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_ME_PLANTBOP_REVISION, new String[] {PROP_PROGRAM});
			VFLovService.setLovValue(lst_Program, lovValues.getLOVValue(PropertyDefines.TYPE_ME_PLANTBOP_REVISION, PROP_PROGRAM));
			addRequiredDecoration(lst_Program);
			
			BOMLine = (TCComponentBOMLine) selectedObject;
			TCComponentItemRevision revision = BOMLine.window().getTopBOMLine().getItemRevision();
			if(revision.getProperty(PROP_PROGRAM).length() != 0) {
				String[] values = revision.getTCProperty(PROP_PROGRAM).getStringArrayValue();
				lst_Program.setSelection(values);;
				lst_Program.setEnabled(false);
			}

			lst_Program.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

			//Program
			Label lb_Name = new Label(container, SWT.NONE);
			lb_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Name.setText("Line Code:");

			txt_ShopName = new Text(container, SWT.BORDER);
			txt_ShopName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			//txt_ShopName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_ShopName.setTextLimit(4);
			txt_ShopName.setEnabled(false);
			addRequiredDecoration(txt_ShopName);
			txt_ShopName.setText(BOMLine.parent().getProperty("bl_rev_object_name")+selectedObject.getProperty("bl_rev_object_name"));

			txt_Name = new Text(container, SWT.BORDER);
			txt_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_Name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_Name.setTextLimit(4);
			txt_Name.setToolTipText("Enter 4 digit line name (Ex: 0101,1201....) ");
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText().toUpperCase()+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});
			addRequiredDecoration(txt_Name);

			Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 3, 1));
			//Display Name
			Label lb_ShowText = new Label(container, SWT.NONE);
			lb_ShowText.setText("Display Name:");
			lb_ShowText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 3, 1));

			txt_ShowName = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt_ShowName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			txt_ShowName.setEnabled(false);
			txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText());
			//Listeners
			cb_Plant.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText().toUpperCase()+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

			lst_Program.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(cb_Plant.getText()+"_"+txt_ShopName.getText().toUpperCase()+txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildOperationPanel(Composite area) {

		try {
			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 100;
			container.setLayoutData(gd_container);

			//plant
			Label lb_Plant = new Label(container, SWT.NONE);
			lb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lb_Plant.setText("Plant Code:");


			cb_Plant = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			addRequiredDecoration(cb_Plant);
			cb_Plant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cb_Plant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			VFLovValues lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_CHANGE_NOTICE, new String[] {PropertyDefines.ECM_PLANT});
			VFLovService.setLovValue(cb_Plant, lovValues.getLOVValue(PropertyDefines.TYPE_CHANGE_NOTICE, PropertyDefines.ECM_PLANT));
			TCComponentBOMLine BOMLine = (TCComponentBOMLine) selectedObject;
			if(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").length() != 0) {
				cb_Plant.setText(BOMLine.window().getTopBOMLine().getProperty("bl_rev_object_name").substring(0,4));
				cb_Plant.setEnabled(false);
			}

			//Program
			Label lb_Program = new Label(container, SWT.NONE);
			lb_Program.setText("Program:");

			lst_Program = new List(container, SWT.V_SCROLL | SWT.MULTI| SWT.BORDER);
			addRequiredDecoration(lst_Program);
			lst_Program.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd.heightHint = 70;
			lst_Program.setLayoutData(gd);
			lovValues = VFLovService.loadLOVValues(session, PropertyDefines.TYPE_ME_PLANTBOP_REVISION, new String[] {PROP_PROGRAM});
			VFLovService.setLovValue(lst_Program, lovValues.getLOVValue(PropertyDefines.TYPE_ME_PLANTBOP_REVISION, PROP_PROGRAM));
			
			BOMLine = (TCComponentBOMLine) selectedObject;
			TCComponentItemRevision revision = BOMLine.window().getTopBOMLine().getItemRevision();
			if(revision.getProperty(PROP_PROGRAM).length() != 0) {
				String[] values = revision.getTCProperty(PROP_PROGRAM).getStringArrayValue();
				lst_Program.setSelection(values);;
			}

			//Program
			Label lb_Name = new Label(container, SWT.NONE);
			lb_Name.setText("Operation Name:");

			txt_Name = new Text(container, SWT.BORDER);
			addRequiredDecoration(txt_Name);
			txt_Name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txt_Name.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txt_Name.setTextLimit(128);

			Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
			//Display Name
			Label lb_ShowText = new Label(container, SWT.NONE);
			lb_ShowText.setText("Display Name:");
			lb_ShowText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));


			txt_ShowName = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txt_ShowName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txt_ShowName.setEnabled(false);
			//Listeners
			txt_Name.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					txt_ShowName.setText(txt_Name.getText().toUpperCase());
					if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
						btn_Create.setEnabled(false);
					}else {
						btn_Create.setEnabled(true);
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//		setSaveButton(createButton(parent, IDialogConstants.OK_ID, "Save", true));
		btn_Create = createButton(parent, IDialogConstants.OK_ID, "Create", true);
		btn_Create.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/save_16.png"));
		btn_Create.setEnabled(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("New Item...");
	}

	@Override
	protected void okPressed() {
		// super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	public Button getCreateButton() {
		return btn_Create;
	}

	public String getPlantCode() {
		return cb_Plant.getText().toUpperCase();
	}
	public String[] getProgramCode() {
		return lst_Program.getSelection();
	}
	public String[] getProgramCodeData() {
		String[] rtnValues = {};
		if(lst_Program.getSelectionCount() != 0) {
			String[] values = lst_Program.getSelection();
			rtnValues =  new String[lst_Program.getSelectionCount()];
			for(int i=0;i<lst_Program.getSelectionCount();i++) {
				rtnValues[i] = lst_Program.getData(values[i]).toString();
			}
		}
		return rtnValues;
	}
	public String getName() {
		return txt_Name.getText().toUpperCase();
	}
	public String getDescription() {
		return txt_ShowName.getText().toUpperCase();
	}

	private ControlDecoration addRequiredDecoration(Control control) {
		ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText("Field cannot be empty");
		controlDecoration.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/Mandatory.png"));
		return controlDecoration;
	}

	private String[] getProgramCodes() {

		String[] codeList = null;
		LinkedHashMap<String, String> entries = new LinkedHashMap<>();
		entries.put("Item ID", "*");
		TCComponent[] objects = TCExtension.queryItem(session, entries, "__Configuration_Item_Name_or_ID");
		if(objects != null) {
			codeList =  new String[objects.length];
			for(int i= 0; i<objects.length; i++) {
				try {
					codeList[i] = objects[i].getProperty("item_id");
				} catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return codeList;
	}
	private void fillName() {
		
		if(radios[1].getSelection() == true) {
			txt_ShowName.setText(cb_Plant.getText()+"_"+txt_Name.getText().toUpperCase());
			if(cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
				btn_Create.setEnabled(false);
			}else {
				btn_Create.setEnabled(true);
			}
		}else {
			txt_ShowName.setText(cb_Plant.getText()+"_"+cb_ModelYear.getText()+"_"+txt_Name.getText().toUpperCase());
			if(cb_ModelYear.getText().isEmpty() || cb_Plant.getText().isEmpty() || lst_Program.getSelectionCount() == 0 || txt_Name.getText().isEmpty()) {
				btn_Create.setEnabled(false);
			}else {
				btn_Create.setEnabled(true);
			}
		}
	}
}

