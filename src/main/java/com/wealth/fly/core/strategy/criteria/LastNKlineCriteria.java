package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
public class LastNKlineCriteria extends AbstractCriteria implements Criteria{

    private int n;
    private SimpleCriteria matcher;
    private MatchType matchType;

    public LastNKlineCriteria(){
    }

    public LastNKlineCriteria(int n, SimpleCriteria matcher, MatchType matchType) {
        this.n = n;
        this.matcher = matcher;
        this.matchType = matchType;
    }

    @Override
    public CriteriaType getCriteriaType() {
        return CriteriaType.LAST_N_KLINE;
    }

    public enum MatchType{
        ONE_MATCH,
        ALL_MATCH
    }
}
