package com.wealth.fly.core.exchanger;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;

import java.util.List;

/**
 * 交易所接口
 */
public interface Exchanger {

    List<KLine> getKlineData(String currency, DataGranularity dataGranularity);

}
