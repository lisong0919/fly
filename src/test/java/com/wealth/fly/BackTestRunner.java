package com.wealth.fly;

import com.wealth.fly.backtest.MABreakBackTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BackTestRunner {
    @Autowired
    private MABreakBackTester backTester;

    @Test
    public void backTest(){
        backTester.run();
    }
}
