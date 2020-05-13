package com.wealth.fly.core.constants;

public enum DataGranularity {
    ONE_MINUTE(60),
    THREE_MINUTES(180),
    FIVE_MINUTES(300),
    FIFTEEN_MINUTES(900),
    THIRTY_MINUTES(1800),
    ONE_HOUR(3600),
    TWO_HOUR(7200),
    FOUR_HOUR(14400),
    SIX_HOUR(21600),
    TWELVE_HOUR(43200),
    ONE_DAY(86400),
    ONE_WEEK(604800);

    DataGranularity(int seconds) {
        this.seconds = seconds;
    }

    private int seconds;

    public int getSeconds() {
        return seconds;
    }

}
