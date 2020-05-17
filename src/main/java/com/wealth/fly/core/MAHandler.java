package com.wealth.fly.core;


import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.entity.KLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MAHandler {


    private Map<String, MACalculator> maContainer = new ConcurrentHashMap<>();

    public BigDecimal push(MAType maType, KLine kLine, int maLevel) {
        BigDecimal pushValue = null;
        if (maType == MAType.PRICE) {
            pushValue = kLine.getClose();
        } else {
            pushValue = kLine.getCurrencyVolume();
        }
        return getMACalculator(maType, kLine.getCurrencyId(), kLine.getGranularity(), maLevel).push(kLine.getDataTime(), pushValue);
    }

    private MACalculator getMACalculator(MAType maType, Integer currencyId, String dataGranularity, int maLevel) {
        String key = getMAContainerKey(maType, currencyId, dataGranularity, maLevel);
        MACalculator maCalculator = maContainer.get(key);
        if (maCalculator == null) {
            maCalculator = new MACalculator(key, maLevel);
            maContainer.put(key, maCalculator);
        }
        return maCalculator;
    }

    private String getMAContainerKey(MAType maType, Integer currencyId, String dataGranularity, int maLevel) {
        return maType.name() + ";" + currencyId + ";" + dataGranularity + ";" + maLevel;
    }

}
