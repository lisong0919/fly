package com.wealth.fly;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.dao.KLineDao;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MACDRefresher {
  @Autowired
  private KLineDao kLineDao;

  @Test
  public void refresh(){

//    double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
//    double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
//    double diff = MathUtil.caculateDIF(ema12, ema26);
//    double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);
//
//    prevMACD = new BigDecimal(MathUtil.caculateMACD(diff, dea9));
  }

}
