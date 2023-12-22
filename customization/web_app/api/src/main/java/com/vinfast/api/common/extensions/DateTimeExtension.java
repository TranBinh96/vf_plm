package com.vinfast.api.common.extensions;

import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DateTimeExtension {
    public static Timestamp convertStringToTimestamp(String dateString, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = (Date) dateFormat.parse(dateString);
            return new Timestamp(date.getTime());
        } catch (Exception exception) {
            return null;
        }
    }

    public static Time convertStringToTime(String dateString, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = (Date) dateFormat.parse(dateString);
            return new Time(date.getTime());
        } catch (Exception exception) {
            return null;
        }
    }

    public static Date convertStringToDate(String dateString, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = (Date) dateFormat.parse(dateString);
            return new Date(date.getTime());
        } catch (Exception exception) {
            return null;
        }
    }
}
