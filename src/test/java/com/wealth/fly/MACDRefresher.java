package com.wealth.fly;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;

import java.math.BigDecimal;
import java.util.List;

import com.wealth.fly.core.entity.KLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MACDRefresher {
    @Autowired
    private KLineDao kLineDao;

    @Test
    public void refresh() {

        List<KLine> kLineList = kLineDao
                .getLastKLineByGranularity(DataGranularity.TWO_HOUR.name(), 100000);

        KLine prevKLine = kLineList.get(kLineList.size() - 1);

        for (int i = kLineList.size() - 2; i >= 0; i--) {
            KLine kLine = kLineList.get(i);

            double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
            double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
            double diff = MathUtil.caculateDIF(ema12, ema26);
            double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);

            kLine.setEma12(new BigDecimal(ema12));
            kLine.setEma26(new BigDecimal(ema26));
            kLine.setDea9(new BigDecimal(dea9));
            kLine.setMacd(new BigDecimal(MathUtil.caculateMACD(diff, dea9)));


            kLineDao.updateByPrimaryKeySelective(kLine);
            prevKLine = kLine;
            System.out.println("update " + kLine.getDataTime() + " finshed...");
        }

        System.out.println("all finished....");

    }

}
