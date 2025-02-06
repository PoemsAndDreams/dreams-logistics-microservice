package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 车辆计划表
 * @TableName truck_plan
 */
@TableName(value ="truck_plan")
@Data
public class TruckPlan implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 车次id
     */
    private Long transportTripsId;

    /**
     * 司机id
     */
    private String driverIds;

    /**
     * 状态 0：禁用 1：正常，2-完成
     */
    private Integer status;

    /**
     * 计划发车时间
     */
    private Date planDepartureTime;

    /**
     * 计划到达时间
     */
    private Date planArrivalTime;

    /**
     * 计划调度状态，0-待分配，1-已分配，2已调度
     */
    private Integer scheduleStatus;

    /**
     * 
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}