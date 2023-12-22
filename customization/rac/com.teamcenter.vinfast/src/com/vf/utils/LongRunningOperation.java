package com.vf.utils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class LongRunningOperation implements IRunnableWithProgress {
	private static final int TOTAL_TIME = 10000;

	private static final int INCREMENT = 500;

	private boolean indeterminate;
	private String beginTaskName = "Processing operation...";

	public LongRunningOperation(boolean indeterminate, String beginTaskName) {
		this.indeterminate = indeterminate;
		this.beginTaskName = beginTaskName;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(beginTaskName, indeterminate ? IProgressMonitor.UNKNOWN : TOTAL_TIME);
//		for (int total = 0; total < TOTAL_TIME && !monitor.isCanceled(); total += INCREMENT) {
//			Thread.sleep(INCREMENT);
//			monitor.worked(INCREMENT);
//			monitor.subTask(total + "/" + TOTAL_TIME);
//		}
		monitor.done();
		if (monitor.isCanceled())
			throw new InterruptedException("The long running operation was cancelled");
	}
}
