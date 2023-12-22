package com.teamcenter.vinfast.mhu;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.vinfast.escooter.sorprocess.model.ResourcesModel;
import com.vf.utils.Query;

public class UpdatePreferredMPNHandler extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(UpdatePreferredMPNHandler.class);
	private String preferredMPN = "";
	public UpdatePreferredMPNHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		if(appComps.length > 1) {
			MessageBox.post("This feature is applicable to single line only.", "Information", MessageBox.INFORMATION);
			return null;
		}
		try {
			TCComponentItemRevision partRev = ((TCComponentBOMLine)appComps[0]).getItemRevision();
			UpdatePreferredMPNDialog dlg = new UpdatePreferredMPNDialog(new Shell());
			dlg.create();
			getMPN(partRev, dlg, session);
			dlg.getTxtMPNPreferred().setText(((TCComponentBOMLine)appComps[0]).getProperty("VF4_preferred_MPN"));
//			dlg.getTxtMPNPreferred().setText(((TCComponentBOMLine)appComps[0]).getProperty("VL5_bom_comment"));
			dlg.getTxtMPNPreferred().setEnabled(false);
			preferredMPN = ((TCComponentBOMLine)appComps[0]).getProperty("VL5_bom_comment");
			dlg.getTblMPN().addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event e) {
					System.out.println("handleEvent///");
					Table tblMPN = dlg.getTblMPN();
					TableItem[] items = tblMPN.getSelection();
					if(!dlg.getTxtMPNPreferred().getText().contains(items[0].getText())) {
						if(!dlg.getTxtMPNPreferred().getText().isEmpty()) {
							dlg.getTxtMPNPreferred().setText(dlg.getTxtMPNPreferred().getText().concat(";").concat(items[0].getText()));
							preferredMPN = dlg.getTxtMPNPreferred().getText();
						}else {
							dlg.getTxtMPNPreferred().setText(items[0].getText());
							preferredMPN = items[0].getText();
						}
					}
				}
			});
			
			dlg.getOk().addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					try {
						System.out.println(preferredMPN);
						((TCComponentBOMLine)appComps[0]).setStringProperty("VF4_preferred_MPN", preferredMPN);
					} catch (TCException e1) {
						logger.error("[getMPN] Exception: " + e1.toString());
					}
				}
			});
			
			dlg.open();
			
		} catch (Exception e) {
			MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}

		return null;
	}
	
	
	private void getMPN(TCComponentItemRevision partRev, UpdatePreferredMPNDialog dlg, TCSession session) {
		try {
			TCComponent[] ecSupplierCmps = partRev.getRelatedComponents("VF4_EC_Supplier_Relation");
			if(ecSupplierCmps != null && ecSupplierCmps.length > 0) {
				dlg.getTblMPN().removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(ecSupplierCmps, new String[] { "object_name", "vf4_supplier_name", "vf4_manufacturer"});

				for (TCComponent obj : ecSupplierCmps) {
					try {
						String[] propValues = obj.getProperties(new String[] { "object_name", "vf4_supplier_name", "vf4_manufacturer" });
						dlg.setTableRow(propValues);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MessageBox.post("Exception: " + e.toString(),"ERROR", MessageBox.ERROR);
					}
				}
				dlg.getTblMPN().redraw();
			}
		} catch (TCException e) {
			logger.error("[getMPN] Exception: " + e.toString());
		}
	}

}
