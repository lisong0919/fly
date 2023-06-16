package com.wealth.fly.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/5/6
 */
@Data
public class Config {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String configKey;

    private String value;

    private Date createdAt;

    private Date updatedAt;
}
