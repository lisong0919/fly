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
                Date date = new Date();

                for (DataGranularity dataGranularity : DataGranularity.values()) {
                    List<KLine> kLineList = exchanger.getKlineData("BTC-USD-SWAP", dataGranularity);
                    if (kLineList != null) {
                        Collections.sort(kLineList, new Comparator<KLine>() {
                            @Override
                            public int compare(KLine o1, KLine o2) {
                                return o2.getDataTime().compareTo(o1.getDataTime());
                            }
                        });

                        // 根据数据库中最大值，和取回来的最大值做比较，找出需要存储的数据

                    }
                }

            }
        }, 60000L, 60000L);
    }

    public static void main(String[] args) throws Exception {

    }
}
