package com.dreams.logistics.model.dto.driverJob;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 司机作业单
 * @TableName driver_job
 */
@Data
public class DriverJobQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 起始机构id
     */
    private Long startAgencyId;

    /**
     * 目的机构id
     */
    private Long endAgencyId;

    /**
     * 作业状态，1为待执行（对应 待提货）、2为进行中（对应在途）、3为改派（对应 已交付）、4为已完成（对应 已交付）、5为已作废
     */
    private Integer status;

    /**
     * 司机id
     */
    private Long driverId;

    /**
     * 运输任务id
     */
    private Long transportTaskId;

    /**
     * 提货对接人
     */
    private String startHandover;

    /**
     * 交付对接人
     */
    private String finishHandover;

    /**
     * 计划发车时间
     */
    private Date planDepartureTime;

    /**
     * 实际发车时间
     */
    private Date actualDepartureTime;

    /**
     * 计划到达时间
     */
    private Date planArrivalTime;

    /**
     * 实际到达时间
     */
    private Date actualArrivalTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}