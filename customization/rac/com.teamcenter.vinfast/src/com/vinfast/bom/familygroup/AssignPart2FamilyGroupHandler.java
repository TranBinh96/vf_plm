package com.vinfast.bom.familygroup;

import java.util.LinkedHashMap;

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
import org.eclipse.swt.widgets.Shell;

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
import com.vinfast.bom.familygroup.FamilyGroupRuleCreationHandler.FGDefinition;


public class AssignPart2FamilyGroupHandler extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(AssignPart2FamilyGroupHandler.class);
	private TCSession session;
	private LinkedHashMap<String, FGDefinition> fgDifinition;
	private TCComponentItem[] partTargets;
	public AssignPart2FamilyGroupHandler() {
		super();
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.fgDifinition = new LinkedHashMap<String, FGDefinition>();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		try {
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] appComps = app.getTargetComponents();
			partTargets = new TCComponentItem[appComps.length];
			int i = 0;
			for(InterfaceAIFComponent comm : appComps) {
				TCComponentItem part = (((TCComponent)comm).isTypeOf("Item")) ? (TCComponentItem)comm : (((TCComponent)comm).isTypeOf("ItemRevision")) ? ((TCComponentItemRevision)comm).getItem() : ((TCComponentBOMLine)comm).getItem();
				partTargets[i] = part;
				i++;
			}
			
			AssignPart2FamilyGroupDialog dlg = new AssignPart2FamilyGroupDialog(new Shell());
			dlg.create();
			dlg.setTitle("Update Business Object");
			dlg.setMessage("Define business object update information",IMessageProvider.INFORMATION);
			String[] allFG = FamilyGroupRuleCreationHandler.getAllFamilyGroup(this.session, this.fgDifinition);
			if(allFG != null && allFG.length > 0) {
				dlg.getCbFGCode().setItems(allFG);
			}
			dlg.getCbFGCode().addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent arg0) {
					dlg.getBtnSave().setEnabled(true);
				}
			});
			
			dlg.getBtnSave().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					for(TCComponentItem part : partTargets) {
						try {
							part.setProperty("vf4_fg_code", dlg.getCbFGCode().getText().split("-")[0]);
						} catch (TCException e1) {
							dlg.setMessage("Exception: " + e1.toString(), IMessageProvider.ERROR);
							return;
						}
					}
					dlg.setMessage("Update successfully", IMessageProvider.INFORMATION);
					dlg.getBtnSave().setEnabled(false);
				}
			});
			
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

}
