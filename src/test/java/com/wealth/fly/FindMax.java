package com.wealth.fly;


import com.wealth.fly.backtest.MACDBackTester;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.CloseStock;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FindMax extends MACDBackTester {

    @Autowired
    private KLineDao kLineDao;

    //key表示第几根线，Value表示多少次
    private Map<Integer, Integer> stasticMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        strategyHandler.addStrategyAction(new StrategyAction() {
            @Override
            public void onOpenStock(Strategy strategy, OpenStock openStock) {
                long datatime = openStock.getOpenDataTime();
                long preDateTime = DateUtil.getPreDateTime(datatime, getDataGranularity(), 6);

                List<KLine> kLineList = kLineDao.getLastKLineByDataTimeRange(getDataGranularity().name(), preDateTime, datatime);

                int extreIndex = -1;
                BigDecimal extreValue = kLineList.get(0).getOpen();

                for (int i = 0; i < kLineList.size(); i++) {
                    if (strategy.isGoingLong() && kLineList.get(i).getHigh().compareTo(extreValue) >= 0) {
                        extreIndex = i;
                        extreValue = kLineList.get(i).getHigh();
                    }
                    if (!strategy.isGoingLong() && kLineList.get(i).getLow().compareTo(extreValue) <= 0) {
                        extreIndex = i;
                        extreValue = kLineList.get(i).getLow();
                    }
                }

                Integer count = stasticMap.get(extreIndex);
                if (count == null) {
                    count = 0;
                }
                stasticMap.put(extreIndex, count + 1);
            }

            @Override
            public void onCloseStock(Strategy openStrategy, Strategy closeStrategy, CloseStock closeStock) {

            }
        });
    }

    public Map<Integer, Integer> getStasticMap() {
        return stasticMap;
    }

}
