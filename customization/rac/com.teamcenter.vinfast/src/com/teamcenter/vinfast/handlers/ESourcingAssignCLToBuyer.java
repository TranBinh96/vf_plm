package com.teamcenter.vinfast.handlers;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentRoleType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.vf.dialog.FrameWithOneComboBox;
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ESourcingAssignCLToBuyer extends AbstractHandler {
	/**
	 * The constructor.
	 */
	FrameWithOneComboBox frame = null;
	TCSession session = null;
	TCComponentGroup group = null;
	TCComponentRole role = null;
	TCComponentUser user = null;
	boolean isvalidObject = true;
	String selectedBuyer = null;
	InterfaceAIFComponent[] targetComp = null;
	private final Logger LOGGER = Logger.getLogger(this.getClass());
	
	public ESourcingAssignCLToBuyer() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

				session = (TCSession)AIFUtility.getCurrentApplication().getSession();
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				
				if ((group.toString().equals("Purchase") && role.toString().equals("CL") || group.toString().equals("Purchase.Scooter") && role.toString().equals("CL")
						|| group.toString().equals("dba") && role.toString().equals("DBA")) == false) {
					MessageBox.post("CL role users are authorized to perform this operation. Please contact Administrator.", "Not Authorized", MessageBox.ERROR);
					return;
				} else {
					isvalidObject = validateObjects(targetComp);
					if (isvalidObject == false) {
						MessageBox.post("Operation cannot be performed on Checked-Out objects. \nPlease check-in or ignore checked-out objects and retry.", "Error", MessageBox.ERROR);
						return;
					}
					createDialog();
				}
			}
		});
		return null;
	}
	
	public String[] getCarBuyerList(){

		String[] buyer_users = {};
		try {

			TCComponentGroupType groupType =  (TCComponentGroupType)session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType)session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
			TCComponentGroup group = groupType.find("Purchase");

			TCComponentRole role = roleType.find("Buyer");
			TCComponentGroupMember[] buyer_groupMem = GMType.findByRole(role, group);

			buyer_users =  new String[buyer_groupMem.length] ;
			for(int i=0;i<buyer_groupMem.length;i++){

				buyer_users[i]= buyer_groupMem[i].getUser().toString();
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buyer_users;

	}
	
	public String[] getEscooterBuyerList(){

		String[] buyer_users = {};
		try {

			TCComponentGroupType groupType =  (TCComponentGroupType)session.getTypeComponent("Group");
			TCComponentRoleType roleType = (TCComponentRoleType)session.getTypeComponent("Role");
			TCComponentGroupMemberType GMType = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
			TCComponentGroup group = groupType.find("Purchase.Scooter");

			TCComponentRole role = roleType.find("Buyer");
			TCComponentGroupMember[] buyer_groupMem = GMType.findByRole(role, group);

			buyer_users =  new String[buyer_groupMem.length] ;
			for(int i=0;i<buyer_groupMem.length;i++){

				buyer_users[i]= buyer_groupMem[i].getUser().toString();
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buyer_users;

	}
	
	public boolean validateObjects(InterfaceAIFComponent[] targetComp){

		for(int i=0; i<targetComp.length;i++){

			TCComponent object = (TCComponent)targetComp[i];

			if(object.isCheckedOut() == true){
				return false;
			}
		}

		return true;
	}

	public void createDialog(){

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource(
				"/icons/KIT.png"));
		Icon save_Icon = new ImageIcon(getClass().getResource(
				"/icons/save_16.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource(
				"/icons/cancel_16.png"));
		
		frame = new FrameWithOneComboBox();
		frame.setTitle("Assign to Buyer Dialog");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(500, 150));

		frame.label1.setText("Choose Buyer:");
		frame.btnLeft.setIcon(save_Icon);
		frame.btnLeft.setText("Assign");
		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");
		
		if(group.toString().equals("Purchase.Scooter")) {
			for(String tu: getEscooterBuyerList()) {
				frame.comboBox1.addItem(tu);;
			}
		}else {
			for(String tu: getCarBuyerList()) {
				frame.comboBox1.addItem(tu);;
			}
		}
		
		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				selectedBuyer = frame.comboBox1.getSelectedItem().toString();
				if(selectedBuyer == null){
					
					MessageBox.post("Please select Buyer.", "Error", MessageBox.ERROR);
					return;
				}
				saveOperation(targetComp);
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

	
	public void saveOperation(InterfaceAIFComponent[] targetComp){

		transferOwnership(targetComp);
		
	}
	
	public void transferOwnership(InterfaceAIFComponent[] targetComp) {
		try {
			String selectedUserID = selectedBuyer.substring(selectedBuyer.indexOf("(") + 1, selectedBuyer.indexOf(")"));
			for (int i = 0; i < targetComp.length; i++) {
				TCAccessControlService acl = session.getTCAccessControlService();
				TCComponentItemRevision revision = (TCComponentItemRevision) targetComp[i];
				String vf4_bom_vfParNumber = revision.getProperty("vf4_bom_vfPartNumber");
				String vf4_platform_module = revision.getProperty("vf4_platform_module");
				if (selectedUserID != null) {
					String vf4_buyerValue = revision.getProperty("vf4_buyer");
					TCComponent targetBuyerComp = acl.findAccessor("User", selectedUserID);
					if (targetBuyerComp == null) {
						MessageBox.post("[transferOwnership] Invalid User: " + selectedUserID, "Error", MessageBox.ERROR);
						break;
					}
					String currentBuyerID = "";
					if (!vf4_buyerValue.equals("")) {
						if (vf4_buyerValue.contains("(") && vf4_buyerValue.contains(")")) {
							currentBuyerID = vf4_buyerValue.substring(vf4_buyerValue.indexOf("(") + 1, vf4_buyerValue.indexOf(")"));
						} else {
							MessageBox.post("[transferOwnership] Wrong format vf4_buyer: " + vf4_bom_vfParNumber + "|" +vf4_buyerValue + "|" + vf4_platform_module, "Error", MessageBox.ERROR);
							LOGGER.error("[transferOwnership] Wrong format vf4_buyer: " + vf4_bom_vfParNumber + "|" + vf4_buyerValue + "|" + vf4_platform_module);
						}
						TCComponent currBuyerComp = null;
						try {
							currBuyerComp = acl.findAccessor("User", currentBuyerID);
						} catch (Exception e) {
							MessageBox.post("[transferOwnership] Invalid User: " + vf4_bom_vfParNumber + "|" +currentBuyerID + "|" + vf4_platform_module, "Error", MessageBox.ERROR);
							LOGGER.error("[transferOwnership] Invalid User: " + vf4_bom_vfParNumber + "|" + currentBuyerID + "|" + vf4_platform_module);
						}
						if (currBuyerComp != null) {
							boolean isGranted = acl.checkUsersPrivilege((TCComponentUser) currBuyerComp, revision, "WRITE");
							if (isGranted == true) {
								acl.removeAccessor(revision, currBuyerComp);
							} else {
								LOGGER.error("[transferOwnership] Buyer and Privilege not matching: " + vf4_bom_vfParNumber + "|" + currBuyerComp + "|" + vf4_platform_module);
							}
						}
					} 
					acl.grantPrivilege(revision, targetBuyerComp, new String[]{"READ","WRITE"});
					revision.setProperty("vf4_buyer", selectedBuyer);
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			MessageBox.post(e.toString(), "Error", MessageBox.ERROR);
		}
	}
	
	public TCComponentFolder searchSourceFolder(String folder_name , String CL_user)
	{
		TCComponentFolder folder = null ;
		try
		{

			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
			String name[]={"General..."};
			String desc[]={""};
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0]=qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[]result=responce1.savedQueries;

			SavedQueryInput qc=new SavedQueryInput();
			SavedQueryInput qc_v[]=new SavedQueryInput[1];

			qc.query=(TCComponentQuery) result[0];
			qc.entries= new String[] {"Name","Type","Owning User"};
			qc.values=new String[] {folder_name,"Folder",CL_user};
			qc_v[0]=qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results=responce.arrayOfResults;

			if(results[0].numOfObjects!=0){

				//isPartSourceable =  false ;
				TCComponent[] tcComp = results[0].objects ;
				folder = (TCComponentFolder)tcComp[0] ;
			}
			else
			{
				System.out.println("NO Object FOUND");
			}
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return folder ;
	}
	
	
}


