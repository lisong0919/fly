package com.wealth.fly.core.fetcher;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.MACDHandler;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;

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
import org.springframework.core.env.Environment;
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

    @Value("${fetcher.inst}")
    private String instIds;

    @Resource
    private Environment env;

    private static List<KLineListener> kLineListenerList = new ArrayList<>();


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        fetchAll();
    }

    private void fetchAll() {
        List<DataGranularity> granularities = Arrays.asList(DataGranularity.FOUR_HOUR, DataGranularity.ONE_HOUR, DataGranularity.FIFTEEN_MINUTES);

        for (String instId : instIds.split(",")) {
            for (DataGranularity dataGranularity : granularities) {
                try {
                    fetch(instId, dataGranularity);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        }
    }


    public void registerKLineListener(KLineListener listener) {
        kLineListenerList.add(listener);
    }

    private void notifyKLineListenerNewLine(String instId, KLine kLine) {
        for (KLineListener listener : kLineListenerList) {
            try {
                listener.onNewKLine(instId, kLine);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private void fetch(String instId, DataGranularity dataGranularity) {
        List<KLine> lastKLines = kLineDao.getLastKLineByGranularity(instId, dataGranularity.name(), 1);

        if (lastKLines == null || lastKLines.isEmpty()) {
            log.error("数据库无初始化Kline，无法获取后续K线, instId:{},dataGranularity:{}", instId, dataGranularity);
            return;
        }

        Long fetchTimeStart = DateUtil.getNextKlineDataTime(lastKLines.get(0).getDataTime(), dataGranularity);
        Long fetchTimeEnd = fetchTimeStart;
        Long fetchTimeMax = DateUtil.getLatestKLineDataTime(new Date(), dataGranularity);

        if (lastKLines.get(0).getDataTime() == fetchTimeMax.longValue()) {
            log.debug("[{}] [{}] k线是最新数据", instId, dataGranularity);
            return;
        }

        for (int i = 0; i < 99; i++) {
            fetchTimeEnd = DateUtil.getNextKlineDataTime(fetchTimeEnd, dataGranularity);
            if (fetchTimeEnd >= fetchTimeMax) {
                //一次最多取100条
                break;
            }
        }

        log.info("[{}] [{}] start to fetch kline data from {} to {}", instId, dataGranularity, fetchTimeStart, fetchTimeEnd);

        //取数据的起始时间未设置的情况下，取回所有能取的数据
        List<KLine> kLineList = null;
        Date fetchStart = DateUtil.parseStandardTime(fetchTimeStart);

        //起始时间非今天，取历史数据
        if (DateUtils.isSameDay(fetchStart, new Date())) {
            kLineList = ExchangerManager.getExchangerByAccountId(defaultAccount)
                    .getKlineData(instId, fetchStart, DateUtil.parseStandardTime(fetchTimeEnd), dataGranularity);
        } else {
            kLineList = ExchangerManager.getExchangerByAccountId(defaultAccount)
                    .getHistoryKlineData(instId, fetchStart, DateUtil.parseStandardTime(fetchTimeEnd), dataGranularity);
        }

        afterFetch(instId, dataGranularity, kLineList, lastKLines);
        if (fetchTimeEnd < fetchTimeMax) {
            fetch(instId, dataGranularity);
        }

        log.debug("[{}] [{}] fetch kline data from exchanger success.", instId, dataGranularity);
    }

    private void afterFetch(String instId, DataGranularity dataGranularity, List<KLine> newKLineList, List<KLine> lastKLines) {
        if (CollectionUtils.isNotEmpty(newKLineList)) {
            newKLineList.sort((o1, o2) -> {
                if (o1.getDataTime().equals(o2.getDataTime())) {
                    return 0;
                }
                return o1.getDataTime() - o2.getDataTime() > 0 ? 1 : -1;
            });
        }

        for (KLine newKLine : newKLineList) {
            newKLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
            newKLine.setCurrencyId(1);
            newKLine.setInstId(instId);

            if (newKLine.getDataTime() > lastKLines.get(0).getDataTime()) {
                macdHandler.setMACD(newKLine);
                kLineDao.insert(newKLine);
                notifyKLineListenerNewLine(instId, newKLine);
            }
        }
        if (CollectionUtils.isNotEmpty(newKLineList)) {
            log.info("[{}] save kline data to db success,", dataGranularity);
        }
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


}
