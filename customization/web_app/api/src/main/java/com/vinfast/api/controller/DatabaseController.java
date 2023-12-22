package com.vinfast.api.controller;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.dataForm.*;
import com.vinfast.api.model.db.*;
import com.vinfast.api.service.interfaces.IDatabaseService;

import java.security.Key;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/database"})
public class DatabaseController {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseController.class);

    @Autowired
    private ApplicationEventPublisher appEventPublisher;

    @Autowired
    private IDatabaseService databaseService;

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPuller_GetAll"})
    public BasePagingModel<TCDataPullerWipModel> dataPuller_GetAll(@RequestParam String Type,
                                                                   @RequestParam String UID,
                                                                   @RequestParam String Status,
                                                                   @RequestParam String Error,
                                                                   @RequestParam String CreateFromDate,
                                                                   @RequestParam String CreateToDate,
                                                                   @RequestParam String UpdateFromDate,
                                                                   @RequestParam String UpdateToDate,
                                                                   @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                   @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                   @RequestParam String SortColumn,
                                                                   @RequestParam(defaultValue = "desc") String SortColumnDir) {
        TCDataPullerWipPagingModel listModel = new TCDataPullerWipPagingModel();
        if (Type.compareTo("null") == 0)
            Type = "";
        listModel.setType(Type);
        if (UID.compareTo("null") == 0)
            UID = "";
        listModel.setUid(UID);
        if (Status.compareTo("null") == 0)
            Status = "";
        listModel.setStatus(Status);
        if (Error.compareTo("null") == 0)
            Error = "";
        listModel.setError(Error);
        try {
            Timestamp ts = Timestamp.valueOf(CreateFromDate);
            listModel.setCreateFromDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(CreateToDate);
            listModel.setCreateToDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(UpdateFromDate);
            listModel.setUpdateFromDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(UpdateToDate);
            listModel.setUpdateToDate(ts);
        } catch (Exception ignored) {
        }
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.dataPuller_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerList_GetDataForm"})
    public DataFormTCPullerList dataPullerList_GetDataForm() {
        return this.databaseService.dataPullerList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPuller_GetByID"})
    public TCDataPullerWipModel dataPuller_GetByID(long id) {
        return this.databaseService.dataPuller_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/dataPuller_Create"})
    public BaseModel dataPuller_Create(@RequestBody TCDataPullerWipModel model) {
        return this.databaseService.dataPuller_Create(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/dataPuller_Update"})
    public BaseModel dataPuller_Update(@RequestBody TCDataPullerWipModel model) {
        return this.databaseService.dataPuller_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/dataPuller_MultiUpdate"})
    public BaseModel dataPuller_MultiUpdate(@RequestBody TCDataPullerWipMultiUpdateModel model) {
        return this.databaseService.dataPuller_MultiUpdate(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bomVerifyMaster_GetAll"})
    public BasePagingModel<BomVerifyMasterModel> bomVerifyMaster_GetAll(@RequestParam String ProgramName,
                                                                        @RequestParam String ModuleName,
                                                                        @RequestParam String NotifyUser,
                                                                        @RequestParam String BomType,
                                                                        @RequestParam String RevisionRule,
                                                                        @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                        @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                        @RequestParam String SortColumn,
                                                                        @RequestParam(defaultValue = "desc") String SortColumnDir) {
        BomVerifyMasterPagingModel listModel = new BomVerifyMasterPagingModel();
        if (BomType.compareTo("null") == 0)
            BomType = "";
        listModel.setBomType(BomType);
        if (ProgramName.compareTo("null") == 0)
            ProgramName = "";
        listModel.setProgramName(ProgramName);
        if (ModuleName.compareTo("null") == 0)
            ModuleName = "";
        listModel.setModuleName(ModuleName);
        if (NotifyUser.compareTo("null") == 0)
            NotifyUser = "";
        listModel.setNotifier(NotifyUser);
        if (RevisionRule.compareTo("null") == 0)
            RevisionRule = "";
        listModel.setRevisionRule(RevisionRule);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.bomVerifyMaster_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bomVerifyMasterList_GetDataForm"})
    public DataFormBomVerifyMasterList bomVerifyMasterList_GetDataForm() {
        return this.databaseService.bomVerifyMasterList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bomVerifyMasterUpdate_GetDataForm"})
    public DataFormBomVerifyMasterUpdate bomVerifyMasterUpdate_GetDataForm() {
        return this.databaseService.bomVerifyMasterUpdate_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bomVerifyMaster_GetByID"})
    public BomVerifyMasterModel bomVerifyMaster_GetByID(long id) {
        return this.databaseService.bomVerifyMaster_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/bomVerifyMaster_Create"})
    public BaseModel bomVerifyMaster_Create(@RequestBody BomVerifyMasterModel model) {
        return this.databaseService.bomVerifyMaster_Create(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/bomVerifyMaster_Update"})
    public BaseModel bomVerifyMaster_Update(@RequestBody BomVerifyMasterModel model) {
        if (model.getId() == null)
            return this.databaseService.bomVerifyMaster_Create(model);
        return this.databaseService.bomVerifyMaster_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/bomVerifyMaster_MultiUpdate"})
    public BaseModel bomVerifyMaster_MultiUpdate(@RequestBody BomVerifyMasterMultiUpdateModel model) {
        return this.databaseService.bomVerifyMaster_MultiUpdate(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/emailSender_GetAll"})
    public BasePagingModel<EmailSenderModel> emailSender_GetAll(@RequestParam String EmailTo,
                                                                @RequestParam String Subject,
                                                                @RequestParam String Status,
                                                                @RequestParam String CreateFromDate,
                                                                @RequestParam String CreateToDate,
                                                                @RequestParam String UpdateFromDate,
                                                                @RequestParam String UpdateToDate,
                                                                @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                @RequestParam String SortColumn,
                                                                @RequestParam(defaultValue = "desc") String SortColumnDir) {
        EmailSenderPagingModel listModel = new EmailSenderPagingModel();
        if (EmailTo.compareTo("null") == 0)
            EmailTo = "";
        listModel.setEmailTo(EmailTo);
        if (Subject.compareTo("null") == 0)
            Subject = "";
        listModel.setSubject(Subject);
        if (Status.compareTo("null") == 0)
            Status = "";
        listModel.setStatus(Status);
        try {
            Timestamp ts = Timestamp.valueOf(CreateFromDate);
            listModel.setCreateFromDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(CreateToDate);
            listModel.setCreateToDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(UpdateFromDate);
            listModel.setUpdateFromDate(ts);
        } catch (Exception ignored) {
        }
        try {
            Timestamp ts = Timestamp.valueOf(UpdateToDate);
            listModel.setUpdateToDate(ts);
        } catch (Exception ignored) {
        }
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.emailSender_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/emailSenderList_GetDataForm"})
    public DataFormEmailSenderList emailSender_GetDataForm() {
        return this.databaseService.emailSenderList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/emailSender_GetByID"})
    public EmailSenderModel emailSender_GetByID(long id) {
        return this.databaseService.emailSender_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/emailSender_Update"})
    public BaseModel emailSender_Update(@RequestBody EmailSenderModel model) {
        return this.databaseService.emailSender_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/revisionRuleMaster_GetAll"})
    public BasePagingModel<RevisionRuleMasterModel> revisionRuleMaster_GetAll(@RequestParam String Name,
                                                                              @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                              @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                              @RequestParam String SortColumn,
                                                                              @RequestParam(defaultValue = "desc") String SortColumnDir) {
        RevisionRuleMasterPagingModel listModel = new RevisionRuleMasterPagingModel();
        if (Name.compareTo("null") == 0)
            Name = "";
        listModel.setName(Name);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.revisionRuleMaster_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/revisionRuleMaster_GetByID"})
    public RevisionRuleMasterModel revisionRuleMaster_GetByID(long id) {
        return this.databaseService.revisionRuleMaster_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/revisionRuleMaster_Update"})
    public BaseModel revisionRuleMaster_Update(@RequestBody RevisionRuleMasterModel model) {
        if (model.getId() == null)
            return this.databaseService.revisionRuleMaster_Create(model);
        return this.databaseService.revisionRuleMaster_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerSubscriptionHandler_GetAll"})
    public BasePagingModel<DataPullerSubScriptionHandlerModel> dataPullerSubscriptionHandler_GetAll(@RequestParam String TCEventType,
                                                                                                    @RequestParam String ObjectType,
                                                                                                    @RequestParam String EventID,
                                                                                                    @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                                                    @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                                                    @RequestParam String SortColumn,
                                                                                                    @RequestParam(defaultValue = "desc") String SortColumnDir) {
        DataPullerSubScriptionHandlerPagingModel listModel = new DataPullerSubScriptionHandlerPagingModel();
        if (TCEventType.compareTo("null") == 0)
            TCEventType = "";
        listModel.setTc_event_type(TCEventType);
        if (ObjectType.compareTo("null") == 0)
            ObjectType = "";
        listModel.setObject_type(ObjectType);
        try {
            listModel.setEvent_id(Integer.valueOf(Integer.parseInt(EventID)));
        } catch (Exception ignored) {
        }

        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.dataPullerSubscriptionHandler_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerSubscriptionHandler_GetByID"})
    public DataPullerSubScriptionHandlerModel dataPullerSubscriptionHandler_GetByID(long id) {
        return this.databaseService.dataPullerSubscriptionHandler_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/dataPullerSubscriptionHandler_Update"})
    public BaseModel dataPullerSubscriptionHandler_Update(@RequestBody DataPullerSubScriptionHandlerModel model) {
        if (model.getId() == null)
            return this.databaseService.dataPullerSubscriptionHandler_Create(model);
        return this.databaseService.dataPullerSubscriptionHandler_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerSubscriptionList_GetDataForm"})
    public DataFormDataPullerSubscriptionList dataPullerSubscriptionList_GetDataForm() {
        return this.databaseService.dataPullerSubscriptionList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerEventMaster_GetAll"})
    public BasePagingModel<DataPullerEventMasterModel> dataPullerEventMaster_GetAll(@RequestParam String Name,
                                                                                    @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                                    @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                                    @RequestParam String SortColumn,
                                                                                    @RequestParam(defaultValue = "desc") String SortColumnDir) {
        DataPullerEventMasterPagingModel listModel = new DataPullerEventMasterPagingModel();
        if (Name.compareTo("null") == 0)
            Name = "";
        listModel.setName(Name);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.dataPullerEventMaster_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/dataPullerEventMaster_GetByID"})
    public DataPullerEventMasterModel dataPullerEventMaster_GetByID(long id) {
        return this.databaseService.dataPullerEventMaster_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/dataPullerEventMaster_Update"})
    public BaseModel dataPullerEventMaster_Update(@RequestBody DataPullerEventMasterModel model) {
        if (model.getId() == null)
            return this.databaseService.dataPullerEventMaster_Create(model);
        return this.databaseService.dataPullerEventMaster_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/technicianMap_GetAll"})
    public BasePagingModel<TechnicianMapModel> technicianMap_GetAll(@RequestParam String Code,
                                                                    @RequestParam String Name,
                                                                    @RequestParam String KeyWord,
                                                                    @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                    @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                    @RequestParam String SortColumn,
                                                                    @RequestParam(defaultValue = "desc") String SortColumnDir) {
        TechnicianMapPagingModel listModel = new TechnicianMapPagingModel();
        if (Code.compareTo("null") == 0)
            Code = "";
        listModel.setEmployee_code(Code);
        if (Name.compareTo("null") == 0)
            Name = "";
        listModel.setEmployee_name(Name);
        if (KeyWord.compareTo("null") == 0)
            KeyWord = "";
        listModel.setKey_word(KeyWord);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.databaseService.technicianMap_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/technicianMap_GetByID"})
    public TechnicianMapModel technicianMap_GetByID(long id) {
        return this.databaseService.technicianMap_GetByID(id);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/technicianMap_Update"})
    public BaseModel technicianMap_Update(@RequestBody TechnicianMapModel model) {
        if (model.getId() == null)
            return this.databaseService.technicianMap_Create(model);
        return this.databaseService.technicianMap_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/technicianMapUpdate_GetDataForm"})
    public DataFormTechnicianMapUpdate technicianMapUpdate_GetDataForm() {
        return this.databaseService.technicianMapUpdate_GetDataForm();
    }
}
