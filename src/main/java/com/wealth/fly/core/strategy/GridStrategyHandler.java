package com.wealth.fly.core.strategy;

import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.GridLogType;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.GridHistoryDao;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.GridHistory;
import com.wealth.fly.core.entity.GridLog;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exception.InsufficientBalanceException;
import com.wealth.fly.core.exception.TPCannotLowerThanMPException;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.fetcher.GridStatusFetcher;
import com.wealth.fly.core.listener.GridStatusChangeListener;
import com.wealth.fly.core.model.GridStrategy;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.fetcher.MarkPriceFetcher;
import com.wealth.fly.core.listener.MarkPriceListener;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
    private KLineDao kLineDao;
    @Resource
    private ConfigService configService;


    private volatile boolean createOrderLock = false;


    @PostConstruct
    public void init() {
        markPriceFetcher.registerListener(this);
        gridStatusFetcher.registerGridStatusChangeListener(this);
    }

    @Override
    public void onNewMarkPrice(MarkPrice markPrice) {
        List<GridStrategy> gridStrategies = configService.getActiveGridStrategies();
        if (CollectionUtils.isEmpty(gridStrategies)) {
            return;
        }

        // 过滤出markPrice对应产品的策略
        gridStrategies = gridStrategies.stream().filter(s -> s.getInstId().equals(markPrice.getInstId())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(gridStrategies)) {
            return;
        }

        for (GridStrategy gridStrategy : gridStrategies) {
            try {
                onNewMarkPrice(markPrice, gridStrategy);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void onNewMarkPrice(MarkPrice markPrice, GridStrategy strategy) {
        Exchanger exchanger = ExchangerManager.getExchangerByAccountId(strategy.getAccount());

        try {
            BigDecimal currentForceClosePrice = exchanger.getForceClosePrice(markPrice.getInstId());
            if (currentForceClosePrice != null
                    && currentForceClosePrice.compareTo(new BigDecimal(strategy.getMinForceClosePrice())) >= 0
                    && currentForceClosePrice.compareTo(new BigDecimal(markPrice.getMarkPx())) < 0
            ) {
                log.error("持仓强平价格{}大于{}，不能继续开仓", currentForceClosePrice, strategy.getMinForceClosePrice());
                return;
            }
        } catch (IOException e) {
            log.error("查询强平价格报错" + e.getMessage(), e);
            return;
        }

        if (!isMACDFilterPass(strategy)) {
            // 如果1小时MACD未通过，则市价全平止损
            if (!isMACDFilterPass(new Date(), DataGranularity.ONE_HOUR, strategy, true)) {
                closeAll(exchanger, strategy.getInstId());
            }
            return;
        }

        try {
            if (createOrderLock) {
                log.info("下单加锁失败，有任务正在下单");
                return;
            }
            createOrderLock = true;
            createOrder(markPrice, strategy);
        } finally {
            createOrderLock = false;
        }
    }

    private void closeAll(Exchanger exchanger, String instId) {
        BigDecimal availPos = null;
        try {
            availPos = exchanger.getAvailPos(instId);
        } catch (IOException e) {
            log.info("获取可平仓数量失败 {}", instId);
            log.error(e.getMessage(), e);
            return;
        }
        if (availPos == null || availPos.intValue() <= 0) {
            return;
        }
        Order order = new Order();
        order.setInstId(instId);
        order.setTdMode("cross");
        order.setSide("sell");
        order.setPosSide("long");
        order.setOrdType("market");
        order.setSz(String.valueOf(availPos.longValue()));

        try {
            exchanger.createOrder(order);
            log.info("平仓止损下单成功 {}", order.getInstId());
        } catch (IOException e) {
            log.info("平仓失败 {}", JsonUtil.toJSONString(order));
            log.error(e.getMessage(), e);
        }
    }


    private boolean isMACDFilterPass(GridStrategy strategy) {
        Date now = new Date();
        return isMACDFilterPass(now, DataGranularity.FIFTEEN_MINUTES, strategy, true)
                && isMACDFilterPass(now, DataGranularity.ONE_HOUR, strategy, true)
                && isMACDFilterPass(now, DataGranularity.FOUR_HOUR, strategy, false);
    }

    private boolean isMACDFilterPass(Date now, DataGranularity dataGranularity, GridStrategy strategy, boolean isStrict) {
        Long preDataTime = DateUtil.getLatestKLineDataTime(now, dataGranularity);
        Long prePreDataTime = DateUtil.getPreKLineDataTime(preDataTime, dataGranularity);

        KLine prePreKline = kLineDao.getKlineByDataTime(strategy.getWatchInstId(), dataGranularity.name(), prePreDataTime);
        KLine preKline = kLineDao.getKlineByDataTime(strategy.getWatchInstId(), dataGranularity.name(), preDataTime);
        if (preKline == null) {
            log.info("[{}-{}] k线不存在，macd滤网不通过 {}", strategy.getId(), strategy.getInstId(), preDataTime);
            return false;
        }
        if (prePreKline == null) {
            log.info("[{}-{}] k线不存在，macd滤网不通过 {}", strategy.getId(), strategy.getInstId(), prePreDataTime);
            return false;
        }
        if (isStrict && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("[{}-{}] macd递减趋势，滤网不通过 {} {}-{}", strategy.getId(), strategy.getInstId(), dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        //非严格模式下，MACD小于零且递减才不通过
        if (!isStrict && preKline.getMacd().compareTo(new BigDecimal("0")) < 0
                && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("[{}-{}] macd递减趋势，滤网不通过 {} {}-{}", strategy.getId(), strategy.getInstId(), dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        return true;
    }

    private void createOrder(MarkPrice markPrice, GridStrategy strategy) {
        //先查出比当前价格低的网格
        List<Grid> gridList = gridDao.listGrids(strategy.getId(), new BigDecimal(markPrice.getMarkPx()), 1);
        if (!CollectionUtils.isEmpty(gridList)) {
            gridList = gridList.stream()
                    .filter(g -> g.getStatus() == GridStatus.IDLE.getCode().intValue())
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(gridList)) {
            log.debug("无合适网格 {} ", markPrice.getInstId());
            return;
        }
        Exchanger exchanger = ExchangerManager.getExchangerByAccountId(strategy.getAccount());
        for (Grid grid : gridList) {
            try {
                String customerOrderId = UUID.randomUUID().toString().replaceAll("-", "");
                //下单
                Order order = Order.builder()
                        .instId(markPrice.getInstId())
                        .tdMode("cross")
                        .side("buy")
                        .posSide("long")
                        .ordType("limit")
                        .sz(grid.getNum()) //TODO 根据保证金计算
                        .px(grid.getBuyPrice())
                        .clOrdId(customerOrderId)
                        .tag(String.valueOf(grid.getId()))
                        .build();
                String orderId = null;
                try {
                    orderId = exchanger.createOrder(order);
                } catch (InsufficientBalanceException e) {
                    log.debug("[{}-{}-{}-{}]余额不足，无法下单", strategy.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
                    return;
                } catch (Exception e) {
                    log.error("下单出错 " + e.getMessage(), e);

                    //有些特殊情况，下单报错，但是可能由于read timeout等原因，实际交易所订单成功
                    order = exchanger.getOrderByCustomerOrderId(grid.getInstId(), customerOrderId);
                    if (order != null && StringUtils.isNotBlank(order.getOrdId())) {
                        log.info("[{}-{}-{}-{}]下单出错，但实际交易所订单成功，订单id:{}", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum(), orderId);
                        orderId = order.getOrdId();
                    } else {
                        continue;
                    }
                }

                //更新网格订单id
                gridDao.updateOrderId(grid.getId(), orderId);
                gridDao.updateGridStatus(grid.getId(), GridStatus.PENDING.getCode());

                //记日志
                GridLog gridLog = GridLog.builder()
                        .gridId(grid.getId())
                        .strategyId(grid.getStrategy())
                        .type(GridLogType.CREATE_PENDING_ORDER.getCode())
                        .message(String.format("[%s-%s-%s]网格委托下单成功", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum()))
                        .build();
                gridLogDao.save(gridLog);
                log.info("[{}-{}-{}-{}]网格委托下单成功", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onActive(Grid grid, Order buyOrder) {
        log.info("[{}-{}-{}-{}]网格挂单已成交,网格已被激活", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
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
        boolean closeDirectly = false;
        Exchanger exchanger = ExchangerManager.getExchangerByGridStrategy(grid.getStrategy());

        try {
            algoId = exchanger.createAlgoOrder(order);
        } catch (TPCannotLowerThanMPException e) {
            log.error("委托单的止盈点低于现价，可能是价格波动太大, detailMsg:" + e.getMessage(), e);
            //价格波动太大的情况直接市价平仓
            try {
                closeLongOrder(grid);
                log.info("[{}-{}-{}-{}]委托单的止盈点低于现价，可能是价格波动太大，直接市价平仓", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
                closeDirectly = true;
            } catch (IOException ioException) {
                log.error("市价平仓失败 " + e.getMessage(), e);
                return;
            }
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
        if (closeDirectly) {
            gridDao.updateGridFinished(grid.getId());
            String message = String.format("[%s-%s-%s]委托单的止盈点低于现价，直接市价平仓", grid.getBuyPrice(), grid.getSellPrice(), buyOrder.getSz());
            saveLog(GridLogType.GRID_FINISHED_PROFIT, grid, message, gridHistory.getId());
        } else {
            gridDao.updateGridActive(grid.getId(), algoId, gridHistory.getId());
            String message = String.format("[%s-%s-%s]委托单成交，网格被激活，止盈策略单已创建", grid.getBuyPrice(), grid.getSellPrice(), buyOrder.getSz());
            saveLog(GridLogType.GRID_ACTIVE, grid, message, gridHistory.getId());
        }
    }


    @Override
    public void onFinished(Grid grid, Order algoOrder, Order sellOrder) {
        log.info("[{}-{}-{}-{}]收到网格已完成通知", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), sellOrder.getSz());
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
                .strategyId(grid.getStrategy())
                .gridHistoryId(gridHistory.getId())
                .message(String.format("[%s-%s-%s]网格已止盈:%s", grid.getBuyPrice(), grid.getSellPrice(), sellOrder.getSz(), sellOrder.getPnl()))
                .build();
        gridLogDao.save(gridLog);
    }

    @Override
    public void onCancel(Grid grid) {
        log.info("[{}-{}-{}-{}]收到网格委托买单撤销通知", grid.getInstId(), grid.getBuyPrice(), grid.getSellPrice(), grid.getNum());
        gridDao.updateGridFinished(grid.getId());

        GridLog gridLog = GridLog.builder()
                .type(GridLogType.GRID_BUY_ORDER_CANCEL.getCode())
                .gridId(grid.getId())
                .strategyId(grid.getStrategy())
                .message(String.format("[%s-%s-%s]网格委托买单撤销", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum()))
                .build();
        gridLogDao.save(gridLog);
    }


    private void closeLongOrder(Grid grid) throws IOException {
        Exchanger exchanger = ExchangerManager.getExchangerByGridStrategy(grid.getStrategy());
        Order order = Order.builder()
                .instId(grid.getInstId())
                .tdMode("cross")
                .side("sell")
                .posSide("long")
                .ordType("market")
                .sz(grid.getNum())
                .px(grid.getBuyPrice())
                .tag(String.valueOf(grid.getId()))
                .build();
        exchanger.createOrder(order);
    }

    private void saveLog(GridLogType gridLogType, Grid grid, String logMessage, Long historyId) {
        GridLog gridLog = GridLog.builder()
                .type(gridLogType.getCode())
                .gridId(grid.getId())
                .strategyId(grid.getStrategy())
                .gridHistoryId(historyId)
                .message(logMessage)
                .build();
        gridLogDao.save(gridLog);
    }
}
