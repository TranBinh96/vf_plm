package com.teamcenter.vinfast.model;

import java.util.LinkedHashMap;
import java.util.List;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class OwnerTransferModel {
	private TCSession session;
	private String MBOMPartID;
	private String UserID;
	private String GroupID;

	private TCComponentItemRevision MBOMRev;
	private TCComponentItemRevision BOPRev;
	private TCComponentItemRevision PlantModelRev;
	private TCComponentGroupMember Reviewer;

	public OwnerTransferModel(TCSession _session) {
		Reviewer = null;
		session = _session;
	}

	public void setPartID(String value) {
		this.MBOMPartID = value;
	}

	public void setUserID(String value) {
		this.UserID = value;
	}

	public void setGroupID(String value) {
		this.GroupID = value;
	}

	public void CheckReviewer() {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Id", UserID);
		TCComponent[] item_search = Query.queryItem(session, inputQuery, "Admin - User Memberships");

		try {
			if (item_search != null && item_search.length > 0) {
				for (TCComponent tcComponent : item_search) {
					TCComponentGroupMember groupMember = (TCComponentGroupMember) tcComponent;
					if (groupMember.getGroup().toString().compareToIgnoreCase(GroupID) == 0) {
						Reviewer = groupMember;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public void CheckReferenced() {
		try {
			// Get MBOM Rev
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", this.MBOMPartID);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Item...");

			if (item_search != null) {
				if (item_search.length > 0) {
					TCComponentItem item = (TCComponentItem) item_search[0];
					this.MBOMRev = item.getLatestItemRevision();
				}
			}

			// Get BOP Rev
			if (this.MBOMRev != null) {
				List<TCComponent> refList = TCExtension.getReferenced(this.MBOMRev, "", session, "");
				TCComponent bop = null;
				for (TCComponent tcComponent : refList) {
					if (tcComponent.getType().compareToIgnoreCase("Mfg0MEPlantBOPRevision") == 0) {
						BOPRev = (TCComponentItemRevision) tcComponent;
					}
				}
			}
			if (this.BOPRev != null) {
				TCComponent[] refList = this.BOPRev.getRelatedComponents("IMAN_MEWorkArea");
				if (refList != null && refList.length > 0) {
					PlantModelRev = (TCComponentItemRevision) refList[0];
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			// TODO: handle exception
		}
	}

	public TCComponentGroupMember GetReviewer() {
		return Reviewer;
	}

	public TCComponentItemRevision GetMBOMTop() {
		return MBOMRev;
	}

	public TCComponentBOMLine GetBOPTop() {
		return GetTopBom(this.BOPRev);
	}

	public TCComponentBOMLine GetPlantModelTop() {
		return GetTopBom(this.PlantModelRev);
	}

	private TCComponentBOMLine GetTopBom(TCComponentItemRevision itemRevision) {
		OpenContextInfo[] createdBOMViews = TCExtension.CreateContextViews(itemRevision, session);
		if (createdBOMViews != null) {
			if (createdBOMViews.length > 0) {
				for (int i = 0; i < createdBOMViews.length; i++) {
					if (createdBOMViews[i].context.getType().equals("BOMLine")) {
						return (TCComponentBOMLine) createdBOMViews[0].context;
					}
				}
			}
		}
		return null;
	}
}
