package com.vinfast.api.controller;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.db.ChangePasswordModel;
import com.vinfast.api.model.db.EmployeeModel;
import com.vinfast.api.model.db.EmployeePagingModel;
import com.vinfast.api.service.interfaces.ISystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/system")
public class SystemController {
    private static final  Logger LOGGER = LogManager.getLogger(SystemController.class);
    @Autowired
    private ApplicationEventPublisher appEventPublisher;
    @Autowired
    private ISystemService systemService;

    @CrossOrigin("http://localhost:4200/")
    @PostMapping(value = "/login")
    public BaseModel login(String userName,
                           String password) {
        return systemService.login(userName, password);
    }

    @CrossOrigin("http://localhost:4200/")
    @PostMapping(value = "/changePassword")
    public BaseModel changePassword(String loginName,
                                    String currentPass,
                                    String newPass,
                                    String reNewPass) {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setLoginName(loginName);
        model.setCurrentPass(currentPass);
        model.setNewPass(newPass);
        model.setReNewPass(reNewPass);
        return systemService.changePassword(model);
    }

    @CrossOrigin("http://localhost:4200/")
    @GetMapping(value = "/employee_GetByCode")
    public EmployeeModel employee_GetByCode(String code) {
        return systemService.employee_GetByCode(code);
    }
    @CrossOrigin({"http://localhost:4200/"})
    @GetMapping({"/employee_GetAll"})
    public BasePagingModel<EmployeeModel> employee_GetAll(@RequestParam String EmployeeCode,
                                                          @RequestParam String EmployeeName,
                                                          @RequestParam(defaultValue = "1") Integer PageIndex,
                                                          @RequestParam(defaultValue = "0") Integer ItemsPerPage,
                                                          @RequestParam String SortColumn,
                                                          @RequestParam(defaultValue = "desc") String SortColumnDir) {
        EmployeePagingModel listModel = new EmployeePagingModel();
        if (EmployeeCode.compareTo("null") == 0)
            EmployeeCode = "";
        listModel.setEmployeeCode(EmployeeCode);
        if (EmployeeName.compareTo("null") == 0)
            EmployeeName = "";
        listModel.setEmployeeName(EmployeeName);
        listModel.setCurrentPage(PageIndex.intValue());
        listModel.setItemsPerPage(ItemsPerPage.intValue());
        listModel.setSortColumn(SortColumn);
        listModel.setSortColumnDir(SortColumnDir);
        return this.systemService.employee_GetAll(listModel);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/employee_Update"})
    public BaseModel employee_Update(@RequestBody EmployeeModel model) {
        return this.systemService.employee_Update(model);
    }

    @CrossOrigin({"http://localhost:4200/"})
    @PostMapping({"/employee_Create"})
    public BaseModel employee_Create(@RequestBody EmployeeModel model) {
        return this.systemService.employee_Create(model);
    }
}
