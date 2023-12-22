package com.teamcenter.vinfast.admin.ras;

public class ReAssignTask_Constant {
	public static final String STATE_COMPLETED = "COMPLETED";
	public static final String STATE_STARTED = "STARTED";
	public static final String STATE_PENDING = "PENDING";
	public static final String STATE_PROMOTED = "PROMOTED";

	public static final int TASKSTATE_COMPLETED = 8;
	public static final int TASKSTATE_STARTED = 4;
	public static final int TASKSTATE_PENDING = 2;
	public static final int TASKSTATE_PROMOTED = 16;
	public static final int TASKSTATE_ABORTED = 32;

	public static final String TASKTYPE_TASK = "EPMTask";
	public static final String TASKTYPE_REVIEW = "EPMReviewTask";
	public static final String TASKTYPE_CONDITION = "EPMConditionTask";
	public static final String TASKTYPE_ACKNOWLEDGE = "EPMAcknowledgeTask";
	public static final String TASKTYPE_DO = "EPMDoTask";
	public static final String TASKTYPE_ROUTE = "EPMRouteTask";
	public static final String TASKTYPE_PERFORMSIGNOFF = "EPMPerformSignoffTask";
	public static final String TASKTYPE_SELECTSIGNOFF = "EPMSelectSignoffTask";

	public static final int ASSIGN_ACTION = 1;
	public static final int START_ACTION = 2;
	public static final int FAIL_ACTION = 10;
	public static final int COMPLETE_ACTION = 4;
	public static final int SKIP_ACTION = 5;
	public static final int SUSPEND_ACTION = 6;
	public static final int RESUME_ACTION = 7;
	public static final int UNDO_ACTION = 8;
	public static final int ABORT_ACTION = 9;
	public static final int PERFORM_ACTION = 100;
	public static final int ADD_ATTACHMENT_ACTION = 101;
	public static final int REMOVE_ATTACHMENT_ACTION = 1102;
	public static final int APPROVE_ACTION = 104;
	public static final int REJECT_ACTION = 105;
	public static final int PROMOTE_ACTION = 106;
	public static final int DEMOTE_ACTION = 107;
	public static final int REFUSE_ACTION = 108;
	public static final int ASSIGN_APPROVER_ACTION = 109;
	public static final int USER_ACTION = 1000;
}
