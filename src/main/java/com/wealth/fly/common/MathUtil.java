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
}
