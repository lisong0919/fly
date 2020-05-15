package com.wealth.fly.core;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.Exchanger;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataFetcher {

    @Autowired
    private KLineDao kLineDao;
    @Autowired
    private Exchanger exchanger;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFetcher.class);

    @PostConstruct
    public void init() {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                for (DataGranularity dataGranularity : DataGranularity.values()) {

                    KLine lastKLine = kLineDao.getLastKLineByGranularity(dataGranularity.name());

                    Date fetchMinTime = null;
                    Date fetchMaxTime = null;

                    if (lastKLine != null) {
                        Date now = Calendar.getInstance(Locale.CHINA).getTime();
                        Date lastLineDate = DateUtil.parseStandardTime(lastKLine.getDataTime());
                        //最后一条数据的时间，距离当前时间，是否超过数据粒度对应的时间间隔
                        if (now.getTime() - lastLineDate.getTime() >= dataGranularity.getSeconds() * 1000) {
                            fetchMinTime = lastLineDate;
                            fetchMaxTime = now;
                        } else {
                            LOGGER.info("[{}] data is uptodate", dataGranularity);
                            continue;
                        }
                    } else {
                        LOGGER.info("[{}] no data in db,fetch all.", dataGranularity);
                    }

                    //取数据的起始时间未设置的情况下，取回所有能取的数据
                    List<KLine> kLineList = exchanger
                            .getKlineData("BTC-USDT-SWAP", fetchMinTime, fetchMaxTime, dataGranularity);

                    LOGGER.info("[{}] fetch kline data from exchanger success.", dataGranularity);
                    for (KLine kLine : kLineList) {
                        kLine.setCreateTime(new Date());
                        kLine.setCurrencyId(1);
                        kLineDao.insert(kLine);
                    }
                    LOGGER.info("[{}] save kline data to db success,", dataGranularity);
                }

            }
        }, 10000L, 60000L);

        LOGGER.info("init data fetcher timer finished.");
    }

    public static void main(String[] args) throws Exception {

        System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+08").format(new Date()));
    }
}
