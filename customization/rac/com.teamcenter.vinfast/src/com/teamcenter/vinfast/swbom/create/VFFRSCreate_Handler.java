package com.teamcenter.vinfast.swbom.create;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class VFFRSCreate_Handler extends AbstractHandler {
	private TCSession session;
	private VFFRSCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private static String OBJECT_TYPE = "VF3_FRS";

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		try {
			selectedObject = (TCComponent) targetComp[0];

			// Init data
			LinkedHashMap<String, String> marketDataForm = TCExtension.GetLovValueAndDisplay("vf4_market_arr", "VF3_FRSRevision", session);
			LinkedHashMap<String, String> programDataForm = TCExtension.GetLovValueAndDisplay("vf4_program", "VF3_FRSRevision", session);
			String[] modelDataForm = TCExtension.GetLovValues("vf4_model", "VF3_FRSRevision", session);
			String[] lifecycleDataForm = TCExtension.GetLovValues("vf4_lifecycle", "VF3_FRSRevision", session);
			String[] fusaDataForm = TCExtension.GetLovValues("vf4_FuSA", "VF3_FRSRevision", session);
			// Init UI
			dlg = new VFFRSCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create VF FRS");
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbMarket, marketDataForm);
			dlg.btnMarketAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("MARKET");
				}
			});

			dlg.btnMarketRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("MARKET");
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, programDataForm);

			dlg.cbModel.setItems(modelDataForm);
			dlg.btnModelAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					addValue("MODEL");
				}
			});

			dlg.btnModelRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					removeValue("MODEL");
				}
			});

			dlg.cbLifecycle.setItems(lifecycleDataForm);
			dlg.cbFUSA.setItems(fusaDataForm);

			PaintListener verify = new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					updateName();
				}
			};

			dlg.txtVersion.addPaintListener(verify);
			dlg.txtMajorVersion.addPaintListener(verify);
			dlg.txtMinorVersion.addPaintListener(verify);
			dlg.txtHotfixNumber.addPaintListener(verify);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateName() {
		String version = dlg.txtVersion.getText();
		String major = dlg.txtMajorVersion.getText();
		String minor = dlg.txtMinorVersion.getText();
		String hotfix = dlg.txtHotfixNumber.getText();

		String name = version + "." + major + "." + minor + (hotfix.isEmpty() ? "" : ("." + hotfix));
		dlg.txtName.setText("FRS " + name);
	}

	private void addValue(String type) {
		switch (type) {
		case "MARKET":
			String market = dlg.cbMarket.getText();
			if (!market.isEmpty()) {
				if (checkExistInList(dlg.lstMarket.getItems(), market))
					dlg.lstMarket.add(market);
			}
			break;
		case "MODEL":
			String variant = dlg.cbModel.getText();
			if (!variant.isEmpty()) {
				if (checkExistInList(dlg.lstModel.getItems(), variant))
					dlg.lstModel.add(variant);
			}
			break;
		}
	}

	private void removeValue(String type) {
		switch (type) {
		case "MARKET":
			int indexMarket = dlg.lstMarket.getSelectionIndex();
			if (indexMarket >= 0)
				dlg.lstMarket.remove(indexMarket);
			break;
		case "MODEL":
			int indexVariant = dlg.lstModel.getSelectionIndex();
			if (indexVariant >= 0)
				dlg.lstModel.remove(indexVariant);
			break;
		}
	}

	private boolean checkExistInList(String[] origin, String target) {
		if (origin == null || origin.length == 0)
			return true;

		for (String item : origin) {
			if (item.compareTo(target) == 0)
				return false;
		}
		return true;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			DataManagementService dms = DataManagementService.getService(session);
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();

			Set<String> marketValue = new HashSet<String>();
			String[] marketDisplay = dlg.lstMarket.getItems();
			if (marketDisplay != null && marketDisplay.length > 0) {
				for (String str : marketDisplay) {
					marketValue.add((String) dlg.cbMarket.getData(str));
				}
			}
			String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
			String[] model = dlg.lstModel.getItems();
			Double version = Double.parseDouble(dlg.txtVersion.getText());
			BigInteger majorVersion = new BigInteger(dlg.txtMajorVersion.getText());
			BigInteger minorVersion = new BigInteger(dlg.txtMinorVersion.getText());
			BigInteger hotfixNumber = null;
			if (!dlg.txtHotfixNumber.getText().isEmpty())
				hotfixNumber = new BigInteger(dlg.txtHotfixNumber.getText());
			String referenceNumber = dlg.txtReferenceNumber.getText();
			String lifecycle = dlg.cbLifecycle.getText();
			String fusa = dlg.cbFUSA.getText();

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);

			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			itemRevisionDef.stringArrayProps.put("vf4_market_arr", marketValue.toArray(new String[0]));
			itemRevisionDef.stringProps.put("vf4_program", program);
			itemRevisionDef.stringArrayProps.put("vf4_model", model);
			itemRevisionDef.doubleProps.put("vf4_version", version);
			itemRevisionDef.intProps.put("vf4_major_version", majorVersion);
			itemRevisionDef.intProps.put("vf4_minor_version", minorVersion);
			if (hotfixNumber != null)
				itemRevisionDef.intProps.put("vf3_hotfix_number", hotfixNumber);
			itemRevisionDef.stringProps.put("vf3_ref_num", referenceNumber);
			itemRevisionDef.stringProps.put("vf4_lifecycle", lifecycle);
			itemRevisionDef.stringProps.put("vf4_FuSA", fusa);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });

			CreateInput idGen = new CreateInput();
			idGen.boName = "VF4_FRS_Gen_ID";
			idGen.stringProps.put("vf4_program", program);
			itemDef.data.compoundCreateInput.put("fnd0IdGenerator", new CreateInput[] { idGen });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
				if (cfgContext != null) {
					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item (" + itemRev.getPropertyDisplayableValue("item_id") + ") has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				MessageBox.post("Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private Boolean checkRequired() {
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.lstMarket.getItemCount() == 0)
			return false;
		if (dlg.cbProgram.getText().isEmpty())
			return false;
		if (dlg.lstModel.getItemCount() == 0)
			return false;
		if (dlg.txtVersion.getText().isEmpty())
			return false;
		if (dlg.txtMajorVersion.getText().isEmpty())
			return false;
		if (dlg.txtMinorVersion.getText().isEmpty())
			return false;

		return true;
	}

	private void resetDialog() {
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.lstMarket.removeAll();
		dlg.cbProgram.deselectAll();
		dlg.lstModel.removeAll();
		dlg.txtVersion.setText("");
		dlg.txtMajorVersion.setText("");
		dlg.txtMinorVersion.setText("");
		dlg.txtHotfixNumber.setText("");
		dlg.txtReferenceNumber.setText("");
		dlg.cbLifecycle.deselectAll();
		dlg.cbFUSA.deselectAll();
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
