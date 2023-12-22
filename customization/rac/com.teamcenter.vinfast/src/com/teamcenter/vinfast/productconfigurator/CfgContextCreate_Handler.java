package com.teamcenter.vinfast.productconfigurator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class CfgContextCreate_Handler extends AbstractHandler {
	private TCSession session;
	private String firstDigit;
	private String sndDigit;
	private String fourthDigit;
	private String modelYear;
	private LinkedHashMap<String, Integer> vehGenMapping = new LinkedHashMap<>();
	private CfgContextCreate_Dialog dlg;

	public CfgContextCreate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		vehGenMapping.put("1", 1);
		vehGenMapping.put("2", 2);
		vehGenMapping.put("3", 3);
		vehGenMapping.put("4", 4);
		vehGenMapping.put("5", 5);
		vehGenMapping.put("6", 6);
		vehGenMapping.put("7", 7);
		vehGenMapping.put("8", 8);
		vehGenMapping.put("9", 9);
		vehGenMapping.put("A", 10);
		vehGenMapping.put("B", 11);
		vehGenMapping.put("C", 12);
		vehGenMapping.put("D", 13);
		vehGenMapping.put("E", 14);
		vehGenMapping.put("F", 15);
		vehGenMapping.put("G", 16);
		vehGenMapping.put("H", 17);
		vehGenMapping.put("I", 18);
		vehGenMapping.put("J", 19);
		vehGenMapping.put("K", 20);
		vehGenMapping.put("L", 21);
		vehGenMapping.put("M", 22);
		vehGenMapping.put("N", 23);
		vehGenMapping.put("O", 24);
		vehGenMapping.put("P", 25);
		vehGenMapping.put("Q", 26);
		vehGenMapping.put("R", 27);
		vehGenMapping.put("S", 28);
		vehGenMapping.put("T", 29);
		vehGenMapping.put("U", 30);
		vehGenMapping.put("V", 31);
		vehGenMapping.put("W", 32);
		vehGenMapping.put("X", 33);
		vehGenMapping.put("Y", 34);
		vehGenMapping.put("Z", 35);
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		firstDigit = "";
		sndDigit = "";
		fourthDigit = "";
		modelYear = "";

		try {
			String[] propulsionPre = TCExtension.GetPreferenceValues("VINF_CFG_CONTEXT_PROPULSION", session);
			String[] segSizePre = TCExtension.GetPreferenceValues("VINF_CFG_CONTEXT_SEG_SIZE", session);
			String[] bodyStylePre = TCExtension.GetPreferenceValues("VINF_CFG_CONTEXT_BODY_STYLE", session);
			String[] vehTypePre = TCExtension.GetPreferenceValues("VINF_CFG_CONTEXT_VEH_TYPE", session);
			LinkedHashMap<String, String> vehProgramDataForm = TCExtension.GetLovValueAndDisplay("vf4_veh_program", "Cfg0ProductItem", session);
			LinkedHashMap<String, String> esVehLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", "Cfg0ProductItem", session);

			dlg = new CfgContextCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create Configurator Context");
			dlg.setMessage("Define input information", IMessageProvider.INFORMATION);

			dlg.cbVehType.setItems(vehTypePre);
			dlg.cbVehType.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					resetDialog();
					String vehType = dlg.cbVehType.getText();
					ArrayList<String> propulsionValue = new ArrayList<>();
					ArrayList<String> bodyStyleVal = new ArrayList<>();
					ArrayList<String> segnSizeVal = new ArrayList<>();

					for (String str : propulsionPre) {
						if (str.contains(vehType)) {
							propulsionValue.add(str.split("=")[0]);
						}
					}

					for (String str : segSizePre) {
						if (str.contains(vehType)) {
							segnSizeVal.add(str.split("=")[0]);
						}
					}

					for (String str : bodyStylePre) {
						if (str.contains(vehType)) {
							bodyStyleVal.add(str.split("=")[0]);
						}
					}
					dlg.cbPropulsion.setItems(propulsionValue.toArray(new String[propulsionValue.size()]));
					dlg.cbBodyStyle.setItems(bodyStyleVal.toArray(new String[bodyStyleVal.size()]));
					dlg.cbSegnSize.setItems(segnSizeVal.toArray(new String[segnSizeVal.size()]));

					if (dlg.cbVehType.getText().compareToIgnoreCase("EE Components") == 0) {
						dlg.lblPropulsion.setText("Product line: (*)");
						dlg.lblSegSize.setText("Assembly: (*)");
						dlg.lblVehGen.setText("Product Generation: ");
						dlg.lblBodyStyle.setText("Product type: (*)");
					} else {
						dlg.lblPropulsion.setText("Propulsion: (*)");
						dlg.lblSegSize.setText("Segment/Size: (*)");
						dlg.lblVehGen.setText("Vehicle Generation: ");
						dlg.lblBodyStyle.setText("Body Style: (*)");
					}

					if (dlg.cbVehType.getText().compareToIgnoreCase("Scooter") == 0) {
						dlg.lblVehicleProgram.setText("EScooter Model/Vehicle Line: (*)");
						StringExtension.UpdateValueTextCombobox(dlg.cbVehProgram, esVehLineDataForm);
					} else {
						dlg.lblVehicleProgram.setText("Vehicle Program:");
						StringExtension.UpdateValueTextCombobox(dlg.cbVehProgram, vehProgramDataForm);
					}
				}
			});

			dlg.cbPropulsion.setItems(new String[] { "Please select Vehicle Type" });
			dlg.cbPropulsion.select(0);
			dlg.cbPropulsion.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (dlg.cbPropulsion.getText().contains("-")) {
						firstDigit = dlg.cbPropulsion.getText().split("-")[0];
					}
					dlg.txtVehGen.setText(getVehicleGen());
				}
			});

			dlg.cbSegnSize.setItems(new String[] { "Please select Vehicle Type" });
			dlg.cbSegnSize.select(0);
			dlg.cbSegnSize.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (dlg.cbSegnSize.getText().contains("-")) {
						sndDigit = dlg.cbSegnSize.getText().split("-")[0];
					}
					dlg.txtVehGen.setText(getVehicleGen());
					if (dlg.cbVehType.getText().compareToIgnoreCase("EE Components") == 0) {
						ArrayList<String> bodyStyleVal = new ArrayList<>();
						for (String str : bodyStylePre) {
							String[] line = str.split("=");
							if (line.length > 2) {
								if (line[2].compareToIgnoreCase(sndDigit) == 0) {
									bodyStyleVal.add(str.split("=")[0]);
								}
							}
						}
						dlg.cbBodyStyle.setItems(bodyStyleVal.toArray(new String[bodyStyleVal.size()]));
					}
				}
			});

			dlg.cbBodyStyle.setItems(new String[] { "Please select Vehicle Type" });
			dlg.cbBodyStyle.select(0);
			dlg.cbBodyStyle.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (dlg.cbBodyStyle.getText().contains("-")) {
						fourthDigit = dlg.cbBodyStyle.getText().split("-")[0];
					}
					dlg.txtVehGen.setText(getVehicleGen());
				}
			});

			ArrayList<String> years = get100YearFromNow();
			dlg.cbModelYear.setItems(years.toArray(new String[years.size()]));
			dlg.cbModelYear.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					modelYear = dlg.cbModelYear.getText();
					dlg.txtVehGen.setText(getVehicleGen());
				}
			});

			dlg.txtVehGen.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					if (!firstDigit.isEmpty() && !sndDigit.isEmpty() && !dlg.txtVehGen.getText().isEmpty() && !fourthDigit.isEmpty() && !modelYear.isEmpty()) {
						dlg.txtAutoID.setText(firstDigit + sndDigit + dlg.txtVehGen.getText() + fourthDigit + "_" + modelYear);
					}

				}
			});

			dlg.txtVehGen.setText(getVehicleGen());

			dlg.btnOk.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private ArrayList<String> get100YearFromNow() {
		ArrayList<String> output = new ArrayList<>();
		int year = Calendar.getInstance().get(Calendar.YEAR);
		for (int i = 0; i < 100; i++) {
			output.add(String.valueOf(year + i));
		}
		return output;
	}

	private void createNewItem() {
		if (!validationData()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		try {
			DataManagementService dms = DataManagementService.getService(session);
			String partId = firstDigit + sndDigit + dlg.txtVehGen.getText() + fourthDigit + "_" + modelYear;
			String name = dlg.txtName.getText();
			String description = dlg.txtDesc.getText();
			String vehProgram = "";
			if (!dlg.cbVehProgram.getText().isEmpty())
				vehProgram = (String) dlg.cbVehProgram.getData(dlg.cbVehProgram.getText());

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "Cfg0ProductItem";
			itemDef.data.stringProps.put("item_id", partId);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.boolProps.put("cfg0PosBiasedVariantAvail", false);
			if (dlg.cbVehType.getText().compareToIgnoreCase("Scooter") == 0) {
				itemDef.data.stringProps.put("vf4_es_model_veh_line", vehProgram);
			} else {
				itemDef.data.stringProps.put("vf4_veh_program", vehProgram);
			}

			CreateResponse createResponse = dms.createObjects(new CreateIn[] { itemDef });

			if (createResponse.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = createResponse.output[0].objects[0];
				try {
					session.getUser().getNewStuffFolder().add("contents", cfgContext);
				} catch (TCException e1) {
					MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
				}
				dlg.setMessage("Created successfully, new item " + partId + " has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
				resetUI();
			} else {
				ServiceData serviceData = createResponse.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception e) {
			MessageBox.post("Exception: " + e, "ERROR", MessageBox.ERROR);
		}
	}

	private boolean validationData() {
		if (dlg.txtAutoID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.cbVehType.getText().compareToIgnoreCase("Scooter") == 0) {
			if (dlg.cbVehProgram.getText().isEmpty())
				return false;
		}

		return true;
	}

	private String getVehicleGen() {
		int currGen = 0;
		if (!firstDigit.isEmpty() && !sndDigit.isEmpty() && !fourthDigit.isEmpty() && !modelYear.isEmpty()) {
			try {
				LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
				queryInput.put("Item ID", firstDigit + sndDigit + "*" + fourthDigit + "_" + modelYear);
				queryInput.put("Type", "Configurator Context");
				TCComponent[] mos = Query.queryItem(session, queryInput, "Item...");
				if (mos != null && mos.length > 0) {
					for (TCComponent mo : mos) {
						String id = mo.getProperty("item_id");
						char vehGen = id.charAt(2);
						if (vehGenMapping.get(Character.toString(vehGen)) > currGen) {
							currGen = vehGenMapping.get(Character.toString(vehGen));
						}
					}
				}
			} catch (Exception e) {
				MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			}
			for (Map.Entry<String, Integer> entry : vehGenMapping.entrySet()) {
				if (entry.getValue() == currGen + 1) {
					return entry.getKey();
				}
			}
		}

		return "";
	}

	private void resetDialog() {
		dlg.cbBodyStyle.removeAll();
		dlg.cbPropulsion.removeAll();
		dlg.cbPropulsion.removeAll();
		dlg.txtVehGen.setText("");
		dlg.txtAutoID.setText("");
		firstDigit = "";
		sndDigit = "";
		fourthDigit = "";
	}

	private void resetUI() {
		dlg.cbVehType.deselectAll();
		dlg.cbPropulsion.removeAll();
		dlg.cbSegnSize.removeAll();
		dlg.txtVehGen.setText("");
		dlg.cbBodyStyle.removeAll();
		dlg.cbModelYear.deselectAll();
		dlg.txtAutoID.setText("");
		dlg.txtName.setText("");
		dlg.txtDesc.setText("");
		dlg.cbVehProgram.deselectAll();
	}
}
