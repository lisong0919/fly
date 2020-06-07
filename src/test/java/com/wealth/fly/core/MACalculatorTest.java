package com.wealth.fly.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class MACalculatorTest {


    public static void main(String[] args) {
//        MACalculator maCalculator = new MACalculator("测试", 30);
//        String[] closePirces = new String[]{"9687.5", "9683.0", "9701.6", "9693.5", "9690.3", "9695.7", "9696.3", "9700.1", "9705.7", "9688.5", "9687.0", "9687.3", "9692.9", "9689.1", "9683.7", "9690.2", "9682.6", "9681.2", "9683.0", "9676.3", "9676.8", "9679.4", "9680.4", "9680.7", "9682.9", "9682.0", "9679.5", "9680.1", "9687.0", "9694.9"};
//        for (int i = closePirces.length; i >= 1; i--) {
//            System.out.println(maCalculator.push(i, new BigDecimal(closePirces[i - 1])));
//        }
        BigDecimal price=new BigDecimal("9667");

        String priceStr= price.toPlainString();
        if(priceStr.contains(".")){
            priceStr=priceStr.substring(0,priceStr.indexOf("."));
        }
        System.out.println(price);
        System.out.println(priceStr);

    }

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
