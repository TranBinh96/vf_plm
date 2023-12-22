package com.vinfast.api.controller;

import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormBMIDEObjectList;
import com.vinfast.api.service.interfaces.IEngineeringService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/engineering")
public class EngineeringController {
    private static final Logger LOGGER = LogManager.getLogger(EngineeringController.class);
    @Autowired
    private IEngineeringService engineeringService;

    @GetMapping(value = "/impactedProgram_Get")
    public BaseListModel<ImpactedProgramModel> impactedProgram_Get(@RequestParam String[] uid) {
        ImpactedProgramListModel listModel = new ImpactedProgramListModel();
        listModel.setUid(uid);
        BaseListModel<ImpactedProgramModel> dataReturn = engineeringService.impactedProgram_Get(listModel);
        LOGGER.info("[impactedProgram_Get] success.");
        return dataReturn;
    }

    @GetMapping({"/ecrStatus_Get"})
    public BaseListModel<ECRStatusModel> ecrStatus_Get(@RequestParam String[] ecrList) {
        ECRStatusListModel model = new ECRStatusListModel();
        model.setEcrList(ecrList);
        BaseListModel<ECRStatusModel> dataReturn = this.engineeringService.ecrStatus_Get(model);
        LOGGER.info("[ecrStatus_Get] success.");
        return dataReturn;
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bmideObject_GetList"})
    public BaseListModel<BMIDEObjectModel> bmideObject_GetList(@RequestParam String objectType) {
        BMIDEObjectListModel model = new BMIDEObjectListModel();
        model.setObjectName(objectType);
        return this.engineeringService.bmideObject_GetList(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/bmideObjectList_GetDataForm"})
    public DataFormBMIDEObjectList bmideObjectList_GetDataForm() {
        return this.engineeringService.bmideObjectList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/lovValue_GetList"})
    public BaseListModel<LOVValueModel> lovValue_GetList(@RequestParam String objectName, @RequestParam String propertyName) {
        LOVValueListModel model = new LOVValueListModel();
        model.setObjectName(objectName);
        model.setPropertyName(propertyName);
        return this.engineeringService.lovValue_GetList(model);
    }
}
