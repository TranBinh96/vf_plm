export class ApiUrl {
  //database
  static dataPuller_GetAll = 'database/dataPuller_GetAll';
  static dataPuller_GetByID = 'database/dataPuller_GetByID';
  static dataPuller_Create = 'database/dataPuller_Create';
  static dataPuller_Update = 'database/dataPuller_Update';
  static dataPuller_MultiUpdate = 'database/dataPuller_MultiUpdate';
  static dataPullerList_GetDataForm = 'database/dataPullerList_GetDataForm';

  static bomVerifyMaster_GetAll = 'database/bomVerifyMaster_GetAll';
  static bomVerifyMaster_GetByID = 'database/bomVerifyMaster_GetByID';
  static bomVerifyMaster_Create = 'database/bomVerifyMaster_Create';
  static bomVerifyMaster_Update = 'database/bomVerifyMaster_Update';
  static bomVerifyMaster_MultiUpdate = 'database/bomVerifyMaster_MultiUpdate';
  static bomVerifyMasterList_GetDataForm = 'database/bomVerifyMasterList_GetDataForm';
  static bomVerifyMasterUpdate_GetDataForm = 'database/bomVerifyMasterUpdate_GetDataForm';

  static emailSender_GetAll = 'database/emailSender_GetAll';
  static emailSender_GetByID = 'database/emailSender_GetByID';
  static emailSender_Update = 'database/emailSender_Update';
  static emailSenderList_GetDataForm = 'database/emailSenderList_GetDataForm';

  static sapMaterialMaster_GetAll = 'sap/sapMaterialMaster_GetAll';
  static sapMaterialMaster_GetByID = 'sap/sapMaterialMaster_GetByID';
  static sapMaterialMasterList_GetDataForm = 'sap/sapMaterialMasterList_GetDataForm';

  static revisionRuleMaster_GetAll = 'database/revisionRuleMaster_GetAll';
  static revisionRuleMaster_GetByID = 'database/revisionRuleMaster_GetByID';
  static revisionRuleMaster_Update = 'database/revisionRuleMaster_Update';

  static dataPullerEventMaster_GetAll = 'database/dataPullerEventMaster_GetAll';
  static dataPullerEventMaster_GetByID = 'database/dataPullerEventMaster_GetByID';
  static dataPullerEventMaster_Update = 'database/dataPullerEventMaster_Update';

  static dataPullerSubscriptionHandler_GetAll = 'database/dataPullerSubscriptionHandler_GetAll';
  static dataPullerSubscriptionHandler_GetByID = 'database/dataPullerSubscriptionHandler_GetByID';
  static dataPullerSubscriptionHandler_Update = 'database/dataPullerSubscriptionHandler_Update';
  static dataPullerSubscriptionList_GetDataForm = 'database/dataPullerSubscriptionList_GetDataForm';

  static technicianMap_GetAll = 'database/technicianMap_GetAll';
  static technicianMap_GetByID = 'database/technicianMap_GetByID';
  static technicianMap_Update = 'database/technicianMap_Update';
  static technicianMapUpdate_GetDataForm = 'database/technicianMapUpdate_GetDataForm';

  //Engineering
  static bmideObject_GetList = 'engineering/bmideObject_GetList';
  static bmideObjectList_GetDataForm = 'engineering/bmideObjectList_GetDataForm';
  static lovValue_GetList = 'engineering/lovValue_GetList';

  //Task Scheduler
  static jobMaster_GetAll = 'taskSchedule/jobMaster_GetAll';
  static jobMaster_GetByID = 'taskSchedule/jobMaster_GetByID';
  static jobMaster_Create = 'taskSchedule/jobMaster_Create';
  static jobMaster_Update = 'taskSchedule/jobMaster_Update';
  static jobMaster_SyncTask = 'taskSchedule/jobMaster_SyncTask';
  static jobMasterList_GetDataForm = 'taskSchedule/jobMasterList_GetDataForm';

  static jobCategory_GetAll = 'taskSchedule/jobCategory_GetAll';
  static jobCategory_GetByID = 'taskSchedule/jobCategory_GetByID';
  static jobCategory_Update = 'taskSchedule/jobCategory_Update';

  static jobHistory_GetAll = 'taskSchedule/jobHistory_GetAll';
  static jobHistory_GetByID = 'taskSchedule/jobHistory_GetByID';
  static jobHistory_Create = 'taskSchedule/jobHistory_Create';
  static jobHistoryList_GetDataForm = 'taskSchedule/jobHistoryList_GetDataForm';

  static taskSchedule_GetAll = 'taskSchedule/taskSchedule_GetAll';
  static taskSchedule_Run = 'taskSchedule/taskSchedule_Run';
  static taskScheduleList_GetDataForm = 'taskSchedule/taskScheduleList_GetDataForm';

  static missingFileInAWS_GetAll = 'taskSchedule/missingFileInAWS_GetAll';

  //FRS Integration
  static frsInternal_Download = 'frsIntegration/frsInternalDownload';
  static frsDSA_Download = 'frsIntegration/frsDSADownload';

  // System
  static employee_GetByCode = 'system/employee_GetByCode';
  static employee_GetAll = 'system/employee_GetAll';
  static employee_Create = 'system/employee_Create';
  static employee_Update = 'system/employee_Update';
  static checkPermission = 'system/CheckPermission';
  static calendarMonth_GetAll = 'system/CalendarMonth_GetAll';
  // System account
  static userLogin = 'system/login';
  static userChangePassword = 'system/changePassword';
  
  //Data Form
  static GetListFile_ByFolder = 'common/GetListFile_ByFolder';
  static UploadFile = 'common/UploadFile';

  //
  static requestMaster_GetByID = 'request/RequestMaster_GetByID';
  static requestMaster_GetCompBySampleType = 'request/RequestMaster_GetCompBySampleType';
  static GetDataForm_RequestCompReport = 'dataform/GetDataFormRequestCompReport';
}
