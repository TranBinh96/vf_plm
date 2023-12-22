package com.teamcenter.vinfast.admin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.administration._2012_09.PreferenceManagement.CompletePreference;
import com.vf.utils.TCExtension;

public class PreferenceAssistant_Handler extends AbstractHandler {
	private TCSession session;
	private PreferenceAssistant_Dialog dlg;

	private static String[] GROUP_PERMISSION = { "dba" };

	public PreferenceAssistant_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			dlg = new PreferenceAssistant_Dialog(new Shell());
			dlg.create();

			dlg.txtALName.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						refreshPreference();
					}
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updatePreference();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void refreshPreference() {
		dlg.txtName.setText("");
		dlg.txtDesc.setText("");
		dlg.txtValue.setText("");
		String preferenceName = dlg.txtALName.getText();
		if (!preferenceName.isEmpty()) {
			CompletePreference preItem = TCExtension.getPreference(preferenceName, session);
			dlg.txtName.setText(preItem.definition.name);
			dlg.txtDesc.setText(preItem.definition.description);
			String[] preValue = preItem.values.values;
			String value = "";
			if (preValue != null && preValue.length > 0) {
				value = String.join("\r\n", preValue);
			}
			dlg.txtValue.setText(value);
		}
	}

	private void updatePreference() {
		if (dlg.txtName.getText().isEmpty())
			return;

		String newValue = dlg.txtValue.getText();
		String[] newValues = null;
		if (newValue.contains("\r\n")) {
			newValues = newValue.split("\r\n");
		} else {
			newValues = new String[] { newValue };
		}

		String mess = TCExtension.updatePreference(session, dlg.txtName.getText(), newValues);
		if (mess.isEmpty())
			MessageBox.post("Preference update success.", "Error", MessageBox.INFORMATION);
		else
			MessageBox.post(mess, "Error", MessageBox.ERROR);
	}
}
