package com.vinfast.api.service.implement;

import com.vinfast.api.service.interfaces.IIntegrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IntegrationService implements IIntegrationService {
    private static final Logger LOGGER = LogManager.getLogger(IntegrationService.class);
}
