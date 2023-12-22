package com.vinfast.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public class NoticeMessage extends ModelAbstract{
	public enum NoticeType{
		INFO,
		ERROR,
		WARNING,
	}
	
	public NoticeMessage(String notice, NoticeType type) {
		super(ModelType.NOTICE_MESSAGE);
		this.notice = notice;
		this.type = type;
	}
	
	public String notice;
	public NoticeType type;

	public NoticeType getType() {
		return type;
	}

	public void setType(NoticeType type) {
		this.type = type;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}
}
