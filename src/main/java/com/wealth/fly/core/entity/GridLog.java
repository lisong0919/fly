package com.wealth.fly.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GridLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer type;

    private Integer gridId;

    private Long gridHistoryId;

    private String message;

    private Date createdAt;

}
