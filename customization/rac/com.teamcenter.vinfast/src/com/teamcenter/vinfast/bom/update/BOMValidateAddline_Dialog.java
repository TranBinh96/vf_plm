package com.teamcenter.vinfast.bom.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.ResourceManager;

public class BOMValidateAddline_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;

	private Label lblAttribute;
	public Table tblSearch;
	private Label lblNewLabel;
	public Text txtPart;
	private Label lblNewLabel_1;
	public Text txtMakeBuy;
	private TabFolder tabFolder;
	private TabItem tbtmBomAttributes;
	private Composite composite;
	private Label lblNewLabel_3;
	public Combo cbBOMPurchaseLevel;
	private Label lblModuleGroupEnglish;
	public Combo cbBOMModuleGroupEnglish;
	private Label lblMainModuleEnglish;
	public Combo cbBOMMainModuleEnglish;
	private Label lblModuleName;
	public Combo cbBOMModuleName;
	public Text txtPartNumber;
	private Label lblNewLabel_2;
	public Text txtRevision;
	public Button btnSearch;
	private Label lblNewLabel_4;
	public Text txtQuantity;
	private Label lblNewLabel_5;
	public Text txtOccurence;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_3;
	private Label lblNewLabel_6;
	public Text txtParentBomline;
	public Button btnSelectParent;

	public BOMValidateAddline_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginWidth = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(5, false);
			gl_container.marginHeight = 0;
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblAttribute = new Label(container, SWT.NONE);
				lblAttribute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblAttribute.setText("Part ID:");
			}
			{
				txtPartNumber = new Text(container, SWT.BORDER);
				txtPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblNewLabel_2 = new Label(container, SWT.NONE);
				lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel_2.setText("Revision:");
			}
			{
				txtRevision = new Text(container, SWT.BORDER);
				txtRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnSearch = new Button(container, SWT.NONE);
				btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			}
			{
				tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
				GridData gd_tblSearch = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
				gd_tblSearch.widthHint = 689;
				tblSearch.setLayoutData(gd_tblSearch);
				tblSearch.setHeaderVisible(true);
				tblSearch.setLinesVisible(true);
				{
					tblclmnNewColumn = new TableColumn(tblSearch, SWT.NONE);
					tblclmnNewColumn.setWidth(250);
					tblclmnNewColumn.setText("Part");
				}
				{
					tblclmnNewColumn_3 = new TableColumn(tblSearch, SWT.NONE);
					tblclmnNewColumn_3.setWidth(150);
					tblclmnNewColumn_3.setText("Type");
				}
			}
			{
				lblNewLabel_6 = new Label(container, SWT.NONE);
				lblNewLabel_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel_6.setText("Parent Bomline:");
			}
			{
				txtParentBomline = new Text(container, SWT.BORDER);
				txtParentBomline.setEditable(false);
				txtParentBomline.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				btnSelectParent = new Button(container, SWT.NONE);
				btnSelectParent.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/select_editable_target_16.png"));
			}
			{
				tabFolder = new TabFolder(container, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
				{
					tbtmBomAttributes = new TabItem(tabFolder, SWT.NONE);
					tbtmBomAttributes.setText("Bom Attributes");
					{
						composite = new Composite(tabFolder, SWT.NONE);
						tbtmBomAttributes.setControl(composite);
						composite.setLayout(new GridLayout(2, false));
						{
							lblNewLabel = new Label(composite, SWT.NONE);
							lblNewLabel.setText("Part:");
						}
						{
							txtPart = new Text(composite, SWT.BORDER);
							txtPart.setEditable(false);
							txtPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblNewLabel_1 = new Label(composite, SWT.NONE);
							lblNewLabel_1.setText("Part Make/Buy:");
						}
						{
							txtMakeBuy = new Text(composite, SWT.BORDER);
							txtMakeBuy.setEditable(false);
							txtMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						}
						{
							lblNewLabel_5 = new Label(composite, SWT.NONE);
							lblNewLabel_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblNewLabel_5.setText("Number of occurrences:");
						}
						{
							txtOccurence = new Text(composite, SWT.BORDER);
							txtOccurence.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblNewLabel_4 = new Label(composite, SWT.NONE);
							lblNewLabel_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblNewLabel_4.setText("Quantity per occurrences:");
						}
						{
							txtQuantity = new Text(composite, SWT.BORDER);
							txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblNewLabel_3 = new Label(composite, SWT.NONE);
							lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblNewLabel_3.setText("Purchase Level: (*)");
						}
						{
							cbBOMPurchaseLevel = new Combo(composite, SWT.READ_ONLY);
							cbBOMPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbBOMPurchaseLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblModuleGroupEnglish = new Label(composite, SWT.NONE);
							lblModuleGroupEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblModuleGroupEnglish.setText("Module Group English: (*)");
						}
						{
							cbBOMModuleGroupEnglish = new Combo(composite, SWT.READ_ONLY);
							cbBOMModuleGroupEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbBOMModuleGroupEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblMainModuleEnglish = new Label(composite, SWT.NONE);
							lblMainModuleEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblMainModuleEnglish.setText("Main Module English: (*)");
						}
						{
							cbBOMMainModuleEnglish = new Combo(composite, SWT.READ_ONLY);
							cbBOMMainModuleEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbBOMMainModuleEnglish.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
						{
							lblModuleName = new Label(composite, SWT.NONE);
							lblModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
							lblModuleName.setText("Module Name: (*)");
						}
						{
							cbBOMModuleName = new Combo(composite, SWT.READ_ONLY);
							cbBOMModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
							cbBOMModuleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Add", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Add new Bomline");
	}
}
