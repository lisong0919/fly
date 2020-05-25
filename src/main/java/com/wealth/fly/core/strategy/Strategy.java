package com.wealth.fly.core.strategy;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.strategy.criteria.Criteria;

public class Strategy {
    private Criteria criteria;
    private int currencyId;
    private DataGranularity dataGranularity;
    private Action action;
    private boolean goingLong;

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public DataGranularity getDataGranularity() {
        return dataGranularity;
    }

    public void setDataGranularity(DataGranularity dataGranularity) {
        this.dataGranularity = dataGranularity;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isGoingLong() {
        return goingLong;
    }

    public void setGoingLong(boolean goingLong) {
        this.goingLong = goingLong;
    }
}
