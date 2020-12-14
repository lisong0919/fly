package com.wealth.fly.backtest;


import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.SimpleStatisticStrategyAction;
import com.wealth.fly.statistic.StatisticStrategyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class BackTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackTester.class);

    protected Long startTime;
    protected Long endTime;

    //数据分析的执行器
    private SimpleStatisticStrategyAction simpleStatisticStrategyAction =new SimpleStatisticStrategyAction();

    //抽象方法
    public abstract StrategyHandler getStrategyHandler();
    public abstract List<KLine> generateKline(KLine lastKLine);
    public abstract List<RealTimePrice> generateRealTimePrice(KLine kLine);

    public BackTester(Long startTime, Long endTime){
        this.startTime=startTime;
        this.endTime=endTime;
    }

    public void run(){
        getStrategyHandler().addStrategyAction(simpleStatisticStrategyAction);//简单的数据分析执行器

        LOGGER.info("start to run back test from {} to {}",startTime,endTime);

        KLine lastKline=null;

        while(true){
            List<KLine> kLineList=generateKline(lastKline);

            if(kLineList==null || kLineList.isEmpty()){
                break;
            }
            lastKline=kLineList.get(kLineList.size()-1);

            for(KLine kLine:kLineList){
                getStrategyHandler().onNewKLine(kLine);
               List<RealTimePrice> realTimePriceList= generateRealTimePrice(kLine);
                if(realTimePriceList==null || realTimePriceList.isEmpty()){
                   continue;
                }
                for(RealTimePrice realTimePrice:realTimePriceList){
                    getStrategyHandler().onRealTime(realTimePrice.getDataTime(), realTimePrice.getPrice());
                }
            }
        }

        statisticResult();

        LOGGER.info("run back test finished from {} to {}",startTime,endTime);
    }

    private void statisticResult(){
        //打印数据分析结果
        Map<String, StatisticStrategyAction.StatisticItem> kLineMap = simpleStatisticStrategyAction.getTargetKlineMap();
        System.out.println(
                "startTime,win,direct,endTime,spendDays,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
        long maxDataTime = 0L;
        for (StatisticStrategyAction.StatisticItem item : kLineMap.values()) {
            if (item.getStartDataTime() < maxDataTime) {
                continue;
            }
            double spendDays = DateUtil.getDistanceDays(item.getStartDataTime(), item.getEndDataTime());
            System.out.println(
                    "`" + item.getStartDataTime() + "," + item.getIsWin() + "," + (item.getGoingLong()
                            ? "long" : "short") + ",`" + item.getEndDataTime() + "," + spendDays + "," + item
                            .getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice()
                            + "," + item.getAmplitudeFromOpenPrice() + "," + item.getProfitPercent());
            maxDataTime = item.getEndDataTime();
        }
    }
}
