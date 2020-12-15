package com.wealth.fly.core.reinforce;

import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MADataReinforce implements NewKLineDataReinforce{

    private static final Logger LOGGER= LoggerFactory.getLogger(MADataReinforce.class);

    @Autowired
    private MAHandler maHandler;
    @Autowired
    private KLineDao kLineDao;

    private MAType maType;
    private int maLevel;
    private DataGranularity dataGranularity;
    private Sector.SectorType sectorType;


    public MADataReinforce(MAType maType,Sector sector,DataGranularity dataGranularity){
        this.maType=maType;
        this.dataGranularity=dataGranularity;

        this.sectorType=sector.getType();
        this.maLevel= Integer.valueOf(String.valueOf(sector.getValue()));

    }

    @PostConstruct
    public void init(){
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(dataGranularity.name(), maLevel);
        for (int i = maLevel; i >= 1; i--) {
            maHandler.push(maType, kLineList.get(i - 1), maLevel);
        }
    }

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void reinfore(KLine kLine, Map<String, BigDecimal> sectorValues) {
        BigDecimal maValue= maHandler.push(maType,kLine,maLevel);
        sectorValues.put(sectorType.name()+"_"+maLevel, maValue);
    }
}
