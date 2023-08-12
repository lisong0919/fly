package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.dao.mapper.KLineMapper;
import com.wealth.fly.core.entity.KLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author : lisong
 * @date : 2023/6/10
 */
@Repository
@Slf4j
public class KLineDaoImpl extends ServiceImpl<KLineMapper, KLine> implements KLineDao {

    @Override
    public int insert(KLine record) {
        return baseMapper.insert(record);
    }

    @Override
    public int updateByPrimaryKey(KLine record) {
        return baseMapper.updateById(record);
    }

    @Override
    public List<KLine> getLastKLineByGranularity(String instId, String dataGranularity, int limit) {
        LambdaQueryWrapper<KLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KLine::getInstId, instId);
        wrapper.eq(KLine::getGranularity, dataGranularity);
        wrapper.orderByDesc(KLine::getDataTime);
        wrapper.last("limit " + limit);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<KLine> getLastKLineGTDataTime(String instId, String dataGranularity, Long dataTime, int limit) {
        LambdaQueryWrapper<KLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KLine::getInstId, instId);
        wrapper.eq(KLine::getGranularity, dataGranularity);
        wrapper.gt(KLine::getDataTime, dataTime);
        wrapper.orderByAsc(KLine::getDataTime);
        wrapper.last("limit " + limit);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public KLine getKlineByDataTime(String instId, String dataGranularity, Long dataTime) {
        LambdaQueryWrapper<KLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KLine::getInstId, instId);
        wrapper.eq(KLine::getGranularity, dataGranularity);
        wrapper.eq(KLine::getDataTime, dataTime);
        List<KLine> list = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        if (list.size() > 1) {
            log.error("发现多条数据:{}", JsonUtil.toJSONString(list));
//            throw new RuntimeException("发现多条数据");
        }
        return list.get(0);
    }
}
