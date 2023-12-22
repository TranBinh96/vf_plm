package com.vinfast.integration.model;

public class SuperScooterReport extends ReportMessage{
	public SuperScooterReport() {
		this.data = new String[7];
		header = new String[] {"S.No","Sub Group","Record","BOMLine ID","Message","Action","Result"};
	}
	
	public SuperScooterReport(String no, String subGroup, String record, String bomlineId, String message, String action, String result) {
		this.data = new String[7];
		header = new String[] {"S.No","Sub Group","Record","BOMLine ID","Message","Action","Result"};
		this.no = no;
		this.subGroup = subGroup;
		this.record = record;
		this.bomlineId = bomlineId;
		this.message = message;
		this.action = action;
		this.result = result;
	}
	

	private final static int NO_POS = 0;
	private final static int SUBGROUP_POS = 1;
	private final static int RECORD_POS = 2;
	private final static int BOMLINEID_POS = 3;
	private final static int MESSAGE_POS = 4;
	private final static int ACTION_POS = 5;
	private final static int RESULT_POS = 6;
	
	@Override
	protected void parseReportToArray() {
		this.data[NO_POS] = no;
		this.data[SUBGROUP_POS] = subGroup;
		this.data[RECORD_POS] = record;
		this.data[BOMLINEID_POS] = bomlineId;
		this.data[MESSAGE_POS] = message;
		this.data[ACTION_POS] = action;
		this.data[RESULT_POS] = result;
	}
	
	
	
	private String no = "--";
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getSubGroup() {
		return subGroup;
	}
	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	public String getBomlineId() {
		return bomlineId;
	}
	public void setBomlineId(String bomlineId) {
		this.bomlineId = bomlineId;
	}
	@Override
	public String getMessage() {
		return message;
	}
	@Override
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public static int getNoPos() {
		return NO_POS;
	}
	public static int getSubgroupPos() {
		return SUBGROUP_POS;
	}
	public static int getRecordPos() {
		return RECORD_POS;
	}
	public static int getBomlineidPos() {
		return BOMLINEID_POS;
	}
	public static int getMessagePos() {
		return MESSAGE_POS;
	}
	public static int getActionPos() {
		return ACTION_POS;
	}
	public static int getResultPos() {
		return RESULT_POS;
	}


	private String subGroup = "--";
	private String record = "--";
	private String bomlineId = "--";
	private String message = "--";
	private String action = "--";
	private String result = "--";
}
