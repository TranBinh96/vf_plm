package com.teamcenter.vinfast.car.engineering.specdoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONObject;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.vinfast.model.VRMModel;
import com.teamcenter.vinfast.model.VRMModel.AttachmentModel;
import com.teamcenter.vinfast.model.VRMModel.CommentModel;
import com.vf.utils.MessageConst;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class PulseIntegration_Handler extends AbstractHandler {
	private TCSession session;
	private PulseIntegration_Dialog dlg;
	private int maxPartInCall = 10;

	public PulseIntegration_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			dlg = new PulseIntegration_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			dlg.btnFolderSelect.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					DirectoryDialog dialog = new DirectoryDialog(new Shell(), SWT.OPEN);
					dialog.setFilterPath("c:\\");
					dlg.txtDownloadFolder.setText(dialog.open());
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					downloadMRV();
//					test();
				}
			});

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void test() {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pulse-ttl.riada.se/rest/api/2/search?jql=key%20in(VRM-30170,VRM-30840)&fields=attachment")).header("Accept", "*/*").header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
					.header("Authorization", "Basic VmluZmFzdDpTdXBwbGllcjEh").method("GET", HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.body());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void downloadMRV() {
		if (dlg.txtMRV.getText().isEmpty() || dlg.txtDownloadFolder.getText().isEmpty()) {
			dlg.setMessage(MessageConst.REQUIRED_INFORMATION, IMessageProvider.WARNING);
			return;
		}

		String folder = dlg.txtDownloadFolder.getText();
		String[] vrmIDList = dlg.txtMRV.getText().split(",");

		LinkedHashMap<String, VRMModel> vrmList = new LinkedHashMap<String, VRMModel>();
		try {
			// get VRM Info
			Set<String> uidList = new HashSet<String>();
			int i = 0;
			for (String vrmID : vrmIDList) {
				uidList.add(vrmID);
				i++;
				if (i % maxPartInCall == 0 || i == vrmIDList.length) {
					try {
						JSONObject json = getVRMInfo(String.join(",", uidList));
						JSONArray issues = json.getJSONArray("issues");
						if (issues != null) {
							for (Object issueItem : issues) {
								VRMModel newItem = new VRMModel();
								String key = ((JSONObject) issueItem).getString("key");
								JSONObject fields = ((JSONObject) issueItem).getJSONObject("fields");
								JSONArray attachment = fields.getJSONArray("attachment");
								if (attachment != null) {
									for (Object attachmentItem : attachment) {
										String fileName = ((JSONObject) attachmentItem).getString("filename");
										String path = ((JSONObject) attachmentItem).getString("content");
										newItem.addAttachment(fileName, path);
									}
								}

								vrmList.put(key, newItem);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					i = 0;
					uidList = new HashSet<String>();
				}
			}

			// get VRM Comment
			for (Map.Entry<String, VRMModel> entry : vrmList.entrySet()) {
				JSONObject json = getVRMComment(entry.getKey());
				if (json != null) {
					JSONArray comments = json.getJSONArray("comments");
					if (comments != null) {
						for (Object commentItem : comments) {
							String body = ((JSONObject) commentItem).getString("body");
							String createDate = ((JSONObject) commentItem).getString("created");
							String authorName = "";
							String authorEmail = "";
							JSONObject author = ((JSONObject) commentItem).getJSONObject("author");
							if (author != null) {
								authorName = ((JSONObject) author).getString("displayName");
								authorEmail = ((JSONObject) author).getString("emailAddress");
							}

							entry.getValue().addComment(body, authorName, authorEmail, createDate);
						}
					}
				}
			}

			//
			for (Map.Entry<String, VRMModel> entry : vrmList.entrySet()) {
				String vrmID = entry.getKey();
				String vrmFolder = folder + "/" + vrmID;
				TCExtension.createFolder(vrmFolder);

				for (AttachmentModel attach : entry.getValue().attachments) {
					downloadAttachFile(attach.fileName, attach.path, vrmFolder);
				}

				createCommentLogFile(entry.getValue().comments, vrmFolder);
			}
			dlg.setMessage("Download successfully.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONObject getVRMInfo(String vrmIDList) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pulse-ttl.riada.se/rest/api/2/search?jql=key%20in(" + vrmIDList + ")&fields=attachment")).header("Accept", "*/*").header("Authorization", "Basic VmluZmFzdDpTdXBwbGllcjEh")
					.method("GET", HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.body());

			return new JSONObject(response.body());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject getVRMComment(String vrmID) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pulse-ttl.riada.se/rest/api/2/issue/" + vrmID + "/comment")).header("Accept", "*/*").header("Authorization", "Basic VmluZmFzdDpTdXBwbGllcjEh").method("GET", HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.body());

			return new JSONObject(response.body());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	private void downloadAttachFile(String fileName, String fileURL, String folder) {
//		try {
//			URL url = new URL(fileURL);
//			try (InputStream in = url.openStream()) {
//				Path savePath = Path.of(folder, fileName);
//				Files.createDirectories(savePath.getParent());
//				Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private void downloadAttachFile(String fileName, String fileURL, String folder) {
		String username = "Vinfast";
		String password = "Supplier1!";
		try {
			URL url = new URL(fileURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set basic authentication header
			String authString = username + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
			String authHeaderValue = "Basic " + encodedAuth;
			connection.setRequestProperty("Authorization", authHeaderValue);

			InputStream inputStream = connection.getInputStream();

			FileOutputStream outputStream = new FileOutputStream(folder + "\\" + fileName);

			int bytesRead;
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createCommentLogFile(LinkedList<CommentModel> commentList, String folder) {
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet spreadsheet = wb.createSheet("Data");

			int rowCounter = 0;
			int cellCounter = 0;
			String[] headers = new String[] { "Author Name", "Author Email", "Content", "Create Date" };
			XSSFRow headerRow = spreadsheet.createRow(rowCounter++);
			cellCounter = 0;
			for (String header : headers) {
				XSSFCell cell = headerRow.createCell(cellCounter++);
				cell.setCellValue(header);
			}

			for (CommentModel commentItem : commentList) {
				XSSFRow bodyRow = spreadsheet.createRow(rowCounter++);

				XSSFCell cell = bodyRow.createCell(0);
				cell.setCellValue(commentItem.authorName);

				XSSFCell cell1 = bodyRow.createCell(1);
				cell1.setCellValue(commentItem.authorEmail);

				XSSFCell cell2 = bodyRow.createCell(2);
				cell2.setCellValue(commentItem.content);

				XSSFCell cell3 = bodyRow.createCell(3);
				cell3.setCellValue(commentItem.createDate);
			}

			String fileName = "Comment_" + StringExtension.getTimeStamp() + ".xlsx";
			File file = new File(folder + "/" + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
