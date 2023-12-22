package com.teamcenter.vinfast.handlers;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentRoleType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.rac.core._2007_01.DataManagement;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.vf.dialog.FrameWithOneComboBox;
import com.vf.utils.EnvelopeSend;
import com.vf98.services.rac.customsrv.*;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ESourcingAssignToCL extends AbstractHandler {
	/**
	 * The constructor.
	 */

	FrameWithOneComboBox frame = null;
	TCSession session = null;
	TCComponentGroup group = null;
	TCComponentRole role = null;
	TCComponentUser user = null;
	boolean isvalidObject = true;
	String selected_CL = null;
	InterfaceAIFComponent[] targetComp = null;
	HashMap<String, TCComponentUser> clUsers;
	boolean save_result = false;

	public ESourcingAssignToCL() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from
	 * the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

				session = (TCSession) AIFUtility.getCurrentApplication().getSession();
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				if (group.toString().equals("Purchase.Scooter")) {
					clUsers = getEscooterCLList();
				} else {
					clUsers = getCarCLList();
				}

				if ((group.toString().equals("Purchase") && role.toString().equals("CL")
						|| group.toString().equals("Purchase.Scooter") && role.toString().equals("CL")
						|| group.toString().equals("dba") && role.toString().equals("DBA")) == false) {
					MessageBox.post(
							"CL role users are authorized to perform this operation. Please contact Administrator.",
							"Not Authorized", MessageBox.ERROR);
					return;
				} else {
					isvalidObject = validateObjects(targetComp);
					if (isvalidObject == false) {
						MessageBox.post(
								"Operation cannot be performed on Checked-Out objects. \nPlease check-in or ignore checked-out objects and retry.",
								"Error", MessageBox.ERROR);
						return;
					}
					createDialog();
				}
			}
		});
		return null;
	}

	public HashMap<String, TCComponentUser> getCarCLList() {

		// <UserName, UserObject>
		HashMap<String, TCComponentUser> cl_users = new HashMap<String, TCComponentUser>();

		try {

			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
			TCComponentGroup group = groupType.find("Purchase");

			TCComponentRole CL_role = roleType.find("CL");
			TCComponentGroupMember[] CL_groupMem = GMType.findByRole(CL_role, group);

			for (int j = 0; j < CL_groupMem.length; j++) {
				cl_users.put(CL_groupMem[j].getUser().toString(), CL_groupMem[j].getUser());
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cl_users;
	}

	public HashMap<String, TCComponentUser> getEscooterCLList() {

		// <UserName, UserObject>
		HashMap<String, TCComponentUser> cl_users = new HashMap<String, TCComponentUser>();

		try {

			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
			TCComponentGroup group = groupType.find("Purchase.Scooter");

			TCComponentRole CL_role = roleType.find("CL");
			TCComponentGroupMember[] CL_groupMem = GMType.findByRole(CL_role, group);

			for (int j = 0; j < CL_groupMem.length; j++) {
				cl_users.put(CL_groupMem[j].getUser().toString(), CL_groupMem[j].getUser());
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cl_users;
	}

	public boolean validateObjects(InterfaceAIFComponent[] targetComp) {

		for (int i = 0; i < targetComp.length; i++) {

			TCComponent object = (TCComponent) targetComp[i];

			if (object.isCheckedOut() == true) {
				return false;
			}
		}

		return true;
	}

	public void createDialog() {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource("/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new FrameWithOneComboBox();
		frame.setTitle("Assign to CL Dialog");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(450, 150));

		frame.label1.setText("Choose CL:");
		frame.btnLeft.setIcon(save_Icon);
		frame.btnLeft.setText("Assign");
		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");

		for (String tu : clUsers.keySet()) {
			frame.comboBox1.addItem(tu);
		}

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				selected_CL = frame.comboBox1.getSelectedItem().toString();
				if (selected_CL == null) {

					MessageBox.post("Please select CL.", "Error", MessageBox.ERROR);
					return;
				}

				save_result = true;
				saveOperation(targetComp);
				if (save_result)
					notifyAssignedCL();

				frame.dispose();
			}

		});

		frame.btnRight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void saveOperation(InterfaceAIFComponent[] targetComp) {

		transferOwnership(targetComp);

	}

	private void notifyAssignedCL() {
		int err = 0;
		try {
			// 1. check if new CL is not current CL
			String currentUser = user.toString();
			if (currentUser.compareTo(selected_CL) == 0)
				return;

			// 2. find email of new CL to send
			TCComponentUser newCL = clUsers.get(selected_CL);

			String email = null;

			List<Object> personInfo = newCL.getUserInformation();
			err = 311;

			for (Object obj : personInfo) {

				if (obj instanceof TCComponentPerson) {
					String pa9 = ((TCComponentPerson) obj).getStringProperty(TCComponentPerson.PROP_PA9);
					err = 316;
					if (pa9.matches(".*?@.*")) {
						email = pa9;
						break;
					}
				}
			}

			if (email == null) {
				MessageBox.post(
						"Selected CL has no email information, please inform him/her manually about your assignment!",
						"INFO", MessageBox.INFORMATION);
				return;
			}

			// 3. get list of reassigned items/revision
			String string_list_items = "";
			int tgLength = targetComp.length;

			for (int i = 0; i < tgLength; i++) {

				string_list_items += "<tr><th>" + (i + 1) + "</th>" + "<th><p align=\"left\">"
						+ ((TCComponent) targetComp[i]).toStringLabel() + "</p></th></tr>\r\n";
			}

			// 4. compose email
			String subject = "[TC-SourceTracker]New %_item_s% object(s) assigned to you".replace("%_item_s%",
					"" + tgLength);
			String contentTCMail = "<html>" + "<body>" + "Dear User,<br>"
					+ "${currentUser} has assigned ${_item_count} object(s) to you:<br>"
					+ "<table width=\"100%\" border=\"1\">" + "<tr bgcolor=\"#80ced6\">" + "<th>S.No</th>"
					+ "<th>VF Part</th>" + "</tr>" + "${listObject}" + "</table><br>" + "<br>"
					+ "Note: This is an automatically generated email. Please do not reply, to this email. If you have any questions please email IT Service Desk (VinFast) itservicedesk@vinfast.vn<br>"
					+ "</body>" + "</html>";

			contentTCMail = contentTCMail.replace("${currentUser}", currentUser)
					.replace("${_item_count}", "" + tgLength).replace("${listObject}", string_list_items);

			// 5. send

			// info("To:"+ email+"\nContent:\n"+string_list_items); // DEBUG!!
			sendMailWiVinfastService(session, new String[] { email }, subject, contentTCMail);
			err = 362;

		} catch (Exception e) {
			MessageBox.post("An error sending email to assigned CL, error code #" + err, "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	/**
	 * simple MIME composer
	 * 
	 * @param isession
	 * @param toExtMail
	 * @param subject
	 * @param content
	 */
	private static void sendMailWiVinfastService(TCSession isession, String toExtMail[], String subject,
			String content) {

		String mailTemplate = "From: ${from}\r\n" + "To: ${to}\r\n" + "Subject: ${subject}\r\n"
				+ "Mime-Version: 1.0;\r\n" + "Content-Type: text/html; charset=\"ISO-8859-1\"\r\n"
				+ "Content-Transfer-Encoding: 7bit;\r\n" + "\r\n" + "${body}\r\n";

		String toList = toExtMail[0];
		for (int i = 1; i < toExtMail.length; i++) {
			toList = "," + toExtMail[i];
		}

		mailTemplate = mailTemplate.replace("${from}", "Datamanager@vinfast.vn").replace("${to}", toList)
				.replace("${subject}", subject).replace("${body}", content);

		try {

			MailSenderService mailSrv = MailSenderService.getService(isession);
			mailSrv.sendEmail("Datamanager@vinfast.vn", toList, "(unused)cc", "(unused)subject", mailTemplate,
					"(unused)attach");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void transferOwnership(InterfaceAIFComponent[] targetComp) {

		String selected_user = selected_CL.substring(selected_CL.indexOf("(") + 1, selected_CL.indexOf(")"));
		try {

			TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
			TCComponentUser user = userType.find(selected_user.trim());
			for (int i = 0; i < targetComp.length; i++) {
				TCComponentItemRevision rev = (TCComponentItemRevision) targetComp[i];
				rev.setProperty("vf4_cl", selected_CL);
				TCComponentItem item = rev.getItem();
				TCComponent rev_master = rev.getRelatedComponent("IMAN_master_form_rev");
				TCComponent item_master = item.getRelatedComponent("IMAN_master_form");

				item_master.changeOwner(user, group);
				item_master.refresh();
				rev_master.changeOwner(user, group);
				rev_master.refresh();
				item.changeOwner(user, group);
				item.refresh();
				rev.changeOwner(user, group);
				rev.refresh();

			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TCComponentFolder searchSourceFolder(String folder_name, String CL_user) {
		TCComponentFolder folder = null;
		try {

			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { "General..." };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[] { "Name", "Type", "Owning User" };
			qc.values = new String[] { folder_name, "Folder", CL_user };
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {

				TCComponent[] tcComp = results[0].objects;
				folder = (TCComponentFolder) tcComp[0];
			} else {
				System.out.println("NO Object FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return folder;
	}

}
