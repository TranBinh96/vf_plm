package com.teamcenter.vinfast.escooter.sorprocess.model;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class ResourcesModel {
	String[] performer = null;
	TCComponentGroupMember[] groupMember = null;
	TCComponentUser[] user = null;
	
	public ResourcesModel(TCComponent rawModel) throws TCException {
		TCComponent[] res = rawModel.getRelatedComponents("resources");
		if(res != null && res.length > 0) {
			this.performer = new String[res.length];
			this.groupMember = new TCComponentGroupMember[res.length];
			this.user = new TCComponentUser[res.length];
			
			for(int i = 0; i < res.length; i++) {
				this.groupMember[i] = ((TCComponentGroupMember) res[i]);
				this.user[i] = groupMember[i].getUser();
				this.performer[i] = this.user[i].toString();
			}
		}
	}

	public String[] getPerformer() {
		return performer;
	}

	public void setPerformer(String[] performer) {
		this.performer = performer;
	}
	
	public void setPerformerAt(int index, String performer) {
		this.performer[index] = performer;
	}

	public TCComponentGroupMember[] getGroupMember() {
		return groupMember;
	}

	public void setGroupMember(TCComponentGroupMember[] groupMember) {
		this.groupMember = groupMember;
	}
	
	public void setGroupMemberAt(int index, TCComponentGroupMember groupMember) {
		this.groupMember[index] = groupMember;
	}

	public TCComponentUser[] getUser() {
		return user;
	}

	public void setUser(TCComponentUser[] user) {
		this.user = user;
	}
	
	public void setUserAt(int index, TCComponentUser user) {
		this.user[index] = user;
	}
	
}
