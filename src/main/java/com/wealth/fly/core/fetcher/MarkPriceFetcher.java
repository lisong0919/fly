package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.Monitor;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.model.GridStrategy;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.listener.MarkPriceListener;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Component
@Slf4j
public class MarkPriceFetcher extends QuartzJobBean {

    @Resource
    private ConfigService configService;

    private static List<MarkPriceListener> markPriceListeners = new ArrayList<>();

    public void registerListener(MarkPriceListener listener) {
        markPriceListeners.add(listener);
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (Monitor.stopAll) {
            log.debug(">>>>> stopAll");
            return;
        }

        List<Integer> activeGridStrategies = configService.getActiveGridStrategies();

        for (Integer activeGridStrategy : activeGridStrategies) {
            try {
                MarkPrice markPrice = null;
                GridStrategy strategy = configService.getGridStrategy(activeGridStrategy);
                Exchanger exchanger = ExchangerManager.getExchangerByGridStrategy(activeGridStrategy);

                try {
                    markPrice = exchanger.getMarkPriceByInstId(strategy.getInstId());
                } catch (Exception e) {
                    log.error("获取标记价格出错, detailMsg: " + e.getMessage(), e);
                }

                for (MarkPriceListener listener : markPriceListeners) {
                    try {
                        listener.onNewMarkPrice(markPrice, strategy);
                    } catch (Exception e) {
                        log.error("MarkPrice监听器处理出错,detailMsg:" + e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        Monitor.markPriceLastFetchTime = new Date();
    }
}
