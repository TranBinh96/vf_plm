package com.teamcenter.vinfast.general.create;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.MessageConst;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class MoldPartCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	TCSession session;

	private Label lblNewLabel_1;
	private Text txtID;
	private Text txtRevision;
	private Text txtName;
	private Text txtDesc;
//	private Combo comboModel;
	private Combo cbCategory;
	private Combo cbPartMakeBuy;
	private Combo cbUOM;
//	private Map<String, String> modelAndPrefixNumber;
	private Button btnCreate = null;

	public MoldPartCreate_Dialog(Shell parentShell, TCSession session) throws TCException {
		super(parentShell);
		this.session = session;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create Mold Part");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			// Load data form
			String[] categoryTypes = TCExtension.GetPreferenceValues("VF_MOLD_PART_TYPES", session);
			String[] makeBuyList = TCExtension.GetLovValues("vf4_item_make_buy", "Item", session);
//			String[] UOM = TCExtension.GetLovValues("uom_tag", "Item", session);
			String[] UOM = TCExtension.GetUOMList(session);

			// init object UI
//			lblNewLabel_1 = new Label(area, SWT.NONE);
//			lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//			lblNewLabel_1.setText("General Information   ");

			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblSubCategory = new Label(container, SWT.NONE);
			lblSubCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSubCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblSubCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblSubCategory.setText("Sub Category: (*)");
			lblSubCategory.setToolTipText("Mandatory field: Please fill to enable Save button");

			cbCategory = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCategory.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbCategory.setItems(categoryTypes);
			cbCategory.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					String value = generateNextID(cbCategory.getText().substring(0, 3), "ME Part");
					txtID.setText(value);
				}
			});

			Label lblId = new Label(container, SWT.NONE);
			lblId.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblId.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblId.setText("ID:");

			txtID = new Text(container, SWT.BORDER);
			txtID.setEnabled(false);
			txtID.setEditable(false);
			txtID.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblRevision = new Label(container, SWT.NONE);
			lblRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblRevision.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblRevision.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblRevision.setText("Revision:");

			txtRevision = new Text(container, SWT.BORDER);
			txtRevision.setEditable(false);
			txtRevision.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtRevision.setText("01");

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtName.setToolTipText("Mandatory field: Please fill to enable Save button");

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblDescription.setText("Description: (*)");

			txtDesc = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			txtDesc.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			txtDesc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesc.heightHint = 100;
			txtDesc.setToolTipText("Mandatory field: Please fill to enable Save button");
			txtDesc.setLayoutData(gd_txtDesc);

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakeBuy.setText("Part Make/Buy: (*)");

			cbPartMakeBuy = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPartMakeBuy.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbPartMakeBuy.setItems(makeBuyList);

			Label lblUnitOfMeasure = new Label(container, SWT.NONE);
			lblUnitOfMeasure.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			lblUnitOfMeasure.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUnitOfMeasure.setText("Unit Of Measure: (*)");

			cbUOM = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbUOM.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			cbUOM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbUOM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbUOM.setItems(UOM);
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
		return area;
	}

	private void onEventClick_BtnCreate() {
		System.out.println(cbCategory.getText());
		String mes = isCreateable();
		if (!mes.isEmpty()) {
			MessageBox.post("Fields below are required:\n " + mes, "Warning", MessageBox.WARNING);
		} else {
			try {
				TCComponentItemRevision newRev = CreatingItems(txtID.getText(), txtName.getText(), txtDesc.getText(), cbUOM.getText(), cbPartMakeBuy.getText(), session);
				if (newRev != null) {
					try {
						newRev.setStringProperty("object_desc", txtDesc.getText().toString());
					} catch (TCException e1) {
						e1.printStackTrace();
					}

					MessageBox.post(txtID.getText() + MessageConst.CREATE_PART_SUCCESS, "Information", MessageBox.INFORMATION);
					resetUI();
				} else {
					MessageBox.post(MessageConst.CREATE_PART_FAIL, "Error", MessageBox.ERROR);
				}
			} catch (TCException e2) {
				e2.printStackTrace();
			}
		}
	}

	private void resetUI() {
		cbCategory.setText("");
		txtID.setText("");
		txtRevision.setText("");
		txtName.setText("");
		txtDesc.setText("");
		cbPartMakeBuy.setText("");
		cbUOM.setText("");
	}

	private String isCreateable() {
		String meString = "";
		if (cbCategory.getText().isEmpty())
			meString += "- Sub Category \n ";
		if (txtID.getText().isEmpty())
			meString += "- ID \n ";
		if (txtName.getText().isEmpty())
			meString += "- Name \n ";
		if (txtDesc.getText().isEmpty())
			meString += "- Description \n ";
		if (cbPartMakeBuy.getText().isEmpty())
			meString += "- Part Make/Buy \n ";
		if (cbUOM.getText().isEmpty())
			meString += "- Unit Of Measure \n ";

		return meString;
	}

	public String generateNextID(String inputString, String type) {
		try {
			System.out.println(inputString);
			String search_Items = "";
			search_Items = inputString;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", type);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				newIDValue = search_Items + StringExtension.ConvertNumberToString(1, 8);
			} else {
				int id = 0;
//				for(int i = 0; i < item_search.length; i++){
//					String split = item_search[i].toString().substring(4,11);
//					if(id < Integer.parseInt(split)) id = Integer.parseInt(split);
//				}
				String split = item_search[0].toString().substring(4, 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 8);
			}
			return newIDValue;
		} catch (Exception ex) {
			System.out.println(" ::: UpdateItemIDTextBox - >  " + ex);
			return null;
		}
	}

	public TCComponentItemRevision CreatingItems(String ID, String Name, String Desc, String uom, String partMakeBuy, TCSession session) throws TCException {
		TCComponentFolder hm_flder;
		TCComponentItemRevision me_item_revision = null;
		TCComponentItem ME_Item;

		try {
			hm_flder = session.getUser().getNewStuffFolder();
			TCComponent item = null;
			me_item_revision = createItem(ID, Name, "VF3_manuf_part", Desc, partMakeBuy, uom);
			hm_flder.add("contents", me_item_revision.getItem());

		} catch (TCException e) {
			e.printStackTrace();
		}

		return me_item_revision;
	}

	private TCComponentItemRevision createItem(String id, String name, String type, String description, String partMakeBuy, String uom) {
		TCComponentItemRevision newItemRevision = null;

		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		DataManagementService dms = DataManagementService.getService(session);

		TCComponent UOMTag = null;
		try {
			String UOMValue = uom;
			TCComponent[] uom_value_tags = TCExtension.GetUOMLOVValues();

			if (!UOMValue.equals("") && uom_value_tags.length != 0) {
				for (int i = 0; i < uom_value_tags.length; i++) {
					TCComponentUnitOfMeasure unit = (TCComponentUnitOfMeasure) uom_value_tags[i];
					if (unit.getProperty("symbol").equals(UOMValue)) {
						UOMTag = uom_value_tags[i];
						break;
					}
				}
			}

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF3_manuf_part";
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			// itemDef.data.stringProps.put("vf4_part_category", "NONE");
			itemDef.data.tagProps.put("uom_tag", UOMTag);

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF3_manuf_partRevision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			ServiceData serviceData = response.serviceData;

			if (serviceData.sizeOfPartialErrors() > 0) {

				String errorMsg = SoaUtil.buildErrorMessage(serviceData);
				MessageBox.post(errorMsg, "Error", MessageBox.ERROR);

			} else {

				CreateOut[] createOutResp = response.output;
				TCComponent[] component = createOutResp[0].objects;
				for (TCComponent rev : component) {
					if (rev.getType().equals("VF3_manuf_partRevision")) {
						newItemRevision = (TCComponentItemRevision) rev;
						return newItemRevision;
					}
				}
			}

		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
		btnCreate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				onEventClick_BtnCreate();
			}
		});
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 450);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Create Mold Part");
	}
}
