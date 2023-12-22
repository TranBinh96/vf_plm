package com.teamcenter.vinfast.car.engineering.update;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class DocumentRevisionAttributeUpdate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private DocumentRevisionAttributeUpdate_Dialog dlg;
	private static String[] OBJECT_TYPE_AVAILABLE = new String[] { "VF3_spec_docRevision", "VF3_AT_TGSS_DocRevision", "VF3_AT_VFTE_DocRevision", "VF4_AT_DFMEA_docRevision", "VF3_VTSS_docRevision", "VF3_CMFS_docRevision", "VF3_AT_ESOM_DocRevision", "VF3_DTSS_docRevision", "VF4_VFFS_docRevision",
			"VF3_AT_DVPR_docRevision" };
	private static String[] GROUP_PERMISSION = new String[] { "QSC.VINFAST", "dba" };
	private boolean isReleaseDateNull = true;
	private boolean isReviseDateNull = true;

	public DocumentRevisionAttributeUpdate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		if (validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one " + String.join(" / ", OBJECT_TYPE_AVAILABLE) + ".", "Information", MessageBox.INFORMATION);
			return null;
		}

		if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
			MessageBox.post("You don't have write access to update information.", "Warning", MessageBox.WARNING);
			return null;
		}

		try {
			dlg = new DocumentRevisionAttributeUpdate_Dialog(new Shell());
			dlg.create();

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateItem();
				}
			});

			dlg.btnInfoTargetReleaseDate.setVisible(false);
			dlg.btnInfoTargetReviseDate.setVisible(false);
			updateCurrentValue();
			validateUpdate();
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void updateItem() {
		try {
			if (dlg.btnInfoTargetReleaseDate.getVisible()) {
				Calendar targetReleaseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReleaseDate);
				selectedObject.setDateProperty("vf4_target_release_date", targetReleaseDate.getTime());
			}

			if (dlg.btnInfoTargetReviseDate.getVisible()) {
				Calendar targetReviseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReviseDate);
				selectedObject.setDateProperty("vf4_target_revise_date", targetReviseDate.getTime());
			}

			dlg.setMessage("Update successfully.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			dlg.setMessage("Update unsuccessfully. Exception: " + e.getMessage(), IMessageProvider.ERROR);
			e.printStackTrace();
		}
	}

	private void validateUpdate() {
		try {
			boolean isTargetReleaseDateUpdateable = true;
			if (!isReleaseDateNull && !TCExtension.checkUserHasGroup(GROUP_PERMISSION, session)) {
				isTargetReleaseDateUpdateable = false;
				dlg.btnInfoTargetReleaseDate.setVisible(true);
				dlg.btnInfoTargetReleaseDate.setToolTipText("You don't have write access to update Target Release Date property. Please contact with ISO team.");
				dlg.datTargetReleaseDate.setEnabled(false);
			}

			boolean isTargetReviseDateUpdateable = true;
			if (!isReviseDateNull && !TCExtension.checkUserHasGroup(GROUP_PERMISSION, session)) {
				isTargetReviseDateUpdateable = false;
				dlg.btnInfoTargetReviseDate.setVisible(true);
				dlg.btnInfoTargetReviseDate.setToolTipText("You don't have write access to update Target Revise Date property. Please contact with ISO team.");
				dlg.datTargetReviseDate.setEnabled(false);
			}

			dlg.btnAccept.setEnabled(isTargetReleaseDateUpdateable || isTargetReviseDateUpdateable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateCurrentValue() {
		try {
			Date targetReleaseDatePro = selectedObject.getDateProperty("vf4_target_release_date");
			if (targetReleaseDatePro != null) {
				isReleaseDateNull = false;
				Calendar targetReleaseDate = Calendar.getInstance();
				targetReleaseDate.setTime(targetReleaseDatePro);
				dlg.datTargetReleaseDate.setDate(targetReleaseDate.get(Calendar.YEAR), targetReleaseDate.get(Calendar.MONTH), targetReleaseDate.get(Calendar.DAY_OF_MONTH));
			}

			Date targetReviseDatePro = selectedObject.getDateProperty("vf4_target_revise_date");
			if (targetReviseDatePro != null) {
				isReviseDateNull = false;
				Calendar targetReviseDate = Calendar.getInstance();
				targetReviseDate.setTime(targetReviseDatePro);
				dlg.datTargetReviseDate.setDate(targetReviseDate.get(Calendar.YEAR), targetReviseDate.get(Calendar.MONTH), targetReviseDate.get(Calendar.DAY_OF_MONTH));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		try {
			if (targetComponents == null)
				return true;

			if (targetComponents.length > 1)
				return true;

			if (targetComponents[0] instanceof TCComponentItemRevision) {
				selectedObject = (TCComponentItemRevision) targetComponents[0];
			}

			if (selectedObject == null)
				return true;

			String type = selectedObject.getType();
			return !Arrays.asList(OBJECT_TYPE_AVAILABLE).contains(type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}
