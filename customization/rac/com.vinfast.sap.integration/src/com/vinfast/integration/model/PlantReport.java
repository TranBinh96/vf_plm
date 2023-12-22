package com.vinfast.integration.model;

public class PlantReport extends ReportMessage{

	public PlantReport() {
		this.data = new String[6];
		header = new String[] {"S.No","Plant ID/Rev","Message","Station","Server","Result"};
	}
	

	private final static int NO_POS = 0;
	private final static int PLANTID_POS = 1;
	private final static int MESSAGE_POS = 2;
	private final static int STATION_POS = 3;
	private final static int SERVER_POS = 4;
	private final static int RESULT_POS = 5;
	
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getPlantID() {
		return plantID;
	}
	public void setPlantID(String plantID) {
		this.plantID = plantID;
	}
	@Override
	public String getMessage() {
		return Message;
	}
	@Override
	public void setMessage(String message) {
		Message = message;
	}
	public String getStation() {
		return Station;
	}
	public void setStation(String station) {
		Station = station;
	}
	public static String getServer() {
		return Server;
	}
	public static void setServer(String server) {
		Server = server;
	}
	public String getResult() {
		return Result;
	}
	public void setResult(String result) {
		Result = result;
	}
	
	@Override
	protected void parseReportToArray() {
		this.data[NO_POS] = no;
		this.data[PLANTID_POS] = plantID;
		this.data[MESSAGE_POS] = Message;
		this.data[STATION_POS] = Station;
		this.data[SERVER_POS] = Server;
		this.data[RESULT_POS] = Result;
	}
	
	private String no = "--";
	private String plantID = "--";
	private String Message = "--";
	private String Station = "--";
	private static String Server = "--";
	private String Result = "--";
	

}
