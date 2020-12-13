package com.wealth.fly.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {

    public static BigDecimal addPercent(BigDecimal source, BigDecimal percentInDecimal) {
        BigDecimal addValue = source.multiply(percentInDecimal);
        return source.add(addValue);
    }

    /**
     * @param source
     * @param percentInDecimal 加百分之几用小数表示，如百分之三：0.03
     * @return
     */
    public static BigDecimal addPercent(BigDecimal source, String percentInDecimal) {
        return  addPercent(source,new BigDecimal(percentInDecimal));
    }

    public static BigDecimal distancePercentInDecimal(BigDecimal one, BigDecimal compareTo) {
        BigDecimal distance = one.subtract(compareTo).abs();

        return distance.divide(compareTo, 5, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal caculateMACDNew(BigDecimal DIF, BigDecimal DEA) {
        return DIF.subtract(DEA).multiply(new BigDecimal(2));
    }

    public static double caculateMACD(double DIF, double DEA) {
        return 2 * (DIF - DEA);
    }

    public static BigDecimal caculateDIFNew(BigDecimal EMA12, BigDecimal EMA26) {
        return EMA12.subtract(EMA26);
    }

    public static double caculateDIF(double EMA12, double EMA26) {

        return EMA12 - EMA26;
    }

    public static BigDecimal caculateDEANew(BigDecimal DEAYesterday, BigDecimal DIFToday) {
        BigDecimal p1=DEAYesterday.multiply(new BigDecimal(8)).divide(new BigDecimal(10));
        BigDecimal p2=DIFToday.multiply(new BigDecimal(2)).divide(new BigDecimal(10));
        return p1.add(p2);
    }

    public static double caculateDEA(double DEAYesterday, double DIFToday) {
        return DEAYesterday * 8 / 10 + DIFToday * 2 / 10;
    }

    public static BigDecimal calculateEMANew(BigDecimal todaysPrice, BigDecimal numberOfDays, BigDecimal EMAYesterday) {
        BigDecimal k = new BigDecimal(2).divide(numberOfDays.add(new BigDecimal(1)),RoundingMode.DOWN);
        return todaysPrice.multiply(k).add(EMAYesterday.multiply(new BigDecimal(1).subtract(k)));
    }

    public static double calculateEMA(double todaysPrice, double numberOfDays, double EMAYesterday) {
        double k = 2 / (numberOfDays + 1);
        return todaysPrice * k + EMAYesterday * (1 - k);
    }
}
