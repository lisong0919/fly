package com.wealth.fly;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.OkexExchanger;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.statistic.StatisticStrategyAction;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;


import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlyTestApplication.class)
class FlyTests {

    //    @Resource
    private StrategyHandler strategyHandler;

    //    @Resource
    private KLineDao kLineDao;

    @Resource
    private OkexExchanger okexExchanger;

    @Resource
    private GridDao gridDao;


//    @Autowired
//    private StatisticVolumeStrategyAction action;

    //    @Autowired
    private StatisticStrategyAction action;


    @Test
    public void initGrid() {
        BigDecimal flag = new BigDecimal(1200);
        String weight = "0.003";

        for (int i = 0; i < 300; i++) {
            String buyPrice = flag.toPlainString();
            flag = flag.add(flag.multiply(new BigDecimal(weight)));
            flag = flag.setScale(2, RoundingMode.HALF_UP);
            String sellPrice = flag.toPlainString();

            System.out.println(buyPrice + "-" + sellPrice);

            Grid grid = Grid.builder()
                    .instId("ETH-USD-230630")
                    .strategy(2)
                    .strategyDesc("千三/100张")
                    .weight(weight)
                    .buyPrice(buyPrice)
                    .sellPrice(sellPrice)
                    .num("100")
                    .build();
            gridDao.save(grid);
        }

        System.out.println(">>>>>>>>>网格初始化成功");
    }


    @Test
    public void statistics() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), 100000);

        for (int i = kLineList.size(); i >= 1; i--) {
            KLine kLine = kLineList.get(i - 1);
            strategyHandler.onNewKLine(kLine);
        }

        Map<String, StatisticStrategyAction.StatisticItem> kLineMap = action.getTargetKlineMap();
        System.out.println("startTime,win,endTime,startPrice,endPrice,amplitudeFromMAPrice,amplitudeFromOpenPrice,profitPercent");
        long maxDataTime = 0L;
        for (StatisticStrategyAction.StatisticItem item : kLineMap.values()) {
            if (item.getStartDataTime() < maxDataTime) {
                continue;
            }
            System.out.println("`" + item.getStartDataTime() + "," + item.getIsWin() + ",`" + item.getEndDataTime() + "," + item.getStartPrice() + "," + item.getEndPrice() + "," + item.getAmplitudeFromMAPrice() + "," + item.getAmplitudeFromOpenPrice() + "," + item.getProfitPercent());
            maxDataTime = item.getEndDataTime();
        }
    }

    @Test
    public void fillMACD() throws ParseException {

        String min = "20230609010000";
        DataGranularity dataGranularity = DataGranularity.FIFTEEN_MINUTES;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Long preKlineDataTime = DateUtil.getPreKLineDataTime(Long.parseLong(min), dataGranularity);
        KLine prevKLine = kLineDao.getKlineByDataTime(dataGranularity.name(), preKlineDataTime);
        KLine kLine = kLineDao.getKlineByDataTime(dataGranularity.name(), Long.parseLong(min));

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
            kLine = kLineDao.getKlineByDataTime(DataGranularity.ONE_HOUR.name(), Long.parseLong(min));
            System.out.println(">>>>>>>>>" + min);
            if (kLine == null) {
                System.out.println("not found" + min);
                break;
            }
        }

    }


}
