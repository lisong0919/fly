package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.Monitor;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.constants.OkexAlgoOrderState;
import com.wealth.fly.core.constants.OkexOrderState;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.exception.CancelOrderAlreadyFinishedException;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.listener.GridStatusChangeListener;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
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

    @Value("${grid.default.strategy}")
    private Integer gridStrategy;

    @Value("${grid.inst.id}")
    private String instId;

    private final List<GridStatusChangeListener> statusChangeListeners = new ArrayList<>();

    @PostConstruct
    public void init() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    detectActiveGrid();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    detectPendingGrid();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                Monitor.gridStatusLastFetchTime = new Date();

                cancelUnnecessaryGrids();

            }
        }, 5000L, 5000L);

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
                gridFinished(grid);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void gridFinished(Grid grid) {
        if (StringUtils.isEmpty(grid.getAlgoOrderId())) {
            //TODO 告警
            log.error("网格也挂单但无止盈策略单id,gridId:{}", grid.getId());
            return;
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
            return;
        }

        if (OkexAlgoOrderState.LIVE.equals(algoOrder.getState()) || OkexAlgoOrderState.PARTIALLY_EFFECTIVE.equals(algoOrder.getState())) {
            return;
        } else if (OkexAlgoOrderState.EFFECTIVE.equals(algoOrder.getState())) {
            if (sellOrder != null && OkexOrderState.FILLED.equals(sellOrder.getState())) {
                for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                    statusChangeListener.onFinished(grid, algoOrder, sellOrder);
                }
            }
        } else if (OkexAlgoOrderState.CANCELED.equals(algoOrder.getState())) {
            for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                statusChangeListener.onCancel(grid);
            }
        } else {
            //TODO告警
            throw new RuntimeException("发现非计划内策略委托单状态" + algoOrder.getState() + ",委托单id:" + grid.getAlgoOrderId() + ",网格id:" + grid.getId());
        }
    }

    /**
     * 探测已挂单网格是否已激活或撤销
     */
    private void detectPendingGrid() {
        List<Grid> gridList = gridDao.listByStatusOrderByBuyPrice(Collections.singletonList(GridStatus.PENDING.getCode()), 100);
        if (CollectionUtils.isEmpty(gridList)) {
            return;
        }

        for (Grid grid : gridList) {
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
            } else if (OkexOrderState.CANCELED.equals(order.getState())) {
                for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
                    statusChangeListener.onCancel(grid);
                }
            }
        }

        try {
            cancelUnnecessaryGrids();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 价格低的挂单全部取消掉，只保留一个价格比市价低一级的挂单，其他全部取消掉，以避免占用仓位
     */
    private void cancelUnnecessaryGrids() {
        List<Grid> gridList = gridDao.listByStatusOrderByBuyPrice(Collections.singletonList(GridStatus.PENDING.getCode()), 100);
        BigDecimal markPrice = null;
        try {
            MarkPrice markPriceObj = exchanger.getMarkPriceByInstId(instId);
            markPrice = new BigDecimal(markPriceObj.getMarkPx());
        } catch (IOException e) {
            log.error("调用查标记价格接口报错:" + e.getMessage(), e);
            return;
        }

        Grid maxPriceGrid = null;
        for (int i = 0; i < gridList.size(); i++) {
            try {
                Grid grid = gridList.get(i);
                if (StringUtils.isEmpty(grid.getBuyOrderId())) {
                    //TODO 告警
                    log.error("网格有挂单但无订单id,gridId:{}", grid.getId());
                    gridDao.updateGridFinished(grid.getId());
                    continue;
                }

                BigDecimal buyPrice = new BigDecimal(grid.getBuyPrice());
                //比市价高的
                if (buyPrice.compareTo(markPrice) > 0) {
                    continue;
                }
                if (maxPriceGrid == null) {
                    maxPriceGrid = grid;
                    continue;
                }
                if (buyPrice.compareTo(new BigDecimal(maxPriceGrid.getBuyPrice())) > 0) {
                    maxPriceGrid = grid;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        if (maxPriceGrid == null) {
            return;
        }

        //非目标网格全部撤销
        for (Grid grid : gridList) {
            try {
                //比市价高的挂单，理论上应该已经成交或部分成交，不能直接取消
                if (new BigDecimal(grid.getBuyPrice()).compareTo(markPrice) > 0) {
                    //TODO 告警
                    log.error("[{}-{}-{}]发现比buyPrice比市价高的网格仍然pending", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
                    continue;
                }
                if (grid.getId().intValue() != maxPriceGrid.getId().intValue() ||
                        new BigDecimal(grid.getSellPrice()).compareTo(markPrice) < 0) {
                    cancelPendingGrid(grid);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private void cancelPendingGrid(Grid grid) {
        try {
            exchanger.cancelOrder(instId, grid.getBuyOrderId());
        } catch (IOException e) {
            log.error("撤销订单出错 " + e.getMessage(), e);
            return;
        } catch (CancelOrderAlreadyFinishedException e) {
            log.error(e.getMessage(), e);
            gridDao.updateGridFinished(grid.getId());
            return;
        }
        for (GridStatusChangeListener statusChangeListener : statusChangeListeners) {
            statusChangeListener.onCancel(grid);
        }
    }
}
