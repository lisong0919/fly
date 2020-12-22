package com.wealth.fly.core.strategy.criteria;


import java.util.HashSet;
import java.util.Set;

public class CriteriaParser {


    public static Set<Sector> parseSectorType(Criteria criteria) {
        Set<Sector> result = new HashSet<>();
        if (criteria instanceof SimpleCriteria) {
            SimpleCriteria simpleCriteria = (SimpleCriteria) criteria;
            result.add(simpleCriteria.getSource());
            result.add(simpleCriteria.getTarget());
        } else if (criteria instanceof CompoundCriteria) {
            CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;
            for (Criteria c : compoundCriteria.getCriteriaList()) {
                result.addAll(parseSectorType(c));
            }
        } else if (criteria instanceof LastNKlineCriteria) {
            LastNKlineCriteria lastNKlineCriteria = (LastNKlineCriteria) criteria;
            result.addAll(parseSectorType(lastNKlineCriteria.getMatcher()));
        }
        return result;
    }

    public static Set<Integer> getLastKlineValues(Criteria criteria) {
        Set<Integer> lastKlineSet = new HashSet<>();
        if (criteria instanceof CompoundCriteria) {
            CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;
            for (Criteria c : compoundCriteria.getCriteriaList()) {
                lastKlineSet.addAll(getLastKlineValues(c));
            }
        } else if (criteria instanceof LastNKlineCriteria) {
            LastNKlineCriteria lastNKlineCriteria = (LastNKlineCriteria) criteria;

            lastKlineSet.add(lastNKlineCriteria.getN());
        }
        return lastKlineSet;
    }

}
