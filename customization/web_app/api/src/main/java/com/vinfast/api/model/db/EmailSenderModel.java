package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qemail_sender;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EmailSenderModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String email_to;
    private String subject;
    private String message;
    private String priority;
    private String status;
    private Timestamp created_on;
    private Timestamp updated_on;

    public boolean validData() {

        return true;
    }

    private final static Qemail_sender objectTable = Qemail_sender.email_sender;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("email_to", objectTable.email_to);
        put("subject", objectTable.subject);
        put("message", objectTable.message);
        put("priority", objectTable.priority);
        put("status", objectTable.status);
        put("created_on", objectTable.created_on);
        put("updated_on", objectTable.updated_on);
    }};
}
