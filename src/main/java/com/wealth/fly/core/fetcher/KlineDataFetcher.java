package com.wealth.fly.core.fetcher;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.MACDHandler;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;

import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.listener.KLineListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KlineDataFetcher extends QuartzJobBean {

    @Resource
    private KLineDao kLineDao;
    @Resource
    private MACDHandler macdHandler;

    @Value("${okex.account.default}")
    private String defaultAccount;

    private static List<KLineListener> kLineListenerList = new ArrayList<>();


    public static void registerKLineListener(KLineListener listener) {
        kLineListenerList.add(listener);
    }

    private void notifyKLineListenerNewLine(KLine kLine) {
        for (KLineListener listener : kLineListenerList) {
            listener.onNewKLine(kLine);
        }
    }


    private void fetch(DataGranularity dataGranularity) throws ParseException {
        List<KLine> lastKLines = kLineDao.getLastKLineByGranularity(dataGranularity.name(), 1);

        Date fetchMinTime = null;
        Date fetchMaxTime = null;

        if (lastKLines != null && lastKLines.size() > 0) {
            Date now = Calendar.getInstance(Locale.CHINA).getTime();
//            Date lastLineDate = DateUtil.parseStandardTime(lastKLines.get(0).getDataTime());

            Date[] timeRange = getDateFetchRang(lastKLines.get(0).getDataTime(), now, dataGranularity);
            //最后一条数据的时间，距离当前时间，是否超过数据粒度对应的时间间隔
            if (timeRange != null) {
                fetchMinTime = timeRange[0];
                fetchMaxTime = timeRange[1];
            } else {
                log.debug("[{}] data is uptodate", dataGranularity);
                return;
            }
        } else {
            log.info("[{}] no data in db,fetch all.", dataGranularity);
        }

        log.debug("[{}] start to fetch kline data from {} to {}", dataGranularity, fetchMinTime, fetchMaxTime);
        //取数据的起始时间未设置的情况下，取回所有能取的数据
        List<KLine> kLineList = ExchangerManager.getExchangerByAccountId(defaultAccount)
                .getKlineData("ETH-USDT-SWAP", fetchMinTime, fetchMaxTime, dataGranularity);

        log.debug("[{}] fetch kline data from exchanger success.", dataGranularity);

        if (CollectionUtils.isNotEmpty(kLineList)) {
            kLineList.sort((o1, o2) -> {
                if (o1.getDataTime().equals(o2.getDataTime())) {
                    return 0;
                }
                return o1.getDataTime() - o2.getDataTime() > 0 ? 1 : -1;
            });
        }

        boolean isDBEmpty = lastKLines == null || lastKLines.isEmpty();
        for (KLine kLine : kLineList) {
            kLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
            kLine.setCurrencyId(1);

            if (isDBEmpty || kLine.getDataTime() > lastKLines.get(0).getDataTime()) {
                macdHandler.setMACD(kLine);
                kLineDao.insert(kLine);
            }

            if (!isDBEmpty && kLine.getDataTime() > lastKLines.get(0).getDataTime()) {
                notifyKLineListenerNewLine(kLine);
            }
        }
        if (CollectionUtils.isNotEmpty(kLineList)) {
            log.info("[{}] save kline data to db success,", dataGranularity);
        }
    }

    protected Date[] getDateFetchRang(Long lastLineTime, Date now, DataGranularity dataGranularity) {
        Date[] result = new Date[2];

        result[0] = DateUtils.addSeconds(DateUtil.parseStandardTime(lastLineTime), 1);
        result[1] = now;

        //不到最新数据的形成时间
        if (result[1].getTime() - result[0].getTime() < 0) {
            return null;
        }
        return result;
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

        result[0] = lastLineTime;

        //余数，如5分钟时间粒度的情况下：lastLineTime=14:15，now：14:37,相差22分钟；那么余数就是2分钟，即：2*60*1000
        long remainder = timeDistance % (dataGranularity.getSeconds() * 1000);
        //取数据的截止日期还要再往前推一个时间周期，只能是14:30，因为14:37分时，14:35的5分钟的k线数据还未完，要到14:40才能形成
        result[1] = DateUtils.addMilliseconds(now, -(int) (remainder + dataGranularity.getSeconds() * 1000));

        //不到最新数据的形成时间
        if (result[1].getTime() - result[0].getTime() <= 0) {
            return null;
        }

        return result;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        fetchAll();
    }

    private void fetchAll() {
        List<DataGranularity> granularities = Arrays.asList(DataGranularity.FIFTEEN_MINUTES, DataGranularity.ONE_HOUR, DataGranularity.FOUR_HOUR);

        for (DataGranularity dataGranularity : granularities) {
            try {
                fetch(dataGranularity);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
        }
    }
}
