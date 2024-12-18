package com.wealth.fly.core.exchanger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OkexExchanger implements Exchanger {

    @Value("${okex.host}")
    private String host;
    private static final Logger LOGGER = LoggerFactory.getLogger(OkexExchanger.class);

    private final String KLINE_PATH = "/api/swap/v3/instruments";

    public List<KLine> getKlineData(String currency, Date startTime, Date endTime,
                                    DataGranularity dataGranularity) {
        try {
            String url =
                    host + KLINE_PATH + "/" + currency + "/candles?granularity=" + dataGranularity
                            .getSeconds();

            if (startTime != null) {
                url += "&start=" + transferTimeToOkexStandard(startTime);
            }
            if (endTime != null) {
                url += "&end=" + transferTimeToOkexStandard(endTime);
            }
            String jsonResponse = com.wealth.fly.common.HttpClientUtil.get(url);
            if (StringUtils.isEmpty(jsonResponse)) {
                throw new RuntimeException("获取k线数据失败");
            }
            JSONArray jsonArray = JSONObject.parseArray(jsonResponse);
            List<KLine> kLineList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray data = (JSONArray) jsonArray.get(i);
                Long dateTime = new Long(transferTimeToSystemStandard((String) data.get(0)));

                KLine kLine = new KLine();
                kLine.setDataTime(dateTime);
                kLine.setOpen(new BigDecimal((String) data.get(1)));
                kLine.setHigh(new BigDecimal((String) data.get(2)));
                kLine.setLow(new BigDecimal((String) data.get(3)));
                kLine.setClose(new BigDecimal((String) data.get(4)));
                kLine.setVolume(Long.parseLong((String) data.get(5)));
                kLine.setCurrencyVolume(new BigDecimal((String) data.get(4)));
                kLine.setGranularity(dataGranularity.name());
                kLineList.add(kLine);
            }
            return kLineList;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("获取k线数据失败");
        }

    }

    private static String transferTimeToSystemStandard(String time) {
        Date date = null;
        try {
            date = new SimpleDateFormat(CommonConstants.OK_DATE_FORMAT).parse(time);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        date = DateUtils.addHours(date, 8);
        return new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(date);
    }

    private static String transferTimeToOkexStandard(Date time) {
        return new SimpleDateFormat(CommonConstants.OK_DATE_FORMAT).format(DateUtils.addHours(time, -8));
    }
}
