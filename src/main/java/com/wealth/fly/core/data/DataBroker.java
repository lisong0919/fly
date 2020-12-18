package com.wealth.fly.core.data;

import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.listener.KLineListener;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.data.manufacturer.*;
import com.wealth.fly.core.data.manufacturer.interf.DataManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyAction;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.core.strategy.criteria.Sector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataBroker implements KLineListener, StrategyAction {
    @Autowired
    private DataFetcher dataFetcher;

    @Autowired
    private KLineDao kLineDao;

    @Autowired
    private StrategyHandler strategyHandler;

    //数据加工器
    private List<DataManufacturer> newKLineEventDataManufacturerList =new ArrayList<>();
    private List<DataManufacturer> realTimeEventDataManufacturerList =new ArrayList<>();
    private List<DataManufacturer> openStockEventDataManufacturerList =new ArrayList<>();

    private static final SimpleOpenStockManufacturer openStockManuFacturer=new SimpleOpenStockManufacturer();

    @PostConstruct
    public void init(){
        dataFetcher.registerKLineListener(this);

        newKLineEventDataManufacturerList.add(new CommonKLineManufacturer());
        newKLineEventDataManufacturerList.add(new MACDManufacturer());
        //MA
        MAManufacturer priceMaDataReinforce=new MAManufacturer(MAType.PRICE,new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM),CommonConstants.DEFAULT_DATA_GRANULARITY);
        MAManufacturer volumeMaDataReinforce=new MAManufacturer(MAType.VOLUME,new Sector(Sector.SectorType.KLINE_VOLUME_MA,CommonConstants.DEFAULT_MA_VOLUME_NUM),CommonConstants.DEFAULT_DATA_GRANULARITY);
        newKLineEventDataManufacturerList.add(priceMaDataReinforce);
        newKLineEventDataManufacturerList.add(volumeMaDataReinforce);

        newKLineEventDataManufacturerList.add(new PreKLineManufacturer());
        newKLineEventDataManufacturerList.add(new MACDManufacturer());

        realTimeEventDataManufacturerList.add(new SimpleRealtimeManufacturer());
        openStockEventDataManufacturerList.add(new SimpleOpenStockManufacturer());
    }

    public Map<String, BigDecimal> getKLineDataByStrategy(Strategy strategy,long datatime){
        Map<String, BigDecimal> result=new HashMap<>();

        //平仓策略需要对应开仓数据
        if(!strategy.isOpenStock()){
            StrategyHandler.HoldingStock holdingStock= strategyHandler.getHoldingStockMap().get(strategy.getCloseStrategyId());
            if(holdingStock!=null){
                OpenStock openStock=new OpenStock();
                openStock.setOpenPrice(holdingStock.getOpenStockPrice());
                openStockManuFacturer.manufact(openStock,result);
            }
        }



        //MA 数据预充

        // LastKline 数据预充

        //preKline 数据预充
        return null;
    }

    private void setOpenStockDataIfNecessary(){

    }


    public Map<String, BigDecimal> getRealTimeDataByStrategy(Strategy strategy){


        return null;
    }

    @Override
    public void onNewKLine(KLine kLine) {

    }

    @Override
    public void onOpenStock(Strategy strategy, KLine kLine) {

    }

    @Override
    public void onCloseStock(Strategy openStrategy, KLine openKLine, Strategy closeStrategy, BigDecimal closePrice, long closeDataTime) {

    }
}
