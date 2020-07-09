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
import com.wealth.fly.statistic.StatisticStrategyAction;

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
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), 100000);

        for (int i = kLineList.size(); i >= 1; i--) {
            KLine kLine = kLineList.get(i - 1);
            strategyHandler.onNewKLine(kLine);
        }

        Map<String, StatisticStrategyAction.StatisticItem> kLineMap = action.getTargetKlineMap();
        System.out.println("startTime,win,direct,endTime,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
        long maxDataTime = 0L;
        for (StatisticStrategyAction.StatisticItem item : kLineMap.values()) {
//            if (item.getStartDataTime() < maxDataTime) {
//                continue;
//            }
            System.out.println("`" + item.getStartDataTime() + "," + item.getIsWin() + "," + (item.getGoingLong() ? "long" : "short") + ",`" + item.getEndDataTime() + "," + item.getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice() + "," + item.getAmplitudeFromOpenPrice() + "," + item.getProfitPercent());
            maxDataTime = item.getEndDataTime();
        }
    }

    @Test
    public void aggregate() {
        int period = 2;
        long min = 20140203000000L;

        while (true) {
            List<KLine> kLineList = kLineDao.getLastKLineGTDataTime(DataGranularity.ONE_HOUR.name(), min, period);
            if (kLineList == null || kLineList.size() < period) {
                System.out.println(">>>>>>>>> no more data");
                break;
            }

            aggregateNewLine(kLineList);

            Date date = DateUtils.addHours(DateUtil.parseStandardTime(min), period);
            min = Long.parseLong(DateUtil.formatToStandardTime(date.getTime()));
        }
        System.out.println("all finished.....");

    }

    private void aggregateNewLine(List<KLine> kLineList) {
        KLine newLine = new KLine();
        newLine.setOpen(kLineList.get(0).getOpen());
        newLine.setClose(kLineList.get(kLineList.size() - 1).getClose());
        newLine.setDataTime(kLineList.get(0).getDataTime());

        newLine.setGranularity(DataGranularity.TWO_HOUR.name());
        newLine.setEma26(new BigDecimal(1));
        newLine.setEma12(new BigDecimal(1));
        newLine.setDea9(new BigDecimal(1));
        newLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
        newLine.setCurrencyId(1);

        newLine.setHigh(new BigDecimal(0));
        newLine.setLow(kLineList.get(0).getLow());
        newLine.setVolume(new BigDecimal(0));
        newLine.setCurrencyVolume(new BigDecimal(0));

        for (KLine line : kLineList) {
            newLine.setVolume(newLine.getVolume().add(line.getVolume()));
            newLine.setCurrencyVolume(newLine.getCurrencyVolume().add(line.getCurrencyVolume()));

            if (line.getHigh().compareTo(newLine.getHigh()) > 0) {
                newLine.setHigh(line.getHigh());
            }
            if (line.getLow().compareTo(newLine.getLow()) < 0) {
                newLine.setLow(line.getLow());
            }
        }
        kLineDao.insert(newLine);
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


    @Test
    public void fillMACD() throws ParseException {

        String min = "20200530234000";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        KLine prevKLine = kLineDao.getKlineByDataTime(DataGranularity.FIVE_MINUTES.name(), 20200530233500L);
        KLine kLine = kLineDao.getKlineByDataTime(DataGranularity.FIVE_MINUTES.name(), Long.parseLong(min));

        while (true) {
            //计算macd并设置
            double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
            double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
            double diff = MathUtil.caculateDIF(ema12, ema26);
            double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);

            kLine.setEma12(new BigDecimal(ema12));
            kLine.setEma26(new BigDecimal(ema26));
            kLine.setDea9(new BigDecimal(dea9));
            kLineDao.updateByPrimaryKey(kLine);


            prevKLine = kLine;
            Date date = DateUtils.addMinutes(simpleDateFormat.parse(min), 5);
            min = simpleDateFormat.format(date);
            kLine = kLineDao.getKlineByDataTime(DataGranularity.FIVE_MINUTES.name(), Long.parseLong(min));
            System.out.println(">>>>>>>>>" + min);
            if (kLine == null) {
                System.out.println("not found" + min);
                break;
            }
        }

    }


}
