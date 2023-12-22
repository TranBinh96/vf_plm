package com.teamcenter.vinfast.car.engineering.update;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class OriginalPartNumberUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnAccept;
	public Button btnRemove;
	public Text txtPartNumber;
	public Button btnSearch;
	public Table tblSearchResult;
	public Button btnAdd;
	public Table tblOriginalPart;
	private Label label;
	private TableColumn tblclmnPartNumber;
	private TableColumn tblclmnPartNumber_1;
	private TableColumn tblclmnModuleGroup;

	public OriginalPartNumberUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Original Part Number");
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblVehicleCategory = new Label(container, SWT.NONE);
			lblVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblVehicleCategory.setText("ID:");
			{
				txtPartNumber = new Text(container, SWT.BORDER);
				txtPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSearch = new Button(container, SWT.NONE);
				GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_btnSearch.widthHint = 25;
				btnSearch.setLayoutData(gd_btnSearch);
				btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			}
			{
				tblSearchResult = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
				GridData gd_tblSearchResult = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
				gd_tblSearchResult.heightHint = 109;
				tblSearchResult.setLayoutData(gd_tblSearchResult);
				tblSearchResult.setHeaderVisible(true);
				tblSearchResult.setLinesVisible(true);
				{
					tblclmnPartNumber = new TableColumn(tblSearchResult, SWT.NONE);
					tblclmnPartNumber.setWidth(410);
					tblclmnPartNumber.setText("Part Number");
				}
			}
			{
				btnAdd = new Button(container, SWT.NONE);
				GridData gd_btnAdd = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
				gd_btnAdd.widthHint = 25;
				btnAdd.setLayoutData(gd_btnAdd);
				btnAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
			}
			{
				tblOriginalPart = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
				tblOriginalPart.setLinesVisible(true);
				tblOriginalPart.setHeaderVisible(true);
				GridData gd_tblOriginalPart = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
				gd_tblOriginalPart.heightHint = 161;
				tblOriginalPart.setLayoutData(gd_tblOriginalPart);
				{
					tblclmnPartNumber_1 = new TableColumn(tblOriginalPart, SWT.NONE);
					tblclmnPartNumber_1.setWidth(210);
					tblclmnPartNumber_1.setText("Part Number");
				}
				{
					tblclmnModuleGroup = new TableColumn(tblOriginalPart, SWT.NONE);
					tblclmnModuleGroup.setWidth(200);
					tblclmnModuleGroup.setText("Module Group");
				}
			}
			{
				btnRemove = new Button(container, SWT.NONE);
				btnRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
				GridData gd_btnRemove = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
				gd_btnRemove.widthHint = 25;
				btnRemove.setLayoutData(gd_btnRemove);
			}
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Update");
	}
}
