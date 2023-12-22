package com.teamcenter.integration.arch;

import java.util.HashMap;

import com.vinfast.sap.util.PropertyDefines;

public class ProcessorManager {
	private static final ProcessorManager INSTANCE = new ProcessorManager();

	// Private constructor to avoid client applications to use constructor
	private ProcessorManager() {

	}

	public static ProcessorManager getInstance() {
		return INSTANCE;
	}

	public void reset() {
		for (HashMap.Entry<String, ProcessorAbstract> entry : mapManager.entrySet()) {
			entry.getValue().stopProcessor();
		}
		mapManager.clear();
	}

	public void registerProcessor(String name, ProcessorAbstract processor) {
		if (!mapManager.containsKey(name)) {
			mapManager.put(name, processor);
		} else {
			System.out.println(String.format("Processor %s existed", name));
		}

	}

	HashMap<String, ProcessorAbstract> mapManager = new HashMap<String, ProcessorAbstract>();

	void sendMessage(String processorName, ModelAbstract msg) {
		if (processorName.equals(PropertyDefines.UI_STORE)) {
			updateToUiHandler(msg);
		} else {
			if (mapManager.containsKey(processorName) && msg != null) {
				mapManager.get(processorName).pushMessage(msg);
			}
		}

	}

	public void addListener(IntegrationModuleAbstract uiHandler) {
		this.uiHandler = uiHandler;
	}

	private void updateToUiHandler(ModelAbstract event) {
		if (uiHandler != null) {
			uiHandler.onUpdateUiEvent(event);
		}
	}

	public void startAllProcessor() {
		for (HashMap.Entry<String, ProcessorAbstract> entry : mapManager.entrySet()) {
			entry.getValue().startProcessor();
			System.out.println(String.format("Start processor %s ... ", entry.getKey()));
		}
	}

	public void stopAllProcessor() {
		for (HashMap.Entry<String, ProcessorAbstract> entry : mapManager.entrySet()) {
			entry.getValue().stopProcessor();
			System.out.println(String.format("Stop processor %s ... ", entry.getKey()));
		}
	}

	IntegrationModuleAbstract uiHandler = null;
}
