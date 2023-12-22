package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.dataForm.DataFormSapMaterialMasterList;
import com.vinfast.api.model.db.SapMaterialMasterModel;
import com.vinfast.api.model.db.SapMaterialMasterPagingModel;
import org.springframework.stereotype.Service;

@Service
public interface ISAPService {

    DataFormSapMaterialMasterList sapMaterialMasterList_GetDataForm();

    BasePagingModel<SapMaterialMasterModel> sapMaterialMaster_GetAll(SapMaterialMasterPagingModel paramSapMaterialMasterPagingModel);

    SapMaterialMasterModel sapMaterialMaster_GetByID(long paramLong);
}
