package com.teamcenter.vinfast.impactedprogram;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedPartBean;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedProgramBean;
import com.teamcenter.vinfast.impactedprogram.providers.ImpactedPartTableCLProvider;
import com.teamcenter.vinfast.impactedprogram.providers.ImpactedProgramTableCLProvider;
import com.teamcenter.vinfast.impactedprogram.providers.ImpactedProgramTableEditingSupport;


public class ImpactedProgramDialog extends Shell 
{
	private Group groupImpactedProgramBox_ = null;
	private Group groupImpactedPartBox_ = null;
	private Table tableImpactedProgram_ = null;
	private Table tableImpactedPart_ = null;
	private Button buttonSave2Excel_ = null;
	private Button buttonAddReport2ECR_ = null;
	private TableViewerColumn tableColumnArrayForTableImpactedProgram_[] = null;
	private TableViewerColumn tableColumnArrayForTableImpactedPart_[] = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private Point point;
	private Float resizeFactor = 0.0f;
	private boolean bChangeReferenceButton = true;
	private TCComponentItemRevision tcECRRevision = null;
	
	public ImpactedProgramDialog(Shell parentShell, final Display display, TCSession session, InterfaceAIFComponent[] tcComponentList) 
	{
		super(parentShell, SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			double width = screenSize.getWidth();
			double height = screenSize.getHeight();
			point = display.getDPI();
			resizeFactor = (float)((point.x * 1.024)/100);
			
			setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast.impactedprogram", "icons/teamcenter_app_16.png"));
			setSize((int)(Math.min(width, 800) * resizeFactor), (int)(Math.min(height, 600) * resizeFactor));
			setText("Impacted Programs");
			
			Shell activeShell = display.getActiveShell();
			Rectangle activeShellBounds = activeShell.getBounds();
			Rectangle rectangle = this.getBounds();
			
			int xAxisLength = activeShellBounds.x + ((activeShellBounds.width - rectangle.width) / 2);
			int yAxisLength = activeShellBounds.y + ((activeShellBounds.height - rectangle.height) / 2);
			
			this.setLocation(xAxisLength, yAxisLength);
			
			ArrayList<TCComponent> inputCompList = new ArrayList<TCComponent>();
			
			if (tcComponentList.length == 1) {
				TCComponent tcComponent = (TCComponent)tcComponentList[0];
				String objType = tcComponent.getProperty("object_type");
				if (objType.equals("Engineering Change Request Revision")) {
					String relStatus = tcComponent.getProperty("release_status_list");
					tcECRRevision = (TCComponentItemRevision)tcComponent;
					if (relStatus.length() > 0) {
						boolean result = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "Engineering Change Request Revision is Released. '" + Constants.getBtnAddReport2ECR() + "' button shall be disabled. Do you want to continue?");
						
						if (result) {
							bChangeReferenceButton = false;
							TCComponent[] tcCompArray = tcComponent.getReferenceListProperty("CMHasProblemItem");
							for (TCComponent tcComp : tcCompArray)
								inputCompList.add(tcComp);
						}							
						else {
							super.getShell().dispose();
							return;
						}
					}
					else {
						TCComponent[] tcCompArray = tcComponent.getReferenceListProperty("CMHasProblemItem");
						for (TCComponent tcComp : tcCompArray)
							inputCompList.add(tcComp);
					}
				}
				else if (objType.equals("Engineering Change Request")) {
					TCComponentItem tcECRItem = (TCComponentItem)tcComponent;
					tcECRRevision = (TCComponentItemRevision)tcECRItem.getLatestItemRevision();
					String relStatus = tcECRRevision.getProperty("release_status_list");
					if (relStatus.length() > 0) {
						boolean result = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "Latest Engineering Change Request Revision is Released. '" + Constants.getBtnAddReport2ECR() + "' button shall be disabled. Do you want to continue?");
						
						if (result) {
							bChangeReferenceButton = false;
							TCComponent[] tcCompArray = tcECRRevision.getReferenceListProperty("CMHasProblemItem");
							for (TCComponent tcComp : tcCompArray)
								inputCompList.add(tcComp);
						}							
						else {
							super.getShell().dispose();
							return;
						}
					}
					else {
						TCComponent[] tcCompArray = tcECRRevision.getReferenceListProperty("CMHasProblemItem");
						for (TCComponent tcComp : tcCompArray)
							inputCompList.add(tcComp);
					}
				}
				else {
					bChangeReferenceButton = false;
					inputCompList.add(tcComponent);
				}					
			}
			else if (tcComponentList.length > 1) {
				bChangeReferenceButton = false;
				boolean bECRFlag = false;
				for (int ii = 0; ii < tcComponentList.length; ii++) {
					TCComponent tcComponent = (TCComponent)tcComponentList[ii];
					String objType = tcComponent.getProperty("object_type");
					if (objType.equals("Engineering Change Request") 
							|| objType.equals("Engineering Change Request Revision")) {
						bECRFlag = true;
						break;
					}
					else {
						inputCompList.add(tcComponent);
					}
				}
				
				if (bECRFlag) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "Engineering Change Request Revision object should not be selected with other objects. Please select Engineering Change Request Revision object only.");
					super.getShell().dispose();
					return;
				}				
			}
			
			if (progressMonitorDialog == null) {
				progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
				progressMonitorDialog.open();
			}
			
			TCPreferenceService tcPref = session.getPreferenceService();			
			String[] revisionRuleList = tcPref.getStringValuesAtLocation("TC_config_rule_name", com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation.OVERLAY_LOCATION);
			String revisionRule;
			
			if (revisionRuleList != null)
				revisionRule = revisionRuleList[0];
			else
				revisionRule = "VINFAST_WORKING_RULE";
			
			//Call Operation service and display result here
			ImpactedProgram_Operations impactedprogramoperations = new ImpactedProgram_Operations(session, inputCompList, revisionRule);
			Map<TCComponent, TCComponent[]> impactedProgramMap = impactedprogramoperations.getImpactedProgramList();
			
			if (impactedProgramMap == null || impactedProgramMap.size() == 0) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "No Impacted Programs found for selected Components");
				super.getShell().dispose();
			}
			else {
				ArrayList<ImpactedPartBean> myPartList = impactedprogramoperations.getImpactedPartList(impactedProgramMap);
				ArrayList<ImpactedProgramBean> myProgramList = impactedprogramoperations.getImpactedProgramList(impactedProgramMap);
				
				//Trying with Table data
				groupImpactedProgramBox_ = new Group(this, SWT.NONE);
				//groupImpactedProgramBox_.setText("Program View");
				groupImpactedProgramBox_.setBounds(10, 50, (int)(Math.min(width, 800) * resizeFactor) - 40, (int)(Math.min(height, 600) * resizeFactor) - 165);
				
				groupImpactedPartBox_ = new Group(this, SWT.NONE);
				//groupImpactedPartBox_.setText("Part View");
				groupImpactedPartBox_.setBounds(10, 50, (int)(Math.min(width, 800) * resizeFactor) - 40, (int)(Math.min(height, 600) * resizeFactor) - 165);
				
				//CheckboxTableViewer tableImpactedProgramViewer_ = CheckboxTableViewer.newCheckList(groupImpactedProgramBox_, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
				Label revRuleLabel = new Label(this, SWT.NONE);
				revRuleLabel.setText("Revision Rule: ");
				revRuleLabel.setFont(SWTResourceManager.getFont(Constants.getFont(), Constants.getFontSize(), SWT.BOLD));
				revRuleLabel.setBounds(10, 10, 135, 30);
				
				Label revRuleValueLabel = new Label(this, SWT.NONE);
				revRuleValueLabel.setText(revisionRule);
				//revRuleValueLabel.setFont(SWTResourceManager.getFont(Constants.getFont(), Constants.getFontSize(), SWT.BOLD));
				revRuleValueLabel.setBounds(145, 10, 400, 30);
				
				Button buttonProgramView = new Button(this, SWT.RADIO);
				buttonProgramView.setText("Program View");
				buttonProgramView.setSelection(true);
				buttonProgramView.setBounds(825, 10, 150, 30);
				
		        Button buttonPartView = new Button(this, SWT.RADIO);
		        buttonPartView.setText("Part View");
		        buttonPartView.setBounds(995, 10, 150, 30);
		        
		        if (buttonPartView.getSelection())
		        	System.out.println("Button Part View is pressed");
		        
		        buttonProgramView.addSelectionListener(new SelectionAdapter()  {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		                Button source=  (Button) e.getSource();
		                if(source.getSelection())  {
		                	groupImpactedProgramBox_.setVisible(true);
		                	groupImpactedPartBox_.setVisible(false);
		                }
		            }
		        });
		        buttonPartView.addSelectionListener(new SelectionAdapter()  {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		                Button source=  (Button) e.getSource();
		                if(source.getSelection())  {
		                	groupImpactedProgramBox_.setVisible(false);
		                	groupImpactedPartBox_.setVisible(true);
		                }
		            }
		        });
		        
				
				TableViewer tableImpactedProgramViewer_ = new TableViewer(groupImpactedProgramBox_, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
				ImpactedProgramTableCLProvider provider = new ImpactedProgramTableCLProvider(activeShell);
				tableImpactedProgramViewer_.setContentProvider(provider);
				tableImpactedProgramViewer_.setLabelProvider(provider);
				
				tableImpactedProgram_ = tableImpactedProgramViewer_.getTable();
				tableImpactedProgram_.setBounds(0, 0, (int)(Math.min(width, 800) * resizeFactor) - 40, (int)(Math.min(height, 600) * resizeFactor) - 165);
				tableImpactedProgram_.setHeaderVisible(true);
				tableImpactedProgram_.setLinesVisible(true);

				
				tableColumnArrayForTableImpactedProgram_ = new TableViewerColumn[2];
				
				tableColumnArrayForTableImpactedProgram_[0] = new TableViewerColumn(tableImpactedProgramViewer_, SWT.NONE);
				tableColumnArrayForTableImpactedProgram_[0].getColumn().setText(Constants.getTblHdrImpactedPrograms());
				tableColumnArrayForTableImpactedProgram_[0].getColumn().setWidth(450);
				tableColumnArrayForTableImpactedProgram_[0].setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element)
					{
						ImpactedProgramBean impProgBean = (ImpactedProgramBean) element;
						return impProgBean.getStrProgramName();
					}
				});
				tableColumnArrayForTableImpactedProgram_[0].setEditingSupport(new ImpactedProgramTableEditingSupport(tableImpactedProgramViewer_, "Impacted Programs", 0));
				
				
				tableColumnArrayForTableImpactedProgram_[1] = new TableViewerColumn(tableImpactedProgramViewer_, SWT.NONE);
				tableColumnArrayForTableImpactedProgram_[1].getColumn().setText(Constants.getTblHdrSelectedParts());
				tableColumnArrayForTableImpactedProgram_[1].getColumn().setWidth(650);
				tableColumnArrayForTableImpactedProgram_[1].setLabelProvider(new ColumnLabelProvider() {
				    @Override
				    public String getText(Object element) {			        
				    	ImpactedProgramBean impProgBean = (ImpactedProgramBean) element;
				    	return impProgBean.getStrPartList();
				    }
				});
				tableColumnArrayForTableImpactedProgram_[1].setEditingSupport(new ImpactedProgramTableEditingSupport(tableImpactedProgramViewer_, "Impacted Programs", 1));
				
				tableImpactedProgramViewer_.setInput(myProgramList);
				
				
				
				//Part View Table
				TableViewer tableImpactedPartViewer_ = new TableViewer(groupImpactedPartBox_, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

				ImpactedPartTableCLProvider impactedPartProvider = new ImpactedPartTableCLProvider(activeShell);
				tableImpactedPartViewer_.setContentProvider(impactedPartProvider);
				tableImpactedPartViewer_.setLabelProvider(impactedPartProvider);

				tableImpactedPart_ = tableImpactedPartViewer_.getTable();
				// tableHistory_ = new Table(groupBoxHistory_, SWT.BORDER | SWT.FULL_SELECTION);
				tableImpactedPart_.setBounds(0, 0, (int)(Math.min(width, 800) * resizeFactor) - 40, (int)(Math.min(height, 600) * resizeFactor) - 165);
				tableImpactedPart_.setHeaderVisible(true);
				tableImpactedPart_.setLinesVisible(true);
				
				tableColumnArrayForTableImpactedPart_ = new TableViewerColumn[2];
				
				tableColumnArrayForTableImpactedPart_[0] = new TableViewerColumn(tableImpactedPartViewer_, SWT.NONE);
				tableColumnArrayForTableImpactedPart_[0].getColumn().setText(Constants.getTblHdrSelectedParts());
				tableColumnArrayForTableImpactedPart_[0].getColumn().setWidth(450);
				tableColumnArrayForTableImpactedPart_[0].setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element)
					{
						ImpactedPartBean impPartBean = (ImpactedPartBean) element;
				    	return impPartBean.getStrPartName();
					}
				});
				tableColumnArrayForTableImpactedPart_[0].setEditingSupport(new ImpactedProgramTableEditingSupport(tableImpactedPartViewer_, "Part View", 0));
				
				
				tableColumnArrayForTableImpactedPart_[1] = new TableViewerColumn(tableImpactedPartViewer_, SWT.NONE);
				tableColumnArrayForTableImpactedPart_[1].getColumn().setText(Constants.getTblHdrImpactedPrograms());
				tableColumnArrayForTableImpactedPart_[1].getColumn().setWidth(650);
				tableColumnArrayForTableImpactedPart_[1].setLabelProvider(new ColumnLabelProvider() {
				    @Override
				    public String getText(Object element) {			        
				    	ImpactedPartBean impPartBean = (ImpactedPartBean) element;
				    	return impPartBean.getStrProgramList();
				    }
				});
				tableColumnArrayForTableImpactedPart_[1].setEditingSupport(new ImpactedProgramTableEditingSupport(tableImpactedPartViewer_, "Part View", 1));
				
				tableImpactedPartViewer_.setInput(myPartList);
				
				buttonSave2Excel_ = new Button(this, SWT.NONE);
				buttonSave2Excel_.setText(Constants.getBtnSave2Excel());
				buttonSave2Excel_.setFont(SWTResourceManager.getFont(Constants.getFont(), Constants.getFontSize(), SWT.BOLD));
				buttonSave2Excel_.setBounds(725, (int)(Math.min(height, 600) * resizeFactor) - 100, 165, 35);
				buttonSave2Excel_.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent selectionEvent) {
						saveExcel(parentShell, display, impactedprogramoperations, myPartList, myProgramList);
					}
				});

					
				
				
				buttonAddReport2ECR_ = new Button(this, SWT.NONE);
				buttonAddReport2ECR_.setText(Constants.getBtnAddReport2ECR());
				buttonAddReport2ECR_.setFont(SWTResourceManager.getFont(Constants.getFont(), Constants.getFontSize(), SWT.BOLD));
				buttonAddReport2ECR_.setBounds(896, (int)(Math.min(height, 600) * resizeFactor) - 100, 250, 35);
				buttonAddReport2ECR_.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent selectionEvent) {
						try {
							LocalDateTime now = LocalDateTime.now();
							DateTimeFormatter format1 = DateTimeFormatter.ofPattern("dd-MMM-yyyy_HHmmss");
							DateTimeFormatter format2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
							
					 		String currentDate = format1.format(now);
					 		String reportDate = format2.format(now);
					 		
					 		String strECRName = tcECRRevision.getProperty("item_id") + "/" + tcECRRevision.getProperty("item_revision_id") + "-" + tcECRRevision.getProperty("object_name");
							String filename = tcECRRevision.getProperty("item_id") + "_" + tcECRRevision.getProperty("item_revision_id") + "_" + Constants.getImpactedProgramExcelFileName() + "_" + currentDate + ".xlsx";// Windows path
					 		String filepath = System.getProperty("java.io.tmpdir") + "/" + filename;
							
							if (filepath != null) {
								if (progressMonitorDialog == null) {
									progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
									progressMonitorDialog.open();
								}
								
								impactedprogramoperations.writeToExcel(filepath, strECRName, reportDate, myProgramList, myPartList);
								
								File file = new File(filepath);
								impactedprogramoperations.createAndAttachReportDataset(tcECRRevision, file);
								
								if (progressMonitorDialog != null) {
									progressMonitorDialog.close();
									progressMonitorDialog = null;
								}
								
								bChangeReferenceButton = false;
								buttonAddReport2ECR_.setEnabled(false);
								
								if (file.exists())
									file.delete();
							}							
						}
						catch (Exception ex) {
							if (progressMonitorDialog != null) {
								progressMonitorDialog.close();
								progressMonitorDialog = null;
							}
							ex.printStackTrace();
						}
						finally {
							if (!display.getActiveShell().isDisposed()) {
								display.getActiveShell().forceFocus();
							}
						}
					}
				});
				
				if ((myProgramList.size() == 1 
						&& myProgramList.get(0).getStrProgramName().equals(Constants.getNoImpactedProgram()))
						|| !bChangeReferenceButton)
					buttonAddReport2ECR_.setEnabled(false);
			
//				saveExcel(parentShell, display, impactedprogramoperations, myPartList, myProgramList);
//				this.close();
			}
			
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
			
			
		}
		catch (Exception ex) {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
			ex.printStackTrace();
		}
		finally {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
				
				
			}
		}
	}
	
	@Override
	protected void checkSubclass()
	{
		//disable the check that prevents subclassing of swt components
	}
	
	public void saveExcel(Shell parentShell, final Display display,
			ImpactedProgram_Operations impactedprogramoperations, ArrayList<ImpactedPartBean> myPartList,
			ArrayList<ImpactedProgramBean> myProgramList) {
		try {
			FileDialog dialog = new FileDialog(parentShell, SWT.SAVE);
			dialog.setFilterNames(new String[] { "Excel Files", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.xlsx", "*.*" });
		
			String fileDirPath = System.getProperty("user.home") + "\\Downloads";
			dialog.setFilterPath(fileDirPath); // Windows path
			
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter format1 = DateTimeFormatter.ofPattern("dd-MMM-yyyy_HHmmss");
			DateTimeFormatter format2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
			
	 		String currentDate = format1.format(now);
	 		String reportDate = format2.format(now);
	 		
	 		String filename = "";
	 		String strECRName = "";
	 		
	 		if (tcECRRevision != null) {
	 			strECRName = tcECRRevision.getProperty("item_id") + "/" + tcECRRevision.getProperty("item_revision_id") + "-" + tcECRRevision.getProperty("object_name");
				filename = tcECRRevision.getProperty("item_id") + "_" + tcECRRevision.getProperty("item_revision_id") + "_" + Constants.getImpactedProgramExcelFileName() + "_" + currentDate + ".xlsx";// Windows path
	 		}
	 		else {
	 			filename = Constants.getImpactedProgramExcelFileName() + "_" + currentDate + ".xlsx";// Windows path
	 		}
	 		
			dialog.setFileName(filename);							
			String filepath = dialog.open();
			
			if (filepath != null) {
				if (progressMonitorDialog == null) {
					progressMonitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
					progressMonitorDialog.open();
				}
				
				impactedprogramoperations.writeToExcel(filepath, strECRName, reportDate, myProgramList, myPartList);
				
				if (progressMonitorDialog != null) {
					progressMonitorDialog.close();
					progressMonitorDialog = null;
				}
				
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Constants.getMsgBoxTitle(), "File saved to system - " + filepath);
			}
		}
		catch (Exception ex) {
			if (progressMonitorDialog != null) {
				progressMonitorDialog.close();
				progressMonitorDialog = null;
			}
			ex.printStackTrace();
		}
		finally {
			if (!display.getActiveShell().isDisposed()) {
				display.getActiveShell().forceFocus();
			}
		}
	}

}
