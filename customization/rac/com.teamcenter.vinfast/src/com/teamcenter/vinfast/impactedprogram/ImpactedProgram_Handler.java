package com.teamcenter.vinfast.impactedprogram;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedPartBean;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedProgramBean;
import com.vf.utils.TCExtension;

public class ImpactedProgram_Handler extends AbstractHandler {
	private TCSession session;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private ImpactedProgram_Dialog dlg;

	private TCComponentItemRevision tcECRRevision = null;
	private ImpactedProgram_Operations impactedprogramoperations = null;
	private Map<TCComponent, TCComponent[]> impactedProgramMap = null;
	private String[] OBJECTTYPE_AVAILABLE = { "VF Design", "VF Design Revision", "VF Component Design",
			"VF Component Design Revision" };

	public ImpactedProgram_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp.length == 0) {
				MessageBox.post("Please Select Item.", "Error", MessageBox.ERROR);
				return null;
			}

			impactedProgramMap = null;
			// init Data
			String[] revRulesDataForm = TCExtension.GetRevisionRules(session);
			// init UI
			dlg = new ImpactedProgram_Dialog(new Shell());
			dlg.create();

			dlg.cbRevisionRule.setItems(revRulesDataForm);

			dlg.cbRevisionRule.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					loadData(targetComp);
				}
			});

			SelectionAdapter rbtSelectEvent = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateTable();
				}
			};

			dlg.rbtProgramView.addSelectionListener(rbtSelectEvent);

			dlg.rbtPartView.addSelectionListener(rbtSelectEvent);

			dlg.btnSaveExcel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					saveExcel();
				}
			});

			int defaultRevRuleIndex = 0;
			boolean found = false;
			for (String revRule : revRulesDataForm) {
				if (revRule.contains("VINFAST_WORKING_RULE")) {
					found = true;
					break;
				}
				defaultRevRuleIndex++;
			}
			if (found) {
				dlg.cbRevisionRule.select(defaultRevRuleIndex);				
			}
			
			dlg.open();			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void loadData(InterfaceAIFComponent[] targetComp) {
		showProgressDialog(true);
		try {
			String revisionRule = dlg.cbRevisionRule.getText();
			if (revisionRule.isEmpty())
				return;
			ArrayList<TCComponent> inputCompList = new ArrayList<TCComponent>();
			for (InterfaceAIFComponent item : targetComp) {
				if (item instanceof TCComponent) {
					TCComponent itemSelect = (TCComponent) item;
					String objectType = itemSelect.getProperty("object_type");
					if (objectType.compareTo("Engineering Change Request") == 0) {
						TCComponentItemRevision tcECRRevision = (TCComponentItemRevision) ((TCComponentItem) itemSelect).getLatestItemRevision();
						TCComponent[] tcCompArray = tcECRRevision.getReferenceListProperty("CMHasProblemItem");
						for (TCComponent tcComp : tcCompArray)
							if (checkTypeAvailable(tcComp.getProperty("object_type")))
								inputCompList.add(tcComp);
					} else if (objectType.compareTo("Engineering Change Request Revision") == 0) {
						TCComponent[] tcCompArray = itemSelect.getReferenceListProperty("CMHasProblemItem");
						for (TCComponent tcComp : tcCompArray)
							if (checkTypeAvailable(tcComp.getProperty("object_type")))
								inputCompList.add(tcComp);
					} else {
						if (checkTypeAvailable(objectType))
							inputCompList.add(itemSelect);
					}
				}
			}

			// Call Operation service and display result here
			impactedprogramoperations = new ImpactedProgram_Operations(session, inputCompList, revisionRule);
			impactedProgramMap = impactedprogramoperations.getImpactedProgramList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		showProgressDialog(false);
		updateTable();
	}

	private void updateTable() {
		dlg.tblMaster.removeAll();
		if (dlg.rbtProgramView.getSelection()) {
			dlg.tblclmnColumn1.setText("Impacted Vehicle Program");
			dlg.tblclmnColumn2.setText("Selected Component");
			if (impactedProgramMap != null && impactedProgramMap.size() > 0) {
				ArrayList<ImpactedProgramBean> myProgramList = impactedprogramoperations.getImpactedProgramList(impactedProgramMap);
				for (ImpactedProgramBean programItem : myProgramList) {
					TableItem tableItem = new TableItem(dlg.tblMaster, SWT.NONE);
					tableItem.setText(new String[] { programItem.getStrProgramName(), programItem.getStrPartList() });
				}
			}
		} else {
			dlg.tblclmnColumn2.setText("Impacted Vehicle Program");
			dlg.tblclmnColumn1.setText("Selected Component");
			if (impactedProgramMap != null && impactedProgramMap.size() > 0) {
				ArrayList<ImpactedPartBean> myPartList = impactedprogramoperations.getImpactedPartList(impactedProgramMap);
				for (ImpactedPartBean partItem : myPartList) {
					TableItem tableItem = new TableItem(dlg.tblMaster, SWT.NONE);
					tableItem.setText(new String[] { partItem.getStrPartName(), partItem.getStrProgramList() });
				}
			}
		}
	}

	private void saveExcel() {
		try {
			FileDialog dialog = new FileDialog(dlg.getShell(), SWT.SAVE);
			dialog.setFilterNames(new String[] { "Excel Files", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.xlsx", "*.*" });

			String fileDirPath = System.getProperty("user.home") + "\\Downloads";
			dialog.setFilterPath(fileDirPath); // Windows path

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter format1 = DateTimeFormatter.ofPattern("dd-MMM-yyyy_HHmmss");
			DateTimeFormatter format2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

			String currentDate = format1.format(now);
			String reportDate = format2.format(now);

			String filename = "";
			String strECRName = "";

			if (tcECRRevision != null) {
				strECRName = tcECRRevision.getProperty("item_id") + "/" + tcECRRevision.getProperty("item_revision_id") + "-" + tcECRRevision.getProperty("object_name");
				filename = tcECRRevision.getProperty("item_id") + "_" + tcECRRevision.getProperty("item_revision_id") + "_" + Constants.getImpactedProgramExcelFileName() + "_" + currentDate + ".xlsx";
			} else {
				filename = Constants.getImpactedProgramExcelFileName() + "_" + currentDate + ".xlsx";// Windows path
			}

			dialog.setFileName(filename);
			String filepath = dialog.open();

			if (filepath != null) {
				showProgressDialog(true);

				ArrayList<ImpactedProgramBean> myProgramList = impactedprogramoperations.getImpactedProgramList(impactedProgramMap);
				ArrayList<ImpactedPartBean> myPartList = impactedprogramoperations.getImpactedPartList(impactedProgramMap);
				impactedprogramoperations.writeToExcel(filepath, strECRName, reportDate, myProgramList, myPartList);

				showProgressDialog(false);

				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "File saved to system - " + filepath);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean checkTypeAvailable(String objectType) {
		for (String value : OBJECTTYPE_AVAILABLE) {
			if (value.compareTo(objectType) == 0)
				return true;
		}
		return false;
	}
	
	private void showProgressDialog(boolean isShow) {
		if(isShow) {
			if (progressMonitorDialog == null) {
				progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
				progressMonitorDialog.open();
			}
		} else {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
		}
	}
}
