package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.BaseFileModel;
import com.vinfast.api.model.common.BaseModel;
import org.springframework.stereotype.Service;

@Service
public interface IFRSIntegrationService {
    BaseModel frsSOTATransfer(String topBOMUID);
    BaseFileModel frsInternalDownload(String topBOMUID);
    BaseModel frsInternalTransfer(String topBOMUID);
    BaseFileModel frsDSADownload(String topBOMUID);
    BaseModel frsDSATransfer(String topBOMUID);
}
