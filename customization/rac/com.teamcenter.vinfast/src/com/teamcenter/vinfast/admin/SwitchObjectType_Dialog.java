package com.teamcenter.vinfast.admin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCException;

public class SwitchObjectType_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button btnCreate;
	private Table tblItem;
	public List lstOrigin;
	public List lstReplace;
	private Label lblNewLabel;
	public Text txtObjectType;

	public SwitchObjectType_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("New Label");
			}
			{
				txtObjectType = new Text(container, SWT.BORDER);
				txtObjectType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lstOrigin = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd_lstOrigin = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
				gd_lstOrigin.heightHint = 196;
				gd_lstOrigin.widthHint = 224;
				lstOrigin.setLayoutData(gd_lstOrigin);
			}
			{
				lstReplace = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd_lstReplace = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
				gd_lstReplace.heightHint = 134;
				lstReplace.setLayoutData(gd_lstReplace);
			}
			{
				tblItem = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
				tblItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				tblItem.setHeaderVisible(true);
				tblItem.setLinesVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Switch Object Type");
	}
}
