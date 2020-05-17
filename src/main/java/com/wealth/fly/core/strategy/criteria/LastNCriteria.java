package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
public class LastNCriteria implements Criteria{

    private int n;
    private SimpleCriteria matcher;
    private MatchType matchType;

    public LastNCriteria(){
    }

    public LastNCriteria(int n, SimpleCriteria matcher, MatchType matchType) {
        this.n = n;
        this.matcher = matcher;
        this.matchType = matchType;
    }

    public enum MatchType{
        ONE_MATCH,
        ALL_MATCH
    }
}
