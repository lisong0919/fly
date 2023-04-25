package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.listener.MarkPriceListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Component
@Slf4j
public class MarkPriceFetcher {

    @Resource
    private Exchanger exchanger;

    private List<MarkPriceListener> markPriceListeners = new ArrayList<>();

    @Value("${grid.inst.id}")
    private String instId;

    @PostConstruct
    public void init() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //TODO 动态算
                MarkPrice markPrice = null;
                try {
                    markPrice = exchanger.getMarkPriceByInstId(instId);
                } catch (Exception e) {
                    log.error("获取标记价格出错, detailMsg: " + e.getMessage(), e);
                }

                for (MarkPriceListener listener : markPriceListeners) {
                    try {
                        listener.onNewMarkPrice(markPrice);
                    } catch (Exception e) {
                        log.error("MarkPrice监听器处理出错,detailMsg:" + e.getMessage(), e);
                    }
                }
            }
        }, 3000L, 3000L);

        log.info("init mark price data fetcher timer finished.");
    }

    public void registerListener(MarkPriceListener listener) {
        markPriceListeners.add(listener);
    }


}