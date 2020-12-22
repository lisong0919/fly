package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.exception.DataInsufficientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;


public class LastKLineManufacturer implements NewKLineManufacturer {

    private LinkedHashMap<Long,Map<String, BigDecimal>> lastKlineSectorValuesMap = new LinkedHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LastKLineManufacturer.class);

    private int lastKlineMaxNum;
    private DataGranularity dataGranularity;



    public LastKLineManufacturer(int lastKlineMaxNum, DataGranularity dataGranularity) {
        this.lastKlineMaxNum = lastKlineMaxNum;
        this.dataGranularity = dataGranularity;
    }

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    private Long getLastKey(){
        Iterator<Long> it= lastKlineSectorValuesMap.keySet().iterator();
        Long res=null;
        while(it.hasNext()){
            res=it.next();
        }
        return res;
    }

    public void initAndManufact(LinkedHashMap<Long,Map<String, BigDecimal>> sectorValuesMap,Map<String, BigDecimal> sectorValues){
        lastKlineSectorValuesMap.putAll(sectorValuesMap);
        setValues(sectorValues);
    }

    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {

        long preDataTime= DateUtil.getPreDateTime(kLine.getDataTime(),dataGranularity);
        if(lastKlineSectorValuesMap.size() != lastKlineMaxNum || getLastKey()!=preDataTime){
            throw new DataInsufficientException("lastKlineSectorValuesList not ready.");
        }

        //因为下面要边遍历边修改，所以不能add原map
        Map<String, BigDecimal> tempSectorValues = new HashMap<>();
        tempSectorValues.putAll(sectorValues);

        long firstKey=lastKlineSectorValuesMap.keySet().iterator().next();
        lastKlineSectorValuesMap.remove(firstKey);
        lastKlineSectorValuesMap.put(kLine.getDataTime(),tempSectorValues);

        setValues(sectorValues);
    }

    private void setValues(Map<String, BigDecimal> sectorValues){
        for (int i = 0; i < lastKlineSectorValuesMap.size(); i++) {
            Map<String, BigDecimal> lastSectorValues = lastKlineSectorValuesMap.get(i);

            for (String key : lastSectorValues.keySet()) {
                if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                    sectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + (i + 1) + "_" + key, lastSectorValues.get(key));
                }
            }
        }
    }
}
