package com.wealth.fly;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 检查k先的完整性
 */
@SpringBootTest
public class CheckKlineCompleteness {

    @Autowired
    private KLineDao kLineDao;

    @Test
    public void check() {
        //时间从晚到早
        List<KLine> kLineList = kLineDao
                .getLastKLineByGranularity(DataGranularity.THIRTY_MINUTES.name(), 100000);

        checkBatch(kLineList);
        System.out.println("all finished........");

    }

    private boolean checkBatch(List<KLine> kLineList){
        KLine prevKline = null;

        for (KLine kLine : kLineList) {
            if (prevKline == null) {
                prevKline = kLine;
                continue;
            }

            int distance = (int) DateUtil.getDistanceMinutes(prevKline.getDataTime(), kLine.getDataTime());
            distance = Math.abs(distance);
            if (distance != 30) {
                System.out.println("==================>not complete between " + kLine.getDataTime() + " and " + prevKline.getDataTime());
                return false;
            }

            prevKline = kLine;
        }
        return true;
    }

    public List<KLine> generateKline(DataGranularity dataGranularity, KLine lastKLine, long startTime, long endTime) {
        if (lastKLine != null) {
            startTime = lastKLine.getDataTime();
        }
        return kLineDao.getLastKLineByDataTime(dataGranularity.name(), startTime, endTime, 200);
    }


}
