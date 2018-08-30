package de.hgu.gsehen.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
  @SuppressWarnings({"checkstyle:javadocmethod"})
  public static Date truncToDayUsingCalendar(Calendar calendar, Date date) {
    calendar.setTime(date);
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    return calendar.getTime();
  } 

  public static Date truncToDay(Date date) {
    return truncToDayUsingCalendar(Calendar.getInstance(), date);
  }

  public static boolean between(Date date, Date start, Date end) {
    if (date == null || (start == null && end == null)) {
      return false;
    }
    return (
      start == null || start.getTime() <= date.getTime()
    )
    && (
      end == null || end.getTime() >= date.getTime()
    );
  }
}
