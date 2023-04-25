package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.dao.mapper.GridLogMapper;
import com.wealth.fly.core.entity.GridLog;
import org.springframework.stereotype.Repository;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
@Repository
public class GridLogDaoImpl extends ServiceImpl<GridLogMapper, GridLog> implements GridLogDao {
}