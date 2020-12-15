package com.wealth.fly.backtest;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class MACDBackTester extends BackTester{

    private Long startTime;
    private Long endTime;

    @Override
    public List<RealTimePrice> generateRealTimePrice(KLine kLine) {
        return null;
    }

    @Override
    public Long getStartTime() {
        return this.startTime;
    }

    @Override
    public Long getEndTime() {
        return this.endTime;
    }


    @Override
    public DataGranularity getDataGranularity() {
        return CommonConstants.DEFAULT_DATA_GRANULARITY;
    }

    public List<Strategy> getStrategyList() {

        List<Strategy> strategyList = new ArrayList<>();

        Strategy strategy1 = new Strategy();
        strategy1.setId("ClassicMALongOpenStrategy");
        strategy1.setCriteria(StrategyFactory.getClassicMALongCriteria());
        strategy1.setGoingLong(true);
        strategy1.setOpenStock(true);
        strategyList.add(strategy1);

        Strategy strategy2 = new Strategy();
        strategy2.setId("ClassicMAShortOpenStrategy");
        strategy2.setCriteria(StrategyFactory.getClassicMAShortCriteria());
        strategy2.setGoingLong(false);
        strategy2.setOpenStock(true);
        strategyList.add(strategy2);


        Strategy strategy3 = new Strategy();
        strategy3.setId("ClassicMALongCloseStrategy");
        strategy3.setCriteria(StrategyFactory.getClassicMALongCloseCriteria());
        strategy3.setGoingLong(true);
        strategy3.setOpenStock(false);
        strategy3.setCloseStrategyId(strategy1.getId());
        strategyList.add(strategy3);


        Strategy strategy4 = new Strategy();
        strategy4.setId("ClassicMAShortCloseStrategy");
        strategy4.setCriteria(StrategyFactory.getClassicMAShortCloseCriteria());
        strategy4.setGoingLong(false);
        strategy4.setOpenStock(false);
        strategy4.setCloseStrategyId(strategy2.getId());
        strategyList.add(strategy4);

        System.out.println(JSONObject.toJSONString(strategyList));

        return strategyList;
    }


}
