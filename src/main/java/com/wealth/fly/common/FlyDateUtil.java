package com.wealth.fly.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FlyDateUtil {

    private static final Logger LOGGER= LoggerFactory.getLogger(FlyDateUtil.class);



    public static String transferTime(String fromDateFormat, String toDateformat, String time) {
        try {
            Date date = new SimpleDateFormat(fromDateFormat).parse(time);
            return new SimpleDateFormat(toDateformat).format(date);
        } catch (ParseException e) {
            LOGGER.info(e.getMessage(), e);
            throw new RuntimeException("invalid date format " + time);
        }
    }
}
