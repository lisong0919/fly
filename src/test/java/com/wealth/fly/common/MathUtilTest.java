package com.wealth.fly.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class MathUtilTest {

    @Test
    public void testAddPercent() {

        Assertions.assertEquals("9715.7601",MathUtil.addPercent(new BigDecimal("9686.7"), "0.003").toPlainString());

        Assertions.assertEquals("9657.6399",MathUtil.addPercent(new BigDecimal("9686.7"), "-0.003").toPlainString());
    }

    @Test
    public void distancePercentInDecimalTest(){
        Assertions.assertEquals("0.003",MathUtil.distancePercentInDecimal(new BigDecimal("9657.6399"),new BigDecimal("9686.7")).toPlainString());
        Assertions.assertEquals("0.003",MathUtil.distancePercentInDecimal(new BigDecimal("9715.7601"),new BigDecimal("9686.7")).toPlainString());
    }

}
