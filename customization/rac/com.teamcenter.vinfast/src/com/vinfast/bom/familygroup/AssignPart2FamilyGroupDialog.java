package com.vinfast.bom.familygroup;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.handlers.ModuleGroupInputAutomation;
import com.teamcenter.vinfast.utils.Utils;

import vfplm.soa.common.service.VFUtility;

public class AssignPart2FamilyGroupDialog extends TitleAreaDialog {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	private Combo cbFGCode;
	private Button btnSave;
	private static final Logger logger = Logger.getLogger(AssignPart2FamilyGroupDialog.class);

	public AssignPart2FamilyGroupDialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	public Button getBtnSave() {
		return btnSave;
	}



	public void setBtnSave(Button btnSave) {
		this.btnSave = btnSave;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		try {
			Composite container = new Composite(area, SWT.NONE);
			GridLayout glContainer = new GridLayout(1, true);
			glContainer.marginRight = 10;
			glContainer.marginLeft = 10;
			container.setLayout(glContainer);
			GridData gdContainer = new GridData(GridData.FILL_BOTH);
			gdContainer.grabExcessVerticalSpace = false;
			gdContainer.widthHint = 680;
			container.setLayoutData(gdContainer);
			
			cbFGCode = new Combo(container, SWT.FILL);
			cbFGCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", true);
		btnSave.setEnabled(false);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Assign Part to Family Group");
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		//super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(353, 236);
	}

	public Combo getCbFGCode() {
		return cbFGCode;
	}

	public void setCbFGCode(Combo cbFGCode) {
		this.cbFGCode = cbFGCode;
	}
	
}
