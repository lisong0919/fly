package com.wealth.fly.core.strategy;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.TradeMode;
import com.wealth.fly.core.constants.TradeStatus;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.dao.TradeDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.Trade;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.fetcher.KlineDataFetcher;
import com.wealth.fly.core.fetcher.TradeStatusFetcher;
import com.wealth.fly.core.listener.KLineListener;
import com.wealth.fly.core.listener.TradeStatusChangeListener;
import com.wealth.fly.core.model.GoldForkStrategy;
import com.wealth.fly.core.model.MaxOpenSize;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Component
@Slf4j
public class GoldForkStrategyHandler implements KLineListener, TradeStatusChangeListener {
    @Resource
    private KLineDao kLineDao;
    @Resource
    private ConfigService configService;
    @Resource
    private TradeDao tradeDao;
    @Resource
    private KlineDataFetcher klineDataFetcher;
    @Resource
    private TradeStatusFetcher tradeStatusFetcher;
    @Resource
    private Environment env;


    @PostConstruct
    public void init() {
        klineDataFetcher.registerKLineListener(this);
        tradeStatusFetcher.registerTradeStatusChangeListener(this);
    }

    @Override
    public void onNewKLine(String instId, KLine kLine) {
        DataGranularity dataGranularity = DataGranularity.FIFTEEN_MINUTES;
        if (!dataGranularity.name().equals(kLine.getGranularity())) {
            return;
        }

        Date now = new Date();
        Long latestKLineDataTime = DateUtil.getLatestKLineDataTime(now, dataGranularity);
        KLine preKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), latestKLineDataTime);

        Long prePreDataTime = DateUtil.getPreKLineDataTime(latestKLineDataTime, dataGranularity);
        KLine prePreKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), prePreDataTime);


        BigDecimal zero = new BigDecimal("0");
        boolean isGoldFork = prePreKline.getMacd().compareTo(zero) < 0 && preKline.getMacd().compareTo(zero) > 0;
        if (!isGoldFork) {
            log.info("[{}] 非金叉", instId);
            return;
        }

        log.info("[{}] 检测到15分钟金叉{}-{}", instId, prePreDataTime, latestKLineDataTime);
        if (!isMACDFilterPass(instId, now, DataGranularity.ONE_HOUR, true) || !isMACDFilterPass(instId, now, DataGranularity.FOUR_HOUR, false)) {
            return;
        }
        log.info("[{}] 金叉滤网通过{}-{}", instId, prePreDataTime, latestKLineDataTime);

        List<GoldForkStrategy> strategyList = configService.getActiveGoldForkStrategies();
        if (strategyList == null) {
            log.info("[{}] 未配置gold-fork策略", instId);
            return;
        }

        strategyList = strategyList.stream().filter(s -> s.getWatchInstId().equals(instId)).collect(Collectors.toList());
        if (strategyList == null) {
            log.info("[{}] 未配置gold-fork策略", instId);
            return;
        }


        for (GoldForkStrategy strategy : strategyList) {
            try {
                if (tradeDao.getProcessingTrade(strategy.getId()) != null) {
                    log.info("[{}] [{}] 已有进行中交易，不开单", instId, strategy.getId());
                    continue;
                }
                createOrder(strategy, dataGranularity, kLine.getDataTime());
                log.info("[{}] [{}] gold-fork下单成功", instId, strategy.getId());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void createOrder(GoldForkStrategy strategy, DataGranularity dataGranularity, long kLineDataTime) throws IOException {
        Exchanger exchanger = ExchangerManager.getExchangerByAccountId(strategy.getAccount());

        String customerOrderId = UUID.randomUUID().toString().replaceAll("-", "");
        Order order = null;
        String orderId = null;


        //下单
        try {
            MaxOpenSize maxOpenSize = exchanger.getMaxOpenSize(strategy.getInstId(), TradeMode.cross);
            order = Order.builder()
                    .instId(strategy.getInstId())
                    .tdMode("cross")
                    .side("buy")
                    .posSide("long")
                    .ordType("limit")
                    .sz(maxOpenSize.getMaxBuy())
                    .px(getExpectOpenPrice(exchanger, strategy.getInstId(), dataGranularity, kLineDataTime).toPlainString())
//                    .px(exchanger.getMarkPriceByInstId(strategy.getInstId()).getMarkPx())
                    .clOrdId(customerOrderId)
                    .attachAlgoClOrdId(UUID.randomUUID().toString().replaceAll("-", ""))
                    .build();

            //设置止盈止损
            BigDecimal open = new BigDecimal(order.getPx());
            order.setTpTriggerPx(open.add(open.multiply(strategy.getProfitPercent())).toPlainString()); //止盈触发价
            order.setTpOrdPx(order.getTpTriggerPx());//止盈委托价
            order.setSlTriggerPx(open.subtract(open.multiply(strategy.getProfitPercent())).toPlainString()); //止损触发价
            order.setSlOrdPx("-1");//市价止损
            order.setTpTriggerPxType("mark");
            order.setSlTriggerPxType("mark");

            orderId = exchanger.createOrder(order);

        } catch (Exception e) {
            log.error("下单出错 " + e.getMessage(), e);

            //有些特殊情况，下单报错，但是可能由于read timeout等原因，实际交易所订单成功
            order = exchanger.getOrderByCustomerOrderId(strategy.getInstId(), customerOrderId);
            if (order != null && StringUtils.isNotBlank(order.getOrdId())) {
                log.info("gold-fork 下单出错，但实际交易所订单成功，订单id:{}", orderId);
                orderId = order.getOrdId();
            }
        }
        Trade trade = Trade.builder()
                .strategy(strategy.getId())
                .openOrderId(orderId)
                .instId(strategy.getInstId())
                .posSide(order.getPosSide())
                .planOpenSize(new BigDecimal(order.getSz()))
                .planOpenPrice(new BigDecimal(order.getPx()))
                .triggerWinPrice(new BigDecimal(order.getTpTriggerPx()))
                .planWinPrice(new BigDecimal(order.getTpOrdPx()))
                .algoOrderId(order.getAttachAlgoClOrdId())
                .triggerMissPrice(new BigDecimal(order.getSlTriggerPx()))
                .planMissPrice(new BigDecimal(order.getSlOrdPx()))
                .status(TradeStatus.PENDING.getCode())
                .build();
        tradeDao.save(trade);
    }

    private BigDecimal getExpectOpenPrice(Exchanger exchanger, String instId, DataGranularity dataGranularity, long kLineDataTime) {
        Date now = new Date();
        Date latestKLineTime = DateUtil.getLatestKLineTime(now, dataGranularity);
        Date start = DateUtils.addSeconds(latestKLineTime, -1);
        Date end = DateUtils.addSeconds(latestKLineTime, +1);
        List<KLine> kLineList = exchanger.getKlineData(instId, start, end, dataGranularity);
        if (CollectionUtils.isEmpty(kLineList)) {
            log.error("k线返回为空，未获取到开仓价格");
            return null;
        }
        if (kLineList.size() > 1) {
            log.error("k线数据有多条，未获取到开仓价格");
            return null;
        }
//        if (kLineList.get(0).getDataTime() != kLineDataTime) {
//            log.error("指定时间k线不存在，未获取到开仓价格");
//            return null;
//        }

        return kLineList.get(0).getClose();
    }

    private boolean isMACDFilterPass(String instId, Date now, DataGranularity dataGranularity, boolean isStrict) {
        Long preDataTime = DateUtil.getLatestKLineDataTime(now, dataGranularity);
        Long prePreDataTime = DateUtil.getPreKLineDataTime(preDataTime, dataGranularity);

        KLine prePreKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), prePreDataTime);
        KLine preKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), preDataTime);
        if (preKline == null) {
            log.info("[{}] gold-fork k线不存在，macd滤网不通过 {}", instId, preDataTime);
            return false;
        }
        if (prePreKline == null) {
            log.info("[{}] gold-fork k线不存在，macd滤网不通过 {}", instId, prePreDataTime);
            return false;
        }
        if (isStrict && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("[{}] gold-fork macd递减趋势，滤网不通过 {} {}-{}", instId, dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        //非严格模式下，MACD小于零且递减才不通过
        if (!isStrict && preKline.getMacd().compareTo(new BigDecimal("0")) < 0
                && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("[{}] gold-fork macd递减趋势，滤网不通过 {} {}-{}", instId, dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        return true;
    }

    @Override
    public void onOpen(Trade trade, Order openOrder) {
        trade.setOpenTime(new Date(openOrder.getFillTime()));
        trade.setStatus(TradeStatus.OPENED.getCode());
        trade.setActualOpenPrice(new BigDecimal(openOrder.getAvgPx()));
        trade.setOpenFeeCcy(openOrder.getFeeCcy());
        trade.setOpenFee(new BigDecimal(openOrder.getFee()));


        tradeDao.updateById(trade);
        log.info("交易已开仓成功 交易id:{},成交数量:{},成交均价:{}", trade.getId(), openOrder.getAccFillSz(), openOrder.getAvgPx());
    }

    @Override
    public void onClose(Trade trade, Order algoOrder, Order closeOrder) {
        trade.setCloseOrderId(closeOrder.getOrdId());
        trade.setActualClosePrice(new BigDecimal(closeOrder.getAvgPx()));
        trade.setStatus(TradeStatus.CLOSED.getCode());
        trade.setCloseFee(new BigDecimal(closeOrder.getFee()));
        trade.setCloseFeeCcy(closeOrder.getFeeCcy());
        trade.setCloseTime(new Date(closeOrder.getFillTime()));
        trade.setProfit(new BigDecimal(closeOrder.getPnl()));

        tradeDao.updateById(trade);
        log.info("交易已平仓成功,交易id:{},成交数量:{},成交均价:{},收益:{}", trade.getId(), closeOrder.getAccFillSz(), closeOrder.getAvgPx(), closeOrder.getPnl());
    }

    @Override
    public void onCancel(Trade trade) {
        trade.setCancelTime(new Date());
        trade.setStatus(TradeStatus.CANCEL.getCode());
        tradeDao.updateById(trade);
        log.info("交易已成功取消,交易id:{}", trade.getId());
    }
}
