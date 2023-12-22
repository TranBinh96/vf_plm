package com.teamcenter.vinfast.car.manuf.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.subdialog.SearchItem_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ColorPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ColorPartCreate_Dialog dlg;
	private boolean isAddToBom = false;
	private TCComponent selectedObject = null;
	private TCComponentItem basePartItem = null;
	private TCComponentBOMLine parentBomLine = null;
	private String OBJECT_TYPE = "VF3_manuf_part";
	private String[] objectTypeAvai = new String[] { "VF4_Design", "VF3_manuf_part", "VF4_BP_Design" };

	private HashMap<String, ArrayList<String>> subColorMapping = null;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

			if (targetComp[0] instanceof TCComponentBOMLine) {
				isAddToBom = true;
				selectedObject = (TCComponent) targetComp[0];
				parentBomLine = getValidBOMLine((TCComponent) targetComp[0]);
				if (parentBomLine == null) {
					MessageBox.post("Please select BOMLine is VF Item/Structure Part.", "Warning", MessageBox.WARNING);
					return null;
				}
			} else if (targetComp[0] instanceof TCComponentItemRevision) {
				selectedObject = (TCComponent) targetComp[0];
				basePartItem = ((TCComponentItemRevision) selectedObject).getItem();
			} else if (targetComp[0] instanceof TCComponentItem) {
				selectedObject = (TCComponent) targetComp[0];
				basePartItem = (TCComponentItem) selectedObject;
			}

			if (basePartItem != null) {
				String objectType = basePartItem.getType();
				if (!Arrays.asList(objectTypeAvai).contains(objectType))
					basePartItem = null;
			}

			String[] lovCategory = TCExtension.GetPreferenceValues("VF_MBOM_Color_Category", session);
			String[] lovBaseColor = TCExtension.GetPreferenceValues("VF_MBOM_Color_Code", session);
			String[] lovSubColor = TCExtension.GetPreferenceValues("VF_MBOM_Color_Sub_Code", session);
			String[] lovColorSpec = TCExtension.GetLovValues("VF3Color_Spec");
			String[] lovGrain = TCExtension.GetLovValues("VF3Grain");
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE, session);
			LinkedHashMap<String, String> partTracebilityDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", OBJECT_TYPE, session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] lovGloss = TCExtension.GetLovValues("VF3Gloss");

			if (lovSubColor.length != 0) {
				subColorMapping = new HashMap<String, ArrayList<String>>();
				for (int i = 0; i < lovSubColor.length; i++) {
					String split[] = lovSubColor[i].split("=");
					if (subColorMapping.containsKey(split[0])) {
						ArrayList<String> subCodes = subColorMapping.get(split[0]);
						subCodes.add(split[1]);
					} else {
						ArrayList<String> subCodes = new ArrayList<String>();
						subCodes.add(split[1]);
						subColorMapping.put(split[0], subCodes);
					}
				}
			}

			dlg = new ColorPartCreate_Dialog(new Shell());
			dlg.create();

			dlg.cbCategory.setItems(lovCategory);

			dlg.cbBaseColor.setItems(lovBaseColor);
			dlg.cbBaseColor.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.cbBaseColor.getText().isEmpty()) {
						String categoryIndex = dlg.cbCategory.getText().substring(0, 1);
						String baseColorIndex = dlg.cbBaseColor.getText().substring(0, 2);
						getSubColorLOV(subColorMapping, categoryIndex + baseColorIndex);
						dlg.txtColorCode.setText("");
					}
				}
			});

			dlg.cbSubColor.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.cbSubColor.getText().isEmpty()) {
						String baseColorIndex = dlg.cbBaseColor.getText().substring(0, 2);
						String colorCodeIndex = dlg.cbSubColor.getText().substring(0, 1);
						dlg.txtColorCode.setText(baseColorIndex + colorCodeIndex);
					}
					fillDescription();
				}
			});

			dlg.cbColorSpec.setItems(lovColorSpec);
			dlg.cbColorSpec.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					fillDescription();
				}
			});

			dlg.cbGloss.setItems(lovGloss);
			dlg.cbGloss.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					fillDescription();
				}
			});

			dlg.cbGrain.setItems(lovGrain);
			dlg.cbGrain.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					fillDescription();
				}
			});

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);
			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTracebilityDataForm);

			dlg.cbUOM.setItems(uomDataForm);

			dlg.btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SearchItem_Dialog searchDlg = new SearchItem_Dialog(new Shell(), String.join(";", objectTypeAvai));
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							TCComponent item = searchDlg.itemSearch.get(index);
							if (item instanceof TCComponentItem) {
								basePartItem = (TCComponentItem) item;
								updateBasePartInfo();
							}

							searchDlg.getShell().dispose();
						}
					});
					searchDlg.open();
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			updateBasePartInfo();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateBasePartInfo() {
		if (basePartItem == null)
			return;

		dlg.txtBasePart.setText(basePartItem.toString());
		try {
			String[] propValues = basePartItem.getProperties(new String[] { "item_id", "object_name", "uom_tag" });
			dlg.txtID.setText(propValues[0]);
			dlg.txtID.setData(basePartItem);
			dlg.txtName.setText(propValues[1]);
			dlg.cbUOM.setText((propValues[2].equals("each")) ? "EA" : propValues[2]);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private TCComponentBOMLine getValidBOMLine(TCComponent selectedBOMLine) {
		try {
			TCComponentBOMLine line = (TCComponentBOMLine) selectedBOMLine;
			String type = line.getItemRevision().getType();
			if (type.compareTo("VF3_vfitemRevision") == 0 || type.compareTo("VF4_str_partRevision") == 0) {
				return line;
			} else {
				TCComponentBOMLine parent = line.parent();
				type = parent.getItemRevision().getType();
				if (type.compareTo("VF3_vfitemRevision") == 0 || type.compareTo("VF4_str_partRevision") == 0) {
					return parent;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean checkRequired() {
		boolean check = true;

		if (dlg.txtID.getText().isEmpty()) {
			warningLabel(dlg.lblId, true);
			check = false;
		} else {
			warningLabel(dlg.lblId, false);
		}
		if (dlg.txtColorCode.getText().isEmpty()) {
			warningLabel(dlg.lblId, true);
			check = false;
		} else {
			warningLabel(dlg.lblId, false);
		}
		if (dlg.txtName.getText().isEmpty()) {
			warningLabel(dlg.lblName, true);
			check = false;
		} else {
			warningLabel(dlg.lblName, false);
		}
		if (dlg.txtDescription.getText().isEmpty()) {
			warningLabel(dlg.lblDescription, true);
			check = false;
		} else {
			warningLabel(dlg.lblDescription, false);
		}
		if (dlg.cbPartMakeBuy.getText().isEmpty()) {
			warningLabel(dlg.lblPartMakebuy, true);
			check = false;
		} else {
			warningLabel(dlg.lblPartMakebuy, false);
		}
		if (dlg.cbUOM.getText().isEmpty()) {
			warningLabel(dlg.lblUom, true);
			check = false;
		} else {
			warningLabel(dlg.lblUom, false);
		}

		return check;
	}

	private boolean validateValues() {
		if (!dlg.txtDescription.getText().isEmpty()) {
			Pattern special = Pattern.compile("[!@#$%&*()+=|<>?{}\\[\\]~]");
			Matcher hasSpecial = special.matcher(dlg.txtDescription.getText());
			if (hasSpecial.find()) {
				return false;
			}
		}

		return true;
	}

	private void createNewItem() {
		if (!validateValues()) {
			dlg.setMessage("Please remove special characters in description. Replace it by - or _", IMessageProvider.WARNING);
			return;
		}

		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		DataManagementService dmService = DataManagementService.getService(session);

		String id = dlg.txtID.getText() + dlg.txtColorCode.getText();
		String name = dlg.txtName.getText();
		String makeBuy = dlg.cbPartMakeBuy.getText();
		String description = dlg.txtDescription.getText();
		String uom = dlg.cbUOM.getText();
		String partTraceability = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
		Boolean isAfterSale = null;
		if (dlg.rbtIsAfterSaleTrue.getSelection())
			isAfterSale = true;
		if (dlg.rbtIsAfterSaleFalse.getSelection())
			isAfterSale = false;

		try {
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("vf4_item_make_buy", makeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);
			if (isAfterSale != null)
				itemDef.data.boolProps.put("vf4_itm_after_sale_relevant", isAfterSale);

			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = OBJECT_TYPE + "Revision";
			itemRevisionDef.stringProps.put("object_desc", description);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage("Create unsuccessful. Exception: " + TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			CreateOut[] createOutResp = response.output;
			TCComponent[] component = createOutResp[0].objects;
			TCComponentItemRevision newItemRevision = null;
			for (TCComponent rev : component) {
				if (rev.getType().compareTo(OBJECT_TYPE + "Revision") == 0) {
					newItemRevision = (TCComponentItemRevision) rev;
				}
			}

			if (newItemRevision == null) {
				dlg.setMessage("Create unsuccessfully. Please contact with administrator.", IMessageProvider.ERROR);
				return;
			}

			session.getUser().getNewStuffFolder().add("contents", newItemRevision.getItem());

			if (isAddToBom)
				addToBOMLine(newItemRevision);

			attachRelation(newItemRevision.getItem());

			openOnCreate(newItemRevision);

			dlg.setMessage("Create successful. New item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
			resetDialog();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToBOMLine(TCComponentItemRevision newRevision) {
		try {
			TCComponentBOMLine bomline = (TCComponentBOMLine) parentBomLine;
			TCComponentBOMLine child = bomline.add(newRevision.getItem(), newRevision, null, false);
			child.setProperty("bl_quantity", "1");
			bomline.window().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void attachRelation(TCComponent newItem) {
		try {
			TCComponentProcessType processType = (TCComponentProcessType) session.getTypeComponent("Job");
			TCComponentTaskTemplateType templateType = (TCComponentTaskTemplateType) session.getTypeComponent(TCComponentTaskTemplateType.EPM_TASKTEMPLATE_TYPE);
			TCComponentTaskTemplate template = templateType.find("Grant_Write_Access", 0);
			if (template != null) {
				TCComponentProcess process = (TCComponentProcess) processType.create(newItem.getProperty("object_string"), "Grant_Write_Access", template, new TCComponent[] { basePartItem }, new int[] { 1 });
				if (process != null) {
					basePartItem.add("VF4_PLM4_Base2ColorREL", newItem);
					basePartItem.setProperty("vf4_colored_part", "True");
					TCComponentTask task = process.getRootTask().getSubtask("Grant_Write_Access");
					task.setProperty("task_result", "Completed");
					task.performAction(TCComponentTask.COMPLETE_ACTION, TCComponentTask.PROP_STATE);
				} else {
					dlg.setMessage("Error in granting access. Failed to create color part", IMessageProvider.ERROR);
					newItem.delete();
				}
			}

		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e.getMessage(), "Error", MessageBox.ERROR);
		}
	}

	private void getSubColorLOV(HashMap<String, ArrayList<String>> sub_color_map, String prefix) {
		ArrayList<String> value = sub_color_map.get(prefix);
		if (value == null) {
			dlg.cbSubColor.removeAll();
		} else {
			String[] subCodes = value.toArray(new String[value.size()]);
			dlg.cbSubColor.removeAll();
			dlg.cbSubColor.setItems(subCodes);
		}
	}

	private void fillDescription() {
		String value = "";
		String sub_color = dlg.cbSubColor.getText();
		String spec = dlg.cbColorSpec.getText().replace("/", "_");
		String gloss = dlg.cbGloss.getText().replace(",", ".");
		String grain = dlg.cbGrain.getText();
		if (sub_color.length() != 0) {
			value = sub_color;
		}
		if (spec.length() != 0) {
			if (!value.equals("")) {
				value = value + "- " + spec;
			} else {
				value = spec;
			}
		}
		if (gloss.length() != 0) {
			if (!value.equals("")) {
				value = value + "- " + gloss;
			} else {
				value = gloss;
			}
		}
		if (grain.length() != 0) {
			if (!value.equals("")) {
				value = value + "- " + grain;
			} else {
				value = grain;
			}
		}

		dlg.txtDescription.setText(value);
	}

	private void resetDialog() {
		dlg.cbCategory.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.cbUOM.deselectAll();
		dlg.cbGrain.deselectAll();
		dlg.cbGloss.deselectAll();
		dlg.txtColorCode.setText("");
		dlg.cbBaseColor.deselectAll();
		dlg.cbSubColor.deselectAll();
		dlg.cbColorSpec.deselectAll();
	}

	private void warningLabel(Label target, boolean warning) {
		if (warning)
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
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
