package com.wealth.fly.core.strategy;

import com.wealth.fly.core.constants.GridLogType;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.GridHistoryDao;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.GridHistory;
import com.wealth.fly.core.entity.GridLog;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.listener.GridStatusChangeListener;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.fetcher.MarkPriceFetcher;
import com.wealth.fly.core.listener.MarkPriceListener;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Component
@Slf4j
public class GridStrategyHandler implements MarkPriceListener, GridStatusChangeListener {

    @Resource
    private MarkPriceFetcher markPriceFetcher;
    @Resource
    private GridDao gridDao;
    @Resource
    private GridHistoryDao gridHistoryDao;
    @Resource
    private GridLogDao gridLogDao;
    @Resource
    private Exchanger exchanger;

    @PostConstruct
    public void init() {
        markPriceFetcher.registerListener(this);
    }

    @Override
    public void onNewMarkPrice(MarkPrice markPrice) {
        //先查出比当前价格低的网格
        List<Grid> gridList = gridDao.listGrids(markPrice.getInstId(), GridStatus.IDLE.getCode(), new BigDecimal(markPrice.getMarkPx()), 3);
        if (CollectionUtils.isEmpty(gridList)) {
            log.error("无合适网格 {} ", markPrice.getInstId());
            return;
        }
        for (Grid grid : gridList) {
            try {
                //先更新状态为已委托，防止下单成功但数据库未更新成功，从而导致重复下单
                gridDao.updateGridStatus(grid.getId(), GridStatus.PENDING.getCode());

                //下单
                Order order = Order.builder()
                        .instId(markPrice.getInstId())
                        .tdMode("cross")
                        .side("buy")
                        .posSide("long")
                        .ordType("limit")
                        .sz(grid.getNum()) //TODO 根据保证金计算
                        .px(grid.getBuyPrice())
                        .tpTriggerPx(grid.getSellPrice())
                        .tpTriggerPxType("mark")
                        .tpOrdPx(grid.getSellPrice())
                        .tag(String.valueOf(grid.getId()))
                        .build();
                String orderId = null;
                try {
                    orderId = exchanger.createOrder(order);
                } catch (IOException e) {
                    log.error("下单出错 " + e.getMessage(), e);
                    gridDao.updateGridStatus(grid.getId(), GridStatus.IDLE.getCode());
                    continue;
                }

                //更新网格订单id
                gridDao.updateOrderId(grid.getId(), orderId);

                //记日志
                GridLog gridLog = GridLog.builder()
                        .gridId(grid.getId())
                        .type(GridLogType.CREATE_PENDING_ORDER.getCode())
                        .message(String.format("[%s-%s-%s]网格委托下单成功", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum()))
                        .build();
                gridLogDao.save(gridLog);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onActive(Grid grid, Order buyOrder) {
        //下止盈策略单
//        Order order = Order.builder()
//                .instId(grid.getInstId())
//                .tdMode("cross")
//                .side("sell")
//                .posSide("long")
//                .ordType("conditional")
//                .sz(grid.getNum())
//                .tag("" + grid.getId())
//                .tpTriggerPx(grid.getSellPrice())
//                .tpTriggerPxType("mark")
//                .tpOrdPx(grid.getSellPrice())
//                .build();
//        String algoId = null;
//        try {
//            algoId = exchanger.createAlgoOrder(order);
//        } catch (IOException e) {
//            log.error("止盈委托下单失败 " + e.getMessage(), e);
//            return;
//        }

        GridHistory gridHistory = GridHistory.builder()
                .gridId(grid.getId())
                .instId(buyOrder.getInstId())
                .buyOrderId(grid.getBuyOrderId())
                .num(buyOrder.getSz())
                .buyPrice(buyOrder.getAvgPx())
                .buyFee(buyOrder.getFee())
                .feeCcy(buyOrder.getFeeCcy())
                .pendingTime(new Date(buyOrder.getCTime()))
                .buyTime(new Date(buyOrder.getFillTime()))
                .build();
        BigDecimal money = new BigDecimal(buyOrder.getAvgPx()).multiply(new BigDecimal(buyOrder.getSz()));
        gridHistory.setMoney(money.toPlainString());
        gridHistoryDao.save(gridHistory);

        //下单成功后才更新状态和策略单id
        gridDao.updateGridActive(grid.getId(), buyOrder.getAlgoId(), gridHistory.getGridId());

        GridLog gridLog = GridLog.builder()
                .type(GridLogType.GRID_ACTIVE.getCode())
                .gridId(grid.getId())
                .gridHistoryId(gridHistory.getId())
                .message(String.format("[%s-%s-%s]委托单成交，网格被激活，止盈策略单已创建", grid.getBuyPrice(), grid.getSellPrice(), buyOrder.getSz()))
                .build();
        gridLogDao.save(gridLog);
    }

    @Override
    public void onFinished(Grid grid, Order algoOrder, Order sellOrder) {
        gridDao.updateGridFinished(grid.getId());

        GridHistory gridHistory = GridHistory.builder()
                .id(grid.getGridHistoryId())
                .sellPrice(sellOrder.getAvgPx())
                .algoOrderId(algoOrder.getOrdId())
                .sellOrderId(sellOrder.getOrdId())
                .orderProfit(sellOrder.getPnl())
                .gridProfitPercent(grid.getWeight())
                .sellTime(new Date(sellOrder.getFillTime()))
                .build();
        gridHistoryDao.updateById(gridHistory);


        GridLog gridLog = GridLog.builder()
                .type(GridLogType.GRID_FINISHED_PROFIT.getCode())
                .gridId(grid.getId())
                .gridHistoryId(gridHistory.getId())
                .message(String.format("[%s-%s-%s]网格已止盈:%s", grid.getBuyPrice(), grid.getSellPrice(), sellOrder.getSz(), sellOrder.getPnl()))
                .build();
        gridLogDao.save(gridLog);
    }

}
