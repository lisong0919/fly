package com.wealth.fly.core.exchanger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CryptoCompareExchanger implements Exchanger {

    private static final String URL_PREFIX = "https://min-api.cryptocompare.com/data/v2/histohour";
    private static final String API_KEY = "71da9cef22ecae7aac2f7511c426f043ad01dc1760139a4525148d38bc872c4a";

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoCompareExchanger.class);

    public List<KLine> getKlineData(Date endTime, int limit) {
        try {
            String responseStr = HttpClientUtil.get(URL_PREFIX + "?fsym=BTC&tsym=USD&limit=" + limit + "&api_key=" + API_KEY + "&toTs=" + (endTime.getTime() / 1000));

            /*
             *
             * For example, for the BTC-USD pair  “Volume From” is the number of Bitcoins traded for US dollars
             * while “Volume To” is the number of dollars traded (for the period) for Bitcoins.
             *
             * */
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            JSONArray jsonArray = jsonObject.getJSONObject("Data").getJSONArray("Data");
            List<KLine> kLineList = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                long timestamp = item.getLong("time") * 1000;

                KLine kLine = new KLine();
                kLine.setDataTime(Long.parseLong(DateUtil.formatToStandardTime(timestamp)));
                kLine.setOpen(item.getBigDecimal("open"));
                kLine.setHigh(item.getBigDecimal("high"));
                kLine.setLow(item.getBigDecimal("low"));
                kLine.setClose(item.getBigDecimal("close"));
                kLine.setVolume(item.getBigDecimal("volumeto"));
                kLine.setCurrencyVolume(item.getBigDecimal("volumefrom"));
                kLine.setGranularity(DataGranularity.ONE_HOUR.name());
                kLine.setEma26(new BigDecimal(1));
                kLine.setEma12(new BigDecimal(1));
                kLine.setDea9(new BigDecimal(1));
                kLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
                kLine.setCurrencyId(1);
                kLineList.add(kLine);
            }

            return kLineList;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("获取k线数据失败");
        }

    }


    @Override
    public List<KLine> getKlineData(String currency, Date startTime, Date endTime, DataGranularity dataGranularity) {
        return null;
    }
}
