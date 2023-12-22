package com.vinfast.bom.familygroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.utils.Utils;

import vfplm.soa.common.service.VFUtility;


public class FamilyGroupRuleCreationHandler extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(FamilyGroupRuleCreationHandler.class);
	private TCSession session = null;
	private TCComponent newForm = null;
	private LinkedHashMap<String, LinkedList<String>> prg2Variant;
	private LinkedHashMap<String, FGDefinition> fgDifinition;
	private TCComponentItemRevision masterObj;
	private LinkedHashMap<String, String> donorVehDisVal2RealVal;
	public FamilyGroupRuleCreationHandler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.prg2Variant = new LinkedHashMap<String, LinkedList<String>>();
		this.fgDifinition = new LinkedHashMap<String, FGDefinition>();
		getPre_PROGRAM_2_CONTEXT(session, prg2Variant);
		try {
			donorVehDisVal2RealVal = Utils.getLovDetailInfo(session, "vf4_donor_vehicle", "Design");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public TCComponentItemRevision getMasterObj() {
		return masterObj;
	}

	public void setMasterObj(TCComponentItemRevision masterObj) {
		this.masterObj = masterObj;
	}

	public static class FGDefinition{
		public String fgCode;
		public String fgDesc;
		public double maxQty;
		public double minQty;
		public String valType;
	}
	
	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		if(appComps.length > 1) {
			MessageBox.post("This feature is applicable to single part only.", "Information", MessageBox.INFORMATION);
			return null;
		}
		try {
			TCComponentItemRevision part = null;
			if(((TCComponent) appComps[0]).isTypeOf("ItemRevision")){
				part = (TCComponentItemRevision)appComps[0];
			}else {
				TCComponent[] compo = Utils.GetPrimaryObjectByRelationName(this.session, ((TCComponent) appComps[0]), "ItemRevision", "IMAN_specification");
				if(compo != null) {
					part = (TCComponentItemRevision)compo[0];
				}
			}
			this.masterObj = part;
			FamilyGroupRuleDialog dlg = new FamilyGroupRuleDialog(new Shell(), part, null, this.session);
			dlg.create();
			dlg.setTitle("Create New Business Object");
			dlg.setMessage("Define business object create information",IMessageProvider.INFORMATION);
			dlg.getCbPrg().addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent arg0) {
					String currenctTxt = dlg.getCbPrg().getText();
					if(getPrg2Variant().containsKey(currenctTxt)) {
						String[] variantVal = (String[]) getPrg2Variant().get(currenctTxt).toArray(new String[0]);
						dlg.getCbVariant().setItems(variantVal);
					}
				}
			});
			dlg.getCbPrg().setItems(this.prg2Variant.keySet().stream().toArray(String[]::new));
			String[] allFG = getAllFamilyGroup(this.session, this.fgDifinition);
			if(allFG != null && allFG.length > 0) {
				dlg.getCbFGCode().setItems(allFG);
			}
			dlg.getCbFGCode().addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent arg0) {
					dlg.getCbValiType().setText(getFgDifinition().get(dlg.getCbFGCode().getText().split("-")[0]).valType);
					dlg.getTxtMaxQty().setText("");
					dlg.getTxtMinQty().setText("");
					dlg.getTxtMaxQty().setText(String.valueOf(getFgDifinition().get(dlg.getCbFGCode().getText().split("-")[0]).maxQty));
					dlg.getTxtMinQty().setText(String.valueOf(getFgDifinition().get(dlg.getCbFGCode().getText().split("-")[0]).minQty));
					dlg.getTxtFGDes().setText(getFgDifinition().get(dlg.getCbFGCode().getText().split("-")[0]).fgDesc);
				}
			});
			
			try {
				dlg.getCbValiType().setItems(Utils.getLovValues2(session, "vf4_val_type", "VF4_FG_Rule"));
			} catch (TCException e1) {
				MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
				return null;
			}
			
			dlg.getTxtMinQty().addListener(SWT.Verify, new Listener() {

				@Override
				public void handleEvent(Event arg0) {
					onlyAllowDigit(arg0, dlg.getTxtMinQty().getText());
				}

			});
			
			dlg.getTxtMaxQty().addListener(SWT.Verify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					onlyAllowDigit(arg0, dlg.getTxtMaxQty().getText());
				}
			});
			
			dlg.getBtnSave().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(!isFullyInput(dlg)) {
						dlg.setMessage("Please input all required information",IMessageProvider.WARNING);
						return; 
					}else {
						//TODO call function create form
						TCComponent newForm = createFormVF4_FG_Rule(dlg);
						if(newForm != null) {
							setNewForm(newForm);
							pasteToMasterObj(newForm, getMasterObj());
							dlg.setMessage("Create successfully",IMessageProvider.INFORMATION);
							dlg.getBtnSave().setEnabled(false);
						}else {
							dlg.setMessage("Cannot create new Family Group Rule, please contact admin",IMessageProvider.ERROR);
							return; 
						}
					}
				}

		      });
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}
	
	public static void onlyAllowDigit(Event arg0, String currTxt) {
		String string = arg0.text;
		char[] chars = new char[string.length()];
		string.getChars(0, chars.length, chars, 0);
		for (int i = 0; i < chars.length; i++) {
			if (!('0' <= chars[i] && chars[i] <= '9') && chars[i] != '.') {
				arg0.doit = false;
				return;
			}
			if(chars[i] == '.' && currTxt.contains(".")) {
				arg0.doit = false;
				return;
			}
		}
	}
	
	public static boolean isFullyInput(FamilyGroupRuleDialog dlg) {
		if(!dlg.getCbPrg().getText().isEmpty() && !dlg.getCbFGCode().getText().isEmpty() && !dlg.getCbValiType().getText().isEmpty()) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private TCComponent createFormVF4_FG_Rule(FamilyGroupRuleDialog dlg) {
		DataManagementService dms = DataManagementService.getService(this.session);
		CreateIn in = new CreateIn();
		String formName = dlg.getCbPrg().getText() + "_" + dlg.getCbFGCode().getText().split("-")[0] + "_" + dlg.getCbVariant().getText();
		if (this.donorVehDisVal2RealVal.containsValue(dlg.getCbPrg().getText())) {
		    for (final Object entry : this.donorVehDisVal2RealVal.keySet()) {
		        if (this.donorVehDisVal2RealVal.get(entry).compareToIgnoreCase(dlg.getCbPrg().getText()) == 0) {
		        	in.data.stringProps.put("vf4_program", entry.toString());
		        	break;
		        }
		    }
		}else {
			MessageBox.post("Program " + dlg.getCbPrg().getText() + " is not valid", "ERROR", MessageBox.ERROR);
			return null;
		}
		in.data.boName = "VF4_FG_Rule";
		in.data.stringProps.put("object_name", formName);
		in.data.stringProps.put("vf4_fg_code", dlg.getCbFGCode().getText().split("-")[0]);
		in.data.stringProps.put("vf4_val_type", dlg.getCbValiType().getText());
		in.data.stringProps.put("vf4_variant", dlg.getCbVariant().getText());
		in.data.stringProps.put("object_desc", dlg.getTxtFGDes().getText());
		if(!dlg.getTxtMaxQty().getText().isEmpty()) {
			in.data.doubleProps.put("vf4_max_quantity", Double.valueOf(dlg.getTxtMaxQty().getText()));
		}
		if(!dlg.getTxtMinQty().getText().isEmpty()) {
			in.data.doubleProps.put("vf4_min_quantity", Double.valueOf(dlg.getTxtMinQty().getText()));
		}
		try {
			CreateIn[] ins = new CreateIn[] { in };
			CreateResponse res;
			res = dms.createObjects(ins);
			if (res.output.length > 0 && res.output[0].objects.length > 0) {
				TCComponent form = res.output[0].objects[0];
				return form;
			} else {
				MessageBox.post("Exception: " + VFUtility.HanlderServiceData(res.serviceData), "ERROR", MessageBox.ERROR);
				return null;
			}
		} catch (ServiceException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
	}
	
	public static void getPre_PROGRAM_2_CONTEXT(TCSession session, LinkedHashMap<String, LinkedList<String>> output) {
		String[]tmp = session.getPreferenceService().getStringValues("VF_PROGRAM_2_CONTEXT");
		if(tmp != null && tmp.length > 0) {
			for(int i = 0; i < tmp.length; i++) {
				if(tmp[i].contains("=")) {
					String prg = tmp[i].split("=")[0];
					String context = tmp[i].split("=")[1];
					LinkedList<String> contextName = new LinkedList<String>();
					
					LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
					queryInput.put("ID", "*" + context + "*");
					TCComponent[] results = com.vf.utils.Query.queryItem(session, queryInput, "__TNH_Find_VariantRule_by_ContextID");
					if(results != null && results.length > 0) {
						for(TCComponent obj : results) {
							try {
								String varName = obj.getProperty("object_name");
								contextName.add(varName);
							} catch (TCException e) {
								MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
								continue;
							}
						}
					}
					output.put(prg, contextName);
				}
			}
		}
	}

	public LinkedHashMap<String, LinkedList<String>> getPrg2Variant() {
		return prg2Variant;
	}
	
	public static String[] getAllFamilyGroup(TCSession session, LinkedHashMap<String, FGDefinition> fgDifinition) {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		String queryName = "General...";
		inputQuery.put("Type", "VF4_Family_Group");
		TCComponent[] results = com.vf.utils.Query.queryItem(session, inputQuery, queryName);
		if(results != null && results.length > 0) {
			String[] fgCode = new String[results.length];
			int i = 0;
			for(TCComponent obj : results) {
				try {
					FGDefinition fgd = new FGDefinition();
					String tmp = obj.getProperty("vf4_code");
					fgd.valType = obj.getProperty("vf4_def_val_type");
					fgd.minQty = obj.getDoubleProperty("vf4_def_min_quantity");
					fgd.maxQty = obj.getDoubleProperty("vf4_def_max_quantity");
					fgd.fgDesc = obj.getProperty("vf4_desc");
					fgCode[i] = tmp+ "-"+ fgd.fgDesc;
					fgDifinition.put(tmp, fgd);
				} catch (TCException e) {
					MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
					continue;
				}
				i++;
			}
			return fgCode;
		}
		return null;
	}
	
	
	
	public TCComponent getNewForm() {
		return newForm;
	}

	public void setNewForm(TCComponent newForm) {
		this.newForm = newForm;
	}
	public static void pasteToMasterObj(TCComponent newForm, TCComponentItemRevision masterObj) {
		try {
			TCComponent[] existedChild = masterObj.getRelatedComponents("IMAN_specification");
			List<TCComponent> arrlist = new ArrayList<TCComponent>(Arrays.asList(existedChild));
			arrlist.add(newForm);
			masterObj.setRelated("IMAN_specification", arrlist.toArray(new TCComponent[0]));
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}
	}

	public LinkedHashMap<String, FGDefinition> getFgDifinition() {
		return fgDifinition;
	}
	
}
