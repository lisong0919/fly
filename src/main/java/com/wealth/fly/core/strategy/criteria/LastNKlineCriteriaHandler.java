package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.constants.CommonConstants;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.MOST_MATCH)) {
            return isMostMatch(lastNKlineCriteria, sectorValues);
        }
        if (lastNKlineCriteria.getMatchType().equals(LastNKlineCriteria.MatchType.RANGE_MATCH)) {
            return isRangeMatch(lastNKlineCriteria, sectorValues);
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

    private boolean isRangeMatch(LastNKlineCriteria lastNKlineCriteria, Map<String, BigDecimal> sectorValues) {
        List<Integer> matchList = new ArrayList<>();
        for (int i = 1; i <= lastNKlineCriteria.getN(); i++) {
            boolean match = isSpecificOneMatch(lastNKlineCriteria, sectorValues, i);
            if (match) {
                matchList.add(i);
            }
        }

        for (int i = lastNKlineCriteria.getRangeStart(); i <= lastNKlineCriteria.getRangeEnd(); i++) {
            Integer key = (Integer) i;
            if (!matchList.contains(key)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMostMatch(LastNKlineCriteria lastNKlineCriteria, Map<String, BigDecimal> sectorValues) {
        int count = 0;
        for (int i = 1; i <= lastNKlineCriteria.getN(); i++) {
            boolean match = isSpecificOneMatch(lastNKlineCriteria, sectorValues, i);
            if (match) {
                count++;
            }
        }
        if(count==0){
            return false;
        }
        return lastNKlineCriteria.getN() / count < 2;
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
