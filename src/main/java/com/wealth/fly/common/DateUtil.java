package com.wealth.fly.common;


import com.wealth.fly.core.constants.CommonConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);


  public static String transferTime(String time, String fromDateFormat, String toDateformat) {
    try {
      Date date = new SimpleDateFormat(fromDateFormat).parse(time);
      return new SimpleDateFormat(toDateformat).format(date);
    } catch (ParseException e) {
      LOGGER.info(e.getMessage(), e);
      throw new RuntimeException("invalid date format " + time);
    }
  }

  public static String formatToStandardTime(Date date) {
    return new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(date);
  }

  public static Date parseStandardTime(long time) {
    return DateUtils
        .parseDate(String.valueOf(time), new String[]{CommonConstants.SYSTEM_DATE_FORMAT});
  }
}
