package com.dreams.logistics.model.dto.truckTrips;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 车次信息表
 * @TableName truck_trips
 */
@Data
public class TruckTripsAddRequest implements Serializable {


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
    private Integer departureTime;

    /**
     * 预计时间（分钟）
     */
    private Integer estimatedTime;


    /**
     * 状态  0：禁用   1：正常
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}