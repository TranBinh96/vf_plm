package com.teamcenter.vinfast.qcheck;

import java.util.LinkedList;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.subdialog.SearchPartRev_Dialog;
import com.teamcenter.vinfast.suppliercollaboration.DDECreate_Dialog;

public class QCheckRequest_Dialog extends TitleAreaDialog{
	private Composite area;
	private Composite container;
	public Button btnCreate;

	protected Object result;
	public Table tblParts;
	public Text txtDescription;
	public Combo cbRevisionRule;
	
	private LinkedList<TCComponent> partLists = null;

	public QCheckRequest_Dialog(Shell parentShell, TCSession _session) {
		super(parentShell);
//		setBlockOnOpen(false);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		
		try {
			setMessage("Define business object create information");
			setTitle("Create Q-Checker automation ticket");

			container = new Composite(area, SWT.BORDER);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblPartNumber.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 3));
			lblPartNumber.setText("Part Number:");
	
			tblParts = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_tblParts = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gd_tblParts.widthHint = 150;
			gd_tblParts.heightHint = 150;
			tblParts.setLayoutData(gd_tblParts);
			tblParts.setLinesVisible(true);
			tblParts.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partDetails = new TableColumn(tblParts, SWT.FILL);
			partDetails.setResizable(true);
			partDetails.setWidth(145);
			partDetails.setText("Part Number(s)");
			partDetails.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partType = new TableColumn(tblParts, SWT.FILL);
			partType.setResizable(true);
			partType.setWidth(135);
			partType.setText("Object Type");
			partType.setToolTipText("Search and choose Part Numbers (Mandatory");
			
			Button btnSearch = new Button(container, SWT.FLAT);
			btnSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnSearch.setImage(new Image(null, DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/search.png")));
			btnSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchPartRev_Dialog searchDlg = new SearchPartRev_Dialog(parent.getShell());
					searchDlg.open();
					Button ok = searchDlg.getOKButton();
	
					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int[] items = searchDlg.tblSearch.getSelectionIndices();
							
							for (int index = 0; index < items.length; index++) {
								TCComponent item = searchDlg.itemSearch.get(index);
								if (!checkPartDuplicate(item)) {
									try {
										TableItem setItem = new TableItem(tblParts, SWT.NONE);
										String[] value = { item.getPropertyDisplayableValue("object_string"),
															item.getPropertyDisplayableValue("object_type") };
										setItem.setText(value);
									} catch (Exception e2) {
									}
								}
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});
	
			Button btnRemove = new Button(container, SWT.FLAT);
			btnRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnRemove.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
			btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnRemove.setToolTipText("Remove selected parts");
			btnRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					tblParts.remove(tblParts.getSelectionIndices());
				}
			});
			
			Button btnPaste = new Button(container, SWT.FLAT);
			btnPaste.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPaste.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/paste_16.png")));
			btnPaste.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPaste.setToolTipText("Paste copied parts");
			btnPaste.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					AIFClipboard clipBoard = AIFPortal.getClipboard();
					if(!clipBoard.isEmpty()) {
						Vector<TCComponent> clipboardComponents = clipBoard.toVector();
						for(TCComponent objectRevision : clipboardComponents) {
							try {
								String className = objectRevision.getClass().getName();
								String typeName = className.substring(className.lastIndexOf(".")+1, className.length());
								if(typeName.equals("TCComponentItemRevision")) {
									String[] propValues = objectRevision.getProperties(new String[] {"object_string","object_type"});
									TableItem row = new TableItem(tblParts, SWT.NONE);
									row.setText(propValues);
								}
							} 
							catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});
	
			Label lblRevisionRule = new Label(container, SWT.NONE);
			lblRevisionRule.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblRevisionRule.setText("Revision Rule:");
	
			cbRevisionRule = new Combo(container, SWT.BORDER);
			cbRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);
			
			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblDescription.setText("Description:");
	
			txtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDescription.heightHint = 72;
			txtDescription.setLayoutData(gd_txtDescription);
			txtDescription.setToolTipText("Enter Description (Optional)");
			new Label(container, SWT.NONE);
			
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
	
			Label label = new Label(container, SWT.HORIZONTAL);
			GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
			gd_label.widthHint = 431;
			label.setLayoutData(gd_label);
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	private boolean checkPartDuplicate(TCComponent item) {
		if (partLists.size() > 0) {
			for (TCComponent part : partLists) {
				if (part == item)
					return true;
			}
		}

		return false;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create QCA");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(470, 387);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}
}
