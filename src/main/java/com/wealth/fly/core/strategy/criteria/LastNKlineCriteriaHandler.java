package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.constants.CommonConstants;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LastNKlineCriteriaHandler implements CriteriaHandler {
    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues) {
        LastNKlineCriteria lastNKlineCriteria = (LastNKlineCriteria) criteria;

        if (lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.FIRST_MATCH)) {
            return isSpecificOneMatch(lastNKlineCriteria, sectorValues, 1);
        }
        if (lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.SECOND_MATCH)) {
            return isSpecificOneMatch(lastNKlineCriteria, sectorValues, 2);
        }

        boolean allMatch = true;
        boolean oneMatch = false;

        for (int i = 1; i <= lastNKlineCriteria.getN(); i++) {
            boolean match = isSpecificOneMatch(lastNKlineCriteria, sectorValues, i);

            if (match) {
                oneMatch = true;
            } else {
                allMatch = false;
            }
        }

        return lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.ALL_MATCH) ? allMatch : oneMatch;
    }

    private Map<String, BigDecimal> getNestValues(Map<String, BigDecimal> sectorValues, int index) {
        Map<String, BigDecimal> nestValues = new HashMap<>();
        for (Sector.SectorType sectorType : Sector.SectorType.values()) {
            BigDecimal value = sectorValues.get(CommonConstants.LAST_KLINE_PARAM + "_" + index + "_" + sectorType.name());
            nestValues.put(sectorType.name(), value);
        }
        return nestValues;
    }

    private boolean isSpecificOneMatch(LastNKlineCriteria lastNKlineCriteria, Map<String, BigDecimal> sectorValues, int index) {
        Map<String, BigDecimal> nestValues = getNestValues(sectorValues, index);
        //            System.out.println("======"+nestValues);
        CriteriaHandler matcherHandler = lastNKlineCriteria.getMatcher().getCriteriaType().getCriteriaHandler();
        return matcherHandler.match(lastNKlineCriteria.getMatcher(), nestValues);
    }


}
