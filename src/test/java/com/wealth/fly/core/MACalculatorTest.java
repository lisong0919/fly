package com.wealth.fly.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class MACalculatorTest {
    @Test
    public void pushTest() {
        //push前两个元素，并校验平均值是null
        MACalculator maCalculator = new MACalculator("3日均线", 3, 1);
        maCalculator.push("3-1", new BigDecimal("5.323"));
        Assertions.assertNull(maCalculator.push("3-2", new BigDecimal("3.82")));

        //push第三个元素并校验平均值
        BigDecimal average = maCalculator.push("3-3", new BigDecimal("22.55"));
        Assertions.assertEquals("10.6", average.toString());//五入


        //校验计算器中的三个元素是否正确
        MACalculator.Node first = maCalculator.getFirst();
        Assertions.assertEquals("5.323", first.getValue().toString());
        MACalculator.Node second = first.getNext();
        Assertions.assertEquals("3.82", second.getValue().toString());
        MACalculator.Node third = second.getNext();
        Assertions.assertEquals("22.55", third.getValue().toString());


        //push第四个元素并校验，push挤掉头部后的平均值
        average = maCalculator.push("3-4", new BigDecimal("10.01"));
        Assertions.assertEquals("12.1", average.toString());//四舍

        //校验计算器中的三个元素是否正确
        first = maCalculator.getFirst();
        Assertions.assertEquals("3.82", first.getValue().toString());
        second = first.getNext();
        Assertions.assertEquals("22.55", second.getValue().toString());
        third = second.getNext();
        Assertions.assertEquals("10.01", third.getValue().toString());
    }

    @Test
    public void relaceLastTest() {
        // push第一个元素，并校验relaceLast返回null
        MACalculator maCalculator = new MACalculator("3日均线", 3, 1);
        maCalculator.push("3-1", new BigDecimal("5.323"));
        Assertions.assertNull(maCalculator.replaceLast("3-2", new BigDecimal("3.82")));//这次relace不会成功

        //push第二个元素和第三个元素并校验平均值
        maCalculator.push("3-2", new BigDecimal("3.82"));
        BigDecimal average = maCalculator.push("3-3", new BigDecimal("22.55"));
        Assertions.assertEquals("10.6", average.toString());

        //替换第三个元素
        average = maCalculator.replaceLast("3-3", new BigDecimal("32.6"));

        //校验替换后计算器中的三个元素是否正确
        MACalculator.Node first = maCalculator.getFirst();
        Assertions.assertEquals("5.323", first.getValue().toString());
        MACalculator.Node second = first.getNext();
        Assertions.assertEquals("3.82", second.getValue().toString());
        MACalculator.Node third = second.getNext();
        Assertions.assertEquals("32.6", third.getValue().toString());

        //验证替换后的平均值
        Assertions.assertEquals("13.9", average.toString());
    }


}
