package com.teamcenter.vinfast.admin;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.VerifyPDFResultModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class VerifyPDF_Handler extends AbstractHandler {
	private TCSession session;
	private static String[] GROUP_PERMISSION = new String[] { "dba" };
	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedList<VerifyPDFResultModel> itemList = null;
	private VerifyPDF_Dialog dlg;

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", 1);
				return null;
			}
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			itemList = new LinkedList<>();
			dlg = new VerifyPDF_Dialog(new Shell());
			dlg.create();
			dlg.btnAccept.addListener(13, new Listener() {
				public void handleEvent(Event arg0) {
//					test1(targetComp);
					verifyProcess(targetComp);
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void test1(InterfaceAIFComponent[] targetComp) {
		try {
			TCComponent selectObject = (TCComponent) targetComp[0];
			selectObject.setProperty("vf4_me_part_type", "test1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void verifyProcess(final InterfaceAIFComponent[] targetComp) {
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Verify progressing...", -1);
					try {
						for (InterfaceAIFComponent target : targetComp) {
							if (target instanceof TCComponentItemRevision) {
								TCComponentItemRevision parentRevison = (TCComponentItemRevision) target;
								if (parentRevison.getPropertyDisplayableValue("object_type").compareTo("DVPR Document Revision") == 0) {
									TCComponent[] testReports = parentRevison.getRelatedComponents("VF4_TestReports");
									if (testReports != null) {
										for (TCComponent vfteItem : testReports) {
											if (vfteItem.getPropertyDisplayableValue("object_type").compareTo("VFTE Document") == 0) {
												TCComponentItemRevision vfteRevision = ((TCComponentItem) vfteItem).getLatestItemRevision();
												TCComponent[] files = vfteRevision.getRelatedComponents("IMAN_specification");
												if (files != null) {
													for (TCComponent pdfFile : files) {
														if (pdfFile instanceof TCComponentDataset && pdfFile.getPropertyDisplayableValue("object_type").compareTo("PDF") == 0)
															checkFileNotAllowed((TCComponentDataset) pdfFile, vfteItem.getPropertyDisplayableValue("item_id"));
													}
												}
											}
										}
									}
								} else if (parentRevison.getPropertyDisplayableValue("object_type").compareTo("AT VFTE Doc Revision") == 0) {
									TCComponent[] files = parentRevison.getRelatedComponents("IMAN_specification");
									if (files != null) {
										for (TCComponent pdfFile : files) {
											if (pdfFile instanceof TCComponentDataset && pdfFile.getPropertyDisplayableValue("object_type").compareTo("PDF") == 0)
												checkFileNotAllowed((TCComponentDataset) pdfFile, parentRevison.getPropertyDisplayableValue("item_id"));
										}
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateReviewerTable();
	}

	private void updateReviewerTable() {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		validationResultText.append(StringExtension.genTableHeader(new String[] { "VFTE", "File Name", "Status" }));
		try {
			for (VerifyPDFResultModel entry : itemList) {
				validationResultText.append("<tr>");
				validationResultText.append("<td><p>" + entry.getVfteID() + "</p></td>");
				validationResultText.append("<td><p>" + entry.getFileName() + "</p></td>");
				if (entry.getErrorMessage().isEmpty()) {
					validationResultText.append("<td><p>" + StringExtension.genBadgetDefault("No need update") + "</p></td>");
				} else if (entry.getErrorMessage().compareTo("Success") == 0) {
					validationResultText.append("<td><p>" + StringExtension.genBadgetSuccess("Success") + "</p></td>");
				} else {
					validationResultText.append("<td><p>" + StringExtension.genBadgetFail(entry.getErrorMessage()) + "</p></td>");
				}
				validationResultText.append("</tr>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");
		dlg.brwReport.setText(validationResultText.toString());
	}

	private void test() {
		try {
			File file = new File("C:\\temp\\MDSReport_1141955003(VF36 RR BIW80251553).pdf");
			PDDocument sourceDocument = Loader.loadPDF(file);
			PDDocument newDocument = new PDDocument();
			for (PDPage page : sourceDocument.getPages())
				newDocument.addPage(page);
			try {
				newDocument.save("C:\\temp\\MDSReport_1141955003(VF36 RR BIW80251553)_clone.pdf");
				newDocument.close();
			} catch (StackOverflowError e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkFileNotAllowed(TCComponentDataset dataset, String vfteID) {
		try {
			String fileName = dataset.getPropertyDisplayableValue("object_name");
			String folder = String.valueOf(createLogFolder(vfteID)) + "\\";
			File file = TCExtension.downloadDataset("C:\\Temp\\", dataset, "PDF", fileName, session);
			if (file != null) {
				VerifyPDFResultModel newItem = new VerifyPDFResultModel();
				newItem.setVfteID(vfteID);
				newItem.setFileName(file.getName());
				try {
					PDDocument sourceDocument = Loader.loadPDF(file);
					AccessPermission accessPermission = sourceDocument.getCurrentAccessPermission();
					if (sourceDocument.isEncrypted()) {
						boolean canModify = accessPermission.canModify();
						if (!canModify) {
							if (cloneFile(sourceDocument, String.valueOf(folder) + fileName)) {
								newItem.setErrorMessage("Success");
								dataset.removeNamedReference("PDF_Reference");
								dataset.setFiles(new String[] { String.valueOf(folder) + fileName }, new String[] { "PDF_Reference" });
							} else {
								newItem.setErrorMessage("Unsuccess");
							}
						}
					}
					sourceDocument.close();
//					file.delete();
				} catch (Exception e) {
					newItem.setErrorMessage("Unsuccess");
				}
				itemList.add(newItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean cloneFile(PDDocument sourceDocument, String path) {
		boolean check = true;
		try {
			PDDocument newDocument = new PDDocument();
			for (PDPage page : sourceDocument.getPages())
				newDocument.addPage(page);
			try {
				newDocument.save(path);
			} catch (StackOverflowError e1) {
				e1.printStackTrace();
				check = false;
			}
			newDocument.close();
		} catch (Exception e) {
			e.printStackTrace();
			check = false;
		}
		return check;
	}

	public static String createLogFolder(String name) {
		String LOG_PATH_U = "C:\\Temp";
		String LOG_PATH = "";
		String logFolder = "";
		File temp_Folder_U = new File(LOG_PATH_U);
		if (!temp_Folder_U.exists() && !temp_Folder_U.isDirectory()) {
			if (temp_Folder_U.mkdir()) {
				LOG_PATH = temp_Folder_U.getPath();
			} else {
				System.out.println("Failed to create log directory!");
			}
		} else {
			LOG_PATH = temp_Folder_U.getPath();
		}
		File file = new File(String.valueOf(LOG_PATH) + "\\" + name);
		if (!file.exists() && !file.isDirectory()) {
			if (file.mkdir()) {
				logFolder = file.getPath();
			} else {
				System.out.println("Failed to create log directory!");
			}
		} else {
			logFolder = file.getPath();
		}
		return logFolder;
	}
}
