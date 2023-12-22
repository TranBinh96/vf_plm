package com.vinfast.bom.familygroup;

import java.util.LinkedHashMap;
import java.util.LinkedList;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.Utils;
import com.vinfast.bom.familygroup.FamilyGroupRuleCreationHandler.FGDefinition;

public class FamilyGroupRuleModifyHandler extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(FamilyGroupRuleModifyHandler.class);
	private LinkedHashMap<String, LinkedList<String>> prg2Variant;
	private LinkedHashMap<String, FGDefinition> fgDifinition;
	private TCSession session = null;
	private TCComponentForm targetForm;
	private LinkedHashMap<String, String> donorVehDisVal2RealVal;
	public FamilyGroupRuleModifyHandler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.prg2Variant = new LinkedHashMap<String, LinkedList<String>>();
		this.fgDifinition = new LinkedHashMap<String, FGDefinition>();
		FamilyGroupRuleCreationHandler.getPre_PROGRAM_2_CONTEXT(session, prg2Variant);
		try {
			donorVehDisVal2RealVal = Utils.getLovDetailInfo(session, "vf4_donor_vehicle", "Design");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, LinkedList<String>> getPrg2Variant() {
		return prg2Variant;
	}

	public TCComponentForm getTargetForm() {
		return targetForm;
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
			if(((TCComponent) appComps[0]).isTypeOf("VF4_FG_Rule")){
				FamilyGroupRuleDialog dlg = new FamilyGroupRuleDialog(new Shell(), null, null, (TCSession) AIFUtility.getCurrentApplication().getSession());
				dlg.create();
				dlg.setTitle("Update Business Object");
				dlg.setMessage("Define business object update information",IMessageProvider.INFORMATION);
				TCComponentForm fgRuleForm = (TCComponentForm) appComps[0];
				targetForm = fgRuleForm;
				String fgCode = fgRuleForm.getProperty("vf4_fg_code");
				String desc = fgRuleForm.getProperty("object_desc");
				double maxQty = fgRuleForm.getDoubleProperty("vf4_max_quantity");
				double minQty = fgRuleForm.getDoubleProperty("vf4_min_quantity");
				String prg = fgRuleForm.getProperty("vf4_program");
				String validateType = fgRuleForm.getProperty("vf4_val_type");
				String variant = fgRuleForm.getProperty("vf4_variant");
				
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
				dlg.getCbPrg().setText(prg);
				dlg.getCbVariant().setText(variant);
				String[] allFG = FamilyGroupRuleCreationHandler.getAllFamilyGroup(this.session, this.fgDifinition);
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
				dlg.getCbFGCode().setText(fgCode+"-"+desc);
				
				try {
					dlg.getCbValiType().setItems(Utils.getLovValues2(session, "vf4_val_type", "VF4_FG_Rule"));
					dlg.getCbValiType().setText(validateType);
				} catch (TCException e1) {
					MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
					return null;
				}
				
				dlg.getTxtMinQty().setText(String.valueOf(minQty));
				dlg.getTxtMinQty().addListener(SWT.Verify, new Listener() {

					@Override
					public void handleEvent(Event arg0) {
						FamilyGroupRuleCreationHandler.onlyAllowDigit(arg0, dlg.getTxtMinQty().getText());
					}
				});
				
				dlg.getTxtMaxQty().setText(String.valueOf(maxQty));
				dlg.getTxtMaxQty().addListener(SWT.Verify, new Listener() {
					@Override
					public void handleEvent(Event arg0) {
						FamilyGroupRuleCreationHandler.onlyAllowDigit(arg0, dlg.getTxtMaxQty().getText());
					}
				});
				
				dlg.getBtnSave().addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if(!FamilyGroupRuleCreationHandler.isFullyInput(dlg)) {
							dlg.setMessage("Please input all required information",IMessageProvider.WARNING);
							return; 
						}else {
							//TODO call function create form
							if(getTargetForm() != null) {
								try {
									if (donorVehDisVal2RealVal.containsValue(dlg.getCbPrg().getText())) {
									    for (final Object entry : donorVehDisVal2RealVal.keySet()) {
									        if (donorVehDisVal2RealVal.get(entry).compareToIgnoreCase(dlg.getCbPrg().getText()) == 0) {
									        	getTargetForm().setProperty("vf4_program", entry.toString());
									        	break;
									        }
									    }
									}else {
										MessageBox.post("Program " + dlg.getCbPrg().getText() + " is not valid", "ERROR", MessageBox.ERROR);
										return;
									}
									getTargetForm().setProperty("vf4_fg_code", dlg.getCbFGCode().getText().split("-")[0]);
									getTargetForm().setProperty("vf4_val_type", dlg.getCbValiType().getText());
									getTargetForm().setProperty("vf4_variant", dlg.getCbVariant().getText());
									getTargetForm().setProperty("object_desc", dlg.getTxtFGDes().getText());
									
									String maxQty = dlg.getTxtMaxQty().getText().isEmpty() ? null : dlg.getTxtMaxQty().getText();
									String minQty = dlg.getTxtMinQty().getText().isEmpty() ? null : dlg.getTxtMinQty().getText();
									getTargetForm().setProperties(new String[] {"vf4_max_quantity", "vf4_min_quantity"}, new String[] {maxQty, minQty});
									dlg.setMessage("Update successfully",IMessageProvider.INFORMATION);
									dlg.getBtnSave().setEnabled(false);
								} catch (TCException e1) {
									dlg.setMessage("Exception: " + e1.toString(),IMessageProvider.ERROR);
								}
							}else {
								dlg.setMessage("Cannot create new Family Group Rule, please contact admin",IMessageProvider.ERROR);
								return; 
							}
						}
					}

			      });
				
				
				dlg.open();
			}
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}
	public LinkedHashMap<String, FGDefinition> getFgDifinition() {
		return fgDifinition;
	}
}
