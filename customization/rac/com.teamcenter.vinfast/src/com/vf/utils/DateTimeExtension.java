package com.vf.utils;

import java.util.Calendar;

public class DateTimeExtension {
	public static long getDayBetweenDates(Calendar startDate, Calendar endDate) {
		long diff = startDate.getTimeInMillis() - endDate.getTimeInMillis();
		return diff / (24 * 60 * 60 * 1000);
	}
}
