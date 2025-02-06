package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 车次信息表
 * @TableName truck_trips
 */
@TableName(value ="truck_trips")
@Data
public class TruckTrips implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 运输路线id
     */
    private Long transportLineId;

    /**
     * 车次名称
     */
    private String name;

    /**
     * 周期，1为天，2为周，3为月
     */
    private Integer period;

    /**
     * 发车时间
     */
    private String departureTime;

    /**
     * 预计时间（分钟）
     */
    private BigDecimal estimatedTime;

    /**
     * 状态  0：禁用   1：正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}