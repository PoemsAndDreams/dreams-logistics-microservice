package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 车辆计划表
 * @TableName truck_plan
 */
@TableName(value ="truck_plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;

    /**
     * 计划到达时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;

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