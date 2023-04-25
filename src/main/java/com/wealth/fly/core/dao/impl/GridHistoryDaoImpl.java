package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.dao.GridHistoryDao;
import com.wealth.fly.core.dao.mapper.GridHistoryMapper;
import com.wealth.fly.core.entity.GridHistory;
import org.springframework.stereotype.Repository;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
@Repository
public class GridHistoryDaoImpl extends ServiceImpl<GridHistoryMapper, GridHistory> implements GridHistoryDao {
}
