package resource.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class qString {

	/**
	 * try to cut of the tail if the len excess <code>len_from_head</code> size. <br>
	 * null and shorter string protected. <br>
	 * ie: <code> qString.try_cut_tail("hamburger", 4) returns "hamb" </code><br>
	 * <code> qString.try_cut_tail("ha", 4) returns "ha" </code><br>
	 * @param origin
	 * @param len_from_head
	 * @return
	 */
	public static String try_cut_tail(String origin, int len_from_head) {
		String a = origin;
		if(origin!=null){
			int len  = origin.length();
			if(len > len_from_head) {
				a = origin.substring(0, len_from_head);
			}
		}	
		return a;
	}
	
	public static boolean is_not_empty(String s) {
		return (s!=null && !s.isEmpty());
	}
	
	
	public static String calendarToTcDateStringWriteIn(final Calendar date)
	{
		// Following two formats are supported for date string:
		// yyyy-MM-ddThh:mm:sszz:zz 2005-05-20T14:32:05-08:00
		// yyyy-MM-ddThh:mm:ss.SSSzz:zz 2005-05-20T14:32:05.345-08:00
		
		// final String format = "%1$tFT%1$tT"; // without millisecons
		final String format = "%1$tFT%1$tT.%1$tL"; // with milliseconds
		final String timeZone = String.format("%1$tz", date);
		
		return String.format(format + timeZone.substring(0, 3) + ":" + timeZone.substring(3), date);
	}
	
	/**
	 * convert display value of data to write value DB TC
	 * dd-Mmm-yyyy hh:mm -> yyyy-MM-ddThh:mm:sszz:zz 2005-05-20T14:32:05-08:00
	 * @param readin
	 * @return
	 */
	public static String calendarTCread2Write(String readin)
	{
		// Following two formats are supported for date string:
		// dd-Mmm-yyyy hh:mm
		
		SimpleDateFormat in = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		Date din;
		try {
			din = in.parse(readin );
			
		} catch (ParseException e) {
			System.out.println("Error Parsing date=" + readin);
			e.printStackTrace();
			return null;
		}
		
		// Following two formats are supported for date string:
		// yyyy-MM-ddThh:mm:sszz:zz 2005-05-20T14:32:05-08:00
		// yyyy-MM-ddThh:mm:ss.SSSzz:zz 2005-05-20T14:32:05.345-08:00
		
		Calendar cal = Calendar.getInstance();
		  cal.setTime(din);

		// final String format = "%1$tFT%1$tT"; // without millisecons
		final String format = "%1$tFT%1$tT.%1$tL"; // with milliseconds
		final String timeZone = String.format("%1$tz", cal);
		
		return String.format(format + timeZone.substring(0, 3) + ":" + timeZone.substring(3), cal);
		
	}
	
	public static String calendarToTcDateStringQueryDate(final Calendar date)
	{
		// Following two formats are supported for date string:
		// dd-Mmm-yyyy hh:mm
		
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-YYYY HH:mm");
		return format1.format(date.getTime());
	}

	/**
	 * 
	 * @param value
	 * @param ignoreDataset_list
	 * @param logic_mode </p>"ONE" one of item in list match </p>
	 *        "ALL" must match all in list
	 * @return
	 */
	public static boolean is_in_list_of(String value,
			List<String> ignoreDataset_list, String logic_mode) {
		boolean matched = false;
		
		if(logic_mode.equals("ONE")) {
			matched = false; //default return
			for(String reg: ignoreDataset_list) {
				if(value.matches(reg)) {
					return true;
				}
			}
		}
		
		if(logic_mode.equals("ALL")) {
			matched = true; //default return
			for(String reg: ignoreDataset_list) {
				if(!value.matches(reg)) {
					return false;
				}
			}
		}
		
		return matched;
	}
}
