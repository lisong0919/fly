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
//        List<Double> list = new ArrayList<>();
//        list.add(9232.6D);
//        list.add(9231.4D);
//        list.add(9221.4D);
//        list.add(9212.2D);
//        list.add(9220D);
//        list.add(9224.7D);
//        list.add(9227.2D);
//        System.out.println">>>>>>>" + getEXPMA(list, 7));
        // 6-25
        System.out.println(">>>>>>" + MathUtil.calculateEMA(9249.7D, 12, 9460.8D));
        System.out.println(">>>>>>" + MathUtil.calculateEMA(9249.7D, 26, 9443.2D));

//        System.out.println(caculateMACD(-0.5D, 35));
        System.out.println(MathUtil.caculateDIF(9428.323076923076D,9428.866666666667D));
//        System.out.println(caculateDEA(43.8D,-0.5D));
    }




}
