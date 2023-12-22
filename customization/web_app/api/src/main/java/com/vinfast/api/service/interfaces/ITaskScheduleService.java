package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormJobHistoryList;
import com.vinfast.api.model.dataForm.DataFormJobMasterList;
import com.vinfast.api.model.dataForm.DataFormTaskScheduleList;
import com.vinfast.api.model.db.*;
import org.springframework.stereotype.Service;

@Service
public interface ITaskScheduleService {
    DataFormJobMasterList jobMasterList_GetDataForm();

    BasePagingModel<JobMasterModel> jobMaster_GetAll(JobMasterPagingModel paramJobMasterPagingModel);

    JobMasterModel jobMaster_GetByID(String paramString);

    BaseModel jobMaster_Create(JobMasterModel paramJobMasterModel);

    BaseModel jobMaster_Update(JobMasterModel paramJobMasterModel);

    BaseModel jobMaster_SyncTask();

    BasePagingModel<JobCategoryModel> jobCategory_GetAll(JobCategoryPagingModel paramJobCategoryPagingModel);

    JobCategoryModel jobCategory_GetByID(long paramLong);

    BaseModel jobCategory_Create(JobCategoryModel paramJobCategoryModel);

    BaseModel jobCategory_Update(JobCategoryModel paramJobCategoryModel);

    DataFormJobHistoryList jobHistoryList_GetDataForm();

    BasePagingModel<JobHistoryModel> jobHistory_GetAll(JobHistoryPagingModel paramJobHistoryPagingModel);

    JobHistoryModel jobHistory_GetByID(long paramLong);

    BaseModel jobHistory_Create(JobHistoryModel paramJobHistoryModel);

    DataFormTaskScheduleList taskScheduleList_GetDataForm();

    BasePagingModel<TaskScheduleModel> taskSchedule_GetAll(TaskSchedulePagingModel paramTaskSchedulePagingModel);

    BaseModel taskSchedule_Run(String paramString);

    BaseListModel<MissingFileInAWSModel> missingFileInAWS_GetAll(MissingFileInAWSListModel paramMissingFileInAWSListModel);
}
