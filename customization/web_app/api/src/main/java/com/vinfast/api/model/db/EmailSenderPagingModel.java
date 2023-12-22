package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class EmailSenderPagingModel extends BasePagingModel<EmailSenderModel> {
    private String emailTo;
    private String subject;
    private String status;
    private Timestamp createFromDate = null;
    private Timestamp createToDate = null;
    private Timestamp updateFromDate = null;
    private Timestamp updateToDate = null;
}

