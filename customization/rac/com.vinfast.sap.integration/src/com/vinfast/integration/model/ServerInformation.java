package com.vinfast.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public class ServerInformation extends ModelAbstract{
	public ServerInformation() {
		super(ModelType.SERVER_INFORMATION);
	}
	
	public ServerInformation(String serverType, String ip) {
		super(ModelType.SERVER_INFORMATION);
		this.serverType = serverType;
		this.ip = ip;
	}
	
	String ip;
	String serverType;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
}
