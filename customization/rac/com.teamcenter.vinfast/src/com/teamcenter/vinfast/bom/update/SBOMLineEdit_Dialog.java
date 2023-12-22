package com.teamcenter.vinfast.bom.update;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;

public class SBOMLineEdit_Dialog extends Dialog {
	public Button btnAccept;

	public String valueSelected = "";
	private Label lblDonorVehicle;
	private Label lblValue_2;
	private Label lblValue_3;
	private Label lblValue_4;
	private Label lblValue_5;
	private Label lblValue_6;
	private Label lblValue_7;
	private Label lblValue_8;
	private Label lblValue_9;
	private Label lblValue_10;
	private Label lblValue_1;
	private Label lblRevision;
	private Label lblPartno;

	private ScrolledComposite scrolledComposite;
	private Composite composite;

	public Text txtPOSID;
	public Text txtQuantity;
	public Text txt1stVINNewPart;
	public Text txtRemarks;
	public Text txtRevision;
	public Text txtPartNo;
	public Text txtOriginalPart;
	public Text txtLevel;
	public Text txtPartName;
	private Button btnMarketRemove;
	private Button btnMarketAdd;
	public Button ckbEffectiveDate;
	public Button ckbValidtillDate;
	private Combo cbMarket;
	public Combo cbSupersession;
	public List lstMarket;
	public DateTime datEffectiveDate;
	public DateTime datValidtillDate;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
	private Label lblValue;
	private Label lblValue_11;
	public Combo cbAftersalesCritical;
	public Combo cbEPCVariant;

	public SBOMLineEdit_Dialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(2, false);
		container.setLayout(gl_shell);

		scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		lblPartno = new Label(composite, SWT.NONE);
		lblPartno.setText("PartNo:");
		lblPartno.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblPartno.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		txtPartNo = new Text(composite, SWT.BORDER);
		txtPartNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblRevision = new Label(composite, SWT.NONE);
		lblRevision.setText("Revision:");
		lblRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		txtRevision = new Text(composite, SWT.BORDER);
		txtRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblDonorVehicle = new Label(composite, SWT.NONE);
		lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDonorVehicle.setText("Part Name:");
		lblDonorVehicle.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPartName = new Text(composite, SWT.BORDER);
		txtPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_2 = new Label(composite, SWT.NONE);
		lblValue_2.setText("POSID:");
		lblValue_2.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtPOSID = new Text(composite, SWT.BORDER);
		txtPOSID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_3 = new Label(composite, SWT.NONE);
		lblValue_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_3.setText("Level:");
		lblValue_3.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtLevel = new Text(composite, SWT.BORDER);
		txtLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_4 = new Label(composite, SWT.NONE);
		lblValue_4.setText("Quantity:");
		lblValue_4.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtQuantity = new Text(composite, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_5 = new Label(composite, SWT.NONE);
		lblValue_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_5.setText("Original Part:");
		lblValue_5.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtOriginalPart = new Text(composite, SWT.BORDER);
		txtOriginalPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_6 = new Label(composite, SWT.NONE);
		lblValue_6.setText("Supersession:");
		lblValue_6.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbSupersession = new Combo(composite, SWT.NONE);
		cbSupersession.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbSupersession.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		new Label(composite, SWT.NONE);

		lblValue_7 = new Label(composite, SWT.NONE);
		lblValue_7.setText("Effective Date:");
		lblValue_7.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		datEffectiveDate = new DateTime(composite, SWT.BORDER);
		datEffectiveDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		ckbEffectiveDate = new Button(composite, SWT.CHECK);
		ckbEffectiveDate.setSelection(true);
		ckbEffectiveDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDateTimeControl();
			}
		});

		lblValue_8 = new Label(composite, SWT.NONE);
		lblValue_8.setText("Valid till Date:");
		lblValue_8.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		datValidtillDate = new DateTime(composite, SWT.BORDER);
		datValidtillDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		ckbValidtillDate = new Button(composite, SWT.CHECK);
		ckbValidtillDate.setSelection(true);
		ckbValidtillDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDateTimeControl();
			}
		});

		lblValue_9 = new Label(composite, SWT.NONE);
		lblValue_9.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblValue_9.setText("Market:");
		lblValue_9.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		lstMarket = new List(composite, SWT.BORDER);
		lstMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnMarketRemove = new Button(composite, SWT.NONE);
		btnMarketRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnMarketRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
		btnMarketRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				removeMarket();
			}
		});
		new Label(composite, SWT.NONE);

		cbMarket = new Combo(composite, SWT.READ_ONLY);
		cbMarket.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbMarket.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnMarketAdd = new Button(composite, SWT.NONE);
		btnMarketAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
		btnMarketAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				addMarket();
			}
		});

		lblValue_10 = new Label(composite, SWT.NONE);
		lblValue_10.setText("1st VIN New Part:");
		lblValue_10.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txt1stVINNewPart = new Text(composite, SWT.BORDER);
		txt1stVINNewPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue = new Label(composite, SWT.NONE);
		lblValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue.setText("Aftersales Critical:");
		lblValue.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbAftersalesCritical = new Combo(composite, SWT.READ_ONLY);
		cbAftersalesCritical.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbAftersalesCritical.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_11 = new Label(composite, SWT.NONE);
		lblValue_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblValue_11.setText("EPC Variant:");
		lblValue_11.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbEPCVariant = new Combo(composite, SWT.READ_ONLY);
		cbEPCVariant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbEPCVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);

		lblValue_1 = new Label(composite, SWT.NONE);
		lblValue_1.setText("Remarks:");
		lblValue_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtRemarks = new Text(composite, SWT.BORDER);
		txtRemarks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(composite, SWT.NONE);
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return container;
	}

	public void initData(TCSession session, LinkedHashMap<String, String> bomInfo) {
		try {
			String[] supersessionDataForm = TCExtension.GetLovValues("VF4_supersession_code");
			cbSupersession.setItems(supersessionDataForm);
			String[] marketDataForm = TCExtension.GetPreferenceValues("VINF_SBOM_Info_Market", session);
			cbMarket.setItems(marketDataForm);

			String[] aftersaleDataForm = TCExtension.GetLovValues("VF4_Critical");
			cbAftersalesCritical.setItems(aftersaleDataForm);
			String[] epcVariantDataForm = TCExtension.GetLovValues("VF4_EPC_Variant_LOV");
			cbEPCVariant.setItems(epcVariantDataForm);

			if (bomInfo != null) {
				txtPartNo.setText(bomInfo.get("bl_item_item_id"));
				txtRevision.setText(bomInfo.get("bl_rev_item_revision_id"));
				txtPartName.setText(bomInfo.get("bl_item_object_name"));
				txtPOSID.setText(bomInfo.get("VF3_pos_ID"));
				txtLevel.setText(bomInfo.get("bl_level_starting_0"));
				txtQuantity.setText(bomInfo.get("bl_quantity"));
				txtOriginalPart.setText(bomInfo.get("bl_item_vf4_orginal_part_number"));
				cbSupersession.setText(bomInfo.get("VF4_supersessionCode"));
				cbAftersalesCritical.setText(bomInfo.get("VF4_LNT_af_critical"));
				cbEPCVariant.setText(bomInfo.get("VF4_EPC_Variant"));
				if (bomInfo.get("VF4_effective_date").isEmpty()) {
					ckbEffectiveDate.setSelection(false);
				} else {
					ckbEffectiveDate.setSelection(true);
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(sdf.parse(bomInfo.get("VF4_effective_date")));
						datEffectiveDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (bomInfo.get("VF4_valid_till_date").isEmpty()) {
					ckbValidtillDate.setSelection(false);
				} else {
					ckbValidtillDate.setSelection(true);
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(sdf.parse(bomInfo.get("VF4_valid_till_date")));
						datValidtillDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				txt1stVINNewPart.setText(bomInfo.get("VF4_first_vin_newp"));
				txtRemarks.setText(bomInfo.get("VF4_remarks"));
				String market = bomInfo.get("VF4_market");
				if (!market.isEmpty()) {
					Set<String> marketList = new HashSet<>();
					if (market.contains(",")) {
						for (String item : market.split(",")) {
							if (!item.isEmpty())
								marketList.add(item.trim());
						}
					} else {
						marketList.add(market.trim());
					}

					for (String item : marketList) {
						boolean check = false;
						for (String marketItem : marketDataForm) {
							if (marketItem.compareTo(item) == 0) {
								check = true;
								break;
							}
						}
						if (check)
							lstMarket.add(item);
					}
				}
				updateDateTimeControl();
				disableUI();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addMarket() {
		String market = cbMarket.getText();
		if (!market.isEmpty()) {
			if (checkExistInList(lstMarket.getItems(), market))
				lstMarket.add(market);
		}
	}

	private void removeMarket() {
		int indexMarket = lstMarket.getSelectionIndex();
		if (indexMarket >= 0)
			lstMarket.remove(indexMarket);
	}

	private boolean checkExistInList(String[] origin, String target) {
		if (origin == null || origin.length == 0)
			return true;

		for (String item : origin) {
			if (item.compareTo(target) == 0)
				return false;
		}
		return true;
	}

	private void disableUI() {
		txtPartNo.setEnabled(false);
		txtRevision.setEnabled(false);
		txtPartName.setEnabled(false);
		txtPOSID.setEnabled(false);
		txtLevel.setEnabled(false);
		txtQuantity.setEnabled(false);
		txtOriginalPart.setEnabled(false);
	}

	public void clearUI() {
		txtPartNo.setText("");
		txtRevision.setText("");
		txtPartName.setText("");
		txtPOSID.setText("");
		txtLevel.setText("");
		txtQuantity.setText("");
		txtOriginalPart.setText("");
		cbSupersession.deselectAll();
		cbAftersalesCritical.deselectAll();
		cbEPCVariant.deselectAll();
	}

	private void updateDateTimeControl() {
		datEffectiveDate.setVisible(ckbEffectiveDate.getSelection());
		datValidtillDate.setVisible(ckbValidtillDate.getSelection());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update SBOM Info...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.CLOSE_ID);
	}
}
