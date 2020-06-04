package com.wealth.fly.core.strategy;

import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class StrategyHandler implements KLineListener {

  private KLine lastKline;
  private BigDecimal lastKlineMa;


  private List<Strategy> strategyList;

  @PostConstruct
  public void initStrategy() {

  }

  @Override
  public void onNewKLine(KLine kLine) {
    List<KLine> lastKlineList = new ArrayList<>();
    lastKlineList.add(lastKline);
    lastKlineList.add(kLine);



    lastKline = kLine;



    Map longSectorValues=new HashMap();
    longSectorValues.put(SectorType.KLINE_VOLUME.name(),kLine.getVolume());
    longSectorValues.put(SectorType.KLINE_POSITIVE_PRICE.name(),kLine.getHigh());
    longSectorValues.put(SectorType.KLINE_NEGATIVE_PRICE.name(),kLine.getLow());
    longSectorValues.put(SectorType.KLINE_PRICE_CLOSE.name(),kLine.getClose());


    Map shortSectorValues=new HashMap();



    longSectorValues.put(SectorType.KLINE_PRICE_MA.name(),null);
    longSectorValues.put(SectorType.KLINE_PRICE_MA_DIRECTION.name(),null);
    longSectorValues.put(SectorType.KLINE_VOLUME_MA.name(),null);






  }

}
