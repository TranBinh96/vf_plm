package com.vf.utils;

import java.util.Arrays;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class GrandWriterAccessService {
	private TCSession session;
	private TCComponent[] selectedObjects;
	private TCComponentTask task;

	public GrandWriterAccessService(TCComponent[] selectedObjects, TCSession session) {
		this.selectedObjects = selectedObjects;
		this.session = session;

	}

	public boolean triggerProcess(String processName) {
		try {
			TCComponentProcessType processType = (TCComponentProcessType) session.getTypeComponent("Job");
			TCComponentTaskTemplateType templateType = (TCComponentTaskTemplateType) session.getTypeComponent(TCComponentTaskTemplateType.EPM_TASKTEMPLATE_TYPE);
			TCComponentTaskTemplate template = templateType.find("Grant_Write_Access", 0);
			if (template != null) {
				int[] workProcessAttachmentType = new int[selectedObjects.length];
				Arrays.fill(workProcessAttachmentType, 1);
				TCComponentProcess process = (TCComponentProcess) processType.create(processName, "Grant_Write_Access", template, selectedObjects, workProcessAttachmentType);
				if (process != null) {
					task = process.getRootTask().getSubtask("Grant_Write_Access");
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void completeTask() {
		if (task != null) {
			try {
				task.setProperty("task_result", "Completed");
				task.performAction(TCComponentTask.COMPLETE_ACTION, TCComponentTask.PROP_STATE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
