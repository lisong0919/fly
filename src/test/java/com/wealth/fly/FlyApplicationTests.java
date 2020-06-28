package com.wealth.fly;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.StatisticStrategyAction;
import com.wealth.fly.statistic.StatisticVolumeStrategyAction;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
class FlyApplicationTests {

    @Autowired
    private StrategyHandler strategyHandler;

    @Autowired
    private KLineDao kLineDao;

//    @Autowired
//    private StatisticVolumeStrategyAction action;

    @Autowired
    private StatisticStrategyAction action;

    @Test
    public void statistics() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), 100000);

        for (int i = kLineList.size(); i >= 1; i--) {
            KLine kLine = kLineList.get(i - 1);
            strategyHandler.onNewKLine(kLine);
        }

        Map<String, StatisticStrategyAction.StatisticItem> kLineMap = action.getTargetKlineMap();
        System.out.println("startTime,win,endTime,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
        for (StatisticStrategyAction.StatisticItem item : kLineMap.values()) {
            System.out.println("`" + item.getStartDataTime() + "," + item.getIsWin() + ",`" + item.getEndDataTime() + "," + item.getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice() + "," + item.getAmplitudeFromOpenPrice() + "," + item.getProfitPercent());
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
