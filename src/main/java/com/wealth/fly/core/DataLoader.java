package com.wealth.fly.core;

import com.wealth.fly.api.common.HttpClientUtil;
import com.wealth.fly.core.dao.KLineDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class DataLoader {

    @Autowired
    private KLineDao kLineDao;

    @PostConstruct
    public void init() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

            }
        }, 60000L, 60000L);
    }
}
