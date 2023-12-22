package com.teamcenter.vinfast.admin;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.VerifyPDFResultModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.VFRacDefine;
import com.vf.utils.VFRacDefine.PartExtraInformationForm;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
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

public class PartExtraInforUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private static String[] GROUP_PERMISSION = new String[] { "dba" };
	private ProgressMonitorDialog progressMonitorDialog = null;
	private PartExtraInforUpdate_Dialog dlg;

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", 1);
				return null;
			}
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			dlg = new PartExtraInforUpdate_Dialog(new Shell());
			dlg.create();
			dlg.btnAccept.addListener(13, new Listener() {
				public void handleEvent(Event arg0) {
					partExtraInforUpdate(targetComp);
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void partExtraInforUpdate(final InterfaceAIFComponent[] targetComp) {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Verify progressing...", -1);
					try {
						for (InterfaceAIFComponent target : targetComp) {
							if (target instanceof TCComponentItemRevision) {
								TCComponentItemRevision ecrRevison = (TCComponentItemRevision) target;
								if (ecrRevison.getType().compareTo("Vf6_ECRRevision") == 0) {
									String ecrNumber = ecrRevison.getPropertyDisplayableValue("item_id");
									TCComponent[] partList = ecrRevison.getRelatedComponents("Cm0HasProposal");
									if (partList != null) {
										for (TCComponent partRev : partList) {
											if (partRev.getType().compareTo("VF4_DesignRevision") == 0) {
												TCComponentItem partItem = ((TCComponentItemRevision) partRev).getItem();
												TCComponent[] forms = partItem.getRelatedComponents("VF4_part_extra_infor_rela");
												if (forms != null && forms.length > 0) {
													if (forms[0].getPropertyDisplayableValue("object_type").compareTo("Part Extra Information") == 0) {
														updatePartExtraInfor((TCComponentItemRevision) partRev, forms[0], ecrNumber);
													}
												}
											}
										}
									}
								} else if (ecrRevison.getType().compareTo("VF4_DesignRevision") == 0) {

								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePartExtraInfor(TCComponentItemRevision partRevision, TCComponent form, String ecrNumber) {
		try {
			String releaseStatus = partRevision.getPropertyDisplayableValue("release_status_list");
			String dateRelease = partRevision.getPropertyDisplayableValue("date_released");

			String maturityLevels = form.getPropertyDisplayableValue(PartExtraInformationForm.PROPERTY_MATURITY_LEVEL);
			String releaseDates = form.getPropertyDisplayableValue(PartExtraInformationForm.PROPERTY_ALL_RELEASE_DATE);
			String ecrNumbers = form.getPropertyDisplayableValue(PartExtraInformationForm.PROPERTY_ECR_NUMBERL);

			if (!maturityLevels.isEmpty()) {
				boolean check = false;
				for (String status : maturityLevels.split(";")) {
					if (status.compareTo(releaseStatus) == 0) {
						check = true;
						break;
					}
				}

				if (check)
					return;
			}

			form.setStringProperty(PartExtraInformationForm.PROPERTY_MATURITY_LEVEL, maturityLevels.isEmpty() ? releaseStatus : maturityLevels + ";" + releaseStatus);
			form.setStringProperty(PartExtraInformationForm.PROPERTY_ALL_RELEASE_DATE, releaseDates.isEmpty() ? dateRelease : releaseDates + ";" + dateRelease);
			if (!ecrNumber.isEmpty()) {
				form.setStringProperty(PartExtraInformationForm.PROPERTY_ECR_NUMBERL, ecrNumbers.isEmpty() ? ecrNumber : ecrNumbers + ";" + ecrNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
