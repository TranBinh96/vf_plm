package com.teamcenter.integration.arch;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class ProcessorAbstract extends Thread {

	public ProcessorAbstract(String processName, int interval) {
		this.processName = processName;
		this.interval = interval;
		ProcessorManager.getInstance().registerProcessor(processName, this);
		mMessageQueue = new ArrayBlockingQueue<>(100);
	}

	private BlockingQueue<ModelAbstract> mMessageQueue = null;
	private String processName;

	private boolean isRunning = false;
	private int interval = 1000;

	public void pushMessage(ModelAbstract msg) {
		if (msg != null) {
			try {
				mMessageQueue.put(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public String getProcessName() {
		return processName;
	}

	public void publish(String targetProcessor, ModelAbstract msg) {
		ProcessorManager.getInstance().sendMessage(targetProcessor, msg);
	}

	public void startProcessor() {
		isRunning = true;
		this.start();
	}

	public void stopProcessor() {
		isRunning = false;
	}

	@Override
	public void run() {
		while (isRunning) {
			while (mMessageQueue.size() > 0) {
				try {
					ModelAbstract msg = mMessageQueue.take();
					onMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			onBusiness();
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract void onMessage(ModelAbstract msg);

	protected void onBusiness() {
	}
}
