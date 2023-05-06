package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.Monitor;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.constants.OkexAlgoOrderState;
import com.wealth.fly.core.constants.OkexOrderState;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.listener.GridStatusChangeListener;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
@Component
@Slf4j
public class GridStatusFetcher {
    @Resource
    private Exchanger exchanger;
    @Resource
    private GridDao gridDao;

    private final List<GridStatusChangeListener> statusChangeListeners = new ArrayList<>();

    @PostConstruct
    public void init() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Monitor.stopAll) {
                    log.info(">>>>> stopAll");
                    return;
                }
                detectPendingGrid();
                detectActiveGrid();
                Monitor.gridStatusLastFetchTime = new Date();
            }
        }, 6000L, 6000L);

        log.info("init mark price data fetcher timer finished.");
    }


    public void registerGridStatusChangeListener(GridStatusChangeListener listener) {
        statusChangeListeners.add(listener);
    }

    /**
     * 探测已激活网格是否已完成
     */
    private void detectActiveGrid() {
        //价格最低的几个未完成，价格高的肯定也是未完成
        List<Grid> gridList = gridDao.listByStatusOrderByBuyPrice(Collections.singletonList(GridStatus.ACTIVE.getCode()), 3);
        if (CollectionUtils.isEmpty(gridList)) {
            return;
        }
        for (Grid grid : gridList) {
            try {
                if (StringUtils.isEmpty(grid.getAlgoOrderId())) {
                    //TODO 告警
                    log.error("网格也挂单但无止盈策略单id,gridId:{}", grid.getId());
                    continue;
                }
                Order algoOrder = null;
                Order sellOrder = null;
                try {
                    algoOrder = exchanger.getAlgoOrder(grid.getAlgoOrderId());
                    if (!StringUtils.isEmpty(algoOrder.getOrdId()) && !"0".equals(algoOrder.getOrdId())) {
                        sellOrder = exchanger.getOrder(grid.getInstId(), algoOrder.getOrdId());
                    }
                } catch (IOException e) {
                    log.error("查策略委托单信息报错 " + e.getMessage(), e);
                    continue;
                }

                if (OkexAlgoOrderState.LIVE.equals(algoOrder.getState()) || OkexAlgoOrderState.PARTIALLY_EFFECTIVE.equals(algoOrder.getState())) {
                    continue;
                } else if (OkexAlgoOrderState.EFFECTIVE.equals(algoOrder.getState())) {
                    if (sellOrder != null && OkexOrderState.FILLED.equals(sellOrder.getState())) {
                        for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                            statusChangeListener.onFinished(grid, algoOrder, sellOrder);
                        }
                    }
                } else {
                    //TODO告警
                    throw new RuntimeException("发现非计划内策略委托单状态" + algoOrder.getState() + ",委托单id:" + grid.getAlgoOrderId() + ",网格id:" + grid.getId());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 探测已挂单网格是否已激活或撤销
     */
    private void detectPendingGrid() {
        //价格最低的几个未完成，价格高的肯定也是未完成
        List<Grid> gridList = gridDao.listByStatusOrderByBuyPrice(Collections.singletonList(GridStatus.PENDING.getCode()), 3);
        if (CollectionUtils.isEmpty(gridList)) {
            return;
        }
        for (Grid grid : gridList) {
            try {
                if (StringUtils.isEmpty(grid.getBuyOrderId())) {
                    //TODO 告警
                    log.error("网格有挂单但无订单id,gridId:{}", grid.getId());
                    continue;
                }
                Order order = null;
                try {
                    order = exchanger.getOrder(grid.getInstId(), grid.getBuyOrderId());
                } catch (IOException e) {
                    log.error("调用查订单接口报错 " + e.getMessage(), e);
                    continue;
                }

                if (OkexOrderState.FILLED.equals(order.getState())) {
                    for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                        statusChangeListener.onActive(grid, order);
                    }
                }else if(OkexOrderState.CANCELED.equals(order.getState())){
                    for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                        statusChangeListener.onCancel(grid, order);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
