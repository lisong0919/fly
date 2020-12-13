package com.wealth.fly.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MathUtilTest {

    @Test
    public void testAddPercent() {

        Assertions.assertEquals("9715.7601", MathUtil.addPercent(new BigDecimal("9686.7"), "0.003").toPlainString());

        Assertions.assertEquals("9657.6399", MathUtil.addPercent(new BigDecimal("9686.7"), "-0.003").toPlainString());
    }

    @Test
    public void distancePercentInDecimalTest() {
        Assertions.assertEquals("0.003", MathUtil.distancePercentInDecimal(new BigDecimal("9657.6399"), new BigDecimal("9686.7")).toPlainString());
        Assertions.assertEquals("0.003", MathUtil.distancePercentInDecimal(new BigDecimal("9715.7601"), new BigDecimal("9686.7")).toPlainString());
    }

    public static void main(String[] args) {
//        double ema12 = MathUtil.calculateEMA(6767.6, 12, 6863D);
//        double ema26 = MathUtil.calculateEMA(6767.6, 26, 6933.2D);
//        double diff = MathUtil.caculateDIF(ema12, ema26);
//        double dea9 = MathUtil.caculateDEA(-59.2, diff);
//        BigDecimal macd= new BigDecimal(MathUtil.caculateMACD(diff, dea9));

        BigDecimal ema12 = MathUtil.calculateEMANew(new BigDecimal("6767.6"), new BigDecimal("12"), new BigDecimal("6863"));
        BigDecimal ema26 = MathUtil.calculateEMANew(new BigDecimal("6767.6"), new BigDecimal("26"), new BigDecimal("6933.2"));
        BigDecimal diff = MathUtil.caculateDIFNew(ema12, ema26);
        BigDecimal dea9 = MathUtil.caculateDEANew(new BigDecimal("-59.2"), diff);
        BigDecimal macd= MathUtil.caculateMACDNew(diff, dea9);


        System.out.println(">>>>>>ema12" + ema12);
        System.out.println(">>>>>>ema26" + ema26);
        System.out.println(">>>>>>diff" + diff);
        System.out.println(">>>>>>macd" + macd);



//        System.out.println(caculateDEA(43.8D,-0.5D));
    }




}
