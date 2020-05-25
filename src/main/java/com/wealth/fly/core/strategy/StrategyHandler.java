package com.wealth.fly.core.strategy;

import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.entity.KLine;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class StrategyHandler implements KLineListener {
    private List<Strategy> strategyList;

    @PostConstruct
    public void initStrategy(){

    }

    @Override
    public void onNewKLine(KLine kLine) {


    }

}
