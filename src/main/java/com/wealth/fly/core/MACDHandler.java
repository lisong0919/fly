package com.wealth.fly.core;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.fetcher.KlineDataFetcher;
import com.wealth.fly.core.listener.KLineListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author : lisong
 * @date : 2023/6/9
 */
@Component
public class MACDHandler {

    @Resource
    private KLineDao kLineDao;


    public void setMACD(KLine kLine) {
        Long preKlineDataTime = DateUtil.getPreKLineDataTime(kLine.getDataTime(), DataGranularity.valueOf(kLine.getGranularity()));
        KLine prevKLine = kLineDao.getKlineByDataTime(kLine.getGranularity(), preKlineDataTime);

        double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
        double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
        double diff = MathUtil.caculateDIF(ema12, ema26);
        double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);

        BigDecimal macd = new BigDecimal(MathUtil.caculateMACD(diff, dea9));
        kLine.setEma12(new BigDecimal(ema12));
        kLine.setEma26(new BigDecimal(ema26));
        kLine.setDea9(new BigDecimal(dea9));
        kLine.setMacd(macd);
    }


}
