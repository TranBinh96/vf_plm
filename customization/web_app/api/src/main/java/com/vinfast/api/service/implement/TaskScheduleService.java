package com.vinfast.api.service.implement;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.vinfast.api.common.constants.CommonConst;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.common.extensions.DateTimeExtension;
import com.vinfast.api.entity.*;
import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormJobHistoryList;
import com.vinfast.api.model.dataForm.DataFormJobMasterList;
import com.vinfast.api.model.dataForm.DataFormTaskScheduleList;
import com.vinfast.api.model.db.*;
import com.vinfast.api.service.interfaces.ITaskScheduleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
public class TaskScheduleService implements ITaskScheduleService {
    private static final Logger LOGGER = LogManager.getLogger(TaskScheduleService.class);

    @PersistenceContext
    private EntityManager em;

    private static final Qview_tsc_job_history jobHistoryView = Qview_tsc_job_history.view_tsc_job_history;

    private static final Qview_tsc_job_master jobMasterView = Qview_tsc_job_master.view_tsc_job_master;

    private static final Qtsc_job_master jobMasterTable = Qtsc_job_master.tsc_job_master;

    private static final Qtsc_file_master fileMasterTable = Qtsc_file_master.tsc_file_master;

    private static final Qtsc_job_category jobCategoryTable = Qtsc_job_category.tsc_job_category;

    private static final Qtsc_job_history jobHistoryTable = Qtsc_job_history.tsc_job_history;

    public DataFormJobMasterList jobMasterList_GetDataForm() {
        DataFormJobMasterList modelReturn = new DataFormJobMasterList();
        try {
            JPAQuery<tsc_job_category> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobCategoryTable);
            for (tsc_job_category item : jpaQuery.fetch())
                modelReturn.jobCategoryList.add(new SelectItem(item.getId().toString(), item.getJob_category_name()));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<JobMasterModel> jobMaster_GetAll(JobMasterPagingModel listModel) {
        try {
            JPAQuery<view_tsc_job_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobMasterView);
            if (!listModel.getJobName().isEmpty())
                jpaQuery.where(jobMasterView.job_name.contains(listModel.getJobName()));
            if (listModel.getJobCategoryID() != null)
                jpaQuery.where(jobMasterView.job_category_id.eq(listModel.getJobCategoryID()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(JobMasterModel.sortPropertiesView.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(JobMasterModel.sortPropertiesView.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(JobMasterModel.sortPropertiesView.get("uid").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (view_tsc_job_master item : jpaQuery.fetch()) {
                JobMasterModel model = new JobMasterModel();
                model.setRowIndex(++index);
                model.setUid(item.getUid());
                model.setJob_name(item.getJob_name());
                model.setJob_category_id(item.getJob_category_id());
                model.setJob_category_name(item.getJob_category_name());
                model.setTask_name(item.getTask_name());
                model.setStatus(item.getStatus());
                model.setSchedule_type(item.getSchedule_type());
                model.setSchedule_months(item.getSchedule_months());
                model.setSchedule_days(item.getSchedule_days());
                model.setStart_time(item.getStart_time());
                model.setRepeat_every(item.getRepeat_every());
                model.setRemark(item.getRemark());
                model.setCreate_by(item.getCreate_by());
                model.setCreate_date(item.getCreate_date());
                model.setUpdate_by(item.getUpdate_by());
                model.setUpdate_date(item.getUpdate_date());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public JobMasterModel jobMaster_GetByID(String uid) {
        JobMasterModel modelReturn = new JobMasterModel();
        try {
            JPAQuery<tsc_job_master> jpaQuery = new JPAQuery<>(em);
            tsc_job_master item = jpaQuery.from(jobMasterTable).where(jobMasterTable.uid.eq(uid)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            modelReturn.setUid(item.getUid());
            modelReturn.setJob_name(item.getJob_name());
            modelReturn.setJob_category_id(item.getJob_category_id());
            modelReturn.setTask_name(item.getTask_name());

            JPAQuery<tsc_file_master> fileMasterQuery = new JPAQuery<>(em);
            fileMasterQuery.from(fileMasterTable);
            fileMasterQuery.where(fileMasterTable.job_uid.eq(uid));
            fileMasterQuery.orderBy(FileMasterModel.sortProperties.get("id").desc());
            List<FileMasterModel> fileList = new LinkedList<>();
            for (tsc_file_master fileMasterItem : fileMasterQuery.fetch()) {
                FileMasterModel newItem = new FileMasterModel();
                newItem.setId(fileMasterItem.getId());
                newItem.setName(fileMasterItem.getName());
                newItem.setJob_uid(fileMasterItem.getJob_uid());
                newItem.setBytes(fileMasterItem.getBytes());
                newItem.setRows(fileMasterItem.getRows());
                newItem.setRemark(fileMasterItem.getRemark());
                fileList.add(newItem);
            }
            modelReturn.setFileList(fileList);
        } catch (Exception ex) {
            ex.printStackTrace();
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel jobMaster_Create(JobMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            this.em.createNativeQuery("INSERT INTO tsc_job_master (uid, job_name, job_category_id, task_name, remark) VALUES ('" + model.getUid() + "','" + model.getJob_name() + "'," + model.getJob_category_id() + ",'" + model.getTask_name() + "','" + model.getRemark() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel jobMaster_Update(JobMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<tsc_job_master> jpaQuery = new JPAQuery<>(em);
            tsc_job_master item = jpaQuery.from(jobMasterTable).where(jobMasterTable.uid.eq(model.getUid())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(jobMasterTable);
            updateClause.where(jobMasterTable.uid.eq(model.getUid()));
            updateClause.set(jobMasterTable.job_name, model.getJob_name());
            updateClause.set(jobMasterTable.job_category_id, model.getJob_category_id());
            updateClause.set(jobMasterTable.task_name, model.getTask_name());
            updateClause.set(jobMasterTable.remark, model.getRemark());
            long result = updateClause.execute();
            if (result == 0L)
                throw new Exception("Update not success");
            updateFileListOfJobs(model.getUid(), model.getFileList());
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    private void updateFileListOfJobs(String uid, List<FileMasterModel> fileList) {
        List<Long> delList = new LinkedList<>();
        JPAQuery<tsc_file_master> fileMasterQuery = new JPAQuery<>(em);
        for (tsc_file_master fileMasterItem : fileMasterQuery.from(fileMasterTable).where(fileMasterTable.job_uid.eq(uid)).fetch()) {
            boolean check = false;
            for (FileMasterModel fileItem : fileList) {
                if (Objects.equals(fileItem.getId(), fileMasterItem.getId())) {
                    check = true;
                    break;
                }
            }
            if (!check)
                delList.add(fileMasterItem.getId());
        }
        for (FileMasterModel fileItem : fileList) {
            if (fileItem.getId() != null) {
                tsc_file_master item = fileMasterQuery.from(fileMasterTable).where(fileMasterTable.id.eq(fileItem.getId())).fetchOne();
                if (item != null) {
                    JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(fileMasterTable);
                    updateClause.where(fileMasterTable.id.eq(fileItem.getId()));
                    updateClause.set(fileMasterTable.name, fileItem.getName());
                    updateClause.set(fileMasterTable.remark, fileItem.getRemark());
                    updateClause.execute();
                }
                continue;
            }
            this.em.createNativeQuery("INSERT INTO tsc_file_master (name, job_uid, remark) VALUES ('" + fileItem.getName() + "','" + uid + "','" + fileItem.getRemark() + "')")
                    .executeUpdate();
        }
        for (Long delID : delList) {
            tsc_file_master item = fileMasterQuery.from(fileMasterTable).where(fileMasterTable.id.eq(delID)).fetchOne();
            if (item != null) {
                JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(fileMasterTable);
                updateClause.where(fileMasterTable.id.eq(delID));
                updateClause.set(fileMasterTable.job_uid, "");
                updateClause.execute();
            }
        }
    }

    @Transactional
    public BaseModel jobMaster_SyncTask() {
        BaseModel modelReturn = new BaseModel();
        try {
            LinkedList<TaskScheduleModel> taskList = getTaskSchedulerTasks();
            JPAQuery<tsc_job_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobMasterTable);
            for (tsc_job_master item : jpaQuery.fetch()) {
                for (TaskScheduleModel taskItem : taskList) {
                    if (item.getTask_name().compareTo(taskItem.getFullName()) == 0) {
                        JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(jobMasterTable);
                        updateClause.where(jobMasterTable.uid.eq(item.getUid()));
                        updateClause.set(jobMasterTable.status, taskItem.getStatus());
                        updateClause.set(jobMasterTable.start_time, taskItem.getStartTime());
                        updateClause.set(jobMasterTable.schedule_type, taskItem.getScheduleType());
                        updateClause.set(jobMasterTable.schedule_days, taskItem.getDays());
                        updateClause.set(jobMasterTable.schedule_months, taskItem.getMonths());
                        updateClause.set(jobMasterTable.repeat_every, taskItem.getRepeatEvery());
                        long result = updateClause.execute();
                    }
                }
            }
            modelReturn.setMessage("Sync success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<JobCategoryModel> jobCategory_GetAll(JobCategoryPagingModel listModel) {
        try {
            JPAQuery<tsc_job_category> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobCategoryTable);
            if (!listModel.getJobCategoryName().isEmpty())
                jpaQuery.where(jobCategoryTable.job_category_name.contains(listModel.getJobCategoryName()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(JobCategoryModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(JobCategoryModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(JobCategoryModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (tsc_job_category item : jpaQuery.fetch()) {
                JobCategoryModel model = new JobCategoryModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setJob_category_name(item.getJob_category_name());
                model.setRemark(item.getRemark());
                model.setCreate_by(item.getCreate_by());
                model.setCreate_date(item.getCreate_date());
                model.setUpdate_by(item.getUpdate_by());
                model.setUpdate_date(item.getUpdate_date());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public JobCategoryModel jobCategory_GetByID(long id) {
        JobCategoryModel modelReturn = new JobCategoryModel();
        try {
            JPAQuery<tsc_job_category> jpaQuery = new JPAQuery<>(em);
            tsc_job_category item = jpaQuery.from(jobCategoryTable).where(jobCategoryTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setJob_category_name(item.getJob_category_name());
            modelReturn.setRemark(item.getRemark());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel jobCategory_Create(JobCategoryModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO tsc_job_category (job_category_name, remark) VALUES ('" + model.getJob_category_name() + "','" + model.getRemark() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel jobCategory_Update(JobCategoryModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            JPAQuery<tsc_job_category> jpaQuery = new JPAQuery<>(em);
            tsc_job_category item = jpaQuery.from(jobCategoryTable).where(jobCategoryTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(jobCategoryTable);
            updateClause.where(jobCategoryTable.id.eq(model.getId()));
            updateClause.set(jobCategoryTable.job_category_name, model.getJob_category_name());
            updateClause.set(jobCategoryTable.remark, model.getRemark());
            long result = updateClause.execute();
            if (result == 0L)
                throw new Exception("Update not success");
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public DataFormJobHistoryList jobHistoryList_GetDataForm() {
        DataFormJobHistoryList modelReturn = new DataFormJobHistoryList();
        try {
            JPAQuery<tsc_job_category> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobCategoryTable);
            for (tsc_job_category item : jpaQuery.fetch())
                modelReturn.jobCategoryList.add(new SelectItem(item.getId().toString(), item.getJob_category_name()));
            modelReturn.statusList.add(new SelectItem("SUCCESS", "SUCCESS"));
            modelReturn.statusList.add(new SelectItem("ERROR", "ERROR"));
            modelReturn.statusList.add(new SelectItem("WARNING", "WARNING"));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<JobHistoryModel> jobHistory_GetAll(JobHistoryPagingModel listModel) {
        try {
            JPAQuery<view_tsc_job_history> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(jobHistoryView);
            if (!listModel.getJobName().isEmpty())
                jpaQuery.where(jobHistoryView.job_name.contains(listModel.getJobName()));
            if (listModel.getJobCategoryID() != null)
                jpaQuery.where(jobHistoryView.job_category_id.eq(listModel.getJobCategoryID()));
            if (!listModel.getStatus().isEmpty())
                jpaQuery.where(jobHistoryView.status.eq(listModel.getStatus()));
            if (listModel.getStartFromDate() != null)
                jpaQuery.where(jobHistoryView.start_time.after(listModel.getStartFromDate()));
            if (listModel.getStartToDate() != null)
                jpaQuery.where(jobHistoryView.start_time.before(listModel.getStartToDate()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(JobHistoryModel.sortPropertiesView.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(JobHistoryModel.sortPropertiesView.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(JobHistoryModel.sortPropertiesView.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (view_tsc_job_history item : jpaQuery.fetch()) {
                JobHistoryModel model = new JobHistoryModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setJob_name(item.getJob_name());
                model.setJob_category_name(item.getJob_category_name());
                model.setTask_name(item.getTask_name());
                model.setRemark(item.getRemark());
                model.setStatus(item.getStatus());
                if (item.getStatus().compareTo("SUCCESS") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_SUCCESS);
                } else if (item.getStatus().compareTo("ERROR") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_DANGER);
                } else if (item.getStatus().compareTo("WARNING") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_WARNING);
                }
                model.setStart_time(item.getStart_time());
                model.setEnd_time(item.getEnd_time());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public JobHistoryModel jobHistory_GetByID(long id) {
        JobHistoryModel modelReturn = new JobHistoryModel();
        try {
            JPAQuery<tsc_job_history> jpaQuery = new JPAQuery<>(em);
            tsc_job_history item = jpaQuery.from(jobHistoryTable).where(jobHistoryTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setJob_uid(item.getJob_uid());
            modelReturn.setMessage(item.getMessage());
            modelReturn.setRemark(item.getRemark());
            modelReturn.setStart_time(item.getStart_time());
            modelReturn.setEnd_time(item.getEnd_time());
            modelReturn.setStatus(item.getStatus());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel jobHistory_Create(JobHistoryModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO tsc_job_history (job_uid, status, message, remark, start_time, end_time) VALUES ('" +
                            model.getJob_uid() + "','" +
                            model.getStatus() + "','" +
                            model.getMessage() + "','" +
                            model.getRemark() + "','" +
                            model.getStart_time() + "','" +
                            model.getEnd_time() + "')")

                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public DataFormTaskScheduleList taskScheduleList_GetDataForm() {
        DataFormTaskScheduleList modelReturn = new DataFormTaskScheduleList();
        String[] statusList = {"Ready", "Running", "Disabled"};
        try {
            for (String status : statusList)
                modelReturn.statusList.add(new SelectItem(status, status));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<TaskScheduleModel> taskSchedule_GetAll(TaskSchedulePagingModel listModel) {
        try {
            LinkedList<TaskScheduleModel> taskList = getTaskSchedulerTasks();
            listModel.setTotalItems(taskList.size());
            int index = 0;
            for (TaskScheduleModel item : taskList) {
                boolean check = true;
                if (!listModel.getStatus().isEmpty())
                    check = (item.getStatus().compareTo(listModel.getStatus()) == 0);
                if (!listModel.getTaskName().isEmpty())
                    check = item.getTaskName().contains(listModel.getTaskName());
                if (check) {
                    item.setRowIndex(++index);
                    listModel.dataList.add(item);
                }
            }
        } catch (Exception ex) {
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    @PostMapping({"/run-task"})
    public BaseModel taskSchedule_Run(String taskName) {
        BaseModel returnModel = new BaseModel();
        String command = "schtasks /run /tn " + taskName;
        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"cmd.exe", "/c", command});
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0)
                throw new Exception("Run task failed. Error code: " + exitCode);
            returnModel.setMessage("Run task success.");
        } catch (Exception e) {
            returnModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            returnModel.setMessage(e.toString());
        }
        return returnModel;
    }

    public BaseListModel<MissingFileInAWSModel> missingFileInAWS_GetAll(MissingFileInAWSListModel listModel) {
        try {
            Format formatter = new SimpleDateFormat("yyyyMMdd");
            String today = formatter.format(listModel.getDateRequest());
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://fb93d2gfod.execute-api.ap-southeast-1.amazonaws.com/v1/getlistbom")).header("Accept", "*/*").header("Content-Type", "application/json").method("POST", HttpRequest.BodyPublishers.ofString("{\"batchdate\": \"" + today + "\"}")).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            String status = jsonObject.get("statusCode").getAsString();
            int state = jsonObject.get("state").getAsInt();
            JsonArray bodyArray = gson.fromJson(jsonObject.get("body").getAsString(), JsonArray.class);
            if (bodyArray.size() > 0)
                for (int i = 0; i < bodyArray.size(); i++) {
                    String fullName = bodyArray.get(i).getAsString();
                    MissingFileInAWSModel newItem = new MissingFileInAWSModel();
                    newItem.setRowIndex(i + 1);
                    newItem.setFullName(fullName);
                    listModel.dataList.add(newItem);
                }
            JsonArray recordsArray = gson.fromJson(jsonObject.get("records").getAsString(), JsonArray.class);
            if (recordsArray.size() > 0)
                for (int i = 0; i < recordsArray.size(); i++) {
                    String str = recordsArray.get(i).getAsString();
                }
        } catch (Exception e) {
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(e.toString());
        }
        return listModel;
    }

    @GetMapping({"/tasks"})
    private LinkedList<TaskScheduleModel> getTaskSchedulerTasks() {
        LinkedList<TaskScheduleModel> output = new LinkedList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"cmd.exe", "/c", "schtasks /query /tn \\VF_PLM_Scheduler\\ /fo list /v"});
        try {
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String folder = "";
            TaskScheduleModel newItem = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Folder:")) {
                    folder = getValueFromLine(line, "Folder:");
                    continue;
                }
                if (line.contains("HostName:")) {
                    if (newItem != null)
                        output.add(newItem);
                    newItem = new TaskScheduleModel();
                    newItem.setHostName(getValueFromLine(line, "HostName:"));
                    continue;
                }
                if (line.contains("TaskName:")) {
                    assert newItem != null;
                    newItem.setFolder(folder);
                    String taskName = getValueFromLine(line, "TaskName:");
                    newItem.setFullName(taskName);
                    newItem.setTaskName(taskName.replace(folder, "").replace("\\", ""));
                    continue;
                }
                if (line.contains("Next Run Time:")) {
                    assert newItem != null;
                    newItem.setNextRunTime(getTimestampFromLine(line));
                    continue;
                }
                if (line.contains("Status:")) {
                    assert newItem != null;
                    newItem.setStatus(getValueFromLine(line, "Status:"));
                    continue;
                }
                if (line.contains("Logon Mode:")) {
                    assert newItem != null;
                    newItem.setLogonMode(getValueFromLine(line, "Logon Mode:"));
                    continue;
                }
                if (line.contains("Last Run Time:")) {
                    assert newItem != null;
                    newItem.setLastRunTime(getTimestampFromLine(line));
                    continue;
                }
                if (line.contains("Task To Run:")) {
                    assert newItem != null;
                    newItem.setTaskToRun(getValueFromLine(line, "Task To Run:"));
                    continue;
                }
                if (line.contains("Start In:")) {
                    assert newItem != null;
                    newItem.setTaskToRun(getValueFromLine(line, "Start In:"));
                    continue;
                }
                if (line.contains("Scheduled Task State:")) {
                    assert newItem != null;
                    newItem.setScheduledTaskState(getValueFromLine(line, "Scheduled Task State:"));
                    continue;
                }
                if (line.contains("Run As User:")) {
                    assert newItem != null;
                    newItem.setRunAsUser(getValueFromLine(line, "Run As User:"));
                    continue;
                }
                if (line.contains("Schedule Type:")) {
                    assert newItem != null;
                    newItem.setScheduleType(getValueFromLine(line, "Schedule Type:"));
                    continue;
                }
                if (line.contains("Start Time:")) {
                    assert newItem != null;
                    Time startTime = getTimeFromLine(line);
                    newItem.setStartTime(startTime);
                    newItem.setStartTimeString(startTime.toString());
                    continue;
                }
                if (line.contains("Start Date:")) {
                    assert newItem != null;
                    newItem.setStartDate(getDateFromLine(line));
                    continue;
                }
                if (line.contains("Days:")) {
                    assert newItem != null;
                    newItem.setDays(getValueFromLine(line, "Days:"));
                    continue;
                }
                if (line.contains("Months:")) {
                    assert newItem != null;
                    newItem.setMonths(getValueFromLine(line, "Months:"));
                }

                if (line.contains("Repeat: Every:")) {
                    assert newItem != null;
                    newItem.setRepeatEvery(getValueFromLine(line, "Repeat: Every:"));
                }

//                if (line.contains("Repeat: Until: Time:")) {
//                    assert newItem != null;
//                    newItem.setRepeatEvery(line);
//                }
//
//                if (line.contains("Repeat: Until: Duration:")) {
//                    assert newItem != null;
//                    newItem.setRepeatEvery(line);
//                }
//
//                if (line.contains("Repeat: Stop If Still Running:")) {
//                    assert newItem != null;
//                    newItem.setRepeatEvery(line);
//                }
            }
            if (newItem != null)
                output.add(newItem);
            int exitCode = process.waitFor();
            if (exitCode != 0)
                throw new Exception("Lkhi ldanh stask ttask scheduler");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private String getValueFromLine(String inputLine, String header) {
        if (inputLine.contains(header)) {
            return inputLine.replace(header, "").trim();
        }
        return "";
    }

    private Timestamp getTimestampFromLine(String inputLine) {
        Timestamp output = null;
        if (inputLine.contains(":")) {
            String[] parts = inputLine.split(":");
            String dateString = inputLine.replace(parts[0] + ":", "").trim();
            output = DateTimeExtension.convertStringToTimestamp(dateString, "MM/dd/yyyy hh:mm:ss a");
        }
        return output;
    }

    private Time getTimeFromLine(String inputLine) {
        Time output = null;
        if (inputLine.contains(":")) {
            String[] parts = inputLine.split(":");
            String dateString = inputLine.replace(parts[0] + ":", "").trim();
            output = DateTimeExtension.convertStringToTime(dateString, "hh:mm:ss a");
        }
        return output;
    }

    private Date getDateFromLine(String inputLine) {
        Date output = null;
        if (inputLine.startsWith(":")) {
            String[] parts = inputLine.split(":");
            String dateString = inputLine.replace(parts[0] + ":", "").trim();
            output = DateTimeExtension.convertStringToDate(dateString, "MM/dd/yyyy");
        }
        return output;
    }
}
