package com.wealth.fly.core.exchanger;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;

import java.util.Date;
import java.util.List;


public interface Exchanger {

    List<KLine> getKlineData(String currency, Date startTime, Date endTime, DataGranularity dataGranularity);

}
