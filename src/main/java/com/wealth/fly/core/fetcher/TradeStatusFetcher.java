package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.constants.OkexAlgoOrderState;
import com.wealth.fly.core.constants.OrderStatus;
import com.wealth.fly.core.constants.TradeStatus;
import com.wealth.fly.core.dao.TradeDao;
import com.wealth.fly.core.entity.Trade;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.listener.TradeStatusChangeListener;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Component
@Slf4j
public class TradeStatusFetcher extends QuartzJobBean {
    @Resource
    private TradeDao tradeDao;

    private static List<TradeStatusChangeListener> tradeStatusChangeListeners = new ArrayList<>();

    public void registerTradeStatusChangeListener(TradeStatusChangeListener listener) {
        tradeStatusChangeListeners.add(listener);
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            detectPendingTrade();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            detectActiveTrade();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void detectActiveTrade() {
        List<Trade> tradeList = tradeDao.listByStatus(Collections.singletonList(TradeStatus.OPENED.getCode()));
        if (CollectionUtils.isEmpty(tradeList)) {
            return;
        }

        for (Trade trade : tradeList) {
            try {
                handleActiveTrade(trade);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void handleActiveTrade(Trade trade) {
        Order algoOrder = null;
        Order closeOrder = null;
        try {
            Exchanger exchanger = ExchangerManager.getExchangerByGoldForkStrategy(trade.getStrategy());
            algoOrder = exchanger.getAlgoOrderByCustomerId(trade.getAlgoOrderId());
            if (!StringUtils.isEmpty(algoOrder.getOrdId()) && !"0".equals(algoOrder.getOrdId())) {
                closeOrder = exchanger.getOrder(trade.getInstId(), algoOrder.getOrdId());
            }
        } catch (IOException e) {
            log.error("查策略委托单信息报错 " + e.getMessage(), e);
            return;
        }

        if (OkexAlgoOrderState.LIVE.equals(algoOrder.getState()) || OkexAlgoOrderState.PARTIALLY_EFFECTIVE.equals(algoOrder.getState())) {
            return;
        } else if (OkexAlgoOrderState.EFFECTIVE.equals(algoOrder.getState())) {
            if (closeOrder != null && OrderStatus.FILLED.equals(closeOrder.getState())) {
                for (TradeStatusChangeListener listener : tradeStatusChangeListeners) {
                    listener.onClose(trade, algoOrder, closeOrder);
                }
            }
        } else if (OkexAlgoOrderState.CANCELED.equals(algoOrder.getState())) {
            log.info("止盈止损委托单已取消,交易id:{}", trade.getId());
//            for (TradeStatusChangeListener listener : tradeStatusChangeListeners) {
//                listener.onCancel(trade);
//            }
        } else {
            //TODO告警
            throw new RuntimeException("发现非计划内策略委托单状态" + algoOrder.getState() + ",委托单id:" + trade.getAlgoOrderId() + ",交易id:" + trade.getId());
        }
    }

    private void detectPendingTrade() {
        List<Trade> tradeList = tradeDao.listByStatus(Collections.singletonList(TradeStatus.PENDING.getCode()));
        if (CollectionUtils.isEmpty(tradeList)) {
            return;
        }
        for (Trade trade : tradeList) {
            Exchanger exchanger = ExchangerManager.getExchangerByGoldForkStrategy(trade.getStrategy());

            Order order = null;
            try {
                order = exchanger.getOrder(trade.getInstId(), trade.getOpenOrderId());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return;
            }

            if (OrderStatus.CANCELED.equals(order.getState())) {
                for (TradeStatusChangeListener listener : tradeStatusChangeListeners) {
                    try {
                        listener.onCancel(trade);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

            if (OrderStatus.FILLED.equals(order.getState())) {
                for (TradeStatusChangeListener listener : tradeStatusChangeListeners) {
                    try {
                        listener.onOpen(trade, order);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
