package com.teamcenter.vinfast.handlers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.rac.administration.OrganizationManagementService;
import com.teamcenter.services.internal.rac.administration._2012_10.OrganizationManagement.GetOrganizationUserMembersResponse;
import com.teamcenter.services.internal.rac.administration._2012_10.OrganizationManagement.GroupElement;
import com.teamcenter.services.internal.rac.administration._2012_10.OrganizationManagement.OrganizationMembersInput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.utils.Utils;

public class CheckCATUploadFailed extends AbstractHandler {

	public static final Logger logger = Logger.getLogger(CheckCATUploadFailed.class);
	private final TCSession session;
	
	public CheckCATUploadFailed() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			Date currentDate = new Date();
			long oneDaysBefore = currentDate.getTime() - (24*3600*1000);
			Date date = new Date(oneDaysBefore);
			final String searchFromDate = dateFormatter.format(date);//"03-Apr-1900 00:00";// 
			final String searchToDate = dateFormatter.format(currentDate);
			final String currentUserName = session.getUserName();
			final Collection<String> errorPartsStr = new HashSet<String>();
			AbstractAIFOperation op = new AbstractAIFOperation() {

				@Override
				public void executeOperation() throws Exception {
					searchFailedUpload(searchFromDate, currentUserName, errorPartsStr);
				}

				private void searchFailedUpload(final String searchFromDate, final String currentUserName,
						final Collection<String> errorPartsStr) throws Exception {
					final TCComponent[] currentUserTodayModifiedFiles = Utils.executeSavedQuery("CATIA Upload Files",
							new String[] { "*.CAT*", currentUserName, searchFromDate },
							new String[] { "File Type", "Id", "Date Created" });
					DataManagementService dm = DataManagementService.getService(session);

					ServiceData response = dm.getProperties(currentUserTodayModifiedFiles,
							new String[] { "file_size", "original_file_name" });
					for (int i = 0; i < response.sizeOfPlainObjects(); i++) {
						try {
							TCComponent file = response.getPlainObject(i);
							String fileSize = file.getProperty("file_size");
							if (fileSize == null || fileSize.trim().isEmpty() ||fileSize.trim().startsWith("0 ")) {
								for (AIFComponentContext ds : file.whereReferenced()) {
									TCComponent dsComp = (TCComponent)ds.getComponent();
									if ((dsComp.getType().toLowerCase().contains("catdrawing") || dsComp.getType().toLowerCase().contains("catpart") || dsComp.getType().toLowerCase().contains("catproduct"))&& dsComp.getProperty("object_string").contains(";") == false) {
//										String originFileName = file.getProperty("original_file_name");
//										if (originFileName.lastIndexOf("_") != originFileName.indexOf("_")) {
//											originFileName = originFileName.replace(originFileName.substring(originFileName.lastIndexOf("_"), originFileName.lastIndexOf(".")), "");
//										}
//										String partString = originFileName.substring(0, originFileName.lastIndexOf(".")).replace("_",
//												"/");
										String partString = dsComp.getPropertyDisplayableValue("object_name");
										errorPartsStr.add(partString);
									}
								}
							}
						} catch (TCException e) {
							logger.error(e);
							e.printStackTrace();
						}
					}
				}
			};

			op.setName("Check CAD Upload");
			session.queueOperationAndWait(op);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if (errorPartsStr.isEmpty() == false) {
						showFailUploadPartInfo(errorPartsStr);
					} else {
						String currentDate = dateFormatter.format(new Date());
						MessageBox.post("Not detect any fail CATIA upload belonged to TC user \"" + currentUserName + "\" from " + searchFromDate + " to " + currentDate + ".",
								"Information", MessageBox.INFORMATION);
					}
				}
			});

			createLog(errorPartsStr, currentUserName, searchFromDate, searchToDate);
			
		} catch (Exception e1) {
			logger.error(e1);
			e1.printStackTrace();
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void createLog(final Collection<String> errorPartsStr, final String currentUserName, final String searchFromDate, final String searchToDate) throws ServiceException, TCException {
		AbstractAIFOperation op = new AbstractAIFOperation() {
			
			@Override
			public void executeOperation() throws Exception {
				String part_type="Item Master";
				DataManagementService dms = DataManagementService.getService(session) ;
				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = part_type; //Item BO
				itemDef.data.stringProps.put("object_name", currentUserName + "#" + searchFromDate + "#" + searchToDate);
				
				StringBuffer desc = new StringBuffer();
				StringBuffer itemComment = new StringBuffer();
				// max 40 
				for (String errorPart : errorPartsStr) {
					String addingStr = errorPart + "#";
					if (desc.length() + addingStr.length() <= 240) {
						desc.append(addingStr);
					} else if (itemComment.length() + addingStr.length() <= 240) {
						itemComment.append(addingStr);
					}
				}
				itemDef.data.stringProps.put("object_desc", desc.toString());
				itemDef.data.stringProps.put("item_comment", itemComment.toString());
				
				CreateResponse createLogResponse;
				createLogResponse = dms.createObjects(new CreateIn[] { itemDef });
				if (createLogResponse.output.length >= 1 && createLogResponse.output[0].objects.length >= 1) {
					logger.error("NOT_ERROR: create log object successfully - response.output[0].objects.length=" + createLogResponse.output[0].objects.length);
					changeOwnerShipToDBA(itemDef, createLogResponse);
				} else {
					logger.error("create log object FAIL!");
					if (createLogResponse.serviceData.sizeOfPartialErrors() > 0) {
						for (int i = 0; i < createLogResponse.serviceData.sizeOfPartialErrors(); i++) {
							logger.error(Arrays.asList(createLogResponse.serviceData.getPartialError(i).getMessages()).toString());
						}
					}
				}
			}
		};
		op.setName("Logging");
		session.queueOperationAndWait(op);
	}

	@SuppressWarnings("unchecked")
	private void changeOwnerShipToDBA(CreateIn itemDef, CreateResponse createLogResponse) throws TCException {
		TCComponentUser dbaUser = null;
		TCComponentGroup dbaGroup = null;
		OrganizationManagementService orgSrv = OrganizationManagementService.getService(session);
		OrganizationMembersInput input = new OrganizationMembersInput();
		input.groupName = "dba";
		input.roleName = "DBA";
		input.userID = "infodba";
		input.includeInactive = false;
		input.includeSubGroups = false;
		input.userName = "infodba";
		
		GetOrganizationUserMembersResponse orgResponse = orgSrv.getOrganizationGroupMembers(input);
		Map<TCComponentGroup, GroupElement> map = orgResponse.groupElementMap;
		if (map.size() > 0) {
			for (Entry<TCComponentGroup, GroupElement> entry : map.entrySet()) {
				com.teamcenter.services.internal.rac.administration._2012_10.OrganizationManagement.GroupElement foundGM = entry.getValue();
				if (foundGM.members.length > 0 && foundGM.members[0].members.length > 0) {
					TCComponentGroupMember gm = ((com.teamcenter.services.internal.rac.administration._2011_06.OrganizationManagement.RoleUsers)foundGM.members[0]).members[0];
					dbaUser = gm.getUser();
					dbaGroup = gm.getGroup();
				}
			}
			
		}
		
		if (dbaUser != null && dbaGroup != null) {
			itemDef.data.tagProps.put("owning_user", dbaUser);
			itemDef.data.tagProps.put("owning_group", dbaGroup);
			createLogResponse.output[0].objects[0].changeOwner(dbaUser, dbaGroup);
		}
	}

	private void showFailUploadPartInfo(final Collection<String> errorPartsStr) {
		StringBuffer html = new StringBuffer();
		html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
		html.append("<tr><td><b>No</b></td><td><b>Part Info</b></td><td><b>Note</b></td></tr>");

		int i = 1;
		for (String errorPart : errorPartsStr) {
			html.append("<tr><td>").append(Integer.toString(i)).append("</td>").append("<td>")
					.append(errorPart).append("</td>").append("<td>Please re-upload CATIA files for the part.</td>")
					.append("</tr>");
			i++;
		}

		html.append("</table><br/>To re-upload:<br/>" + 
				"1) Open the failing files from catuii OR your local backup in CATIA window<br/>" + 
				"2) Save back to Teamcenter with option “Force Save for unmodified files” enabled in \"Save Manager dialog\"<br/>" + 
				"3) Verify by \"Tools (VF) -> CATIA Upload Validation\" after uploaded<br/>" + 
				"*Refer to full guideline working over VPN </body></html>");
		StringViewerDialog dlg = new StringViewerDialog(new String[] { html.toString() });

		dlg.setTitle("Upload CATIA Fail Summary");
		dlg.setVisible(true);
	}

}
