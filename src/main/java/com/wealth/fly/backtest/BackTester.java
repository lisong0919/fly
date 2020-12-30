package com.wealth.fly.backtest;


import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.SimpleStatisticStrategyAction;
import com.wealth.fly.statistic.StatisticItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Map;

public abstract class BackTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackTester.class);

    @Autowired
    private KLineDao kLineDao;

    @Autowired
    protected StrategyHandler strategyHandler;


    //数据分析的执行器
    private SimpleStatisticStrategyAction simpleStatisticStrategyAction =new SimpleStatisticStrategyAction();

    //抽象方法
    public abstract List<Strategy> getStrategyList();
    public abstract List<RealTimePrice> generateRealTimePrice(KLine kLine);
    public abstract Long getStartTime();
    public abstract Long getEndTime();
    public abstract DataGranularity getDataGranularity();



    public void run(){
        strategyHandler.addStrategyAction(simpleStatisticStrategyAction);//简单的数据分析执行器
        strategyHandler.setStrategyList(getStrategyList());

        LOGGER.info("start to run back test from {} to {}",getStartTime(),getEndTime());

        KLine lastKline=null;

        while(true){
            List<KLine> kLineList=generateKline(lastKline);

            if(kLineList==null || kLineList.isEmpty()){
                break;
            }
            lastKline=kLineList.get(kLineList.size()-1);

            for(KLine kLine:kLineList){
                strategyHandler.onNewKLine(kLine);
               List<RealTimePrice> realTimePriceList= generateRealTimePrice(kLine);
                if(realTimePriceList==null || realTimePriceList.isEmpty()){
                   continue;
                }
                for(RealTimePrice realTimePrice:realTimePriceList){

                    strategyHandler.onRealTime(realTimePrice);
                }
            }
        }

        statisticResult();

        LOGGER.info("run back test finished from {} to {}",getStartTime(),getEndTime());
    }

    public List<KLine> generateKline(KLine lastKLine) {
        Long startTime=getStartTime();
        if(lastKLine!=null){
            startTime=lastKLine.getDataTime();
        }
        return kLineDao.getLastKLineByDataTime(getDataGranularity().name(),startTime,getEndTime(),200);
    }

    private void statisticResult(){
        //打印数据分析结果
        Map<String, StatisticItem> kLineMap = simpleStatisticStrategyAction.getTargetKlineMap();
        System.out.println(
                "startTime,win,direct,endTime,spendDays,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
        long maxDataTime = 0L;
        for (StatisticItem item : kLineMap.values()) {
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