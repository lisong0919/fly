package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.dao.GridHistoryDao;
import com.wealth.fly.core.dao.mapper.GridHistoryMapper;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.GridHistory;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
@Repository
public class GridHistoryDaoImpl extends ServiceImpl<GridHistoryMapper, GridHistory> implements GridHistoryDao {
    @Override
    public boolean save(GridHistory entity) {
        return retBool(baseMapper.insert(entity));
    }

    @Override
    public boolean updateById(GridHistory entity) {
        return retBool(baseMapper.updateById(entity));
    }


    @Override
    public GridHistory getById(Long id) {
        return baseMapper.selectById(id);
    }
}
