package com.wealth.fly.core.strategy.criteria;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LastNKlineCriteria extends AbstractCriteria implements Criteria {

    private int n;
    private Criteria matcher;
    private MatchType matchType;
    private int rangeStart;
    private int rangeEnd;

    public LastNKlineCriteria() {
    }

    public LastNKlineCriteria(int n, Criteria matcher, MatchType matchType) {
        this.n = n;
        this.matcher = matcher;
        this.matchType = matchType;
    }

    @Override
    public CriteriaType getCriteriaType() {
        return CriteriaType.LAST_N_KLINE;
    }

    public enum MatchType {
        ONE_MATCH,
        FIRST_MATCH,
        SECOND_MATCH,
        ALL_MATCH,
        MOST_MATCH,
        RANGE_MATCH
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public void setRange(int rangeStart, int rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }
}
