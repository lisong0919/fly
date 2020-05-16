package com.wealth.fly.core;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.Exchanger;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateUtils;
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

                        LOGGER.info("=====>lastLineDate:" + lastLineDate + ",now:" + now);
                        Date[] timeRange = getDateFetchRang(lastLineDate, now, dataGranularity);
                        //最后一条数据的时间，距离当前时间，是否超过数据粒度对应的时间间隔
                        if (timeRange != null) {
                            fetchMinTime = timeRange[0];
                            fetchMaxTime = timeRange[1];
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
                        kLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
                        kLine.setCurrencyId(1);
                        kLineDao.insert(kLine);
                    }
                    LOGGER.info("[{}] save kline data to db success,", dataGranularity);
                }

            }
        }, 10000L, 60000L);

        LOGGER.info("init data fetcher timer finished.");
    }


    /**
     * 计算获取数据的时间范围
     *
     * @param lastLineTime
     * @param now
     * @param dataGranularity
     * @return
     */
    protected Date[] getDateFetchRang(Date lastLineTime, Date now, DataGranularity dataGranularity) {

        long timeDistance = now.getTime() - lastLineTime.getTime();
        Date[] result = new Date[2];

        result[0] = DateUtils.addSeconds(lastLineTime, dataGranularity.getSeconds());

        //余数，如5分钟时间粒度的情况下：lastLineTime=14:15，now：14:37,相差22分钟；那么余数就是2分钟，即：2*60*1000
        long remainder = timeDistance % (dataGranularity.getSeconds() * 1000);
        //取数据的截止日期还要再往前推一个时间周期，只能是14:30，因为14:37分时，14:35的5分钟的k线数据还未完，要到14:40才能形成
        result[1] = DateUtils.addMilliseconds(now, -(int) (remainder + dataGranularity.getSeconds() * 1000));

        //不到最新数据的形成时间
        if (result[1].getTime() - result[0].getTime() < 0) {
            return null;
        }

        return result;
    }


}
