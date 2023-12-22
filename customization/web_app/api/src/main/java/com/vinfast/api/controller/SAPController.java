package com.vinfast.api.controller;

import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.dataForm.DataFormSapMaterialMasterList;
import com.vinfast.api.model.db.SapMaterialMasterModel;
import com.vinfast.api.model.db.SapMaterialMasterPagingModel;
import com.vinfast.api.service.interfaces.ISAPService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@RestController
@RequestMapping({"/sap"})
public class SAPController {
    private static final Logger LOGGER = LogManager.getLogger(SAPController.class);
    @Autowired
    private ApplicationEventPublisher appEventPublisher;

    @Autowired
    private ISAPService sapService;
    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/sapMaterialMaster_GetAll"})
    public BasePagingModel<SapMaterialMasterModel> sapMaterialMaster_GetAll(@RequestParam String PartNumber,
                                                                            @RequestParam String PartName,
                                                                            @RequestParam String Status,
                                                                            @RequestParam String MaterialType,
                                                                            @RequestParam String CreateFromDate,
                                                                            @RequestParam String CreateToDate,
                                                                            @RequestParam String UpdateFromDate,
                                                                            @RequestParam String UpdateToDate,
                                                                            @RequestParam(defaultValue = "1") Integer PageIndex,
                                                                            @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                                            @RequestParam String SortColumn,
                                                                            @RequestParam(defaultValue = "desc") String SortColumnDir) {
        SapMaterialMasterPagingModel listModel = new SapMaterialMasterPagingModel();
        if (PartNumber.compareTo("null") == 0)
            PartNumber = "";
        listModel.setPartNumber(PartNumber);
        if (PartName.compareTo("null") == 0)
            PartName = "";
        listModel.setPartName(PartName);
        if (Status.compareTo("null") == 0)
            Status = "";
        listModel.setStatus(Status);
        if (MaterialType.compareTo("null") == 0)
            MaterialType = "";
        listModel.setMaterialType(MaterialType);
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
        return this.sapService.sapMaterialMaster_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/sapMaterialMasterList_GetDataForm"})
    public DataFormSapMaterialMasterList sapMaterialMaster_GetDataForm() {
        return this.sapService.sapMaterialMasterList_GetDataForm();
    }

    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/sapMaterialMaster_GetByID"})
    public SapMaterialMasterModel sapMaterialMaster_GetByID(long id) {
        return this.sapService.sapMaterialMaster_GetByID(id);
    }

}
