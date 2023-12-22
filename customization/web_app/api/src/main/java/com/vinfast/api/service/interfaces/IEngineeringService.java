package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormBMIDEObjectList;
import org.springframework.stereotype.Service;

@Service
public interface IEngineeringService {
    BaseListModel<ImpactedProgramModel> impactedProgram_Get(ImpactedProgramListModel paramImpactedProgramListModel);

    BaseListModel<ECRStatusModel> ecrStatus_Get(ECRStatusListModel paramECRStatusListModel);

    BaseListModel<BMIDEObjectModel> bmideObject_GetList(BMIDEObjectListModel paramBMIDEObjectListModel);

    DataFormBMIDEObjectList bmideObjectList_GetDataForm();
    BaseListModel<LOVValueModel> lovValue_GetList(LOVValueListModel listModel);
}
