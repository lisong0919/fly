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
import com.wealth.fly.core.fetcher.GridStatusFetcher;
import com.wealth.fly.core.listener.GridStatusChangeListener;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.fetcher.MarkPriceFetcher;
import com.wealth.fly.core.listener.MarkPriceListener;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private GridStatusFetcher gridStatusFetcher;
    @Resource
    private GridDao gridDao;
    @Resource
    private GridHistoryDao gridHistoryDao;
    @Resource
    private GridLogDao gridLogDao;
    @Resource
    private Exchanger exchanger;

    @Value("${min.force.close.price.eth}")
    private String minForceClosePriceForETH;

    @Value("${grid.default.strategy}")
    private Integer gridStrategy;

    @PostConstruct
    public void init() {
        markPriceFetcher.registerListener(this);
        gridStatusFetcher.registerGridStatusChangeListener(this);
    }

    @Override
    public void onNewMarkPrice(MarkPrice markPrice) {
        try {
            BigDecimal currentForceClosePrice = exchanger.getForceClosePrice(markPrice.getInstId());
            if (currentForceClosePrice.compareTo(new BigDecimal(minForceClosePriceForETH)) >= 0) {
                log.error("持仓强平价格{}大于{}，不能继续开仓", currentForceClosePrice, minForceClosePriceForETH);
                return;
            }
        } catch (IOException e) {
            log.error("查询强平价格报错" + e.getMessage(), e);
            return;
        }

        //先查出比当前价格低的网格
        List<Grid> gridList = gridDao.listGrids(gridStrategy, new BigDecimal(markPrice.getMarkPx()), 3);
        if (!CollectionUtils.isEmpty(gridList)) {
            gridList = gridList.stream()
                    .filter(g -> g.getStatus() == GridStatus.IDLE.getCode().intValue())
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(gridList)) {
            log.debug("无合适网格 {} ", markPrice.getInstId());
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
                log.info("[{}-{}-{}]网格委托下单成功", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onActive(Grid grid, Order buyOrder) {
        log.info("[{}-{}-{}]网格挂单已成交,网格已被激活", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
        //下止盈策略单
        Order order = Order.builder()
                .instId(grid.getInstId())
                .tdMode("cross")
                .side("sell")
                .posSide("long")
                .ordType("conditional")
                .sz(grid.getNum())
                .tag("" + grid.getId())
                .tpTriggerPx(grid.getSellPrice())
                .tpTriggerPxType("mark")
                .tpOrdPx(grid.getSellPrice())
                .build();
        String algoId = null;
        try {
            algoId = exchanger.createAlgoOrder(order);
        } catch (IOException e) {
            log.error("止盈委托下单失败 " + e.getMessage(), e);
            return;
        }

        GridHistory gridHistory = GridHistory.builder()
                .gridId(grid.getId())
                .instId(buyOrder.getInstId())
                .buyOrderId(grid.getBuyOrderId())
                .num(buyOrder.getSz())
                .buyPrice(buyOrder.getAvgPx())
                .buyFee(buyOrder.getFee())
                .feeCcy(buyOrder.getFeeCcy())
                .pendingTime(new Date())
                .buyTime(new Date(buyOrder.getFillTime()))
                .build();
        //每张合约单价值10U
        BigDecimal usdtAmount = new BigDecimal(10).multiply(new BigDecimal(buyOrder.getSz()));
        usdtAmount = usdtAmount.setScale(2, RoundingMode.FLOOR);
        gridHistory.setUsdtAmount(usdtAmount.toPlainString());
        gridHistory.setCurrencyAmount(usdtAmount.divide(new BigDecimal(buyOrder.getAvgPx()), 6, RoundingMode.FLOOR).toPlainString());
        gridHistoryDao.save(gridHistory);

        //下单成功后才更新状态和策略单id
        gridDao.updateGridActive(grid.getId(), algoId, gridHistory.getId());

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
        log.info("[{}-{}-{}]收到网格已完成通知", grid.getBuyPrice(), grid.getSellPrice(), sellOrder.getSz());
        gridDao.updateGridFinished(grid.getId());

        GridHistory gridHistory = GridHistory.builder()
                .id(grid.getGridHistoryId())
                .sellPrice(sellOrder.getAvgPx())
                .algoOrderId(algoOrder.getOrdId())
                .sellOrderId(sellOrder.getOrdId())
                .orderProfit(sellOrder.getPnl())
                .sellTime(new Date(sellOrder.getFillTime()))
                .build();

        //计算网格收益
        GridHistory existGridHistory = gridHistoryDao.getById(grid.getGridHistoryId());
        BigDecimal sellPrice = new BigDecimal(sellOrder.getAvgPx());
        BigDecimal buyPrice = new BigDecimal(existGridHistory.getBuyPrice());
        BigDecimal gridProfitPercent = sellPrice.subtract(buyPrice).divide(buyPrice, 6, RoundingMode.FLOOR);

        gridHistory.setGridProfitPercent(gridProfitPercent.toPlainString());
        gridHistory.setGridProfit(new BigDecimal(existGridHistory.getCurrencyAmount()).multiply(gridProfitPercent).setScale(6, RoundingMode.HALF_UP).toPlainString());

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
