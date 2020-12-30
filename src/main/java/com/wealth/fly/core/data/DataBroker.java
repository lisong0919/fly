package com.wealth.fly.core.data;


import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.MAParam;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.data.manufacturer.*;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.core.strategy.criteria.CriteriaParser;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.exception.DataInsufficientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.*;

@Component
public class DataBroker {

    @Autowired
    private KLineDao kLineDao;


    private static final Logger LOGGER = LoggerFactory.getLogger(DataBroker.class);


    //数据加工器
    private CommonKLineManufacturer commonKLineManufacturer = new CommonKLineManufacturer();
    private static final SimpleOpenStockManufacturer openStockManuFacturer = new SimpleOpenStockManufacturer();
    private static final SimpleRealtimeManufacturer realtimeManufacturer = new SimpleRealtimeManufacturer();

    private HashMap<String, MAManufacturer> maManufacturerMap = new HashMap<>();
    private HashMap<String, LastKLineManufacturer> lastKLineManufacturerHashMap = new HashMap<>();
    private HashMap<String, PreKLineManufacturer> preKLineManufacturerHashMap = new HashMap<>();

    private static final BigDecimal ZERO = new BigDecimal(0);


    public Map<String, BigDecimal> getKLineDataByStrategy(Strategy strategy, long datatime, Map<String, StrategyHandler.HoldingStock> holdingStockMap) {

        KLine kLine = kLineDao.getKlineByDataTime(strategy.getDataGranularity().name(), datatime);
        if (kLine == null) {
            throw new DataInsufficientException("无数据: " + datatime);
        }
        Map<String, BigDecimal> sectorValues = getKLineDataByStrategyWithoutLastKlineData(strategy, kLine, holdingStockMap);
        processLastKline(strategy, kLine, sectorValues, holdingStockMap);
        sectorValues.put(Sector.SectorType.ZERO.name(), ZERO);
        return sectorValues;
    }

    public BigDecimal getMaValue(Sector sector, DataGranularity dataGranularity, MAParam maParam) {
        return getMAManufacturer(sector, dataGranularity).getMaValue(maParam);
    }

    public Map<String, BigDecimal> getRealTimeDataByStrategy(Strategy strategy, RealTimePrice realTimePrice, Map<String, StrategyHandler.HoldingStock> holdingStockMap) {
        Map<String, BigDecimal> result = new HashMap<>();
        realtimeManufacturer.manufact(realTimePrice, result);
        processOpenStockData(strategy, result, holdingStockMap);
        Set<Sector> sectorSet = CriteriaParser.parseSectorType(strategy.getCriteria());
        processMa(sectorSet, strategy, new MAParam(realTimePrice.getDataTime(), realTimePrice.getPrice()), result);
        result.put(Sector.SectorType.ZERO.name(), ZERO);
        return result;
    }

    public Map<String, BigDecimal> getKLineDataByStrategyWithoutLastKlineData(Strategy strategy, KLine kLine, Map<String, StrategyHandler.HoldingStock> holdingStockMap) {
        Map<String, BigDecimal> result = new HashMap<>();

        commonKLineManufacturer.manufact(kLine, result);
        processOpenStockData(strategy, result, holdingStockMap);
        Set<Sector> sectorSet = CriteriaParser.parseSectorType(strategy.getCriteria());
        processMa(sectorSet, strategy, new MAParam(kLine.getDataTime(), kLine.getClose()), result);

        PreKLineManufacturer preKLineManufacturer = preKLineManufacturerHashMap.get(strategy.getDataGranularity().name());
        if (preKLineManufacturer == null) {
            preKLineManufacturer = new PreKLineManufacturer(strategy.getDataGranularity(), kLineDao);
            preKLineManufacturerHashMap.put(strategy.getDataGranularity().name(), preKLineManufacturer);
        }
        preKLineManufacturer.manufact(kLine, result);

        return result;
    }

    private void processOpenStockData(Strategy strategy, Map<String, BigDecimal> sectorValues, Map<String, StrategyHandler.HoldingStock> holdingStockMap) {
        //平仓策略需要对应开仓数据
        if (!strategy.isOpenStock()) {
            StrategyHandler.HoldingStock holdingStock = holdingStockMap.get(strategy.getCloseStrategyId());
            if (holdingStock != null) {
                OpenStock openStock = new OpenStock(holdingStock.getOpenStockPrice(), holdingStock.getOpenDataTime());
                openStockManuFacturer.manufact(openStock, sectorValues);
            }
        }
    }

    private void processLastKline(Strategy strategy, KLine kLine, Map<String, BigDecimal> sectorValues, Map<String, StrategyHandler.HoldingStock> holdingStockMap) {
        Set<Integer> lastKlineValues = CriteriaParser.getLastKlineValues(strategy.getCriteria());
        for (Integer n : lastKlineValues) {
            String key = strategy.getDataGranularity().name() + "_" + n;
            LastKLineManufacturer lastKLineManufacturer = lastKLineManufacturerHashMap.get(key);
            if (lastKLineManufacturer == null) {
                lastKLineManufacturer = new LastKLineManufacturer(n, strategy.getDataGranularity());
                lastKLineManufacturerHashMap.put(key, lastKLineManufacturer);
            }

            try {
                lastKLineManufacturer.manufact(kLine, sectorValues);
            } catch (DataInsufficientException e) {
                List<KLine> kLineList = kLineDao.getLastKLineLEDataTime(strategy.getDataGranularity().name(), kLine.getDataTime(), n);

                if (kLineList == null || kLineList.size() < n) {
                    LOGGER.error("lastKline数据不足: " + key);
                    throw new DataInsufficientException("lastKline数据不足: " + key);
                }

                LinkedHashMap<Long, Map<String, BigDecimal>> sectorValuesMap = new LinkedHashMap<>();
                for (KLine l : kLineList) {
                    sectorValuesMap.put(l.getDataTime(), getKLineDataByStrategyWithoutLastKlineData(strategy, l, holdingStockMap));
                }

                lastKLineManufacturer.initAndManufact(sectorValuesMap, sectorValues);

            }
        }
    }


    private void processMa(Set<Sector> sectorSet, Strategy strategy, MAParam maParam, Map<String, BigDecimal> sectorValues) {
        for (Sector sector : sectorSet) {
            if (sector.getType().isMa()) {
                MAManufacturer maManufacturer = getMAManufacturer(sector, strategy.getDataGranularity());
                maManufacturer.manufact(maParam, sectorValues);
            }
        }
    }


    private MAManufacturer getMAManufacturer(Sector sector, DataGranularity dataGranularity) {
        String key = sector.toString() + sector.getType().name() + "_" + sector.getValue() + "_" + dataGranularity.name();
        MAManufacturer maManufacturer = maManufacturerMap.get(key);
        if (maManufacturer == null) {
            maManufacturer = new MAManufacturer(sector, dataGranularity, kLineDao);
            maManufacturerMap.put(key, maManufacturer);
        }
        return maManufacturer;
    }


}
