package com.wealth.fly.common;

import java.math.BigDecimal;

public class MathUtil {


    /**
     * @param source
     * @param percentInDecimal 加百分之几用小数表示，如百分之三：0.03
     * @return
     */
    public static BigDecimal addPercent(BigDecimal source, String percentInDecimal) {
        BigDecimal addValue = source.multiply(new BigDecimal(percentInDecimal));
        return source.add(addValue);
    }

    public static BigDecimal distancePercentInDecimal(BigDecimal one, BigDecimal compareTo) {
        BigDecimal distance = one.subtract(compareTo).abs();

        return distance.divide(compareTo, 5, BigDecimal.ROUND_DOWN);
    }

    public static double caculateMACD(double DIF, double DEA) {
        return 2 * (DIF - DEA);
    }

    public static double caculateDIF(double EMA12, double EMA26) {

        return EMA12 - EMA26;
    }

    public static double caculateDEA(double DEAYesterday, double DIFToday) {
        return DEAYesterday * 8 / 10 + DIFToday * 2 / 10;
    }

    public static double calculateEMA(double todaysPrice, double numberOfDays, double EMAYesterday) {
        double k = 2 / (numberOfDays + 1);
        return todaysPrice * k + EMAYesterday * (1 - k);
    }
}
