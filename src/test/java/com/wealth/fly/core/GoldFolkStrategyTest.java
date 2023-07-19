package com.wealth.fly.core;

import com.wealth.fly.FlyTestApplication;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.OkexExchanger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : lisong
 * @date : 2023/7/3
 */
@SpringBootTest(classes = FlyTestApplication.class)
@Slf4j
public class GoldFolkStrategyTest {
    @Resource
    private KLineDao kLineDao;
    @Resource
    private MACDHandler macdHandler;
    private OkexExchanger exchanger = new OkexExchanger(null);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class TriggerResult {
        private Boolean win;
        private BigDecimal triggerPrice;
        private BigDecimal openPrice;
        private BigDecimal percent;
        private Long openDataTime;
        private Long triggerDataTime;
        private Long timeDistanceInMinutes;
    }

    @Test
    public void testStrategy() {
        DataGranularity dataGranularity = DataGranularity.FIFTEEN_MINUTES;
        long greatThan = 20230101010000L;
        long lessThan = 20230705200000L;
        String instId = "ETH-USDT-SWAP";
        BigDecimal profitPercent = new BigDecimal("0.003");
        List<TriggerResult> triggerResults = new ArrayList<>();

        while (true) {
            List<KLine> kLineList = kLineDao.getLastKLineGTDataTime(instId, dataGranularity.name(), greatThan, 100);
            if (CollectionUtils.isEmpty(kLineList)) {
                break;
            }
            for (KLine kLine : kLineList) {
                try {
                    Long preDataTime = DateUtil.getPreKLineDataTime(kLine.getDataTime(), dataGranularity);
                    Long prePreDataTime = DateUtil.getPreKLineDataTime(preDataTime, dataGranularity);
                    KLine preKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), preDataTime);
                    KLine prePreKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), prePreDataTime);

                    BigDecimal zero = new BigDecimal("0");
                    Date now = DateUtil.parseStandardTime(kLine.getDataTime() + 1);
                    boolean isGoldFork = prePreKline.getMacd().compareTo(zero) < 0 && preKline.getMacd().compareTo(new BigDecimal("0")) > 0;

                    if (isGoldFork) {
//                        long pre = DateUtil.getPreKLineDataTime(prePreDataTime, dataGranularity);
//                        long prePre = DateUtil.getPreKLineDataTime(pre, dataGranularity);
//                        if (kLineDao.getKlineByDataTime(dataGranularity.name(), pre).getMacd().compareTo(zero) >= 0) {
//                            log.info("=============前置红k太少: ", kLine.getDataTime());
//                            continue;
//                        }
//                        if (kLineDao.getKlineByDataTime(dataGranularity.name(), prePre).getMacd().compareTo(zero) >= 0) {
//                            log.info("=============前置红k太少: ", kLine.getDataTime());
//                            continue;
//                        }


                        if (isMACDFilterPass(instId, now, DataGranularity.ONE_HOUR, true) && isMACDFilterPass(instId, now, DataGranularity.FOUR_HOUR, false)) {
                            TriggerResult triggerResult = goldFork(kLine, profitPercent, dataGranularity);
                            triggerResults.add(triggerResult);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    greatThan = kLine.getDataTime();
                }

                if (greatThan >= lessThan) {
                    break;
                }
            }
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info(JsonUtil.toJSONString(triggerResults));

        int win = 0;
        int miss = 0;
        int unknown = 0;

        for (TriggerResult triggerResult : triggerResults) {
            if (triggerResult.win == null) {
                unknown++;
            } else if (triggerResult.win) {
                win++;
            } else {
                miss++;
            }
            String str = String.format("%s,%s,%s,%s,%s,%s,%s", triggerResult.openDataTime, triggerResult.triggerDataTime, triggerResult.win, triggerResult.openPrice, triggerResult.triggerPrice, triggerResult.timeDistanceInMinutes, triggerResult.percent);
            System.out.println(str);
        }

        log.info("win:{},miss:{},unknown:{},percent:{}", win, miss, unknown, new BigDecimal(win).divide(new BigDecimal(win + miss), 2, RoundingMode.FLOOR));
    }


    private TriggerResult goldFork(KLine kLine, BigDecimal profitPercent, DataGranularity dataGranularity) {
        BigDecimal winPrice = getWinPrice(kLine.getOpen(), profitPercent);
        BigDecimal missPrice = getMissPrice(kLine.getLow(), profitPercent);

        TriggerResult triggerResult = trigger(winPrice, missPrice, kLine);

        if (triggerResult == null) {
            long dataTime = kLine.getDataTime();
            while (true) {
                try {
                    Long nextDataTime = DateUtil.getNextKlineDataTime(dataTime, dataGranularity);
                    KLine nextKline = kLineDao.getKlineByDataTime(kLine.getInstId(), dataGranularity.name(), nextDataTime);
                    if (nextKline == null) {
                        break;
                    }
                    triggerResult = trigger(winPrice, missPrice, nextKline);
                    if (triggerResult != null) {
                        break;
                    }
                    dataTime = nextDataTime;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        if (triggerResult == null) {
            triggerResult = new TriggerResult();
        }
        triggerResult.setOpenDataTime(kLine.getDataTime());
        triggerResult.setOpenPrice(kLine.getOpen());
        triggerResult.setPercent(profitPercent);
        triggerResult.setTimeDistanceInMinutes(getTimeDistanceInSeconds(triggerResult.openDataTime, triggerResult.triggerDataTime) / 60);
        return triggerResult;
    }


    private static long getTimeDistanceInSeconds(long start, long end) {
        Date startDate = DateUtil.parseStandardTime(start);
        Date endDate = DateUtil.parseStandardTime(end);
        return (endDate.getTime() - startDate.getTime()) / 1000;
    }

    private TriggerResult trigger(BigDecimal winPrice, BigDecimal missPrice, KLine kLine) {
        if (isMiss(missPrice, kLine)) {
            return TriggerResult.builder()
                    .win(false).triggerPrice(missPrice).triggerDataTime(kLine.getDataTime())
                    .build();
        }
        if (isWin(winPrice, kLine)) {
            return TriggerResult.builder()
                    .win(true).triggerPrice(winPrice).triggerDataTime(kLine.getDataTime()).build();
        }

        return null;
    }

    private boolean isMiss(BigDecimal missPrice, KLine kLine) {
        return kLine.getLow().compareTo(missPrice) <= 0;
    }

    private boolean isWin(BigDecimal winPrice, KLine kLine) {
        return kLine.getHigh().compareTo(winPrice) >= 0;
    }

    private BigDecimal getMissPrice(BigDecimal open, BigDecimal percent) {
        return open.subtract(open.multiply(percent));
    }

    private BigDecimal getWinPrice(BigDecimal open, BigDecimal percent) {
        return open.add(open.multiply(percent));
    }


    @Test
    public void fillKlineData() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2023-06-16 14:01:00");
        Date endTime = sdf.parse("2023-07-05 20:00:00");
        DataGranularity dataGranularity = DataGranularity.ONE_HOUR;

        while (startTime.getTime() <= endTime.getTime()) {
            Date nextDay = DateUtils.addDays(startTime, 1);
            List<KLine> kLineList = exchanger.getHistoryKlineData("ETH-USDT-SWAP", startTime, nextDay, dataGranularity);

            kLineList.sort((o1, o2) -> {
                if (o1.getDataTime().equals(o2.getDataTime())) {
                    return 0;
                }
                return o1.getDataTime() - o2.getDataTime() > 0 ? 1 : -1;
            });

            for (KLine kLine : kLineList) {
                kLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
                kLine.setCurrencyId(1);
                macdHandler.setMACD(kLine);
                kLineDao.insert(kLine);
                log.info("插入数据成功:" + kLine.getDataTime());
            }
            startTime = nextDay;
        }
        log.info("全部数据初始化完成");
    }

    private boolean isMACDFilterPass(String instId, Date now, DataGranularity dataGranularity, boolean isStrict) {
        Long preDataTime = DateUtil.getLatestKLineDataTime(now, dataGranularity);
        Long prePreDataTime = DateUtil.getPreKLineDataTime(preDataTime, dataGranularity);

        KLine prePreKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), prePreDataTime);
        KLine preKline = kLineDao.getKlineByDataTime(instId, dataGranularity.name(), preDataTime);
        if (preKline == null) {
            log.info("k线不存在，macd滤网不通过 {}", preDataTime);
            return false;
        }
        if (prePreKline == null) {
            log.info("k线不存在，macd滤网不通过 {}", prePreDataTime);
            return false;
        }
        if (isStrict && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("macd递减趋势，滤网不通过 {} {}-{}", dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        //非严格模式下，MACD小于零且递减才不通过
        if (!isStrict && preKline.getMacd().compareTo(new BigDecimal("0")) < 0
                && preKline.getMacd().compareTo(prePreKline.getMacd()) < 0) {
            log.info("macd递减趋势，滤网不通过 {} {}-{}", dataGranularity, prePreDataTime, preDataTime);
            return false;
        }

        //非严格模式下macd大于零就行
//        if (!isStrict && preKline.getMacd().compareTo(new BigDecimal("0")) < 0) {
//            log.info("macd递减趋势，滤网不通过 {} {}-{}", dataGranularity, prePreDataTime, preDataTime);
//            return false;
//        }

        return true;
    }
}
