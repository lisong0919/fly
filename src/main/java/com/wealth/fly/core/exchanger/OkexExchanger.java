package com.wealth.fly.core.exchanger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.xdevapi.JsonArray;
import com.wealth.fly.api.common.HttpClientUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OkexExchanger implements Exchanger {

    @Value("${okex.host}")
    private String host;
    private static final String SYSTEM_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String OK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final Logger LOGGER = LoggerFactory.getLogger(OkexExchanger.class);

    private final String KLINE_PATH = "/api/swap/v3/instruments";

    public List<KLine> getKlineData(String currency, String startTime, String endTime, DataGranularity dataGranularity) {
        try {
            String url = host + KLINE_PATH + currency + "/candles?start=" + transferTimeToOkexStandard(startTime) + "&end=" + transferTimeToOkexStandard(endTime) + "&granularity";
            String jsonResponse = HttpClientUtil.get(url);


        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        String str = "[[\"2020-05-06T15:35:00.000Z\",\"9269.2\",\"9275.9\",\"9259\",\"9265.9\",\"20305\",\"203.05\"],[\"2020-05-06T15:30:00.000Z\",\"9281.4\",\"9298.4\",\"9265\",\"9269.2\",\"29294\",\"292.94\"]]";
        JSONArray jsonArray = JSONObject.parseArray(str);


        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray data = (JSONArray) jsonArray.get(i);
            Long dateTime=new Long(transferTimeToSystemStandard((String) data.get(0)));

            KLine kLine = new KLine();
            kLine.setDataTime(dateTime);
            kLine.setOpen(new BigDecimal((String) data.get(1)));
            kLine.setHigh(new BigDecimal((String) data.get(2)));
            kLine.setLow(new BigDecimal((String) data.get(3)));
            kLine.setClose(new BigDecimal((String) data.get(4)));
            kLine.setVolume( Long.parseLong((String) data.get(5)));
            kLine.setCurrencyVolume(new BigDecimal((String) data.get(4)));


            System.out.println(kLine);
        }
    }

    private static String transferTimeToOkexStandard(String time) {
        return transferTime(SYSTEM_DATE_FORMAT, OK_DATE_FORMAT, time);
    }

    private static String transferTimeToSystemStandard(String time) {
        return transferTime(OK_DATE_FORMAT, SYSTEM_DATE_FORMAT, time);
    }

    private static String transferTime(String fromDateFormat, String toDateformat, String time) {
        Date date = null;
        try {
            date = new SimpleDateFormat(fromDateFormat).parse(time);
        } catch (ParseException e) {
            LOGGER.info(e.getMessage(), e);
            throw new RuntimeException("invalid date format " + time);
        }

        return new SimpleDateFormat(toDateformat).format(date);
    }
}
