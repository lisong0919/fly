package com.wealth.fly.core.dao;

import com.wealth.fly.core.entity.KLine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface KLineDao {

    int insert(KLine record);

    int updateByPrimaryKey(KLine record);

    List<KLine> getLastKLineByGranularity(@Param("instId") String instId, @Param("granularity") String dataGranularity, @Param("limit") int limit);

    List<KLine> getLastKLineGTDataTime(@Param("instId") String instId, @Param("granularity") String dataGranularity, @Param("dataTime") Long dataTime, @Param("limit") int limit);

    KLine getKlineByDataTime(@Param("instId") String instId, @Param("granularity") String dataGranularity, @Param("dataTime") Long dataTime);
}