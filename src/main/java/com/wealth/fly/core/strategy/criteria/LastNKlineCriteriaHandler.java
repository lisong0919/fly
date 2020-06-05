package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LastNKlineCriteriaHandler implements CriteriaHandler {
    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues, boolean goingLong) {
        LastNKlineCriteria lastNKlineCriteria = (LastNKlineCriteria) criteria;

        CriteriaHandler matcherHandler = lastNKlineCriteria.getMatcher().getCriteriaType().getCriteriaHandler();

        LastNKlineCriteria.MatchType matchType = lastNKlineCriteria.getMatchType();

        boolean allMatch = true;
        boolean oneMatch = false;
        if (LastNKlineCriteria.MatchType.ONE_MATCH.equals(matchType)) {
            for (int i = 1; i <= lastNKlineCriteria.getN(); i++) {
                Map<String, BigDecimal> nestValues = new HashMap<>();
                for (Sector.SectorType sectorType : Sector.SectorType.values()) {
                    BigDecimal value = sectorValues.get(CommonConstants.LAST_KLINE_PARAM + "_" + i + "_" + sectorType.name());
                    nestValues.put(sectorType.name(), value);
                }
                boolean match = matcherHandler.match(lastNKlineCriteria.getMatcher(), nestValues, goingLong);
                if (match) {
                    oneMatch = true;
                } else {
                    allMatch = false;
                }
            }
        }

        return lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.ALL_MATCH) ? allMatch : oneMatch;
    }


}
