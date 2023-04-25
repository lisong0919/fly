package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.constants.DataGranularity;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

public class KlineDataFetcherTest {


    @Test
    public void getDateFetchRangTest() throws ParseException {
        KlineDataFetcher klineDataFetcher = new KlineDataFetcher();


        Date lastLineTime = DateUtils.parseDate("2020-05-16 14:15:00", "yyyy-MM-dd HH:mm:ss");

        Date now = DateUtils.parseDate("2020-05-16 14:17:00", "yyyy-MM-dd HH:mm:ss");
        Date[] result = klineDataFetcher.getDateFetchRang(lastLineTime, now, DataGranularity.FIVE_MINUTES);
        Assertions.assertTrue(result == null);

        now = DateUtils.parseDate("2020-05-16 14:22:00", "yyyy-MM-dd HH:mm:ss");
        result = klineDataFetcher.getDateFetchRang(lastLineTime, now, DataGranularity.FIVE_MINUTES);
        Assertions.assertTrue(result == null);

        now = DateUtils.parseDate("2020-05-16 14:25:00", "yyyy-MM-dd HH:mm:ss");
        result = klineDataFetcher.getDateFetchRang(lastLineTime, now, DataGranularity.FIVE_MINUTES);
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:15:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[0].getTime());
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:20:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[1].getTime());


        now = DateUtils.parseDate("2020-05-16 14:38:00", "yyyy-MM-dd HH:mm:ss");
        result = klineDataFetcher.getDateFetchRang(lastLineTime, now, DataGranularity.FIVE_MINUTES);
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:15:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[0].getTime());
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:30:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[1].getTime());

        //三分钟粒度
        lastLineTime = DateUtils.parseDate("2020-05-16 14:04:00", "yyyy-MM-dd HH:mm:ss");
        now = DateUtils.parseDate("2020-05-16 14:15:33", "yyyy-MM-dd HH:mm:ss");
        result = klineDataFetcher.getDateFetchRang(lastLineTime, now, DataGranularity.THREE_MINUTES);
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:04:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[0].getTime());
        Assertions.assertEquals(DateUtils.parseDate("2020-05-16 14:10:00", "yyyy-MM-dd HH:mm:ss").getTime(),result[1].getTime());


    }

}
