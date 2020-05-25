package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class LastNKlineCriteriaHandler implements CriteriaHandler {
    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues, Map<String, Object> extraParam, boolean goingLong) {
        LastNKlineCriteria lastNKlineCriteria = (LastNKlineCriteria) criteria;
        List<KLine> lineList = (List<KLine>) extraParam.get(CommonConstants.LAST_KLINE_PARAM);


        return false;
    }
}
