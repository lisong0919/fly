package com.wealth.fly;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.StatisticStrategyAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.Map;

@SpringBootTest
class FlyApplicationTests {

    @Autowired
    private StrategyHandler strategyHandler;

    @Autowired
    private KLineDao kLineDao;

    @Autowired
    private StatisticStrategyAction action;

    @Test
    public void statistics() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(DataGranularity.FIVE_MINUTES.name(), 10000);

        for (int i = kLineList.size(); i >= 1; i--) {
            KLine kLine = kLineList.get(i - 1);
            strategyHandler.onNewKLine(kLine);
        }

        Map<String, StatisticStrategyAction.StatisticItem> kLineMap = action.getTargetKlineMap();
        System.out.println("startTime,win,endTime,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice");
        for (StatisticStrategyAction.StatisticItem item : kLineMap.values()) {
            System.out.println("`"+item.getStartDataTime() + "," + item.getIsWin() + ",`" + item.getEndDataTime() + "," + item.getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice() + "," + item.getAmplitudeFromOpenPrice());
        }
    }


}
