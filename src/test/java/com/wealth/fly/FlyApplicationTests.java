package com.wealth.fly;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.CryptoCompareExchanger;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.SimpleStatisticStrategyAction;
import com.wealth.fly.statistic.StatisticItem;


import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class FlyApplicationTests {

  @Autowired
  private StrategyHandler strategyHandler;

  @Autowired
  private KLineDao kLineDao;

  @Autowired
  private CryptoCompareExchanger exchanger;

//    @Autowired
//    private StatisticVolumeStrategyAction action;

  @Autowired
  private SimpleStatisticStrategyAction action;

  @Test
  public void statistics() {
    List<KLine> kLineList = kLineDao
        .getLastKLineByGranularity(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), 100000);

    for (int i = kLineList.size(); i >= 1; i--) {
      KLine kLine = kLineList.get(i - 1);
      strategyHandler.onNewKLine(kLine);

      BigDecimal mockRealTimePrice = triggerRealTimeIfPossiable(kLine);
      if (mockRealTimePrice != null) {
        strategyHandler.onRealTime(kLine.getDataTime(), mockRealTimePrice);
      }
    }

    Map<String, StatisticItem> kLineMap = action.getTargetKlineMap();
    System.out.println(
        "startTime,win,direct,endTime,spendDays,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
    long maxDataTime = 0L;
    for (StatisticItem item : kLineMap.values()) {
//            if (item.getStartDataTime() < maxDataTime) {
//                continue;
//            }
      double spendDays = DateUtil.getDistanceDays(item.getStartDataTime(), item.getEndDataTime());
      System.out.println(
          "`" + item.getStartDataTime() + "," + item.getIsWin() + "," + (item.getGoingLong()
              ? "long" : "short") + ",`" + item.getEndDataTime() + "," + spendDays + "," + item
              .getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice()
              + "," + item.getAmplitudeFromOpenPrice() + "," + item.getProfitPercent());
      maxDataTime = item.getEndDataTime();
    }
  }


  private BigDecimal triggerRealTimeIfPossiable(KLine kLine) {

    if (!strategyHandler.getHoldingStockMap().isEmpty()) {
      for (StrategyHandler.HoldingStock holdingStock : strategyHandler.getHoldingStockMap()
          .values()) {

        if (kLine.getDataTime().longValue() == holdingStock.getOpenKline().getDataTime()
            .longValue()) {
          continue;
        }

        BigDecimal openStockPrice = holdingStock.getOpenKline().getClose();
        BigDecimal priceMA = null;
//        BigDecimal priceMA = strategyHandler.getPriceMA();

        if ("ClassicMALongOpenStrategy".equals(holdingStock.getOpenStrategy().getId())) {
          BigDecimal missPrice = MathUtil.addPercent(priceMA,
              new BigDecimal(CommonConstants.MISS_PERCENT).multiply(new BigDecimal(-1)));
          if (kLine.getLow().compareTo(missPrice) < 0) {
            return missPrice.subtract(new BigDecimal(2));
          }

//                    BigDecimal missPriceOpen = MathUtil.addPercent(openStockPrice, new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(-1)));
//                    if (kLine.getLow().compareTo(missPriceOpen) < 0) {
//                        return missPriceOpen.subtract(new BigDecimal(2));
//                    }

          BigDecimal winPrice = MathUtil.addPercent(openStockPrice, CommonConstants.WIN_PERCENT);
          if (kLine.getHigh().compareTo(winPrice) > 0) {
            return winPrice.add(new BigDecimal(2));
          }

        }
        if ("ClassicMAShortOpenStrategy".equals(holdingStock.getOpenStrategy().getId())) {
          BigDecimal missPrice = MathUtil.addPercent(priceMA, CommonConstants.MISS_PERCENT);
          if (kLine.getHigh().compareTo(missPrice) > 0) {
            return missPrice.add(new BigDecimal(2));
          }

//                    BigDecimal missPriceOpen = MathUtil.addPercent(openStockPrice, CommonConstants.WIN_PERCENT);
//                    if (kLine.getHigh().compareTo(missPriceOpen) > 0) {
//                        return missPriceOpen.add(new BigDecimal(2));
//                    }

          BigDecimal winPrice = MathUtil.addPercent(openStockPrice,
              new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(-1)));
          if (kLine.getLow().compareTo(winPrice) < 0) {
            return winPrice.subtract(new BigDecimal(2));
          }
        }
      }
    }

    return null;
  }



  @Test
  public void fillKline() throws ParseException {
    Date endtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-07-01 16:00:00");

    while (true) {
      List<KLine> kLines = exchanger.getKlineData(endtime, 2000);
      if (kLines == null || kLines.isEmpty()) {
        System.out.println("all finished......");
        break;
      }
      long maxDataTime = 0;
      for (KLine kLine : kLines) {
        kLineDao.insert(kLine);
        if (kLine.getDataTime() > maxDataTime) {
          maxDataTime = kLine.getDataTime();
        }
      }

      endtime = DateUtil.parseStandardTime(maxDataTime);
      endtime = DateUtils.addHours(endtime, -kLines.size());
    }

  }

}
