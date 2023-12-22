package com.vinfast.api.controller;

import com.vinfast.api.common.extensions.StringExtension;
import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormJobHistoryList;
import com.vinfast.api.model.dataForm.DataFormJobMasterList;
import com.vinfast.api.model.dataForm.DataFormTaskScheduleList;
import com.vinfast.api.model.db.*;
import com.vinfast.api.service.interfaces.ITaskScheduleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Timestamp;
@RestController
@RequestMapping({"/taskSchedule"})
public class TaskScheduleController {
    private static final Logger LOGGER = LogManager.getLogger(TaskScheduleController.class);
    @Autowired
    private ITaskScheduleService taskScheduleService;

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobMaster_GetAll"})
    public BasePagingModel<JobMasterModel> jobMaster_GetAll(@RequestParam String JobName,
                                                            @RequestParam String JobCategoryID,
                                                            @RequestParam(defaultValue = "1") Integer PageIndex,
                                                            @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                            @RequestParam String SortColumn,
                                                            @RequestParam(defaultValue = "desc") String SortColumnDir) {
        JobMasterPagingModel listModel = new JobMasterPagingModel();
        if (JobName.compareTo("null") == 0) JobName = "";
        listModel.setJobName(JobName);
        try {
            listModel.setJobCategoryID(Integer.valueOf(Integer.parseInt(JobCategoryID)));
        } catch (Exception ignored) {
        }


        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);

        return this.taskScheduleService.jobMaster_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobMasterList_GetDataForm"})
    public DataFormJobMasterList jobMasterList_GetDataForm() {
        return this.taskScheduleService.jobMasterList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobMaster_GetByID"})
    public JobMasterModel jobMaster_GetByID(String uid) {
        return this.taskScheduleService.jobMaster_GetByID(uid);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/jobMaster_Create"})
    public BaseModel jobMaster_Create(@RequestBody JobMasterModel model) {
        return this.taskScheduleService.jobMaster_Create(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/jobMaster_Update"})
    public BaseModel jobMaster_Update(@RequestBody JobMasterModel model) {
        if (model.getUid().isEmpty()) {
            model.setUid(StringExtension.getRandomString(6));
            return this.taskScheduleService.jobMaster_Create(model);
        }
        return this.taskScheduleService.jobMaster_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/jobMaster_SyncTask"})
    public BaseModel jobMaster_SyncTask() {
        return this.taskScheduleService.jobMaster_SyncTask();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobCategory_GetAll"})
    public BasePagingModel<JobCategoryModel> jobCategory_GetAll(@RequestParam String JobCategoryName,
                                                                @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                @RequestParam String SortColumn,
                                                                @RequestParam(defaultValue = "desc") String SortColumnDir) {
        JobCategoryPagingModel listModel = new JobCategoryPagingModel();
        if (JobCategoryName.compareTo("null") == 0) JobCategoryName = "";
        listModel.setJobCategoryName(JobCategoryName);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.taskScheduleService.jobCategory_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobCategory_GetByID"})
    public JobCategoryModel jobCategory_GetByID(int id) {
        return this.taskScheduleService.jobCategory_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/jobCategory_Update"})
    public BaseModel jobCategory_Update(@RequestBody JobCategoryModel model) {
        if (model.getId() != null) {
            return this.taskScheduleService.jobCategory_Update(model);
        }
        return this.taskScheduleService.jobCategory_Create(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobHistoryList_GetDataForm"})
    public DataFormJobHistoryList jobHistoryList_GetDataForm() {
        return this.taskScheduleService.jobHistoryList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobHistory_GetAll"})
    public BasePagingModel<JobHistoryModel> jobHistory_GetAll(@RequestParam String JobName,
                                                              @RequestParam String JobCategoryID,
                                                              @RequestParam String Status,
                                                              @RequestParam String StartFromDate,
                                                              @RequestParam String StartToDate,
                                                              @RequestParam(defaultValue = "1") Integer PageIndex,
                                                              @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                              @RequestParam String SortColumn,
                                                              @RequestParam(defaultValue = "desc") String SortColumnDir) {
        JobHistoryPagingModel listModel = new JobHistoryPagingModel();
        if (JobName.compareTo("null") == 0) JobName = "";
        listModel.setJobName(JobName);
        if (Status.compareTo("null") == 0) Status = "";
        listModel.setStatus(Status);
        try {
            listModel.setJobCategoryID(Integer.valueOf(Integer.parseInt(JobCategoryID)));
        } catch (Exception ignored) {
        }

        try {
            Timestamp ts = Timestamp.valueOf(StartFromDate);
            listModel.setStartFromDate(ts);
        } catch (Exception ignored) {
        }

        try {
            Timestamp ts = Timestamp.valueOf(StartToDate);
            listModel.setStartToDate(ts);
        } catch (Exception ignored) {
        }
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.taskScheduleService.jobHistory_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/jobHistory_GetByID"})
    public JobHistoryModel jobHistory_GetByID(int id) {
        return this.taskScheduleService.jobHistory_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/jobHistory_Create"})
    public BaseModel jobHistory_Create(@RequestBody JobHistoryModel model) {
        return this.taskScheduleService.jobHistory_Create(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/taskScheduleList_GetDataForm"})
    public DataFormTaskScheduleList taskScheduleList_GetDataForm() {
        return this.taskScheduleService.taskScheduleList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/taskSchedule_GetAll"})
    public BasePagingModel<TaskScheduleModel> taskSchedule_GetAll(@RequestParam String Status, @RequestParam String TaskName, @RequestParam(defaultValue = "1") Integer PageIndex, @RequestParam(defaultValue = "0") Integer ItemsPerPage, @RequestParam String SortColumn, @RequestParam(defaultValue = "desc") String SortColumnDir) {
        TaskSchedulePagingModel listModel = new TaskSchedulePagingModel();
        if (Status.compareTo("null") == 0) Status = "";
        listModel.setStatus(Status);
        if (TaskName.compareTo("null") == 0) TaskName = "";
        listModel.setTaskName(TaskName);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);

        return this.taskScheduleService.taskSchedule_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/taskSchedule_Run"})
    public BaseModel taskSchedule_Run(@RequestParam String taskName) {
        return this.taskScheduleService.taskSchedule_Run(taskName);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/missingFileInAWS_GetAll"})
    public BaseListModel<MissingFileInAWSModel> missingFileInAWS_GetAll(@RequestParam String DateRequest) {
        MissingFileInAWSListModel listModel = new MissingFileInAWSListModel();
        try {
            Date ts = Date.valueOf(DateRequest);
            listModel.setDateRequest(ts);
        } catch (Exception ignored) {
        }
        return this.taskScheduleService.missingFileInAWS_GetAll(listModel);
    }
}