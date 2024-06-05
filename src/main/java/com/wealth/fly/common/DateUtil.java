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


    public static Date getNextKlineDate(Long dataTime, DataGranularity dataGranularity) {
        Date date = null;
        try {
            date = DateUtils.parseDate(String.valueOf(dataTime), CommonConstants.SYSTEM_DATE_FORMAT);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        return DateUtils.addSeconds(date, dataGranularity.getSeconds());
    }

    public static Long getNextKlineDataTime(Long dataTime, DataGranularity dataGranularity) {
        Date nextKlineDate = getNextKlineDate(dataTime, dataGranularity);

        return parseToDataTime(nextKlineDate);
    }


    public static Long getPreKLineDataTime(long dateTime, DataGranularity dataGranularity) {
        Date date = null;
        try {
            date = DateUtils.parseDate(String.valueOf(dateTime), CommonConstants.SYSTEM_DATE_FORMAT);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        Date preDate = DateUtils.addSeconds(date, -dataGranularity.getSeconds());
        return Long.valueOf(new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(preDate));
    }

    public static Long getLatestKLineDataTime(Date date, DataGranularity dataGranularity) {
        String strRes = new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(getLatestKLineTime(date, dataGranularity));
        return Long.valueOf(strRes);
    }

    public static Date getLatestKLineTime(Date date, DataGranularity dataGranularity) {
        //余数，如5分钟时间粒度的情况下：lastLineTime=14:15，now：14:37,相差22分钟；那么余数就是2分钟，即：2*60*1000
        long timeSeconds = date.getTime() / 1000;
        long ts = timeSeconds - timeSeconds % dataGranularity.getSeconds() - dataGranularity.getSeconds();

        return new Date(ts * 1000);
    }

    public static void main(String[] args) {
        System.out.println(parseStandardTime(1698768000000L));
    }

    public static Long parseToDataTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT);

        return Long.valueOf(sdf.format(date));
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
