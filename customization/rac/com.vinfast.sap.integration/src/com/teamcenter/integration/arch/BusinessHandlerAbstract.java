package com.teamcenter.integration.arch;

public abstract class BusinessHandlerAbstract extends ProcessorAbstract{
	
	public BusinessHandlerAbstract(String processName) {
		super(processName, 2000);
	}
}
