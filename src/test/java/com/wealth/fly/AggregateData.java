package com.wealth.fly;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
@SpringBootTest
public class AggregateData {
    @Autowired
    private KLineDao kLineDao;

    @Test
    public void aggregate() {
        int period = 2;
        long min = 20140203000000L;

        while (true) {
            List<KLine> kLineList = kLineDao
                    .getLastKLineGTDataTime(DataGranularity.ONE_HOUR.name(), min, period);
            if (kLineList == null || kLineList.size() < period) {
                System.out.println(">>>>>>>>> no more data");
                break;
            }

            aggregateNewLine(kLineList);

            Date date = DateUtils.addHours(DateUtil.parseStandardTime(min), period);
            min = Long.parseLong(DateUtil.formatToStandardTime(date.getTime()));
        }
        System.out.println("all finished.....");

    }

    private void aggregateNewLine(List<KLine> kLineList) {
        KLine newLine = new KLine();
        newLine.setOpen(kLineList.get(0).getOpen());
        newLine.setClose(kLineList.get(kLineList.size() - 1).getClose());
        newLine.setDataTime(kLineList.get(0).getDataTime());

        newLine.setGranularity(DataGranularity.TWO_HOUR.name());
        newLine.setEma26(new BigDecimal(1));
        newLine.setEma12(new BigDecimal(1));
        newLine.setDea9(new BigDecimal(1));
        newLine.setCreateTime(Calendar.getInstance(Locale.CHINA).getTime());
        newLine.setCurrencyId(1);

        newLine.setHigh(new BigDecimal(0));
        newLine.setLow(kLineList.get(0).getLow());
        newLine.setVolume(new BigDecimal(0));
        newLine.setCurrencyVolume(new BigDecimal(0));

        for (KLine line : kLineList) {
            newLine.setVolume(newLine.getVolume().add(line.getVolume()));
            newLine.setCurrencyVolume(newLine.getCurrencyVolume().add(line.getCurrencyVolume()));

            if (line.getHigh().compareTo(newLine.getHigh()) > 0) {
                newLine.setHigh(line.getHigh());
            }
            if (line.getLow().compareTo(newLine.getLow()) < 0) {
                newLine.setLow(line.getLow());
            }
        }
        kLineDao.insert(newLine);
    }


}
