package com.wealth.fly;

import com.wealth.fly.backtest.MABreakBackTester;
import com.wealth.fly.backtest.MACDBackTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BackTestRunner {
//    @Autowired
//    private MABreakBackTester backTester;

    @Autowired
    private MACDBackTester backTester;


    @Test
    public void backTest(){

        backTester.setStartTime(20200521160000L);

//        backTester.setStartTime(20201126000000L);
        backTester.setEndTime(20200621160000L);
        backTester.run();
    }
}
