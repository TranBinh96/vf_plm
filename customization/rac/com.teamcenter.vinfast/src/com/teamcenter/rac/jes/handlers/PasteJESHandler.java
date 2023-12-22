package com.teamcenter.rac.jes.handlers;

import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AIFTransferable;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PasteJESHandler  extends AbstractHandler {
	private ProgressMonitorDialog progressMonitorDialog = null;
	StringBuilder errorLog = null;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		errorLog = new StringBuilder();
		InterfaceAIFComponent[] aifComps = AIFUtility.getCurrentApplication().getTargetComponents();
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AIFClipboard clipBoard = AIFPortal.getClipboard();
		if(clipBoard.isEmpty() == false) {
			AIFTransferable transferable = (AIFTransferable) clipBoard.getContents(clipBoard.getOwner());
			List<InterfaceAIFComponent> interfaceComponents = transferable.getComponents();
			if(interfaceComponents.isEmpty() == true) {
				MessageBox.post("No objects copied to paste to selected Items", "Paste...", MessageBox.INFORMATION);
			}else {
				if (progressMonitorDialog == null)
					progressMonitorDialog = new ProgressMonitorDialog(new Shell());
				try {
					progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Pasting...", IProgressMonitor.UNKNOWN);
							for (InterfaceAIFComponent component : transferable.getComponents()) {
								if(component.getType().equals("Image")) {
									for(InterfaceAIFComponent AIFComponent : aifComps) {
										TCComponentBOMLine bomline =  (TCComponentBOMLine)AIFComponent;
										try {
											TCComponentItemRevision revision = bomline.getItemRevision();
											revision.add("TC_Attaches", (TCComponent)component);
										}catch (TCException e) {
											// TODO Auto-generated catch block
											errorLog.append(e.getMessage());
											errorLog.append("\n");
											e.printStackTrace();
										}
									}
								}
								if(component.getType().equals("CfgActivityLine")) {
									TCComponent newActivity = (TCComponent)component;
									ArrayList<TCComponentMEActivity> newActivityArray = getActivityLines(newActivity);
									for(InterfaceAIFComponent AIFComponent : aifComps) {
										TCComponentBOMLine selectedBomline =  (TCComponentBOMLine)AIFComponent;
										try {
											String value = newActivity.getProperty("al_activity_vf4_detail_step_desc");
											TCComponent existingActivity = selectedBomline.getRelatedComponent("bl_me_activity_lines");
											if(existingActivity != null) {
												TCComponentCfgActivityLine existingActivityLine =  (TCComponentCfgActivityLine)existingActivity;
												existingActivityLine.setProperty("al_activity_vf4_detail_step_desc", value);
												ArrayList<TCComponentMEActivity> existingActivityArray = getActivityLines(existingActivity);
												for(TCComponentMEActivity activity : existingActivityArray) {
													activity.delete();
												}
												if(newActivityArray.isEmpty() == false) {
													existingActivity.getDefaultPasteRelation();
													for(TCComponent step : newActivityArray) {
														existingActivity.add("contents",step);
													}
												}
											}
										}catch (TCException e) {
											// TODO Auto-generated catch block
											errorLog.append(e.getMessage());
											errorLog.append("\n");
											e.printStackTrace();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
							clipBoard.clearClipboard();
							monitor.done();
							if (monitor.isCanceled()){
								throw new InterruptedException("The long running operation was cancelled");
							}
						}
					});
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(errorLog.length() == 0) {
					MessageBox.post("Successfully pasted JES to Operation(s)", "Paste...", MessageBox.INFORMATION);
				}else {
					MessageBox.post(new Window(new Frame()), errorLog.toString(),"Warning",MessageBox.WARNING,true);
				}
				
			}
		}

		return null;
	}

	private ArrayList<TCComponentMEActivity> getActivityLines(TCComponent component){
		ArrayList<TCComponentMEActivity> activitiesList = null;
		try {
			TCComponentCfgActivityLine activityLine =  (TCComponentCfgActivityLine)component;
			AIFComponentContext[] rootActivity = activityLine.getRelated("me_cl_source");
			if(rootActivity != null) {
				activitiesList =  new ArrayList<TCComponentMEActivity>();
				TCComponentMEActivity rootActivityLine = (TCComponentMEActivity) rootActivity[0].getComponent();
				AIFComponentContext[] rootActivityChilds = rootActivityLine.getRelated("contents");
				for (AIFComponentContext content : rootActivityChilds) {
					if (content.getComponent().getClass().getSimpleName().equals(TCComponentMEActivity.class.getSimpleName())) {
						activitiesList.add((TCComponentMEActivity) content.getComponent());
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return activitiesList;
	}

}
