package com.teamcenter.rac.jes.handlers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.soa.client.FileManagementUtility;

public class DownloadDialog extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	String selectedFolder = null;
	JTextField JT_ID = null;
	TCComponent[] selectedObject = null;
	TCSession session = null;
	FileManagementService fmService;
	FileManagementUtility fileUtility;
	private static String prefixFileName;
	private String type;

	public DownloadDialog(TCSession session, TCComponent[] selected, String type) {
		selectedObject = selected;
		this.session = session;
		this.type = type;

		createAndShowGUI();
	}

	public void createAndShowGUI() {
		JPanel downloadPanel = new JPanel();
		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/foldercollapsed_16.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));
		downloadPanel.setLayout(null);
		downloadPanel.setBackground(Color.white);
		downloadPanel.setPreferredSize(new Dimension(410, 120));
		setTitle("Download " + type);

		JLabel JL_ID = new JLabel("Choose Path:");
		JL_ID.setBounds(20, 20, 200, 25);

		JT_ID = new JTextField();
		JT_ID.setToolTipText("Choose folder to download...");
		JT_ID.setBounds(115, 20, 210, 25);
		JT_ID.setEditable(false);

		JButton iB_Assign = new JButton(frame_Icon);
		iB_Assign.setBounds(330, 20, 25, 25);
		iB_Assign.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setAcceptAllFileFilterUsed(false);
				int returnValue = jfc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFolder = jfc.getSelectedFile().toString();
					selectedFolder = selectedFolder + "\\";
					JT_ID.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		JSeparator separator = new JSeparator();
		separator.setBounds(20, 70, 330, 25);

		JButton JB_Save = new JButton("Download");
		JB_Save.setIcon(save_Icon);
		JB_Save.setBounds(70, 80, 125, 25);

		JB_Save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JT_ID.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Please choose folder to download files.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					try {
						downloadExcelOperation(selectedObject);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					dispose();
				}
			}
		});

		JButton JB_Cancel = new JButton("Cancel");
		JB_Cancel.setIcon(cancel_Icon);
		JB_Cancel.setBounds(200, 80, 125, 25);
		JB_Cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		});

		downloadPanel.add(JL_ID);
		downloadPanel.add(JT_ID);
		downloadPanel.add(iB_Assign);
		downloadPanel.add(separator);
		downloadPanel.add(JB_Save);
		downloadPanel.add(JB_Cancel);

		getContentPane().add(downloadPanel);
		setIconImage(frame_Icon.getImage());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void downloadExcelOperation(TCComponent[] selectedObject) throws Exception {
		// TODO Auto-generated method stub
		fmService = FileManagementService.getService(session);
		fileUtility = new FileManagementUtility(session.getSoaConnection());
		GenerateJESAction generator = new GenerateJESAction();

		TCComponentBOMLine[] ProcessLines = new TCComponentBOMLine[selectedObject.length];
		for (int i = 0; i < ProcessLines.length; i++) {
			ProcessLines[i] = (TCComponentBOMLine) selectedObject[i];
		}
		/// (TCComponentBOMLine[])selectedObject;
		try {

			for (TCComponentBOMLine ProcessLine : ProcessLines) {
				AIFComponentContext[] workStations = ProcessLine.getChildren();

				for (AIFComponentContext workStation : workStations) {
					TCComponentBOMLine wsBOMLine = (TCComponentBOMLine) workStation.getComponent();
					TCComponent wsRev = wsBOMLine.getItemRevision();
					prefixFileName = wsRev.getStringProperty("object_name");
					prefixFileName = prefixFileName.replace(":", "_").replace("/", "_").replace("\\", "_")
							.replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_")
							.replace("|", "_");
					// Download file code
					downloadDataset(wsRev);

					AIFComponentContext[] wsChilds = wsBOMLine.getChildren();

					for (AIFComponentContext workStationChild : wsChilds) {

						TCComponentBOMLine operationBOMLine = (TCComponentBOMLine) workStationChild.getComponent();
						TCComponent operation = operationBOMLine.getItemRevision();

						if (operation.getType().equals("MEOPRevision")) {
							String origFileName = operation.getPropertyDisplayableValue("object_name");
							origFileName = origFileName.replace(":", "_").replace("/", "_").replace("\\", "_")
									.replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_")
									.replace(">", "_").replace("|", "_");
							String outputFilePathWithoutExtension = "";
							String findNo = operationBOMLine.getStringProperty("bl_sequence_no");
							findNo = (findNo != null && findNo.trim().isEmpty() == false) ? findNo + "_" : "";

							if (origFileName.compareTo(prefixFileName) != 0) {
								outputFilePathWithoutExtension = selectedFolder + prefixFileName + "_" + findNo
										+ origFileName;
							}

							try {
								generator.generateReport(operationBOMLine, outputFilePathWithoutExtension, false);
								// downloadDataset(operation);
							} catch (Exception ex) {
								System.out.println("Donload fail as below.");
								ex.printStackTrace();
							}
						}
					}
				}
			}

		} catch (TCException e) {
			e.printStackTrace();
		}
		MessageBox.post("Download files completed.", "Success", MessageBox.INFORMATION);
	}

	public void downloadDataset(TCComponent wsRev) {
		try {
			if (type.compareTo("JES-SOS") == 0) {
				DownloadBySpecRelation(wsRev);
			} else if (type.compareTo("PFM-PCP") == 0) {
				DownloadByReferRelation(wsRev);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void DownloadBySpecRelation(TCComponent wsRev) throws TCException {
		AIFComponentContext[] arrComponentSpec = wsRev.getRelated("VF4_SOS_Relation");
		if (arrComponentSpec == null || arrComponentSpec.length == 0) {
			arrComponentSpec = wsRev.getRelated("IMAN_specification");
		}
		for (AIFComponentContext componentSpec : arrComponentSpec) {

			TCComponentDataset dataset = (TCComponentDataset) componentSpec.getComponent();
			if (dataset.getType().equals("MSExcelX")) {
				TCComponentTcFile[] files = dataset.getTcFiles();
				FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);
				Map<TCComponentTcFile, String> map = ticketResp.tickets;
				for (TCComponentTcFile tcFile : map.keySet()) {
					String origFileName = tcFile.getProperty("original_file_name");
					String ticket = map.get(tcFile);
					System.out.println("[DownloadAction] Downloading file: " + origFileName);
					String outputFilePath = "";
					if (origFileName.compareTo(prefixFileName) != 0 && wsRev.getType().equals("MEOPRevision")) {
						outputFilePath = selectedFolder + prefixFileName + "_" + origFileName;
					} else {
						outputFilePath = selectedFolder + origFileName;
					}
					fileUtility.getTransientFile(ticket, outputFilePath);
				}
			}
		}
	}

	private void DownloadByReferRelation(TCComponent wsRev) throws TCException {
		AIFComponentContext[] arrComponentRefer = wsRev.getRelated("IMAN_reference");
		for (AIFComponentContext componentRefer : arrComponentRefer) {

			TCComponentDataset dataset = (TCComponentDataset) componentRefer.getComponent();
			if (dataset.getType().equals("MSExcelX")) {
				TCComponentTcFile[] files = dataset.getTcFiles();
				FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);
				Map<TCComponentTcFile, String> map = ticketResp.tickets;
				for (TCComponentTcFile tcFile : map.keySet()) {
					String origFileName = tcFile.getProperty("original_file_name");
					String ticket = map.get(tcFile);
					System.out.println("[DownloadAction] Downloading file: " + origFileName);
					String outputFilePath = "";
					if (origFileName.compareTo(prefixFileName) != 0) {
						outputFilePath = selectedFolder + prefixFileName + "_" + origFileName;
					} else {
						outputFilePath = selectedFolder + origFileName;
					}
					fileUtility.getTransientFile(ticket, outputFilePath);
				}
			}
		}
	}

	public Object callWithProgressMonitor(final Runnable caller, String startMessage) {
		Object object = null;
		final AbstractAIFOperation operation = new AbstractAIFOperation() {
			@Override
			public void executeOperation() throws Exception {
				caller.run();
			}
		};

		try {
			IRunnableWithProgress progress = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor progressMonitor)
						throws InvocationTargetException, InterruptedException {
					progressMonitor.beginTask(operation.getName(), IProgressMonitor.UNKNOWN);
					try {
						operation.executeOperation();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					progressMonitor.done();
				}
			};
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(progress);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			session.setReadyStatus();
		}

		object = operation.getOperationResult();
		return object;
	}
}
