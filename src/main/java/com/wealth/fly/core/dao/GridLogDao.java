package com.wealth.fly.core.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.fly.core.entity.GridLog;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
public interface GridLogDao{

    boolean save(GridLog entity);
}
