package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class LastKLineManufacturer implements NewKLineManufacturer {

    private LinkedList<Map<String, BigDecimal>> lastKlineSectorValuesList = new LinkedList<>();
    private static final Logger LOGGER= LoggerFactory.getLogger(LastKLineManufacturer.class);

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        synchronized (lastKlineSectorValuesList) {
            if (lastKlineSectorValuesList.size() == CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                lastKlineSectorValuesList.removeFirst();
            }
            //因为下面要边遍历边修改，所以不能add原map
            Map<String, BigDecimal> tempSectorValues=new HashMap<>();
            tempSectorValues.putAll(sectorValues);
            lastKlineSectorValuesList.add(tempSectorValues);

            if (lastKlineSectorValuesList.size() < CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                LOGGER.info("lastKlineSectorValuesList not ready.");
                return;
            }
            for (int i = 0; i < lastKlineSectorValuesList.size(); i++) {
                Map<String, BigDecimal> lastSectorValues = lastKlineSectorValuesList.get(i);

                for (String key : lastSectorValues.keySet()) {
                    if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                        sectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + (i + 1) + "_" + key, lastSectorValues.get(key));
                    }
                }
            }
        }
    }
}
