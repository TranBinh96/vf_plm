package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.dataForm.*;
import com.vinfast.api.model.db.*;
import org.springframework.stereotype.Service;

@Service
public interface IDatabaseService {
    BasePagingModel<TCDataPullerWipModel> dataPuller_GetAll(TCDataPullerWipPagingModel paramTCDataPullerWipPagingModel);

    DataFormTCPullerList dataPullerList_GetDataForm();

    TCDataPullerWipModel dataPuller_GetByID(long paramLong);

    BaseModel dataPuller_Create(TCDataPullerWipModel paramTCDataPullerWipModel);

    BaseModel dataPuller_Update(TCDataPullerWipModel paramTCDataPullerWipModel);

    BaseModel dataPuller_MultiUpdate(TCDataPullerWipMultiUpdateModel paramTCDataPullerWipMultiUpdateModel);

    BasePagingModel<BomVerifyMasterModel> bomVerifyMaster_GetAll(BomVerifyMasterPagingModel paramBomVerifyMasterPagingModel);

    BomVerifyMasterModel bomVerifyMaster_GetByID(long paramLong);

    BaseModel bomVerifyMaster_Create(BomVerifyMasterModel paramBomVerifyMasterModel);

    BaseModel bomVerifyMaster_Update(BomVerifyMasterModel paramBomVerifyMasterModel);

    BaseModel bomVerifyMaster_MultiUpdate(BomVerifyMasterMultiUpdateModel paramBomVerifyMasterMultiUpdateModel);

    DataFormBomVerifyMasterList bomVerifyMasterList_GetDataForm();

    DataFormBomVerifyMasterUpdate bomVerifyMasterUpdate_GetDataForm();

    BasePagingModel<EmailSenderModel> emailSender_GetAll(EmailSenderPagingModel paramEmailSenderPagingModel);

    EmailSenderModel emailSender_GetByID(long paramLong);

    BaseModel emailSender_Update(EmailSenderModel paramEmailSenderModel);

    DataFormEmailSenderList emailSenderList_GetDataForm();

    BasePagingModel<RevisionRuleMasterModel> revisionRuleMaster_GetAll(RevisionRuleMasterPagingModel paramRevisionRuleMasterPagingModel);

    RevisionRuleMasterModel revisionRuleMaster_GetByID(long paramLong);

    BaseModel revisionRuleMaster_Create(RevisionRuleMasterModel paramRevisionRuleMasterModel);

    BaseModel revisionRuleMaster_Update(RevisionRuleMasterModel paramRevisionRuleMasterModel);

    BasePagingModel<DataPullerEventMasterModel> dataPullerEventMaster_GetAll(DataPullerEventMasterPagingModel listModel);

    DataPullerEventMasterModel dataPullerEventMaster_GetByID(long id);

    BaseModel dataPullerEventMaster_Create(DataPullerEventMasterModel model);

    BaseModel dataPullerEventMaster_Update(DataPullerEventMasterModel model);

    BasePagingModel<DataPullerSubScriptionHandlerModel> dataPullerSubscriptionHandler_GetAll(DataPullerSubScriptionHandlerPagingModel listModel);

    DataPullerSubScriptionHandlerModel dataPullerSubscriptionHandler_GetByID(long id);

    BaseModel dataPullerSubscriptionHandler_Create(DataPullerSubScriptionHandlerModel model);

    BaseModel dataPullerSubscriptionHandler_Update(DataPullerSubScriptionHandlerModel model);

    DataFormDataPullerSubscriptionList dataPullerSubscriptionList_GetDataForm();

    BasePagingModel<TechnicianMapModel> technicianMap_GetAll(TechnicianMapPagingModel listModel);

    TechnicianMapModel technicianMap_GetByID(long id);

    BaseModel technicianMap_Create(TechnicianMapModel model);

    BaseModel technicianMap_Update(TechnicianMapModel model);

    DataFormTechnicianMapUpdate technicianMapUpdate_GetDataForm();
}