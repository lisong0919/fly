package com.wealth.fly.common;


import com.wealth.fly.core.constants.CommonConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);



    public static Date parseStandardTime(long time) {
        try {
            return DateUtils.parseDate(String.valueOf(time), CommonConstants.SYSTEM_DATE_FORMAT);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
