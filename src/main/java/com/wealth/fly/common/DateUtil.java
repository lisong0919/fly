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

    public static String formatToStandardTime(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(date);
    }

    public static float getDistanceDays(long start, long end) {

        Date startDate = DateUtil.parseStandardTime(start);
        Date endDate = DateUtil.parseStandardTime(end);

        return (endDate.getTime() - startDate.getTime()) / 1000 / 60 / 60 / 24;
    }

    public static float getDistanceHours(long start, long end) {

        Date startDate = parseStandardTime(start);
        Date endDate = parseStandardTime(end);

        return (endDate.getTime() - startDate.getTime()) / 1000 / 60 / 60;
    }


    public static float getDistanceMinutes(long start, long end) {

        Date startDate = parseStandardTime(start);
        Date endDate = parseStandardTime(end);

        return (endDate.getTime() - startDate.getTime()) / 1000 / 60;
    }

    public static long getPreDateTime(long standardDateTime, DataGranularity dataGranularity) {
        return getPreDateTime(standardDateTime, dataGranularity, 1);
    }

    public static long getPreDateTime(long standardDateTime, DataGranularity dataGranularity, int preCount) {
        Date date = parseStandardTime(standardDateTime);

        Date resDate = DateUtils.addSeconds(date, -dataGranularity.getSeconds() * preCount);

        return Long.valueOf(new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(resDate));
    }

    public static void main(String[] args) {

        System.out.println(getPreDateTime(202012202000L, DataGranularity.FOUR_HOUR, 3));
    }
}
