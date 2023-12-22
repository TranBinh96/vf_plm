package com.teamcenter.rac.jes;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.TCExtension;

public class JESMigrationByLine_Handler extends AbstractHandler {
	private TCSession session;
	private JESMigrationByLine_Dialog dlg;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedList<TCComponentBOPLine> lineList = null;
	private static String[] GROUP_PERMISSION = { "dba" };

	public JESMigrationByLine_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] appComps = app.getTargetComponents();

			if (appComps == null || appComps.length == 0) {
				MessageBox.post("This feature is applicable to single part only.", "Warning", MessageBox.WARNING);
				return null;
			}

			lineList = new LinkedList<TCComponentBOPLine>();
			for (InterfaceAIFComponent interfaceAIFComponent : appComps) {
				if (interfaceAIFComponent instanceof TCComponentBOPLine)
					lineList.add((TCComponentBOPLine) interfaceAIFComponent);
			}

			dlg = new JESMigrationByLine_Dialog(new Shell());
			dlg.create();

			dlg.tblShop.removeAll();

			for (TCComponentBOPLine child : lineList) {
				TableItem item = new TableItem(dlg.tblShop, SWT.NONE);
				try {
					item.setText(new String[] { "", child.getPropertyDisplayableValue("bl_indented_title") });
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					migration();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void migration() {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Export Report...", IProgressMonitor.UNKNOWN);
					try {
						int i = 0;
						for (TCComponentBOPLine line : lineList) {
							monitor.subTask("Report processed: " + i++ + "/" + lineList.size());
							JESMigration_Thread trigger = new JESMigration_Thread("", line, session);
							trigger.run();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			MessageBox.post("The report is exported successfully to folder C:\\temp.", "Information", MessageBox.INFORMATION);
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}
}
