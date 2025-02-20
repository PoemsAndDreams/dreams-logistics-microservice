package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.dreams.logistics.enums.TransportTaskAssignedStatus;
import com.dreams.logistics.enums.TransportTaskLoadingStatus;
import com.dreams.logistics.enums.TransportTaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 运输任务表
 * @TableName transport_task
 */
@TableName(value ="transport_task")
@Data
public class TransportTask implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 车辆计划id
     */
    private Long truckPlanId;

    /**
     * 车次id
     */
    private Long transportTripsId;

    /**
     * 起始机构id
     */
    private Long startAgencyId;

    /**
     * 目的机构id
     */
    private Long endAgencyId;

    /**
     * 任务状态，1为待执行（对应 未发车）、2为进行中（对应在途）、3为待确认（保留状态）、4为已完成（对应 已交付）、5为已取消
     */
    private TransportTaskStatus status;

    /**
     * 任务分配状态(1未分配2已分配3待人工分配)
     */
    private TransportTaskAssignedStatus assignedStatus;

    /**
     * 满载状态(1.半载2.满载3.空载)
     */
    private TransportTaskLoadingStatus loadingStatus;

    /**
     * 车辆id
     */
    private String truckId;

    /**
     * 提货凭证
     */
    private String cargoPickUpPicture;

    /**
     * 货物照片
     */
    private String cargoPicture;

    /**
     * 运回单凭证
     */
    private String transportCertificate;

    /**
     * 交付货物照片
     */
    private String deliverPicture;

    /**
     * 提货纬度值
     */
    private String deliveryLatitude;

    /**
     * 提货经度值
     */
    private String deliveryLongitude;

    /**
     * 交付纬度值
     */
    private String deliverLatitude;

    /**
     * 交付经度值
     */
    private String deliverLongitude;

    /**
     * 计划发车时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;

    /**
     * 实际发车时间
     */
    private LocalDateTime actualDepartureTime;

    /**
     * 计划到达时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;

    /**
     * 实际到达时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrivalTime;

    /**
     * 备注
     */
    private String mark;

    /**
     * 距离，单位：米
     */
    private Double distance;

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