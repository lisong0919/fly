package com.wealth.fly.core.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.fly.core.entity.GridHistory;
import com.wealth.fly.core.entity.GridLog;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public interface GridHistoryDao {
    boolean save(GridHistory entity);

    boolean updateById(GridHistory entity);

    GridHistory getById(Long id);
}
