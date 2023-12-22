package com.vinfast.api.controller;

import com.vinfast.api.model.common.BaseFileModel;
import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.service.interfaces.IFRSIntegrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/frsIntegration")
public class FRSIntegrationController {
    private static final Logger LOGGER = LogManager.getLogger(FRSIntegrationController.class);
    @Autowired
    private IFRSIntegrationService integrationService;

    @GetMapping("/frsDSATransfer/{id}")
    public BaseModel frsDSATransfer(@PathVariable String id) {
        BaseModel returnData = integrationService.frsDSATransfer(id);
        LOGGER.info("[frsDSATransfer] finish.");
        return returnData;
    }

    @GetMapping("/sotaTransfer/{id}")
    public BaseModel sotaTransfer(@PathVariable String id) {
        BaseModel returnData = integrationService.frsSOTATransfer(id);
        LOGGER.info("[sotaTransfer] finish.");
        return returnData;
    }

    @GetMapping(value = "/frsDSADownload/{id}")
    public ResponseEntity<ByteArrayResource> frsDSADownload(@PathVariable String id) {
        BaseFileModel baseModel = integrationService.frsDSADownload(id);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ProductTemplate.xml");
        return new ResponseEntity<>(new ByteArrayResource(baseModel.getFile().toByteArray()), header, HttpStatus.OK);

//        try {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            XSSFWorkbook workbook = new XSSFWorkbook();
//            HttpHeaders header = new HttpHeaders();
//            header.setContentType(new MediaType("application", "force-download"));
//            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ProductTemplate.xlsx");
//            workbook.write(stream);
//            workbook.close();
//            return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()), header, HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    @GetMapping(value = "/frsInternalTransfer/{id}")
    public BaseModel frsInternalTransfer(@PathVariable String id) {
        BaseModel returnData = integrationService.frsInternalTransfer(id);
        LOGGER.info("[frsInternalTransfer] finish.");
        return returnData;
    }

    @GetMapping(value = "/frsInternalDownload/{id}")
    public ResponseEntity<ByteArrayResource> frsInternalDownload(@PathVariable String id) {
        BaseFileModel baseModel = integrationService.frsInternalDownload(id);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ProductTemplate.xml");
        return new ResponseEntity<>(new ByteArrayResource(baseModel.getFile().toByteArray()), header, HttpStatus.OK);
    }
}
