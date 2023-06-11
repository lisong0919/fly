package com.wealth.fly.common;


import com.wealth.fly.core.constants.CommonConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.wealth.fly.core.constants.DataGranularity;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);


    public static Long getPreKLineDataTime(long time, DataGranularity dataGranularity) {
        Date date = null;
        try {
            date = DateUtils.parseDate(String.valueOf(time), CommonConstants.SYSTEM_DATE_FORMAT);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        Date preDate = DateUtils.addSeconds(date, -dataGranularity.getSeconds());
        return Long.valueOf(new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(preDate));
    }

    public static Date parseStandardTime(long time) {
        try {
            Date result = DateUtils.parseDate(String.valueOf(time), CommonConstants.SYSTEM_DATE_FORMAT);
            result = DateUtils.toCalendar(result, TimeZone.getTimeZone("Asia/Shanghai")).getTime();
            return result;
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

}
