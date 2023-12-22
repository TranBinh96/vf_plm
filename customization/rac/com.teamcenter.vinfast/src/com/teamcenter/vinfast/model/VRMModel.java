package com.teamcenter.vinfast.model;

import java.util.LinkedList;

public class VRMModel {
	public class CommentModel {
		public String content = "";
		public String authorName = "";
		public String authorEmail = "";
		public String createDate = "";
	}

	public class AttachmentModel {
		public String fileName = "";
		public String path = "";
	}

	public LinkedList<AttachmentModel> attachments = new LinkedList<AttachmentModel>();
	public LinkedList<CommentModel> comments = new LinkedList<CommentModel>();

	public void addAttachment(String fileName, String path) {
		AttachmentModel newAttachment = new AttachmentModel();
		newAttachment.fileName = fileName;
		newAttachment.path = path;
		attachments.add(newAttachment);
	}

	public void addComment(String content, String authorName, String authorEmail, String createDate) {
		CommentModel newComment = new CommentModel();
		newComment.content = content;
		newComment.authorName = authorName;
		newComment.authorEmail = authorEmail;
		newComment.createDate = createDate;
		comments.add(newComment);
	}
}
