package com.wealth.fly.core;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.Exchanger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class DataLoader {

    @Autowired
    private KLineDao kLineDao;
    @Autowired
    private Exchanger exchanger;

    @PostConstruct
    public void init() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //从数据库中取出最后一条数据，用当前时间-最大时间/时间粒度，如果大于零，

                Date date = new Date();

                for (DataGranularity dataGranularity : DataGranularity.values()) {
                    KLine kLine = kLineDao.getLastKLineByGranularity(dataGranularity);

                    String fetchMinTime = null;
                    String fetchMaxTime = null;

                    if (kLine != null) {

                    }

                    List<KLine> kLineList = exchanger.getKlineData("BTC-USD-SWAP", fetchMinTime, fetchMaxTime, dataGranularity);
                    for (KLine kLine1 : kLineList) {
                        kLineDao.insert(kLine);
                    }

                }

            }
        }, 60000L, 60000L);
    }

    public static void main(String[] args) throws Exception {

    }
}
