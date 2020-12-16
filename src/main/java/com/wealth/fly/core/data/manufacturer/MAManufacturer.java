package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MAManufacturer implements NewKLineManufacturer {

    private static final MAHandler maHandler=new MAHandler();


    private MAType maType;
    private int maLevel;
    private DataGranularity dataGranularity;
    private Sector.SectorType sectorType;


    public MAManufacturer(MAType maType, Sector sector, DataGranularity dataGranularity){
        this.maType=maType;
        this.dataGranularity=dataGranularity;

        this.sectorType=sector.getType();
        this.maLevel= Integer.valueOf(String.valueOf(sector.getValue()));
    }

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }


    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        BigDecimal maValue= maHandler.push(maType,kLine,maLevel);
        sectorValues.put(sectorType.name()+"_"+maLevel, maValue);
    }
}
